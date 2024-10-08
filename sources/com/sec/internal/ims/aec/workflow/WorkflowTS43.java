package com.sec.internal.ims.aec.workflow;

import android.content.Context;
import android.os.Handler;
import android.os.Looper;
import android.os.Message;
import android.text.TextUtils;
import com.sec.internal.constants.ims.aec.AECNamespace;
import com.sec.internal.helper.HttpRequest;
import com.sec.internal.helper.SimpleEventLog;
import com.sec.internal.helper.httpclient.HttpController;
import com.sec.internal.ims.aec.util.ContentParser;
import com.sec.internal.ims.aec.util.DataConnectivity;
import com.sec.internal.ims.aec.util.HttpClient;
import com.sec.internal.ims.aec.util.HttpStore;
import com.sec.internal.ims.aec.util.URLExtractor;
import com.sec.internal.ims.servicemodules.ss.UtStateMachine;
import com.sec.internal.imscr.LogClass;
import com.sec.internal.log.AECLog;
import com.sec.internal.log.IMSLog;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.Locale;
import java.util.Map;

class WorkflowTS43 extends WorkflowImpl {
    protected final int MAX_TRY_COUNT = 3;
    protected int mTryCnt = 3;

    protected interface Workflow {
        Workflow run() throws Exception;
    }

    WorkflowTS43(Context context, Looper looper, Handler handler, String str) {
        super(context, looper, handler, str);
    }

    /* access modifiers changed from: protected */
    public void requestEntitlement(int i) {
        String url = URLExtractor.getUrl(this.mContext, this.mPhoneId, this.mAECJar.getEntitlementDomain(), this.mAECJar.getEntitlementPort(), this.mAECJar.getEntitlementPath(), this.mAECJar.getDomainFromImpi());
        if (i < 0 || TextUtils.isEmpty(url) || this.mAECJar.getAppId().isEmpty()) {
            sendMessage(obtainMessage(1002, this.mPhoneId, this.mAECJar.getHttpResponse()));
            return;
        }
        this.mPowerCtrl.lock(90000);
        if (TextUtils.isEmpty(this.mHttpJar.getHttpUrl())) {
            this.mHttpJar.setUserAgent(this.mAECJar.getEntitlementVersion());
            this.mHttpJar.setHostName(URLExtractor.getHostName(url));
            if (this.mAECJar.getSNIInHeader()) {
                this.mHttpJar.setHttpUrl(url);
            } else {
                this.mHttpJar.setHttpUrls(URLExtractor.getIpAddress(this.mPhoneId, url, DataConnectivity.isWifiConnected(this.mContext) ? null : this.mPsDataOffExempt.getNetwork()));
                HttpStore httpStore = this.mHttpJar;
                httpStore.setHttpUrl(httpStore.getHttpUrls().poll());
            }
        }
        doWorkflow();
        this.mPowerCtrl.release();
    }

    /* access modifiers changed from: protected */
    public HttpClient.Response getHttpGetResponse(String str) throws Exception {
        this.mHttpClient.setHeaders(this.mHttpJar.getHttpHeaders());
        this.mHttpClient.setHostName(this.mHttpJar.getHostName());
        this.mHttpClient.setParams(this.mHttpJar.getHttpParams());
        this.mHttpClient.setNetwork(DataConnectivity.isWifiConnected(this.mContext) ? null : this.mPsDataOffExempt.getNetwork());
        HttpClient.Response uRLConnection = this.mHttpClient.getURLConnection(str);
        this.mHttpClient.closeURLConnection();
        return uRLConnection;
    }

    /* access modifiers changed from: protected */
    public HttpClient.Response getHttpPostResponse(String str) throws Exception {
        this.mHttpClient.setHeaders(this.mHttpJar.getHttpHeaders());
        this.mHttpClient.setHostName(this.mHttpJar.getHostName());
        this.mHttpClient.setPostData(this.mHttpJar.getHttpPostData());
        this.mHttpClient.setNetwork(DataConnectivity.isWifiConnected(this.mContext) ? null : this.mPsDataOffExempt.getNetwork());
        HttpClient.Response postURLConnection = this.mHttpClient.postURLConnection(str);
        this.mHttpClient.closeURLConnection();
        return postURLConnection;
    }

