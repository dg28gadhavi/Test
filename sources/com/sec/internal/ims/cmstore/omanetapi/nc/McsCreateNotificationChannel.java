package com.sec.internal.ims.cmstore.omanetapi.nc;

import android.content.Context;
import android.text.TextUtils;
import com.google.gson.Gson;
import com.sec.internal.constants.ims.cmstore.omanetapi.OMASyncEventType;
import com.sec.internal.constants.ims.settings.GlobalSettingsConstants;
import com.sec.internal.helper.httpclient.HttpRequestParams;
import com.sec.internal.helper.httpclient.HttpResponseParams;
import com.sec.internal.ims.cmstore.MessageStoreClient;
import com.sec.internal.ims.cmstore.helper.EventLogHelper;
import com.sec.internal.ims.cmstore.utils.CloudMessagePreferenceManager;
import com.sec.internal.ims.cmstore.utils.CmsUtil;
import com.sec.internal.imscr.LogClass;
import com.sec.internal.interfaces.ims.cmstore.IAPICallFlowListener;
import com.sec.internal.interfaces.ims.cmstore.IControllerCommonInterface;
import com.sec.internal.interfaces.ims.cmstore.IHttpAPICommonInterface;
import com.sec.internal.log.IMSLog;
import com.sec.internal.omanetapi.common.data.McsOMAApiResponseParam;
import com.sec.internal.omanetapi.nc.NotificationChannels;
import com.sec.internal.omanetapi.nc.data.McsChannelData;
import com.sec.internal.omanetapi.nc.data.McsLargeDataPolling;
import com.sec.internal.omanetapi.nc.data.McsNotificationChannel;
import java.io.IOException;

public class McsCreateNotificationChannel extends NotificationChannels {
    /* access modifiers changed from: private */
    public String TAG = McsCreateNotificationChannel.class.getSimpleName();
    /* access modifiers changed from: private */
    public final Context mContext;
    /* access modifiers changed from: private */
    public final IControllerCommonInterface mControllerInterface;
    /* access modifiers changed from: private */
    public final IHttpAPICommonInterface mHttpInterface;
    /* access modifiers changed from: private */
    public final IAPICallFlowListener mIAPICallFlowListener;
    /* access modifiers changed from: private */
    public final int mPhoneId;

