package com.sec.internal.constants.ims.servicemodules.volte2;

public class CmcInfoEvent {
    private String mExternalCallId;
    private int mRecordEvent;

    public CmcInfoEvent() {
        this.mRecordEvent = -1;
        this.mExternalCallId = "";
    }

    public CmcInfoEvent(int i, String str) {
        this.mRecordEvent = i;
        this.mExternalCallId = str;
    }

    public int getRecordEvent() {
        return this.mRecordEvent;
    }

    public void setRecordEvent(int i) {
        this.mRecordEvent = i;
    }

    public String getExternalCallId() {
        return this.mExternalCallId;
    }

    public void setExternalCallId(String str) {
        this.mExternalCallId = str;
    }
}
