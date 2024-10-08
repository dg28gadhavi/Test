package com.sec.internal.helper;

import android.net.Network;
import android.util.Log;
import com.sec.internal.helper.httpclient.HttpController;
import com.sec.internal.ims.util.ImsUtil;
import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.Closeable;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.Flushable;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.InetSocketAddress;
import java.net.MalformedURLException;
import java.net.Proxy;
import java.net.URL;
import java.nio.ByteBuffer;
import java.nio.CharBuffer;
import java.nio.charset.Charset;
import java.nio.charset.CharsetEncoder;
import java.security.GeneralSecurityException;
import java.security.cert.X509Certificate;
import java.util.concurrent.Callable;
import java.util.zip.GZIPInputStream;
import javax.net.ssl.HostnameVerifier;
import javax.net.ssl.HttpsURLConnection;
import javax.net.ssl.KeyManager;
import javax.net.ssl.SSLContext;
import javax.net.ssl.SSLSession;
import javax.net.ssl.SSLSocketFactory;
import javax.net.ssl.TrustManager;
import javax.net.ssl.X509TrustManager;

public class HttpRequest {
    public static final String BOUNDARY = "00content0boundary00";
    public static final String CHARSET_UTF8 = "UTF-8";
    private static ConnectionFactory CONNECTION_FACTORY = ConnectionFactory.DEFAULT;
    private static final String CONTENT_TYPE_MULTIPART = "multipart/form-data; boundary=00content0boundary00";
    private static final String CRLF = "\r\n";
    public static final String ENCODING_GZIP = "gzip";
    public static final String HEADER_ACCEPT = "Accept";
    public static final String HEADER_ACCEPT_ENCODING = "Accept-Encoding";
    public static final String HEADER_AUTHENTICATION_INFO = "Authentication-Info";
    public static final String HEADER_AUTHORIZATION = "Authorization";
    public static final String HEADER_CONTENT_ENCODING = "Content-Encoding";
    public static final String HEADER_CONTENT_LENGTH = "Content-Length";
    public static final String HEADER_CONTENT_RANGE = "Content-Range";
    public static final String HEADER_CONTENT_TYPE = "Content-Type";
    public static final String HEADER_DATE = "Date";
    public static final String HEADER_LOCATION = "Location";
    public static final String HEADER_RANGE = "Range";
    public static final String HEADER_RETRY_AFTER = "Retry-After";
    public static final String HEADER_SUPPORTED_VERSIONS = "Supported-Versions";
    public static final String HEADER_TRANSFER_ENCODING = "Transfer-Encoding";
    public static final String HEADER_USER_AGENT = "User-Agent";
    public static final String HEADER_WWW_AUTHENTICATE = "WWW-Authenticate";
    public static final String METHOD_GET = "GET";
    public static final String METHOD_POST = "POST";
    public static final String METHOD_PUT = "PUT";
    public static final String PARAM_CHARSET = "charset";
    private static SSLSocketFactory TRUSTED_FACTORY;
    private static HostnameVerifier TRUSTED_VERIFIER;
    /* access modifiers changed from: private */
    public int bufferSize = 8192;
    private HttpURLConnection connection = null;
    private String httpProxyHost;
    private int httpProxyPort;
    private boolean ignoreCloseExceptions = true;
    private boolean multipart;
    private Network network;
    private RequestOutputStream output;
    /* access modifiers changed from: private */
    public UploadProgress progress = UploadProgress.DEFAULT;
    private final String requestMethod;
    /* access modifiers changed from: private */
    public long totalSize = -1;
    /* access modifiers changed from: private */
    public long totalWritten = 0;
    private boolean uncompress = false;
    private final URL url;

    public interface ConnectionFactory {
        public static final ConnectionFactory DEFAULT = new ConnectionFactory() {
            public HttpURLConnection create(URL url, Network network) throws IOException {
                if (network != null) {
                    return (HttpURLConnection) network.openConnection(url);
                }
                return (HttpURLConnection) url.openConnection();
            }

            public HttpURLConnection create(URL url, Proxy proxy, Network network) throws IOException {
                if (network != null) {
                    return (HttpURLConnection) network.openConnection(url, proxy);
                }
                return (HttpURLConnection) url.openConnection(proxy);
            }
        };

        HttpURLConnection create(URL url, Network network) throws IOException;

        HttpURLConnection create(URL url, Proxy proxy, Network network) throws IOException;
    }

