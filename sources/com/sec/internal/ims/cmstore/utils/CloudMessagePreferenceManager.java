package com.sec.internal.ims.cmstore.utils;

import android.content.Context;
import android.content.SharedPreferences;
import android.database.Cursor;
import android.database.SQLException;
import android.icu.text.SimpleDateFormat;
import android.net.Uri;
import android.text.TextUtils;
import android.util.Log;
import com.sec.internal.constants.ims.cmstore.McsConstants;
import com.sec.internal.constants.ims.cmstore.ReqConstant;
import com.sec.internal.constants.ims.cmstore.enumprovision.EnumProvision;
import com.sec.internal.constants.ims.cmstore.utils.CloudMessagePreferenceConstants;
import com.sec.internal.ims.cmstore.MessageStoreClient;
import com.sec.internal.ims.cmstore.helper.ATTGlobalVariables;
import com.sec.internal.ims.cmstore.helper.DebugFlag;
import com.sec.internal.ims.cmstore.helper.EventLogHelper;
import com.sec.internal.imscr.LogClass;
import com.sec.internal.log.IMSLog;

public class CloudMessagePreferenceManager {
    private static final String ACS_HAS_NMS = "acs_has_nms";
    private static final String ACS_NMS_HOST = "acs_nms_host";
    private static final String APP_VER = "app_ver";
    private static final String ATS_TOKEN = "ats_token";
    private static final String AUTH_ZCODE = "auth_zcode";
    private static final String BUFFER_DB_LOADED = "buffer_db_loaded";
    private static final String COUNT_USER_INPUT_PHONE_NUMBER = "count_user_input_phone_number";
    private static final String DEVICE_ID = "device_id";
    private static final String FCM_RETRY_COUNT = "fcm_retry_count";
    private static final String HAS_SHOWN_POPUP_OPT_IN = "has_shown_popup_opt_in";
    private static final String HUI_6014_ERR = "6014_err";
    private static final String INITIAL_SYNC_STATUS = "initial sync status";
    private static final String IS_IMSI_FIXED_FOR_ATT_DATABASE = "is_imsi_fixed_for_att_database";
    private static final String IS_NATIVE_MSGAPP_DEFAULT = "is_native_message_app_default";
    private static final String LAST_API_CREATE_SERVICE = "last_api_create_service";
    private static final String LAST_SCREEN = "last_screen";
    private static final String LAST_SCREEN_USER_STOP_BACKUP = "last_screen_where_user_stop_backup";
    private static final String MSG_STORE_TOKEN = "msg_store_token";
    private static final String NC_HOST = "nc_host";
    private static final String NETWORK_OK_TIME = "network_is_available_time";
    private static final String NEW_USER_OPT_IN_CASE = "new_user_opt_in_case";
    private static final String NMS_HOST = "nms_host";
    private static final String NSDS_AUTHORY = "com.samsung.ims.nsds.provider";
    private static final String OBJECT_SEARCH_CURSOR = "object_search_cursor";
    private static final String OMA_CALLBACK_URL = "oma_callback_url";
    private static final String OMA_CHANNELS_CHANNEL_URL = "oma_channels_channel_url";
    private static final String OMA_CHANNELS_RESOURCE_URL = "oma_channels_resources_url";
    private static final String OMA_CHANNEL_CREATE_TIME = "oma_channel_create_lifetime";
    private static final String OMA_CHANNEL_LIFETIME = "oma_channel_lifetime";
    private static final String OMA_RETRY_COUNT = "oma_retry_count";
    private static final String OMA_SUBSCIRPTION_CHANNEL_DURATION = "oma_subscription_channel_duration";
    private static final String OMA_SUBSCIRPTION_RESTART_TOKEN = "oma_subscription_restart_token";
    private static final String OMA_SUBSCIRPTION_RESURL = "oma_subscription_resurl";
    private static final String OMA_SUBSCIRPTION_TIME = "oma_subscription_time";
    private static final String OMA_SUBSCRIPTION_INDEX = "oma_subscription_index";
    private static final String PAT = "cps_pat";
    private static final String PAT_GENERATE_TIME = "pat_generate_time";
    private static final String PREFERENCE_FILE_NAME = "cloudmessage";
    private static final String PREFERENCE_MIGRATE_SUCCESS = "cmsmigratesuccess";
    private static final String PREFERENCE_USER_DEBUG = "cmsuserdebug";
    private static final String PREF_KEY_RETRY_STACK = "Retry_Stack";
    private static final String REDIRECT_DOMAIN = "redirect_domain";
    private static final String SIM_IMSI = "sim_imsi";
    private static final String STEADY_STATE_FLAG = "steady_state_flag";
    private static final String TBS_REQUIRED = "tbs_required";
    private static final String TERM_CONDITION_ID = "T&C";
    private static final String USER_CTN = "user_ctn_id";
    private static final String USER_CTN_IS_INPUT = "is_user_input_ctn";
    private static final String USER_DELETE_ACCOUNT = "user_requested_delete_account";
    private static final String USER_OPT_IN_FLAG = "user_opt_in_flag";
    private static final String ZCODE_COUNTER = "zcode_counter";
    private static final String ZCODE_LAST_REQUEST_ID = "zcode_last_request_id";
    private static SharedPreferences mUserDebugPreference;
    private String TAG = CloudMessagePreferenceManager.class.getSimpleName();
    private Context mContext;
    private SharedPreferences mMigrateSuccessPreference;
    private int mPhoneID = 0;
    private SharedPreferences mPreferences;
    private MessageStoreClient mStoreClient;

