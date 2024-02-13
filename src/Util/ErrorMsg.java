package Util;

public class ErrorMsg {
    private int line;
    private ErrorType errorType;

    public ErrorMsg(int line, ErrorType errorType) {
        this.line = line;
        this.errorType = errorType;
    }

    public int getLine() {
        return line;
    }

    public ErrorType getErrorType() {
        return errorType;
    }
}
