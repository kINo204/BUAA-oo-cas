package tool;

public class Debuger {

    private static final boolean
            openDebug = false;

    public static void println(String str) {
        if (openDebug) {
            System.out.println(str);
        }
    }

}
