package ToMips.AsmInstruction;

import ToMips.AsmInstr;
import ToMips.Register;

public class LaAsm extends AsmInstr {
    private Register reg;
    private String name;

    public LaAsm(Register reg, String name) {
        this.reg = reg;
        this.name = name;
    }

    public String getName() {
        return name;
    }

    public Register getReg() {
        return reg;
    }

    public String toString(){
        return "la "+reg+","+name;
    }
}
