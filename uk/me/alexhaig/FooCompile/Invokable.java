
package uk.me.alexhaig.FooCompile;

import java.util.Vector;

public class Invokable extends Symbol {
    Scope scope;
    Scope getscope;
    Scope setscope;
    int kind;
    Type returnType;
    public Vector params;
    NodeDeclaration owner;
    Class ownerClass;
    Package ownerPackage = null;


    public static final int FUNCTION = 0;
    public static final int DELEGATE = 1;
    public static final int PROPERTY = 2;
    public static final int CONSTRUCTOR = 3;
    public static final int DESTRUCTOR = 4;

    public Invokable(String name, Scope scope, int kind, Type returnType, Class ownerClass, Package ownerPackage) {
        this.name = name;
        this.scope = scope;
        this.kind = kind;
        this.returnType = returnType;
        // FIXME: Need a constructor with params as a parameter
        this.params = new Vector();
		this.ownerClass = ownerClass;
		this.ownerPackage = ownerPackage;
    }

    public Invokable(Module m, NodeFunctionDec funcDec, Scope parentScope, Class ownerClass, Package ownerPackage) {
		this.ownerClass = ownerClass;
		this.ownerPackage = ownerPackage;
		this.owner = funcDec;
        this.kind = FUNCTION;
        this.name = funcDec.name;
        this.returnType = funcDec.type.type;
        this.scope = new Scope(parentScope);
        this.scope.owner = funcDec;
        Checker.getStmtScope(m, funcDec.code, this.scope);

        java.util.Iterator i = funcDec.params.members.iterator();

        this.params = new Vector();
        while (i.hasNext()) {
            NodeTypedNameDec param = (NodeTypedNameDec)i.next();
            Variable vp = new Variable(param);
            params.add(vp);
            this.scope.addItem(m, vp);
        }
        m.registerScope(funcDec, this.scope);
    }

    public Invokable(NodeDelegateDec deleDec, Scope parentScope, Class ownerClass, Package ownerPackage) {
		this.ownerClass = ownerClass;
		this.ownerPackage = ownerPackage;
		this.owner = deleDec;
        this.kind = DELEGATE;
        this.name = deleDec.name;
        this.returnType = deleDec.type.type;
        this.scope = null;

        java.util.Iterator i = deleDec.params.members.iterator();

        this.params = new Vector();
        while (i.hasNext()) {
            NodeTypedNameDec param = (NodeTypedNameDec)i.next();
            Variable vp = new Variable(param);
            params.add(vp);
        }
    }

    public Invokable(Module m, NodePropertyDec propDec, Scope parentScope, Class ownerClass, Package ownerPackage) {
		this.ownerClass = ownerClass;
		this.ownerPackage = ownerPackage;
		this.owner = propDec;
        this.kind = PROPERTY;
        this.name = propDec.name;
        this.returnType = propDec.type.type;
        this.getscope = Checker.getStmtScope(m, propDec.getStmt, parentScope);
        if (propDec.setStmt != null) {
            this.setscope = Checker.getStmtScope(m, propDec.setStmt, parentScope);
            this.setscope.addItem(m, new Variable("value", returnType));
		}
        this.params = null;
    }

    public Invokable(Module m, NodeSpecialFunctionDec specDec, Scope parentScope, Class ownerClass, Package ownerPackage) {
		this.ownerClass = ownerClass;
		this.ownerPackage = ownerPackage;
		this.owner = specDec;
        this.kind = (specDec.isDes ? DESTRUCTOR : CONSTRUCTOR);
        this.name = specDec.name;
        this.returnType = null;
        this.scope = new Scope(parentScope);
        this.scope.owner = specDec;
        Checker.getStmtScope(m, specDec.code, this.scope);

        java.util.Iterator i = specDec.params.members.iterator();

        this.params = new Vector();
        while (i.hasNext()) {
            NodeTypedNameDec param = (NodeTypedNameDec)i.next();
            Variable vp = new Variable(param);
            params.add(vp);
            this.scope.addItem(m, vp);
        }
        m.registerScope(specDec, this.scope);
    }

    public void checkParameters(Module m, NodeParamIn callParams, Scope localScope) {
		if (this.params.size() != callParams.members.size()) {
			throw new CheckerException(callParams, "Parameter list length incorrect", "Expecting "+this.params.size()+" parameters, but got "+callParams.members.size());
		}
		int pos = 0;
		Type thisType = null;
		Type callType = null;
		NodeExpr callExpr = null;
		java.util.Iterator i = callParams.members.iterator();
		while (i.hasNext()) {
			thisType = ((Variable)this.params.elementAt(pos)).type;
			callExpr = (NodeExpr)i.next();
			callType = callExpr.getType(m, thisType, localScope);
			if (callType.canBe(m, thisType) == false) throw CheckerException.incompatibleTypes(callExpr, thisType, callType);
			pos++;
		}
	}

    public String symbolKind() {
        switch (kind) {
            case FUNCTION:
                return "function";
            case DELEGATE:
                return "delegate";
            case PROPERTY:
                return "property";
            case CONSTRUCTOR:
                return "constructor";
            case DESTRUCTOR:
                return "destructor";
            default:
                return "unknown invokable";
        }
	}

    public void printSelf(IndentingWriter w) {
        String strKind = symbolKind();

        if (scope != null) {
            w.println(this.getClass().getName().substring(26)  + "["+strKind+"] (" + returnType + " " + name + ") [");
            w.indent();
            scope.printSelf(w);
            w.outdent();
            w.println("]");
		} else if (kind == PROPERTY) {
            w.println(this.getClass().getName().substring(26)  + "["+strKind+"] (" + returnType + " " + name + ") [");
            w.indent();
            if (getscope != null) getscope.printSelf(w);
            if (setscope != null) setscope.printSelf(w);
            w.outdent();
            w.println("]");
        } else {
            w.println(this.getClass().getName().substring(26)  + "["+strKind+"] (" + returnType + " " + name + ");");
        }
    }
}
