package Optimize;

import LlvmIr.BasicBlock;
import LlvmIr.Constant;
import LlvmIr.Global.Function;
import LlvmIr.Global.GlobalVar;
import LlvmIr.IRBuilder;
import LlvmIr.Instr;
import LlvmIr.Instruction.Alloca;
import LlvmIr.Instruction.Call;
import LlvmIr.Instruction.Store;
import LlvmIr.Module;
import LlvmIr.Type.LlvmType;
import LlvmIr.Type.PointerType;
import LlvmIr.User;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;

public class GlobalVarLocalize {
    public static void globalVarLocalize(Module module){
        usedMap=new HashMap<>();
        calledMap=new HashMap<>();
        analyzeGlobalVarUse(module);
        createCallMap(module);
        localize(module);
    }
    private static HashMap<GlobalVar,HashSet<Function>> usedMap;
    public static void analyzeGlobalVarUse(Module module){
        for(GlobalVar globalVar:module.getGlobalVars()){
            for(User user:globalVar.getUsers()){
                Function func=((Instr)user).getParentBlock().getParentFunc();
                if(!usedMap.containsKey(globalVar)){
                    usedMap.put(globalVar,new HashSet<>());
                }
                usedMap.get(globalVar).add(func);
            }
        }
    }
    private static HashMap<Function,HashSet<Function>> calledMap;
    public static void createCallMap(Module module){
        for(Function func: module.getFunctions()){
            for(BasicBlock block: func.getBlocks()){
                for(Instr instr: block.getInstrs()){
                    if(instr instanceof Call){
                        Function target= (Function) instr.getOperands().get(0);
                        if(!calledMap.containsKey(target)){
                            calledMap.put(target,new HashSet<>());
                        }
                        calledMap.get(target).add(func);
                    }
                }
            }
        }
    }
    public static void localize(Module module){
        ArrayList<GlobalVar> copy=new ArrayList<>(module.getGlobalVars());
        for(GlobalVar globalVar:copy){
            if(!usedMap.containsKey(globalVar)){
                module.getGlobalVars().remove(globalVar);
            }
            else if(usedMap.get(globalVar).size()==1){
                Function func=usedMap.get(globalVar).iterator().next();
                if(func.getActiveCnt()>18){
                    continue;
                }
                if(!calledMap.containsKey(func)&&((PointerType)(globalVar.getLlvmType())).getPointedType().isInt32()){
                    BasicBlock block=func.getBlocks().get(0);
                    Alloca alloca=new Alloca(IRBuilder.tempName+func.getVarId(),block,LlvmType.Int32);
                    block.getInstrs().add(0,alloca);
                    int initial;
                    if(globalVar.isZeroInitial()){
                        initial=0;
                    }
                    else{
                        initial=globalVar.getInitial().get(0);
                    }
                    Store store=new Store(new Constant(initial),alloca,block);
                    block.getInstrs().add(1,store);
                    globalVar.modifyValueForUsers(alloca);
                    module.getGlobalVars().remove(globalVar);
                }
            }
        }
    }
}
