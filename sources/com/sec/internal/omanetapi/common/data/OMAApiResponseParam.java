package com.sec.internal.omanetapi.common.data;

import com.sec.internal.ims.cmstore.omanetapi.tmoappapi.data.VvmFolders;
import com.sec.internal.ims.cmstore.omanetapi.tmoappapi.data.VvmServiceProfile;
import com.sec.internal.omanetapi.nc.data.NotificationChannel;
import com.sec.internal.omanetapi.nc.data.NotificationChannelLifetime;
import com.sec.internal.omanetapi.nc.data.NotificationChannelList;
import com.sec.internal.omanetapi.nc.data.NotificationList;
import com.sec.internal.omanetapi.nms.data.BulkResponseList;
import com.sec.internal.omanetapi.nms.data.Empty;
import com.sec.internal.omanetapi.nms.data.NmsSubscription;
import com.sec.internal.omanetapi.nms.data.Object;
import com.sec.internal.omanetapi.nms.data.ObjectList;
import com.sec.internal.omanetapi.nms.data.Reference;

public class OMAApiResponseParam {
    public BulkResponseList bulkResponseList;
    public Empty empty;
    public FileUploadResponse file;
    public VvmFolders folder;
    public NmsSubscription nmsSubscription;
    public NotificationChannel notificationChannel;
    public NotificationChannelLifetime notificationChannelLifetime;
    public NotificationChannelList[] notificationChannelList;
    public NotificationList[] notificationList;
    public Object object;
    public ObjectList objectList;
    public Reference reference;
    public RequestError requestError;
    public VvmServiceProfile vvmserviceProfile;
}
