package com.sec.internal.imsphone.cmc;

import android.content.Context;
import android.content.SharedPreferences;
import android.os.Build;
import android.os.Handler;
import android.os.Looper;
import android.os.Message;
import android.provider.Settings;
import android.text.TextUtils;
import android.util.Log;
import com.sec.ims.IImsRegistrationListener;
import com.sec.ims.ImsRegistration;
import com.sec.ims.ImsRegistrationError;
import com.sec.ims.settings.ImsProfile;
import com.sec.ims.settings.ImsProfileLoader;
import com.sec.internal.constants.ims.ImsConstants;
import com.sec.internal.helper.ImsSharedPrefHelper;
import com.sec.internal.helper.NetworkUtil;
import com.sec.internal.helper.SimUtil;
import com.sec.internal.helper.os.ITelephonyManager;
import com.sec.internal.helper.os.TelephonyManagerWrapper;
import com.sec.internal.ims.registry.ImsRegistry;
import com.sec.internal.imsphone.cmc.ICmcConnectivityController;
import com.sec.internal.interfaces.ims.core.IRegistrationManager;
import com.sec.internal.log.IMSLog;
import java.net.NetworkInterface;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashSet;

public class CmcConnectivityController extends Handler implements ICmcConnectivityController {
    private static final boolean DBG = "eng".equals(Build.TYPE);
    private static final int EVENT_TRY_NSD_BIND = 1001;
    private static final int EVENT_TRY_NSD_BIND_DELAYED = 1002;
    private static final String IMS_PCSCF_IP = "ims_pcscf_ip";
    /* access modifiers changed from: private */
    public static final String LOG_TAG = CmcConnectivityController.class.getSimpleName();
    private static final int NSD_BIND_TIMEOUT = 10000;
    private static final String SERVER_DOMAIN = "p2p.samsungims.com";
    private static final String SIP_DOMAIN = "samsungims.com";
    private static final String URN_PREFIX = "urn:duid:";
    private static final String WIFI_HS_PCSCF_PREF = "mobile_hotspot_pcscf";
    private static final String WIFI_HS_PDN_NAME = "swlan";
    private static final String WIFI_HS_PROFILE_NAME_PD = "SamsungCMC_WIFI_HS_PD";
    private static final String WIFI_PDN_NAME = "wlan";
    private static final String WIFI_PROFILE_NAME_PD = "SamsungCMC_WIFI_PD";
    private static final String WIFI_PROFILE_NAME_SD = "SamsungCMC_WIFI_SD";
    public static boolean mWifiRegistered = false;
    private final Context mContext;
    private ICmcConnectivityController.DeviceType mDeviceType;
    private String mFirstTrigger;
    private String mHotspotAuthToken;
    /* access modifiers changed from: private */
    public String mHotspotHostPcscfIp;
    private boolean mHotspotIsConnect;
    private String mHotspotLocalIp;
    private ImsProfile mHotspotProfile;
    /* access modifiers changed from: private */
    public boolean mHotspotRegistered;
    /* access modifiers changed from: private */
    public IpServiceManager mIpServiceManager = null;
    private boolean mNsdBound;
    private String mOwnDeviceId;
    private String mOwnDuid;
    /* access modifiers changed from: private */
    public CmcP2pController mP2pController;
    /* access modifiers changed from: private */
    public int mPhoneCount = 0;
    private String mPrimaryDuid;
    private final IImsRegistrationListener mRegisterP2pListener;
    private int mRetryCountBound;
    /* access modifiers changed from: private */
    public IRegistrationManager mRm;
    private ITelephonyManager mTelephonyManager = null;
    private String mWifiAuthToken;
    private String mWifiHostPcscfIp;
    private boolean mWifiIsConnect;
    private String mWifiLocalIp;
    /* access modifiers changed from: private */
    public ImsProfile mWifiProfile;

    private void onWifiDirectConnectionChanged(boolean z, String str) {
    }

