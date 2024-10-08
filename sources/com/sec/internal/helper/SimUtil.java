package com.sec.internal.helper;

import android.content.Context;
import android.os.Build;
import android.os.SemSystemProperties;
import android.provider.Settings;
import android.telephony.SubscriptionInfo;
import android.telephony.SubscriptionManager;
import android.text.TextUtils;
import android.util.Log;
import com.samsung.android.feature.SemCscFeature;
import com.sec.ims.extensions.Extensions;
import com.sec.internal.constants.Mno;
import com.sec.internal.constants.ims.ImsConstants;
import com.sec.internal.constants.ims.core.SimConstants;
import com.sec.internal.constants.ims.os.SecFeature;
import com.sec.internal.helper.os.DeviceUtil;
import com.sec.internal.helper.os.TelephonyManagerWrapper;
import com.sec.internal.ims.core.sim.SimManagerFactory;
import com.sec.internal.ims.util.ImsUtil;
import com.sec.internal.interfaces.ims.core.ISimManager;
import com.sec.internal.log.IMSLog;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.ConcurrentHashMap;

public class SimUtil {
    private static final String LOG_TAG = "SimUtil";
    private static int sActiveDataPhoneId;
    private static final Map<Integer, Mno> sMnoMap = new ConcurrentHashMap();
    private static int sPhoneCount;
    private static SubscriptionManager sSubMgr;

    public static int getSimSlotPriority() {
        int i = 0;
        if (sSubMgr == null) {
            Log.d(LOG_TAG, "getSimSlotPriority: SubscriptionManager is not created. Return 0..");
            return 0;
        }
        int activeDataPhoneIdFromTelephony = getActiveDataPhoneIdFromTelephony();
        if (isValidSimSlot(activeDataPhoneIdFromTelephony)) {
            return activeDataPhoneIdFromTelephony;
        }
        Log.d(LOG_TAG, "getSimSlotPriority: Invalid ADS slot: " + activeDataPhoneIdFromTelephony + ", phoneCount: " + sPhoneCount);
        while (i < sPhoneCount) {
            SubscriptionInfo activeSubscriptionInfoForSimSlotIndex = sSubMgr.getActiveSubscriptionInfoForSimSlotIndex(i);
            if (activeSubscriptionInfoForSimSlotIndex == null || activeSubscriptionInfoForSimSlotIndex.getSubscriptionId() == -1) {
                i++;
            } else {
                Log.d(LOG_TAG, "subInfo is valid on slot#" + i);
                return i;
            }
        }
        return getActiveDataPhoneId();
    }

    public static boolean isValidSimSlot(int i) {
        return i >= 0 && i < sPhoneCount;
    }

    public static void setSubMgr(SubscriptionManager subscriptionManager) {
        sSubMgr = subscriptionManager;
    }

    public static int getPhoneCount() {
        return sPhoneCount;
    }

    public static void setPhoneCount(int i) {
        sPhoneCount = i;
    }

    public static int getActiveDataPhoneId() {
        return sActiveDataPhoneId;
    }

    public static void setActiveDataPhoneId(int i) {
        sActiveDataPhoneId = i;
    }

    public static String getConfigDualIMS() {
        if (sPhoneCount < 2) {
            return SimConstants.SINGLE;
        }
        return isDSDACapableDevice() ? SimConstants.DSDA_DI : SimConstants.DSDS_DI;
    }

    public static boolean isDualIMS() {
        String configDualIMS = getConfigDualIMS();
        return SimConstants.DSDS_DI.equals(configDualIMS) || SimConstants.DSDA_DI.equals(configDualIMS);
    }

    public static int getAvailableSimCount() {
        int i = SemSystemProperties.getInt("ro.multisim.simslotcount", 1);
        if (i < 2) {
            return 0;
        }
        int i2 = 0;
        for (int i3 = 0; i3 < i; i3++) {
            ISimManager simManagerFromSimSlot = SimManagerFactory.getSimManagerFromSimSlot(i3);
            if (simManagerFromSimSlot != null && simManagerFromSimSlot.isSimAvailable()) {
                i2++;
            }
        }
        Log.d(LOG_TAG, "availableSim = " + i2);
        return i2;
    }

