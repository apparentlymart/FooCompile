
package uk.me.alexhaig.FooCompile;

public class NodeTerm extends NodeExpr {

    public static boolean canStart(Tokeniser t) {
        return (t.peek() == TokenSymbol.LPAREN
               || NodeLiteral.canStart(t)
               || NodeVariableRef.canStart(t)
               || NodeNewOp.canStart(t));
    }

    public static Node parse(Tokeniser t) {
        int startline = t.getLine(), startcolumn = t.getColumn();
        if (t.peek() == TokenSymbol.LPAREN) {
            t.require(TokenSymbol.LPAREN);

            Node ret;
            if (NodeExpr.canStart(t)) ret = NodeExpr.parse(t);
            else throw new ParserException(t, "Invalid expression", "Expecting start of parenthetic expression, but got "+t.peek());

            t.require(TokenSymbol.RPAREN);
            return setPos(ret, startline, startcolumn);
        }
        if (NodeNewOp.canStart(t))     return setPos(NodeNewOp.parse(t), startline, startcolumn);
        if (NodeLiteral.canStart(t))     return setPos(NodeLiteral.parse(t), startline, startcolumn);
        if (NodeVariableRef.canStart(t)) return setPos(NodeVariableRef.parse(t), startline, startcolumn);

        throw new ParserException(t, "Invalid term", "Cannot start a term with "+t.peek());
    }

    public String toString() {
        return "NodeTerm()";
    }
}
