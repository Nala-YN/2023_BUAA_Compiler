package SyntaxParser;

import Symbol.ConstSymbol;
import Symbol.SymbolCenter;
import SyntaxParser.ExpNode.AddExpNode;
import SyntaxParser.ExpNode.ConstExpNode;
import SyntaxParser.ExpNode.EqExpNode;
import SyntaxParser.ExpNode.ExpNode;
import SyntaxParser.ExpNode.LAndExpNode;
import SyntaxParser.ExpNode.LOrExpNode;
import SyntaxParser.ExpNode.MulExpNode;
import SyntaxParser.ExpNode.PrimaryExpNode;
import SyntaxParser.ExpNode.RelExpNode;
import SyntaxParser.ExpNode.UnaryExpNode;
import SyntaxParser.OtherNode.BTypeNode;
import SyntaxParser.OtherNode.BlockItemNode;
import SyntaxParser.OtherNode.BlockNode;
import SyntaxParser.OtherNode.CompUnitNode;
import SyntaxParser.OtherNode.CondNode;
import SyntaxParser.OtherNode.ConstDeclNode;
import SyntaxParser.OtherNode.ConstDefNode;
import SyntaxParser.OtherNode.ConstInitValNode;
import SyntaxParser.OtherNode.DeclNode;
import SyntaxParser.OtherNode.FuncDefNode;
import SyntaxParser.OtherNode.FuncFParamNode;
import SyntaxParser.OtherNode.FuncFParamsNode;
import SyntaxParser.OtherNode.FuncRParamsNode;
import SyntaxParser.OtherNode.FuncTypeNode;
import SyntaxParser.OtherNode.InitValNode;
import SyntaxParser.OtherNode.LValNode;
import SyntaxParser.OtherNode.MainFuncDefNode;
import SyntaxParser.OtherNode.NumberNode;
import SyntaxParser.OtherNode.TokenNode;
import SyntaxParser.OtherNode.UnaryOpNode;
import SyntaxParser.OtherNode.VarDeclNode;
import SyntaxParser.OtherNode.VarDefNode;
import SyntaxParser.StmtNode.AssignStmtNode;
import SyntaxParser.StmtNode.BlockStmtNode;
import SyntaxParser.StmtNode.BreakStmtNode;
import SyntaxParser.StmtNode.ContinueStmtNode;
import SyntaxParser.StmtNode.ExpStmtNode;
import SyntaxParser.StmtNode.ForAssignStmtNode;
import SyntaxParser.StmtNode.GetintStmtNode;
import SyntaxParser.StmtNode.IfStmtNode;
import SyntaxParser.StmtNode.PrintStmtNode;
import SyntaxParser.StmtNode.ReturnStmtNode;
import SyntaxParser.StmtNode.ForStmtNode;
import SyntaxParser.StmtNode.WhileStmtNode;
import TokenParser.Token;
import TokenParser.TokenStream;
import TokenParser.TokenType;
import Util.ErrorMsg;
import Util.ErrorType;
import Util.Printer;

import java.io.IOException;
import java.util.ArrayList;

public class SyntaxParser {
    private TokenStream tokenStream;
    private Token ntoken;
    private Token pointToken;
    private boolean isGlobal=true;
    public SyntaxParser(TokenStream tokenStream) throws IOException {
        this.tokenStream = tokenStream;
    }

    private void read() {
        ntoken = tokenStream.read();
    }

    private void unread() {
        ntoken = tokenStream.unread();
    }

    private void setPoint() {
        tokenStream.setLastPos();
        pointToken = ntoken;
        Printer.setOutputOn(false);
    }

    private void rollBack() {
        tokenStream.rollBackPos();
        ntoken = pointToken;
        Printer.setOutputOn(true);
    }

    private TokenType lookType(int step) {
        if (tokenStream.look(step) == null) {
            return null;
        }
        return tokenStream.look(step).getType();
    }

    private int getLineNum(int step) {
        return tokenStream.look(step).getLineNum();
    }

    private void printToken() throws IOException {
        Printer.printToken(ntoken);
    }

    //解析器保证本部分结束时ntoken为自身部分的最后一个token
    //CompUnit → {Decl} {FuncDef} MainFuncDef
    public CompUnitNode parseCompUnitNode() throws IOException {
        int startLine = getLineNum(1);
        ArrayList<DeclNode> declNodes = new ArrayList<>();
        ArrayList<FuncDefNode> funcDefNodes = new ArrayList<>();
        MainFuncDefNode mainFuncDefNode = null;
        SymbolCenter.enterBlock();
        while (true) {
            read();
            if (ntoken == null) {
                break;
            } else if (lookType(1) == TokenType.MAINTK) {
                isGlobal=false;
                unread();
                mainFuncDefNode = parseMainFuncDefNode();
            } else if (lookType(2) == TokenType.LPARENT) {
                isGlobal=false;
                unread();
                funcDefNodes.add(parseFuncDefNode());
            } else {
                unread();
                declNodes.add(parseDeclNode());
            }
        }
        unread();
        int endLine = ntoken.getLineNum();
        SymbolCenter.leaveBlock();
        CompUnitNode compUnitNode = new CompUnitNode(startLine, endLine, declNodes, funcDefNodes, mainFuncDefNode);
        Printer.printNode(compUnitNode);
        return compUnitNode;
    }

    //MainFuncDef → 'int' 'main' '(' ')' Block
    private MainFuncDefNode parseMainFuncDefNode() throws IOException {
        read();
        int startLine = ntoken.getLineNum();
        printToken();
        read();
        printToken();
        read();
        printToken();
        read();
        printToken();
        SymbolCenter.enterFunc(TokenType.INTTK);
        BlockNode blockNode = parseBlockNode();
        SymbolCenter.leaveFunc();
        MainFuncDefNode mainFuncDefNode = new MainFuncDefNode(startLine, ntoken.getLineNum(), blockNode);
        Printer.printNode(mainFuncDefNode);
        return mainFuncDefNode;
    }

