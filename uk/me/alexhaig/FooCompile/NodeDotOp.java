
package uk.me.alexhaig.FooCompile;

public class NodeDotOp extends NodeOperation {
    NodeExpr lhs;
    NodeExpr rhs;

    public NodeDotOp(NodeExpr lhs, NodeExpr rhs) {
        this.lhs = lhs;
        this.rhs = rhs;
    }

    public static boolean canStart(Tokeniser t) {
        return NodeUnaryNegOp.canStart(t);
    }

    public static Node parse(Tokeniser t) {
        int startline = t.getLine(), startcolumn = t.getColumn();
        NodeExpr lhs = (NodeExpr)(NodeUnaryNegOp.parse(t));

        Token tok = t.peek();

        if (tok == TokenSymbol.DOT) {
            t.read();
            return setPos(new NodeDotOp(lhs, (NodeExpr)(NodeExpr.parse(t))), startline, startcolumn);
        }
        else return setPos(lhs, startline, startcolumn);
    }

    public Type getType(Module m, Type coerceType, Scope localScope) {
		Type lhsType = lhs.getType(m, coerceType, localScope);

        //TODO: Complete NodeDotOp's getType function
        //scopes findSymbol in getrType will fail if lsh is another NodeDotOp

		if (rhs instanceof NodeFunctionCall) return ((NodeFunctionCall)rhs).getType(m, coerceType, localScope, lhsType.getClass(m));
		if (rhs instanceof NodeVariableRef) return ((NodeVariableRef)rhs).getType(m, coerceType, localScope, lhsType.getClass(m));

        return rhs.getType(m, coerceType, localScope);
    }

    public boolean isConstant(Module m, Scope localScope) {
		return false;
	}

    public NodeLiteral evaluateConstant(Module m, Scope localScope) {
		throw new InternalErrorException("This kind of reference can't be constant");
	}

    public String toString() {
        return "NodeDotOp(" + lhs + "." + rhs + ")";
    }

    public void printSelf(IndentingWriter w) {
        w.println(getClassName() + " [");
        w.indent();
        lhs.printSelf(w);
        rhs.printSelf(w);
        w.outdent();
        w.println("]");
    }
}
