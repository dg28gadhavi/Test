package com.sec.internal.ims.core.imslogger;

import android.content.Context;

public class ImsLoggerPlus extends ImsDiagnosticMonitorNotifier {
    public ImsLoggerPlus(Context context, String str, String str2) {
        super(context, str, str2, true);
        this.LOG_TAG = ImsLoggerPlus.class.getSimpleName();
    }
}
