package com.sec.internal.ims.core.cmc;

import android.content.Context;
import android.content.SharedPreferences;
import android.content.pm.ApplicationInfo;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.os.Message;
import android.os.SemSystemProperties;
import android.telephony.PhoneNumberUtils;
import android.telephony.emergency.EmergencyNumber;
import android.text.TextUtils;
import android.util.Log;
import com.sec.ims.settings.ImsProfile;
import com.sec.ims.settings.ImsProfileLoader;
import com.sec.internal.constants.ims.ImsConstants;
import com.sec.internal.constants.ims.core.RegistrationConstants;
import com.sec.internal.helper.ImsSharedPrefHelper;
import com.sec.internal.helper.SimUtil;
import com.sec.internal.helper.SimpleEventLog;
import com.sec.internal.helper.os.DeviceUtil;
import com.sec.internal.helper.os.TelephonyManagerWrapper;
import com.sec.internal.ims.core.cmc.CmcConstants;
import com.sec.internal.ims.core.cmc.CmcInfo;
import com.sec.internal.ims.core.cmc.CmcInfoUpdateResult;
import com.sec.internal.ims.core.cmc.CmcSAManager;
import com.sec.internal.ims.registry.ImsRegistry;
import com.sec.internal.imsphone.cmc.ICmcConnectivityController;
import com.sec.internal.interfaces.ims.core.ICmcAccountManager;
import com.sec.internal.interfaces.ims.core.IRegisterTask;
import com.sec.internal.interfaces.ims.core.IRegistrationManager;
import com.sec.internal.interfaces.ims.core.ISequentialInitializable;
import com.sec.internal.interfaces.ims.servicemodules.volte2.IVolteServiceModule;
import com.sec.internal.log.IMSLog;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

public class CmcAccountManager implements ICmcAccountManager, CmcSAManager.CmcSAEventListener, ISequentialInitializable {
    private static final int EVENT_CMC_DEVICE_CHANGED = 5;
    private static final int EVENT_CMC_NW_PREF_CHANGED = 6;
    private static final int EVENT_SA_REQUEST = 1;
    private static final int EVENT_SA_REQUEST_EXPIRED = 7;
    private static final int EVENT_SA_REQUEST_FAILED = 8;
    private static final int EVENT_SA_UPDATE = 2;
    private static final int EVENT_START_CMC_REGISTRATION = 3;
    private static final int EVENT_STOP_CMC_REGISTRATION = 4;
    private static final String LOG_TAG = "CmcAccountManager";
    private static boolean mIsCmcServiceInstalled = true;
    private CmcInfo mCmcInfo = new CmcInfo();
    private CmcInfoUpdateResult mCmcInfoUpdatedResult = new CmcInfoUpdateResult();
    private CmcSettingManagerWrapper mCmcSetting;
    private Context mContext;
    private Map<Integer, List<String>> mEmergencyNumberMap = new ConcurrentHashMap();
    private SimpleEventLog mEventLog;
    private final InternalHandler mHandler;
    private boolean mIsCmcProfileAdded = false;
    private int mPhoneCount;
    private Map<Integer, ImsProfile> mProfileMap = new HashMap();
    private List<String> mRegiEventNotifyHostInfo = new ArrayList();
    private IRegistrationManager mRm;
    private int mSABindRetryCount = 0;
    private int mSARequestRetryCount = 0;
    private CmcSAManager mSaService;
    private String mSaToken = "";
    private String mSaUrl = "";

    private enum DeviceType {
        PD,
        SD;

        static boolean isPD(String str) {
            return PD.name().equalsIgnoreCase(str);
        }

        static boolean isSD(String str) {
            return SD.name().equalsIgnoreCase(str);
        }
    }

    public CmcAccountManager(Context context, Looper looper) {
        Log.i(LOG_TAG, "CmcAccountManager create");
        this.mContext = context;
        this.mEventLog = new SimpleEventLog(context, LOG_TAG, 100);
        this.mPhoneCount = SimUtil.getPhoneCount();
        this.mCmcSetting = CmcSettingManagerFactory.createCmcSettingManager(this.mContext, this);
        this.mSaService = new CmcSAManager(this.mContext, this, this);
        this.mHandler = new InternalHandler(looper);
        mIsCmcServiceInstalled = isCmcServiceInstalled();
        initCmcFromPref();
    }

    public void initSequentially() {
        this.mRm = ImsRegistry.getRegistrationManager();
        this.mCmcSetting.init();
        makeProfileMap();
        if (mIsCmcServiceInstalled && DeviceUtil.isWifiOnlyModel()) {
            Log.i(LOG_TAG, "initSequentially: start cmc registration for wifi only model");
            startDelayedCmcRegistration(5);
        }
    }

    private void makeProfileMap() {
        ImsProfile imsProfile;
        for (int i = 0; i < this.mPhoneCount; i++) {
            Iterator it = ImsProfileLoader.getProfileListWithMnoName(this.mContext, "MDMN", i).iterator();
            while (true) {
                if (!it.hasNext()) {
                    imsProfile = null;
                    break;
                }
                imsProfile = (ImsProfile) it.next();
                if (CmcConstants.Profile.DEFAULT_NAME.equalsIgnoreCase(imsProfile.getName())) {
                    break;
                }
            }
            if (imsProfile == null) {
                Log.i(LOG_TAG, "makeProfileMap: No pre-defined profile slot: " + i);
            } else {
                Log.i(LOG_TAG, "makeProfileMap: CMC profile found slot: " + i);
                if (isSecondaryDevice()) {
                    int[] iArr = {6, 5, 12, 14};
                    HashSet hashSet = new HashSet();
                    hashSet.add("mmtel");
                    for (int i2 = 0; i2 < 4; i2++) {
                        int i3 = iArr[i2];
                        imsProfile.setServiceSet(i3, hashSet);
                        imsProfile.setNetworkEnabled(i3, true);
                    }
                }
                this.mProfileMap.put(Integer.valueOf(i), imsProfile);
            }
        }
    }

    public void startCmcRegistration() {
        this.mHandler.sendEmptyMessage(3);
    }

