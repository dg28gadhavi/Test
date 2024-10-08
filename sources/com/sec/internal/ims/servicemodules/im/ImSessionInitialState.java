package com.sec.internal.ims.servicemodules.im;

import android.os.Message;
import android.text.TextUtils;
import android.util.Log;
import com.sec.internal.constants.ims.SipMsg;
import com.sec.internal.constants.ims.servicemodules.im.ImError;
import com.sec.internal.constants.ims.servicemodules.im.event.ImIncomingMessageEvent;
import com.sec.internal.constants.ims.servicemodules.im.event.ImIncomingSessionEvent;
import com.sec.internal.constants.ims.servicemodules.im.params.RejectImSessionParams;
import com.sec.internal.constants.ims.servicemodules.im.reason.ImErrorReason;
import com.sec.internal.constants.ims.servicemodules.im.reason.ImSessionRejectReason;
import com.sec.internal.constants.ims.servicemodules.im.result.RejectImSessionResult;
import com.sec.internal.helper.AsyncResult;
import com.sec.internal.helper.FileUtils;
import com.sec.internal.ims.servicemodules.im.ImSession;
import com.sec.internal.ims.servicemodules.im.data.info.ImSessionInfo;
import java.util.ArrayList;
import java.util.List;

public class ImSessionInitialState extends ImSessionStateBase {
    private static final String LOG_TAG = "InitialState";

    ImSessionInitialState(int i, ImSession imSession) {
        super(i, imSession);
    }

    public void enter() {
        ImSession imSession = this.mImSession;
        imSession.logi("InitialState enter. " + this.mImSession.getChatId());
        ImSession imSession2 = this.mImSession;
        imSession2.mListener.onChatStatusUpdate(imSession2, ImSession.SessionState.INITIAL);
    }

    /* access modifiers changed from: protected */
    public boolean processMessagingEvent(Message message) {
        ImSession imSession = this.mImSession;
        imSession.logi("InitialState, processMessagingEvent: " + message.what + " ChatId: " + this.mImSession.getChatId());
        int i = message.what;
        if (i == 3001) {
            onSendMessage((MessageBase) message.obj);
        } else if (i != 3025) {
            return false;
        } else {
            onSendCanceledNotification((List) message.obj);
        }
        return true;
    }

    /* access modifiers changed from: protected */
    public boolean processGroupChatManagementEvent(Message message) {
        ImSession imSession = this.mImSession;
        imSession.logi("InitialState, processGroupChatManagementEvent: " + message.what + " ChatId: " + this.mImSession.getChatId());
        int i = message.what;
        if (i == 2001) {
            onAddParticipants(message);
        } else if (i == 2006) {
            onRemoveParticipants(message);
        } else if (i == 2008) {
            onChangeGCLeader(message);
        } else if (i == 2010) {
            onChangeGCSubject(message);
        } else if (i == 2012) {
            onChangeGroupAlias(message);
        } else if (i != 2014) {
            return false;
        } else {
            onChangeGCIcon(message);
        }
        return true;
    }

    /* access modifiers changed from: protected */
    public boolean processSessionConnectionEvent(Message message) {
        ImSession imSession = this.mImSession;
        imSession.logi("InitialState, processSessionConnectionEvent: " + message.what + " ChatId: " + this.mImSession.getChatId());
        int i = message.what;
        if (i == 1001) {
            onSendMessage((MessageBase) message.obj);
            return true;
        } else if (i == 1012) {
            return onCloseAllSession();
        } else {
            if (i == 1005) {
                onProcessIncomingSession((ImIncomingSessionEvent) message.obj);
                return true;
            } else if (i == 1006) {
                onAcceptSession(((Boolean) message.obj).booleanValue());
                return true;
            } else if (i == 1020) {
                this.mImSession.mStartingState.onStartSession((MessageBase) null, ImSessionInfo.StartingReason.AUTOMATIC_REJOINING, false);
                return true;
            } else if (i != 1021) {
                switch (i) {
                    case 1008:
                        onRejectSession((Integer) message.obj);
                        return true;
                    case 1009:
                        onRejectSessionDone((RejectImSessionResult) ((AsyncResult) message.obj).result);
                        return true;
                    case 1010:
                        onProcessIncomingSnfSession((ImIncomingSessionEvent) message.obj);
                        return true;
                    default:
                        return false;
                }
            } else {
                this.mImSession.mStartingState.onStartSession((MessageBase) null, ImSessionInfo.StartingReason.AUTOMATIC_REJOINING, true);
                return true;
            }
        }
    }

