
package uk.me.alexhaig.FooCompile;

import java.util.HashMap;

public class Package extends Symbol {
    HashMap globalFunc;
    HashMap classes;
    boolean imported = false;
    public HashMap children;
    NodeQRef packageRef;

    public Package(String name, HashMap globalFunc, HashMap classes, boolean imported, HashMap children, NodeQRef packageRef) {
        this.name = name;
        this.globalFunc = globalFunc;
        this.classes = classes;
        this.imported = imported;
        this.children = children;
        this.packageRef = packageRef;
    }

    public Package(String name, boolean imported) {
        this.name = name;
        this.imported = imported;
        this.children = new HashMap();
        this.classes = new HashMap();
        this.globalFunc = new HashMap();
        this.packageRef = new NodeQRef(name);
    }

    public Package(String name, boolean imported, Package parent) {
        parent.children.put(name, this);
        this.name = name;
        this.imported = imported;
        this.children = new HashMap();
        this.classes = new HashMap();
        this.globalFunc = new HashMap();
        this.packageRef = new NodeQRef(parent.packageRef, name);
    }

    public void addInvokable(Invokable inv) {
		if (this.globalFunc.containsKey(inv.name)) throw CheckerException.duplicateDeclaration(inv.owner);
        this.globalFunc.put(inv.name, inv);
    }

    public void addClass(Class cla) {
        this.classes.put(cla.name, cla);
    }

    public String symbolKind() {
        return "package";
	}

    public void printSelf(IndentingWriter w) {
        w.println(this.getClass().getName().substring(26)  + " (" + name + ") [");
        w.indent();

        java.util.Iterator i = classes.values().iterator();

        while (i.hasNext()) {
            ((Class)i.next()).printSelf(w);
        }

        i = globalFunc.values().iterator();

        while (i.hasNext()) {
            ((Invokable)i.next()).printSelf(w);
        }

        java.util.Iterator it = children.keySet().iterator();
        while (it.hasNext()) {
			Package cp = (Package)(children.get(it.next()));

			cp.printSelf(w);
		}
        w.outdent();
        w.println("]");
    }

}
