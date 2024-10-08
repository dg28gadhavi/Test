package com.sec.internal.ims.cmstore.ambs.cloudrequest;

import android.util.Log;
import com.sec.internal.helper.httpclient.HttpRequestParams;
import com.sec.internal.helper.httpclient.HttpResponseParams;
import com.sec.internal.ims.cmstore.MessageStoreClient;
import com.sec.internal.ims.cmstore.ambs.globalsetting.BaseProvisionAPIRequest;
import com.sec.internal.interfaces.ims.cmstore.IAPICallFlowListener;
import com.sec.internal.interfaces.ims.cmstore.ICloudMessageManagerHelper;
import com.sec.internal.interfaces.ims.cmstore.IRetryStackAdapterHelper;
import java.io.IOException;

public class ReqRetireSession extends BaseProvisionAPIRequest {
    private static final long serialVersionUID = 2492635640514901L;
    /* access modifiers changed from: private */
    public String TAG = ReqRetireSession.class.getSimpleName();

    public ReqRetireSession(IAPICallFlowListener iAPICallFlowListener, MessageStoreClient messageStoreClient, ICloudMessageManagerHelper iCloudMessageManagerHelper) {
        super(iAPICallFlowListener, messageStoreClient);
        this.TAG += "[" + messageStoreClient.getClientID() + "]";
        setMethod(HttpRequestParams.Method.GET);
        updateUrl();
        setCallback(new HttpRequestParams.HttpRequestCallback() {
            public void onComplete(HttpResponseParams httpResponseParams) {
                String r2 = ReqRetireSession.this.TAG;
                Log.d(r2, "StatusCode: " + httpResponseParams.getStatusCode());
                httpResponseParams.getStatusCode();
            }

            public void onFail(IOException iOException) {
                Log.d(ReqRetireSession.this.TAG, "call was failed");
                ReqRetireSession.this.goFailedCall();
            }
        });
    }

    public void updateUrl() {
        setUrl("https://" + getMsDomainAndSessionHelper() + "/logout");
    }

    private String getMsDomainAndSessionHelper() {
        return this.mStoreClient.getPrerenceManager().getRedirectDomain() + "/handset/session" + this.mStoreClient.getPrerenceManager().getMsgStoreSessionId();
    }

    public HttpRequestParams getRetryInstance(IAPICallFlowListener iAPICallFlowListener, MessageStoreClient messageStoreClient, ICloudMessageManagerHelper iCloudMessageManagerHelper, IRetryStackAdapterHelper iRetryStackAdapterHelper) {
        return new ReqRetireSession(iAPICallFlowListener, messageStoreClient, iCloudMessageManagerHelper);
    }
}
