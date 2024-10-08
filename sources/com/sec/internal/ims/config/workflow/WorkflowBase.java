package com.sec.internal.ims.config.workflow;

import android.app.PendingIntent;
import android.content.BroadcastReceiver;
import android.content.ContentResolver;
import android.content.ContentValues;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.SharedPreferences;
import android.database.ContentObserver;
import android.net.Network;
import android.net.NetworkRequest;
import android.net.TrafficStats;
import android.net.Uri;
import android.os.Handler;
import android.os.Looper;
import android.os.Message;
import android.os.Process;
import android.text.TextUtils;
import android.util.Log;
import com.sec.internal.constants.Mno;
import com.sec.internal.constants.ims.DiagnosisConstants;
import com.sec.internal.constants.ims.ImsConstants;
import com.sec.internal.constants.ims.config.ConfigConstants;
import com.sec.internal.constants.ims.os.PhoneConstants;
import com.sec.internal.constants.ims.settings.GlobalSettingsConstants;
import com.sec.internal.helper.AlarmTimer;
import com.sec.internal.helper.HashManager;
import com.sec.internal.helper.HttpRequest;
import com.sec.internal.helper.ImsSharedPrefHelper;
import com.sec.internal.helper.OmcCode;
import com.sec.internal.helper.SimUtil;
import com.sec.internal.helper.SimpleEventLog;
import com.sec.internal.ims.config.ConfigContract;
import com.sec.internal.ims.config.ConfigProvider;
import com.sec.internal.ims.config.PowerController;
import com.sec.internal.ims.config.SharedInfo;
import com.sec.internal.ims.config.adapters.DialogAdapterConsentDecorator;
import com.sec.internal.ims.config.exception.InvalidHeaderException;
import com.sec.internal.ims.config.exception.NoInitialDataException;
import com.sec.internal.ims.config.exception.UnknownStatusException;
import com.sec.internal.ims.core.sim.SimManagerFactory;
import com.sec.internal.ims.rcs.RcsPolicyManager;
import com.sec.internal.ims.registry.ImsRegistry;
import com.sec.internal.ims.servicemodules.im.strategy.IMnoStrategy;
import com.sec.internal.ims.settings.DeviceConfigManager;
import com.sec.internal.ims.settings.RcsPolicySettings;
import com.sec.internal.ims.util.ConfigUtil;
import com.sec.internal.ims.util.ImsUtil;
import com.sec.internal.imscr.LogClass;
import com.sec.internal.interfaces.ims.config.IConfigModule;
import com.sec.internal.interfaces.ims.config.IDialogAdapter;
import com.sec.internal.interfaces.ims.config.IHttpAdapter;
import com.sec.internal.interfaces.ims.config.IStorageAdapter;
import com.sec.internal.interfaces.ims.config.ITelephonyAdapter;
import com.sec.internal.interfaces.ims.config.IWorkflow;
import com.sec.internal.interfaces.ims.config.IXmlParserAdapter;
import com.sec.internal.interfaces.ims.core.IRegistrationManager;
import com.sec.internal.interfaces.ims.core.ISimManager;
import com.sec.internal.interfaces.ims.rcs.IRcsPolicyManager;
import com.sec.internal.log.IMSLog;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Calendar;
import java.util.Date;
import java.util.List;
import java.util.Locale;
import java.util.Map;

public abstract class WorkflowBase extends Handler implements IWorkflow {
    protected static int AUTO_CONFIG_MAX_FLOWCOUNT = 20;
    protected static final long AUTO_CONFIG_MAX_TIMEOUT = 60000;
    protected static final int AUTO_CONFIG_RETRY_INTERVAL = 300;
    protected static final String CHARSET = "utf-8";
    protected static final String CLIENT_VENDOR_INFO = "clientVendor";
    protected static final String CLIENT_VERSION_INFO = "clientVersion";
    protected static final int HANDLE_AUTO_CONFIG_ADS_CHANGED = 15;
    protected static final int HANDLE_AUTO_CONFIG_CLEAN_UP = 14;
    protected static final int HANDLE_AUTO_CONFIG_CLEAR_DB = 2;
    protected static final int HANDLE_AUTO_CONFIG_CLIENT_INFO_CHANGED = 16;
    protected static final int HANDLE_AUTO_CONFIG_DUALSIM = 6;
    protected static final int HANDLE_AUTO_CONFIG_FORCE = 0;
    protected static final int HANDLE_AUTO_CONFIG_GENERAL_ERROR_RETRY_TIMER_EXPIRED = 13;
    protected static final int HANDLE_AUTO_CONFIG_IMS_REGI_STATUS_CHANGED = 12;
    protected static final int HANDLE_AUTO_CONFIG_MOBILE_CONNECTION_FAILURE = 4;
    protected static final int HANDLE_AUTO_CONFIG_MOBILE_CONNECTION_SUCCESSFUL = 3;
    protected static final int HANDLE_AUTO_CONFIG_RESET = 8;
    protected static final int HANDLE_AUTO_CONFIG_SHARED_PREFERENCE_CHANGED = 10;
    protected static final int HANDLE_AUTO_CONFIG_SMS_DEFAULT_APPLICATION_CHANGED = 5;
    protected static final int HANDLE_AUTO_CONFIG_START = 1;
    protected static final int HANDLE_CUR_CONFIG_START = 11;
    protected static final int HANDLE_SHOW_MSISDN_DIALOG = 7;
    protected static final String INTENT_VALIDITY_TIMEOUT = "com.sec.internal.ims.config.workflow.validity_timeout";
    private static final String LAST_RCS_PROFILE = "lastRcsProfile";
    protected static final String LAST_SW_VERSION = "lastSwVersion";
    /* access modifiers changed from: private */
    public static String LOG_TAG = WorkflowBase.class.getSimpleName();
    protected static final int NOTIFY_AUTO_CONFIGURATION_COMPLETED = 52;
    protected static final String PREFERENCE_NAME = "workflowbase";
    protected static final String RCS_ENABLED_BY_USER = "rcsEnabledByUser";
    private static final String RCS_PROFILE = "rcsprofile";
    protected static final String RCS_PROFILE_INFO = "rcsProfile";
    protected static final String RCS_VERSION_INFO = "rcsVersion";
    protected static final String TIMESTAMP_FORMAT = "EEE, dd MMM yyyy HH:mm:ss ZZZZ";
    private boolean isGlobalsettingsObserverRegisted;
    protected String mClientPlatform;
    protected String mClientVersion;
    protected Context mContext;
    protected WorkflowCookieHandler mCookieHandler;
    protected IDialogAdapter mDialog;
    /* access modifiers changed from: private */
    public SimpleEventLog mEventLog;
    protected IHttpAdapter mHttp;
    protected boolean mHttpRedirect;
    protected String mIdentity;
    BroadcastReceiver mIntentReceiver;
    protected boolean mIsConfigOngoing;
    protected boolean mIsUsingcheckSetToGS;
    protected int mLastErrorCode;
    protected int mLastErrorCodeNonRemote;
    protected String mLastErrorMessage;
    protected Mno mMno;
    protected boolean mMobileNetwork;
    protected final IConfigModule mModule;
    protected WorkflowMsisdnHandler mMsisdnHandler;
    protected boolean mNeedToStopWork;
    protected Network mNetwork;
    protected NetworkRequest mNetworkRequest;
    protected WorkflowParamHandler mParamHandler;
    protected int mPhoneId;
    protected PowerController mPowerController;
    protected List<String> mRcsAppList;
    /* access modifiers changed from: private */
    public int mRcsAutoconfigSource;
    protected String mRcsCustomServerUrl;
    private final ContentObserver mRcsCustomServerUrlObserver;
    protected String mRcsEnabledByUser;
    protected String mRcsProfile;
    protected String mRcsProvisioningVersion;
    protected String mRcsUPProfile;
    protected String mRcsVersion;
    protected IRegistrationManager mRm;
    protected SharedInfo mSharedInfo;
    protected SharedPreferences mSharedPreferences;
    protected ISimManager mSm;
    protected boolean mStartForce;
    State mState;
    protected IStorageAdapter mStorage;
    protected ITelephonyAdapter mTelephonyAdapter;
    protected PendingIntent mValidityIntent;
    protected IXmlParserAdapter mXmlParser;

    public interface Workflow {
        public static final int AUTHORIZE = 4;
        public static final int FETCH_HTTP = 2;
        public static final int FETCH_HTTPS = 3;
        public static final int FETCH_OTP = 5;
        public static final int FINISH = 8;
        public static final int INITIALIZE = 1;
        public static final int PARSE = 6;
        public static final int STORE = 7;

        Workflow run() throws Exception;
    }

    public boolean checkNetworkConnectivity() {
        return true;
    }

    /* access modifiers changed from: protected */
    public abstract Workflow getNextWorkflow(int i);

    public IStorageAdapter getStorage() {
        return null;
    }

    /* access modifiers changed from: package-private */
    public abstract void work();

    protected abstract class Initialize implements Workflow {
        protected Initialize() {
        }

        public Workflow run() throws Exception {
            init();
            WorkflowBase workflowBase = WorkflowBase.this;
            if (workflowBase.mStartForce) {
                return workflowBase.getNextWorkflow(2);
            }
            int i = AnonymousClass3.$SwitchMap$com$sec$internal$ims$config$workflow$WorkflowBase$OpMode[workflowBase.getOpMode().ordinal()];
            return (i == 1 || i == 2 || i == 3) ? WorkflowBase.this.getNextWorkflow(2) : WorkflowBase.this.getNextWorkflow(8);
        }

        /* access modifiers changed from: protected */
        public void init() throws NoInitialDataException {
            WorkflowBase workflowBase = WorkflowBase.this;
            workflowBase.mSharedInfo.setUrl(workflowBase.mParamHandler.initUrl());
            WorkflowBase.this.mCookieHandler.clearCookie();
        }
    }

