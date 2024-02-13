package Optimize;

import LlvmIr.BasicBlock;
import LlvmIr.Constant;
import LlvmIr.Global.Function;
import LlvmIr.Instr;
import LlvmIr.Instruction.Phi;
import LlvmIr.Module;
import LlvmIr.Undef;
import LlvmIr.Value;

import java.util.ArrayList;
import java.util.Iterator;

public class PhiOptimize {
    public static void optimizePhi(Module module) {
        for (Function func : module.getFunctions()) {
            for (BasicBlock block : func.getBlocks()) {
                ArrayList<Instr> instrs = new ArrayList<>(block.getInstrs());
                for (Instr instr : instrs) {
                    if (!(instr instanceof Phi)) {
                        break;
                    }
                    Phi phi = (Phi) instr;
                    ArrayList<Value> operands = phi.getOperands();
                    for(int i=0;i<=operands.size()-1;i++){
                        if(phi.getBlocks().get(i).isDeleted()/*||operands.get(i) instanceof Undef*/){
                            operands.remove(i);
                            phi.getBlocks().remove(i);
                            i--;
                        }
                    }
                    boolean allSame = true;
                    for (Value value : operands) {
                        if (value != operands.get(0)){
                            allSame=false;
                            break;
                        }
                    }
                    if (allSame || phi.getUsers().size()==0) {
                        Value value = operands.get(0);
                        block.getInstrs().remove(phi);
                        phi.modifyValueForUsers(value);
                        phi.removeOperands();
                    }
                }
                /*instrs=new ArrayList<>(block.getInstrs());
                for(int i=0;i<=instrs.size()-1;i++){
                    Instr instr1=instrs.get(i);
                    if(!(instr1 instanceof Phi phi1)){
                        break;
                    }
                    for(int j=i+1;j<=instrs.size()-1;j++){
                        Instr instr2=instrs.get(j);
                        if(!(instr2 instanceof Phi phi2)){
                            break;
                        }
                        if(phi1.getOperands().size()!=phi2.getOperands().size())continue;
                        boolean flag=true;
                        for(int k=0;k<=phi1.getOperands().size()-1;k++){
                            if(phi1.getOperands().get(k)!=phi2.getOperands().get(k)||
                            phi1.getBlocks().get(k)!=phi2.getBlocks().get(k)){
                                flag=false;
                                break;
                            }
                        }
                        if(flag&&block.getInstrs().contains(phi2)){
                            phi2.removeOperands();
                            phi2.modifyValueForUsers(phi1);
                        }
                    }
                }*/
            }
        }
    }
}
