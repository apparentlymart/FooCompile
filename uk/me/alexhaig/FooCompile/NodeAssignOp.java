
package uk.me.alexhaig.FooCompile;

public class NodeAssignOp extends NodeOperation {
    NodeExpr lhs;
    NodeExpr rhs;

    public NodeAssignOp(NodeExpr lhs, NodeExpr rhs) {
        this.lhs = lhs;
        this.rhs = rhs;
    }

    public static boolean canStart(Tokeniser t) {
        return NodeConditionOp.canStart(t);
    }

    public static Node parse(Tokeniser t) {
        int startline = t.getLine(), startcolumn = t.getColumn();
        NodeExpr lhs = (NodeExpr)(NodeConditionOp.parse(t));
        if (t.peek() == TokenSymbol.ASSIGN) {
            t.read();
            return setPos(new NodeAssignOp(lhs, (NodeExpr)(NodeExpr.parse(t))), startline, startcolumn);
        }
        else return setPos(lhs, startline, startcolumn);
    }

    public Type getType(Module m, Type coerceType, Scope localScope) {
        if (! Checker.isLValue(lhs)) throw CheckerException.lValueExpected(lhs);

        Type lhsType = lhs.getType(m, coerceType, localScope);

        if (!( rhs.getType(m, lhsType, localScope).canBe(m, lhsType))) throw CheckerException.incompatibleTypes(rhs, lhsType, rhs.getType(m, lhsType, localScope));

        return lhsType;
    }

    public boolean isConstant(Module m, Scope localScope) {
		return false;
	}

    public NodeLiteral evaluateConstant(Module m, Scope localScope) {
		throw new InternalErrorException("Can't use assignment in constant expression");
	}

    public String toString() {
        return "NodeAssignOp(" + lhs + "=" + rhs + ")";
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
