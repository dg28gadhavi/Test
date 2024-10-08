package com.sec.internal.ims.servicemodules.im;

import android.os.Message;
import android.util.Log;
import com.sec.ims.options.Capabilities;
import com.sec.ims.options.CapabilityRefreshType;
import com.sec.ims.util.ImsUri;
import com.sec.internal.constants.ims.servicemodules.im.ImConstants;
import com.sec.internal.constants.ims.servicemodules.im.ImDirection;
import com.sec.internal.constants.ims.servicemodules.im.ImError;
import com.sec.internal.constants.ims.servicemodules.im.ImIconData;
import com.sec.internal.constants.ims.servicemodules.im.ImParticipant;
import com.sec.internal.constants.ims.servicemodules.im.NotificationStatus;
import com.sec.internal.constants.ims.servicemodules.im.SipResponse;
import com.sec.internal.constants.ims.servicemodules.im.event.ImIncomingSessionEvent;
import com.sec.internal.constants.ims.servicemodules.im.event.ImSessionClosedEvent;
import com.sec.internal.constants.ims.servicemodules.im.event.ImSessionConferenceInfoUpdateEvent;
import com.sec.internal.constants.ims.servicemodules.im.event.ImSessionEstablishedEvent;
import com.sec.internal.constants.ims.servicemodules.im.params.SendImdnParams;
import com.sec.internal.constants.ims.servicemodules.im.params.StopImSessionParams;
import com.sec.internal.constants.ims.servicemodules.im.reason.ImErrorReason;
import com.sec.internal.constants.ims.servicemodules.im.reason.ImSessionClosedReason;
import com.sec.internal.constants.ims.servicemodules.im.reason.ImSessionStopReason;
import com.sec.internal.constants.ims.servicemodules.im.result.Result;
import com.sec.internal.constants.ims.servicemodules.im.result.SendMessageResult;
import com.sec.internal.constants.ims.servicemodules.im.result.SendSlmResult;
import com.sec.internal.constants.ims.servicemodules.im.result.StartImSessionResult;
import com.sec.internal.helper.AsyncResult;
import com.sec.internal.helper.PreciseAlarmManager;
import com.sec.internal.helper.Preconditions;
import com.sec.internal.ims.core.sim.SimManagerFactory;
import com.sec.internal.ims.registry.ImsRegistry;
import com.sec.internal.ims.servicemodules.im.data.event.ImSessionEvent;
import com.sec.internal.ims.servicemodules.im.data.info.ImSessionInfo;
import com.sec.internal.ims.servicemodules.im.strategy.DefaultRCSMnoStrategy;
import com.sec.internal.ims.servicemodules.im.strategy.IMnoStrategy;
import com.sec.internal.ims.settings.RcsPolicySettings;
import com.sec.internal.ims.util.ChatbotUriUtil;
import com.sec.internal.interfaces.ims.servicemodules.options.ICapabilityDiscoveryModule;
import com.sec.internal.log.IMSLog;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Date;
import java.util.EnumSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

public class ImSessionDefaultState extends ImSessionStateBase {
    private static final String LOG_TAG = "DefaultState";
    private boolean mIsTriggeredCapex;

    ImSessionDefaultState(int i, ImSession imSession) {
        super(i, imSession);
    }

    public boolean processMessage(Message message) {
        boolean processMessage = super.processMessage(message);
        if (!processMessage) {
            ImSession imSession = this.mImSession;
            imSession.loge("Unexpected event " + message.what + ". current state is " + this.mImSession.getCurrentState().getName());
        }
        return processMessage;
    }

    /* access modifiers changed from: protected */
    public boolean processMessagingEvent(Message message) {
        ImSession imSession = this.mImSession;
        imSession.logi("DefaultState, processMessagingEvent: " + message.what + " ChatId: " + this.mImSession.getChatId());
        int i = message.what;
        if (i == 3023) {
            this.mImSession.mIsComposing = false;
        } else if (i != 3026) {
            switch (i) {
                case ImSessionEvent.SEND_MESSAGE_DONE /*3002*/:
                    onSendImMessageDone((AsyncResult) message.obj);
                    break;
                case ImSessionEvent.RECEIVE_MESSAGE /*3003*/:
                case ImSessionEvent.RECEIVE_SLM_MESSAGE /*3009*/:
                    onReceiveMessage((MessageBase) message.obj);
                    break;
                case ImSessionEvent.ATTACH_FILE /*3004*/:
                    onAttachFile(message);
                    break;
                case ImSessionEvent.SEND_FILE /*3005*/:
                    onSendFile(message);
                    break;
                case ImSessionEvent.FILE_COMPLETE /*3006*/:
                    onFileComplete((FtMessage) message.obj);
                    break;
                case ImSessionEvent.SEND_SLM_MESSAGE /*3007*/:
                    this.mImSession.onSendSlmMessage((MessageBase) message.obj);
                    break;
                case ImSessionEvent.SEND_SLM_MESSAGE_DONE /*3008*/:
                    onSendSlmMessageDone((AsyncResult) message.obj);
                    break;
                case ImSessionEvent.SEND_DELIVERED_NOTIFICATION /*3010*/:
                    onSendDeliveredNodification((MessageBase) message.obj);
                    break;
                case ImSessionEvent.SEND_DELIVERED_NOTIFICATION_DONE /*3011*/:
                    onSendDeliveredNodificationDone(message);
                    break;
                case ImSessionEvent.SEND_DISPLAYED_NOTIFICATION /*3012*/:
                    onSendDisplayedNotification();
                    break;
                case ImSessionEvent.SEND_DISPLAYED_NOTIFICATION_DONE /*3013*/:
                    onSendDisplayedNotificationDone(message);
                    break;
                case ImSessionEvent.SEND_MESSAGE_RESPONSE_TIMEOUT /*3014*/:
                    onSendMessageResponseTimeout((ImMessage) message.obj);
                    break;
                case ImSessionEvent.DELIVERY_TIMEOUT /*3015*/:
                    onExpireDeliveryTimeout();
                    break;
                case ImSessionEvent.SEND_MESSAGE_REVOKE_REQUEST /*3016*/:
                    onSendMessageRevokeRequest((List) message.obj);
                    break;
                case ImSessionEvent.SEND_MESSAGE_REVOKE_REQUEST_INTERNAL_DONE /*3017*/:
                    onSendMessageRevokeRequestInternalDone(message);
                    break;
                case ImSessionEvent.MESSAGE_REVOKE_TIMER_EXPIRED /*3018*/:
                    onMessageRevokeTimerExpired();
                    break;
                case ImSessionEvent.MESSAGE_REVOKE_OPERATION_TIMEOUT /*3019*/:
                    onMessageRevokeOperationTimeout((String) message.obj);
                    break;
                case ImSessionEvent.RESEND_MESSAGE_REVOKE_REQUEST /*3020*/:
                    onResendMessageRevokeRequest();
                    break;
                case ImSessionEvent.SEND_ISCOMPOSING_NOTIFICATION /*3021*/:
                    onSendIscomposingNotification();
                    break;
                default:
                    return false;
            }
        } else {
            onSendCanceledNotificationDone(message);
        }
        return true;
    }

