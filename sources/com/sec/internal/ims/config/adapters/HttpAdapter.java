package com.sec.internal.ims.config.adapters;

import android.content.Context;
import android.net.Network;
import android.net.TrafficStats;
import android.os.Process;
import com.sec.internal.constants.ims.cmstore.ScheduleConstant;
import com.sec.internal.helper.HttpRequest;
import com.sec.internal.helper.header.AuthenticationHeaders;
import com.sec.internal.helper.httpclient.HttpController;
import com.sec.internal.helper.os.Debug;
import com.sec.internal.ims.registry.ImsRegistry;
import com.sec.internal.ims.servicemodules.ss.UtStateMachine;
import com.sec.internal.interfaces.ims.config.IHttpAdapter;
import com.sec.internal.log.IMSLog;
import java.io.BufferedInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InterruptedIOException;
import java.io.UnsupportedEncodingException;
import java.net.ConnectException;
import java.net.CookieHandler;
import java.net.CookieManager;
import java.net.CookieStore;
import java.net.HttpCookie;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.SocketException;
import java.net.SocketTimeoutException;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.URL;
import java.net.URLConnection;
import java.net.URLEncoder;
import java.net.UnknownHostException;
import java.security.KeyManagementException;
import java.security.NoSuchAlgorithmException;
import java.security.SecureRandom;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import javax.net.ssl.HttpsURLConnection;
import javax.net.ssl.KeyManager;
import javax.net.ssl.SSLContext;
import javax.net.ssl.SSLException;
import javax.net.ssl.SSLHandshakeException;
import javax.net.ssl.SSLSocketFactory;
import javax.net.ssl.TrustManager;

public class HttpAdapter implements IHttpAdapter {
    protected static final String CHUNKED = "chunked";
    protected static final String GZIP = "gzip";
    protected static final String LOG_TAG = "HttpAdapter";
    protected static final int MAX_CHUNK_SIZE = 512000;
    protected static final long MAX_TIMEOUT = 30000;
    protected static final int MIN_CHUNK_SIZE = 61440;
    protected static final String SSL_PROTOCOL = "TLS";
    protected static CookieStore sCookieStore;
    protected final Map<String, List<String>> mHeaders = new HashMap();
    protected String mHttpMethodName = "GET";
    protected HttpURLConnection mHttpURLConn = null;
    protected Network mNetwork = null;
    protected final Map<String, String> mParams = new HashMap();
    protected int mPhoneId = 0;
    protected State mState;
    protected URL mURL = null;
    protected URLConnection mURLConn = null;
    protected StringBuffer mUrl = null;

    protected interface State extends IHttpAdapter {
    }

    public void setMethod(String str) {
    }

    static {
        CookieManager cookieManager = new CookieManager();
        CookieHandler.setDefault(cookieManager);
        sCookieStore = cookieManager.getCookieStore();
    }

    public HttpAdapter(int i) {
        this.mPhoneId = i;
        this.mState = new IdleState();
    }

    public boolean open(String str) {
        return this.mState.open(str);
    }

    public boolean close() {
        return this.mState.close();
    }

    public void setHeaders(Map<String, List<String>> map) {
        this.mState.setHeaders(map);
    }

    public void setParams(Map<String, String> map) {
        this.mState.setParams(map);
    }

    public void setContext(Context context) {
        this.mState.setContext(context);
    }

    public void setNetwork(Network network) {
        this.mState.setNetwork(network);
    }

    public Network getNetwork() {
        return this.mState.getNetwork();
    }

    public IHttpAdapter.Response request() {
        return this.mState.request();
    }

    protected class IdleState implements State {
        public boolean close() {
            return false;
        }

        public IHttpAdapter.Response request() {
            return null;
        }

        public void setContext(Context context) {
        }

        protected IdleState() {
        }

        public boolean open(String str) {
            if (!HttpAdapter.this.configureUrlConnection(str)) {
                return false;
            }
            HttpAdapter httpAdapter = HttpAdapter.this;
            httpAdapter.mState = new ReadyState();
            return true;
        }

        public void setHeaders(Map<String, List<String>> map) {
            HttpAdapter.this.mHeaders.clear();
            HttpAdapter.this.mHeaders.putAll(map);
        }

