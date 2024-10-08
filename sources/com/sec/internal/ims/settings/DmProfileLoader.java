package com.sec.internal.ims.settings;

import android.content.ContentValues;
import android.content.Context;
import android.text.TextUtils;
import android.util.Log;
import com.sec.ims.configuration.DATA;
import com.sec.ims.settings.ImsProfile;
import com.sec.ims.settings.NvConfiguration;
import com.sec.internal.constants.Mno;
import com.sec.internal.constants.ims.DiagnosisConstants;
import com.sec.internal.helper.DmConfigHelper;
import java.util.List;
import java.util.Map;
import java.util.concurrent.CopyOnWriteArrayList;

public class DmProfileLoader {
    public static final String LOG_TAG = "DmProfileLoader";
    protected static boolean mIsKorOp = false;
    protected static List<String> mLboPcscfAddrList = new CopyOnWriteArrayList();
    protected static int mLboPcscfPort = -1;
    protected static boolean mSmsOverIms = false;
    protected static ContentValues mValueList = new ContentValues();

    public static ImsProfile getProfile(Context context, ImsProfile imsProfile, int i) {
        return getFromDmStorage(context, imsProfile, i);
    }

    private static ImsProfile getFromDmStorage(Context context, ImsProfile imsProfile, int i) {
        int indexOf;
        mValueList.clear();
        mLboPcscfAddrList.clear();
        mLboPcscfPort = -1;
        mIsKorOp = Mno.fromName(imsProfile.getMnoName()).isKor();
        Log.e(LOG_TAG, "mIsKorOp: " + mIsKorOp);
        for (Map.Entry next : DmConfigHelper.read(context, "omadm/*", i).entrySet()) {
            String str = (String) next.getValue();
            String replaceFirst = ((String) next.getKey()).replaceFirst(DmConfigModule.DM_PATH, "");
            setValueWithUri(replaceFirst, str);
            if (replaceFirst != null && !TextUtils.isEmpty(str) && replaceFirst.contains("./3GPP_IMS/LBO_P-CSCF_Address") && replaceFirst.endsWith("Address")) {
                if (str.indexOf(91) < 0 || str.indexOf(93) < 0) {
                    int indexOf2 = str.indexOf(58);
                    if (indexOf2 >= 0) {
                        mLboPcscfPort = Integer.parseInt(str.substring(indexOf2 + 1));
                        str = str.substring(0, indexOf2);
                    }
                    mLboPcscfAddrList.add(str);
                } else {
                    if (str.indexOf("]:") > 0 && (indexOf = str.indexOf("]:")) >= 0) {
                        mLboPcscfPort = Integer.parseInt(str.substring(indexOf + 2));
                    }
                    mLboPcscfAddrList.add(str.substring(str.indexOf(91) + 1, str.indexOf(93)));
                }
                if (mLboPcscfPort == -1) {
                    mLboPcscfPort = imsProfile.getSipPort();
                }
            }
        }
        if (mLboPcscfAddrList.isEmpty() && !imsProfile.getPcscfList().isEmpty()) {
            mLboPcscfAddrList.addAll(imsProfile.getPcscfList());
            mLboPcscfPort = imsProfile.getSipPort();
        }
        if (mIsKorOp) {
            mSmsOverIms = TextUtils.equals(NvConfiguration.get(context, "sms_over_ip_network_indication", "", i), "1");
            Log.e(LOG_TAG, "mSmsOverIms: " + mSmsOverIms);
        } else {
            mSmsOverIms = NvConfiguration.getSmsIpNetworkIndi(context, i);
        }
        ImsProfile imsProfile2 = new ImsProfile(imsProfile);
        updateDbInfoToProfile(imsProfile2);
        return imsProfile2;
    }