    public CloudMessagePreferenceManager(MessageStoreClient messageStoreClient) {
        this.mStoreClient = messageStoreClient;
        this.mPhoneID = messageStoreClient.getClientID();
        this.TAG += "[" + this.mPhoneID + "]";
        Context context = messageStoreClient.getContext();
        this.mContext = context;
        if (this.mPhoneID == 0) {
            this.mPreferences = context.getSharedPreferences(PREFERENCE_FILE_NAME, 0);
        } else {
            this.mPreferences = context.getSharedPreferences(PREFERENCE_FILE_NAME + this.mPhoneID, 0);
        }
        if (mUserDebugPreference == null) {
            mUserDebugPreference = this.mContext.getSharedPreferences(PREFERENCE_USER_DEBUG, 0);
        }
        this.mMigrateSuccessPreference = this.mContext.getSharedPreferences(PREFERENCE_MIGRATE_SUCCESS, 0);
        initUserDebug();
    }

    public void clearAll() {
        Log.d(this.TAG, "clear all preferences data");
        SharedPreferences.Editor prefEditor = getPrefEditor();
        prefEditor.clear();
        prefEditor.apply();
    }

    private SharedPreferences.Editor getPrefEditor() {
        return this.mPreferences.edit();
    }

    public void removeKey(String str) {
        String str2 = this.TAG;
        IMSLog.s(str2, "remove key: " + str);
        SharedPreferences.Editor prefEditor = getPrefEditor();
        prefEditor.remove(str);
        prefEditor.apply();
    }

    public void saveKeyStringValue(String str, String str2) {
        String str3 = this.TAG;
        IMSLog.s(str3, "save key: " + str + ",value: " + str2);
        SharedPreferences.Editor prefEditor = getPrefEditor();
        prefEditor.putString(str, str2);
        prefEditor.apply();
    }

    public String getKeyStringValue(String str, String str2) {
        return this.mPreferences.getString(str, str2);
    }

    public String getKeyStringValueOfUserDebug(String str, String str2) {
        return mUserDebugPreference.getString(str, str2);
    }

    private void saveKeyIntegerValue(String str, int i) {
        String str2 = this.TAG;
        IMSLog.s(str2, "save key: " + str + ",value: " + i);
        SharedPreferences.Editor prefEditor = getPrefEditor();
        prefEditor.putInt(str, i);
        prefEditor.apply();
    }

    private int getKeyIntegerValue(String str, int i) {
        return this.mPreferences.getInt(str, i);
    }

    private void saveKeyBooleanValue(String str, boolean z) {
        String str2 = this.TAG;
        IMSLog.s(str2, "save key: " + str + ",value: " + z);
        SharedPreferences.Editor prefEditor = getPrefEditor();
        prefEditor.putBoolean(str, z);
        prefEditor.apply();
    }

    private boolean getKeyBooleanValue(String str, boolean z) {
        return this.mPreferences.getBoolean(str, z);
    }

    private void saveKeyLongValue(String str, long j) {
        String str2 = this.TAG;
        IMSLog.s(str2, "save key: " + str + ",value: " + j);
        SharedPreferences.Editor prefEditor = getPrefEditor();
        prefEditor.putLong(str, j);
        prefEditor.apply();
    }

    private long getKeyLongValue(String str, long j) {
        return this.mPreferences.getLong(str, j);
    }

    public boolean isEmptyPref() {
        SharedPreferences sharedPreferences = this.mPreferences;
        return sharedPreferences == null || sharedPreferences.getAll().size() == 0;
    }

    public int getMcsUser() {
        int keyIntegerValue = getKeyIntegerValue(McsConstants.McsSharedPref.PREF_MCS_USER, -1);
        logMcsRegistrationStatus(keyIntegerValue);
        return keyIntegerValue;
    }

    public void saveMcsUser(int i) {
        logMcsRegistrationStatus(i);
        IMSLog.c(LogClass.MCS_PV_REGI_STATUS, this.mPhoneID + ",PV:" + i);
        saveKeyIntegerValue(McsConstants.McsSharedPref.PREF_MCS_USER, i);
    }

