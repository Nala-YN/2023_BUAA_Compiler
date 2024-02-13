package ToMips.AsmInstruction.Global;

public class SpaceAsm extends GlobalAsm{
    private int size; //字节为单位

    public SpaceAsm(String name, int size) {
        super(name);
        this.size = size;
    }
    public String toString(){
        return name+":.space "+size;
    }
}
