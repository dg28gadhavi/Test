package com.sec.internal.ims.entitlement.nsds.ericssonnsds.flow;

import android.content.Context;
import android.os.Looper;
import android.os.Messenger;
import android.util.Log;

public class DeviceDeactivation extends OperationUsingManageConnectivity {
    private static final String LOG_TAG = DeviceDeactivation.class.getSimpleName();

    public DeviceDeactivation(Looper looper, Context context, BaseFlowImpl baseFlowImpl, Messenger messenger, String str, String str2, String str3) {
        super(looper, context, baseFlowImpl, messenger, str, str2, str3);
        Log.i(LOG_TAG, "created.");
    }

    public void deactivateDevice() {
        this.mOperation = 2;
        executeOperationWithChallenge();
    }
}
