package com.sec.internal.ims.config.workflow;

import android.content.Intent;
import android.database.sqlite.SQLiteFullException;
import android.os.CountDownTimer;
import android.os.Message;
import android.text.TextUtils;
import android.util.Log;
import com.sec.ims.extensions.ContextExt;
import com.sec.ims.settings.ImsProfile;
import com.sec.internal.constants.Mno;
import com.sec.internal.constants.ims.DiagnosisConstants;
import com.sec.internal.constants.ims.cmstore.CloudMessageProviderContract;
import com.sec.internal.constants.ims.config.ConfigConstants;
import com.sec.internal.constants.ims.settings.GlobalSettingsConstants;
import com.sec.internal.helper.DmConfigHelper;
import com.sec.internal.helper.ImsCallUtil;
import com.sec.internal.helper.SimUtil;
import com.sec.internal.helper.httpclient.HttpController;
import com.sec.internal.ims.config.ConfigContract;
import com.sec.internal.ims.config.SharedInfo;
import com.sec.internal.ims.config.exception.EmptyBodyAndCookieException;
import com.sec.internal.ims.config.exception.InvalidHeaderException;
import com.sec.internal.ims.config.exception.InvalidXmlException;
import com.sec.internal.ims.config.exception.NoInitialDataException;
import com.sec.internal.ims.config.exception.UnknownStatusException;
import com.sec.internal.ims.config.workflow.WorkflowBase;
import com.sec.internal.ims.config.workflow.WorkflowParamHandler;
import com.sec.internal.ims.rcs.util.RcsUtils;
import com.sec.internal.ims.registry.ImsRegistry;
import com.sec.internal.ims.servicemodules.ss.UtStateMachine;
import com.sec.internal.ims.settings.DeviceConfigManager;
import com.sec.internal.ims.util.ConfigUtil;
import com.sec.internal.interfaces.ims.config.IConfigModule;
import com.sec.internal.interfaces.ims.config.IHttpAdapter;
import com.sec.internal.log.IMSLog;
import java.util.Locale;
import java.util.Map;

public class WorkflowUp extends WorkflowUpBase {
    static final String CHAT_AUTH_FULL_PATH = "root/application/1/services/ChatAuth";
    protected static final int INTERNAL403ERR_RETRY_MAX_COUNT = 60;
    protected static final int INTERNAL404ERR_RETRY_MAX_COUNT = 30;
    /* access modifiers changed from: private */
    public static final String LOG_TAG = WorkflowUp.class.getSimpleName();
    protected static final int UNKNOWNERR_RETRY_MAX_COUNT = 70;
    protected static final int[] mExponentialInternalErrorRetry = {5, 60, 300, 600, 1800, 7200, 21600, 43200, 86400};
    protected int mAuthHiddenTryCount = 0;
    protected int mAuthTryCount = 0;
    protected boolean mIsHeaderEnrichment = false;
    protected boolean mIsXmlReceived = false;
    protected CountDownTimer mMsisdnTimer = null;
    protected String mSmsPort = null;

    /* access modifiers changed from: protected */
    public WorkflowBase.Workflow getNextWorkflow(int i) {
        return null;
    }

    /* JADX WARNING: Illegal instructions before constructor call */
    /* Code decompiled incorrectly, please refer to instructions dump. */
    public WorkflowUp(android.os.Looper r13, android.content.Context r14, com.sec.internal.interfaces.ims.config.IConfigModule r15, com.sec.internal.constants.Mno r16, int r17) {
        /*
            r12 = this;
            r11 = r12
            r2 = r14
            r3 = r15
            r10 = r17
            com.sec.internal.ims.config.adapters.TelephonyAdapterPrimaryDeviceUp r5 = new com.sec.internal.ims.config.adapters.TelephonyAdapterPrimaryDeviceUp
            r5.<init>(r14, r15, r10)
            com.sec.internal.ims.config.adapters.StorageAdapter r6 = new com.sec.internal.ims.config.adapters.StorageAdapter
            r6.<init>()
            com.sec.internal.ims.config.adapters.HttpAdapterUp r7 = new com.sec.internal.ims.config.adapters.HttpAdapterUp
            r7.<init>(r10)
            com.sec.internal.ims.config.adapters.XmlParserAdapter r8 = new com.sec.internal.ims.config.adapters.XmlParserAdapter
            r8.<init>()
            com.sec.internal.ims.config.adapters.DialogAdapter r9 = new com.sec.internal.ims.config.adapters.DialogAdapter
            r9.<init>(r14, r15, r10)
            r0 = r12
            r1 = r13
            r4 = r16
            r0.<init>(r1, r2, r3, r4, r5, r6, r7, r8, r9, r10)
            r0 = 0
            r11.mMsisdnTimer = r0
            r1 = 0
            r11.mIsXmlReceived = r1
            r11.mSmsPort = r0
            r11.mAuthTryCount = r1
            r11.mAuthHiddenTryCount = r1
            r11.mIsHeaderEnrichment = r1
            com.sec.internal.ims.config.SharedInfo r0 = r11.mSharedInfo
            r0.setInternal403ErrRetryCount(r1)
            com.sec.internal.ims.config.SharedInfo r0 = r11.mSharedInfo
            r0.setInternal404ErrRetryCount(r1)
            com.sec.internal.ims.config.SharedInfo r0 = r11.mSharedInfo
            r0.setUnknownErrRetryCount(r1)
            return
        */
        throw new UnsupportedOperationException("Method not decompiled: com.sec.internal.ims.config.workflow.WorkflowUp.<init>(android.os.Looper, android.content.Context, com.sec.internal.interfaces.ims.config.IConfigModule, com.sec.internal.constants.Mno, int):void");
    }

