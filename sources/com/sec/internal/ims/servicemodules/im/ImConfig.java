package com.sec.internal.ims.servicemodules.im;

import android.content.Context;
import android.net.Uri;
import android.text.TextUtils;
import android.util.Base64;
import android.util.Log;
import com.sec.ims.settings.ImsProfile;
import com.sec.ims.util.ImsUri;
import com.sec.internal.constants.Mno;
import com.sec.internal.constants.ims.config.ConfigConstants;
import com.sec.internal.constants.ims.servicemodules.im.ImConstants;
import com.sec.internal.constants.ims.servicemodules.im.ImDefaultConst;
import com.sec.internal.constants.ims.servicemodules.im.ImSettings;
import com.sec.internal.constants.ims.settings.GlobalSettingsConstants;
import com.sec.internal.helper.OmcCode;
import com.sec.internal.helper.RcsConfigurationHelper;
import com.sec.internal.helper.SimUtil;
import com.sec.internal.helper.os.ITelephonyManager;
import com.sec.internal.helper.os.TelephonyManagerWrapper;
import com.sec.internal.ims.config.ConfigContract;
import com.sec.internal.ims.core.handler.secims.imsCommonStruc.MNO;
import com.sec.internal.ims.registry.ImsRegistry;
import com.sec.internal.ims.util.ConfigUtil;
import com.sec.internal.ims.util.RcsSettingsUtils;
import com.sec.internal.ims.util.TapiServiceUtil;
import com.sec.internal.log.IMSLog;
import java.util.HashMap;
import java.util.Map;

public class ImConfig {
    private static final String LOG_TAG = "ImConfig";
    public static final long UNDEFINED_MAX_SIZE_FILE_TR_INCOMING = -1;
    private static Map<Integer, ImConfig> sInstances = new HashMap();
    private int m1ToManySelectedTech;
    private boolean mAutAccept;
    private boolean mAutAcceptGroupChat;
    private boolean mBotPrivacyDisable;
    private int mCallComposerTimerIdle;
    private Uri mCatalogUri;
    private Uri mCbftHTTPCSURI;
    private boolean mCfsTrigger;
    private boolean mChatEnabled;
    private int mChatRevokeTimer;
    private ImConstants.ChatbotMsgTechConfig mChatbotMsgTech;
    private ImsUri mConfFctyUri;
    private ImsUri mDeferredMsgFuncUri;
    private int mDisplayNotificationSwitch;
    private boolean mEnableFtAutoResumable;
    private boolean mEnableGroupChatListRetrieve;
    private ImsUri mExploderUri;
    protected long mExtAttImMSRPFtMaxSize;
    protected int mExtAttImSlmMaxRecipients;
    private boolean mFirstMsgInvite;
    private boolean mFtAutAccept;
    private boolean mFtAutAcceptOriginalConfig;
    private boolean mFtCancelMemoryFull;
    private boolean mFtCapAlwaysOn;
    private ImConstants.FtMech mFtDefaultMech;
    private boolean mFtEnabled;
    private boolean mFtFallbackAllFail;
    private int mFtFbDefault;
    private Uri mFtHTTPExtraCSURI;
    private boolean mFtHttpCapAlwaysOn;
    private String mFtHttpCsPwd;
    private Uri mFtHttpCsUri;
    private String mFtHttpCsUser;
    private String mFtHttpDLUrl;
    private boolean mFtHttpEnabled;
    private int mFtHttpFallback;
    private boolean mFtHttpTrustAllCerts;
    private int mFtMax1ToManyRecipients;
    private boolean mFtStAndFwEnabled;
    private boolean mFtThumb;
    private long mFtWarnSize;
    private boolean mGlsPullEnabled;
    private boolean mGlsPushEnabled;
    private boolean mGroupChatEnabled;
    private boolean mGroupChatFullStandFwd;
    private boolean mGroupChatOnlyFStandFwd;
    private boolean mImCapAlwaysOn;
    private boolean mImCapNonRcs;
    private ImConstants.ImMsgTech mImMsgTech;
    private ImConstants.ImSessionStart mImSessionStart;
    private boolean mImWarnIw;
    private boolean mImWarnSf;
    private boolean mIsAggrImdnSupported;
    private boolean mIsFullSFGroupChat;
    private boolean mJoynIntegratedMessaging;
    private boolean mLegacyLatching;
    private int mMax1ToManyRecipients;
    private int mMaxAdhocGroupSize;
    private int mMaxConcurrentSession;
    private long mMaxSize;
    private long mMaxSize1To1;
    private long mMaxSize1ToM;
    private long mMaxSizeExtraFileTr;
    private long mMaxSizeFileTr;
    private long mMaxSizeFileTrIncoming;
    private ImConstants.MessagingUX mMessagingUX;
    private int mMsgCapValidityTime;
    private int mMsgFbDefault;
    private boolean mMultiMediaChat;
    private int mPagerModeLimit;
    private final int mPhoneId;
    private boolean mPlugInEnabled;
    private boolean mPresSrvCap;
    private String mPublicAccountAddr;
    private String mRcsProfile = "";
    private boolean mRealtimeUserAliasAuth;
    private int mReconnectGuardTimer;
    private boolean mRespondDisplay;
    private int mServiceAvailabilityInfoExpiry;
    private ImConstants.SlmAuth mSlmAuth;
    private long mSlmMaxMsgSize;
    private int mSlmSwitchOverSize;
    private boolean mSmsFallbackAuth;
    private ITelephonyManager mTelephony;
    private int mTimerIdle;
    private String mUserAgent;
    private String mUserAlias;
    private boolean mUserAliasEnabled;
    private boolean mfThttpDefaultPdn;

    private ImConfig(int i) {
        this.mPhoneId = i;
    }

    public static synchronized ImConfig getInstance(int i) {
        ImConfig imConfig;
        synchronized (ImConfig.class) {
            imConfig = sInstances.get(Integer.valueOf(i));
            if (imConfig == null) {
                imConfig = new ImConfig(i);
                sInstances.put(Integer.valueOf(i), imConfig);
            }
        }
        return imConfig;
    }

