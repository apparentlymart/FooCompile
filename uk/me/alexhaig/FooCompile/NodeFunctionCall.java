
package uk.me.alexhaig.FooCompile;

public class NodeFunctionCall extends NodeTerm {
    NodeQRef qref;
    NodeParamIn params;

    public NodeFunctionCall(NodeQRef qref, NodeParamIn params) {
        this.qref = qref;
        this.params = params;
    }

    public static boolean canStart(Tokeniser t) {
        return NodeVariableRef.canStart(t);
    }

    public static Node parse(Tokeniser t) {
        int startline = t.getLine(), startcolumn = t.getColumn();
        NodeQRef qr = (NodeQRef)(NodeQRef.parse(t));

        return setPos(continueParsing(t, qr), startline, startcolumn);
    }

    public static Node continueParsing(Tokeniser t, NodeQRef name) {
        NodeParamIn par;

        if (NodeParamIn.canStart(t)) {
            par = (NodeParamIn)(NodeParamIn.parse(t));
        } else {
            throw new ParserException(t, "Invalid function call specification", "Invalid function call specification, used invalid parameters.");
        }

        return new NodeFunctionCall(name, par);
    }

    public Type getType(Module m, Type coerceType, Scope localScope, Class startClass) {
		Symbol s = localScope.findSymbol(m, qref, startClass);
		Invokable f = null;
		if (s == null) throw CheckerException.symbolNotFound(qref);
		if (s instanceof Invokable && (((Invokable)s).kind == Invokable.FUNCTION || ((Invokable)s).kind == Invokable.DELEGATE)) {
			f = (Invokable)s;
		}
		else throw CheckerException.wrongSymbolKind(qref, "delegate or function call", s);

		f.checkParameters(m, params, localScope);

		if (f.returnType == null) throw new InternalErrorException("Invokable must hava a return type");

		if (f.ownerPackage != m.defPackage) m.registerImport(f);

		return f.returnType;
	}

    public Type getType(Module m, Type coerceType, Scope localScope) {
        return getType(m,coerceType,localScope,null);
    }

    public boolean isConstant(Module m, Scope localScope) {
		return false;
	}

    public NodeLiteral evaluateConstant(Module m, Scope localScope) {
		throw new InternalErrorException("An array derefence cannot be static");
	}

    public String toString() {
        return "NodeFunctionCall("+qref+")";
    }
    public void printSelf(IndentingWriter w) {
        w.println(getClassName() + " [");
        w.indent();
        qref.printSelf(w);
        params.printSelf(w);
        w.outdent();
        w.println("]");
    }
}
