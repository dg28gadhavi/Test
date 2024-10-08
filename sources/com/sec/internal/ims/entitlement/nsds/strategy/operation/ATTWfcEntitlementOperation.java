package com.sec.internal.ims.entitlement.nsds.strategy.operation;

import android.os.Bundle;
import android.util.Log;
import com.sec.internal.constants.ims.entitilement.NSDSNamespaces;

public class ATTWfcEntitlementOperation {
    private static final String LOG_TAG = "ATTWfcEntitlementOperation";

    protected static int getInitialOperation(int i, int i2) {
        int i3 = 3;
        if (i == 3) {
            return 5;
        }
        if (i != 5) {
            i3 = 2;
            if (i != 7) {
                if (i == 19) {
                    return 15;
                }
                if (i == 11) {
                    return 1;
                }
                if (i != 12) {
                    if (i == 14) {
                        return 10;
                    }
                    if (i == 15) {
                        return 11;
                    }
                }
            }
            if (i2 != 1000) {
                return -1;
            }
        }
        return i3;
    }

    protected static int getOperationAfterLocAndTcCheck(int i, int i2, boolean z, boolean z2) {
        if (i == 2) {
            return (i2 != 1000 || (z && !z2)) ? -1 : 8;
        }
        if (i == 5 && i2 == 1000) {
            return 13;
        }
    }

    protected static int getOperationAfterPushTokenRegistration(int i, int i2) {
        return (i2 == 1000 && i == 2) ? 3 : -1;
    }

    public static int getOperation(int i, int i2, int i3, Bundle bundle) {
        String str;
        boolean z;
        boolean z2;
        Log.i(LOG_TAG, "getOperation: eventType " + i + " prevOp " + i2);
        if (bundle != null) {
            boolean z3 = bundle.getBoolean(NSDSNamespaces.NSDSDataMapKey.LOC_AND_TC_STATUS);
            boolean z4 = bundle.getBoolean(NSDSNamespaces.NSDSDataMapKey.SVC_PROV_STATUS);
            str = bundle.getString(NSDSNamespaces.NSDSDataMapKey.E911_AID_EXP);
            z2 = z3;
            z = z4;
        } else {
            str = null;
            z2 = false;
            z = false;
        }
        if (i2 == -1) {
            return getInitialOperation(i, i3);
        }
        return getNextOperation(i2, i, i3, z2, z, str);
    }

    protected static int getNextOperation(int i, int i2, int i3, boolean z, boolean z2, String str) {
        if (i == 2) {
            return getOperationAfterEntitlementCheck(i2, i3, str, z2);
        }
        if (i == 3) {
            return getOperationAfterLocAndTcCheck(i2, i3, z, z2);
        }
        if (i == 4) {
            return getOperationAfterPushTokenRegistration(i2, i3);
        }
        if (i != 16) {
            return -1;
        }
        return getOperationAfterLocAndTcCheckforAutoOn(i2, i3, z, z2);
    }

    /* JADX WARNING: Code restructure failed: missing block: B:19:0x0052, code lost:
        return -1;
     */
    /* JADX WARNING: Code restructure failed: missing block: B:20:?, code lost:
        return 4;
     */
    /* JADX WARNING: Code restructure failed: missing block: B:6:0x0014, code lost:
        if (r6 != 1048) goto L_0x0052;
     */
    /* Code decompiled incorrectly, please refer to instructions dump. */
    protected static int getOperationAfterEntitlementCheck(int r5, int r6, java.lang.String r7, boolean r8) {
        /*
            r0 = 3
            r1 = 1048(0x418, float:1.469E-42)
            r2 = 4
            r3 = 1000(0x3e8, float:1.401E-42)
            switch(r5) {
                case 1: goto L_0x0018;
                case 2: goto L_0x0012;
                case 3: goto L_0x0009;
                case 4: goto L_0x000d;
                case 5: goto L_0x0009;
                case 6: goto L_0x000a;
                case 7: goto L_0x000a;
                case 8: goto L_0x000a;
                case 9: goto L_0x000a;
                case 10: goto L_0x000a;
                default: goto L_0x0009;
            }
        L_0x0009:
            goto L_0x0052
        L_0x000a:
            if (r6 != r3) goto L_0x0052
            goto L_0x0053
        L_0x000d:
            if (r6 != r3) goto L_0x0010
            goto L_0x0016
        L_0x0010:
            r0 = 5
            goto L_0x0053
        L_0x0012:
            if (r6 == r3) goto L_0x0016
            if (r6 != r1) goto L_0x0052
        L_0x0016:
            r0 = r2
            goto L_0x0053
        L_0x0018:
            java.lang.String r5 = LOG_TAG
            java.lang.StringBuilder r2 = new java.lang.StringBuilder
            r2.<init>()
            java.lang.String r4 = "[ATT_AutoOn] getOperationAfterEntitlementCheck responseCode: "
            r2.append(r4)
            r2.append(r6)
            java.lang.String r4 = ",onSvcProv:"
            r2.append(r4)
            r2.append(r8)
            java.lang.String r2 = r2.toString()
            android.util.Log.i(r5, r2)
            if (r6 != r3) goto L_0x003f
            boolean r5 = com.sec.internal.ims.entitlement.util.E911AidValidator.validate(r7)
            if (r5 != 0) goto L_0x003f
            goto L_0x0053
        L_0x003f:
            r5 = 0
            boolean r5 = com.sec.internal.ims.entitlement.util.EntFeatureDetector.checkWFCAutoOnEnabled(r5)
            if (r5 == 0) goto L_0x0052
            r0 = 19
            if (r6 != r1) goto L_0x004d
            if (r8 != 0) goto L_0x0052
            goto L_0x0053
        L_0x004d:
            r5 = 1063(0x427, float:1.49E-42)
            if (r6 != r5) goto L_0x0052
            goto L_0x0053
        L_0x0052:
            r0 = -1
        L_0x0053:
            return r0
        */
        throw new UnsupportedOperationException("Method not decompiled: com.sec.internal.ims.entitlement.nsds.strategy.operation.ATTWfcEntitlementOperation.getOperationAfterEntitlementCheck(int, int, java.lang.String, boolean):int");
    }

    private static int getOperationAfterLocAndTcCheckforAutoOn(int i, int i2, boolean z, boolean z2) {
        if (i == 2) {
            String str = LOG_TAG;
            Log.i(str, "[ATT_AutoOn] getOperationAfterLocAndTcCheckforAutoOn responseCode: " + i2 + ",onSvcProv:" + z2);
            if (i2 != 1000) {
                return ((i2 != 1048 || z2) && i2 != 1063) ? 17 : 18;
            }
            if (!z) {
                Log.i(str, "[ATT_AutoOn] getOperationAfterLocAndTcCheckforAutoOn responseCode: " + i2 + ",locAndTcStatus:" + z);
            }
        }
        return -1;
    }
}
