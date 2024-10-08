package com.sec.internal.ims.config.workflow;

import android.database.sqlite.SQLiteFullException;
import android.net.Network;
import android.os.Message;
import android.text.TextUtils;
import android.util.Log;
import com.sec.internal.constants.ims.DiagnosisConstants;
import com.sec.internal.constants.ims.config.ConfigConstants;
import com.sec.internal.helper.DmConfigHelper;
import com.sec.internal.helper.SimUtil;
import com.sec.internal.helper.httpclient.HttpController;
import com.sec.internal.ims.config.SharedInfo;
import com.sec.internal.ims.config.exception.EmptyBodyAndCookieException;
import com.sec.internal.ims.config.exception.InvalidHeaderException;
import com.sec.internal.ims.config.exception.NoInitialDataException;
import com.sec.internal.ims.config.exception.UnknownStatusException;
import com.sec.internal.ims.config.workflow.WorkflowBase;
import com.sec.internal.ims.servicemodules.ss.UtStateMachine;
import com.sec.internal.ims.settings.DeviceConfigManager;
import com.sec.internal.ims.util.ConfigUtil;
import com.sec.internal.imscr.LogClass;
import com.sec.internal.interfaces.ims.config.IHttpAdapter;
import com.sec.internal.log.IMSLog;
import java.util.Date;
import java.util.Map;

public class WorkflowInterop extends WorkflowUpBase {
    protected static final int HTTPERR_RETRY_AFTER_TIME = 10;
    protected static final int HTTPERR_TRY_MAX_COUNT = 2;
    protected static final String LOG_TAG = WorkflowJibe.class.getSimpleName();
    protected static final String OTP_SMS_BINARY_TYPE = "binary";
    protected static final String OTP_SMS_TEXT_TYPE = "text";
    protected static final int OTP_SMS_TIME_OUT = 700;
    protected int m503ErrCount = 0;
    protected int m511ErrCount = 0;
    protected int mAuthHiddenTryCount = 0;
    protected int mAuthTryCount = 0;
    protected int mHttpResponse = 0;
    protected boolean mIsMobileConfigCompleted = false;
    protected boolean mIsMobileConnected = false;
    protected boolean mIsMobileRequested = false;

    /* JADX WARNING: Illegal instructions before constructor call */
    /* Code decompiled incorrectly, please refer to instructions dump. */
    public WorkflowInterop(android.os.Looper r13, android.content.Context r14, com.sec.internal.interfaces.ims.config.IConfigModule r15, com.sec.internal.constants.Mno r16, int r17) {
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
            r11.mIsMobileRequested = r0
            r11.mIsMobileConnected = r0
            r11.mIsMobileConfigCompleted = r0
            r11.mHttpResponse = r0
            r11.mAuthTryCount = r0
            r11.mAuthHiddenTryCount = r0
            r11.m511ErrCount = r0
            r11.m503ErrCount = r0
            return
        */
        throw new UnsupportedOperationException("Method not decompiled: com.sec.internal.ims.config.workflow.WorkflowInterop.<init>(android.os.Looper, android.content.Context, com.sec.internal.interfaces.ims.config.IConfigModule, com.sec.internal.constants.Mno, int):void");
    }