    public interface UploadProgress {
        public static final UploadProgress DEFAULT = new UploadProgress() {
            public boolean isCancelled() {
                return false;
            }

            public void onUpload(long j, long j2) {
            }
        };

        boolean isCancelled();

        void onUpload(long j, long j2);
    }

    /* access modifiers changed from: private */
    public static /* synthetic */ boolean lambda$trustAllHosts$0(String str, SSLSession sSLSession) {
        return true;
    }

    /* access modifiers changed from: private */
    public static String getValidCharset(String str) {
        return (str == null || str.length() <= 0) ? "UTF-8" : str;
    }

    private static SSLSocketFactory getTrustedFactory() throws HttpRequestException {
        SSLSocketFactory sSLSocketFactory;
        synchronized (HttpRequest.class) {
            if (TRUSTED_FACTORY == null) {
                TrustManager[] trustManagerArr = {new X509TrustManager() {
                    public void checkClientTrusted(X509Certificate[] x509CertificateArr, String str) {
                    }

                    public void checkServerTrusted(X509Certificate[] x509CertificateArr, String str) {
                    }

                    public X509Certificate[] getAcceptedIssuers() {
                        return new X509Certificate[0];
                    }
                }};
                try {
                    SSLContext instance = SSLContext.getInstance("TLS");
                    instance.init((KeyManager[]) null, trustManagerArr, ImsUtil.getRandom());
                    TRUSTED_FACTORY = instance.getSocketFactory();
                } catch (GeneralSecurityException e) {
                    IOException iOException = new IOException("Security exception configuring SSL context");
                    iOException.initCause(e);
                    throw new HttpRequestException(iOException);
                }
            }
            sSLSocketFactory = TRUSTED_FACTORY;
        }
        return sSLSocketFactory;
    }

    private static HostnameVerifier getTrustedVerifier() {
        HostnameVerifier hostnameVerifier;
        synchronized (HttpRequest.class) {
            if (TRUSTED_VERIFIER == null) {
                TRUSTED_VERIFIER = new HostnameVerifier() {
                    /* JADX DEBUG: Multi-variable search result rejected for TypeSearchVarInfo{r0v0, resolved type: int} */
                    /* JADX DEBUG: Multi-variable search result rejected for TypeSearchVarInfo{r0v1, resolved type: int} */
                    /* JADX DEBUG: Multi-variable search result rejected for TypeSearchVarInfo{r2v0, resolved type: boolean} */
                    /* JADX DEBUG: Multi-variable search result rejected for TypeSearchVarInfo{r0v2, resolved type: int} */
                    /* JADX DEBUG: Multi-variable search result rejected for TypeSearchVarInfo{r0v3, resolved type: int} */
                    /* JADX WARNING: Multi-variable type inference failed */
                    /* Code decompiled incorrectly, please refer to instructions dump. */
                    public boolean verify(java.lang.String r5, javax.net.ssl.SSLSession r6) {
                        /*
                            r4 = this;
                            okhttp3.internal.tls.OkHostnameVerifier r4 = okhttp3.internal.tls.OkHostnameVerifier.INSTANCE
                            r0 = 0
                            java.security.cert.Certificate[] r6 = r6.getPeerCertificates()     // Catch:{ SSLException -> 0x001a }
                            int r1 = r6.length     // Catch:{ SSLException -> 0x001a }
                            r2 = r0
                        L_0x0009:
                            if (r0 >= r1) goto L_0x0022
                            r3 = r6[r0]     // Catch:{ SSLException -> 0x0019 }
                            java.security.cert.X509Certificate r3 = (java.security.cert.X509Certificate) r3     // Catch:{ SSLException -> 0x0019 }
                            boolean r2 = r4.verify((java.lang.String) r5, (java.security.cert.X509Certificate) r3)     // Catch:{ SSLException -> 0x0019 }
                            if (r2 == 0) goto L_0x0016
                            goto L_0x0022
                        L_0x0016:
                            int r0 = r0 + 1
                            goto L_0x0009
                        L_0x0019:
                            r0 = r2
                        L_0x001a:
                            java.lang.String r4 = "HttpRequest"
                            java.lang.String r5 = "SSL Exception with HostNameVerify Fail"
                            android.util.Log.e(r4, r5)
                            r2 = r0
                        L_0x0022:
                            return r2
                        */
                        throw new UnsupportedOperationException("Method not decompiled: com.sec.internal.helper.HttpRequest.AnonymousClass2.verify(java.lang.String, javax.net.ssl.SSLSession):boolean");
                    }
                };
            }
            hostnameVerifier = TRUSTED_VERIFIER;
        }
        return hostnameVerifier;
    }

