
package uk.me.alexhaig.FooCompile;

public class NodeLiteral extends NodeTerm {

    public static boolean canStart(Tokeniser t) {
        return (NodeBooleanLiteral.canStart(t)
             || NodeNumberLiteral.canStart(t)
             || NodeCharacterLiteral.canStart(t)
             || NodeStringLiteral.canStart(t));
    }

    public static Node parse(Tokeniser t) {
        int startline = t.getLine(), startcolumn = t.getColumn();
        if (NodeBooleanLiteral.canStart(t))   return setPos(NodeBooleanLiteral.parse(t), startline, startcolumn);
        if (NodeNumberLiteral.canStart(t))    return setPos(NodeNumberLiteral.parse(t), startline, startcolumn);
        if (NodeCharacterLiteral.canStart(t)) return setPos(NodeCharacterLiteral.parse(t), startline, startcolumn);
        if (NodeStringLiteral.canStart(t))    return setPos(NodeStringLiteral.parse(t), startline, startcolumn);
        throw new InternalErrorException("Can't parse literal");
    }

    public boolean isConstant(Module m, Scope localScope) {
		return true;
	}

    public NodeLiteral evaluateConstant(Module m, Scope localScope) {
		return this;
	}

    public String toString() {
        return "NodeLiteral()";
    }

    public double getFloatValue() {
		throw new InternalErrorException("Can't convert "+this+" to float");
	}

    public int getIntegerValue() {
		throw new InternalErrorException("Can't convert "+this+" to int");
	}

    public char getCharacterValue() {
		throw new InternalErrorException("Can't convert "+this+" to character");
	}

}