    public ImSessionInfo getPendingSessionInfoByType(ImSessionInfo.SessionType sessionType) {
        for (ImSessionInfo next : this.mImSession.mImSessionInfoList) {
            if (next.mState == ImSessionInfo.ImSessionState.PENDING_INVITE && next.mSessionType == sessionType) {
                return next;
            }
        }
        return null;
    }

    private void onRejectSession(Integer num) {
        ImSessionRejectReason imSessionRejectReason;
        if (!this.mImSession.mIncomingMessageEvents.isEmpty()) {
            this.mImSession.logi("REJECT_SESSION: discard pended incoming message events");
            this.mImSession.mIncomingMessageEvents.clear();
        }
        if (this.mImSession.isChatbotManualAcceptUsed()) {
            Log.i(LOG_TAG, "chatbotRejectReason=" + num);
            imSessionRejectReason = ImSessionRejectReason.CHATBOT_PROFILE_RETRIEVAL_FAIL;
        } else if (this.mImSession.isGroupChat()) {
            imSessionRejectReason = ImSessionRejectReason.VOLUNTARILY;
        } else {
            imSessionRejectReason = ImSessionRejectReason.BUSY_HERE;
        }
        if (!TextUtils.isEmpty(this.mImSession.getServiceId()) && (this.mImSession.getServiceId().equalsIgnoreCase(SipMsg.FEATURE_TAG_ENRICHED_SHARED_SKETCH) || this.mImSession.getServiceId().equalsIgnoreCase(SipMsg.FEATURE_TAG_ENRICHED_SHARED_MAP))) {
            imSessionRejectReason = ImSessionRejectReason.VOLUNTARILY;
        }
        for (ImSessionInfo imSessionInfo : new ArrayList(this.mImSession.mImSessionInfoList)) {
            if (imSessionInfo != null && imSessionInfo.mState == ImSessionInfo.ImSessionState.PENDING_INVITE) {
                this.mImSession.removeImSessionInfo(imSessionInfo);
                this.mImSession.mImsService.rejectImSession(new RejectImSessionParams(this.mImSession.getChatId(), imSessionInfo.mRawHandle, imSessionRejectReason, this.mImSession.obtainMessage(1009)));
            }
        }
    }

    private void onSendMessage(MessageBase messageBase) {
        for (ImSessionInfo next : this.mImSession.mImSessionInfoList) {
            if (next.mState == ImSessionInfo.ImSessionState.PENDING_INVITE) {
                this.mImSession.handleAcceptSession(next);
            }
        }
        if (this.mImSession.hasActiveImSessionInfo()) {
            if (messageBase != null) {
                this.mImSession.mCurrentMessages.add(messageBase);
            }
            this.mImSession.transitionToProperState();
            return;
        }
        if (messageBase != null && this.mImSession.mConfig.getChatRevokeTimer() > 0) {
            this.mImSession.setNetworkFallbackMech(ImSession.ChatFallbackMech.MESSAGE_REVOCATION);
        }
        this.mImSession.mStartingState.onStartSession(messageBase, ImSessionInfo.StartingReason.NORMAL, false);
    }

    private void onSendCanceledNotification(List<MessageBase> list) {
        for (ImSessionInfo next : this.mImSession.mImSessionInfoList) {
            if (next.mState == ImSessionInfo.ImSessionState.PENDING_INVITE) {
                this.mImSession.handleAcceptSession(next);
            }
        }
        this.mImSession.mCurrentCanceledMessages.addAll(list);
        if (this.mImSession.hasActiveImSessionInfo()) {
            this.mImSession.transitionToProperState();
        } else {
            this.mImSession.mStartingState.onStartSession((MessageBase) null, ImSessionInfo.StartingReason.NORMAL, false);
        }
    }

