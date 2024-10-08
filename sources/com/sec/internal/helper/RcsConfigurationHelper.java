package com.sec.internal.helper;

import android.content.Context;
import android.database.Cursor;
import android.net.Uri;
import android.os.PersistableBundle;
import android.provider.Telephony;
import android.telephony.CarrierConfigManager;
import android.text.TextUtils;
import com.sec.ims.settings.ImsProfile;
import com.sec.ims.util.ImsUri;
import com.sec.internal.constants.ims.DiagnosisConstants;
import com.sec.internal.constants.ims.SipMsg;
import com.sec.internal.constants.ims.config.ConfigConstants;
import com.sec.internal.constants.ims.servicemodules.im.ImConstants;
import com.sec.internal.constants.ims.settings.GlobalSettingsConstants;
import com.sec.internal.google.SecImsNotifier;
import com.sec.internal.ims.config.ConfigContract;
import com.sec.internal.ims.registry.ImsRegistry;
import com.sec.internal.ims.settings.ServiceConstants;
import com.sec.internal.ims.util.ImsUtil;
import com.sec.internal.log.IMSLog;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.TreeMap;

public final class RcsConfigurationHelper {
    private static final String LOG_TAG = "RcsConfigurationHelper";

    public static Boolean readBoolParam(Context context, String str) {
        return Boolean.valueOf("1".equals(readStringParam(context, str)));
    }

    public static Boolean readBoolParam(Context context, String str, Boolean bool) {
        String readStringParam = readStringParam(context, str);
        return readStringParam != null ? Boolean.valueOf("1".equals(readStringParam)) : bool;
    }

    public static String getPathWithPhoneId(String str, int i) {
        return str + "#" + "simslot" + i;
    }

    public static Integer readIntParam(Context context, String str, Integer num) {
        String readStringParam = readStringParam(context, str);
        if (readStringParam == null) {
            return num;
        }
        try {
            return Integer.valueOf(Integer.parseInt(readStringParam));
        } catch (NumberFormatException e) {
            e.printStackTrace();
            return num;
        }
    }

    public static Long readLongParam(Context context, String str, Long l) {
        String readStringParam = readStringParam(context, str);
        if (readStringParam == null) {
            return l;
        }
        try {
            return Long.valueOf(Long.parseLong(readStringParam));
        } catch (NumberFormatException e) {
            e.printStackTrace();
            return l;
        }
    }

    public static ImsUri readImsUriParam(Context context, String str) {
        String readStringParam = readStringParam(context, str);
        if (!TextUtils.isEmpty(readStringParam)) {
            return ImsUri.parse(readStringParam);
        }
        return null;
    }

    public static List<ImsUri> readListImsUriParam(Context context, String str) {
        ImsUri parse;
        ArrayList arrayList = new ArrayList();
        for (String next : readListStringParam(context, str)) {
            if (!TextUtils.isEmpty(next) && (parse = ImsUri.parse(next)) != null) {
                arrayList.add(parse);
            }
        }
        return arrayList;
    }

    public static String readStringParam(Context context, String str) {
        Map<String, String> readParam = readParam(context, str);
        if (readParam == null || readParam.isEmpty()) {
            return null;
        }
        Iterator<Map.Entry<String, String>> it = readParam.entrySet().iterator();
        if (it.hasNext()) {
            return (String) it.next().getValue();
        }
        return null;
    }

    public static String readStringParam(Context context, String str, String str2) {
        Map<String, String> readParam = readParam(context, str);
        if (readParam != null && !readParam.isEmpty()) {
            Iterator<Map.Entry<String, String>> it = readParam.entrySet().iterator();
            if (it.hasNext()) {
                return (String) it.next().getValue();
            }
        }
        return str2;
    }

    public static List<String> readListStringParam(Context context, String str) {
        ArrayList arrayList = new ArrayList();
        Map<String, String> readParam = readParam(context, str);
        if (readParam != null && !readParam.isEmpty()) {
            for (Map.Entry<String, String> value : readParam.entrySet()) {
                arrayList.add((String) value.getValue());
            }
        }
        return arrayList;
    }

    private static Map<String, String> readParam(Context context, String str) {
        Uri uri = ConfigConstants.CONTENT_URI;
        Uri uriParamWithPhoneId = getUriParamWithPhoneId(uri, "parameter/" + str);
        if (uriParamWithPhoneId == null) {
            return null;
        }
        TreeMap treeMap = new TreeMap(String.CASE_INSENSITIVE_ORDER);
        Cursor query = context.getContentResolver().query(uriParamWithPhoneId, (String[]) null, (String) null, (String[]) null, (String) null);
        if (query != null) {
            try {
                if (query.moveToFirst()) {
                    if (query.getColumnCount() != 1 || !"NODATA".equals(query.getColumnName(0))) {
                        for (int i = 0; i < query.getColumnCount(); i++) {
                            treeMap.put(query.getColumnName(i), query.getString(i));
                        }
                    } else {
                        query.close();
                        return null;
                    }
                }
            } catch (Throwable th) {
                th.addSuppressed(th);
            }
        }
        if (query != null) {
            query.close();
        }
        return treeMap;
        throw th;
    }

    public static Boolean readBoolParamWithPath(Context context, String str) {
        return Boolean.valueOf("1".equals(readStringParamWithPath(context, str)));
    }

