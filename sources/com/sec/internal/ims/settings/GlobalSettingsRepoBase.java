package com.sec.internal.ims.settings;

import android.content.ContentValues;
import android.content.Context;
import android.content.SharedPreferences;
import android.database.Cursor;
import android.database.MatrixCursor;
import android.os.SemSystemProperties;
import android.telephony.SubscriptionManager;
import android.text.TextUtils;
import android.util.Log;
import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonNull;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import com.google.gson.JsonPrimitive;
import com.google.gson.stream.JsonReader;
import com.sec.imsservice.R;
import com.sec.internal.constants.Mno;
import com.sec.internal.constants.ims.DiagnosisConstants;
import com.sec.internal.constants.ims.ImsConstants;
import com.sec.internal.constants.ims.settings.GlobalSettingsConstants;
import com.sec.internal.helper.CollectionUtils;
import com.sec.internal.helper.DmConfigHelper;
import com.sec.internal.helper.ImsSharedPrefHelper;
import com.sec.internal.helper.JsonUtil;
import com.sec.internal.helper.OmcCode;
import com.sec.internal.helper.SimUtil;
import com.sec.internal.helper.SimpleEventLog;
import com.sec.internal.helper.header.AuthenticationHeaders;
import com.sec.internal.helper.os.DeviceUtil;
import com.sec.internal.ims.util.CarrierConfigUtil;
import com.sec.internal.ims.util.CscParser;
import com.sec.internal.imscr.LogClass;
import com.sec.internal.interfaces.ims.core.ISimManager;
import com.sec.internal.log.IMSLog;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Locale;
import java.util.Map;

public class GlobalSettingsRepoBase extends GlobalSettingsRepo {
    private final String LOG_TAG;
    protected ImsAutoUpdate mAutoUpdate;
    protected SimpleEventLog mEventLog;
    protected ImsSimMobilityUpdate mImsSimMobilityUpdate = ImsSimMobilityUpdate.getInstance(this.mContext);

    /* access modifiers changed from: protected */
    public void cleanUp() {
    }

    /* access modifiers changed from: protected */
    public boolean isRcsUserSettingValueAllowed(int i) {
        return i == -1 || i == 0 || i == 1 || i == 2;
    }

    public GlobalSettingsRepoBase(Context context, int i) {
        String simpleName = GlobalSettingsRepoBase.class.getSimpleName();
        this.LOG_TAG = simpleName;
        this.mContext = context;
        this.mPhoneId = i;
        this.mEventLog = new SimpleEventLog(context, i, simpleName, 200);
        this.mAutoUpdate = ImsAutoUpdate.getInstance(this.mContext, i);
    }

    public void update(ContentValues contentValues) {
        synchronized (this.mLock) {
            save(contentValues);
        }
    }

    /* access modifiers changed from: protected */
    public void save(JsonObject jsonObject) {
        SharedPreferences.Editor edit = ImsSharedPrefHelper.getSharedPref(this.mPhoneId, this.mContext, ImsSharedPrefHelper.GLOBAL_SETTINGS, 0, false).edit();
        for (Map.Entry entry : jsonObject.entrySet()) {
            String str = (String) entry.getKey();
            JsonElement jsonElement = (JsonElement) entry.getValue();
            String str2 = this.LOG_TAG;
            StringBuilder sb = new StringBuilder();
            sb.append("save : ");
            sb.append(str);
            sb.append(AuthenticationHeaders.HEADER_PRARAM_SPERATOR);
            sb.append(jsonElement != null ? jsonElement.toString() : "null");
            Log.d(str2, sb.toString());
            if (jsonElement != null) {
                if (jsonElement.isJsonArray() || jsonElement.isJsonObject()) {
                    edit.putString(str, jsonElement.toString());
                } else {
                    JsonPrimitive asJsonPrimitive = jsonElement.getAsJsonPrimitive();
                    if (!asJsonPrimitive.isBoolean()) {
                        edit.putString(str, jsonElement.getAsString());
                    } else if (asJsonPrimitive.getAsBoolean()) {
                        edit.putString(str, "1");
                    } else {
                        edit.putString(str, "0");
                    }
                }
            }
        }
        edit.apply();
    }

    /* access modifiers changed from: protected */
    public void save(ContentValues contentValues) {
        SharedPreferences.Editor edit = ImsSharedPrefHelper.getSharedPref(this.mPhoneId, this.mContext, ImsSharedPrefHelper.GLOBAL_SETTINGS, 0, false).edit();
        for (Map.Entry next : contentValues.valueSet()) {
            String str = this.LOG_TAG;
            StringBuilder sb = new StringBuilder();
            sb.append("   ");
            sb.append((String) next.getKey());
            sb.append(AuthenticationHeaders.HEADER_PRARAM_SPERATOR);
            sb.append(next.getValue() != null ? next.getValue().toString() : "null");
            Log.d(str, sb.toString());
            if (next.getValue() != null) {
                edit.putString((String) next.getKey(), next.getValue().toString());
            }
        }
        edit.apply();
    }

    /* JADX WARNING: Can't wrap try/catch for region: R(8:4|(1:6)|7|8|9|10|18|11) */
    /* JADX WARNING: Code restructure failed: missing block: B:12:0x004f, code lost:
        r10 = move-exception;
     */
    /* JADX WARNING: Code restructure failed: missing block: B:13:0x0050, code lost:
        r10.printStackTrace();
     */
    /* JADX WARNING: Code restructure failed: missing block: B:14:0x005a, code lost:
        throw new java.lang.IllegalArgumentException("Boolean type is not supported in globalSettings");
     */
    /* JADX WARNING: Failed to process nested try/catch */
    /* JADX WARNING: Missing exception handler attribute for start block: B:9:0x0041 */
    /* Code decompiled incorrectly, please refer to instructions dump. */
    private java.util.Map<java.lang.String, java.lang.Object> readSettings(java.lang.String r11, java.lang.String[] r12) {
        /*
            r10 = this;
            java.util.HashMap r0 = new java.util.HashMap
            r0.<init>()
            int r1 = r10.mPhoneId
            android.content.Context r2 = r10.mContext
            r3 = 0
            android.content.SharedPreferences r1 = com.sec.internal.helper.ImsSharedPrefHelper.getSharedPref(r1, r2, r11, r3, r3)
            if (r12 == 0) goto L_0x005b
            int r2 = r12.length
            r4 = r3
        L_0x0012:
            if (r4 >= r2) goto L_0x0062
            r5 = r12[r4]
            boolean r6 = r1.contains(r5)
            r7 = 0
            if (r6 != 0) goto L_0x0039
            java.lang.String r6 = r10.LOG_TAG
            java.lang.StringBuilder r8 = new java.lang.StringBuilder
            r8.<init>()
            r8.append(r11)
            java.lang.String r9 = " No matched key : "
            r8.append(r9)
            r8.append(r5)
            java.lang.String r8 = r8.toString()
            android.util.Log.e(r6, r8)
            r0.put(r5, r7)
        L_0x0039:
            java.lang.String r6 = r1.getString(r5, r7)     // Catch:{ ClassCastException -> 0x0041 }
            r0.put(r5, r6)     // Catch:{ ClassCastException -> 0x0041 }
            goto L_0x004c
        L_0x0041:
            int r6 = r1.getInt(r5, r3)     // Catch:{ ClassCastException -> 0x004f }
            java.lang.Integer r6 = java.lang.Integer.valueOf(r6)     // Catch:{ ClassCastException -> 0x004f }
            r0.put(r5, r6)     // Catch:{ ClassCastException -> 0x004f }
        L_0x004c:
            int r4 = r4 + 1
            goto L_0x0012
        L_0x004f:
            r10 = move-exception
            r10.printStackTrace()
            java.lang.IllegalArgumentException r10 = new java.lang.IllegalArgumentException
            java.lang.String r11 = "Boolean type is not supported in globalSettings"
            r10.<init>(r11)
            throw r10
        L_0x005b:
            java.util.Map r10 = r1.getAll()
            r0.putAll(r10)
        L_0x0062:
            return r0
        */
        throw new UnsupportedOperationException("Method not decompiled: com.sec.internal.ims.settings.GlobalSettingsRepoBase.readSettings(java.lang.String, java.lang.String[]):java.util.Map");
    }

