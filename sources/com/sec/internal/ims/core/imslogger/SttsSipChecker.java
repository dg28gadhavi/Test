package com.sec.internal.ims.core.imslogger;

import android.content.Context;

public class SttsSipChecker extends ImsDiagnosticMonitorNotifier {
    public SttsSipChecker(Context context, String str, String str2) {
        super(context, str, str2, false);
        this.LOG_TAG = SttsSipChecker.class.getSimpleName();
    }
}
