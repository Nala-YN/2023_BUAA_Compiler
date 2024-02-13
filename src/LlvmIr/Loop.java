package LlvmIr;

public class Loop {
    private BasicBlock condBlock;
    private BasicBlock bodyBlock;
    private BasicBlock followBlock;

    public Loop(BasicBlock condBlock, BasicBlock bodyBlock, BasicBlock followBlock) {
        this.condBlock = condBlock;
        this.bodyBlock = bodyBlock;
        this.followBlock = followBlock;
    }

    public BasicBlock getCondBlock() {
        return condBlock;
    }

    public BasicBlock getBodyBlock() {
        return bodyBlock;
    }

    public BasicBlock getFollowBlock() {
        return followBlock;
    }
}
