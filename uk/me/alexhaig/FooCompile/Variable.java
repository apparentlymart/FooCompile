
package uk.me.alexhaig.FooCompile;

import java.util.HashMap;

public class Variable extends Symbol {

    Type type;
    NodeDeclaration owner;

    public Variable(String name, Type type) {
        this.name = name;
        this.type = type;
    }

    public Variable(NodeVariableDec owner) {
		this.owner = owner;
        this.name = owner.name;
        this.type = owner.type.type;
    }

    public Variable(NodeTypedNameDec param) {
        this.name = param.name;
        this.type = param.type.type;
    }

    public String symbolKind() {
        return "variable";
	}

    public void printSelf(IndentingWriter w) {
        w.println(this.getClass().getName().substring(26)  + " ("  + type + " " + name + ");");
    }
}
