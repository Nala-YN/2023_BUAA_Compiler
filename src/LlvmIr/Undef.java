package LlvmIr;

import LlvmIr.Type.LlvmType;

public class Undef extends Constant{
    public Undef() {
        super(0);
    }

    public String toString(){
        return "undef";
    }
}
