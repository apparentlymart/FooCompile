
package uk.me.alexhaig.FooCompile;

public class NodeEqualityTestOp extends NodeOperation {
    NodeExpr lhs;
    NodeExpr rhs;
    boolean equals;

    public NodeEqualityTestOp(NodeExpr lhs, NodeExpr rhs, boolean equals) {
        this.lhs = lhs;
        this.rhs = rhs;
        this.equals = equals;
    }

    public static boolean canStart(Tokeniser t) {
        return NodeRelationTestOp.canStart(t);
    }

    public static Node parse(Tokeniser t) {
        int startline = t.getLine(), startcolumn = t.getColumn();
        NodeExpr lhs = (NodeExpr)(NodeRelationTestOp.parse(t));
        boolean equals;

        Token tok = t.peek();
        if (tok == TokenSymbol.EQUAL || tok == TokenSymbol.NOTEQUAL) {
            equals = (tok == TokenSymbol.EQUAL);
            t.read();
            return setPos(new NodeEqualityTestOp(lhs, (NodeExpr)(NodeExpr.parse(t)), equals), startline, startcolumn);
        }
        else return setPos(lhs, startline, startcolumn);
    }

    public Type getType(Module m, Type coerceType, Scope localScope) {
        Type lhsType = lhs.getType(m, coerceType, localScope);

        if (!(rhs.getType(m, lhsType, localScope).equals(m, lhsType, localScope))) throw CheckerException.typeMismatch(rhs, lhsType, rhs.getType(m, lhsType, localScope));

        return Type.BOOL;
    }

    public boolean isConstant(Module m, Scope localScope) {
		return (lhs.isConstant(m, localScope) && rhs.isConstant(m, localScope));
	}

    public NodeLiteral evaluateConstant(Module m, Scope localScope) {
		NodeLiteral ll = lhs.evaluateConstant(m, localScope);
		NodeLiteral rl = rhs.evaluateConstant(m, localScope);
		boolean test;
		if (ll instanceof NodeStringLiteral && rl instanceof NodeStringLiteral) {
			test = (((NodeStringLiteral) ll).st == ((NodeStringLiteral) rl).st);
		}
		else if (ll instanceof NodeCharacterLiteral && rl instanceof NodeCharacterLiteral) {
			test = (((NodeCharacterLiteral) ll).ch == ((NodeCharacterLiteral) rl).ch);
		}
		else if (ll instanceof NodeBooleanLiteral && rl instanceof NodeBooleanLiteral) {
			test = (((NodeBooleanLiteral) ll).val == ((NodeBooleanLiteral) rl).val);
		}

		else if (ll instanceof NodeNumberLiteral && ll instanceof NodeNumberLiteral ) {
			test = (((NodeNumberLiteral) ll).getFloatValue() == ((NodeNumberLiteral) rl).getFloatValue());
		}
		else throw CheckerException.typeMismatch(this, lhs.getType(m, Type.VOID, localScope), rhs.getType(m, lhs.getType(m, Type.VOID, localScope), localScope));

		return new NodeBooleanLiteral(test == equals);
	}

    public String toString() {
        return "NodeEqualityTestOp(" + lhs + (equals ? "==" : "!=") + rhs + ")";
    }

    public void printSelf(IndentingWriter w) {
        w.println(getClassName() + " ("+(equals ? "==" : "!=")+") [");
        w.indent();
        lhs.printSelf(w);
        rhs.printSelf(w);
        w.outdent();
        w.println("]");
    }
}
