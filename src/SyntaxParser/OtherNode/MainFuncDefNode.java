package SyntaxParser.OtherNode;

import SyntaxParser.ExpNode.EqExpNode;
import SyntaxParser.ExpNode.LAndExpNode;
import SyntaxParser.Node;

public class MainFuncDefNode extends Node {
    private BlockNode blockNode;

    public MainFuncDefNode(int startLine, int endLine, BlockNode blockNode) {
        super(startLine, endLine);
        this.blockNode = blockNode;
    }

    public BlockNode getBlockNode() {
        return blockNode;
    }

    public String toString(){
        return "<MainFuncDef>\n";
    }
}
