package com.sec.internal.ims.cmstore.omanetapi.nc;

import android.text.TextUtils;
import com.google.gson.Gson;
import com.google.gson.JsonSyntaxException;
import com.sec.internal.constants.ims.cmstore.McsConstants;
import com.sec.internal.constants.ims.cmstore.omanetapi.OMASyncEventType;
import com.sec.internal.helper.httpclient.HttpRequestParams;
import com.sec.internal.helper.httpclient.HttpResponseParams;
import com.sec.internal.ims.cmstore.MessageStoreClient;
import com.sec.internal.ims.cmstore.helper.EventLogHelper;
import com.sec.internal.interfaces.ims.cmstore.IAPICallFlowListener;
import com.sec.internal.interfaces.ims.cmstore.IControllerCommonInterface;
import com.sec.internal.interfaces.ims.cmstore.IHttpAPICommonInterface;
import com.sec.internal.log.IMSLog;
import com.sec.internal.omanetapi.common.data.McsOMAApiResponseParam;
import com.sec.internal.omanetapi.nc.IndividualNotificationChannel;
import com.sec.internal.omanetapi.nc.data.ChannelDeleteData;
import com.sec.internal.omanetapi.nc.data.McsNotificationChannel;
import java.io.IOException;

public class McsGetNotificationChannelInfo extends IndividualNotificationChannel {
    /* access modifiers changed from: private */
    public String TAG = McsGetNotificationChannelInfo.class.getSimpleName();
    /* access modifiers changed from: private */
    public final IHttpAPICommonInterface mHttpInterface;
    /* access modifiers changed from: private */
    public final IAPICallFlowListener mIAPICallFlowListener;
    /* access modifiers changed from: private */
    public final int mPhoneId;

    public McsGetNotificationChannelInfo(IAPICallFlowListener iAPICallFlowListener, IControllerCommonInterface iControllerCommonInterface, String str, MessageStoreClient messageStoreClient) {
        super(messageStoreClient.getPrerenceManager().getOasisServerRoot(), messageStoreClient.getCloudMessageStrategyManager().getStrategy().getOMAApiVersion(), messageStoreClient.getPrerenceManager().getUserTelCtn(), str, messageStoreClient);
        int clientID = messageStoreClient.getClientID();
        this.mPhoneId = clientID;
        this.TAG += "[" + clientID + "]";
        this.mIAPICallFlowListener = iAPICallFlowListener;
        this.mHttpInterface = this;
        initMcsCommonRequestHeaders("application/json", this.mStoreClient.getCloudMessageStrategyManager().getStrategy().getAuthorizationBearer());
        initGetRequest();
        setCallback(new HttpRequestParams.HttpRequestCallback() {
            public void onComplete(HttpResponseParams httpResponseParams) {
                McsNotificationChannel mcsNotificationChannel;
                int statusCode = httpResponseParams.getStatusCode();
                String r1 = McsGetNotificationChannelInfo.this.TAG;
                IMSLog.i(r1, "onComplete: statusCode: " + statusCode);
                String r12 = McsGetNotificationChannelInfo.this.TAG;
                int r2 = McsGetNotificationChannelInfo.this.mPhoneId;
                EventLogHelper.add(r12, r2, "onComplete: statusCode: " + statusCode);
                if (statusCode == 200) {
                    McsGetNotificationChannelInfo.this.mStoreClient.getMcsRetryMapAdapter().remove(McsGetNotificationChannelInfo.this.mHttpInterface);
                    String dataString = httpResponseParams.getDataString();
                    if (McsGetNotificationChannelInfo.this.mStoreClient.getCloudMessageStrategyManager().getStrategy().isEncrypted()) {
                        String decrypt = McsGetNotificationChannelInfo.this.mStoreClient.getCloudMessageStrategyManager().getStrategy().decrypt(dataString, true);
                        String r13 = McsGetNotificationChannelInfo.this.TAG;
                        IMSLog.s(r13, "onComplete: decryptedData: " + decrypt);
                        if (!TextUtils.isEmpty(decrypt)) {
                            dataString = decrypt;
                        } else {
                            EventLogHelper.add(McsGetNotificationChannelInfo.this.TAG, McsGetNotificationChannelInfo.this.mPhoneId, "decryptedData is empty");
                        }
                    }
                    try {
                        McsOMAApiResponseParam mcsOMAApiResponseParam = (McsOMAApiResponseParam) new Gson().fromJson(dataString, McsOMAApiResponseParam.class);
                        if (mcsOMAApiResponseParam == null || (mcsNotificationChannel = mcsOMAApiResponseParam.notificationChannel) == null) {
                            IMSLog.i(McsGetNotificationChannelInfo.this.TAG, "onComplete: there is no notificationChannel");
                            return;
                        }
                        String str = mcsNotificationChannel.resourceURL;
                        String r0 = McsGetNotificationChannelInfo.this.TAG;
                        IMSLog.s(r0, "onComplete: notificationChannel resUrl: " + str);
                        ChannelDeleteData channelDeleteData = new ChannelDeleteData();
                        channelDeleteData.channelUrl = str;
                        channelDeleteData.isNeedRecreateChannel = false;
                        channelDeleteData.deleteReason = McsConstants.ChannelDeleteReason.NORMAL;
                        McsGetNotificationChannelInfo.this.mIAPICallFlowListener.onSuccessfulEvent(McsGetNotificationChannelInfo.this.mHttpInterface, OMASyncEventType.DELETE_NOTIFICATION_CHANNEL.getId(), channelDeleteData);
                    } catch (JsonSyntaxException e) {
                        String r5 = McsGetNotificationChannelInfo.this.TAG;
                        IMSLog.i(r5, "onComplete: Exception: " + e.getMessage());
                    }
                } else if (McsGetNotificationChannelInfo.this.isErrorCodeSupported(statusCode)) {
                    if (statusCode == 404) {
                        McsGetNotificationChannelInfo.this.mIAPICallFlowListener.onGoToEvent(OMASyncEventType.CREATE_NOTIFICATION_CHANNEL.getId(), (Object) null);
                    }
                    McsGetNotificationChannelInfo.this.mStoreClient.getCloudMessageStrategyManager().getStrategy().handleNCCommonError(McsGetNotificationChannelInfo.this.mIAPICallFlowListener, McsGetNotificationChannelInfo.this.mHttpInterface, statusCode, McsGetNotificationChannelInfo.this.checkRetryAfter(httpResponseParams, McsGetNotificationChannelInfo.this.mStoreClient.getMcsRetryMapAdapter().getRetryCount(McsGetNotificationChannelInfo.this.mHttpInterface.getClass().getSimpleName())));
                }
            }

            public void onFail(IOException iOException) {
                String r0 = McsGetNotificationChannelInfo.this.TAG;
                IMSLog.e(r0, " onFail: exception " + iOException.getMessage());
                EventLogHelper.add(McsGetNotificationChannelInfo.this.TAG, McsGetNotificationChannelInfo.this.mPhoneId, "onFail: IOException");
                McsGetNotificationChannelInfo.this.mIAPICallFlowListener.onFailedCall(McsGetNotificationChannelInfo.this.mHttpInterface, String.valueOf(802));
            }
        });
    }
}
