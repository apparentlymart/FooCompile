
package uk.me.alexhaig.FooCompile;

public class ParserException extends RuntimeException {
    public String message;
    public String detail;
    private int line;
    private int column;

    public ParserException(Tokeniser t, String m, String d) {
        super(m);
        line = t.getLine();
        column = t.getColumn();
        this.message = m;
        this.detail = d;
    }
    public ParserException(Scanner s, String m, String d) {
        super(m);
        line = s.getLine();
        column = s.getColumn();
        this.message = m;
        this.detail = d;
    }

    public String getMessage() {
        return message+" at line "+line+", column "+column+"; "+detail;
    }

    public String getSummary() {
        return message;
    }

    public String getDetail() {
        return detail;
    }

    public int getLine() {
        return line;
    }

    public int getColumn() {
        return column;
    }
}