        public void setMethod(String str) {
            HttpAdapter.this.mState.setMethod(str);
            HttpAdapter.this.mHttpMethodName = str;
        }

        public void setParams(Map<String, String> map) {
            HttpAdapter.this.mParams.clear();
            HttpAdapter.this.mParams.putAll(map);
        }

        public void setNetwork(Network network) {
            String str = HttpAdapter.LOG_TAG;
            int i = HttpAdapter.this.mPhoneId;
            IMSLog.i(str, i, "setNetwork: " + network);
            HttpAdapter.this.mNetwork = network;
        }

        public Network getNetwork() {
            String str = HttpAdapter.LOG_TAG;
            int i = HttpAdapter.this.mPhoneId;
            IMSLog.i(str, i, "getNetwork: " + HttpAdapter.this.mNetwork);
            return HttpAdapter.this.mNetwork;
        }
    }

    protected class ReadyState implements State {
        public Network getNetwork() {
            return null;
        }

        public boolean open(String str) {
            return false;
        }

        public void setContext(Context context) {
        }

        public void setHeaders(Map<String, List<String>> map) {
        }

        public void setNetwork(Network network) {
        }

        public void setParams(Map<String, String> map) {
        }

        protected ReadyState() {
        }

        public boolean close() {
            HttpAdapter.this.mHttpURLConn.disconnect();
            HttpAdapter httpAdapter = HttpAdapter.this;
            httpAdapter.mState = new IdleState();
            return true;
        }

        public void setMethod(String str) {
            HttpAdapter.this.mHttpMethodName = str;
        }

        public IHttpAdapter.Response request() {
            HttpAdapter.this.tryToConnectHttpUrlConnectionWithinTimeOut();
            String stringBuffer = HttpAdapter.this.mUrl.toString();
            HttpAdapter httpAdapter = HttpAdapter.this;
            int resStatusCode = httpAdapter.getResStatusCode(httpAdapter.mHttpURLConn);
            HttpAdapter httpAdapter2 = HttpAdapter.this;
            String resStatusMessage = httpAdapter2.getResStatusMessage(httpAdapter2.mHttpURLConn);
            HttpAdapter httpAdapter3 = HttpAdapter.this;
            Map<String, List<String>> resHeader = httpAdapter3.getResHeader(httpAdapter3.mHttpURLConn);
            boolean equalsIgnoreCase = HttpAdapter.this.mHttpMethodName.equalsIgnoreCase("POST");
            HttpAdapter httpAdapter4 = HttpAdapter.this;
            return new IHttpAdapter.Response(stringBuffer, resStatusCode, resStatusMessage, resHeader, equalsIgnoreCase ? httpAdapter4.getPostResBody(httpAdapter4.mHttpURLConn) : httpAdapter4.getResBody(httpAdapter4.mHttpURLConn));
        }
    }

    /* access modifiers changed from: protected */
    public boolean configureUrlConnection(String str) {
        this.mUrl = createReqUrl(str, new StringBuffer(str), this.mParams);
        if (!openUrlConnection()) {
            return false;
        }
        setUrlConnection();
        IMSLog.i(LOG_TAG, this.mPhoneId, "configure httpUrlConnection based on urlConnection");
        this.mHttpURLConn = (HttpURLConnection) this.mURLConn;
        return true;
    }

