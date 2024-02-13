package LlvmIr.Instruction;

import LlvmIr.BasicBlock;
import LlvmIr.Constant;
import LlvmIr.Instr;
import LlvmIr.Type.LlvmType;
import LlvmIr.Value;

public class OffsetStore extends Instr {
    public OffsetStore(String name, Value point, Constant offset, BasicBlock parent,Value value){
        super(name, LlvmType.Int32,InstrType.LOAD,parent);
        addOperand(value);
        addOperand(point);
        addOperand(offset);
    }
    public String toString() {
        Value from = operands.get(0);
        Value to = operands.get(1);
        int cons=((Constant)operands.get(2)).getValue();
        return "offsetStore " + from.getLlvmType() + " " + from.getName() + ", " + to.getLlvmType() + " " + '('+cons+')'+to.getName();
    }
}
