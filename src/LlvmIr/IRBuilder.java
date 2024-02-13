package LlvmIr;

import LlvmIr.Global.CstStr;
import LlvmIr.Global.Function;
import LlvmIr.Global.GlobalVar;
import LlvmIr.Instruction.Alloca;
import LlvmIr.Instruction.Alu;
import LlvmIr.Instruction.Branch;
import LlvmIr.Instruction.Call;
import LlvmIr.Instruction.IO.GetInt;
import LlvmIr.Instruction.GetPtr;
import LlvmIr.Instruction.Icmp;
import LlvmIr.Instruction.Jmp;
import LlvmIr.Instruction.IO.PutInt;
import LlvmIr.Instruction.IO.PutStr;
import LlvmIr.Instruction.Load;
import LlvmIr.Instruction.Ret;
import LlvmIr.Instruction.Store;
import LlvmIr.Instruction.Zext;
import LlvmIr.Type.LlvmType;
import LlvmIr.Type.PointerType;
import Symbol.FuncDefSymbol;
import Symbol.ConstSymbol;
import Symbol.Symbol;
import Symbol.SymbolCenter;
import Symbol.VarSymbol;
import SyntaxParser.ExpNode.AddExpNode;
import SyntaxParser.ExpNode.EqExpNode;
import SyntaxParser.ExpNode.ExpNode;
import SyntaxParser.ExpNode.LAndExpNode;
import SyntaxParser.ExpNode.LOrExpNode;
import SyntaxParser.ExpNode.MulExpNode;
import SyntaxParser.ExpNode.PrimaryExpNode;
import SyntaxParser.ExpNode.RelExpNode;
import SyntaxParser.ExpNode.UnaryExpNode;
import SyntaxParser.Node;
import SyntaxParser.OtherNode.BlockItemNode;
import SyntaxParser.OtherNode.BlockNode;
import SyntaxParser.OtherNode.CompUnitNode;
import SyntaxParser.OtherNode.CondNode;
import SyntaxParser.OtherNode.ConstDeclNode;
import SyntaxParser.OtherNode.ConstDefNode;
import SyntaxParser.OtherNode.DeclNode;
import SyntaxParser.OtherNode.FuncDefNode;
import SyntaxParser.OtherNode.FuncFParamNode;
import SyntaxParser.OtherNode.FuncFParamsNode;
import SyntaxParser.OtherNode.FuncRParamsNode;
import SyntaxParser.OtherNode.InitValNode;
import SyntaxParser.OtherNode.LValNode;
import SyntaxParser.OtherNode.MainFuncDefNode;
import SyntaxParser.OtherNode.TokenNode;
import SyntaxParser.OtherNode.VarDeclNode;
import SyntaxParser.OtherNode.VarDefNode;
import SyntaxParser.StmtNode.AssignStmtNode;
import SyntaxParser.StmtNode.BlockStmtNode;
import SyntaxParser.StmtNode.BreakStmtNode;
import SyntaxParser.StmtNode.ContinueStmtNode;
import SyntaxParser.StmtNode.ExpStmtNode;
import SyntaxParser.StmtNode.ForAssignStmtNode;
import SyntaxParser.StmtNode.ForStmtNode;
import SyntaxParser.StmtNode.GetintStmtNode;
import SyntaxParser.StmtNode.IfStmtNode;
import SyntaxParser.StmtNode.PrintStmtNode;
import SyntaxParser.StmtNode.ReturnStmtNode;
import SyntaxParser.StmtNode.WhileStmtNode;
import TokenParser.TokenType;

import java.util.ArrayList;
import java.util.Stack;

public class IRBuilder {
    //CompUnit → {Decl} {FuncDef} MainFuncDef
    private int varId = 0;
    private int blockId = 0;
    private int funcId = 0;
    private int globalId = 0;
    private int strId = 0;
    private int paramId = 0;
    private boolean isGlobal = false;
    private Function curFunc;
    private BasicBlock curBlock;
    private Stack<Loop> loopStack = new Stack<>();
    private Module module = new Module();
    public static String blockName = "b";
    private static String globalName = "@g";
    private static String strName = "@str";
    public static String paraName = "%a";
    public static String tempName = "%t";
    private static String funcName = "@func";

    public IRBuilder() {
    }

    private int getVarId() {
        if(varId==152){
            int u=1;
        }
        varId++;
        return varId - 1;
    }

    private int getFuncId() {
        funcId++;
        return funcId - 1;
    }

    private int getBlockId() {
        blockId++;
        return blockId - 1;
    }

    private int getGlobalId() {
        globalId++;
        return globalId - 1;
    }

    private int getStrId() {
        strId++;
        return strId - 1;
    }

    private int getParamId() {
        paramId++;
        return paramId - 1;
    }

    public Module getModule() {
        return module;
    }

    public void buildCompUnit(CompUnitNode compUnitNode) {
        SymbolCenter.enterBlock();
        ArrayList<DeclNode> declNodes = compUnitNode.getDeclNodes();
        ArrayList<FuncDefNode> funcDefNodes = compUnitNode.getFuncDefNodes();
        MainFuncDefNode mainFuncDefNode = compUnitNode.getMainFuncDefNode();
        isGlobal = true;
        for (DeclNode declNode : declNodes) {
            buildDecl(declNode);
        }
        isGlobal = false;
        for (FuncDefNode funcDefNode : funcDefNodes) {
            buildFuncDef(funcDefNode);
        }
        buildMainFuncDef(mainFuncDefNode);
        SymbolCenter.enterBlock();
    }

