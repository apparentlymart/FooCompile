
// A backend which generates C++ code

package uk.me.alexhaig.FooCompile;

import java.util.Iterator;
import java.io.*;

public class Backend_CPP implements Backend {
    Module m;
    IndentingWriter out;

    public Backend_CPP(Module m, PrintStream pw) {
        this.m = m;
        this.out = new IndentingWriter(pw);
    }

    public void generateCode() {
        printHeader();
        out.inprintln("");
        printFunctions();
    }

    public void printHeader() {
        // First, so the C++ compiler knows about all the classes,
        // print bodyless class declarations.
        Iterator it = m.classes.iterator();
        while (it.hasNext()) {
            NodeClass cla = (NodeClass)it.next();
            out.println("class "+cla.name+";");
        }

        it = m.classes.iterator();
        while (it.hasNext()) {
            NodeClass cla = (NodeClass)it.next();
            out.print("class "+cla.name);
            if (cla.extd != null) {
                out.inprint(" : "+cla.extd.name);
            }
            out.inprintln(" {");
            out.indent();
            out.println("public:");
            out.indent();

            Iterator mit = cla.members.iterator();
            while (mit.hasNext()) {
                printClassMember((NodeDeclaration)mit.next(), cla);
            }

            out.outdent();
            out.outdent();
            out.println("};");
        }
        it = m.globalfuncs.iterator();
        while (it.hasNext()) {
            printClassMember((NodeDeclaration)it.next(), null);
        }
    }

    public void printClassMember(NodeDeclaration dec, NodeClass cla) {
        if (dec instanceof NodeVariableDec) {
            NodeVariableDec ldec = (NodeVariableDec)dec;
            out.print(getTypeAsCPP(ldec.type.type)+" "+ldec.name);
            if (ldec.expr != null) {
                out.inprint(" = ");
                printExpression(ldec.expr, null, false);
	    }
	    out.inprintln(";");
        }
        else if (dec instanceof NodeFunctionDec) {
            NodeFunctionDec ldec = (NodeFunctionDec)dec;
            out.print(getTypeAsCPP(ldec.type.type)+" "+ldec.name+"(");
            Iterator it = ldec.params.members.iterator();
            boolean notFirst = false;
            while (it.hasNext()) {
                if (notFirst) {
                    out.inprint(", ");
                }
                else notFirst = true;
                NodeTypedNameDec name = (NodeTypedNameDec)it.next();
                out.inprint(getTypeAsCPP(name.type.type)+" "+name.name);

            }
            out.inprintln(");");
        }
        else if (dec instanceof NodeDelegateDec) {
            NodeDelegateDec ldec = (NodeDelegateDec)dec;
            out.print(getTypeAsCPP(ldec.type.type)+" (*"+ldec.name+")(");
            Iterator it = ldec.params.members.iterator();
            boolean notFirst = false;
            while (it.hasNext()) {
                if (notFirst) {
                    out.inprint(", ");
                }
                else notFirst = true;
                NodeTypedNameDec name = (NodeTypedNameDec)it.next();
                out.inprint(getTypeAsCPP(name.type.type)+" "+name.name);

            }
            out.inprintln(");");
        }
        else if (dec instanceof NodePropertyDec) {
            NodePropertyDec ldec = (NodePropertyDec)dec;
            out.println(getTypeAsCPP(ldec.type.type)+" ___propget_"+ldec.name+"();");
            out.println("void ___propset_"+ldec.name+"("+getTypeAsCPP(ldec.type.type)+" value);");
        }
        else if (dec instanceof NodeSpecialFunctionDec) {
            NodeSpecialFunctionDec ldec = (NodeSpecialFunctionDec)dec;
            if (ldec.isDes) {
		out.print("~"+cla.name);
	    }
	    else {
		out.print(cla.name);
	    }

            out.inprint("(");
            Iterator it = ldec.params.members.iterator();
            boolean notFirst = false;
            while (it.hasNext()) {
                if (notFirst) {
                    out.inprint(", ");
                }
                else notFirst = true;
                NodeTypedNameDec name = (NodeTypedNameDec)it.next();
                out.inprint(getTypeAsCPP(name.type.type)+" "+name.name);

            }
            out.inprintln(");");
        }
    }

