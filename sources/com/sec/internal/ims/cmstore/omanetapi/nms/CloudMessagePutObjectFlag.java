package com.sec.internal.ims.cmstore.omanetapi.nms;

import android.util.Log;
import com.sec.internal.constants.ims.cmstore.omanetapi.SyncMsgType;
import com.sec.internal.helper.httpclient.HttpRequestParams;
import com.sec.internal.helper.httpclient.HttpResponseParams;
import com.sec.internal.ims.cmstore.MessageStoreClient;
import com.sec.internal.ims.cmstore.params.BufferDBChangeParam;
import com.sec.internal.ims.cmstore.params.ParamOMAresponseforBufDB;
import com.sec.internal.interfaces.ims.cmstore.IAPICallFlowListener;
import com.sec.internal.omanetapi.nms.IndividualFlag;
import java.io.IOException;

public class CloudMessagePutObjectFlag extends IndividualFlag {
    private static final long serialVersionUID = -8234485964056243622L;
    /* access modifiers changed from: private */
    public String TAG = CloudMessagePutObjectFlag.class.getSimpleName();

    public CloudMessagePutObjectFlag(final IAPICallFlowListener iAPICallFlowListener, String str, String str2, final BufferDBChangeParam bufferDBChangeParam, MessageStoreClient messageStoreClient) {
        super(messageStoreClient.getCloudMessageStrategyManager().getStrategy().getNmsHost(), messageStoreClient.getCloudMessageStrategyManager().getStrategy().getOMAApiVersion(), messageStoreClient.getCloudMessageStrategyManager().getStrategy().getStoreName(), bufferDBChangeParam.mLine, str, str2, messageStoreClient);
        this.TAG += "[" + messageStoreClient.getClientID() + "]";
        initCommonRequestHeaders(this.mStoreClient.getCloudMessageStrategyManager().getStrategy().getContentType(), this.mStoreClient.getCloudMessageStrategyManager().getStrategy().getValidTokenByLine(bufferDBChangeParam.mLine));
        initPutRequest(true);
        setCallback(new HttpRequestParams.HttpRequestCallback() {
            public void onComplete(HttpResponseParams httpResponseParams) {
                ParamOMAresponseforBufDB paramOMAresponseforBufDB;
                if (httpResponseParams.getStatusCode() != 401 || !CloudMessagePutObjectFlag.this.handleUnAuthorized(httpResponseParams)) {
                    if (httpResponseParams.getStatusCode() == 204) {
                        httpResponseParams.setStatusCode(404);
                    }
                    if (httpResponseParams.getStatusCode() == 404 || httpResponseParams.getStatusCode() == 201) {
                        paramOMAresponseforBufDB = new ParamOMAresponseforBufDB.Builder().setActionType(ParamOMAresponseforBufDB.ActionType.OBJECT_FLAG_UPDATED).setBufferDBChangeParam(bufferDBChangeParam).build();
                    } else {
                        paramOMAresponseforBufDB = null;
                    }
                    if (CloudMessagePutObjectFlag.this.mStoreClient.getCloudMessageStrategyManager().getStrategy().isEnableATTHeader() || !CloudMessagePutObjectFlag.this.mStoreClient.getCloudMessageStrategyManager().getStrategy().isRetryRequired(httpResponseParams.getStatusCode())) {
                        if (CloudMessagePutObjectFlag.this.shouldCareAfterResponsePreProcess(iAPICallFlowListener, httpResponseParams, paramOMAresponseforBufDB, bufferDBChangeParam, Integer.MIN_VALUE)) {
                            iAPICallFlowListener.onMoveOnToNext(CloudMessagePutObjectFlag.this, paramOMAresponseforBufDB);
                            return;
                        }
                        return;
                    }
                    iAPICallFlowListener.onFailedCall(this, bufferDBChangeParam, SyncMsgType.VM, httpResponseParams.getStatusCode());
                }
            }

            public void onFail(IOException iOException) {
                String r0 = CloudMessagePutObjectFlag.this.TAG;
                Log.e(r0, "Http request onFail: " + iOException.getMessage());
                iAPICallFlowListener.onFailedCall(this, bufferDBChangeParam);
            }
        });
    }
}
