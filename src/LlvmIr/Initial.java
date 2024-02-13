package LlvmIr;

import LlvmIr.Type.LlvmType;

import java.util.ArrayList;

public class Initial {
    private LlvmType type;
    private ArrayList<Integer> values;

    public Initial(LlvmType type, ArrayList<Integer> values) {
        this.type = type;
        this.values = values;
    }
}
