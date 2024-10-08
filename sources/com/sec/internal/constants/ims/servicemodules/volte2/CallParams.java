package com.sec.internal.constants.ims.servicemodules.volte2;

import android.os.Bundle;
import android.text.TextUtils;
import com.sec.ims.util.ImsUri;
import com.sec.internal.constants.ims.entitilement.NSDSNamespaces;
import com.sec.internal.log.IMSLog;

public class CallParams {
    private Bundle composerData;
    private String mAlertInfo;
    private String mAudioBitRate;
    private String mAudioCodec;
    private int mAudioEarlyMediaDir;
    private int mAudioRxTrackId;
    private int mCallState = 0;
    private String mCmcDeviceId;
    private int mCmcEdCallSlot = -1;
    private int mConfSessionId = -1;
    private String mConferenceSupported;
    private boolean mDelayRinging;
    private String mDtmfEvent;
    private String mFeatureCaps;
    private boolean mHasDiversion;
    private String mHistoryInfo;
    private boolean mIncomingCall;
    private int mIndicationFlag;
    private String mIsFocus;
    private boolean mLocalHoldTone;
    private int mLocalVideoRTCPPort;
    private int mLocalVideoRTPPort;
    private String mModifySupported;
    private String mNumberPlus;
    private String mOrganization;
    private String mPLettering;
    private String mPhotoRing;
    private String mReferredBy;
    private int mRejectCode = -1;
    private String mRejectProtocol;
    private String mRejectText;
    private boolean mRemoteHeld;
    private int mRemoteVideoRTCPPort;
    private int mRemoteVideoRTPPort;
    private String mReplaces;
    private int mRetryAfter;
    private String mSipCallId;
    private String mSipInviteMsg;
    private ImsUri mTerminatingId;
    private boolean mTouchScreenEnabled = false;
    private String mVerstat;
    private int mVideoCrbtType;
    private int mVideoHeight = 640;
    private int mVideoOrientation;
    private int mVideoWidth = NSDSNamespaces.NSDSHttpResponseCode.TEMPORARILY_UNAVAILABLE;
    private int misHDIcon;

    public Bundle getComposerData() {
        return this.composerData;
    }

    public void setComposerData(Bundle bundle) {
        this.composerData = bundle;
    }

    public String getPLettering() {
        return this.mPLettering;
    }

    public void setPLettering(String str) {
        this.mPLettering = str;
    }

    public String getHistoryInfo() {
        return this.mHistoryInfo;
    }

    public void setHistoryInfo(String str) {
        this.mHistoryInfo = str;
    }

    public String getDtmfEvent() {
        return this.mDtmfEvent;
    }

    public void setDtmfEvent(String str) {
        this.mDtmfEvent = str;
    }

    public String getModifyHeader() {
        return this.mModifySupported;
    }

    public void setModifyHeader(String str) {
        this.mModifySupported = str;
    }

    public String getAudioCodec() {
        return this.mAudioCodec;
    }

    public void setAudioCodec(String str) {
        this.mAudioCodec = str;
    }

    public String getNumberPlus() {
        return this.mNumberPlus;
    }

    public void setNumberPlus(String str) {
        this.mNumberPlus = str;
    }

    public String getConferenceSupported() {
        return this.mConferenceSupported;
    }

    public void setConferenceSupported(String str) {
        this.mConferenceSupported = str;
    }

    public String getIsFocus() {
        return this.mIsFocus;
    }

    public void setIsFocus(String str) {
        this.mIsFocus = str;
    }

    public int getLocalVideoRTPPort() {
        return this.mLocalVideoRTPPort;
    }

    public void setLocalVideoRTPPort(int i) {
        this.mLocalVideoRTPPort = i;
    }

    public int getLocalVideoRTCPPort() {
        return this.mLocalVideoRTCPPort;
    }

    public void setLocalVideoRTCPPort(int i) {
        this.mLocalVideoRTCPPort = i;
    }

    public int getRemoteVideoRTPPort() {
        return this.mRemoteVideoRTPPort;
    }

    public void setRemoteVideoRTPPort(int i) {
        this.mRemoteVideoRTPPort = i;
    }

