package LlvmIr.Instruction;

import LlvmIr.BasicBlock;
import LlvmIr.Instr;
import LlvmIr.Type.LlvmType;
import LlvmIr.Value;

public class Store extends Instr {
    public Store( Value from, Value to, BasicBlock parent){
        super(null, LlvmType.Void,InstrType.STORE,parent);
        addOperand(from);
        addOperand(to);
    }
    @Override
    public String toString() {
        Value from = operands.get(0);
        Value to = operands.get(1);
        return "store " + from.getLlvmType() + " " + from.getName() + ", " + to.getLlvmType() + " " + to.getName();
    }
    public Value getFrom(){
        return operands.get(0);
    }
    public Value getTo(){
        return operands.get(1);
    }
}
