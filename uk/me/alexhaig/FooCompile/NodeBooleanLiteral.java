
package uk.me.alexhaig.FooCompile;

public class NodeBooleanLiteral extends NodeLiteral {
    boolean val;

    public NodeBooleanLiteral(boolean val) {
        this.val = val;
    }

    public static boolean canStart(Tokeniser t) {
        Token tok = t.peek();
        return (tok == TokenKeyword.TRUE || tok == TokenKeyword.FALSE);
    }

    public static Node parse(Tokeniser t) {
        int startline = t.getLine(), startcolumn = t.getColumn();

        if (t.peek() == TokenKeyword.TRUE) {
            t.read();
            return setPos(new NodeBooleanLiteral(true), startline, startcolumn);
        }
        else if (t.peek() == TokenKeyword.FALSE) {
            t.read();
            return setPos(new NodeBooleanLiteral(false), startline, startcolumn);
        }
        else throw new InternalErrorException("Can't parse boolean literal");
    }

    public Type getType(Module m, Type coerceType, Scope localScope) {
        return Type.BOOL;
    }

    public boolean getBooleanValue() {
		return val;
	}

    public String toString() {
        return "NodeBooleanLiteral("+val+")";
    }

    public void printSelf(IndentingWriter w) {
        w.println(getClassName()  + " (" + val + ");");
    }

}
