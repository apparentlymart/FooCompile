
package uk.me.alexhaig.FooCompile;

public class NodeDeclaration extends Node {
    String name;
    NodeModList mods;

    public static boolean canStart(Tokeniser t) {
        return (NodeVariableDec.canStart(t) ||
                NodeFunctionDec.canStart(t) ||
                NodeSpecialFunctionDec.canStart(t) ||
                NodeDelegateDec.canStart(t) ||
                NodePropertyDec.canStart(t));
    }

    public static Node parse(Tokeniser t) {
        int startline = t.getLine(), startcolumn = t.getColumn();
        if (NodeVariableDec.canStart(t)) return setPos(NodeVariableDec.parse(t), startline, startcolumn);
        if (NodeFunctionDec.canStart(t)) return setPos(NodeFunctionDec.parse(t), startline, startcolumn);
        if (NodeSpecialFunctionDec.canStart(t)) return setPos(NodeSpecialFunctionDec.parse(t), startline, startcolumn);
        if (NodeDelegateDec.canStart(t)) return setPos(NodeDelegateDec.parse(t), startline, startcolumn);
        if (NodePropertyDec.canStart(t)) return setPos(NodePropertyDec.parse(t), startline, startcolumn);

        return setPos(null, startline, startcolumn);
    }

    public String toString() {
        return "NodeDeclaration()";
    }

}
