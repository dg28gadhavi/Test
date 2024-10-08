package com.sec.internal.ims.servicemodules.im;

import android.content.Context;
import android.database.Cursor;
import android.text.TextUtils;
import android.util.Log;
import com.sec.ims.options.Capabilities;
import com.sec.ims.options.CapabilityRefreshType;
import com.sec.ims.util.ImsUri;
import com.sec.internal.constants.ims.servicemodules.im.ImConstants;
import com.sec.internal.constants.ims.servicemodules.im.ImContract;
import com.sec.internal.constants.ims.servicemodules.im.ImDirection;
import com.sec.internal.constants.ims.servicemodules.im.NotificationStatus;
import com.sec.internal.constants.ims.servicemodules.im.event.ImComposingEvent;
import com.sec.internal.constants.ims.servicemodules.im.event.ImdnNotificationEvent;
import com.sec.internal.constants.ims.servicemodules.im.event.SendImdnFailedEvent;
import com.sec.internal.constants.ims.servicemodules.im.reason.CancelReason;
import com.sec.internal.ims.rcs.RcsPolicyManager;
import com.sec.internal.ims.registry.ImsRegistry;
import com.sec.internal.ims.servicemodules.im.data.event.ImSessionEvent;
import com.sec.internal.ims.servicemodules.im.listener.IChatEventListener;
import com.sec.internal.ims.servicemodules.im.listener.IFtEventListener;
import com.sec.internal.ims.servicemodules.im.listener.IMessageEventListener;
import com.sec.internal.ims.settings.RcsPolicySettings;
import com.sec.internal.interfaces.ims.servicemodules.options.ICapabilityDiscoveryModule;
import java.util.ArrayList;
import java.util.List;

public class ImdnHandler {
    private static final String LOG_TAG = "ImdnHandler";
    private ImCache mCache;
    private Context mContext;
    private FtProcessor mFtProcessor;
    private ImModule mImModule;
    private ImProcessor mImProcessor;
    private ImSessionProcessor mImSessionProcessor;

    public ImdnHandler(Context context, ImModule imModule, ImCache imCache, ImProcessor imProcessor, FtProcessor ftProcessor, ImSessionProcessor imSessionProcessor) {
        this.mContext = context;
        this.mImModule = imModule;
        this.mCache = imCache;
        this.mImProcessor = imProcessor;
        this.mFtProcessor = ftProcessor;
        this.mImSessionProcessor = imSessionProcessor;
    }

    /* access modifiers changed from: protected */
    public void readMessages(String str, List<String> list, boolean z) {
        String str2 = LOG_TAG;
        Log.i(str2, "readMessage: cid " + str + " index : " + list);
        int phoneIdByChatId = this.mImModule.getPhoneIdByChatId(str);
        this.mCache.readMessagesforCloudSync(phoneIdByChatId, list);
        if (z) {
            for (String message : list) {
                updateDbForReadMessage(this.mCache.getMessage(message, ImDirection.INCOMING, str));
            }
            return;
        }
        ImSession imSession = this.mCache.getImSession(str);
        if (imSession == null) {
            Log.e(str2, "readMessage: Session not found in the cache.");
            return;
        }
        ImDump imDump = this.mImModule.getImDump();
        imDump.addEventLogs("sendDisplayedNotification: chatId=" + imSession.getChatId() + ", convId=" + imSession.getConversationId() + ", contId=" + imSession.getContributionId() + ", imdnIds=" + list);
        List<MessageBase> messageListToRead = getMessageListToRead(str, list);
        if (!this.mImModule.isRegistered(phoneIdByChatId)) {
            Log.i(str2, "readMessage: not registered, mark status as displayed.");
            this.mCache.updateDesiredNotificationStatusAsDisplay(messageListToRead);
        } else if (!this.mImModule.getRcsStrategy().needToCapabilityCheckForImdn(imSession.isGroupChat()) || !handleReadMessageForNonRcs(phoneIdByChatId, imSession, messageListToRead)) {
            sendDisplayedNotification(imSession, messageListToRead);
        }
    }

