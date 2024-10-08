package com.sec.internal.interfaces.ims.core;

import android.os.Bundle;
import java.util.List;

public interface ICmcAccountManager {
    Bundle getCmcRegiConfigForUserAgent();

    IRegisterTask getCmcRegisterTask(int i);

    String getCurrentLineOwnerDeviceId();

    int getCurrentLineSlotIndex();

    List<String> getRegiEventNotifyHostInfo();

    boolean hasSecondaryDevice();

    boolean isCmcActivated();

    boolean isCmcEnabled();

    boolean isCmcProfileAdded();

    boolean isEmergencyCallSupported();

    boolean isEmergencyNumber(String str, int i);

    boolean isPotentialEmergencyNumber(String str, int i);

    boolean isProfileUpdateFailed();

    boolean isSecondaryDevice();

    boolean isSupportDualSimCMC();

    boolean isSupportSameWiFiOnly();

    boolean isWifiOnly();

    void notifyCmcDeviceChanged();

    void onSimRefresh(int i);

    void setEmergencyNumbers(String str);

    void setRegiEventNotifyHostInfo(List<String> list);

    void startCmcRegistration();

    void startSAService(boolean z);
}
