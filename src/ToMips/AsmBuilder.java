package ToMips;

import LlvmIr.BasicBlock;
import LlvmIr.Constant;
import LlvmIr.Global.CstStr;
import LlvmIr.Global.Function;
import LlvmIr.Global.GlobalVar;
import LlvmIr.Instr;
import LlvmIr.Instruction.Alloca;
import LlvmIr.Instruction.Alu;
import LlvmIr.Instruction.Branch;
import LlvmIr.Instruction.Call;
import LlvmIr.Instruction.GetPtr;
import LlvmIr.Instruction.IO.GetInt;
import LlvmIr.Instruction.IO.PutInt;
import LlvmIr.Instruction.IO.PutStr;
import LlvmIr.Instruction.Icmp;
import LlvmIr.Instruction.Jmp;
import LlvmIr.Instruction.Load;
import LlvmIr.Instruction.Move;
import LlvmIr.Instruction.OffsetLoad;
import LlvmIr.Instruction.OffsetStore;
import LlvmIr.Instruction.Ret;
import LlvmIr.Instruction.Store;
import LlvmIr.Instruction.Zext;
import LlvmIr.Module;
import LlvmIr.Param;
import LlvmIr.Type.LlvmType;
import LlvmIr.User;
import LlvmIr.Value;
import Optimize.Optimizer;
import Optimize.peepHole;
import ToMips.AsmInstruction.AluAsm;
import ToMips.AsmInstruction.BranchAsm;
import ToMips.AsmInstruction.CmpAsm;
import ToMips.AsmInstruction.Comment;
import ToMips.AsmInstruction.Global.AsciiAsm;
import ToMips.AsmInstruction.Global.GlobalAsm;
import ToMips.AsmInstruction.Global.SpaceAsm;
import ToMips.AsmInstruction.Global.WordAsm;
import ToMips.AsmInstruction.JumpAsm;
import ToMips.AsmInstruction.LaAsm;
import ToMips.AsmInstruction.LiAsm;
import ToMips.AsmInstruction.MemAsm;
import ToMips.AsmInstruction.MoveAsm;
import ToMips.AsmInstruction.MoveFromAsm;
import ToMips.AsmInstruction.MoveToAsm;
import ToMips.AsmInstruction.SyscallAsm;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;

public class AsmBuilder {
    public static boolean autoAdd = true;
    private static ArrayList<GlobalAsm> data;
    private static ArrayList<AsmInstr> text;
    private static HashMap<Value, Integer> value2Offset;
    private static int curOffset;
    private static HashMap<Value, Register> var2reg;
    private static Function curFunc;
    private static boolean isMain;

    public static void addToData(GlobalAsm globalAsm) {
        data.add(globalAsm);
    }

    public static void addToText(AsmInstr asmInstr) {
        text.add(asmInstr);
    }

    public static String genAsm(Module module) {
        data = new ArrayList<>();
        text = new ArrayList<>();
        ArrayList<CstStr> cstStrs = module.getCstStrs();
        ArrayList<GlobalVar> globalVars = module.getGlobalVars();
        ArrayList<Function> funcs = module.getFunctions();
        for (GlobalVar globalVar : globalVars) {
            buildGlobalVar(globalVar);
        }
        for (CstStr cstStr : cstStrs) {
            buildCstStr(cstStr);
        }
        isMain=true;
        for (Function func : funcs) {
            if(func.getName().equals("@main")){
                buildFunction(func);
            }
        }
        isMain=false;
        for (Function func : funcs) {
            if(!func.getName().equals("@main")){
                buildFunction(func);
            }
        }
        if (!Optimizer.basicOptimize) {
            optimize();
        }
        StringBuilder sb = new StringBuilder();
        sb.append(".data\n");
        for (GlobalAsm globalAsm : data) {
            sb.append(globalAsm.toString()).append("\n");
        }
        sb.append("\n\n");
        sb.append(".text\n");
        for (AsmInstr asmInstr : text) {
            if (!(asmInstr instanceof LableAsm)) {
                sb.append("         ");
            }
            sb.append(asmInstr.toString()).append("\n");
        }
        return sb.toString();
    }

    public static void optimize() {
        peepHole.sameMemSwLw(text);
        peepHole.addToMove(text);
        peepHole.deadMoveDel(text);
        peepHole.jumpAsmDel(text);
        peepHole.deadDefRemove(text);
        peepHole.deadDefRemove(text);
        peepHole.liLaSameValueRemove(text);
        peepHole.uselessCallMemEmit(text);
    }

    public static void buildCstStr(CstStr cstStr) {
        new AsciiAsm(cstStr.getName().substring(1), cstStr.getContent());
    }

    public static void buildGlobalVar(GlobalVar globalVar) {
        if (globalVar.isZeroInitial()) {
            new SpaceAsm(globalVar.getName().substring(1), globalVar.getLen() * 4);
        } else {
            new WordAsm(globalVar.getName().substring(1), globalVar.getInitial());
        }
    }

