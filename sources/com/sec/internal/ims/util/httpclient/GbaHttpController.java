package com.sec.internal.ims.util.httpclient;

import android.text.TextUtils;
import android.util.Log;
import com.sec.internal.constants.Mno;
import com.sec.internal.helper.SimUtil;
import com.sec.internal.helper.header.AuthorizationHeader;
import com.sec.internal.helper.header.WwwAuthenticateHeader;
import com.sec.internal.helper.httpclient.DigestAuth;
import com.sec.internal.helper.httpclient.DnsController;
import com.sec.internal.helper.httpclient.HttpController;
import com.sec.internal.helper.httpclient.HttpRequestParams;
import com.sec.internal.helper.httpclient.HttpResponseParams;
import com.sec.internal.helper.os.Debug;
import com.sec.internal.helper.parser.AuthInfoHeaderParser;
import com.sec.internal.helper.parser.WwwAuthHeaderParser;
import com.sec.internal.ims.gba.GbaException;
import com.sec.internal.ims.gba.GbaUtility;
import com.sec.internal.ims.registry.ImsRegistry;
import com.sec.internal.ims.servicemodules.ss.UtUtils;
import com.sec.internal.imscr.LogClass;
import com.sec.internal.interfaces.ims.core.imslogger.IImsDiagMonitor;
import com.sec.internal.interfaces.ims.gba.IGbaCallback;
import com.sec.internal.interfaces.ims.gba.IGbaServiceModule;
import com.sec.internal.log.IMSLog;
import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URL;
import java.nio.charset.StandardCharsets;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.TimeZone;
import java.util.concurrent.ConcurrentHashMap;

public class GbaHttpController {
    /* access modifiers changed from: private */
    public static final String TAG = "GbaHttpController";
    private static volatile GbaHttpController sInstance = new GbaHttpController();
    /* access modifiers changed from: private */
    public IGbaServiceModule mGbaServiceModule = null;
    Map<String, LastAuthInfo> mLastAuthInfoMap = new ConcurrentHashMap();
    HttpRequestParams mNafRequestParams = null;

    protected static class LastAuthInfo {
        public String LifeTime = null;
        public String btid = null;
        public DigestAuth digestAuth = null;
        public String etag = null;
        public String gbaKey = null;
        public HttpResponseParams lastNafResult = null;
        public String nextNonce = null;

        protected LastAuthInfo() {
        }

        public void reset() {
            this.btid = null;
            this.gbaKey = null;
            this.lastNafResult = null;
            this.digestAuth = null;
            this.nextNonce = null;
            this.etag = null;
            this.LifeTime = null;
        }
    }

    private GbaHttpController() {
    }

    public static GbaHttpController getInstance() {
        return sInstance;
    }

    public void clearLastAuthInfo(int i) {
        int subId = SimUtil.getSubId(i);
        String str = TAG;
        IMSLog.d(str, "clearLastAuthInfo: phoneId:" + i + " - subId:" + subId);
        this.mLastAuthInfoMap.entrySet().removeIf(new GbaHttpController$$ExternalSyntheticLambda0(subId));
    }

