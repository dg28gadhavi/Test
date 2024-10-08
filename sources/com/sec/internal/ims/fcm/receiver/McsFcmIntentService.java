package com.sec.internal.ims.fcm.receiver;

import android.app.IntentService;
import android.content.Intent;
import android.text.TextUtils;
import com.google.firebase.iid.FirebaseInstanceId;
import com.sec.ims.extensions.ContextExt;
import com.sec.internal.helper.os.IntentUtil;
import com.sec.internal.ims.registry.ImsRegistry;
import com.sec.internal.log.IMSLog;

public class McsFcmIntentService extends IntentService {
    private static final String FCM_REGISTRATION_TOKEN = "fcmRegistrationToken";
    private static final String FCM_REGISTRATION_TOKEN_REFRESHED = "fcmRegistrationTokenRefreshed";
    private static final String INTENT_RECEIVE_FCM_REGISTRATION_TOKEN = "com.sec.internal.ims.fcm.action.RECEIVE_FCM_REGISTRATION_TOKEN";
    private static final String LOG_TAG = McsFcmIntentService.class.getSimpleName();
    private static final String PHONE_ID = "phoneId";
    private static final String SENDER_ID = "senderId";
    private static final Object mLock = new Object();

    public McsFcmIntentService() {
        super(LOG_TAG);
    }

    /* access modifiers changed from: protected */
    public void onHandleIntent(Intent intent) {
        synchronized (mLock) {
            int intExtra = intent.getIntExtra("phoneId", -1);
            String stringExtra = intent.getStringExtra("senderId");
            boolean booleanExtra = intent.getBooleanExtra(FCM_REGISTRATION_TOKEN_REFRESHED, false);
            String str = LOG_TAG;
            IMSLog.s(str, "onHandleIntent: phoneId: " + intExtra + " senderId: " + stringExtra + " isFcmRegistrationTokenRefreshed: " + booleanExtra);
            try {
                if (!TextUtils.isEmpty(stringExtra)) {
                    String token = FirebaseInstanceId.getInstance().getToken(stringExtra, "FCM");
                    StringBuilder sb = new StringBuilder();
                    sb.append("onHandleIntent: fcmRegistrationToken: ");
                    sb.append((token == null || token.length() <= 0) ? "null" : IMSLog.checker(token));
                    IMSLog.i(str, sb.toString());
                    sendFcmRegistrationToken(intExtra, stringExtra, token, booleanExtra);
                }
            } catch (Exception e) {
                String str2 = LOG_TAG;
                IMSLog.i(str2, "onHandleIntent: fail to get fcmRegistrationToken: " + e.getMessage());
                sendFcmRegistrationToken(intExtra, stringExtra, (String) null, booleanExtra);
            }
        }
    }

    private void sendFcmRegistrationToken(int i, String str, String str2, boolean z) {
        Intent intent = new Intent(INTENT_RECEIVE_FCM_REGISTRATION_TOKEN);
        intent.putExtra("phoneId", i);
        intent.putExtra("senderId", str);
        intent.putExtra(FCM_REGISTRATION_TOKEN, str2);
        intent.putExtra(FCM_REGISTRATION_TOKEN_REFRESHED, z);
        intent.setPackage(ImsRegistry.getContext().getPackageName());
        IMSLog.i(LOG_TAG, "sendFcmRegistrationToken: sendBroadcast INTENT_RECEIVE_FCM_REGISTRATION_TOKEN");
        IntentUtil.sendBroadcast(this, intent, ContextExt.CURRENT_OR_SELF);
    }
}
