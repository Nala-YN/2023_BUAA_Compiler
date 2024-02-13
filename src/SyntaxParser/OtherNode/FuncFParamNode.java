package SyntaxParser.OtherNode;

import LlvmIr.Type.LlvmType;
import Symbol.SymbolCenter;
import Symbol.VarSymbol;
import SyntaxParser.ExpNode.ConstExpNode;
import SyntaxParser.ExpNode.EqExpNode;
import SyntaxParser.ExpNode.LAndExpNode;
import SyntaxParser.Node;
import Util.ErrorType;
import Util.Printer;

import java.util.ArrayList;

public class FuncFParamNode extends Node {
    private BTypeNode bTypeNode;
    private TokenNode identNode;
    private ArrayList<ConstExpNode> constExpNodes; //为null说明为空
    private VarSymbol varSymbol;
    public FuncFParamNode(int startLine, int endLine, BTypeNode bTypeNode, TokenNode identNode, ArrayList<ConstExpNode> constExpNodes) {
        super(startLine, endLine);
        this.bTypeNode = bTypeNode;
        this.identNode = identNode;
        this.constExpNodes = constExpNodes;
    }
    public Integer getDim(){
        return constExpNodes.size();
    }
    public VarSymbol creatSymbol(){
        ArrayList<Integer> dimLens=new ArrayList<>();
        for(ConstExpNode constExpNode:constExpNodes){
            if(constExpNode==null){
                dimLens.add(-1);
            }
            else{
                dimLens.add(constExpNode.compute());
            }
        }
        return new VarSymbol(identNode.getToken().getValue(),getDim(),dimLens);
    }
    public void checkError(){
        this.varSymbol=creatSymbol();
        if(!SymbolCenter.addSymbol(varSymbol)){
            Printer.addErrorMsg(ErrorType.b, identNode.getStartLine());
        }
    }

    public VarSymbol getVarSymbol() {
        return varSymbol;
    }

    public String toString(){
        return "<FuncFParam>\n";
    }
}
