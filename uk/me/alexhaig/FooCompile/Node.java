
package uk.me.alexhaig.FooCompile;

public abstract class Node {
    int line, column;

    public static boolean canStart(Tokeniser t) { return false; }

    public static Node parse(Tokeniser t) { return null; }

    public void check(Module m, Scope localScope) {
        throw new InternalErrorException("Can't check "+getClassName()+" -- no check implementation!");
    }

    protected static Node setPos(Node n, int line, int column) {
        n.line = line;
        n.column = column;
        return n;
    }

    public String getClassName() {
        return this.getClass().getName().substring(30)+"<"+line+","+column+">";
    }

    public String toString() {
        return "Node()";
    }

    public void printSelf(IndentingWriter w) {
        w.println(getClassName() + " (printSelf not implemented);");
    }

}