    /* access modifiers changed from: protected */
    public void doWorkflow() {
        Workflow initialize;
        Workflow initialize2 = new Initialize();
        while (initialize2 != null) {
            int i = this.mTryCnt;
            if (i >= 0) {
                if (i == 0) {
                    try {
                        HttpStore httpStore = this.mHttpJar;
                        httpStore.setHttpUrl(httpStore.getHttpUrls().poll());
                        if (TextUtils.isEmpty(this.mHttpJar.getHttpUrl())) {
                            initialize2 = new Stop();
                        } else {
                            clearAkaToken();
                            this.mTryCnt = 3;
                            this.mHttpJar.setEapChallengeResp("");
                            this.mPowerCtrl.sleep(6000);
                        }
                    } catch (IOException e) {
                        initialize = new Initialize();
                        String str = this.LOG_TAG;
                        AECLog.e(str, "doWorkflow: " + e.getMessage(), this.mPhoneId);
                        int i2 = this.mTryCnt + -1;
                        this.mTryCnt = i2;
                        if (i2 > 0) {
                            this.mPowerCtrl.sleep(((long) (3 - i2)) * UtStateMachine.HTTP_READ_TIMEOUT_GCF);
                        }
                        initialize2 = initialize;
                    } catch (Exception e2) {
                        initialize = new Stop();
                        String str2 = this.LOG_TAG;
                        AECLog.e(str2, "doWorkflow: " + e2.getMessage(), this.mPhoneId);
                        initialize2 = initialize;
                    }
                }
                initialize2 = initialize2.run();
            } else {
                return;
            }
        }
    }

    /* access modifiers changed from: protected */
    public Workflow handleNotOkResponse(int i) {
        long j;
        AECLog.i(this.LOG_TAG, "handleNotOkResponse: " + i, this.mPhoneId);
        IMSLog.c(LogClass.ES_HTTP_RESPONSE, this.mPhoneId + ", " + i);
        this.mEventLog.add("handleNotOkResponse[" + this.mTryCnt + "]: " + i);
        Initialize initialize = new Initialize();
        setValidEntitlement(false);
        if (!(i == 400 || i == 415 || i == 500)) {
            if (i == 503) {
                try {
                    String str = (String) this.mHttpJar.getHttpResponse().getHeader().get(HttpRequest.HEADER_RETRY_AFTER).get(0);
                    if (str.matches("[0-9]+")) {
                        j = (long) Integer.parseInt(str);
                    } else {
                        SimpleDateFormat simpleDateFormat = new SimpleDateFormat("EEE, dd MMM yyyy HH:mm:ss ZZZZ", Locale.ENGLISH);
                        Calendar instance = Calendar.getInstance();
                        instance.setTime(simpleDateFormat.parse(str));
                        long timeInMillis = instance.getTimeInMillis();
                        instance.setTime(simpleDateFormat.parse((String) this.mHttpJar.getHttpResponse().getHeader().get("Date").get(0)));
                        j = timeInMillis - instance.getTimeInMillis();
                    }
                    if (j <= 0) {
                        j = 10;
                    }
                    int i2 = this.mTryCnt - 1;
                    this.mTryCnt = i2;
                    if (i2 <= 0) {
                        return initialize;
                    }
                    AECLog.i(this.LOG_TAG, "Retry-After: " + j + " sec", this.mPhoneId);
                    this.mPowerCtrl.sleep(j * 1000);
                    return initialize;
                } catch (Exception e) {
                    AECLog.e(this.LOG_TAG, "Invalid Retry-After Header: " + e.getMessage(), this.mPhoneId);
                    return new Finish();
                }
            } else if (i == 511) {
                this.mTryCnt = 3;
                clearAkaToken();
                return new FetchEapId();
            } else if (!(i == 403 || i == 404)) {
                int i3 = this.mTryCnt - 1;
                this.mTryCnt = i3;
                if (i3 <= 0) {
                    return initialize;
                }
                this.mPowerCtrl.sleep(((long) (3 - i3)) * UtStateMachine.HTTP_READ_TIMEOUT_GCF);
                return initialize;
            }
        }
        clearAkaToken();
        return new Finish();
    }

