package com.sec.internal.ims.core.imsdc;

import android.util.Log;

public class IdcImsCallSessionData {
    public static final String NO_DATA = "NO_DATA";
    String LOG_TAG = IdcImsCallSessionData.class.getSimpleName();
    State mCurrentState = State.IDLE;
    private boolean mIsNotifiedTelecomCallId = false;
    private String mLocalBdcTlsId = "";
    private String mLocalSdp = "";
    private String mRemoteSdp = "";
    private String mTelecomCallId = "";

    public enum State {
        IDLE,
        NEGOTIATING,
        NEGOTIATED,
        MODIFYING,
        MODIFY_REQUESTED
    }

    public void transitState(State state) {
        if (this.mCurrentState != state) {
            String str = this.LOG_TAG;
            Log.i(str, "transitState: new [" + state + "] <- old [" + this.mCurrentState + "] ");
            this.mCurrentState = state;
        }
    }

    public State getCurrentState() {
        return this.mCurrentState;
    }

    public void setLocalBdcSdp(String str) {
        this.mLocalSdp = str;
    }

    public String getLocalBdcSdp() {
        return this.mLocalSdp;
    }

    public void setRemoteBdcSdp(String str) {
        this.mRemoteSdp = str;
    }

    public String getRemoteBdcSdp() {
        return this.mRemoteSdp;
    }

    public String getTelecomCallId() {
        return this.mTelecomCallId;
    }

    public void setTelecomCallId(String str) {
        this.mTelecomCallId = str;
    }

    public String getLocalBdcTlsId() {
        return this.mLocalBdcTlsId;
    }

    public void setLocalBdcTlsId(String str) {
        this.mLocalBdcTlsId = str;
    }

    public boolean getIsNotifiedTelecomCallId() {
        return this.mIsNotifiedTelecomCallId;
    }

    public void setIsNotifiedTelecomCallId(boolean z) {
        this.mIsNotifiedTelecomCallId = z;
    }

    public String toString() {
        return "IdcImsCallSessionData [mTelecomCallId=" + this.mTelecomCallId + " mLocalBdcTlsId=" + this.mLocalBdcTlsId + " mIsNotifiedTelecomCallId=" + this.mIsNotifiedTelecomCallId + "]";
    }
}