    public McsCreateNotificationChannel(IAPICallFlowListener iAPICallFlowListener, IControllerCommonInterface iControllerCommonInterface, String str, MessageStoreClient messageStoreClient) {
        super(messageStoreClient.getPrerenceManager().getOasisServerRoot(), messageStoreClient.getCloudMessageStrategyManager().getStrategy().getOMAApiVersion(), messageStoreClient.getPrerenceManager().getUserTelCtn(), messageStoreClient);
        int clientID = messageStoreClient.getClientID();
        this.mPhoneId = clientID;
        this.TAG += "[" + clientID + "]";
        Context context = messageStoreClient.getContext();
        this.mContext = context;
        this.mIAPICallFlowListener = iAPICallFlowListener;
        this.mControllerInterface = iControllerCommonInterface;
        this.mHttpInterface = this;
        McsNotificationChannel mcsNotificationChannel = new McsNotificationChannel();
        mcsNotificationChannel.channelType = CmsUtil.getStringGlobalSettings(context, clientID, GlobalSettingsConstants.CMS.CMS_CHANNEL_TYPE, "NativeChannel");
        McsChannelData mcsChannelData = new McsChannelData();
        mcsChannelData.channelSubType = CmsUtil.getStringGlobalSettings(context, clientID, GlobalSettingsConstants.CMS.CMS_CHANNEL_SUB_TYPE, "FCM");
        mcsChannelData.channelSubTypeVersion = CmsUtil.getStringGlobalSettings(context, clientID, GlobalSettingsConstants.CMS.CMS_CHANNEL_SUB_TYPE_VERSION, "1.0");
        mcsChannelData.maxNotifications = CmsUtil.getIntGlobalSettings(context, clientID, GlobalSettingsConstants.CMS.CMS_MAX_NOTIFICATIONS, 3);
        mcsChannelData.registrationToken = str;
        McsLargeDataPolling mcsLargeDataPolling = new McsLargeDataPolling();
        mcsLargeDataPolling.maxPollingNotifications = CmsUtil.getIntGlobalSettings(context, clientID, GlobalSettingsConstants.CMS.CMS_MAX_POLLING_NOTIFICATIONS, 20);
        mcsLargeDataPolling.pollingEnabled = CmsUtil.getBooleanGlobalSettings(context, clientID, GlobalSettingsConstants.CMS.CMS_POLLING_ENABLED, true);
        mcsChannelData.largeDataPolling = mcsLargeDataPolling;
        mcsNotificationChannel.channelData = mcsChannelData;
        mcsNotificationChannel.channelLifetime = CmsUtil.getIntGlobalSettings(context, clientID, GlobalSettingsConstants.CMS.CMS_CHANNEL_LIFETIME, 86400);
        mcsNotificationChannel.toString();
        initMcsCommonRequestHeaders("application/json", this.mStoreClient.getCloudMessageStrategyManager().getStrategy().getAuthorizationBearer());
        initPostRequest(mcsNotificationChannel, true);
        setCallback(new HttpRequestParams.HttpRequestCallback() {
            public void onComplete(HttpResponseParams httpResponseParams) {
                McsNotificationChannel mcsNotificationChannel;
                int statusCode = httpResponseParams.getStatusCode();
                String r1 = McsCreateNotificationChannel.this.TAG;
                IMSLog.i(r1, "onComplete: statusCode: " + statusCode);
                String r12 = McsCreateNotificationChannel.this.TAG;
                int r2 = McsCreateNotificationChannel.this.mPhoneId;
                EventLogHelper.add(r12, r2, "onComplete: statusCode: " + statusCode);
                if (statusCode == 200 || statusCode == 201) {
                    McsCreateNotificationChannel.this.mStoreClient.getMcsRetryMapAdapter().remove(McsCreateNotificationChannel.this.mHttpInterface);
                    String dataString = httpResponseParams.getDataString();
                    if (McsCreateNotificationChannel.this.mStoreClient.getCloudMessageStrategyManager().getStrategy().isEncrypted()) {
                        String decrypt = McsCreateNotificationChannel.this.mStoreClient.getCloudMessageStrategyManager().getStrategy().decrypt(dataString, true);
                        String r13 = McsCreateNotificationChannel.this.TAG;
                        IMSLog.s(r13, "onComplete: decryptedData: " + decrypt);
                        if (!TextUtils.isEmpty(decrypt)) {
                            dataString = decrypt;
                        } else {
                            EventLogHelper.add(McsCreateNotificationChannel.this.TAG, McsCreateNotificationChannel.this.mPhoneId, "decryptedData is empty");
                        }
                    }
                    try {
                        McsOMAApiResponseParam mcsOMAApiResponseParam = (McsOMAApiResponseParam) new Gson().fromJson(dataString, McsOMAApiResponseParam.class);
                        if (mcsOMAApiResponseParam == null || (mcsNotificationChannel = mcsOMAApiResponseParam.notificationChannel) == null) {
                            IMSLog.i(McsCreateNotificationChannel.this.TAG, "onComplete: there is no notificationChannel");
                            return;
                        }
                        CloudMessagePreferenceManager prerenceManager = McsCreateNotificationChannel.this.mStoreClient.getPrerenceManager();
                        String str = mcsNotificationChannel.resourceURL;
                        String str2 = "";
                        if (str == null) {
                            str = str2;
                        }
                        prerenceManager.saveOMAChannelResURL(str);
                        String str3 = mcsNotificationChannel.callbackURL;
                        if (str3 != null) {
                            str2 = str3;
                        }
                        String r3 = McsCreateNotificationChannel.this.TAG;
                        IMSLog.s(r3, "onComplete: callbackUrl: " + str2 + " resUrl:" + str);
                        prerenceManager.saveOMACallBackURL(str2);
                        int i = mcsNotificationChannel.channelLifetime;
                        int intGlobalSettings = CmsUtil.getIntGlobalSettings(McsCreateNotificationChannel.this.mContext, McsCreateNotificationChannel.this.mPhoneId, GlobalSettingsConstants.CMS.CMS_CHANNEL_EXPIRATION, 1800);
                        int i2 = i - intGlobalSettings;
                        if (i2 <= 0) {
                            i2 = i;
                        }
                        String r32 = McsCreateNotificationChannel.this.TAG;
                        IMSLog.i(r32, "onComplete: channelLifetime: " + i + " channelExpiration: " + intGlobalSettings + " delay: " + i2);
                        prerenceManager.saveOMAChannelLifeTime((long) i);
                        prerenceManager.saveOMAChannelCreateTime(System.currentTimeMillis());
                        prerenceManager.clearOMASubscriptionChannelDuration();
                        prerenceManager.clearOMASubscriptionTime();
                        McsCreateNotificationChannel.this.mControllerInterface.updateDelay(OMASyncEventType.CHECK_NOTIFICATION_CHANNEL_LIFETIME.getId(), ((long) i2) * 1000);
                        McsCreateNotificationChannel.this.mIAPICallFlowListener.onSuccessfulEvent(McsCreateNotificationChannel.this.mHttpInterface, OMASyncEventType.CREATE_NOTIFICATION_CHANNEL_FINISHED.getId(), (Object) null);
                        EventLogHelper.add(McsCreateNotificationChannel.this.TAG, McsCreateNotificationChannel.this.mPhoneId, "NotificationChannel created");
                        IMSLog.c(LogClass.MCS_NC_CREATED, McsCreateNotificationChannel.this.mPhoneId + ",NC:CRT");
                    } catch (Exception e) {
                        String r6 = McsCreateNotificationChannel.this.TAG;
                        IMSLog.i(r6, "onComplete: Exception: " + e.getMessage());
                    }
                } else if (McsCreateNotificationChannel.this.isErrorCodeSupported(statusCode)) {
                    McsCreateNotificationChannel.this.mStoreClient.getCloudMessageStrategyManager().getStrategy().handleNCCommonError(McsCreateNotificationChannel.this.mIAPICallFlowListener, McsCreateNotificationChannel.this.mHttpInterface, statusCode, McsCreateNotificationChannel.this.checkRetryAfter(httpResponseParams, McsCreateNotificationChannel.this.mStoreClient.getMcsRetryMapAdapter().getRetryCount(McsCreateNotificationChannel.this.mHttpInterface.getClass().getSimpleName())));
                }
            }

            public void onFail(IOException iOException) {
                String r0 = McsCreateNotificationChannel.this.TAG;
                IMSLog.e(r0, " onFail: exception " + iOException.getMessage());
                EventLogHelper.add(McsCreateNotificationChannel.this.TAG, McsCreateNotificationChannel.this.mPhoneId, "onFail: IOException");
                McsCreateNotificationChannel.this.mIAPICallFlowListener.onFailedCall(McsCreateNotificationChannel.this.mHttpInterface, String.valueOf(802));
            }
        });
    }
}