    //Decl → ConstDecl | VarDecl
    public void buildDecl(DeclNode declNode) {
        if (declNode.getConstDeclNode() != null) {
            buildConstDecl(declNode.getConstDeclNode());
        } else {
            buildVarDecl(declNode.getVarDeclNode());
        }
    }

    // FuncDef → FuncType Ident '(' [FuncFParams] ')' Block
    public void buildFuncDef(FuncDefNode funcDefNode) {
        FuncDefSymbol symbol = funcDefNode.getFuncDefSymbol();
        SymbolCenter.addSymbol(funcDefNode.getFuncDefSymbol());
        SymbolCenter.enterFunc(symbol.getDefineType());
        varId = 0;
        blockId = 0;
        paramId = 0;
        LlvmType retType = symbol.getDefineType() == TokenType.VOIDTK ? LlvmType.Void : LlvmType.Int32;
        curFunc = new Function(funcName + getFuncId(), retType);
        module.addFunction(curFunc);
        symbol.setLlvmIr(curFunc);
        curBlock = new BasicBlock(blockName + getBlockId(), curFunc);
        curFunc.addBasicBlock(curBlock);
        if (funcDefNode.getFuncFParamsNode() != null) {
            buildFuncFParams(funcDefNode.getFuncFParamsNode());
        }
        buildBlock(funcDefNode.getBlockNode());
        if (!curBlock.hasRet()) {
            curBlock.addInstr(new Ret(null, curBlock));
        }
        SymbolCenter.leaveFunc();
        curFunc.setVarId(varId);
        curFunc.setBlockId(blockId);
    }

    //MainFuncDef → 'int' 'main' '(' ')' Block
    public void buildMainFuncDef(MainFuncDefNode mainFuncDefNode) {
        FuncDefSymbol funcDefSymbol = new FuncDefSymbol("main", TokenType.INTTK);
        SymbolCenter.addSymbol(funcDefSymbol);
        SymbolCenter.enterFunc(TokenType.INTTK);
        varId = 0;
        blockId = 0;
        curFunc = new Function("@main", LlvmType.Int32);
        module.addFunction(curFunc);
        curBlock = new BasicBlock(blockName + getBlockId(), curFunc);
        curFunc.addBasicBlock(curBlock);
        buildBlock(mainFuncDefNode.getBlockNode());
        SymbolCenter.leaveFunc();
        curFunc.setVarId(varId);
        curFunc.setBlockId(blockId);
    }

    //ConstDecl → 'const' BType ConstDef { ',' ConstDef } ';'
    public void buildConstDecl(ConstDeclNode constDeclNode) {
        for (ConstDefNode constDefNode : constDeclNode.getConstDefNodes()) {
            buildConstDef(constDefNode);
        }
    }

    //VarDecl → BType VarDef { ',' VarDef } ';'
    public void buildVarDecl(VarDeclNode varDeclNode) {
        for (VarDefNode varDefNode : varDeclNode.getVarDefNodes()) {
            buildVarDef(varDefNode);
        }
    }

    //FuncFParams → FuncFParam { ',' FuncFParam }
    public void buildFuncFParams(FuncFParamsNode funcFParamsNode) {
        for (FuncFParamNode funcFParamNode : funcFParamsNode.getFuncFParamNodes()) {
            buildFuncFParam(funcFParamNode);
        }
    }

    //Block → '{' { BlockItem } '}'
    public void buildBlock(BlockNode blockNode) {
        for (BlockItemNode blockItemNode : blockNode.getBlockItemNodes()) {
            buildBlockItem(blockItemNode);
        }
    }

    //ConstDef → Ident { '[' ConstExp ']' } '=' ConstInitVal
    public void buildConstDef(ConstDefNode constDefNode) {
        ConstSymbol constSymbol = constDefNode.getConstSymbol();
        SymbolCenter.addSymbol(constSymbol);
        if (isGlobal) {
            GlobalVar globalVar = new GlobalVar(globalName + getGlobalId(), new PointerType(constSymbol.getLlvmType()),
                    constSymbol.getInitial(), constSymbol.isZeroInitial(), constSymbol.getLlvmType().getArrayLen(),true);
            module.addGlobalVar(globalVar);
            constSymbol.setLlvmIr(globalVar);
        } else {
            if (constSymbol.getDim() == 0) {
                Alloca allocaInstr = new Alloca(tempName + getVarId(), curBlock, LlvmType.Int32);
                curBlock.addInstr(allocaInstr);
                Store storeInstr = new Store(new Constant(constSymbol.getInitial().get(0)), allocaInstr, curBlock);
                curBlock.addInstr(storeInstr);
                constSymbol.setLlvmIr(allocaInstr);
            } else {
                Alloca allocaInstr = new Alloca(tempName + getVarId(), curBlock, constSymbol.getLlvmType(),constSymbol.getInitial());
                curBlock.addInstr(allocaInstr);
                GetPtr firstPtr = new GetPtr(tempName + getVarId(), allocaInstr, new Constant(0), curBlock);
                Store storeInstr = new Store(new Constant(constSymbol.getInitial().get(0)), firstPtr, curBlock);
                curBlock.addInstr(firstPtr);
                curBlock.addInstr(storeInstr);
                constSymbol.setLlvmIr(firstPtr);
                for (int i = 1; i <= constSymbol.getInitial().size() - 1; i++) {
                    GetPtr getPtrInstr = new GetPtr(tempName + getVarId(), firstPtr, new Constant(i), curBlock);
                    storeInstr = new Store(new Constant(constSymbol.getInitial().get(i)), getPtrInstr, curBlock);
                    curBlock.addInstr(getPtrInstr);
                    curBlock.addInstr(storeInstr);
                }
            }
        }
    }

