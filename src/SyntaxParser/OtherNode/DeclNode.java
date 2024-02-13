package SyntaxParser.OtherNode;

import SyntaxParser.ExpNode.EqExpNode;
import SyntaxParser.ExpNode.LAndExpNode;
import SyntaxParser.Node;

public class DeclNode extends Node {
    private ConstDeclNode constDeclNode;
    private VarDeclNode varDeclNode;

    public DeclNode(int startLine, int endLine, ConstDeclNode constDeclNode, VarDeclNode varDeclNode) {
        super(startLine, endLine);
        this.constDeclNode = constDeclNode;
        this.varDeclNode = varDeclNode;
    }

    public ConstDeclNode getConstDeclNode() {
        return constDeclNode;
    }

    public VarDeclNode getVarDeclNode() {
        return varDeclNode;
    }

    public String toString(){
        return "<Decl>\n";
    }
}
