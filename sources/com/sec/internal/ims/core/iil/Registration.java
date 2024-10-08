package com.sec.internal.ims.core.iil;

public class Registration {
    public static final int CDPN_REGISTERED = 32;
    public static final int RCS_REGISTERED = 4;
    public static final int SMS_OVER_IMS_REGISTERED = 2;
    public static final int VOLTE_REGISTERED = 1;
    public static final int VT_REGISTERED = 8;
    private int mDereiReasonCode;
    private int mEcmpMode;
    private int mEpdgMode;
    private int mError;
    private String mErrorMsg;
    private int mFeatureMask;
    private String mFeatureTags;
    private String mImpu;
    private int mLimitedMode;
    private int mPdnType;
    private int mRat;

    public Registration(int i, int i2, int i3, int i4, int i5, int i6) {
        this.mError = 0;
        this.mDereiReasonCode = 0;
        this.mErrorMsg = null;
        this.mFeatureTags = "";
        this.mImpu = null;
        this.mFeatureMask = i;
        this.mPdnType = i2;
        this.mEcmpMode = i3;
        this.mLimitedMode = i4;
        this.mEpdgMode = i5;
        this.mRat = i6;
    }

    public Registration() {
        this.mErrorMsg = null;
        this.mFeatureTags = "";
        this.mImpu = null;
        this.mFeatureMask = 0;
        this.mPdnType = 0;
        this.mEcmpMode = 0;
        this.mLimitedMode = 0;
        this.mEpdgMode = 0;
        this.mRat = 0;
        this.mError = 0;
        this.mDereiReasonCode = 0;
    }

    public void setSipError(int i) {
        this.mError = i;
    }

    public int getSipError() {
        return this.mError;
    }

    public void setDeregiReasonCode(int i) {
        this.mDereiReasonCode = i;
    }

    public int getDeregiReasonCode() {
        return this.mDereiReasonCode;
    }

    public void setErrorMessage(String str) {
        this.mErrorMsg = str;
    }

    public String getErrorMessage() {
        return this.mErrorMsg;
    }

    public void setFeatureMask(int i) {
        this.mFeatureMask = i;
    }

    public int getFeatureMask() {
        return this.mFeatureMask;
    }

    public void setNetworkType(int i) {
        this.mPdnType = i;
    }

    public int getNetworkType() {
        return this.mPdnType;
    }

    public void setEcmpMode(int i) {
        this.mEcmpMode = i;
    }

    public int getEcmpMode() {
        return this.mEcmpMode;
    }

    public void setLimitedMode(int i) {
        this.mLimitedMode = i;
    }

    public int getLimitedMode() {
        return this.mLimitedMode;
    }

    public void setEpdgMode(int i) {
        this.mEpdgMode = i;
    }

    public int getEpdgMode() {
        return this.mEpdgMode;
    }

    public void setFeatureTags(String str) {
        this.mFeatureTags = str;
    }

    public String getFeatureTags() {
        return this.mFeatureTags;
    }

    public void setImpu(String str) {
        this.mImpu = str;
    }

    public String getImpu() {
        return this.mImpu;
    }

    public void setRegiRat(int i) {
        this.mRat = i;
    }

    public int getRegiRat() {
        return this.mRat;
    }
}