    // VarDef → Ident { '[' ConstExp ']' } // 包含普通变量、一维数组、二维数组定义
    //| Ident { '[' ConstExp ']' } '=' InitVal
    public void buildVarDef(VarDefNode varDefNode) {
        VarSymbol varSymbol = varDefNode.getVarSymbol();
        SymbolCenter.addSymbol(varSymbol);
        if (isGlobal) {
            GlobalVar globalVar = new GlobalVar(globalName + getGlobalId(), new PointerType(varSymbol.getLlvmType()),
                    varSymbol.getInitial(), varSymbol.isZeroInitial(), varSymbol.getLlvmType().getArrayLen(),false);
            module.addGlobalVar(globalVar);
            varSymbol.setLlvmIr(globalVar);
        } else {
            InitValNode initValNode = varDefNode.getInitValNode();
            if (varSymbol.getDim() == 0) {
                Alloca allocaInstr = new Alloca(tempName + getVarId(), curBlock, LlvmType.Int32);
                varSymbol.setLlvmIr(allocaInstr);
                curBlock.addInstr(allocaInstr);
                if (initValNode != null) {
                    ArrayList<Value> values = buildInitVal(initValNode);
                    Store storeInstr = new Store(values.get(0), allocaInstr, curBlock);
                    curBlock.addInstr(storeInstr);
                }
            } else {
                Alloca allocaInstr = new Alloca(tempName + getVarId(), curBlock, varSymbol.getLlvmType());
                curBlock.addInstr(allocaInstr);
                if (initValNode != null) {
                    ArrayList<Value> values = buildInitVal(initValNode);
                    GetPtr firstPtr = new GetPtr(tempName + getVarId(), allocaInstr, new Constant(0), curBlock);
                    Store storeInstr = new Store( values.get(0), firstPtr, curBlock);
                    curBlock.addInstr(firstPtr);
                    curBlock.addInstr(storeInstr);
                    varSymbol.setLlvmIr(firstPtr);
                    for (int i = 1; i <= values.size() - 1; i++) {
                        GetPtr getPtrInstr = new GetPtr(tempName + getVarId(), firstPtr, new Constant(i), curBlock);
                        storeInstr = new Store( values.get(i), getPtrInstr, curBlock);
                        curBlock.addInstr(getPtrInstr);
                        curBlock.addInstr(storeInstr);
                    }
                }
                else{
                    varSymbol.setLlvmIr(allocaInstr);
                }
            }
        }
    }

    // FuncFParam → BType Ident ['[' ']' { '[' ConstExp ']' }]
    public void buildFuncFParam(FuncFParamNode funcFParamNode) {
        VarSymbol varSymbol = funcFParamNode.getVarSymbol();
        SymbolCenter.addSymbol(varSymbol);
        LlvmType llvmType = varSymbol.getDim() == 0 ? LlvmType.Int32 : new PointerType(LlvmType.Int32);
        Param param = new Param(paraName + getParamId(), llvmType, curFunc);
        curFunc.addParam(param);
        if (llvmType == LlvmType.Int32) {
            Alloca allocaInstr = new Alloca(tempName + getVarId(), curBlock, LlvmType.Int32);
            Store storeInstr = new Store(param, allocaInstr, curBlock);
            curBlock.addInstr(allocaInstr);
            curBlock.addInstr(storeInstr);
            varSymbol.setLlvmIr(allocaInstr);
        } else {
            varSymbol.setLlvmIr(param);
        }
    }

    // BlockItem → Decl | Stmt
    public void buildBlockItem(BlockItemNode blockItemNode) {
        if (blockItemNode.getDeclNode() != null) {
            buildDecl(blockItemNode.getDeclNode());
        } else {
            buildStmt(blockItemNode.getStmtNode());
        }
    }

    //InitVal → Exp | '{' [ InitVal { ',' InitVal } ] '}'
    public ArrayList<Value> buildInitVal(InitValNode initValNode) {
        ExpNode expNode = initValNode.getExpNode();
        ArrayList<InitValNode> initValNodes = initValNode.getInitValNodes();
        ArrayList<Value> ret = new ArrayList<>();
        if (expNode != null) {
            Value value = buildExp(expNode);
            ret.add(value);
        }
        if (initValNodes != null) {
            for (InitValNode node : initValNodes) {
                ret.addAll(buildInitVal(node));
            }
        }
        return ret;
    }

