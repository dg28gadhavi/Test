package com.sec.internal.helper.os;

import android.content.Context;
import android.os.SemSystemProperties;
import android.telephony.CellInfo;
import android.telephony.ServiceState;
import android.telephony.SubscriptionManager;
import android.telephony.TelephonyCallback;
import android.telephony.TelephonyManager;
import android.telephony.emergency.EmergencyNumber;
import android.text.TextUtils;
import android.util.Log;
import android.util.SparseArray;
import com.samsung.android.cmcsetting.CmcSettingManager;
import com.samsung.android.cmcsetting.CmcSettingManagerConstants;
import com.sec.ims.extensions.Extensions;
import com.sec.internal.constants.ims.entitilement.NSDSNamespaces;
import com.sec.internal.constants.ims.os.PhoneConstants;
import com.sec.internal.helper.OmcCode;
import com.sec.internal.helper.SimUtil;
import com.sec.internal.ims.core.cmc.CmcConstants;
import com.sec.internal.log.IMSLog;
import java.net.URI;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.Executor;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

public class TelephonyManagerWrapper implements ITelephonyManager {
    public static final int DEFAULT_ID = -1;
    private static final String LOG_TAG = "TelephonyManagerWrapper";
    private static volatile TelephonyManagerWrapper mInstance;
    private Context mContext;
    private String mDeviceType = "";
    private SparseArray<String> mGid1 = new SparseArray<>();
    private SparseArray<String> mGid2 = new SparseArray<>();
    public SparseArray<String> mHomeDomain = new SparseArray<>();
    private SparseArray<String> mImei = new SparseArray<>();
    public SparseArray<String> mImpi = new SparseArray<>();
    public SparseArray<String[]> mImpus = new SparseArray<>();
    private SparseArray<String> mImsi = new SparseArray<>();
    private SparseArray<String> mOperatorCode = new SparseArray<>();
    private Map<TelephonyCallback, Integer> mTelephonyCallbackCache;

    public void setCallState(int i) {
    }

    public static synchronized ITelephonyManager getInstance(Context context) {
        TelephonyManagerWrapper telephonyManagerWrapper;
        synchronized (TelephonyManagerWrapper.class) {
            if (mInstance == null) {
                synchronized (TelephonyManagerWrapper.class) {
                    if (mInstance == null) {
                        mInstance = new TelephonyManagerWrapper(context);
                    }
                }
            }
            telephonyManagerWrapper = mInstance;
        }
        return telephonyManagerWrapper;
    }

    public TelephonyManagerWrapper(Context context) {
        this.mContext = context;
        this.mTelephonyCallbackCache = new HashMap();
    }

    private TelephonyManager getTelephonyManager() {
        return (TelephonyManager) this.mContext.getSystemService(PhoneConstants.PHONE_KEY);
    }

    private TelephonyManager getTelephonyManager(int i) {
        return ((TelephonyManager) this.mContext.getSystemService(PhoneConstants.PHONE_KEY)).createForSubscriptionId(i);
    }

    public void registerTelephonyCallback(Executor executor, TelephonyCallback telephonyCallback) {
        registerTelephonyCallbackForSlot(SubscriptionManager.getSlotIndex(SubscriptionManager.getDefaultSubscriptionId()), executor, telephonyCallback);
    }

    public void registerTelephonyCallbackForSlot(int i, Executor executor, TelephonyCallback telephonyCallback) {
        int subscriptionId = SubscriptionManager.getSubscriptionId(i);
        if (((Integer) Optional.ofNullable(this.mTelephonyCallbackCache.get(telephonyCallback)).orElse(-1)).intValue() != -1) {
            this.mTelephonyCallbackCache.remove(telephonyCallback);
            unregisterTelephonyCallbackForSlot(i, telephonyCallback);
        }
        this.mTelephonyCallbackCache.put(telephonyCallback, Integer.valueOf(subscriptionId));
        getTelephonyManager(subscriptionId).registerTelephonyCallback(executor, telephonyCallback);
    }

    public void unregisterTelephonyCallback(TelephonyCallback telephonyCallback) {
        unregisterTelephonyCallbackForSlot(SubscriptionManager.getSlotIndex(SubscriptionManager.getDefaultSubscriptionId()), telephonyCallback);
    }

