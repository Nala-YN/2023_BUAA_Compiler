package Optimize;

import LlvmIr.BasicBlock;
import LlvmIr.Global.Function;
import LlvmIr.IRBuilder;
import LlvmIr.Instr;
import LlvmIr.Instruction.Call;
import LlvmIr.Instruction.Jmp;
import LlvmIr.Instruction.Phi;
import LlvmIr.Instruction.Ret;
import LlvmIr.Module;
import LlvmIr.Type.LlvmType;
import LlvmIr.Value;
import Optimize.AnalyzedInfo.FunctionCopy;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;

public class FunctionInline {
    private static Module module;
    private static boolean change=true;
    private static HashMap<Function,HashSet<Function>> callMap;
    private static HashMap<Function,HashSet<Function>> calledByMap;
    public static void inlineFunction(Module curModule){
        module=curModule;
        change=true;
        while(change){
            change=false;
            initCall();
            for(Function called:module.getFunctions()){
                if(calledByMap.get(called).size()==0){
                    continue;
                }
                if(called.getName().equals("@main")||callMap.get(called).size()!=0){
                    continue;
                }
                if(callMap.get(called).contains(called)){
                    continue;
                }
                inline(called);
                change=true;
            }
        }
    }
    public static void initCall(){
        callMap=new HashMap<>();
        calledByMap=new HashMap<>();
        for(Function func:module.getFunctions()){
            callMap.put(func,new HashSet<>());
            calledByMap.put(func,new HashSet<>());
        }
        for(Function func:module.getFunctions()){
            for(BasicBlock block: func.getBlocks()){
                for(Instr instr:block.getInstrs()){
                    if(instr instanceof Call){
                        Call call=(Call)instr;
                        Function target= (Function) call.getOperands().get(0);
                        callMap.get(func).add(target);
                        calledByMap.get(target).add(func);
                    }
                }
            }
        }
    }
    public static void inline(Function func){
        ArrayList<Call> calls=new ArrayList<>();
        for(Function caller:calledByMap.get(func)){
            for(BasicBlock block:caller.getBlocks()){
                for(Instr instr:block.getInstrs()){
                    if(instr instanceof Call){
                        Call call=(Call) instr;
                        if(call.getOperands().get(0).getName().equals(func.getName())){
                            calls.add(call);
                        }
                    }
                }
            }
        }
        for(Call call:calls){
            Function calledFunc=(Function) call.getOperands().get(0);
            Function callFunc=call.getParentBlock().getParentFunc();
            replaceCall(call,callFunc,calledFunc);
        }
    }
    public static void replaceCall(Call call,Function callFunc,Function calledFunc){
        BasicBlock curBlock=call.getParentBlock();
        LlvmType retType=calledFunc.getRetType();
        BasicBlock nextBlock=new BasicBlock(callFunc.getBlockName(),callFunc);
        callFunc.getBlocks().add(callFunc.getBlocks().indexOf(curBlock)+1,nextBlock);
        boolean reachAfterCall=false;
        /* 把call后面的都放在新建的下一块中*/
        ArrayList<Instr> copy=new ArrayList<>(curBlock.getInstrs());
        for(Instr instr: copy){
            if(!reachAfterCall && instr==call){
                reachAfterCall=true;
                continue;
            }
            if(reachAfterCall){
                curBlock.getInstrs().remove(instr);
                nextBlock.getInstrs().add(nextBlock.getInstrs().size(),instr);
                instr.setParentBlock(nextBlock);
            }
        }
        for(BasicBlock child:curBlock.getChild()){
            for(Instr instr:child.getInstrs()){
                if(instr instanceof Phi && ((Phi) instr).getBlocks().contains(curBlock)){
                    ((Phi) instr).getBlocks().set(((Phi) instr).getBlocks().indexOf(curBlock),nextBlock);
                    nextBlock.addUser(instr);
                    curBlock.getUsers().remove(instr);
                }
            }
        }
        nextBlock.setChild(curBlock.getChild());
        for(BasicBlock child:curBlock.getChild()){
            child.getParent().set(child.getParent().indexOf(curBlock),nextBlock);
        }
        curBlock.setChild(new ArrayList<>());
        /*------------------分割结束-------------------*/
        Function copyFunc= FunctionCopy.copyFunction(calledFunc,callFunc);
        for(int i=0;i<=copyFunc.getParams().size()-1;i++){
            Value formalParam=copyFunc.getParams().get(i);
            Value realParam=call.getOperands().get(i+1);
            formalParam.modifyValueForUsers(realParam);
        }
        Jmp jmp=new Jmp(copyFunc.getBlocks().get(0),curBlock);
        curBlock.addInstr(jmp);
        curBlock.addChild(copyFunc.getBlocks().get(0));
        copyFunc.getBlocks().get(0).addParent(curBlock);
        ArrayList<Ret> rets=new ArrayList<>();
        ArrayList<BasicBlock> blocks=new ArrayList<>();
        for(BasicBlock block:copyFunc.getBlocks()){
            for(Instr instr:block.getInstrs()){
                if(instr instanceof Ret){
                    rets.add((Ret) instr);
                    blocks.add(block);
                }
            }
        }
        if(retType == LlvmType.Int32){
            Phi phi=new Phi(callFunc.getVarName(),nextBlock,blocks);
            nextBlock.getInstrs().add(0,phi);
            for(Ret ret:rets){
                phi.addValue(ret.getParentBlock(),ret.getOperands().get(0));
                ArrayList<BasicBlock> child=new ArrayList<>();
                child.add(nextBlock);
                ret.getParentBlock().setChild(child);
                nextBlock.addParent(ret.getParentBlock());
                Jmp jmp1=new Jmp(nextBlock,ret.getParentBlock());
                ret.getParentBlock().addInstr(jmp1);
                ret.getParentBlock().getInstrs().remove(ret);
                ret.removeOperands();
            }
            call.modifyValueForUsers(phi);
        }
        else if(retType==LlvmType.Void){
            for(Ret ret:rets){
                Jmp jmp1=new Jmp(nextBlock,ret.getParentBlock());
                ret.getParentBlock().getInstrs().remove(ret);
                ret.removeOperands();
                ret.getParentBlock().addInstr(jmp1);
                ArrayList<BasicBlock> child=new ArrayList<>();
                child.add(nextBlock);
                ret.getParentBlock().setChild(child);
                nextBlock.addParent(ret.getParentBlock());
            }
        }
        for(BasicBlock block:copyFunc.getBlocks()){
            callFunc.getBlocks().add(callFunc.getBlocks().indexOf(nextBlock),block);
            block.setParentFunc(callFunc);
        }
        call.removeOperands();
        curBlock.getInstrs().remove(call);
    }
}
