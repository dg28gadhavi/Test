package com.sec.internal.ims.core;

import android.content.Context;
import android.os.Handler;
import android.os.Looper;
import android.os.SystemClock;
import android.provider.Settings;
import android.util.Log;
import android.util.NtpTrustedTime;
import com.sec.internal.ims.core.handler.secims.StackIF;
import com.sec.internal.interfaces.ims.core.INtpTimeChangedListener;
import com.sec.internal.interfaces.ims.core.INtpTimeController;
import com.sec.internal.log.IMSLog;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class NtpTimeController extends Handler implements INtpTimeController {
    private static final String LOG_TAG = NtpTimeController.class.getSimpleName();
    private boolean isForceRefreshed = false;
    private Context mContext;
    private final ExecutorService mExecutorService = Executors.newSingleThreadExecutor();
    private ArrayList<INtpTimeChangedListener> mNtpTimeChangedListnerList = new ArrayList<>();
    private long mNtpTimeOffset = 0;
    private NtpTrustedTime mNtpTrustedTime;

    public NtpTimeController(Context context, Looper looper) {
        super(looper);
        this.mContext = context;
        this.mNtpTrustedTime = NtpTrustedTime.getInstance(context);
    }

    public void registerNtpTimeChangedListener(INtpTimeChangedListener iNtpTimeChangedListener) {
        boolean contains = this.mNtpTimeChangedListnerList.contains(iNtpTimeChangedListener);
        String str = LOG_TAG;
        IMSLog.s(str, "registerNtpTimeChangedListener: alreadyRegistered=" + contains);
        if (!contains && iNtpTimeChangedListener != null) {
            try {
                this.mNtpTimeChangedListnerList.add(iNtpTimeChangedListener);
                iNtpTimeChangedListener.onNtpTimeOffsetChanged(this.mNtpTimeOffset);
            } catch (Exception e) {
                Log.e(LOG_TAG, e.getMessage(), e);
            }
        }
    }

    public void unregisterNtpTimeChangedListener(INtpTimeChangedListener iNtpTimeChangedListener) {
        IMSLog.s(LOG_TAG, "unregisterNtpTimeChangedListener:");
        if (iNtpTimeChangedListener != null) {
            try {
                this.mNtpTimeChangedListnerList.remove(iNtpTimeChangedListener);
            } catch (Exception e) {
                Log.e(LOG_TAG, e.getMessage(), e);
            }
        }
    }

    public void refreshNtpTime() {
        requestNtpTime(true);
    }

    private void updateNtpTimeOffset(long j, int i) {
        String str = LOG_TAG;
        Log.i(str, "updateNtpTimeOffset (" + i + ") : " + j);
        this.mNtpTimeOffset = j;
        StackIF.getInstance().updateNtpTimeOffset(j);
        sendNtpTimeOffsetChanged(j);
    }

    private void sendNtpTimeOffsetChanged(long j) {
        Iterator<INtpTimeChangedListener> it = this.mNtpTimeChangedListnerList.iterator();
        while (it.hasNext()) {
            try {
                it.next().onNtpTimeOffsetChanged(j);
            } catch (Exception e) {
                Log.e(LOG_TAG, "sendNtpTimeOffsetChanged failed", e);
            }
        }
    }

    public void initSequentially() {
        requestNtpTime(false);
    }

    private synchronized void requestNtpTime(boolean z) {
        Object obj;
        boolean isAutomaticTimeRequested = isAutomaticTimeRequested(this.mContext);
        String str = LOG_TAG;
        StringBuilder sb = new StringBuilder();
        sb.append("requestNtpTime : forceRefresh=");
        sb.append(z);
        sb.append(" isForceRefreshed=");
        sb.append(this.isForceRefreshed);
        sb.append(" isAutomaticTimeRequested=");
        sb.append(isAutomaticTimeRequested);
        sb.append(" hasCache=");
        NtpTrustedTime ntpTrustedTime = this.mNtpTrustedTime;
        if (ntpTrustedTime != null) {
            obj = Boolean.valueOf(ntpTrustedTime.getCachedTimeResult() != null);
        } else {
            obj = "null";
        }
        sb.append(obj);
        Log.i(str, sb.toString());
        try {
            if (this.isForceRefreshed) {
                sendNtpTimeOffsetChanged(this.mNtpTimeOffset);
            } else if (z) {
                this.mExecutorService.submit(new NtpTimeController$$ExternalSyntheticLambda0(this));
            } else if (isAutomaticTimeRequested) {
                updateNtpTimeOffset(System.currentTimeMillis() - SystemClock.elapsedRealtime(), 0);
            } else {
                updateNtpTimeOffset(-1, 0);
            }
        } catch (Exception e) {
            Log.e(LOG_TAG, e.getMessage());
        }
        return;
    }

    /* access modifiers changed from: private */
    public /* synthetic */ void lambda$requestNtpTime$0() {
        try {
            NtpTrustedTime ntpTrustedTime = this.mNtpTrustedTime;
            if (ntpTrustedTime != null && ntpTrustedTime.getCachedTimeResult() == null) {
                long currentTimeMillis = System.currentTimeMillis();
                if (this.mNtpTrustedTime.forceRefresh()) {
                    updateNtpTimeOffset(this.mNtpTrustedTime.getCachedTimeResult().currentTimeMillis() - this.mNtpTrustedTime.getCachedTimeResult().getElapsedRealtimeMillis(), (int) (System.currentTimeMillis() - currentTimeMillis));
                    this.isForceRefreshed = true;
                    return;
                }
                IMSLog.s(LOG_TAG, "forceRefresh failed");
            }
        } catch (Exception e) {
            Log.e(LOG_TAG, e.getMessage());
        }
    }

    private static boolean isAutomaticTimeRequested(Context context) {
        boolean z = false;
        if (Settings.Global.getInt(context.getContentResolver(), "auto_time", 0) != 0) {
            z = true;
        }
        String str = LOG_TAG;
        IMSLog.s(str, "isAutomaticTimeRequested : " + z);
        return z;
    }
}
