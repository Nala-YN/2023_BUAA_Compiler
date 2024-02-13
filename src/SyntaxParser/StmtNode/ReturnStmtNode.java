package SyntaxParser.StmtNode;

import Symbol.SymbolCenter;
import SyntaxParser.ExpNode.EqExpNode;
import SyntaxParser.ExpNode.ExpNode;
import SyntaxParser.ExpNode.LAndExpNode;
import SyntaxParser.Node;
import TokenParser.TokenType;
import Util.ErrorType;
import Util.Printer;

public class ReturnStmtNode extends Node {
    private ExpNode expNode;
    public ReturnStmtNode(int startLine,int endLine,ExpNode expNode){
        super(startLine,endLine);
        this.expNode=expNode;
    }
    public ExpNode getExpNode(){
        return expNode;
    }
    public void checkError(){
        if(SymbolCenter.getFuncType()== TokenType.VOIDTK&&expNode!=null){
            Printer.addErrorMsg(ErrorType.f,getStartLine());
        }
    }
    public String toString(){
        return "<Stmt>\n";
    }
}