    public void logMcsRegistrationStatus(int i) {
        if (i == 0) {
            IMSLog.i(this.TAG, "MCS registration : false");
        } else if (i == 1) {
            IMSLog.i(this.TAG, "MCS registration : true");
        } else {
            IMSLog.i(this.TAG, "MCS registration : unknown");
        }
    }

    public String getAuthCode() {
        return getKeyStringValue(McsConstants.McsSharedPref.PREF_AUTHENTICATION_CODE, "");
    }

    public void saveAuthCode(String str) {
        saveKeyStringValue(McsConstants.McsSharedPref.PREF_AUTHENTICATION_CODE, str);
    }

    public String getRegCode() {
        return getKeyStringValue("registration_code", "");
    }

    public void saveRegCode(String str) {
        saveKeyStringValue("registration_code", str);
    }

    public String getOasisAuthRoot() {
        String keyStringValueOfUserDebug = getKeyStringValueOfUserDebug(DebugFlag.MCS_URL, McsConstants.Auth.ROOT_URL);
        if (!McsConstants.Auth.ROOT_URL.equals(keyStringValueOfUserDebug)) {
            return keyStringValueOfUserDebug;
        }
        return McsConstants.Auth.ROOT_URL;
    }

    public String getOasisServerRoot() {
        return getKeyStringValue("oasis_server_root", "");
    }

    public void saveOasisServerRoot(String str) {
        saveKeyStringValue("oasis_server_root", str);
    }

    public String getMcsAccessToken() {
        return getKeyStringValue("access_token", "");
    }

    public void saveMcsAccessToken(String str) {
        saveKeyStringValue("access_token", str);
    }

    public long getMcsAccessTokenExpireTime() {
        return getKeyLongValue(McsConstants.McsSharedPref.PREF_ACCESS_TOKEN_EXPIRE_TIME, 0);
    }

    public void saveMcsAccessTokenExpireTime(long j) {
        saveKeyLongValue(McsConstants.McsSharedPref.PREF_ACCESS_TOKEN_EXPIRE_TIME, j);
    }

    public String getMcsRefreshToken() {
        return getKeyStringValue("refresh_token", "");
    }

    public void saveMcsRefreshToken(String str) {
        saveKeyStringValue("refresh_token", str);
    }

    public long getMcsRefreshTokenExpireTime() {
        return getKeyLongValue(McsConstants.McsSharedPref.PREF_REFRESH_TOKEN_EXPIRE_TIME, 0);
    }

    public void saveMcsRefreshTokenExpireTime(long j) {
        saveKeyLongValue(McsConstants.McsSharedPref.PREF_REFRESH_TOKEN_EXPIRE_TIME, j);
    }

    public int getCmsDataTtl() {
        return getKeyIntegerValue("cms_data_ttl", McsConstants.ServerConfig.DEFAULT_CMS_TTL_VALUE);
    }

    public void saveCmsDataTtl(int i) {
        IMSLog.c(LogClass.MCS_SYNC_TTL_VALUE, this.mPhoneID + ",cms:" + i);
        String str = this.TAG;
        int i2 = this.mPhoneID;
        EventLogHelper.infoLogAndAdd(str, i2, "cmsDataTtl: " + i);
        if (i <= 0) {
            i = McsConstants.ServerConfig.DEFAULT_CMS_TTL_VALUE;
        }
        saveKeyIntegerValue("cms_data_ttl", i);
    }

    public String getFcmSenderId() {
        return getKeyStringValue("fcm_sender_id", "");
    }

    public void saveFcmSenderId(String str) {
        saveKeyStringValue("fcm_sender_id", str);
    }

    public int getMaxUploadFileSize() {
        return getKeyIntegerValue("max_upload_file_size", 0);
    }

    public void saveMaxUploadFileSize(int i) {
        saveKeyIntegerValue("max_upload_file_size", i);
    }

    public int getMaxSmallFileSize() {
        return getKeyIntegerValue("max_small_file_size", 5);
    }

    public void saveMaxSmallFileSize(int i) {
        saveKeyIntegerValue("max_small_file_size", i);
    }

    public String getOasisSmallFileServerRoot() {
        return getKeyStringValue("oasis_small_file_server_root", "");
    }

    public void saveOasisSmallFileServerRoot(String str) {
        saveKeyStringValue("oasis_small_file_server_root", str);
    }

    public String getOasisLargeFileServerRoot() {
        return getKeyStringValue("oasis_large_file_server_root", "");
    }

    public void saveOasisLargeFileServerRoot(String str) {
        saveKeyStringValue("oasis_large_file_server_root", str);
    }

    public String getOasisServerVersion() {
        return getKeyStringValue(McsConstants.McsSharedPref.PREF_OASIS_SERVER_VERSION, McsConstants.ServerConfig.OASIS_VERSION);
    }