    private void startDelayedCmcRegistration(int i) {
        this.mHandler.sendEmptyMessageDelayed(3, (long) (i * 1000));
    }

    private void stopCmcRegistration() {
        this.mHandler.sendEmptyMessage(4);
    }

    public void notifyCmcDeviceChanged() {
        if (!this.mHandler.hasMessages(5)) {
            this.mHandler.sendEmptyMessage(5);
        }
    }

    public void notifyCmcNwPrefChanged() {
        if (this.mHandler.hasMessages(6)) {
            this.mHandler.removeMessages(6);
        }
        this.mHandler.sendEmptyMessageDelayed(6, 600);
    }

    /* access modifiers changed from: protected */
    public void onCmcDeviceChanged() {
        IRegistrationManager registrationManager = ImsRegistry.getRegistrationManager();
        if (registrationManager == null) {
            IMSLog.e(LOG_TAG, "onCmcDeviceChanged: RegistrationManagerBase is null");
            return;
        }
        int i = 0;
        while (i < this.mPhoneCount) {
            IRegisterTask cmcRegisterTask = getCmcRegisterTask(i);
            if (cmcRegisterTask == null || cmcRegisterTask.getState() != RegistrationConstants.RegisterTaskState.DEREGISTERING) {
                i++;
            } else {
                IMSLog.i(LOG_TAG, i, "onCmcDeviceChanged: deregistering");
                return;
            }
        }
        int cmcPhoneId = getCmcPhoneId();
        IRegisterTask cmcRegisterTask2 = getCmcRegisterTask(cmcPhoneId);
        if (cmcRegisterTask2 != null) {
            updateCmcProfile();
            if (!isCmcRegistrationRequired()) {
                IMSLog.i(LOG_TAG, cmcPhoneId, "onCmcDeviceChanged: stopCmcRegistration");
                stopCmcRegistration();
            } else if (this.mCmcInfoUpdatedResult.isNotUpdated()) {
                IMSLog.i(LOG_TAG, cmcPhoneId, "onCmcDeviceChanged: Not updated");
            } else {
                this.mRm.releaseThrottleByCmc(cmcRegisterTask2);
                int i2 = (!DeviceType.isPD(this.mCmcInfo.mDeviceType) || !this.mCmcInfoUpdatedResult.isUpdated()) ? cmcPhoneId : this.mCmcInfo.mLineSlotIndex;
                if (cmcRegisterTask2.isOneOf(RegistrationConstants.RegisterTaskState.REGISTERED, RegistrationConstants.RegisterTaskState.REGISTERING)) {
                    if (needDeregiOnDeviceChange(cmcRegisterTask2)) {
                        cmcRegisterTask2.setReason("CMC profile updated");
                        cmcRegisterTask2.setDeregiReason(29);
                        boolean z = i2 != cmcPhoneId;
                        this.mEventLog.logAndAdd("onCmcDeviceChanged: deregister slot[" + cmcPhoneId + "] local: " + z);
                        registrationManager.deregister(cmcRegisterTask2, z, false, "CMC profile updated");
                    }
                } else if (cmcRegisterTask2.isOneOf(RegistrationConstants.RegisterTaskState.CONNECTING, RegistrationConstants.RegisterTaskState.CONNECTED)) {
                    this.mEventLog.logAndAdd("onCmcDeviceChanged: stopPdn slot[" + cmcPhoneId + "]");
                    registrationManager.stopPdnConnectivity(cmcRegisterTask2.getPdnType(), cmcRegisterTask2);
                    cmcRegisterTask2.setState(RegistrationConstants.RegisterTaskState.IDLE);
                }
                IVolteServiceModule volteServiceModule = ImsRegistry.getServiceModuleManager().getVolteServiceModule();
                if (volteServiceModule != null) {
                    Log.i(LOG_TAG, "onCmcDeviceChanged: update lineId and deviceId for p2p");
                    volteServiceModule.getCmcServiceHelper().setP2pServiceInfo(CmcConstants.URN_PREFIX + this.mCmcInfo.mDeviceId, this.mCmcInfo.mLineId);
                }
                registrationManager.requestTryRegsiter(i2, 500);
            }
        } else {
            IMSLog.i(LOG_TAG, cmcPhoneId, "onCmcDeviceChanged: startCmcRegistration");
            startCmcRegistration();
        }
    }

    public void onSimRefresh(int i) {
        SimpleEventLog simpleEventLog = this.mEventLog;
        simpleEventLog.logAndAdd("onSimRefresh(" + i + ")");
        if (!this.mIsCmcProfileAdded) {
            return;
        }
        if (getCmcRegisterTask(i) != null) {
            IMSLog.i(LOG_TAG, "onSimRefresh: RegisterTask is already in the slot [" + i + "]");
            return;
        }
        registerProfile(i);
    }

    /* access modifiers changed from: private */
    public void onStartCmcRegistration() {
        if (!mIsCmcServiceInstalled) {
            this.mEventLog.logAndAdd("onStartCmcRegistration: Cmc service not installed");
        } else if (this.mIsCmcProfileAdded) {
            IMSLog.i(LOG_TAG, "onStartCmcRegistration: Cmc Profile is already added");
        } else if (!isCmcRegistrationRequired()) {
            IMSLog.i(LOG_TAG, "onStartCmcRegistration: CMC registration is not required");
        } else {
            IVolteServiceModule volteServiceModule = ImsRegistry.getServiceModuleManager().getVolteServiceModule();
            if (volteServiceModule != null && !volteServiceModule.isRunning()) {
                Log.i(LOG_TAG, "Start VoLteService");
                volteServiceModule.start();
            }
            updateCmcProfile();
            if (this.mCmcInfoUpdatedResult.isFailed()) {
                IMSLog.i(LOG_TAG, "onStartCmcRegistration: updateCmcProfile failed");
                return;
            }
            for (int i = 0; i < this.mPhoneCount; i++) {
                if (getCmcRegisterTask(i) != null) {
                    IMSLog.i(LOG_TAG, i, "onStartCmcRegistration: manual deregister is ongoing");
                    return;
                }
            }
            this.mEventLog.logAndAdd("onStartCmcRegistration: registerProfile CMC: same WiFi: " + isSupportSameWiFiOnly() + ", ecall: " + isEmergencyCallSupported());
            for (int i2 = 0; i2 < this.mPhoneCount; i2++) {
                registerProfile(i2);
            }
            this.mIsCmcProfileAdded = true;
            if (volteServiceModule != null) {
                Log.i(LOG_TAG, "onStartCmcRegistration: update lineId and deviceId for p2p");
                volteServiceModule.getCmcServiceHelper().setP2pServiceInfo(CmcConstants.URN_PREFIX + this.mCmcInfo.mDeviceId, this.mCmcInfo.mLineId);
            }
        }
    }

