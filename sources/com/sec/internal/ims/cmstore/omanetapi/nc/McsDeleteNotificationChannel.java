package com.sec.internal.ims.cmstore.omanetapi.nc;

import android.text.TextUtils;
import com.sec.internal.constants.ims.cmstore.omanetapi.OMASyncEventType;
import com.sec.internal.helper.httpclient.HttpRequestParams;
import com.sec.internal.helper.httpclient.HttpResponseParams;
import com.sec.internal.ims.cmstore.MessageStoreClient;
import com.sec.internal.ims.cmstore.helper.EventLogHelper;
import com.sec.internal.ims.cmstore.utils.CloudMessagePreferenceManager;
import com.sec.internal.imscr.LogClass;
import com.sec.internal.interfaces.ims.cmstore.IAPICallFlowListener;
import com.sec.internal.interfaces.ims.cmstore.IHttpAPICommonInterface;
import com.sec.internal.log.IMSLog;
import com.sec.internal.omanetapi.nc.IndividualNotificationChannel;
import java.io.IOException;

public class McsDeleteNotificationChannel extends IndividualNotificationChannel {
    /* access modifiers changed from: private */
    public String TAG = McsDeleteNotificationChannel.class.getSimpleName();
    /* access modifiers changed from: private */
    public final IHttpAPICommonInterface mHttpInterface;
    /* access modifiers changed from: private */
    public final IAPICallFlowListener mIAPICallFlowListener;

    public McsDeleteNotificationChannel(IAPICallFlowListener iAPICallFlowListener, MessageStoreClient messageStoreClient, final String str, final boolean z, String str2, String str3) {
        super(messageStoreClient.getPrerenceManager().getOasisServerRoot(), messageStoreClient.getCloudMessageStrategyManager().getStrategy().getOMAApiVersion(), messageStoreClient.getPrerenceManager().getUserTelCtn(), str, messageStoreClient);
        this.mPhoneId = messageStoreClient.getClientID();
        this.TAG += "[" + this.mPhoneId + "]";
        this.mIAPICallFlowListener = iAPICallFlowListener;
        this.mHttpInterface = this;
        if (TextUtils.isEmpty(messageStoreClient.getPrerenceManager().getUserTelCtn())) {
            this.mBaseUrl = str3;
            IMSLog.i(this.TAG, "mBaseUrl from OMA resUrl:" + IMSLog.numberChecker(this.mBaseUrl));
        }
        initMcsCommonRequestHeaders("application/json", this.mStoreClient.getCloudMessageStrategyManager().getStrategy().getAuthorizationBearer());
        initDeleteRequest(str2);
        setCallback(new HttpRequestParams.HttpRequestCallback() {
            public void onComplete(HttpResponseParams httpResponseParams) {
                int statusCode = httpResponseParams.getStatusCode();
                String r1 = McsDeleteNotificationChannel.this.TAG;
                IMSLog.i(r1, "onComplete: statusCode: " + statusCode);
                String r12 = McsDeleteNotificationChannel.this.TAG;
                int access$000 = McsDeleteNotificationChannel.this.mPhoneId;
                EventLogHelper.add(r12, access$000, "onComplete: statusCode: " + statusCode + ", isNeedRecreateChannel: " + z);
                if (statusCode == 200 || statusCode == 204) {
                    McsDeleteNotificationChannel.this.mStoreClient.getMcsRetryMapAdapter().remove(McsDeleteNotificationChannel.this.mHttpInterface);
                    McsDeleteNotificationChannel.this.updateChannelData(str);
                    if (z) {
                        IMSLog.i(McsDeleteNotificationChannel.this.TAG, "onComplete: notificationChannel is deleted: try to create notificationChannel");
                        McsDeleteNotificationChannel.this.mIAPICallFlowListener.onSuccessfulEvent(McsDeleteNotificationChannel.this.mHttpInterface, OMASyncEventType.CREATE_NOTIFICATION_CHANNEL.getId(), (Object) null);
                    } else {
                        IMSLog.i(McsDeleteNotificationChannel.this.TAG, "onComplete: notificationChannel is deleted");
                        McsDeleteNotificationChannel.this.mIAPICallFlowListener.onSuccessfulCall(McsDeleteNotificationChannel.this.mHttpInterface);
                    }
                    IMSLog.c(LogClass.MCS_NC_DELETED, McsDeleteNotificationChannel.this.mPhoneId + ",NC:DEL");
                } else if (McsDeleteNotificationChannel.this.isErrorCodeSupported(statusCode)) {
                    McsDeleteNotificationChannel.this.mStoreClient.getCloudMessageStrategyManager().getStrategy().handleNCCommonError(McsDeleteNotificationChannel.this.mIAPICallFlowListener, McsDeleteNotificationChannel.this.mHttpInterface, statusCode, McsDeleteNotificationChannel.this.checkRetryAfter(httpResponseParams, McsDeleteNotificationChannel.this.mStoreClient.getMcsRetryMapAdapter().getRetryCount(McsDeleteNotificationChannel.this.mHttpInterface.getClass().getSimpleName())));
                }
            }

            public void onFail(IOException iOException) {
                String r0 = McsDeleteNotificationChannel.this.TAG;
                IMSLog.e(r0, " onFail: exception " + iOException.getMessage());
                EventLogHelper.add(McsDeleteNotificationChannel.this.TAG, McsDeleteNotificationChannel.this.mPhoneId, "onFail: IOException");
            }
        });
    }

    /* access modifiers changed from: private */
    public void updateChannelData(String str) {
        CloudMessagePreferenceManager prerenceManager = this.mStoreClient.getPrerenceManager();
        String oMAChannelResURL = prerenceManager.getOMAChannelResURL();
        String str2 = this.TAG;
        IMSLog.s(str2, "updateChannelData: resUrl: " + oMAChannelResURL);
        if (!TextUtils.isEmpty(oMAChannelResURL)) {
            String substring = oMAChannelResURL.substring(oMAChannelResURL.lastIndexOf("/") + 1);
            String str3 = this.TAG;
            IMSLog.s(str3, "updateChannelData: channelId: " + str + " currentChannelId: " + substring);
            if (!TextUtils.isEmpty(str) && str.equalsIgnoreCase(substring)) {
                prerenceManager.saveOMAChannelResURL("");
                prerenceManager.saveOMACallBackURL("");
                prerenceManager.saveOMAChannelLifeTime(0);
                prerenceManager.saveOMAChannelCreateTime(0);
                prerenceManager.clearOMASubscriptionChannelDuration();
                prerenceManager.clearOMASubscriptionTime();
            }
        }
    }
}