    public void saveOasisServerVersion(String str) {
        saveKeyStringValue(McsConstants.McsSharedPref.PREF_OASIS_SERVER_VERSION, str);
    }

    public int getMmsRevokeTtlSecs() {
        return getKeyIntegerValue("mms_revoke_ttl_secs", 86400);
    }

    public void saveMmsRevokeTtlSecs(int i) {
        IMSLog.c(LogClass.MCS_SYNC_TTL_VALUE, this.mPhoneID + ",mms:" + i);
        String str = this.TAG;
        int i2 = this.mPhoneID;
        EventLogHelper.infoLogAndAdd(str, i2, "mmsRevokeTtlSecs: " + i);
        if (i <= 0) {
            i = 86400;
        }
        saveKeyIntegerValue("mms_revoke_ttl_secs", i);
    }

    public int getSmsRevokeTtlSecs() {
        return getKeyIntegerValue("sms_revoke_ttl_secs", 86400);
    }

    public void saveSmsRevokeTtlSecs(int i) {
        IMSLog.c(LogClass.MCS_SYNC_TTL_VALUE, this.mPhoneID + ",sms:" + i);
        String str = this.TAG;
        int i2 = this.mPhoneID;
        EventLogHelper.infoLogAndAdd(str, i2, "smsRevokeTtlSecs: " + i);
        if (i <= 0) {
            i = 86400;
        }
        saveKeyIntegerValue("sms_revoke_ttl_secs", i);
    }

    public String getFcmRegistrationToken() {
        return getKeyStringValue(McsConstants.McsSharedPref.PREF_FCM_REGISTRATION_TOKEN, "");
    }

    public void saveFcmRegistrationToken(String str) {
        saveKeyStringValue(McsConstants.McsSharedPref.PREF_FCM_REGISTRATION_TOKEN, str);
    }

    public String getMcsAccountId() {
        return getKeyStringValue("account_id", "");
    }

    public void saveMcsAccountId(String str) {
        saveKeyStringValue("account_id", str);
    }

    public String getMcsAlias() {
        return getKeyStringValue("alias", "");
    }

    public void saveMcsAlias(String str) {
        saveKeyStringValue("alias", str);
    }

    public String getZCodeLastRequestId(String str) {
        return getKeyStringValue(ZCODE_LAST_REQUEST_ID, str);
    }

    public void saveZCodeLastRequestId(String str) {
        saveKeyStringValue(ZCODE_LAST_REQUEST_ID, str);
    }

    public void saveAppVer(String str) {
        saveKeyStringValue(APP_VER, str);
    }

    public String getTermConditionId() {
        return getKeyStringValue(TERM_CONDITION_ID, "");
    }

    public void saveTermConditionId(String str) {
        saveKeyStringValue(TERM_CONDITION_ID, str);
    }

    public void saveAuthZCode(String str) {
        saveKeyStringValue(AUTH_ZCODE, str);
    }

    public String getAuthZCode() {
        return getKeyStringValue(AUTH_ZCODE, "");
    }

    public void saveSimImsi(String str) {
        if (!TextUtils.isEmpty(str)) {
            saveKeyStringValue("sim_imsi", str);
        }
    }

    public String getSimImsi() {
        return getKeyStringValue("sim_imsi", "");
    }

    public void saveUserTbsRquired(boolean z) {
        saveKeyBooleanValue(TBS_REQUIRED, z);
    }

    public boolean getUserTbs() {
        return getKeyBooleanValue(TBS_REQUIRED, false);
    }

    public void saveUserCtn(String str, boolean z) {
        if (!TextUtils.isEmpty(str)) {
            saveKeyStringValue(USER_CTN, str);
            saveKeyBooleanValue(USER_CTN_IS_INPUT, z);
        }
    }

    public void clearInvalidUserCtn() {
        removeKey(USER_CTN);
        removeKey(USER_CTN_IS_INPUT);
    }

    public String getUserCtn() {
        return getKeyStringValue(USER_CTN, "");
    }

    /* JADX WARNING: Code restructure failed: missing block: B:2:0x0012, code lost:
        r2 = com.sec.internal.ims.cmstore.utils.Util.getNormalizedTelUri(r0, r2);
     */
    /* Code decompiled incorrectly, please refer to instructions dump. */
    public java.lang.String getUserTelCtn() {
        /*
            r2 = this;
            java.lang.String r0 = r2.getUserCtn()
            android.content.Context r1 = r2.mContext
            int r2 = r2.mPhoneID
            java.lang.String r2 = com.sec.internal.ims.cmstore.utils.Util.getSimCountryCode(r1, r2)
            boolean r1 = android.text.TextUtils.isEmpty(r0)
            if (r1 != 0) goto L_0x001d
            com.sec.ims.util.ImsUri r2 = com.sec.internal.ims.cmstore.utils.Util.getNormalizedTelUri(r0, r2)
            if (r2 == 0) goto L_0x001d
            java.lang.String r2 = r2.toString()
            return r2
        L_0x001d:
            java.lang.String r2 = ""
            return r2
        */
        throw new UnsupportedOperationException("Method not decompiled: com.sec.internal.ims.cmstore.utils.CloudMessagePreferenceManager.getUserTelCtn():java.lang.String");
    }

