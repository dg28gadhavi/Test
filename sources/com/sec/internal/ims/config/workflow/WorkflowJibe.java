package com.sec.internal.ims.config.workflow;

import android.content.Intent;
import android.database.sqlite.SQLiteFullException;
import android.net.ConnectivityManager;
import android.net.Network;
import android.net.NetworkRequest;
import android.os.Message;
import android.text.TextUtils;
import android.util.Log;
import com.sec.ims.extensions.ContextExt;
import com.sec.internal.constants.Mno;
import com.sec.internal.constants.ims.DiagnosisConstants;
import com.sec.internal.constants.ims.cmstore.CloudMessageProviderContract;
import com.sec.internal.constants.ims.entitilement.SoftphoneNamespaces;
import com.sec.internal.helper.DmConfigHelper;
import com.sec.internal.helper.httpclient.HttpController;
import com.sec.internal.ims.config.SharedInfo;
import com.sec.internal.ims.config.exception.EmptyBodyAndCookieException;
import com.sec.internal.ims.config.exception.InvalidHeaderException;
import com.sec.internal.ims.config.exception.NoInitialDataException;
import com.sec.internal.ims.config.exception.UnknownStatusException;
import com.sec.internal.ims.config.workflow.WorkflowBase;
import com.sec.internal.ims.config.workflow.WorkflowParamHandler;
import com.sec.internal.ims.servicemodules.ss.UtStateMachine;
import com.sec.internal.ims.settings.DeviceConfigManager;
import com.sec.internal.ims.util.ConfigUtil;
import com.sec.internal.imscr.LogClass;
import com.sec.internal.interfaces.ims.config.IHttpAdapter;
import com.sec.internal.log.IMSLog;
import java.util.Date;
import java.util.Locale;
import java.util.Map;

public class WorkflowJibe extends WorkflowUpBase {
    protected static final int BADREQERR_RETRY_AFTER_TIME = 43200;
    protected static final int HTTPERR_RETRY_AFTER_TIME = 10;
    protected static final int HTTPERR_TRY_MAX_COUNT = 2;
    protected static final int IIDTOKENERR_RETRY_AFTER_TIME = 60;
    protected static final int IIDTOKENERR_RETRY_LIMIT = 3;
    protected static final String LOG_TAG = WorkflowJibe.class.getSimpleName();
    protected static final int MSISDN_TRY_MAX_COUNT = 1;
    protected static final int MSISDN_TRY_MAX_COUNT_NON_GC = 3;
    protected static final String OTP_SMS_BINARY_TYPE = "binary";
    protected static final String OTP_SMS_TEXT_TYPE = "text";
    protected static final int OTP_SMS_TIME_OUT = 700;
    protected int m503ErrCount = 0;
    protected int m511ErrCount = 0;
    protected int mAuthHiddenTryCount = 0;
    protected int mAuthTryCount = 0;
    protected int mHttpResponse = 0;
    protected String mIidToken = null;
    protected int mIidTokenRetryLimit = 0;
    protected boolean mIsEnrichedHeaderFailed = false;
    protected boolean mIsMobileConfigCompleted = false;
    protected boolean mIsMobileConfigNeeded = false;
    protected boolean mIsMobileConnected = false;
    protected boolean mIsMobileRequested = false;
    protected boolean mIsWifiConnected = false;
    final ConnectivityManager.NetworkCallback mMobileStateCallback = new ConnectivityManager.NetworkCallback() {
        public void onAvailable(Network network) {
            WorkflowJibe.this.onMobileConnectionChanged(network, true);
        }

        public void onLost(Network network) {
            WorkflowJibe.this.onMobileConnectionChanged(network, false);
        }
    };
    protected int mMsisdnTryCount = 0;

    /* JADX WARNING: Illegal instructions before constructor call */
    /* Code decompiled incorrectly, please refer to instructions dump. */
    public WorkflowJibe(android.os.Looper r13, android.content.Context r14, com.sec.internal.interfaces.ims.config.IConfigModule r15, com.sec.internal.constants.Mno r16, int r17) {
        /*
            r12 = this;
            r11 = r12
            r2 = r14
            r3 = r15
            r10 = r17
            com.sec.internal.ims.config.adapters.TelephonyAdapterPrimaryDeviceJibe r5 = new com.sec.internal.ims.config.adapters.TelephonyAdapterPrimaryDeviceJibe
            r5.<init>(r14, r15, r10)
            com.sec.internal.ims.config.adapters.StorageAdapter r6 = new com.sec.internal.ims.config.adapters.StorageAdapter
            r6.<init>()
            com.sec.internal.ims.config.adapters.HttpAdapterJibeAndSec r7 = new com.sec.internal.ims.config.adapters.HttpAdapterJibeAndSec
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
            r11.mIsMobileRequested = r0
            r11.mIsMobileConnected = r0
            r11.mIsWifiConnected = r0
            r11.mIsMobileConfigNeeded = r0
            r11.mIsMobileConfigCompleted = r0
            r11.mIsEnrichedHeaderFailed = r0
            r1 = 0
            r11.mIidToken = r1
            r11.mIidTokenRetryLimit = r0
            r11.mHttpResponse = r0
            r11.mAuthTryCount = r0
            r11.mAuthHiddenTryCount = r0
            r11.mMsisdnTryCount = r0
            r11.m511ErrCount = r0
            r11.m503ErrCount = r0
            com.sec.internal.ims.config.workflow.WorkflowJibe$1 r0 = new com.sec.internal.ims.config.workflow.WorkflowJibe$1
            r0.<init>()
            r11.mMobileStateCallback = r0
            return
        */
        throw new UnsupportedOperationException("Method not decompiled: com.sec.internal.ims.config.workflow.WorkflowJibe.<init>(android.os.Looper, android.content.Context, com.sec.internal.interfaces.ims.config.IConfigModule, com.sec.internal.constants.Mno, int):void");
    }

