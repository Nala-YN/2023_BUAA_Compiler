package Optimize;

import LlvmIr.BasicBlock;
import LlvmIr.Constant;
import LlvmIr.Global.Function;
import LlvmIr.Global.GlobalVar;
import LlvmIr.IRBuilder;
import LlvmIr.Instr;
import LlvmIr.Instruction.Alloca;
import LlvmIr.Instruction.Alu;
import LlvmIr.Instruction.Branch;
import LlvmIr.Instruction.Call;
import LlvmIr.Instruction.GetPtr;
import LlvmIr.Instruction.Icmp;
import LlvmIr.Instruction.Jmp;
import LlvmIr.Instruction.Load;
import LlvmIr.Instruction.Store;
import LlvmIr.Instruction.Zext;
import LlvmIr.Module;
import LlvmIr.User;
import LlvmIr.Value;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;

public class GVN {
    private static HashMap<String, Value> gvnMap = new HashMap<>();
    private static BasicBlock curBlock;
    public static boolean openAlu=true;

    public static void optimize(Module module) {
        for (Function func : module.getFunctions()) {
            gvnMap = new HashMap<>();
            visitBlock(func.getBlocks().get(0));
        }
        for (Function func : module.getFunctions()) {
            deletableBlock = new HashSet<>();
            for (BasicBlock block : func.getBlocks()) {
                curBlock = block;
                ArrayList<Instr> instrs = new ArrayList<>(block.getInstrs());
                for (Instr instr : instrs) {//把rem转成div
                    if (instr instanceof Alu aluInstr&&aluInstr.getOp()== Alu.OP.SREM) {
                        rem2div((Alu) instr);
                    }
                }
                instrs = new ArrayList<>(block.getInstrs());
                for (Instr instr : instrs) {
                    if (instr instanceof Alu) {
                        aluOptimize((Alu) instr);
                    }
                    if (instr instanceof Icmp) {
                        icmpOptimize((Icmp) instr);
                    }
                    if (instr instanceof Branch) {
                        branchOptimize((Branch) instr);
                    }
                    if (instr instanceof GetPtr) {
                        gloVarToCons((GetPtr) instr);
                        localConstToCons((GetPtr) instr);
                    }
                    if (instr instanceof Zext) {
                        zextRemove((Zext) instr);
                    }
                }
                instrs = new ArrayList<>(block.getInstrs());
                for(Instr instr:instrs){
                    if(instr instanceof Alu){
                        aggreAluOptimize((Alu)instr);
                    }
                }
            }
            for (BasicBlock block : deletableBlock) {
                block.setDeleted();
                for(Instr instr: block.getInstrs()){
                    instr.removeOperands();
                }
            }
            func.getBlocks().removeIf(block -> deletableBlock.contains(block));
        }
        BlockSimplify.simplify(module);
    }
    public static void aggreAluOptimize(Alu alu){
        int constCnt = 0;
        for (Value value : alu.getOperands()) {
            if (value instanceof Constant) {
                constCnt++;
            }
        }
        if(constCnt==1){
            Value operand1=alu.getOperands().get(0);
            Value operand2=alu.getOperands().get(1);
            Alu.OP op=alu.getOp();
            if(operand1 instanceof Constant && (op==Alu.OP.ADD || op== Alu.OP.MUL)){
                alu.setOperands(operand1,1);
                alu.setOperands(operand2,0);
                operand1=alu.getOperands().get(0);
                operand2=alu.getOperands().get(1);
            }
            if(op== Alu.OP.SUB&& operand2 instanceof Constant){
                op= Alu.OP.ADD;
                Constant cons=new Constant(-((Constant)operand2).getValue());
                alu.setOp(op);
                alu.setOperands(cons,1);
                operand2=cons;
            }
            Alu lastAlu;
            int curCons,curCosPos;
            if(operand1 instanceof Constant){
                curCons=((Constant) operand1).getValue();
                if(!(operand2 instanceof Alu))return;
                lastAlu= (Alu) operand2;
                curCosPos=0;
            }
            else{
                if(!(operand1 instanceof Alu))return;
                lastAlu= (Alu) operand1;
                curCons=((Constant) operand2).getValue();
                curCosPos=1;
            }
            int lastCons,lastConsPos;
            if((lastAlu.getOperands().get(1) instanceof Constant)){
               lastCons= ((Constant)lastAlu.getOperands().get(1)).getValue();
               lastConsPos=1;
            }
            else if((lastAlu.getOperands().get(0) instanceof Constant)){
                lastCons= ((Constant)lastAlu.getOperands().get(0)).getValue();
                lastConsPos=0;
            }
            else{
                return;
            }
            Alu.OP lastOp=lastAlu.getOp();
            Alu newAlu=null;
            if(op == Alu.OP.ADD){
                if(lastOp==Alu.OP.ADD){
                    if(lastConsPos==0){
                        newAlu=new Alu(curBlock.getParentFunc().getVarName(), lastAlu.getOperands().get(1),
                                new Constant(curCons+lastCons),Alu.OP.ADD,curBlock);
                    }
                    else{
                        newAlu=new Alu(curBlock.getParentFunc().getVarName(),
                                lastAlu.getOperands().get(0),new Constant(curCons+lastCons), Alu.OP.ADD,curBlock);
                    }
                }
                else if(lastOp== Alu.OP.SUB){
                    if(lastConsPos==0){
                        newAlu=new Alu(curBlock.getParentFunc().getVarName(),new Constant(curCons+lastCons),
                                lastAlu.getOperands().get(1), Alu.OP.SUB,curBlock);
                    }
                    else{
                        newAlu=new Alu(curBlock.getParentFunc().getVarName(),
                                lastAlu.getOperands().get(0), new Constant(lastCons-curCons),Alu.OP.ADD,curBlock);
                    }
                }
            }
            else if(op== Alu.OP.SUB){
                if(lastOp== Alu.OP.ADD){
                    if(lastConsPos==0){
                        if(curCosPos==0){
                            newAlu=new Alu(curBlock.getParentFunc().getVarName(),new Constant(curCons-lastCons),
                                    lastAlu.getOperands().get(1), Alu.OP.SUB,curBlock);
                        }
                        else{
                            newAlu=new Alu(curBlock.getParentFunc().getVarName(),lastAlu.getOperands().get(1),
                                    new Constant(lastCons-curCons), Alu.OP.ADD,curBlock);
                        }
                    }
                    else{
                        if(curCosPos==0){
                            newAlu=new Alu(curBlock.getParentFunc().getVarName(),new Constant(curCons-lastCons),
                                    lastAlu.getOperands().get(0), Alu.OP.SUB,curBlock);
                        }
                        else{
                            newAlu=new Alu(curBlock.getParentFunc().getVarName(),lastAlu.getOperands().get(0),
                                    new Constant(lastCons-curCons), Alu.OP.ADD,curBlock);
                        }
                    }
                }
                else if(lastOp== Alu.OP.SUB){
                    if(lastConsPos==0){
                        if(curCosPos==0){
                            newAlu=new Alu(curBlock.getParentFunc().getVarName(), lastAlu.getOperands().get(1),
                                    new Constant(curCons-lastCons), Alu.OP.ADD,curBlock);
                        }
                        else{
                            newAlu=new Alu(curBlock.getParentFunc().getVarName(),new Constant(lastCons-curCons),
                                    lastAlu.getOperands().get(1), Alu.OP.SUB,curBlock);
                        }
                    }
                    else{
                        if(curCosPos==0){
                            newAlu=new Alu(curBlock.getParentFunc().getVarName(), new Constant(curCons+lastCons),
                                    lastAlu.getOperands().get(0), Alu.OP.SUB,curBlock);
                        }
                        else{
                            newAlu=new Alu(curBlock.getParentFunc().getVarName(), lastAlu.getOperands().get(0),
                                    new Constant(-(lastCons+curCons)), Alu.OP.ADD,curBlock);
                        }
                    }
                }
            }
            else if(op== Alu.OP.MUL){
                if(lastOp== Alu.OP.MUL){
                    Value base;
                    if(lastConsPos==0){
                        base=lastAlu.getOperands().get(1);
                    }
                    else{
                        base=lastAlu.getOperands().get(0);
                    }
                    newAlu=new Alu(curBlock.getParentFunc().getVarName(), base,
                            new Constant(curCons*lastCons), Alu.OP.MUL,curBlock);
                }
            }
            if(newAlu!=null){
                if(lastAlu.getUsers().size()==1){
                    lastAlu.removeOperands();
                    curBlock.getInstrs().remove(lastAlu);
                }
                curBlock.getInstrs().set(curBlock.getInstrs().indexOf(alu),newAlu);
                alu.modifyValueForUsers(newAlu);
                alu.removeOperands();
            }
        }
    }
    public static void visitBlock(BasicBlock block) {
        ArrayList<Instr> instrs = new ArrayList<>(block.getInstrs());
        HashSet<String> inserted = new HashSet<>();
        for (Instr instr : instrs) {
            if (instr instanceof Alu ||
                    instr instanceof Icmp ||
                    instr instanceof Call && ((Function) instr.getOperands().get(0)).isGvnAble() ||
                    instr instanceof GetPtr) {
                String gvnHash = instr.getGvnHash();
                if (gvnMap.containsKey(gvnHash)) {
                    instr.modifyValueForUsers(gvnMap.get(gvnHash));
                    block.getInstrs().remove(instr);
                    instr.removeOperands();
                } else {
                    gvnMap.put(gvnHash, instr);
                    inserted.add(gvnHash);
                }
            }
        }
        for (BasicBlock imdommed : block.getImdom()) {
            visitBlock(imdommed);
        }
        for (String gvnHash : inserted) {
            gvnMap.remove(gvnHash);
        }
    }