    protected class Initialize implements Workflow {
        protected Initialize() {
        }

        public Workflow run() throws Exception {
            if (TextUtils.isEmpty(WorkflowTS43.this.mHttpJar.getHttpUrl())) {
                return new Stop();
            }
            if (!TextUtils.isEmpty(WorkflowTS43.this.getAkaToken())) {
                return new FetchToKen();
            }
            if (TextUtils.isEmpty(WorkflowTS43.this.mHttpJar.getEapChallengeResp())) {
                return new FetchEapId();
            }
            return new FetchEapChallengeResp();
        }
    }

    protected class FetchEapId implements Workflow {
        protected FetchEapId() {
        }

        public Workflow run() throws Exception {
            WorkflowTS43 workflowTS43 = WorkflowTS43.this;
            workflowTS43.mHttpJar.initHttpGetInfo(workflowTS43.mAECJar.getVersion(), WorkflowTS43.this.mAECJar.getEntitlementVersion());
            WorkflowTS43 workflowTS432 = WorkflowTS43.this;
            workflowTS432.mHttpJar.setHttpPushParam(workflowTS432.mAECJar.getNotifAction(), WorkflowTS43.this.mAECJar.getNotifToken());
            WorkflowTS43 workflowTS433 = WorkflowTS43.this;
            workflowTS433.mHttpJar.setHttpParam("EAP_ID", workflowTS433.mCalcEapAka.getImsiEap());
            WorkflowTS43 workflowTS434 = WorkflowTS43.this;
            HttpStore httpStore = workflowTS434.mHttpJar;
            httpStore.setHttpResponse(workflowTS434.getHttpGetResponse(httpStore.getHttpUrl()));
            WorkflowTS43 workflowTS435 = WorkflowTS43.this;
            workflowTS435.mAECJar.setHttpResponse(workflowTS435.mHttpJar.getHttpResponse().getStatusCode());
            if (WorkflowTS43.this.mHttpJar.getHttpResponse().getStatusCode() == 200) {
                return new ParseContent();
            }
            WorkflowTS43 workflowTS436 = WorkflowTS43.this;
            return workflowTS436.handleNotOkResponse(workflowTS436.mHttpJar.getHttpResponse().getStatusCode());
        }
    }

    protected class FetchEapChallengeResp implements Workflow {
        protected FetchEapChallengeResp() {
        }

        public Workflow run() throws Exception {
            HttpStore httpStore = WorkflowTS43.this.mHttpJar;
            httpStore.initHttpPostInfo(httpStore.getEapChallengeResp(), WorkflowTS43.this.mHttpJar.getHttpResponse().getHeader().get(HttpController.HEADER_SET_COOKIE));
            WorkflowTS43 workflowTS43 = WorkflowTS43.this;
            HttpStore httpStore2 = workflowTS43.mHttpJar;
            httpStore2.setHttpResponse(workflowTS43.getHttpPostResponse(httpStore2.getHttpUrl()));
            WorkflowTS43 workflowTS432 = WorkflowTS43.this;
            workflowTS432.mAECJar.setHttpResponse(workflowTS432.mHttpJar.getHttpResponse().getStatusCode());
            if (WorkflowTS43.this.mHttpJar.getHttpResponse().getStatusCode() == 200) {
                return new ParseContent();
            }
            WorkflowTS43 workflowTS433 = WorkflowTS43.this;
            return workflowTS433.handleNotOkResponse(workflowTS433.mHttpJar.getHttpResponse().getStatusCode());
        }
    }

    protected class FetchToKen implements Workflow {
        protected FetchToKen() {
        }