    private void onProcessIncomingSession(ImIncomingSessionEvent imIncomingSessionEvent) {
        ImSessionInfo pendingSessionInfoByType;
        ImSession imSession = this.mImSession;
        imSession.mIsOfflineGCInvitation = imIncomingSessionEvent.mIsSendOnly;
        ImIncomingMessageEvent imIncomingMessageEvent = null;
        imSession.mClosedEvent = null;
        if (imSession.isVoluntaryDeparture()) {
            this.mImSession.logi("User already left the chat voluntarily. Reject the invite");
            this.mImSession.leaveSessionWithReject(imIncomingSessionEvent.mRawHandle);
            return;
        }
        ImSession imSession2 = this.mImSession;
        ImSessionInfo.ImSessionState imSessionState = ImSessionInfo.ImSessionState.PENDING_INVITE;
        boolean hasImSessionInfo = imSession2.hasImSessionInfo((Object) imSessionState);
        if (imIncomingSessionEvent.mFromBlocked) {
            ImIncomingMessageEvent imIncomingMessageEvent2 = imIncomingSessionEvent.mReceivedMessage;
            if (imIncomingMessageEvent2 != null && !TextUtils.isEmpty(imIncomingMessageEvent2.mBody)) {
                imIncomingMessageEvent2.mChatId = this.mImSession.getChatId();
                this.mImSession.mListener.onBlockedMessageReceived(imIncomingMessageEvent2);
            }
            ImSession imSession3 = this.mImSession;
            imSession3.mImsService.rejectImSession(new RejectImSessionParams(imSession3.getChatId(), imIncomingSessionEvent.mRawHandle, ImSessionRejectReason.BUSY_HERE, (Message) null));
        } else {
            if ((this.mImSession.isChatbotManualAcceptUsed() || (!this.mImSession.isAutoAccept() && !this.mImSession.isGroupChat())) && (pendingSessionInfoByType = getPendingSessionInfoByType(ImSessionInfo.SessionType.NORMAL)) != null) {
                ImSession imSession4 = this.mImSession;
                imSession4.logi("Reject previous invite rawHandle = " + pendingSessionInfoByType.mRawHandle);
                this.mImSession.removeImSessionInfo(pendingSessionInfoByType.mRawHandle);
                ImSession imSession5 = this.mImSession;
                imSession5.mImsService.rejectImSession(new RejectImSessionParams(imSession5.getChatId(), pendingSessionInfoByType.mRawHandle, ImSessionRejectReason.BUSY_HERE, (Message) null));
            }
            ImSessionInfo addImSessionInfo = this.mImSession.addImSessionInfo(imIncomingSessionEvent, imSessionState);
            this.mImSession.updateSessionInfo(addImSessionInfo);
            if (imIncomingSessionEvent.mReceivedMessage != null && this.mImSession.isChatbotManualAcceptUsed()) {
                this.mImSession.logi("Pending first message in INVITE from Chatbot");
                this.mImSession.mIncomingMessageEvents.add(imIncomingSessionEvent.mReceivedMessage);
            }
            if (!this.mImSession.isChatbotManualAcceptUsed() && this.mImSession.isAutoAccept() && !this.mImSession.needToUseGroupChatInvitationUI() && TextUtils.isEmpty(this.mImSession.getServiceId())) {
                this.mImSession.handleAcceptSession(addImSessionInfo);
                this.mImSession.transitionToStartingState();
            }
            ImSession imSession6 = this.mImSession;
            if (!imSession6.isChatbotManualAcceptUsed()) {
                imIncomingMessageEvent = imIncomingSessionEvent.mReceivedMessage;
            }
            imSession6.onIncomingSessionProcessed(imIncomingMessageEvent, !hasImSessionInfo && !this.mImSession.mIsBlockedIncomingSession);
        }
        this.mImSession.releaseWakeLock(imIncomingSessionEvent.mRawHandle);
    }