    // Stmt → LVal '=' Exp ';' // 每种类型的语句都要覆盖
    //| [Exp] ';' //有无Exp两种情况
    //| Block
    //| 'if' '(' Cond ')' Stmt [ 'else' Stmt ] // 1.有else 2.无else
    //| 'for' '(' [ForStmt] ';' [Cond] ';' [ForStmt] ')' Stmt // 1. 无缺省 2. 缺省第一个
    //ForStmt 3. 缺省Cond 4. 缺省第二个ForStmt
    //| 'break' ';' | 'continue' ';'
    //| 'return' [Exp] ';' // 1.有Exp 2.无Exp
    //| LVal '=' 'getint''('')'';'
    //| 'printf''('FormatString{','Exp}')'';' // 1.有Exp 2.无Exp
    public void buildStmt(Node node) {
        if (node instanceof AssignStmtNode) {
            buildAssignStmt((AssignStmtNode) node);
        } else if (node instanceof ExpStmtNode) {
            buildExpStmt((ExpStmtNode) node);
        } else if (node instanceof ForStmtNode) {
            buildForStmt((ForStmtNode) node);
        } else if (node instanceof WhileStmtNode) {
            buildWhileStmt((WhileStmtNode) node);
        } else if (node instanceof BlockStmtNode) {
            buildBlockStmt((BlockStmtNode) node);
        } else if (node instanceof IfStmtNode) {
            buildIfStmt((IfStmtNode) node);
        } else if (node instanceof BreakStmtNode) {
            buildBreakStmt((BreakStmtNode) node);
        } else if (node instanceof ContinueStmtNode) {
            buildContinueStmt((ContinueStmtNode) node);
        } else if (node instanceof ReturnStmtNode) {
            buildRetStmt((ReturnStmtNode) node);
        } else if (node instanceof GetintStmtNode) {
            buildGetIntStmt((GetintStmtNode) node);
        } else if (node instanceof PrintStmtNode) {
            buildPrintStmt((PrintStmtNode) node);
        }
    }

    //Exp → AddExp
    public Value buildExp(ExpNode expNode) {
        return buildAddExp(expNode.getAddExpNode());
    }

    //LVal '=' Exp ';'
    public void buildAssignStmt(AssignStmtNode assignStmtNode) {
        Value lVal = buildLValForAssign(assignStmtNode.getlValNode());
        Value exp = buildExp(assignStmtNode.getExpNode());
        Store storeInstr = new Store( exp, lVal, curBlock);
        curBlock.addInstr(storeInstr);
    }

    //[Exp] ';'
    public void buildExpStmt(ExpStmtNode expStmtNode) {
        ExpNode expNode = expStmtNode.getExpNode();
        if (expNode != null) {
            buildExp(expNode);
        }
    }

    //Block
    public void buildBlockStmt(BlockStmtNode blockStmtNode) {
        SymbolCenter.enterBlock();
        buildBlock(blockStmtNode.getBlockNode());
        SymbolCenter.leaveBlock();
    }

    //'if' '(' Cond ')' Stmt [ 'else' Stmt ]
    public void buildIfStmt(IfStmtNode ifStmtNode) {
        CondNode condNode = ifStmtNode.getCondNode();
        Node stmtNode = ifStmtNode.getStmtNode();
        Node elseNode = ifStmtNode.getElseStmtNode();
        BasicBlock trueBlock = new BasicBlock(blockName + getBlockId(), curFunc);
        if (elseNode != null) {
            BasicBlock elseBlock = new BasicBlock(blockName + getBlockId(), curFunc);
            BasicBlock followBlock = new BasicBlock(blockName + getBlockId(), curFunc);
            curFunc.addBasicBlock(trueBlock);
            curFunc.addBasicBlock(elseBlock);
            curFunc.addBasicBlock(followBlock);
            buildCond(condNode, trueBlock, elseBlock);
            curBlock = trueBlock;
            buildStmt(stmtNode);
            Jmp jmpInstr = new Jmp(followBlock, curBlock);
            curBlock.addInstr(jmpInstr);
            curBlock = elseBlock;
            buildStmt(elseNode);
            jmpInstr = new Jmp(followBlock, curBlock);
            curBlock.addInstr(jmpInstr);
            curBlock = followBlock;
        } else {
            BasicBlock followBlock = new BasicBlock(blockName + getBlockId(), curFunc);
            curFunc.addBasicBlock(trueBlock);
            curFunc.addBasicBlock(followBlock);
            buildCond(condNode, trueBlock, followBlock);
            curBlock = trueBlock;
            buildStmt(stmtNode);
            Jmp jmpInstr = new Jmp(followBlock, curBlock);
            curBlock.addInstr(jmpInstr);
            curBlock = followBlock;
        }
    }

    public void buildBreakStmt(BreakStmtNode breakStmtNode) {
        Jmp jmp = new Jmp(loopStack.peek().getFollowBlock(), curBlock);
        curBlock.addInstr(jmp);
    }

    public void buildContinueStmt(ContinueStmtNode continueStmtNode) {
        Jmp jmp = new Jmp(loopStack.peek().getCondBlock(), curBlock);
        curBlock.addInstr(jmp);
    }

