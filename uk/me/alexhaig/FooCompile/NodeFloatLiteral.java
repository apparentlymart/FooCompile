
package uk.me.alexhaig.FooCompile;

public class NodeFloatLiteral extends NodeNumberLiteral {
    double val;

    public NodeFloatLiteral(double val) {
        this.val = val;
    }

    public static boolean canStart(Tokeniser t) {
        return t.peek() instanceof TokenFloatLiteral;
    }

    public static Node parse(Tokeniser t) {
        int startline = t.getLine(), startcolumn = t.getColumn();
        TokenFloatLiteral tok = (TokenFloatLiteral)(t.read());
        if (!(tok instanceof TokenFloatLiteral))
            throw new ParserException(t, "Invalid float literal", "Expecting a float literal but got " + tok);

        return setPos(new NodeFloatLiteral(tok.val), startline, startcolumn);
    }

    public Type getType(Module m, Type coerceType, Scope localScope) {
        if (coerceType.size != -1 && coerceType.canStore(this)) {
            return coerceType;
        }
        return Type.FLOAT;
    }

    public String toString() {
        return "NodeFloatLiteral("+val+")";
    }

    public double getFloatValue() {
		return val;
	}

    public int getIntegerValue() {
		return (int)val;
	}

    public void printSelf(IndentingWriter w) {
        w.println(getClassName()  + " (" + val + ");");
    }

}
