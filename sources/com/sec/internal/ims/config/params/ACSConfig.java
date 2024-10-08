package com.sec.internal.ims.config.params;

public class ACSConfig {
    private boolean mAcsCompleted;
    private int mAcsLastError;
    private int mAcsVersion;
    private boolean mIsTriggeredByNrcr;
    private boolean mNeedForceAcs;
    private boolean mRcsBlocked;
    private boolean mRcsDormantMode;

    public boolean isRcsDormantMode() {
        return this.mRcsDormantMode;
    }

    public void setRcsDormantMode(boolean z) {
        this.mRcsDormantMode = z;
    }

    public boolean isRcsDisabled() {
        return this.mRcsBlocked;
    }

    public void disableRcsByAcs(boolean z) {
        this.mRcsBlocked = z;
    }

    public int getAcsLastError() {
        return this.mAcsLastError;
    }

    public void setAcsLastError(int i) {
        this.mAcsLastError = i;
    }

    public int getAcsVersion() {
        return this.mAcsVersion;
    }

    public void setAcsVersion(int i) {
        this.mAcsVersion = i;
    }

    public boolean isAcsCompleted() {
        return this.mAcsCompleted;
    }

    public boolean needForceAcs() {
        return this.mNeedForceAcs;
    }

    public void setAcsCompleteStatus(boolean z) {
        this.mAcsCompleted = z;
    }

    public void setForceAcs(boolean z) {
        this.mNeedForceAcs = z;
    }

    public void setIsTriggeredByNrcr(boolean z) {
        this.mIsTriggeredByNrcr = z;
    }

    public boolean isTriggeredByNrcr() {
        return this.mIsTriggeredByNrcr;
    }

    public void resetAcsSettings() {
        this.mAcsCompleted = false;
        this.mNeedForceAcs = true;
    }

    public void clear() {
        this.mAcsVersion = 0;
        this.mAcsLastError = 0;
        this.mAcsCompleted = false;
        this.mRcsDormantMode = false;
        this.mNeedForceAcs = true;
        this.mRcsBlocked = false;
    }
}
