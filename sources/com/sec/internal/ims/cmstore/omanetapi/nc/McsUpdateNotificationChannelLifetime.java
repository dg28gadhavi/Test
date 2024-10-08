package com.sec.internal.ims.cmstore.omanetapi.nc;

import android.content.Context;
import android.text.TextUtils;
import com.google.gson.Gson;
import com.google.gson.JsonSyntaxException;
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
import com.sec.internal.omanetapi.nc.IndividualNotificationChannelLifetime;
import com.sec.internal.omanetapi.nc.data.McsNotificationChannelLifetime;
import java.io.IOException;

public class McsUpdateNotificationChannelLifetime extends IndividualNotificationChannelLifetime {
    /* access modifiers changed from: private */
    public String TAG = McsUpdateNotificationChannelLifetime.class.getSimpleName();
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

    public McsUpdateNotificationChannelLifetime(IAPICallFlowListener iAPICallFlowListener, final IControllerCommonInterface iControllerCommonInterface, String str, MessageStoreClient messageStoreClient) {
        super(messageStoreClient.getPrerenceManager().getOasisServerRoot(), messageStoreClient.getCloudMessageStrategyManager().getStrategy().getOMAApiVersion(), messageStoreClient.getPrerenceManager().getUserTelCtn(), str, messageStoreClient);
        int clientID = messageStoreClient.getClientID();
        this.mPhoneId = clientID;
        this.TAG += "[" + clientID + "]";
        Context context = messageStoreClient.getContext();
        this.mContext = context;
        this.mIAPICallFlowListener = iAPICallFlowListener;
        this.mControllerInterface = iControllerCommonInterface;
        this.mHttpInterface = this;
        McsNotificationChannelLifetime mcsNotificationChannelLifetime = new McsNotificationChannelLifetime();
        mcsNotificationChannelLifetime.channelLifetime = CmsUtil.getIntGlobalSettings(context, clientID, GlobalSettingsConstants.CMS.CMS_CHANNEL_LIFETIME, 86400);
        IMSLog.i(this.TAG, "notificationChannelLifetime.channelLifetime: " + mcsNotificationChannelLifetime.channelLifetime);
        initMcsCommonRequestHeaders("application/json", this.mStoreClient.getCloudMessageStrategyManager().getStrategy().getAuthorizationBearer());
        initPutRequest(mcsNotificationChannelLifetime, true);
        setCallback(new HttpRequestParams.HttpRequestCallback() {
            public void onComplete(HttpResponseParams httpResponseParams) {
                McsNotificationChannelLifetime mcsNotificationChannelLifetime;
                int statusCode = httpResponseParams.getStatusCode();
                String r1 = McsUpdateNotificationChannelLifetime.this.TAG;
                IMSLog.i(r1, "onComplete: statusCode: " + statusCode);
                String r12 = McsUpdateNotificationChannelLifetime.this.TAG;
                int r2 = McsUpdateNotificationChannelLifetime.this.mPhoneId;
                EventLogHelper.add(r12, r2, "onComplete: statusCode: " + statusCode);
                if (statusCode == 200) {
                    McsUpdateNotificationChannelLifetime.this.mStoreClient.getMcsRetryMapAdapter().remove(McsUpdateNotificationChannelLifetime.this.mHttpInterface);
                    String dataString = httpResponseParams.getDataString();
                    boolean z = true;
                    if (McsUpdateNotificationChannelLifetime.this.mStoreClient.getCloudMessageStrategyManager().getStrategy().isEncrypted()) {
                        String decrypt = McsUpdateNotificationChannelLifetime.this.mStoreClient.getCloudMessageStrategyManager().getStrategy().decrypt(dataString, true);
                        String r22 = McsUpdateNotificationChannelLifetime.this.TAG;
                        IMSLog.s(r22, "onComplete: decryptedData: " + decrypt);
                        if (!TextUtils.isEmpty(decrypt)) {
                            dataString = decrypt;
                        } else {
                            EventLogHelper.add(McsUpdateNotificationChannelLifetime.this.TAG, McsUpdateNotificationChannelLifetime.this.mPhoneId, "decryptedData is empty");
                        }
                    }
                    try {
                        McsOMAApiResponseParam mcsOMAApiResponseParam = (McsOMAApiResponseParam) new Gson().fromJson(dataString, McsOMAApiResponseParam.class);
                        if (mcsOMAApiResponseParam == null || (mcsNotificationChannelLifetime = mcsOMAApiResponseParam.notificationChannelLifetime) == null) {
                            IMSLog.i(McsUpdateNotificationChannelLifetime.this.TAG, "onComplete: there is no notificationChannel");
                            return;
                        }
                        int i = mcsNotificationChannelLifetime.channelLifetime;
                        int intGlobalSettings = CmsUtil.getIntGlobalSettings(McsUpdateNotificationChannelLifetime.this.mContext, McsUpdateNotificationChannelLifetime.this.mPhoneId, GlobalSettingsConstants.CMS.CMS_CHANNEL_EXPIRATION, 1800);
                        int i2 = i - intGlobalSettings;
                        if (i2 <= 0) {
                            i2 = i;
                        }
                        String r3 = McsUpdateNotificationChannelLifetime.this.TAG;
                        IMSLog.i(r3, "onComplete: channelLifetime: " + i + " channelExpiration: " + intGlobalSettings + " delay: " + i2);
                        CloudMessagePreferenceManager prerenceManager = McsUpdateNotificationChannelLifetime.this.mStoreClient.getPrerenceManager();
                        prerenceManager.saveOMAChannelCreateTime(System.currentTimeMillis());
                        prerenceManager.saveOMAChannelLifeTime((long) i);
                        McsUpdateNotificationChannelLifetime.this.mControllerInterface.updateDelay(OMASyncEventType.CHECK_NOTIFICATION_CHANNEL_LIFETIME.getId(), ((long) i2) * 1000);
                        if (i <= 0) {
                            z = false;
                        }
                        McsUpdateNotificationChannelLifetime.this.mIAPICallFlowListener.onGoToEvent(OMASyncEventType.UPDATE_NOTIFICATION_CHANNEL_LIFETIME_FINISHED.getId(), Boolean.valueOf(z));
                        String r9 = McsUpdateNotificationChannelLifetime.this.TAG;
                        int r0 = McsUpdateNotificationChannelLifetime.this.mPhoneId;
                        EventLogHelper.add(r9, r0, "NotificationChannel lifetime updated: " + z);
                        IMSLog.c(LogClass.MCS_NC_LIFETIME_UPDATED, McsUpdateNotificationChannelLifetime.this.mPhoneId + ",NC:LT_UP," + z);
                    } catch (JsonSyntaxException e) {
                        String r8 = McsUpdateNotificationChannelLifetime.this.TAG;
                        IMSLog.i(r8, "onComplete: Exception: " + e.getMessage());
                    }
                } else if (!McsUpdateNotificationChannelLifetime.this.isErrorCodeSupported(statusCode)) {
                } else {
                    if (statusCode == 404) {
                        iControllerCommonInterface.start();
                        return;
                    }
                    McsUpdateNotificationChannelLifetime.this.mStoreClient.getCloudMessageStrategyManager().getStrategy().handleNCCommonError(McsUpdateNotificationChannelLifetime.this.mIAPICallFlowListener, McsUpdateNotificationChannelLifetime.this.mHttpInterface, statusCode, McsUpdateNotificationChannelLifetime.this.checkRetryAfter(httpResponseParams, McsUpdateNotificationChannelLifetime.this.mStoreClient.getMcsRetryMapAdapter().getRetryCount(McsUpdateNotificationChannelLifetime.this.mHttpInterface.getClass().getSimpleName())));
                }
            }

            public void onFail(IOException iOException) {
                String r0 = McsUpdateNotificationChannelLifetime.this.TAG;
                IMSLog.e(r0, " onFail: exception " + iOException.getMessage());
                EventLogHelper.add(McsUpdateNotificationChannelLifetime.this.TAG, McsUpdateNotificationChannelLifetime.this.mPhoneId, "onFail: IOException");
                McsUpdateNotificationChannelLifetime.this.mIAPICallFlowListener.onFailedCall(McsUpdateNotificationChannelLifetime.this.mHttpInterface, String.valueOf(802));
            }
        });
    }
}
