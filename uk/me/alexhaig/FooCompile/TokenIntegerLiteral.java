
package uk.me.alexhaig.FooCompile;

public class TokenIntegerLiteral extends TokenNumericLiteral{
    protected int val;

    public TokenIntegerLiteral(String val, boolean isHex) {
        this.val = Integer.parseInt(val, (isHex? 16:10));
    }

    public String toString() {
        return "TokenIntegerLiteral("+val+")";
    }

}
