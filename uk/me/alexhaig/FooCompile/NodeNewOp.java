package uk.me.alexhaig.FooCompile;

public class NodeNewOp extends NodeTerm {
    NodeQRef name;
    NodeParamIn params;

    public NodeNewOp(NodeQRef name, NodeParamIn params) {
        this.name = name;
        this.params = params;
    }

    public static boolean canStart(Tokeniser t) {
        Token tok = t.peek();
        return tok == TokenKeyword.NEW;
    }

    public static Node parse(Tokeniser t) {
        int startline = t.getLine(), startcolumn = t.getColumn();
        t.require(TokenKeyword.NEW);

        if (NodeQRef.canStart(t)) {
            NodeQRef qr = (NodeQRef)(NodeQRef.parse(t));
            if (NodeParamIn.canStart(t)) {
                return setPos(new NodeNewOp(qr, (NodeParamIn)(NodeParamIn.parse(t))), startline, startcolumn);
            } else {
                throw new ParserException(t, "Expecting parameter list", "Expecting constructor arguments but got "+t.peek());
            }
        } else {
            throw new ParserException(t, "Expecting class name", "Expecting class name but got "+t.peek());
        }
    }

    public Type getType(Module m, Type coerceType, Scope localScope) {
        //TODO: write this when there is a function to canonicalise a class QRef
        //TODO: m.registerImport when out of the default package
        return coerceType;
    }

    public boolean isConstant(Module m, Scope localScope) {
		return false;
	}

    public NodeLiteral evaluateConstant(Module m, Scope localScope) {
		throw new InternalErrorException("New creates objects. A constant cannot be evaluated");
	}

    public void printSelf(IndentingWriter w) {
        w.println(getClassName() + " [");
        w.indent();
        name.printSelf(w);
        params.printSelf(w);
        w.outdent();
        w.println("]");
    }
}
