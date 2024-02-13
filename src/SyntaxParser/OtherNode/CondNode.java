package SyntaxParser.OtherNode;

import SyntaxParser.ExpNode.LOrExpNode;
import SyntaxParser.Node;

public class CondNode extends Node {
    private LOrExpNode lOrExpNode;
    public CondNode(int startLine,int endLine,LOrExpNode lOrExpNode){
        super(startLine,endLine);
        this.lOrExpNode=lOrExpNode;
    }

    public LOrExpNode getlOrExpNode() {
        return lOrExpNode;
    }

    public String toString(){
        return "<Cond>\n";
    }
}
