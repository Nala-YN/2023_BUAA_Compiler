package LlvmIr.Type;

public class ArrayType extends LlvmType{
    private int size;
    private LlvmType eleType;

    public ArrayType(int size, LlvmType eleType) {
        this.size = size;
        this.eleType = eleType;
    }

    public int getSize() {
        return size;
    }

    public LlvmType getEleType() {
        return eleType;
    }
    public String toString() {
        return "[" + size + " x " + eleType + "]";
    }
}
