package com.sec.internal.ims.cmstore.receiver;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.text.TextUtils;
import android.util.Log;
import com.google.gson.Gson;
import com.sec.internal.constants.ims.entitilement.NSDSNamespaces;
import com.sec.internal.constants.ims.os.PhoneConstants;
import com.sec.internal.ims.cmstore.MessageStoreClient;
import com.sec.internal.ims.cmstore.helper.MailBoxHelper;
import com.sec.internal.ims.cmstore.params.ParamOMAresponseforBufDB;
import com.sec.internal.ims.cmstore.utils.Util;
import com.sec.internal.log.IMSLog;
import com.sec.internal.omanetapi.common.data.Link;
import com.sec.internal.omanetapi.common.data.OMAApiResponseParam;
import com.sec.internal.omanetapi.nc.data.NotificationList;
import com.sec.internal.omanetapi.nms.data.LargePollingNotification;
import java.net.URL;
import java.util.Enumeration;
import java.util.Hashtable;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

public class GcmIntentBroadcastReceiver extends BroadcastReceiver {
    private static final String LOG_TAG = GcmIntentBroadcastReceiver.class.getSimpleName();
    private Hashtable<Integer, MessageStoreClient> mClients;

    public GcmIntentBroadcastReceiver(Hashtable<Integer, MessageStoreClient> hashtable) {
        this.mClients = hashtable;
    }

    public void onReceive(Context context, Intent intent) {
        String str = LOG_TAG;
        Log.d(str, " onReceive " + intent);
        handleIntent(intent);
    }

    private void handleIntent(Intent intent) {
        String action = intent.getAction();
        String str = LOG_TAG;
        IMSLog.s(str, "Received intent: " + action);
        if (TextUtils.equals(action, NSDSNamespaces.NSDSActions.RECEIVED_GCM_EVENT_NOTIFICATION)) {
            onReceiveNativeChannelNotification(intent);
        }
    }

    private void onReceiveNativeChannelNotification(Intent intent) {
        IMSLog.s(LOG_TAG, "onReceiveNativeChannelNotification");
        Hashtable hashtable = new Hashtable();
        Enumeration<Integer> keys = this.mClients.keys();
        int i = 0;
        while (keys.hasMoreElements()) {
            int intValue = keys.nextElement().intValue();
            String str = LOG_TAG;
            Log.e(str, "get key from clients: " + intValue);
            MessageStoreClient messageStoreClient = this.mClients.get(Integer.valueOf(intValue));
            if (messageStoreClient != null) {
                boolean isCmsProfileActive = messageStoreClient.getNetAPIWorkingStatusController().isCmsProfileActive();
                Log.e(str, "isAMBSActive: " + isCmsProfileActive);
                if (isCmsProfileActive && !TextUtils.isEmpty(this.mClients.get(Integer.valueOf(intValue)).getPrerenceManager().getOMASubscriptionResUrl())) {
                    hashtable.put(Integer.valueOf(i), messageStoreClient);
                    i++;
                }
            }
        }
        if (hashtable.size() == 0) {
            Log.e(LOG_TAG, "it should not receive gcm notifications here");
            return;
        }
        String string = intent.getExtras().getString(NSDSNamespaces.NSDSExtras.ORIG_PUSH_MESSAGE);
        String str2 = LOG_TAG;
        IMSLog.s(str2, "pushMessage: " + string);
        try {
            OMAApiResponseParam oMAApiResponseParam = (OMAApiResponseParam) new Gson().fromJson(string, OMAApiResponseParam.class);
            if (oMAApiResponseParam != null) {
                NotificationList[] notificationListArr = oMAApiResponseParam.notificationList;
                if (notificationListArr != null) {
                    handlePushNotification(notificationListArr, hashtable, string);
                    return;
                }
            }
            Log.e(str2, "response or notificationList is null, polling failed");
        } catch (Exception e) {
            String str3 = LOG_TAG;
            Log.e(str3, "onReceiveNativeChannelNotification: " + e.getMessage());
        }
    }