    private boolean isCmcRegistrationRequired() {
        if (!isCmcActivated()) {
            IMSLog.i(LOG_TAG, "isCmcRegistrationRequired: CMC not activated");
            return false;
        } else if (hasCallForkingService()) {
            return true;
        } else {
            IMSLog.i(LOG_TAG, "isCmcRegistrationRequired: CMC Call forking disabled");
            return false;
        }
    }

    private void registerProfile(int i) {
        if (isReadyRegisterP2p()) {
            IMSLog.i(LOG_TAG, "registerProfile: ready to D2D register");
            ICmcConnectivityController iCmcConnectivityController = ImsRegistry.getICmcConnectivityController();
            CmcInfo cmcInfo = this.mCmcInfo;
            iCmcConnectivityController.startRegi(cmcInfo.mDeviceId, cmcInfo.mLineOwnerDeviceId);
            return;
        }
        IMSLog.i(LOG_TAG, "registerProfile(" + i + ")");
        ImsProfile profile = getProfile(i);
        if (profile != null) {
            ImsRegistry.getRegistrationManager().registerProfile(profile, i);
        }
    }

    private boolean isReadyRegisterP2p() {
        ApplicationInfo applicationInfo;
        Bundle bundle;
        try {
            PackageManager packageManager = this.mContext.getPackageManager();
            if (!(packageManager == null || (applicationInfo = packageManager.getApplicationInfo(CmcConstants.SERVICE_PACKAGE_NAME, 128)) == null || (bundle = applicationInfo.metaData) == null)) {
                return bundle.getBoolean("d2d_trial", false);
            }
        } catch (PackageManager.NameNotFoundException e) {
            Log.d(LOG_TAG, e.toString());
        }
        return false;
    }

    /* access modifiers changed from: private */
    public void onStopCmcRegistration() {
        if (!this.mIsCmcProfileAdded) {
            IMSLog.i(LOG_TAG, "onStopCmcRegistration: no profile added");
            return;
        }
        for (int i = 0; i < this.mPhoneCount; i++) {
            if (getCmcRegisterTask(i) == null) {
                IMSLog.i(LOG_TAG, i, "onStopCmcRegistration: manual register is ongoing");
                return;
            }
        }
        this.mEventLog.logAndAdd("onStopCmcRegistration: deregisterProfile: activation[" + isCmcActivated() + "] isSD[" + isSecondaryDevice() + "] hasCallForking[" + hasCallForkingService() + "]");
        if (!isReadyRegisterP2p() || isSdHasCallForkingService()) {
            for (int i2 = 0; i2 < this.mPhoneCount; i2++) {
                ImsProfile profile = getProfile(i2);
                if (profile != null) {
                    ImsRegistry.getRegistrationManager().deregisterProfile(profile.getId(), i2);
                }
            }
            this.mIsCmcProfileAdded = false;
            return;
        }
        ImsRegistry.getICmcConnectivityController().stopRegi();
        this.mIsCmcProfileAdded = false;
    }

    public void setPcscfList() {
        int i;
        List<String> list = this.mCmcInfo.mPcscfAddrList;
        if (list == null || list.isEmpty()) {
            Log.e(LOG_TAG, "setPcscfList: PcscfAddrList is empty");
        } else if (this.mProfileMap.isEmpty()) {
            Log.e(LOG_TAG, "setPcscfList: mProfileMap is empty");
        } else {
            List<String> list2 = this.mCmcInfo.mPcscfAddrList;
            ArrayList arrayList = new ArrayList();
            StringBuilder sb = new StringBuilder();
            int i2 = 8000;
            for (String next : list2) {
                if (next.lastIndexOf(":") > 0) {
                    i = Integer.valueOf(next.substring(next.lastIndexOf(":") + 1)).intValue();
                    next = next.substring(0, next.lastIndexOf(":"));
                } else {
                    i = 8000;
                }
                sb.append("(pcscf = ");
                sb.append(next);
                sb.append(" / port = ");
                sb.append(i);
                sb.append(")");
                arrayList.add(next);
                i2 = i;
            }
            Log.i(LOG_TAG, "pcscfList size[" + arrayList.size() + "] : " + sb);
            for (ImsProfile next2 : this.mProfileMap.values()) {
                next2.setPcscfList(arrayList);
                next2.setSipPort(i2);
            }
        }
    }

    private void initProfile() {
        String str;
        String str2;
        Log.i(LOG_TAG, "initProfile: build ImsProfile for CMC");
        String str3 = this.mCmcInfo.mLineImpu;
        String str4 = CmcConstants.URN_PREFIX + this.mCmcInfo.mDeviceId;
        String str5 = CmcConstants.URN_PREFIX + this.mCmcInfo.mLineOwnerDeviceId;
        String str6 = DeviceType.isPD(this.mCmcInfo.mDeviceType) ? CmcConstants.Profile.PD_NAME : CmcConstants.Profile.SD_NAME;
        String substring = str3.contains("sip:") ? str3.substring(str3.lastIndexOf(":") + 1) : str3;
        if (substring.indexOf("@") > 0) {
            str = substring.substring(0, substring.indexOf("@"));
            str2 = substring.substring(substring.lastIndexOf("@") + 1);
            Log.i(LOG_TAG, "initProfile: password = " + str + " / domain = " + str2);
        } else {
            str2 = "";
            str = substring;
        }
        for (ImsProfile next : this.mProfileMap.values()) {
            next.setName(str6);
            next.setSipPort(8000);
            next.setPassword(str);
            next.setDomain(str2);
            next.setVceConfigEnabled(true);
            next.setDuid(str4);
            next.setAccessToken(this.mCmcInfo.mAccessToken);
            next.setPriDeviceIdWithURN(str5);
            next.setDisplayName(this.mCmcInfo.mDeviceId);
            next.setImpi(substring);
            next.setImpuList(str3);
            ArrayList arrayList = new ArrayList();
            arrayList.add(str3);
            next.setExtImpuList(arrayList);
        }
    }

