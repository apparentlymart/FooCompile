
package uk.me.alexhaig.FooCompile;

import java.io.*;
import java.util.Vector;
import java.util.Iterator;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;

public class Module {
    // Parse Tree Members
    Vector classes;
    Vector globalfuncs;

    // Checker Members
    boolean isChecked = false;
    Package defPackage = null;
    public Map packages = null;
    Map scopeTable = null;
    Vector importTable;

    public Module() {
        classes = new Vector();
        globalfuncs = new Vector();
        packages = new HashMap();
        scopeTable = new HashMap();
        importTable = new Vector(3);
    }

    public static Module fromStream(Reader is) {
        Scanner s = new Scanner(is);
        Tokeniser t = new Tokeniser(s);
        Lexer l = new Lexer(t);
        return l.parse();
    }

    public void addClass(NodeClass n) {
        classes.add(n);
    }

    public void addFunction(NodeFunctionDec n) {
        globalfuncs.add(n);
    }

    public Class findClass(NodeQRef qref) {
        if (qref.parts.size() == 1) {
            return (Class)defPackage.classes.get(qref.parts.elementAt(0));
        }
        Iterator i = qref.parts.iterator();
        Package p = (Package)(packages.get(i.next()));
        int stop = qref.parts.size();
        int current = 2;

        while (i.hasNext()) {
			if (p == null) return null;
            String uref = ((String)i.next());
            if (current == stop) {
				return (Class)(p.classes.get(uref));
            }
            else {
                p = (Package)(p.children.get(uref));
            }
            current++;
        }
        return null;
    }

    public Invokable findInvokable(NodeQRef invRef) {
		if (invRef.parts.size() < 2) {
			return (Invokable)defPackage.globalFunc.get(invRef.parts.elementAt(0));
		}

		String uName = (String)Scope.getUniqueInvokableName(invRef);
		NodeQRef classRef = new NodeQRef();
		int stop = invRef.parts.size() - 1;
		for (int i = 0; i < stop; i++) {
			classRef.parts.add(invRef.parts.elementAt(i));
		}
		Class ownerClass = findClass(classRef);
		if (ownerClass == null) return null;
		Invokable inv = (Invokable)ownerClass.scope.findInvokable(uName);
		return inv;
	}

    public boolean isValidType(Type type) {
		if (type.isIntrinsic) return true;
		//if findclass doesn't return null it must exist as a class in a package somewhere already
		return findClass(type.name) != null;
	}

    public void registerScope(Node nd, Scope sc) {
        scopeTable.put(nd, sc);
    }

    public Scope getScope(Node n) {
		Scope ret = (Scope)scopeTable.get(n);
		if (ret == null) throw new InternalErrorException("Scope for "+n+" is missing!");
        return ret;
    }

    public void registerImport(Invokable inv) {
		importTable.add(inv);
	}

    public void printParseTree(IndentingWriter w) {
        Iterator i = globalfuncs.iterator();
        Node n;
        while (i.hasNext()) {
            n = (Node)i.next();
            n.printSelf(w);
        }
        i = classes.iterator();
        while (i.hasNext()) {
            n = (Node)i.next();
            n.printSelf(w);
        }
    }

    public void printSymbolTable(IndentingWriter w) {
        if (! isChecked) {
            throw new InternalErrorException("printSymbolTable called on unchecked module");
        }
        //defPackage.printSelf(w);

        java.util.Iterator it = packages.keySet().iterator();
        while (it.hasNext()) {
			Package cp = (Package)(packages.get(it.next()));

			cp.printSelf(w);
		}
    }

    public void printScopeTable() {
        Set s = (Set)scopeTable.keySet();
        Iterator i = s.iterator();

        PrintStream pw = new PrintStream(System.out);
        while(i.hasNext()) {
            pw.println(i.next());
        }

    }
}
