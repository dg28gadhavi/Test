package com.sec.internal.ims.core;

import android.net.ConnectivityManager;
import android.net.Network;
import android.net.NetworkCapabilities;
import com.sec.internal.helper.SimUtil;
import com.sec.internal.helper.os.LinkPropertiesWrapper;
import com.sec.internal.ims.core.PdnController;
import com.sec.internal.interfaces.ims.core.PdnEventListener;
import com.sec.internal.log.IMSLog;
import java.net.InetAddress;
import java.util.List;

class NetworkCallback extends ConnectivityManager.NetworkCallback {
    static final int LOCAL_IP_CHANGED = 1;
    static final int LOCAL_STACKED_IP_CHANGED = 2;
    private static final String LOG_TAG = PdnController.class.getSimpleName();
    boolean isSuspended = false;
    boolean mDisconnectRequested = false;
    LinkPropertiesWrapper mLinkProperties = new LinkPropertiesWrapper();
    final PdnEventListener mListener;
    Network mNetwork = null;
    final int mNetworkType;
    boolean mPdnConnected = false;
    private final PdnController mPdnController;
    int mPhoneId;

    public NetworkCallback(PdnController pdnController, int i, PdnEventListener pdnEventListener, int i2) {
        this.mPdnController = pdnController;
        this.mListener = pdnEventListener;
        this.mNetworkType = i;
        this.mPhoneId = i2;
    }

    public void setDisconnectRequested() {
        this.mDisconnectRequested = true;
    }

    public boolean isDisconnectRequested() {
        return this.mDisconnectRequested;
    }

    public void onAvailable(Network network) {
        if (SimUtil.getSimMno(this.mPhoneId).isRjil() && this.mNetworkType == 15) {
            PdnController pdnController = this.mPdnController;
            if (pdnController.mIsDisconnecting && !pdnController.isNetworkRequested(this.mListener)) {
                String str = LOG_TAG;
                int i = this.mPhoneId;
                IMSLog.i(str, i, "ignore onAvailable: network " + network);
                return;
            }
        }
        this.mPdnController.obtainMessage(108, this.mNetworkType, this.mPhoneId, new PdnController.PdnConnectedEvent(this.mListener, network)).sendToTarget();
    }

    public void onLost(Network network) {
        String str = LOG_TAG;
        int i = this.mPhoneId;
        IMSLog.i(str, i, "onLost: network " + network + " " + this.mPdnController);
        this.mPdnController.obtainMessage(103, this.mNetworkType, this.mPhoneId, this.mListener).sendToTarget();
    }

    public void onLosing(Network network, int i) {
        String str = LOG_TAG;
        int i2 = this.mPhoneId;
        IMSLog.i(str, i2, "onLosing: network " + network + " maxMsToLive " + i);
    }

    /* JADX WARNING: Code restructure failed: missing block: B:12:0x0059, code lost:
        if (r2 == null) goto L_0x005f;
     */
    /* JADX WARNING: Code restructure failed: missing block: B:14:0x005d, code lost:
        if (r2.mNetwork != null) goto L_0x0068;
     */
    /* JADX WARNING: Code restructure failed: missing block: B:15:0x005f, code lost:
        android.util.Log.i(r0, "onLinkPropertiesChanged: null callback");
     */
    /* JADX WARNING: Code restructure failed: missing block: B:16:0x0064, code lost:
        return;
     */
    /* Code decompiled incorrectly, please refer to instructions dump. */
    public void onLinkPropertiesChanged(android.net.Network r5, android.net.LinkProperties r6) {
        /*
            r4 = this;
            java.lang.String r0 = LOG_TAG
            int r1 = r4.mPhoneId
            java.lang.StringBuilder r2 = new java.lang.StringBuilder
            r2.<init>()
            java.lang.String r3 = "onLinkPropertiesChanged: network "
            r2.append(r3)
            r2.append(r5)
            java.lang.String r3 = " lp "
            r2.append(r3)
            r2.append(r6)
            java.lang.String r3 = " old "
            r2.append(r3)
            com.sec.internal.helper.os.LinkPropertiesWrapper r3 = r4.mLinkProperties
            r2.append(r3)
            java.lang.String r2 = r2.toString()
            com.sec.internal.log.IMSLog.s(r0, r1, r2)
            int r1 = r4.mPhoneId
            com.sec.internal.constants.Mno r1 = com.sec.internal.helper.SimUtil.getSimMno(r1)
            boolean r1 = r1.isKor()
            if (r1 == 0) goto L_0x0068
            com.sec.internal.ims.core.PdnController r1 = r4.mPdnController
            java.util.Map<com.sec.internal.interfaces.ims.core.PdnEventListener, com.sec.internal.ims.core.NetworkCallback> r1 = r1.mNetworkCallbacks
            monitor-enter(r1)
            com.sec.internal.ims.core.PdnController r2 = r4.mPdnController     // Catch:{ all -> 0x0065 }
            java.util.Map<com.sec.internal.interfaces.ims.core.PdnEventListener, com.sec.internal.ims.core.NetworkCallback> r2 = r2.mNetworkCallbacks     // Catch:{ all -> 0x0065 }
            boolean r2 = r2.isEmpty()     // Catch:{ all -> 0x0065 }
            if (r2 == 0) goto L_0x004c
            java.lang.String r4 = "onLinkPropertiesChanged: No callback exists"
            android.util.Log.i(r0, r4)     // Catch:{ all -> 0x0065 }
            monitor-exit(r1)     // Catch:{ all -> 0x0065 }
            return
        L_0x004c:
            com.sec.internal.ims.core.PdnController r2 = r4.mPdnController     // Catch:{ all -> 0x0065 }
            java.util.Map<com.sec.internal.interfaces.ims.core.PdnEventListener, com.sec.internal.ims.core.NetworkCallback> r2 = r2.mNetworkCallbacks     // Catch:{ all -> 0x0065 }
            com.sec.internal.interfaces.ims.core.PdnEventListener r3 = r4.mListener     // Catch:{ all -> 0x0065 }
            java.lang.Object r2 = r2.get(r3)     // Catch:{ all -> 0x0065 }
            com.sec.internal.ims.core.NetworkCallback r2 = (com.sec.internal.ims.core.NetworkCallback) r2     // Catch:{ all -> 0x0065 }
            monitor-exit(r1)     // Catch:{ all -> 0x0065 }
            if (r2 == 0) goto L_0x005f
            android.net.Network r1 = r2.mNetwork
            if (r1 != 0) goto L_0x0068
        L_0x005f:
            java.lang.String r4 = "onLinkPropertiesChanged: null callback"
            android.util.Log.i(r0, r4)
            return
        L_0x0065:
            r4 = move-exception
            monitor-exit(r1)     // Catch:{ all -> 0x0065 }
            throw r4
        L_0x0068:
            com.sec.internal.constants.ims.core.LinkPropertiesChangedEvent r0 = new com.sec.internal.constants.ims.core.LinkPropertiesChangedEvent
            com.sec.internal.interfaces.ims.core.PdnEventListener r1 = r4.mListener
            r0.<init>(r5, r1, r6)
            com.sec.internal.ims.core.PdnController r5 = r4.mPdnController
            int r6 = r4.mNetworkType
            int r4 = r4.mPhoneId
            r1 = 111(0x6f, float:1.56E-43)
            android.os.Message r4 = r5.obtainMessage(r1, r6, r4, r0)
            r4.sendToTarget()
            return
        */
        throw new UnsupportedOperationException("Method not decompiled: com.sec.internal.ims.core.NetworkCallback.onLinkPropertiesChanged(android.net.Network, android.net.LinkProperties):void");
    }

