package com.sec.internal.ims.aec.util;

import android.content.Context;
import android.net.ConnectivityManager;
import android.net.Network;
import android.net.NetworkCapabilities;
import android.telephony.TelephonyManager;
import com.sec.internal.constants.ims.ImsConstants;
import com.sec.internal.constants.ims.os.PhoneConstants;
import com.sec.internal.log.AECLog;

public class DataConnectivity {
    private static final String LOG_TAG = "DataConnectivity";

    public static boolean isWifiConnected(Context context) {
        ConnectivityManager connectivityManager = (ConnectivityManager) context.getSystemService("connectivity");
        Network activeNetwork = connectivityManager.getActiveNetwork();
        if (activeNetwork == null) {
            AECLog.d(LOG_TAG, "isWifiConnected: Default NW is null ");
            return false;
        }
        NetworkCapabilities networkCapabilities = connectivityManager.getNetworkCapabilities(activeNetwork);
        if (networkCapabilities == null || !networkCapabilities.hasTransport(1) || !networkCapabilities.hasCapability(16) || !networkCapabilities.hasCapability(12)) {
            return false;
        }
        return true;
    }

    public static boolean isDataAvailable(Context context) {
        boolean isNetworkRoaming = ((TelephonyManager) context.getSystemService(PhoneConstants.PHONE_KEY)).isNetworkRoaming();
        boolean z = ImsConstants.SystemSettings.MOBILE_DATA.get(context, 1) == 1;
        boolean z2 = ImsConstants.SystemSettings.DATA_ROAMING.get(context, 0) == 1;
        String str = LOG_TAG;
        AECLog.d(str, "isNetworkRoaming: " + isNetworkRoaming + ", MobileDataOn: " + z + ", DataRoamingOn:" + z2);
        if (!isNetworkRoaming) {
            return z;
        }
        if (!z || !z2) {
            return false;
        }
        return true;
    }
}
