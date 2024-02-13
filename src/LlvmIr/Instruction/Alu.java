package LlvmIr.Instruction;

import LlvmIr.BasicBlock;
import LlvmIr.Instr;
import LlvmIr.Type.LlvmType;
import LlvmIr.Value;

public class Alu extends Instr {
    public enum OP{
        ADD,
        SUB,
        SREM,
        MUL,
        SDIV,
    }
    private OP op;
    public Alu(String name, Value v1, Value v2, OP op, BasicBlock parent){
        super(name, LlvmType.Int32,InstrType.ALU,parent);
        this.op=op;
        addOperand(v1);
        addOperand(v2);
    }
    @Override
    public String toString() {
        return name + " = " + op.toString().toLowerCase() + " i32 " + operands.get(0).getName() + ", " + operands.get(1).getName();
    }
    public String getGvnHash(){
        String op1=operands.get(0).getName();
        String op2=operands.get(1).getName();
        if(op==OP.ADD || op==OP.MUL){
            if(op1.compareTo(op2)<0){
                op2=operands.get(0).getName();
                op1=operands.get(1).getName();
            }
        }
        return op1+op+op2;
    }
    public OP getOp() {
        return op;
    }

    public void setOp(OP op) {
        this.op = op;
    }
}
