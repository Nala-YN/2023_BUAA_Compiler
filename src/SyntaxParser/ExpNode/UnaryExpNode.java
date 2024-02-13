package SyntaxParser.ExpNode;

import Symbol.FuncDefSymbol;
import Symbol.Symbol;
import Symbol.SymbolCenter;
import SyntaxParser.Node;
import SyntaxParser.OtherNode.FuncDefNode;
import SyntaxParser.OtherNode.FuncRParamsNode;
import SyntaxParser.OtherNode.TokenNode;
import SyntaxParser.OtherNode.UnaryOpNode;
import TokenParser.TokenType;
import Util.ErrorType;
import Util.Printer;

import java.util.ArrayList;

public class UnaryExpNode extends Node {
    private PrimaryExpNode primaryExpNode;
    private TokenNode identNode;
    private FuncRParamsNode funcRParamsNode;
    private UnaryOpNode unaryOpNode;
    private UnaryExpNode unaryExpNode;
    public UnaryExpNode(int startLine,int endLine,PrimaryExpNode primaryExpNode,
                        TokenNode identNode,FuncRParamsNode funcRParamsNode,
                        UnaryOpNode unaryOpNode,UnaryExpNode unaryExpNode){
        super(startLine,endLine);
        this.primaryExpNode=primaryExpNode;
        this.identNode=identNode;
        this.funcRParamsNode=funcRParamsNode;
        this.unaryOpNode=unaryOpNode;
        this.unaryExpNode=unaryExpNode;
    }
    public int getDim(){
        if(primaryExpNode!=null){
            return primaryExpNode.getDim();
        }
        else if(identNode!=null){
            return ((FuncDefSymbol)SymbolCenter.getSymbol(identNode.getToken().getValue())).getDefineType()==TokenType.VOIDTK?-1:0;
        }
        else{
            return unaryExpNode.getDim();
        }
    }
    public int compute(){
        if(primaryExpNode!=null){
            return primaryExpNode.compute();
        }
        else{
            if(unaryOpNode.getTokenNode().getToken().getType()== TokenType.PLUS){
                return unaryExpNode.compute();
            }
            else{
                return -unaryExpNode.compute();
            }
        }
    }
    public void checkError(){
        if(identNode!=null){
            Symbol symbol=SymbolCenter.getSymbol(identNode.getToken().getValue());
            if(!SymbolCenter.findSymbol(identNode.getToken().getValue())||!(symbol instanceof FuncDefSymbol)){
                Printer.addErrorMsg(ErrorType.c,identNode.getStartLine());
            }
            else{
                FuncDefSymbol funcDefSymbol=(FuncDefSymbol) symbol;
                ArrayList<ExpNode> funcRParams=funcRParamsNode==null?new ArrayList<>():funcRParamsNode.getExpNodes();
                if(funcDefSymbol.getParaLen()!=funcRParams.size()){
                    Printer.addErrorMsg(ErrorType.d,identNode.getStartLine());
                    return;
                }
                for(int i=0;i<=funcDefSymbol.getParaLen()-1;i++){
                    if(funcDefSymbol.getDims().get(i)!=funcRParams.get(i).getDim()){
                        //System.out.println(funcDefSymbol.getDims().get(i)+" "+funcRParams.get(i).getDim());
                        Printer.addErrorMsg(ErrorType.e,identNode.getStartLine());
                        break;
                    }
                }
            }
        }
    }

    public PrimaryExpNode getPrimaryExpNode() {
        return primaryExpNode;
    }

    public TokenNode getIdentNode() {
        return identNode;
    }

    public FuncRParamsNode getFuncRParamsNode() {
        return funcRParamsNode;
    }

    public UnaryOpNode getUnaryOpNode() {
        return unaryOpNode;
    }

    public UnaryExpNode getUnaryExpNode() {
        return unaryExpNode;
    }

    public String toString(){
        return "<UnaryExp>\n";
    }
}
