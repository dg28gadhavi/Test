package com.sec.internal.ims.servicemodules.im;

import android.content.BroadcastReceiver;
import com.sec.internal.helper.SimUtil;

public class MmsReceiver extends BroadcastReceiver {
    public static final String MMS_MIME_TYPE = "application/vnd.wap.mms-message";
    public static final String MMS_RECEIVED = "android.provider.Telephony.WAP_PUSH_RECEIVED";
    private String TAG = MmsReceiver.class.getSimpleName();
    private ImLatchingProcessor mModule;
    private int mPhoneId;

    public MmsReceiver(ImLatchingProcessor imLatchingProcessor) {
        this.mModule = imLatchingProcessor;
        this.mPhoneId = SimUtil.getActiveDataPhoneId();
    }

    /* JADX WARNING: Code restructure failed: missing block: B:10:0x003f, code lost:
        r0 = r0.substring(r1, r7);
     */
    /* Code decompiled incorrectly, please refer to instructions dump. */
    public void onReceive(android.content.Context r7, android.content.Intent r8) {
        /*
            r6 = this;
            java.lang.String r7 = r8.getAction()
            java.lang.String r0 = "android.provider.Telephony.WAP_PUSH_RECEIVED"
            boolean r7 = r0.equals(r7)
            if (r7 == 0) goto L_0x0092
            java.lang.String r7 = "application/vnd.wap.mms-message"
            java.lang.String r0 = r8.getType()
            boolean r7 = r7.equals(r0)
            if (r7 == 0) goto L_0x0092
            android.os.Bundle r7 = r8.getExtras()
            if (r7 == 0) goto L_0x0092
            java.lang.String r8 = "phone"
            int r0 = r6.mPhoneId
            int r8 = r7.getInt(r8, r0)
            java.lang.String r0 = "data"
            byte[] r7 = r7.getByteArray(r0)
            java.lang.String r0 = new java.lang.String
            java.nio.charset.Charset r1 = java.nio.charset.StandardCharsets.UTF_8
            r0.<init>(r7, r1)
            java.lang.String r7 = "/TYPE"
            int r7 = r0.indexOf(r7)
            if (r7 <= 0) goto L_0x004f
            int r1 = r7 + -15
            if (r1 <= 0) goto L_0x004f
            java.lang.String r0 = r0.substring(r1, r7)
            java.lang.String r7 = "+"
            int r7 = r0.indexOf(r7)
            if (r7 <= 0) goto L_0x004f
            java.lang.String r0 = r0.substring(r7)
        L_0x004f:
            long r1 = java.lang.System.currentTimeMillis()
            com.sec.internal.ims.servicemodules.im.ImLatchingProcessor r7 = r6.mModule
            java.lang.StringBuilder r3 = new java.lang.StringBuilder
            r3.<init>()
            java.lang.String r4 = "tel:"
            r3.append(r4)
            r3.append(r0)
            java.lang.String r3 = r3.toString()
            com.sec.ims.util.ImsUri r3 = com.sec.ims.util.ImsUri.parse(r3)
            com.sec.ims.util.ImsUri r7 = r7.normalizeUri(r8, r3)
            java.lang.String r3 = r6.TAG
            java.lang.StringBuilder r4 = new java.lang.StringBuilder
            r4.<init>()
            java.lang.String r5 = "origNum - "
            r4.append(r5)
            r4.append(r0)
            java.lang.String r0 = ", mmsTime - "
            r4.append(r0)
            r4.append(r1)
            java.lang.String r0 = r4.toString()
            android.util.Log.d(r3, r0)
            com.sec.internal.ims.servicemodules.im.ImLatchingProcessor r6 = r6.mModule
            r6.processForResolvingLatchingStatus(r7, r1, r8)
        L_0x0092:
            return
        */
        throw new UnsupportedOperationException("Method not decompiled: com.sec.internal.ims.servicemodules.im.MmsReceiver.onReceive(android.content.Context, android.content.Intent):void");
    }
}
