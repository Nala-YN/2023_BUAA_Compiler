package Optimize;

import LlvmIr.BasicBlock;
import LlvmIr.Global.Function;
import LlvmIr.Global.GlobalVar;
import LlvmIr.Instr;
import LlvmIr.Instruction.Call;
import LlvmIr.Instruction.GetPtr;
import LlvmIr.Instruction.Load;
import LlvmIr.Instruction.Store;
import LlvmIr.Module;
import LlvmIr.Value;

import java.util.ArrayList;
import java.util.HashMap;

public class MemoryAccessOptimize {
    public static void optimize(Module module){
        for(Function func: module.getFunctions()){
            for(BasicBlock block: func.getBlocks()){
                localizeGlobalVar(block);
                mergeUselessLoad(block);
            }
        }
    }
    private static void localizeGlobalVar(BasicBlock block){
        ArrayList<Instr> instrs=new ArrayList<>(block.getInstrs());
        HashMap<Value, Value> gvBuffer = new HashMap<>();
        HashMap<Value, Value> writeBackBuffer = new HashMap<>();
        for(Instr instr:instrs){
            if(instr instanceof Load loadInstr){
                Value point=loadInstr.getOperands().get(0);
                if(point instanceof GlobalVar globalVar){
                    if(gvBuffer.containsKey(globalVar)){
                        loadInstr.modifyValueForUsers(gvBuffer.get(globalVar));
                        loadInstr.removeOperands();
                        block.getInstrs().remove(loadInstr);
                    }
                    else{
                        gvBuffer.put(globalVar,loadInstr);
                    }
                }
            }
            else if(instr instanceof Store storeInstr){
                Value point=storeInstr.getTo();
                if(point instanceof GlobalVar globalVar){
                    gvBuffer.put(globalVar,storeInstr.getFrom());
                    writeBackBuffer.put(globalVar,storeInstr.getFrom());
                    storeInstr.removeOperands();
                    block.getInstrs().remove(storeInstr);
                }
            }
            else if(instr instanceof Call call){
                gvBuffer.clear();
                writeBackBuffer.forEach((globalVal,value)->{
                    Store store=new Store(value,globalVal,block);
                    block.getInstrs().add(block.getInstrs().indexOf(call),store);
                });
                writeBackBuffer.clear();
            }
        }
        writeBackBuffer.forEach((globalVar,value)->{
            Store store=new Store(value,globalVar,block);
            block.getInstrs().add(block.getInstrs().size()-1,store);
        });
    }
    public static void mergeUselessLoad(BasicBlock block){
        ArrayList<Instr> instrs=new ArrayList<>(block.getInstrs());
        HashMap<Value,Value> addrValueMap=new HashMap<>();
        for(Instr instr:instrs){
            if(instr instanceof Load load){
                Value addr=load.getOperands().get(0);
                if(!addrValueMap.containsKey(addr)){
                    addrValueMap.put(addr,load);
                }
                else{
                    load.modifyValueForUsers(addrValueMap.get(addr));
                    load.removeOperands();
                    block.getInstrs().remove(load);
                }
            }
            else if(instr instanceof Store store){
                addrValueMap.clear();
                addrValueMap.put(store.getTo(),store.getFrom());
            }
            else if(instr instanceof Call){
                addrValueMap.clear();
            }
        }
    }
}
