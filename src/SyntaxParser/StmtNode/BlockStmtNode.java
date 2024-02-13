package SyntaxParser.StmtNode;

import SyntaxParser.ExpNode.EqExpNode;
import SyntaxParser.ExpNode.LAndExpNode;
import SyntaxParser.Node;
import SyntaxParser.OtherNode.BlockNode;

public class BlockStmtNode extends Node {
    private BlockNode blockNode;
    public BlockStmtNode(int startLine,int endLine,BlockNode blockNode){
        super(startLine,endLine);
        this.blockNode=blockNode;
    }

    public BlockNode getBlockNode() {
        return blockNode;
    }

    public String toString(){
        return "<Stmt>\n";
    }
}
