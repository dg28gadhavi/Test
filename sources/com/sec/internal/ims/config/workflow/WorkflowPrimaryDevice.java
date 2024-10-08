package com.sec.internal.ims.config.workflow;

import android.content.Context;
import android.content.SharedPreferences;
import android.database.sqlite.SQLiteFullException;
import android.os.Message;
import android.text.TextUtils;
import android.util.Log;
import com.android.internal.util.ArrayUtils;
import com.sec.internal.constants.Mno;
import com.sec.internal.constants.ims.DiagnosisConstants;
import com.sec.internal.constants.ims.ImsConstants;
import com.sec.internal.constants.ims.config.ConfigConstants;
import com.sec.internal.helper.SimUtil;
import com.sec.internal.helper.header.AuthenticationHeaders;
import com.sec.internal.helper.header.WwwAuthenticateHeader;
import com.sec.internal.helper.httpclient.DigestAuth;
import com.sec.internal.helper.httpclient.HttpController;
import com.sec.internal.ims.config.ConfigContract;
import com.sec.internal.ims.config.SharedInfo;
import com.sec.internal.ims.config.exception.InvalidXmlException;
import com.sec.internal.ims.config.exception.NoInitialDataException;
import com.sec.internal.ims.config.exception.UnknownStatusException;
import com.sec.internal.ims.config.workflow.WorkflowBase;
import com.sec.internal.ims.registry.ImsRegistry;
import com.sec.internal.ims.servicemodules.ss.UtStateMachine;
import com.sec.internal.ims.util.ConfigUtil;
import java.io.UnsupportedEncodingException;
import java.nio.charset.StandardCharsets;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import org.json.JSONException;
import org.json.JSONObject;

public class WorkflowPrimaryDevice extends WorkflowBase {
    private static final String BODY = "";
    private static final String DIGEST_URI = "/";
    private static final String IMS_SWITCH = "imsswitch";
    private static final String LOG_TAG_BASE = WorkflowPrimaryDevice.class.getSimpleName();
    private static final String PASSWD = "";
    private static final String USER_NAME = "";
    /* access modifiers changed from: private */
    public String LOG_TAG;
    protected boolean mIsheaderEnrichment = false;
    SharedPreferences.OnSharedPreferenceChangeListener mRcsSwitchListener = new WorkflowPrimaryDevice$$ExternalSyntheticLambda0(this);
    protected boolean mRescheduleValidityTimer = false;

    /* access modifiers changed from: protected */
    public WorkflowBase.Workflow getNextWorkflow(int i) {
        return null;
    }

    /* access modifiers changed from: private */
    public /* synthetic */ void lambda$new$0(SharedPreferences sharedPreferences, String str) {
        Log.i(this.LOG_TAG, "mRcsSwitchListener onChange");
        if (ImsConstants.SystemSettings.RCS_USER_SETTING1.getName().equals(str)) {
            sendMessage(obtainMessage(10, Boolean.valueOf(sharedPreferences.getBoolean(str, false))));
        }
    }

