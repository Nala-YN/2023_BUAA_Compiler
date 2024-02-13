package Optimize;

import LlvmIr.BasicBlock;
import LlvmIr.Constant;
import LlvmIr.Global.Function;
import LlvmIr.Global.GlobalVar;
import LlvmIr.Instr;
import LlvmIr.Instruction.Alloca;
import LlvmIr.Instruction.Branch;
import LlvmIr.Instruction.Call;
import LlvmIr.Instruction.GetPtr;
import LlvmIr.Instruction.IO.GetInt;
import LlvmIr.Instruction.IO.PutInt;
import LlvmIr.Instruction.IO.PutStr;
import LlvmIr.Instruction.Jmp;
import LlvmIr.Instruction.Load;
import LlvmIr.Instruction.Ret;
import LlvmIr.Instruction.Store;
import LlvmIr.Module;
import LlvmIr.Param;
import LlvmIr.User;
import LlvmIr.Value;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;

public class DeadCodeRemove {
    public static void removeDeadCode(Module module) {
        for (Function func : module.getFunctions()) {
            for (BasicBlock block : func.getBlocks()) {
                Iterator<Instr> it = block.getInstrs().iterator();
                while (it.hasNext()) {
                    Instr instr = it.next();
                    if (instr.getUsers().isEmpty() && instr.hasLVal() && !(instr instanceof Call || instr instanceof GetInt)) {
                        instr.removeOperands();
                        it.remove();
                    }
                    if (instr instanceof Call) {
                        Function target = (Function) instr.getOperands().get(0);
                        if (instr.getUsers().isEmpty() && !target.isHasSideEffects()) {
                            instr.removeOperands();
                            it.remove();
                        }
                    }
                }
            }
        }
    }

    public static void romoveDeadCall(Module module) {
        for (Function func : module.getFunctions()) {
            for (BasicBlock block : func.getBlocks()) {
                Iterator<Instr> it = block.getInstrs().iterator();
                while (it.hasNext()) {
                    Instr instr = it.next();
                    if (instr instanceof Call) {
                        Function target = (Function) instr.getOperands().get(0);
                        if (instr.getUsers().isEmpty() && !target.isHasSideEffects()) {
                            instr.removeOperands();
                            it.remove();
                        }
                    }
                }
            }
        }
    }

    private static HashSet<Instr> usefulInstr;

    //private static HashMap<GlobalVar,>
    public static void removeUselessCode(Module module) {
        for (Function func : module.getFunctions()) {
            usefulInstr = new HashSet<>();
            for (BasicBlock block : func.getBlocks()) {
                for (Instr instr : block.getInstrs()) {
                    if (instr instanceof Branch ||
                            instr instanceof Ret ||
                            instr instanceof Jmp ||
                            instr instanceof PutInt ||
                            instr instanceof GetInt ||
                            instr instanceof PutStr ||
                            instr instanceof Store ||
                            (instr instanceof Call && ((Function) (instr.getOperands().get(0))).isHasSideEffects())) {
                        findClosure(instr);
                    }
                }
            }
            for (BasicBlock block : func.getBlocks()) {
                ArrayList<Instr> instrs = new ArrayList<>(block.getInstrs());
                for (Instr instr : instrs) {
                    if (!usefulInstr.contains(instr)) {
                        block.getInstrs().remove(instr);
                        instr.removeOperands();
                    } else if (instr instanceof Store) {
                        boolean hasUser = false;
                        for (int i = instrs.indexOf(instr) + 1; i <= instrs.size() - 1; i++) {
                            if (instrs.get(i) instanceof Store) {
                                Store store = (Store) instrs.get(i);
                                if (instr.getOperands().get(1) == store.getOperands().get(1)) {
                                    break;
                                }
                            } else if (instr.getOperands().contains(instr.getOperands().get(1))) {
                                hasUser = true;
                                break;
                            }
                        }
                        if (!hasUser) {
                            instr.removeOperands();
                            block.getInstrs().remove(instr);
                        }
                    }
                }
            }
        }
    }
    public static void aggressiveDeadCodeRemove(Module module){
        HashSet<Instr> usefulInstrs=new HashSet<>();
        BasicBlock endBlock=null;
        for(Function func:module.getFunctions()){
            if(func.getName().equals("@main")){
                for(BasicBlock block:func.getBlocks()){
                    for(Instr instr:block.getInstrs()){
                        if(instr instanceof GetInt){
                            if(instr.getParentBlock()!=func.getBlocks().get(0)){
                                return;
                            }
                        }
                        else if(instr instanceof PutStr || instr instanceof PutInt){
                            usefulInstrs.add(instr);
                            if(endBlock==null){
                                endBlock=instr.getParentBlock();
                            }
                            else if(!endBlock.equals(instr.getParentBlock())){
                                return;
                            }
                        }
                        else if(instr instanceof Ret ret){
                            usefulInstrs.add(ret);
                            if(endBlock==null){
                                endBlock=ret.getParentBlock();
                            }
                            else if(!endBlock.equals(ret.getParentBlock())){
                                return;
                            }
                        }
                    }
                }
                if(endBlock==null){
                    return;
                }
                for(Instr instr:usefulInstrs){
                    for(Value value:instr.getOperands()){
                        if(!(value instanceof Constant)){
                            return;
                        }
                    }
                }
                BasicBlock entryBlock=func.getBlocks().get(0);
                if(entryBlock==endBlock){
                    return;
                }
                Jmp jmp=new Jmp(endBlock,entryBlock);
                entryBlock.getInstrs().set(entryBlock.getInstrs().size()-1,jmp);
                entryBlock.setChild(new ArrayList<>());
                entryBlock.addChild(endBlock);
                endBlock.setParent(new ArrayList<>());
                endBlock.addParent(entryBlock);
            }

        }
    }
    //private static HashMap<>
    public static void removeUseLessStore(Module module) {
        /*for(Function func:module.getFunctions()){
            HashSet<GetPtr> storeBase=new HashSet<>();
            HashSet<GetPtr> loadBase=new HashSet<>();
            for(BasicBlock block:func.getBlocks()){
                for(Instr instr:block.getInstrs()){
                    if(instr instanceof Alloca alloca){
                        storeBase.add((GetPtr) alloca.getUsers().get(0));
                    }
                }
            }
            for(BasicBlock block:func.getBlocks()){
                for(Instr instr:block.getInstrs()){
                    if(instr instanceof Load load){
                        if(load.getOperands().get(0) instanceof GetPtr getPtr){
                            if(getPtr.getOperands().get(0) instanceof GetPtr){
                                loadBase.add((GetPtr) getPtr.getOperands().get(0));
                            }
                        }
                    }
                }
            }
            for(GetPtr getPtr:storeBase){
                if(!loadBase.contains(getPtr)){
                    removeStore(getPtr);
                }
            }
        }*/

    }

    public static void removeStore(Instr instr) {
        instr.removeOperands();
        instr.getParentBlock().getInstrs().remove(instr);
        ArrayList<User> users = new ArrayList<>(instr.getUsers());
        for (User user : users) {
            removeStore((Instr) user);
        }
    }

    public static void findClosure(Instr instr) {
        if (!usefulInstr.contains(instr)) {
            usefulInstr.add(instr);
            for (Value operand : instr.getOperands()) {
                if (operand instanceof Instr) {
                    findClosure((Instr) operand);

                }
            }
        }
    }
}
