
package uk.me.alexhaig.FooCompile;

import java.io.*;

public class Scanner {
    private Reader is;
    private char peeked;
    private boolean eof = false;

    private int line = 1;
    private int column = 1;

    public Scanner(Reader is) {
        this.is = is;
    }

    public char read() {
        char ret = (peeked == 0 ? peek() : peeked);
        peeked = 0;
        if (ret == '\n') {
            column = 1;
            line++;
        } else {
            column++;
        }
        return ret;
    }

    public char peek() {
        if (peeked != 0) return peeked;
        try {
            int r = is.read();
            if (r == -1) {
                eof = true;
                return peeked = 0;
            }
            else return peeked = (char)r;

        }
        catch (IOException e) {
            return 0;
        }
    }

    public boolean atEOF() {
        return eof;
    }

    public int getLine() {
        return line;
    }

    public int getColumn() {
        return column;
    }

}