    public void handleMessage(Message message) {
    }

    public CmcConnectivityController(Looper looper, IRegistrationManager iRegistrationManager) {
        super(looper);
        ICmcConnectivityController.DeviceType deviceType = ICmcConnectivityController.DeviceType.None;
        this.mDeviceType = deviceType;
        this.mFirstTrigger = "";
        this.mOwnDeviceId = "";
        this.mOwnDuid = "";
        this.mPrimaryDuid = "";
        this.mNsdBound = false;
        this.mRetryCountBound = 0;
        this.mWifiIsConnect = false;
        this.mWifiLocalIp = "";
        this.mWifiHostPcscfIp = "";
        this.mWifiAuthToken = "";
        this.mWifiProfile = null;
        this.mHotspotIsConnect = false;
        this.mHotspotRegistered = false;
        this.mHotspotLocalIp = "";
        this.mHotspotHostPcscfIp = "";
        this.mHotspotAuthToken = "";
        this.mHotspotProfile = null;
        this.mRegisterP2pListener = new IImsRegistrationListener.Stub() {
            public void onRegistered(ImsRegistration imsRegistration) {
                int cmcType = imsRegistration.getImsProfile().getCmcType();
                String r0 = CmcConnectivityController.LOG_TAG;
                Log.i(r0, "onRegistered, cmcType: " + cmcType);
                int i = 0;
                if (cmcType == 2) {
                    String r5 = CmcConnectivityController.LOG_TAG;
                    IMSLog.i(r5, "cmc is registered, mWifiRegistered: " + CmcConnectivityController.mWifiRegistered);
                    if (CmcConnectivityController.mWifiRegistered && CmcConnectivityController.this.mWifiProfile != null) {
                        while (i < CmcConnectivityController.this.mPhoneCount) {
                            CmcConnectivityController.this.mRm.deregisterProfile(CmcConnectivityController.this.mWifiProfile.getId(), i, true);
                            i++;
                        }
                    }
                } else if (cmcType == 3) {
                    CmcConnectivityController.mWifiRegistered = true;
                } else if (cmcType == 4) {
                    CmcConnectivityController.mWifiRegistered = true;
                    if (CmcConnectivityController.this.mRm.isCmcRegistered(SimUtil.getActiveDataPhoneId()) > 0) {
                        IMSLog.i(CmcConnectivityController.LOG_TAG, "There is already cmc registration. deregister");
                        while (i < CmcConnectivityController.this.mPhoneCount) {
                            CmcConnectivityController.this.mRm.deregisterProfile(CmcConnectivityController.this.mWifiProfile.getId(), i, true);
                            i++;
                        }
                    }
                } else if (cmcType == 5) {
                    CmcConnectivityController.this.mHotspotRegistered = true;
                    String r52 = CmcConnectivityController.this.getHSPref();
                    if (!TextUtils.isEmpty(r52)) {
                        CmcConnectivityController.this.mIpServiceManager.ipRuleRemove("local_network", r52);
                    }
                    CmcConnectivityController.this.mIpServiceManager.ipRuleAdd("local_network", CmcConnectivityController.this.mHotspotHostPcscfIp);
                    CmcConnectivityController cmcConnectivityController = CmcConnectivityController.this;
                    cmcConnectivityController.setHSPref(cmcConnectivityController.mHotspotHostPcscfIp);
                } else if (cmcType == 7 || cmcType == 8) {
                    CmcConnectivityController.this.mP2pController.onRegistered(cmcType);
                }
            }

            public void onDeregistered(ImsRegistration imsRegistration, ImsRegistrationError imsRegistrationError) {
                int cmcType = imsRegistration.getImsProfile().getCmcType();
                Log.i(CmcConnectivityController.LOG_TAG, "onDeregistered, cmcType: " + cmcType + ", ErrorCode: " + imsRegistrationError.getSipErrorCode() + ", DeregistrationReason: " + imsRegistrationError.getDeregistrationReason());
                if (cmcType == 2) {
                    CmcConnectivityController.this.retryWifiRegister(CmcConnectivityController.WIFI_PROFILE_NAME_SD);
                } else if (cmcType == 3) {
                    CmcConnectivityController.mWifiRegistered = false;
                    CmcConnectivityController.this.retryWifiRegister(CmcConnectivityController.WIFI_PROFILE_NAME_PD);
                } else if (cmcType == 4) {
                    CmcConnectivityController.mWifiRegistered = false;
                    if (CmcConnectivityController.this.mWifiProfile == null) {
                        return;
                    }
                    if (imsRegistrationError.getDeregistrationReason() == 24 || imsRegistrationError.getDeregistrationReason() == 25) {
                        CmcConnectivityController.this.retryWifiRegister(CmcConnectivityController.WIFI_PROFILE_NAME_SD);
                        return;
                    }
                    for (int i = 0; i < CmcConnectivityController.this.mPhoneCount; i++) {
                        CmcConnectivityController.this.mRm.deregisterProfile(CmcConnectivityController.this.mWifiProfile.getId(), i, false);
                    }
                } else if (cmcType == 7 || cmcType == 8) {
                    CmcConnectivityController.this.mP2pController.onDeregistered(cmcType);
                }
            }
        };
        Context context = ImsRegistry.getContext();
        this.mContext = context;
        this.mRm = iRegistrationManager;
        this.mTelephonyManager = TelephonyManagerWrapper.getInstance(context);
        this.mIpServiceManager = new IpServiceManager(context);
        this.mP2pController = new CmcP2pController(looper, this, iRegistrationManager, this.mIpServiceManager);
        String string = Settings.Global.getString(context.getContentResolver(), "cmc_device_type");
        if ("pd".equals(string)) {
            this.mDeviceType = ICmcConnectivityController.DeviceType.PDevice;
        } else if ("sd".equals(string)) {
            this.mDeviceType = ICmcConnectivityController.DeviceType.SDevice;
        } else {
            this.mDeviceType = deviceType;
        }
        if (this.mDeviceType != deviceType) {
            registerP2pListener();
        }
        this.mPhoneCount = SimUtil.getPhoneCount();
        String str = LOG_TAG;
        IMSLog.i(str, "mPhoneCount: " + this.mPhoneCount);
    }

