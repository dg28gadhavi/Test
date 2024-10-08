package com.sec.internal.helper;

import android.content.Context;
import android.database.Cursor;
import android.os.Bundle;
import android.provider.BlockedNumberContract;
import android.sec.enterprise.EnterpriseDeviceManager;
import android.sec.enterprise.PhoneRestrictionPolicy;
import android.text.TextUtils;
import android.util.Log;
import com.sec.internal.constants.ims.ImsConstants;
import com.sec.internal.constants.ims.servicemodules.im.ImDirection;
import com.sec.internal.log.IMSLog;
import java.util.Collections;
import java.util.HashSet;
import java.util.Set;

public class BlockedNumberUtil {
    private static final String CATEGORY = "category";
    private static final String LOG_TAG = "BlockedNumberUtil";
    private static final String SERVICE_ID = "service_id";

    public static boolean isBlockedNumber(Context context, String str) {
        try {
            if (!BlockedNumberContract.canCurrentUserBlockNumbers(context) || BlockedNumberContract.SystemContract.shouldSystemBlockNumber(context, str, (Bundle) null) == 0) {
                return false;
            }
            return true;
        } catch (IllegalArgumentException unused) {
            Log.e(LOG_TAG, "isBlockedNumber occur IllegalArgumentException");
            return false;
        }
    }

    public static boolean isKnoxBlockRequied() {
        return EnterpriseDeviceManager.getInstance().getPhoneRestrictionPolicy().isSmsPatternCheckRequired();
    }

    public static boolean isKnoxBlockedNumber(String str, ImDirection imDirection) {
        boolean z;
        PhoneRestrictionPolicy phoneRestrictionPolicy = EnterpriseDeviceManager.getInstance().getPhoneRestrictionPolicy();
        if (imDirection == ImDirection.OUTGOING) {
            z = phoneRestrictionPolicy.canOutgoingSms(str);
        } else {
            z = phoneRestrictionPolicy.canIncomingSms(str);
        }
        boolean z2 = !z;
        Log.i(LOG_TAG, "isKnoxBlockedNumber: num=" + IMSLog.numberChecker(str) + ", isBlocked=" + z2);
        return z2;
    }

    public static Set<String> getBlockedNumbersList(Context context) {
        Cursor query;
        HashSet hashSet = new HashSet();
        try {
            query = context.getContentResolver().query(BlockedNumberContract.BlockedNumbers.CONTENT_URI, new String[]{"original_number"}, (String) null, (String[]) null, (String) null);
            if (query != null) {
                if (query.moveToFirst()) {
                    do {
                        String string = query.getString(query.getColumnIndex("original_number"));
                        if (!TextUtils.isEmpty(string)) {
                            hashSet.add(string);
                        }
                    } while (query.moveToNext());
                }
            }
            if (query != null) {
                query.close();
            }
            return hashSet;
        } catch (IllegalArgumentException unused) {
            Log.e(LOG_TAG, "getBlockedNumbersList occur IllegalArgumentException");
            return Collections.emptySet();
        } catch (Throwable th) {
            th.addSuppressed(th);
        }
        throw th;
    }

    public static Set<String> getBlockedNumbersListFromNW(Context context) {
        Cursor query;
        HashSet hashSet = new HashSet();
        try {
            query = context.getContentResolver().query(ImsConstants.Uris.SPECIFIC_BOT_URI, new String[]{CATEGORY, "service_id"}, (String) null, (String[]) null, (String) null);
            if (query != null) {
                if (query.moveToFirst()) {
                    do {
                        if (query.getString(query.getColumnIndex(CATEGORY)).compareToIgnoreCase("BLACKLISTED") == 0) {
                            String string = query.getString(query.getColumnIndex("service_id"));
                            if (!TextUtils.isEmpty(string)) {
                                String str = LOG_TAG;
                                Log.d(str, "block list is " + string);
                                hashSet.add(string);
                            }
                        }
                    } while (query.moveToNext());
                }
            }
            if (query != null) {
                query.close();
            }
            return hashSet;
        } catch (IllegalArgumentException | IllegalStateException e) {
            String str2 = LOG_TAG;
            Log.e(str2, "getBlockedNumbersList occur " + e);
            return Collections.emptySet();
        } catch (Throwable th) {
            th.addSuppressed(th);
        }
        throw th;
    }

    /* JADX WARNING: Code restructure failed: missing block: B:9:0x001f, code lost:
        if (r2 < 0) goto L_0x002c;
     */
    /* Code decompiled incorrectly, please refer to instructions dump. */
    public static long getBlockExpires(android.content.Context r8) {
        /*
            r0 = 0
            android.content.ContentResolver r2 = r8.getContentResolver()     // Catch:{ IllegalArgumentException | IllegalStateException -> 0x0033 }
            android.net.Uri r3 = com.sec.internal.constants.ims.ImsConstants.Uris.SPECIFIC_BOT_EXPIRES     // Catch:{ IllegalArgumentException | IllegalStateException -> 0x0033 }
            r4 = 0
            r5 = 0
            r6 = 0
            r7 = 0
            android.database.Cursor r8 = r2.query(r3, r4, r5, r6, r7)     // Catch:{ IllegalArgumentException | IllegalStateException -> 0x0033 }
            if (r8 == 0) goto L_0x002c
            boolean r2 = r8.moveToFirst()     // Catch:{ all -> 0x0022 }
            if (r2 == 0) goto L_0x002c
            r2 = 1
            long r2 = r8.getLong(r2)     // Catch:{ all -> 0x0022 }
            int r4 = (r2 > r0 ? 1 : (r2 == r0 ? 0 : -1))
            if (r4 >= 0) goto L_0x002d
            goto L_0x002c
        L_0x0022:
            r2 = move-exception
            r8.close()     // Catch:{ all -> 0x0027 }
            goto L_0x002b
        L_0x0027:
            r8 = move-exception
            r2.addSuppressed(r8)     // Catch:{ IllegalArgumentException | IllegalStateException -> 0x0033 }
        L_0x002b:
            throw r2     // Catch:{ IllegalArgumentException | IllegalStateException -> 0x0033 }
        L_0x002c:
            r2 = r0
        L_0x002d:
            if (r8 == 0) goto L_0x0032
            r8.close()     // Catch:{ IllegalArgumentException | IllegalStateException -> 0x0033 }
        L_0x0032:
            return r2
        L_0x0033:
            r8 = move-exception
            java.lang.String r2 = LOG_TAG
            java.lang.StringBuilder r3 = new java.lang.StringBuilder
            r3.<init>()
            java.lang.String r4 = "getBlockExpires occur "
            r3.append(r4)
            r3.append(r8)
            java.lang.String r8 = r3.toString()
            android.util.Log.e(r2, r8)
            return r0
        */
        throw new UnsupportedOperationException("Method not decompiled: com.sec.internal.helper.BlockedNumberUtil.getBlockExpires(android.content.Context):long");
    }
}
