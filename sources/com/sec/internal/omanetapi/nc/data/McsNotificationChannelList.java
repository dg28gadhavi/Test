package com.sec.internal.omanetapi.nc.data;

import java.util.Arrays;

public class McsNotificationChannelList {
    public McsNotificationChannel[] notificationChannel;
    public String resourceURL;

    public String toString() {
        return "notificationChannelList{ notificationChannel: " + Arrays.toString(this.notificationChannel) + " resourceURL: " + this.resourceURL + " }";
    }
}
