package com.sec.internal.ims.util;

import android.content.ContentValues;
import android.content.Context;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.database.Cursor;
import android.net.Uri;
import android.os.Environment;
import android.provider.Telephony;
import android.text.TextUtils;
import android.util.Base64;
import android.util.Log;
import com.sec.ims.settings.ImsProfile;
import com.sec.internal.constants.Mno;
import com.sec.internal.constants.ims.ImsConstants;
import com.sec.internal.constants.ims.config.ConfigConstants;
import com.sec.internal.constants.ims.settings.GlobalSettingsConstants;
import com.sec.internal.helper.CollectionUtils;
import com.sec.internal.helper.ImsSharedPrefHelper;
import com.sec.internal.helper.OmcCode;
import com.sec.internal.helper.SimUtil;
import com.sec.internal.helper.os.ITelephonyManager;
import com.sec.internal.helper.os.PackageUtils;
import com.sec.internal.helper.os.TelephonyManagerWrapper;
import com.sec.internal.ims.config.workflow.WorkflowBase;
import com.sec.internal.ims.core.sim.SimManagerFactory;
import com.sec.internal.ims.rcs.util.RcsUtils;
import com.sec.internal.ims.registry.ImsRegistry;
import com.sec.internal.ims.settings.ImsProfileLoaderInternal;
import com.sec.internal.ims.settings.ImsServiceSwitch;
import com.sec.internal.ims.settings.ServiceConstants;
import com.sec.internal.interfaces.ims.core.IRegistrationManager;
import com.sec.internal.interfaces.ims.core.ISimManager;
import com.sec.internal.log.IMSLog;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.Locale;
import java.util.Map;
import javax.crypto.Cipher;
import javax.crypto.spec.IvParameterSpec;
import javax.crypto.spec.SecretKeySpec;

public class ConfigUtil {
    private static final String ALGORITHM = "AES";
    public static final String LOCAL_CONFIG_FILE = "localconfig";
    private static final String LOG_TAG = "ConfigUtil";
    public static final String SDCARD_CONFIG_FILE = "/localconfig/config-local.xml";
    private static final String SP_KEY_GLOBAL_GC_ENABLED = "globalgcenabled";
    public static final String TRANSFORMATION = "AES/CBC/PKCS5Padding";
    private static final String UTF8 = "UTF-8";
    private static final byte[] mAesIvBytes = {0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0};
    private static final byte[] mAesKeyBytes = {1, 2, 3, 4, 5, 6, 7, 8, 9, 0, 1, 2, 3, 4, 5, 6, 1, 2, 3, 4, 5, 6, 7, 8, 9, 0, 1, 2, 3, 4, 5, 6};

    public static String getAcsServerType(int i) {
        return ImsRegistry.getString(i, GlobalSettingsConstants.RCS.APPLICATION_SERVER, "");
    }

    public static String getAcsCustomServerUrl(int i) {
        return ImsRegistry.getString(i, GlobalSettingsConstants.RCS.CUSTOM_CONFIG_SERVER_URL, "");
    }

    public static String getNetworkType(int i) {
        return ImsRegistry.getString(i, GlobalSettingsConstants.RCS.NETWORK_TYPE, "ims,internet,wifi");
    }

    public static String getModelName(int i) {
        return ImsRegistry.getString(i, GlobalSettingsConstants.RCS.MODEL_NAME, "");
    }

    public static String getSmsType(int i) {
        return ImsRegistry.getString(i, GlobalSettingsConstants.RCS.OTP_SMS_TYPE, "");
    }

    public static String getSmsPort(int i) {
        return ImsRegistry.getString(i, GlobalSettingsConstants.RCS.OTP_SMS_PORT, "");
    }

    public static String getSetting(String str, int i) {
        return ImsRegistry.getString(i, str, "");
    }

