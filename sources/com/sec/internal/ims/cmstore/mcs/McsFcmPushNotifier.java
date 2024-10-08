package com.sec.internal.ims.cmstore.mcs;

import android.os.RemoteCallbackList;
import android.os.RemoteException;
import com.sec.ims.ICentralMsgStoreServiceListener;
import com.sec.internal.constants.ims.cmstore.McsConstants;
import com.sec.internal.ims.cmstore.MessageStoreClient;
import com.sec.internal.interfaces.ims.cmstore.IMcsFcmPushNotificationListener;
import com.sec.internal.log.IMSLog;
import com.sec.internal.omanetapi.nc.data.McsLargePollingNotification;
import com.sec.internal.omanetapi.nms.data.NmsEventList;

public class McsFcmPushNotifier {
    private final String LOG_TAG;
    private final Object mLock = new Object();
    private final int mPhoneId;
    private final MessageStoreClient mStoreClient;

    public McsFcmPushNotifier(MessageStoreClient messageStoreClient, int i) {
        String simpleName = McsFcmPushNotifier.class.getSimpleName();
        this.LOG_TAG = simpleName;
        this.mStoreClient = messageStoreClient;
        this.mPhoneId = i;
        registerMcsFcmPushNotificationListener();
        IMSLog.i(simpleName, i, "created");
    }

    private void registerMcsFcmPushNotificationListener() {
        this.mStoreClient.setMcsFcmPushNotificationListener(new IMcsFcmPushNotificationListener() {
            public void largePollingPushNotification(McsLargePollingNotification mcsLargePollingNotification) {
            }

            public void nmsEventListPushNotification(NmsEventList nmsEventList) {
            }

            public void syncContactPushNotification(String str) {
            }

            public void syncMessagePushNotification(String str, int i) {
            }

            public void syncStatusPushNotification(String str) {
            }

            public void syncConfigPushNotification(String str) {
                McsFcmPushNotifier.this.notifyMcsFcmPushMessages(McsConstants.PushMessages.TYPE_SYNC_CONFIG, McsConstants.PushMessages.KEY_CONFIG_TYPE, str);
            }

            public void syncBlockfilterPushNotification(String str) {
                McsFcmPushNotifier.this.notifyMcsFcmPushMessages(McsConstants.PushMessages.TYPE_SYNC_BLOCKFILTER, McsConstants.PushMessages.KEY_SYNC_TYPE, str);
            }
        });
    }

    public void notifyMcsFcmPushMessages(String str, String str2, String str3) {
        synchronized (this.mLock) {
            RemoteCallbackList<ICentralMsgStoreServiceListener> mcsProvisioningListener = this.mStoreClient.getMcsProvisioningListener();
            if (mcsProvisioningListener == null) {
                IMSLog.i(this.LOG_TAG, this.mPhoneId, "notifyMcsFcmPushMessages: listeners is empty");
                return;
            }
            try {
                int beginBroadcast = mcsProvisioningListener.beginBroadcast();
                String str4 = this.LOG_TAG;
                int i = this.mPhoneId;
                IMSLog.i(str4, i, "notifyMcsFcmPushMessages: length: " + beginBroadcast + ", pushType: " + str);
                for (int i2 = 0; i2 < beginBroadcast; i2++) {
                    try {
                        mcsProvisioningListener.getBroadcastItem(i2).onCmsPushMessageReceived(str, str2, str3);
                    } catch (RemoteException | NullPointerException e) {
                        String str5 = this.LOG_TAG;
                        int i3 = this.mPhoneId;
                        IMSLog.e(str5, i3, "notifyMcsFcmPushMessages: onCmsPushMessageReceived call failed: " + e);
                    }
                }
            } catch (IllegalStateException e2) {
                try {
                    String str6 = this.LOG_TAG;
                    int i4 = this.mPhoneId;
                    IMSLog.e(str6, i4, "notifyMcsFcmPushMessages: failed: " + e2);
                } catch (Throwable th) {
                    mcsProvisioningListener.finishBroadcast();
                    throw th;
                }
            }
            mcsProvisioningListener.finishBroadcast();
        }
    }
}
