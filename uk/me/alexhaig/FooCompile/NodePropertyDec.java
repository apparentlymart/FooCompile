
package uk.me.alexhaig.FooCompile;

public class NodePropertyDec extends NodeDeclaration {
    NodeTypeSpec type;
    NodeStmt getStmt;
    NodeStmt setStmt;

    public NodePropertyDec(NodeModList mods, String name, NodeTypeSpec type, NodeStmt getStmt, NodeStmt setStmt) {
        this.mods = mods;
        this.name = name;
        this.type = type;
        this.getStmt = getStmt;
        this.setStmt = setStmt;
    }

    public static boolean canStart(Tokeniser t) {
        return t.peek() == TokenKeyword.PROPERTY;
    }

    public static Node parse(Tokeniser t) {
        int startline = t.getLine(), startcolumn = t.getColumn();
        NodeModList mods = null;
        String name;
        NodeTypeSpec type;
        NodeStmt getStmt = null;
        NodeStmt setStmt = null;


        t.require(TokenKeyword.PROPERTY);

        if (NodeModList.canStart(t)) mods = (NodeModList)(NodeModList.parse(t));
        type = (NodeTypeSpec)(NodeTypeSpec.parse(t));

        name = ((TokenIdent)t.requireIdent()).name;

        t.require(TokenSymbol.LBRACE);

        Token tok = t.peek();


        if(tok == TokenKeyword.GET || tok == TokenKeyword.SET) {
            t.read();
            if (tok == TokenKeyword.GET) {
                getStmt = ((NodeStmt)NodeStmt.parse(t));
            } else {
                setStmt = ((NodeStmt)NodeStmt.parse(t));
            }
        } else throw new ParserException(t,"Invalid property block","Cannot start a property block with " + tok);;

        tok = t.peek();
        if (tok != TokenSymbol.RBRACE) {
			if(tok == TokenKeyword.GET || tok == TokenKeyword.SET) {
				t.read();
				if (tok == TokenKeyword.GET) {
					if (getStmt == null) getStmt = ((NodeStmt)NodeStmt.parse(t));
					else throw new ParserException(t, "Multiple get blocks declared", "A property may only have a single get block");
				} else {
					if (setStmt == null) setStmt = ((NodeStmt)NodeStmt.parse(t));
					else throw new ParserException(t, "Multiple set blocks declared", "A property may only have a single set block");
				}
			} else throw new ParserException(t,"Invalid property block","Cannot start a property block with " + tok);;
		}

        t.require(TokenSymbol.RBRACE);

        return setPos(new NodePropertyDec(mods, name, type, getStmt, setStmt), startline, startcolumn);
    }

    public void check(Module m, Scope localScope) {
	    if (getStmt != null) getStmt.check(m, localScope);
    	if (setStmt != null) setStmt.check(m, localScope);
		type.check(m, localScope);
		if (mods != null) mods.check(m, localScope);
	}

    public String toString() {
        return "NodePropertyDec(" + name + ")";
    }

    public void printSelf(IndentingWriter w) {
        w.println(getClassName() + " (" + type.type + " " + name + ") [");
        w.indent();
        if (mods != null) mods.printSelf(w);
        if (getStmt != null) getStmt.printSelf(w);
        if (setStmt != null) setStmt.printSelf(w);
        w.outdent();
        w.println("]");
    }

}