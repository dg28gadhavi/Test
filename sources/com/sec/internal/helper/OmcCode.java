package com.sec.internal.helper;

import android.os.SemSystemProperties;
import android.text.TextUtils;
import android.util.Log;
import com.samsung.android.feature.SemFloatingFeature;
import com.sec.internal.constants.Mno;
import com.sec.internal.constants.ims.cmstore.CloudMessageProviderContract;
import com.sec.internal.constants.ims.config.ConfigConstants;
import com.sec.internal.constants.ims.os.SecFeature;
import com.sec.internal.helper.os.DeviceUtil;
import com.sec.internal.log.IMSLog;

public class OmcCode {
    private static final String CSC_PATH = "/system/csc";
    private static final String LOG_TAG = "OmcCode";
    public static final String OMC_CODE_PROPERTY = "ro.csc.sales_code";
    private static final String PERSIST_LAST_OMCNW_CODE = "persist.sys.ims.last_omcnw_code";
    private static final String PERSIST_LAST_OMCNW_CODE2 = "persist.sys.ims.last_omcnw_code2";
    private static final String PERSIST_OMCNW_PATH = "persist.sys.omcnw_path";
    private static final String PERSIST_OMCNW_PATH2 = "persist.sys.omcnw_path2";
    private static final String PERSIST_OMCSUPPORT = "persist.sys.omc_support";
    public static final String PERSIST_OMC_CODE_PROPERTY = "persist.omc.sales_code";
    private static final String PERSIST_OMC_ETC_PATH = "persist.sys.omc_etcpath";
    private static final String PERSIST_OMC_PATH = "persist.sys.omc_path";

    public static String get() {
        String str = SemSystemProperties.get(PERSIST_OMC_CODE_PROPERTY, "");
        return TextUtils.isEmpty(str) ? SemSystemProperties.get(OMC_CODE_PROPERTY, "") : str;
    }

    public static String getUserAgentNWCode(int i, Mno mno) {
        return DeviceUtil.isTSS2_0() ? getTSS2_0_NWCode(mno) : getNWCode(i);
    }

    private static String getTSS2_0_NWCode(Mno mno) {
        String representativeCountryISO = DeviceUtil.representativeCountryISO(Mno.getCountryFromMnomap(mno.getName()).getCountryIso());
        String str = SemSystemProperties.get("ro.boot.activatedid", "");
        String countryIso = SemSystemProperties.getCountryIso();
        Log.d(LOG_TAG, "activatedId : " + str);
        if (!TextUtils.equals(countryIso, representativeCountryISO.toUpperCase()) || TextUtils.isEmpty(str) || "EUX".equalsIgnoreCase(str) || "EUY".equalsIgnoreCase(str)) {
            return getOpenBuyerByCountryIso(representativeCountryISO);
        }
        return str;
    }

    private static String getOmcCodeByNwPath(int i, boolean z) {
        String nWPath = getNWPath(SimUtil.isMultiSimSupported() ? i : 0);
        if (nWPath.contains("/")) {
            String[] split = nWPath.split("/");
            int length = split.length;
            int i2 = 0;
            while (i2 < length) {
                String str = split[i2];
                if (str.length() != 3 || !isUpperCaseOrDigit(Character.valueOf(str.charAt(0))) || !isUpperCaseOrDigit(Character.valueOf(str.charAt(1))) || !isUpperCaseOrDigit(Character.valueOf(str.charAt(2)))) {
                    i2++;
                } else {
                    IMSLog.i(LOG_TAG, i, "getOmcCodeByNwPath : " + str);
                    return str;
                }
            }
        }
        return z ? get() : "";
    }

    public static String getLastOmcNwCode(int i) {
        return SemSystemProperties.get(i == 0 ? PERSIST_LAST_OMCNW_CODE : PERSIST_LAST_OMCNW_CODE2);
    }

    public static void saveLastOmcNwCode(int i, String str) {
        SemSystemProperties.set(i == 0 ? PERSIST_LAST_OMCNW_CODE : PERSIST_LAST_OMCNW_CODE2, str);
    }

    public static String getOmcNwPath(int i) {
        return getUpdatedPathUsingLastNwCode(i, getNWPath(i));
    }

    private static String getUpdatedPathUsingLastNwCode(int i, String str) {
        String str2;
        String lastOmcNwCode = getLastOmcNwCode(i);
        String str3 = get();
        if (lastOmcNwCode.isEmpty() || lastOmcNwCode.equals(str3)) {
            str2 = str;
        } else {
            str2 = str.replace("/" + str3, "/" + lastOmcNwCode);
        }
        IMSLog.i(LOG_TAG, i, "getUpdatedPathUsingLastNwCode omcCode:" + str3 + ", last:" + lastOmcNwCode + ", path: " + str + "=>" + str2);
        return str2;
    }

