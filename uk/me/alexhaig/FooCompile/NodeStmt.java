
package uk.me.alexhaig.FooCompile;

public class NodeStmt extends Node {

    public static boolean canStart(Tokeniser t) {
        return NodeCodeBlock.canStart(t)
               || NodeExprStmt.canStart(t)
               || NodeIfStmt.canStart(t)
               || NodeSelectStmt.canStart(t)
               || NodeVariableDecStmt.canStart(t)
               || NodeForStmt.canStart(t)
               || NodeDeleteStmt.canStart(t)
               || NodeReturnStmt.canStart(t)
               || NodeDelegateAssignStmt.canStart(t)
               || NodeWhileStmt.canStart(t);
    }

    public static Node parse(Tokeniser t) {
        int startline = t.getLine(), startcolumn = t.getColumn();
        if (NodeIfStmt.canStart(t)) return setPos(NodeIfStmt.parse(t), startline, startcolumn);
        if (NodeWhileStmt.canStart(t)) return setPos(NodeWhileStmt.parse(t), startline, startcolumn);
        if (NodeSelectStmt.canStart(t)) return setPos(NodeSelectStmt.parse(t), startline, startcolumn);
        if (NodeVariableDecStmt.canStart(t)) return setPos(NodeVariableDecStmt.parse(t), startline, startcolumn);
        if (NodeForStmt.canStart(t)) return setPos(NodeForStmt.parse(t), startline, startcolumn);
        if (NodeDeleteStmt.canStart(t)) return setPos(NodeDeleteStmt.parse(t), startline, startcolumn);
        if (NodeReturnStmt.canStart(t)) return setPos(NodeReturnStmt.parse(t), startline, startcolumn);
        if (NodeDelegateAssignStmt.canStart(t)) return setPos(NodeDelegateAssignStmt.parse(t), startline, startcolumn);
        if (NodeCodeBlock.canStart(t)) return setPos(NodeCodeBlock.parse(t), startline, startcolumn);
        if (NodeExprStmt.canStart(t)) return setPos(NodeExprStmt.parse(t), startline, startcolumn);
        throw new InternalErrorException("Can't parse statement");
    }

    public void printSelf(IndentingWriter w) {
        w.println(getClassName() + " (printSelf not implemented);");
    }

    public String toString() {
        return "NodeStmt()";
    }

}