    //Block → '{' { BlockItem } '}'
    private BlockNode parseBlockNode() throws IOException {
        read();
        int startLine = ntoken.getLineNum();
        printToken();
        ArrayList<BlockItemNode> blockItemNodes = new ArrayList<>();
        boolean funcDef = SymbolCenter.isFuncDef();
        SymbolCenter.setFuncDef(false);
        if (!funcDef) {
            SymbolCenter.enterBlock();
        }
        while (true) {
            if (lookType(1) == TokenType.RBRACE) {
                read();
                printToken();
                break;
            } else {
                blockItemNodes.add(parseBlockItemNode());
            }
        }
        if (funcDef) {
            if (SymbolCenter.getFuncType() == TokenType.INTTK) {

                if (blockItemNodes.size() - 1 < 0) {
                    Printer.addErrorMsg(ErrorType.g, ntoken.getLineNum());
                } else {
                    BlockItemNode blockItemNode = blockItemNodes.get(blockItemNodes.size() - 1);
                    Node stmtNode = blockItemNode.getStmtNode();
                    if (!(stmtNode instanceof ReturnStmtNode) ||
                            ((ReturnStmtNode) stmtNode).getExpNode() == null ||
                            ((ReturnStmtNode) stmtNode).getExpNode().getDim() != 0) {
                        Printer.addErrorMsg(ErrorType.g, ntoken.getLineNum());
                    }
                }
            }
        } else {
            SymbolCenter.leaveBlock();
        }
        BlockNode blockNode = new BlockNode(startLine, ntoken.getLineNum(), blockItemNodes);
        Printer.printNode(blockNode);
        return blockNode;
    }

    //BlockItem → Decl | Stmt
    private BlockItemNode parseBlockItemNode() throws IOException {
        read();
        int startLine = ntoken.getLineNum();
        unread();
        BlockItemNode blockItemNode;
        if (lookType(1) == TokenType.CONSTTK ||
                lookType(1) == TokenType.INTTK) {
            DeclNode declNode = parseDeclNode();
            blockItemNode = new BlockItemNode(startLine, ntoken.getLineNum(), declNode, null);
        } else {
            Node stmtNode = parseStmtNode();
            blockItemNode = new BlockItemNode(startLine, ntoken.getLineNum(), null, stmtNode);
        }
        return blockItemNode;
    }

    private Node parseStmtNode() throws IOException {
        if (lookType(1) == TokenType.LBRACE) {
            return parseBlockStmtNode();
        } else if (lookType(1) == TokenType.IFTK) {
            return parseIfStmtNode();
        } else if (lookType(1) == TokenType.FORTK) {
            return parseForStmtNode();
        } else if (lookType(1) == TokenType.BREAKTK) {
            return parseBreakStmtNode();
        } else if (lookType(1) == TokenType.WHILETK) {
            return parseWhileStmtNode();
        } else if (lookType(1) == TokenType.CONTINUETK) {
            return parseContinueStmtNode();
        } else if (lookType(1) == TokenType.RETURNTK) {
            return parseReturnStmtNode();
        } else if (lookType(1) == TokenType.PRINTFTK) {
            return parsePrintStmtNode();
        } else if (lookType(1) == TokenType.SEMICN) {
            return parseExpStmtNode();
        } else {
            setPoint();
            parseExpNode();
            if (lookType(1) == TokenType.ASSIGN) {
                if (lookType(2) == TokenType.GETINTTK) {
                    rollBack();
                    return parseGetintStmtNode();
                } else {
                    rollBack();
                    return parseAssignStmtNode();
                }
            } else {
                rollBack();
                return parseExpStmtNode();
            }
           /* boolean assign = false;
            boolean getInt = false;
            int cnt = 1;
            while (lookType(cnt) != TokenType.SEMICN) {
                if (lookType(cnt) == TokenType.ASSIGN) {
                    assign = true;
                    if (lookType(cnt + 1) == TokenType.GETINTTK) {
                        getInt = true;
                    }
                    break;
                }
                cnt++;
            }
            if (assign) {
                if (getInt) {
                    return parseGetintStmtNode();
                } else {
                    return parseAssignStmtNode();
                }
            } else {
                return parseExpStmtNode();
            }*/
        }
    }

    //Stmt → LVal '=' Exp ';' // 每种类型的语句都要覆盖
    //| [Exp] ';' //有无Exp两种情况
    //| Block
    //| 'if' '(' Cond ')' Stmt [ 'else' Stmt ] // 1.有else 2.无else
    //| 'while' '(' Cond ')' Stmt
    //| 'break' ';' | 'continue' ';'
    //| 'return' [Exp] ';' // 1.有Exp 2.无Exp
    //| LVal '=' 'getint''('')'';'
    //| 'printf''('FormatString{','Exp}')'';' // 1.有Exp 2.无Exp
    private BlockStmtNode parseBlockStmtNode() throws IOException {
        read();
        int startLine = ntoken.getLineNum();
        unread();
        BlockNode blockNode = parseBlockNode();
        BlockStmtNode blockStmtNode = new BlockStmtNode(startLine, ntoken.getLineNum(), blockNode);
        Printer.printNode(blockStmtNode);
        return blockStmtNode;
    }

    //'if' '(' Cond ')' Stmt [ 'else' Stmt ]
    private IfStmtNode parseIfStmtNode() throws IOException {
        read();
        int startLine = ntoken.getLineNum();
        printToken();
        read();
        printToken();
        CondNode condNode = parseCondNode();
        if (lookType(1) == TokenType.RPARENT) {
            read();
            printToken();
        } else {
            Printer.addErrorMsg(ErrorType.j, ntoken.getLineNum());
        }
        Node stmtNode = parseStmtNode();
        Node elseNode = null;
        IfStmtNode ifStmtNode;
        if (lookType(1) == TokenType.ELSETK) {
            read();
            printToken();
            elseNode = parseStmtNode();
        }
        ifStmtNode = new IfStmtNode(startLine, ntoken.getLineNum(), condNode, stmtNode, elseNode);
        Printer.printNode(ifStmtNode);
        return ifStmtNode;
    }