    public boolean isEnabledWifiDirectFeature() {
        return this.mP2pController.isEnabledWifiDirectFeature();
    }

    public void stopP2p() {
        this.mP2pController.stopP2p();
    }

    public void startP2pBind() {
        this.mP2pController.startP2pBind();
    }

    public ICmcConnectivityController.DeviceType getP2pDeviceType() {
        return this.mP2pController.getDeviceType();
    }

    public void needP2pCallSession(boolean z) {
        this.mP2pController.needP2pCallSession(z);
    }

    public int getP2pCallSessionId() {
        return this.mP2pController.getP2pCallSessionId();
    }

    public void setP2pCallSessionId(int i) {
        this.mP2pController.setP2pCallSessionId(i);
    }

    public void setCmcActivation(boolean z) {
        this.mP2pController.setCmcActivation(z);
    }

    public void setP2pPD() {
        this.mP2pController.setP2pPD();
    }

    public boolean isExistP2pConnection() {
        return this.mP2pController.isExistP2pConnection();
    }

    public void startNsdBind() {
        Log.i(LOG_TAG, "startNsdBind");
        sendEmptyMessage(1001);
    }

    public void startRegi(String str, String str2) {
        this.mOwnDeviceId = str;
        this.mOwnDuid = "urn:duid:" + str;
        this.mPrimaryDuid = "urn:duid:" + str2;
        String str3 = LOG_TAG;
        Log.i(str3, "ownDuid: " + this.mOwnDuid + ", primaryDuid: " + this.mPrimaryDuid + ", deviceType: " + this.mDeviceType);
        if (this.mDeviceType == ICmcConnectivityController.DeviceType.SDevice) {
            retryWifiRegister(WIFI_PROFILE_NAME_SD);
        }
    }