    /* access modifiers changed from: protected */
    public boolean processGroupChatManagementEvent(Message message) {
        ImSession imSession = this.mImSession;
        imSession.logi("DefaultState, processGroupChatManagementEvent: " + message.what + " ChatId: " + this.mImSession.getChatId());
        int i = message.what;
        if (i == 2016) {
            onDownloadGroupIconDone((ImIconData) message.obj);
        } else if (i != 2017) {
            switch (i) {
                case ImSessionEvent.EXTEND_TO_GROUP_CHAT /*2003*/:
                    this.mImSession.deferMessage(message);
                    break;
                case ImSessionEvent.EXTEND_TO_GROUP_CHAT_DONE /*2004*/:
                    onExtendToGroupChatDone(message);
                    break;
                case ImSessionEvent.CONFERENCE_INFO_UPDATED /*2005*/:
                    this.mImSession.onConferenceInfoUpdated((ImSessionConferenceInfoUpdateEvent) message.obj);
                    break;
                default:
                    return false;
            }
        } else {
            this.mImSession.handleRequestTimeout();
        }
        return true;
    }

    /* access modifiers changed from: protected */
    public boolean processSessionConnectionEvent(Message message) {
        ImSession imSession = this.mImSession;
        imSession.logi("DefaultState, processSessionConnectionEvent: " + message.what + " ChatId: " + this.mImSession.getChatId());
        int i = message.what;
        if (i == 1003) {
            onSessionEstablished((ImSessionEstablishedEvent) message.obj);
        } else if (i == 1019) {
            onSessionTimeoutThreshold((ImMessage) message.obj);
        } else if (i == 1022) {
            ImSession imSession2 = this.mImSession;
            imSession2.logi("REFRESH_CAPEX_UPDATE. current state is " + this.mImSession.getCurrentState().getName());
            this.mIsTriggeredCapex = false;
        } else if (i != 1023) {
            switch (i) {
                case 1010:
                    onProcessIncomingSnfSession((ImIncomingSessionEvent) message.obj);
                    break;
                case 1011:
                    onAcceptSnfSessionDone(message);
                    break;
                case 1012:
                    onCloseAllSession((ImSessionStopReason) message.obj);
                    break;
                default:
                    switch (i) {
                        case 1014:
                            this.mImSession.mClosedState.onSessionClosed((ImSessionClosedEvent) message.obj);
                            break;
                        case 1015:
                            onForceCloseSession();
                            break;
                        case 1016:
                            onStartSessionProvisionalResponse(message);
                            break;
                        case 1017:
                            onStartSessionSynchronousDone(message);
                            break;
                        default:
                            return false;
                    }
            }
        } else {
            onCloseSessionTimeout(message.obj);
        }
        return true;
    }

    private void onExtendToGroupChatDone(Message message) {
        AsyncResult asyncResult = (AsyncResult) message.obj;
        List list = (List) asyncResult.userObj;
        if (((StartImSessionResult) asyncResult.result).mResult.getImError() == ImError.SUCCESS) {
            this.mImSession.onAddParticipantsSucceeded(list);
        } else {
            this.mImSession.onAddParticipantsFailed(list, ImErrorReason.ENGINE_ERROR);
        }
        this.mImSession.transitionToProperState();
    }

    private void onSendImMessageDone(AsyncResult asyncResult) {
        Object obj;
        if (asyncResult.exception != null || (obj = asyncResult.result) == null) {
            this.mImSession.loge("result is null");
            return;
        }
        MessageBase messageBase = (MessageBase) asyncResult.userObj;
        SendMessageResult sendMessageResult = (SendMessageResult) obj;
        this.mImSession.removeMessages(1019, messageBase);
        if (!sendMessageResult.mIsProvisional || this.mImSession.getRcsStrategy(this.mPhoneId).boolSetting(RcsPolicySettings.RcsPolicy.USE_PROVISIONAL_RESPONSE_ASSENT)) {
            ImError imError = sendMessageResult.mResult.getImError();
            boolean z = !this.mImSession.isGroupChat() && ChatbotUriUtil.hasChatbotUri(this.mImSession.getParticipantsUri(), this.mPhoneId);
            ImSession imSession = this.mImSession;
            imSession.logi("onSendImMessageDone : " + imError + " retryTimer: " + this.mImSession.mRetryTimer + " hasChatbotUri: " + z);
            setRevokeTimer(messageBase, z, sendMessageResult.mResult);
            ImError imError2 = ImError.SUCCESS;
            if (imError == imError2) {
                messageBase.onSendMessageDone(sendMessageResult.mResult, new IMnoStrategy.StrategyResponse(IMnoStrategy.StatusCode.NONE));
                return;
            }
            MessageBase message = this.mImSession.mGetter.getMessage(messageBase.getId());
            if (message != null) {
                if (message.getNotificationStatus() != NotificationStatus.NONE) {
                    this.mImSession.logi("onSendImMessageDone : msg has already been delivered successfully");
                    messageBase.onSendMessageDone(new Result(imError2, sendMessageResult.mResult), new IMnoStrategy.StrategyResponse(IMnoStrategy.StatusCode.NONE));
                    return;
                } else if (message.getStatus() == ImConstants.Status.FAILED) {
                    ImSession imSession2 = this.mImSession;
                    imSession2.loge("onSendImMessageDone : Message Id " + messageBase.getId() + " had been failed");
                    return;
                }
            }
            if (this.mImSession.isGroupChat() || imError != ImError.FORBIDDEN_CHATBOT_CONVERSATION_NEEDED) {
                IMnoStrategy rcsStrategy = this.mImSession.getRcsStrategy(this.mPhoneId);
                int currentRetryCount = messageBase.getCurrentRetryCount();
                ImSession imSession3 = this.mImSession;
                IMnoStrategy.StrategyResponse handleSendingMessageFailure = rcsStrategy.handleSendingMessageFailure(imError, currentRetryCount, imSession3.mRetryTimer, imSession3.getChatType(), false, z, messageBase instanceof FtHttpOutgoingMessage);
                IMnoStrategy.StatusCode statusCode = handleSendingMessageFailure.getStatusCode();
                if (messageBase.getType() == ImConstants.Type.LOCATION && statusCode == IMnoStrategy.StatusCode.FALLBACK_TO_SLM) {
                    this.mImSession.logi("onSendImMessageDone : GLS fallback to legacy");
                    statusCode = IMnoStrategy.StatusCode.FALLBACK_TO_LEGACY;
                }
                this.mImSession.getRcsStrategy(this.mPhoneId).forceRefreshCapability(this.mImSession.getParticipantsUri(), false, imError);
                if (shouldCloseSession(imError) || this.mImSession.getRcsStrategy(this.mPhoneId).isCloseSessionNeeded(imError)) {
                    this.mImSession.mClosedState.handleCloseSession(sendMessageResult.mRawHandle, ImSessionStopReason.INVOLUNTARILY);
                    this.mImSession.transitionToProperState();
                }
                handleSendImResult(handleSendingMessageFailure, messageBase, sendMessageResult);
                ImSession imSession4 = this.mImSession;
                imSession4.logi("onSendImMessageDone - msgId: " + messageBase.mId + " statusCode: " + statusCode);
                return;
            }
            this.mImSession.loge("onStartSessionDone : chatbot conversation needed");
            this.mImSession.updateIsChatbotRole(true);
            ChatbotUriUtil.updateChatbotCapability(this.mPhoneId, this.mImSession.getRemoteUri(), true);
            if (message != null) {
                this.mImSession.sendImMessage(message);
            }
        }
    }

