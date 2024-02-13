package SyntaxParser.OtherNode;

import SyntaxParser.ExpNode.EqExpNode;
import SyntaxParser.ExpNode.ExpNode;
import SyntaxParser.ExpNode.LAndExpNode;
import SyntaxParser.Node;

import java.util.ArrayList;

public class InitValNode extends Node {
    private ExpNode expNode;
    private ArrayList<InitValNode> initValNodes;

    public InitValNode(int startLine, int endLine, ExpNode expNode, ArrayList<InitValNode> initValNodes) {
        super(startLine, endLine);
        this.expNode = expNode;
        this.initValNodes = initValNodes;
    }
    public boolean isZeroInitial(){
        return expNode==null&&initValNodes.size()==0;
    }
    public ArrayList<Integer> compute(){
        ArrayList<Integer> initVals=new ArrayList<>();
        if(expNode!=null){
            initVals.add(expNode.compute());
        }
        if(initValNodes!=null){
            for(InitValNode initValNode:initValNodes){
                initVals.addAll(initValNode.compute());
            }
        }
        return initVals;
    }

    public ExpNode getExpNode() {
        return expNode;
    }

    public ArrayList<InitValNode> getInitValNodes() {
        return initValNodes;
    }

    public String toString(){
        return "<InitVal>\n";
    }
}