    public void stopRegi() {
        Log.i(LOG_TAG, "stopRegi, mWifiRegistered: " + mWifiRegistered);
        if (this.mDeviceType == ICmcConnectivityController.DeviceType.SDevice && mWifiRegistered) {
            for (int i = 0; i < this.mPhoneCount; i++) {
                this.mRm.deregisterProfile(this.mWifiProfile.getId(), i, true);
            }
            mWifiRegistered = false;
        }
    }

    public ICmcConnectivityController.DeviceType getDeviceType() {
        return this.mDeviceType;
    }

    private void onShutDownNsd(boolean z) {
        onWifiDirectConnectionChanged(false, "");
        onWifiConnectionChanged(false, "", "", (ArrayList<String>) null);
        if (z) {
            sendEmptyMessageDelayed(1002, 10000);
        }
    }

    private void onWifiConnectionChanged(boolean z, String str, String str2, ArrayList<String> arrayList) {
        if (this.mDeviceType != ICmcConnectivityController.DeviceType.None) {
            String str3 = LOG_TAG;
            IMSLog.i(str3, "onWifiConnectionChanged()");
            if (this.mDeviceType != ICmcConnectivityController.DeviceType.PDevice) {
                wifiConnectionChanged(z, str, str2);
            } else if (arrayList == null || arrayList.isEmpty()) {
                IMSLog.i(str3, "there are no network interface, all disconnect");
                hotspotConnectionChanged(false, str, str2);
                wifiConnectionChanged(false, str, str2);
            } else if (arrayList.size() > 1) {
                IMSLog.i(str3, "wifi register by priority (WIFI > MOBILE-HOTSPOT)");
                hotspotConnectionChanged(false, str, str2);
                wifiConnectionChanged(z, str, str2);
            } else {
                String str4 = arrayList.get(0);
                IMSLog.i(str3, "tryRegister intf: " + str4);
                if ("wlan0".equals(str4)) {
                    hotspotConnectionChanged(false, str, str2);
                    wifiConnectionChanged(z, str, str2);
                    return;
                }
                wifiConnectionChanged(false, str, str2);
                hotspotConnectionChanged(z, str, str2);
            }
        }
    }

    private void wifiConnectionChanged(boolean z, String str, String str2) {
        if (z) {
            this.mWifiIsConnect = true;
            this.mWifiHostPcscfIp = str;
            this.mWifiAuthToken = str2;
            if (this.mDeviceType == ICmcConnectivityController.DeviceType.PDevice) {
                ICmcConnectivityController.ConnectType connectType = ICmcConnectivityController.ConnectType.Wifi;
                this.mWifiHostPcscfIp = getIpAddress(connectType);
                if (isReadyToWifiPDRegister()) {
                    imsRegister(connectType, WIFI_PDN_NAME, WIFI_PROFILE_NAME_PD);
                }
            } else if (isReadyToWifiSDRegister()) {
                imsRegister(ICmcConnectivityController.ConnectType.Wifi, WIFI_PDN_NAME, WIFI_PROFILE_NAME_SD);
            }
        } else if (this.mWifiIsConnect) {
            IMSLog.i(LOG_TAG, "the Wifi are all disconnected");
            this.mWifiIsConnect = false;
            this.mWifiLocalIp = "";
            this.mWifiHostPcscfIp = "";
            this.mWifiAuthToken = "";
            if (mWifiRegistered && this.mWifiProfile != null) {
                for (int i = 0; i < this.mPhoneCount; i++) {
                    this.mRm.deregisterProfile(this.mWifiProfile.getId(), i, false);
                }
            }
            mWifiRegistered = false;
        }
    }

