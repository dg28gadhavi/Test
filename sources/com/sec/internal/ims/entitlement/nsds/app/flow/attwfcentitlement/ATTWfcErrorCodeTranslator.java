package com.sec.internal.ims.entitlement.nsds.app.flow.attwfcentitlement;

import android.util.Log;
import com.sec.internal.constants.ims.entitilement.NSDSNamespaces;
import com.sec.internal.ims.entitlement.storagehelper.NSDSDatabaseHelper;
import java.util.HashMap;
import java.util.Map;

public class ATTWfcErrorCodeTranslator {
    private static final int ENTITLEMENT_CHECK_MAX_RETRY = 2;
    private static final String LOG_TAG = "ATTWfcErrorCodeTranslator";
    private static final Map<Integer, Integer> sMapE911FilteredFailureCodes;
    private static final Map<Integer, Integer> sMapE911FilteredSuccessCodes;

    static {
        HashMap hashMap = new HashMap();
        sMapE911FilteredSuccessCodes = hashMap;
        Integer valueOf = Integer.valueOf(NSDSNamespaces.NSDSDefinedResponseCode.SVC_PROVISION_COMPLETED_SUCCESS_CODE);
        hashMap.put(7, valueOf);
        hashMap.put(10, valueOf);
        hashMap.put(9, valueOf);
        Integer valueOf2 = Integer.valueOf(NSDSNamespaces.NSDSDefinedResponseCode.LOCATIONANDTC_UPDATE_SUCCESS_CODE);
        hashMap.put(8, valueOf2);
        hashMap.put(12, valueOf2);
        hashMap.put(2, Integer.valueOf(NSDSNamespaces.NSDSDefinedResponseCode.LOCATIONANDTC_UPDATE_NOT_REQUIRED));
        Integer valueOf3 = Integer.valueOf(NSDSNamespaces.NSDSDefinedResponseCode.FORCE_TOGGLE_OFF_ERROR_CODE);
        hashMap.put(3, valueOf3);
        HashMap hashMap2 = new HashMap();
        sMapE911FilteredFailureCodes = hashMap2;
        Integer valueOf4 = Integer.valueOf(NSDSNamespaces.NSDSDefinedResponseCode.SVC_NOT_PROVISIONED_ERROR_CODE);
        hashMap2.put(7, valueOf4);
        hashMap2.put(10, valueOf4);
        hashMap2.put(9, valueOf4);
        hashMap2.put(8, valueOf3);
        hashMap2.put(12, valueOf3);
        hashMap2.put(2, valueOf3);
    }

    public static int translateErrorCode(NSDSDatabaseHelper nSDSDatabaseHelper, int i, boolean z, int i2, int i3, String str) {
        String str2 = LOG_TAG;
        Log.i(str2, "translateErrorCode: deviceEventType " + i + "success " + z + "nsdsErrorCode " + i2 + "retryCount " + i3);
        if (i2 == 1000) {
            return filterSuccessCodeWithE911Validity(nSDSDatabaseHelper, i, str);
        }
        if (i2 != 1046) {
            if (i2 == 2500 || i2 == 2300 || i2 == 2301) {
                return i2;
            }
            return translateErrorCodeByEventType(i, z, i3);
        } else if (i == 2) {
            return NSDSNamespaces.NSDSDefinedResponseCode.SVC_NOT_PROVISIONED_ERROR_CODE;
        } else {
            return -1;
        }
    }

    private static int filterSuccessCodeWithE911Validity(NSDSDatabaseHelper nSDSDatabaseHelper, int i, String str) {
        Integer num;
        if (nSDSDatabaseHelper == null || !nSDSDatabaseHelper.isE911InfoAvailForNativeLine(str)) {
            num = sMapE911FilteredFailureCodes.get(Integer.valueOf(i));
            if (num == null) {
                num = Integer.valueOf(NSDSNamespaces.NSDSDefinedResponseCode.FORCE_TOGGLE_OFF_ERROR_CODE);
            }
        } else {
            num = sMapE911FilteredSuccessCodes.get(Integer.valueOf(i));
            if (num == null) {
                num = 1000;
            }
        }
        return num.intValue();
    }

    private static int translateErrorCodeByEventType(int i, boolean z, int i2) {
        if (z) {
            Log.e(LOG_TAG, "translateErrorCodeByEventType: result cannot be success");
            return -1;
        }
        if (i != 1) {
            if (i != 2) {
                if (i != 4) {
                    if (i != 9) {
                        if (i != 10) {
                            return -1;
                        }
                        if (i2 != 2) {
                            return NSDSNamespaces.NSDSDefinedResponseCode.SVC_PROVISION_PENDING_ERROR_CODE;
                        }
                    }
                }
            }
            return NSDSNamespaces.NSDSDefinedResponseCode.SVC_NOT_PROVISIONED_ERROR_CODE;
        }
        return NSDSNamespaces.NSDSDefinedResponseCode.FORCE_TOGGLE_OFF_ERROR_CODE;
    }
}
