package com.sec.internal.ims.config.workflow;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.database.sqlite.SQLiteFullException;
import android.os.Message;
import android.text.TextUtils;
import android.util.Log;
import com.sec.internal.constants.ims.DiagnosisConstants;
import com.sec.internal.constants.ims.ImsConstants;
import com.sec.internal.constants.ims.config.ConfigConstants;
import com.sec.internal.constants.ims.settings.GlobalSettingsConstants;
import com.sec.internal.helper.DmConfigHelper;
import com.sec.internal.helper.ImsSharedPrefHelper;
import com.sec.internal.helper.httpclient.HttpController;
import com.sec.internal.ims.config.SharedInfo;
import com.sec.internal.ims.config.exception.EmptyBodyAndCookieException;
import com.sec.internal.ims.config.exception.InvalidHeaderException;
import com.sec.internal.ims.config.exception.NoInitialDataException;
import com.sec.internal.ims.config.exception.UnknownStatusException;
import com.sec.internal.ims.config.workflow.WorkflowBase;
import com.sec.internal.ims.core.handler.secims.imsCommonStruc.Id;
import com.sec.internal.ims.core.sim.SimManagerFactory;
import com.sec.internal.ims.registry.ImsRegistry;
import com.sec.internal.ims.servicemodules.ss.UtStateMachine;
import com.sec.internal.ims.settings.DeviceConfigManager;
import com.sec.internal.ims.util.ConfigUtil;
import com.sec.internal.imscr.LogClass;
import com.sec.internal.interfaces.ims.config.IConfigModule;
import com.sec.internal.log.IMSLog;
import java.util.Arrays;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Optional;

public class WorkflowSec extends WorkflowUpBase {
    static final int INTERNALERR_RETRY_MAX_COUNT = 1;
    static final int INTERNAL_503_ERR_RETRY_MAX_COUNT = 1;
    static final int INTERNAL_511_ERR_RETRY_MAX_COUNT = 1;
    protected static final String LOG_TAG = WorkflowSec.class.getSimpleName();
    static final String OTP_SMS_BINARY_TYPE = "binary";
    static final String OTP_SMS_TEXT_TYPE = "text";
    static final int OTP_SMS_TIME_OUT = 700;
    static final int RESET_RETRY_MAX_COUNT = 3;
    static final int[] RETRY_INTERVAL = {Id.REQUEST_SIP_DIALOG_SEND_SIP, 3600, 7200, 14400, 28800};
    static final int RETRY_INTERVAL_DAILY = 86400;
    static final int STORAGE_STATE_READY = 1;
    static final List<String> VALID_REJECT_CODES = Arrays.asList(new String[]{"2", DiagnosisConstants.RCSM_ORST_REGI, "6", "8"});
    /* access modifiers changed from: private */
    public int mAuthHiddenTryCount = 0;
    /* access modifiers changed from: private */
    public int mAuthTryCount = 0;
    protected int mHttpResponse = 0;
    BroadcastReceiver mIntentReceiver = new BroadcastReceiver() {
        public void onReceive(Context context, Intent intent) {
            String action = intent.getAction();
            String str = WorkflowSec.LOG_TAG;
            int i = WorkflowSec.this.mPhoneId;
            IMSLog.i(str, i, "onReceive: " + action);
            if (ImsConstants.Intents.INTENT_ACTION_REGIST_REJECT.equals(action) || ImsConstants.Intents.INTENT_ACTION_LTE_REJECT.equals(action)) {
                String stringExtra = intent.getStringExtra(ImsConstants.Intents.EXTRA_CAUSE_KEY);
                IMSLog.c(LogClass.WFS_LTE_REJECT, WorkflowSec.this.mPhoneId + ", LTE reject by cause " + stringExtra);
                if (WorkflowSec.VALID_REJECT_CODES.contains(stringExtra)) {
                    WorkflowSec workflowSec = WorkflowSec.this;
                    workflowSec.mResetRetryCount = 0;
                    workflowSec.mModule.setAcsTryReason(workflowSec.mPhoneId, DiagnosisConstants.RCSA_ATRE.REJECT_LTE);
                    if (WorkflowSec.this.mStorage.getState() != 1) {
                        Log.i(str, "StorageAdapter's state is idle");
                        WorkflowSec.this.removeMessages(8);
                        WorkflowSec workflowSec2 = WorkflowSec.this;
                        workflowSec2.sendMessageDelayed(workflowSec2.obtainMessage(8), 10000);
                        return;
                    }
                    WorkflowSec.this.resetAutoConfigInfo(Boolean.TRUE);
                }
            }
        }
    };
    int mResetRetryCount = 0;
    int mTrialCount;