    public static void icmpOptimize(Icmp icmp) {
        if (!Optimizer.basicOptimize) {
            Value value1 = icmp.getOperands().get(0);
            Value value2 = icmp.getOperands().get(1);
            if (value1 instanceof Constant && value2 instanceof Constant) {
                Icmp.OP op = icmp.getOp();
                int cons1 = ((Constant) value1).getValue();
                int cons2 = ((Constant) value2).getValue();
                Constant cons;
                if (op == Icmp.OP.EQ && cons1 == cons2 ||
                        op == Icmp.OP.NE && cons1 != cons2 ||
                        op == Icmp.OP.SGE && cons1 >= cons2 ||
                        op == Icmp.OP.SGT && cons1 > cons2 ||
                        op == Icmp.OP.SLE && cons1 <= cons2 ||
                        op == Icmp.OP.SLT && cons1 < cons2) {
                    cons = new Constant(1);
                } else {
                    cons = new Constant(0);
                }
                curBlock.getInstrs().remove(icmp);
                icmp.removeOperands();
                icmp.modifyValueForUsers(cons);
            }
        }
    }

    private static HashSet<BasicBlock> deletableBlock;

    public static void branchOptimize(Branch branch) {
        if (Optimizer.againstLlvm) {
            Value value = branch.getOperands().get(0);
            if (value instanceof Constant) {
                Jmp jmpInstr;
                if (((Constant) value).getValue() == 0) {
                    jmpInstr = new Jmp(branch.getElseBlock(), curBlock);
                    curBlock.deleteForPhi(branch.getThenBlock());
                    curBlock.getChild().remove(branch.getThenBlock());
                    branch.getThenBlock().getParent().remove(curBlock);
                } else {
                    jmpInstr = new Jmp(branch.getThenBlock(), curBlock);
                    curBlock.deleteForPhi(branch.getElseBlock());
                    curBlock.getChild().remove(branch.getElseBlock());
                    branch.getElseBlock().getParent().remove(curBlock);
                }
                if (branch.getThenBlock().getParent().isEmpty()) {
                    deletableBlock.add(branch.getThenBlock());
                }
                if (branch.getElseBlock().getParent().isEmpty()) {
                    deletableBlock.add(branch.getElseBlock());
                }
                branch.removeOperands();
                curBlock.getInstrs().set(curBlock.getInstrs().indexOf(branch), jmpInstr);
            }
        }
    }

