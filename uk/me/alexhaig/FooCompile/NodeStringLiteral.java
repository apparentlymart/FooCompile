
package uk.me.alexhaig.FooCompile;

public class NodeStringLiteral extends NodeLiteral{
    String st;

    public NodeStringLiteral(String st) {
        this.st = st;
    }

    public static boolean canStart(Tokeniser t) {
        Token tok = t.peek();
        if (tok instanceof TokenStringLiteral) {
            return !((TokenStringLiteral) tok).isChar;
        } else {
            return false;
        }
    }

    public static Node parse(Tokeniser t) {
        int startline = t.getLine(), startcolumn = t.getColumn();
        return setPos(new NodeStringLiteral(((TokenStringLiteral) t.read()).st), startline, startcolumn);
    }

    //Effectively strings are not intrinsic, so should not be able to be constant
    public boolean isConstant(Module m, Scope localScope) {
		return false;
	}

    public Type getType(Module m, Type coerceType, Scope localScope) {
        return Type.MAGICSTRING;
    }

    public String toString() {
        return "NodeStringLiteral(\""+ st +"\")";
    }

    public void printSelf(IndentingWriter w) {
        w.println(getClassName()  + " (" + st + ");");
    }

}
