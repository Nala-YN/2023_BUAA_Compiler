package LlvmIr;

import LlvmIr.Type.LlvmType;

import java.util.ArrayList;

public class User extends Value{
    protected ArrayList<Value> operands=new ArrayList<>();
    public User(String name, LlvmType type) {
        super(name, type);
    }
    public void addOperand(Value value){
        operands.add(value);
        if(value!=null) value.addUser(this);
    }
    public void modifyValue(Value oldValue,Value newValue){
        while(true) {
            int index = operands.indexOf(oldValue);
            if(index==-1)break;
            operands.set(index,newValue);
            newValue.addUser(this);
        }
    }
    public void removeOperands(){
        for(Value value:operands){
            if(value!=null){
                value.removeUser(this);
            }
        }
    }

    public void setOperands(Value value,int pos) {
        this.operands.set(pos,value);
    }

    public ArrayList<Value> getOperands() {
        return operands;
    }
}
