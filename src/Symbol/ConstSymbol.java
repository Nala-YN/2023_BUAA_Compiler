package Symbol;

import LlvmIr.Type.LlvmType;
import LlvmIr.Value;

import java.util.ArrayList;

public class ConstSymbol extends Symbol{
    private int dim;
    private ArrayList<Integer> dimLens;
    private ArrayList<Integer> initial;
    private boolean isZeroInitial;
    private LlvmType llvmType;
    private Value LlvmIr;
    public ConstSymbol(String name,int dim, ArrayList<Integer> dimLens,
                       ArrayList<Integer> initial,boolean isZeroInitial,LlvmType llvmType) {
        super(name);
        this.dim = dim;
        this.dimLens = dimLens;
        this.initial = initial;
        this.isZeroInitial=isZeroInitial;
        this.llvmType=llvmType;
    }
    public int getValue(int var1,int var2){
        if(dim==0){
            return initial.get(0);
        }
        else if(dim==1){
            return initial.get(var1);
        }
        else if(dim==2){
            return initial.get(var1*dimLens.get(0)+var2);
        }
        return 0;
    }

    public int getDim() {
        return dim;
    }

    public ArrayList<Integer> getDimLens() {
        return dimLens;
    }

    public ArrayList<Integer> getInitial() {
        return initial;
    }

    public void setLlvmIr(Value llvmIr) {
        LlvmIr = llvmIr;
    }

    public LlvmType getLlvmType() {
        return llvmType;
    }

    public Value getLlvmIr() {
        return LlvmIr;
    }

    public boolean isZeroInitial() {
        return isZeroInitial;
    }

}
