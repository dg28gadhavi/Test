package com.sec.internal.ims.cmstore.omanetapi.nc;

import android.text.TextUtils;
import android.util.Log;
import com.google.gson.GsonBuilder;
import com.sec.internal.constants.ims.cmstore.omanetapi.OMASyncEventType;
import com.sec.internal.constants.ims.cmstore.utils.OMAGlobalVariables;
import com.sec.internal.helper.httpclient.HttpRequestParams;
import com.sec.internal.helper.httpclient.HttpResponseParams;
import com.sec.internal.ims.cmstore.MessageStoreClient;
import com.sec.internal.ims.cmstore.helper.ATTGlobalVariables;
import com.sec.internal.ims.cmstore.omanetapi.nms.data.GsonInterfaceAdapter;
import com.sec.internal.ims.cmstore.params.BufferDBChangeParam;
import com.sec.internal.ims.cmstore.utils.CloudMessagePreferenceManager;
import com.sec.internal.interfaces.ims.cmstore.IAPICallFlowListener;
import com.sec.internal.interfaces.ims.cmstore.IControllerCommonInterface;
import com.sec.internal.log.IMSLog;
import com.sec.internal.omanetapi.common.data.OMAApiResponseParam;
import com.sec.internal.omanetapi.nc.NotificationChannels;
import com.sec.internal.omanetapi.nc.data.ChannelData;
import com.sec.internal.omanetapi.nc.data.ChannelType;
import com.sec.internal.omanetapi.nc.data.GcmChannelData;
import com.sec.internal.omanetapi.nc.data.LongPollingData;
import com.sec.internal.omanetapi.nc.data.NotificationChannel;
import java.io.IOException;
import java.net.URL;

public class CloudMessageCreateNotificationChannels extends NotificationChannels {
    private static final long serialVersionUID = 3299934859221120896L;
    /* access modifiers changed from: private */
    public String TAG = CloudMessageCreateNotificationChannels.class.getSimpleName();
    /* access modifiers changed from: private */
    public final transient IAPICallFlowListener mIAPICallFlowListener;
    /* access modifiers changed from: private */
    public final transient IControllerCommonInterface mIControllerCommonInterface;