    public void execute(final HttpRequestParams httpRequestParams) {
        this.mNafRequestParams = httpRequestParams;
        LastAuthInfo lastAuthInfo = getLastAuthInfo(httpRequestParams.getUrl(), httpRequestParams.getPhoneId());
        try {
            final URL url = new URL(httpRequestParams.getUrl());
            if (isValidAuthInfo(lastAuthInfo)) {
                sendRequestWithAuthorization(url, httpRequestParams, lastAuthInfo.lastNafResult, lastAuthInfo.btid, lastAuthInfo.gbaKey, false);
                return;
            }
            Map<String, String> headers = httpRequestParams.getHeaders();
            HttpRequestParams makeHttpRequestParams = makeHttpRequestParams(httpRequestParams.getMethod(), httpRequestParams.getUrl(), headers, new HttpRequestParams.HttpRequestCallback() {
                public void onComplete(HttpResponseParams httpResponseParams) {
                    if (httpResponseParams == null) {
                        IMSLog.e(GbaHttpController.TAG, "execute(): onComplete: response build failure");
                        httpRequestParams.getCallback().onFail(new IOException("okhttp response build failure"));
                        return;
                    }
                    GbaHttpController.this.loggingHttpMessage(httpResponseParams.toString(), 1);
                    int statusCode = httpResponseParams.getStatusCode();
                    IMSLog.c(LogClass.UT_HTTP, httpRequestParams.getPhoneId() + ",<," + statusCode);
                    if (statusCode != 401 || !GbaHttpController.this.useGba(httpRequestParams)) {
                        IMSLog.i(GbaHttpController.TAG, "NO GBA process");
                        httpRequestParams.getCallback().onComplete(httpResponseParams);
                        return;
                    }
                    handleAuthenticateWithGba(httpResponseParams, statusCode);
                }

                private void handleAuthenticateWithGba(HttpResponseParams httpResponseParams, int i) {
                    if (GbaHttpController.isNeedCSFB(i, httpRequestParams.getPhoneId())) {
                        IMSLog.i(GbaHttpController.TAG, "Special case: TIM operator requires CSFB for 401.");
                        httpResponseParams.setStatusCode(403);
                        httpRequestParams.getCallback().onComplete(httpResponseParams);
                        return;
                    }
                    GbaHttpController.this.storeLastAuthInfo(httpResponseParams, httpRequestParams);
                    WwwAuthenticateHeader r9 = GbaHttpController.this.getWwwAuthenticateHeader(httpResponseParams);
                    if (r9 == null) {
                        IMSLog.e(GbaHttpController.TAG, "execute(): onComplete: missing header: WWW-Authenticate");
                        httpRequestParams.getCallback().onComplete(httpResponseParams);
                        return;
                    }
                    String realm = r9.getRealm();
                    if (realm.contains("3GPP-bootstrapping")) {
                        if (httpRequestParams.getIpVersion() > 0) {
                            DnsController dnsController = (DnsController) httpRequestParams.getDns();
                            dnsController.setNaf(false);
                            httpRequestParams.setDns(dnsController);
                        }
                        GbaHttpController.this.mGbaServiceModule = ImsRegistry.getServiceModuleManager().getGbaServiceModule();
                        IGbaServiceModule r0 = GbaHttpController.this.mGbaServiceModule;
                        HttpRequestParams httpRequestParams = httpRequestParams;
                        r0.getBtidAndGbaKey(httpRequestParams, realm, httpResponseParams, GbaHttpController.this.getGbaCallback(httpResponseParams, httpRequestParams, url));
                        return;
                    }
                    IMSLog.i(GbaHttpController.TAG, "HTTP digest without GBA");
                    GbaHttpController.this.sendRequestWithAuthorization(url, httpRequestParams, httpResponseParams, httpRequestParams.getUserName(), httpRequestParams.getPassword(), false);
                }

                public void onFail(IOException iOException) {
                    IMSLog.c(LogClass.UT_ERROR_HANDLE, httpRequestParams.getPhoneId() + "," + IMSLog.numberChecker(iOException.getMessage()));
                    httpRequestParams.getCallback().onFail(iOException);
                }
            }, httpRequestParams);
            if (httpRequestParams.getPostBody() != null) {
                makeHttpRequestParams.setPostBody(httpRequestParams.getPostBody());
            }
            IMSLog.c(LogClass.UT_HTTP, makeHttpRequestParams.getPhoneId() + ",>," + makeHttpRequestParams.getMethodString());
            HttpController.getInstance().execute(makeHttpRequestParams);
            loggingHttpMessage(makeHttpRequestParams.toString(), 0);
        } catch (MalformedURLException e) {
            e.printStackTrace();
        }
    }

