package Optimize;

import LlvmIr.BasicBlock;
import LlvmIr.Global.Function;
import LlvmIr.Instr;
import LlvmIr.Instruction.Alloca;
import LlvmIr.Instruction.Call;
import LlvmIr.Instruction.GetPtr;
import LlvmIr.Instruction.Icmp;
import LlvmIr.Instruction.Phi;
import LlvmIr.Module;
import LlvmIr.User;
import LlvmIr.Value;
import ToMips.Register;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
/*在寄存器分配中，由于我们的编译器没有真正与操作系统交互，因此可以将$k0和$k1作为临时寄存器，$s*和$t*的所有寄存器都作为全局寄存器。
我采用的分配算法是引用计数和线性扫描相结合的寄存器分配算法。具体来说，我们先做活跃变量分析，得到每个基本块的in和out集合，按照支配树
的顺序从根节点开始对基本块进行前序遍历，初始化var2reg映射存储每个变量被分配到的寄存器，reg2var映射存储每个寄存器当前已经被分配给的
变量。如果遇到全局变量不够的情况，考虑变量被引用的次数（循环内变量增加权值），将寄存器尽可能分配给引用次数较多的变量。在块内分配寄
存器时，若变量不在该块的out集中，则说明其出块后不再活跃，可以记录该变量在块中最后被引用的位置，在该位置后释放其所占用的寄存器。在为
当前块分配完寄存器后，尝试为后继块分配寄存器，若reg2var中的变量不在后继块的in集中，则说明其在后继块中不再活跃，可以将其占用的寄存
器释放。*/
public class RegAlloc {
    private static HashMap<Value, Register> var2reg;
    private static HashMap<Register, Value> reg2var;
    private static HashMap<Value, Integer> useCnt;
    private static Function curFunc;
    private static ArrayList<Register> regSet;

    public static void allocReg(Module module) {
        regSet = new ArrayList<>();
        for (int i = Register.t0.ordinal(); i <= Register.s7.ordinal(); i++) {
            regSet.add(Register.values()[i]);
        }
        for (Function func : module.getFunctions()) {
            var2reg = new HashMap<>();
            reg2var = new HashMap<>();
            useCnt = new HashMap<>();
            curFunc = func;
            initUseCnt();
            visitBlock(func.getBlocks().get(0));
            for (BasicBlock block : func.getBlocks()) {
                for (Instr instr : block.getInstrs()) {
                    if (instr instanceof Call call) {
                        HashSet<Register> regSet = new HashSet<>();
                        for (Value value : block.getOutSet()) {
                            if (var2reg.containsKey(value)) {
                                regSet.add(var2reg.get(value));
                            }
                        }
                        for (int i = block.getInstrs().indexOf(call) + 1; i <= block.getInstrs().size() - 1; i++) {
                            for (Value value : block.getInstrs().get(i).getOperands()) {
                                if (var2reg.containsKey(value)) {
                                    regSet.add(var2reg.get(value));
                                }
                            }
                        }
                        call.setActiveReg(regSet);
                    }
                }
            }
            for (Value value : var2reg.keySet()) {
                System.out.println(value.getName() + "->" + var2reg.get(value));
            }
            curFunc.setVar2reg(var2reg);
        }
    }

    public static void initUseCnt() {
        for (BasicBlock block : curFunc.getBlocks()) {
            for (Instr instr : block.getInstrs()) {
                for (Value value : instr.getOperands()) {
                    if (useCnt.containsKey(value)) {
                        useCnt.put(value, useCnt.get(value) + 1+10000*block.getLoopDepth());
                    } else {
                        useCnt.put(value, 1+10000*block.getLoopDepth());
                    }
                }
                if (instr.hasLVal()) {
                    if (useCnt.containsKey(instr)) {
                        useCnt.put(instr, useCnt.get(instr) + 1+10000*block.getLoopDepth());
                    } else {
                        useCnt.put(instr, 1+10000*block.getLoopDepth());
                    }
                }
            }
        }
    }

    public static void visitBlock(BasicBlock entry) {
        ArrayList<Instr> instrs = entry.getInstrs();
        HashSet<Value> localDefed = new HashSet<>();
        HashMap<Value, Instr> lastUse = new HashMap<>();
        HashSet<Value> neverUsed = new HashSet<>();
        for (Instr instr : instrs) {
            for (Value value : instr.getOperands()) {
                lastUse.put(value, instr);
            }
        }
        for (Instr instr : instrs) {
            if (!(instr instanceof Phi)) {
                for (Value operand : instr.getOperands()) {
                    if (lastUse.get(operand) == instr && !entry.getOutSet().contains(operand) && var2reg.containsKey(operand)) {
                        reg2var.remove(var2reg.get(operand));
                        neverUsed.add(operand);
                    }
                }
            }
            if (instr.hasLVal() && !((instr instanceof Alloca) && instr.getLlvmType().isArray())) {
                localDefed.add(instr);
                tryAllocReg(instr);
            }
        }
        for (BasicBlock block : entry.getImdom()) {
            HashMap<Register, Value> curChildNeverUse = new HashMap<>();
            for (Register reg : reg2var.keySet()) {
                if (!block.getInSet().contains(reg2var.get(reg))) {
                    curChildNeverUse.put(reg, reg2var.get(reg));
                }
            }
            for (Register reg : curChildNeverUse.keySet()) {
                reg2var.remove(reg);
            }
            visitBlock(block);
            for (Register reg : curChildNeverUse.keySet()) {
                reg2var.put(reg, curChildNeverUse.get(reg));
            }
        }
        for (Value value : localDefed) {
            if (var2reg.containsKey(value)) {
                reg2var.remove(var2reg.get(value));
            }
        }
        for (Value value : neverUsed) {
            if (!localDefed.contains(value) && var2reg.containsKey(value)) {
                reg2var.put(var2reg.get(value), value);
            }
        }
    }

    public static void tryAllocReg(Value value) {
        Register allocReg = null;
        for (Register reg : regSet) {
            if (!reg2var.containsKey(reg)) {
                allocReg = reg;
                break;
            }
        }
        if (allocReg == null) {
            int mincnt = Integer.MAX_VALUE;
            for (Register reg : regSet) {
                if (mincnt > useCnt.get(reg2var.get(reg))) {
                    mincnt = useCnt.get(reg2var.get(reg));
                    allocReg = reg;
                }
            }
            if (useCnt.get(value)< mincnt) {
                return;
            }
            if (reg2var.containsKey(allocReg)) {
                var2reg.remove(reg2var.get(allocReg));
            }
        }
        var2reg.put(value, allocReg);
        reg2var.put(allocReg, value);
    }
}