    public static void zextRemove(Zext zext) {
        if (Optimizer.againstLlvm) {
            Value value = zext.getOperands().get(0);
            zext.modifyValueForUsers(value);
            zext.removeOperands();
            curBlock.getInstrs().remove(zext);
        }
    }
    public static void rem2div(Alu instr){
        Value operand1=instr.getOperands().get(0);
        Value operand2=instr.getOperands().get(1);
        if(!(operand1 instanceof Constant)&&(operand2 instanceof Constant)){
            Alu divInstr=new Alu(curBlock.getParentFunc().getVarName(),operand1,operand2, Alu.OP.SDIV,curBlock);
            Alu mulInstr=new Alu(curBlock.getParentFunc().getVarName(),divInstr,operand2, Alu.OP.MUL,curBlock);
            Alu subInstr=new Alu(curBlock.getParentFunc().getVarName(),operand1,mulInstr, Alu.OP.SUB,curBlock);
            curBlock.getInstrs().set(curBlock.getInstrs().indexOf(instr),divInstr);
            curBlock.getInstrs().add(curBlock.getInstrs().indexOf(divInstr)+1,mulInstr);
            curBlock.getInstrs().add(curBlock.getInstrs().indexOf(mulInstr)+1,subInstr);
            instr.modifyValueForUsers(subInstr);
            instr.removeOperands();
            curBlock.getInstrs().remove(instr);
        }
    }
    public static void aluOptimize(Alu instr) {
        int constCnt = 0;
        for (Value value : instr.getOperands()) {
            if (value instanceof Constant) {
                constCnt++;
            }
        }
        if (constCnt == 2) {
            if(openAlu)optimize2ConstAlu(instr);
        } else if (constCnt == 1) {
            optimize1ConstAlu(instr);
        } else {
            if (instr.getOperands().get(0).getName().equals(instr.getOperands().get(1).getName())) {
                if (instr.getOp() == Alu.OP.SUB) {
                    Constant newValue = new Constant(0);
                    instr.modifyValueForUsers(newValue);
                    curBlock.getInstrs().remove(instr);
                    instr.removeOperands();
                } else if (instr.getOp() == Alu.OP.SDIV) {
                    Constant newValue = new Constant(1);
                    instr.modifyValueForUsers(newValue);
                    curBlock.getInstrs().remove(instr);
                    instr.removeOperands();
                } else if (instr.getOp() == Alu.OP.SREM) {
                    Constant newValue = new Constant(0);
                    instr.modifyValueForUsers(newValue);
                    curBlock.getInstrs().remove(instr);
                    instr.removeOperands();
                }
            }
        }
    }

