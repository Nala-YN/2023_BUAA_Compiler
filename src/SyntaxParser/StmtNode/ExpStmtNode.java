package SyntaxParser.StmtNode;

import SyntaxParser.ExpNode.EqExpNode;
import SyntaxParser.ExpNode.ExpNode;
import SyntaxParser.ExpNode.LAndExpNode;
import SyntaxParser.Node;

public class ExpStmtNode extends Node {
    private ExpNode expNode;
    public ExpStmtNode(int startLine,int endLine,ExpNode expNode){
        super(startLine,endLine);
        this.expNode=expNode;
    }

    public ExpNode getExpNode() {
        return expNode;
    }

    public String toString(){
        return "<Stmt>\n";
    }
}
