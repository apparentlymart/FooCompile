
package uk.me.alexhaig.FooCompile;

import java.io.*;

public class IndentingWriter {
    int ind = 0;
    PrintStream pw;

    public IndentingWriter(PrintStream pw) {
        this.pw = pw;
    }

    public void indent() {
        ind++;
    }

    public void outdent() {
        if (ind >= 0) ind--;
    }

    public void println(String s) {
        for (int i = 0; i < ind; i++) {
            pw.print("    ");
        }
        pw.println(s);
    }

    public void print(String s) {
        for (int i = 0; i < ind; i++) {
            pw.print("    ");
        }
	pw.print(s);
    }

    public void inprintln(String s) {
	pw.println(s);
    }

    public void inprint(String s) {
	pw.print(s);
    }
}
