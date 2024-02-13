package ToMips.AsmInstruction;

import ToMips.AsmInstr;
import ToMips.Register;

public class MemAsm extends AsmInstr {
    public enum OP{
        sw,
        lw
    }
    private OP op;
    private int offset;
    private Register base;
    private Register value;

    public MemAsm(OP op, int offset, Register base, Register value) {
        this.op = op;
        this.offset = offset;
        this.base = base;
        this.value = value;
    }

    public OP getOp() {
        return op;
    }

    public int getOffset() {
        return offset;
    }

    public Register getBase() {
        return base;
    }

    public Register getValue() {
        return value;
    }

    public String toString(){
        return op+" "+value+","+offset+"("+base+")";
    }
}