    /* JADX WARNING: Illegal instructions before constructor call */
    /* Code decompiled incorrectly, please refer to instructions dump. */
    public WorkflowSec(android.os.Looper r13, android.content.Context r14, com.sec.internal.interfaces.ims.config.IConfigModule r15, com.sec.internal.constants.Mno r16, int r17) {
        /*
            r12 = this;
            r11 = r12
            r2 = r14
            r3 = r15
            r10 = r17
            com.sec.internal.ims.config.adapters.TelephonyAdapterPrimaryDeviceSec r5 = new com.sec.internal.ims.config.adapters.TelephonyAdapterPrimaryDeviceSec
            r5.<init>(r14, r15, r10)
            com.sec.internal.ims.config.adapters.StorageAdapter r6 = new com.sec.internal.ims.config.adapters.StorageAdapter
            r6.<init>()
            com.sec.internal.ims.config.adapters.HttpAdapterJibeAndSec r7 = new com.sec.internal.ims.config.adapters.HttpAdapterJibeAndSec
            r7.<init>(r10)
            com.sec.internal.ims.config.adapters.XmlParserAdapter r8 = new com.sec.internal.ims.config.adapters.XmlParserAdapter
            r8.<init>()
            com.sec.internal.ims.config.adapters.DialogAdapter r9 = new com.sec.internal.ims.config.adapters.DialogAdapter
            r9.<init>(r14, r15)
            r0 = r12
            r1 = r13
            r4 = r16
            r0.<init>(r1, r2, r3, r4, r5, r6, r7, r8, r9, r10)
            r0 = 0
            r11.mHttpResponse = r0
            r11.mAuthTryCount = r0
            r11.mAuthHiddenTryCount = r0
            r11.mResetRetryCount = r0
            com.sec.internal.ims.config.workflow.WorkflowSec$1 r0 = new com.sec.internal.ims.config.workflow.WorkflowSec$1
            r0.<init>()
            r11.mIntentReceiver = r0
            android.content.IntentFilter r0 = new android.content.IntentFilter
            r0.<init>()
            java.lang.String r1 = "com.samsung.intent.action.regist_reject"
            r0.addAction(r1)
            java.lang.String r1 = "com.samsung.intent.action.LTE_REJECT"
            r0.addAction(r1)
            android.content.Context r1 = r11.mContext
            android.content.BroadcastReceiver r2 = r11.mIntentReceiver
            r1.registerReceiver(r2, r0)
            return
        */
        throw new UnsupportedOperationException("Method not decompiled: com.sec.internal.ims.config.workflow.WorkflowSec.<init>(android.os.Looper, android.content.Context, com.sec.internal.interfaces.ims.config.IConfigModule, com.sec.internal.constants.Mno, int):void");
    }

