package TokenParser;

import java.io.IOException;
import java.io.PushbackInputStream;
import java.util.ArrayList;

public class Lexer {
    private PushbackInputStream input;
    private char nchar;
    private int lineNum=1;
    private TokenStream tokenStream=new TokenStream();
    public Lexer(PushbackInputStream input) throws IOException {
        this.input=input;
    }
    public void parse() throws IOException {
        Token token=getNextToken();
        while(token.getType()!= TokenType.EOF){
            tokenStream.addToken(token);
            token=getNextToken();
            System.out.println(token);
        }
    }
    private Token getNextToken() throws IOException {
        read();
        while(isBlank()){
            read();
        }
        StringBuilder value=new StringBuilder();
        if(isLetter()||nchar=='_'){
            while(isLetter()||nchar=='_'||isDigit()){
                value.append(nchar);
                read();
            }
            unread();
            TokenType type=getTokenType(value.toString());
            if(type== TokenType.IDENFR){
                return new Token(value.toString(), TokenType.IDENFR,lineNum);
            }
            else{
                return new Token(value.toString(),type,lineNum);
            }
        }
        else if(isDigit()){
            while(isDigit()){
                value.append(nchar);
                read();
            }
            unread();
            return new Token(value.toString(), TokenType.INTCON,lineNum);
        }
        else if(nchar=='\"'){
            value.append(nchar);
            read();
            while(nchar!='\"'){
                value.append(nchar);
                read();
            }
            value.append(nchar);
            return new Token(value.toString(), TokenType.STRCON,lineNum);
        }
        else if(nchar=='!'){
            read();
            if(nchar=='='){
                return new Token("!=", TokenType.NEQ,lineNum);
            }
            else{
                unread();
                return new Token("!", TokenType.NOT,lineNum);
            }
        }
        else if(nchar=='&'){
            read();
            return new Token("&&", TokenType.AND,lineNum);
        }
        else if(nchar=='|'){
            read();
            return new Token("||", TokenType.OR,lineNum);
        }
        else if(nchar=='+'){
            return new Token("+", TokenType.PLUS,lineNum);
        }
        else if(nchar=='-'){
            return new Token("-", TokenType.MINU,lineNum);
        }
        else if(nchar=='*'){
            return new Token("*", TokenType.MULT,lineNum);
        }
        else if(nchar=='/'){
            read();
            if(nchar=='/'){
                while(nchar!='\n'){
                    if(nchar=='\uFFFF'){
                        return new Token("\uFFFF",TokenType.EOF,lineNum);
                    }
                    read();
                }
                lineNum++;
                return getNextToken();
            }
            else if(nchar=='*'){
                read();
                if(nchar=='\n')lineNum++;
                while(true){
                    if(nchar=='\uFFFF'){
                        return new Token("\uFFFF",TokenType.EOF,lineNum);
                    }
                    if(nchar=='*'){
                        read();
                        if(nchar=='/'){
                            break;
                        }
                        else{
                            unread();
                        }
                    }
                    read();
                    if(nchar=='\n')lineNum++;
                }
                return getNextToken();
            }
            unread();
            return new Token("/", TokenType.DIV,lineNum);
        }
        else if(nchar=='%'){
            return new Token("%", TokenType.MOD,lineNum);
        }
        else if(nchar=='<'){
            read();
            if(nchar=='='){
                return new Token("<=", TokenType.LEQ,lineNum);
            }
            else{
                unread();
                return new Token("<", TokenType.LSS,lineNum);
            }
        }
        else if(nchar=='>'){
            read();
            if(nchar=='='){
                return new Token(">=", TokenType.GEQ,lineNum);
            }
            else{
                unread();
                return new Token(">", TokenType.GRE,lineNum);
            }
        }
        else if(nchar=='='){
            read();
            if(nchar=='='){
                return new Token("==", TokenType.EQL,lineNum);
            }
            else{
                unread();
                return new Token("=", TokenType.ASSIGN,lineNum);
            }
        }
        else if(nchar==';'){
            return new Token(";", TokenType.SEMICN,lineNum);
        }
        else if(nchar==','){
            return new Token(",", TokenType.COMMA,lineNum);
        }
        else if(nchar=='('){
            return new Token("(", TokenType.LPARENT,lineNum);
        }
        else if(nchar==')'){
            return new Token(")", TokenType.RPARENT,lineNum);
        }
        else if(nchar=='['){
            return new Token("[", TokenType.LBRACK,lineNum);
        }
        else if(nchar==']'){
            return new Token("]", TokenType.RBRACK,lineNum);
        }
        else if(nchar=='{'){
            return new Token("{", TokenType.LBRACE,lineNum);
        }
        else if(nchar=='}'){
            return new Token("}", TokenType.RBRACE,lineNum);
        }
        else if(nchar=='\n'){
            lineNum++;
            return getNextToken();
        }
        else if(nchar=='\r'){
            return getNextToken();
        }
        else if(isEOF()){
            return new Token(null, TokenType.EOF,lineNum);
        }
        else{
            while(!isEOF()){
                read();
                //System.out.print(nchar);
            }
            throw new RuntimeException("ERROR");
        }
    }
    private TokenType getTokenType(String str){
        switch (str){
            case "main" : return TokenType.MAINTK;
            case "const" : return TokenType.CONSTTK;
            case "int" : return TokenType.INTTK;
            case "break" : return TokenType.BREAKTK;
            case "continue" : return TokenType.CONTINUETK;
            case "if" : return TokenType.IFTK;
            case "else" : return TokenType.ELSETK;
            case "for" : return TokenType.FORTK;
            case "while":return TokenType.WHILETK;
            case "getint" : return TokenType.GETINTTK;
            case "printf" : return TokenType.PRINTFTK;
            case "return" : return TokenType.RETURNTK;
            case "void" : return TokenType.VOIDTK;
            default: return TokenType.IDENFR;
        }

    }
    private boolean isBlank(){
        return nchar==' '||nchar=='\t';
    }
    private boolean isDigit(){
        return nchar>='0'&&nchar<='9';
    }
    private boolean isNextLine(){
        return nchar=='\n';
    }
    private boolean isLetter(){
        return nchar>='a'&&nchar<='z'||
                nchar>='A'&&nchar<='Z';
    }
    private boolean isEOF(){
        return nchar=='\uFFFF';
    }
    private void read() throws IOException {
        nchar=(char)input.read();
        //System.out.print(nchar);
    }
    private void unread() throws IOException {
        input.unread(nchar);
    }

    public TokenStream getTokenStream() {
        return tokenStream;
    }
}
