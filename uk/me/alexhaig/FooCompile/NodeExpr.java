
package uk.me.alexhaig.FooCompile;

public class NodeExpr extends Node {

    public NodeExpr(){
    }

    public static boolean canStart(Tokeniser t) {
        return NodeOperation.canStart(t);
    }

    public static Node parse(Tokeniser t) {
        int startline = t.getLine(), startcolumn = t.getColumn();
        System.gc();
        if (NodeOperation.canStart(t)) return setPos(NodeOperation.parse(t), startline, startcolumn);
        throw new ParserException (t, "Invalid expression", "Cannot start an expression with " + t.peek());
    }

    public Type getType(Module m, Type coerceType, Scope localScope) {
        throw new InternalErrorException("Cannot call getType on this node");
    }

    public boolean isConstant(Module m, Scope localScope) {
		return false;
	}

    public NodeLiteral evaluateConstant(Module m, Scope localScope) {
		throw new InternalErrorException("evaluateConstant not written for this expression");
	}

    public String toString() {
        return "NodeExpr()";
    }

}