    // 'return' [Exp] ';'
    public void buildRetStmt(ReturnStmtNode returnStmtNode) {
        ExpNode expNode = returnStmtNode.getExpNode();
        Value exp = null;
        if (expNode != null) {
            exp = buildExp(expNode);
        } else if (curFunc.getRetType() != LlvmType.Void) {
            exp = new Constant(0);
        }
        Ret ret = new Ret(exp, curBlock);
        curBlock.addInstr(ret);
    }

    public void buildGetIntStmt(GetintStmtNode getintStmtNode) {
        GetInt getIntInstr = new GetInt(tempName + getVarId(), curBlock);
        curBlock.addInstr(getIntInstr);
        LValNode lValNode = getintStmtNode.getlValNode();
        Value point = buildLValForAssign(lValNode);
        Store storeInstr = new Store(getIntInstr, point, curBlock);
        curBlock.addInstr(storeInstr);
    }

    public void buildPrintStmt(PrintStmtNode printStmtNode) {
        String content = printStmtNode.getFmtStrNode().getToken().getValue();
        content = content.substring(1, content.length() - 1);
        ArrayList<Value> exps = new ArrayList<>();
        for (ExpNode expNode : printStmtNode.getExpNodes()) {
            exps.add(buildExp(expNode));
        }
        StringBuilder sb = new StringBuilder();
        for (int i = 0; i <= content.length() - 1; i++) {
            char ch = content.charAt(i);
            if (ch == '%') {
                if (sb.length() != 0) {
                    CstStr cstStr = new CstStr(strName + getStrId(), sb.toString());
                    module.addStr(cstStr);
                    PutStr putStrInstr = new PutStr(curBlock, cstStr);
                    curBlock.addInstr(putStrInstr);
                    sb = new StringBuilder();
                }
                i++;
                PutInt putIntInstr = new PutInt(exps.get(0), curBlock);
                curBlock.addInstr(putIntInstr);
                exps.remove(0);
            } else if (ch == '\\') {
                i++;
                sb.append('\n');
            } else {
                sb.append(ch);
            }
        }
        if (sb.length() != 0) {
            CstStr cstStr = new CstStr(strName + getStrId(), sb.toString());
            module.addStr(cstStr);
            PutStr putStrInstr = new PutStr(curBlock, cstStr);
            curBlock.addInstr(putStrInstr);
        }
    }

    //AddExp → MulExp | AddExp ('+' | '−') MulExp
    public Value buildAddExp(AddExpNode addExpNode) {
        ArrayList<TokenNode> addMinus = addExpNode.getAddMinusNodes();
        ArrayList<MulExpNode> mulExpNodes = addExpNode.getMulExpNodes();
        Value operand1 = buildMulExp(mulExpNodes.get(0));
        mulExpNodes.remove(0);
        for (int i = 0; i <= mulExpNodes.size() - 1; i++) {
            Value operand2 = buildMulExp(mulExpNodes.get(i));
            Alu.OP op = addMinus.get(i).getToken().getType() == TokenType.PLUS ? Alu.OP.ADD : Alu.OP.SUB;
            Alu aluInstr = new Alu(tempName + getVarId(), operand1, operand2, op, curBlock);
            operand1 = aluInstr;
            curBlock.addInstr(aluInstr);
        }
        return operand1;
    }

    // LVal → Ident {'[' Exp ']'}
    public Value buildLValForAssign(LValNode lValNode) {
        String name = lValNode.getIdentNode().getToken().getValue();
        VarSymbol symbol = (VarSymbol) SymbolCenter.getSymbol(name);
        ArrayList<Value> values = new ArrayList<>();
        for (ExpNode expNode : lValNode.getExpNodes()) {
            values.add(buildExp(expNode));
        }
        if (symbol.getDim() == 0) {
            return symbol.getLlvmIr();
        } else if (symbol.getDim() == 1) {
            GetPtr getPtrInstr = new GetPtr(tempName + getVarId(), symbol.getLlvmIr(), values.get(0), curBlock);
            curBlock.addInstr(getPtrInstr);
            return getPtrInstr;
        } else {
            Alu aluInstr = new Alu(tempName + getVarId(), new Constant(symbol.getDimLens().get(1))
                    , values.get(0), Alu.OP.MUL, curBlock);
            curBlock.addInstr(aluInstr);
            aluInstr = new Alu(tempName + getVarId(), aluInstr, values.get(1), Alu.OP.ADD, curBlock);
            curBlock.addInstr(aluInstr);
            GetPtr getPtrInstr = new GetPtr(tempName + getVarId(), symbol.getLlvmIr(), aluInstr, curBlock);
            curBlock.addInstr(getPtrInstr);
            return getPtrInstr;
        }
    }

    //Cond → LOrExp
    public void buildCond(CondNode condNode, BasicBlock trueBlock, BasicBlock elseBlock) {
        buildLOrExp(condNode.getlOrExpNode(), trueBlock, elseBlock);
    }

