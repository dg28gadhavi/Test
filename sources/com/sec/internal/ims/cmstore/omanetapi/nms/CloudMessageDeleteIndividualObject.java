package com.sec.internal.ims.cmstore.omanetapi.nms;

import android.util.Log;
import com.sec.internal.constants.ims.cmstore.omanetapi.SyncMsgType;
import com.sec.internal.helper.httpclient.HttpRequestParams;
import com.sec.internal.helper.httpclient.HttpResponseParams;
import com.sec.internal.ims.cmstore.MessageStoreClient;
import com.sec.internal.ims.cmstore.params.BufferDBChangeParam;
import com.sec.internal.ims.cmstore.params.ParamOMAresponseforBufDB;
import com.sec.internal.interfaces.ims.cmstore.IAPICallFlowListener;
import com.sec.internal.omanetapi.nms.IndividualObject;
import java.io.IOException;

public class CloudMessageDeleteIndividualObject extends IndividualObject {
    private static final long serialVersionUID = 8158555957984259234L;
    /* access modifiers changed from: private */
    public String TAG = CloudMessageDeleteIndividualObject.class.getSimpleName();

    public CloudMessageDeleteIndividualObject(final IAPICallFlowListener iAPICallFlowListener, String str, final BufferDBChangeParam bufferDBChangeParam, MessageStoreClient messageStoreClient) {
        super(messageStoreClient.getCloudMessageStrategyManager().getStrategy().getNmsHost(), messageStoreClient.getCloudMessageStrategyManager().getStrategy().getOMAApiVersion(), messageStoreClient.getCloudMessageStrategyManager().getStrategy().getStoreName(), bufferDBChangeParam.mLine, str, messageStoreClient);
        this.TAG += "[" + messageStoreClient.getClientID() + "]";
        initCommonRequestHeaders(this.mStoreClient.getCloudMessageStrategyManager().getStrategy().getContentType(), this.mStoreClient.getCloudMessageStrategyManager().getStrategy().getValidTokenByLine(bufferDBChangeParam.mLine));
        initDeleteRequest();
        setCallback(new HttpRequestParams.HttpRequestCallback() {
            public void onComplete(HttpResponseParams httpResponseParams) {
                ParamOMAresponseforBufDB paramOMAresponseforBufDB;
                if (httpResponseParams.getStatusCode() != 401 || !CloudMessageDeleteIndividualObject.this.handleUnAuthorized(httpResponseParams)) {
                    if (httpResponseParams.getStatusCode() == 206) {
                        httpResponseParams.setStatusCode(200);
                    }
                    if (httpResponseParams.getStatusCode() == 204) {
                        httpResponseParams.setStatusCode(404);
                    }
                    if (httpResponseParams.getStatusCode() == 200 || httpResponseParams.getStatusCode() == 404) {
                        paramOMAresponseforBufDB = new ParamOMAresponseforBufDB.Builder().setActionType(ParamOMAresponseforBufDB.ActionType.OBJECT_FLAGS_UPDATE_COMPLETE).setBufferDBChangeParam(bufferDBChangeParam).build();
                    } else {
                        paramOMAresponseforBufDB = null;
                    }
                    if (CloudMessageDeleteIndividualObject.this.mStoreClient.getCloudMessageStrategyManager().getStrategy().isEnableATTHeader() || !CloudMessageDeleteIndividualObject.this.mStoreClient.getCloudMessageStrategyManager().getStrategy().isRetryRequired(httpResponseParams.getStatusCode())) {
                        if (CloudMessageDeleteIndividualObject.this.shouldCareAfterResponsePreProcess(iAPICallFlowListener, httpResponseParams, paramOMAresponseforBufDB, bufferDBChangeParam, Integer.MIN_VALUE)) {
                            iAPICallFlowListener.onMoveOnToNext(CloudMessageDeleteIndividualObject.this, paramOMAresponseforBufDB);
                            return;
                        }
                        return;
                    }
                    iAPICallFlowListener.onFailedCall(this, bufferDBChangeParam, SyncMsgType.VM, httpResponseParams.getStatusCode());
                }
            }

            public void onFail(IOException iOException) {
                String r0 = CloudMessageDeleteIndividualObject.this.TAG;
                Log.e(r0, "Http request onFail: " + iOException.getMessage());
                iAPICallFlowListener.onFailedCall(this);
            }
        });
    }
}
