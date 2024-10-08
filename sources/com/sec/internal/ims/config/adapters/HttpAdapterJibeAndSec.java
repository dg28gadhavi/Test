package com.sec.internal.ims.config.adapters;

import com.sec.internal.ims.config.adapters.HttpAdapter;
import com.sec.internal.interfaces.ims.config.IHttpAdapter;
import java.util.List;
import java.util.Map;

public class HttpAdapterJibeAndSec extends HttpAdapter {
    protected static final String LOG_TAG = "HttpAdapterJibeAndSec";

    public HttpAdapterJibeAndSec(int i) {
        super(i);
        this.mState = new IdleState();
    }

    protected class IdleState extends HttpAdapter.IdleState {
        protected IdleState() {
            super();
        }

        public boolean open(String str) {
            if (!HttpAdapterJibeAndSec.this.configureUrlConnection(str)) {
                return false;
            }
            HttpAdapterJibeAndSec httpAdapterJibeAndSec = HttpAdapterJibeAndSec.this;
            httpAdapterJibeAndSec.mState = new ReadyState();
            return true;
        }
    }

    protected class ReadyState extends HttpAdapter.ReadyState {
        protected ReadyState() {
            super();
        }

        public IHttpAdapter.Response request() {
            HttpAdapterJibeAndSec.this.tryToConnectHttpUrlConnection();
            String stringBuffer = HttpAdapterJibeAndSec.this.mUrl.toString();
            HttpAdapterJibeAndSec httpAdapterJibeAndSec = HttpAdapterJibeAndSec.this;
            int resStatusCode = httpAdapterJibeAndSec.getResStatusCode(httpAdapterJibeAndSec.mHttpURLConn);
            HttpAdapterJibeAndSec httpAdapterJibeAndSec2 = HttpAdapterJibeAndSec.this;
            String resStatusMessage = httpAdapterJibeAndSec2.getResStatusMessage(httpAdapterJibeAndSec2.mHttpURLConn);
            HttpAdapterJibeAndSec httpAdapterJibeAndSec3 = HttpAdapterJibeAndSec.this;
            Map<String, List<String>> resHeader = httpAdapterJibeAndSec3.getResHeader(httpAdapterJibeAndSec3.mHttpURLConn);
            HttpAdapterJibeAndSec httpAdapterJibeAndSec4 = HttpAdapterJibeAndSec.this;
            return new IHttpAdapter.Response(stringBuffer, resStatusCode, resStatusMessage, resHeader, httpAdapterJibeAndSec4.getResBody(httpAdapterJibeAndSec4.mHttpURLConn));
        }

        public boolean close() {
            HttpAdapterJibeAndSec.this.mHttpURLConn.disconnect();
            HttpAdapterJibeAndSec httpAdapterJibeAndSec = HttpAdapterJibeAndSec.this;
            httpAdapterJibeAndSec.mState = new IdleState();
            return true;
        }
    }
}
