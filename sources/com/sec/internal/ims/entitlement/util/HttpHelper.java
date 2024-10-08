package com.sec.internal.ims.entitlement.util;

import android.os.Message;
import android.util.Log;
import com.sec.internal.helper.httpclient.HttpController;
import com.sec.internal.helper.httpclient.HttpRequestParams;
import com.sec.internal.helper.httpclient.HttpResponseParams;
import com.sec.internal.imscr.LogClass;
import com.sec.internal.log.IMSLog;
import java.io.IOException;
import java.util.Map;
import javax.net.SocketFactory;
import okhttp3.Dns;
import org.json.JSONArray;

public class HttpHelper {
    /* access modifiers changed from: private */
    public static final String LOG_TAG = "HttpHelper";
    protected HttpController mHttpController = HttpController.getInstance();

    public void executeNSDSRequest(String str, Map<String, String> map, JSONArray jSONArray, Message message, SocketFactory socketFactory, Dns dns) {
        HttpRequestParams createHttpRequestParams = createHttpRequestParams(HttpRequestParams.Method.POST, str, map, message);
        if (socketFactory != null) {
            createHttpRequestParams.setSocketFactory(socketFactory);
        }
        if (dns != null) {
            createHttpRequestParams.setDns(dns);
        }
        createHttpRequestParams.setPostBody(jSONArray);
        this.mHttpController.execute(createHttpRequestParams);
    }

    private HttpRequestParams createHttpRequestParams(HttpRequestParams.Method method, String str, Map<String, String> map, Message message) {
        return new HttpRequestParams(method, str, map, buildHttpRequestCallback(message));
    }

    private HttpRequestParams.HttpRequestCallback buildHttpRequestCallback(final Message message) {
        return new HttpRequestParams.HttpRequestCallback() {
            public void onComplete(HttpResponseParams httpResponseParams) {
                Log.i(HttpHelper.LOG_TAG, "Http request 200 ok");
                Message message = message;
                message.obj = httpResponseParams;
                message.sendToTarget();
            }

            public void onFail(IOException iOException) {
                IMSLog.c(LogClass.ES_HTTP_FAIL, "HTP FAIL:" + iOException.getMessage());
                HttpResponseParams httpResponseParams = new HttpResponseParams();
                httpResponseParams.setStatusReason(iOException.getMessage());
                Message message = message;
                message.obj = httpResponseParams;
                message.sendToTarget();
            }
        };
    }
}
