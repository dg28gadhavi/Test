package com.sec.internal.omanetapi.nms;

import android.net.Uri;
import android.os.Message;
import android.util.Log;
import com.google.gson.Gson;
import com.sec.internal.constants.ims.cmstore.CommonErrorName;
import com.sec.internal.constants.ims.cmstore.omanetapi.OMASyncEventType;
import com.sec.internal.constants.ims.cmstore.omanetapi.SyncMsgType;
import com.sec.internal.helper.HttpRequest;
import com.sec.internal.helper.httpclient.HttpRequestParams;
import com.sec.internal.helper.httpclient.HttpResponseParams;
import com.sec.internal.ims.cmstore.MessageStoreClient;
import com.sec.internal.ims.cmstore.helper.TMOVariables;
import com.sec.internal.ims.cmstore.omanetapi.clouddatasynchandler.BaseDataChangeHandler;
import com.sec.internal.ims.cmstore.omanetapi.devicedataupdateHandler.VvmHandler;
import com.sec.internal.ims.cmstore.omanetapi.synchandler.BaseSyncHandler;
import com.sec.internal.ims.cmstore.omanetapi.tmoappapi.data.VvmFolders;
import com.sec.internal.ims.cmstore.params.BufferDBChangeParam;
import com.sec.internal.ims.cmstore.params.ParamOMAresponseforBufDB;
import com.sec.internal.interfaces.ims.cmstore.IAPICallFlowListener;
import com.sec.internal.log.IMSLog;
import com.sec.internal.omanetapi.common.data.OMAApiResponseParam;
import java.io.IOException;
import java.util.List;

public class CloudMessageGetVVMQuotaInfo extends BaseNMSRequest {
    /* access modifiers changed from: private */
    public static final String TAG = CloudMessageGetVVMQuotaInfo.class.getSimpleName();
    private static final String mAttrFilter = "Quota";
    private static final String mFolder = "folders";

