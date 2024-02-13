package SyntaxParser.OtherNode;

import SyntaxParser.ExpNode.EqExpNode;
import SyntaxParser.ExpNode.LAndExpNode;
import SyntaxParser.Node;

public class BlockItemNode extends Node {
    private DeclNode declNode;
    private Node stmtNode;
    public BlockItemNode(int startLine,int endLine,DeclNode declNode,Node stmtNode){
        super(startLine,endLine);
        this.declNode=declNode;
        this.stmtNode=stmtNode;
    }

    public DeclNode getDeclNode() {
        return declNode;
    }
    public Node getStmtNode() {
        return stmtNode;
    }
}
