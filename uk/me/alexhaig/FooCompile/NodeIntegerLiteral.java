
package uk.me.alexhaig.FooCompile;

public class NodeIntegerLiteral extends NodeNumberLiteral {
    int val;

    public NodeIntegerLiteral(int val) {
        this.val = val;
    }

    public static boolean canStart(Tokeniser t) {
        return t.peek() instanceof TokenIntegerLiteral;
    }

    public static Node parse(Tokeniser t) {
        int startline = t.getLine(), startcolumn = t.getColumn();
        TokenIntegerLiteral tok = (TokenIntegerLiteral)(t.read());
        if (!(tok instanceof TokenIntegerLiteral)) throw new ParserException(t, "Invalid integer literal", "Expecting an integer literal but got " + tok);

        return setPos(new NodeIntegerLiteral(tok.val), startline, startcolumn);
    }

    public Type getType(Module m, Type coerceType, Scope localScope) {
        if (coerceType.size != -1 && coerceType.canStore(this)) {
            return coerceType;
        }
        return Type.INT;
    }

    public String toString() {
        return "NodeIntegerLiteral("+val+")";
    }

    public double getFloatValue() {
		return val;
	}

    public int getIntegerValue() {
		return val;
	}

    public void printSelf(IndentingWriter w) {
        w.println(getClassName()  + " (" + val + ");");
    }


}