    public int getRemoteVideoRTCPPort() {
        return this.mRemoteVideoRTCPPort;
    }

    public void setRemoteVideoRTCPPort(int i) {
        this.mRemoteVideoRTCPPort = i;
    }

    public int getIndicationFlag() {
        return this.mIndicationFlag;
    }

    public void setIndicationFlag(int i) {
        this.mIndicationFlag = i;
    }

    public int getisHDIcon() {
        return this.misHDIcon;
    }

    public void setisHDIcon(int i) {
        this.misHDIcon = i;
    }

    public int getRetryAfter() {
        return this.mRetryAfter;
    }

    public void setRetryAfter(int i) {
        this.mRetryAfter = i;
    }

    public String getPhotoRing() {
        return this.mPhotoRing;
    }

    public void setPhotoRing(String str) {
        this.mPhotoRing = str;
    }

    public String getAlertInfo() {
        return this.mAlertInfo;
    }

    public void setAlertInfo(String str) {
        this.mAlertInfo = str;
    }

    public int getVideoCrbtType() {
        return this.mVideoCrbtType;
    }

    public void setVideoCrbtType(int i) {
        this.mVideoCrbtType = i;
    }

    public int getVideoOrientation() {
        return this.mVideoOrientation;
    }

    public void setVideoOrientation(int i) {
        this.mVideoOrientation = i;
    }

    public void setReferredBy(String str) {
        this.mReferredBy = str;
    }

    public String getSipCallId() {
        return this.mSipCallId;
    }

    public void setSipCallId(String str) {
        this.mSipCallId = str;
    }

    public String getSipInviteMsg() {
        return this.mSipInviteMsg;
    }

    public void setSipInviteMsg(String str) {
        this.mSipInviteMsg = str;
    }

    public ImsUri getTerminatingId() {
        return this.mTerminatingId;
    }

    public void setTerminatingId(ImsUri imsUri) {
        this.mTerminatingId = imsUri;
    }

    public void setReplaces(String str) {
        this.mReplaces = str;
    }

    public String getReplaces() {
        return this.mReplaces;
    }

    public void setLocalHoldTone(boolean z) {
        this.mLocalHoldTone = z;
    }

    public boolean getLocalHoldTone() {
        return this.mLocalHoldTone;
    }

    public void setVerstat(String str) {
        this.mVerstat = str;
    }

    public String getVerstat() {
        return this.mVerstat;
    }

    public void setOrganization(String str) {
        this.mOrganization = str;
    }

    public String getOrganization() {
        return this.mOrganization;
    }

    public void setVideoWidth(int i) {
        this.mVideoWidth = i;
    }

    public int getVideoWidth() {
        return this.mVideoWidth;
    }

    public void setVideoHeight(int i) {
        this.mVideoHeight = i;
    }

    public int getVideoHeight() {
        return this.mVideoHeight;
    }

    public String getCmcDeviceId() {
        return this.mCmcDeviceId;
    }

    public void setCmcDeviceId(String str) {
        this.mCmcDeviceId = str;
    }

    public void setAudioRxTrackId(int i) {
        this.mAudioRxTrackId = i;
    }

    public int getAudioRxTrackId() {
        return this.mAudioRxTrackId;
    }

    public void setAudioBitRate(String str) {
        this.mAudioBitRate = str;
    }

    public String getAudioBitRate() {
        return this.mAudioBitRate;
    }

    public void setFeatureCaps(String str) {
        this.mFeatureCaps = str;
    }

    public String getFeatureCaps() {
        return this.mFeatureCaps;
    }

    public boolean isIncomingCall() {
        return this.mIncomingCall;
    }

    public void setAsIncomingCall() {
        this.mIncomingCall = true;
    }

    public void setAudioEarlyMediaDir(int i) {
        this.mAudioEarlyMediaDir = i;
    }

    public int getAudioEarlyMediaDir() {
        return this.mAudioEarlyMediaDir;
    }

    public void setHasDiversion(boolean z) {
        this.mHasDiversion = z;
    }

    public boolean getHasDiversion() {
        return this.mHasDiversion;
    }

