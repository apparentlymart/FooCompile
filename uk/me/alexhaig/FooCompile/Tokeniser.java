
package uk.me.alexhaig.FooCompile;

public class Tokeniser {
    Scanner s;
    Token peeked;

    private int line;
    private int column;


    public Tokeniser(Scanner s) {
        this.s = s;
    }

    public Token read() {
        Token ret = (peeked == null ? peek() : peeked);
        peeked = null;
        return ret;
    }

    public Token require(Token req) {
        Token ret = read();
        if (ret != null) {
            if (ret != req) throw new ParserException(this, "Syntax error","Expected " + req.toString() + " but got " + ret.toString());
        } else {
            throw new ParserException(this, "Syntax error","Expected " + req.toString() + " but encountered end of file");
        }
        return ret;
    }

    public TokenIdent requireIdent() {
        Token ret = read();
        if (ret != null) {
            if (!(ret instanceof TokenIdent)) throw new ParserException(this, "Syntax error","Expected identifier but got " + ret.toString());
        } else {
            throw new ParserException(this, "Syntax error","Expected identifier but encountered end of file");
        }
        return (TokenIdent)ret;
    }

    public Token peek() {
        if (peeked != null) return peeked;
        skipWhiteSpace(s);

        line = s.getLine();
        column = s.getColumn();

        if (s.atEOF()) return null;


        if (TokenIdent.canStart(s)) return peeked = TokenIdent.parse(s);
        if (TokenNumericLiteral.canStart(s)) return peeked = TokenNumericLiteral.parse(s);
        if (TokenStringLiteral.canStart(s)) return peeked = TokenStringLiteral.parse(s);
        if (TokenSymbol.canStart(s)) return peeked = TokenSymbol.parse(s);

        // No tokens can start
        throw new ParserException(this, "Invalid character encountered", "Encountered: "+s.peek());
    }

    protected void skipWhiteSpace(Scanner s) {
        while (s.peek() == ' ' || s.peek() == '\n' || s.peek() == '\r' || s.peek() == '\t' || s.peek() == '#' || s.peek() == '@') {
            if (s.peek() == '#' || s.peek() == '@') {
                while (s.read() != '\n' && s.read() != '\r' && s.read() != 0) {}
            } else {
                s.read();
            }
        }
    }

    protected int getLine() {
        return line;
    }
    protected int getColumn() {
        return column;
    }
}