    public void unregisterTelephonyCallbackForSlot(int i, TelephonyCallback telephonyCallback) {
        getTelephonyManager(SubscriptionManager.getSubscriptionId(i)).unregisterTelephonyCallback(telephonyCallback);
    }

    public void sendRawRequestToTelephony(Context context, byte[] bArr) {
        getTelephonyManager().invokeOemRilRequestRaw(bArr, new byte[4]);
    }

    public String getMsisdn(int i) {
        return getTelephonyManager().getMsisdn(i);
    }

    public boolean isNetworkRoaming() {
        return getTelephonyManager().isNetworkRoaming();
    }

    public boolean isNetworkRoaming(int i) {
        return getTelephonyManager(i).isNetworkRoaming();
    }

    public String getNetworkOperator(int i) {
        return getTelephonyManager(i).getNetworkOperator();
    }

    public String getNetworkOperatorForPhone(int i) {
        return getTelephonyManager().getNetworkOperatorForPhone(i);
    }

    public String getNetworkCountryIso() {
        return getTelephonyManager().getNetworkCountryIso();
    }

    public String getNetworkCountryIso(int i) {
        return getTelephonyManager(i).getNetworkCountryIso();
    }

    public int getVoiceNetworkType() {
        return getTelephonyManager().getVoiceNetworkType();
    }

    public int getVoiceNetworkType(int i) {
        return getTelephonyManager(i).getVoiceNetworkType();
    }

    public void clearCache() {
        this.mImei.clear();
        this.mImsi.clear();
        this.mImpi.clear();
        this.mImpus.clear();
        this.mHomeDomain.clear();
        this.mOperatorCode.clear();
        this.mGid1.clear();
        this.mGid2.clear();
    }

    public ServiceState getServiceState(int i) {
        return getTelephonyManager(i).getServiceStateForSubscriber(i);
    }

    public int getServiceState() {
        ServiceState serviceState = getTelephonyManager().getServiceState();
        if (serviceState != null) {
            return serviceState.getState();
        }
        return -1;
    }

    public int getServiceStateForSubscriber(int i) {
        ServiceState serviceState = getTelephonyManager(i).getServiceState();
        if (serviceState != null) {
            return serviceState.getState();
        }
        return -1;
    }

    public int getDataNetworkType() {
        return getTelephonyManager().getDataNetworkType();
    }

    public int getDataNetworkType(int i) {
        return getTelephonyManager(i).getDataNetworkType();
    }

    public int getNetworkType() {
        return getTelephonyManager().getNetworkType();
    }

    public int getPhoneCount() {
        int phoneCount = getTelephonyManager().getPhoneCount();
        if (phoneCount != 0 || !isCmcSecondaryDevice()) {
            return phoneCount;
        }
        return 1;
    }

    public int getSimState() {
        return getTelephonyManager().getSimState();
    }

    public int getSimState(int i) {
        return getTelephonyManager().getSimState(i);
    }

    public boolean isGbaSupported() {
        return getTelephonyManager().isGbaSupported();
    }

    public boolean isGbaSupported(int i) {
        return getTelephonyManager().isGbaSupported(i);
    }

    public String getIccAuthentication(int i, int i2, int i3, String str) {
        return getTelephonyManager(i).getIccAuthentication(i2, i3, str);
    }

    public String getApnOperatorCode(String str, int i) {
        if (TextUtils.isEmpty(str)) {
            return "";
        }
        String nWCode = OmcCode.getNWCode(i);
        if ("LRA".equalsIgnoreCase(nWCode) || "ACG".equalsIgnoreCase(nWCode)) {
            String str2 = SemSystemProperties.get("gsm.apn.sim.operator.numeric", "");
            if (!TextUtils.isEmpty(str2)) {
                String[] split = str2.split(",");
                if (i >= 0 && i < split.length && split[i] != null) {
                    Log.e(LOG_TAG, "for " + nWCode + " use apnOperatorCode " + split[i]);
                    return split[i];
                }
            }
        }
        return str;
    }