    public void load(Context context, String str, boolean z) {
        RcsConfigurationHelper.ConfigData configData = RcsConfigurationHelper.getConfigData(context, "root/*", this.mPhoneId);
        this.mRcsProfile = str;
        initRcsConfiguration(context);
        loadGlobalSettings(context);
        loadRcsConfiguration(context, configData);
        updateRcsConfiguration(context, configData);
        loadUserAlias(context);
        setFtAutAccept(context, ImUserPreference.getInstance().getFtAutAccept(context, this.mPhoneId), z);
        this.mRespondDisplay = ImDefaultConst.DEFAULT_CHAT_RESPOND_TO_DISPLAY_REPORTS.booleanValue();
        this.mTelephony = TelephonyManagerWrapper.getInstance(context);
    }

    private void initRcsConfiguration(Context context) {
        if (ImsProfile.isRcsUpProfile(this.mRcsProfile)) {
            this.mMaxAdhocGroupSize = 100;
            this.mServiceAvailabilityInfoExpiry = 60;
            return;
        }
        this.mMaxAdhocGroupSize = ImsRegistry.getInt(this.mPhoneId, "max_adhoc_group_size", 10);
        this.mServiceAvailabilityInfoExpiry = 0;
    }

    private void loadGlobalSettings(Context context) {
        this.mUserAgent = getSipUserAgent(context);
        this.mIsFullSFGroupChat = ImsRegistry.getBoolean(this.mPhoneId, GlobalSettingsConstants.RCS.FULL_SF_GROUP_CHAT, false);
        this.mIsAggrImdnSupported = ImsRegistry.getBoolean(this.mPhoneId, GlobalSettingsConstants.RCS.AGGR_IMDN_SUPPORTED, false);
        this.mEnableGroupChatListRetrieve = ImsRegistry.getBoolean(this.mPhoneId, GlobalSettingsConstants.RCS.ENABLE_GROUP_CHAT_LIST_RETRIEVE, false);
        this.mFtHttpTrustAllCerts = ImsRegistry.getBoolean(this.mPhoneId, GlobalSettingsConstants.RCS.FTHTTP_TRUST_ALL_CERTS, false);
        this.mFtCancelMemoryFull = ImsRegistry.getBoolean(this.mPhoneId, GlobalSettingsConstants.RCS.FT_CANCEL_MEMORY_FULL, false);
        this.mFtFallbackAllFail = ImsRegistry.getBoolean(this.mPhoneId, GlobalSettingsConstants.RCS.FT_FALLBACK_ALL_FAIL, false);
        this.mEnableFtAutoResumable = ImsRegistry.getBoolean(this.mPhoneId, GlobalSettingsConstants.RCS.ENABLE_FT_AUTO_RESUMABLE, false);
        this.mfThttpDefaultPdn = ImsRegistry.getBoolean(this.mPhoneId, GlobalSettingsConstants.RCS.FTHTTP_OVER_DEFAULT_PDN, false);
        this.mPagerModeLimit = ImsRegistry.getInt(this.mPhoneId, GlobalSettingsConstants.RCS.PAGER_MODE_SIZE_LIMIT, 0);
    }

