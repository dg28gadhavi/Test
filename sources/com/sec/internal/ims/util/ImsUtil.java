package com.sec.internal.ims.util;

import android.content.ContentValues;
import android.content.Context;
import android.content.Intent;
import android.net.ConnectivityManager;
import android.net.Network;
import android.net.NetworkCapabilities;
import android.os.RemoteException;
import android.os.SemSystemProperties;
import android.os.ServiceManager;
import android.provider.Settings;
import android.telephony.CellIdentity;
import android.telephony.CellIdentityGsm;
import android.telephony.CellIdentityLte;
import android.telephony.CellIdentityNr;
import android.telephony.CellIdentityTdscdma;
import android.telephony.CellIdentityWcdma;
import android.telephony.CellInfo;
import android.text.TextUtils;
import android.util.Log;
import com.android.internal.telephony.ISemTelephony;
import com.samsung.android.feature.SemFloatingFeature;
import com.sec.ims.ImsRegistration;
import com.sec.ims.options.Capabilities;
import com.sec.ims.settings.ImsProfile;
import com.sec.ims.util.ImsUri;
import com.sec.internal.constants.Mno;
import com.sec.internal.constants.ims.ImsConstants;
import com.sec.internal.constants.ims.SipMsg$$ExternalSyntheticLambda2;
import com.sec.internal.constants.ims.cmstore.data.MessageContextValues;
import com.sec.internal.constants.ims.config.ConfigConstants;
import com.sec.internal.constants.ims.os.SecFeature;
import com.sec.internal.constants.ims.settings.GlobalSettingsConstants;
import com.sec.internal.google.SecImsNotifier;
import com.sec.internal.helper.DmConfigHelper;
import com.sec.internal.helper.ImsSharedPrefHelper;
import com.sec.internal.helper.OmcCode;
import com.sec.internal.helper.RcsConfigurationHelper;
import com.sec.internal.helper.SimUtil;
import com.sec.internal.helper.UriUtil;
import com.sec.internal.helper.os.DeviceUtil;
import com.sec.internal.helper.os.IntentUtil;
import com.sec.internal.ims.core.SlotBasedConfig;
import com.sec.internal.ims.core.sim.SimManagerFactory;
import com.sec.internal.ims.imsservice.ImsServiceStub;
import com.sec.internal.ims.rcs.util.RcsUtils;
import com.sec.internal.ims.registry.ImsRegistry;
import com.sec.internal.ims.servicemodules.im.ImModule;
import com.sec.internal.ims.servicemodules.tapi.service.api.ChatServiceImpl;
import com.sec.internal.ims.settings.DeviceConfigManager;
import com.sec.internal.ims.settings.ImsAutoUpdate;
import com.sec.internal.imscr.LogClass;
import com.sec.internal.interfaces.ims.core.ISimManager;
import com.sec.internal.interfaces.ims.servicemodules.presence.IPresenceModule;
import com.sec.internal.interfaces.ims.servicemodules.ss.IUtServiceModule;
import com.sec.internal.log.IMSLog;
import java.security.SecureRandom;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Objects;
import java.util.Set;
import java.util.stream.Stream;

public class ImsUtil {
    public static final String IPME_STATUS = "ipme_status";
    private static final String LOG_TAG = "ImsUtil";
    private static final SecureRandom sSecureRandom = new SecureRandom();

    public static int getHandle(long j) {
        return (int) j;
    }

    public static boolean isRttModeOnFromCallSettings(Context context, int i) {
        int i2 = Settings.Secure.getInt(context.getContentResolver(), "preferred_rtt_mode", 0);
        String str = LOG_TAG;
        IMSLog.d(str, i, "preferred_rtt_mode" + " : " + i2);
        if (i2 != 0) {
            return true;
        }
        return false;
    }

