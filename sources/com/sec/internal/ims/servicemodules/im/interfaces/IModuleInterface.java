package com.sec.internal.ims.servicemodules.im.interfaces;

import android.content.Context;

public interface IModuleInterface {
    IRcsBigDataProcessor getBigDataProcessor();

    Context getContext();

    String getUserAlias(int i, boolean z);

    boolean isWifiConnected();
}
