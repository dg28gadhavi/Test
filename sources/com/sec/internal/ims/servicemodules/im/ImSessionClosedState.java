package com.sec.internal.ims.servicemodules.im;

import android.os.Message;
import com.sec.ims.util.ImsUri;
import com.sec.internal.constants.ims.servicemodules.im.ChatData;
import com.sec.internal.constants.ims.servicemodules.im.ImError;
import com.sec.internal.constants.ims.servicemodules.im.event.ImSessionClosedEvent;
import com.sec.internal.constants.ims.servicemodules.im.params.RejectImSessionParams;
import com.sec.internal.constants.ims.servicemodules.im.params.StopImSessionParams;
import com.sec.internal.constants.ims.servicemodules.im.reason.CancelReason;
import com.sec.internal.constants.ims.servicemodules.im.reason.ImErrorReason;
import com.sec.internal.constants.ims.servicemodules.im.reason.ImSessionClosedReason;
import com.sec.internal.constants.ims.servicemodules.im.reason.ImSessionRejectReason;
import com.sec.internal.constants.ims.servicemodules.im.reason.ImSessionStopReason;
import com.sec.internal.constants.ims.servicemodules.im.result.Result;
import com.sec.internal.constants.ims.servicemodules.im.result.StopImSessionResult;
import com.sec.internal.helper.AsyncResult;
import com.sec.internal.ims.servicemodules.im.ImSession;
import com.sec.internal.ims.servicemodules.im.data.event.ImSessionEvent;
import com.sec.internal.ims.servicemodules.im.data.info.ImSessionInfo;
import com.sec.internal.ims.servicemodules.im.strategy.ChnStrategy;
import com.sec.internal.ims.servicemodules.im.strategy.IMnoStrategy;
import com.sec.internal.ims.settings.RcsPolicySettings;
import com.sec.internal.log.IMSLog;
import java.util.ArrayList;
import java.util.List;

public class ImSessionClosedState extends ImSessionStateBase {
    private static final String LOG_TAG = "ClosedState";
    protected ImSessionStopReason mStopReason;

    ImSessionClosedState(int i, ImSession imSession) {
        super(i, imSession);
    }

    public void enter() {
        ImSession imSession = this.mImSession;
        imSession.logi("ClosedState enter. " + this.mImSession.getChatId() + ", mClosedReason=" + this.mImSession.mClosedReason + ", ChatState=" + this.mImSession.getChatData().getState());
        this.mImSession.removeMessages(1023);
        ImSession imSession2 = this.mImSession;
        imSession2.mIsComposing = false;
        for (ImsUri onComposingReceived : imSession2.getComposingActiveUris()) {
            ImSession imSession3 = this.mImSession;
            imSession3.mListener.onComposingReceived(imSession3, onComposingReceived, (String) null, false, imSession3.mComposingNotificationInterval);
        }
        this.mImSession.getComposingActiveUris().clear();
        ImSession imSession4 = this.mImSession;
        ImSessionClosedReason imSessionClosedReason = imSession4.mClosedReason;
        if (imSessionClosedReason == ImSessionClosedReason.CLOSED_INVOLUNTARILY) {
            imSession4.getChatData().updateState(ChatData.State.CLOSED_INVOLUNTARILY);
        } else if (imSessionClosedReason == ImSessionClosedReason.KICKED_OUT_BY_LEADER || imSessionClosedReason == ImSessionClosedReason.GROUP_CHAT_DISMISSED || imSessionClosedReason == ImSessionClosedReason.LEFT_BY_SERVER) {
            imSession4.getChatData().updateState(ChatData.State.NONE);
        } else if (!imSession4.isChatState(ChatData.State.CLOSED_VOLUNTARILY) && !this.mImSession.isChatState(ChatData.State.CLOSED_INVOLUNTARILY) && !this.mImSession.isChatState(ChatData.State.NONE)) {
            this.mImSession.getChatData().updateState(ChatData.State.CLOSED_BY_USER);
        }
        ImSession imSession5 = this.mImSession;
        if (imSession5.mClosedReason == ImSessionClosedReason.ALL_PARTICIPANTS_LEFT) {
            imSession5.setSessionUri((ImsUri) null);
            ArrayList arrayList = new ArrayList();
            arrayList.addAll(this.mImSession.mParticipants.values());
            ImSession imSession6 = this.mImSession;
            imSession6.mListener.onParticipantsDeleted(imSession6, arrayList);
        }
        ImSession imSession7 = this.mImSession;
        imSession7.mListener.onChatStatusUpdate(imSession7, ImSession.SessionState.CLOSED);
        ImSession imSession8 = this.mImSession;
        imSession8.mListener.onChatClosed(imSession8, imSession8.mClosedReason);
        if (this.mImSession.getRcsStrategy(this.mPhoneId).boolSetting(RcsPolicySettings.RcsPolicy.SUPPORT_OFFLINE_GC_INVITATION)) {
            ImSession imSession9 = this.mImSession;
            if (imSession9.mIsOfflineGCInvitation && imSession9.mClosedReason == ImSessionClosedReason.CLOSED_BY_REMOTE && imSession9.getChatData().getState() == ChatData.State.CLOSED_BY_USER && this.mImSession.isRejoinable()) {
                ImSession imSession10 = this.mImSession;
                imSession10.mIsOfflineGCInvitation = false;
                imSession10.sendMessage(imSession10.obtainMessage(1020));
            }
        }
    }

