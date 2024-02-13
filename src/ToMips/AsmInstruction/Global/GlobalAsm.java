package ToMips.AsmInstruction.Global;

import ToMips.AsmInstr;

public class GlobalAsm extends AsmInstr {
    protected String name;
    public GlobalAsm(String name){
        this.name=name;
    }
}