    //'for' '(' [ForStmt] ';' [Cond] ';' [ForStmt] ')' Stmt
    private ForStmtNode parseForStmtNode() throws IOException {
        read();
        printToken();
        int startLine = ntoken.getLineNum();
        read();
        printToken();
        ForAssignStmtNode leftAssign = null;
        CondNode condNode = null;
        ForAssignStmtNode rightAssign = null;
        if (lookType(1) != TokenType.SEMICN) {
            leftAssign = parseForAssignStmtNode();
        }
        if (lookType(1) != TokenType.SEMICN) {
            Printer.addErrorMsg(ErrorType.i, ntoken.getLineNum());
        } else {
            read();
            printToken();
        }
        if (lookType(1) != TokenType.SEMICN) {
            condNode = parseCondNode();
        }
        if (lookType(1) != TokenType.SEMICN) {
            Printer.addErrorMsg(ErrorType.i, ntoken.getLineNum());
        } else {
            read();
            printToken();
        }
        if (lookType(1) == TokenType.IDENFR) {
            rightAssign = parseForAssignStmtNode();
        }
        if (lookType(1) == TokenType.RPARENT) {
            read();
            printToken();
        } else {
            Printer.addErrorMsg(ErrorType.j, ntoken.getLineNum());
        }
        SymbolCenter.enterLoop();
        Node stmtNode = parseStmtNode();
        SymbolCenter.leaveLoop();
        ForStmtNode forStmtNode = new ForStmtNode(startLine, ntoken.getLineNum(), leftAssign, condNode, rightAssign, stmtNode);
        Printer.printNode(forStmtNode);
        return forStmtNode;
    }

    //'while' '(' Cond ')' Stmt
    private WhileStmtNode parseWhileStmtNode() throws IOException {
        read();
        int startLine = ntoken.getLineNum();
        printToken();
        read();
        printToken();
        CondNode condNode = parseCondNode();
        if(lookType(1)==TokenType.RPARENT){
            read();
            printToken();
        }
        else{
            Printer.addErrorMsg(ErrorType.j,ntoken.getLineNum());
        }
        SymbolCenter.enterLoop();
        Node stmtNode = parseStmtNode();
        SymbolCenter.leaveLoop();
        WhileStmtNode whileStmtNode = new WhileStmtNode(startLine, ntoken.getLineNum(), condNode, stmtNode);
        Printer.printNode(whileStmtNode);
        return whileStmtNode;
    }

    //'break' ';'
    private BreakStmtNode parseBreakStmtNode() throws IOException {
        read();
        int startLine = ntoken.getLineNum();
        printToken();
        if (lookType(1) != TokenType.SEMICN) {
            Printer.addErrorMsg(ErrorType.i, ntoken.getLineNum());
        } else {
            read();
            printToken();
        }
        if (SymbolCenter.getLoopDepth() == 0) {
            Printer.addErrorMsg(ErrorType.m, startLine);
        }
        BreakStmtNode breakStmtNode = new BreakStmtNode(startLine, ntoken.getLineNum());
        Printer.printNode(breakStmtNode);
        return breakStmtNode;
    }

    //'continue' ';'
    private ContinueStmtNode parseContinueStmtNode() throws IOException {
        read();
        int startLine = ntoken.getLineNum();
        printToken();
        if (lookType(1) != TokenType.SEMICN) {
            Printer.addErrorMsg(ErrorType.i, ntoken.getLineNum());
        } else {
            read();
            printToken();
        }
        ContinueStmtNode continueStmtNode = new ContinueStmtNode(startLine, ntoken.getLineNum());
        if (SymbolCenter.getLoopDepth() == 0) {
            Printer.addErrorMsg(ErrorType.m, startLine);
        }
        Printer.printNode(continueStmtNode);
        return continueStmtNode;
    }

    //'return' [Exp] ';'
    private ReturnStmtNode parseReturnStmtNode() throws IOException {
        read();
        int startLine = ntoken.getLineNum();
        printToken();
        ReturnStmtNode returnStmtNode;
        ExpNode expNode = null;
        if (lookType(1) == TokenType.PLUS || lookType(1) == TokenType.MINU ||
                lookType(1) == TokenType.NOT || lookType(1) == TokenType.IDENFR ||
                lookType(1) == TokenType.LPARENT || lookType(1) == TokenType.INTCON) {
            expNode = parseExpNode();
        }
        if (lookType(1) != TokenType.SEMICN) {
            Printer.addErrorMsg(ErrorType.i, ntoken.getLineNum());
        } else {
            read();
            printToken();
        }
        returnStmtNode = new ReturnStmtNode(startLine, ntoken.getLineNum(), expNode);
        returnStmtNode.checkError();
        Printer.printNode(returnStmtNode);
        return returnStmtNode;
    }

