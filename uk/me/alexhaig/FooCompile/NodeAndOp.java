
package uk.me.alexhaig.FooCompile;

public class NodeAndOp extends NodeOperation {
    NodeExpr lhs;
    NodeExpr rhs;
    boolean bitwise;

    public NodeAndOp(NodeExpr lhs, NodeExpr rhs, boolean bitwise) {
        this.lhs = lhs;
        this.rhs = rhs;
        this.bitwise = bitwise;
    }

    public static boolean canStart(Tokeniser t) {
        return NodeEqualityTestOp.canStart(t);
    }

    public static Node parse(Tokeniser t) {
        int startline = t.getLine(), startcolumn = t.getColumn();
        NodeExpr lhs = (NodeExpr)(NodeEqualityTestOp.parse(t));
        boolean bitwise = false;
        Token tok = t.peek();
        if (tok == TokenSymbol.LOGAND || tok == TokenSymbol.BITAND) {
            bitwise = (tok == TokenSymbol.BITAND);
            t.read();
            return setPos(new NodeAndOp(lhs, (NodeExpr)(NodeExpr.parse(t)), bitwise), startline, startcolumn);
        }
        else return setPos(lhs, startline, startcolumn);
    }

    public Type getType(Module m, Type coerceType, Scope localScope) {
        if (bitwise) {
            Type retType;
            if (coerceType.isNumeric()) retType = coerceType;
            else retType = Type.INT;

            if (!lhs.getType(m, retType, localScope).isNumeric()) {
                throw CheckerException.numberExpected(lhs, lhs.getType(m, retType, localScope));
            }
            if (!rhs.getType(m, retType, localScope).isNumeric()) {
                throw CheckerException.numberExpected(rhs, rhs.getType(m, retType, localScope));
            }
            return retType;
        }
        else {
            if (!(lhs.getType(m, Type.BOOL, localScope).equals(m, Type.BOOL, localScope))) throw CheckerException.typeMismatch(lhs, Type.BOOL, lhs.getType(m, Type.BOOL, localScope));
            if (!(rhs.getType(m, Type.BOOL, localScope).equals(m, Type.BOOL, localScope))) throw CheckerException.typeMismatch(rhs, Type.BOOL, rhs.getType(m, Type.BOOL, localScope));
            return Type.BOOL;
        }
    }

    public boolean isConstant(Module m, Scope localScope) {
		return (lhs.isConstant(m, localScope) && rhs.isConstant(m, localScope));
	}

    public NodeLiteral evaluateConstant(Module m, Scope localScope) {
		NodeLiteral ll = lhs.evaluateConstant(m, localScope);
		NodeLiteral rl = rhs.evaluateConstant(m, localScope);

		if (bitwise) {
			return new NodeBooleanLiteral(((NodeBooleanLiteral)ll).getBooleanValue() && ((NodeBooleanLiteral)rl).getBooleanValue());
		}
		else return new NodeIntegerLiteral(ll.getIntegerValue() & rl.getIntegerValue());
	}

    public String toString() {
        return "NodeAndOp(" + lhs + ( bitwise ? "&" : "&&" ) + rhs + ")";
    }

    public void printSelf(IndentingWriter w) {
        w.println(getClassName() + " ("+( bitwise ? "bitwise" : "logical" )+") [");
        w.indent();
        lhs.printSelf(w);
        rhs.printSelf(w);
        w.outdent();
        w.println("]");
    }
}
