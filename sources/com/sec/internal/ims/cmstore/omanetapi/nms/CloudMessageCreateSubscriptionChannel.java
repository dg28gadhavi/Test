package com.sec.internal.ims.cmstore.omanetapi.nms;

import android.util.Log;
import com.google.gson.Gson;
import com.sec.internal.constants.ims.cmstore.omanetapi.OMASyncEventType;
import com.sec.internal.helper.httpclient.HttpRequestParams;
import com.sec.internal.helper.httpclient.HttpResponseParams;
import com.sec.internal.ims.cmstore.MessageStoreClient;
import com.sec.internal.ims.cmstore.helper.ATTGlobalVariables;
import com.sec.internal.ims.cmstore.params.BufferDBChangeParam;
import com.sec.internal.ims.cmstore.utils.CloudMessagePreferenceManager;
import com.sec.internal.ims.cmstore.utils.McsNotificationListContainer;
import com.sec.internal.ims.cmstore.utils.NotificationListContainer;
import com.sec.internal.interfaces.ims.cmstore.IAPICallFlowListener;
import com.sec.internal.interfaces.ims.cmstore.IControllerCommonInterface;
import com.sec.internal.log.IMSLog;
import com.sec.internal.omanetapi.common.data.CallbackReference;
import com.sec.internal.omanetapi.common.data.OMAApiResponseParam;
import com.sec.internal.omanetapi.nms.AllSubscriptions;
import com.sec.internal.omanetapi.nms.data.NmsSubscription;
import com.sec.internal.omanetapi.nms.data.SearchCriteria;
import com.sec.internal.omanetapi.nms.data.SearchCriterion;
import java.io.IOException;
import java.net.URL;

public class CloudMessageCreateSubscriptionChannel extends AllSubscriptions {
    private static final long serialVersionUID = 3483856569808284340L;
    /* access modifiers changed from: private */
    public String TAG = CloudMessageCreateSubscriptionChannel.class.getSimpleName();
    /* access modifiers changed from: private */
    public final transient IControllerCommonInterface mIControllerCommonInterface;

