package SyntaxParser.StmtNode;

import SyntaxParser.ExpNode.EqExpNode;
import SyntaxParser.ExpNode.LAndExpNode;
import SyntaxParser.Node;
import SyntaxParser.OtherNode.CondNode;

public class IfStmtNode extends Node {
    private CondNode condNode;
    private Node stmtNode;
    private Node elseStmtNode;
    public IfStmtNode(int startLine,int endLine,CondNode condNode,Node stmtNode,Node elseStmtNode){
        super(startLine,endLine);
        this.condNode=condNode;
        this.stmtNode=stmtNode;
        this.elseStmtNode=elseStmtNode;
    }

    public CondNode getCondNode() {
        return condNode;
    }

    public Node getStmtNode() {
        return stmtNode;
    }

    public Node getElseStmtNode() {
        return elseStmtNode;
    }

    public String toString(){
        return "<Stmt>\n";
    }
}
