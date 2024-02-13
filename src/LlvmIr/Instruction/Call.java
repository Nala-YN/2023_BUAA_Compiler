package LlvmIr.Instruction;

import LlvmIr.BasicBlock;
import LlvmIr.Global.Function;
import LlvmIr.Instr;
import LlvmIr.Value;
import ToMips.Register;

import java.util.ArrayList;
import java.util.HashSet;

public class Call extends Instr {
    private HashSet<Register> activeReg;
    public Call(Function func, String name, ArrayList<Value> values,BasicBlock parent){
        super(name, func.getRetType(),InstrType.CALL,parent);
        addOperand(func);
        for(Value value:values){
            addOperand(value);
        }
    }
    public Call(Function func, ArrayList<Value> values,BasicBlock parent){
        super(null, func.getRetType(),InstrType.CALL,parent);
        addOperand(func);
        for(Value value:values){
            addOperand(value);
        }
    }

    public HashSet<Register> getActiveReg() {
        return activeReg;
    }

    public void setActiveReg(HashSet<Register> activeReg) {
        this.activeReg = activeReg;
    }

    public String getGvnHash(){
        StringBuilder sb=new StringBuilder(operands.get(0).getName());
        sb.append("(");
        for(int i=1;i<=operands.size()-1;i++){
            if(i==1){
                sb.append(operands.get(i).getName());
            }
            else{
                sb.append(",").append(operands.get(i).getName());
            }
        }
        sb.append(")");
        return sb.toString();
    }
    @Override
    public String toString() {
        StringBuilder sb=new StringBuilder();
        for(int i=1;i<=operands.size()-1;i++){
            if(i>1){
                sb.append(",");
            }
            sb.append(operands.get(i).getLlvmType()).append(" ").append(operands.get(i).getName());
        }
        if (type.isVoid()) {
            return "call void " + operands.get(0).getName() + "(" + sb.toString() +")";
        } else {
            return name + " = call i32 " + operands.get(0).getName() + "(" + sb.toString() +")";
        }
    }
}
