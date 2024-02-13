package SyntaxParser.OtherNode;

import SyntaxParser.ExpNode.EqExpNode;
import SyntaxParser.ExpNode.LAndExpNode;
import SyntaxParser.Node;

public class FuncTypeNode extends Node {
    private TokenNode voidIntNode;

    public FuncTypeNode(int startLine, int endLine, TokenNode voidIntNode) {
        super(startLine, endLine);
        this.voidIntNode = voidIntNode;
    }

    public TokenNode getVoidIntNode() {
        return voidIntNode;
    }

    public String toString(){
        return "<FuncType>\n";
    }
}
