package com.sec.internal.ims.servicemodules.options;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import com.sec.ims.extensions.Extensions;
import com.sec.internal.constants.ims.ImsConstants;
import com.sec.internal.helper.SimUtil;
import com.sec.internal.log.IMSLog;

public class CapabilityIntentReceiver extends BroadcastReceiver {
    protected static final String INTENT_PERIODIC_POLL_PARTIAL = "com.sec.internal.ims.servicemodules.options.poll_partial";
    protected static final String INTENT_PERIODIC_POLL_TIMEOUT = "com.sec.internal.ims.servicemodules.options.poll_timeout";
    protected static final String INTENT_THROTTLED_RETRY_TIMEOUT = "com.sec.internal.ims.servicemodules.options.sub_throttled_timeout";
    private static final String LOG_TAG = "CapabilityIntentReceiver";
    private CapabilityDiscoveryModule mCapabilityDiscovery;

    public CapabilityIntentReceiver(CapabilityDiscoveryModule capabilityDiscoveryModule) {
        this.mCapabilityDiscovery = capabilityDiscoveryModule;
    }

    public IntentFilter getIntentFilter() {
        IntentFilter intentFilter = new IntentFilter();
        intentFilter.addAction(INTENT_PERIODIC_POLL_TIMEOUT);
        intentFilter.addAction(INTENT_THROTTLED_RETRY_TIMEOUT);
        intentFilter.addAction(INTENT_PERIODIC_POLL_PARTIAL);
        intentFilter.addAction(Extensions.Intent.ACTION_USER_SWITCHED);
        intentFilter.addAction("android.intent.action.BOOT_COMPLETED");
        return intentFilter;
    }

    public void onReceive(Context context, Intent intent) {
        int intExtra = intent.getIntExtra(ImsConstants.Intents.EXTRA_PHONE_ID, SimUtil.getActiveDataPhoneId());
        IMSLog.i(LOG_TAG, intExtra, "onReceive: " + intent.getAction());
        if (INTENT_PERIODIC_POLL_TIMEOUT.equals(intent.getAction())) {
            CapabilityDiscoveryModule capabilityDiscoveryModule = this.mCapabilityDiscovery;
            capabilityDiscoveryModule.sendMessage(capabilityDiscoveryModule.obtainMessage(17, Integer.valueOf(intExtra)));
            CapabilityDiscoveryModule capabilityDiscoveryModule2 = this.mCapabilityDiscovery;
            capabilityDiscoveryModule2.sendMessage(capabilityDiscoveryModule2.obtainMessage(18, 0, 0, Integer.valueOf(intExtra)));
        } else if (INTENT_PERIODIC_POLL_PARTIAL.equals(intent.getAction())) {
            boolean booleanExtra = intent.getBooleanExtra("force", false);
            CapabilityDiscoveryModule capabilityDiscoveryModule3 = this.mCapabilityDiscovery;
            capabilityDiscoveryModule3.sendMessage(capabilityDiscoveryModule3.obtainMessage(18, booleanExtra ? 1 : 0, 0, Integer.valueOf(intExtra)));
        } else if (INTENT_THROTTLED_RETRY_TIMEOUT.equals(intent.getAction())) {
            boolean booleanExtra2 = intent.getBooleanExtra("IS_PERIODIC", false);
            IMSLog.i(LOG_TAG, intExtra, "onReceive: subscription throttled timeout. isPeriodic = " + booleanExtra2);
            if (booleanExtra2) {
                CapabilityDiscoveryModule capabilityDiscoveryModule4 = this.mCapabilityDiscovery;
                capabilityDiscoveryModule4.sendMessage(capabilityDiscoveryModule4.obtainMessage(18, 0, 0, Integer.valueOf(intExtra)));
                return;
            }
            CapabilityDiscoveryModule capabilityDiscoveryModule5 = this.mCapabilityDiscovery;
            capabilityDiscoveryModule5.sendMessage(capabilityDiscoveryModule5.obtainMessage(1, Integer.valueOf(intExtra)));
        } else if (Extensions.Intent.ACTION_USER_SWITCHED.equals(intent.getAction())) {
            CapabilityDiscoveryModule capabilityDiscoveryModule6 = this.mCapabilityDiscovery;
            capabilityDiscoveryModule6.sendMessage(capabilityDiscoveryModule6.obtainMessage(11));
        } else if ("android.intent.action.BOOT_COMPLETED".equals(intent.getAction())) {
            CapabilityDiscoveryModule capabilityDiscoveryModule7 = this.mCapabilityDiscovery;
            capabilityDiscoveryModule7.sendMessage(capabilityDiscoveryModule7.obtainMessage(12));
        }
    }
}