    public String getSimOperator() {
        String simOperator = getTelephonyManager().getSimOperator();
        int activeDataPhoneIdFromTelephony = SimUtil.getActiveDataPhoneIdFromTelephony();
        if (TextUtils.isEmpty(simOperator)) {
            simOperator = this.mOperatorCode.get(-1);
            Log.e(LOG_TAG, "use backup operatorCode : " + IMSLog.checker(simOperator));
        } else {
            this.mOperatorCode.put(-1, simOperator);
        }
        return getApnOperatorCode(simOperator, activeDataPhoneIdFromTelephony);
    }

    public String getSimOperator(int i) {
        int slotId = Extensions.SubscriptionManager.getSlotId(i);
        String simOperator = getTelephonyManager(i).getSimOperator(i);
        if (TextUtils.isEmpty(simOperator)) {
            simOperator = this.mOperatorCode.get(i);
            Log.e(LOG_TAG, "use backup operatorCode : " + IMSLog.checker(simOperator));
        } else {
            this.mOperatorCode.put(i, simOperator);
        }
        if (slotId == -1) {
            slotId = SimUtil.getActiveDataPhoneIdFromTelephony();
        }
        return getApnOperatorCode(simOperator, slotId);
    }

    public String getIsimImpi(int i) {
        String isimImpi = getTelephonyManager(i).getIsimImpi();
        if (TextUtils.isEmpty(isimImpi)) {
            String str = this.mImpi.get(i);
            Log.e(LOG_TAG, "use backup impi : " + IMSLog.checker(str));
            return str;
        }
        String filterOutInverted = filterOutInverted(getSimOperator(i), isimImpi);
        this.mImpi.put(i, filterOutInverted);
        return filterOutInverted;
    }

    private String filterOutInverted(String str, String str2) {
        return (TextUtils.equals("73603", str) && hasInvertedDomainFormat(str2)) ? "" : str2;
    }

    /* access modifiers changed from: protected */
    public boolean hasInvertedDomainFormat(String str) {
        return Pattern.compile("ims\\.mcc\\d+\\.mnc\\d+\\.3gppnetwork\\.org").matcher(str).find();
    }

    public String getIsimDomain(int i) {
        String isimDomain = getTelephonyManager(i).getIsimDomain();
        if (TextUtils.isEmpty(isimDomain)) {
            String str = this.mHomeDomain.get(i);
            Log.e(LOG_TAG, "use backup domain : " + IMSLog.checker(str));
            return str;
        }
        String filterOutInverted = filterOutInverted(getSimOperator(i), isimDomain);
        this.mHomeDomain.put(i, filterOutInverted);
        return filterOutInverted;
    }

    public String[] getIsimImpu(int i) {
        String[] isimImpu = getTelephonyManager(i).getIsimImpu();
        if (isimImpu == null || isimImpu.length == 0) {
            String[] strArr = this.mImpus.get(i);
            Log.e(LOG_TAG, "use backup impu : " + IMSLog.checker(Arrays.toString(strArr)));
            return strArr;
        }
        String[] filterOutInvertedImpu = filterOutInvertedImpu(getSimOperator(i), isimImpu);
        this.mImpus.put(i, filterOutInvertedImpu);
        return filterOutInvertedImpu;
    }

    private String[] filterOutInvertedImpu(String str, String[] strArr) {
        if (!TextUtils.equals("73603", str)) {
            return strArr;
        }
        List asList = Arrays.asList(strArr);
        ((List) asList.stream().filter(new TelephonyManagerWrapper$$ExternalSyntheticLambda0(this)).collect(Collectors.toList())).forEach(new TelephonyManagerWrapper$$ExternalSyntheticLambda1(asList));
        return (String[]) asList.toArray(new TelephonyManagerWrapper$$ExternalSyntheticLambda2());
    }

    /* access modifiers changed from: private */
    public /* synthetic */ boolean lambda$filterOutInvertedImpu$0(String str) {
        return !TextUtils.isEmpty(str) && hasInvertedDomainFormat(str);
    }

    /* access modifiers changed from: private */
    public static /* synthetic */ String[] lambda$filterOutInvertedImpu$2(int i) {
        return new String[i];
    }

    public String getLine1Number() {
        return getTelephonyManager().getLine1Number();
    }

    public String getLine1Number(int i) {
        return getTelephonyManager(i).getLine1Number();
    }

