package com.sec.internal.constants.ims.os;

public enum EmcBsIndication {
    UNKNOWN,
    SUPPORTED,
    NOT_SUPPORTED;

    public static EmcBsIndication translateEmcbs(int i) {
        if (i == 2) {
            return SUPPORTED;
        }
        if (i == 3) {
            return NOT_SUPPORTED;
        }
        return UNKNOWN;
    }

    public static EmcBsIndication translateEmcbs(boolean z) {
        return z ? SUPPORTED : NOT_SUPPORTED;
    }
}
