package com.sec.internal.ims.servicemodules.tapi.service.broadcaster;

import android.os.RemoteCallbackList;
import com.gsma.services.rcs.contact.ContactId;
import com.gsma.services.rcs.extension.IMultimediaMessagingSessionListener;
import com.gsma.services.rcs.extension.MultimediaSession;

public class MultimediaMessagingSessionEventBroadcaster implements IMultimediaMessagingSessionEventBroadcaster {
    private final RemoteCallbackList<IMultimediaMessagingSessionListener> mMultimediaMessagingListeners = new RemoteCallbackList<>();

    public void addMultimediaMessagingEventListener(IMultimediaMessagingSessionListener iMultimediaMessagingSessionListener) {
        this.mMultimediaMessagingListeners.register(iMultimediaMessagingSessionListener);
    }

    public void removeMultimediaMessagingEventListener(IMultimediaMessagingSessionListener iMultimediaMessagingSessionListener) {
        this.mMultimediaMessagingListeners.unregister(iMultimediaMessagingSessionListener);
    }

    public void broadcastMessageReceived(ContactId contactId, String str, byte[] bArr) {
        int beginBroadcast = this.mMultimediaMessagingListeners.beginBroadcast();
        for (int i = 0; i < beginBroadcast; i++) {
            try {
                this.mMultimediaMessagingListeners.getBroadcastItem(i).onMessageReceived(contactId, str, bArr, "");
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
        this.mMultimediaMessagingListeners.finishBroadcast();
    }

    public void broadcastMessageReceived(ContactId contactId, String str, byte[] bArr, String str2) {
        int beginBroadcast = this.mMultimediaMessagingListeners.beginBroadcast();
        for (int i = 0; i < beginBroadcast; i++) {
            try {
                this.mMultimediaMessagingListeners.getBroadcastItem(i).onMessageReceived(contactId, str, bArr, str2);
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
        this.mMultimediaMessagingListeners.finishBroadcast();
    }

    public void broadcastStateChanged(ContactId contactId, String str, MultimediaSession.State state, MultimediaSession.ReasonCode reasonCode) {
        int beginBroadcast = this.mMultimediaMessagingListeners.beginBroadcast();
        for (int i = 0; i < beginBroadcast; i++) {
            try {
                this.mMultimediaMessagingListeners.getBroadcastItem(i).onStateChanged(contactId, str, state, reasonCode);
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
        this.mMultimediaMessagingListeners.finishBroadcast();
    }

    public void broadcastMessagesFlushed(ContactId contactId, String str) {
        int beginBroadcast = this.mMultimediaMessagingListeners.beginBroadcast();
        for (int i = 0; i < beginBroadcast; i++) {
            try {
                this.mMultimediaMessagingListeners.getBroadcastItem(i).onMessagesFlushed(contactId, str);
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
        this.mMultimediaMessagingListeners.finishBroadcast();
    }
}