    private static void updateDbInfoToProfile(ImsProfile imsProfile) {
        Log.e(LOG_TAG, "updateDbInfoToProfile");
        if (getIntValue("12") > 0) {
            imsProfile.setTimer1(getIntValue("12"));
        }
        if (getIntValue("13") > 0) {
            imsProfile.setTimer2(getIntValue("13"));
        }
        if (getIntValue("14") > 0) {
            imsProfile.setTimer4(getIntValue("14"));
        }
        if (getIntValue("15") > 0) {
            imsProfile.setTimerA(getIntValue("15"));
        }
        if (getIntValue("16") > 0) {
            imsProfile.setTimerB(getIntValue("16"));
        }
        if (getIntValue("17") > 0) {
            imsProfile.setTimerC(getIntValue("17"));
        }
        if (getIntValue("18") > 0) {
            imsProfile.setTimerD(getIntValue("18"));
        }
        if (getIntValue("19") > 0) {
            imsProfile.setTimerE(getIntValue("19"));
        }
        if (getIntValue("20") > 0) {
            imsProfile.setTimerF(getIntValue("20"));
        }
        if (getIntValue("21") > 0) {
            imsProfile.setTimerG(getIntValue("21"));
        }
        if (getIntValue("22") > 0) {
            imsProfile.setTimerH(getIntValue("22"));
        }
        if (getIntValue("23") > 0) {
            imsProfile.setTimerI(getIntValue("23"));
        }
        if (getIntValue("24") > 0) {
            imsProfile.setTimerJ(getIntValue("24"));
        }
        if (getIntValue("25") > 0) {
            imsProfile.setTimerK(getIntValue("25"));
        }
        setInt(imsProfile, "amrnboa_payload", getIntValue("66"));
        setInt(imsProfile, "amrnbbe_payload", getIntValue("67"));
        setInt(imsProfile, "amrwboa_payload", getIntValue("64"));
        setInt(imsProfile, "amrwbbe_payload", getIntValue("65"));
        setInt(imsProfile, "dtmf_nb_payload", getIntValue("71"));
        setInt(imsProfile, "dtmf_wb_payload", getIntValue("70"));
        setInt(imsProfile, "h264_qvga_payload", getIntValue("69"));
        setInt(imsProfile, "h264_vga_payload", getIntValue("68"));
        setInt(imsProfile, "h264_vgal_payload", getIntValue("108"));
        setInt(imsProfile, "h263_qcif_payload", getIntValue("132"));
        setInt(imsProfile, "audio_port_start", getIntValue("60"));
        setInt(imsProfile, "audio_port_end", getIntValue("61"));
        setInt(imsProfile, "video_port_start", getIntValue("62"));
        setInt(imsProfile, "video_port_end", getIntValue("63"));
        boolean z = false;
        if (getStringValue("129") != null) {
            setInt(imsProfile, "evs_payload", getIntValue("129"));
            imsProfile.put("enable_evs_codec", Boolean.valueOf(getIntValue("129") > 0));
        }
        if (getStringValue("131") != null) {
            setString(imsProfile, "evs_default_bitrate", getStringValue("131"));
        }
        if (getStringValue("130") != null) {
            setString(imsProfile, "evs_default_bandwidth", getStringValue("130"));
        }
        imsProfile.setSmsPsi(getStringValue("73"));
        imsProfile.setLboPcscfAddressList(mLboPcscfAddrList);
        imsProfile.setLboPcscfPort(mLboPcscfPort);
        setString(imsProfile, "amrnb_mode", getStringValue("6"));
        setString(imsProfile, "amrwb_mode", getStringValue("7"));
        setInt(imsProfile, "publish_timer", getIntValue("36"));
        setInt(imsProfile, "extended_publish_timer", getIntValue("37"));
        setInt(imsProfile, "cap_cache_exp", getIntValue("26"));
        setInt(imsProfile, "cap_poll_interval", getIntValue("27"));
        setInt(imsProfile, "src_throttle_publish", getIntValue("28"));
        setInt(imsProfile, "poll_list_sub_exp", getIntValue("35"));
        imsProfile.put("enable_gzip", Boolean.valueOf(getIntValue("38") == 1));
        setInt(imsProfile, "subscribe_max_entry", getIntValue("29"));
        imsProfile.setSupportSmsOverIms(mSmsOverIms);
        setInt(imsProfile, "dm_polling_period", getIntValue("90"));
        if (getIntValue("116") >= 0) {
            imsProfile.put("support_ipsec", Boolean.valueOf(getIntValue("116") == 1));
        }
        if (mIsKorOp && getIntValue("116") != -100000) {
            imsProfile.put("support_ipsec", Boolean.valueOf(getIntValue("116") == 1));
        }
        if (getIntValue("72") >= 0) {
            if (getIntValue("72") == 1) {
                z = true;
            }
            imsProfile.put("volte_service_status", Boolean.valueOf(z));
        }
        if (getStringValue("55") != null && getStringValue("55").equals("0")) {
            imsProfile.put("audio_capabilities", DiagnosisConstants.RCSM_ORST_REGI);
        }
        setInt(imsProfile, "h265_hd720p_payload", getIntValue("159"));
        setInt(imsProfile, "reg_retry_base_time", getIntValue("84"));
        setInt(imsProfile, "reg_retry_max_time", getIntValue("85"));
    }

    private static int getIntValue(String str) {
        int i = mIsKorOp ? -100000 : -1;
        try {
            return Integer.parseInt((String) mValueList.get(str));
        } catch (NumberFormatException unused) {
            Log.e(LOG_TAG, "no Value for " + str);
            return i;
        }
    }

    private static String getStringValue(String str) {
        return (String) mValueList.get(str);
    }

    private static int getUriIndex(String str) {
        for (DATA.DM_FIELD_INFO dm_field_info : DATA.DM_FIELD_LIST) {
            String name = dm_field_info.getName();
            if (dm_field_info.getType() != 0) {
                name = "./3GPP_IMS/" + name;
            }
            if (str.equals(name)) {
                return dm_field_info.getIndex();
            }
        }
        return -1;
    }

    public static void setValueWithUri(String str, String str2) {
        int uriIndex;
        if (str != null && str2 != null && (uriIndex = getUriIndex(str)) != -1) {
            mValueList.put(Integer.toString(uriIndex), str2);
        }
    }

    private static void setInt(ImsProfile imsProfile, String str, int i) {
        if (mIsKorOp) {
            if (i != -100000) {
                imsProfile.put(str, Integer.valueOf(i));
            }
        } else if (i > 0) {
            imsProfile.put(str, Integer.valueOf(i));
        }
    }

    private static void setString(ImsProfile imsProfile, String str, String str2) {
        if (str2 != null) {
            imsProfile.put(str, str2);
        }
    }
}