    /* access modifiers changed from: protected */
    public void cancelMessages(String str, List<String> list) {
        String str2 = LOG_TAG;
        Log.i(str2, "cancelMessages: cid " + str + " imdnIds : " + list);
        int phoneIdByChatId = this.mImModule.getPhoneIdByChatId(str);
        this.mCache.cancelMessagesforCloudSync(phoneIdByChatId, list);
        ImSession imSession = this.mCache.getImSession(str);
        if (imSession == null) {
            Log.e(str2, "cancelMessages: Session not found in the cache.");
            onCancelMessagesFailed(str, list);
            return;
        }
        ImDump imDump = this.mImModule.getImDump();
        imDump.addEventLogs("sendCanceledNotification: conversationId=" + imSession.getConversationId() + ", imdnIds=" + list);
        if (this.mImModule.isRegistered(phoneIdByChatId)) {
            imSession.cancelMessages(list);
        } else {
            onCancelMessagesFailed(str, list);
        }
        this.mImSessionProcessor.getBigDataProcessor().onMessageCancelSent(imSession.getPhoneId(), list.size());
    }

    /* access modifiers changed from: protected */
    public void sendComposingNotification(String str, int i, boolean z) {
        String str2 = LOG_TAG;
        Log.i(str2, "sendComposingNotification: chatId=" + str + " typing=" + z + " interval=" + i);
        ImSession imSession = this.mCache.getImSession(str);
        if (imSession == null) {
            Log.e(str2, "Session not found in the cache.");
            return;
        }
        int phoneIdByIMSI = this.mImModule.getPhoneIdByIMSI(imSession.getChatData().getOwnIMSI());
        if (!this.mImModule.isRegistered(phoneIdByIMSI)) {
            Log.e(str2, "sendComposingNotification: not registered");
            return;
        }
        if (!imSession.isAutoAccept() && this.mImModule.getImConfig(phoneIdByIMSI).getImSessionStart() != ImConstants.ImSessionStart.WHEN_PRESSES_SEND_BUTTON) {
            imSession.acceptSession(false);
        }
        imSession.sendComposing(z, i);
    }

    /* access modifiers changed from: protected */
    public void onImdnNotificationReceived(ImdnNotificationEvent imdnNotificationEvent) {
        String str = LOG_TAG;
        Log.i(str, "onImdnNotificationReceived: " + imdnNotificationEvent);
        MessageBase message = this.mCache.getMessage(imdnNotificationEvent.mImdnId, ImDirection.OUTGOING, (String) null);
        if (message == null) {
            Log.e(str, "onImdnNotificationReceived: Couldn't find the im message.");
            return;
        }
        ImModule imModule = this.mImModule;
        ImsUri normalizeUri = imModule.normalizeUri(imModule.getPhoneIdByIMSI(message.getOwnIMSI()), imdnNotificationEvent.mRemoteUri);
        imdnNotificationEvent.mRemoteUri = normalizeUri;
        NotificationStatus notificationStatus = this.mCache.getNotificationStatus(imdnNotificationEvent.mImdnId, normalizeUri);
        if (!isValidImdnNotification(notificationStatus, imdnNotificationEvent.mStatus)) {
            Log.i(str, "onImdnNotificationReceived: ignore. current status=" + notificationStatus);
            return;
        }
        ImSession imSession = this.mCache.getImSession(message.getChatId());
        if (imSession == null) {
            Log.e(str, "onImdnNotificationReceived: Session not found.");
            return;
        }
        ImDump imDump = this.mImModule.getImDump();
        imDump.addEventLogs("onImdnNotificationReceived: chatId=" + imSession.getChatId() + ", convId=" + imSession.getConversationId() + ", contId=" + imSession.getContributionId() + ", imdnId=" + imdnNotificationEvent.mImdnId + ", status=" + imdnNotificationEvent.mStatus + ", remoteUri=" + imdnNotificationEvent.mRemoteUri.toStringLimit());
        int phoneIdByIMSI = this.mImModule.getPhoneIdByIMSI(imSession.getOwnImsi());
        boolean isGroupChat = imSession.isGroupChat();
        updateImdnStatusAndNotifyToListener(imdnNotificationEvent, imSession, isGroupChat, getMessagesForReceivedImdn(!imSession.isGroupChat() && RcsPolicyManager.getRcsStrategy(phoneIdByIMSI).boolSetting(RcsPolicySettings.RcsPolicy.USE_AGGREGATION_DISPLAYED_IMDN), imdnNotificationEvent.mStatus, imSession.getChatId(), message));
        if (!isGroupChat) {
            releaseLegacyLatching(imSession, phoneIdByIMSI, imdnNotificationEvent.mRemoteUri);
        }
        NotificationStatus notificationStatus2 = imdnNotificationEvent.mStatus;
        if ((notificationStatus2 == NotificationStatus.DELIVERED || notificationStatus2 == NotificationStatus.DISPLAYED) && !isGroupChat) {
            this.mImModule.updateServiceAvailability(imSession.getChatData().getOwnIMSI(), imdnNotificationEvent.mRemoteUri, imdnNotificationEvent.mCpimDate);
        }
        if (this.mImModule.getImConfig(phoneIdByIMSI).getRealtimeUserAliasAuth()) {
            imSession.updateParticipantAlias(imdnNotificationEvent.mUserAlias, imSession.getParticipant(imdnNotificationEvent.mRemoteUri));
        }
    }

