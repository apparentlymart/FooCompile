
package uk.me.alexhaig.FooCompile;

import java.util.Vector;
import java.util.Iterator;

public class NodeQRef extends Node {
    public Vector parts;
    String name;

    public NodeQRef() {
        parts = new Vector(3);
    }
    public NodeQRef(String s) {
        parts = new Vector(1);
        parts.add(s);
        name = s;
    }
    public NodeQRef(Vector parts, String name) {
		this.parts = parts;
		this.name = name;
	}
    public NodeQRef(NodeQRef parentRef, String newRef) {
		Iterator i = parentRef.parts.iterator();
		this.parts = new Vector(parentRef.parts.size());

		while (i.hasNext()) {
			this.parts.add(i.next());
		}
		this.parts.add(newRef);

		this.name = parentRef.name + "." + newRef;
	}
    protected NodeQRef(int special) {
        parts = new Vector(3);
        if (special == 1) {
            this.name = "foo.lang.String";
            this.parts.add("foo");
            this.parts.add("lang");
            this.parts.add("String");
        }
    }

    public static boolean canStart(Tokeniser t) {
        Token ret = t.peek();
        return (ret instanceof TokenIdent);
    }

    public static Node parse(Tokeniser t) {
        int startline = t.getLine(), startcolumn = t.getColumn();
        NodeQRef ret = new NodeQRef();
        StringBuffer name = new StringBuffer(10);
        TokenIdent i = t.requireIdent();
        ret.parts.add(i.name);
        name.append(i.name);

        while (t.peek() == TokenSymbol.DOT) {
            name.append(".");
            t.require(TokenSymbol.DOT);
            i = t.requireIdent();
            ret.parts.add(i.name);
            name.append(i.name);
        }

        ret.name = name.toString();
        return setPos(ret, startline, startcolumn);
    }

    public void check(Module m, Scope s) {
		if (parts == null || parts.size() != 0) throw new InternalErrorException ("Invalid QRef found when checked");
		//This should not get called, this is to fulfill the node stub
	}

    public String getURef() {
        return ((String)parts.elementAt(parts.size() - 1));
    }

    public String getName() {
        if (name == null) {
			StringBuffer sb = new StringBuffer(parts.size());
			Iterator i = parts.iterator();
			sb.append(i.next());
			while(i.hasNext()) {
				sb.append('.');
				sb.append(i.next());
			}
			name = sb.toString();
		}
		return name;
    }

    public NodeQRef partial(int start, int end) {
		Vector v = new Vector(end - start);
		StringBuffer name = new StringBuffer(10);
		boolean notfirst = false;
        for (int i = start; i <= end; i++) {
            if (notfirst) {
				name.append(".");
			}
			else {
				notfirst = true;
			}
			name.append(this.parts.elementAt(i).toString());
			v.add(this.parts.elementAt(i));
		}
		return new NodeQRef(v, name.toString());
	}

    public String toString() {
        return "NodeQRef("+name+")";
    }

    public void printSelf(IndentingWriter w) {
        w.println(getClassName()  + " (" + name + ");");
    }

}
