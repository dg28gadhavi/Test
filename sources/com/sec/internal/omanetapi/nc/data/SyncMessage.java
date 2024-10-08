package com.sec.internal.omanetapi.nc.data;

public class SyncMessage {
    public int cms_data_ttl = -1;
    public String syncType;

    public String toString() {
        return "SyncMessage { syncType: " + this.syncType + "cms_data_ttl: " + this.cms_data_ttl + " }";
    }
}
