
package uk.me.alexhaig.FooCompile;

public class NodeForStmt extends NodeStmt {
    Node init;
    NodeExpr cond;
    NodeExpr iter;
    NodeStmt code;


    public NodeForStmt(NodeStmt code, Node init, NodeExpr cond, NodeExpr iter) {
        this.init = init;
        this.cond = cond;
        this.iter = iter;
        this.code = code;
    }

    public static boolean canStart(Tokeniser t) {
        return (t.peek() == TokenKeyword.FOR);
    }

    public static Node parse(Tokeniser t) {
        int startline = t.getLine(), startcolumn = t.getColumn();
        t.require(TokenKeyword.FOR);
        t.require(TokenSymbol.LPAREN);

        Node init;

        // VariableDec includes semicolon
        if (NodeVariableDec.canStart(t)) init = NodeVariableDec.parse(t);
        else if (NodeExpr.canStart(t)) {
            init = NodeExpr.parse(t);
            t.require(TokenSymbol.SEMICOLON);
        }
        else throw new ParserException (t, "Invalid loop initialiser", "Must initialise for loop with expression or variable declaration");

        NodeExpr cond = (NodeExpr)(NodeExpr.parse(t));
        t.require(TokenSymbol.SEMICOLON);
        NodeExpr iter = (NodeExpr)(NodeExpr.parse(t));
        t.require(TokenSymbol.RPAREN);

        NodeStmt code = (NodeStmt)(NodeStmt.parse(t));

        return setPos(new NodeForStmt(code, init, cond, iter), startline, startcolumn);
    }

    public void check(Module m, Scope localScope) {
        if (init instanceof NodeVariableDec) {
            ((NodeVariableDec)init).check(m, localScope);

        } else {
            ((NodeExpr)init).getType(m, Type.VOID, localScope);
        }

        cond.getType(m, Type.BOOL, m.getScope(this));
        iter.getType(m, Type.VOID, m.getScope(this));
        code.check(m, m.getScope(this));
    }

    public void printSelf(IndentingWriter w) {
        w.println(getClassName() + " [");
        w.indent();
        init.printSelf(w);
        cond.printSelf(w);
        iter.printSelf(w);
        code.printSelf(w);
        w.outdent();
        w.println("]");
    }

    public String toString() {
        return "NodeForStmt( ... )";
    }

}