    public static String readStringParamWithPath(Context context, String str) {
        Map<String, String> readParamWithPath = readParamWithPath(context, str);
        if (readParamWithPath == null || readParamWithPath.isEmpty()) {
            return null;
        }
        Iterator<Map.Entry<String, String>> it = readParamWithPath.entrySet().iterator();
        if (it.hasNext()) {
            return (String) it.next().getValue();
        }
        return null;
    }

    private static Map<String, String> readParamWithPath(Context context, String str) {
        Uri uriParamWithPhoneId = getUriParamWithPhoneId(ConfigConstants.CONTENT_URI, str);
        if (uriParamWithPhoneId == null) {
            return null;
        }
        TreeMap treeMap = new TreeMap(String.CASE_INSENSITIVE_ORDER);
        Cursor query = context.getContentResolver().query(uriParamWithPhoneId, (String[]) null, (String) null, (String[]) null, (String) null);
        if (query != null) {
            try {
                if (query.moveToFirst()) {
                    if (query.getColumnCount() != 1 || !"NODATA".equals(query.getColumnName(0))) {
                        for (int i = 0; i < query.getColumnCount(); i++) {
                            treeMap.put(query.getColumnName(i), query.getString(i));
                        }
                    } else {
                        query.close();
                        return null;
                    }
                }
            } catch (Throwable th) {
                th.addSuppressed(th);
            }
        }
        if (query != null) {
            query.close();
        }
        return treeMap;
        throw th;
    }

    public static String getUuid(Context context, int i) {
        ConfigData configData = getConfigData(context, "root/application/*", i);
        if (configData != null) {
            return configData.readString("uuid_Value", "");
        }
        IMSLog.i(LOG_TAG, "getUuid: configData is not found");
        return "";
    }

    public static List<String> getRcsEnabledServiceList(Context context, int i, String str) {
        ArrayList arrayList = new ArrayList();
        ConfigData configData = getConfigData(context, "root/application/*", i);
        if (configData == null) {
            IMSLog.i(LOG_TAG, "getRcsEnabledServiceList: configData is not found");
            return arrayList;
        }
        if (configData.readInt(ConfigConstants.ConfigTable.CAPDISCOVERY_DEFAULT_DISC, 0).intValue() != 2) {
            arrayList.add("options");
            arrayList.add(SipMsg.EVENT_PRESENCE);
        }
        updateImEnabledServices(i, configData, arrayList, str);
        Boolean bool = Boolean.FALSE;
        if (configData.readBool(ConfigConstants.ConfigTable.SERVICES_IS_AUTH, bool).booleanValue()) {
            arrayList.add("is");
        }
        if (configData.readInt(ConfigConstants.ConfigTable.SERVICES_VS_AUTH, 0).intValue() != 0) {
            arrayList.add("vs");
        }
        if (!TextUtils.isEmpty(configData.readString(ConfigConstants.ConfigTable.EXT_END_USER_CONF_REQID, ""))) {
            arrayList.add("euc");
        }
        if (configData.readBool(ConfigConstants.ConfigTable.SERVICES_GEOPUSH_AUTH, bool).booleanValue()) {
            arrayList.add("gls");
        }
        updateComposerEnabledServices(i, configData, arrayList);
        arrayList.add("profile");
        if (configData.readBool(ConfigConstants.ConfigTable.CAPDISCOVERY_JOYN_LASTSEENACTIVE, bool).booleanValue()) {
            arrayList.add("lastseen");
        }
        IMSLog.i(LOG_TAG, i, "getRcsEnabledServiceList: svcList = " + arrayList);
        return arrayList;
    }

    private static void updateImEnabledServices(int i, ConfigData configData, List<String> list, String str) {
        Boolean bool = Boolean.FALSE;
        if (configData.readBool(ConfigConstants.ConfigTable.SERVICES_CHAT_AUTH, bool).booleanValue()) {
            list.add("im");
        }
        boolean booleanValue = configData.readBool(ConfigConstants.ConfigTable.SERVICES_FT_AUTH, bool).booleanValue();
        String readString = configData.readString(ConfigConstants.ConfigTable.IM_FT_HTTP_CS_URI, "");
        ImConstants.FtMech ftDefaultTech = getFtDefaultTech(configData, str, i);
        if (!ImsProfile.isRcsUp2Profile(str)) {
            if (booleanValue) {
                list.add("ft");
            }
            if (!TextUtils.isEmpty(readString) && ftDefaultTech == ImConstants.FtMech.HTTP) {
                list.add("ft_http");
            }
        } else if (booleanValue && !TextUtils.isEmpty(readString)) {
            list.add("ft_http");
        }
        if (configData.readInt(ConfigConstants.ConfigTable.SERVICES_SLM_AUTH, 0).intValue() != 0) {
            list.add("slm");
        }
        if (configData.readInt(ConfigConstants.ConfigTable.CHATBOT_CHATBOT_MSG_TECH, 1).intValue() != 0) {
            list.add(ServiceConstants.SERVICE_CHATBOT_COMMUNICATION);
        }
        if (!TextUtils.isEmpty(configData.readString(ConfigConstants.ConfigTable.PLUGINS_CATALOGURI, ""))) {
            list.add("plug-in");
        }
    }