    public static boolean isCdmalessModel(Context context, int i) {
        if (SemSystemProperties.getInt(ImsConstants.SystemProperties.FIRST_API_VERSION, 0) >= 31 || "FKR".equalsIgnoreCase(OmcCode.getNWCode(i))) {
            return true;
        }
        if ("LRA".equalsIgnoreCase(OmcCode.get())) {
            return ImsAutoUpdate.getInstance(context, i).getDefaultGlobalSettingsFromImsUpdate(GlobalSettingsConstants.Registration.EXCEPTIVE_CDMA_LESS_FOR_LRA);
        }
        String string = SemFloatingFeature.getInstance().getString(SecFeature.FLOATING.CDMALESS);
        if (TextUtils.isEmpty(string)) {
            IMSLog.i(LOG_TAG, i, "CDMALESS Not enabled");
            return false;
        }
        Mno simMno = SimUtil.getSimMno(i);
        List asList = Arrays.asList(simMno.getAllSalesCodes());
        String str = (String) asList.get(0);
        List asList2 = Arrays.asList(string.split(","));
        Stream skip = asList.stream().skip(1);
        Objects.requireNonNull(asList2);
        IMSLog.assertFalse(LOG_TAG, i, "SEC_FLOATING_FEATURE_COMMON_CDMALESS SHOULD has first salescode [" + str + "]!", skip.anyMatch(new ImsUtil$$ExternalSyntheticLambda0(asList2)));
        if (!Boolean.parseBoolean(string) || Mno.VZW != simMno) {
            Stream map = asList2.stream().map(new SipMsg$$ExternalSyntheticLambda2());
            Objects.requireNonNull(str);
            if (!map.anyMatch(new ImsUtil$$ExternalSyntheticLambda1(str))) {
                return false;
            }
        }
        return true;
    }

    public static boolean isCdmalessEnabled(Context context, int i) {
        return isCdmalessEnabled(context, i, isSimMobilityActivated(i));
    }

    public static boolean isCdmalessEnabled(Context context, int i, boolean z) {
        ISimManager simManagerFromSimSlot = SimManagerFactory.getSimManagerFromSimSlot(i);
        if (simManagerFromSimSlot != null) {
            return (!DeviceUtil.isTablet() && z) || isCdmalessModel(context, i) || isVzwInboundWithCdmaLess(simManagerFromSimSlot.getSimMno(), simManagerFromSimSlot.getSimMnoName());
        }
        Log.d(LOG_TAG, "isCdmalessEnabled, SIM not ready");
        return isCdmalessModel(context, i);
    }

    public static boolean isVzwInboundWithCdmaLess(Mno mno, String str) {
        if (mno != Mno.VZW) {
            return false;
        }
        String str2 = OmcCode.get();
        for (String equals : Mno.VZW.getAllSalesCodes()) {
            if (TextUtils.equals(equals, str2)) {
                return false;
            }
        }
        if (!"VZW_US:TFN".equalsIgnoreCase(str) || !"TFN".equalsIgnoreCase(str2)) {
            return true;
        }
        return false;
    }

    public static String getPathWithPhoneId(String str, int i) {
        return str + "#" + "simslot" + i;
    }

    public static boolean isSimMobilityActivated(int i) {
        return SlotBasedConfig.getInstance(i).isSimMobilityActivated();
    }

    public static boolean isSimMobilityActivatedForRcs(int i) {
        return SlotBasedConfig.getInstance(i).isSimMobilityActivatedForRcs();
    }

    public static boolean isSimMobilityActivatedForAmRcs(Context context, int i) {
        return isSimMobilityActivated(i) && RcsUtils.isSingleRegiRequiredAndAndroidMessageAppInUsed(context, i);
    }

    public static boolean isMatchedService(Set<String> set, String str) {
        String[] strArr;
        if ("volte".equals(str)) {
            strArr = ImsProfile.getVoLteServiceList();
        } else if (DeviceConfigManager.RCS.equals(str)) {
            strArr = ImsProfile.getRcsServiceList();
        } else if (ChatServiceImpl.SUBJECT.equals(str)) {
            strArr = ImsProfile.getChatServiceList();
        } else {
            Log.d(LOG_TAG, "invalid service type : " + str);
            return false;
        }
        for (String contains : strArr) {
            if (set.contains(contains)) {
                return true;
            }
        }
        return false;
    }

