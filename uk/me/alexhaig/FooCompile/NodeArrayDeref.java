
package uk.me.alexhaig.FooCompile;

public class NodeArrayDeref extends NodeTerm {
    NodeVariableRef varRef;
    int index;

    public NodeArrayDeref(NodeVariableRef varRef, int index) {
        this.varRef = varRef;
        this.index = index;
    }

    public static boolean canStart(Tokeniser t) {
        return NodeVariableRef.canStart(t);
    }

    public static Node parse(Tokeniser t) {
        int startline = t.getLine(), startcolumn = t.getColumn();
        NodeVariableRef vr = (NodeVariableRef)(NodeVariableRef.parse(t));

        return setPos(continueParsing(t, vr), startline, startcolumn);
    }

    public static Node continueParsing(Tokeniser t, NodeVariableRef name) {
        int index;

        t.require(TokenSymbol.LBRACKET);

        Token tok = t.peek();
        if (tok instanceof TokenIntegerLiteral) {
            t.read();
            index = ((TokenIntegerLiteral)tok).val;
        }
        else throw new ParserException(t, "Invalid array index", "Expected array index, but got "+tok.toString());

        t.require(TokenSymbol.RBRACKET);

        return new NodeArrayDeref(name, index);
    }

	public Type getType(Module m, Type coerceType, Scope localScope) {
		Type coercer = new Type(coerceType);
		coercer.arraymod++;
		coercer = new Type(varRef.getType(m, coercer, localScope));
		if (coercer.arraymod < 1) throw new CheckerException(this, "Expected array", "Expected an array, but got " + getType(m, coercer, localScope));
		coercer.arraymod--;
		return coercer;
	}

    public boolean isConstant(Module m, Scope localScope) {
		return false;
	}

    public NodeLiteral evaluateConstant(Module m, Scope localScope) {
		throw new InternalErrorException("An array derefence cannot be static");
	}

    public String toString() {
        return "NodeArrayDeref()";
    }

    public void printSelf(IndentingWriter w) {
        w.println(getClassName() + " [");
        w.indent();
        varRef.printSelf(w);
        w.println(index+";");
        w.outdent();
        w.println("]");
    }
}
