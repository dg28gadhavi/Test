package com.sec.internal.ims.entitlement.config.app.nsdsconfig.strategy.operation;

import android.util.Log;

public class DefaultNsdsOperation {
    private static final String LOG_TAG = "DefaultNsdsOperation";

    public static int getOperation(int i, int i2) {
        String str = LOG_TAG;
        Log.i(str, "getOperation: eventType-" + i + " prevOp-" + i2);
        if (i2 != -1) {
            return -1;
        }
        if (i != 14) {
            return i != 15 ? -1 : 11;
        }
        return 10;
    }
}
