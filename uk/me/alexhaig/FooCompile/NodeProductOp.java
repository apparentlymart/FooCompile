
package uk.me.alexhaig.FooCompile;

public class NodeProductOp extends NodeOperation {
    NodeExpr lhs;
    NodeExpr rhs;

    char mode;

    public static final char MULT = '*';
    public static final char DIV = '/';
    public static final char MOD = '%';


    public NodeProductOp(NodeExpr lhs, NodeExpr rhs, char mode) {
        this.lhs = lhs;
        this.rhs = rhs;
        this.mode = mode;
    }

    public static boolean canStart(Tokeniser t) {
        return NodeBitShiftOp.canStart(t);
    }

    public static Node parse(Tokeniser t) {
        int startline = t.getLine(), startcolumn = t.getColumn();
        NodeExpr lhs = (NodeExpr)(NodeBitShiftOp.parse(t));
        char mode;

        Token tok = t.peek();

        if (tok == TokenSymbol.MULTIPLY || tok == TokenSymbol.DIVIDE || tok == TokenSymbol.MODULUS) {
            mode = (tok == TokenSymbol.DIVIDE ? DIV :(tok == TokenSymbol.MODULUS ? MOD : MULT));
            t.read();
            return setPos(new NodeProductOp(lhs, (NodeExpr)(NodeExpr.parse(t)), mode), startline, startcolumn);
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

		if (mode == '*') return new NodeIntegerLiteral(ll.getIntegerValue() * rl.getIntegerValue());
		else if (mode == '/') return new NodeIntegerLiteral(ll.getIntegerValue() / rl.getIntegerValue());
		else if (mode == '%') return new NodeIntegerLiteral(ll.getIntegerValue() % rl.getIntegerValue());
		else throw new InternalErrorException("Product operation mode undefined");

	}

    public String toString() {
        return "NodeProductOp(" + lhs + " " + mode + " " + rhs + ")";
    }

    public void printSelf(IndentingWriter w) {
        w.println(getClassName() + " (" + mode + ") [");
        w.indent();
        lhs.printSelf(w);
        rhs.printSelf(w);
        w.outdent();
        w.println("]");
    }
}