    /* renamed from: com.sec.internal.ims.config.workflow.WorkflowBase$3  reason: invalid class name */
    static /* synthetic */ class AnonymousClass3 {
        static final /* synthetic */ int[] $SwitchMap$com$sec$internal$ims$config$workflow$WorkflowBase$OpMode;

        /* JADX WARNING: Can't wrap try/catch for region: R(12:0|1|2|3|4|5|6|7|8|9|10|12) */
        /* JADX WARNING: Code restructure failed: missing block: B:13:?, code lost:
            return;
         */
        /* JADX WARNING: Failed to process nested try/catch */
        /* JADX WARNING: Missing exception handler attribute for start block: B:3:0x0012 */
        /* JADX WARNING: Missing exception handler attribute for start block: B:5:0x001d */
        /* JADX WARNING: Missing exception handler attribute for start block: B:7:0x0028 */
        /* JADX WARNING: Missing exception handler attribute for start block: B:9:0x0033 */
        static {
            /*
                com.sec.internal.ims.config.workflow.WorkflowBase$OpMode[] r0 = com.sec.internal.ims.config.workflow.WorkflowBase.OpMode.values()
                int r0 = r0.length
                int[] r0 = new int[r0]
                $SwitchMap$com$sec$internal$ims$config$workflow$WorkflowBase$OpMode = r0
                com.sec.internal.ims.config.workflow.WorkflowBase$OpMode r1 = com.sec.internal.ims.config.workflow.WorkflowBase.OpMode.ACTIVE     // Catch:{ NoSuchFieldError -> 0x0012 }
                int r1 = r1.ordinal()     // Catch:{ NoSuchFieldError -> 0x0012 }
                r2 = 1
                r0[r1] = r2     // Catch:{ NoSuchFieldError -> 0x0012 }
            L_0x0012:
                int[] r0 = $SwitchMap$com$sec$internal$ims$config$workflow$WorkflowBase$OpMode     // Catch:{ NoSuchFieldError -> 0x001d }
                com.sec.internal.ims.config.workflow.WorkflowBase$OpMode r1 = com.sec.internal.ims.config.workflow.WorkflowBase.OpMode.DISABLE_TEMPORARY     // Catch:{ NoSuchFieldError -> 0x001d }
                int r1 = r1.ordinal()     // Catch:{ NoSuchFieldError -> 0x001d }
                r2 = 2
                r0[r1] = r2     // Catch:{ NoSuchFieldError -> 0x001d }
            L_0x001d:
                int[] r0 = $SwitchMap$com$sec$internal$ims$config$workflow$WorkflowBase$OpMode     // Catch:{ NoSuchFieldError -> 0x0028 }
                com.sec.internal.ims.config.workflow.WorkflowBase$OpMode r1 = com.sec.internal.ims.config.workflow.WorkflowBase.OpMode.DORMANT     // Catch:{ NoSuchFieldError -> 0x0028 }
                int r1 = r1.ordinal()     // Catch:{ NoSuchFieldError -> 0x0028 }
                r2 = 3
                r0[r1] = r2     // Catch:{ NoSuchFieldError -> 0x0028 }
            L_0x0028:
                int[] r0 = $SwitchMap$com$sec$internal$ims$config$workflow$WorkflowBase$OpMode     // Catch:{ NoSuchFieldError -> 0x0033 }
                com.sec.internal.ims.config.workflow.WorkflowBase$OpMode r1 = com.sec.internal.ims.config.workflow.WorkflowBase.OpMode.DISABLE     // Catch:{ NoSuchFieldError -> 0x0033 }
                int r1 = r1.ordinal()     // Catch:{ NoSuchFieldError -> 0x0033 }
                r2 = 4
                r0[r1] = r2     // Catch:{ NoSuchFieldError -> 0x0033 }
            L_0x0033:
                int[] r0 = $SwitchMap$com$sec$internal$ims$config$workflow$WorkflowBase$OpMode     // Catch:{ NoSuchFieldError -> 0x003e }
                com.sec.internal.ims.config.workflow.WorkflowBase$OpMode r1 = com.sec.internal.ims.config.workflow.WorkflowBase.OpMode.DISABLE_PERMANENTLY     // Catch:{ NoSuchFieldError -> 0x003e }
                int r1 = r1.ordinal()     // Catch:{ NoSuchFieldError -> 0x003e }
                r2 = 5
                r0[r1] = r2     // Catch:{ NoSuchFieldError -> 0x003e }
            L_0x003e:
                return
            */
            throw new UnsupportedOperationException("Method not decompiled: com.sec.internal.ims.config.workflow.WorkflowBase.AnonymousClass3.<clinit>():void");
        }
    }

    protected abstract class FetchHttp implements Workflow {
        protected FetchHttp() {
        }

        public Workflow run() throws Exception {
            setHttpHeader();
            WorkflowBase workflowBase = WorkflowBase.this;
            workflowBase.mSharedInfo.setHttpResponse(workflowBase.getHttpResponse());
            WorkflowBase workflowBase2 = WorkflowBase.this;
            return workflowBase2.handleResponse(this, workflowBase2.mSharedInfo.getHttpResponse().getStatusCode());
        }

        /* access modifiers changed from: protected */
        public void setHttpHeader() {
            WorkflowBase.this.mSharedInfo.setHttpDefault();
        }
    }

    protected abstract class FetchHttps implements Workflow {
        /* access modifiers changed from: protected */
        public abstract void setHttps();

        protected FetchHttps() {
        }

        public Workflow run() throws Exception {
            setHttps();
            WorkflowBase workflowBase = WorkflowBase.this;
            workflowBase.mSharedInfo.setHttpResponse(workflowBase.getHttpResponse());
            WorkflowBase workflowBase2 = WorkflowBase.this;
            return workflowBase2.handleResponse(this, workflowBase2.mSharedInfo.getHttpResponse().getStatusCode());
        }
    }

    protected abstract class Parse implements Workflow {
        protected Parse() {
        }

        public Workflow run() throws Exception {
            Map<String, String> parsedXmlFromBody = WorkflowBase.this.mParamHandler.getParsedXmlFromBody();
            if (WorkflowBase.this.mParamHandler.isRequiredAuthentication(parsedXmlFromBody)) {
                return WorkflowBase.this.getNextWorkflow(4);
            }
            parseParam(parsedXmlFromBody);
            WorkflowBase.this.mSharedInfo.setParsedXml(parsedXmlFromBody);
            return WorkflowBase.this.getNextWorkflow(7);
        }

        /* access modifiers changed from: protected */
        public void parseParam(Map<String, String> map) {
            WorkflowBase.this.mParamHandler.parseParam(map);
        }
    }

    protected abstract class Authorize implements Workflow {
        protected Authorize() {
        }

        public Workflow run() throws Exception {
            WorkflowBase.this.mPowerController.release();
            String otp = getOtp();
            String r1 = WorkflowBase.LOG_TAG;
            int i = WorkflowBase.this.mPhoneId;
            IMSLog.i(r1, i, "otp: " + IMSLog.checker(otp));
            if (otp == null) {
                return WorkflowBase.this.getNextWorkflow(8);
            }
            WorkflowBase.this.mSharedInfo.setOtp(otp);
            WorkflowBase.this.mPowerController.lock();
            return WorkflowBase.this.getNextWorkflow(5);
        }

        /* access modifiers changed from: protected */
        public String getOtp() {
            return WorkflowBase.this.mTelephonyAdapter.getOtp();
        }
    }

    protected abstract class FetchOtp implements Workflow {
        protected FetchOtp() {
        }

        public Workflow run() throws Exception {
            WorkflowBase.this.mSharedInfo.setHttpClean();
            setHttp();
            WorkflowBase workflowBase = WorkflowBase.this;
            workflowBase.mSharedInfo.setHttpResponse(workflowBase.getHttpResponse());
            WorkflowBase workflowBase2 = WorkflowBase.this;
            return workflowBase2.handleResponse(this, workflowBase2.mSharedInfo.getHttpResponse().getStatusCode());
        }

        /* access modifiers changed from: protected */
        public void setHttp() {
            SharedInfo sharedInfo = WorkflowBase.this.mSharedInfo;
            sharedInfo.addHttpParam(ConfigConstants.PNAME.OTP, sharedInfo.getOtp());
        }
    }

    protected abstract class Store implements Workflow {
        public abstract Workflow run() throws Exception;

        protected Store() {
        }
    }

    protected abstract class Finish implements Workflow {
        protected Finish() {
        }

        public Workflow run() throws Exception {
            if (WorkflowBase.this.mSharedInfo.getHttpResponse() != null) {
                WorkflowBase workflowBase = WorkflowBase.this;
                workflowBase.setLastErrorCode(workflowBase.mSharedInfo.getHttpResponse().getStatusCode());
            }
            IMSLog.i(WorkflowBase.LOG_TAG, WorkflowBase.this.mPhoneId, "workflow is finished");
            return null;
        }
    }

    public enum OpMode {
        ACTIVE(1),
        DISABLE_TEMPORARY(0),
        DISABLE_PERMANENTLY(-1),
        DISABLE(-2),
        DORMANT(-3),
        DISABLE_RCS_BY_USER(-4),
        ENABLE_RCS_BY_USER(-5),
        DISABLE_TEMPORARY_BY_RCS_DISABLED_STATE(-6),
        DISABLE_PERMANENTLY_BY_RCS_DISABLED_STATE(-7),
        DISABLE_BY_RCS_DISABLED_STATE(-8),
        DORMANT_BY_RCS_DISABLED_STATE(-9),
        TURNEDOFF_BY_RCS_DISABLED_STATE(-10),
        DISABLED_TERMS_AND_CONDIDIONTS_REJECTED(-11),
        NONE(-12);
        
        int mValue;

        private OpMode(int i) {
            this.mValue = i;
        }

        /* access modifiers changed from: package-private */
        public int value() {
            return this.mValue;
        }
    }

