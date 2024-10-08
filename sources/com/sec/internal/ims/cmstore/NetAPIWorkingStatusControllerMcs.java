package com.sec.internal.ims.cmstore;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.net.ConnectivityManager;
import android.net.LinkAddress;
import android.net.LinkProperties;
import android.net.Network;
import android.net.NetworkRequest;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.os.Message;
import android.provider.Settings;
import android.provider.Telephony;
import android.text.TextUtils;
import android.util.Log;
import android.widget.Toast;
import com.sec.ims.ICentralMsgStoreServiceListener;
import com.sec.internal.constants.ims.ImsConstants;
import com.sec.internal.constants.ims.cmstore.McsConstants;
import com.sec.internal.constants.ims.entitilement.NSDSContractExt;
import com.sec.internal.helper.AsyncResult;
import com.sec.internal.helper.os.IntentUtil;
import com.sec.internal.ims.cmstore.helper.EventLogHelper;
import com.sec.internal.ims.cmstore.params.ParamOMAresponseforBufDB;
import com.sec.internal.ims.cmstore.receiver.McsFcmEventListenerReceiver;
import com.sec.internal.ims.cmstore.utils.CmsUtil;
import com.sec.internal.ims.fcm.receiver.McsFcmIntentService;
import com.sec.internal.imscr.LogClass;
import com.sec.internal.interfaces.ims.cmstore.IHttpAPICommonInterface;
import com.sec.internal.interfaces.ims.cmstore.IUIEventCallback;
import com.sec.internal.interfaces.ims.cmstore.IWorkingStatusProvisionListener;
import com.sec.internal.log.IMSLog;
import java.net.Inet4Address;
import java.net.Inet6Address;
import java.net.InetAddress;
import java.util.List;

public class NetAPIWorkingStatusControllerMcs extends NetAPIWorkingStatusController {
    protected static final int EVENT_CELLULAR_CONNECTION_CHANGED = 20;
    protected static final int EVENT_WIFI_CONNECTION_CHANGED = 19;
    private static final String FCM_REGISTRATION_TOKEN = "fcmRegistrationToken";
    private static final String FCM_REGISTRATION_TOKEN_REFRESHED = "fcmRegistrationTokenRefreshed";
    private static final String INTENT_RECEIVE_FCM_PUSH_NOTIFICATION = "com.sec.internal.ims.fcm.action.RECEIVE_FCM_PUSH_NOTIFICATION";
    private static final String INTENT_RECEIVE_FCM_REGISTRATION_TOKEN = "com.sec.internal.ims.fcm.action.RECEIVE_FCM_REGISTRATION_TOKEN";
    private static final String INTENT_REFRESH_FCM_REGISTRATION_TOKEN = "com.sec.internal.ims.fcm.action.REFRESH_FCM_REGISTRATION_TOKEN";
    private static final String INTENT_RESET_BUFFERDB_MCS = "com.sec.internal.ims.cmstore.mcs.action.RESET_BUFFERDB_MCS";
    private static final String PHONE_ID = "phoneId";
    private static final String SENDER_ID = "senderId";
    /* access modifiers changed from: private */
    public String TAG = NetAPIWorkingStatusControllerMcs.class.getSimpleName();
    final ConnectivityManager.NetworkCallback mCellularStateListener;
    private ICentralMsgStoreServiceListener mCentralMsgStoreServiceListener;
    final ConnectivityManager.NetworkCallback mDefaultNetworkListener;
    private boolean mIsMobileConnected = false;
    private McsFcmEventListenerReceiver mMcsFcmEventListenerReceiver;
    private BroadcastReceiver mMcsFcmInstanceIdServiceReceiver;
    private IntentFilter mMcsFcmIntentFilter;
    private BroadcastReceiver mMcsFcmIntentServiceReceiver;
    private IntentFilter mMcsRestartIntentFilter;
    private BroadcastReceiver mMcsRestartServiceReceiver;
    private IntentFilter mMcsTokenIntentFilter;
    private BroadcastReceiver mMcsTokenValidityTimeoutReceiver;
    private String mMobileIp;
    /* access modifiers changed from: private */
    public String mOldFcmToken = "";
    /* access modifiers changed from: private */
    public int mPhoneId;
    final ConnectivityManager.NetworkCallback mWifiStateListener;

    public void resetDataReceiver() {
    }

