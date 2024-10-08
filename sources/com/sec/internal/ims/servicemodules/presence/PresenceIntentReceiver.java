package com.sec.internal.ims.servicemodules.presence;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.util.Log;
import com.sec.internal.helper.SimUtil;

public class PresenceIntentReceiver {
    static final String INTENT_ACTION_TRIGGER_OMADM_TREE_SYNC = "com.samsung.sdm.START_DM_SYNC_SESSION";
    static final String INTENT_BAD_EVENT_TIMEOUT = "com.sec.internal.ims.servicemodules.presence.bad_event_timeout";
    static final String INTENT_EXTRA_KEY_PHONE_ID = "KEY_PHONE_ID";
    static final String INTENT_EXTRA_KEY_SUBSCRIPTION_ID = "KEY_SUBSCRIPTION_ID";
    static final String INTENT_PERIODIC_PUBLISH_TIMEOUT = "com.sec.internal.ims.servicemodules.presence.publish_timeout";
    static final String INTENT_RETRY_PUBLISH = "com.sec.internal.ims.servicemodules.presence.retry_publish";
    static final String INTENT_RETRY_SUBSCRIBE = "com.sec.internal.ims.servicemodules.presence.retry_subscribe";
    private static final String LOG_TAG = "PresenceIntentReceiver";
    static BroadcastReceiver mIntentReceiver = new BroadcastReceiver() {
        public void onReceive(Context context, Intent intent) {
            Log.i(PresenceIntentReceiver.LOG_TAG, "onReceive: " + intent.getAction());
            int intExtra = intent.getIntExtra("sim_slot_id", SimUtil.getActiveDataPhoneId());
            if (PresenceIntentReceiver.INTENT_PERIODIC_PUBLISH_TIMEOUT.equals(intent.getAction())) {
                PresenceIntentReceiver.mPresence.sendMessage(PresenceIntentReceiver.mPresence.obtainMessage(4, Integer.valueOf(intExtra)));
            } else if (PresenceIntentReceiver.INTENT_BAD_EVENT_TIMEOUT.equals(intent.getAction())) {
                PresenceIntentReceiver.mPresence.sendMessage(PresenceIntentReceiver.mPresence.obtainMessage(14, Integer.valueOf(intExtra)));
            } else if (PresenceIntentReceiver.INTENT_RETRY_PUBLISH.equals(intent.getAction())) {
                PresenceIntentReceiver.mPresence.sendMessage(PresenceIntentReceiver.mPresence.obtainMessage(18, Integer.valueOf(intExtra)));
            }
        }
    };
    /* access modifiers changed from: private */
    public static PresenceModule mPresence;
    static BroadcastReceiver mSubscribeRetryIntentReceiver = new BroadcastReceiver() {
        public void onReceive(Context context, Intent intent) {
            Log.i(PresenceIntentReceiver.LOG_TAG, "onReceive: " + intent.getAction());
            if (PresenceIntentReceiver.INTENT_RETRY_SUBSCRIBE.equals(intent.getAction())) {
                String stringExtra = intent.getStringExtra(PresenceIntentReceiver.INTENT_EXTRA_KEY_SUBSCRIPTION_ID);
                int intExtra = intent.getIntExtra(PresenceIntentReceiver.INTENT_EXTRA_KEY_PHONE_ID, 0);
                if (stringExtra != null) {
                    PresenceIntentReceiver.mPresence.sendMessage(PresenceIntentReceiver.mPresence.obtainMessage(8, PresenceSubscriptionController.getSubscription(stringExtra, intExtra)));
                }
            }
        }
    };

    PresenceIntentReceiver(PresenceModule presenceModule) {
        mPresence = presenceModule;
    }

    /* access modifiers changed from: package-private */
    public IntentFilter getIntentFilter() {
        IntentFilter intentFilter = new IntentFilter();
        intentFilter.addAction(INTENT_PERIODIC_PUBLISH_TIMEOUT);
        intentFilter.addAction(INTENT_BAD_EVENT_TIMEOUT);
        intentFilter.addAction(INTENT_RETRY_PUBLISH);
        return intentFilter;
    }

    /* access modifiers changed from: package-private */
    public IntentFilter getSubscribeRetryIntentFilter() {
        IntentFilter intentFilter = new IntentFilter();
        intentFilter.addAction(INTENT_RETRY_SUBSCRIBE);
        intentFilter.addDataScheme("urn");
        intentFilter.addDataSchemeSpecificPart("subscriptionid", 1);
        return intentFilter;
    }
}
