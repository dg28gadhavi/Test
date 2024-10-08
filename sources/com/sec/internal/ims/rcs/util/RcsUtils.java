package com.sec.internal.ims.rcs.util;

import android.app.AlertDialog;
import android.content.ContentValues;
import android.content.Context;
import android.content.DialogInterface;
import android.database.ContentObserver;
import android.os.PersistableBundle;
import android.os.RemoteException;
import android.os.SemSystemProperties;
import android.telephony.CarrierConfigManager;
import android.text.TextUtils;
import android.util.Log;
import com.sec.ims.settings.ImsProfile;
import com.sec.ims.settings.UserConfiguration;
import com.sec.imsservice.R;
import com.sec.internal.constants.Mno;
import com.sec.internal.constants.ims.ImsConstants;
import com.sec.internal.constants.ims.cmstore.CloudMessageProviderContract;
import com.sec.internal.constants.ims.entitilement.NSDSNamespaces;
import com.sec.internal.constants.ims.settings.GlobalSettingsConstants;
import com.sec.internal.helper.CollectionUtils;
import com.sec.internal.helper.NetworkUtil;
import com.sec.internal.helper.OmcCode;
import com.sec.internal.helper.SimUtil;
import com.sec.internal.helper.os.ITelephonyManager;
import com.sec.internal.ims.core.sim.SimManagerFactory;
import com.sec.internal.ims.registry.ImsRegistry;
import com.sec.internal.ims.settings.ImsProfileLoaderInternal;
import com.sec.internal.ims.settings.ImsServiceSwitch;
import com.sec.internal.ims.util.ConfigUtil;
import com.sec.internal.interfaces.ims.core.ISimManager;
import com.sec.internal.log.IMSLog;
import java.util.Arrays;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Map;
import java.util.Set;

public class RcsUtils {
    /* access modifiers changed from: private */
    public static String LOG_TAG = "RcsUtils";

    public static class DualRcs {
        private static Map<Integer, Boolean> mIsDualRcsRegForSlot = new HashMap();
        private static boolean mIsDualRcsSettings = false;

        public static boolean isDualRcsReg() {
            if (SimUtil.getPhoneCount() < 2) {
                Log.i(RcsUtils.LOG_TAG, "isDualRcsReg: false");
                return false;
            }
            for (int i = 0; i < SimUtil.getPhoneCount(); i++) {
                if (!mIsDualRcsRegForSlot.getOrDefault(Integer.valueOf(i), Boolean.FALSE).booleanValue()) {
                    Log.i(RcsUtils.LOG_TAG, "isDualRcsReg: false");
                    return false;
                }
            }
            Log.i(RcsUtils.LOG_TAG, "isDualRcsReg: true");
            return true;
        }

        public static boolean isDualRcsSettings() {
            String r0 = RcsUtils.LOG_TAG;
            IMSLog.i(r0, "isDualRcsSettings: " + mIsDualRcsSettings);
            return mIsDualRcsSettings;
        }

        public static void refreshDualRcsReg(Context context) {
            refreshDualRcsSettings(context);
            HashMap hashMap = new HashMap();
            for (int i = 0; i < SimUtil.getPhoneCount(); i++) {
                hashMap.put(Integer.valueOf(i), Boolean.valueOf(isRegAllowed(context, i)));
            }
            updateDualRcsRegi(context, hashMap);
        }

        private static void updateDualRcsRegi(Context context, Map<Integer, Boolean> map) {
            String r0 = RcsUtils.LOG_TAG;
            IMSLog.i(r0, "updateDualRcsRegi: " + mIsDualRcsRegForSlot + "->" + map);
            int i = 0;
            while (true) {
                if (i >= SimUtil.getPhoneCount()) {
                    break;
                } else if (mIsDualRcsRegForSlot.getOrDefault(Integer.valueOf(i), Boolean.FALSE) != map.get(Integer.valueOf(i))) {
                    context.getContentResolver().notifyChange(ImsConstants.Uris.RCS_PREFERENCE_PROVIDER_SUPPORT_DUAL_RCS, (ContentObserver) null);
                    break;
                } else {
                    i++;
                }
            }
            mIsDualRcsRegForSlot.putAll(map);
        }

