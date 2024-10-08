package com.sec.internal.ims.config.workflow;

import android.database.sqlite.SQLiteFullException;
import android.text.TextUtils;
import android.util.Log;
import com.sec.internal.constants.ims.config.ConfigConstants;
import com.sec.internal.ims.config.SharedInfo;
import com.sec.internal.ims.config.exception.InvalidXmlException;
import com.sec.internal.ims.config.exception.NoInitialDataException;
import com.sec.internal.ims.config.exception.UnknownStatusException;
import com.sec.internal.ims.config.workflow.WorkflowBase;
import com.sec.internal.ims.servicemodules.ss.UtStateMachine;
import com.sec.internal.ims.util.ConfigUtil;
import com.sec.internal.interfaces.ims.config.IStorageAdapter;
import java.net.ConnectException;
import java.util.Map;
import java.util.Optional;

public class WorkflowRjil extends WorkflowBase {
    public static final String LOG_TAG = WorkflowRjil.class.getSimpleName();

    /* access modifiers changed from: protected */
    public WorkflowBase.Workflow getNextWorkflow(int i) {
        return null;
    }

    /* JADX WARNING: Illegal instructions before constructor call */
    /* Code decompiled incorrectly, please refer to instructions dump. */
    public WorkflowRjil(android.os.Looper r12, android.content.Context r13, com.sec.internal.interfaces.ims.config.IConfigModule r14, com.sec.internal.constants.Mno r15, int r16) {
        /*
            r11 = this;
            r2 = r13
            r3 = r14
            r10 = r16
            com.sec.internal.ims.config.adapters.TelephonyAdapterPrimaryDeviceRjil r5 = new com.sec.internal.ims.config.adapters.TelephonyAdapterPrimaryDeviceRjil
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
        throw new UnsupportedOperationException("Method not decompiled: com.sec.internal.ims.config.workflow.WorkflowRjil.<init>(android.os.Looper, android.content.Context, com.sec.internal.interfaces.ims.config.IConfigModule, com.sec.internal.constants.Mno, int):void");
    }

    /* access modifiers changed from: package-private */
    public void work() {
        Initialize initialize;
        WorkflowBase.Workflow initialize2 = new Initialize();
        int i = WorkflowBase.AUTO_CONFIG_MAX_FLOWCOUNT;
        while (!this.mNeedToStopWork && initialize2 != null && i > 0) {
            try {
                initialize2 = initialize2.run();
            } catch (NoInitialDataException e) {
                Log.i(LOG_TAG, "NoInitialDataException occur:" + e.getMessage());
                Log.i(LOG_TAG, "wait 10 sec. and retry");
                sleep(10000);
                initialize = new Initialize();
                e.printStackTrace();
                initialize2 = initialize;
                i--;
            } catch (UnknownStatusException e2) {
                Log.i(LOG_TAG, "UnknownStatusException occur:" + e2.getMessage());
                Log.i(LOG_TAG, "wait 2 sec. and retry");
                sleep(UtStateMachine.HTTP_READ_TIMEOUT_GCF);
                initialize = new Initialize();
                e2.printStackTrace();
                initialize2 = initialize;
                i--;
            } catch (ConnectException e3) {
                Log.i(LOG_TAG, "ConnectException occur:" + e3.getMessage());
                Log.i(LOG_TAG, "wait 2 sec. and retry");
                sleep(UtStateMachine.HTTP_READ_TIMEOUT_GCF);
                initialize = new Initialize();
                e3.printStackTrace();
                initialize2 = initialize;
                i--;
            } catch (SQLiteFullException e4) {
                Log.i(LOG_TAG, "SQLiteFullException occur:" + e4.getMessage());
                Log.i(LOG_TAG, "finish");
                e4.printStackTrace();
            } catch (Exception e5) {
                Log.i(LOG_TAG, "unknown exception occur:" + e5.getMessage());
                Log.i(LOG_TAG, "wait 1 sec. and retry");
                sleep(1000);
                initialize = new Initialize();
                e5.printStackTrace();
                initialize2 = initialize;
                i--;
            }
            i--;
        }
        if (this.mNeedToStopWork) {
            Log.i(LOG_TAG, "work interrupted");
            this.mNeedToStopWork = false;
        }
    }

    /* access modifiers changed from: private */
    public static /* synthetic */ boolean lambda$getStorage$0(IStorageAdapter iStorageAdapter) {
        return iStorageAdapter.getState() == 1;
    }

    public IStorageAdapter getStorage() {
        return (IStorageAdapter) Optional.ofNullable(this.mStorage).filter(new WorkflowRjil$$ExternalSyntheticLambda0()).orElse((Object) null);
    }

    class Initialize implements WorkflowBase.Workflow {
        Initialize() {
        }

        public WorkflowBase.Workflow run() throws Exception {
            WorkflowBase.Workflow workflow;
            WorkflowRjil workflowRjil = WorkflowRjil.this;
            workflowRjil.mSharedInfo.setUrl(workflowRjil.mParamHandler.initUrl());
            WorkflowRjil.this.mCookieHandler.clearCookie();
            WorkflowRjil workflowRjil2 = WorkflowRjil.this;
            if (workflowRjil2.mStartForce) {
                workflow = new FetchHttp();
            } else {
                int i = AnonymousClass1.$SwitchMap$com$sec$internal$ims$config$workflow$WorkflowBase$OpMode[workflowRjil2.getOpMode().ordinal()];
                workflow = (i == 1 || i == 2 || i == 3) ? new FetchHttp() : (i == 4 || i == 5) ? new Finish() : null;
            }
            if (!(workflow instanceof FetchHttp) || WorkflowRjil.this.mMobileNetwork) {
                return workflow;
            }
            Log.i(WorkflowRjil.LOG_TAG, "now use wifi. try non-ps step directly.");
            return new FetchHttps();
        }
    }

    /* renamed from: com.sec.internal.ims.config.workflow.WorkflowRjil$1  reason: invalid class name */
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
            throw new UnsupportedOperationException("Method not decompiled: com.sec.internal.ims.config.workflow.WorkflowRjil.AnonymousClass1.<clinit>():void");
        }
    }

    class FetchHttp implements WorkflowBase.Workflow {
        FetchHttp() {
        }

        public WorkflowBase.Workflow run() throws Exception {
            WorkflowRjil.this.mSharedInfo.setHttpDefault();
            WorkflowRjil workflowRjil = WorkflowRjil.this;
            workflowRjil.mSharedInfo.setHttpResponse(workflowRjil.getHttpResponse());
            if (WorkflowRjil.this.mSharedInfo.getHttpResponse().getStatusCode() == 511) {
                return new FetchHttps();
            }
            WorkflowRjil workflowRjil2 = WorkflowRjil.this;
            return workflowRjil2.handleResponse2(new Initialize(), new FetchHttps(), new Finish());
        }
    }

    class FetchHttps implements WorkflowBase.Workflow {
        FetchHttps() {
        }

        public WorkflowBase.Workflow run() throws Exception {
            WorkflowRjil.this.mSharedInfo.setHttpsDefault();
            WorkflowRjil workflowRjil = WorkflowRjil.this;
            workflowRjil.mCookieHandler.handleCookie(workflowRjil.mSharedInfo.getHttpResponse());
            WorkflowRjil workflowRjil2 = WorkflowRjil.this;
            workflowRjil2.mSharedInfo.addHttpParam("vers", String.valueOf(workflowRjil2.getVersion()));
            WorkflowRjil workflowRjil3 = WorkflowRjil.this;
            workflowRjil3.mSharedInfo.addHttpParam("IMSI", workflowRjil3.mTelephonyAdapter.getImsi());
            WorkflowRjil workflowRjil4 = WorkflowRjil.this;
            workflowRjil4.mSharedInfo.addHttpParam(ConfigConstants.PNAME.IMEI, workflowRjil4.mTelephonyAdapter.getImei());
            WorkflowRjil workflowRjil5 = WorkflowRjil.this;
            workflowRjil5.mSharedInfo.addHttpParam(ConfigConstants.PNAME.RJIL_TOKEN, workflowRjil5.mParamHandler.encodeRFC7254(workflowRjil5.mTelephonyAdapter.getImei()));
            WorkflowRjil workflowRjil6 = WorkflowRjil.this;
            workflowRjil6.mSharedInfo.addHttpParam(ConfigConstants.PNAME.SIM_MODE, workflowRjil6.mTelephonyAdapter.getMnc());
            WorkflowRjil.this.mSharedInfo.addHttpParam("terminal_model", ConfigConstants.BUILD.TERMINAL_MODEL);
            SharedInfo sharedInfo = WorkflowRjil.this.mSharedInfo;
            sharedInfo.addHttpParam("client_version", "RCSAndJIO-" + WorkflowRjil.this.mClientVersion);
            WorkflowRjil.this.mSharedInfo.addHttpParam("default_sms_app", "1");
            WorkflowRjil workflowRjil7 = WorkflowRjil.this;
            if (!workflowRjil7.mMobileNetwork || workflowRjil7.mSharedInfo.getHttpResponse().getStatusCode() == 511) {
                String userMsisdn = WorkflowRjil.this.mSharedInfo.getUserMsisdn();
                if (TextUtils.isEmpty(userMsisdn)) {
                    userMsisdn = WorkflowRjil.this.mTelephonyAdapter.getMsisdn();
                }
                if (!TextUtils.isEmpty(userMsisdn)) {
                    WorkflowRjil workflowRjil8 = WorkflowRjil.this;
                    workflowRjil8.mSharedInfo.addHttpParam("msisdn", workflowRjil8.mParamHandler.encodeRFC3986(userMsisdn));
                }
                WorkflowRjil workflowRjil9 = WorkflowRjil.this;
                workflowRjil9.mSharedInfo.addHttpParam(ConfigConstants.PNAME.SMS_PORT, workflowRjil9.mTelephonyAdapter.getSmsDestPort());
                WorkflowRjil workflowRjil10 = WorkflowRjil.this;
                workflowRjil10.mSharedInfo.addHttpParam("token", workflowRjil10.getToken());
            }
            WorkflowRjil.this.mSharedInfo.addHttpParam("terminal_vendor", "SEC");
            WorkflowRjil workflowRjil11 = WorkflowRjil.this;
            workflowRjil11.mSharedInfo.addHttpParam("terminal_sw_version", workflowRjil11.mParamHandler.getModelInfoFromCarrierVersion(ConfigUtil.getModelName(workflowRjil11.mPhoneId), ConfigConstants.PVALUE.TERMINAL_SW_VERSION, 8, true));
            WorkflowRjil workflowRjil12 = WorkflowRjil.this;
            if (workflowRjil12.mStartForce) {
                workflowRjil12.mSharedInfo.addHttpParam("vers", "0");
            }
            if (WorkflowRjil.this.getOpMode() == WorkflowBase.OpMode.DORMANT) {
                String str = WorkflowRjil.LOG_TAG;
                Log.i(str, "DORMANT mode. use backup version :" + WorkflowRjil.this.getVersionBackup());
                WorkflowRjil workflowRjil13 = WorkflowRjil.this;
                workflowRjil13.mSharedInfo.addHttpParam("vers", workflowRjil13.getVersionBackup());
            }
            WorkflowRjil workflowRjil14 = WorkflowRjil.this;
            workflowRjil14.mSharedInfo.setHttpResponse(workflowRjil14.getHttpResponse());
            int statusCode = WorkflowRjil.this.mSharedInfo.getHttpResponse().getStatusCode();
            if (statusCode == 200) {
                Log.i(WorkflowRjil.LOG_TAG, "200 OK received. try parsing");
                return new Parse();
            } else if (statusCode == 403) {
                Log.i(WorkflowRjil.LOG_TAG, "403 received. Finish");
                return new Finish();
            } else {
                String str2 = WorkflowRjil.LOG_TAG;
                Log.i(str2, " http status : " + statusCode);
                WorkflowRjil workflowRjil15 = WorkflowRjil.this;
                return workflowRjil15.handleResponse2(new Initialize(), new FetchHttps(), new Finish());
            }
        }
    }

    class FetchOtp implements WorkflowBase.Workflow {
        FetchOtp() {
        }

        public WorkflowBase.Workflow run() throws Exception {
            WorkflowRjil.this.mSharedInfo.setHttpClean();
            SharedInfo sharedInfo = WorkflowRjil.this.mSharedInfo;
            sharedInfo.addHttpParam(ConfigConstants.PNAME.OTP, sharedInfo.getOtp());
            WorkflowRjil workflowRjil = WorkflowRjil.this;
            workflowRjil.mSharedInfo.setHttpResponse(workflowRjil.getHttpResponse());
            if (WorkflowRjil.this.mSharedInfo.getHttpResponse().getStatusCode() == 200) {
                return new Parse();
            }
            WorkflowRjil workflowRjil2 = WorkflowRjil.this;
            return workflowRjil2.handleResponse2(new Initialize(), new FetchHttps(), new Finish());
        }
    }

    class Authorize implements WorkflowBase.Workflow {
        Authorize() {
        }

        public WorkflowBase.Workflow run() throws Exception {
            Log.i(WorkflowRjil.LOG_TAG, "get OTP & save it to shared info");
            WorkflowRjil.this.mPowerController.release();
            WorkflowRjil.this.mTelephonyAdapter.registerUneregisterForOTP(true);
            String otp = WorkflowRjil.this.mTelephonyAdapter.getOtp();
            if (otp == null) {
                WorkflowRjil.this.mTelephonyAdapter.registerUneregisterForOTP(false);
                return new Finish();
            }
            WorkflowRjil.this.mTelephonyAdapter.registerUneregisterForOTP(false);
            WorkflowRjil.this.mSharedInfo.setOtp(otp);
            WorkflowRjil.this.mPowerController.lock();
            return new FetchOtp();
        }
    }

    class Parse implements WorkflowBase.Workflow {
        Parse() {
        }

        public WorkflowBase.Workflow run() throws Exception {
            byte[] body = WorkflowRjil.this.mSharedInfo.getHttpResponse().getBody();
            if (body == null) {
                body = "".getBytes();
            }
            Map<String, String> parse = WorkflowRjil.this.mXmlParser.parse(new String(body, "utf-8"));
            if (parse == null) {
                throw new InvalidXmlException("no parsed xml ConfigContract.");
            } else if (parse.get("root/vers/version") == null || parse.get("root/vers/validity") == null) {
                Log.i(WorkflowRjil.LOG_TAG, "config xml must contain atleast 2 items(version & validity).");
                WorkflowRjil workflowRjil = WorkflowRjil.this;
                if (workflowRjil.mCookieHandler.isCookie(workflowRjil.mSharedInfo.getHttpResponse())) {
                    return new Authorize();
                }
                throw new UnknownStatusException("no body & no cookie. something wrong");
            } else {
                WorkflowRjil.this.mSharedInfo.setParsedXml(parse);
                return new Store();
            }
        }
    }

    class Store implements WorkflowBase.Workflow {
        Store() {
        }

        public WorkflowBase.Workflow run() throws Exception {
            WorkflowRjil workflowRjil = WorkflowRjil.this;
            boolean userAccept = workflowRjil.mParamHandler.getUserAccept(workflowRjil.mSharedInfo.getParsedXml());
            WorkflowRjil workflowRjil2 = WorkflowRjil.this;
            workflowRjil2.mParamHandler.setOpModeWithUserAccept(userAccept, workflowRjil2.mSharedInfo.getParsedXml(), WorkflowBase.OpMode.DISABLE);
            if (WorkflowRjil.this.getOpMode() == WorkflowBase.OpMode.ACTIVE) {
                WorkflowRjil workflowRjil3 = WorkflowRjil.this;
                workflowRjil3.setValidityTimer(workflowRjil3.getValidity());
            }
            WorkflowRjil.this.setTcUserAccept(userAccept ? 1 : 0);
            return new Finish();
        }
    }

    class Finish implements WorkflowBase.Workflow {
        Finish() {
        }

        public WorkflowBase.Workflow run() throws Exception {
            if (WorkflowRjil.this.mSharedInfo.getHttpResponse() != null) {
                WorkflowRjil workflowRjil = WorkflowRjil.this;
                workflowRjil.setLastErrorCode(workflowRjil.mSharedInfo.getHttpResponse().getStatusCode());
            }
            Log.i(WorkflowRjil.LOG_TAG, "all workflow finished");
            WorkflowRjil.this.createSharedInfo();
            return null;
        }
    }
}
