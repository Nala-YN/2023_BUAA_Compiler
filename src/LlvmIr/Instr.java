package LlvmIr;

import LlvmIr.Instruction.Alloca;
import LlvmIr.Instruction.Alu;
import LlvmIr.Instruction.Call;
import LlvmIr.Instruction.GetPtr;
import LlvmIr.Instruction.IO.GetInt;
import LlvmIr.Instruction.Icmp;
import LlvmIr.Instruction.Load;
import LlvmIr.Instruction.OffsetLoad;
import LlvmIr.Instruction.Phi;
import LlvmIr.Instruction.Zext;
import LlvmIr.Type.LlvmType;

public class Instr extends User {
    public static enum InstrType {
        ALU,
        ALLOCA,
        BRANCH,
        CALL,
        GETPTR,
        ICMP,
        JUMP,
        LOAD,
        RETURN,
        STORE,
        ZEXT,
        GETINT,
        PUTSTR,
        PUTINT,
        PHI,
        MOVE,
        OFFSETLOAD,
        OFFSETSTORE,
    }
    private InstrType instrType;
    private BasicBlock parentBlock;

    public Instr(String name, LlvmType type, InstrType instrType, BasicBlock parentBlock) {
        super(name, type);
        this.instrType = instrType;
        this.parentBlock = parentBlock;
    }
    public boolean hasLVal(){
        return this instanceof GetInt ||
                this instanceof Alloca ||
                this instanceof Alu ||
                (this instanceof Call && this.getLlvmType()!=LlvmType.Void) ||
                this instanceof GetPtr ||
                this instanceof OffsetLoad ||
                this instanceof Icmp ||
                this instanceof Load ||
                this instanceof Phi ||
                this instanceof Zext;
    }
    public InstrType getInstrType() {
        return instrType;
    }
    public String getGvnHash(){
        return "error";
    }
    public BasicBlock getParentBlock() {
        return parentBlock;
    }

    public void setParentBlock(BasicBlock parentBlock) {
        this.parentBlock = parentBlock;
    }
}
