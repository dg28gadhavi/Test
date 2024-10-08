package com.sec.internal.helper.os;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.util.Log;
import java.util.HashSet;
import java.util.Iterator;

public class ImsFrameworkState {
    private static final String LOG_TAG = "ImsFrameworkState";
    /* access modifiers changed from: private */
    public static final Object MUTEX = new Object();
    private static volatile ImsFrameworkState sInstance = null;
    /* access modifiers changed from: private */
    public static volatile boolean sIsFwReady = false;
    /* access modifiers changed from: private */
    public Context mContext;
    /* access modifiers changed from: private */
    public BroadcastReceiver mFwStatusReceiver;
    private HashSet<FrameworkStateListener> mListeners = null;

    public interface FrameworkStateListener {
        void onFwReady();
    }

    private ImsFrameworkState(Context context) {
        this.mContext = context;
        this.mListeners = new HashSet<>();
        this.mFwStatusReceiver = new BroadcastReceiver() {
            public void onReceive(Context context, Intent intent) {
                if ("com.sec.ims.imsmanager.RESTART".equals(intent.getAction())) {
                    Log.i(ImsFrameworkState.LOG_TAG, "ImsService is ready.");
                    synchronized (ImsFrameworkState.MUTEX) {
                        ImsFrameworkState.sIsFwReady = true;
                        ImsFrameworkState.this.mContext.unregisterReceiver(ImsFrameworkState.this.mFwStatusReceiver);
                        ImsFrameworkState.this.mContext = null;
                        ImsFrameworkState.this.notifyFrameworkState();
                    }
                }
            }
        };
        IntentFilter intentFilter = new IntentFilter();
        intentFilter.addAction("com.sec.ims.imsmanager.RESTART");
        this.mContext.registerReceiver(this.mFwStatusReceiver, intentFilter);
    }

    public static boolean isFrameworkReady() {
        return sIsFwReady;
    }

    public static ImsFrameworkState getInstance(Context context) {
        ImsFrameworkState imsFrameworkState = sInstance;
        if (imsFrameworkState == null) {
            synchronized (MUTEX) {
                imsFrameworkState = sInstance;
                if (imsFrameworkState == null) {
                    imsFrameworkState = new ImsFrameworkState(context);
                    sInstance = imsFrameworkState;
                }
            }
        }
        return imsFrameworkState;
    }

    /* JADX WARNING: Code restructure failed: missing block: B:12:0x001c, code lost:
        return;
     */
    /* Code decompiled incorrectly, please refer to instructions dump. */
    public void registerForFrameworkState(com.sec.internal.helper.os.ImsFrameworkState.FrameworkStateListener r3) {
        /*
            r2 = this;
            java.lang.String r0 = "ImsFrameworkState"
            java.lang.String r1 = "registerForFrameworkState."
            android.util.Log.i(r0, r1)
            java.lang.Object r0 = MUTEX
            monitor-enter(r0)
            boolean r1 = sIsFwReady     // Catch:{ all -> 0x001d }
            if (r1 == 0) goto L_0x0014
            r3.onFwReady()     // Catch:{ all -> 0x001d }
            monitor-exit(r0)     // Catch:{ all -> 0x001d }
            return
        L_0x0014:
            java.util.HashSet<com.sec.internal.helper.os.ImsFrameworkState$FrameworkStateListener> r2 = r2.mListeners     // Catch:{ all -> 0x001d }
            if (r2 == 0) goto L_0x001b
            r2.add(r3)     // Catch:{ all -> 0x001d }
        L_0x001b:
            monitor-exit(r0)     // Catch:{ all -> 0x001d }
            return
        L_0x001d:
            r2 = move-exception
            monitor-exit(r0)     // Catch:{ all -> 0x001d }
            throw r2
        */
        throw new UnsupportedOperationException("Method not decompiled: com.sec.internal.helper.os.ImsFrameworkState.registerForFrameworkState(com.sec.internal.helper.os.ImsFrameworkState$FrameworkStateListener):void");
    }

    /* access modifiers changed from: private */
    public void notifyFrameworkState() {
        HashSet<FrameworkStateListener> hashSet = this.mListeners;
        if (hashSet != null) {
            Iterator<FrameworkStateListener> it = hashSet.iterator();
            while (it.hasNext()) {
                it.next().onFwReady();
            }
            this.mListeners.clear();
        }
    }
}
