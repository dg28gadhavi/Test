package com.sec.internal.ims.cmstore.omanetapi.nc;

import android.text.TextUtils;
import android.util.Log;
import com.google.gson.Gson;
import com.sec.internal.constants.ims.cmstore.omanetapi.OMASyncEventType;
import com.sec.internal.constants.ims.entitilement.SoftphoneNamespaces;
import com.sec.internal.helper.httpclient.HttpRequestParams;
import com.sec.internal.helper.httpclient.HttpResponseParams;
import com.sec.internal.ims.cmstore.MessageStoreClient;
import com.sec.internal.ims.cmstore.helper.EventLogHelper;
import com.sec.internal.ims.cmstore.params.ParamOMAresponseforBufDB;
import com.sec.internal.ims.cmstore.utils.McsNotificationListContainer;
import com.sec.internal.ims.cmstore.utils.Util;
import com.sec.internal.interfaces.ims.cmstore.IAPICallFlowListener;
import com.sec.internal.interfaces.ims.cmstore.IControllerCommonInterface;
import com.sec.internal.interfaces.ims.cmstore.IHttpAPICommonInterface;
import com.sec.internal.log.IMSLog;
import com.sec.internal.omanetapi.common.data.McsOMAApiResponseParam;
import com.sec.internal.omanetapi.nc.NotificationList;
import com.sec.internal.omanetapi.nc.data.LongPollingRequestParameters;
import com.sec.internal.omanetapi.nms.data.NmsEventList;
import java.io.IOException;
import java.util.Map;

public class McsCreateLargeDataPolling extends NotificationList {
    private static final long serialVersionUID = 1;
    /* access modifiers changed from: private */
    public String TAG = McsCreateLargeDataPolling.class.getSimpleName();
    /* access modifiers changed from: private */
    public final IHttpAPICommonInterface mHttpInterface;
    /* access modifiers changed from: private */
    public final transient IAPICallFlowListener mIAPICallFlowListener;
    /* access modifiers changed from: private */
    public final transient IControllerCommonInterface mIControllerCommonInterface;
    /* access modifiers changed from: private */
    public final int mPhoneId;