    public WorkflowBase(Looper looper, Context context, IConfigModule iConfigModule, Mno mno, ITelephonyAdapter iTelephonyAdapter, IStorageAdapter iStorageAdapter, IHttpAdapter iHttpAdapter, IXmlParserAdapter iXmlParserAdapter, IDialogAdapter iDialogAdapter, int i) {
        super(looper);
        this.mLastErrorCode = IWorkflow.DEFAULT_ERROR_CODE;
        this.mLastErrorCodeNonRemote = 200;
        this.mLastErrorMessage = "";
        this.mStartForce = false;
        this.mMobileNetwork = false;
        this.mHttpRedirect = false;
        this.mIsConfigOngoing = false;
        this.mIsUsingcheckSetToGS = false;
        this.mRcsCustomServerUrl = null;
        this.mRcsUPProfile = null;
        this.mNeedToStopWork = false;
        this.mRcsAutoconfigSource = -1;
        this.mIdentity = null;
        this.isGlobalsettingsObserverRegisted = false;
        this.mNetwork = null;
        this.mNetworkRequest = null;
        this.mValidityIntent = null;
        this.mIntentReceiver = new BroadcastReceiver() {
            public void onReceive(Context context, Intent intent) {
                if (!WorkflowBase.this.checkRcsSwitchEur()) {
                    Log.i(WorkflowBase.LOG_TAG, "onReceive: validity period expired. but RCS is switch off. it should perform when switch on.");
                } else if (WorkflowBase.INTENT_VALIDITY_TIMEOUT.equals(intent.getAction())) {
                    WorkflowBase workflowBase = WorkflowBase.this;
                    workflowBase.mValidityIntent = null;
                    workflowBase.mModule.setAcsTryReason(workflowBase.mPhoneId, DiagnosisConstants.RCSA_ATRE.EXPIRE_VALIDITY);
                    if (WorkflowBase.this.isNetworkAvailable()) {
                        int intExtra = intent.getIntExtra(PhoneConstants.PHONE_KEY, 0);
                        WorkflowBase workflowBase2 = WorkflowBase.this;
                        if (workflowBase2.mPhoneId == intExtra) {
                            workflowBase2.mEventLog.logAndAdd(WorkflowBase.this.mPhoneId, "onReceive: Same phoneId with this intent");
                            IMSLog.c(LogClass.WFB_INTENT_VALIDITY_TIMEOUT, "onReceive: Same phoneId");
                            WorkflowBase workflowBase3 = WorkflowBase.this;
                            workflowBase3.mModule.startAutoConfig(false, (Message) null, workflowBase3.mPhoneId);
                        }
                    } else {
                        Handler handler = WorkflowBase.this.mModule.getHandler();
                        WorkflowBase workflowBase4 = WorkflowBase.this;
                        handler.sendMessage(workflowBase4.obtainMessage(17, Integer.valueOf(workflowBase4.mPhoneId)));
                    }
                    String r4 = WorkflowBase.LOG_TAG;
                    Log.i(r4, "onReceive: validity period expired. start config, mMobileNetwork = " + WorkflowBase.this.mMobileNetwork);
                }
            }
        };
        this.mRcsCustomServerUrlObserver = new ContentObserver(this) {
            public void onChange(boolean z, Uri uri) {
                WorkflowBase workflowBase = WorkflowBase.this;
                if (TextUtils.equals(workflowBase.mIdentity, workflowBase.mTelephonyAdapter.getIdentityByPhoneId(workflowBase.mPhoneId)) && uri != null && uri.getPath().startsWith(GlobalSettingsConstants.CONTENT_URI.getPath())) {
                    String string = ImsRegistry.getString(WorkflowBase.this.mPhoneId, GlobalSettingsConstants.RCS.CUSTOM_CONFIG_SERVER_URL, "");
                    int autoconfigSourceWithFeature = ConfigUtil.getAutoconfigSourceWithFeature(WorkflowBase.this.mPhoneId, 0);
                    String string2 = ImsRegistry.getString(WorkflowBase.this.mPhoneId, GlobalSettingsConstants.RCS.UP_PROFILE, "");
                    if (!TextUtils.equals(WorkflowBase.this.mRcsCustomServerUrl, string) || WorkflowBase.this.mRcsAutoconfigSource != autoconfigSourceWithFeature || !TextUtils.equals(WorkflowBase.this.mRcsUPProfile, string2)) {
                        String r1 = WorkflowBase.LOG_TAG;
                        int i = WorkflowBase.this.mPhoneId;
                        IMSLog.i(r1, i, "new rcs_custom_config_server_url=" + string + ", new rcs_autoconfig_source=" + autoconfigSourceWithFeature + ", new rcs_up_profile=" + string2);
                        WorkflowBase workflowBase2 = WorkflowBase.this;
                        workflowBase2.mRcsCustomServerUrl = string;
                        workflowBase2.mRcsAutoconfigSource = autoconfigSourceWithFeature;
                        WorkflowBase.this.removeMessages(2);
                        WorkflowBase.this.sendEmptyMessage(2);
                    }
                }
                WorkflowBase workflowBase3 = WorkflowBase.this;
                if (workflowBase3.mIsUsingcheckSetToGS) {
                    workflowBase3.mParamHandler.checkSetToGS((Map<String, String>) null);
                }
                WorkflowBase workflowBase4 = WorkflowBase.this;
                workflowBase4.mIdentity = workflowBase4.mTelephonyAdapter.getIdentityByPhoneId(workflowBase4.mPhoneId);
            }
        };
        this.mPhoneId = i;
        IMSLog.i(LOG_TAG, i, "created");
        this.mEventLog = new SimpleEventLog(context, "Workflow", 500);
        this.mContext = context;
        this.mModule = iConfigModule;
        this.mTelephonyAdapter = iTelephonyAdapter;
        this.mStorage = iStorageAdapter;
        this.mHttp = iHttpAdapter;
        this.mXmlParser = iXmlParserAdapter;
        this.mRcsProfile = ConfigUtil.getRcsProfileLoaderInternalWithFeature(context, mno.getName(), this.mPhoneId);
        this.mRcsVersion = ImsRegistry.getString(this.mPhoneId, "rcs_version", "6.0");
        this.mClientPlatform = ImsRegistry.getString(this.mPhoneId, GlobalSettingsConstants.RCS.RCS_CLIENT_PLATFORM, ConfigConstants.PVALUE.CLIENT_VERSION_NAME);
        this.mClientVersion = ImsRegistry.getString(this.mPhoneId, GlobalSettingsConstants.RCS.RCS_CLIENT_VERSION, "6.0");
        this.mRcsProvisioningVersion = ImsRegistry.getString(this.mPhoneId, GlobalSettingsConstants.RCS.RCS_PROVISIONING_VERSION, "2.0");
        this.mRcsAppList = Arrays.asList(ImsRegistry.getStringArray(this.mPhoneId, GlobalSettingsConstants.RCS.RCS_APP_LIST, new String[0]));
        if (iDialogAdapter != null) {
            this.mDialog = new DialogAdapterConsentDecorator(iDialogAdapter, i);
        }
        this.mSharedPreferences = this.mContext.getSharedPreferences(PREFERENCE_NAME, 0);
        this.mPowerController = new PowerController(context, 60000);
        this.mState = new IdleState();
        this.mSm = SimManagerFactory.getSimManagerFromSimSlot(this.mPhoneId);
        this.mRm = ImsRegistry.getRegistrationManager();
        this.mMno = mno;
        this.mCookieHandler = new WorkflowCookieHandler(this, this.mPhoneId);
        this.mParamHandler = new WorkflowParamHandler(this, this.mPhoneId, this.mTelephonyAdapter);
        this.mMsisdnHandler = new WorkflowMsisdnHandler(this);
        createSharedInfo();
        IntentFilter intentFilter = new IntentFilter();
        intentFilter.addAction(INTENT_VALIDITY_TIMEOUT);
        this.mContext.registerReceiver(this.mIntentReceiver, intentFilter);
        registerGlobalSettingsObserver();
    }

    /* JADX WARNING: Illegal instructions before constructor call */
    /* Code decompiled incorrectly, please refer to instructions dump. */
    public WorkflowBase(android.os.Looper r12, android.content.Context r13, com.sec.internal.interfaces.ims.config.IConfigModule r14, com.sec.internal.constants.Mno r15, int r16) {
        /*
            r11 = this;
            r2 = r13
            r3 = r14
            r10 = r16
            com.sec.internal.ims.config.adapters.TelephonyAdapterPrimaryDevice r5 = new com.sec.internal.ims.config.adapters.TelephonyAdapterPrimaryDevice
            r5.<init>(r13, r14, r10)
            com.sec.internal.ims.config.adapters.StorageAdapter r6 = new com.sec.internal.ims.config.adapters.StorageAdapter
            r6.<init>()
            com.sec.internal.ims.config.adapters.HttpAdapter r7 = new com.sec.internal.ims.config.adapters.HttpAdapter
            r7.<init>(r10)
            com.sec.internal.ims.config.adapters.XmlParserAdapter r8 = new com.sec.internal.ims.config.adapters.XmlParserAdapter
            r8.<init>()
            com.sec.internal.ims.config.adapters.DialogAdapter r9 = new com.sec.internal.ims.config.adapters.DialogAdapter
            r9.<init>(r13, r14)
            r0 = r11
            r1 = r12
            r4 = r15
            r0.<init>(r1, r2, r3, r4, r5, r6, r7, r8, r9, r10)
            return
        */
        throw new UnsupportedOperationException("Method not decompiled: com.sec.internal.ims.config.workflow.WorkflowBase.<init>(android.os.Looper, android.content.Context, com.sec.internal.interfaces.ims.config.IConfigModule, com.sec.internal.constants.Mno, int):void");
    }

    public void handleMessage(Message message) {
        String str = LOG_TAG;
        int i = this.mPhoneId;
        IMSLog.i(str, i, "message: " + message.what);
        addEventLog(LOG_TAG + ": message: " + message.what);
        int i2 = message.what;
        if (i2 == 0) {
            IMSLog.i(LOG_TAG, this.mPhoneId, "forced startAutoConfig");
            removeMessages(0);
            Mno simMno = SimUtil.getSimMno(this.mPhoneId);
            if (ConfigUtil.isRcsChn(simMno) || ConfigUtil.isRcsEur(simMno)) {
                resetStorage();
            }
            this.mStartForce = true;
        } else if (i2 != 1) {
            if (i2 == 2) {
                IMSLog.i(LOG_TAG, this.mPhoneId, "clearStorage");
                clearStorage();
                return;
            } else if (i2 == 6) {
                IMSLog.i(LOG_TAG, this.mPhoneId, "autoconfig dualsim");
                resetStorage();
                sendEmptyMessage(1);
                return;
            } else if (i2 != 7) {
                IMSLog.i(LOG_TAG, this.mPhoneId, "unknown message!!!");
                return;
            } else {
                return;
            }
        }
        removeMessages(1);
        if (this.mIsConfigOngoing) {
            IMSLog.i(LOG_TAG, this.mPhoneId, "AutoConfig:Already started");
            return;
        }
        this.mIsConfigOngoing = true;
        IMSLog.i(LOG_TAG, this.mPhoneId, "AutoConfig:START");
        this.mPowerController.lock();
        int version = getVersion();
        if (needScheduleAutoconfig(this.mPhoneId)) {
            scheduleAutoconfig(version);
        }
        int version2 = getVersion();
        String str2 = LOG_TAG;
        int i3 = this.mPhoneId;
        IMSLog.i(str2, i3, "oldVersion : " + version + " newVersion : " + version2);
        IMSLog.i(LOG_TAG, this.mPhoneId, "AutoConfig:FINISH");
        setCompleted(true);
        this.mModule.getHandler().removeMessages(3, Integer.valueOf(this.mPhoneId));
        this.mModule.getHandler().sendMessage(obtainMessage(3, version, version2, Integer.valueOf(this.mPhoneId)));
        this.mStartForce = false;
        this.mPowerController.release();
        this.mIsConfigOngoing = false;
    }

    /* access modifiers changed from: protected */
    public boolean isNetworkAvailable() {
        String networkType = ConfigUtil.getNetworkType(this.mPhoneId);
        if (networkType.contains("internet") && this.mModule.getAvailableNetworkForNetworkType(this.mPhoneId, 1) != null) {
            return true;
        }
        if (networkType.contains(DeviceConfigManager.IMS) && this.mModule.getAvailableNetworkForNetworkType(this.mPhoneId, 2) != null) {
            return true;
        }
        if (!networkType.contains("wifi") || this.mModule.getAvailableNetworkForNetworkType(this.mPhoneId, 3) == null) {
            return false;
        }
        return true;
    }

