package com.sec.internal.ims.settings;

import android.annotation.SuppressLint;
import android.content.ContentValues;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.database.ContentObserver;
import android.os.SemSystemProperties;
import android.provider.Settings;
import android.provider.Telephony;
import android.text.TextUtils;
import android.util.Log;
import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.sec.ims.settings.ImsProfile;
import com.sec.internal.constants.Mno;
import com.sec.internal.constants.ims.ImsConstants;
import com.sec.internal.helper.CollectionUtils;
import com.sec.internal.helper.ImsSharedPrefHelper;
import com.sec.internal.helper.JsonUtil;
import com.sec.internal.helper.OmcCode;
import com.sec.internal.helper.SimUtil;
import com.sec.internal.helper.SimpleEventLog;
import com.sec.internal.helper.UriUtil;
import com.sec.internal.helper.os.DeviceUtil;
import com.sec.internal.helper.os.PackageUtils;
import com.sec.internal.helper.os.TelephonyManagerWrapper;
import com.sec.internal.ims.core.sim.SimManagerFactory;
import com.sec.internal.ims.settings.ImsServiceSwitch;
import com.sec.internal.ims.util.ConfigUtil;
import com.sec.internal.ims.util.ImsUtil;
import com.sec.internal.imscr.LogClass;
import com.sec.internal.interfaces.ims.core.ISimManager;
import com.sec.internal.log.IMSLog;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.Optional;

public class ImsServiceSwitchBase extends ImsServiceSwitch {
    private final String LOG_TAG;
    protected Map<String, Boolean> mServiceMap = new HashMap();

    /* access modifiers changed from: protected */
    public boolean isCscRcsEnabled() {
        return true;
    }

    /* access modifiers changed from: protected */
    public ContentValues overrideImsSwitchForCarrier(ContentValues contentValues) {
        return contentValues;
    }

    public void unregisterObserver() {
    }

    public ImsServiceSwitchBase(Context context, int i) {
        String simpleName = ImsServiceSwitchBase.class.getSimpleName();
        this.LOG_TAG = simpleName;
        this.mContext = context;
        this.mPhoneId = i;
        Log.d(simpleName + "[" + this.mPhoneId + "]", "created");
        this.mEventLog = new SimpleEventLog(this.mContext, i, simpleName, 200);
        initSwitchPref(ImsSharedPrefHelper.getSharedPref(this.mPhoneId, this.mContext, "imsswitch", 0, false).contains("volte"));
        dumpServiceSwitch();
    }

    /* access modifiers changed from: protected */
    public void initSwitchPref(boolean z) {
        if (z) {
            loadImsSwitchFromSharedPreferences();
        } else {
            init();
        }
    }

    public void init() {
        boolean z;
        boolean z2;
        Log.d(this.LOG_TAG + "[" + this.mPhoneId + "]", "init:");
        String str = SemSystemProperties.get(Mno.MOCK_MNO_PROPERTY, "");
        ISimManager simManagerFromSimSlot = SimManagerFactory.getSimManagerFromSimSlot(this.mPhoneId);
        int simState = TelephonyManagerWrapper.getInstance(this.mContext).getSimState(this.mPhoneId);
        if (!(!TextUtils.isEmpty(str) || simState == 0 || simState == 1)) {
            if (simManagerFromSimSlot == null) {
                Log.d(this.LOG_TAG + "[" + this.mPhoneId + "]", "init: Not SIM ready yet.");
            } else {
                z2 = simManagerFromSimSlot.isLabSimCard();
                z = simManagerFromSimSlot.isSimLoaded();
                initServiceSwitch(z2, z);
                persist();
            }
        }
        z2 = false;
        z = false;
        initServiceSwitch(z2, z);
        persist();
    }

