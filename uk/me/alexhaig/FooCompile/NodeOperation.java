
package uk.me.alexhaig.FooCompile;

public class NodeOperation extends NodeExpr {

    public static boolean canStart(Tokeniser t) {
        return (NodeAssignOp.canStart(t));
    }

    public static Node parse(Tokeniser t) {
        int startline = t.getLine(), startcolumn = t.getColumn();
        return setPos((NodeAssignOp.parse(t)), startline, startcolumn);
    }

    public String toString() {
        return "NodeOperation()";
    }
}
