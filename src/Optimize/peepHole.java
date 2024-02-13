package Optimize;

import LlvmIr.Instruction.Alu;
import LlvmIr.Instruction.Branch;
import LlvmIr.Instruction.Move;
import ToMips.AsmBuilder;
import ToMips.AsmInstr;
import ToMips.AsmInstruction.AluAsm;
import ToMips.AsmInstruction.BranchAsm;
import ToMips.AsmInstruction.CmpAsm;
import ToMips.AsmInstruction.Comment;
import ToMips.AsmInstruction.Global.GlobalAsm;
import ToMips.AsmInstruction.JumpAsm;
import ToMips.AsmInstruction.LaAsm;
import ToMips.AsmInstruction.LiAsm;
import ToMips.AsmInstruction.MemAsm;
import ToMips.AsmInstruction.MoveAsm;
import ToMips.AsmInstruction.MoveFromAsm;
import ToMips.AsmInstruction.MoveToAsm;
import ToMips.AsmInstruction.SyscallAsm;
import ToMips.LableAsm;
import ToMips.Register;
import jdk.jfr.Label;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;

public class peepHole {
    public static void jumpAsmDel(ArrayList<AsmInstr> asmInstrs){
        ArrayList<AsmInstr> asmInstrs1=new ArrayList<>(asmInstrs);
        for(int i=0;i<=asmInstrs1.size()-2;i++){
            AsmInstr asmInstr=asmInstrs1.get(i);
            AsmInstr asmInstr1=asmInstrs1.get(i+1);
            if(asmInstr instanceof JumpAsm
                    && asmInstr1 instanceof LableAsm
                    && ((JumpAsm) asmInstr).getLabel()!=null
                    && ((JumpAsm) asmInstr).getLabel().equals(((LableAsm) asmInstr1).getLabel())){
                asmInstrs.remove(asmInstr);
            }
        }
    }
    //addu with num 0->move
    public static void addToMove(ArrayList<AsmInstr> asmInstrs){
        AsmBuilder.autoAdd=false;
        ArrayList<AsmInstr> asmInstrs1=new ArrayList<>(asmInstrs);
        for(AsmInstr asmInstr:asmInstrs1){
            if(asmInstr instanceof AluAsm){
                AluAsm aluAsm= (AluAsm) asmInstr;
                if(aluAsm.getOp()== AluAsm.OP.addiu&&
                aluAsm.getNum()==0){
                    MoveAsm moveAsm=new MoveAsm(aluAsm.getTo(),aluAsm.getOperand1());
                    asmInstrs.set(asmInstrs.indexOf(aluAsm),moveAsm);
                }
            }
        }
        AsmBuilder.autoAdd=true;
    }
    //move $t1,$t2 ; move $t1,$t3
    //move $t1,$t1
    //move $t1,$t2;move $t2,$t1
    public static void deadMoveDel(ArrayList<AsmInstr> asmInstrs){
        ArrayList<AsmInstr> asmInstrs1=new ArrayList<>(asmInstrs);
        for(AsmInstr asmInstr:asmInstrs1){
            if(asmInstr instanceof MoveAsm
            && ((MoveAsm) asmInstr).getFrom()==((MoveAsm) asmInstr).getTo()){
                asmInstrs.remove(asmInstr);
            }
            if(asmInstr instanceof AluAsm){
                AluAsm aluAsm=(AluAsm) asmInstr;
                if(aluAsm.getOp()== AluAsm.OP.addiu&&
                aluAsm.getNum()==0&&
                aluAsm.getTo()==aluAsm.getOperand1()){
                    asmInstrs.remove(asmInstr);
                }
            }
        }
        asmInstrs1=new ArrayList<>(asmInstrs);
        for(int i=0;i<=asmInstrs1.size()-2;i++){
            if(asmInstrs1.get(i) instanceof MoveAsm&&
            asmInstrs1.get(i+1) instanceof MoveAsm){
                MoveAsm moveAsm1= (MoveAsm) asmInstrs1.get(i);
                MoveAsm moveAsm2= (MoveAsm) asmInstrs1.get(i+1);
                if(moveAsm1.getTo()==moveAsm2.getTo()||
                        moveAsm2.getTo()==moveAsm1.getFrom()&&moveAsm2.getFrom()==moveAsm1.getTo()){
                    asmInstrs.remove(moveAsm2);
                }
            }
        }
    }
    public static void sameMemSwLw(ArrayList<AsmInstr> asmInstrs){
        for(int i=0;i<=asmInstrs.size()-2;i++){
            AsmInstr asm1=asmInstrs.get(i);
            AsmInstr asm2=asmInstrs.get(i+1);
            if(asm1 instanceof MemAsm && ((MemAsm) asm1).getOp()== MemAsm.OP.sw
            && asm2 instanceof MemAsm && ((MemAsm) asm2).getOp()==MemAsm.OP.lw
            && ((MemAsm) asm1).getBase()==((MemAsm) asm2).getBase()
            && ((MemAsm) asm1).getOffset()==((MemAsm) asm2).getOffset()){
                MoveAsm moveAsm=new MoveAsm(((MemAsm) asm2).getValue(),((MemAsm) asm1).getValue());
                asmInstrs.set(i+1,moveAsm);
            }
        }
    }
    public static void liLaSameValueRemove(ArrayList<AsmInstr> asmInstrs){
        ArrayList<AsmInstr> copy=new ArrayList<>(asmInstrs);
        HashMap<Register,Integer> liReg=null;
        HashMap<Register, String> laReg=null;
        for(AsmInstr asmInstr:copy){
            if(asmInstr instanceof LableAsm){
                liReg=new HashMap<>();
                laReg=new HashMap<>();
            }
            else if(asmInstr instanceof Comment){
                continue;
            }
            else{
                Register to=null;
                if(asmInstr instanceof AluAsm aluAsm){
                    to=aluAsm.getTo();
                }
                else if(asmInstr instanceof CmpAsm cmpAsm){
                    to=cmpAsm.getTo();
                }
                else if(asmInstr instanceof LaAsm laAsm){
                    to=laAsm.getReg();
                    String name= laAsm.getName();
                    if(laReg.containsKey(to)&&laReg.get(to).equals(name)){
                        asmInstrs.remove(asmInstr);
                    }
                    laReg.put(to,name);
                    liReg.remove(to);
                    continue;
                }
                else if(asmInstr instanceof LiAsm liAsm){
                    to=liAsm.getReg();
                    int value=liAsm.getValue();
                    if(liReg.containsKey(to)&&liReg.get(to)==value){
                        asmInstrs.remove(asmInstr);
                    }
                    liReg.put(to,value);
                    laReg.remove(to);
                    continue;
                }
                else if(asmInstr instanceof MemAsm memAsm){
                    if(memAsm.getOp()== MemAsm.OP.lw){
                        to=memAsm.getValue();
                    }
                }
                else if(asmInstr instanceof SyscallAsm){
                    if(!liReg.containsKey(Register.v0)){
                        throw new RuntimeException();
                    }
                    else{
                        if(liReg.get(Register.v0)==5){
                            to=Register.v0;
                        }
                    }
                }
                else if(asmInstr instanceof MoveAsm moveAsm){
                    to=moveAsm.getTo();
                }
                else if(asmInstr instanceof JumpAsm jumpAsm){
                    if(jumpAsm.getOp()== JumpAsm.OP.jal){
                        liReg=new HashMap<>();
                        laReg=new HashMap<>();
                    }
                }
                else if(asmInstr instanceof MoveFromAsm moveFromAsm){
                    to=moveFromAsm.getTo();
                }
                if(to!=null){
                    liReg.remove(to);
                    laReg.remove(to);
                }
            }
        }
    }
    public static void deadDefRemove(ArrayList<AsmInstr> asmInstrs){
        //if(1==1)return;
        HashMap<Register,AsmInstr> noUseDef = null;
        ArrayList<AsmInstr> copy=new ArrayList<>(asmInstrs);
        HashMap<Register,Integer> liReg=null;
        for(AsmInstr asmInstr:copy){
            if(asmInstr instanceof LableAsm){
                noUseDef=new HashMap<>();
                liReg=new HashMap<>();
            }
            else if(asmInstr instanceof Comment){
                continue;
            }
            else{
                Register use1=null;
                Register use2=null;
                Register to=null;
                if(asmInstr instanceof AluAsm aluAsm){
                    use1=aluAsm.getOperand1();
                    use2=aluAsm.getOperand2();
                    to=aluAsm.getTo();
                }
                else if(asmInstr instanceof BranchAsm branchAsm){
                    use1=branchAsm.getReg1();
                    use2=branchAsm.getReg2();
                }
                else if(asmInstr instanceof CmpAsm cmpAsm){
                    use1=cmpAsm.getOperand1();
                    use2=cmpAsm.getOperand2();
                    to=cmpAsm.getTo();
                }
                else if(asmInstr instanceof LaAsm laAsm){
                    to=laAsm.getReg();
                }
                else if(asmInstr instanceof LiAsm liAsm){
                    to=liAsm.getReg();
                }
                else if(asmInstr instanceof SyscallAsm){
                    use1=Register.v0;
                    use2=Register.a0;
                }
                else if(asmInstr instanceof MemAsm memAsm){
                    if(memAsm.getOp()== MemAsm.OP.lw){
                        use1=memAsm.getBase();
                        to=memAsm.getValue();
                    }
                    else{
                        use1=memAsm.getBase();
                        use2=memAsm.getValue();
                    }
                }
                else if(asmInstr instanceof MoveAsm moveAsm){
                    use1=moveAsm.getFrom();
                    to=moveAsm.getTo();
                }
                else if(asmInstr instanceof JumpAsm jumpAsm){
                    if(jumpAsm.getOp()== JumpAsm.OP.jal){
                        noUseDef.remove(Register.a1);
                        noUseDef.remove(Register.a2);
                        noUseDef.remove(Register.a3);
                    }
                    else if(jumpAsm.getOp()== JumpAsm.OP.jr){
                        noUseDef.remove(Register.v0);
                    }
                }
                else if(asmInstr instanceof MoveFromAsm moveFromAsm){
                    to=moveFromAsm.getTo();
                }
                else if(asmInstr instanceof MoveToAsm moveToAsm){
                    use1=moveToAsm.getTo();
                }
                if(use1!=null){
                    noUseDef.remove(use1);
                }
                if(use2!=null){
                    noUseDef.remove(use2);
                }
                if(to!=null/*&&to!=Register.a0&&to!=Register.a1&&to!=Register.a2&&to!=Register.a3
                        &&to!=Register.v0*/&&to!=Register.ra&&to!=Register.sp){
                    if(noUseDef.containsKey(to)){
                        asmInstrs.remove(noUseDef.get(to));
                    }
                    noUseDef.put(to,asmInstr);
                }
            }
        }
    }
    public static void uselessCallMemEmit(ArrayList<AsmInstr> instrs){
        ArrayList<AsmInstr> copy=new ArrayList<>(instrs);
        for(AsmInstr instr:copy){
            if(instr instanceof JumpAsm jumpAsm && jumpAsm.getOp()== JumpAsm.OP.jal){
                for(MemAsm lw:jumpAsm.getLws()){
                    if(!instrs.contains(lw)){
                        Register reg=lw.getValue();
                        MemAsm uselessSw=null;
                        for(MemAsm sw: jumpAsm.getSws()){
                            if(sw.getValue()==reg){
                                uselessSw=sw;
                                break;
                            }
                        }
                        if(uselessSw!=null){
                            instrs.remove(uselessSw);
                        }
                    }
                }
            }
        }
    }
/*    public static void mulDivToSll(ArrayList<AsmInstr> asmInstrs){
        ArrayList<AsmInstr> copy=new ArrayList<>(asmInstrs);
        for(AsmInstr instr:copy){
            if(instr instanceof AluAsm aluAsm && aluAsm.getOp()== AluAsm.OP.mul){

            }
        }
    }*/
}
