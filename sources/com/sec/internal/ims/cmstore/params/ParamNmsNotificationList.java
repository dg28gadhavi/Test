package com.sec.internal.ims.cmstore.params;

import com.sec.internal.log.IMSLog;
import com.sec.internal.omanetapi.nms.data.GCMPushNotification;
import com.sec.internal.omanetapi.nms.data.NmsEventList;

public class ParamNmsNotificationList {
    public final int mDataContractType;
    public final String mDataType;
    public final String mLine;
    public final NmsEventList mNmsEventList;

    public ParamNmsNotificationList(String str, int i, String str2, GCMPushNotification gCMPushNotification) {
        this.mDataType = str;
        this.mDataContractType = i;
        this.mNmsEventList = gCMPushNotification.nmsEventList;
        this.mLine = str2;
    }

    public String toString() {
        return "ParamNmsNotificationList [mDataType= " + this.mDataType + " mDataContractType = " + this.mDataContractType + " mLine = " + IMSLog.checker(this.mLine) + " mOriginalMessage = ]";
    }
}
