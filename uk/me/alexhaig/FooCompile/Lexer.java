
package uk.me.alexhaig.FooCompile;

public class Lexer {
    Tokeniser t;
    Token peeked;

    public Lexer(Tokeniser t) {
        this.t = t;
    }

    public Module parse() {
        Token tok = t.peek();

        Module m = new Module();

        while (tok != null) {
            if (NodeClass.canStart(t)) m.addClass((NodeClass)NodeClass.parse(t));
            else if (NodeFunctionDec.canStart(t)) m.addFunction((NodeFunctionDec)NodeFunctionDec.parse(t));
            else {
                throw new ParserException(t, "Invalid top-level declaration", "Can't start a top-level declaration with "+tok.toString());
            }

            tok = t.peek();
        }

        return m;
    }
}
