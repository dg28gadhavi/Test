package com.sec.internal.ims.servicemodules.volte2.data;

import com.sec.ims.ImsRegistration;
import com.sec.ims.util.ImsUri;
import com.sec.ims.volte2.data.CallProfile;
import com.sec.ims.volte2.data.MediaProfile;
import com.sec.internal.constants.Mno;
import com.sec.internal.constants.ims.servicemodules.volte2.CallParams;

public abstract class CallProfileBuilder {
    protected final String LOG_TAG;
    protected CallParams mCallParams;
    protected CallProfile mCallProfile;

    public abstract CallProfile build(CallProfile callProfile);

    public abstract CallProfileBuilder setCmcDeviceId(IncomingCallEvent incomingCallEvent, ImsRegistration imsRegistration, CallProfile callProfile);

    public abstract CallProfileBuilder setCmcEdCallSlot(int i);

    public abstract CallProfileBuilder setComposerData(IncomingCallEvent incomingCallEvent);

    public abstract CallProfileBuilder setDisplayName(IncomingCallEvent incomingCallEvent, Mno mno, boolean z);

    public abstract CallProfileBuilder setEchoCallId(IncomingCallEvent incomingCallEvent, Mno mno, String str);

    public CallProfileBuilder(CallProfile callProfile) {
        this.LOG_TAG = "CallProfileBuilder";
        this.mCallParams = null;
        this.mCallProfile = callProfile;
    }

    public CallProfileBuilder() {
        this.LOG_TAG = "CallProfileBuilder";
        this.mCallProfile = null;
        this.mCallParams = null;
    }

    public CallProfile build() {
        return this.mCallProfile;
    }

    public CallProfileBuilder builder() {
        if (this.mCallProfile == null) {
            this.mCallProfile = new CallProfile();
        }
        if (this.mCallParams == null) {
            this.mCallParams = new CallParams();
        }
        return this;
    }

    public CallProfileBuilder builder(CallProfile callProfile) {
        this.mCallProfile = callProfile;
        return this;
    }

    public CallProfileBuilder setLetteringText(String str) {
        this.mCallProfile.setLetteringText(str);
        return this;
    }

    public CallProfileBuilder setHistoryInfo(String str) {
        this.mCallProfile.setHistoryInfo(str);
        return this;
    }

    public CallProfileBuilder setAlertInfo(String str) {
        this.mCallProfile.setAlertInfo(str);
        return this;
    }

    public CallProfileBuilder setPhotoRing(String str) {
        this.mCallProfile.setPhotoRing(str);
        return this;
    }

    public CallProfileBuilder setNumberPlus(String str) {
        this.mCallProfile.setNumberPlus(str);
        return this;
    }

    public CallProfileBuilder setModifyHeader(String str) {
        this.mCallProfile.setModifyHeader(str);
        return this;
    }

    public CallProfileBuilder setConferenceSupported(String str) {
        this.mCallProfile.setConferenceSupported(str);
        return this;
    }

    public CallProfileBuilder setIsFocus(String str) {
        this.mCallProfile.setIsFocus(str);
        return this;
    }

    public CallProfileBuilder setHDIcon(int i) {
        this.mCallProfile.setHDIcon(i);
        return this;
    }

    public CallProfileBuilder setRetryAfterTime(int i) {
        this.mCallProfile.setRetryAfterTime(i);
        return this;
    }

    public CallProfileBuilder setAudioRxTrackId(int i) {
        this.mCallProfile.setAudioRxTrackId(i);
        return this;
    }

    public CallProfileBuilder setFeatureCaps(String str) {
        this.mCallProfile.setFeatureCaps(str);
        return this;
    }

    public CallProfileBuilder setAudioEarlyMediaDir(int i) {
        this.mCallProfile.setAudioEarlyMediaDir(i);
        return this;
    }

    public CallProfileBuilder setDelayRinging(boolean z) {
        this.mCallProfile.setDelayRinging(z);
        return this;
    }

