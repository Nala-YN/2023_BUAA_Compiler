package SyntaxParser.ExpNode;

import SyntaxParser.Node;
import SyntaxParser.OtherNode.LValNode;
import SyntaxParser.OtherNode.NumberNode;

public class PrimaryExpNode extends Node {
    private ExpNode expNode;
    private LValNode lValNode;
    private NumberNode numberNode;
    public PrimaryExpNode(int startLine,int endLine,ExpNode expNode,LValNode lValNode,NumberNode numberNode){
        super(startLine,endLine);
        this.expNode=expNode;
        this.lValNode=lValNode;
        this.numberNode=numberNode;
    }
    public int getDim(){
        if(numberNode!=null){
            return 0;
        }
        else if(lValNode!=null){
            return lValNode.getDim();
        }
        else{
            return expNode.getDim();
        }
    }
    public int compute(){
        if(expNode!=null){
            return expNode.compute();
        }
        else if(lValNode!=null){
            return lValNode.compute();
        }
        else{
            return numberNode.compute();
        }
    }

    public ExpNode getExpNode() {
        return expNode;
    }

    public LValNode getlValNode() {
        return lValNode;
    }

    public NumberNode getNumberNode() {
        return numberNode;
    }

    public String toString(){
        return "<PrimaryExp>\n";
    }
}