    /* access modifiers changed from: protected */
    public boolean checkRcsSwitchEur() {
        boolean z = true;
        if (ConfigUtil.isRcsEur(SimUtil.getSimMno(this.mPhoneId))) {
            if (ImsConstants.SystemSettings.getRcsUserSetting(this.mContext, 0, this.mPhoneId) != 1) {
                z = false;
            }
            IMSLog.i(LOG_TAG, this.mPhoneId, "RCS switch : " + z);
        }
        return z;
    }

    /* access modifiers changed from: protected */
    public void scheduleAutoconfig(int i) {
        IMSLog.i(LOG_TAG, this.mPhoneId, "scheduleAutoconfig enter");
        int versionFromServer = getVersionFromServer();
        String str = LOG_TAG;
        int i2 = this.mPhoneId;
        IMSLog.i(str, i2, "scheduleAutoconfig: getOpMode(): " + getOpMode() + " currentVersion: " + i + " getVersionBackup(): " + getVersionBackup() + " versionFromServer: " + versionFromServer);
        addEventLog(LOG_TAG + ": scheduleAutoconfig: getOpMode(): " + getOpMode() + " currentVersion: " + i + " getVersionBackup(): " + getVersionBackup() + " versionFromServer: " + versionFromServer);
        IMSLog.c(LogClass.WFB_VERS, this.mPhoneId + ",OP:" + getOpMode() + ",CV:" + i + ",BV:" + getVersionBackup() + ",SV:" + versionFromServer);
        if (this.mStartForce) {
            cancelValidityTimer();
            IMSLog.i(LOG_TAG, this.mPhoneId, "Query autoconfig server now: force");
            work();
        } else if (i == -1 || versionFromServer == -1) {
            IMSLog.i(LOG_TAG, this.mPhoneId, "Skip querying autoconfig server since current version is -1");
        } else {
            boolean z = true;
            if (i == -2 || versionFromServer == -2) {
                this.mStartForce = true;
                IMSLog.i(LOG_TAG, this.mPhoneId, "Autoconfig version is -2. If scheduleAutoconfig was called, it means that user enabled RCS in settings. Force autoconfig.");
                scheduleAutoconfig(i);
                return;
            }
            long nextAutoconfigTime = getNextAutoconfigTime();
            String str2 = LOG_TAG;
            int i3 = this.mPhoneId;
            IMSLog.i(str2, i3, "nextAutoconfigTime=" + nextAutoconfigTime);
            Date date = new Date();
            int time = (int) ((nextAutoconfigTime - date.getTime()) / 1000);
            int validity = getValidity();
            if (validity > 0 && time > validity) {
                IMSLog.i(LOG_TAG, this.mPhoneId, "remainValidity > getValidity()");
                setNextAutoconfigTime(date.getTime() + (((long) validity) * 1000));
                time = validity;
            }
            String str3 = LOG_TAG;
            int i4 = this.mPhoneId;
            IMSLog.i(str3, i4, "remainValidity=" + time);
            if (time <= 0) {
                if (SimUtil.getSimMno(this.mPhoneId) == Mno.TCE) {
                    IMSLog.i(LOG_TAG, this.mPhoneId, "waiting for query autoconfig");
                    z = this.mDialog.getNextCancel();
                }
                if (z) {
                    IMSLog.i(LOG_TAG, this.mPhoneId, "Query autoconfig server now");
                    work();
                    return;
                }
                IMSLog.i(LOG_TAG, this.mPhoneId, "Query autoconfig server - cancel by user");
                if (getVersion() > 0) {
                    setVersion(OpMode.DISABLE_TEMPORARY.value());
                }
            } else if (nextAutoconfigTime > 0) {
                String str4 = LOG_TAG;
                int i5 = this.mPhoneId;
                IMSLog.i(str4, i5, "Query autoconfig server after " + time + " seconds");
                IMSLog.c(LogClass.WFB_REMAIN_VALIDITY, this.mPhoneId + ",RVAL:" + time);
                addEventLog(LOG_TAG + ": Query autoconfig server after " + time + " seconds");
                setValidityTimer(time);
            }
        }
    }

    public void init() {
        this.mState.init();
    }

    public void cleanup() {
        IMSLog.i(LOG_TAG, this.mPhoneId, "cleanup");
        this.mTelephonyAdapter.cleanup();
        this.mState.cleanup();
        this.mDialog.cleanup();
        this.mPowerController.cleanup();
        this.mHttp.close();
        unregisterGlobalSettingsObserver();
        this.mContext.unregisterReceiver(this.mIntentReceiver);
        cancelValidityTimer();
    }

    public void reInitIfNeeded() {
        if (this.mTelephonyAdapter.isReady() && SimUtil.isSoftphoneEnabled() && !(this.mState instanceof ReadyState)) {
            IMSLog.i(LOG_TAG, this.mPhoneId, "reInitIfNeeded: identity changed, re-init storage");
            addEventLog(LOG_TAG + ": reInitIfNeeded: identity changed, re-init storage");
            this.mStorage.close();
            init();
        }
    }

    public void clearAutoConfigStorage(DiagnosisConstants.RCSA_TDRE rcsa_tdre) {
        IMSLog.i(LOG_TAG, this.mPhoneId, "clearAutoConfigStorage");
        removeMessages(2);
        sendEmptyMessage(2);
        this.mModule.setTokenDeletedReason(this.mPhoneId, rcsa_tdre);
    }

    public void clearToken(DiagnosisConstants.RCSA_TDRE rcsa_tdre) {
        IMSLog.i(LOG_TAG, this.mPhoneId, "clearToken");
        setToken("", rcsa_tdre);
    }

    public void removeValidToken() {
        IMSLog.i(LOG_TAG, this.mPhoneId, "remove valid token");
        ImsSharedPrefHelper.remove(this.mPhoneId, this.mContext, ImsSharedPrefHelper.VALID_RCS_CONFIG, "IMSI_" + SimManagerFactory.getImsiFromPhoneId(this.mPhoneId));
    }

    public void startAutoConfig(boolean z) {
        this.mState.startAutoConfig(z);
    }

    public void startAutoConfigDualsim(boolean z) {
        this.mState.startAutoConfigDualsim(z);
    }

    public void forceAutoConfig(boolean z) {
        this.mState.forceAutoConfig(z);
    }

    public Map<String, String> read(String str) {
        return this.mState.read(str);
    }

    public void forceAutoConfigNeedResetConfig(boolean z) {
        setOpMode(OpMode.DISABLE_TEMPORARY, (Map<String, String>) null);
        this.mState.forceAutoConfig(z);
    }

    public boolean isConfigOngoing() {
        return this.mIsConfigOngoing;
    }

    public void stopWorkFlow() {
        IMSLog.i(LOG_TAG, this.mPhoneId, "Stop work flow in workflow state");
        this.mNeedToStopWork = true;
        this.mIsConfigOngoing = false;
        this.mHttp.close();
    }

    public void handleMSISDNDialog() {
        this.mState.handleMSISDNDialog();
    }

    public void onDefaultSmsPackageChanged() {
        this.mState.onDefaultSmsPackageChanged();
    }

    static abstract class State implements IWorkflow {
        public void cleanup() {
        }

        public void closeStorage() {
        }

        public void forceAutoConfig(boolean z) {
        }

        public void forceAutoConfigNeedResetConfig(boolean z) {
        }

        public IStorageAdapter getStorage() {
            return null;
        }

        public void handleMSISDNDialog() {
        }

        public void onDefaultSmsPackageChanged() {
        }

        public Map<String, String> read(String str) {
            return null;
        }

        public void startAutoConfig(boolean z) {
        }

        public void startAutoConfigDualsim(boolean z) {
        }

        State() {
        }
    }

    protected class IdleState extends State {
        protected IdleState() {
        }

        public void init() {
            if (WorkflowBase.this.initStorage()) {
                WorkflowBase workflowBase = WorkflowBase.this;
                workflowBase.handleSwVersionChange(workflowBase.getLastSwVersion());
                WorkflowBase workflowBase2 = WorkflowBase.this;
                workflowBase2.handleRcsProfileChange(workflowBase2.getLastRcsProfile());
                WorkflowBase workflowBase3 = WorkflowBase.this;
                workflowBase3.mState = new ReadyState();
            } else if (WorkflowBase.this.mMno.isRjil()) {
                WorkflowBase.this.setCompleted(true);
                WorkflowBase.this.mModule.getHandler().removeMessages(3);
                Handler handler = WorkflowBase.this.mModule.getHandler();
                WorkflowBase workflowBase4 = WorkflowBase.this;
                handler.sendMessage(workflowBase4.obtainMessage(3, 0, 0, Integer.valueOf(workflowBase4.mPhoneId)));
            }
            SimpleEventLog r0 = WorkflowBase.this.mEventLog;
            int i = WorkflowBase.this.mPhoneId;
            r0.logAndAdd(i, "init: " + WorkflowBase.this.mState.getClass().getSimpleName());
        }
    }

    protected class ReadyState extends State {
        protected ReadyState() {
        }

        public void init() {
            IMSLog.i(WorkflowBase.LOG_TAG, WorkflowBase.this.mPhoneId, "already initialized");
        }

        public void cleanup() {
            WorkflowBase.this.mIsConfigOngoing = false;
        }

        public void startAutoConfig(boolean z) {
            String r0 = WorkflowBase.LOG_TAG;
            int i = WorkflowBase.this.mPhoneId;
            IMSLog.i(r0, i, "startAutoConfig mobile:" + z + " Config status =" + WorkflowBase.this.mIsConfigOngoing);
            WorkflowBase workflowBase = WorkflowBase.this;
            workflowBase.mMobileNetwork = z;
            if (!workflowBase.mIsConfigOngoing && !workflowBase.hasMessages(1)) {
                WorkflowBase.this.sendEmptyMessage(1);
            }
        }

        public void startAutoConfigDualsim(boolean z) {
            String r0 = WorkflowBase.LOG_TAG;
            int i = WorkflowBase.this.mPhoneId;
            IMSLog.i(r0, i, "startAutoConfigDualsim mobile:" + z);
            WorkflowBase workflowBase = WorkflowBase.this;
            workflowBase.mMobileNetwork = z;
            workflowBase.sendEmptyMessage(6);
        }

        public void forceAutoConfig(boolean z) {
            String r0 = WorkflowBase.LOG_TAG;
            int i = WorkflowBase.this.mPhoneId;
            IMSLog.i(r0, i, "forceAutoConfig mobile:" + z);
            WorkflowBase workflowBase = WorkflowBase.this;
            workflowBase.mMobileNetwork = z;
            workflowBase.sendEmptyMessage(0);
        }

        public Map<String, String> read(String str) {
            return WorkflowBase.this.mStorage.readAll(str);
        }