    public static int getActiveSimCount(Context context) {
        int i = SemSystemProperties.getInt("ro.multisim.simslotcount", 1);
        if (i < 2) {
            return i;
        }
        int i2 = 0;
        for (int i3 = 0; i3 < i; i3++) {
            if (isSimActive(context, i3)) {
                i2++;
            }
        }
        Log.d(LOG_TAG, "activeSimCount= " + i2);
        return i2;
    }

    public static boolean isSimActive(Context context, int i) {
        ISimManager simManagerFromSimSlot = SimManagerFactory.getSimManagerFromSimSlot(i);
        if (simManagerFromSimSlot == null) {
            return false;
        }
        int simState = simManagerFromSimSlot.getSimState();
        SubscriptionInfo activeSubscriptionInfoForSimSlotIndex = sSubMgr.getActiveSubscriptionInfoForSimSlotIndex(i);
        Log.d(LOG_TAG, "subInfo[" + i + "]: " + activeSubscriptionInfoForSimSlotIndex + ", simState: " + simState);
        if (activeSubscriptionInfoForSimSlotIndex == null) {
            return false;
        }
        if (activeSubscriptionInfoForSimSlotIndex.isEmbedded()) {
            if (simState != 6) {
                return true;
            }
            return false;
        } else if (simState == 1) {
            return false;
        } else {
            if (Settings.Global.getInt(context.getContentResolver(), i == 0 ? "phone1_on" : "phone2_on", -1) == 1) {
                return true;
            }
            return false;
        }
    }

    public static boolean isDdsSimSlot(int i) {
        return i == getActiveDataPhoneIdFromTelephony();
    }

    public static int getSubId(int i) {
        int[] subId = Extensions.SubscriptionManager.getSubId(i);
        if (subId != null) {
            return subId[0];
        }
        IMSLog.e(LOG_TAG, i, "subIdArray is null");
        return -1;
    }

    public static int getSubId() {
        return getSubId(sActiveDataPhoneId);
    }

    public static boolean isDSDACapableDevice() {
        return "dsda".equals(SemSystemProperties.get("persist.radio.multisim.config", ""));
    }

    public static boolean isCurrentDSDASupport() {
        return isDSDACapableDevice() && SemSystemProperties.get("ril.msim.submode", "").toLowerCase().contains("dsda");
    }

    public static boolean isMultiSimSupported() {
        String str = SemSystemProperties.get("persist.radio.multisim.config", "");
        return "dsds".equals(str) || "dsda".equals(str);
    }

    public static boolean isSimMobilityFeatureEnabled() {
        int i = SemSystemProperties.getInt(ImsConstants.SystemProperties.SIMMOBILITY_ENABLE, -1);
        if (i == 1) {
            Log.i(LOG_TAG, "SimMobility Enabled for test");
            return true;
        } else if (i != 0) {
            return true;
        } else {
            Log.i(LOG_TAG, "SimMobility disabled by manual");
            return false;
        }
    }

    public static boolean isSoftphoneEnabled() {
        return DeviceUtil.isTablet() && ("ATT".equals(OmcCode.get()) || "APP".equals(OmcCode.get()));
    }

    public static boolean isDSH(int i) {
        return "TMobile_US:DSH".equals((String) Optional.ofNullable(SimManagerFactory.getSimManagerFromSimSlot(i)).map(new SimUtil$$ExternalSyntheticLambda0()).orElse(""));
    }

    public static boolean isDSH5G(int i) {
        return "Dish_US".equals((String) Optional.ofNullable(SimManagerFactory.getSimManagerFromSimSlot(i)).map(new SimUtil$$ExternalSyntheticLambda0()).orElse(""));
    }

