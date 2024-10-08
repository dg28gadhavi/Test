package com.sec.internal.imsphone.cmc;

public interface ICmcConnectivityController {

    public enum ConnectType {
        Wifi,
        Wifi_HS,
        Internet
    }

    public enum DeviceType {
        PDevice,
        SDevice,
        None
    }

    DeviceType getDeviceType();

    int getP2pCallSessionId();

    DeviceType getP2pDeviceType();

    boolean isEnabledWifiDirectFeature();

    boolean isExistP2pConnection();

    boolean isWifiRegistered();

    void needP2pCallSession(boolean z);

    void setCmcActivation(boolean z);

    void setP2pCallSessionId(int i);

    void setP2pPD();

    void startNsdBind();

    void startP2pBind();

    void startRegi(String str, String str2);

    void stopP2p();

    void stopRegi();
}