    public void handleMessage(Message message) {
        String str = LOG_TAG;
        int i = this.mPhoneId;
        IMSLog.i(str, i, "handleMessage: " + message.what);
        int i2 = message.what;
        if (i2 == 0) {
            this.mStartForce = true;
        } else if (i2 != 1) {
            if (i2 == 3) {
                removeMessages(4);
                if (this.mIsMobileConfigCompleted) {
                    Log.i(str, "mIsMobileConfigCompleted: " + this.mIsMobileConfigCompleted);
                    return;
                }
                this.mPowerController.lock();
                executeAutoConfig();
                this.mIsMobileConfigCompleted = true;
                unregisterMobileNetwork(this.mConnectivityManager, this.mMobileStateCallback);
                this.mNewVersion = getVersion();
                endAutoConfig(true);
                int i3 = this.mPhoneId;
                IMSLog.i(str, i3, "oldVersion: " + this.mOldVersion + " newVersion: " + this.mNewVersion);
                IMSLog.i(str, this.mPhoneId, "AutoConfig: finish");
                this.mModule.getHandler().sendMessage(obtainMessage(3, this.mOldVersion, this.mNewVersion, Integer.valueOf(this.mPhoneId)));
                this.mPowerController.release();
                return;
            } else if (i2 != 4) {
                if (i2 != 5) {
                    super.handleMessage(message);
                    return;
                } else if (DmConfigHelper.getImsSwitchValue(this.mContext, DeviceConfigManager.DEFAULTMSGAPPINUSE, this.mPhoneId) == 1) {
                    IMSLog.i(str, this.mPhoneId, "sms default application is changed to samsung");
                    WorkflowBase.OpMode opMode = WorkflowBase.OpMode.DISABLE_TEMPORARY;
                    setVersion(opMode.value());
                    setRcsState(String.valueOf(opMode.value()));
                    setRcsDisabledState("");
                    setValidity(opMode.value());
                    cancelValidityTimer();
                    setNextAutoconfigTime((long) opMode.value());
                    this.mIsConfigOngoing = false;
                    return;
                } else if (!this.mModule.isValidConfigDb(this.mPhoneId) || TextUtils.isEmpty(getToken())) {
                    IMSLog.i(str, this.mPhoneId, "not to trigger a config because of invalid config");
                    return;
                } else {
                    IMSLog.i(str, this.mPhoneId, "sms default application is changed to non-samsung");
                    setOpMode(WorkflowBase.OpMode.DISABLE_RCS_BY_USER, (Map<String, String>) null);
                    removeMessages(1);
                    sendEmptyMessage(1);
                    return;
                }
            } else if (this.mIsMobileConfigCompleted) {
                Log.i(str, "mIsMobileConfigCompleted: " + this.mIsMobileConfigCompleted);
                return;
            } else {
                this.mPowerController.lock();
                changeOpMode(true);
                unregisterMobileNetwork(this.mConnectivityManager, this.mMobileStateCallback);
                this.mNewVersion = getVersion();
                endAutoConfig(false);
                int i4 = this.mPhoneId;
                IMSLog.i(str, i4, "oldVersion: " + this.mOldVersion + " newVersion: " + this.mNewVersion);
                IMSLog.i(str, this.mPhoneId, "AutoConfig: finish");
                this.mModule.getHandler().sendMessage(obtainMessage(3, this.mOldVersion, this.mNewVersion, Integer.valueOf(this.mPhoneId)));
                this.mPowerController.release();
                return;
            }
        }
        if (this.mIsConfigOngoing) {
            Log.i(str, "AutoConfig: ongoing");
            return;
        }
        this.mIsConfigOngoing = true;
        int i5 = this.mPhoneId;
        IMSLog.i(str, i5, "AutoConfig: start, mStartForce: " + this.mStartForce);
        this.mModule.getHandler().removeMessages(3, Integer.valueOf(this.mPhoneId));
        this.mPowerController.lock();
        initAutoConfig();
        int version = getVersion();
        this.mOldVersion = version;
        if (scheduleAutoconfigForJibe(version)) {
            Log.i(str, "mIsWifiConnected: " + this.mIsWifiConnected + " mIsMobileConfigNeeded: " + this.mIsMobileConfigNeeded);
            if (!this.mIsWifiConnected || !this.mIsMobileConfigNeeded) {
                executeAutoConfig();
            } else {
                Log.i(str, "use mobile network");
                this.mIsMobileRequested = true;
                NetworkRequest build = new NetworkRequest.Builder().addTransportType(0).addCapability(12).build();
                this.mNetworkRequest = build;
                registerMobileNetwork(this.mConnectivityManager, build, this.mMobileStateCallback);
                removeMessages(4);
                sendMessageDelayed(obtainMessage(4), SoftphoneNamespaces.SoftphoneSettings.LONG_BACKOFF);
                this.mPowerController.release();
                return;
            }
        }
        this.mNewVersion = getVersion();
        endAutoConfig(true);
        int i6 = this.mPhoneId;
        IMSLog.i(str, i6, "oldVersion: " + this.mOldVersion + " newVersion: " + this.mNewVersion);
        IMSLog.i(str, this.mPhoneId, "AutoConfig: finish");
        this.mModule.getHandler().sendMessage(obtainMessage(3, this.mOldVersion, this.mNewVersion, Integer.valueOf(this.mPhoneId)));
        this.mPowerController.release();
    }

    /* access modifiers changed from: package-private */
    public void onMobileConnectionChanged(Network network, boolean z) {
        if (z) {
            if (this.mIsMobileRequested && !this.mIsMobileConnected) {
                String str = LOG_TAG;
                Log.i(str, "onMobileConnectionChanged: onAvailable");
                if (network != null) {
                    Log.i(str, "mobile connection is successful");
                    this.mNetwork = network;
                    this.mIsMobileConnected = true;
                    sendEmptyMessage(3);
                    return;
                }
                Log.i(str, "mobile connection info is empty");
            }
        } else if (this.mIsMobileRequested) {
            Log.i(LOG_TAG, "onMobileConnectionChanged: onLost");
            this.mIsMobileConnected = false;
        }
    }

    /* access modifiers changed from: protected */
    public boolean scheduleAutoconfigForJibe(int i) {
        String str = LOG_TAG;
        IMSLog.i(str, this.mPhoneId, "scheduleAutoconfigForJibe");
        if (!needScheduleAutoconfig(this.mPhoneId)) {
            Log.i(str, "needScheduleAutoconfig: false");
            return false;
        } else if (this.mStartForce) {
            cancelValidityTimer();
            Log.i(str, "force autoconfig");
            return true;
        } else if (i == -1 || i == -2) {
            Log.i(str, "currentVersion: " + i + " skip autoconfig");
            return false;
        } else {
            long nextAutoconfigTime = getNextAutoconfigTime();
            Log.i(str, "nextAutoconfigTime: " + nextAutoconfigTime);
            int time = (int) ((nextAutoconfigTime - new Date().getTime()) / 1000);
            Log.i(str, "remainValidity: " + time);
            if (time <= 0) {
                Log.i(str, "need autoconfig");
                return true;
            }
            if (nextAutoconfigTime > 0) {
                Log.i(str, "autoconfig schedule: after " + time + " seconds");
                IMSLog.c(LogClass.WFJ_VALIDITY_NON_EXPIRED, this.mPhoneId + ",VNE:" + time);
                addEventLog(str + ": autoconfig schedule: after " + time + " seconds");
                setValidityTimer(time);
            }
            return false;
        }
    }

    /* access modifiers changed from: protected */
    public void initAutoConfig() {
        IMSLog.i(LOG_TAG, this.mPhoneId, "initAutoConfig");
        this.mNetwork = null;
        this.mIsMobileRequested = false;
        this.mIsMobileConnected = false;
        this.mIsWifiConnected = checkWifiConnection(this.mConnectivityManager);
        if (this.mMno == Mno.SPRINT) {
            this.mIsMobileConfigNeeded = true;
        }
        this.mIsMobileConfigCompleted = false;
        this.mAuthTryCount = 0;
        this.mAuthHiddenTryCount = 0;
        this.mMsisdnTryCount = 0;
    }

    /* access modifiers changed from: protected */
    public void endAutoConfig(boolean z) {
        super.endAutoConfig(z);
        this.mIsMobileRequested = false;
        this.mIsMobileConnected = false;
        this.mIsMobileConfigNeeded = false;
    }

    /* access modifiers changed from: package-private */
    public void work() {
        WorkflowBase.Workflow workflow;
        WorkflowBase.Workflow nextWorkflow = getNextWorkflow(1);
        int i = WorkflowBase.AUTO_CONFIG_MAX_FLOWCOUNT;
        while (nextWorkflow != null && i > 0) {
            try {
                nextWorkflow = nextWorkflow.run();
            } catch (NoInitialDataException e) {
                Log.i(LOG_TAG, "NoInitialDataException: " + e.getMessage());
                Log.i(LOG_TAG, "wait 10 sec and retry");
                sleep(10000);
                workflow = getNextWorkflow(1);
                e.printStackTrace();
                nextWorkflow = workflow;
                i--;
            } catch (UnknownStatusException e2) {
                String message = e2.getMessage();
                Log.i(LOG_TAG, "UnknownStatusException: " + message);
                if (e2 instanceof EmptyBodyAndCookieException) {
                    workflow = getNextWorkflow(8);
                } else {
                    Log.i(LOG_TAG, "wait 2 sec and retry");
                    sleep(UtStateMachine.HTTP_READ_TIMEOUT_GCF);
                    workflow = getNextWorkflow(1);
                }
                e2.printStackTrace();
                nextWorkflow = workflow;
                i--;
            } catch (SQLiteFullException e3) {
                Log.i(LOG_TAG, "SQLiteFullException occur: " + e3.getMessage());
                workflow = getNextWorkflow(8);
                e3.printStackTrace();
                nextWorkflow = workflow;
                i--;
            } catch (Exception e4) {
                if (e4.getMessage() != null) {
                    Log.i(LOG_TAG, "unknown exception occur: " + e4.getMessage());
                }
                Log.i(LOG_TAG, "wait 1 sec and retry");
                sleep(1000);
                workflow = getNextWorkflow(1);
                e4.printStackTrace();
                nextWorkflow = workflow;
                i--;
            }
            i--;
        }
    }