    /* access modifiers changed from: protected */
    public StringBuffer createReqUrl(String str, StringBuffer stringBuffer, Map<String, String> map) {
        IMSLog.i(LOG_TAG, this.mPhoneId, str);
        if (!(stringBuffer == null || map == null || map.size() <= 0)) {
            if (this.mHttpMethodName.equalsIgnoreCase("GET")) {
                if (stringBuffer.charAt(stringBuffer.length() - 1) == '/') {
                    stringBuffer.deleteCharAt(stringBuffer.length() - 1);
                }
                stringBuffer.append("?");
                for (Map.Entry next : map.entrySet()) {
                    stringBuffer.append((String) next.getKey());
                    stringBuffer.append(AuthenticationHeaders.HEADER_PRARAM_SPERATOR);
                    try {
                        if (next.getValue() == null) {
                            stringBuffer.append("&");
                        } else {
                            if (((String) next.getValue()).contains("%")) {
                                stringBuffer.append((String) next.getValue());
                            } else {
                                stringBuffer.append(URLEncoder.encode((String) next.getValue(), "utf-8"));
                            }
                            stringBuffer.append("&");
                        }
                    } catch (UnsupportedEncodingException unused) {
                        IMSLog.e(LOG_TAG, this.mPhoneId, "UnsupportedEncodingException occur. use plain string");
                        stringBuffer.append((String) next.getValue());
                    }
                }
                stringBuffer.deleteCharAt(stringBuffer.length() - 1);
            } else if (this.mHttpMethodName.equalsIgnoreCase("POST")) {
                StringBuilder sb = new StringBuilder();
                if (stringBuffer.charAt(stringBuffer.length() - 1) == '/') {
                    stringBuffer.deleteCharAt(stringBuffer.length() - 1);
                }
                stringBuffer.append("?");
                for (Map.Entry next2 : map.entrySet()) {
                    if (sb.length() != 0) {
                        sb.append('&');
                    }
                    sb.append((String) next2.getKey());
                    sb.append(AuthenticationHeaders.HEADER_PRARAM_SPERATOR);
                    try {
                        if (((String) next2.getValue()).contains("%")) {
                            IMSLog.e(LOG_TAG, "already encoded. use plain string");
                            sb.append((String) next2.getValue());
                        } else {
                            sb.append(URLEncoder.encode((String) next2.getValue(), "utf-8"));
                        }
                    } catch (UnsupportedEncodingException unused2) {
                        IMSLog.i(LOG_TAG, "UnsupportedEncodingException occur. use plain string");
                        sb.append((String) next2.getValue());
                    }
                }
                stringBuffer.append(sb.toString());
            }
            IMSLog.s(LOG_TAG, this.mPhoneId, stringBuffer.toString());
        }
        return stringBuffer;
    }

    /* access modifiers changed from: protected */
    public boolean openUrlConnection() {
        try {
            URL url = new URL(this.mUrl.toString());
            this.mURL = url;
            Network network = this.mNetwork;
            this.mURLConn = network != null ? network.openConnection(url) : url.openConnection();
            return true;
        } catch (MalformedURLException e) {
            IMSLog.i(LOG_TAG, this.mPhoneId, "wrong url address");
            e.printStackTrace();
            return false;
        } catch (IOException e2) {
            IMSLog.i(LOG_TAG, this.mPhoneId, "cannot open url connection");
            e2.printStackTrace();
            return false;
        }
    }

    /* access modifiers changed from: protected */
    public void setUrlConnection() {
        if (this.mURLConn instanceof HttpsURLConnection) {
            setSocketFactory();
        } else {
            removeOldCookies();
        }
    }

    /* access modifiers changed from: protected */
    public void setSocketFactory() {
        try {
            SSLContext instance = SSLContext.getInstance(SSL_PROTOCOL);
            instance.init((KeyManager[]) null, (TrustManager[]) null, (SecureRandom) null);
            SSLSocketFactory socketFactory = instance.getSocketFactory();
            IMSLog.i(LOG_TAG, this.mPhoneId, "get socketFactory for HTTPS");
            setSSLSocketFactory(socketFactory);
        } catch (KeyManagementException | NoSuchAlgorithmException e) {
            throw new Error(e);
        }
    }

    /* access modifiers changed from: protected */
    public void setSSLSocketFactory(SSLSocketFactory sSLSocketFactory) {
        IMSLog.i(LOG_TAG, this.mPhoneId, "set sslSocketFactory for HTTPS");
        ((HttpsURLConnection) this.mURLConn).setSSLSocketFactory(sSLSocketFactory);
    }

    /* access modifiers changed from: protected */
    public void removeOldCookies() {
        try {
            URI uri = this.mURL.toURI();
            for (HttpCookie remove : sCookieStore.get(uri)) {
                sCookieStore.remove(uri, remove);
            }
            IMSLog.i(LOG_TAG, this.mPhoneId, "remove old cookies for HTTP request");
        } catch (URISyntaxException e) {
            throw new Error(e);
        }
    }

