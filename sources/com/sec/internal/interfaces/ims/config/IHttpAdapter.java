package com.sec.internal.interfaces.ims.config;

import android.content.Context;
import android.net.Network;
import android.util.Log;
import com.sec.internal.helper.os.Debug;
import com.sec.internal.ims.registry.ImsRegistry;
import com.sec.internal.imscr.LogClass;
import com.sec.internal.log.IMSLog;
import java.io.UnsupportedEncodingException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;
import java.util.Locale;
import java.util.Map;

public interface IHttpAdapter {
    boolean close();

    Network getNetwork();

    boolean open(String str);

    Response request();

    void setContext(Context context);

    void setHeaders(Map<String, List<String>> map);

    void setMethod(String str);

    void setNetwork(Network network);

    void setParams(Map<String, String> map);

    public static class Response {
        private static final String CHARSET = "utf-8";
        public static final int EXCEPTION_CONNECT = 802;
        public static final int EXCEPTION_SOCKET = 803;
        public static final int EXCEPTION_SOCKET_TIMEOUT = 804;
        public static final int EXCEPTION_SSL = 801;
        public static final int EXCEPTION_SSL_HANDSHAKE = 800;
        public static final int EXCEPTION_UNKNOWN_HOST = 805;
        private static final String LOG_TAG = IHttpAdapter.class.getSimpleName();
        private byte[] mBody;
        private Map<String, List<String>> mHeader;
        private int mStatusCode;
        private String mStatusMessage;

        public Response(int i, String str, Map<String, List<String>> map, byte[] bArr) {
            this.mStatusCode = i;
            this.mStatusMessage = str;
            this.mHeader = map;
            this.mBody = bArr;
            debugPrint();
        }

        public Response(String str, int i, Map<String, List<String>> map, byte[] bArr) {
            this(i, "", map, bArr);
        }

        public Response(String str, int i, String str2, Map<String, List<String>> map, byte[] bArr) {
            this(i, str2, map, bArr);
        }

        public int getStatusCode() {
            return this.mStatusCode;
        }

        public void setStatusCode(int i) {
            this.mStatusCode = i;
        }

        public String getStatusMessage() {
            return this.mStatusMessage;
        }

        public void setStatusMessage(String str) {
            this.mStatusMessage = str;
        }

        public Map<String, List<String>> getHeader() {
            return this.mHeader;
        }

        public byte[] getBody() {
            return this.mBody;
        }

        private void debugPrint() {
            String str = LOG_TAG;
            Log.d(str, "HTTP(S) response : status code:" + this.mStatusCode);
            IMSLog.c(LogClass.HTTP_RESPONSE, "HR:" + this.mStatusCode);
            StringBuilder sb = new StringBuilder();
            StringBuilder sb2 = new StringBuilder();
            if (this.mHeader != null) {
                Log.d(str, "+++ HTTP(S) response : header");
                if (this.mHeader.size() > 0) {
                    for (Map.Entry next : this.mHeader.entrySet()) {
                        sb.append((String) next.getKey());
                        sb.append(":");
                        for (String append : (List) next.getValue()) {
                            sb.append("[");
                            sb.append(append);
                            sb.append("]");
                        }
                        Log.d(LOG_TAG, sb.toString());
                        sb2.append(sb);
                        sb.setLength(0);
                    }
                }
                Log.d(LOG_TAG, "--- HTTP(S) response : header");
            }
            if (this.mBody != null) {
                String str2 = LOG_TAG;
                Log.d(str2, "+++ HTTP(S) response : body");
                sb2.append("\n");
                try {
                    String str3 = new String(this.mBody, CHARSET);
                    sb2.append(str3);
                    byte[] bArr = this.mBody;
                    if (bArr.length > 256) {
                        Log.d(str2, new String(bArr, 0, 128, CHARSET));
                        byte[] bArr2 = this.mBody;
                        Log.d(str2, new String(bArr2, bArr2.length - 128, 128, CHARSET));
                    } else {
                        Log.d(str2, str3);
                    }
                } catch (UnsupportedEncodingException e) {
                    String str4 = LOG_TAG;
                    Log.e(str4, "UnsupportedEncodingException: " + e.getMessage());
                }
                Log.d(LOG_TAG, "--- HTTP(S) response : body");
            }
            if (Debug.ALLOW_DIAGNOSTICS) {
                String format = new SimpleDateFormat("MM/dd/yyyy HH:mm:ss.SSS", Locale.US).format(new Date());
                if (sb2.length() > 0) {
                    ImsRegistry.getImsDiagMonitor().onIndication(1, sb2.toString(), 100, 1, format, "", "", "");
                }
            }
        }
    }
}
