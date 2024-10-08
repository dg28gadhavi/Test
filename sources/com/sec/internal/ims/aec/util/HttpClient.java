package com.sec.internal.ims.aec.util;

import android.net.Network;
import android.net.Uri;
import com.sec.internal.log.AECLog;
import java.io.IOException;
import java.io.OutputStream;
import java.net.CookieHandler;
import java.net.HttpURLConnection;
import java.net.URL;
import java.nio.charset.StandardCharsets;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import javax.net.ssl.HttpsURLConnection;
import org.json.JSONObject;

public class HttpClient {
    private static final String GET = "GET";
    /* access modifiers changed from: private */
    public static final String LOG_TAG = "HttpClient";
    private static final int MAX_CHUNK_SIZE = 512000;
    private static final int MAX_CONN_TIMEOUT = 30000;
    private static final int MAX_READ_TIMEOUT = 30000;
    private static final int MIN_CHUNK_SIZE = 61440;
    private static final String POST = "POST";
    protected Map<String, List<String>> mHeaders = null;
    protected String mHostName = null;
    protected HttpsURLConnection mHttpsURLConn = null;
    protected Network mNetwork = null;
    protected Map<String, String> mParams = null;
    /* access modifiers changed from: private */
    public final int mPhoneId;
    protected JSONObject mPostData = null;

    public HttpClient(int i) {
        this.mPhoneId = i;
    }

    public void setHeaders(Map<String, List<String>> map) {
        HashMap hashMap = new HashMap();
        this.mHeaders = hashMap;
        hashMap.putAll(map);
    }

    public void setHostName(String str) {
        this.mHostName = str;
    }

    public String getHostName() {
        if (this.mHostName.indexOf(58) <= 0) {
            return this.mHostName;
        }
        String str = this.mHostName;
        return str.substring(0, str.indexOf(58));
    }

    public void setParams(Map<String, String> map) {
        HashMap hashMap = new HashMap();
        this.mParams = hashMap;
        hashMap.putAll(map);
    }

    public void setPostData(JSONObject jSONObject) {
        this.mPostData = jSONObject;
    }

    public String getPostData() {
        return this.mPostData.toString().replaceAll("\\\\", "");
    }

    public void setNetwork(Network network) {
        this.mNetwork = network;
    }

    private HttpsURLConnection openURLConnection(String str) throws Exception {
        HttpsURLConnection httpsURLConnection;
        URL url = new URL(str);
        Network network = this.mNetwork;
        if (network == null) {
            httpsURLConnection = (HttpsURLConnection) url.openConnection();
        } else {
            httpsURLConnection = (HttpsURLConnection) network.openConnection(url);
        }
        if (httpsURLConnection != null) {
            httpsURLConnection.setHostnameVerifier(new HttpClient$$ExternalSyntheticLambda0(this));
        }
        return httpsURLConnection;
    }

