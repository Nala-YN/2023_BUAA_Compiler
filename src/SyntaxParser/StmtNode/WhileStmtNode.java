package SyntaxParser.StmtNode;

import SyntaxParser.Node;
import SyntaxParser.OtherNode.CondNode;

public class WhileStmtNode extends Node {
    private CondNode condNode;
    private Node stmtNode;

    public WhileStmtNode(int startLine, int endLine, CondNode condNode, Node stmtNode) {
        super(startLine, endLine);
        this.condNode = condNode;
        this.stmtNode = stmtNode;
    }

    public CondNode getCondNode() {
        return condNode;
    }

    public Node getStmtNode() {
        return stmtNode;
    }

    public String toString(){
        return "<Stmt>\n";
    }
}
