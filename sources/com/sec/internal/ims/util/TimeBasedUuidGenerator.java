package com.sec.internal.ims.util;

import android.content.Context;
import android.util.Log;
import com.sec.internal.helper.ImsSharedPrefHelper;
import com.sec.internal.helper.RcsConfigurationHelper;
import com.sec.internal.log.IMSLog;
import java.util.UUID;
import java.util.regex.Pattern;

public class TimeBasedUuidGenerator {
    private static final String LOG_TAG = "TimeBasedUuidGenerator";
    protected static final String SHAREDPREF_INSTANCE_ID_UUID_KEY = "instanceIdUuid";
    protected static final String UUID_CORE_PATTERN = "[0-9a-f]{8}-[0-9a-f]{4}-[0-9a-f]{4}-[0-9a-f]{4}-[0-9a-f]{12}";
    protected static final Pattern UUID_PURE_PATTERN = Pattern.compile("^[0-9a-f]{8}-[0-9a-f]{4}-[0-9a-f]{4}-[0-9a-f]{4}-[0-9a-f]{12}$");
    private static final Pattern UUID_STRIP = Pattern.compile("(<)|(urn:uuid:)|(>)");
    private Context mContext;
    private int mPhoneId;
    private UuidSource mUuidSource;

    private enum UuidSource {
        AUTOCONFIG,
        SHAREDPREFS,
        GENERATOR
    }

    public TimeBasedUuidGenerator(int i, Context context) {
        this.mPhoneId = i;
        this.mContext = context;
    }

    private String generate() {
        return generate(compute100nsTimestamp(), randSeq(), getWifiMacAddr());
    }

    /* access modifiers changed from: protected */
    public String generate(long j, long j2, long j3) {
        return new UUID(((4294967295L & j) << 32) | (((281470681743360L & j) >>> 32) << 16) | 4096 | ((j & 1152640029630136320L) >>> 48), (j2 << 48) | Long.MIN_VALUE | j3).toString();
    }

    /* JADX WARNING: Code restructure failed: missing block: B:2:0x000d, code lost:
        r3 = r3.getConnectionInfo();
     */
    /* Code decompiled incorrectly, please refer to instructions dump. */
    private long getWifiMacAddr() {
        /*
            r3 = this;
            android.content.Context r3 = r3.mContext
            java.lang.String r0 = "wifi"
            java.lang.Object r3 = r3.getSystemService(r0)
            android.net.wifi.WifiManager r3 = (android.net.wifi.WifiManager) r3
            if (r3 == 0) goto L_0x0026
            android.net.wifi.WifiInfo r3 = r3.getConnectionInfo()
            if (r3 == 0) goto L_0x0026
            java.lang.String r0 = r3.getMacAddress()
            if (r0 == 0) goto L_0x0026
            java.lang.String r3 = r3.getMacAddress()
            java.lang.String r0 = ":"
            java.lang.String r1 = ""
            java.lang.String r3 = r3.replace(r0, r1)
            goto L_0x0028
        L_0x0026:
            java.lang.String r3 = "000000000000"
        L_0x0028:
            java.lang.String r0 = LOG_TAG
            java.lang.StringBuilder r1 = new java.lang.StringBuilder
            r1.<init>()
            java.lang.String r2 = "getWifiMacAddr: ["
            r1.append(r2)
            r1.append(r3)
            java.lang.String r2 = "]"
            r1.append(r2)
            java.lang.String r1 = r1.toString()
            com.sec.internal.log.IMSLog.s(r0, r1)
            java.lang.StringBuilder r0 = new java.lang.StringBuilder
            r0.<init>()
            java.lang.String r1 = "0x"
            r0.append(r1)
            r0.append(r3)
            java.lang.String r3 = r0.toString()
            java.lang.Long r3 = java.lang.Long.decode(r3)
            long r0 = r3.longValue()
            return r0
        */
        throw new UnsupportedOperationException("Method not decompiled: com.sec.internal.ims.util.TimeBasedUuidGenerator.getWifiMacAddr():long");
    }

    private long compute100nsTimestamp() {
        String str = LOG_TAG;
        StringBuilder sb = new StringBuilder();
        sb.append("compute100nsTimestamp: ");
        long currentTimeMillis = (System.currentTimeMillis() * 10000) + 122192928000000000L;
        sb.append(currentTimeMillis);
        Log.d(str, sb.toString());
        return currentTimeMillis;
    }

    private long randSeq() {
        byte[] bArr = new byte[2];
        ImsUtil.getRandom().nextBytes(bArr);
        return ((((long) bArr[1]) * 256) + ((long) bArr[0])) & 16383;
    }

    public String getUuidInstanceId() {
        String obtainUuid = obtainUuid();
        if (!obtainUuid.isEmpty() && this.mUuidSource == UuidSource.GENERATOR) {
            ImsSharedPrefHelper.save(this.mPhoneId, this.mContext, ImsSharedPrefHelper.IMS_USER_DATA, SHAREDPREF_INSTANCE_ID_UUID_KEY, obtainUuid);
        }
        return "<urn:uuid:" + obtainUuid + ">";
    }

    private String obtainUuid() {
        String replaceAll = UUID_STRIP.matcher(RcsConfigurationHelper.getUuid(this.mContext, this.mPhoneId).toLowerCase()).replaceAll("");
        String str = LOG_TAG;
        IMSLog.s(str, "selectUuidInstanceId from config: " + replaceAll);
        if (UUID_PURE_PATTERN.matcher(replaceAll).matches()) {
            this.mUuidSource = UuidSource.AUTOCONFIG;
            return replaceAll;
        }
        String string = ImsSharedPrefHelper.getString(this.mPhoneId, this.mContext, ImsSharedPrefHelper.IMS_USER_DATA, SHAREDPREF_INSTANCE_ID_UUID_KEY, "");
        if (!string.isEmpty()) {
            IMSLog.s(str, "selectUuidInstanceId from sharedPref: " + string);
            this.mUuidSource = UuidSource.SHAREDPREFS;
            return string;
        }
        Log.d(str, "selectUuidInstanceId from sharedPref Empty");
        String generate = generate();
        IMSLog.s(str, "selectUuidInstanceId from Generator: " + generate);
        this.mUuidSource = UuidSource.GENERATOR;
        return generate;
    }
}
