
package uk.me.alexhaig.FooCompile;

import java.util.Vector;

public class NodeFunctionDec extends NodeDeclaration {
    NodeTypeSpec type;
    NodeParamDec params;
    NodeStmt code = null;

    public NodeFunctionDec(NodeTypeSpec type, String name, NodeModList mods, NodeParamDec params, NodeStmt code) {
        this.type = type;
        this.name = name;
        this.mods = mods;
        this.params = params;
        this.code = code;
    }

    public static boolean canStart(Tokeniser t) {
        return t.peek() == TokenKeyword.FUNCTION;
    }

    public static Node parse(Tokeniser t) {
        int startline = t.getLine(), startcolumn = t.getColumn();
        t.require(TokenKeyword.FUNCTION);

        NodeTypeSpec type;
        String name;
        NodeModList mods = null;
        NodeParamDec params;

        if (NodeModList.canStart(t)) mods = (NodeModList)(NodeModList.parse(t));

        if (NodeTypeSpec.canStart(t))
            type = (NodeTypeSpec)(NodeTypeSpec.parse(t));
        else
            throw new ParserException(t, "Invalid type specification","Expecting type, but got " + t.peek());
        name = t.requireIdent().name;

        if (NodeParamDec.canStart(t)) params = (NodeParamDec)(NodeParamDec.parse(t));
        else throw new ParserException(t, "Missing parameter list", "Expecting opening parenthesis but got "+t.peek());

        NodeStmt code = null;
        if (NodeStmt.canStart(t)) code = (NodeStmt)(NodeStmt.parse(t));
        else t.require(TokenSymbol.SEMICOLON);

        return setPos(new NodeFunctionDec(type, name, mods, params, code), startline, startcolumn);
    }

    public void check(Module m, Scope localScope) {
        type.check(m, localScope);
        if (params != null) params.check(m, localScope);
        if (code != null) code.check(m, m.getScope(this));
        if (mods != null && mods.mods.contains(TokenKeyword.CONST)) {
            throw new CheckerException(mods, "Invalid modifier", "Cannot declare a function as constant");
        }

        //TODO: Find out if there is a reason for static top-level functions

        // Look through the function for any return statements,
        // ensuring they are of the correct type.
        Checker.checkReturnStmts(m, code, type.type, m.getScope(this));
    }

    public String toString() {
        return "NodeFunctionDec(" + name + ")";
    }

    public void printSelf(IndentingWriter w) {
        w.println(getClassName() + " (" + name + ") [");
        w.indent();
        type.printSelf(w);
        if (mods != null) mods.printSelf(w);
        if (params != null) params.printSelf(w);
        if (code != null) code.printSelf(w);
        w.outdent();
        w.println("]");
    }
}
