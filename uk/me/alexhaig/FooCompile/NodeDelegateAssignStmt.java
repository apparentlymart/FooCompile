
package uk.me.alexhaig.FooCompile;

import java.util.Vector;
import java.util.Iterator;

public class NodeDelegateAssignStmt extends NodeStmt {
    NodeQRef deleg;
    NodeQRef func;

    public NodeDelegateAssignStmt(NodeQRef deleg, NodeQRef func) {
        this.deleg = deleg;
        this.func = func;
    }

    public static boolean canStart(Tokeniser t) {
        return t.peek() == TokenKeyword.TIE;
    }

    public static Node parse(Tokeniser t) {
        int startline = t.getLine(), startcolumn = t.getColumn();
        t.require(TokenKeyword.TIE);
        NodeQRef deleg = (NodeQRef)(NodeQRef.parse(t));
        t.require(TokenSymbol.DELEGATEASSIGN);
        NodeQRef func = (NodeQRef)(NodeQRef.parse(t));

        t.require(TokenSymbol.SEMICOLON);
        return setPos(new NodeDelegateAssignStmt(deleg, func), startline, startcolumn);
    }

    public void check(Module m, Scope localScope) {
		if (deleg.parts.size() == 0 || deleg == null) throw new InternalErrorException("Invalid delegate reference");
		if (func.parts.size() == 0 || func == null) throw new InternalErrorException("Invalid function reference");

        Invokable d = null;
        try {
  		    d = (Invokable)(localScope.findSymbol(m, deleg));
		}
		catch (ClassCastException e) {
			throw CheckerException.wrongSymbolKind(deleg, "delegate", d);
		}

		if (d == null) throw CheckerException.symbolNotFound(deleg);
		//if (d.owner.mods.mods.contains(TokenKeyword.STATIC) || d.owner.mods.mods.contains(TokenKeyword.CONST)) throw CheckerException.symbolNotFound(deleg);

        Invokable f = null;
        try {
		    f = (Invokable)(localScope.findSymbol(m, func));
		}
		catch (ClassCastException e) {
			throw CheckerException.wrongSymbolKind(deleg, "function", d);
		}

		if (f == null) throw CheckerException.symbolNotFound(deleg);
		//if (f.owner.mods.mods.contains(TokenKeyword.STATIC) || f.owner.mods.mods.contains(TokenKeyword.CONST)) throw CheckerException.symbolNotFound(func);

		if (d.returnType.canBe(m, f.returnType) == false) throw CheckerException.typeMismatch(this, d.returnType, f.returnType);

		int pos = 0;
		Variable v = null;
		Iterator i = d.params.iterator();
		Type fType = null, dType = null;
		while (i.hasNext()) {
			v = (Variable) i.next();
			fType = ((Variable)f.params.elementAt(pos)).type;
			dType = v.type;
			if (dType.canBe(m, fType) == false) throw CheckerException.incompatibleTypes(this, dType, fType);
			pos++;
		}
	}

    public void printSelf(IndentingWriter w) {
        w.println(getClassName() + " [");
        w.indent();
        deleg.printSelf(w);
        func.printSelf(w);
        w.outdent();
        w.println("]");
    }

    public String toString() {
        return "NodeDelegateAssignStmt("+deleg.name+" =~ "+func.name+")";
    }

}