    public static void updateSsDomain(Context context, int i, String str) {
        String str2 = LOG_TAG;
        Log.d(str2, "update SS domain : " + str);
        ContentValues contentValues = new ContentValues();
        contentValues.put(GlobalSettingsConstants.SS.DOMAIN, str);
        context.getContentResolver().update(UriUtil.buildUri(GlobalSettingsConstants.CONTENT_URI.toString(), i), contentValues, (String) null, (String[]) null);
        IUtServiceModule utServiceModule = ImsRegistry.getServiceModuleManager().getUtServiceModule();
        if (utServiceModule != null) {
            utServiceModule.updateCapabilities(i);
        }
    }

    public static List<CellInfo> getAvailableCellInfo(List<CellInfo> list) {
        ArrayList arrayList = new ArrayList();
        if (list != null && !list.isEmpty()) {
            for (CellInfo next : list) {
                CellIdentity cellIdentity = next.getCellIdentity();
                if (cellIdentity instanceof CellIdentityGsm) {
                    CellIdentityGsm cellIdentityGsm = (CellIdentityGsm) cellIdentity;
                    if (!(cellIdentityGsm.getLac() == Integer.MAX_VALUE || cellIdentityGsm.getCid() == Integer.MAX_VALUE)) {
                        arrayList.add(next);
                    }
                } else if (cellIdentity instanceof CellIdentityWcdma) {
                    CellIdentityWcdma cellIdentityWcdma = (CellIdentityWcdma) cellIdentity;
                    if (!(cellIdentityWcdma.getLac() == Integer.MAX_VALUE || cellIdentityWcdma.getCid() == Integer.MAX_VALUE)) {
                        arrayList.add(next);
                    }
                } else if (cellIdentity instanceof CellIdentityTdscdma) {
                    CellIdentityTdscdma cellIdentityTdscdma = (CellIdentityTdscdma) cellIdentity;
                    if (!(cellIdentityTdscdma.getLac() == Integer.MAX_VALUE || cellIdentityTdscdma.getCid() == Integer.MAX_VALUE)) {
                        arrayList.add(next);
                    }
                } else if (cellIdentity instanceof CellIdentityLte) {
                    CellIdentityLte cellIdentityLte = (CellIdentityLte) cellIdentity;
                    if (!(cellIdentityLte.getTac() == Integer.MAX_VALUE || cellIdentityLte.getCi() == Integer.MAX_VALUE)) {
                        arrayList.add(next);
                    }
                } else if (cellIdentity instanceof CellIdentityNr) {
                    CellIdentityNr cellIdentityNr = (CellIdentityNr) cellIdentity;
                    if (!(cellIdentityNr.getTac() == Integer.MAX_VALUE || cellIdentityNr.getNci() == Long.MAX_VALUE)) {
                        arrayList.add(next);
                    }
                }
            }
        }
        String str = LOG_TAG;
        IMSLog.s(str, "getAvailableCellInfo : " + arrayList);
        return arrayList;
    }

    public static boolean needForceToUsePsE911(int i, boolean z) {
        return ImsRegistry.getString(i, GlobalSettingsConstants.Call.EMERGENCY_CALL_DOMAIN, "CS").equals("CS") && !z;
    }

    public static void notifyImsProfileLoaded(Context context, int i) {
        IMSLog.i(LOG_TAG, i, "notifyImsProfileLoaded");
        Intent intent = new Intent();
        intent.setAction(ImsConstants.Intents.ACTION_IMS_PROFILE_LOADED);
        intent.putExtra(ImsConstants.Intents.EXTRA_ANDORID_PHONE_ID, i);
        intent.addFlags(LogClass.SIM_EVENT);
        intent.setPackage("com.sec.epdg");
        IntentUtil.sendBroadcast(context, intent);
    }