    public String getSubscriberId(int i) {
        if (getTelephonyProperty(Extensions.SubscriptionManager.getSlotId(i), "ril.simoperator", "ETC").contains("CTC")) {
            String subscriberIdForUiccAppType = getSubscriberIdForUiccAppType(i, 2);
            if (!TextUtils.isEmpty(subscriberIdForUiccAppType)) {
                return subscriberIdForUiccAppType;
            }
        }
        String subscriberId = getTelephonyManager(i).getSubscriberId();
        if (TextUtils.isEmpty(subscriberId)) {
            String str = this.mImsi.get(i);
            Log.e(LOG_TAG, "use backup imsi : " + IMSLog.checker(str));
            return str;
        }
        this.mImsi.put(i, subscriberId);
        return subscriberId;
    }

    public String getSubscriberIdForUiccAppType(int i, int i2) {
        String subscriberIdForUiccAppType = getTelephonyManager(i).getSubscriberIdForUiccAppType(i, i2);
        if (TextUtils.isEmpty(subscriberIdForUiccAppType)) {
            String str = this.mImsi.get(i);
            Log.e(LOG_TAG, "use backup imsi : " + IMSLog.checker(str));
            return str;
        }
        this.mImsi.put(i, subscriberIdForUiccAppType);
        return subscriberIdForUiccAppType;
    }

    public String getSimSerialNumber() {
        return getTelephonyManager().getSimSerialNumber();
    }

    public String getSimSerialNumber(int i) {
        return getTelephonyManager(i).getSimSerialNumber();
    }

    public boolean validateMsisdn(int i) {
        if (TextUtils.isEmpty(getMsisdn(i))) {
            Log.e(LOG_TAG, "empty msisdn");
            return false;
        } else if ("0000000000".equals(getCdmaMdn(i))) {
            Log.e(LOG_TAG, "empty mdn");
            return false;
        } else if (isValidIsimMsisdn(i)) {
            return true;
        } else {
            Log.e(LOG_TAG, "empty iSimMsisdn");
            return false;
        }
    }

    private boolean isValidIsimMsisdn(int i) {
        String[] isimImpu = getIsimImpu(i);
        String str = "";
        if (!(isimImpu == null || isimImpu.length == 0)) {
            for (String str2 : isimImpu) {
                if (str2 != null && (str2.contains("+") || str2.startsWith("tel"))) {
                    str = extractNumber(str2);
                }
            }
        }
        return !"+8200000000000".equals(str);
    }

    private String extractNumber(String str) {
        String lowerCase = URI.create(str.trim()).getSchemeSpecificPart().toLowerCase();
        int indexOf = lowerCase.indexOf("@");
        return indexOf != -1 ? lowerCase.substring(0, indexOf) : lowerCase;
    }

    public int getCallState() {
        return getTelephonyManager().getCallState();
    }

    public int getCallState(int i) {
        try {
            int[] subId = SubscriptionManager.getSubId(i);
            if (subId != null) {
                if (subId.length != 0) {
                    return getTelephonyManager().getCallState(subId[0]);
                }
            }
        } catch (NullPointerException unused) {
        }
        return 0;
    }

    public int getCurrentPhoneTypeForSlot(int i) {
        return getTelephonyManager().getCurrentPhoneTypeForSlot(i);
    }

    public String getGroupIdLevel1() {
        String groupIdLevel1 = getTelephonyManager().getGroupIdLevel1();
        if (TextUtils.isEmpty(groupIdLevel1)) {
            String str = this.mGid1.get(-1);
            Log.e(LOG_TAG, "use backup gid : " + IMSLog.checker(str));
            return str;
        }
        this.mGid1.put(-1, groupIdLevel1);
        return groupIdLevel1;
    }

    public String getGroupIdLevel1(int i) {
        String groupIdLevel1 = getTelephonyManager().getGroupIdLevel1(i);
        if (TextUtils.isEmpty(groupIdLevel1)) {
            String str = this.mGid1.get(i);
            Log.e(LOG_TAG, "use backup gid : " + IMSLog.checker(str));
            return str;
        }
        this.mGid1.put(i, groupIdLevel1);
        return groupIdLevel1;
    }