    //'printf''('FormatString{','Exp}')'';' // 1.有Exp 2.无Exp
    private PrintStmtNode parsePrintStmtNode() throws IOException {
        int dcnt = 0;
        int expcnt = 0;
        read();
        int startLine = ntoken.getLineNum();
        printToken();
        read();
        printToken();
        read();
        printToken();
        TokenNode tokenNode = new TokenNode(ntoken.getLineNum(), ntoken.getLineNum(), ntoken);
        String fmtStr = ntoken.getValue();
        for (int i = 1; i <= fmtStr.length() - 2; i++) {
            char ch = fmtStr.charAt(i);
            if (ch == '\\') {
                ch = fmtStr.charAt(i + 1);
                if (ch != 'n') {
                    Printer.addErrorMsg(ErrorType.a, ntoken.getLineNum());
                    break;
                }
                i++;
            } else if (ch == '%') {
                ch = fmtStr.charAt(i + 1);
                if (ch != 'd') {
                    Printer.addErrorMsg(ErrorType.a, ntoken.getLineNum());
                    break;
                }
                dcnt++;
                i++;
            } else if (!(ch == 32 || ch == 33 || ch >= 40 && ch <= 126)) {
                Printer.addErrorMsg(ErrorType.a, ntoken.getLineNum());
            }
        }
        ArrayList<ExpNode> expNodes = new ArrayList<>();
        while (true) {
            if (lookType(1) == TokenType.COMMA) {
                read();
                printToken();
                expNodes.add(parseExpNode());
                expcnt++;
            } else {
                break;
            }
        }
        if (dcnt != expcnt) {
            Printer.addErrorMsg(ErrorType.l, startLine);
        }
        if (lookType(1) != TokenType.RPARENT) {
            Printer.addErrorMsg(ErrorType.j, ntoken.getLineNum());
        } else {
            read();
            printToken();
        }
        if (lookType(1) != TokenType.SEMICN) {
            Printer.addErrorMsg(ErrorType.i, ntoken.getLineNum());
        } else {
            read();
            printToken();
        }
        PrintStmtNode printStmtNode = new PrintStmtNode(startLine, ntoken.getLineNum(), tokenNode, expNodes);
        Printer.printNode(printStmtNode);
        return printStmtNode;
    }

    //LVal '=' 'getint''('')'';'
    private GetintStmtNode parseGetintStmtNode() throws IOException {
        read();
        int startLine = ntoken.getLineNum();
        unread();
        LValNode lValNode = parseLValNode();
        if (SymbolCenter.getSymbol(lValNode.getIdentNode().getToken().getValue()) instanceof ConstSymbol) {
            Printer.addErrorMsg(ErrorType.h, lValNode.getStartLine());
        }
        for (int i = 1; i <= 3; i++) {
            read();
            printToken();
        }
        if (lookType(1) == TokenType.RPARENT) {
            read();
            printToken();
        } else {
            Printer.addErrorMsg(ErrorType.j, ntoken.getLineNum());
        }
        if (lookType(1) != TokenType.SEMICN) {
            Printer.addErrorMsg(ErrorType.i, ntoken.getLineNum());
        } else {
            read();
            printToken();
        }
        GetintStmtNode getintStmtNode = new GetintStmtNode(startLine, ntoken.getLineNum(), lValNode);
        Printer.printNode(getintStmtNode);
        return getintStmtNode;
    }

    //LVal '=' Exp ';'
    private AssignStmtNode parseAssignStmtNode() throws IOException {
        read();
        int startLine = ntoken.getLineNum();
        unread();
        LValNode lValNode = parseLValNode();
        if (SymbolCenter.getSymbol(lValNode.getIdentNode().getToken().getValue()) instanceof ConstSymbol) {
            Printer.addErrorMsg(ErrorType.h, lValNode.getStartLine());
        }
        read();
        printToken();
        ExpNode expNode = parseExpNode();
        if (lookType(1) != TokenType.SEMICN) {
            Printer.addErrorMsg(ErrorType.i, ntoken.getLineNum());
        } else {
            read();
            printToken();
        }
        AssignStmtNode assignStmtNode = new AssignStmtNode(startLine, ntoken.getLineNum(), lValNode, expNode);
        Printer.printNode(assignStmtNode);
        return assignStmtNode;
    }

    //ForStmt → LVal '=' Exp
    private ForAssignStmtNode parseForAssignStmtNode() throws IOException {
        read();
        int startLine = ntoken.getLineNum();
        unread();
        LValNode lValNode = parseLValNode();
        read();
        printToken();
        ExpNode expNode = parseExpNode();
        ForAssignStmtNode forAssignStmtNode = new ForAssignStmtNode(startLine, ntoken.getLineNum(), lValNode, expNode);
        Printer.printNode(forAssignStmtNode);
        return forAssignStmtNode;
    }

    //[Exp] ';'
    private ExpStmtNode parseExpStmtNode() throws IOException {
        read();
        int startLine = ntoken.getLineNum();
        unread();
        ExpNode expNode = null;
        if (lookType(1) != TokenType.SEMICN) {
            expNode = parseExpNode();
        }
        if (lookType(1) != TokenType.SEMICN) {
            Printer.addErrorMsg(ErrorType.i, ntoken.getLineNum());
        } else {
            read();
            printToken();
        }
        ExpStmtNode expStmtNode = new ExpStmtNode(startLine, ntoken.getLineNum(), expNode);
        Printer.printNode(expStmtNode);
        return expStmtNode;
    }

    //Cond → LOrExp
    private CondNode parseCondNode() throws IOException {
        read();
        int startLine = ntoken.getLineNum();
        unread();
        LOrExpNode lOrExpNode = parseLOrExpNode();
        CondNode condNode = new CondNode(startLine, ntoken.getLineNum(), lOrExpNode);
        Printer.printNode(condNode);
        return condNode;
    }

    //LOrExp → LAndExp | LOrExp '||' LAndExp
    private LOrExpNode parseLOrExpNode() throws IOException {
        read();
        int startLine = ntoken.getLineNum();
        unread();
        ArrayList<LAndExpNode> lAndExpNodes = new ArrayList<>();
        lAndExpNodes.add(parseLAndExpNode());
        Printer.printNode(new LOrExpNode(0, 0, null));
        while (true) {
            if (lookType(1) == TokenType.OR) {
                read();
                printToken();
                lAndExpNodes.add(parseLAndExpNode());
                Printer.printNode(new LOrExpNode(0, 0, null));
            } else {
                break;
            }
        }
        return new LOrExpNode(startLine, ntoken.getLineNum(), lAndExpNodes);
    }

