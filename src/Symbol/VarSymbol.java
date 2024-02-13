package Symbol;

import LlvmIr.Type.LlvmType;
import LlvmIr.Value;

import java.util.ArrayList;

public class VarSymbol extends Symbol{
    private int dim;
    private ArrayList<Integer> dimLens;  //-1说明为空
    private ArrayList<Integer> initial;
    private boolean isZeroInitial=false;
    private Value llvmIr;
    private LlvmType llvmType;

    public VarSymbol(String symbolName, int dim, ArrayList<Integer> dimLens,
                     ArrayList<Integer> initial, boolean isZeroInitial, LlvmType llvmType) {
        super(symbolName);
        this.dim = dim;
        this.dimLens = dimLens;
        this.initial = initial;
        this.isZeroInitial = isZeroInitial;
        this.llvmType = llvmType;
    }
    public VarSymbol(String symbolName, int dim, ArrayList<Integer> dimLens) {
        super(symbolName);
        this.dim = dim;
        this.dimLens = dimLens;
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
    public void setLlvmIr(Value llvmIr){
        this.llvmIr=llvmIr;
    }
    public ArrayList<Integer> getInitial() {
        return initial;
    }

    public boolean isZeroInitial() {
        return isZeroInitial;
    }

    public Value getLlvmIr() {
        return llvmIr;
    }

    public LlvmType getLlvmType() {
        return llvmType;
    }

    public int getDim() {
        return dim;
    }

    public ArrayList<Integer> getDimLens() {
        return dimLens;
    }
}
