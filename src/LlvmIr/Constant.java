package LlvmIr;

import LlvmIr.Type.LlvmType;

public class Constant extends Value{
    private int value;
    public Constant(int value){
        super(String.valueOf(value), LlvmType.Int32);
        this.value=value;
    }

    public int getValue() {
        return value;
    }
}