    public String getGid2(int i) {
        String groupIdLevel2 = getTelephonyManager().getGroupIdLevel2(i);
        if (TextUtils.isEmpty(groupIdLevel2)) {
            String str = this.mGid2.get(i);
            Log.e(LOG_TAG, "use backup gid2 : " + IMSLog.checker(str));
            return str;
        }
        this.mGid2.put(i, groupIdLevel2);
        return groupIdLevel2;
    }

    public String[] getIsimPcscf() {
        return getTelephonyManager().getIsimPcscf();
    }

    public int getDataServiceState(int i) {
        return getTelephonyManager().semGetDataServiceState(i);
    }

    public String getDeviceSoftwareVersion(int i) {
        return getTelephonyManager(i).getDeviceSoftwareVersion();
    }

    public void setGbaBootstrappingParams(byte[] bArr, String str, String str2) {
        getTelephonyManager().setGbaBootstrappingParams(bArr, str, str2);
    }

    public void setGbaBootstrappingParams(int i, byte[] bArr, String str, String str2) {
        getTelephonyManager(i).setGbaBootstrappingParams(bArr, str, str2);
    }

    public String getAidForAppType(int i) {
        return getTelephonyManager().getAidForAppType(i);
    }

    public String getAidForAppType(int i, int i2) {
        return getTelephonyManager().getAidForAppType(i, i2);
    }

    public byte[] getRand(int i) {
        return getTelephonyManager(i).getRand();
    }

    public String getBtid(int i) {
        return getTelephonyManager(i).getBtid();
    }

    public String getKeyLifetime(int i) {
        return getTelephonyManager(i).getKeyLifetime();
    }

    public List<CellInfo> getAllCellInfo() {
        return getTelephonyManager().getAllCellInfo();
    }

    public List<CellInfo> getAllCellInfoBySubId(int i) {
        return getTelephonyManager().getAllCellInfoBySubId(i);
    }

    public String getSimCountryIso() {
        return getTelephonyManager().getSimCountryIso();
    }

    public String getSimCountryIsoForSubId(int i) {
        return getTelephonyManager(i).getSimCountryIso();
    }

    public String getSimCountryIsoForPhone(int i) {
        getTelephonyManager();
        return TelephonyManager.getSimCountryIsoForPhone(i);
    }

    public boolean setPreferredNetworkType(int i, int i2) {
        return getTelephonyManager().setPreferredNetworkType(i, i2);
    }

    public int getPreferredNetworkType(int i) {
        return getTelephonyManager().getPreferredNetworkType(i);
    }

    public String getMsisdn() {
        return getTelephonyManager().getMsisdn();
    }

    private String getCdmaMdn(int i) {
        return getTelephonyManager().getCdmaMdn(i);
    }

    private String getCdmaMdn() {
        return getTelephonyManager().getCdmaMdn();
    }

    public String getImei() {
        return getTelephonyManager().getImei();
    }

    public String getImei(int i) {
        return getTelephonyManager().getImei(i);
    }

    public String getMeid(int i) {
        return getTelephonyManager().getMeid(i);
    }

    public String getIsimDomain() {
        String isimDomain = getTelephonyManager().getIsimDomain();
        if (TextUtils.isEmpty(isimDomain)) {
            String str = this.mHomeDomain.get(-1);
            Log.e(LOG_TAG, "use backup domain : " + IMSLog.checker(str));
            return str;
        }
        if (TextUtils.equals("73603", getSimOperator()) && hasInvertedDomainFormat(isimDomain)) {
            Log.e(LOG_TAG, "Inverted domain : " + IMSLog.checker(isimDomain));
            isimDomain = "";
        }
        this.mHomeDomain.put(-1, isimDomain);
        return isimDomain;
    }

    public void setRadioPower(boolean z) {
        getTelephonyManager().setRadioPower(z);
    }

    public String getTelephonyProperty(int i, String str, String str2) {
        getTelephonyManager();
        return TelephonyManager.getTelephonyProperty(i, str, str2);
    }

    public String getRilSimOperator(int i) {
        getTelephonyManager();
        String telephonyProperty = TelephonyManager.getTelephonyProperty(i, "ril.simoperator", "ETC");
        IMSLog.i(LOG_TAG, i, "getRilSimOperator: " + telephonyProperty);
        return telephonyProperty;
    }