    private void loadRcsConfiguration(Context context, RcsConfigurationHelper.ConfigData configData) {
        ImConstants.MessagingUX messagingUX;
        Context context2 = context;
        RcsConfigurationHelper.ConfigData configData2 = configData;
        Boolean bool = Boolean.FALSE;
        boolean booleanValue = configData2.readBool(ConfigConstants.ConfigTable.SERVICES_CHAT_AUTH, bool).booleanValue();
        this.mChatEnabled = booleanValue;
        this.mGroupChatEnabled = configData2.readBool(ConfigConstants.ConfigTable.SERVICES_GROUP_CHAT_AUTH, Boolean.valueOf(booleanValue)).booleanValue();
        this.mFtEnabled = configData2.readBool(ConfigConstants.ConfigTable.SERVICES_FT_AUTH, bool).booleanValue();
        this.mFtHttpCsUri = configData2.readUri(ConfigConstants.ConfigTable.IM_FT_HTTP_CS_URI, (Uri) null);
        this.mFtDefaultMech = RcsConfigurationHelper.getFtDefaultTech(configData2, this.mRcsProfile, this.mPhoneId);
        Uri uri = this.mFtHttpCsUri;
        this.mFtHttpEnabled = uri != null && !"".equals(uri.toString().trim()) && this.mFtDefaultMech == ImConstants.FtMech.HTTP;
        this.mSlmAuth = ImConstants.SlmAuth.values()[configData2.readIntWithinRange(ConfigConstants.ConfigTable.SERVICES_SLM_AUTH, 0, 0, Integer.valueOf(ImConstants.SlmAuth.values().length - 1)).intValue()];
        this.mSmsFallbackAuth = configData2.readBool(ConfigConstants.ConfigTable.IM_SMS_FALLBACK_AUTH, bool).booleanValue();
        this.mGlsPushEnabled = configData2.readBool(ConfigConstants.ConfigTable.SERVICES_GEOPUSH_AUTH, bool).booleanValue();
        this.mGlsPullEnabled = configData2.readInt(ConfigConstants.ConfigTable.SERVICES_GEOPULL_AUTH, 0).intValue() != 0;
        this.mChatbotMsgTech = ImConstants.ChatbotMsgTechConfig.values()[configData2.readIntWithinRange(ConfigConstants.ConfigTable.CHATBOT_CHATBOT_MSG_TECH, 1, 0, Integer.valueOf(ImConstants.ChatbotMsgTechConfig.values().length - 1)).intValue()];
        this.mPresSrvCap = configData2.readBool(ConfigConstants.ConfigTable.IM_PRES_SRV_CAP, bool).booleanValue();
        this.mMaxAdhocGroupSize = configData2.readInt("max_adhoc_group_size", Integer.valueOf(this.mMaxAdhocGroupSize)).intValue();
        this.mConfFctyUri = configData2.readImsUri(ConfigConstants.ConfigTable.IM_CONF_FCTY_URI, (ImsUri) null);
        if (!Mno.CMCC.getName().equalsIgnoreCase(ImsRegistry.getString(this.mPhoneId, "mnoname", "")) || ImsProfile.isRcsUpProfile(this.mRcsProfile)) {
            this.mExploderUri = configData2.readImsUri(ConfigConstants.ConfigTable.IM_EXPLODER_URI, (ImsUri) null);
        } else {
            this.mExploderUri = configData2.readImsUri(ConfigConstants.ConfigTable.IM_MASS_FCTY_URI, (ImsUri) null);
        }
        this.mDeferredMsgFuncUri = configData2.readImsUri(ConfigConstants.ConfigTable.IM_DEFERRED_MSG_FUNC_URI, (ImsUri) null);
        this.mImCapAlwaysOn = getImCapAlwaysOn(context, configData);
        this.mImWarnSf = configData2.readBool(ConfigConstants.ConfigTable.IM_IM_WARN_SF, bool).booleanValue();
        this.mGroupChatFullStandFwd = configData2.readBool(ConfigConstants.ConfigTable.IM_GROUP_CHAT_FULL_STAND_FWD, bool).booleanValue();
        this.mGroupChatOnlyFStandFwd = configData2.readBool(ConfigConstants.ConfigTable.IM_GROUP_CHAT_ONLY_F_STAND_FWD, bool).booleanValue();
        this.mImCapNonRcs = configData2.readBool(ConfigConstants.ConfigTable.IM_IM_CAP_NON_RCS, bool).booleanValue();
        this.mImWarnIw = configData2.readBool(ConfigConstants.ConfigTable.IM_IM_WARN_IW, bool).booleanValue();
        this.mAutAccept = configData2.readBool(ConfigConstants.ConfigTable.IM_AUT_ACCEPT, bool).booleanValue();
        this.mImSessionStart = ImConstants.ImSessionStart.values()[configData2.readIntWithinRange(ConfigConstants.ConfigTable.IM_IM_SESSION_START, 0, 0, Integer.valueOf(ImConstants.ImSessionStart.values().length - 1)).intValue()];
        this.mAutAcceptGroupChat = configData2.readBool(ConfigConstants.ConfigTable.IM_AUT_ACCEPT_GROUP_CHAT, bool).booleanValue();
        this.mFirstMsgInvite = configData2.readBool(ConfigConstants.ConfigTable.IM_FIRST_MSG_INVITE, bool).booleanValue();
        this.mTimerIdle = configData2.readInt(ConfigConstants.ConfigTable.IM_TIMER_IDLE, 0).intValue();
        this.mMaxConcurrentSession = configData2.readInt(ConfigConstants.ConfigTable.IM_MAX_CONCURRENT_SESSION, 0).intValue();
        this.mMultiMediaChat = configData2.readBool(ConfigConstants.ConfigTable.IM_MULTIMEDIA_CHAT, bool).booleanValue();
        this.mMaxSize1To1 = configData2.readLong(ConfigConstants.ConfigTable.IM_MAX_SIZE_1_TO_1, 0L).longValue();
        this.mMaxSize1ToM = configData2.readLong(ConfigConstants.ConfigTable.IM_MAX_SIZE_1_TO_M, 0L).longValue();
        long longValue = configData2.readLong(ConfigConstants.ConfigTable.CPM_SLM_MAX_MSG_SIZE, 0L).longValue();
        this.mSlmMaxMsgSize = longValue;
        if (longValue == 0) {
            this.mSlmMaxMsgSize = configData2.readLong("MaxSize", 0L).longValue();
        }
        this.mImMsgTech = RcsConfigurationHelper.getImMsgTech(configData2, this.mRcsProfile, this.mPhoneId);
        this.mFtHttpCsUser = configData2.readString(ConfigConstants.ConfigTable.IM_FT_HTTP_CS_USER, (String) null);
        this.mFtHttpCsPwd = configData2.readString(ConfigConstants.ConfigTable.IM_FT_HTTP_CS_PWD, (String) null);
        this.mMaxSizeFileTr = configData2.readLong(ConfigConstants.ConfigTable.IM_MAX_SIZE_FILE_TR, 0L).longValue();
        this.mFtWarnSize = configData2.readLong(ConfigConstants.ConfigTable.IM_FT_WARN_SIZE, 0L).longValue();
        this.mFtThumb = configData2.readBool(ConfigConstants.ConfigTable.IM_FT_THUMB, bool).booleanValue();
        this.mFtStAndFwEnabled = configData2.readBool(ConfigConstants.ConfigTable.IM_FT_ST_AND_FW_ENABLED, bool).booleanValue();
        this.mFtCapAlwaysOn = configData2.readBool(ConfigConstants.ConfigTable.IM_FT_CAP_ALWAYS_ON, bool).booleanValue();
        boolean booleanValue2 = configData2.readBool(ConfigConstants.ConfigTable.IM_FT_AUT_ACCEPT, bool).booleanValue();
        this.mFtAutAccept = booleanValue2;
        this.mFtAutAcceptOriginalConfig = booleanValue2;
        this.mFtHttpDLUrl = configData2.readString(ConfigConstants.ConfigTable.IM_FT_HTTP_DL_URI, (String) null);
        this.mCallComposerTimerIdle = configData2.readInt(ConfigConstants.ConfigTable.OTHER_CALL_COMPOSER_TIMER_IDLE, Integer.valueOf(MNO.EVR_ESN)).intValue();
        this.mJoynIntegratedMessaging = RcsConfigurationHelper.readBoolParamWithPath(context2, ConfigConstants.ConfigPath.JOYN_UX_MESSAGING_UX).booleanValue();
        this.mMsgCapValidityTime = configData2.readInt(ConfigConstants.ConfigTable.CAPDISCOVERY_JOYN_MSGCAPVALIDITY, 30).intValue();
        this.mFtHttpCapAlwaysOn = configData2.readBool(ConfigConstants.ConfigTable.CLIENTCONTROL_FT_HTTP_CAP_ALWAYS_ON, Boolean.valueOf(this.mImCapAlwaysOn)).booleanValue();
        this.mChatRevokeTimer = configData2.readInt(ConfigConstants.ConfigTable.IM_CHAT_REVOKE_TIMER, 0).intValue();
        long longValue2 = configData2.readLong(ConfigConstants.ConfigTable.IM_MAX_SIZE_FILE_TR_INCOMING, -1L).longValue();
        this.mMaxSizeFileTrIncoming = longValue2;
        if (longValue2 == -1) {
            this.mMaxSizeFileTrIncoming = this.mMaxSizeFileTr;
        }
        this.mMaxSize = configData2.readLong("MaxSize", 0L).longValue();
        this.mFtHttpFallback = configData2.readInt(ConfigConstants.ConfigTable.IM_FT_HTTP_FALLBACK, 0).intValue();
        this.mPublicAccountAddr = configData2.readString(ConfigConstants.ConfigTable.PUBLIC_ACCOUNT_ADDR, (String) null);
        this.mMaxSizeExtraFileTr = (long) configData2.readInt(ConfigConstants.ConfigTable.IM_EXT_MAX_SIZE_EXTRA_FILE_TR, 0).intValue();
        this.mFtHTTPExtraCSURI = configData2.readUri(ConfigConstants.ConfigTable.IM_EXT_FT_HTTP_EXTRA_CS_URI, (Uri) null);
        this.mCbftHTTPCSURI = configData2.readUri(ConfigConstants.ConfigTable.IM_EXT_CB_FT_HTTP_CS_URI, (Uri) null);
        this.mRealtimeUserAliasAuth = ImsRegistry.getBoolean(this.mPhoneId, GlobalSettingsConstants.RCS.SUPPORT_REALTIME_USER_ALIAS, false) && configData2.readBool(ConfigConstants.ConfigTable.UX_REALTIME_USER_ALIAS_AUTH, bool).booleanValue();
        if (configData2.readInt(ConfigConstants.ConfigTable.UX_MESSAGING_UX, 0).intValue() == 0) {
            messagingUX = ImConstants.MessagingUX.SEAMLESS;
        } else {
            messagingUX = ImConstants.MessagingUX.INTEGRATED;
        }
        this.mMessagingUX = messagingUX;
        this.mUserAliasEnabled = this.mRealtimeUserAliasAuth || configData2.readBool(ConfigConstants.ConfigTable.UX_USER_ALIAS_AUTH, Boolean.TRUE).booleanValue();
        this.mMsgFbDefault = configData2.readInt(ConfigConstants.ConfigTable.UX_MSG_FB_DEFAULT, 0).intValue();
        this.mReconnectGuardTimer = configData2.readInt(ConfigConstants.ConfigTable.CLIENTCONTROL_RECONNECT_GUARD_TIMER, 0).intValue();
        this.mCfsTrigger = configData2.readBool(ConfigConstants.ConfigTable.CLIENTCONTROL_CFS_TRIGGER, bool).booleanValue();
        this.mMax1ToManyRecipients = configData2.readInt(ConfigConstants.ConfigTable.CLIENTCONTROL_MAX1_TO_MANY_RECIPIENTS, 0).intValue();
        this.m1ToManySelectedTech = configData2.readInt(ConfigConstants.ConfigTable.CLIENTCONTROL_ONE_TO_MANY_SELECTED_TECH, 0).intValue();
        this.mDisplayNotificationSwitch = configData2.readInt(ConfigConstants.ConfigTable.CLIENTCONTROL_DISPLAY_NOTIFICATION_SWITCH, 0).intValue();
        this.mFtMax1ToManyRecipients = configData2.readInt(ConfigConstants.ConfigTable.CLIENTCONTROL_FT_MAX1_TO_MANY_RECIPIENTS, 0).intValue();
        this.mFtFbDefault = configData2.readInt(ConfigConstants.ConfigTable.UX_FT_FB_DEFAULT, 0).intValue();
        this.mServiceAvailabilityInfoExpiry = configData2.readInt(ConfigConstants.ConfigTable.CLIENTCONTROL_SERVICE_AVAILABILITY_INFO_EXPIRY, Integer.valueOf(this.mServiceAvailabilityInfoExpiry)).intValue();
        Uri readUri = configData2.readUri(ConfigConstants.ConfigTable.PLUGINS_CATALOGURI, (Uri) null);
        this.mCatalogUri = readUri;
        this.mPlugInEnabled = readUri != null && !"".equals(readUri.toString().trim());
        this.mBotPrivacyDisable = configData2.readBool(ConfigConstants.ConfigTable.CHATBOT_PRIVACY_DISABLE, bool).booleanValue();
        this.mSlmSwitchOverSize = configData2.readInt(ConfigConstants.ConfigTable.SLM_SWITCH_OVER_SIZE, 1300).intValue();
        String readStringParamWithPath = RcsConfigurationHelper.readStringParamWithPath(context2, "root/application/1/im/ext/att/slmMaxRecipients");
        if (!TextUtils.isEmpty(readStringParamWithPath)) {
            try {
                this.mExtAttImSlmMaxRecipients = Integer.parseInt(readStringParamWithPath);
            } catch (NumberFormatException e) {
                e.printStackTrace();
            }
        }
        String readStringParamWithPath2 = RcsConfigurationHelper.readStringParamWithPath(context2, "root/application/1/im/ext/att/MSRPFtMaxSize");
        if (!TextUtils.isEmpty(readStringParamWithPath2)) {
            try {
                this.mExtAttImMSRPFtMaxSize = Long.parseLong(readStringParamWithPath2);
            } catch (NumberFormatException e2) {
                e2.printStackTrace();
            }
        }
    }