    public NetAPIWorkingStatusControllerMcs(Looper looper, MessageStoreClient messageStoreClient, IUIEventCallback iUIEventCallback) {
        super(looper, messageStoreClient, iUIEventCallback);
        AnonymousClass5 r2 = new ConnectivityManager.NetworkCallback() {
            public void onAvailable(Network network) {
                String r0 = NetAPIWorkingStatusControllerMcs.this.TAG;
                Log.i(r0, "mDefaultNetworkListener: onAvailable " + network);
                NetAPIWorkingStatusControllerMcs.this.setMobileIp(network);
            }

            public void onLost(Network network) {
                String r2 = NetAPIWorkingStatusControllerMcs.this.TAG;
                Log.i(r2, "mDefaultNetworkListener: onLost " + network);
            }
        };
        this.mDefaultNetworkListener = r2;
        this.mWifiStateListener = new ConnectivityManager.NetworkCallback() {
            public void onAvailable(Network network) {
                IMSLog.i(NetAPIWorkingStatusControllerMcs.this.TAG, "onAvailable wifi");
                NetAPIWorkingStatusControllerMcs.this.sendNetworkChangeMsg(true, 19);
            }

            public void onLost(Network network) {
                IMSLog.i(NetAPIWorkingStatusControllerMcs.this.TAG, "onLost wifi");
                NetAPIWorkingStatusControllerMcs.this.sendNetworkChangeMsg(false, 19);
            }
        };
        this.mCellularStateListener = new ConnectivityManager.NetworkCallback() {
            public void onAvailable(Network network) {
                IMSLog.i(NetAPIWorkingStatusControllerMcs.this.TAG, "onAvailable cellular");
                NetAPIWorkingStatusControllerMcs.this.sendNetworkChangeMsg(true, 20);
            }

            public void onLost(Network network) {
                IMSLog.i(NetAPIWorkingStatusControllerMcs.this.TAG, "onLost cellular");
                NetAPIWorkingStatusControllerMcs.this.sendNetworkChangeMsg(false, 20);
            }
        };
        this.TAG += "[" + messageStoreClient.getClientID() + "]";
        this.mPhoneId = messageStoreClient.getClientID();
        this.mContext = messageStoreClient.getContext();
        IntentFilter intentFilter = new IntentFilter();
        this.mMcsFcmIntentFilter = intentFilter;
        intentFilter.addAction(INTENT_RECEIVE_FCM_REGISTRATION_TOKEN);
        this.mMcsFcmIntentFilter.addAction(INTENT_RECEIVE_FCM_PUSH_NOTIFICATION);
        this.mMcsFcmIntentFilter.addAction(INTENT_REFRESH_FCM_REGISTRATION_TOKEN);
        IntentFilter intentFilter2 = new IntentFilter();
        this.mMcsTokenIntentFilter = intentFilter2;
        intentFilter2.addAction(McsConstants.McsActions.INTENT_ACCESS_TOKEN_VALIDITY_TIMEOUT);
        this.mMcsTokenIntentFilter.addAction(McsConstants.McsActions.INTENT_REFRESH_TOKEN_VALIDITY_TIMEOUT);
        IntentFilter intentFilter3 = new IntentFilter();
        this.mMcsRestartIntentFilter = intentFilter3;
        intentFilter3.addAction(INTENT_RESET_BUFFERDB_MCS);
        registerMcsRestartServiceReceiver();
        registerDefaultSmsPackageChangeReceiver(this.mContext);
        ((ConnectivityManager) this.mContext.getSystemService("connectivity")).registerDefaultNetworkCallback(r2);
        registerAirplaneMode(this.mContext);
    }

    private void registerMcsRestartServiceReceiver() {
        unregisterMcsRestartServiceReceiver();
        BroadcastReceiver mcsRestartServiceReceiver = getMcsRestartServiceReceiver();
        this.mMcsRestartServiceReceiver = mcsRestartServiceReceiver;
        this.mContext.registerReceiver(mcsRestartServiceReceiver, this.mMcsRestartIntentFilter);
    }

    public void unregisterMcsRestartServiceReceiver() {
        BroadcastReceiver broadcastReceiver = this.mMcsRestartServiceReceiver;
        if (broadcastReceiver != null) {
            this.mContext.unregisterReceiver(broadcastReceiver);
            this.mMcsRestartServiceReceiver = null;
        }
    }

    private BroadcastReceiver getMcsRestartServiceReceiver() {
        return new BroadcastReceiver() {
            public void onReceive(Context context, Intent intent) {
                if (NetAPIWorkingStatusControllerMcs.INTENT_RESET_BUFFERDB_MCS.equals(intent.getAction())) {
                    String r3 = NetAPIWorkingStatusControllerMcs.this.TAG;
                    IMSLog.i(r3, "getMcsRestartServiceReceiver: onReceive: " + intent.getAction());
                    if (intent.getIntExtra(NSDSContractExt.QueryParams.SLOT_ID, -1) == NetAPIWorkingStatusControllerMcs.this.mPhoneId && NetAPIWorkingStatusControllerMcs.this.mStoreClient.getPrerenceManager().isDebugEnable()) {
                        NetAPIWorkingStatusControllerMcs.this.onRestartService();
                    }
                }
            }
        };
    }

    public void resetMcsRestartReceiver() {
        Log.i(this.TAG, "resetMcsRestartReceiver");
        unregisterMcsRestartServiceReceiver();
        if (this.mStoreClient.getPrerenceManager().isDebugEnable()) {
            registerMcsRestartServiceReceiver();
        }
    }

    public void onRestartService() {
        Log.i(this.TAG, "Entry restartService Internal Restart case:");
        this.mStoreClient.getMcsRetryMapAdapter().clearRetryHistory();
        this.mStoreClient.getHttpController().getCookieJar().removeAll();
        onCleanBufferDbRequired();
        Toast.makeText(this.mContext, "Buffer DB delete request Triggered", 0).show();
    }

    public void init() {
        initDeviceID();
        this.mStoreClient.getCloudMessageStrategyManager().createStrategy();
        registerWifiStateListener();
        registerCellularStateListener();
    }

    public void clearData() {
        IMSLog.i(this.TAG, "clearData");
        this.mStoreClient.getPrerenceManager().clearAll();
        this.mStoreClient.getHttpController().getCookieJar().removeAll();
        onCleanBufferDbRequired();
        this.mHasNotifiedBufferDBProvisionSuccess = false;
    }