        public static void refreshDualRcsSettings(Context context) {
            if (!SimUtil.isDualIMS()) {
                mIsDualRcsSettings = false;
                return;
            }
            for (int i = 0; i < SimUtil.getPhoneCount(); i++) {
                ISimManager simManagerFromSimSlot = SimManagerFactory.getSimManagerFromSimSlot(i);
                if (simManagerFromSimSlot == null || !simManagerFromSimSlot.isSimAvailable()) {
                    mIsDualRcsSettings = false;
                    return;
                }
            }
            for (int i2 = 0; i2 < SimUtil.getPhoneCount(); i2++) {
                if (dualRcsPolicyCase(context, i2)) {
                    mIsDualRcsSettings = true;
                    return;
                }
            }
            mIsDualRcsSettings = false;
        }

        public static boolean needToCheckOmcCodeAndSimMno(int i) {
            int dualRcsPolicy = getDualRcsPolicy(i);
            return dualRcsPolicy == 1 || dualRcsPolicy == 4;
        }

        public static int getDualRcsPolicy(int i) {
            ISimManager simManagerFromSimSlot = SimManagerFactory.getSimManagerFromSimSlot(i);
            if (SimUtil.getSimMno(i) != Mno.GOOGLEGC || simManagerFromSimSlot == null || !"us".equalsIgnoreCase(simManagerFromSimSlot.getSimCountryIso())) {
                return ImsRegistry.getInt(i, "dual_rcs_policy", 0);
            }
            return 1;
        }

        public static boolean isRegAllowed(Context context, int i) {
            if (RcsUtils.isSingleRegiRequiredAndAndroidMessageAppInUsed(context, i)) {
                return true;
            }
            if (i == SimUtil.getActiveDataPhoneId() && (!needToCheckOmcCodeAndSimMno(i) || !SimUtil.isDualIMS())) {
                return true;
            }
            if (!SimUtil.isDualIMS()) {
                return false;
            }
            if (!SimUtil.isDishCrossOver() && !SimUtil.isCctChaCBRS(i)) {
                return dualRcsPolicyCase(context, i);
            }
            Log.i(RcsUtils.LOG_TAG, "DishCross or CctChaCBRS - isRegAllowed: return true");
            return true;
        }

        public static boolean dualRcsPolicyCase(Context context, int i) {
            int dualRcsPolicy = getDualRcsPolicy(i);
            int i2 = i == 0 ? 1 : 0;
            String r4 = RcsUtils.LOG_TAG;
            IMSLog.i(r4, i, "dualRcsPolicyCase: policy " + dualRcsPolicy);
            if (dualRcsPolicy == 0) {
                return false;
            }
            if (dualRcsPolicy == 1) {
                Mno simMno = SimUtil.getSimMno(i);
                String str = OmcCode.get();
                return RcsUtils.getMatchedSalesCode(str, simMno).equals(str) && (simMno.equals(SimUtil.getSimMno(i2)) || i == SimUtil.getActiveDataPhoneId());
            } else if (dualRcsPolicy == 2) {
                return !UiUtils.isRcsEnabledinSettings(context, i2) || ImsRegistry.getRcsProfileType(i2).equals(ImsRegistry.getRcsProfileType(i));
            } else {
                if (dualRcsPolicy == 3) {
                    return true;
                }
                if (dualRcsPolicy == 4) {
                    String representSalesCode = RcsUtils.getRepresentSalesCode(OmcCode.get());
                    String representSalesCode2 = RcsUtils.getRepresentSalesCode(OmcCode.getNWCode(i));
                    String representSalesCode3 = RcsUtils.getRepresentSalesCode(OmcCode.getNWCode(i2));
                    String r42 = RcsUtils.LOG_TAG;
                    IMSLog.i(r42, i, "dualRcsPolicyCase: omcCode: " + representSalesCode + ", omcNwCode: " + representSalesCode2 + ", counterOmcNwCode: " + representSalesCode3);
                    return representSalesCode.equals(representSalesCode2) && (i == SimUtil.getActiveDataPhoneId() || representSalesCode2.equals(representSalesCode3));
                } else if (dualRcsPolicy == 5) {
                    return !UiUtils.isRcsEnabledinSettings(context, i2) || SimUtil.getSimMno(i).equals(SimUtil.getSimMno(i2));
                } else {
                    String r7 = RcsUtils.LOG_TAG;
                    Log.i(r7, "dualRcsPolicyCase: Invalid policy " + dualRcsPolicy);
                    return false;
                }
            }
        }
    }

    private static String[] getProperSalesCodeIfEmpty(String str) {
        return (("GCI_US".equals(str) || "Geoverse_US".equals(str) || "Union_US".equals(str) || "DPAC_US".equals(str) || "GTA_US".equals(str) || "ITE_US".equals(str) || "ASTCA_US".equals(str)) ? "XAA" : "Interop_US".equals(str) ? "ACG" : "").split(",");
    }