    /* access modifiers changed from: protected */
    public boolean processMessagingEvent(Message message) {
        ImSession imSession = this.mImSession;
        imSession.logi("ClosedState, processMessagingEvent: " + message.what + " ChatId: " + this.mImSession.getChatId());
        int i = message.what;
        if (i == 3004) {
            onAttachFile((FtMessage) message.obj);
        } else if (i != 3005) {
            return false;
        } else {
            onSendFile((FtMessage) message.obj);
        }
        return true;
    }

    /* access modifiers changed from: protected */
    public boolean processGroupChatManagementEvent(Message message) {
        ImSession imSession = this.mImSession;
        imSession.logi("ClosedState, processGroupChatManagementEvent: " + message.what + " ChatId: " + this.mImSession.getChatId());
        if (message.what != 2003) {
            return false;
        }
        this.mImSession.onAddParticipantsFailed((List) message.obj, ImErrorReason.ENGINE_ERROR);
        return true;
    }

    /* access modifiers changed from: protected */
    public boolean processSessionConnectionEvent(Message message) {
        ImSession imSession = this.mImSession;
        imSession.logi("ClosedState, processSessionConnectionEvent: " + message.what + " ChatId: " + this.mImSession.getChatId());
        return false;
    }

    public void handleCloseSession(Object obj, ImSessionStopReason imSessionStopReason) {
        ImSessionInfo imSessionInfo = this.mImSession.getImSessionInfo(obj);
        this.mStopReason = imSessionStopReason;
        if (imSessionInfo != null) {
            ImSession imSession = this.mImSession;
            imSession.logi("handleCloseSession, info.mState=" + imSessionInfo.mState);
            switch (AnonymousClass1.$SwitchMap$com$sec$internal$ims$servicemodules$im$data$info$ImSessionInfo$ImSessionState[imSessionInfo.mState.ordinal()]) {
                case 1:
                    this.mImSession.removeImSessionInfo(obj);
                    return;
                case 2:
                    RejectImSessionParams rejectImSessionParams = new RejectImSessionParams(this.mImSession.getChatId(), imSessionInfo.mRawHandle);
                    if (!this.mImSession.isGroupChat()) {
                        rejectImSessionParams.mSessionRejectReason = ImSessionRejectReason.BUSY_HERE;
                    } else if (imSessionStopReason == ImSessionStopReason.INVOLUNTARILY) {
                        rejectImSessionParams.mSessionRejectReason = ImSessionRejectReason.INVOLUNTARILY;
                    } else if (imSessionStopReason == ImSessionStopReason.VOLUNTARILY) {
                        rejectImSessionParams.mSessionRejectReason = ImSessionRejectReason.VOLUNTARILY;
                    }
                    this.mImSession.mImsService.rejectImSession(rejectImSessionParams);
                    this.mImSession.removeImSessionInfo(obj);
                    return;
                case 3:
                    if (!imSessionInfo.isSnFSession()) {
                        this.mImSession.mEstablishedImSessionInfo.remove(imSessionInfo);
                        if (!this.mImSession.mEstablishedImSessionInfo.isEmpty()) {
                            ImSession imSession2 = this.mImSession;
                            imSession2.updateSessionInfo(imSession2.mEstablishedImSessionInfo.get(0));
                        }
                    }
                    imSessionInfo.mState = ImSessionInfo.ImSessionState.CLOSING;
                    if (imSessionStopReason == ImSessionStopReason.VOLUNTARILY) {
                        imSessionInfo.mIsTryToLeave = true;
                    }
                    ImSession imSession3 = this.mImSession;
                    imSession3.mImsService.stopImSession(new StopImSessionParams(imSessionInfo.mRawHandle, imSessionStopReason, imSession3.obtainMessage(1013)));
                    return;
                case 4:
                case 5:
                case 6:
                    this.mImSession.getHandler().removeMessages(1004, imSessionInfo.mRawHandle);
                    imSessionInfo.mState = ImSessionInfo.ImSessionState.CLOSING;
                    if (imSessionStopReason == ImSessionStopReason.VOLUNTARILY) {
                        imSessionInfo.mIsTryToLeave = true;
                    }
                    ImSession imSession4 = this.mImSession;
                    imSession4.mImsService.stopImSession(new StopImSessionParams(imSessionInfo.mRawHandle, imSessionStopReason, imSession4.obtainMessage(1013)));
                    return;
                default:
                    return;
            }
        } else {
            ImSession imSession5 = this.mImSession;
            imSession5.logi("handleCloseSession cannot find ImSessionInfo with rawHandle : " + obj);
        }
    }

