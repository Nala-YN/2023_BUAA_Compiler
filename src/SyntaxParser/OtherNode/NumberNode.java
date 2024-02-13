package SyntaxParser.OtherNode;

import SyntaxParser.ExpNode.EqExpNode;
import SyntaxParser.ExpNode.LAndExpNode;
import SyntaxParser.Node;

public class NumberNode extends Node {
    private TokenNode intConst;
    public NumberNode(int startLine,int endLine,TokenNode intConst){
        super(startLine,endLine);
        this.intConst=intConst;
    }

    public TokenNode getIntConst() {
        return intConst;
    }

    public int compute(){
        return Integer.parseInt(intConst.getToken().getValue());
    }
    public String toString(){
        return "<Number>\n";
    }
}
