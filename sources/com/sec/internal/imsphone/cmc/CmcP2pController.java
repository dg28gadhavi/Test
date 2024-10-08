package com.sec.internal.imsphone.cmc;

import android.content.Context;
import android.os.Build;
import android.os.Handler;
import android.os.Looper;
import android.os.Message;
import android.provider.Settings;
import android.text.TextUtils;
import android.util.Log;
import com.sec.ims.settings.ImsProfile;
import com.sec.ims.settings.ImsProfileLoader;
import com.sec.internal.helper.NetworkUtil;
import com.sec.internal.helper.SimUtil;
import com.sec.internal.helper.os.ITelephonyManager;
import com.sec.internal.helper.os.TelephonyManagerWrapper;
import com.sec.internal.ims.registry.ImsRegistry;
import com.sec.internal.imsphone.cmc.ICmcConnectivityController;
import com.sec.internal.interfaces.ims.core.IRegistrationManager;
import com.sec.internal.interfaces.ims.servicemodules.volte2.IVolteServiceModule;
import com.sec.internal.log.IMSLog;
import java.net.NetworkInterface;
import java.util.ArrayList;
import java.util.Collections;

public class CmcP2pController extends Handler {
    private static final boolean DBG = "eng".equals(Build.TYPE);
    private static final int EVENT_SHUT_DOWN_P2P = 1001;
    private static final String LOG_TAG = CmcP2pController.class.getSimpleName();
    private static final String SERVER_DOMAIN = "p2p.samsungims.com";
    private static final int SHUT_DOWN_P2P_TIMEOUT = 1000;
    private static final String SIP_DOMAIN = "samsungims.com";
    private static final String WD_CMC_ACTIVATION = "cmc_activation";
    private static final String WD_HOST_PCSCF_IP = "192.168.49.1";
    private static final String WD_PDN_NAME = "p2p-wlan";
    private static final String WD_PROFILE_NAME_PD = "SamsungCMC_WIFI_P2P_PD";
    private static final String WD_PROFILE_NAME_SD = "SamsungCMC_WIFI_P2P_SD";
    private ICmcConnectivityController mCmcConnectivityController;
    private final Context mContext;
    private ICmcConnectivityController.DeviceType mDeviceType;
    private IpServiceManager mIpServiceManager = null;
    private final boolean mIsEnableP2pFeature = false;
    private boolean mNeedP2pCallSession;
    private String mOwnDeviceId;
    private int mP2pCallSessionId;
    private int mPhoneCount = 0;
    private int mPrevAct;
    private String mPrimaryDuid;
    private IRegistrationManager mRm;
    private ITelephonyManager mTelephonyManager = null;
    private String mWifiDirectAuthToken;
    private boolean mWifiDirectEnabled;
    private boolean mWifiDirectIsConnect;
    private ImsProfile mWifiDirectProfile;
    private boolean mWifiDirectRegistered;
    private String mWifiLocalIp;

    public boolean isEnabledWifiDirectFeature() {
        return false;
    }

    public void startP2pBind() {
    }

    public CmcP2pController(Looper looper, ICmcConnectivityController iCmcConnectivityController, IRegistrationManager iRegistrationManager, IpServiceManager ipServiceManager) {
        super(looper);
        ICmcConnectivityController.DeviceType deviceType = ICmcConnectivityController.DeviceType.None;
        this.mDeviceType = deviceType;
        this.mOwnDeviceId = "";
        this.mPrimaryDuid = "";
        this.mWifiLocalIp = "";
        this.mWifiDirectIsConnect = false;
        this.mWifiDirectRegistered = false;
        this.mWifiDirectAuthToken = "";
        this.mWifiDirectProfile = null;
        this.mNeedP2pCallSession = false;
        this.mP2pCallSessionId = -1;
        this.mPrevAct = -1;
        this.mWifiDirectEnabled = false;
        Context context = ImsRegistry.getContext();
        this.mContext = context;
        this.mRm = iRegistrationManager;
        this.mTelephonyManager = TelephonyManagerWrapper.getInstance(context);
        this.mIpServiceManager = ipServiceManager;
        this.mDeviceType = deviceType;
        this.mPhoneCount = SimUtil.getPhoneCount();
        this.mCmcConnectivityController = iCmcConnectivityController;
        String str = LOG_TAG;
        IMSLog.i(str, "mPhoneCount: " + this.mPhoneCount);
    }

    private void onShutDownP2p() {
        this.mNeedP2pCallSession = false;
        this.mP2pCallSessionId = -1;
        onWifiDirectConnectionChanged(false, "");
    }

