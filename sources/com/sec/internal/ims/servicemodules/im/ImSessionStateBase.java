package com.sec.internal.ims.servicemodules.im;

import android.os.Message;
import com.sec.internal.helper.State;

public abstract class ImSessionStateBase extends State {
    ImSession mImSession;
    int mPhoneId;

    /* access modifiers changed from: protected */
    public boolean processGroupChatManagementEvent(Message message) {
        return false;
    }

    /* access modifiers changed from: protected */
    public boolean processMessagingEvent(Message message) {
        return false;
    }

    /* access modifiers changed from: protected */
    public boolean processSessionConnectionEvent(Message message) {
        return false;
    }

    ImSessionStateBase(int i, ImSession imSession) {
        this.mPhoneId = i;
        this.mImSession = imSession;
    }

    public boolean processMessage(Message message) {
        int i = message.what;
        if (i > 3000) {
            return processMessagingEvent(message);
        }
        if (i > 2000) {
            return processGroupChatManagementEvent(message);
        }
        return processSessionConnectionEvent(message);
    }
}
