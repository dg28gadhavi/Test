package com.sec.internal.ims.aec.receiver.fcm;

import android.content.Context;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import com.sec.internal.ims.core.cmc.CmcConstants;
import com.sec.internal.ims.fcm.interfaces.IFcmEventListener;
import com.sec.internal.log.AECLog;
import java.util.HashMap;
import java.util.Map;
import org.json.JSONException;
import org.json.JSONObject;

public class FcmNotification implements IFcmEventListener {
    private static final String FCM_APP = "app";
    private static final String FCM_DATA = "data";
    private static final String FCM_FROM = "from";
    private static final String FCM_TIMESTAMP = "timestamp";
    private static final String LOG_TAG = "FcmNotification";
    private static final String[] filterStr = {CmcConstants.E_NUM_STR_QUOTE, "\\[", "\\]", "app="};
    private final int mPhoneId;
    private final Handler mWorkflowHandler;

    public FcmNotification(int i, Handler handler) {
        this.mPhoneId = i;
        this.mWorkflowHandler = handler;
    }

    public void onMessageReceived(Context context, String str, Map map) {
        String str2 = LOG_TAG;
        AECLog.s(str2, "onMessageReceived: " + map.toString() + " from " + str, this.mPhoneId);
        sendFcmNotification(getFcmNotification(str, map));
    }

    public void onTokenRefresh(Context context) {
        AECLog.i(LOG_TAG, "onTokenRefresh", this.mPhoneId);
        Message obtainMessage = this.mWorkflowHandler.obtainMessage();
        obtainMessage.what = 1015;
        obtainMessage.arg1 = this.mPhoneId;
        this.mWorkflowHandler.sendMessage(obtainMessage);
    }

    private void sendFcmNotification(Map<String, String> map) {
        Bundle bundle = new Bundle();
        bundle.putString("from", map.get("from"));
        bundle.putString("app", map.get("app"));
        bundle.putString("timestamp", map.get("timestamp"));
        Message obtainMessage = this.mWorkflowHandler.obtainMessage();
        obtainMessage.what = 1014;
        obtainMessage.arg1 = this.mPhoneId;
        obtainMessage.obj = bundle;
        this.mWorkflowHandler.sendMessage(obtainMessage);
    }

    /* access modifiers changed from: package-private */
    public Map<String, String> getFcmNotification(String str, Map map) {
        HashMap hashMap = new HashMap();
        try {
            hashMap.put("from", str);
            if (map.get(FCM_DATA) != null) {
                JSONObject jSONObject = new JSONObject(String.valueOf(map.get(FCM_DATA)));
                hashMap.put("app", filterStr(jSONObject.optString("app", "")));
                hashMap.put("timestamp", jSONObject.optString("timestamp", ""));
            } else if (!(map.get("app") == null || map.get("timestamp") == null)) {
                hashMap.put("app", filterStr(String.valueOf(map.get("app"))));
                hashMap.put("timestamp", String.valueOf(map.get("timestamp")));
            }
        } catch (JSONException e) {
            String str2 = LOG_TAG;
            AECLog.e(str2, "getFcmNotification: " + e.getMessage(), this.mPhoneId);
        }
        return hashMap;
    }

    /* access modifiers changed from: package-private */
    public String filterStr(String str) {
        String trim = str.replaceAll("&", ",").trim();
        for (String replaceAll : filterStr) {
            trim = trim.replaceAll(replaceAll, "");
        }
        return trim;
    }
}
