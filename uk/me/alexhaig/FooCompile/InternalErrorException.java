
package uk.me.alexhaig.FooCompile;

// Exception class for cases which should be impossible
// If this is thrown it indicates a bug.

public class InternalErrorException extends RuntimeException {
    public InternalErrorException(String m) {
        super(m);
    }
}
