package LlvmIr.Instruction;

import LlvmIr.BasicBlock;
import LlvmIr.Instr;
import LlvmIr.Type.LlvmType;
import LlvmIr.Value;

public class Load extends Instr {
    public Load(String name, Value point, BasicBlock parent){
        super(name, LlvmType.Int32,InstrType.LOAD,parent);
        addOperand(point);
    }
    public String toString(){
        return name + " = load " + type + ", " + operands.get(0).getLlvmType() + " " + operands.get(0).getName();
    }
}
