package SyntaxParser.OtherNode;

import SyntaxParser.ExpNode.ConstExpNode;
import SyntaxParser.ExpNode.EqExpNode;
import SyntaxParser.ExpNode.LAndExpNode;
import SyntaxParser.Node;

import java.util.ArrayList;
// ConstInitVal → ConstExp
//| '{' [ ConstInitVal { ',' ConstInitVal } ] '}'
public class ConstInitValNode extends Node {
    private ConstExpNode constExpNode;
    private ArrayList<ConstInitValNode> constInitValNodes;

    public ConstInitValNode(int startLine, int endLine, ConstExpNode constExpNode, ArrayList<ConstInitValNode> constInitValNodes) {
        super(startLine, endLine);
        this.constExpNode = constExpNode;
        this.constInitValNodes = constInitValNodes;
    }
    public ArrayList<Integer> compute(){
        ArrayList<Integer> initial=new ArrayList<>();
        if(constExpNode!=null){
            initial.add(constExpNode.compute());
        }
        else{
            for(ConstInitValNode constInitValNode:constInitValNodes){
                initial.addAll(constInitValNode.compute());
            }
        }
        return initial;
    }

    public ConstExpNode getConstExpNode() {
        return constExpNode;
    }

    public ArrayList<ConstInitValNode> getConstInitValNodes() {
        return constInitValNodes;
    }

    public String toString(){
        return "<ConstInitVal>\n";
    }
}