    public String getSimOperatorName(int i) {
        return getTelephonyManager(i).getSimOperatorName();
    }

    private boolean isCmcSecondaryDevice() {
        if (!TextUtils.isEmpty(this.mDeviceType)) {
            IMSLog.d(LOG_TAG, "getPhoneCount: isCmcSecondaryDevice: cache " + this.mDeviceType);
            return "sd".equalsIgnoreCase(this.mDeviceType);
        }
        CmcSettingManager cmcSettingManager = new CmcSettingManager();
        cmcSettingManager.init(this.mContext);
        CmcSettingManagerConstants.DeviceType ownDeviceType = cmcSettingManager.getOwnDeviceType();
        cmcSettingManager.deInit();
        IMSLog.d(LOG_TAG, "getPhoneCount: isCmcSecondaryDevice: api: " + ownDeviceType);
        if (ownDeviceType == CmcSettingManagerConstants.DeviceType.DEVICE_TYPE_SD) {
            this.mDeviceType = "sd";
            return true;
        } else if (ownDeviceType == CmcSettingManagerConstants.DeviceType.DEVICE_TYPE_PD) {
            this.mDeviceType = "pd";
            return false;
        } else {
            if (TextUtils.isEmpty(this.mDeviceType)) {
                String str = SemSystemProperties.get(CmcConstants.SystemProperties.CMC_DEVICE_TYPE_PROP, "");
                if (!TextUtils.isEmpty(str)) {
                    this.mDeviceType = str;
                    IMSLog.d(LOG_TAG, "getPhoneCount: isCmcSecondaryDevice: prop " + this.mDeviceType);
                    return "sd".equalsIgnoreCase(str);
                }
                this.mDeviceType = NSDSNamespaces.NSDSSimAuthType.UNKNOWN;
            }
            return false;
        }
    }

    public boolean isVoiceCapable() {
        return getTelephonyManager().isVoiceCapable();
    }

    public boolean hasCall(String str) {
        return getTelephonyManager().hasCall(str);
    }

    public int iccOpenLogicalChannelAndGetChannel(int i, String str) {
        return getTelephonyManager().iccOpenLogicalChannel(i, str, 4).getChannel();
    }

    public String iccTransmitApduLogicalChannel(int i, int i2, int i3, int i4, int i5, int i6, int i7, String str) {
        return getTelephonyManager().iccTransmitApduLogicalChannel(i, i2, i3, i4, i5, i6, i7, str);
    }

    public boolean iccCloseLogicalChannel(int i, int i2) {
        return getTelephonyManager().iccCloseLogicalChannel(i, i2);
    }

    public Map<Integer, List<EmergencyNumber>> getEmergencyNumberList() {
        return getTelephonyManager().getEmergencyNumberList();
    }

    public String checkCallControl(String str, int i) {
        int[] subId = SubscriptionManager.getSubId(i);
        return (subId == null || subId.length <= 0) ? str : getTelephonyManager(subId[0]).checkCallControl(str);
    }

    public int semGetDataState(int i) {
        return getTelephonyManager().semGetDataState(i);
    }

    public void semSetNrMode(int i, int i2) {
        IMSLog.d(LOG_TAG, "semSetNrMode: phoneId :" + i + " ,mode :" + i2);
        getTelephonyManager().semSetNrMode(i, i2, false);
    }

    public int semGetNrMode(int i) {
        IMSLog.d(LOG_TAG, "semGetNrMode: phoneId :" + i);
        return getTelephonyManager(SubscriptionManager.getSubId(i)[0]).semGetNrMode();
    }

    public String semGetTelephonyProperty(int i, String str, String str2) {
        getTelephonyManager();
        return TelephonyManager.semGetTelephonyProperty(i, str, str2);
    }

    public long getNextRetryTime() {
        return getTelephonyManager().getNextRetryTime();
    }

    public boolean semIsVoNrEnabled(int i) {
        IMSLog.d(LOG_TAG, "semIsVoNrEnabled: phoneId :" + i);
        return getTelephonyManager(SubscriptionManager.getSubId(i)[0]).semIsVoNrEnabled();
    }
}