    public void handleMessage(Message message) {
        String str = LOG_TAG;
        IMSLog.i(str, this.mPhoneId, "handleMessage: " + message.what);
        addEventLog(str + "handleMessage: " + message.what);
        int i = message.what;
        if (i == 0) {
            this.mStartForce = true;
        } else if (i != 1) {
            if (i != 5) {
                if (i != 8) {
                    super.handleMessage(message);
                    return;
                } else if (checkMobileConnection(this.mConnectivityManager)) {
                    Log.i(str, "ignore auto config reset in mobile connection state");
                    return;
                } else {
                    this.mResetRetryCount++;
                    if (this.mStorage.getState() == 1 || this.mResetRetryCount >= 3) {
                        resetAutoConfigInfo(Boolean.TRUE);
                        return;
                    }
                    Log.i(str, "StorageAdapter's state is idle");
                    removeMessages(8);
                    sendMessageDelayed(obtainMessage(8), 10000);
                    return;
                }
            } else if (DmConfigHelper.getImsSwitchValue(this.mContext, DeviceConfigManager.DEFAULTMSGAPPINUSE, this.mPhoneId) == 1) {
                IMSLog.i(str, this.mPhoneId, "sms default application is changed to samsung");
                resetAutoConfigInfo(Boolean.FALSE);
                this.mModule.setAcsTryReason(this.mPhoneId, DiagnosisConstants.RCSA_ATRE.CHANGE_MSG_APP);
                return;
            } else {
                IMSLog.i(str, this.mPhoneId, "sms default application is changed to non-samsung");
                setOpMode(WorkflowBase.OpMode.DISABLE_RCS_BY_USER, (Map<String, String>) null);
                return;
            }
        }
        if (this.mIsConfigOngoing) {
            Log.i(str, "AutoConfig: ongoing");
            return;
        }
        this.mIsConfigOngoing = true;
        this.mModule.getHandler().removeMessages(3, Integer.valueOf(this.mPhoneId));
        this.mAuthTryCount = 0;
        this.mAuthHiddenTryCount = 0;
        this.mSharedInfo.setInternalErrRetryCount(0);
        this.mSharedInfo.setInternal503ErrRetryCount(0);
        this.mSharedInfo.setInternal511ErrRetryCount(0);
        this.mPowerController.lock();
        this.mOldVersion = getVersion();
        if (needScheduleAutoconfig(this.mPhoneId)) {
            scheduleAutoconfig(this.mOldVersion);
        }
        this.mNewVersion = getVersion();
        IMSLog.i(str, this.mPhoneId, "oldVersion: " + this.mOldVersion + " newVersion: " + this.mNewVersion);
        IMSLog.i(str, this.mPhoneId, "AutoConfig: finish");
        if (this.mOldVersion >= 0 && !isValidRcsDisabledState(getRcsDisabledState())) {
            this.mTelephonyAdapter.notifyAutoConfigurationListener(52, this.mNewVersion > 0);
        }
        setCompleted(true);
        this.mModule.getHandler().sendMessage(obtainMessage(3, this.mOldVersion, this.mNewVersion, Integer.valueOf(this.mPhoneId)));
        this.mStartForce = false;
        this.mPowerController.release();
        this.mIsConfigOngoing = false;
    }

    public void reInitIfNeeded() {
        if (this.mTelephonyAdapter.isReady()) {
            String identityByPhoneId = this.mTelephonyAdapter.getIdentityByPhoneId(this.mPhoneId);
            if (!TextUtils.isEmpty(identityByPhoneId) && !TextUtils.equals(this.mIdentity, identityByPhoneId)) {
                String str = LOG_TAG;
                IMSLog.i(str, this.mPhoneId, "reInitIfNeeded: identity changed, re-init storage");
                IMSLog.c(LogClass.WFS_STORAGE_RE_INIT, this.mPhoneId + ",STOR_RI");
                addEventLog(str + ": reInitIfNeeded: identity changed, re-init storage");
                resetStorage();
            }
        }
    }

    /* access modifiers changed from: package-private */
    public void resetAutoConfigInfo(Boolean bool) {
        IMSLog.i(LOG_TAG, this.mPhoneId, "resetAutoConfigInfo");
        IMSLog.c(LogClass.WFB_RESET_CONFIG, this.mPhoneId + ",resetAutoConfigInfo");
        if (bool.booleanValue()) {
            setVersion(WorkflowBase.OpMode.DISABLE_TEMPORARY.value());
        }
        WorkflowBase.OpMode opMode = WorkflowBase.OpMode.DISABLE_TEMPORARY;
        setRcsState(String.valueOf(opMode.value()));
        setRcsDisabledState("");
        setValidity(opMode.value());
        cancelValidityTimer();
        setNextAutoconfigTime(0);
        IConfigModule iConfigModule = this.mModule;
        if (iConfigModule != null) {
            iConfigModule.getAcsConfig(this.mPhoneId).setAcsCompleteStatus(false);
            this.mModule.getAcsConfig(this.mPhoneId).setForceAcs(true);
        }
    }

    /* access modifiers changed from: package-private */
    public int getTrialInterval() {
        String str = LOG_TAG;
        int i = this.mPhoneId;
        IMSLog.i(str, i, "mTrialCount=" + this.mTrialCount);
        int i2 = this.mTrialCount;
        int[] iArr = RETRY_INTERVAL;
        if (i2 < iArr.length) {
            return iArr[i2];
        }
        Log.i(str, "Trial Count is bigger than retry count. So retry once a day");
        return -1;
    }

