
package uk.me.alexhaig.FooCompile;

public class NodeVariableDecStmt extends NodeStmt {
    NodeVariableDec vd;

    public NodeVariableDecStmt(NodeVariableDec vd) {
        this.vd = vd;
    }

    public static boolean canStart(Tokeniser t) {
        return NodeVariableDec.canStart(t);
    }

    public static Node parse(Tokeniser t) {
        int startline = t.getLine(), startcolumn = t.getColumn();
        return setPos(new NodeVariableDecStmt((NodeVariableDec)(NodeVariableDec.parse(t))), startline, startcolumn);
    }

    public void check(Module m, Scope localScope) {
        vd.check(m, localScope);
    }

    public void printSelf(IndentingWriter w) {
        w.println(getClassName() + " [");
        w.indent();
        vd.printSelf(w);
        w.outdent();
        w.println("]");
    }

    public String toString() {
        return "NodeVariableDecStmt( ... )";
    }

}
