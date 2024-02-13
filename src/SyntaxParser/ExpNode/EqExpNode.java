package SyntaxParser.ExpNode;

import SyntaxParser.Node;
import SyntaxParser.OtherNode.TokenNode;

import java.util.ArrayList;

public class EqExpNode extends Node {
    private ArrayList<RelExpNode> relExpNodes;
    private ArrayList<TokenNode> eqNodes;

    public EqExpNode(int startLine, int endLine, ArrayList<RelExpNode> relExpNodes, ArrayList<TokenNode> eqNodes) {
        super(startLine, endLine);
        this.relExpNodes = relExpNodes;
        this.eqNodes = eqNodes;
    }

    public ArrayList<RelExpNode> getRelExpNodes() {
        return relExpNodes;
    }

    public ArrayList<TokenNode> getEqNodes() {
        return eqNodes;
    }

    public String toString(){
        return "<EqExp>\n";
    }
}
