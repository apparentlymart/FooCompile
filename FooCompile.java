
import java.io.*;
import java.util.Vector;
import java.util.Iterator;
import uk.me.alexhaig.FooCompile.*;
import uk.me.alexhaig.FooCompile.Type;
import java.lang.reflect.*;
import java.util.Hashtable;

public class FooCompile {

    public static void main(String args[]) throws FileNotFoundException {
        int op = -1;
        String infile = null;
        String output = null;
        boolean isbeopts = false;
        String be = null;
        Hashtable beopts = new Hashtable();
        Boolean nullvalue = new Boolean(true);

        Iterator i = java.util.Arrays.asList(args).iterator();

        while (i.hasNext()) {
            String arg = (String)i.next();
            if (!isbeopts) {
                if (arg.substring(0,2).equals("--")) {
                    if (arg.substring(2).equals("help")) {
                        usage();
                    }
                    if (arg.substring(2,7).equals("mode=")) {
                        if (op != -1) usage();
                        String mo = arg.substring(7);
                        if (mo.equals("code")) {
                            op=3;
                        }
                        else if (mo.equals("scopes")) {
                            op=2;
                        }
                        else if (mo.equals("symbols")) {
                            op=1;
                        }
                        else if (mo.equals("parsetree")) {
                            op=0;
                        }
                        else usage();
                    }
                    if (arg.substring(2,9).equals("target=")){
                        be = arg.substring(9);
                        isbeopts = true;
                    }
                }
                else if (arg.substring(0,1).equals("-")) {
                    usage();
                }
                else {
                    if (infile != null) {
                        usage();
                    }
                    infile = arg;
                }
            }
            else {
                if (arg.substring(0,2).equals("--")) {
                    int equat = arg.indexOf('=');
                    String key = arg.substring(2,equat);
                    if (equat == -1) {
                        beopts.put(key, nullvalue);
                    }
                    else {
                        beopts.put(key, arg.substring(equat+1));
                    }
                }
                else usage();
            }
        }

        if (infile == null) usage();
        if (op == -1) op = 3;
        if (be == null || be.equals("")) usage();
        String ben = "uk.me.alexhaig.FooCompile.Backend_"+be;
        BufferedReader is = new BufferedReader(new FileReader(infile));
        Module f = null;
        try {
            f = Module.fromStream(is);
        }
        catch (ParserException e) {
            System.err.println(e.getSummary()+" at line "+e.getLine()+", column "+e.getColumn());
            System.err.println(e.getDetail()+"\n\n\n");
            e.printStackTrace();
            System.exit(1);
        }

        IndentingWriter w = new IndentingWriter(System.out);

        if (op == 0) {
            f.printParseTree(w);
        }
        else if (op > 0) {
            try {
                Checker.addPackage(f, new NodeQRef("fooutil"));
                uk.me.alexhaig.FooCompile.Package p = ((uk.me.alexhaig.FooCompile.Package)((uk.me.alexhaig.FooCompile.Package)f.packages.get("fooutil")));
                Invokable in = new Invokable("putCh", new Scope(null), Invokable.FUNCTION, Type.VOID, null, p);
                in.params.add(new Variable("ch", Type.CHAR));
                p.addInvokable(in);
                in = new Invokable("putInt", new Scope(null), Invokable.FUNCTION, Type.VOID, null, p);
                in.params.add(new Variable("in", Type.INT));
                p.addInvokable(in);
                in = new Invokable("doNothing", new Scope(null), Invokable.FUNCTION, Type.INT, null, p);
                p.addInvokable(in);
                in = new Invokable("screenOn", new Scope(null), Invokable.FUNCTION, Type.VOID, null, p);
                p.addInvokable(in);
                in = new Invokable("screenOff", new Scope(null), Invokable.FUNCTION, Type.VOID, null, p);
                p.addInvokable(in);
                in = new Invokable("graphicsOn", new Scope(null), Invokable.FUNCTION, Type.VOID, null, p);
                p.addInvokable(in);
                in = new Invokable("toChar", new Scope(null), Invokable.FUNCTION, Type.CHAR, null, p);
                in.params.add(new Variable("in", Type.INT));
                p.addInvokable(in);
                in = new Invokable("setBorderColor", new Scope(null), Invokable.FUNCTION, Type.VOID, null, p);
                in.params.add(new Variable("in", Type.INT));
                p.addInvokable(in);
                in = new Invokable("setPixel", new Scope(null), Invokable.FUNCTION, Type.VOID, null, p);
                in.params.add(new Variable("lowX", Type.INT));
                in.params.add(new Variable("highX", Type.INT));
                in.params.add(new Variable("Y", Type.INT));
                p.addInvokable(in);
                in = new Invokable("clearPixel", new Scope(null), Invokable.FUNCTION, Type.VOID, null, p);
                in.params.add(new Variable("lowX", Type.INT));
                in.params.add(new Variable("highX", Type.INT));
                in.params.add(new Variable("Y", Type.INT));
                p.addInvokable(in);
                Checker.check(f);
            }
            catch (CheckerException e) {
                System.err.println(e.getSummary()+" at line "+e.getLine()+", column "+e.getColumn());
                System.err.println(e.getDetail()+"\n\n\n");
                e.printStackTrace();
                System.exit(1);
            }
            if (op == 1) {
                f.printSymbolTable(w);
            } else if (op ==2) {
                f.printScopeTable();
            } else if (op == 3) {
                Backend beo = null;
                try {
                    java.lang.Class bec = java.lang.Class.forName(ben);
                    java.lang.Class[] conspara = new java.lang.Class[] { f.getClass(), System.out.getClass() };
                    Object[] consarg = new Object[] { f, System.out };
                    Constructor cons = bec.getConstructor(conspara);
                    beo = (Backend)cons.newInstance(consarg);
                }
                catch (Exception e) {
                    System.err.println("Unable to load backend '"+be+"'");
                    e.printStackTrace();
                    System.exit(1);
                }
                beo.generateCode();
            }
        }
    }

    public static void usage() {
        usage(1);
    }

    public static void usage(int stat) {
        System.out.println("Usage: FooCompile [options] <sourcefile> --backend=<backend> [backend-options]");
        System.exit(stat);
    }
}