    private static void updateComposerEnabledServices(int i, ConfigData configData, List<String> list) {
        int intValue = configData.readInt(ConfigConstants.ConfigTable.SERVICES_COMPOSER_AUTH, 0).intValue();
        IMSLog.i(LOG_TAG, i, "updateComposerEnabledServices: composer auth = " + intValue);
        if (!(intValue == 1 || intValue == 3)) {
            Boolean bool = Boolean.FALSE;
            if (!configData.readBool(ConfigConstants.ConfigTable.SERVICES_SHARED_MAP_AUTH, bool).booleanValue() && !configData.readBool(ConfigConstants.ConfigTable.SERVICES_SHARED_SKETCH_AUTH, bool).booleanValue() && !configData.readBool(ConfigConstants.ConfigTable.SERVICES_POST_CALL_AUTH, bool).booleanValue()) {
                return;
            }
        }
        list.add("ec");
    }

    public static ConfigData getConfigData(Context context, String str, int i) {
        return new ConfigData(context, getPathWithPhoneId(str, i));
    }

    protected static Uri getUriParamWithPhoneId(Uri uri, String str) {
        int i;
        if (str == null) {
            return null;
        }
        if (str.contains("#simslot0")) {
            i = 0;
        } else if (str.contains("#simslot1")) {
            i = 1;
        } else {
            i = SimUtil.getSimSlotPriority();
        }
        Uri.Builder buildUpon = Uri.parse(uri + str.replaceAll("#simslot\\d", "")).buildUpon();
        return buildUpon.fragment("simslot" + i).build();
    }

    public static class ConfigData {
        Map<String, String> mDataMap;

        public ConfigData(Map map) {
            new TreeMap();
            this.mDataMap = map;
        }

        public ConfigData(Context context, String str) {
            this.mDataMap = new TreeMap();
            Uri uriParamWithPhoneId = RcsConfigurationHelper.getUriParamWithPhoneId(ConfigConstants.CONTENT_URI, str);
            if (uriParamWithPhoneId != null) {
                TreeMap treeMap = new TreeMap(String.CASE_INSENSITIVE_ORDER);
                Cursor query = context.getContentResolver().query(uriParamWithPhoneId, (String[]) null, (String) null, (String[]) null, (String) null);
                if (query != null) {
                    try {
                        if (query.moveToFirst()) {
                            if (query.getColumnCount() != 1 || !"NODATA".equals(query.getColumnName(0))) {
                                for (int i = 0; i < query.getColumnCount(); i++) {
                                    treeMap.put(query.getColumnName(i), query.getString(i));
                                }
                            } else {
                                query.close();
                                return;
                            }
                        }
                    } catch (Throwable th) {
                        th.addSuppressed(th);
                    }
                }
                if (query != null) {
                    query.close();
                }
                this.mDataMap.putAll(treeMap);
                return;
            }
            return;
            throw th;
        }

        public Boolean readBool(String str, Boolean bool) {
            String readFromMap = readFromMap(str);
            return !TextUtils.isEmpty(readFromMap) ? Boolean.valueOf("1".equals(readFromMap)) : bool;
        }

        public Integer readInt(String str, Integer num) {
            String readFromMap = readFromMap(str);
            if (TextUtils.isEmpty(readFromMap)) {
                return num;
            }
            try {
                return Integer.valueOf(Integer.parseInt(readFromMap));
            } catch (NumberFormatException e) {
                e.printStackTrace();
                return num;
            }
        }

        public Integer readIntWithinRange(String str, Integer num, Integer num2, Integer num3) {
            Integer readInt = readInt(str, num);
            return (readInt.intValue() < num2.intValue() || readInt.intValue() > num3.intValue()) ? num : readInt;
        }

        public Long readLong(String str, Long l) {
            String readFromMap = readFromMap(str);
            if (TextUtils.isEmpty(readFromMap)) {
                return l;
            }
            try {
                return Long.valueOf(Long.parseLong(readFromMap));
            } catch (NumberFormatException e) {
                e.printStackTrace();
                return l;
            }
        }

        public String readString(String str, String str2) {
            String readFromMap = readFromMap(str);
            return (TextUtils.isEmpty(readFromMap) || "null".equalsIgnoreCase(readFromMap)) ? str2 : readFromMap;
        }

        public List<String> readListString(String str) {
            return readListFromMap(str);
        }

        public Uri readUri(String str, Uri uri) {
            String readFromMap = readFromMap(str);
            return (TextUtils.isEmpty(readFromMap) || "null".equalsIgnoreCase(readFromMap)) ? uri : Uri.parse(readFromMap);
        }

        public ImsUri readImsUri(String str, ImsUri imsUri) {
            String readFromMap = readFromMap(str);
            return (TextUtils.isEmpty(readFromMap) || "null".equalsIgnoreCase(readFromMap)) ? imsUri : ImsUri.parse(readFromMap);
        }

