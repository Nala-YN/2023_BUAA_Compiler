package SyntaxParser.StmtNode;

import SyntaxParser.Node;
import SyntaxParser.OtherNode.CondNode;

public class ForStmtNode extends Node {
    private ForAssignStmtNode leftAssign;
    private CondNode condNode;
    private ForAssignStmtNode rightAssign;
    private Node stmtNode;

    public ForStmtNode(int startLine, int endLine, ForAssignStmtNode leftAssign, CondNode condNode, ForAssignStmtNode rightAssign, Node stmtNode) {
        super(startLine, endLine);
        this.leftAssign = leftAssign;
        this.condNode = condNode;
        this.rightAssign = rightAssign;
        this.stmtNode = stmtNode;
    }

    public ForAssignStmtNode getLeftAssign() {
        return leftAssign;
    }

    public CondNode getCondNode() {
        return condNode;
    }

    public ForAssignStmtNode getRightAssign() {
        return rightAssign;
    }

    public Node getStmtNode() {
        return stmtNode;
    }

    public String toString(){
        return "<Stmt>\n";
    }
}