    private ImsProfile getProfile(int i) {
        ImsProfile imsProfile = this.mProfileMap.get(Integer.valueOf(i));
        if (imsProfile == null) {
            Log.e(LOG_TAG, "mProfile is null");
            return null;
        }
        Log.i(LOG_TAG, "mProfile = " + imsProfile);
        return imsProfile;
    }

    private String getCmcRelayType() {
        return isSupportSameWiFiOnly() ? "priv-p2p" : "";
    }

    private void updateCmcProfile() {
        CmcInfo cmcInfo = this.mCmcInfo;
        this.mCmcInfo = getCmcInfo();
        this.mCmcInfoUpdatedResult.clearChangedCmcInfoList();
        if (!isCmcInfoValid(this.mCmcInfo)) {
            SimpleEventLog simpleEventLog = this.mEventLog;
            simpleEventLog.logAndAdd("updateCmcProfile: Invalid CmcInfo: " + this.mCmcInfoUpdatedResult.getProfileUpdateReason());
            this.mCmcInfoUpdatedResult.setProfileUpdatedResult(CmcInfoUpdateResult.ProfileUpdateResult.FAILED);
        } else if (isCmcInfoEqual(cmcInfo, this.mCmcInfo)) {
            Log.i(LOG_TAG, "updateCmcProfile: Same CmcInfo");
            this.mCmcInfoUpdatedResult.setProfileUpdatedResult(CmcInfoUpdateResult.ProfileUpdateResult.NOT_UPDATED);
        } else {
            initProfile();
            setPcscfList();
            SimpleEventLog simpleEventLog2 = this.mEventLog;
            simpleEventLog2.logAndAdd("updateCmcProfile: Update CmcInfo: Line[" + this.mCmcInfo.mLineSlotIndex + "] " + this.mCmcInfoUpdatedResult.getProfileUpdateReason());
            this.mCmcInfoUpdatedResult.setProfileUpdatedResult(CmcInfoUpdateResult.ProfileUpdateResult.UPDATED);
        }
    }

    public boolean isCmcProfileAdded() {
        return this.mIsCmcProfileAdded;
    }

    private boolean isCmcInfoValid(CmcInfo cmcInfo) {
        String str;
        if (cmcInfo != null) {
            CmcInfo.CmcInfoType[] values = CmcInfo.CmcInfoType.values();
            int length = values.length;
            int i = 0;
            while (true) {
                if (i >= length) {
                    str = "";
                    break;
                }
                CmcInfo.CmcInfoType cmcInfoType = values[i];
                if (!cmcInfo.checkValid(cmcInfoType)) {
                    str = cmcInfoType.name() + " empty";
                    break;
                }
                i++;
            }
        } else {
            str = "OwnDeviceInfo null";
        }
        if (!str.isEmpty()) {
            Log.i(LOG_TAG, "isCmcInfoValid: fail - " + str);
            this.mCmcInfoUpdatedResult.setProfileUpdateReason(str);
            return false;
        }
        IMSLog.s(LOG_TAG, "isCmcInfoValid: true " + cmcInfo.toString());
        return true;
    }

    private boolean isCmcInfoEqual(CmcInfo cmcInfo, CmcInfo cmcInfo2) {
        if (cmcInfo == null) {
            return false;
        }
        StringBuilder sb = new StringBuilder();
        for (CmcInfo.CmcInfoType cmcInfoType : CmcInfo.CmcInfoType.values()) {
            if (!cmcInfo.compare(cmcInfoType, cmcInfo2)) {
                sb.append(cmcInfoType.name());
                if (cmcInfoType.isDumpPrintAvailable()) {
                    sb.append("[" + cmcInfo2.getValue(cmcInfoType) + "]");
                }
                sb.append(", ");
                this.mCmcInfoUpdatedResult.addChangedCmcInfo(cmcInfoType);
            }
        }
        if (sb.toString().isEmpty()) {
            return true;
        }
        if (this.mCmcInfoUpdatedResult.isFailed()) {
            sb.delete(0, sb.length());
            sb.append("New valid CmcInfo ");
        }
        Log.i(LOG_TAG, "isCmcInfoEqual: false - " + sb.toString());
        this.mCmcInfoUpdatedResult.setProfileUpdateReason(sb.toString());
        return false;
    }

    private CmcInfo getCmcInfo() {
        CmcInfo cmcInfo = new CmcInfo();
        cmcInfo.mOobe = this.mCmcSetting.getCmcSupported();
        cmcInfo.mActivation = this.mCmcSetting.getOwnCmcActivation();
        cmcInfo.mDeviceType = this.mCmcSetting.getDeviceType();
        cmcInfo.mDeviceId = this.mCmcSetting.getDeviceId();
        cmcInfo.mAccessToken = getAccessTokenFromCmcPref();
        cmcInfo.mLineId = this.mCmcSetting.getLineId();
        cmcInfo.mLineOwnerDeviceId = getPrimaryDeviceId();
        cmcInfo.mLineImpu = getImpuFromLineId();
        cmcInfo.mPcscfAddrList = this.mCmcSetting.getPcscfAddressList();
        cmcInfo.mSaServerUrl = this.mSaUrl;
        cmcInfo.mLineSlotIndex = getLineSlotId();
        cmcInfo.mHasSd = hasSecondaryDevice();
        cmcInfo.mNetworkPref = this.mCmcSetting.getPreferredNetwork();
        cmcInfo.mCallforkingEnabled = isCallAllowedSdByPd(cmcInfo.mDeviceId);
        cmcInfo.mIsEmergencyCallSupported = this.mCmcSetting.isEmergencyCallSupported();
        cmcInfo.mIsSameWiFiOnly = this.mCmcSetting.isSameWifiNetworkOnly();
        cmcInfo.mIsDualCmc = this.mCmcSetting.isDualCmc();
        IMSLog.s(LOG_TAG, "getCmcInfo: LineId: " + cmcInfo.mLineId + ", PcscfAddrList: " + cmcInfo.mPcscfAddrList);
        return cmcInfo;
    }

