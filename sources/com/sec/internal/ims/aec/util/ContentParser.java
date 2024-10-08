package com.sec.internal.ims.aec.util;

import android.text.TextUtils;
import com.sec.internal.constants.ims.aec.AECNamespace;
import com.sec.internal.constants.ims.config.ConfigConstants;
import com.sec.internal.helper.header.AuthenticationHeaders;
import com.sec.internal.log.AECLog;
import java.io.BufferedReader;
import java.io.StringReader;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.TreeMap;
import org.json.JSONObject;
import org.xmlpull.v1.XmlPullParser;
import org.xmlpull.v1.XmlPullParserFactory;

public class ContentParser {
    private static final String ACCESS_TYPE = "accesstype";
    private static final String APPLICATION = "application";
    private static final String ENTITLEMENT_STATUS = "entitlementstatus";
    private static final String HOME_ROAMING_NW_TYPE = "homeroamingnwtype";
    private static final ArrayList<String> INCREASE_TAG_LIST = new ArrayList<String>() {
        {
            add(ContentParser.APPLICATION);
            add(ContentParser.RAT_VOICE_ENTITLE_INFO_DETAILS);
        }
    };
    private static final String LOG_TAG = "ContentParser";
    private static final String MESSAGE_FOR_INCOMPATIBLE = "messageforincompatible";
    private static final String NETWORK_VOICE_IRAT_CAPABILITY = "networkvoiceiratcapability";
    private static final String PATH_DIVIDER = "/";
    private static final String PATH_ROOT = "root";
    private static final String RAT_VOICE_ENTITLE_INFO_DETAILS = "ratvoiceentitleinfodetails";
    private static final String TAG_CHARACTERISTIC = "characteristic";
    private static final int TAG_CHARACTERISTIC_ATTR_COUNT = 1;
    private static final String TAG_CHARACTERISTIC_ATTR_TYPE = "type";
    private static final String TAG_PARAM = "param";
    private static final String TAG_PARM = "parm";
    private static final int TAG_PARM_ATTR_COUNT = 2;
    private static final String TAG_PARM_ATTR_NAME = "name";
    private static final String TAG_PARM_ATTR_VALUE = "value";
    private static final String TAG_WAPPROVISIONINGDOC = "wap-provisioningdoc";
    private static final int TAG_WAPPROVISIONINGDOC_ATTR_COUNT = 1;
    private static final String VOLTE_4G = "volte/4G";
    private static final String VOLTE_5G = "volte/5G";

    /* JADX WARNING: Code restructure failed: missing block: B:14:0x0018, code lost:
        return false;
     */
    /* Code decompiled incorrectly, please refer to instructions dump. */
    public static synchronized boolean isJSONValid(java.util.List<java.lang.String> r2) {
        /*
            java.lang.Class<com.sec.internal.ims.aec.util.ContentParser> r0 = com.sec.internal.ims.aec.util.ContentParser.class
            monitor-enter(r0)
            if (r2 == 0) goto L_0x0017
            boolean r1 = r2.isEmpty()     // Catch:{ all -> 0x0014 }
            if (r1 == 0) goto L_0x000c
            goto L_0x0017
        L_0x000c:
            java.lang.String r1 = "application/vnd.gsma.eap-relay.v1.0+json"
            boolean r2 = r2.contains(r1)     // Catch:{ all -> 0x0014 }
            monitor-exit(r0)
            return r2
        L_0x0014:
            r2 = move-exception
            monitor-exit(r0)
            throw r2
        L_0x0017:
            monitor-exit(r0)
            r2 = 0
            return r2
        */
        throw new UnsupportedOperationException("Method not decompiled: com.sec.internal.ims.aec.util.ContentParser.isJSONValid(java.util.List):boolean");
    }

    /* JADX WARNING: Code restructure failed: missing block: B:11:0x000f, code lost:
        return false;
     */
    /* JADX WARNING: Exception block dominator not found, dom blocks: [] */
    /* Code decompiled incorrectly, please refer to instructions dump. */
    public static synchronized boolean isJSONValid(java.lang.String r2) {
        /*
            java.lang.Class<com.sec.internal.ims.aec.util.ContentParser> r0 = com.sec.internal.ims.aec.util.ContentParser.class
            monitor-enter(r0)
            org.json.JSONObject r1 = new org.json.JSONObject     // Catch:{ JSONException -> 0x000e, all -> 0x000b }
            r1.<init>(r2)     // Catch:{ JSONException -> 0x000e, all -> 0x000b }
            monitor-exit(r0)
            r2 = 1
            return r2
        L_0x000b:
            r2 = move-exception
            monitor-exit(r0)
            throw r2
        L_0x000e:
            monitor-exit(r0)
            r2 = 0
            return r2
        */
        throw new UnsupportedOperationException("Method not decompiled: com.sec.internal.ims.aec.util.ContentParser.isJSONValid(java.lang.String):boolean");
    }

