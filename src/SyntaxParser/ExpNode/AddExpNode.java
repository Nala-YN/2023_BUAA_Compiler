package SyntaxParser.ExpNode;

import SyntaxParser.Node;
import SyntaxParser.OtherNode.TokenNode;
import TokenParser.TokenType;

import java.util.ArrayList;

public class AddExpNode extends Node {
    private ArrayList<TokenNode> addMinusNodes;
    private ArrayList<MulExpNode> mulExpNodes;

    public AddExpNode(int startLine, int endLine, ArrayList<TokenNode> addMinusNodes, ArrayList<MulExpNode> mulExpNodes) {
        super(startLine, endLine);
        this.addMinusNodes = addMinusNodes;
        this.mulExpNodes = mulExpNodes;
    }
    public int getDim(){
        return mulExpNodes.get(0).getDim();
    }
    public int compute(){
        int value=mulExpNodes.get(0).compute();
        for(int i=1;i<=mulExpNodes.size()-1;i++){
            int temp=mulExpNodes.get(i).compute();
            if(addMinusNodes.get(i-1).getToken().getType()== TokenType.PLUS){
                value+=temp;
            }
            else{
                value-=temp;
            }
        }
        return value;
    }

    public ArrayList<TokenNode> getAddMinusNodes() {
        return addMinusNodes;
    }

    public ArrayList<MulExpNode> getMulExpNodes() {
        return mulExpNodes;
    }

    public String toString(){
        return "<AddExp>\n";
    }
}