    public void handleMessage(Message message) {
        String str = LOG_TAG;
        int i = this.mPhoneId;
        IMSLog.i(str, i, "handleMessage: " + message.what);
        addEventLog(str + "handleMessage: " + message.what);
        int i2 = message.what;
        if (i2 == 0) {
            this.mStartForce = true;
        } else if (i2 != 1) {
            if (i2 != 5) {
                if (i2 != 7) {
                    super.handleMessage(message);
                    return;
                }
                Log.i(str, "show MSISDN dialog,");
                sendEmptyMessage(1);
                return;
            } else if (DmConfigHelper.getImsSwitchValue(this.mContext, DeviceConfigManager.DEFAULTMSGAPPINUSE, this.mPhoneId) == 1) {
                Log.i(str, "sms default application is changed to samsung");
                if (this.mMno == Mno.PLUS_POLAND) {
                    setOpMode(WorkflowBase.OpMode.ENABLE_RCS_BY_USER, (Map<String, String>) null);
                    return;
                }
                WorkflowBase.OpMode opMode = WorkflowBase.OpMode.DISABLE_TEMPORARY;
                setVersion(opMode.value());
                setRcsState(String.valueOf(opMode.value()));
                setRcsDisabledState("");
                setValidity(opMode.value());
                cancelValidityTimer();
                setNextAutoconfigTime((long) opMode.value());
                return;
            } else {
                Log.i(str, "sms default application is changed to non-samsung");
                setOpMode(WorkflowBase.OpMode.DISABLE_RCS_BY_USER, (Map<String, String>) null);
                sendEmptyMessage(1);
                return;
            }
        }
        if (this.mIsConfigOngoing) {
            Log.i(str, "AutoConfig: ongoing");
            return;
        }
        this.mIsConfigOngoing = true;
        IMSLog.i(str, this.mPhoneId, "AutoConfig: start");
        this.mIsXmlReceived = false;
        this.mMsisdnHandler.setMsisdnTimer(this.mMsisdnTimer);
        this.mModule.getHandler().removeMessages(3, Integer.valueOf(this.mPhoneId));
        this.mSmsPort = null;
        this.mAuthTryCount = 0;
        this.mAuthHiddenTryCount = 0;
        this.mSharedInfo.setInternalErrRetryCount(0);
        this.mPowerController.lock();
        this.mOldVersion = getVersion();
        if (needScheduleAutoconfig(this.mPhoneId)) {
            scheduleAutoconfig(this.mOldVersion);
        }
        this.mNewVersion = getVersion();
        int i3 = this.mPhoneId;
        IMSLog.i(str, i3, "oldVersion: " + this.mOldVersion + " newVersion: " + this.mNewVersion);
        IMSLog.i(str, this.mPhoneId, "AutoConfig: finish");
        setCompleted(true);
        Log.i(str, "mIsReceicedXml: " + this.mIsXmlReceived);
        if (this.mIsXmlReceived) {
            this.mMsisdnHandler.cancelMsisdnTimer(this.mMsisdnTimer, true);
            this.mIsXmlReceived = false;
        }
        this.mModule.getHandler().sendMessage(obtainMessage(3, this.mOldVersion, this.mNewVersion, Integer.valueOf(this.mPhoneId)));
        this.mStartForce = false;
        this.mPowerController.release();
        this.mIsConfigOngoing = false;
    }