    public static void updateEmergencyCallDomain(Context context, int i, ImsProfile imsProfile, ISimManager iSimManager, String str) {
        boolean z;
        String str2 = LOG_TAG;
        IMSLog.d(str2, i, "updateEmergencyCallDomain:");
        if (iSimManager != null) {
            IMSLog.d(str2, i, "emergencyCallDomain: " + str);
            String str3 = "PS";
            boolean z2 = true;
            boolean z3 = str3.equalsIgnoreCase(str) || "IMS".equalsIgnoreCase(str);
            IMSLog.d(str2, i, "emergencyCallDomain: isPsDomainInSettings-" + z3 + ", SIM absent-" + iSimManager.hasNoSim());
            if (!iSimManager.hasNoSim()) {
                String string = ImsSharedPrefHelper.getString(i, context, ImsSharedPrefHelper.GLOBAL_SETTINGS, "originalEmergencyCallDomain", str);
                Mno simMno = iSimManager.getSimMno();
                boolean z4 = !simMno.isEur() && !simMno.isMea() && !simMno.isSea() && !simMno.isSwa() && !simMno.isOce() && !simMno.isTw();
                if ((imsProfile != null && imsProfile.getSimMobility() && z4) || DeviceUtil.getGcfMode()) {
                    string = str3;
                }
                IMSLog.d(str2, i, "emergencyCallDomain: targetDomain from globalsetting-" + string);
                z = str3.equalsIgnoreCase(string);
            } else {
                z = updateEmergencyCallDomainForNoSim(i, iSimManager, imsProfile, false);
                if (TextUtils.isEmpty(ImsSharedPrefHelper.getString(i, context, ImsSharedPrefHelper.GLOBAL_SETTINGS, "originalEmergencyCallDomain", ""))) {
                    ImsSharedPrefHelper.save(i, context, ImsSharedPrefHelper.GLOBAL_SETTINGS, "originalEmergencyCallDomain", str);
                }
            }
            Mno devMno = iSimManager.getDevMno();
            if (devMno.isAus() || z3 != z) {
                if (!devMno.isAus()) {
                    if (z) {
                        if (imsProfile == null) {
                            z2 = false;
                        }
                        IMSLog.d(str2, i, "hasEmergencyProfile: " + z2);
                        if (!z2) {
                            IMSLog.d(str2, i, "emergencyCallDomain: no E911 profile keep e-domain as CS");
                            return;
                        }
                    }
                    z2 = z;
                }
                if (!z2) {
                    str3 = "CS";
                }
                IMSLog.d(str2, i, "update emergency Domain: " + str + " => " + str3);
                ContentValues contentValues = new ContentValues();
                contentValues.put(GlobalSettingsConstants.Call.EMERGENCY_CALL_DOMAIN, str3);
                context.getContentResolver().update(UriUtil.buildUri(GlobalSettingsConstants.CONTENT_URI.toString(), i), contentValues, (String) null, (String[]) null);
                return;
            }
            StringBuilder sb = new StringBuilder();
            sb.append("emergencyCallDomain: already ");
            if (!z) {
                str3 = "CS";
            }
            sb.append(str3);
            Log.d(str2, sb.toString());
        }
    }

