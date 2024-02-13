package ToMips.AsmInstruction;

import ToMips.AsmInstr;
import ToMips.Register;

public class MoveToAsm extends AsmInstr {
    public enum OP{
        hi,
        lo
    }
    private OP op;
    private Register to;

    public MoveToAsm(OP op, Register to) {
        this.op = op;
        this.to = to;
    }

    public OP getOp() {
        return op;
    }

    public Register getTo() {
        return to;
    }
    public String toString(){
        return "mt"+op+" "+to;
    }
}