    public void setCmsProfileEnabled(boolean z) {
        String str = this.TAG;
        IMSLog.i(str, "setCmsProfileEnabled: mIsCmsProfileEnabled: " + this.mIsCmsProfileEnabled + " value: " + z);
        if (!this.mIsCmsProfileEnabled || !z) {
            this.mIsCmsProfileEnabled = z;
            if (z) {
                init();
                onNetworkChangeDetected();
                this.mIsDefaultMsgAppNative = CmsUtil.isDefaultMessageAppInUse(this.mContext);
                this.mStoreClient.getPrerenceManager().saveNativeMsgAppIsDefault(this.mIsDefaultMsgAppNative);
                return;
            }
            unregisterNetworkChangeListener();
            stopCMNWorking();
        }
    }

    /* access modifiers changed from: protected */
    public void unregisterNetworkChangeListener() {
        Log.i(this.TAG, "unregisterNetworkChangeListener");
        ConnectivityManager connectivityManager = (ConnectivityManager) this.mContext.getSystemService("connectivity");
        this.mIsWifiConnected = false;
        this.mIsNetworkValid = false;
        this.mIsMobileConnected = false;
        try {
            connectivityManager.unregisterNetworkCallback(this.mWifiStateListener);
            connectivityManager.unregisterNetworkCallback(this.mCellularStateListener);
        } catch (RuntimeException e) {
            Log.e(this.TAG, e.getMessage());
        }
    }

    public void onOmaProvisionFailed(ParamOMAresponseforBufDB paramOMAresponseforBufDB, long j) {
        Class<? extends IHttpAPICommonInterface> lastFailedApi = this.mStoreClient.getCloudMessageStrategyManager().getStrategy().getLastFailedApi();
        String str = this.TAG;
        StringBuilder sb = new StringBuilder();
        sb.append("onOmaProvisionFailed: ");
        sb.append(paramOMAresponseforBufDB);
        sb.append(" lastFailedApi ");
        sb.append(lastFailedApi != null ? lastFailedApi.getSimpleName() : "");
        Log.d(str, sb.toString());
        this.mStoreClient.getMcsRetryMapAdapter().clearRetryHistory();
        this.mStoreClient.updateDelay(4, j);
    }

    private ICentralMsgStoreServiceListener getCentralMsgStoreServiceListener() {
        return new ICentralMsgStoreServiceListener.Stub() {
            public void onCmsAccountInfoDelivered(String str, String str2, int i) {
            }

            public void onCmsPushMessageReceived(String str, String str2, String str3) {
            }

            public void onCmsSdChanged(boolean z, String str, int i) {
            }

            public void onCmsSdManagementCompleted(int i, String str, int i2, int i3) {
            }

            public void onCmsRegistrationCompleted(int i, int i2) {
                String r0 = NetAPIWorkingStatusControllerMcs.this.TAG;
                IMSLog.i(r0, "getCentralMsgStoreServiceListener: onCmsRegistrationCompleted: result: " + i + " details: " + i2);
                if (i == 100 && i2 != 1) {
                    String r4 = NetAPIWorkingStatusControllerMcs.this.TAG;
                    int r02 = NetAPIWorkingStatusControllerMcs.this.mPhoneId;
                    EventLogHelper.add(r4, r02, "MCS registration completed. details: " + i2);
                    NetAPIWorkingStatusControllerMcs.this.removeMessages(12);
                    NetAPIWorkingStatusControllerMcs.this.sendEmptyMessage(12);
                    NetAPIWorkingStatusControllerMcs.this.onCmsRegistrationCompletedEvent();
                }
            }

            public void onCmsDeRegistrationCompleted(int i) {
                String r0 = NetAPIWorkingStatusControllerMcs.this.TAG;
                IMSLog.i(r0, "getCentralMsgStoreServiceListener: onCmsDeRegistrationCompleted: result: " + i);
                if (i == 100) {
                    EventLogHelper.add(NetAPIWorkingStatusControllerMcs.this.TAG, NetAPIWorkingStatusControllerMcs.this.mPhoneId, "MCS deregistration completed");
                    NetAPIWorkingStatusControllerMcs.this.removeMessages(13);
                    NetAPIWorkingStatusControllerMcs.this.sendEmptyMessage(13);
                }
            }
        };
    }

    public void registerCentralMsgStoreServiceListener() {
        unregisterCentralMsgStoreServiceListener();
        ICentralMsgStoreServiceListener centralMsgStoreServiceListener = getCentralMsgStoreServiceListener();
        this.mCentralMsgStoreServiceListener = centralMsgStoreServiceListener;
        this.mStoreClient.registerCmsProvisioningListener(centralMsgStoreServiceListener, false);
    }

    public void unregisterCentralMsgStoreServiceListener() {
        ICentralMsgStoreServiceListener iCentralMsgStoreServiceListener = this.mCentralMsgStoreServiceListener;
        if (iCentralMsgStoreServiceListener != null) {
            this.mStoreClient.unregisterCmsProvisioningListener(iCentralMsgStoreServiceListener);
            this.mCentralMsgStoreServiceListener = null;
        }
    }

