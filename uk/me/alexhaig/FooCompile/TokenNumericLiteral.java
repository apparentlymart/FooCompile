
package uk.me.alexhaig.FooCompile;

public abstract class TokenNumericLiteral extends Token{


    public static boolean canStart(Scanner s) {
        int i = s.peek();
        return (i >= '0' && i <= '9');
    }


    public static Token parse(Scanner s) {

        StringBuffer sb = new StringBuffer(10);

        boolean isFloat = false;
        boolean isHex = false;

        char l;

        if (s.peek() == '0') {
            sb.append(s.read());
            if (s.peek() == 'x'){
                isHex = true;
                s.read();
            }
        }

        l = s.peek();

        while ((l >= '0' && l <= '9' )  // Allow digits 0-9
             || (isHex ? (l >= 'a' && l <= 'f') || (l >= 'A' && l <= 'F') : false) // If hex, allow a-f and A-F
             || l == '.') // Allow dots, as multiple dots are trapped below
        {
            sb.append(s.read());
            if (l == '.' && !isFloat) isFloat = true;
            else if (l == '.' && isFloat) throw new ParserException(s, "Multiple decimal points",
                                                                       "A float literal may have only a single decimal point");
            l = s.peek();
        }

        try {
            if (isFloat) {
                return new TokenFloatLiteral(sb.toString());
            } else {
                return new TokenIntegerLiteral(sb.toString(), isHex);
            }
        }
        catch (NumberFormatException e) {
            throw new ParserException(s, "Invalid numeric literal", "Can't parse '"+sb+"' as a number");
        }
    }
}