    private void onProcessIncomingSnfSession(ImIncomingSessionEvent imIncomingSessionEvent) {
        ImSessionInfo pendingSessionInfoByType;
        ImSession imSession = this.mImSession;
        ImSessionInfo.ImSessionState imSessionState = ImSessionInfo.ImSessionState.PENDING_INVITE;
        boolean hasImSessionInfo = imSession.hasImSessionInfo((Object) imSessionState);
        ImIncomingMessageEvent imIncomingMessageEvent = null;
        if (imIncomingSessionEvent.mFromBlocked) {
            ImIncomingMessageEvent imIncomingMessageEvent2 = imIncomingSessionEvent.mReceivedMessage;
            if (imIncomingMessageEvent2 != null && !TextUtils.isEmpty(imIncomingMessageEvent2.mBody)) {
                imIncomingMessageEvent2.mChatId = this.mImSession.getChatId();
                this.mImSession.mListener.onBlockedMessageReceived(imIncomingMessageEvent2);
            }
            ImSession imSession2 = this.mImSession;
            imSession2.mImsService.rejectImSession(new RejectImSessionParams(imSession2.getChatId(), imIncomingSessionEvent.mRawHandle, ImSessionRejectReason.BUSY_HERE, (Message) null));
            return;
        }
        if ((this.mImSession.isChatbotManualAcceptUsed() || (!this.mImSession.isAutoAccept() && !this.mImSession.isGroupChat())) && (pendingSessionInfoByType = getPendingSessionInfoByType(ImSessionInfo.SessionType.SNF_SESSION)) != null) {
            ImSession imSession3 = this.mImSession;
            imSession3.logi("Reject previous invite rawHandle = " + pendingSessionInfoByType.mRawHandle);
            this.mImSession.removeImSessionInfo(pendingSessionInfoByType.mRawHandle);
            ImSession imSession4 = this.mImSession;
            imSession4.mImsService.rejectImSession(new RejectImSessionParams(imSession4.getChatId(), pendingSessionInfoByType.mRawHandle, ImSessionRejectReason.BUSY_HERE, (Message) null));
        }
        ImSessionInfo addImSessionInfo = this.mImSession.addImSessionInfo(imIncomingSessionEvent, imSessionState);
        if (imIncomingSessionEvent.mReceivedMessage != null && this.mImSession.isChatbotManualAcceptUsed()) {
            this.mImSession.logi("Pending first message in INVITE from Chatbot");
            this.mImSession.mIncomingMessageEvents.add(imIncomingSessionEvent.mReceivedMessage);
        }
        if ((!this.mImSession.isChatbotManualAcceptUsed() && this.mImSession.isAutoAccept() && !this.mImSession.needToUseGroupChatInvitationUI()) || imIncomingSessionEvent.mIsForStoredNoti) {
            this.mImSession.handleAcceptSession(addImSessionInfo);
        }
        ImSession imSession5 = this.mImSession;
        if (!imSession5.isChatbotManualAcceptUsed()) {
            imIncomingMessageEvent = imIncomingSessionEvent.mReceivedMessage;
        }
        imSession5.onIncomingSessionProcessed(imIncomingMessageEvent, !imIncomingSessionEvent.mIsForStoredNoti && !hasImSessionInfo && !this.mImSession.mIsBlockedIncomingSession);
    }

    private void onAcceptSession(boolean z) {
        ImSession imSession = this.mImSession;
        imSession.logi("ACCEPT_SESSION, explicit=" + z + ", mImSessionInfoList.size()=" + this.mImSession.mImSessionInfoList.size() + ", isGroupChat()=" + this.mImSession.isGroupChat() + ", isRejoinable()=" + this.mImSession.isRejoinable() + ", isChatbotRole()=" + this.mImSession.isChatbotRole());
        if (!this.mImSession.mIncomingMessageEvents.isEmpty()) {
            this.mImSession.logi("ACCEPT_SESSION: process pended incoming message events");
            for (ImIncomingMessageEvent onIncomingMessageProcessed : this.mImSession.mIncomingMessageEvents) {
                ImSession imSession2 = this.mImSession;
                imSession2.mListener.onIncomingMessageProcessed(onIncomingMessageProcessed, imSession2);
            }
            this.mImSession.mIncomingMessageEvents.clear();
        }
        if (!z || !this.mImSession.mImSessionInfoList.isEmpty() || !this.mImSession.isGroupChat()) {
            for (ImSessionInfo next : this.mImSession.mImSessionInfoList) {
                if (next.mState == ImSessionInfo.ImSessionState.PENDING_INVITE) {
                    this.mImSession.handleAcceptSession(next);
                }
            }
            this.mImSession.transitionToProperState();
            return;
        }
        this.mImSession.mStartingState.onStartSession((MessageBase) null, ImSessionInfo.StartingReason.NORMAL, false);
    }

