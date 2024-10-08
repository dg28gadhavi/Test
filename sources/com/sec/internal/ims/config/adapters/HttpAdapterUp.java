package com.sec.internal.ims.config.adapters;

import com.sec.internal.helper.SimUtil;
import com.sec.internal.ims.config.adapters.HttpAdapter;
import com.sec.internal.interfaces.ims.config.IHttpAdapter;
import com.sec.internal.log.IMSLog;
import java.net.HttpURLConnection;
import java.util.List;
import java.util.Map;

public class HttpAdapterUp extends HttpAdapter {
    protected static final String LOG_TAG = "HttpAdapterUp";

    public HttpAdapterUp(int i) {
        super(i);
        this.mState = new IdleState();
    }

    protected class IdleState extends HttpAdapter.IdleState {
        protected IdleState() {
            super();
        }

        public boolean open(String str) {
            HttpAdapterUp httpAdapterUp = HttpAdapterUp.this;
            httpAdapterUp.mUrl = httpAdapterUp.createReqUrlWithMask(new StringBuffer(str), HttpAdapterUp.this.mParams, false);
            HttpAdapterUp httpAdapterUp2 = HttpAdapterUp.this;
            httpAdapterUp2.dumpAutoConfUrl(str, httpAdapterUp2.mUrl, httpAdapterUp2.mParams);
            if (!HttpAdapterUp.this.openUrlConnection()) {
                return false;
            }
            HttpAdapterUp.this.setUrlConnection();
            HttpAdapterUp httpAdapterUp3 = HttpAdapterUp.this;
            httpAdapterUp3.mHttpURLConn = (HttpURLConnection) httpAdapterUp3.mURLConn;
            httpAdapterUp3.mState = new ReadyState();
            return true;
        }
    }

    protected class ReadyState extends HttpAdapter.ReadyState {
        protected ReadyState() {
            super();
        }

        public boolean close() {
            HttpAdapterUp.this.mHttpURLConn.disconnect();
            HttpAdapterUp httpAdapterUp = HttpAdapterUp.this;
            httpAdapterUp.mState = new IdleState();
            return true;
        }

        public IHttpAdapter.Response request() {
            HttpAdapterUp.this.tryToConnectHttpUrlConnection();
            String stringBuffer = HttpAdapterUp.this.mUrl.toString();
            HttpAdapterUp httpAdapterUp = HttpAdapterUp.this;
            int resStatusCode = httpAdapterUp.getResStatusCode(httpAdapterUp.mHttpURLConn);
            HttpAdapterUp httpAdapterUp2 = HttpAdapterUp.this;
            String resStatusMessage = httpAdapterUp2.getResStatusMessage(httpAdapterUp2.mHttpURLConn);
            HttpAdapterUp httpAdapterUp3 = HttpAdapterUp.this;
            Map<String, List<String>> resHeader = httpAdapterUp3.getResHeader(httpAdapterUp3.mHttpURLConn);
            HttpAdapterUp httpAdapterUp4 = HttpAdapterUp.this;
            return new IHttpAdapter.Response(stringBuffer, resStatusCode, resStatusMessage, resHeader, httpAdapterUp4.getResBody(httpAdapterUp4.mHttpURLConn));
        }
    }