    public CloudMessageCreateNotificationChannels(final IAPICallFlowListener iAPICallFlowListener, IControllerCommonInterface iControllerCommonInterface, final boolean z, MessageStoreClient messageStoreClient) {
        super(messageStoreClient.getCloudMessageStrategyManager().getStrategy().getNcHost(), messageStoreClient.getCloudMessageStrategyManager().getStrategy().getOMAApiVersion(), messageStoreClient.getPrerenceManager().getUserTelCtn(), messageStoreClient);
        this.TAG += "[" + messageStoreClient.getClientID() + "]";
        this.mIAPICallFlowListener = iAPICallFlowListener;
        this.mIControllerCommonInterface = iControllerCommonInterface;
        NotificationChannel notificationChannel = new NotificationChannel();
        notificationChannel.clientCorrelator = "";
        notificationChannel.applicationTag = "";
        notificationChannel.channelLifetime = 86400;
        if (ATTGlobalVariables.isGcmReplacePolling()) {
            notificationChannel.channelType = ChannelType.NativeChannel;
            GcmChannelData gcmChannelData = new GcmChannelData();
            gcmChannelData.channelSubType = OMAGlobalVariables.CHANNEL_TYPE_GCM;
            gcmChannelData.channelSubTypeVersion = "1.0";
            gcmChannelData.registrationToken = this.mStoreClient.getPrerenceManager().getGcmTokenFromVsim();
            gcmChannelData.maxNotifications = 1;
            notificationChannel.channelData = gcmChannelData;
        } else {
            notificationChannel.channelType = ChannelType.LongPolling;
            LongPollingData longPollingData = new LongPollingData();
            longPollingData.maxNotifications = 1;
            notificationChannel.channelData = longPollingData;
        }
        initCommonRequestHeaders(this.mStoreClient.getCloudMessageStrategyManager().getStrategy().getContentType(), this.mStoreClient.getCloudMessageStrategyManager().getStrategy().getValidTokenByLine(this.mStoreClient.getPrerenceManager().getUserTelCtn()));
        initPostRequest(notificationChannel, true);
        setCallback(new HttpRequestParams.HttpRequestCallback() {
            public void onComplete(HttpResponseParams httpResponseParams) {
                NotificationChannel notificationChannel;
                if (httpResponseParams.getStatusCode() == 201) {
                    GsonBuilder gsonBuilder = new GsonBuilder();
                    gsonBuilder.registerTypeAdapter(ChannelData.class, new GsonInterfaceAdapter(LongPollingData.class));
                    try {
                        OMAApiResponseParam oMAApiResponseParam = (OMAApiResponseParam) gsonBuilder.create().fromJson(httpResponseParams.getDataString(), OMAApiResponseParam.class);
                        if (oMAApiResponseParam == null || (notificationChannel = oMAApiResponseParam.notificationChannel) == null) {
                            CloudMessageCreateNotificationChannels.this.mIAPICallFlowListener.onFailedCall(this);
                            return;
                        }
                        CloudMessagePreferenceManager prerenceManager = CloudMessageCreateNotificationChannels.this.mStoreClient.getPrerenceManager();
                        URL url = notificationChannel.resourceURL;
                        String url2 = url == null ? "" : url.toString();
                        if (!ATTGlobalVariables.isGcmReplacePolling()) {
                            URL url3 = ((LongPollingData) notificationChannel.channelData).channelURL;
                            if (url3 == null) {
                                CloudMessageCreateNotificationChannels.this.mIAPICallFlowListener.onFailedCall(this);
                                return;
                            }
                            prerenceManager.saveOMAChannelURL(url3.toString());
                        }
                        long j = (long) notificationChannel.channelLifetime;
                        if (ATTGlobalVariables.isGcmReplacePolling()) {
                            CloudMessageCreateNotificationChannels.this.mIControllerCommonInterface.updateDelay(OMASyncEventType.UPDATE_NOTIFICATION_CHANNEL_LIFETIME.getId(), (j - 900) * 1000);
                        }
                        String r5 = CloudMessageCreateNotificationChannels.this.TAG;
                        Log.i(r5, "channelLifeTime=" + j + " callbackURL: " + IMSLog.checker(notificationChannel.callbackURL) + " isNeedDeleteSubscription: " + z);
                        String str = notificationChannel.callbackURL;
                        if (str != null && !str.equals(prerenceManager.getOMACallBackURL())) {
                            if (z && !TextUtils.isEmpty(prerenceManager.getOMASubscriptionResUrl())) {
                                CloudMessageCreateNotificationChannels.this.mIAPICallFlowListener.onGoToEvent(OMASyncEventType.DELETE_SUBCRIPTION_CHANNEL.getId(), prerenceManager.getOMASubscriptionResUrl());
                            }
                            CloudMessageCreateNotificationChannels.this.mStoreClient.getPrerenceManager().saveOMASubscriptionTime(0);
                            CloudMessageCreateNotificationChannels.this.mStoreClient.getPrerenceManager().saveOMASubscriptionChannelDuration(0);
                        }
                        prerenceManager.saveOMAChannelResURL(url2);
                        String str2 = notificationChannel.callbackURL;
                        if (str2 != null) {
                            prerenceManager.saveOMACallBackURL(str2.toString());
                        }
                        prerenceManager.saveOMAChannelCreateTime(System.currentTimeMillis());
                        prerenceManager.saveOMAChannelLifeTime(j);
                        prerenceManager.clearOMASubscriptionChannelDuration();
                        prerenceManager.clearOMASubscriptionTime();
                        CloudMessageCreateNotificationChannels.this.mIControllerCommonInterface.update(OMASyncEventType.CREATE_NOTIFICATION_CHANNEL_FINISHED.getId());
                    } catch (Exception e) {
                        String r0 = CloudMessageCreateNotificationChannels.this.TAG;
                        Log.e(r0, "exception occurred " + e.getMessage());
                        e.printStackTrace();
                        CloudMessageCreateNotificationChannels.this.mIAPICallFlowListener.onFailedCall(this);
                        return;
                    }
                }
                if (CloudMessageCreateNotificationChannels.this.shouldCareAfterResponsePreProcess(iAPICallFlowListener, httpResponseParams, (Object) null, (BufferDBChangeParam) null, Integer.MIN_VALUE)) {
                    iAPICallFlowListener.onFailedCall(CloudMessageCreateNotificationChannels.this);
                }
            }

            public void onFail(IOException iOException) {
                String r0 = CloudMessageCreateNotificationChannels.this.TAG;
                Log.e(r0, "Http request onFail: " + iOException.getMessage());
                CloudMessageCreateNotificationChannels.this.mIAPICallFlowListener.onFailedCall(this);
            }
        });
    }

    public HttpRequestParams getRetryInstance(IAPICallFlowListener iAPICallFlowListener, MessageStoreClient messageStoreClient) {
        return new CloudMessageCreateNotificationChannels(this.mIAPICallFlowListener, this.mIControllerCommonInterface, true, messageStoreClient);
    }
}
