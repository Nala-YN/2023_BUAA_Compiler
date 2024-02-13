package SyntaxParser.StmtNode;

import SyntaxParser.ExpNode.EqExpNode;
import SyntaxParser.ExpNode.LAndExpNode;
import SyntaxParser.Node;

public class BreakStmtNode extends Node {

    public BreakStmtNode(int startLine,int endLine){
        super(startLine,endLine);
    }
    public String toString(){
        return "<Stmt>\n";
    }
}
