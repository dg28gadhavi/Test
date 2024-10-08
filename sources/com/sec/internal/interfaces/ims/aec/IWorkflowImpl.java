package com.sec.internal.interfaces.ims.aec;

public interface IWorkflowImpl {
    void changeConnectivity();

    void clearAkaToken();

    void clearResource();

    void dump();

    String getAkaToken();

    boolean getEntitlementForSMSoIp();

    boolean getEntitlementForVoLte();

    boolean getEntitlementForVoWiFi();

    boolean getEntitlementInitFromApp();

    boolean getSMSoIpEntitlementStatus();

    boolean getVoLteEntitlementStatus();

    boolean getVoWiFiEntitlementStatus();

    void initWorkflow(int i, String str, String str2);

    boolean isEntitlementOngoing();

    boolean isReadyToNotifyApp();

    boolean isSharedAkaToken();

    void performEntitlement(Object obj);

    void receivedSmsNotification(String str);

    void setReadyToNotifyApp(boolean z);

    void setSharedAkaToken(boolean z);

    void setValidEntitlement(boolean z);

    void triggerAutoConfigForApp();

    void updateFcmToken(String str, String str2);
}
