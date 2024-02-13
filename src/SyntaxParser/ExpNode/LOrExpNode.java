package SyntaxParser.ExpNode;

import SyntaxParser.Node;
import SyntaxParser.OtherNode.TokenNode;

import java.util.ArrayList;

public class LOrExpNode extends Node {
    private ArrayList<LAndExpNode> lAndExpNodes;

    public LOrExpNode(int startLine, int endLine, ArrayList<LAndExpNode> lAndExpNodes) {
        super(startLine, endLine);
        this.lAndExpNodes = lAndExpNodes;
    }

    public ArrayList<LAndExpNode> getlAndExpNodes() {
        return lAndExpNodes;
    }

    public String toString(){
        return "<LOrExp>\n";
    }
}