    public CloudMessageGetVVMQuotaInfo(final IAPICallFlowListener iAPICallFlowListener, final BufferDBChangeParam bufferDBChangeParam, MessageStoreClient messageStoreClient) {
        super(messageStoreClient.getCloudMessageStrategyManager().getStrategy().getNmsHost(), messageStoreClient.getCloudMessageStrategyManager().getStrategy().getOMAApiVersion(), messageStoreClient.getCloudMessageStrategyManager().getStrategy().getStoreName(), bufferDBChangeParam.mLine, messageStoreClient);
        buildAPISpecificURLFromBase();
        initCommonRequestHeaders((String) null, messageStoreClient.getCloudMessageStrategyManager().getStrategy().getValidTokenByLine(bufferDBChangeParam.mLine));
        initGetRequest();
        setCallback(new HttpRequestParams.HttpRequestCallback() {
            public void onComplete(HttpResponseParams httpResponseParams) {
                List list;
                int statusCode = httpResponseParams.getStatusCode();
                String dataString = httpResponseParams.getDataString();
                String r2 = CloudMessageGetVVMQuotaInfo.TAG;
                Log.d(r2, "onComplete StatusCode: " + statusCode + " strbody: " + IMSLog.checker(dataString));
                if (statusCode == 404 || statusCode == 400) {
                    IAPICallFlowListener iAPICallFlowListener = iAPICallFlowListener;
                    if ((iAPICallFlowListener instanceof BaseSyncHandler) || (iAPICallFlowListener instanceof BaseDataChangeHandler)) {
                        ParamOMAresponseforBufDB.Builder bufferDBChangeParam = new ParamOMAresponseforBufDB.Builder().setActionType(ParamOMAresponseforBufDB.ActionType.OBJECT_NOT_FOUND).setBufferDBChangeParam(bufferDBChangeParam);
                        Message message = new Message();
                        message.obj = bufferDBChangeParam.build();
                        message.what = OMASyncEventType.VVM_CHANGE_ERROR.getId();
                        iAPICallFlowListener.onFixedFlowWithMessage(message);
                        return;
                    }
                }
                if (statusCode == 401) {
                    if (!CloudMessageGetVVMQuotaInfo.this.handleUnAuthorized(httpResponseParams)) {
                        ParamOMAresponseforBufDB.Builder line = new ParamOMAresponseforBufDB.Builder().setBufferDBChangeParam(bufferDBChangeParam).setLine(CloudMessageGetVVMQuotaInfo.this.getBoxId());
                        Message message2 = new Message();
                        message2.obj = line.build();
                        message2.what = OMASyncEventType.CREDENTIAL_EXPIRED.getId();
                        iAPICallFlowListener.onFixedFlowWithMessage(message2);
                        iAPICallFlowListener.onFailedCall(this, bufferDBChangeParam);
                    }
                } else if (CloudMessageGetVVMQuotaInfo.this.mStoreClient.getCloudMessageStrategyManager().getStrategy().isRetryRequired(statusCode)) {
                    iAPICallFlowListener.onFailedCall(this, bufferDBChangeParam, SyncMsgType.VM, statusCode);
                } else if (statusCode == 429 && (list = httpResponseParams.getHeaders().get(HttpRequest.HEADER_RETRY_AFTER)) != null && list.size() > 0) {
                    try {
                        int parseInt = Integer.parseInt((String) list.get(0));
                        if (parseInt > 0) {
                            iAPICallFlowListener.onOverRequest(this, CommonErrorName.RETRY_HEADER, parseInt);
                        } else {
                            iAPICallFlowListener.onFailedCall(this, bufferDBChangeParam);
                        }
                    } catch (NumberFormatException e) {
                        Log.e(CloudMessageGetVVMQuotaInfo.TAG, e.getMessage());
                        iAPICallFlowListener.onFailedCall(this, bufferDBChangeParam);
                    }
                } else if (statusCode != 200) {
                    iAPICallFlowListener.onFailedCall(this, bufferDBChangeParam);
                } else {
                    try {
                        try {
                            VvmFolders vvmFolders = ((OMAApiResponseParam) new Gson().fromJson(dataString, OMAApiResponseParam.class)).folder;
                            if (vvmFolders == null) {
                                iAPICallFlowListener.onFailedCall(this, bufferDBChangeParam);
                            } else if (iAPICallFlowListener instanceof VvmHandler) {
                                ParamOMAresponseforBufDB.Builder bufferDBChangeParam2 = new ParamOMAresponseforBufDB.Builder().setActionType(ParamOMAresponseforBufDB.ActionType.VVM_QUOTA_INFO).setVvmFolders(vvmFolders).setBufferDBChangeParam(bufferDBChangeParam);
                                Message message3 = new Message();
                                message3.obj = bufferDBChangeParam2.build();
                                message3.what = OMASyncEventType.VVM_CHANGE_SUCCEED.getId();
                                iAPICallFlowListener.onFixedFlowWithMessage(message3);
                            }
                        } catch (Exception e2) {
                            String r6 = CloudMessageGetVVMQuotaInfo.TAG;
                            Log.e(r6, e2.toString() + " ");
                        }
                    } catch (Exception e3) {
                        Log.e(CloudMessageGetVVMQuotaInfo.TAG, e3.getMessage());
                    }
                }
            }

            public void onFail(IOException iOException) {
                String r0 = CloudMessageGetVVMQuotaInfo.TAG;
                Log.e(r0, "Http request onFail: " + iOException.getMessage());
                iAPICallFlowListener.onFailedCall(this);
            }
        });
    }

    public void initGetRequest() {
        super.initCommonGetRequest();
    }

    /* access modifiers changed from: protected */
    public void buildAPISpecificURLFromBase() {
        Uri.Builder buildUpon = Uri.parse(this.mBaseUrl).buildUpon();
        String str = TMOVariables.TmoMessageFolderId.mVVMailInbox;
        buildUpon.appendPath(mFolder);
        buildUpon.appendPath(str);
        this.mBaseUrl = buildUpon + "%3FattrFilter%3DQuota";
        String str2 = TAG;
        Log.i(str2, "buildAPISpecificURLFromBase: " + IMSLog.checker(this.mBaseUrl));
    }
}
