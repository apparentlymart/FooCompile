
package uk.me.alexhaig.FooCompile;

import java.util.Vector;
import java.util.Iterator;

public class Checker {

    public static void check(Module m) {
        buildSymbolTable(m);

        Iterator i = m.globalfuncs.iterator();

        NodeFunctionDec inv;
        while (i.hasNext()) {
            inv = ((NodeFunctionDec)i.next());
            inv.check(m, null);
        }

        i = m.classes.iterator();
        NodeClass cla;
        while (i.hasNext()) {
            cla = ((NodeClass)i.next());
            cla.check(m, null);
        }
        m.isChecked = true;
    }

    private static void buildSymbolTable(Module m) {
        Package p = addPackage(m,null);
        m.defPackage = p;

        Iterator i = m.globalfuncs.iterator();
        NodeFunctionDec inv;
        while (i.hasNext()) {
            inv = ((NodeFunctionDec)i.next());
            p.addInvokable(new Invokable(m, inv, null, null, p));
        }

        i = m.classes.iterator();
        NodeClass cla;
        while (i.hasNext()) {
            cla = ((NodeClass)i.next());
            Class claSymbol = new Class(cla, null, m, p);
            p.addClass(claSymbol);
        }
    }

    public static Package addPackage(Module m, NodeQRef name) {
        Package p;

        if (name == null) {
            p = new Package("", false);
            m.packages.put("", p);
            return p;
        }

        Iterator i = name.parts.iterator();
        String URef;

        URef = ((String)i.next());

        if (m.packages.containsKey(URef)) {
            p = (Package)(m.packages.get(URef));
        }
        else {
            p = new Package(URef, false);
            m.packages.put(URef, p);
        }

        while(i.hasNext()) {
            URef = ((String)i.next());

            if (p.children.containsKey(URef)) {
                p = (Package)(p.children.get(URef));
            }
            else {
                p = new Package(URef, false, p);
            }

        }

        return p;
    }

    public static Scope getStmtScope(Module m, NodeStmt stmt, Scope parentScope) {

        Scope ret = new Scope(parentScope);

        if (stmt == null) return ret;

        ret.owner = stmt;

        m.registerScope(stmt, ret);

        if (stmt instanceof NodeVariableDecStmt) {
            ret.addItem(m, ((NodeVariableDecStmt)stmt).vd);
            return ret;
        }

        if (! (stmt instanceof NodeCodeBlock)) {
            return ret;
        }

        Iterator i = ((NodeCodeBlock)stmt).stmts.iterator();
        while (i.hasNext()) {
            NodeStmt s = ((NodeStmt)i.next());
            if (s instanceof NodeCodeBlock) {
                getStmtScope(m, s, ret); // Sets up parent-child relationship as side-effect
            }
            if (s instanceof NodeVariableDecStmt) {
                ret.addItem(m, ((NodeVariableDecStmt)s).vd);
            }
            if (s instanceof NodeWhileStmt) {
                Scope ns = getStmtScope(m, s, ret);
                getStmtScope(m, ((NodeWhileStmt)s).code, ns);
            }
            if (s instanceof NodeIfStmt) {
                Scope ns = getStmtScope(m, s, ret);
                getStmtScope(m, ((NodeIfStmt)s).ifcode, ns);
                if (((NodeIfStmt)s).elsecode != null) getStmtScope(m, ((NodeIfStmt)s).elsecode, ns);
            }
            if (s instanceof NodeForStmt) {
                NodeForStmt fs = (NodeForStmt)s;
                Scope forScope = getStmtScope(m, fs, ret);
                if (fs.init instanceof NodeVariableDec) {
                    forScope.addItem(m, (NodeVariableDec)(fs.init));
                }
                getStmtScope(m, fs.code, forScope);
            }
            if (s instanceof NodeSelectStmt) {
                Iterator it = ((NodeSelectStmt)s).cases.iterator();

                while (it.hasNext()) {
                    NodeSelectCaseBlock scb = (NodeSelectCaseBlock)(it.next());
                    getStmtScope(m, scb.code, ret);
                }
            }
        }
        return ret;
    }

    public static boolean isLValue(Node n) {
		//TODO: this check could be improved
        if (n instanceof NodeVariableRef || n instanceof NodeArrayDeref) {
            return true;
        }
        else if (n instanceof NodeDotOp) {
			return ((NodeDotOp)n).rhs instanceof NodeVariableRef;
		}
        return false;
    }

    public static void checkList(Vector list, Module m, Scope localScope) {
        Iterator i = list.iterator();

        while (i.hasNext()) {
            ((Node)i.next()).check(m, localScope);
        }
    }

    public static void checkReturnStmts(Module m, NodeStmt start, Type expected, Scope localScope) {
        // This makes the code look cleaner below at the
        // cost of one extra recursion when a non-existant
        // code block is checked.
        if (start == null) return;

        if (start instanceof NodeReturnStmt) {
            NodeExpr returnExpr = ((NodeReturnStmt)start).expr;
            Type exprType;
            if (returnExpr == null) exprType = Type.VOID;
            //else exprType = returnExpr.getType(m, expected, m.getScope(start));
            else exprType = returnExpr.getType(m, expected, localScope);
            if (! exprType.canBe(m, expected)) {
                throw new CheckerException(start, "Incorrect return type", "Expected return type " + expected + ", but found " + exprType);
            }
        }
        else if (start instanceof NodeCodeBlock) {
            NodeCodeBlock cb = (NodeCodeBlock)start;
            Iterator i = cb.stmts.iterator();

            // Call this function recursively for each child stmt.
            while (i.hasNext()) {
                NodeStmt s = (NodeStmt)i.next();

                checkReturnStmts(m, s, expected, m.getScope(start));
            }
        }
        else if (start instanceof NodeWhileStmt) {
            checkReturnStmts(m, ((NodeWhileStmt)start).code, expected, m.getScope(start));
        }
        else if (start instanceof NodeForStmt) {
            checkReturnStmts(m, ((NodeForStmt)start).code, expected, m.getScope(start));
        }
        else if (start instanceof NodeIfStmt) {
            checkReturnStmts(m, ((NodeIfStmt)start).ifcode, expected, m.getScope(start));
            checkReturnStmts(m, ((NodeIfStmt)start).elsecode, expected, m.getScope(start));
        }
        else if (start instanceof NodeSelectStmt) {
            NodeSelectCaseBlock caseBlock;
            Iterator i =  ((NodeSelectStmt)start).cases.iterator();

            while (i.hasNext()) {
				caseBlock = (NodeSelectCaseBlock)i.next();
				checkReturnStmts(m, caseBlock.code, expected, m.getScope(start));
			}
        }
        else {
            // It's not anything important
            return;
        }
    }

}
