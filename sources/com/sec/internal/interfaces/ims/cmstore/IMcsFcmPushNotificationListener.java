package com.sec.internal.interfaces.ims.cmstore;

import com.sec.internal.omanetapi.nc.data.McsLargePollingNotification;
import com.sec.internal.omanetapi.nms.data.NmsEventList;

public interface IMcsFcmPushNotificationListener {
    void largePollingPushNotification(McsLargePollingNotification mcsLargePollingNotification);

    void nmsEventListPushNotification(NmsEventList nmsEventList);

    void syncBlockfilterPushNotification(String str);

    void syncConfigPushNotification(String str);

    void syncContactPushNotification(String str);

    void syncMessagePushNotification(String str, int i);

    void syncStatusPushNotification(String str);
}