    public static String getNWCode(int i) {
        String lastOmcNwCode = getLastOmcNwCode(i);
        return !lastOmcNwCode.isEmpty() ? lastOmcNwCode : getOmcCodeByNwPath(i, true);
    }

    public static String getCurrentNWCode(int i) {
        return getOmcCodeByNwPath(i, true);
    }

    public static boolean isTmpSimSwap(int i) {
        return !get().equals(getNWCode(i));
    }

    public static String getPath() {
        return SemSystemProperties.get(PERSIST_OMC_PATH, CSC_PATH);
    }

    public static String getNWPath(int i) {
        return SemSystemProperties.get(i == 0 ? PERSIST_OMCNW_PATH : PERSIST_OMCNW_PATH2, getPath());
    }

    public static String getEtcPath() {
        return SemSystemProperties.get(PERSIST_OMC_ETC_PATH, getNWPath(0));
    }

    public static boolean isOmcModel() {
        return CloudMessageProviderContract.JsonData.TRUE.equals(SemSystemProperties.get(PERSIST_OMCSUPPORT, ConfigConstants.VALUE.INFO_COMPLETED));
    }

    public static boolean isSKTOmcCode() {
        return "SKT".equals(get()) || "SKC".equals(get());
    }

    public static boolean isKTTOmcCode() {
        return "KTT".equals(get()) || "KTC".equals(get());
    }

    public static boolean isLGTOmcCode() {
        return "LGT".equals(get()) || "LUC".equals(get());
    }

    public static boolean isKorOpenOmcCode() {
        return "KOO".equals(get()) || get().contains("K0");
    }

    public static boolean isKorOpenOnlyOmcCode() {
        return "KOO".equals(get());
    }

    public static boolean isKOROmcCode() {
        return isKorOpenOmcCode() || isSKTOmcCode() || isKTTOmcCode() || isLGTOmcCode();
    }

    public static boolean isChinaOmcCode() {
        String str = get();
        return "CHC".equals(str) || "CHM".equals(str) || "TGY".equals(str) || "BRI".equals(str);
    }

    public static boolean isMainlandChinaOmcCode() {
        String str = get();
        return "CHC".equals(str) || "CHM".equals(str);
    }

    public static boolean isJPNOmcCode() {
        return "DCM".equals(get()) || "KDI".equals(get()) || "KDR".equals(get()) || "UQM".equals(get()) || "JCO".equals(get()) || "SJP".equals(get());
    }

    public static boolean isKDIMhs() {
        return Boolean.parseBoolean(SemFloatingFeature.getInstance().getString(SecFeature.FLOATING.MHS_DONGLE)) && ("KDI".equals(get()) || "KDR".equals(get()) || "UQM".equals(get()));
    }

    public static boolean isDCMOmcCode() {
        return "DCM".equals(get());
    }

    public static boolean isRKTOmcCode() {
        return "RKT".equals(get());
    }

    public static boolean isJPNOpenOmcCode() {
        return "SM-F700J".equals(SemSystemProperties.get("ro.product.model", ""));
    }

    public enum EUR_TSS2_0_COUNTRYCODE {
        AT("ATO"),
        BG("BGL"),
        CZ("XEZ"),
        DE("DBT"),
        ES("PHE"),
        FR("XEF"),
        GB("BTU"),
        GR("EUR"),
        HU("XEH"),
        IT("ITV"),
        LU("LUX"),
        LV("SEB"),
        NL("PHN"),
        PL("XEO"),
        PT("TPH"),
        RO("ROM"),
        SE("NEE"),
        SI("SIO"),
        SK("ORX"),
        RS("SEE"),
        CH("AUT");
        
        private final String openBuyer;

        private EUR_TSS2_0_COUNTRYCODE(String str) {
            this.openBuyer = str;
        }

        public String getOpenBuyer() {
            return this.openBuyer;
        }
    }

    public static String getOpenBuyerByCountryIso(String str) {
        String str2 = SemSystemProperties.get(OMC_CODE_PROPERTY, "");
        if (TextUtils.isEmpty(str)) {
            return str2;
        }
        for (EUR_TSS2_0_COUNTRYCODE eur_tss2_0_countrycode : EUR_TSS2_0_COUNTRYCODE.values()) {
            if (TextUtils.equals(eur_tss2_0_countrycode.name(), str.toUpperCase())) {
                return eur_tss2_0_countrycode.getOpenBuyer();
            }
        }
        return str2;
    }

    private static boolean isUpperCaseOrDigit(Character ch) {
        return Character.isDigit(ch.charValue()) || Character.isUpperCase(ch.charValue());
    }
}