    /* JADX WARNING: Code restructure failed: missing block: B:11:0x0014, code lost:
        return false;
     */
    /* JADX WARNING: Exception block dominator not found, dom blocks: [] */
    /* Code decompiled incorrectly, please refer to instructions dump. */
    public static synchronized boolean hasEapRelayPacket(java.lang.String r2) {
        /*
            java.lang.Class<com.sec.internal.ims.aec.util.ContentParser> r0 = com.sec.internal.ims.aec.util.ContentParser.class
            monitor-enter(r0)
            org.json.JSONObject r1 = new org.json.JSONObject     // Catch:{ JSONException -> 0x0013, all -> 0x0010 }
            r1.<init>(r2)     // Catch:{ JSONException -> 0x0013, all -> 0x0010 }
            java.lang.String r2 = "eap-relay-packet"
            boolean r2 = r1.has(r2)     // Catch:{ JSONException -> 0x0013, all -> 0x0010 }
            monitor-exit(r0)
            return r2
        L_0x0010:
            r2 = move-exception
            monitor-exit(r0)
            throw r2
        L_0x0013:
            monitor-exit(r0)
            r2 = 0
            return r2
        */
        throw new UnsupportedOperationException("Method not decompiled: com.sec.internal.ims.aec.util.ContentParser.hasEapRelayPacket(java.lang.String):boolean");
    }

    public static synchronized Map<String, String> parseJson(String str) throws Exception {
        Map<String, String> convertMap;
        synchronized (ContentParser.class) {
            TreeMap treeMap = new TreeMap();
            try {
                if (!TextUtils.isEmpty(str)) {
                    JSONObject jSONObject = new JSONObject(str);
                    Iterator<String> keys = jSONObject.keys();
                    while (keys.hasNext()) {
                        String next = keys.next();
                        Object obj = jSONObject.get(next);
                        if (obj instanceof JSONObject) {
                            JSONObject jSONObject2 = (JSONObject) obj;
                            if (jSONObject2.length() > 0) {
                                StringBuilder sb = new StringBuilder();
                                if (next.equalsIgnoreCase("vers")) {
                                    sb.append(ConfigConstants.ConfigPath.VERS_PATH);
                                } else if (next.equalsIgnoreCase("token")) {
                                    sb.append(ConfigConstants.ConfigPath.TOKEN_PATH);
                                } else if (next.equalsIgnoreCase("ap2003")) {
                                    treeMap.put(AECNamespace.Path.APPLICATION_0_APPID, "ap2003");
                                    sb.append(ConfigConstants.ConfigPath.APPLICATION_CHARACTERISTIC_PATH);
                                } else if (next.equalsIgnoreCase("ap2004")) {
                                    treeMap.put(AECNamespace.Path.APPLICATION_1_APPID, "ap2004");
                                    sb.append("root/application/1/");
                                } else if (next.equalsIgnoreCase("ap2005")) {
                                    treeMap.put(AECNamespace.Path.APPLICATION_2_APPID, "ap2005");
                                    sb.append("root/application/2/");
                                }
                                Iterator<String> keys2 = jSONObject2.keys();
                                while (keys2.hasNext()) {
                                    String next2 = keys2.next();
                                    treeMap.put(sb + next2.toLowerCase(Locale.US), String.valueOf(jSONObject2.get(next2)));
                                }
                            }
                        } else {
                            treeMap.put("root/" + next, String.valueOf(obj));
                        }
                    }
                }
                convertMap = convertMap(treeMap);
            } catch (Exception e) {
                throw new Exception("parseJson: " + e.getMessage());
            }
        }
        return convertMap;
    }

