
package uk.me.alexhaig.FooCompile;

public abstract class Token {
    public static boolean canStart(Scanner s) { return false; }
    public static Token parse(Scanner s) { return null; }

    public String toString() {
        return "Token()";
    }

}
