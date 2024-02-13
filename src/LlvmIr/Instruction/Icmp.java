package LlvmIr.Instruction;

import LlvmIr.BasicBlock;
import LlvmIr.Instr;
import LlvmIr.Type.LlvmType;
import LlvmIr.User;
import LlvmIr.Value;

public class Icmp extends Instr {
    public enum OP {
        EQ,
        NE,
        SLT,
        SLE,
        SGT,
        SGE
    }
    private OP op;
    public Icmp(Value v1, Value v2, String name, BasicBlock parent,OP op){
        super(name, LlvmType.Int1,InstrType.ICMP,parent);
        addOperand(v1);
        addOperand(v2);
        this.op=op;
    }
    public String getGvnHash(){
        String name1=operands.get(0).getName();
        String name2=operands.get(1).getName();
        OP temp = op;
        if(name1.compareTo(name2)<0){
            name1=operands.get(1).getName();
            name2=operands.get(0).getName();
            if(op==OP.SGE){
                temp=OP.SLE;
            }
            else if(op==OP.SLE){
                temp=OP.SGE;
            }
            else if(op==OP.SGT){
                temp=OP.SLT;
            }
            else if(op==OP.SLT){
                temp=OP.SGT;
            }
        }
        return name1+temp+name2;
    }
    public LlvmType cmpType(){
        return operands.get(0).getLlvmType();
    }
    public String toString(){
        String type=cmpType()==LlvmType.Int32?"i32":"i1";
        return name+" = icmp "+op.toString().toLowerCase()+" "+type+" "
                +operands.get(0).getName()+","+operands.get(1).getName();
    }
    public boolean onlyBranchUse(){
        boolean onlyBranchUse = true;
        for (User user : this.getUsers()) {
            if (!(user instanceof Branch)) {
                onlyBranchUse = false;
                break;
            }
        }
        return onlyBranchUse;
    }
    public OP getOp() {
        return op;
    }
}
