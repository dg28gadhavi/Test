package com.sec.internal.constants.ims.servicemodules.volte2;

import com.sec.ims.util.NameAddr;
import com.sec.ims.util.SipError;
import com.sec.internal.ims.servicemodules.volte2.idc.IdcExtra;
import com.sec.internal.log.IMSLog;
import java.util.ArrayList;
import java.util.List;

public class CallStateEvent {
    private ALTERNATIVE_SERVICE mAlternativeService = ALTERNATIVE_SERVICE.NONE;
    private String mAlternativeServiceReason = "";
    private String mAlternativeServiceType = "";
    private String mAlternativeServiceUrn = "";
    private int mCallType = -1;
    private String mCmcCallTime = "";
    private String mCmcDeviceId = "";
    private IdcExtra mIdcExtra = new IdcExtra();
    private boolean mIsConference = false;
    private boolean mIsSdToSdPull = false;
    private CallParams mParams = null;
    private NameAddr mPeerAddr = null;
    private boolean mRemoteVideoCapa = false;
    private int mRetryAfter = 0;
    private int mSessionID = -1;
    private SipError mSipErrorCode = null;
    private CALL_STATE mState = CALL_STATE.NOT_INITIALIZED;
    private List<ParticipantUser> mUpdatedParticipants = new ArrayList();

    public enum ALTERNATIVE_SERVICE {
        NONE,
        INITIAL_REGISTRATION,
        EMERGENCY_REGISTRATION,
        EMERGENCY
    }

    public enum CALL_STATE {
        NOT_INITIALIZED,
        CALLING,
        TRYING,
        EARLY_MEDIA_START,
        SESSIONPROGRESS,
        RINGING_BACK,
        FORWARDED,
        ESTABLISHED,
        REFRESHFAIL,
        ENDED,
        HELD_LOCAL,
        HELD_REMOTE,
        HELD_BOTH,
        MODIFY_REQUESTED,
        MODIFIED,
        CONFERENCE_ADDED,
        CONFERENCE_REMOVED,
        CONFERENCE_PARTICIPANTS_UPDATED,
        EXTEND_TO_CONFERENCE,
        ERROR
    }

    public static class ParticipantUser {
        private final int mParticipantId;
        private final int mParticipantStatus;
        private final int mSessionId;
        private final String mUri;

        public ParticipantUser(int i, int i2, int i3, String str) {
            this.mParticipantId = i;
            this.mSessionId = i2;
            this.mParticipantStatus = i3;
            this.mUri = str;
        }

        public int getParticipantId() {
            return this.mParticipantId;
        }

        public int getParticipantStatus() {
            return this.mParticipantStatus;
        }

        public int getSessionId() {
            return this.mSessionId;
        }

        public String getUri() {
            return this.mUri;
        }
    }

    public CallStateEvent() {
    }

    public CallStateEvent(CALL_STATE call_state) {
        this.mState = call_state;
    }

    public void setState(CALL_STATE call_state) {
        this.mState = call_state;
    }

    public CALL_STATE getState() {
        return this.mState;
    }

    public void setSessionID(int i) {
        this.mSessionID = i;
    }

    public int getSessionID() {
        return this.mSessionID;
    }

    public void setCallType(int i) {
        this.mCallType = i;
    }

    public int getCallType() {
        return this.mCallType;
    }

    public void setPeerAddr(NameAddr nameAddr) {
        this.mPeerAddr = nameAddr;
    }

    public NameAddr getPeerAddr() {
        return this.mPeerAddr;
    }

    public void setRemoteVideoCapa(boolean z) {
        this.mRemoteVideoCapa = z;
    }

    public boolean getRemoteVideoCapa() {
        return this.mRemoteVideoCapa;
    }

    public void setParams(CallParams callParams) {
        this.mParams = callParams;
    }

    public CallParams getParams() {
        return this.mParams;
    }

    public void addUpdatedParticipantsList(String str, int i, int i2, int i3) {
        this.mUpdatedParticipants.add(new ParticipantUser(i, i2, i3, str));
    }

    public List<ParticipantUser> getUpdatedParticipantsList() {
        return this.mUpdatedParticipants;
    }

    public void setErrorCode(SipError sipError) {
        this.mSipErrorCode = sipError;
    }

    public SipError getErrorCode() {
        return this.mSipErrorCode;
    }

    public void setAlternativeService(ALTERNATIVE_SERVICE alternative_service) {
        this.mAlternativeService = alternative_service;
    }

    public ALTERNATIVE_SERVICE getAlternativeService() {
        return this.mAlternativeService;
    }

    public void setAlternativeServiceType(String str) {
        this.mAlternativeServiceType = str;
    }

    public String getAlternativeServiceType() {
        return this.mAlternativeServiceType;
    }

    public void setAlternativeServiceReason(String str) {
        this.mAlternativeServiceReason = str;
    }

    public String getAlternativeServiceReason() {
        return this.mAlternativeServiceReason;
    }

    public void setAlternativeServiceUrn(String str) {
        this.mAlternativeServiceUrn = str;
    }

    public String getAlternativeServiceUrn() {
        return this.mAlternativeServiceUrn;
    }

    public void setRetryAfter(int i) {
        this.mRetryAfter = i;
    }

    public int getRetryAfter() {
        return this.mRetryAfter;
    }

    public void setConference(boolean z) {
        this.mIsConference = z;
    }

    public boolean isConference() {
        return this.mIsConference;
    }

    public String getCmcDeviceId() {
        return this.mCmcDeviceId;
    }

    public void setCmcDeviceId(String str) {
        this.mCmcDeviceId = str;
    }

    public String getCmcCallTime() {
        return this.mCmcCallTime;
    }

    public void setCmcCallTime(String str) {
        this.mCmcCallTime = str;
    }

    public boolean getIsSdToSdPull() {
        return this.mIsSdToSdPull;
    }

    public void setIsSdToSdPull(boolean z) {
        this.mIsSdToSdPull = z;
    }

    public IdcExtra getIdcExtra() {
        return this.mIdcExtra;
    }

    public void setIdcExtra(String str) {
        this.mIdcExtra.parse(str);
    }

    public String toString() {
        return "CallStateEvent [sessionId=" + this.mSessionID + ", state=" + this.mState + ", peer=" + IMSLog.checker(this.mPeerAddr) + ", mSipErrorCode=" + this.mSipErrorCode + ", Params=" + this.mParams + "]";
    }
}