    private void hotspotConnectionChanged(boolean z, String str, String str2) {
        if (z) {
            this.mHotspotIsConnect = true;
            this.mHotspotAuthToken = str2;
            ICmcConnectivityController.ConnectType connectType = ICmcConnectivityController.ConnectType.Wifi_HS;
            this.mHotspotHostPcscfIp = getIpAddress(connectType);
            if (isReadyToHotspotRegister()) {
                imsRegister(connectType, WIFI_HS_PDN_NAME, WIFI_HS_PROFILE_NAME_PD);
            }
        } else if (this.mHotspotIsConnect) {
            IMSLog.i(LOG_TAG, "the Hotspot are all disconnected");
            if (!TextUtils.isEmpty(this.mHotspotHostPcscfIp)) {
                this.mIpServiceManager.ipRuleRemove("local_network", this.mHotspotHostPcscfIp);
            }
            this.mHotspotIsConnect = false;
            this.mHotspotLocalIp = "";
            this.mHotspotHostPcscfIp = "";
            this.mHotspotAuthToken = "";
            if (this.mHotspotRegistered && this.mHotspotProfile != null) {
                for (int i = 0; i < this.mPhoneCount; i++) {
                    this.mRm.deregisterProfile(this.mHotspotProfile.getId(), i, false);
                }
            }
            this.mHotspotRegistered = false;
        }
    }

    private void imsRegister(ICmcConnectivityController.ConnectType connectType, String str, String str2) {
        int i = 0;
        for (ImsProfile imsProfile : ImsProfileLoader.getProfileListWithMnoName(this.mContext, "MDMN", 0)) {
            if ("SamsungCMC_P2P".equals(imsProfile.getName())) {
                imsProfile.setDuid(this.mOwnDuid);
                imsProfile.setPdn(str);
                imsProfile.setName(str2);
                imsProfile.setDomain(SERVER_DOMAIN);
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
                imsProfile.setVceConfigEnabled(true);
                ArrayList arrayList = new ArrayList();
                if (connectType == ICmcConnectivityController.ConnectType.Wifi) {
                    arrayList.add(this.mWifiHostPcscfIp);
                    imsProfile.setPcscfList(arrayList);
                    imsProfile.setAccessToken(this.mWifiAuthToken);
                    this.mWifiProfile = imsProfile;
                    while (i < this.mPhoneCount) {
                        this.mRm.registerProfile(this.mWifiProfile, i);
                        i++;
                    }
                    return;
                } else if (connectType == ICmcConnectivityController.ConnectType.Wifi_HS) {
                    arrayList.add(this.mHotspotHostPcscfIp);
                    imsProfile.setPcscfList(arrayList);
                    imsProfile.setAccessToken(this.mHotspotAuthToken);
                    HashSet hashSet = new HashSet();
                    hashSet.add("mmtel");
                    imsProfile.setServiceSet(13, hashSet);
                    imsProfile.setNetworkEnabled(13, true);
                    this.mHotspotProfile = imsProfile;
                    while (i < this.mPhoneCount) {
                        this.mRm.registerProfile(this.mHotspotProfile, i);
                        i++;
                    }
                    return;
                } else {
                    return;
                }
            }
        }
    }

    private boolean isReadyToWifiRegister() {
        String str = LOG_TAG;
        IMSLog.i(str, "mWifiIsConnect: " + this.mWifiIsConnect + ", mWifiRegistered: " + mWifiRegistered);
        if (this.mWifiIsConnect && !mWifiRegistered && !TextUtils.isEmpty(this.mWifiHostPcscfIp)) {
            return true;
        }
        return false;
    }

    private boolean isReadyToWifiPDRegister() {
        if (!isReadyToWifiRegister()) {
            return false;
        }
        String str = LOG_TAG;
        IMSLog.i(str, "mHotspotRegistered: " + this.mHotspotRegistered);
        if (!this.mHotspotRegistered) {
            return true;
        }
        IMSLog.i(str, "There is already [mobile-hotspot] registration. don't wifi registration");
        return false;
    }