    /* access modifiers changed from: private */
    public void sendRequestWithAuthorization(URL url, HttpRequestParams httpRequestParams, HttpResponseParams httpResponseParams, String str, String str2, boolean z) {
        String str3;
        String str4;
        String str5 = TAG;
        IMSLog.d(str5, "GBA: sendRequestWithAuthorization()");
        WwwAuthenticateHeader wwwAuthenticateHeader = getWwwAuthenticateHeader(httpResponseParams);
        if (wwwAuthenticateHeader == null || wwwAuthenticateHeader.getRealm() == null || TextUtils.isEmpty(wwwAuthenticateHeader.getQop())) {
            HttpRequestParams httpRequestParams2 = httpRequestParams;
            IMSLog.e(str5, "sendRequestWithAuthorization(): missing header: WWW-Authenticate");
            httpRequestParams.getCallback().onFail(new IOException("Invalid WwwAuthenticateHeader"));
            return;
        }
        String[] split = wwwAuthenticateHeader.getRealm().split(";");
        final String realm = wwwAuthenticateHeader.getRealm();
        int length = split.length;
        int i = 0;
        while (true) {
            if (i >= length) {
                str3 = "";
                break;
            }
            str3 = split[i];
            if ((str3.contains("uicc") && z) || (!str3.contains("uicc") && !z)) {
                break;
            }
            i++;
        }
        String str6 = str3;
        LastAuthInfo lastAuthInfo = getLastAuthInfo(url.toString(), httpRequestParams.getPhoneId());
        String[] split2 = wwwAuthenticateHeader.getQop().split(",");
        String str7 = lastAuthInfo.nextNonce;
        if (str7 != null) {
            wwwAuthenticateHeader.setNonce(str7);
        }
        DigestAuth digestAuth = lastAuthInfo.digestAuth;
        if (url.getQuery() != null) {
            str4 = url.getPath() + "?" + url.getQuery();
        } else {
            str4 = url.getPath();
        }
        String str8 = "/";
        digestAuth.setDigestAuth(str, str2, str6, wwwAuthenticateHeader.getNonce(), httpRequestParams.getMethodString(), str4.isEmpty() ? str8 : str4, wwwAuthenticateHeader.getAlgorithm(), split2[0]);
        HttpRequestParams.Method method = httpRequestParams.getMethod();
        HttpRequestParams.Method method2 = HttpRequestParams.Method.PUT;
        if (method == method2) {
            digestAuth.setBody(new String(httpRequestParams.getPostBody().getData(), StandardCharsets.UTF_8));
        } else if (SimUtil.getSimMno(httpRequestParams.getPhoneId()) != Mno.XPLORE) {
            if (!url.getPath().isEmpty()) {
                str8 = url.getPath();
            }
            digestAuth.setDigestURI(str8);
        }
        String authorizationHeader = AuthorizationHeader.getAuthorizationHeader(digestAuth, wwwAuthenticateHeader);
        HashMap hashMap = new HashMap();
        hashMap.put(HttpController.HEADER_HOST, httpRequestParams.getHeaders().get(HttpController.HEADER_HOST));
        hashMap.put("User-Agent", httpRequestParams.getHeaders().get("User-Agent"));
        hashMap.put("Authorization", authorizationHeader);
        hashMap.put("Accept", "*/*");
        hashMap.put("Accept-Encoding", getAcceptEncoding(httpRequestParams.getPhoneId()));
        if (httpRequestParams.getMethod() == method2) {
            hashMap.put("If-Match", lastAuthInfo.etag);
            hashMap.put("Content-Type", httpRequestParams.getHeaders().get("Content-Type"));
        }
        if (!TextUtils.isEmpty(httpRequestParams.getHeaders().get(HttpController.HEADER_X_TMUS_IMEI))) {
            hashMap.put(HttpController.HEADER_X_TMUS_IMEI, httpRequestParams.getHeaders().get(HttpController.HEADER_X_TMUS_IMEI));
        }
        if (!TextUtils.isEmpty(httpRequestParams.getHeaders().get("X-3GPP-Intended-Identity"))) {
            hashMap.put("X-3GPP-Intended-Identity", httpRequestParams.getHeaders().get("X-3GPP-Intended-Identity"));
        }
        final HttpRequestParams httpRequestParams3 = httpRequestParams;
        final URL url2 = url;
        final String str9 = str;
        final String str10 = str2;
        HttpRequestParams makeHttpRequestParams = makeHttpRequestParams(httpRequestParams.getMethod(), httpRequestParams.getUrl(), hashMap, new HttpRequestParams.HttpRequestCallback() {
            private boolean handleRequestSuccessWithGba(HttpResponseParams httpResponseParams, LastAuthInfo lastAuthInfo) {
                Map<String, List<String>> headers = httpResponseParams.getHeaders();
                List list = headers.get("Authentication-Info");
                if (list != null) {
                    try {
                        String nextNonce = new AuthInfoHeaderParser().parseHeaderValue((String) list.get(0)).getNextNonce();
                        if (!TextUtils.isEmpty(nextNonce)) {
                            lastAuthInfo.nextNonce = nextNonce;
                        }
                    } catch (IllegalArgumentException e) {
                        String r3 = GbaHttpController.TAG;
                        IMSLog.e(r3, "onComplete: unable to parse authInfoParsedHeader : " + e.getMessage());
                        e.printStackTrace();
                        return true;
                    }
                }
                List list2 = headers.get(HttpController.HEADER_ETAG);
                if (list2 != null) {
                    String str = (String) list2.get(0);
                    if (!TextUtils.isEmpty(str)) {
                        lastAuthInfo.etag = str;
                    }
                }
                return false;
            }

            private void handleRequestWithAuthenticate(HttpResponseParams httpResponseParams) {
                GbaHttpController.this.storeLastAuthInfo(httpResponseParams, httpRequestParams3);
                WwwAuthenticateHeader r0 = GbaHttpController.this.getWwwAuthenticateHeader(httpResponseParams);
                if (r0 == null) {
                    IMSLog.e(GbaHttpController.TAG, "sendRequestWithAuthorization(): onComplete: missing header: WWW-Authenticate");
                    httpRequestParams3.getCallback().onFail(new IOException("AuthenticateHeader null"));
                } else if (r0.isStale()) {
                    IMSLog.i(GbaHttpController.TAG, "Stale is true. Reuse same username..");
                    GbaHttpController.this.sendRequestWithAuthorization(url2, httpRequestParams3, httpResponseParams, str9, str10, false);
                } else {
                    String realm = r0.getRealm();
                    if (realm.contains("3GPP-bootstrapping")) {
                        IMSLog.i(GbaHttpController.TAG, "Retry GBA authentication...");
                        if (httpRequestParams3.getIpVersion() > 0) {
                            DnsController dnsController = (DnsController) httpRequestParams3.getDns();
                            dnsController.setNaf(false);
                            httpRequestParams3.setDns(dnsController);
                        }
                        IMSLog.i(GbaHttpController.TAG, "onComplete: 401 Unauthorized. reset GbaKey");
                        GbaHttpController.this.mGbaServiceModule.resetGbaKey(realm, httpRequestParams3.getPhoneId());
                        IGbaServiceModule r1 = GbaHttpController.this.mGbaServiceModule;
                        HttpRequestParams httpRequestParams = httpRequestParams3;
                        r1.getBtidAndGbaKey(httpRequestParams, realm, httpResponseParams, GbaHttpController.this.getGbaCallback(httpResponseParams, httpRequestParams, url2));
                        return;
                    }
                    IMSLog.i(GbaHttpController.TAG, "HTTP digest without GBA");
                    GbaHttpController.this.sendRequestWithAuthorization(url2, httpRequestParams3, httpResponseParams, httpRequestParams3.getUserName(), httpRequestParams3.getPassword(), false);
                }
            }

            public void onComplete(HttpResponseParams httpResponseParams) {
                if (httpResponseParams == null) {
                    IMSLog.e(GbaHttpController.TAG, "onComplete: the response of 2nd time naf request build failure");
                    return;
                }
                GbaHttpController.this.loggingHttpMessage(httpResponseParams.toString(), 1);
                int statusCode = httpResponseParams.getStatusCode();
                IMSLog.c(LogClass.UT_HTTP, httpRequestParams3.getPhoneId() + ",<," + statusCode);
                LastAuthInfo r1 = GbaHttpController.this.getLastAuthInfo(url2.toString(), httpRequestParams3.getPhoneId());
                if (statusCode == 200 || statusCode == 201 || statusCode == 202) {
                    if (r1 != null) {
                        r1.btid = str9;
                        r1.gbaKey = str10;
                        if (handleRequestSuccessWithGba(httpResponseParams, r1)) {
                            httpRequestParams3.getCallback().onFail(new IOException("AuthInfoHeader is invalid"));
                            return;
                        } else {
                            GbaHttpController.this.mLastAuthInfoMap.put(GbaUtility.generateLastAuthInfoKey(url2.toString(), httpRequestParams3.getPhoneId()), r1);
                        }
                    }
                } else if (statusCode == 401) {
                    handleRequestWithAuthenticate(httpResponseParams);
                    return;
                } else {
                    IMSLog.e(GbaHttpController.TAG, "onComplete: The response status code of 2nd time naf request is not 200");
                }
                httpRequestParams3.getCallback().onComplete(httpResponseParams);
            }

            public void onFail(IOException iOException) {
                String r0 = GbaHttpController.TAG;
                IMSLog.d(r0, "The Second time naf request onFail: " + iOException.getMessage());
                if (GbaHttpController.this.mGbaServiceModule != null) {
                    GbaHttpController.this.mGbaServiceModule.resetGbaKey(realm, httpRequestParams3.getPhoneId());
                }
                LastAuthInfo r02 = GbaHttpController.this.getLastAuthInfo(url2.toString(), httpRequestParams3.getPhoneId());
                if (r02 != null) {
                    r02.reset();
                }
                httpRequestParams3.getCallback().onFail(iOException);
            }
        }, httpRequestParams);
        if (httpRequestParams.getMethod() == method2) {
            makeHttpRequestParams.setPostBody(httpRequestParams.getPostBody());
        }
        if (httpRequestParams.getDns() != null) {
            if (httpRequestParams.getIpVersion() > 0) {
                DnsController dnsController = (DnsController) httpRequestParams.getDns();
                dnsController.setNaf(true);
                httpRequestParams.setDns(dnsController);
            } else {
                HttpRequestParams httpRequestParams4 = httpRequestParams;
            }
            makeHttpRequestParams.setDns(httpRequestParams.getDns());
        }
        IMSLog.c(LogClass.UT_HTTP, makeHttpRequestParams.getPhoneId() + ",>," + makeHttpRequestParams.getMethodString());
        HttpController.getInstance().execute(makeHttpRequestParams);
        loggingHttpMessage(makeHttpRequestParams.toString(), 0);
    }

