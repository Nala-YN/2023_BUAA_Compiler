package SyntaxParser.ExpNode;

import SyntaxParser.Node;
import SyntaxParser.OtherNode.TokenNode;

import java.util.ArrayList;

public class RelExpNode extends Node {
    private ArrayList<AddExpNode> addExpNodes;
    private ArrayList<TokenNode> tokenNodes;

    public RelExpNode(int startLine, int endLine, ArrayList<AddExpNode> addExpNodes, ArrayList<TokenNode> tokenNodes) {
        super(startLine, endLine);
        this.tokenNodes = tokenNodes;
        this.addExpNodes = addExpNodes;
    }

    public ArrayList<AddExpNode> getAddExpNodes() {
        return addExpNodes;
    }

    public ArrayList<TokenNode> getTokenNodes() {
        return tokenNodes;
    }

    public String toString(){
        return "<RelExp>\n";
    }
}
