package com.sec.internal.ims.entitlement.config.app.nsdsconfig.strategy.operation;

import android.util.Log;

public class TmoNsdsOperation {
    private static final String LOG_TAG = "TmoNsdsOperation";

    public static int getOperation(int i, int i2) {
        String str = LOG_TAG;
        Log.i(str, "getOperation: eventType-" + i + " prevOp-" + i2);
        if (i2 != -1) {
            return -1;
        }
        if (i == 14) {
            return 10;
        }
        if (i != 15) {
            return i != 18 ? -1 : 14;
        }
        return 11;
    }
}