    public static boolean isRcsChatEnabled(Context context, int i, ISimManager iSimManager, boolean z) {
        if (iSimManager != null) {
            ContentValues mnoInfo = iSimManager.getMnoInfo();
            z = (CollectionUtils.getBooleanValue(mnoInfo, ImsServiceSwitch.ImsSwitch.DeviceManagement.ENABLE_IMS, false) && CollectionUtils.getBooleanValue(mnoInfo, ImsServiceSwitch.ImsSwitch.RCS.ENABLE_RCS, false) && CollectionUtils.getBooleanValue(mnoInfo, ImsServiceSwitch.ImsSwitch.RCS.ENABLE_RCS_CHAT_SERVICE, false)) || RcsUtils.isSingleRegiRequiredAndAndroidMessageAppInUsed(context, i);
            String str = LOG_TAG;
            IMSLog.i(str, i, "isRcsChatEnabled: " + z);
        }
        return z;
    }

    public static String getRcsProfileWithFeature(Context context, int i, ImsProfile imsProfile) {
        if (imsProfile == null) {
            IMSLog.e(LOG_TAG, i, "getRcsProfileWithFeature: imsProfile: empty");
            return "";
        }
        String rcsProfile = imsProfile.getRcsProfile();
        String str = LOG_TAG;
        IMSLog.d(str, i, "getRcsProfileWithFeature: rcsProfile from imsProfile: " + rcsProfile);
        ISimManager simManagerFromSimSlot = SimManagerFactory.getSimManagerFromSimSlot(i);
        Mno netMno = simManagerFromSimSlot != null ? simManagerFromSimSlot.getNetMno() : Mno.DEFAULT;
        String acsServerType = getAcsServerType(i);
        if (!TextUtils.isEmpty(rcsProfile) && !ImsConstants.RCS_AS.JIBE.equals(acsServerType) && netMno == Mno.VZW) {
            if (!isRcsChatEnabled(context, i, simManagerFromSimSlot, rcsProfile.startsWith("UP"))) {
                IMSLog.d(str, i, "getRcsProfileWithFeature: use default rcsProfile");
                return "";
            }
            IMSLog.d(str, i, "getRcsProfileWithFeature: use " + rcsProfile + " rcsProfile");
        }
        return rcsProfile;
    }

    public static String getRcsProfileLoaderInternalWithFeature(Context context, String str, int i) {
        String str2;
        Mno mno = Mno.DEFAULT;
        ISimManager simManagerFromSimSlot = SimManagerFactory.getSimManagerFromSimSlot(i);
        if (simManagerFromSimSlot != null) {
            str2 = ImsProfileLoaderInternal.getRcsProfile(context, simManagerFromSimSlot.getSimMnoName(), i);
            mno = simManagerFromSimSlot.getNetMno();
        } else {
            str2 = ImsProfileLoaderInternal.getRcsProfile(context, str, i);
        }
        String str3 = LOG_TAG;
        IMSLog.d(str3, i, "getRcsProfileLoaderInternalWithFeature: rcsProfile: " + str2);
        String acsServerType = getAcsServerType(i);
        if (!TextUtils.isEmpty(str2) && !ImsConstants.RCS_AS.JIBE.equals(acsServerType) && mno == Mno.VZW) {
            if (!isRcsChatEnabled(context, i, simManagerFromSimSlot, str2.startsWith("UP"))) {
                IMSLog.d(str3, i, "getRcsProfileLoaderInternalWithFeature: use default rcsProfile");
                return "";
            }
            IMSLog.d(str3, i, "getRcsProfileLoaderInternalWithFeature: rcsProfile: " + str2);
        }
        return str2;
    }