    public boolean isCmcActivated() {
        return this.mCmcSetting.getOwnCmcActivation();
    }

    private boolean hasCallForkingService() {
        return isSecondaryDevice() ? isSdHasCallForkingService() : isPdHasCallForkingService();
    }

    private boolean isPdHasCallForkingService() {
        if (getCmcCallActivation(getPrimaryDeviceId())) {
            return true;
        }
        this.mEventLog.logAndAdd("isPdHasCallForkingService: PD CmcCallActivation false");
        return false;
    }

    private boolean isSdHasCallForkingService() {
        String deviceId = this.mCmcSetting.getDeviceId();
        if (deviceId == null) {
            this.mEventLog.logAndAdd("isSdHasCallForkingService: deviceId is null");
            return false;
        } else if (!isCallAllowedSdByPd(deviceId)) {
            this.mEventLog.logAndAdd("isSdHasCallForkingService: isCallAllowedSdByPd false");
            return false;
        } else if (!getCmcCallActivation(deviceId)) {
            this.mEventLog.logAndAdd("isSdHasCallForkingService: Device CmcCallActivation false");
            return false;
        } else if (isPdHasCallForkingService()) {
            return true;
        } else {
            this.mEventLog.logAndAdd("isSdHasCallForkingService: PD CmcCallActivation false");
            return false;
        }
    }

    private boolean isCallAllowedSdByPd(String str) {
        return this.mCmcSetting.isCallAllowedSdByPd(str);
    }

    private boolean getCmcCallActivation(String str) {
        return this.mCmcSetting.getCmcCallActivation(str);
    }

    private String getImpuFromLineId() {
        String lineImpu = this.mCmcSetting.getLineImpu();
        IMSLog.s(LOG_TAG, "getImpuFromLineId: " + lineImpu);
        return lineImpu == null ? "" : lineImpu;
    }

    private String getPrimaryDeviceId() {
        String str;
        List<String> deviceIdList = this.mCmcSetting.getDeviceIdList();
        if (deviceIdList != null && !deviceIdList.isEmpty()) {
            Iterator<String> it = deviceIdList.iterator();
            while (true) {
                if (!it.hasNext()) {
                    break;
                }
                str = it.next();
                String deviceTypeWithDeviceId = this.mCmcSetting.getDeviceTypeWithDeviceId(str);
                if (!TextUtils.isEmpty(deviceTypeWithDeviceId) && DeviceType.isPD(deviceTypeWithDeviceId)) {
                    break;
                }
            }
            IMSLog.s(LOG_TAG, "getPrimaryDeviceId: " + str);
            return str;
        }
        str = "";
        IMSLog.s(LOG_TAG, "getPrimaryDeviceId: " + str);
        return str;
    }

    private int getLineSlotId() {
        List<Integer> selectedSimSlotsOnPd = this.mCmcSetting.getSelectedSimSlotsOnPd();
        Log.i(LOG_TAG, "getLineSlotId: selectedSimSlotOnPd: " + selectedSimSlotsOnPd);
        int i = 0;
        if (selectedSimSlotsOnPd != null && selectedSimSlotsOnPd.size() == 1) {
            i = selectedSimSlotsOnPd.get(0).intValue();
        }
        Log.i(LOG_TAG, "getLineSlotId: lineSlotId: " + i);
        return i;
    }

    public int getCurrentLineSlotIndex() {
        return this.mCmcInfo.mLineSlotIndex;
    }

    public String getCurrentLineOwnerDeviceId() {
        return this.mCmcInfo.mLineOwnerDeviceId;
    }

    public boolean hasSecondaryDevice() {
        String deviceType = this.mCmcSetting.getDeviceType();
        List<String> deviceIdList = this.mCmcSetting.getDeviceIdList();
        if (!DeviceType.isPD(deviceType) || deviceIdList == null || deviceIdList.size() > 1) {
            return true;
        }
        Log.i(LOG_TAG, "hasSecondaryDevice : no SD with current PD");
        return false;
    }

    public boolean isWifiOnly() {
        return this.mCmcInfo.mNetworkPref == 1;
    }

    public void startSAService(boolean z) {
        if (!this.mHandler.hasMessages(1)) {
            SimpleEventLog simpleEventLog = this.mEventLog;
            simpleEventLog.logAndAdd("startSAService: request SA, isLocal: " + z);
            InternalHandler internalHandler = this.mHandler;
            internalHandler.sendMessage(internalHandler.obtainMessage(1, Boolean.valueOf(z)));
        }
    }

    /* access modifiers changed from: private */
    public void startSAServiceInternal(boolean z) {
        resetSARetryCount();
        this.mSaService.connectToSamsungAccountService(z);
    }

    public void onChangedSamsungAccountInfo(String str) {
        if (!isCmcProfileAdded()) {
            return;
        }
        if (!this.mSaService.isSAServiceIdle()) {
            IMSLog.i(LOG_TAG, "onChangedSamsungAccountInfo: SA service not IDLE state");
            return;
        }
        IRegisterTask cmcRegisterTask = getCmcRegisterTask(getCmcPhoneId());
        if (cmcRegisterTask != null && !cmcRegisterTask.isOneOf(RegistrationConstants.RegisterTaskState.REGISTERED, RegistrationConstants.RegisterTaskState.IDLE)) {
            IMSLog.i(LOG_TAG, "onChangedSamsungAccountInfo: CmcTask is NOT REGISTERED or IDLE state");
        } else if (!TextUtils.isEmpty(str) && !str.equals(getAccessTokenFromCmcPref())) {
            IMSLog.i(LOG_TAG, "onChangedSamsungAccountInfo: startSAService");
            startSAService(true);
        }
    }

