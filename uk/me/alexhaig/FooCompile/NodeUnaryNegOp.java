
package uk.me.alexhaig.FooCompile;

public class NodeUnaryNegOp extends NodeOperation {
    NodeExpr expr;

    boolean bitwise;

    public NodeUnaryNegOp(NodeExpr expr, boolean bitwise) {
        this.expr = expr;
        this.bitwise = bitwise;
    }

    public static boolean canStart(Tokeniser t) {
        Token tok = t.peek();
        return (tok == TokenSymbol.LOGNOT || tok == TokenSymbol.BITNOT || NodeIncrOp.canStart(t));
    }

    public static Node parse(Tokeniser t) {
        int startline = t.getLine(), startcolumn = t.getColumn();
        boolean bitwise;

        Token tok = t.peek();

        if (tok == TokenSymbol.LOGNOT || tok == TokenSymbol.BITNOT) {
            bitwise = (tok == TokenSymbol.BITNOT);
            t.read();

            return setPos(new NodeUnaryNegOp((NodeExpr)(NodeExpr.parse(t)), bitwise), startline, startcolumn);
        }
        else return setPos(NodeIncrOp.parse(t), startline, startcolumn);
    }

    public Type getType(Module m, Type coerceType, Scope localScope) {
        if (bitwise) {
            Type retType;
            retType = Type.INT;
            if (!(expr.getType(m, retType, localScope).equals(m, retType, localScope))) throw CheckerException.typeMismatch(expr, retType, expr.getType(m, retType, localScope));
            return retType;
        } else {
            if (!(expr.getType(m, Type.BOOL, localScope).equals(m, Type.BOOL, localScope))) throw CheckerException.typeMismatch(expr, Type.BOOL, expr.getType(m, Type.BOOL, localScope));
            return Type.BOOL;
        }
    }

    public boolean isConstant(Module m, Scope localScope) {
		return expr.isConstant(m, localScope);
	}

    public NodeLiteral evaluateConstant(Module m, Scope localScope) {
		NodeLiteral exprNL = expr.evaluateConstant(m, localScope);

		if (bitwise) {
			if (exprNL instanceof NodeFloatLiteral) {
				throw new InternalErrorException("Cannot apply bitwise unary negation to a constant value of type float");
			}
			else {
				int val = ~(exprNL.getIntegerValue());
				return new NodeIntegerLiteral(val);
			}
		}
		else {
			boolean val = !(((NodeBooleanLiteral)exprNL).val);
			return new NodeBooleanLiteral(val);
		}
	}

    public String toString() {
        return "NodeUnaryNegOp(" + (bitwise ? "~" : "!") + expr + ")";
    }

    public void printSelf(IndentingWriter w) {
        w.println(getClassName() + " (" + (bitwise ? "~" : "!") + ") [");
        w.indent();
        expr.printSelf(w);
        w.outdent();
        w.println("]");
    }
}
