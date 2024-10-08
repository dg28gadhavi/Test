package com.sec.internal.ims.cmstore.ambs.cloudrequest;

import android.text.TextUtils;
import android.util.Log;
import com.sec.internal.constants.ims.cmstore.ATTConstants;
import com.sec.internal.constants.ims.cmstore.enumprovision.EnumProvision;
import com.sec.internal.helper.httpclient.HttpRequestParams;
import com.sec.internal.helper.httpclient.HttpResponseParams;
import com.sec.internal.ims.cmstore.MessageStoreClient;
import com.sec.internal.ims.cmstore.ambs.globalsetting.BaseProvisionAPIRequest;
import com.sec.internal.ims.cmstore.helper.ATTGlobalVariables;
import com.sec.internal.ims.core.cmc.CmcConstants;
import com.sec.internal.interfaces.ims.cmstore.IAPICallFlowListener;
import com.sec.internal.interfaces.ims.cmstore.ICloudMessageManagerHelper;
import com.sec.internal.interfaces.ims.cmstore.IRetryStackAdapterHelper;
import com.sec.internal.log.IMSLog;
import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.URLEncoder;
import java.util.ArrayList;
import okhttp3.Cookie;
import okhttp3.HttpUrl;

public class RequestHUIToken extends BaseProvisionAPIRequest {
    private static final long serialVersionUID = -5155400496558292974L;
    /* access modifiers changed from: private */
    public String TAG = RequestHUIToken.class.getSimpleName();
    /* access modifiers changed from: private */
    public transient Cookie cookieServerIDInBody;
    /* access modifiers changed from: private */
    public transient Cookie cookieTokenInBody;

    public RequestHUIToken(final IAPICallFlowListener iAPICallFlowListener, MessageStoreClient messageStoreClient, ICloudMessageManagerHelper iCloudMessageManagerHelper) {
        super(iAPICallFlowListener, messageStoreClient);
        this.TAG += "[" + messageStoreClient.getClientID() + "]";
        setMethod(HttpRequestParams.Method.GET);
        updateUrl();
        setCallback(new HttpRequestParams.HttpRequestCallback() {
            public void onComplete(HttpResponseParams httpResponseParams) {
                String dataString = httpResponseParams.getDataString();
                String r1 = RequestHUIToken.this.TAG;
                Log.d(r1, "onComplete StatusCode: " + httpResponseParams.getStatusCode() + " strbody: " + IMSLog.checker(dataString));
                if (httpResponseParams.getStatusCode() != 200 || TextUtils.isEmpty(dataString)) {
                    if (httpResponseParams.getStatusCode() == 503 || httpResponseParams.getStatusCode() == 429) {
                        int access$1500 = RequestHUIToken.this.checkRetryAfter(httpResponseParams);
                        if (access$1500 > 0) {
                            iAPICallFlowListener.onOverRequest(this, ATTConstants.ATTErrorNames.ERR_RETRY_AFTER, access$1500);
                            return;
                        }
                        return;
                    }
                    RequestHUIToken.this.goFailedCall(ATTConstants.ATTErrorNames.ERR_HUI_JSON);
                } else if (dataString.indexOf(ATTConstants.ATTErrorNames.encoreesb) < 0) {
                    Log.d(RequestHUIToken.this.TAG, "NOT 6014");
                    RequestHUIToken.this.mStoreClient.getPrerenceManager().saveIfHUI6014Err(false);
                    String r8 = RequestHUIToken.this.getParameter(dataString, "msToken=\"", CmcConstants.E_NUM_STR_QUOTE);
                    String r2 = RequestHUIToken.this.getParameter(dataString, "serverID=\"", CmcConstants.E_NUM_STR_QUOTE);
                    String r4 = RequestHUIToken.this.getParameter(dataString, "redirectDomain=\"", CmcConstants.E_NUM_STR_QUOTE);
                    String r0 = RequestHUIToken.this.getParameter(dataString, "cometRedirectDomain=\"", CmcConstants.E_NUM_STR_QUOTE);
                    String r3 = RequestHUIToken.this.TAG;
                    Log.d(r3, "msToken=" + r8 + ", serverID=" + r2 + ", redirectDomain=" + r4 + ", cometRedirectDomain" + r0);
                    if (!ATTGlobalVariables.isGcmReplacePolling()) {
                        Cookie.Builder builder = new Cookie.Builder();
                        builder.domain(r4).name("MSToken").value(r8);
                        RequestHUIToken.this.cookieTokenInBody = builder.build();
                        Cookie.Builder builder2 = new Cookie.Builder();
                        builder2.domain(r4).name("SERVERID").value(r2);
                        RequestHUIToken.this.cookieServerIDInBody = builder2.build();
                    }
                    if (!TextUtils.isEmpty(r8) && !TextUtils.isEmpty(r4)) {
                        RequestHUIToken.this.mStoreClient.getPrerenceManager().saveMsgStoreSessionId(r8);
                        if (ATTGlobalVariables.isGcmReplacePolling()) {
                            String r82 = RequestHUIToken.this.TAG;
                            Log.d(r82, "nms value in SP =" + RequestHUIToken.this.mStoreClient.getPrerenceManager().getNmsHost());
                            RequestHUIToken.this.mStoreClient.getPrerenceManager().saveNmsHost(r4);
                        } else {
                            RequestHUIToken requestHUIToken = RequestHUIToken.this;
                            requestHUIToken.updateCookie(requestHUIToken.getUrl());
                            RequestHUIToken.this.mStoreClient.getPrerenceManager().saveNmsHost(r4);
                            RequestHUIToken.this.mStoreClient.getPrerenceManager().saveNcHost(r0);
                        }
                        String redirectDomain = RequestHUIToken.this.mStoreClient.getPrerenceManager().getRedirectDomain();
                        RequestHUIToken.this.mStoreClient.getPrerenceManager().saveRedirectDomain(r4);
                        if (!TextUtils.isEmpty(r4) && !TextUtils.isEmpty(redirectDomain) && !r4.equals(redirectDomain)) {
                            Log.d(RequestHUIToken.this.TAG, "redirect domain changed, need mail reset.");
                            iAPICallFlowListener.onGoToEvent(EnumProvision.ProvisionEventType.MAILBOX_MIGRATION_RESET.getId(), (Object) null);
                        }
                        RequestHUIToken.this.mStoreClient.getPrerenceManager().saveLastApiRequestCreateAccount(false);
                        RequestHUIToken.this.goSuccessfulCall();
                    }
                } else if (dataString.indexOf(ATTConstants.ATTErrorNames.ERR_ENCORE_METASWITCH_ACCOUNT_NOT_PROVISIONED) >= 0) {
                    RequestHUIToken.this.mStoreClient.getPrerenceManager().saveIfHUI6014Err(true);
                    if (RequestHUIToken.this.mStoreClient.getPrerenceManager().isLastAPIRequestCreateAccount()) {
                        Log.d(RequestHUIToken.this.TAG, "Last successful API call was CreateServiceAccount");
                        RequestHUIToken.this.goFailedCall(ATTConstants.ATTErrorNames.LAST_RETRY_CREATE_ACCOUNT);
                        return;
                    }
                    RequestHUIToken.this.goFailedCall(ATTConstants.ATTErrorNames.ERR_ENCORE_METASWITCH_ACCOUNT_NOT_PROVISIONED);
                } else {
                    RequestHUIToken.this.goFailedCall(ATTConstants.ATTErrorNames.ERR_HUI_JSON);
                }
            }

            public void onFail(IOException iOException) {
                String r0 = RequestHUIToken.this.TAG;
                Log.e(r0, "Http request onFail: " + iOException.getMessage());
                RequestHUIToken.this.goFailedCall();
            }
        });
    }