    public static class HttpRequestException extends RuntimeException {
        private static final long serialVersionUID = -1170466989781746231L;

        public HttpRequestException(IOException iOException) {
            super(iOException);
        }

        public IOException getCause() {
            return (IOException) super.getCause();
        }
    }

    protected static abstract class Operation<V> implements Callable<V> {
        /* access modifiers changed from: protected */
        public abstract void done() throws IOException;

        /* access modifiers changed from: protected */
        public abstract V run() throws HttpRequestException, IOException;

        protected Operation() {
        }

        public V call() throws HttpRequestException {
            boolean z;
            Throwable th;
            try {
                V run = run();
                try {
                    done();
                    return run;
                } catch (IOException e) {
                    throw new HttpRequestException(e);
                }
            } catch (HttpRequestException e2) {
                throw e2;
            } catch (IOException e3) {
                throw new HttpRequestException(e3);
            } catch (Throwable th2) {
                Throwable th3 = th2;
                z = true;
                th = th3;
                done();
                throw th;
            }
        }
    }

    protected static abstract class CloseOperation<V> extends Operation<V> {
        private final Closeable closeable;
        FileOutputStream fileOutputStream;
        private final boolean ignoreCloseExceptions;

        protected CloseOperation(Closeable closeable2, boolean z, FileOutputStream fileOutputStream2) {
            this.closeable = closeable2;
            this.ignoreCloseExceptions = z;
            this.fileOutputStream = fileOutputStream2;
        }

        /* access modifiers changed from: protected */
        public void done() throws IOException {
            Closeable closeable2 = this.closeable;
            if (closeable2 instanceof Flushable) {
                ((Flushable) closeable2).flush();
            }
            if (this.ignoreCloseExceptions) {
                try {
                    this.closeable.close();
                    FileOutputStream fileOutputStream2 = this.fileOutputStream;
                    if (fileOutputStream2 != null) {
                        fileOutputStream2.close();
                    }
                } catch (IOException unused) {
                }
            } else {
                this.closeable.close();
                FileOutputStream fileOutputStream3 = this.fileOutputStream;
                if (fileOutputStream3 != null) {
                    fileOutputStream3.close();
                }
            }
        }
    }

    public static class RequestOutputStream extends BufferedOutputStream {
        private final CharsetEncoder encoder;

        public RequestOutputStream(OutputStream outputStream, String str, int i) {
            super(outputStream, i);
            this.encoder = Charset.forName(HttpRequest.getValidCharset(str)).newEncoder();
        }

        public RequestOutputStream write(String str) throws IOException {
            ByteBuffer encode = this.encoder.encode(CharBuffer.wrap(str));
            super.write(encode.array(), 0, encode.limit());
            return this;
        }
    }

    public static HttpRequest get(CharSequence charSequence) throws HttpRequestException {
        return new HttpRequest(charSequence, "GET");
    }

    public static HttpRequest post(CharSequence charSequence) throws HttpRequestException {
        return new HttpRequest(charSequence, "POST");
    }

    public static HttpRequest put(CharSequence charSequence) throws HttpRequestException {
        return new HttpRequest(charSequence, "PUT");
    }

    public static HttpRequest put(URL url2) throws HttpRequestException {
        return new HttpRequest(url2, "PUT");
    }

    public HttpRequest(CharSequence charSequence, String str) throws HttpRequestException {
        try {
            this.url = new URL(charSequence.toString());
            this.requestMethod = str;
        } catch (MalformedURLException e) {
            throw new HttpRequestException(e);
        }
    }

    public HttpRequest(URL url2, String str) throws HttpRequestException {
        this.url = url2;
        this.requestMethod = str;
    }

    private Proxy createProxy() {
        return new Proxy(Proxy.Type.HTTP, new InetSocketAddress(this.httpProxyHost, this.httpProxyPort));
    }

