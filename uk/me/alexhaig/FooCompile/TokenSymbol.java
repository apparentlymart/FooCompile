
package uk.me.alexhaig.FooCompile;

public class TokenSymbol extends Token {
    protected String punct;

    public static final TokenSymbol LBRACKET        = new TokenSymbol("[");
    public static final TokenSymbol RBRACKET        = new TokenSymbol("]");
    public static final TokenSymbol LPAREN          = new TokenSymbol("(");
    public static final TokenSymbol RPAREN          = new TokenSymbol(")");
    public static final TokenSymbol LBRACE          = new TokenSymbol("{");
    public static final TokenSymbol RBRACE          = new TokenSymbol("}");
    public static final TokenSymbol LPOINTY         = new TokenSymbol("<");
    public static final TokenSymbol RPOINTY         = new TokenSymbol(">");
    public static final TokenSymbol LTE             = new TokenSymbol("<=");
    public static final TokenSymbol GTE             = new TokenSymbol(">=");
    public static final TokenSymbol SEMICOLON       = new TokenSymbol(";");
    public static final TokenSymbol ASSIGN          = new TokenSymbol("=");
    public static final TokenSymbol DELEGATEASSIGN  = new TokenSymbol("=~");
    public static final TokenSymbol NOTEQUAL        = new TokenSymbol("!=");
    public static final TokenSymbol EQUAL           = new TokenSymbol("==");
    public static final TokenSymbol ARRAYMARK       = new TokenSymbol("[]");
    public static final TokenSymbol COLON           = new TokenSymbol(":");
    public static final TokenSymbol QMARK           = new TokenSymbol("?");
    public static final TokenSymbol DOT             = new TokenSymbol(".");
    public static final TokenSymbol COMMA           = new TokenSymbol(",");
    public static final TokenSymbol QUOTE           = new TokenSymbol("\"");
    public static final TokenSymbol SQUOTE          = new TokenSymbol("'");
    public static final TokenSymbol PLUS            = new TokenSymbol("+");
    public static final TokenSymbol MINUS           = new TokenSymbol("-");
    public static final TokenSymbol MULTIPLY        = new TokenSymbol("*");
    public static final TokenSymbol POWER           = new TokenSymbol("**");
    public static final TokenSymbol DIVIDE          = new TokenSymbol("/");
    public static final TokenSymbol MODULUS         = new TokenSymbol("%");
    public static final TokenSymbol LOGAND          = new TokenSymbol("&&");
    public static final TokenSymbol LOGOR           = new TokenSymbol("||");
    public static final TokenSymbol LOGNOT          = new TokenSymbol("!");
    public static final TokenSymbol BITAND          = new TokenSymbol("&");
    public static final TokenSymbol BITOR           = new TokenSymbol("|");
    public static final TokenSymbol BITNOT          = new TokenSymbol("~");
    public static final TokenSymbol SHIFTLEFT       = new TokenSymbol("<<");
    public static final TokenSymbol SHIFTRIGHT      = new TokenSymbol(">>");
    public static final TokenSymbol INCREMENT       = new TokenSymbol("++");
    public static final TokenSymbol DECREMENT       = new TokenSymbol("--");


    private TokenSymbol(String punct) {
        this.punct = punct;
    }

    public static boolean canStart(Scanner s) {
        char c = s.peek();

        return (s.peek() == '?'  ||
                s.peek() == '}'  ||
                s.peek() == '~'  ||
                s.peek() == '!'  ||
                s.peek() == '"'  ||
                s.peek() == '%'  ||
                s.peek() == '&'  ||
                s.peek() == '\'' ||
                s.peek() == '('  ||
                s.peek() == ')'  ||
                s.peek() == '*'  ||
                s.peek() == '+'  ||
                s.peek() == ','  ||
                s.peek() == '-'  ||
                s.peek() == '.'  ||
                s.peek() == '/'  ||
                s.peek() == ':'  ||
                s.peek() == ';'  ||
                s.peek() == '<'  ||
                s.peek() == '['  ||
                s.peek() == '{'  ||
                s.peek() == '='  ||
                s.peek() == '|'  ||
                s.peek() == '>'  ||
                s.peek() == ']');
    }

    public static Token parse(Scanner s) {
        char fc = s.read();

        if (fc == '?') {
            return QMARK;
        }

        if (fc == '}') {
            return RBRACE;
        }

        if (fc == '~') {
            return BITNOT;
        }

        if (fc == '!') {
            if (s.peek() == '=') {
                s.read();
                return NOTEQUAL;
            }

            return LOGNOT;
        }

        if (fc == '"') {
            return QUOTE;
        }

        if (fc == '%') {
            return MODULUS;
        }

        if (fc == '&') {
            if (s.peek() == '&') {
                s.read();
                return LOGAND;
            }

            return BITAND;
        }

        if (fc == '\'') {
            return SQUOTE;
        }

        if (fc == '(') {
            return LPAREN;
        }

        if (fc == ')') {
            return RPAREN;
        }

        if (fc == '*') {
            if (s.peek() == '*') {
                s.read();
                return POWER;
            }

            return MULTIPLY;
        }

        if (fc == '+') {
            if (s.peek() == '+') {
                s.read();
                return INCREMENT;
            }
            return PLUS;
        }

        if (fc == ',') {
            return COMMA;
        }

        if (fc == '-') {
            if (s.peek() == '-') {
                s.read();
                return DECREMENT;
            }
            return MINUS;
        }

        if (fc == '.') {
            return DOT;
        }

        if (fc == '/') {
            return DIVIDE;
        }

        if (fc == ':') {
            return COLON;
        }

        if (fc == ';') {
            return SEMICOLON;
        }

        if (fc == '<') {
            if (s.peek() == '<') {
                s.read();
                return SHIFTLEFT;
            }
            if (s.peek() == '=') {
                s.read();
                return LTE;
            }

            return LPOINTY;
        }

        if (fc == '[') {
            if (s.peek() == ']') {
                s.read();
                return ARRAYMARK;
            }

            return LBRACKET;
        }

        if (fc == '{') {
            return LBRACE;
        }

        if (fc == '=') {
            if (s.peek() == '~') {
                s.read();
                return DELEGATEASSIGN;
            }

            if (s.peek() == '=') {
                s.read();
                return EQUAL;
            }

            return ASSIGN;
        }

        if (fc == '|') {
            if (s.peek() == '|') {
                s.read();
                return LOGOR;
            }

            return BITOR;
        }

        if (fc == '>') {
            if (s.peek() == '>') {
                s.read();
                return SHIFTRIGHT;
            }
            if (s.peek() == '=') {
                s.read();
                return GTE;
            }

            return RPOINTY;
        }

        if (fc == ']') {
            return RBRACKET;
        }


        return null;
    }

    public String toString() {
        return "TokenSymbol("+punct+")";
    }
}