    /* access modifiers changed from: private */
    public String getParameter(String str, String str2, String str3) {
        int indexOf = str.indexOf(str2);
        if (indexOf < 0) {
            return null;
        }
        int length = indexOf + str2.length();
        int indexOf2 = str.indexOf(str3, length);
        return indexOf2 > 0 ? str.substring(length, indexOf2) : str.substring(length);
    }

    public void updateUrl() {
        try {
            StringBuilder sb = new StringBuilder();
            sb.append("https://");
            sb.append(ATTGlobalVariables.MSG_PROXY_HOST_NAME);
            sb.append("/encore/security/GetHUIMSToken?clientType=handset&ApplicationId=");
            sb.append(URLEncoder.encode(ATTGlobalVariables.APPLICATION_ID, "UTF-8"));
            sb.append("&ContextInfo=");
            sb.append(URLEncoder.encode("version=" + ATTGlobalVariables.VERSION_NAME, "UTF-8"));
            setUrl(sb.toString());
        } catch (UnsupportedEncodingException e) {
            Log.e(this.TAG, e.getMessage());
        }
    }

    /* access modifiers changed from: private */
    public void updateCookie(String str) {
        Log.d(this.TAG, "updateCookie");
        try {
            this.mCookieJar.removeAll();
            HttpUrl httpUrl = HttpUrl.get(new URI(str));
            ArrayList arrayList = new ArrayList();
            arrayList.add(this.cookieTokenInBody);
            this.mCookieJar.saveFromResponse(httpUrl, arrayList);
            arrayList.clear();
            arrayList.add(this.cookieServerIDInBody);
            this.mCookieJar.saveFromResponse(httpUrl, arrayList);
        } catch (URISyntaxException e) {
            Log.e(this.TAG, e.getMessage());
        }
    }

    public HttpRequestParams getRetryInstance(IAPICallFlowListener iAPICallFlowListener, MessageStoreClient messageStoreClient, ICloudMessageManagerHelper iCloudMessageManagerHelper, IRetryStackAdapterHelper iRetryStackAdapterHelper) {
        return new RequestHUIToken(iAPICallFlowListener, messageStoreClient, iCloudMessageManagerHelper);
    }
}