    private boolean isReadyToWifiSDRegister() {
        if (!isReadyToWifiRegister()) {
            return false;
        }
        if (this.mRm.isCmcRegistered(SimUtil.getActiveDataPhoneId()) <= 0) {
            return true;
        }
        IMSLog.i(LOG_TAG, "There is already cmc registration. don't wifi registration");
        return false;
    }

    private boolean isReadyToHotspotRegister() {
        String str = LOG_TAG;
        IMSLog.i(str, "mHotspotIsConnect: " + this.mHotspotIsConnect + ", mHotspotRegistered: " + this.mHotspotRegistered);
        if (!this.mHotspotIsConnect || this.mHotspotRegistered || TextUtils.isEmpty(this.mHotspotHostPcscfIp)) {
            return false;
        }
        IMSLog.i(str, "mWifiRegistered: " + mWifiRegistered);
        if (!mWifiRegistered) {
            return true;
        }
        IMSLog.i(str, "There is already [wifi] registration. don't mobile-hotspot registration");
        return false;
    }

    /* access modifiers changed from: private */
    public void retryWifiRegister(String str) {
        String str2 = LOG_TAG;
        IMSLog.i(str2, "retryWifiRegister: " + str);
        if (this.mDeviceType == ICmcConnectivityController.DeviceType.PDevice) {
            if (isReadyToWifiPDRegister()) {
                imsRegister(ICmcConnectivityController.ConnectType.Wifi, WIFI_PDN_NAME, str);
            }
        } else if (isReadyToWifiSDRegister()) {
            imsRegister(ICmcConnectivityController.ConnectType.Wifi, WIFI_PDN_NAME, str);
        }
    }

    /* access modifiers changed from: private */
    public String getHSPref() {
        return ImsSharedPrefHelper.getSharedPref(ImsConstants.Phone.SLOT_1, this.mContext, IMS_PCSCF_IP, 0, false).getString(WIFI_HS_PCSCF_PREF, "");
    }

    /* access modifiers changed from: private */
    public void setHSPref(String str) {
        SharedPreferences.Editor edit = ImsSharedPrefHelper.getSharedPref(ImsConstants.Phone.SLOT_1, this.mContext, IMS_PCSCF_IP, 0, false).edit();
        edit.putString(WIFI_HS_PCSCF_PREF, str);
        edit.apply();
        String str2 = LOG_TAG;
        IMSLog.i(str2, "setHSPref: " + str);
    }

    private void registerP2pListener() {
        Log.i(LOG_TAG, "registerP2pListener");
        try {
            this.mRm.registerP2pListener(this.mRegisterP2pListener);
        } catch (Exception unused) {
            Log.e(LOG_TAG, "registerP2pListener failed");
        }
    }

    private void unregisterImsRegistrationListener() {
        Log.i(LOG_TAG, "unregisterImsRegistrationListener");
    }

    private String getIpAddress(ICmcConnectivityController.ConnectType connectType) {
        try {
            for (T t : Collections.list(NetworkInterface.getNetworkInterfaces())) {
                if (connectType == ICmcConnectivityController.ConnectType.Wifi_HS) {
                    if (!t.getName().contains(WIFI_HS_PDN_NAME)) {
                    }
                } else if (connectType == ICmcConnectivityController.ConnectType.Wifi) {
                    if (!t.getName().contains(WIFI_PDN_NAME)) {
                    }
                } else if (connectType == ICmcConnectivityController.ConnectType.Internet && !t.getName().contains("rmnet0")) {
                }
                for (T t2 : Collections.list(t.getInetAddresses())) {
                    if (!t2.isLoopbackAddress() && NetworkUtil.isIPv4Address(t2.getHostAddress())) {
                        return t2.getHostAddress().toString();
                    }
                }
                continue;
            }
        } catch (Exception unused) {
            IMSLog.i(LOG_TAG, "error in parsing");
        }
        IMSLog.i(LOG_TAG, "returning empty ip address");
        return "";
    }

    public boolean isWifiRegistered() {
        return mWifiRegistered;
    }
}
