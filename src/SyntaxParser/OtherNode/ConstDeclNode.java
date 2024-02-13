package SyntaxParser.OtherNode;

import Symbol.ConstSymbol;
import SyntaxParser.ExpNode.EqExpNode;
import SyntaxParser.ExpNode.LAndExpNode;
import SyntaxParser.Node;

import java.util.ArrayList;

public class ConstDeclNode extends Node {
    private ArrayList<ConstDefNode> constDefNodes;
    public ConstDeclNode(int startLine, int endLine,  ArrayList<ConstDefNode> constDefNodes) {
        super(startLine, endLine);
        this.constDefNodes = constDefNodes;
    }

    public ArrayList<ConstDefNode> getConstDefNodes() {
        return constDefNodes;
    }

    public String toString(){
        return "<ConstDecl>\n";
    }
}
