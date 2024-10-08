package com.sec.internal.ims.settings;

import android.content.ContentValues;
import android.content.Context;
import android.content.SharedPreferences;
import com.sec.ims.settings.ImsProfile;
import com.sec.internal.constants.Mno;
import com.sec.internal.helper.CollectionUtils;
import com.sec.internal.helper.ImsSharedPrefHelper;
import com.sec.internal.helper.SimUtil;
import com.sec.internal.helper.SimpleEventLog;
import com.sec.internal.helper.header.AuthenticationHeaders;
import com.sec.internal.ims.util.ConfigUtil;
import com.sec.internal.imscr.LogClass;
import com.sec.internal.log.IMSLog;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.ConcurrentHashMap;
import java.util.stream.Collectors;
import java.util.stream.Stream;

public class ImsServiceSwitch {
    protected static final String IMS_FEATURE_RCS = "rcs";
    protected static final String IMS_FEATURE_VOLTE = "volte";
    protected static final String IMS_FEATURE_VT = "videocall";
    protected static final String IMS_SETTINGS_UPDATED = "android.intent.action.IMS_SETTINGS_UPDATED";
    private static final String LOG_TAG = "ImsServiceSwitch";
    protected static final String SP_KEY_MNONAME = "mnoname";
    private static final List<String> mImsServiceSwitchTable;
    private static final HashMap<String, String> mImsVolteSwitchTable;
    protected boolean isLoded;
    protected Context mContext;
    protected boolean mDefaultSms;
    protected SimpleEventLog mEventLog;
    protected int mPhoneId;
    protected boolean mRcsEnabled;
    protected Map<String, Boolean> mRcsServiceSwitch;
    protected boolean mSsEnabled;
    protected boolean mVoLteEnabled;
    protected Map<String, Boolean> mVolteServiceSwitch;
    protected ImsServiceSwitchBase sInstance;

    public static class ImsSwitch {

        public static class DeviceManagement {
            public static final String ENABLE_IMS = "enableIms";
            public static final String ENABLE_VOWIFI = "enableServiceVowifi";
        }

        public static class RCS {
            public static final String ENABLE_RCS = "enableServiceRcs";
            public static final String ENABLE_RCS_CHAT_SERVICE = "enableServiceRcschat";
        }

        public static class VoLTE {
            public static final String ENABLE_SMS_IP = "enableServiceSmsip";
            public static final String ENABLE_VIDEO_CALL = "enableServiceVilte";
            public static final String ENABLE_VOLTE = "enableServiceVolte";
        }
    }

    public ImsServiceSwitch() {
        this.mVolteServiceSwitch = new ConcurrentHashMap();
        this.mRcsServiceSwitch = new ConcurrentHashMap();
        this.mRcsEnabled = false;
        this.mVoLteEnabled = false;
        this.mSsEnabled = false;
        this.mPhoneId = 0;
        this.sInstance = null;
        this.mDefaultSms = true;
        this.isLoded = true;
    }

    public ImsServiceSwitch(Context context, int i) {
        this.mVolteServiceSwitch = new ConcurrentHashMap();
        this.mRcsServiceSwitch = new ConcurrentHashMap();
        this.mRcsEnabled = false;
        this.mVoLteEnabled = false;
        this.mSsEnabled = false;
        this.sInstance = null;
        this.mDefaultSms = true;
        this.isLoded = true;
        this.mContext = context;
        this.mPhoneId = i;
        makeInstance(SimUtil.getMno(i), i);
    }

    private void makeInstance(Mno mno, int i) {
        IMSLog.d(LOG_TAG, i, "makeInstance: " + mno);
        if (mno.isUSA()) {
            this.sInstance = new ImsServiceSwitchUsa(this.mContext, i);
        } else if (ConfigUtil.isRcsEur(mno) || mno.isOce()) {
            this.sInstance = new ImsServiceSwitchEur(this.mContext, i);
        } else if (mno.isKor()) {
            this.sInstance = new ImsServiceSwitchKor(this.mContext, i);
        } else if (mno.isJpn()) {
            this.sInstance = new ImsServiceSwitchJpn(this.mContext, i);
        } else {
            this.sInstance = new ImsServiceSwitchBase(this.mContext, i);
        }
    }

