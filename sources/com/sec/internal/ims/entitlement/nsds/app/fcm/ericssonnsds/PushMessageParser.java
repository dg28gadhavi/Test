package com.sec.internal.ims.entitlement.nsds.app.fcm.ericssonnsds;

import android.text.TextUtils;
import com.google.gson.Gson;
import com.google.gson.JsonSyntaxException;
import com.sec.internal.constants.ims.entitilement.FcmNamespaces;
import com.sec.internal.ims.entitlement.nsds.app.fcm.data.ericssonnsds.FcmMessage;
import com.sec.internal.ims.entitlement.nsds.app.fcm.data.ericssonnsds.PushMessage;
import com.sec.internal.log.IMSLog;
import java.util.Map;

public class PushMessageParser implements IFcmMessageParser {
    private static final String LOG_TAG = "PushMessageParser";

    public FcmMessage parseMessage(Map map) {
        Gson gson = new Gson();
        PushMessage pushMessage = null;
        String obj = map.get(FcmNamespaces.PUSH_MESSAGE) != null ? map.get(FcmNamespaces.PUSH_MESSAGE).toString() : null;
        String obj2 = map.get("confirmation_url") != null ? map.get("confirmation_url").toString() : null;
        try {
            if (TextUtils.isEmpty(obj)) {
                return null;
            }
            PushMessage pushMessage2 = (PushMessage) gson.fromJson(obj, PushMessage.class);
            try {
                pushMessage2.setOrigMessage(obj);
                pushMessage2.setConfirmUrl(obj2);
                String str = LOG_TAG;
                IMSLog.i(str, "parseMessage: message type-" + pushMessage2.pnsType + " subtype-" + pushMessage2.pnsSubtype);
                return pushMessage2;
            } catch (JsonSyntaxException e) {
                e = e;
                pushMessage = pushMessage2;
                String str2 = LOG_TAG;
                IMSLog.e(str2, "cannot parse received message" + e.getMessage());
                return pushMessage;
            }
        } catch (JsonSyntaxException e2) {
            e = e2;
            String str22 = LOG_TAG;
            IMSLog.e(str22, "cannot parse received message" + e.getMessage());
            return pushMessage;
        }
    }
}
