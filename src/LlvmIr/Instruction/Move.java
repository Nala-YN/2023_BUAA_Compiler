package LlvmIr.Instruction;

import LlvmIr.BasicBlock;
import LlvmIr.Instr;
import LlvmIr.Type.LlvmType;
import LlvmIr.Value;

public class Move extends Instr {

    public Move( Value to, Value from,BasicBlock parentBlock) {
        super(null, LlvmType.Void,InstrType.MOVE, parentBlock);
        addOperand(to);
        addOperand(from);
    }

    public Value getFrom() {
        return operands.get(1);
    }

    public Value getTo() {
        return operands.get(0);
    }
    public void setFrom(Value value){
        if(value!=null) value.addUser(this);
        operands.set(1,value);
    }
    public String toString(){
        return "move "+getTo().getName()+","+getFrom().getName();
    }
}
