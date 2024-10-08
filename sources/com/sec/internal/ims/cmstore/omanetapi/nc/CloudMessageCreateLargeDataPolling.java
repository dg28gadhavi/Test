package com.sec.internal.ims.cmstore.omanetapi.nc;

import android.util.Log;
import com.google.gson.Gson;
import com.sec.internal.constants.ims.cmstore.omanetapi.OMASyncEventType;
import com.sec.internal.constants.ims.entitilement.SoftphoneNamespaces;
import com.sec.internal.helper.httpclient.HttpRequestParams;
import com.sec.internal.helper.httpclient.HttpResponseParams;
import com.sec.internal.ims.cmstore.MessageStoreClient;
import com.sec.internal.ims.cmstore.helper.MailBoxHelper;
import com.sec.internal.ims.cmstore.params.BufferDBChangeParam;
import com.sec.internal.ims.cmstore.params.ParamOMAresponseforBufDB;
import com.sec.internal.ims.cmstore.utils.NotificationListContainer;
import com.sec.internal.ims.cmstore.utils.Util;
import com.sec.internal.interfaces.ims.cmstore.IAPICallFlowListener;
import com.sec.internal.interfaces.ims.cmstore.IControllerCommonInterface;
import com.sec.internal.omanetapi.common.data.OMAApiResponseParam;
import com.sec.internal.omanetapi.nc.NotificationList;
import com.sec.internal.omanetapi.nc.data.LongPollingRequestParameters;
import com.sec.internal.omanetapi.nms.data.NmsEventList;
import java.io.IOException;
import java.util.Arrays;

public class CloudMessageCreateLargeDataPolling extends NotificationList {
    /* access modifiers changed from: private */
    public String TAG = CloudMessageCreateLargeDataPolling.class.getSimpleName();
    /* access modifiers changed from: private */
    public final transient IAPICallFlowListener mIAPICallFlowListener;
    /* access modifiers changed from: private */
    public final transient IControllerCommonInterface mIControllerCommonInterface;