    public static void gloVarToCons(GetPtr instr) {
        Value var1 = instr.getOperands().get(0);
        Value var2 = instr.getOperands().get(1);
        if (var1 instanceof GlobalVar && ((GlobalVar) var1).isConst()) {
            if (var2 instanceof Constant) {
                Constant newValue = new Constant(((GlobalVar) var1).getInitial().get(((Constant) var2).getValue()));
                Iterator<User> it = instr.getUsers().iterator();
                boolean isAllLoad = true;
                while (it.hasNext()) {
                    User user = it.next();
                    if (user instanceof Load) {
                        user.modifyValueForUsers(newValue);
                        it.remove();
                        ((Load) user).getParentBlock().getInstrs().remove(user);
                        user.removeOperands();
                    } else {
                        isAllLoad = false;
                    }
                }
                if (isAllLoad) {
                    curBlock.getInstrs().remove(instr);
                    instr.removeOperands();
                }
            }
        }
    }
    public static void localConstToCons(GetPtr getPtr){
        Value var1 = getPtr.getOperands().get(0);
        Value var2 = getPtr.getOperands().get(1);
        if(var1 instanceof GetPtr getPtr1){
            Value base=getPtr1.getOperands().get(0);
            if(base instanceof Alloca alloca && alloca.isConst() && var2 instanceof Constant cons2){
                ArrayList<Integer> initial=alloca.getInitial();
                Constant newValue = new Constant(initial.get(cons2.getValue()));
                Iterator<User> it = getPtr.getUsers().iterator();
                boolean isAllLoad = true;
                while (it.hasNext()) {
                    User user = it.next();
                    if (user instanceof Load) {
                        user.modifyValueForUsers(newValue);
                        it.remove();
                        ((Load) user).getParentBlock().getInstrs().remove(user);
                        user.removeOperands();
                    } else {
                        isAllLoad = false;
                    }
                }
                if (isAllLoad) {
                    curBlock.getInstrs().remove(getPtr);
                    getPtr.removeOperands();
                }
            }
        }
    }
    public static void optimize2ConstAlu(Alu instr) {
        Constant cons1 = (Constant) instr.getOperands().get(0);
        Constant cons2 = (Constant) instr.getOperands().get(1);
        int value1 = cons1.getValue();
        int value2 = cons2.getValue();
        Alu.OP op = instr.getOp();
        int newValue = 0;
        if (op == Alu.OP.ADD) {
            newValue = value1 + value2;
        } else if (op == Alu.OP.MUL) {
            newValue = value1 * value2;
        } else if (op == Alu.OP.SUB) {
            newValue = value1 - value2;
        } else if (op == Alu.OP.SDIV) {
            if (value2 != 0) {
                newValue = value1 / value2;
            }
        } else if (op == Alu.OP.SREM) {
            if (value2 != 0) {
                newValue = value1 % value2;
            }
        }
        else{
            throw new RuntimeException();
        }
        Constant newCons = new Constant(newValue);
        instr.modifyValueForUsers(newCons);
        curBlock.getInstrs().remove(instr);
        instr.removeOperands();
    }

