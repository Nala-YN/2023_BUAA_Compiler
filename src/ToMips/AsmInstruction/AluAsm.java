package ToMips.AsmInstruction;

import ToMips.AsmInstr;
import ToMips.Register;

public class AluAsm extends AsmInstr {
    public enum OP{
        addu,
        addiu,
        subu,
        srl,
        sra,
        sll,
        mul,
        mult,
        div,
        madd
    }
    private Register operand1;
    private Register operand2;
    private OP op;
    private Register to;
    private int num;
    public AluAsm(Register operand1, Register operand2, OP op, Register to,int num) {
        this.operand1 = operand1;
        this.operand2 = operand2;
        this.op = op;
        this.to = to;
        this.num=num;
    }
    public String toString(){
        StringBuilder sb=new StringBuilder();
        sb.append(op).append(" ");
        if(op==OP.div||op==OP.mult){
            sb.append(operand1).append(",").append(operand2);
        }
        else{
            if(op==OP.madd){
                sb.append(operand1).append(",").append(operand2).append("\n\t")
                        .append("mfhi ").append(to);
            }
            else{
                sb.append(to).append(",").append(operand1).append(",");
                if(operand2==null){
                    sb.append(num);
                }
                else{
                    sb.append(operand2);
                }
            }
        }
        return sb.toString();
    }
    public Register getOperand1() {
        return operand1;
    }

    public Register getOperand2() {
        return operand2;
    }

    public OP getOp() {
        return op;
    }

    public Register getTo() {
        return to;
    }

    public int getNum() {
        return num;
    }
}