    public boolean getIsUserInputCtn() {
        return getKeyBooleanValue(USER_CTN_IS_INPUT, false);
    }

    public void increaseUserInputNumberCount() {
        saveKeyIntegerValue(COUNT_USER_INPUT_PHONE_NUMBER, getKeyIntegerValue(COUNT_USER_INPUT_PHONE_NUMBER, 0) + 1);
    }

    public void removeUserInputNumberCount() {
        SharedPreferences.Editor prefEditor = getPrefEditor();
        prefEditor.remove(COUNT_USER_INPUT_PHONE_NUMBER);
        prefEditor.apply();
    }

    public boolean isNoMoreChanceUserInputNumber() {
        return getKeyIntegerValue(COUNT_USER_INPUT_PHONE_NUMBER, 0) >= 2;
    }

    public void increazeZCodeCounter() {
        saveKeyIntegerValue(ZCODE_COUNTER, getKeyIntegerValue(ZCODE_COUNTER, 0) + 1);
    }

    public boolean isZCodeMax2Tries() {
        return getKeyIntegerValue(ZCODE_COUNTER, 0) >= 1;
    }

    public void removeZCodeCounter() {
        SharedPreferences.Editor prefEditor = getPrefEditor();
        prefEditor.remove(ZCODE_COUNTER);
        prefEditor.apply();
    }

    public void saveUserDeleteAccount(boolean z) {
        saveKeyBooleanValue(USER_DELETE_ACCOUNT, z);
    }

    public boolean hasUserDeleteAccount() {
        boolean keyBooleanValue = getKeyBooleanValue(USER_DELETE_ACCOUNT, false);
        String str = this.TAG;
        Log.i(str, "hasUserDeleteAccount: " + keyBooleanValue);
        return keyBooleanValue;
    }

    public int getLastScreen() {
        return getKeyIntegerValue(LAST_SCREEN, -1);
    }

    public void saveLastScreen(int i) {
        saveKeyIntegerValue(LAST_SCREEN, i);
    }

    public int getLastScreenUserStopBackup() {
        return getKeyIntegerValue(LAST_SCREEN_USER_STOP_BACKUP, -1);
    }

    public void saveLastScreenUserStopBackup(int i) {
        saveKeyIntegerValue(LAST_SCREEN_USER_STOP_BACKUP, i);
    }

    public void saveAtsToken(String str) {
        saveKeyStringValue(ATS_TOKEN, str);
    }

    public String getAtsToken() {
        String keyStringValue = getKeyStringValue(ATS_TOKEN, "");
        String str = this.TAG;
        Log.i(str, "atsToken: " + keyStringValue);
        return keyStringValue;
    }

    public void saveMsgStoreSessionId(String str) {
        saveKeyStringValue(MSG_STORE_TOKEN, str);
    }

    public String getMsgStoreSessionId() {
        return getKeyStringValue(MSG_STORE_TOKEN, "");
    }

    public void saveNmsHost(String str) {
        saveKeyStringValue(NMS_HOST, str);
    }

    public String getNmsHost() {
        return getKeyStringValue(NMS_HOST, "");
    }

    public void saveAcsNmsHost(String str) {
        saveKeyStringValue(ACS_NMS_HOST, str);
    }

    public String getAcsNmsHost() {
        return getKeyStringValue(ACS_NMS_HOST, "");
    }

    public void saveRedirectDomain(String str) {
        saveKeyStringValue(REDIRECT_DOMAIN, str);
    }

    public String getRedirectDomain() {
        return getKeyStringValue(REDIRECT_DOMAIN, "");
    }

    public void savePATAndTime(String str) {
        saveKeyStringValue(PAT, str);
        saveKeyLongValue(PAT_GENERATE_TIME, System.currentTimeMillis());
    }

    public String getValidPAT() {
        String keyStringValue = getKeyStringValue(PAT, (String) null);
        if (!TextUtils.isEmpty(keyStringValue)) {
            if (System.currentTimeMillis() - getKeyLongValue(PAT_GENERATE_TIME, System.currentTimeMillis()) < ReqConstant.PAT_LIFE_CYCLE) {
                return keyStringValue;
            }
        }
        return null;
    }

    public boolean hasUserOptedIn() {
        return getKeyBooleanValue(USER_OPT_IN_FLAG, false);
    }

    public void saveUserOptedIn(boolean z) {
        saveKeyBooleanValue(USER_OPT_IN_FLAG, z);
    }

    public boolean hasShownPopupOptIn() {
        return getKeyBooleanValue(HAS_SHOWN_POPUP_OPT_IN, false);
    }

