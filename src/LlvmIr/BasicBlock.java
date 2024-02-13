package LlvmIr;

import LlvmIr.Global.Function;
import LlvmIr.Global.GlobalVar;
import LlvmIr.Instruction.Phi;
import LlvmIr.Type.LlvmType;
import Optimize.AnalyzedInfo.LoopInfo;

import java.util.ArrayList;
import java.util.HashSet;

public class BasicBlock extends User {
    private ArrayList<Instr> instrs = new ArrayList<>();
    private Function parentFunc;
    //支配树
    private ArrayList<BasicBlock> child;
    private ArrayList<BasicBlock> parent;
    private ArrayList<BasicBlock> imdom;
    private ArrayList<BasicBlock> dom;
    private BasicBlock imdommedBy;
    private ArrayList<BasicBlock> DF;
    private int imdomDepth;

    private boolean isDeleted = false;
    //活跃变量分析
    private HashSet<Value> defSet=new HashSet<>();
    private HashSet<Value> useSet=new HashSet<>();
    private HashSet<Value> inSet;
    private HashSet<Value> outSet;

    //循环分析
    private LoopInfo parentLoop;
    public BasicBlock(String name, Function func) {
        super(name, LlvmType.FuncType);
        this.parentFunc = func;
    }
    public int getLoopDepth()
    {
        if (parentLoop == null)
        {
            return 0;
        }
        return parentLoop.getLoopDepth();
    }


    public boolean hasRet() {
        if (instrs.size() > 0 && instrs.get(instrs.size() - 1).getInstrType() == Instr.InstrType.RETURN) {
            return true;
        }
        return false;
    }

    public Function getParentFunc() {
        return parentFunc;
    }

    public ArrayList<Instr> getInstrs() {
        return instrs;
    }

    public String toString() {
        StringBuilder sb = new StringBuilder();
        sb.append(name).append(":\n");
        for (Instr instr : instrs) {
            sb.append("\t").append(instr).append("\n");
        }
        return sb.toString();
    }
    public void buildDefUse(){
        HashSet<Value> use=new HashSet<>();
        HashSet<Value> def=new HashSet<>();
        for(Instr instr:instrs){
            if(instr instanceof Phi){
                for(Value value:instr.getOperands()){
                    if(value instanceof Instr || value instanceof Param || value instanceof GlobalVar){
                        use.add(value);
                    }
                }
            }
        }

        for(Instr instr:instrs){
            for(Value value:instr.getOperands()){
                if(!def.contains(value)&&(value instanceof Instr || value instanceof Param || value instanceof GlobalVar)){
                    use.add(value);
                }
            }
            if(!use.contains(instr)&& instr.hasLVal()){
                def.add(instr);
            }
        }
        useSet=use;
        defSet=def;
    }
    public void deleteForPhi(BasicBlock block){
        for(User user:users){
            if(user instanceof Phi phi && phi.getParentBlock()==block){
                for(int i=0;i<=phi.getBlocks().size()-1;i++){
                    if(phi.getBlocks().get(i)==this){
                        phi.getBlocks().remove(i);
                        phi.getOperands().remove(i);
                        i--;
                    }
                }
            }
        }
    }
    public void addParent(BasicBlock parentBlock){
        if(parent==null)parent=new ArrayList<>();
        parent.add(parentBlock);
    }
    public void addChild(BasicBlock childBlock){
        if(child==null)child=new ArrayList<>();
        child.add(childBlock);
    }
    public void setParentFunc(Function parentFunc) {
        this.parentFunc = parentFunc;
    }

    public void addInstr(Instr instr) {
        instrs.add(instr);
    }
    public void setChild(ArrayList<BasicBlock> child) {
        this.child = child;
    }

    public void setParent(ArrayList<BasicBlock> parent) {
        this.parent = parent;
    }

    public void setDeleted() {
        isDeleted = true;
    }

    public boolean isDeleted() {
        return isDeleted;
    }

    public void setImdom(ArrayList<BasicBlock> imdom) {
        this.imdom = imdom;
    }

    public void setDom(ArrayList<BasicBlock> dom) {
        this.dom = dom;
    }

    public void setImdommedBy(BasicBlock imdommedBy) {
        this.imdommedBy = imdommedBy;
    }

    public void setDF(ArrayList<BasicBlock> DF) {
        this.DF = DF;
    }

    public ArrayList<BasicBlock> getChild() {
        return child;
    }

    public ArrayList<BasicBlock> getParent() {
        return parent;
    }

    public ArrayList<BasicBlock> getImdom() {
        return imdom;
    }

    public ArrayList<BasicBlock> getDom() {
        return dom;
    }

    public BasicBlock getImdommedBy() {
        return imdommedBy;
    }

    public ArrayList<BasicBlock> getDF() {
        return DF;
    }

    public HashSet<Value> getInSet() {
        return inSet;
    }

    public void setInSet(HashSet<Value> inSet) {
        this.inSet = inSet;
    }

    public HashSet<Value> getOutSet() {
        return outSet;
    }

    public void setOutSet(HashSet<Value> outSet) {
        this.outSet = outSet;
    }

    public HashSet<Value> getDefSet() {
        return defSet;
    }

    public LoopInfo getParentLoop() {
        return parentLoop;
    }

    public void setParentLoop(LoopInfo parentLoop) {
        this.parentLoop = parentLoop;
    }
    public HashSet<Value> getUseSet() {
        return useSet;
    }

    public int getImdomDepth() {
        return imdomDepth;
    }

    public void setImdomDepth(int imdomDepth) {
        this.imdomDepth = imdomDepth;
    }
}