    /* JADX WARNING: Illegal instructions before constructor call */
    /* Code decompiled incorrectly, please refer to instructions dump. */
    public WorkflowPrimaryDevice(android.os.Looper r14, android.content.Context r15, com.sec.internal.interfaces.ims.config.IConfigModule r16, com.sec.internal.constants.Mno r17, com.sec.internal.interfaces.ims.config.ITelephonyAdapter r18, int r19) {
        /*
            r13 = this;
            r11 = r13
            r12 = r19
            com.sec.internal.ims.config.adapters.StorageAdapter r6 = new com.sec.internal.ims.config.adapters.StorageAdapter
            r6.<init>()
            com.sec.internal.ims.config.adapters.HttpAdapter r7 = new com.sec.internal.ims.config.adapters.HttpAdapter
            r7.<init>(r12)
            com.sec.internal.ims.config.adapters.XmlParserAdapter r8 = new com.sec.internal.ims.config.adapters.XmlParserAdapter
            r8.<init>()
            com.sec.internal.ims.config.adapters.DialogAdapter r9 = new com.sec.internal.ims.config.adapters.DialogAdapter
            r2 = r15
            r3 = r16
            r9.<init>(r15, r3)
            r0 = r13
            r1 = r14
            r4 = r17
            r5 = r18
            r10 = r19
            r0.<init>(r1, r2, r3, r4, r5, r6, r7, r8, r9, r10)
            java.lang.String r0 = LOG_TAG_BASE
            r11.LOG_TAG = r0
            r1 = 0
            r11.mIsheaderEnrichment = r1
            r11.mRescheduleValidityTimer = r1
            com.sec.internal.ims.config.workflow.WorkflowPrimaryDevice$$ExternalSyntheticLambda0 r1 = new com.sec.internal.ims.config.workflow.WorkflowPrimaryDevice$$ExternalSyntheticLambda0
            r1.<init>(r13)
            r11.mRcsSwitchListener = r1
            com.sec.internal.constants.Mno r1 = r11.mMno
            boolean r1 = com.sec.internal.ims.util.ConfigUtil.isRcsEur((com.sec.internal.constants.Mno) r1)
            if (r1 == 0) goto L_0x0040
            r13.registerListenersAndObservers()
        L_0x0040:
            java.lang.StringBuilder r1 = new java.lang.StringBuilder
            r1.<init>()
            r1.append(r0)
            java.lang.String r0 = "["
            r1.append(r0)
            r1.append(r12)
            java.lang.String r0 = "]"
            r1.append(r0)
            java.lang.String r0 = r1.toString()
            r11.LOG_TAG = r0
            return
        */
        throw new UnsupportedOperationException("Method not decompiled: com.sec.internal.ims.config.workflow.WorkflowPrimaryDevice.<init>(android.os.Looper, android.content.Context, com.sec.internal.interfaces.ims.config.IConfigModule, com.sec.internal.constants.Mno, com.sec.internal.interfaces.ims.config.ITelephonyAdapter, int):void");
    }

    public void handleMessage(Message message) {
        int i = message.what;
        if (i != 5) {
            if (i != 10) {
                super.handleMessage(message);
            } else if (!((Boolean) message.obj).booleanValue() || !this.mRescheduleValidityTimer) {
                cancelValidityTimer();
                this.mRescheduleValidityTimer = true;
            } else {
                Log.i(this.LOG_TAG, "Rescheduling validity timer due to RCS switch change");
                this.mRescheduleValidityTimer = false;
                scheduleAutoconfig(getVersion());
            }
        } else if (!ConfigUtil.isSecDmaPackageInuse(this.mContext, this.mPhoneId)) {
            Log.i(this.LOG_TAG, "sms default application is changed to non-samsung, cancel validity timer");
            cancelValidityTimer();
        } else if (!ImsRegistry.isRcsEnabledByPhoneId(this.mPhoneId) || !ConfigUtil.isRcsAvailable(this.mContext, this.mPhoneId, this.mSm)) {
            this.mRescheduleValidityTimer = true;
        } else {
            Log.i(this.LOG_TAG, "sms default application is changed to samsung, schedule autoconf");
            scheduleAutoconfig(getVersion());
        }
    }

    private void registerListenersAndObservers() {
        Context context = this.mContext;
        context.getSharedPreferences("imsswitch_" + this.mPhoneId, 0).registerOnSharedPreferenceChangeListener(this.mRcsSwitchListener);
    }