    /* JADX DEBUG: Multi-variable search result rejected for TypeSearchVarInfo{r0v0, resolved type: int} */
    /* JADX DEBUG: Multi-variable search result rejected for TypeSearchVarInfo{r0v1, resolved type: int} */
    /* JADX DEBUG: Multi-variable search result rejected for TypeSearchVarInfo{r2v0, resolved type: boolean} */
    /* JADX DEBUG: Multi-variable search result rejected for TypeSearchVarInfo{r0v2, resolved type: int} */
    /* JADX DEBUG: Multi-variable search result rejected for TypeSearchVarInfo{r0v3, resolved type: int} */
    /* access modifiers changed from: private */
    /* JADX WARNING: Multi-variable type inference failed */
    /* Code decompiled incorrectly, please refer to instructions dump. */
    public /* synthetic */ boolean lambda$openURLConnection$0(java.lang.String r6, javax.net.ssl.SSLSession r7) {
        /*
            r5 = this;
            okhttp3.internal.tls.OkHostnameVerifier r6 = okhttp3.internal.tls.OkHostnameVerifier.INSTANCE
            r0 = 0
            java.lang.String r1 = LOG_TAG     // Catch:{ SSLException -> 0x0038 }
            java.lang.StringBuilder r2 = new java.lang.StringBuilder     // Catch:{ SSLException -> 0x0038 }
            r2.<init>()     // Catch:{ SSLException -> 0x0038 }
            java.lang.String r3 = "HostNameVerify: "
            r2.append(r3)     // Catch:{ SSLException -> 0x0038 }
            java.lang.String r3 = r5.mHostName     // Catch:{ SSLException -> 0x0038 }
            r2.append(r3)     // Catch:{ SSLException -> 0x0038 }
            java.lang.String r2 = r2.toString()     // Catch:{ SSLException -> 0x0038 }
            int r3 = r5.mPhoneId     // Catch:{ SSLException -> 0x0038 }
            com.sec.internal.log.AECLog.s(r1, r2, r3)     // Catch:{ SSLException -> 0x0038 }
            java.security.cert.Certificate[] r7 = r7.getPeerCertificates()     // Catch:{ SSLException -> 0x0038 }
            int r1 = r7.length     // Catch:{ SSLException -> 0x0038 }
            r2 = r0
        L_0x0023:
            if (r0 >= r1) goto L_0x0042
            r3 = r7[r0]     // Catch:{ SSLException -> 0x0037 }
            java.lang.String r4 = r5.getHostName()     // Catch:{ SSLException -> 0x0037 }
            java.security.cert.X509Certificate r3 = (java.security.cert.X509Certificate) r3     // Catch:{ SSLException -> 0x0037 }
            boolean r2 = r6.verify((java.lang.String) r4, (java.security.cert.X509Certificate) r3)     // Catch:{ SSLException -> 0x0037 }
            if (r2 == 0) goto L_0x0034
            goto L_0x0042
        L_0x0034:
            int r0 = r0 + 1
            goto L_0x0023
        L_0x0037:
            r0 = r2
        L_0x0038:
            java.lang.String r6 = LOG_TAG
            java.lang.String r7 = "SSL Exception with HostNameVerify Fail"
            int r5 = r5.mPhoneId
            com.sec.internal.log.AECLog.e(r6, r7, r5)
            r2 = r0
        L_0x0042:
            return r2
        */
        throw new UnsupportedOperationException("Method not decompiled: com.sec.internal.ims.aec.util.HttpClient.lambda$openURLConnection$0(java.lang.String, javax.net.ssl.SSLSession):boolean");
    }

    public Response getURLConnection(String str) throws Exception {
        try {
            String str2 = LOG_TAG;
            AECLog.i(str2, "[HTTP GET] " + str, this.mPhoneId);
            HttpsURLConnection openURLConnection = openURLConnection(appendQueryParams(str, this.mParams));
            this.mHttpsURLConn = openURLConnection;
            setRequestHeader(openURLConnection, this.mHeaders);
            this.mHttpsURLConn.setConnectTimeout(30000);
            this.mHttpsURLConn.setReadTimeout(30000);
            this.mHttpsURLConn.setRequestMethod("GET");
            this.mHttpsURLConn.setChunkedStreamingMode(0);
            this.mHttpsURLConn.connect();
            return getResponse(this.mHttpsURLConn);
        } catch (IOException e) {
            closeURLConnection();
            throw new IOException("getURLConnection IOException: " + e.getMessage());
        } catch (Exception e2) {
            closeURLConnection();
            throw new Exception("getURLConnection Exception: " + e2.getMessage());
        }
    }

    public Response postURLConnection(String str) throws Exception {
        OutputStream outputStream;
        try {
            String str2 = LOG_TAG;
            AECLog.i(str2, "[HTTP POST] " + str, this.mPhoneId);
            CookieHandler.setDefault((CookieHandler) null);
            HttpsURLConnection openURLConnection = openURLConnection(str);
            this.mHttpsURLConn = openURLConnection;
            setRequestHeader(openURLConnection, this.mHeaders);
            AECLog.d(str2, getPostData(), this.mPhoneId);
            this.mHttpsURLConn.setConnectTimeout(30000);
            this.mHttpsURLConn.setReadTimeout(30000);
            this.mHttpsURLConn.setRequestMethod("POST");
            this.mHttpsURLConn.setUseCaches(false);
            this.mHttpsURLConn.setDoOutput(true);
            this.mHttpsURLConn.setDoInput(true);
            outputStream = this.mHttpsURLConn.getOutputStream();
            outputStream.write(getPostData().getBytes(StandardCharsets.UTF_8));
            outputStream.flush();
            outputStream.close();
            return getResponse(this.mHttpsURLConn);
        } catch (IOException e) {
            closeURLConnection();
            throw new IOException("postURLConnection IOException: " + e.getMessage());
        } catch (Exception e2) {
            closeURLConnection();
            throw new Exception("postURLConnection Exception: " + e2.getMessage());
        } catch (Throwable th) {
            th.addSuppressed(th);
        }
        throw th;
    }