    public boolean isCmcEnabled() {
        if (!mIsCmcServiceInstalled) {
            IMSLog.i(LOG_TAG, "isCmcEnabled: Not installed ");
            return false;
        }
        boolean isCmcActivated = isCmcActivated();
        IMSLog.i(LOG_TAG, "isCmcEnabled: CmcActivated: " + isCmcActivated);
        return isCmcActivated;
    }

    public boolean isSecondaryDevice() {
        CmcSettingManagerWrapper cmcSettingManagerWrapper = this.mCmcSetting;
        if (cmcSettingManagerWrapper == null) {
            IMSLog.e(LOG_TAG, "isSecondaryDevice : cmcsetting is null");
            return false;
        } else if (DeviceType.isSD(cmcSettingManagerWrapper.getDeviceType())) {
            IMSLog.i(LOG_TAG, "isSecondaryDevice: by cmcsetting");
            return true;
        } else if (!DeviceType.isSD(SemSystemProperties.get(CmcConstants.SystemProperties.CMC_DEVICE_TYPE_PROP, ""))) {
            return false;
        } else {
            IMSLog.i(LOG_TAG, "isSecondaryDevice: by prop");
            return true;
        }
    }

    public List<String> getRegiEventNotifyHostInfo() {
        return this.mRegiEventNotifyHostInfo;
    }

    public void setRegiEventNotifyHostInfo(List<String> list) {
        this.mRegiEventNotifyHostInfo = list;
    }

    /* access modifiers changed from: private */
    public void onSaUpdated() {
        IMSLog.i(LOG_TAG, "onSaUpdated: ");
        updateCmcPref();
        if (!this.mHandler.hasMessages(2)) {
            if (!this.mIsCmcProfileAdded) {
                startCmcRegistration();
                return;
            }
            IMSLog.i(LOG_TAG, "onSaUpdated: notifyCmcDeviceChanged with access token");
            notifyCmcDeviceChanged();
        }
    }

    private SharedPreferences getSharedPreference() {
        return ImsSharedPrefHelper.getSharedPref(ImsConstants.Phone.SLOT_1, this.mContext, CmcConstants.SA.ACCOUNT_SP, 0, false);
    }

    private String getSharedPrefString(String str, String str2) {
        SharedPreferences sharedPreference = getSharedPreference();
        return sharedPreference == null ? str2 : sharedPreference.getString(str, str2);
    }

    private void initCmcFromPref() {
        this.mSaToken = getSharedPrefString(CmcConstants.SA.TOKEN_SP, CmcConstants.SA.TOKEN_DEFAULT);
        this.mSaUrl = getSharedPrefString(CmcConstants.SA.URL_SP, CmcConstants.SA.URL_DEFAULT);
        IMSLog.i(LOG_TAG, "initCmcFromPref: ");
    }

    private void updateCmcPref() {
        SharedPreferences sharedPreference = getSharedPreference();
        if (sharedPreference == null) {
            IMSLog.e(LOG_TAG, "updateCmcPref: sp is null");
            return;
        }
        SharedPreferences.Editor edit = sharedPreference.edit();
        edit.putString(CmcConstants.SA.TOKEN_SP, this.mSaToken);
        edit.putString(CmcConstants.SA.URL_SP, this.mSaUrl);
        edit.apply();
        IMSLog.s(LOG_TAG, "updateCmcPref: token: " + this.mSaToken + ", SaUrl: " + this.mSaUrl);
    }

    public String getAccessTokenFromCmcPref() {
        String sharedPrefString = getSharedPrefString(CmcConstants.SA.TOKEN_SP, CmcConstants.SA.TOKEN_DEFAULT);
        IMSLog.s(LOG_TAG, "getAccessTokenFromCmcPref: token: " + sharedPrefString);
        return sharedPrefString;
    }

    private int getCmcPhoneId() {
        return (this.mCmcInfo.mLineSlotIndex == -1 || isSecondaryDevice()) ? SimUtil.getActiveDataPhoneId() : this.mCmcInfo.mLineSlotIndex;
    }

    public IRegisterTask getCmcRegisterTask(int i) {
        List<IRegisterTask> pendingRegistration = ImsRegistry.getRegistrationManager().getPendingRegistration(i);
        if (pendingRegistration == null) {
            IMSLog.e(LOG_TAG, i, "getCmcRegisterTask: rtl is null");
            return null;
        }
        for (IRegisterTask next : pendingRegistration) {
            if (isCmcProfile(next.getProfile())) {
                return next;
            }
        }
        return null;
    }

    public boolean isProfileUpdateFailed() {
        return this.mCmcInfoUpdatedResult.isFailed();
    }

    public Bundle getCmcRegiConfigForUserAgent() {
        Bundle bundle = new Bundle();
        String str = this.mCmcInfo.mSaServerUrl;
        String cmcRelayType = getCmcRelayType();
        String emergencyCallNumberString = getEmergencyCallNumberString();
        bundle.putString("saServerUrl", str);
        bundle.putString("relayType", cmcRelayType);
        bundle.putString("eCallNum", emergencyCallNumberString);
        IMSLog.i(LOG_TAG, "getCmcRegiConfigForUserAgent: SA url: " + IMSLog.checker(str) + ", relayType: " + cmcRelayType + ", ecallnumlist: " + IMSLog.checker(emergencyCallNumberString));
        return bundle;
    }

    public boolean isSupportSameWiFiOnly() {
        if ("1".equals(SemSystemProperties.get("persist.cmc.enable_cmc30", ""))) {
            return true;
        }
        return this.mCmcSetting.isSameWifiNetworkOnly();
    }

    public boolean isSupportDualSimCMC() {
        if ("1".equals(SemSystemProperties.get("persist.cmc.enable_dualsim_cmc", ""))) {
            return true;
        }
        IMSLog.i(LOG_TAG, "isSupportDualSimCMC: " + this.mCmcInfo.mIsDualCmc);
        return this.mCmcInfo.mIsDualCmc;
    }

