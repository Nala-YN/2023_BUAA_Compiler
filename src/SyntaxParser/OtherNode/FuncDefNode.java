package SyntaxParser.OtherNode;

import Symbol.FuncDefSymbol;
import Symbol.SymbolCenter;
import SyntaxParser.ExpNode.EqExpNode;
import SyntaxParser.ExpNode.LAndExpNode;
import SyntaxParser.Node;
import Util.ErrorType;
import Util.Printer;

import java.util.ArrayList;

public class FuncDefNode extends Node {
    private FuncTypeNode funcTypeNode;
    private TokenNode identNode;
    private FuncFParamsNode funcFParamsNode;
    private BlockNode blockNode;
    private FuncDefSymbol funcDefSymbol;
    public FuncDefNode(int startLine, int endLine, FuncTypeNode funcTypeNode,
                       TokenNode identNode) {
        super(startLine, endLine);
        this.funcTypeNode = funcTypeNode;
        this.identNode = identNode;
    }
    public void setBlockNode(BlockNode blockNode) {
        this.blockNode = blockNode;
    }
    public void setFuncFParamsNode(FuncFParamsNode funcFParamsNode){
        this.funcFParamsNode=funcFParamsNode;
        funcDefSymbol.setDims(funcFParamsNode==null?new ArrayList<>():funcFParamsNode.getDims());
    }
    public FuncDefSymbol creatSymbol(){
        funcDefSymbol=new FuncDefSymbol(identNode.getToken().getValue(),funcTypeNode.getVoidIntNode().getToken().getType());
        return funcDefSymbol;
    }
    public void checkError(){
        this.funcDefSymbol=creatSymbol();
        if(!SymbolCenter.addSymbol(funcDefSymbol)){
            Printer.addErrorMsg(ErrorType.b,identNode.getStartLine());
        }
    }

    public FuncDefSymbol getFuncDefSymbol() {
        return funcDefSymbol;
    }

    public FuncFParamsNode getFuncFParamsNode() {
        return funcFParamsNode;
    }

    public BlockNode getBlockNode() {
        return blockNode;
    }

    public String toString(){
        return "<FuncDef>\n";
    }
}
