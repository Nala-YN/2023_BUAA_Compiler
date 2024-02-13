package Optimize;

import LlvmIr.BasicBlock;
import LlvmIr.Constant;
import LlvmIr.Global.Function;
import LlvmIr.IRBuilder;
import LlvmIr.Instr;
import LlvmIr.Instruction.Alu;
import LlvmIr.Instruction.Branch;
import LlvmIr.Instruction.Jmp;
import LlvmIr.Instruction.Move;
import LlvmIr.Instruction.Phi;
import LlvmIr.Module;
import LlvmIr.Type.LlvmType;
import LlvmIr.Undef;
import LlvmIr.Value;
import ToMips.Register;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;

public class PhiRemove {
    private static HashMap<Value, Register> var2reg;
    public static void removePhi(Module module){
        for(Function func: module.getFunctions()){
            var2reg=func.getVar2reg();
            ArrayList<BasicBlock> blocks=new ArrayList<>(func.getBlocks());
            for(BasicBlock block:blocks){
                removeBlockPhi(block);
            }
        }
    }
    public static void removeBlockPhi(BasicBlock curBlock){
        Iterator<Instr> it=curBlock.getInstrs().iterator();
        HashMap<BasicBlock,ArrayList<Move>> parent2moves=new HashMap<>();
        for(BasicBlock parent:curBlock.getParent()){
            parent2moves.put(parent,new ArrayList<>());
        }
        while(it.hasNext()){ //获取每个父块的move指令
            Instr instr=it.next();
            if(!(instr instanceof Phi)){
                break;
            }
            Phi phiInstr=(Phi)instr;
            ArrayList<Value> operands=phiInstr.getOperands();
            ArrayList<BasicBlock> parents=phiInstr.getBlocks();
            for(int i=0;i<=parents.size()-1;i++){
                if(!curBlock.getParent().contains(parents.get(i))){
                    parents.remove(i);
                    operands.remove(i);
                    i--;
                }
            }
            for(int i=0;i<=operands.size()-1;i++){
                if(!(operands.get(i) instanceof Undef)){
                    parent2moves.get(parents.get(i)).add(
                            new Move(phiInstr,operands.get(i),parents.get(i)));
                }
            }
            it.remove();
        }
        ArrayList<BasicBlock> parents=new ArrayList<>(curBlock.getParent());
        for(BasicBlock parent:parents){
            if(parent2moves.get(parent).size()==0)continue;
            //保证指令的并行性
            ArrayList<Move> paralleledMoves=new ArrayList<>();
            ArrayList<Move> oriMoves=parent2moves.get(parent);

            for(int i=0;i<=oriMoves.size()-1;i++){
                for(int j=i+1;j<=oriMoves.size()-1;j++){
                    if(oriMoves.get(i).getTo()==oriMoves.get(j).getFrom()){
                        Value value=new Value(IRBuilder.tempName+curBlock.getParentFunc().getVarId(), LlvmType.Int32);
                        Move temp=new Move(value,oriMoves.get(i).getTo(),curBlock);
                        paralleledMoves.add(0,temp);
                        for(int k=j;k<= oriMoves.size()-1;k++){
                            if(oriMoves.get(i).getTo()==oriMoves.get(k).getFrom()){
                                oriMoves.get(k).setFrom(value);
                            }
                        }
                    }
                }
                paralleledMoves.add(oriMoves.get(i));
            }
            //解决指令共享寄存器的问题
            ArrayList<Move> finalMoves=new ArrayList<>();
            for(int i=0;i<=paralleledMoves.size()-1;i++){
                for(int j=i+1;j<=paralleledMoves.size()-1;j++){
                    if(var2reg.containsKey(paralleledMoves.get(i).getTo())&&
                    var2reg.containsKey(paralleledMoves.get(j).getFrom())&&
                    var2reg.get(paralleledMoves.get(i).getTo())==var2reg.get(paralleledMoves.get(j).getFrom())){
                        Value value=new Value(IRBuilder.tempName+curBlock.getParentFunc().getVarId(), LlvmType.Int32);
                        Move temp=new Move(value,paralleledMoves.get(i).getTo(),curBlock);
                        finalMoves.add(0,temp);
                        for(int k=j;k<= paralleledMoves.size()-1;k++){
                            if(var2reg.containsKey(paralleledMoves.get(k).getFrom())&&
                                    var2reg.get(paralleledMoves.get(i).getTo())==var2reg.get(paralleledMoves.get(k).getFrom())){
                                paralleledMoves.get(k).setFrom(value);
                            }
                        }
                    }
                }
                finalMoves.add(paralleledMoves.get(i));
            }
            if(parent.getChild().size()>1){
                BasicBlock newBlock=new BasicBlock(IRBuilder.blockName+curBlock.getParentFunc().getBlockId(),
                        curBlock.getParentFunc());
                ArrayList<BasicBlock> blocks=curBlock.getParentFunc().getBlocks();
                blocks.add(blocks.indexOf(curBlock),newBlock);
                for(Instr instr:finalMoves){
                    newBlock.addInstr(instr);
                }
                Jmp jmpInstr=new Jmp(curBlock,newBlock);
                newBlock.addInstr(jmpInstr);
                Branch branchInstr= (Branch) parent.getInstrs().get(parent.getInstrs().size()-1);
                branchInstr.getOperands().set(branchInstr.getOperands().indexOf(curBlock),newBlock);
            }
            else{
                for(Instr instr:finalMoves){
                    parent.getInstrs().add(parent.getInstrs().size()-1,instr);
                }
            }
        }
    }
}
