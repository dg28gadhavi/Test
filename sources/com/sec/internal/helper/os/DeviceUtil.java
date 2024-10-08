package com.sec.internal.helper.os;

import android.content.Context;
import android.os.SemSystemProperties;
import android.os.UserManager;
import android.provider.Settings;
import android.telephony.TelephonyManager;
import android.util.Log;
import com.android.internal.util.ArrayUtils;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParseException;
import com.google.gson.JsonParser;
import com.google.gson.stream.JsonReader;
import com.samsung.android.feature.SemCarrierFeature;
import com.sec.internal.constants.Mno;
import com.sec.internal.constants.ims.ImsConstants;
import com.sec.internal.constants.ims.cmstore.CloudMessageProviderContract;
import com.sec.internal.constants.ims.config.ConfigConstants;
import com.sec.internal.constants.ims.os.SecFeature;
import com.sec.internal.helper.OmcCode;
import com.sec.internal.helper.header.WwwAuthenticateHeader;
import com.sec.internal.ims.core.cmc.CmcConstants;
import com.sec.internal.ims.util.ImsUtil;
import com.sec.internal.log.IMSLog;
import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.Arrays;
import java.util.Iterator;
import java.util.Locale;
import java.util.Optional;

public class DeviceUtil {
    private static final String LOG_TAG = "DeviceUtil";
    private static final String OMC_DATA_FILE = "omc_data.json";
    private static final String OMC_PATH_PRISM = "/prism/etc/";
    private static final String[][] REPRESENTATIVE_COUNTRY_ISO = {new String[]{"SE", "NO", "DK", "FI", "IS", "GL"}, new String[]{"LU", "BE"}, new String[]{"LV", "LT", "EE"}, new String[]{"RS", "AL", "MK"}, new String[]{"GB", "IE"}, new String[]{"GR", "CY"}, new String[]{"SI", "HR"}};

    public static boolean isTablet() {
        return SemSystemProperties.get("ro.build.characteristics", "").contains("tablet");
    }

    public static boolean isWifiOnlyModel() {
        return "wifi-only".equalsIgnoreCase(SemSystemProperties.get("ro.carrier", WwwAuthenticateHeader.HEADER_PARAM_UNKNOWN_SCHEME)) || "yes".equalsIgnoreCase(SemSystemProperties.get("ro.radio.noril", "no"));
    }

    public static boolean isUSOpenDevice() {
        return SemSystemProperties.get("ro.simbased.changetype", "").contains("SED");
    }

    public static boolean isUSMvnoDevice() {
        return ArrayUtils.contains(new String[]{"TFN", "TFV", "TFA", "TFO", "XAG", "XAR"}, OmcCode.get());
    }

    public static boolean isOtpAuthorized() {
        try {
            byte[] readAllBytes = Files.readAllBytes(Paths.get(ImsConstants.SystemPath.EFS, new String[]{".otp_auth"}));
            if (readAllBytes == null || !Arrays.equals(readAllBytes, CloudMessageProviderContract.JsonData.TRUE.getBytes(StandardCharsets.UTF_8))) {
                return false;
            }
            return true;
        } catch (IOException unused) {
            return false;
        }
    }

    public static boolean isUserUnlocked(Context context) {
        UserManager userManager;
        if (context != null && (userManager = (UserManager) context.getSystemService(UserManager.class)) != null) {
            return userManager.isUserUnlocked();
        }
        IMSLog.d(LOG_TAG, "temp log : User is lock");
        return false;
    }

    public static String getModemBoardName() {
        return SemSystemProperties.get("ril.modem.board", "").trim();
    }

    public static String getChipName() {
        return SemSystemProperties.get("ro.hardware.chipname", "").trim();
    }

    public static boolean getGcfMode() {
        return "1".equals(SemSystemProperties.get(ImsConstants.SystemProperties.GCF_MODE_PROPERTY, "0"));
    }