        public void forceAutoConfigNeedResetConfig(boolean z) {
            String r0 = WorkflowBase.LOG_TAG;
            int i = WorkflowBase.this.mPhoneId;
            IMSLog.i(r0, i, "forceAutoConfigNeedResetConfig mobile:" + z);
            WorkflowBase workflowBase = WorkflowBase.this;
            workflowBase.mMobileNetwork = z;
            workflowBase.setOpMode(OpMode.DISABLE_TEMPORARY, (Map<String, String>) null);
            WorkflowBase.this.sendEmptyMessage(0);
        }

        public void handleMSISDNDialog() {
            IMSLog.i(WorkflowBase.LOG_TAG, WorkflowBase.this.mPhoneId, "handleMSISDNDialog()");
            WorkflowBase.this.sendEmptyMessage(7);
        }

        public void closeStorage() {
            WorkflowBase.this.mStorage.close();
        }

        public void onDefaultSmsPackageChanged() {
            IMSLog.i(WorkflowBase.LOG_TAG, WorkflowBase.this.mPhoneId, "onDefaultSmsPackageChanged");
            WorkflowBase.this.sendEmptyMessage(5);
        }
    }

    /* access modifiers changed from: protected */
    public IHttpAdapter.Response getHttpResponse() {
        TrafficStats.setThreadStatsTag(Process.myTid());
        this.mHttp.close();
        this.mHttp.setHeaders(this.mSharedInfo.getHttpHeaders());
        this.mHttp.setParams(this.mSharedInfo.getHttpParams());
        this.mHttp.setMethod(this.mSharedInfo.getUserMethod());
        this.mSharedInfo.setUserMethod("GET");
        this.mHttp.setContext(this.mContext);
        this.mHttp.open(this.mSharedInfo.getUrl());
        String str = LOG_TAG;
        int i = this.mPhoneId;
        IMSLog.s(str, i, "request starts " + this.mSharedInfo.getUrl());
        IHttpAdapter.Response request = this.mHttp.request();
        this.mHttp.close();
        return request;
    }

    public int getLastErrorCode() {
        return this.mLastErrorCode;
    }

    /* access modifiers changed from: protected */
    public void setLastErrorCode(int i) {
        IMSLog.c(LogClass.WFB_LAST_ERROR_CODE, this.mPhoneId + ",LEC:" + i);
        this.mLastErrorCode = i;
    }

    /* access modifiers changed from: protected */
    public String getLastErrorMessage() {
        return this.mLastErrorMessage;
    }

    /* access modifiers changed from: protected */
    public void setLastErrorMessage(String str) {
        this.mLastErrorMessage = str;
    }

    /* access modifiers changed from: protected */
    public Workflow handleResponse(Workflow workflow, int i) throws InvalidHeaderException, UnknownStatusException {
        String str = LOG_TAG;
        int i2 = this.mPhoneId;
        IMSLog.i(str, i2, "handleResponse: " + i);
        addEventLog(LOG_TAG + ": handleResponse: " + i);
        this.mLastErrorCode = i;
        if (i == 0) {
            IMSLog.i(LOG_TAG, this.mPhoneId, "RCS configuration server is unreachable");
            return getNextWorkflow(8);
        } else if (i == 200) {
            IMSLog.i(LOG_TAG, this.mPhoneId, "200 ok received and it's normal case");
            if (workflow instanceof FetchHttp) {
                return getNextWorkflow(3);
            }
            if ((workflow instanceof FetchHttps) || (workflow instanceof FetchOtp)) {
                return getNextWorkflow(6);
            }
            return null;
        } else if (i == 403) {
            IMSLog.i(LOG_TAG, this.mPhoneId, "set version to zero");
            setOpMode(OpMode.DISABLE_TEMPORARY, (Map<String, String>) null);
            return getNextWorkflow(8);
        } else if (i == 500) {
            IMSLog.i(LOG_TAG, this.mPhoneId, "internal server error");
            return getNextWorkflow(8);
        } else if (i == 503) {
            long j = getretryAfterTime();
            String str2 = LOG_TAG;
            int i3 = this.mPhoneId;
            IMSLog.i(str2, i3, "retry after " + j + " sec");
            sleep(j * 1000);
            return getNextWorkflow(3);
        } else if (i != 511) {
            switch (i) {
                case 800:
                case 801:
                    IMSLog.i(LOG_TAG, this.mPhoneId, "SSL error happened");
                    return getNextWorkflow(8);
                case 802:
                case 803:
                case 804:
                    IMSLog.i(LOG_TAG, this.mPhoneId, "Socket error happened");
                    return getNextWorkflow(8);
                case 805:
                    IMSLog.i(LOG_TAG, this.mPhoneId, "Unknown Host error happened");
                    return getNextWorkflow(8);
                default:
                    throw new UnknownStatusException("unknown http status code");
            }
        } else {
            IMSLog.i(LOG_TAG, this.mPhoneId, "The token isn't valid");
            setToken("", DiagnosisConstants.RCSA_TDRE.INVALID_TOKEN);
            removeValidToken();
            return getNextWorkflow(1);
        }
    }

