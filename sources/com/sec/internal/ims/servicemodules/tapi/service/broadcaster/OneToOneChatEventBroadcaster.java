package com.sec.internal.ims.servicemodules.tapi.service.broadcaster;

import android.content.Context;
import android.content.Intent;
import android.os.RemoteCallbackList;
import android.os.UserHandle;
import android.util.Log;
import com.gsma.services.rcs.chat.ChatLog;
import com.gsma.services.rcs.chat.IOneToOneChatListener;
import com.gsma.services.rcs.contact.ContactId;
import com.sec.internal.ims.servicemodules.csh.event.ICshConstants;
import com.sec.internal.log.IMSLog;
import java.util.ArrayList;
import java.util.Set;

public class OneToOneChatEventBroadcaster implements IOneToOneChatEventBroadcaster {
    private static final String LOG_TAG = "OneToOneChatEventBroadcaster";
    private Context mContext;
    private final RemoteCallbackList<IOneToOneChatListener> mOneToOneChatListeners = new RemoteCallbackList<>();

    public OneToOneChatEventBroadcaster(Context context) {
        this.mContext = context;
    }

    public void addOneToOneChatEventListener(IOneToOneChatListener iOneToOneChatListener) {
        this.mOneToOneChatListeners.register(iOneToOneChatListener);
    }

    public void removeOneToOneChatEventListener(IOneToOneChatListener iOneToOneChatListener) {
        this.mOneToOneChatListeners.unregister(iOneToOneChatListener);
    }

    public void broadcastMessageStatusChanged(ContactId contactId, String str, String str2, ChatLog.Message.Content.Status status, ChatLog.Message.Content.ReasonCode reasonCode) {
        Log.d(LOG_TAG, "start : broadcastMessageStatusChanged()");
        int beginBroadcast = this.mOneToOneChatListeners.beginBroadcast();
        for (int i = 0; i < beginBroadcast; i++) {
            try {
                this.mOneToOneChatListeners.getBroadcastItem(i).onMessageStatusChanged(contactId, str, str2, status, reasonCode);
            } catch (Exception e) {
                e.printStackTrace();
                String str3 = LOG_TAG;
                Log.e(str3, "Can't notify listener : " + e);
            }
        }
        this.mOneToOneChatListeners.finishBroadcast();
    }

    public void broadcastComposingEvent(ContactId contactId, boolean z) {
        Log.d(LOG_TAG, "start : broadcastComposingEvent()");
        int beginBroadcast = this.mOneToOneChatListeners.beginBroadcast();
        for (int i = 0; i < beginBroadcast; i++) {
            try {
                this.mOneToOneChatListeners.getBroadcastItem(i).onComposingEvent(contactId, z);
            } catch (Exception e) {
                String str = LOG_TAG;
                Log.e(str, "Can't notify listener : " + e);
            }
        }
        this.mOneToOneChatListeners.finishBroadcast();
    }

    public void broadcastMessageDeleted(String str, Set<String> set) {
        Log.d(LOG_TAG, "start : broadcastComposingEvent()");
        ContactId contactId = new ContactId(str);
        ArrayList arrayList = new ArrayList(set);
        int beginBroadcast = this.mOneToOneChatListeners.beginBroadcast();
        for (int i = 0; i < beginBroadcast; i++) {
            try {
                this.mOneToOneChatListeners.getBroadcastItem(i).onMessagesDeleted(contactId, arrayList);
            } catch (Exception e) {
                String str2 = LOG_TAG;
                Log.e(str2, "Can't notify listener : " + e);
            }
        }
        this.mOneToOneChatListeners.finishBroadcast();
    }

    public void broadcastMessageReceived(String str, String str2, String str3, UserHandle userHandle) {
        String str4 = LOG_TAG;
        Log.d(str4, "start : broadcastMessageReceived() msgId:" + str + ",mimeType:" + str2 + ",contact:" + IMSLog.checker(str3));
        Intent intent = new Intent("com.gsma.services.rcs.chat.action.NEW_ONE_TO_ONE_CHAT_MESSAGE");
        intent.putExtra("messageId", str);
        intent.putExtra("mimeType", str2);
        intent.putExtra(ICshConstants.ShareDatabase.KEY_TARGET_CONTACT, str3);
        this.mContext.sendBroadcastAsUser(intent, userHandle, "com.gsma.services.permission.RCS");
    }
}
