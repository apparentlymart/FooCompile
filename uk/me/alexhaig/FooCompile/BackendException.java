
package uk.me.alexhaig.FooCompile;

public class BackendException extends RuntimeException {
    public String message;
    public String detail;
    private int line;
    private int column;

    public BackendException(Node n, String m, String d) {
        super(m);
        this.line = n.line;
        this.column = n.column;
        this.message = m;
        this.detail = d;
    }
    public BackendException(int line, int column, String m, String d) {
        super(m);
        this.line = line;
        this.column = column;
        this.message = m;
        this.detail = d;
    }
    public BackendException(String m, String d) {
        super(m);
        this.message = m;
        this.detail = d;
    }

    public String getMessage() {
	if (line != 0) {
            return message+" at line "+line+", column "+column+"; "+detail;
	}
	else {
            return message+"; "+detail;
	}
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
