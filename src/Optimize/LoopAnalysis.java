package Optimize;

import LlvmIr.BasicBlock;
import LlvmIr.Global.Function;
import LlvmIr.Loop;
import LlvmIr.Module;
import Optimize.AnalyzedInfo.LoopInfo;

import java.util.ArrayList;
import java.util.HashSet;

public class LoopAnalysis {
    public static void analyzeLoop(Module module){
        for(Function func: module.getFunctions()){
            for(BasicBlock block:func.getBlocks()){
                block.setParentLoop(null);
            }
            analyzeLoopForFunc(func);
        }
    }
    public static void analyzeLoopForFunc(Function func){
        ArrayList<BasicBlock> postOrder=func.getPostOrderForIdomTree();
        //block应该是循环的进入块，loopSons是以block为进入块的循环末尾的块（即下一次会跳转到进入块）
        for(BasicBlock block:postOrder){
            ArrayList<BasicBlock> loopEnds=new ArrayList<>();
            for(BasicBlock parent:block.getParent()){
                if(block.getDom().contains(parent)){
                    loopEnds.add(parent);
                }
            }
            if(!loopEnds.isEmpty()){
                LoopInfo loop=new LoopInfo(block,loopEnds);
                bfsLoop(loop,loopEnds);
            }
        }
        visited=new HashSet<>();
        visitBlocks(func.getBlocks().get(0));
    }
    public static void bfsLoop(LoopInfo loop,ArrayList<BasicBlock> loopEnds){
        ArrayList<BasicBlock> queue=new ArrayList<>(loopEnds);
        while(!queue.isEmpty()){
            BasicBlock block=queue.get(0);
            queue.remove(0);
            LoopInfo subLoop=block.getParentLoop();
            if(subLoop==null){
                block.setParentLoop(loop);
                if(block!=loop.getEntry()){
                    queue.addAll(block.getParent());
                }
            }
            else{
                LoopInfo parent=subLoop.getParentLoop();
                while(parent!=null){
                    subLoop=parent;
                    parent=parent.getParentLoop();
                }
                if(subLoop!=loop){
                    subLoop.setParentLoop(loop);
                    for(BasicBlock parentBlock:subLoop.getEntry().getParent()){
                        if(parentBlock.getParentLoop()!=subLoop){
                            queue.add(parentBlock);
                        }
                    }
                }
            }
        }
    }
    private static HashSet<BasicBlock> visited;
    //生成父子循环信息和块的循环深度
    //暂时不需要记录子信息
    private static void visitBlocks(BasicBlock curBlock){
        visited.add(curBlock);
        LoopInfo subLoop=curBlock.getParentLoop();
        if(subLoop!=null&&curBlock==subLoop.getEntry()){
            int depth=1;
            LoopInfo temp=subLoop.getParentLoop();
            while(temp!=null){
                temp=temp.getParentLoop();
                depth++;
            }
            subLoop.setLoopDepth(depth);
        }
        for(BasicBlock child:curBlock.getChild()){
            if(!visited.contains(child)){
                visitBlocks(child);
            }
        }
    }
}
