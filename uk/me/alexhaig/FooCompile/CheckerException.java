
package uk.me.alexhaig.FooCompile;

public class CheckerException extends RuntimeException {
    public String message;
    public String detail;
    private int line;
    private int column;

    public CheckerException(Node n, String m, String d) {
        super(m);
        this.line = n.line;
        this.column = n.column;
        this.message = m;
        this.detail = d;
    }
    public CheckerException(int line, int column, String m, String d) {
        super(m);
        this.line = line;
        this.column = column;
        this.message = m;
        this.detail = d;
    }

    public static CheckerException typeMismatch(Node n, Type expected, Type got) {
        return new CheckerException(n, "Type mismatch", "Expected "+expected+" but got "+got);
    }
    public static CheckerException typeMismatch(Node n, String expected, Type got) {
        return new CheckerException(n, "Type mismatch", "Expected "+expected+" but got "+got);
    }
    public static CheckerException incompatibleTypes(Node n, Type expected, Type got) {
        return new CheckerException(n, "Incompatible types", "Cannot use "+got+ " in place of "+expected);
    }
    public static CheckerException incompatibleTypes(Node n, String expected, Type got) {
        return new CheckerException(n, "Incompatible types", "Cannot use "+got+ " in place of "+expected);
    }
    public static CheckerException symbolNotFound(NodeQRef qr) {
        return new CheckerException(qr, "Unresolved symbol", "'"+qr.name+"' could not be resolved to a valid symbol");
    }
    public static CheckerException wrongSymbolKind(NodeQRef qr, String expected, Symbol got) {
        return new CheckerException(qr, "Expected "+expected, "'" + qr.name + "' is a "+got.symbolKind()+", while "+expected+" was expected");
    }
    public static CheckerException numberExpected(Node n, Type got) {
        return new CheckerException(n, "Invalid operand", "Numeric type required, but got "+got);
    }
    public static CheckerException lValueExpected(Node n) {
        return new CheckerException(n, "Invalid operand", "Assignable operand required, but got "+n);
    }
    public static CheckerException intrinsicTypeExpected(Node n, Type got) {
        return new CheckerException(n, "Intrinsic type expected", "Intrinisic type expected, but got object "+got);
    }
    public static CheckerException constantTypeExpected(Node n, NodeExpr ne) {
        return new CheckerException(n, "Constant expected", "Required constant value, but got "+ne);
    }
    public static CheckerException duplicateDeclaration(NodeDeclaration thisDec) {
        return new CheckerException(thisDec, "Duplicate declaration", "The symbol " + thisDec.name + " has already been declared");
    }
    public static CheckerException duplicateDeclaration(NodeDeclaration thisDec, NodeDeclaration prevDec) {
        return new CheckerException(thisDec, "Duplicate declaration", "The symbol " + thisDec.name + " was previously declared at line " + thisDec.line + ", column " + thisDec.column);
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
