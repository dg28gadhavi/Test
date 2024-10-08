package com.sec.internal.helper;

import android.content.ContentResolver;
import android.content.Context;
import android.net.ConnectivityManager;
import android.net.Network;
import android.net.NetworkCapabilities;
import android.net.wifi.WifiManager;
import android.provider.Settings;
import android.telephony.TelephonyManager;
import android.util.Log;
import android.util.Patterns;
import com.sec.internal.constants.ims.ImsConstants;
import com.sec.internal.helper.os.TelephonyManagerWrapper;
import com.sec.internal.log.IMSLog;
import java.net.Inet4Address;
import java.net.Inet6Address;
import java.net.InetAddress;
import java.net.UnknownHostException;

public final class NetworkUtil {
    private static final String LOG_TAG = "NetworkUtil";

    public static boolean is3gppLegacyNetwork(int i) {
        if (!(i == 1 || i == 2 || i == 3 || i == 15 || i == 16)) {
            switch (i) {
                case 8:
                case 9:
                case 10:
                    break;
                default:
                    return false;
            }
        }
        return true;
    }

    public static boolean is3gppPsVoiceNetwork(int i) {
        return i == 13 || i == 20;
    }

    public static boolean isIPv4Address(String str) {
        try {
            if (InetAddress.getByName(str) instanceof Inet4Address) {
                return true;
            }
            return false;
        } catch (UnknownHostException unused) {
            StringBuilder sb = new StringBuilder();
            sb.append("getIPversion : invalid ip : ");
            if (IMSLog.isShipBuild()) {
                str = "xxx";
            }
            sb.append(str);
            Log.e(LOG_TAG, sb.toString());
            return false;
        }
    }

    public static boolean isIPv6Address(String str) {
        try {
            if (InetAddress.getByName(str) instanceof Inet6Address) {
                return true;
            }
            return false;
        } catch (UnknownHostException unused) {
            StringBuilder sb = new StringBuilder();
            sb.append("getIPversion : invalid ip : ");
            if (IMSLog.isShipBuild()) {
                str = "xxx";
            }
            sb.append(str);
            Log.e(LOG_TAG, sb.toString());
            return false;
        }
    }

    public static boolean isMobileDataOn(Context context) {
        return ImsConstants.SystemSettings.MOBILE_DATA.get(context, 1) == 1;
    }

    public static boolean isMobileDataPressed(Context context) {
        return ImsConstants.SystemSettings.MOBILE_DATA_PRESSED.get(context, 1) == 1;
    }

    public static boolean isWifiOn(Context context) {
        WifiManager wifiManager = (WifiManager) context.getSystemService("wifi");
        if (wifiManager != null) {
            return wifiManager.isWifiEnabled();
        }
        return false;
    }

    public static boolean isRoaming(Context context) {
        return TelephonyManagerWrapper.getInstance(context).isNetworkRoaming();
    }

    public static boolean isValidPcscfAddress(String str) {
        if (str == null || str.isEmpty()) {
            return false;
        }
        if (Patterns.DOMAIN_NAME.matcher(str).matches() || isIPv4Address(str) || isIPv6Address(str)) {
            return true;
        }
        return false;
    }

    public static boolean isConnected(Context context) {
        return isConnected(context, false);
    }

    public static int getPreferredNetworkMode(Context context, int i) {
        ContentResolver contentResolver = context.getContentResolver();
        return Settings.Global.getInt(contentResolver, ImsConstants.SystemSettings.PREFFERED_NETWORK_MODE.getName() + i, TelephonyManager.DEFAULT_PREFERRED_NETWORK_MODE);
    }

    public static String transportTypeToString(int i) {
        if (i == -1) {
            return "INVALID";
        }
        if (i == 1) {
            return "WWAN";
        }
        if (i == 2) {
            return "WLAN";
        }
        return "UNKNOWN(" + i + ")";
    }

    public static String dataStateToString(int i) {
        if (i == -1) {
            return "UNKNOWN";
        }
        if (i == 0) {
            return "DISCONNECTED";
        }
        if (i == 1) {
            return "CONNECTING";
        }
        if (i == 2) {
            return "CONNECTED";
        }
        if (i == 3) {
            return "SUSPENDED";
        }
        if (i == 4) {
            return "DISCONNECTING";
        }
        return "UNKNOWN(" + i + ")";
    }

    protected static boolean isConnected(Context context, boolean z) {
        ConnectivityManager connectivityManager = (ConnectivityManager) context.getSystemService("connectivity");
        Network activeNetwork = connectivityManager.getActiveNetwork();
        boolean z2 = false;
        if (activeNetwork == null) {
            Log.i(LOG_TAG, "isConnected : Default NW is null");
            return false;
        }
        NetworkCapabilities networkCapabilities = connectivityManager.getNetworkCapabilities(activeNetwork);
        if (networkCapabilities != null) {
            IMSLog.i(LOG_TAG, "Network Cap check : INTERNET : " + networkCapabilities.hasCapability(12));
            if (networkCapabilities.hasCapability(12)) {
                if (z) {
                    z2 = networkCapabilities.hasTransport(0);
                } else if (networkCapabilities.hasTransport(0) || networkCapabilities.hasTransport(1)) {
                    z2 = true;
                }
            }
        }
        IMSLog.i(LOG_TAG, "isConnected = " + z2 + " isCellularOnly : " + z);
        return z2;
    }

    public static boolean isMobileDataConnected(Context context) {
        return isConnected(context, true);
    }
}