    private BroadcastReceiver getMcsFcmIntentServiceReceiver() {
        return new BroadcastReceiver() {
            public void onReceive(Context context, Intent intent) {
                int intExtra = intent.getIntExtra("phoneId", -1);
                IMSLog.i(NetAPIWorkingStatusControllerMcs.this.TAG, "getMcsFcmIntentServiceReceiver: onReceive: phoneId: " + intExtra + " mPhoneId: " + NetAPIWorkingStatusControllerMcs.this.mPhoneId + " Action:" + intent.getAction());
                if (NetAPIWorkingStatusControllerMcs.INTENT_RECEIVE_FCM_REGISTRATION_TOKEN.equals(intent.getAction()) && intExtra == NetAPIWorkingStatusControllerMcs.this.mPhoneId) {
                    IMSLog.c(LogClass.MCS_NC_FCM_REGI_TOKEN_RECEIVE, NetAPIWorkingStatusControllerMcs.this.mPhoneId + ",NC:REG_TK_RCV");
                    String stringExtra = intent.getStringExtra("senderId");
                    String fcmSenderId = NetAPIWorkingStatusControllerMcs.this.mStoreClient.getPrerenceManager().getFcmSenderId();
                    String stringExtra2 = intent.getStringExtra(NetAPIWorkingStatusControllerMcs.FCM_REGISTRATION_TOKEN);
                    boolean z = false;
                    boolean booleanExtra = intent.getBooleanExtra(NetAPIWorkingStatusControllerMcs.FCM_REGISTRATION_TOKEN_REFRESHED, false);
                    if (!TextUtils.isEmpty(stringExtra) && stringExtra.equals(fcmSenderId) && !TextUtils.isEmpty(stringExtra2)) {
                        z = true;
                    }
                    EventLogHelper.add(NetAPIWorkingStatusControllerMcs.this.TAG, intExtra, "Receive FCM registration token, isValidRegi: " + z);
                    IMSLog.s(NetAPIWorkingStatusControllerMcs.this.TAG, "getMcsFcmIntentServiceReceiver: onReceive: senderId: " + stringExtra + " cursenderId: " + fcmSenderId + " token: " + stringExtra2 + " isFcmRegistrationTokenRefreshed: " + booleanExtra + " oldFcmTokenEmpty: " + TextUtils.isEmpty(NetAPIWorkingStatusControllerMcs.this.mOldFcmToken));
                    if (z) {
                        NetAPIWorkingStatusControllerMcs.this.mOldFcmToken = "";
                        NetAPIWorkingStatusControllerMcs.this.sendFcmRegistrationSuccess(stringExtra2, booleanExtra);
                        return;
                    }
                    int fcmRetryCount = NetAPIWorkingStatusControllerMcs.this.mStoreClient.getPrerenceManager().getFcmRetryCount();
                    EventLogHelper.add(NetAPIWorkingStatusControllerMcs.this.TAG, intExtra, "getMcsFcmIntentServiceReceiver: onReceive: EVENT_RECEIVE_FCM_REGISTRATION_TOKEN failure retryCount: " + fcmRetryCount);
                    if (fcmRetryCount < 3) {
                        int i = fcmRetryCount + 1;
                        NetAPIWorkingStatusControllerMcs.this.mStoreClient.getPrerenceManager().saveFcmRetryCount(i);
                        NetAPIWorkingStatusControllerMcs netAPIWorkingStatusControllerMcs = NetAPIWorkingStatusControllerMcs.this;
                        netAPIWorkingStatusControllerMcs.sendMessageDelayed(netAPIWorkingStatusControllerMcs.obtainMessage(14, Boolean.FALSE), ((long) (Math.pow(5.0d, (double) i) * 1000.0d)) + 5);
                        return;
                    }
                    IMSLog.c(LogClass.MCS_NC_FCM_REGI_TOKEN_FAILURE, NetAPIWorkingStatusControllerMcs.this.mPhoneId + ",NC:REG_TK_FAIL");
                    if (!TextUtils.isEmpty(NetAPIWorkingStatusControllerMcs.this.mOldFcmToken)) {
                        NetAPIWorkingStatusControllerMcs netAPIWorkingStatusControllerMcs2 = NetAPIWorkingStatusControllerMcs.this;
                        netAPIWorkingStatusControllerMcs2.sendFcmRegistrationSuccess(netAPIWorkingStatusControllerMcs2.mOldFcmToken, booleanExtra);
                    }
                }
            }
        };
    }

    public void sendFcmRegistrationSuccess(String str, boolean z) {
        this.mStoreClient.getPrerenceManager().saveFcmRetryCount(0);
        Bundle bundle = new Bundle();
        bundle.putString(FCM_REGISTRATION_TOKEN, str);
        bundle.putBoolean(FCM_REGISTRATION_TOKEN_REFRESHED, z);
        IMSLog.i(this.TAG, "getMcsFcmIntentServiceReceiver: send EVENT_RECEIVE_FCM_REGISTRATION_TOKEN");
        removeMessages(15);
        sendMessage(obtainMessage(15, bundle));
    }

    public void onStartFcmRetry() {
        removeMessages(14);
        sendMessage(obtainMessage(14, Boolean.FALSE));
    }

    public void registerMcsFcmIntentServiceReceiver() {
        unregisterMcsFcmIntentServiceReceiver();
        BroadcastReceiver mcsFcmIntentServiceReceiver = getMcsFcmIntentServiceReceiver();
        this.mMcsFcmIntentServiceReceiver = mcsFcmIntentServiceReceiver;
        this.mContext.registerReceiver(mcsFcmIntentServiceReceiver, this.mMcsFcmIntentFilter);
    }

    public void unregisterMcsFcmIntentServiceReceiver() {
        BroadcastReceiver broadcastReceiver = this.mMcsFcmIntentServiceReceiver;
        if (broadcastReceiver != null) {
            this.mContext.unregisterReceiver(broadcastReceiver);
            this.mMcsFcmIntentServiceReceiver = null;
        }
    }