    /* access modifiers changed from: protected */
    public void tryToConnectHttpUrlConnectionWithinTimeOut() {
        long timeInMillis = Calendar.getInstance().getTimeInMillis();
        TrafficStats.setThreadStatsTag(Process.myTid());
        long j = 30000;
        while (this.mState instanceof ReadyState) {
            if (j < 30000) {
                try {
                    URL url = new URL(this.mUrl.toString());
                    this.mURL = url;
                    Network network = this.mNetwork;
                    this.mURLConn = network != null ? network.openConnection(url) : url.openConnection();
                    setUrlConnection();
                    this.mHttpURLConn = (HttpURLConnection) this.mURLConn;
                } catch (MalformedURLException e) {
                    e.printStackTrace();
                } catch (IOException e2) {
                    e2.printStackTrace();
                    this.mHttpURLConn.disconnect();
                    j = Calendar.getInstance().getTimeInMillis() - timeInMillis;
                }
            }
            setHttpUrlConnection();
            addReqHeader(this.mHttpURLConn, this.mHeaders);
            this.mHttpURLConn.connect();
            j = 30000;
            int i = (j > 30000 ? 1 : (j == 30000 ? 0 : -1));
            if (i < 0) {
                sleep(UtStateMachine.HTTP_READ_TIMEOUT_GCF);
                continue;
            }
            if (i >= 0) {
                return;
            }
        }
        IMSLog.i(LOG_TAG, this.mPhoneId, "NOT ReadyState");
    }

    /* access modifiers changed from: protected */
    public void tryToConnectHttpUrlConnection() {
        TrafficStats.setThreadStatsTag(Process.myTid());
        try {
            setHttpUrlConnection();
            addReqHeader(this.mHttpURLConn, this.mHeaders);
            this.mHttpURLConn.connect();
        } catch (IOException e) {
            e.printStackTrace();
            this.mHttpURLConn.disconnect();
        }
    }

    /* access modifiers changed from: protected */
    public void setHttpUrlConnection() throws IOException {
        HttpURLConnection.setFollowRedirects(false);
        this.mHttpURLConn.setConnectTimeout(30000);
        this.mHttpURLConn.setReadTimeout(ScheduleConstant.UPDATE_SUBSCRIPTION_DELAY_TIME);
        if (this.mHttpMethodName.equalsIgnoreCase("POST")) {
            this.mHttpURLConn.setRequestMethod("POST");
        } else {
            this.mHttpURLConn.setRequestMethod("GET");
        }
        this.mHttpURLConn.setChunkedStreamingMode(0);
    }

    /* access modifiers changed from: protected */
    public void addReqHeader(HttpURLConnection httpURLConnection, Map<String, List<String>> map) {
        IMSLog.i(LOG_TAG, this.mPhoneId, "+++ request header");
        for (Map.Entry next : map.entrySet()) {
            boolean z = true;
            for (String str : (List) next.getValue()) {
                if (z) {
                    httpURLConnection.setRequestProperty((String) next.getKey(), str);
                } else {
                    httpURLConnection.addRequestProperty((String) next.getKey(), str);
                }
                displayReqHeader((String) next.getKey(), str);
                z = false;
            }
        }
        IMSLog.i(LOG_TAG, this.mPhoneId, "--- request header");
        if (Debug.ALLOW_DIAGNOSTICS) {
            StringBuilder sb = new StringBuilder();
            String format = new SimpleDateFormat("MM/dd/yyyy HH:mm:ss.SSS", Locale.US).format(new Date());
            sb.append(httpURLConnection instanceof HttpsURLConnection ? "HTTPS " : "HTTP ");
            sb.append(this.mHttpMethodName.equalsIgnoreCase("POST") ? "POST\n" : "GET\n");
            for (Map.Entry next2 : map.entrySet()) {
                for (String str2 : (List) next2.getValue()) {
                    sb.append((String) next2.getKey());
                    sb.append(": ");
                    if (((String) next2.getKey()).equals(HttpController.HEADER_HOST)) {
                        str2 = this.mUrl.toString();
                    }
                    sb.append(str2);
                    sb.append("\n");
                }
            }
            ImsRegistry.getImsDiagMonitor().onIndication(1, sb.toString(), 100, 0, format, "", "", "");
        }
    }

    /* access modifiers changed from: protected */
    public void displayReqHeader(String str, String str2) {
        String str3 = LOG_TAG;
        int i = this.mPhoneId;
        IMSLog.i(str3, i, str + ":" + str2);
    }

