package com.sec.internal.ims.entitlement.nsds.strategy.operation;

import android.os.Bundle;
import android.util.Log;
import com.sec.internal.constants.ims.entitilement.NSDSNamespaces;

public class XAAEntitlementOperation {
    private static final String LOG_TAG = "XAAEntitlementOperation";

    protected static int getInitialOperation(int i, int i2) {
        if (i == 5) {
            return 3;
        }
        if (i != 7) {
            if (i == 19) {
                return 15;
            }
            if (i == 11) {
                return 1;
            }
            if (i != 12) {
                return 2;
            }
        }
        return i2 == 1000 ? 2 : -1;
    }

    protected static int getOperationAfterLocAndTcCheck(int i, int i2, boolean z) {
        if (i == 2) {
            return (i2 != 1000 || z) ? -1 : 8;
        }
        if (i == 5 && i2 == 1000) {
            return 13;
        }
    }

    public static int getOperation(int i, int i2, int i3, Bundle bundle) {
        String str = LOG_TAG;
        Log.i(str, "getOperation: eventType " + i + " prevOp " + i2);
        boolean z = bundle != null ? bundle.getBoolean(NSDSNamespaces.NSDSDataMapKey.LOC_AND_TC_STATUS) : false;
        if (i2 == -1) {
            return getInitialOperation(i, i3);
        }
        return getNextOperation(i2, i, i3, z);
    }

    protected static int getNextOperation(int i, int i2, int i3, boolean z) {
        if (i != 2) {
            if (i == 3) {
                return getOperationAfterLocAndTcCheck(i2, i3, z);
            }
        } else if (i3 == 1000) {
            return 3;
        }
        return -1;
    }
}
