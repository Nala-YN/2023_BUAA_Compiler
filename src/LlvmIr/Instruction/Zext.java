package LlvmIr.Instruction;

import LlvmIr.BasicBlock;
import LlvmIr.Instr;
import LlvmIr.Type.LlvmType;
import LlvmIr.Value;

public class Zext extends Instr {
    private LlvmType aimType;

    public Zext(String name, Value value,BasicBlock parentBlock, LlvmType aimType) {
        super(name, aimType, InstrType.ZEXT, parentBlock);
        this.aimType = aimType;
        addOperand(value);
    }
    public String toString(){
        return name+"=zext "+operands.get(0).getLlvmType()+" "+operands.get(0).getName()+" to "+aimType;
    }

    public LlvmType getAimType() {
        return aimType;
    }
}