    private BroadcastReceiver getMcsFcmInstanceIdServiceReceiver() {
        return new BroadcastReceiver() {
            public void onReceive(Context context, Intent intent) {
                if (NetAPIWorkingStatusControllerMcs.INTENT_REFRESH_FCM_REGISTRATION_TOKEN.equals(intent.getAction())) {
                    IMSLog.i(NetAPIWorkingStatusControllerMcs.this.TAG, "getMcsFcmInstanceIdServiceReceiver: onReceive: INTENT_REFRESH_FCM_REGISTRATION_TOKEN");
                    EventLogHelper.add(NetAPIWorkingStatusControllerMcs.this.TAG, NetAPIWorkingStatusControllerMcs.this.mPhoneId, "Refresh FCM registration token");
                    IMSLog.c(LogClass.MCS_NC_FCM_REGI_TOKEN_REFRESH, NetAPIWorkingStatusControllerMcs.this.mPhoneId + ",NC:REG_TK_REF");
                    NetAPIWorkingStatusControllerMcs.this.removeMessages(16);
                    NetAPIWorkingStatusControllerMcs.this.sendEmptyMessage(16);
                }
            }
        };
    }

    /* access modifiers changed from: private */
    public void setMobileIp(Network network) {
        LinkProperties linkProperties = ((ConnectivityManager) this.mContext.getSystemService("connectivity")).getLinkProperties(network);
        if (linkProperties != null) {
            List<LinkAddress> linkAddresses = linkProperties.getLinkAddresses();
            for (LinkAddress address : linkAddresses) {
                InetAddress address2 = address.getAddress();
                if (!address2.isLoopbackAddress() && (address2 instanceof Inet6Address)) {
                    String hostAddress = address2.getHostAddress();
                    this.mMobileIp = decompressIpv6Address(hostAddress) + "::/64";
                    Log.i(this.TAG, "setMobileIp: IPv6 decompressed address");
                    return;
                }
            }
            for (LinkAddress address3 : linkAddresses) {
                InetAddress address4 = address3.getAddress();
                if (!address4.isLoopbackAddress() && (address4 instanceof Inet4Address)) {
                    this.mMobileIp = address4.getHostAddress();
                    Log.i(this.TAG, "setMobileIp: IPv4 address");
                    return;
                }
            }
        }
    }

    /* access modifiers changed from: private */
    public void sendNetworkChangeMsg(boolean z, int i) {
        Message message = new Message();
        message.obj = Boolean.valueOf(z);
        message.what = i;
        sendMessage(message);
    }

    private void registerWifiStateListener() {
        ((ConnectivityManager) this.mContext.getSystemService("connectivity")).registerNetworkCallback(new NetworkRequest.Builder().addTransportType(1).addCapability(12).build(), this.mWifiStateListener);
    }

    private void registerCellularStateListener() {
        ((ConnectivityManager) this.mContext.getSystemService("connectivity")).registerNetworkCallback(new NetworkRequest.Builder().addTransportType(0).addCapability(12).build(), this.mCellularStateListener);
    }

    public String decompressIpv6Address(String str) {
        if (str == null) {
            return null;
        }
        String trim = str.trim();
        StringBuilder sb = new StringBuilder();
        String[] split = trim.split(":");
        int length = split.length;
        for (int i = 0; i < length; i++) {
            String str2 = split[i];
            if ("".equals(str2)) {
                for (int i2 = 0; i2 <= 8 - split.length; i2++) {
                    sb.append("0000:");
                }
            } else {
                while (str2.length() != 4) {
                    str2 = "0" + str2;
                }
                sb.append(str2 + ":");
            }
        }
        return sb.length() > 19 ? sb.substring(0, 19) : sb.toString();
    }

    public String getMobileIp() {
        return this.mMobileIp;
    }

    public void registerMcsFcmInstanceIdServiceReceiver() {
        unregisterMcsFcmInstanceIdServiceReceiver();
        BroadcastReceiver mcsFcmInstanceIdServiceReceiver = getMcsFcmInstanceIdServiceReceiver();
        this.mMcsFcmInstanceIdServiceReceiver = mcsFcmInstanceIdServiceReceiver;
        this.mContext.registerReceiver(mcsFcmInstanceIdServiceReceiver, this.mMcsFcmIntentFilter);
    }

    public void unregisterMcsFcmInstanceIdServiceReceiver() {
        BroadcastReceiver broadcastReceiver = this.mMcsFcmInstanceIdServiceReceiver;
        if (broadcastReceiver != null) {
            this.mContext.unregisterReceiver(broadcastReceiver);
            this.mMcsFcmInstanceIdServiceReceiver = null;
        }
    }

    public void registerMcsFcmEventListenerReceiver() {
        unregisterMcsFcmEventListenerReceiver();
        McsFcmEventListenerReceiver mcsFcmEventListenerReceiver = new McsFcmEventListenerReceiver(this.mContext, this.mPhoneId, this.mStoreClient);
        this.mMcsFcmEventListenerReceiver = mcsFcmEventListenerReceiver;
        this.mContext.registerReceiver(mcsFcmEventListenerReceiver, this.mMcsFcmIntentFilter);
    }

    public void unregisterMcsFcmEventListenerReceiver() {
        McsFcmEventListenerReceiver mcsFcmEventListenerReceiver = this.mMcsFcmEventListenerReceiver;
        if (mcsFcmEventListenerReceiver != null) {
            this.mContext.unregisterReceiver(mcsFcmEventListenerReceiver);
            this.mMcsFcmEventListenerReceiver = null;
        }
    }