    /* access modifiers changed from: protected */
    public IHttpAdapter.Response getHttpResponse() {
        String str = LOG_TAG;
        IMSLog.i(str, this.mPhoneId, "getHttpResponse");
        this.mHttp.close();
        this.mHttp.setHeaders(this.mSharedInfo.getHttpHeaders());
        this.mHttp.setParams(this.mSharedInfo.getHttpParams());
        this.mHttp.setContext(this.mContext);
        Log.i(str, "mIsMobileConfigNeeded: " + this.mIsMobileConfigNeeded + ", mIsMobileRequested: " + this.mIsMobileRequested + ", mIsMobileConnected: " + this.mIsMobileConnected);
        if (!this.mIsMobileConfigNeeded || !this.mIsMobileRequested || !this.mIsMobileConnected || !checkMobileConnection(this.mConnectivityManager)) {
            Log.i(str, "set network to default network");
            this.mHttp.setNetwork((Network) null);
        } else {
            Log.i(str, "set network to mobile network");
            this.mHttp.setNetwork(this.mNetwork);
        }
        this.mHttp.open(this.mSharedInfo.getUrl());
        IHttpAdapter.Response request = this.mHttp.request();
        this.mHttp.close();
        return request;
    }

    /* access modifiers changed from: protected */
    public WorkflowBase.Workflow handleResponse(WorkflowBase.Workflow workflow, int i) throws InvalidHeaderException, UnknownStatusException {
        String str;
        String str2 = LOG_TAG;
        IMSLog.i(str2, this.mPhoneId, "handleResponse: " + i);
        addEventLog(str2 + ": handleResponse: " + i);
        setLastErrorCode(i);
        int lastErrorCode = getLastErrorCode();
        if (lastErrorCode != 0) {
            if (lastErrorCode == 200) {
                this.m511ErrCount = 0;
                this.m503ErrCount = 0;
            } else if (lastErrorCode != 403) {
                if (lastErrorCode == 503) {
                    long j = getretryAfterTime();
                    Log.i(str2, "m503ErrCount: " + this.m503ErrCount + " retryAfterTime: " + j);
                    int i2 = this.m503ErrCount;
                    if (i2 < 2) {
                        this.m503ErrCount = i2 + 1;
                        Log.i(str2, "retry after " + j + " sec");
                        int i3 = (int) j;
                        setValidityTimer(i3);
                        setNextAutoconfigTimeAfter(i3);
                    }
                    return getNextWorkflow(8);
                } else if (lastErrorCode != 511) {
                    if (lastErrorCode == 400) {
                        Log.i(str2, "bad request received, set version to zero");
                        setOpMode(WorkflowBase.OpMode.DISABLE_TEMPORARY, (Map<String, String>) null);
                        return getNextWorkflow(8);
                    } else if (lastErrorCode != 401) {
                        switch (lastErrorCode) {
                            case 800:
                            case 801:
                            case 802:
                            case 803:
                            case 804:
                            case 805:
                                break;
                        }
                    } else {
                        setToken("", DiagnosisConstants.RCSA_TDRE.INVALID_IIDTOKEN);
                        Log.i(str2, "retry after 43200 sec");
                        setValidityTimer(BADREQERR_RETRY_AFTER_TIME);
                        setNextAutoconfigTimeAfter(BADREQERR_RETRY_AFTER_TIME);
                        return getNextWorkflow(8);
                    }
                } else if (workflow instanceof WorkflowBase.FetchHttp) {
                    return getNextWorkflow(3);
                } else {
                    Log.i(str2, "The token isn't valid: m511ErrCount: " + this.m511ErrCount);
                    setToken("", DiagnosisConstants.RCSA_TDRE.INVALID_TOKEN);
                    int i4 = this.m511ErrCount;
                    if (i4 < 2) {
                        this.m511ErrCount = i4 + 1;
                        Log.i(str2, "retry after 10 sec");
                        setValidityTimer(10);
                        setNextAutoconfigTimeAfter(10);
                    }
                    return getNextWorkflow(8);
                }
            } else if (!(workflow instanceof WorkflowBase.FetchHttps)) {
                return getNextWorkflow(8);
            } else {
                if (String.valueOf(WorkflowBase.OpMode.DISABLE_RCS_BY_USER.value()).equals(getRcsState())) {
                    return getNextWorkflow(8);
                }
                Log.i(str2, "403 is received, mMsisdnTryCount:" + this.mMsisdnTryCount);
                if (isMsisdnForGcNeeded()) {
                    this.mMsisdnTryCount++;
                    if (this.mMobileNetwork || this.mMsisdnHandler.getMsisdnSkipCount() != 3) {
                        str = getMsisdnForGc();
                        if ("skip".equals(str)) {
                            WorkflowMsisdnHandler workflowMsisdnHandler = this.mMsisdnHandler;
                            workflowMsisdnHandler.setMsisdnSkipCount(workflowMsisdnHandler.getMsisdnSkipCount() + 1);
                            Log.i(str2, "user enter skip msisdn.");
                            this.mMsisdnHandler.setMsisdnMsguiDisplay(CloudMessageProviderContract.JsonData.TRUE);
                            Intent intent = new Intent();
                            intent.setAction("com.sec.rcs.config.action.SET_SHOW_MSISDN_DIALOG");
                            intent.putExtra("isNeeded", this.mMsisdnHandler.getIsNeeded());
                            ContextExt.sendBroadcastAsUser(this.mContext, intent, ContextExt.ALL);
                            return getNextWorkflow(8);
                        }
                    } else {
                        Log.i(str2, "Retry counter for msisdn reached. Abort.");
                        return getNextWorkflow(8);
                    }
                } else {
                    str = getMsisdnForJibe();
                }
                this.mSharedInfo.setUserMsisdn(str);
                if (TextUtils.isEmpty(str)) {
                    Log.i(str2, "msisdn doesn't exist");
                    return getNextWorkflow(8);
                }
                Log.i(str2, "msisdn exists");
                return getNextWorkflow(1);
            }
            return super.handleResponse(workflow, i);
        }
        if (this.mMno != Mno.SPRINT || !(workflow instanceof WorkflowBase.FetchHttp) || this.mIsEnrichedHeaderFailed) {
            return super.handleResponse(workflow, i);
        }
        Log.i(str2, "http enriched header is failed, retry it with default header");
        this.mIsEnrichedHeaderFailed = true;
        return getNextWorkflow(2);
    }

    private String getMsisdnForJibe() {
        int i = this.mMsisdnTryCount;
        if (i >= 1) {
            return null;
        }
        this.mMsisdnTryCount = i + 1;
        String str = LOG_TAG;
        Log.i(str, "need msisdn from telephony/application");
        this.mPowerController.release();
        String msisdnNumber = this.mTelephonyAdapter.getMsisdnNumber();
        Log.i(str, "receive msisdn from telephony/application");
        IMSLog.s(str, "msisdn: " + msisdnNumber);
        this.mPowerController.lock();
        return msisdnNumber;
    }

    private boolean isMsisdnForGcNeeded() {
        return this.mMsisdnTryCount < 3 && !ConfigUtil.isRcsPreConsent(this.mPhoneId) && (this.mMno.isEmeasewaoce() || this.mMno == Mno.CLARO_DOMINICAN);
    }

