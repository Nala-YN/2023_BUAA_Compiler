package LlvmIr.Instruction;

import LlvmIr.BasicBlock;
import LlvmIr.Instr;
import LlvmIr.Type.LlvmType;
import LlvmIr.Type.PointerType;
import LlvmIr.Value;

public class GetPtr extends Instr {
    public GetPtr(String name, Value array, Value offset, BasicBlock parent){
        super(name, new PointerType(LlvmType.Int32),InstrType.GETPTR,parent);
        addOperand(array);
        addOperand(offset);
    }
    public String getGvnHash(){
        return "GetPtr "+operands.get(0).getName()+" "+operands.get(1).getName();
    }
    public String toString(){
        Value pointer = operands.get(0);
        Value offset = operands.get(1);
        PointerType pointerType = (PointerType) pointer.getLlvmType();
        LlvmType pointType = pointerType.getPointedType();
        if(pointType.isInt32()){
            return  name+" = getelementptr inbounds i32, i32* "+pointer.getName()+", i32 "+offset.getName();
        }
        else{
            return name + " = getelementptr inbounds " + pointType + ", " + pointerType + " " +
                    pointer.getName() + ", i32 0, i32 " + offset.getName();
        }
    }
}
