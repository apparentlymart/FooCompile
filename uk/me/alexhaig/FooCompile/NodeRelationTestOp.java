
package uk.me.alexhaig.FooCompile;

public class NodeRelationTestOp extends NodeOperation {
    NodeExpr lhs;
    NodeExpr rhs;

    boolean great;
    boolean equalTo;

    public NodeRelationTestOp(NodeExpr lhs, NodeExpr rhs, boolean great, boolean equalTo) {
        this.lhs = lhs;
        this.rhs = rhs;
        this.great = great;
        this.equalTo = equalTo;
    }

    public static boolean canStart(Tokeniser t) {
        return NodeSumOp.canStart(t);
    }

    public static Node parse(Tokeniser t) {
        int startline = t.getLine(), startcolumn = t.getColumn();
        NodeExpr lhs = (NodeExpr)(NodeSumOp.parse(t));
        boolean great;
        boolean equalTo;

        Token tok = t.peek();
        if (tok == TokenSymbol.LPOINTY || tok == TokenSymbol.RPOINTY
            || tok == TokenSymbol.LTE || tok == TokenSymbol.GTE) {

            great = (tok == TokenSymbol.RPOINTY || tok == TokenSymbol.GTE);
            equalTo = (tok == TokenSymbol.LTE || tok == TokenSymbol.GTE);

            t.read();
            return setPos(new NodeRelationTestOp(lhs, (NodeExpr)(NodeExpr.parse(t)), great, equalTo), startline, startcolumn);
        }
        else return setPos(lhs, startline, startcolumn);
    }

    public Type getType(Module m, Type coerceType, Scope localScope) {
        Type lhsType = lhs.getType(m, Type.FLOAT, localScope);

        if (! lhsType.isNumeric()) {
            throw CheckerException.numberExpected(lhs, lhs.getType(m, Type.FLOAT, localScope));
        }
        if (!rhs.getType(m, Type.FLOAT, localScope).isNumeric()) {
            throw CheckerException.numberExpected(rhs, rhs.getType(m, Type.FLOAT, localScope));
        }

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
		if (great) {
			if (equalTo) {
				if ((ll instanceof NodeNumberLiteral || ll instanceof NodeCharacterLiteral) &&
				    (rl instanceof NodeNumberLiteral || rl instanceof NodeCharacterLiteral) ) {

					test = (ll.getFloatValue() >= rl.getFloatValue());
				}
				else throw CheckerException.incompatibleTypes(this, lhs.getType(m, Type.VOID, localScope), rhs.getType(m, Type.VOID, localScope));
			}
			else {
				if ((ll instanceof NodeNumberLiteral || ll instanceof NodeCharacterLiteral) &&
				    (rl instanceof NodeNumberLiteral || rl instanceof NodeCharacterLiteral) ) {

					test = (ll.getFloatValue() > rl.getFloatValue());
				}
				else throw CheckerException.incompatibleTypes(this, lhs.getType(m, Type.VOID, localScope), rhs.getType(m, Type.VOID, localScope));
			}
		}
		else {
			if (equalTo) {
				if ((ll instanceof NodeNumberLiteral || ll instanceof NodeCharacterLiteral) &&
				    (rl instanceof NodeNumberLiteral || rl instanceof NodeCharacterLiteral) ) {

					test = (ll.getFloatValue() <= rl.getFloatValue());
				}
				else throw CheckerException.incompatibleTypes(this, lhs.getType(m, Type.VOID, localScope), rhs.getType(m, Type.VOID, localScope));
			}
			else {
				if ((ll instanceof NodeNumberLiteral || ll instanceof NodeCharacterLiteral) &&
				    (rl instanceof NodeNumberLiteral || rl instanceof NodeCharacterLiteral) ) {

					test = (ll.getFloatValue() < rl.getFloatValue());
				}
				else throw CheckerException.incompatibleTypes(this, lhs.getType(m, Type.VOID, localScope), rhs.getType(m, Type.VOID, localScope));
			}
		}

		return new NodeBooleanLiteral(test);
	}

    public String toString() {
        return "NodeRelationTestOp(" + lhs + (great ? '>' : '<') + (equalTo ? "=" : "") + rhs + ")";
    }

    public void printSelf(IndentingWriter w) {
        w.println(getClassName() + " (" + (great ? '>' : '<') + (equalTo ? "=" : "") + ") [");
        w.indent();
        lhs.printSelf(w);
        rhs.printSelf(w);
        w.outdent();
        w.println("]");
    }
}
