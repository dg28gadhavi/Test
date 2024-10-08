package com.sec.internal.ims.servicemodules.ss;

import android.os.Bundle;
import android.text.TextUtils;
import com.sec.internal.constants.ims.cmstore.CloudMessageProviderContract;
import com.sec.internal.log.IMSLog;

public class UtLog {
    private static String getCrLogStatus(int i) {
        return i == 0 ? "X" : "O";
    }

    private static String getCrLogStatus(boolean z) {
        return z ? "O" : "X";
    }

    private static String getStringStatus(int i) {
        return i == 0 ? "Deactivated" : "Activated";
    }

    private static String getStringStatus(boolean z) {
        return z ? "Activated" : "Deactivated";
    }

    public static String extractLogFromUtProfile(UtProfile utProfile) {
        if (utProfile == null) {
            return "profile is null";
        }
        StringBuilder sb = new StringBuilder();
        int i = utProfile.type;
        switch (i) {
            case 100:
                sb.append("queryCallForward");
                sb.append(" cfType = ");
                sb.append(getStringCfType(utProfile.condition));
                break;
            case 101:
                sb.append("updateCallForward");
                sb.append(" {cfType = ");
                sb.append(getStringCfType(utProfile.condition));
                sb.append(", action = ");
                sb.append(getStringAction(utProfile.action));
                sb.append(", ssClass = ");
                sb.append(getStringSsClass(utProfile.serviceClass));
                sb.append(", noReplyTimer = ");
                sb.append(utProfile.timeSeconds);
                sb.append(", number = ");
                sb.append(IMSLog.checker(utProfile.number));
                sb.append("}");
                break;
            case 102:
            case 104:
                sb.append("queryCallBarring");
                sb.append(" cbType = ");
                sb.append(getStringCbType(utProfile.condition));
                break;
            case 103:
            case 105:
                sb.append("updateCallBarring");
                sb.append(" {cbType = ");
                sb.append(getStringCbType(utProfile.condition));
                sb.append(", action = ");
                sb.append(getStringAction(utProfile.action));
                sb.append(", ssClass = ");
                sb.append(getStringSsClass(utProfile.serviceClass));
                sb.append(", password = ");
                sb.append(IMSLog.checker(utProfile.password));
                sb.append("}");
                break;
            case 106:
                sb.append("queryCLIP");
                break;
            case 107:
                sb.append("updateCLIP");
                sb.append(" {enable = ");
                sb.append(utProfile.enable);
                sb.append("}");
                break;
            case 108:
                sb.append("queryCLIR");
                break;
            case 109:
                sb.append("updateCLIR");
                sb.append(" {clirMode = ");
                sb.append(getStringClirMode(utProfile.condition));
                sb.append("}");
                break;
            case 110:
                sb.append("queryCOLP");
                break;
            case 111:
                sb.append("updateCOLP");
                sb.append(" {enable = ");
                sb.append(utProfile.enable);
                sb.append("}");
                break;
            case 112:
                sb.append("queryCOLR");
                break;
            case 113:
                sb.append("updateCOLR");
                sb.append(" {presentation = ");
                sb.append(utProfile.condition);
                sb.append("}");
                break;
            case 114:
                sb.append("queryCallWaiting");
                break;
            case 115:
                sb.append("updateCallWaiting");
                sb.append(" {enable = ");
                sb.append(getStringStatus(utProfile.enable));
                sb.append(", ssClass = ");
                sb.append(getStringSsClass(utProfile.serviceClass));
                sb.append("}");
                break;
            case 116:
                sb.append("querySimServDoc");
                break;
            case 118:
                sb.append("queryACB");
                break;
            case 119:
                sb.append("updateACB");
                break;
            default:
                sb.append(i);
                break;
        }
        return sb.toString();
    }

