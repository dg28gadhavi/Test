package com.sec.internal.ims.imsservice;

import android.content.Intent;
import android.os.IBinder;
import android.util.Log;
import com.sec.ims.extensions.Extensions;

public class SemGbaService extends ImsServiceBase {
    private static final String LOG_TAG = SemGbaService.class.getSimpleName();
    public static final String SERVICE_INTERFACE = "android.telephony.gba.GbaService";

    public SemGbaService() {
        Log.d(LOG_TAG, "GBA service created");
    }

    public void onCreate() {
        super.onCreate();
        Log.d(LOG_TAG, "onCreate");
    }

    public IBinder onBind(Intent intent) {
        String str = LOG_TAG;
        Log.d(str, "onBind:" + intent);
        if (Extensions.UserHandle.myUserId() != 0) {
            Log.d(str, "Do not allow bind on non-system user");
            return null;
        } else if (!SERVICE_INTERFACE.equals(intent.getAction())) {
            return null;
        } else {
            Log.d(str, "GbaService Bound.");
            return this.mBinder.getBinder("GbaService");
        }
    }
}
