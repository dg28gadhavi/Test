package com.sec.internal.helper;

import android.app.PendingIntent;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Build;
import android.os.Message;
import android.os.PowerManager;
import android.os.SystemClock;
import android.util.Log;
import com.sec.internal.log.IMSLog;
import java.util.Iterator;
import java.util.concurrent.PriorityBlockingQueue;

public class PreciseAlarmManager {
    protected static final String INTENT_ALARM_TIMEOUT = "com.sec.internal.ims.imsservice.alarmmanager";
    private static final String LOG_TAG = "PreciseAlarmManager";
    private static final int PRECISION = 250;
    private static final int WAKE_LOCK_TIMEOUT = 10000;
    private static volatile PreciseAlarmManager sInstance;
    Context mContext = null;
    private final BroadcastReceiver mIntentReceiver;
    SimpleEventLog mLog = null;
    Thread mThread = null;
    /* access modifiers changed from: private */
    public final PriorityBlockingQueue<DelayedMessage> mTimers = new PriorityBlockingQueue<>();
    PowerManager.WakeLock mWakeLock;

    public static synchronized PreciseAlarmManager getInstance(Context context) {
        PreciseAlarmManager preciseAlarmManager;
        synchronized (PreciseAlarmManager.class) {
            if (sInstance == null) {
                synchronized (PreciseAlarmManager.class) {
                    if (sInstance == null) {
                        sInstance = new PreciseAlarmManager(context);
                        if (!isRoboUnitTest()) {
                            sInstance.start();
                        }
                    }
                }
            }
            preciseAlarmManager = sInstance;
        }
        return preciseAlarmManager;
    }

    public static boolean isRoboUnitTest() {
        return "robolectric".equals(Build.FINGERPRINT);
    }

    private PreciseAlarmManager(Context context) {
        AnonymousClass2 r0 = new BroadcastReceiver() {
            public void onReceive(Context context, Intent intent) {
                Log.d(PreciseAlarmManager.LOG_TAG, "sendMessageDelayed: get intent, get wake lock for 10secs.");
                PreciseAlarmManager.this.mWakeLock.acquire(10000);
            }
        };
        this.mIntentReceiver = r0;
        this.mContext = context;
        IntentFilter intentFilter = new IntentFilter();
        intentFilter.addAction(INTENT_ALARM_TIMEOUT);
        this.mContext.registerReceiver(r0, intentFilter);
        createWakelock();
        this.mLog = new SimpleEventLog(context, LOG_TAG, 500);
    }

    /* access modifiers changed from: private */
    public void registerAlarmManager() {
        synchronized (this.mTimers) {
            if (this.mTimers.size() > 0) {
                Iterator<DelayedMessage> it = this.mTimers.iterator();
                while (true) {
                    if (!it.hasNext()) {
                        break;
                    }
                    DelayedMessage next = it.next();
                    Message msg = next.getMsg();
                    if (msg != null) {
                        if (msg.getTarget() != null) {
                            long timeout = next.getTimeout() - SystemClock.elapsedRealtime();
                            Log.d(LOG_TAG, "next the soonest timer: " + msg.what + " from " + msg.getTarget() + " timeout=" + next.getTimeout() + " after msec=" + timeout);
                            if (timeout > 0) {
                                Intent intent = new Intent(INTENT_ALARM_TIMEOUT);
                                intent.setPackage(this.mContext.getPackageName());
                                AlarmTimer.start(this.mContext, PendingIntent.getBroadcast(this.mContext, 0, intent, 33554432), timeout);
                                break;
                            }
                        }
                    }
                    Log.e(LOG_TAG, "message is wrong do not handle");
                }
            } else {
                Log.d(LOG_TAG, "No pended alarm Timer. remove the registered timer from alarmManager.");
                Intent intent2 = new Intent(INTENT_ALARM_TIMEOUT);
                intent2.setPackage(this.mContext.getPackageName());
                AlarmTimer.stop(this.mContext, PendingIntent.getBroadcast(this.mContext, 0, intent2, 33554432));
            }
        }
    }

    public synchronized DelayedMessage sendMessageDelayed(String str, Message message, long j) {
        DelayedMessage delayedMessage;
        synchronized (this.mTimers) {
            delayedMessage = new DelayedMessage(message, SystemClock.elapsedRealtime() + j);
            this.mTimers.put(delayedMessage);
            Log.d(LOG_TAG, "sendMessageDelayed: " + delayedMessage + ", remaining timers:" + this.mTimers.size());
        }
        wakeLockInfo(str, message, j);
        registerAlarmManager();
        return delayedMessage;
    }

    public synchronized void removeMessage(Message message) {
        Log.d(LOG_TAG, "removeMessage: " + message.what);
        this.mTimers.remove(new DelayedMessage(message, 0));
        registerAlarmManager();
    }

    public synchronized void removeMessage(DelayedMessage delayedMessage) {
        Log.d(LOG_TAG, "removeMessage: " + delayedMessage);
        this.mTimers.remove(delayedMessage);
        registerAlarmManager();
    }

    private void start() {
        Thread thread = new Thread(new Runnable() {
            public void run() {
                while (true) {
                    int size = PreciseAlarmManager.this.mTimers.size();
                    if (size > 0) {
                        long elapsedRealtime = SystemClock.elapsedRealtime();
                        Iterator it = PreciseAlarmManager.this.mTimers.iterator();
                        while (it.hasNext()) {
                            DelayedMessage delayedMessage = (DelayedMessage) it.next();
                            Message msg = delayedMessage.getMsg();
                            if (msg != null && msg.getTarget() != null) {
                                if (delayedMessage.getTimeout() >= elapsedRealtime) {
                                    break;
                                }
                                Log.d(PreciseAlarmManager.LOG_TAG, "expiring message " + msg.what + " from " + msg.getTarget() + " timeout=" + delayedMessage.getTimeout());
                                PreciseAlarmManager.this.mWakeLock.acquire(10000);
                                msg.sendToTarget();
                                it.remove();
                                StringBuilder sb = new StringBuilder();
                                sb.append("remaining timers ");
                                sb.append(PreciseAlarmManager.this.mTimers.size());
                                Log.d(PreciseAlarmManager.LOG_TAG, sb.toString());
                            } else {
                                Log.e(PreciseAlarmManager.LOG_TAG, "message is wrong do not handle");
                                it.remove();
                            }
                        }
                        if (PreciseAlarmManager.this.mTimers.size() != size) {
                            PreciseAlarmManager.this.registerAlarmManager();
                        }
                    }
                    try {
                        Thread.sleep(250);
                    } catch (InterruptedException e) {
                        e.printStackTrace();
                    }
                }
            }
        });
        this.mThread = thread;
        thread.start();
    }

    private void createWakelock() {
        this.mWakeLock = ((PowerManager) this.mContext.getSystemService("power")).newWakeLock(1, "ImsService");
    }

    private void wakeLockInfo(String str, Message message, long j) {
        if (message != null) {
            SimpleEventLog simpleEventLog = this.mLog;
            simpleEventLog.add(str + "(" + message.what + ") : " + j);
            return;
        }
        SimpleEventLog simpleEventLog2 = this.mLog;
        simpleEventLog2.add(str + " : " + j);
    }

    public void dump() {
        IMSLog.dump(LOG_TAG, "Dump of " + getClass().getSimpleName() + ":");
        this.mLog.dump();
    }
}
