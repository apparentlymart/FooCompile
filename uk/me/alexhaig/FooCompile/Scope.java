
package uk.me.alexhaig.FooCompile;

import java.util.HashMap;
import java.util.Vector;

public class Scope {
    Node owner;
    HashMap vars;
    HashMap invokables;
    Scope parent;
    Vector children;

    public Scope(Scope parent) {
        this.parent = parent;
        this.vars = new HashMap();
        this.invokables = new HashMap();
        this.children = new Vector();
        if (parent != null) {
            parent.children.add(this);
        }
    }

    //TODO: To allow invokable overloading the invokables map should contain sets of
    //invokables with the same name, but different parameters. These sets should also
    //only allow a single return type.

    //The HashMap could then take a qref and return a set of nodes

    public void addItem(Module m, NodeDeclaration n) {
		addItem(m, n, null, null);
	}

	public void addItem(Module m, NodeDeclaration n, Class ownerClass, Package ownerPackage) {
		//NOTE: These two construct a unique name manually
		NodeDeclaration previous = null;
		boolean duplicate = false;
		if (invokables.containsKey("I" + n.name)) {
			duplicate = true;
			previous = ((Invokable)invokables.get("I" + n.name)).owner;
		}
		if (vars.containsKey("V" + n.name)) {
			duplicate = true;
			previous = ((Variable)vars.get("V" + n.name)).owner;
		}
		if (duplicate) {
			if (previous != null) throw CheckerException.duplicateDeclaration(n, previous);
  	        else throw CheckerException.duplicateDeclaration(n);
		}

		if (n instanceof NodeVariableDec) {
			Variable var = new Variable((NodeVariableDec)n);
		    vars.put(getUniqueName((NodeVariableDec)n), var);
		    var.setScope(this);

        }
		else if (n instanceof NodeFunctionDec) {
			Invokable inv = new Invokable(m, (NodeFunctionDec)n, this, ownerClass, ownerPackage);
			invokables.put(getUniqueName((NodeFunctionDec)n), inv);
			inv.setScope(this);
		}
		else if (n instanceof NodeDelegateDec) {
			Invokable inv = new Invokable((NodeDelegateDec)n, this, ownerClass, ownerPackage);
			invokables.put(getUniqueName((NodeDelegateDec)n), inv);
			inv.setScope(this);
		}
		else if (n instanceof NodePropertyDec) {
			Invokable inv = new Invokable(m, (NodePropertyDec)n, this, ownerClass, ownerPackage);
			invokables.put(getUniqueName((NodePropertyDec)n), inv);
			inv.setScope(this);
		}
		else throw new InternalErrorException("Can't add " + n + " to a scope!");
    }

    public void addItem(Module m, Variable var) {
        vars.put(getUniqueName(var), var);
        var.setScope(this);
    }
    public void addItem(Module m, Invokable i) {
        invokables.put(getUniqueName(i), i);
        i.setScope(this);
    }

    private static StringBuffer getUniqueNameBuffer(char t) {
        StringBuffer sb = new StringBuffer();
        sb.append(t);
        return sb;
    }

    public static String getUniqueName(NodeFunctionDec n) {
        StringBuffer sb = getUniqueNameBuffer('I');
        sb.append(n.name);

        return sb.toString();
    }

    public static String getUniqueName(NodePropertyDec n) {
        StringBuffer sb = getUniqueNameBuffer('I');
        sb.append(n.name);
        return sb.toString();
    }

    public static String getUniqueName(NodeDelegateDec n) {
        StringBuffer sb = getUniqueNameBuffer('I');
        sb.append(n.name);

        return sb.toString();
    }

    public static String getUniqueName(NodeVariableDec n) {
        StringBuffer sb = getUniqueNameBuffer('V');
        sb.append(n.name);
        return sb.toString();
    }

    public static String getUniqueName(NodeSpecialFunctionDec n) {
        StringBuffer sb;
        if (n.isDes) sb = getUniqueNameBuffer('D');
        else  sb = getUniqueNameBuffer('C');

        return sb.toString();
    }

    public static String getUniqueName(NodeVariableRef n) {
        StringBuffer sb = getUniqueNameBuffer('V');

        sb.append(n.QRef.getURef());

        return sb.toString();
    }

    public static String getUniqueName(NodeFunctionCall n) {
        StringBuffer sb = getUniqueNameBuffer('I');

        sb.append(n.qref.getURef());

        return sb.toString();
    }

    public static String getUniqueName(Variable v) {
        StringBuffer sb = getUniqueNameBuffer('V');
        sb.append(v.name);
        return sb.toString();
    }

    public static String getUniqueVariableName(NodeQRef qref) {
        StringBuffer sb = getUniqueNameBuffer('V');
        sb.append(qref.parts.lastElement());
        return sb.toString();
    }

    public static String getUniqueInvokableName(NodeQRef qref) {
        StringBuffer sb = getUniqueNameBuffer('I');
        sb.append(qref.parts.lastElement());
        return sb.toString();
    }

    public static String getUniqueName(Invokable i) {
        StringBuffer sb;
        if (i.kind == Invokable.CONSTRUCTOR) {
            sb = getUniqueNameBuffer('C');
        }
        else if (i.kind == Invokable.DESTRUCTOR) {
            sb = getUniqueNameBuffer('D');
        }
        else {
            sb = getUniqueNameBuffer('I');
        }
        sb.append(i.name);
        return sb.toString();
    }

