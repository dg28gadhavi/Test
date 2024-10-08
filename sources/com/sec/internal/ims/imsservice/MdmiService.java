package com.sec.internal.ims.imsservice;

import android.content.Intent;
import android.os.IBinder;
import android.util.Log;
import com.sec.ims.extensions.Extensions;

public class MdmiService extends ImsServiceBase {
    private static final String LOG_TAG = MdmiService.class.getSimpleName();

    public void onCreate() {
        super.onCreate();
        if (Extensions.UserHandle.myUserId() != Extensions.ActivityManager.getCurrentUser()) {
            Log.e(LOG_TAG, "Do not initialize on background user");
            stopSelf();
            return;
        }
        Log.d(LOG_TAG, "onCreate");
    }

    public IBinder onBind(Intent intent) {
        String str = LOG_TAG;
        Log.d(str, "onBind:" + intent);
        if (Extensions.UserHandle.myUserId() == Extensions.ActivityManager.getCurrentUser()) {
            return this.mBinder.getBinder("mdmi");
        }
        Log.d(str, "Do not allow bind on background user");
        return null;
    }
}