    public static String extractCrLogFromUtProfile(int i, UtProfile utProfile) {
        StringBuilder sb = new StringBuilder(i + "," + utProfile.requestId + ",>,");
        switch (utProfile.type) {
            case 100:
                sb.append("GET_CF,");
                sb.append(getCrLogCfType(utProfile.condition));
                break;
            case 101:
                sb.append("PUT_CF,");
                sb.append(getCrLogCfType(utProfile.condition));
                sb.append(",");
                sb.append(getCrLogAction(utProfile.action));
                sb.append(",");
                sb.append(getCrLogSsClass(utProfile.serviceClass));
                sb.append(",");
                sb.append(utProfile.timeSeconds);
                sb.append(",");
                sb.append(IMSLog.checker(utProfile.number));
                break;
            case 102:
            case 104:
                sb.append("GET_CB,");
                sb.append(getCrLogCbType(utProfile.condition));
                break;
            case 103:
            case 105:
                sb.append("PUT_CB,");
                sb.append(getCrLogCbType(utProfile.condition));
                sb.append(",");
                sb.append(getCrLogAction(utProfile.action));
                sb.append(",");
                sb.append(getCrLogSsClass(utProfile.serviceClass));
                sb.append(",");
                sb.append(IMSLog.checker(utProfile.password));
                break;
            case 106:
                sb.append("GET_CLIP");
                break;
            case 107:
                sb.append("PUT_CLIP,");
                sb.append(utProfile.enable);
                break;
            case 108:
                sb.append("GET_CLIR");
                break;
            case 109:
                sb.append("PUT_CLIR,");
                sb.append(utProfile.condition);
                break;
            case 110:
                sb.append("GET_COLP");
                break;
            case 111:
                sb.append("PUT_COLP,");
                sb.append(utProfile.enable);
                break;
            case 112:
                sb.append("GET_COLR");
                break;
            case 113:
                sb.append("PUT_COLR,");
                sb.append(utProfile.enable);
                break;
            case 114:
                sb.append("GET_CW");
                break;
            case 115:
                sb.append("PUT_CW,");
                sb.append(getCrLogStatus(utProfile.enable));
                break;
            case 116:
                sb.append("GET_SD");
                break;
        }
        return sb.toString();
    }

    public static String extractLogFromResponse(int i, Bundle[] bundleArr) {
        int i2 = i;
        Bundle[] bundleArr2 = bundleArr;
        StringBuilder sb = new StringBuilder();
        int i3 = 1;
        int i4 = 0;
        if (i2 == 100) {
            sb.append("queryCallForward");
            int length = bundleArr2.length;
            int i5 = 0;
            while (i5 < length) {
                Bundle bundle = bundleArr2[i5];
                int i6 = bundle.getInt(UtConstant.SERVICECLASS, i3);
                int i7 = bundle.getInt(UtConstant.CONDITION, i4);
                String string = bundle.getString("number");
                if (TextUtils.isEmpty(string)) {
                    string = "";
                }
                sb.append(" {cfType: ");
                sb.append(getStringCfType(i7));
                sb.append(",");
                sb.append(" status: ");
                sb.append(getStringStatus(bundle.getInt("status", 0)));
                sb.append(",");
                sb.append(" number: ");
                sb.append(IMSLog.checker(string));
                sb.append(",");
                if (i7 == 2) {
                    sb.append(" NoReplyTimer: ");
                    i4 = 0;
                    sb.append(bundle.getInt("NoReplyTimer", 0));
                    sb.append(",");
                } else {
                    i4 = 0;
                }
                sb.append(" serviceClass: ");
                sb.append(getStringSsClass(i6));
                sb.append("}");
                i5++;
                i3 = 1;
            }
        } else if (i2 == 102 || i2 == 104) {
            sb.append("queryCallBarring");
            for (Bundle bundle2 : bundleArr2) {
                sb.append(" {cbType: ");
                sb.append(getStringCbType(bundle2.getInt(UtConstant.CONDITION, 0)));
                sb.append(",");
                sb.append(" status: ");
                sb.append(getStringStatus(bundle2.getInt("status", 0)));
                sb.append(",");
                sb.append(" serviceClass: ");
                sb.append(getStringSsClass(bundle2.getInt(UtConstant.SERVICECLASS, 1)));
                sb.append("}");
            }
        } else if (i2 == 108) {
            int[] intArray = bundleArr2[0].getIntArray(UtConstant.QUERYCLIR);
            if (intArray != null) {
                sb.append("queryCLIR {");
                sb.append(intArray[0]);
                sb.append(",");
                sb.append(intArray[1]);
                sb.append("}");
            }
        } else if (i2 != 114) {
            sb.append("requestType[");
            sb.append(getStringRequestType(i));
            sb.append("]");
        } else {
            sb.append("queryCallWaiting");
            sb.append(" {status: ");
            sb.append(getStringStatus(bundleArr2[0].getBoolean("status", false)));
            sb.append("}");
        }
        return sb.toString();
    }

