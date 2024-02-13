package ToMips.AsmInstruction;

import ToMips.AsmInstr;
import ToMips.Register;

public class LiAsm extends AsmInstr {
    private Register reg;
    private int value;

    public LiAsm(Register reg, int value) {
        this.reg = reg;
        this.value = value;
    }

    public int getValue() {
        return value;
    }

    public Register getReg() {
        return reg;
    }

    public String toString(){
        return "li "+reg+","+value;
    }
}
