
package uk.me.alexhaig.FooCompile;

import java.util.Iterator;

public class NodeTypedNameDec extends Node {
    NodeTypeSpec type;
    String name;

    public NodeTypedNameDec(NodeTypeSpec type, String name) {
        this.type = type;
        this.name = name;
    }

    public static boolean canStart(Tokeniser t) {
        return NodeTypeSpec.canStart(t);
    }

    public static Node parse(Tokeniser t) {
        int startline = t.getLine(), startcolumn = t.getColumn();
        return setPos(new NodeTypedNameDec((NodeTypeSpec)(NodeTypeSpec.parse(t)), t.requireIdent().name), startline, startcolumn);
    }

    public void check(Module m, Scope localScope) {
        type.check(m, localScope);
    }

    public String toString() {
        return "NodeTypedNameDec(" + type.type + ", " + name + ")";
    }

    public void printSelf(IndentingWriter w) {
        w.println(getClassName() + " (" + type.type + ", " + name + ");");
    }
}