    public Variable findVariable(String name) {
		//name must be a unique name
        Scope s = this;
        Variable v = (Variable)s.vars.get(name);

        while (v == null) {
            if (s.parent == null) return null;

            s = s.parent;
            v = (Variable)s.vars.get(name);
        }

        return v;
    }

    public Invokable findInvokable(String name) {
		//name must be a unique name
        Scope s = this;
        Invokable i = (Invokable)s.invokables.get(name);

        while (i == null) {
            if (s.parent == null) return null;

            s = s.parent;
            i = (Invokable)s.invokables.get(name);
        }

        return i;
    }

    public Symbol findSymbol(Module m, NodeQRef nodeRef) {
		return findSymbol(m, nodeRef, null);
	}

	public Symbol findSymbol(Module m, NodeQRef nodeRef, Class startClass) {
		//this function is based on a finite state machine
		Vector symbolRef = new Vector(3);

		int state;

		if (startClass == null) {
			state = 0;
		}
		else {
			symbolRef.add(startClass);
			state = 8;
		}

		int stop = nodeRef.parts.size();

        String part;
        Symbol s = null;

		for (int pos = 0; pos < stop; pos++) {

			part = (String)(nodeRef.parts.elementAt(pos));

			switch (state) {
				case 0:
					//local variable
					s = findVariable("V" + part);
					if (s != null) { state = 2; break; }

					//local invokable
					s = findInvokable("I" + part);
					if (s != null) { state = 3; break; }

					//invokable in the default package
					s = (Symbol)m.defPackage.globalFunc.get(part);
					if (s != null) { state = 3; break; }

					//class in the default package
					s = (Symbol)m.defPackage.classes.get(part);
					if (s != null) { state = 5; break; }

					//package
					s = (Symbol)m.packages.get(part);
					if (s != null) { state = 1; break; }
					break;

				case 1:
					Package parentP = ((Package)symbolRef.lastElement());

					//package in package
					s = (Symbol)parentP.children.get(part);
					if (s != null) { state = 1; break; }

					//top level function in package
					s = (Symbol)parentP.globalFunc.get(part);
					if (s != null) { state = 4; break; }

					//top level class in package
					s = (Symbol)parentP.classes.get(part);
					if (s != null) { state = 5; break; }
					break;
				case 2:
					Variable parentLV = ((Variable)symbolRef.lastElement());
					Class LVtypeClass = parentLV.type.getClass(m);
					if (LVtypeClass == null) break;

					//variable in variable
					s = LVtypeClass.findVariable(part);
					if (s != null) { state = 2; break; }

					//invokable in variable
					s = LVtypeClass.findInvokable(part);
					if (s != null) { state = 3; break; }
					//possible END STATE
					break;
				case 3:
					//END STATE
					break;
				case 4:
					//END STATE
					break;
				case 5:
					Class parentC = ((Class)symbolRef.lastElement());

					//static variable in class(or parent class recursively)
					Variable cv = parentC.findVariable(part);
					if (cv != null && cv.owner != null && cv.owner.mods != null && cv.owner.mods.mods.contains(TokenKeyword.STATIC)) s = cv;
					if (s != null) { state = 6; break; }

					Invokable ci = parentC.findInvokable(part);
					if (ci != null && ci.owner != null && ci.owner.mods != null && ci.owner.mods.mods.contains(TokenKeyword.STATIC)) s = ci;
					if (s != null) { state = 7; break; }
					//possible END STATE
					break;
				case 6:
					Variable parentV = ((Variable)symbolRef.lastElement());
					Class VtypeClass = parentV.type.getClass(m);
					if (VtypeClass == null) break;

					//variable in variable
					s = VtypeClass.findVariable(part);
					if (s != null) { state = 6; break; }

					//invokable in variable
					s = VtypeClass.findInvokable(part);
					if (s != null) { state = 7; break; }
					//possible END STATE
					break;
				case 7:
					//END STATE
					break;
				case 8:
					Class inClass = (Class)(symbolRef.lastElement());

					//variable in variable
					s = inClass.findVariable(part);
					if (s != null) { state = 2; break; }

					//invokable in variable
					s = inClass.findInvokable(part);
					if (s != null) { state = 3; break; }
					//possible END STATE
					break;
				default:
					throw new InternalErrorException("Unknown state resolving " + nodeRef);
			}

			if (s == null) {
				NodeQRef errorRef = (NodeQRef)(Node.setPos(nodeRef.partial(0, pos), nodeRef.line, nodeRef.column));

				throw CheckerException.symbolNotFound(errorRef);
			}
			else {
				symbolRef.add(s);
			}
			s = null;
		}

		// if the loop ends in a non-terminal state, something is amiss.
		if (state == 0) {
			throw new InternalErrorException("Ended in initial state while resolving " + nodeRef);
		}

		if (state == 1) {
			throw new CheckerException(nodeRef, "Invalid symbol type for expression", "Cannot reference a " + (Symbol)symbolRef.lastElement() + " as part of an expression");
		}

		return (Symbol)(symbolRef.lastElement());
	}

    public void printSelf(IndentingWriter w) {
        w.println(this.getClass().getName().substring(26)  + " (" + owner + ") [");
        w.indent();

        java.util.Iterator i = vars.values().iterator();

        while (i.hasNext()) {
            ((Variable)i.next()).printSelf(w);
        }

        i = invokables.values().iterator();

        while (i.hasNext()) {
            ((Invokable)i.next()).printSelf(w);
        }

        i = children.iterator();

        while (i.hasNext()) {
            ((Scope)i.next()).printSelf(w);
        }
        w.outdent();
        w.println("]");
    }
}