    public Cursor query(String[] strArr, String str, String[] strArr2) {
        if (!isLoaded()) {
            Log.e(this.LOG_TAG, "query: globalsettings not loaded. loading now.");
            load();
        }
        HashMap hashMap = new HashMap(readSettings(ImsSharedPrefHelper.GLOBAL_SETTINGS, strArr));
        if (getGlobalGcEnabled()) {
            for (Map.Entry next : readSettings(ImsSharedPrefHelper.GLOBAL_GC_SETTINGS, strArr).entrySet()) {
                if (next.getValue() != null) {
                    if (TextUtils.equals((CharSequence) next.getKey(), GlobalSettingsConstants.Registration.BLOCK_REGI_ON_INVALID_ISIM) || TextUtils.equals((CharSequence) next.getKey(), GlobalSettingsConstants.Registration.VOICE_DOMAIN_PREF_EUTRAN) || TextUtils.equals((CharSequence) next.getKey(), GlobalSettingsConstants.Registration.SHOW_REGI_INFO_IN_SEC_SETTINGS) || TextUtils.equals((CharSequence) next.getKey(), "mnoname")) {
                        Log.i(this.LOG_TAG, "query: Don't override some values");
                    } else {
                        hashMap.put((String) next.getKey(), next.getValue());
                    }
                }
            }
        }
        MatrixCursor matrixCursor = new MatrixCursor((String[]) hashMap.keySet().toArray(new String[0]));
        matrixCursor.addRow(hashMap.values());
        return matrixCursor;
    }

    public void load() {
        synchronized (this.mLock) {
            if (!isLoaded()) {
                String nWCode = OmcCode.getNWCode(this.mPhoneId);
                Mno fromSalesCode = Mno.fromSalesCode(nWCode);
                String str = this.LOG_TAG;
                Log.d(str, "load: initial with OMCNW_CODE(" + nWCode + ") Mno=" + fromSalesCode.getName());
                loadGlobalSettingsFromJson(SimUtil.isSoftphoneEnabled(), fromSalesCode.getName(), 0, new ContentValues());
            }
        }
    }

    /* access modifiers changed from: protected */
    public void loadGlobalGcSettings(boolean z) {
        JsonReader jsonReader;
        String str = this.LOG_TAG;
        int i = this.mPhoneId;
        IMSLog.d(str, i, "loadGlobalGcSettings isGlobalGcEnabled=" + z);
        SharedPreferences.Editor edit = ImsSharedPrefHelper.getSharedPref(this.mPhoneId, this.mContext, ImsSharedPrefHelper.GLOBAL_GC_SETTINGS, 0, false).edit();
        edit.clear();
        if (z) {
            IMSLog.d(this.LOG_TAG, this.mPhoneId, " getResources : globalsettings.json");
            try {
                InputStream openRawResource = this.mContext.getResources().openRawResource(R.raw.globalsettings);
                try {
                    jsonReader = new JsonReader(new BufferedReader(new InputStreamReader(openRawResource)));
                    JsonElement parse = new JsonParser().parse(jsonReader);
                    jsonReader.close();
                    if (openRawResource != null) {
                        openRawResource.close();
                    }
                    JsonArray asJsonArray = parse.getAsJsonObject().getAsJsonArray(ImsAutoUpdate.TAG_GLOBALSETTING);
                    if (!JsonUtil.isValidJsonElement(asJsonArray)) {
                        IMSLog.e(this.LOG_TAG, this.mPhoneId, "load: parse failed.");
                        return;
                    }
                    JsonElement jsonElement = JsonNull.INSTANCE;
                    Iterator it = asJsonArray.iterator();
                    while (true) {
                        if (!it.hasNext()) {
                            break;
                        }
                        JsonElement jsonElement2 = (JsonElement) it.next();
                        JsonElement asJsonObject = jsonElement2.getAsJsonObject();
                        if (jsonElement2.getAsJsonObject().get("mnoname").getAsString().equalsIgnoreCase("GoogleGC_ALL")) {
                            IMSLog.d(this.LOG_TAG, this.mPhoneId, "loadGlobalGcSettings GoogleGC_ALL found");
                            jsonElement = asJsonObject;
                            break;
                        }
                    }
                    if (jsonElement.isJsonNull()) {
                        IMSLog.i(this.LOG_TAG, this.mPhoneId, "loadGlobalGcSettings GoogleGC_ALL is not exist");
                        return;
                    }
                    ImsAutoUpdate imsAutoUpdate = this.mAutoUpdate;
                    if (imsAutoUpdate != null) {
                        jsonElement = imsAutoUpdate.getUpdatedGlobalSetting(jsonElement);
                    }
                    for (Map.Entry entry : jsonElement.getAsJsonObject().entrySet()) {
                        String str2 = (String) entry.getKey();
                        JsonElement jsonElement3 = (JsonElement) entry.getValue();
                        String str3 = this.LOG_TAG;
                        Log.d(str3, "save : " + str2 + AuthenticationHeaders.HEADER_PRARAM_SPERATOR + jsonElement3);
                        if (jsonElement3 != null) {
                            if (jsonElement3.isJsonArray()) {
                                edit.putString(str2, jsonElement3.toString());
                            } else {
                                JsonPrimitive asJsonPrimitive = jsonElement3.getAsJsonPrimitive();
                                if (!asJsonPrimitive.isBoolean()) {
                                    edit.putString(str2, jsonElement3.getAsString());
                                } else if (asJsonPrimitive.getAsBoolean()) {
                                    edit.putString(str2, "1");
                                } else {
                                    edit.putString(str2, "0");
                                }
                            }
                        }
                    }
                } catch (Throwable th) {
                    if (openRawResource != null) {
                        openRawResource.close();
                    }
                    throw th;
                }
            } catch (IOException e) {
                e.printStackTrace();
                return;
            } catch (Throwable th2) {
                th.addSuppressed(th2);
            }
        }
        edit.apply();
        return;
        throw th;
    }