    public void onSessionClosed(ImSessionClosedEvent imSessionClosedEvent) {
        ImSession imSession = this.mImSession;
        imSession.mClosedEvent = imSessionClosedEvent;
        imSession.logi("onSessionClosed : " + imSessionClosedEvent);
        Object obj = imSessionClosedEvent.mRawHandle;
        if (obj != null) {
            ImSessionInfo removeImSessionInfo = this.mImSession.removeImSessionInfo(obj);
            if (removeImSessionInfo == null || removeImSessionInfo.isSnFSession()) {
                ImSession imSession2 = this.mImSession;
                imSession2.logi("onSessionClosed : unknown rawHandle = " + imSessionClosedEvent.mRawHandle);
                return;
            }
            if (imSessionClosedEvent.mRawHandle.equals(this.mImSession.getRawHandle())) {
                this.mImSession.mClosedReason = getClosedReasonByImError(imSessionClosedEvent.mResult.getImError(), imSessionClosedEvent.mReferredBy, removeImSessionInfo.mIsTryToLeave);
                if (this.mImSession.getParticipantsSize() < 1 && imSessionClosedEvent.mResult.getImError() == ImError.NORMAL_RELEASE_GONE) {
                    forceCancelFt(true, CancelReason.CANCELED_BY_USER, true);
                }
                if (removeImSessionInfo.mIsTryToLeave && this.mImSession.isVoluntaryDeparture()) {
                    ImSession imSession3 = this.mImSession;
                    if (imSession3.mClosedReason == ImSessionClosedReason.CLOSED_BY_LOCAL) {
                        imSession3.getChatData().updateState(ChatData.State.NONE);
                        ImSession imSession4 = this.mImSession;
                        imSession4.mListener.onChatDeparted(imSession4);
                    } else if (imSession3.getRcsStrategy(this.mPhoneId).boolSetting(RcsPolicySettings.RcsPolicy.HANDLE_LEAVE_OGC_FAILURE)) {
                        this.mImSession.getChatData().updateState(ChatData.State.NONE);
                    }
                }
            } else {
                ImSession imSession5 = this.mImSession;
                imSession5.logi("session closed event for invalid handle current : " + this.mImSession.getRawHandle() + " event.mRawHandle : " + imSessionClosedEvent.mRawHandle);
            }
            if (removeImSessionInfo.mState == ImSessionInfo.ImSessionState.ESTABLISHED) {
                this.mImSession.mEstablishedImSessionInfo.remove(removeImSessionInfo);
                if (!this.mImSession.mEstablishedImSessionInfo.isEmpty()) {
                    ImSession imSession6 = this.mImSession;
                    imSession6.updateSessionInfo(imSession6.mEstablishedImSessionInfo.get(0));
                }
            } else {
                this.mImSession.getHandler().removeMessages(1004, removeImSessionInfo.mRawHandle);
            }
            if (!this.mImSession.hasActiveImSessionInfo()) {
                this.mImSession.failCurrentMessages(imSessionClosedEvent.mRawHandle, imSessionClosedEvent.mResult);
            }
            this.mImSession.transitionToProperState();
        }
    }

