package com.sec.internal.ims.cmstore.omanetapi.nms;

import android.util.Log;
import com.sec.internal.constants.ims.ImsConstants;
import com.sec.internal.helper.httpclient.HttpRequestParams;
import com.sec.internal.helper.httpclient.HttpResponseParams;
import com.sec.internal.ims.cmstore.MessageStoreClient;
import com.sec.internal.ims.cmstore.omanetapi.clouddatasynchandler.BaseDataChangeHandler;
import com.sec.internal.ims.cmstore.omanetapi.synchandler.BaseSyncHandler;
import com.sec.internal.ims.cmstore.params.BufferDBChangeParam;
import com.sec.internal.ims.cmstore.params.ParamOMAresponseforBufDB;
import com.sec.internal.ims.cmstore.utils.Util;
import com.sec.internal.interfaces.ims.cmstore.IAPICallFlowListener;
import com.sec.internal.log.IMSLog;
import com.sec.internal.omanetapi.nms.IndividualPayload;
import java.io.IOException;

public class CloudMessageGetIndividualPayLoad extends IndividualPayload {
    private static final long serialVersionUID = -5816641182872600506L;
    /* access modifiers changed from: private */
    public String TAG = CloudMessageGetIndividualPayLoad.class.getSimpleName();

    public static CloudMessageGetIndividualPayLoad buildFromPayloadUrl(IAPICallFlowListener iAPICallFlowListener, String str, BufferDBChangeParam bufferDBChangeParam, MessageStoreClient messageStoreClient) {
        return new CloudMessageGetIndividualPayLoad(iAPICallFlowListener, str, bufferDBChangeParam, messageStoreClient);
    }

    private CloudMessageGetIndividualPayLoad(IAPICallFlowListener iAPICallFlowListener, String str, BufferDBChangeParam bufferDBChangeParam, MessageStoreClient messageStoreClient) {
        super(str, messageStoreClient);
        this.TAG += "[" + messageStoreClient.getClientID() + "]";
        this.mBaseUrl = Util.replaceUrlPrefix(this.mBaseUrl, messageStoreClient);
        buildInternal(iAPICallFlowListener, bufferDBChangeParam);
    }

    private void buildInternal(final IAPICallFlowListener iAPICallFlowListener, final BufferDBChangeParam bufferDBChangeParam) {
        String validTokenByLine = this.mStoreClient.getCloudMessageStrategyManager().getStrategy().getValidTokenByLine(bufferDBChangeParam.mLine);
        if (this.isCmsMcsEnabled) {
            initMcsCommonRequestHeaders(this.mStoreClient.getCloudMessageStrategyManager().getStrategy().getContentType(), this.mStoreClient.getCloudMessageStrategyManager().getStrategy().getAuthorizationBearer());
            setProtocol(true);
        } else {
            initCommonRequestHeaders(this.mStoreClient.getCloudMessageStrategyManager().getStrategy().getContentType(), validTokenByLine);
        }
        initGetRequest();
        String str = this.TAG;
        Log.i(str, ImsConstants.FtDlParams.FT_DL_URL + IMSLog.checker(this.mBaseUrl));
        setCallback(new HttpRequestParams.HttpRequestCallback() {
            public void onComplete(HttpResponseParams httpResponseParams) {
                ParamOMAresponseforBufDB paramOMAresponseforBufDB;
                if (httpResponseParams.getStatusCode() != 401 || !CloudMessageGetIndividualPayLoad.this.handleUnAuthorized(httpResponseParams)) {
                    byte[] dataBinary = httpResponseParams.getDataBinary();
                    if (httpResponseParams.getStatusCode() == 206) {
                        httpResponseParams.setStatusCode(200);
                    }
                    if (httpResponseParams.getStatusCode() == 404 || httpResponseParams.getStatusCode() == 403) {
                        paramOMAresponseforBufDB = new ParamOMAresponseforBufDB.Builder().setActionType(ParamOMAresponseforBufDB.ActionType.OBJECT_NOT_FOUND).setBufferDBChangeParam(bufferDBChangeParam).build();
                    } else if (httpResponseParams.getStatusCode() != 200) {
                        paramOMAresponseforBufDB = null;
                    } else if (dataBinary == null) {
                        iAPICallFlowListener.onFailedCall(this, bufferDBChangeParam);
                        return;
                    } else {
                        ParamOMAresponseforBufDB.Builder bufferDBChangeParam = new ParamOMAresponseforBufDB.Builder().setPayloadUrl(CloudMessageGetIndividualPayLoad.this.mBaseUrl).setByte(dataBinary).setBufferDBChangeParam(bufferDBChangeParam);
                        IAPICallFlowListener iAPICallFlowListener = iAPICallFlowListener;
                        if (iAPICallFlowListener instanceof BaseSyncHandler) {
                            bufferDBChangeParam.setActionType(ParamOMAresponseforBufDB.ActionType.ONE_PAYLOAD_DOWNLOAD);
                        } else if (iAPICallFlowListener instanceof BaseDataChangeHandler) {
                            bufferDBChangeParam.setActionType(ParamOMAresponseforBufDB.ActionType.NOTIFICATION_PAYLOAD_DOWNLOADED);
                        }
                        paramOMAresponseforBufDB = bufferDBChangeParam.build();
                    }
                    if (CloudMessageGetIndividualPayLoad.this.shouldCareAfterResponsePreProcess(iAPICallFlowListener, httpResponseParams, paramOMAresponseforBufDB, bufferDBChangeParam, Integer.MIN_VALUE)) {
                        iAPICallFlowListener.onMoveOnToNext(CloudMessageGetIndividualPayLoad.this, paramOMAresponseforBufDB);
                    }
                }
            }

            public void onFail(IOException iOException) {
                String r0 = CloudMessageGetIndividualPayLoad.this.TAG;
                Log.e(r0, "Http request onFail: " + iOException.getMessage());
                iAPICallFlowListener.onFailedCall(this);
            }
        });
    }
}
