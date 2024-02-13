package LlvmIr.Instruction;

import LlvmIr.BasicBlock;
import LlvmIr.Constant;
import LlvmIr.Instr;
import LlvmIr.Type.LlvmType;
import LlvmIr.Value;

public class OffsetLoad extends Instr {
    public OffsetLoad(String name, Value point, Constant offset, BasicBlock parent){
        super(name, LlvmType.Int32,InstrType.LOAD,parent);
        addOperand(point);
        addOperand(offset);
    }
    public String toString(){
        return name + " = offsetLoad " + type + ", " + operands.get(0).getLlvmType()
                + " " +"("+((Constant)operands.get(1)).getValue()+")" +operands.get(0).getName();
    }
}