    public void loadImsSwitchFromResource() {
        Log.d(this.LOG_TAG + "[" + this.mPhoneId + "]", "updateSwitchByDynamicUpdate:");
        ISimManager simManagerFromSimSlot = SimManagerFactory.getSimManagerFromSimSlot(this.mPhoneId);
        if (simManagerFromSimSlot != null) {
            ContentValues loadImsSwitchFromJson = loadImsSwitchFromJson(simManagerFromSimSlot.getSimMno().getName(), SimUtil.getMvnoName(simManagerFromSimSlot.getSimMnoName()), 4);
            if (SimUtil.isSimMobilityAvailable(this.mContext, this.mPhoneId, simManagerFromSimSlot.getSimMno()) && !ImsUtil.isSimMobilityActivatedForRcs(this.mPhoneId)) {
                Log.d(this.LOG_TAG + "[" + this.mPhoneId + "]", "Disable RCS in SimMobility");
                Boolean bool = Boolean.FALSE;
                loadImsSwitchFromJson.put(ImsServiceSwitch.ImsSwitch.RCS.ENABLE_RCS, bool);
                loadImsSwitchFromJson.put(ImsServiceSwitch.ImsSwitch.RCS.ENABLE_RCS_CHAT_SERVICE, bool);
            }
            loadImsSwitchFromJson.put(ISimManager.KEY_GLOBALGC_ENABLED, Boolean.valueOf(ConfigUtil.getGlobalGcEnabled(this.mContext, this.mPhoneId)));
            turnOffAllSwitch();
            updateServiceSwitchInternal(loadImsSwitchFromJson);
            saveImsSwitch(loadImsSwitchFromJson);
            enable(this.mServiceMap);
            return;
        }
        IMSLog.e(this.LOG_TAG, "SimManager is null");
    }

    /* access modifiers changed from: protected */
    public void loadImsSwitchFromSharedPreferences() {
        SharedPreferences sharedPref = ImsSharedPrefHelper.getSharedPref(this.mPhoneId, this.mContext, "imsswitch", 0, false);
        String[] voLteServiceList = ImsProfile.getVoLteServiceList();
        String[] rcsServiceList = ImsProfile.getRcsServiceList();
        for (String str : voLteServiceList) {
            this.mVolteServiceSwitch.put(str, Boolean.valueOf(sharedPref.getBoolean(str, false)));
        }
        for (String str2 : rcsServiceList) {
            this.mRcsServiceSwitch.put(str2, Boolean.valueOf(sharedPref.getBoolean(str2, false)));
        }
        this.mVoLteEnabled = sharedPref.getBoolean("volte", false);
        this.mRcsEnabled = sharedPref.getBoolean(DeviceConfigManager.RCS, false);
        this.mSsEnabled = sharedPref.getBoolean("ss", false);
        if (!sharedPref.contains(ServiceConstants.SERVICE_CHATBOT_COMMUNICATION)) {
            Log.d(this.LOG_TAG, "load: new switch chatbot-communication being set to " + this.mRcsEnabled);
            this.mRcsServiceSwitch.put(ServiceConstants.SERVICE_CHATBOT_COMMUNICATION, Boolean.valueOf(this.mRcsEnabled));
            persist();
        }
        dumpServiceSwitch();
        IMSLog.c(LogClass.SWITCH_LOAD, this.mPhoneId + ",LOAD:" + getSwitchDump());
    }

    /* access modifiers changed from: protected */
    @SuppressLint({"WorldReadableFiles"})
    public void persist() {
        Log.d(this.LOG_TAG, "persist.");
        SharedPreferences.Editor edit = ImsSharedPrefHelper.getSharedPref(this.mPhoneId, this.mContext, "imsswitch", 0, false).edit();
        for (Map.Entry next : this.mVolteServiceSwitch.entrySet()) {
            edit.putBoolean((String) next.getKey(), ((Boolean) next.getValue()).booleanValue());
        }
        for (Map.Entry next2 : this.mRcsServiceSwitch.entrySet()) {
            edit.putBoolean((String) next2.getKey(), ((Boolean) next2.getValue()).booleanValue());
        }
        Log.d(this.LOG_TAG + "[" + this.mPhoneId + "]", "load: volte [" + this.mVoLteEnabled + "], rcs [" + this.mRcsEnabled + "]");
        edit.putBoolean("volte", this.mVoLteEnabled);
        edit.putBoolean(DeviceConfigManager.RCS, this.mRcsEnabled);
        edit.putBoolean("ss", this.mSsEnabled);
        edit.apply();
        this.mContext.getContentResolver().notifyChange(UriUtil.buildUri("content://com.sec.ims.settings/imsswitch", this.mPhoneId), (ContentObserver) null);
        this.mContext.getContentResolver().notifyChange(UriUtil.buildUri("content://com.sec.ims.settings/imsswitch/mmtel", this.mPhoneId), (ContentObserver) null);
        this.mContext.getContentResolver().notifyChange(UriUtil.buildUri("content://com.sec.ims.settings/imsswitch/mmtel-video", this.mPhoneId), (ContentObserver) null);
    }

    public String getIpmeSpKeyName(String str) {
        return "ipme_status_" + str;
    }