    // 'for' '(' [ForStmt] ';' [Cond] ';' [ForStmt] ')' Stmt
    public void buildForStmt(ForStmtNode forStmtNode) {
        ForAssignStmtNode leftAssign = forStmtNode.getLeftAssign();
        CondNode cond = forStmtNode.getCondNode();
        ForAssignStmtNode rightAssign = forStmtNode.getRightAssign();
        Node stmtNode = forStmtNode.getStmtNode();
        if (leftAssign != null) {
            buildForAssignStmt(leftAssign);
        }
        BasicBlock condBlock = new BasicBlock(blockName + getBlockId(), curFunc);
        BasicBlock stmtBlock = new BasicBlock(blockName + getBlockId(), curFunc);
        BasicBlock forStmtBlock = new BasicBlock(blockName + getBlockId(), curFunc);
        BasicBlock followBlock = new BasicBlock(blockName + getBlockId(), curFunc);
        curFunc.addBasicBlock(condBlock);
        curFunc.addBasicBlock(stmtBlock);
        curFunc.addBasicBlock(forStmtBlock);
        curFunc.addBasicBlock(followBlock);
        Jmp jmpInstr = new Jmp(condBlock, curBlock);
        curBlock.addInstr(jmpInstr);
        if (cond == null) {
            jmpInstr = new Jmp(stmtBlock, condBlock);
            condBlock.addInstr(jmpInstr);
        } else {
            curBlock = condBlock;
            buildCond(cond, stmtBlock, followBlock);
        }
        if (rightAssign != null) {
            curBlock = forStmtBlock;
            buildForAssignStmt(rightAssign);
        }
        Jmp jmp = new Jmp(condBlock, forStmtBlock);
        forStmtBlock.addInstr(jmp);
        Loop loop = new Loop(forStmtBlock, stmtBlock, followBlock);
        loopStack.push(loop);
        curBlock = stmtBlock;
        buildStmt(stmtNode);
        jmp = new Jmp(forStmtBlock, curBlock);
        curBlock.addInstr(jmp);
        curBlock = followBlock;
        loopStack.pop();
    }

    public void buildWhileStmt(WhileStmtNode whileStmtNode) {
        CondNode condNode = whileStmtNode.getCondNode();
        Node stmtNode = whileStmtNode.getStmtNode();
        BasicBlock condBlock = new BasicBlock(blockName + getBlockId(), curFunc);
        BasicBlock followBlock = new BasicBlock(blockName + getBlockId(), curFunc);
        BasicBlock stmtBlock = new BasicBlock(blockName + getBlockId(), curFunc);
        curFunc.addBasicBlock(condBlock);
        curFunc.addBasicBlock(stmtBlock);
        curFunc.addBasicBlock(followBlock);
        Jmp jmpInstr = new Jmp(condBlock, curBlock);
        curBlock.addInstr(jmpInstr);
        curBlock = condBlock;
        buildCond(condNode, stmtBlock, followBlock);
        Loop loop = new Loop(condBlock, stmtBlock, followBlock);
        loopStack.push(loop);
        curBlock = stmtBlock;
        buildStmt(stmtNode);
        jmpInstr = new Jmp(condBlock, curBlock);
        curBlock.addInstr(jmpInstr);
        curBlock = followBlock;
        loopStack.pop();
    }

    //MulExp → UnaryExp | MulExp ('*' | '/' | '%') UnaryExp
    public Value buildMulExp(MulExpNode mulExpNode) {
        ArrayList<TokenNode> tokenNodes = mulExpNode.getTokenNodes();
        ArrayList<UnaryExpNode> unaryExpNodes = mulExpNode.getUnaryExpNodes();
        Value operand1 = buildUnaryExp(unaryExpNodes.get(0));
        unaryExpNodes.remove(0);
        for (int i = 0; i <= tokenNodes.size() - 1; i++) {
            Value operand2 = buildUnaryExp(unaryExpNodes.get(i));
            TokenType tokenType = tokenNodes.get(i).getToken().getType();
            Alu.OP op = tokenType == TokenType.MULT ? Alu.OP.MUL : tokenType == TokenType.DIV ? Alu.OP.SDIV : Alu.OP.SREM;
            Alu aluInstr = new Alu(tempName + getVarId(), operand1, operand2, op, curBlock);
            curBlock.addInstr(aluInstr);
            operand1 = aluInstr;
        }
        return operand1;
    }

    //LOrExp → LAndExp | LOrExp '||' LAndExp
    public void buildLOrExp(LOrExpNode lOrExpNode, BasicBlock trueBlock, BasicBlock elseBlock) {
        ArrayList<LAndExpNode> lAndExpNodes = lOrExpNode.getlAndExpNodes();
        for (int i = 0; i <= lAndExpNodes.size() - 2; i++) {
            BasicBlock nextBlock = new BasicBlock(blockName + getBlockId(), curFunc);
            curFunc.addBasicBlock(nextBlock);
            buildLAndExp(lAndExpNodes.get(i), trueBlock, nextBlock);
            curBlock = nextBlock;
        }
        buildLAndExp(lAndExpNodes.get(lAndExpNodes.size() - 1), trueBlock, elseBlock);
    }

    // ForStmt → LVal '=' Exp
    public void buildForAssignStmt(ForAssignStmtNode forAssignStmtNode) {
        Value lVal = buildLValForAssign(forAssignStmtNode.getlValNode());
        Value exp = buildExp(forAssignStmtNode.getExpNode());
        Store storeInstr = new Store(exp, lVal, curBlock);
        curBlock.addInstr(storeInstr);
    }