        private String readFromMap(String str) {
            IMSLog.i(RcsConfigurationHelper.LOG_TAG, "readFromMap: param: " + str);
            Map<String, String> map = this.mDataMap;
            if (map == null || map.isEmpty() || str == null) {
                IMSLog.i(RcsConfigurationHelper.LOG_TAG, "readFromMap: cannot read the param");
                return "";
            }
            Map<String, String> map2 = ConfigContract.PATH_TABLE;
            Locale locale = Locale.US;
            String str2 = map2.get(str.toLowerCase(locale));
            IMSLog.i(RcsConfigurationHelper.LOG_TAG, "readFromMap: path: " + str2);
            if (str2 == null) {
                IMSLog.i(RcsConfigurationHelper.LOG_TAG, "readFromMap: path is null");
                return "";
            }
            String str3 = this.mDataMap.get(str2);
            if (str3 == null || str3.isEmpty()) {
                String readFromSecondMap = RcsConfigurationHelper.readFromSecondMap(str2, str.toLowerCase(locale), this.mDataMap);
                IMSLog.s(RcsConfigurationHelper.LOG_TAG, "readFromMap: param: " + str + " value: " + readFromSecondMap);
                if (str2.equalsIgnoreCase("root/application/0/ext/uuid_Value")) {
                    IMSLog.i(RcsConfigurationHelper.LOG_TAG, "readFromMap: " + str + "'s value is null, trying to get from old path");
                    readFromSecondMap = this.mDataMap.get("root/application/1/other/uuid_Value".toLowerCase(locale));
                }
                if (readFromSecondMap != null && !readFromSecondMap.isEmpty()) {
                    IMSLog.s(RcsConfigurationHelper.LOG_TAG, "readFromMap: param: " + str + " value: " + readFromSecondMap);
                    return readFromSecondMap;
                } else if (!str2.equalsIgnoreCase("root/application/0/ext/uuid_Value")) {
                    return readFromSecondMap;
                } else {
                    IMSLog.i(RcsConfigurationHelper.LOG_TAG, "readFromMap: " + str + "'s value is null, trying to get from UP20 path");
                    return this.mDataMap.get("root/application/0/3gpp_ims/ext/gsma/uuid_Value".toLowerCase(locale));
                }
            } else {
                IMSLog.s(RcsConfigurationHelper.LOG_TAG, "readFromMap: param: " + str + " value: " + str3);
                return str3;
            }
        }

        private List<String> getPublicUserIdentities(Map<String, String> map) {
            List<String> publicUserIdentities = RcsConfigurationHelper.getPublicUserIdentities(ConfigConstants.ConfigPath.PUBLIC_USER_IDENTITY_CHARACTERISTIC_PATH, map);
            if (publicUserIdentities.isEmpty()) {
                publicUserIdentities = RcsConfigurationHelper.getPublicUserIdentities(ConfigConstants.ConfigPath.PUBLIC_USER_IDENTITY_CHARACTERISTIC_UP10_PATH, map);
            }
            return !publicUserIdentities.isEmpty() ? publicUserIdentities : RcsConfigurationHelper.getPublicUserIdentities(ConfigConstants.ConfigPath.PUBLIC_USER_IDENTITY_CHARACTERISTIC_UP20_PATH, map);
        }

        private List<String> getLboPcscfAddresses(String str, Map<String, String> map) {
            List<String> lboPcscfAddresses = RcsConfigurationHelper.getLboPcscfAddresses(ConfigConstants.ConfigPath.LBO_PCSCF_ADDRESS_CHARACTERISTIC_PATH, str, map);
            if (lboPcscfAddresses.isEmpty()) {
                lboPcscfAddresses = RcsConfigurationHelper.getLboPcscfAddresses(ConfigConstants.ConfigPath.LBO_PCSCF_ADDRESSES_CHARACTERISTIC_UP10_PATH, str, map);
            }
            if (lboPcscfAddresses.isEmpty()) {
                lboPcscfAddresses = RcsConfigurationHelper.getLboPcscfAddresses(ConfigConstants.ConfigPath.LBO_PCSCF_ADDRESS_CHARACTERISTIC_UP20_PATH, str, map);
            }
            return !lboPcscfAddresses.isEmpty() ? lboPcscfAddresses : RcsConfigurationHelper.getLboPcscfAddresses(ConfigConstants.ConfigPath.LBO_PCSCF_ADDRESSES_CHARACTERISTIC_UP20_PATH, str, map);
        }

        private List<String> readListFromMap(String str) {
            ArrayList arrayList = new ArrayList();
            IMSLog.i(RcsConfigurationHelper.LOG_TAG, "readListFromMap: param: " + str);
            Map<String, String> map = this.mDataMap;
            if (map == null || map.isEmpty() || str == null) {
                IMSLog.i(RcsConfigurationHelper.LOG_TAG, "readListFromMap: cannot read the param");
                return arrayList;
            } else if (ConfigConstants.ConfigTable.CAPDISCOVERY_ALLOWED_PREFIXES.equalsIgnoreCase(str)) {
                return RcsConfigurationHelper.getCapAllowedPrefixes(ConfigConstants.ConfigPath.CAPDISCOVERY_ALLOWED_PREFIXES_PATH, this.mDataMap);
            } else {
                if (ConfigConstants.ConfigTable.PUBLIC_USER_IDENTITY.equalsIgnoreCase(str)) {
                    return getPublicUserIdentities(this.mDataMap);
                }
                if ("address".equalsIgnoreCase(str) || ConfigConstants.ConfigTable.LBO_PCSCF_ADDRESS_TYPE.equalsIgnoreCase(str)) {
                    return getLboPcscfAddresses(str, this.mDataMap);
                }
                IMSLog.s(RcsConfigurationHelper.LOG_TAG, "readListFromMap: param: " + str + " value: " + arrayList);
                return arrayList;
            }
        }
    }

    public static String getUserName(Context context, int i) {
        String readStringParam = readStringParam(context, getPathWithPhoneId("UserName", i), "");
        IMSLog.s(LOG_TAG, "userName: " + readStringParam);
        return readStringParam;
    }