    public void handlePushNotification(NotificationList[] notificationListArr, Hashtable<Integer, MessageStoreClient> hashtable, String str) {
        boolean z;
        if (notificationListArr != null) {
            NotificationList notificationList = notificationListArr[0];
            int i = 0;
            while (i < hashtable.size()) {
                MessageStoreClient messageStoreClient = hashtable.get(Integer.valueOf(i));
                LargePollingNotification largePollingNotification = notificationList.largePollingNotification;
                if (largePollingNotification != null) {
                    z = messageStoreClient.getPrerenceManager().getUserTelCtn().equals(Util.getLineTelUriFromObjUrl(largePollingNotification.channelURL));
                } else {
                    z = false;
                }
                if (notificationList.nmsEventList != null) {
                    z = isMatchedSubscriptionID(notificationList, messageStoreClient);
                }
                if (!z || !messageStoreClient.getNetAPIWorkingStatusController().isPushNotiProcessPaused()) {
                    i++;
                } else {
                    Log.i(LOG_TAG, "Push Notification Processing is paused. Wait for app to come in foreground");
                    return;
                }
            }
            if (notificationList.largePollingNotification != null) {
                handleLargePollingNoti(notificationList, hashtable);
            } else if (MailBoxHelper.isMailBoxReset(str)) {
                Log.i(LOG_TAG, "MailBoxReset true");
                for (int i2 = 0; i2 < hashtable.size(); i2++) {
                    MessageStoreClient messageStoreClient2 = hashtable.get(Integer.valueOf(i2));
                    if (isMatchedSubscriptionID(notificationList, messageStoreClient2)) {
                        messageStoreClient2.getCloudMessageBufferSchedulingHandler().onNativeChannelReceived(new ParamOMAresponseforBufDB.Builder().setActionType(ParamOMAresponseforBufDB.ActionType.MAILBOX_RESET).build());
                    }
                }
            } else if (notificationList.nmsEventList != null) {
                handleNmsEvent(notificationListArr, notificationList, hashtable);
            }
        }
    }

    private void handleLargePollingNoti(NotificationList notificationList, Hashtable<Integer, MessageStoreClient> hashtable) {
        LargePollingNotification largePollingNotification = notificationList.largePollingNotification;
        String str = largePollingNotification.channelURL;
        String str2 = largePollingNotification.channelExpiry;
        String lineTelUriFromObjUrl = Util.getLineTelUriFromObjUrl(str);
        for (int i = 0; i < hashtable.size(); i++) {
            MessageStoreClient messageStoreClient = hashtable.get(Integer.valueOf(i));
            if (messageStoreClient.getPrerenceManager().getUserTelCtn().equals(lineTelUriFromObjUrl)) {
                if (!Util.hasChannelExpired(str2)) {
                    String str3 = LOG_TAG;
                    Log.i(str3, "largePollingNotification " + str);
                    messageStoreClient.getPrerenceManager().saveOMAChannelURL(str);
                    messageStoreClient.getNetAPIWorkingStatusController().handleLargeDataPolling();
                } else {
                    Log.i(LOG_TAG, "largePollingNotification channel expired");
                    messageStoreClient.getNetAPIWorkingStatusController().updateSubscriptionChannel();
                }
            }
        }
    }

