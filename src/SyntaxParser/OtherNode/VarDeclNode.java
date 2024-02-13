package SyntaxParser.OtherNode;

import Symbol.VarSymbol;
import SyntaxParser.ExpNode.EqExpNode;
import SyntaxParser.ExpNode.LAndExpNode;
import SyntaxParser.Node;

import java.util.ArrayList;

public class VarDeclNode extends Node {
    private ArrayList<VarDefNode> varDefNodes;
    public VarDeclNode(int startLine, int endLine, ArrayList<VarDefNode> varDefNodes) {
        super(startLine, endLine);
        this.varDefNodes = varDefNodes;
    }

    public ArrayList<VarDefNode> getVarDefNodes() {
        return varDefNodes;
    }

    public String toString(){
        return "<VarDecl>\n";
    }
}