    public void sendBsfRequest(String str, int i, String str2, String str3, String str4, byte[] bArr, byte[] bArr2, boolean z, HttpRequestParams httpRequestParams) {
        String str5 = str;
        int i2 = i;
        String buildUrl = buildUrl(httpRequestParams.getPhoneId(), httpRequestParams.getUseTls(), str, i);
        if (this.mGbaServiceModule == null) {
            this.mGbaServiceModule = ImsRegistry.getServiceModuleManager().getGbaServiceModule();
        }
        HashMap hashMap = new HashMap();
        hashMap.put(HttpController.HEADER_HOST, str);
        StringBuilder sb = new StringBuilder();
        sb.append("GBA-service; 0.1; ");
        sb.append(z ? "3gpp-gba-uicc" : "3gpp-gba-tmpi");
        hashMap.put("User-Agent", sb.toString());
        String str6 = str2;
        hashMap.put("Authorization", AuthorizationHeader.getAuthorizationHeader(str2, str4, "/", "", ""));
        if (httpRequestParams.getUseImei()) {
            hashMap.put(HttpController.HEADER_X_TMUS_IMEI, str3);
        } else {
            String str7 = str3;
        }
        HttpRequestParams makeHttpRequestParams = makeHttpRequestParams(HttpRequestParams.Method.GET, buildUrl, hashMap, getBsfRequestCallback(str, str2, str3, bArr, bArr2, z, httpRequestParams, buildUrl), httpRequestParams);
        if (UtUtils.isBsfDisableTls(makeHttpRequestParams.getPhoneId())) {
            IMSLog.i(TAG, "sendBsfRequest() Bsf disable Tls");
            makeHttpRequestParams.setUseTls(false);
        }
        IMSLog.c(LogClass.UT_HTTP, makeHttpRequestParams.getPhoneId() + ",>," + makeHttpRequestParams.getMethodString());
        HttpController.getInstance().execute(makeHttpRequestParams);
        loggingHttpMessage(makeHttpRequestParams.toString(), 0);
    }