    private void onReceiveMessage(MessageBase messageBase) {
        Preconditions.checkNotNull(messageBase, "msg cannot be null");
        IMSLog.s(LOG_TAG, "onReceiveImMessage: " + messageBase);
        messageBase.updateStatus(ImConstants.Status.UNREAD);
        messageBase.updateDeliveredTimestamp(System.currentTimeMillis());
        if (messageBase.isDeliveredNotificationRequired()) {
            messageBase.updateDesiredNotificationStatus(NotificationStatus.DELIVERED);
            ImSession imSession = this.mImSession;
            imSession.sendMessage(imSession.obtainMessage((int) ImSessionEvent.SEND_DELIVERED_NOTIFICATION, (Object) messageBase));
        }
        if (!this.mImSession.mIsBlockedIncomingSession) {
            if (messageBase instanceof ImMessage) {
                ((ImMessage) messageBase).onReceived();
            } else if (messageBase instanceof FtHttpIncomingMessage) {
                ((FtHttpIncomingMessage) messageBase).receiveTransfer();
            }
            if (this.mImSession.getComposingActiveUris().remove(messageBase.mRemoteUri)) {
                ImSession imSession2 = this.mImSession;
                imSession2.mListener.onComposingReceived(imSession2, messageBase.mRemoteUri, (String) null, false, imSession2.mComposingNotificationInterval);
            }
        }
    }

    private void onFileComplete(FtMessage ftMessage) {
        Preconditions.checkNotNull(ftMessage);
        ImSession imSession = this.mImSession;
        imSession.logi("onFileComplete: mProcessingFileTransfer size: " + this.mImSession.mProcessingFileTransfer.size() + ", mPendingFileTrasfer size: " + this.mImSession.mPendingFileTransfer.size());
        if (ftMessage instanceof FtHttpOutgoingMessage) {
            ftMessage.updateStatus(ImConstants.Status.TO_SEND);
            ImSession imSession2 = this.mImSession;
            imSession2.mListener.onRequestSendMessage(imSession2, ftMessage);
            return;
        }
        boolean remove = this.mImSession.mProcessingFileTransfer.remove(ftMessage);
        ImSession imSession3 = this.mImSession;
        imSession3.logi("onFileComplete isRemoved: " + remove + ", mProcessingFileTransfer size: " + this.mImSession.mProcessingFileTransfer.size());
        if (!remove) {
            boolean remove2 = this.mImSession.mPendingFileTransfer.remove(ftMessage);
            ImSession imSession4 = this.mImSession;
            imSession4.logi("onFileComplete isRemoved: " + remove2 + ", mPendingFileTransfer size: " + this.mImSession.mPendingFileTransfer.size());
        }
        if (this.mImSession.mProcessingFileTransfer.isEmpty()) {
            this.mImSession.logi("onFileComplete next send file");
            FtMessage removeNextFtMessage = removeNextFtMessage();
            if (removeNextFtMessage != null) {
                if (removeNextFtMessage.getFtCallback() == null) {
                    removeNextFtMessage.setFtCompleteCallback(this.mImSession.obtainMessage(ImSessionEvent.FILE_COMPLETE));
                }
                if (this.mImSession.isGroupChat() && !this.mImSession.isBroadcastMsg(removeNextFtMessage)) {
                    ImSession imSession5 = this.mImSession;
                    imSession5.sendMessage(imSession5.obtainMessage(1001));
                }
                ImSession imSession6 = this.mImSession;
                imSession6.sendMessage(imSession6.obtainMessage((int) ImSessionEvent.SEND_FILE, (Object) removeNextFtMessage));
                this.mImSession.addToProcessingFileTransfer(removeNextFtMessage);
                return;
            }
            ImSession imSession7 = this.mImSession;
            imSession7.mListener.onProcessingFileTransferChanged(imSession7);
        }
    }

    private void onForceCloseSession() {
        if (this.mImSession.isRejoinable() && !this.mImSession.mEstablishedImSessionInfo.isEmpty()) {
            this.mImSession.mClosedReason = ImSessionClosedReason.CLOSED_INVOLUNTARILY;
        }
        IMnoStrategy rcsStrategy = this.mImSession.getRcsStrategy(this.mPhoneId);
        if (rcsStrategy == null) {
            rcsStrategy = new DefaultRCSMnoStrategy(this.mImSession.getContext(), SimManagerFactory.getPhoneId(this.mImSession.getOwnImsi()));
        }
        this.mImSession.handleCloseAllSession(rcsStrategy.getSessionStopReason(this.mImSession.isGroupChat()));
        this.mImSession.mImSessionInfoList.clear();
        this.mImSession.mEstablishedImSessionInfo.clear();
        this.mImSession.transitionToProperState();
    }

    private boolean updateParticipantWithPAI(MessageBase messageBase, String str) {
        if (str == null || str.isEmpty()) {
            return false;
        }
        Log.i(LOG_TAG, "updateParticipantWithPAI, sipNumber = " + IMSLog.numberChecker(str));
        ImsUri parse = ImsUri.parse(str);
        if (parse == null || parse.equals(ImsUri.EMPTY)) {
            return false;
        }
        ArrayList arrayList = new ArrayList();
        ArrayList arrayList2 = new ArrayList();
        arrayList.add(new ImParticipant(this.mImSession.getChatId(), ImParticipant.Status.INITIAL, parse));
        arrayList2.addAll(this.mImSession.getParticipants());
        ImSession imSession = this.mImSession;
        imSession.mListener.onParticipantsInserted(imSession, arrayList);
        ImSession imSession2 = this.mImSession;
        imSession2.mListener.onParticipantsDeleted(imSession2, arrayList2);
        messageBase.updateRemoteUri(parse);
        return true;
    }

    private void onSendSlmMessageDone(AsyncResult asyncResult) {
        SendSlmResult sendSlmResult = (SendSlmResult) asyncResult.result;
        Result result = sendSlmResult.mResult;
        ImError imError = result.getImError();
        MessageBase messageBase = (MessageBase) asyncResult.userObj;
        this.mImSession.removeMessages(1019, messageBase);
        if (imError == ImError.SUCCESS) {
            if (this.mImSession.getRcsStrategy(this.mPhoneId).boolSetting(RcsPolicySettings.RcsPolicy.CHECK_P_ASSERTED_IDENTITY)) {
                updateParticipantWithPAI(messageBase, sendSlmResult.mPAssertedIdentity);
            }
            messageBase.onSendMessageDone(result, new IMnoStrategy.StrategyResponse(IMnoStrategy.StatusCode.NONE));
            return;
        }
        MessageBase pendingMessage = this.mImSession.mGetter.getPendingMessage(messageBase.getId());
        if (pendingMessage == null) {
            this.mImSession.logi("onSendSlmMessageDone: No message in pending message list. Ignore.");
        } else if (imError == ImError.FORBIDDEN_CHATBOT_CONVERSATION_NEEDED) {
            this.mImSession.loge("onSendSlmMessageDone : chatbot conversation needed");
            if (this.mImSession.getRcsStrategy(this.mPhoneId).boolSetting(RcsPolicySettings.RcsPolicy.CHECK_P_ASSERTED_IDENTITY) && updateParticipantWithPAI(pendingMessage, sendSlmResult.mPAssertedIdentity)) {
                pendingMessage.incrementRetryCount();
            }
            this.mImSession.updateIsChatbotRole(true);
            ChatbotUriUtil.updateChatbotCapability(this.mPhoneId, this.mImSession.getRemoteUri(), true);
            ImSession imSession = this.mImSession;
            imSession.sendMessage(imSession.obtainMessage((int) ImSessionEvent.SEND_SLM_MESSAGE, (Object) pendingMessage));
        } else {
            IMnoStrategy rcsStrategy = this.mImSession.getRcsStrategy(this.mPhoneId);
            int currentRetryCount = pendingMessage.getCurrentRetryCount();
            ImSession imSession2 = this.mImSession;
            IMnoStrategy.StrategyResponse handleSendingMessageFailure = rcsStrategy.handleSendingMessageFailure(imError, currentRetryCount, imSession2.mRetryTimer, imSession2.getChatType(), true, pendingMessage instanceof FtHttpOutgoingMessage);
            if (AnonymousClass1.$SwitchMap$com$sec$internal$ims$servicemodules$im$strategy$IMnoStrategy$StatusCode[handleSendingMessageFailure.getStatusCode().ordinal()] != 1) {
                pendingMessage.onSendMessageDone(result, handleSendingMessageFailure);
                return;
            }
            ImSession imSession3 = this.mImSession;
            imSession3.logi("onSendSlmMessageDone retry msgId : " + pendingMessage.getId());
            pendingMessage.incrementRetryCount();
            ImSession imSession4 = this.mImSession;
            imSession4.sendMessage(imSession4.obtainMessage((int) ImSessionEvent.SEND_SLM_MESSAGE, (Object) pendingMessage));
        }
    }