    //LAndExp → EqExp | LAndExp '&&' EqExp
    private LAndExpNode parseLAndExpNode() throws IOException {
        read();
        int startLine = ntoken.getLineNum();
        unread();
        ArrayList<EqExpNode> eqExpNodes = new ArrayList<>();
        eqExpNodes.add(parseEqExpNode());
        Printer.printNode(new LAndExpNode(0, 0, null));
        while (true) {
            if (lookType(1) == TokenType.AND) {
                read();
                printToken();
                eqExpNodes.add(parseEqExpNode());
                Printer.printNode(new LAndExpNode(0, 0, null));
            } else {
                break;
            }
        }
        return new LAndExpNode(startLine, ntoken.getLineNum(), eqExpNodes);
    }

    //EqExp → RelExp | EqExp ('==' | '!=') RelExp
    private EqExpNode parseEqExpNode() throws IOException {
        read();
        int startLine = ntoken.getLineNum();
        unread();
        ArrayList<RelExpNode> relExpNodes = new ArrayList<>();
        ArrayList<TokenNode> tokenNodes = new ArrayList<>();
        relExpNodes.add(parseRelExpNode());
        Printer.printNode(new EqExpNode(0, 0, null, null));
        while (true) {
            if (lookType(1) == TokenType.EQL
                    || lookType(1) == TokenType.NEQ) {
                read();
                tokenNodes.add(new TokenNode(ntoken.getLineNum(), ntoken.getLineNum(), ntoken));
                printToken();
                relExpNodes.add(parseRelExpNode());
                Printer.printNode(new EqExpNode(0, 0, null, null));
            } else {
                break;
            }
        }
        return new EqExpNode(startLine, ntoken.getLineNum(), relExpNodes, tokenNodes);
    }

    //RelExp → AddExp | RelExp ('<' | '>' | '<=' | '>=') AddExp
    private RelExpNode parseRelExpNode() throws IOException {
        read();
        int startLine = ntoken.getLineNum();
        unread();
        ArrayList<AddExpNode> addExpNodes = new ArrayList<>();
        ArrayList<TokenNode> tokenNodes = new ArrayList<>();
        addExpNodes.add(parseAddExpNode());
        Printer.printNode(new RelExpNode(0, 0, null, null));
        while (true) {
            if (lookType(1) == TokenType.GEQ
                    || lookType(1) == TokenType.LSS
                    || lookType(1) == TokenType.GRE
                    || lookType(1) == TokenType.LEQ) {
                read();
                tokenNodes.add(new TokenNode(ntoken.getLineNum(), ntoken.getLineNum(), ntoken));
                printToken();
                addExpNodes.add(parseAddExpNode());
                Printer.printNode(new RelExpNode(0, 0, null, null));
            } else {
                break;
            }
        }
        return new RelExpNode(startLine, ntoken.getLineNum(), addExpNodes, tokenNodes);
    }

    //FuncDef → FuncType Ident '(' [FuncFParams] ')' Block
    private FuncDefNode parseFuncDefNode() throws IOException {
        read();
        int startLine = ntoken.getLineNum();
        unread();
        FuncTypeNode funcTypeNode = parseFuncTypeNode();
        read();
        TokenNode identNode = new TokenNode(ntoken.getLineNum(), ntoken.getLineNum(), ntoken);
        printToken();
        read();
        printToken();
        FuncDefNode funcDefNode = new FuncDefNode(startLine, ntoken.getLineNum(),
                funcTypeNode, identNode);
        funcDefNode.checkError();
        SymbolCenter.enterFunc(funcTypeNode.getVoidIntNode().getToken().getType());
        FuncFParamsNode funcFParamsNode = null;
        if (lookType(1) == TokenType.INTTK) {
            funcFParamsNode = parseFuncFParamsNode();
        }
        if (lookType(1) == TokenType.RPARENT) {
            read();
            printToken();
        } else {
            Printer.addErrorMsg(ErrorType.j, ntoken.getLineNum());
        }
        funcDefNode.setFuncFParamsNode(funcFParamsNode);
        BlockNode blockNode = parseBlockNode();
        funcDefNode.setBlockNode(blockNode);
        SymbolCenter.leaveFunc();
        Printer.printNode(funcDefNode);
        return funcDefNode;
    }

    //FuncType → 'void' | 'int'
    private FuncTypeNode parseFuncTypeNode() throws IOException {
        read();
        printToken();
        FuncTypeNode funcTypeNode = new FuncTypeNode(ntoken.getLineNum(), ntoken.getLineNum(),
                new TokenNode(ntoken.getLineNum(), ntoken.getLineNum(), ntoken));
        Printer.printNode(funcTypeNode);
        return funcTypeNode;
    }

    //FuncFParams → FuncFParam { ',' FuncFParam }
    private FuncFParamsNode parseFuncFParamsNode() throws IOException {
        read();
        int startLine = ntoken.getLineNum();
        unread();
        ArrayList<FuncFParamNode> funcFParamNodes = new ArrayList<>();
        funcFParamNodes.add(parseFuncFParamNode());
        while (true) {
            if (lookType(1) == TokenType.COMMA) {
                read();
                printToken();
                funcFParamNodes.add(parseFuncFParamNode());
            } else {
                break;
            }
        }
        FuncFParamsNode funcFParamsNode = new FuncFParamsNode(startLine, ntoken.getLineNum(), funcFParamNodes);
        Printer.printNode(funcFParamsNode);
        return funcFParamsNode;
    }