    public void saveIfHasShownPopupOptIn(boolean z) {
        saveKeyBooleanValue(HAS_SHOWN_POPUP_OPT_IN, z);
    }

    public int getNewUserOptInCase() {
        return getKeyIntegerValue(NEW_USER_OPT_IN_CASE, EnumProvision.NewUserOptInCase.DEFAULT.getId());
    }

    public void saveNewUserOptInCase(int i) {
        saveKeyIntegerValue(NEW_USER_OPT_IN_CASE, i);
    }

    public boolean isLastAPIRequestCreateAccount() {
        return getKeyBooleanValue(LAST_API_CREATE_SERVICE, false);
    }

    public void saveLastApiRequestCreateAccount(boolean z) {
        saveKeyBooleanValue(LAST_API_CREATE_SERVICE, z);
    }

    public void saveAMBSStopService(boolean z) {
        String str = this.TAG;
        Log.i(str, "saveAMBSStopService :" + z);
        saveKeyBooleanValue(CloudMessagePreferenceConstants.AMBS_STOP_SERVICE, z);
    }

    public boolean getAMBSStopService() {
        boolean keyBooleanValue = getKeyBooleanValue(CloudMessagePreferenceConstants.AMBS_STOP_SERVICE, false);
        String str = this.TAG;
        Log.i(str, "getAMBSStopService :" + keyBooleanValue);
        return keyBooleanValue;
    }

    public void saveAMBSPauseService(boolean z) {
        String str = this.TAG;
        Log.i(str, "saveAMBSPauseService :" + z);
        saveKeyBooleanValue(CloudMessagePreferenceConstants.AMBS_PAUSE_SERVICE, z);
    }

    public boolean getAMBSPauseService() {
        boolean keyBooleanValue = getKeyBooleanValue(CloudMessagePreferenceConstants.AMBS_PAUSE_SERVICE, false);
        String str = this.TAG;
        Log.i(str, "getAMBSPauseService :" + keyBooleanValue);
        return keyBooleanValue;
    }

    public boolean isHUI6014Err() {
        return getKeyBooleanValue(HUI_6014_ERR, false);
    }

    public void saveIfHUI6014Err(boolean z) {
        saveKeyBooleanValue(HUI_6014_ERR, z);
    }

    public void saveIfSteadyState(boolean z) {
        saveKeyBooleanValue(STEADY_STATE_FLAG, z);
    }

    public boolean ifSteadyState() {
        return getKeyBooleanValue(STEADY_STATE_FLAG, false);
    }

    public void saveNativeMsgAppIsDefault(boolean z) {
        saveKeyBooleanValue(IS_NATIVE_MSGAPP_DEFAULT, z);
    }

    public int getTotalRetryCounter() {
        return getKeyIntegerValue(CloudMessagePreferenceConstants.RETRY_TOTAL_COUNTER, 0);
    }

    public void saveTotalRetryCounter(int i) {
        saveKeyIntegerValue(CloudMessagePreferenceConstants.RETRY_TOTAL_COUNTER, i);
    }

    public void saveLastRetryTime(long j) {
        saveKeyLongValue(CloudMessagePreferenceConstants.LAST_RETRY_TIME, j);
    }

    public String getRetryStackData() {
        return getKeyStringValue(PREF_KEY_RETRY_STACK, "");
    }

    public void saveLastVVMStatus(String str) {
        saveKeyStringValue(CloudMessagePreferenceConstants.VVM_ON_STATUS, str);
    }

    public String getLastVVMStatus() {
        return getKeyStringValue(CloudMessagePreferenceConstants.VVM_ON_STATUS, "");
    }

    public void saveRetryStackData(String str) {
        saveKeyStringValue(PREF_KEY_RETRY_STACK, str);
    }

    public void saveInitialSyncStatus(int i) {
        saveKeyIntegerValue(INITIAL_SYNC_STATUS, i);
    }

    public void saveObjectSearchCursor(String str) {
        saveKeyStringValue(OBJECT_SEARCH_CURSOR, str);
    }

    public void saveNcHost(String str) {
        saveKeyStringValue(NC_HOST, str);
    }

    public String getNcHost() {
        return getKeyStringValue(NC_HOST, "");
    }

    public void saveOMAChannelResURL(String str) {
        saveKeyStringValue(OMA_CHANNELS_RESOURCE_URL, str);
    }

    public String getOMAChannelResURL() {
        return getKeyStringValue(OMA_CHANNELS_RESOURCE_URL, "");
    }

    public void saveOMAChannelURL(String str) {
        saveKeyStringValue(OMA_CHANNELS_CHANNEL_URL, str);
    }

    public String getOMAChannelURL() {
        return getKeyStringValue(OMA_CHANNELS_CHANNEL_URL, "");
    }

