package com.sec.internal.ims.entitlement.nsds.ericssonnsds.flow;

public class NSDSCommonParameters {
    private String mAkaToken;
    private String mChallengeResponse;
    private String mDeviceId;
    private String mImsiEap;

    public NSDSCommonParameters(String str, String str2, String str3, String str4) {
        this.mChallengeResponse = str;
        this.mAkaToken = str2;
        this.mImsiEap = str3;
        this.mDeviceId = str4;
    }

    public String getChallengeResponse() {
        return this.mChallengeResponse;
    }

    public String getAkaToken() {
        return this.mAkaToken;
    }

    public String getImsiEap() {
        return this.mImsiEap;
    }

    public String getDeviceId() {
        return this.mDeviceId;
    }
}
