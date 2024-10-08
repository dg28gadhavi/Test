package com.sec.internal.ims.xq.att;

import android.content.Context;
import android.os.Handler;
import android.os.Message;
import android.util.Log;
import com.sec.sve.generalevent.VcidEvent;

public class ImsXqReporter extends Handler {
    private static final String LOG_TAG = "ImsXqReporterDummy";
    private final Context mContext;
    private int mPhoneId;

    public void handleMessage(Message message) {
    }

    public ImsXqReporter(Context context, int i) {
        this.mContext = context;
        this.mPhoneId = i;
    }

    public static boolean isXqEnabled(Context context, int i) {
        Log.i(LOG_TAG, "Default Case, Check SEC_PRODUCT_FEATURE_COMMON_SUPPORT_IQI");
        return false;
    }

    public void start() {
        Log.d(LOG_TAG, "Not start");
    }

    public void stop() {
        Log.d(LOG_TAG, VcidEvent.BUNDLE_VALUE_ACTION_STOP);
    }
}
