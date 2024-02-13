package ToMips.AsmInstruction;

import ToMips.AsmInstr;
import ToMips.Register;

public class CmpAsm extends AsmInstr {
    public enum OP{
        sgt,
        slt,
        sge,
        sle,
        seq,
        sne
    }
    private Register operand1;
    private Register operand2;
    private Register to;
    private OP op;

    public CmpAsm(Register operand1, Register operand2, Register to, OP op) {
        this.operand1 = operand1;
        this.operand2 = operand2;
        this.to = to;
        this.op = op;
    }

    public Register getOperand1() {
        return operand1;
    }

    public Register getOperand2() {
        return operand2;
    }

    public Register getTo() {
        return to;
    }

    public String toString(){
        return op+" "+to+","+operand1+","+operand2;
    }
}