    public void handleMessage(Message message) {
        String str = LOG_TAG;
        int i = this.mPhoneId;
        IMSLog.i(str, i, "handleMessage: " + message.what);
        int i2 = message.what;
        if (i2 == 0) {
            this.mStartForce = true;
        } else if (i2 != 1) {
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
                return;
            } else {
                IMSLog.i(str, this.mPhoneId, "sms default application is changed to non-samsung");
                setOpMode(WorkflowBase.OpMode.DISABLE_RCS_BY_USER, (Map<String, String>) null);
                removeMessages(1);
                sendEmptyMessage(1);
                return;
            }
        }
        if (this.mIsConfigOngoing) {
            Log.i(str, "AutoConfig: ongoing");
            return;
        }
        this.mIsConfigOngoing = true;
        int i3 = this.mPhoneId;
        IMSLog.i(str, i3, "AutoConfig: start, mStartForce: " + this.mStartForce);
        this.mModule.getHandler().removeMessages(3, Integer.valueOf(this.mPhoneId));
        this.mPowerController.lock();
        initAutoConfig();
        int version = getVersion();
        this.mOldVersion = version;
        if (scheduleAutoconfigForInterop(version)) {
            executeAutoConfig();
        }
        this.mNewVersion = getVersion();
        endAutoConfig(true);
        int i4 = this.mPhoneId;
        IMSLog.i(str, i4, "oldVersion: " + this.mOldVersion + " newVersion: " + this.mNewVersion);
        IMSLog.i(str, this.mPhoneId, "AutoConfig: finish");
        this.mModule.getHandler().sendMessage(obtainMessage(3, this.mOldVersion, this.mNewVersion, Integer.valueOf(this.mPhoneId)));
        this.mPowerController.release();
    }

    /* access modifiers changed from: protected */
    public boolean scheduleAutoconfigForInterop(int i) {
        String str = LOG_TAG;
        IMSLog.i(str, this.mPhoneId, "scheduleAutoconfigForInterop");
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
        this.mAuthTryCount = 0;
        this.mAuthHiddenTryCount = 0;
    }

    /* access modifiers changed from: protected */
    public void endAutoConfig(boolean z) {
        super.endAutoConfig(z);
        this.mIsMobileRequested = false;
        this.mIsMobileConnected = false;
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
        Log.i(str, "mIsMobileRequested: " + this.mIsMobileRequested + ", mIsMobileConnected: " + this.mIsMobileConnected);
        this.mHttp.setNetwork((Network) null);
        this.mHttp.open(this.mSharedInfo.getUrl());
        IHttpAdapter.Response request = this.mHttp.request();
        this.mHttp.close();
        return request;
    }

    /* access modifiers changed from: protected */
    public WorkflowBase.Workflow handleResponse(WorkflowBase.Workflow workflow, int i) throws InvalidHeaderException, UnknownStatusException {
        String str = LOG_TAG;
        int i2 = this.mPhoneId;
        IMSLog.i(str, i2, "handleResponse: " + i);
        addEventLog(str + "handleResponse: " + i);
        setLastErrorCode(i);
        int lastErrorCode = getLastErrorCode();
        if (lastErrorCode == 0) {
            return super.handleResponse(workflow, i);
        }
        if (lastErrorCode == 200) {
            this.m511ErrCount = 0;
            this.m503ErrCount = 0;
        } else if (lastErrorCode == 403) {
            Log.i(str, "set version to zero");
            setOpMode(WorkflowBase.OpMode.DISABLE_TEMPORARY, (Map<String, String>) null);
            return getNextWorkflow(8);
        } else if (lastErrorCode == 503) {
            long j = getretryAfterTime();
            Log.i(str, "m503ErrCount: " + this.m503ErrCount + " retryAfterTime: " + j);
            int i3 = this.m503ErrCount;
            if (i3 < 2) {
                this.m503ErrCount = i3 + 1;
                Log.i(str, "retry after " + j + " sec");
                int i4 = (int) j;
                setValidityTimer(i4);
                setNextAutoconfigTimeAfter(i4);
            }
            return getNextWorkflow(8);
        } else if (lastErrorCode == 511) {
            if (workflow instanceof WorkflowBase.FetchHttp) {
                return getNextWorkflow(3);
            }
            Log.i(str, "The token isn't valid: m511ErrCount: " + this.m511ErrCount);
            setToken("", DiagnosisConstants.RCSA_TDRE.INVALID_TOKEN);
            int i5 = this.m511ErrCount;
            if (i5 < 2) {
                this.m511ErrCount = i5 + 1;
                Log.i(str, "retry after 10 sec");
                setValidityTimer(10);
                setNextAutoconfigTimeAfter(10);
            }
            return getNextWorkflow(8);
        }
        return super.handleResponse(workflow, i);
    }

    /* access modifiers changed from: protected */
    public WorkflowBase.Workflow getNextWorkflow(int i) {
        switch (i) {
            case 1:
                return new WorkflowBase.Initialize() {
                    public WorkflowBase.Workflow run() throws Exception {
                        String str = WorkflowInterop.LOG_TAG;
                        IMSLog.i(str, WorkflowInterop.this.mPhoneId, "Initialize:");
                        WorkflowBase.Workflow run = super.run();
                        if (!(run instanceof WorkflowBase.FetchHttp) || WorkflowInterop.this.mMobileNetwork) {
                            return run;
                        }
                        Log.i(str, "mMobileNetwork: false, try FetchHttps step");
                        return WorkflowInterop.this.getNextWorkflow(3);
                    }

                    /* access modifiers changed from: protected */
                    public void init() throws NoInitialDataException {
                        WorkflowInterop.this.mHttpResponse = 0;
                        super.init();
                    }
                };
            case 2:
                return new WorkflowBase.FetchHttp() {
                    public WorkflowBase.Workflow run() throws Exception {
                        IMSLog.i(WorkflowInterop.LOG_TAG, WorkflowInterop.this.mPhoneId, "FetchHttp:");
                        WorkflowBase.Workflow run = super.run();
                        WorkflowInterop workflowInterop = WorkflowInterop.this;
                        workflowInterop.mHttpResponse = workflowInterop.mSharedInfo.getHttpResponse().getStatusCode();
                        return run;
                    }
                };
            case 3:
                return new WorkflowBase.FetchHttps() {
                    public WorkflowBase.Workflow run() throws Exception {
                        IMSLog.i(WorkflowInterop.LOG_TAG, WorkflowInterop.this.mPhoneId, "FetchHttps:");
                        return super.run();
                    }

                    /* access modifiers changed from: protected */
                    public void setHttps() {
                        WorkflowInterop.this.mSharedInfo.setHttpsDefault();
                        int subId = SimUtil.getSubId(WorkflowInterop.this.mPhoneId);
                        WorkflowInterop workflowInterop = WorkflowInterop.this;
                        workflowInterop.mSharedInfo.addHttpParam("IMSI", workflowInterop.mTelephonyAdapter.getSubscriberId(subId));
                        WorkflowInterop workflowInterop2 = WorkflowInterop.this;
                        workflowInterop2.mSharedInfo.addHttpParam(ConfigConstants.PNAME.IMEI, workflowInterop2.mTelephonyAdapter.getDeviceId(workflowInterop2.mPhoneId));
                        WorkflowInterop workflowInterop3 = WorkflowInterop.this;
                        workflowInterop3.mSharedInfo.addHttpParam("default_sms_app", ConfigUtil.isSecDmaPackageInuse(workflowInterop3.mContext, workflowInterop3.mPhoneId) ? "1" : "2");
                        WorkflowInterop workflowInterop4 = WorkflowInterop.this;
                        if (!workflowInterop4.mMobileNetwork || workflowInterop4.mHttpResponse == 511) {
                            String msisdn = workflowInterop4.mTelephonyAdapter.getMsisdn();
                            if (!TextUtils.isEmpty(msisdn)) {
                                Log.i(WorkflowInterop.LOG_TAG, "use msisdn from telephony");
                                WorkflowInterop workflowInterop5 = WorkflowInterop.this;
                                workflowInterop5.mSharedInfo.addHttpParam("msisdn", workflowInterop5.mParamHandler.encodeRFC3986(msisdn));
                            }
                            String userMsisdn = WorkflowInterop.this.mSharedInfo.getUserMsisdn();
                            if (!TextUtils.isEmpty(userMsisdn)) {
                                Log.i(WorkflowInterop.LOG_TAG, "use msisdn from sharedInfo");
                                WorkflowInterop workflowInterop6 = WorkflowInterop.this;
                                workflowInterop6.mSharedInfo.addHttpParam("msisdn", workflowInterop6.mParamHandler.encodeRFC3986(userMsisdn));
                            }
                            WorkflowInterop workflowInterop7 = WorkflowInterop.this;
                            workflowInterop7.mSharedInfo.addHttpParam(ConfigConstants.PNAME.SMS_PORT, ConfigUtil.getSmsPort(workflowInterop7.mPhoneId));
                            IMSLog.c(LogClass.WFJ_OTP_SMS_PORT, WorkflowInterop.this.mPhoneId + ",OSP:" + ConfigUtil.getSmsPort(WorkflowInterop.this.mPhoneId));
                            WorkflowInterop workflowInterop8 = WorkflowInterop.this;
                            workflowInterop8.mSharedInfo.addHttpParam("token", workflowInterop8.getToken());
                        }
                        WorkflowInterop.this.mSharedInfo.addHttpParam("terminal_vendor", "SEC");
                        WorkflowInterop.this.mSharedInfo.addHttpParam("terminal_model", ConfigConstants.PVALUE.TERMINAL_MODEL);
                        WorkflowInterop workflowInterop9 = WorkflowInterop.this;
                        workflowInterop9.mSharedInfo.addHttpParam("terminal_sw_version", workflowInterop9.mParamHandler.getModelInfoFromBuildVersion(ConfigUtil.getModelName(workflowInterop9.mPhoneId), ConfigConstants.PVALUE.TERMINAL_SW_VERSION, 8, true));
                        WorkflowInterop.this.mSharedInfo.addHttpParam("client_vendor", "SEC");
                        SharedInfo sharedInfo = WorkflowInterop.this.mSharedInfo;
                        sharedInfo.addHttpParam("client_version", WorkflowInterop.this.mClientPlatform + WorkflowInterop.this.mClientVersion);
                        WorkflowInterop.this.mSharedInfo.addHttpParam("rcs_version", ConfigConstants.PVALUE.GOOG_DEFAULT_RCS_VERSION);
                        WorkflowInterop.this.mSharedInfo.addHttpParam(ConfigConstants.PNAME.RCS_PROFILE, "UP_2.0-b1");
                        WorkflowInterop.this.mSharedInfo.addHttpParam(ConfigConstants.PNAME.PROVISIONING_VERSION, ConfigConstants.PVALUE.PROVISIONING_VERSION_4_0);
                        WorkflowInterop workflowInterop10 = WorkflowInterop.this;
                        workflowInterop10.setRcsState(workflowInterop10.convertRcsStateWithSpecificParam());
                        WorkflowInterop workflowInterop11 = WorkflowInterop.this;
                        workflowInterop11.mSharedInfo.addHttpParam(ConfigConstants.PNAME.RCS_STATE, workflowInterop11.getRcsState());
                        if (WorkflowInterop.this.mStartForce && !String.valueOf(WorkflowBase.OpMode.DISABLE_RCS_BY_USER.value()).equals(WorkflowInterop.this.getRcsState())) {
                            Log.i(WorkflowInterop.LOG_TAG, "mStartForce: true, vers: 0");
                            WorkflowInterop.this.mSharedInfo.addHttpParam("vers", "0");
                        }
                        if (WorkflowInterop.this.getOpMode() == WorkflowBase.OpMode.DORMANT) {
                            String str = WorkflowInterop.LOG_TAG;
                            Log.i(str, "use backup version in case of dormant, vers: " + WorkflowInterop.this.getVersionBackup());
                            WorkflowInterop workflowInterop12 = WorkflowInterop.this;
                            workflowInterop12.mSharedInfo.addHttpParam("vers", workflowInterop12.getVersionBackup());
                        }
                    }
                };
            case 4:
                return new WorkflowBase.Authorize() {
                    public WorkflowBase.Workflow run() throws Exception {
                        IMSLog.i(WorkflowInterop.LOG_TAG, WorkflowInterop.this.mPhoneId, "Authorize:");
                        WorkflowBase.Workflow run = super.run();
                        if (run instanceof WorkflowBase.Finish) {
                            WorkflowInterop.this.mSharedInfo.getHttpResponse().setStatusCode(700);
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
                            com.sec.internal.ims.config.workflow.WorkflowInterop r0 = com.sec.internal.ims.config.workflow.WorkflowInterop.this
                            int r0 = r0.mPhoneId
                            java.lang.String r0 = com.sec.internal.ims.util.ConfigUtil.getSmsType(r0)
                            java.lang.String r1 = com.sec.internal.ims.config.workflow.WorkflowInterop.LOG_TAG
                            com.sec.internal.ims.config.workflow.WorkflowInterop r2 = com.sec.internal.ims.config.workflow.WorkflowInterop.this
                            int r2 = r2.mPhoneId
                            java.lang.StringBuilder r3 = new java.lang.StringBuilder
                            r3.<init>()
                            java.lang.String r4 = "otpSmsType: "
                            r3.append(r4)
                            r3.append(r0)
                            java.lang.String r4 = " mAuthTryCount: "
                            r3.append(r4)
                            com.sec.internal.ims.config.workflow.WorkflowInterop r4 = com.sec.internal.ims.config.workflow.WorkflowInterop.this
                            int r4 = r4.mAuthTryCount
                            r3.append(r4)
                            java.lang.String r4 = " mAuthHiddenTryCount: "
                            r3.append(r4)
                            com.sec.internal.ims.config.workflow.WorkflowInterop r4 = com.sec.internal.ims.config.workflow.WorkflowInterop.this
                            int r4 = r4.mAuthHiddenTryCount
                            r3.append(r4)
                            java.lang.String r3 = r3.toString()
                            com.sec.internal.log.IMSLog.i(r1, r2, r3)
                            java.lang.StringBuilder r1 = new java.lang.StringBuilder
                            r1.<init>()
                            com.sec.internal.ims.config.workflow.WorkflowInterop r2 = com.sec.internal.ims.config.workflow.WorkflowInterop.this
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
                            com.sec.internal.ims.config.workflow.WorkflowInterop r1 = com.sec.internal.ims.config.workflow.WorkflowInterop.this
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
                            com.sec.internal.ims.config.workflow.WorkflowInterop r5 = com.sec.internal.ims.config.workflow.WorkflowInterop.this
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
                        throw new UnsupportedOperationException("Method not decompiled: com.sec.internal.ims.config.workflow.WorkflowInterop.AnonymousClass5.getOtp():java.lang.String");
                    }
                };
            case 5:
                return new WorkflowBase.FetchOtp() {
                    public WorkflowBase.Workflow run() throws Exception {
                        IMSLog.i(WorkflowInterop.LOG_TAG, WorkflowInterop.this.mPhoneId, "FetchOtp:");
                        return super.run();
                    }

                    /* access modifiers changed from: protected */
                    public void setHttp() {
                        super.setHttp();
                        SharedInfo sharedInfo = WorkflowInterop.this.mSharedInfo;
                        sharedInfo.addHttpHeader(HttpController.HEADER_COOKIE, sharedInfo.getHttpResponse().getHeader().get(HttpController.HEADER_SET_COOKIE));
                    }
                };
            case 6:
                return new WorkflowBase.Parse() {
                    public WorkflowBase.Workflow run() throws Exception {
                        IMSLog.i(WorkflowInterop.LOG_TAG, WorkflowInterop.this.mPhoneId, "Parse:");
                        return super.run();
                    }
                };
            case 7:
                return new WorkflowBase.Store() {
                    public WorkflowBase.Workflow run() throws Exception {
                        String str = WorkflowInterop.LOG_TAG;
                        IMSLog.i(str, WorkflowInterop.this.mPhoneId, "Store:");
                        Map<String, String> parsedXml = WorkflowInterop.this.mSharedInfo.getParsedXml();
                        boolean z = WorkflowInterop.this.getVersion() != WorkflowInterop.this.getVersion(parsedXml);
                        Log.i(str, "versionChange: " + z);
                        WorkflowBase.OpMode rcsDisabledState = WorkflowInterop.this.getRcsDisabledState(parsedXml);
                        if (WorkflowInterop.this.isValidRcsDisabledState(rcsDisabledState)) {
                            WorkflowInterop.this.setOpMode(rcsDisabledState, parsedXml);
                            return WorkflowInterop.this.getNextWorkflow(8);
                        }
                        WorkflowInterop workflowInterop = WorkflowInterop.this;
                        workflowInterop.setOpMode(workflowInterop.getOpMode(parsedXml), parsedXml);
                        if (WorkflowInterop.this.getOpMode() == WorkflowBase.OpMode.ACTIVE) {
                            WorkflowInterop workflowInterop2 = WorkflowInterop.this;
                            workflowInterop2.setValidityTimer(workflowInterop2.getValidity());
                        }
                        return WorkflowInterop.this.getNextWorkflow(8);
                    }
                };
            case 8:
                return new WorkflowBase.Finish() {
                    public WorkflowBase.Workflow run() {
                        IMSLog.i(WorkflowInterop.LOG_TAG, WorkflowInterop.this.mPhoneId, "Finish:");
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
