package LlvmIr.Instruction;

import LlvmIr.BasicBlock;
import LlvmIr.Instr;
import LlvmIr.Value;

public class Ret extends Instr {
    public Ret(Value exp, BasicBlock parent){
        super(null,null,InstrType.RETURN,parent);
        addOperand(exp);
    }
    public String toString() {
        if (operands.get(0) == null) return "ret void";
        return "ret " + operands.get(0).getLlvmType() + " " + operands.get(0).getName();
    }
}
