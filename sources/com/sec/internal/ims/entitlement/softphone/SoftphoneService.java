package com.sec.internal.ims.entitlement.softphone;

import android.app.Service;
import android.content.Intent;
import android.os.IBinder;
import android.util.Log;
import com.sec.internal.log.IndentingPrintWriter;
import java.io.FileDescriptor;
import java.io.PrintWriter;

public class SoftphoneService extends Service {
    private static final String LOG_TAG = SoftphoneService.class.getSimpleName();
    private IBinder mBinder = null;
    private SoftphoneServiceStub mService = null;

    /* JADX WARNING: type inference failed for: r0v1, types: [com.sec.internal.ims.entitlement.softphone.SoftphoneServiceStub, android.os.IBinder] */
    public void onCreate() {
        super.onCreate();
        Log.i(LOG_TAG, "onCreate()");
        ? softphoneServiceStub = new SoftphoneServiceStub(this);
        this.mService = softphoneServiceStub;
        this.mBinder = softphoneServiceStub;
    }

    public int onStartCommand(Intent intent, int i, int i2) {
        String str = LOG_TAG;
        Log.i(str, "onStartCommand(): Received start id: " + i2 + ", flags: " + i + ", Intent: " + intent);
        return 1;
    }

    public IBinder onBind(Intent intent) {
        Log.i(LOG_TAG, "onBind");
        return this.mBinder;
    }

    /* access modifiers changed from: protected */
    public void dump(FileDescriptor fileDescriptor, PrintWriter printWriter, String[] strArr) {
        this.mService.dump(new IndentingPrintWriter(printWriter, "  "));
    }
}