    public static String getImpu(Context context, int i) {
        List<ImsUri> readListImsUriParam = readListImsUriParam(context, getPathWithPhoneId(ConfigConstants.ConfigTable.PUBLIC_USER_IDENTITY, i));
        for (ImsUri next : readListImsUriParam) {
            if (next.getUriType() == ImsUri.UriType.SIP_URI) {
                return next.toString();
            }
        }
        if (readListImsUriParam.size() <= 0) {
            return null;
        }
        StringBuilder sb = new StringBuilder();
        sb.append("sip:");
        sb.append(readListImsUriParam.get(0).getMsisdn());
        sb.append("@");
        sb.append(readStringParam(context, getPathWithPhoneId(ConfigConstants.ConfigTable.HOME_NETWORK_DOMAIN_NAME, i), (String) null));
        IMSLog.s(LOG_TAG, "getImpuFromProfile::ConvertingTELtoSIP: " + sb.toString());
        return sb.toString();
    }

    public static ImsUri.UriType getNetworkUriType(Context context, String str, boolean z, int i) {
        ImsUri.UriType uriType = ImsUri.UriType.TEL_URI;
        int intValue = readIntParam(context, getPathWithPhoneId(ConfigConstants.ConfigTable.EXT_INT_URL_FORMAT, i), -1).intValue();
        IMSLog.i(LOG_TAG, i, "getNetworkUriType: rcsUriFmt[" + intValue + "]");
        if (!z || intValue < 0) {
            return "sip".equalsIgnoreCase(str) ? ImsUri.UriType.SIP_URI : uriType;
        }
        return intValue == 1 ? ImsUri.UriType.SIP_URI : ImsUri.UriType.TEL_URI;
    }

    public static String readFromSecondMap(String str, String str2, Map<String, String> map) {
        IMSLog.i(LOG_TAG, "readFromSecondMap: param: " + str2 + " path: " + str);
        if (isRootAppUp20Param(str2)) {
            IMSLog.i(LOG_TAG, "readFromSecondMap: read the param on the second path: root/application/0/3gpp_ims/");
            return map.get(ConfigConstants.ConfigPath.APPLICATION_CHARACTERISTIC_UP20_PATH + str2);
        } else if (ConfigConstants.ConfigPath.EXT_CHARACTERISTIC_PATH.equalsIgnoreCase(str)) {
            IMSLog.i(LOG_TAG, "readFromSecondMap: read the param on the second path: root/application/0/3gpp_ims/ext/gsma/");
            return map.get("root/application/0/3gpp_ims/ext/gsma/" + str2);
        } else if (ConfigConstants.ConfigPath.APPAUTH_CHARACTERISTIC_PATH.equalsIgnoreCase(str)) {
            IMSLog.i(LOG_TAG, "readFromSecondMap: read the param on the second path: root/application/0/3gpp_ims/ext/gsma/");
            return map.get("root/application/0/3gpp_ims/ext/gsma/" + str2);
        } else if (isJoynParam(str2)) {
            IMSLog.i(LOG_TAG, "readFromSecondMap: read the param on the second path: root/application/1/capdiscovery/ext/joyn/");
            return map.get(ConfigConstants.ConfigPath.CAPDISCOVERY_EXT_JOYN_PATH + str2);
        } else if (ConfigConstants.ConfigTable.PRESENCE_LOC_INFO_MAX_VALID_TIME.equalsIgnoreCase(str2)) {
            IMSLog.i(LOG_TAG, "readFromSecondMap: read the param on the second path: root/application/1/presence/location/");
            return map.get(ConfigConstants.ConfigPath.PRESENCE_LOCATION_PATH + str2);
        } else if (isImFtExtParam(str2)) {
            IMSLog.i(LOG_TAG, "readFromSecondMap: read the param on the second path: root/application/1/im/ext/");
            return map.get(ConfigConstants.ConfigPath.IM_EXT_CHARACTERISTIC_PATH + str2);
        } else if (isChatParam(str2)) {
            IMSLog.i(LOG_TAG, "readFromSecondMap: read the param on the second path: root/application/1/messaging/chat/");
            return map.get(ConfigConstants.ConfigPath.CHAT_CHARACTERISTIC_PATH + str2);
        } else if (isFiletransferParam(str2)) {
            IMSLog.i(LOG_TAG, "readFromSecondMap: read the param on the second path: root/application/1/messaging/filetransfer/");
            return map.get(ConfigConstants.ConfigPath.FILETRANSFER_CHARACTERISTIC_PATH + str2);
        } else if (ConfigConstants.ConfigTable.IM_EXPLODER_URI.equalsIgnoreCase(str2)) {
            IMSLog.i(LOG_TAG, "readFromSecondMap: read the param on the second path: root/application/1/messaging/standalonemsg/");
            return map.get(ConfigConstants.ConfigPath.STANDALONEMSG_CHARACTERISTIC_PATH + str2);
        } else if (isStandaloneMsgUp20Param(str2)) {
            IMSLog.i(LOG_TAG, "readFromSecondMap: read the param on the second path: root/application/1/messaging/standalonemsg/");
            return map.get(ConfigConstants.ConfigPath.STANDALONEMSG_CHARACTERISTIC_PATH + str2);
        } else if (isCpmParam(str2)) {
            IMSLog.i(LOG_TAG, "readFromSecondMap: read the param on the second path: root/application/1/cpm/");
            return map.get(ConfigConstants.ConfigPath.CPM_CHARACTERISTIC_PATH + str2);
        } else if (isMessageStoreParam(str2)) {
            IMSLog.i(LOG_TAG, "readFromSecondMap: read the param on the second path: root/application/1/messaging/messagestore/");
            return map.get(ConfigConstants.ConfigPath.MESSAGESTORE_CHARACTERISTIC_PATH + str2);
        } else if (ConfigConstants.ConfigPath.TRANSPORT_PROTO_CHARACTERISTIC_PATH.equalsIgnoreCase(str)) {
            String str3 = map.get(ConfigConstants.ConfigPath.TRANSPORT_PROTO_CHARACTERISTIC_UP10_PATH + str2);
            if (str3 != null && !str3.isEmpty()) {
                return str3;
            }
            return map.get(ConfigConstants.ConfigPath.TRANSPORT_PROTO_CHARACTERISTIC_UP20_PATH + str2);
        } else if (isJoynUxParam(str2)) {
            IMSLog.i(LOG_TAG, "readFromSecondMap: read the param on the second path: root/application/1/serviceproviderext/joyn/ux/");
            return map.get(ConfigConstants.ConfigPath.JOYN_UX_PATH + str2);
        } else if (isClientControlJoynMessagingParam(str2)) {
            String str4 = map.get(ConfigConstants.ConfigPath.JOYN_MESSAGING_CHARACTERISTIC_PATH + str2);
            if (str4 != null && !str4.isEmpty()) {
                return str4;
            }
            return map.get(ConfigConstants.ConfigPath.CHAT_CHARACTERISTIC_PATH + str2);
        } else if (ConfigConstants.ConfigTable.CLIENTCONTROL_SERVICE_AVAILABILITY_INFO_EXPIRY.equalsIgnoreCase(str2)) {
            IMSLog.i(LOG_TAG, "readFromSecondMap: read the param on the second path: root/application/1/capdiscovery/");
            return map.get(ConfigConstants.ConfigPath.CAPDISCOVERY_CHARACTERISTIC_PATH + str2);
        } else if (isClientControlMessagingParam(str2)) {
            IMSLog.i(LOG_TAG, "readFromSecondMap: read the param on the second path: root/application/1/messaging/");
            return map.get(ConfigConstants.ConfigPath.MESSAGING_CHARACTERISTIC_PATH + str2);
        } else if (ConfigConstants.ConfigTable.CLIENTCONTROL_FT_MAX1_TO_MANY_RECIPIENTS.equalsIgnoreCase(str2)) {
            IMSLog.i(LOG_TAG, "readFromSecondMap: read the param on the second path: root/application/1/messaging/filetransfer/");
            return map.get(ConfigConstants.ConfigPath.FILETRANSFER_CHARACTERISTIC_PATH + str2);
        } else if (!isEnrichedCallingParam(str2)) {
            return null;
        } else {
            IMSLog.i(LOG_TAG, "readFromSecondMap: read the param on the second path: root/application/4/");
            return map.get(ConfigConstants.ConfigPath.ENRICHED_CALLING_CHARACTERISTIC_PATH + str2);
        }
    }