    public static int getAutoconfigSourceWithFeature(int i, int i2) {
        int i3 = ImsRegistry.getInt(i, GlobalSettingsConstants.RCS.LOCAL_CONFIG_SERVER, i2);
        String acsServerType = getAcsServerType(i);
        String str = LOG_TAG;
        IMSLog.d(str, i, "getAutoconfigSourceWithFeature: " + i3 + " from globalSettings");
        ISimManager simManagerFromSimSlot = SimManagerFactory.getSimManagerFromSimSlot(i);
        Mno netMno = simManagerFromSimSlot != null ? simManagerFromSimSlot.getNetMno() : Mno.DEFAULT;
        if (!ImsConstants.RCS_AS.JIBE.equals(acsServerType) && netMno == Mno.VZW && i3 == 0) {
            if (!isRcsChatEnabled(ImsRegistry.getContext(), i, simManagerFromSimSlot, true)) {
                i3 = 2;
            }
            IMSLog.d(str, i, "getAutoconfigSourceWithFeature: use " + i3);
        }
        return i3;
    }

    public static boolean hasAcsProfile(Context context, int i, ISimManager iSimManager) {
        String str = LOG_TAG;
        IMSLog.d(str, i, "hasAcsProfile:");
        if (!isRcsAvailable(context, i, iSimManager)) {
            return false;
        }
        IRegistrationManager registrationManager = ImsRegistry.getRegistrationManager();
        if (CollectionUtils.isNullOrEmpty((Object[]) registrationManager.getProfileList(i))) {
            IMSLog.e(str, i, "no profile found");
            return false;
        } else if (iSimManager.getSimMno() == Mno.DEFAULT) {
            IMSLog.e(str, i, "no SIM loaded");
            return false;
        } else if (getGlobalGcEnabled(context, i) || isSimMobilityRCS(context, i, iSimManager, registrationManager)) {
            for (ImsProfile needAutoconfig : registrationManager.getProfileList(i)) {
                if (needAutoconfig.getNeedAutoconfig()) {
                    return true;
                }
            }
            return false;
        } else {
            IMSLog.e(str, i, "This is a other country SIM, RCS disabled in SIM mobility");
            return false;
        }
    }

    /* JADX WARNING: Code restructure failed: missing block: B:16:0x0044, code lost:
        r4 = com.sec.internal.constants.ims.ImsConstants.SystemSettings.getRcsUserSetting(r4, -1, r5);
     */
    /* Code decompiled incorrectly, please refer to instructions dump. */
    public static boolean isRcsAvailable(android.content.Context r4, int r5, com.sec.internal.interfaces.ims.core.ISimManager r6) {
        /*
            java.lang.String r0 = LOG_TAG
            java.lang.String r1 = "isRcsAvailable:"
            com.sec.internal.log.IMSLog.d(r0, r5, r1)
            r1 = 0
            if (r6 == 0) goto L_0x005f
            boolean r2 = r6.hasNoSim()
            if (r2 == 0) goto L_0x0011
            goto L_0x005f
        L_0x0011:
            boolean r2 = com.sec.internal.ims.rcs.util.RcsUtils.DualRcs.isRegAllowed(r4, r5)
            if (r2 != 0) goto L_0x001d
            java.lang.String r4 = "DDS set to other SIM"
            com.sec.internal.log.IMSLog.d(r0, r5, r4)
            return r1
        L_0x001d:
            com.sec.internal.constants.Mno r0 = r6.getSimMno()
            com.sec.internal.constants.Mno r2 = com.sec.internal.constants.Mno.ATT
            r3 = 1
            if (r0 == r2) goto L_0x0038
            com.sec.internal.constants.Mno r0 = r6.getSimMno()
            com.sec.internal.constants.Mno r2 = com.sec.internal.constants.Mno.VZW
            if (r0 == r2) goto L_0x0038
            com.sec.internal.constants.Mno r6 = r6.getSimMno()
            boolean r6 = isRcsChn(r6)
            if (r6 == 0) goto L_0x0044
        L_0x0038:
            java.lang.String r6 = "jibe"
            java.lang.String r0 = getAcsServerType(r5)
            boolean r6 = r6.equals(r0)
            if (r6 == 0) goto L_0x0051
        L_0x0044:
            r6 = -1
            int r4 = com.sec.internal.constants.ims.ImsConstants.SystemSettings.getRcsUserSetting(r4, r6, r5)
            if (r4 == r3) goto L_0x0051
            r6 = 2
            if (r4 != r6) goto L_0x004f
            goto L_0x0051
        L_0x004f:
            r4 = r1
            goto L_0x0052
        L_0x0051:
            r4 = r3
        L_0x0052:
            com.sec.internal.interfaces.ims.config.IConfigModule r6 = com.sec.internal.ims.registry.ImsRegistry.getConfigModule()
            boolean r5 = r6.isRcsEnabled(r5)
            if (r5 == 0) goto L_0x005f
            if (r4 == 0) goto L_0x005f
            r1 = r3
        L_0x005f:
            return r1
        */
        throw new UnsupportedOperationException("Method not decompiled: com.sec.internal.ims.util.ConfigUtil.isRcsAvailable(android.content.Context, int, com.sec.internal.interfaces.ims.core.ISimManager):boolean");
    }

