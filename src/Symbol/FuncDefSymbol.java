package Symbol;

import LlvmIr.Global.Function;
import TokenParser.TokenType;

import java.util.ArrayList;

public class FuncDefSymbol extends Symbol{
    ArrayList<Integer> dims;
    ArrayList<Integer> secLens;
    int paraLen;
    TokenType defineType;
    Function llvmIr;
    public FuncDefSymbol(String symbolName, TokenType defineType) {
        super(symbolName);
        this.defineType = defineType;
    }
    public void setDims(ArrayList<Integer> dims){
        this.dims=dims;
        paraLen=dims.size();
    }
    public ArrayList<Integer> getDims() {
        return dims;
    }

    public ArrayList<Integer> getSecLens() {
        return secLens;
    }

    public void setLlvmIr(Function llvmIr) {
        this.llvmIr = llvmIr;
    }

    public int getParaLen() {
        return paraLen;
    }

    public TokenType getDefineType() {
        return defineType;
    }

    public Function getLlvmIr() {
        return llvmIr;
    }
}