    public static void buildFunction(Function func) {
        curFunc = func;
        value2Offset = new HashMap<>();
        curOffset = 0;
        var2reg = func.getVar2reg();
        new LableAsm(false, func.getName().substring(1));
        for (int i = 0; i <= func.getParams().size() - 1; i++) {
            curOffset -= 4;
            if (i <= 2) {
                var2reg.put(func.getParams().get(i), Register.getByOffset(Register.a1, i));
            }
            value2Offset.put(func.getParams().get(i), curOffset);
        }
        for (BasicBlock block : func.getBlocks()) {
            for (Instr instr : block.getInstrs()) {
                if (instr.hasLVal() && !var2reg.containsKey(instr) && !value2Offset.containsKey(instr)) {
                    curOffset -= 4;
                    value2Offset.put(instr, curOffset);
                } else if (instr instanceof Move) {
                    Move move = (Move) instr;
                    if (!var2reg.containsKey(move.getTo()) && !value2Offset.containsKey(move.getTo())) {
                        curOffset -= 4;
                        value2Offset.put(move.getTo(), curOffset);
                    }
                }
            }
        }
        for (BasicBlock block : func.getBlocks()) {
            buildBlock(block);
        }
    }

    public static void buildBlock(BasicBlock block) {
        new LableAsm(true, curFunc.getName().substring(1) + "_" + block.getName());
        for (Instr instr : block.getInstrs()) {
            new Comment(instr);
            buildInstr(instr);
        }
    }

    public static void buildInstr(Instr instr) {
        if (instr instanceof GetInt) {
            buildGetInt((GetInt) instr);
        } else if (instr instanceof PutInt) {
            buildPutInt((PutInt) instr);
        } else if (instr instanceof PutStr) {
            buildPutStr((PutStr) instr);
        } else if (instr instanceof Alloca) {
            buildAlloca((Alloca) instr);
        } else if (instr instanceof Alu) {
            if (Optimizer.basicOptimize) {
                buildBasicAlu((Alu) instr);
            } else {
                buildAlu((Alu) instr);
            }
        }
        else if(instr instanceof OffsetLoad){
            buildOffsetLoad((OffsetLoad) instr);
        }
        else if(instr instanceof OffsetStore){
            buildOffsetStore((OffsetStore) instr);
        }
        else if (instr instanceof Branch) {
            buildBranch((Branch) instr);
        } else if (instr instanceof Call) {
            buildCall((Call) instr);
        } else if (instr instanceof GetPtr) {
            buildGetPtr((GetPtr) instr);
        } else if (instr instanceof Icmp) {
            buildIcmp((Icmp) instr);
        } else if (instr instanceof Jmp) {
            buildJmp((Jmp) instr);
        } else if (instr instanceof Load) {
            buildLoad((Load) instr);
        } else if (instr instanceof Ret) {
            buildRetInstr((Ret) instr);
        } else if (instr instanceof Store) {
            buildStore((Store) instr);
        } else if (instr instanceof Zext) {
            buildZext((Zext) instr);
        } else if (instr instanceof Move) {
            buildMove((Move) instr);
        }
    }

    public static void buildGetInt(GetInt getIntInstr) {
        new LiAsm(Register.v0, 5);
        new SyscallAsm();
        if (var2reg.containsKey(getIntInstr)) {
            new AluAsm(Register.v0, null, AluAsm.OP.addiu, var2reg.get(getIntInstr), 0);
        } else {
            new MemAsm(MemAsm.OP.sw, value2Offset.get(getIntInstr), Register.sp, Register.v0);
        }
    }

    public static void buildPutInt(PutInt putIntInstr) {
        Value value = putIntInstr.getOperands().get(0);
        if (value instanceof Constant) {
            new LiAsm(Register.a0, ((Constant) value).getValue());
        } else if (var2reg.containsKey(value)) {
            new AluAsm(var2reg.get(value), null, AluAsm.OP.addiu, Register.a0, 0);
        } else {
            int offset = value2Offset.get(value);
            new MemAsm(MemAsm.OP.lw, offset, Register.sp, Register.a0);
        }
        new LiAsm(Register.v0, 1);
        new SyscallAsm();
    }

    public static void buildPutStr(PutStr putStrInstr) {
        new LaAsm(Register.a0, putStrInstr.getContent().getName().substring(1));
        new LiAsm(Register.v0, 4);
        new SyscallAsm();
    }

    public static void buildAlloca(Alloca alloca) {
        LlvmType pointedType = alloca.getPointedType();
        if (pointedType.isArray()) {
            curOffset -= 4 * pointedType.getArrayLen();
        } else {
            curOffset -= 4;
        }
        if (var2reg.containsKey(alloca)) {
            new AluAsm(Register.sp, null, AluAsm.OP.addiu, var2reg.get(alloca), curOffset);
        } else {
            new AluAsm(Register.sp, null, AluAsm.OP.addiu, Register.k0, curOffset);
            new MemAsm(MemAsm.OP.sw, value2Offset.get(alloca), Register.sp, Register.k0);
        }
    }

