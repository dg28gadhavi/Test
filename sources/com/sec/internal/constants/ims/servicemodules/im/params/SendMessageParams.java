package com.sec.internal.constants.ims.servicemodules.im.params;

import android.os.Message;
import com.sec.ims.util.ImsUri;
import com.sec.internal.constants.ims.servicemodules.im.NotificationStatus;
import com.sec.internal.log.IMSLog;
import java.util.Date;
import java.util.Set;

public class SendMessageParams {
    public String mBody;
    public Message mCallback;
    public String mContentType;
    public Set<NotificationStatus> mDispositionNotification;
    public Set<ImsUri> mGroupCcList;
    public String mImdnMessageId;
    public Date mImdnTime;
    public String mMaapTrafficType;
    public Object mRawHandle;
    public String mReferenceId;
    public String mReferenceType;
    public String mReferenceValue;
    public String mUserAlias;

    public SendMessageParams(Object obj, String str, String str2, String str3, String str4, Date date, Set<NotificationStatus> set, Set<ImsUri> set2, Message message, String str5, String str6, String str7, String str8) {
        this.mRawHandle = obj;
        this.mBody = str;
        this.mUserAlias = str2;
        this.mContentType = str3;
        this.mImdnMessageId = str4;
        this.mImdnTime = date;
        this.mCallback = message;
        this.mDispositionNotification = set;
        this.mGroupCcList = set2;
        this.mMaapTrafficType = str5;
        this.mReferenceId = str6;
        this.mReferenceType = str7;
        this.mReferenceValue = str8;
    }

    public String toString() {
        return "SendMessageParams [mRawHandle=" + this.mRawHandle + ", mBody=" + IMSLog.checker(this.mBody) + ", mUserAlias=" + IMSLog.checker(this.mUserAlias) + ", mContentType=" + this.mContentType + ", mImdnMessageId=" + this.mImdnMessageId + ", mImdnTime=" + this.mImdnTime + ", mDispositionNotification=" + this.mDispositionNotification + ", mMaapTrafficType=" + this.mMaapTrafficType + ", mReferenceId=" + this.mReferenceId + ", mReferenceType=" + this.mReferenceType + ", mReferenceValue=" + this.mReferenceValue + "]";
    }
}
