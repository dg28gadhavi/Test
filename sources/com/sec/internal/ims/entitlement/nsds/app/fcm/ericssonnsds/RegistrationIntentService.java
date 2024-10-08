package com.sec.internal.ims.entitlement.nsds.app.fcm.ericssonnsds;

import android.app.IntentService;
import android.content.Context;
import android.text.TextUtils;
import android.util.Base64;
import com.sec.internal.constants.ims.entitilement.NSDSNamespaces;
import com.sec.internal.ims.entitlement.storagehelper.NSDSSharedPrefHelper;
import com.sec.internal.log.IMSLog;

public class RegistrationIntentService extends IntentService {
    private static final String TAG = RegistrationIntentService.class.getSimpleName();
    private Object mLock = new Object();

    public RegistrationIntentService() {
        super(TAG);
    }

    /* access modifiers changed from: protected */
    /* JADX WARNING: Code restructure failed: missing block: B:24:?, code lost:
        return;
     */
    /* Code decompiled incorrectly, please refer to instructions dump. */
    public void onHandleIntent(android.content.Intent r8) {
        /*
            r7 = this;
            java.lang.String r0 = "gcm_sender_id"
            java.lang.String r0 = r8.getStringExtra(r0)
            java.lang.String r1 = "gcm_protocol_to_server"
            java.lang.String r1 = r8.getStringExtra(r1)
            java.lang.String r2 = "device_id"
            java.lang.String r8 = r8.getStringExtra(r2)
            java.lang.Object r2 = r7.mLock     // Catch:{ Exception -> 0x007e }
            monitor-enter(r2)     // Catch:{ Exception -> 0x007e }
            com.google.firebase.iid.FirebaseInstanceId r3 = com.google.firebase.iid.FirebaseInstanceId.getInstance()     // Catch:{ all -> 0x007b }
            boolean r4 = android.text.TextUtils.isEmpty(r0)     // Catch:{ all -> 0x007b }
            if (r4 == 0) goto L_0x0028
            java.lang.String r0 = TAG     // Catch:{ all -> 0x007b }
            java.lang.String r3 = "FCM_Sender_ID is not ready yet. Will get token once its ready"
            com.sec.internal.log.IMSLog.i(r0, r3)     // Catch:{ all -> 0x007b }
            monitor-exit(r2)     // Catch:{ all -> 0x007b }
            return
        L_0x0028:
            java.lang.String r4 = TAG     // Catch:{ all -> 0x007b }
            java.lang.StringBuilder r5 = new java.lang.StringBuilder     // Catch:{ all -> 0x007b }
            r5.<init>()     // Catch:{ all -> 0x007b }
            java.lang.String r6 = "FCMSenderID: "
            r5.append(r6)     // Catch:{ all -> 0x007b }
            r5.append(r0)     // Catch:{ all -> 0x007b }
            java.lang.String r5 = r5.toString()     // Catch:{ all -> 0x007b }
            com.sec.internal.log.IMSLog.s(r4, r5)     // Catch:{ all -> 0x007b }
            java.lang.String r5 = "FCM"
            java.lang.String r3 = r3.getToken(r0, r5)     // Catch:{ all -> 0x007b }
            java.lang.StringBuilder r5 = new java.lang.StringBuilder     // Catch:{ all -> 0x007b }
            r5.<init>()     // Catch:{ all -> 0x007b }
            java.lang.String r6 = "FCM Registration Token: "
            r5.append(r6)     // Catch:{ all -> 0x007b }
            r5.append(r3)     // Catch:{ all -> 0x007b }
            java.lang.String r6 = " for FCMsenderId:"
            r5.append(r6)     // Catch:{ all -> 0x007b }
            r5.append(r0)     // Catch:{ all -> 0x007b }
            java.lang.String r0 = r5.toString()     // Catch:{ all -> 0x007b }
            com.sec.internal.log.IMSLog.s(r4, r0)     // Catch:{ all -> 0x007b }
            java.lang.String r0 = "TKN:GET_TKN_SUCCESS"
            r4 = 336068608(0x14080000, float:6.866245E-27)
            com.sec.internal.log.IMSLog.c(r4, r0)     // Catch:{ all -> 0x007b }
            java.lang.String r0 = "managePushToken"
            boolean r0 = r0.equals(r1)     // Catch:{ all -> 0x007b }
            if (r0 == 0) goto L_0x0079
            r7.sendRegistrationToServer(r3, r8)     // Catch:{ all -> 0x007b }
            java.lang.String r0 = "sent_token_to_server"
            r3 = 1
            com.sec.internal.ims.entitlement.storagehelper.NSDSSharedPrefHelper.save((android.content.Context) r7, (java.lang.String) r8, (java.lang.String) r0, (boolean) r3)     // Catch:{ all -> 0x007b }
        L_0x0079:
            monitor-exit(r2)     // Catch:{ all -> 0x007b }
            goto L_0x00ae
        L_0x007b:
            r0 = move-exception
            monitor-exit(r2)     // Catch:{ all -> 0x007b }
            throw r0     // Catch:{ Exception -> 0x007e }
        L_0x007e:
            r0 = move-exception
            java.lang.String r2 = TAG
            java.lang.StringBuilder r3 = new java.lang.StringBuilder
            r3.<init>()
            java.lang.String r4 = "Failed to complete token refresh"
            r3.append(r4)
            java.lang.String r0 = r0.getMessage()
            r3.append(r0)
            java.lang.String r0 = r3.toString()
            com.sec.internal.log.IMSLog.i(r2, r0)
            java.lang.String r0 = "managePushToken"
            boolean r0 = r0.equals(r1)
            if (r0 == 0) goto L_0x00ae
            java.lang.String r0 = "sent_token_to_server"
            com.sec.internal.ims.entitlement.storagehelper.NSDSSharedPrefHelper.remove(r7, r8, r0)
            java.lang.String r0 = "sent_token_to_server"
            r1 = 0
            com.sec.internal.ims.entitlement.storagehelper.NSDSSharedPrefHelper.save((android.content.Context) r7, (java.lang.String) r8, (java.lang.String) r0, (boolean) r1)
        L_0x00ae:
            return
        */
        throw new UnsupportedOperationException("Method not decompiled: com.sec.internal.ims.entitlement.nsds.app.fcm.ericssonnsds.RegistrationIntentService.onHandleIntent(android.content.Intent):void");
    }

    private void sendRegistrationToServer(String str, String str2) {
        String str3 = TAG;
        IMSLog.s(str3, "Received token from FCM:" + str);
        String encodeToString = Base64.encodeToString(str.getBytes(), 2);
        String str4 = NSDSSharedPrefHelper.get(this, str2, NSDSNamespaces.NSDSSharedPref.PREF_PUSH_TOKEN);
        if (TextUtils.isEmpty(str4)) {
            str4 = "";
        }
        if (!str4.equals(encodeToString)) {
            NSDSSharedPrefHelper.save((Context) this, str2, NSDSNamespaces.NSDSSharedPref.PREF_PUSH_TOKEN, encodeToString);
        }
    }
}
