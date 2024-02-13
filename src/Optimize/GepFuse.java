package Optimize;

import LlvmIr.BasicBlock;
import LlvmIr.Constant;
import LlvmIr.Global.Function;
import LlvmIr.Global.GlobalVar;
import LlvmIr.Instr;
import LlvmIr.Instruction.Alloca;
import LlvmIr.Instruction.GetPtr;
import LlvmIr.Instruction.Load;
import LlvmIr.Instruction.OffsetLoad;
import LlvmIr.Instruction.OffsetStore;
import LlvmIr.Instruction.Store;
import LlvmIr.Module;
import LlvmIr.User;

import java.util.ArrayList;

public class GepFuse {
    public static void fuseGep(Module module){
        for(Function func: module.getFunctions()){
            for(BasicBlock block:func.getBlocks()){
                ArrayList<Instr> copy=new ArrayList<>(block.getInstrs());
                for(Instr instr:copy){
                    if(instr instanceof GetPtr getPtr){
                        if(getPtr.getOperands().get(1) instanceof Constant cons
                                &&!(getPtr.getOperands().get(0) instanceof Alloca)){
                            if(getPtr.getOperands().get(0) instanceof GlobalVar)continue;
                            int off=cons.getValue();
                            ArrayList<User> users=new ArrayList<>(getPtr.getUsers());
                            for(User user:users){
                                if(user instanceof Store store){
                                    OffsetStore offsetStore=new OffsetStore(null,getPtr.getOperands().get(0)
                                            ,new Constant(off),store.getParentBlock(),store.getFrom());
                                    ArrayList<Instr> parentInstrs=store.getParentBlock().getInstrs();
                                    store.modifyValueForUsers(offsetStore);
                                    store.removeOperands();
                                    parentInstrs.set(parentInstrs.indexOf(store),offsetStore);
                                }
                                else if(user instanceof Load load){
                                    OffsetLoad offsetLoad=new OffsetLoad(func.getVarName(),getPtr.getOperands().get(0)
                                            ,new Constant(off),load.getParentBlock());
                                    ArrayList<Instr> parentInstrs=load.getParentBlock().getInstrs();
                                    load.modifyValueForUsers(offsetLoad);
                                    load.removeOperands();
                                    parentInstrs.set(parentInstrs.indexOf(load),offsetLoad);
                                }
                            }
                            if(getPtr.getUsers().size()==0){
                                getPtr.removeOperands();
                                block.getInstrs().remove(getPtr);
                            }
                        }
                    }
                }
            }
        }
    }
}
