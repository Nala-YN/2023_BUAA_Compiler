package ToMips.AsmInstruction;

import LlvmIr.Instruction.Load;
import LlvmIr.Instruction.Store;
import ToMips.AsmInstr;
import ToMips.Register;

import java.util.ArrayList;

public class JumpAsm extends AsmInstr {
    public enum OP{
        jal,
        jr,
        j
    }
    private Register to;
    private String label;
    private OP op;
    private ArrayList<MemAsm> lws;
    private ArrayList<MemAsm> sws;
    public JumpAsm(Register to, String label, OP op) {
        this.to = to;
        this.label = label;
        this.op = op;
    }

    public OP getOp() {
        return op;
    }

    public ArrayList<MemAsm> getLws() {
        return lws;
    }

    public void setLws(ArrayList<MemAsm> lws) {
        this.lws = lws;
    }

    public ArrayList<MemAsm> getSws() {
        return sws;
    }

    public void setSws(ArrayList<MemAsm> sws) {
        this.sws = sws;
    }

    public String getLabel() {
        return label;
    }

    public Register getTo() {
        return to;
    }

    public String toString(){
        if(op==OP.jr){
            return op+" "+to;
        }
        else{
            return op+" "+label;
        }
    }
}