    private static boolean isRootAppUp20Param(String str) {
        return ConfigConstants.ConfigTable.HOME_NETWORK_DOMAIN_NAME.equalsIgnoreCase(str) || ConfigConstants.ConfigTable.PRIVATE_USER_IDENTITY.equalsIgnoreCase(str) || ConfigConstants.ConfigTable.KEEP_ALIVE_ENABLED.equalsIgnoreCase(str);
    }

    private static boolean isJoynParam(String str) {
        return ConfigConstants.ConfigTable.CAPDISCOVERY_JOYN_MSGCAPVALIDITY.equalsIgnoreCase(str) || ConfigConstants.ConfigTable.CAPDISCOVERY_JOYN_LASTSEENACTIVE.equalsIgnoreCase(str);
    }

    private static boolean isImFtExtParam(String str) {
        return ConfigConstants.ConfigTable.IM_EXT_CB_FT_HTTP_CS_URI.equalsIgnoreCase(str) || ConfigConstants.ConfigTable.IM_EXT_MAX_SIZE_EXTRA_FILE_TR.equalsIgnoreCase(str) || ConfigConstants.ConfigTable.IM_EXT_FT_HTTP_EXTRA_CS_URI.equalsIgnoreCase(str);
    }

    private static boolean isChatParam(String str) {
        return ConfigConstants.ConfigTable.IM_AUT_ACCEPT.equalsIgnoreCase(str) || ConfigConstants.ConfigTable.IM_AUT_ACCEPT_GROUP_CHAT.equalsIgnoreCase(str) || ConfigConstants.ConfigTable.IM_CHAT_REVOKE_TIMER.equalsIgnoreCase(str) || ConfigConstants.ConfigTable.IM_CONF_FCTY_URI.equalsIgnoreCase(str) || "max_adhoc_group_size".equalsIgnoreCase(str) || "MaxSize".equalsIgnoreCase(str) || ConfigConstants.ConfigTable.IM_TIMER_IDLE.equalsIgnoreCase(str);
    }

    private static boolean isFiletransferParam(String str) {
        return ConfigConstants.ConfigTable.IM_FT_AUT_ACCEPT.equalsIgnoreCase(str) || ConfigConstants.ConfigTable.IM_FT_HTTP_CS_PWD.equalsIgnoreCase(str) || ConfigConstants.ConfigTable.IM_FT_HTTP_CS_URI.equalsIgnoreCase(str) || ConfigConstants.ConfigTable.IM_FT_HTTP_DL_URI.equalsIgnoreCase(str) || ConfigConstants.ConfigTable.IM_FT_HTTP_CS_USER.equalsIgnoreCase(str) || ConfigConstants.ConfigTable.IM_FT_HTTP_FALLBACK.equalsIgnoreCase(str) || ConfigConstants.ConfigTable.IM_FT_WARN_SIZE.equalsIgnoreCase(str) || ConfigConstants.ConfigTable.IM_MAX_SIZE_FILE_TR.equalsIgnoreCase(str) || ConfigConstants.ConfigTable.IM_FT_DEFAULT_MECH.equalsIgnoreCase(str);
    }