    public McsCreateLargeDataPolling(IAPICallFlowListener iAPICallFlowListener, IControllerCommonInterface iControllerCommonInterface, final String str, MessageStoreClient messageStoreClient) {
        super(str, messageStoreClient);
        int clientID = messageStoreClient.getClientID();
        this.mPhoneId = clientID;
        this.TAG += "[" + clientID + "]";
        this.mIControllerCommonInterface = iControllerCommonInterface;
        this.mIAPICallFlowListener = iAPICallFlowListener;
        this.mHttpInterface = this;
        initMcsCommonRequestHeaders("application/json", this.mStoreClient.getCloudMessageStrategyManager().getStrategy().getAuthorizationBearer());
        initPostRequest((LongPollingRequestParameters) null, true);
        setCallback(new HttpRequestParams.HttpRequestCallback() {
            public void onComplete(HttpResponseParams httpResponseParams) {
                NmsEventList nmsEventList;
                int statusCode = httpResponseParams.getStatusCode();
                String r2 = McsCreateLargeDataPolling.this.TAG;
                IMSLog.i(r2, "onComplete: statusCode: " + statusCode);
                if (statusCode == 201 || statusCode == 200) {
                    HttpResponseParams httpResponseParams2 = httpResponseParams;
                    McsCreateLargeDataPolling.this.mStoreClient.getMcsRetryMapAdapter().remove(McsCreateLargeDataPolling.this.mHttpInterface);
                    String dataString = httpResponseParams.getDataString();
                    if (McsCreateLargeDataPolling.this.mStoreClient.getCloudMessageStrategyManager().getStrategy().isEncrypted()) {
                        String decrypt = McsCreateLargeDataPolling.this.mStoreClient.getCloudMessageStrategyManager().getStrategy().decrypt(dataString, true);
                        String r3 = McsCreateLargeDataPolling.this.TAG;
                        IMSLog.s(r3, "onComplete: decryptedData: " + decrypt);
                        if (!TextUtils.isEmpty(decrypt)) {
                            dataString = decrypt;
                        } else {
                            EventLogHelper.add(McsCreateLargeDataPolling.this.TAG, McsCreateLargeDataPolling.this.mPhoneId, "decryptedData is empty");
                        }
                    }
                    try {
                        McsOMAApiResponseParam mcsOMAApiResponseParam = (McsOMAApiResponseParam) new Gson().fromJson(dataString, McsOMAApiResponseParam.class);
                        if (mcsOMAApiResponseParam == null || (nmsEventList = mcsOMAApiResponseParam.nmsEventList) == null) {
                            IMSLog.i(McsCreateLargeDataPolling.this.TAG, "onComplete: response or nmsEventList is null");
                            McsCreateLargeDataPolling.this.mIAPICallFlowListener.onGoToEvent(OMASyncEventType.SEND_LARGE_DATA_POLLING_FINISHED.getId(), (Object) null);
                            return;
                        }
                        boolean z = mcsOMAApiResponseParam.ncListComplete;
                        boolean z2 = false;
                        if (Util.isMatchedSubscriptionID(nmsEventList, McsCreateLargeDataPolling.this.mStoreClient)) {
                            long longValue = mcsOMAApiResponseParam.nmsEventList.index.longValue();
                            long oMASubscriptionIndex = McsCreateLargeDataPolling.this.mStoreClient.getPrerenceManager().getOMASubscriptionIndex();
                            String r0 = McsCreateLargeDataPolling.this.TAG;
                            IMSLog.i(r0, "onComplete: currIndex: " + longValue + " savedIndex:" + oMASubscriptionIndex + " ncListComplete:" + z);
                            if (longValue <= oMASubscriptionIndex + McsCreateLargeDataPolling.serialVersionUID) {
                                McsCreateLargeDataPolling.this.mStoreClient.getPrerenceManager().saveOMASubscriptionRestartToken(nmsEventList.restartToken);
                                McsCreateLargeDataPolling.this.mStoreClient.getPrerenceManager().saveOMASubscriptionIndex(longValue);
                                IMSLog.i(McsCreateLargeDataPolling.this.TAG, "onComplete: NmsEventList being processed");
                                McsCreateLargeDataPolling.this.mStoreClient.getCloudMessageBufferSchedulingHandler().onNativeChannelReceived(new ParamOMAresponseforBufDB.Builder().setActionType(ParamOMAresponseforBufDB.ActionType.RECEIVE_NOTIFICATION).setMcsNmsEventList(nmsEventList).build());
                                long oMASubscriptionIndex2 = McsCreateLargeDataPolling.this.mStoreClient.getPrerenceManager().getOMASubscriptionIndex();
                                while (true) {
                                    if (McsNotificationListContainer.getInstance(McsCreateLargeDataPolling.this.mStoreClient.getClientID()).isEmpty() || McsNotificationListContainer.getInstance(McsCreateLargeDataPolling.this.mStoreClient.getClientID()).peekFirstIndex() != oMASubscriptionIndex2 + McsCreateLargeDataPolling.serialVersionUID) {
                                        break;
                                    }
                                    Map.Entry<Long, NmsEventList> popFirstEntry = McsNotificationListContainer.getInstance(McsCreateLargeDataPolling.this.mStoreClient.getClientID()).popFirstEntry();
                                    if (popFirstEntry == null) {
                                        Log.e(McsCreateLargeDataPolling.this.TAG, "handleNmsEvent: firstEntry is null");
                                    } else {
                                        String r22 = McsCreateLargeDataPolling.this.TAG;
                                        IMSLog.i(r22, "onComplete: Process nmsEventList from the NotificationListContainer, savedIndex: " + oMASubscriptionIndex2 + " currIndex:" + longValue);
                                        NmsEventList value = popFirstEntry.getValue();
                                        String str = value.restartToken;
                                        longValue = value.index.longValue();
                                        McsCreateLargeDataPolling.this.mStoreClient.getPrerenceManager().saveOMASubscriptionRestartToken(str);
                                        McsCreateLargeDataPolling.this.mStoreClient.getPrerenceManager().saveOMASubscriptionIndex(longValue);
                                        McsCreateLargeDataPolling.this.mStoreClient.getCloudMessageBufferSchedulingHandler().onNativeChannelReceived(new ParamOMAresponseforBufDB.Builder().setActionType(ParamOMAresponseforBufDB.ActionType.RECEIVE_NOTIFICATION).setMcsNmsEventList(value).build());
                                        oMASubscriptionIndex2 = McsCreateLargeDataPolling.this.mStoreClient.getPrerenceManager().getOMASubscriptionIndex();
                                        if (McsNotificationListContainer.getInstance(McsCreateLargeDataPolling.this.mStoreClient.getClientID()).isEmpty()) {
                                            McsCreateLargeDataPolling.this.mIControllerCommonInterface.update(OMASyncEventType.REMOVE_UPDATE_SUBSCRIPTION_CHANNEL.getId());
                                            break;
                                        }
                                    }
                                }
                            } else {
                                boolean isEmpty = McsNotificationListContainer.getInstance(McsCreateLargeDataPolling.this.mStoreClient.getClientID()).isEmpty();
                                McsNotificationListContainer.getInstance(McsCreateLargeDataPolling.this.mStoreClient.getClientID()).insertContainer(Long.valueOf(longValue), nmsEventList, McsCreateLargeDataPolling.this.mStoreClient.getClientID(), Long.valueOf(oMASubscriptionIndex));
                                z2 = isEmpty;
                            }
                        } else {
                            String r02 = McsCreateLargeDataPolling.this.TAG;
                            Log.e(r02, "subscription url did not match with clientId: " + McsCreateLargeDataPolling.this.mStoreClient.getClientID());
                        }
                        if (z2) {
                            McsCreateLargeDataPolling.this.mIControllerCommonInterface.updateDelay(OMASyncEventType.UPDATE_SUBSCRIPTION_CHANNEL_DELAY.getId(), SoftphoneNamespaces.SoftphoneSettings.LONG_BACKOFF);
                        }
                        if (z) {
                            McsCreateLargeDataPolling.this.mIAPICallFlowListener.onSuccessfulEvent(this, OMASyncEventType.SEND_LARGE_DATA_POLLING_FINISHED.getId(), (Object) null);
                        } else {
                            McsCreateLargeDataPolling.this.mIAPICallFlowListener.onSuccessfulEvent(this, OMASyncEventType.SEND_LARGE_DATA_POLLING_REQUEST.getId(), str);
                        }
                    } catch (Exception e) {
                        String r23 = McsCreateLargeDataPolling.this.TAG;
                        IMSLog.i(r23, "onComplete: Exception: " + e.getMessage());
                        McsCreateLargeDataPolling.this.mIAPICallFlowListener.onGoToEvent(OMASyncEventType.SEND_LARGE_DATA_POLLING_FINISHED.getId(), (Object) null);
                    }
                } else {
                    String r24 = McsCreateLargeDataPolling.this.TAG;
                    int r32 = McsCreateLargeDataPolling.this.mPhoneId;
                    EventLogHelper.add(r24, r32, "onComplete: statusCode: " + statusCode);
                    if (McsCreateLargeDataPolling.this.isErrorCodeSupported(statusCode)) {
                        if (!McsCreateLargeDataPolling.this.mStoreClient.getCloudMessageStrategyManager().getStrategy().handleNCCommonError(McsCreateLargeDataPolling.this.mIAPICallFlowListener, McsCreateLargeDataPolling.this.mHttpInterface, statusCode, McsCreateLargeDataPolling.this.checkRetryAfter(httpResponseParams, McsCreateLargeDataPolling.this.mStoreClient.getMcsRetryMapAdapter().getRetryCount(McsCreateLargeDataPolling.this.mHttpInterface.getClass().getSimpleName())))) {
                            McsCreateLargeDataPolling.this.mIControllerCommonInterface.update(OMASyncEventType.UPDATE_SUBSCRIPTION_CHANNEL.getId());
                        }
                    } else if (statusCode >= 400 && statusCode < 500) {
                        McsCreateLargeDataPolling.this.mIControllerCommonInterface.update(OMASyncEventType.UPDATE_SUBSCRIPTION_CHANNEL.getId());
                    }
                }
            }

            public void onFail(IOException iOException) {
                String r0 = McsCreateLargeDataPolling.this.TAG;
                IMSLog.e(r0, "onFail: exception " + iOException.getMessage());
                EventLogHelper.add(McsCreateLargeDataPolling.this.TAG, McsCreateLargeDataPolling.this.mPhoneId, "onFail: IOException");
            }
        });
    }
}
