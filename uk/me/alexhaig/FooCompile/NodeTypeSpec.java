
package uk.me.alexhaig.FooCompile;

public class NodeTypeSpec extends Node {
    Type type;

    public NodeTypeSpec(NodeQRef name) {
        this.type = new Type(name);
    }
    public NodeTypeSpec(NodeQRef name, int arraymod) {
        this.type = new Type(name, arraymod);
    }
    public NodeTypeSpec(TokenKeyword intrinsicType) {
        if (intrinsicType == TokenKeyword.VOID) this.type = Type.VOID;
        else if (intrinsicType == TokenKeyword.INT) this.type = Type.INT;
        else if (intrinsicType == TokenKeyword.FLOAT) this.type = Type.FLOAT;
        else if (intrinsicType == TokenKeyword.BOOL) this.type = Type.BOOL;
        else if (intrinsicType == TokenKeyword.CHAR) this.type = Type.CHAR;
        else throw new InternalErrorException("Type specifier must provide a keyword that represents an intrinsic type");
    }
    public NodeTypeSpec(TokenKeyword intrinsicType, int size, int arraymod) {
        this.type = new Type(intrinsicType, size, arraymod);
    }

    public static boolean canStart(Tokeniser t) {
        Token peek = t.peek();
        return (peek == TokenKeyword.VOID  ||
                peek == TokenKeyword.INT   ||
                peek == TokenKeyword.FLOAT ||
                peek == TokenKeyword.BOOL  ||
                peek == TokenKeyword.CHAR  ||
                NodeQRef.canStart(t));
    }

    public static Node parse(Tokeniser t) {
        int startline = t.getLine(), startcolumn = t.getColumn();
        int arraymod = 0;
        if (NodeQRef.canStart(t)) {
            NodeQRef name = (NodeQRef)(NodeQRef.parse(t));
            if (t.peek() == TokenSymbol.LPOINTY) {
                throw new ParserException(t, "Can't resize objects", "Size modifier on a class type is not allowed");
            }

			while (t.peek() == TokenSymbol.ARRAYMARK) {
				t.read();
				arraymod++;
			}

            return setPos(new NodeTypeSpec(name, arraymod), startline, startcolumn);
        } else {
            TokenKeyword itype;
            if (t.peek() instanceof TokenKeyword) itype = (TokenKeyword)t.read();
            else throw new ParserException(t, "Invalid type", "Expected type name but got "+t.peek());

            if (! (itype == TokenKeyword.VOID ||
                   itype == TokenKeyword.INT ||
                   itype == TokenKeyword.FLOAT ||
                   itype == TokenKeyword.CHAR ||
                   itype == TokenKeyword.BOOL)) {
                throw new ParserException(t, "Invalid type", "Expected type name but got "+itype);
            }

            int size = -1;
            if (t.peek() == TokenSymbol.LPOINTY) {
                t.read();
                Token sizetok = t.read();
                if (!(sizetok instanceof TokenIntegerLiteral))throw new ParserException(t, "Invalid type modifier", "Expected size of variable but got "+sizetok);
                size = ((TokenIntegerLiteral)sizetok).val;
                if (size < 1) {
                    throw new ParserException(t, "Invalid size", "Expecting positive integer but got "+size);
                }

                if (! (sizetok instanceof TokenIntegerLiteral)) {
                    throw new ParserException(t, "Invalid type modifier", "Expected size of variable but got "+sizetok);
                }
                t.require(TokenSymbol.RPOINTY);
            }

			while (t.peek() == TokenSymbol.ARRAYMARK) {
				t.read();
				arraymod++;
			}

            return setPos(new NodeTypeSpec(itype, size, arraymod), startline, startcolumn);
        }
    }

    public void check(Module m, Scope localScope) {
        if (type.isIntrinsic) {
            if (type.name != null) throw new InternalErrorException("Intrinsic types must not have a name");
            if (type.intrinsicType == null) throw new InternalErrorException("Intrinsic types must have a type keyword");

            if (type.intrinsicType == TokenKeyword.VOID && type.size != -1)
                throw new CheckerException(this, "Invalid size", "Void type may not have a size");
        }
        else {
            if (type.name == null) throw new InternalErrorException("Non-intrinsic types must have a name");
            if (type.intrinsicType != null) throw new InternalErrorException("Non-intrinsic types must not have a type keyword");
        }
        if (!(m.isValidType(type))) throw new CheckerException(this, "Invalid type", "The type " + type + " is not valid" );
    }

    public String toString() {
        return "NodeTypeSpec(" + type + ")";
    }

    public void printSelf(IndentingWriter w) {
        w.println(getClassName() + " (" + type + ");");
    }
}