    /* access modifiers changed from: private */
    public static String getMatchedSalesCode(String str, Mno mno) {
        String[] allSalesCodes = mno.getAllSalesCodes();
        if (TextUtils.isEmpty(allSalesCodes[0])) {
            allSalesCodes = getProperSalesCodeIfEmpty(mno.getName());
        }
        for (String equals : allSalesCodes) {
            if (equals.equals(str)) {
                return str;
            }
        }
        return allSalesCodes[0];
    }

    public static String getRepresentSalesCode(String str) {
        if (TextUtils.equals(str, "APP")) {
            return "ATT";
        }
        return TextUtils.equals(str, "VPP") ? "VZW" : str;
    }

    public static class UiUtils {
        public static final int RCS_PREF_ALWAYS_ASK = 2;
        public static final int RCS_PREF_ALWAYS_CONNECT = 1;
        public static final int RCS_PREF_NEVER = 0;
        /* access modifiers changed from: private */
        public static boolean mHasRcsUserConsent = false;
        private static AlertDialog mRcsPdnDialog;

        public static boolean isMainSwitchVisible(Context context, int i) throws RemoteException {
            ISimManager simManagerFromSimSlot = SimManagerFactory.getSimManagerFromSimSlot(i);
            if (simManagerFromSimSlot == null || !simManagerFromSimSlot.isSimAvailable()) {
                return false;
            }
            boolean z = ImsRegistry.getBoolean(i, GlobalSettingsConstants.RCS.SHOW_MAIN_SWITCH, false);
            boolean isRcsEnabledinSettings = isRcsEnabledinSettings(context, i);
            String r2 = RcsUtils.LOG_TAG;
            IMSLog.i(r2, i, "isMainSwitchVisible: mIsVisible= " + z + ", rcsEnabled= " + isRcsEnabledinSettings);
            if (!z || !isRcsEnabledinSettings) {
                return false;
            }
            return true;
        }

        public static boolean isRcsEnabledinSettings(Context context, int i) {
            return isRcsEnabledInImsSwitch(context, i);
        }

        private static boolean isRcsEnabledInImsSwitch(Context context, int i) {
            ContentValues mnoInfo;
            ISimManager simManagerFromSimSlot = SimManagerFactory.getSimManagerFromSimSlot(i);
            if (simManagerFromSimSlot == null || (mnoInfo = simManagerFromSimSlot.getMnoInfo()) == null) {
                return false;
            }
            if (simManagerFromSimSlot.isLabSimCard() || SimUtil.isSoftphoneEnabled() || CollectionUtils.getBooleanValue(mnoInfo, ImsServiceSwitch.ImsSwitch.RCS.ENABLE_RCS, false) || RcsUtils.isSingleRegiRequiredAndAndroidMessageAppInUsed(context, i)) {
                return true;
            }
            return false;
        }

        public static boolean isSameRcsOperator(ImsProfile imsProfile, ImsProfile imsProfile2) {
            String str;
            String str2;
            String mnoName = imsProfile.getMnoName();
            String rcsConfigMark = imsProfile.getRcsConfigMark();
            if (imsProfile2 != null) {
                str2 = imsProfile2.getRcsConfigMark();
                str = imsProfile2.getMnoName();
                if (str.length() > 3) {
                    str = str.substring(0, str.length() - 3);
                }
            } else {
                str = "";
                str2 = str;
            }
            if (mnoName.length() > 3) {
                mnoName = mnoName.substring(0, mnoName.length() - 3);
            }
            String r2 = RcsUtils.LOG_TAG;
            Log.i(r2, "isSameOperatorByProfile: rcsConfigMark = " + rcsConfigMark + ", otherSlotRcsConfigMark = " + str2 + ", mnoName = " + mnoName + ", otherSlotMnoName = " + str);
            if ("".equals(rcsConfigMark) || "".equals(str2)) {
                if (!mnoName.equals(str)) {
                    return false;
                }
                return true;
            } else if (!rcsConfigMark.equals(str2)) {
                return false;
            } else {
                return true;
            }
        }

