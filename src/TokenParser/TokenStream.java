package TokenParser;

import TokenParser.Token;

import java.util.ArrayList;

public class TokenStream {
    private ArrayList<Token> tokens=new ArrayList<>();
    private int pos=0;
    private int lastPos=0;
    public Token read(){
        if(pos>=tokens.size()){
            return null;
        }
        else{
            pos++;
            return tokens.get(pos-1);
        }
    }
    public void setLastPos(){
        lastPos=pos;
    }
    public void rollBackPos(){
        pos=lastPos;
    }
    public Token unread(){
        if(pos>0){
            pos--;
            if(pos>0){
                return tokens.get(pos-1);
            }
        }
        return null;
    }
    public Token look(int step){
        if(pos+step-1>=tokens.size()){
            return null;
        }
        else{
            return tokens.get(pos+step-1);
        }
    }
    public void addToken(Token token){
        tokens.add(token);
    }
    public ArrayList<Token> getTokens(){
        return tokens;
    }
}
