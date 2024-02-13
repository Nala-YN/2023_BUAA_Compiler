package ToMips.AsmInstruction.Global;

import LlvmIr.Instr;

import java.util.ArrayList;

public class WordAsm extends GlobalAsm{
    private ArrayList<Integer> initial;

    public WordAsm(String name, ArrayList<Integer> initial) {
        super(name);
        this.initial = initial;
    }
    @Override
    public String toString(){
        String str=name+":.word ";
        for(int i=0;i<=initial.size()-1;i++){
            if(i==0){
                str=str+initial.get(i);
            }
            else{
                str=str+","+initial.get(i);
            }
        }
        return str;
    }
}
