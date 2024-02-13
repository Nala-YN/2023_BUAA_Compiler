package Optimize;

import LlvmIr.BasicBlock;
import LlvmIr.Global.Function;
import LlvmIr.IRBuilder;
import LlvmIr.Instr;
import LlvmIr.Instruction.Branch;
import LlvmIr.Instruction.Jmp;
import LlvmIr.Instruction.Phi;
import LlvmIr.Instruction.Ret;
import LlvmIr.Module;
import LlvmIr.Value;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;

public class BlockSimplify {
    private static HashSet<BasicBlock> reachedBlocks;

    public static void rearrange(Module module) {
        for (Function func : module.getFunctions()) {
                BasicBlock block0 = func.getBlocks().get(0);
                ArrayList<BasicBlock> blocks = new ArrayList<>(func.getBlocks());
                for (BasicBlock block : blocks) {
                    Instr last = block.getInstrs().get(block.getInstrs().size() - 1);
                    if(last instanceof Ret)continue;
                    BasicBlock to;
                    if (last instanceof Jmp) {
                        to = ((Jmp) last).getToBlock();
                    }
                    else{
                        to=((Branch)last).getElseBlock();
                    }
                    if(to==block)continue;
                    func.getBlocks().remove(block);
                    func.getBlocks().add(func.getBlocks().indexOf(to), block);
                }
                if (func.getBlocks().get(0) != block0) {
                    BasicBlock newblock = new BasicBlock(IRBuilder.blockName + func.getBlockId(), func);
                    Jmp jmp = new Jmp(block0, newblock);
                    newblock.addInstr(jmp);
                    func.getBlocks().add(0, newblock);
                }
                if(func.getName().equals("@main")){
                    ArrayList<Instr> instrs=func.getBlocks().get(func.getBlocks().size()-1).getInstrs();
                    boolean flag=instrs.get(instrs.size()-1) instanceof Ret;
                    if(flag&&module.getFunctions().size()==1){
                        instrs.remove(instrs.size()-1);
                    }
                }
        }
    }

    public static void simplify(Module module) {
        ArrayList<Function> funcs = module.getFunctions();
        for (Function func : funcs) {
            for (BasicBlock block : func.getBlocks()) {
                deleteDeadInstr(block);
            }
            BasicBlock entry = func.getBlocks().get(0);
            reachedBlocks = new HashSet<>();
            findReachable(entry);
            Iterator<BasicBlock> it = func.getBlocks().iterator();
            while (it.hasNext()) {
                BasicBlock block = it.next();
                if (!reachedBlocks.contains(block)) {
                    for (Instr instr : block.getInstrs()) {
                        instr.removeOperands();
                    }
                    block.removeOperands();
                    it.remove();
                    block.setDeleted();
                }
            }
        }
    }

    public static void deleteDeadInstr(BasicBlock block) {
        int index = 0;
        ArrayList<Instr> instrs = block.getInstrs();
        while (true) {
            Instr.InstrType type = instrs.get(index).getInstrType();
            if (type == Instr.InstrType.JUMP ||
                    type == Instr.InstrType.BRANCH ||
                    type == Instr.InstrType.RETURN) {
                break;
            }
            index++;
        }
        index++;
        while (index <= instrs.size() - 1) {
            instrs.get(index).removeOperands();
            instrs.remove(index);
        }
    }

    public static void findReachable(BasicBlock block) {
        if (reachedBlocks.contains(block)) {
            return;
        }
        reachedBlocks.add(block);
        Instr lastInstr = block.getInstrs().get(block.getInstrs().size() - 1);
        if (lastInstr instanceof Jmp) {
            findReachable(((Jmp) lastInstr).getToBlock());
        } else if (lastInstr instanceof Branch) {
            findReachable(((Branch) lastInstr).getThenBlock());
            findReachable(((Branch) lastInstr).getElseBlock());
        }
    }

    public static void blockMerge(Module module) {
        for (Function func : module.getFunctions()) {
            for (BasicBlock block : func.getBlocks()) {
                if (!block.isDeleted()) {
                    if (block.getChild().size() == 1) {
                        BasicBlock child = block.getChild().get(0);
                        if (child.getParent().size() == 1) {
                            Instr jumpInstr = block.getInstrs().get(block.getInstrs().size() - 1);
                            jumpInstr.removeOperands();
                            block.getInstrs().remove(jumpInstr);
                            Iterator<Instr> it = child.getInstrs().iterator();
                            while (it.hasNext()) {
                                Instr instr = it.next();
                                if (instr instanceof Phi) {
                                    Phi phi = (Phi) instr;
                                    ArrayList<Value> operands = phi.getOperands();
                                    ArrayList<BasicBlock> blocks = phi.getBlocks();
                                    phi.modifyValueForUsers(operands.get(blocks.indexOf(block)));
                                    phi.removeOperands();
                                } else {
                                    block.getInstrs().add(instr);
                                    instr.setParentBlock(block);
                                }
                                it.remove();
                            }
                            child.modifyValueForUsers(block);
                            child.setDeleted();
                            }
                        }
                    }
                }
                Iterator<BasicBlock> it = func.getBlocks().iterator();
                while (it.hasNext()) {
                    if (it.next().isDeleted()) {
                        it.remove();
                    }
                }
            }
        }
    }