    /* access modifiers changed from: private */
    public boolean isIidTokenInvalid(String str) {
        if (str != null && !"".equals(str)) {
            return false;
        }
        StringBuilder sb = new StringBuilder();
        String str2 = LOG_TAG;
        sb.append(str2);
        sb.append("iidToken is null or empty");
        addEventLog(sb.toString());
        Log.i(str2, "retry after 60 sec, IIDTOKENERR_RETRY_LIMIT is " + this.mIidTokenRetryLimit);
        int i = this.mIidTokenRetryLimit;
        if (i < 3) {
            this.mIidTokenRetryLimit = i + 1;
            setValidityTimer(60);
            setNextAutoconfigTimeAfter(60);
        }
        return true;
    }

    /* access modifiers changed from: private */
    public void retryConfigAfterTime() {
        String str = LOG_TAG;
        Log.i(str, "retry after 60 sec, IIDTOKENERR_RETRY_LIMIT is " + this.mIidTokenRetryLimit);
        int i = this.mIidTokenRetryLimit;
        if (i < 3) {
            this.mIidTokenRetryLimit = i + 1;
            setValidityTimer(60);
            setNextAutoconfigTimeAfter(60);
        }
    }

    /* access modifiers changed from: private */
    public String getIidToken() {
        String str = LOG_TAG;
        Log.i(str, "need iid token from telephony/application");
        this.mPowerController.release();
        String iidToken = this.mTelephonyAdapter.getIidToken();
        Log.i(str, "receive iid token from telephony/application");
        IMSLog.s(str, "iidToken: " + iidToken);
        this.mPowerController.lock();
        return iidToken;
    }

    private String getMsisdnForGc() {
        String str;
        if (this.mMsisdnHandler.getMsisdnSkipCount() == -1) {
            this.mMsisdnHandler.setMsisdnSkipCount(0);
        }
        this.mPowerController.release();
        if (!TextUtils.isEmpty(this.mMsisdnHandler.getLastMsisdnValue())) {
            str = this.mDialog.getMsisdn(this.mTelephonyAdapter.getSimCountryCode(), this.mMsisdnHandler.getLastMsisdnValue());
        } else {
            str = this.mDialog.getMsisdn(this.mTelephonyAdapter.getSimCountryCode());
        }
        String str2 = LOG_TAG;
        IMSLog.s(str2, "msisdn: " + str);
        this.mPowerController.lock();
        return str;
    }

