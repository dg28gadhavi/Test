package com.sec.internal.constants.ims.os;

public enum VoPsIndication {
    UNKNOWN,
    SUPPORTED,
    NOT_SUPPORTED;

    public static VoPsIndication translateVops(String str) {
        int i;
        try {
            i = Integer.parseInt(str);
        } catch (NumberFormatException unused) {
            i = 1;
        }
        return translateVops(i);
    }

    public static VoPsIndication translateVops(int i) {
        if (i == 2) {
            return SUPPORTED;
        }
        if (i == 3) {
            return NOT_SUPPORTED;
        }
        return UNKNOWN;
    }

    public static VoPsIndication translateVops(boolean z, boolean z2) {
        if (z2) {
            return UNKNOWN;
        }
        return z ? SUPPORTED : NOT_SUPPORTED;
    }
}
