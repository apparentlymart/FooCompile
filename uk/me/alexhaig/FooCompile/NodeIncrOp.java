
package uk.me.alexhaig.FooCompile;

public class NodeIncrOp extends NodeOperation {
    boolean preOp = false;
    boolean positive;
    NodeExpr expr;

    public NodeIncrOp(NodeExpr expr, boolean preOp, boolean positive) {
        this.expr = expr;
        this.preOp = preOp;
        this.positive = positive;
    }

    public static boolean canStart(Tokeniser t) {
        Token tok = t.peek();
        if (tok == TokenSymbol.INCREMENT || tok == TokenSymbol.DECREMENT) {
            return true;
        }
        else return NodeTerm.canStart(t);
    }

    public static Node parse(Tokeniser t) {
        int startline = t.getLine(), startcolumn = t.getColumn();
        boolean pre = false;
        boolean pos = true;

        Token tok = t.peek();
        if (tok == TokenSymbol.INCREMENT) {
            pre = true;
            pos = true;
            t.read();
        }
        else if (tok == TokenSymbol.DECREMENT) {
            pre = true;
            pos = false;
            t.read();
        }

        NodeExpr expr = (NodeExpr)(NodeTerm.parse(t));

        if (pre == true) {
            return setPos(new NodeIncrOp(expr, pre, pos), startline, startcolumn);
        }

        tok = t.peek();
        if (tok == TokenSymbol.INCREMENT) {
            pos = true;
            t.read();
            return setPos(new NodeIncrOp(expr, false, true), startline, startcolumn);
        }
        else if (tok == TokenSymbol.DECREMENT) {
            pos = false;
            t.read();
            return setPos(new NodeIncrOp(expr, false, false), startline, startcolumn);
        }
        else {
            return setPos(expr, startline, startcolumn);
        }
    }

    public Type getType(Module m, Type coerceType, Scope localScope) {
        if (! Checker.isLValue(expr)) throw CheckerException.lValueExpected(expr);

        //expr must be a NodeVariableRef or NodeArrayDeref, must have a numeric type

        Type exprType = expr.getType(m, coerceType, localScope);
        if (!exprType.isNumeric()) throw CheckerException.numberExpected(expr, exprType);

        return exprType;
    }

    public boolean isConstant(Module m, Scope localScope) {
		return false;
	}

    public NodeLiteral evaluateConstant(Module m, Scope localScope) {
		throw new InternalErrorException("Can't use assignment in constant expression");
	}

    public String toString() {
        return "NodeIncrOp()";
    }

    public void printSelf(IndentingWriter w) {
        w.println(getClassName() + " ("+(preOp ? "pre": "post")+","+(positive ? "++": "--")+") [");
        w.indent();
        expr.printSelf(w);
        w.outdent();
        w.println("]");
    }
}
