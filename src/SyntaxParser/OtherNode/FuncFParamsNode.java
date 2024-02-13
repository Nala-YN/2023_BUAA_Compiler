package SyntaxParser.OtherNode;


import SyntaxParser.Node;

import java.util.ArrayList;

public class FuncFParamsNode extends Node {
    private ArrayList<FuncFParamNode> funcFParamNodes;

    public FuncFParamsNode(int startLine, int endLine, ArrayList<FuncFParamNode> funcFParamNodes) {
        super(startLine, endLine);
        this.funcFParamNodes = funcFParamNodes;
    }
    public ArrayList<Integer> getDims(){
        ArrayList<Integer> dims=new ArrayList<>();
        for(FuncFParamNode funcFParamNode:funcFParamNodes){
            dims.add(funcFParamNode.getDim());
        }
        return dims;
    }

    public ArrayList<FuncFParamNode> getFuncFParamNodes() {
        return funcFParamNodes;
    }

    public String toString(){
        return "<FuncFParams>\n";
    }
}
