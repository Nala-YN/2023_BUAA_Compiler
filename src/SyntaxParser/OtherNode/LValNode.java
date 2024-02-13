package SyntaxParser.OtherNode;

import Symbol.ConstSymbol;
import Symbol.VarSymbol;
import Symbol.Symbol;
import Symbol.SymbolCenter;
import SyntaxParser.ExpNode.EqExpNode;
import SyntaxParser.ExpNode.ExpNode;
import SyntaxParser.ExpNode.LAndExpNode;
import SyntaxParser.Node;
import Util.ErrorType;
import Util.Printer;

import java.util.ArrayList;

public class LValNode extends Node {
    private TokenNode identNode;
    private ArrayList<ExpNode> expNodes;
    public LValNode(int startLine,int endLine,TokenNode identNode,ArrayList<ExpNode> expNodes){
        super(startLine,endLine);
        this.identNode=identNode;
        this.expNodes=expNodes;
    }
    public void checkError(){
        if(!SymbolCenter.findSymbol(identNode.getToken().getValue())){
            Printer.addErrorMsg(ErrorType.c,identNode.getStartLine());
        }
    }
    public int getDim(){
        Symbol symbol=SymbolCenter.getSymbol(identNode.getToken().getValue());
        if(symbol==null)return 0;
        if(symbol instanceof ConstSymbol){
            return ((ConstSymbol)symbol).getDim()-expNodes.size();
        }
        else{
            return ((VarSymbol)symbol).getDim()-expNodes.size();
        }
    }
    public int compute(){
        Symbol symbol= SymbolCenter.getSymbol(identNode.getToken().getValue());
        int[] var=new int[2];
        for(int i=0;i<=expNodes.size()-1;i++){
            var[i]=expNodes.get(i).compute();
        }
        if(symbol instanceof ConstSymbol){
           return ((ConstSymbol) symbol).getValue(var[0],var[1]);
        }
        else{
            return ((VarSymbol)symbol).getValue(var[0],var[1]);
        }
    }

    public TokenNode getIdentNode() {
        return identNode;
    }

    public ArrayList<ExpNode> getExpNodes() {
        return expNodes;
    }

    public String toString(){
        return "<LVal>\n";
    }
}