    /* access modifiers changed from: private */
    /* JADX WARNING: Code restructure failed: missing block: B:31:0x0098, code lost:
        if (r3.equals(com.sec.internal.constants.ims.config.ConfigConstants.PNAME.OTP) == false) goto L_0x00d7;
     */
    /* Code decompiled incorrectly, please refer to instructions dump. */
    public java.lang.StringBuffer createReqUrlWithMask(java.lang.StringBuffer r13, java.util.Map<java.lang.String, java.lang.String> r14, boolean r15) {
        /*
            r12 = this;
            java.lang.String r0 = "%"
            java.lang.String r1 = "&"
            if (r13 == 0) goto L_0x00fb
            if (r14 == 0) goto L_0x00fb
            int r2 = r14.size()
            if (r2 <= 0) goto L_0x00fb
            int r2 = r13.length()
            int r2 = r2 + -1
            char r2 = r13.charAt(r2)
            r3 = 47
            if (r2 != r3) goto L_0x0025
            int r2 = r13.length()
            int r2 = r2 + -1
            r13.deleteCharAt(r2)
        L_0x0025:
            java.lang.String r2 = "?"
            r13.append(r2)
            java.util.Set r14 = r14.entrySet()
            java.util.Iterator r14 = r14.iterator()
        L_0x0032:
            boolean r2 = r14.hasNext()
            if (r2 == 0) goto L_0x00f2
            java.lang.Object r2 = r14.next()
            java.util.Map$Entry r2 = (java.util.Map.Entry) r2
            java.lang.Object r3 = r2.getKey()
            java.lang.String r3 = (java.lang.String) r3
            java.lang.Object r4 = r2.getValue()
            if (r4 != 0) goto L_0x0053
            r13.append(r3)
            java.lang.String r2 = "=&"
            r13.append(r2)
            goto L_0x0032
        L_0x0053:
            java.lang.Object r2 = r2.getValue()
            java.lang.String r2 = (java.lang.String) r2
            java.lang.String r4 = "\\|\\|"
            java.lang.String[] r2 = r2.split(r4)
            int r4 = r2.length
            r5 = 0
            r6 = r5
        L_0x0062:
            if (r6 >= r4) goto L_0x0032
            r7 = r2[r6]
            r13.append(r3)     // Catch:{ UnsupportedEncodingException -> 0x00df }
            java.lang.String r8 = "="
            r13.append(r8)     // Catch:{ UnsupportedEncodingException -> 0x00df }
            boolean r8 = r7.contains(r0)     // Catch:{ UnsupportedEncodingException -> 0x00df }
            if (r8 == 0) goto L_0x0076
            r8 = r7
            goto L_0x007d
        L_0x0076:
            java.lang.String r8 = "utf-8"
            java.lang.String r8 = java.net.URLEncoder.encode(r7, r8)     // Catch:{ UnsupportedEncodingException -> 0x00df }
        L_0x007d:
            if (r15 == 0) goto L_0x00d8
            java.lang.String r9 = "IMSI"
            boolean r9 = r3.equals(r9)     // Catch:{ UnsupportedEncodingException -> 0x00df }
            java.lang.String r10 = "xxx"
            if (r9 != 0) goto L_0x009a
            java.lang.String r9 = "msisdn"
            boolean r9 = r3.equals(r9)     // Catch:{ UnsupportedEncodingException -> 0x00df }
            if (r9 != 0) goto L_0x009a
            java.lang.String r9 = "OTP"
            boolean r9 = r3.equals(r9)     // Catch:{ UnsupportedEncodingException -> 0x00df }
            if (r9 == 0) goto L_0x00d7
        L_0x009a:
            boolean r9 = r8.contains(r0)     // Catch:{ UnsupportedEncodingException -> 0x00df }
            if (r9 == 0) goto L_0x00bc
            int r9 = r8.length()     // Catch:{ UnsupportedEncodingException -> 0x00df }
            r11 = 8
            if (r9 <= r11) goto L_0x00d7
            java.lang.StringBuilder r9 = new java.lang.StringBuilder     // Catch:{ UnsupportedEncodingException -> 0x00df }
            r9.<init>()     // Catch:{ UnsupportedEncodingException -> 0x00df }
            java.lang.String r8 = r8.substring(r5, r11)     // Catch:{ UnsupportedEncodingException -> 0x00df }
            r9.append(r8)     // Catch:{ UnsupportedEncodingException -> 0x00df }
            r9.append(r10)     // Catch:{ UnsupportedEncodingException -> 0x00df }
            java.lang.String r8 = r9.toString()     // Catch:{ UnsupportedEncodingException -> 0x00df }
            goto L_0x00d8
        L_0x00bc:
            int r9 = r8.length()     // Catch:{ UnsupportedEncodingException -> 0x00df }
            r11 = 5
            if (r9 <= r11) goto L_0x00d7
            java.lang.StringBuilder r9 = new java.lang.StringBuilder     // Catch:{ UnsupportedEncodingException -> 0x00df }
            r9.<init>()     // Catch:{ UnsupportedEncodingException -> 0x00df }
            java.lang.String r8 = r8.substring(r5, r11)     // Catch:{ UnsupportedEncodingException -> 0x00df }
            r9.append(r8)     // Catch:{ UnsupportedEncodingException -> 0x00df }
            r9.append(r10)     // Catch:{ UnsupportedEncodingException -> 0x00df }
            java.lang.String r8 = r9.toString()     // Catch:{ UnsupportedEncodingException -> 0x00df }
            goto L_0x00d8
        L_0x00d7:
            r8 = r10
        L_0x00d8:
            r13.append(r8)     // Catch:{ UnsupportedEncodingException -> 0x00df }
            r13.append(r1)     // Catch:{ UnsupportedEncodingException -> 0x00df }
            goto L_0x00ee
        L_0x00df:
            java.lang.String r8 = LOG_TAG
            int r9 = r12.mPhoneId
            java.lang.String r10 = "UnsupportedEncodingException occur. use plain string"
            com.sec.internal.log.IMSLog.i(r8, r9, r10)
            r13.append(r7)
            r13.append(r1)
        L_0x00ee:
            int r6 = r6 + 1
            goto L_0x0062
        L_0x00f2:
            int r12 = r13.length()
            int r12 = r12 + -1
            r13.deleteCharAt(r12)
        L_0x00fb:
            return r13
        */
        throw new UnsupportedOperationException("Method not decompiled: com.sec.internal.ims.config.adapters.HttpAdapterUp.createReqUrlWithMask(java.lang.StringBuffer, java.util.Map, boolean):java.lang.StringBuffer");
    }

    /* access modifiers changed from: private */
    public void dumpAutoConfUrl(String str, StringBuffer stringBuffer, Map<String, String> map) {
        String str2 = LOG_TAG;
        IMSLog.i(str2, this.mPhoneId, str);
        if (!SimUtil.getSimMno(this.mPhoneId).isVodafone() || !IMSLog.isShipBuild()) {
            IMSLog.s(str2, this.mPhoneId, stringBuffer.toString());
            return;
        }
        IMSLog.i(str2, this.mPhoneId, createReqUrlWithMask(new StringBuffer(str), map, true).toString());
    }
}