    public String getTypeAsCPP(Type t) {
        if (! t.isIntrinsic()) {
            return t.name.name+"*";
        }
        else if (t.intrinsicType == TokenKeyword.INT) {
            if (t.size == -1 || t.size == 32) {
                return "int"+(t.arraymod > 0 ? "*" : "");
            }
            if (t.size == 16) {
                return "short"+(t.arraymod > 0 ? "*" : "");
            }
            if (t.size == 8) {
                return "char"+(t.arraymod > 0 ? "*" : "");
            }
            throw new BackendException("Unsupported Type", "Integers must be 8-, 16- or 32-bit");
        }
        else if (t.intrinsicType == TokenKeyword.FLOAT) {
            if (t.size == -1 || t.size == 32) {
                return "double"+(t.arraymod > 0 ? "*" : "");
            }
            if (t.size == 16) {
                return "float"+(t.arraymod > 0 ? "*" : "");
            }
            throw new BackendException("Unsupported Type", "Floats must be 16- or 32-bit");
        }
        else if (t.intrinsicType == TokenKeyword.BOOL) {
            if (t.size == -1) {
                return "bool"+(t.arraymod > 0 ? "*" : "");
            }
            throw new BackendException("Unsupported Type", "Must not specify size of boolean");
        }
        else if (t.intrinsicType == TokenKeyword.CHAR) {
            if (t.size == -1) {
                return "char"+(t.arraymod > 0 ? "*" : "");
            }
            throw new BackendException("Unsupported Type", "Must not specify size of boolean");
        }
        else if (t.intrinsicType == TokenKeyword.VOID) {
            if (t.size == -1) {
                // Can't have a void array
                return "void";
            }
            throw new BackendException("Unsupported Type", "Must not specify size of void");
        }
        throw new BackendException("Unsupported Type", "Can't use "+t);
    }

    public void printFunctions() {
        Iterator it = m.classes.iterator();
        while (it.hasNext()) {
            NodeClass cla = (NodeClass)it.next();

            Iterator mit = cla.members.iterator();
            while (mit.hasNext()) {
		printFunction((NodeDeclaration)mit.next(), cla);
            }
        }
        it = m.globalfuncs.iterator();
        while (it.hasNext()) {
            printFunction((NodeDeclaration)it.next(), null);
        }
    }

    public void printFuncBody(NodeStmt s, Scope funcScope) {
        printStatement(s, funcScope, true);
    }

    public String getDecNameCPP(NodeDeclaration ldec, NodeClass cla) {
        return (cla != null ? cla.name + "::" : "") + ldec.name;
    }

    public void printFunction(NodeDeclaration dec, NodeClass cla) {
        if (dec instanceof NodeFunctionDec) {
            NodeFunctionDec ldec = (NodeFunctionDec)dec;
            out.print(getTypeAsCPP(ldec.type.type)+" "+getDecNameCPP(ldec,cla)+"(");
            Iterator it = ldec.params.members.iterator();
            boolean notFirst = false;
            while (it.hasNext()) {
                if (notFirst) {
                    out.inprint(", ");
                }
                else notFirst = true;
                NodeTypedNameDec name = (NodeTypedNameDec)it.next();
                out.inprint(getTypeAsCPP(name.type.type)+" "+name.name);

            }
            out.inprint(") ");
            printFuncBody(ldec.code, null);
        }
        else if (dec instanceof NodePropertyDec) {
            NodePropertyDec ldec = (NodePropertyDec)dec;
            out.print(getTypeAsCPP(ldec.type.type)+" "+(cla != null ? cla.name + "::" : "") + "___propget_" + ldec.name+"() ");
            printFuncBody(ldec.getStmt, null);
            out.print("void "+(cla != null ? cla.name + "::" : "") + "___propset_" + ldec.name+"("+getTypeAsCPP(ldec.type.type)+" value) ");
            printFuncBody(ldec.setStmt, null);
        }
        else if (dec instanceof NodeSpecialFunctionDec) {
            NodeSpecialFunctionDec ldec = (NodeSpecialFunctionDec)dec;
            if (ldec.isDes) {
		out.print("~"+cla.name);
	    }
	    else {
		out.print(cla.name);
	    }

            out.inprint("(");
            Iterator it = ldec.params.members.iterator();
            boolean notFirst = false;
            while (it.hasNext()) {
                if (notFirst) {
                    out.inprint(", ");
                }
                else notFirst = true;
                NodeTypedNameDec name = (NodeTypedNameDec)it.next();
                out.inprint(getTypeAsCPP(name.type.type)+" "+name.name);

            }
            out.inprint(") ");
            printFuncBody(ldec.code, null);
        }
    }

