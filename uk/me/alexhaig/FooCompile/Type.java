
package uk.me.alexhaig.FooCompile;

public class Type {
    NodeQRef name = null;
    boolean isIntrinsic = false;
    TokenKeyword intrinsicType = null;
    int size = -1;
    int arraymod = 0;

    //Non-intrinsic type's class is set when the symbol table is built
    Class cla = null;

    public static final Type MAGICSTRING = new Type(new NodeQRef(1));
    public static final Type INT = new Type(TokenKeyword.INT);
    public static final Type FLOAT = new Type(TokenKeyword.FLOAT);
    public static final Type BOOL = new Type(TokenKeyword.BOOL);
    public static final Type CHAR = new Type(TokenKeyword.CHAR);
    public static final Type VOID = new Type(TokenKeyword.VOID);

    public Type(NodeQRef name) {
        this.name = name;
        this.isIntrinsic = false;
    }
    public Type(NodeClass fromClass) {
        this.name = new NodeQRef(fromClass.name);
        this.isIntrinsic = false;
    }
    public Type(NodeQRef name, int arraymod) {
        this.name = name;
        this.isIntrinsic = false;
        this.arraymod = arraymod;
    }
    public Type(TokenKeyword intrinsicType) {
        this.intrinsicType = intrinsicType;
        this.isIntrinsic = true;
    }
    public Type(TokenKeyword intrinsicType, int size) {
        this.intrinsicType = intrinsicType;
        this.size = size;
        this.isIntrinsic = true;
    }
    public Type(TokenKeyword intrinsicType, int size, int arraymod) {
        this.intrinsicType = intrinsicType;
        this.size = size;
        this.arraymod = arraymod;
        this.isIntrinsic = true;
    }
    public Type(Type t) {
        this.intrinsicType = t.intrinsicType;
        this.size = t.size;
        this.arraymod = t.arraymod;
        this.isIntrinsic = t.isIntrinsic;
		this.name = t.name;
		this.cla = t.cla;
    }

    public Class getClass(Module m) {
		if (isIntrinsic) return null;
		if (cla == null) cla = m.findClass(name);
		return cla;
	}

    public boolean isIntrinsic() {
        return this.isIntrinsic;
    }

    public boolean sameBase(Module m, Type t, Scope s) {
        if (isIntrinsic != t.isIntrinsic) return false;
        if (isIntrinsic) {
            return intrinsicType == t.intrinsicType && size == t.size;
        } else {
            Symbol thatS = s.findSymbol(m, t.name);
            Symbol thisS = s.findSymbol(m, this.name);

            if (thisS == null) CheckerException.symbolNotFound(this.name);
            if (thatS == null) CheckerException.symbolNotFound(t.name);

            return thisS.name.equals(thatS.name);
        }
    }

    public boolean canBe(Module m, Type t) {
		if (isIntrinsic != t.isIntrinsic) return false;
		if(t.arraymod != this.arraymod) return false;

		if (isIntrinsic) {
			if (this.intrinsicType == t.intrinsicType) {
				if(isNumeric()) {
					return t.size >= this.size;
				}
				else return true;
			}
			else if (t.intrinsicType == TokenKeyword.FLOAT && this.intrinsicType == TokenKeyword.INT) {
				return true;
			}
			else return false;
		}
		else {
			NodeQRef classRef = new NodeQRef();
			int stop = t.name.parts.size();
			for (int i = 0; i < stop; i++) {
				classRef.parts.add(t.name.parts.elementAt(i));
			}
			Class tClass = m.findClass(classRef);

			classRef = new NodeQRef();
			stop = this.name.parts.size();
			for (int i = 0; i < stop; i++) {
				classRef.parts.add(this.name.parts.elementAt(i));
			}
			Class thisClass = m.findClass(classRef);

			Class cla = thisClass;
			while (cla != null) {
				if (cla.name == tClass.name) return true;

				cla = cla.parent;
			}
			return false;
		}
	}

    public boolean canStore(NodeNumberLiteral n) {
        if (!isIntrinsic) return false;
        if (intrinsicType == TokenKeyword.INT) {
            if (n instanceof NodeIntegerLiteral) {
                if (size == -1) return true;
                int testvalue = ((NodeIntegerLiteral)n).val;
                return (testvalue < Math.pow(2, size));
            }
            else if (n instanceof NodeFloatLiteral) {
                if (size == -1) return true;
                double testvalue = ((NodeFloatLiteral)n).val;
                return (testvalue < Math.pow(2, size));
            }
        }
        else if (intrinsicType == TokenKeyword.FLOAT) {
            // TODO: Figure out if float capacity can be confirmed
            //   at compile time. (Probably not)
            return true;
        }
        return false;
    }

    public boolean isNumeric() {
		if (isIntrinsic && arraymod == 0) {
			return (intrinsicType == TokenKeyword.INT || intrinsicType == TokenKeyword.CHAR || intrinsicType == TokenKeyword.FLOAT);
		}
		else return false;
    }

    public boolean equals() {
		throw new InternalErrorException("Call equals(Module,Type,Scope)");
	}
    public boolean equals(Module m, Type t, Scope s) {
        return sameBase(m, t, s) && arraymod == t.arraymod;
    }

    public String toString() {
        StringBuffer sb = new StringBuffer();
        if (isIntrinsic) {
            sb.append(intrinsicType.name);
            if (size > 0) {
                sb.append("<" + size + ">");
            }
        }
        else {
            sb.append(name.name);
        }
        for (int i = 0; i < arraymod; i++) {
            sb.append("[]");
        }

        return sb.toString();

    }
}