    public void onCloseSessionDone(Message message) {
        Result result;
        StopImSessionResult stopImSessionResult = (StopImSessionResult) ((AsyncResult) message.obj).result;
        ImSession imSession = this.mImSession;
        imSession.logi("onCloseSessionDone : " + stopImSessionResult);
        ImError imError = stopImSessionResult.mError;
        ImSessionInfo imSessionInfo = this.mImSession.getImSessionInfo(stopImSessionResult.mRawHandle);
        if (imSessionInfo != null) {
            if (!imSessionInfo.isSnFSession() && !this.mImSession.hasActiveImSessionInfo()) {
                ImSessionStopReason imSessionStopReason = this.mStopReason;
                ImSessionStopReason imSessionStopReason2 = ImSessionStopReason.NO_RESPONSE;
                if (imSessionStopReason == imSessionStopReason2 && !this.mImSession.mCurrentMessages.isEmpty()) {
                    ImSession imSession2 = this.mImSession;
                    if (imSession2.isFirstMessageInStart(imSession2.mCurrentMessages.get(0).getBody())) {
                        this.mImSession.logi("Retry when MSRP is not respond");
                        retryCurrentMessages();
                    }
                }
                if (this.mImSession.mClosedReason == ImSessionClosedReason.CLOSED_INVOLUNTARILY) {
                    result = new Result(imError, Result.Type.DEVICE_UNREGISTERED);
                } else if (this.mStopReason == imSessionStopReason2) {
                    result = new Result(imError, Result.Type.NETWORK_ERROR);
                } else {
                    result = new Result(imError, Result.Type.ENGINE_ERROR);
                }
                this.mImSession.failCurrentMessages(stopImSessionResult.mRawHandle, result);
            }
            ImSession imSession3 = this.mImSession;
            imSession3.sendMessageDelayed(imSession3.obtainMessage(1023, stopImSessionResult.mRawHandle), 180000);
            return;
        }
        ImSession imSession4 = this.mImSession;
        imSession4.logi("onCloseSessionDone : unknown rawHandle=" + stopImSessionResult.mRawHandle);
    }

    private void retryCurrentMessages() {
        this.mImSession.logi("send pending messages");
        for (MessageBase obtainMessage : this.mImSession.mCurrentMessages) {
            ImSession imSession = this.mImSession;
            imSession.sendMessage(imSession.obtainMessage((int) ImSessionEvent.SEND_MESSAGE, (Object) obtainMessage));
        }
        this.mImSession.mCurrentMessages.clear();
    }

    private void onSendFile(FtMessage ftMessage) {
        if (!this.mImSession.isGroupChat() || !(ftMessage instanceof FtMsrpMessage) || this.mImSession.isBroadcastMsg(ftMessage)) {
            ftMessage.sendFile();
            return;
        }
        IMnoStrategy.StatusCode statusCode = this.mImSession.getRcsStrategy(this.mPhoneId).handleAttachFileFailure(this.mImSession.mClosedReason).getStatusCode();
        if (statusCode == IMnoStrategy.StatusCode.FALLBACK_TO_LEGACY) {
            ftMessage.cancelTransfer(CancelReason.REMOTE_TEMPORARILY_UNAVAILABLE);
        } else if (statusCode == IMnoStrategy.StatusCode.FALLBACK_TO_SLM) {
            ftMessage.sendFile();
        }
    }

    private void onAttachFile(FtMessage ftMessage) {
        IMnoStrategy rcsStrategy = this.mImSession.getRcsStrategy(this.mPhoneId);
        if (this.mImSession.isBroadcastMsg(ftMessage) && !(rcsStrategy instanceof ChnStrategy)) {
            ftMessage.attachSlmFile();
        } else if (!this.mImSession.isGroupChat() || !(ftMessage instanceof FtMsrpMessage) || rcsStrategy == null || (rcsStrategy instanceof ChnStrategy)) {
            ftMessage.attachFile(true);
        } else {
            IMnoStrategy.StatusCode statusCode = rcsStrategy.handleAttachFileFailure(this.mImSession.mClosedReason).getStatusCode();
            if (statusCode == IMnoStrategy.StatusCode.FALLBACK_TO_LEGACY) {
                ftMessage.cancelTransfer(CancelReason.REMOTE_TEMPORARILY_UNAVAILABLE);
            } else if (statusCode == IMnoStrategy.StatusCode.FALLBACK_TO_SLM) {
                ftMessage.attachSlmFile();
                if (!ftMessage.isResuming()) {
                    ftMessage.sendFile();
                }
            }
        }
    }