    /* JADX WARNING: Code restructure failed: missing block: B:6:0x0025, code lost:
        r9 = r8.mFtHttpCsUri;
     */
    /* Code decompiled incorrectly, please refer to instructions dump. */
    private void updateRcsConfiguration(android.content.Context r9, com.sec.internal.helper.RcsConfigurationHelper.ConfigData r10) {
        /*
            r8 = this;
            java.lang.String r9 = r8.mRcsProfile
            boolean r9 = com.sec.ims.settings.ImsProfile.isRcsUpProfile(r9)
            r0 = 120(0x78, float:1.68E-43)
            java.lang.Integer r0 = java.lang.Integer.valueOf(r0)
            java.lang.String r1 = "reconnectGuardTimer"
            java.lang.String r2 = "ftHTTPCapAlwaysOn"
            java.lang.String r3 = ""
            r4 = 0
            if (r9 == 0) goto L_0x0093
            java.lang.String r9 = r8.mRcsProfile
            int r5 = r8.mPhoneId
            boolean r9 = com.sec.internal.helper.RcsConfigurationHelper.isUp2NonTransitional(r9, r5)
            r5 = 1
            if (r9 == 0) goto L_0x003f
            boolean r9 = r8.mFtEnabled
            if (r9 == 0) goto L_0x0039
            android.net.Uri r9 = r8.mFtHttpCsUri
            if (r9 == 0) goto L_0x0039
            java.lang.String r9 = r9.toString()
            java.lang.String r9 = r9.trim()
            boolean r9 = r3.equals(r9)
            if (r9 != 0) goto L_0x0039
            r9 = r5
            goto L_0x003a
        L_0x0039:
            r9 = r4
        L_0x003a:
            r8.mFtHttpEnabled = r9
            r8.mFtEnabled = r4
            goto L_0x005c
        L_0x003f:
            android.net.Uri r9 = r8.mFtHttpCsUri
            if (r9 == 0) goto L_0x0059
            java.lang.String r9 = r9.toString()
            java.lang.String r9 = r9.trim()
            boolean r9 = r3.equals(r9)
            if (r9 != 0) goto L_0x0059
            com.sec.internal.constants.ims.servicemodules.im.ImConstants$FtMech r9 = r8.mFtDefaultMech
            com.sec.internal.constants.ims.servicemodules.im.ImConstants$FtMech r6 = com.sec.internal.constants.ims.servicemodules.im.ImConstants.FtMech.HTTP
            if (r9 != r6) goto L_0x0059
            r9 = r5
            goto L_0x005a
        L_0x0059:
            r9 = r4
        L_0x005a:
            r8.mFtHttpEnabled = r9
        L_0x005c:
            long r6 = r8.mMaxSize
            r8.mMaxSize1To1 = r6
            r8.mMaxSize1ToM = r6
            java.lang.Integer r9 = r10.readInt(r1, r0)
            int r9 = r9.intValue()
            r8.mReconnectGuardTimer = r9
            int r9 = r8.mPhoneId
            java.lang.String r6 = "legacy_latching"
            boolean r9 = com.sec.internal.ims.registry.ImsRegistry.getBoolean(r9, r6, r4)
            r8.mLegacyLatching = r9
            java.lang.String r9 = "firstMessageInvite"
            java.lang.Boolean r6 = java.lang.Boolean.FALSE
            java.lang.Boolean r9 = r10.readBool(r9, r6)
            boolean r9 = r9.booleanValue()
            com.sec.internal.constants.ims.servicemodules.im.ImConstants$ImMsgTech r6 = r8.mImMsgTech
            com.sec.internal.constants.ims.servicemodules.im.ImConstants$ImMsgTech r7 = com.sec.internal.constants.ims.servicemodules.im.ImConstants.ImMsgTech.SIMPLE_IM
            if (r6 == r7) goto L_0x008c
            if (r9 == 0) goto L_0x008b
            goto L_0x008c
        L_0x008b:
            r5 = r4
        L_0x008c:
            r8.mFirstMsgInvite = r5
            r8.mFtEnabled = r4
            r8.mFtThumb = r4
            goto L_0x00a9
        L_0x0093:
            java.lang.String r9 = "joyn_cpr"
            java.lang.String r5 = r8.mRcsProfile
            boolean r9 = r9.equals(r5)
            if (r9 == 0) goto L_0x00a9
            java.lang.Boolean r9 = java.lang.Boolean.TRUE
            java.lang.Boolean r9 = r10.readBool(r2, r9)
            boolean r9 = r9.booleanValue()
            r8.mFtHttpCapAlwaysOn = r9
        L_0x00a9:
            int r9 = r8.mPhoneId
            java.lang.String r5 = "mnoname"
            java.lang.String r9 = com.sec.internal.ims.registry.ImsRegistry.getString(r9, r5, r3)
            int r3 = r8.mPhoneId
            java.lang.String r3 = com.sec.internal.ims.util.ConfigUtil.getAcsServerType(r3)
            int r5 = r8.mPhoneId
            int r4 = com.sec.internal.ims.util.ConfigUtil.getAutoconfigSourceWithFeature(r5, r4)
            java.lang.StringBuilder r5 = new java.lang.StringBuilder
            r5.<init>()
            java.lang.String r6 = "name:"
            r5.append(r6)
            r5.append(r9)
            java.lang.String r6 = ", rcs_local_config_server:"
            r5.append(r6)
            r5.append(r4)
            java.lang.String r5 = r5.toString()
            java.lang.String r6 = "ImConfig"
            android.util.Log.i(r6, r5)
            com.sec.internal.constants.Mno r5 = com.sec.internal.constants.Mno.SPRINT
            java.lang.String r5 = r5.getName()
            boolean r5 = r5.equalsIgnoreCase(r9)
            if (r5 == 0) goto L_0x00f3
            java.lang.Integer r9 = r10.readInt(r1, r0)
            int r9 = r9.intValue()
            r8.mReconnectGuardTimer = r9
            goto L_0x0173
        L_0x00f3:
            com.sec.internal.constants.Mno r0 = com.sec.internal.constants.Mno.RJIL
            java.lang.String r0 = r0.getName()
            boolean r0 = r0.equalsIgnoreCase(r9)
            if (r0 == 0) goto L_0x010c
            java.lang.Boolean r9 = java.lang.Boolean.TRUE
            java.lang.Boolean r9 = r10.readBool(r2, r9)
            boolean r9 = r9.booleanValue()
            r8.mFtHttpCapAlwaysOn = r9
            goto L_0x0173
        L_0x010c:
            com.sec.internal.constants.Mno r10 = com.sec.internal.constants.Mno.ATT
            java.lang.String r10 = r10.getName()
            boolean r10 = r10.equalsIgnoreCase(r9)
            if (r10 == 0) goto L_0x0149
            java.lang.String r10 = "jibe"
            boolean r10 = r10.equalsIgnoreCase(r3)
            if (r10 != 0) goto L_0x0149
            r9 = 2
            if (r4 == r9) goto L_0x0173
            r9 = 3
            if (r4 == r9) goto L_0x0173
            java.lang.String r9 = r8.mFtHttpCsUser
            java.lang.String r9 = r8.decrypt(r9)
            r8.mFtHttpCsUser = r9
            java.lang.String r9 = r8.mFtHttpCsPwd
            java.lang.String r9 = r8.decrypt(r9)
            r8.mFtHttpCsPwd = r9
            android.net.Uri r9 = r8.mFtHttpCsUri
            if (r9 == 0) goto L_0x0173
            java.lang.String r9 = r9.toString()
            java.lang.String r9 = r8.decrypt(r9)
            android.net.Uri r9 = android.net.Uri.parse(r9)
            r8.mFtHttpCsUri = r9
            goto L_0x0173
        L_0x0149:
            com.sec.internal.constants.Mno r10 = com.sec.internal.constants.Mno.CMCC
            java.lang.String r10 = r10.getName()
            boolean r10 = r10.equalsIgnoreCase(r9)
            if (r10 != 0) goto L_0x016d
            com.sec.internal.constants.Mno r10 = com.sec.internal.constants.Mno.CTC
            java.lang.String r10 = r10.getName()
            boolean r10 = r10.equalsIgnoreCase(r9)
            if (r10 != 0) goto L_0x016d
            com.sec.internal.constants.Mno r10 = com.sec.internal.constants.Mno.CU
            java.lang.String r10 = r10.getName()
            boolean r9 = r10.equalsIgnoreCase(r9)
            if (r9 == 0) goto L_0x0173
        L_0x016d:
            if (r4 != 0) goto L_0x0173
            com.sec.internal.constants.ims.servicemodules.im.ImConstants$ImMsgTech r9 = com.sec.internal.constants.ims.servicemodules.im.ImConstants.ImMsgTech.CPM
            r8.mImMsgTech = r9
        L_0x0173:
            return
        */
        throw new UnsupportedOperationException("Method not decompiled: com.sec.internal.ims.servicemodules.im.ImConfig.updateRcsConfiguration(android.content.Context, com.sec.internal.helper.RcsConfigurationHelper$ConfigData):void");
    }

