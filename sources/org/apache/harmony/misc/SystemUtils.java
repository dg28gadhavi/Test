package org.apache.harmony.misc;

public class SystemUtils {
    private static int os;

    public static int getOS() {
        if (os == 0) {
            String substring = System.getProperty("os.name").substring(0, 3);
            if (substring.compareToIgnoreCase("win") == 0) {
                os = 1;
            } else if (substring.compareToIgnoreCase("lin") == 0) {
                os = 2;
            } else {
                os = -1;
            }
        }
        return os;
    }
}
