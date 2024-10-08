package com.sec.internal.ims.servicemodules.im;

import android.os.Message;
import com.sec.internal.constants.ims.servicemodules.im.event.ImIncomingSessionEvent;
import com.sec.internal.constants.ims.servicemodules.im.event.ImSessionClosedEvent;
import com.sec.internal.constants.ims.servicemodules.im.event.ImSessionConferenceInfoUpdateEvent;
import com.sec.internal.ims.servicemodules.im.ImSession;
import com.sec.internal.ims.servicemodules.im.data.event.ImSessionEvent;
import com.sec.internal.ims.servicemodules.im.data.info.ImSessionInfo;
import java.util.ArrayList;

public class ImSessionClosingState extends ImSessionStateBase {
    private static final String LOG_TAG = "ClosingState";

    ImSessionClosingState(int i, ImSession imSession) {
        super(i, imSession);
    }

    public void enter() {
        ImSession imSession = this.mImSession;
        imSession.logi("ClosingState enter. " + this.mImSession.getChatId());
        ImSession imSession2 = this.mImSession;
        imSession2.mListener.onChatStatusUpdate(imSession2, ImSession.SessionState.CLOSING);
    }

    /* access modifiers changed from: protected */
    public boolean processMessagingEvent(Message message) {
        ImSession imSession = this.mImSession;
        imSession.logi("ClosingState, processMessagingEvent: " + message.what + " ChatId: " + this.mImSession.getChatId());
        int i = message.what;
        if (i == 3001) {
            this.mImSession.deferMessage(message);
        } else if (i == 3010) {
            onSendDeliveredNotification(message);
        } else if (i == 3012) {
            onSendDisplayedNotification(message);
        } else if (i != 3025) {
            return false;
        } else {
            this.mImSession.deferMessage(message);
        }
        return true;
    }

    /* access modifiers changed from: protected */
    public boolean processGroupChatManagementEvent(Message message) {
        ImSession imSession = this.mImSession;
        imSession.logi("ClosingState, processGroupChatManagementEvent: " + message.what + " ChatId: " + this.mImSession.getChatId());
        int i = message.what;
        if (!(i == 2001 || i == 2008 || i == 2010 || i == 2012 || i == 2014)) {
            if (i == 2005) {
                this.mImSession.onConferenceInfoUpdated((ImSessionConferenceInfoUpdateEvent) message.obj);
                return true;
            } else if (i != 2006) {
                return false;
            }
        }
        this.mImSession.deferMessage(message);
        return true;
    }

    /* access modifiers changed from: protected */
    public boolean processSessionConnectionEvent(Message message) {
        ImSession imSession = this.mImSession;
        imSession.logi("ClosingState, processSessionConnectionEvent: " + message.what + " ChatId: " + this.mImSession.getChatId());
        int i = message.what;
        if (i == 1001) {
            this.mImSession.deferMessage(message);
        } else if (i != 1005) {
            switch (i) {
                case 1012:
                    return onCloseAllSession(message);
                case 1013:
                    this.mImSession.mClosedState.onCloseSessionDone(message);
                    break;
                case 1014:
                    this.mImSession.mClosedState.onSessionClosed((ImSessionClosedEvent) message.obj);
                    break;
                default:
                    return false;
            }
        } else {
            onProcessIncomingSession(message);
        }
        return true;
    }

    private void onProcessIncomingSession(Message message) {
        if (this.mImSession.isVoluntaryDeparture()) {
            this.mImSession.logi("Explicit departure is in progress. Reject the incoming invite");
            this.mImSession.leaveSessionWithReject(((ImIncomingSessionEvent) message.obj).mRawHandle);
            return;
        }
        this.mImSession.deferMessage(message);
    }

    private void onSendDeliveredNotification(Message message) {
        MessageBase messageBase = (MessageBase) message.obj;
        ImSessionInfo imSessionInfoByMessageId = this.mImSession.getImSessionInfoByMessageId(messageBase.getId());
        if (imSessionInfoByMessageId == null || !imSessionInfoByMessageId.isSnFSession()) {
            this.mImSession.deferMessage(message);
        } else {
            messageBase.sendDeliveredNotification(imSessionInfoByMessageId.mState == ImSessionInfo.ImSessionState.ESTABLISHED ? imSessionInfoByMessageId.mRawHandle : null, this.mImSession.getConversationId(), this.mImSession.getContributionId(), this.mImSession.obtainMessage((int) ImSessionEvent.SEND_DELIVERED_NOTIFICATION_DONE, (Object) messageBase), this.mImSession.getChatData().getOwnIMSI(), this.mImSession.isGroupChat(), this.mImSession.isBotSessionAnonymized());
        }
    }

    private void onSendDisplayedNotification(Message message) {
        synchronized (this.mImSession.mMessagesToSendDisplayNotification) {
            for (MessageBase messageBase : new ArrayList(this.mImSession.mMessagesToSendDisplayNotification)) {
                ImSessionInfo imSessionInfoByMessageId = this.mImSession.getImSessionInfoByMessageId(messageBase.getId());
                if (imSessionInfoByMessageId != null && imSessionInfoByMessageId.isSnFSession()) {
                    messageBase.sendDisplayedNotification(imSessionInfoByMessageId.mState == ImSessionInfo.ImSessionState.ESTABLISHED ? imSessionInfoByMessageId.mRawHandle : null, this.mImSession.getConversationId(), this.mImSession.getContributionId(), this.mImSession.obtainMessage((int) ImSessionEvent.SEND_DISPLAYED_NOTIFICATION_DONE, (Object) messageBase.toList()), this.mImSession.getChatData().getOwnIMSI(), this.mImSession.isGroupChat(), this.mImSession.isBotSessionAnonymized());
                    this.mImSession.mMessagesToSendDisplayNotification.remove(messageBase);
                }
            }
            if (!this.mImSession.mMessagesToSendDisplayNotification.isEmpty()) {
                this.mImSession.deferMessage(message);
            }
        }
    }

    private boolean onCloseAllSession(Message message) {
        if (!this.mImSession.isVoluntaryDeparture()) {
            return false;
        }
        this.mImSession.logi("Voluntary departure in ClosingState. DeferMessage");
        this.mImSession.deferMessage(message);
        return true;
    }
}
