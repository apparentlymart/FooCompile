
package uk.me.alexhaig.FooCompile;

import java.io.*;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Vector;
import java.util.Iterator;

public class Backend_6502 implements Backend {
    HashMap uniqtable;
    HashSet imports;
    int nextuniq;
    Module m;
    ASMWriter out;

    public Backend_6502(Module m, PrintStream pw) {
        uniqtable = new HashMap();
        imports = new HashSet();
        nextuniq = 1;
        this.m = m;
        this.out = new ASMWriter(pw);
    }

    public void generateCode() {
        out.printHeader();
        printImportsAndExport();
        printStartUp();
        printVariableSpace();
        printInvokables();
    }

    public void printStartUp() {
		//TODO: (Assembly used from cc65 runtime)
		out.printSegment("STARTUP");
		out.println(".word\tHead");
		out.println("Head", ".word\t@Next");
		out.println(".word\t1000");
		out.println(".byte\t$9E,\"2061\"");
		out.println(".byte\t$00");
		out.println("@Next", ".word\t0");

		out.println("lda $01");
		out.println("pha");
		out.println("and #$F8");
		out.println("ora #$06");
		out.println("sta $01");

		/*out.startProc("_main: near");
		out.println("looploop", "jsr _func@@@main");
		out.println("jmp looploop");
		out.endProc();*/
	}

	/*public void accumulatorDisplay() {
		out.println("jsr $FFD2");
	}*/

    public void printImportsAndExport() {
        Iterator i = m.importTable.iterator();
        while (i.hasNext()) {
			String uniName = getUniqueName((Invokable)i.next());
			if (!(imports.contains(uniName))) {
				imports.add(uniName);
				out.imp(uniName);
			}
        }
        if (m.defPackage.globalFunc.get("main") != null) out.exp("_func@@@main");
        out.exp("parameterVal0");
        out.exp("parameterVal1");
        out.exp("parameterVal2");
        out.exp("parameterVal3");
        out.exp("parameterVal4");
        out.exp("parameterVal5");
        out.exp("parameterVal6");
        out.exp("parameterVal7");
        out.exp("temp1");
        out.exp("temp2");
    }

    public void printVariableSpace() {
	out.printSegment("DATA");

        Iterator is = m.scopeTable.values().iterator();
        while (is.hasNext()) {
            Scope s = (Scope)is.next();

            Iterator iv = s.vars.values().iterator();
            while (iv.hasNext()) {
                Variable var = (Variable)iv.next();
				out.println(formSymbolicLabel(s, var.name), ".byte $0");
            }
        }
        out.println("temp1", ".byte $00");
        out.println("temp2", ".byte $00");
        out.println("parameterVal0", ".byte $00");
        out.println("parameterVal1", ".byte $00");
        out.println("parameterVal2", ".byte $00");
        out.println("parameterVal3", ".byte $00");
        out.println("parameterVal4", ".byte $00");
        out.println("parameterVal5", ".byte $00");
        out.println("parameterVal6", ".byte $00");
        out.println("parameterVal7", ".byte $00");
    }