    /* JADX WARNING: type inference failed for: r0v23, types: [com.sec.internal.helper.httpclient.HttpRequestParams$HttpRequestCallback] */
    /* access modifiers changed from: private */
    /* JADX WARNING: Multi-variable type inference failed */
    /* Code decompiled incorrectly, please refer to instructions dump. */
    public void sendBsfRequestWithAuthorization(java.lang.String r19, java.lang.String r20, com.sec.internal.helper.header.WwwAuthenticateHeader r21, java.lang.String r22, java.lang.String r23, byte[] r24, byte[] r25, boolean r26, com.sec.internal.helper.httpclient.HttpRequestParams r27) {
        /*
            r18 = this;
            r9 = r18
            r7 = r26
            java.lang.String r10 = TAG
            java.lang.StringBuilder r0 = new java.lang.StringBuilder
            r0.<init>()
            java.lang.String r1 = "GBA: sendBsfRequestWithAuthorization(): username: "
            r0.append(r1)
            r2 = r20
            r0.append(r2)
            java.lang.String r0 = r0.toString()
            com.sec.internal.log.IMSLog.d(r10, r0)
            com.sec.internal.interfaces.ims.gba.IGbaServiceModule r0 = r9.mGbaServiceModule
            java.lang.String r1 = r21.getNonce()
            int r3 = r27.getPhoneId()
            com.sec.internal.ims.gba.params.GbaData r6 = r0.getPassword(r1, r7, r3)
            if (r6 != 0) goto L_0x003c
            int r0 = r27.getToken()
            com.sec.internal.ims.gba.GbaException r1 = new com.sec.internal.ims.gba.GbaException
            java.lang.String r2 = "GBA FAIL akaKeys null"
            r3 = 3
            r1.<init>(r2, r3)
            r9.gbaFailCallbacksDeQ(r0, r1)
            return
        L_0x003c:
            java.util.HashMap r8 = new java.util.HashMap
            r8.<init>()
            java.lang.String r0 = "Host"
            r1 = r22
            r8.put(r0, r1)
            java.lang.StringBuilder r0 = new java.lang.StringBuilder
            r0.<init>()
            java.lang.String r3 = "GBA-service; 0.1; "
            r0.append(r3)
            if (r7 == 0) goto L_0x0057
            java.lang.String r3 = "3gpp-gba-uicc"
            goto L_0x0059
        L_0x0057:
            java.lang.String r3 = "3gpp-gba-tmpi"
        L_0x0059:
            r0.append(r3)
            java.lang.String r0 = r0.toString()
            java.lang.String r3 = "User-Agent"
            r8.put(r3, r0)
            boolean r0 = r27.getUseImei()
            if (r0 == 0) goto L_0x0073
            java.lang.String r0 = "X-TMUS-IMEI"
            r3 = r23
            r8.put(r0, r3)
            goto L_0x0075
        L_0x0073:
            r3 = r23
        L_0x0075:
            java.lang.String r0 = r6.getPassword()
            java.lang.String r4 = "dc"
            boolean r4 = r0.startsWith(r4)
            java.lang.String r5 = "Authorization"
            if (r4 == 0) goto L_0x0103
            java.lang.StringBuilder r4 = new java.lang.StringBuilder
            r4.<init>()
            java.lang.String r6 = "sendBsfRequestWithAuthorization - AUTH_SQN_FAIL, akaPassword = "
            r4.append(r6)
            r4.append(r0)
            java.lang.String r4 = r4.toString()
            com.sec.internal.log.IMSLog.i(r10, r4)
            byte[] r0 = com.sec.internal.helper.StrUtil.hexStringToBytes(r0)
            r4 = 0
            if (r0 == 0) goto L_0x00f6
            int r6 = r0.length
            r11 = 1
            if (r6 <= r11) goto L_0x00f6
            byte r6 = r0[r11]
            if (r6 <= 0) goto L_0x00e9
            java.lang.String r4 = new java.lang.String
            r11 = 2
            int r6 = r6 + r11
            byte[] r0 = java.util.Arrays.copyOfRange(r0, r11, r6)
            byte[] r0 = org.apache.commons.codec.binary.Base64.encodeBase64(r0)
            java.nio.charset.Charset r6 = java.nio.charset.StandardCharsets.UTF_8
            r4.<init>(r0, r6)
            java.lang.String r12 = ""
            java.lang.String r13 = r21.getRealm()
            java.lang.String r14 = "GET"
            java.lang.String r15 = "/"
            r11 = r20
            r16 = r4
            r17 = r21
            java.lang.String r0 = com.sec.internal.helper.header.AuthorizationHeader.getAuthorizationHeader(r11, r12, r13, r14, r15, r16, r17)
            r8.put(r5, r0)
            r0 = r18
            r1 = r22
            r2 = r20
            r3 = r23
            r4 = r24
            r5 = r25
            r6 = r26
            r7 = r27
            r15 = r8
            r8 = r19
            com.sec.internal.helper.httpclient.HttpRequestParams$HttpRequestCallback r0 = r0.getBsfRequestCallback(r1, r2, r3, r4, r5, r6, r7, r8)
            r11 = r0
            r8 = r15
            goto L_0x012e
        L_0x00e9:
            java.lang.String r0 = "Invalid autsLength."
            com.sec.internal.log.IMSLog.e(r10, r0)
            int r0 = r27.getToken()
            r9.gbaFailCallbacksDeQ(r0, r4)
            return
        L_0x00f6:
            java.lang.String r0 = "Invalid simResponse."
            com.sec.internal.log.IMSLog.e(r10, r0)
            int r0 = r27.getToken()
            r9.gbaFailCallbacksDeQ(r0, r4)
            return
        L_0x0103:
            r15 = r8
            java.lang.String r12 = r6.getPassword()
            java.lang.String r13 = r21.getRealm()
            java.lang.String r14 = "GET"
            java.lang.String r0 = "/"
            r11 = r20
            r15 = r0
            r16 = r21
            java.lang.String r0 = com.sec.internal.helper.header.AuthorizationHeader.getAuthorizationHeader(r11, r12, r13, r14, r15, r16)
            r8.put(r5, r0)
            com.sec.internal.ims.util.httpclient.GbaHttpController$3 r11 = new com.sec.internal.ims.util.httpclient.GbaHttpController$3
            r0 = r11
            r1 = r18
            r2 = r27
            r3 = r21
            r4 = r24
            r5 = r25
            r7 = r26
            r0.<init>(r2, r3, r4, r5, r6, r7)
        L_0x012e:
            com.sec.internal.helper.httpclient.HttpRequestParams$Method r0 = com.sec.internal.helper.httpclient.HttpRequestParams.Method.GET
            r20 = r18
            r21 = r0
            r22 = r19
            r23 = r8
            r24 = r11
            r25 = r27
            com.sec.internal.helper.httpclient.HttpRequestParams r0 = r20.makeHttpRequestParams(r21, r22, r23, r24, r25)
            int r1 = r0.getPhoneId()
            boolean r1 = com.sec.internal.ims.servicemodules.ss.UtUtils.isBsfDisableTls(r1)
            r2 = 0
            if (r1 == 0) goto L_0x0153
            java.lang.String r1 = "GBA: Bsf disable Tls"
            com.sec.internal.log.IMSLog.i(r10, r1)
            r0.setUseTls(r2)
        L_0x0153:
            java.lang.StringBuilder r1 = new java.lang.StringBuilder
            r1.<init>()
            int r3 = r0.getPhoneId()
            r1.append(r3)
            java.lang.String r3 = ",>,"
            r1.append(r3)
            java.lang.String r3 = r0.getMethodString()
            r1.append(r3)
            java.lang.String r1 = r1.toString()
            r3 = 822083585(0x31000001, float:1.8626454E-9)
            com.sec.internal.log.IMSLog.c(r3, r1)
            com.sec.internal.helper.httpclient.HttpController r1 = com.sec.internal.helper.httpclient.HttpController.getInstance()
            r1.execute(r0)
            java.lang.String r0 = r0.toString()
            r9.loggingHttpMessage(r0, r2)
            return
        */
        throw new UnsupportedOperationException("Method not decompiled: com.sec.internal.ims.util.httpclient.GbaHttpController.sendBsfRequestWithAuthorization(java.lang.String, java.lang.String, com.sec.internal.helper.header.WwwAuthenticateHeader, java.lang.String, java.lang.String, byte[], byte[], boolean, com.sec.internal.helper.httpclient.HttpRequestParams):void");
    }