    public static void buildBasicAlu(Alu aluInstr){
        Value value1 = aluInstr.getOperands().get(0);
        Value value2 = aluInstr.getOperands().get(1);
        Alu.OP op = aluInstr.getOp();
        Register op1=Register.k0;
        Register op2=Register.k1;
        if(value1 instanceof Constant cons){
            new LiAsm(op1,cons.getValue());
        }
        else if(var2reg.containsKey(value1)){
            op1=var2reg.get(value1);
        }
        else{
            new MemAsm(MemAsm.OP.lw,value2Offset.get(value1),Register.sp,op1);
        }
        if(value2 instanceof Constant cons){
            new LiAsm(op2,cons.getValue());
        }
        else if(var2reg.containsKey(value2)){
            op2=var2reg.get(value2);
        }
        else{
            new MemAsm(MemAsm.OP.lw,value2Offset.get(value2),Register.sp,op2);
        }
        Register to=Register.k0;
        if(var2reg.containsKey(aluInstr)){
            to=var2reg.get(aluInstr);
        }
        if(op== Alu.OP.ADD){
            new AluAsm(op1,op2, AluAsm.OP.addu,to,0);
        }
        else if(op== Alu.OP.SUB){
            new AluAsm(op1,op2, AluAsm.OP.subu,to,0);
        }
        else if(op== Alu.OP.MUL){
            new AluAsm(op1,op2, AluAsm.OP.mul,to,0);
        }
        else{
            new AluAsm(op1,op2, AluAsm.OP.div,to,0);
            if(op== Alu.OP.SREM){
                new MoveFromAsm(MoveFromAsm.OP.hi,to);
            }
            else{
                new MoveFromAsm(MoveFromAsm.OP.lo,to);
            }
        }
        if(to==Register.k0){
            new MemAsm(MemAsm.OP.sw,value2Offset.get(aluInstr),Register.sp,to);
        }
    }
    public static void buildAluWithoutOptimize(Alu aluInstr) {
        Value value1 = aluInstr.getOperands().get(0);
        Value value2 = aluInstr.getOperands().get(1);
        Alu.OP op = aluInstr.getOp();
        int consCnt = 0;
        if (value1 instanceof Constant) {
            consCnt++;
        }
        if (value2 instanceof Constant) {
            consCnt++;
        }
        Register to;
        if (var2reg.containsKey(aluInstr)) {
            to = var2reg.get(aluInstr);
        } else {
            to = Register.k0;
        }
        if (consCnt == 2) {
            new LiAsm(Register.k0, ((Constant) value1).getValue());
            if (!(op == Alu.OP.ADD || op == Alu.OP.SUB)) {
                new LiAsm(Register.k1, ((Constant) value2).getValue());
                if (op == Alu.OP.MUL) {
                    new AluAsm(Register.k0, Register.k1, AluAsm.OP.mul, to, 0);
                } else {
                    new AluAsm(Register.k0, Register.k1, AluAsm.OP.div, null, 0);
                    if (op == Alu.OP.SREM) {
                        new MoveFromAsm(MoveFromAsm.OP.hi, to);
                    } else {
                        new MoveFromAsm(MoveFromAsm.OP.lo, to);
                    }
                }
            } else {
                int num = op == Alu.OP.ADD ? ((Constant) value2).getValue() : -((Constant) value2).getValue();
                new AluAsm(Register.k0, null, AluAsm.OP.addiu, to, num);
            }
        } else if (consCnt == 1) {
            if (value1 instanceof Constant) {
                Register operand2 = Register.k0;
                if (var2reg.containsKey(value2)) {
                    operand2 = var2reg.get(value2);
                } else {
                    new MemAsm(MemAsm.OP.lw, value2Offset.get(value2), Register.sp, Register.k0);
                }
                if (op == Alu.OP.ADD) {
                    new AluAsm(operand2, null, AluAsm.OP.addiu, to, ((Constant) value1).getValue());
                } else {
                    new LiAsm(Register.k1, ((Constant) value1).getValue());
                    if (op == Alu.OP.MUL) {
                        new AluAsm(Register.k1, operand2, AluAsm.OP.mul, to, 0);
                    } else if (op == Alu.OP.SUB) {
                        new AluAsm(Register.k1, operand2, AluAsm.OP.subu, to, 0);
                    } else {
                        new AluAsm(Register.k1, operand2, AluAsm.OP.div, null, 0);
                        if (op == Alu.OP.SDIV) {
                            new MoveFromAsm(MoveFromAsm.OP.lo, to);
                        } else {
                            new MoveFromAsm(MoveFromAsm.OP.hi, to);
                        }
                    }
                }

            } else {
                Register operand1 = Register.k0;
                int num;
                if (var2reg.containsKey(value1)) {
                    operand1 = var2reg.get(value1);
                } else {
                    new MemAsm(MemAsm.OP.lw, value2Offset.get(value1), Register.sp, Register.k0);
                }
                num = ((Constant) value2).getValue();
                if (!(op == Alu.OP.ADD || op == Alu.OP.SUB)) {
                    new LiAsm(Register.k1, num);
                    if (op == Alu.OP.MUL) {
                        new AluAsm(operand1, Register.k1, AluAsm.OP.mul, to, 0);
                    } else {
                        new AluAsm(operand1, Register.k1, AluAsm.OP.div, null, 0);
                        if (op == Alu.OP.SREM) {
                            new MoveFromAsm(MoveFromAsm.OP.hi, to);
                        } else {
                            new MoveFromAsm(MoveFromAsm.OP.lo, to);
                        }
                    }
                } else {
                    if (op == Alu.OP.SUB) num = -num;
                    new AluAsm(operand1, null, AluAsm.OP.addiu, to, num);
                }
            }
        } else {
            Register operand1 = Register.k0;
            Register operand2 = Register.k1;
            if (var2reg.containsKey(value1)) {
                operand1 = var2reg.get(value1);
            } else {
                new MemAsm(MemAsm.OP.lw, value2Offset.get(value1), Register.sp, Register.k0);
            }
            if (var2reg.containsKey(value2)) {
                operand2 = var2reg.get(value2);
            } else {
                new MemAsm(MemAsm.OP.lw, value2Offset.get(value2), Register.sp, Register.k1);
            }
            if (op == Alu.OP.ADD) {
                new AluAsm(operand1, operand2, AluAsm.OP.addu, to, 0);
            } else if (op == Alu.OP.SUB) {
                new AluAsm(operand1, operand2, AluAsm.OP.subu, to, 0);
            } else if (op == Alu.OP.MUL) {
                new AluAsm(operand1, operand2, AluAsm.OP.mul, to, 0);
            } else if (op == Alu.OP.SDIV) {
                new AluAsm(operand1, operand2, AluAsm.OP.div, null, 0);
                new MoveFromAsm(MoveFromAsm.OP.lo, to);
            } else if (op == Alu.OP.SREM) {
                new AluAsm(operand1, operand2, AluAsm.OP.div, null, 0);
                new MoveFromAsm(MoveFromAsm.OP.hi, to);
            }
        }
        if (to == Register.k0) {
            new MemAsm(MemAsm.OP.sw, value2Offset.get(aluInstr), Register.sp, Register.k0);
        }
    }

