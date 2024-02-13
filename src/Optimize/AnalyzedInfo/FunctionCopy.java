package Optimize.AnalyzedInfo;

import LlvmIr.BasicBlock;
import LlvmIr.Constant;
import LlvmIr.Global.Function;
import LlvmIr.Global.GlobalVar;
import LlvmIr.IRBuilder;
import LlvmIr.Instr;
import LlvmIr.Instruction.Alloca;
import LlvmIr.Instruction.Alu;
import LlvmIr.Instruction.Branch;
import LlvmIr.Instruction.Call;
import LlvmIr.Instruction.GetPtr;
import LlvmIr.Instruction.IO.GetInt;
import LlvmIr.Instruction.IO.PutInt;
import LlvmIr.Instruction.IO.PutStr;
import LlvmIr.Instruction.Icmp;
import LlvmIr.Instruction.Jmp;
import LlvmIr.Instruction.Load;
import LlvmIr.Instruction.Move;
import LlvmIr.Instruction.Phi;
import LlvmIr.Instruction.Ret;
import LlvmIr.Instruction.Store;
import LlvmIr.Instruction.Zext;
import LlvmIr.Param;
import LlvmIr.Value;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
//解决函数内联时的引用问题
public class FunctionCopy {
    private static HashMap<Value,Value> copyMap=new HashMap<>();
    private static Function callFunc;
    public static Function copyFunction(Function calledFunc,Function callFuncIn){
        copyMap=new HashMap<>();
        callFunc=callFuncIn;
        Function copyFunc=new Function(calledFunc.getName()+"_copy",calledFunc.getRetType());
        for(Param param:calledFunc.getParams()){
            Param paramCopy=new Param(callFunc.getVarName(),param.getLlvmType(),copyFunc);
            copyMap.put(param,paramCopy);
            copyFunc.addParam(paramCopy);
        }
        for(BasicBlock block:calledFunc.getBlocks()){
            BasicBlock blockCopy=new BasicBlock(callFunc.getBlockName(),copyFunc);
            copyFunc.addBasicBlock(blockCopy);
            copyMap.put(block,blockCopy);
        }
        visited=new HashSet<>();
        phis=new HashSet<>();
        copiedInstr=new HashSet<>();
        copyBlock(calledFunc.getBlocks().get(0));
        //深度优先遍历可能会使得phi要用的value还未加入map
        for(Phi phi:phis){
            for(int i=0;i<=phi.getOperands().size()-1;i++){
                ((Phi)findValue(phi)).addValue((BasicBlock) findValue(phi.getBlocks().get(i)),findValue(phi.getOperands().get(i)));
            }
        }
        return copyFunc;
    }
    private static HashSet<BasicBlock> visited;
    private static HashSet<Phi> phis;
    public static void copyBlock(BasicBlock block){
        visited.add(block);
        for(Instr instr:block.getInstrs()){
            copyInstr(instr,block,false);
        }
        for(BasicBlock child:block.getChild()){
            if(!visited.contains(child)){
                copyBlock(child);
            }
        }
    }
    public static Value findValue(Value value){
        if(value==null){
            return null;
        }
        if(value instanceof Constant ||
        value instanceof Function ||
        value instanceof GlobalVar){
            return value;
        }
        else if(copyMap.containsKey(value)){
            return copyMap.get(value);
        }
        else{
            if(value instanceof BasicBlock){
                return null;
            }
            Instr instr=(Instr)value;
            copyInstr(instr,instr.getParentBlock(),true);
            copiedInstr.add(instr);
            return copyMap.get(instr);
        }
    }
    private static HashSet<Instr> copiedInstr;
    public static void copyInstr(Instr instr,BasicBlock parentSrt,boolean advancedCopy){
        if(instr.getName()!=null&&instr.getName().equals("%t42")){
            int o=1;
        }
        Instr instrCopy = null;
        BasicBlock parent= (BasicBlock) findValue(parentSrt);
        if(copiedInstr.contains(instr)){
            parent.addInstr((Instr) findValue(instr));
            return ;
        }
        if(instr instanceof GetInt){
            instrCopy=new GetInt(callFunc.getVarName(), parent);
        }
        else if(instr instanceof PutInt){
            instrCopy=new PutInt(findValue(instr.getOperands().get(0)),parent);
        }
        else if(instr instanceof PutStr){
            instrCopy=new PutStr(parent,((PutStr) instr).getContent());
        }
        else if(instr instanceof Alloca){
            instrCopy=new Alloca(callFunc.getVarName(),parent,((Alloca) instr).getPointedType());
        }
        else if(instr instanceof Alu){
            instrCopy=new Alu(callFunc.getVarName(), findValue(instr.getOperands().get(0)),
                    findValue(instr.getOperands().get(1)),((Alu) instr).getOp(),parent);
        }
        else if(instr instanceof Branch){
            instrCopy=new Branch(findValue(instr.getOperands().get(0)),
                    (BasicBlock) findValue(instr.getOperands().get(1)),
                    (BasicBlock) findValue(instr.getOperands().get(2)),parent);
            parent.addChild((BasicBlock) findValue(instr.getOperands().get(1)));
            parent.addChild((BasicBlock) findValue(instr.getOperands().get(2)));
            ((BasicBlock) findValue(instr.getOperands().get(1))).addParent(parent);
            ((BasicBlock) findValue(instr.getOperands().get(2))).addParent(parent);
        }
        else if(instr instanceof Call){
            throw new RuntimeException("no call");
        }
        else if(instr instanceof GetPtr){
            instrCopy=new GetPtr(callFunc.getVarName(),findValue(instr.getOperands().get(0)),
                    findValue(instr.getOperands().get(1)),parent);
        }
        else if(instr instanceof Icmp){
            instrCopy=new Icmp(findValue(instr.getOperands().get(0)),findValue(instr.getOperands().get(1)),
                    callFunc.getVarName(), parent,((Icmp) instr).getOp());
        }
        else if(instr instanceof Jmp){
            instrCopy=new Jmp((BasicBlock) findValue(instr.getOperands().get(0)),parent);
            parent.addChild((BasicBlock) findValue(instr.getOperands().get(0)));
            ((BasicBlock) findValue(instr.getOperands().get(0))).addParent(parent);
        }
        else if(instr instanceof Load){
            instrCopy=new Load(callFunc.getVarName(), findValue(instr.getOperands().get(0)),parent);
        }
        else if(instr instanceof Move){
            throw new RuntimeException("no move");
        }
        else if(instr instanceof Phi){
            ArrayList<BasicBlock> blocksCopy=new ArrayList<>();
            for(BasicBlock block:((Phi) instr).getBlocks()){
                blocksCopy.add((BasicBlock) findValue(block));
            }
            instrCopy=new Phi(callFunc.getVarName(), parent,blocksCopy);
            phis.add((Phi) instr);
        }
        else if(instr instanceof Ret){
            instrCopy=new Ret(findValue(instr.getOperands().get(0)),parent);
        }
        else if(instr instanceof Store){
            instrCopy=new Store(findValue(instr.getOperands().get(0)),findValue(instr.getOperands().get(1)),parent);
        }
        else if(instr instanceof Zext){
            instrCopy=new Zext(callFunc.getVarName(), findValue(instr.getOperands().get(0)),parent,((Zext) instr).getAimType());
        }
        if(!advancedCopy){
            parent.addInstr(instrCopy);
        }
        copyMap.put(instr,instrCopy);
    }
}