    public boolean isEmergencyCallSupported() {
        if ("1".equals(SemSystemProperties.get("persist.cmc.enable_cmc30", ""))) {
            return true;
        }
        return this.mCmcSetting.isEmergencyCallSupported();
    }

    public void setEmergencyNumbers(String str) {
        String str2;
        if (TextUtils.isEmpty(str)) {
            IMSLog.i(LOG_TAG, "setEmergencyNumbers: no numbers");
            return;
        }
        this.mEmergencyNumberMap.clear();
        String replace = str.replace(CmcConstants.E_NUM_STR_QUOTE, "");
        if (replace.contains(CmcConstants.E_NUM_SLOT_SPLIT)) {
            List asList = Arrays.asList(replace.split(CmcConstants.E_NUM_SLOT_SPLIT));
            int[] iArr = {ImsConstants.Phone.SLOT_1, ImsConstants.Phone.SLOT_2};
            for (int i = 0; i < 2; i++) {
                int i2 = iArr[i];
                if (asList.size() > i2) {
                    str2 = (String) asList.get(i2);
                } else {
                    str2 = "";
                }
                this.mEmergencyNumberMap.put(Integer.valueOf(i2), Arrays.asList(str2.split("\\,")));
            }
        } else {
            this.mEmergencyNumberMap.put(Integer.valueOf(this.mCmcInfo.mLineSlotIndex), Arrays.asList(replace.split("\\,")));
        }
        IMSLog.i(LOG_TAG, "setEmergencyNumbers: " + IMSLog.checker(this.mEmergencyNumberMap));
    }

    public boolean isEmergencyNumber(String str, int i) {
        boolean isEmergencyNumberInternal = isEmergencyNumberInternal(str, i, true);
        IMSLog.i(LOG_TAG, "isEmergencyNumber: " + isEmergencyNumberInternal + ", number: " + IMSLog.checker(str));
        return isEmergencyNumberInternal;
    }

    public boolean isPotentialEmergencyNumber(String str, int i) {
        boolean isEmergencyNumberInternal = isEmergencyNumberInternal(str, i, false);
        IMSLog.i(LOG_TAG, "isPotentialEmergencyNumber: " + isEmergencyNumberInternal + ", number: " + IMSLog.checker(str));
        return isEmergencyNumberInternal;
    }

    private boolean isEmergencyNumberInternal(String str, int i, boolean z) {
        if (!isSupportDualSimCMC()) {
            i = this.mCmcInfo.mLineSlotIndex;
        }
        if (!(str == null || this.mEmergencyNumberMap.get(Integer.valueOf(i)) == null || this.mEmergencyNumberMap.get(Integer.valueOf(i)).isEmpty())) {
            IMSLog.i(LOG_TAG, "isEmergencyNumberInternal: current emergencyNumbers: " + IMSLog.checker(this.mEmergencyNumberMap.get(Integer.valueOf(i))));
            String stripSeparators = PhoneNumberUtils.stripSeparators(str);
            for (String str2 : this.mEmergencyNumberMap.get(Integer.valueOf(i))) {
                if (z) {
                    if (str2.equals(stripSeparators)) {
                        return true;
                    }
                } else if (stripSeparators.startsWith(str2)) {
                    return true;
                }
            }
        }
        return false;
    }

    private String getEmergencyCallNumberString() {
        String str;
        if (isSupportDualSimCMC()) {
            str = getEmergencyCallNumberString(ImsConstants.Phone.SLOT_1) + CmcConstants.E_NUM_SLOT_SPLIT + getEmergencyCallNumberString(ImsConstants.Phone.SLOT_2);
        } else {
            str = getEmergencyCallNumberString(this.mCmcInfo.mLineSlotIndex);
        }
        if (str.isEmpty() || CmcConstants.E_NUM_SLOT_SPLIT.equals(str)) {
            return "";
        }
        return CmcConstants.E_NUM_STR_QUOTE + str + CmcConstants.E_NUM_STR_QUOTE;
    }

    private String getEmergencyCallNumberString(int i) {
        String str = "";
        if (isSecondaryDevice()) {
            return str;
        }
        Map<Integer, List<EmergencyNumber>> emergencyNumberList = TelephonyManagerWrapper.getInstance(this.mContext).getEmergencyNumberList();
        if (emergencyNumberList == null || emergencyNumberList.isEmpty()) {
            IMSLog.i(LOG_TAG, "getEmergencyCallNumberString: ecall list map empty");
            return str;
        }
        List<EmergencyNumber> list = emergencyNumberList.get(Integer.valueOf(SimUtil.getSubId(i)));
        if (list == null || list.isEmpty()) {
            IMSLog.i(LOG_TAG, "getEmergencyCallNumberString: ecall list empty");
            return str;
        }
        LinkedHashSet linkedHashSet = new LinkedHashSet();
        for (EmergencyNumber number : list) {
            linkedHashSet.add(number.getNumber());
        }
        if (linkedHashSet.size() > 0) {
            str = String.join(",", linkedHashSet);
        }
        IMSLog.i(LOG_TAG, "getEmergencyCallNumberString: slot(" + i + ") ecall numbers result: " + IMSLog.checker(str));
        return str;
    }

    private boolean isCmcProfile(ImsProfile imsProfile) {
        int cmcType = imsProfile.getCmcType();
        return (cmcType == 0 || cmcType == 7 || cmcType == 8) ? false : true;
    }

    private boolean isCmcServiceInstalled() {
        try {
            this.mContext.getPackageManager().getApplicationInfo(CmcConstants.SERVICE_PACKAGE_NAME, 128);
            this.mEventLog.logAndAdd("isCmcServiceInstalled: true");
            return true;
        } catch (PackageManager.NameNotFoundException unused) {
            this.mEventLog.logAndAdd("isCmcServiceInstalled: false");
            return false;
        }
    }