    /* renamed from: com.sec.internal.ims.servicemodules.im.ImSessionDefaultState$1  reason: invalid class name */
    static /* synthetic */ class AnonymousClass1 {
        static final /* synthetic */ int[] $SwitchMap$com$sec$internal$ims$servicemodules$im$strategy$IMnoStrategy$StatusCode;

        /* JADX WARNING: Can't wrap try/catch for region: R(14:0|1|2|3|4|5|6|7|8|9|10|11|12|(3:13|14|16)) */
        /* JADX WARNING: Can't wrap try/catch for region: R(16:0|1|2|3|4|5|6|7|8|9|10|11|12|13|14|16) */
        /* JADX WARNING: Failed to process nested try/catch */
        /* JADX WARNING: Missing exception handler attribute for start block: B:11:0x003e */
        /* JADX WARNING: Missing exception handler attribute for start block: B:13:0x0049 */
        /* JADX WARNING: Missing exception handler attribute for start block: B:3:0x0012 */
        /* JADX WARNING: Missing exception handler attribute for start block: B:5:0x001d */
        /* JADX WARNING: Missing exception handler attribute for start block: B:7:0x0028 */
        /* JADX WARNING: Missing exception handler attribute for start block: B:9:0x0033 */
        static {
            /*
                com.sec.internal.ims.servicemodules.im.strategy.IMnoStrategy$StatusCode[] r0 = com.sec.internal.ims.servicemodules.im.strategy.IMnoStrategy.StatusCode.values()
                int r0 = r0.length
                int[] r0 = new int[r0]
                $SwitchMap$com$sec$internal$ims$servicemodules$im$strategy$IMnoStrategy$StatusCode = r0
                com.sec.internal.ims.servicemodules.im.strategy.IMnoStrategy$StatusCode r1 = com.sec.internal.ims.servicemodules.im.strategy.IMnoStrategy.StatusCode.RETRY_IMMEDIATE     // Catch:{ NoSuchFieldError -> 0x0012 }
                int r1 = r1.ordinal()     // Catch:{ NoSuchFieldError -> 0x0012 }
                r2 = 1
                r0[r1] = r2     // Catch:{ NoSuchFieldError -> 0x0012 }
            L_0x0012:
                int[] r0 = $SwitchMap$com$sec$internal$ims$servicemodules$im$strategy$IMnoStrategy$StatusCode     // Catch:{ NoSuchFieldError -> 0x001d }
                com.sec.internal.ims.servicemodules.im.strategy.IMnoStrategy$StatusCode r1 = com.sec.internal.ims.servicemodules.im.strategy.IMnoStrategy.StatusCode.SUCCESS     // Catch:{ NoSuchFieldError -> 0x001d }
                int r1 = r1.ordinal()     // Catch:{ NoSuchFieldError -> 0x001d }
                r2 = 2
                r0[r1] = r2     // Catch:{ NoSuchFieldError -> 0x001d }
            L_0x001d:
                int[] r0 = $SwitchMap$com$sec$internal$ims$servicemodules$im$strategy$IMnoStrategy$StatusCode     // Catch:{ NoSuchFieldError -> 0x0028 }
                com.sec.internal.ims.servicemodules.im.strategy.IMnoStrategy$StatusCode r1 = com.sec.internal.ims.servicemodules.im.strategy.IMnoStrategy.StatusCode.RETRY_AFTER     // Catch:{ NoSuchFieldError -> 0x0028 }
                int r1 = r1.ordinal()     // Catch:{ NoSuchFieldError -> 0x0028 }
                r2 = 3
                r0[r1] = r2     // Catch:{ NoSuchFieldError -> 0x0028 }
            L_0x0028:
                int[] r0 = $SwitchMap$com$sec$internal$ims$servicemodules$im$strategy$IMnoStrategy$StatusCode     // Catch:{ NoSuchFieldError -> 0x0033 }
                com.sec.internal.ims.servicemodules.im.strategy.IMnoStrategy$StatusCode r1 = com.sec.internal.ims.servicemodules.im.strategy.IMnoStrategy.StatusCode.RETRY_AFTER_SESSION     // Catch:{ NoSuchFieldError -> 0x0033 }
                int r1 = r1.ordinal()     // Catch:{ NoSuchFieldError -> 0x0033 }
                r2 = 4
                r0[r1] = r2     // Catch:{ NoSuchFieldError -> 0x0033 }
            L_0x0033:
                int[] r0 = $SwitchMap$com$sec$internal$ims$servicemodules$im$strategy$IMnoStrategy$StatusCode     // Catch:{ NoSuchFieldError -> 0x003e }
                com.sec.internal.ims.servicemodules.im.strategy.IMnoStrategy$StatusCode r1 = com.sec.internal.ims.servicemodules.im.strategy.IMnoStrategy.StatusCode.FALLBACK_TO_SLM     // Catch:{ NoSuchFieldError -> 0x003e }
                int r1 = r1.ordinal()     // Catch:{ NoSuchFieldError -> 0x003e }
                r2 = 5
                r0[r1] = r2     // Catch:{ NoSuchFieldError -> 0x003e }
            L_0x003e:
                int[] r0 = $SwitchMap$com$sec$internal$ims$servicemodules$im$strategy$IMnoStrategy$StatusCode     // Catch:{ NoSuchFieldError -> 0x0049 }
                com.sec.internal.ims.servicemodules.im.strategy.IMnoStrategy$StatusCode r1 = com.sec.internal.ims.servicemodules.im.strategy.IMnoStrategy.StatusCode.DISPLAY_ERROR     // Catch:{ NoSuchFieldError -> 0x0049 }
                int r1 = r1.ordinal()     // Catch:{ NoSuchFieldError -> 0x0049 }
                r2 = 6
                r0[r1] = r2     // Catch:{ NoSuchFieldError -> 0x0049 }
            L_0x0049:
                int[] r0 = $SwitchMap$com$sec$internal$ims$servicemodules$im$strategy$IMnoStrategy$StatusCode     // Catch:{ NoSuchFieldError -> 0x0054 }
                com.sec.internal.ims.servicemodules.im.strategy.IMnoStrategy$StatusCode r1 = com.sec.internal.ims.servicemodules.im.strategy.IMnoStrategy.StatusCode.RETRY_AFTER_REGI     // Catch:{ NoSuchFieldError -> 0x0054 }
                int r1 = r1.ordinal()     // Catch:{ NoSuchFieldError -> 0x0054 }
                r2 = 7
                r0[r1] = r2     // Catch:{ NoSuchFieldError -> 0x0054 }
            L_0x0054:
                return
            */
            throw new UnsupportedOperationException("Method not decompiled: com.sec.internal.ims.servicemodules.im.ImSessionDefaultState.AnonymousClass1.<clinit>():void");
        }
    }