    public void updateServiceSwitch(ContentValues contentValues) {
        int intValue;
        IMSLog.d(this.LOG_TAG, this.mPhoneId, "updateServiceSwitch:");
        boolean booleanValue = ((Boolean) Optional.ofNullable(contentValues.getAsBoolean(ISimManager.KEY_HAS_SIM)).orElse(Boolean.FALSE)).booleanValue();
        ISimManager simManagerFromSimSlot = SimManagerFactory.getSimManagerFromSimSlot(this.mPhoneId);
        boolean z = simManagerFromSimSlot != null && simManagerFromSimSlot.isLabSimCard();
        String str = this.LOG_TAG;
        int i = this.mPhoneId;
        IMSLog.d(str, i, "updateServiceSwitch: isLabSimCard [" + z + "]");
        int intValue2 = ((Integer) Optional.ofNullable(contentValues.getAsInteger(ISimManager.KEY_IMSSWITCH_TYPE)).orElse(0)).intValue();
        if (!booleanValue || !(!z || intValue2 == 4 || intValue2 == 3)) {
            this.mContext.sendBroadcast(new Intent("android.intent.action.IMS_SETTINGS_UPDATED"));
            IMSLog.e(this.LOG_TAG, this.mPhoneId, "No operator code for settings. Update UI!");
            return;
        }
        String str2 = this.LOG_TAG;
        int i2 = this.mPhoneId;
        IMSLog.d(str2, i2, "updateMno: hasSim:" + booleanValue + ", imsSwitchType:" + intValue2 + ", mnoinfo:" + contentValues);
        if (intValue2 == 3 || intValue2 == 4 || intValue2 == 5) {
            ContentValues loadImsSwitchFromJson = loadImsSwitchFromJson(contentValues.getAsString("mnoname"), contentValues.getAsString(ISimManager.KEY_MVNO_NAME), intValue2);
            if (intValue2 == 4) {
                if (loadImsSwitchFromJson.size() > 0) {
                    loadImsSwitchFromJson.remove("mnoname");
                    contentValues.putAll(loadImsSwitchFromJson);
                } else {
                    for (String put : ImsServiceSwitch.getImsServiceSwitchTable()) {
                        contentValues.put(put, Boolean.FALSE);
                    }
                }
            } else if (intValue2 == 3 && ((intValue = ((Integer) Optional.ofNullable(contentValues.getAsInteger(ISimManager.KEY_SIMMO_TYPE)).orElse(0)).intValue()) == 3 || intValue == 1)) {
                String[] strArr = {ImsServiceSwitch.ImsSwitch.DeviceManagement.ENABLE_IMS, ImsServiceSwitch.ImsSwitch.DeviceManagement.ENABLE_VOWIFI, ImsServiceSwitch.ImsSwitch.VoLTE.ENABLE_SMS_IP, ImsServiceSwitch.ImsSwitch.VoLTE.ENABLE_VIDEO_CALL, ImsServiceSwitch.ImsSwitch.VoLTE.ENABLE_VOLTE};
                for (int i3 = 0; i3 < 5; i3++) {
                    String str3 = strArr[i3];
                    contentValues.put(str3, loadImsSwitchFromJson.getAsBoolean(str3));
                }
            }
            turnOffAllSwitch();
            updateServiceSwitchInternal(contentValues);
            saveImsSwitch(contentValues);
            enable(this.mServiceMap);
            return;
        }
        IMSLog.e(this.LOG_TAG, this.mPhoneId, "can not find a matched ims switch type");
        init();
    }

    public void enable(String str, boolean z) {
        IMSLog.i(this.LOG_TAG + "[" + this.mPhoneId + "]", "enable: " + str + " : " + z);
        if (this.mVolteServiceSwitch.containsKey(str)) {
            this.mVolteServiceSwitch.put(str, Boolean.valueOf(z));
        }
        if (this.mRcsServiceSwitch.containsKey(str)) {
            this.mRcsServiceSwitch.put(str, Boolean.valueOf(z));
        }
        persist();
    }

