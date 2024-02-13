package SyntaxParser.ExpNode;

import SyntaxParser.Node;

public class ConstExpNode extends Node {
    private AddExpNode addExpNode;
    private int val;
    public ConstExpNode(int startLine,int endLine,AddExpNode addExpNode){
        super(startLine,endLine);
        this.addExpNode=addExpNode;
    }
    public int compute(){
        val=addExpNode.compute();
        return addExpNode.compute();
    }

    public int getVal() {
        return val;
    }

    @Override
    public String toString(){
        return "<ConstExp>\n";
    }
}