    public static void buildAlu(Alu aluInstr) {
        Value value1 = aluInstr.getOperands().get(0);
        Value value2 = aluInstr.getOperands().get(1);
        Alu.OP op = aluInstr.getOp();
        int consCnt = 0;
        if (value1 instanceof Constant) {
            consCnt++;
        }
        if (value2 instanceof Constant) {
            consCnt++;
        }
        Register to;
        if (var2reg.containsKey(aluInstr)) {
            to = var2reg.get(aluInstr);
        } else {
            to = Register.k0;
        }
        if (consCnt == 2) {
            new LiAsm(Register.k0, ((Constant) value1).getValue());
            if (!(op == Alu.OP.ADD || op == Alu.OP.SUB)) {
                new LiAsm(Register.k1, ((Constant) value2).getValue());
                if (op == Alu.OP.MUL) {
                    new AluAsm(Register.k0, Register.k1, AluAsm.OP.mul, to, 0);
                } else {
                    new AluAsm(Register.k0, Register.k1, AluAsm.OP.div, null, 0);
                    if (op == Alu.OP.SREM) {
                        new MoveFromAsm(MoveFromAsm.OP.hi, to);
                    } else {
                        new MoveFromAsm(MoveFromAsm.OP.lo, to);
                    }
                }
            } else {
                int num = op == Alu.OP.ADD ? ((Constant) value2).getValue() : -((Constant) value2).getValue();
                new AluAsm(Register.k0, null, AluAsm.OP.addiu, to, num);
            }
        } else if (consCnt == 1) {
            if (value1 instanceof Constant) {
                Register operand2 = Register.k0;
                if (var2reg.containsKey(value2)) {
                    operand2 = var2reg.get(value2);
                } else {
                    new MemAsm(MemAsm.OP.lw, value2Offset.get(value2), Register.sp, Register.k0);
                }
                if (op == Alu.OP.ADD) {
                    new AluAsm(operand2, null, AluAsm.OP.addiu, to, ((Constant) value1).getValue());
                } else {
                    if (op == Alu.OP.MUL) {
                        int cons = ((Constant) value1).getValue();
                        buildMulWithCons(operand2, cons, to);
                    } else {
                        new LiAsm(Register.k1, ((Constant) value1).getValue());
                        if (op == Alu.OP.SUB) {
                            new AluAsm(Register.k1, operand2, AluAsm.OP.subu, to, 0);
                        } else {
                            new AluAsm(Register.k1, operand2, AluAsm.OP.div, null, 0);
                            if (op == Alu.OP.SDIV) {
                                new MoveFromAsm(MoveFromAsm.OP.lo, to);
                            } else {
                                new MoveFromAsm(MoveFromAsm.OP.hi, to);
                            }
                        }
                    }
                }

            } else {
                Register operand1 = Register.k0;
                int num;
                if (var2reg.containsKey(value1)) {
                    operand1 = var2reg.get(value1);
                } else {
                    new MemAsm(MemAsm.OP.lw, value2Offset.get(value1), Register.sp, Register.k0);
                }
                num = ((Constant) value2).getValue();
                if (!(op == Alu.OP.ADD || op == Alu.OP.SUB)) {
                    if (op == Alu.OP.MUL) {
                        buildMulWithCons(operand1, num, to);
                    } else {
                        if (op == Alu.OP.SREM) {
                            new LiAsm(Register.k1, num);
                            new AluAsm(operand1, Register.k1, AluAsm.OP.div, null, 0);
                            new MoveFromAsm(MoveFromAsm.OP.hi, to);
                        } else {
                            buildDivWithCons(operand1, num, to);
                        }
                    }
                } else {
                    if (op == Alu.OP.SUB) num = -num;
                    new AluAsm(operand1, null, AluAsm.OP.addiu, to, num);
                }
            }
        } else {
            Register operand1 = Register.k0;
            Register operand2 = Register.k1;
            if (var2reg.containsKey(value1)) {
                operand1 = var2reg.get(value1);
            } else {
                new MemAsm(MemAsm.OP.lw, value2Offset.get(value1), Register.sp, Register.k0);
            }
            if (var2reg.containsKey(value2)) {
                operand2 = var2reg.get(value2);
            } else {
                new MemAsm(MemAsm.OP.lw, value2Offset.get(value2), Register.sp, Register.k1);
            }
            if (op == Alu.OP.ADD) {
                new AluAsm(operand1, operand2, AluAsm.OP.addu, to, 0);
            } else if (op == Alu.OP.SUB) {
                new AluAsm(operand1, operand2, AluAsm.OP.subu, to, 0);
            } else if (op == Alu.OP.MUL) {
                new AluAsm(operand1, operand2, AluAsm.OP.mul, to, 0);
            } else if (op == Alu.OP.SDIV) {
                new AluAsm(operand1, operand2, AluAsm.OP.div, null, 0);
                new MoveFromAsm(MoveFromAsm.OP.lo, to);
            } else if (op == Alu.OP.SREM) {
                new AluAsm(operand1, operand2, AluAsm.OP.div, null, 0);
                new MoveFromAsm(MoveFromAsm.OP.hi, to);
            }
        }
        if (to == Register.k0) {
            new MemAsm(MemAsm.OP.sw, value2Offset.get(aluInstr), Register.sp, Register.k0);
        }
    }

