package SyntaxParser.OtherNode;

import SyntaxParser.ExpNode.EqExpNode;
import SyntaxParser.ExpNode.LAndExpNode;
import SyntaxParser.Node;

public class UnaryOpNode extends Node {
    private TokenNode tokenNode;
    public UnaryOpNode(int startLine,int endLine,TokenNode tokenNode){
        super(startLine,endLine);
        this.tokenNode=tokenNode;
    }

    public TokenNode getTokenNode() {
        return tokenNode;
    }

    public String toString(){
        return "<UnaryOp>\n";
    }
}
