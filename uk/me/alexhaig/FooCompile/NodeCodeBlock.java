
package uk.me.alexhaig.FooCompile;

import java.util.Vector;
import java.util.Iterator;

public class NodeCodeBlock extends NodeStmt{
    Vector stmts;

    public NodeCodeBlock(Vector stmts) {
        this.stmts = stmts;
    }

    public static boolean canStart(Tokeniser t) {
        return t.peek() == TokenSymbol.LBRACE;
    }

    public static Node parse(Tokeniser t) {
        int startline = t.getLine(), startcolumn = t.getColumn();
        t.require(TokenSymbol.LBRACE);

        Vector stmts = new Vector(4);

        Token tok = t.peek();
        while (tok != TokenSymbol.RBRACE) {
            System.gc();
            if (NodeStmt.canStart(t)) stmts.add(NodeStmt.parse(t));
            else throw new ParserException(t, "Statement expected", "Cannot start a statement with "+tok);

            tok = t.peek();
        }

        t.require(TokenSymbol.RBRACE);
        return setPos(new NodeCodeBlock(stmts), startline, startcolumn);
    }

    public void check(Module m, Scope localScope) {
        Checker.checkList(stmts, m, m.getScope(this));
    }

    public void printSelf(IndentingWriter w) {
        w.println(getClassName() + " [");
        w.indent();
        Iterator i = stmts.iterator();
        while (i.hasNext()) {
            Node n = (Node)(i.next());
            n.printSelf(w);
        }
        w.outdent();
        w.println("]");
    }

    public String toString() {
        return "NodeCodeBlock( ... )";
    }

}