    private BroadcastReceiver getMcsTokenValidityTimeoutReceiver() {
        return new BroadcastReceiver() {
            public void onReceive(Context context, Intent intent) {
                if (McsConstants.McsActions.INTENT_ACCESS_TOKEN_VALIDITY_TIMEOUT.equals(intent.getAction()) || McsConstants.McsActions.INTENT_REFRESH_TOKEN_VALIDITY_TIMEOUT.equals(intent.getAction())) {
                    String r3 = NetAPIWorkingStatusControllerMcs.this.TAG;
                    IMSLog.i(r3, "getMcsTokenValidityTimeoutReceiver: onReceive: " + intent.getAction());
                    NetAPIWorkingStatusControllerMcs.this.removeMessages(18);
                    NetAPIWorkingStatusControllerMcs.this.sendEmptyMessage(18);
                }
            }
        };
    }

    public void registerTokenValidityTimeoutReceiver() {
        unregisterTokenValidityTimeoutReceiver();
        BroadcastReceiver mcsTokenValidityTimeoutReceiver = getMcsTokenValidityTimeoutReceiver();
        this.mMcsTokenValidityTimeoutReceiver = mcsTokenValidityTimeoutReceiver;
        this.mContext.registerReceiver(mcsTokenValidityTimeoutReceiver, this.mMcsTokenIntentFilter);
    }

    public void unregisterTokenValidityTimeoutReceiver() {
        BroadcastReceiver broadcastReceiver = this.mMcsTokenValidityTimeoutReceiver;
        if (broadcastReceiver != null) {
            this.mContext.unregisterReceiver(broadcastReceiver);
            this.mMcsTokenValidityTimeoutReceiver = null;
        }
    }

    /* access modifiers changed from: protected */
    public void setNetworkStatus(boolean z) {
        String str = this.TAG;
        IMSLog.i(str, "setNetworkStatus: " + z + " mIsNetworkValid:" + this.mIsNetworkValid + " mIsCMNWorkingStarted:" + this.mIsCMNWorkingStarted + " provisionStatus:" + this.mStoreClient.getProvisionStatus() + " ProfileActive:" + isCmsProfileActive() + " mIsCmsProfileEnabled:" + this.mIsCmsProfileEnabled + " isMobileConnected:" + this.mIsMobileConnected + " isWifiConnected:" + this.mIsWifiConnected);
        if (this.mIsCmsProfileEnabled || !z) {
            boolean z2 = this.mIsNetworkValid;
            if (z2 && !z) {
                String str2 = this.TAG;
                int i = this.mPhoneId;
                EventLogHelper.add(str2, i, "Network changed: " + z);
                pauseCMNWorkingForDeregi();
                this.mIsNetworkValid = z;
            } else if (z2 == z) {
                IMSLog.d(this.TAG, "same network state, nothing to be done");
            } else {
                this.mIsNetworkValid = z;
                if (this.mStoreClient.getProvisionStatus() && isCmsProfileActive() && this.mIsDefaultMsgAppNative) {
                    String str3 = this.TAG;
                    int i2 = this.mPhoneId;
                    EventLogHelper.add(str3, i2, "Network changed: " + z + ", mIsCMNWorkingStarted: " + this.mIsCMNWorkingStarted);
                    if (!this.mIsCMNWorkingStarted) {
                        this.mIsCMNWorkingStarted = true;
                        startCMNWorking();
                        return;
                    }
                    resumeCMNWorking();
                }
            }
        }
    }

    /* access modifiers changed from: protected */
    public void startCMNWorking() {
        IMSLog.i(this.TAG, "startCMNWorking");
        this.mIsCMNWorkingStarted = true;
        this.mNetAPIHandler.startforMcs();
    }

    public void stopCMNWorking() {
        IMSLog.i(this.TAG, "stopCMNWorking");
        this.mIsCMNWorkingStarted = false;
        this.mNetAPIHandler.stopforMcs();
    }

    /* access modifiers changed from: protected */
    public void pauseCMNWorkingForDeregi() {
        IMSLog.i(this.TAG, "pauseCMNWorkingForDeregi");
        this.mNetAPIHandler.pauseMcsForDeregi();
        pauseCMNWorking();
    }

    /* access modifiers changed from: protected */
    public void pauseCMNWorking() {
        IMSLog.i(this.TAG, "pauseCMNWorking");
        this.mIsOMAAPIRunning = false;
        this.mNetAPIHandler.pauseforMcs();
    }

    /* access modifiers changed from: protected */
    public void resumeCMNWorking() {
        IMSLog.i(this.TAG, "resumeCMNWorking");
        if (!this.mIsCMNWorkingStarted) {
            IMSLog.i(this.TAG, "resume called before starting. This should not be processed");
            return;
        }
        this.mIsOMAAPIRunning = true;
        this.mNetAPIHandler.resumeforMcs(false);
    }

