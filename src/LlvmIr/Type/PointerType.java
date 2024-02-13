package LlvmIr.Type;

public class PointerType extends LlvmType{
    private LlvmType pointedType;

    public PointerType(LlvmType pointedType) {
        this.pointedType = pointedType;
    }

    public LlvmType getPointedType() {
        return pointedType;
    }
    public String toString(){
        return pointedType+"*";
    }
}