    private ImSessionClosedReason getClosedReasonByImError(ImError imError, ImsUri imsUri, boolean z) {
        ImSessionClosedReason imSessionClosedReason = ImSessionClosedReason.CLOSED_BY_REMOTE;
        switch (AnonymousClass1.$SwitchMap$com$sec$internal$constants$ims$servicemodules$im$ImError[imError.ordinal()]) {
            case 1:
                return this.mImSession.isGroupChat() ? ImSessionClosedReason.KICKED_OUT_BY_LEADER : imSessionClosedReason;
            case 2:
                if (this.mImSession.isGroupChat()) {
                    return ImSessionClosedReason.GROUP_CHAT_DISMISSED;
                }
                return imSessionClosedReason;
            case 3:
            case 4:
                return ImSessionClosedReason.CLOSED_WITH_480_REASON_CODE;
            case 5:
            case 6:
                if (this.mImSession.isRejoinable()) {
                    return ImSessionClosedReason.CLOSED_INVOLUNTARILY;
                }
                return imSessionClosedReason;
            case 7:
                if (z) {
                    return ImSessionClosedReason.LEAVE_SESSION_FAILED;
                }
                if (this.mImSession.isChatState(ChatData.State.ACTIVE) || this.mImSession.isChatState(ChatData.State.CLOSED_INVOLUNTARILY)) {
                    return ImSessionClosedReason.CLOSED_INVOLUNTARILY;
                }
                return imSessionClosedReason;
            case 8:
                if (imsUri != null) {
                    ImSession imSession = this.mImSession;
                    imSession.logi("receive BYE with 410 reason. referred by = " + IMSLog.numberChecker(imsUri.toString()));
                    this.mImSession.setSessionUri((ImsUri) null);
                    return ImSessionClosedReason.KICKED_OUT_BY_LEADER;
                } else if (this.mImSession.getRcsStrategy(this.mPhoneId).boolSetting(RcsPolicySettings.RcsPolicy.SUPPORT_CHAT_CLOSE_BY_SERVER)) {
                    return ImSessionClosedReason.LEFT_BY_SERVER;
                } else {
                    return imSessionClosedReason;
                }
            case 9:
                if (!z) {
                    return imSessionClosedReason;
                }
                if (this.mStopReason == ImSessionStopReason.VOLUNTARILY) {
                    return ImSessionClosedReason.CLOSED_BY_LOCAL;
                }
                return ImSessionClosedReason.LEAVE_SESSION_FAILED;
            default:
                return imSessionClosedReason;
        }
    }

