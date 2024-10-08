package com.sec.internal.ims.cmstore.omanetapi.nc;

import android.net.Uri;
import android.util.Log;
import com.google.gson.Gson;
import com.sec.internal.constants.ims.cmstore.omanetapi.OMASyncEventType;
import com.sec.internal.helper.httpclient.HttpRequestParams;
import com.sec.internal.helper.httpclient.HttpResponseParams;
import com.sec.internal.ims.cmstore.MessageStoreClient;
import com.sec.internal.ims.cmstore.params.BufferDBChangeParam;
import com.sec.internal.ims.cmstore.utils.CloudMessagePreferenceManager;
import com.sec.internal.interfaces.ims.cmstore.IAPICallFlowListener;
import com.sec.internal.interfaces.ims.cmstore.IControllerCommonInterface;
import com.sec.internal.omanetapi.common.data.OMAApiResponseParam;
import com.sec.internal.omanetapi.nc.IndividualNotificationChannel;
import com.sec.internal.omanetapi.nc.data.NotificationChannelLifetime;
import java.io.IOException;

public class CloudMessageUpdateNotificationChannelLifeTime extends IndividualNotificationChannel {
    private static final long serialVersionUID = 8158555957984259234L;
    public String TAG = CloudMessageUpdateNotificationChannelLifeTime.class.getSimpleName();
    /* access modifiers changed from: private */
    public final transient IAPICallFlowListener mIAPICallFlowListener;
    /* access modifiers changed from: private */
    public final transient IControllerCommonInterface mIControllerCommonInterface;

    public CloudMessageUpdateNotificationChannelLifeTime(final IAPICallFlowListener iAPICallFlowListener, IControllerCommonInterface iControllerCommonInterface, String str, MessageStoreClient messageStoreClient) {
        super(messageStoreClient.getCloudMessageStrategyManager().getStrategy().getNcHost(), messageStoreClient.getCloudMessageStrategyManager().getStrategy().getOMAApiVersion(), messageStoreClient.getPrerenceManager().getUserTelCtn(), str, messageStoreClient);
        Uri.Builder buildUpon = Uri.parse(this.mBaseUrl).buildUpon();
        buildUpon.appendPath("channelLifetime");
        this.TAG += "[" + messageStoreClient.getClientID() + "]";
        String uri = buildUpon.build().toString();
        this.mBaseUrl = uri;
        Log.i(this.TAG, uri);
        this.mIAPICallFlowListener = iAPICallFlowListener;
        this.mIControllerCommonInterface = iControllerCommonInterface;
        NotificationChannelLifetime notificationChannelLifetime = new NotificationChannelLifetime();
        notificationChannelLifetime.channelLifetime = 86400;
        initCommonRequestHeaders(this.mStoreClient.getCloudMessageStrategyManager().getStrategy().getContentType(), this.mStoreClient.getCloudMessageStrategyManager().getStrategy().getValidTokenByLine(this.mStoreClient.getPrerenceManager().getUserTelCtn()));
        this.mNCRequestHeaderMap.remove("Authorization");
        initPutRequest(notificationChannelLifetime, true);
        setCallback(new HttpRequestParams.HttpRequestCallback() {
            public void onComplete(HttpResponseParams httpResponseParams) {
                if (httpResponseParams.getStatusCode() == 206) {
                    httpResponseParams.setStatusCode(200);
                }
                if (httpResponseParams.getStatusCode() == 200) {
                    try {
                        OMAApiResponseParam oMAApiResponseParam = (OMAApiResponseParam) new Gson().fromJson(httpResponseParams.getDataString(), OMAApiResponseParam.class);
                        if (oMAApiResponseParam == null) {
                            CloudMessageUpdateNotificationChannelLifeTime.this.mIAPICallFlowListener.onFailedCall(this);
                            return;
                        }
                        long j = oMAApiResponseParam.notificationChannelLifetime.channelLifetime;
                        String str = CloudMessageUpdateNotificationChannelLifeTime.this.TAG;
                        Log.i(str, "channelLifeTime: " + j);
                        CloudMessagePreferenceManager prerenceManager = CloudMessageUpdateNotificationChannelLifeTime.this.mStoreClient.getPrerenceManager();
                        prerenceManager.saveOMAChannelCreateTime(System.currentTimeMillis());
                        prerenceManager.saveOMAChannelLifeTime(j);
                        CloudMessageUpdateNotificationChannelLifeTime.this.mIControllerCommonInterface.updateDelay(OMASyncEventType.UPDATE_NOTIFICATION_CHANNEL_LIFETIME.getId(), (j - 900) * 1000);
                    } catch (Exception e) {
                        String str2 = CloudMessageUpdateNotificationChannelLifeTime.this.TAG;
                        Log.e(str2, "exception occurred " + e.getMessage());
                        e.printStackTrace();
                        CloudMessageUpdateNotificationChannelLifeTime.this.mIAPICallFlowListener.onFailedCall(this);
                        return;
                    }
                }
                CloudMessageUpdateNotificationChannelLifeTime.this.shouldCareAfterResponsePreProcess(iAPICallFlowListener, httpResponseParams, (Object) null, (BufferDBChangeParam) null, Integer.MIN_VALUE);
            }

            public void onFail(IOException iOException) {
                String str = CloudMessageUpdateNotificationChannelLifeTime.this.TAG;
                Log.e(str, "Http request onFail: " + iOException.getMessage());
                CloudMessageUpdateNotificationChannelLifeTime.this.mIAPICallFlowListener.onFailedCall(this);
            }
        });
    }
}