    public void onCapabilitiesChanged(Network network, NetworkCapabilities networkCapabilities) {
        String str = LOG_TAG;
        int i = this.mPhoneId;
        IMSLog.i(str, i, "onCapabilitiesChanged: network " + network + " networkCapabilities " + networkCapabilities);
        if (networkCapabilities.hasCapability(21)) {
            if (this.isSuspended) {
                IMSLog.i(str, this.mPhoneId, "resume!");
                this.isSuspended = false;
                this.mListener.onResumed(this.mNetworkType);
            }
        } else if (!this.isSuspended) {
            IMSLog.i(str, this.mPhoneId, "suspend!");
            this.isSuspended = true;
            this.mListener.onSuspended(this.mNetworkType);
        }
    }

    /* access modifiers changed from: package-private */
    public int isLocalIpChanged(LinkPropertiesWrapper linkPropertiesWrapper) {
        List<InetAddress> filterAddresses = this.mPdnController.filterAddresses(this.mLinkProperties.getAddresses());
        List<InetAddress> filterAddresses2 = this.mPdnController.filterAddresses(linkPropertiesWrapper.getAddresses());
        if (filterAddresses == null || filterAddresses2 == null || (filterAddresses.isEmpty() && filterAddresses2.isEmpty())) {
            return 0;
        }
        if (!this.mLinkProperties.isIdenticalInterfaceName(linkPropertiesWrapper) || filterAddresses.size() != filterAddresses2.size() || !filterAddresses.containsAll(filterAddresses2)) {
            return 1;
        }
        List<InetAddress> filterAddresses3 = this.mPdnController.filterAddresses(this.mLinkProperties.getAllAddresses());
        List<InetAddress> filterAddresses4 = this.mPdnController.filterAddresses(linkPropertiesWrapper.getAllAddresses());
        if (filterAddresses3.size() != filterAddresses4.size() || !filterAddresses3.containsAll(filterAddresses4)) {
            return 2;
        }
        return 0;
    }

    /* access modifiers changed from: package-private */
    public boolean isPcscfAddressChanged(LinkPropertiesWrapper linkPropertiesWrapper) {
        List<InetAddress> filterAddresses = this.mPdnController.filterAddresses(this.mLinkProperties.getPcscfServers());
        List<InetAddress> filterAddresses2 = this.mPdnController.filterAddresses(linkPropertiesWrapper.getPcscfServers());
        if (filterAddresses == null || filterAddresses2 == null || filterAddresses2.isEmpty()) {
            return false;
        }
        if (!this.mLinkProperties.isIdenticalInterfaceName(linkPropertiesWrapper) || filterAddresses.size() != filterAddresses2.size() || !filterAddresses.containsAll(filterAddresses2)) {
            return true;
        }
        return false;
    }
}