    public static void optimize1ConstAlu(Alu instr) {
        Constant cons;
        Value another;
        int consPos = 0;
        if (instr.getOperands().get(0) instanceof Constant) {
            cons = (Constant) instr.getOperands().get(0);
            another = instr.getOperands().get(1);
        } else {
            cons = (Constant) instr.getOperands().get(1);
            another = instr.getOperands().get(0);
            consPos = 1;
        }
        int value = cons.getValue();
        Alu.OP op = instr.getOp();
        if (!Optimizer.basicOptimize) {
            if (another instanceof Alu && ((Alu) another).getOp() == op) {
                if ((op == Alu.OP.MUL || op == Alu.OP.ADD) && another.getUsers().size() == 1) {
                    Constant constant = null;
                    Value anoOfAno = null;
                    if (((Alu) another).getOperands().get(0) instanceof Constant) {
                        constant = (Constant) ((Alu) another).getOperands().get(0);
                        anoOfAno = ((Alu) another).getOperands().get(1);
                    } else if (((Alu) another).getOperands().get(1) instanceof Constant) {
                        constant = (Constant) ((Alu) another).getOperands().get(1);
                        anoOfAno = ((Alu) another).getOperands().get(0);
                    }
                    if (constant != null) {
                        if (op == Alu.OP.ADD) {
                            value = constant.getValue() + cons.getValue();
                        } else {
                            value = constant.getValue() * cons.getValue();
                        }
                        constant = new Constant(value);
                        Alu alu = new Alu(curBlock.getParentFunc().getVarName(), anoOfAno, constant, op, curBlock);
                        curBlock.getInstrs().set(curBlock.getInstrs().indexOf(instr), alu);
                        instr.removeOperands();
                        instr.modifyValueForUsers(alu);
                        ((Alu) another).removeOperands();
                        ((Alu) another).getParentBlock().getInstrs().remove(another);
                        instr = alu;
                        another = anoOfAno;
                    }
                }
            }
        }
        if (op == Alu.OP.ADD && value == 0) {
            instr.modifyValueForUsers(another);
            curBlock.getInstrs().remove(instr);
            instr.removeOperands();
        } else if (op == Alu.OP.MUL) {
            if (value == 0) {
                Constant newCons = new Constant(0);
                instr.modifyValueForUsers(newCons);
                curBlock.getInstrs().remove(instr);
                instr.removeOperands();
            } else if (value == 1) {
                instr.modifyValueForUsers(another);
                curBlock.getInstrs().remove(instr);
                instr.removeOperands();
            } else if (value == -1) {
                Alu subInstr = new Alu(curBlock.getParentFunc().getVarName(),
                        new Constant(0), another, Alu.OP.SUB, curBlock);
                instr.modifyValueForUsers(subInstr);
                instr.removeOperands();
                curBlock.getInstrs().set(curBlock.getInstrs().indexOf(instr),subInstr);
            } else if (value >= -4 && value <= 5 &&Optimizer.basicOptimize) {
                Alu aluInstr;
                aluInstr = new Alu(IRBuilder.tempName + curBlock.getParentFunc().getVarId(),
                        another, another, Alu.OP.ADD, curBlock);
                curBlock.getInstrs().add(curBlock.getInstrs().indexOf(instr), aluInstr);
                for (int i = 1; i <= Math.abs(value) - 2; i++) {
                    aluInstr = new Alu(IRBuilder.tempName + curBlock.getParentFunc().getVarId(),
                            aluInstr, another, Alu.OP.ADD, curBlock);
                    curBlock.getInstrs().add(curBlock.getInstrs().indexOf(instr), aluInstr);
                }
                if (value < 0) {
                    aluInstr = new Alu(IRBuilder.tempName + curBlock.getParentFunc().getVarId(),
                            new Constant(0), aluInstr, Alu.OP.SUB, curBlock);
                    curBlock.getInstrs().add(curBlock.getInstrs().indexOf(instr), aluInstr);
                }
                instr.modifyValueForUsers(aluInstr);
                curBlock.getInstrs().remove(instr);
                instr.removeOperands();
            }
        } else if (op == Alu.OP.SDIV) {
            if (value == 0 && consPos == 0) {
                Constant newCons = new Constant(0);
                instr.modifyValueForUsers(newCons);
                curBlock.getInstrs().remove(instr);
                instr.removeOperands();
            } else if (value == 1 && consPos == 1) {
                instr.modifyValueForUsers(another);
                curBlock.getInstrs().remove(instr);
                instr.removeOperands();
            } else if (value == -1 && consPos == 1) {
                Alu subInstr = new Alu(curBlock.getParentFunc().getVarName(),
                        new Constant(0), another, Alu.OP.SUB, curBlock);
                instr.modifyValueForUsers(subInstr);
                instr.removeOperands();
                curBlock.getInstrs().set(curBlock.getInstrs().indexOf(instr),subInstr);
            }
        } else if (op == Alu.OP.SREM) {
            if (value == 0 && consPos == 0 || (value == 1 || value == -1) && consPos == 1) {
                Constant newCons = new Constant(0);
                instr.modifyValueForUsers(newCons);
                curBlock.getInstrs().remove(instr);
                instr.removeOperands();
            }
        } else if (op == Alu.OP.SUB) {
            if (value == 0 && consPos == 1) {
                instr.modifyValueForUsers(another);
                curBlock.getInstrs().remove(instr);
                instr.removeOperands();
            }
        }
    }
}
