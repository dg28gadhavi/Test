package com.sec.internal.ims.servicemodules.volte2.data;

import com.sec.ims.ImsRegistration;
import com.sec.ims.util.NameAddr;
import com.sec.internal.constants.ims.servicemodules.volte2.CallParams;

public class IncomingCallEvent {
    private final int mCallType;
    private final String mIdcExtra;
    private final CallParams mParams;
    private final NameAddr mPeerAddr;
    private final boolean mPreAlerting;
    private final ImsRegistration mRegistration;
    private boolean mRemoteVideoCapa;
    private final int mSessionID;

    public IncomingCallEvent(ImsRegistration imsRegistration, int i, int i2, NameAddr nameAddr, boolean z, boolean z2, String str, CallParams callParams) {
        this.mRegistration = imsRegistration;
        this.mSessionID = i;
        this.mCallType = i2;
        this.mPeerAddr = nameAddr;
        this.mPreAlerting = z;
        this.mRemoteVideoCapa = z2;
        this.mIdcExtra = str;
        this.mParams = callParams;
    }

    public ImsRegistration getImsRegistration() {
        return this.mRegistration;
    }

    public boolean getPreAlerting() {
        return this.mPreAlerting;
    }

    public int getSessionID() {
        return this.mSessionID;
    }

    public int getCallType() {
        return this.mCallType;
    }

    public NameAddr getPeerAddr() {
        return this.mPeerAddr;
    }

    public boolean getRemoteVideoCapa() {
        return this.mRemoteVideoCapa;
    }

    public String getIdcExtra() {
        return this.mIdcExtra;
    }

    public CallParams getParams() {
        return this.mParams;
    }
}
