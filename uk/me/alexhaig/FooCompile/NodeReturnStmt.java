
package uk.me.alexhaig.FooCompile;

public class NodeReturnStmt extends NodeStmt {
    NodeExpr expr;

    public NodeReturnStmt(NodeExpr expr) {
        this.expr = expr;
    }

    public static boolean canStart(Tokeniser t) {
        return t.peek() == TokenKeyword.RETURN;
    }

    public static Node parse(Tokeniser t) {
        int startline = t.getLine(), startcolumn = t.getColumn();
        t.require(TokenKeyword.RETURN);
        NodeExpr expr = null;
        if (NodeExpr.canStart(t)) {
            expr = ((NodeExpr)NodeExpr.parse(t));
	    }
        t.require(TokenSymbol.SEMICOLON);
        return setPos(new NodeReturnStmt(expr), startline, startcolumn);
    }

    public void check(Module m, Scope localScope) {
		// The type of the expression is checked in NodeFunction
		// rather than here, since NodeFunction knows what return
		// type it needs.
	}

    public void printSelf(IndentingWriter w) {
        w.println(getClassName() + " [");
        w.indent();
        expr.printSelf(w);
        w.outdent();
        w.println("]");
    }

    public String toString() {
        return "NodeReturnStmt( ... )";
    }

}
