package com.sec.internal.ims.entitlement.nsds.strategy.operation;

import android.os.Bundle;
import android.util.Log;
import com.sec.internal.constants.ims.entitilement.NSDSNamespaces;

public class DefaultNsdsOperation {
    private static final String LOG_TAG = "DefaultNsdsOperation";

    protected static int getOperationAfterLocAndTcCheck(int i, int i2, boolean z) {
        return (i == 2 && i2 == 1000 && !z) ? 8 : -1;
    }

    public static int getOperation(int i, int i2, int i3, Bundle bundle) {
        boolean z;
        String str = LOG_TAG;
        Log.i(str, "getOperation: eventType-" + i + " prevOp-" + i2);
        if (bundle != null) {
            z = bundle.getBoolean(NSDSNamespaces.NSDSDataMapKey.LOC_AND_TC_STATUS);
        } else {
            z = false;
        }
        if (i2 == -1) {
            if (i != 2) {
                if (i != 7) {
                    if (i == 11) {
                        return 1;
                    }
                    if (i == 19) {
                        return 15;
                    }
                    switch (i) {
                        case 13:
                            return 9;
                        case 14:
                            return 10;
                        case 15:
                            return 11;
                        default:
                            return -1;
                    }
                } else if (i3 != 1000) {
                    return -1;
                }
            }
            return 2;
        } else if (i2 != 2) {
            if (i2 != 3) {
                return -1;
            }
            return getOperationAfterLocAndTcCheck(i, i3, z);
        } else if (i3 != 1000) {
            return -1;
        } else {
            Log.i(str, "getOperation(): BULK_ENTITLEMENT_CHECK");
            return 3;
        }
    }
}
