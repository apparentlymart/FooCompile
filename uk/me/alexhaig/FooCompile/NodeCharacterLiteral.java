package uk.me.alexhaig.FooCompile;

public class NodeCharacterLiteral extends NodeLiteral {
    char ch;

    public NodeCharacterLiteral(char ch) {
        this.ch = ch;
    }

    public static boolean canStart(Tokeniser t) {
        Token tok = t.peek();
        if (tok instanceof TokenStringLiteral) {
            return ((TokenStringLiteral) tok).isChar;
        } else {
            return false;
        }
    }

    public static Node parse(Tokeniser t) {
        int startline = t.getLine(), startcolumn = t.getColumn();
        Token tok = t.read();
        char[] firstchar = new char[1];
        ((TokenStringLiteral) tok).st.getChars(0,1,firstchar,0);
        char ch = firstchar[0];
        return setPos(new NodeCharacterLiteral(ch), startline, startcolumn);
    }

    public Type getType(Module m, Type coerceType, Scope localScope) {
        return Type.CHAR;
    }

    public String toString() {
        return "NodeCharacterLiteral('"+ch+"')";
    }

    public double getFloatValue() {
		return ch;
	}

    public int getIntegerValue() {
		return ch;
	}

    public char getCharacterValue() {
		return ch;
	}

    public void printSelf(IndentingWriter w) {
        w.println(getClassName()  + " (" + ch + ");");
    }

}
