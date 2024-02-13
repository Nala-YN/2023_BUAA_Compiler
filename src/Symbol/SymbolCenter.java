package Symbol;

import TokenParser.TokenType;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Stack;

public class SymbolCenter {
    private static Stack<SymbolTable> symbolTables=new Stack<>();
    private static HashMap<String,Stack<SymbolTable>> symbolToValue=new HashMap<>();
    private static TokenType funcType;
    private static boolean funcDef=false;
    private static int loopDepth=0;
    public static boolean findSymbol(String name){
        return symbolToValue.containsKey(name);
    }
    public static boolean addSymbol(Symbol symbol){
        SymbolTable top=symbolTables.peek();
        if(top.findSymbol(symbol)){
            return false;
        }
        else{
            top.addSymbol(symbol);
            if(symbolToValue.containsKey(symbol.getSymbolName())){
                symbolToValue.get(symbol.getSymbolName()).push(top);
            }
            else{
                symbolToValue.put(symbol.getSymbolName(), new Stack<>());
                symbolToValue.get(symbol.getSymbolName()).push(top);
            }
        }
        return true;
    }

    public static boolean isFuncDef() {
        return funcDef;
    }

    public static void setFuncDef(boolean funcDef) {
        SymbolCenter.funcDef = funcDef;
    }

    public static void enterBlock(){
            SymbolTable top=new SymbolTable();
            symbolTables.push(top);
    }
    public static void leaveBlock(){
            SymbolTable top=symbolTables.peek();
            for(Symbol symbol:top.symbols){
                symbolToValue.get(symbol.getSymbolName()).pop();
                if(symbolToValue.get(symbol.getSymbolName()).empty()){
                    symbolToValue.remove(symbol.getSymbolName());
                }
            }
            symbolTables.pop();
    }
    public static void enterFunc(TokenType tokenType){
        enterBlock();
        funcType=tokenType;
        funcDef=true;
    }
    public static void leaveFunc(){
        leaveBlock();
        funcType=null;
        funcDef=false;
    }
    public static void enterLoop(){
        loopDepth++;
    }
    public static void leaveLoop(){
        loopDepth--;
    }

    public static int getLoopDepth() {
        return loopDepth;
    }

    public static Stack<SymbolTable> getSymbolTables() {
        return symbolTables;
    }

    public static HashMap<String, Stack<SymbolTable>> getSymbolToValue() {
        return symbolToValue;
    }
    public static TokenType getFuncType() {
        return funcType;
    }

    public static Symbol getSymbol(String name){
        if(!findSymbol(name))return null;
        return symbolToValue.get(name).peek().getSymbol(name);
    }
}
