package com.sec.internal.ims.imsservice;

import android.content.Intent;
import android.os.IBinder;
import android.util.Log;

public class QuantumEncryptionService extends ImsServiceBase {
    private static final String LOG_TAG = "QuantumEncryptionService";

    public void onCreate() {
        super.onCreate();
        Log.d(LOG_TAG, "onCreate");
    }

    public IBinder onBind(Intent intent) {
        Log.d(LOG_TAG, "onBind:" + intent);
        return this.mBinder.getBinder("quantum");
    }
}