    /* access modifiers changed from: private */
    public IGbaCallback getGbaCallback(final HttpResponseParams httpResponseParams, final HttpRequestParams httpRequestParams, final URL url) {
        return new IGbaCallback() {
            public void onComplete(int i, String str, String str2, boolean z, HttpResponseParams httpResponseParams) {
                if (str == null || str2 == null) {
                    IMSLog.e(GbaHttpController.TAG, "gbaCallBack:  cannot get username and password for GBA");
                    httpRequestParams.getCallback().onFail(new IOException("GbaKey null"));
                    return;
                }
                GbaHttpController.this.sendRequestWithAuthorization(url, httpRequestParams, httpResponseParams, str, str2, z);
            }

            public void onFail(int i, GbaException gbaException) {
                httpRequestParams.getCallback().onFail(new IOException(gbaException.getMessage(), gbaException));
            }
        };
    }

    private HttpRequestParams.HttpRequestCallback getBsfRequestCallback(String str, String str2, String str3, byte[] bArr, byte[] bArr2, boolean z, HttpRequestParams httpRequestParams, String str4) {
        final HttpRequestParams httpRequestParams2 = httpRequestParams;
        final String str5 = str4;
        final String str6 = str2;
        final String str7 = str;
        final String str8 = str3;
        final byte[] bArr3 = bArr;
        final byte[] bArr4 = bArr2;
        final boolean z2 = z;
        return new HttpRequestParams.HttpRequestCallback() {
            public void onComplete(HttpResponseParams httpResponseParams) {
                if (httpResponseParams == null) {
                    IMSLog.e(GbaHttpController.TAG, "bsfRequestCallback: onComplete: response build failure");
                    GbaHttpController.this.gbaFailCallbacksDeQ(httpRequestParams2.getToken(), new GbaException("Result null", 3));
                    return;
                }
                GbaHttpController.this.loggingHttpMessage(httpResponseParams.toString(), 1);
                int statusCode = httpResponseParams.getStatusCode();
                IMSLog.c(LogClass.UT_HTTP, httpRequestParams2.getPhoneId() + ",<," + statusCode);
                if (statusCode != 401) {
                    String r13 = GbaHttpController.TAG;
                    IMSLog.e(r13, "bsfRequestCallback: onComplete: unexpected response code: " + statusCode);
                    GbaHttpController.this.gbaFailCallbacksDeQ(httpRequestParams2.getToken(), new GbaException("unexpected response", statusCode));
                    return;
                }
                WwwAuthenticateHeader r5 = GbaHttpController.this.getWwwAuthenticateHeader(httpResponseParams);
                if (r5 == null) {
                    IMSLog.e(GbaHttpController.TAG, "bsfRequestCallback: onComplete: missing header: WWW-Authenticate");
                    GbaHttpController.this.gbaFailCallbacksDeQ(httpRequestParams2.getToken(), new GbaException("AuthenticateHeader null", 3));
                    return;
                }
                GbaHttpController.this.sendBsfRequestWithAuthorization(str5, str6, r5, str7, str8, bArr3, bArr4, z2, httpRequestParams2);
            }

            public void onFail(IOException iOException) {
                IMSLog.c(LogClass.UT_ERROR_HANDLE, httpRequestParams2.getPhoneId() + "," + IMSLog.numberChecker(iOException.getMessage()));
                GbaHttpController.this.gbaFailCallbacksDeQ(httpRequestParams2.getToken(), new GbaException(iOException.getMessage(), 3));
            }
        };
    }

