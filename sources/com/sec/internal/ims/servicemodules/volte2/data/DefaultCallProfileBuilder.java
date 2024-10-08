package com.sec.internal.ims.servicemodules.volte2.data;

import android.util.Log;
import com.sec.ims.ImsRegistration;
import com.sec.ims.util.ImsUri;
import com.sec.ims.volte2.data.CallProfile;
import com.sec.internal.constants.Mno;
import com.sec.internal.constants.ims.servicemodules.volte2.CallParams;
import com.sec.internal.helper.ImsCallUtil;
import com.sec.internal.helper.UriUtil;

public class DefaultCallProfileBuilder extends CallProfileBuilder {
    public CallProfileBuilder setCmcDeviceId(IncomingCallEvent incomingCallEvent, ImsRegistration imsRegistration, CallProfile callProfile) {
        return this;
    }

    public CallProfileBuilder setCmcEdCallSlot(int i) {
        return this;
    }

    public CallProfileBuilder setComposerData(IncomingCallEvent incomingCallEvent) {
        return this;
    }

    public CallProfileBuilder setDisplayName(IncomingCallEvent incomingCallEvent, Mno mno, boolean z) {
        return this;
    }

    public CallProfileBuilder setEchoCallId(IncomingCallEvent incomingCallEvent, Mno mno, String str) {
        return this;
    }

    public DefaultCallProfileBuilder() {
    }

    public DefaultCallProfileBuilder(CallProfile callProfile) {
        super(callProfile);
    }

    public DefaultCallProfileBuilder(CallParams callParams) {
        builder();
        setLetteringText(callParams.getPLettering());
        setHistoryInfo(callParams.getHistoryInfo());
        setAlertInfo(callParams.getAlertInfo());
        setPhotoRing(callParams.getPhotoRing());
        setNumberPlus(callParams.getNumberPlus());
        setModifyHeader(callParams.getModifyHeader());
        setConferenceSupported(callParams.getConferenceSupported());
        setIsFocus(callParams.getIsFocus());
        setHDIcon(callParams.getisHDIcon());
        setRetryAfterTime(callParams.getRetryAfter());
        setAudioRxTrackId(callParams.getAudioRxTrackId());
        setFeatureCaps(callParams.getFeatureCaps());
        setAudioEarlyMediaDir(callParams.getAudioEarlyMediaDir());
        setDelayRinging(callParams.getDelayRinging());
        setSipCallId(callParams.getSipCallId());
        setOriginatingUri(callParams.getTerminatingId());
        if (callParams.getTerminatingId() != null) {
            String msisdnNumber = UriUtil.getMsisdnNumber(callParams.getTerminatingId());
            setLineMsisdn(msisdnNumber == null ? callParams.getTerminatingId().toString() : msisdnNumber);
        } else {
            setTerminatingId((ImsUri) null);
        }
        setRejectProtocol(callParams.getRejectProtocol());
        setRejectCode(callParams.getRejectCode());
        setRejectText(callParams.getRejectText());
        setHasDiversion(callParams.getHasDiversion());
        setRemoteHeld(callParams.getRemoteHeld());
        setVerstat(ImsCallUtil.getVerstat(callParams));
        setSipInviteMsg(callParams.getSipInviteMsg());
        if (callParams.getOrganization() != null) {
            setOrganization(callParams.getOrganization());
        }
        setConfSessionId(callParams.getConfSessionId());
        setTouchScreenEnabled(callParams.getTouchScreenEnabled());
    }

    public CallProfile build(CallProfile callProfile) {
        Log.i("CallProfileBuilder", "build CallProfile with CallParams" + this.mCallProfile);
        if (this.mCallProfile.getLetteringText() != null) {
            callProfile.setLetteringText(this.mCallProfile.getLetteringText());
        }
        if (this.mCallProfile.getHistoryInfo() != null) {
            callProfile.setHistoryInfo(this.mCallProfile.getHistoryInfo());
        }
        if (this.mCallProfile.getAlertInfo() != null) {
            callProfile.setAlertInfo(this.mCallProfile.getAlertInfo());
        }
        if (this.mCallProfile.getPhotoRing() != null) {
            callProfile.setPhotoRing(this.mCallProfile.getPhotoRing());
        }
        if (this.mCallProfile.getNumberPlus() != null) {
            callProfile.setNumberPlus(this.mCallProfile.getNumberPlus());
        }
        if (this.mCallProfile.getModifyHeader() != null) {
            callProfile.setModifyHeader(this.mCallProfile.getModifyHeader());
        }
        if (this.mCallProfile.getConferenceSupported() != null) {
            callProfile.setConferenceSupported(this.mCallProfile.getConferenceSupported());
        }
        if (this.mCallProfile.getIsFocus() != null) {
            callProfile.setIsFocus(this.mCallProfile.getIsFocus());
        }
        if (this.mCallProfile.getHDIcon() > 0) {
            callProfile.setHDIcon(this.mCallProfile.getHDIcon());
        }
        if (this.mCallProfile.getRetryAfterTime() > 0) {
            callProfile.setRetryAfterTime(this.mCallProfile.getRetryAfterTime());
        }
        if (this.mCallProfile.getAudioRxTrackId() > 0) {
            callProfile.setAudioRxTrackId(this.mCallProfile.getAudioRxTrackId());
        }
        if (this.mCallProfile.getFeatureCaps() != null) {
            callProfile.setFeatureCaps(this.mCallProfile.getFeatureCaps());
        }
        callProfile.setAudioEarlyMediaDir(this.mCallProfile.getAudioEarlyMediaDir());
        callProfile.setDelayRinging(this.mCallProfile.getDelayRinging());
        if (this.mCallProfile.getSipCallId() != null) {
            callProfile.setSipCallId(this.mCallProfile.getSipCallId());
        }
        if (this.mCallProfile.getSipInviteMsg() != null) {
            callProfile.setSipInviteMsg(this.mCallProfile.getSipInviteMsg());
        }
        if (this.mCallParams.getTerminatingId() != null) {
            callProfile.setOriginatingUri(this.mCallParams.getTerminatingId());
            callProfile.setLineMsisdn(this.mCallProfile.getLineMsisdn());
        }
        if (this.mCallProfile.getRejectCode() != -1) {
            callProfile.setRejectProtocol(this.mCallProfile.getRejectProtocol());
            callProfile.setRejectCode(this.mCallProfile.getRejectCode());
            callProfile.setRejectText(this.mCallProfile.getRejectText());
        }
        callProfile.setVerstat(this.mCallProfile.getVerstat());
        if (this.mCallProfile.getHasDiversion()) {
            callProfile.setHasDiversion(this.mCallProfile.getHasDiversion());
        }
        callProfile.setRemoteHeld(this.mCallProfile.isRemoteHeld());
        if (this.mCallProfile.getOrganization() != null) {
            callProfile.setOrganization(this.mCallProfile.getOrganization());
        }
        callProfile.setConfSessionId(this.mCallProfile.getConfSessionId());
        callProfile.setTouchScreenEnabled(this.mCallProfile.getTouchScreenEnabled());
        return callProfile;
    }
}
