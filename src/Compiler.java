import LlvmIr.IRBuilder;
import LlvmIr.Module;
import SyntaxParser.OtherNode.CompUnitNode;
import SyntaxParser.SyntaxParser;
import ToMips.AsmBuilder;
import TokenParser.Lexer;
import TokenParser.TokenStream;
import Util.Printer;
import Optimize.Optimizer;

import java.io.FileInputStream;
import java.io.IOException;
import java.io.PushbackInputStream;

public class Compiler {
    public static void main(String[] args) throws IOException {
        PushbackInputStream in=new PushbackInputStream(new FileInputStream("testfile.txt"),16);
        Printer.setMode(2);
        Lexer lexer=new Lexer(in);
        lexer.parse();
        TokenStream tokenStream=lexer.getTokenStream();
        Printer.printTokenStream(tokenStream);
        if(Printer.getMode()<=0)return;
        SyntaxParser syntaxParser=new SyntaxParser(tokenStream);
        CompUnitNode rootNode=syntaxParser.parseCompUnitNode();
        Printer.printError();
        if(Printer.getMode()<=1||Printer.hasError())return;
        IRBuilder irBuilder=new IRBuilder();
        irBuilder.buildCompUnit(rootNode);
        Module module=irBuilder.getModule();
        if(Printer.optimizeOn){
            Optimizer.optimize(module);
        }
        Printer.printMips(AsmBuilder.genAsm(module));
    }
}