    /* access modifiers changed from: package-private */
    public void work() {
        WorkflowBase.Workflow workflow;
        WorkflowBase.Workflow initialize = new Initialize();
        int i = WorkflowBase.AUTO_CONFIG_MAX_FLOWCOUNT;
        while (initialize != null && i > 0) {
            try {
                initialize = initialize.run();
            } catch (NoInitialDataException e) {
                Log.i(LOG_TAG, "NoInitialDataException: " + e.getMessage());
                Log.i(LOG_TAG, "wait 10 sec and retry");
                sleep(10000);
                workflow = new Initialize();
                e.printStackTrace();
                initialize = workflow;
                i--;
            } catch (UnknownStatusException e2) {
                String message = e2.getMessage();
                Log.i(LOG_TAG, "UnknownStatusException: " + message);
                if (e2 instanceof EmptyBodyAndCookieException) {
                    workflow = new Finish();
                    e2.printStackTrace();
                } else {
                    Log.i(LOG_TAG, "wait 2 sec and retry");
                    sleep(UtStateMachine.HTTP_READ_TIMEOUT_GCF);
                    workflow = new Initialize();
                    e2.printStackTrace();
                }
                initialize = workflow;
                i--;
            } catch (SQLiteFullException e3) {
                Log.i(LOG_TAG, "SQLiteFullException occur:" + e3.getMessage());
                Log.i(LOG_TAG, "finish workflow");
                workflow = new Finish();
                e3.printStackTrace();
                initialize = workflow;
                i--;
            } catch (Exception e4) {
                if (e4.getMessage() != null) {
                    Log.i(LOG_TAG, "unknown exception occur:" + e4.getMessage());
                }
                Log.i(LOG_TAG, "wait 1 sec and retry");
                sleep(1000);
                workflow = new Initialize();
                e4.printStackTrace();
                initialize = workflow;
                i--;
            }
            i--;
        }
    }

    /* access modifiers changed from: protected */
    public WorkflowBase.Workflow handleResponseForUpOther(WorkflowBase.Workflow workflow, WorkflowBase.Workflow workflow2, WorkflowBase.Workflow workflow3) throws InvalidHeaderException {
        int i;
        String str = LOG_TAG;
        int i2 = this.mPhoneId;
        IMSLog.i(str, i2, "handleResponseForUpOther: mLastErrorCode: " + getLastErrorCode());
        int lastErrorCode = getLastErrorCode();
        if (lastErrorCode == 200) {
            Log.i(str, "normal case");
            return workflow2;
        } else if (lastErrorCode != 503) {
            if (lastErrorCode != 511) {
                if (lastErrorCode == 403) {
                    int internal403ErrRetryCount = this.mSharedInfo.getInternal403ErrRetryCount();
                    Log.i(str, "set version to zero & retry after 24 hours, retryCount: " + internal403ErrRetryCount);
                    setOpMode(WorkflowBase.OpMode.DISABLE_TEMPORARY, (Map<String, String>) null);
                    if (internal403ErrRetryCount < 60) {
                        this.mSharedInfo.setInternal403ErrRetryCount(internal403ErrRetryCount + 1);
                        setValidityTimer(86400);
                        setNextAutoconfigTimeAfter(86400);
                    }
                } else if (lastErrorCode == 404) {
                    int internal404ErrRetryCount = this.mSharedInfo.getInternal404ErrRetryCount();
                    Log.i(str, "retry after 24 hours, retryCount: " + internal404ErrRetryCount);
                    if (internal404ErrRetryCount < 30) {
                        this.mSharedInfo.setInternal404ErrRetryCount(internal404ErrRetryCount + 1);
                        setValidityTimer(86400);
                        setNextAutoconfigTimeAfter(86400);
                    }
                }
                return workflow3;
            }
            Log.i(str, "The token is no longer valid");
            setToken("", DiagnosisConstants.RCSA_TDRE.INVALID_TOKEN);
            removeValidToken();
            int unknownErrRetryCount = this.mSharedInfo.getUnknownErrRetryCount();
            Log.i(str, "fail. run retry mechanism. retryCount: " + unknownErrRetryCount);
            if (unknownErrRetryCount < 70) {
                int[] iArr = mExponentialInternalErrorRetry;
                if (unknownErrRetryCount < iArr.length) {
                    i = iArr[unknownErrRetryCount];
                } else {
                    i = iArr[iArr.length - 1];
                }
                this.mSharedInfo.setUnknownErrRetryCount(unknownErrRetryCount + 1);
                setValidityTimer(i);
                setNextAutoconfigTimeAfter(i);
            }
            return workflow3;
        } else {
            sleep(getretryAfterTime() * 1000);
            return workflow2;
        }
    }

    /* access modifiers changed from: protected */
    public boolean isDataFullUpdateNeeded(Map<String, String> map) {
        String str = LOG_TAG;
        IMSLog.i(str, "startForce = " + this.mStartForce + ", isRcsByUser = " + this.mSharedInfo.isRcsByUser() + ", rcsState = " + getRcsState());
        int version = getVersion(map);
        return (getVersion() < version || ((this.mStartForce && !this.mSharedInfo.isRcsByUser()) || (this.mMno.isOneOf(Mno.SWISSCOM, Mno.MTS_RUSSIA) && version > 0))) && !String.valueOf(WorkflowBase.OpMode.DISABLE_RCS_BY_USER.value()).equals(getRcsState()) && (!this.mMno.isOneOf(Mno.SWISSCOM, Mno.MTS_RUSSIA) || !TextUtils.isEmpty(map.get(CHAT_AUTH_FULL_PATH.toLowerCase(Locale.US))));
    }

    /* access modifiers changed from: protected */
    public void checkAndUpdateData(Map<String, String> map) {
        IMSLog.i(LOG_TAG, "Update of client configuration control parameters");
        setValidity(getValidity(map));
        String token = getToken(map);
        if (!TextUtils.isEmpty(token)) {
            setToken(token, DiagnosisConstants.RCSA_TDRE.UPDATE_TOKEN);
        }
    }