    public void handleMessage(Message message) {
        IMSLog.i(this.TAG, "handleMessage: msg: " + message.what);
        int i = message.what;
        boolean z = true;
        if (i == 1) {
            handleEventMessageAppChanged();
        } else if (i == 2) {
            this.mIsAirPlaneModeOn = Settings.System.getInt(this.mContext.getContentResolver(), "airplane_mode_on", 0) != 0;
            if (!this.mIsMobileConnected && !this.mIsWifiConnected) {
                z = false;
            }
            Log.d(this.TAG, "Network available: " + z + " mobile:" + this.mIsMobileConnected + " wifi:" + this.mIsWifiConnected);
            if (this.mIsNetworkValid && !z) {
                Log.i(this.TAG, "no available network, reset channel state.");
                this.mNetAPIHandler.resetChannelState();
            }
            setNetworkStatus(z);
        } else if (i != 8) {
            String str = "";
            switch (i) {
                case 12:
                    registerTokenValidityTimeoutReceiver();
                    this.mStoreClient.getPrerenceManager().saveFcmRetryCount(0);
                    removeMessages(14);
                    sendMessage(obtainMessage(14, Boolean.FALSE));
                    startInitSync();
                    return;
                case 13:
                    unregisterMcsFcmIntentServiceReceiver();
                    unregisterMcsFcmInstanceIdServiceReceiver();
                    unregisterMcsFcmEventListenerReceiver();
                    unregisterTokenValidityTimeoutReceiver();
                    pauseCMNWorkingForDeregi();
                    return;
                case 14:
                    EventLogHelper.add(this.TAG, this.mPhoneId, "Request FCM registration token");
                    IMSLog.c(LogClass.MCS_NC_FCM_REGI_TOKEN_REQUEST, this.mPhoneId + ",NC:REG_TK_REQ");
                    registerMcsFcmEventListenerReceiver();
                    Object obj = message.obj;
                    if (obj == null || !((Boolean) obj).booleanValue()) {
                        z = false;
                    }
                    String fcmSenderId = this.mStoreClient.getPrerenceManager().getFcmSenderId();
                    IMSLog.s(this.TAG, "handleMessage: EVENT_REQUEST_FCM_REGISTRATION_TOKEN: senderId: " + fcmSenderId + " isFcmRegistrationTokenRefreshed:" + z);
                    if (!z) {
                        this.mOldFcmToken = this.mStoreClient.getPrerenceManager().getFcmRegistrationToken();
                        this.mStoreClient.getPrerenceManager().saveFcmRegistrationToken(str);
                    }
                    if (!TextUtils.isEmpty(fcmSenderId)) {
                        registerMcsFcmIntentServiceReceiver();
                        registerMcsFcmInstanceIdServiceReceiver();
                        Intent intent = new Intent(this.mContext, McsFcmIntentService.class);
                        intent.putExtra("phoneId", this.mPhoneId);
                        intent.putExtra("senderId", fcmSenderId);
                        intent.putExtra(FCM_REGISTRATION_TOKEN_REFRESHED, z);
                        intent.addFlags(IntentUtil.FLAG_RECEIVER_INCLUDE_BACKGROUND);
                        IMSLog.i(this.TAG, "handleMessage: EVENT_REQUEST_FCM_REGISTRATION_TOKEN: sendBroadcast McsFcmIntentService");
                        this.mContext.startService(intent);
                        return;
                    }
                    return;
                case 15:
                    unregisterMcsFcmIntentServiceReceiver();
                    Object obj2 = message.obj;
                    if (obj2 != null) {
                        str = ((Bundle) obj2).getString(FCM_REGISTRATION_TOKEN);
                    }
                    String fcmRegistrationToken = this.mStoreClient.getPrerenceManager().getFcmRegistrationToken();
                    Object obj3 = message.obj;
                    if (obj3 == null || !((Bundle) obj3).getBoolean(FCM_REGISTRATION_TOKEN_REFRESHED)) {
                        z = false;
                    }
                    IMSLog.i(this.TAG, "handleMessage: EVENT_RECEIVE_FCM_REGISTRATION_TOKEN: token: " + IMSLog.checker(str) + " isFcmRegistrationTokenRefreshed: " + z + " " + this.mIsNetworkValid + " " + this.mStoreClient.getProvisionStatus() + "  " + isCmsProfileActive());
                    if (TextUtils.equals(str, fcmRegistrationToken)) {
                        IMSLog.d(this.TAG, "token remained same after refresh, do nothing");
                        return;
                    }
                    this.mStoreClient.getPrerenceManager().saveFcmRegistrationToken(str);
                    if (this.mIsNetworkValid && this.mStoreClient.getProvisionStatus() && isCmsProfileActive() && this.mIsDefaultMsgAppNative) {
                        if (z || this.mIsCMNWorkingStarted) {
                            resumeCMNWorkingForTokenRefresh();
                            return;
                        } else {
                            startCMNWorking();
                            return;
                        }
                    } else {
                        return;
                    }
                case 16:
                    removeMessages(14);
                    sendMessage(obtainMessage(14, Boolean.TRUE));
                    return;
                default:
                    switch (i) {
                        case 18:
                            EventLogHelper.add(this.TAG, this.mPhoneId, "Token validity timed out");
                            unregisterMcsFcmIntentServiceReceiver();
                            unregisterMcsFcmInstanceIdServiceReceiver();
                            unregisterMcsFcmEventListenerReceiver();
                            unregisterTokenValidityTimeoutReceiver();
                            stopCMNWorking();
                            return;
                        case 19:
                            this.mIsWifiConnected = ((Boolean) message.obj).booleanValue();
                            sendMessage(obtainMessage(2));
                            return;
                        case 20:
                            this.mIsMobileConnected = ((Boolean) message.obj).booleanValue();
                            sendMessage(obtainMessage(2));
                            return;
                        default:
                            IMSLog.i(this.TAG, "handleMessage: unknown msg");
                            super.handleMessage(message);
                            return;
                    }
            }
        } else {
            if (Settings.System.getInt(this.mContext.getContentResolver(), "airplane_mode_on", 0) == 0) {
                z = false;
            }
            this.mIsAirPlaneModeOn = z;
            IMSLog.i(this.TAG, "airplane mode change :" + this.mIsAirPlaneModeOn + " oldMobile:" + this.mIsMobileConnected + " oldWifi:" + this.mIsWifiConnected);
            String str2 = this.TAG;
            int i2 = this.mPhoneId;
            StringBuilder sb = new StringBuilder();
            sb.append("AirplaneMode changed: ");
            sb.append(this.mIsAirPlaneModeOn);
            EventLogHelper.add(str2, i2, sb.toString());
            if (this.mIsAirPlaneModeOn) {
                setNetworkStatus(false);
                this.mIsWifiConnected = false;
                this.mIsMobileConnected = false;
            }
        }
    }

