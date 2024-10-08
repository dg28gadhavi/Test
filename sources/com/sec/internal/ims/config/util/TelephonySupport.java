package com.sec.internal.ims.config.util;

import com.sec.internal.helper.ByteArrayWriter;
import com.sec.internal.helper.StrUtil;
import com.sec.internal.log.IMSLog;

public class TelephonySupport {
    private static final String LOG_TAG = "TelephonySupport";

    /* JADX WARNING: Removed duplicated region for block: B:50:0x008d A[ADDED_TO_REGION] */
    /* Code decompiled incorrectly, please refer to instructions dump. */
    public static com.sec.internal.ims.config.util.AkaResponse buildAkaResponse(java.lang.String r8) {
        /*
            byte[] r8 = com.sec.internal.helper.StrUtil.hexStringToBytes(r8)
            r0 = 0
            if (r8 == 0) goto L_0x0087
            r1 = 0
            byte r2 = r8[r1]     // Catch:{ Exception -> 0x0067 }
            r3 = -37
            r4 = 1
            r5 = 2
            if (r2 != r3) goto L_0x004f
            java.lang.String r2 = LOG_TAG     // Catch:{ Exception -> 0x0067 }
            java.lang.String r3 = "calculateAkaResponse: in"
            android.util.Log.d(r2, r3)     // Catch:{ Exception -> 0x0067 }
            byte r2 = r8[r4]     // Catch:{ Exception -> 0x0067 }
            if (r2 <= 0) goto L_0x0021
            byte[] r3 = new byte[r2]     // Catch:{ Exception -> 0x0067 }
            java.lang.System.arraycopy(r8, r5, r3, r1, r2)     // Catch:{ Exception -> 0x004b }
            goto L_0x0022
        L_0x0021:
            r3 = r0
        L_0x0022:
            int r4 = r2 + 2
            byte r4 = r8[r4]     // Catch:{ Exception -> 0x004b }
            if (r4 <= 0) goto L_0x0030
            byte[] r5 = new byte[r4]     // Catch:{ Exception -> 0x004b }
            int r6 = r2 + 3
            java.lang.System.arraycopy(r8, r6, r5, r1, r4)     // Catch:{ Exception -> 0x0047 }
            goto L_0x0031
        L_0x0030:
            r5 = r0
        L_0x0031:
            int r6 = r2 + 3
            int r6 = r6 + r4
            byte r6 = r8[r6]     // Catch:{ Exception -> 0x0047 }
            if (r6 <= 0) goto L_0x0044
            byte[] r7 = new byte[r6]     // Catch:{ Exception -> 0x0047 }
            int r2 = r2 + 4
            int r2 = r2 + r4
            java.lang.System.arraycopy(r8, r2, r7, r1, r6)     // Catch:{ Exception -> 0x0041 }
            goto L_0x0045
        L_0x0041:
            r8 = move-exception
            r1 = r0
            goto L_0x006c
        L_0x0044:
            r7 = r0
        L_0x0045:
            r8 = r0
            goto L_0x008b
        L_0x0047:
            r8 = move-exception
            r1 = r0
            r7 = r1
            goto L_0x006c
        L_0x004b:
            r8 = move-exception
            r1 = r0
            r5 = r1
            goto L_0x006b
        L_0x004f:
            r3 = -36
            if (r2 != r3) goto L_0x0087
            byte r2 = r8[r4]     // Catch:{ Exception -> 0x0067 }
            if (r2 <= 0) goto L_0x0087
            byte[] r3 = new byte[r2]     // Catch:{ Exception -> 0x0067 }
            java.lang.System.arraycopy(r8, r5, r3, r1, r2)     // Catch:{ Exception -> 0x0061 }
            r5 = r0
            r7 = r5
            r8 = r3
            r3 = r7
            goto L_0x008b
        L_0x0061:
            r8 = move-exception
            r5 = r0
            r7 = r5
            r1 = r3
            r3 = r7
            goto L_0x006c
        L_0x0067:
            r8 = move-exception
            r1 = r0
            r3 = r1
            r5 = r3
        L_0x006b:
            r7 = r5
        L_0x006c:
            r8.printStackTrace()
            java.lang.String r2 = LOG_TAG
            java.lang.StringBuilder r4 = new java.lang.StringBuilder
            r4.<init>()
            java.lang.String r6 = "error2:"
            r4.append(r6)
            r4.append(r8)
            java.lang.String r8 = r4.toString()
            android.util.Log.d(r2, r8)
            r8 = r1
            goto L_0x008b
        L_0x0087:
            r8 = r0
            r3 = r8
            r5 = r3
            r7 = r5
        L_0x008b:
            if (r3 != 0) goto L_0x0090
            if (r8 != 0) goto L_0x0090
            goto L_0x0095
        L_0x0090:
            com.sec.internal.ims.config.util.AkaResponse r0 = new com.sec.internal.ims.config.util.AkaResponse
            r0.<init>(r5, r7, r8, r3)
        L_0x0095:
            return r0
        */
        throw new UnsupportedOperationException("Method not decompiled: com.sec.internal.ims.config.util.TelephonySupport.buildAkaResponse(java.lang.String):com.sec.internal.ims.config.util.AkaResponse");
    }

    public static byte[] buildMainKey(String str, String str2) {
        AkaResponse buildAkaResponse = buildAkaResponse(str2);
        if (buildAkaResponse == null) {
            return null;
        }
        byte[] bytes = str.getBytes();
        byte[] ik = buildAkaResponse.getIk();
        String str3 = LOG_TAG;
        IMSLog.s(str3, "IK :" + StrUtil.bytesToHexString(ik));
        byte[] ck = buildAkaResponse.getCk();
        IMSLog.s(str3, "CK :" + StrUtil.bytesToHexString(ck));
        if (ik == null || ck == null) {
            return null;
        }
        ByteArrayWriter byteArrayWriter = new ByteArrayWriter(bytes.length + ik.length + ck.length);
        byteArrayWriter.write(bytes);
        byteArrayWriter.write(ik);
        byteArrayWriter.write(ck);
        return byteArrayWriter.getResult();
    }
}
