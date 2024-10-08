package com.sec.internal.omanetapi.nc.data;

public class McsLargeDataPolling {
    public int maxPollingNotifications;
    public boolean pollingEnabled;

    public String toString() {
        return "largeDataPolling{ pollingEnabled: " + this.pollingEnabled + " maxPollingNotifications: " + this.maxPollingNotifications + " }";
    }
}
