
package uk.me.alexhaig.FooCompile;

import java.util.Vector;
import java.util.Iterator;

public class NodeSelectCaseBlock extends Node {
    boolean def;
    Vector exprs;
    NodeStmt code;

    public NodeSelectCaseBlock(NodeStmt code) {
        this.code = code;
        this.def = true;
        this.exprs = null;
    }
    public NodeSelectCaseBlock(Vector exprs, NodeStmt code) {
        this.code = code;
        this.def = false;
        this.exprs = exprs;
    }

    public static boolean canStart(Tokeniser t) {
        return (t.peek() == TokenKeyword.CASE ||
                t.peek() == TokenKeyword.DEFAULT);
    }

    public static Node parse(Tokeniser t) {
        int startline = t.getLine(), startcolumn = t.getColumn();
        Token tok = t.read();
        if (tok == TokenKeyword.CASE) {
            Vector exprs = new Vector(1);
            t.require(TokenSymbol.LPAREN);
            exprs.add(NodeExpr.parse(t));
            while (t.peek() == TokenSymbol.COMMA) {
                t.read();
                exprs.add(NodeExpr.parse(t));
            }
            t.require(TokenSymbol.RPAREN);
            NodeStmt code = (NodeStmt)(NodeStmt.parse(t));
            return setPos(new NodeSelectCaseBlock(exprs, code), startline, startcolumn);
        }
        else if (tok == TokenKeyword.DEFAULT) {
            NodeStmt code;
            code = (NodeStmt)(NodeStmt.parse(t));
            return setPos(new NodeSelectCaseBlock(code), startline, startcolumn);
        }
        else {
            throw new ParserException(t, "Invalid select case", "Expected 'default' or 'case', but found "+t.peek());
        }
    }

    public void check(Module m, Scope localScope) {
		code.check(m, localScope);

		NodeExpr expr;

		if (! def) {
			Iterator i = exprs.iterator();
			while (i.hasNext()) {
				expr = (NodeExpr)i.next();
				expr.getType(m, Type.VOID, localScope);
			}
		}
	}

    public void printSelf(IndentingWriter w) {
        w.println(getClassName() + (def ? " (default)" : "" ) + " [");
        w.indent();
        if (! def) {
            Iterator i = exprs.iterator();
            while (i.hasNext()) {
                Node n = (Node)(i.next());
                n.printSelf(w);
            }
        }
        code.printSelf(w);
        w.outdent();
        w.println("]");
    }

    public String toString() {
        return "NodeSelectStmt( ... )";
    }
}
