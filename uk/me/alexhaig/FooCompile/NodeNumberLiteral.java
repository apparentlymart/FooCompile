
package uk.me.alexhaig.FooCompile;

public class NodeNumberLiteral extends NodeLiteral {

    public static boolean canStart(Tokeniser t) {
        return (NodeIntegerLiteral.canStart(t) || NodeFloatLiteral.canStart(t));
    }

    public static Node parse(Tokeniser t) {
        int startline = t.getLine(), startcolumn = t.getColumn();
        if (NodeIntegerLiteral.canStart(t)) return setPos(NodeIntegerLiteral.parse(t), startline, startcolumn);
        if (NodeFloatLiteral.canStart(t)) return setPos(NodeFloatLiteral.parse(t), startline, startcolumn);
        throw new InternalErrorException("Unable to parse numeric literal");
    }

    public String toString() {
        return "NodeNumberLiteral()";
    }

}