    public CloudMessageCreateSubscriptionChannel(final IAPICallFlowListener iAPICallFlowListener, String str, String str2, IControllerCommonInterface iControllerCommonInterface, boolean z, MessageStoreClient messageStoreClient) {
        super(messageStoreClient.getCloudMessageStrategyManager().getStrategy().getNcHost(), messageStoreClient.getCloudMessageStrategyManager().getStrategy().getOMAApiVersion(), messageStoreClient.getCloudMessageStrategyManager().getStrategy().getStoreName(), messageStoreClient.getPrerenceManager().getUserTelCtn(), messageStoreClient);
        this.TAG += "[" + messageStoreClient.getClientID() + "]";
        this.mIControllerCommonInterface = iControllerCommonInterface;
        NmsSubscription nmsSubscription = new NmsSubscription();
        CallbackReference callbackReference = new CallbackReference();
        callbackReference.notifyURL = str;
        if (ATTGlobalVariables.isGcmReplacePolling()) {
            callbackReference.callbackData = "custom_data";
        } else if (this.isCmsMcsEnabled) {
            callbackReference.callbackData = "abc";
        } else {
            callbackReference.notificationFormat = this.mStoreClient.getCloudMessageStrategyManager().getStrategy().getNotificaitonFormat();
        }
        nmsSubscription.callbackReference = callbackReference;
        nmsSubscription.duration = 86400;
        nmsSubscription.clientCorrelator = "";
        nmsSubscription.restartToken = str2;
        Log.i(this.TAG, "notifyURL " + IMSLog.numberChecker(str) + " request restartToken " + str2 + " isGcmReplacePolling: " + ATTGlobalVariables.isGcmReplacePolling() + " needPresetSearchRemove" + z);
        if (ATTGlobalVariables.isGcmReplacePolling() && !z) {
            SearchCriteria searchCriteria = new SearchCriteria();
            SearchCriterion[] searchCriterionArr = {new SearchCriterion()};
            SearchCriterion searchCriterion = searchCriterionArr[0];
            searchCriterion.type = "PresetSearch";
            searchCriterion.name = "UPOneDotO";
            searchCriterion.value = "";
            searchCriteria.criterion = searchCriterionArr;
            nmsSubscription.filter = searchCriteria;
        }
        String authorizationBearer = this.isCmsMcsEnabled ? this.mStoreClient.getCloudMessageStrategyManager().getStrategy().getAuthorizationBearer() : this.mStoreClient.getCloudMessageStrategyManager().getStrategy().getValidTokenByLine(this.mStoreClient.getPrerenceManager().getUserTelCtn());
        if (ATTGlobalVariables.isGcmReplacePolling()) {
            initSubscribeRequestHeaders(this.mStoreClient.getCloudMessageStrategyManager().getStrategy().getContentType(), authorizationBearer);
        } else if (this.isCmsMcsEnabled) {
            initMcsCommonRequestHeaders(this.mStoreClient.getCloudMessageStrategyManager().getStrategy().getContentType(), authorizationBearer);
        } else {
            initCommonRequestHeaders(this.mStoreClient.getCloudMessageStrategyManager().getStrategy().getContentType(), authorizationBearer);
        }
        initPostRequest(nmsSubscription, true);
        setCallback(new HttpRequestParams.HttpRequestCallback() {
            public void onComplete(HttpResponseParams httpResponseParams) {
                String str;
                String r0 = CloudMessageCreateSubscriptionChannel.this.TAG;
                Log.i(r0, "The content of the response = " + IMSLog.numberChecker(httpResponseParams.getDataString()));
                if (httpResponseParams.getStatusCode() != 401 || !CloudMessageCreateSubscriptionChannel.this.handleUnAuthorized(httpResponseParams)) {
                    iAPICallFlowListener.onGoToEvent(OMASyncEventType.CREATE_SUBSCRIPTION_FINISHED.getId(), (Object) null);
                    if (httpResponseParams.getStatusCode() == 201) {
                        OMAApiResponseParam response = CloudMessageCreateSubscriptionChannel.this.getResponse(httpResponseParams);
                        if (response == null) {
                            iAPICallFlowListener.onFailedCall(this);
                            return;
                        }
                        NmsSubscription nmsSubscription = response.nmsSubscription;
                        if (nmsSubscription != null) {
                            String str2 = nmsSubscription.restartToken;
                            int intValue = nmsSubscription.duration.intValue();
                            long j = (((long) intValue) * 1000) - 360000;
                            if (ATTGlobalVariables.isGcmReplacePolling()) {
                                CloudMessageCreateSubscriptionChannel.this.mIControllerCommonInterface.updateDelay(OMASyncEventType.CHECK_SUBSCRIPTION_AND_START_LONG_POLLING.getId(), j);
                            } else if (CloudMessageCreateSubscriptionChannel.this.isCmsMcsEnabled) {
                                CloudMessageCreateSubscriptionChannel.this.mIControllerCommonInterface.updateDelay(OMASyncEventType.CHECK_SUBSCRIPTION_CHANNEL.getId(), j);
                            }
                            CloudMessagePreferenceManager prerenceManager = CloudMessageCreateSubscriptionChannel.this.mStoreClient.getPrerenceManager();
                            prerenceManager.saveOMASubscriptionIndex(nmsSubscription.index.longValue() - 1);
                            prerenceManager.saveOMASubscriptionRestartToken(str2);
                            prerenceManager.saveOMASubscriptionTime(System.currentTimeMillis());
                            prerenceManager.saveOMASubscriptionChannelDuration(intValue);
                            URL url = nmsSubscription.resourceURL;
                            if (url != null) {
                                prerenceManager.saveOMASubscriptionResUrl(url.toString());
                                if (CloudMessageCreateSubscriptionChannel.this.isCmsMcsEnabled) {
                                    McsNotificationListContainer.getInstance(CloudMessageCreateSubscriptionChannel.this.mStoreClient.getClientID()).clear(CloudMessageCreateSubscriptionChannel.this.mStoreClient.getClientID());
                                } else {
                                    NotificationListContainer.getInstance(CloudMessageCreateSubscriptionChannel.this.mStoreClient.getClientID()).clear();
                                }
                            }
                            if (ATTGlobalVariables.isGcmReplacePolling() || CloudMessageCreateSubscriptionChannel.this.isCmsMcsEnabled) {
                                iAPICallFlowListener.onSuccessfulCall(this, (String) null);
                            } else {
                                iAPICallFlowListener.onSuccessfulEvent(this, OMASyncEventType.SEND_LONG_POLLING_REQUEST.getId(), (Object) null);
                            }
                        } else {
                            iAPICallFlowListener.onFailedCall(this);
                        }
                    } else {
                        if (!CloudMessageCreateSubscriptionChannel.this.isCmsMcsEnabled && httpResponseParams.getStatusCode() == 400) {
                            try {
                                OMAApiResponseParam oMAApiResponseParam = (OMAApiResponseParam) new Gson().fromJson(httpResponseParams.getDataString(), OMAApiResponseParam.class);
                                str = oMAApiResponseParam != null ? oMAApiResponseParam.requestError.serviceException.messageId : null;
                            } catch (RuntimeException e) {
                                e.printStackTrace();
                                str = CloudMessageCreateSubscriptionChannel.this.getResponseMessageId(httpResponseParams.getDataString());
                            }
                            if (str != null && str.equals("SVC0003")) {
                                String r10 = CloudMessageCreateSubscriptionChannel.this.TAG;
                                Log.d(r10, "messageId is " + str + ", remove PresetSearch Filter and resend subscription HTTP request");
                                iAPICallFlowListener.onFailedEvent(OMASyncEventType.REQUEST_SUBSCRIPTION_AFTER_PSF_REMOVED.getId(), (Object) null);
                                return;
                            }
                        }
                        if (CloudMessageCreateSubscriptionChannel.this.shouldCareAfterResponsePreProcess(iAPICallFlowListener, httpResponseParams, (Object) null, (BufferDBChangeParam) null, Integer.MIN_VALUE) && !CloudMessageCreateSubscriptionChannel.this.isCmsMcsEnabled) {
                            iAPICallFlowListener.onFailedCall(CloudMessageCreateSubscriptionChannel.this);
                        }
                    }
                }
            }

            public void onFail(IOException iOException) {
                String r0 = CloudMessageCreateSubscriptionChannel.this.TAG;
                Log.e(r0, "Http request onFail: " + iOException.getMessage());
                iAPICallFlowListener.onFailedCall(this);
            }
        });
    }
}
