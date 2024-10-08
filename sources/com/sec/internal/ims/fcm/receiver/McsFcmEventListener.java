package com.sec.internal.ims.fcm.receiver;

import android.content.Context;
import android.content.Intent;
import com.sec.ims.extensions.ContextExt;
import com.sec.internal.helper.os.IntentUtil;
import com.sec.internal.ims.fcm.interfaces.IFcmEventListener;
import com.sec.internal.log.IMSLog;
import java.util.Map;

public class McsFcmEventListener implements IFcmEventListener {
    private static final String FROM_FIELD = "from";
    private static final String INTENT_RECEIVE_FCM_PUSH_NOTIFICATION = "com.sec.internal.ims.fcm.action.RECEIVE_FCM_PUSH_NOTIFICATION";
    private static final String INTENT_REFRESH_FCM_REGISTRATION_TOKEN = "com.sec.internal.ims.fcm.action.REFRESH_FCM_REGISTRATION_TOKEN";
    private static final String LOG_TAG = "McsFcmEventListener";
    private static final String MESSAGE_FIELD = "message";
    private static McsFcmEventListener sInstance;

    public void onMessageReceived(Context context, String str, Map map) {
        if (map == null || map.get("message") == null) {
            IMSLog.e(LOG_TAG, "onMessageReceived: message is empty");
            return;
        }
        String str2 = LOG_TAG;
        IMSLog.s(str2, "onMessageReceived: from: " + str + " data: " + map.toString());
        IMSLog.i(str2, "onMessageReceived: sendBroadcast INTENT_RECEIVE_FCM_PUSH_NOTIFICATION");
        Intent intent = new Intent(INTENT_RECEIVE_FCM_PUSH_NOTIFICATION);
        intent.putExtra("from", str);
        intent.putExtra("message", map.get("message").toString());
        IntentUtil.sendBroadcast(context, intent, ContextExt.CURRENT_OR_SELF);
    }

    public void onTokenRefresh(Context context) {
        IMSLog.i(LOG_TAG, "onTokenRefresh: sendBroadcast INTENT_REFRESH_FCM_REGISTRATION_TOKEN");
        IntentUtil.sendBroadcast(context, new Intent(INTENT_REFRESH_FCM_REGISTRATION_TOKEN), ContextExt.CURRENT_OR_SELF);
    }

    public static synchronized McsFcmEventListener getInstance() {
        McsFcmEventListener mcsFcmEventListener;
        synchronized (McsFcmEventListener.class) {
            if (sInstance == null) {
                IMSLog.e(LOG_TAG, "getInstance: create McsFcmEventListener");
                sInstance = new McsFcmEventListener();
            }
            mcsFcmEventListener = sInstance;
        }
        return mcsFcmEventListener;
    }
}