    public CallProfileBuilder setSipCallId(String str) {
        this.mCallProfile.setSipCallId(str);
        return this;
    }

    public CallProfileBuilder setOriginatingUri(ImsUri imsUri) {
        this.mCallProfile.setOriginatingUri(imsUri);
        return this;
    }

    public CallProfileBuilder setTerminatingId(ImsUri imsUri) {
        this.mCallParams.setTerminatingId(imsUri);
        return this;
    }

    public CallProfileBuilder setLineMsisdn(String str) {
        this.mCallProfile.setLineMsisdn(str);
        return this;
    }

    public CallProfileBuilder setRejectProtocol(String str) {
        this.mCallProfile.setRejectProtocol(str);
        return this;
    }

    public CallProfileBuilder setRejectCode(int i) {
        this.mCallProfile.setRejectCode(i);
        return this;
    }

    public CallProfileBuilder setRejectText(String str) {
        this.mCallProfile.setRejectText(str);
        return this;
    }

    public CallProfileBuilder setHasDiversion(boolean z) {
        this.mCallProfile.setHasDiversion(z);
        return this;
    }

    public CallProfileBuilder setRemoteHeld(boolean z) {
        this.mCallProfile.setRemoteHeld(z);
        return this;
    }

    public CallProfileBuilder setVerstat(String str) {
        this.mCallProfile.setVerstat(str);
        return this;
    }

    public CallProfileBuilder setSipInviteMsg(String str) {
        this.mCallProfile.setSipInviteMsg(str);
        return this;
    }

    public CallProfileBuilder setReplaceSipCallId(String str) {
        this.mCallProfile.setReplaceSipCallId(str);
        return this;
    }

    public CallProfileBuilder setCallType(int i) {
        this.mCallProfile.setCallType(i);
        return this;
    }

    public CallProfileBuilder setPhoneId(int i) {
        this.mCallProfile.setPhoneId(i);
        return this;
    }

    public CallProfileBuilder setEmergencyRat(String str) {
        this.mCallProfile.setEmergencyRat(str);
        return this;
    }

    public CallProfileBuilder setUrn(String str) {
        this.mCallProfile.setUrn(str);
        return this;
    }

    public CallProfileBuilder setCLI(String str) {
        this.mCallProfile.setCLI(str);
        return this;
    }

    public CallProfileBuilder setConferenceCall(int i) {
        this.mCallProfile.setConferenceCall(i);
        return this;
    }

    public CallProfileBuilder setMediaProfile(MediaProfile mediaProfile) {
        this.mCallProfile.setMediaProfile(mediaProfile);
        return this;
    }

    public CallProfileBuilder setCmcBoundSessionId(int i) {
        this.mCallProfile.setCmcBoundSessionId(i);
        return this;
    }

    public CallProfileBuilder setCmcType(int i) {
        this.mCallProfile.setCmcType(i);
        return this;
    }

    public CallProfileBuilder setForceCSFB(boolean z) {
        this.mCallProfile.setForceCSFB(z);
        return this;
    }

    public CallProfileBuilder setDialingNumber(String str) {
        this.mCallProfile.setDialingNumber(str);
        return this;
    }

    public CallProfileBuilder setNetworkType(int i) {
        this.mCallProfile.setNetworkType(i);
        return this;
    }

    public CallProfileBuilder setSamsungMdmnCall(boolean z) {
        this.mCallProfile.setSamsungMdmnCall(z);
        return this;
    }

    public CallProfileBuilder setOrganization(String str) {
        this.mCallProfile.setOrganization(str);
        return this;
    }

    public CallProfileBuilder setDirection(int i) {
        this.mCallProfile.setDirection(i);
        return this;
    }

    public CallProfileBuilder setConfSessionId(int i) {
        this.mCallProfile.setConfSessionId(i);
        return this;
    }

    public CallProfileBuilder setTouchScreenEnabled(boolean z) {
        this.mCallProfile.setTouchScreenEnabled(z);
        return this;
    }
}
