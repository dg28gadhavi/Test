package com.sec.internal.ims.util;

import android.net.Network;
import com.sec.internal.helper.HttpRequest;
import com.sec.internal.ims.rcs.RcsPolicyManager;
import com.sec.internal.ims.servicemodules.im.util.FileTaskUtil;
import com.sec.internal.ims.settings.RcsPolicySettings;

public class OpenIdAuth {
    private static final String LOG_TAG = "OpenIdAuth";

    public static class OpenIdRequest {
        /* access modifiers changed from: private */
        public final boolean mIsTrustAllCert;
        /* access modifiers changed from: private */
        public final Network mNetwork;
        /* access modifiers changed from: private */
        public final int mPhoneId;
        /* access modifiers changed from: private */
        public final String mUrl;
        /* access modifiers changed from: private */
        public final String mUserAgent;

        public OpenIdRequest(int i, String str, Network network, String str2, boolean z) {
            this.mPhoneId = i;
            this.mUrl = str;
            this.mNetwork = network;
            this.mUserAgent = str2;
            this.mIsTrustAllCert = z;
        }
    }

    /* JADX WARNING: Removed duplicated region for block: B:19:0x00d0  */
    /* JADX WARNING: Removed duplicated region for block: B:21:0x00d7  */
    /* Code decompiled incorrectly, please refer to instructions dump. */
    public static java.lang.String sendAuthRequest(com.sec.internal.ims.util.OpenIdAuth.OpenIdRequest r6) throws com.sec.internal.helper.HttpRequest.HttpRequestException {
        /*
            java.lang.String r0 = r6.mUrl
            com.sec.internal.helper.HttpRequest r0 = com.sec.internal.helper.HttpRequest.get(r0)
            setDefaultHeaders(r0, r6)
            int r1 = r0.code()
            r2 = 200(0xc8, float:2.8E-43)
            r3 = 302(0x12e, float:4.23E-43)
            if (r1 == r2) goto L_0x008d
            if (r1 == r3) goto L_0x0085
            r2 = 401(0x191, float:5.62E-43)
            if (r1 == r2) goto L_0x003c
            java.lang.String r6 = LOG_TAG
            java.lang.StringBuilder r2 = new java.lang.StringBuilder
            r2.<init>()
            java.lang.String r4 = "Receive HTTP response "
            r2.append(r4)
            java.lang.String r4 = r0.message()
            r2.append(r4)
            java.lang.String r4 = " neither 302 nor UNAUTHORIZED"
            r2.append(r4)
            java.lang.String r2 = r2.toString()
            com.sec.internal.log.IMSLog.s(r6, r2)
            goto L_0x00ce
        L_0x003c:
            java.lang.String r1 = LOG_TAG
            java.lang.String r2 = "Receive 401 Unauthorized, attempt to generate response"
            android.util.Log.d(r1, r2)
            r0.disconnect()
            java.lang.String r2 = r0.wwwAuthenticate()
            java.lang.StringBuilder r4 = new java.lang.StringBuilder
            r4.<init>()
            java.lang.String r5 = "challenge: "
            r4.append(r5)
            r4.append(r2)
            java.lang.String r4 = r4.toString()
            com.sec.internal.log.IMSLog.s(r1, r4)
            int r1 = r6.mPhoneId
            java.lang.String r4 = r6.mUrl
            java.lang.String r5 = "GET"
            java.lang.String r0 = r0.getCipherSuite()
            java.lang.String r0 = com.sec.internal.ims.util.HttpAuthGenerator.getAuthorizationHeader(r1, r4, r2, r5, r0)
            java.lang.String r1 = r6.mUrl
            com.sec.internal.helper.HttpRequest r1 = com.sec.internal.helper.HttpRequest.get(r1)
            setDefaultHeaders(r1, r6)
            r1.authorization(r0)
            int r6 = r1.code()
            r0 = r1
        L_0x0083:
            r1 = r6
            goto L_0x00ce
        L_0x0085:
            java.lang.String r6 = LOG_TAG
            java.lang.String r2 = "Received 302"
            android.util.Log.d(r6, r2)
            goto L_0x00ce
        L_0x008d:
            java.lang.String r2 = LOG_TAG
            java.lang.String r4 = "200 OK received"
            android.util.Log.d(r2, r4)
            java.lang.String r2 = "Content-Type"
            java.lang.String r4 = r0.header(r2)
            if (r4 == 0) goto L_0x00ce
            java.lang.String r2 = r0.header(r2)
            java.lang.String r4 = "application/vnd.gsma.eap-relay.v1.0+json"
            boolean r2 = r2.contains(r4)
            if (r2 == 0) goto L_0x00ce
            r0.disconnect()
            java.lang.String r2 = r0.body()
            if (r2 == 0) goto L_0x00ce
            int r4 = r6.mPhoneId
            java.lang.String r2 = com.sec.internal.ims.util.HttpAuthGenerator.getEAPAkaChallengeResponse(r4, r2)
            if (r2 == 0) goto L_0x00ce
            java.lang.String r0 = r6.mUrl
            com.sec.internal.helper.HttpRequest r0 = com.sec.internal.helper.HttpRequest.post(r0)
            setDefaultHeaders(r0, r6)
            r0.send((java.lang.CharSequence) r2)
            int r6 = r0.code()
            goto L_0x0083
        L_0x00ce:
            if (r1 != r3) goto L_0x00d7
            java.lang.String r6 = "Location"
            java.lang.String r6 = r0.header(r6)
            return r6
        L_0x00d7:
            java.lang.String r6 = LOG_TAG
            java.lang.StringBuilder r0 = new java.lang.StringBuilder
            r0.<init>()
            java.lang.String r2 = "Did not receive 302 after authentication, received : "
            r0.append(r2)
            r0.append(r1)
            java.lang.String r0 = r0.toString()
            android.util.Log.d(r6, r0)
            r6 = 0
            return r6
        */
        throw new UnsupportedOperationException("Method not decompiled: com.sec.internal.ims.util.OpenIdAuth.sendAuthRequest(com.sec.internal.ims.util.OpenIdAuth$OpenIdRequest):java.lang.String");
    }

    private static void setDefaultHeaders(HttpRequest httpRequest, OpenIdRequest openIdRequest) {
        httpRequest.setParams(openIdRequest.mNetwork, false, 10000, FileTaskUtil.READ_DATA_TIMEOUT, openIdRequest.mUserAgent);
        if (openIdRequest.mIsTrustAllCert) {
            httpRequest.trustAllCerts().trustAllHosts();
        }
        if (RcsPolicyManager.getRcsStrategy(openIdRequest.mPhoneId).boolSetting(RcsPolicySettings.RcsPolicy.IS_EAP_SUPPORTED)) {
            httpRequest.header("Accept", "application/vnd.gsma.eap-relay.v1.0+json");
        }
    }
}
