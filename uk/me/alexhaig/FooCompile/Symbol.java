
package uk.me.alexhaig.FooCompile;

public abstract class Symbol {
	String name;
	Scope containingScope = null;

	public void setScope(Scope containingScope) {
			this.containingScope = containingScope;
	}

    public String symbolKind() {
        throw new InternalErrorException("No overridden symbolKind for "+this);
	}
}