    public void updateServiceSwitch(ContentValues contentValues) {
        makeInstance(Mno.fromName(contentValues.getAsString("mnoname")), this.mPhoneId);
        this.sInstance.updateServiceSwitch(contentValues);
        this.sInstance.dumpServiceSwitch();
        IMSLog.c(LogClass.SWITCH_UPDATE_DONE, this.mPhoneId + ",UPDATE:" + this.sInstance.getSwitchDump());
    }

    public boolean isRcsEnabled() {
        return this.sInstance.isRcsEnabled();
    }

    public boolean isRcsSwitchEnabled() {
        return this.sInstance.isRcsSwitchEnabled();
    }

    public boolean isEnabled(String str) {
        return this.sInstance.isEnabled(str);
    }

    public boolean isImsEnabled() {
        return this.sInstance.isImsEnabled();
    }

    public boolean isVoLteEnabled() {
        return this.sInstance.isVoLteEnabled();
    }

    public int getVoiceCallType(String str) {
        return this.sInstance.getVoiceCallType(str);
    }

    public int getVideoCallType(String str) {
        return this.sInstance.getVideoCallType(str);
    }

    public int getRcsUserSetting() {
        return this.sInstance.getRcsUserSetting();
    }

    public boolean isDefaultMessageAppInUse() {
        return this.sInstance.isDefaultMessageAppInUse();
    }

    public void enable(String str, boolean z) {
        this.sInstance.enable(str, z);
    }

    public void enableVoLte(boolean z) {
        this.sInstance.enableVoLte(z);
    }

    public void enableRcs(boolean z) {
        this.sInstance.enableRcs(z);
        IMSLog.c(LogClass.SWITCH_ENABLE_RCS, this.mPhoneId + ",RCS SW:" + z);
    }

    public static ContentValues getSimMobilityImsSwitchSetting(Context context, int i) {
        IMSLog.i(LOG_TAG, i, "getSimMobilityImsSwitchSetting according to imsswitch");
        ContentValues contentValues = new ContentValues();
        SharedPreferences sharedPref = ImsSharedPrefHelper.getSharedPref(i, context, "imsswitch", 0, false);
        String[] strArr = {ImsSwitch.DeviceManagement.ENABLE_IMS, ImsSwitch.DeviceManagement.ENABLE_VOWIFI, ImsSwitch.VoLTE.ENABLE_SMS_IP, ImsSwitch.VoLTE.ENABLE_VIDEO_CALL, ImsSwitch.VoLTE.ENABLE_VOLTE};
        for (int i2 = 0; i2 < 5; i2++) {
            String str = strArr[i2];
            contentValues.put(str, Boolean.valueOf(sharedPref.getBoolean(str, false)));
        }
        return contentValues;
    }

    public void dump() {
        this.sInstance.dump();
    }

    public void setVoiceCallType(String str, int i) {
        this.sInstance.setVoiceCallType(str, i);
    }

    public void setVideoCallType(String str, int i) {
        this.sInstance.setVideoCallType(str, i);
    }

    public void setRcsUserSetting(int i) {
        this.sInstance.setRcsUserSetting(i);
    }

    public void doInit() {
        this.sInstance.doInit();
    }

    public void updateSwitchByDynamicUpdate() {
        this.sInstance.loadImsSwitchFromResource();
    }

    /* access modifiers changed from: protected */
    public void dumpServiceSwitch() {
        SimpleEventLog simpleEventLog = this.mEventLog;
        int i = this.mPhoneId;
        simpleEventLog.logAndAdd(i, "dumpServiceSwitch: volte [" + this.mVoLteEnabled + "] rcs [" + this.mRcsEnabled + "]");
        SimpleEventLog simpleEventLog2 = this.mEventLog;
        int i2 = this.mPhoneId;
        StringBuilder sb = new StringBuilder();
        sb.append("dumpServiceSwitch: ");
        sb.append((String) Stream.concat(this.mVolteServiceSwitch.entrySet().stream(), this.mRcsServiceSwitch.entrySet().stream()).map(new ImsServiceSwitch$$ExternalSyntheticLambda0()).collect(Collectors.joining(", ")));
        simpleEventLog2.logAndAdd(i2, sb.toString());
    }

    /* access modifiers changed from: private */
    public static /* synthetic */ String lambda$dumpServiceSwitch$0(Map.Entry entry) {
        return ((String) entry.getKey()) + AuthenticationHeaders.HEADER_PRARAM_SPERATOR + entry.getValue();
    }

