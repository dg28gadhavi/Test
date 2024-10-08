package com.sec.internal.ims.entitlement.nsds.ericssonnsds.flow;

import android.content.Context;
import android.os.Looper;
import android.os.Messenger;
import android.util.Log;

public class ConfigurationUpdate extends ConfigurationRetrievalWithSIM {
    private static final String LOG_TAG = ConfigurationUpdate.class.getSimpleName();

    public ConfigurationUpdate(Looper looper, Context context, BaseFlowImpl baseFlowImpl, Messenger messenger, String str, String str2, String str3) {
        super(looper, context, baseFlowImpl, messenger, str, str2, str3);
        Log.d(LOG_TAG, "created.");
    }

    public void updateDeviceConfiguration(String str, String str2, String str3, String str4, String str5, int i, int i2) {
        this.mOperation = 1;
        this.mVIMSI = str2;
        this.mBaseFlowImpl.getNSDSClient().setDeviceParameter(str3, str4, str5, i, i2);
        this.mDeviceGroup = str;
        executeOperationWithChallenge();
    }
}
