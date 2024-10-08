package com.sec.internal.ims.servicemodules.volte2.data;

public class SIPDataEvent {
    boolean mIsRequest;
    String mSipMessage;

    public String getSipMessage() {
        return this.mSipMessage;
    }

    public boolean getIsRequest() {
        return this.mIsRequest;
    }

    public SIPDataEvent(String str, boolean z) {
        this.mSipMessage = str;
        this.mIsRequest = z;
    }
}