    public void stopP2p() {
        String str = LOG_TAG;
        Log.i(str, "stopP2p: curDeviceType: " + this.mDeviceType + " -> None");
        sendEmptyMessageDelayed(1001, 1000);
    }

    public void setCmcActivation(boolean z) {
        if (z) {
            this.mPrevAct = Settings.Global.getInt(this.mContext.getContentResolver(), WD_CMC_ACTIVATION, 0);
            String str = LOG_TAG;
            IMSLog.i(str, "connected, mPrevAct: " + this.mPrevAct);
            Settings.Global.putInt(this.mContext.getContentResolver(), WD_CMC_ACTIVATION, 1);
            return;
        }
        String str2 = LOG_TAG;
        IMSLog.i(str2, "disconnected, mPrevAct: " + this.mPrevAct);
        if (this.mPrevAct != -1) {
            Settings.Global.putInt(this.mContext.getContentResolver(), WD_CMC_ACTIVATION, this.mPrevAct);
            this.mPrevAct = -1;
        }
    }

    public void setP2pPD() {
        Log.i(LOG_TAG, "setP2pPD");
        this.mDeviceType = ICmcConnectivityController.DeviceType.PDevice;
    }

    public boolean isExistP2pConnection() {
        String str = LOG_TAG;
        Log.i(str, "isExistP2pConnection, mWifiDirectIsConnect: " + this.mWifiDirectIsConnect);
        return this.mWifiDirectIsConnect;
    }

    public ICmcConnectivityController.DeviceType getDeviceType() {
        String str = LOG_TAG;
        Log.i(str, "getDeviceType, mDeviceType: " + this.mDeviceType);
        return this.mDeviceType;
    }

    public void needP2pCallSession(boolean z) {
        this.mNeedP2pCallSession = z;
    }

    public int getP2pCallSessionId() {
        return this.mP2pCallSessionId;
    }

    public void setP2pCallSessionId(int i) {
        this.mP2pCallSessionId = i;
    }

    public void onRegistered(int i) {
        String str = LOG_TAG;
        Log.i(str, "onRegistered, cmcType: " + i);
        if (i == 7 || i == 8) {
            this.mWifiDirectRegistered = true;
            if (i == 7 && this.mNeedP2pCallSession) {
                this.mIpServiceManager.ipRuleAdd("local_network", WD_HOST_PCSCF_IP);
            }
        }
    }

    public void onDeregistered(int i) {
        String str = LOG_TAG;
        Log.i(str, "onDeregistered, cmcType: " + i);
        this.mNeedP2pCallSession = false;
        this.mP2pCallSessionId = -1;
        if (i == 8) {
            Log.i(str, "wifi-direct disconnect, releaseP2pNetwork!");
        }
    }

    private void onWifiDirectConnectionChanged(boolean z, String str) {
        String str2 = LOG_TAG;
        IMSLog.i(str2, "onWifiDirectConnectionChanged(), mDeviceType: " + this.mDeviceType);
        if (z) {
            IMSLog.i(str2, "mWifiDirectIsConnect: " + this.mWifiDirectIsConnect + ", mWifiDirectRegistered: " + this.mWifiDirectRegistered);
            if (this.mWifiDirectRegistered) {
                if (this.mWifiDirectIsConnect) {
                    IMSLog.i(str2, "already wifi direct is registered, maybe it'll be connected for 3rd SD");
                    return;
                }
                imsDeregister();
            }
            this.mNeedP2pCallSession = true;
            this.mWifiDirectIsConnect = true;
            this.mWifiDirectAuthToken = str;
            if (this.mDeviceType == ICmcConnectivityController.DeviceType.PDevice) {
                imsRegister(WD_PDN_NAME, WD_PROFILE_NAME_PD);
                this.mIpServiceManager.ipRuleRemove("local_network", WD_HOST_PCSCF_IP);
                if (this.mRm.isCmcRegistered(SimUtil.getActiveDataPhoneId()) > 0 || this.mCmcConnectivityController.isWifiRegistered()) {
                    String wifiIpAddress = getWifiIpAddress();
                    this.mWifiLocalIp = wifiIpAddress;
                    this.mIpServiceManager.ipRuleAdd("wlan0", wifiIpAddress);
                    return;
                }
                return;
            }
            this.mDeviceType = ICmcConnectivityController.DeviceType.SDevice;
            setCmcActivation(true);
            if (isReadyToWifiDirectRegister()) {
                imsRegister(WD_PDN_NAME, WD_PROFILE_NAME_SD);
                return;
            }
            return;
        }
        if (this.mWifiDirectIsConnect) {
            IMSLog.i(str2, "the Wifi-Direct are all disconnected");
            if (this.mDeviceType == ICmcConnectivityController.DeviceType.PDevice) {
                this.mIpServiceManager.ipRuleRemove("local_network", WD_HOST_PCSCF_IP);
                if (!TextUtils.isEmpty(this.mWifiLocalIp)) {
                    this.mIpServiceManager.ipRuleRemove("wlan0", this.mWifiLocalIp);
                }
            }
            imsDeregister();
        }
        this.mWifiDirectIsConnect = false;
        this.mWifiDirectAuthToken = "";
        this.mWifiLocalIp = "";
        this.mWifiDirectRegistered = false;
        this.mDeviceType = ICmcConnectivityController.DeviceType.None;
        setCmcActivation(false);
    }