    public void enable(Map<String, Boolean> map) {
        for (Map.Entry next : map.entrySet()) {
            String str = (String) next.getKey();
            Boolean valueOf = Boolean.valueOf(((Boolean) next.getValue()).booleanValue());
            if (this.mVolteServiceSwitch.containsKey(str)) {
                this.mVolteServiceSwitch.put(str, valueOf);
            }
            if (this.mRcsServiceSwitch.containsKey(str)) {
                this.mRcsServiceSwitch.put(str, valueOf);
            }
        }
        SimpleEventLog simpleEventLog = this.mEventLog;
        int i = this.mPhoneId;
        simpleEventLog.logAndAdd(i, "enable: volte: " + this.mVolteServiceSwitch.toString());
        SimpleEventLog simpleEventLog2 = this.mEventLog;
        int i2 = this.mPhoneId;
        simpleEventLog2.logAndAdd(i2, "enable: rcs: " + this.mRcsServiceSwitch.toString());
        persist();
    }

    public String toString() {
        return "Simslot[" + this.mPhoneId + "] ImsServiceSwitch mRcsEnabled [" + this.mRcsEnabled + "], mVoLteEnabled [" + this.mVoLteEnabled + "], mVolteServiceSwitch [" + this.mVolteServiceSwitch + "], mRcsServiceSwitch [" + this.mRcsServiceSwitch + "]";
    }

    public boolean isImsEnabled() {
        return isVoLteEnabled() || isRcsEnabled();
    }

    public boolean isEnabled(String str) {
        if (this.mVolteServiceSwitch.containsKey(str)) {
            if ("ss".equals(str)) {
                if ((this.mSsEnabled || this.mVoLteEnabled) && this.mVolteServiceSwitch.get(str).booleanValue()) {
                    return true;
                }
                return false;
            } else if (!this.mVoLteEnabled || !this.mVolteServiceSwitch.get(str).booleanValue()) {
                return false;
            } else {
                return true;
            }
        } else if (!this.mRcsServiceSwitch.containsKey(str)) {
            return false;
        } else {
            if (!this.mRcsEnabled || !this.mRcsServiceSwitch.get(str).booleanValue()) {
                return false;
            }
            return true;
        }
    }

    public boolean isVoLteEnabled() {
        Log.d(this.LOG_TAG + "[" + this.mPhoneId + "]", "isVoLteEnabled: " + this.mVoLteEnabled);
        return this.mVoLteEnabled;
    }

    public void enableVoLte(boolean z) {
        Log.d(this.LOG_TAG + "[" + this.mPhoneId + "]", "enableVoLte: " + z);
        this.mVoLteEnabled = z;
        persist();
    }

    public boolean isRcsEnabled() {
        return this.mRcsEnabled;
    }

    public boolean isRcsSwitchEnabled() {
        return this.mRcsEnabled;
    }

    public void enableRcs(boolean z) {
        Log.d(this.LOG_TAG + "[" + this.mPhoneId + "]", "enableRcs: " + z);
        this.mRcsEnabled = z;
        persist();
    }

    public boolean isDefaultMessageAppInUse() {
        String str;
        boolean z;
        try {
            str = Telephony.Sms.getDefaultSmsPackage(this.mContext);
        } catch (Exception e) {
            Log.e(this.LOG_TAG + "[" + this.mPhoneId + "]", "Failed to getDefaultSmsPackage: ", e);
            str = null;
        }
        if (str == null) {
            str = Settings.Secure.getString(this.mContext.getContentResolver(), "sms_default_application");
            Log.d(this.LOG_TAG + "[" + this.mPhoneId + "]", "smsApplication is null check from Settings : " + str);
        }
        if (str == null) {
            Log.e(this.LOG_TAG + "[" + this.mPhoneId + "]", "smsApplication is null");
            z = false;
        } else {
            z = TextUtils.equals(str, PackageUtils.getMsgAppPkgName(this.mContext));
        }
        Log.d(this.LOG_TAG + "[" + this.mPhoneId + "]", "isDefaultMessageAppInUse : Result [" + z + "] Name [" + str + "] ");
        return z;
    }

    public void doInit() {
        Log.d(this.LOG_TAG + "[" + this.mPhoneId + "]", "doInit from ImsSettings");
        init();
    }

    public int getVideoCallType(String str) {
        String videoSpKeyName = getVideoSpKeyName(str);
        int i = ImsSharedPrefHelper.getInt(this.mPhoneId, this.mContext, "imsswitch", videoSpKeyName, -1);
        Log.d(this.LOG_TAG + "[" + this.mPhoneId + "]", "getVideoCallType: " + videoSpKeyName + " = [" + i + "]");
        return i;
    }

    public void setVideoCallType(String str, int i) {
        String videoSpKeyName = getVideoSpKeyName(str);
        ImsSharedPrefHelper.save(this.mPhoneId, this.mContext, "imsswitch", videoSpKeyName, i);
        SimpleEventLog simpleEventLog = this.mEventLog;
        simpleEventLog.logAndAdd("simSlot[" + this.mPhoneId + "] setVideoCallType: " + videoSpKeyName + " = [" + i + "]");
    }