    public void loadByDynamicConfig() {
        synchronized (this.mLock) {
            ContentValues contentValues = this.mMnoinfo;
            if (contentValues != null) {
                Boolean asBoolean = contentValues.getAsBoolean(ISimManager.KEY_HAS_SIM);
                if (asBoolean == null) {
                    asBoolean = Boolean.FALSE;
                }
                Boolean asBoolean2 = this.mMnoinfo.getAsBoolean(ISimManager.KEY_GLOBALGC_ENABLED);
                if (asBoolean2 == null) {
                    asBoolean2 = Boolean.FALSE;
                }
                String asString = this.mMnoinfo.getAsString("mnoname");
                String asString2 = this.mMnoinfo.getAsString(ISimManager.KEY_MVNO_NAME);
                Integer asInteger = this.mMnoinfo.getAsInteger(ISimManager.KEY_IMSSWITCH_TYPE);
                if (asInteger == null) {
                    asInteger = 0;
                }
                loadGlobalSettingsFromJson(asBoolean.booleanValue(), asString, asString2, asInteger.intValue(), this.mMnoinfo);
                loadGlobalGcSettings(asBoolean2.booleanValue());
            }
        }
    }

    public boolean isLoaded() {
        return ImsSharedPrefHelper.getBoolean(this.mPhoneId, this.mContext, ImsSharedPrefHelper.GLOBAL_SETTINGS, "loaded", false);
    }

    public void loadGlobalSettingsFromJson(boolean z, String str, int i, ContentValues contentValues) {
        loadGlobalSettingsFromJson(z, str, "", i, contentValues);
    }