    private String decrypt(String str) {
        if (str == null) {
            return null;
        }
        try {
            return new String(Base64.decode(str, 0));
        } catch (IllegalArgumentException unused) {
            Log.e(LOG_TAG, "Failed to decrypt the data");
            return str;
        }
    }

    public void loadUserAlias(Context context) {
        if (this.mUserAliasEnabled) {
            this.mUserAlias = getUserAliasFromPreference(context);
        } else {
            this.mUserAlias = "";
        }
    }

    public int getPhoneId() {
        return this.mPhoneId;
    }

    public String getRcsProfile() {
        return this.mRcsProfile;
    }

    public boolean getChatEnabled() {
        return this.mChatEnabled;
    }

    public boolean getGroupChatEnabled() {
        return this.mGroupChatEnabled;
    }

    public boolean getFtEnabled() {
        return this.mFtEnabled;
    }

    public boolean getFtHttpEnabled() {
        return this.mFtHttpEnabled;
    }

    public boolean getGlsPushEnabled() {
        return this.mGlsPushEnabled;
    }

    public boolean getGlsPullEnabled() {
        return this.mGlsPullEnabled;
    }

    public ImConstants.SlmAuth getSlmAuth() {
        return this.mSlmAuth;
    }

    public ImConstants.ImMsgTech getImMsgTech() {
        return this.mImMsgTech;
    }

