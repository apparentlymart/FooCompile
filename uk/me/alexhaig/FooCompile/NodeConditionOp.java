
package uk.me.alexhaig.FooCompile;

public class NodeConditionOp extends NodeOperation {
    NodeExpr cond;
    NodeExpr te;
    NodeExpr fe;

    public NodeConditionOp(NodeExpr cond, NodeExpr te, NodeExpr fe) {
        this.cond = cond;
        this.te = te;
        this.fe = fe;
    }

    public static boolean canStart(Tokeniser t) {
        return NodeOrOp.canStart(t);
    }

    public static Node parse(Tokeniser t) {
        int startline = t.getLine(), startcolumn = t.getColumn();
        NodeExpr cond = (NodeExpr)(NodeOrOp.parse(t));
        NodeExpr te;
        NodeExpr fe;

        if (t.peek() == TokenSymbol.QMARK) {
            t.read();
            te = (NodeExpr)(NodeExpr.parse(t));
            t.require(TokenSymbol.COLON);
            fe = (NodeExpr)(NodeExpr.parse(t));
            return setPos(new NodeConditionOp(cond, te, fe), startline, startcolumn);
        }
        else return setPos(cond, startline, startcolumn);
    }

    public Type getType(Module m, Type coerceType, Scope localScope) {

        if (!(((Type)cond.getType(m, Type.BOOL, localScope)).equals(m, Type.BOOL, localScope))) throw CheckerException.typeMismatch(cond, Type.BOOL, cond.getType(m, Type.BOOL, localScope));

        Type teType = te.getType(m, coerceType, localScope);

        if (!(fe.getType(m, teType, localScope) == teType)) throw CheckerException.typeMismatch(te, teType, te.getType(m, teType, localScope));

        return teType;
    }

    public boolean isConstant(Module m, Scope localScope) {
		if (cond.isConstant(m, localScope)) {
			NodeLiteral condNL = (NodeLiteral)cond.evaluateConstant(m, localScope);
			if (condNL instanceof NodeBooleanLiteral) {
				if(((NodeBooleanLiteral)condNL).val) {
					return te.isConstant(m, localScope);
				}
				else {
					return fe.isConstant(m, localScope);
				}
			}
			else throw new InternalErrorException("A condition operation must have a boolean type condition");
		}
		else return false;
	}

    public NodeLiteral evaluateConstant(Module m, Scope localScope) {
		if (cond.isConstant(m, localScope)) {
			NodeLiteral condNL = (NodeLiteral)cond.evaluateConstant(m, localScope);
			if (condNL instanceof NodeBooleanLiteral) {
				if(((NodeBooleanLiteral)condNL).val) {
					return te.evaluateConstant(m, localScope);
				}
				else {
					return fe.evaluateConstant(m, localScope);
				}
			}
			else throw new InternalErrorException("Can't evaluate a constant, non-boolean condition at compile time");
		}
		else throw new InternalErrorException("Can't evaluate a non-constant condition at compile time");
	}

    public String toString() {
        return "NodeConditionOp(" + cond + " ? " + te + " : " + fe + ")";
    }

    public void printSelf(IndentingWriter w) {
        w.println(getClassName() + " [");
        w.indent();
        cond.printSelf(w);
        te.printSelf(w);
        fe.printSelf(w);
        w.outdent();
        w.println("]");
    }
}
