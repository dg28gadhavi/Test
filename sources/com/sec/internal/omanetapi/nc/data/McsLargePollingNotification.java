package com.sec.internal.omanetapi.nc.data;

public class McsLargePollingNotification {
    public String channelExpiry;
    public String channelURL;

    public String toString() {
        return "largePollingNotification{ channelURL: " + this.channelURL + " channelExpiry: " + this.channelExpiry + " }";
    }
}