    public static void buildMulWithCons(Register src, int cons, Register to) {
        int cnt = 0;
        int temp = cons;
        int sll1 = 0;
        int sll2 = 0;
        for (int i = 1; i <= 31; i++) {
            if ((temp & 1) == 1) {
                cnt++;
                if (cnt == 1) sll1 = i - 1;
                if (cnt == 2) sll2 = i - 1;
            }
            temp = temp >> 1;
        }
        if (cons < 0 || cnt > 2) {
            new LiAsm(Register.v0, cons);
            new AluAsm(Register.v0, src, AluAsm.OP.mul, to, 0);
        } else {
            if (cnt == 1) {
                new AluAsm(src, null, AluAsm.OP.sll, to, sll1);
            } else {
                if (sll1 == 0) {
                    new AluAsm(src, null, AluAsm.OP.sll, Register.v1, sll2);
                    new AluAsm(src, Register.v1, AluAsm.OP.addu, to, 0);
                } else {
                    new AluAsm(src, null, AluAsm.OP.sll, Register.v0, sll1);
                    new AluAsm(src, null, AluAsm.OP.sll, Register.v1, sll2);
                    new AluAsm(Register.v0, Register.v1, AluAsm.OP.addu, to, 0);
                }
            }
        }
    }

    public static int getSllCounts(int temp) {
        int l = 0;
        temp = temp >>> 1;
        while (temp != 0) {
            temp = temp >>> 1;
            l++;
        }
        return l;
    }

    //为了向上取整
    public static Register getDividend(Register oldDividend, int abs) {
        int l = getSllCounts(abs);
        new AluAsm(oldDividend, null, AluAsm.OP.sra, Register.v0, 31);
        if (l > 0) {
            new AluAsm(Register.v0, null, AluAsm.OP.srl, Register.v0, 32 - l);
        }
        new AluAsm(oldDividend, Register.v0, AluAsm.OP.addu, Register.v1, 0);
        return Register.v1;
    }

    public static void buildDivWithCons(Register src, int cons, Register to) {
        int abs = Math.abs(cons);
        if ((abs & (abs - 1)) == 0) {
            int l = getSllCounts(abs);
            Register newDiviend = getDividend(src, abs);
            new AluAsm(newDiviend, null, AluAsm.OP.sra, to, l);
        } else {
            long t = 32;
            long nc = ((long) 1 << 31) - (((long) 1 << 31) % abs) - 1;
            while (((long) 1 << t) <= nc * (abs - ((long) 1 << t) % abs)) {
                t++;
            }
            long m = ((((long) 1 << t) + (long) abs - ((long) 1 << t) % abs) / (long) abs);
            int n = (int) ((m << 32) >>> 32);
            int shift = (int) (t - 32);
            new LiAsm(Register.v0, n);
            if (m >= 0x80000000L) {
                new MoveToAsm(MoveToAsm.OP.hi, src);
                new AluAsm(src, Register.v0, AluAsm.OP.madd, Register.v1, 0);
            } else {
                new AluAsm(src, Register.v0, AluAsm.OP.mult, null, 0);
                new MoveFromAsm(MoveFromAsm.OP.hi, Register.v1);
            }
            new AluAsm(Register.v1, null, AluAsm.OP.sra, Register.v0, shift);
            new AluAsm(src, null, AluAsm.OP.srl, Register.a0, 31);
            new AluAsm(Register.v0, Register.a0, AluAsm.OP.addu, to, 0);
        }
        if (cons < 0) {
            new AluAsm(Register.zero, to, AluAsm.OP.subu, to, 0);
        }
    }