    public static boolean hasChatbotService(int i, IRegistrationManager iRegistrationManager) {
        boolean z = false;
        for (ImsProfile hasService : iRegistrationManager.getProfileList(i)) {
            z = hasService.hasService(ServiceConstants.SERVICE_CHATBOT_COMMUNICATION);
            if (z) {
                break;
            }
        }
        return z;
    }

    public static boolean isRcsOnly(ImsProfile imsProfile) {
        return !imsProfile.hasService("mmtel") && !imsProfile.hasService("mmtel-video") && !imsProfile.hasService("smsip");
    }

    public static boolean isRcsEur(int i) {
        return isRcsEur(SimUtil.getSimMno(i));
    }

    public static boolean isRcsEur(Mno mno) {
        return mno.isEur() || mno.isSea() || mno.isMea() || mno.isSwa();
    }

    public static boolean isRcsCanada(Mno mno) {
        return mno.isCanada();
    }

    public static boolean isRcsChn(Mno mno) {
        return mno == Mno.CTC || mno == Mno.CU || mno == Mno.CMCC;
    }

    public static boolean isRcsEurNonRjil(Mno mno) {
        return isRcsEur(mno) && !mno.isRjil();
    }

    public static boolean isSimMobilityRCS(Context context, int i, ISimManager iSimManager, IRegistrationManager iRegistrationManager) {
        boolean isSimMobilityActivatedForAmRcs = ImsUtil.isSimMobilityActivatedForAmRcs(context, i);
        if (ImsUtil.isSimMobilityActivatedForRcs(i) || isSimMobilityActivatedForAmRcs) {
            boolean z = false;
            if (CollectionUtils.isNullOrEmpty((Object[]) iRegistrationManager.getProfileList(i))) {
                IMSLog.d(LOG_TAG, i, "isSimMobilityRCS: no profile found");
            } else {
                ImsProfile[] profileList = iRegistrationManager.getProfileList(i);
                int length = profileList.length;
                int i2 = 0;
                while (true) {
                    if (i2 >= length) {
                        break;
                    } else if (profileList[i2].getEnableRcs() || isSimMobilityActivatedForAmRcs) {
                        IMSLog.d(LOG_TAG, i, "isSimMobilityRCS: RCS is enabled in SimMobility");
                        z = true;
                    } else {
                        i2++;
                    }
                }
            }
            if (!OmcCode.isKorOpenOmcCode() || !iSimManager.getSimMno().isKor()) {
                return z;
            }
            return true;
        }
        IMSLog.d(LOG_TAG, i, "isSimMobilityRCS: no need to check about SimMobility");
        return true;
    }

    public static boolean isGcForEur(int i) {
        return isRcsEur(i) && ImsConstants.RCS_AS.JIBE.equals(getAcsServerType(i)) && isRcsPreConsent(i);
    }

    public static boolean isRcsPreConsent(int i) {
        return ImsRegistry.getInt(i, GlobalSettingsConstants.RCS.PRE_CONSENT, 0) == 1;
    }

