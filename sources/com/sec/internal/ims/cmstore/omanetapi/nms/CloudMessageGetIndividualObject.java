package com.sec.internal.ims.cmstore.omanetapi.nms;

import android.util.Log;
import com.sec.internal.helper.httpclient.HttpRequestParams;
import com.sec.internal.helper.httpclient.HttpResponseParams;
import com.sec.internal.ims.cmstore.MessageStoreClient;
import com.sec.internal.ims.cmstore.omanetapi.clouddatasynchandler.BaseDataChangeHandler;
import com.sec.internal.ims.cmstore.omanetapi.synchandler.BaseSyncHandler;
import com.sec.internal.ims.cmstore.params.BufferDBChangeParam;
import com.sec.internal.ims.cmstore.params.ParamOMAresponseforBufDB;
import com.sec.internal.interfaces.ims.cmstore.IAPICallFlowListener;
import com.sec.internal.omanetapi.common.data.OMAApiResponseParam;
import com.sec.internal.omanetapi.nms.IndividualObject;
import com.sec.internal.omanetapi.nms.data.Object;
import java.io.IOException;

public class CloudMessageGetIndividualObject extends IndividualObject {
    private static final long serialVersionUID = 8158555957984259234L;
    public String TAG = CloudMessageGetIndividualObject.class.getSimpleName();

    public CloudMessageGetIndividualObject(final IAPICallFlowListener iAPICallFlowListener, String str, final BufferDBChangeParam bufferDBChangeParam, MessageStoreClient messageStoreClient) {
        super(messageStoreClient.getCloudMessageStrategyManager().getStrategy().getNmsHost(), messageStoreClient.getCloudMessageStrategyManager().getStrategy().getOMAApiVersion(), messageStoreClient.getCloudMessageStrategyManager().getStrategy().getStoreName(), bufferDBChangeParam.mLine, str, messageStoreClient);
        this.TAG += "[" + messageStoreClient.getClientID() + "]";
        String validTokenByLine = this.mStoreClient.getCloudMessageStrategyManager().getStrategy().getValidTokenByLine(bufferDBChangeParam.mLine);
        if (this.isCmsMcsEnabled) {
            initMcsCommonRequestHeaders(this.mStoreClient.getCloudMessageStrategyManager().getStrategy().getContentType(), this.mStoreClient.getCloudMessageStrategyManager().getStrategy().getAuthorizationBearer());
        } else {
            initCommonRequestHeaders(this.mStoreClient.getCloudMessageStrategyManager().getStrategy().getContentType(), validTokenByLine);
        }
        initGetRequest();
        setCallback(new HttpRequestParams.HttpRequestCallback() {
            public void onComplete(HttpResponseParams httpResponseParams) {
                ParamOMAresponseforBufDB paramOMAresponseforBufDB;
                if (httpResponseParams.getStatusCode() != 401 || !CloudMessageGetIndividualObject.this.handleUnAuthorized(httpResponseParams)) {
                    if (httpResponseParams.getStatusCode() == 206) {
                        httpResponseParams.setStatusCode(200);
                    }
                    if (httpResponseParams.getStatusCode() == 404) {
                        paramOMAresponseforBufDB = new ParamOMAresponseforBufDB.Builder().setActionType(ParamOMAresponseforBufDB.ActionType.OBJECT_NOT_FOUND).setBufferDBChangeParam(bufferDBChangeParam).build();
                    } else if (httpResponseParams.getStatusCode() == 200) {
                        OMAApiResponseParam response = CloudMessageGetIndividualObject.this.getResponse(httpResponseParams);
                        if (response == null) {
                            iAPICallFlowListener.onFailedCall(this, bufferDBChangeParam);
                            return;
                        }
                        Object object = response.object;
                        if (object == null) {
                            iAPICallFlowListener.onFailedCall(this, bufferDBChangeParam);
                            return;
                        }
                        ParamOMAresponseforBufDB.Builder object2 = new ParamOMAresponseforBufDB.Builder().setObject(object);
                        IAPICallFlowListener iAPICallFlowListener = iAPICallFlowListener;
                        if (iAPICallFlowListener instanceof BaseSyncHandler) {
                            object2.setActionType(ParamOMAresponseforBufDB.ActionType.ONE_MESSAGE_DOWNLOAD);
                        } else if (iAPICallFlowListener instanceof BaseDataChangeHandler) {
                            object2.setActionType(ParamOMAresponseforBufDB.ActionType.NOTIFICATION_OBJECT_DOWNLOADED);
                        }
                        object2.setBufferDBChangeParam(bufferDBChangeParam);
                        paramOMAresponseforBufDB = object2.build();
                    } else {
                        paramOMAresponseforBufDB = null;
                    }
                    if (CloudMessageGetIndividualObject.this.shouldCareAfterResponsePreProcess(iAPICallFlowListener, httpResponseParams, paramOMAresponseforBufDB, bufferDBChangeParam, Integer.MIN_VALUE)) {
                        iAPICallFlowListener.onMoveOnToNext(CloudMessageGetIndividualObject.this, paramOMAresponseforBufDB);
                    }
                }
            }

            public void onFail(IOException iOException) {
                String str = CloudMessageGetIndividualObject.this.TAG;
                Log.e(str, "Http request onFail: " + iOException.getMessage());
                iAPICallFlowListener.onFailedCall(this);
            }
        });
    }
}
