
package uk.me.alexhaig.FooCompile;

public class NodeBitShiftOp extends NodeOperation {
    NodeExpr lhs;
    NodeExpr rhs;

    boolean left;

    public NodeBitShiftOp(NodeExpr lhs, NodeExpr rhs, boolean left) {
        this.lhs = lhs;
        this.rhs = rhs;
        this.left = left;
    }

    public static boolean canStart(Tokeniser t) {
        return NodeDotOp.canStart(t);
    }

    public static Node parse(Tokeniser t) {
        int startline = t.getLine(), startcolumn = t.getColumn();
        NodeExpr lhs = (NodeExpr)(NodeDotOp.parse(t));
        boolean left;

        Token tok = t.peek();

        if (tok == TokenSymbol.SHIFTLEFT || tok == TokenSymbol.SHIFTRIGHT) {
            left = (tok == TokenSymbol.SHIFTLEFT);
            t.read();
            return setPos(new NodeBitShiftOp(lhs, (NodeExpr)(NodeExpr.parse(t)), left), startline, startcolumn);
        }
        else return setPos(lhs, startline, startcolumn);
    }

    public Type getType(Module m, Type coerceType, Scope localScope) {
        Type retType;
        if (coerceType.intrinsicType == TokenKeyword.INT) {
            retType = coerceType;
        }
        else {
            retType = Type.INT;
        }

        Type lhsType = lhs.getType(m, retType, localScope);

        if (!(rhs.getType(m, Type.INT, localScope).equals(m, Type.INT, localScope))) throw CheckerException.typeMismatch(rhs, Type.INT, rhs.getType(m, Type.INT, localScope));

        return lhsType;
    }

    public boolean isConstant(Module m, Scope localScope) {
		return (lhs.isConstant(m, localScope) && rhs.isConstant(m, localScope));
	}

    public NodeLiteral evaluateConstant(Module m, Scope localScope) {
		NodeLiteral ll = lhs.evaluateConstant(m, localScope);
		NodeLiteral rl = rhs.evaluateConstant(m, localScope);

		if (left) return new NodeIntegerLiteral(ll.getIntegerValue() << rl.getIntegerValue());
		else return new NodeIntegerLiteral(ll.getIntegerValue() >> rl.getIntegerValue());
	}

    public String toString() {
        return "NodeBitShiftOp(" + lhs + " " + (left ? "<<" : ">>") + " " + rhs + ")";
    }

    public void printSelf(IndentingWriter w) {
        w.println(getClassName() + " (" + (left ? "<<" : ">>") + ") [");
        w.indent();
        lhs.printSelf(w);
        rhs.printSelf(w);
        w.outdent();
        w.println("]");
    }
}
