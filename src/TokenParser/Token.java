package TokenParser;

public class Token {
    private String value;
    private TokenType type;
    private int lineNum;
    public Token(String value, TokenType type, int lineNum){
        this.lineNum=lineNum;
        this.type=type;
        this.value=value;
    }

    public String getValue() {
        return value;
    }

    public TokenType getType() {
        return type;
    }

    public int getLineNum() {
        return lineNum;
    }

    @Override
    public String toString() {
        return type.toString()+" "+value+'\n';
    }
}
