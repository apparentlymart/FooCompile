
package uk.me.alexhaig.FooCompile;

public class TokenFloatLiteral extends TokenNumericLiteral {
    protected double val;

    public TokenFloatLiteral(String val) {
        this.val = Double.parseDouble(val);
    }
    public String toString() {
        return "TokenFloatLiteral("+val+")";
    }

}