    /* access modifiers changed from: protected */
    public void onCanceledNotificationReceived(ImdnNotificationEvent imdnNotificationEvent) {
        ImSession imSessionByConversationId;
        String str = LOG_TAG;
        Log.i(str, "onCanceledNotificationReceived: " + imdnNotificationEvent);
        String str2 = null;
        if (!TextUtils.isEmpty(imdnNotificationEvent.mConversationId) && !TextUtils.isEmpty(imdnNotificationEvent.mOwnImsi) && (imSessionByConversationId = this.mCache.getImSessionByConversationId(imdnNotificationEvent.mOwnImsi, imdnNotificationEvent.mConversationId, true)) != null) {
            str2 = imSessionByConversationId.getChatId();
        }
        MessageBase message = this.mCache.getMessage(imdnNotificationEvent.mImdnId, ImDirection.INCOMING, str2);
        if (message == null) {
            Log.e(str, "onCanceledNotificationReceived: Couldn't find the im message.");
        } else if (message.getLastNotificationType() == NotificationStatus.CANCELED) {
            Log.i(str, "onCanceledNotificationReceived: ignore. current status=" + message.getNotificationStatus());
        } else {
            ImSession imSession = this.mCache.getImSession(message.getChatId());
            if (imSession == null) {
                Log.e(str, "onCanceledNotificationReceived: Session not found.");
                return;
            }
            ImDump imDump = this.mImModule.getImDump();
            imDump.addEventLogs("onCanceledNotificationReceived: conversationId=" + imSession.getConversationId() + ", imdnId=" + imdnNotificationEvent.mImdnId + ", status=" + imdnNotificationEvent.mStatus);
            message.onCanceledNotificationReceived(imdnNotificationEvent);
            notifyCanceledNotificationToListener(imdnNotificationEvent, imSession, message);
            if (this.mImModule.getImConfig(imSession.getPhoneId()).getRealtimeUserAliasAuth()) {
                imSession.updateParticipantAlias(imdnNotificationEvent.mUserAlias, imSession.getParticipant(imdnNotificationEvent.mRemoteUri));
            }
        }
    }