    public static void setGcfMode(boolean z) {
        String str = "";
        SemSystemProperties.set(Mno.MOCK_MNO_PROPERTY, z ? Mno.GCF_OPERATOR_CODE : str);
        String str2 = Mno.MOCK_MNONAME_PROPERTY;
        if (z) {
            str = Mno.GCF_OPERATOR_NAME;
        }
        SemSystemProperties.set(str2, str);
        String str3 = "1";
        SemSystemProperties.set(ImsConstants.SystemProperties.GCF_MODE_PROPERTY, z ? str3 : "0");
        if (!z) {
            str3 = "0";
        }
        SemSystemProperties.set(ImsConstants.SystemProperties.GCF_MODE_PROPERTY_P_OS, str3);
    }

    public static int getWifiStatus(Context context, int i) {
        try {
            i = Settings.Global.getInt(context.getContentResolver(), "wifi_on");
        } catch (Settings.SettingNotFoundException unused) {
        }
        String str = LOG_TAG;
        IMSLog.d(str, "getWifiStatus: " + i);
        return i;
    }

    public static boolean isSupport5G(Context context) {
        TelephonyManager telephonyManager = (TelephonyManager) context.getSystemService(TelephonyManager.class);
        if (telephonyManager == null || (telephonyManager.getSupportedRadioAccessFamily() & 524288) <= 0) {
            IMSLog.d(LOG_TAG, "Support5G() : false");
            return false;
        }
        IMSLog.d(LOG_TAG, "Support5G() : true");
        return true;
    }

    public static boolean isUnifiedSalesCodeInTSS() {
        String str = SemSystemProperties.get("mdc.unified", ConfigConstants.VALUE.INFO_COMPLETED);
        String str2 = LOG_TAG;
        IMSLog.d(str2, "UnifiedSalesCodeInTSS() : " + str);
        return CloudMessageProviderContract.JsonData.TRUE.equalsIgnoreCase(str);
    }

    public static boolean isTSS2_0() {
        String str = SemSystemProperties.get(OmcCode.OMC_CODE_PROPERTY, "");
        return isUnifiedSalesCodeInTSS() && ("EUX".equals(str) || "EUY".equals(str));
    }

    public static String representativeCountryISO(String str) {
        String str2;
        String[][] strArr = REPRESENTATIVE_COUNTRY_ISO;
        int length = strArr.length;
        int i = 0;
        while (true) {
            if (i >= length) {
                str2 = str;
                break;
            }
            String[] strArr2 = strArr[i];
            if (ArrayUtils.contains(strArr2, str)) {
                str2 = strArr2[0];
                break;
            }
            i++;
        }
        IMSLog.i(LOG_TAG, "representativeCountryISO " + str + " ==> " + str2);
        return str2;
    }

    public static boolean includedSimByTSS(String str) {
        return includedSimByTSS(str, "/prism/etc//omc_data.json");
    }

    public static boolean includedSimByTSS(String str, String str2) {
        JsonReader jsonReader;
        String representativeCountryISO = representativeCountryISO(Mno.getCountryFromMnomap(str).getCountryIso());
        File file = new File(str2);
        boolean z = false;
        if (!file.exists() || file.length() <= 0) {
            IMSLog.e(LOG_TAG, "omc_data.json not found.");
        } else {
            try {
                jsonReader = new JsonReader(new BufferedReader(new FileReader(file)));
                JsonElement parse = new JsonParser().parse(jsonReader);
                if (!parse.isJsonNull() && parse.isJsonObject() && parse.getAsJsonObject().has("unified_sales_code_list")) {
                    JsonObject asJsonObject = parse.getAsJsonObject().getAsJsonObject("unified_sales_code_list");
                    String str3 = SemSystemProperties.get(OmcCode.OMC_CODE_PROPERTY, "");
                    if (!asJsonObject.isJsonNull() && asJsonObject.isJsonObject() && asJsonObject.getAsJsonObject().has(str3)) {
                        JsonElement jsonElement = asJsonObject.getAsJsonObject().get(str3);
                        if (!jsonElement.isJsonNull() && jsonElement.isJsonArray()) {
                            Iterator it = jsonElement.getAsJsonArray().iterator();
                            while (true) {
                                if (it.hasNext()) {
                                    if (((JsonElement) it.next()).getAsString().equalsIgnoreCase(representativeCountryISO)) {
                                        z = true;
                                        break;
                                    }
                                }
                            }
                        }
                    }
                }
                jsonReader.close();
            } catch (JsonParseException | IOException e) {
                IMSLog.e(LOG_TAG, "omc_data.json parsing failed by " + e);
            } catch (Throwable th) {
                th.addSuppressed(th);
            }
        }
        IMSLog.i(LOG_TAG, "includedSimByTSS " + z + " in Unified Sales Code (TSS2.0)");
        return z;
        throw th;
    }