    public void saveOMACallBackURL(String str) {
        saveKeyStringValue(OMA_CALLBACK_URL, str);
    }

    public String getOMACallBackURL() {
        return getKeyStringValue(OMA_CALLBACK_URL, "");
    }

    public void saveOMASubscriptionIndex(long j) {
        saveKeyLongValue(OMA_SUBSCRIPTION_INDEX, j);
    }

    public long getOMASubscriptionIndex() {
        return getKeyLongValue(OMA_SUBSCRIPTION_INDEX, 0);
    }

    public void saveOMASubscriptionRestartToken(String str) {
        saveKeyStringValue(OMA_SUBSCIRPTION_RESTART_TOKEN, str);
    }

    public String getOMASSubscriptionRestartToken() {
        return getKeyStringValue(OMA_SUBSCIRPTION_RESTART_TOKEN, (String) null);
    }

    public void saveOMASubscriptionTime(long j) {
        saveKeyLongValue(OMA_SUBSCIRPTION_TIME, j);
    }

    public long getOMASubscriptionTime() {
        return getKeyLongValue(OMA_SUBSCIRPTION_TIME, 0);
    }

    public void clearOMASubscriptionTime() {
        removeKey(OMA_SUBSCIRPTION_TIME);
    }

    public void saveOMASubscriptionChannelDuration(int i) {
        saveKeyIntegerValue(OMA_SUBSCIRPTION_CHANNEL_DURATION, i);
    }

    public int getOMASubscriptionChannelDuration() {
        return getKeyIntegerValue(OMA_SUBSCIRPTION_CHANNEL_DURATION, 0);
    }

    public void clearOMASubscriptionChannelDuration() {
        removeKey(OMA_SUBSCIRPTION_CHANNEL_DURATION);
    }

    public void saveOMAChannelLifeTime(long j) {
        saveKeyLongValue(OMA_CHANNEL_LIFETIME, j);
    }

    public long getOMAChannelLifeTime() {
        return getKeyLongValue(OMA_CHANNEL_LIFETIME, 0);
    }

    public void saveOMAChannelCreateTime(long j) {
        saveKeyLongValue(OMA_CHANNEL_CREATE_TIME, j);
    }

    public long getOMAChannelCreateTime() {
        return getKeyLongValue(OMA_CHANNEL_CREATE_TIME, 0);
    }

    public void saveOMASubscriptionResUrl(String str) {
        saveKeyStringValue(OMA_SUBSCIRPTION_RESURL, str);
    }

    public String getOMASubscriptionResUrl() {
        return getKeyStringValue(OMA_SUBSCIRPTION_RESURL, "");
    }

    public int getOmaRetryCounter() {
        return getKeyIntegerValue(OMA_RETRY_COUNT, 0);
    }

    public void saveOmaRetryCounter(int i) {
        saveKeyIntegerValue(OMA_RETRY_COUNT, i);
    }

    public int getFcmRetryCount() {
        return getKeyIntegerValue(FCM_RETRY_COUNT, 0);
    }

    public void saveFcmRetryCount(int i) {
        saveKeyIntegerValue(FCM_RETRY_COUNT, i);
    }

    public boolean getBufferDbLoaded() {
        return getKeyBooleanValue(BUFFER_DB_LOADED, false);
    }

    public void saveBufferDbLoaded(boolean z) {
        saveKeyBooleanValue(BUFFER_DB_LOADED, z);
    }

    public void saveDeviceId(String str) {
        saveKeyStringValue("device_id", str);
    }

    public String getDeviceId() {
        String keyStringValue = getKeyStringValue("device_id", "");
        if (TextUtils.isEmpty(keyStringValue)) {
            keyStringValue = Util.getImei(this.mStoreClient);
            if (TextUtils.isEmpty(keyStringValue)) {
                Log.d(this.TAG, "can't get imei from sp and telephonymgr");
                return "";
            }
            saveDeviceId(keyStringValue);
        }
        return keyStringValue;
    }

    public boolean isDebugEnable() {
        SharedPreferences sharedPreferences = mUserDebugPreference;
        if (sharedPreferences == null) {
            return false;
        }
        return sharedPreferences.getBoolean(DebugFlag.DEBUG_FLAG, false);
    }

    public String getGcmTokenFromVsim() {
        Cursor query;
        String str = null;
        try {
            query = this.mContext.getContentResolver().query(Uri.parse("content://com.samsung.ims.nsds.provider/devices/push_token"), (String[]) null, (String) null, (String[]) null, (String) null);
            if (query == null) {
                if (query != null) {
                    query.close();
                }
                return null;
            }
            while (query.moveToNext()) {
                str = query.getString(query.getColumnIndex("device_push_token"));
            }
            query.close();
            return str;
        } catch (SQLException | IllegalArgumentException e) {
            String str2 = this.TAG;
            Log.e(str2, "!!!Could not get data from db " + e.toString());
        } catch (Throwable th) {
            th.addSuppressed(th);
        }
        throw th;
    }

