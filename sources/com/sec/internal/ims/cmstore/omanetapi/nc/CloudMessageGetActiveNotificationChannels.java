package com.sec.internal.ims.cmstore.omanetapi.nc;

import android.util.Log;
import com.google.gson.Gson;
import com.sec.internal.constants.ims.cmstore.omanetapi.OMASyncEventType;
import com.sec.internal.helper.httpclient.HttpRequestParams;
import com.sec.internal.helper.httpclient.HttpResponseParams;
import com.sec.internal.ims.cmstore.MessageStoreClient;
import com.sec.internal.ims.cmstore.params.BufferDBChangeParam;
import com.sec.internal.interfaces.ims.cmstore.IAPICallFlowListener;
import com.sec.internal.interfaces.ims.cmstore.IControllerCommonInterface;
import com.sec.internal.log.IMSLog;
import com.sec.internal.omanetapi.common.data.OMAApiResponseParam;
import com.sec.internal.omanetapi.nc.IndividualNotificationChannel;
import com.sec.internal.omanetapi.nc.data.ChannelDeleteData;
import com.sec.internal.omanetapi.nc.data.NotificationChannelList;
import java.io.IOException;
import java.net.URL;

public class CloudMessageGetActiveNotificationChannels extends IndividualNotificationChannel {
    public String TAG = CloudMessageGetActiveNotificationChannels.class.getSimpleName();
    /* access modifiers changed from: private */
    public final transient IAPICallFlowListener mIAPICallFlowListener;

    public CloudMessageGetActiveNotificationChannels(final IAPICallFlowListener iAPICallFlowListener, IControllerCommonInterface iControllerCommonInterface, MessageStoreClient messageStoreClient) {
        super(messageStoreClient.getCloudMessageStrategyManager().getStrategy().getNcHost(), messageStoreClient.getCloudMessageStrategyManager().getStrategy().getOMAApiVersion(), messageStoreClient.getPrerenceManager().getUserTelCtn(), messageStoreClient);
        this.TAG += "[" + messageStoreClient.getClientID() + "]";
        this.mIAPICallFlowListener = iAPICallFlowListener;
        initCommonRequestHeaders(this.mStoreClient.getCloudMessageStrategyManager().getStrategy().getContentType(), this.mStoreClient.getCloudMessageStrategyManager().getStrategy().getValidTokenByLine(this.mStoreClient.getPrerenceManager().getUserTelCtn()));
        initGetRequest();
        setCallback(new HttpRequestParams.HttpRequestCallback() {
            public void onComplete(HttpResponseParams httpResponseParams) {
                NotificationChannelList[] notificationChannelListArr;
                if (httpResponseParams.getStatusCode() == 206) {
                    httpResponseParams.setStatusCode(200);
                }
                if (httpResponseParams.getStatusCode() == 200) {
                    try {
                        OMAApiResponseParam oMAApiResponseParam = (OMAApiResponseParam) new Gson().fromJson(httpResponseParams.getDataString(), OMAApiResponseParam.class);
                        if (oMAApiResponseParam == null || (notificationChannelListArr = oMAApiResponseParam.notificationChannelList) == null || notificationChannelListArr.length == 0) {
                            Log.d(CloudMessageGetActiveNotificationChannels.this.TAG, "no active channels, need create channel");
                            CloudMessageGetActiveNotificationChannels.this.mIAPICallFlowListener.onSuccessfulEvent(this, OMASyncEventType.CREATE_NOTIFICATION_CHANNEL.getId(), (Object) null);
                            return;
                        }
                        String str = CloudMessageGetActiveNotificationChannels.this.TAG;
                        Log.i(str, "get active channels, notificationChannelList length = " + notificationChannelListArr.length);
                        int length = notificationChannelListArr.length;
                        int i = 0;
                        for (int i2 = 0; i2 < length; i2++) {
                            URL url = notificationChannelListArr[i2].notificationChannel.resourceURL;
                            String str2 = CloudMessageGetActiveNotificationChannels.this.TAG;
                            IMSLog.s(str2, "get active channels, resourceURL: " + url);
                            i++;
                            if (url != null) {
                                ChannelDeleteData channelDeleteData = new ChannelDeleteData();
                                channelDeleteData.channelUrl = url.toString();
                                channelDeleteData.isNeedRecreateChannel = false;
                                if (i == notificationChannelListArr.length) {
                                    channelDeleteData.isNeedRecreateChannel = true;
                                }
                                CloudMessageGetActiveNotificationChannels.this.mIAPICallFlowListener.onSuccessfulEvent(this, OMASyncEventType.DELETE_NOTIFICATION_CHANNEL.getId(), channelDeleteData);
                            }
                        }
                    } catch (Exception e) {
                        String str3 = CloudMessageGetActiveNotificationChannels.this.TAG;
                        Log.e(str3, "exception occurred " + e.getMessage());
                        e.printStackTrace();
                        CloudMessageGetActiveNotificationChannels.this.mIAPICallFlowListener.onFailedCall(this);
                    }
                } else if (CloudMessageGetActiveNotificationChannels.this.shouldCareAfterResponsePreProcess(iAPICallFlowListener, httpResponseParams, (Object) null, (BufferDBChangeParam) null, Integer.MIN_VALUE)) {
                    iAPICallFlowListener.onFailedCall(CloudMessageGetActiveNotificationChannels.this);
                }
            }

            public void onFail(IOException iOException) {
                String str = CloudMessageGetActiveNotificationChannels.this.TAG;
                Log.e(str, "Http request onFail: " + iOException.getMessage());
                CloudMessageGetActiveNotificationChannels.this.mIAPICallFlowListener.onFailedCall(this);
            }
        });
    }
}
