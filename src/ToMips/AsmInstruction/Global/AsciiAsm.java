package ToMips.AsmInstruction.Global;

public class AsciiAsm extends GlobalAsm{
    private String content;

    public AsciiAsm(String name, String content) {
        super(name);
        this.content = content;
    }
    public String toString() {
        return name + ": .asciiz \"" + content.replace("\n", "\\n") + "\"";
    }
}