    private HttpURLConnection createConnection() {
        HttpURLConnection httpURLConnection;
        try {
            if (this.httpProxyHost != null) {
                httpURLConnection = CONNECTION_FACTORY.create(this.url, createProxy(), this.network);
            } else {
                httpURLConnection = CONNECTION_FACTORY.create(this.url, this.network);
            }
            httpURLConnection.setRequestMethod(this.requestMethod);
            if (httpURLConnection instanceof HttpsURLConnection) {
                ((HttpsURLConnection) httpURLConnection).setHostnameVerifier(getTrustedVerifier());
            }
            return httpURLConnection;
        } catch (IOException e) {
            throw new HttpRequestException(e);
        }
    }

    public String toString() {
        return method() + ' ' + url();
    }

    public HttpURLConnection getConnection() {
        if (this.connection == null) {
            this.connection = createConnection();
        }
        return this.connection;
    }

    public int code() throws HttpRequestException {
        try {
            closeOutput();
            return getConnection().getResponseCode();
        } catch (IOException e) {
            throw new HttpRequestException(e);
        }
    }

    public String getCipherSuite() {
        HttpURLConnection httpURLConnection = this.connection;
        if (httpURLConnection != null && (httpURLConnection instanceof HttpsURLConnection)) {
            try {
                return ((HttpsURLConnection) httpURLConnection).getCipherSuite();
            } catch (Exception unused) {
            }
        }
        return null;
    }

    public boolean ok() throws HttpRequestException {
        return 200 == code();
    }

    public String message() throws HttpRequestException {
        try {
            closeOutput();
            return getConnection().getResponseMessage();
        } catch (IOException e) {
            throw new HttpRequestException(e);
        }
    }

    public HttpRequest disconnect() {
        getConnection().disconnect();
        return this;
    }

    public HttpRequest chunk(int i) {
        getConnection().setChunkedStreamingMode(i);
        return this;
    }

    public HttpRequest bufferSize(int i) {
        if (i >= 1) {
            this.bufferSize = i;
            return this;
        }
        throw new IllegalArgumentException("Size must be greater than zero");
    }

    /* access modifiers changed from: protected */
    public ByteArrayOutputStream byteStream() {
        int contentLength = contentLength();
        if (contentLength > 0) {
            return new ByteArrayOutputStream(contentLength);
        }
        return new ByteArrayOutputStream();
    }

    public String body(String str) throws HttpRequestException {
        ByteArrayOutputStream byteStream = byteStream();
        try {
            copy(buffer(), byteStream);
            return byteStream.toString(getValidCharset(str));
        } catch (IOException e) {
            throw new HttpRequestException(e);
        }
    }

    public String body() throws HttpRequestException {
        return body(charset());
    }

    public BufferedInputStream buffer() throws HttpRequestException {
        return new BufferedInputStream(stream(), this.bufferSize);
    }

    public InputStream stream() throws HttpRequestException {
        InputStream inputStream;
        if (code() < 400) {
            try {
                inputStream = getConnection().getInputStream();
            } catch (IOException e) {
                throw new HttpRequestException(e);
            }
        } else {
            inputStream = getConnection().getErrorStream();
            if (inputStream == null) {
                try {
                    inputStream = getConnection().getInputStream();
                } catch (IOException e2) {
                    if (contentLength() <= 0) {
                        inputStream = new ByteArrayInputStream(new byte[0]);
                    } else {
                        throw new HttpRequestException(e2);
                    }
                }
            }
        }
        if (!this.uncompress || !"gzip".equals(contentEncoding())) {
            return inputStream;
        }
        try {
            return new GZIPInputStream(inputStream);
        } catch (IOException e3) {
            if (inputStream != null) {
                try {
                    inputStream.close();
                } catch (IOException e4) {
                    throw new HttpRequestException(e4);
                }
            }
            throw new HttpRequestException(e3);
        }
    }

    public HttpRequest receive(OutputStream outputStream) throws HttpRequestException {
        try {
            return copy(buffer(), outputStream);
        } catch (IOException e) {
            throw new HttpRequestException(e);
        }
    }

    public HttpRequest readTimeout(int i) {
        getConnection().setReadTimeout(i);
        return this;
    }

    public HttpRequest connectTimeout(int i) {
        getConnection().setConnectTimeout(i);
        return this;
    }

    public HttpRequest header(String str, String str2) {
        getConnection().setRequestProperty(str, str2);
        return this;
    }

    public String header(String str) throws HttpRequestException {
        closeOutputQuietly();
        return getConnection().getHeaderField(str);
    }