    private void onRejectSessionDone(RejectImSessionResult rejectImSessionResult) {
        if (rejectImSessionResult.mError != ImError.SUCCESS) {
            ImSession imSession = this.mImSession;
            imSession.loge("Failed to reject session:" + rejectImSessionResult.mError);
        }
    }

    private void onChangeGroupAlias(Message message) {
        if (message.arg1 == 1) {
            this.mImSession.onChangeGroupAliasFailed((String) message.obj, ImErrorReason.ENGINE_ERROR);
            return;
        }
        message.arg1 = 1;
        this.mImSession.deferMessage(message);
        this.mImSession.mStartingState.onStartSession((MessageBase) null, ImSessionInfo.StartingReason.NORMAL, false);
    }

    private void onChangeGCLeader(Message message) {
        if (message.arg1 == 1) {
            this.mImSession.onChangeGroupChatLeaderFailed((List) message.obj, ImErrorReason.ENGINE_ERROR);
            return;
        }
        message.arg1 = 1;
        this.mImSession.deferMessage(message);
        this.mImSession.mStartingState.onStartSession((MessageBase) null, ImSessionInfo.StartingReason.NORMAL, false);
    }

    private void onChangeGCSubject(Message message) {
        if (message.arg1 == 1) {
            this.mImSession.onChangeGroupChatSubjectFailed((String) message.obj, ImErrorReason.ENGINE_ERROR);
            return;
        }
        message.arg1 = 1;
        this.mImSession.deferMessage(message);
        this.mImSession.mStartingState.onStartSession((MessageBase) null, ImSessionInfo.StartingReason.NORMAL, false);
    }

    private void onChangeGCIcon(Message message) {
        if (message.arg1 == 1) {
            this.mImSession.onChangeGroupChatIconFailed(FileUtils.getFileNameFromPath((String) message.obj), ImErrorReason.ENGINE_ERROR);
            return;
        }
        message.arg1 = 1;
        this.mImSession.deferMessage(message);
        this.mImSession.mStartingState.onStartSession((MessageBase) null, ImSessionInfo.StartingReason.NORMAL, false);
    }

    private void onAddParticipants(Message message) {
        if (message.arg1 == 1) {
            this.mImSession.onAddParticipantsFailed((List) message.obj, ImErrorReason.ENGINE_ERROR);
            return;
        }
        message.arg1 = 1;
        this.mImSession.deferMessage(message);
        this.mImSession.mStartingState.onStartSession((MessageBase) null, ImSessionInfo.StartingReason.NORMAL, false);
    }

    private void onRemoveParticipants(Message message) {
        if (message.arg1 == 1) {
            this.mImSession.onRemoveParticipantsFailed((List) message.obj, ImErrorReason.ENGINE_ERROR);
            return;
        }
        message.arg1 = 1;
        this.mImSession.deferMessage(message);
        this.mImSession.mStartingState.onStartSession((MessageBase) null, ImSessionInfo.StartingReason.NORMAL, false);
    }

    private boolean onCloseAllSession() {
        if (!this.mImSession.isVoluntaryDeparture() || this.mImSession.getParticipantsSize() <= 0) {
            return false;
        }
        this.mImSession.mStartingState.onStartSession((MessageBase) null, ImSessionInfo.StartingReason.NORMAL, true);
        return true;
    }
}