    /* access modifiers changed from: protected */
    public String getSwitchDump() {
        StringBuilder sb = new StringBuilder();
        String str = "1";
        sb.append(this.mVoLteEnabled ? str : "0");
        sb.append((String) Arrays.stream(ImsProfile.getVoLteServiceList()).map(new ImsServiceSwitch$$ExternalSyntheticLambda1(this)).collect(Collectors.joining("", "_", ",")));
        if (!this.mRcsEnabled) {
            str = "0";
        }
        sb.append(str);
        sb.append((String) Arrays.stream(ImsProfile.getRcsServiceList()).map(new ImsServiceSwitch$$ExternalSyntheticLambda2(this)).collect(Collectors.joining("", "_", "")));
        return sb.toString();
    }

    /* access modifiers changed from: private */
    public /* synthetic */ String lambda$getSwitchDump$1(String str) {
        return ((Boolean) Optional.ofNullable(this.mVolteServiceSwitch.get(str)).orElse(Boolean.FALSE)).booleanValue() ? "1" : "0";
    }

    /* access modifiers changed from: private */
    public /* synthetic */ String lambda$getSwitchDump$2(String str) {
        return ((Boolean) Optional.ofNullable(this.mRcsServiceSwitch.get(str)).orElse(Boolean.FALSE)).booleanValue() ? "1" : "0";
    }

    /* access modifiers changed from: protected */
    public void saveImsSwitch(ContentValues contentValues) {
        SharedPreferences.Editor edit = ImsSharedPrefHelper.getSharedPref(this.mPhoneId, this.mContext, "imsswitch", 0, false).edit();
        for (String next : getImsServiceSwitchTable()) {
            edit.putBoolean(next, CollectionUtils.getBooleanValue(contentValues, next, false));
        }
        edit.apply();
    }

    static {
        HashMap<String, String> hashMap = new HashMap<>();
        mImsVolteSwitchTable = hashMap;
        ArrayList arrayList = new ArrayList();
        mImsServiceSwitchTable = arrayList;
        hashMap.put(ImsSwitch.VoLTE.ENABLE_VOLTE, "mmtel");
        hashMap.put(ImsSwitch.VoLTE.ENABLE_VIDEO_CALL, "mmtel-video");
        hashMap.put(ImsSwitch.VoLTE.ENABLE_SMS_IP, "smsip");
        arrayList.add(ImsSwitch.DeviceManagement.ENABLE_IMS);
        arrayList.add(ImsSwitch.DeviceManagement.ENABLE_VOWIFI);
        arrayList.add(ImsSwitch.VoLTE.ENABLE_SMS_IP);
        arrayList.add(ImsSwitch.VoLTE.ENABLE_VIDEO_CALL);
        arrayList.add(ImsSwitch.VoLTE.ENABLE_VOLTE);
        arrayList.add(ImsSwitch.RCS.ENABLE_RCS);
        arrayList.add(ImsSwitch.RCS.ENABLE_RCS_CHAT_SERVICE);
    }

    public static HashMap<String, String> getVolteServiceSwitchTable() {
        return mImsVolteSwitchTable;
    }

    public static List<String> getImsServiceSwitchTable() {
        return mImsServiceSwitchTable;
    }

    public static ContentValues getXasImsSwitchSetting() {
        ContentValues contentValues = new ContentValues();
        Boolean bool = Boolean.TRUE;
        contentValues.put(ImsSwitch.DeviceManagement.ENABLE_IMS, bool);
        contentValues.put(ImsSwitch.VoLTE.ENABLE_VOLTE, bool);
        contentValues.put(ImsSwitch.DeviceManagement.ENABLE_VOWIFI, bool);
        contentValues.put(ImsSwitch.VoLTE.ENABLE_SMS_IP, bool);
        contentValues.put(ImsSwitch.RCS.ENABLE_RCS, bool);
        contentValues.put(ImsSwitch.RCS.ENABLE_RCS_CHAT_SERVICE, bool);
        return contentValues;
    }

    public void unregisterObserver() {
        this.sInstance.unregisterObserver();
    }

    public boolean isImsSwitchEnabled(String str) {
        return ImsSharedPrefHelper.getSharedPref(this.mPhoneId, this.mContext, "imsswitch", 0, false).getBoolean(str, false);
    }
}
