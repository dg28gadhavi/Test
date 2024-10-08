package com.sec.internal.ims.imsservice;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import com.sec.internal.ims.core.sim.SimManagerFactory;
import com.sec.internal.interfaces.ims.core.IRegistrationManager;
import java.util.Arrays;
import java.util.Set;

public class ImsIntentReceiver extends BroadcastReceiver {
    private static final String LOG_TAG = ImsIntentReceiver.class.getSimpleName();

    public void onReceive(Context context, Intent intent) {
        String action = intent.getAction();
        String str = LOG_TAG;
        Log.d(str, "ImsIntentReceiver: " + intent);
        if ("android.telephony.action.CARRIER_SIGNAL_PCO_VALUE".equals(action)) {
            Bundle extras = intent.getExtras();
            if (extras == null) {
                Log.d(str, "PcoReceiver: Invalid PCO_INFO.(Not exist Extras)");
                return;
            }
            Set keySet = extras.keySet();
            if (keySet == null || keySet.isEmpty()) {
                Log.d(str, "PcoReceiver: Invalid PCO_INFO.(Invalid keyset)");
                return;
            }
            int i = extras.getInt("android.telephony.extra.APN_TYPE");
            int i2 = extras.getInt("android.telephony.extra.SUBSCRIPTION_INDEX");
            byte[] byteArray = extras.getByteArray("android.telephony.extra.PCO_VALUE");
            Log.d(str, "ACTION_PCO_INFO, pdn: " + i + ", pcokey: " + Arrays.asList(new byte[][]{byteArray}) + ", subId:" + i2);
            int i3 = byteArray != null ? byteArray[0] - 48 : -1;
            if (i3 < 0) {
                Log.e(str, "Invalid pcoValue: " + i3);
                return;
            }
            IRegistrationManager registrationManager = ImsServiceStub.getInstance().getRegistrationManager();
            if (registrationManager != null) {
                Log.d(str, "ACTION_PCO_INFO: PCO (" + i3 + "), PDN (" + i + ")");
                registrationManager.updatePcoInfo(SimManagerFactory.getSlotId(i2), i, i3);
                return;
            }
            Log.e(str, "ACTION_PCO_INFO: RegistrationManager is null..");
        }
    }
}
