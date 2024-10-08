package com.sec.internal.omanetapi.nc.data;

public class McsChannelData extends ChannelData {
    public String channelSubType;
    public String channelSubTypeVersion;
    public McsLargeDataPolling largeDataPolling;
    public int maxNotifications;
    public String registrationToken;

    public String toString() {
        return "channelData{ channelSubType: " + this.channelSubType + " channelSubTypeVersion: " + this.channelSubTypeVersion + " registrationToken: " + this.registrationToken + " maxNotifications: " + this.maxNotifications + " largeDataPolling: " + this.largeDataPolling + " }";
    }
}