    public int intHeader(String str) throws HttpRequestException {
        return intHeader(str, -1);
    }

    public int intHeader(String str, int i) throws HttpRequestException {
        closeOutputQuietly();
        return getConnection().getHeaderFieldInt(str, i);
    }

    public String parameter(String str, String str2) {
        return getParam(header(str), str2);
    }

    /*  JADX ERROR: JadxOverflowException in pass: RegionMakerVisitor
        jadx.core.utils.exceptions.JadxOverflowException: Regions count limit reached
        	at jadx.core.utils.ErrorsCounter.addError(ErrorsCounter.java:47)
        	at jadx.core.utils.ErrorsCounter.methodError(ErrorsCounter.java:81)
        */
    /* JADX WARNING: Removed duplicated region for block: B:11:0x0025  */
    /* JADX WARNING: Removed duplicated region for block: B:30:0x006f A[EDGE_INSN: B:30:0x006f->B:29:0x006f ?: BREAK  , SYNTHETIC] */
    protected java.lang.String getParam(java.lang.String r8, java.lang.String r9) {
        /*
            r7 = this;
            r7 = 0
            if (r8 == 0) goto L_0x006f
            int r0 = r8.length()
            if (r0 != 0) goto L_0x000a
            goto L_0x006f
        L_0x000a:
            int r0 = r8.length()
            r1 = 59
            int r2 = r8.indexOf(r1)
            r3 = 1
            int r2 = r2 + r3
            if (r2 == 0) goto L_0x006f
            if (r2 != r0) goto L_0x001b
            goto L_0x006f
        L_0x001b:
            int r4 = r8.indexOf(r1, r2)
            r5 = -1
            if (r4 != r5) goto L_0x0023
        L_0x0022:
            r4 = r0
        L_0x0023:
            if (r2 >= r4) goto L_0x006f
            r6 = 61
            int r6 = r8.indexOf(r6, r2)
            if (r6 == r5) goto L_0x0066
            if (r6 >= r4) goto L_0x0066
            java.lang.String r2 = r8.substring(r2, r6)
            java.lang.String r2 = r2.trim()
            boolean r2 = r9.equals(r2)
            if (r2 == 0) goto L_0x0066
            int r6 = r6 + 1
            java.lang.String r2 = r8.substring(r6, r4)
            java.lang.String r2 = r2.trim()
            int r6 = r2.length()
            if (r6 == 0) goto L_0x0066
            r7 = 2
            if (r6 <= r7) goto L_0x0065
            r7 = 0
            char r7 = r2.charAt(r7)
            r8 = 34
            if (r8 != r7) goto L_0x0065
            int r6 = r6 - r3
            char r7 = r2.charAt(r6)
            if (r8 != r7) goto L_0x0065
            java.lang.String r7 = r2.substring(r3, r6)
            return r7
        L_0x0065:
            return r2
        L_0x0066:
            int r2 = r4 + 1
            int r4 = r8.indexOf(r1, r2)
            if (r4 != r5) goto L_0x0023
            goto L_0x0022
        L_0x006f:
            return r7
        */
        throw new UnsupportedOperationException("Method not decompiled: com.sec.internal.helper.HttpRequest.getParam(java.lang.String, java.lang.String):java.lang.String");
    }

    public String charset() {
        return parameter("Content-Type", "charset");
    }

    public HttpRequest userAgent(String str) {
        return header("User-Agent", str);
    }

    public HttpRequest useCaches(boolean z) {
        getConnection().setUseCaches(z);
        return this;
    }

    public HttpRequest acceptEncoding(String str) {
        return header("Accept-Encoding", str);
    }

    public String contentEncoding() {
        return header("Content-Encoding");
    }

    public HttpRequest authorization(String str) {
        return header("Authorization", str);
    }

    public HttpRequest contentType(String str) {
        return contentType(str, (String) null);
    }

    public HttpRequest contentType(String str, String str2) {
        if (str2 == null || str2.length() <= 0) {
            return header("Content-Type", str);
        }
        return header("Content-Type", str + "; charset=" + str2);
    }

    public int contentLength() {
        return intHeader("Content-Length");
    }

    public HttpRequest contentLength(String str) {
        return contentLength(Integer.parseInt(str));
    }

    public HttpRequest contentLength(int i) {
        getConnection().setFixedLengthStreamingMode(i);
        return this;
    }