    private void unregisterListenersAndObservers() {
        Context context = this.mContext;
        context.getSharedPreferences("imsswitch_" + this.mPhoneId, 0).unregisterOnSharedPreferenceChangeListener(this.mRcsSwitchListener);
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
                Log.i(this.LOG_TAG, "NoInitialDataException occur:" + e.getMessage());
                Log.i(this.LOG_TAG, "wait 10 sec. and retry");
                sleep(10000);
                workflow = new Initialize();
                e.printStackTrace();
                initialize = workflow;
                i--;
            } catch (UnknownStatusException e2) {
                Log.i(this.LOG_TAG, "UnknownStatusException occur:" + e2.getMessage());
                Log.i(this.LOG_TAG, "wait 2 sec. and retry");
                sleep(UtStateMachine.HTTP_READ_TIMEOUT_GCF);
                workflow = new Initialize();
                e2.printStackTrace();
                initialize = workflow;
                i--;
            } catch (SQLiteFullException e3) {
                Log.i(this.LOG_TAG, "SQLiteFullException occur:" + e3.getMessage());
                Log.i(this.LOG_TAG, "finish workflow");
                workflow = new Finish();
                e3.printStackTrace();
                initialize = workflow;
                i--;
            } catch (Exception e4) {
                if (e4.getMessage() != null) {
                    Log.i(this.LOG_TAG, "unknown exception occur:" + e4.getMessage());
                }
                Log.i(this.LOG_TAG, "wait 1 sec. and retry");
                sleep(1000);
                workflow = new Initialize();
                e4.printStackTrace();
                initialize = workflow;
                i--;
            }
            i--;
        }
    }

    class Initialize implements WorkflowBase.Workflow {
        Initialize() {
        }

        public WorkflowBase.Workflow run() throws Exception {
            WorkflowBase.Workflow workflow;
            WorkflowPrimaryDevice workflowPrimaryDevice = WorkflowPrimaryDevice.this;
            workflowPrimaryDevice.mSharedInfo.setUrl(workflowPrimaryDevice.mParamHandler.initUrl());
            WorkflowPrimaryDevice.this.mCookieHandler.clearCookie();
            WorkflowPrimaryDevice workflowPrimaryDevice2 = WorkflowPrimaryDevice.this;
            if (workflowPrimaryDevice2.mStartForce) {
                workflowPrimaryDevice2.setToken("", DiagnosisConstants.RCSA_TDRE.FORCE_ACS);
                workflow = new FetchHttp();
            } else {
                int i = AnonymousClass1.$SwitchMap$com$sec$internal$ims$config$workflow$WorkflowBase$OpMode[workflowPrimaryDevice2.getOpMode().ordinal()];
                workflow = (i == 1 || i == 2 || i == 3) ? new FetchHttp() : (i == 4 || i == 5) ? new Finish() : null;
            }
            if (!(workflow instanceof FetchHttp)) {
                return workflow;
            }
            WorkflowPrimaryDevice workflowPrimaryDevice3 = WorkflowPrimaryDevice.this;
            if (workflowPrimaryDevice3.mMobileNetwork) {
                return workflow;
            }
            Log.i(workflowPrimaryDevice3.LOG_TAG, "now use wifi. try non-ps step directly.");
            return new FetchHttps();
        }
    }

    /* renamed from: com.sec.internal.ims.config.workflow.WorkflowPrimaryDevice$1  reason: invalid class name */
    static /* synthetic */ class AnonymousClass1 {
        static final /* synthetic */ int[] $SwitchMap$com$sec$internal$ims$config$workflow$WorkflowBase$OpMode;

        /* JADX WARNING: Can't wrap try/catch for region: R(14:0|1|2|3|4|5|6|7|8|9|10|11|12|(3:13|14|16)) */
        /* JADX WARNING: Can't wrap try/catch for region: R(16:0|1|2|3|4|5|6|7|8|9|10|11|12|13|14|16) */
        /* JADX WARNING: Failed to process nested try/catch */
        /* JADX WARNING: Missing exception handler attribute for start block: B:11:0x003e */
        /* JADX WARNING: Missing exception handler attribute for start block: B:13:0x0049 */
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
                int[] r0 = $SwitchMap$com$sec$internal$ims$config$workflow$WorkflowBase$OpMode     // Catch:{ NoSuchFieldError -> 0x0049 }
                com.sec.internal.ims.config.workflow.WorkflowBase$OpMode r1 = com.sec.internal.ims.config.workflow.WorkflowBase.OpMode.ENABLE_RCS_BY_USER     // Catch:{ NoSuchFieldError -> 0x0049 }
                int r1 = r1.ordinal()     // Catch:{ NoSuchFieldError -> 0x0049 }
                r2 = 6
                r0[r1] = r2     // Catch:{ NoSuchFieldError -> 0x0049 }
            L_0x0049:
                int[] r0 = $SwitchMap$com$sec$internal$ims$config$workflow$WorkflowBase$OpMode     // Catch:{ NoSuchFieldError -> 0x0054 }
                com.sec.internal.ims.config.workflow.WorkflowBase$OpMode r1 = com.sec.internal.ims.config.workflow.WorkflowBase.OpMode.DISABLE_RCS_BY_USER     // Catch:{ NoSuchFieldError -> 0x0054 }
                int r1 = r1.ordinal()     // Catch:{ NoSuchFieldError -> 0x0054 }
                r2 = 7
                r0[r1] = r2     // Catch:{ NoSuchFieldError -> 0x0054 }
            L_0x0054:
                return
            */
            throw new UnsupportedOperationException("Method not decompiled: com.sec.internal.ims.config.workflow.WorkflowPrimaryDevice.AnonymousClass1.<clinit>():void");
        }
    }

    class FetchHttp implements WorkflowBase.Workflow {
        FetchHttp() {
        }

        public WorkflowBase.Workflow run() throws Exception {
            WorkflowPrimaryDevice.this.mSharedInfo.setHttpDefault();
            WorkflowPrimaryDevice workflowPrimaryDevice = WorkflowPrimaryDevice.this;
            workflowPrimaryDevice.mSharedInfo.setHttpResponse(workflowPrimaryDevice.getHttpResponse());
            int statusCode = WorkflowPrimaryDevice.this.mSharedInfo.getHttpResponse().getStatusCode();
            if (statusCode == 200 || statusCode == 511) {
                if (statusCode == 511) {
                    WorkflowPrimaryDevice.this.mIsheaderEnrichment = true;
                }
                return new FetchHttps();
            }
            WorkflowPrimaryDevice workflowPrimaryDevice2 = WorkflowPrimaryDevice.this;
            return workflowPrimaryDevice2.handleResponse2(new Initialize(), new FetchHttps(), new Finish());
        }
    }

    class FetchHttps implements WorkflowBase.Workflow {
        FetchHttps() {
        }

        public WorkflowBase.Workflow run() throws Exception {
            WorkflowPrimaryDevice.this.setSharedInfoWithParam();
            int statusCode = WorkflowPrimaryDevice.this.mSharedInfo.getHttpResponse().getStatusCode();
            if (statusCode == 200) {
                byte[] body = WorkflowPrimaryDevice.this.mSharedInfo.getHttpResponse().getBody();
                if (ArrayUtils.isEmpty(body)) {
                    Log.i(WorkflowPrimaryDevice.this.LOG_TAG, "200 OK received. Body empty or null. Start Parsing.");
                    return new Parse();
                }
                String str = new String(body, StandardCharsets.UTF_8);
                try {
                    new JSONObject(str.substring(str.indexOf("{"), str.lastIndexOf("}") + 1));
                } catch (Exception unused) {
                    Log.d(WorkflowPrimaryDevice.this.LOG_TAG, "200 OK received. Body non-empty, but not Json either. Start Parsing");
                    return new Parse();
                }
            } else if (statusCode == 403) {
                WorkflowPrimaryDevice workflowPrimaryDevice = WorkflowPrimaryDevice.this;
                if (workflowPrimaryDevice.mMno == Mno.BELL || (workflowPrimaryDevice.mMobileNetwork && !workflowPrimaryDevice.mIsheaderEnrichment)) {
                    Log.i(workflowPrimaryDevice.LOG_TAG, "403 received. Finish");
                    return new Finish();
                } else if (!workflowPrimaryDevice.mSharedInfo.getHttpParams().containsKey("msisdn")) {
                    Log.i(WorkflowPrimaryDevice.this.LOG_TAG, "no msisdn. try to get user");
                    WorkflowPrimaryDevice.this.mPowerController.release();
                    WorkflowPrimaryDevice workflowPrimaryDevice2 = WorkflowPrimaryDevice.this;
                    String msisdn = workflowPrimaryDevice2.mDialog.getMsisdn(workflowPrimaryDevice2.mTelephonyAdapter.getSimCountryCode());
                    WorkflowPrimaryDevice.this.mPowerController.lock();
                    if (TextUtils.isEmpty(msisdn)) {
                        Log.i(WorkflowPrimaryDevice.this.LOG_TAG, "user didn't enter msisdn finish process");
                        return new Finish();
                    }
                    WorkflowPrimaryDevice.this.mSharedInfo.setUserMsisdn(msisdn);
                    return new Initialize();
                } else if (!TextUtils.isEmpty(WorkflowPrimaryDevice.this.mSharedInfo.getUserMsisdn())) {
                    Log.i(WorkflowPrimaryDevice.this.LOG_TAG, "wrong MSISDN from USER. try again after 300");
                    WorkflowPrimaryDevice.this.setValidityTimer(300);
                    WorkflowPrimaryDevice.this.mMsisdnHandler.setMsisdnValue("");
                    return new Finish();
                }
            }
            WorkflowPrimaryDevice workflowPrimaryDevice3 = WorkflowPrimaryDevice.this;
            return workflowPrimaryDevice3.handleResponse2(new Initialize(), new FetchHttps(), new Finish());
        }
    }

    /* access modifiers changed from: protected */
    public void setSharedInfoWithParam() {
        this.mSharedInfo.setHttpsDefault();
        this.mCookieHandler.clearCookie();
        if (this.mParamHandler.isConfigProxy()) {
            this.mSharedInfo.changeConfigProxyUriForHttp();
            this.mSharedInfo.setHttpProxyDefault();
        }
        if (this.mMobileNetwork && this.mSharedInfo.getHttpResponse().getHeader().get(HttpController.HEADER_SET_COOKIE) != null) {
            this.mCookieHandler.handleCookie(this.mSharedInfo.getHttpResponse());
        }
        this.mSharedInfo.addHttpParam("vers", String.valueOf(getVersion()));
        this.mSharedInfo.addHttpParam("IMSI", this.mTelephonyAdapter.getSubscriberId(SimUtil.getSubId(this.mPhoneId)));
        this.mSharedInfo.addHttpParam(ConfigConstants.PNAME.IMEI, this.mTelephonyAdapter.getDeviceId(this.mPhoneId));
        this.mSharedInfo.addHttpParam("terminal_model", ConfigContract.BUILD.getTerminalModel());
        this.mSharedInfo.addHttpParam("terminal_vendor", "SEC");
        this.mSharedInfo.addHttpParam("terminal_sw_version", this.mParamHandler.getModelInfoFromCarrierVersion(ConfigUtil.getModelName(this.mPhoneId), ConfigConstants.PVALUE.TERMINAL_SW_VERSION, 8, true));
        this.mSharedInfo.addHttpParam("default_sms_app", ConfigUtil.isSecDmaPackageInuse(this.mContext, this.mPhoneId) ? "1" : "2");
        setSharedInfoWithAuthParam();
        setOpenIdAuthParams();
        if (this.mStartForce) {
            this.mSharedInfo.addHttpParam("vers", "0");
        }
        if (getOpMode() == WorkflowBase.OpMode.DORMANT) {
            String str = this.LOG_TAG;
            Log.i(str, "DORMANT mode. use backup version :" + getVersionBackup());
            this.mSharedInfo.addHttpParam("vers", getVersionBackup());
        }
        this.mSharedInfo.setHttpResponse(getHttpResponse());
    }

    /* access modifiers changed from: protected */
    public void setOpenIdAuthParams() {
        if (this.mSharedInfo.getHttpResponse() != null) {
            int statusCode = this.mSharedInfo.getHttpResponse().getStatusCode();
            if (statusCode == 401) {
                Optional.ofNullable(this.mSharedInfo.getHttpResponse().getHeader().get("WWW-Authenticate")).map(new WorkflowPrimaryDevice$$ExternalSyntheticLambda2()).ifPresent(new WorkflowPrimaryDevice$$ExternalSyntheticLambda3(this));
            } else if (statusCode == 302) {
                Log.d(this.LOG_TAG, "302 Received");
                Optional.ofNullable(this.mSharedInfo.getHttpResponse().getHeader().get("Location")).map(new WorkflowPrimaryDevice$$ExternalSyntheticLambda4()).ifPresent(new WorkflowPrimaryDevice$$ExternalSyntheticLambda5(this));
            } else if (statusCode == 200) {
                Optional.ofNullable(this.mSharedInfo.getHttpResponse().getBody()).ifPresent(new WorkflowPrimaryDevice$$ExternalSyntheticLambda6(this));
            }
        }
    }

    /* access modifiers changed from: private */
    public static /* synthetic */ String lambda$setOpenIdAuthParams$1(List list) {
        return (String) list.get(0);
    }

    /* access modifiers changed from: private */
    public /* synthetic */ void lambda$setOpenIdAuthParams$2(String str) {
        this.mSharedInfo.parseAkaParams(str);
        HashMap<String, String> aKAParams = this.mSharedInfo.getAKAParams();
        DigestAuth digestAuth = new DigestAuth();
        digestAuth.setDigestAuth("", "", aKAParams.get("realm"), aKAParams.get(WwwAuthenticateHeader.HEADER_PARAM_NONCE), "POST", DIGEST_URI, aKAParams.get(WwwAuthenticateHeader.HEADER_PARAM_ALGORITHM), aKAParams.get(AuthenticationHeaders.HEADER_PARAM_QOP), "");
        String createCnonce = DigestAuth.createCnonce();
        String resp = digestAuth.getResp();
        SharedInfo sharedInfo = this.mSharedInfo;
        sharedInfo.addHttpHeader("Authorization", Collections.singletonList(str + ",cnonce=" + createCnonce + ",response=" + resp));
    }

    /* access modifiers changed from: private */
    public static /* synthetic */ String lambda$setOpenIdAuthParams$3(List list) {
        return (String) list.get(0);
    }

    /* access modifiers changed from: private */
    public /* synthetic */ void lambda$setOpenIdAuthParams$5(String str) {
        String str2 = this.LOG_TAG;
        Log.d(str2, "TOP Location: " + str);
        Map<String, List<String>> header = this.mSharedInfo.getHttpResponse().getHeader();
        if (header.get("access_token") != null) {
            Log.d(this.LOG_TAG, "prepare for configuration request");
        } else if (header.get("code") != null) {
            Log.d(this.LOG_TAG, "should reach out token end point for access token");
        } else {
            this.mSharedInfo.parseOidcParams(str);
            this.mSharedInfo.getOidcParams().forEach(new WorkflowPrimaryDevice$$ExternalSyntheticLambda1(this));
        }
    }

    /* access modifiers changed from: private */
    public /* synthetic */ void lambda$setOpenIdAuthParams$4(String str, String str2) {
        this.mSharedInfo.addHttpParam(str, str2);
    }

    /* access modifiers changed from: private */
    public /* synthetic */ void lambda$setOpenIdAuthParams$6(byte[] bArr) {
        try {
            String str = new String(bArr, "UTF-8");
            if (str.indexOf("{") == -1 || str.lastIndexOf("}") == -1) {
                Log.d(this.LOG_TAG, "Not a JSON Body");
                return;
            }
            JSONObject jSONObject = new JSONObject(str.substring(str.indexOf("{"), str.lastIndexOf("}") + 1));
            if (jSONObject.has("access_token")) {
                this.mSharedInfo.addHttpParam("access_token", jSONObject.getString("access_token"));
                if (jSONObject.has(AuthenticationHeaders.HEADER_PARAM_ID_TOKEN)) {
                    this.mSharedInfo.addHttpParam(AuthenticationHeaders.HEADER_PARAM_ID_TOKEN, jSONObject.getString(AuthenticationHeaders.HEADER_PARAM_ID_TOKEN));
                }
                this.mSharedInfo.setUrl(this.mParamHandler.initUrl());
            }
        } catch (UnsupportedEncodingException unused) {
            Log.d(this.LOG_TAG, "fail to create a new string by UnsupportedEncodingException");
        } catch (JSONException unused2) {
            Log.d(this.LOG_TAG, "Not a JSON Body by JSONException");
        } catch (Exception unused3) {
            Log.d(this.LOG_TAG, "Not a JSON Body");
        }
    }

    /* access modifiers changed from: protected */
    public void setSharedInfoWithAuthParam() {
        if (!this.mMobileNetwork || this.mIsheaderEnrichment || this.mSharedInfo.getHttpResponse().getStatusCode() == 511) {
            if (!TextUtils.isEmpty(this.mSharedInfo.getUserMsisdn())) {
                SharedInfo sharedInfo = this.mSharedInfo;
                sharedInfo.addHttpParam("msisdn", this.mParamHandler.encodeRFC3986(sharedInfo.getUserMsisdn()));
            } else if (!TextUtils.isEmpty(this.mTelephonyAdapter.getMsisdn())) {
                this.mSharedInfo.addHttpParam("msisdn", this.mParamHandler.encodeRFC3986(this.mTelephonyAdapter.getMsisdn()));
            } else if (!TextUtils.isEmpty(this.mMsisdnHandler.getLastMsisdnValue())) {
                this.mSharedInfo.addHttpParam("msisdn", this.mMsisdnHandler.getLastMsisdnValue());
            }
            this.mSharedInfo.addHttpParam(ConfigConstants.PNAME.SMS_PORT, this.mTelephonyAdapter.getSmsDestPort());
            this.mSharedInfo.addHttpParam("token", getToken());
        }
    }

    class FetchOtp implements WorkflowBase.Workflow {
        FetchOtp() {
        }

        public WorkflowBase.Workflow run() throws Exception {
            WorkflowPrimaryDevice.this.mSharedInfo.setHttpClean();
            WorkflowPrimaryDevice.this.mCookieHandler.clearCookie();
            WorkflowPrimaryDevice workflowPrimaryDevice = WorkflowPrimaryDevice.this;
            workflowPrimaryDevice.mCookieHandler.handleCookie(workflowPrimaryDevice.mSharedInfo.getHttpResponse());
            SharedInfo sharedInfo = WorkflowPrimaryDevice.this.mSharedInfo;
            sharedInfo.addHttpParam(ConfigConstants.PNAME.OTP, sharedInfo.getOtp());
            WorkflowPrimaryDevice workflowPrimaryDevice2 = WorkflowPrimaryDevice.this;
            workflowPrimaryDevice2.mSharedInfo.setHttpResponse(workflowPrimaryDevice2.getHttpResponse());
            if (WorkflowPrimaryDevice.this.mSharedInfo.getHttpResponse().getStatusCode() == 200) {
                return new Parse();
            }
            WorkflowPrimaryDevice workflowPrimaryDevice3 = WorkflowPrimaryDevice.this;
            return workflowPrimaryDevice3.handleResponse2(new Initialize(), new FetchHttps(), new Finish());
        }
    }

    class Authorize implements WorkflowBase.Workflow {
        Authorize() {
        }

        public WorkflowBase.Workflow run() throws Exception {
            Log.i(WorkflowPrimaryDevice.this.LOG_TAG, "get OTP & save it to shared info");
            WorkflowPrimaryDevice.this.mPowerController.release();
            String otp = WorkflowPrimaryDevice.this.mTelephonyAdapter.getOtp();
            if (otp == null) {
                WorkflowPrimaryDevice.this.setValidityTimer(0);
                return new Finish();
            }
            WorkflowPrimaryDevice.this.mSharedInfo.setOtp(otp);
            WorkflowPrimaryDevice.this.mPowerController.lock();
            return new FetchOtp();
        }
    }

    class Parse implements WorkflowBase.Workflow {
        Parse() {
        }

        public WorkflowBase.Workflow run() throws Exception {
            byte[] body = WorkflowPrimaryDevice.this.mSharedInfo.getHttpResponse().getBody();
            if (body == null) {
                body = "".getBytes(StandardCharsets.UTF_8);
            }
            Map<String, String> parse = WorkflowPrimaryDevice.this.mXmlParser.parse(new String(body, "utf-8"));
            if (parse == null) {
                throw new InvalidXmlException("no parsed xml data.");
            } else if (parse.get("root/vers/version") == null || parse.get("root/vers/validity") == null) {
                Log.i(WorkflowPrimaryDevice.this.LOG_TAG, "config xml must contain atleast 2 items(version & validity).");
                WorkflowPrimaryDevice workflowPrimaryDevice = WorkflowPrimaryDevice.this;
                if (workflowPrimaryDevice.mCookieHandler.isCookie(workflowPrimaryDevice.mSharedInfo.getHttpResponse())) {
                    return new Authorize();
                }
                throw new UnknownStatusException("no body & no cookie. something wrong");
            } else {
                parse.put(ConfigConstants.PATH.RAW_CONFIG_XML_FILE, new String(body, "utf-8"));
                WorkflowPrimaryDevice.this.mSharedInfo.setParsedXml(parse);
                WorkflowPrimaryDevice workflowPrimaryDevice2 = WorkflowPrimaryDevice.this;
                workflowPrimaryDevice2.mMsisdnHandler.setMsisdnValue(workflowPrimaryDevice2.mSharedInfo.getUserMsisdn());
                return new Store();
            }
        }
    }

    class Store implements WorkflowBase.Workflow {
        Store() {
        }

        public WorkflowBase.Workflow run() throws Exception {
            WorkflowPrimaryDevice workflowPrimaryDevice = WorkflowPrimaryDevice.this;
            Map<String, String> userMessage = workflowPrimaryDevice.mParamHandler.getUserMessage(workflowPrimaryDevice.mSharedInfo.getParsedXml());
            boolean z = true;
            if (userMessage.size() == 4) {
                int i = AnonymousClass1.$SwitchMap$com$sec$internal$ims$config$workflow$WorkflowBase$OpMode[WorkflowPrimaryDevice.this.getOpMode().ordinal()];
                boolean z2 = false;
                boolean z3 = i == 1 || i == 3 || i == 6 || i == 7;
                WorkflowPrimaryDevice workflowPrimaryDevice2 = WorkflowPrimaryDevice.this;
                int version = workflowPrimaryDevice2.getVersion(workflowPrimaryDevice2.mSharedInfo.getParsedXml());
                boolean z4 = WorkflowPrimaryDevice.this.getVersion() != version;
                if (version < 1) {
                    z2 = true;
                }
                if ((!z4 || z3) && !z2) {
                    Log.i(WorkflowPrimaryDevice.this.LOG_TAG, "Previously working configuration available for this IMSI. Don't display T&C.");
                } else {
                    z = WorkflowPrimaryDevice.this.mParamHandler.getUserAcceptWithDialog(userMessage);
                }
            }
            WorkflowPrimaryDevice workflowPrimaryDevice3 = WorkflowPrimaryDevice.this;
            workflowPrimaryDevice3.mParamHandler.setOpModeWithUserAccept(z, workflowPrimaryDevice3.mSharedInfo.getParsedXml(), WorkflowBase.OpMode.DISABLE);
            if (WorkflowPrimaryDevice.this.getOpMode() == WorkflowBase.OpMode.ACTIVE) {
                WorkflowPrimaryDevice workflowPrimaryDevice4 = WorkflowPrimaryDevice.this;
                workflowPrimaryDevice4.setValidityTimer(workflowPrimaryDevice4.getValidity());
            }
            WorkflowPrimaryDevice.this.setTcUserAccept(z ? 1 : 0);
            return new Finish();
        }
    }

    class Finish implements WorkflowBase.Workflow {
        Finish() {
        }

        public WorkflowBase.Workflow run() throws Exception {
            if (WorkflowPrimaryDevice.this.mSharedInfo.getHttpResponse() != null) {
                WorkflowPrimaryDevice workflowPrimaryDevice = WorkflowPrimaryDevice.this;
                workflowPrimaryDevice.setLastErrorCode(workflowPrimaryDevice.mSharedInfo.getHttpResponse().getStatusCode());
            }
            Log.i(WorkflowPrimaryDevice.this.LOG_TAG, "all workflow finished");
            WorkflowPrimaryDevice.this.createSharedInfo();
            return null;
        }
    }

    public void cleanup() {
        super.cleanup();
        unregisterListenersAndObservers();
    }
}
