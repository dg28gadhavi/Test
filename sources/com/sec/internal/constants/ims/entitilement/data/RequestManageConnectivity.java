package com.sec.internal.constants.ims.entitilement.data;

import com.google.gson.annotations.SerializedName;

public class RequestManageConnectivity extends NSDSRequest {
    public String csr;
    @SerializedName("device-group")
    public String deviceGroup;
    @SerializedName("device-parameter-info")
    public DeviceParameter deviceParameterInfo;
    public int operation;
    @SerializedName("remote-device-id")
    public String remoteDeviceId;
    public String vimsi;

    public static class DeviceParameter {
        @SerializedName("tracking-area-code")
        public Integer areacode;
        @SerializedName("carrier-configuration-client-version")
        public String clientversion;
        @SerializedName("cached-configuration-file-version")
        public String configversion;
        @SerializedName("msisdn")
        public String msisdn;
        @SerializedName("action-trigger-code")
        public Integer triggercode;
    }
}
