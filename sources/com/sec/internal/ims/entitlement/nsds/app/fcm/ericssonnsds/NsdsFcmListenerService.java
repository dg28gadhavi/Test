package com.sec.internal.ims.entitlement.nsds.app.fcm.ericssonnsds;

import android.content.Context;
import android.content.Intent;
import android.database.Cursor;
import com.sec.internal.constants.ims.entitilement.FcmNamespaces;
import com.sec.internal.constants.ims.entitilement.NSDSContractExt;
import com.sec.internal.constants.ims.entitilement.NSDSNamespaces;
import com.sec.internal.constants.ims.entitilement.data.FcmTokenDetail;
import com.sec.internal.ims.entitlement.nsds.app.fcm.data.ericssonnsds.FcmMessage;
import com.sec.internal.ims.fcm.interfaces.IFcmEventListener;
import com.sec.internal.log.IMSLog;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

public class NsdsFcmListenerService implements IFcmEventListener {
    private static final String LOG_TAG = "NsdsFcmListenerService";
    private static NsdsFcmListenerService sInstance;
    private final Context mContext;

    public NsdsFcmListenerService(Context context) {
        this.mContext = context;
    }

    public void onMessageReceived(Context context, String str, Map map) {
        String str2 = LOG_TAG;
        IMSLog.s(str2, "onMessageReceived: From: " + str + "data: " + map.toString());
        IFcmMessageParser pnsParser = getPnsParser(map);
        if (pnsParser != null) {
            FcmMessage parseMessage = pnsParser.parseMessage(map);
            if (parseMessage != null && parseMessage.shouldBroadcast(context)) {
                parseMessage.broadcastFcmMessage(context);
                return;
            }
            return;
        }
        IMSLog.e(str2, "onMessageReceived: parsing failed.");
    }

    private IFcmMessageParser getPnsParser(Map map) {
        if (map == null) {
            IMSLog.e(LOG_TAG, "getPnsParser: data null, vail");
            return null;
        } else if (map.get(FcmNamespaces.PUSH_MESSAGE) != null) {
            IMSLog.s(LOG_TAG, "getPnsParser: PushMessageParser");
            return new PushMessageParser();
        } else if (map.get("message") == null) {
            return null;
        } else {
            IMSLog.s(LOG_TAG, "getPnsParser: EventListMessageParser");
            return new EventListMessageParser();
        }
    }

    public void onTokenRefresh(Context context) {
        IMSLog.s(LOG_TAG, "onTokenRefresh()");
        for (FcmTokenDetail next : getAllFcmTokenDetails()) {
            startTokenRefresh(next.senderId, next.protocolToServer, next.deviceUid);
        }
    }

    private void startTokenRefresh(String str, String str2, String str3) {
        Intent intent = new Intent(this.mContext, RegistrationIntentService.class);
        intent.putExtra("gcm_sender_id", str);
        intent.putExtra(NSDSNamespaces.NSDSExtras.GCM_PROTOCOL_TO_SERVER, str2);
        intent.putExtra("device_id", str3);
        this.mContext.startService(intent);
    }

    private List<FcmTokenDetail> getAllFcmTokenDetails() {
        Cursor query = this.mContext.getContentResolver().query(NSDSContractExt.GcmTokens.CONTENT_URI, new String[]{NSDSContractExt.GcmTokensColumns.SENDER_ID, NSDSContractExt.GcmTokensColumns.PROTOCOL_TO_SERVER, "device_uid"}, (String) null, (String[]) null, (String) null);
        try {
            ArrayList arrayList = new ArrayList();
            if (!(query == null || !query.moveToFirst() || query.getString(0) == null)) {
                FcmTokenDetail fcmTokenDetail = new FcmTokenDetail();
                fcmTokenDetail.senderId = query.getString(0);
                fcmTokenDetail.protocolToServer = query.getString(1);
                fcmTokenDetail.deviceUid = query.getString(2);
                arrayList.add(fcmTokenDetail);
            }
            if (query != null) {
                query.close();
            }
            return arrayList;
        } catch (Throwable th) {
            th.addSuppressed(th);
        }
        throw th;
    }

    public static synchronized NsdsFcmListenerService getInstance(Context context) {
        NsdsFcmListenerService nsdsFcmListenerService;
        synchronized (NsdsFcmListenerService.class) {
            if (sInstance == null) {
                sInstance = new NsdsFcmListenerService(context);
            }
            nsdsFcmListenerService = sInstance;
        }
        return nsdsFcmListenerService;
    }
}
