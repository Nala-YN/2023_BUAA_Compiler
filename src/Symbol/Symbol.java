package Symbol;

import LlvmIr.Type.LlvmType;

public class Symbol {
    String symbolName;

    public Symbol(String symbolName) {
        this.symbolName = symbolName;
    }

    @Override
    public boolean equals(Object o){
        return symbolName.equals(((Symbol)o).getSymbolName());
    }
    @Override
    public int hashCode(){
        return symbolName.hashCode();
    }
    public String getSymbolName() {
        return symbolName;
    }
}
