
package uk.me.alexhaig.FooCompile;

import java.util.Map;
import java.util.HashMap;

public class Class extends Symbol {
	Scope scope;
    Class parent = null;
    Map constructors;
    Map destructors;
    NodeClass owner;
    Package ownerPackage;

    public Class (String name, Scope scope) {
		this.owner = null;
        this.name = name;
        this.scope = scope;
        this.constructors = new HashMap();
        this.destructors = new HashMap();
    }

    public Class (NodeClass nc, Scope parentScope, Module m, Package ownerPackage) {
		this.owner = nc;
        this.name = nc.name;
        this.ownerPackage = ownerPackage;

        if (nc.extd != null) {
			parent = m.findClass(nc.extd);
			if (parent == null) throw new CheckerException(nc, "Parent class not found", "Cannot find parent class named "+nc.extd.name);
        }

        this.scope = new Scope(parentScope);
        this.scope.owner = nc;
        m.registerScope(nc, this.scope);
        this.constructors = new HashMap();
        this.destructors = new HashMap();

        java.util.Iterator i = nc.members.iterator();

        while (i.hasNext()) {
            NodeDeclaration nodeDec = (NodeDeclaration)i.next();

            if (nodeDec instanceof NodeSpecialFunctionDec) {
                NodeSpecialFunctionDec sd = (NodeSpecialFunctionDec)nodeDec;
                if (sd.isDes) {
                    if (constructors.containsKey(Scope.getUniqueName(sd))) throw new CheckerException(nodeDec, "Duplicate Destructor", "Destructor overloading is not supported");
                    destructors.put(Scope.getUniqueName(sd), new Invokable(m, sd, scope, this, this.ownerPackage));
                }
                else {
					if (constructors.containsKey(Scope.getUniqueName(sd))) throw new CheckerException(nodeDec, "Duplicate constructor", "Constructor overloading is not supported");
                    constructors.put(Scope.getUniqueName(sd), new Invokable(m, sd, scope, this, this.ownerPackage));
                }
            }
            else {
                this.scope.addItem(m, nodeDec, this, ownerPackage);
            }
        }

		Variable v = new Variable("this", new Type(owner));

		i = this.scope.invokables.values().iterator();
		Invokable inv = null;
		while(i.hasNext()) {
			 inv = (Invokable)i.next();
			 if ((inv.owner.mods == null) || (! inv.owner.mods.mods.contains(TokenKeyword.STATIC))) {
				 // delegates have null scope
				 if (inv.scope != null) inv.scope.addItem(m, v);
			 }
		}
    }

    public String symbolKind() {
        return "class";
	}

	//find variable in this class or parent
    public Variable findVariable(String name) {
		if (name == null) return null;
		Variable v = (Variable)scope.vars.get("V" + name);

		Class p = this.parent;
        while (p != null && v == null) {
			if (p.parent == null) return null;

            p = p.parent;
            v = (Variable)p.scope.vars.get("V" + name);
        }

        return v;
    }

   	//find invokable in this class or parent
    public Invokable findInvokable(String name) {
		Invokable i = (Invokable)scope.invokables.get("I" + name);

		Class p = this.parent;
        while (i == null) {
			if (p.parent == null) return null;

            p = p.parent;
            i = (Invokable)p.scope.vars.get("I" + name);
        }

        return i;
    }

    public void printSelf(IndentingWriter w) {
        w.println(this.getClass().getName().substring(26)  + " (" + name + ") [");
        w.indent();

        if (! constructors.isEmpty()) {
            w.println("constructors [");
            w.indent();
            java.util.Iterator i = constructors.values().iterator();
            while (i.hasNext()) {
                ((Invokable)i.next()).printSelf(w);
            }
            w.outdent();
            w.println("]");
        }

        if (! destructors.isEmpty()) {
            w.println("destructors [");
            w.indent();
            java.util.Iterator i = destructors.values().iterator();
            while (i.hasNext()) {
                ((Invokable)i.next()).printSelf(w);
            }
            w.outdent();
            w.println("]");
        }

        scope.printSelf(w);


        w.outdent();
        w.println("]");
    }

    public String toString() {
		return "class " + name;
	}
}
