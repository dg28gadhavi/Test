package com.samsung.android.cmcsetting;

import com.samsung.android.cmcsetting.CmcSettingManagerConstants;

public class CmcDeviceInfo {
    private CmcSettingManagerConstants.DeviceCategory mDeviceCategory = null;
    private String mDeviceId = "";
    private String mDeviceName = "";
    private CmcSettingManagerConstants.DeviceType mDeviceType = null;
    private boolean mIsActivation = false;
    private boolean mIsCallActivation = false;
    private boolean mIsCallAllowedSdByPd = false;
    private boolean mIsEmergencyCallSupported = false;
    private boolean mIsMessageActivation = false;
    private boolean mIsMessageAllowedSdByPd = false;

    public void setDeviceId(String str) {
        this.mDeviceId = str;
    }

    public void setDeviceName(String str) {
        this.mDeviceName = str;
    }

    public void setDeviceCategory(CmcSettingManagerConstants.DeviceCategory deviceCategory) {
        this.mDeviceCategory = deviceCategory;
    }

    public void setDeviceType(CmcSettingManagerConstants.DeviceType deviceType) {
        this.mDeviceType = deviceType;
    }

    public CmcSettingManagerConstants.DeviceType getDeviceType() {
        return this.mDeviceType;
    }

    public void setMessageAllowedSdByPd(boolean z) {
        this.mIsMessageAllowedSdByPd = z;
    }

    public void setCallAllowedSdByPd(boolean z) {
        this.mIsCallAllowedSdByPd = z;
    }

    public void setActivation(boolean z) {
        this.mIsActivation = z;
    }

    public void setMessageActivation(boolean z) {
        this.mIsMessageActivation = z;
    }

    public void setCallActivation(boolean z) {
        this.mIsCallActivation = z;
    }

    public boolean isEmergencyCallSupported() {
        return this.mIsEmergencyCallSupported;
    }

    public void setEmergencyCallSupported(boolean z) {
        this.mIsEmergencyCallSupported = z;
    }

    public String toString() {
        return (((((((((("{" + "deviceId:" + this.mDeviceId) + ",deviceName:" + this.mDeviceName) + ",deviceCategory:" + this.mDeviceCategory) + ",deviceType:" + this.mDeviceType) + ",isCallAllowedSdByPd:" + this.mIsCallAllowedSdByPd) + ",isMessageAllowedSdByPd:" + this.mIsMessageAllowedSdByPd) + ",isActivation:" + this.mIsActivation) + ",isMessageActivation:" + this.mIsMessageActivation) + ",isCallActivation:" + this.mIsCallActivation) + ",isEmergencyCallSupported:" + this.mIsEmergencyCallSupported) + "}";
    }
}
