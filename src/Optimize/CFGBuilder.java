package Optimize;

import LlvmIr.BasicBlock;
import LlvmIr.Global.Function;
import LlvmIr.Instr;
import LlvmIr.Instruction.Branch;
import LlvmIr.Instruction.Jmp;
import LlvmIr.Module;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;

public class CFGBuilder {
    private static HashMap<BasicBlock, ArrayList<BasicBlock>> child;
    private static HashMap<BasicBlock,ArrayList<BasicBlock>> parent;
    private static HashMap<BasicBlock,ArrayList<BasicBlock>> dommedBy;
    private static HashMap<BasicBlock,ArrayList<BasicBlock>> dom;
    private static HashMap<BasicBlock,BasicBlock> imdommedBy;
    private static HashMap<BasicBlock,ArrayList<BasicBlock>> imdom;
    private static Function curFunc;
    public static void buildCFG(Module module){
        for(Function func:module.getFunctions()){
            child=new HashMap<>();
            parent=new HashMap<>();
            dom=new HashMap<>();
            dommedBy=new HashMap<>();
            imdommedBy=new HashMap<>();
            imdom=new HashMap<>();
            for(BasicBlock block:func.getBlocks()){
                child.put(block,new ArrayList<>());
                parent.put(block,new ArrayList<>());
                dom.put(block,new ArrayList<>());
                dommedBy.put(block,new ArrayList<>());
                imdom.put(block,new ArrayList<>());
            }
            curFunc=func;
            buildGraph();
            buildDomTree();
            buildDF();
        }
    }
    public static void buildGraph(){
        for(BasicBlock block: curFunc.getBlocks()){
            Instr lastInstr=block.getInstrs().get(block.getInstrs().size()-1);
            if(lastInstr instanceof Jmp){
                BasicBlock son=((Jmp) lastInstr).getToBlock();
                child.get(block).add(son);
                parent.get(son).add(block);
            }
            else if(lastInstr instanceof Branch){
                BasicBlock son1=((Branch) lastInstr).getElseBlock();
                BasicBlock son2=((Branch) lastInstr).getThenBlock();
                child.get(block).add(son1);
                child.get(block).add(son2);
                parent.get(son1).add(block);
                parent.get(son2).add(block);
            }
        }
        curFunc.setChild(child);
        curFunc.setParent(parent);
        for(BasicBlock block:curFunc.getBlocks()){
            block.setChild(child.get(block));
            block.setParent(parent.get(block));
        }
    }
    public static void buildDomTree(){
        BasicBlock entry=curFunc.getBlocks().get(0);
        for(BasicBlock dommer:curFunc.getBlocks()){
            HashSet<BasicBlock> reachBlocks=new HashSet<>();
            findReach(entry,dommer,reachBlocks);
            for(BasicBlock block: curFunc.getBlocks()){
                if(!reachBlocks.contains(block)){
                    dom.get(dommer).add(block);
                    dommedBy.get(block).add(dommer);
                }
            }
            dommer.setDom(dom.get(dommer));
        }
        for(BasicBlock dommed: curFunc.getBlocks()){
            for(BasicBlock dommer:dommedBy.get(dommed)){
                boolean flag=true;
                for(BasicBlock dommer1:dommedBy.get(dommed)){
                    if(dom.get(dommer).contains(dommer1)){
                        flag=false;
                        break;
                    }
                }
                if(flag){
                    dommed.setImdommedBy(dommer);
                    imdommedBy.put(dommed,dommer);
                    imdom.get(dommer).add(dommed);
                    break;
                }
            }
        }
        curFunc.setImdom(imdom);
        for(BasicBlock block: curFunc.getBlocks()){
            block.setImdom(imdom.get(block));
        }
        getIdomTreeDepth(curFunc.getBlocks().get(0),0 );
    }
    public static void getIdomTreeDepth(BasicBlock block,int depth){
        block.setImdomDepth(depth);
        for(BasicBlock imdommed:block.getImdom()){
            getIdomTreeDepth(imdommed,depth+1);
        }
    }
    public static void findReach(BasicBlock curBlock, BasicBlock dommer, HashSet<BasicBlock> reachBlocks){
        reachBlocks.add(curBlock);
        if(curBlock==dommer){
            return;
        }
        for(BasicBlock block:curBlock.getChild()){
            if(!reachBlocks.contains(block)){
                findReach(block,dommer,reachBlocks);
            }
        }
    }
    public static void buildDF(){
        for(BasicBlock dommer: curFunc.getBlocks()){
            ArrayList<BasicBlock> df=new ArrayList<>();
            for(BasicBlock dommed:dommer.getDom()){
                for(BasicBlock child:dommed.getChild()){
                    if(!dommer.getDom().contains(child)&&!df.contains(child)){
                        df.add(child);
                    }
                }
            }
            for(BasicBlock child:dommer.getChild()){
                if(!dommer.getDom().contains(child)&&!df.contains(child)){
                    df.add(child);
                }
            }
            dommer.setDF(df);
        }
        /*for(BasicBlock block: curFunc.getBlocks()){
            for(BasicBlock basicBlock:block.getDF()){
                System.out.println(block.getName()+"->"+basicBlock.getName());
            }
        }
        System.out.println("\n\n");
        for(BasicBlock block: curFunc.getBlocks()){
            for(BasicBlock basicBlock:block.getImdom()){
                System.out.println(block.getName()+"->"+basicBlock.getName());
            }
        }*/
    }
}