    // FIXME: Make this have a localScope parameter so that
    //      delete statements can find out if they are deleting
    //      an array.
    public void printStatement(NodeStmt stmt, Scope localScope, boolean inline) {
	if (! inline) out.print("");
        if (stmt instanceof NodeCodeBlock) {
	    NodeCodeBlock cb = (NodeCodeBlock)stmt;
            out.inprintln("{");
            out.indent();
            Iterator it = cb.stmts.iterator();
            while (it.hasNext()) {
		printStatement((NodeStmt)it.next(), localScope, false);
	    }
            out.outdent();
            out.println("}");
	}
	else if (stmt instanceof NodeIfStmt) {
	    NodeIfStmt s = (NodeIfStmt)stmt;
            out.inprint("if ");
            printExpression(s.expr, localScope, true);
            printStatement(s.ifcode, localScope, true);
            if (s.elsecode != null) {
		out.print("else ");
                printStatement(s.elsecode, localScope, true);
	    }
	}
	else if (stmt instanceof NodeWhileStmt) {
	    NodeWhileStmt s = (NodeWhileStmt)stmt;
            out.inprint("while ");
            printExpression(s.expr, localScope, true);
            printStatement(s.code, localScope, true);
	}
	else if (stmt instanceof NodeForStmt) {
	    NodeForStmt s = (NodeForStmt)stmt;
            out.inprint("for (");
            if (s.init instanceof NodeExpr) {
                printExpression((NodeExpr)s.init, localScope, true);
	    }
	    else if (s.init instanceof NodeVariableDec) {
		NodeVariableDec vd = (NodeVariableDec)s.init;
                out.inprint("var "+getTypeAsCPP(vd.type.type)+" "+vd.name);
	    }
            out.inprint("; ");
            printExpression(s.cond, localScope, false);
            out.inprint("; ");
            printExpression(s.iter, localScope, false);
            out.inprint(") ");
            printStatement(s.code, localScope, true);
	}
	else if (stmt instanceof NodeVariableDecStmt) {
	    NodeVariableDecStmt s = (NodeVariableDecStmt)stmt;
            out.inprint(getTypeAsCPP(s.vd.type.type)+" "+s.vd.name);
            if (s.vd.expr != null) {
                out.inprint(" = ");
                printExpression(s.vd.expr, null, false);
	    }
	    out.inprintln(";");
	}
	else if (stmt instanceof NodeReturnStmt) {
	    NodeReturnStmt s = (NodeReturnStmt)stmt;
            out.inprint("return ");
            printExpression(s.expr, localScope, false);
            out.inprintln(";");
	}
	else if (stmt instanceof NodeDeleteStmt) {
	    NodeDeleteStmt s = (NodeDeleteStmt)stmt;
	    // FIXME: Must check the type of the variable to see if
	    //    it is an array. If it is, must do: delete [] name;
            out.inprintln("delete "+s.vr.QRef.name+";");
	}
	else if (stmt instanceof NodeExprStmt) {
	    NodeExprStmt s = (NodeExprStmt)stmt;
            printExpression(s.expr, localScope, false);
            out.inprintln(";");
	}
	else throw new BackendException(stmt, "Unrecognised statement type",
	                               "Don't know how to generate "+stmt);
    }

    public void printExpression(NodeExpr expr, Scope localScope, boolean wrapParens) {
	// This generates excessive parens, but by doing so avoids problems
	// with precedence differences between Foo and C++. Also, since the parser
	// doesn't store anything special for parenthesised expressions must
	// err on the side of caution.
        if (wrapParens) {
   	    out.inprint("(");
	}

        if (expr instanceof NodeAssignOp) {
	    NodeAssignOp e = (NodeAssignOp)expr;
	    printExpression(e.lhs, localScope, true);
	    out.inprint("=");
	    printExpression(e.rhs, localScope, true);
	}
        else if (expr instanceof NodeEqualityTestOp) {
	    NodeEqualityTestOp e = (NodeEqualityTestOp)expr;
	    printExpression(e.lhs, localScope, true);
	    out.inprint(e.equals ? "==" : "!=");
	    printExpression(e.rhs, localScope, true);
	}
        else if (expr instanceof NodeRelationTestOp) {
	    NodeRelationTestOp e = (NodeRelationTestOp)expr;
	    printExpression(e.lhs, localScope, true);
	    out.inprint((e.great ? ">" : "<") + (e.equalTo ? "=" : ""));
	    printExpression(e.rhs, localScope, true);
	}
        else if (expr instanceof NodeIncrOp) {
	    NodeIncrOp e = (NodeIncrOp)expr;
	    String op = e.positive ? "++" : "--";
	    if (e.preOp) out.inprint(op);
	    printExpression(e.expr, localScope, true);
	    if (! e.preOp) out.inprint(op);
	}
        else if (expr instanceof NodeNewOp) {
	    NodeNewOp e = (NodeNewOp)expr;
            out.inprint("new "+e.name.name+"(");
            Iterator it = e.params.members.iterator();
            boolean notFirst = false;
            while (it.hasNext()) {
                if (notFirst) {
                    out.inprint(", ");
                }
                else notFirst = true;
                printExpression((NodeExpr)it.next(), localScope, false);
            }
            out.inprint(")");
	}
        else if (expr instanceof NodeFunctionCall) {
	    NodeFunctionCall e = (NodeFunctionCall)expr;
	    out.inprint(e.qref.name);
	    out.inprint("(");
            Iterator it = e.params.members.iterator();
            boolean notFirst = false;
            while (it.hasNext()) {
                if (notFirst) {
                    out.inprint(", ");
                }
                else notFirst = true;
                printExpression((NodeExpr)it.next(), localScope, false);
            }

	    out.inprint(")");
	}
	else if (expr instanceof NodeVariableRef) {
            out.inprint(((NodeVariableRef)expr).QRef.name);
        }
	else if (expr instanceof NodeIntegerLiteral) {
            out.inprint(((NodeIntegerLiteral)expr).val+"");
        }
	else if (expr instanceof NodeFloatLiteral) {
            out.inprint(((NodeFloatLiteral)expr).val+"");
        }
	else if (expr instanceof NodeBooleanLiteral) {
            out.inprint(((NodeBooleanLiteral)expr).val ? "true" : "false");
        }
	else throw new BackendException(expr, "Unrecognised expression type",
	                               "Don't know how to generate "+expr);
        if (wrapParens) {
	    out.inprint(")");
	}
    }

}
