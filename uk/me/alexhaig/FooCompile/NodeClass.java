
package uk.me.alexhaig.FooCompile;

import java.util.Vector;
import java.util.Iterator;

public class NodeClass extends Node {
    String name;
    Vector members;
    NodeQRef extd;

    public NodeClass(String name, Vector members, NodeQRef extd) {
        this.name = name;
        this.members = members;
        this.extd = extd;
    }

    public void addMember(NodeDeclaration n) {
        members.add(n);
    }

    public static boolean canStart(Tokeniser t) {
        return t.peek() == TokenKeyword.CLASS;
    }

    public static Node parse(Tokeniser t) {
        int startline = t.getLine(), startcolumn = t.getColumn();
        t.read();
        Vector members = new Vector();
        String name = t.requireIdent().name;
        NodeQRef extd = null;

        //TODO: When a class can belong to a package, must canonicalise this reference
        if (t.peek() == TokenKeyword.EXTENDS) {
			t.read();
            extd = (NodeQRef)(NodeQRef.parse(t));
        }

        t.require(TokenSymbol.LBRACE);

        Token tok = t.peek();
        while (tok != TokenSymbol.RBRACE && tok != null) {
            if (NodeDeclaration.canStart(t)) members.add(NodeDeclaration.parse(t));
            else throw new ParserException(t, "Invalid class member", "Cannot start a class member with "+tok.toString());

            tok = t.peek();
        }

        t.require(TokenSymbol.RBRACE);
        NodeClass ret = new NodeClass(name, members, extd);
        return setPos(ret, startline, startcolumn);
    }

    public void check(Module m, Scope localScope) {
		Iterator i = members.iterator();

		while (i.hasNext()) {
			NodeDeclaration nodeDec = (NodeDeclaration)i.next();
			nodeDec.check(m, m.getScope(this));
			if (nodeDec instanceof NodeVariableDec) {
				if (((NodeVariableDec)nodeDec).expr != null && !((NodeVariableDec)nodeDec).expr.isConstant(m, m.getScope(this))) {
					throw new CheckerException(nodeDec, "Constant expression required", "Global variables must be initialised with a constant expression");
				}
			}
        }
    }

    public String toString() {
        return "NodeClass("+name+")";
    }

    public void printSelf(IndentingWriter w) {
        w.println(getClassName() + " ("+name+") [");
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
