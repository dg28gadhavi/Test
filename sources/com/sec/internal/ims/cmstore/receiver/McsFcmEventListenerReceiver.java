package com.sec.internal.ims.cmstore.receiver;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.telephony.PhoneNumberUtils;
import android.text.TextUtils;
import com.google.gson.Gson;
import com.google.gson.JsonParseException;
import com.sec.internal.ims.cmstore.MessageStoreClient;
import com.sec.internal.ims.cmstore.helper.EventLogHelper;
import com.sec.internal.ims.cmstore.utils.Util;
import com.sec.internal.imscr.LogClass;
import com.sec.internal.interfaces.ims.cmstore.IMcsFcmPushNotificationListener;
import com.sec.internal.log.IMSLog;
import com.sec.internal.omanetapi.common.data.McsOMAApiResponseParam;
import com.sec.internal.omanetapi.nc.data.McsLargePollingNotification;
import com.sec.internal.omanetapi.nc.data.SyncBlockfilter;
import com.sec.internal.omanetapi.nc.data.SyncConfig;
import com.sec.internal.omanetapi.nc.data.SyncContact;
import com.sec.internal.omanetapi.nc.data.SyncMessage;
import com.sec.internal.omanetapi.nc.data.SyncStatus;
import com.sec.internal.omanetapi.nms.data.NmsEventList;
import java.util.ArrayList;
import java.util.Iterator;

public class McsFcmEventListenerReceiver extends BroadcastReceiver {
    private static final String FROM_FIELD = "from";
    private static final String INTENT_RECEIVE_FCM_PUSH_NOTIFICATION = "com.sec.internal.ims.fcm.action.RECEIVE_FCM_PUSH_NOTIFICATION";
    private static final String MESSAGE_FIELD = "message";
    private static final String TAG = McsFcmEventListenerReceiver.class.getSimpleName();
    private Context mContext;
    private int mPhoneId;
    private MessageStoreClient mStoreClient;

    public McsFcmEventListenerReceiver(Context context, int i, MessageStoreClient messageStoreClient) {
        this.mPhoneId = i;
        this.mContext = context;
        this.mStoreClient = messageStoreClient;
    }