    public boolean isImCapAlwaysOn() {
        return this.mImCapAlwaysOn;
    }

    public boolean isImWarnSf() {
        return this.mImWarnSf;
    }

    public boolean isGroupChatFullStandFwd() {
        return this.mGroupChatFullStandFwd;
    }

    public boolean isSmsFallbackAuth() {
        return this.mSmsFallbackAuth;
    }

    public boolean isAutAccept() {
        return this.mAutAccept;
    }

    public ImConstants.ImSessionStart getImSessionStart() {
        return this.mImSessionStart;
    }

    public boolean isAutAcceptGroupChat() {
        return this.mAutAcceptGroupChat;
    }

    public boolean isFirstMsgInvite() {
        return this.mFirstMsgInvite;
    }

    public int getTimerIdle() {
        return this.mTimerIdle;
    }

    public int getCallComposerTimerIdle() {
        return this.mCallComposerTimerIdle;
    }

    public int getMaxConcurrentSession() {
        return this.mMaxConcurrentSession;
    }

    public long getMaxSize1To1() {
        return this.mMaxSize1To1;
    }

    public long getMaxSize1ToM() {
        return this.mMaxSize1ToM;
    }

    public long getFtWarnSize() {
        return this.mFtWarnSize * 1024;
    }

    public long getMaxSizeFileTr() {
        return this.mMaxSizeFileTr * 1024;
    }

    public long getMaxSizeFileTrIncoming() {
        return this.mMaxSizeFileTrIncoming * 1024;
    }

    public boolean isFtThumb() {
        return this.mFtThumb;
    }

    public boolean isFtStAndFwEnabled() {
        return this.mFtStAndFwEnabled;
    }

    public boolean isFtAutAccept() {
        return this.mFtAutAccept;
    }

    public Uri getFtHttpCsUri() {
        return this.mFtHttpCsUri;
    }

    public String getFtHttpDLUrl() {
        return this.mFtHttpDLUrl;
    }

    public String getFtHttpCsUser() {
        ITelephonyManager iTelephonyManager;
        if (!"VZW".equals(OmcCode.get()) || !TextUtils.isEmpty(this.mFtHttpCsUser) || (iTelephonyManager = this.mTelephony) == null) {
            return this.mFtHttpCsUser;
        }
        String msisdn = iTelephonyManager.getMsisdn();
        return TextUtils.isEmpty(msisdn) ? this.mTelephony.getLine1Number() : msisdn;
    }

    public String getFtHttpCsPwd() {
        return this.mFtHttpCsPwd;
    }

    public ImConstants.FtMech getFtDefaultMech() {
        return this.mFtDefaultMech;
    }

    public boolean isFtHttpCapAlwaysOn() {
        return this.mFtHttpCapAlwaysOn;
    }

    public int getMaxAdhocGroupSize() {
        return this.mMaxAdhocGroupSize;
    }

    public long getSlmMaxMsgSize() {
        return this.mSlmMaxMsgSize;
    }

    public boolean isFullSFGroupChat() {
        return this.mIsFullSFGroupChat;
    }

    public boolean isAggrImdnSupported() {
        return this.mIsAggrImdnSupported;
    }

    public boolean isEnableGroupChatListRetrieve() {
        return this.mEnableGroupChatListRetrieve && this.mConfFctyUri != null;
    }

    public ImsUri getConfFactoryUri() {
        return this.mConfFctyUri;
    }

    public ImsUri getExploderUri() {
        return this.mExploderUri;
    }

    public void setFtAutAccept(Context context, int i, boolean z) {
        ImUserPreference instance = ImUserPreference.getInstance();
        if (instance.getFtAutAccept(context, getPhoneId()) != i) {
            instance.setFtAutAccept(context, getPhoneId(), i);
        }
        boolean z2 = false;
        if (i == -1) {
            if (!z && this.mFtAutAcceptOriginalConfig) {
                z2 = true;
            }
            this.mFtAutAccept = z2;
            return;
        }
        if (i == 2 || (i == 1 && !z)) {
            z2 = true;
        }
        this.mFtAutAccept = z2;
    }

