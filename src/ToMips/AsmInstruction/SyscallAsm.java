package ToMips.AsmInstruction;

import ToMips.AsmInstr;

public class SyscallAsm extends AsmInstr {
    public SyscallAsm() {
    }
    public String toString(){
        return "syscall";
    }
}