    /* access modifiers changed from: protected */
    public void loadGlobalSettingsFromJson(boolean z, String str, String str2, int i, ContentValues contentValues) {
        boolean z2;
        String str3;
        JsonElement jsonElement;
        JsonElement jsonElement2;
        ContentValues contentValues2;
        boolean z3;
        JsonElement asJsonObject;
        boolean z4 = z;
        String str4 = str;
        String str5 = str2;
        int i2 = i;
        ContentValues contentValues3 = contentValues;
        IMSLog.d(this.LOG_TAG, this.mPhoneId, "loadGlobalSettings: mnoname=" + str4 + ",  mvnoname=" + str5);
        if (str4 == null || str.isEmpty()) {
            IMSLog.e(this.LOG_TAG, this.mPhoneId, "load: globalSettings is not identified.");
            return;
        }
        Mno fromName = Mno.fromName(str);
        if (!TextUtils.isEmpty(str2)) {
            str3 = str4 + Mno.MVNO_DELIMITER + str5;
            z2 = true;
        } else {
            str3 = str4;
            z2 = false;
        }
        InputStream inputStream = null;
        try {
            inputStream = this.mContext.getResources().openRawResource(R.raw.globalsettings);
            JsonParser jsonParser = new JsonParser();
            JsonReader jsonReader = new JsonReader(new BufferedReader(new InputStreamReader(inputStream)));
            JsonElement parse = jsonParser.parse(jsonReader);
            jsonReader.close();
            if (inputStream != null) {
                try {
                    inputStream.close();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
            JsonObject asJsonObject2 = parse.getAsJsonObject();
            JsonElement jsonElement3 = asJsonObject2.get(ImsAutoUpdate.TAG_GLOBALSETTINGS_DEFAULT);
            if (jsonElement3.isJsonNull()) {
                IMSLog.e(this.LOG_TAG, this.mPhoneId, "load: No default setting.");
                return;
            }
            JsonElement jsonElement4 = asJsonObject2.get(ImsAutoUpdate.TAG_GLOBALSETTINGS_NOHIT);
            ImsAutoUpdate imsAutoUpdate = this.mAutoUpdate;
            if (imsAutoUpdate != null) {
                jsonElement4 = imsAutoUpdate.applyNohitSettingUpdate(jsonElement4);
                jsonElement3 = this.mAutoUpdate.applyDefaultSettingUpdate(jsonElement3);
            }
            JsonElement jsonElement5 = JsonNull.INSTANCE;
            if (fromName != Mno.DEFAULT) {
                JsonArray asJsonArray = asJsonObject2.getAsJsonArray(ImsAutoUpdate.TAG_GLOBALSETTING);
                if (!JsonUtil.isValidJsonElement(asJsonArray)) {
                    IMSLog.e(this.LOG_TAG, this.mPhoneId, "load: parse failed.");
                    return;
                }
                Iterator it = asJsonArray.iterator();
                JsonElement jsonElement6 = jsonElement5;
                while (true) {
                    if (!it.hasNext()) {
                        break;
                    }
                    JsonElement jsonElement7 = (JsonElement) it.next();
                    asJsonObject = jsonElement7.getAsJsonObject();
                    String asString = jsonElement7.getAsJsonObject().get("mnoname").getAsString();
                    if (z2) {
                        if (asString.equalsIgnoreCase(str3)) {
                            IMSLog.d(this.LOG_TAG, this.mPhoneId, "loadGlobalSettings - mvnoname on json:" + asString + " found");
                            break;
                        }
                        if (asString.equalsIgnoreCase(str4)) {
                            jsonElement6 = asJsonObject;
                        }
                        boolean z5 = z;
                        ContentValues contentValues4 = contentValues;
                    } else if (asString.equalsIgnoreCase(str4)) {
                        IMSLog.d(this.LOG_TAG, this.mPhoneId, "loadGlobalSettings - mnoname on json:" + asString + " found");
                        break;
                    } else {
                        boolean z52 = z;
                        ContentValues contentValues42 = contentValues;
                    }
                }
                jsonElement5 = asJsonObject;
                if (jsonElement5.isJsonNull()) {
                    JsonElement jsonObject = new JsonObject();
                    jsonObject.addProperty("mnoname", str3);
                    ImsAutoUpdate imsAutoUpdate2 = this.mAutoUpdate;
                    if (imsAutoUpdate2 != null) {
                        jsonObject = imsAutoUpdate2.getUpdatedGlobalSetting(jsonObject);
                    }
                    if (jsonObject.getAsJsonObject().size() <= 1) {
                        IMSLog.d(this.LOG_TAG, this.mPhoneId, "loadGlobalSettings - not matched");
                        if (!z2 || jsonElement6.isJsonNull()) {
                            IMSLog.e(this.LOG_TAG, this.mPhoneId, "load: No matched setting load default setting");
                        } else {
                            IMSLog.d(this.LOG_TAG, this.mPhoneId, "loadGlobalSettings - primary mnoname on json:" + str4 + " found");
                            jsonElement = i2 == 3 ? this.mImsSimMobilityUpdate.overrideGlobalSettingsForSimMobilityUpdateOnDemand(jsonElement6, this.mPhoneId) : jsonElement6;
                            ImsAutoUpdate imsAutoUpdate3 = this.mAutoUpdate;
                            if (imsAutoUpdate3 != null) {
                                jsonElement = imsAutoUpdate3.getUpdatedGlobalSetting(jsonElement);
                            }
                        }
                    } else {
                        jsonElement = jsonObject;
                    }
                } else {
                    if (i2 == 3) {
                        jsonElement5 = this.mImsSimMobilityUpdate.overrideGlobalSettingsForSimMobilityUpdateOnDemand(jsonElement5, this.mPhoneId);
                    }
                    ImsAutoUpdate imsAutoUpdate4 = this.mAutoUpdate;
                    jsonElement = imsAutoUpdate4 != null ? imsAutoUpdate4.getUpdatedGlobalSetting(jsonElement5) : jsonElement5;
                }
                jsonElement2 = JsonUtil.merge(jsonElement3, jsonElement);
            } else {
                jsonElement2 = JsonUtil.merge(jsonElement3, jsonElement);
            }
            JsonObject asJsonObject3 = jsonElement2.getAsJsonObject();
            if ("XAS".equals(OmcCode.getNWCode(this.mPhoneId))) {
                overwriteXasGlobalSettings(asJsonObject3);
            }
            save(asJsonObject3);
            if (fromName != Mno.DEFAULT) {
                z3 = z;
                contentValues2 = contentValues;
                if (z3) {
                    setInitialSettings(asJsonObject3, contentValues2);
                }
            } else {
                z3 = z;
                contentValues2 = contentValues;
            }
            SharedPreferences.Editor edit = ImsSharedPrefHelper.getSharedPref(this.mPhoneId, this.mContext, ImsSharedPrefHelper.GLOBAL_SETTINGS, 0, false).edit();
            edit.putBoolean("loaded", true);
            edit.putString("nwcode", OmcCode.getNWCode(this.mPhoneId));
            edit.putString("mnoname", str4);
            edit.putInt("cscimssettingtype", i2);
            edit.putBoolean(ISimManager.KEY_HAS_SIM, z3);
            edit.putBoolean("gcfmode", DeviceUtil.getGcfMode());
            edit.putString("buildinfo", saveBuildInfo());
            String asString2 = contentValues2.getAsString("imsi");
            if (!TextUtils.isEmpty(asString2)) {
                edit.putString("imsi", asString2);
            }
            edit.apply();
            CarrierConfigUtil.overrideConfigFromGlobalSettings(this.mContext, this.mPhoneId, asJsonObject3);
        } catch (IOException e2) {
            e2.printStackTrace();
            if (inputStream != null) {
                try {
                    inputStream.close();
                } catch (IOException e3) {
                    e3.printStackTrace();
                }
            }
        } catch (Throwable th) {
            Throwable th2 = th;
            if (inputStream != null) {
                try {
                    inputStream.close();
                } catch (IOException e4) {
                    e4.printStackTrace();
                }
            }
            throw th2;
        }
    }

    /* access modifiers changed from: protected */
    public void overwriteXasGlobalSettings(JsonObject jsonObject) {
        jsonObject.addProperty(GlobalSettingsConstants.Registration.VOICE_DOMAIN_PREF_EUTRAN, DiagnosisConstants.RCSM_ORST_REGI);
        jsonObject.addProperty(GlobalSettingsConstants.Call.EMERGENCY_CALL_DOMAIN, "PS");
        jsonObject.addProperty(GlobalSettingsConstants.SS.DOMAIN, "ps_only_psregied");
    }

    /* access modifiers changed from: protected */
    public void setInitialSettings(JsonObject jsonObject, ContentValues contentValues) {
        String str;
        String str2 = "1";
        if (ImsConstants.SystemSettings.getVoiceCallType(this.mContext, -1, this.mPhoneId) == -1) {
            boolean z = !jsonObject.get(GlobalSettingsConstants.Registration.VOLTE_DOMESTIC_DEFAULT_ENABLED).getAsBoolean();
            ImsConstants.SystemSettings.setVoiceCallType(this.mContext, z ? 1 : 0, this.mPhoneId);
            if (!z) {
                str = str2;
            } else {
                str = "0";
            }
            int subId = SimUtil.getSubId(this.mPhoneId);
            if (subId != -1) {
                SubscriptionManager.setSubscriptionProperty(subId, "volte_vt_enabled", str);
            }
            this.mEventLog.logAndAdd(this.mPhoneId, "Set voicecall_type to [" + z + "] from global settings as initial");
            IMSLog.c(LogClass.GLOBAL_INIT_VOICE_DB, this.mPhoneId + ",INITIAL:" + z + ", SIMINFO:" + str);
        }
        if (ImsConstants.SystemSettings.getVideoCallType(this.mContext, -1, this.mPhoneId) == -1) {
            boolean z2 = !jsonObject.get(GlobalSettingsConstants.Registration.VIDEO_DEFAULT_ENABLED).getAsBoolean();
            ImsConstants.SystemSettings.setVideoCallType(this.mContext, z2 ? 1 : 0, this.mPhoneId);
            if (z2) {
                str2 = "0";
            }
            int subId2 = SimUtil.getSubId(this.mPhoneId);
            if (subId2 != -1) {
                SubscriptionManager.setSubscriptionProperty(subId2, "vt_ims_enabled", str2);
            }
            this.mEventLog.logAndAdd(this.mPhoneId, "Set videocall_type to [" + z2 + "] from global settings as initial");
            IMSLog.c(LogClass.GLOBAL_INIT_VIDEO_DB, this.mPhoneId + ",INITIAL:" + z2 + ", SIMINFO:" + str2);
        }
        boolean booleanValue = CollectionUtils.getBooleanValue(contentValues, ISimManager.KEY_GLOBALGC_ENABLED, false);
        if (ImsConstants.SystemSettings.getRcsUserSetting(this.mContext, -1, this.mPhoneId) == -1 && !booleanValue) {
            int asInt = jsonObject.get(GlobalSettingsConstants.RCS.RCS_DEFAULT_ENABLED).getAsInt();
            ImsConstants.SystemSettings.setRcsUserSetting(this.mContext, asInt, this.mPhoneId);
            this.mEventLog.logAndAdd(this.mPhoneId, "Set rcs_user_setting to [" + asInt + "] from global settings as initial");
            IMSLog.c(LogClass.GLOBAL_INIT_RCS_DB, this.mPhoneId + ",SET INITIAL RCS SETTING:" + asInt);
        }
    }

    /* access modifiers changed from: protected */
    public void logMnoInfo(ContentValues contentValues) {
        ContentValues contentValues2 = new ContentValues(contentValues);
        if (!TextUtils.isEmpty(contentValues2.getAsString("imsi"))) {
            contentValues2.remove("imsi");
        }
        SimpleEventLog simpleEventLog = this.mEventLog;
        simpleEventLog.logAndAdd("simSlot[" + this.mPhoneId + "] updateMno: mnoInfo:" + contentValues2);
    }

    public boolean updateMno(ContentValues contentValues) {
        boolean booleanValue;
        String asString;
        String stringValue;
        int intValue;
        String asString2;
        int i;
        boolean z;
        ContentValues contentValues2 = contentValues;
        synchronized (this.mLock) {
            booleanValue = CollectionUtils.getBooleanValue(contentValues2, ISimManager.KEY_HAS_SIM, false);
            asString = contentValues2.getAsString("mnoname");
            stringValue = CollectionUtils.getStringValue(contentValues2, ISimManager.KEY_MVNO_NAME, "");
            intValue = CollectionUtils.getIntValue(contentValues2, ISimManager.KEY_IMSSWITCH_TYPE, 0);
            asString2 = contentValues2.getAsString("imsi");
        }
        SimpleEventLog simpleEventLog = this.mEventLog;
        simpleEventLog.logAndAdd("simSlot[" + this.mPhoneId + "] updateMno: hasSIM:" + booleanValue + ", imsSwitchType:" + intValue);
        logMnoInfo(contentValues);
        boolean prevGcEnabled = getPrevGcEnabled();
        boolean booleanValue2 = CollectionUtils.getBooleanValue(contentValues2, ISimManager.KEY_GLOBALGC_ENABLED, false);
        boolean z2 = prevGcEnabled != booleanValue2;
        int readRcsDefaultEnabled = readRcsDefaultEnabled(prevGcEnabled);
        if (!z2 || !booleanValue) {
            i = readRcsDefaultEnabled;
        } else {
            String str = this.LOG_TAG;
            int i2 = this.mPhoneId;
            IMSLog.i(str, i2, "updateMno: prevGcEnabled: " + prevGcEnabled + ", newGcEnabled: " + booleanValue2);
            i = readRcsDefaultEnabled;
            setSettingsFromSp(false, -1, false, -1, true, readRcsDefaultEnabled(booleanValue2));
            setIsGcEnabledChange(z2);
        }
        if (booleanValue) {
            setPrevGcEnabled(booleanValue2);
        }
        Mno fromName = Mno.fromName(asString);
        SharedPreferences sharedPref = ImsSharedPrefHelper.getSharedPref(this.mPhoneId, this.mContext, ImsSharedPrefHelper.GLOBAL_SETTINGS, 0, false);
        String previousImsi = getPreviousImsi(sharedPref);
        int imsUserSetting = DmConfigHelper.getImsUserSetting(this.mContext, ImsConstants.SystemSettings.RCS_USER_SETTING1.getName(), this.mPhoneId);
        int imsUserSetting2 = DmConfigHelper.getImsUserSetting(this.mContext, ImsConstants.SystemSettings.VOLTE_SLOT1.getName(), this.mPhoneId);
        int imsUserSetting3 = DmConfigHelper.getImsUserSetting(this.mContext, ImsConstants.SystemSettings.VILTE_SLOT1.getName(), this.mPhoneId);
        boolean z3 = !TextUtils.isEmpty(asString2) && !TextUtils.equals(asString2, previousImsi);
        this.mMnoinfo = contentValues2;
        int preUpdateSystemSettings = preUpdateSystemSettings(fromName, imsUserSetting2, z3, booleanValue);
        if (!updateRequires(contentValues)) {
            SimpleEventLog simpleEventLog2 = this.mEventLog;
            simpleEventLog2.logAndAdd("simSlot[" + this.mPhoneId + "] updateMno: update not requires");
            initRcsUserSetting(imsUserSetting, i);
            if (!z3) {
                return false;
            }
            SharedPreferences.Editor edit = sharedPref.edit();
            edit.putString("imsi", asString2);
            SimpleEventLog simpleEventLog3 = this.mEventLog;
            simpleEventLog3.logAndAdd("simSlot[" + this.mPhoneId + "] imsi changed:" + IMSLog.checker(previousImsi) + " --> " + IMSLog.checker(asString2));
            edit.apply();
            return false;
        }
        SimpleEventLog simpleEventLog4 = this.mEventLog;
        simpleEventLog4.logAndAdd("simSlot[" + this.mPhoneId + "] updateMno: update requires");
        String previousMno = getPreviousMno(sharedPref);
        int readVolteDefaultEnabled = readVolteDefaultEnabled();
        boolean z4 = sharedPref.getBoolean(ISimManager.KEY_GLOBALGC_ENABLED, false);
        reset();
        SharedPreferences.Editor edit2 = sharedPref.edit();
        edit2.putBoolean(ISimManager.KEY_GLOBALGC_ENABLED, z4);
        edit2.apply();
        String str2 = this.LOG_TAG;
        int i3 = this.mPhoneId;
        StringBuilder sb = new StringBuilder();
        int i4 = readVolteDefaultEnabled;
        sb.append("updateMno: [");
        sb.append(previousMno);
        sb.append("] => [");
        sb.append(asString);
        sb.append("]");
        IMSLog.d(str2, i3, sb.toString());
        int i5 = i4;
        int i6 = i;
        String str3 = previousMno;
        int i7 = imsUserSetting;
        updateSystemSettings(fromName, contentValues, asString, str3, preUpdateSystemSettings, imsUserSetting3);
        synchronized (this.mLock) {
            loadGlobalSettingsFromJson(booleanValue, asString, stringValue, intValue, contentValues);
            if (this.mVersionUpdated) {
                loadGlobalGcSettings(booleanValue2);
            }
        }
        int readRcsDefaultEnabled2 = readRcsDefaultEnabled(booleanValue2);
        int readVolteDefaultEnabled2 = readVolteDefaultEnabled();
        String requiredForceVolteDefaultEnabled = requiredForceVolteDefaultEnabled();
        if (this.mVersionUpdated) {
            if (i6 != readRcsDefaultEnabled2 && booleanValue) {
                SimpleEventLog simpleEventLog5 = this.mEventLog;
                simpleEventLog5.logAndAdd("updateMno : rcs_default_enabled: [" + i6 + "] => [" + readRcsDefaultEnabled2 + "]");
                setSettingsFromSp(false, -1, false, -1, true, readRcsDefaultEnabled2);
            }
        } else if (fromName != Mno.DEFAULT && booleanValue) {
            initRcsUserSetting(i7, readRcsDefaultEnabled2);
        }
        if (!needToCheckResetSetting()) {
            return true;
        }
        int i8 = i4;
        if (needResetVolteAsDefault(i8, readVolteDefaultEnabled2, asString, requiredForceVolteDefaultEnabled)) {
            SimpleEventLog simpleEventLog6 = this.mEventLog;
            simpleEventLog6.logAndAdd("updateMno : volte_domestic_default_enabled: [" + i8 + "] => [" + readVolteDefaultEnabled2 + "]");
            z = true;
            setSettingsFromSp(true, readVolteDefaultEnabled2 == 1 ? 0 : 1, false, -1, false, -1);
        } else {
            z = true;
        }
        initNeedToCheckResetSetting();
        return z;
    }

    /* access modifiers changed from: protected */
    public boolean needResetVolteAsDefault(int i, int i2, String str, String str2) {
        String upperCase = str2.toUpperCase(Locale.US);
        if (TextUtils.equals(upperCase, "ALWAYS")) {
            return true;
        }
        if (!TextUtils.equals(upperCase, "ONETIME") || !isNotFinishResetVoiceCallType(this.mPhoneId, str)) {
            return false;
        }
        finishResetVoiceCallType(this.mPhoneId, str);
        return true;
    }

    private void finishResetVoiceCallType(int i, String str) {
        ImsSharedPrefHelper.save(i, this.mContext, "imsswitch", "reset_voicecall_type_done_" + str, true);
        String str2 = this.LOG_TAG;
        IMSLog.s(str2, i, "finishResetVoiceCallType: Mno(" + str + ")");
    }

    private boolean isNotFinishResetVoiceCallType(int i, String str) {
        boolean z = ImsSharedPrefHelper.getBoolean(i, this.mContext, "imsswitch", "reset_voicecall_type_done_" + str, false);
        String str2 = this.LOG_TAG;
        StringBuilder sb = new StringBuilder();
        sb.append("isNotFinishResetVoiceCallType: Mno(");
        sb.append(str);
        sb.append("):");
        sb.append(!z);
        IMSLog.s(str2, i, sb.toString());
        return !z;
    }

    /* access modifiers changed from: protected */
    public boolean needToCheckResetSetting() {
        return this.mVersionUpdated;
    }

    /* access modifiers changed from: protected */
    public void initNeedToCheckResetSetting() {
        this.mVersionUpdated = false;
    }

    /* access modifiers changed from: protected */
    public void setPrevGcEnabled(boolean z) {
        String str = this.LOG_TAG;
        int i = this.mPhoneId;
        IMSLog.i(str, i, "setPrevGcEnabled: " + z);
        SharedPreferences.Editor edit = ImsSharedPrefHelper.getSharedPref(this.mPhoneId, this.mContext, "imsswitch", 0, false).edit();
        edit.putBoolean("prevGcEnabled", z);
        edit.apply();
    }

    /* access modifiers changed from: protected */
    public boolean getPrevGcEnabled() {
        return ImsSharedPrefHelper.getSharedPref(this.mPhoneId, this.mContext, "imsswitch", 0, false).getBoolean("prevGcEnabled", false);
    }

    /* access modifiers changed from: protected */
    public void setIsGcEnabledChange(boolean z) {
        String str = this.LOG_TAG;
        int i = this.mPhoneId;
        IMSLog.i(str, i, "setIsGcEnabledChange: " + z);
        SharedPreferences.Editor edit = ImsSharedPrefHelper.getSharedPref(this.mPhoneId, this.mContext, "imsswitch", 0, false).edit();
        edit.putBoolean("isGcEnabledChange", z);
        edit.apply();
    }

    /* access modifiers changed from: protected */
    public void initRcsUserSetting(int i, int i2) {
        int rcsUserSetting = ImsConstants.SystemSettings.getRcsUserSetting(this.mContext, -3, this.mPhoneId);
        SimpleEventLog simpleEventLog = this.mEventLog;
        simpleEventLog.logAndAdd("simSlot[" + this.mPhoneId + "] initRcsUserSetting: system [" + rcsUserSetting + "], sp [" + i + "], default [" + i2 + "]");
        if (i == -1) {
            i = i2;
        }
        IMSLog.c(LogClass.GLOBAL_INIT_RCS_DB, this.mPhoneId + "GLB:" + i);
        if (rcsUserSetting != i && isRcsUserSettingValueAllowed(i)) {
            ImsConstants.SystemSettings.setRcsUserSetting(this.mContext, i, this.mPhoneId);
        }
    }

    /* access modifiers changed from: protected */
    public void updateSystemSettings(Mno mno, ContentValues contentValues, String str, String str2, int i, int i2) {
        int i3;
        boolean z;
        if (mno.isKor() && !TextUtils.equals(str, str2)) {
            i = -1;
        }
        boolean isNeedToBeSetVoLTE = isNeedToBeSetVoLTE(str, str2);
        if (!DeviceUtil.removeVolteMenuByCsc(this.mPhoneId) || mno.isChn()) {
            i3 = i;
            z = isNeedToBeSetVoLTE;
        } else {
            IMSLog.d(this.LOG_TAG, this.mPhoneId, "reset voice and vt call settings db because of VOICECLLCSC CONFIGOPSTYLEMOBILENETWORKSETTINGMENU Feature");
            z = true;
            i3 = -1;
        }
        setSettingsFromSp(z, i3, isNeedToBeSetViLTE(contentValues, str, str2), i2, false, -1);
    }

    /* access modifiers changed from: protected */
    /* JADX WARNING: Removed duplicated region for block: B:12:0x0099  */
    /* Code decompiled incorrectly, please refer to instructions dump. */
    public boolean isNeedToBeSetViLTE(android.content.ContentValues r6, java.lang.String r7, java.lang.String r8) {
        /*
            r5 = this;
            int r6 = r5.mPhoneId
            boolean r6 = r5.needResetCallSettingBySim(r6)
            r0 = 1
            java.lang.String r1 = "simSlot["
            if (r6 == 0) goto L_0x0029
            com.sec.internal.helper.SimpleEventLog r6 = r5.mEventLog
            java.lang.StringBuilder r8 = new java.lang.StringBuilder
            r8.<init>()
            r8.append(r1)
            int r2 = r5.mPhoneId
            r8.append(r2)
            java.lang.String r2 = "] reset vt call settings db by simcard change"
            r8.append(r2)
            java.lang.String r8 = r8.toString()
            r6.logAndAdd(r8)
        L_0x0027:
            r6 = r0
            goto L_0x0052
        L_0x0029:
            boolean r6 = android.text.TextUtils.equals(r7, r8)
            if (r6 != 0) goto L_0x0051
            boolean r6 = android.text.TextUtils.isEmpty(r8)
            if (r6 != 0) goto L_0x0051
            com.sec.internal.helper.SimpleEventLog r6 = r5.mEventLog
            java.lang.StringBuilder r8 = new java.lang.StringBuilder
            r8.<init>()
            r8.append(r1)
            int r2 = r5.mPhoneId
            r8.append(r2)
            java.lang.String r2 = "] reset video call settings db by simcard change"
            r8.append(r2)
            java.lang.String r8 = r8.toString()
            r6.logAndAdd(r8)
            goto L_0x0027
        L_0x0051:
            r6 = 0
        L_0x0052:
            com.sec.internal.constants.Mno r7 = com.sec.internal.constants.Mno.fromName(r7)
            android.content.Context r8 = r5.mContext
            com.sec.internal.constants.ims.ImsConstants$SystemSettings$SettingsItem r2 = com.sec.internal.constants.ims.ImsConstants.SystemSettings.VILTE_SLOT1
            java.lang.String r2 = r2.getName()
            int r3 = r5.mPhoneId
            int r8 = com.sec.internal.helper.DmConfigHelper.getImsUserSetting(r8, r2, r3)
            com.sec.internal.helper.SimpleEventLog r2 = r5.mEventLog
            java.lang.StringBuilder r3 = new java.lang.StringBuilder
            r3.<init>()
            r3.append(r1)
            int r4 = r5.mPhoneId
            r3.append(r4)
            java.lang.String r4 = "] videocall_type_"
            r3.append(r4)
            java.lang.String r7 = r7.getName()
            r3.append(r7)
            java.lang.String r7 = ": ["
            r3.append(r7)
            r3.append(r8)
            java.lang.String r7 = "]"
            r3.append(r7)
            java.lang.String r7 = r3.toString()
            r2.logAndAdd(r7)
            boolean r7 = com.sec.internal.helper.os.DeviceUtil.getGcfMode()
            if (r7 != 0) goto L_0x00bb
            com.sec.internal.helper.SimpleEventLog r7 = r5.mEventLog
            java.lang.StringBuilder r2 = new java.lang.StringBuilder
            r2.<init>()
            r2.append(r1)
            int r5 = r5.mPhoneId
            r2.append(r5)
            java.lang.String r5 = "] NOT Temporal SIM swapped: Set Video DB - "
            r2.append(r5)
            r2.append(r8)
            java.lang.String r5 = r2.toString()
            r7.logAndAdd(r5)
            r5 = -1
            if (r8 == r5) goto L_0x00bb
            goto L_0x00bc
        L_0x00bb:
            r0 = r6
        L_0x00bc:
            return r0
        */
        throw new UnsupportedOperationException("Method not decompiled: com.sec.internal.ims.settings.GlobalSettingsRepoBase.isNeedToBeSetViLTE(android.content.ContentValues, java.lang.String, java.lang.String):boolean");
    }

    /* access modifiers changed from: protected */
    public boolean isNeedToBeSetVoLTE(String str, String str2) {
        if (DmConfigHelper.getImsUserSetting(this.mContext, ImsConstants.SystemSettings.VOLTE_SLOT1.getName(), this.mPhoneId) != -1) {
            return true;
        }
        if (needResetCallSettingBySim(this.mPhoneId)) {
            SimpleEventLog simpleEventLog = this.mEventLog;
            simpleEventLog.logAndAdd("simSlot[" + this.mPhoneId + "] reset voice call settings db by simcard change");
            return true;
        } else if (TextUtils.equals(str, str2) || TextUtils.isEmpty(str2)) {
            return false;
        } else {
            SimpleEventLog simpleEventLog2 = this.mEventLog;
            simpleEventLog2.logAndAdd("simSlot[" + this.mPhoneId + "] reset voice call settings db by simcard change");
            return true;
        }
    }

    public void resetUserSettingAsDefault(boolean z, boolean z2, boolean z3) {
        if (z) {
            ImsConstants.SystemSettings.setVoiceCallType(this.mContext, -1, this.mPhoneId);
            IMSLog.c(LogClass.GLOBAL_LOAD_VOICE_DB, this.mPhoneId + ",SET:" + -1);
        }
        if (z2) {
            ImsConstants.SystemSettings.setVideoCallType(this.mContext, -1, this.mPhoneId);
            IMSLog.c(LogClass.GLOBAL_LOAD_VIDEO_DB, this.mPhoneId + ",SET:" + -1);
        }
        if (z3) {
            ImsConstants.SystemSettings.setRcsUserSetting(this.mContext, -1, this.mPhoneId);
            IMSLog.c(LogClass.GLOBAL_LOAD_RCS_DB, this.mPhoneId + ",SET:" + -1);
        }
    }

    /* access modifiers changed from: protected */
    public void setSettingsFromSp(boolean z, int i, boolean z2, int i2, boolean z3, int i3) {
        if (z) {
            ImsConstants.SystemSettings.setVoiceCallType(this.mContext, i, this.mPhoneId);
            IMSLog.c(LogClass.GLOBAL_LOAD_VOICE_DB, this.mPhoneId + ",SET:" + i);
        }
        if (z2) {
            ImsConstants.SystemSettings.setVideoCallType(this.mContext, i2, this.mPhoneId);
            IMSLog.c(LogClass.GLOBAL_LOAD_VIDEO_DB, this.mPhoneId + ",SET:" + i2);
        }
        if (z3) {
            ImsConstants.SystemSettings.setRcsUserSetting(this.mContext, i3, this.mPhoneId);
            IMSLog.c(LogClass.GLOBAL_LOAD_RCS_DB, this.mPhoneId + ",SET RCS DB:" + i3);
        }
    }

    /* access modifiers changed from: protected */
    public boolean updateRequires(ContentValues contentValues) {
        Boolean asBoolean;
        String asString;
        String asString2;
        String asString3;
        Integer asInteger;
        synchronized (this.mLock) {
            asBoolean = contentValues.getAsBoolean(ISimManager.KEY_HAS_SIM);
            if (asBoolean == null) {
                asBoolean = Boolean.FALSE;
            }
            asString = contentValues.getAsString("mnoname");
            asString2 = contentValues.getAsString(ISimManager.KEY_MVNO_NAME);
            asString3 = contentValues.getAsString(ISimManager.KEY_NW_NAME);
            if (asString3 == null) {
                asString3 = "";
            }
            asInteger = contentValues.getAsInteger(ISimManager.KEY_IMSSWITCH_TYPE);
            if (asInteger == null) {
                asInteger = 0;
            }
        }
        SharedPreferences sharedPref = ImsSharedPrefHelper.getSharedPref(this.mPhoneId, this.mContext, ImsSharedPrefHelper.GLOBAL_SETTINGS, 0, false);
        if (isVersionUpdated()) {
            Log.d(this.LOG_TAG, "PDA or CSC version changed");
            this.mVersionUpdated = true;
            return true;
        } else if (CscParser.isCscChanged(this.mContext, this.mPhoneId)) {
            this.mEventLog.logAndAdd("update Requires: CSC Info Changed");
            this.mVersionUpdated = true;
            return true;
        } else if (!getPreviousMno(sharedPref).equals(asString)) {
            Log.d(this.LOG_TAG, "update Requires: different mnoname");
            this.mMnoNameUpdated = true;
            return true;
        } else if (!getPreviousMvno(sharedPref).equals(asString2)) {
            Log.d(this.LOG_TAG, "update Requires: different MVNO name");
            this.mMnoNameUpdated = true;
            return true;
        } else if (!getPreviousNwCode(sharedPref).equals(OmcCode.getNWCode(this.mPhoneId))) {
            Log.d(this.LOG_TAG, "update Requires: different omc_nw code");
            return true;
        } else if (!getPreviousNwName(sharedPref).equals(asString3)) {
            Log.d(this.LOG_TAG, "update Requires: different network name");
            return true;
        } else if (getPreviousHasSim(sharedPref) != asBoolean.booleanValue()) {
            String str = this.LOG_TAG;
            Log.d(str, "update Requires: hasSim Changed " + asBoolean);
            return true;
        } else {
            int previousCscImsSettingType = getPreviousCscImsSettingType(sharedPref);
            if (previousCscImsSettingType != asInteger.intValue()) {
                String str2 = this.LOG_TAG;
                Log.d(str2, "update Requires: cscImsSettingType changed " + previousCscImsSettingType + " => " + asInteger);
                return true;
            }
            ImsAutoUpdate imsAutoUpdate = this.mAutoUpdate;
            if (imsAutoUpdate == null || !imsAutoUpdate.isUpdateNeeded()) {
                Log.d(this.LOG_TAG, "update not requires: same mno, same version");
                return false;
            }
            Log.d(this.LOG_TAG, "imsupdate changed.");
            return true;
        }
    }

    public void reset() {
        synchronized (this.mLock) {
            if (isLoaded()) {
                SharedPreferences.Editor edit = ImsSharedPrefHelper.getSharedPref(this.mPhoneId, this.mContext, ImsSharedPrefHelper.GLOBAL_SETTINGS, 0, false).edit();
                edit.clear();
                edit.putBoolean("loaded", false);
                edit.apply();
            }
        }
    }

    /* access modifiers changed from: protected */
    public String getPreviousNwCode(SharedPreferences sharedPreferences) {
        return sharedPreferences.getString("nwcode", OmcCode.getNWCode(this.mPhoneId));
    }

    /* access modifiers changed from: protected */
    public boolean getPreviousHasSim(SharedPreferences sharedPreferences) {
        return sharedPreferences.getBoolean(ISimManager.KEY_HAS_SIM, false);
    }

    /* access modifiers changed from: protected */
    public int getPreviousCscImsSettingType(SharedPreferences sharedPreferences) {
        return sharedPreferences.getInt("cscimssettingtype", -1);
    }

    /* access modifiers changed from: protected */
    public boolean getPreviousGcfMode(SharedPreferences sharedPreferences) {
        return sharedPreferences.getBoolean("gcfmode", false);
    }

    /* access modifiers changed from: protected */
    public String getPreviousMno(SharedPreferences sharedPreferences) {
        return sharedPreferences.getString("mnoname", "");
    }

    /* access modifiers changed from: protected */
    public String getPreviousMvno(SharedPreferences sharedPreferences) {
        return sharedPreferences.getString(ISimManager.KEY_MVNO_NAME, "");
    }

    /* access modifiers changed from: protected */
    public String getPreviousNwName(SharedPreferences sharedPreferences) {
        return sharedPreferences.getString(ISimManager.KEY_NW_NAME, "");
    }

    /* access modifiers changed from: protected */
    public String getPreviousImsi(SharedPreferences sharedPreferences) {
        return sharedPreferences.getString("imsi", "");
    }

    public String getPreviousMno() {
        return ImsSharedPrefHelper.getString(this.mPhoneId, this.mContext, ImsSharedPrefHelper.GLOBAL_SETTINGS, "mnoname", "");
    }

    public boolean getGlobalGcEnabled() {
        return ImsSharedPrefHelper.getBoolean(this.mPhoneId, this.mContext, ImsSharedPrefHelper.GLOBAL_SETTINGS, ISimManager.KEY_GLOBALGC_ENABLED, false);
    }

    /* access modifiers changed from: protected */
    public boolean isVersionUpdated() {
        String str = SemSystemProperties.get("ro.build.PDA", "");
        String str2 = SemSystemProperties.get("ril.official_cscver", "");
        String string = ImsSharedPrefHelper.getString(this.mPhoneId, this.mContext, ImsSharedPrefHelper.GLOBAL_SETTINGS, "buildinfo", "");
        StringBuilder sb = new StringBuilder();
        sb.append(str);
        sb.append("_");
        sb.append(str2);
        return !string.equals(sb.toString());
    }

    /* access modifiers changed from: protected */
    public String saveBuildInfo() {
        String str = SemSystemProperties.get("ro.build.PDA", "");
        String str2 = SemSystemProperties.get("ril.official_cscver", "");
        return str + "_" + str2;
    }

    public void dump() {
        this.mEventLog.dump();
        SharedPreferences sharedPref = ImsSharedPrefHelper.getSharedPref(this.mPhoneId, this.mContext, ImsSharedPrefHelper.GLOBAL_SETTINGS, 0, false);
        if (sharedPref != null && sharedPref.getBoolean("loaded", false)) {
            Map<String, ?> all = sharedPref.getAll();
            all.remove("imsi");
            IMSLog.increaseIndent(this.LOG_TAG);
            IMSLog.dump(this.LOG_TAG, this.mPhoneId, "\nLast values of GlobalSettings:");
            IMSLog.increaseIndent(this.LOG_TAG);
            for (Map.Entry next : all.entrySet()) {
                String str = this.LOG_TAG;
                int i = this.mPhoneId;
                IMSLog.dump(str, i, ((String) next.getKey()) + " = [" + next.getValue() + "]");
            }
            IMSLog.decreaseIndent(this.LOG_TAG);
            IMSLog.decreaseIndent(this.LOG_TAG);
        }
    }

    /* access modifiers changed from: protected */
    public int readVolteDefaultEnabled() {
        return Integer.parseInt(ImsSharedPrefHelper.getString(this.mPhoneId, this.mContext, ImsSharedPrefHelper.GLOBAL_SETTINGS, GlobalSettingsConstants.Registration.VOLTE_DOMESTIC_DEFAULT_ENABLED, "-1"));
    }

    /* access modifiers changed from: protected */
    public String requiredForceVolteDefaultEnabled() {
        return ImsSharedPrefHelper.getString(this.mPhoneId, this.mContext, ImsSharedPrefHelper.GLOBAL_SETTINGS, "force_volte_default_enabled", "");
    }

    /* access modifiers changed from: protected */
    public int readRcsDefaultEnabled(boolean z) {
        if (z) {
            return Integer.parseInt(ImsSharedPrefHelper.getString(this.mPhoneId, this.mContext, ImsSharedPrefHelper.GLOBAL_GC_SETTINGS, GlobalSettingsConstants.RCS.RCS_DEFAULT_ENABLED, "-1"));
        }
        return Integer.parseInt(ImsSharedPrefHelper.getString(this.mPhoneId, this.mContext, ImsSharedPrefHelper.GLOBAL_SETTINGS, GlobalSettingsConstants.RCS.RCS_DEFAULT_ENABLED, "-1"));
    }
}
