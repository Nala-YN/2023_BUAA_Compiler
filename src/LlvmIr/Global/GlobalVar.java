package LlvmIr.Global;

import LlvmIr.Initial;
import LlvmIr.Type.LlvmType;
import LlvmIr.Type.PointerType;
import LlvmIr.User;

import java.util.ArrayList;

public class GlobalVar extends User {
    private ArrayList<Integer> initial;
    private boolean isZeroInitial;
    private int len;
    private boolean isConst;
    public GlobalVar(String name, LlvmType type, ArrayList<Integer> initial, boolean isZeroInitial, int len,boolean isConst) {
        super(name, type); //type为对Symbol的类型的point类型，symbol类型为array或int32
        this.initial = initial;
        this.isZeroInitial = isZeroInitial;
        this.len = len;
        if(isZeroInitial){
            for(int i=1;i<=len;i++){
                this.initial.add(0);
            }
        }
        this.isConst=isConst;
    }
    @Override
    public String toString(){
        StringBuilder initStr;
        if(((PointerType)type).getPointedType().isArray()){
            initStr = new StringBuilder("[" + len + " x i32] ");
            if(isZeroInitial){
                initStr.append("zeroinitializer");
            }
            else{
                initStr.append("[");
                initStr.append("i32 ").append(initial.get(0));
                for(int i=1;i<=initial.size()-1;i++){
                    initStr.append(",i32 ").append(initial.get(i));
                }
                initStr.append("]");
            }
        }
        else{
            if(isZeroInitial){
                initStr = new StringBuilder("i32 0");
            }
            else{
                initStr = new StringBuilder("i32 " + initial.get(0));
            }
        }
        return name + " = dso_local global " + initStr;
    }

    public boolean isConst() {
        return isConst;
    }

    public boolean isZeroInitial() {
        return isZeroInitial;
    }

    public int getLen() {
        return len;
    }

    public ArrayList<Integer> getInitial() {
        return initial;
    }
}
