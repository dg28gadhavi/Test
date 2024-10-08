package com.samsung.android.cmcp2phelper;

public class MdmnServiceInfo {
    String deviceId;
    String lineId;
    String serviceName = "samsung_cmc";

    public String toString() {
        return "com.samsung.android.cmcp2phelper.MdmnServiceInfo{serviceName='" + this.serviceName + '\'' + ", deviceId='" + this.deviceId + '\'' + '}';
    }

    public MdmnServiceInfo(String str, String str2) {
        this.lineId = str2;
        this.deviceId = str;
    }

    public String getLineId() {
        return this.lineId;
    }

    public String getDeviceId() {
        return this.deviceId;
    }
}