    private void triggerCapex() {
        ImSession imSession = this.mImSession;
        imSession.logi(getName() + "triggerCapex");
        if (!this.mIsTriggeredCapex && !this.mImSession.isGroupChat() && !this.mImSession.getParticipantsUri().isEmpty()) {
            this.mIsTriggeredCapex = true;
            ICapabilityDiscoveryModule capabilityDiscoveryModule = ImsRegistry.getServiceModuleManager().getCapabilityDiscoveryModule();
            Capabilities capabilities = capabilityDiscoveryModule != null ? capabilityDiscoveryModule.getCapabilities(this.mImSession.getParticipantsUri().iterator().next(), CapabilityRefreshType.ONLY_IF_NOT_FRESH_IN_MSG_CTX, SimManagerFactory.getPhoneId(this.mImSession.getChatData().getOwnIMSI())) : null;
            long msgCapValidityTime = ((long) this.mImSession.mConfig.getMsgCapValidityTime()) * 1000;
            if (capabilities != null) {
                long time = new Date().getTime() - capabilities.getTimestamp().getTime();
                if (time < msgCapValidityTime) {
                    msgCapValidityTime = time;
                }
            }
            ImSession imSession2 = this.mImSession;
            imSession2.logi("SEND_ISCOMPOSING_NOTIFICATION. TimeGap is " + msgCapValidityTime);
            this.mImSession.removeMessages(1022);
            ImSession imSession3 = this.mImSession;
            imSession3.sendMessageDelayed(imSession3.obtainMessage(1022), msgCapValidityTime);
        }
    }

    private void onExpireDeliveryTimeout() {
        ImSession imSession = this.mImSession;
        for (MessageBase next : imSession.mGetter.getAllPendingMessages(imSession.getChatId())) {
            if (next instanceof ImMessage) {
                ImMessage imMessage = (ImMessage) next;
                if (imMessage.getStatus() == ImConstants.Status.TO_SEND || imMessage.getStatus() == ImConstants.Status.SENDING) {
                    Log.i(LOG_TAG, "onExpireDeliveryTimeout : sending failed " + imMessage.getId());
                    imMessage.onSendMessageDone(new Result(ImError.SESSION_DELIVERY_TIMEOUT, Result.Type.ENGINE_ERROR), new IMnoStrategy.StrategyResponse(IMnoStrategy.StatusCode.NONE));
                }
            }
        }
    }

    private void setRevokeTimer(MessageBase messageBase, boolean z, Result result) {
        NotificationStatus notificationStatus;
        if (this.mImSession.getRcsStrategy(this.mPhoneId).isRevocationAvailableMessage(messageBase) && !z) {
            if (this.mImSession.isMsgRevocationSupported() && result.getImError() == ImError.SUCCESS && (result.getSipResponse() != SipResponse.SIP_486_BUSY_HERE || this.mImSession.getRcsStrategy(this.mPhoneId).boolSetting(RcsPolicySettings.RcsPolicy.SUPPORT_REVOKE_MSG_FOR_486_RESP))) {
                MessageBase message = this.mImSession.mGetter.getMessage(messageBase.getId());
                if (message != null && message.getNotificationStatus() == (notificationStatus = NotificationStatus.NONE) && !message.isTemporary() && !message.getDispositionNotification().contains(notificationStatus)) {
                    messageBase.updateRevocationStatus(ImConstants.RevocationStatus.AVAILABLE);
                    this.mImSession.getNeedToRevokeMessages().put(message.getImdnId(), Integer.valueOf(message.getId()));
                    ImSession imSession = this.mImSession;
                    if (!imSession.mIsRevokeTimerRunning) {
                        imSession.logi("setRevokeTimer() : msg id : " + message.getId() + " time : " + this.mImSession.mConfig.getChatRevokeTimer());
                        ImSession imSession2 = this.mImSession;
                        imSession2.mIsRevokeTimerRunning = true;
                        PreciseAlarmManager.getInstance(imSession2.getContext()).sendMessageDelayed(getClass().getSimpleName(), this.mImSession.obtainMessage(ImSessionEvent.MESSAGE_REVOKE_TIMER_EXPIRED), ((long) this.mImSession.mConfig.getChatRevokeTimer()) * 1000);
                        return;
                    }
                    imSession.logi("setRevokeTimer() : msg id : " + message.getId() + " aleady timer running");
                }
            } else if (this.mImSession.getNeedToRevokeMessages().containsKey(messageBase.getImdnId())) {
                messageBase.updateRevocationStatus(ImConstants.RevocationStatus.NONE);
                this.mImSession.removeMsgFromListForRevoke(messageBase.getImdnId());
            }
        }
    }

    private boolean shouldCloseSession(ImError imError) {
        return imError == ImError.MSRP_ACTION_NOT_ALLOWED || imError == ImError.MSRP_SESSION_DOES_NOT_EXIST || imError == ImError.MSRP_SESSION_ON_OTHER_CONNECTION || imError == ImError.MSRP_DO_NOT_SEND_THIS_MESSAGE;
    }

    private FtMessage removeNextFtMessage() {
        ImSession imSession = this.mImSession;
        imSession.logi("getNextFtMessage, current queue size: " + this.mImSession.mPendingFileTransfer.size());
        if (this.mImSession.mPendingFileTransfer.isEmpty()) {
            return null;
        }
        return this.mImSession.mPendingFileTransfer.remove(0);
    }