    public void closeURLConnection() {
        HttpsURLConnection httpsURLConnection = this.mHttpsURLConn;
        if (httpsURLConnection != null) {
            httpsURLConnection.disconnect();
            this.mHttpsURLConn = null;
        }
    }

    /* access modifiers changed from: protected */
    public String appendQueryParams(String str, Map<String, String> map) {
        Uri.Builder buildUpon = Uri.parse(str).buildUpon();
        if (map != null && map.size() > 0) {
            for (Map.Entry next : map.entrySet()) {
                if (((String) next.getValue()).contains(",")) {
                    for (String trim : ((String) next.getValue()).split(",")) {
                        buildUpon.appendQueryParameter((String) next.getKey(), trim.trim());
                    }
                } else {
                    buildUpon.appendQueryParameter((String) next.getKey(), (String) next.getValue());
                }
            }
            AECLog.s(LOG_TAG, buildUpon.toString(), this.mPhoneId);
        }
        return buildUpon.toString();
    }

    /* access modifiers changed from: protected */
    public void setRequestHeader(HttpURLConnection httpURLConnection, Map<String, List<String>> map) {
        for (Map.Entry next : map.entrySet()) {
            for (String str : (List) next.getValue()) {
                httpURLConnection.setRequestProperty((String) next.getKey(), str);
                String str2 = LOG_TAG;
                AECLog.i(str2, ((String) next.getKey()) + " : " + str, this.mPhoneId);
            }
        }
    }

    /* access modifiers changed from: protected */
    public Response getResponse(HttpURLConnection httpURLConnection) {
        return new Response(getResStatusCode(httpURLConnection), getResHeader(httpURLConnection), getResBody(httpURLConnection));
    }

    /* access modifiers changed from: protected */
    public int getResStatusCode(HttpURLConnection httpURLConnection) {
        try {
            return httpURLConnection.getResponseCode();
        } catch (IOException unused) {
            return 0;
        }
    }

    /* access modifiers changed from: protected */
    public Map<String, List<String>> getResHeader(HttpURLConnection httpURLConnection) {
        return httpURLConnection.getHeaderFields();
    }