    /* access modifiers changed from: protected */
    public void onComposingNotificationReceived(ImComposingEvent imComposingEvent) {
        String str;
        String str2 = LOG_TAG;
        Log.i(str2, "onComposingNotificationReceived: " + imComposingEvent);
        ImSession imSession = this.mCache.getImSession(imComposingEvent.mChatId);
        if (imSession == null) {
            Log.e(str2, "onComposingNotificationReceived: Session not found.");
            return;
        }
        int phoneIdByIMSI = this.mImModule.getPhoneIdByIMSI(imSession.getChatData().getOwnIMSI());
        ImsUri normalizeUri = this.mImModule.normalizeUri(ImsUri.parse(imComposingEvent.mUri));
        boolean isGroupChat = imSession.isGroupChat();
        if (!this.mImModule.isBlockedNumber(phoneIdByIMSI, normalizeUri, isGroupChat)) {
            imSession.receiveComposingNotification(imComposingEvent);
            if (!isGroupChat) {
                releaseLegacyLatching(imSession, phoneIdByIMSI, normalizeUri);
            }
            if (this.mImModule.getImConfig(phoneIdByIMSI).getUserAliasEnabled()) {
                str = imComposingEvent.mUserAlias;
                if (this.mImModule.getImConfig(phoneIdByIMSI).getRealtimeUserAliasAuth() && isGroupChat) {
                    imSession.updateParticipantAlias(str, imSession.getParticipant(normalizeUri));
                }
            } else {
                str = "";
            }
            String str3 = str;
            for (IChatEventListener onComposingNotificationReceived : this.mImSessionProcessor.mChatEventListeners) {
                onComposingNotificationReceived.onComposingNotificationReceived(imComposingEvent.mChatId, imSession.isGroupChat(), normalizeUri, str3, imComposingEvent.mIsComposing, imComposingEvent.mInterval);
            }
        }
    }

    /* access modifiers changed from: protected */
    public void onSendImdnFailed(SendImdnFailedEvent sendImdnFailedEvent) {
        String str = LOG_TAG;
        Log.i(str, "onSendImdnFailed: " + sendImdnFailedEvent);
        MessageBase message = this.mCache.getMessage(sendImdnFailedEvent.mImdnId, ImDirection.INCOMING, sendImdnFailedEvent.mChatId);
        if (message == null) {
            Log.e(str, "onSendImdnFailed: Message not found.");
            return;
        }
        ImSession imSession = this.mCache.getImSession(sendImdnFailedEvent.mChatId);
        if (imSession == null) {
            Log.e(str, "onSendImdnFailed: Session not found.");
        } else {
            imSession.onSendImdnFailed(sendImdnFailedEvent, message);
        }
    }

    private void updateDbForReadMessage(MessageBase messageBase) {
        if (messageBase == null) {
            return;
        }
        if ((messageBase instanceof FtHttpIncomingMessage) || messageBase.getStatus() != ImConstants.Status.FAILED) {
            if (messageBase.getStatus() == ImConstants.Status.CANCELLATION_UNREAD) {
                messageBase.updateStatus(ImConstants.Status.CANCELLATION);
            } else {
                messageBase.updateStatus(ImConstants.Status.READ);
            }
            messageBase.updateDisplayedTimestamp(System.currentTimeMillis());
            NotificationStatus notificationStatus = NotificationStatus.DISPLAYED;
            messageBase.updateDesiredNotificationStatus(notificationStatus);
            messageBase.updateNotificationStatus(notificationStatus);
            return;
        }
        String str = LOG_TAG;
        Log.e(str, "Do not update message with status FAILED: messageId" + messageBase.getId());
    }

    private List<MessageBase> getMessageListToRead(String str, List<String> list) {
        ArrayList arrayList = new ArrayList();
        for (String message : list) {
            MessageBase message2 = this.mCache.getMessage(message, ImDirection.INCOMING, str);
            if (message2 != null) {
                arrayList.add(message2);
            }
        }
        arrayList.sort(new ImdnHandler$$ExternalSyntheticLambda0());
        return arrayList;
    }

    /* access modifiers changed from: private */
    public static /* synthetic */ int lambda$getMessageListToRead$0(MessageBase messageBase, MessageBase messageBase2) {
        int i = ((messageBase.getInsertedTimestamp() - messageBase2.getInsertedTimestamp()) > 0 ? 1 : ((messageBase.getInsertedTimestamp() - messageBase2.getInsertedTimestamp()) == 0 ? 0 : -1));
        if (i == 0) {
            if (messageBase.getId() < messageBase2.getId()) {
                return -1;
            }
            return 1;
        } else if (i < 0) {
            return -1;
        } else {
            return 1;
        }
    }

