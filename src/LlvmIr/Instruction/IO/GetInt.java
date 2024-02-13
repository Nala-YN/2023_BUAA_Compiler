package LlvmIr.Instruction.IO;

import LlvmIr.BasicBlock;
import LlvmIr.Instr;
import LlvmIr.Type.LlvmType;

public class GetInt extends Instr {
    public GetInt(String name, BasicBlock parent){
        super(name, LlvmType.Int32,InstrType.GETINT,parent);
    }

    public String toString(){
        return name + " = call i32 (...) @getint()";
    }

}
