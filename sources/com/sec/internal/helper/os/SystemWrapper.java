package com.sec.internal.helper.os;

public class SystemWrapper {
    public static void exit(int i) {
        System.exit(i);
    }

    public static void explicitGc() {
        System.gc();
        System.runFinalization();
    }
}
