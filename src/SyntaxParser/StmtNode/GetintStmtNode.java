package SyntaxParser.StmtNode;

import SyntaxParser.ExpNode.EqExpNode;
import SyntaxParser.ExpNode.LAndExpNode;
import SyntaxParser.Node;
import SyntaxParser.OtherNode.LValNode;

public class GetintStmtNode extends Node {
    private LValNode lValNode;
    public GetintStmtNode(int startLine,int endLine,LValNode lValNode){
        super(startLine,endLine);
        this.lValNode=lValNode;
    }

    public LValNode getlValNode() {
        return lValNode;
    }

    public String toString(){
        return "<Stmt>\n";
    }
}