    /* access modifiers changed from: protected */
    public void setDisableRcsByUserOpMode() {
        super.setDisableRcsByUserOpMode();
        this.mSharedInfo.setRcsByUser(true);
    }

    /* access modifiers changed from: protected */
    public void setEnableRcsByUserOpMode() {
        super.setEnableRcsByUserOpMode();
        if (getOpMode() == WorkflowBase.OpMode.ACTIVE) {
            this.mSharedInfo.setRcsByUser(true);
        }
        this.mModule.getAcsConfig(this.mPhoneId).disableRcsByAcs(false);
    }

    class Initialize implements WorkflowBase.Workflow {
        Initialize() {
        }

        public WorkflowBase.Workflow run() throws Exception {
            WorkflowBase.Workflow workflow;
            WorkflowUp workflowUp = WorkflowUp.this;
            workflowUp.mSharedInfo.setUrl(workflowUp.mParamHandler.initUrl());
            WorkflowUp.this.mCookieHandler.clearCookie();
            if (WorkflowUp.this.mMno.isEur()) {
                int activeDataPhoneId = SimUtil.getActiveDataPhoneId();
                WorkflowUp workflowUp2 = WorkflowUp.this;
                if (activeDataPhoneId == workflowUp2.mPhoneId) {
                    workflowUp2.mMobileNetwork = workflowUp2.isMobilePreferred();
                } else if (RcsUtils.DualRcs.isDualRcsReg()) {
                    WorkflowUp.this.mMobileNetwork = false;
                }
            }
            WorkflowUp workflowUp3 = WorkflowUp.this;
            if (workflowUp3.mStartForce) {
                workflow = new FetchHttp();
            } else {
                int i = AnonymousClass1.$SwitchMap$com$sec$internal$ims$config$workflow$WorkflowBase$OpMode[workflowUp3.getOpMode().ordinal()];
                workflow = (i == 1 || i == 2 || i == 3) ? new FetchHttp() : (i == 4 || i == 5) ? new Finish() : null;
            }
            if (!(workflow instanceof FetchHttp) || WorkflowUp.this.mMobileNetwork) {
                return workflow;
            }
            Log.i(WorkflowUp.LOG_TAG, "mMobileNetwork: false, try FetchHttps step");
            return new FetchHttps();
        }
    }

