
package uk.me.alexhaig.FooCompile;

public class TokenKeyword extends Token {
    String name;

    public static final TokenKeyword INT            = new TokenKeyword("int");
    public static final TokenKeyword FLOAT          = new TokenKeyword("float");
    public static final TokenKeyword BOOL           = new TokenKeyword("bool");
    public static final TokenKeyword VOID           = new TokenKeyword("void");
    public static final TokenKeyword CHAR           = new TokenKeyword("char");
    public static final TokenKeyword IF             = new TokenKeyword("if");
    public static final TokenKeyword ELSE           = new TokenKeyword("else");
    public static final TokenKeyword SELECT         = new TokenKeyword("select");
    public static final TokenKeyword CASE           = new TokenKeyword("case");
    public static final TokenKeyword DEFAULT        = new TokenKeyword("default");
    public static final TokenKeyword FOR            = new TokenKeyword("for");
    public static final TokenKeyword NEW            = new TokenKeyword("new");
    public static final TokenKeyword DELETE         = new TokenKeyword("delete");
    public static final TokenKeyword WHILE          = new TokenKeyword("while");
    public static final TokenKeyword VAR            = new TokenKeyword("var");
    public static final TokenKeyword CONST          = new TokenKeyword("const");
    public static final TokenKeyword DELEGATE       = new TokenKeyword("delegate");
    public static final TokenKeyword TIE            = new TokenKeyword("tie");
    public static final TokenKeyword FUNCTION       = new TokenKeyword("function");
    public static final TokenKeyword PROPERTY       = new TokenKeyword("property");
    public static final TokenKeyword CONSTRUCTOR    = new TokenKeyword("constructor");
    public static final TokenKeyword DESTRUCTOR     = new TokenKeyword("destructor");
    public static final TokenKeyword IMPORT         = new TokenKeyword("import");
    public static final TokenKeyword CLASS          = new TokenKeyword("class");
    public static final TokenKeyword PACKAGE        = new TokenKeyword("package");
    public static final TokenKeyword TRUE           = new TokenKeyword("true");
    public static final TokenKeyword FALSE          = new TokenKeyword("false");
    public static final TokenKeyword STATIC         = new TokenKeyword("static");
    public static final TokenKeyword BREAK          = new TokenKeyword("break");
    public static final TokenKeyword NEXT           = new TokenKeyword("next");
    public static final TokenKeyword RETURN         = new TokenKeyword("return");
    public static final TokenKeyword GET            = new TokenKeyword("get");
    public static final TokenKeyword SET            = new TokenKeyword("set");
    public static final TokenKeyword EXTENDS        = new TokenKeyword("extends");

    private TokenKeyword(String name) {
        this.name = name;
    }

    public static Token identOrKeyword(TokenIdent ti) {
        String tin = ti.name;
        if (tin.equals("int")) return INT;
        if (tin.equals("float")) return FLOAT;
        if (tin.equals("bool")) return BOOL;
        if (tin.equals("void")) return VOID;
        if (tin.equals("char")) return CHAR;
        if (tin.equals("if")) return IF;
        if (tin.equals("else")) return ELSE;
        if (tin.equals("select")) return SELECT;
        if (tin.equals("case")) return CASE;
        if (tin.equals("default")) return DEFAULT;
        if (tin.equals("for")) return FOR;
        if (tin.equals("new")) return NEW;
        if (tin.equals("delete")) return DELETE;
        if (tin.equals("while")) return WHILE;
        if (tin.equals("var")) return VAR;
        if (tin.equals("const")) return CONST;
        if (tin.equals("delegate")) return DELEGATE;
        if (tin.equals("tie")) return TIE;
        if (tin.equals("function")) return FUNCTION;
        if (tin.equals("property")) return PROPERTY;
        if (tin.equals("constructor")) return CONSTRUCTOR;
        if (tin.equals("destructor")) return DESTRUCTOR;
        if (tin.equals("import")) return IMPORT;
        if (tin.equals("class")) return CLASS;
        if (tin.equals("package")) return PACKAGE;
        if (tin.equals("true")) return TRUE;
        if (tin.equals("false")) return FALSE;
        if (tin.equals("static")) return STATIC;
        if (tin.equals("break")) return BREAK;
        if (tin.equals("next")) return NEXT;
        if (tin.equals("return")) return RETURN;
        if (tin.equals("get")) return GET;
        if (tin.equals("set")) return SET;
        if (tin.equals("extends")) return EXTENDS;
        return ti;
    }

    public String toString() {
        return ("TokenKeyword(" + name + ")");
    }
}
