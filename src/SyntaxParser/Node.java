package SyntaxParser;

import java.util.ArrayList;

public class Node {
    private int startLine;
    private int endLine;
    public Node(int startLine,int endLine){
        this.startLine=startLine;
        this.endLine=endLine;
    }
    public int getStartLine() {
        return startLine;
    }

    public int getEndLine() {
        return endLine;
    }
}
