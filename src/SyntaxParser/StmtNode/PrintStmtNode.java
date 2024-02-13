package SyntaxParser.StmtNode;

import SyntaxParser.ExpNode.EqExpNode;
import SyntaxParser.ExpNode.ExpNode;
import SyntaxParser.ExpNode.LAndExpNode;
import SyntaxParser.Node;
import SyntaxParser.OtherNode.TokenNode;
import Util.ErrorType;
import Util.Printer;

import java.util.ArrayList;

public class PrintStmtNode extends Node {
    private TokenNode fmtStrNode;
    private ArrayList<ExpNode> expNodes;

    public PrintStmtNode(int startLine, int endLine, TokenNode fmtStrNode, ArrayList<ExpNode> expNodes) {
        super(startLine, endLine);
        this.fmtStrNode = fmtStrNode;
        this.expNodes = expNodes;
    }

    public TokenNode getFmtStrNode() {
        return fmtStrNode;
    }

    public ArrayList<ExpNode> getExpNodes() {
        return expNodes;
    }

    public String toString() {
        return "<Stmt>\n";
    }
}
