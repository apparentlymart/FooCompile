
package uk.me.alexhaig.FooCompile;

public class NodeVariableRef extends NodeTerm {
    NodeQRef QRef;

    public NodeVariableRef(NodeQRef QRef) {
        this.QRef = QRef;
    }

    public static boolean canStart(Tokeniser t) {
        return NodeQRef.canStart(t);
    }

    public static Node parse(Tokeniser t) { // may return a function call or array deref
        int startline = t.getLine(), startcolumn = t.getColumn();
        NodeQRef qr = (NodeQRef)(NodeQRef.parse(t));

        if (t.peek() == TokenSymbol.LPAREN) {
            return (NodeFunctionCall)(NodeFunctionCall.continueParsing(t, qr));
        }
        else if (t.peek() == TokenSymbol.LBRACKET){
            return setPos((NodeArrayDeref)NodeArrayDeref.continueParsing(t, (NodeVariableRef)setPos(new NodeVariableRef(qr), startline, startcolumn)), startline, startcolumn);
        }
        else {
            return setPos(new NodeVariableRef(qr), startline, startcolumn);
        }
    }

    public Type getType(Module m, Type coerceType, Scope localScope, Class startClass) {
		Symbol s = localScope.findSymbol(m, QRef, startClass);
		Variable v = null;
		Invokable p = null;
		if (s == null) throw CheckerException.symbolNotFound(QRef);
		if (s instanceof Variable) {
			v = (Variable)s;
			return v.type;
		}
		else if (s instanceof Invokable && ((Invokable)s).kind == Invokable.PROPERTY) {
			p = (Invokable)s;
			if (p.ownerPackage != null) m.registerImport(p);
			return p.returnType;
		}
		else throw CheckerException.wrongSymbolKind(QRef, "variable or property", s);
	}

	public Type getType(Module m, Type coerceType, Scope localScope) {
		return getType(m, coerceType, localScope, null);
	}

    public String toString() {
        return "NodeVariableRef("+QRef+")";
    }

    public void printSelf(IndentingWriter w) {
        w.println(getClassName() + " [");
        w.indent();
        QRef.printSelf(w);
        w.outdent();
        w.println("]");
    }
}
