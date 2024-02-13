package LlvmIr.Instruction.IO;

import LlvmIr.BasicBlock;
import LlvmIr.Instr;
import LlvmIr.Type.LlvmType;
import LlvmIr.Value;

public class PutInt extends Instr {
    public PutInt(Value value, BasicBlock parent){
        super(null, LlvmType.Void,InstrType.PUTINT,parent);
        if(value==null){
            int o=1;
        }
        addOperand(value);
    }
    public String toString() {
        return "call void @putint(i32 " + operands.get(0).getName() + ")";
    }
}
