package ToMips;

import ToMips.AsmInstruction.Global.GlobalAsm;

public class AsmInstr {
    public AsmInstr(){
        if(AsmBuilder.autoAdd){
            if(this instanceof GlobalAsm){
                AsmBuilder.addToData((GlobalAsm) this);
            }
            else{
                AsmBuilder.addToText(this);
            }
        }
    }
}
