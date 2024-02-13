package ToMips.AsmInstruction;

import ToMips.AsmInstr;
import ToMips.Register;

public class BranchAsm extends AsmInstr {
    public enum OP{
        bgt,
        blt,
        bge,
        ble,
        beq,
        bne
    }
    private OP op;
    private Register reg1;
    private Register reg2;
    private String label;
    private int num;
    public BranchAsm(Register reg1, Register reg2, String label, int num, OP op) {
        this.reg1 = reg1;
        this.reg2 = reg2;
        this.label = label;
        this.op=op;
        this.num=num;
    }

    public Register getReg1() {
        return reg1;
    }

    public Register getReg2() {
        return reg2;
    }

    public String toString(){
        if(reg2!=null){
            return op+" "+reg1+","+reg2+","+label;
        }
        else{
            return op+" "+reg1+","+num+","+label;
        }
    }
}
