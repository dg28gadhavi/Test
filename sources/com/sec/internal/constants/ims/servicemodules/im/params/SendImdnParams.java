package com.sec.internal.constants.ims.servicemodules.im.params;

import android.os.Message;
import com.sec.ims.util.ImsUri;
import com.sec.internal.constants.ims.servicemodules.im.ImImdnRecRoute;
import com.sec.internal.constants.ims.servicemodules.im.NotificationStatus;
import com.sec.internal.log.IMSLog;
import java.util.Collections;
import java.util.Date;
import java.util.List;
import java.util.Map;

public final class SendImdnParams {
    public final Message mCallback;
    public final String mChatId;
    public String mContributionId;
    public String mConversationId;
    public final Date mCpimDate;
    public final String mDeviceId;
    public Map<String, String> mImExtensionMNOHeaders;
    public final List<ImdnData> mImdnDataList;
    public boolean mIsBotSessionAnonymized;
    public boolean mIsGroupChat;
    public String mOwnImsi;
    public final Object mRawHandle;
    public final ImsUri mUri;
    public String mUserAlias;

    public static class ImdnData {
        public final Date mImdnDate;
        public final String mImdnId;
        public final String mImdnOriginalTo;
        public final List<ImImdnRecRoute> mImdnRecRouteList;
        public final NotificationStatus mStatus;

        public ImdnData(NotificationStatus notificationStatus, String str, Date date, List<ImImdnRecRoute> list, String str2) {
            this.mStatus = notificationStatus;
            this.mImdnId = str;
            this.mImdnDate = date;
            this.mImdnRecRouteList = list;
            this.mImdnOriginalTo = str2;
        }

        public String toString() {
            return "ImdnData [mStatus=" + this.mStatus + ", mImdnId=" + this.mImdnId + ", mImdnDate=" + this.mImdnDate + ", mImdnRecRouteList=" + this.mImdnRecRouteList + ", mImdnOriginalTo=" + IMSLog.numberChecker(this.mImdnOriginalTo) + "]";
        }
    }

    public SendImdnParams(Object obj, ImsUri imsUri, String str, String str2, String str3, String str4, Message message, String str5, ImdnData imdnData, boolean z, Date date, boolean z2, String str6) {
        this(obj, imsUri, str, str2, str3, str4, message, str5, (List<ImdnData>) Collections.singletonList(imdnData), z, date, z2, str6);
    }

    public SendImdnParams(Object obj, ImsUri imsUri, String str, String str2, String str3, String str4, Message message, String str5, List<ImdnData> list, boolean z, Date date, boolean z2, String str6) {
        this.mRawHandle = obj;
        this.mUri = imsUri;
        this.mChatId = str;
        this.mConversationId = str2;
        this.mContributionId = str3;
        this.mOwnImsi = str4;
        this.mCallback = message;
        this.mDeviceId = str5;
        this.mImdnDataList = list;
        this.mIsGroupChat = z;
        this.mIsBotSessionAnonymized = z2;
        this.mCpimDate = date;
        this.mUserAlias = str6;
    }

    public void addImExtensionMNOHeaders(Map<String, String> map) {
        this.mImExtensionMNOHeaders = map;
    }

    public String toString() {
        return "SendImdnParams [mRawHandle=" + this.mRawHandle + ", mUri=" + IMSLog.numberChecker(this.mUri) + ", mChatId=" + this.mChatId + ", mConversationId=" + this.mConversationId + ", mContributionId=" + this.mContributionId + ", mImdnDataList=" + this.mImdnDataList + ", mDeviceId=" + IMSLog.checker(this.mDeviceId) + ", mImExtensionMNOHeaders=" + this.mImExtensionMNOHeaders + ", mCallback=" + this.mCallback + ", mIsGroupChat=" + this.mIsGroupChat + ", mCpimDate=" + this.mCpimDate + ", mUserAlias=" + IMSLog.checker(this.mUserAlias) + "]";
    }
}