    public CloudMessageCreateLargeDataPolling(final IAPICallFlowListener iAPICallFlowListener, IControllerCommonInterface iControllerCommonInterface, String str, MessageStoreClient messageStoreClient) {
        super(str, messageStoreClient);
        this.TAG += "[" + messageStoreClient.getClientID() + "]";
        this.mIControllerCommonInterface = iControllerCommonInterface;
        this.mIAPICallFlowListener = iAPICallFlowListener;
        this.mBaseUrl = Util.replaceUrlPrefix(this.mBaseUrl, messageStoreClient);
        initCommonRequestHeaders(this.mStoreClient.getCloudMessageStrategyManager().getStrategy().getContentType(), this.mStoreClient.getCloudMessageStrategyManager().getStrategy().getValidTokenByLine(this.mStoreClient.getPrerenceManager().getUserTelCtn()));
        initPostRequest((LongPollingRequestParameters) null, true);
        setCallback(new HttpRequestParams.HttpRequestCallback() {
            public void onComplete(HttpResponseParams httpResponseParams) {
                com.sec.internal.omanetapi.nc.data.NotificationList[] notificationListArr;
                if (httpResponseParams.getStatusCode() == 206) {
                    httpResponseParams.setStatusCode(200);
                }
                if (httpResponseParams.getStatusCode() == 200) {
                    try {
                        OMAApiResponseParam oMAApiResponseParam = (OMAApiResponseParam) new Gson().fromJson(httpResponseParams.getDataString(), OMAApiResponseParam.class);
                        String r2 = CloudMessageCreateLargeDataPolling.this.TAG;
                        StringBuilder sb = new StringBuilder();
                        sb.append("OMAApiResponseParam response ");
                        sb.append(httpResponseParams.getDataString());
                        sb.append(" response.notificationList: ");
                        sb.append(oMAApiResponseParam != null ? Arrays.toString(oMAApiResponseParam.notificationList) : null);
                        Log.i(r2, sb.toString());
                        if (oMAApiResponseParam == null || (notificationListArr = oMAApiResponseParam.notificationList) == null) {
                            CloudMessageCreateLargeDataPolling.this.mIAPICallFlowListener.onFailedCall(this);
                            CloudMessageCreateLargeDataPolling.this.mIAPICallFlowListener.onGoToEvent(OMASyncEventType.SEND_LARGE_DATA_POLLING_FINISHED.getId(), (Object) null);
                            return;
                        }
                        boolean z = false;
                        if (notificationListArr.length > 0) {
                            if (MailBoxHelper.isMailBoxReset(httpResponseParams.getDataString())) {
                                Log.i(CloudMessageCreateLargeDataPolling.this.TAG, "MailBoxReset true");
                                CloudMessageCreateLargeDataPolling.this.mIAPICallFlowListener.onSuccessfulEvent(this, OMASyncEventType.MAILBOX_RESET.getId(), (Object) null);
                                CloudMessageCreateLargeDataPolling.this.mIAPICallFlowListener.onGoToEvent(OMASyncEventType.SEND_LARGE_DATA_POLLING_FINISHED.getId(), (Object) null);
                                return;
                            }
                            NmsEventList nmsEventList = notificationListArr[0].nmsEventList;
                            if (nmsEventList != null && Util.isMatchedSubscriptionID(nmsEventList, CloudMessageCreateLargeDataPolling.this.mStoreClient)) {
                                long oMASubscriptionIndex = CloudMessageCreateLargeDataPolling.this.mStoreClient.getPrerenceManager().getOMASubscriptionIndex();
                                long longValue = notificationListArr[0].nmsEventList.index.longValue();
                                String r11 = CloudMessageCreateLargeDataPolling.this.TAG;
                                Log.i(r11, "curindex: " + longValue + ",savedindex: " + oMASubscriptionIndex);
                                int i = (longValue > (oMASubscriptionIndex + 1) ? 1 : (longValue == (oMASubscriptionIndex + 1) ? 0 : -1));
                                if (i > 0) {
                                    z = NotificationListContainer.getInstance(CloudMessageCreateLargeDataPolling.this.mStoreClient.getClientID()).isEmpty();
                                    NotificationListContainer.getInstance(CloudMessageCreateLargeDataPolling.this.mStoreClient.getClientID()).insertContainer(Long.valueOf(longValue), notificationListArr);
                                } else if (i == 0) {
                                    CloudMessageCreateLargeDataPolling.this.mStoreClient.getPrerenceManager().saveOMASubscriptionRestartToken(notificationListArr[0].nmsEventList.restartToken);
                                    CloudMessageCreateLargeDataPolling.this.mStoreClient.getPrerenceManager().saveOMASubscriptionIndex(longValue);
                                    CloudMessageCreateLargeDataPolling.this.mIAPICallFlowListener.onGoToEvent(OMASyncEventType.CLOUD_UPDATE.getId(), new ParamOMAresponseforBufDB.Builder().setActionType(ParamOMAresponseforBufDB.ActionType.RECEIVE_NOTIFICATION).setNotificationList(notificationListArr).build());
                                    long oMASubscriptionIndex2 = CloudMessageCreateLargeDataPolling.this.mStoreClient.getPrerenceManager().getOMASubscriptionIndex();
                                    while (true) {
                                        if (NotificationListContainer.getInstance(CloudMessageCreateLargeDataPolling.this.mStoreClient.getClientID()).isEmpty() || NotificationListContainer.getInstance(CloudMessageCreateLargeDataPolling.this.mStoreClient.getClientID()).peekFirstIndex() != oMASubscriptionIndex2 + 1) {
                                            break;
                                        }
                                        com.sec.internal.omanetapi.nc.data.NotificationList[] value = NotificationListContainer.getInstance(CloudMessageCreateLargeDataPolling.this.mStoreClient.getClientID()).popFirstEntry().getValue();
                                        NmsEventList nmsEventList2 = value[0].nmsEventList;
                                        String str = nmsEventList2.restartToken;
                                        long longValue2 = nmsEventList2.index.longValue();
                                        CloudMessageCreateLargeDataPolling.this.mStoreClient.getPrerenceManager().saveOMASubscriptionRestartToken(str);
                                        CloudMessageCreateLargeDataPolling.this.mStoreClient.getPrerenceManager().saveOMASubscriptionIndex(longValue2);
                                        CloudMessageCreateLargeDataPolling.this.mIAPICallFlowListener.onGoToEvent(OMASyncEventType.CLOUD_UPDATE.getId(), new ParamOMAresponseforBufDB.Builder().setActionType(ParamOMAresponseforBufDB.ActionType.RECEIVE_NOTIFICATION).setNotificationList(value).build());
                                        oMASubscriptionIndex2 = CloudMessageCreateLargeDataPolling.this.mStoreClient.getPrerenceManager().getOMASubscriptionIndex();
                                        if (NotificationListContainer.getInstance(CloudMessageCreateLargeDataPolling.this.mStoreClient.getClientID()).isEmpty()) {
                                            CloudMessageCreateLargeDataPolling.this.mIControllerCommonInterface.update(OMASyncEventType.REMOVE_UPDATE_SUBSCRIPTION_CHANNEL.getId());
                                            break;
                                        }
                                    }
                                }
                            }
                        }
                        if (z) {
                            CloudMessageCreateLargeDataPolling.this.mIControllerCommonInterface.updateDelay(OMASyncEventType.UPDATE_SUBSCRIPTION_CHANNEL_DELAY.getId(), SoftphoneNamespaces.SoftphoneSettings.LONG_BACKOFF);
                        }
                        CloudMessageCreateLargeDataPolling.this.mIAPICallFlowListener.onSuccessfulEvent(this, OMASyncEventType.SEND_LARGE_DATA_POLLING_FINISHED.getId(), (Object) null);
                    } catch (Exception e) {
                        String r0 = CloudMessageCreateLargeDataPolling.this.TAG;
                        Log.e(r0, "exception occurred " + e.getMessage());
                        e.printStackTrace();
                        CloudMessageCreateLargeDataPolling.this.mIAPICallFlowListener.onFailedCall(this);
                        CloudMessageCreateLargeDataPolling.this.mIAPICallFlowListener.onGoToEvent(OMASyncEventType.SEND_LARGE_DATA_POLLING_FINISHED.getId(), (Object) null);
                    }
                } else if (!CloudMessageCreateLargeDataPolling.this.shouldCareAfterResponsePreProcess(iAPICallFlowListener, httpResponseParams, (Object) null, (BufferDBChangeParam) null, Integer.MIN_VALUE)) {
                    CloudMessageCreateLargeDataPolling.this.mIAPICallFlowListener.onSuccessfulEvent(this, OMASyncEventType.SEND_LARGE_DATA_POLLING_FINISHED.getId(), (Object) null);
                } else {
                    iAPICallFlowListener.onFailedCall(CloudMessageCreateLargeDataPolling.this);
                    CloudMessageCreateLargeDataPolling.this.mIAPICallFlowListener.onGoToEvent(OMASyncEventType.SEND_LARGE_DATA_POLLING_FINISHED.getId(), (Object) null);
                }
            }

            public void onFail(IOException iOException) {
                String r0 = CloudMessageCreateLargeDataPolling.this.TAG;
                Log.e(r0, "Http request onFail: " + iOException.getMessage());
                CloudMessageCreateLargeDataPolling.this.mIAPICallFlowListener.onFailedCall(this);
                CloudMessageCreateLargeDataPolling.this.mIAPICallFlowListener.onGoToEvent(OMASyncEventType.SEND_LARGE_DATA_POLLING_FINISHED.getId(), (Object) null);
            }
        });
    }
}