    private String getVideoSpKeyName(String str) {
        return ImsConstants.SystemSettings.VILTE_SLOT1.getName() + "_" + str;
    }

    public int getVoiceCallType(String str) {
        String voLteSpKeyName = getVoLteSpKeyName(str);
        int i = ImsSharedPrefHelper.getInt(this.mPhoneId, this.mContext, "imsswitch", voLteSpKeyName, -1);
        IMSLog.i(this.LOG_TAG + "[" + this.mPhoneId + "]", "getVoiceCallType: " + voLteSpKeyName + " = [" + i + "]");
        return i;
    }

    public void setVoiceCallType(String str, int i) {
        String voLteSpKeyName = getVoLteSpKeyName(str);
        ImsSharedPrefHelper.save(this.mPhoneId, this.mContext, "imsswitch", voLteSpKeyName, i);
        SimpleEventLog simpleEventLog = this.mEventLog;
        simpleEventLog.logAndAdd("simSlot[" + this.mPhoneId + "] setVoiceCallType: " + voLteSpKeyName + " = [" + i + "]");
    }

    private String getVoLteSpKeyName(String str) {
        return ImsConstants.SystemSettings.VOLTE_SLOT1.getName() + "_" + str;
    }

    public int getRcsUserSetting() {
        String rcsUserSettingSpKeyName = getRcsUserSettingSpKeyName();
        int i = ImsSharedPrefHelper.getInt(this.mPhoneId, this.mContext, "imsswitch", rcsUserSettingSpKeyName, -1);
        IMSLog.i(this.LOG_TAG + "[" + this.mPhoneId + "]", "getRcsUserSetting: " + IMSLog.numberChecker(rcsUserSettingSpKeyName) + " = [" + i + "]");
        return i;
    }

    public void setRcsUserSetting(int i) {
        ImsSharedPrefHelper.save(this.mPhoneId, this.mContext, "imsswitch", getRcsUserSettingSpKeyName(), i);
    }

    private String getRcsUserSettingSpKeyName() {
        ISimManager simManagerFromSimSlot = SimManagerFactory.getSimManagerFromSimSlot(this.mPhoneId);
        String imsi = simManagerFromSimSlot != null ? simManagerFromSimSlot.getImsi() : "";
        return ImsConstants.SystemSettings.RCS_USER_SETTING1.getName() + "_" + imsi;
    }

    public void dump() {
        this.mEventLog.dump();
        IMSLog.increaseIndent(this.LOG_TAG);
        String str = this.LOG_TAG;
        IMSLog.dump(str, "Last state of " + this.LOG_TAG + "<" + this.mPhoneId + ">:");
        IMSLog.increaseIndent(this.LOG_TAG);
        String str2 = this.LOG_TAG;
        IMSLog.dump(str2, "mVoLteEnabled [" + this.mVoLteEnabled + "], mRcsEnabled [" + this.mRcsEnabled + "]");
        for (Map.Entry next : this.mVolteServiceSwitch.entrySet()) {
            String str3 = this.LOG_TAG;
            IMSLog.dump(str3, "<" + this.mPhoneId + "> " + ((String) next.getKey()) + " = " + next.getValue());
        }
        for (Map.Entry next2 : this.mRcsServiceSwitch.entrySet()) {
            String str4 = this.LOG_TAG;
            IMSLog.dump(str4, "<" + this.mPhoneId + "> " + ((String) next2.getKey()) + " = " + next2.getValue());
        }
        IMSLog.decreaseIndent(this.LOG_TAG);
        IMSLog.decreaseIndent(this.LOG_TAG);
    }

    public String getDefaultMessageApp() {
        String str;
        try {
            str = Telephony.Sms.getDefaultSmsPackage(this.mContext);
        } catch (Exception e) {
            Log.e(this.LOG_TAG + "[" + this.mPhoneId + "]", "Failed to getDefaultSmsPackage: ", e);
            str = null;
        }
        Log.d(this.LOG_TAG + "[" + this.mPhoneId + "]", "getDefaultMessageApp : [" + str + "] ");
        return str;
    }

    /* access modifiers changed from: protected */
    public void initVolteServiceSwitch(boolean z) {
        this.mVoLteEnabled = z;
        this.mSsEnabled = z;
        for (String put : ImsProfile.getVoLteServiceList()) {
            this.mVolteServiceSwitch.put(put, Boolean.valueOf(z));
        }
    }

