package com.sec.internal.ims.config;

import android.app.AlarmManager;
import android.app.PendingIntent;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.PowerManager;
import android.util.Log;
import java.util.Arrays;
import java.util.List;

public class PowerController {
    /* access modifiers changed from: private */
    public final String LOG_TAG;
    protected AlarmManager mAlarmManager = null;
    private final Context mContext;
    protected PendingIntent mPendingIntent = null;
    protected final Receiver mReceiver;
    protected State mState = null;
    /* access modifiers changed from: private */
    public long mTimeout = 0;
    /* access modifiers changed from: private */
    public final PowerManager.WakeLock mWakeLock;

    interface State {
        void lock();

        void release();

        void sleep(long j);
    }

    public PowerController(Context context, long j) {
        String simpleName = PowerController.class.getSimpleName();
        this.LOG_TAG = simpleName;
        Receiver receiver = new Receiver();
        this.mReceiver = receiver;
        Log.i(simpleName, "PowerController");
        this.mContext = context;
        this.mWakeLock = ((PowerManager) context.getSystemService("power")).newWakeLock(1, "PowerController");
        this.mAlarmManager = (AlarmManager) context.getSystemService("alarm");
        this.mPendingIntent = PendingIntent.getBroadcast(context, 0, new Intent(receiver.ACTION_SLEEP_ALARM_EXPIRED).setPackage(context.getPackageName()), 33554432);
        context.registerReceiver(receiver, receiver.getIntentFilter());
        this.mTimeout = j;
        this.mState = new ReleaseState();
    }

    public void lock() {
        this.mState.lock();
    }

    public void release() {
        this.mState.release();
    }

    public void sleep(long j) {
        this.mState.sleep(j);
    }

    public void cleanup() {
        this.mContext.unregisterReceiver(this.mReceiver);
    }

    class ReleaseState implements State {
        public ReleaseState() {
            Log.i(PowerController.this.LOG_TAG, "ReleaseState");
            if (PowerController.this.mWakeLock.isHeld()) {
                PowerController.this.mWakeLock.release();
            }
        }

        public synchronized void lock() {
            PowerController powerController = PowerController.this;
            powerController.mState = new LockState();
        }

        public synchronized void release() {
            Log.i(PowerController.this.LOG_TAG, "already released");
        }

        public synchronized void sleep(long j) {
            String r0 = PowerController.this.LOG_TAG;
            Log.i(r0, "+++ sleep start:" + j);
            try {
                Thread.sleep(j);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
            Log.i(PowerController.this.LOG_TAG, "--- sleep end");
        }
    }

    class LockState implements State {
        public LockState() {
            Log.i(PowerController.this.LOG_TAG, "LockState");
            PowerController.this.mWakeLock.acquire(PowerController.this.mTimeout);
        }

        public synchronized void lock() {
            Log.i(PowerController.this.LOG_TAG, "already locked");
        }

        public synchronized void release() {
            PowerController powerController = PowerController.this;
            powerController.mState = new ReleaseState();
        }

        public synchronized void sleep(long j) {
            long j2;
            String r0 = PowerController.this.LOG_TAG;
            Log.i(r0, "+++ sleep start:" + j);
            if (j > 1000) {
                j2 = 100;
            } else {
                j2 = j / 10;
            }
            PowerController.this.mAlarmManager.setExact(0, System.currentTimeMillis() + (j - j2), PowerController.this.mPendingIntent);
            release();
            try {
                Thread.sleep(j);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
            PowerController.this.mState.lock();
            PowerController powerController = PowerController.this;
            powerController.mAlarmManager.cancel(powerController.mPendingIntent);
            Log.i(PowerController.this.LOG_TAG, "--- sleep end");
        }
    }

    protected class Receiver extends BroadcastReceiver {
        public final String ACTION_SLEEP_ALARM_EXPIRED;
        private IntentFilter mIntentFilter = null;
        private final List<String> mIntentFilterAction;

        public Receiver() {
            String str = Receiver.class.getName() + ".SLEEP_ALARM_EXPIRED";
            this.ACTION_SLEEP_ALARM_EXPIRED = str;
            List<String> asList = Arrays.asList(new String[]{str});
            this.mIntentFilterAction = asList;
            Log.i(PowerController.this.LOG_TAG, "Receiver");
            this.mIntentFilter = new IntentFilter();
            for (String addAction : asList) {
                this.mIntentFilter.addAction(addAction);
            }
        }

        public void onReceive(Context context, Intent intent) {
            Log.i(PowerController.this.LOG_TAG, intent.getAction());
            if (intent.getAction().equals(this.ACTION_SLEEP_ALARM_EXPIRED)) {
                Log.i(PowerController.this.LOG_TAG, "received alarm expired. acquire wake lock");
                PowerController.this.mState.lock();
            }
        }

        public IntentFilter getIntentFilter() {
            return this.mIntentFilter;
        }
    }
}