    public static String extractCrLogFromResponse(int i, Bundle[] bundleArr) {
        StringBuilder sb = new StringBuilder();
        if (i == 100) {
            for (Bundle bundle : bundleArr) {
                int i2 = bundle.getInt(UtConstant.SERVICECLASS, 1);
                int i3 = bundle.getInt(UtConstant.CONDITION, 0);
                String string = bundle.getString("number");
                if (TextUtils.isEmpty(string)) {
                    string = "";
                }
                sb.append(",{");
                sb.append(getCrLogStatus(bundle.getInt("status", 0)));
                sb.append(",");
                sb.append(IMSLog.checker(string));
                sb.append(",");
                if (i3 == 2) {
                    sb.append(bundle.getInt("NoReplyTimer", 0));
                    sb.append(",");
                }
                sb.append(getCrLogSsClass(i2));
                sb.append("}");
            }
        } else if (i == 102 || i == 104) {
            for (Bundle bundle2 : bundleArr) {
                sb.append(",{");
                sb.append(getCrLogStatus(bundle2.getInt("status", 0)));
                sb.append(",");
                sb.append(getCrLogSsClass(bundle2.getInt(UtConstant.SERVICECLASS, 1)));
                sb.append("}");
            }
        } else if (i == 114) {
            sb.append(",");
            sb.append(getCrLogStatus(bundleArr[0].getBoolean("status", false)));
        }
        return sb.toString();
    }

    public static String extractLogFromError(Bundle bundle) {
        return " {error: " + getStringErrorCode(bundle.getInt("originErrorCode", 0)) + "," + " converted error: " + bundle.getInt("errorCode", 0) + "}";
    }

    public static String getStringSsClass(int i) {
        if (i == 0) {
            return "ALL";
        }
        if (i == 1) {
            return "AUDIO";
        }
        if (i == 8) {
            return CloudMessageProviderContract.DataTypes.SMS;
        }
        if (i != 16) {
            return i != 255 ? Integer.toString(i) : "ALL";
        }
        return "VIDEO";
    }

    public static String getCrLogSsClass(int i) {
        if (i == 0) {
            return "ALL";
        }
        if (i == 1) {
            return "A";
        }
        if (i != 16) {
            return i != 255 ? Integer.toString(i) : "ALL";
        }
        return "V";
    }

    public static String getStringAction(int i) {
        if (i == 0) {
            return "Deactivation";
        }
        if (i == 1) {
            return "Activation";
        }
        if (i != 3) {
            return i != 4 ? Integer.toString(i) : "Erasure";
        }
        return "Registration";
    }

    public static String getCrLogAction(int i) {
        if (i == 0) {
            return "D";
        }
        if (i == 1) {
            return "A";
        }
        if (i != 3) {
            return i != 4 ? Integer.toString(i) : "E";
        }
        return "R";
    }

    public static String getStringRequestType(int i) {
        switch (i) {
            case 100:
                return "SS_GET_CF";
            case 101:
                return "SS_PUT_CF";
            case 102:
                return "SS_GET_ICB";
            case 103:
                return "SS_PUT_ICB";
            case 104:
                return "SS_GET_OCB";
            case 105:
                return "SS_PUT_OCB";
            case 106:
                return "SS_GET_CLIP";
            case 107:
                return "SS_PUT_CLIP";
            case 108:
                return "SS_GET_CLIR";
            case 109:
                return "SS_PUT_CLIR";
            case 110:
                return "SS_GET_COLP";
            case 111:
                return "SS_PUT_COLP";
            case 112:
                return "SS_GET_COLR";
            case 113:
                return "SS_PUT_COLR";
            case 114:
                return "SS_GET_CW";
            case 115:
                return "SS_PUT_CW";
            case 116:
                return "SS_GET_SD";
            case 118:
                return "SS_GET_ACB";
            case 119:
                return "SS_PUT_ACB";
            default:
                return Integer.toString(i);
        }
    }

    public static String getStringCfType(int i) {
        switch (i) {
            case 0:
                return "Unconditional";
            case 1:
                return "Busy";
            case 2:
                return "Unanswered";
            case 3:
                return "Not reachable";
            case 4:
                return "All";
            case 5:
                return "All conditional";
            case 6:
                return "Not logged in";
            default:
                return Integer.toString(i);
        }
    }