    private static boolean updateEmergencyCallDomainForNoSim(int i, ISimManager iSimManager, ImsProfile imsProfile, boolean z) {
        boolean z2;
        Mno fromSalesCode = Mno.fromSalesCode(OmcCode.get());
        Mno simMno = iSimManager.getSimMno();
        if (fromSalesCode == simMno || fromSalesCode.isChn() || fromSalesCode.isHkMo() || fromSalesCode.isEmeasewaoce() || fromSalesCode.isTw() || (DeviceUtil.isUnifiedSalesCodeInTSS() && ("EUX".equals(OmcCode.get()) || "EUY".equals(OmcCode.get())))) {
            z2 = "PS".equalsIgnoreCase(ImsRegistry.getString(i, GlobalSettingsConstants.Call.EMERGENCY_CALL_DOMAIN_WITHOUT_SIM, "CS"));
        } else {
            z2 = true;
        }
        IMSLog.d(LOG_TAG, i, "supportNoSimPsE911: " + z2 + ", simMno: " + simMno.getName() + ", salesCodeMno: " + fromSalesCode.getName());
        if (z2) {
            z = true;
        }
        if ((OmcCode.isChinaOmcCode() || OmcCode.isJPNOmcCode()) && DeviceUtil.getGcfMode()) {
            z = true;
        }
        if (OmcCode.isDCMOmcCode()) {
            z = true;
        }
        if (iSimManager.getNetMno() == Mno.KDDI) {
            return true;
        }
        return z;
    }

    public static boolean isPttSupported() {
        boolean z = SemFloatingFeature.getInstance().getBoolean(SecFeature.FLOATING.SUPPORT_PTT);
        String str = LOG_TAG;
        Log.d(str, "isPttSupported: " + z);
        return z;
    }

    public static String hideInfo(String str, int i) {
        try {
            if (!TextUtils.isEmpty(str)) {
                return str.substring(0, Math.min(i, str.length()));
            }
            return MessageContextValues.none;
        } catch (StringIndexOutOfBoundsException unused) {
            Log.d(LOG_TAG, "hideInfo had OutOfBoundeEception");
            return MessageContextValues.none;
        }
    }

    public static void listToDumpFormat(int i, int i2, String str, List<String> list) {
        try {
            list.add(0, Integer.toString(i2));
            list.add(1, hideInfo(str, 4));
            IMSLog.c(i, String.join(",", list));
        } catch (Exception unused) {
            Log.e(LOG_TAG, "listToDumpFormat has an exception");
        }
    }

    public static void listToDumpFormat(int i, int i2, String str) {
        listToDumpFormat(i, i2, hideInfo(str, 4), new ArrayList());
    }

    public static String getPublicId(int i) {
        ImsUri uri;
        String str = null;
        if (ImsServiceStub.getInstance() == null) {
            IMSLog.d(LOG_TAG, i, "getImModule: getInstance is null");
            return null;
        }
        ImsRegistration imsRegistration = ((ImModule) ImsServiceStub.getInstance().getServiceModuleManager().getImModule()).getImsRegistration(i);
        if (imsRegistration == null || (uri = imsRegistration.getPreferredImpu().getUri()) == null) {
            ISimManager simManagerFromSimSlot = SimManagerFactory.getSimManagerFromSimSlot(i);
            if (simManagerFromSimSlot != null) {
                str = simManagerFromSimSlot.getImpuFromIsim(0);
                if (TextUtils.isEmpty(str)) {
                    str = simManagerFromSimSlot.getDerivedImpu();
                }
            }
            if (!TextUtils.isEmpty(str)) {
                return str;
            }
            IMSLog.e(LOG_TAG, i, "There is no impu");
            return "";
        }
        String str2 = LOG_TAG;
        IMSLog.d(str2, i, "getPublicId: registered IMPU=" + IMSLog.checker(uri.toString()));
        return uri.toString();
    }

    private static boolean checkIR94PolicyForUsDsds(int i) {
        int i2 = i == 0 ? 1 : 0;
        String representSalesCode = RcsUtils.getRepresentSalesCode(OmcCode.get());
        String representSalesCode2 = RcsUtils.getRepresentSalesCode(OmcCode.getNWCode(i));
        String representSalesCode3 = RcsUtils.getRepresentSalesCode(OmcCode.getNWCode(i2));
        String str = LOG_TAG;
        IMSLog.i(str, i, "checkIR94PolicyForUsDsds: omcCode: " + representSalesCode + ", omcNwCode: " + representSalesCode2 + ", counterOmcNwCode: " + representSalesCode3);
        return representSalesCode.equals(representSalesCode2) && (i == SimUtil.getActiveDataPhoneId() || representSalesCode2.equals(representSalesCode3));
    }

