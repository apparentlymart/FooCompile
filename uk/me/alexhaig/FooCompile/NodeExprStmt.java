
package uk.me.alexhaig.FooCompile;

import java.util.Vector;

public class NodeExprStmt extends NodeStmt {
    NodeExpr expr;

    public NodeExprStmt(NodeExpr expr) {
        this.expr = expr;
    }

    public static boolean canStart(Tokeniser t) {
        return NodeExpr.canStart(t);
    }

    public static Node parse(Tokeniser t) {
        int startline = t.getLine(), startcolumn = t.getColumn();

        // NodeExpr.parse will return the topmost expression
        // after making the expression parse tree.
        NodeExpr expr = null;
        try {
            expr = (NodeExpr)(NodeExpr.parse(t));
        }
        catch (ClassCastException e) {
            throw new InternalErrorException("Can't start an expression statement here");
        }

        // Eat the terminating semicolon
        t.require(TokenSymbol.SEMICOLON);
        return setPos(new NodeExprStmt(expr), startline, startcolumn);
    }

    public void check(Module m, Scope localScope) {
    	expr.getType(m, Type.VOID, localScope);
	}

    public void printSelf(IndentingWriter w) {
        w.println(getClassName() + " [");
        w.indent();
        expr.printSelf(w);
        w.outdent();
        w.println("]");
    }

    public String toString() {
        return "NodeExprStmt( ... )";
    }

}
