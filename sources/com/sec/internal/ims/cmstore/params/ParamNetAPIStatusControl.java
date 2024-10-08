package com.sec.internal.ims.cmstore.params;

public class ParamNetAPIStatusControl {
    public final boolean mIsDefaultMsgAppNative;
    public final boolean mIsMsgAppForeground;
    public final boolean mIsNetworkValid;
    public final boolean mIsOMANetAPIRunning;
    public final boolean mIsProvisionSuccess;
    public final boolean mIsServicePaused;
    public final boolean mIsUserDeleteAccount;

    public ParamNetAPIStatusControl(boolean z, boolean z2, boolean z3, boolean z4, boolean z5, boolean z6, boolean z7) {
        this.mIsMsgAppForeground = z;
        this.mIsNetworkValid = z2;
        this.mIsOMANetAPIRunning = z3;
        this.mIsDefaultMsgAppNative = z4;
        this.mIsUserDeleteAccount = z5;
        this.mIsProvisionSuccess = z6;
        this.mIsServicePaused = z7;
    }

    public String toString() {
        return "ParamNetAPIStatusControl [mIsMsgAppForeground= " + this.mIsMsgAppForeground + " mIsNetworkValid = " + this.mIsNetworkValid + " mIsOMANetAPIRunning = " + this.mIsOMANetAPIRunning + "mIsDefaultMsgAppNative = " + this.mIsDefaultMsgAppNative + "mIsUserDeleteAccount = " + this.mIsUserDeleteAccount + " mIsProvisionSuccess = " + this.mIsProvisionSuccess + "]";
    }
}
