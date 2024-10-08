package com.sec.internal.interfaces.ims.aec;

import com.sec.internal.constants.ims.os.NetworkEvent;
import com.sec.internal.interfaces.ims.core.ISequentialInitializable;

public interface IAECModule extends ISequentialInitializable {
    void dump();

    String getAkaToken(int i);

    boolean getEntitlementForSMSoIp(int i);

    boolean getEntitlementForVoLte(int i);

    boolean getEntitlementForVoWiFi(int i);

    boolean getSMSoIpEntitlementStatus(int i);

    boolean getVoLteEntitlementStatus(int i);

    boolean getVoWiFiEntitlementStatus(int i);

    boolean isEntitlementRequired(int i);

    void onNetworkEventChanged(NetworkEvent networkEvent, NetworkEvent networkEvent2, int i);

    void triggerAutoConfigForApp(int i);
}