    /* access modifiers changed from: private */
    public boolean useGba(HttpRequestParams httpRequestParams) {
        Map<String, String> headers = httpRequestParams.getHeaders();
        String str = headers != null ? headers.get("User-Agent") : null;
        if (str == null) {
            IMSLog.d(TAG, "useGba(): no headers");
            return false;
        }
        String str2 = TAG;
        IMSLog.d(str2, "useGba(): User-Agent: " + str);
        return str.contains("3gpp-gba");
    }

    /* access modifiers changed from: private */
    public WwwAuthenticateHeader getWwwAuthenticateHeader(HttpResponseParams httpResponseParams) {
        Map<String, List<String>> headers = httpResponseParams.getHeaders();
        List list = headers.get("WWW-Authenticate");
        if (list == null || list.size() == 0) {
            list = headers.get("WWW-Authenticate".toLowerCase());
        }
        if (!(list == null || list.size() == 0)) {
            try {
                return new WwwAuthHeaderParser().parseHeaderValue((String) list.get(0));
            } catch (IllegalArgumentException e) {
                String str = TAG;
                IMSLog.e(str, "getWwwAuthenticateHeader: unable to parse wwwAuthHeader : " + e.getMessage());
                e.printStackTrace();
            }
        }
        return null;
    }

    private String buildUrl(int i, boolean z, String str, int i2) {
        StringBuilder sb = new StringBuilder();
        if (i2 == 443 || (z && SimUtil.getSimMno(i).isOneOf(Mno.SPARK))) {
            sb.append("https://");
        } else {
            sb.append("http://");
        }
        sb.append(str);
        sb.append(':');
        sb.append(i2);
        sb.append('/');
        return sb.toString();
    }

    /* access modifiers changed from: private */
    public void gbaCallbacksDeQ(int i, String str, String str2, boolean z, HttpResponseParams httpResponseParams) {
        IGbaCallback gbaCallback = this.mGbaServiceModule.getGbaCallback(i);
        if (gbaCallback != null) {
            this.mGbaServiceModule.removeGbaCallback(i);
            gbaCallback.onComplete(i, str, str2, z, httpResponseParams);
        }
    }

    /* access modifiers changed from: private */
    public void gbaFailCallbacksDeQ(int i, GbaException gbaException) {
        IGbaCallback gbaCallback = this.mGbaServiceModule.getGbaCallback(i);
        if (gbaCallback != null) {
            this.mGbaServiceModule.removeGbaCallback(i);
            gbaCallback.onFail(i, gbaException);
        }
    }