    public void onSessionEstablished(ImSessionEstablishedEvent imSessionEstablishedEvent) {
        Object obj;
        ImSession imSession = this.mImSession;
        imSession.logi("onSessionEstablished : " + imSessionEstablishedEvent);
        ImSessionInfo imSessionInfo = this.mImSession.getImSessionInfo(imSessionEstablishedEvent.mRawHandle);
        if (imSessionInfo != null) {
            imSessionInfo.mState = ImSessionInfo.ImSessionState.ESTABLISHED;
            if (!imSessionInfo.isSnFSession()) {
                this.mImSession.updateSessionInfo(imSessionInfo);
                this.mImSession.mEstablishedImSessionInfo.add(0, imSessionInfo);
                this.mImSession.mSupportedFeatures = EnumSet.copyOf(imSessionEstablishedEvent.mFeatures);
                ImSession imSession2 = this.mImSession;
                imSession2.mRemoteAcceptTypes = imSessionEstablishedEvent.mAcceptTypes;
                imSession2.mRemoteAcceptWrappedTypes = imSessionEstablishedEvent.mAcceptWrappedTypes;
                Iterator<FtMessage> it = imSession2.mPendingFileTransfer.iterator();
                while (it.hasNext()) {
                    it.next().conferenceUriChanged();
                }
            }
            this.mImSession.getHandler().removeMessages(1004, imSessionInfo.mRawHandle);
            this.mImSession.transitionToProperState();
            if (imSessionInfo.mStartingReason == ImSessionInfo.StartingReason.EXTENDING_1_1_TO_GROUP && (obj = imSessionInfo.mPrevExtendRawHandle) != null) {
                this.mImSession.mImsService.stopImSession(new StopImSessionParams(obj, ImSessionStopReason.INVOLUNTARILY, (Message) null));
                imSessionInfo.mPrevExtendRawHandle = null;
                return;
            }
            return;
        }
        ImSession imSession3 = this.mImSession;
        imSession3.logi("SESSION_ESTABLISHED unknown rawHandle : " + imSessionEstablishedEvent.mRawHandle);
    }

    public void sendAggregatedDisplayReport() {
        IMSLog.s(LOG_TAG, "sendAggregatedDisplayReport : messages = " + this.mImSession.mMessagesToSendDisplayNotification);
        ArrayList arrayList = new ArrayList();
        ArrayList arrayList2 = new ArrayList();
        while (!this.mImSession.mMessagesToSendDisplayNotification.isEmpty()) {
            MessageBase pollFirst = this.mImSession.mMessagesToSendDisplayNotification.pollFirst();
            arrayList.add(pollFirst.getNewImdnData(NotificationStatus.DISPLAYED));
            arrayList2.add(pollFirst);
        }
        this.mImSession.mImsService.sendDisplayedNotification(new SendImdnParams((Object) null, this.mImSession.getRemoteUri(), this.mImSession.getChatId(), this.mImSession.getConversationId(), this.mImSession.getContributionId(), this.mImSession.getOwnImsi(), this.mImSession.obtainMessage((int) ImSessionEvent.SEND_DISPLAYED_NOTIFICATION_DONE, (Object) arrayList2), this.mImSession.getDeviceId(), (List<SendImdnParams.ImdnData>) arrayList, this.mImSession.isGroupChat(), new Date(), this.mImSession.isBotSessionAnonymized(), this.mImSession.getUserAlias(true)));
    }

    private void onSendFile(Message message) {
        FtMessage ftMessage = (FtMessage) message.obj;
        if (!this.mImSession.isGroupChat() || !(ftMessage instanceof FtMsrpMessage) || this.mImSession.isBroadcastMsg(ftMessage) || ftMessage.getIsSlmSvcMsg()) {
            ftMessage.sendFile();
            return;
        }
        this.mImSession.logi("SEND_FILE in defaultState, conference uri will be changed");
        ftMessage.conferenceUriChanged();
        this.mImSession.deferMessage(message);
    }

    private void onAttachFile(Message message) {
        FtMessage ftMessage = (FtMessage) message.obj;
        boolean z = ftMessage instanceof FtMsrpMessage;
        if (this.mImSession.getRcsStrategy(this.mPhoneId).checkCapability(this.mImSession.getParticipantsUri(), (long) (z ? Capabilities.FEATURE_FT_SERVICE : Capabilities.FEATURE_FT_HTTP), this.mImSession.getChatType(), this.mImSession.isBroadcastMsg(ftMessage)).getStatusCode() == IMnoStrategy.StatusCode.FALLBACK_TO_SLM && !ftMessage.isFtSms()) {
            ftMessage.attachSlmFile();
        } else if (!this.mImSession.isGroupChat() || !z || this.mImSession.isBroadcastMsg(ftMessage)) {
            ftMessage.attachFile(true);
        } else {
            this.mImSession.deferMessage(message);
            this.mImSession.transitionToProperState();
        }
    }

    private void onSendDeliveredNodification(MessageBase messageBase) {
        ImSessionInfo imSessionInfoByMessageId = this.mImSession.getImSessionInfoByMessageId(messageBase.getId());
        Object rawHandle = this.mImSession.getRawHandle();
        if (imSessionInfoByMessageId != null && imSessionInfoByMessageId.isSnFSession() && imSessionInfoByMessageId.mState == ImSessionInfo.ImSessionState.ESTABLISHED) {
            rawHandle = imSessionInfoByMessageId.mRawHandle;
        }
        messageBase.sendDeliveredNotification(rawHandle, this.mImSession.getConversationId(), this.mImSession.getContributionId(), this.mImSession.obtainMessage((int) ImSessionEvent.SEND_DELIVERED_NOTIFICATION_DONE, (Object) messageBase), this.mImSession.getChatData().getOwnIMSI(), this.mImSession.isGroupChat(), this.mImSession.isBotSessionAnonymized());
    }

    private void onSendDisplayedNotification() {
        if (!this.mImSession.mConfig.isAggrImdnSupported() || !this.mImSession.isGroupChat() || this.mImSession.mMessagesToSendDisplayNotification.size() <= 1) {
            ArrayList arrayList = new ArrayList();
            while (!this.mImSession.mMessagesToSendDisplayNotification.isEmpty() && arrayList.size() < this.mImSession.getRcsStrategy(this.mPhoneId).intSetting(RcsPolicySettings.RcsPolicy.NUM_OF_DISPLAY_NOTIFICATION_ATONCE)) {
                arrayList.add(this.mImSession.mMessagesToSendDisplayNotification.pollFirst());
            }
            this.mImSession.onSendDisplayedNotification(arrayList);
            return;
        }
        sendAggregatedDisplayReport();
    }

    private void onSendDeliveredNodificationDone(Message message) {
        AsyncResult asyncResult = (AsyncResult) message.obj;
        MessageBase messageBase = (MessageBase) asyncResult.userObj;
        if (((Result) asyncResult.result).getImError() == ImError.ENGINE_ERROR) {
            this.mImSession.loge("There is ENGINE Error during sending DELIVERED");
        } else {
            messageBase.onSendDeliveredNotificationDone();
        }
    }

    private void onSendDisplayedNotificationDone(Message message) {
        AsyncResult asyncResult = (AsyncResult) message.obj;
        if (((Result) asyncResult.result).getImError() == ImError.ENGINE_ERROR) {
            this.mImSession.loge("There is ENGINE Error during sending DISPLAYED");
            return;
        }
        for (MessageBase onSendDisplayedNotificationDone : (List) asyncResult.userObj) {
            onSendDisplayedNotificationDone.onSendDisplayedNotificationDone();
        }
    }

    private void onSendMessageResponseTimeout(ImMessage imMessage) {
        ImConstants.Status status = imMessage.getStatus();
        if (status == ImConstants.Status.TO_SEND || status == ImConstants.Status.SENDING) {
            imMessage.onSendMessageResponseTimeout();
        }
    }

    private void onSessionTimeoutThreshold(ImMessage imMessage) {
        if (imMessage != null) {
            ImSession imSession = this.mImSession;
            imSession.loge("pendingMsg status : " + imMessage.getStatus());
            if (imMessage.getStatus() == ImConstants.Status.TO_SEND || imMessage.getStatus() == ImConstants.Status.SENDING) {
                imMessage.onSendMessageDone(new Result(ImError.SESSION_TIMED_OUT, Result.Type.ENGINE_ERROR), new IMnoStrategy.StrategyResponse(IMnoStrategy.StatusCode.DISPLAY_ERROR));
            }
        }
    }