    private static boolean isStandaloneMsgUp20Param(String str) {
        return ConfigConstants.ConfigTable.CPM_SLM_MAX_MSG_SIZE.equalsIgnoreCase(str) || "MaxSize".equalsIgnoreCase(str) || ConfigConstants.ConfigTable.SLM_SWITCH_OVER_SIZE.equalsIgnoreCase(str);
    }

    private static boolean isCpmParam(String str) {
        return ConfigConstants.ConfigTable.CPM_MESSAGE_STORE_USER_NAME.equalsIgnoreCase(str) || ConfigConstants.ConfigTable.CPM_MESSAGE_STORE_USER_PWD.equalsIgnoreCase(str);
    }

    private static boolean isMessageStoreParam(String str) {
        return "EventRpting".equalsIgnoreCase(str) || ConfigConstants.ConfigTable.CPM_MESSAGE_STORE_AUTH_ARCHIVE.equalsIgnoreCase(str) || "SMSStore".equalsIgnoreCase(str) || "MMSStore".equalsIgnoreCase(str);
    }

    private static boolean isJoynUxParam(String str) {
        return ConfigConstants.ConfigTable.UX_MESSAGING_UX.equalsIgnoreCase(str) || ConfigConstants.ConfigTable.UX_MSG_FB_DEFAULT.equalsIgnoreCase(str);
    }

    private static boolean isClientControlJoynMessagingParam(String str) {
        return ConfigConstants.ConfigTable.CLIENTCONTROL_RECONNECT_GUARD_TIMER.equalsIgnoreCase(str) || ConfigConstants.ConfigTable.CLIENTCONTROL_CFS_TRIGGER.equalsIgnoreCase(str);
    }

    private static boolean isClientControlMessagingParam(String str) {
        return ConfigConstants.ConfigTable.CLIENTCONTROL_ONE_TO_MANY_SELECTED_TECH.equalsIgnoreCase(str) || ConfigConstants.ConfigTable.CLIENTCONTROL_DISPLAY_NOTIFICATION_SWITCH.equalsIgnoreCase(str) || ConfigConstants.ConfigTable.CLIENTCONTROL_MAX1_TO_MANY_RECIPIENTS.equalsIgnoreCase(str);
    }

    private static boolean isEnrichedCallingParam(String str) {
        return ConfigConstants.ConfigTable.SERVICES_COMPOSER_AUTH.equalsIgnoreCase(str) || ConfigConstants.ConfigTable.SERVICES_SHARED_MAP_AUTH.equalsIgnoreCase(str) || ConfigConstants.ConfigTable.SERVICES_SHARED_SKETCH_AUTH.equalsIgnoreCase(str) || ConfigConstants.ConfigTable.SERVICES_POST_CALL_AUTH.equalsIgnoreCase(str) || ConfigConstants.ConfigTable.DATAOFF_CONTENT_SHARE.equalsIgnoreCase(str) || ConfigConstants.ConfigTable.DATAOFF_PRE_AND_POST_CALL.equalsIgnoreCase(str);
    }

    public static List<String> getCapAllowedPrefixes(String str, Map<String, String> map) {
        ArrayList arrayList = new ArrayList();
        String str2 = map.get(str + Integer.toString(1));
        int i = 1;
        while (str2 != null && !str2.isEmpty()) {
            arrayList.add(str2);
            i++;
            str2 = map.get(str + Integer.toString(i));
        }
        return arrayList;
    }

    public static List<String> getPublicUserIdentities(String str, Map<String, String> map) {
        String str2;
        ArrayList arrayList = new ArrayList();
        String lowerCase = str.toLowerCase(Locale.US);
        int i = 0;
        while (true) {
            lowerCase.hashCode();
            char c = 65535;
            switch (lowerCase.hashCode()) {
                case -635263783:
                    if (lowerCase.equals(ConfigConstants.ConfigPath.PUBLIC_USER_IDENTITY_CHARACTERISTIC_PATH)) {
                        c = 0;
                        break;
                    }
                    break;
                case 952202542:
                    if (lowerCase.equals(ConfigConstants.ConfigPath.PUBLIC_USER_IDENTITY_CHARACTERISTIC_UP20_PATH)) {
                        c = 1;
                        break;
                    }
                    break;
                case 1002817916:
                    if (lowerCase.equals(ConfigConstants.ConfigPath.PUBLIC_USER_IDENTITY_CHARACTERISTIC_UP10_PATH)) {
                        c = 2;
                        break;
                    }
                    break;
            }
            switch (c) {
                case 0:
                    str2 = map.get(str + ConfigConstants.ConfigTable.PUBLIC_USER_IDENTITY + "/" + i);
                    break;
                case 1:
                    str2 = map.get(str + "node/" + i + "/" + ConfigConstants.ConfigTable.PUBLIC_USER_IDENTITY);
                    break;
                case 2:
                    str2 = map.get(str + ConfigConstants.ConfigTable.PUBLIC_USER_IDENTITY + (i + 1));
                    break;
                default:
                    str2 = null;
                    break;
            }
            if (TextUtils.isEmpty(str2)) {
                return arrayList;
            }
            arrayList.add(str2);
            i++;
        }
    }

