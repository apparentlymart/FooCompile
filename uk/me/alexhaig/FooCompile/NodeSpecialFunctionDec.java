
package uk.me.alexhaig.FooCompile;

import java.util.Vector;

public class NodeSpecialFunctionDec extends NodeDeclaration {
    NodeParamDec params;
    NodeStmt code = null;
    boolean isDes = false;

    public NodeSpecialFunctionDec(boolean isDes, NodeModList mods, NodeParamDec params, NodeStmt code) {
        this.mods = mods;
        this.params = params;
        this.code = code;
        this.isDes = isDes;
    }

    public static boolean canStart(Tokeniser t) {
        return t.peek() == TokenKeyword.CONSTRUCTOR || t.peek() == TokenKeyword.DESTRUCTOR;
    }

    public static Node parse(Tokeniser t) {
        int startline = t.getLine(), startcolumn = t.getColumn();
        NodeModList mods = null;
        NodeParamDec params;
        NodeStmt code = null;
        boolean isDes = false;

        Token tok = t.peek();
        if (tok == TokenKeyword.CONSTRUCTOR) {
           isDes = false;
        } else if (tok == TokenKeyword.DESTRUCTOR) {
           isDes = true;
        } else throw new InternalErrorException("Invalid special function keyword: " + tok);
        t.read(); // eat keyword

        if (NodeModList.canStart(t)) mods = (NodeModList)(NodeModList.parse(t));

        if (NodeParamDec.canStart(t)) params = (NodeParamDec)(NodeParamDec.parse(t));
        else throw new ParserException(t, "Missing parameter list", "Expecting opening parenthesis but got "+t.peek());

        if (NodeStmt.canStart(t)) code = (NodeStmt)(NodeStmt.parse(t));
        else t.require(TokenSymbol.SEMICOLON);

        return setPos(new NodeSpecialFunctionDec(isDes, mods, params, code), startline, startcolumn);
    }

    public void check(Module m, Scope localScope) {
		params.check(m, localScope);
		code.check(m, m.getScope(this));
		if (mods != null) throw new CheckerException(this, "Invalid modifier", "The modifier " + mods.mods.toArray()[0] + " is not allowed on " + (isDes? "destructors" : "constructors"));

	}

    public String toString() {
        return "NodeSpecialFunctionDec(" + (isDes ? "destructor" : "constructor") + ")";
    }

    public void printSelf(IndentingWriter w) {
        w.println(getClassName() + " (" + (isDes ? "destructor" : "constructor") + ") [");
        w.indent();
        if (mods != null) mods.printSelf(w);
        if (params != null) params.printSelf(w);
        if (code != null) code.printSelf(w);
        w.outdent();
        w.println("]");
    }
}
