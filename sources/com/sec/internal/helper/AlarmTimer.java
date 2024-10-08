package com.sec.internal.helper;

import android.app.AlarmManager;
import android.app.PendingIntent;
import android.content.Context;
import android.os.SystemClock;
import android.util.Log;

public class AlarmTimer {
    private static final String LOG_TAG = "AlarmTimer";

    public static void start(Context context, PendingIntent pendingIntent, long j) {
        start(context, pendingIntent, j, true);
    }

    public static void start(Context context, PendingIntent pendingIntent, long j, boolean z) {
        Log.d(LOG_TAG, "start: " + pendingIntent + " millis " + j);
        ((AlarmManager) context.getSystemService("alarm")).setExact(z ? 2 : 3, SystemClock.elapsedRealtime() + j, pendingIntent);
    }

    public static void stop(Context context, PendingIntent pendingIntent) {
        Log.d(LOG_TAG, "stop: " + pendingIntent);
        ((AlarmManager) context.getSystemService("alarm")).cancel(pendingIntent);
    }
}