    public static boolean isDualVideoCallAllowed(int i) {
        if (SimUtil.getPhoneCount() < 2 || SimUtil.isDishCrossOver()) {
            return true;
        }
        if (isSimMobilityActivated(i)) {
            int activeDataPhoneId = SimUtil.getActiveDataPhoneId();
            String str = LOG_TAG;
            IMSLog.i(str, i, "isDualVideoCallAllowed: Check video for ADS: " + activeDataPhoneId);
            if (i == activeDataPhoneId) {
                return true;
            }
            return false;
        }
        boolean checkIR94PolicyForUsDsds = checkIR94PolicyForUsDsds(i);
        String str2 = LOG_TAG;
        IMSLog.i(str2, i, "isDualVideoCallAllowed: Check video for IR94Policy: " + checkIR94PolicyForUsDsds);
        return checkIR94PolicyForUsDsds;
    }

    /* JADX WARNING: Code restructure failed: missing block: B:9:0x0019, code lost:
        if (r1 != null) goto L_0x001d;
     */
    /* Code decompiled incorrectly, please refer to instructions dump. */
    public static java.lang.String getSystemProperty(java.lang.String r1, int r2, java.lang.String r3) {
        /*
            java.lang.String r1 = android.os.SemSystemProperties.get(r1)
            if (r1 == 0) goto L_0x001c
            int r0 = r1.length()
            if (r0 <= 0) goto L_0x001c
            java.lang.String r0 = ","
            java.lang.String[] r1 = r1.split(r0)
            if (r2 < 0) goto L_0x001c
            int r0 = r1.length
            if (r2 >= r0) goto L_0x001c
            r1 = r1[r2]
            if (r1 == 0) goto L_0x001c
            goto L_0x001d
        L_0x001c:
            r1 = 0
        L_0x001d:
            boolean r2 = android.text.TextUtils.isEmpty(r1)
            if (r2 == 0) goto L_0x0024
            goto L_0x0025
        L_0x0024:
            r3 = r1
        L_0x0025:
            return r3
        */
        throw new UnsupportedOperationException("Method not decompiled: com.sec.internal.ims.util.ImsUtil.getSystemProperty(java.lang.String, int, java.lang.String):java.lang.String");
    }

    public static int getRatInNoSimCase(int i, Context context) {
        String str = LOG_TAG;
        Log.d(str, "getRatInNoSimCase");
        ISemTelephony asInterface = ISemTelephony.Stub.asInterface(ServiceManager.getService("isemtelephony"));
        int i2 = 13;
        if (asInterface != null) {
            try {
                CellIdentity cellLocationBySubId = asInterface.getCellLocationBySubId(SimUtil.getSubId(i), context.getOpPackageName(), context.getAttributionTag());
                Log.d(str, "RAT from cellIdentity = " + cellLocationBySubId.getType());
                if (cellLocationBySubId.getType() == 6) {
                    i2 = 20;
                }
            } catch (RemoteException e) {
                e.printStackTrace();
            }
        } else {
            Log.e(str, "Unable to find ISemTelephony interface.");
        }
        Log.d(LOG_TAG, "final rat = " + i2);
        return i2;
    }

    public static int getComposerAuthValue(int i, Context context) {
        return RcsConfigurationHelper.readIntParam(context, getPathWithPhoneId(ConfigConstants.ConfigTable.SERVICES_COMPOSER_AUTH, i), DmConfigHelper.readInt(context, ConfigConstants.ConfigTable.SERVICES_COMPOSER_AUTH, 0, i)).intValue();
    }

    public static int getVBCAuthValue(int i, Context context) {
        return RcsConfigurationHelper.readIntParam(context, getPathWithPhoneId(ConfigConstants.ConfigTable.SERVICES_VBC_AUTH, i), 0).intValue();
    }