    public static void buildBranch(Branch branchInstr) {
        Icmp icmpInstr = (Icmp) branchInstr.getOperands().get(0);
        if(!icmpInstr.onlyBranchUse()){
            if(var2reg.containsKey(icmpInstr)){
                new BranchAsm(var2reg.get(icmpInstr), null, curFunc.getName().substring(1) + "_"
                        + branchInstr.getThenBlock().getName(), 1, BranchAsm.OP.beq);
            }
            else{
                new MemAsm(MemAsm.OP.lw, value2Offset.get(icmpInstr), Register.sp, Register.k0);
                new BranchAsm(Register.k0, null, curFunc.getName().substring(1) + "_"
                        + branchInstr.getThenBlock().getName(), 1, BranchAsm.OP.beq);
            }
            new JumpAsm(null, curFunc.getName().substring(1) + "_"
                    + branchInstr.getElseBlock().getName(), JumpAsm.OP.j);
            return;
        }
        Icmp.OP op = icmpInstr.getOp();
        BranchAsm.OP op1;
        if (op == Icmp.OP.EQ) {
            op1 = BranchAsm.OP.beq;
        } else if (op == Icmp.OP.NE) {
            op1 = BranchAsm.OP.bne;
        } else if (op == Icmp.OP.SLT) {
            op1 = BranchAsm.OP.blt;
        } else if (op == Icmp.OP.SGT) {
            op1 = BranchAsm.OP.bgt;
        } else if (op == Icmp.OP.SGE) {
            op1 = BranchAsm.OP.bge;
        } else {
            op1 = BranchAsm.OP.ble;
        }
        Value value1 = icmpInstr.getOperands().get(0);
        Value value2 = icmpInstr.getOperands().get(1);
        Register operand1 = Register.k0;
        Register operand2 = Register.k1;
        Constant cons1 = null;
        Constant cons2 = null;
        if (value1 instanceof Constant) {
            cons1 = (Constant) value1;
        } else if (var2reg.containsKey(value1)) {
            operand1 = var2reg.get(value1);
        } else {
            new MemAsm(MemAsm.OP.lw, value2Offset.get(value1), Register.sp, Register.k0);
        }
        if (value2 instanceof Constant) {
            cons2 = (Constant) value2;
        } else if (var2reg.containsKey(value2)) {
            operand2 = var2reg.get(value2);
        } else {
            new MemAsm(MemAsm.OP.lw, value2Offset.get(value2), Register.sp, Register.k1);
        }
        if (cons1 != null && cons2 == null || cons1 == null && cons2 != null) {
            int num;
            if (cons1 == null) {
                num = cons2.getValue();
            } else {
                num = cons1.getValue();
                operand1 = operand2;
                if (op1 == BranchAsm.OP.bgt) {
                    op1 = BranchAsm.OP.blt;
                } else if (op1 == BranchAsm.OP.ble) {
                    op1 = BranchAsm.OP.bge;
                } else if (op1 == BranchAsm.OP.blt) {
                    op1 = BranchAsm.OP.bgt;
                } else if (op1 == BranchAsm.OP.bge) {
                    op1 = BranchAsm.OP.ble;
                }
            }
            new BranchAsm(operand1, null, curFunc.getName().substring(1) + "_"
                    + branchInstr.getThenBlock().getName(), num, op1);
        } else if (cons1 != null && cons2 != null) {
            new LiAsm(Register.k0, cons1.getValue());
            new BranchAsm(operand1, null, curFunc.getName().substring(1) + "_"
                    + branchInstr.getThenBlock().getName(), cons2.getValue(), op1);
        } else {
            new BranchAsm(operand1, operand2, curFunc.getName().substring(1) + "_" +
                    branchInstr.getThenBlock().getName(), 0, op1);
        }
        new JumpAsm(null, curFunc.getName().substring(1) + "_"
                + branchInstr.getElseBlock().getName(), JumpAsm.OP.j);
    }

    public static void buildCall(Call callInstr) {
        ArrayList<Register> alloctedRegs;
        if(Optimizer.basicOptimize){
            alloctedRegs=new ArrayList<>(var2reg.values());
        }
        else{
            alloctedRegs = new ArrayList<>(new HashSet<>(callInstr.getActiveReg()));
        }
        ArrayList<MemAsm> lws=new ArrayList<>();
        ArrayList<MemAsm> sws=new ArrayList<>();
        for(Register reg:var2reg.values()){
            if(reg==Register.a1||reg==Register.a2||reg==Register.a3){
                alloctedRegs.add(reg);
            }
        }
        for (int i = 1; i <= alloctedRegs.size(); i++) {
            sws.add(new MemAsm(MemAsm.OP.sw, curOffset - i * 4, Register.sp, alloctedRegs.get(i - 1)));
        }
        new MemAsm(MemAsm.OP.sw, curOffset - alloctedRegs.size() * 4 - 4, Register.sp, Register.ra);
        Function calledFunc = (Function) callInstr.getOperands().get(0);
        for (int i = 1; i <= callInstr.getOperands().size() - 1; i++) {
            Value param = callInstr.getOperands().get(i);
            if (i <= 3) {
                Register paramReg = Register.values()[Register.a0.ordinal() + i];
                if (param instanceof Constant) {
                    new LiAsm(paramReg, ((Constant) param).getValue());
                } else if (var2reg.containsKey(param)) {
                    if (param instanceof Param) {
                        new MemAsm(MemAsm.OP.lw,
                                curOffset - (alloctedRegs.indexOf(var2reg.get(param)) + 1) * 4, Register.sp, paramReg);
                    } else {
                        new MoveAsm(paramReg,var2reg.get(param));
                    }
                } else {
                    new MemAsm(MemAsm.OP.lw, value2Offset.get(param), Register.sp, paramReg);
                }
            } else {
                Register reg = Register.k0;
                if (param instanceof Constant) {
                    new LiAsm(reg, ((Constant) param).getValue());
                } else if (var2reg.containsKey(param)) {
                    if (param instanceof Param) {
                        new MemAsm(MemAsm.OP.lw,
                                curOffset - (alloctedRegs.indexOf(var2reg.get(param)) + 1) * 4, Register.sp, reg);
                    } else {
                        reg = var2reg.get(param);
                    }
                } else {
                    new MemAsm(MemAsm.OP.lw, value2Offset.get(param), Register.sp, reg);
                }
                new MemAsm(MemAsm.OP.sw, curOffset - alloctedRegs.size() * 4 - 4 - i * 4, Register.sp, reg);
            }
        }
        new AluAsm(Register.sp, null, AluAsm.OP.addiu, Register.sp, curOffset - 4 * alloctedRegs.size() - 4);
        JumpAsm jal=new JumpAsm(null, calledFunc.getName().substring(1), JumpAsm.OP.jal);
        new MemAsm(MemAsm.OP.lw, 0, Register.sp, Register.ra);
        new AluAsm(Register.sp, null, AluAsm.OP.addiu, Register.sp, -(curOffset - 4 * alloctedRegs.size() - 4));
        for (int i = 1; i <= alloctedRegs.size(); i++) {
            lws.add(new MemAsm(MemAsm.OP.lw, curOffset - i * 4, Register.sp, alloctedRegs.get(i - 1)));
        }
        jal.setLws(lws);
        jal.setSws(sws);
        if (calledFunc.getRetType() != LlvmType.Void) {
            if (var2reg.containsKey(callInstr)) {
                new AluAsm(Register.v0, null, AluAsm.OP.addiu, var2reg.get(callInstr), 0);
            } else {
                new MemAsm(MemAsm.OP.sw, value2Offset.get(callInstr), Register.sp, Register.v0);
            }
        }
    }

