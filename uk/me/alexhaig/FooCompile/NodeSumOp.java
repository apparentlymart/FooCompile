
package uk.me.alexhaig.FooCompile;

public class NodeSumOp extends NodeOperation {
    NodeExpr lhs;
    NodeExpr rhs;

    boolean neg;

    public NodeSumOp(NodeExpr lhs, NodeExpr rhs, boolean neg) {
        this.lhs = lhs;
        this.rhs = rhs;
        this.neg = neg;
    }

    public static boolean canStart(Tokeniser t) {
        return NodeProductOp.canStart(t);
    }

    public static Node parse(Tokeniser t) {
        int startline = t.getLine(), startcolumn = t.getColumn();
        NodeExpr lhs = (NodeExpr)(NodeProductOp.parse(t));
        boolean neg;

        Token tok = t.peek();

        if (tok == TokenSymbol.PLUS || tok == TokenSymbol.MINUS) {
            neg = (tok == TokenSymbol.MINUS);
            t.read();
            return setPos(new NodeSumOp(lhs, (NodeExpr)(NodeExpr.parse(t)), neg), startline, startcolumn);
        }
        else return setPos(lhs, startline, startcolumn);
    }

    public Type getType(Module m, Type coerceType, Scope localScope) {
        Type retType;
        if (coerceType.isNumeric()) retType = coerceType;
        else retType = lhs.getType(m, Type.INT, localScope);

        if (!lhs.getType(m, retType, localScope).isNumeric()) {
            throw CheckerException.numberExpected(lhs, lhs.getType(m, retType, localScope));
        }
        if (!rhs.getType(m, retType, localScope).isNumeric()) {
            throw CheckerException.numberExpected(rhs, rhs.getType(m, retType, localScope));
        }
        return retType;
    }

    public boolean isConstant(Module m, Scope localScope) {
		return (lhs.isConstant(m, localScope) && rhs.isConstant(m, localScope));
	}

    public NodeLiteral evaluateConstant(Module m, Scope localScope) {
		NodeLiteral ll = lhs.evaluateConstant(m, localScope);
		NodeLiteral rl = rhs.evaluateConstant(m, localScope);

		if (neg) return new NodeIntegerLiteral(ll.getIntegerValue() - rl.getIntegerValue());
		else return new NodeIntegerLiteral(ll.getIntegerValue() + rl.getIntegerValue());
	}

    public String toString() {
        return "NodeSumOp(" + lhs + (neg ? '-' : '+') + rhs + ")";
    }

    public void printSelf(IndentingWriter w) {
        w.println(getClassName() + " (" + (neg ? '-' : '+') + ") [");
        w.indent();
        lhs.printSelf(w);
        rhs.printSelf(w);
        w.outdent();
        w.println("]");
    }
}
