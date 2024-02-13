package Util;

import LlvmIr.Module;
import LlvmIr.Value;
import SyntaxParser.Node;
import TokenParser.Token;
import TokenParser.TokenStream;

import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;

public class Printer {
    private static FileOutputStream out;
    private static boolean debug=true;
    private static int mode=0; //0 for TokenParser,1 for SyntaxParser
    private static boolean outputOn=true;
    public static boolean optimizeOn=true;
    private static ArrayList<ErrorMsg> errorMsgs=new ArrayList<>();
    static {
        try {
            out = new FileOutputStream("output.txt");
        } catch (FileNotFoundException e) {
            throw new RuntimeException(e);
        }
    }

    public static void debugOutput(String s){
        if(debug){
            System.out.print(s);
        }
    }
    public static void printTokenStream(TokenStream tokenStream) throws IOException {
        if(mode==0){
            for(Token token:tokenStream.getTokens()){
                out.write(token.toString().getBytes());
                debugOutput(token.toString());
            }
        }
    }
    public static void printToken(Token token) throws IOException {
        if(outputOn){
            if(mode==1){
                out.write(token.toString().getBytes());
                debugOutput(token.toString());
            }
        }

    }
    public static void printNode(Node node) throws IOException {
        if(outputOn){
            if(mode==1){
                out.write(node.toString().getBytes());
                debugOutput(node.toString());
            }
        }

    }

    public static boolean isOutputOn() {
        return outputOn;
    }
    public static void printError() throws IOException {
        FileOutputStream error = new FileOutputStream("error.txt");
        errorMsgs.sort(new Comparator<ErrorMsg>() {
            @Override
            public int compare(ErrorMsg o1, ErrorMsg o2) {
                if (o1.getLine() < o2.getLine()) {
                    return -1;
                } else if (o1.getLine() > o2.getLine()) {
                    return 1;
                } else {
                    return 0;
                }
            }
        });
        for(ErrorMsg errorMsg:errorMsgs){
            String errorInfo=errorMsg.getLine()+" "+errorMsg.getErrorType()+"\n";
            error.write(errorInfo.getBytes());
        }
    }
    public static boolean hasError(){
        return !errorMsgs.isEmpty();
    }
    public static void printLlvmIr(Value value) throws IOException {
        FileOutputStream llvmIr=new FileOutputStream("llvm_ir.txt");
        llvmIr.write(value.toString().getBytes());
    }
    public static void printMips(String string) throws IOException {
        FileOutputStream mips=new FileOutputStream("mips.txt");
        mips.write(string.getBytes());
    }
    public static void setOutputOn(boolean outputOn) {
        Printer.outputOn = outputOn;
    }

    public static void addErrorMsg(ErrorType errorType, int line){
        if(outputOn){
            System.out.println(line+" "+errorType);
            for(ErrorMsg errorMsg:errorMsgs){
                if(errorMsg.getLine()==line){
                    return;
                }
            }
            errorMsgs.add(new ErrorMsg(line, errorType));
        }
    }

    public static int getMode() {
        return mode;
    }

    public static void setMode(int mode) {
        Printer.mode = mode;
    }
}
