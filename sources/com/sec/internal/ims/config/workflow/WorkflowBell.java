package com.sec.internal.ims.config.workflow;

import android.content.Context;
import android.database.sqlite.SQLiteFullException;
import android.os.Looper;
import android.text.TextUtils;
import android.util.Log;
import com.sec.internal.constants.Mno;
import com.sec.internal.constants.ims.config.ConfigConstants;
import com.sec.internal.ims.config.SharedInfo;
import com.sec.internal.ims.config.exception.InvalidXmlException;
import com.sec.internal.ims.config.exception.NoInitialDataException;
import com.sec.internal.ims.config.exception.UnknownStatusException;
import com.sec.internal.ims.config.workflow.WorkflowBase;
import com.sec.internal.ims.servicemodules.ss.UtStateMachine;
import com.sec.internal.ims.util.ConfigUtil;
import com.sec.internal.interfaces.ims.config.IConfigModule;
import com.sec.internal.log.IMSLog;
import java.util.Map;

public class WorkflowBell extends WorkflowBase {
    public static final String LOG_TAG = WorkflowBell.class.getSimpleName();

    /* access modifiers changed from: protected */
    public WorkflowBase.Workflow getNextWorkflow(int i) {
        return null;
    }

    public WorkflowBell(Looper looper, Context context, IConfigModule iConfigModule, Mno mno, int i) {
        super(looper, context, iConfigModule, mno, i);
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
                Log.i(LOG_TAG, "NoInitialDataException occur:" + e.getMessage());
                Log.i(LOG_TAG, "wait 10 sec. and retry");
                sleep(10000);
                workflow = new Initialize();
                e.printStackTrace();
                initialize = workflow;
                i--;
            } catch (UnknownStatusException e2) {
                Log.i(LOG_TAG, "UnknownStatusException occur:" + e2.getMessage());
                Log.i(LOG_TAG, "wait 2 sec. and retry");
                sleep(UtStateMachine.HTTP_READ_TIMEOUT_GCF);
                workflow = new Initialize();
                e2.printStackTrace();
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
                Log.i(LOG_TAG, "wait 1 sec. and retry");
                sleep(1000);
                workflow = new Initialize();
                e4.printStackTrace();
                initialize = workflow;
                i--;
            }
            i--;
        }
    }

    protected class Initialize implements WorkflowBase.Workflow {
        protected Initialize() {
        }

        public WorkflowBase.Workflow run() throws Exception {
            WorkflowBase.Workflow workflow;
            String str = WorkflowBell.LOG_TAG;
            IMSLog.i(str, WorkflowBell.this.mPhoneId, "Initialize:");
            WorkflowBell workflowBell = WorkflowBell.this;
            workflowBell.mSharedInfo.setUrl(workflowBell.mParamHandler.initUrl());
            WorkflowBell.this.mCookieHandler.clearCookie();
            WorkflowBell workflowBell2 = WorkflowBell.this;
            if (workflowBell2.mStartForce) {
                workflow = new FetchHttp();
            } else {
                int i = AnonymousClass1.$SwitchMap$com$sec$internal$ims$config$workflow$WorkflowBase$OpMode[workflowBell2.getOpMode().ordinal()];
                workflow = (i == 1 || i == 2 || i == 3) ? new FetchHttp() : (i == 4 || i == 5) ? new Finish() : null;
            }
            if (!(workflow instanceof FetchHttp) || WorkflowBell.this.mMobileNetwork) {
                return workflow;
            }
            Log.i(str, "now use wifi. try non-ps step directly.");
            return new FetchHttps();
        }
    }

    /* renamed from: com.sec.internal.ims.config.workflow.WorkflowBell$1  reason: invalid class name */
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
            throw new UnsupportedOperationException("Method not decompiled: com.sec.internal.ims.config.workflow.WorkflowBell.AnonymousClass1.<clinit>():void");
        }
    }

    protected class FetchHttp implements WorkflowBase.Workflow {
        protected FetchHttp() {
        }

        public WorkflowBase.Workflow run() throws Exception {
            IMSLog.i(WorkflowBell.LOG_TAG, WorkflowBell.this.mPhoneId, "FetchHttp:");
            WorkflowBell.this.mSharedInfo.setHttpDefault();
            WorkflowBell workflowBell = WorkflowBell.this;
            workflowBell.mSharedInfo.setHttpResponse(workflowBell.getHttpResponse());
            if (WorkflowBell.this.mSharedInfo.getHttpResponse().getStatusCode() == 200 || WorkflowBell.this.mSharedInfo.getHttpResponse().getStatusCode() == 511) {
                return new FetchHttps();
            }
            WorkflowBell workflowBell2 = WorkflowBell.this;
            return workflowBell2.handleResponse2(new Initialize(), new FetchHttps(), new Finish());
        }
    }

    protected class FetchHttps implements WorkflowBase.Workflow {
        protected FetchHttps() {
        }

        public WorkflowBase.Workflow run() throws Exception {
            String str = WorkflowBell.LOG_TAG;
            IMSLog.i(str, WorkflowBell.this.mPhoneId, "FetchHttps:");
            WorkflowBell.this.mSharedInfo.setHttpsDefault();
            WorkflowBell workflowBell = WorkflowBell.this;
            workflowBell.mSharedInfo.addHttpParam("vers", String.valueOf(workflowBell.getVersion()));
            WorkflowBell workflowBell2 = WorkflowBell.this;
            workflowBell2.mSharedInfo.addHttpParam("IMSI", workflowBell2.mTelephonyAdapter.getImsi());
            WorkflowBell workflowBell3 = WorkflowBell.this;
            workflowBell3.mSharedInfo.addHttpParam(ConfigConstants.PNAME.IMEI, workflowBell3.mTelephonyAdapter.getImei());
            WorkflowBell.this.mSharedInfo.addHttpParam("terminal_model", ConfigConstants.BUILD.TERMINAL_MODEL);
            WorkflowBell workflowBell4 = WorkflowBell.this;
            workflowBell4.mSharedInfo.addHttpParam("default_sms_app", ConfigUtil.isSecDmaPackageInuse(workflowBell4.mContext, workflowBell4.mPhoneId) ? "1" : "2");
            WorkflowBell workflowBell5 = WorkflowBell.this;
            if (!workflowBell5.mMobileNetwork || workflowBell5.mSharedInfo.getHttpResponse().getStatusCode() == 511) {
                if (!TextUtils.isEmpty(WorkflowBell.this.mTelephonyAdapter.getMsisdn())) {
                    WorkflowBell workflowBell6 = WorkflowBell.this;
                    workflowBell6.mSharedInfo.addHttpParam("msisdn", workflowBell6.mParamHandler.encodeRFC3986(workflowBell6.mTelephonyAdapter.getMsisdn()));
                }
                if (!TextUtils.isEmpty(WorkflowBell.this.mSharedInfo.getUserMsisdn())) {
                    WorkflowBell workflowBell7 = WorkflowBell.this;
                    SharedInfo sharedInfo = workflowBell7.mSharedInfo;
                    sharedInfo.addHttpParam("msisdn", workflowBell7.mParamHandler.encodeRFC3986(sharedInfo.getUserMsisdn()));
                }
                WorkflowBell workflowBell8 = WorkflowBell.this;
                workflowBell8.mSharedInfo.addHttpParam(ConfigConstants.PNAME.SMS_PORT, workflowBell8.mTelephonyAdapter.getSmsDestPort());
                WorkflowBell workflowBell9 = WorkflowBell.this;
                workflowBell9.mSharedInfo.addHttpParam("token", workflowBell9.getToken());
            }
            WorkflowBell.this.mSharedInfo.addHttpParam("terminal_vendor", "SEC");
            WorkflowBell workflowBell10 = WorkflowBell.this;
            workflowBell10.mSharedInfo.addHttpParam("terminal_sw_version", workflowBell10.mParamHandler.getModelInfoFromBuildVersion(ConfigUtil.getModelName(workflowBell10.mPhoneId), ConfigConstants.PVALUE.TERMINAL_SW_VERSION, 8, true));
            WorkflowBell workflowBell11 = WorkflowBell.this;
            if (workflowBell11.mStartForce) {
                workflowBell11.mSharedInfo.addHttpParam("vers", "0");
            }
            if (WorkflowBell.this.getOpMode() == WorkflowBase.OpMode.DORMANT) {
                Log.i(str, "DORMANT mode. use backup version :" + WorkflowBell.this.getVersionBackup());
                WorkflowBell workflowBell12 = WorkflowBell.this;
                workflowBell12.addEventLog(str + "DORMANT mode. use backup version :" + WorkflowBell.this.getVersionBackup());
                WorkflowBell workflowBell13 = WorkflowBell.this;
                workflowBell13.mSharedInfo.addHttpParam("vers", workflowBell13.getVersionBackup());
            }
            WorkflowBell workflowBell14 = WorkflowBell.this;
            workflowBell14.mSharedInfo.setHttpResponse(workflowBell14.getHttpResponse());
            if (WorkflowBell.this.mSharedInfo.getHttpResponse().getStatusCode() == 200) {
                Log.i(str, "200 OK received. try parsing");
                return new Parse();
            }
            if (WorkflowBell.this.mSharedInfo.getHttpResponse().getStatusCode() == 403) {
                if (!WorkflowBell.this.mSharedInfo.getHttpParams().containsKey("msisdn")) {
                    Log.i(str, "no msisdn. try to get user");
                    WorkflowBell workflowBell15 = WorkflowBell.this;
                    workflowBell15.addEventLog(str + "no msisdn. try to get user");
                    WorkflowBell.this.mPowerController.release();
                    WorkflowBell workflowBell16 = WorkflowBell.this;
                    String msisdn = workflowBell16.mDialog.getMsisdn(workflowBell16.mTelephonyAdapter.getSimCountryCode());
                    WorkflowBell.this.mPowerController.lock();
                    if (TextUtils.isEmpty(msisdn)) {
                        Log.i(str, "user didn't enter msisdn finish process");
                        return new Finish();
                    }
                    WorkflowBell.this.mSharedInfo.setUserMsisdn(msisdn);
                    return new Initialize();
                } else if (!TextUtils.isEmpty(WorkflowBell.this.mSharedInfo.getUserMsisdn())) {
                    Log.i(str, "wrong MSISDN from USER. try again after AUTO_CONFIG_RETRY_INTERVAL.");
                    WorkflowBell.this.setValidityTimer(300);
                    return new Finish();
                }
            }
            WorkflowBell workflowBell17 = WorkflowBell.this;
            return workflowBell17.handleResponse2(new Initialize(), new FetchHttps(), new Finish());
        }
    }

    protected class FetchOtp implements WorkflowBase.Workflow {
        protected FetchOtp() {
        }

        public WorkflowBase.Workflow run() throws Exception {
            IMSLog.i(WorkflowBell.LOG_TAG, WorkflowBell.this.mPhoneId, "FetchOtp:");
            WorkflowBell.this.mSharedInfo.setHttpClean();
            SharedInfo sharedInfo = WorkflowBell.this.mSharedInfo;
            sharedInfo.addHttpParam(ConfigConstants.PNAME.OTP, sharedInfo.getOtp());
            WorkflowBell workflowBell = WorkflowBell.this;
            workflowBell.mSharedInfo.setHttpResponse(workflowBell.getHttpResponse());
            if (WorkflowBell.this.mSharedInfo.getHttpResponse().getStatusCode() == 200) {
                return new Parse();
            }
            WorkflowBell workflowBell2 = WorkflowBell.this;
            return workflowBell2.handleResponse2(new Initialize(), new FetchHttps(), new Finish());
        }
    }

    protected class Authorize implements WorkflowBase.Workflow {
        protected Authorize() {
        }

        public WorkflowBase.Workflow run() throws Exception {
            IMSLog.i(WorkflowBell.LOG_TAG, WorkflowBell.this.mPhoneId, "get OTP & save it to shared info");
            WorkflowBell.this.mPowerController.release();
            String otp = WorkflowBell.this.mTelephonyAdapter.getOtp();
            if (otp == null) {
                WorkflowBell.this.setValidityTimer(0);
                return new Finish();
            }
            WorkflowBell.this.mSharedInfo.setOtp(otp);
            WorkflowBell.this.mPowerController.lock();
            return new FetchOtp();
        }
    }

    protected class Parse implements WorkflowBase.Workflow {
        protected Parse() {
        }

        public WorkflowBase.Workflow run() throws Exception {
            String str = WorkflowBell.LOG_TAG;
            IMSLog.i(str, WorkflowBell.this.mPhoneId, "Parse:");
            byte[] body = WorkflowBell.this.mSharedInfo.getHttpResponse().getBody();
            if (body == null) {
                body = "".getBytes();
            }
            Map<String, String> parse = WorkflowBell.this.mXmlParser.parse(new String(body, "utf-8"));
            if (parse == null) {
                throw new InvalidXmlException("no parsed xml data.");
            } else if (parse.get("root/vers/version") == null || parse.get("root/vers/validity") == null) {
                IMSLog.i(str, WorkflowBell.this.mPhoneId, "config xml must contain atleast 2 items(version & validity).");
                WorkflowBell workflowBell = WorkflowBell.this;
                if (workflowBell.mCookieHandler.isCookie(workflowBell.mSharedInfo.getHttpResponse())) {
                    return new Authorize();
                }
                throw new UnknownStatusException("no body & no cookie. something wrong");
            } else {
                WorkflowBell.this.mSharedInfo.setParsedXml(parse);
                return new Store();
            }
        }
    }

    protected class Store implements WorkflowBase.Workflow {
        protected Store() {
        }

        public WorkflowBase.Workflow run() throws Exception {
            IMSLog.i(WorkflowBell.LOG_TAG, WorkflowBell.this.mPhoneId, "Store:");
            WorkflowBell workflowBell = WorkflowBell.this;
            WorkflowParamHandler workflowParamHandler = workflowBell.mParamHandler;
            workflowParamHandler.setOpModeWithUserAccept(workflowParamHandler.getUserAccept(workflowBell.mSharedInfo.getParsedXml()), WorkflowBell.this.mSharedInfo.getParsedXml(), WorkflowBase.OpMode.DISABLE);
            if (WorkflowBell.this.getOpMode() == WorkflowBase.OpMode.ACTIVE) {
                WorkflowBell workflowBell2 = WorkflowBell.this;
                workflowBell2.setValidityTimer(workflowBell2.getValidity());
            }
            return new Finish();
        }
    }

    protected class Finish implements WorkflowBase.Workflow {
        protected Finish() {
        }

        public WorkflowBase.Workflow run() throws Exception {
            if (WorkflowBell.this.mSharedInfo.getHttpResponse() != null) {
                WorkflowBell workflowBell = WorkflowBell.this;
                workflowBell.setLastErrorCode(workflowBell.mSharedInfo.getHttpResponse().getStatusCode());
            }
            IMSLog.i(WorkflowBell.LOG_TAG, WorkflowBell.this.mPhoneId, "all workflow finished");
            WorkflowBell.this.createSharedInfo();
            return null;
        }
    }
}