    /* JADX DEBUG: Multi-variable search result rejected for TypeSearchVarInfo{r9v19, resolved type: java.lang.Object} */
    /* JADX DEBUG: Multi-variable search result rejected for TypeSearchVarInfo{r4v2, resolved type: java.lang.String} */
    /* access modifiers changed from: protected */
    /* JADX WARNING: Code restructure failed: missing block: B:21:0x0066, code lost:
        throw new com.sec.internal.ims.config.exception.UnknownStatusException("unknown http status code");
     */
    /* JADX WARNING: Multi-variable type inference failed */
    /* Code decompiled incorrectly, please refer to instructions dump. */
    public com.sec.internal.ims.config.workflow.WorkflowBase.Workflow handleResponse2(com.sec.internal.ims.config.workflow.WorkflowBase.Workflow r8, com.sec.internal.ims.config.workflow.WorkflowBase.Workflow r9, com.sec.internal.ims.config.workflow.WorkflowBase.Workflow r10) throws com.sec.internal.ims.config.exception.InvalidHeaderException, com.sec.internal.ims.config.exception.UnknownStatusException, java.net.ConnectException {
        /*
            r7 = this;
            com.sec.internal.ims.config.SharedInfo r0 = r7.mSharedInfo
            com.sec.internal.interfaces.ims.config.IHttpAdapter$Response r0 = r0.getHttpResponse()
            int r0 = r0.getStatusCode()
            r7.setLastErrorCode(r0)
            java.lang.String r0 = LOG_TAG
            int r1 = r7.mPhoneId
            java.lang.StringBuilder r2 = new java.lang.StringBuilder
            r2.<init>()
            java.lang.String r3 = "handleResponse2: mLastErrorCode: "
            r2.append(r3)
            int r3 = r7.getLastErrorCode()
            r2.append(r3)
            java.lang.String r2 = r2.toString()
            com.sec.internal.log.IMSLog.i(r0, r1, r2)
            int r0 = r7.getLastErrorCode()
            if (r0 == 0) goto L_0x01a0
            r1 = 200(0xc8, float:2.8E-43)
            if (r0 == r1) goto L_0x0195
            r1 = 401(0x191, float:5.62E-43)
            java.lang.String r2 = "POST"
            if (r0 == r1) goto L_0x0186
            r1 = 403(0x193, float:5.65E-43)
            r3 = 0
            if (r0 == r1) goto L_0x0176
            r1 = 500(0x1f4, float:7.0E-43)
            if (r0 == r1) goto L_0x016c
            r1 = 503(0x1f7, float:7.05E-43)
            if (r0 == r1) goto L_0x014b
            r1 = 511(0x1ff, float:7.16E-43)
            java.lang.String r4 = ""
            if (r0 == r1) goto L_0x0136
            r1 = 301(0x12d, float:4.22E-43)
            r5 = 0
            java.lang.String r6 = "Location"
            if (r0 == r1) goto L_0x00fc
            r8 = 302(0x12e, float:4.23E-43)
            if (r0 == r8) goto L_0x0083
            switch(r0) {
                case 800: goto L_0x0078;
                case 801: goto L_0x0067;
                case 802: goto L_0x005b;
                case 803: goto L_0x005b;
                default: goto L_0x005a;
            }
        L_0x005a:
            goto L_0x005e
        L_0x005b:
            r7.sendConfigCompleteForPSOnlyNw()
        L_0x005e:
            com.sec.internal.ims.config.exception.UnknownStatusException r7 = new com.sec.internal.ims.config.exception.UnknownStatusException
            java.lang.String r8 = "unknown http status code"
            r7.<init>(r8)
            throw r7
        L_0x0067:
            java.lang.String r8 = LOG_TAG
            int r7 = r7.mPhoneId
            java.lang.String r9 = "Connect exception, please retry"
            com.sec.internal.log.IMSLog.i(r8, r7, r9)
            java.net.ConnectException r7 = new java.net.ConnectException
            java.lang.String r8 = "Connection failed"
            r7.<init>(r8)
            throw r7
        L_0x0078:
            java.lang.String r8 = LOG_TAG
            int r7 = r7.mPhoneId
            java.lang.String r9 = "SSL handshake failed"
            com.sec.internal.log.IMSLog.i(r8, r7, r9)
            goto L_0x01a9
        L_0x0083:
            java.lang.String r8 = LOG_TAG
            int r10 = r7.mPhoneId
            java.lang.String r0 = "oidc redirects"
            com.sec.internal.log.IMSLog.i(r8, r10, r0)
            com.sec.internal.ims.config.SharedInfo r8 = r7.mSharedInfo
            com.sec.internal.interfaces.ims.config.IHttpAdapter$Response r8 = r8.getHttpResponse()
            java.util.Map r8 = r8.getHeader()
            java.lang.String r10 = "Authentication-Info"
            java.lang.Object r8 = r8.get(r10)
            if (r8 == 0) goto L_0x00a3
            com.sec.internal.ims.config.SharedInfo r8 = r7.mSharedInfo
            r8.setUserMethod(r2)
        L_0x00a3:
            com.sec.internal.ims.config.SharedInfo r8 = r7.mSharedInfo
            com.sec.internal.interfaces.ims.config.IHttpAdapter$Response r8 = r8.getHttpResponse()
            java.util.Map r8 = r8.getHeader()
            java.lang.Object r8 = r8.get(r6)
            if (r8 == 0) goto L_0x00f9
            com.sec.internal.ims.config.SharedInfo r8 = r7.mSharedInfo
            com.sec.internal.interfaces.ims.config.IHttpAdapter$Response r8 = r8.getHttpResponse()
            java.util.Map r8 = r8.getHeader()
            java.lang.Object r8 = r8.get(r6)
            java.util.List r8 = (java.util.List) r8
            java.lang.Object r8 = r8.get(r5)
            java.lang.String r8 = (java.lang.String) r8
            if (r8 == 0) goto L_0x00f9
            java.lang.String r10 = "https"
            boolean r10 = r8.startsWith(r10)
            if (r10 == 0) goto L_0x00e7
            com.sec.internal.ims.config.SharedInfo r10 = r7.mSharedInfo
            java.lang.String r0 = "\\?"
            java.lang.String[] r8 = r8.split(r0)
            r8 = r8[r5]
            r10.setUrl(r8)
            com.sec.internal.ims.config.workflow.WorkflowCookieHandler r7 = r7.mCookieHandler
            r7.clearCookie()
            goto L_0x019e
        L_0x00e7:
            java.lang.String r8 = LOG_TAG
            int r7 = r7.mPhoneId
            java.lang.String r9 = "https redirect not found"
            com.sec.internal.log.IMSLog.i(r8, r7, r9)
            com.sec.internal.ims.config.exception.InvalidHeaderException r7 = new com.sec.internal.ims.config.exception.InvalidHeaderException
            java.lang.String r8 = "redirect location should be https instead of http"
            r7.<init>(r8)
            throw r7
        L_0x00f9:
            r8 = r3
            goto L_0x01aa
        L_0x00fc:
            java.lang.String r9 = LOG_TAG
            int r10 = r7.mPhoneId
            java.lang.String r0 = "http redirects"
            com.sec.internal.log.IMSLog.i(r9, r10, r0)
            com.sec.internal.ims.config.SharedInfo r9 = r7.mSharedInfo
            com.sec.internal.interfaces.ims.config.IHttpAdapter$Response r9 = r9.getHttpResponse()
            java.util.Map r9 = r9.getHeader()
            java.lang.Object r9 = r9.get(r6)
            if (r9 == 0) goto L_0x012c
            com.sec.internal.ims.config.SharedInfo r9 = r7.mSharedInfo
            com.sec.internal.interfaces.ims.config.IHttpAdapter$Response r9 = r9.getHttpResponse()
            java.util.Map r9 = r9.getHeader()
            java.lang.Object r9 = r9.get(r6)
            java.util.List r9 = (java.util.List) r9
            java.lang.Object r9 = r9.get(r5)
            r4 = r9
            java.lang.String r4 = (java.lang.String) r4
        L_0x012c:
            com.sec.internal.ims.config.SharedInfo r9 = r7.mSharedInfo
            r9.setUrl(r4)
            r9 = 1
            r7.mHttpRedirect = r9
            goto L_0x01aa
        L_0x0136:
            java.lang.String r9 = LOG_TAG
            int r10 = r7.mPhoneId
            java.lang.String r0 = "RCC07_RCS 5.1 Specification 2.3.3.4.2.1 - The token is no longer valid"
            com.sec.internal.log.IMSLog.i(r9, r10, r0)
            com.sec.internal.constants.ims.DiagnosisConstants$RCSA_TDRE r9 = com.sec.internal.constants.ims.DiagnosisConstants.RCSA_TDRE.INVALID_TOKEN
            r7.setToken(r4, r9)
            r7.removeValidToken()
            r7.sendConfigCompleteForPSOnlyNw()
            goto L_0x01aa
        L_0x014b:
            long r0 = r7.getretryAfterTime()
            r7.sendConfigCompleteForPSOnlyNw()
            r2 = 1000(0x3e8, double:4.94E-321)
            long r0 = r0 * r2
            r7.sleep(r0)
            com.sec.internal.constants.Mno r10 = com.sec.internal.helper.SimUtil.getMno()
            boolean r10 = com.sec.internal.ims.util.ConfigUtil.isRcsChn(r10)
            if (r10 == 0) goto L_0x019e
            java.lang.String r9 = LOG_TAG
            int r7 = r7.mPhoneId
            java.lang.String r10 = "chn - next init"
            com.sec.internal.log.IMSLog.i(r9, r7, r10)
            goto L_0x01aa
        L_0x016c:
            java.lang.String r8 = LOG_TAG
            int r7 = r7.mPhoneId
            java.lang.String r9 = "fail. retry next boot"
            com.sec.internal.log.IMSLog.i(r8, r7, r9)
            goto L_0x01a9
        L_0x0176:
            java.lang.String r8 = LOG_TAG
            int r9 = r7.mPhoneId
            java.lang.String r0 = "set version to 0. retry next boot"
            com.sec.internal.log.IMSLog.i(r8, r9, r0)
            com.sec.internal.ims.config.workflow.WorkflowBase$OpMode r8 = com.sec.internal.ims.config.workflow.WorkflowBase.OpMode.DISABLE_TEMPORARY
            r7.setOpMode(r8, r3)
            goto L_0x01a9
        L_0x0186:
            java.lang.String r8 = LOG_TAG
            int r10 = r7.mPhoneId
            java.lang.String r0 = "401"
            com.sec.internal.log.IMSLog.i(r8, r10, r0)
            com.sec.internal.ims.config.SharedInfo r7 = r7.mSharedInfo
            r7.setUserMethod(r2)
            goto L_0x019e
        L_0x0195:
            java.lang.String r8 = LOG_TAG
            int r7 = r7.mPhoneId
            java.lang.String r10 = "normal case"
            com.sec.internal.log.IMSLog.i(r8, r7, r10)
        L_0x019e:
            r8 = r9
            goto L_0x01aa
        L_0x01a0:
            java.lang.String r8 = LOG_TAG
            int r7 = r7.mPhoneId
            java.lang.String r9 = "RCS configuration server is unreachable. retry next boot"
            com.sec.internal.log.IMSLog.i(r8, r7, r9)
        L_0x01a9:
            r8 = r10
        L_0x01aa:
            return r8
        */
        throw new UnsupportedOperationException("Method not decompiled: com.sec.internal.ims.config.workflow.WorkflowBase.handleResponse2(com.sec.internal.ims.config.workflow.WorkflowBase$Workflow, com.sec.internal.ims.config.workflow.WorkflowBase$Workflow, com.sec.internal.ims.config.workflow.WorkflowBase$Workflow):com.sec.internal.ims.config.workflow.WorkflowBase$Workflow");
    }

    /* access modifiers changed from: protected */
    public void sendConfigCompleteForPSOnlyNw() {
        IMnoStrategy rcsStrategy = RcsPolicyManager.getRcsStrategy(this.mPhoneId);
        if (rcsStrategy != null && rcsStrategy.boolSetting(RcsPolicySettings.RcsPolicy.PS_ONLY_NETWORK)) {
            this.mModule.getHandler().removeMessages(3);
            this.mModule.getHandler().sendMessage(obtainMessage(3, 0, 0, Integer.valueOf(this.mPhoneId)));
        }
    }

    /* access modifiers changed from: protected */
    public OpMode getOpMode(Map<String, String> map) {
        OpMode opMode = OpMode.ACTIVE;
        int version = getVersion(map);
        if (opMode.value() <= version) {
            String str = LOG_TAG;
            int i = this.mPhoneId;
            IMSLog.i(str, i, "version :" + version);
            return opMode;
        }
        OpMode[] values = OpMode.values();
        int length = values.length;
        int i2 = 0;
        while (true) {
            if (i2 >= length) {
                break;
            }
            OpMode opMode2 = values[i2];
            if (opMode2.value() == version) {
                opMode = opMode2;
                break;
            }
            i2++;
        }
        String str2 = LOG_TAG;
        int i3 = this.mPhoneId;
        IMSLog.i(str2, i3, "operation mode :" + opMode.name());
        return opMode;
    }

    /* access modifiers changed from: protected */
    public OpMode getOpMode() {
        OpMode opMode = OpMode.ACTIVE;
        int version = getVersion();
        String str = LOG_TAG;
        int i = this.mPhoneId;
        IMSLog.i(str, i, "getOpMode :" + version);
        if (opMode.value() <= version) {
            String str2 = LOG_TAG;
            int i2 = this.mPhoneId;
            IMSLog.i(str2, i2, "OpMode.ACTIVE.value(): " + opMode.value());
            return opMode;
        }
        OpMode[] values = OpMode.values();
        int length = values.length;
        int i3 = 0;
        while (true) {
            if (i3 >= length) {
                break;
            }
            OpMode opMode2 = values[i3];
            if (opMode2.value() == version) {
                opMode = opMode2;
                break;
            }
            i3++;
        }
        String str3 = LOG_TAG;
        int i4 = this.mPhoneId;
        IMSLog.i(str3, i4, "operation mode :" + opMode.name());
        return opMode;
    }

    /* access modifiers changed from: protected */
    public void writeDataToStorage(Map<String, String> map) {
        synchronized (IRcsPolicyManager.class) {
            clearStorage(DiagnosisConstants.RCSA_TDRE.UPDATE_REMOTE_CONFIG);
            this.mStorage.writeAll(map);
        }
    }

    /* access modifiers changed from: protected */
    public void setOpMode(OpMode opMode, Map<String, String> map) {
        String str = LOG_TAG;
        int i = this.mPhoneId;
        IMSLog.i(str, i, "new operation mode :" + opMode.name());
        IMSLog.c(LogClass.WFB_OP_MODE_NAME, this.mPhoneId + ",NOP:" + opMode.name());
        addEventLog(LOG_TAG + ": new operation mode :" + opMode.name());
        int i2 = AnonymousClass3.$SwitchMap$com$sec$internal$ims$config$workflow$WorkflowBase$OpMode[opMode.ordinal()];
        if (i2 != 1) {
            if (i2 != 2) {
                if (i2 == 3) {
                    if (getVersion() != OpMode.DORMANT.value()) {
                        setVersionBackup(getVersion());
                    }
                    setVersion(opMode.value());
                    return;
                } else if (!(i2 == 4 || i2 == 5)) {
                    return;
                }
            }
            clearStorage(DiagnosisConstants.RCSA_TDRE.DISABLE_RCS);
            setVersion(opMode.value());
            setValidity(opMode.value());
            return;
        }
        if (map != null) {
            String str2 = LOG_TAG;
            int i3 = this.mPhoneId;
            IMSLog.s(str2, i3, "data :" + map);
            Mno simMno = SimUtil.getSimMno(this.mPhoneId);
            if (this.mStartForce || simMno.isKor() || simMno == Mno.USCC || ConfigUtil.isRcsChn(simMno) || getVersion() < getVersion(map) || (getVersion() != getVersion(map) && simMno == Mno.RJIL)) {
                writeDataToStorage(map);
            } else {
                IMSLog.i(LOG_TAG, this.mPhoneId, "the same or lower version and not RJIL. remain to previous data");
                String token = getToken();
                String token2 = getToken(map);
                setValidity(getValidity(map));
                if (token2 != null && (token == null || !token2.equals(token))) {
                    String str3 = LOG_TAG;
                    int i4 = this.mPhoneId;
                    IMSLog.i(str3, i4, "token is changed. setToken : " + token + " -> " + token2);
                    setToken(token2, DiagnosisConstants.RCSA_TDRE.UPDATE_TOKEN);
                }
            }
        } else {
            IMSLog.i(LOG_TAG, this.mPhoneId, "null data. remain previous mode & data");
        }
        setNextAutoconfigTimeAfter(getValidity());
    }

