package com.sec.internal.ims.fcm.interfaces;

import android.content.Context;
import java.util.Map;

public interface IFcmHandler {
    void onMessageReceived(Context context, String str, Map map);

    void onTokenRefresh(Context context);

    void registerFcmEventListener(IFcmEventListener iFcmEventListener);

    void unregisterFcmEventListener(IFcmEventListener iFcmEventListener);
}
