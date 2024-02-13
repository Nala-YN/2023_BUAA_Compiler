package SyntaxParser.ExpNode;

import SyntaxParser.Node;

public class ExpNode extends Node {
    private AddExpNode addExpNode;
    public ExpNode(int startLine,int endLine,AddExpNode addExpNode){
        super(startLine,endLine);
        this.addExpNode=addExpNode;
    }
    public int getDim(){
        return addExpNode.getDim();
    }
    public int compute(){
        return addExpNode.compute();
    }

    public AddExpNode getAddExpNode() {
        return addExpNode;
    }

    public String toString(){
        return "<Exp>\n";
    }
}