    public void saveNetworkAvailableTime(long j) {
        new SimpleDateFormat("YYYY-MM-dd HH:mm:ss");
        saveKeyLongValue(NETWORK_OK_TIME, j);
    }

    public long getNetworkAvailableTime() {
        return getKeyLongValue(NETWORK_OK_TIME, -1);
    }

    public void saveMigrateSuccessFlag(boolean z) {
        String str = this.TAG;
        Log.d(str, "saveMigrateSuccess is " + z);
        SharedPreferences sharedPreferences = this.mMigrateSuccessPreference;
        if (sharedPreferences != null) {
            SharedPreferences.Editor edit = sharedPreferences.edit();
            edit.putBoolean(IS_IMSI_FIXED_FOR_ATT_DATABASE, z);
            edit.apply();
        }
    }

    public boolean getMigrateSuccessFlag() {
        Log.d(this.TAG, "getMigrateSuccessFlag ");
        SharedPreferences sharedPreferences = this.mMigrateSuccessPreference;
        if (sharedPreferences == null) {
            return false;
        }
        return sharedPreferences.getBoolean(IS_IMSI_FIXED_FOR_ATT_DATABASE, false);
    }

    public void initUserDebug() {
        SharedPreferences sharedPreferences = mUserDebugPreference;
        if (sharedPreferences == null) {
            IMSLog.s(this.TAG, "mUserDebugPreference is null failed to init");
            return;
        }
        boolean z = sharedPreferences.getBoolean(DebugFlag.DEBUG_FLAG, false);
        String str = this.TAG;
        Log.i(str, "debug preference :" + z);
        if (z) {
            String string = mUserDebugPreference.getString("app_id", "");
            String string2 = mUserDebugPreference.getString(DebugFlag.CPS_HOST_NAME, "");
            String string3 = mUserDebugPreference.getString(DebugFlag.AUTH_HOST_NAME, "");
            String string4 = mUserDebugPreference.getString(DebugFlag.RETRY_TIME, "");
            ATTGlobalVariables.setValue(string, string3, string2, mUserDebugPreference.getString(DebugFlag.NC_HOST_NAME, ""));
            DebugFlag.DEBUG_RETRY_TIMELINE_FLAG = true;
            DebugFlag.setRetryTimeLine(string4);
            DebugFlag.DEBUG_MCS_URL = mUserDebugPreference.getString(DebugFlag.MCS_URL, DebugFlag.DEBUG_MCS_URL);
        } else {
            ATTGlobalVariables.initDefault();
            DebugFlag.DEBUG_RETRY_TIMELINE_FLAG = false;
            DebugFlag.initRetryTimeLine();
        }
        DebugFlag.DEBUG_OASIS_VERSION = getOasisServerVersion();
        String str2 = this.TAG;
        IMSLog.s(str2, "appId=" + ATTGlobalVariables.APP_ID + ", cpsHostName=" + ATTGlobalVariables.CPS_HOST_NAME + ", authHostName=" + ATTGlobalVariables.ACMS_HOST_NAME + ", ncHostName=" + ATTGlobalVariables.DEFAULT_PRODUCT_NC_HOST + "timeLine=" + DebugFlag.debugRetryTimeLine + ", mcsUrl=" + DebugFlag.DEBUG_MCS_URL + ", oasisVersion=" + DebugFlag.DEBUG_OASIS_VERSION);
    }

    public void saveUserDebug() {
        SharedPreferences sharedPreferences = mUserDebugPreference;
        if (sharedPreferences == null) {
            String str = this.TAG;
            IMSLog.s(str, "mUserDebugPreference is null failed to save, debug:" + DebugFlag.DEBUG_RETRY_TIMELINE_FLAG);
            return;
        }
        SharedPreferences.Editor edit = sharedPreferences.edit();
        edit.putBoolean(DebugFlag.DEBUG_FLAG, DebugFlag.DEBUG_RETRY_TIMELINE_FLAG);
        edit.putString("app_id", ATTGlobalVariables.APP_ID);
        edit.putString(DebugFlag.CPS_HOST_NAME, ATTGlobalVariables.CPS_HOST_NAME);
        edit.putString(DebugFlag.AUTH_HOST_NAME, ATTGlobalVariables.ACMS_HOST_NAME);
        edit.putString(DebugFlag.RETRY_TIME, DebugFlag.debugRetryTimeLine);
        edit.putString(DebugFlag.NC_HOST_NAME, ATTGlobalVariables.DEFAULT_PRODUCT_NC_HOST);
        edit.putString(DebugFlag.MCS_URL, DebugFlag.DEBUG_MCS_URL);
        edit.putString(DebugFlag.OASIS_VERSION, DebugFlag.DEBUG_OASIS_VERSION);
        edit.apply();
    }
}
