
package uk.me.alexhaig.FooCompile;

import java.util.Vector;
import java.util.Iterator;

public class NodeParamIn extends Node {
    Vector members;

    public NodeParamIn(Vector members) {
        this.members = members;
    }

    public static boolean canStart(Tokeniser t) {
        return t.peek() == TokenSymbol.LPAREN;
    }

    public static Node parse(Tokeniser t) {
        int startline = t.getLine(), startcolumn = t.getColumn();
        Vector members = new Vector();
        t.require(TokenSymbol.LPAREN);

        if (NodeExpr.canStart(t)) members.add(NodeExpr.parse(t));

        Token tok = t.peek();
        while(tok == TokenSymbol.COMMA) {
            t.read();
            if (NodeExpr.canStart(t)) members.add(NodeExpr.parse(t));
            tok = t.peek();
        }

        t.require(TokenSymbol.RPAREN);
        return setPos(new NodeParamIn(members), startline, startcolumn);
    }

    public void check(Module m, Scope localScope) {
		//This should be checked by the caller using Invokable's checkParameters()
	}

    public String toString() {
        return "NodeParamIn(...)";
    }

    public void printSelf(IndentingWriter w) {
        w.println(getClassName() + " [");
        w.indent();
        Iterator i = members.iterator();
        while (i.hasNext()) {
            Node n = (Node)(i.next());
            n.printSelf(w);
        }
        w.outdent();
        w.println("]");
    }

}