    //FuncFParam → BType Ident ['[' ']' { '[' ConstExp ']' }]
    private FuncFParamNode parseFuncFParamNode() throws IOException {
        read();
        BTypeNode bTypeNode = new BTypeNode(ntoken.getLineNum(), ntoken.getLineNum());
        printToken();
        int startLine = ntoken.getLineNum();
        read();
        printToken();
        TokenNode identToken = new TokenNode(ntoken.getLineNum(), ntoken.getLineNum(), ntoken);
        ArrayList<ConstExpNode> constExpNodes = new ArrayList<>();
        if (lookType(1) == TokenType.LBRACK) {
            constExpNodes.add(null);
            read();
            printToken();
            if (lookType(1) == TokenType.RBRACK) {
                read();
                printToken();
            } else {
                Printer.addErrorMsg(ErrorType.k, ntoken.getLineNum());
            }
        }
        while (true) {
            if (lookType(1) == TokenType.LBRACK) {
                read();
                printToken();
                constExpNodes.add(parseConstExpNode());
                if (lookType(1) == TokenType.RBRACK) {
                    read();
                    printToken();
                } else {
                    Printer.addErrorMsg(ErrorType.k, ntoken.getLineNum());
                }
            } else {
                break;
            }
        }
        FuncFParamNode funcFParamNode = new FuncFParamNode(startLine, ntoken.getLineNum(), bTypeNode, identToken, constExpNodes);
        funcFParamNode.checkError();
        Printer.printNode(funcFParamNode);
        return funcFParamNode;
    }

    //Decl → ConstDecl | VarDecl
    private DeclNode parseDeclNode() throws IOException {
        read();
        ConstDeclNode constDeclNode;
        VarDeclNode varDeclNode;
        DeclNode declNode;
        int startLine = ntoken.getLineNum();
        if (ntoken.getType() == TokenType.CONSTTK) {
            unread();
            constDeclNode = parseConstDeclNode();
            declNode = new DeclNode(startLine, ntoken.getLineNum(), constDeclNode, null);
        } else {
            unread();
            varDeclNode = parseVarDeclNode();
            declNode = new DeclNode(startLine, ntoken.getLineNum(), null, varDeclNode);
        }
        return declNode;
    }

    //ConstDecl → 'const' BType ConstDef { ',' ConstDef } ';'
    private ConstDeclNode parseConstDeclNode() throws IOException {
        read();
        printToken();
        int startLine = ntoken.getLineNum();
        read();
        printToken();
        ArrayList<ConstDefNode> constDefNodes = new ArrayList<>();
        constDefNodes.add(parseConstDefNode());
        while (true) {
            if (lookType(1) == TokenType.COMMA) {
                read();
                printToken();
                constDefNodes.add(parseConstDefNode());
            } else {
                break;
            }
        }
        if (lookType(1) != TokenType.SEMICN) {
            Printer.addErrorMsg(ErrorType.i, ntoken.getLineNum());
        } else {
            read();
            printToken();
        }
        ConstDeclNode constDeclNode = new ConstDeclNode(startLine, ntoken.getLineNum(), constDefNodes);
        Printer.printNode(constDeclNode);
        return constDeclNode;
    }

    //VarDecl → BType VarDef { ',' VarDef } ';'
    private VarDeclNode parseVarDeclNode() throws IOException {
        read();
        printToken();
        int startLine = ntoken.getLineNum();
        ArrayList<VarDefNode> varDefNodes = new ArrayList<>();
        varDefNodes.add(parseVarDefNode());
        while (true) {
            if (lookType(1) == TokenType.COMMA) {
                read();
                printToken();
                varDefNodes.add(parseVarDefNode());
            } else {
                break;
            }
        }
        if (lookType(1) != TokenType.SEMICN) {
            Printer.addErrorMsg(ErrorType.i, ntoken.getLineNum());
        } else {
            read();
            printToken();
        }
        VarDeclNode varDeclNode = new VarDeclNode(startLine, ntoken.getLineNum(), varDefNodes);
        Printer.printNode(varDeclNode);
        return varDeclNode;
    }

    //ConstDef → Ident { '[' ConstExp ']' } '=' ConstInitVal
    private ConstDefNode parseConstDefNode() throws IOException {
        read();
        printToken();
        int startLine = ntoken.getLineNum();
        TokenNode identNode = new TokenNode(ntoken.getLineNum(), ntoken.getLineNum(), ntoken);
        ArrayList<ConstExpNode> constExpNodes = new ArrayList<>();
        while (true) {
            read();
            printToken();
            if (ntoken.getType() == TokenType.LBRACK) {
                constExpNodes.add(parseConstExpNode());
                if (lookType(1) == TokenType.RBRACK) {
                    read();
                    printToken();
                } else {
                    Printer.addErrorMsg(ErrorType.k, ntoken.getLineNum());
                }
            } else {
                break;
            }
        }
        ConstInitValNode constInitValNode = parseConstInitValNode();
        ConstDefNode constDefNode = new ConstDefNode(startLine, ntoken.getLineNum(), identNode, constExpNodes, constInitValNode);
        constDefNode.checkError();
        Printer.printNode(constDefNode);
        return constDefNode;
    }

    //VarDef → Ident { '[' ConstExp ']' } // 包含普通变量、一维数组、二维数组定义
    //| Ident { '[' ConstExp ']' } '=' InitVal
    private VarDefNode parseVarDefNode() throws IOException {
        read();
        int startLine = ntoken.getLineNum();
        //System.out.println(startLine);
        printToken();
        TokenNode identNode = new TokenNode(startLine, startLine, ntoken);
        ArrayList<ConstExpNode> constExpNodes = new ArrayList<>();
        InitValNode initValNode = null;
        while (true) {
            read();
            if (ntoken.getType() == TokenType.LBRACK) {
                printToken();
                constExpNodes.add(parseConstExpNode());
                if (lookType(1) == TokenType.RBRACK) {
                    read();
                    printToken();
                } else {
                    Printer.addErrorMsg(ErrorType.k, ntoken.getLineNum());
                }
            } else {
                unread();
                break;
            }
        }
        if (lookType(1) == TokenType.ASSIGN) {
            read();
            printToken();
            initValNode = parseInitValNode();
        }
        VarDefNode varDefNode = new VarDefNode(startLine, ntoken.getLineNum(), identNode, constExpNodes, initValNode);
        varDefNode.checkError(isGlobal);
        Printer.printNode(varDefNode);
        return varDefNode;
    }