    public void onReceive(Context context, Intent intent) {
        if (INTENT_RECEIVE_FCM_PUSH_NOTIFICATION.equals(intent.getAction())) {
            String str = TAG;
            IMSLog.i(str, this.mPhoneId, "onReceive: INTENT_RECEIVE_FCM_PUSH_NOTIFICATION");
            String stringExtra = intent.getStringExtra("from");
            String stringExtra2 = intent.getStringExtra("message");
            String fcmSenderId = this.mStoreClient.getPrerenceManager().getFcmSenderId();
            int i = this.mPhoneId;
            IMSLog.s(str, i, "onReceive: message: " + stringExtra2 + " from: " + stringExtra + " senderId: " + fcmSenderId);
            if (TextUtils.isEmpty(stringExtra) || TextUtils.isEmpty(stringExtra2) || TextUtils.isEmpty(fcmSenderId) || !TextUtils.equals(stringExtra, fcmSenderId)) {
                IMSLog.e(str, this.mPhoneId, "onReceive: invalid data");
                return;
            }
            try {
                McsOMAApiResponseParam mcsOMAApiResponseParam = (McsOMAApiResponseParam) new Gson().fromJson(stringExtra2, McsOMAApiResponseParam.class);
                if (mcsOMAApiResponseParam == null) {
                    IMSLog.e(str, this.mPhoneId, "onReceive: response is null");
                    return;
                }
                String str2 = mcsOMAApiResponseParam.mdn;
                String formatNumberToE164 = PhoneNumberUtils.formatNumberToE164(this.mStoreClient.getPrerenceManager().getUserCtn(), Util.getSimCountryCode(this.mContext, this.mPhoneId));
                if (!TextUtils.isEmpty(str2)) {
                    if (TextUtils.equals(str2, formatNumberToE164)) {
                        ArrayList<IMcsFcmPushNotificationListener> mcsFcmPushNotificationListener = this.mStoreClient.getMcsFcmPushNotificationListener();
                        SyncStatus syncStatus = mcsOMAApiResponseParam.syncStatus;
                        if (syncStatus != null) {
                            String str3 = syncStatus.status;
                            int i2 = this.mPhoneId;
                            EventLogHelper.infoLogAndAdd(str, i2, "syncStatus [status: " + str3 + "]");
                            IMSLog.c(LogClass.MCS_NC_PUSH_SYNC_STATUS, this.mPhoneId + ",NC:PS_SC_STS," + str3);
                            Iterator<IMcsFcmPushNotificationListener> it = mcsFcmPushNotificationListener.iterator();
                            while (it.hasNext()) {
                                IMcsFcmPushNotificationListener next = it.next();
                                String str4 = TAG;
                                int i3 = this.mPhoneId;
                                IMSLog.s(str4, i3, "syncStatusPushNotification: listener = " + next);
                                next.syncStatusPushNotification(str3);
                            }
                            return;
                        }
                        NmsEventList nmsEventList = mcsOMAApiResponseParam.nmsEventList;
                        if (nmsEventList != null) {
                            IMSLog.i(str, this.mPhoneId, "nmsEventList");
                            if (Util.isMatchedSubscriptionID(nmsEventList, this.mStoreClient)) {
                                Iterator<IMcsFcmPushNotificationListener> it2 = mcsFcmPushNotificationListener.iterator();
                                while (it2.hasNext()) {
                                    IMcsFcmPushNotificationListener next2 = it2.next();
                                    String str5 = TAG;
                                    int i4 = this.mPhoneId;
                                    IMSLog.s(str5, i4, "nmsEventListPushNotification: listener = " + next2);
                                    next2.nmsEventListPushNotification(nmsEventList);
                                }
                                return;
                            }
                            return;
                        }
                        SyncContact syncContact = mcsOMAApiResponseParam.syncContact;
                        if (syncContact != null) {
                            String str6 = syncContact.syncType;
                            int i5 = this.mPhoneId;
                            EventLogHelper.infoLogAndAdd(str, i5, "syncContact [syncType: " + str6 + "]");
                            IMSLog.c(LogClass.MCS_NC_PUSH_SYNC_CONTACT, this.mPhoneId + ",NC:PS_SC_CT," + str6);
                            Iterator<IMcsFcmPushNotificationListener> it3 = mcsFcmPushNotificationListener.iterator();
                            while (it3.hasNext()) {
                                IMcsFcmPushNotificationListener next3 = it3.next();
                                String str7 = TAG;
                                int i6 = this.mPhoneId;
                                IMSLog.s(str7, i6, "syncContactPushNotification: listener = " + next3);
                                next3.syncContactPushNotification(str6);
                            }
                            return;
                        }
                        SyncConfig syncConfig = mcsOMAApiResponseParam.syncConfig;
                        if (syncConfig != null) {
                            String str8 = syncConfig.configType;
                            int i7 = this.mPhoneId;
                            EventLogHelper.infoLogAndAdd(str, i7, "syncConfig [configType: " + str8 + "]");
                            IMSLog.c(LogClass.MCS_NC_PUSH_SYNC_CONFIG, this.mPhoneId + ",NC:PS_SC_CFG," + str8);
                            Iterator<IMcsFcmPushNotificationListener> it4 = mcsFcmPushNotificationListener.iterator();
                            while (it4.hasNext()) {
                                IMcsFcmPushNotificationListener next4 = it4.next();
                                String str9 = TAG;
                                int i8 = this.mPhoneId;
                                IMSLog.s(str9, i8, "syncConfigPushNotification: listener = " + next4);
                                next4.syncConfigPushNotification(str8);
                            }
                            return;
                        }
                        SyncMessage syncMessage = mcsOMAApiResponseParam.syncMessage;
                        if (syncMessage != null) {
                            String str10 = syncMessage.syncType;
                            int i9 = syncMessage.cms_data_ttl;
                            int i10 = this.mPhoneId;
                            EventLogHelper.infoLogAndAdd(str, i10, "syncMessage [syncType: " + str10 + ", cmsDataTtl: " + i9 + "]");
                            IMSLog.c(LogClass.MCS_NC_PUSH_SYNC_MESSAGE, this.mPhoneId + ",NC:PS_SC_MSG," + str10 + "," + i9);
                            Iterator<IMcsFcmPushNotificationListener> it5 = mcsFcmPushNotificationListener.iterator();
                            while (it5.hasNext()) {
                                IMcsFcmPushNotificationListener next5 = it5.next();
                                String str11 = TAG;
                                int i11 = this.mPhoneId;
                                IMSLog.s(str11, i11, "syncMessagePushNotification: listener = " + next5);
                                next5.syncMessagePushNotification(str10, i9);
                            }
                            return;
                        }
                        McsLargePollingNotification mcsLargePollingNotification = mcsOMAApiResponseParam.largePollingNotification;
                        if (mcsLargePollingNotification != null) {
                            IMSLog.i(str, this.mPhoneId, "largePollingNotification");
                            Iterator<IMcsFcmPushNotificationListener> it6 = mcsFcmPushNotificationListener.iterator();
                            while (it6.hasNext()) {
                                IMcsFcmPushNotificationListener next6 = it6.next();
                                String str12 = TAG;
                                int i12 = this.mPhoneId;
                                IMSLog.s(str12, i12, "largePollingPushNotification: listener = " + next6);
                                next6.largePollingPushNotification(mcsLargePollingNotification);
                            }
                            return;
                        }
                        SyncBlockfilter syncBlockfilter = mcsOMAApiResponseParam.syncBlockfilter;
                        if (syncBlockfilter != null) {
                            String str13 = syncBlockfilter.syncType;
                            int i13 = this.mPhoneId;
                            EventLogHelper.infoLogAndAdd(str, i13, "syncBlockfilter [syncType: " + str13 + "]");
                            IMSLog.c(LogClass.MCS_NC_PUSH_SYNC_BLOCKFILTER, this.mPhoneId + ",NC:PS_SC_BLKFLT," + str13);
                            Iterator<IMcsFcmPushNotificationListener> it7 = mcsFcmPushNotificationListener.iterator();
                            while (it7.hasNext()) {
                                IMcsFcmPushNotificationListener next7 = it7.next();
                                String str14 = TAG;
                                int i14 = this.mPhoneId;
                                IMSLog.s(str14, i14, "syncBlockfilterPushNotification: listener = " + next7);
                                next7.syncBlockfilterPushNotification(str13);
                            }
                            return;
                        }
                        return;
                    }
                }
                IMSLog.e(str, this.mPhoneId, "onReceive: mdn is different with userCtn");
            } catch (JsonParseException | NullPointerException e) {
                String str15 = TAG;
                int i15 = this.mPhoneId;
                IMSLog.e(str15, i15, "onReceive: Exception: " + e.getMessage());
            }
        }
    }
}
