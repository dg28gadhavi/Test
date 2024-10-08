package com.sec.internal.ims.aec.util;

import android.content.Context;
import android.os.PowerManager;
import com.sec.internal.log.AECLog;

public class PowerController {
    private final String LOG_TAG;
    private final int mPhoneId;
    private final String mTag;
    final PowerManager.WakeLock mWakeLock;

    public PowerController(Context context, int i) {
        String simpleName = PowerController.class.getSimpleName();
        this.LOG_TAG = simpleName;
        this.mPhoneId = i;
        String str = simpleName + i;
        this.mTag = str;
        this.mWakeLock = ((PowerManager) context.getSystemService("power")).newWakeLock(1, str);
    }

    public void lock(long j) {
        String str = this.LOG_TAG;
        AECLog.d(str, "lock: " + this.mTag, this.mPhoneId);
        this.mWakeLock.acquire(j);
    }

    public void release() {
        if (this.mWakeLock.isHeld()) {
            String str = this.LOG_TAG;
            AECLog.d(str, "release: " + this.mTag, this.mPhoneId);
            this.mWakeLock.release();
        }
    }

    public void sleep(long j) {
        String str = this.LOG_TAG;
        AECLog.d(str, "sleep: " + j, this.mPhoneId);
        try {
            Thread.sleep(j);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
    }
}