    public static String getCrLogCfType(int i) {
        if (i == 0) {
            return "U";
        }
        if (i == 1) {
            return "B";
        }
        if (i != 2) {
            return i != 3 ? Integer.toString(i) : "NRc";
        }
        return "NRy";
    }

    public static String getStringCbType(int i) {
        if (i == 1) {
            return "All incoming calls";
        }
        if (i == 2) {
            return "All outgoing calls";
        }
        if (i == 3) {
            return "Outgoing international calls";
        }
        if (i != 4) {
            return i != 5 ? Integer.toString(i) : "Incoming calls when roaming";
        }
        return "Outgoing international calls except home";
    }

    public static String getCrLogCbType(int i) {
        if (i == 1) {
            return "AI";
        }
        if (i == 2) {
            return "AO";
        }
        if (i == 3) {
            return "OI";
        }
        if (i != 4) {
            return i != 5 ? Integer.toString(i) : "ICWR";
        }
        return "OIEXHC";
    }

    public static String getStringClirMode(int i) {
        if (i == 0) {
            return "Default";
        }
        if (i != 1) {
            return i != 2 ? Integer.toString(i) : "Suppression";
        }
        return "Invocation";
    }

    public static String getStringMessage(int i) {
        if (i == 100) {
            return "EVENT_REQUEST_PDN";
        }
        switch (i) {
            case 1:
                return "EVENT_PDN_CONNECTED";
            case 2:
                return "EVENT_DISCONNECT_PDN";
            case 3:
                return "EVENT_PDN_DISCONNECTED";
            case 4:
                return "EVENT_TERMINAL_REQUEST";
            case 5:
                return "EVENT_DOCUMENT_CACHE_RESET";
            case 6:
                return "EVENT_SEPARATE_CFNL";
            case 7:
                return "EVENT_SEPARATE_CFNRY";
            case 8:
                return "EVENT_SEPARATE_CF_ALL";
            case 9:
                return "EVENT_SEPARATE_MEDIA";
            case 10:
                return "EVENT_HTTP_COMPLETE";
            case 11:
                return "EVENT_HTTP_FAIL";
            case 12:
                return "EVENT_REQUEST_FAIL";
            case 13:
                return "EVENT_CACHE_RESULT_PARSE";
            case 14:
                return "EVENT_INIT_SS_403";
            case 15:
                return "EVENT_REQUEST_TIMEOUT";
            default:
                return Integer.toString(i);
        }
    }

    public static String getStringErrorCode(int i) {
        switch (i) {
            case 1002:
                return "NOT_SUPPORT_BA_ALL";
            case 1003:
                return "ALREADY_FORBIDDEN";
            case 1004:
                return "SIM_NOT_READY";
            case 1005:
                return "NO_DDS_SLOT";
            case 1006:
                return "NO_MATCHED_PROFILE";
            case 1007:
                return "NOT_CONFIGURED";
            case 1008:
                return "INVALID_REQUEST";
            case 1009:
                return "NO_XCAP_APN";
            case 1010:
                return "NOT_SUPPORT_BARRING";
            case 1011:
                return "SERVICE_DEACTIVATED";
            case 1012:
                return "PUT_BLOCKED";
            case 1013:
                return "NOT_REGISTERED_IN_VOLTEREGIED";
            case 1014:
                return "START_PDN_FAILURE";
            case 1015:
                return "HTTP_CONNECTION_ERROR";
            case 1016:
                return "INVALID_PDN_REQUEST";
            case 1017:
                return "TIMEOUT";
            case 1018:
                return "EMPTY_DNS";
            default:
                if (!isPdnFailCause(i)) {
                    return Integer.toString(i);
                }
                StringBuilder sb = new StringBuilder();
                sb.append("PDN_REJECT: ");
                sb.append(i - 10022);
                return sb.toString();
        }
    }

    public static boolean isPdnFailCause(int i) {
        return i / 10000 > 0;
    }

    public static void i(String str, String str2) {
        IMSLog.i(str, "UtXcap " + str2);
    }

    public static void i(String str, int i, String str2) {
        IMSLog.i(str, i, "UtXcap " + str2);
    }
}
