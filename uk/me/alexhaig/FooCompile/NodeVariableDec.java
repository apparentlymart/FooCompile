
package uk.me.alexhaig.FooCompile;

import java.util.Vector;

public class NodeVariableDec extends NodeDeclaration {
    NodeTypeSpec type;
    NodeExpr expr = null;

    public NodeVariableDec(NodeTypeSpec type, String name, NodeModList mods) {
        this.type = type;
        this.name = name;
        this.mods = mods;
    }

    public NodeVariableDec(NodeTypeSpec type, String name, NodeModList mods, NodeExpr expr) {
        this.type = type;
        this.name = name;
        this.mods = mods;
        this.expr = expr;
    }

    public static boolean canStart(Tokeniser t) {
        return t.peek() == TokenKeyword.VAR;
    }

    public static Node parse(Tokeniser t) {
        int startline = t.getLine(), startcolumn = t.getColumn();
        t.require(TokenKeyword.VAR);

        NodeTypeSpec type;
        String name;
        NodeModList mods = null;
        NodeExpr expr;

        if (NodeModList.canStart(t))  mods = (NodeModList)(NodeModList.parse(t));
        if (NodeTypeSpec.canStart(t)) type = (NodeTypeSpec)(NodeTypeSpec.parse(t));
        else throw new ParserException(t, "Invalid type specification","Expecting type, but got " + t.peek());

        name = t.requireIdent().name;

        if (t.peek() == TokenSymbol.ASSIGN) {
            t.read();
            if (NodeExpr.canStart(t)) expr = ((NodeExpr)(NodeExpr.parse(t)));
            else throw new ParserException(t,"Expression required","Cannot initialise variable with "+ t.peek());
            t.require(TokenSymbol.SEMICOLON);
            return setPos(new NodeVariableDec(type, name, mods, expr), startline, startcolumn);
        }

        t.require(TokenSymbol.SEMICOLON);

        return setPos(new NodeVariableDec(type, name, mods), startline, startcolumn);
    }

    public void check(Module m, Scope localScope) {
        type.check(m, localScope);
        if (mods != null) mods.check(m, localScope);
        if (expr != null) {
            Type exprtype = expr.getType(m, type.type, localScope);
            if (! (exprtype.canBe(m, type.type))) throw CheckerException.incompatibleTypes(expr, type.type, exprtype);
        }
    }

    public String toString() {
        return "NodeVariableDec()";
    }

    public void printSelf(IndentingWriter w) {
        w.println(getClassName() + " (" + name + ") [");
        w.indent();
        type.printSelf(w);
        if (mods != null) mods.printSelf(w);
        if (expr != null) expr.printSelf(w);
        w.outdent();
        w.println("]");
    }

}
