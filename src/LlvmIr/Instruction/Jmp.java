package LlvmIr.Instruction;

import LlvmIr.BasicBlock;
import LlvmIr.Instr;

public class Jmp extends Instr {
    public Jmp(BasicBlock toBlock,BasicBlock parent){
        super(null,null,InstrType.JUMP,parent);
        addOperand(toBlock);
    }
    public BasicBlock getToBlock(){
        return (BasicBlock) operands.get(0);
    }
    public String toString(){
        return "br label %" + operands.get(0).getName();
    }
}
