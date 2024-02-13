package SyntaxParser.StmtNode;

import SyntaxParser.ExpNode.ExpNode;
import SyntaxParser.Node;
import SyntaxParser.OtherNode.LValNode;

public class ForAssignStmtNode extends Node {
    private LValNode lValNode;
    private ExpNode expNode;
    public ForAssignStmtNode(int startLine,int endLine,LValNode lValNode,ExpNode expNode){
        super(startLine,endLine);
        this.lValNode=lValNode;
        this.expNode=expNode;
    }

    public LValNode getlValNode() {
        return lValNode;
    }

    public ExpNode getExpNode() {
        return expNode;
    }

    public String toString(){
        return "<ForStmt>\n";
    }
}