    private void imsRegister(String str, String str2) {
        String str3 = LOG_TAG;
        IMSLog.i(str3, "imsRegister(), mDeviceType: " + this.mDeviceType);
        for (ImsProfile imsProfile : ImsProfileLoader.getProfileListWithMnoName(this.mContext, "MDMN", 0)) {
            if ("SamsungCMC_P2P".equals(imsProfile.getName())) {
                imsProfile.setDuid(this.mTelephonyManager.getImei());
                imsProfile.setPdn(str);
                imsProfile.setName(str2);
                imsProfile.setDomain(SERVER_DOMAIN);
                String str4 = LOG_TAG;
                Log.i(str4, "mPrimaryDuid: " + this.mPrimaryDuid);
                Log.i(str4, "mOwnDeviceId: " + this.mOwnDeviceId);
                if (this.mPrimaryDuid.isEmpty()) {
                    this.mPrimaryDuid = this.mWifiDirectAuthToken;
                }
                imsProfile.setPriDeviceIdWithURN(this.mPrimaryDuid);
                imsProfile.setDisplayName(this.mOwnDeviceId);
                imsProfile.setImpuList("sip:D2D@samsungims.com");
                imsProfile.setImpi("D2D@samsungims.com");
                imsProfile.setSslType(4);
                imsProfile.setNetworkEnabled(13, false);
                imsProfile.setNetworkEnabled(3, false);
                imsProfile.setNetworkEnabled(10, false);
                imsProfile.setNetworkEnabled(15, false);
                imsProfile.setNetworkEnabled(8, false);
                imsProfile.setNetworkEnabled(9, false);
                ArrayList arrayList = new ArrayList();
                arrayList.add(WD_HOST_PCSCF_IP);
                imsProfile.setPcscfList(arrayList);
                imsProfile.setAccessToken(this.mWifiDirectAuthToken);
                imsProfile.setId(imsProfile.getId() + 10000);
                this.mWifiDirectProfile = imsProfile;
                for (int i = 0; i < this.mPhoneCount; i++) {
                    this.mRm.registerProfile(this.mWifiDirectProfile, i);
                }
                return;
            }
        }
    }

    private void imsDeregister() {
        IMSLog.i(LOG_TAG, "imsDeregister()");
        if ((this.mWifiDirectIsConnect || this.mWifiDirectRegistered) && this.mWifiDirectProfile != null) {
            for (int i = 0; i < this.mPhoneCount; i++) {
                this.mRm.deregisterProfile(this.mWifiDirectProfile.getId(), i, false);
            }
            return;
        }
        this.mNeedP2pCallSession = false;
        this.mP2pCallSessionId = -1;
    }

    private boolean isReadyToWifiDirectRegister() {
        if (!isExistCalls()) {
            return true;
        }
        Log.e(LOG_TAG, "there are calls with [wifi], releaseP2pNetwork!");
        return false;
    }

    private boolean isExistCalls() {
        IVolteServiceModule volteServiceModule = ImsRegistry.getServiceModuleManager().getVolteServiceModule();
        if (volteServiceModule != null) {
            return volteServiceModule.hasActiveCall(SimUtil.getActiveDataPhoneId());
        }
        return false;
    }

    public void handleMessage(Message message) {
        if (message.what == 1001) {
            Log.i(LOG_TAG, "EVENT_SHUT_DOWN_P2P");
            onShutDownP2p();
        }
    }

    private String getWifiIpAddress() {
        try {
            for (T t : Collections.list(NetworkInterface.getNetworkInterfaces())) {
                if (t.getName().contains("wlan")) {
                    for (T t2 : Collections.list(t.getInetAddresses())) {
                        if (!t2.isLoopbackAddress() && NetworkUtil.isIPv4Address(t2.getHostAddress())) {
                            return t2.getHostAddress().toString();
                        }
                    }
                    continue;
                }
            }
        } catch (Exception unused) {
            IMSLog.i(LOG_TAG, "error in parsing");
        }
        IMSLog.i(LOG_TAG, "returning empty ip address");
        return "";
    }
}
