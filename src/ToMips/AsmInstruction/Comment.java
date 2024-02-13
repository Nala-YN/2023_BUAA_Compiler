package ToMips.AsmInstruction;

import LlvmIr.Instr;
import ToMips.AsmInstr;

public class Comment extends AsmInstr {
    private Instr llvmInstr;

    public Comment(Instr llvmInstr) {
        this.llvmInstr = llvmInstr;
    }
    public String toString(){
        return "#"+llvmInstr.toString();
    }
}
