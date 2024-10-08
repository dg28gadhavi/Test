package com.sec.internal.ims.config;

import android.app.IntentService;
import android.content.Intent;
import android.util.Log;
import com.sec.internal.ims.registry.ImsRegistry;
import com.sec.internal.interfaces.ims.config.IConfigModule;

public class UserMessageIntentService extends IntentService {
    private static final String LOG_TAG = UserMessageIntentService.class.getSimpleName();
    public static final String SHOW_MSISDN_DIALOG = "com.sec.rcs.config.action.SHOW_MSISDN_DIALOG";

    public UserMessageIntentService() {
        super(LOG_TAG);
    }

    /* access modifiers changed from: protected */
    public void onHandleIntent(Intent intent) {
        IConfigModule configModule;
        if (intent != null) {
            String str = LOG_TAG;
            Log.i(str, "onHandleIntent: " + intent.getAction());
            String action = intent.getAction();
            action.hashCode();
            if (action.equals(SHOW_MSISDN_DIALOG) && (configModule = ImsRegistry.getConfigModule()) != null) {
                configModule.showMSIDSNDialog();
                return;
            }
            return;
        }
        Log.i(LOG_TAG, "onHandleIntent: intent is null");
    }
}