    /* access modifiers changed from: protected */
    public void initCallComposer(boolean z, boolean z2) {
        this.mVolteServiceSwitch.put("mmtel-call-composer", Boolean.valueOf(z && z2));
    }

    /* access modifiers changed from: protected */
    public void initRcsServiceSwitch(boolean z) {
        this.mRcsEnabled = z;
        for (String str : ImsProfile.getRcsServiceList()) {
            if (!TextUtils.equals(str, "plug-in")) {
                this.mRcsServiceSwitch.put(str, Boolean.valueOf(z));
            }
        }
    }

    /* access modifiers changed from: protected */
    public void initServiceSwitch(boolean z, boolean z2) {
        if (DeviceUtil.getGcfMode() || "GCF".equalsIgnoreCase(OmcCode.get()) || z || !z2 || SimUtil.isSoftphoneEnabled()) {
            boolean isCscRcsEnabled = isCscRcsEnabled();
            ContentValues initImsSwitch = getInitImsSwitch();
            if (!isCscRcsEnabled) {
                Boolean bool = Boolean.FALSE;
                initImsSwitch.put(ImsServiceSwitch.ImsSwitch.RCS.ENABLE_RCS, bool);
                initImsSwitch.put(ImsServiceSwitch.ImsSwitch.RCS.ENABLE_RCS_CHAT_SERVICE, bool);
            }
            saveImsSwitch(initImsSwitch);
            Log.d(this.LOG_TAG + "[" + this.mPhoneId + "]", "init: volteEnabled=" + true);
            initVolteServiceSwitch(true);
            initCallComposer(true, isCscRcsEnabled);
            Log.d(this.LOG_TAG + "[" + this.mPhoneId + "]", "init: rcsEnabled=" + isCscRcsEnabled);
            initRcsServiceSwitch(isCscRcsEnabled);
            StringBuilder sb = new StringBuilder();
            sb.append(this.mPhoneId);
            sb.append(",INIT SW:");
            String str = "1_";
            sb.append(str);
            sb.append(str);
            if (!isCscRcsEnabled) {
                str = "0_";
            }
            sb.append(str);
            IMSLog.c(LogClass.SWITCH_INIT_DONE, sb.toString());
            return;
        }
        loadImsSwitchFromResource();
    }

    /* access modifiers changed from: protected */
    public ContentValues getInitImsSwitch() {
        ContentValues contentValues = new ContentValues();
        for (String put : ImsServiceSwitch.getImsServiceSwitchTable()) {
            contentValues.put(put, Boolean.TRUE);
        }
        return contentValues;
    }

    /* access modifiers changed from: protected */
    public void turnOffAllSwitch() {
        this.mServiceMap.clear();
        for (String put : ImsProfile.getVoLteServiceList()) {
            this.mServiceMap.put(put, Boolean.FALSE);
        }
        for (String put2 : ImsProfile.getRcsServiceList()) {
            this.mServiceMap.put(put2, Boolean.FALSE);
        }
        this.mEventLog.logAndAdd(this.mPhoneId, "updateServiceSwitch: Turning all the switches off.");
    }

    /* access modifiers changed from: protected */
    public void parseImsSwitch(ContentValues contentValues) {
        for (Map.Entry next : ImsServiceSwitch.getVolteServiceSwitchTable().entrySet()) {
            String str = (String) next.getKey();
            String str2 = (String) next.getValue();
            String asString = contentValues.getAsString(str);
            SimpleEventLog simpleEventLog = this.mEventLog;
            int i = this.mPhoneId;
            simpleEventLog.logAndAdd(i, "CSC(Json) field: " + str + "[" + asString + "] -> Switching " + str2);
            if ("TRUE".equalsIgnoreCase(asString)) {
                this.mServiceMap.put(str2, Boolean.TRUE);
                IMSLog.c(LogClass.SWITCH_UPDATE_ON, this.mPhoneId + ",ON:" + str2);
            }
        }
    }

