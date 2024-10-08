package com.sec.internal.google;

import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.ServiceConnection;
import android.os.IBinder;
import com.sec.internal.helper.os.SystemWrapper;
import com.sec.internal.imsphone.ImsConfigImpl;
import com.sec.internal.imsphone.ImsRegistrationImpl;
import com.sec.internal.imsphone.MmTelFeatureImpl;
import com.sec.internal.imsphone.RcsFeatureImpl;
import com.sec.internal.imsphone.SecImsService;
import com.sec.internal.imsphone.SipTransportImpl;
import com.sec.internal.log.IMSLog;
import java.util.Optional;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.Executor;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;

public class SecImsServiceConnector {
    private static final String LOG_TAG = "SecImsServiceConnector";
    /* access modifiers changed from: private */
    public CountDownLatch mConnectedLatch = new CountDownLatch(1);
    private Executor mExecutor = Executors.newSingleThreadExecutor();
    /* access modifiers changed from: private */
    public SecImsService mService;
    private ServiceConnection mServiceConnection = new ServiceConnection() {
        public void onServiceConnected(ComponentName componentName, IBinder iBinder) {
            IMSLog.i(SecImsServiceConnector.LOG_TAG, "onServiceConnected: " + componentName);
            SecImsServiceConnector.this.mService = ((SecImsService.LocalBinder) iBinder).getService();
            SecImsServiceConnector.this.mConnectedLatch.countDown();
        }

        public void onServiceDisconnected(ComponentName componentName) {
            IMSLog.i(SecImsServiceConnector.LOG_TAG, "onServiceDisconnected: " + componentName);
            SecImsServiceConnector.this.mService = null;
        }
    };

    public SecImsServiceConnector(Context context) {
        context.bindService(new Intent(context, SecImsService.class), 1, this.mExecutor, this.mServiceConnection);
    }

    private SecImsService requireService() {
        boolean z = false;
        for (int i = 1; i <= 10 && !z; i++) {
            IMSLog.e(LOG_TAG, "connect to local SecImsService #" + i + " attempt");
            try {
                z = this.mConnectedLatch.await(1, TimeUnit.SECONDS);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }
        if (!z) {
            IMSLog.e(LOG_TAG, "Cannot connect to local SecImsService");
            SystemWrapper.exit(0);
        }
        return this.mService;
    }

    public ImsConfigImpl getImsConfigImpl(int i) {
        IMSLog.i(LOG_TAG, i, "getImsConfigImpl");
        return requireService().getImsConfigImpl(i);
    }

    public MmTelFeatureImpl getMmTelFeatureImpl(int i) {
        IMSLog.i(LOG_TAG, i, "getMmTelFeatureImpl");
        return requireService().getMmTelFeatureImpl(i);
    }

    public RcsFeatureImpl getRcsFeatureImpl(int i) {
        IMSLog.i(LOG_TAG, i, "getRcsFeatureImpl");
        return requireService().getRcsFeatureImpl(i);
    }

    public ImsRegistrationImpl getImsRegistrationImpl(int i) {
        IMSLog.i(LOG_TAG, i, "getImsRegistrationImpl");
        return requireService().getImsRegistrationImpl(i);
    }

    public SipTransportImpl getSipTransportImpl(int i) {
        IMSLog.i(LOG_TAG, i, "getSipTransportImpl");
        return (SipTransportImpl) Optional.ofNullable(requireService().getSipTransport(i)).orElseThrow(new SecImsServiceConnector$$ExternalSyntheticLambda0());
    }

    /* access modifiers changed from: private */
    public static /* synthetic */ IllegalStateException lambda$getSipTransportImpl$0() {
        return new IllegalStateException("SipTransport should exist!");
    }
}
