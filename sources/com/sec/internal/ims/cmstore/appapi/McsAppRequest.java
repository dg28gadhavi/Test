package com.sec.internal.ims.cmstore.appapi;

import android.os.Message;
import android.text.TextUtils;
import com.sec.internal.constants.ims.cmstore.omanetapi.OMASyncEventType;
import com.sec.internal.helper.httpclient.HttpRequestParams;
import com.sec.internal.helper.httpclient.HttpResponseParams;
import com.sec.internal.ims.cmstore.MessageStoreClient;
import com.sec.internal.ims.cmstore.params.ParamAppResponseObject;
import com.sec.internal.interfaces.ims.cmstore.IAPICallFlowListener;
import com.sec.internal.log.IMSLog;
import java.io.IOException;

public class McsAppRequest extends BaseAppAPIRequest {
    /* access modifiers changed from: private */
    public String TAG = McsAppRequest.class.getSimpleName();
    /* access modifiers changed from: private */
    public IAPICallFlowListener callBackListener;

    public McsAppRequest(MessageStoreClient messageStoreClient, IAPICallFlowListener iAPICallFlowListener, String str, String str2, int i) {
        super(messageStoreClient, str2);
        this.TAG += "[" + messageStoreClient.getClientID() + "]";
        this.callBackListener = iAPICallFlowListener;
        String authorizationBearer = messageStoreClient.getCloudMessageStrategyManager().getStrategy().getAuthorizationBearer();
        IMSLog.i(this.TAG, "McsPostAppRequest auth: " + authorizationBearer);
        if (TextUtils.isEmpty(authorizationBearer)) {
            IMSLog.e(this.TAG, "auth is empty, do not process");
        }
        initCommonRequestHeaders(authorizationBearer);
        initMethodAndBody(str, i);
        setCallback(new HttpRequestParams.HttpRequestCallback() {
            public void onComplete(HttpResponseParams httpResponseParams) {
                int statusCode = httpResponseParams.getStatusCode();
                String decryptedString = McsAppRequest.this.getDecryptedString(httpResponseParams.getDataString());
                Message message = new Message();
                message.obj = new ParamAppResponseObject(statusCode, decryptedString);
                String r2 = McsAppRequest.this.TAG;
                IMSLog.i(r2, "onComplete: " + statusCode + " strBody:" + decryptedString);
                if (statusCode == 200 || statusCode == 201) {
                    message.what = OMASyncEventType.API_SUCCEED.getId();
                } else {
                    message.what = OMASyncEventType.UPDATE_FAILED.getId();
                }
                McsAppRequest.this.callBackListener.onFixedFlowWithMessage(message);
            }

            public void onFail(IOException iOException) {
                IMSLog.i(McsAppRequest.this.TAG, "onFail");
                Message message = new Message();
                ParamAppResponseObject paramAppResponseObject = new ParamAppResponseObject(-1, "requestFailed");
                message.what = OMASyncEventType.UPDATE_FAILED.getId();
                message.obj = paramAppResponseObject;
                McsAppRequest.this.callBackListener.onFixedFlowWithMessage(message);
            }
        });
    }
}