    public void printInvokables() {
        Invokable inv;
        NodeFunctionDec funcDec;

        Iterator i = m.defPackage.globalFunc.values().iterator();
        while (i.hasNext()) {
            inv = (Invokable)i.next();
            Scope invScope = inv.scope;
            if (inv.owner instanceof NodeFunctionDec) funcDec = (NodeFunctionDec)inv.owner;
            else throw new InternalErrorException("Global invokables should only be functions.");

            out.startProc(getUniqueName(inv));

			if (funcDec.params.members.size() > 8) {
				throw BackendException.unsupportedFeature("more than 8 function parameters");
			}

			Iterator iNFD = funcDec.params.members.iterator();
			int iNFDcounter = 0;
			while (iNFD.hasNext()) {
				NodeTypedNameDec typedNameDec = (NodeTypedNameDec)iNFD.next();
				String param = typedNameDec.name;
				out.println("lda parameterVal"+iNFDcounter);
				out.println("sta "+formSymbolicLabel(invScope, param));
				iNFDcounter++;
			}

            printStatement(funcDec.code, invScope);
            out.println("rts");
            out.endProc();
        }

        Class cla;

        Iterator classI = m.defPackage.classes.values().iterator();
        while (classI.hasNext()) {
            cla = (Class)classI.next();

            i = cla.scope.invokables.values().iterator();
            while (i.hasNext()) {
                inv = (Invokable)i.next();
                Scope invScope = inv.scope;

                if (inv.owner instanceof NodeFunctionDec) {
                    funcDec = (NodeFunctionDec)inv.owner;
                    out.startProc(getUniqueName(inv));

                    if (funcDec.params.members.size() > 8) {
                	    throw BackendException.unsupportedFeature("more than 8 function parameters");
					}

                    Iterator iNFD = funcDec.params.members.iterator();
                    int iNFDcounter = 0;
                    while (iNFD.hasNext()) {
						NodeTypedNameDec typedNameDec = (NodeTypedNameDec)iNFD.next();
						String param = typedNameDec.name;
						out.println("lda parameterVal"+iNFDcounter+"+1");
						out.println("sta "+formSymbolicLabel(invScope, param)+"+1");
						out.println("lda parameterVal"+iNFDcounter);
						out.println("sta "+formSymbolicLabel(invScope, param));
						iNFDcounter++;
					}

                    printStatement(funcDec.code, invScope);
                    out.endProc();
                }
                else if (inv.owner instanceof NodeDelegateDec) {
                    out.println(getUniqueName(inv),".addr $00");
                }
                else if (inv.owner instanceof NodePropertyDec) {
                    NodePropertyDec propDec = (NodePropertyDec)inv.owner;

                    if (propDec.getStmt != null) {
                        Scope invGetScope = inv.getscope;

                        out.startProc(formSymbolicLabel(inv, "get"));
                        printStatement(propDec.getStmt, invGetScope);
                        out.endProc();
                    }
                    if (propDec.setStmt != null) {
                        Scope invSetScope = inv.setscope;

                        out.startProc(formSymbolicLabel(inv, "set"));
                        printStatement(propDec.setStmt, invSetScope);
                        out.endProc();
                    }
                }
                else throw new InternalErrorException("Unexpected " + inv);
            }

            i = cla.constructors.values().iterator();
            while (i.hasNext()) {
                inv = (Invokable)i.next();
                Scope invScope = inv.scope;

                if (!(inv.owner instanceof NodeSpecialFunctionDec) || ((NodeSpecialFunctionDec)inv.owner).isDes) throw new InternalErrorException(inv.owner + " should be a constructor");
                NodeSpecialFunctionDec specProc = (NodeSpecialFunctionDec)inv.owner;
                out.startProc(getUniqueName(inv));
                printStatement(((NodeSpecialFunctionDec)inv.owner).code, invScope);
                out.endProc();
            }
            i = cla.destructors.values().iterator();
            while (i.hasNext()) {
                inv = (Invokable)i.next();
                Scope invScope = inv.scope;

                if (!(inv.owner instanceof NodeSpecialFunctionDec) || !(((NodeSpecialFunctionDec)inv.owner).isDes)) throw new InternalErrorException(inv.owner + " should be a destructor");
                NodeSpecialFunctionDec specProc = (NodeSpecialFunctionDec)inv.owner;
                out.startProc(getUniqueName(inv));
                printStatement(((NodeSpecialFunctionDec)inv.owner).code, invScope);
                out.endProc();
            }
        }
    }

