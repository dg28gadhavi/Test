package com.sec.internal.ims.cmstore.adapters;

import android.content.ContentResolver;
import android.content.Context;
import android.database.Cursor;
import android.net.Uri;
import android.util.Log;
import com.sec.internal.constants.ims.entitilement.NSDSContractExt;
import com.sec.internal.log.IMSLog;
import java.util.ArrayList;

public class JanskyProviderAdapter {
    public static final Uri AUTHORITY_URI = Uri.parse("content://com.samsung.ims.nsds.provider");
    private static final String LOG_TAG = "JanskyProviderAdapter";
    public static final String PROVIDER_NAME = "com.samsung.ims.nsds.provider";
    private final Context mContext;
    private ContentResolver mResolver = null;

    public void onTokenExpired() {
    }

    public JanskyProviderAdapter(Context context) {
        Log.d(LOG_TAG, "Create JanskyServiceTranslation.");
        this.mContext = context;
        this.mResolver = context.getContentResolver();
    }

    public String getSIT(String str) {
        String str2 = "";
        if (str == null) {
            return str2;
        }
        ArrayList arrayList = new ArrayList();
        Uri buildActiveLinesWithServicveUri = NSDSContractExt.Lines.buildActiveLinesWithServicveUri();
        arrayList.clear();
        Cursor query = this.mContext.getContentResolver().query(buildActiveLinesWithServicveUri, (String[]) null, "status = ?", new String[]{"1"}, (String) null);
        if (query != null) {
            try {
                if (query.moveToFirst()) {
                    do {
                        String string = query.getString(query.getColumnIndex("msisdn"));
                        String string2 = query.getString(query.getColumnIndex(NSDSContractExt.ServiceColumns.SERVICE_INSTANCE_TOKEN));
                        String str3 = LOG_TAG;
                        Log.i(str3, "line: " + IMSLog.checker(str) + " msisdn " + IMSLog.checker(string) + ", token " + IMSLog.checker(string2));
                        if (str.contains(string)) {
                            str2 = string2;
                        }
                    } while (query.moveToNext());
                }
            } catch (Throwable th) {
                th.addSuppressed(th);
            }
        }
        if (query != null) {
            query.close();
        }
        return str2;
        throw th;
    }

    public Cursor getActiveLines() {
        return this.mResolver.query(NSDSContractExt.Lines.buildActiveLinesWithServicveUri(), (String[]) null, (String) null, (String[]) null, (String) null);
    }

    /* JADX WARNING: Removed duplicated region for block: B:18:0x0073  */
    /* Code decompiled incorrectly, please refer to instructions dump. */
    public java.lang.String getNativeLine() {
        /*
            r7 = this;
            java.util.ArrayList r0 = new java.util.ArrayList
            r0.<init>()
            android.net.Uri r2 = com.sec.internal.constants.ims.entitilement.NSDSContractExt.Lines.buildActiveLinesWithServicveUri()
            r0.clear()
            java.lang.String r4 = "status = ?"
            java.lang.String r0 = "1"
            java.lang.String[] r5 = new java.lang.String[]{r0}
            android.content.Context r7 = r7.mContext
            android.content.ContentResolver r1 = r7.getContentResolver()
            r3 = 0
            r6 = 0
            android.database.Cursor r7 = r1.query(r2, r3, r4, r5, r6)
            if (r7 == 0) goto L_0x006f
            boolean r1 = r7.moveToFirst()     // Catch:{ all -> 0x0065 }
            if (r1 == 0) goto L_0x006f
        L_0x0029:
            java.lang.String r1 = "is_native"
            int r1 = r7.getColumnIndex(r1)     // Catch:{ all -> 0x0065 }
            java.lang.String r1 = r7.getString(r1)     // Catch:{ all -> 0x0065 }
            boolean r1 = r0.equals(r1)     // Catch:{ all -> 0x0065 }
            if (r1 == 0) goto L_0x005e
            java.lang.String r0 = "msisdn"
            int r0 = r7.getColumnIndex(r0)     // Catch:{ all -> 0x0065 }
            java.lang.String r0 = r7.getString(r0)     // Catch:{ all -> 0x0065 }
            java.lang.String r1 = LOG_TAG     // Catch:{ all -> 0x0065 }
            java.lang.StringBuilder r2 = new java.lang.StringBuilder     // Catch:{ all -> 0x0065 }
            r2.<init>()     // Catch:{ all -> 0x0065 }
            java.lang.String r3 = "msisdn: "
            r2.append(r3)     // Catch:{ all -> 0x0065 }
            java.lang.String r3 = com.sec.internal.log.IMSLog.checker(r0)     // Catch:{ all -> 0x0065 }
            r2.append(r3)     // Catch:{ all -> 0x0065 }
            java.lang.String r2 = r2.toString()     // Catch:{ all -> 0x0065 }
            android.util.Log.i(r1, r2)     // Catch:{ all -> 0x0065 }
            goto L_0x0071
        L_0x005e:
            boolean r1 = r7.moveToNext()     // Catch:{ all -> 0x0065 }
            if (r1 != 0) goto L_0x0029
            goto L_0x006f
        L_0x0065:
            r0 = move-exception
            r7.close()     // Catch:{ all -> 0x006a }
            goto L_0x006e
        L_0x006a:
            r7 = move-exception
            r0.addSuppressed(r7)
        L_0x006e:
            throw r0
        L_0x006f:
            java.lang.String r0 = ""
        L_0x0071:
            if (r7 == 0) goto L_0x0076
            r7.close()
        L_0x0076:
            return r0
        */
        throw new UnsupportedOperationException("Method not decompiled: com.sec.internal.ims.cmstore.adapters.JanskyProviderAdapter.getNativeLine():java.lang.String");
    }
}