    /* access modifiers changed from: protected */
    public int getResStatusCode(HttpURLConnection httpURLConnection) {
        int i;
        int i2 = 0;
        try {
            return httpURLConnection.getResponseCode();
        } catch (IOException e) {
            IMSLog.i(LOG_TAG, this.mPhoneId, "getResStatusCode: fail to read status code");
            if (e instanceof SSLHandshakeException) {
                IMSLog.i(LOG_TAG, this.mPhoneId, "SSLHandshakeException: response code define 800");
                i = 800;
            } else if (e instanceof SSLException) {
                IMSLog.i(LOG_TAG, this.mPhoneId, "SSLException: response code define 801");
                i = 801;
            } else {
                if (!(e instanceof ConnectException)) {
                    if (!(e instanceof InterruptedIOException)) {
                        if (e instanceof SocketException) {
                            IMSLog.i(LOG_TAG, this.mPhoneId, "SocketException: response code define 803");
                            i = 803;
                        } else if (e instanceof SocketTimeoutException) {
                            IMSLog.i(LOG_TAG, this.mPhoneId, "SocketTimeoutException: response code define 804");
                            i = 804;
                        } else {
                            if (e instanceof UnknownHostException) {
                                IMSLog.i(LOG_TAG, this.mPhoneId, "UnknownHostException: response code define 805");
                                i = 805;
                            }
                            e.printStackTrace();
                        }
                    }
                }
                IMSLog.i(LOG_TAG, this.mPhoneId, "ConnectException: response code define 802");
                i = 802;
            }
            i2 = i;
            e.printStackTrace();
        } catch (Throwable unused) {
        }
        return i2;
    }

    /* access modifiers changed from: protected */
    public String getResStatusMessage(HttpURLConnection httpURLConnection) {
        try {
            return httpURLConnection.getResponseMessage();
        } catch (IOException e) {
            String str = LOG_TAG;
            int i = this.mPhoneId;
            IMSLog.i(str, i, "getResStatusMessage: IOException: " + e.getMessage());
        } catch (Throwable unused) {
        }
        return "";
    }

    /* access modifiers changed from: protected */
    public Map<String, List<String>> getResHeader(HttpURLConnection httpURLConnection) {
        return httpURLConnection.getHeaderFields();
    }

    /* access modifiers changed from: protected */
    public byte[] getContentLengthBody(byte[] bArr, HttpURLConnection httpURLConnection, int i) {
        BufferedInputStream bufferedInputStream;
        try {
            bufferedInputStream = new BufferedInputStream(httpURLConnection.getInputStream());
            int i2 = i * 2;
            byte[] bArr2 = new byte[i2];
            bArr = new byte[i];
            int i3 = 0;
            while (true) {
                int read = bufferedInputStream.read(bArr2, i3, i2 - i3);
                if (read == -1) {
                    break;
                }
                i3 += read;
            }
            if (i != i3) {
                IMSLog.i(LOG_TAG, this.mPhoneId, "getContentLengthBody: wrong http header(header:" + i + ",actual:" + i3 + ")");
            }
            System.arraycopy(bArr2, 0, bArr, 0, i);
            bufferedInputStream.close();
            return bArr;
        } catch (FileNotFoundException unused) {
            IMSLog.i(LOG_TAG, this.mPhoneId, "getContentLengthBody: no body");
            return bArr;
        } catch (IOException e) {
            try {
                IMSLog.i(LOG_TAG, this.mPhoneId, "getContentLengthBody: fail to read body");
                e.printStackTrace();
                return bArr;
            } catch (Throwable unused2) {
                return bArr;
            }
        } catch (Throwable th) {
            th.addSuppressed(th);
        }
        throw th;
    }