    public String getUserAgent() {
        return this.mUserAgent;
    }

    /* JADX WARNING: Code restructure failed: missing block: B:4:0x0005, code lost:
        r0 = r1.mUserAlias;
     */
    /* Code decompiled incorrectly, please refer to instructions dump. */
    public synchronized java.lang.String getUserAlias() {
        /*
            r1 = this;
            monitor-enter(r1)
            boolean r0 = r1.mUserAliasEnabled     // Catch:{ all -> 0x000f }
            if (r0 == 0) goto L_0x000b
            java.lang.String r0 = r1.mUserAlias     // Catch:{ all -> 0x000f }
            if (r0 == 0) goto L_0x000b
            monitor-exit(r1)
            return r0
        L_0x000b:
            java.lang.String r0 = ""
            monitor-exit(r1)
            return r0
        L_0x000f:
            r0 = move-exception
            monitor-exit(r1)
            throw r0
        */
        throw new UnsupportedOperationException("Method not decompiled: com.sec.internal.ims.servicemodules.im.ImConfig.getUserAlias():java.lang.String");
    }

    public synchronized String getUserAliasFromPreference(Context context) {
        return ImUserPreference.getInstance().getUserAlias(context, getPhoneId());
    }

    public synchronized void setUserAlias(Context context, String str) {
        if (!this.mUserAliasEnabled) {
            Log.i(LOG_TAG, "alias disabled");
        } else if (str == null) {
            this.mUserAlias = "";
        } else {
            this.mUserAlias = str;
        }
        ImUserPreference instance = ImUserPreference.getInstance();
        int phoneId = getPhoneId();
        if (str == null) {
            str = "";
        }
        instance.setUserAlias(context, phoneId, str);
    }

    public boolean isJoynIntegratedMessaging() {
        return this.mJoynIntegratedMessaging;
    }

    public int getMsgCapValidityTime() {
        return this.mMsgCapValidityTime;
    }

    private String getSipUserAgent(Context context) {
        String string = ImsRegistry.getString(this.mPhoneId, GlobalSettingsConstants.Registration.USER_AGENT, "");
        if (!TextUtils.isEmpty(string)) {
            return string;
        }
        String terminalModel = ConfigContract.BUILD.getTerminalModel();
        String terminalSwVersion = ConfigContract.BUILD.getTerminalSwVersion();
        String string2 = ImsRegistry.getString(this.mPhoneId, GlobalSettingsConstants.RCS.RCS_CLIENT_VERSION, "6.0");
        if ("VOD".equals(OmcCode.get())) {
            string2 = "4.1";
        }
        if ("DTM".equals(OmcCode.get()) || "DTR".equals(OmcCode.get()) || "SFR".equals(OmcCode.get()) || "TMZ".equals(OmcCode.get())) {
            if (terminalSwVersion.length() > 8) {
                terminalSwVersion = terminalSwVersion.substring(terminalSwVersion.length() - 8);
            }
        } else if (terminalSwVersion.length() > 3) {
            terminalSwVersion = terminalSwVersion.substring(terminalSwVersion.length() - 3);
        }
        return ConfigUtil.getFormattedUserAgent(SimUtil.getSimMno(this.mPhoneId), terminalModel, terminalSwVersion, string2);
    }

    public long getMaxSizeExtraFileTr() {
        return this.mMaxSizeExtraFileTr * 1024;
    }

    public Uri getFtHTTPExtraCSURI() {
        return this.mFtHTTPExtraCSURI;
    }

    public Uri getCbftHTTPCSURI() {
        return this.mCbftHTTPCSURI;
    }

    public boolean isFtHttpTrustAllCerts() {
        return this.mFtHttpTrustAllCerts;
    }

    public boolean getFtCancelMemoryFull() {
        return this.mFtCancelMemoryFull;
    }

    public boolean getFtFallbackAllFail() {
        return this.mFtFallbackAllFail;
    }

    public boolean getRespondDisplay() {
        boolean z = true;
        if (TapiServiceUtil.isSupportTapi()) {
            RcsSettingsUtils instance = RcsSettingsUtils.getInstance();
            if (instance != null) {
                this.mRespondDisplay = Boolean.parseBoolean(instance.readParameter(ImSettings.CHAT_RESPOND_TO_DISPLAY_REPORTS));
            }
        } else {
            this.mRespondDisplay = true;
        }
        if (ImsProfile.isRcsUpProfile(this.mRcsProfile)) {
            if (!this.mRespondDisplay || this.mDisplayNotificationSwitch != 0) {
                z = false;
            }
            this.mRespondDisplay = z;
        }
        return this.mRespondDisplay;
    }

    public boolean getEnableFtAutoResumable() {
        return this.mEnableFtAutoResumable;
    }

    public boolean isFtHttpOverDefaultPdn() {
        return this.mfThttpDefaultPdn;
    }

    public boolean getUserAliasEnabled() {
        return this.mUserAliasEnabled;
    }

    public boolean getRealtimeUserAliasAuth() {
        return this.mRealtimeUserAliasAuth;
    }

    public int getReconnectGuardTimer() {
        return this.mReconnectGuardTimer;
    }

    public boolean isCfsTrigger() {
        return this.mCfsTrigger;
    }

    public int getChatRevokeTimer() {
        return this.mChatRevokeTimer;
    }

    public boolean getLegacyLatching() {
        return this.mLegacyLatching;
    }

    public int getPagerModeLimit() {
        if (ImsProfile.isRcsUp23AndUp24Profile(this.mRcsProfile)) {
            return this.mSlmSwitchOverSize;
        }
        return this.mPagerModeLimit;
    }

    public ImConstants.ChatbotMsgTechConfig getChatbotMsgTech() {
        return this.mChatbotMsgTech;
    }

    public boolean getBotPrivacyDisable() {
        return this.mBotPrivacyDisable;
    }

    public Uri getCatalogUri() {
        return this.mCatalogUri;
    }

    public boolean getPlugInEnabled() {
        return this.mPlugInEnabled;
    }

    private boolean getImCapAlwaysOn(Context context, RcsConfigurationHelper.ConfigData configData) {
        if (RcsConfigurationHelper.isUp2NonTransitional(this.mRcsProfile, this.mPhoneId)) {
            return true;
        }
        return configData.readBool(ConfigConstants.ConfigTable.IM_IM_CAP_ALWAYS_ON, Boolean.FALSE).booleanValue();
    }