    public void printStatement(NodeStmt nodeStatement, Scope scope) {
        out.printComment(" +++ " + nodeStatement.toString());

		//Codeblock
        if (nodeStatement instanceof NodeCodeBlock) {
            Scope newScope = m.getScope(nodeStatement);

            Iterator i = ((NodeCodeBlock)nodeStatement).stmts.iterator();
            while (i.hasNext()) {
                printStatement((NodeStmt)i.next(), newScope);
            }
        }
        //Select
        else if (nodeStatement instanceof NodeSelectStmt) {
            NodeSelectStmt nsSelect = (NodeSelectStmt)nodeStatement;
            HashMap blockLabels = new HashMap(nsSelect.cases.size());
            evaluateExpression(nsSelect.expr, scope);

            String defLabel = null;

            Iterator i = nsSelect.cases.iterator();
            while (i.hasNext()) {
                NodeSelectCaseBlock nCaseBlock = (NodeSelectCaseBlock)i.next();
                String label = formSymbolicLabel(nCaseBlock, "case");

                blockLabels.put(label, nCaseBlock.code);

                if (nCaseBlock.def) {
                    defLabel = label;
                }
                else {
                    Iterator it = nCaseBlock.exprs.iterator();
                    while (it.hasNext()) {
                        NodeExpr nExpr = (NodeExpr)it.next();
                        NodeLiteral nl = nExpr.evaluateConstant(m, scope);
                        out.println("cmp #$" + nl.getIntegerValue());
                        out.println("beq " + label);
                    }
                }
            }
            if (defLabel != null) out.println("jmp " + defLabel);

            String afterLabel = formSymbolicLabel(nsSelect, "after");

            i = blockLabels.keySet().iterator();
            while (i.hasNext()) {
                String label = (String)i.next();
                NodeStmt ns = (NodeStmt)blockLabels.get(label);
                out.println(label, "");
                printStatement(ns, scope);
                out.println("jmp " + afterLabel);
            }
            out.println(afterLabel, "");
        }
        //If
        else if (nodeStatement instanceof NodeIfStmt) {
            NodeIfStmt nsIf = (NodeIfStmt)nodeStatement;
            evaluateExpression(nsIf.expr, scope);

            String elseLabel = formSymbolicLabel(nsIf, "else");
            String afterLabel = formSymbolicLabel(nsIf, "after");

            out.println("cmp #$0");
            if (nsIf.elsecode == null) {
                out.println("beq " + afterLabel);
                printStatement(nsIf.ifcode, scope);
            }
            else {
                out.println("beq " + elseLabel);
                printStatement(nsIf.ifcode, scope);
                out.println("jmp " + afterLabel);
                out.println(elseLabel, "");
                printStatement(nsIf.elsecode, scope);
            }
            out.println(afterLabel, "");
        }
        //Variable Declaration
        else if (nodeStatement instanceof NodeVariableDecStmt) {
            NodeVariableDec variableDec = ((NodeVariableDecStmt)nodeStatement).vd;
            if (variableDec.expr != null) {
				evaluateExpression(variableDec.expr, scope);
				String label = formSymbolicLabel(scope, variableDec.name);
				out.println("sta " + label);
			}
        }
        //For
        else if (nodeStatement instanceof NodeForStmt) {
            NodeForStmt nsFor = (NodeForStmt)nodeStatement;
            Scope newScope = m.getScope(nsFor);

            if (nsFor.init instanceof NodeVariableDec) {
				NodeVariableDec vd = (NodeVariableDec)nsFor.init;

				String label = formSymbolicLabel(newScope, vd.name);

                evaluateExpression(vd.expr, scope);
				out.println("sta " + label);
            }
            else { //(NodeExpr)
                evaluateExpression(((NodeExpr)nsFor.init), scope);
            }

            String startLabel = formSymbolicLabel(nsFor, "start");
            String afterLabel = formSymbolicLabel(nsFor, "after");

            out.println(startLabel, "");
            evaluateExpression(nsFor.cond, scope);
            out.println("cmp #$00");
            out.println("beq " + afterLabel);
            printStatement(nsFor.code, newScope);
            evaluateExpression(nsFor.iter, scope);
            out.println("jmp " + startLabel);
            out.println(afterLabel, "");
        }
		//Delete
        else if (nodeStatement instanceof NodeDeleteStmt) {
            NodeDeleteStmt nsDelete = (NodeDeleteStmt)nodeStatement;
            //TODO this
        }
        //Return
        else if (nodeStatement instanceof NodeReturnStmt) {
            NodeReturnStmt nsReturn = (NodeReturnStmt)nodeStatement;
            evaluateExpression(nsReturn.expr, scope);
            out.println("rts");
        }
        //Delegate Assignment
        else if (nodeStatement instanceof NodeDelegateAssignStmt) {
            NodeDelegateAssignStmt nsDelegateAssign = (NodeDelegateAssignStmt)nodeStatement;

            String dele = getUniqueName((Invokable)scope.findSymbol(m, nsDelegateAssign.deleg));
            String func = getUniqueName((Invokable)scope.findSymbol(m, nsDelegateAssign.func));

			//# means literal value, < means lower byte
			out.println("lda #<"+func+"");
			out.println("sta "+dele+"");
			out.println("lda #>"+func+"");
			out.println("sta "+dele+"+1");
        }
        //While
        else if (nodeStatement instanceof NodeWhileStmt) {
            NodeWhileStmt nsWhile = (NodeWhileStmt)nodeStatement;

            String startLabel = formSymbolicLabel(nsWhile, "start");
            String afterLabel = formSymbolicLabel(nsWhile, "after");

            out.println(startLabel, "");
            evaluateExpression(nsWhile.expr, scope);
            out.println("cmp #$00");
            out.println("beq " + afterLabel);
            printStatement(nsWhile.code, scope);
            out.println("jmp " + startLabel);
            out.println(afterLabel, "");
        }
        //Expression
        else if (nodeStatement instanceof NodeExprStmt) {
            NodeExprStmt nsExpr = (NodeExprStmt)nodeStatement;
            evaluateExpression(nsExpr.expr, scope);
        }
        else throw new InternalErrorException(nodeStatement + " is not a valid Statement");

        out.printComment(" --- " + nodeStatement.toString());
    }