    /* access modifiers changed from: protected */
    public byte[] getTransferEncodingBody(byte[] bArr, HttpURLConnection httpURLConnection) {
        byte[] bArr2;
        int i;
        int i2;
        boolean z;
        try {
            BufferedInputStream bufferedInputStream = new BufferedInputStream(httpURLConnection.getInputStream());
            try {
                bArr2 = new byte[512000];
                i = 0;
                i2 = 0;
                do {
                    i2 = bufferedInputStream.read(bArr2, i, 61440);
                    if (i2 > 0) {
                        i += i2;
                        continue;
                    }
                } while (i2 >= 0);
                z = false;
            } catch (IOException e) {
                IMSLog.i(LOG_TAG, this.mPhoneId, "getTransferEncodingBody: error reading chunked input stream" + e.getMessage());
                z = true;
            } catch (Throwable th) {
                bufferedInputStream.close();
                throw th;
            }
            if (i2 != -1 || i <= 0 || z) {
                IMSLog.i(LOG_TAG, this.mPhoneId, "getTransferEncodingBody: chunked body empty or error");
            } else {
                bArr = new byte[i];
                System.arraycopy(bArr2, 0, bArr, 0, i);
                IMSLog.i(LOG_TAG, this.mPhoneId, "getTransferEncodingBody: chunked response length [" + i + "]");
            }
            bufferedInputStream.close();
            return bArr;
        } catch (IOException e2) {
            try {
                IMSLog.i(LOG_TAG, this.mPhoneId, "getTransferEncodingBody: fail to read body");
                e2.printStackTrace();
            } catch (Throwable unused) {
            }
            return bArr;
        } catch (Throwable th2) {
            th.addSuppressed(th2);
        }
    }

    /* access modifiers changed from: protected */
    public byte[] getResBody(HttpURLConnection httpURLConnection) {
        byte[] bArr = null;
        if (httpURLConnection.getHeaderField("Content-Length") != null) {
            int parseInt = Integer.parseInt(httpURLConnection.getHeaderField("Content-Length"));
            String str = LOG_TAG;
            int i = this.mPhoneId;
            IMSLog.i(str, i, "getResBody: Content-Length " + parseInt);
            if (parseInt <= 0) {
                return null;
            }
            bArr = getContentLengthBody((byte[]) null, httpURLConnection, parseInt);
        }
        if (!CHUNKED.equals(httpURLConnection.getHeaderField(HttpRequest.HEADER_TRANSFER_ENCODING))) {
            return bArr;
        }
        IMSLog.i(LOG_TAG, this.mPhoneId, "getResBody: Transfer-Encoding");
        return getTransferEncodingBody(bArr, httpURLConnection);
    }

