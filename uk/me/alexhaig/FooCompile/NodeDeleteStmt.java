
package uk.me.alexhaig.FooCompile;

public class NodeDeleteStmt extends NodeStmt {
    NodeVariableRef vr;

    public NodeDeleteStmt(NodeVariableRef vr) {
        this.vr = vr;
    }

    public static boolean canStart(Tokeniser t) {
        return t.peek() == TokenKeyword.DELETE;
    }

    public static Node parse(Tokeniser t) {
        int startline = t.getLine(), startcolumn = t.getColumn();
        t.require(TokenKeyword.DELETE);
        NodeVariableRef vr = (NodeVariableRef)(NodeVariableRef.parse(t));
        t.require(TokenSymbol.SEMICOLON);
        return setPos(new NodeDeleteStmt(vr), startline, startcolumn);
    }

    public void check(Module m, Scope localScope) {
		Type vrType = vr.getType(m, Type.VOID, localScope);

		if (vrType.isIntrinsic) {
			if (vrType.arraymod == 0) {
				throw new CheckerException(this, "Incompatible types", "Cannot delete intrinisic variables");
			}
		}
		else {
			Symbol s = localScope.findSymbol(m, vrType.name);
			if (s == null || !(s instanceof Class)) throw new CheckerException(this, "Variable Expected", "Cannot delete " + s + ", variable required");
		}
	}

    public void printSelf(IndentingWriter w) {
        w.println(getClassName() + " [");
        w.indent();
        vr.printSelf(w);
        w.outdent();
        w.println("]");
    }

    public String toString() {
        return "NodeDeleteStmt( ... )";
    }

}