    //ConstExp → AddExp
    private ConstExpNode parseConstExpNode() throws IOException {
        int startLine = ntoken.getLineNum();
        AddExpNode addExpNode = parseAddExpNode();
        ConstExpNode constExpNode = new ConstExpNode(startLine, ntoken.getLineNum(), addExpNode);
        Printer.printNode(constExpNode);
        return constExpNode;
    }

    //ConstInitVal → ConstExp
    //| '{' [ ConstInitVal { ',' ConstInitVal } ] '}'
    private ConstInitValNode parseConstInitValNode() throws IOException {
        int startLine;
        ConstInitValNode constInitValNode;
        ArrayList<ConstInitValNode> constInitValNodes = new ArrayList<>();
        read();
        startLine = ntoken.getLineNum();
        unread();
        if (lookType(1) == TokenType.LBRACE) {
            read();
            printToken();
            if (lookType(1) == TokenType.RBRACE) {
                read();
                printToken();
                constInitValNode = new ConstInitValNode(startLine, ntoken.getLineNum(), null, constInitValNodes);
            } else {
                constInitValNodes.add(parseConstInitValNode());
                while (true) {
                    read();
                    printToken();
                    if (ntoken.getType() == TokenType.COMMA) {
                        constInitValNodes.add(parseConstInitValNode());
                    } else {
                        break;
                    }
                }
                constInitValNode = new ConstInitValNode(startLine, ntoken.getLineNum(), null, constInitValNodes);
            }
        } else {
            ConstExpNode constExpNode = parseConstExpNode();
            constInitValNode = new ConstInitValNode(startLine, ntoken.getLineNum(), constExpNode, null);
        }
        Printer.printNode(constInitValNode);
        return constInitValNode;
    }

    //InitVal → Exp | '{' [ InitVal { ',' InitVal } ] '}'// 1.表达式初值 2.一
    //维数组初值 3.二维数组初值
    private InitValNode parseInitValNode() throws IOException {
        int startLine;
        InitValNode initValNode;
        ArrayList<InitValNode> initValNodes = new ArrayList<>();
        read();
        startLine = ntoken.getLineNum();
        unread();
        if (lookType(1) == TokenType.LBRACE) {
            read();
            printToken();
            if (lookType(1) == TokenType.RBRACE) {
                read();
                printToken();
                initValNode = new InitValNode(startLine, ntoken.getLineNum(), null, initValNodes);
            } else {
                initValNodes.add(parseInitValNode());
                while (true) {
                    read();
                    printToken();
                    if (ntoken.getType() == TokenType.COMMA) {
                        initValNodes.add(parseInitValNode());
                    } else {
                        break;
                    }
                }
                initValNode = new InitValNode(startLine, ntoken.getLineNum(), null, initValNodes);
            }
        } else {
            ExpNode expNode = parseExpNode();
            initValNode = new InitValNode(startLine, ntoken.getLineNum(), expNode, null);
        }
        Printer.printNode(initValNode);
        return initValNode;
    }

    //AddExp → MulExp | AddExp ('+' | '−') MulExp // 1.MulExp 2.+ 需覆盖 3.-
    //需覆盖
    private AddExpNode parseAddExpNode() throws IOException {
        ArrayList<MulExpNode> mulExpNodes = new ArrayList<>();
        ArrayList<TokenNode> tokenNodes = new ArrayList<>();
        read();
        int startLine = ntoken.getLineNum();
        unread();
        mulExpNodes.add(parseMulExpNode());
        Printer.printNode(new AddExpNode(0, 0, null, null));
        while (true) {
            read();
            if (ntoken.getType() == TokenType.PLUS ||
                    ntoken.getType() == TokenType.MINU) {
                printToken();
                tokenNodes.add(new TokenNode(ntoken.getLineNum(), ntoken.getLineNum(), ntoken));
                mulExpNodes.add(parseMulExpNode());
                Printer.printNode(new AddExpNode(0, 0, null, null));
            } else {
                unread();
                break;
            }
        }
        return new AddExpNode(startLine, ntoken.getLineNum(), tokenNodes, mulExpNodes);
    }

    //Exp → AddExp
    private ExpNode parseExpNode() throws IOException {
        read();
        int startLine = ntoken.getLineNum();
        unread();
        AddExpNode addExpNode = parseAddExpNode();
        ExpNode expNode = new ExpNode(startLine, ntoken.getLineNum(), addExpNode);
        Printer.printNode(expNode);
        return expNode;
    }

    //MulExp → UnaryExp | MulExp ('*' | '/' | '%') UnaryExp
    private MulExpNode parseMulExpNode() throws IOException {
        read();
        int startLine = ntoken.getLineNum();
        unread();
        ArrayList<TokenNode> tokenNodes = new ArrayList<>();
        ArrayList<UnaryExpNode> unaryExpNodes = new ArrayList<>();
        unaryExpNodes.add(parseUnaryExpNode());
        Printer.printNode(new MulExpNode(0, 0, null, null));
        while (true) {
            if (lookType(1) == TokenType.MULT ||
                    lookType(1) == TokenType.DIV ||
                    lookType(1) == TokenType.MOD) {
                read();
                printToken();
                tokenNodes.add(new TokenNode(ntoken.getLineNum(), ntoken.getLineNum(), ntoken));
                unaryExpNodes.add(parseUnaryExpNode());
                Printer.printNode(new MulExpNode(0, 0, null, null));
            } else {
                break;
            }
        }
        return new MulExpNode(startLine, ntoken.getLineNum(), tokenNodes, unaryExpNodes);
    }

