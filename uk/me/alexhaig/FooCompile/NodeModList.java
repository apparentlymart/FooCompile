
package uk.me.alexhaig.FooCompile;

import java.util.HashSet;
import java.util.Iterator;

public class NodeModList extends Node {

    HashSet mods;

    public NodeModList(HashSet mods){
        this.mods = mods;
    }

    public static boolean canStart(Tokeniser t) {
        return t.peek() == TokenSymbol.LPOINTY;
    }

    public static Node parse(Tokeniser t) {
        int startline = t.getLine(), startcolumn = t.getColumn();
        t.require(TokenSymbol.LPOINTY);
        HashSet mods = new HashSet();
        mods.add(requireModifier(t));

        while (t.peek() == TokenSymbol.COMMA) {
            t.require(TokenSymbol.COMMA);
            mods.add(requireModifier(t));
        }

        t.require(TokenSymbol.RPOINTY);
        return setPos(new NodeModList(mods), startline, startcolumn);
    }

    public static TokenKeyword requireModifier(Tokeniser t) {
        Token tok = t.read();
        if (tok == TokenKeyword.STATIC || tok == TokenKeyword.CONST) {
            return (TokenKeyword)tok;
        } else {
            throw new ParserException(t, "Invalid modifier", "Expected modifier but got " + tok);
        }
    }

    public void check(Module m, Scope localScope) {

	}

    public String toString() {
        return "NodeModList()";
    }

    public void printSelf(IndentingWriter w) {
        StringBuffer sb = new StringBuffer(7);
        Iterator i = mods.iterator();
        while (i.hasNext()) {
            sb.append(((TokenKeyword)i.next()).name);
            if (i.hasNext()) sb.append(",");
        }

        w.println(getClassName() + " (" + sb + ");");
    }
}
