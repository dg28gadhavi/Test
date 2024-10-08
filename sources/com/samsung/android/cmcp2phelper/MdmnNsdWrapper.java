package com.samsung.android.cmcp2phelper;

import android.content.Context;
import android.net.ConnectivityManager;
import android.net.Network;
import android.os.Handler;
import android.text.TextUtils;
import android.util.Log;
import com.samsung.android.cmcp2phelper.data.CphDeviceManager;
import com.samsung.android.cmcp2phelper.transport.CphManager;
import com.samsung.android.cmcp2phelper.utils.P2pUtils;
import java.util.ArrayList;
import java.util.Collection;

public class MdmnNsdWrapper {
    public static final String LOG_TAG = ("cmcp2phelper/1.3.06/" + MdmnNsdWrapper.class.getSimpleName());
    TransportStatus curStatus = TransportStatus.IDLE;
    Context mContext;
    MdmnServiceInfo mServiceInfo;
    CphManager mTransportManager;
    private final ConnectivityManager.NetworkCallback wifiNetworkStateListener = new ConnectivityManager.NetworkCallback() {
        public void onAvailable(Network network) {
            String str = MdmnNsdWrapper.LOG_TAG;
            Log.i(str, "onWiFiAvailable:");
            if (MdmnNsdWrapper.this.curStatus == TransportStatus.STARTING) {
                Log.i(str, "The p2p transport manager is restarted");
                MdmnNsdWrapper.this.mTransportManager.stopReceive();
                MdmnNsdWrapper.this.mTransportManager.startReceive();
                MdmnNsdWrapper.this.curStatus = TransportStatus.STARTED;
            }
        }

        public void onLost(Network network) {
            String str = MdmnNsdWrapper.LOG_TAG;
            Log.i(str, "onWiFiLost:");
            if (MdmnNsdWrapper.this.curStatus == TransportStatus.STARTED) {
                Log.i(str, "The p2p transport manager is stopped");
                MdmnNsdWrapper.this.mTransportManager.stopReceive();
                MdmnNsdWrapper.this.curStatus = TransportStatus.STARTING;
            }
        }
    };

    public enum TransportStatus {
        IDLE,
        STARTING,
        STARTED
    }

    public MdmnNsdWrapper(Context context, MdmnServiceInfo mdmnServiceInfo) {
        Log.i(LOG_TAG, "cmcp2phelper version 1.3.06");
        this.mServiceInfo = mdmnServiceInfo;
        this.mContext = context;
        this.mTransportManager = new CphManager(context, this.mServiceInfo);
    }

    public Collection<MdmnServiceInfo> getSupportDevices() {
        return CphDeviceManager.getDeviceList(this.mServiceInfo.getLineId());
    }

    public void setServiceInfo(MdmnServiceInfo mdmnServiceInfo) {
        if (!isConfigurationInvalid(mdmnServiceInfo)) {
            this.mTransportManager.stopReceive();
            this.mServiceInfo = mdmnServiceInfo;
            this.mTransportManager = new CphManager(this.mContext, this.mServiceInfo);
            if (this.curStatus != TransportStatus.STARTED) {
                return;
            }
            if (P2pUtils.isWifiConnected(this.mContext)) {
                this.mTransportManager.startReceive();
            } else {
                this.curStatus = TransportStatus.STARTING;
            }
        }
    }

    private boolean isConfigurationInvalid(MdmnServiceInfo mdmnServiceInfo) {
        return TextUtils.isEmpty(mdmnServiceInfo.getDeviceId()) || TextUtils.isEmpty(mdmnServiceInfo.getLineId());
    }

    public void start() {
        P2pUtils.registerWiFiNetworkCallback(this.mContext, this.wifiNetworkStateListener);
        if (!P2pUtils.isWifiConnected(this.mContext)) {
            Log.i(LOG_TAG, "WiFi is not connected to receive discovery packet");
            this.curStatus = TransportStatus.STARTING;
            return;
        }
        this.mTransportManager.startReceive();
        this.curStatus = TransportStatus.STARTED;
    }

    public void stop() {
        P2pUtils.unregisterWifiNetworkCallback(this.mContext, this.wifiNetworkStateListener);
        this.mTransportManager.stopReceive();
        this.curStatus = TransportStatus.IDLE;
    }

    public int startDiscovery(Handler handler, int i, ArrayList<String> arrayList) {
        if (arrayList == null) {
            Log.i(LOG_TAG, "No ip list for p2p discovery");
            return 0;
        }
        String str = LOG_TAG;
        Log.i(str, "Try discovery : " + arrayList);
        CphDeviceManager.clearCache();
        CphDeviceManager.setMaxPeer(arrayList.size());
        CphDeviceManager.setCallback(handler, i);
        if (!P2pUtils.isWifiConnected(this.mContext)) {
            Log.i(str, "WiFi is not enabled for p2p discovery");
            this.mTransportManager.stopReceive();
            this.curStatus = TransportStatus.STARTING;
            return 0;
        }
        this.mTransportManager.startReceive();
        this.mTransportManager.startDiscoveryUnicast(handler, i, this.mServiceInfo.getDeviceId(), this.mServiceInfo.getLineId(), arrayList);
        return 1;
    }
}