    //UnaryExp → PrimaryExp | Ident '(' [FuncRParams] ')' // 3种情况均需覆盖,
    //函数调用也需要覆盖FuncRParams的不同情况
    //| UnaryOp UnaryExp // 存在即可
    private UnaryExpNode parseUnaryExpNode() throws IOException {
        UnaryExpNode unaryExpNode;
        read();
        int startLine = ntoken.getLineNum();
        if (ntoken.getType() == TokenType.IDENFR && lookType(1) == TokenType.LPARENT) {
            printToken();
            TokenNode tokenNode = new TokenNode(ntoken.getLineNum(), ntoken.getLineNum(), ntoken);
            read();
            printToken();
            FuncRParamsNode funcRParamsNode = null;
            if (lookType(1) == TokenType.PLUS || lookType(1) == TokenType.MINU ||
                    lookType(1) == TokenType.NOT || lookType(1) == TokenType.IDENFR ||
                    lookType(1) == TokenType.LPARENT || lookType(1) == TokenType.INTCON) {
                funcRParamsNode = parseFuncRParamsNode();
            }
            if (lookType(1) == TokenType.RPARENT) {
                read();
                printToken();
            } else {
                Printer.addErrorMsg(ErrorType.j, ntoken.getLineNum());
            }
            unaryExpNode = new UnaryExpNode(startLine, ntoken.getLineNum(), null,
                    tokenNode, funcRParamsNode, null, null);
        } else if (ntoken.getType() == TokenType.PLUS ||
                ntoken.getType() == TokenType.MINU ||
                ntoken.getType() == TokenType.NOT) {
            unread();
            UnaryOpNode unaryOpNode = parseUnaryOpNode();
            UnaryExpNode unaryExpNode1 = parseUnaryExpNode();
            unaryExpNode = new UnaryExpNode(startLine, ntoken.getLineNum(), null,
                    null, null, unaryOpNode, unaryExpNode1);
        } else {
            unread();
            PrimaryExpNode primaryExpNode = parsePrimaryExpNode();
            unaryExpNode = new UnaryExpNode(startLine, ntoken.getLineNum(), primaryExpNode,
                    null, null, null, null);
        }
        unaryExpNode.checkError();
        Printer.printNode(unaryExpNode);
        return unaryExpNode;
    }

    //FuncRParams → Exp { ',' Exp } // 1.花括号内重复0次 2.花括号内重复多次 3.
    //Exp需要覆盖数组传参和部分数组传参
    private FuncRParamsNode parseFuncRParamsNode() throws IOException {
        read();
        int startLine = ntoken.getLineNum();
        unread();
        ArrayList<ExpNode> expNodes = new ArrayList<>();
        expNodes.add(parseExpNode());
        while (true) {
            read();
            if (ntoken.getType() == TokenType.COMMA) {
                printToken();
                expNodes.add(parseExpNode());
            } else {
                unread();
                break;
            }
        }
        FuncRParamsNode funcRParamsNode = new FuncRParamsNode(startLine, ntoken.getLineNum(), expNodes);
        Printer.printNode(funcRParamsNode);
        return funcRParamsNode;
    }

    //UnaryOp → '+' | '−' | '!'
    private UnaryOpNode parseUnaryOpNode() throws IOException {
        read();
        printToken();
        TokenNode tokenNode = new TokenNode(ntoken.getLineNum(), ntoken.getLineNum(), ntoken);
        UnaryOpNode unaryOpNode = new UnaryOpNode(ntoken.getLineNum(), ntoken.getLineNum(), tokenNode);
        Printer.printNode(unaryOpNode);
        return unaryOpNode;
    }

    //PrimaryExp → '(' Exp ')' | LVal | Number // 三种情况均需覆盖
    private PrimaryExpNode parsePrimaryExpNode() throws IOException {
        PrimaryExpNode primaryExpNode;
        read();
        int startLine = ntoken.getLineNum();
        unread();
        if (lookType(1) == TokenType.LPARENT) {
            read();
            printToken();
            ExpNode expNode = parseExpNode();
            read();
            printToken();
            primaryExpNode = new PrimaryExpNode(startLine, ntoken.getLineNum(), expNode, null, null);
        } else if (lookType(1) == TokenType.INTCON) {
            NumberNode numberNode = parseNumberNode();
            primaryExpNode = new PrimaryExpNode(startLine, ntoken.getLineNum(), null, null, numberNode);
        } else {
            LValNode lValNode = parseLValNode();
            primaryExpNode = new PrimaryExpNode(startLine, ntoken.getLineNum(), null, lValNode, null);
        }
        Printer.printNode(primaryExpNode);
        return primaryExpNode;
    }

    //Number → IntConst
    private NumberNode parseNumberNode() throws IOException {
        read();
        printToken();
        TokenNode tokenNode = new TokenNode(ntoken.getLineNum(), ntoken.getLineNum(), ntoken);
        NumberNode numberNode = new NumberNode(ntoken.getLineNum(), ntoken.getLineNum(), tokenNode);
        Printer.printNode(numberNode);
        return numberNode;
    }

    //LVal → Ident {'[' Exp ']'} //1.普通变量 2.一维数组 3.二维数组
    private LValNode parseLValNode() throws IOException {
        read();
        int startLine = ntoken.getLineNum();
        printToken();
        TokenNode identNode = new TokenNode(ntoken.getLineNum(), ntoken.getLineNum(), ntoken);
        ArrayList<ExpNode> expNodes = new ArrayList<>();
        while (true) {
            if (lookType(1) == TokenType.LBRACK) {
                read();
                printToken();
                expNodes.add(parseExpNode());
                if (lookType(1) != TokenType.RBRACK) {
                    Printer.addErrorMsg(ErrorType.k, ntoken.getLineNum());
                } else {
                    read();
                    printToken();
                }

            } else {
                break;
            }
        }
        LValNode lValNode = new LValNode(startLine, ntoken.getLineNum(), identNode, expNodes);
        lValNode.checkError();
        Printer.printNode(lValNode);
        return lValNode;
    }
}
