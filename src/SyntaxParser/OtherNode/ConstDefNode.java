package SyntaxParser.OtherNode;

import LlvmIr.Type.ArrayType;
import LlvmIr.Type.LlvmType;
import LlvmIr.Type.PointerType;
import Symbol.Symbol;
import Symbol.SymbolCenter;
import SyntaxParser.ExpNode.ConstExpNode;
import SyntaxParser.ExpNode.EqExpNode;
import SyntaxParser.ExpNode.LAndExpNode;
import SyntaxParser.Node;
import java.util.ArrayList;
import Symbol.ConstSymbol;
import Util.ErrorType;
import Util.Printer;

public class ConstDefNode extends Node {
    private TokenNode identNode;
    private ArrayList<ConstExpNode> constExpNodes;
    private ConstInitValNode constInitValNode;
    private ConstSymbol constSymbol;
    public ConstDefNode(int startLine, int endLine, TokenNode identNode, ArrayList<ConstExpNode> constExpNodes, ConstInitValNode constInitValNode) {
        super(startLine, endLine);
        this.identNode = identNode;
        this.constExpNodes = constExpNodes;
        this.constInitValNode = constInitValNode;
    }
    public ConstSymbol createSymbol(){
        String name=identNode.getToken().getValue();
        int dim=constExpNodes.size();
        ArrayList<Integer> lens=new ArrayList<>();
        int length=1;
        for(ConstExpNode constExpNode:constExpNodes){
            lens.add(constExpNode.compute());
            length*=lens.get(lens.size()-1);
        }
        LlvmType llvmType;
        if(dim==0){
            llvmType=LlvmType.Int32;
        }
        else{
            llvmType=new ArrayType(length,LlvmType.Int32);
        }
        boolean isZeroInitial=(constInitValNode.getConstExpNode()==null&&constInitValNode.getConstInitValNodes().size()==0);
        ArrayList<Integer> initial=constInitValNode.compute();
        return new ConstSymbol(name,dim,lens,initial,isZeroInitial,llvmType);
    }
    public void checkError(){
        this.constSymbol=createSymbol();
        if(!SymbolCenter.addSymbol(constSymbol)){
            Printer.addErrorMsg(ErrorType.b,identNode.getStartLine());
        }
    }

    public ConstSymbol getConstSymbol() {
        return constSymbol;
    }

    public String toString(){
        return "<ConstDef>\n";
    }
}