    private void onProcessIncomingSnfSession(ImIncomingSessionEvent imIncomingSessionEvent) {
        this.mImSession.handleAcceptSession(this.mImSession.addImSessionInfo(imIncomingSessionEvent, ImSessionInfo.ImSessionState.PENDING_INVITE));
        this.mImSession.onIncomingSessionProcessed(imIncomingSessionEvent.mReceivedMessage, false);
    }

    private void onAcceptSnfSessionDone(Message message) {
        StartImSessionResult startImSessionResult = (StartImSessionResult) ((AsyncResult) message.obj).result;
        ImSession imSession = this.mImSession;
        imSession.logi("ACCEPT_SNF_SESSION_DONE : " + startImSessionResult);
        if (startImSessionResult.mResult.getImError() != ImError.SUCCESS) {
            this.mImSession.removeImSessionInfo(startImSessionResult.mRawHandle);
        }
        this.mImSession.releaseWakeLock(startImSessionResult.mRawHandle);
    }

    private void onStartSessionSynchronousDone(Message message) {
        AsyncResult asyncResult = (AsyncResult) message.obj;
        Object obj = asyncResult.result;
        String str = (String) asyncResult.userObj;
        ImSession imSession = this.mImSession;
        imSession.logi("START_SESSION_SYNCHRONOUS_DONE : sessionKey=" + str + ", rawHandle=" + obj);
        ImSessionInfo imSessionInfo = this.mImSession.getImSessionInfo(str);
        if (imSessionInfo != null) {
            imSessionInfo.mState = ImSessionInfo.ImSessionState.STARTING;
            imSessionInfo.mRawHandle = obj;
            if (str.equals(this.mImSession.getRawHandle())) {
                this.mImSession.setRawHandle(obj);
            }
            this.mImSession.mStartingState.startSessionEstablishmentTimer(obj);
            return;
        }
        ImSession imSession2 = this.mImSession;
        imSession2.loge("cannot find the imSessionInfo using sessionKey : " + str);
        this.mImSession.mImsService.stopImSession(new StopImSessionParams(obj, ImSessionStopReason.INVOLUNTARILY, (Message) null));
    }

    private void onCloseAllSession(ImSessionStopReason imSessionStopReason) {
        if (!this.mImSession.mInProgressRequestCallbacks.isEmpty()) {
            ImSession imSession = this.mImSession;
            imSession.mPendingEvents.add(imSession.obtainMessage(1012, (Object) imSessionStopReason));
            return;
        }
        this.mImSession.handleCloseAllSession(imSessionStopReason);
        this.mImSession.transitionToProperState();
    }

    private void onSendIscomposingNotification() {
        ImSession imSession = this.mImSession;
        imSession.logi("SEND_ISCOMPOSING_NOTIFICATION received in " + this.mImSession.getCurrentState().getName());
        IMnoStrategy rcsStrategy = this.mImSession.getRcsStrategy(this.mPhoneId);
        if (rcsStrategy == null) {
            this.mImSession.loge("SEND_ISCOMPOSING_NOTIFICATION : Failed to get strategy");
        } else if (rcsStrategy.boolSetting(RcsPolicySettings.RcsPolicy.TRIGGER_CAPEX_WHEN_STARTTYPING) && !this.mImSession.mConfig.isImCapAlwaysOn()) {
            triggerCapex();
        }
    }

    private void onStartSessionProvisionalResponse(Message message) {
        StartImSessionResult startImSessionResult = (StartImSessionResult) ((AsyncResult) message.obj).result;
        ImSessionInfo imSessionInfo = this.mImSession.getImSessionInfo(startImSessionResult.mRawHandle);
        ImSession imSession = this.mImSession;
        imSession.logi("START_SESSION_PROVISIONAL_RESPONSE : response=" + startImSessionResult);
        if (imSessionInfo != null) {
            imSessionInfo.mLastProvisionalResponse = startImSessionResult.mResult.getImError();
        }
    }

    private void onMessageRevokeTimerExpired() {
        ImSession imSession = this.mImSession;
        imSession.logi("MESSAGE_REVOKE_TIMER_EXPIRED : " + this.mImSession.getNeedToRevokeMessages());
        ImSession imSession2 = this.mImSession;
        imSession2.mIsRevokeTimerRunning = false;
        Map<String, Integer> needToRevokeMessages = imSession2.getNeedToRevokeMessages();
        ArrayList<String> arrayList = new ArrayList<>(needToRevokeMessages.keySet());
        Collections.sort(arrayList, new ImSessionDefaultState$$ExternalSyntheticLambda2(needToRevokeMessages));
        for (String message : arrayList) {
            MessageBase message2 = this.mImSession.mGetter.getMessage(message, ImDirection.OUTGOING, (String) null);
            if (message2 != null) {
                message2.updateRevocationStatus(ImConstants.RevocationStatus.PENDING);
            }
        }
        ImSession imSession3 = this.mImSession;
        imSession3.mListener.onMessageRevokeTimerExpired(imSession3.getChatId(), arrayList, this.mImSession.getChatData().getOwnIMSI());
    }

    private void onSendMessageRevokeRequest(List<String> list) {
        ImSession imSession = this.mImSession;
        imSession.logi("SEND_MESSAGE_REVOKE_REQUEST : " + list);
        ImSession imSession2 = this.mImSession;
        imSession2.mListener.setLegacyLatching(imSession2.getRemoteUri(), true, this.mImSession.getChatData().getOwnIMSI());
        ArrayList arrayList = new ArrayList();
        Collections.sort(list, new ImSessionDefaultState$$ExternalSyntheticLambda1(this.mImSession.getNeedToRevokeMessages()));
        for (String next : list) {
            MessageBase message = this.mImSession.mGetter.getMessage(next, ImDirection.OUTGOING, (String) null);
            if (message == null || message.getRevocationStatus() != ImConstants.RevocationStatus.PENDING) {
                ImSession imSession3 = this.mImSession;
                imSession3.loge("SEND_MESSAGE_REVOKE_REQUEST : message can't find - imdnId : " + next);
            } else {
                this.mImSession.setNotEmptyContributionId();
                this.mImSession.setNotEmptyConversationId();
                message.sendMessageRevokeRequest(this.mImSession.getConversationId(), this.mImSession.getContributionId(), this.mImSession.obtainMessage((int) ImSessionEvent.SEND_MESSAGE_REVOKE_REQUEST_INTERNAL_DONE, (Object) message), this.mImSession.getChatData().getOwnIMSI());
                if (this.mImSession.mConfig.isCfsTrigger()) {
                    message.updateRevocationStatus(ImConstants.RevocationStatus.SENDING);
                    ImSession imSession4 = this.mImSession;
                    imSession4.mListener.addToRevokingMessages(next, imSession4.getChatId());
                } else {
                    arrayList.add(message);
                }
            }
        }
        if (!arrayList.isEmpty()) {
            ImSession imSession5 = this.mImSession;
            imSession5.mListener.onMessageRevocationDone(ImConstants.RevocationStatus.NONE, arrayList, imSession5);
        }
    }