    /* access modifiers changed from: protected */
    public boolean initStorage() {
        IMSLog.i(LOG_TAG, this.mPhoneId, "initStorage()");
        if (this.mStorage.getState() != 1) {
            if (!this.mTelephonyAdapter.isReady()) {
                this.mEventLog.logAndAdd(this.mPhoneId, "initStorage: Telephony readiness check start.");
            }
            int i = 60;
            while (!this.mTelephonyAdapter.isReady() && i > 0) {
                sleep(1000);
                i--;
            }
            this.mEventLog.logAndAdd(this.mPhoneId, "initStorage: Telephony readiness check done. Now check identity.");
            this.mIdentity = "";
            while (true) {
                if (i <= 0) {
                    break;
                }
                String identityByPhoneId = this.mTelephonyAdapter.getIdentityByPhoneId(this.mPhoneId);
                this.mIdentity = identityByPhoneId;
                if (identityByPhoneId != null && !identityByPhoneId.isEmpty()) {
                    IMSLog.i(LOG_TAG, this.mPhoneId, "initStorage. getIdentityByPhoneId is valid");
                    break;
                }
                sleep(1000);
                i--;
            }
            if (i <= 0) {
                IMSLog.i(LOG_TAG, this.mPhoneId, "initStorage: failed");
                IMSLog.c(LogClass.WFB_STORAGE_INIT_FAIL, this.mPhoneId + ",STOR_IF");
                addEventLog(LOG_TAG + ": initStorage: failed");
                return false;
            }
            String generateMD5 = HashManager.generateMD5(this.mIdentity);
            this.mEventLog.logAndAdd(this.mPhoneId, "Open storage: " + IMSLog.checker(this.mIdentity));
            this.mStorage.open(this.mContext, ConfigProvider.CONFIG_DB_NAME_PREFIX + generateMD5, this.mPhoneId);
        }
        checkStorage();
        Mno simMno = SimUtil.getSimMno(this.mPhoneId);
        String string = ImsRegistry.getString(this.mPhoneId, GlobalSettingsConstants.RCS.APPLICATION_SERVER, "");
        if (simMno == Mno.TCE || simMno == Mno.VZW || simMno == Mno.SPRINT || ImsConstants.RCS_AS.JIBE.equals(string) || ImsConstants.RCS_AS.SEC.equals(string)) {
            this.mParamHandler.checkSetToGS((Map<String, String>) null);
            this.mIsUsingcheckSetToGS = true;
        }
        return true;
    }

    /* access modifiers changed from: protected */
    public void resetStorage() {
        IMSLog.c(LogClass.WFB_RESET_CONFIG, this.mPhoneId + ",reset ACS Config");
        this.mStorage.close();
        initStorage();
    }

    /* access modifiers changed from: protected */
    public void clearStorage() {
        IMSLog.c(LogClass.WFB_RESET_CONFIG, this.mPhoneId + ",clearStorage");
        addEventLog(LOG_TAG + ": clearStorage");
        this.mStorage.deleteAll();
        removeValidToken();
        checkStorage();
    }

    /* access modifiers changed from: protected */
    public void clearStorage(DiagnosisConstants.RCSA_TDRE rcsa_tdre) {
        IMSLog.c(LogClass.WFB_RESET_CONFIG, this.mPhoneId + ",clearStorage. reason: " + rcsa_tdre.name());
        addEventLog(LOG_TAG + ": clearStorage. reason: " + rcsa_tdre.name());
        this.mModule.setTokenDeletedReason(this.mPhoneId, rcsa_tdre);
        this.mStorage.deleteAll();
        removeValidToken();
        checkStorage();
    }

    /* access modifiers changed from: protected */
    public void checkStorage() {
        ArrayList arrayList = new ArrayList();
        for (Map.Entry next : ConfigContract.STORAGE_DEFAULT.entrySet()) {
            if (this.mStorage.read((String) next.getKey()) == null) {
                arrayList.add((String) next.getKey());
                this.mStorage.write((String) next.getKey(), (String) next.getValue());
            }
        }
        int size = arrayList.size();
        if (size > 0) {
            String str = LOG_TAG;
            int i = this.mPhoneId;
            IMSLog.i(str, i, "checkStorage: Default set(" + size + "): " + arrayList);
        }
    }

    public void closeStorage() {
        this.mState.closeStorage();
    }

    /* access modifiers changed from: protected */
    public void setCompleted(boolean z) {
        ContentValues contentValues = new ContentValues();
        contentValues.put(ConfigConstants.PATH.INFO_COMPLETED, String.valueOf(z));
        ContentResolver contentResolver = this.mContext.getContentResolver();
        Uri.Builder buildUpon = ConfigConstants.CONTENT_URI.buildUpon();
        contentResolver.insert(buildUpon.fragment("simslot" + this.mPhoneId).build(), contentValues);
    }

    /* access modifiers changed from: protected */
    public int getVersion(Map<String, String> map) {
        try {
            return Integer.parseInt(map.get("root/vers/version"));
        } catch (NullPointerException | NumberFormatException e) {
            e.printStackTrace();
            return getVersion();
        }
    }

    /* access modifiers changed from: protected */
    public int getVersion() {
        try {
            return Integer.parseInt(this.mStorage.read("root/vers/version"));
        } catch (NullPointerException | NumberFormatException e) {
            e.printStackTrace();
            return 0;
        }
    }

    /* access modifiers changed from: protected */
    public void setVersion(int i) {
        IMSLog.c(LogClass.WFB_SET_VERSION, this.mPhoneId + ",VER:" + i);
        this.mStorage.write("root/vers/version", String.valueOf(i));
    }

    /* access modifiers changed from: protected */
    public int getVersionFromServer() {
        try {
            return Integer.parseInt(this.mStorage.read(ConfigConstants.PATH.VERS_VERSION_FROM_SERVER));
        } catch (NullPointerException | NumberFormatException e) {
            String str = LOG_TAG;
            int i = this.mPhoneId;
            IMSLog.i(str, i, "getVersionFromServer: " + e.getMessage());
            return 0;
        }
    }

    /* access modifiers changed from: protected */
    public void setVersionFromServer(int i) {
        IMSLog.c(LogClass.WFB_VER_FROM_SERVER, this.mPhoneId + ",VERFS:" + i);
        this.mStorage.write(ConfigConstants.PATH.VERS_VERSION_FROM_SERVER, String.valueOf(i));
    }

    /* access modifiers changed from: protected */
    public String getVersionBackup() {
        String read = this.mStorage.read(ConfigConstants.PATH.VERS_VERSION_BACKUP);
        return TextUtils.isEmpty(read) ? "0" : read;
    }

    /* access modifiers changed from: protected */
    public int getParsedIntVersionBackup() {
        try {
            return Integer.parseInt(getVersionBackup());
        } catch (NullPointerException | NumberFormatException e) {
            String str = LOG_TAG;
            int i = this.mPhoneId;
            IMSLog.i(str, i, "getParsedIntVersionBackup: cannot get backupVersion: " + e.getMessage());
            return 0;
        }
    }

    /* access modifiers changed from: protected */
    public void setVersionBackup(int i) {
        this.mStorage.write(ConfigConstants.PATH.VERS_VERSION_BACKUP, String.valueOf(i));
    }

    /* access modifiers changed from: protected */
    public int getValidity(Map<String, String> map) {
        int i;
        try {
            i = Integer.parseInt(map.get("root/vers/validity"));
        } catch (NullPointerException | NumberFormatException e) {
            e.printStackTrace();
            i = 0;
        }
        String str = LOG_TAG;
        int i2 = this.mPhoneId;
        IMSLog.i(str, i2, "getValidity from config.xml :" + i);
        return i;
    }

    /* access modifiers changed from: protected */
    public int getValidity() {
        int i;
        try {
            i = Integer.parseInt(this.mStorage.read("root/vers/validity"));
        } catch (NullPointerException | NumberFormatException e) {
            e.printStackTrace();
            i = 0;
        }
        String str = LOG_TAG;
        int i2 = this.mPhoneId;
        IMSLog.i(str, i2, "getValidity from config DB :" + i);
        return i;
    }

    /* access modifiers changed from: protected */
    public void setValidity(int i) {
        IMSLog.c(LogClass.WFB_VALIDITY, this.mPhoneId + ",VAL:" + i);
        this.mStorage.write("root/vers/validity", String.valueOf(i));
    }

    /* access modifiers changed from: protected */
    public int removeToken(DiagnosisConstants.RCSA_TDRE rcsa_tdre) {
        this.mModule.setTokenDeletedReason(this.mPhoneId, rcsa_tdre);
        return this.mStorage.delete("root/token/token");
    }

    /* access modifiers changed from: protected */
    public String getToken(Map<String, String> map) {
        return map.get("root/token/token");
    }

    /* access modifiers changed from: protected */
    public String getToken() {
        return this.mStorage.read("root/token/token");
    }

    /* access modifiers changed from: protected */
    public void setToken(String str, DiagnosisConstants.RCSA_TDRE rcsa_tdre) {
        if ("".equals(str)) {
            IMSLog.c(LogClass.WFB_RESET_TOKEN, this.mPhoneId + ",reset ACS token. reason: " + rcsa_tdre.name());
            addEventLog(LOG_TAG + ": reset ACS token. reason: " + rcsa_tdre.name());
        }
        this.mStorage.write("root/token/token", str);
        this.mModule.setTokenDeletedReason(this.mPhoneId, rcsa_tdre);
    }

    /* access modifiers changed from: protected */
    public void sleep(long j) {
        this.mPowerController.sleep(j);
    }

    /* access modifiers changed from: protected */
    public void setValidityTimer(int i) {
        if (this.mValidityIntent != null) {
            IMSLog.i(LOG_TAG, this.mPhoneId, "setValidityTimer: validityTimer is already running. Stopping it.");
            cancelValidityTimer();
        }
        String str = LOG_TAG;
        int i2 = this.mPhoneId;
        IMSLog.i(str, i2, "setValidityTimer: start validity period timer (" + i + " sec)");
        if (i == 0) {
            sendEmptyMessage(1);
        } else if (i > 0) {
            Intent intent = new Intent(INTENT_VALIDITY_TIMEOUT);
            intent.putExtra(PhoneConstants.PHONE_KEY, this.mPhoneId);
            intent.setPackage(this.mContext.getPackageName());
            PendingIntent broadcast = PendingIntent.getBroadcast(this.mContext, 0, intent, 33554432);
            this.mValidityIntent = broadcast;
            AlarmTimer.start(this.mContext, broadcast, ((long) i) * 1000);
        }
    }

