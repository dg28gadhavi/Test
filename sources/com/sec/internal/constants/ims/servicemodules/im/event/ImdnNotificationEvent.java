package com.sec.internal.constants.ims.servicemodules.im.event;

import com.sec.ims.util.ImsUri;
import com.sec.internal.constants.ims.servicemodules.im.NotificationStatus;
import com.sec.internal.log.IMSLog;
import java.util.Date;

public final class ImdnNotificationEvent {
    public final String mConversationId;
    public final Date mCpimDate;
    public final Date mImdnDate;
    public final String mImdnId;
    public final String mOwnImsi;
    public ImsUri mRemoteUri;
    public final NotificationStatus mStatus;
    public final String mUserAlias;

    public ImdnNotificationEvent(String str, Date date, ImsUri imsUri, String str2, String str3, NotificationStatus notificationStatus, Date date2, String str4) {
        this.mImdnId = str;
        this.mImdnDate = date;
        this.mRemoteUri = imsUri;
        this.mConversationId = str2;
        this.mOwnImsi = str3;
        this.mStatus = notificationStatus;
        this.mCpimDate = date2;
        this.mUserAlias = str4;
    }

    public String toString() {
        return "ImdnNotificationEvent [mImdnId=" + this.mImdnId + ", mImdnDate=" + this.mImdnDate + ", mRemoteUri=" + IMSLog.numberChecker(this.mRemoteUri) + ", mConversationId=" + this.mConversationId + ", mOwnImsi=" + IMSLog.checker(this.mOwnImsi) + ", mStatus=" + this.mStatus + ", mCpimDate=" + this.mCpimDate + ", mUserAlias=" + IMSLog.checker(this.mUserAlias) + "]";
    }
}