    public void setDelayRinging(boolean z) {
        this.mDelayRinging = z;
    }

    public boolean getDelayRinging() {
        return this.mDelayRinging;
    }

    public void setRejectProtocol(String str) {
        this.mRejectProtocol = str;
    }

    public String getRejectProtocol() {
        return this.mRejectProtocol;
    }

    public void setRejectCode(int i) {
        this.mRejectCode = i;
    }

    public int getRejectCode() {
        return this.mRejectCode;
    }

    public void setRejectText(String str) {
        this.mRejectText = str;
    }

    public String getRejectText() {
        return this.mRejectText;
    }

    public void setRemoteHeld(boolean z) {
        this.mRemoteHeld = z;
    }

    public boolean getRemoteHeld() {
        return this.mRemoteHeld;
    }

    public void setCmcEdCallSlot(int i) {
        this.mCmcEdCallSlot = i;
    }

    public int getCmcEdCallSlot() {
        return this.mCmcEdCallSlot;
    }

    public void setConfSessionId(int i) {
        this.mConfSessionId = i;
    }

    public int getConfSessionId() {
        return this.mConfSessionId;
    }

    public void setCallState(int i) {
        this.mCallState = i;
    }

    public int getCallState() {
        return this.mCallState;
    }

    public void setTouchScreenEnabled(boolean z) {
        this.mTouchScreenEnabled = z;
    }

    public boolean getTouchScreenEnabled() {
        return this.mTouchScreenEnabled;
    }

    public String toString() {
        String checker = TextUtils.isEmpty(this.mHistoryInfo) ? this.mHistoryInfo : IMSLog.checker(this.mHistoryInfo);
        return "CallParams [mPLettering=" + this.mPLettering + ", mHistoryInfo=" + checker + ", mDtmfEvent=" + this.mDtmfEvent + ", mModifySupported=" + this.mModifySupported + ", mAudioCodec=" + this.mAudioCodec + ", mNumberPlus=" + IMSLog.checker(this.mNumberPlus) + ", mConferenceSupported=" + this.mConferenceSupported + ", mIsFocus=" + this.mIsFocus + ", mIndicationFlag=" + this.mIndicationFlag + ", misHDIcon=" + this.misHDIcon + ", mPhotoRing=" + this.mPhotoRing + ", mLocalVideoRTPPort=" + this.mLocalVideoRTPPort + ", mLocalVideoRTCPPort=" + this.mLocalVideoRTCPPort + ", mRemoteVideoRTPPort=" + this.mRemoteVideoRTPPort + ", mRemoteVideoRTCPPort=" + this.mRemoteVideoRTCPPort + ", mRetryAfter=" + this.mRetryAfter + ", mAlertInfo=" + this.mAlertInfo + ", mVideoOrientation=" + this.mVideoOrientation + ", mReferredBy=" + IMSLog.checker(this.mReferredBy) + ", mSipCallId=" + this.mSipCallId + ", mOrganization=" + this.mOrganization + ", mComposerData=" + this.composerData + ", mLocalHoldTone=" + this.mLocalHoldTone + ", mVideoWidth=" + this.mVideoWidth + ", mVideoHeight=" + this.mVideoHeight + ", mVideoCrbtType=" + this.mVideoCrbtType + ", mFeatureCaps=" + this.mFeatureCaps + ", mAudioEarlyMediaDir=" + this.mAudioEarlyMediaDir + ", mVerstat=" + IMSLog.checker(this.mVerstat) + ", mHasDiversion=" + this.mHasDiversion + ", mDelayRinging=" + this.mDelayRinging + ", mRejectProtocol=" + this.mRejectProtocol + ", mRejectCode=" + this.mRejectCode + ", mRejectText=" + this.mRejectText + ", mRemoteHeld=" + this.mRemoteHeld + ", mCmcEdCallSlot=" + this.mCmcEdCallSlot + ", mConfSessionId=" + this.mConfSessionId + ", mCallState=" + this.mCallState + ", mTouchScreenEnabled=" + this.mTouchScreenEnabled + "]";
    }
}
