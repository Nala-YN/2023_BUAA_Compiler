package ToMips;

public class LableAsm extends AsmInstr{
    private String label;
    private boolean isBlock;

    public LableAsm(boolean isBlock,String label) {
        this.label = label;
        this.isBlock=isBlock;
    }

    public String getLabel() {
        return label;
    }

    public String toString(){
        if(isBlock){
            return "    "+label+":";
        }
        else{
            return label+":";
        }
    }
}
