package com.sec.internal.ims.core;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.net.ConnectivityManager;
import android.net.LinkProperties;
import android.net.Network;
import android.net.NetworkCapabilities;
import android.net.NetworkRequest;
import android.os.Build;
import android.os.Handler;
import android.os.Looper;
import android.os.Message;
import android.os.RemoteException;
import android.os.SemSystemProperties;
import android.telephony.CellIdentity;
import android.telephony.CellInfo;
import android.text.TextUtils;
import android.util.ArrayMap;
import android.util.ArraySet;
import android.util.Log;
import android.util.Pair;
import com.sec.epdg.EpdgManager;
import com.sec.ims.ImsManager;
import com.sec.ims.extensions.ConnectivityManagerExt;
import com.sec.ims.extensions.ServiceStateExt;
import com.sec.internal.constants.Mno;
import com.sec.internal.constants.ims.ImsConstants;
import com.sec.internal.constants.ims.core.LinkPropertiesChangedEvent;
import com.sec.internal.constants.ims.os.EmcBsIndication;
import com.sec.internal.constants.ims.os.NetworkState;
import com.sec.internal.constants.ims.os.VoPsIndication;
import com.sec.internal.helper.CollectionUtils;
import com.sec.internal.helper.NetworkUtil;
import com.sec.internal.helper.SimUtil;
import com.sec.internal.helper.SimpleEventLog;
import com.sec.internal.helper.os.CellIdentityWrapper;
import com.sec.internal.helper.os.ITelephonyManager;
import com.sec.internal.helper.os.LinkPropertiesWrapper;
import com.sec.internal.helper.os.TelephonyManagerWrapper;
import com.sec.internal.ims.core.sim.SimManagerFactory;
import com.sec.internal.ims.servicemodules.ss.UtStateMachine;
import com.sec.internal.ims.settings.DeviceConfigManager;
import com.sec.internal.ims.util.SemTelephonyAdapter;
import com.sec.internal.interfaces.ims.IImsFramework;
import com.sec.internal.interfaces.ims.core.IPdnController;
import com.sec.internal.interfaces.ims.core.IRegisterTask;
import com.sec.internal.interfaces.ims.core.ISimManager;
import com.sec.internal.interfaces.ims.core.NetworkStateListener;
import com.sec.internal.interfaces.ims.core.PdnEventListener;
import com.sec.internal.log.IMSLog;
import com.sec.internal.log.IMSLogTimer;
import java.net.InetAddress;
import java.net.UnknownHostException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class PdnController extends Handler implements IPdnController {
    private static final boolean DBG = "eng".equals(Build.TYPE);
    protected static final String ECC_IWLAN = "IWLAN";
    protected static final int EVENT_CLEAR_EMERGENCY_QUALIFIEDNETWORK = 112;
    protected static final int EVENT_DEFAULT_NETWORK_CHANGED = 110;
    protected static final int EVENT_EPDG_CONNECTION_CHANGED = 104;
    protected static final int EVENT_EPDG_IKEERROR = 109;
    protected static final int EVENT_LINK_PROPERTIES_CHANGED = 111;
    protected static final int EVENT_PDN_CONNECTED = 108;
    protected static final int EVENT_PDN_DISCONNECTED = 103;
    protected static final int EVENT_REQUEST_NETWORK = 101;
    protected static final int EVENT_REQUEST_STOP_PDN = 107;
    protected static final int EVENT_STOP_PDN_COMPLETED = 102;
    protected static final int EVENT_WIFI_CONNECTED = 105;
    protected static final int EVENT_WIFI_DISCONNECTED = 106;
    /* access modifiers changed from: private */
    public static final String LOG_TAG = PdnController.class.getSimpleName();
    protected static final String PROPERTY_ECC_PATH = "ril.subtype";
    static final int TEMP_SA_DISABLE = 4;
    static final int TEMP_SA_ENABLE = 3;
    protected static Map<Integer, Integer> mDataState = new HashMap();
    protected int mActiveDataPhoneId = 0;
    protected ConnectivityManager mConnectivityManager;
    private final Context mContext;
    final ConnectivityManager.NetworkCallback mDefaultNetworkListener = new ConnectivityManager.NetworkCallback() {
        public void onAvailable(Network network) {
            String r0 = PdnController.LOG_TAG;
            Log.i(r0, "mDefaultNetworkListener: onAvailable network=" + network);
            if (!PdnController.this.hasMessages(110)) {
                PdnController.this.sendEmptyMessage(110);
            }
        }

        public void onLost(Network network) {
            String r2 = PdnController.LOG_TAG;
            Log.i(r2, "mDefaultNetworkListener: onLost network=" + network);
        }
    };
    protected int[] mEPDNQN;
    protected String[] mEPDNintfName;
    protected ImsManager.EpdgListener mEpdgHandoverListener;
    protected SimpleEventLog mEventLog;
    private final IImsFramework mImsFramework;
    protected boolean mIsDisconnecting = false;
    protected boolean mNeedCellLocationUpdate = false;
    protected final Map<PdnEventListener, NetworkCallback> mNetworkCallbacks = new ArrayMap();
    protected final Set<NetworkStateListener> mNetworkStateListeners = new ArraySet();
    protected List<NetworkState> mNetworkStates = new ArrayList();
    private final BroadcastReceiver mPcscfRestorationEventReceiver = new BroadcastReceiver() {
        private final ExecutorService mExecutor = Executors.newFixedThreadPool(1);

        public void onReceive(Context context, Intent intent) {
            String r3 = PdnController.LOG_TAG;
            IMSLog.i(r3, "onReceive:" + intent.getAction());
            this.mExecutor.execute(new PdnController$3$$ExternalSyntheticLambda0(this, intent));
        }

        /* access modifiers changed from: private */
        public /* synthetic */ void lambda$onReceive$0(Intent intent) {
            PdnController.this.handlePcscfRestorationIntent(intent);
        }
    };
    protected final Set<Pair<Pair<Integer, Integer>, PdnEventListener>> mPendingRequests = new ArraySet();
    protected List<? extends ISimManager> mSimManagers;
    protected final Map<Integer, TelephonyCallbackForPdnController> mTelephonyCallbacks = new HashMap();
    protected ITelephonyManager mTelephonyManager = null;
    final ConnectivityManager.NetworkCallback mWifiStateListener = new ConnectivityManager.NetworkCallback() {
        public void onAvailable(Network network) {
            PdnController.this.sendEmptyMessage(105);
        }

        public void onLost(Network network) {
            PdnController.this.sendEmptyMessage(106);
        }
    };

    private int getNetworkCapability(int i) {
        if (i == 11) {
            return 4;
        }
        if (i != 15) {
            return i != 27 ? 12 : 9;
        }
        return 10;
    }

    public PdnController(Context context, Looper looper, IImsFramework iImsFramework) {
        super(looper);
        this.mContext = context;
        this.mImsFramework = iImsFramework;
        this.mConnectivityManager = (ConnectivityManager) context.getSystemService("connectivity");
        this.mTelephonyManager = TelephonyManagerWrapper.getInstance(context);
        this.mSimManagers = SimManagerFactory.getAllSimManagers();
        this.mEpdgHandoverListener = new ImsEpdgEventListener(this, iImsFramework);
        int size = this.mSimManagers.size();
        this.mEPDNintfName = new String[size];
        this.mEPDNQN = new int[size];
        this.mActiveDataPhoneId = SimUtil.getActiveDataPhoneId();
        this.mEventLog = new SimpleEventLog(context, LOG_TAG, 200);
    }

    public void initSequentially() {
        for (ISimManager simSlotIndex : this.mSimManagers) {
            this.mNetworkStates.add(NetworkState.create(simSlotIndex.getSimSlotIndex()));
        }
        for (ISimManager simSlotIndex2 : this.mSimManagers) {
            registerTelephonyCallback(simSlotIndex2.getSimSlotIndex());
        }
        this.mImsFramework.getWfcEpdgManager().registerEpdgHandoverListener(this.mEpdgHandoverListener);
        this.mConnectivityManager.registerNetworkCallback(new NetworkRequest.Builder().addTransportType(1).addCapability(12).build(), this.mWifiStateListener);
        this.mConnectivityManager.registerDefaultNetworkCallback(this.mDefaultNetworkListener);
        IntentFilter intentFilter = new IntentFilter();
        intentFilter.addAction(ImsConstants.Intents.ACTION_UPDATE_PCSCF_RESTORATION);
        this.mContext.registerReceiver(this.mPcscfRestorationEventReceiver, intentFilter);
    }

    public void registerTelephonyCallback(int i) {
        int subId = SimUtil.getSubId(i);
        String str = LOG_TAG;
        IMSLog.i(str, i, "registerPhoneStateListener subId=" + subId);
        if (subId >= 0) {
            TelephonyCallbackForPdnController telephonyCallbackForPdnController = this.mTelephonyCallbacks.get(Integer.valueOf(i));
            if (telephonyCallbackForPdnController != null) {
                IMSLog.i(str, i, "registerPhoneStateListener: callback exits subId:" + telephonyCallbackForPdnController.getSubId());
                if (telephonyCallbackForPdnController.getSubId() != subId) {
                    unRegisterTelephonyCallback(i);
                } else {
                    return;
                }
            }
            TelephonyCallbackForPdnController telephonyCallbackForPdnController2 = new TelephonyCallbackForPdnController(this, this.mImsFramework, i, subId);
            this.mTelephonyCallbacks.put(Integer.valueOf(i), telephonyCallbackForPdnController2);
            this.mTelephonyManager.registerTelephonyCallbackForSlot(i, this.mContext.getMainExecutor(), telephonyCallbackForPdnController2);
        }
    }

    public void unRegisterTelephonyCallback(int i) {
        IMSLog.i(LOG_TAG, i, "unRegisterTelephonyCallback:");
        TelephonyCallbackForPdnController telephonyCallbackForPdnController = this.mTelephonyCallbacks.get(Integer.valueOf(i));
        if (telephonyCallbackForPdnController != null) {
            this.mTelephonyManager.unregisterTelephonyCallbackForSlot(i, telephonyCallbackForPdnController);
            this.mTelephonyCallbacks.remove(Integer.valueOf(i));
        }
    }

    public boolean isPendedEPDGWeakSignal(int i) {
        return getNetworkState(i).isPendedEPDGWeakSignal();
    }

    public void setPendedEPDGWeakSignal(int i, boolean z) {
        String str = LOG_TAG;
        IMSLog.i(str, i, "setPendedEPDGWeakSignal");
        NetworkState networkState = getNetworkState(i);
        if (!z) {
            networkState.setPendedEpdgWeakSignal(false);
        } else if (!SimUtil.getSimMno(i).isOneOf(Mno.TMOUS, Mno.DISH, Mno.VZW, Mno.ATT)) {
        } else {
            if (networkState == null) {
                IMSLog.i(str, i, "setPendedEPDGWeakSignal, networkState is not exist.");
                return;
            }
            int dataRegState = networkState.getDataRegState();
            int dataNetworkType = networkState.getDataNetworkType();
            if (dataRegState == 1 || dataRegState == 3 || !(dataNetworkType == 13 || dataNetworkType == 14 || dataRegState != 0)) {
                IMSLog.i(str, i, "VzW/ATT/TMOUS/DISH : LOST_LTE_WIFI_CONNECTION:12");
                networkState.setPendedEpdgWeakSignal(true);
            }
        }
    }

    public boolean isEpsOnlyReg(int i) {
        NetworkState networkState = getNetworkState(i);
        return networkState.isPsOnlyReg() && NetworkUtil.is3gppPsVoiceNetwork(networkState.getDataNetworkType());
    }

    public boolean hasEmergencyServiceOnly(int i) {
        return this.mTelephonyManager.getDataServiceState(SimUtil.getSubId(i)) != 0;
    }

    public VoPsIndication getVopsIndication(int i) {
        return getNetworkState(i).getVopsIndication();
    }

    public boolean isVoiceRoaming(int i) {
        return getNetworkState(i).isVoiceRoaming();
    }

    public boolean isDataRoaming(int i) {
        return getNetworkState(i).isDataRoaming();
    }

    public int getVoiceRegState(int i) {
        return getNetworkState(i).getVoiceRegState();
    }

    public int getEpdgPhysicalInterface(int i) {
        try {
            return getNetworkState(i).getEpdgPhysicalInterface();
        } catch (NullPointerException unused) {
            IMSLog.i(LOG_TAG, i, "Network State is NULL");
            return 0;
        }
    }

    public int getMobileDataRegState(int i) {
        return getNetworkState(i).getMobileDataRegState();
    }

    public boolean isPsOnlyReg(int i) {
        return getNetworkState(i).isPsOnlyReg();
    }

    public EmcBsIndication getEmcBsIndication(int i) {
        return getNetworkState(i).getEmcBsIndication();
    }

    public List<CellInfo> getAllCellInfo(int i, boolean z) {
        List<CellInfo> allCellInfo = getNetworkState(i).getAllCellInfo();
        String str = LOG_TAG;
        IMSLog.i(str, i, "getAllCellInfo mNeedCellLocationUpdate : " + this.mNeedCellLocationUpdate);
        int subId = SimUtil.getSubId(i);
        if (allCellInfo != null && !allCellInfo.isEmpty() && !z && !this.mNeedCellLocationUpdate && subId != -1) {
            return allCellInfo;
        }
        List<CellInfo> allCellInfoBySubId = this.mTelephonyManager.getAllCellInfoBySubId(subId);
        IMSLog.i(str, i, "get latest cellInfo and store, subId = " + subId);
        getNetworkState(i).setAllCellInfo(allCellInfoBySubId);
        List<CellInfo> allCellInfo2 = getNetworkState(i).getAllCellInfo();
        this.mNeedCellLocationUpdate = false;
        return allCellInfo2;
    }

    public CellIdentity getCellIdentity(int i, boolean z) {
        CellIdentity cellIdentity;
        if (z) {
            cellIdentity = SemTelephonyAdapter.getCellIdentityFromSemTelephony(i, this.mContext.getOpPackageName(), this.mContext.getAttributionTag());
        } else {
            cellIdentity = getNetworkState(i).getCellIdentity();
            if (cellIdentity == null) {
                IMSLog.i(LOG_TAG, i, "reget cid from ril since null restored value");
                cellIdentity = SemTelephonyAdapter.getCellIdentityFromSemTelephony(i, this.mContext.getOpPackageName(), this.mContext.getAttributionTag());
            }
        }
        getNetworkState(i).setCellIdentity(cellIdentity);
        return cellIdentity;
    }

    public CellIdentityWrapper getCurrentCellIdentity(int i, int i2) {
        if (SimUtil.getMno(i).isChn()) {
            return CellIdentityWrapper.from(getCellIdentity(i, false));
        }
        List<CellInfo> allCellInfo = getAllCellInfo(i, false);
        if (allCellInfo != null) {
            return (CellIdentityWrapper) allCellInfo.stream().map(new PdnController$$ExternalSyntheticLambda0()).map(new PdnController$$ExternalSyntheticLambda1()).filter(new PdnController$$ExternalSyntheticLambda2(i2)).findFirst().orElse(CellIdentityWrapper.DEFAULT);
        }
        IMSLog.e(LOG_TAG, i, "getCurrentCellIdentity: getAllCellInfo null");
        return CellIdentityWrapper.DEFAULT;
    }

    public boolean isInternationalRoaming(int i) {
        return getNetworkState(i).isInternationalRoaming();
    }

    public boolean isNeedCellLocationUpdate() {
        return this.mNeedCellLocationUpdate;
    }

    public NetworkState getNetworkState(int i) {
        for (NetworkState next : this.mNetworkStates) {
            if (next.getSimSlot() == i) {
                return next;
            }
        }
        IMSLog.e(LOG_TAG, i, "NetworkState is not exist. Return default NetworkState.");
        return NetworkState.create(i);
    }

    public void resetNetworkState(int i) {
        NetworkState networkState = getNetworkState(i);
        if (networkState != null) {
            networkState.setDataNetworkType(0);
            networkState.setMobileDataNetworkType(0);
            networkState.setDataRegState(1);
            networkState.setVoiceRegState(1);
            networkState.setMobileDataRegState(1);
            networkState.setSnapshotState(ServiceStateExt.SNAPSHOT_STATUS_DEACTIVATED);
            networkState.setAllCellInfo((List<CellInfo>) null);
            networkState.setCellIdentity((CellIdentity) null);
        }
    }

    /* access modifiers changed from: protected */
    public List<String> readPcscfFromLinkProperties(LinkPropertiesWrapper linkPropertiesWrapper) {
        String str = LOG_TAG;
        StringBuilder sb = new StringBuilder();
        sb.append("readPcscfFromLinkProperties: lp=");
        sb.append(linkPropertiesWrapper == null ? "null" : "not null");
        Log.i(str, sb.toString());
        ArrayList arrayList = new ArrayList();
        if (linkPropertiesWrapper == null) {
            return arrayList;
        }
        List<InetAddress> pcscfServers = linkPropertiesWrapper.getPcscfServers();
        if (!CollectionUtils.isNullOrEmpty((Collection<?>) pcscfServers)) {
            for (InetAddress hostAddress : pcscfServers) {
                String hostAddress2 = hostAddress.getHostAddress();
                if (!TextUtils.isEmpty(hostAddress2) && !"0.0.0.0".equals(hostAddress2) && !"0:0:0:0:0:0:0:0".equals(hostAddress2) && !"::".equals(hostAddress2)) {
                    String str2 = LOG_TAG;
                    Log.i(str2, "readPcscfFromLinkProperties: Valid pcscf: " + hostAddress2);
                    arrayList.add(hostAddress2);
                }
            }
        }
        return arrayList;
    }

    public void handleMessage(Message message) {
        String str = LOG_TAG;
        Log.i(str, "handleMessage: what " + message.what);
        switch (message.what) {
            case 101:
                requestNetwork(message.arg1, message.arg2, (PdnEventListener) message.obj);
                return;
            case 102:
                onStopPdnCompleted();
                return;
            case 103:
                onPdnDisconnected(message.arg1, message.arg2, (PdnEventListener) message.obj);
                return;
            case 104:
                int i = message.arg1;
                String str2 = (String) message.obj;
                boolean z = true;
                if (message.arg2 != 1) {
                    z = false;
                }
                onEpdgConnected(i, str2, z);
                return;
            case 105:
                onWifiConnected();
                return;
            case 106:
                onWifiDisconnected();
                return;
            case 107:
                requestStopNetwork(message.arg1, message.arg2, (PdnEventListener) message.obj);
                return;
            case 108:
                PdnConnectedEvent pdnConnectedEvent = (PdnConnectedEvent) message.obj;
                onPdnConnected(message.arg1, message.arg2, pdnConnectedEvent.mListener, pdnConnectedEvent.mNetwork);
                return;
            case 109:
                onEpdgIkeError(message.arg1);
                return;
            case 110:
                onDefaultNetworkChanged();
                return;
            case 111:
                LinkPropertiesChangedEvent linkPropertiesChangedEvent = (LinkPropertiesChangedEvent) message.obj;
                onLinkPropertiesChanged(message.arg1, linkPropertiesChangedEvent.getNetwork(), linkPropertiesChangedEvent.getListener(), linkPropertiesChangedEvent.getLinkProperties());
                return;
            case 112:
                applyEmergencyQualifiedNetowrk(message.arg1);
                return;
            default:
                return;
        }
    }

    public void registerForNetworkState(NetworkStateListener networkStateListener) {
        this.mNetworkStateListeners.add(networkStateListener);
    }

    public void unregisterForNetworkState(NetworkStateListener networkStateListener) {
        this.mNetworkStateListeners.remove(networkStateListener);
    }

    public int startPdnConnectivity(int i, PdnEventListener pdnEventListener, int i2) {
        String str = LOG_TAG;
        IMSLog.i(str, i2, "startPdnConnectivity: networkType " + i);
        sendMessage(obtainMessage(101, i, i2, pdnEventListener));
        return 1;
    }

    /* access modifiers changed from: package-private */
    /* JADX WARNING: Code restructure failed: missing block: B:24:0x008d, code lost:
        return;
     */
    /* JADX WARNING: Code restructure failed: missing block: B:26:0x008f, code lost:
        r0 = false;
     */
    /* JADX WARNING: Code restructure failed: missing block: B:27:0x0091, code lost:
        if (r8 != 1) goto L_0x0095;
     */
    /* JADX WARNING: Code restructure failed: missing block: B:28:0x0093, code lost:
        r2 = 1;
     */
    /* JADX WARNING: Code restructure failed: missing block: B:29:0x0095, code lost:
        r2 = 0;
     */
    /* JADX WARNING: Code restructure failed: missing block: B:30:0x0096, code lost:
        r3 = getNetworkCapability(r8);
     */
    /* JADX WARNING: Code restructure failed: missing block: B:31:0x00a2, code lost:
        if (com.sec.internal.helper.SimUtil.getSimMno(r9).isKor() == false) goto L_0x00ab;
     */
    /* JADX WARNING: Code restructure failed: missing block: B:33:0x00a8, code lost:
        if (needRequestMobileNetwork(r8, r9) == false) goto L_0x00ab;
     */
    /* JADX WARNING: Code restructure failed: missing block: B:34:0x00aa, code lost:
        r0 = true;
     */
    /* JADX WARNING: Code restructure failed: missing block: B:35:0x00ab, code lost:
        if (r0 == false) goto L_0x00af;
     */
    /* JADX WARNING: Code restructure failed: missing block: B:36:0x00ad, code lost:
        r3 = 12;
     */
    /* JADX WARNING: Code restructure failed: missing block: B:37:0x00af, code lost:
        com.sec.internal.log.IMSLog.i(LOG_TAG, r9, "startPdnConnectivity: transport " + r2 + " capability " + r3 + " needRequestMobileNetwork " + r0);
        r4 = new android.net.NetworkRequest.Builder();
        r4.addTransportType(r2).addCapability(r3);
     */
    /* JADX WARNING: Code restructure failed: missing block: B:38:0x00e2, code lost:
        if (r2 != 0) goto L_0x0100;
     */
    /* JADX WARNING: Code restructure failed: missing block: B:39:0x00e4, code lost:
        r2 = com.sec.internal.helper.SimUtil.getSubId(r9);
     */
    /* JADX WARNING: Code restructure failed: missing block: B:40:0x00e8, code lost:
        if (r2 <= 0) goto L_0x0100;
     */
    /* JADX WARNING: Code restructure failed: missing block: B:42:0x00ee, code lost:
        if (com.sec.internal.helper.SimUtil.isDualIMS() == false) goto L_0x0100;
     */
    /* JADX WARNING: Code restructure failed: missing block: B:43:0x00f0, code lost:
        r4.setNetworkSpecifier(new android.net.TelephonyNetworkSpecifier.Builder().setSubscriptionId(r2).build());
     */
    /* JADX WARNING: Code restructure failed: missing block: B:44:0x0100, code lost:
        r2 = r4.build();
        r3 = new com.sec.internal.ims.core.NetworkCallback(r7, r8, r10, r9);
        r7.mNetworkCallbacks.put(r10, r3);
     */
    /* JADX WARNING: Code restructure failed: missing block: B:45:0x0110, code lost:
        if (r8 != 15) goto L_0x0118;
     */
    /* JADX WARNING: Code restructure failed: missing block: B:47:?, code lost:
        applyEmergencyQualifiedNetowrk(r9);
     */
    /* JADX WARNING: Code restructure failed: missing block: B:48:0x0116, code lost:
        r8 = move-exception;
     */
    /* JADX WARNING: Code restructure failed: missing block: B:50:0x0118, code lost:
        if (r8 == 1) goto L_0x011c;
     */
    /* JADX WARNING: Code restructure failed: missing block: B:51:0x011a, code lost:
        if (r8 != 0) goto L_0x011e;
     */
    /* JADX WARNING: Code restructure failed: missing block: B:52:0x011c, code lost:
        if (r0 == false) goto L_0x0124;
     */
    /* JADX WARNING: Code restructure failed: missing block: B:53:0x011e, code lost:
        r7.mConnectivityManager.requestNetwork(r2, r3);
     */
    /* JADX WARNING: Code restructure failed: missing block: B:54:0x0124, code lost:
        r7.mConnectivityManager.registerNetworkCallback(r2, r3);
     */
    /* JADX WARNING: Code restructure failed: missing block: B:55:0x012a, code lost:
        android.util.Log.e(LOG_TAG, r8.toString());
        r10.onNetworkRequestFail();
        r7.mNetworkCallbacks.remove(r10);
     */
    /* JADX WARNING: Code restructure failed: missing block: B:65:?, code lost:
        return;
     */
    /* JADX WARNING: Code restructure failed: missing block: B:66:?, code lost:
        return;
     */
    /* JADX WARNING: Code restructure failed: missing block: B:67:?, code lost:
        return;
     */
    /* Code decompiled incorrectly, please refer to instructions dump. */
    public void requestNetwork(int r8, int r9, com.sec.internal.interfaces.ims.core.PdnEventListener r10) {
        /*
            r7 = this;
            java.util.Map<com.sec.internal.interfaces.ims.core.PdnEventListener, com.sec.internal.ims.core.NetworkCallback> r0 = r7.mNetworkCallbacks
            java.lang.Object r0 = r0.get(r10)
            com.sec.internal.ims.core.NetworkCallback r0 = (com.sec.internal.ims.core.NetworkCallback) r0
            com.sec.internal.helper.SimpleEventLog r1 = r7.mEventLog
            java.lang.StringBuilder r2 = new java.lang.StringBuilder
            r2.<init>()
            java.lang.String r3 = "requestNetwork: networkType "
            r2.append(r3)
            r2.append(r8)
            java.lang.String r3 = ", callback="
            r2.append(r3)
            if (r0 != 0) goto L_0x0022
            java.lang.String r3 = "null"
            goto L_0x0028
        L_0x0022:
            int r3 = r0.mNetworkType
            java.lang.Integer r3 = java.lang.Integer.valueOf(r3)
        L_0x0028:
            r2.append(r3)
            java.lang.String r2 = r2.toString()
            r1.logAndAdd(r9, r2)
            if (r0 == 0) goto L_0x0058
            int r1 = r0.mNetworkType
            if (r1 == r8) goto L_0x0049
            android.net.ConnectivityManager r1 = r7.mConnectivityManager     // Catch:{ IllegalArgumentException -> 0x003e }
            r1.unregisterNetworkCallback(r0)     // Catch:{ IllegalArgumentException -> 0x003e }
            goto L_0x0058
        L_0x003e:
            r0 = move-exception
            java.lang.String r1 = LOG_TAG
            java.lang.String r0 = r0.getMessage()
            android.util.Log.e(r1, r0)
            goto L_0x0058
        L_0x0049:
            boolean r9 = r7.isConnected(r8, r10)
            if (r9 == 0) goto L_0x0057
            com.sec.internal.ims.core.PdnController$$ExternalSyntheticLambda9 r9 = new com.sec.internal.ims.core.PdnController$$ExternalSyntheticLambda9
            r9.<init>(r10, r8, r0)
            r7.post(r9)
        L_0x0057:
            return
        L_0x0058:
            java.util.Set<android.util.Pair<android.util.Pair<java.lang.Integer, java.lang.Integer>, com.sec.internal.interfaces.ims.core.PdnEventListener>> r0 = r7.mPendingRequests
            monitor-enter(r0)
            boolean r1 = r7.mIsDisconnecting     // Catch:{ all -> 0x013c }
            if (r1 == 0) goto L_0x008e
            java.lang.String r1 = LOG_TAG     // Catch:{ all -> 0x013c }
            java.lang.String r2 = "Wait until ongoing stop request done."
            android.util.Log.i(r1, r2)     // Catch:{ all -> 0x013c }
            java.util.Set<android.util.Pair<android.util.Pair<java.lang.Integer, java.lang.Integer>, com.sec.internal.interfaces.ims.core.PdnEventListener>> r2 = r7.mPendingRequests     // Catch:{ all -> 0x013c }
            java.lang.Integer r9 = java.lang.Integer.valueOf(r9)     // Catch:{ all -> 0x013c }
            java.lang.Integer r8 = java.lang.Integer.valueOf(r8)     // Catch:{ all -> 0x013c }
            android.util.Pair r8 = android.util.Pair.create(r9, r8)     // Catch:{ all -> 0x013c }
            android.util.Pair r8 = android.util.Pair.create(r8, r10)     // Catch:{ all -> 0x013c }
            r2.add(r8)     // Catch:{ all -> 0x013c }
            r8 = 102(0x66, float:1.43E-43)
            boolean r9 = r7.hasMessages(r8)     // Catch:{ all -> 0x013c }
            if (r9 != 0) goto L_0x008c
            java.lang.String r9 = "requestNetwork: Unexpected event missing case. Send EVENT_STOP_PDN_COMPLETED again"
            android.util.Log.i(r1, r9)     // Catch:{ all -> 0x013c }
            r7.sendEmptyMessage(r8)     // Catch:{ all -> 0x013c }
        L_0x008c:
            monitor-exit(r0)     // Catch:{ all -> 0x013c }
            return
        L_0x008e:
            monitor-exit(r0)     // Catch:{ all -> 0x013c }
            r0 = 0
            r1 = 1
            if (r8 != r1) goto L_0x0095
            r2 = r1
            goto L_0x0096
        L_0x0095:
            r2 = r0
        L_0x0096:
            int r3 = r7.getNetworkCapability(r8)
            com.sec.internal.constants.Mno r4 = com.sec.internal.helper.SimUtil.getSimMno(r9)
            boolean r4 = r4.isKor()
            if (r4 == 0) goto L_0x00ab
            boolean r4 = r7.needRequestMobileNetwork(r8, r9)
            if (r4 == 0) goto L_0x00ab
            r0 = r1
        L_0x00ab:
            if (r0 == 0) goto L_0x00af
            r3 = 12
        L_0x00af:
            java.lang.String r4 = LOG_TAG
            java.lang.StringBuilder r5 = new java.lang.StringBuilder
            r5.<init>()
            java.lang.String r6 = "startPdnConnectivity: transport "
            r5.append(r6)
            r5.append(r2)
            java.lang.String r6 = " capability "
            r5.append(r6)
            r5.append(r3)
            java.lang.String r6 = " needRequestMobileNetwork "
            r5.append(r6)
            r5.append(r0)
            java.lang.String r5 = r5.toString()
            com.sec.internal.log.IMSLog.i(r4, r9, r5)
            android.net.NetworkRequest$Builder r4 = new android.net.NetworkRequest$Builder
            r4.<init>()
            android.net.NetworkRequest$Builder r5 = r4.addTransportType(r2)
            r5.addCapability(r3)
            if (r2 != 0) goto L_0x0100
            int r2 = com.sec.internal.helper.SimUtil.getSubId(r9)
            if (r2 <= 0) goto L_0x0100
            boolean r3 = com.sec.internal.helper.SimUtil.isDualIMS()
            if (r3 == 0) goto L_0x0100
            android.net.TelephonyNetworkSpecifier$Builder r3 = new android.net.TelephonyNetworkSpecifier$Builder
            r3.<init>()
            android.net.TelephonyNetworkSpecifier$Builder r2 = r3.setSubscriptionId(r2)
            android.net.TelephonyNetworkSpecifier r2 = r2.build()
            r4.setNetworkSpecifier(r2)
        L_0x0100:
            android.net.NetworkRequest r2 = r4.build()
            com.sec.internal.ims.core.NetworkCallback r3 = new com.sec.internal.ims.core.NetworkCallback
            r3.<init>(r7, r8, r10, r9)
            java.util.Map<com.sec.internal.interfaces.ims.core.PdnEventListener, com.sec.internal.ims.core.NetworkCallback> r4 = r7.mNetworkCallbacks
            r4.put(r10, r3)
            r4 = 15
            if (r8 != r4) goto L_0x0118
            r7.applyEmergencyQualifiedNetowrk(r9)     // Catch:{ IllegalArgumentException -> 0x0116 }
            goto L_0x0118
        L_0x0116:
            r8 = move-exception
            goto L_0x012a
        L_0x0118:
            if (r8 == r1) goto L_0x011c
            if (r8 != 0) goto L_0x011e
        L_0x011c:
            if (r0 == 0) goto L_0x0124
        L_0x011e:
            android.net.ConnectivityManager r8 = r7.mConnectivityManager     // Catch:{ IllegalArgumentException -> 0x0116 }
            r8.requestNetwork(r2, r3)     // Catch:{ IllegalArgumentException -> 0x0116 }
            goto L_0x013b
        L_0x0124:
            android.net.ConnectivityManager r8 = r7.mConnectivityManager     // Catch:{ IllegalArgumentException -> 0x0116 }
            r8.registerNetworkCallback(r2, r3)     // Catch:{ IllegalArgumentException -> 0x0116 }
            goto L_0x013b
        L_0x012a:
            java.lang.String r9 = LOG_TAG
            java.lang.String r8 = r8.toString()
            android.util.Log.e(r9, r8)
            r10.onNetworkRequestFail()
            java.util.Map<com.sec.internal.interfaces.ims.core.PdnEventListener, com.sec.internal.ims.core.NetworkCallback> r7 = r7.mNetworkCallbacks
            r7.remove(r10)
        L_0x013b:
            return
        L_0x013c:
            r7 = move-exception
            monitor-exit(r0)     // Catch:{ all -> 0x013c }
            throw r7
        */
        throw new UnsupportedOperationException("Method not decompiled: com.sec.internal.ims.core.PdnController.requestNetwork(int, int, com.sec.internal.interfaces.ims.core.PdnEventListener):void");
    }

    private void requestStopNetwork(int i, int i2, PdnEventListener pdnEventListener) {
        if (i == 15) {
            this.mEPDNintfName[i2] = null;
        }
        synchronized (this.mPendingRequests) {
            this.mPendingRequests.remove(Pair.create(Pair.create(Integer.valueOf(i2), Integer.valueOf(i)), pdnEventListener));
        }
        synchronized (this.mNetworkCallbacks) {
            ConnectivityManager.NetworkCallback networkCallback = this.mNetworkCallbacks.get(pdnEventListener);
            SimpleEventLog simpleEventLog = this.mEventLog;
            StringBuilder sb = new StringBuilder();
            sb.append("requestStopNetwork: network ");
            sb.append(i);
            sb.append(", callback is ");
            sb.append(networkCallback != null ? "exist" : "null");
            simpleEventLog.logAndAdd(i2, sb.toString());
            if (networkCallback != null) {
                pdnEventListener.onResumed(i);
                pdnEventListener.onResumedBySnapshot(i);
                try {
                    this.mConnectivityManager.unregisterNetworkCallback(networkCallback);
                } catch (IllegalArgumentException e) {
                    Log.e(LOG_TAG, e.toString());
                }
                this.mNetworkCallbacks.remove(pdnEventListener);
                this.mIsDisconnecting = true;
                removeMessages(102);
                sendMessageDelayed(obtainMessage(102), SimUtil.getSimMno(i2).isKor() && needRequestMobileNetwork(i, i2) ? UtStateMachine.HTTP_READ_TIMEOUT_GCF : 1000);
            }
        }
        NetworkState networkState = getNetworkState(i2);
        if (i == 11 && networkState.isEpdgConnected()) {
            networkState.setEpdgConnected(false);
            if (networkState.getDataNetworkType() != 18) {
                notifyDataConnectionState(networkState.getDataNetworkType(), networkState.getDataRegState(), true, i2);
            }
        } else if (i == 15) {
            if (networkState.isEmergencyEpdgConnected()) {
                networkState.setEmergencyEpdgConnected(false);
            }
            this.mEPDNQN[i2] = 0;
            applyEmergencyQualifiedNetowrk(i2);
        }
    }

    private void onStopPdnCompleted() {
        synchronized (this.mPendingRequests) {
            this.mIsDisconnecting = false;
            for (Pair next : this.mPendingRequests) {
                requestNetwork(((Integer) ((Pair) next.first).second).intValue(), ((Integer) ((Pair) next.first).first).intValue(), (PdnEventListener) next.second);
            }
            this.mPendingRequests.clear();
        }
    }

    private boolean needRequestMobileNetwork(int i, int i2) {
        int translateNetworkBearer = translateNetworkBearer(getDefaultNetworkBearer());
        int dataRegState = getNetworkState(i2).getDataRegState();
        if (isDataRoaming(i2) || !NetworkUtil.isMobileDataOn(this.mContext) || !NetworkUtil.isMobileDataPressed(this.mContext) || dataRegState == 1 || ImsConstants.SystemSettings.AIRPLANE_MODE.get(this.mContext, 0) == ImsConstants.SystemSettings.AIRPLANE_MODE_ON || i != 0 || translateNetworkBearer != 1) {
            return false;
        }
        return true;
    }

    /* access modifiers changed from: package-private */
    public void onPdnConnected(int i, int i2, PdnEventListener pdnEventListener, Network network) {
        NetworkCallback networkCallback;
        if (i == 11) {
            IMSLogTimer.setPdnEndTime(i2);
            IMSLog.lazer((IRegisterTask) (RegisterTask) pdnEventListener, "PDN SETUP TIME : " + (((double) (IMSLogTimer.getPdnEndTime(i2) - IMSLogTimer.getPdnStartTime(i2))) / 1000.0d) + "s");
        }
        synchronized (this.mNetworkCallbacks) {
            networkCallback = this.mNetworkCallbacks.get(pdnEventListener);
        }
        if (networkCallback == null) {
            SimpleEventLog simpleEventLog = this.mEventLog;
            simpleEventLog.logAndAdd("ignore onPdnConnected: network " + network + " as requestStopNetwork preceded this");
            return;
        }
        LinkProperties linkProperties = this.mConnectivityManager.getLinkProperties(network);
        if (linkProperties == null || linkProperties.getInterfaceName() == null) {
            IMSLog.i(LOG_TAG, i2, "onPdnConnected: linkProperties or interface name is null, wait for next onPdnConnected()");
            return;
        }
        SimpleEventLog simpleEventLog2 = this.mEventLog;
        simpleEventLog2.logAndAdd("onPdnConnected: network=" + network + ", " + linkProperties.getInterfaceName() + ", " + toString());
        networkCallback.mNetwork = network;
        boolean z = true;
        networkCallback.mPdnConnected = true;
        LinkPropertiesWrapper linkPropertiesWrapper = new LinkPropertiesWrapper(linkProperties);
        String str = LOG_TAG;
        IMSLog.i(str, i2, "onPdnConnected: link properties " + linkPropertiesWrapper);
        handleConnectedPdnType(i, i2, networkCallback, linkPropertiesWrapper.getInterfaceName());
        if (networkCallback.mLinkProperties.getInterfaceName() != null) {
            int isLocalIpChanged = networkCallback.isLocalIpChanged(linkPropertiesWrapper);
            if (isLocalIpChanged >= 1) {
                if (isLocalIpChanged != 2) {
                    z = false;
                }
                pdnEventListener.onLocalIpChanged(i, network, z);
            }
            if (networkCallback.isPcscfAddressChanged(linkPropertiesWrapper)) {
                pdnEventListener.onPcscfAddressChanged(i, network, readPcscfFromLinkProperties(linkPropertiesWrapper));
            }
            networkCallback.mLinkProperties = linkPropertiesWrapper;
            return;
        }
        networkCallback.mLinkProperties = linkPropertiesWrapper;
        networkCallback.mListener.onConnected(i, network);
    }

    private void handleConnectedPdnType(int i, int i2, NetworkCallback networkCallback, String str) {
        NetworkState networkState = getNetworkState(i2);
        if (networkState == null) {
            IMSLog.e(LOG_TAG, i2, "onPdnConnected: NetworkState is null!");
        } else if (i == 11 && networkCallback.mLinkProperties.getInterfaceName() == null) {
            if (networkState.isEpdgConnected()) {
                this.mEventLog.logAndAdd(i2, "onPdnConnected: epdg network for ims pdn");
                synchronized (this.mNetworkStateListeners) {
                    for (NetworkStateListener next : this.mNetworkStateListeners) {
                        next.onDataConnectionStateChanged(networkState.getDataNetworkType(), true, i2);
                        next.onEpdgConnected(i2);
                    }
                }
            }
        } else if (i == 15) {
            String str2 = SemSystemProperties.get(PROPERTY_ECC_PATH, "");
            SimpleEventLog simpleEventLog = this.mEventLog;
            simpleEventLog.logAndAdd("eccPath : " + str2);
            if (str2.equalsIgnoreCase(ECC_IWLAN)) {
                networkState.setEmergencyEpdgConnected(true);
            }
            this.mEPDNintfName[i2] = str;
            String str3 = LOG_TAG;
            IMSLog.i(str3, i2, "handleConnectedPdnType: eccPath=" + str2 + "mEPDNintfName : " + this.mEPDNintfName[i2]);
        }
    }

    private void onPdnDisconnected(int i, int i2, PdnEventListener pdnEventListener) {
        SimpleEventLog simpleEventLog = this.mEventLog;
        simpleEventLog.logAndAdd("onPdnDisconnected: networkType " + i);
        NetworkState networkState = getNetworkState(i2);
        pdnEventListener.onResumed(i);
        if (i == 11 && networkState.isEpdgConnected()) {
            networkState.setEpdgConnected(false);
            notifyDataConnectionState(networkState.getDataNetworkType(), networkState.getDataRegState(), true, i2);
        } else if (i == 15 && networkState.isEmergencyEpdgConnected()) {
            networkState.setEmergencyEpdgConnected(false);
        }
        if (i == 15) {
            this.mEPDNintfName[i2] = null;
        }
        synchronized (this.mNetworkCallbacks) {
            if (this.mNetworkCallbacks.containsKey(pdnEventListener)) {
                pdnEventListener.onDisconnected(i);
                NetworkCallback networkCallback = this.mNetworkCallbacks.get(pdnEventListener);
                networkCallback.mLinkProperties = new LinkPropertiesWrapper();
                networkCallback.mNetwork = null;
                networkCallback.mPdnConnected = false;
            }
        }
    }

    private void onLinkPropertiesChanged(int i, Network network, PdnEventListener pdnEventListener, LinkProperties linkProperties) {
        NetworkCallback networkCallback;
        synchronized (this.mNetworkCallbacks) {
            networkCallback = this.mNetworkCallbacks.get(pdnEventListener);
        }
        if (networkCallback == null) {
            this.mEventLog.logAndAdd("ignore onLinkPropertiesChanged as requestStopNetwork preceded this");
            return;
        }
        SimpleEventLog simpleEventLog = this.mEventLog;
        simpleEventLog.logAndAdd("onLinkPropertiesChanged: networkType=" + i);
        String str = LOG_TAG;
        IMSLog.s(str, "onLinkPropertiesChanged: linkProperties=" + linkProperties);
        LinkPropertiesWrapper linkPropertiesWrapper = new LinkPropertiesWrapper(linkProperties);
        int isLocalIpChanged = networkCallback.isLocalIpChanged(linkPropertiesWrapper);
        boolean isPcscfAddressChanged = networkCallback.isPcscfAddressChanged(linkPropertiesWrapper);
        boolean z = true;
        if (isLocalIpChanged >= 1 || isPcscfAddressChanged) {
            networkCallback.mLinkProperties = linkPropertiesWrapper;
            if (isLocalIpChanged >= 1) {
                SimpleEventLog simpleEventLog2 = this.mEventLog;
                simpleEventLog2.logAndAdd("onLinkPropertiesChanged: LinkProperties changed type=" + isLocalIpChanged + " call onLocalIpChanged");
                if (isLocalIpChanged != 2) {
                    z = false;
                }
                pdnEventListener.onLocalIpChanged(i, network, z);
            }
            if (isPcscfAddressChanged) {
                this.mEventLog.logAndAdd("onLinkPropertiesChanged: LinkProperties changed call onPcscfAddressChanged");
                pdnEventListener.onPcscfAddressChanged(i, network, readPcscfFromLinkProperties(linkPropertiesWrapper));
            }
        }
    }

    private void onEpdgIkeError(int i) {
        synchronized (this.mNetworkStateListeners) {
            for (NetworkStateListener onIKEAuthFAilure : this.mNetworkStateListeners) {
                onIKEAuthFAilure.onIKEAuthFAilure(i);
            }
        }
    }

    private void onEpdgConnected(int i, String str, boolean z) {
        boolean z2;
        NetworkState networkState = getNetworkState(i);
        if (TextUtils.equals(str, "emergency") && networkState != null) {
            IMSLog.i(LOG_TAG, i, "EpdgEvent onEpdgConnected: emergency  connected=" + z + " mIsEmergencyEpdgConnected=" + networkState.isEmergencyEpdgConnected());
            networkState.setEmergencyEpdgConnected(z);
        } else if (TextUtils.equals(str, DeviceConfigManager.IMS) && networkState != null) {
            IMSLog.i(LOG_TAG, i, "EpdgEvent onEpdgConnected: apnType=" + str + " connected=" + z + " mIsEpdgConnected=" + networkState.isEpdgConnected());
            Iterator<NetworkCallback> it = this.mNetworkCallbacks.values().iterator();
            while (true) {
                if (it.hasNext()) {
                    if (it.next().mNetworkType == 11) {
                        z2 = true;
                        break;
                    }
                } else {
                    z2 = false;
                    break;
                }
            }
            IMSLog.i(LOG_TAG, i, "onEpdgConnected: existCallBack=" + z2 + " connected=" + z + " dataRat=" + networkState.getDataNetworkType() + " mobileDataRat=" + networkState.getMobileDataNetworkType() + " voiceRat =" + networkState.getVoiceNetworkType());
            if (!z2) {
                networkState.setEpdgConnected(false);
            } else if (z) {
                if (!networkState.isEpdgConnected()) {
                    networkState.setEpdgConnected(true);
                    synchronized (this.mNetworkStateListeners) {
                        for (NetworkStateListener next : this.mNetworkStateListeners) {
                            next.onDataConnectionStateChanged(18, true, i);
                            next.onEpdgConnected(i);
                        }
                    }
                }
            } else if (networkState.isEpdgConnected()) {
                networkState.setEpdgConnected(false);
                int dataNetworkType = networkState.getDataNetworkType();
                if (dataNetworkType == 18) {
                    dataNetworkType = networkState.getMobileDataNetworkType();
                }
                synchronized (this.mNetworkStateListeners) {
                    for (NetworkStateListener next2 : this.mNetworkStateListeners) {
                        next2.onDataConnectionStateChanged(dataNetworkType, isWifiConnected(), i);
                        next2.onEpdgDisconnected(i);
                    }
                }
            }
        }
    }

    private void onWifiConnected() {
        Log.i(LOG_TAG, "onWifiConnected:");
        synchronized (this.mNetworkStateListeners) {
            if (SimUtil.isDualIMS()) {
                for (NetworkStateListener next : this.mNetworkStateListeners) {
                    for (ISimManager iSimManager : this.mSimManagers) {
                        NetworkState networkState = getNetworkState(iSimManager.getSimSlotIndex());
                        if (networkState != null) {
                            next.onDataConnectionStateChanged(networkState.getDataNetworkType(), true, iSimManager.getSimSlotIndex());
                        }
                    }
                }
            } else {
                for (NetworkStateListener next2 : this.mNetworkStateListeners) {
                    int activeDataPhoneId = SimUtil.getActiveDataPhoneId();
                    NetworkState networkState2 = getNetworkState(activeDataPhoneId);
                    if (networkState2 != null) {
                        next2.onDataConnectionStateChanged(networkState2.getDataNetworkType(), true, activeDataPhoneId);
                    }
                }
            }
        }
        for (ISimManager iSimManager2 : this.mSimManagers) {
            try {
                if (iSimManager2.isSimAvailable() && iSimManager2.getSimMno() == Mno.ZAIN_KUWAIT && this.mImsFramework.isServiceAvailable("mmtel", 20, iSimManager2.getSimSlotIndex()) && this.mTelephonyManager.getCallState(iSimManager2.getSimSlotIndex()) == 0) {
                    this.mTelephonyManager.semSetNrMode(iSimManager2.getSimSlotIndex(), 4);
                }
            } catch (RemoteException e) {
                e.printStackTrace();
            }
        }
    }

    private void onWifiDisconnected() {
        Log.i(LOG_TAG, "onWifiDisConnected:");
        synchronized (this.mNetworkStateListeners) {
            if (SimUtil.isDualIMS()) {
                for (NetworkStateListener next : this.mNetworkStateListeners) {
                    for (ISimManager iSimManager : this.mSimManagers) {
                        NetworkState networkState = getNetworkState(iSimManager.getSimSlotIndex());
                        if (networkState != null) {
                            next.onDataConnectionStateChanged(networkState.getDataNetworkType(), false, iSimManager.getSimSlotIndex());
                        }
                    }
                }
            } else {
                for (NetworkStateListener next2 : this.mNetworkStateListeners) {
                    int activeDataPhoneId = SimUtil.getActiveDataPhoneId();
                    NetworkState networkState2 = getNetworkState(activeDataPhoneId);
                    if (networkState2 != null) {
                        next2.onDataConnectionStateChanged(networkState2.getDataNetworkType(), false, activeDataPhoneId);
                    }
                }
            }
        }
        for (ISimManager iSimManager2 : this.mSimManagers) {
            try {
                if (iSimManager2.isSimAvailable() && iSimManager2.getSimMno() == Mno.ZAIN_KUWAIT && this.mImsFramework.isServiceAvailable("mmtel", 20, iSimManager2.getSimSlotIndex()) && this.mTelephonyManager.getCallState(iSimManager2.getSimSlotIndex()) == 0) {
                    this.mTelephonyManager.semSetNrMode(iSimManager2.getSimSlotIndex(), 3);
                }
            } catch (RemoteException e) {
                e.printStackTrace();
            }
        }
    }

    private void onDefaultNetworkChanged() {
        Log.i(LOG_TAG, "onDefaultNetworkChanged:");
        synchronized (this.mNetworkStateListeners) {
            if (SimUtil.isDualIMS()) {
                for (NetworkStateListener next : this.mNetworkStateListeners) {
                    for (ISimManager simSlotIndex : this.mSimManagers) {
                        next.onDefaultNetworkStateChanged(simSlotIndex.getSimSlotIndex());
                    }
                }
            } else {
                for (NetworkStateListener onDefaultNetworkStateChanged : this.mNetworkStateListeners) {
                    onDefaultNetworkStateChanged.onDefaultNetworkStateChanged(SimUtil.getActiveDataPhoneId());
                }
            }
        }
    }

    public int stopPdnConnectivity(int i, PdnEventListener pdnEventListener) {
        return stopPdnConnectivity(i, this.mActiveDataPhoneId, pdnEventListener);
    }

    /* JADX WARNING: Code restructure failed: missing block: B:15:0x004d, code lost:
        sendMessage(obtainMessage(107, r6, r7, r8));
     */
    /* JADX WARNING: Code restructure failed: missing block: B:16:0x0057, code lost:
        return 1;
     */
    /* Code decompiled incorrectly, please refer to instructions dump. */
    public int stopPdnConnectivity(int r6, int r7, com.sec.internal.interfaces.ims.core.PdnEventListener r8) {
        /*
            r5 = this;
            java.util.Map<com.sec.internal.interfaces.ims.core.PdnEventListener, com.sec.internal.ims.core.NetworkCallback> r0 = r5.mNetworkCallbacks
            monitor-enter(r0)
            java.util.Map<com.sec.internal.interfaces.ims.core.PdnEventListener, com.sec.internal.ims.core.NetworkCallback> r1 = r5.mNetworkCallbacks     // Catch:{ all -> 0x0058 }
            java.lang.Object r1 = r1.get(r8)     // Catch:{ all -> 0x0058 }
            com.sec.internal.ims.core.NetworkCallback r1 = (com.sec.internal.ims.core.NetworkCallback) r1     // Catch:{ all -> 0x0058 }
            com.sec.internal.helper.SimpleEventLog r2 = r5.mEventLog     // Catch:{ all -> 0x0058 }
            java.lang.StringBuilder r3 = new java.lang.StringBuilder     // Catch:{ all -> 0x0058 }
            r3.<init>()     // Catch:{ all -> 0x0058 }
            java.lang.String r4 = "stopPdnConnectivity: network "
            r3.append(r4)     // Catch:{ all -> 0x0058 }
            r3.append(r6)     // Catch:{ all -> 0x0058 }
            java.lang.String r4 = ", callback is "
            r3.append(r4)     // Catch:{ all -> 0x0058 }
            if (r1 == 0) goto L_0x0025
            java.lang.String r4 = "exist"
            goto L_0x0027
        L_0x0025:
            java.lang.String r4 = "null"
        L_0x0027:
            r3.append(r4)     // Catch:{ all -> 0x0058 }
            java.lang.String r3 = r3.toString()     // Catch:{ all -> 0x0058 }
            r2.logAndAdd(r7, r3)     // Catch:{ all -> 0x0058 }
            if (r1 == 0) goto L_0x0037
            r1.setDisconnectRequested()     // Catch:{ all -> 0x0058 }
            goto L_0x004c
        L_0x0037:
            java.lang.String r1 = LOG_TAG     // Catch:{ all -> 0x0058 }
            java.lang.String r2 = "requestStopNetwork: callback not found"
            com.sec.internal.log.IMSLog.e(r1, r7, r2)     // Catch:{ all -> 0x0058 }
            com.sec.internal.constants.Mno r1 = com.sec.internal.helper.SimUtil.getSimMno(r7)     // Catch:{ all -> 0x0058 }
            boolean r1 = r1.isKor()     // Catch:{ all -> 0x0058 }
            if (r1 == 0) goto L_0x004c
            monitor-exit(r0)     // Catch:{ all -> 0x0058 }
            r5 = 2
            return r5
        L_0x004c:
            monitor-exit(r0)     // Catch:{ all -> 0x0058 }
            r0 = 107(0x6b, float:1.5E-43)
            android.os.Message r6 = r5.obtainMessage(r0, r6, r7, r8)
            r5.sendMessage(r6)
            r5 = 1
            return r5
        L_0x0058:
            r5 = move-exception
            monitor-exit(r0)     // Catch:{ all -> 0x0058 }
            throw r5
        */
        throw new UnsupportedOperationException("Method not decompiled: com.sec.internal.ims.core.PdnController.stopPdnConnectivity(int, int, com.sec.internal.interfaces.ims.core.PdnEventListener):int");
    }

    public List<InetAddress> filterAddresses(Iterable<InetAddress> iterable) {
        ArrayList arrayList = new ArrayList();
        if (iterable != null) {
            for (InetAddress next : iterable) {
                boolean z = DBG;
                if (z) {
                    String str = LOG_TAG;
                    Log.i(str, "getIpAddressList: inetAddress: " + next);
                }
                if (next != null && !next.isAnyLocalAddress() && !next.isLinkLocalAddress() && !next.isLoopbackAddress() && !next.isMulticastAddress()) {
                    if (z) {
                        String str2 = LOG_TAG;
                        Log.i(str2, "getIpAddressList: inetAddress IP: " + next.getHostAddress());
                    }
                    if (NetworkUtil.isIPv4Address(next.getHostAddress()) || NetworkUtil.isIPv6Address(next.getHostAddress())) {
                        arrayList.add(next);
                    }
                }
            }
        }
        return arrayList;
    }

    private InetAddress determineIpAddress(String str) {
        if (str == null || str.length() == 0) {
            Log.e(LOG_TAG, "determineIpAddress: empty address.");
            return null;
        }
        try {
            return InetAddress.getByName(str);
        } catch (UnknownHostException unused) {
            String str2 = LOG_TAG;
            Log.e(str2, "determineIpAddress: invalid address -  " + str);
            return null;
        }
    }

    public boolean requestRouteToHostAddress(int i, String str) {
        InetAddress determineIpAddress = determineIpAddress(str);
        boolean requestRouteToHostAddress = determineIpAddress != null ? ConnectivityManagerExt.requestRouteToHostAddress(this.mConnectivityManager, i, determineIpAddress) : false;
        String str2 = LOG_TAG;
        Log.i(str2, "requestRouteToHostAddress: hostAddress=" + str + " networkType=" + i + " address=" + IMSLog.checker(determineIpAddress) + " result : " + requestRouteToHostAddress);
        return requestRouteToHostAddress;
    }

    public boolean removeRouteToHostAddress(int i, String str) {
        String str2 = LOG_TAG;
        Log.i(str2, "removeRouteToHostAddress: hostAddress " + str + " networkType " + i);
        InetAddress determineIpAddress = determineIpAddress(str);
        if (determineIpAddress != null) {
            return ConnectivityManagerExt.removeRouteToHostAddress(this.mConnectivityManager, i, determineIpAddress);
        }
        return false;
    }

    /* JADX WARNING: Code restructure failed: missing block: B:15:0x0032, code lost:
        if (r6 == null) goto L_0x0096;
     */
    /* JADX WARNING: Code restructure failed: missing block: B:17:0x0036, code lost:
        if (r6.mNetwork == null) goto L_0x0096;
     */
    /* JADX WARNING: Code restructure failed: missing block: B:19:0x003a, code lost:
        if (r6.mNetworkType == r5) goto L_0x003d;
     */
    /* JADX WARNING: Code restructure failed: missing block: B:21:0x0041, code lost:
        if (r6.isDisconnectRequested() == false) goto L_0x005f;
     */
    /* JADX WARNING: Code restructure failed: missing block: B:22:0x0043, code lost:
        r4.mEventLog.logAndAdd("isConnected: Disconnect msg is in queue for networkType [" + r5 + "]");
     */
    /* JADX WARNING: Code restructure failed: missing block: B:23:0x005e, code lost:
        return false;
     */
    /* JADX WARNING: Code restructure failed: missing block: B:24:0x005f, code lost:
        if (r5 == 0) goto L_0x0067;
     */
    /* JADX WARNING: Code restructure failed: missing block: B:25:0x0061, code lost:
        if (r5 != 1) goto L_0x0064;
     */
    /* JADX WARNING: Code restructure failed: missing block: B:26:0x0064, code lost:
        r4 = r6.mPdnConnected;
     */
    /* JADX WARNING: Code restructure failed: missing block: B:27:0x0067, code lost:
        r4 = !android.text.TextUtils.isEmpty(r6.mLinkProperties.getInterfaceName());
     */
    /* JADX WARNING: Code restructure failed: missing block: B:28:0x0072, code lost:
        android.util.Log.i(LOG_TAG, "isConnected:  [" + r4 + "] networktype [" + r5 + "]");
     */
    /* JADX WARNING: Code restructure failed: missing block: B:29:0x0095, code lost:
        return r4;
     */
    /* JADX WARNING: Code restructure failed: missing block: B:30:0x0096, code lost:
        return false;
     */
    /* Code decompiled incorrectly, please refer to instructions dump. */
    public boolean isConnected(int r5, com.sec.internal.interfaces.ims.core.PdnEventListener r6) {
        /*
            r4 = this;
            r0 = 15
            r1 = 1
            r2 = 0
            if (r5 != r0) goto L_0x0015
            com.sec.internal.ims.core.RegisterTask r6 = (com.sec.internal.ims.core.RegisterTask) r6
            int r5 = r6.getPhoneId()
            java.lang.String[] r4 = r4.mEPDNintfName
            r4 = r4[r5]
            if (r4 == 0) goto L_0x0013
            goto L_0x0014
        L_0x0013:
            r1 = r2
        L_0x0014:
            return r1
        L_0x0015:
            java.util.Map<com.sec.internal.interfaces.ims.core.PdnEventListener, com.sec.internal.ims.core.NetworkCallback> r0 = r4.mNetworkCallbacks
            monitor-enter(r0)
            java.util.Map<com.sec.internal.interfaces.ims.core.PdnEventListener, com.sec.internal.ims.core.NetworkCallback> r3 = r4.mNetworkCallbacks     // Catch:{ all -> 0x0097 }
            boolean r3 = r3.isEmpty()     // Catch:{ all -> 0x0097 }
            if (r3 == 0) goto L_0x0029
            java.lang.String r4 = LOG_TAG     // Catch:{ all -> 0x0097 }
            java.lang.String r5 = "isConnected: No callback exists"
            android.util.Log.i(r4, r5)     // Catch:{ all -> 0x0097 }
            monitor-exit(r0)     // Catch:{ all -> 0x0097 }
            return r2
        L_0x0029:
            java.util.Map<com.sec.internal.interfaces.ims.core.PdnEventListener, com.sec.internal.ims.core.NetworkCallback> r3 = r4.mNetworkCallbacks     // Catch:{ all -> 0x0097 }
            java.lang.Object r6 = r3.get(r6)     // Catch:{ all -> 0x0097 }
            com.sec.internal.ims.core.NetworkCallback r6 = (com.sec.internal.ims.core.NetworkCallback) r6     // Catch:{ all -> 0x0097 }
            monitor-exit(r0)     // Catch:{ all -> 0x0097 }
            if (r6 == 0) goto L_0x0096
            android.net.Network r0 = r6.mNetwork
            if (r0 == 0) goto L_0x0096
            int r0 = r6.mNetworkType
            if (r0 == r5) goto L_0x003d
            goto L_0x0096
        L_0x003d:
            boolean r0 = r6.isDisconnectRequested()
            if (r0 == 0) goto L_0x005f
            com.sec.internal.helper.SimpleEventLog r4 = r4.mEventLog
            java.lang.StringBuilder r6 = new java.lang.StringBuilder
            r6.<init>()
            java.lang.String r0 = "isConnected: Disconnect msg is in queue for networkType ["
            r6.append(r0)
            r6.append(r5)
            java.lang.String r5 = "]"
            r6.append(r5)
            java.lang.String r5 = r6.toString()
            r4.logAndAdd(r5)
            return r2
        L_0x005f:
            if (r5 == 0) goto L_0x0067
            if (r5 != r1) goto L_0x0064
            goto L_0x0067
        L_0x0064:
            boolean r4 = r6.mPdnConnected
            goto L_0x0072
        L_0x0067:
            com.sec.internal.helper.os.LinkPropertiesWrapper r4 = r6.mLinkProperties
            java.lang.String r4 = r4.getInterfaceName()
            boolean r4 = android.text.TextUtils.isEmpty(r4)
            r4 = r4 ^ r1
        L_0x0072:
            java.lang.String r6 = LOG_TAG
            java.lang.StringBuilder r0 = new java.lang.StringBuilder
            r0.<init>()
            java.lang.String r1 = "isConnected:  ["
            r0.append(r1)
            r0.append(r4)
            java.lang.String r1 = "] networktype ["
            r0.append(r1)
            r0.append(r5)
            java.lang.String r5 = "]"
            r0.append(r5)
            java.lang.String r5 = r0.toString()
            android.util.Log.i(r6, r5)
            return r4
        L_0x0096:
            return r2
        L_0x0097:
            r4 = move-exception
            monitor-exit(r0)     // Catch:{ all -> 0x0097 }
            throw r4
        */
        throw new UnsupportedOperationException("Method not decompiled: com.sec.internal.ims.core.PdnController.isConnected(int, com.sec.internal.interfaces.ims.core.PdnEventListener):boolean");
    }

    public LinkPropertiesWrapper getLinkProperties(PdnEventListener pdnEventListener) {
        NetworkCallback networkCallback = this.mNetworkCallbacks.get(pdnEventListener);
        if (networkCallback != null) {
            return networkCallback.mLinkProperties;
        }
        return null;
    }

    public boolean isEpdgConnected(int i) {
        return ((Boolean) Optional.ofNullable(getNetworkState(i)).map(new PdnController$$ExternalSyntheticLambda8()).orElse(Boolean.FALSE)).booleanValue();
    }

    public boolean isEpdgAvailable(int i) {
        return ((Boolean) Optional.ofNullable(getNetworkState(i)).map(new PdnController$$ExternalSyntheticLambda7()).orElse(Boolean.FALSE)).booleanValue();
    }

    public boolean isEmergencyEpdgConnected(int i) {
        return ((Boolean) Optional.ofNullable(getNetworkState(i)).map(new PdnController$$ExternalSyntheticLambda6()).orElse(Boolean.FALSE)).booleanValue();
    }

    public boolean isWifiConnected() {
        Network activeNetwork = this.mConnectivityManager.getActiveNetwork();
        if (activeNetwork == null) {
            Log.i(LOG_TAG, "isWifiConnected: Default NW is null ");
            return false;
        }
        NetworkCapabilities networkCapabilities = this.mConnectivityManager.getNetworkCapabilities(activeNetwork);
        if (networkCapabilities == null || !networkCapabilities.hasTransport(1)) {
            return false;
        }
        if (networkCapabilities.hasCapability(12) || networkCapabilities.hasCapability(4)) {
            return true;
        }
        return false;
    }

    /* JADX WARNING: Code restructure failed: missing block: B:12:0x001a, code lost:
        return null;
     */
    /* Code decompiled incorrectly, please refer to instructions dump. */
    public java.lang.String getInterfaceName(com.sec.internal.interfaces.ims.core.PdnEventListener r2) {
        /*
            r1 = this;
            java.util.Map<com.sec.internal.interfaces.ims.core.PdnEventListener, com.sec.internal.ims.core.NetworkCallback> r0 = r1.mNetworkCallbacks
            monitor-enter(r0)
            java.util.Map<com.sec.internal.interfaces.ims.core.PdnEventListener, com.sec.internal.ims.core.NetworkCallback> r1 = r1.mNetworkCallbacks     // Catch:{ all -> 0x001c }
            java.lang.Object r1 = r1.get(r2)     // Catch:{ all -> 0x001c }
            com.sec.internal.ims.core.NetworkCallback r1 = (com.sec.internal.ims.core.NetworkCallback) r1     // Catch:{ all -> 0x001c }
            if (r1 == 0) goto L_0x0019
            com.sec.internal.helper.os.LinkPropertiesWrapper r1 = r1.mLinkProperties     // Catch:{ all -> 0x001c }
            if (r1 == 0) goto L_0x0019
            java.lang.String r1 = r1.getInterfaceName()     // Catch:{ all -> 0x001c }
            if (r1 == 0) goto L_0x0019
            monitor-exit(r0)     // Catch:{ all -> 0x001c }
            return r1
        L_0x0019:
            monitor-exit(r0)     // Catch:{ all -> 0x001c }
            r1 = 0
            return r1
        L_0x001c:
            r1 = move-exception
            monitor-exit(r0)     // Catch:{ all -> 0x001c }
            throw r1
        */
        throw new UnsupportedOperationException("Method not decompiled: com.sec.internal.ims.core.PdnController.getInterfaceName(com.sec.internal.interfaces.ims.core.PdnEventListener):java.lang.String");
    }

    /* JADX WARNING: Code restructure failed: missing block: B:11:0x001c, code lost:
        return null;
     */
    /* Code decompiled incorrectly, please refer to instructions dump. */
    public java.util.List<java.lang.String> getDnsServers(com.sec.internal.interfaces.ims.core.PdnEventListener r3) {
        /*
            r2 = this;
            java.util.Map<com.sec.internal.interfaces.ims.core.PdnEventListener, com.sec.internal.ims.core.NetworkCallback> r0 = r2.mNetworkCallbacks
            monitor-enter(r0)
            java.util.Map<com.sec.internal.interfaces.ims.core.PdnEventListener, com.sec.internal.ims.core.NetworkCallback> r1 = r2.mNetworkCallbacks     // Catch:{ all -> 0x001e }
            java.lang.Object r3 = r1.get(r3)     // Catch:{ all -> 0x001e }
            com.sec.internal.ims.core.NetworkCallback r3 = (com.sec.internal.ims.core.NetworkCallback) r3     // Catch:{ all -> 0x001e }
            if (r3 == 0) goto L_0x001b
            com.sec.internal.helper.os.LinkPropertiesWrapper r3 = r3.mLinkProperties     // Catch:{ all -> 0x001e }
            if (r3 == 0) goto L_0x001b
            android.net.LinkProperties r3 = r3.getLinkProperties()     // Catch:{ all -> 0x001e }
            java.util.List r2 = r2.getDnsServers((android.net.LinkProperties) r3)     // Catch:{ all -> 0x001e }
            monitor-exit(r0)     // Catch:{ all -> 0x001e }
            return r2
        L_0x001b:
            monitor-exit(r0)     // Catch:{ all -> 0x001e }
            r2 = 0
            return r2
        L_0x001e:
            r2 = move-exception
            monitor-exit(r0)     // Catch:{ all -> 0x001e }
            throw r2
        */
        throw new UnsupportedOperationException("Method not decompiled: com.sec.internal.ims.core.PdnController.getDnsServers(com.sec.internal.interfaces.ims.core.PdnEventListener):java.util.List");
    }

    public List<String> getDnsServersByNetType() {
        ConnectivityManager connectivityManager = this.mConnectivityManager;
        return getDnsServers(connectivityManager.getLinkProperties(connectivityManager.getActiveNetwork()));
    }

    private List<String> getDnsServers(LinkProperties linkProperties) {
        if (linkProperties == null) {
            return null;
        }
        List<InetAddress> dnsServers = linkProperties.getDnsServers();
        ArrayList arrayList = new ArrayList();
        ArrayList arrayList2 = new ArrayList();
        ArrayList arrayList3 = new ArrayList();
        for (InetAddress next : dnsServers) {
            if (NetworkUtil.isIPv4Address(next.getHostAddress())) {
                arrayList2.add(next.getHostAddress());
            } else if (NetworkUtil.isIPv6Address(next.getHostAddress())) {
                arrayList3.add(next.getHostAddress());
            }
        }
        arrayList.addAll(arrayList3);
        arrayList.addAll(arrayList2);
        return arrayList;
    }

    public String getIntfNameByNetType() {
        return getIntfNameByNetType(this.mConnectivityManager.getActiveNetwork());
    }

    public String getIntfNameByNetType(Network network) {
        LinkProperties linkProperties = this.mConnectivityManager.getLinkProperties(network);
        if (linkProperties != null) {
            return linkProperties.getInterfaceName();
        }
        return null;
    }

    public boolean isNetworkAvailable(int i, int i2, int i3) {
        if (i2 == 15 || i2 == -1) {
            return true;
        }
        NetworkState networkState = getNetworkState(i3);
        String str = LOG_TAG;
        IMSLog.i(str, i3, "isNetworkAvailable: isEpdgConnected=" + networkState.isEpdgConnected() + " getDataNetworkType()=" + networkState.getDataNetworkType());
        if (i != 18 || i2 == 1) {
            if (ImsConstants.SystemSettings.AIRPLANE_MODE.get(this.mContext, 0) != ImsConstants.SystemSettings.AIRPLANE_MODE_ON) {
                return true;
            }
            return false;
        } else if (networkState.isEpdgConnected() || networkState.getDataNetworkType() == 18) {
            return true;
        } else {
            return false;
        }
    }

    public boolean isNetworkRequested(PdnEventListener pdnEventListener) {
        return this.mNetworkCallbacks.containsKey(pdnEventListener);
    }

    /* access modifiers changed from: package-private */
    public void notifyDataConnectionState(int i, int i2, boolean z, int i3) {
        String str = LOG_TAG;
        IMSLog.i(str, i3, "notifyDataConnectionState");
        NetworkState networkState = getNetworkState(i3);
        if (networkState != null) {
            if (NetworkUtil.is3gppPsVoiceNetwork(i)) {
                IMSLog.i(str, i3, "initialize PendedEPDGWeakSignal flag");
                setPendedEPDGWeakSignal(i3, false);
            }
            IMSLog.i(str, i3, "notifyDataConnectionState: needNotify=" + z + " networkType=" + i + " isEpdgConnected=" + networkState.isEpdgConnected() + " dataNetType=" + networkState.getDataNetworkType() + "=>" + i + " dataRegState=" + networkState.getDataRegState() + "=>" + i2);
            if (networkState.isEpdgConnected() && i != 18 && !SimUtil.getSimMno(i3).isOneOf(Mno.TMOUS, Mno.DISH)) {
                networkState.setDataNetworkType(i);
                networkState.setDataRegState(i2);
            }
            if (z || i != networkState.getDataNetworkType() || i2 != networkState.getDataRegState()) {
                networkState.setDataNetworkType(i);
                networkState.setDataRegState(i2);
                synchronized (this.mNetworkStateListeners) {
                    for (NetworkStateListener onDataConnectionStateChanged : this.mNetworkStateListeners) {
                        onDataConnectionStateChanged.onDataConnectionStateChanged(networkState.getDataNetworkType(), isWifiConnected(), i3);
                    }
                }
            }
        }
    }

    public boolean isDisconnecting() {
        return this.mIsDisconnecting;
    }

    public String toString() {
        StringBuilder sb = new StringBuilder();
        for (NetworkState next : this.mNetworkStates) {
            sb.append(" phoneId: " + next.getSimSlot());
            sb.append(" mIsEpdgConnected: " + next.isEpdgConnected());
            sb.append(" isWifiConnected: " + isWifiConnected());
            sb.append(" mVopsIndication: " + next.getVopsIndication());
            sb.append(" mDataRoaming:  " + next.isDataRoaming());
            sb.append(" mDataConnectionState: " + next.isDataConnectedState());
            sb.append(" mVoiceRoaming: " + next.isVoiceRoaming());
            sb.append(" mEmergencyOnly: " + next.isEmergencyOnly());
            sb.append(" mIsDisconnecting: " + this.mIsDisconnecting);
            sb.append(" mPendedEPDGWeakSignal: " + next.isPendedEPDGWeakSignal());
            sb.append(" mEmcbsIndication: " + next.getEmcBsIndication());
        }
        return sb.toString();
    }

    public void dump() {
        String str = LOG_TAG;
        IMSLog.dump(str, "Dump of " + getClass().getSimpleName());
        IMSLog.increaseIndent(str);
        IMSLog.dump(str, "State: " + toString());
        IMSLog.dump(str, "History of PdnController:");
        IMSLog.increaseIndent(str);
        this.mEventLog.dump();
        IMSLog.decreaseIndent(str);
        IMSLog.decreaseIndent(str);
    }

    /* JADX WARNING: Code restructure failed: missing block: B:5:0x001b, code lost:
        if (r3.hasCapability(12) != false) goto L_0x001f;
     */
    /* Code decompiled incorrectly, please refer to instructions dump. */
    public int getDefaultNetworkBearer() {
        /*
            r3 = this;
            android.net.ConnectivityManager r0 = r3.mConnectivityManager
            android.net.Network r0 = r0.getActiveNetwork()
            android.net.ConnectivityManager r3 = r3.mConnectivityManager
            android.net.NetworkCapabilities r3 = r3.getNetworkCapabilities(r0)
            if (r3 == 0) goto L_0x001e
            r0 = 1
            boolean r1 = r3.hasTransport(r0)
            if (r1 == 0) goto L_0x001e
            r1 = 12
            boolean r3 = r3.hasCapability(r1)
            if (r3 == 0) goto L_0x001e
            goto L_0x001f
        L_0x001e:
            r0 = 0
        L_0x001f:
            java.lang.String r3 = LOG_TAG
            java.lang.StringBuilder r1 = new java.lang.StringBuilder
            r1.<init>()
            java.lang.String r2 = "getDefaultNetworkBearer: "
            r1.append(r2)
            if (r0 != 0) goto L_0x0030
            java.lang.String r2 = "CELLULAR"
            goto L_0x0032
        L_0x0030:
            java.lang.String r2 = " WIFI"
        L_0x0032:
            r1.append(r2)
            java.lang.String r1 = r1.toString()
            android.util.Log.i(r3, r1)
            return r0
        */
        throw new UnsupportedOperationException("Method not decompiled: com.sec.internal.ims.core.PdnController.getDefaultNetworkBearer():int");
    }

    public int translateNetworkBearer(int i) {
        if (i == 0) {
            return 0;
        }
        if (1 == i) {
            return 1;
        }
        String str = LOG_TAG;
        Log.i(str, "Invalid bearer: " + i);
        return -1;
    }

    public int getDataState(int i) {
        if (mDataState.containsKey(Integer.valueOf(i))) {
            return mDataState.get(Integer.valueOf(i)).intValue();
        }
        return -1;
    }

    public void setDataState(int i, int i2) {
        mDataState.put(Integer.valueOf(i), Integer.valueOf(i2));
    }

    public void setEmergencyQualifiedNetowrk(int i, int i2) {
        this.mEPDNQN[i] = i2;
    }

    /* access modifiers changed from: protected */
    public void applyEmergencyQualifiedNetowrk(int i) {
        removeMessages(112);
        EpdgManager epdgMgr = this.mImsFramework.getWfcEpdgManager().getEpdgMgr();
        if (epdgMgr != null) {
            String str = LOG_TAG;
            IMSLog.i(str, i, "setEmergencyRat: set ePDN QN to " + this.mEPDNQN[i]);
            epdgMgr.setEmergencyQualifiedNetwork(i, this.mEPDNQN[i]);
            try {
                Thread.sleep(200);
            } catch (InterruptedException e) {
                String str2 = LOG_TAG;
                Log.e(str2, "Sleep exception : " + e);
            }
        } else {
            IMSLog.i(LOG_TAG, i, "setEmergencyRat: em is null");
        }
    }

    protected static class PdnConnectedEvent {
        /* access modifiers changed from: private */
        public PdnEventListener mListener;
        /* access modifiers changed from: private */
        public Network mNetwork;

        public PdnConnectedEvent(PdnEventListener pdnEventListener, Network network) {
            this.mListener = pdnEventListener;
            this.mNetwork = network;
        }
    }

    /* access modifiers changed from: protected */
    public void handlePcscfRestorationIntent(Intent intent) {
        String str = "";
        if (intent.hasExtra(ImsConstants.Intents.EXTRA_PCSCF_RESTORATION_V4)) {
            str = str + intent.getStringExtra(ImsConstants.Intents.EXTRA_PCSCF_RESTORATION_V4);
        }
        if (intent.hasExtra(ImsConstants.Intents.EXTRA_PCSCF_RESTORATION_V6)) {
            if (!TextUtils.isEmpty(str)) {
                str = str + ",";
            }
            str = str + intent.getStringExtra(ImsConstants.Intents.EXTRA_PCSCF_RESTORATION_V6);
        }
        int intExtra = intent.getIntExtra(ImsConstants.Intents.EXTRA_PCSCF_RESTORATION_PHONEID, 0);
        List asList = Arrays.asList(str.split(","));
        String str2 = LOG_TAG;
        IMSLog.i(str2, "phoneId=" + intExtra + ", pcscfList for restoration=" + asList);
        if (asList.isEmpty()) {
            IMSLog.i(str2, "invalid pcscf restoration intent");
        } else {
            this.mNetworkCallbacks.entrySet().stream().filter(new PdnController$$ExternalSyntheticLambda3(intExtra)).map(new PdnController$$ExternalSyntheticLambda4()).findFirst().ifPresent(new PdnController$$ExternalSyntheticLambda5(asList));
        }
    }

    /* access modifiers changed from: private */
    public static /* synthetic */ boolean lambda$handlePcscfRestorationIntent$2(int i, Map.Entry entry) {
        return ((NetworkCallback) entry.getValue()).mNetworkType == 11 && ((NetworkCallback) entry.getValue()).mPhoneId == i;
    }
}
