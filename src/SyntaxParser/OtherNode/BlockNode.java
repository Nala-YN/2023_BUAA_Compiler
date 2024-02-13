package SyntaxParser.OtherNode;

import SyntaxParser.ExpNode.EqExpNode;
import SyntaxParser.ExpNode.LAndExpNode;
import SyntaxParser.Node;

import java.util.ArrayList;

public class BlockNode extends Node {
    private ArrayList<BlockItemNode> blockItemNodes;
    public BlockNode(int startLine,int endLine,ArrayList<BlockItemNode> blockItemNodes){
        super(startLine,endLine);
        this.blockItemNodes=blockItemNodes;
    }

    public ArrayList<BlockItemNode> getBlockItemNodes() {
        return blockItemNodes;
    }

    public String toString(){
        return "<Block>\n";
    }
}