    public static List<String> getLboPcscfAddresses(String str, String str2, Map<String, String> map) {
        String str3;
        ArrayList arrayList = new ArrayList();
        Locale locale = Locale.US;
        String lowerCase = str.toLowerCase(locale);
        String lowerCase2 = str2.toLowerCase(locale);
        if (!lowerCase2.equals("address") && !lowerCase2.equals(ConfigConstants.ConfigTable.LBO_PCSCF_ADDRESS_TYPE)) {
            return arrayList;
        }
        int i = 0;
        while (true) {
            lowerCase.hashCode();
            char c = 65535;
            switch (lowerCase.hashCode()) {
                case -1083842858:
                    if (lowerCase.equals(ConfigConstants.ConfigPath.LBO_PCSCF_ADDRESSES_CHARACTERISTIC_UP20_PATH)) {
                        c = 0;
                        break;
                    }
                    break;
                case 1820524985:
                    if (lowerCase.equals(ConfigConstants.ConfigPath.LBO_PCSCF_ADDRESS_CHARACTERISTIC_PATH)) {
                        c = 1;
                        break;
                    }
                    break;
                case 1894509373:
                    if (lowerCase.equals(ConfigConstants.ConfigPath.LBO_PCSCF_ADDRESSES_CHARACTERISTIC_UP10_PATH)) {
                        c = 2;
                        break;
                    }
                    break;
                case 1916188420:
                    if (lowerCase.equals(ConfigConstants.ConfigPath.LBO_PCSCF_ADDRESS_CHARACTERISTIC_UP20_PATH)) {
                        c = 3;
                        break;
                    }
                    break;
            }
            switch (c) {
                case 0:
                case 2:
                    str3 = map.get(str + lowerCase2 + (i + 1));
                    break;
                case 1:
                    str3 = map.get(str + lowerCase2 + "/" + i);
                    break;
                case 3:
                    str3 = map.get(str + "node/" + i + "/" + lowerCase2);
                    break;
                default:
                    str3 = null;
                    break;
            }
            if (TextUtils.isEmpty(str3)) {
                return arrayList;
            }
            arrayList.add(str3);
            i++;
        }
    }

    public static boolean isUp2NonTransitional(String str, int i) {
        String string = ImsRegistry.getString(i, GlobalSettingsConstants.RCS.UP_PROFILE, "");
        if ("NAGuidelines".equals(str)) {
            Context context = ImsRegistry.getContext();
            if (isImsSingleRegiRequired(context, i) && isGoogDmaPackageInuse(context, i) && isSrUseAcsForRcs(context, i)) {
                String rcsClientConfiguration = SecImsNotifier.getInstance().getRcsClientConfiguration(i, 1);
                String readStringParamWithPath = !TextUtils.isEmpty(rcsClientConfiguration) ? rcsClientConfiguration : readStringParamWithPath(context, ImsUtil.getPathWithPhoneId(ConfigConstants.PATH.INFO_RCS_PROFILE, i));
                IMSLog.i(LOG_TAG, i, String.format(Locale.US, "isUp2NonTransitional: rcc [%s] => [%s]", new Object[]{rcsClientConfiguration, readStringParamWithPath}));
                str = readStringParamWithPath;
            } else if (!string.isEmpty()) {
                str = string;
            }
        }
        if (!ImsProfile.isRcsUp2Profile(str) || ImsProfile.isRcsUpTransitionProfile(string)) {
            return false;
        }
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

    private static boolean isImsSingleRegiRequired(Context context, int i) {
        return readBooleanCarrierConfigValue(context, i, "ims.ims_single_registration_required_bool");
    }

    private static boolean isSrUseAcsForRcs(Context context, int i) {
        return readBooleanCarrierConfigValue(context, i, "use_acs_for_rcs_bool");
    }

    private static boolean isGoogDmaPackageInuse(Context context, int i) {
        String defaultSmsPackage = Telephony.Sms.getDefaultSmsPackage(context);
        if (TextUtils.isEmpty(defaultSmsPackage)) {
            IMSLog.i(LOG_TAG, "default sms app is null");
            return false;
        }
        String string = ImsRegistry.getString(i, GlobalSettingsConstants.RCS.GOOG_MESSAGE_APP_PACKAGE, "");
        boolean equals = TextUtils.equals(defaultSmsPackage, string);
        IMSLog.i(LOG_TAG, "default app: " + defaultSmsPackage + " googlePackage: " + string + "result: " + equals);
        return equals;
    }

    public static ImConstants.ImMsgTech getImMsgTech(ConfigData configData, String str, int i) {
        if (isUp2NonTransitional(str, i)) {
            return ImConstants.ImMsgTech.CPM;
        }
        return configData.readInt(ConfigConstants.ConfigTable.IM_IM_MSG_TECH, 0).intValue() == 0 ? ImConstants.ImMsgTech.SIMPLE_IM : ImConstants.ImMsgTech.CPM;
    }

    public static ImConstants.FtMech getFtDefaultTech(ConfigData configData, String str, int i) {
        if (isUp2NonTransitional(str, i)) {
            return ImConstants.FtMech.HTTP;
        }
        if (DiagnosisConstants.RCSM_KEY_MSRP.equals(configData.readString(ConfigConstants.ConfigTable.IM_FT_DEFAULT_MECH, DiagnosisConstants.RCSM_KEY_MSRP))) {
            return ImConstants.FtMech.MSRP;
        }
        return ImConstants.FtMech.HTTP;
    }
}