    public String wwwAuthenticate() {
        return header("WWW-Authenticate");
    }

    public HttpRequest range(long j, long j2) {
        if (j < 0) {
            throw new IllegalArgumentException("Cannot have negative start: " + j);
        } else if (j2 < 0) {
            return header("Range", String.format("bytes=%s-", new Object[]{Long.valueOf(j)}));
        } else {
            return header("Range", String.format("bytes=%s-%s", new Object[]{Long.valueOf(j), Long.valueOf(j2)}));
        }
    }

    public HttpRequest contentRange(long j, long j2, long j3) {
        if (j >= 0 && j2 >= 0 && j <= j2) {
            return header("Content-Range", String.format("bytes %s-%s/%s", new Object[]{Long.valueOf(j), Long.valueOf(j2), Long.valueOf(j3)}));
        }
        throw new IllegalArgumentException("Invalid argument: " + j + "," + j2);
    }

    /* access modifiers changed from: protected */
    public HttpRequest copy(InputStream inputStream, OutputStream outputStream) throws IOException {
        final InputStream inputStream2 = inputStream;
        final OutputStream outputStream2 = outputStream;
        return (HttpRequest) new CloseOperation<HttpRequest>(inputStream, this.ignoreCloseExceptions, (FileOutputStream) null) {
            public HttpRequest run() throws IOException {
                byte[] bArr = new byte[HttpRequest.this.bufferSize];
                do {
                    int read = inputStream2.read(bArr);
                    if (read == -1) {
                        break;
                    }
                    outputStream2.write(bArr, 0, read);
                    outputStream2.flush();
                    HttpRequest httpRequest = HttpRequest.this;
                    httpRequest.totalWritten = httpRequest.totalWritten + ((long) read);
                    HttpRequest.this.progress.onUpload(HttpRequest.this.totalWritten, HttpRequest.this.totalSize);
                } while (!HttpRequest.this.progress.isCancelled());
                return HttpRequest.this;
            }
        }.call();
    }

    public HttpRequest progress(UploadProgress uploadProgress) {
        if (uploadProgress == null) {
            this.progress = UploadProgress.DEFAULT;
        } else {
            this.progress = uploadProgress;
        }
        this.totalWritten = 0;
        return this;
    }

    private HttpRequest incrementTotalSize(long j) {
        if (this.totalSize == -1) {
            this.totalSize = 0;
        }
        this.totalSize += j;
        return this;
    }

    /* access modifiers changed from: protected */
    public HttpRequest closeOutput() throws IOException {
        RequestOutputStream requestOutputStream = this.output;
        if (requestOutputStream == null) {
            return this;
        }
        if (this.multipart) {
            requestOutputStream.write("\r\n--00content0boundary00--\r\n");
        }
        if (this.ignoreCloseExceptions) {
            try {
                this.output.close();
            } catch (IOException unused) {
            }
        } else {
            this.output.close();
        }
        this.output = null;
        return this;
    }

    /* access modifiers changed from: protected */
    public HttpRequest closeOutputQuietly() throws HttpRequestException {
        try {
            return closeOutput();
        } catch (IOException e) {
            throw new HttpRequestException(e);
        }
    }

    /* access modifiers changed from: protected */
    public HttpRequest openOutput() throws IOException {
        if (this.output != null) {
            return this;
        }
        getConnection().setDoOutput(true);
        this.output = new RequestOutputStream(getConnection().getOutputStream(), getParam(getConnection().getRequestProperty("Content-Type"), "charset"), this.bufferSize);
        return this;
    }

    /* access modifiers changed from: protected */
    public HttpRequest startPart() throws IOException {
        if (!this.multipart) {
            this.multipart = true;
            contentType(CONTENT_TYPE_MULTIPART).openOutput();
            this.output.write("--00content0boundary00\r\n");
        } else {
            this.output.write("\r\n--00content0boundary00\r\n");
        }
        return this;
    }

    /* access modifiers changed from: protected */
    public HttpRequest writePartHeader(String str, String str2, String str3) throws IOException {
        StringBuilder sb = new StringBuilder();
        sb.append("form-data; name=\"");
        sb.append(str);
        if (str2 != null) {
            sb.append("\"; filename=\"");
            sb.append(str2);
        }
        sb.append('\"');
        partHeader(HttpController.HEADER_CONTENT_DISPOSITION, sb.toString());
        if (str3 != null) {
            partHeader("Content-Type", str3);
        }
        return send((CharSequence) CRLF);
    }

