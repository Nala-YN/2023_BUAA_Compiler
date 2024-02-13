package LlvmIr.Instruction.IO;

import LlvmIr.BasicBlock;
import LlvmIr.Global.CstStr;
import LlvmIr.Instr;
import LlvmIr.Type.LlvmType;
import LlvmIr.Type.PointerType;

public class PutStr extends Instr {
    private CstStr content;

    public PutStr(BasicBlock parentBlock, CstStr content) {
        super(null, LlvmType.Void, InstrType.PUTSTR, parentBlock);
        this.content = content;
    }
    public String toString() {
        PointerType pointerType = (PointerType) content.getLlvmType();
        return "call void @putstr(i8* getelementptr inbounds (" +
                pointerType.getPointedType() + ", " +
                pointerType + " " +
                content.getName() +", i32 0, i32 0))";
    }

    public CstStr getContent() {
        return content;
    }
    //getelementptr inbounds ([10 x i8], [10 x i8]* @str0, i32 0, i32 0)
}
