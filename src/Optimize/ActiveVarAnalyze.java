package Optimize;

import LlvmIr.BasicBlock;
import LlvmIr.Constant;
import LlvmIr.Global.Function;
import LlvmIr.Instruction.Alloca;
import LlvmIr.Instruction.Alu;
import LlvmIr.Instruction.GetPtr;
import LlvmIr.Module;
import LlvmIr.Value;

import java.util.HashMap;
import java.util.HashSet;

public class ActiveVarAnalyze {
    public static void buildInOut(Module module){
        HashMap<BasicBlock,HashSet<Value>> inMap;
        HashMap<BasicBlock,HashSet<Value>> outMap;
        for(Function func: module.getFunctions()){
            inMap=new HashMap<>();
            outMap=new HashMap<>();
            for(BasicBlock block: func.getBlocks()){
                block.buildDefUse();
                inMap.put(block,new HashSet<>());
                outMap.put(block,new HashSet<>());
            }
            boolean change=true;
            while(change){
                change=false;
                for(int i=func.getBlocks().size()-1;i>=0;i--){
                    BasicBlock block=func.getBlocks().get(i);
                    HashSet<Value> inSet=new HashSet<>();
                    HashSet<Value> outSet=new HashSet<>();
                    for(BasicBlock child: block.getChild()){
                        outSet.addAll(inMap.get(child));
                    }
                    inSet.addAll(outSet);
                    inSet.removeAll(block.getDefSet());
                    inSet.addAll(block.getUseSet());
                    if(!inSet.equals(inMap.get(block))||!outSet.equals(outMap.get(block))){
                        change=true;
                    }
                    inMap.put(block,inSet);
                    outMap.put(block,outSet);
                }
            }
            int maxActiveCnt=0;
            for(BasicBlock block: func.getBlocks()){
                block.setInSet(inMap.get(block));
                block.setOutSet(outMap.get(block));
                int tmpCnt=0;
                for(Value value:inMap.get(block)){
                    if(outMap.get(block).contains(value)){
                        tmpCnt++;
                    }
                }
                if(tmpCnt>maxActiveCnt)maxActiveCnt=tmpCnt;
            }
            func.setActiveCnt(maxActiveCnt);
        }
    }
}