    /* access modifiers changed from: protected */
    public void enableRcsSwitch(boolean z) {
        for (String put : ImsProfile.getRcsServiceList()) {
            this.mServiceMap.put(put, Boolean.TRUE);
        }
        this.mEventLog.logAndAdd(this.mPhoneId, "updateServiceSwitch: Turning on all the RCS services.");
        if (!z) {
            this.mEventLog.logAndAdd(this.mPhoneId, "updateServiceSwitch: Turning off RCS Chat Service");
            for (String put2 : ImsProfile.getChatServiceList()) {
                this.mServiceMap.put(put2, Boolean.FALSE);
            }
            IMSLog.c(LogClass.SWITCH_UPDATE_OFF_CHAT, this.mPhoneId + ",OFF CHAT SW");
        }
    }

    /* access modifiers changed from: protected */
    public void updateServiceSwitchInternal(ContentValues contentValues) {
        if (CollectionUtils.getBooleanValue(contentValues, ISimManager.KEY_GLOBALGC_ENABLED, false)) {
            ContentValues contentValues2 = new ContentValues();
            Boolean bool = Boolean.TRUE;
            contentValues2.put(ImsServiceSwitch.ImsSwitch.DeviceManagement.ENABLE_IMS, bool);
            contentValues2.put(ImsServiceSwitch.ImsSwitch.RCS.ENABLE_RCS, bool);
            contentValues2.put(ImsServiceSwitch.ImsSwitch.RCS.ENABLE_RCS_CHAT_SERVICE, bool);
            contentValues.putAll(contentValues2);
        }
        if (DeviceUtil.isTablet() && TextUtils.equals("TMobile_US", contentValues.getAsString("mnoname")) && TextUtils.equals("Inbound", contentValues.getAsString(ISimManager.KEY_MVNO_NAME)) && TelephonyManagerWrapper.getInstance(this.mContext).isVoiceCapable()) {
            ContentValues contentValues3 = new ContentValues();
            Boolean bool2 = Boolean.TRUE;
            contentValues3.put(ImsServiceSwitch.ImsSwitch.DeviceManagement.ENABLE_IMS, bool2);
            contentValues3.put(ImsServiceSwitch.ImsSwitch.VoLTE.ENABLE_VOLTE, bool2);
            contentValues3.put(ImsServiceSwitch.ImsSwitch.VoLTE.ENABLE_SMS_IP, bool2);
            contentValues.putAll(contentValues3);
        }
        boolean booleanValue = CollectionUtils.getBooleanValue(contentValues, ImsServiceSwitch.ImsSwitch.DeviceManagement.ENABLE_IMS, false);
        boolean booleanValue2 = CollectionUtils.getBooleanValue(contentValues, ImsServiceSwitch.ImsSwitch.RCS.ENABLE_RCS, false);
        boolean z = booleanValue && booleanValue2 && CollectionUtils.getBooleanValue(contentValues, ImsServiceSwitch.ImsSwitch.RCS.ENABLE_RCS_CHAT_SERVICE, false);
        boolean z2 = booleanValue && (CollectionUtils.getBooleanValue(contentValues, ImsServiceSwitch.ImsSwitch.VoLTE.ENABLE_VOLTE, false) || CollectionUtils.getBooleanValue(contentValues, ImsServiceSwitch.ImsSwitch.DeviceManagement.ENABLE_VOWIFI, false) || CollectionUtils.getBooleanValue(contentValues, ImsServiceSwitch.ImsSwitch.VoLTE.ENABLE_SMS_IP, false));
        if (booleanValue) {
            parseImsSwitch(contentValues);
            if (z2) {
                Map<String, Boolean> map = this.mServiceMap;
                Boolean bool3 = Boolean.TRUE;
                map.put("ss", bool3);
                this.mServiceMap.put("datachannel", bool3);
            }
            if (booleanValue2) {
                enableRcsSwitch(z);
                SharedPreferences sharedPref = ImsSharedPrefHelper.getSharedPref(this.mPhoneId, this.mContext, "imsswitch", 0, false);
                ImsConstants.SystemSettings.SettingsItem settingsItem = ImsConstants.SystemSettings.RCS_USER_SETTING1;
                if (sharedPref.contains(settingsItem.getName()) && (Mno.SPRINT == Mno.fromName(contentValues.getAsString("mnoname")) || ConfigUtil.isRcsChn(Mno.fromName(contentValues.getAsString("mnoname"))))) {
                    booleanValue2 = sharedPref.getBoolean(settingsItem.getName(), true);
                }
                if (booleanValue2 && z2) {
                    this.mServiceMap.put("mmtel-call-composer", Boolean.TRUE);
                }
            }
        }
        SimpleEventLog simpleEventLog = this.mEventLog;
        int i = this.mPhoneId;
        simpleEventLog.logAndAdd(i, "updateServiceSwitch: ims [" + booleanValue + "] volte [" + z2 + "] rcs [" + booleanValue2 + "]");
        this.mVoLteEnabled = z2;
        this.mRcsEnabled = booleanValue2;
    }

