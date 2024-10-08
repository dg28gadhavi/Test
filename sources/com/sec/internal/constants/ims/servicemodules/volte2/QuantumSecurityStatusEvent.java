package com.sec.internal.constants.ims.servicemodules.volte2;

public class QuantumSecurityStatusEvent {
    QuantumEvent mEvent;
    String mQtSessionId;
    int mSessionId;

    public enum QuantumEvent {
        FALLBACK_TO_NORMAL_CALL,
        SUCCESS,
        NOTIFY_SESSION_ID
    }

    public QuantumSecurityStatusEvent(int i, QuantumEvent quantumEvent, String str) {
        this.mSessionId = i;
        this.mEvent = quantumEvent;
        this.mQtSessionId = str;
    }

    public int getSessionId() {
        return this.mSessionId;
    }

    public QuantumEvent getEvent() {
        return this.mEvent;
    }

    public String getQtSessionId() {
        return this.mQtSessionId;
    }
}
