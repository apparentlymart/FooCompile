
package uk.me.alexhaig.FooCompile;

import java.util.Vector;
import java.util.Iterator;

public class NodeSelectStmt extends NodeStmt {
    NodeExpr expr;
    Vector cases;

    public NodeSelectStmt(NodeExpr expr, Vector cases) {
        this.expr = expr;
        this.cases = cases;
    }

    public static boolean canStart(Tokeniser t) {
        return (t.peek() == TokenKeyword.SELECT);
    }

    public static Node parse(Tokeniser t) {
        int startline = t.getLine(), startcolumn = t.getColumn();
        t.require(TokenKeyword.SELECT);
        t.require(TokenSymbol.LPAREN);
        NodeExpr expr = (NodeExpr)(NodeExpr.parse(t));
        t.require(TokenSymbol.RPAREN);
        t.require(TokenSymbol.LBRACE);
        Vector cases = new Vector(2);
        while (NodeSelectCaseBlock.canStart(t)) {
            cases.add(NodeSelectCaseBlock.parse(t));
        }
        t.require(TokenSymbol.RBRACE);
        return setPos(new NodeSelectStmt(expr, cases), startline, startcolumn);
    }

    public void check(Module m, Scope localScope) {

		Type slctExprType = expr.getType(m, Type.VOID, localScope);

		if (!slctExprType.isIntrinsic) throw CheckerException.intrinsicTypeExpected(this, slctExprType);

		NodeSelectCaseBlock caseBlock = null;
		NodeExpr caseBlockExpr = null;
		Type caseBlockExprType = null;

		Iterator is = cases.iterator();
		while (is.hasNext()) {
			caseBlock = ((NodeSelectCaseBlock)is.next());
			if (!caseBlock.def) {
				Iterator ic = caseBlock.exprs.iterator();
				while (ic.hasNext()) {
					caseBlockExpr = ((NodeExpr)ic.next());
					caseBlockExprType = caseBlockExpr.getType(m, slctExprType, localScope);
					if (caseBlockExprType != slctExprType) CheckerException.typeMismatch(this, slctExprType, caseBlockExprType);
					if (!caseBlockExpr.isConstant(m, localScope)) throw CheckerException.constantTypeExpected(this, caseBlockExpr);

				}
			}
			caseBlock.check(m, localScope);
		}
	}

    public void printSelf(IndentingWriter w) {
        w.println(getClassName() + " [");
        w.indent();
        expr.printSelf(w);
        Iterator i = cases.iterator();
        while (i.hasNext()) {
            Node n = (Node)(i.next());
            n.printSelf(w);
        }
        w.outdent();
        w.println("]");
    }

    public String toString() {
        return "NodeSelectStmt( ... )";
    }

}