    public void evaluateExpression(NodeExpr expr, Scope scope) {
        out.printComment(" +++ " + expr.toString());

		//Assignment
		if (expr instanceof NodeAssignOp) {
			NodeAssignOp noAssign = (NodeAssignOp)expr;
			NodeVariableRef varRef;
			if (noAssign.lhs instanceof NodeVariableRef) varRef = (NodeVariableRef)noAssign.lhs;
			else if (noAssign.lhs instanceof NodeArrayDeref) varRef = ((NodeArrayDeref)noAssign.lhs).varRef;
			else if (noAssign.lhs instanceof NodeDotOp) varRef = (NodeVariableRef)((NodeDotOp)noAssign.lhs).rhs;
			else throw new InternalErrorException("Invalid L value");

			Variable var = (Variable)scope.findSymbol(m, varRef.QRef);
			String varLabel = formSymbolicLabel(var.containingScope, var.name);

			evaluateExpression(noAssign.rhs, scope);

			out.println("sta " + varLabel);
		}
		//Condition
		else if (expr instanceof NodeConditionOp) {
			NodeConditionOp noCondition = (NodeConditionOp)expr;

			evaluateExpression(noCondition.cond, scope);

            String falseLabel = formSymbolicLabel(expr, "else");
            String afterLabel = formSymbolicLabel(expr, "after");

            out.println("cmp #0");
			out.println("beq " + falseLabel);

			evaluateExpression(noCondition.te, scope);

			out.println("jmp " + afterLabel);
			out.println(falseLabel, "");

			evaluateExpression(noCondition.fe, scope);

            out.println(afterLabel, "");
		}
		//Or
		else if (expr instanceof NodeOrOp) {
			NodeOrOp noOr = (NodeOrOp)expr;

			evaluateExpression(noOr.rhs, scope);
			out.println("sta temp1");
			evaluateExpression(noOr.lhs, scope);
			out.println("ora temp1");
		}
		//And
		else if (expr instanceof NodeAndOp) {
			NodeAndOp noAnd = (NodeAndOp)expr;

			evaluateExpression(noAnd.rhs, scope);
			out.println("sta temp1");
			evaluateExpression(noAnd.lhs, scope);
			out.println("and temp1");
		}
		//Equality Test
		else if (expr instanceof NodeEqualityTestOp) {
			NodeEqualityTestOp noEqualityTest = (NodeEqualityTestOp)expr;

			evaluateExpression(noEqualityTest.rhs, scope);
			out.println("sta temp1");
			evaluateExpression(noEqualityTest.lhs, scope);

			String trueLabel = formSymbolicLabel(expr, "true");
            String afterLabel = formSymbolicLabel(expr, "after");

			out.println("cmp temp1");
			out.println("beq "+trueLabel);
            if (noEqualityTest.equals) out.println("lda #$0");
            else out.println("lda #$ff");
			out.println("jmp "+afterLabel);
			out.println(trueLabel, "");
            if (noEqualityTest.equals) out.println("lda #$ff");
            else out.println("lda #$0");
			out.println(afterLabel, "");
		}
		//Relation Test
		else if (expr instanceof NodeRelationTestOp) {
			NodeRelationTestOp noRelationTest = (NodeRelationTestOp)expr;

			evaluateExpression(noRelationTest.rhs, scope);
			out.println("sta temp1");
			evaluateExpression(noRelationTest.lhs, scope);

			String trueLabel = formSymbolicLabel(expr, "true");
            String falseLabel = formSymbolicLabel(expr, "false");
            String afterLabel = formSymbolicLabel(expr, "after");

            out.println("cmp temp1");

            if (noRelationTest.equalTo) {
				out.println("beq "+trueLabel);
			}
			else {
				out.println("beq "+falseLabel);
			}

            if (noRelationTest.great) {
				out.println("bcs "+trueLabel);
			}
			else {
				out.println("bmi "+trueLabel);
			}

			out.println(falseLabel, "");
            out.println("lda #$0");
            out.println("jmp "+afterLabel);
			out.println(trueLabel, "");
            out.println("lda #$ff");
			out.println(afterLabel, "");
		}
		//Sum
		else if (expr instanceof NodeSumOp) {
			NodeSumOp noSum = (NodeSumOp)expr;

			evaluateExpression(noSum.rhs, scope);
			out.println("sta temp1");
			evaluateExpression(noSum.lhs, scope);

			if (!noSum.neg) out.println("adc temp1");
			else {
				out.println("sbc temp1");
   		        out.println("adc #0");
			}
		}
		//Product
		else if (expr instanceof NodeProductOp) {
			//FIXME: divide, modulus?
			NodeProductOp noProduct = (NodeProductOp)expr;

            String startLabel = formSymbolicLabel(expr, "start");
            String afterLabel = formSymbolicLabel(expr, "after");
            String jmpToLabel = formSymbolicLabel(expr, "jmpTo");
            String lhsValLabel = formSymbolicLabel(expr, "lhsVal");
            String rhsValLabel = formSymbolicLabel(expr, "rhsVal");

            out.println("jmp " + jmpToLabel);
            out.println(lhsValLabel, ".byte $0");
            out.println(rhsValLabel, ".byte $0");
			out.println(jmpToLabel, "");

			evaluateExpression(noProduct.lhs, scope);
			out.println("sta "+lhsValLabel);

			evaluateExpression(noProduct.rhs, scope);
			out.println("sta "+rhsValLabel);

			out.println("lda #0");
			out.println("sta temp1");
			out.println("lda #1");
			out.println("sta temp2");

			out.println(startLabel, "");
			out.println("lda temp1");
			out.println("adc "+lhsValLabel);
			out.println("adc #0");
			out.println("sta temp1");
			out.println("lda temp2");
			out.println("cmp "+rhsValLabel);
			out.println("beq "+afterLabel);
			out.println("inc temp2");
			out.println("jmp "+startLabel);
			out.println(afterLabel, "");

			out.println("lda temp1");
		}
		//Bit Shift
		else if (expr instanceof NodeBitShiftOp) {
			NodeBitShiftOp noBitShift = (NodeBitShiftOp)expr;
			boolean isLeft = noBitShift.left;

            String startLabel = formSymbolicLabel(expr, "start");
            String afterLabel = formSymbolicLabel(expr, "after");
            String lhsValLabel = formSymbolicLabel(expr, "lhsVal");
            out.println(lhsValLabel, "");
            String rhsValLabel = formSymbolicLabel(expr, "rhsVal");
            out.println(rhsValLabel, "");

			evaluateExpression(noBitShift.lhs, scope);
			out.println("sta "+lhsValLabel);

			evaluateExpression(noBitShift.rhs, scope);
			out.println("sta "+rhsValLabel);

			out.println("lda #$0");
			out.println("sta temp1");
			out.println("sta temp2");

			out.println(startLabel, "");
			out.println("lda "+lhsValLabel);
			if (isLeft) out.println("asl A");	// Arithmetic Shift Left
			else out.println("lsr A");			// Logical Shift Right
			out.println("sta temp1");
			out.println("lda temp2");
			out.println("cmp "+rhsValLabel);
			out.println("beq "+afterLabel);
			out.println("inc temp2");
			out.println("jmp "+startLabel);
			out.println(afterLabel, "");

			out.println("lda temp1");
		}
		//Dot
		else if (expr instanceof NodeDotOp) {
			//TODO: this
		}
		//Unary Negator
		else if (expr instanceof NodeUnaryNegOp) {
			//NodeExpr nunoExpr = ((NodeUnaryNegOp)expr).expr;
			evaluateExpression(((NodeUnaryNegOp)expr).expr, scope);
			out.println("eor #$ff");
		}
		//Increment
		else if (expr instanceof NodeIncrOp) {
			NodeIncrOp noIncr = (NodeIncrOp)expr;
			//FIXME: could be more than just a varRef
			NodeVariableRef varRef = (NodeVariableRef)noIncr.expr;
			Variable var = (Variable)scope.findSymbol(m, varRef.QRef);
			String varLabel = formSymbolicLabel(var.containingScope, var.name);
			out.println("lda "+varLabel);

			//evaluateExpression(noIncr.expr, scope);
			out.println("sta temp2");
			out.println("sta temp1");

			if (noIncr.positive) out.println("inc temp2");
			else out.println("dec temp2");

			if (noIncr.preOp) {
				out.println("lda temp2");
				out.println("sta temp1");
				out.println("sta "+varLabel);
			}
			else {
				out.println("lda temp2");
				out.println("sta "+varLabel);
				out.println("lda temp1");
			}
		}
		//ArrayDeref
		else if (expr instanceof NodeArrayDeref) {
			//TODO: Without a memory manager Arrays cannot be supported
			throw BackendException.unsupportedFeature("array referencing");
		}
		//Function Call
		else if (expr instanceof NodeFunctionCall) {
			NodeFunctionCall funcCall = (NodeFunctionCall)expr;
			Invokable inv = (Invokable)scope.findSymbol(m, funcCall.qref);
			String funcCallLabel = getUniqueName(inv);

			if (funcCall.params.members.size() > 8) {
				throw BackendException.unsupportedFeature("more than 8 function parameters");
			}

			int ifcCounter = 0;
			Iterator iFC = funcCall.params.members.iterator();
			while (iFC.hasNext()) {
				evaluateExpression((NodeExpr)iFC.next(), scope);
				out.println("sta parameterVal"+ifcCounter);
				ifcCounter++;
			}

			out.println("jsr " + funcCallLabel);
		}
		//Boolean Literal
		else if (expr instanceof NodeBooleanLiteral) {
			if (((NodeBooleanLiteral)expr).val) {
				out.println("lda #$ff");
			}
			else {
				out.println("lda #$00");
			}
		}
		//Character Literal
		else if (expr instanceof NodeCharacterLiteral) {
			int value = ((NodeCharacterLiteral)expr).getIntegerValue();
			out.println("lda #"+value);
		}
		//Float Literal
		else if (expr instanceof NodeFloatLiteral) {
			throw BackendException.unsupportedFeature("float types");
		}
		//Integer Literal
		else if (expr instanceof NodeIntegerLiteral) {
			int value = ((NodeIntegerLiteral)expr).val;
			out.println("lda #"+value);
		}
		//String Literal
		else if (expr instanceof NodeStringLiteral) {
			//TODO: Without a memory manager Objects cannot be supported
			throw BackendException.unsupportedFeature("string types");
		}
		//New
		else if (expr instanceof NodeNewOp) {
			//TODO: Without a memory manager Objects cannot be supported
			throw BackendException.unsupportedFeature("object creation");
		}
		//Variable Reference
		else if (expr instanceof NodeVariableRef) {
			NodeVariableRef varRef = (NodeVariableRef)expr;
			Variable var = (Variable)scope.findSymbol(m, varRef.QRef);
			String varLabel = formSymbolicLabel(var.containingScope, var.name);

			out.println("lda "+varLabel);
		}
		else {
			if (expr != null) throw new InternalErrorException(expr+" is not a valid expression");
		}
        out.printComment(" --- " + expr.toString());
	}

