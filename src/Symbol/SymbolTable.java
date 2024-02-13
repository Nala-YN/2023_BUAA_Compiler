package Symbol;

import java.util.HashSet;

public class SymbolTable {
    HashSet<Symbol> symbols;

    public SymbolTable() {
        this.symbols = new HashSet<>();
    }

    public boolean findSymbol(Symbol symbol){
        return symbols.contains(symbol);
    }
    public Symbol getSymbol(String name){
        for(Symbol symbol:symbols){
            if(symbol.getSymbolName().equals(name)){
                return symbol;
            }
        }
        return null;
    }
    public void addSymbol(Symbol symbol){
        symbols.add(symbol);
    }
}