    /* JADX WARNING: Removed duplicated region for block: B:23:0x0159  */
    /* JADX WARNING: Removed duplicated region for block: B:28:0x0160 A[SYNTHETIC] */
    /* Code decompiled incorrectly, please refer to instructions dump. */
    private void handleNmsEvent(com.sec.internal.omanetapi.nc.data.NotificationList[] r11, com.sec.internal.omanetapi.nc.data.NotificationList r12, java.util.Hashtable<java.lang.Integer, com.sec.internal.ims.cmstore.MessageStoreClient> r13) {
        /*
            r10 = this;
            r10 = 0
            r0 = r10
        L_0x0002:
            int r1 = r13.size()
            if (r0 >= r1) goto L_0x0164
            java.lang.Integer r1 = java.lang.Integer.valueOf(r0)
            java.lang.Object r1 = r13.get(r1)
            com.sec.internal.ims.cmstore.MessageStoreClient r1 = (com.sec.internal.ims.cmstore.MessageStoreClient) r1
            com.sec.internal.omanetapi.nms.data.NmsEventList r2 = r12.nmsEventList
            boolean r2 = com.sec.internal.ims.cmstore.utils.Util.isMatchedSubscriptionID(r2, r1)
            if (r2 == 0) goto L_0x013b
            com.sec.internal.ims.cmstore.utils.CloudMessagePreferenceManager r2 = r1.getPrerenceManager()
            long r2 = r2.getOMASubscriptionIndex()
            com.sec.internal.omanetapi.nms.data.NmsEventList r4 = r12.nmsEventList
            java.lang.Long r4 = r4.index
            long r4 = r4.longValue()
            java.lang.String r6 = LOG_TAG
            int r7 = r1.getClientID()
            java.lang.StringBuilder r8 = new java.lang.StringBuilder
            r8.<init>()
            java.lang.String r9 = "handleNmsEvent:  notification curindex="
            r8.append(r9)
            r8.append(r4)
            java.lang.String r9 = " savedindex="
            r8.append(r9)
            r8.append(r2)
            java.lang.String r8 = r8.toString()
            com.sec.internal.ims.cmstore.helper.EventLogHelper.debugLogAndAdd(r6, r7, r8)
            r6 = 1
            long r2 = r2 + r6
            int r2 = (r4 > r2 ? 1 : (r4 == r2 ? 0 : -1))
            if (r2 <= 0) goto L_0x0070
            int r2 = r1.getClientID()
            com.sec.internal.ims.cmstore.utils.NotificationListContainer r2 = com.sec.internal.ims.cmstore.utils.NotificationListContainer.getInstance(r2)
            boolean r2 = r2.isEmpty()
            int r3 = r1.getClientID()
            com.sec.internal.ims.cmstore.utils.NotificationListContainer r3 = com.sec.internal.ims.cmstore.utils.NotificationListContainer.getInstance(r3)
            java.lang.Long r4 = java.lang.Long.valueOf(r4)
            r3.insertContainer(r4, r11)
            goto L_0x0157
        L_0x0070:
            if (r2 != 0) goto L_0x0156
            com.sec.internal.omanetapi.nms.data.NmsEventList r2 = r12.nmsEventList
            java.lang.String r2 = r2.restartToken
            com.sec.internal.ims.cmstore.utils.CloudMessagePreferenceManager r3 = r1.getPrerenceManager()
            r3.saveOMASubscriptionRestartToken(r2)
            com.sec.internal.ims.cmstore.utils.CloudMessagePreferenceManager r2 = r1.getPrerenceManager()
            r2.saveOMASubscriptionIndex(r4)
            com.sec.internal.ims.cmstore.params.ParamOMAresponseforBufDB$Builder r2 = new com.sec.internal.ims.cmstore.params.ParamOMAresponseforBufDB$Builder
            r2.<init>()
            com.sec.internal.ims.cmstore.params.ParamOMAresponseforBufDB$ActionType r3 = com.sec.internal.ims.cmstore.params.ParamOMAresponseforBufDB.ActionType.RECEIVE_NOTIFICATION
            com.sec.internal.ims.cmstore.params.ParamOMAresponseforBufDB$Builder r2 = r2.setActionType(r3)
            com.sec.internal.ims.cmstore.params.ParamOMAresponseforBufDB$Builder r2 = r2.setNotificationList(r11)
            com.sec.internal.ims.cmstore.cloudmessagebuffer.CloudMessageBufferSchedulingHandler r3 = r1.getCloudMessageBufferSchedulingHandler()
            com.sec.internal.ims.cmstore.params.ParamOMAresponseforBufDB r2 = r2.build()
            r3.onNativeChannelReceived(r2)
            com.sec.internal.ims.cmstore.utils.CloudMessagePreferenceManager r2 = r1.getPrerenceManager()
            long r2 = r2.getOMASubscriptionIndex()
        L_0x00a6:
            int r4 = r1.getClientID()
            com.sec.internal.ims.cmstore.utils.NotificationListContainer r4 = com.sec.internal.ims.cmstore.utils.NotificationListContainer.getInstance(r4)
            boolean r4 = r4.isEmpty()
            if (r4 != 0) goto L_0x0156
            int r4 = r1.getClientID()
            com.sec.internal.ims.cmstore.utils.NotificationListContainer r4 = com.sec.internal.ims.cmstore.utils.NotificationListContainer.getInstance(r4)
            long r4 = r4.peekFirstIndex()
            long r8 = r2 + r6
            int r4 = (r4 > r8 ? 1 : (r4 == r8 ? 0 : -1))
            if (r4 != 0) goto L_0x0156
            int r4 = r1.getClientID()
            com.sec.internal.ims.cmstore.utils.NotificationListContainer r4 = com.sec.internal.ims.cmstore.utils.NotificationListContainer.getInstance(r4)
            java.util.Map$Entry r4 = r4.popFirstEntry()
            if (r4 != 0) goto L_0x00dc
            java.lang.String r4 = LOG_TAG
            java.lang.String r5 = "handleNmsEvent: firstEntry is null"
            android.util.Log.e(r4, r5)
            goto L_0x00a6
        L_0x00dc:
            java.lang.Object r11 = r4.getValue()
            com.sec.internal.omanetapi.nc.data.NotificationList[] r11 = (com.sec.internal.omanetapi.nc.data.NotificationList[]) r11
            r12 = r11[r10]
            com.sec.internal.omanetapi.nms.data.NmsEventList r2 = r12.nmsEventList
            java.lang.String r3 = r2.restartToken
            java.lang.Long r2 = r2.index
            long r4 = r2.longValue()
            com.sec.internal.ims.cmstore.utils.CloudMessagePreferenceManager r2 = r1.getPrerenceManager()
            r2.saveOMASubscriptionRestartToken(r3)
            com.sec.internal.ims.cmstore.utils.CloudMessagePreferenceManager r2 = r1.getPrerenceManager()
            r2.saveOMASubscriptionIndex(r4)
            com.sec.internal.ims.cmstore.params.ParamOMAresponseforBufDB$Builder r2 = new com.sec.internal.ims.cmstore.params.ParamOMAresponseforBufDB$Builder
            r2.<init>()
            com.sec.internal.ims.cmstore.params.ParamOMAresponseforBufDB$ActionType r3 = com.sec.internal.ims.cmstore.params.ParamOMAresponseforBufDB.ActionType.RECEIVE_NOTIFICATION
            com.sec.internal.ims.cmstore.params.ParamOMAresponseforBufDB$Builder r2 = r2.setActionType(r3)
            com.sec.internal.ims.cmstore.params.ParamOMAresponseforBufDB$Builder r2 = r2.setNotificationList(r11)
            com.sec.internal.ims.cmstore.cloudmessagebuffer.CloudMessageBufferSchedulingHandler r3 = r1.getCloudMessageBufferSchedulingHandler()
            com.sec.internal.ims.cmstore.params.ParamOMAresponseforBufDB r2 = r2.build()
            r3.onNativeChannelReceived(r2)
            com.sec.internal.ims.cmstore.utils.CloudMessagePreferenceManager r2 = r1.getPrerenceManager()
            long r2 = r2.getOMASubscriptionIndex()
            int r4 = r1.getClientID()
            com.sec.internal.ims.cmstore.utils.NotificationListContainer r4 = com.sec.internal.ims.cmstore.utils.NotificationListContainer.getInstance(r4)
            boolean r4 = r4.isEmpty()
            if (r4 == 0) goto L_0x00a6
            java.lang.String r2 = LOG_TAG
            java.lang.String r3 = "NotificationListContainer is empty, all the disordered notifications have been proceeded, remove UPDATE_SUBSCRIPTION_CHANNEL_DELAY"
            android.util.Log.i(r2, r3)
            com.sec.internal.ims.cmstore.NetAPIWorkingStatusController r2 = r1.getNetAPIWorkingStatusController()
            r2.removeUpdateSubscriptionChannelEvent()
            goto L_0x0156
        L_0x013b:
            java.lang.String r2 = LOG_TAG
            java.lang.StringBuilder r3 = new java.lang.StringBuilder
            r3.<init>()
            java.lang.String r4 = "subscription url did not match with clientId: "
            r3.append(r4)
            int r4 = r1.getClientID()
            r3.append(r4)
            java.lang.String r3 = r3.toString()
            android.util.Log.e(r2, r3)
        L_0x0156:
            r2 = r10
        L_0x0157:
            if (r2 == 0) goto L_0x0160
            com.sec.internal.ims.cmstore.NetAPIWorkingStatusController r1 = r1.getNetAPIWorkingStatusController()
            r1.updateDelayedSubscriptionChannel()
        L_0x0160:
            int r0 = r0 + 1
            goto L_0x0002
        L_0x0164:
            return
        */
        throw new UnsupportedOperationException("Method not decompiled: com.sec.internal.ims.cmstore.receiver.GcmIntentBroadcastReceiver.handleNmsEvent(com.sec.internal.omanetapi.nc.data.NotificationList[], com.sec.internal.omanetapi.nc.data.NotificationList, java.util.Hashtable):void");
    }

