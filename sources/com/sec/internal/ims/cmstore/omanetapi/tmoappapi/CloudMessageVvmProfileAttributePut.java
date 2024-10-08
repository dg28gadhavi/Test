package com.sec.internal.ims.cmstore.omanetapi.tmoappapi;

import android.os.Message;
import android.util.Log;
import com.sec.internal.constants.ims.cmstore.CommonErrorName;
import com.sec.internal.constants.ims.cmstore.omanetapi.OMASyncEventType;
import com.sec.internal.constants.ims.cmstore.omanetapi.SyncMsgType;
import com.sec.internal.helper.HttpRequest;
import com.sec.internal.helper.httpclient.HttpRequestParams;
import com.sec.internal.helper.httpclient.HttpResponseParams;
import com.sec.internal.ims.cmstore.MessageStoreClient;
import com.sec.internal.ims.cmstore.helper.EventLogHelper;
import com.sec.internal.ims.cmstore.omanetapi.tmoappapi.data.VvmServiceProfile;
import com.sec.internal.ims.cmstore.params.BufferDBChangeParam;
import com.sec.internal.ims.cmstore.params.ParamOMAresponseforBufDB;
import com.sec.internal.interfaces.ims.cmstore.IAPICallFlowListener;
import java.io.IOException;
import java.util.List;

public class CloudMessageVvmProfileAttributePut extends IndividualVvmProfile {
    private static final long serialVersionUID = -622276329732847902L;
    /* access modifiers changed from: private */
    public String LOG_TAG = CloudMessageVvmProfileAttributePut.class.getSimpleName();

    public CloudMessageVvmProfileAttributePut(IAPICallFlowListener iAPICallFlowListener, VvmServiceProfile vvmServiceProfile, BufferDBChangeParam bufferDBChangeParam, MessageStoreClient messageStoreClient) {
        super(messageStoreClient.getCloudMessageStrategyManager().getStrategy().getNmsHost(), messageStoreClient.getCloudMessageStrategyManager().getStrategy().getOMAApiVersion(), messageStoreClient.getCloudMessageStrategyManager().getStrategy().getStoreName(), bufferDBChangeParam.mLine, messageStoreClient);
        this.LOG_TAG += "[" + messageStoreClient.getClientID() + "]";
        initCommonRequestHeaders(this.mStoreClient.getCloudMessageStrategyManager().getStrategy().getContentType(), this.mStoreClient.getCloudMessageStrategyManager().getStrategy().getValidTokenByLine(bufferDBChangeParam.mLine));
        initPutRequest(vvmServiceProfile, true);
        final BufferDBChangeParam bufferDBChangeParam2 = bufferDBChangeParam;
        final IAPICallFlowListener iAPICallFlowListener2 = iAPICallFlowListener;
        final VvmServiceProfile vvmServiceProfile2 = vvmServiceProfile;
        setCallback(new HttpRequestParams.HttpRequestCallback() {
            public void onComplete(HttpResponseParams httpResponseParams) {
                List list;
                if (httpResponseParams.getStatusCode() == 401) {
                    if (!CloudMessageVvmProfileAttributePut.this.handleUnAuthorized(httpResponseParams)) {
                        ParamOMAresponseforBufDB.Builder line = new ParamOMAresponseforBufDB.Builder().setBufferDBChangeParam(bufferDBChangeParam2).setLine(CloudMessageVvmProfileAttributePut.this.getBoxId());
                        Message message = new Message();
                        message.obj = line.build();
                        message.what = OMASyncEventType.CREDENTIAL_EXPIRED.getId();
                        iAPICallFlowListener2.onFixedFlowWithMessage(message);
                        iAPICallFlowListener2.onFailedCall(this, bufferDBChangeParam2);
                    }
                } else if (CloudMessageVvmProfileAttributePut.this.mStoreClient.getCloudMessageStrategyManager().getStrategy().isRetryRequired(httpResponseParams.getStatusCode())) {
                    iAPICallFlowListener2.onFailedCall(this, bufferDBChangeParam2, SyncMsgType.VM, httpResponseParams.getStatusCode());
                } else if (httpResponseParams.getStatusCode() == 429 && (list = httpResponseParams.getHeaders().get(HttpRequest.HEADER_RETRY_AFTER)) != null && list.size() > 0) {
                    Log.i(CloudMessageVvmProfileAttributePut.this.LOG_TAG, list.toString());
                    String str = (String) list.get(0);
                    String r0 = CloudMessageVvmProfileAttributePut.this.LOG_TAG;
                    Log.d(r0, "retryAfter is " + str + "seconds");
                    try {
                        int parseInt = Integer.parseInt(str);
                        if (parseInt > 0) {
                            iAPICallFlowListener2.onOverRequest(this, CommonErrorName.RETRY_HEADER, parseInt);
                        } else {
                            iAPICallFlowListener2.onFailedCall(this, bufferDBChangeParam2);
                        }
                    } catch (NumberFormatException e) {
                        Log.e(CloudMessageVvmProfileAttributePut.this.LOG_TAG, e.getMessage());
                        iAPICallFlowListener2.onFailedCall(this, bufferDBChangeParam2);
                    }
                } else if (httpResponseParams.getStatusCode() == 200 || httpResponseParams.getStatusCode() == 201 || httpResponseParams.getStatusCode() == 202) {
                    ParamOMAresponseforBufDB.Builder bufferDBChangeParam = new ParamOMAresponseforBufDB.Builder().setActionType(ParamOMAresponseforBufDB.ActionType.ONE_MESSAGE_UPLOADED).setVvmServiceProfile(vvmServiceProfile2).setBufferDBChangeParam(bufferDBChangeParam2);
                    Message message2 = new Message();
                    message2.obj = bufferDBChangeParam.build();
                    message2.what = OMASyncEventType.VVM_CHANGE_SUCCEED.getId();
                    iAPICallFlowListener2.onFixedFlowWithMessage(message2);
                } else {
                    String r02 = CloudMessageVvmProfileAttributePut.this.LOG_TAG;
                    int clientID = CloudMessageVvmProfileAttributePut.this.mStoreClient.getClientID();
                    EventLogHelper.add(r02, clientID, " statusCode: " + httpResponseParams.getStatusCode() + " mDataString: " + httpResponseParams.getDataString());
                    iAPICallFlowListener2.onFailedCall(this, bufferDBChangeParam2);
                }
            }

            public void onFail(IOException iOException) {
                String r0 = CloudMessageVvmProfileAttributePut.this.LOG_TAG;
                Log.e(r0, "Http request onFail: " + iOException.getMessage());
                iAPICallFlowListener2.onFailedCall(this, bufferDBChangeParam2);
            }
        });
    }
}