    public static boolean checkMdmRcsStatus(Context context, int i) {
        Cursor query;
        String string;
        String valueOf = String.valueOf(i);
        Uri parse = Uri.parse("content://com.sec.knox.provider2/PhoneRestrictionPolicy");
        String[] strArr = {"1", ConfigConstants.VALUE.INFO_COMPLETED, valueOf};
        boolean z = true;
        if (context == null) {
            return true;
        }
        try {
            query = context.getContentResolver().query(parse, (String[]) null, "isRCSEnabled", strArr, (String) null);
            if (query != null) {
                if (query.moveToFirst() && (string = query.getString(query.getColumnIndex("isRCSEnabled"))) != null && string.equals(ConfigConstants.VALUE.INFO_COMPLETED)) {
                    IMSLog.d(LOG_TAG, i, "checkMdmRcsStatus: Disabled RCS");
                    z = false;
                }
            }
            if (query != null) {
                query.close();
            }
        } catch (IllegalArgumentException unused) {
            IMSLog.e(LOG_TAG, i, "checkMdmRcsStatus: isAllowed = true due to IllegalArgumentException");
        } catch (Throwable th) {
            th.addSuppressed(th);
        }
        return z;
        throw th;
    }

    public static boolean shallUsePreviousCookie(int i, Mno mno) {
        return mno == Mno.SWISSCOM && i >= 500 && i != 511;
    }

    public static boolean doesUpRcsProfileMatchProvisioningVersion(String str, String str2) {
        if (str == null) {
            return false;
        }
        if (str.startsWith("UP_1.0") || str.startsWith("UP_T")) {
            return "2.0".equals(str2);
        }
        if (str.startsWith("UP_2.0") || str.startsWith("UP_2.2")) {
            return ConfigConstants.PVALUE.PROVISIONING_VERSION_4_0.equals(str2);
        }
        if (str.startsWith("UP_2.3") || str.startsWith("UP_2.4")) {
            return ConfigConstants.PVALUE.PROVISIONING_VERSION_5_0.equals(str2);
        }
        return false;
    }

    public static int getConfigId(Context context, String str) {
        try {
            return context.getResources().getIdentifier(str, "raw", context.getPackageName());
        } catch (Exception e) {
            e.printStackTrace();
            return -1;
        }
    }

    public static String getResourcesFromFile(Context context, int i, String str, String str2) {
        InputStream inputStream;
        String str3 = LOG_TAG;
        IMSLog.d(str3, i, "getResourcesFromFile: fileName: " + str);
        String str4 = null;
        try {
            if (SDCARD_CONFIG_FILE.equals(str)) {
                inputStream = new FileInputStream(Environment.getExternalStorageDirectory().getPath() + SDCARD_CONFIG_FILE);
            } else {
                inputStream = context.getResources().openRawResource(getConfigId(context, LOCAL_CONFIG_FILE));
            }
            byte[] bArr = new byte[inputStream.available()];
            if (inputStream.read(bArr) < 0) {
                IMSLog.e(str3, i, "fail to read buffer");
            }
            String str5 = new String(bArr, str2);
            try {
                inputStream.close();
                return str5;
            } catch (IOException | NullPointerException e) {
                e = e;
                str4 = str5;
                e.printStackTrace();
                return str4;
            }
        } catch (IOException | NullPointerException e2) {
            e = e2;
            e.printStackTrace();
            return str4;
        } catch (Throwable th) {
            th.addSuppressed(th);
        }
        throw th;
    }

    public static String encryptParam(String str) {
        try {
            SecretKeySpec secretKeySpec = new SecretKeySpec(mAesKeyBytes, "AES");
            IvParameterSpec ivParameterSpec = new IvParameterSpec(mAesIvBytes);
            Cipher instance = Cipher.getInstance(TRANSFORMATION);
            instance.init(1, secretKeySpec, ivParameterSpec);
            return new String(Base64.encode(instance.doFinal(str.getBytes("UTF-8")), 0), "UTF-8");
        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }
    }

