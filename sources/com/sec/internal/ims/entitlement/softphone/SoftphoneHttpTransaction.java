package com.sec.internal.ims.entitlement.softphone;

import android.os.Message;
import android.util.Log;
import com.sec.internal.helper.httpclient.HttpController;
import com.sec.internal.helper.httpclient.HttpQueryParams;
import com.sec.internal.helper.httpclient.HttpRequestParams;
import com.sec.internal.helper.httpclient.HttpResponseParams;
import java.io.IOException;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.Map;
import org.json.JSONArray;
import org.json.JSONObject;

public class SoftphoneHttpTransaction {
    /* access modifiers changed from: private */
    public static final String LOG_TAG = "SoftphoneHttpTransaction";
    final SoftphoneClient mClient;
    private long mConnectionTimeout = -1;
    JSONObject mContent = null;
    private JSONArray mContents = new JSONArray();
    byte[] mData = null;
    Map<String, String> mHeader = new HashMap();
    HttpRequestParams.Method mMethod = null;
    LinkedHashMap<String, String> mQueryParams = new LinkedHashMap<>();
    boolean mQueryParamsEncoded = false;
    private long mReadTimeout = -1;
    String mStringBody = null;
    String mURL = null;
    private long mWriteTimeout = -1;

    public SoftphoneHttpTransaction(SoftphoneClient softphoneClient) {
        this.mClient = softphoneClient;
    }

    public void setRequestURL(String str) {
        this.mURL = str;
    }

    public void setRequestMethod(HttpRequestParams.Method method) {
        this.mMethod = method;
    }

    public void setQueryParameters(LinkedHashMap<String, String> linkedHashMap, boolean z) {
        this.mQueryParams = linkedHashMap;
        this.mQueryParamsEncoded = z;
    }

    public void addRequestHeader(String str, String str2) {
        this.mHeader.put(str, str2);
    }

    public void setStringBody(String str) {
        this.mStringBody = str;
    }

    public void setJsonBody(JSONObject jSONObject) {
        this.mContent = jSONObject;
    }

    public void setByteData(byte[] bArr) {
        this.mData = bArr;
    }

    public void commit(Message message) {
        executeRequest(message);
    }

    public void setTimeout(long j) {
        setConnectionTimeout(j);
        setReadTimeout(j);
        setWriteTimeout(j);
    }

    public void initHttpRequest(String str) {
        this.mURL = "https://" + this.mClient.mHost + str;
        this.mHeader.clear();
        this.mHeader.put(HttpController.HEADER_HOST, this.mClient.mHost);
        this.mHeader.put("Accept", "application/json");
        if (this.mClient.getAccessToken() != null) {
            Map<String, String> map = this.mHeader;
            map.put("Authorization", this.mClient.getAccessTokenType() + " " + this.mClient.getAccessToken());
        }
    }

    /* access modifiers changed from: package-private */
    public HttpRequestParams buildRequestParams(HttpRequestParams.HttpRequestCallback httpRequestCallback) {
        HttpRequestParams httpRequestParams = new HttpRequestParams(this.mMethod, this.mURL, this.mHeader, httpRequestCallback);
        String str = this.mStringBody;
        if (str != null) {
            httpRequestParams.setPostBody(str);
        } else {
            JSONObject jSONObject = this.mContent;
            if (jSONObject != null) {
                httpRequestParams.setPostBody(jSONObject);
            } else if (this.mContents.length() > 0) {
                httpRequestParams.setPostBody(this.mContents);
            } else {
                byte[] bArr = this.mData;
                if (bArr != null) {
                    httpRequestParams.setPostBody(bArr);
                }
            }
        }
        if (!this.mQueryParams.isEmpty()) {
            httpRequestParams.setQueryParams(new HttpQueryParams(this.mQueryParams, this.mQueryParamsEncoded));
        }
        long j = this.mConnectionTimeout;
        if (j != -1) {
            httpRequestParams.setConnectionTimeout(j);
        }
        long j2 = this.mReadTimeout;
        if (j2 != -1) {
            httpRequestParams.setReadTimeout(j2);
        }
        long j3 = this.mWriteTimeout;
        if (j3 != -1) {
            httpRequestParams.setWriteTimeout(j3);
        }
        httpRequestParams.setFollowRedirects(false);
        return httpRequestParams;
    }

    /* access modifiers changed from: package-private */
    public void executeRequest(final Message message) {
        HttpController.getInstance().execute(buildRequestParams(new HttpRequestParams.HttpRequestCallback() {
            public void onComplete(HttpResponseParams httpResponseParams) {
                Message message = message;
                message.obj = httpResponseParams;
                message.sendToTarget();
            }

            public void onFail(IOException iOException) {
                Log.e(SoftphoneHttpTransaction.LOG_TAG, "Http request failed");
                message.obj = new HttpResponseParams();
                message.sendToTarget();
            }
        }));
    }

    /* access modifiers changed from: package-private */
    public void setConnectionTimeout(long j) {
        this.mConnectionTimeout = j;
    }

    /* access modifiers changed from: package-private */
    public void setReadTimeout(long j) {
        this.mReadTimeout = j;
    }

    /* access modifiers changed from: package-private */
    public void setWriteTimeout(long j) {
        this.mWriteTimeout = j;
    }
}
