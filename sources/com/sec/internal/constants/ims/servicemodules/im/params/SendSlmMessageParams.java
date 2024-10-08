package com.sec.internal.constants.ims.servicemodules.im.params;

import android.os.Message;
import com.sec.ims.util.ImsUri;
import com.sec.internal.constants.ims.servicemodules.im.NotificationStatus;
import com.sec.internal.log.IMSLog;
import java.util.Date;
import java.util.Set;

public class SendSlmMessageParams {
    public String mBody;
    public Message mCallback;
    public String mChatId;
    public String mContentType;
    public String mContributionId;
    public String mConversationId;
    public Set<NotificationStatus> mDispositionNotification;
    public String mImdnMessageId;
    public Date mImdnTime;
    public String mInReplyToContributionId;
    public boolean mIsBroadcastMsg;
    public boolean mIsChatbotParticipant;
    public boolean mIsPublicAccountMsg;
    public String mMaapTrafficType;
    public int mMessageId;
    public String mOwnImsi;
    public Set<ImsUri> mReceivers;
    public SendReportMsgParams mReportMsgParams;
    public String mUserAlias;

    public SendSlmMessageParams(int i, String str, String str2, String str3, String str4, String str5, Date date, Set<NotificationStatus> set, String str6, String str7, String str8, Set<ImsUri> set2, Message message, boolean z, boolean z2, String str9, boolean z3, String str10) {
        this.mMessageId = i;
        this.mChatId = str;
        this.mBody = str2;
        this.mContentType = str3;
        this.mUserAlias = str4;
        this.mImdnMessageId = str5;
        this.mImdnTime = date;
        this.mCallback = message;
        this.mDispositionNotification = set;
        this.mContributionId = str6;
        this.mConversationId = str7;
        this.mInReplyToContributionId = str8;
        this.mReceivers = set2;
        this.mIsPublicAccountMsg = z;
        this.mIsBroadcastMsg = z2;
        this.mOwnImsi = str9;
        this.mIsChatbotParticipant = z3;
        this.mMaapTrafficType = str10;
    }

    public String toString() {
        return "SendMessageParams [mMessageId=" + this.mMessageId + ", mChatId=" + this.mChatId + ", mBody=" + IMSLog.checker(this.mBody) + ", mContentType=" + this.mContentType + ", mImdnMessageId=" + this.mImdnMessageId + ", mImdnTime=" + this.mImdnTime + ", mDispositionNotification=" + this.mDispositionNotification + ", mCallback=" + this.mCallback + ", mIsPublicAccountMsg = " + this.mIsPublicAccountMsg + ", mIsBroadcastMsg = " + this.mIsBroadcastMsg + ", mOwnImsi=" + IMSLog.checker(this.mOwnImsi) + ", mIsChatbotParticipant=" + this.mIsChatbotParticipant + ", mMaapTrafficType=" + this.mMaapTrafficType + ", mInReplyToContributionId = " + this.mInReplyToContributionId + ", mReceivers=" + IMSLog.checker(this.mReceivers) + "]";
    }
}
