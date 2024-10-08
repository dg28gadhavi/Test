package com.sec.internal.omanetapi.common.data;

import com.sec.internal.omanetapi.nc.data.McsLargePollingNotification;
import com.sec.internal.omanetapi.nc.data.McsNotificationChannel;
import com.sec.internal.omanetapi.nc.data.McsNotificationChannelLifetime;
import com.sec.internal.omanetapi.nc.data.McsNotificationChannelList;
import com.sec.internal.omanetapi.nc.data.SyncBlockfilter;
import com.sec.internal.omanetapi.nc.data.SyncConfig;
import com.sec.internal.omanetapi.nc.data.SyncContact;
import com.sec.internal.omanetapi.nc.data.SyncMessage;
import com.sec.internal.omanetapi.nc.data.SyncStatus;
import com.sec.internal.omanetapi.nms.data.NmsEventList;

public class McsOMAApiResponseParam {
    public LargeFileResponse LargefileResponse;
    public FileUploadResponse fileResponse;
    public McsLargePollingNotification largePollingNotification;
    public String mdn;
    public boolean ncListComplete;
    public NmsEventList nmsEventList;
    public McsNotificationChannel notificationChannel;
    public McsNotificationChannelLifetime notificationChannelLifetime;
    public McsNotificationChannelList notificationChannelList;
    public RequestError requestError;
    public SyncBlockfilter syncBlockfilter;
    public SyncConfig syncConfig;
    public SyncContact syncContact;
    public SyncMessage syncMessage;
    public SyncStatus syncStatus;

    public String toString() {
        return "McsOMAApiResponseParam { notificationChannel = " + this.notificationChannel + ", notificationChannelList = " + this.notificationChannelList + ", notificationChannelLifetime = " + this.notificationChannelLifetime + ", requestError = " + this.requestError + ", nmsEventList = " + this.nmsEventList + ", largePollingNotification = " + this.largePollingNotification + ", ncListComplete = " + this.ncListComplete + ", syncStatus = " + this.syncStatus + ", syncContact = " + this.syncContact + ", syncConfig = " + this.syncConfig + ", syncMessage = " + this.syncMessage + ", syncBlockfilter = " + this.syncBlockfilter + " }";
    }
}