    public static void buildGetPtr(GetPtr getPtrInstr) {
        Value point = getPtrInstr.getOperands().get(0);
        Value offset = getPtrInstr.getOperands().get(1);
        Register pointReg = Register.k0;
        Register offsetReg = Register.k1;
        Register resReg = Register.k0;
        if (point instanceof GlobalVar) {
            new LaAsm(pointReg, point.getName().substring(1));
        } else if (var2reg.containsKey(point)) {
            pointReg = var2reg.get(point);
        } else {
            new MemAsm(MemAsm.OP.lw, value2Offset.get(point), Register.sp, pointReg);
        }
        if (offset instanceof Constant) {
            if (var2reg.containsKey(getPtrInstr)) {
                new AluAsm(pointReg, null, AluAsm.OP.addiu, var2reg.get(getPtrInstr),
                        ((Constant) offset).getValue() * 4);
            } else {
                new AluAsm(pointReg, null, AluAsm.OP.addiu, resReg,
                        ((Constant) offset).getValue() * 4);
                new MemAsm(MemAsm.OP.sw, value2Offset.get(getPtrInstr), Register.sp, resReg);
            }
        } else {
            if (var2reg.containsKey(offset)) {
                offsetReg = var2reg.get(offset);
            } else {
                new MemAsm(MemAsm.OP.lw, value2Offset.get(offset), Register.sp, offsetReg);
            }
            new AluAsm(offsetReg, null, AluAsm.OP.sll, Register.k1, 2);
            if (var2reg.containsKey(getPtrInstr)) {
                new AluAsm(pointReg, Register.k1, AluAsm.OP.addu, var2reg.get(getPtrInstr), 0);
            } else {
                new AluAsm(pointReg, Register.k1, AluAsm.OP.addu, resReg, 0);
                new MemAsm(MemAsm.OP.sw, value2Offset.get(getPtrInstr), Register.sp, resReg);
            }
        }
    }

    public static void buildIcmp(Icmp icmpInstr) {
        if (icmpInstr.onlyBranchUse()) return;
        Icmp.OP op = icmpInstr.getOp();
        CmpAsm.OP op1;
        if (op == Icmp.OP.EQ) {
            op1 = CmpAsm.OP.seq;
        } else if (op == Icmp.OP.NE) {
            op1 = CmpAsm.OP.sne;
        } else if (op == Icmp.OP.SLT) {
            op1 = CmpAsm.OP.slt;
        } else if (op == Icmp.OP.SGT) {
            op1 = CmpAsm.OP.sgt;
        } else if (op == Icmp.OP.SGE) {
            op1 = CmpAsm.OP.sge;
        } else {
            op1 = CmpAsm.OP.sle;
        }
        Value value1 = icmpInstr.getOperands().get(0);
        Value value2 = icmpInstr.getOperands().get(1);
        Register operand1 = Register.k0;
        Register operand2 = Register.k1;
        if (value1 instanceof Constant) {
            new LiAsm(Register.k0, ((Constant) value1).getValue());
        } else if (var2reg.containsKey(value1)) {
            operand1 = var2reg.get(value1);
        } else {
            new MemAsm(MemAsm.OP.lw, value2Offset.get(value1), Register.sp, Register.k0);
        }
        if (value2 instanceof Constant) {
            new LiAsm(Register.k1, ((Constant) value2).getValue());
        } else if (var2reg.containsKey(value2)) {
            operand2 = var2reg.get(value2);
        } else {
            new MemAsm(MemAsm.OP.lw, value2Offset.get(value2), Register.sp, Register.k1);
        }
        if (var2reg.containsKey(icmpInstr)) {
            new CmpAsm(operand1, operand2, var2reg.get(icmpInstr), op1);
        } else {
            new CmpAsm(operand1, operand2, Register.k0, op1);
            new MemAsm(MemAsm.OP.sw, value2Offset.get(icmpInstr), Register.sp, Register.k0);
        }
    }

    public static void buildJmp(Jmp jmpInstr) {
        String label = curFunc.getName().substring(1) + "_"
                + jmpInstr.getToBlock().getName();
        new JumpAsm(null, label, JumpAsm.OP.j);
    }

    public static void buildLoad(Load loadInstr) {
        Register point = Register.k0;
        if (loadInstr.getOperands().get(0) instanceof GlobalVar) {
            new LaAsm(Register.k0, loadInstr.getOperands().get(0).getName().substring(1));
        } else if (var2reg.containsKey(loadInstr.getOperands().get(0))) {
            point = var2reg.get(loadInstr.getOperands().get(0));
        } else {
            new MemAsm(MemAsm.OP.lw, value2Offset.get(loadInstr.getOperands().get(0)), Register.sp, Register.k0);
        }
        if (var2reg.containsKey(loadInstr)) {
            new MemAsm(MemAsm.OP.lw, 0, point, var2reg.get(loadInstr));
        } else {
            new MemAsm(MemAsm.OP.lw, 0, point, Register.k0);
            new MemAsm(MemAsm.OP.sw, value2Offset.get(loadInstr), Register.sp, Register.k0);
        }
    }

