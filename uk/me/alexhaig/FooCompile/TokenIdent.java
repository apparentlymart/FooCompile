
package uk.me.alexhaig.FooCompile;

public class TokenIdent extends Token {
    protected String name;

    private TokenIdent(String name) {
        this.name = name;
    }

    public static boolean canStart(Scanner s) {
        char c = s.peek();
        return ((c >= 'a' && c <= 'z') || (c >= 'A' && c <= 'Z'));
    }

    public static Token parse(Scanner s) {
        StringBuffer sb = new StringBuffer(10);

        char l;

        do {
            sb.append(s.read());
            l = s.peek();
        } while ((l >= 'a' && l <= 'z') || (l >= 'A' && l <= 'Z') || (l >= '0' && l <= '9'));

        Token ret = new TokenIdent(sb.toString());
        return TokenKeyword.identOrKeyword((TokenIdent)ret);
    }

    public String toString() {
        return ("TokenIdent(" + name + ")");
    }
}