    public String toString() {
        return "ImConfig(phoneId: " + this.mPhoneId + ")[mRcsProfile=" + this.mRcsProfile + ", mChatEnabled=" + this.mChatEnabled + ", mGroupChatEnabled=" + this.mGroupChatEnabled + ", mFtEnabled=" + this.mFtEnabled + ", mFtHttpCsUri=" + this.mFtHttpCsUri + ", mFtHttpCsUser=" + IMSLog.checker(this.mFtHttpCsUser) + ", mFtHttpCsPwd=" + IMSLog.checker(this.mFtHttpCsPwd) + ", mFtHttpDLUrl=" + this.mFtHttpDLUrl + ", mFtHttpEnabled=" + this.mFtHttpEnabled + ", mSlmAuth=" + this.mSlmAuth + ", mSmsFallbackAuth=" + this.mSmsFallbackAuth + ", mGlsPushEnabled=" + this.mGlsPushEnabled + ", mGlsPullEnabled=" + this.mGlsPullEnabled + ", mPresSrvCap=" + this.mPresSrvCap + ", mMaxAdhocGroupSize=" + this.mMaxAdhocGroupSize + ", mConfFctyUri=" + this.mConfFctyUri + ", mExploderUri=" + this.mExploderUri + ", mDeferredMsgFuncUri=" + this.mDeferredMsgFuncUri + ", mImCapAlwaysOn=" + this.mImCapAlwaysOn + ", mImWarnSf=" + this.mImWarnSf + ", mGroupChatFullStandFwd=" + this.mGroupChatFullStandFwd + ", mGroupChatOnlyFStandFwd=" + this.mGroupChatOnlyFStandFwd + ", mImCapNonRcs=" + this.mImCapNonRcs + ", mImWarnIw=" + this.mImWarnIw + ", mAutAccept=" + this.mAutAccept + ", mImSessionStart=" + this.mImSessionStart + ", mAutAcceptGroupChat=" + this.mAutAcceptGroupChat + ", mFirstMsgInvite=" + this.mFirstMsgInvite + ", mTimerIdle=" + this.mTimerIdle + ", mMaxConcurrentSession=" + this.mMaxConcurrentSession + ", mMultiMediaChat=" + this.mMultiMediaChat + ", mMaxSize1To1=" + this.mMaxSize1To1 + ", mMaxSize1ToM=" + this.mMaxSize1ToM + ", mSlmMaxMsgSize=" + this.mSlmMaxMsgSize + ", mImMsgTech=" + this.mImMsgTech + ", mChatbotMsgTech=" + this.mChatbotMsgTech + ", mMaxSizeFileTr=" + this.mMaxSizeFileTr + ", mFtWarnSize=" + this.mFtWarnSize + ", mFtThumb=" + this.mFtThumb + ", mFtStAndFwEnabled=" + this.mFtStAndFwEnabled + ", mFtCapAlwaysOn=" + this.mFtCapAlwaysOn + ", mFtAutAccept=" + this.mFtAutAccept + ", mFtDefaultMech=" + this.mFtDefaultMech + ", mJoynIntegratedMessaging=" + this.mJoynIntegratedMessaging + ", mMsgCapValidityTime=" + this.mMsgCapValidityTime + ", mFtHttpCapAlwaysOn=" + this.mFtHttpCapAlwaysOn + ", mChatRevokeTimer=" + this.mChatRevokeTimer + ", mMaxSizeFileTrIncoming=" + this.mMaxSizeFileTrIncoming + ", mMaxSize=" + this.mMaxSize + ", mFtHttpFallback=" + this.mFtHttpFallback + ", mMessagingUX=" + this.mMessagingUX + ", mUserAliasEnabled=" + this.mUserAliasEnabled + ", mRealtimeUserAliasAuth=" + this.mRealtimeUserAliasAuth + ", mMsgFbDefault=" + this.mMsgFbDefault + ", mReconnectGuardTimer=" + this.mReconnectGuardTimer + ", mCfsTrigger=" + this.mCfsTrigger + ", mMax1ToManyRecipients=" + this.mMax1ToManyRecipients + ", m1ToManySelectedTech=" + this.m1ToManySelectedTech + ", mDisplayNotificationSwitch=" + this.mDisplayNotificationSwitch + ", mFtMax1ToManyRecipients=" + this.mFtMax1ToManyRecipients + ", mFtFbDefault=" + this.mFtFbDefault + ", mServiceAvailabilityInfoExpiry=" + this.mServiceAvailabilityInfoExpiry + ", mPublicAccountAddr=" + this.mPublicAccountAddr + ", mMaxSizeExtraFileTr=" + this.mMaxSizeExtraFileTr + ", mFtHTTPExtraCSURI=" + this.mFtHTTPExtraCSURI + ", mCbftHTTPCSURI=" + this.mCbftHTTPCSURI + ", mIsFullSFGroupChat=" + this.mIsFullSFGroupChat + ", mIsAggrImdnSupported=" + this.mIsAggrImdnSupported + ", mUserAgent=" + this.mUserAgent + ", mUserAlias=" + IMSLog.checker(this.mUserAlias) + ", mFtHttpTrustAllCerts=" + this.mFtHttpTrustAllCerts + ", mFtCancelMemoryFull=" + this.mFtCancelMemoryFull + ", mFtFallbackAllFail=" + this.mFtFallbackAllFail + ", mRespondDisplay=" + this.mRespondDisplay + ", mEnableGroupChatListRetrieve=" + this.mEnableGroupChatListRetrieve + ", mEnableFtAutoResumable=" + this.mEnableFtAutoResumable + ", mLegacyLatching=" + this.mLegacyLatching + ", mExtAttImSlmMaxRecipients=" + this.mExtAttImSlmMaxRecipients + ", mExtAttImMSRPFtMaxSize=" + this.mExtAttImMSRPFtMaxSize + ", mfThttpDefaultPdn=" + this.mfThttpDefaultPdn + ", mPagerModeLimit=" + this.mPagerModeLimit + ", mBotPrivacyDisable=" + this.mBotPrivacyDisable + ", mSwitchOverSize=" + this.mSlmSwitchOverSize + ", mCatalogUri=" + this.mCatalogUri + ", mPlugInEnabled=" + this.mPlugInEnabled + "]";
    }
}
