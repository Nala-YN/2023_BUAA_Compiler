package SyntaxParser.OtherNode;

import SyntaxParser.ExpNode.EqExpNode;
import SyntaxParser.ExpNode.ExpNode;
import SyntaxParser.ExpNode.LAndExpNode;
import SyntaxParser.Node;

import java.util.ArrayList;

public class FuncRParamsNode extends Node {
    private ArrayList<ExpNode> expNodes;
    public FuncRParamsNode(int startLine,int endLine,ArrayList<ExpNode> expNodes){
        super(startLine,endLine);
        this.expNodes=expNodes;
    }

    public ArrayList<ExpNode> getExpNodes() {
        return expNodes;
    }

    public String toString(){
        return "<FuncRParams>\n";
    }
}
