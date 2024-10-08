package com.sec.internal.constants.ims.servicemodules.im.params;

import android.os.Message;
import com.sec.ims.util.ImsUri;
import com.sec.internal.constants.ims.servicemodules.im.NotificationStatus;
import com.sec.internal.log.IMSLog;
import java.util.Set;

public class SendSlmFileTransferParams {
    public Message mCallback;
    public String mConfUri;
    public String mContentType;
    public String mContributionId;
    public String mConversationId;
    public Set<NotificationStatus> mDispositionNotification;
    public String mFileName;
    public String mFilePath;
    public long mFileSize;
    public String mImdnMsgId;
    public String mInReplyToContributionId;
    public boolean mIsBroadcastMsg;
    public int mMessageId;
    public String mOwnImsi;
    public Set<ImsUri> mRecipients;
    public String mSdpContentType;
    public String mUserAlias;

    public SendSlmFileTransferParams(int i, Set<ImsUri> set, String str, String str2, String str3, String str4, long j, String str5, String str6, String str7, String str8, String str9, String str10, Set<NotificationStatus> set2, Message message, boolean z, String str11) {
        this.mMessageId = i;
        this.mRecipients = set;
        this.mConfUri = str;
        this.mUserAlias = str2;
        this.mFileName = str3;
        this.mFilePath = str4;
        this.mFileSize = j;
        this.mContentType = str5;
        this.mSdpContentType = str6;
        this.mContributionId = str7;
        this.mConversationId = str8;
        this.mInReplyToContributionId = str9;
        this.mImdnMsgId = str10;
        this.mDispositionNotification = set2;
        this.mCallback = message;
        this.mIsBroadcastMsg = z;
        this.mOwnImsi = str11;
    }

    public String toString() {
        return "SendSlmFileTransferParams [mMessageId=" + this.mMessageId + ", mRecipients=" + IMSLog.checker(this.mRecipients) + ", mConfUri=" + this.mConfUri + ", mUserAlias=" + IMSLog.checker(this.mUserAlias) + ", mFileName=" + IMSLog.checker(this.mFileName) + ", mFilePath=" + IMSLog.checker(this.mFilePath) + ", mFileSize=" + this.mFileSize + ", mContentType=" + this.mContentType + ", mSdpContentType=" + this.mSdpContentType + ", mContributionId=" + this.mContributionId + ", mConversationId=" + this.mConversationId + ", mInReplyToContributionId=" + this.mInReplyToContributionId + ", mImdnMsgId=" + this.mImdnMsgId + ", mDispositionNotification=" + this.mDispositionNotification + ", mCallback=" + this.mCallback + ", mIsBroadcastMsg=" + this.mIsBroadcastMsg + "]";
    }
}
