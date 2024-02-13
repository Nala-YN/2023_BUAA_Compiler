package Optimize;

import LlvmIr.BasicBlock;
import LlvmIr.Global.Function;
import LlvmIr.Instr;
import LlvmIr.Instruction.Alu;
import LlvmIr.Instruction.Call;
import LlvmIr.Instruction.GetPtr;
import LlvmIr.Instruction.Icmp;
import LlvmIr.Instruction.Load;
import LlvmIr.Instruction.OffsetLoad;
import LlvmIr.Instruction.Phi;
import LlvmIr.Instruction.Zext;
import LlvmIr.Module;
import LlvmIr.Type.LlvmType;
import LlvmIr.Type.PointerType;
import LlvmIr.User;
import LlvmIr.Value;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashSet;

public class GCM {
    public static void moveInstrs(Module module) {
        for (Function func : module.getFunctions()) {
            curFunc = func;
            moveInstrsForFunc(func);
        }
    }

    private static HashSet<Instr> visited = new HashSet<>();
    private static Function curFunc;

    public static void moveInstrsForFunc(Function func) {
        visited.clear();
        curFunc = func;
        ArrayList<BasicBlock> postOrder = func.getPostOrderForIdomTree();
        Collections.reverse(postOrder);
        ArrayList<Instr> instrs = new ArrayList<>();
        for (BasicBlock block : postOrder) {
            instrs.addAll(block.getInstrs());
        }
        for (Instr instr : instrs) {
            scheduleEarly(instr);
        }
        visited.clear();
        Collections.reverse(instrs);
        for (Instr instr : instrs) {
            scheduleLate(instr);
        }
    }

    public static void scheduleEarly(Instr instr) {
        if (!isMovable(instr) || visited.contains(instr)) {
            return;
        }
        visited.add(instr);
        BasicBlock firstBlock = curFunc.getBlocks().get(0);
        instr.getParentBlock().getInstrs().remove(instr);
        firstBlock.getInstrs().add(firstBlock.getInstrs().size() - 1, instr);
        instr.setParentBlock(firstBlock);
        for (Value operand : instr.getOperands()) {
            if (operand instanceof Instr) {
                Instr operandInstr = (Instr) operand;
                scheduleEarly(operandInstr);
                if (instr.getParentBlock().getImdomDepth() < operandInstr.getParentBlock().getImdomDepth()) {
                    instr.getParentBlock().getInstrs().remove(instr);
                    BasicBlock operandBlock = operandInstr.getParentBlock();
                    operandBlock.getInstrs().add(operandBlock.getInstrs().size() - 1, instr);
                    instr.setParentBlock(operandBlock);
                }
            }
        }
    }

    public static BasicBlock findLca(BasicBlock block1, BasicBlock block2) {
        if (block1 == null) {
            return block2;
        }
        while (block1.getImdomDepth() < block2.getImdomDepth()) {
            block2 = block2.getImdommedBy();
        }
        while (block2.getImdomDepth() < block1.getImdomDepth()) {
            block1 = block1.getImdommedBy();
        }
        while (!(block1 == block2)) {
            block1 = block1.getImdommedBy();
            block2 = block2.getImdommedBy();
        }
        return block1;
    }

    public static void scheduleLate(Instr instr) {
        if(instr.getName()!=null&&instr.getName().equals("%t119")){
            int o=1;
        }
        if (!isMovable(instr) || visited.contains(instr)) {
            return;
        }
        visited.add(instr);
        // lca表示instr的user的共同祖先
        BasicBlock lca = null;
        //这里是为了找到user的共同祖先
        for (User user : instr.getUsers()) {
            if (user instanceof Instr) {
                Instr userInstr = (Instr) user;
                scheduleLate(userInstr);
                BasicBlock userBlock;
                if (user instanceof Phi) {
                    for (int i = 0; i <= user.getOperands().size() - 1; i++) {
                        Value value = user.getOperands().get(i);
                        if (value == instr) {
                            userBlock = ((Phi) user).getBlocks().get(i);
                            lca = findLca(lca, userBlock);
                        }
                    }
                } else {
                    userBlock = userInstr.getParentBlock();
                    lca = findLca(lca, userBlock);
                }
            }
        }
        if(instr.getName()!=null&&instr.getName().equals("%t119")){
            int o=1;
        }
        BasicBlock posBlock = lca;
        while (lca != instr.getParentBlock()) {
            if(lca==null){
                int o=1;
            }
            lca = lca.getImdommedBy();
            if (lca.getLoopDepth() < posBlock.getLoopDepth()) {
                posBlock = lca;
            }
        }
        instr.getParentBlock().getInstrs().remove(instr);
        posBlock.getInstrs().add(posBlock.getInstrs().size() - 1, instr);
        instr.setParentBlock(posBlock);
        for (Instr inst : posBlock.getInstrs()) {
            if (inst != instr && !(inst instanceof Phi) && inst.getOperands().contains(instr)) {
                posBlock.getInstrs().remove(instr);
                posBlock.getInstrs().add(posBlock.getInstrs().indexOf(inst), instr);
                break;
            }
        }
    }

    public static boolean isMovable(Instr instr) {
        if (instr instanceof Alu ||
                instr instanceof GetPtr ||
                instr instanceof Icmp||
                instr instanceof Zext) {
            return true;
        }
        if (instr instanceof Call) {
            Call call = (Call) instr;
            Function callee = ((Function) (call.getOperands().get(0)));
            if (callee.isHasSideEffects() || instr.getParentBlock().getParentFunc() == callee) {
                return false;
            }
            if (call.getUsers().isEmpty()) {
                return false;
            }
            for (Value user : call.getUsers()) {
                if (user instanceof GetPtr || user instanceof Load ||user instanceof OffsetLoad|| user.getLlvmType() instanceof PointerType) {
                    return false;
                }
            }
            return true;
        }
        return false;
    }
}