    public String getUniqueName(Invokable i) {
        StringBuffer sb = new StringBuffer();

        sb.append("_");
        sb.append(i.symbolKind().substring(0,4));
        sb.append("@");
        if (i.ownerPackage != null) {

            Iterator it = i.ownerPackage.packageRef.parts.iterator();
            sb.append(it.next());
            while (it.hasNext()) {
                sb.append('$');
                sb.append(it.next());
            }
        }
        sb.append("@");
        if (i.ownerClass != null) {
            sb.append(i.ownerClass.name);
        }
        sb.append("@");
        sb.append(i.name);

        return sb.toString();
    }

    public int getUniq(Node n) {
        if (uniqtable.containsKey(n)) {
            return ((Integer)uniqtable.get(n)).intValue();
        }
        else {
            uniqtable.put(n, new Integer(nextuniq));
            return nextuniq++;
        }
    }

    public int getUniq(Scope s) {
        if (uniqtable.containsKey(s)) {
            return ((Integer)uniqtable.get(s)).intValue();
        }
        else {
            uniqtable.put(s, new Integer(nextuniq));
            return nextuniq++;
        }
    }

    public int sizeOf(Type t, Module m) {
        if (t.isIntrinsic()) {
            if (t == Type.VOID) return 0;
            if (t == Type.FLOAT) throw BackendException.unsupportedFeature("float types");
            if (t == Type.INT || t == Type.CHAR) {
                if (t == Type.INT && t.size != 8) throw BackendException.unsupportedFeature("integer types of size "+t.size);
                return 1;
            }
            if (t == Type.BOOL) return 1;
        }
        else {
			throw BackendException.unsupportedFeature("pointer types");
		}

		throw BackendException.unsupportedFeature("Bad sizeOf Type");

        /*int retSize = 0;
        Class typeClass = t.getClass(m);

        java.util.Iterator i = typeClass.scope.vars.keySet().iterator();

        while (i.hasNext()) {
            retSize += sizeOf((Variable)i.next(), m);
        }

        Class parent = typeClass.parent;

        java.util.Iterator pi;

        while (parent != null) {

            pi = parent.scope.vars.keySet().iterator();

            while (pi.hasNext()) {
                retSize += sizeOf((Variable)i.next(), m);
            }

            parent = parent.parent;
        }

        return retSize;*/

    }
    public int sizeOf(Variable v, Module m) {
        return sizeOf(v.type, m);
    }

