package com.sec.internal.ims.entitlement.util;

import android.util.Log;

public class EntFeatureDetector {
    private static final String LOG_TAG = "EntFeatureDetector";

    public static boolean checkVSimFeatureEnabled(String str, int i) {
        String configServer = NSDSConfigHelper.getConfigServer(i);
        String str2 = LOG_TAG;
        Log.i(str2, "checkVSimFeatureEnabled: " + str + " configserver:" + configServer);
        return str != null && str.equalsIgnoreCase(configServer);
    }

    public static boolean checkWFCAutoOnEnabled(int i) {
        boolean isWFCAutoOnEnabled = NSDSConfigHelper.isWFCAutoOnEnabled(i);
        String str = LOG_TAG;
        Log.i(str, "checkWFCAutoOnEnabled: " + isWFCAutoOnEnabled);
        return isWFCAutoOnEnabled;
    }
}