        public Workflow run() throws Exception {
            WorkflowTS43 workflowTS43 = WorkflowTS43.this;
            workflowTS43.mHttpJar.initHttpGetInfo(workflowTS43.mAECJar.getVersion(), WorkflowTS43.this.mAECJar.getEntitlementVersion());
            WorkflowTS43 workflowTS432 = WorkflowTS43.this;
            workflowTS432.mHttpJar.setHttpPushParam(workflowTS432.mAECJar.getNotifAction(), WorkflowTS43.this.mAECJar.getNotifToken());
            WorkflowTS43 workflowTS433 = WorkflowTS43.this;
            workflowTS433.mHttpJar.setHttpParam("token", workflowTS433.mAECJar.getAkaToken());
            WorkflowTS43 workflowTS434 = WorkflowTS43.this;
            workflowTS434.mHttpJar.setHttpParam("IMSI", workflowTS434.mAECJar.getImsi());
            WorkflowTS43 workflowTS435 = WorkflowTS43.this;
            HttpStore httpStore = workflowTS435.mHttpJar;
            httpStore.setHttpResponse(workflowTS435.getHttpGetResponse(httpStore.getHttpUrl()));
            WorkflowTS43 workflowTS436 = WorkflowTS43.this;
            workflowTS436.mAECJar.setHttpResponse(workflowTS436.mHttpJar.getHttpResponse().getStatusCode());
            if (WorkflowTS43.this.mHttpJar.getHttpResponse().getStatusCode() == 200) {
                return new ParseContent();
            }
            WorkflowTS43 workflowTS437 = WorkflowTS43.this;
            return workflowTS437.handleNotOkResponse(workflowTS437.mHttpJar.getHttpResponse().getStatusCode());
        }
    }

    protected class CalcEapChallenge implements Workflow {
        protected CalcEapChallenge() {
        }

        public Workflow run() throws Exception {
            Message obtainMessage = WorkflowTS43.this.obtainMessage();
            obtainMessage.what = 1005;
            WorkflowTS43 workflowTS43 = WorkflowTS43.this;
            workflowTS43.mCalcEapAka.requestEapChallengeResp(obtainMessage, workflowTS43.mHttpJar.getEapChallenge());
            return new WaitingEapChallengeResp();
        }
    }

    protected class WaitingEapChallengeResp implements Workflow {
        protected WaitingEapChallengeResp() {
        }

        public Workflow run() throws Exception {
            Message obtainMessage = WorkflowTS43.this.obtainMessage();
            obtainMessage.what = 1004;
            WorkflowTS43.this.sendMessage(obtainMessage);
            return null;
        }
    }

    protected class ParseContent implements Workflow {
        protected ParseContent() {
        }

        public Workflow run() throws Exception {
            if (ContentParser.hasEapRelayPacket(new String(WorkflowTS43.this.mHttpJar.getHttpResponse().getBody(), StandardCharsets.UTF_8))) {
                return new ParseEapChallenge();
            }
            return new ParseConfiguration();
        }
    }

    protected class ParseEapChallenge implements Workflow {
        protected ParseEapChallenge() {
        }

        public Workflow run() throws Exception {
            Map<String, String> parseJson = ContentParser.parseJson(new String(WorkflowTS43.this.mHttpJar.getHttpResponse().getBody(), StandardCharsets.UTF_8));
            if (parseJson.isEmpty()) {
                return new Stop();
            }
            ContentParser.debugPrint(WorkflowTS43.this.mPhoneId, parseJson);
            WorkflowTS43 workflowTS43 = WorkflowTS43.this;
            workflowTS43.mHttpJar.setEapChallenge(workflowTS43.mCalcEapAka.decodeChallenge(parseJson.get(AECNamespace.Path.EAP_RELAY_PACKET)));
            return new CalcEapChallenge();
        }
    }

    protected class ParseConfiguration implements Workflow {
        protected ParseConfiguration() {
        }

