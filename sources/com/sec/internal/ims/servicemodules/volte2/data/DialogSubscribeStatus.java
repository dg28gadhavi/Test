package com.sec.internal.ims.servicemodules.volte2.data;

public class DialogSubscribeStatus {
    private int mPhoneId;
    private String mReasonPhrase;
    private int mStatusCode;

    public DialogSubscribeStatus(int i, int i2, String str) {
        this.mPhoneId = i;
        this.mStatusCode = i2;
        this.mReasonPhrase = str;
    }

    public int getPhoneId() {
        return this.mPhoneId;
    }

    public int getStatusCode() {
        return this.mStatusCode;
    }

    public String toString() {
        return "Phone#" + this.mPhoneId + " statusCode : " + this.mStatusCode + " reasonPhrase : " + this.mReasonPhrase;
    }
}
