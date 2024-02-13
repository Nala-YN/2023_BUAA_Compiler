package LlvmIr;

import LlvmIr.Global.Function;
import LlvmIr.Type.LlvmType;

public class Param extends Value{
    private Function parentFunc;

    public Param(String name, LlvmType type, Function parentFunc) {
        super(name, type);
        this.parentFunc = parentFunc;
    }
    public String toString(){
        return type+" "+name;
    }
}
