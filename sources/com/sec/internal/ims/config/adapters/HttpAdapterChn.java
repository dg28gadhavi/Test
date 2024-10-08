package com.sec.internal.ims.config.adapters;

import android.net.Network;
import com.sec.internal.ims.config.adapters.HttpAdapter;
import com.sec.internal.ims.util.ImsUtil;
import com.sec.internal.interfaces.ims.config.IHttpAdapter;
import com.sec.internal.log.IMSLog;
import java.io.IOException;
import java.net.HttpURLConnection;
import java.security.KeyManagementException;
import java.security.NoSuchAlgorithmException;
import java.security.cert.CertificateException;
import java.security.cert.X509Certificate;
import java.util.List;
import java.util.Map;
import javax.net.ssl.HostnameVerifier;
import javax.net.ssl.HttpsURLConnection;
import javax.net.ssl.KeyManager;
import javax.net.ssl.SSLContext;
import javax.net.ssl.SSLSession;
import javax.net.ssl.SSLSocketFactory;
import javax.net.ssl.TrustManager;
import javax.net.ssl.X509TrustManager;

public class HttpAdapterChn extends HttpAdapter {
    private static final HostnameVerifier DO_NOT_VERIFY = new HostnameVerifier() {
        public boolean verify(String str, SSLSession sSLSession) {
            return true;
        }
    };
    protected static final String LOG_TAG = "HttpAdapterChn";

    public HttpAdapterChn(int i) {
        super(i);
        this.mState = new IdleState();
    }

    protected class IdleState extends HttpAdapter.IdleState {
        protected IdleState() {
            super();
        }

        public boolean open(String str) {
            setNetwork((Network) null);
            if (!HttpAdapterChn.this.configureUrlConnection(str)) {
                return false;
            }
            HttpAdapterChn httpAdapterChn = HttpAdapterChn.this;
            httpAdapterChn.mState = new ReadyState();
            return true;
        }
    }

    protected class ReadyState extends HttpAdapter.ReadyState {
        protected ReadyState() {
            super();
        }

        public IHttpAdapter.Response request() {
            HttpAdapterChn.this.tryToConnectHttpUrlConnectionWithinTimeOut();
            String stringBuffer = HttpAdapterChn.this.mUrl.toString();
            HttpAdapterChn httpAdapterChn = HttpAdapterChn.this;
            int resStatusCode = httpAdapterChn.getResStatusCode(httpAdapterChn.mHttpURLConn);
            HttpAdapterChn httpAdapterChn2 = HttpAdapterChn.this;
            String resStatusMessage = httpAdapterChn2.getResStatusMessage(httpAdapterChn2.mHttpURLConn);
            HttpAdapterChn httpAdapterChn3 = HttpAdapterChn.this;
            Map<String, List<String>> resHeader = httpAdapterChn3.getResHeader(httpAdapterChn3.mHttpURLConn);
            HttpAdapterChn httpAdapterChn4 = HttpAdapterChn.this;
            return new IHttpAdapter.Response(stringBuffer, resStatusCode, resStatusMessage, resHeader, httpAdapterChn4.getResBody(httpAdapterChn4.mHttpURLConn));
        }

        public boolean close() {
            HttpAdapterChn.this.mHttpURLConn.disconnect();
            HttpAdapterChn httpAdapterChn = HttpAdapterChn.this;
            httpAdapterChn.mState = new IdleState();
            return true;
        }
    }

    private static class miTM implements TrustManager, X509TrustManager {
        public void checkClientTrusted(X509Certificate[] x509CertificateArr, String str) throws CertificateException {
        }

        public void checkServerTrusted(X509Certificate[] x509CertificateArr, String str) throws CertificateException {
        }

        public X509Certificate[] getAcceptedIssuers() {
            return null;
        }

        private miTM() {
        }
    }

    /* access modifiers changed from: protected */
    public void setSocketFactory() {
        try {
            SSLContext instance = SSLContext.getInstance("TLS");
            instance.init((KeyManager[]) null, new TrustManager[]{new miTM()}, ImsUtil.getRandom());
            SSLSocketFactory socketFactory = instance.getSocketFactory();
            IMSLog.i(LOG_TAG, this.mPhoneId, "get socketFactory for HTTPS");
            setSSLSocketFactory(socketFactory);
        } catch (KeyManagementException | NoSuchAlgorithmException e) {
            throw new Error(e);
        }
    }

    /* access modifiers changed from: protected */
    public void setSSLSocketFactory(SSLSocketFactory sSLSocketFactory) {
        super.setSSLSocketFactory(sSLSocketFactory);
        ((HttpsURLConnection) this.mURLConn).setHostnameVerifier(DO_NOT_VERIFY);
    }

    /* access modifiers changed from: protected */
    public void setHttpUrlConnection() throws IOException {
        super.setHttpUrlConnection();
        this.mHttpURLConn.setInstanceFollowRedirects(false);
    }

    /* access modifiers changed from: protected */
    public int getResStatusCode(HttpURLConnection httpURLConnection) {
        try {
            return httpURLConnection.getResponseCode();
        } catch (IOException e) {
            IMSLog.i(LOG_TAG, this.mPhoneId, "fail to read status code");
            e.printStackTrace();
        } catch (Throwable unused) {
        }
        return 0;
    }
}
