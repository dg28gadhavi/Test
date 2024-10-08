package com.sec.internal.ims.cmstore.omanetapi.nc;

import android.util.Log;
import com.google.gson.Gson;
import com.sec.internal.constants.ims.cmstore.omanetapi.OMASyncEventType;
import com.sec.internal.helper.httpclient.HttpRequestParams;
import com.sec.internal.helper.httpclient.HttpResponseParams;
import com.sec.internal.ims.cmstore.MessageStoreClient;
import com.sec.internal.ims.cmstore.helper.MailBoxHelper;
import com.sec.internal.ims.cmstore.params.BufferDBChangeParam;
import com.sec.internal.ims.cmstore.params.ParamOMAresponseforBufDB;
import com.sec.internal.ims.cmstore.utils.Util;
import com.sec.internal.interfaces.ims.cmstore.IAPICallFlowListener;
import com.sec.internal.omanetapi.common.data.OMAApiResponseParam;
import com.sec.internal.omanetapi.nc.NotificationList;
import com.sec.internal.omanetapi.nc.data.LongPollingRequestParameters;
import java.io.IOException;

public class CloudMessageCreateLongPolling extends NotificationList {
    private static final long serialVersionUID = -1240603457039213893L;
    /* access modifiers changed from: private */
    public String TAG = CloudMessageCreateLongPolling.class.getSimpleName();
    /* access modifiers changed from: private */
    public final transient IAPICallFlowListener mIAPICallFlowListener;

    public CloudMessageCreateLongPolling(final IAPICallFlowListener iAPICallFlowListener, String str, MessageStoreClient messageStoreClient) {
        super(str, messageStoreClient);
        this.mIAPICallFlowListener = iAPICallFlowListener;
        this.TAG += "[" + messageStoreClient.getClientID() + "]";
        this.mBaseUrl = Util.replaceUrlPrefix(this.mBaseUrl, messageStoreClient);
        initCommonRequestHeaders(this.mStoreClient.getCloudMessageStrategyManager().getStrategy().getContentType(), this.mStoreClient.getCloudMessageStrategyManager().getStrategy().getValidTokenByLine(this.mStoreClient.getPrerenceManager().getUserTelCtn()));
        initPostRequest((LongPollingRequestParameters) null, true);
        setCallback(new HttpRequestParams.HttpRequestCallback() {
            public void onComplete(HttpResponseParams httpResponseParams) {
                com.sec.internal.omanetapi.nc.data.NotificationList[] notificationListArr;
                CloudMessageCreateLongPolling.this.mIAPICallFlowListener.onGoToEvent(OMASyncEventType.ONE_POLLING_FINISHED.getId(), (Object) null);
                if (httpResponseParams.getStatusCode() == 206) {
                    httpResponseParams.setStatusCode(200);
                }
                if (httpResponseParams.getStatusCode() == 200) {
                    try {
                        OMAApiResponseParam oMAApiResponseParam = (OMAApiResponseParam) new Gson().fromJson(httpResponseParams.getDataString(), OMAApiResponseParam.class);
                        if (oMAApiResponseParam == null || (notificationListArr = oMAApiResponseParam.notificationList) == null) {
                            Log.i(CloudMessageCreateLongPolling.this.TAG, "response or notificationList is null, polling failed");
                            CloudMessageCreateLongPolling.this.mIAPICallFlowListener.onFailedCall(this);
                            return;
                        }
                        boolean z = false;
                        if (notificationListArr.length > 0) {
                            if (MailBoxHelper.isMailBoxReset(httpResponseParams.getDataString())) {
                                Log.i(CloudMessageCreateLongPolling.this.TAG, "MailBoxReset true");
                                CloudMessageCreateLongPolling.this.mIAPICallFlowListener.onSuccessfulEvent(this, OMASyncEventType.MAILBOX_RESET.getId(), (Object) null);
                                return;
                            } else if (notificationListArr[0].nmsEventList != null) {
                                long oMASubscriptionIndex = CloudMessageCreateLongPolling.this.mStoreClient.getPrerenceManager().getOMASubscriptionIndex();
                                long longValue = notificationListArr[0].nmsEventList.index.longValue();
                                String r1 = CloudMessageCreateLongPolling.this.TAG;
                                Log.i(r1, "savedindex: " + oMASubscriptionIndex + " curindex: " + longValue);
                                boolean z2 = oMASubscriptionIndex != 0 && longValue > oMASubscriptionIndex + 1;
                                String str = notificationListArr[0].nmsEventList.restartToken;
                                CloudMessageCreateLongPolling.this.mStoreClient.getPrerenceManager().saveOMASubscriptionIndex(longValue);
                                CloudMessageCreateLongPolling.this.mStoreClient.getPrerenceManager().saveOMASubscriptionRestartToken(str);
                                z = z2;
                            }
                        }
                        ParamOMAresponseforBufDB.Builder notificationList = new ParamOMAresponseforBufDB.Builder().setActionType(ParamOMAresponseforBufDB.ActionType.RECEIVE_NOTIFICATION).setNotificationList(notificationListArr);
                        if (z) {
                            CloudMessageCreateLongPolling.this.mIAPICallFlowListener.onGoToEvent(OMASyncEventType.UPDATE_SUBSCRIPTION_CHANNEL.getId(), (Object) null);
                        }
                        CloudMessageCreateLongPolling.this.mIAPICallFlowListener.onGoToEvent(OMASyncEventType.CLOUD_UPDATE.getId(), notificationList.build());
                    } catch (Exception e) {
                        String r0 = CloudMessageCreateLongPolling.this.TAG;
                        Log.e(r0, "exception occurred " + e.getMessage());
                        e.printStackTrace();
                        CloudMessageCreateLongPolling.this.mIAPICallFlowListener.onFailedCall(this);
                        return;
                    }
                }
                if (CloudMessageCreateLongPolling.this.shouldCareAfterResponsePreProcess(iAPICallFlowListener, httpResponseParams, (Object) null, (BufferDBChangeParam) null, Integer.MIN_VALUE)) {
                    iAPICallFlowListener.onFailedCall(CloudMessageCreateLongPolling.this);
                }
            }

            public void onFail(IOException iOException) {
                String r0 = CloudMessageCreateLongPolling.this.TAG;
                Log.e(r0, "Http request onFail: " + iOException.getMessage());
                CloudMessageCreateLongPolling.this.mIAPICallFlowListener.onFailedCall(this);
            }
        });
    }

    public HttpRequestParams getRetryInstance(IAPICallFlowListener iAPICallFlowListener, MessageStoreClient messageStoreClient) {
        return new CloudMessageCreateLongPolling(this.mIAPICallFlowListener, messageStoreClient.getPrerenceManager().getOMAChannelURL(), messageStoreClient);
    }
}
