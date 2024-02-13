package LlvmIr.Instruction;

import LlvmIr.BasicBlock;
import LlvmIr.Instr;
import LlvmIr.Type.LlvmType;
import LlvmIr.Value;

import java.util.ArrayList;

public class Phi extends Instr {
    private ArrayList<BasicBlock> blocks;

    public Phi(String name,BasicBlock parentBlock, ArrayList<BasicBlock> blocks) {
        super(name, LlvmType.Int32, InstrType.PHI, parentBlock);
        this.blocks = blocks;
        for(int i=0;i<=blocks.size()-1;i++){
            operands.add(null);
            blocks.get(i).addUser(this);
        }
    }
    public void addValue(BasicBlock block, Value value){
        int index=blocks.indexOf(block);
        operands.set(index,value);
        value.addUser(this);
    }
    @Override
    public void modifyValue(Value oldValue,Value newValue){
        if(oldValue instanceof BasicBlock){
            while(true) {
                int index = blocks.indexOf(oldValue);
                if(index==-1)break;
                blocks.set(index, (BasicBlock) newValue);
                newValue.addUser(this);
            }
        }
        else{
            while(true) {
                int index = operands.indexOf(oldValue);
                if(index==-1)break;
                operands.set(index,newValue);
                newValue.addUser(this);
            }
        }
    }
    public ArrayList<BasicBlock> getBlocks() {
        return blocks;
    }

    public String toString(){
        //  %4 = phi i32 [ 1, %2 ], [ %6, %5 ]
        StringBuilder sb=new StringBuilder(name+" = phi "+type+" ");
        for(int i=0;i<=blocks.size()-1;i++){
            if(i==0){
                sb.append("[ ").append(operands.get(i).getName()).append(", ").append("%").
                        append(blocks.get(i).getName()).append(" ]");
            }
            else{
                sb.append(",[ ").append(operands.get(i).getName()).append(", ").append("%").
                        append(blocks.get(i).getName()).append(" ]");
            }
        }
        return sb.toString();
    }
}
