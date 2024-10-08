package com.sec.internal.ims.entitlement.nsds.ericssonnsds.flow;

import android.content.Context;
import android.os.Looper;
import android.os.Messenger;
import android.util.Log;

public class ConfigurationRetrievalWithSIM extends OperationUsingManageConnectivity {
    private static final String LOG_TAG = ConfigurationRetrievalWithSIM.class.getSimpleName();

    public ConfigurationRetrievalWithSIM(Looper looper, Context context, BaseFlowImpl baseFlowImpl, Messenger messenger, String str, String str2, String str3) {
        super(looper, context, baseFlowImpl, messenger, str, str2, str3);
        Log.i(LOG_TAG, "created.");
    }

    public void retriveDeviceConfiguration(String str, String str2, String str3, String str4, String str5, String str6, int i, int i2) {
        this.mOperation = 3;
        this.mVIMSI = str3;
        this.mDeviceGroup = str2;
        this.mBaseFlowImpl.getNSDSClient().setDeviceParameter(str4, str5, str6, i, i2);
        this.mBaseFlowImpl.getNSDSClient().setRequestUrl(str);
        executeOperationWithChallenge();
    }
}