    public static boolean isDishCrossOver() {
        String str = (String) Optional.ofNullable(SimManagerFactory.getSimManagerFromSimSlot(0)).map(new SimUtil$$ExternalSyntheticLambda1()).orElse("");
        String str2 = (String) Optional.ofNullable(SimManagerFactory.getSimManagerFromSimSlot(1)).map(new SimUtil$$ExternalSyntheticLambda1()).orElse("");
        if (!"ATT_US:DSH".equals((String) Optional.ofNullable(SimManagerFactory.getSimManagerFromSimSlot(0)).map(new SimUtil$$ExternalSyntheticLambda0()).orElse(""))) {
            return false;
        }
        if ((!"3438".equals(str) && !"3440".equals(str)) || !"DSG".equals(OmcCode.getNWCode(1))) {
            return false;
        }
        if ("6730".equals(str2) || "6738".equals(str2)) {
            return true;
        }
        return false;
    }

    public static boolean isCctChaCbrsMsoSim(int i) {
        String str = (String) Optional.ofNullable(SimManagerFactory.getSimManagerFromSimSlot(i)).map(new SimUtil$$ExternalSyntheticLambda1()).orElse("");
        String str2 = (String) Optional.ofNullable(SimManagerFactory.getSimManagerFromSimSlot(i)).map(new SimUtil$$ExternalSyntheticLambda4()).orElse("");
        return "314200".equals(str2) || ("314020".equals(str2) && "BA01490000000000".equals(str));
    }

    private static boolean isCctOrChaMnoSim(int i) {
        String str = (String) Optional.ofNullable(SimManagerFactory.getSimManagerFromSimSlot(i)).map(new SimUtil$$ExternalSyntheticLambda0()).orElse("");
        return ("VZW_US:CCT".equals(str) && "BA01450000000000".equals((String) Optional.ofNullable(SimManagerFactory.getSimManagerFromSimSlot(i)).map(new SimUtil$$ExternalSyntheticLambda1()).orElse(""))) || "VZW_US:CHA".equals(str);
    }

    public static boolean isCctChaCBRS(int i) {
        boolean z = isCctOrChaMnoSim(i) && isCctChaCbrsMsoSim(getOppositeSimSlot(i));
        Log.i(LOG_TAG, "isCctChaCBRS: result = " + z);
        return z;
    }

    public static boolean isTmoInbound(int i) {
        return "TMobile_US:Inbound".equals((String) Optional.ofNullable(SimManagerFactory.getSimManagerFromSimSlot(i)).map(new SimUtil$$ExternalSyntheticLambda0()).orElse(""));
    }

    public static boolean isCSpire(int i) {
        return "GenericIR92_US:CSpire".equals((String) Optional.ofNullable(SimManagerFactory.getSimManagerFromSimSlot(i)).map(new SimUtil$$ExternalSyntheticLambda0()).orElse(""));
    }

    public static boolean isUnited(int i) {
        return "GenericIR92_US:United".equals((String) Optional.ofNullable(SimManagerFactory.getSimManagerFromSimSlot(i)).map(new SimUtil$$ExternalSyntheticLambda0()).orElse(""));
    }

    public static boolean isLLA(int i) {
        return "ATT_US:LLA".equals((String) Optional.ofNullable(SimManagerFactory.getSimManagerFromSimSlot(i)).map(new SimUtil$$ExternalSyntheticLambda0()).orElse(""));
    }

    public static boolean isCCT(int i) {
        return "VZW_US:CCT".equals((String) Optional.ofNullable(SimManagerFactory.getSimManagerFromSimSlot(i)).map(new SimUtil$$ExternalSyntheticLambda0()).orElse(""));
    }

    public static boolean isNoSIM(int i) {
        return ((Boolean) Optional.ofNullable(SimManagerFactory.getSimManagerFromSimSlot(i)).map(new SimUtil$$ExternalSyntheticLambda2()).orElse(Boolean.TRUE)).booleanValue();
    }

