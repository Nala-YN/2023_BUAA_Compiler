package LlvmIr.Type;

public class BType extends LlvmType{
    private int bitWidth;

    public BType(int bitWidth) {
        this.bitWidth = bitWidth;
    }
    public String toString(){
        if(bitWidth==0){
            return "void";
        }
        else if(bitWidth==32){
            return "i32";
        }
        else if(bitWidth==8){
            return "i8";
        }
        else if(bitWidth==1){
            return "i1";
        }
        return null;
    }
}