    public static boolean removeVolteMenuByCsc(int i) {
        String string = SemCarrierFeature.getInstance().getString(i, SecFeature.CARRIER.CONFIGOPSTYLEMOBILENETWORKSETTINGMENU, "", false);
        Locale locale = Locale.US;
        String upperCase = string.toUpperCase(locale);
        String upperCase2 = ImsCscFeature.getInstance().getString(i, SecFeature.CSC.TAG_CSCFEATURE_VOICECALL_CONFIGOPSTYLEMOBILENETWORKSETTINGMENU).toUpperCase(locale);
        if (upperCase.contains("-VOLTECALL") || upperCase2.contains("-VOLTECALL")) {
            return true;
        }
        return false;
    }

    public static boolean removeVolteMenuWithSimMobility(int i) {
        String upperCase = SemCarrierFeature.getInstance().getString(i, SecFeature.CARRIER.CONFIGOPSTYLEMOBILENETWORKSETTINGMENU, "", false).toUpperCase(Locale.US);
        if (ImsUtil.isSimMobilityActivated(i)) {
            return upperCase.contains("-VOLTECALL_SIM_MOBILITY");
        }
        return upperCase.contains("-VOLTECALL");
    }

    public static boolean dimVolteMenuBySaMode(int i) {
        String string = SemCarrierFeature.getInstance().getString(i, SecFeature.CARRIER.CONFIGOPSTYLEFORMOBILENETSETTING, "", false);
        Locale locale = Locale.US;
        String upperCase = string.toUpperCase(locale);
        String upperCase2 = ImsCscFeature.getInstance().getString(i, SecFeature.CSC.TAG_CSCFEATURE_VOICECALL_CONFIGOPSTYLEFORMOBILENETSETTING).toUpperCase(locale);
        if (upperCase.contains("SUPPORT_VOLTE_DIM_BY_SA_MODE") || upperCase2.contains("SUPPORT_VOLTE_DIM_BY_SA_MODE")) {
            return true;
        }
        return false;
    }

    public static boolean isApAssistedMode() {
        return "AP-Assisted".equalsIgnoreCase(SemSystemProperties.get("ro.telephony.iwlan_operation_mode", "AP-Assisted"));
    }

    public static boolean isSupportNrMode(ITelephonyManager iTelephonyManager, int i) {
        int semGetNrMode = iTelephonyManager.semGetNrMode(i);
        boolean z = semGetNrMode == 0 || semGetNrMode == 2 || semGetNrMode == 3;
        String str = LOG_TAG;
        IMSLog.d(str, i, " mTelephonyManager.semGetNrMode : " + semGetNrMode);
        return z;
    }

    public static String getFormattedDeviceId(String str, String str2) {
        String str3;
        if (str == null || str.length() < 14) {
            String str4 = LOG_TAG;
            StringBuilder sb = new StringBuilder();
            sb.append("getFormattedDeviceId: ");
            if (str == null) {
                str3 = "null!";
            } else {
                str3 = "length = " + str.length();
            }
            sb.append(str3);
            Log.d(str4, sb.toString());
            return str;
        }
        return str.substring(0, 8) + CmcConstants.E_NUM_SLOT_SPLIT + str.substring(8, 14) + "-0" + ((String) Optional.ofNullable(str2).filter(new DeviceUtil$$ExternalSyntheticLambda0()).map(new DeviceUtil$$ExternalSyntheticLambda1()).map(new DeviceUtil$$ExternalSyntheticLambda2()).orElse(""));
    }
}
