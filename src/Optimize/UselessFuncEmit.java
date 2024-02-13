package Optimize;

import LlvmIr.BasicBlock;
import LlvmIr.Global.Function;
import LlvmIr.Instr;
import LlvmIr.Instruction.Call;
import LlvmIr.Module;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;

public class UselessFuncEmit {
    public static void emitUselessFunc(Module module){
        HashSet<Function> mainCalledClosure=new HashSet<>();
        HashMap<Function,HashSet<Function>> callMap=new HashMap<>();
        Function main = null;
        for(Function func:module.getFunctions()){
            callMap.put(func,new HashSet<>());
        }
        for(Function func:module.getFunctions()){
            if(func.getName().equals("@main")){
                main=func;
            }
            for(BasicBlock block:func.getBlocks()){
                for(Instr instr:block.getInstrs()){
                    if(instr instanceof Call call){
                        callMap.get(func).add((Function) call.getOperands().get(0));
                    }
                }
            }
        }
        mainCalledClosure.add(main);
        boolean change=true;
        while (change){
            change=false;
            HashSet<Function> addFunc=new HashSet<>();
            for(Function func:mainCalledClosure){
                for(Function called:callMap.get(func)){
                    if(!mainCalledClosure.contains(called)){
                        addFunc.add(called);
                        change=true;
                    }
                }
            }
            mainCalledClosure.addAll(addFunc);
        }
        Iterator<Function> it=module.getFunctions().iterator();
        while (it.hasNext()){
            Function func=it.next();
            if(!mainCalledClosure.contains(func)){
                for(BasicBlock block:func.getBlocks()){
                    for(Instr instr:block.getInstrs()){
                        instr.removeOperands();
                    }
                    block.removeOperands();
                }
                func.removeOperands();
                it.remove();
            }
        }
    }
}