    private HttpRequestParams makeHttpRequestParams(HttpRequestParams.Method method, String str, Map<String, String> map, HttpRequestParams.HttpRequestCallback httpRequestCallback, HttpRequestParams httpRequestParams) {
        HttpRequestParams httpRequestParams2 = new HttpRequestParams(method, str, map, httpRequestCallback);
        if (httpRequestParams.getSocketFactory() != null) {
            httpRequestParams2.setSocketFactory(httpRequestParams.getSocketFactory());
        }
        if (httpRequestParams.getDns() != null) {
            httpRequestParams2.setDns(httpRequestParams.getDns());
        }
        httpRequestParams2.setReuseConnection(httpRequestParams.isReuseConnection());
        httpRequestParams2.setPhoneId(httpRequestParams.getPhoneId());
        httpRequestParams2.setUseTls(httpRequestParams.getUseTls());
        httpRequestParams2.setIgnoreServerCert(httpRequestParams.getIgnoreServerCert());
        httpRequestParams2.setConnectionTimeout(httpRequestParams.getConnectionTimeout());
        httpRequestParams2.setReadTimeout(httpRequestParams.getReadTimeout());
        httpRequestParams2.setProxy(httpRequestParams.getProxy());
        httpRequestParams2.setUseProxy(httpRequestParams.getUseProxy());
        httpRequestParams2.setToken(httpRequestParams.getToken());
        httpRequestParams2.setCipherSuiteType(httpRequestParams.getCipherSuiteType());
        return httpRequestParams2;
    }

    /* access modifiers changed from: private */
    public static boolean isNeedCSFB(int i, int i2) {
        return SimUtil.getSimMno(i2) == Mno.TELECOM_ITALY && i == 401;
    }

    private static String getAcceptEncoding(int i) {
        return SimUtil.getSimMno(i).isOneOf(Mno.H3G, Mno.SMARTFREN, Mno.TMOUS, Mno.DISH, Mno.TELE2_RUSSIA) ? "" : "*";
    }

    /* access modifiers changed from: private */
    public LastAuthInfo getLastAuthInfo(String str, int i) {
        return this.mLastAuthInfoMap.get(GbaUtility.generateLastAuthInfoKey(str, i));
    }

    /* access modifiers changed from: private */
    public void storeLastAuthInfo(HttpResponseParams httpResponseParams, HttpRequestParams httpRequestParams) {
        LastAuthInfo lastAuthInfo = new LastAuthInfo();
        lastAuthInfo.digestAuth = new DigestAuth();
        lastAuthInfo.lastNafResult = httpResponseParams;
        this.mLastAuthInfoMap.put(GbaUtility.generateLastAuthInfoKey(httpRequestParams.getUrl(), httpRequestParams.getPhoneId()), lastAuthInfo);
    }

    private boolean isValidAuthInfo(LastAuthInfo lastAuthInfo) {
        Date date;
        if (lastAuthInfo == null || lastAuthInfo.btid == null || lastAuthInfo.LifeTime == null) {
            return false;
        }
        SimpleDateFormat simpleDateFormat = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss'Z'", Locale.US);
        simpleDateFormat.setTimeZone(TimeZone.getTimeZone("GMT"));
        try {
            date = simpleDateFormat.parse(lastAuthInfo.LifeTime);
        } catch (ParseException e) {
            String str = TAG;
            IMSLog.e(str, "lifetime parseException" + e.getMessage());
            lastAuthInfo.reset();
            date = null;
        }
        if (isKeyAlive(date) && lastAuthInfo.lastNafResult != null) {
            return true;
        }
        IMSLog.e(TAG, "Btid LifeTime expired");
        lastAuthInfo.LifeTime = null;
        return false;
    }

    private boolean isKeyAlive(Date date) {
        if (date == null) {
            return false;
        }
        SimpleDateFormat simpleDateFormat = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss'Z'", Locale.US);
        Date date2 = new Date();
        simpleDateFormat.setTimeZone(TimeZone.getTimeZone("GMT"));
        if (date.getTime() > date2.getTime() + (((long) 0) * 1000)) {
            return true;
        }
        return false;
    }

    /* access modifiers changed from: private */
    public void loggingHttpMessage(String str, int i) {
        if (str != null && !Debug.isProductShip()) {
            String hidePrivateInfoFromMsg = hidePrivateInfoFromMsg(str.replaceAll("HttpRequestParams.*\r\n.*mMethod: ", "").replaceAll("HttpResponseParams.*\r\n.*mStatusCode=", "HTTP/1.1 ").replaceAll("\r\n.*mUrl: ", " "));
            String format = new SimpleDateFormat("MM/dd/yyyy HH:mm:ss.SSS", Locale.getDefault()).format(new Date());
            IImsDiagMonitor imsDiagMonitor = ImsRegistry.getImsDiagMonitor();
            if (imsDiagMonitor == null) {
                Log.i(TAG, "NULL Diag Mointor Pointer");
            } else {
                imsDiagMonitor.onIndication(1, hidePrivateInfoFromMsg, 100, i, format, "", "", "");
            }
        }
    }

    private String hidePrivateInfoFromMsg(String str) {
        if (!Debug.isProductShip()) {
            return str;
        }
        return str.replaceAll("sip:+[0-9+-]+", "sip:xxxxxxxxxxxxxxx").replaceAll("tel:+[0-9+-]+", "tel:xxxxxxxxxxxxxxx").replaceAll("imei:+[0-9+-]+", "imei:xxxxxxxx").replaceAll("username=\"+[^\"]+", "username=xxxxxxxxxxxxxxx").replaceAll("\"+[0-9+-]+\"", "\"xxxxxxxxxxxxxxx\"").replaceAll("target>+.+</.*target", "target>xxxxxxxxxxxxxxx</target");
    }
}
