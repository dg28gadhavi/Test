package com.sec.internal.ims.gba;

public class GbaException extends Exception {
    public static final int GBA_FAILURE_REASON_END = 99;
    public static final int GBA_FAILURE_REASON_FEATURE_NOT_READY = 2;
    public static final int GBA_FAILURE_REASON_FEATURE_NOT_SUPPORTED = 1;
    public static final int GBA_FAILURE_REASON_INCORRECT_NAF_ID = 4;
    public static final int GBA_FAILURE_REASON_NETWORK_FAILURE = 3;
    public static final int GBA_FAILURE_REASON_SECURITY_PROTOCOL_NOT_SUPPORTED = 5;
    public static final int GBA_FAILURE_REASON_UNKNOWN = 0;
    private int mCode;

    public GbaException() {
    }

    public GbaException(String str) {
        super(str);
        this.mCode = 0;
    }

    public GbaException(String str, int i) {
        super(str);
        this.mCode = i;
    }

    public int getCode() {
        return this.mCode;
    }
}