    /* renamed from: com.sec.internal.ims.config.workflow.WorkflowUp$1  reason: invalid class name */
    static /* synthetic */ class AnonymousClass1 {
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
            throw new UnsupportedOperationException("Method not decompiled: com.sec.internal.ims.config.workflow.WorkflowUp.AnonymousClass1.<clinit>():void");
        }
    }

    class FetchHttp implements WorkflowBase.Workflow {
        FetchHttp() {
        }

        public WorkflowBase.Workflow run() throws Exception {
            WorkflowUp.this.mSharedInfo.setHttpDefault();
            WorkflowUp workflowUp = WorkflowUp.this;
            workflowUp.mSharedInfo.setHttpResponse(workflowUp.getHttpResponse());
            int statusCode = WorkflowUp.this.mSharedInfo.getHttpResponse().getStatusCode();
            if (statusCode == 200 || statusCode == 511) {
                if (statusCode == 511) {
                    WorkflowUp.this.mIsHeaderEnrichment = true;
                }
                return new FetchHttps();
            }
            WorkflowUp workflowUp2 = WorkflowUp.this;
            return workflowUp2.handleResponseForUp(new Initialize(), new FetchHttps(), new Finish());
        }
    }

    class FetchHttps implements WorkflowBase.Workflow {
        FetchHttps() {
        }

        public WorkflowBase.Workflow run() throws Exception {
            WorkflowUp.this.setSharedInfoWithParamForUp();
            int statusCode = WorkflowUp.this.mSharedInfo.getHttpResponse().getStatusCode();
            if (statusCode == 200) {
                Log.i(WorkflowUp.LOG_TAG, "200 OK is received, try to parse");
                return new Parse();
            }
            if (statusCode == 403) {
                if (String.valueOf(WorkflowBase.OpMode.DISABLE_RCS_BY_USER.value()).equals(WorkflowUp.this.getRcsState())) {
                    return new Finish();
                }
                WorkflowUp workflowUp = WorkflowUp.this;
                if (workflowUp.mMobileNetwork && !workflowUp.mIsHeaderEnrichment) {
                    workflowUp.setOpMode(WorkflowBase.OpMode.DISABLE_TEMPORARY, (Map<String, String>) null);
                    Log.i(WorkflowUp.LOG_TAG, "403 is received, set version to zero");
                    return new Finish();
                } else if (!workflowUp.mSharedInfo.getHttpParams().containsKey("msisdn")) {
                    return WorkflowUp.this.getMsisdnWithDialog();
                } else {
                    if (!TextUtils.isEmpty(WorkflowUp.this.mSharedInfo.getUserMsisdn())) {
                        Log.i(WorkflowUp.LOG_TAG, "msisdn is wrong from user, try it again after 300 sec");
                        WorkflowUp.this.setValidityTimer(300);
                        return new Finish();
                    }
                }
            }
            WorkflowUp workflowUp2 = WorkflowUp.this;
            return workflowUp2.handleResponseForUp(new Initialize(), new FetchHttps(), new Finish());
        }
    }

    /* access modifiers changed from: package-private */
    public WorkflowBase.Workflow getMsisdnWithDialog() {
        String str;
        int msisdnSkipCount = this.mMsisdnHandler.getMsisdnSkipCount();
        if (this.mMobileNetwork || msisdnSkipCount != 3) {
            String str2 = LOG_TAG;
            Log.i(str2, "no msisdn, try to get user");
            if (msisdnSkipCount == -1) {
                this.mMsisdnHandler.setMsisdnSkipCount(0);
                msisdnSkipCount = 0;
            }
            this.mPowerController.release();
            String lastMsisdnValue = this.mMsisdnHandler.getLastMsisdnValue();
            if (!TextUtils.isEmpty(lastMsisdnValue)) {
                str = this.mDialog.getMsisdn(this.mTelephonyAdapter.getSimCountryCode(), lastMsisdnValue);
            } else {
                str = this.mDialog.getMsisdn(this.mTelephonyAdapter.getSimCountryCode());
            }
            this.mPowerController.lock();
            if (TextUtils.isEmpty(str)) {
                Log.i(str2, "user didn't enter msisdn finish process");
                return new Finish();
            } else if ("skip".equals(str)) {
                this.mMsisdnHandler.setMsisdnSkipCount(msisdnSkipCount + 1);
                Log.i(str2, "user enter skip msisdn.");
                this.mMsisdnHandler.setMsisdnMsguiDisplay(CloudMessageProviderContract.JsonData.TRUE);
                Intent intent = new Intent("com.sec.rcs.config.action.SET_SHOW_MSISDN_DIALOG");
                intent.putExtra("isNeeded", this.mMsisdnHandler.getIsNeeded());
                ContextExt.sendBroadcastAsUser(this.mContext, intent, ContextExt.ALL);
                return new Finish();
            } else {
                this.mSharedInfo.setUserMsisdn(str);
                return new Initialize();
            }
        } else {
            Log.i(LOG_TAG, "Retry counter for msisdn reached. Abort.");
            return new Finish();
        }
    }

    /* access modifiers changed from: protected */
    public void setSharedInfoWithParamForUp() {
        if (ConfigUtil.shallUsePreviousCookie(this.mLastErrorCode, this.mMno)) {
            this.mSharedInfo.setHttpsWithPreviousCookies();
        } else {
            this.mSharedInfo.setHttpsDefault();
        }
        if (this.mParamHandler.isConfigProxy()) {
            this.mSharedInfo.changeConfigProxyUriForHttp();
            this.mSharedInfo.setHttpProxyDefault();
        }
        IConfigModule configModule = ImsRegistry.getConfigModule();
        if (configModule.getAcsConfig(this.mPhoneId).isTriggeredByNrcr() && this.mMno.isOneOf(Mno.SWISSCOM, Mno.TMOBILE)) {
            setRcsState(String.valueOf(getVersion()));
            setRcsDisabledState("");
        }
        this.mCookieHandler.handleCookie(this.mSharedInfo.getHttpResponse());
        this.mSharedInfo.addHttpParam("vers", String.valueOf(getVersion()));
        this.mSharedInfo.addHttpParam("terminal_model", ConfigContract.BUILD.getTerminalModel());
        this.mSharedInfo.addHttpParam("terminal_vendor", "SEC");
        this.mSharedInfo.addHttpParam("terminal_sw_version", this.mParamHandler.getModelInfoFromCarrierVersion(ConfigUtil.getModelName(this.mPhoneId), ConfigConstants.PVALUE.TERMINAL_SW_VERSION, 8, true));
        this.mSharedInfo.addHttpParam("IMSI", this.mTelephonyAdapter.getSubscriberId(SimUtil.getSubId(this.mPhoneId)));
        this.mSharedInfo.addHttpParam(ConfigConstants.PNAME.IMEI, this.mTelephonyAdapter.getDeviceId(this.mPhoneId));
        this.mSharedInfo.addHttpParam("default_sms_app", ConfigUtil.isSecDmaPackageInuse(this.mContext, this.mPhoneId) ? "1" : "2");
        this.mSharedInfo.addHttpParam("rcs_version", this.mRcsVersion);
        String str = LOG_TAG;
        Log.i(str, "rcsProfile read and used : " + this.mRcsUPProfile);
        this.mSharedInfo.addHttpParam(ConfigConstants.PNAME.RCS_PROFILE, this.mRcsUPProfile);
        this.mSharedInfo.addHttpParam(ConfigConstants.PNAME.PROVISIONING_VERSION, this.mRcsProvisioningVersion);
        if (!ConfigUtil.doesUpRcsProfileMatchProvisioningVersion(this.mRcsUPProfile, this.mRcsProvisioningVersion)) {
            Log.w(str, "Provisioning version <-> RCS profile mismatch. Rcs profile is: " + this.mRcsUPProfile + " Provisioning version is: " + this.mRcsProvisioningVersion);
        }
        if (ImsProfile.isRcsUp2Profile(this.mRcsUPProfile) && !this.mRcsAppList.isEmpty()) {
            this.mSharedInfo.addHttpParam("app", String.join("||", this.mRcsAppList));
        }
        setRcsState(convertRcsStateWithSpecificParam());
        this.mSharedInfo.addHttpParam(ConfigConstants.PNAME.RCS_STATE, getRcsState());
        setSharedInfoWithAuthParamForUp();
        if (this.mStartForce && !String.valueOf(WorkflowBase.OpMode.DISABLE_RCS_BY_USER.value()).equals(getRcsState()) && (!configModule.getAcsConfig(this.mPhoneId).isTriggeredByNrcr() || this.mMno != Mno.MTS_RUSSIA)) {
            Log.i(str, "mStartForce: true, vers: 0");
            this.mSharedInfo.addHttpParam("vers", "0");
        }
        if (getOpMode() == WorkflowBase.OpMode.DORMANT) {
            Log.i(str, "use backup version in case of dormant, vers: " + getVersionBackup());
            this.mSharedInfo.addHttpParam("vers", getVersionBackup());
        }
        this.mSharedInfo.setHttpResponse(getHttpResponse());
    }

    /* access modifiers changed from: protected */
    public void setSharedInfoWithAuthParamForUp() {
        if (!this.mMobileNetwork || this.mIsHeaderEnrichment || this.mSharedInfo.getHttpResponse().getStatusCode() == 511) {
            String msisdn = this.mTelephonyAdapter.getMsisdn(SimUtil.getSubId(this.mPhoneId));
            if (!TextUtils.isEmpty(msisdn)) {
                this.mSharedInfo.addHttpParam("msisdn", this.mParamHandler.encodeRFC3986(ImsCallUtil.validatePhoneNumber(msisdn, this.mTelephonyAdapter.getSimCountryCode())));
            }
            if (!TextUtils.isEmpty(this.mSharedInfo.getUserMsisdn())) {
                SharedInfo sharedInfo = this.mSharedInfo;
                sharedInfo.addHttpParam("msisdn", this.mParamHandler.encodeRFC3986(sharedInfo.getUserMsisdn()));
            }
            this.mSharedInfo.addHttpParam(ConfigConstants.PNAME.SMS_PORT, ImsRegistry.getString(this.mPhoneId, GlobalSettingsConstants.RCS.SMS_DEST_PORT, this.mTelephonyAdapter.getSmsDestPort()));
            this.mSharedInfo.addHttpParam("token", getToken());
        }
    }

    class FetchOtp implements WorkflowBase.Workflow {
        FetchOtp() {
        }

        public WorkflowBase.Workflow run() throws Exception {
            WorkflowUp.this.mSharedInfo.setHttpClean();
            IHttpAdapter.Response httpResponse = WorkflowUp.this.mSharedInfo.getHttpResponse();
            if (WorkflowUp.this.mMno.isEmeasewaoce()) {
                WorkflowUp.this.mCookieHandler.handleCookie(httpResponse);
            } else {
                WorkflowUp.this.mSharedInfo.addHttpHeader(HttpController.HEADER_COOKIE, httpResponse.getHeader().get(HttpController.HEADER_SET_COOKIE));
            }
            SharedInfo sharedInfo = WorkflowUp.this.mSharedInfo;
            sharedInfo.addHttpParam(ConfigConstants.PNAME.OTP, sharedInfo.getOtp());
            IHttpAdapter.Response httpResponse2 = WorkflowUp.this.getHttpResponse();
            WorkflowUp.this.mSharedInfo.setHttpResponse(httpResponse2);
            if (httpResponse2.getStatusCode() == 200) {
                return new Parse();
            }
            WorkflowUp workflowUp = WorkflowUp.this;
            return workflowUp.handleResponseForUp(new Initialize(), new FetchHttps(), new Finish());
        }
    }

    class Authorize implements WorkflowBase.Workflow {
        Authorize() {
        }

        /* JADX WARNING: Code restructure failed: missing block: B:13:0x004c, code lost:
            r0 = r5.this$0;
         */
        /* JADX WARNING: Code restructure failed: missing block: B:6:0x002c, code lost:
            r0 = r5.this$0;
         */
        /* Code decompiled incorrectly, please refer to instructions dump. */
        public com.sec.internal.ims.config.workflow.WorkflowBase.Workflow run() throws java.lang.Exception {
            /*
                r5 = this;
                com.sec.internal.ims.config.workflow.WorkflowUp r0 = com.sec.internal.ims.config.workflow.WorkflowUp.this
                com.sec.internal.ims.config.PowerController r0 = r0.mPowerController
                r0.release()
                com.sec.internal.ims.config.workflow.WorkflowUp r0 = com.sec.internal.ims.config.workflow.WorkflowUp.this
                com.sec.internal.ims.config.SharedInfo r0 = r0.mSharedInfo
                java.lang.String r0 = r0.getOtp()
                com.sec.internal.ims.config.workflow.WorkflowUp r1 = com.sec.internal.ims.config.workflow.WorkflowUp.this
                java.lang.String r1 = r1.mSmsPort
                java.lang.String r2 = "0"
                boolean r1 = r2.equals(r1)
                r2 = 1
                if (r1 == 0) goto L_0x003c
                com.sec.internal.ims.config.workflow.WorkflowUp r1 = com.sec.internal.ims.config.workflow.WorkflowUp.this
                com.sec.internal.interfaces.ims.config.ITelephonyAdapter r1 = r1.mTelephonyAdapter
                java.lang.String r1 = r1.getExistingOtp()
                if (r1 == 0) goto L_0x002c
                boolean r0 = android.text.TextUtils.equals(r1, r0)
                if (r0 == 0) goto L_0x0064
            L_0x002c:
                com.sec.internal.ims.config.workflow.WorkflowUp r0 = com.sec.internal.ims.config.workflow.WorkflowUp.this
                int r3 = r0.mAuthTryCount
                if (r3 >= r2) goto L_0x0064
                int r3 = r3 + r2
                r0.mAuthTryCount = r3
                com.sec.internal.interfaces.ims.config.ITelephonyAdapter r0 = r0.mTelephonyAdapter
                java.lang.String r1 = r0.getOtp()
                goto L_0x0064
            L_0x003c:
                com.sec.internal.ims.config.workflow.WorkflowUp r1 = com.sec.internal.ims.config.workflow.WorkflowUp.this
                com.sec.internal.interfaces.ims.config.ITelephonyAdapter r1 = r1.mTelephonyAdapter
                java.lang.String r1 = r1.getExistingPortOtp()
                if (r1 == 0) goto L_0x004c
                boolean r0 = android.text.TextUtils.equals(r1, r0)
                if (r0 == 0) goto L_0x0064
            L_0x004c:
                com.sec.internal.ims.config.workflow.WorkflowUp r0 = com.sec.internal.ims.config.workflow.WorkflowUp.this
                int r3 = r0.mAuthHiddenTryCount
                r4 = 3
                if (r3 >= r4) goto L_0x0064
                int r3 = r3 + r2
                r0.mAuthHiddenTryCount = r3
                com.sec.internal.interfaces.ims.config.ITelephonyAdapter r0 = r0.mTelephonyAdapter
                java.lang.String r1 = r0.getPortOtp()
                if (r1 != 0) goto L_0x0064
                com.sec.internal.ims.config.workflow.WorkflowUp r0 = com.sec.internal.ims.config.workflow.WorkflowUp.this
                r2 = 0
                r0.setValidityTimer(r2)
            L_0x0064:
                if (r1 == 0) goto L_0x0098
                java.lang.String r0 = com.sec.internal.ims.config.workflow.WorkflowUp.LOG_TAG
                java.lang.StringBuilder r2 = new java.lang.StringBuilder
                r2.<init>()
                java.lang.String r3 = "otp: "
                r2.append(r3)
                java.lang.String r3 = com.sec.internal.log.IMSLog.checker(r1)
                r2.append(r3)
                java.lang.String r2 = r2.toString()
                android.util.Log.i(r0, r2)
                com.sec.internal.ims.config.workflow.WorkflowUp r0 = com.sec.internal.ims.config.workflow.WorkflowUp.this
                com.sec.internal.ims.config.SharedInfo r0 = r0.mSharedInfo
                r0.setOtp(r1)
                com.sec.internal.ims.config.workflow.WorkflowUp r0 = com.sec.internal.ims.config.workflow.WorkflowUp.this
                com.sec.internal.ims.config.PowerController r0 = r0.mPowerController
                r0.lock()
                com.sec.internal.ims.config.workflow.WorkflowUp$FetchOtp r0 = new com.sec.internal.ims.config.workflow.WorkflowUp$FetchOtp
                com.sec.internal.ims.config.workflow.WorkflowUp r5 = com.sec.internal.ims.config.workflow.WorkflowUp.this
                r0.<init>()
                return r0
            L_0x0098:
                java.lang.String r0 = com.sec.internal.ims.config.workflow.WorkflowUp.LOG_TAG
                java.lang.String r1 = "otp: null, go to finish state"
                android.util.Log.i(r0, r1)
                com.sec.internal.ims.config.workflow.WorkflowUp$Finish r0 = new com.sec.internal.ims.config.workflow.WorkflowUp$Finish
                com.sec.internal.ims.config.workflow.WorkflowUp r5 = com.sec.internal.ims.config.workflow.WorkflowUp.this
                r0.<init>()
                return r0
            */
            throw new UnsupportedOperationException("Method not decompiled: com.sec.internal.ims.config.workflow.WorkflowUp.Authorize.run():com.sec.internal.ims.config.workflow.WorkflowBase$Workflow");
        }
    }

    class Parse implements WorkflowBase.Workflow {
        Parse() {
        }

        public WorkflowBase.Workflow run() throws Exception {
            byte[] body = WorkflowUp.this.mSharedInfo.getHttpResponse().getBody();
            if (body == null) {
                body = "".getBytes();
            }
            Map<String, String> parse = WorkflowUp.this.mXmlParser.parse(new String(body, "utf-8"));
            if (parse == null) {
                throw new InvalidXmlException("parsedXml is null");
            } else if (parse.get("root/vers/version") == null || parse.get("root/vers/validity") == null) {
                Log.i(WorkflowUp.LOG_TAG, "parsedXml need to contain version, validity items");
                WorkflowUp workflowUp = WorkflowUp.this;
                if (workflowUp.mCookieHandler.isCookie(workflowUp.mSharedInfo.getHttpResponse())) {
                    WorkflowUp.this.mSmsPort = parse.get(ConfigConstants.PATH.POLICY_SMS_PORT);
                    return new Authorize();
                }
                throw new EmptyBodyAndCookieException("body and cookie are null");
            } else {
                WorkflowUp workflowUp2 = WorkflowUp.this;
                workflowUp2.mIsXmlReceived = true;
                workflowUp2.mSharedInfo.setParsedXml(parse);
                WorkflowUp workflowUp3 = WorkflowUp.this;
                workflowUp3.mMsisdnHandler.setMsisdnValue(workflowUp3.mSharedInfo.getUserMsisdn());
                return new Store();
            }
        }
    }

    class Store implements WorkflowBase.Workflow {
        Store() {
        }

        public WorkflowBase.Workflow run() throws Exception {
            WorkflowBase.OpMode opMode = WorkflowBase.OpMode.DISABLE_RCS_BY_USER;
            if (String.valueOf(opMode.value()).equals(WorkflowUp.this.getRcsState()) && !WorkflowUp.this.mMno.isOneOf(Mno.TELEFONICA_GERMANY, Mno.TELEFONICA_SPAIN, Mno.TELEFONICA_UK, Mno.MTS_RUSSIA)) {
                return new Finish();
            }
            Map<String, String> parsedXml = WorkflowUp.this.mSharedInfo.getParsedXml();
            WorkflowBase.OpMode rcsDisabledState = WorkflowUp.this.getRcsDisabledState(parsedXml);
            if (WorkflowUp.this.isValidRcsDisabledState(rcsDisabledState)) {
                WorkflowUp.this.setOpMode(rcsDisabledState, parsedXml);
                return new Finish();
            }
            WorkflowParamHandler.UserAccept userAcceptDetailed = WorkflowUp.this.mParamHandler.getUserAcceptDetailed(parsedXml);
            int i = 1;
            if (userAcceptDetailed == WorkflowParamHandler.UserAccept.NON_DEFAULT_MSG_APP) {
                WorkflowUp.this.setOpMode(opMode, (Map<String, String>) null);
            } else {
                WorkflowUp.this.mParamHandler.setOpModeWithUserAccept(userAcceptDetailed == WorkflowParamHandler.UserAccept.ACCEPT, parsedXml, WorkflowBase.OpMode.DISABLE);
                if (WorkflowUp.this.getOpMode() == WorkflowBase.OpMode.ACTIVE) {
                    WorkflowUp workflowUp = WorkflowUp.this;
                    workflowUp.setValidityTimer(workflowUp.getValidity());
                }
            }
            WorkflowUp.this.mMsisdnHandler.setMsisdnSkipCount(0);
            WorkflowUp workflowUp2 = WorkflowUp.this;
            if (userAcceptDetailed != WorkflowParamHandler.UserAccept.ACCEPT) {
                i = 0;
            }
            workflowUp2.setTcUserAccept(i);
            return new Finish();
        }
    }

    class Finish implements WorkflowBase.Workflow {
        Finish() {
        }

        public WorkflowBase.Workflow run() throws Exception {
            if (WorkflowUp.this.mSharedInfo.getHttpResponse() != null) {
                if (WorkflowUp.this.mSharedInfo.getHttpResponse().getStatusCode() == 200) {
                    WorkflowUp.this.mSharedInfo.setInternal403ErrRetryCount(0);
                    WorkflowUp.this.mSharedInfo.setInternal404ErrRetryCount(0);
                    WorkflowUp.this.mSharedInfo.setUnknownErrRetryCount(0);
                }
                WorkflowUp workflowUp = WorkflowUp.this;
                workflowUp.setLastErrorCode(workflowUp.mSharedInfo.getHttpResponse().getStatusCode());
            }
            WorkflowUp.this.mSharedInfo.setRcsByUser(false);
            Log.i(WorkflowUp.LOG_TAG, "all workflow is finished");
            return null;
        }
    }
}