    public String formSymbolicLabel(Node n, String sym) {
        StringBuffer sb = new StringBuffer(10);
        sb.append("__");
        sb.append(getUniq(n));
        sb.append("$");
        sb.append(sym);
        return sb.toString();
    }

    public String formSymbolicLabel(Invokable inv, String sym) {
        StringBuffer sb = new StringBuffer(10);
        sb.append(getUniqueName(inv));
        sb.append("$");
        sb.append(sym);
        return sb.toString();
    }

    public String formSymbolicLabel(Scope s, String sym) {
        StringBuffer sb = new StringBuffer(10);
        sb.append("__");
        if (s != null) sb.append(getUniq(s));
        sb.append("$");
        sb.append(sym);
        return sb.toString();
    }

    public class ASMWriter {
        PrintStream pw;

        public ASMWriter(PrintStream pw) {
            this.pw = pw;
        }

        public void println(String s) {
            pw.println("\t"+s);
        }
        public void println(String label, String s) {
            pw.println(label+":\t"+s);
        }
        public void printSegment(String name) {
			pw.println(".segment\t\""+name+"\"");
		}
        public void startProc(String name) {
            printSegment("CODE");
            pw.println("\n.proc\t"+name);
        }
        public void endProc() {
            pw.println(".endproc");
        }
        public void imp(String name) {
            pw.println("\t.import "+name);
        }
        public void exp(String name) {
            pw.println("\t.export "+name);
        }
        public void printHeader() {
            pw.println("; Generated by FooCompile, 6502 backend");
            pw.println("\t.fopt compiler,\"FooCompile\"");
            pw.println("\t.feature at_in_identifiers");
            pw.println("\t.feature dollar_in_identifiers");
        }
        public void printComment(String comment) {
            pw.println("; " + comment);
        }
    }

	public static class BackendException extends RuntimeException {
		public String message;

		public BackendException(String message) {
			super(message);
			this.message = message;
		}

		public static BackendException unsupportedFeature(String feature) {
			return new BackendException("The 6502 backend does not support the use of " + feature);
		}

	}

}
