package Optimize.AnalyzedInfo;

import LlvmIr.BasicBlock;

import java.util.ArrayList;

public class LoopInfo {
    private int loopDepth;
    private LoopInfo parentLoop;
    private ArrayList<BasicBlock> loopEnds;
    private BasicBlock entry;
    public LoopInfo(BasicBlock entry, ArrayList<BasicBlock> loopEnds){
        this.entry=entry;
        this.loopEnds=loopEnds;
        entry.setParentLoop(this);
    }

    public int getLoopDepth() {
        return loopDepth;
    }

    public void setLoopDepth(int loopDepth) {
        this.loopDepth = loopDepth;
    }

    public void setParentLoop(LoopInfo parentLoop) {
        this.parentLoop = parentLoop;
    }

    public LoopInfo getParentLoop() {
        return parentLoop;
    }

    public ArrayList<BasicBlock> getLoopEnds() {
        return loopEnds;
    }

    public BasicBlock getEntry() {
        return entry;
    }
}