    private boolean handleReadMessageForNonRcs(int i, ImSession imSession, List<MessageBase> list) {
        ICapabilityDiscoveryModule capabilityDiscoveryModule = ImsRegistry.getServiceModuleManager().getCapabilityDiscoveryModule();
        if (capabilityDiscoveryModule == null) {
            return false;
        }
        Capabilities capabilities = capabilityDiscoveryModule.getCapabilities(imSession.getParticipantsUri().iterator().next(), CapabilityRefreshType.ONLY_IF_NOT_FRESH, i);
        if (capabilities == null) {
            Log.i(LOG_TAG, "readMessage: cap is null");
            return false;
        } else if (!capabilities.hasFeature(Capabilities.FEATURE_NON_RCS_USER) || imSession.isEstablishedState()) {
            return false;
        } else {
            for (MessageBase next : list) {
                next.updateDesiredNotificationStatus(NotificationStatus.DISPLAYED);
                next.onSendDisplayedNotificationDone();
            }
            return true;
        }
    }

    private void sendDisplayedNotification(ImSession imSession, List<MessageBase> list) {
        boolean isRespondDisplay = imSession.isRespondDisplay();
        for (MessageBase next : list) {
            if ((next instanceof FtHttpIncomingMessage) || next.getStatus() != ImConstants.Status.FAILED) {
                if (next.getStatus() == ImConstants.Status.CANCELLATION_UNREAD) {
                    next.updateStatus(ImConstants.Status.CANCELLATION);
                } else {
                    next.updateStatus(ImConstants.Status.READ);
                }
                next.updateDisplayedTimestamp(System.currentTimeMillis());
                if (next.isDisplayedNotificationRequired() && isRespondDisplay) {
                    next.updateDesiredNotificationStatus(NotificationStatus.DISPLAYED);
                    imSession.mMessagesToSendDisplayNotification.add(next);
                }
            } else {
                String str = LOG_TAG;
                Log.e(str, "Do not update message with status FAILED: " + next.getId());
            }
        }
        if (!imSession.mMessagesToSendDisplayNotification.isEmpty()) {
            imSession.sendMessage(imSession.obtainMessage(ImSessionEvent.SEND_DISPLAYED_NOTIFICATION));
        }
    }

    private boolean isValidImdnNotification(NotificationStatus notificationStatus, NotificationStatus notificationStatus2) {
        if (notificationStatus == null || notificationStatus == NotificationStatus.DISPLAYED) {
            return false;
        }
        NotificationStatus notificationStatus3 = NotificationStatus.DELIVERED;
        return (notificationStatus == notificationStatus3 && notificationStatus2 == notificationStatus3) ? false : true;
    }

    private List<MessageBase> getMessagesForReceivedImdn(boolean z, NotificationStatus notificationStatus, String str, MessageBase messageBase) {
        ArrayList arrayList = new ArrayList();
        if (!z || notificationStatus != NotificationStatus.DISPLAYED) {
            arrayList.add(messageBase);
        } else {
            List<String> messageIdsForDisplayAggregation = this.mCache.getMessageIdsForDisplayAggregation(str, ImDirection.OUTGOING, Long.valueOf(messageBase.getDeliveredTimestamp()));
            messageIdsForDisplayAggregation.remove(String.valueOf(messageBase.getId()));
            if (!messageIdsForDisplayAggregation.isEmpty()) {
                arrayList.addAll(this.mCache.getMessages(messageIdsForDisplayAggregation));
            }
            arrayList.add(messageBase);
            if (arrayList.size() > 1) {
                arrayList.sort(new ImdnHandler$$ExternalSyntheticLambda1());
            }
        }
        return arrayList;
    }

