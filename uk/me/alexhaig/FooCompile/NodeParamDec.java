
package uk.me.alexhaig.FooCompile;

import java.util.Vector;
import java.util.Iterator;

public class NodeParamDec extends Node {
    Vector members = new Vector();

    public NodeParamDec(Vector members) {
        this.members = members;
    }

    public static boolean canStart(Tokeniser t) {
        return t.peek() == TokenSymbol.LPAREN;
    }

    public static Node parse(Tokeniser t) {
        int startline = t.getLine(), startcolumn = t.getColumn();
        Vector members = new Vector();
        t.require(TokenSymbol.LPAREN);

        if (NodeTypedNameDec.canStart(t)) members.add(NodeTypedNameDec.parse(t));

        Token tok = t.peek();
        while(tok == TokenSymbol.COMMA) {
            t.read();
            if (NodeTypedNameDec.canStart(t)) members.add(NodeTypedNameDec.parse(t));
            tok = t.peek();
        }

        t.require(TokenSymbol.RPAREN);
        return setPos(new NodeParamDec(members), startline, startcolumn);
    }

    public String toString() {
        return "NodeParamDec(...)";
    }

    public void check(Module m, Scope localScope) {
        Checker.checkList(members, m, localScope);
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