    /* access modifiers changed from: package-private */
    public void retryExpBackoff() {
        WorkflowBase.OpMode rcsDisabledState = getRcsDisabledState();
        int version = getVersion();
        if (rcsDisabledState == WorkflowBase.OpMode.DISABLE_TEMPORARY_BY_RCS_DISABLED_STATE || version == WorkflowBase.OpMode.DISABLE_TEMPORARY.value()) {
            String str = LOG_TAG;
            int i = this.mPhoneId;
            IMSLog.i(str, i, "retryExpBackoff: rcsDisabledState: " + convertRcsDisabledStateToValue(rcsDisabledState) + ", Current version: " + version);
            int trialInterval = getTrialInterval();
            if (this.mLastErrorCode == 403) {
                Log.i(str, "mLastErrorCode is 403, No retry");
                cancelValidityTimer();
            } else if (trialInterval < 0) {
                Log.i(str, "retryExpBackoff: Once a day");
                IMSLog.c(LogClass.WFS_RETRY_DAILY, this.mPhoneId + ",RID");
                addEventLog(str + ": retryExpBackoff: Once a day");
                setValidityTimer(86400);
                setNextAutoconfigTimeAfter(86400);
            } else {
                Log.i(str, "retryExpBackoff: interval: " + trialInterval + ImsConstants.RCS_AS.SEC);
                IMSLog.c(LogClass.WFS_RETRY_DAILY, this.mPhoneId + ",RBOI:" + trialInterval);
                addEventLog(str + ": retryExpBackoff: interval: " + trialInterval + ImsConstants.RCS_AS.SEC);
                setValidityTimer(trialInterval);
                setNextAutoconfigTimeAfter(trialInterval);
                this.mTrialCount = this.mTrialCount + 1;
            }
        }
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
                Log.i(LOG_TAG, "UnknownStatusException: " + e2.getMessage());
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
                Log.i(LOG_TAG, "SQLiteFullException occur:" + e3.getMessage());
                workflow = getNextWorkflow(8);
                e3.printStackTrace();
                nextWorkflow = workflow;
                i--;
            } catch (Exception e4) {
                if (e4.getMessage() != null) {
                    Log.i(LOG_TAG, "unknown exception occur:" + e4.getMessage());
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
        if (ImsRegistry.getBoolean(this.mPhoneId, GlobalSettingsConstants.RCS.RCS_SUPPORT_EXPONENTIAL_RETRY_ACS, false)) {
            retryExpBackoff();
        }
    }

    /* access modifiers changed from: protected */
    public WorkflowBase.Workflow handleResponse(WorkflowBase.Workflow workflow, int i) throws InvalidHeaderException, UnknownStatusException {
        String str = LOG_TAG;
        int i2 = this.mPhoneId;
        IMSLog.i(str, i2, "handleResponse: " + i);
        addEventLog(str + "handleResponse: " + i);
        this.mLastErrorCode = i;
        if (i == 511) {
            if (workflow instanceof WorkflowBase.FetchHttp) {
                return getNextWorkflow(3);
            }
            setToken("", DiagnosisConstants.RCSA_TDRE.INVALID_TOKEN);
            removeValidToken();
            int internal511ErrRetryCount = this.mSharedInfo.getInternal511ErrRetryCount() + 1;
            if (internal511ErrRetryCount <= 1) {
                int i3 = this.mPhoneId;
                IMSLog.i(str, i3, "The token is no longer valid, retry511Cnt: " + internal511ErrRetryCount);
                this.mSharedInfo.setInternal511ErrRetryCount(internal511ErrRetryCount);
                return getNextWorkflow(1);
            }
            IMSLog.i(str, this.mPhoneId, "The token is no longer valid, finish");
            return getNextWorkflow(8);
        } else if (i != 403 || !(workflow instanceof WorkflowBase.FetchHttps)) {
            if (i == 500) {
                IMSLog.i(str, this.mPhoneId, "internal server error");
                int internalErrRetryCount = this.mSharedInfo.getInternalErrRetryCount() + 1;
                if (internalErrRetryCount > 1) {
                    return getNextWorkflow(8);
                }
                Log.i(str, "retryCnt: " + internalErrRetryCount);
                this.mSharedInfo.setInternalErrRetryCount(internalErrRetryCount);
                return getNextWorkflow(1);
            } else if (i != 503) {
                return super.handleResponse(workflow, i);
            } else {
                IMSLog.i(str, this.mPhoneId, "service unavailable");
                int internal503ErrRetryCount = this.mSharedInfo.getInternal503ErrRetryCount() + 1;
                if (internal503ErrRetryCount > 1) {
                    return getNextWorkflow(8);
                }
                Log.i(str, "retry503Cnt: " + internal503ErrRetryCount);
                this.mSharedInfo.setInternal503ErrRetryCount(internal503ErrRetryCount);
                sleep(getretryAfterTime() * 1000);
                return getNextWorkflow(3);
            }
        } else if (this.mMobileNetwork && this.mHttpResponse != 511) {
            setOpMode(WorkflowBase.OpMode.DISABLE_TEMPORARY, (Map<String, String>) null);
            IMSLog.i(str, this.mPhoneId, "403 received. Set version to 0. Finish");
            return getNextWorkflow(8);
        } else if (!this.mSharedInfo.getHttpParams().containsKey("msisdn")) {
            IMSLog.i(str, this.mPhoneId, "no msisdn. try to get user");
            this.mPowerController.release();
            String msisdn = this.mDialog.getMsisdn(this.mTelephonyAdapter.getSimCountryCode());
            this.mPowerController.lock();
            if (TextUtils.isEmpty(msisdn)) {
                Log.i(str, "user didn't enter msisdn finish process");
                return getNextWorkflow(8);
            }
            this.mSharedInfo.setUserMsisdn(msisdn);
            return getNextWorkflow(1);
        } else if (TextUtils.isEmpty(this.mSharedInfo.getUserMsisdn())) {
            return super.handleResponse(workflow, i);
        } else {
            IMSLog.i(str, this.mPhoneId, "msisdn is wrong from user, try it again after 300 sec");
            setValidityTimer(300);
            return getNextWorkflow(8);
        }
    }

    /* access modifiers changed from: protected */
    public WorkflowBase.Workflow getNextWorkflow(int i) {
        switch (i) {
            case 1:
                return new WorkflowBase.Initialize() {
                    public WorkflowBase.Workflow run() throws Exception {
                        String str = WorkflowSec.LOG_TAG;
                        IMSLog.i(str, WorkflowSec.this.mPhoneId, "Initialize:");
                        WorkflowSec.this.mHttpResponse = 0;
                        WorkflowBase.Workflow run = super.run();
                        if (!(run instanceof WorkflowBase.FetchHttp)) {
                            return run;
                        }
                        if (WorkflowSec.this.mStorage.getState() != 1) {
                            IMSLog.i(str, WorkflowSec.this.mPhoneId, "getNextWorkflow: mStorage is not ready");
                            IMSLog.c(LogClass.WFS_STORAGE_NOT_READY, WorkflowSec.this.mPhoneId + ",STOR_NR");
                            WorkflowSec workflowSec = WorkflowSec.this;
                            workflowSec.addEventLog(str + ": getNextWorkflow: mStorage is not ready");
                            return WorkflowSec.this.getNextWorkflow(8);
                        } else if (WorkflowSec.this.mMobileNetwork) {
                            return run;
                        } else {
                            Log.i(str, "mMobileNetwork: false, try FetchHttps step");
                            return WorkflowSec.this.getNextWorkflow(3);
                        }
                    }
                };
            case 2:
                return new WorkflowBase.FetchHttp() {
                    public WorkflowBase.Workflow run() throws Exception {
                        IMSLog.i(WorkflowSec.LOG_TAG, WorkflowSec.this.mPhoneId, "FetchHttp:");
                        WorkflowBase.Workflow run = super.run();
                        WorkflowSec workflowSec = WorkflowSec.this;
                        workflowSec.mHttpResponse = workflowSec.mSharedInfo.getHttpResponse().getStatusCode();
                        return run;
                    }
                };
            case 3:
                return new WorkflowBase.FetchHttps() {
                    public WorkflowBase.Workflow run() throws Exception {
                        IMSLog.i(WorkflowSec.LOG_TAG, WorkflowSec.this.mPhoneId, "FetchHttps:");
                        return super.run();
                    }

                    /* access modifiers changed from: protected */
                    public void setHttps() {
                        WorkflowSec.this.mSharedInfo.setHttpsDefault();
                        if (WorkflowSec.this.mParamHandler.isConfigProxy()) {
                            WorkflowSec.this.mSharedInfo.changeConfigProxyUriForHttp();
                            WorkflowSec.this.mSharedInfo.setHttpProxyDefault();
                        }
                        String str = WorkflowSec.LOG_TAG;
                        StringBuilder sb = new StringBuilder();
                        sb.append("FetchHttps: NetType = ");
                        sb.append(WorkflowSec.this.mTelephonyAdapter.getNetType());
                        sb.append(", Identity = ");
                        WorkflowSec workflowSec = WorkflowSec.this;
                        sb.append(workflowSec.mTelephonyAdapter.getIdentityByPhoneId(workflowSec.mPhoneId));
                        sb.append(", SipUri = ");
                        sb.append(WorkflowSec.this.mTelephonyAdapter.getSipUri());
                        IMSLog.s(str, sb.toString());
                        WorkflowSec workflowSec2 = WorkflowSec.this;
                        workflowSec2.mSharedInfo.addHttpParam("vers", String.valueOf(workflowSec2.getVersion()));
                        WorkflowSec workflowSec3 = WorkflowSec.this;
                        workflowSec3.mSharedInfo.addHttpParam("IMSI", workflowSec3.mTelephonyAdapter.getImsi());
                        WorkflowSec workflowSec4 = WorkflowSec.this;
                        workflowSec4.mSharedInfo.addHttpParam(ConfigConstants.PNAME.IMEI, workflowSec4.mTelephonyAdapter.getImei());
                        WorkflowSec.this.mSharedInfo.addHttpParam("terminal_model", ConfigConstants.BUILD.TERMINAL_MODEL);
                        WorkflowSec workflowSec5 = WorkflowSec.this;
                        workflowSec5.mSharedInfo.addHttpParam("default_sms_app", ConfigUtil.isSecDmaPackageInuse(workflowSec5.mContext, workflowSec5.mPhoneId) ? "1" : "2");
                        WorkflowSec workflowSec6 = WorkflowSec.this;
                        if (!workflowSec6.mMobileNetwork || workflowSec6.mHttpResponse == 511) {
                            if (!TextUtils.isEmpty(workflowSec6.mTelephonyAdapter.getMsisdn())) {
                                WorkflowSec workflowSec7 = WorkflowSec.this;
                                workflowSec7.mSharedInfo.addHttpParam("msisdn", workflowSec7.mParamHandler.encodeRFC3986(workflowSec7.mTelephonyAdapter.getMsisdn()));
                            } else {
                                IMSLog.i(str, "FetchHttps: MSISDN is null, using the PAU");
                                WorkflowSec workflowSec8 = WorkflowSec.this;
                                int i = workflowSec8.mPhoneId;
                                Context context = workflowSec8.mContext;
                                String string = ImsSharedPrefHelper.getString(i, context, IConfigModule.MSISDN_FROM_PAU, "IMSI_" + SimManagerFactory.getImsiFromPhoneId(WorkflowSec.this.mPhoneId), "");
                                WorkflowSec workflowSec9 = WorkflowSec.this;
                                workflowSec9.addEventLog(str + ": pauFromSP");
                                IMSLog.s(str, "pauFromSP: " + string);
                                IMSLog.c(LogClass.WFS_PAU_FROM_SP, WorkflowSec.this.mPhoneId + "PAU_FROM_SP");
                                if (!TextUtils.isEmpty(string)) {
                                    WorkflowSec workflowSec10 = WorkflowSec.this;
                                    workflowSec10.mSharedInfo.addHttpParam("msisdn", workflowSec10.mParamHandler.encodeRFC3986(string));
                                }
                            }
                            if (!TextUtils.isEmpty(WorkflowSec.this.mSharedInfo.getUserMsisdn())) {
                                WorkflowSec workflowSec11 = WorkflowSec.this;
                                SharedInfo sharedInfo = workflowSec11.mSharedInfo;
                                sharedInfo.addHttpParam("msisdn", workflowSec11.mParamHandler.encodeRFC3986(sharedInfo.getUserMsisdn()));
                            }
                            WorkflowSec workflowSec12 = WorkflowSec.this;
                            workflowSec12.mSharedInfo.addHttpParam(ConfigConstants.PNAME.SMS_PORT, ConfigUtil.getSmsPort(workflowSec12.mPhoneId));
                            String token = WorkflowSec.this.getToken();
                            if (TextUtils.isEmpty(token)) {
                                WorkflowSec workflowSec13 = WorkflowSec.this;
                                int i2 = workflowSec13.mPhoneId;
                                Context context2 = workflowSec13.mContext;
                                String string2 = ImsSharedPrefHelper.getString(i2, context2, ImsSharedPrefHelper.VALID_RCS_CONFIG, "IMSI_" + SimManagerFactory.getImsiFromPhoneId(WorkflowSec.this.mPhoneId), "");
                                if (!TextUtils.isEmpty(string2)) {
                                    Log.i(str, "use last valid token");
                                    token = string2;
                                }
                            }
                            WorkflowSec.this.mSharedInfo.addHttpParam("token", token);
                        }
                        WorkflowSec.this.mSharedInfo.addHttpParam("terminal_vendor", "SEC");
                        WorkflowSec workflowSec14 = WorkflowSec.this;
                        workflowSec14.mSharedInfo.addHttpParam("terminal_sw_version", workflowSec14.mParamHandler.getModelInfoFromBuildVersion(ConfigUtil.getModelName(workflowSec14.mPhoneId), ConfigConstants.PVALUE.TERMINAL_SW_VERSION, 8, true));
                        WorkflowSec workflowSec15 = WorkflowSec.this;
                        workflowSec15.mSharedInfo.addHttpParam("rcs_version", workflowSec15.mRcsVersion);
                        WorkflowSec.this.mSharedInfo.addHttpParam(ConfigConstants.PNAME.RCS_PROFILE, "UP_1.0");
                        WorkflowSec.this.mSharedInfo.addHttpParam(ConfigConstants.PNAME.PROVISIONING_VERSION, "2.0");
                        WorkflowSec workflowSec16 = WorkflowSec.this;
                        workflowSec16.setRcsState(workflowSec16.convertRcsStateWithSpecificParam());
                        WorkflowSec workflowSec17 = WorkflowSec.this;
                        workflowSec17.mSharedInfo.addHttpParam(ConfigConstants.PNAME.RCS_STATE, workflowSec17.getRcsState());
                        if (WorkflowSec.this.mStartForce && !String.valueOf(WorkflowBase.OpMode.DISABLE_RCS_BY_USER.value()).equals(WorkflowSec.this.getRcsState())) {
                            Log.i(str, "mStartForce: true, vers: 0");
                            WorkflowSec.this.mSharedInfo.addHttpParam("vers", "0");
                        }
                        if (WorkflowSec.this.getOpMode() == WorkflowBase.OpMode.DORMANT) {
                            Log.i(str, "use backup version in case of dormant, vers: " + WorkflowSec.this.getVersionBackup());
                            WorkflowSec workflowSec18 = WorkflowSec.this;
                            workflowSec18.mSharedInfo.addHttpParam("vers", workflowSec18.getVersionBackup());
                        }
                    }
                };
            case 4:
                return new WorkflowBase.Authorize() {
                    public WorkflowBase.Workflow run() throws Exception {
                        IMSLog.i(WorkflowSec.LOG_TAG, WorkflowSec.this.mPhoneId, "Authorize:");
                        WorkflowBase.Workflow run = super.run();
                        if (run instanceof WorkflowBase.Finish) {
                            WorkflowSec.this.mSharedInfo.getHttpResponse().setStatusCode(700);
                        }
                        return run;
                    }

                    /* access modifiers changed from: protected */
                    public String getOtp() {
                        String str;
                        String smsType = ConfigUtil.getSmsType(WorkflowSec.this.mPhoneId);
                        if ("text".equals(smsType)) {
                            if (WorkflowSec.this.mAuthTryCount < 1) {
                                WorkflowSec workflowSec = WorkflowSec.this;
                                workflowSec.mAuthTryCount = workflowSec.mAuthTryCount + 1;
                                str = WorkflowSec.this.mTelephonyAdapter.getOtp();
                                String str2 = WorkflowSec.LOG_TAG;
                                Log.i(str2, "otp: " + IMSLog.checker(str));
                                return str;
                            }
                        } else if ("binary".equals(smsType) && WorkflowSec.this.mAuthHiddenTryCount < 3) {
                            WorkflowSec workflowSec2 = WorkflowSec.this;
                            workflowSec2.mAuthHiddenTryCount = workflowSec2.mAuthHiddenTryCount + 1;
                            str = WorkflowSec.this.mTelephonyAdapter.getPortOtp();
                            String str22 = WorkflowSec.LOG_TAG;
                            Log.i(str22, "otp: " + IMSLog.checker(str));
                            return str;
                        }
                        str = null;
                        String str222 = WorkflowSec.LOG_TAG;
                        Log.i(str222, "otp: " + IMSLog.checker(str));
                        return str;
                    }
                };
            case 5:
                return new WorkflowBase.FetchOtp() {
                    public WorkflowBase.Workflow run() throws Exception {
                        IMSLog.i(WorkflowSec.LOG_TAG, WorkflowSec.this.mPhoneId, "FetchOtp:");
                        return super.run();
                    }

                    /* access modifiers changed from: protected */
                    public void setHttp() {
                        super.setHttp();
                        SharedInfo sharedInfo = WorkflowSec.this.mSharedInfo;
                        sharedInfo.addHttpHeader(HttpController.HEADER_COOKIE, sharedInfo.getHttpResponse().getHeader().get(HttpController.HEADER_SET_COOKIE));
                    }
                };
            case 6:
                return new WorkflowBase.Parse() {
                    public WorkflowBase.Workflow run() throws Exception {
                        IMSLog.i(WorkflowSec.LOG_TAG, WorkflowSec.this.mPhoneId, "Parse:");
                        return super.run();
                    }
                };
            case 7:
                return new WorkflowBase.Store() {
                    public WorkflowBase.Workflow run() throws Exception {
                        String str = WorkflowSec.LOG_TAG;
                        IMSLog.i(str, WorkflowSec.this.mPhoneId, "Store:");
                        Map<String, String> parsedXml = WorkflowSec.this.mSharedInfo.getParsedXml();
                        int version = WorkflowSec.this.getVersion(parsedXml);
                        Log.i(str, String.format(Locale.US, "Store: version [%s] => [%s]", new Object[]{Integer.valueOf(WorkflowSec.this.getVersion()), Integer.valueOf(version)}));
                        if (version == 0) {
                            WorkflowSec workflowSec = WorkflowSec.this;
                            workflowSec.mModule.setAcsTryReason(workflowSec.mPhoneId, DiagnosisConstants.RCSA_ATRE.VERSION_ZERO);
                        }
                        WorkflowBase.OpMode rcsDisabledState = WorkflowSec.this.getRcsDisabledState(parsedXml);
                        if (WorkflowSec.this.isValidRcsDisabledState(rcsDisabledState)) {
                            WorkflowSec.this.setOpMode(rcsDisabledState, parsedXml);
                            return WorkflowSec.this.getNextWorkflow(8);
                        }
                        WorkflowSec workflowSec2 = WorkflowSec.this;
                        workflowSec2.setOpMode(workflowSec2.getOpMode(parsedXml), parsedXml);
                        if (WorkflowSec.this.getOpMode() == WorkflowBase.OpMode.ACTIVE) {
                            WorkflowSec workflowSec3 = WorkflowSec.this;
                            workflowSec3.setValidityTimer(workflowSec3.getValidity());
                        }
                        return WorkflowSec.this.getNextWorkflow(8);
                    }
                };
            case 8:
                return new WorkflowBase.Finish() {
                    public WorkflowBase.Workflow run() throws Exception {
                        Optional.ofNullable(WorkflowSec.this.mSharedInfo.getHttpResponse()).map(new WorkflowSec$9$$ExternalSyntheticLambda0()).ifPresent(new WorkflowSec$9$$ExternalSyntheticLambda1(this));
                        IMSLog.i(WorkflowSec.LOG_TAG, WorkflowSec.this.mPhoneId, "workflow is finished");
                        return null;
                    }

                    /* access modifiers changed from: private */
                    public /* synthetic */ void lambda$run$0(Integer num) {
                        WorkflowSec.this.setLastErrorCode(num.intValue());
                        WorkflowSec.this.mStorage.write(ConfigConstants.PATH.INFO_LAST_ERROR_CODE, String.valueOf(num));
                    }
                };
            default:
                return null;
        }
    }

    public void cleanup() {
        super.cleanup();
        this.mContext.unregisterReceiver(this.mIntentReceiver);
    }
}
