package com.sec.internal.omanetapi.nc.data;

public class McsNotificationChannel {
    public String callbackURL;
    public McsChannelData channelData;
    public int channelLifetime;
    public String channelType;
    public String resourceURL;

    public String toString() {
        return "notificationChannel{ channelType: " + this.channelType + " channelData: " + this.channelData + " channelLifetime: " + this.channelLifetime + " callbackURL: " + this.callbackURL + " resourceURL: " + this.resourceURL + " }";
    }
}
