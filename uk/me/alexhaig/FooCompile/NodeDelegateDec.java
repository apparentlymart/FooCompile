
package uk.me.alexhaig.FooCompile;

import java.util.Vector;

public class NodeDelegateDec extends NodeDeclaration {
    NodeTypeSpec type;
    NodeParamDec params;

    public NodeDelegateDec(NodeTypeSpec type, String name, NodeModList mods, NodeParamDec params) {
        this.type = type;
        this.name = name;
        this.mods = mods;
        this.params = params;
    }

    public static boolean canStart(Tokeniser t) {
        return t.peek() == TokenKeyword.DELEGATE;
    }

    public static Node parse(Tokeniser t) {
        int startline = t.getLine(), startcolumn = t.getColumn();
        t.require(TokenKeyword.DELEGATE);

        NodeTypeSpec type;
        String name;
        NodeModList mods = null;
        NodeParamDec params;

        if (NodeModList.canStart(t))  mods = (NodeModList)(NodeModList.parse(t));
        if (NodeTypeSpec.canStart(t)) type = (NodeTypeSpec)(NodeTypeSpec.parse(t));
        else throw new ParserException(t, "Invalid type specification","Expecting type, but got " + t.peek());

        name = t.requireIdent().name;

        params = (NodeParamDec)(NodeParamDec.parse(t));

        t.require(TokenSymbol.SEMICOLON);

        return setPos(new NodeDelegateDec(type, name, mods, params), startline, startcolumn);
    }

    public void check(Module m, Scope localScope) {
		type.check(m, localScope);
		params.check(m, localScope);
		if (mods != null) mods.check(m, localScope);
	}

    public String toString() {
        return "NodeDelegateDec()";
    }

    public void printSelf(IndentingWriter w) {
        w.println(getClassName() + " (" + name + ") [");
        w.indent();
        type.printSelf(w);
        if (mods != null) mods.printSelf(w);
        params.printSelf(w);
        w.outdent();
        w.println("]");
    }

}
