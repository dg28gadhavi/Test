package com.sec.internal.ims.core.sim;

import android.content.ContentValues;
import android.content.Context;
import android.provider.Settings;
import android.telephony.SubscriptionInfo;
import android.text.TextUtils;
import android.util.Log;
import com.sec.ims.settings.ImsProfile;
import com.sec.internal.constants.Mno;
import com.sec.internal.constants.ims.ImsConstants;
import com.sec.internal.constants.ims.os.IccCardConstants;
import com.sec.internal.helper.CollectionUtils;
import com.sec.internal.helper.os.ITelephonyManager;
import com.sec.internal.ims.settings.ImsServiceSwitch;
import com.sec.internal.interfaces.ims.core.ISimManager;
import com.sec.internal.log.IMSLog;
import java.util.Collections;
import java.util.Iterator;
import java.util.List;
import java.util.Optional;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

class SimManagerUtils {
    private static final String LOG_TAG = "SimManager-Utils";

    SimManagerUtils() {
    }

    static boolean needImsUpOnUnknownState(Context context, int i) {
        return Settings.System.getInt(context.getContentResolver(), new String[]{"phone1_on", "phone2_on"}[i], 1) != 1;
    }

    static String extractMnoFromImpi(String str) {
        if (str == null) {
            Log.e(LOG_TAG, "IMPI is null");
            return null;
        }
        Matcher matcher = Pattern.compile("\\d+@ims\\.mnc\\d+\\.mcc\\d+\\.3gppnetwork\\.org").matcher(str);
        if (TextUtils.isEmpty(str)) {
            Log.e(LOG_TAG, "IMPI is empty");
            return "";
        } else if (matcher.matches()) {
            int indexOf = str.indexOf("mcc") + 3;
            String substring = str.substring(indexOf, str.indexOf(".", indexOf));
            int indexOf2 = str.indexOf("mnc") + 3;
            int indexOf3 = str.indexOf(".", indexOf2);
            return substring + str.substring(indexOf2, indexOf3);
        } else {
            int indexOf4 = str.indexOf("@");
            if (indexOf4 == 14) {
                return str.substring(0, 5);
            }
            if (indexOf4 == 15) {
                return str.substring(0, 6);
            }
            Log.e(LOG_TAG, "Not a IMSI format");
            return "";
        }
    }

    static String extractImsiFromImpi(String str, String str2) {
        if (str == null) {
            Log.e(LOG_TAG, "IMPI is null");
            return str2;
        } else if (TextUtils.isEmpty(str)) {
            Log.e(LOG_TAG, "IMPI is empty");
            return str2;
        } else {
            int indexOf = str.indexOf("@");
            if (indexOf == 14 || indexOf == 15) {
                return str.substring(0, indexOf);
            }
            Log.e(LOG_TAG, "@ not found, IMPI is invalid");
            return str2;
        }
    }

    static int[] parseMccMnc(int i, String str) {
        if (isValidSimOperator(i, str)) {
            return new int[]{Integer.parseInt(str.substring(0, 3)), Integer.parseInt(str.substring(3))};
        }
        IMSLog.e(LOG_TAG, i, "parseMccMnc: mccMnc is invalid");
        return null;
    }

    static boolean isValidSimOperator(int i, String str) {
        if (TextUtils.isEmpty(str) || str.length() < 5) {
            IMSLog.e(LOG_TAG, i, "isValidSimOperator: operator is invalid" + str);
            return false;
        }
        try {
            Integer.parseInt(str);
            return true;
        } catch (NumberFormatException unused) {
            IMSLog.e(LOG_TAG, i, "isValidSimOperator: NumberFormatException " + str);
            return false;
        }
    }

    static boolean isISimAppPresent(int i, ITelephonyManager iTelephonyManager) {
        if (iTelephonyManager.getPhoneCount() == 1) {
            return !TextUtils.isEmpty(iTelephonyManager.getAidForAppType(5));
        }
        return "1".equals(iTelephonyManager.getTelephonyProperty(i, "ril.hasisim", "0"));
    }