    public static void encryptParams(Map<String, String> map, String... strArr) {
        for (String str : strArr) {
            Locale locale = Locale.US;
            String str2 = map.get(str.toLowerCase(locale));
            if (str2 != null) {
                map.put(str.toLowerCase(locale), new String(Base64.encode(str2.getBytes(), 0)));
            }
        }
    }

    public static String decryptParam(String str, String str2) {
        String str3;
        if (str != null && !"".equals(str)) {
            try {
                byte[] decode = Base64.decode(str.getBytes("UTF-8"), 0);
                if (decode != null) {
                    SecretKeySpec secretKeySpec = new SecretKeySpec(mAesKeyBytes, "AES");
                    IvParameterSpec ivParameterSpec = new IvParameterSpec(mAesIvBytes);
                    Cipher instance = Cipher.getInstance(TRANSFORMATION);
                    instance.init(2, secretKeySpec, ivParameterSpec);
                    str3 = new String(instance.doFinal(decode), "UTF-8");
                } else {
                    str3 = null;
                }
                return str3 != null ? str3 : str2;
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
        return str2;
    }

    public static String getFormattedUserAgent(Mno mno, String str, String str2, String str3) {
        String str4;
        if (!mno.isKor()) {
            return String.format(ConfigConstants.TEMPLATE.USER_AGENT, new Object[]{str, str2, str3});
        }
        if (OmcCode.isSKTOmcCode()) {
            str4 = "SKT";
        } else if (OmcCode.isKTTOmcCode()) {
            str4 = "KT";
        } else if (OmcCode.isLGTOmcCode()) {
            str4 = "LGU";
        } else {
            str4 = OmcCode.isKorOpenOmcCode() ? "OMD" : "";
        }
        return String.format(ConfigConstants.TEMPLATE.USER_AGENT_KOR, new Object[]{str, str2, str4});
    }

    public static String buildIdentity(Context context, int i) {
        String str;
        int subId = SimUtil.getSubId(i);
        ITelephonyManager instance = TelephonyManagerWrapper.getInstance(context);
        String subscriberId = instance.getSubscriberId(subId);
        String msisdn = instance.getMsisdn(subId);
        String imei = instance.getImei(i);
        if (!TextUtils.isEmpty(subscriberId)) {
            str = "IMSI_" + subscriberId;
        } else if (!TextUtils.isEmpty(msisdn)) {
            str = "MSISDN_" + msisdn;
        } else if (!TextUtils.isEmpty(imei)) {
            str = "IMEI_" + imei;
        } else {
            IMSLog.e(LOG_TAG, i, "identity error");
            str = "";
        }
        String replaceAll = str.replaceAll("[\\W]", "");
        IMSLog.d(LOG_TAG, i, "buildIdentity: " + subId + ", + identity : " + IMSLog.checker(replaceAll));
        return replaceAll;
    }

    public static boolean getGlobalGcEnabled(Context context, int i) {
        return ImsSharedPrefHelper.getBoolean(i, context, ImsSharedPrefHelper.GLOBAL_SETTINGS, "globalgcenabled", false);
    }

    public static boolean isIidTokenNeeded(Context context, int i, String str) {
        long gmsVersion = getGmsVersion(context, i);
        String string = ImsRegistry.getString(i, GlobalSettingsConstants.RCS.RCS_CLIENT_VERSION, "6.0");
        float parseFloat = !TextUtils.isEmpty(string) ? Float.parseFloat(string) : 0.0f;
        String str2 = LOG_TAG;
        IMSLog.d(str2, i, "GOOGLE_PLAY_SERVICES_PACKAGE Version : " + gmsVersion + ", clientVersion : " + parseFloat);
        return gmsVersion >= ConfigConstants.IID_TOKEN_MIN_GMS_CORE_VERSION && parseFloat >= 8.5f && isRcsPreConsent(i) && (isSecDmaPackageInuse(context, i) || String.valueOf(WorkflowBase.OpMode.DISABLE_RCS_BY_USER).equals(str));
    }

    public static long getGmsVersion(Context context, int i) {
        long j = 0;
        try {
            PackageInfo packageInfo = context.getPackageManager().getPackageInfo("com.google.android.gms", 0);
            if (packageInfo != null) {
                j = packageInfo.getLongVersionCode();
            }
        } catch (PackageManager.NameNotFoundException unused) {
            IMSLog.d(LOG_TAG, i, "calling package NameNotFoundException");
        }
        String str = LOG_TAG;
        IMSLog.d(str, i, "GmsVersion : " + j);
        return j;
    }

    /* JADX WARNING: Can't fix incorrect switch cases order */
    /* Code decompiled incorrectly, please refer to instructions dump. */
    public static java.lang.String decryptConfigParams(java.lang.String r5, java.lang.String r6, com.sec.internal.constants.Mno r7, boolean r8) {
        /*
            if (r8 == 0) goto L_0x009c
            com.sec.internal.constants.Mno r8 = com.sec.internal.constants.Mno.ATT
            if (r7 != r8) goto L_0x009c
            int r7 = r5.hashCode()     // Catch:{ IllegalArgumentException -> 0x006a }
            r8 = 0
            r0 = 5
            r1 = 4
            r2 = 3
            r3 = 2
            r4 = 1
            switch(r7) {
                case -1051385883: goto L_0x0047;
                case -612456943: goto L_0x003d;
                case 110541305: goto L_0x0032;
                case 257333303: goto L_0x0028;
                case 257337958: goto L_0x001e;
                case 2070885700: goto L_0x0014;
                default: goto L_0x0013;
            }     // Catch:{ IllegalArgumentException -> 0x006a }
        L_0x0013:
            goto L_0x0051
        L_0x0014:
            java.lang.String r7 = "nms_url"
            boolean r7 = r5.equals(r7)     // Catch:{ IllegalArgumentException -> 0x006a }
            if (r7 == 0) goto L_0x0051
            r7 = r2
            goto L_0x0052
        L_0x001e:
            java.lang.String r7 = "fthttpcsuri"
            boolean r7 = r5.equals(r7)     // Catch:{ IllegalArgumentException -> 0x006a }
            if (r7 == 0) goto L_0x0051
            r7 = r3
            goto L_0x0052
        L_0x0028:
            java.lang.String r7 = "fthttpcspwd"
            boolean r7 = r5.equals(r7)     // Catch:{ IllegalArgumentException -> 0x006a }
            if (r7 == 0) goto L_0x0051
            r7 = r4
            goto L_0x0052
        L_0x0032:
            java.lang.String r7 = "token"
            boolean r7 = r5.equals(r7)     // Catch:{ IllegalArgumentException -> 0x006a }
            if (r7 == 0) goto L_0x0051
            r7 = r0
            goto L_0x0052
        L_0x003d:
            java.lang.String r7 = "fthttpcsuser"
            boolean r7 = r5.equals(r7)     // Catch:{ IllegalArgumentException -> 0x006a }
            if (r7 == 0) goto L_0x0051
            r7 = r8
            goto L_0x0052
        L_0x0047:
            java.lang.String r7 = "nc_url"
            boolean r7 = r5.equals(r7)     // Catch:{ IllegalArgumentException -> 0x006a }
            if (r7 == 0) goto L_0x0051
            r7 = r1
            goto L_0x0052
        L_0x0051:
            r7 = -1
        L_0x0052:
            if (r7 == 0) goto L_0x005f
            if (r7 == r4) goto L_0x005f
            if (r7 == r3) goto L_0x005f
            if (r7 == r2) goto L_0x005f
            if (r7 == r1) goto L_0x005f
            if (r7 == r0) goto L_0x005f
            goto L_0x009c
        L_0x005f:
            java.lang.String r7 = new java.lang.String     // Catch:{ IllegalArgumentException -> 0x006a }
            byte[] r8 = android.util.Base64.decode(r6, r8)     // Catch:{ IllegalArgumentException -> 0x006a }
            r7.<init>(r8)     // Catch:{ IllegalArgumentException -> 0x006a }
            r6 = r7
            goto L_0x009c
        L_0x006a:
            java.lang.String r7 = LOG_TAG
            java.lang.StringBuilder r8 = new java.lang.StringBuilder
            r8.<init>()
            java.lang.String r0 = "Failed to decrypt this param "
            r8.append(r0)
            r8.append(r5)
            java.lang.String r8 = r8.toString()
            com.sec.internal.log.IMSLog.e(r7, r8)
            java.lang.StringBuilder r8 = new java.lang.StringBuilder
            r8.<init>()
            java.lang.String r0 = "Wrong param "
            r8.append(r0)
            r8.append(r5)
            java.lang.String r5 = ", data "
            r8.append(r5)
            r8.append(r6)
            java.lang.String r5 = r8.toString()
            com.sec.internal.log.IMSLog.s(r7, r5)
        L_0x009c:
            return r6
        */
        throw new UnsupportedOperationException("Method not decompiled: com.sec.internal.ims.util.ConfigUtil.decryptConfigParams(java.lang.String, java.lang.String, com.sec.internal.constants.Mno, boolean):java.lang.String");
    }

    public static boolean isSecDmaPackageInuse(Context context, int i) {
        String dmaPackage = getDmaPackage(context, i);
        if (dmaPackage == null) {
            Log.i(LOG_TAG, "default sms app is null");
            return false;
        }
        String msgAppPkgName = PackageUtils.getMsgAppPkgName(context);
        boolean equals = TextUtils.equals(dmaPackage, msgAppPkgName);
        String str = LOG_TAG;
        Log.i(str, "default sms app:" + dmaPackage + " samsungPackage:" + msgAppPkgName);
        StringBuilder sb = new StringBuilder();
        sb.append("isSecDmaPackageInuse : ");
        sb.append(equals);
        Log.i(str, sb.toString());
        return equals;
    }

    public static boolean isGoogDmaPackageInuse(Context context, int i) {
        String dmaPackage = getDmaPackage(context, i);
        if (TextUtils.isEmpty(dmaPackage)) {
            Log.i(LOG_TAG, "default sms app is null");
            return false;
        }
        String string = ImsRegistry.getString(i, GlobalSettingsConstants.RCS.GOOG_MESSAGE_APP_PACKAGE, "");
        boolean equals = TextUtils.equals(dmaPackage, string);
        String str = LOG_TAG;
        Log.i(str, "default sms app:" + dmaPackage + " googlePackage:" + string);
        StringBuilder sb = new StringBuilder();
        sb.append("isGoogDmaPackageInuse : ");
        sb.append(equals);
        Log.i(str, sb.toString());
        return equals;
    }

    public static String getDmaPackage(Context context, int i) {
        String str;
        try {
            str = Telephony.Sms.getDefaultSmsPackage(context);
            try {
                String str2 = LOG_TAG;
                IMSLog.i(str2, i, "getDmaPackage: defaultSmsApp from Telephony: " + str);
            } catch (Exception e) {
                e = e;
            }
        } catch (Exception e2) {
            e = e2;
            str = null;
            String str3 = LOG_TAG;
            IMSLog.i(str3, i, "getDmaPackage: fail to get from Telephony: " + e.getMessage());
            return str;
        }
        return str;
    }

    public static boolean isValidMsisdn(String str) {
        if (str == null || str.length() < 8) {
            Log.i(LOG_TAG, "invalid msisdn is used");
            return false;
        }
        try {
            if (!"00000000".equals(str.substring(str.length() - 8))) {
                return true;
            }
            Log.i(LOG_TAG, "invalid msisdn is used");
            return false;
        } catch (StringIndexOutOfBoundsException e) {
            String str2 = LOG_TAG;
            Log.e(str2, "isValidMsisdn: Exception: " + e.toString());
            return false;
        }
    }
}
