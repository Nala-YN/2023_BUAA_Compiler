package LlvmIr;

import LlvmIr.Type.LlvmType;

import java.util.ArrayList;

public class Value {
    protected String name;
    protected LlvmType type;
    protected ArrayList<User> users=new ArrayList<>();
    public Value(String name, LlvmType type) {
        this.name = name;
        this.type=type;
    }
    public void removeUser(User user){
        if(users.contains(user)){
            users.remove(user);
        }
    }
    public String getName() {
        return name;
    }

    public LlvmType getLlvmType() {
        return type;
    }
    public void addUser(User user){
        if(!users.contains(user)){
            users.add(user);
        }
    }
    public ArrayList<User> getUsers(){
        return users;
    }
    public void modifyValueForUsers(Value newValue){
        for(User user:users){
            user.modifyValue(this,newValue);
        }
        users=new ArrayList<>();
    }
    public void deleteUser(User user){
        users.remove(user);
    }
}
