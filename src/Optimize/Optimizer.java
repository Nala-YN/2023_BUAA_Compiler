package Optimize;
import LlvmIr.BasicBlock;
import LlvmIr.Global.Function;
import LlvmIr.Module;
import Util.Printer;

import java.io.IOException;

public class Optimizer {
    public static boolean againstLlvm=true;
    public static boolean basicOptimize=false;
    //	%t4 = add i32 %t6, 1
    //	move %t6,%t4
    public static void analyze(Module module){
        BlockSimplify.simplify(module);
        PhiOptimize.optimizePhi(module);
        CFGBuilder.buildCFG(module);
        SideEffectsAnalyze.analyzeSideEffects(module);
        ActiveVarAnalyze.buildInOut(module);
        LoopAnalysis.analyzeLoop(module);
    }
    public static void optimize(Module module) throws IOException {
        if(basicOptimize){
            BlockSimplify.simplify(module);
            CFGBuilder.buildCFG(module);
            Mem2Reg.optimize(module);
            Printer.printLlvmIr(module);
            CFGBuilder.buildCFG(module);
            ActiveVarAnalyze.buildInOut(module);
            RegAlloc.allocReg(module);
            PhiRemove.removePhi(module);

            //BlockSimplify.simplify(module);
            //Printer.printLlvmIr(module);
        }
        else{
            for(int i=1;i<=10;i++){
                analyze(module);
                GlobalVarLocalize.globalVarLocalize(module);
                analyze(module);
                Mem2Reg.optimize(module);
                analyze(module);
                GVN.optimize(module);
                analyze(module);
                FunctionInline.inlineFunction(module);
                analyze(module);
                UselessFuncEmit.emitUselessFunc(module);
                analyze(module);
                BlockSimplify.blockMerge(module);
                analyze(module);
                MemoryAccessOptimize.optimize(module);
                analyze(module);
                DeadCodeRemove.removeDeadCode(module);
                analyze(module);
                DeadCodeRemove.removeUselessCode(module);
                analyze(module);
                DeadCodeRemove.romoveDeadCall(module);
                analyze(module);
                GCM.moveInstrs(module);
            }
            GepFuse.fuseGep(module);
            Printer.printLlvmIr(module);
            analyze(module);
            RegAlloc.allocReg(module);
            PhiRemove.removePhi(module);
            BlockSimplify.rearrange(module);

            /*BlockSimplify.simplify(module);
            CFGBuilder.buildCFG(module);
            GlobalVarLocalize.globalVarLocalize(module);
            Mem2Reg.optimize(module);
            SideEffectsAnalyze.analyzeSideEffects(module);
            GVN.optimize(module);
            DeadCodeRemove.removeDeadCode(module);
            DeadCodeRemove.removeUselessCode(module);
            DeadCodeRemove.romoveDeadCall(module);
            PhiOptimize.optimizePhi(module);
            FunctionInline.inlineFunction(module);
            PhiOptimize.optimizePhi(module);
            BlockSimplify.simplify(module);
            CFGBuilder.buildCFG(module);
            SideEffectsAnalyze.analyzeSideEffects(module);
            GVN.optimize(module);
            DeadCodeRemove.removeDeadCode(module);
            DeadCodeRemove.removeUselessCode(module);
            DeadCodeRemove.romoveDeadCall(module);
            PhiOptimize.optimizePhi(module);
            BlockSimplify.blockMerge(module);
            MemoryAccessOptimize.optimize(module);
            BlockSimplify.simplify(module);
            CFGBuilder.buildCFG(module);
            SideEffectsAnalyze.analyzeSideEffects(module);
            for(int i=1;i<=10;i++){
                CFGBuilder.buildCFG(module);
                SideEffectsAnalyze.analyzeSideEffects(module);
                BlockSimplify.simplify(module);
                GVN.optimize(module);
                DeadCodeRemove.removeDeadCode(module);
                DeadCodeRemove.removeUselessCode(module);
                DeadCodeRemove.romoveDeadCall(module);
                PhiOptimize.optimizePhi(module);
                BlockSimplify.simplify(module);
                CFGBuilder.buildCFG(module);
                ActiveVarAnalyze.buildInOut(module);
                FunctionInline.inlineFunction(module);
                UselessFuncEmit.emitUselessFunc(module);
                GlobalVarLocalize.globalVarLocalize(module);
                Mem2Reg.optimize(module);
                MemoryAccessOptimize.optimize(module);
                BlockSimplify.blockMerge(module);
            }
            GepFuse.fuseGep(module);
            for(int i=1;i<=6;i++){
                CFGBuilder.buildCFG(module);
                SideEffectsAnalyze.analyzeSideEffects(module);
                LoopAnalysis.analyzeLoop(module);
                GCM.moveInstrs(module);
            }
            Printer.printLlvmIr(module);
            CFGBuilder.buildCFG(module);
            SideEffectsAnalyze.analyzeSideEffects(module);
            ActiveVarAnalyze.buildInOut(module);
            RegAlloc.allocReg(module);
            PhiRemove.removePhi(module);
            BlockSimplify.rearrange(module);*/
        }
        /*else{
            for(int i=1;i<=3;i++){
                BlockSimplify.simplify(module);
                CFGBuilder.buildCFG(module);
                Mem2Reg.optimize(module);
                GVN.optimize(module);
                DeadCodeRemove.removeDeadCode(module);
            }
            ActiveVarAnalyze.buildInOut(module);
            RegAlloc.allocReg(module);
            PhiRemove.removePhi(module);
            BlockSimplify.rearrange(module);
        }*/
        for(Function func: module.getFunctions()){
            if(func.getName().equals("@main")){
                for(BasicBlock block: func.getBlocks()){
                    System.out.println(block.getName()+"->"+block.getLoopDepth());
                }
            }
        }
    }
}