        public Workflow run() throws Exception {
            Map<String, String> map;
            String str = new String(WorkflowTS43.this.mHttpJar.getHttpResponse().getBody(), StandardCharsets.UTF_8);
            if (ContentParser.isJSONValid(WorkflowTS43.this.mHttpJar.getHttpResponse().getHeader().get("Content-Type")) || ContentParser.isJSONValid(str)) {
                map = ContentParser.parseJson(str);
            } else {
                map = ContentParser.parseXml(str);
            }
            if (!map.containsKey(AECNamespace.Path.VOLTE_MESSAGE_FOR_INCOMPATIBLE)) {
                map.put(AECNamespace.Path.VOLTE_MESSAGE_FOR_INCOMPATIBLE, "");
            }
            if (!map.containsKey(AECNamespace.Path.VOWIFI_MESSAGE_FOR_INCOMPATIBLE)) {
                map.put(AECNamespace.Path.VOWIFI_MESSAGE_FOR_INCOMPATIBLE, "");
            }
            if (map.isEmpty()) {
                return new Stop();
            }
            WorkflowTS43.this.mHttpJar.setParsedBody(map);
            return new StoreConfiguration();
        }
    }

    protected class StoreConfiguration implements Workflow {
        protected StoreConfiguration() {
        }

        public Workflow run() throws Exception {
            Map<String, String> parsedBody = WorkflowTS43.this.mHttpJar.getParsedBody();
            Date date = new Date(System.currentTimeMillis());
            SimpleDateFormat simpleDateFormat = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ssZZZZZ", Locale.getDefault());
            parsedBody.put(AECNamespace.Path.TIMESTAMP, simpleDateFormat.format(date));
            ContentParser.debugPrint(WorkflowTS43.this.mPhoneId, parsedBody);
            WorkflowTS43.this.mAECJar.setConfiguration(parsedBody);
            SimpleEventLog simpleEventLog = WorkflowTS43.this.mEventLog;
            simpleEventLog.add("StoreConfiguration: " + simpleDateFormat.format(date));
            if (WorkflowTS43.this.mAECJar.getEntitlementForVoLte()) {
                IMSLog.c(LogClass.ES_VOLTE_RESULT, WorkflowTS43.this.mPhoneId + ", " + WorkflowTS43.this.mAECJar.getVoLTEEntitlementStatus());
            }
            if (WorkflowTS43.this.mAECJar.getEntitlementForVoWiFi()) {
                IMSLog.c(LogClass.ES_VOWIFI_RESULT, WorkflowTS43.this.mPhoneId + ", " + WorkflowTS43.this.mAECJar.getVoWiFiActivationMode());
            }
            if (WorkflowTS43.this.mAECJar.getEntitlementForSMSoIp()) {
                IMSLog.c(LogClass.ES_SMSOIP_RESULT, WorkflowTS43.this.mPhoneId + ", " + WorkflowTS43.this.mAECJar.getSMSoIPEntitlementStatus());
            }
            return new Finish();
        }
    }

    protected class Stop implements Workflow {
        protected Stop() {
        }

        public Workflow run() throws Exception {
            WorkflowTS43 workflowTS43 = WorkflowTS43.this;
            workflowTS43.mTryCnt = 3;
            workflowTS43.mHttpJar.clearHttpStore();
            Message obtainMessage = WorkflowTS43.this.obtainMessage();
            obtainMessage.what = 1002;
            WorkflowTS43 workflowTS432 = WorkflowTS43.this;
            obtainMessage.arg1 = workflowTS432.mPhoneId;
            obtainMessage.arg2 = 0;
            workflowTS432.sendMessage(obtainMessage);
            return null;
        }
    }

    protected class Finish implements Workflow {
        protected Finish() {
        }

        public Workflow run() throws Exception {
            WorkflowTS43 workflowTS43 = WorkflowTS43.this;
            workflowTS43.mTryCnt = 3;
            workflowTS43.mHttpJar.clearHttpStore();
            Message obtainMessage = WorkflowTS43.this.obtainMessage();
            obtainMessage.what = 1003;
            WorkflowTS43.this.sendMessage(obtainMessage);
            return null;
        }
    }
}
