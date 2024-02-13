package LlvmIr.Instruction;

import LlvmIr.BasicBlock;
import LlvmIr.Instr;
import LlvmIr.Type.LlvmType;
import LlvmIr.Type.PointerType;

import java.util.ArrayList;

public class Alloca extends Instr {
    private LlvmType pointedType;
    private boolean isConst=false;
    private ArrayList<Integer> initial;
    public Alloca(String name,  BasicBlock parentBlock, LlvmType pointedType) {
        super(name, new PointerType(pointedType), InstrType.ALLOCA, parentBlock);
        this.pointedType = pointedType;
    }
    public Alloca(String name,  BasicBlock parentBlock, LlvmType pointedType,ArrayList<Integer> initial) {
        super(name, new PointerType(pointedType), InstrType.ALLOCA, parentBlock);
        this.pointedType = pointedType;
        this.isConst=true;
        this.initial=initial;
    }

    public boolean isConst() {
        return isConst;
    }

    public ArrayList<Integer> getInitial() {
        return initial;
    }

    public LlvmType getPointedType() {
        return pointedType;
    }

    public String toString() {
        return name + " = alloca " + pointedType;
    }
}