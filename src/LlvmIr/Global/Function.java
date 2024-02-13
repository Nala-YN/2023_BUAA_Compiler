package LlvmIr.Global;

import LlvmIr.BasicBlock;
import LlvmIr.IRBuilder;
import LlvmIr.Instr;
import LlvmIr.Instruction.Call;
import LlvmIr.Instruction.IO.GetInt;
import LlvmIr.Instruction.IO.PutInt;
import LlvmIr.Instruction.IO.PutStr;
import LlvmIr.Param;
import LlvmIr.Type.LlvmType;
import LlvmIr.User;
import LlvmIr.Value;
import ToMips.Register;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;

public class Function extends User {
    private ArrayList<Param> params = new ArrayList<>();
    private ArrayList<BasicBlock> blocks = new ArrayList<>();
    private LlvmType retType;


    private HashMap<BasicBlock, ArrayList<BasicBlock>> child;
    private HashMap<BasicBlock, ArrayList<BasicBlock>> parent;
    private HashMap<BasicBlock, BasicBlock> imdommedBy;
    private HashMap<BasicBlock, ArrayList<BasicBlock>> imdom;


    private HashMap<Value, Register> var2reg = new HashMap<>();
    private Boolean gvnAble = null;
    private int varId;
    private int blockId;
    private int activeCnt;
    private HashSet<Function> call;
    private boolean hasSideEffects;

    public Function(String name, LlvmType retType) {
        super(name, LlvmType.FuncType);
        this.retType = retType;
    }

    public String getVarName() {
        return IRBuilder.tempName + getVarId();
    }

    public int getVarId() {
        if (varId == 312) {
            int o = 1;
        }
        varId++;
        return varId - 1;
    }

    public String getBlockName() {
        return IRBuilder.blockName + getBlockId();
    }

    public int getBlockId() {
        if (blockId == 13) {
            int l = 1;
        }
        blockId++;
        return blockId - 1;
    }

    private ArrayList<BasicBlock> postOrder;

    public ArrayList<BasicBlock> getPostOrderForIdomTree() {
        postOrder = new ArrayList<>();
        visitIdomTree(blocks.get(0));
        return postOrder;
    }

    public void visitIdomTree(BasicBlock curBlock) {
        for (BasicBlock idommed : curBlock.getImdom()) {
            visitIdomTree(idommed);
        }
        postOrder.add(curBlock);
    }

    public String toString() {
        StringBuilder ret = new StringBuilder("define dso_local ");
        if (retType.isVoid()) {
            ret.append("void ");
        } else {
            ret.append("i32 ");
        }
        ret.append(name).append("(");
        for (Param param : params) {
            if (params.indexOf(param) == 0) {
                ret.append(param);
            } else {
                ret.append(",").append(param);
            }
        }
        ret.append("){\n");
        for (BasicBlock block : blocks) {
            ret.append(block);
        }
        ret.append("\n}");
        return ret.toString();
    }

    public boolean isGvnAble() {
        if (gvnAble != null) {
            return gvnAble;
        }
        for (Param param : params) {
            if (param.getLlvmType().isPointer()) {
                gvnAble = false;
                return false;
            }
        }
        for (BasicBlock block : blocks) {
            for (Instr instr : block.getInstrs()) {
                if (instr instanceof Call
                        || instr instanceof GetInt ||
                        instr instanceof PutInt ||
                        instr instanceof PutStr) {
                    gvnAble = false;
                    return false;

                }
                for (Value operand : instr.getOperands()) {
                    if ((operand instanceof GlobalVar && !((GlobalVar) operand).isConst())) {
                        gvnAble = false;
                        return false;
                    }
                }
            }
        }
        gvnAble = true;
        return true;
    }

    public void addBasicBlock(BasicBlock basicBlock) {
        blocks.add(basicBlock);
    }

    public HashSet<Function> getCall() {
        return call;
    }

    public void setCall(HashSet<Function> call) {
        this.call = call;
    }

    public boolean isHasSideEffects() {
        return hasSideEffects;
    }

    public void setHasSideEffects(boolean hasSideEffects) {
        this.hasSideEffects = hasSideEffects;
    }

    public void addParam(Param param) {
        params.add(param);
    }

    public ArrayList<Param> getParams() {
        return params;
    }

    public int getActiveCnt() {
        return activeCnt;
    }

    public void setActiveCnt(int activeCnt) {
        this.activeCnt = activeCnt;
    }

    public void setVarId(int varId) {
        this.varId = varId;
    }

    public void setBlockId(int blockId) {
        this.blockId = blockId;
    }

    public ArrayList<BasicBlock> getBlocks() {
        return blocks;
    }

    public LlvmType getRetType() {
        return retType;
    }

    public void setChild(HashMap<BasicBlock, ArrayList<BasicBlock>> child) {
        this.child = child;
    }

    public void setParent(HashMap<BasicBlock, ArrayList<BasicBlock>> parent) {
        this.parent = parent;
    }

    public void setDommed(HashMap<BasicBlock, BasicBlock> dommed) {
        this.imdommedBy = dommed;
    }

    public HashMap<Value, Register> getVar2reg() {
        return var2reg;
    }

    public void setBlocks(ArrayList<BasicBlock> blocks) {
        this.blocks = blocks;
    }

    public void setVar2reg(HashMap<Value, Register> var2reg) {
        this.var2reg = var2reg;
    }

    public void setImdom(HashMap<BasicBlock, ArrayList<BasicBlock>> imdom) {
        this.imdom = imdom;
    }
}
