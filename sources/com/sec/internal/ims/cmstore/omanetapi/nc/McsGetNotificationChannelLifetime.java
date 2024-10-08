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
import com.sec.internal.ims.cmstore.utils.CmsUtil;
import com.sec.internal.interfaces.ims.cmstore.IAPICallFlowListener;
import com.sec.internal.interfaces.ims.cmstore.IControllerCommonInterface;
import com.sec.internal.interfaces.ims.cmstore.IHttpAPICommonInterface;
import com.sec.internal.log.IMSLog;
import com.sec.internal.omanetapi.common.data.McsOMAApiResponseParam;
import com.sec.internal.omanetapi.nc.IndividualNotificationChannelLifetime;
import java.io.IOException;

public class McsGetNotificationChannelLifetime extends IndividualNotificationChannelLifetime {
    /* access modifiers changed from: private */
    public String TAG = McsGetNotificationChannelLifetime.class.getSimpleName();
    /* access modifiers changed from: private */
    public final Context mContext;
    /* access modifiers changed from: private */
    public final IHttpAPICommonInterface mHttpInterface;
    /* access modifiers changed from: private */
    public final IAPICallFlowListener mIAPICallFlowListener;
    /* access modifiers changed from: private */
    public final int mPhoneId;

    public McsGetNotificationChannelLifetime(IAPICallFlowListener iAPICallFlowListener, final IControllerCommonInterface iControllerCommonInterface, String str, MessageStoreClient messageStoreClient) {
        super(messageStoreClient.getPrerenceManager().getOasisServerRoot(), messageStoreClient.getCloudMessageStrategyManager().getStrategy().getOMAApiVersion(), messageStoreClient.getPrerenceManager().getUserTelCtn(), str, messageStoreClient);
        int clientID = messageStoreClient.getClientID();
        this.mPhoneId = clientID;
        this.TAG += "[" + clientID + "]";
        this.mContext = messageStoreClient.getContext();
        this.mIAPICallFlowListener = iAPICallFlowListener;
        this.mHttpInterface = this;
        initMcsCommonRequestHeaders("application/json", this.mStoreClient.getCloudMessageStrategyManager().getStrategy().getAuthorizationBearer());
        initGetRequest();
        setCallback(new HttpRequestParams.HttpRequestCallback() {
            public void onComplete(HttpResponseParams httpResponseParams) {
                int statusCode = httpResponseParams.getStatusCode();
                String r1 = McsGetNotificationChannelLifetime.this.TAG;
                IMSLog.i(r1, "onComplete: statusCode: " + statusCode);
                String r12 = McsGetNotificationChannelLifetime.this.TAG;
                int r2 = McsGetNotificationChannelLifetime.this.mPhoneId;
                EventLogHelper.add(r12, r2, "onComplete: statusCode: " + statusCode);
                if (statusCode == 200) {
                    McsGetNotificationChannelLifetime.this.mStoreClient.getMcsRetryMapAdapter().remove(McsGetNotificationChannelLifetime.this.mHttpInterface);
                    String dataString = httpResponseParams.getDataString();
                    if (McsGetNotificationChannelLifetime.this.mStoreClient.getCloudMessageStrategyManager().getStrategy().isEncrypted()) {
                        String decrypt = McsGetNotificationChannelLifetime.this.mStoreClient.getCloudMessageStrategyManager().getStrategy().decrypt(dataString, true);
                        String r13 = McsGetNotificationChannelLifetime.this.TAG;
                        IMSLog.s(r13, "onComplete: decryptedData: " + decrypt);
                        if (!TextUtils.isEmpty(decrypt)) {
                            dataString = decrypt;
                        } else {
                            EventLogHelper.add(McsGetNotificationChannelLifetime.this.TAG, McsGetNotificationChannelLifetime.this.mPhoneId, "decryptedData is empty");
                        }
                    }
                    try {
                        McsOMAApiResponseParam mcsOMAApiResponseParam = (McsOMAApiResponseParam) new Gson().fromJson(dataString, McsOMAApiResponseParam.class);
                        if (mcsOMAApiResponseParam == null || mcsOMAApiResponseParam.notificationChannelLifetime == null) {
                            IMSLog.i(McsGetNotificationChannelLifetime.this.TAG, "onComplete: there is no notificationChannelLifetime");
                            return;
                        }
                        long oMAChannelCreateTime = McsGetNotificationChannelLifetime.this.mStoreClient.getPrerenceManager().getOMAChannelCreateTime();
                        int i = mcsOMAApiResponseParam.notificationChannelLifetime.channelLifetime;
                        long currentTimeMillis = System.currentTimeMillis();
                        long j = (((long) i) + oMAChannelCreateTime) - currentTimeMillis;
                        int intGlobalSettings = CmsUtil.getIntGlobalSettings(McsGetNotificationChannelLifetime.this.mContext, McsGetNotificationChannelLifetime.this.mPhoneId, GlobalSettingsConstants.CMS.CMS_CHANNEL_EXPIRATION, 1800);
                        String r7 = McsGetNotificationChannelLifetime.this.TAG;
                        IMSLog.i(r7, "onComplete: channelCreateTime: " + oMAChannelCreateTime + " channelLifetime: " + i + " currentTime: " + currentTimeMillis + " remainTime: " + j + " channelExpiration: " + intGlobalSettings);
                        if (j <= ((long) intGlobalSettings)) {
                            IMSLog.i(McsGetNotificationChannelLifetime.this.TAG, "onComplete: send UPDATE_NOTIFICATION_CHANNEL_LIFETIME");
                            McsGetNotificationChannelLifetime.this.mIAPICallFlowListener.onSuccessfulEvent(McsGetNotificationChannelLifetime.this.mHttpInterface, OMASyncEventType.UPDATE_NOTIFICATION_CHANNEL_LIFETIME.getId(), (Object) null);
                        }
                    } catch (Exception e) {
                        String r10 = McsGetNotificationChannelLifetime.this.TAG;
                        IMSLog.i(r10, "onComplete: Exception: " + e.getMessage());
                    }
                } else if (!McsGetNotificationChannelLifetime.this.isErrorCodeSupported(statusCode)) {
                } else {
                    if (statusCode == 404) {
                        iControllerCommonInterface.start();
                        return;
                    }
                    McsGetNotificationChannelLifetime.this.mStoreClient.getCloudMessageStrategyManager().getStrategy().handleNCCommonError(McsGetNotificationChannelLifetime.this.mIAPICallFlowListener, McsGetNotificationChannelLifetime.this.mHttpInterface, statusCode, McsGetNotificationChannelLifetime.this.checkRetryAfter(httpResponseParams, McsGetNotificationChannelLifetime.this.mStoreClient.getMcsRetryMapAdapter().getRetryCount(McsGetNotificationChannelLifetime.this.mHttpInterface.getClass().getSimpleName())));
                }
            }

            public void onFail(IOException iOException) {
                String r0 = McsGetNotificationChannelLifetime.this.TAG;
                IMSLog.e(r0, " onFail: exception " + iOException.getMessage());
                EventLogHelper.add(McsGetNotificationChannelLifetime.this.TAG, McsGetNotificationChannelLifetime.this.mPhoneId, "onFail: IOException");
                McsGetNotificationChannelLifetime.this.mIAPICallFlowListener.onFailedCall(McsGetNotificationChannelLifetime.this.mHttpInterface, String.valueOf(802));
            }
        });
    }
}
