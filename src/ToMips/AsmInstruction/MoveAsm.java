package ToMips.AsmInstruction;

import ToMips.AsmInstr;
import ToMips.Register;

public class MoveAsm extends AsmInstr {
    private Register to;
    private Register from;

    public MoveAsm(Register to, Register from) {
        this.to = to;
        this.from = from;
    }
    public String toString(){
        return "move "+to+","+from;
    }

    public Register getTo() {
        return to;
    }

    public Register getFrom() {
        return from;
    }
}