    private boolean isMatchedSubscriptionID(NotificationList notificationList, MessageStoreClient messageStoreClient) {
        URL url;
        String oMASubscriptionResUrl = messageStoreClient.getPrerenceManager().getOMASubscriptionResUrl();
        boolean z = false;
        if (TextUtils.isEmpty(oMASubscriptionResUrl) || notificationList.nmsEventList.link == null) {
            String str = LOG_TAG;
            Log.d(str, "isMatchedSubscriptionID " + false);
            return false;
        }
        String lastPathFromUrl = getLastPathFromUrl(oMASubscriptionResUrl);
        String str2 = LOG_TAG;
        Log.d(str2, "isMatchedSubscriptionID subscriptionID = " + lastPathFromUrl);
        Link[] linkArr = notificationList.nmsEventList.link;
        int length = linkArr.length;
        int i = 0;
        while (true) {
            if (i >= length) {
                break;
            }
            Link link = linkArr[i];
            if ((PhoneConstants.SUBSCRIPTION_KEY.equalsIgnoreCase(link.rel) || "NmsSubscription".equalsIgnoreCase(link.rel)) && (url = link.href) != null) {
                String lastPathFromUrl2 = getLastPathFromUrl(url.toString());
                String str3 = LOG_TAG;
                Log.d(str3, "isMatchedSubscriptionID notiSubID = " + lastPathFromUrl2);
                if (lastPathFromUrl.equalsIgnoreCase(lastPathFromUrl2)) {
                    z = true;
                    break;
                }
            }
            i++;
        }
        String str4 = LOG_TAG;
        Log.d(str4, "isMatchedSubscriptionID " + z);
        return z;
    }

    private String getLastPathFromUrl(String str) {
        String[] split = str.split("/");
        return split[split.length - 1];
    }

    private boolean isMailBoxReset(String str) {
        JSONObject jSONObject;
        JSONObject jSONObject2;
        JSONArray jSONArray;
        JSONObject jSONObject3;
        try {
            JSONArray jSONArray2 = new JSONObject(str).getJSONArray("notificationList");
            if (jSONArray2 == null || (jSONObject = (JSONObject) jSONArray2.opt(0)) == null || (jSONObject2 = jSONObject.getJSONObject("nmsEventList")) == null || (jSONArray = jSONObject2.getJSONArray("nmsEvent")) == null || (jSONObject3 = (JSONObject) jSONArray.opt(0)) == null || !jSONObject3.has("resetBox")) {
                return false;
            }
            return true;
        } catch (JSONException e) {
            e.printStackTrace();
            return false;
        }
    }
}