    public static void buildRetInstr(Ret ret) {
        if(isMain){
            new LiAsm(Register.v0,10);
            new SyscallAsm();
            return;
        }
        Value retValue = ret.getOperands().get(0);
        if (retValue != null) {
            if (retValue instanceof Constant) {
                new LiAsm(Register.v0, ((Constant) retValue).getValue());
            } else if (var2reg.containsKey(retValue)) {
                new AluAsm(var2reg.get(retValue), null, AluAsm.OP.addiu, Register.v0, 0);
            } else {
                new MemAsm(MemAsm.OP.lw, value2Offset.get(retValue), Register.sp, Register.v0);
            }
        }
        new JumpAsm(Register.ra, null, JumpAsm.OP.jr);
    }

    public static void buildStore(Store storeInstr) {
        Register point = Register.k0;
        Value from = storeInstr.getFrom();
        Value to = storeInstr.getTo();
        if (to instanceof GlobalVar) {
            new LaAsm(Register.k0, to.getName().substring(1));
        } else if (var2reg.containsKey(to)) {
            point = var2reg.get(to);
        } else {
            new MemAsm(MemAsm.OP.lw, value2Offset.get(to), Register.sp, Register.k0);
        }
        if (from instanceof Constant) {
            new LiAsm(Register.k1, ((Constant) from).getValue());
            new MemAsm(MemAsm.OP.sw, 0, point, Register.k1);
        } else if (var2reg.containsKey(from)) {
            new MemAsm(MemAsm.OP.sw, 0, point, var2reg.get(from));
        } else {
            new MemAsm(MemAsm.OP.lw, value2Offset.get(from), Register.sp, Register.k1);
            new MemAsm(MemAsm.OP.sw, 0, point, Register.k1);
        }
    }

    public static void buildZext(Zext zextInstr) {
        Value value = zextInstr.getOperands().get(0);
        Register reg = Register.k0;
        if (value instanceof Constant) {
            new LiAsm(reg, ((Constant) value).getValue());
        } else if (var2reg.containsKey(value)) {
            reg = var2reg.get(value);
        } else {
            new MemAsm(MemAsm.OP.lw, value2Offset.get(value), Register.sp, Register.k0);
        }
        if (var2reg.containsKey(zextInstr)) {
            new AluAsm(reg, null, AluAsm.OP.addiu, var2reg.get(zextInstr), 0);
        } else {
            new MemAsm(MemAsm.OP.sw, value2Offset.get(zextInstr), Register.sp, reg);
        }
    }
    public static void buildOffsetLoad(OffsetLoad offsetLoad){
        Value base=offsetLoad.getOperands().get(0);
        Register baseReg=Register.k0;
        Register valueReg=Register.k1;
        int cons=((Constant)offsetLoad.getOperands().get(1)).getValue();
        if(var2reg.containsKey(offsetLoad)){
            valueReg=var2reg.get(offsetLoad);
        }
        else{
            new MemAsm(MemAsm.OP.lw,value2Offset.get(offsetLoad),Register.sp,Register.k1);
        }
        if(base instanceof GlobalVar){
            new LaAsm(Register.k0, base.getName().substring(1));
        }
        else if(var2reg.containsKey(base)){
            baseReg=var2reg.get(base);
        }
        else{
            new MemAsm(MemAsm.OP.lw,value2Offset.get(base),Register.sp,Register.k0);
        }
        new MemAsm(MemAsm.OP.lw,4*cons,baseReg,valueReg);
        if(valueReg==Register.k1){
            new MemAsm(MemAsm.OP.sw,value2Offset.get(offsetLoad),Register.sp,Register.k1);
        }
    }
    public static void buildOffsetStore(OffsetStore offsetStore){
        Value base=offsetStore.getOperands().get(1);
        Value from=offsetStore.getOperands().get(0);
        int cons=((Constant)offsetStore.getOperands().get(2)).getValue();
        Register baseReg=Register.k0;
        Register valueReg=Register.k1;
        if(var2reg.containsKey(from)){
            valueReg=var2reg.get(from);
        }
        else{
            if(from instanceof Constant){
                new LiAsm(Register.k1,((Constant) from).getValue());
            }
            else{
                new MemAsm(MemAsm.OP.lw,value2Offset.get(from),Register.sp,Register.k1);
            }
        }
        if(base instanceof GlobalVar){
            new LaAsm(Register.k0, base.getName().substring(1));
        }
        else if(var2reg.containsKey(base)){
            baseReg=var2reg.get(base);
        }
        else{
            new MemAsm(MemAsm.OP.lw,value2Offset.get(base),Register.sp,Register.k0);
        }
        new MemAsm(MemAsm.OP.sw,4*cons,baseReg,valueReg);
    }
    public static void buildMove(Move move) {
        Value to = move.getTo();
        Value from = move.getFrom();
        Register toReg = Register.k0;
        if (var2reg.containsKey(to)) {
            toReg = var2reg.get(to);
        }
        if (from instanceof Constant) {
            new LiAsm(toReg, ((Constant) from).getValue());
        } else if (var2reg.containsKey(from)) {
            new MoveAsm(toReg, var2reg.get(from));
        } else {
            new MemAsm(MemAsm.OP.lw, value2Offset.get(from), Register.sp, toReg);
        }
        if (toReg == Register.k0) {
            new MemAsm(MemAsm.OP.sw, value2Offset.get(to), Register.sp, toReg);
        }
    }
}
