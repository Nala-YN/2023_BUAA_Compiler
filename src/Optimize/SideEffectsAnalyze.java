package Optimize;

import LlvmIr.BasicBlock;
import LlvmIr.Global.Function;
import LlvmIr.Global.GlobalVar;
import LlvmIr.Instr;
import LlvmIr.Instruction.Call;
import LlvmIr.Instruction.GetPtr;
import LlvmIr.Instruction.IO.GetInt;
import LlvmIr.Instruction.IO.PutInt;
import LlvmIr.Instruction.IO.PutStr;
import LlvmIr.Instruction.OffsetStore;
import LlvmIr.Instruction.Store;
import LlvmIr.Module;
import LlvmIr.Param;
import LlvmIr.Value;

import java.util.ArrayList;
import java.util.HashSet;

public class SideEffectsAnalyze {
    public static void analyzeSideEffects(Module module){
        for(Function func:module.getFunctions()){
            boolean hasSideEffects=false;
            HashSet<Function> call=new HashSet<>();
            for(BasicBlock block:func.getBlocks()){
                for(Instr instr:block.getInstrs()){
                    if(instr instanceof Call){
                        Function target=(Function)(instr).getOperands().get(0);
                        call.add(target);
                    }
                    else if(instr instanceof GetInt ||
                    instr instanceof PutInt||
                    instr instanceof PutStr){
                        hasSideEffects=true;
                        break;
                    }
                    else if(instr instanceof Store||instr instanceof OffsetStore){
                        Value to=instr.getOperands().get(1);
                        if(to instanceof GlobalVar){
                            hasSideEffects=true;
                            break;
                        }
                        else if(to instanceof GetPtr){
                            GetPtr getPtr=(GetPtr) to;
                            if(getPtr.getOperands().get(0) instanceof Param||
                            getPtr.getOperands().get(0) instanceof GlobalVar){
                                hasSideEffects=true;
                                break;
                            }
                        }
                    }
                }
                if(hasSideEffects)break;
            }
            func.setCall(call);
            func.setHasSideEffects(hasSideEffects);
        }
        boolean change=true;
        while(change){
            change=false;
            for(Function func:module.getFunctions()){
                for(Function call:func.getCall()){
                    if(call.isHasSideEffects()&&!func.isHasSideEffects()){
                        func.setHasSideEffects(true);
                        change=true;
                        break;
                    }
                }
            }
        }
    }
}
