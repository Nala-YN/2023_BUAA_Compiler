package SyntaxParser.OtherNode;

import LlvmIr.Type.ArrayType;
import LlvmIr.Type.LlvmType;
import LlvmIr.Type.PointerType;
import Symbol.ConstSymbol;
import Symbol.SymbolCenter;
import Symbol.VarSymbol;
import SyntaxParser.ExpNode.ConstExpNode;
import SyntaxParser.ExpNode.EqExpNode;
import SyntaxParser.ExpNode.LAndExpNode;
import SyntaxParser.Node;
import Util.ErrorType;
import Util.Printer;

import java.util.ArrayList;

public class VarDefNode extends Node {
    private TokenNode identNode;
    private ArrayList<ConstExpNode> constExpNodes;
    private InitValNode initValNode;
    private VarSymbol varSymbol;
    public VarDefNode(int startLine, int endLine, TokenNode identNode, ArrayList<ConstExpNode> constExpNodes, InitValNode initValNode) {
        super(startLine, endLine);
        this.identNode = identNode;
        this.constExpNodes = constExpNodes;
        this.initValNode = initValNode;
    }
    public VarSymbol createSymbol(boolean isGlobal){
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
        boolean isZeroInitial=false;
        ArrayList<Integer> initial=new ArrayList<>();
        if(isGlobal){
            if(initValNode!=null){
                initial=initValNode.compute();
                isZeroInitial=initValNode.isZeroInitial();
            }
            else{
                isZeroInitial=true;
            }
        }
        return new VarSymbol(name,dim,lens,initial,isZeroInitial,llvmType);
    }
    public void checkError(boolean isGlobal){
        this.varSymbol=createSymbol(isGlobal);
        if(!SymbolCenter.addSymbol(varSymbol)){
            Printer.addErrorMsg(ErrorType.b, identNode.getStartLine());
        }
    }

    public VarSymbol getVarSymbol() {
        return varSymbol;
    }

    public InitValNode getInitValNode() {
        return initValNode;
    }

    public String toString(){
        return "<VarDef>\n";
    }
}
