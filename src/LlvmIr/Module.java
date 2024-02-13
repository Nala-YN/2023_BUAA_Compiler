package LlvmIr;

import LlvmIr.Global.CstStr;
import LlvmIr.Global.Function;
import LlvmIr.Global.GlobalVar;
import LlvmIr.Type.LlvmType;

import java.util.ArrayList;

public class Module extends Value{
    private ArrayList<CstStr> cstStrs=new ArrayList<>();
    private ArrayList<GlobalVar> globalVars=new ArrayList<>();
    private ArrayList<Function> functions=new ArrayList<>();

    public Module() {
        super(null,null);
    }

    public void addGlobalVar(GlobalVar globalVar){
        this.globalVars.add(globalVar);
    }
    public void addFunction(Function function){
        this.functions.add(function);
    }
    public void addStr(CstStr cstStr){
        this.cstStrs.add(cstStr);
    }

    public ArrayList<CstStr> getCstStrs() {
        return cstStrs;
    }

    public ArrayList<GlobalVar> getGlobalVars() {
        return globalVars;
    }

    public ArrayList<Function> getFunctions() {
        return functions;
    }

    public String toString(){
        StringBuilder sb=new StringBuilder();
        sb.append("declare i32 @getint(...) \n" +
                "declare void @putint(i32)\n" +
                "declare void @putstr(i8* )\n");
        for(CstStr cstStr:cstStrs){
            sb.append(cstStr.toString());
            sb.append("\n");
        }
        for(GlobalVar globalVar:globalVars){
            sb.append(globalVar.toString());
            sb.append("\n");
        }
        for(Function function:functions){
            sb.append(function.toString());
            sb.append("\n");
        }
        return sb.toString();
    }
}
