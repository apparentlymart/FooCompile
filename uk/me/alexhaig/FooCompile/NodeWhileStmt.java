
package uk.me.alexhaig.FooCompile;

public class NodeWhileStmt extends NodeStmt {
    NodeStmt code;
    NodeExpr expr;

    public NodeWhileStmt(NodeStmt code, NodeExpr expr) {
        this.code = code;
        this.expr = expr;
    }

    public static boolean canStart(Tokeniser t) {
        return (t.peek() == TokenKeyword.WHILE);
    }

    public static Node parse(Tokeniser t) {
        int startline = t.getLine(), startcolumn = t.getColumn();
        t.require(TokenKeyword.WHILE);
        t.require(TokenSymbol.LPAREN);
        NodeExpr expr = (NodeExpr)(NodeExpr.parse(t));
        t.require(TokenSymbol.RPAREN);
        NodeStmt code = (NodeStmt)(NodeStmt.parse(t));
        return setPos(new NodeWhileStmt(code, expr), startline, startcolumn);
    }

    public void check(Module m, Scope localScope) {
		code.check(m, m.getScope(this));
		Type gotType = expr.getType(m, Type.BOOL, localScope);
		if (gotType != Type.BOOL) throw CheckerException.typeMismatch(this, Type.BOOL, gotType);
 	}

    public void printSelf(IndentingWriter w) {
        w.println(getClassName() + " [");
        w.indent();
        expr.printSelf(w);
        code.printSelf(w);
        w.outdent();
        w.println("]");
    }

    public String toString() {
        return "NodeWhileStmt( ... )";
    }

}
