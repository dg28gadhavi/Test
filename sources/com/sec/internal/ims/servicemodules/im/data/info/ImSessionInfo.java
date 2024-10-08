package com.sec.internal.ims.servicemodules.im.data.info;

import com.sec.ims.util.ImsUri;
import com.sec.internal.constants.ims.servicemodules.im.ImDirection;
import com.sec.internal.constants.ims.servicemodules.im.ImError;
import java.util.HashSet;
import java.util.Set;

public class ImSessionInfo {
    public String mContributionId;
    public String mConversationId;
    public ImDirection mDirection;
    public String mInReplyToContributionId;
    public boolean mIsTryToLeave;
    public ImError mLastProvisionalResponse;
    public Object mPrevExtendRawHandle;
    public Object mRawHandle;
    public Set<Integer> mReceivedMessageIds;
    public String mSdpContentType;
    public SessionType mSessionType;
    public ImsUri mSessionUri;
    public StartingReason mStartingReason;
    public ImSessionState mState;

    public enum ImSessionState {
        INITIAL,
        PENDING_INVITE,
        STARTING,
        STARTED,
        ACCEPTING,
        ESTABLISHED,
        CLOSING
    }

    public enum SessionType {
        NORMAL,
        SNF_SESSION,
        SNF_NOTIFICATION_SESSION
    }

    public enum StartingReason {
        NORMAL,
        RESTARTING,
        AUTOMATIC_REJOINING,
        RESTARTING_WITH_NEW_ID,
        EXTENDING_1_1_TO_GROUP
    }

    public ImSessionInfo(Object obj, ImSessionState imSessionState, ImDirection imDirection, ImsUri imsUri, String str, String str2, String str3, String str4) {
        this.mState = ImSessionState.INITIAL;
        this.mStartingReason = StartingReason.NORMAL;
        this.mSessionType = SessionType.NORMAL;
        this.mReceivedMessageIds = new HashSet();
        this.mRawHandle = obj;
        this.mState = imSessionState;
        this.mDirection = imDirection;
        this.mSessionUri = imsUri;
        this.mContributionId = str;
        this.mConversationId = str2;
        this.mInReplyToContributionId = str3;
        this.mSdpContentType = str4;
    }

    public ImSessionInfo(ImSessionState imSessionState, ImDirection imDirection, ImsUri imsUri, String str, String str2, String str3, String str4) {
        this((Object) null, imSessionState, imDirection, imsUri, str, str2, str3, str4);
    }

    public boolean isSnFSession() {
        SessionType sessionType = this.mSessionType;
        return sessionType == SessionType.SNF_SESSION || sessionType == SessionType.SNF_NOTIFICATION_SESSION;
    }

    public String toString() {
        return "ImSessionInfo [mRawHandle=" + this.mRawHandle + ", mState=" + this.mState + ", mDirection=" + this.mDirection + ", mSessionUri=" + this.mSessionUri + ", mContributionId=" + this.mContributionId + ", mConversationId=" + this.mConversationId + ", mInReplyToContributionId=" + this.mInReplyToContributionId + ", mPrevExtendRawHandle=" + this.mPrevExtendRawHandle + ", mSdpContentType=" + this.mSdpContentType + ", mStartingReason=" + this.mStartingReason + ", mSessionType=" + this.mSessionType + ", mIsTryToLeave=" + this.mIsTryToLeave + "]";
    }
}