    private void resumeCMNWorkingForTokenRefresh() {
        IMSLog.i(this.TAG, "resumeCMNWorkingForTokenRefresh");
        this.mIsOMAAPIRunning = true;
        this.mNetAPIHandler.resumeforMcs(true);
    }

    public void registerForUpdateFromCloud(Handler handler, int i, Object obj) {
        this.mNetAPIHandler.registerForUpdateFromCloud(handler, i, obj);
    }

    /* access modifiers changed from: protected */
    public void registerDefaultSmsPackageChangeReceiver(Context context) {
        IntentFilter intentFilter = new IntentFilter();
        intentFilter.addAction("android.provider.action.DEFAULT_SMS_PACKAGE_CHANGED_INTERNAL");
        context.registerReceiver(new BroadcastReceiver() {
            public void onReceive(Context context, Intent intent) {
                String str;
                if (intent != null && "android.provider.action.DEFAULT_SMS_PACKAGE_CHANGED_INTERNAL".equals(intent.getAction())) {
                    try {
                        str = Telephony.Sms.getDefaultSmsPackage(NetAPIWorkingStatusControllerMcs.this.mContext);
                    } catch (Exception e) {
                        String r4 = NetAPIWorkingStatusControllerMcs.this.TAG;
                        IMSLog.e(r4, "registerDefaultSmsPackageChangeReceiver: onReceive: fail to get currentPackage: " + e);
                        str = null;
                    }
                    String r42 = NetAPIWorkingStatusControllerMcs.this.TAG;
                    IMSLog.i(r42, "registerDefaultSmsPackageChangeReceiver: onReceive: MessageApplication is changed: " + str);
                    if (str != null) {
                        NetAPIWorkingStatusControllerMcs.this.removeMessages(1);
                        NetAPIWorkingStatusControllerMcs.this.sendEmptyMessage(1);
                    }
                }
            }
        }, intentFilter);
    }

    /* access modifiers changed from: protected */
    public void registerAirplaneMode(Context context) {
        boolean z = false;
        if (Settings.System.getInt(this.mContext.getContentResolver(), "airplane_mode_on", 0) != 0) {
            z = true;
        }
        this.mIsAirPlaneModeOn = z;
        this.mIsNetworkValid = !z;
        IntentFilter intentFilter = new IntentFilter();
        intentFilter.addAction(ImsConstants.Intents.ACTION_AIRPLANE_MODE);
        context.registerReceiver(new BroadcastReceiver() {
            public void onReceive(Context context, Intent intent) {
                String action = intent.getAction();
                String r4 = NetAPIWorkingStatusControllerMcs.this.TAG;
                Log.d(r4, "registerAirplaneMode, BroadcastReceiver, action: " + action);
                if (ImsConstants.Intents.ACTION_AIRPLANE_MODE.equals(action)) {
                    NetAPIWorkingStatusControllerMcs netAPIWorkingStatusControllerMcs = NetAPIWorkingStatusControllerMcs.this;
                    netAPIWorkingStatusControllerMcs.sendMessage(netAPIWorkingStatusControllerMcs.obtainMessage(8));
                }
            }
        }, intentFilter);
    }

    /* access modifiers changed from: protected */
    public void handleEventMessageAppChanged() {
        boolean isDefaultMessageAppInUse = CmsUtil.isDefaultMessageAppInUse(this.mContext);
        String str = this.TAG;
        IMSLog.i(str, "handleEventMessageAppChanged: mIsCmsProfileEnabled:" + this.mIsCmsProfileEnabled + " mIsDefaultMsgAppNative:" + this.mIsDefaultMsgAppNative + " isDefaultMessageApp:" + isDefaultMessageAppInUse);
        if (this.mIsCmsProfileEnabled && this.mIsDefaultMsgAppNative != isDefaultMessageAppInUse) {
            this.mIsDefaultMsgAppNative = isDefaultMessageAppInUse;
            String str2 = this.TAG;
            int i = this.mPhoneId;
            EventLogHelper.add(str2, i, "Default Message App changed. isNative: " + this.mIsDefaultMsgAppNative);
            if (!this.mIsDefaultMsgAppNative) {
                this.mStoreClient.getPrerenceManager().saveNativeMsgAppIsDefault(false);
                this.mNetAPIHandler.deleteNotificationForDMAChange();
                pauseCMNWorking();
                return;
            }
            this.mStoreClient.getPrerenceManager().saveNativeMsgAppIsDefault(true);
            this.mWorkingStatus.notifyRegistrants(new AsyncResult((Object) null, IWorkingStatusProvisionListener.WorkingStatus.DEFAULT_MSGAPP_CHGTO_NATIVE, (Throwable) null));
            String str3 = this.TAG;
            IMSLog.i(str3, "handleEventMessageAppChanged validNetwork:" + this.mIsNetworkValid + " provisionStatus:" + this.mStoreClient.getProvisionStatus() + " profileActive:" + isCmsProfileActive());
            if (this.mIsNetworkValid && this.mStoreClient.getProvisionStatus() && isCmsProfileActive()) {
                resumeCMNWorking();
            }
        }
    }

    public void onChannelLifetimeUpdateComplete() {
        IMSLog.i(this.TAG, "onChannelLifetimeUpdateComplete");
        removeMessages(14);
        sendMessage(obtainMessage(14, Boolean.FALSE));
    }
}
