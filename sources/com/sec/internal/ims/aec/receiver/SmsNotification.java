package com.sec.internal.ims.aec.receiver;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Handler;
import com.sec.internal.constants.ims.aec.AECNamespace;

public class SmsNotification extends BroadcastReceiver {
    private static final String DATA_AUTHORITY = "localhost";
    private static final String DATA_SCHEME = "sms";
    private static final String DEST_PORT = "8095";
    private static final String LOG_TAG = SmsNotification.class.getSimpleName();
    private static final String TS43_SMS_PUSH_MESSAGE = "aescfg";
    private final Context mContext;
    private final Handler mModuleHandler;

    public SmsNotification(Context context, Handler handler) {
        this.mContext = context;
        this.mModuleHandler = handler;
    }

    public IntentFilter getIntentFilter() {
        IntentFilter intentFilter = new IntentFilter();
        intentFilter.addAction(AECNamespace.Action.RECEIVED_SMS_NOTIFICATION);
        intentFilter.addDataScheme(DATA_SCHEME);
        intentFilter.addDataAuthority(DATA_AUTHORITY, DEST_PORT);
        return intentFilter;
    }

    public void onReceive(Context context, Intent intent) {
        sendSmsNotification(intent);
    }

    /* JADX WARNING: Removed duplicated region for block: B:20:0x0063 A[Catch:{ SecurityException -> 0x0081 }] */
    /* JADX WARNING: Removed duplicated region for block: B:26:? A[RETURN, SYNTHETIC] */
    /* Code decompiled incorrectly, please refer to instructions dump. */
    private void sendSmsNotification(android.content.Intent r9) {
        /*
            r8 = this;
            java.lang.String r0 = "sendSmsNotification: "
            android.telephony.SmsMessage[] r1 = android.provider.Telephony.Sms.Intents.getMessagesFromIntent(r9)     // Catch:{ SecurityException -> 0x0081 }
            if (r1 == 0) goto L_0x009a
            r2 = 0
            r1 = r1[r2]     // Catch:{ SecurityException -> 0x0081 }
            if (r1 == 0) goto L_0x009a
            java.lang.String r3 = r1.getDisplayMessageBody()     // Catch:{ SecurityException -> 0x0081 }
            java.lang.String r4 = "subscription"
            r5 = -1
            int r9 = r9.getIntExtra(r4, r5)     // Catch:{ SecurityException -> 0x0081 }
            int r9 = com.sec.internal.helper.SimUtil.getSlotId(r9)     // Catch:{ SecurityException -> 0x0081 }
            java.lang.String r4 = LOG_TAG     // Catch:{ SecurityException -> 0x0081 }
            java.lang.StringBuilder r5 = new java.lang.StringBuilder     // Catch:{ SecurityException -> 0x0081 }
            r5.<init>()     // Catch:{ SecurityException -> 0x0081 }
            r5.append(r0)     // Catch:{ SecurityException -> 0x0081 }
            r5.append(r3)     // Catch:{ SecurityException -> 0x0081 }
            java.lang.String r5 = r5.toString()     // Catch:{ SecurityException -> 0x0081 }
            com.sec.internal.log.AECLog.i(r4, r5, r9)     // Catch:{ SecurityException -> 0x0081 }
            boolean r5 = android.text.TextUtils.isEmpty(r3)     // Catch:{ SecurityException -> 0x0081 }
            java.lang.String r6 = "aescfg"
            r7 = 1
            if (r5 == 0) goto L_0x0053
            java.lang.String r3 = new java.lang.String     // Catch:{ SecurityException -> 0x0081 }
            byte[] r1 = r1.getUserData()     // Catch:{ SecurityException -> 0x0081 }
            java.nio.charset.Charset r4 = java.nio.charset.StandardCharsets.UTF_16     // Catch:{ SecurityException -> 0x0081 }
            r3.<init>(r1, r4)     // Catch:{ SecurityException -> 0x0081 }
            boolean r1 = android.text.TextUtils.isEmpty(r3)     // Catch:{ SecurityException -> 0x0081 }
            if (r1 != 0) goto L_0x0061
            boolean r1 = r3.contains(r6)     // Catch:{ SecurityException -> 0x0081 }
            if (r1 == 0) goto L_0x0061
            goto L_0x0059
        L_0x0053:
            boolean r1 = r3.contains(r6)     // Catch:{ SecurityException -> 0x0081 }
            if (r1 == 0) goto L_0x005b
        L_0x0059:
            r2 = r7
            goto L_0x0061
        L_0x005b:
            java.lang.String r1 = "sendSmsNotification: discard invalid notification"
            com.sec.internal.log.AECLog.i(r4, r1, r9)     // Catch:{ SecurityException -> 0x0081 }
        L_0x0061:
            if (r2 == 0) goto L_0x009a
            android.os.Handler r1 = r8.mModuleHandler     // Catch:{ SecurityException -> 0x0081 }
            android.os.Message r1 = r1.obtainMessage()     // Catch:{ SecurityException -> 0x0081 }
            r2 = 7
            r1.what = r2     // Catch:{ SecurityException -> 0x0081 }
            r1.arg1 = r9     // Catch:{ SecurityException -> 0x0081 }
            java.lang.String r9 = ","
            int r9 = r3.indexOf(r9)     // Catch:{ SecurityException -> 0x0081 }
            int r9 = r9 + r7
            java.lang.String r9 = r3.substring(r9)     // Catch:{ SecurityException -> 0x0081 }
            r1.obj = r9     // Catch:{ SecurityException -> 0x0081 }
            android.os.Handler r8 = r8.mModuleHandler     // Catch:{ SecurityException -> 0x0081 }
            r8.sendMessage(r1)     // Catch:{ SecurityException -> 0x0081 }
            goto L_0x009a
        L_0x0081:
            r8 = move-exception
            java.lang.String r9 = LOG_TAG
            java.lang.StringBuilder r1 = new java.lang.StringBuilder
            r1.<init>()
            r1.append(r0)
            java.lang.String r8 = r8.toString()
            r1.append(r8)
            java.lang.String r8 = r1.toString()
            com.sec.internal.log.AECLog.e(r9, r8)
        L_0x009a:
            return
        */
        throw new UnsupportedOperationException("Method not decompiled: com.sec.internal.ims.aec.receiver.SmsNotification.sendSmsNotification(android.content.Intent):void");
    }
}
