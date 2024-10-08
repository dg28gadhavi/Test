package com.sec.internal.constants.ims.servicemodules.im.params;

import android.os.Message;
import com.sec.ims.util.ImsUri;
import com.sec.internal.constants.ims.servicemodules.im.ImDirection;
import com.sec.internal.constants.ims.servicemodules.im.NotificationStatus;
import com.sec.internal.log.IMSLog;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Set;

public class SendFtSessionParams {
    public final Message mCallback;
    public ImsUri mConfUri;
    public String mContentType;
    public String mContributionId;
    public String mConversationId;
    public ImDirection mDirection;
    public Set<NotificationStatus> mDispositionNotification;
    public String mFileFingerPrint;
    public String mFileName;
    public String mFilePath;
    public long mFileSize;
    public String mFileTransferID;
    public String mImdnId;
    public Date mImdnTime;
    public String mInReplyToContributionId;
    public boolean mIsPublicAccountMsg;
    public boolean mIsResuming;
    public int mMessageId;
    public String mOwnImsi;
    public List<ImsUri> mRecipients;
    public SendReportMsgParams mReportMsgParams;
    public final Message mSessionHandleCallback;
    public String mThumbPath;
    public int mTimeDuration;
    public long mTransferredBytes;
    public String mUserAlias;

    public SendFtSessionParams(int i, String str, String str2, String str3, Message message, Message message2, List<ImsUri> list, ImsUri imsUri, String str4, String str5, String str6, long j, String str7, ImDirection imDirection, boolean z, long j2, Set<NotificationStatus> set, String str8, Date date, String str9, String str10, int i2, boolean z2, String str11, String str12) {
        this.mMessageId = i;
        this.mConversationId = str2;
        this.mInReplyToContributionId = str3;
        this.mCallback = message;
        this.mSessionHandleCallback = message2;
        List<ImsUri> list2 = list;
        this.mRecipients = new ArrayList(list);
        this.mConfUri = imsUri;
        this.mUserAlias = str4;
        this.mFilePath = str6;
        this.mFileName = str5;
        this.mFileSize = j;
        this.mContentType = str7;
        this.mContributionId = str;
        this.mDirection = imDirection;
        this.mIsResuming = z;
        this.mTransferredBytes = j2;
        this.mDispositionNotification = set;
        this.mImdnId = str8;
        this.mImdnTime = date;
        this.mFileTransferID = str9;
        this.mThumbPath = str10;
        this.mTimeDuration = i2;
        this.mIsPublicAccountMsg = z2;
        this.mFileFingerPrint = str11;
        this.mOwnImsi = str12;
    }

    public String toString() {
        return "SendFtSessionParams [mMessageId=" + this.mMessageId + ", mContributionId=" + this.mContributionId + ", mConversationId=" + this.mConversationId + ", mInReplyToContributionId=" + this.mInReplyToContributionId + ", mRecipients=" + IMSLog.checker(this.mRecipients) + ", mConfUri=" + this.mConfUri + ", mUserAlias=" + IMSLog.checker(this.mUserAlias) + ", mFilePath=" + IMSLog.checker(this.mFilePath) + ", mFileName=" + IMSLog.checker(this.mFileName) + ", mFileSize=" + this.mFileSize + ", mContentType=" + this.mContentType + ", mContributionId=" + this.mContributionId + ", mDirection=" + this.mDirection + ", mIsResuming=" + this.mIsResuming + ", mTransferredBytes=" + this.mTransferredBytes + ", mDispositionNotification=" + this.mDispositionNotification + ", mImdnId=" + this.mImdnId + ", mImdnTime=" + this.mImdnTime + ", mFileTransferID = " + this.mFileTransferID + ", mCallback=" + this.mCallback + ", mSessionHandleCallback=" + this.mSessionHandleCallback + ", mThumbPath=" + this.mThumbPath + ", mTimeDuration = " + this.mTimeDuration + ", mIsPublicAccountMsg = " + this.mIsPublicAccountMsg + ", mFileFingerPrint = " + this.mFileFingerPrint + "]";
    }
}
