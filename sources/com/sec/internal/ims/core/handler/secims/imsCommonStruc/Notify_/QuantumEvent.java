package com.sec.internal.ims.core.handler.secims.imsCommonStruc.Notify_;

public final class QuantumEvent {
    public static final int FALLBACK_TO_NORMAL_CALL = 1;
    public static final int NOTIFY_SESSION_ID = 3;
    public static final int SUCCESS = 2;
    public static final int UN = 0;
    public static final String[] names = {"UN", "FALLBACK_TO_NORMAL_CALL", "SUCCESS", "NOTIFY_SESSION_ID"};

    private QuantumEvent() {
    }

    public static String name(int i) {
        return names[i];
    }
}