    /* access modifiers changed from: protected */
    /* JADX WARNING: Can't wrap try/catch for region: R(6:21|20|23|24|(0)|28) */
    /* JADX WARNING: Code restructure failed: missing block: B:22:0x0042, code lost:
        r8 = th;
     */
    /* JADX WARNING: Code restructure failed: missing block: B:27:?, code lost:
        r3.close();
     */
    /* JADX WARNING: Code restructure failed: missing block: B:31:?, code lost:
        r3.close();
     */
    /* JADX WARNING: Code restructure failed: missing block: B:32:0x0059, code lost:
        com.sec.internal.log.AECLog.e(LOG_TAG, "failed to close input stream", r7.mPhoneId);
     */
    /* JADX WARNING: Failed to process nested try/catch */
    /* JADX WARNING: Missing exception handler attribute for start block: B:23:0x0044 */
    /* JADX WARNING: Removed duplicated region for block: B:26:0x004f A[SYNTHETIC, Splitter:B:26:0x004f] */
    /* JADX WARNING: Removed duplicated region for block: B:30:0x0055 A[SYNTHETIC, Splitter:B:30:0x0055] */
    /* Code decompiled incorrectly, please refer to instructions dump. */
    public byte[] getResBody(java.net.HttpURLConnection r8) {
        /*
            r7 = this;
            java.lang.String r0 = "failed to close input stream"
            java.lang.String r1 = ""
            java.nio.charset.Charset r2 = java.nio.charset.StandardCharsets.UTF_8
            byte[] r1 = r1.getBytes(r2)
            r2 = 512000(0x7d000, float:7.17465E-40)
            byte[] r2 = new byte[r2]
            r3 = 0
            java.io.BufferedInputStream r4 = new java.io.BufferedInputStream     // Catch:{ IOException -> 0x0044 }
            java.io.InputStream r8 = r8.getInputStream()     // Catch:{ IOException -> 0x0044 }
            r4.<init>(r8)     // Catch:{ IOException -> 0x0044 }
            r8 = 0
            r3 = r8
        L_0x001b:
            r5 = 61440(0xf000, float:8.6096E-41)
            int r5 = r4.read(r2, r3, r5)     // Catch:{ IOException -> 0x0040, all -> 0x003d }
            if (r5 <= 0) goto L_0x0025
            int r3 = r3 + r5
        L_0x0025:
            if (r5 >= 0) goto L_0x001b
            r6 = -1
            if (r5 != r6) goto L_0x0031
            if (r3 <= 0) goto L_0x0031
            byte[] r1 = new byte[r3]     // Catch:{ IOException -> 0x0040, all -> 0x003d }
            java.lang.System.arraycopy(r2, r8, r1, r8, r3)     // Catch:{ IOException -> 0x0040, all -> 0x003d }
        L_0x0031:
            r4.close()     // Catch:{ IOException -> 0x0035 }
            goto L_0x0052
        L_0x0035:
            java.lang.String r8 = LOG_TAG
            int r7 = r7.mPhoneId
            com.sec.internal.log.AECLog.e(r8, r0, r7)
            goto L_0x0052
        L_0x003d:
            r8 = move-exception
            r3 = r4
            goto L_0x0053
        L_0x0040:
            r3 = r4
            goto L_0x0044
        L_0x0042:
            r8 = move-exception
            goto L_0x0053
        L_0x0044:
            java.lang.String r8 = LOG_TAG     // Catch:{ all -> 0x0042 }
            java.lang.String r2 = "failed to read input stream"
            int r4 = r7.mPhoneId     // Catch:{ all -> 0x0042 }
            com.sec.internal.log.AECLog.e(r8, r2, r4)     // Catch:{ all -> 0x0042 }
            if (r3 == 0) goto L_0x0052
            r3.close()     // Catch:{ IOException -> 0x0035 }
        L_0x0052:
            return r1
        L_0x0053:
            if (r3 == 0) goto L_0x0060
            r3.close()     // Catch:{ IOException -> 0x0059 }
            goto L_0x0060
        L_0x0059:
            java.lang.String r1 = LOG_TAG
            int r7 = r7.mPhoneId
            com.sec.internal.log.AECLog.e(r1, r0, r7)
        L_0x0060:
            throw r8
        */
        throw new UnsupportedOperationException("Method not decompiled: com.sec.internal.ims.aec.util.HttpClient.getResBody(java.net.HttpURLConnection):byte[]");
    }

    public class Response {
        private final byte[] mBody;
        private final Map<String, List<String>> mHeader;
        private final int mStatusCode;

        public Response(int i, Map<String, List<String>> map, byte[] bArr) {
            this.mStatusCode = i;
            this.mHeader = map;
            this.mBody = bArr;
            debugPrint();
        }

        public int getStatusCode() {
            return this.mStatusCode;
        }

        public Map<String, List<String>> getHeader() {
            return this.mHeader;
        }

        public byte[] getBody() {
            return this.mBody;
        }

        private void debugPrint() {
            String r0 = HttpClient.LOG_TAG;
            AECLog.i(r0, "[HTTP " + this.mStatusCode + "]", HttpClient.this.mPhoneId);
            Map<String, List<String>> map = this.mHeader;
            if (map != null && map.size() > 0) {
                for (Map.Entry next : this.mHeader.entrySet()) {
                    StringBuilder sb = new StringBuilder();
                    sb.append((String) next.getKey());
                    sb.append(" : ");
                    for (String append : (List) next.getValue()) {
                        sb.append(append);
                    }
                    AECLog.i(HttpClient.LOG_TAG, sb.toString(), HttpClient.this.mPhoneId);
                }
            }
        }
    }
}