    public static synchronized Map<String, String> parseXml(String str) throws Exception {
        Map<String, String> convertMap;
        synchronized (ContentParser.class) {
            TreeMap treeMap = new TreeMap();
            TreeMap treeMap2 = new TreeMap();
            try {
                XmlPullParser newPullParser = XmlPullParserFactory.newInstance().newPullParser();
                newPullParser.setInput(new StringReader(replaceXMLCharacters(str)));
                ArrayList arrayList = new ArrayList();
                TreeMap treeMap3 = new TreeMap();
                arrayList.add(PATH_ROOT);
                for (int eventType = newPullParser.getEventType(); eventType != 1; eventType = newPullParser.next()) {
                    if (eventType == 2) {
                        int attributeCount = newPullParser.getAttributeCount();
                        String name = newPullParser.getName();
                        Locale locale = Locale.US;
                        String lowerCase = name.toLowerCase(locale);
                        if (!lowerCase.equals(TAG_WAPPROVISIONINGDOC) || attributeCount != 1) {
                            if (lowerCase.equals(TAG_CHARACTERISTIC) && attributeCount == 1) {
                                String lowerCase2 = newPullParser.getAttributeName(0).toLowerCase(locale);
                                String lowerCase3 = newPullParser.getAttributeValue(0).toLowerCase(locale);
                                if (lowerCase2.equals("type")) {
                                    arrayList.add(lowerCase3);
                                    if (INCREASE_TAG_LIST.contains(lowerCase3)) {
                                        arrayList.set(arrayList.size() - 1, lowerCase3 + PATH_DIVIDER + increaseTagCount(treeMap3, convertList(arrayList)));
                                    }
                                }
                            } else if ((lowerCase.equals(TAG_PARM) || lowerCase.equals("param")) && attributeCount == 2) {
                                String lowerCase4 = newPullParser.getAttributeName(0).toLowerCase(locale);
                                String lowerCase5 = newPullParser.getAttributeValue(0).toLowerCase(locale);
                                String lowerCase6 = newPullParser.getAttributeName(1).toLowerCase(locale);
                                String attributeValue = newPullParser.getAttributeValue(1);
                                if (lowerCase4.equals("name") && lowerCase6.equals("value")) {
                                    String convertList = convertList(arrayList);
                                    if (convertList.contains(RAT_VOICE_ENTITLE_INFO_DETAILS)) {
                                        if (!treeMap2.containsKey(convertList)) {
                                            treeMap2.put(convertList, new TreeMap());
                                        }
                                        ((Map) treeMap2.get(convertList)).put(lowerCase5, attributeValue);
                                    } else {
                                        arrayList.add(lowerCase5);
                                        treeMap.put(convertList(arrayList), attributeValue);
                                    }
                                }
                            }
                        }
                    } else if (eventType == 3) {
                        if (!convertList(arrayList).contains(RAT_VOICE_ENTITLE_INFO_DETAILS)) {
                            arrayList.remove(arrayList.size() - 1);
                        } else if (newPullParser.getName().toLowerCase(Locale.US).equals(TAG_CHARACTERISTIC)) {
                            arrayList.remove(arrayList.size() - 1);
                        }
                    }
                }
                if (!treeMap2.isEmpty()) {
                    treeMap.putAll(extractMap(treeMap2));
                }
                convertMap = convertMap(treeMap);
            } catch (Exception e) {
                throw new Exception("parseXml: " + e.getMessage());
            }
        }
        return convertMap;
    }

    public static synchronized void debugPrint(int i, Map<String, String> map) {
        synchronized (ContentParser.class) {
            if (!map.isEmpty()) {
                for (Map.Entry next : map.entrySet()) {
                    String str = LOG_TAG;
                    AECLog.s(str, ((String) next.getKey()) + AuthenticationHeaders.HEADER_PRARAM_SPERATOR + ((String) next.getValue()), i);
                }
            }
        }
    }

    private static String convertList(List<String> list) {
        StringBuilder sb = new StringBuilder();
        for (String append : list) {
            sb.append(append);
            sb.append(PATH_DIVIDER);
        }
        sb.deleteCharAt(sb.length() - 1);
        return sb.toString();
    }

    private static int increaseTagCount(Map<String, Integer> map, String str) {
        int intValue = map.containsKey(str) ? map.get(str).intValue() + 1 : 0;
        map.put(str, Integer.valueOf(intValue));
        return intValue;
    }

    private static String replaceXMLCharacters(String str) throws Exception {
        StringBuilder sb = new StringBuilder();
        BufferedReader bufferedReader = new BufferedReader(new StringReader(str));
        while (true) {
            try {
                String readLine = bufferedReader.readLine();
                if (readLine != null) {
                    sb.append(readLine.replaceAll("&", "&amp;"));
                } else {
                    bufferedReader.close();
                    return sb.toString();
                }
            } catch (Exception e) {
                throw new Exception("replaceXMLCharacters: " + e.getMessage());
            }
        }
    }

