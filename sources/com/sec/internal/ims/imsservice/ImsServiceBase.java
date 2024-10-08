package com.sec.internal.ims.imsservice;

import android.app.Service;
import android.content.Intent;
import android.os.Build;
import android.os.IBinder;
import android.util.Log;
import com.samsung.android.ims.SemImsService;
import com.sec.ims.IImsService;
import com.sec.ims.extensions.Extensions;

public abstract class ImsServiceBase extends Service {
    private static final String LOG_TAG = ImsServiceBase.class.getSimpleName();
    protected IImsService.Stub mBinder = null;
    protected SemImsService.Stub mSemBinder = null;

    public int onStartCommand(Intent intent, int i, int i2) {
        return 1;
    }

    public void onCreate() {
        super.onCreate();
        try {
            if (Extensions.UserHandle.myUserId() != 0) {
                Log.d(LOG_TAG, "Do not initialize on non-system user");
                stopSelf();
                return;
            }
        } catch (IllegalStateException unused) {
            Log.e(LOG_TAG, "IllegalStateException occurred");
        }
        Log.i(LOG_TAG, "onCreate(): ");
        this.mBinder = ImsServiceStub.getInstance();
        if (Build.VERSION.SEM_INT >= 2716) {
            this.mSemBinder = SemImsServiceStub.getInstance();
        }
    }

    public IBinder onBind(Intent intent) {
        if (Extensions.UserHandle.myUserId() == 0) {
            return this.mBinder;
        }
        Log.d(LOG_TAG, "Do not allow bind on non-system user");
        return null;
    }
}