    /* access modifiers changed from: protected */
    public void cancelValidityTimer() {
        if (this.mValidityIntent == null) {
            IMSLog.i(LOG_TAG, this.mPhoneId, "cancelValidityTimer: validityTimer is not running.");
            return;
        }
        IMSLog.i(LOG_TAG, this.mPhoneId, "cancelValidityTimer:");
        AlarmTimer.stop(this.mContext, this.mValidityIntent);
        this.mValidityIntent = null;
    }

    /* access modifiers changed from: protected */
    public long getNextAutoconfigTime() {
        long j;
        String read = this.mStorage.read(ConfigConstants.PATH.NEXT_AUTOCONFIG_TIME);
        if (!TextUtils.isEmpty(read)) {
            try {
                j = Long.parseLong(read);
            } catch (NumberFormatException e) {
                e.printStackTrace();
            }
            String str = LOG_TAG;
            int i = this.mPhoneId;
            IMSLog.i(str, i, "getNextAutoconfigTime = " + j);
            return j;
        }
        j = 0;
        String str2 = LOG_TAG;
        int i2 = this.mPhoneId;
        IMSLog.i(str2, i2, "getNextAutoconfigTime = " + j);
        return j;
    }

    /* access modifiers changed from: protected */
    public void setNextAutoconfigTime(long j) {
        this.mStorage.write(ConfigConstants.PATH.NEXT_AUTOCONFIG_TIME, String.valueOf(j));
    }

    /* access modifiers changed from: protected */
    public void setNextAutoconfigTimeAfter(int i) {
        String str = LOG_TAG;
        int i2 = this.mPhoneId;
        IMSLog.i(str, i2, "setNextAutoconfigTimeAfter:" + i);
        if (i > 0) {
            setNextAutoconfigTime(new Date().getTime() + (((long) i) * 1000));
        }
    }

    /* access modifiers changed from: protected */
    public void setTcUserAccept(int i) {
        String str = LOG_TAG;
        int i2 = this.mPhoneId;
        IMSLog.i(str, i2, "setTcUserAccept:" + i);
        this.mStorage.write(ConfigConstants.PATH.TC_POPUP_USER_ACCEPT, String.valueOf(i));
    }

    /* access modifiers changed from: protected */
    public String getLastSwVersion() {
        String string = this.mContext.getSharedPreferences(PREFERENCE_NAME, 0).getString(LAST_SW_VERSION, "");
        String str = LOG_TAG;
        int i = this.mPhoneId;
        IMSLog.i(str, i, "getLastSwVersion:" + string);
        return string;
    }

    /* access modifiers changed from: protected */
    public void setLastSwVersion(String str) {
        SharedPreferences sharedPreferences = this.mContext.getSharedPreferences(PREFERENCE_NAME, 0);
        String str2 = LOG_TAG;
        int i = this.mPhoneId;
        IMSLog.i(str2, i, "setLastSwVersion:" + str);
        SharedPreferences.Editor edit = sharedPreferences.edit();
        edit.putString(LAST_SW_VERSION, str);
        edit.apply();
    }

    /* access modifiers changed from: protected */
    public String getLastRcsProfile() {
        Context context = this.mContext;
        String string = context.getSharedPreferences("rcsprofile_" + this.mPhoneId, 0).getString(LAST_RCS_PROFILE, "");
        String str = LOG_TAG;
        int i = this.mPhoneId;
        IMSLog.i(str, i, "getLastRcsProfile:" + string);
        return string;
    }

    /* access modifiers changed from: protected */
    public void setLastRcsProfile(String str) {
        Context context = this.mContext;
        SharedPreferences sharedPreferences = context.getSharedPreferences("rcsprofile_" + this.mPhoneId, 0);
        String str2 = LOG_TAG;
        int i = this.mPhoneId;
        IMSLog.i(str2, i, "setLastRcsProfile:" + str);
        SharedPreferences.Editor edit = sharedPreferences.edit();
        edit.putString(LAST_RCS_PROFILE, str);
        edit.apply();
    }

    private void registerGlobalSettingsObserver() {
        this.mContext.getContentResolver().registerContentObserver(GlobalSettingsConstants.CONTENT_URI, false, this.mRcsCustomServerUrlObserver);
        this.isGlobalsettingsObserverRegisted = true;
        this.mRcsCustomServerUrl = ImsRegistry.getString(this.mPhoneId, GlobalSettingsConstants.RCS.CUSTOM_CONFIG_SERVER_URL, "");
        this.mRcsAutoconfigSource = ConfigUtil.getAutoconfigSourceWithFeature(this.mPhoneId, 0);
        this.mRcsUPProfile = ImsRegistry.getString(this.mPhoneId, GlobalSettingsConstants.RCS.UP_PROFILE, "");
        String str = "mRcsCustomConfigServerUrl= " + this.mRcsCustomServerUrl + ", mRcsAutoconfigSource=" + this.mRcsAutoconfigSource + ", mRcsUPProfile=" + this.mRcsUPProfile;
        IMSLog.i(LOG_TAG, this.mPhoneId, "registerGlobalSettingsObserver: " + str);
        addEventLog(LOG_TAG + ": registerGlobalSettingsObserver : " + str);
    }

    private void unregisterGlobalSettingsObserver() {
        IMSLog.i(LOG_TAG, this.mPhoneId, "unregisterGlobalSettingsObserver");
        if (this.isGlobalsettingsObserverRegisted) {
            this.mContext.getContentResolver().unregisterContentObserver(this.mRcsCustomServerUrlObserver);
            this.isGlobalsettingsObserverRegisted = false;
        }
    }

    /* access modifiers changed from: protected */
    public boolean needScheduleAutoconfig(int i) {
        if (OmcCode.isTmpSimSwap(i) && !ImsUtil.isSimMobilityActivatedForRcs(i) && !ImsUtil.isSimMobilityActivatedForAmRcs(this.mContext, i)) {
            Mno simMno = SimUtil.getSimMno(this.mPhoneId);
            if (this.mParamHandler.isSupportCarrierVersion() && !simMno.isVodafone() && !simMno.isOrange() && !simMno.isTmobile()) {
                IMSLog.i(LOG_TAG, this.mPhoneId, "needScheduleAutoconfig: Temporal SIM swapped, skip autoconfiguration");
                setVersion(OpMode.DISABLE_TEMPORARY.value());
                return false;
            }
        }
        return true;
    }

    /* access modifiers changed from: protected */
    public void handleSwVersionChange(String str) {
        IMSLog.i(LOG_TAG, this.mPhoneId, "handleSwVersionChange");
        Mno simMno = SimUtil.getSimMno(this.mPhoneId);
        String str2 = ConfigConstants.BUILD.TERMINAL_SW_VERSION;
        if (str.equals(str2)) {
            return;
        }
        if (simMno == Mno.BELL || simMno.isKor()) {
            IMSLog.i(LOG_TAG, this.mPhoneId, "there is fota upgrade found");
            setNextAutoconfigTime(new Date().getTime());
            setLastSwVersion(str2);
            this.mModule.setAcsTryReason(this.mPhoneId, DiagnosisConstants.RCSA_ATRE.CHANGE_SWVERSION);
        }
    }

    /* access modifiers changed from: protected */
    public void handleRcsProfileChange(String str) {
        IMSLog.i(LOG_TAG, this.mPhoneId, "handleRcsProfileChange");
        String rcsProfileType = ImsRegistry.getRcsProfileType(this.mPhoneId);
        Mno simMno = SimUtil.getSimMno(this.mPhoneId);
        String string = ImsRegistry.getString(this.mPhoneId, GlobalSettingsConstants.RCS.APPLICATION_SERVER, "");
        String str2 = LOG_TAG;
        int i = this.mPhoneId;
        IMSLog.i(str2, i, "handleRcsProfileChange: now: " + rcsProfileType + " last: " + str);
        if (str.equals(rcsProfileType)) {
            return;
        }
        if ((simMno.isVodafone() && ("UP_1.0".equals(rcsProfileType) || "UP_2.0".equals(rcsProfileType))) || ImsConstants.RCS_AS.JIBE.equals(string) || ImsConstants.RCS_AS.SEC.equals(string) || ConfigUtil.isRcsChn(simMno)) {
            IMSLog.i(LOG_TAG, this.mPhoneId, "There is RCS profile update found");
            setVersion(OpMode.DISABLE_TEMPORARY.value());
            setNextAutoconfigTime(new Date().getTime());
            setLastRcsProfile(rcsProfileType);
        }
    }

    /* access modifiers changed from: protected */
    public long getretryAfterTime() throws InvalidHeaderException {
        long j;
        try {
            String str = (String) this.mSharedInfo.getHttpResponse().getHeader().get(HttpRequest.HEADER_RETRY_AFTER).get(0);
            if (str.matches("[0-9]+")) {
                j = (long) Integer.parseInt(str);
            } else {
                SimpleDateFormat simpleDateFormat = new SimpleDateFormat(TIMESTAMP_FORMAT, Locale.ENGLISH);
                Calendar instance = Calendar.getInstance();
                instance.setTime(simpleDateFormat.parse(str));
                long timeInMillis = instance.getTimeInMillis();
                instance.setTime(simpleDateFormat.parse((String) this.mSharedInfo.getHttpResponse().getHeader().get("Date").get(0)));
                j = timeInMillis - instance.getTimeInMillis();
            }
            if (j <= 0) {
                j = 10;
            }
            String str2 = LOG_TAG;
            int i = this.mPhoneId;
            IMSLog.i(str2, i, "retry after " + j + " sec");
            addEventLog(LOG_TAG + ": retry after " + j + " sec");
            return j;
        } catch (IndexOutOfBoundsException | ParseException e) {
            String str3 = LOG_TAG;
            int i2 = this.mPhoneId;
            IMSLog.i(str3, i2, "Exception: retry after related header " + e.getMessage());
            StringBuilder sb = new StringBuilder();
            sb.append("retry after related header ");
            sb.append(e instanceof IndexOutOfBoundsException ? " do not exist" : " is invalid");
            throw new InvalidHeaderException(sb.toString());
        }
    }

    public void addEventLog(String str) {
        this.mEventLog.add(this.mPhoneId, str);
    }

    public void dump() {
        IMSLog.dump(LOG_TAG, "Dump of Workflow:");
        IMSLog.increaseIndent(LOG_TAG);
        this.mEventLog.dump();
        IMSLog.decreaseIndent(LOG_TAG);
    }

    /* access modifiers changed from: protected */
    public void createSharedInfo() {
        this.mSharedInfo = new SharedInfo(this.mContext, this.mSm, this.mRcsProfile, this.mRcsVersion, this.mClientPlatform, this.mClientVersion, this.mRcsEnabledByUser);
    }
}