    //UnaryExp → PrimaryExp | Ident '(' [FuncRParams] ')'| UnaryOp UnaryExp
    public Value buildUnaryExp(UnaryExpNode unaryExpNode) {
        if (unaryExpNode.getPrimaryExpNode() != null) {
            return buildPrimaryExp(unaryExpNode.getPrimaryExpNode());
        } else if (unaryExpNode.getIdentNode() != null) {
            String name = unaryExpNode.getIdentNode().getToken().getValue();
            ArrayList<Value> paras = new ArrayList<>();
            if (unaryExpNode.getFuncRParamsNode() != null) {
                paras = buildFuncRParams(unaryExpNode.getFuncRParamsNode());
            }
            Function func = ((FuncDefSymbol) SymbolCenter.getSymbol(name)).getLlvmIr();
            Call callInstr;
            if (func.getRetType() == LlvmType.Int32) {
                callInstr = new Call(func, tempName + getVarId(), paras, curBlock);
            } else {
                callInstr = new Call(func, paras, curBlock);
            }
            curBlock.addInstr(callInstr);
            return callInstr;
        } else {
            Value value = buildUnaryExp(unaryExpNode.getUnaryExpNode());
            TokenType tokenType = unaryExpNode.getUnaryOpNode().getTokenNode().getToken().getType();
            if (tokenType == TokenType.PLUS) {
                return value;
            } else if (tokenType == TokenType.MINU) {
                Alu alu = new Alu(tempName + getVarId(), new Constant(0), value, Alu.OP.SUB, curBlock);
                curBlock.addInstr(alu);
                return alu;
            } else {
                Icmp icmpInstr = new Icmp(new Constant(0), value, tempName + getVarId(), curBlock, Icmp.OP.EQ);
                Zext zextInstr = new Zext(tempName + getVarId(), icmpInstr, curBlock, LlvmType.Int32);
                curBlock.addInstr(icmpInstr);
                curBlock.addInstr(zextInstr);
                return zextInstr;
            }
        }
    }

    //LAndExp → EqExp | LAndExp '&&' EqExp
    public void buildLAndExp(LAndExpNode lAndExpNode, BasicBlock trueBlock, BasicBlock elseBlock) {
        ArrayList<EqExpNode> eqExpNodes = lAndExpNode.getEqExpNodes();
        for (int i = 0; i <= eqExpNodes.size() - 2; i++) {
            BasicBlock nextBlock = new BasicBlock(blockName + getBlockId(), curFunc);
            curFunc.addBasicBlock(nextBlock);
            buildEqExp(eqExpNodes.get(i), nextBlock, elseBlock);
            curBlock = nextBlock;
        }
        buildEqExp(eqExpNodes.get(eqExpNodes.size() - 1), trueBlock, elseBlock);
    }

    // PrimaryExp → '(' Exp ')' | LVal | Number
    public Value buildPrimaryExp(PrimaryExpNode primaryExpNode) {
        if (primaryExpNode.getExpNode() != null) {
            return buildExp(primaryExpNode.getExpNode());
        } else if (primaryExpNode.getlValNode() != null) {
            return buildLValForValue(primaryExpNode.getlValNode());
        } else {
            return new Constant(Integer.parseInt(primaryExpNode.getNumberNode().getIntConst().getToken().getValue()));
        }
    }

    //FuncRParams → Exp { ',' Exp }
    public ArrayList<Value> buildFuncRParams(FuncRParamsNode funcRParamsNode) {
        ArrayList<Value> ret = new ArrayList<>();
        for (ExpNode expNode : funcRParamsNode.getExpNodes()) {
            ret.add(buildExp(expNode));
        }
        return ret;
    }

    // EqExp → RelExp | EqExp ('==' | '!=') RelExp
    public void buildEqExp(EqExpNode eqExpNode, BasicBlock trueBlock, BasicBlock elseBlock) {
        ArrayList<RelExpNode> relExpNodes = eqExpNode.getRelExpNodes();
        ArrayList<TokenNode> tokenNodes = eqExpNode.getEqNodes();
        Value operand1 = buildRelExp(relExpNodes.get(0));
        relExpNodes.remove(0);
        if (relExpNodes.size() == 0) {
            if (operand1.getLlvmType() == LlvmType.Int32) {
                operand1 = new Icmp(new Constant(0), operand1, tempName + getVarId(), curBlock, Icmp.OP.NE);
                curBlock.addInstr((Instr) operand1);
            }
        } else {
            for (int i = 0; i <= relExpNodes.size() - 1; i++) {
                Value operand2 = buildRelExp(relExpNodes.get(i));
                if (operand1.getLlvmType() != LlvmType.Int1 || operand2.getLlvmType() != LlvmType.Int1) {
                    if (operand1.getLlvmType() == LlvmType.Int1) {
                        Zext zextInstr = new Zext(tempName + getVarId(), operand1, curBlock, LlvmType.Int32);
                        curBlock.addInstr(zextInstr);
                        operand1 = zextInstr;
                    }
                    if (operand2.getLlvmType() == LlvmType.Int1) {
                        Zext zextInstr = new Zext(tempName + getVarId(), operand2, curBlock, LlvmType.Int32);
                        curBlock.addInstr(zextInstr);
                        operand2 = zextInstr;
                    }
                }
                Icmp.OP op = tokenNodes.get(i).getToken().getType() == TokenType.EQL ? Icmp.OP.EQ : Icmp.OP.NE;
                operand1 = new Icmp(operand1, operand2, tempName + getVarId(), curBlock, op);
                curBlock.addInstr((Instr) operand1);
            }
        }
        Branch branchInstr = new Branch(operand1, trueBlock, elseBlock, curBlock);
        curBlock.addInstr(branchInstr);
    }