    public HttpRequest part(String str, String str2, String str3, String str4) throws HttpRequestException {
        try {
            startPart();
            writePartHeader(str, str2, str3);
            this.output.write(str4);
            return this;
        } catch (IOException e) {
            throw new HttpRequestException(e);
        }
    }

    public HttpRequest part(String str, String str2, String str3, File file) throws HttpRequestException {
        BufferedInputStream bufferedInputStream;
        try {
            FileInputStream fileInputStream = new FileInputStream(file);
            try {
                bufferedInputStream = new BufferedInputStream(fileInputStream);
                incrementTotalSize(file.length());
                part(str, str2, str3, (InputStream) bufferedInputStream);
                bufferedInputStream.close();
                fileInputStream.close();
                return this;
            } catch (Throwable th) {
                fileInputStream.close();
                throw th;
            }
            throw th;
        } catch (IOException e) {
            throw new HttpRequestException(e);
        } catch (Throwable th2) {
            th.addSuppressed(th2);
        }
    }

    public HttpRequest part(String str, String str2, String str3, InputStream inputStream) throws HttpRequestException {
        try {
            startPart();
            writePartHeader(str, str2, str3);
            copy(inputStream, this.output);
            return this;
        } catch (IOException e) {
            throw new HttpRequestException(e);
        }
    }

    public HttpRequest partHeader(String str, String str2) throws HttpRequestException {
        return send((CharSequence) str).send((CharSequence) ": ").send((CharSequence) str2).send((CharSequence) CRLF);
    }

    public HttpRequest send(InputStream inputStream) throws HttpRequestException {
        try {
            openOutput();
            copy(inputStream, this.output);
            return this;
        } catch (IOException e) {
            throw new HttpRequestException(e);
        }
    }

    public HttpRequest send(CharSequence charSequence) throws HttpRequestException {
        try {
            openOutput();
            this.output.write(charSequence.toString());
            return this;
        } catch (IOException e) {
            throw new HttpRequestException(e);
        }
    }

    public HttpRequest trustAllCerts() throws HttpRequestException {
        HttpURLConnection connection2 = getConnection();
        if (connection2 instanceof HttpsURLConnection) {
            ((HttpsURLConnection) connection2).setSSLSocketFactory(getTrustedFactory());
        }
        return this;
    }

    public HttpRequest trustAllHosts() {
        HttpURLConnection connection2 = getConnection();
        HttpRequest$$ExternalSyntheticLambda0 httpRequest$$ExternalSyntheticLambda0 = new HttpRequest$$ExternalSyntheticLambda0();
        if (connection2 instanceof HttpsURLConnection) {
            Log.d("HttpRequest", "trustAllHosts() - this connections is instance of HttpsURLConnection ");
            ((HttpsURLConnection) connection2).setHostnameVerifier(httpRequest$$ExternalSyntheticLambda0);
        }
        return this;
    }

    public URL url() {
        return getConnection().getURL();
    }

    public String method() {
        return getConnection().getRequestMethod();
    }

    public HttpRequest useNetwork(Network network2) {
        this.network = network2;
        return this;
    }

    public long getPartHeaderLength(String str, String str2, String str3, boolean z) {
        StringBuilder sb = new StringBuilder();
        if (z) {
            sb.append("--00content0boundary00\r\n");
        } else {
            sb.append("\r\n--00content0boundary00\r\n");
        }
        StringBuilder sb2 = new StringBuilder();
        sb2.append("form-data; name=\"");
        sb2.append(str);
        if (str2 != null) {
            sb2.append("\"; filename=\"");
            sb2.append(str2);
        }
        sb2.append('\"');
        sb.append("Content-Disposition: " + sb2.toString() + CRLF);
        if (str3 != null) {
            sb.append("Content-Type: " + str3 + CRLF);
        }
        sb.append(CRLF);
        Log.d("HttpRequest", "The length of header: " + sb.length());
        return (long) sb.length();
    }

    public HttpRequest setFollowRedirect(boolean z) {
        getConnection().setInstanceFollowRedirects(z);
        return this;
    }

    public HttpRequest setParams(Network network2, boolean z, int i, int i2, String str) {
        return useNetwork(network2).useCaches(z).connectTimeout(i).readTimeout(i2).userAgent(str).setFollowRedirect(false);
    }
}