    public static void setSimMno(int i, Mno mno) {
        IMSLog.i(LOG_TAG, i, "setSimMno : " + mno);
        sMnoMap.put(Integer.valueOf(i), mno);
    }

    public static Mno getMno() {
        return getMno(sActiveDataPhoneId, true);
    }

    public static Mno getMno(int i) {
        return getMno(i, true);
    }

    public static Mno getSimMno(int i) {
        return getMno(i, false);
    }

    private static Mno getMno(int i, boolean z) {
        Mno mockMno = Mno.getMockMno();
        if (mockMno != null) {
            return mockMno;
        }
        Mno mno = sMnoMap.get(Integer.valueOf(i));
        if (mno != null) {
            return mno;
        }
        if (z) {
            IMSLog.e(LOG_TAG, i, "fail to get mno from map");
            mno = Mno.fromSalesCode(OmcCode.getNWCode(i));
        }
        return mno != null ? mno : Mno.DEFAULT;
    }

    public static int getOppositeSimSlot(int i) {
        int i2 = ImsConstants.Phone.SLOT_1;
        return i == i2 ? ImsConstants.Phone.SLOT_2 : i2;
    }

    public static boolean isSupportCarrierVersion(int i) {
        String string = SemCscFeature.getInstance().getString(i, SecFeature.CSC.TAG_CSCFEATURE_GMS_SETCLIENTIDBASEMS);
        return !string.isEmpty() && !string.contains("samsung");
    }

    public static int getActiveDataSubscriptionId() {
        return Extensions.SubscriptionManager.getActiveDataSubscriptionId();
    }

    public static int getActiveDataPhoneIdFromTelephony() {
        return getSlotId(getActiveDataSubscriptionId());
    }

    public static int getSlotId(int i) {
        return Extensions.SubscriptionManager.getSlotId(i);
    }

    public static String getMvnoName(String str) {
        int indexOf = str.indexOf(Mno.MVNO_DELIMITER);
        return indexOf != -1 ? str.substring(indexOf + 1) : "";
    }

    public static int getActiveSubInfoCount() {
        SubscriptionManager subscriptionManager = sSubMgr;
        if (subscriptionManager == null) {
            return -1;
        }
        return subscriptionManager.getActiveSubscriptionInfoCount();
    }

    public static Mno getSimMnoAsNwPlmn(int i) {
        return (Mno) Optional.ofNullable(SimManagerFactory.getSimManagerFromSimSlot(i)).map(new SimUtil$$ExternalSyntheticLambda3(ImsUtil.getSystemProperty("gsm.operator.numeric", i, ""))).orElse(Mno.DEFAULT);
    }

    public static boolean isSimMobilityAvailable(Context context, int i, Mno mno) {
        String str;
        if (!isSimMobilityFeatureEnabled()) {
            str = "SIM Mobility Feature disabled; ";
        } else if (!SimManagerFactory.isOutboundSim(i)) {
            str = "Not outbound Sim - SimMobility should be disabled; ";
        } else {
            if (DeviceUtil.isTablet()) {
                if (!TelephonyManagerWrapper.getInstance(context).isVoiceCapable()) {
                    str = "Disable non voice capable tablet in R OS";
                } else if (!mno.isAmerica() && SemSystemProperties.getInt(ImsConstants.SystemProperties.FIRST_API_VERSION, 0) < 30) {
                    str = "Disable under R OS(first API) except America Region";
                } else if (!mno.isAmerica() && !mno.isChn() && "SM-P619".equalsIgnoreCase(Build.MODEL)) {
                    str = "Disable in SM-P619 except America/CHN Region";
                }
            }
            str = "";
        }
        if (TextUtils.isEmpty(str)) {
            return true;
        }
        IMSLog.d(LOG_TAG, i, str);
        return false;
    }
}
