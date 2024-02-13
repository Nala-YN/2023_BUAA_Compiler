package LlvmIr.Type;

public class LlvmType {
    public static BasicBlockType BBType=new BasicBlockType();
    public static FuncType FuncType=new FuncType();
    public static BType Int8=new BType(8);
    public static BType Int32=new BType(32);
    public static BType Int1=new BType(1);
    public static BType Void=new BType(0);
    public int getArrayLen(){
        if(this instanceof ArrayType){
            return ((ArrayType) this).getSize();
        }
        else{
            return 1;
        }
    }
    public boolean isInt32(){
        return this==Int32;
    }
    public boolean isInt1(){
        return this==Int1;
    }
    public boolean isInt8(){
        return this==Int8;
    }
    public boolean isArray(){
        return this instanceof ArrayType;
    }
    public boolean isVoid(){
        return this==Void;
    }
    public boolean isPointer(){
        return this instanceof PointerType;
    }
}
