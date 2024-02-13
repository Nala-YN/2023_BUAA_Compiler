package Optimize;

import LlvmIr.BasicBlock;
import LlvmIr.Global.Function;
import LlvmIr.IRBuilder;
import LlvmIr.Instr;
import LlvmIr.Instruction.Alloca;
import LlvmIr.Instruction.Load;
import LlvmIr.Instruction.Phi;
import LlvmIr.Instruction.Store;
import LlvmIr.Module;
import LlvmIr.Type.LlvmType;
import LlvmIr.Undef;
import LlvmIr.User;
import LlvmIr.Value;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Stack;

public class Mem2Reg {
    private static Alloca curAlloca;
    private static ArrayList<Instr> defInstrs;
    private static ArrayList<Instr> useInstrs;
    private static ArrayList<BasicBlock> defBlocks;
    private static ArrayList<BasicBlock> useBlocks;
    private static Stack<Value> defStack;

    public static void optimize(Module module) {
        for (Function func : module.getFunctions()) {
            for (BasicBlock block : func.getBlocks()) {
                ArrayList<Instr> instrs=new ArrayList<>(block.getInstrs());
                for (Instr instr : instrs) {
                    if (instr instanceof Alloca && ((Alloca) instr).getPointedType() == LlvmType.Int32) {
                        curAlloca = (Alloca) instr;
                        init();
                        insertPhi();
                        rename(func.getBlocks().get(0));
                    }
                }
            }
        }
    }

    public static void init() {
        useBlocks = new ArrayList<>();
        useInstrs = new ArrayList<>();
        defBlocks = new ArrayList<>();
        defInstrs = new ArrayList<>();
        defStack = new Stack<>();
        for (User user : curAlloca.getUsers()) {
            Instr instr = (Instr) user;
            if (instr instanceof Load && !instr.getParentBlock().isDeleted()) {
                useInstrs.add(instr);
                if (!useBlocks.contains(instr.getParentBlock())) {
                    useBlocks.add(instr.getParentBlock());
                }
            }
            if (instr instanceof Store && !instr.getParentBlock().isDeleted()) {
                defInstrs.add(instr);
                if (!defBlocks.contains(instr.getParentBlock())) {
                    defBlocks.add(instr.getParentBlock());
                }
            }
        }
    }

    public static void insertPhi() {
        HashSet<BasicBlock> f = new HashSet<>();
        ArrayList<BasicBlock> w = new ArrayList<>(defBlocks);
        while (!w.isEmpty()) {
            BasicBlock x = w.get(0);
            w.remove(0);
            for (BasicBlock y : x.getDF()) {
                if (!f.contains(y)) {
                    insertAtBegin(y);
                    f.add(y);
                    if (!defBlocks.contains(y)) {
                        w.add(y);
                    }
                }
            }
        }
    }

    public static void insertAtBegin(BasicBlock block) {
        String name = IRBuilder.tempName + block.getParentFunc().getVarId();
        Phi phiInstr = new Phi(name, block, new ArrayList<>(block.getParent()));
        block.getInstrs().add(0, phiInstr);
        useInstrs.add(phiInstr);
        defInstrs.add(phiInstr);
    }

    public static void rename(BasicBlock block) {
        Iterator<Instr> it = block.getInstrs().iterator();
        int pushCnt = 0;
        while (it.hasNext()) {
            Instr instr = it.next();
            if (instr == curAlloca) {
                instr.removeOperands();
                it.remove();
            } else if (instr instanceof Load && useInstrs.contains(instr)) {
                Value newValue = defStack.empty() ? new Undef() : defStack.peek();
                instr.modifyValueForUsers(newValue);
                instr.removeOperands();
                it.remove();
            } else if (instr instanceof Store && defInstrs.contains(instr)) {
                defStack.push(((Store) instr).getFrom());
                instr.removeOperands();
                pushCnt++;
                it.remove();
            } else if (instr instanceof Phi && defInstrs.contains(instr)) {
                pushCnt++;
                defStack.push(instr);
            }
        }
        for (BasicBlock child : block.getChild()) {
            Instr firstInstr = child.getInstrs().get(0);
            if (firstInstr instanceof Phi && useInstrs.contains(firstInstr)) {
                Value value = defStack.empty() ? new Undef() : defStack.peek();
                ((Phi) firstInstr).addValue(block, value);
            }
        }
        for (BasicBlock imdommed : block.getImdom()) {
            rename(imdommed);
        }
        for (int i = 1; i <= pushCnt; i++) {
            defStack.pop();
        }
    }
}
