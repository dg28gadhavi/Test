package com.sec.internal.interfaces.ims.core;

import android.telephony.CellInfo;
import android.telephony.PreciseDataConnectionState;
import java.util.List;

public interface NetworkStateListener {
    void onCellInfoChanged(List<CellInfo> list, int i);

    void onDataConnectionStateChanged(int i, boolean z, int i2);

    void onDefaultNetworkStateChanged(int i);

    void onEpdgConnected(int i);

    void onEpdgDeregisterRequested(int i);

    void onEpdgDisconnected(int i);

    void onEpdgHandoverEnableChanged(int i, boolean z);

    void onEpdgIpsecDisconnected(int i);

    void onEpdgRegisterRequested(int i, boolean z);

    void onIKEAuthFAilure(int i);

    void onMobileRadioConnected(int i);

    void onMobileRadioDisconnected(int i);

    void onPreciseDataConnectionStateChanged(int i, PreciseDataConnectionState preciseDataConnectionState);
}
