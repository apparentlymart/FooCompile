
package uk.me.alexhaig.FooCompile;

public class TokenStringLiteral extends Token {
    protected String st;
    protected boolean isChar;

    private TokenStringLiteral(String st, boolean isChar) {
        this.st = st;
        this.isChar = isChar;
    }

    public static boolean canStart(Scanner s) {
        char c = s.peek();
        return (c == '"' || c == '\'');
    }

    public static Token parse(Scanner s) {
            StringBuffer sb = new StringBuffer(10);

            char l;
            char m;

            boolean isChar = false;

            if (s.read() == '\'') {
                isChar = true;
            }

            int app = 0;
            do {
                m = s.read();

                if (isChar && app == 1) throw new ParserException(s, "Multi-character character","A character literal may only contain a single character");

                if (m =='\\') {
                    l = s.read();
                    switch (l) {
                        case 'n':
                            sb.append('\n');
                            break;
                        case 't':
                            sb.append('\t');
                            break;
                        case 'r':
                            sb.append('\r');
                            break;
                        case '\\':
                            sb.append('\\');
                            break;
                        case '"':
                            sb.append('"');
                            break;
                        case '\'':
                            sb.append('\'');
                            break;
                        default:
                            throw new ParserException(s, "Unrecognised escape code","Unrecognised escape code: " + l);
                    }
                    app++;
                }
                else {
                    sb.append(m);
                    app++;
                }

                l = s.peek();
            } while (isChar ? l != '\'' : l != '"');


            s.read();

            Token ret = new TokenStringLiteral(sb.toString(), isChar);
            return ret;
    }

    public String toString() {
        return "TokenStringLiteral("+st+")";
    }

}
