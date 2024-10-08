package com.sec.internal.ims.fcm;

import com.google.firebase.messaging.FirebaseMessagingService;
import com.google.firebase.messaging.RemoteMessage;
import com.sec.internal.ims.registry.ImsRegistry;
import com.sec.internal.log.IMSLog;

public class FcmListenerService extends FirebaseMessagingService {
    private static final String LOG_TAG = FcmListenerService.class.getSimpleName();

    public void onMessageReceived(RemoteMessage remoteMessage) {
        String str = LOG_TAG;
        IMSLog.i(str, "onMessageReceived: id: " + remoteMessage.getMessageId());
        ImsRegistry.getFcmHandler().onMessageReceived(this, remoteMessage.getFrom(), remoteMessage.getData());
    }
}