    //LVal → Ident {'[' Exp ']'}
    public Value buildLValForValue(LValNode lValNode) {
        String name = lValNode.getIdentNode().getToken().getValue();
        Symbol symbol = SymbolCenter.getSymbol(name);
        Value value;
        ArrayList<Integer> lens;
        boolean isConst = false;
        ArrayList<Integer> initial = null;
        if (symbol instanceof ConstSymbol) {
            isConst = true;
            value = ((ConstSymbol) symbol).getLlvmIr();
            lens = ((ConstSymbol) symbol).getDimLens();
            initial = ((ConstSymbol) symbol).getInitial();
        } else {
            value = ((VarSymbol) symbol).getLlvmIr();
            lens = ((VarSymbol) symbol).getDimLens();
        }
        ArrayList<Value> exps = new ArrayList<>();
        for (ExpNode expNode : lValNode.getExpNodes()) {
            exps.add(buildExp(expNode));
        }
        int dim = lens.size();
        int explen = lValNode.getExpNodes().size();
        if (dim == 0) {
            if (isConst) {
                value = new Constant(initial.get(0));
            } else {
                value = new Load(tempName + getVarId(), value, curBlock);
                curBlock.addInstr((Instr) value);
            }
        }
        if (dim == 1) {
            if (explen == 1) {
                GetPtr getPtrInstr = new GetPtr(tempName + getVarId(), value, exps.get(0), curBlock);
                curBlock.addInstr(getPtrInstr);
                value = new Load(tempName + getVarId(), getPtrInstr, curBlock);
                curBlock.addInstr((Instr) value);
            } else if(value instanceof GlobalVar){
                value = new GetPtr(tempName + getVarId(), value, new Constant(0), curBlock);
                curBlock.addInstr((Instr) value);
            }
        } else if (dim == 2) {
            if (explen == 2) {
                Alu mulInstr = new Alu(tempName + getVarId(), new Constant(lens.get(1)), exps.get(0), Alu.OP.MUL, curBlock);
                curBlock.addInstr(mulInstr);
                Alu addInstr = new Alu(tempName + getVarId(), mulInstr, exps.get(1), Alu.OP.ADD, curBlock);
                curBlock.addInstr(addInstr);
                GetPtr getPtr = new GetPtr(tempName + getVarId(), value, addInstr, curBlock);
                curBlock.addInstr(getPtr);
                value = new Load(tempName + getVarId(), getPtr, curBlock);
                curBlock.addInstr((Instr) value);
            } else if (explen == 1) {
                Alu mulInstr = new Alu(tempName + getVarId(), new Constant(lens.get(1)), exps.get(0), Alu.OP.MUL, curBlock);
                curBlock.addInstr(mulInstr);
                GetPtr getPtr = new GetPtr(tempName + getVarId(), value, mulInstr, curBlock);
                curBlock.addInstr(getPtr);
                value = getPtr;
            } else if(value instanceof GlobalVar){
                value = new GetPtr(tempName + getVarId(), value, new Constant(0), curBlock);
                curBlock.addInstr((Instr) value);
            }
        }
        return value;
    }

    //RelExp → AddExp | RelExp ('<' | '>' | '<=' | '>=') AddExp
    public Value buildRelExp(RelExpNode relExpNode) {
        ArrayList<AddExpNode> addExpNodes = relExpNode.getAddExpNodes();
        ArrayList<TokenNode> tokenNodes = relExpNode.getTokenNodes();
        Value operand1 = buildAddExp(addExpNodes.get(0));
        addExpNodes.remove(0);
        for (int i = 0; i <= addExpNodes.size() - 1; i++) {
            if (operand1.getLlvmType() == LlvmType.Int1) {
                Zext zextInstr = new Zext(tempName + getVarId(), operand1, curBlock, LlvmType.Int32);
                curBlock.addInstr(zextInstr);
            }
            Value operand2 = buildAddExp(addExpNodes.get(i));
            TokenType tokenType = tokenNodes.get(i).getToken().getType();
            Icmp.OP op = tokenType == TokenType.LSS ? Icmp.OP.SLT :
                    tokenType == TokenType.GRE ? Icmp.OP.SGT :
                            tokenType == TokenType.LEQ ? Icmp.OP.SLE :
                                    Icmp.OP.SGE;
            Icmp icmpInstr = new Icmp(operand1, operand2, tempName + getVarId(), curBlock, op);
            curBlock.addInstr(icmpInstr);
            operand1 = icmpInstr;
        }
        return operand1;
    }

}
