package com.sec.internal.ims.servicemodules.presence;

import android.os.Message;
import com.sec.ims.presence.PresenceInfo;
import com.sec.internal.constants.ims.servicemodules.options.CapabilityConstants;
import com.sec.internal.constants.ims.servicemodules.presence.PresenceNotifyInfo;
import com.sec.internal.constants.ims.servicemodules.presence.PresenceResponse;
import com.sec.internal.constants.ims.servicemodules.presence.PresenceSubscription;
import com.sec.internal.helper.AsyncResult;
import com.sec.internal.ims.servicemodules.presence.PresenceSubscriptionController;

public class PresenceEvent {
    static final int EVT_BAD_EVENT_TIMEOUT = 14;
    public static final int EVT_DEFAULT_MSG_APP_CHANGED = 19;
    static final int EVT_NEW_NOTIFY_INFO = 16;
    static final int EVT_NEW_NOTIFY_STATUS = 17;
    static final int EVT_NEW_PRESENCE_INFO = 10;
    static final int EVT_NEW_PRESENCE_INFO_DELAYED = 11;
    static final int EVT_NEW_WATCHER_INFO = 12;
    static final int EVT_PERIODIC_PUBLISH = 4;
    public static final int EVT_PUBLISH_CAPABILITIES = 15;
    static final int EVT_PUBLISH_COMPLETE = 2;
    public static final int EVT_PUBLISH_REQUEST = 1;
    static final int EVT_RETRY_PUBLISH_TIMEOUT = 18;
    static final int EVT_SUBSCRIBE_COMPLETE = 6;
    static final int EVT_SUBSCRIBE_LIST_REQUESTED = 7;
    static final int EVT_SUBSCRIBE_REQUESTED = 5;
    static final int EVT_SUBSCRIBE_RETRY = 8;
    static final int EVT_SUBSCRIPTION_TERMINATED = 9;
    static final int EVT_UNPUBLISH_REQUEST = 3;
    static final int EVT_WAKE_LOCK_TIMEOUT = 13;
    private static final String LOG_TAG = "PresenceEvent";

    static boolean handleEvent(Message message, PresenceModule presenceModule, PresenceUpdate presenceUpdate, int i) {
        switch (message.what) {
            case 1:
                int intValue = ((Integer) message.obj).intValue();
                presenceModule.publish(presenceModule.getOwnPresenceInfo(intValue), intValue);
                return true;
            case 2:
                PresenceResponse presenceResponse = (PresenceResponse) ((AsyncResult) message.obj).result;
                presenceModule.onPublishComplete(presenceResponse, presenceResponse.getPhoneId());
                return true;
            case 3:
                presenceModule.unpublish(((Integer) message.obj).intValue());
                return true;
            case 4:
                presenceModule.onPeriodicPublish(((Integer) message.obj).intValue());
                return true;
            case 5:
                presenceModule.onSubscribeRequested((PresenceSubscriptionController.SubscriptionRequest) message.obj);
                return true;
            case 6:
                AsyncResult asyncResult = (AsyncResult) message.obj;
                presenceModule.onSubscribeComplete((PresenceSubscription) asyncResult.userObj, (PresenceResponse) asyncResult.result);
                return true;
            case 7:
                presenceModule.onSubscribeListRequested((CapabilityConstants.RequestType) message.obj, message.arg1, message.arg2);
                return true;
            case 8:
                presenceModule.onSubscribeRetry((PresenceSubscription) message.obj);
                return true;
            case 9:
                presenceModule.onSubscriptionTerminated((PresenceSubscription) message.obj);
                return true;
            case 10:
                PresenceInfo presenceInfo = (PresenceInfo) ((AsyncResult) message.obj).result;
                presenceUpdate.onNewPresenceInformation(presenceInfo, presenceInfo.getPhoneId(), PresenceSubscriptionController.getSubscription(presenceInfo.getSubscriptionId(), presenceInfo.getPhoneId()));
                return true;
            case 11:
                PresenceInfo presenceInfo2 = (PresenceInfo) message.obj;
                int i2 = message.arg1;
                presenceUpdate.onNewPresenceInformation(presenceInfo2, i2, PresenceSubscriptionController.getSubscription(presenceInfo2.getSubscriptionId(), i2));
                return true;
            case 12:
                PresenceInfo presenceInfo3 = (PresenceInfo) ((AsyncResult) message.obj).result;
                presenceUpdate.onNewWatcherInformation(presenceInfo3, presenceInfo3.getPhoneId(), PresenceSubscriptionController.getSubscription(presenceInfo3.getSubscriptionId(), presenceInfo3.getPhoneId()));
                return true;
            case 13:
                presenceModule.clearWakeLock();
                return true;
            case 14:
                presenceModule.onBadEventTimeout(((Integer) message.obj).intValue());
                return true;
            case 15:
                int i3 = message.arg1;
                presenceModule.publish(presenceModule.getOwnPresenceInfo(i3), i3, (String) message.obj);
                return true;
            case 16:
                PresenceNotifyInfo presenceNotifyInfo = (PresenceNotifyInfo) ((AsyncResult) message.obj).result;
                presenceModule.onNewNotifyInfo(presenceNotifyInfo, presenceNotifyInfo.getPhoneId());
                return true;
            case 17:
                PresenceResponse presenceResponse2 = (PresenceResponse) ((AsyncResult) message.obj).result;
                presenceModule.onNewNotifyStatus(presenceResponse2, presenceResponse2.getPhoneId());
                return true;
            case 18:
                presenceModule.onRetryPublishTimeout(((Integer) message.obj).intValue());
                return true;
            case 19:
                presenceModule.onDefaultSmsPackageChanged();
                return true;
            default:
                return false;
        }
    }
}
