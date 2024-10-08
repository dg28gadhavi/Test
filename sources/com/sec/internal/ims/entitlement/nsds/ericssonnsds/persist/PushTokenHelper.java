package com.sec.internal.ims.entitlement.nsds.ericssonnsds.persist;

import android.annotation.SuppressLint;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.text.TextUtils;
import android.util.Base64;
import com.sec.internal.constants.ims.entitilement.NSDSNamespaces;
import com.sec.internal.ims.entitlement.nsds.NSDSMultiSimService;
import com.sec.internal.ims.entitlement.storagehelper.NSDSSharedPrefHelper;
import com.sec.internal.ims.util.ImsUtil;
import com.sec.internal.log.IMSLog;

public class PushTokenHelper {
    private static final String LOG_TAG = "PushTokenHelper";

    public static String getPushToken(Context context, String str) {
        SharedPreferences sharedPref = NSDSSharedPrefHelper.getSharedPref(context, NSDSNamespaces.NSDSSharedPref.NAME_SHARED_PREF, 0);
        String str2 = null;
        if (sharedPref != null) {
            str2 = sharedPref.getString(str + ":" + NSDSNamespaces.NSDSSharedPref.PREF_PUSH_TOKEN, (String) null);
        }
        if (!TextUtils.isEmpty(str2)) {
            return str2;
        }
        requestGcmRegistrationToken(context);
        return generatePushToken();
    }

    private static void requestGcmRegistrationToken(Context context) {
        IMSLog.i(LOG_TAG, "push token was dummy.txt. Requesting one from GCM now");
        Intent intent = new Intent(context, NSDSMultiSimService.class);
        intent.setAction(NSDSNamespaces.NSDSActions.ACTION_REFRESH_GCM_TOKEN);
        context.startService(intent);
    }

    @SuppressLint({"TrulyRandom"})
    private static String generatePushToken() {
        String encodeToString = Base64.encodeToString(Long.toHexString(ImsUtil.getRandom().nextLong()).getBytes(), 2);
        String str = LOG_TAG;
        IMSLog.s(str, "generatePushToken: " + encodeToString);
        return encodeToString;
    }
}