    private boolean needDeregiOnDeviceChange(IRegisterTask iRegisterTask) {
        if (this.mCmcInfoUpdatedResult.getChangedCmcInfoList().size() == 1) {
            if (this.mCmcInfoUpdatedResult.getChangedCmcInfoList().contains(CmcInfo.CmcInfoType.NETWORK_PREF) && iRegisterTask.getRegistrationRat() == 18) {
                IMSLog.i(LOG_TAG, "needDeregiOnDeviceChange: false with WiFi");
                return false;
            } else if ((this.mCmcInfoUpdatedResult.getChangedCmcInfoList().contains(CmcInfo.CmcInfoType.ACCESS_TOKEN) || this.mCmcInfoUpdatedResult.getChangedCmcInfoList().contains(CmcInfo.CmcInfoType.PCSCF_ADDR_LIST)) && TelephonyManagerWrapper.getInstance(this.mContext).getCallState(iRegisterTask.getPhoneId()) != 0) {
                IMSLog.i(LOG_TAG, "needDeregiOnDeviceChange: false: access token or pcscf update in call state");
                iRegisterTask.setHasPendingDeregister(true);
                return false;
            }
        }
        return true;
    }

    private class InternalHandler extends Handler {
        public InternalHandler(Looper looper) {
            super(looper);
        }

        public void handleMessage(Message message) {
            Log.i(CmcAccountManager.LOG_TAG, "handleMessage: " + message.what);
            switch (message.what) {
                case 1:
                    CmcAccountManager.this.startSAServiceInternal(((Boolean) message.obj).booleanValue());
                    return;
                case 2:
                    CmcAccountManager.this.onSaUpdated();
                    return;
                case 3:
                    CmcAccountManager.this.onStartCmcRegistration();
                    return;
                case 4:
                    CmcAccountManager.this.onStopCmcRegistration();
                    return;
                case 5:
                case 6:
                    CmcAccountManager.this.onCmcDeviceChanged();
                    return;
                case 7:
                    CmcAccountManager.this.handleSARequestFailed(CmcSAManager.SAErrorReason.REQUEST_TIMER_EXPIRED.setDescription(""));
                    return;
                case 8:
                    CmcAccountManager.this.handleSARequestFailed((CmcSAManager.SAErrorReason) message.obj);
                    return;
                default:
                    return;
            }
        }
    }

    public void onSAServiceBindResult(boolean z, boolean z2) {
        if (z) {
            this.mEventLog.logAndAdd("onSAServiceBindResult: success");
            this.mSABindRetryCount = 0;
            return;
        }
        int i = this.mSABindRetryCount;
        if (i < 5) {
            this.mSABindRetryCount = i + 1;
            SimpleEventLog simpleEventLog = this.mEventLog;
            simpleEventLog.logAndAdd("onSAServiceBindResult: retry (" + this.mSABindRetryCount + "/" + 5 + ") after " + 30 + "secs");
            InternalHandler internalHandler = this.mHandler;
            internalHandler.sendMessageDelayed(internalHandler.obtainMessage(1, Boolean.valueOf(z2)), 30000);
            return;
        }
        this.mEventLog.logAndAdd("onSAServiceBindResult: retry over");
        this.mSABindRetryCount = 0;
    }

    public void onSARequested() {
        this.mEventLog.logAndAdd("onSARequested: expire 31 secs");
        this.mHandler.sendEmptyMessageDelayed(7, 31000);
    }

    public void onSAUpdated(String str, String str2) {
        this.mEventLog.logAndAdd("onSAUpdated");
        this.mHandler.removeMessages(7);
        this.mHandler.removeMessages(8);
        this.mSaService.disconnectToSamsungAccountService();
        if (this.mSaToken.equals(str) && this.mSaService.isLocalCachedAccessTokenRequestState()) {
            Log.i(LOG_TAG, "Same updated token with the previous one. Set force update");
            this.mCmcInfoUpdatedResult.setForceUpdate();
        }
        this.mSaToken = str;
        this.mSaUrl = str2;
        IMSLog.s(LOG_TAG, "onSAUpdated: Url: " + this.mSaUrl + " token: " + this.mSaToken);
        this.mHandler.sendEmptyMessage(2);
    }

    public void onSARequestFailed(CmcSAManager.SAErrorReason sAErrorReason) {
        InternalHandler internalHandler = this.mHandler;
        internalHandler.sendMessage(internalHandler.obtainMessage(8, sAErrorReason));
    }

    public void handleSARequestFailed(CmcSAManager.SAErrorReason sAErrorReason) {
        this.mHandler.removeMessages(7);
        int i = this.mSARequestRetryCount + 1;
        this.mSARequestRetryCount = i;
        if (i > 3) {
            IMSLog.i(LOG_TAG, "handleSARequestFailed: ignore fail: " + sAErrorReason.description());
            return;
        }
        SimpleEventLog simpleEventLog = this.mEventLog;
        simpleEventLog.logAndAdd("handleSARequestFailed: " + sAErrorReason.description() + ", retry(" + this.mSARequestRetryCount + "/" + 3 + ")");
        if (sAErrorReason == CmcSAManager.SAErrorReason.NOT_LOGGED_IN || sAErrorReason == CmcSAManager.SAErrorReason.RESIGN_REQUIRED || sAErrorReason == CmcSAManager.SAErrorReason.OTHERS) {
            IMSLog.i(LOG_TAG, "handleSARequestFailed: stop requesting");
            this.mSARequestRetryCount = 3;
        } else if (sAErrorReason == CmcSAManager.SAErrorReason.NETWORK_UNAVAILABLE) {
            IRegisterTask cmcRegisterTask = getCmcRegisterTask(getCmcPhoneId());
            if (cmcRegisterTask != null && cmcRegisterTask.getGovernor().isThrottled()) {
                IMSLog.i(LOG_TAG, "handleSARequestFailed: release throttle");
                this.mRm.releaseThrottleByCmc(cmcRegisterTask);
            }
            this.mSARequestRetryCount = 3;
        } else if (this.mSARequestRetryCount < 3) {
            this.mSaService.tryGetAccessToken();
        } else {
            IMSLog.i(LOG_TAG, "handleSARequestFailed: max count");
        }
        if (this.mSARequestRetryCount == 3) {
            this.mSaService.disconnectToSamsungAccountService();
        }
    }

    private void resetSARetryCount() {
        this.mSARequestRetryCount = 0;
    }

    public void dump() {
        this.mEventLog.dump();
    }
}