        public static boolean isRcsEnabledEnrichedCalling(int i) {
            ISimManager simManagerFromSimSlot = SimManagerFactory.getSimManagerFromSimSlot(i);
            if (simManagerFromSimSlot == null || !simManagerFromSimSlot.isSimAvailable()) {
                return false;
            }
            boolean anyMatch = Arrays.stream(ImsRegistry.getRegistrationManager().getProfileList(i)).anyMatch(new RcsUtils$UiUtils$$ExternalSyntheticLambda0());
            String r0 = RcsUtils.LOG_TAG;
            Log.i(r0, "isEnrichedCalling = " + anyMatch);
            return anyMatch;
        }

        /* access modifiers changed from: private */
        public static /* synthetic */ boolean lambda$isRcsEnabledEnrichedCalling$0(ImsProfile imsProfile) {
            return imsProfile != null && !imsProfile.hasEmergencySupport() && imsProfile.hasService("ec", -1);
        }

        public static boolean getRcsUserConsent(Context context, ITelephonyManager iTelephonyManager, int i) {
            int userConfig = UserConfiguration.getUserConfig(context, i, "rcs_roaming_pref", 1);
            int userConfig2 = UserConfiguration.getUserConfig(context, i, "rcs_home_pref", 1);
            boolean isNetworkRoaming = iTelephonyManager.isNetworkRoaming();
            if (!isNetworkRoaming) {
                userConfig = userConfig2;
            }
            String r2 = RcsUtils.LOG_TAG;
            Log.i(r2, "getRcsUserConsent: rcsConnectPref = " + userConfig + " , isRoaming = " + isNetworkRoaming);
            if (mHasRcsUserConsent) {
                mHasRcsUserConsent = false;
                return true;
            } else if (userConfig == 0) {
                if (!SimUtil.getSimMno(i).isKor()) {
                    return false;
                }
                setRcsPrefValue(context, i, isNetworkRoaming, 1);
                return true;
            } else if (userConfig != 2) {
                return true;
            } else {
                if (NetworkUtil.isMobileDataOn(context) && ImsConstants.SystemSettings.AIRPLANE_MODE.get(context, 0) != ImsConstants.SystemSettings.AIRPLANE_MODE_ON) {
                    showPdnConfirmation(context, isNetworkRoaming);
                }
                return false;
            }
        }

        private static DialogInterface.OnClickListener createRcsPdnPrefClickListener(final Context context, final boolean z, final int i) {
            return new DialogInterface.OnClickListener() {
                public void onClick(DialogInterface dialogInterface, int i) {
                    dialogInterface.dismiss();
                    String r3 = RcsUtils.LOG_TAG;
                    Log.i(r3, "User preference for RCS PDN: " + i + " (roaming: " + z + ")");
                    int activeDataPhoneId = SimUtil.getActiveDataPhoneId();
                    UiUtils.setRcsPrefValue(context, activeDataPhoneId, z, i);
                    if (i != 0) {
                        UiUtils.mHasRcsUserConsent = true;
                        ImsRegistry.getRegistrationManager().requestTryRegister(activeDataPhoneId);
                    }
                }
            };
        }

        /* access modifiers changed from: private */
        public static void setRcsPrefValue(Context context, int i, boolean z, int i2) {
            if (z) {
                UserConfiguration.setUserConfig(context, i, "rcs_roaming_pref", i2);
            } else {
                UserConfiguration.setUserConfig(context, i, "rcs_home_pref", i2);
            }
        }

        private static void showPdnConfirmation(Context context, boolean z) {
            String str;
            if (!OmcCode.isKOROmcCode() && !OmcCode.isChinaOmcCode() && !OmcCode.isJPNOmcCode() && !NSDSNamespaces.NSDSSettings.CHANNEL_NAME_TMO.equals(OmcCode.get()) && !"VZW".equals(OmcCode.get()) && !"ATT".equals(OmcCode.get()) && !"APP".equals(OmcCode.get()) && !"BMC".equals(OmcCode.get())) {
                AlertDialog alertDialog = mRcsPdnDialog;
                if (alertDialog == null || !alertDialog.isShowing()) {
                    AlertDialog.Builder builder = new AlertDialog.Builder(context, 16974546);
                    builder.setTitle(context.getResources().getString(R.string.dialog_title_rcs_service));
                    if (z) {
                        str = context.getResources().getString(R.string.dialog_text_rcs_pdn_pref_roaming);
                    } else {
                        str = context.getResources().getString(R.string.dialog_text_rcs_pdn_pref_home);
                    }
                    builder.setMessage(str);
                    builder.setPositiveButton(context.getResources().getString(R.string.dialog_text_rcs_pdn_pref_allow_always), createRcsPdnPrefClickListener(context, z, 1));
                    builder.setNeutralButton(context.getResources().getString(R.string.dialog_text_rcs_pdn_pref_allow_once), createRcsPdnPrefClickListener(context, z, 2));
                    builder.setNegativeButton(context.getResources().getString(R.string.dialog_text_rcs_pdn_pref_deny), createRcsPdnPrefClickListener(context, z, 0));
                    AlertDialog create = builder.create();
                    mRcsPdnDialog = create;
                    create.getWindow().setType(2038);
                    mRcsPdnDialog.getWindow().addFlags(65792);
                    mRcsPdnDialog.setCanceledOnTouchOutside(false);
                    mRcsPdnDialog.setCancelable(false);
                    mRcsPdnDialog.show();
                }
            }
        }
    }

