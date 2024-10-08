package com.sec.internal.interfaces.ims.core;

import android.content.Intent;
import com.sec.epdg.EpdgManager;
import com.sec.ims.IEpdgListener;
import com.sec.internal.ims.core.WfcEpdgManager;

public interface IWfcEpdgManager extends ISequentialInitializable {
    void dump();

    EpdgManager getEpdgMgr();

    int getNrInterworkingMode(int i);

    boolean isCrossSimPermanentBlocked(int i);

    boolean isEpdgServiceConnected();

    void onCarrierUpdate(Intent intent);

    void onPermanentPdnFail();

    void onResetSetting(Intent intent);

    void registerEpdgHandoverListener(IEpdgListener iEpdgListener);

    void registerWfcEpdgConnectionListener(WfcEpdgManager.WfcEpdgConnectionListener wfcEpdgConnectionListener);

    void setCrossSimPermanentBlocked(int i, boolean z);

    void unRegisterEpdgHandoverListener(IEpdgListener iEpdgListener);
}
