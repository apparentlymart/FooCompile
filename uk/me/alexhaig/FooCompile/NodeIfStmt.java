
package uk.me.alexhaig.FooCompile;

public class NodeIfStmt extends NodeStmt {
    NodeStmt ifcode;
    NodeStmt elsecode;
    NodeExpr expr;

    public NodeIfStmt(NodeStmt ifcode, NodeStmt elsecode, NodeExpr expr) {
        this.ifcode = ifcode;
        this.elsecode = elsecode;
        this.expr = expr;
    }

    public static boolean canStart(Tokeniser t) {
        return (t.peek() == TokenKeyword.IF);
    }

    public static Node parse(Tokeniser t) {
        int startline = t.getLine(), startcolumn = t.getColumn();
        t.require(TokenKeyword.IF);
        t.require(TokenSymbol.LPAREN);
        NodeExpr expr = (NodeExpr)(NodeExpr.parse(t));
        t.require(TokenSymbol.RPAREN);
        NodeStmt ifcode = (NodeStmt)(NodeStmt.parse(t));
        NodeStmt elsecode = null;
        if (t.peek() == TokenKeyword.ELSE) {
            t.require(TokenKeyword.ELSE);
            elsecode = (NodeStmt)(NodeStmt.parse(t));
        }

        return setPos(new NodeIfStmt(ifcode, elsecode, expr), startline, startcolumn);
    }

    public void check(Module m, Scope localScope) {
		ifcode.check(m, m.getScope(this));
		if (elsecode != null) elsecode.check(m, m.getScope(this));
		Type gotType = expr.getType(m, Type.BOOL, m.getScope(this));
		if (gotType != Type.BOOL) throw CheckerException.typeMismatch(this, Type.BOOL, gotType);
	}

    public void printSelf(IndentingWriter w) {
        w.println(getClassName() + " [");
        w.indent();
        expr.printSelf(w);
        ifcode.printSelf(w);
        if (elsecode != null) elsecode.printSelf(w);
        w.outdent();
        w.println("]");
    }

    public String toString() {
        return "NodeIfStmt( ... )";
    }

}