    public static boolean isAutoConfigNeeded(Set<String> set) {
        HashSet hashSet = new HashSet(set);
        hashSet.retainAll(Arrays.asList(ImsProfile.getRcsServiceList()));
        return !hashSet.isEmpty();
    }

    public static boolean isSingleIncludedForTss() {
        boolean contains = OmcCode.getPath().contains("single");
        String str = LOG_TAG;
        Log.i(str, "isSingleIncludedForTss " + contains);
        return contains;
    }

    public static boolean isRcsEnabledByProfile(Context context, int i) {
        boolean z;
        Iterator<ImsProfile> it = ImsProfileLoaderInternal.getProfileList(context, i).iterator();
        while (true) {
            if (it.hasNext()) {
                if (it.next().getEnableRcs()) {
                    z = true;
                    break;
                }
            } else {
                z = false;
                break;
            }
        }
        String str = LOG_TAG;
        Log.i(str, "isRcsEnabledByProfile = " + z);
        return z;
    }

    public static boolean isTssSecondVers() {
        if (!CloudMessageProviderContract.JsonData.TRUE.equals(SemSystemProperties.get("mdc.singlesku")) || !CloudMessageProviderContract.JsonData.TRUE.equals(SemSystemProperties.get("mdc.unified"))) {
            return false;
        }
        Log.i(LOG_TAG, "isTssSecondVersion = true");
        return true;
    }

    private static boolean readBooleanCarrierConfigValue(Context context, int i, String str) {
        CarrierConfigManager carrierConfigManager = (CarrierConfigManager) context.getSystemService("carrier_config");
        if (carrierConfigManager == null) {
            IMSLog.e(LOG_TAG, i, "readBooleanCarrierConfigValue: CarrierConfigManager is null");
            return false;
        }
        PersistableBundle configForSubId = carrierConfigManager.getConfigForSubId(SimUtil.getSubId(i));
        if (configForSubId != null) {
            return configForSubId.getBoolean(str, false);
        }
        IMSLog.e(LOG_TAG, i, "readBooleanCarrierConfigValue: PersistableBundle is null");
        return false;
    }

    public static boolean isSingleRegiRequiredAndAndroidMessageAppInUsed(Context context, int i) {
        return SimUtil.getSimMno(i).isUSA() && isImsSingleRegiRequired(context, i) && ConfigUtil.isGoogDmaPackageInuse(context, i);
    }

    public static boolean isImsSingleRegiRequired(Context context, int i) {
        return readBooleanCarrierConfigValue(context, i, "ims.ims_single_registration_required_bool");
    }

    public static boolean isSrRcsOptionsEnabled(Context context, int i) {
        return readBooleanCarrierConfigValue(context, i, "use_rcs_sip_options_bool");
    }

    public static boolean isSrRcsPresenceEnabled(Context context, int i) {
        return readBooleanCarrierConfigValue(context, i, "ims.enable_presence_publish_bool");
    }

    public static boolean isSrPresenceCapabilityExchange(Context context, int i) {
        return readBooleanCarrierConfigValue(context, i, "ims.enable_presence_capability_exchange_bool");
    }

    public static boolean isSrBulkCapabilityExchange(Context context, int i) {
        return readBooleanCarrierConfigValue(context, i, "ims.rcs_bulk_capability_exchange_bool");
    }

    public static boolean isSrEnablePresenceGroupSubscribe(Context context, int i) {
        return readBooleanCarrierConfigValue(context, i, "ims.enable_presence_group_subscribe_bool");
    }

    public static long getEpochNanosec() {
        return System.nanoTime() + (((System.currentTimeMillis() - 600000) * 1000000) - System.nanoTime());
    }
}