    /* access modifiers changed from: protected */
    public ContentValues loadImsSwitchFromJson(String str, String str2, int i) {
        String str3 = this.LOG_TAG;
        int i2 = this.mPhoneId;
        IMSLog.d(str3, i2, "loadImsSwitchFromJson: mnoname=" + str + ", mvnoname=" + str2 + ", imsSwitchType=" + i);
        ContentValues contentValues = new ContentValues();
        if (TextUtils.isEmpty(str)) {
            IMSLog.e(this.LOG_TAG, this.mPhoneId, "load: loadImsSwitchFromJson is not identified.");
            return contentValues;
        }
        JsonElement imsSwitchFromJson = ImsServiceSwitchLoader.getImsSwitchFromJson(this.mContext, str, this.mPhoneId);
        if (imsSwitchFromJson.isJsonNull()) {
            return contentValues;
        }
        JsonObject asJsonObject = imsSwitchFromJson.getAsJsonObject();
        JsonElement jsonElement = asJsonObject.get("defaultswitch");
        if (jsonElement.isJsonNull()) {
            IMSLog.e(this.LOG_TAG, this.mPhoneId, "load: No default setting.");
            return contentValues;
        }
        JsonElement matchedJsonElement = ImsServiceSwitchLoader.getMatchedJsonElement(this.mContext, asJsonObject, str, str2, this.mPhoneId);
        if (i != 3 || !matchedJsonElement.isJsonNull()) {
            JsonElement merge = JsonUtil.merge(jsonElement, matchedJsonElement);
            if (!JsonUtil.isValidJsonElement(merge)) {
                return contentValues;
            }
            JsonObject asJsonObject2 = merge.getAsJsonObject();
            for (Map.Entry entry : asJsonObject2.entrySet()) {
                String str4 = (String) entry.getKey();
                JsonElement jsonElement2 = (JsonElement) entry.getValue();
                if (!str4.equals("csc_customization")) {
                    contentValues.put(str4, jsonElement2.getAsString());
                }
            }
            if (contentValues.size() > 0) {
                contentValues = applyCscCustomizationSwitch(contentValues, asJsonObject2);
            }
            return overrideImsSwitchForCarrier(contentValues);
        }
        IMSLog.d(this.LOG_TAG, "No matched mnoname for SimMobility. Turn all ON");
        String[] strArr = {ImsServiceSwitch.ImsSwitch.DeviceManagement.ENABLE_IMS, ImsServiceSwitch.ImsSwitch.DeviceManagement.ENABLE_VOWIFI, ImsServiceSwitch.ImsSwitch.VoLTE.ENABLE_SMS_IP, ImsServiceSwitch.ImsSwitch.VoLTE.ENABLE_VIDEO_CALL, ImsServiceSwitch.ImsSwitch.VoLTE.ENABLE_VOLTE};
        for (int i3 = 0; i3 < 5; i3++) {
            contentValues.put(strArr[i3], Boolean.TRUE);
        }
        return contentValues;
    }

    /* access modifiers changed from: protected */
    public ContentValues applyCscCustomizationSwitch(ContentValues contentValues, JsonObject jsonObject) {
        JsonArray asJsonArray = jsonObject.getAsJsonArray("csc_customization");
        if (!JsonUtil.isValidJsonElement(asJsonArray)) {
            IMSLog.e(this.LOG_TAG, this.mPhoneId, "applyCscCustomizationSwitch : No csc custom option.");
            return contentValues;
        }
        Iterator it = asJsonArray.iterator();
        while (it.hasNext()) {
            JsonObject asJsonObject = ((JsonElement) it.next()).getAsJsonObject();
            String nWCode = OmcCode.getNWCode(this.mPhoneId);
            String str = this.LOG_TAG;
            int i = this.mPhoneId;
            IMSLog.i(str, i, "applyCscCustomizationSwitch : salesCode [ " + nWCode + " ], csc [ " + asJsonObject.get("csc").getAsString() + " ]");
            if (nWCode.equals(asJsonObject.get("csc").getAsString())) {
                for (Map.Entry entry : asJsonObject.entrySet()) {
                    contentValues.put((String) entry.getKey(), ((JsonElement) entry.getValue()).getAsString());
                }
            }
        }
        return contentValues;
    }
}