    /* access modifiers changed from: protected */
    /* JADX WARNING: Code restructure failed: missing block: B:30:?, code lost:
        r2.close();
     */
    /* JADX WARNING: Code restructure failed: missing block: B:31:0x007c, code lost:
        r9 = e;
     */
    /* JADX WARNING: Code restructure failed: missing block: B:32:0x007d, code lost:
        r0 = LOG_TAG;
        r1 = new java.lang.StringBuilder();
     */
    /* JADX WARNING: Failed to process nested try/catch */
    /* JADX WARNING: Missing exception handler attribute for start block: B:26:0x006f */
    /* JADX WARNING: Removed duplicated region for block: B:29:0x0078 A[SYNTHETIC, Splitter:B:29:0x0078] */
    /* JADX WARNING: Removed duplicated region for block: B:36:0x0099 A[SYNTHETIC, Splitter:B:36:0x0099] */
    /* Code decompiled incorrectly, please refer to instructions dump. */
    public byte[] getPostResBody(java.net.HttpURLConnection r9) {
        /*
            r8 = this;
            java.lang.String r8 = "Error closing input stream: "
            java.lang.String r0 = "Content-Length"
            java.lang.String r1 = r9.getHeaderField(r0)
            r2 = 0
            if (r1 == 0) goto L_0x00b7
            java.lang.String r0 = r9.getHeaderField(r0)
            int r0 = java.lang.Integer.parseInt(r0)
            if (r0 > 0) goto L_0x0016
            return r2
        L_0x0016:
            int r1 = r0 * 2
            byte[] r3 = new byte[r1]
            byte[] r4 = new byte[r0]
            java.io.BufferedInputStream r5 = new java.io.BufferedInputStream     // Catch:{ IOException -> 0x006f }
            java.io.InputStream r9 = r9.getInputStream()     // Catch:{ IOException -> 0x006f }
            r5.<init>(r9)     // Catch:{ IOException -> 0x006f }
            r9 = 0
            r2 = r9
        L_0x0027:
            int r6 = r1 - r2
            int r6 = r5.read(r3, r2, r6)     // Catch:{ IOException -> 0x006b, all -> 0x0068 }
            r7 = -1
            if (r6 == r7) goto L_0x0032
            int r2 = r2 + r6
            goto L_0x0027
        L_0x0032:
            if (r0 == r2) goto L_0x0058
            java.lang.String r1 = LOG_TAG     // Catch:{ IOException -> 0x006b, all -> 0x0068 }
            java.lang.StringBuilder r6 = new java.lang.StringBuilder     // Catch:{ IOException -> 0x006b, all -> 0x0068 }
            r6.<init>()     // Catch:{ IOException -> 0x006b, all -> 0x0068 }
            java.lang.String r7 = "wrong http header(header:"
            r6.append(r7)     // Catch:{ IOException -> 0x006b, all -> 0x0068 }
            r6.append(r0)     // Catch:{ IOException -> 0x006b, all -> 0x0068 }
            java.lang.String r7 = ",actual:"
            r6.append(r7)     // Catch:{ IOException -> 0x006b, all -> 0x0068 }
            r6.append(r2)     // Catch:{ IOException -> 0x006b, all -> 0x0068 }
            java.lang.String r2 = ")"
            r6.append(r2)     // Catch:{ IOException -> 0x006b, all -> 0x0068 }
            java.lang.String r2 = r6.toString()     // Catch:{ IOException -> 0x006b, all -> 0x0068 }
            com.sec.internal.log.IMSLog.e(r1, r2)     // Catch:{ IOException -> 0x006b, all -> 0x0068 }
        L_0x0058:
            java.lang.System.arraycopy(r3, r9, r4, r9, r0)     // Catch:{ IOException -> 0x006b, all -> 0x0068 }
            r5.close()     // Catch:{ IOException -> 0x005f }
            goto L_0x0095
        L_0x005f:
            r9 = move-exception
            java.lang.String r0 = LOG_TAG
            java.lang.StringBuilder r1 = new java.lang.StringBuilder
            r1.<init>()
            goto L_0x0084
        L_0x0068:
            r9 = move-exception
            r2 = r5
            goto L_0x0097
        L_0x006b:
            r2 = r5
            goto L_0x006f
        L_0x006d:
            r9 = move-exception
            goto L_0x0097
        L_0x006f:
            java.lang.String r9 = LOG_TAG     // Catch:{ all -> 0x006d }
            java.lang.String r0 = "fail to read body"
            com.sec.internal.log.IMSLog.e(r9, r0)     // Catch:{ all -> 0x006d }
            if (r2 == 0) goto L_0x0095
            r2.close()     // Catch:{ IOException -> 0x007c }
            goto L_0x0095
        L_0x007c:
            r9 = move-exception
            java.lang.String r0 = LOG_TAG
            java.lang.StringBuilder r1 = new java.lang.StringBuilder
            r1.<init>()
        L_0x0084:
            r1.append(r8)
            java.lang.String r8 = r9.getMessage()
            r1.append(r8)
            java.lang.String r8 = r1.toString()
            com.sec.internal.log.IMSLog.e(r0, r8)
        L_0x0095:
            r2 = r4
            goto L_0x00b7
        L_0x0097:
            if (r2 == 0) goto L_0x00b6
            r2.close()     // Catch:{ IOException -> 0x009d }
            goto L_0x00b6
        L_0x009d:
            r0 = move-exception
            java.lang.String r1 = LOG_TAG
            java.lang.StringBuilder r2 = new java.lang.StringBuilder
            r2.<init>()
            r2.append(r8)
            java.lang.String r8 = r0.getMessage()
            r2.append(r8)
            java.lang.String r8 = r2.toString()
            com.sec.internal.log.IMSLog.e(r1, r8)
        L_0x00b6:
            throw r9
        L_0x00b7:
            return r2
        */
        throw new UnsupportedOperationException("Method not decompiled: com.sec.internal.ims.config.adapters.HttpAdapter.getPostResBody(java.net.HttpURLConnection):byte[]");
    }

    /* access modifiers changed from: protected */
    public void sleep(long j) {
        try {
            Thread.sleep(j);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
    }
}