    static String readSimStateProperty(int i, ITelephonyManager iTelephonyManager) {
        if (iTelephonyManager == null) {
            return "UNKNOWN";
        }
        String telephonyProperty = iTelephonyManager.getTelephonyProperty(i, ImsConstants.SystemProperties.SIM_STATE, "UNKNOWN");
        telephonyProperty.hashCode();
        char c = 65535;
        switch (telephonyProperty.hashCode()) {
            case -2044189691:
                if (telephonyProperty.equals(IccCardConstants.INTENT_VALUE_ICC_LOADED)) {
                    c = 0;
                    break;
                }
                break;
            case 0:
                if (telephonyProperty.equals("")) {
                    c = 1;
                    break;
                }
                break;
            case 77848963:
                if (telephonyProperty.equals(IccCardConstants.INTENT_VALUE_ICC_READY)) {
                    c = 2;
                    break;
                }
                break;
            case 433141802:
                if (telephonyProperty.equals("UNKNOWN")) {
                    c = 3;
                    break;
                }
                break;
            case 1034051831:
                if (telephonyProperty.equals(IccCardConstants.INTENT_VALUE_ICC_NOT_READY)) {
                    c = 4;
                    break;
                }
                break;
            case 1924388665:
                if (telephonyProperty.equals(IccCardConstants.INTENT_VALUE_ICC_ABSENT)) {
                    c = 5;
                    break;
                }
                break;
        }
        switch (c) {
            case 0:
            case 2:
            case 4:
            case 5:
                return telephonyProperty;
            case 1:
            case 3:
                return "UNKNOWN";
            default:
                return IccCardConstants.INTENT_VALUE_ICC_LOCKED;
        }
    }

    static String getEhplmn(SubscriptionInfo subscriptionInfo) {
        try {
            return (String) ((List) Optional.ofNullable(subscriptionInfo.getEhplmns()).orElse(Collections.emptyList())).stream().filter(new SimManagerUtils$$ExternalSyntheticLambda0()).findFirst().orElse("");
        } catch (NoSuchMethodError e) {
            Log.e(LOG_TAG, "getEhplmn: " + e);
            return "";
        }
    }

    /* access modifiers changed from: private */
    public static /* synthetic */ boolean lambda$getEhplmn$0(String str) {
        return str.length() >= 5;
    }

    static String getMvnoName(String str) {
        int indexOf = str.indexOf(Mno.MVNO_DELIMITER);
        return indexOf != -1 ? str.substring(indexOf + 1) : "";
    }

    static ContentValues getSimMobilityRcsSettings(int i, List<ImsProfile> list) {
        boolean z;
        boolean z2;
        ContentValues contentValues = new ContentValues();
        Iterator<ImsProfile> it = list.iterator();
        while (true) {
            z = false;
            if (!it.hasNext()) {
                z2 = false;
                break;
            }
            ImsProfile next = it.next();
            if (next.getEnableRcs()) {
                boolean enableRcsChat = next.getEnableRcsChat();
                z2 = true;
                if (enableRcsChat) {
                    z = true;
                } else {
                    z = true;
                    z2 = false;
                }
            }
        }
        if (z) {
            IMSLog.i(LOG_TAG, i, "getSimMobilityRcsSettings: isEnableRcs true");
            Boolean bool = Boolean.TRUE;
            contentValues.put(ImsServiceSwitch.ImsSwitch.DeviceManagement.ENABLE_IMS, bool);
            contentValues.put(ImsServiceSwitch.ImsSwitch.RCS.ENABLE_RCS, bool);
        }
        if (z2) {
            IMSLog.i(LOG_TAG, i, "getSimMobilityRcsSettings: isEnableRcsChat true");
            contentValues.put(ImsServiceSwitch.ImsSwitch.RCS.ENABLE_RCS_CHAT_SERVICE, Boolean.TRUE);
        }
        return contentValues;
    }

    static String convertMnoInfoToString(ContentValues contentValues) {
        String str;
        String str2;
        String str3;
        String str4;
        StringBuilder sb = new StringBuilder();
        sb.append(CollectionUtils.getStringValue(contentValues, ISimManager.KEY_NW_NAME, "?"));
        sb.append("|");
        String str5 = "T";
        sb.append(CollectionUtils.getBooleanValue(contentValues, ImsServiceSwitch.ImsSwitch.DeviceManagement.ENABLE_IMS, false) ? str5 : "F");
        if (CollectionUtils.getBooleanValue(contentValues, ImsServiceSwitch.ImsSwitch.VoLTE.ENABLE_VOLTE, false)) {
            str = str5;
        } else {
            str = "F";
        }
        sb.append(str);
        if (CollectionUtils.getBooleanValue(contentValues, ImsServiceSwitch.ImsSwitch.DeviceManagement.ENABLE_VOWIFI, false)) {
            str2 = str5;
        } else {
            str2 = "F";
        }
        sb.append(str2);
        if (CollectionUtils.getBooleanValue(contentValues, ImsServiceSwitch.ImsSwitch.VoLTE.ENABLE_SMS_IP, false)) {
            str3 = str5;
        } else {
            str3 = "F";
        }
        sb.append(str3);
        if (CollectionUtils.getBooleanValue(contentValues, ImsServiceSwitch.ImsSwitch.RCS.ENABLE_RCS, false)) {
            str4 = str5;
        } else {
            str4 = "F";
        }
        sb.append(str4);
        if (!CollectionUtils.getBooleanValue(contentValues, ImsServiceSwitch.ImsSwitch.RCS.ENABLE_RCS_CHAT_SERVICE, false)) {
            str5 = "F";
        }
        sb.append(str5);
        return sb.toString();
    }
}