    private static Map<String, String> extractMap(String str, String str2, String str3, String str4, String str5) {
        TreeMap treeMap = new TreeMap();
        try {
            TreeMap treeMap2 = new TreeMap();
            int parseInt = Integer.parseInt(str4);
            if (parseInt == 1) {
                treeMap2.put(AECNamespace.Path.VOLTE_HOME_ENTITLEMENT_STATUS, str2);
                treeMap2.put(AECNamespace.Path.VOLTE_ROAMING_ENTITLEMENT_STATUS, str2);
                treeMap2.put(AECNamespace.Path.VOLTE_HOME_MESSAGE_FOR_INCOMPATIBLE, str3);
                treeMap2.put(AECNamespace.Path.VOLTE_ROAMING_MESSAGE_FOR_INCOMPATIBLE, str3);
                treeMap2.put(AECNamespace.Path.VOLTE_HOME_NETWORK_VOICE_IRAT_CAPABILITY, str5);
                treeMap2.put(AECNamespace.Path.VOLTE_ROAMING_NETWORK_VOICE_IRAT_CAPABILITY, str5);
            } else if (parseInt == 2) {
                treeMap2.put(AECNamespace.Path.VOLTE_HOME_ENTITLEMENT_STATUS, str2);
                treeMap2.put(AECNamespace.Path.VOLTE_HOME_MESSAGE_FOR_INCOMPATIBLE, str3);
                treeMap2.put(AECNamespace.Path.VOLTE_HOME_NETWORK_VOICE_IRAT_CAPABILITY, str5);
            } else if (parseInt == 3) {
                treeMap2.put(AECNamespace.Path.VOLTE_ROAMING_ENTITLEMENT_STATUS, str2);
                treeMap2.put(AECNamespace.Path.VOLTE_ROAMING_MESSAGE_FOR_INCOMPATIBLE, str3);
                treeMap2.put(AECNamespace.Path.VOLTE_ROAMING_NETWORK_VOICE_IRAT_CAPABILITY, str5);
            }
            for (Map.Entry entry : treeMap2.entrySet()) {
                String str6 = (String) entry.getKey();
                if (1 == Integer.parseInt(str)) {
                    str6 = str6.replace("*", VOLTE_4G);
                } else if (2 == Integer.parseInt(str)) {
                    str6 = str6.replace("*", VOLTE_5G);
                }
                treeMap.put(str6, (String) entry.getValue());
            }
        } catch (NumberFormatException e) {
            String str7 = LOG_TAG;
            AECLog.e(str7, "extractMap: " + e.getMessage());
        }
        return treeMap;
    }

    private static Map<String, String> extractMap(Map<String, Map<String, String>> map) {
        TreeMap treeMap = new TreeMap();
        for (Map.Entry<String, Map<String, String>> value : map.entrySet()) {
            Map map2 = (Map) value.getValue();
            treeMap.putAll(extractMap((String) map2.getOrDefault(ACCESS_TYPE, "0"), (String) map2.getOrDefault(ENTITLEMENT_STATUS, "0"), (String) map2.getOrDefault(MESSAGE_FOR_INCOMPATIBLE, ""), (String) map2.getOrDefault(HOME_ROAMING_NW_TYPE, "0"), (String) map2.getOrDefault(NETWORK_VOICE_IRAT_CAPABILITY, "")));
        }
        return treeMap;
    }

    private static String convertServiceId(String str) {
        if (str.equalsIgnoreCase("ap2003")) {
            return "root/application/volte";
        }
        if (str.equalsIgnoreCase("ap2004")) {
            return "root/application/vowifi";
        }
        return str.equalsIgnoreCase("ap2005") ? "root/application/smsoip" : str;
    }

    private static Map<String, String> convertMap(Map<String, String> map) {
        TreeMap treeMap = new TreeMap();
        String convertServiceId = convertServiceId(map.getOrDefault(AECNamespace.Path.APPLICATION_0_APPID, ""));
        String convertServiceId2 = convertServiceId(map.getOrDefault(AECNamespace.Path.APPLICATION_1_APPID, ""));
        String convertServiceId3 = convertServiceId(map.getOrDefault(AECNamespace.Path.APPLICATION_2_APPID, ""));
        for (Map.Entry next : map.entrySet()) {
            String str = (String) next.getKey();
            if (str.contains(AECNamespace.Path.APPLICATION_0) && !TextUtils.isEmpty(convertServiceId)) {
                str = str.replace(AECNamespace.Path.APPLICATION_0, convertServiceId.toLowerCase(Locale.ROOT));
            } else if (str.contains(AECNamespace.Path.APPLICATION_1) && !TextUtils.isEmpty(convertServiceId2)) {
                str = str.replace(AECNamespace.Path.APPLICATION_1, convertServiceId2.toLowerCase(Locale.ROOT));
            } else if (str.contains(AECNamespace.Path.APPLICATION_2) && !TextUtils.isEmpty(convertServiceId3)) {
                str = str.replace(AECNamespace.Path.APPLICATION_2, convertServiceId3.toLowerCase(Locale.ROOT));
            }
            treeMap.put(str, (String) next.getValue());
        }
        return treeMap;
    }
}
