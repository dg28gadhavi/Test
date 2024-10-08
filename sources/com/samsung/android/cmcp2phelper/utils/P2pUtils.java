package com.samsung.android.cmcp2phelper.utils;

import android.content.Context;
import android.net.ConnectivityManager;
import android.net.NetworkRequest;
import android.net.wifi.WifiManager;
import android.util.Log;
import java.math.BigInteger;
import java.net.InetAddress;
import java.net.UnknownHostException;
import java.nio.ByteOrder;

public class P2pUtils {
    private static final String LOG_TAG = ("cmcp2phelper/1.3.06/" + P2pUtils.class.getSimpleName());

    public static boolean isWifiConnected(Context context) {
        String localIpAddress;
        if (context != null && ((WifiManager) context.getSystemService("wifi")).isWifiEnabled() && (localIpAddress = getLocalIpAddress(context)) != null && !localIpAddress.isEmpty()) {
            return true;
        }
        return false;
    }

    public static String getLocalIpAddress(Context context) {
        if (context == null) {
            return "";
        }
        int ipAddress = ((WifiManager) context.getSystemService("wifi")).getConnectionInfo().getIpAddress();
        if (ByteOrder.nativeOrder().equals(ByteOrder.LITTLE_ENDIAN)) {
            ipAddress = Integer.reverseBytes(ipAddress);
        }
        try {
            return InetAddress.getByAddress(BigInteger.valueOf((long) ipAddress).toByteArray()).getHostAddress();
        } catch (UnknownHostException unused) {
            Log.e(LOG_TAG, "Unable to get host address.");
            return "";
        }
    }

    public static void registerWiFiNetworkCallback(Context context, ConnectivityManager.NetworkCallback networkCallback) {
        ConnectivityManager connectivityManager = (ConnectivityManager) context.getSystemService("connectivity");
        NetworkRequest build = new NetworkRequest.Builder().addTransportType(1).addCapability(12).build();
        try {
            Log.i(LOG_TAG, "connectivityManager.registerNetworkCallback");
            connectivityManager.registerNetworkCallback(build, networkCallback);
        } catch (Exception unused) {
            Log.i(LOG_TAG, "connectivityManager.registerNetworkCallback exception");
        }
    }

    public static void unregisterWifiNetworkCallback(Context context, ConnectivityManager.NetworkCallback networkCallback) {
        ConnectivityManager connectivityManager = (ConnectivityManager) context.getSystemService("connectivity");
        try {
            Log.i(LOG_TAG, "connectivityManager.unregisterNetworkCallback");
            connectivityManager.unregisterNetworkCallback(networkCallback);
        } catch (Exception unused) {
            Log.i(LOG_TAG, "connectivityManager.unregisterNetworkCallback exception");
        }
    }
}
