package SyntaxParser.ExpNode;

import SyntaxParser.Node;
import SyntaxParser.OtherNode.TokenNode;

import java.util.ArrayList;

public class LAndExpNode extends Node {
    private ArrayList<EqExpNode> eqExpNodes;

    public LAndExpNode(int startLine, int endLine, ArrayList<EqExpNode> eqExpNodes) {
        super(startLine, endLine);
        this.eqExpNodes = eqExpNodes;
    }

    public ArrayList<EqExpNode> getEqExpNodes() {
        return eqExpNodes;
    }

    public String toString(){
        return "<LAndExp>\n";
    }
}