    /* access modifiers changed from: private */
    public static /* synthetic */ int lambda$getMessagesForReceivedImdn$1(MessageBase messageBase, MessageBase messageBase2) {
        return messageBase.getId() - messageBase2.getId() < 0 ? -1 : 1;
    }

    private void updateRevocationStatus(ImSession imSession, MessageBase messageBase) {
        if (imSession.getNeedToRevokeMessages().containsKey(messageBase.getImdnId())) {
            messageBase.updateRevocationStatus(ImConstants.RevocationStatus.NONE);
            imSession.removeMsgFromListForRevoke(messageBase.getImdnId());
            this.mCache.removeFromPendingList(messageBase.getId());
        }
    }

    private void updateImdnStatusAndNotifyToListener(ImdnNotificationEvent imdnNotificationEvent, ImSession imSession, boolean z, List<MessageBase> list) {
        for (MessageBase next : list) {
            next.onImdnNotificationReceived(imdnNotificationEvent);
            updateRevocationStatus(imSession, next);
            String str = null;
            Cursor queryMessages = this.mCache.queryMessages(new String[]{ImContract.Message.MSG_CREATOR}, "imdn_message_id= ?", new String[]{imdnNotificationEvent.mImdnId}, (String) null);
            if (queryMessages != null) {
                try {
                    if (queryMessages.moveToNext()) {
                        str = queryMessages.getString(queryMessages.getColumnIndexOrThrow(ImContract.Message.MSG_CREATOR));
                    }
                } catch (Throwable th) {
                    th.addSuppressed(th);
                }
            }
            if (queryMessages != null) {
                queryMessages.close();
            }
            next.setMessageCreator(str);
            if (next instanceof ImMessage) {
                for (IMessageEventListener onImdnNotificationReceived : this.mImProcessor.getMessageEventListener(next.getType())) {
                    onImdnNotificationReceived.onImdnNotificationReceived(next, imdnNotificationEvent.mRemoteUri, next.getLastNotificationType(), z);
                }
            } else if (next instanceof FtMessage) {
                for (IFtEventListener onImdnNotificationReceived2 : this.mFtProcessor.getFtEventListener(next.getType())) {
                    onImdnNotificationReceived2.onImdnNotificationReceived((FtMessage) next, imdnNotificationEvent.mRemoteUri, next.getLastNotificationType(), z);
                }
            }
        }
        return;
        throw th;
    }

    private void releaseLegacyLatching(ImSession imSession, int i, ImsUri imsUri) {
        this.mImSessionProcessor.setLegacyLatching(imSession.getRemoteUri(), false, imSession.getChatData().getOwnIMSI());
        this.mImModule.getLatchingProcessor().removeUriFromLatchingList(imsUri, i);
    }

    private void onCancelMessagesFailed(String str, List<String> list) {
        for (IMessageEventListener next : this.mImProcessor.getMessageEventListener(ImConstants.Type.TEXT)) {
            for (String onCancelMessageResponse : list) {
                next.onCancelMessageResponse(str, onCancelMessageResponse, false);
            }
        }
    }

    private void notifyCanceledNotificationToListener(ImdnNotificationEvent imdnNotificationEvent, ImSession imSession, MessageBase messageBase) {
        if (messageBase instanceof ImMessage) {
            for (IMessageEventListener onImdnNotificationReceived : this.mImProcessor.getMessageEventListener(messageBase.getType())) {
                onImdnNotificationReceived.onImdnNotificationReceived(messageBase, imdnNotificationEvent.mRemoteUri, messageBase.getLastNotificationType(), imSession.isGroupChat());
            }
        } else if (messageBase instanceof FtMessage) {
            FtMessage ftMessage = (FtMessage) messageBase;
            int stateId = ftMessage.getStateId();
            if (stateId == 2) {
                ftMessage.cancelTransfer(CancelReason.CANCELED_NOTIFICATION);
            }
            if (stateId != 0) {
                this.mFtProcessor.onImdnNotificationReceived(ftMessage, imdnNotificationEvent.mRemoteUri, messageBase.getLastNotificationType(), imSession.isGroupChat());
            }
        }
    }
}