    public static boolean isWifiConnected(Context context) {
        ConnectivityManager connectivityManager = (ConnectivityManager) context.getSystemService("connectivity");
        if (connectivityManager == null) {
            IMSLog.d(LOG_TAG, "isWifiConnected: ConnectivityManager is null ");
            return false;
        }
        Network activeNetwork = connectivityManager.getActiveNetwork();
        if (activeNetwork == null) {
            IMSLog.d(LOG_TAG, "isWifiConnected: Default NW is null ");
            return false;
        }
        NetworkCapabilities networkCapabilities = connectivityManager.getNetworkCapabilities(activeNetwork);
        if (networkCapabilities == null || !networkCapabilities.hasTransport(1) || !networkCapabilities.hasCapability(12)) {
            return false;
        }
        return true;
    }

    public static boolean isAddMmtelCallComposerTag(int i, Context context) {
        if (SimUtil.getSimMno(i) == Mno.TMOUS) {
            int composerAuthValue = getComposerAuthValue(i, context);
            int vBCAuthValue = getVBCAuthValue(i, context);
            String str = LOG_TAG;
            IMSLog.i(str, i, "composerAuthVal: " + composerAuthValue + "vbcAuthVal" + vBCAuthValue);
            if (composerAuthValue == 0 && vBCAuthValue == 1) {
                IMSLog.i(str, i, "set isAddMmtelCallComposerTag as true");
                return true;
            }
        }
        return false;
    }

    public static boolean needForceRegiOrPublishForMmtelCallComposer(Context context, ImsProfile imsProfile, int i) {
        if (imsProfile == null || !imsProfile.hasService("mmtel-call-composer")) {
            IMSLog.e(LOG_TAG, i, "Profile is null or doesn't have mmtel-call-composer");
            return false;
        }
        int composerAuthValue = getComposerAuthValue(i, context);
        int vBCAuthValue = getVBCAuthValue(i, context);
        String str = LOG_TAG;
        IMSLog.i(str, i, "composerAuthVal : " + composerAuthValue + "vbcAuthVal : " + vBCAuthValue);
        boolean z = composerAuthValue == 2 || composerAuthValue == 3;
        boolean z2 = vBCAuthValue == 1;
        IPresenceModule presenceModule = ImsRegistry.getServiceModuleManager().getPresenceModule();
        if (!z && z2 && presenceModule.isOwnPresenceInfoHasTuple(i, Capabilities.FEATURE_MMTEL_CALL_COMPOSER)) {
            IMSLog.i(str, i, "needForceRegi or Publish: callcomposer tuple present case");
            return true;
        } else if (!z || presenceModule.isOwnPresenceInfoHasTuple(i, Capabilities.FEATURE_MMTEL_CALL_COMPOSER)) {
            IMSLog.i(str, i, "needForceRegi or Publish: return false");
            return false;
        } else {
            IMSLog.i(str, i, "needForceRegi or Publish: callcomposer tuple not present case");
            return true;
        }
    }

    public static boolean isMcsSupported(int i) {
        boolean z = false;
        if ((OmcCode.isKorOpenOnlyOmcCode() || OmcCode.isSKTOmcCode()) && ImsRegistry.getBoolean(i, GlobalSettingsConstants.CMS.CMS_MCS_ENABLED, false) && !DeviceUtil.isTablet()) {
            z = true;
        }
        String str = LOG_TAG;
        StringBuilder sb = new StringBuilder();
        sb.append("MCS is ");
        sb.append(z ? "support" : "not support");
        IMSLog.i(str, i, sb.toString());
        return z;
    }

    public static boolean isSingleRegiAppConnected(int i) {
        return SecImsNotifier.getInstance().hasSipDelegate(i);
    }

    public static SecureRandom getRandom() {
        return sSecureRandom;
    }
}
