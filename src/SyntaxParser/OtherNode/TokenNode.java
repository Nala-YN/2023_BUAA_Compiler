package SyntaxParser.OtherNode;

import SyntaxParser.Node;
import TokenParser.Token;

public class TokenNode extends Node {
    private Token token;
    public TokenNode(int startLine,int endLine,Token token){
        super(startLine, endLine);
        this.token=token;
    }
    public Token getToken(){
        return this.token;
    }
}