    /* access modifiers changed from: protected */
    public WorkflowBase.Workflow getNextWorkflow(int i) {
        switch (i) {
            case 1:
                return new WorkflowBase.Initialize() {
                    public WorkflowBase.Workflow run() throws Exception {
                        String str = WorkflowJibe.LOG_TAG;
                        IMSLog.i(str, WorkflowJibe.this.mPhoneId, "Initialize:");
                        WorkflowBase.Workflow run = super.run();
                        if (!(run instanceof WorkflowBase.FetchHttp)) {
                            return run;
                        }
                        WorkflowJibe workflowJibe = WorkflowJibe.this;
                        if (workflowJibe.mMobileNetwork || workflowJibe.mIsMobileConfigNeeded) {
                            return run;
                        }
                        Log.i(str, "mMobileNetwork: false, try FetchHttps step");
                        return WorkflowJibe.this.getNextWorkflow(3);
                    }

                    /* access modifiers changed from: protected */
                    public void init() throws NoInitialDataException {
                        WorkflowJibe workflowJibe = WorkflowJibe.this;
                        workflowJibe.mIsEnrichedHeaderFailed = false;
                        workflowJibe.mHttpResponse = 0;
                        super.init();
                    }
                };
            case 2:
                return new WorkflowBase.FetchHttp() {
                    public WorkflowBase.Workflow run() throws Exception {
                        String str = WorkflowJibe.LOG_TAG;
                        IMSLog.i(str, WorkflowJibe.this.mPhoneId, "FetchHttp:");
                        WorkflowJibe workflowJibe = WorkflowJibe.this;
                        if (ConfigUtil.getGmsVersion(workflowJibe.mContext, workflowJibe.mPhoneId) == 0) {
                            IMSLog.i(str, WorkflowJibe.this.mPhoneId, "GmsVersion is invalid.");
                            WorkflowJibe.this.retryConfigAfterTime();
                            return WorkflowJibe.this.getNextWorkflow(8);
                        }
                        WorkflowJibe workflowJibe2 = WorkflowJibe.this;
                        if (ConfigUtil.isIidTokenNeeded(workflowJibe2.mContext, workflowJibe2.mPhoneId, workflowJibe2.getRcsState())) {
                            WorkflowJibe workflowJibe3 = WorkflowJibe.this;
                            workflowJibe3.mIidToken = workflowJibe3.getIidToken();
                            WorkflowJibe workflowJibe4 = WorkflowJibe.this;
                            if (workflowJibe4.isIidTokenInvalid(workflowJibe4.mIidToken)) {
                                WorkflowJibe workflowJibe5 = WorkflowJibe.this;
                                workflowJibe5.mIidToken = null;
                                return workflowJibe5.getNextWorkflow(8);
                            }
                            WorkflowJibe.this.mIidTokenRetryLimit = 0;
                        }
                        WorkflowBase.Workflow run = super.run();
                        if (!(run instanceof WorkflowBase.FetchHttps)) {
                            WorkflowJibe.this.mIidToken = null;
                        }
                        WorkflowJibe workflowJibe6 = WorkflowJibe.this;
                        workflowJibe6.mHttpResponse = workflowJibe6.mSharedInfo.getHttpResponse().getStatusCode();
                        return run;
                    }

                    /* access modifiers changed from: protected */
                    public void setHttpHeader() {
                        if (WorkflowJibe.this.mMno == Mno.SPRINT) {
                            String str = WorkflowJibe.LOG_TAG;
                            Log.i(str, "mIsEnrichedHeaderFailed: " + WorkflowJibe.this.mIsEnrichedHeaderFailed);
                            WorkflowJibe workflowJibe = WorkflowJibe.this;
                            if (!workflowJibe.mIsEnrichedHeaderFailed) {
                                workflowJibe.mSharedInfo.setHttpSPR();
                            } else {
                                workflowJibe.mSharedInfo.resetHttpSPR();
                            }
                        } else {
                            super.setHttpHeader();
                        }
                    }
                };
            case 3:
                return new WorkflowBase.FetchHttps() {
                    public WorkflowBase.Workflow run() throws Exception {
                        String str;
                        String str2 = WorkflowJibe.LOG_TAG;
                        IMSLog.i(str2, WorkflowJibe.this.mPhoneId, "FetchHttps:");
                        WorkflowJibe workflowJibe = WorkflowJibe.this;
                        if (ConfigUtil.getGmsVersion(workflowJibe.mContext, workflowJibe.mPhoneId) == 0) {
                            IMSLog.i(str2, WorkflowJibe.this.mPhoneId, "GmsVersion is invalid.");
                            WorkflowJibe.this.retryConfigAfterTime();
                            return WorkflowJibe.this.getNextWorkflow(8);
                        }
                        WorkflowJibe workflowJibe2 = WorkflowJibe.this;
                        if (ConfigUtil.isIidTokenNeeded(workflowJibe2.mContext, workflowJibe2.mPhoneId, workflowJibe2.getRcsState()) && ((str = WorkflowJibe.this.mIidToken) == null || "".equals(str))) {
                            WorkflowJibe workflowJibe3 = WorkflowJibe.this;
                            workflowJibe3.mIidToken = workflowJibe3.getIidToken();
                            WorkflowJibe workflowJibe4 = WorkflowJibe.this;
                            if (workflowJibe4.isIidTokenInvalid(workflowJibe4.mIidToken)) {
                                WorkflowJibe workflowJibe5 = WorkflowJibe.this;
                                workflowJibe5.mIidToken = null;
                                return workflowJibe5.getNextWorkflow(8);
                            }
                            WorkflowJibe.this.mIidTokenRetryLimit = 0;
                        }
                        return super.run();
                    }

                    /* access modifiers changed from: protected */
                    /* JADX WARNING: Removed duplicated region for block: B:50:0x0257  */
                    /* JADX WARNING: Removed duplicated region for block: B:55:0x028d  */
                    /* JADX WARNING: Removed duplicated region for block: B:58:? A[RETURN, SYNTHETIC] */
                    /* Code decompiled incorrectly, please refer to instructions dump. */
                    public void setHttps() {
                        /*
                            r7 = this;
                            com.sec.internal.ims.config.workflow.WorkflowJibe r0 = com.sec.internal.ims.config.workflow.WorkflowJibe.this
                            com.sec.internal.constants.Mno r1 = r0.mMno
                            com.sec.internal.constants.Mno r2 = com.sec.internal.constants.Mno.SPRINT
                            r3 = 511(0x1ff, float:7.16E-43)
                            r4 = 1
                            r5 = 0
                            if (r1 != r2) goto L_0x0012
                            com.sec.internal.ims.config.SharedInfo r0 = r0.mSharedInfo
                            r0.setHttpsSPR()
                            goto L_0x0057
                        L_0x0012:
                            com.sec.internal.ims.config.SharedInfo r0 = r0.mSharedInfo
                            r0.setHttpsDefault()
                            com.sec.internal.ims.config.workflow.WorkflowJibe r0 = com.sec.internal.ims.config.workflow.WorkflowJibe.this
                            com.sec.internal.ims.config.SharedInfo r0 = r0.mSharedInfo
                            com.sec.internal.interfaces.ims.config.IHttpAdapter$Response r0 = r0.getHttpResponse()
                            com.sec.internal.ims.config.workflow.WorkflowJibe r1 = com.sec.internal.ims.config.workflow.WorkflowJibe.this
                            com.sec.internal.constants.Mno r1 = r1.mMno
                            boolean r1 = r1.isOrange()
                            if (r1 == 0) goto L_0x004e
                            com.sec.internal.ims.config.workflow.WorkflowJibe r1 = com.sec.internal.ims.config.workflow.WorkflowJibe.this
                            int r1 = r1.mPhoneId
                            boolean r1 = com.sec.internal.ims.util.ConfigUtil.isRcsPreConsent(r1)
                            if (r1 != 0) goto L_0x004e
                            if (r0 == 0) goto L_0x004e
                            int r1 = r0.getStatusCode()
                            if (r1 != r3) goto L_0x004e
                            java.util.Map r1 = r0.getHeader()
                            if (r1 == 0) goto L_0x004e
                            java.util.Map r1 = r0.getHeader()
                            java.lang.String r2 = "Set-Cookie"
                            boolean r1 = r1.containsKey(r2)
                            if (r1 == 0) goto L_0x004e
                            r5 = r4
                        L_0x004e:
                            if (r5 == 0) goto L_0x0057
                            com.sec.internal.ims.config.workflow.WorkflowJibe r1 = com.sec.internal.ims.config.workflow.WorkflowJibe.this
                            com.sec.internal.ims.config.workflow.WorkflowCookieHandler r1 = r1.mCookieHandler
                            r1.handleCookie(r0)
                        L_0x0057:
                            com.sec.internal.ims.config.workflow.WorkflowJibe r0 = com.sec.internal.ims.config.workflow.WorkflowJibe.this
                            int r0 = r0.mPhoneId
                            int r0 = com.sec.internal.helper.SimUtil.getSubId(r0)
                            com.sec.internal.ims.config.workflow.WorkflowJibe r1 = com.sec.internal.ims.config.workflow.WorkflowJibe.this
                            com.sec.internal.ims.config.SharedInfo r2 = r1.mSharedInfo
                            com.sec.internal.interfaces.ims.config.ITelephonyAdapter r1 = r1.mTelephonyAdapter
                            java.lang.String r0 = r1.getSubscriberId(r0)
                            java.lang.String r1 = "IMSI"
                            r2.addHttpParam(r1, r0)
                            com.sec.internal.ims.config.workflow.WorkflowJibe r0 = com.sec.internal.ims.config.workflow.WorkflowJibe.this
                            com.sec.internal.ims.config.SharedInfo r1 = r0.mSharedInfo
                            com.sec.internal.interfaces.ims.config.ITelephonyAdapter r2 = r0.mTelephonyAdapter
                            int r0 = r0.mPhoneId
                            java.lang.String r0 = r2.getDeviceId(r0)
                            java.lang.String r2 = "IMEI"
                            r1.addHttpParam(r2, r0)
                            com.sec.internal.ims.config.workflow.WorkflowJibe r0 = com.sec.internal.ims.config.workflow.WorkflowJibe.this
                            com.sec.internal.ims.config.SharedInfo r1 = r0.mSharedInfo
                            android.content.Context r2 = r0.mContext
                            int r0 = r0.mPhoneId
                            boolean r0 = com.sec.internal.ims.util.ConfigUtil.isSecDmaPackageInuse(r2, r0)
                            if (r0 == 0) goto L_0x0090
                            java.lang.String r0 = "1"
                            goto L_0x0092
                        L_0x0090:
                            java.lang.String r0 = "2"
                        L_0x0092:
                            java.lang.String r2 = "default_sms_app"
                            r1.addHttpParam(r2, r0)
                            com.sec.internal.ims.config.workflow.WorkflowJibe r0 = com.sec.internal.ims.config.workflow.WorkflowJibe.this
                            boolean r1 = r0.mMobileNetwork
                            if (r1 != 0) goto L_0x00a1
                            boolean r1 = r0.mIsMobileConfigNeeded
                            if (r1 == 0) goto L_0x00a5
                        L_0x00a1:
                            int r1 = r0.mHttpResponse
                            if (r1 != r3) goto L_0x0136
                        L_0x00a5:
                            if (r5 != 0) goto L_0x00f3
                            com.sec.internal.interfaces.ims.config.ITelephonyAdapter r0 = r0.mTelephonyAdapter
                            java.lang.String r0 = r0.getMsisdn()
                            boolean r1 = android.text.TextUtils.isEmpty(r0)
                            java.lang.String r2 = "msisdn"
                            if (r1 != 0) goto L_0x00d0
                            boolean r1 = com.sec.internal.ims.util.ConfigUtil.isValidMsisdn(r0)
                            if (r1 == 0) goto L_0x00d0
                            java.lang.String r1 = com.sec.internal.ims.config.workflow.WorkflowJibe.LOG_TAG
                            java.lang.String r3 = "use msisdn from telephony"
                            android.util.Log.i(r1, r3)
                            com.sec.internal.ims.config.workflow.WorkflowJibe r1 = com.sec.internal.ims.config.workflow.WorkflowJibe.this
                            com.sec.internal.ims.config.SharedInfo r3 = r1.mSharedInfo
                            com.sec.internal.ims.config.workflow.WorkflowParamHandler r1 = r1.mParamHandler
                            java.lang.String r0 = r1.encodeRFC3986(r0)
                            r3.addHttpParam(r2, r0)
                        L_0x00d0:
                            com.sec.internal.ims.config.workflow.WorkflowJibe r0 = com.sec.internal.ims.config.workflow.WorkflowJibe.this
                            com.sec.internal.ims.config.SharedInfo r0 = r0.mSharedInfo
                            java.lang.String r0 = r0.getUserMsisdn()
                            boolean r1 = android.text.TextUtils.isEmpty(r0)
                            if (r1 != 0) goto L_0x00f3
                            java.lang.String r1 = com.sec.internal.ims.config.workflow.WorkflowJibe.LOG_TAG
                            java.lang.String r3 = "use msisdn from sharedInfo"
                            android.util.Log.i(r1, r3)
                            com.sec.internal.ims.config.workflow.WorkflowJibe r1 = com.sec.internal.ims.config.workflow.WorkflowJibe.this
                            com.sec.internal.ims.config.SharedInfo r3 = r1.mSharedInfo
                            com.sec.internal.ims.config.workflow.WorkflowParamHandler r1 = r1.mParamHandler
                            java.lang.String r0 = r1.encodeRFC3986(r0)
                            r3.addHttpParam(r2, r0)
                        L_0x00f3:
                            com.sec.internal.ims.config.workflow.WorkflowJibe r0 = com.sec.internal.ims.config.workflow.WorkflowJibe.this
                            com.sec.internal.ims.config.SharedInfo r1 = r0.mSharedInfo
                            int r0 = r0.mPhoneId
                            java.lang.String r0 = com.sec.internal.ims.util.ConfigUtil.getSmsPort(r0)
                            java.lang.String r2 = "SMS_port"
                            r1.addHttpParam(r2, r0)
                            java.lang.StringBuilder r0 = new java.lang.StringBuilder
                            r0.<init>()
                            com.sec.internal.ims.config.workflow.WorkflowJibe r1 = com.sec.internal.ims.config.workflow.WorkflowJibe.this
                            int r1 = r1.mPhoneId
                            r0.append(r1)
                            java.lang.String r1 = ",OSP:"
                            r0.append(r1)
                            com.sec.internal.ims.config.workflow.WorkflowJibe r1 = com.sec.internal.ims.config.workflow.WorkflowJibe.this
                            int r1 = r1.mPhoneId
                            java.lang.String r1 = com.sec.internal.ims.util.ConfigUtil.getSmsPort(r1)
                            r0.append(r1)
                            java.lang.String r0 = r0.toString()
                            r1 = 318898945(0x13020301, float:1.6409788E-27)
                            com.sec.internal.log.IMSLog.c(r1, r0)
                            com.sec.internal.ims.config.workflow.WorkflowJibe r0 = com.sec.internal.ims.config.workflow.WorkflowJibe.this
                            com.sec.internal.ims.config.SharedInfo r1 = r0.mSharedInfo
                            java.lang.String r2 = "token"
                            java.lang.String r0 = r0.getToken()
                            r1.addHttpParam(r2, r0)
                        L_0x0136:
                            com.sec.internal.ims.config.workflow.WorkflowJibe r0 = com.sec.internal.ims.config.workflow.WorkflowJibe.this
                            com.sec.internal.ims.config.SharedInfo r0 = r0.mSharedInfo
                            java.lang.String r1 = "terminal_vendor"
                            java.lang.String r2 = "SEC"
                            r0.addHttpParam(r1, r2)
                            com.sec.internal.ims.config.workflow.WorkflowJibe r0 = com.sec.internal.ims.config.workflow.WorkflowJibe.this
                            com.sec.internal.ims.config.SharedInfo r0 = r0.mSharedInfo
                            java.lang.String r1 = "terminal_model"
                            java.lang.String r3 = com.sec.internal.constants.ims.config.ConfigConstants.PVALUE.TERMINAL_MODEL
                            r0.addHttpParam(r1, r3)
                            com.sec.internal.ims.config.workflow.WorkflowJibe r0 = com.sec.internal.ims.config.workflow.WorkflowJibe.this
                            com.sec.internal.ims.config.SharedInfo r1 = r0.mSharedInfo
                            com.sec.internal.ims.config.workflow.WorkflowParamHandler r3 = r0.mParamHandler
                            int r0 = r0.mPhoneId
                            java.lang.String r0 = com.sec.internal.ims.util.ConfigUtil.getModelName(r0)
                            java.lang.String r5 = com.sec.internal.constants.ims.config.ConfigConstants.PVALUE.TERMINAL_SW_VERSION
                            r6 = 8
                            java.lang.String r0 = r3.getModelInfoFromBuildVersion(r0, r5, r6, r4)
                            java.lang.String r3 = "terminal_sw_version"
                            r1.addHttpParam(r3, r0)
                            com.sec.internal.ims.config.workflow.WorkflowJibe r0 = com.sec.internal.ims.config.workflow.WorkflowJibe.this
                            com.sec.internal.ims.config.SharedInfo r0 = r0.mSharedInfo
                            java.lang.String r1 = "client_vendor"
                            r0.addHttpParam(r1, r2)
                            com.sec.internal.ims.config.workflow.WorkflowJibe r0 = com.sec.internal.ims.config.workflow.WorkflowJibe.this
                            com.sec.internal.ims.config.SharedInfo r0 = r0.mSharedInfo
                            java.lang.StringBuilder r1 = new java.lang.StringBuilder
                            r1.<init>()
                            com.sec.internal.ims.config.workflow.WorkflowJibe r2 = com.sec.internal.ims.config.workflow.WorkflowJibe.this
                            java.lang.String r2 = r2.mClientPlatform
                            r1.append(r2)
                            com.sec.internal.ims.config.workflow.WorkflowJibe r2 = com.sec.internal.ims.config.workflow.WorkflowJibe.this
                            java.lang.String r2 = r2.mClientVersion
                            r1.append(r2)
                            java.lang.String r1 = r1.toString()
                            java.lang.String r2 = "client_version"
                            r0.addHttpParam(r2, r1)
                            com.sec.internal.ims.config.workflow.WorkflowJibe r0 = com.sec.internal.ims.config.workflow.WorkflowJibe.this
                            com.sec.internal.ims.config.SharedInfo r1 = r0.mSharedInfo
                            java.lang.String r2 = "rcs_version"
                            java.lang.String r0 = r0.mRcsVersion
                            r1.addHttpParam(r2, r0)
                            com.sec.internal.ims.config.workflow.WorkflowJibe r0 = com.sec.internal.ims.config.workflow.WorkflowJibe.this
                            com.sec.internal.constants.Mno r0 = r0.mMno
                            boolean r0 = r0.isOrange()
                            java.lang.String r1 = "rcs_profile"
                            if (r0 == 0) goto L_0x01db
                            com.sec.internal.ims.config.workflow.WorkflowJibe r0 = com.sec.internal.ims.config.workflow.WorkflowJibe.this
                            android.content.Context r2 = r0.mContext
                            int r0 = r0.mPhoneId
                            boolean r0 = com.sec.internal.ims.util.ConfigUtil.getGlobalGcEnabled(r2, r0)
                            if (r0 != 0) goto L_0x01db
                            java.lang.String r0 = com.sec.internal.ims.config.workflow.WorkflowJibe.LOG_TAG
                            java.lang.StringBuilder r2 = new java.lang.StringBuilder
                            r2.<init>()
                            java.lang.String r3 = "rcsProfile read and used for Orange: "
                            r2.append(r3)
                            com.sec.internal.ims.config.workflow.WorkflowJibe r3 = com.sec.internal.ims.config.workflow.WorkflowJibe.this
                            java.lang.String r3 = r3.mRcsUPProfile
                            r2.append(r3)
                            java.lang.String r2 = r2.toString()
                            android.util.Log.i(r0, r2)
                            com.sec.internal.ims.config.workflow.WorkflowJibe r0 = com.sec.internal.ims.config.workflow.WorkflowJibe.this
                            com.sec.internal.ims.config.SharedInfo r2 = r0.mSharedInfo
                            java.lang.String r0 = r0.mRcsUPProfile
                            r2.addHttpParam(r1, r0)
                            goto L_0x01ec
                        L_0x01db:
                            java.lang.String r0 = com.sec.internal.ims.config.workflow.WorkflowJibe.LOG_TAG
                            java.lang.String r2 = "set rcs_profile to UP_T-b1 for A2P"
                            android.util.Log.i(r0, r2)
                            com.sec.internal.ims.config.workflow.WorkflowJibe r0 = com.sec.internal.ims.config.workflow.WorkflowJibe.this
                            com.sec.internal.ims.config.SharedInfo r0 = r0.mSharedInfo
                            java.lang.String r2 = "UP_T-b1"
                            r0.addHttpParam(r1, r2)
                        L_0x01ec:
                            com.sec.internal.ims.config.workflow.WorkflowJibe r0 = com.sec.internal.ims.config.workflow.WorkflowJibe.this
                            com.sec.internal.ims.config.SharedInfo r1 = r0.mSharedInfo
                            java.lang.String r2 = "provisioning_version"
                            java.lang.String r0 = r0.mRcsProvisioningVersion
                            r1.addHttpParam(r2, r0)
                            com.sec.internal.ims.config.workflow.WorkflowJibe r0 = com.sec.internal.ims.config.workflow.WorkflowJibe.this
                            java.lang.String r1 = r0.convertRcsStateWithSpecificParam()
                            r0.setRcsState(r1)
                            com.sec.internal.ims.config.workflow.WorkflowJibe r0 = com.sec.internal.ims.config.workflow.WorkflowJibe.this
                            com.sec.internal.ims.config.SharedInfo r1 = r0.mSharedInfo
                            java.lang.String r2 = "rcs_state"
                            java.lang.String r0 = r0.getRcsState()
                            r1.addHttpParam(r2, r0)
                            com.sec.internal.ims.config.workflow.WorkflowJibe r0 = com.sec.internal.ims.config.workflow.WorkflowJibe.this
                            com.sec.internal.ims.config.SharedInfo r1 = r0.mSharedInfo
                            int r0 = r0.getVersion()
                            java.lang.String r0 = java.lang.String.valueOf(r0)
                            java.lang.String r2 = "vers"
                            r1.addHttpParam(r2, r0)
                            com.sec.internal.ims.config.workflow.WorkflowJibe r0 = com.sec.internal.ims.config.workflow.WorkflowJibe.this
                            boolean r0 = r0.mStartForce
                            if (r0 == 0) goto L_0x024d
                            com.sec.internal.ims.config.workflow.WorkflowBase$OpMode r0 = com.sec.internal.ims.config.workflow.WorkflowBase.OpMode.DISABLE_RCS_BY_USER
                            int r0 = r0.value()
                            java.lang.String r0 = java.lang.String.valueOf(r0)
                            com.sec.internal.ims.config.workflow.WorkflowJibe r1 = com.sec.internal.ims.config.workflow.WorkflowJibe.this
                            java.lang.String r1 = r1.getRcsState()
                            boolean r0 = r0.equals(r1)
                            if (r0 != 0) goto L_0x024d
                            java.lang.String r0 = com.sec.internal.ims.config.workflow.WorkflowJibe.LOG_TAG
                            java.lang.String r1 = "mStartForce: true, vers: 0"
                            android.util.Log.i(r0, r1)
                            com.sec.internal.ims.config.workflow.WorkflowJibe r0 = com.sec.internal.ims.config.workflow.WorkflowJibe.this
                            com.sec.internal.ims.config.SharedInfo r0 = r0.mSharedInfo
                            java.lang.String r1 = "0"
                            r0.addHttpParam(r2, r1)
                        L_0x024d:
                            com.sec.internal.ims.config.workflow.WorkflowJibe r0 = com.sec.internal.ims.config.workflow.WorkflowJibe.this
                            com.sec.internal.ims.config.workflow.WorkflowBase$OpMode r0 = r0.getOpMode()
                            com.sec.internal.ims.config.workflow.WorkflowBase$OpMode r1 = com.sec.internal.ims.config.workflow.WorkflowBase.OpMode.DORMANT
                            if (r0 != r1) goto L_0x027f
                            java.lang.String r0 = com.sec.internal.ims.config.workflow.WorkflowJibe.LOG_TAG
                            java.lang.StringBuilder r1 = new java.lang.StringBuilder
                            r1.<init>()
                            java.lang.String r3 = "use backup version in case of dormant, vers: "
                            r1.append(r3)
                            com.sec.internal.ims.config.workflow.WorkflowJibe r3 = com.sec.internal.ims.config.workflow.WorkflowJibe.this
                            java.lang.String r3 = r3.getVersionBackup()
                            r1.append(r3)
                            java.lang.String r1 = r1.toString()
                            android.util.Log.i(r0, r1)
                            com.sec.internal.ims.config.workflow.WorkflowJibe r0 = com.sec.internal.ims.config.workflow.WorkflowJibe.this
                            com.sec.internal.ims.config.SharedInfo r1 = r0.mSharedInfo
                            java.lang.String r0 = r0.getVersionBackup()
                            r1.addHttpParam(r2, r0)
                        L_0x027f:
                            com.sec.internal.ims.config.workflow.WorkflowJibe r0 = com.sec.internal.ims.config.workflow.WorkflowJibe.this
                            java.lang.String r0 = r0.mIidToken
                            if (r0 == 0) goto L_0x02a7
                            java.lang.String r1 = ""
                            boolean r0 = r1.equals(r0)
                            if (r0 != 0) goto L_0x02a7
                            java.util.ArrayList r0 = new java.util.ArrayList
                            r0.<init>(r4)
                            com.sec.internal.ims.config.workflow.WorkflowJibe r1 = com.sec.internal.ims.config.workflow.WorkflowJibe.this
                            java.lang.String r1 = r1.mIidToken
                            r0.add(r1)
                            com.sec.internal.ims.config.workflow.WorkflowJibe r1 = com.sec.internal.ims.config.workflow.WorkflowJibe.this
                            com.sec.internal.ims.config.SharedInfo r1 = r1.mSharedInfo
                            java.lang.String r2 = "gmscore_instance_id_token"
                            r1.addHttpHeader(r2, r0)
                            com.sec.internal.ims.config.workflow.WorkflowJibe r7 = com.sec.internal.ims.config.workflow.WorkflowJibe.this
                            r0 = 0
                            r7.mIidToken = r0
                        L_0x02a7:
                            return
                        */
                        throw new UnsupportedOperationException("Method not decompiled: com.sec.internal.ims.config.workflow.WorkflowJibe.AnonymousClass4.setHttps():void");
                    }
                };
            case 4:
                return new WorkflowBase.Authorize() {
                    public WorkflowBase.Workflow run() throws Exception {
                        IMSLog.i(WorkflowJibe.LOG_TAG, WorkflowJibe.this.mPhoneId, "Authorize:");
                        WorkflowBase.Workflow run = super.run();
                        if (run instanceof WorkflowBase.Finish) {
                            WorkflowJibe.this.mSharedInfo.getHttpResponse().setStatusCode(700);
                        }
                        return run;
                    }

                    /* access modifiers changed from: protected */
                    /* JADX WARNING: Code restructure failed: missing block: B:2:0x0062, code lost:
                        r1 = r5.this$0;
                     */
                    /* JADX WARNING: Code restructure failed: missing block: B:7:0x007a, code lost:
                        r5 = r5.this$0;
                     */
                    /* Code decompiled incorrectly, please refer to instructions dump. */
                    public java.lang.String getOtp() {
                        /*
                            r5 = this;
                            com.sec.internal.ims.config.workflow.WorkflowJibe r0 = com.sec.internal.ims.config.workflow.WorkflowJibe.this
                            int r0 = r0.mPhoneId
                            java.lang.String r0 = com.sec.internal.ims.util.ConfigUtil.getSmsType(r0)
                            java.lang.String r1 = com.sec.internal.ims.config.workflow.WorkflowJibe.LOG_TAG
                            com.sec.internal.ims.config.workflow.WorkflowJibe r2 = com.sec.internal.ims.config.workflow.WorkflowJibe.this
                            int r2 = r2.mPhoneId
                            java.lang.StringBuilder r3 = new java.lang.StringBuilder
                            r3.<init>()
                            java.lang.String r4 = "otpSmsType: "
                            r3.append(r4)
                            r3.append(r0)
                            java.lang.String r4 = " mAuthTryCount: "
                            r3.append(r4)
                            com.sec.internal.ims.config.workflow.WorkflowJibe r4 = com.sec.internal.ims.config.workflow.WorkflowJibe.this
                            int r4 = r4.mAuthTryCount
                            r3.append(r4)
                            java.lang.String r4 = " mAuthHiddenTryCount: "
                            r3.append(r4)
                            com.sec.internal.ims.config.workflow.WorkflowJibe r4 = com.sec.internal.ims.config.workflow.WorkflowJibe.this
                            int r4 = r4.mAuthHiddenTryCount
                            r3.append(r4)
                            java.lang.String r3 = r3.toString()
                            com.sec.internal.log.IMSLog.i(r1, r2, r3)
                            java.lang.StringBuilder r1 = new java.lang.StringBuilder
                            r1.<init>()
                            com.sec.internal.ims.config.workflow.WorkflowJibe r2 = com.sec.internal.ims.config.workflow.WorkflowJibe.this
                            int r2 = r2.mPhoneId
                            r1.append(r2)
                            java.lang.String r2 = ",OST:"
                            r1.append(r2)
                            r1.append(r0)
                            java.lang.String r1 = r1.toString()
                            r2 = 318898946(0x13020302, float:1.640979E-27)
                            com.sec.internal.log.IMSLog.c(r2, r1)
                            java.lang.String r1 = "text"
                            boolean r1 = r1.equals(r0)
                            r2 = 1
                            if (r1 == 0) goto L_0x0072
                            com.sec.internal.ims.config.workflow.WorkflowJibe r1 = com.sec.internal.ims.config.workflow.WorkflowJibe.this
                            int r3 = r1.mAuthTryCount
                            if (r3 >= r2) goto L_0x0072
                            int r3 = r3 + r2
                            r1.mAuthTryCount = r3
                            com.sec.internal.interfaces.ims.config.ITelephonyAdapter r5 = r1.mTelephonyAdapter
                            java.lang.String r5 = r5.getOtp()
                            goto L_0x008c
                        L_0x0072:
                            java.lang.String r1 = "binary"
                            boolean r0 = r1.equals(r0)
                            if (r0 == 0) goto L_0x008b
                            com.sec.internal.ims.config.workflow.WorkflowJibe r5 = com.sec.internal.ims.config.workflow.WorkflowJibe.this
                            int r0 = r5.mAuthHiddenTryCount
                            r1 = 3
                            if (r0 >= r1) goto L_0x008b
                            int r0 = r0 + r2
                            r5.mAuthHiddenTryCount = r0
                            com.sec.internal.interfaces.ims.config.ITelephonyAdapter r5 = r5.mTelephonyAdapter
                            java.lang.String r5 = r5.getPortOtp()
                            goto L_0x008c
                        L_0x008b:
                            r5 = 0
                        L_0x008c:
                            return r5
                        */
                        throw new UnsupportedOperationException("Method not decompiled: com.sec.internal.ims.config.workflow.WorkflowJibe.AnonymousClass6.getOtp():java.lang.String");
                    }
                };
            case 5:
                return new WorkflowBase.FetchOtp() {
                    public WorkflowBase.Workflow run() throws Exception {
                        IMSLog.i(WorkflowJibe.LOG_TAG, WorkflowJibe.this.mPhoneId, "FetchOtp:");
                        return super.run();
                    }

                    /* access modifiers changed from: protected */
                    public void setHttp() {
                        super.setHttp();
                        if (WorkflowJibe.this.mMno.isEmeasewaoce()) {
                            WorkflowJibe workflowJibe = WorkflowJibe.this;
                            workflowJibe.mCookieHandler.handleCookie(workflowJibe.mSharedInfo.getHttpResponse());
                            return;
                        }
                        SharedInfo sharedInfo = WorkflowJibe.this.mSharedInfo;
                        sharedInfo.addHttpHeader(HttpController.HEADER_COOKIE, sharedInfo.getHttpResponse().getHeader().get(HttpController.HEADER_SET_COOKIE));
                    }
                };
            case 6:
                return new WorkflowBase.Parse() {
                    public WorkflowBase.Workflow run() throws Exception {
                        IMSLog.i(WorkflowJibe.LOG_TAG, WorkflowJibe.this.mPhoneId, "Parse:");
                        return super.run();
                    }
                };
            case 7:
                return new WorkflowBase.Store() {
                    public WorkflowBase.Workflow run() throws Exception {
                        String str = WorkflowJibe.LOG_TAG;
                        IMSLog.i(str, WorkflowJibe.this.mPhoneId, "Store:");
                        Map<String, String> parsedXml = WorkflowJibe.this.mSharedInfo.getParsedXml();
                        Log.i(str, String.format(Locale.US, "Store: version [%d] => [%d]", new Object[]{Integer.valueOf(WorkflowJibe.this.getVersion()), Integer.valueOf(WorkflowJibe.this.getVersion(parsedXml))}));
                        WorkflowBase.OpMode rcsDisabledState = WorkflowJibe.this.getRcsDisabledState(parsedXml);
                        if (WorkflowJibe.this.isValidRcsDisabledState(rcsDisabledState)) {
                            WorkflowJibe.this.setOpMode(rcsDisabledState, parsedXml);
                            return WorkflowJibe.this.getNextWorkflow(8);
                        }
                        WorkflowParamHandler.UserAccept userAccept = WorkflowParamHandler.UserAccept.ACCEPT;
                        WorkflowParamHandler.UserAccept userAcceptDetailed = (ConfigUtil.isRcsPreConsent(WorkflowJibe.this.mPhoneId) || !WorkflowJibe.this.mMno.isEmeasewaoce()) ? userAccept : WorkflowJibe.this.mParamHandler.getUserAcceptDetailed(parsedXml);
                        int i = 1;
                        if (userAcceptDetailed == WorkflowParamHandler.UserAccept.NON_DEFAULT_MSG_APP) {
                            WorkflowJibe.this.setOpMode(WorkflowBase.OpMode.DISABLE_RCS_BY_USER, (Map<String, String>) null);
                        } else {
                            WorkflowJibe.this.mParamHandler.setOpModeWithUserAccept(userAcceptDetailed == userAccept, parsedXml, WorkflowBase.OpMode.DISABLE);
                        }
                        if (WorkflowJibe.this.getOpMode() == WorkflowBase.OpMode.ACTIVE || (WorkflowJibe.this.getOpMode() == WorkflowBase.OpMode.DISABLE_TEMPORARY && WorkflowJibe.this.getValidity() > 0)) {
                            WorkflowJibe workflowJibe = WorkflowJibe.this;
                            workflowJibe.setValidityTimer(workflowJibe.getValidity());
                        }
                        WorkflowJibe.this.mMsisdnHandler.setMsisdnSkipCount(0);
                        WorkflowJibe workflowJibe2 = WorkflowJibe.this;
                        if (userAcceptDetailed != userAccept) {
                            i = 0;
                        }
                        workflowJibe2.setTcUserAccept(i);
                        return WorkflowJibe.this.getNextWorkflow(8);
                    }
                };
            case 8:
                return new WorkflowBase.Finish() {
                    public WorkflowBase.Workflow run() {
                        IMSLog.i(WorkflowJibe.LOG_TAG, WorkflowJibe.this.mPhoneId, "Finish:");
                        return null;
                    }
                };
            default:
                String str = LOG_TAG;
                Log.i(str, "getNextWorkflow: Unexpected type [" + i + "] !!!");
                return null;
        }
    }
}