    /* renamed from: com.sec.internal.ims.servicemodules.im.ImSessionClosedState$1  reason: invalid class name */
    static /* synthetic */ class AnonymousClass1 {
        static final /* synthetic */ int[] $SwitchMap$com$sec$internal$constants$ims$servicemodules$im$ImError;
        static final /* synthetic */ int[] $SwitchMap$com$sec$internal$ims$servicemodules$im$data$info$ImSessionInfo$ImSessionState;

        /* JADX WARNING: Can't wrap try/catch for region: R(35:0|(2:1|2)|3|(2:5|6)|7|9|10|11|(2:13|14)|15|(2:17|18)|19|21|22|23|25|26|27|28|29|30|31|33|34|35|36|37|38|39|40|41|42|43|44|(3:45|46|48)) */
        /* JADX WARNING: Can't wrap try/catch for region: R(36:0|(2:1|2)|3|(2:5|6)|7|9|10|11|(2:13|14)|15|17|18|19|21|22|23|25|26|27|28|29|30|31|33|34|35|36|37|38|39|40|41|42|43|44|(3:45|46|48)) */
        /* JADX WARNING: Can't wrap try/catch for region: R(37:0|(2:1|2)|3|5|6|7|9|10|11|(2:13|14)|15|17|18|19|21|22|23|25|26|27|28|29|30|31|33|34|35|36|37|38|39|40|41|42|43|44|(3:45|46|48)) */
        /* JADX WARNING: Can't wrap try/catch for region: R(41:0|1|2|3|5|6|7|9|10|11|13|14|15|17|18|19|21|22|23|25|26|27|28|29|30|31|33|34|35|36|37|38|39|40|41|42|43|44|45|46|48) */
        /* JADX WARNING: Failed to process nested try/catch */
        /* JADX WARNING: Missing exception handler attribute for start block: B:27:0x0054 */
        /* JADX WARNING: Missing exception handler attribute for start block: B:29:0x0060 */
        /* JADX WARNING: Missing exception handler attribute for start block: B:35:0x007d */
        /* JADX WARNING: Missing exception handler attribute for start block: B:37:0x0087 */
        /* JADX WARNING: Missing exception handler attribute for start block: B:39:0x0091 */
        /* JADX WARNING: Missing exception handler attribute for start block: B:41:0x009b */
        /* JADX WARNING: Missing exception handler attribute for start block: B:43:0x00a5 */
        /* JADX WARNING: Missing exception handler attribute for start block: B:45:0x00af */
        static {
            /*
                com.sec.internal.constants.ims.servicemodules.im.ImError[] r0 = com.sec.internal.constants.ims.servicemodules.im.ImError.values()
                int r0 = r0.length
                int[] r0 = new int[r0]
                $SwitchMap$com$sec$internal$constants$ims$servicemodules$im$ImError = r0
                r1 = 1
                com.sec.internal.constants.ims.servicemodules.im.ImError r2 = com.sec.internal.constants.ims.servicemodules.im.ImError.CONFERENCE_PARTY_BOOTED     // Catch:{ NoSuchFieldError -> 0x0012 }
                int r2 = r2.ordinal()     // Catch:{ NoSuchFieldError -> 0x0012 }
                r0[r2] = r1     // Catch:{ NoSuchFieldError -> 0x0012 }
            L_0x0012:
                r0 = 2
                int[] r2 = $SwitchMap$com$sec$internal$constants$ims$servicemodules$im$ImError     // Catch:{ NoSuchFieldError -> 0x001d }
                com.sec.internal.constants.ims.servicemodules.im.ImError r3 = com.sec.internal.constants.ims.servicemodules.im.ImError.CONFERENCE_CALL_COMPLETED     // Catch:{ NoSuchFieldError -> 0x001d }
                int r3 = r3.ordinal()     // Catch:{ NoSuchFieldError -> 0x001d }
                r2[r3] = r0     // Catch:{ NoSuchFieldError -> 0x001d }
            L_0x001d:
                r2 = 3
                int[] r3 = $SwitchMap$com$sec$internal$constants$ims$servicemodules$im$ImError     // Catch:{ NoSuchFieldError -> 0x0028 }
                com.sec.internal.constants.ims.servicemodules.im.ImError r4 = com.sec.internal.constants.ims.servicemodules.im.ImError.NORMAL_RELEASE_BEARER_UNAVAILABLE     // Catch:{ NoSuchFieldError -> 0x0028 }
                int r4 = r4.ordinal()     // Catch:{ NoSuchFieldError -> 0x0028 }
                r3[r4] = r2     // Catch:{ NoSuchFieldError -> 0x0028 }
            L_0x0028:
                r3 = 4
                int[] r4 = $SwitchMap$com$sec$internal$constants$ims$servicemodules$im$ImError     // Catch:{ NoSuchFieldError -> 0x0033 }
                com.sec.internal.constants.ims.servicemodules.im.ImError r5 = com.sec.internal.constants.ims.servicemodules.im.ImError.SESSION_TIMED_OUT     // Catch:{ NoSuchFieldError -> 0x0033 }
                int r5 = r5.ordinal()     // Catch:{ NoSuchFieldError -> 0x0033 }
                r4[r5] = r3     // Catch:{ NoSuchFieldError -> 0x0033 }
            L_0x0033:
                r4 = 5
                int[] r5 = $SwitchMap$com$sec$internal$constants$ims$servicemodules$im$ImError     // Catch:{ NoSuchFieldError -> 0x003e }
                com.sec.internal.constants.ims.servicemodules.im.ImError r6 = com.sec.internal.constants.ims.servicemodules.im.ImError.DEVICE_UNREGISTERED     // Catch:{ NoSuchFieldError -> 0x003e }
                int r6 = r6.ordinal()     // Catch:{ NoSuchFieldError -> 0x003e }
                r5[r6] = r4     // Catch:{ NoSuchFieldError -> 0x003e }
            L_0x003e:
                r5 = 6
                int[] r6 = $SwitchMap$com$sec$internal$constants$ims$servicemodules$im$ImError     // Catch:{ NoSuchFieldError -> 0x0049 }
                com.sec.internal.constants.ims.servicemodules.im.ImError r7 = com.sec.internal.constants.ims.servicemodules.im.ImError.DEDICATED_BEARER_ERROR     // Catch:{ NoSuchFieldError -> 0x0049 }
                int r7 = r7.ordinal()     // Catch:{ NoSuchFieldError -> 0x0049 }
                r6[r7] = r5     // Catch:{ NoSuchFieldError -> 0x0049 }
            L_0x0049:
                r6 = 7
                int[] r7 = $SwitchMap$com$sec$internal$constants$ims$servicemodules$im$ImError     // Catch:{ NoSuchFieldError -> 0x0054 }
                com.sec.internal.constants.ims.servicemodules.im.ImError r8 = com.sec.internal.constants.ims.servicemodules.im.ImError.NETWORK_ERROR     // Catch:{ NoSuchFieldError -> 0x0054 }
                int r8 = r8.ordinal()     // Catch:{ NoSuchFieldError -> 0x0054 }
                r7[r8] = r6     // Catch:{ NoSuchFieldError -> 0x0054 }
            L_0x0054:
                int[] r7 = $SwitchMap$com$sec$internal$constants$ims$servicemodules$im$ImError     // Catch:{ NoSuchFieldError -> 0x0060 }
                com.sec.internal.constants.ims.servicemodules.im.ImError r8 = com.sec.internal.constants.ims.servicemodules.im.ImError.NORMAL_RELEASE_GONE     // Catch:{ NoSuchFieldError -> 0x0060 }
                int r8 = r8.ordinal()     // Catch:{ NoSuchFieldError -> 0x0060 }
                r9 = 8
                r7[r8] = r9     // Catch:{ NoSuchFieldError -> 0x0060 }
            L_0x0060:
                int[] r7 = $SwitchMap$com$sec$internal$constants$ims$servicemodules$im$ImError     // Catch:{ NoSuchFieldError -> 0x006c }
                com.sec.internal.constants.ims.servicemodules.im.ImError r8 = com.sec.internal.constants.ims.servicemodules.im.ImError.NORMAL_RELEASE     // Catch:{ NoSuchFieldError -> 0x006c }
                int r8 = r8.ordinal()     // Catch:{ NoSuchFieldError -> 0x006c }
                r9 = 9
                r7[r8] = r9     // Catch:{ NoSuchFieldError -> 0x006c }
            L_0x006c:
                com.sec.internal.ims.servicemodules.im.data.info.ImSessionInfo$ImSessionState[] r7 = com.sec.internal.ims.servicemodules.im.data.info.ImSessionInfo.ImSessionState.values()
                int r7 = r7.length
                int[] r7 = new int[r7]
                $SwitchMap$com$sec$internal$ims$servicemodules$im$data$info$ImSessionInfo$ImSessionState = r7
                com.sec.internal.ims.servicemodules.im.data.info.ImSessionInfo$ImSessionState r8 = com.sec.internal.ims.servicemodules.im.data.info.ImSessionInfo.ImSessionState.INITIAL     // Catch:{ NoSuchFieldError -> 0x007d }
                int r8 = r8.ordinal()     // Catch:{ NoSuchFieldError -> 0x007d }
                r7[r8] = r1     // Catch:{ NoSuchFieldError -> 0x007d }
            L_0x007d:
                int[] r1 = $SwitchMap$com$sec$internal$ims$servicemodules$im$data$info$ImSessionInfo$ImSessionState     // Catch:{ NoSuchFieldError -> 0x0087 }
                com.sec.internal.ims.servicemodules.im.data.info.ImSessionInfo$ImSessionState r7 = com.sec.internal.ims.servicemodules.im.data.info.ImSessionInfo.ImSessionState.PENDING_INVITE     // Catch:{ NoSuchFieldError -> 0x0087 }
                int r7 = r7.ordinal()     // Catch:{ NoSuchFieldError -> 0x0087 }
                r1[r7] = r0     // Catch:{ NoSuchFieldError -> 0x0087 }
            L_0x0087:
                int[] r0 = $SwitchMap$com$sec$internal$ims$servicemodules$im$data$info$ImSessionInfo$ImSessionState     // Catch:{ NoSuchFieldError -> 0x0091 }
                com.sec.internal.ims.servicemodules.im.data.info.ImSessionInfo$ImSessionState r1 = com.sec.internal.ims.servicemodules.im.data.info.ImSessionInfo.ImSessionState.ESTABLISHED     // Catch:{ NoSuchFieldError -> 0x0091 }
                int r1 = r1.ordinal()     // Catch:{ NoSuchFieldError -> 0x0091 }
                r0[r1] = r2     // Catch:{ NoSuchFieldError -> 0x0091 }
            L_0x0091:
                int[] r0 = $SwitchMap$com$sec$internal$ims$servicemodules$im$data$info$ImSessionInfo$ImSessionState     // Catch:{ NoSuchFieldError -> 0x009b }
                com.sec.internal.ims.servicemodules.im.data.info.ImSessionInfo$ImSessionState r1 = com.sec.internal.ims.servicemodules.im.data.info.ImSessionInfo.ImSessionState.STARTING     // Catch:{ NoSuchFieldError -> 0x009b }
                int r1 = r1.ordinal()     // Catch:{ NoSuchFieldError -> 0x009b }
                r0[r1] = r3     // Catch:{ NoSuchFieldError -> 0x009b }
            L_0x009b:
                int[] r0 = $SwitchMap$com$sec$internal$ims$servicemodules$im$data$info$ImSessionInfo$ImSessionState     // Catch:{ NoSuchFieldError -> 0x00a5 }
                com.sec.internal.ims.servicemodules.im.data.info.ImSessionInfo$ImSessionState r1 = com.sec.internal.ims.servicemodules.im.data.info.ImSessionInfo.ImSessionState.STARTED     // Catch:{ NoSuchFieldError -> 0x00a5 }
                int r1 = r1.ordinal()     // Catch:{ NoSuchFieldError -> 0x00a5 }
                r0[r1] = r4     // Catch:{ NoSuchFieldError -> 0x00a5 }
            L_0x00a5:
                int[] r0 = $SwitchMap$com$sec$internal$ims$servicemodules$im$data$info$ImSessionInfo$ImSessionState     // Catch:{ NoSuchFieldError -> 0x00af }
                com.sec.internal.ims.servicemodules.im.data.info.ImSessionInfo$ImSessionState r1 = com.sec.internal.ims.servicemodules.im.data.info.ImSessionInfo.ImSessionState.ACCEPTING     // Catch:{ NoSuchFieldError -> 0x00af }
                int r1 = r1.ordinal()     // Catch:{ NoSuchFieldError -> 0x00af }
                r0[r1] = r5     // Catch:{ NoSuchFieldError -> 0x00af }
            L_0x00af:
                int[] r0 = $SwitchMap$com$sec$internal$ims$servicemodules$im$data$info$ImSessionInfo$ImSessionState     // Catch:{ NoSuchFieldError -> 0x00b9 }
                com.sec.internal.ims.servicemodules.im.data.info.ImSessionInfo$ImSessionState r1 = com.sec.internal.ims.servicemodules.im.data.info.ImSessionInfo.ImSessionState.CLOSING     // Catch:{ NoSuchFieldError -> 0x00b9 }
                int r1 = r1.ordinal()     // Catch:{ NoSuchFieldError -> 0x00b9 }
                r0[r1] = r6     // Catch:{ NoSuchFieldError -> 0x00b9 }
            L_0x00b9:
                return
            */
            throw new UnsupportedOperationException("Method not decompiled: com.sec.internal.ims.servicemodules.im.ImSessionClosedState.AnonymousClass1.<clinit>():void");
        }
    }

    /* access modifiers changed from: protected */
    public void forceCancelFt(boolean z, CancelReason cancelReason, boolean z2) {
        ImSession imSession = this.mImSession;
        imSession.logi("forceCancelFt :" + this.mImSession.getChatId());
        ImSession imSession2 = this.mImSession;
        for (MessageBase next : imSession2.mGetter.getAllPendingMessages(imSession2.getChatId())) {
            if (next instanceof FtMessage) {
                FtMessage ftMessage = (FtMessage) next;
                if (ftMessage.getStateId() == 2) {
                    ImSession imSession3 = this.mImSession;
                    imSession3.logi("forceCancelFt : mPendingMessages FtMessage.getStateId() = " + ftMessage.getStateId());
                    if (!(next instanceof FtHttpIncomingMessage)) {
                        ftMessage.setFtCompleteCallback((Message) null);
                        ftMessage.cancelTransfer(cancelReason);
                    } else if (!z2) {
                        ftMessage.cancelTransfer(cancelReason);
                    }
                }
            }
        }
        if (z) {
            this.mImSession.cancelPendingFilesInQueue();
        }
    }
}
