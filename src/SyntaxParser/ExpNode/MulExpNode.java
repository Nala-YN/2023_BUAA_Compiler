package SyntaxParser.ExpNode;

import SyntaxParser.Node;
import SyntaxParser.OtherNode.TokenNode;
import TokenParser.TokenType;

import java.util.ArrayList;

public class MulExpNode extends Node {
    private ArrayList<TokenNode> tokenNodes;//*/%
    private ArrayList<UnaryExpNode> unaryExpNodes;

    public MulExpNode(int startLine, int endLine, ArrayList<TokenNode> tokenNodes, ArrayList<UnaryExpNode> unaryExpNodes) {
        super(startLine, endLine);
        this.tokenNodes = tokenNodes;
        this.unaryExpNodes = unaryExpNodes;
    }
    public int getDim(){
        return unaryExpNodes.get(0).getDim();
    }
    public int compute(){
        int value=unaryExpNodes.get(0).compute();
        for(int i=1;i<=unaryExpNodes.size()-1;i++){
            int temp=unaryExpNodes.get(i).compute();
            TokenType tokenType=tokenNodes.get(i-1).getToken().getType();
            if(tokenType== TokenType.MULT){
                value*=temp;
            }
            else if(tokenType==TokenType.DIV){
                value/=temp;
            }
            else{
                value%=temp;
            }
        }
        return value;
    }

    public ArrayList<TokenNode> getTokenNodes() {
        return tokenNodes;
    }

    public ArrayList<UnaryExpNode> getUnaryExpNodes() {
        return unaryExpNodes;
    }

    public String toString(){
        return "<MulExp>\n";
    }
}