    private void onResendMessageRevokeRequest() {
        Map<String, Integer> needToRevokeMessages = this.mImSession.getNeedToRevokeMessages();
        ArrayList<String> arrayList = new ArrayList<>(needToRevokeMessages.keySet());
        Collections.sort(arrayList, new ImSessionDefaultState$$ExternalSyntheticLambda0(needToRevokeMessages));
        for (String str : arrayList) {
            MessageBase message = this.mImSession.mGetter.getMessage(str, ImDirection.OUTGOING, (String) null);
            if (message != null && message.getRevocationStatus() == ImConstants.RevocationStatus.SENDING) {
                ImSession imSession = this.mImSession;
                imSession.logi("RESEND_MESSAGE_REVOKE_REQUEST : imdnId : " + str);
                this.mImSession.setNotEmptyContributionId();
                this.mImSession.setNotEmptyConversationId();
                message.sendMessageRevokeRequest(this.mImSession.getConversationId(), this.mImSession.getContributionId(), this.mImSession.obtainMessage((int) ImSessionEvent.SEND_MESSAGE_REVOKE_REQUEST_INTERNAL_DONE, (Object) message), this.mImSession.getChatData().getOwnIMSI());
            }
        }
    }

    private void onSendMessageRevokeRequestInternalDone(Message message) {
        AsyncResult asyncResult = (AsyncResult) message.obj;
        ImSession imSession = this.mImSession;
        imSession.logi("SEND_MESSAGE_REVOKE_REQUEST_INTERNAL_DONE : msgId = " + ((MessageBase) asyncResult.userObj).getId() + ", result = " + ((ImError) asyncResult.result));
    }

    private void onMessageRevokeOperationTimeout(String str) {
        if (this.mImSession.getNeedToRevokeMessages().containsKey(str)) {
            MessageBase message = this.mImSession.mGetter.getMessage(str, ImDirection.OUTGOING, (String) null);
            if (message == null) {
                this.mImSession.removeMsgFromListForRevoke(str);
                return;
            }
            ImSession imSession = this.mImSession;
            imSession.logi("MESSAGE_REVOKE_OPERATION_TIMEOUT : imdnId = " + str);
            ArrayList arrayList = new ArrayList();
            arrayList.add(message);
            ImSession imSession2 = this.mImSession;
            imSession2.mListener.onMessageRevocationDone(ImConstants.RevocationStatus.FAILED, arrayList, imSession2);
        }
    }

    private void onDownloadGroupIconDone(ImIconData imIconData) {
        ImSession imSession = this.mImSession;
        imSession.logi("DOWNLOAD_GROUP_ICON_DONE : " + imIconData);
        this.mImSession.updateIconData(imIconData);
        ImSession imSession2 = this.mImSession;
        imSession2.mListener.onGroupChatIconUpdated(imSession2.getChatId(), this.mImSession.getIconData());
    }

    private void handleSendImResult(IMnoStrategy.StrategyResponse strategyResponse, MessageBase messageBase, SendMessageResult sendMessageResult) {
        int ftHttpSessionRetryTimer;
        ImError imError = sendMessageResult.mResult.getImError();
        switch (AnonymousClass1.$SwitchMap$com$sec$internal$ims$servicemodules$im$strategy$IMnoStrategy$StatusCode[strategyResponse.getStatusCode().ordinal()]) {
            case 1:
                ImSession imSession = this.mImSession;
                imSession.logi("onSendImMessageDone retry msgId : " + messageBase.getId());
                ImSession imSession2 = this.mImSession;
                imSession2.sendMessage(imSession2.obtainMessage((int) ImSessionEvent.SEND_MESSAGE, (Object) messageBase));
                messageBase.incrementRetryCount();
                if (imError == ImError.UNSUPPORTED_URI_SCHEME) {
                    this.mImSession.logi("onSendImMessageDone retry with other URI format");
                    this.mImSession.mSwapUriType = true;
                    return;
                }
                return;
            case 2:
                messageBase.onSendMessageDone(new Result(ImError.SUCCESS, sendMessageResult.mResult), new IMnoStrategy.StrategyResponse(IMnoStrategy.StatusCode.NONE));
                return;
            case 3:
                ImSession imSession3 = this.mImSession;
                imSession3.logi("onSendImMessageDone retry_after msgId: " + messageBase.getId());
                if ((messageBase instanceof FtHttpOutgoingMessage) && (ftHttpSessionRetryTimer = this.mImSession.getRcsStrategy(this.mPhoneId).getFtHttpSessionRetryTimer(messageBase.getCurrentRetryCount(), imError)) != -1) {
                    this.mImSession.mRetryTimer = ftHttpSessionRetryTimer;
                }
                ImSession imSession4 = this.mImSession;
                imSession4.sendMessageDelayed(imSession4.obtainMessage((int) ImSessionEvent.SEND_MESSAGE, (Object) messageBase), ((long) this.mImSession.mRetryTimer) * 1000);
                messageBase.incrementRetryCount();
                return;
            case 4:
                ImSession imSession5 = this.mImSession;
                imSession5.logi("onSendImMessageDone retry_after_session msgId: " + messageBase.getId());
                ImSession imSession6 = this.mImSession;
                imSession6.sendMessageDelayed(imSession6.obtainMessage((int) ImSessionEvent.SEND_MESSAGE, (Object) messageBase), 1000);
                messageBase.incrementRetryCount();
                return;
            case 5:
                if (messageBase instanceof FtHttpOutgoingMessage) {
                    this.mImSession.handleUploadedFileFallback((FtHttpOutgoingMessage) messageBase);
                    return;
                }
                ImSession imSession7 = this.mImSession;
                imSession7.sendMessage(imSession7.obtainMessage((int) ImSessionEvent.SEND_SLM_MESSAGE, (Object) messageBase));
                return;
            case 6:
                this.mImSession.mClosedState.handleCloseSession(sendMessageResult.mRawHandle, ImSessionStopReason.INVOLUNTARILY);
                this.mImSession.transitionToProperState();
                messageBase.onSendMessageDone(sendMessageResult.mResult, strategyResponse);
                return;
            case 7:
                ImSession imSession8 = this.mImSession;
                imSession8.logi("onSendImMessageDone retry_after_regi msgId: " + messageBase.getId());
                ImSession imSession9 = this.mImSession;
                imSession9.sendMessageDelayed(imSession9.obtainMessage((int) ImSessionEvent.SEND_MESSAGE, (Object) messageBase), 1000);
                messageBase.incrementRetryCount();
                return;
            default:
                messageBase.onSendMessageDone(sendMessageResult.mResult, strategyResponse);
                return;
        }
    }

    private void onCloseSessionTimeout(Object obj) {
        ImSession imSession = this.mImSession;
        imSession.logi("onCloseSessionTimeout : rawHandle = " + obj);
        if (this.mImSession.removeImSessionInfo(obj) != null) {
            this.mImSession.transitionToProperState();
        }
    }

    private void onSendCanceledNotificationDone(Message message) {
        AsyncResult asyncResult = (AsyncResult) message.obj;
        MessageBase messageBase = (MessageBase) asyncResult.userObj;
        boolean z = ((Result) asyncResult.result).getImError() == ImError.SUCCESS;
        if (z) {
            messageBase.onSendCanceledNotificationDone();
        }
        ImSession imSession = this.mImSession;
        imSession.mListener.onSendCanceledNotificationDone(imSession.getChatId(), messageBase.getImdnId(), z);
    }
}
