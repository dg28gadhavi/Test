package com.sec.internal.ims.core.handler.secims;

import android.annotation.SuppressLint;
import android.net.Uri;
import android.os.Handler;
import android.os.Looper;
import android.os.Message;
import android.text.TextUtils;
import android.util.Log;
import com.google.flatbuffers.FlatBufferBuilder;
import com.sec.ims.util.ImsUri;
import com.sec.internal.constants.ims.MIMEContentType;
import com.sec.internal.constants.ims.servicemodules.im.ImDirection;
import com.sec.internal.constants.ims.servicemodules.im.ImError;
import com.sec.internal.constants.ims.servicemodules.im.MaapNamespace;
import com.sec.internal.constants.ims.servicemodules.im.RcsNamespace;
import com.sec.internal.constants.ims.servicemodules.im.params.AcceptFtSessionParams;
import com.sec.internal.constants.ims.servicemodules.im.params.AcceptImSessionParams;
import com.sec.internal.constants.ims.servicemodules.im.params.AddParticipantsParams;
import com.sec.internal.constants.ims.servicemodules.im.params.ChangeGroupAliasParams;
import com.sec.internal.constants.ims.servicemodules.im.params.ChangeGroupChatIconParams;
import com.sec.internal.constants.ims.servicemodules.im.params.ChangeGroupChatLeaderParams;
import com.sec.internal.constants.ims.servicemodules.im.params.ChangeGroupChatSubjectParams;
import com.sec.internal.constants.ims.servicemodules.im.params.ChatbotAnonymizeParams;
import com.sec.internal.constants.ims.servicemodules.im.params.GroupChatInfoParams;
import com.sec.internal.constants.ims.servicemodules.im.params.GroupChatListParams;
import com.sec.internal.constants.ims.servicemodules.im.params.ImSendComposingParams;
import com.sec.internal.constants.ims.servicemodules.im.params.RejectFtSessionParams;
import com.sec.internal.constants.ims.servicemodules.im.params.RejectImSessionParams;
import com.sec.internal.constants.ims.servicemodules.im.params.RemoveParticipantsParams;
import com.sec.internal.constants.ims.servicemodules.im.params.ReportChatbotAsSpamParams;
import com.sec.internal.constants.ims.servicemodules.im.params.SendFtSessionParams;
import com.sec.internal.constants.ims.servicemodules.im.params.SendImdnParams;
import com.sec.internal.constants.ims.servicemodules.im.params.SendMessageParams;
import com.sec.internal.constants.ims.servicemodules.im.params.SendMessageRevokeParams;
import com.sec.internal.constants.ims.servicemodules.im.params.SendReportMsgParams;
import com.sec.internal.constants.ims.servicemodules.im.params.StartImSessionParams;
import com.sec.internal.constants.ims.servicemodules.im.params.StopImSessionParams;
import com.sec.internal.constants.ims.servicemodules.im.reason.FtRejectReason;
import com.sec.internal.constants.ims.servicemodules.im.reason.ImSessionStopReason;
import com.sec.internal.constants.ims.servicemodules.im.result.FtResult;
import com.sec.internal.constants.ims.servicemodules.im.result.RejectImSessionResult;
import com.sec.internal.constants.ims.servicemodules.im.result.Result;
import com.sec.internal.constants.ims.servicemodules.im.result.SendMessageResult;
import com.sec.internal.constants.ims.servicemodules.im.result.StartImSessionResult;
import com.sec.internal.constants.ims.servicemodules.im.result.StopImSessionResult;
import com.sec.internal.helper.AsyncResult;
import com.sec.internal.helper.Iso8601;
import com.sec.internal.helper.Registrant;
import com.sec.internal.helper.RegistrantList;
import com.sec.internal.helper.translate.ContentTypeTranslator;
import com.sec.internal.ims.core.handler.ImHandler;
import com.sec.internal.ims.core.handler.secims.imsCommonStruc.BaseSessionData;
import com.sec.internal.ims.core.handler.secims.imsCommonStruc.CpimNamespace;
import com.sec.internal.ims.core.handler.secims.imsCommonStruc.CpimNamespace_.Pair;
import com.sec.internal.ims.core.handler.secims.imsCommonStruc.FtPayloadParam;
import com.sec.internal.ims.core.handler.secims.imsCommonStruc.Id;
import com.sec.internal.ims.core.handler.secims.imsCommonStruc.ImComposingStatus;
import com.sec.internal.ims.core.handler.secims.imsCommonStruc.ImFileAttr;
import com.sec.internal.ims.core.handler.secims.imsCommonStruc.ImMessageParam;
import com.sec.internal.ims.core.handler.secims.imsCommonStruc.ImdnParams;
import com.sec.internal.ims.core.handler.secims.imsCommonStruc.ReasonHdr;
import com.sec.internal.ims.core.handler.secims.imsCommonStruc.ReportMessageHdr;
import com.sec.internal.ims.core.handler.secims.imsCommonStruc.ReqMsg;
import com.sec.internal.ims.core.handler.secims.imsCommonStruc.Request;
import com.sec.internal.ims.core.handler.secims.imsCommonStruc.Request_.RequestAcceptFtSession;
import com.sec.internal.ims.core.handler.secims.imsCommonStruc.Request_.RequestAcceptImSession;
import com.sec.internal.ims.core.handler.secims.imsCommonStruc.Request_.RequestCancelFtSession;
import com.sec.internal.ims.core.handler.secims.imsCommonStruc.Request_.RequestChatbotAnonymize;
import com.sec.internal.ims.core.handler.secims.imsCommonStruc.Request_.RequestCloseImSession;
import com.sec.internal.ims.core.handler.secims.imsCommonStruc.Request_.RequestGroupInfoSubscribe;
import com.sec.internal.ims.core.handler.secims.imsCommonStruc.Request_.RequestGroupListSubscribe;
import com.sec.internal.ims.core.handler.secims.imsCommonStruc.Request_.RequestImSetMoreInfoToSipUA;
import com.sec.internal.ims.core.handler.secims.imsCommonStruc.Request_.RequestRejectImSession;
import com.sec.internal.ims.core.handler.secims.imsCommonStruc.Request_.RequestReportChatbotAsSpam;
import com.sec.internal.ims.core.handler.secims.imsCommonStruc.Request_.RequestSendImComposingStatus;
import com.sec.internal.ims.core.handler.secims.imsCommonStruc.Request_.RequestSendImMessage;
import com.sec.internal.ims.core.handler.secims.imsCommonStruc.Request_.RequestSendMessageRevokeRequest;
import com.sec.internal.ims.core.handler.secims.imsCommonStruc.Request_.RequestStartFtSession;
import com.sec.internal.ims.core.handler.secims.imsCommonStruc.Request_.RequestStartMedia;
import com.sec.internal.ims.core.handler.secims.imsCommonStruc.Request_.RequestUpdateParticipants;
import com.sec.internal.ims.core.handler.secims.imsCommonStruc.WarningHdr;
import com.sec.internal.ims.settings.ServiceConstants;
import com.sec.internal.ims.translate.ResipTranslatorCollection;
import com.sec.internal.interfaces.ims.IImsFramework;
import com.sec.internal.log.IMSLog;
import java.io.File;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Set;

@SuppressLint({"UseSparseArrays"})
public class ResipImHandler extends ImHandler {
    protected final RegistrantList mChatbotAnonymizeNotifyRegistrants;
    protected final RegistrantList mChatbotAnonymizeResponseRegistrants;
    protected final RegistrantList mComposingRegistrants;
    protected final RegistrantList mConferenceInfoUpdateRegistrants;
    protected final Map<Integer, FtSession> mFtSessions;
    protected final RegistrantList mGroupChatInfoRegistrants;
    protected final RegistrantList mGroupChatListRegistrants;
    protected final RegistrantList mImdnFailedRegistrants;
    private ResipImdnHandler mImdnHandler;
    private IImsFramework mImsFramework;
    protected final RegistrantList mIncomingFileTransferRegistrants;
    protected final RegistrantList mIncomingMessageRegistrants;
    protected final RegistrantList mIncomingSessionRegistrants;
    protected final RegistrantList mMessageFailedRegistrants;
    protected final RegistrantList mMessageRevokeResponseRegistransts;
    protected final Map<String, FtSession> mPendingFtSessions;
    protected final Map<String, ImSession> mPendingSessions;
    protected final RegistrantList mReportChatbotAsSpamRespRegistrants;
    protected final RegistrantList mSendMessageRevokeResponseRegistransts;
    protected final RegistrantList mSessionClosedRegistrants;
    protected final RegistrantList mSessionEstablishedRegistrants;
    protected final Map<Integer, ImSession> mSessions;
    private ResipImResponseHandler mStackResponseHandler;
    protected final RegistrantList mTransferProgressRegistrants;

    private static String adjustFilePath(String str) {
        return str;
    }

    private String parseStr(String str) {
        return str != null ? str : "";
    }

    public void unregisterAllFileTransferProgress() {
    }

    protected static final class ImSession {
        protected Message mAcceptCallback;
        protected Map<String, Message> mAddParticipantsCallbacks;
        protected Map<String, Message> mChangeGCAliasCallbacks;
        protected Map<String, Message> mChangeGCIconCallbacks;
        protected Map<String, Message> mChangeGCLeaderCallbacks;
        protected Map<String, Message> mChangeGCSubjectCallbacks;
        protected String mChatId;
        protected Message mFirstMessageCallback;
        protected boolean mIsSnF;
        protected Message mRejectCallback;
        protected Map<String, Message> mRemoveParticipantsCallbacks;
        protected Map<String, Message> mSendMessageCallbacks;
        protected Integer mSessionHandle;
        protected Message mStartCallback;
        protected Message mStartProvisionalCallback;
        protected Message mStartSyncCallback;
        protected StopImSessionParams mStopParams;
        protected final int mUaHandle;

        protected ImSession(String str, Message message, Message message2, Message message3, boolean z, int i) {
            this.mChangeGCLeaderCallbacks = new HashMap();
            this.mChangeGCSubjectCallbacks = new HashMap();
            this.mChangeGCAliasCallbacks = new HashMap();
            this.mChangeGCIconCallbacks = new HashMap();
            this.mAddParticipantsCallbacks = new HashMap();
            this.mRemoveParticipantsCallbacks = new HashMap();
            this.mSendMessageCallbacks = new HashMap();
            this.mChatId = str;
            this.mStartCallback = message;
            this.mStartSyncCallback = message2;
            this.mStartProvisionalCallback = message3;
            this.mIsSnF = z;
            this.mUaHandle = i;
        }

        protected ImSession(int i, boolean z, int i2) {
            this((String) null, (Message) null, (Message) null, (Message) null, z, i2);
            this.mSessionHandle = Integer.valueOf(i);
        }

        /* access modifiers changed from: protected */
        public Message findAndRemoveCallback(String str) {
            Message message = this.mSendMessageCallbacks.get(str);
            this.mSendMessageCallbacks.remove(str);
            return message;
        }
    }

    protected static final class FtSession {
        protected Message mAcceptCallback;
        protected RejectFtSessionParams mCancelParams;
        protected int mHandle;
        protected int mId = -1;
        protected Message mStartCallback;
        protected Message mStartSessionHandleCallback;
        protected int mUaHandle;

        protected FtSession() {
        }
    }

    public ResipImHandler(Looper looper, IImsFramework iImsFramework) {
        this(looper, iImsFramework, new ResipImdnHandler(looper, iImsFramework));
    }

    public ResipImHandler(Looper looper, IImsFramework iImsFramework, ResipImdnHandler resipImdnHandler) {
        super(looper);
        this.mFtSessions = new HashMap();
        this.mPendingFtSessions = new HashMap();
        this.mSessions = new HashMap();
        this.mPendingSessions = new HashMap();
        this.mSessionEstablishedRegistrants = new RegistrantList();
        this.mSessionClosedRegistrants = new RegistrantList();
        this.mIncomingSessionRegistrants = new RegistrantList();
        this.mIncomingFileTransferRegistrants = new RegistrantList();
        this.mIncomingMessageRegistrants = new RegistrantList();
        this.mComposingRegistrants = new RegistrantList();
        this.mConferenceInfoUpdateRegistrants = new RegistrantList();
        this.mMessageFailedRegistrants = new RegistrantList();
        this.mImdnFailedRegistrants = new RegistrantList();
        this.mTransferProgressRegistrants = new RegistrantList();
        this.mGroupChatListRegistrants = new RegistrantList();
        this.mGroupChatInfoRegistrants = new RegistrantList();
        this.mMessageRevokeResponseRegistransts = new RegistrantList();
        this.mSendMessageRevokeResponseRegistransts = new RegistrantList();
        this.mChatbotAnonymizeResponseRegistrants = new RegistrantList();
        this.mChatbotAnonymizeNotifyRegistrants = new RegistrantList();
        this.mReportChatbotAsSpamRespRegistrants = new RegistrantList();
        this.mImsFramework = iImsFramework;
        this.mStackResponseHandler = new ResipImResponseHandler(looper, this);
        this.mImdnHandler = resipImdnHandler;
        StackIF.getInstance().registerImHandler(this.mStackResponseHandler, 100, (Object) null);
    }

    public void startImSession(StartImSessionParams startImSessionParams) {
        sendMessage(obtainMessage(1, startImSessionParams));
    }

    public void acceptImSession(AcceptImSessionParams acceptImSessionParams) {
        sendMessage(obtainMessage(2, acceptImSessionParams));
    }

    public void stopImSession(StopImSessionParams stopImSessionParams) {
        sendMessage(obtainMessage(3, stopImSessionParams));
    }

    public void rejectImSession(RejectImSessionParams rejectImSessionParams) {
        sendMessage(obtainMessage(17, rejectImSessionParams));
    }

    public void sendImMessage(SendMessageParams sendMessageParams) {
        sendMessage(obtainMessage(4, sendMessageParams));
    }

    public void acceptFtSession(AcceptFtSessionParams acceptFtSessionParams) {
        sendMessage(obtainMessage(5, acceptFtSessionParams));
    }

    public void rejectFtSession(RejectFtSessionParams rejectFtSessionParams) {
        sendMessage(obtainMessage(7, rejectFtSessionParams));
    }

    public void cancelFtSession(RejectFtSessionParams rejectFtSessionParams) {
        sendMessage(obtainMessage(6, rejectFtSessionParams));
    }

    public void sendFtSession(SendFtSessionParams sendFtSessionParams) {
        sendMessage(obtainMessage(8, sendFtSessionParams));
    }

    public void sendFtDeliveredNotification(SendImdnParams sendImdnParams) {
        sendMessage(obtainMessage(14, sendImdnParams));
    }

    public void sendFtDisplayedNotification(SendImdnParams sendImdnParams) {
        sendMessage(obtainMessage(14, sendImdnParams));
    }

    public void removeImParticipants(RemoveParticipantsParams removeParticipantsParams) {
        sendMessage(obtainMessage(21, removeParticipantsParams));
    }

    public void changeGroupChatLeader(ChangeGroupChatLeaderParams changeGroupChatLeaderParams) {
        sendMessage(obtainMessage(19, changeGroupChatLeaderParams));
    }

    public void changeGroupChatSubject(ChangeGroupChatSubjectParams changeGroupChatSubjectParams) {
        sendMessage(obtainMessage(22, changeGroupChatSubjectParams));
    }

    public void changeGroupChatIcon(ChangeGroupChatIconParams changeGroupChatIconParams) {
        sendMessage(obtainMessage(30, changeGroupChatIconParams));
    }

    public void changeGroupAlias(ChangeGroupAliasParams changeGroupAliasParams) {
        sendMessage(obtainMessage(23, changeGroupAliasParams));
    }

    public void sendComposingNotification(ImSendComposingParams imSendComposingParams) {
        sendMessage(obtainMessage(9, imSendComposingParams));
    }

    public void sendDeliveredNotification(SendImdnParams sendImdnParams) {
        sendMessage(obtainMessage(10, sendImdnParams));
    }

    public void sendDisplayedNotification(SendImdnParams sendImdnParams) {
        sendMessage(obtainMessage(10, sendImdnParams));
    }

    public void sendCanceledNotification(SendImdnParams sendImdnParams) {
        sendMessage(obtainMessage(10, sendImdnParams));
    }

    public void sendMessageRevokeRequest(SendMessageRevokeParams sendMessageRevokeParams) {
        sendMessage(obtainMessage(28, sendMessageRevokeParams));
    }

    public void addImParticipants(AddParticipantsParams addParticipantsParams) {
        sendMessage(obtainMessage(12, addParticipantsParams));
    }

    public void extendToGroupChat(StartImSessionParams startImSessionParams) {
        sendMessage(obtainMessage(13, startImSessionParams));
    }

    public void registerForComposingNotification(Handler handler, int i, Object obj) {
        this.mComposingRegistrants.add(new Registrant(handler, i, obj));
    }

    public void unregisterForComposingNotification(Handler handler) {
        this.mComposingRegistrants.remove(handler);
    }

    public void registerForImdnNotification(Handler handler, int i, Object obj) {
        this.mImdnHandler.registerForImdnNotification(handler, i, obj);
    }

    public void unregisterForImdnNotification(Handler handler) {
        this.mImdnHandler.unregisterForImdnNotification(handler);
    }

    public void registerForMessageFailed(Handler handler, int i, Object obj) {
        this.mMessageFailedRegistrants.add(new Registrant(handler, i, obj));
    }

    public void registerForImdnResponse(Handler handler, int i, Object obj) {
        this.mImdnHandler.registerForImdnResponse(handler, i, obj);
    }

    public void unregisterForImdnResponse(Handler handler) {
        this.mImdnHandler.unregisterForImdnResponse(handler);
    }

    public void unregisterForMessageFailed(Handler handler) {
        this.mMessageFailedRegistrants.remove(handler);
    }

    public void registerForImdnFailed(Handler handler, int i, Object obj) {
        this.mImdnFailedRegistrants.add(new Registrant(handler, i, obj));
    }

    public void unregisterForImdnFailed(Handler handler) {
        this.mImdnFailedRegistrants.remove(handler);
    }

    public void registerForConferenceInfoUpdate(Handler handler, int i, Object obj) {
        this.mConferenceInfoUpdateRegistrants.add(new Registrant(handler, i, obj));
    }

    public void unregisterForConferenceInfoUpdate(Handler handler) {
        this.mConferenceInfoUpdateRegistrants.remove(handler);
    }

    public void registerForImSessionEstablished(Handler handler, int i, Object obj) {
        this.mSessionEstablishedRegistrants.add(new Registrant(handler, i, obj));
    }

    public void unregisterForImSessionEstablished(Handler handler) {
        this.mSessionEstablishedRegistrants.remove(handler);
    }

    public void registerForImSessionClosed(Handler handler, int i, Object obj) {
        this.mSessionClosedRegistrants.add(new Registrant(handler, i, obj));
    }

    public void unregisterForImSessionClosed(Handler handler) {
        this.mSessionClosedRegistrants.remove(handler);
    }

    public void registerForImIncomingSession(Handler handler, int i, Object obj) {
        String str = this.LOG_TAG;
        Log.i(str, "registerForImIncomingSession(): " + handler);
        this.mIncomingSessionRegistrants.add(new Registrant(handler, i, obj));
    }

    public void unregisterForImIncomingSession(Handler handler) {
        this.mIncomingSessionRegistrants.remove(handler);
    }

    public void registerForImIncomingFileTransfer(Handler handler, int i, Object obj) {
        this.mIncomingFileTransferRegistrants.add(new Registrant(handler, i, obj));
    }

    public void unregisterForImIncomingFileTransfer(Handler handler) {
        this.mIncomingFileTransferRegistrants.remove(handler);
    }

    public void registerForImIncomingMessage(Handler handler, int i, Object obj) {
        this.mIncomingMessageRegistrants.add(new Registrant(handler, i, obj));
    }

    public void unregisterForImIncomingMessage(Handler handler) {
        this.mIncomingMessageRegistrants.remove(handler);
    }

    public void registerForTransferProgress(Handler handler, int i, Object obj) {
        this.mTransferProgressRegistrants.add(new Registrant(handler, i, obj));
    }

    public void unregisterForTransferProgress(Handler handler) {
        this.mTransferProgressRegistrants.remove(handler);
    }

    public void setFtMessageId(Object obj, int i) {
        Integer num = (Integer) obj;
        String str = this.LOG_TAG;
        Log.i(str, "setFtMessageId():  sessionHandle = " + num + ", msgId:" + i);
        FtSession ftSession = this.mFtSessions.get(num);
        if (ftSession == null) {
            String str2 = this.LOG_TAG;
            Log.i(str2, "setFtMessageId(): no session in map, id = " + num);
            return;
        }
        ftSession.mId = i;
    }

    public void handleMessage(Message message) {
        switch (message.what) {
            case 1:
                handleStartImSessionRequest((StartImSessionParams) message.obj);
                return;
            case 2:
                handleAcceptImSessionRequest((AcceptImSessionParams) message.obj);
                return;
            case 3:
                handleCloseImSessionRequest((StopImSessionParams) message.obj);
                return;
            case 4:
                handleSendMessageRequest((SendMessageParams) message.obj);
                return;
            case 5:
                handleAcceptFtSessionRequest((AcceptFtSessionParams) message.obj);
                return;
            case 6:
                handleCancelFtSessionRequest((RejectFtSessionParams) message.obj);
                return;
            case 7:
                handleRejectFtSessionRequest((RejectFtSessionParams) message.obj);
                return;
            case 8:
                handleStartFtSessionRequest((SendFtSessionParams) message.obj);
                return;
            case 9:
                handleSendComposingNotification((ImSendComposingParams) message.obj);
                return;
            case 10:
                handleSendDispositionNotification((SendImdnParams) message.obj);
                return;
            case 12:
                handleAddParticipantsRequest((AddParticipantsParams) message.obj);
                return;
            case 13:
                handleStartImSessionRequest((StartImSessionParams) message.obj);
                return;
            case 14:
                handleSendFtDispositionNotification((SendImdnParams) message.obj);
                return;
            case 16:
                return;
            case 17:
                handleRejectImSessionRequest((RejectImSessionParams) message.obj);
                return;
            case 18:
                handleStartFtMediaRequest(((Integer) message.obj).intValue());
                return;
            case 19:
                handleChangeGroupChatLeaderRequest((ChangeGroupChatLeaderParams) message.obj);
                return;
            case 20:
                handleRejectImSessionRequest((RejectImSessionParams) message.obj);
                return;
            case 21:
                handleRemoveParticipantsRequest((RemoveParticipantsParams) message.obj);
                return;
            case 22:
                handleChangeGroupChatSubjectRequest((ChangeGroupChatSubjectParams) message.obj);
                return;
            case 23:
                handleChangeGroupChatAliasRequest((ChangeGroupAliasParams) message.obj);
                return;
            case 24:
                onSubscribeGroupChatList((GroupChatListParams) message.obj);
                return;
            case 25:
                onSubscribeGroupChatInfo((GroupChatInfoParams) message.obj);
                return;
            case 28:
                handleSendMessageRevokeRequest((SendMessageRevokeParams) message.obj);
                return;
            case 29:
                handleSetMoreInfoToSipUARequest((String) message.obj, message.arg1);
                return;
            case 30:
                handleChangeGroupChatIconRequest((ChangeGroupChatIconParams) message.obj);
                return;
            case 31:
                handleReportChatbotAsSpam((ReportChatbotAsSpamParams) message.obj);
                return;
            case 32:
                handleRequestChatbotAnonymize((ChatbotAnonymizeParams) message.obj);
                return;
            default:
                Log.e(this.LOG_TAG, "handleMessage: Undefined message.");
                return;
        }
    }

    /* JADX WARNING: Removed duplicated region for block: B:15:0x005c  */
    /* JADX WARNING: Removed duplicated region for block: B:26:0x009b  */
    /* Code decompiled incorrectly, please refer to instructions dump. */
    private void handleStartImSessionRequest(com.sec.internal.constants.ims.servicemodules.im.params.StartImSessionParams r17) {
        /*
            r16 = this;
            r0 = r16
            r1 = r17
            java.lang.String r2 = r0.LOG_TAG
            java.lang.StringBuilder r3 = new java.lang.StringBuilder
            r3.<init>()
            java.lang.String r4 = "handleStartImSessionRequest: params = "
            r3.append(r4)
            r3.append(r1)
            java.lang.String r3 = r3.toString()
            com.sec.internal.log.IMSLog.s(r2, r3)
            android.os.Message r2 = r1.mSynchronousCallback
            java.lang.String r4 = ""
            r5 = 0
            if (r2 == 0) goto L_0x0027
            java.lang.Object r2 = r2.obj
            java.lang.String r2 = (java.lang.String) r2
            r6 = r5
            goto L_0x0029
        L_0x0027:
            r2 = r4
            r6 = 1
        L_0x0029:
            java.lang.String r7 = r1.mOwnImsi
            com.sec.internal.ims.core.handler.secims.UserAgent r7 = r0.getUserAgent((java.lang.String) r7)
            if (r7 != 0) goto L_0x0039
            java.lang.String r6 = r0.LOG_TAG
            java.lang.String r8 = "handleStartImSessionRequest(): UserAgent not found."
            android.util.Log.e(r6, r8)
            r6 = 1
        L_0x0039:
            java.util.List<com.sec.ims.util.ImsUri> r8 = r1.mReceivers
            int r8 = r8.size()
            if (r8 != 0) goto L_0x004a
            java.lang.String r6 = r0.LOG_TAG
            java.lang.String r8 = "handleStartImSessionRequest(): receiver.size() = 0 !"
            android.util.Log.e(r6, r8)
        L_0x0048:
            r6 = 1
            goto L_0x005a
        L_0x004a:
            java.util.List<com.sec.ims.util.ImsUri> r8 = r1.mReceivers
            java.lang.Object r8 = r8.get(r5)
            if (r8 != 0) goto L_0x005a
            java.lang.String r6 = r0.LOG_TAG
            java.lang.String r8 = "handleStartImSessionRequest(): null receiver!"
            android.util.Log.e(r6, r8)
            goto L_0x0048
        L_0x005a:
            if (r6 == 0) goto L_0x009b
            android.os.Message r3 = r1.mSynchronousCallback
            r4 = 0
            if (r3 == 0) goto L_0x0066
            r0.sendCallback(r3, r2)
            r1.mSynchronousCallback = r4
        L_0x0066:
            android.os.Message r3 = r1.mCallback
            if (r3 == 0) goto L_0x007d
            com.sec.internal.constants.ims.servicemodules.im.result.StartImSessionResult r5 = new com.sec.internal.constants.ims.servicemodules.im.result.StartImSessionResult
            com.sec.internal.constants.ims.servicemodules.im.result.Result r6 = new com.sec.internal.constants.ims.servicemodules.im.result.Result
            com.sec.internal.constants.ims.servicemodules.im.ImError r7 = com.sec.internal.constants.ims.servicemodules.im.ImError.ENGINE_ERROR
            com.sec.internal.constants.ims.servicemodules.im.result.Result$Type r8 = com.sec.internal.constants.ims.servicemodules.im.result.Result.Type.ENGINE_ERROR
            r6.<init>((com.sec.internal.constants.ims.servicemodules.im.ImError) r7, (com.sec.internal.constants.ims.servicemodules.im.result.Result.Type) r8)
            r5.<init>(r6, r4, r2)
            r0.sendCallback(r3, r5)
            r1.mCallback = r4
        L_0x007d:
            com.sec.internal.constants.ims.servicemodules.im.params.SendMessageParams r3 = r1.mSendMessageParams
            if (r3 == 0) goto L_0x009a
            android.os.Message r3 = r3.mCallback
            if (r3 == 0) goto L_0x009a
            com.sec.internal.constants.ims.servicemodules.im.result.SendMessageResult r5 = new com.sec.internal.constants.ims.servicemodules.im.result.SendMessageResult
            com.sec.internal.constants.ims.servicemodules.im.result.Result r6 = new com.sec.internal.constants.ims.servicemodules.im.result.Result
            com.sec.internal.constants.ims.servicemodules.im.ImError r7 = com.sec.internal.constants.ims.servicemodules.im.ImError.ENGINE_ERROR
            com.sec.internal.constants.ims.servicemodules.im.result.Result$Type r8 = com.sec.internal.constants.ims.servicemodules.im.result.Result.Type.ENGINE_ERROR
            r6.<init>((com.sec.internal.constants.ims.servicemodules.im.ImError) r7, (com.sec.internal.constants.ims.servicemodules.im.result.Result.Type) r8)
            r5.<init>(r2, r6)
            r0.sendCallback(r3, r5)
            com.sec.internal.constants.ims.servicemodules.im.params.SendMessageParams r0 = r1.mSendMessageParams
            r0.mCallback = r4
        L_0x009a:
            return
        L_0x009b:
            com.sec.internal.ims.core.handler.secims.ResipImHandler$ImSession r6 = new com.sec.internal.ims.core.handler.secims.ResipImHandler$ImSession
            java.lang.String r9 = r1.mChatId
            android.os.Message r10 = r1.mCallback
            android.os.Message r11 = r1.mSynchronousCallback
            android.os.Message r12 = r1.mDedicatedBearerCallback
            r13 = 0
            int r14 = r7.getHandle()
            r8 = r6
            r8.<init>(r9, r10, r11, r12, r13, r14)
            java.util.Map<java.lang.String, com.sec.internal.ims.core.handler.secims.ResipImHandler$ImSession> r8 = r0.mPendingSessions
            r8.put(r2, r6)
            com.google.flatbuffers.FlatBufferBuilder r8 = new com.google.flatbuffers.FlatBufferBuilder
            r8.<init>(r5)
            java.util.List<com.sec.ims.util.ImsUri> r9 = r1.mReceivers
            int r10 = r9.size()
            int[] r9 = r0.getImsUriOffsetArray(r8, r9, r10)
            int r9 = com.sec.internal.ims.core.handler.secims.imsCommonStruc.BaseSessionData.createReceiversVector(r8, r9)
            java.lang.String r2 = r0.parseStr(r2)
            int r2 = r8.createString((java.lang.CharSequence) r2)
            java.lang.String r10 = r1.mSdpContentType
            java.lang.String r10 = r0.parseStr(r10)
            int r10 = r8.createString((java.lang.CharSequence) r10)
            java.lang.String r11 = r1.mUserAlias
            java.lang.String r11 = r0.parseStr(r11)
            int r11 = r8.createString((java.lang.CharSequence) r11)
            java.lang.String r12 = r1.mContributionId
            java.lang.String r12 = r0.parseStr(r12)
            int r12 = r8.createString((java.lang.CharSequence) r12)
            java.lang.String r13 = r1.mConversationId
            java.lang.String r13 = r0.parseStr(r13)
            int r13 = r8.createString((java.lang.CharSequence) r13)
            java.lang.String r14 = r1.mInReplyToContributionId
            java.lang.String r14 = r0.parseStr(r14)
            int r14 = r8.createString((java.lang.CharSequence) r14)
            java.lang.String r15 = r1.mPrevContributionId
            java.lang.String r15 = r0.parseStr(r15)
            int r15 = r8.createString((java.lang.CharSequence) r15)
            java.lang.String r5 = r1.mServiceId
            java.lang.String r5 = r0.parseStr(r5)
            int r5 = r8.createString((java.lang.CharSequence) r5)
            com.sec.internal.constants.ims.servicemodules.im.ChatMode r3 = r1.mChatMode
            if (r3 == 0) goto L_0x011d
            java.lang.String r3 = r3.toString()
            goto L_0x011e
        L_0x011d:
            r3 = r4
        L_0x011e:
            int r3 = r8.createString((java.lang.CharSequence) r3)
            com.sec.internal.ims.core.handler.secims.imsCommonStruc.BaseSessionData.startBaseSessionData(r8)
            com.sec.internal.ims.core.handler.secims.imsCommonStruc.BaseSessionData.addId(r8, r2)
            boolean r2 = r1.mIsConf
            com.sec.internal.ims.core.handler.secims.imsCommonStruc.BaseSessionData.addIsConference(r8, r2)
            boolean r2 = r1.mIsChatbotParticipant
            com.sec.internal.ims.core.handler.secims.imsCommonStruc.BaseSessionData.addIsChatbotParticipant(r8, r2)
            com.sec.internal.ims.core.handler.secims.imsCommonStruc.BaseSessionData.addSdpContentType(r8, r10)
            com.sec.internal.ims.core.handler.secims.imsCommonStruc.BaseSessionData.addReceivers(r8, r9)
            java.lang.String r2 = r1.mUserAlias
            if (r2 == 0) goto L_0x013f
            com.sec.internal.ims.core.handler.secims.imsCommonStruc.BaseSessionData.addUserAlias(r8, r11)
        L_0x013f:
            java.lang.String r2 = r1.mContributionId
            if (r2 == 0) goto L_0x0146
            com.sec.internal.ims.core.handler.secims.imsCommonStruc.BaseSessionData.addContributionId(r8, r12)
        L_0x0146:
            java.lang.String r2 = r1.mConversationId
            if (r2 == 0) goto L_0x014d
            com.sec.internal.ims.core.handler.secims.imsCommonStruc.BaseSessionData.addConversationId(r8, r13)
        L_0x014d:
            java.lang.String r2 = r1.mInReplyToContributionId
            if (r2 == 0) goto L_0x0154
            com.sec.internal.ims.core.handler.secims.imsCommonStruc.BaseSessionData.addInReplyToContributionId(r8, r14)
        L_0x0154:
            java.lang.String r2 = r1.mPrevContributionId
            if (r2 == 0) goto L_0x015b
            com.sec.internal.ims.core.handler.secims.imsCommonStruc.BaseSessionData.addSessionReplaces(r8, r15)
        L_0x015b:
            java.lang.String r2 = r1.mServiceId
            boolean r2 = android.text.TextUtils.isEmpty(r2)
            if (r2 != 0) goto L_0x0166
            com.sec.internal.ims.core.handler.secims.imsCommonStruc.BaseSessionData.addServiceId(r8, r5)
        L_0x0166:
            com.sec.internal.constants.ims.servicemodules.im.ChatMode r2 = r1.mChatMode
            if (r2 == 0) goto L_0x016d
            com.sec.internal.ims.core.handler.secims.imsCommonStruc.BaseSessionData.addChatMode(r8, r3)
        L_0x016d:
            int r2 = com.sec.internal.ims.core.handler.secims.imsCommonStruc.BaseSessionData.endBaseSessionData(r8)
            java.util.List<java.lang.String> r3 = r1.mAcceptTypes
            r5 = -1
            if (r3 == 0) goto L_0x018b
            boolean r3 = r3.isEmpty()
            if (r3 != 0) goto L_0x018b
            java.util.List<java.lang.String> r3 = r1.mAcceptTypes
            int r9 = r3.size()
            int[] r3 = r0.getStringOffsetArray(r8, r3, r9)
            int r3 = com.sec.internal.ims.core.handler.secims.imsCommonStruc.ImSessionParam.createAcceptTypesVector(r8, r3)
            goto L_0x018c
        L_0x018b:
            r3 = r5
        L_0x018c:
            java.util.List<java.lang.String> r9 = r1.mAcceptWrappedTypes
            if (r9 == 0) goto L_0x01a5
            boolean r9 = r9.isEmpty()
            if (r9 != 0) goto L_0x01a5
            java.util.List<java.lang.String> r9 = r1.mAcceptWrappedTypes
            int r10 = r9.size()
            int[] r9 = r0.getStringOffsetArray(r8, r9, r10)
            int r9 = com.sec.internal.ims.core.handler.secims.imsCommonStruc.ImSessionParam.createAcceptWrappedTypesVector(r8, r9)
            goto L_0x01a6
        L_0x01a5:
            r9 = r5
        L_0x01a6:
            java.lang.String r10 = r1.mSubject
            java.lang.String r10 = r0.parseStr(r10)
            int r10 = r8.createString((java.lang.CharSequence) r10)
            com.sec.internal.ims.core.handler.secims.imsCommonStruc.ImSessionParam.startImSessionParam(r8)
            boolean r11 = r1.mIsRejoin
            com.sec.internal.ims.core.handler.secims.imsCommonStruc.ImSessionParam.addIsRejoin(r8, r11)
            boolean r11 = r1.mIsClosedGroupChat
            com.sec.internal.ims.core.handler.secims.imsCommonStruc.ImSessionParam.addIsClosedGroupchat(r8, r11)
            boolean r11 = r1.mIsInviteForBye
            com.sec.internal.ims.core.handler.secims.imsCommonStruc.ImSessionParam.addIsInviteforbye(r8, r11)
            boolean r11 = r1.mIsGeolocationPush
            com.sec.internal.ims.core.handler.secims.imsCommonStruc.ImSessionParam.addIsGeolocationPush(r8, r11)
            java.lang.String r11 = r1.mSubject
            if (r11 == 0) goto L_0x01ce
            com.sec.internal.ims.core.handler.secims.imsCommonStruc.ImSessionParam.addSubject(r8, r10)
        L_0x01ce:
            java.util.List<java.lang.String> r10 = r1.mAcceptTypes
            if (r10 == 0) goto L_0x01db
            boolean r10 = r10.isEmpty()
            if (r10 != 0) goto L_0x01db
            com.sec.internal.ims.core.handler.secims.imsCommonStruc.ImSessionParam.addAcceptTypes(r8, r3)
        L_0x01db:
            java.lang.String r3 = r1.mServiceId
            boolean r3 = android.text.TextUtils.isEmpty(r3)
            if (r3 != 0) goto L_0x01e7
            r3 = 1
            com.sec.internal.ims.core.handler.secims.imsCommonStruc.ImSessionParam.addIsExtension(r8, r3)
        L_0x01e7:
            java.util.List<java.lang.String> r3 = r1.mAcceptWrappedTypes
            if (r3 == 0) goto L_0x01f4
            boolean r3 = r3.isEmpty()
            if (r3 != 0) goto L_0x01f4
            com.sec.internal.ims.core.handler.secims.imsCommonStruc.ImSessionParam.addAcceptWrappedTypes(r8, r9)
        L_0x01f4:
            com.sec.internal.ims.core.handler.secims.imsCommonStruc.ImSessionParam.addBaseSessionData(r8, r2)
            int r2 = com.sec.internal.ims.core.handler.secims.imsCommonStruc.ImSessionParam.endImSessionParam(r8)
            com.sec.internal.constants.ims.servicemodules.im.params.SendMessageParams r1 = r1.mSendMessageParams
            if (r1 == 0) goto L_0x03ad
            java.util.ArrayList r3 = new java.util.ArrayList
            r3.<init>()
            java.lang.String r9 = r1.mContentType
            if (r9 == 0) goto L_0x0233
            boolean r9 = r9.isEmpty()
            if (r9 != 0) goto L_0x0233
            java.lang.String r9 = r1.mContentType
            java.util.Locale r10 = java.util.Locale.US
            java.lang.String r9 = r9.toLowerCase(r10)
            java.lang.String r10 = "charset="
            boolean r9 = r9.contains(r10)
            if (r9 != 0) goto L_0x0233
            java.lang.StringBuilder r9 = new java.lang.StringBuilder
            r9.<init>()
            java.lang.String r10 = r1.mContentType
            r9.append(r10)
            java.lang.String r10 = ";charset=UTF-8"
            r9.append(r10)
            java.lang.String r9 = r9.toString()
            r1.mContentType = r9
        L_0x0233:
            java.lang.String r9 = r1.mMaapTrafficType
            if (r9 == 0) goto L_0x027b
            java.lang.String r9 = "maap"
            int r9 = r8.createString((java.lang.CharSequence) r9)
            java.lang.String r10 = "<http://www.gsma.com/rcs/maap/>"
            int r10 = r8.createString((java.lang.CharSequence) r10)
            java.lang.String r11 = "Traffic-Type"
            int r11 = r8.createString((java.lang.CharSequence) r11)
            java.lang.String r12 = r1.mMaapTrafficType
            int r12 = r8.createString((java.lang.CharSequence) r12)
            com.sec.internal.ims.core.handler.secims.imsCommonStruc.CpimNamespace_.Pair.startPair(r8)
            com.sec.internal.ims.core.handler.secims.imsCommonStruc.CpimNamespace_.Pair.addKey(r8, r11)
            com.sec.internal.ims.core.handler.secims.imsCommonStruc.CpimNamespace_.Pair.addValue(r8, r12)
            int r11 = com.sec.internal.ims.core.handler.secims.imsCommonStruc.CpimNamespace_.Pair.endPair(r8)
            int[] r11 = new int[]{r11}
            int r11 = com.sec.internal.ims.core.handler.secims.imsCommonStruc.CpimNamespace.createHeadersVector(r8, r11)
            com.sec.internal.ims.core.handler.secims.imsCommonStruc.CpimNamespace.startCpimNamespace(r8)
            com.sec.internal.ims.core.handler.secims.imsCommonStruc.CpimNamespace.addName(r8, r9)
            com.sec.internal.ims.core.handler.secims.imsCommonStruc.CpimNamespace.addUri(r8, r10)
            com.sec.internal.ims.core.handler.secims.imsCommonStruc.CpimNamespace.addHeaders(r8, r11)
            int r9 = com.sec.internal.ims.core.handler.secims.imsCommonStruc.CpimNamespace.endCpimNamespace(r8)
            java.lang.Integer r9 = java.lang.Integer.valueOf(r9)
            r3.add(r9)
        L_0x027b:
            java.lang.String r9 = r1.mReferenceId
            if (r9 != 0) goto L_0x028b
            java.lang.String r9 = r1.mReferenceType
            if (r9 != 0) goto L_0x028b
            java.lang.String r9 = r1.mReferenceValue
            if (r9 == 0) goto L_0x0288
            goto L_0x028b
        L_0x0288:
            r11 = 0
            goto L_0x032b
        L_0x028b:
            java.lang.String r9 = "Extended-RCS"
            int r9 = r8.createString((java.lang.CharSequence) r9)
            java.lang.String r10 = "<http://www.tta.or.kr>"
            int r10 = r8.createString((java.lang.CharSequence) r10)
            r11 = 0
            int[] r12 = new int[r11]
            java.lang.String r13 = r1.mReferenceId
            if (r13 == 0) goto L_0x02c0
            java.lang.String r13 = "Reference-ID"
            int r13 = r8.createString((java.lang.CharSequence) r13)
            java.lang.String r14 = r1.mReferenceId
            int r14 = r8.createString((java.lang.CharSequence) r14)
            com.sec.internal.ims.core.handler.secims.imsCommonStruc.CpimNamespace_.Pair.startPair(r8)
            com.sec.internal.ims.core.handler.secims.imsCommonStruc.CpimNamespace_.Pair.addKey(r8, r13)
            com.sec.internal.ims.core.handler.secims.imsCommonStruc.CpimNamespace_.Pair.addValue(r8, r14)
            int r13 = com.sec.internal.ims.core.handler.secims.imsCommonStruc.CpimNamespace_.Pair.endPair(r8)
            r14 = 1
            int[] r12 = java.util.Arrays.copyOf(r12, r14)
            int r15 = r12.length
            int r15 = r15 - r14
            r12[r15] = r13
        L_0x02c0:
            java.lang.String r13 = r1.mReferenceType
            if (r13 == 0) goto L_0x02e8
            java.lang.String r13 = "Reference-Type"
            int r13 = r8.createString((java.lang.CharSequence) r13)
            java.lang.String r14 = r1.mReferenceType
            int r14 = r8.createString((java.lang.CharSequence) r14)
            com.sec.internal.ims.core.handler.secims.imsCommonStruc.CpimNamespace_.Pair.startPair(r8)
            com.sec.internal.ims.core.handler.secims.imsCommonStruc.CpimNamespace_.Pair.addKey(r8, r13)
            com.sec.internal.ims.core.handler.secims.imsCommonStruc.CpimNamespace_.Pair.addValue(r8, r14)
            int r13 = com.sec.internal.ims.core.handler.secims.imsCommonStruc.CpimNamespace_.Pair.endPair(r8)
            int r14 = r12.length
            r15 = 1
            int r14 = r14 + r15
            int[] r12 = java.util.Arrays.copyOf(r12, r14)
            int r14 = r12.length
            int r14 = r14 - r15
            r12[r14] = r13
        L_0x02e8:
            java.lang.String r13 = r1.mReferenceValue
            if (r13 == 0) goto L_0x0310
            java.lang.String r13 = "Reference-Value"
            int r13 = r8.createString((java.lang.CharSequence) r13)
            java.lang.String r14 = r1.mReferenceValue
            int r14 = r8.createString((java.lang.CharSequence) r14)
            com.sec.internal.ims.core.handler.secims.imsCommonStruc.CpimNamespace_.Pair.startPair(r8)
            com.sec.internal.ims.core.handler.secims.imsCommonStruc.CpimNamespace_.Pair.addKey(r8, r13)
            com.sec.internal.ims.core.handler.secims.imsCommonStruc.CpimNamespace_.Pair.addValue(r8, r14)
            int r13 = com.sec.internal.ims.core.handler.secims.imsCommonStruc.CpimNamespace_.Pair.endPair(r8)
            int r14 = r12.length
            r15 = 1
            int r14 = r14 + r15
            int[] r12 = java.util.Arrays.copyOf(r12, r14)
            int r14 = r12.length
            int r14 = r14 - r15
            r12[r14] = r13
        L_0x0310:
            int r12 = com.sec.internal.ims.core.handler.secims.imsCommonStruc.CpimNamespace.createHeadersVector(r8, r12)
            com.sec.internal.ims.core.handler.secims.imsCommonStruc.CpimNamespace.startCpimNamespace(r8)
            com.sec.internal.ims.core.handler.secims.imsCommonStruc.CpimNamespace.addName(r8, r9)
            com.sec.internal.ims.core.handler.secims.imsCommonStruc.CpimNamespace.addUri(r8, r10)
            com.sec.internal.ims.core.handler.secims.imsCommonStruc.CpimNamespace.addHeaders(r8, r12)
            int r9 = com.sec.internal.ims.core.handler.secims.imsCommonStruc.CpimNamespace.endCpimNamespace(r8)
            java.lang.Integer r9 = java.lang.Integer.valueOf(r9)
            r3.add(r9)
        L_0x032b:
            int r9 = r3.size()
            if (r9 <= 0) goto L_0x034c
            int r5 = r3.size()
            int[] r9 = new int[r5]
        L_0x0337:
            if (r11 >= r5) goto L_0x0348
            java.lang.Object r10 = r3.get(r11)
            java.lang.Integer r10 = (java.lang.Integer) r10
            int r10 = r10.intValue()
            r9[r11] = r10
            int r11 = r11 + 1
            goto L_0x0337
        L_0x0348:
            int r5 = com.sec.internal.ims.core.handler.secims.imsCommonStruc.ImMessageParam.createCpimNamespacesVector(r8, r9)
        L_0x034c:
            android.os.Message r9 = r1.mCallback
            r6.mFirstMessageCallback = r9
            java.util.Set<com.sec.internal.constants.ims.servicemodules.im.NotificationStatus> r6 = r1.mDispositionNotification
            int[] r6 = com.sec.internal.ims.translate.ResipTranslatorCollection.translateFwImdnNoti(r6)
            int r6 = com.sec.internal.ims.core.handler.secims.imsCommonStruc.ImdnParams.createNotiVector(r8, r6)
            java.lang.String r9 = r1.mImdnMessageId
            java.lang.String r9 = r0.parseStr(r9)
            int r9 = r8.createString((java.lang.CharSequence) r9)
            java.util.Date r10 = r1.mImdnTime
            if (r10 == 0) goto L_0x036c
            java.lang.String r4 = com.sec.internal.helper.Iso8601.formatMillis(r10)
        L_0x036c:
            int r4 = r8.createString((java.lang.CharSequence) r4)
            java.lang.String r10 = r1.mBody
            java.lang.String r10 = r0.parseStr(r10)
            int r10 = r8.createString((java.lang.CharSequence) r10)
            java.lang.String r11 = r1.mContentType
            java.lang.String r11 = r0.parseStr(r11)
            int r11 = r8.createString((java.lang.CharSequence) r11)
            com.sec.internal.ims.core.handler.secims.imsCommonStruc.ImdnParams.startImdnParams(r8)
            com.sec.internal.ims.core.handler.secims.imsCommonStruc.ImdnParams.addMessageId(r8, r9)
            com.sec.internal.ims.core.handler.secims.imsCommonStruc.ImdnParams.addDatetime(r8, r4)
            com.sec.internal.ims.core.handler.secims.imsCommonStruc.ImdnParams.addNoti(r8, r6)
            int r4 = com.sec.internal.ims.core.handler.secims.imsCommonStruc.ImdnParams.endImdnParams(r8)
            com.sec.internal.ims.core.handler.secims.imsCommonStruc.ImMessageParam.startImMessageParam(r8)
            com.sec.internal.ims.core.handler.secims.imsCommonStruc.ImMessageParam.addBody(r8, r10)
            com.sec.internal.ims.core.handler.secims.imsCommonStruc.ImMessageParam.addContentType(r8, r11)
            com.sec.internal.ims.core.handler.secims.imsCommonStruc.ImMessageParam.addImdn(r8, r4)
            int r3 = r3.size()
            if (r3 <= 0) goto L_0x03a9
            com.sec.internal.ims.core.handler.secims.imsCommonStruc.ImMessageParam.addCpimNamespaces(r8, r5)
        L_0x03a9:
            int r5 = com.sec.internal.ims.core.handler.secims.imsCommonStruc.ImMessageParam.endImMessageParam(r8)
        L_0x03ad:
            com.sec.internal.ims.core.handler.secims.imsCommonStruc.Request_.RequestStartImSession.startRequestStartImSession(r8)
            int r3 = r7.getHandle()
            long r3 = (long) r3
            com.sec.internal.ims.core.handler.secims.imsCommonStruc.Request_.RequestStartImSession.addRegistrationHandle(r8, r3)
            com.sec.internal.ims.core.handler.secims.imsCommonStruc.Request_.RequestStartImSession.addSession(r8, r2)
            if (r1 == 0) goto L_0x03c0
            com.sec.internal.ims.core.handler.secims.imsCommonStruc.Request_.RequestStartImSession.addMessageParam(r8, r5)
        L_0x03c0:
            int r1 = com.sec.internal.ims.core.handler.secims.imsCommonStruc.Request_.RequestStartImSession.endRequestStartImSession(r8)
            com.sec.internal.ims.core.handler.secims.imsCommonStruc.Request.startRequest(r8)
            r2 = 501(0x1f5, float:7.02E-43)
            com.sec.internal.ims.core.handler.secims.imsCommonStruc.Request.addReqid(r8, r2)
            r2 = 39
            com.sec.internal.ims.core.handler.secims.imsCommonStruc.Request.addReqType(r8, r2)
            com.sec.internal.ims.core.handler.secims.imsCommonStruc.Request.addReq(r8, r1)
            int r3 = com.sec.internal.ims.core.handler.secims.imsCommonStruc.Request.endRequest(r8)
            java.lang.String r1 = r0.LOG_TAG
            java.lang.String r2 = "handleStartImSessionRequest(): Armaan: sending to stack!"
            android.util.Log.e(r1, r2)
            r1 = 501(0x1f5, float:7.02E-43)
            com.sec.internal.ims.core.handler.secims.ResipImResponseHandler r2 = r0.mStackResponseHandler
            r4 = 1
            android.os.Message r4 = r2.obtainMessage(r4)
            r0 = r16
            r2 = r8
            r5 = r7
            r0.sendRequestToStack(r1, r2, r3, r4, r5)
            return
        */
        throw new UnsupportedOperationException("Method not decompiled: com.sec.internal.ims.core.handler.secims.ResipImHandler.handleStartImSessionRequest(com.sec.internal.constants.ims.servicemodules.im.params.StartImSessionParams):void");
    }

    private void handleAcceptImSessionRequest(AcceptImSessionParams acceptImSessionParams) {
        String str = this.LOG_TAG;
        IMSLog.s(str, "handleAcceptImSessionRequest(): params " + acceptImSessionParams);
        int intValue = ((Integer) acceptImSessionParams.mRawHandle).intValue();
        ImSession imSession = this.mSessions.get(Integer.valueOf(intValue));
        if (imSession == null) {
            Log.e(this.LOG_TAG, "handleAcceptImSessionRequest: no session in map, return accept failure");
            Message message = acceptImSessionParams.mCallback;
            if (message != null) {
                sendCallback(message, new StartImSessionResult(new Result(ImError.ENGINE_ERROR, Result.Type.ENGINE_ERROR), (ImsUri) null, Integer.valueOf(intValue)));
                acceptImSessionParams.mCallback = null;
                return;
            }
            return;
        }
        imSession.mChatId = acceptImSessionParams.mChatId;
        imSession.mAcceptCallback = acceptImSessionParams.mCallback;
        imSession.mIsSnF = acceptImSessionParams.mIsSnF;
        UserAgent userAgent = getUserAgent(imSession.mUaHandle);
        if (userAgent == null) {
            String str2 = this.LOG_TAG;
            Log.e(str2, "handleAcceptImSessionRequest(): UserAgent not found. UaHandle = " + imSession.mUaHandle);
            Message message2 = imSession.mAcceptCallback;
            if (message2 != null) {
                sendCallback(message2, new StartImSessionResult(new Result(ImError.ENGINE_ERROR, Result.Type.ENGINE_ERROR), (ImsUri) null, Integer.valueOf(intValue)));
                imSession.mAcceptCallback = null;
                return;
            }
            return;
        }
        String parseStr = parseStr(acceptImSessionParams.mUserAlias);
        FlatBufferBuilder flatBufferBuilder = new FlatBufferBuilder(0);
        int createString = flatBufferBuilder.createString((CharSequence) parseStr(parseStr));
        RequestAcceptImSession.startRequestAcceptImSession(flatBufferBuilder);
        RequestAcceptImSession.addSessionId(flatBufferBuilder, (long) intValue);
        RequestAcceptImSession.addUserAlias(flatBufferBuilder, createString);
        int endRequestAcceptImSession = RequestAcceptImSession.endRequestAcceptImSession(flatBufferBuilder);
        Request.startRequest(flatBufferBuilder);
        Request.addReqid(flatBufferBuilder, 502);
        Request.addReqType(flatBufferBuilder, (byte) 40);
        Request.addReq(flatBufferBuilder, endRequestAcceptImSession);
        sendRequestToStack(502, flatBufferBuilder, Request.endRequest(flatBufferBuilder), this.mStackResponseHandler.obtainMessage(2), userAgent);
    }

    /* access modifiers changed from: protected */
    public void handleRejectImSessionRequest(RejectImSessionParams rejectImSessionParams) {
        String str = this.LOG_TAG;
        Log.i(str, "handleRejectImSessionRequest: " + rejectImSessionParams);
        int intValue = ((Integer) rejectImSessionParams.mRawHandle).intValue();
        ImSession imSession = this.mSessions.get(Integer.valueOf(intValue));
        if (imSession == null) {
            Log.e(this.LOG_TAG, "handleRejectImSessionRequest: no session in map, return reject failure");
            Message message = rejectImSessionParams.mCallback;
            if (message != null) {
                sendCallback(message, new RejectImSessionResult(Integer.valueOf(intValue), ImError.ENGINE_ERROR));
                rejectImSessionParams.mCallback = null;
                return;
            }
            return;
        }
        imSession.mRejectCallback = rejectImSessionParams.mCallback;
        UserAgent userAgent = getUserAgent(imSession.mUaHandle);
        if (userAgent == null) {
            Log.e(this.LOG_TAG, "handleRejectImSessionRequest: User Agent not found");
            Message message2 = rejectImSessionParams.mCallback;
            if (message2 != null) {
                sendCallback(message2, new RejectImSessionResult(Integer.valueOf(intValue), ImError.ENGINE_ERROR));
                rejectImSessionParams.mCallback = null;
                return;
            }
            return;
        }
        FlatBufferBuilder flatBufferBuilder = new FlatBufferBuilder(0);
        int createString = flatBufferBuilder.createString((CharSequence) parseStr(rejectImSessionParams.mSessionRejectReason.getWarningText()));
        WarningHdr.startWarningHdr(flatBufferBuilder);
        WarningHdr.addCode(flatBufferBuilder, rejectImSessionParams.mSessionRejectReason.getWarningCode());
        WarningHdr.addText(flatBufferBuilder, createString);
        int endWarningHdr = WarningHdr.endWarningHdr(flatBufferBuilder);
        RequestRejectImSession.startRequestRejectImSession(flatBufferBuilder);
        RequestRejectImSession.addSessionHandle(flatBufferBuilder, (long) intValue);
        RequestRejectImSession.addSipCode(flatBufferBuilder, (long) rejectImSessionParams.mSessionRejectReason.getSipCode());
        RequestRejectImSession.addWarningHdr(flatBufferBuilder, endWarningHdr);
        int endRequestRejectImSession = RequestRejectImSession.endRequestRejectImSession(flatBufferBuilder);
        Request.startRequest(flatBufferBuilder);
        Request.addReqid(flatBufferBuilder, Id.REQUEST_REJECT_IM_SESSION);
        Request.addReqType(flatBufferBuilder, (byte) 51);
        Request.addReq(flatBufferBuilder, endRequestRejectImSession);
        sendRequestToStack(Id.REQUEST_REJECT_IM_SESSION, flatBufferBuilder, Request.endRequest(flatBufferBuilder), this.mStackResponseHandler.obtainMessage(17), userAgent);
    }

    private void handleCloseImSessionRequest(StopImSessionParams stopImSessionParams) {
        String str = this.LOG_TAG;
        Log.i(str, "handleCloseImSessionRequest(): " + stopImSessionParams);
        ImSession imSession = this.mSessions.get(stopImSessionParams.mRawHandle);
        if (imSession == null) {
            Log.e(this.LOG_TAG, "handleCloseImSessionRequest(): unknown session!");
            Message message = stopImSessionParams.mCallback;
            if (message != null) {
                sendCallback(message, new StopImSessionResult(stopImSessionParams.mRawHandle, ImError.ENGINE_ERROR));
                stopImSessionParams.mCallback = null;
                return;
            }
            return;
        }
        imSession.mStopParams = stopImSessionParams;
        sendImCancelRequestToStack(imSession);
    }

    private void onSubscribeGroupChatList(GroupChatListParams groupChatListParams) {
        Log.i(this.LOG_TAG, "onSubscribeGroupChatList()");
        UserAgent userAgent = getUserAgent(groupChatListParams.getOwnImsi());
        if (userAgent == null) {
            Log.e(this.LOG_TAG, "onSubscribeGroupChatList(): UserAgent not found.");
            return;
        }
        FlatBufferBuilder flatBufferBuilder = new FlatBufferBuilder(0);
        int createString = flatBufferBuilder.createString((CharSequence) "SubscribeGroupChatList");
        RequestGroupListSubscribe.startRequestGroupListSubscribe(flatBufferBuilder);
        RequestGroupListSubscribe.addHandle(flatBufferBuilder, (long) userAgent.getHandle());
        RequestGroupListSubscribe.addSubscriptionId(flatBufferBuilder, createString);
        RequestGroupListSubscribe.addVersion(flatBufferBuilder, (long) groupChatListParams.getVersion());
        RequestGroupListSubscribe.addIsIncrease(flatBufferBuilder, groupChatListParams.getIncreaseMode());
        int endRequestGroupListSubscribe = RequestGroupListSubscribe.endRequestGroupListSubscribe(flatBufferBuilder);
        Request.startRequest(flatBufferBuilder);
        Request.addReqid(flatBufferBuilder, Id.REQUEST_GROUP_LIST_SUBSCRIBE);
        Request.addReqType(flatBufferBuilder, (byte) 55);
        Request.addReq(flatBufferBuilder, endRequestGroupListSubscribe);
        sendRequestToStack(Id.REQUEST_GROUP_LIST_SUBSCRIBE, flatBufferBuilder, Request.endRequest(flatBufferBuilder), this.mStackResponseHandler.obtainMessage(24), userAgent);
    }

    private void onSubscribeGroupChatInfo(GroupChatInfoParams groupChatInfoParams) {
        String str = this.LOG_TAG;
        Log.i(str, "onSubscribeGroupChatInfo() uri:" + groupChatInfoParams.getUri());
        UserAgent userAgent = getUserAgent(groupChatInfoParams.getOwnImsi());
        if (userAgent == null) {
            Log.e(this.LOG_TAG, "onSubscribeGroupChatList(): UserAgent not found.");
            return;
        }
        FlatBufferBuilder flatBufferBuilder = new FlatBufferBuilder(0);
        int createString = flatBufferBuilder.createString((CharSequence) "SubscribeGroupChatInfo" + groupChatInfoParams.getUri().toString());
        int createString2 = flatBufferBuilder.createString((CharSequence) parseStr(groupChatInfoParams.getUri().toString()));
        RequestGroupInfoSubscribe.startRequestGroupInfoSubscribe(flatBufferBuilder);
        RequestGroupInfoSubscribe.addHandle(flatBufferBuilder, (long) userAgent.getHandle());
        RequestGroupInfoSubscribe.addSubscriptionId(flatBufferBuilder, createString);
        RequestGroupInfoSubscribe.addUri(flatBufferBuilder, createString2);
        int endRequestGroupInfoSubscribe = RequestGroupInfoSubscribe.endRequestGroupInfoSubscribe(flatBufferBuilder);
        Request.startRequest(flatBufferBuilder);
        Request.addReqid(flatBufferBuilder, Id.REQUEST_GROUP_INFO_SUBSCRIBE);
        Request.addReqType(flatBufferBuilder, (byte) 56);
        Request.addReq(flatBufferBuilder, endRequestGroupInfoSubscribe);
        sendRequestToStack(Id.REQUEST_GROUP_INFO_SUBSCRIBE, flatBufferBuilder, Request.endRequest(flatBufferBuilder), this.mStackResponseHandler.obtainMessage(25, (Object) null), userAgent);
    }

    private void sendImCancelRequestToStack(ImSession imSession) {
        StopImSessionParams stopImSessionParams = imSession.mStopParams;
        if (stopImSessionParams == null) {
            Log.e(this.LOG_TAG, "sendImCancelRequestToStack(): null stop params!");
            return;
        }
        UserAgent userAgent = getUserAgent(imSession.mUaHandle);
        if (userAgent == null) {
            String str = this.LOG_TAG;
            Log.e(str, "sendImCancelRequestToStack: UserAgent not found. UaHandle = " + imSession.mUaHandle);
            Message message = stopImSessionParams.mCallback;
            if (message != null) {
                sendCallback(message, new StopImSessionResult(stopImSessionParams.mRawHandle, ImError.ENGINE_ERROR));
                stopImSessionParams.mCallback = null;
                return;
            }
            return;
        }
        FlatBufferBuilder flatBufferBuilder = new FlatBufferBuilder(0);
        ImSessionStopReason imSessionStopReason = stopImSessionParams.mSessionStopReason;
        int createString = imSessionStopReason != null ? flatBufferBuilder.createString((CharSequence) parseStr(imSessionStopReason.getReasonText())) : -1;
        ReasonHdr.startReasonHdr(flatBufferBuilder);
        ImSessionStopReason imSessionStopReason2 = stopImSessionParams.mSessionStopReason;
        if (imSessionStopReason2 != null) {
            ReasonHdr.addCode(flatBufferBuilder, (long) imSessionStopReason2.getCauseCode());
            ReasonHdr.addText(flatBufferBuilder, createString);
        }
        int endReasonHdr = ReasonHdr.endReasonHdr(flatBufferBuilder);
        RequestCloseImSession.startRequestCloseImSession(flatBufferBuilder);
        RequestCloseImSession.addSessionId(flatBufferBuilder, (long) imSession.mSessionHandle.intValue());
        RequestCloseImSession.addReasonHdr(flatBufferBuilder, endReasonHdr);
        int endRequestCloseImSession = RequestCloseImSession.endRequestCloseImSession(flatBufferBuilder);
        Request.startRequest(flatBufferBuilder);
        Request.addReqid(flatBufferBuilder, 503);
        Request.addReqType(flatBufferBuilder, (byte) 41);
        Request.addReq(flatBufferBuilder, endRequestCloseImSession);
        sendRequestToStack(503, flatBufferBuilder, Request.endRequest(flatBufferBuilder), this.mStackResponseHandler.obtainMessage(3, stopImSessionParams.mCallback), userAgent);
    }

    private void handleSendComposingNotification(ImSendComposingParams imSendComposingParams) {
        String str = this.LOG_TAG;
        Log.i(str, "handleSendComposingNotification(): " + imSendComposingParams);
        ImSession imSession = this.mSessions.get(imSendComposingParams.mRawHandle);
        if (imSession == null) {
            Log.e(this.LOG_TAG, "handleSendComposingNotification(): invalid session handle!");
            return;
        }
        UserAgent userAgent = getUserAgent(imSession.mUaHandle);
        if (userAgent == null) {
            Log.e(this.LOG_TAG, "handleSendComposingNotification(): user agent not found");
            return;
        }
        FlatBufferBuilder flatBufferBuilder = new FlatBufferBuilder(0);
        int createString = flatBufferBuilder.createString((CharSequence) MIMEContentType.PLAIN_TEXT);
        int createString2 = flatBufferBuilder.createString((CharSequence) parseStr(imSendComposingParams.mUserAlias));
        ImComposingStatus.startImComposingStatus(flatBufferBuilder);
        ImComposingStatus.addContentType(flatBufferBuilder, createString);
        ImComposingStatus.addInterval(flatBufferBuilder, (long) imSendComposingParams.mInterval);
        ImComposingStatus.addIsActive(flatBufferBuilder, imSendComposingParams.mIsComposing);
        int endImComposingStatus = ImComposingStatus.endImComposingStatus(flatBufferBuilder);
        RequestSendImComposingStatus.startRequestSendImComposingStatus(flatBufferBuilder);
        RequestSendImComposingStatus.addSessionId(flatBufferBuilder, (long) imSession.mSessionHandle.intValue());
        RequestSendImComposingStatus.addStatus(flatBufferBuilder, endImComposingStatus);
        RequestSendImComposingStatus.addUserAlias(flatBufferBuilder, createString2);
        int endRequestSendImComposingStatus = RequestSendImComposingStatus.endRequestSendImComposingStatus(flatBufferBuilder);
        Request.startRequest(flatBufferBuilder);
        Request.addReqid(flatBufferBuilder, Id.REQUEST_IM_SEND_COMPOSING_STATUS);
        Request.addReqType(flatBufferBuilder, (byte) 44);
        Request.addReq(flatBufferBuilder, endRequestSendImComposingStatus);
        sendRequestToStack(Id.REQUEST_IM_SEND_COMPOSING_STATUS, flatBufferBuilder, Request.endRequest(flatBufferBuilder), this.mStackResponseHandler.obtainMessage(9), userAgent);
    }

    private void handleSendMessageRequest(SendMessageParams sendMessageParams) {
        int i;
        IMSLog.s(this.LOG_TAG, "handleSendMessageRequest(): " + sendMessageParams);
        ImSession imSession = this.mSessions.get(sendMessageParams.mRawHandle);
        if (imSession == null) {
            Log.e(this.LOG_TAG, "handleSendMessageRequest(): invalid session handle!");
            Message message = sendMessageParams.mCallback;
            if (message != null) {
                sendCallback(message, new SendMessageResult(sendMessageParams.mRawHandle, new Result(ImError.TRANSACTION_DOESNT_EXIST, Result.Type.ENGINE_ERROR)));
                return;
            }
            return;
        }
        imSession.mSendMessageCallbacks.put(sendMessageParams.mImdnMessageId, sendMessageParams.mCallback);
        UserAgent userAgent = getUserAgent(imSession.mUaHandle);
        if (userAgent == null) {
            Log.e(this.LOG_TAG, "handleSendMessageRequest(): user agent not found");
            Message message2 = sendMessageParams.mCallback;
            if (message2 != null) {
                sendCallback(message2, new SendMessageResult(sendMessageParams.mRawHandle, new Result(ImError.ENGINE_ERROR, Result.Type.ENGINE_ERROR)));
                sendMessageParams.mCallback = null;
                return;
            }
            return;
        }
        if (!sendMessageParams.mContentType.toLowerCase(Locale.US).contains("charset=")) {
            Log.e(this.LOG_TAG, "handleSendMessageRequest(): missed charset, use utf8!");
            sendMessageParams.mContentType += ";charset=UTF-8";
        }
        int[] translateFwImdnNoti = ResipTranslatorCollection.translateFwImdnNoti(sendMessageParams.mDispositionNotification);
        FlatBufferBuilder flatBufferBuilder = new FlatBufferBuilder(0);
        int createNotiVector = ImdnParams.createNotiVector(flatBufferBuilder, translateFwImdnNoti);
        int createString = flatBufferBuilder.createString((CharSequence) parseStr(sendMessageParams.mImdnMessageId));
        Date date = sendMessageParams.mImdnTime;
        int createString2 = flatBufferBuilder.createString((CharSequence) date != null ? Iso8601.formatMillis(date) : "");
        ImdnParams.startImdnParams(flatBufferBuilder);
        ImdnParams.addMessageId(flatBufferBuilder, createString);
        ImdnParams.addDatetime(flatBufferBuilder, createString2);
        ImdnParams.addNoti(flatBufferBuilder, createNotiVector);
        int endImdnParams = ImdnParams.endImdnParams(flatBufferBuilder);
        ArrayList arrayList = new ArrayList();
        if (sendMessageParams.mMaapTrafficType != null) {
            int createString3 = flatBufferBuilder.createString((CharSequence) MaapNamespace.NAME);
            int createString4 = flatBufferBuilder.createString((CharSequence) MaapNamespace.URI);
            int createString5 = flatBufferBuilder.createString((CharSequence) "Traffic-Type");
            int createString6 = flatBufferBuilder.createString((CharSequence) sendMessageParams.mMaapTrafficType);
            Pair.startPair(flatBufferBuilder);
            Pair.addKey(flatBufferBuilder, createString5);
            Pair.addValue(flatBufferBuilder, createString6);
            int createHeadersVector = CpimNamespace.createHeadersVector(flatBufferBuilder, new int[]{Pair.endPair(flatBufferBuilder)});
            CpimNamespace.startCpimNamespace(flatBufferBuilder);
            CpimNamespace.addName(flatBufferBuilder, createString3);
            CpimNamespace.addUri(flatBufferBuilder, createString4);
            CpimNamespace.addHeaders(flatBufferBuilder, createHeadersVector);
            arrayList.add(Integer.valueOf(CpimNamespace.endCpimNamespace(flatBufferBuilder)));
        }
        if (!(sendMessageParams.mReferenceId == null && sendMessageParams.mReferenceType == null && sendMessageParams.mReferenceValue == null)) {
            int createString7 = flatBufferBuilder.createString((CharSequence) RcsNamespace.KOR.NAME);
            int createString8 = flatBufferBuilder.createString((CharSequence) RcsNamespace.KOR.URI);
            int[] iArr = new int[0];
            if (sendMessageParams.mReferenceId != null) {
                int createString9 = flatBufferBuilder.createString((CharSequence) RcsNamespace.REFERENCE_ID_KEY);
                int createString10 = flatBufferBuilder.createString((CharSequence) sendMessageParams.mReferenceId);
                Pair.startPair(flatBufferBuilder);
                Pair.addKey(flatBufferBuilder, createString9);
                Pair.addValue(flatBufferBuilder, createString10);
                int endPair = Pair.endPair(flatBufferBuilder);
                iArr = Arrays.copyOf(iArr, 1);
                iArr[iArr.length - 1] = endPair;
            }
            if (sendMessageParams.mReferenceType != null) {
                int createString11 = flatBufferBuilder.createString((CharSequence) RcsNamespace.REFERENCE_TYPE_KEY);
                int createString12 = flatBufferBuilder.createString((CharSequence) sendMessageParams.mReferenceType);
                Pair.startPair(flatBufferBuilder);
                Pair.addKey(flatBufferBuilder, createString11);
                Pair.addValue(flatBufferBuilder, createString12);
                int endPair2 = Pair.endPair(flatBufferBuilder);
                iArr = Arrays.copyOf(iArr, iArr.length + 1);
                iArr[iArr.length - 1] = endPair2;
            }
            if (sendMessageParams.mReferenceValue != null) {
                int createString13 = flatBufferBuilder.createString((CharSequence) RcsNamespace.REFERENCE_VALUE_KEY);
                int createString14 = flatBufferBuilder.createString((CharSequence) sendMessageParams.mReferenceValue);
                Pair.startPair(flatBufferBuilder);
                Pair.addKey(flatBufferBuilder, createString13);
                Pair.addValue(flatBufferBuilder, createString14);
                int endPair3 = Pair.endPair(flatBufferBuilder);
                iArr = Arrays.copyOf(iArr, iArr.length + 1);
                iArr[iArr.length - 1] = endPair3;
            }
            int createHeadersVector2 = CpimNamespace.createHeadersVector(flatBufferBuilder, iArr);
            CpimNamespace.startCpimNamespace(flatBufferBuilder);
            CpimNamespace.addName(flatBufferBuilder, createString7);
            CpimNamespace.addUri(flatBufferBuilder, createString8);
            CpimNamespace.addHeaders(flatBufferBuilder, createHeadersVector2);
            arrayList.add(Integer.valueOf(CpimNamespace.endCpimNamespace(flatBufferBuilder)));
        }
        int i2 = -1;
        if (arrayList.size() > 0) {
            int size = arrayList.size();
            int[] iArr2 = new int[size];
            for (int i3 = 0; i3 < size; i3++) {
                iArr2[i3] = ((Integer) arrayList.get(i3)).intValue();
            }
            i = ImMessageParam.createCpimNamespacesVector(flatBufferBuilder, iArr2);
        } else {
            i = -1;
        }
        if (sendMessageParams.mGroupCcList != null) {
            Log.i(this.LOG_TAG, "handleSendMessageRequest, params.mGroupCcList=" + sendMessageParams.mGroupCcList);
            Set<ImsUri> set = sendMessageParams.mGroupCcList;
            i2 = ImMessageParam.createCcParticipantsVector(flatBufferBuilder, getImsUriOffsetArray(flatBufferBuilder, set, set.size()));
        }
        int createString15 = flatBufferBuilder.createString((CharSequence) parseStr(sendMessageParams.mBody));
        int createString16 = flatBufferBuilder.createString((CharSequence) parseStr(sendMessageParams.mContentType));
        int createString17 = flatBufferBuilder.createString((CharSequence) parseStr(sendMessageParams.mUserAlias));
        ImMessageParam.startImMessageParam(flatBufferBuilder);
        ImMessageParam.addBody(flatBufferBuilder, createString15);
        ImMessageParam.addUserAlias(flatBufferBuilder, createString17);
        ImMessageParam.addContentType(flatBufferBuilder, createString16);
        ImMessageParam.addImdn(flatBufferBuilder, endImdnParams);
        if (arrayList.size() > 0) {
            ImMessageParam.addCpimNamespaces(flatBufferBuilder, i);
        }
        if (sendMessageParams.mGroupCcList != null) {
            ImMessageParam.addCcParticipants(flatBufferBuilder, i2);
        }
        int endImMessageParam = ImMessageParam.endImMessageParam(flatBufferBuilder);
        BaseSessionData.startBaseSessionData(flatBufferBuilder);
        BaseSessionData.addSessionHandle(flatBufferBuilder, (long) imSession.mSessionHandle.intValue());
        int endBaseSessionData = BaseSessionData.endBaseSessionData(flatBufferBuilder);
        RequestSendImMessage.startRequestSendImMessage(flatBufferBuilder);
        RequestSendImMessage.addSessionData(flatBufferBuilder, endBaseSessionData);
        RequestSendImMessage.addMessageParam(flatBufferBuilder, endImMessageParam);
        int endRequestSendImMessage = RequestSendImMessage.endRequestSendImMessage(flatBufferBuilder);
        Request.startRequest(flatBufferBuilder);
        Request.addReqid(flatBufferBuilder, Id.REQUEST_IM_SENDMSG);
        Request.addReqType(flatBufferBuilder, (byte) 43);
        Request.addReq(flatBufferBuilder, endRequestSendImMessage);
        sendRequestToStack(Id.REQUEST_IM_SENDMSG, flatBufferBuilder, Request.endRequest(flatBufferBuilder), this.mStackResponseHandler.obtainMessage(4), userAgent);
    }

    private void handleSendDispositionNotification(SendImdnParams sendImdnParams) {
        Object obj = sendImdnParams.mRawHandle;
        ImSession imSession = obj != null ? this.mSessions.get(obj) : null;
        this.mImdnHandler.sendDispositionNotification(sendImdnParams, 1, imSession != null ? imSession.mSessionHandle.intValue() : -1);
    }

    private void handleSendFtDispositionNotification(SendImdnParams sendImdnParams) {
        this.mImdnHandler.sendDispositionNotification(sendImdnParams, 2, -1);
    }

    private void handleSendMessageRevokeRequest(SendMessageRevokeParams sendMessageRevokeParams) {
        String str = this.LOG_TAG;
        IMSLog.s(str, "SendMessageRevokeRequest - " + sendMessageRevokeParams);
        if (sendMessageRevokeParams == null) {
            Log.e(this.LOG_TAG, "params are null, discarding");
        } else if (TextUtils.isEmpty(sendMessageRevokeParams.mOwnImsi)) {
            Log.e(this.LOG_TAG, "mOwnImsi wrong value, discarding");
        } else {
            ImsUri imsUri = sendMessageRevokeParams.mUri;
            if (imsUri == null) {
                Log.e(this.LOG_TAG, "mUri is null, discarding");
                return;
            }
            String imsUri2 = imsUri.toString();
            if (TextUtils.isEmpty(imsUri2)) {
                Log.e(this.LOG_TAG, "uri is empty, discarding");
            } else if (TextUtils.isEmpty(sendMessageRevokeParams.mConversationId)) {
                Log.e(this.LOG_TAG, "mConversationId wrong value, discarding");
            } else if (TextUtils.isEmpty(sendMessageRevokeParams.mContributionId)) {
                Log.e(this.LOG_TAG, "mContributionId wrong value, discarding");
            } else {
                UserAgent userAgent = getUserAgent(sendMessageRevokeParams.mOwnImsi);
                if (userAgent == null) {
                    Log.e(this.LOG_TAG, "sendDispositionNotification(): UserAgent not found.");
                    AsyncResult.forMessage(sendMessageRevokeParams.mCallback, ImError.ENGINE_ERROR, (Throwable) null);
                    sendMessageRevokeParams.mCallback.sendToTarget();
                    return;
                }
                int handle = userAgent.getHandle();
                FlatBufferBuilder flatBufferBuilder = new FlatBufferBuilder(0);
                int createString = flatBufferBuilder.createString((CharSequence) sendMessageRevokeParams.mImdnId);
                int createString2 = flatBufferBuilder.createString((CharSequence) imsUri2);
                int createString3 = flatBufferBuilder.createString((CharSequence) sendMessageRevokeParams.mConversationId);
                int createString4 = flatBufferBuilder.createString((CharSequence) sendMessageRevokeParams.mContributionId);
                RequestSendMessageRevokeRequest.startRequestSendMessageRevokeRequest(flatBufferBuilder);
                RequestSendMessageRevokeRequest.addImdnMessageId(flatBufferBuilder, createString);
                RequestSendMessageRevokeRequest.addRegistrationHandle(flatBufferBuilder, (long) handle);
                RequestSendMessageRevokeRequest.addService(flatBufferBuilder, 1);
                RequestSendMessageRevokeRequest.addUri(flatBufferBuilder, createString2);
                RequestSendMessageRevokeRequest.addConversationId(flatBufferBuilder, createString3);
                RequestSendMessageRevokeRequest.addContributionId(flatBufferBuilder, createString4);
                int endRequestSendMessageRevokeRequest = RequestSendMessageRevokeRequest.endRequestSendMessageRevokeRequest(flatBufferBuilder);
                Request.startRequest(flatBufferBuilder);
                Request.addReqid(flatBufferBuilder, Id.REQUEST_SEND_MSG_REVOKE_REQUEST);
                Request.addReqType(flatBufferBuilder, ReqMsg.request_send_message_revoke_request);
                Request.addReq(flatBufferBuilder, endRequestSendMessageRevokeRequest);
                sendRequestToStack(Id.REQUEST_SEND_MSG_REVOKE_REQUEST, flatBufferBuilder, Request.endRequest(flatBufferBuilder), this.mStackResponseHandler.obtainMessage(28, sendMessageRevokeParams.mCallback), userAgent);
            }
        }
    }

    private void handleAddParticipantsRequest(AddParticipantsParams addParticipantsParams) {
        IMSLog.s(this.LOG_TAG, "handleAddParticipantsRequest: " + addParticipantsParams);
        ImSession imSession = this.mSessions.get(addParticipantsParams.mRawHandle);
        if (imSession == null) {
            Log.e(this.LOG_TAG, "handleAddParticipantsRequest: Session not exist.");
            Message message = addParticipantsParams.mCallback;
            if (message != null) {
                sendCallback(message, ImError.TRANSACTION_DOESNT_EXIST);
                return;
            }
            return;
        }
        Message message2 = addParticipantsParams.mCallback;
        if (message2 != null) {
            message2.obj = addParticipantsParams.mReceivers;
            imSession.mAddParticipantsCallbacks.put(addParticipantsParams.mReqKey, message2);
        }
        UserAgent userAgent = getUserAgent(imSession.mUaHandle);
        if (userAgent == null) {
            Log.e(this.LOG_TAG, "handleAddParticipantsRequest: User agent not found.");
            Message message3 = addParticipantsParams.mCallback;
            if (message3 != null) {
                sendCallback(message3, ImError.ENGINE_ERROR);
                return;
            }
            return;
        }
        FlatBufferBuilder flatBufferBuilder = new FlatBufferBuilder(0);
        int[] iArr = new int[addParticipantsParams.mReceivers.size()];
        Iterator<ImsUri> it = addParticipantsParams.mReceivers.iterator();
        int i = 0;
        while (it.hasNext()) {
            ImsUri next = it.next();
            int i2 = i + 1;
            iArr[i] = flatBufferBuilder.createString((CharSequence) next != null ? next.toString() : "");
            i = i2;
        }
        int createString = flatBufferBuilder.createString((CharSequence) parseStr(addParticipantsParams.mReqKey));
        int createString2 = flatBufferBuilder.createString((CharSequence) parseStr(addParticipantsParams.mSubject));
        int createReceiverVector = RequestUpdateParticipants.createReceiverVector(flatBufferBuilder, iArr);
        RequestUpdateParticipants.startRequestUpdateParticipants(flatBufferBuilder);
        RequestUpdateParticipants.addReceiver(flatBufferBuilder, createReceiverVector);
        RequestUpdateParticipants.addReqKey(flatBufferBuilder, createString);
        RequestUpdateParticipants.addSubject(flatBufferBuilder, createString2);
        RequestUpdateParticipants.addSessionHandle(flatBufferBuilder, (long) imSession.mSessionHandle.intValue());
        RequestUpdateParticipants.addReqType(flatBufferBuilder, 0);
        int endRequestUpdateParticipants = RequestUpdateParticipants.endRequestUpdateParticipants(flatBufferBuilder);
        Request.startRequest(flatBufferBuilder);
        Request.addReqid(flatBufferBuilder, Id.REQUEST_GC_UPDATE_PARTICIPANTS);
        Request.addReqType(flatBufferBuilder, (byte) 54);
        Request.addReq(flatBufferBuilder, endRequestUpdateParticipants);
        sendRequestToStack(Id.REQUEST_GC_UPDATE_PARTICIPANTS, flatBufferBuilder, Request.endRequest(flatBufferBuilder), this.mStackResponseHandler.obtainMessage(12), userAgent);
    }

    private void handleRemoveParticipantsRequest(RemoveParticipantsParams removeParticipantsParams) {
        String str = this.LOG_TAG;
        IMSLog.s(str, "handleRemoveParticipantsRequest: " + removeParticipantsParams);
        ImSession imSession = this.mSessions.get(removeParticipantsParams.mRawHandle);
        if (imSession == null) {
            Log.e(this.LOG_TAG, "handleRemoveParticipantsRequest: Session not exist.");
            Message message = removeParticipantsParams.mCallback;
            if (message != null) {
                sendCallback(message, ImError.TRANSACTION_DOESNT_EXIST);
                return;
            }
            return;
        }
        Message message2 = removeParticipantsParams.mCallback;
        if (message2 != null) {
            message2.obj = removeParticipantsParams.mRemovedParticipants;
            imSession.mRemoveParticipantsCallbacks.put(removeParticipantsParams.mReqKey, message2);
        }
        UserAgent userAgent = getUserAgent(imSession.mUaHandle);
        if (userAgent == null) {
            Log.e(this.LOG_TAG, "handleRemoveParticipantsRequest: User agent not found.");
            Message message3 = removeParticipantsParams.mCallback;
            if (message3 != null) {
                sendCallback(message3, ImError.ENGINE_ERROR);
                return;
            }
            return;
        }
        int i = 0;
        FlatBufferBuilder flatBufferBuilder = new FlatBufferBuilder(0);
        int[] iArr = new int[removeParticipantsParams.mRemovedParticipants.size()];
        Iterator<ImsUri> it = removeParticipantsParams.mRemovedParticipants.iterator();
        while (it.hasNext()) {
            ImsUri next = it.next();
            int i2 = i + 1;
            iArr[i] = flatBufferBuilder.createString((CharSequence) next != null ? next.toString() : "");
            i = i2;
        }
        String str2 = removeParticipantsParams.mReqKey;
        int createString = str2 != null ? flatBufferBuilder.createString((CharSequence) str2) : -1;
        int createReceiverVector = RequestUpdateParticipants.createReceiverVector(flatBufferBuilder, iArr);
        RequestUpdateParticipants.startRequestUpdateParticipants(flatBufferBuilder);
        RequestUpdateParticipants.addReceiver(flatBufferBuilder, createReceiverVector);
        if (createString != -1) {
            RequestUpdateParticipants.addReqKey(flatBufferBuilder, createString);
        }
        RequestUpdateParticipants.addSessionHandle(flatBufferBuilder, (long) imSession.mSessionHandle.intValue());
        RequestUpdateParticipants.addReqType(flatBufferBuilder, 1);
        int endRequestUpdateParticipants = RequestUpdateParticipants.endRequestUpdateParticipants(flatBufferBuilder);
        Request.startRequest(flatBufferBuilder);
        Request.addReqid(flatBufferBuilder, Id.REQUEST_GC_UPDATE_PARTICIPANTS);
        Request.addReqType(flatBufferBuilder, (byte) 54);
        Request.addReq(flatBufferBuilder, endRequestUpdateParticipants);
        sendRequestToStack(Id.REQUEST_GC_UPDATE_PARTICIPANTS, flatBufferBuilder, Request.endRequest(flatBufferBuilder), this.mStackResponseHandler.obtainMessage(21), userAgent);
    }

    private void handleChangeGroupChatLeaderRequest(ChangeGroupChatLeaderParams changeGroupChatLeaderParams) {
        String str = this.LOG_TAG;
        IMSLog.s(str, "handleChangeGroupChatLeaderRequest: " + changeGroupChatLeaderParams);
        ImSession imSession = this.mSessions.get(changeGroupChatLeaderParams.mRawHandle);
        if (imSession == null) {
            Log.e(this.LOG_TAG, "handleChangeGroupChatLeaderRequest: Session not exist.");
            Message message = changeGroupChatLeaderParams.mCallback;
            if (message != null) {
                sendCallback(message, ImError.TRANSACTION_DOESNT_EXIST);
                return;
            }
            return;
        }
        List<ImsUri> list = changeGroupChatLeaderParams.mLeader;
        if (list == null) {
            Log.e(this.LOG_TAG, "handleChangeGroupChatLeaderRequest: Leader info not exist.");
            Message message2 = changeGroupChatLeaderParams.mCallback;
            if (message2 != null) {
                sendCallback(message2, ImError.ENGINE_ERROR);
                return;
            }
            return;
        }
        Message message3 = changeGroupChatLeaderParams.mCallback;
        if (message3 != null) {
            message3.obj = list;
            imSession.mChangeGCLeaderCallbacks.put(changeGroupChatLeaderParams.mReqKey, message3);
        }
        UserAgent userAgent = getUserAgent(imSession.mUaHandle);
        if (userAgent == null) {
            Log.e(this.LOG_TAG, "handleChangeGroupChatLeaderRequest: User agent not found.");
            Message message4 = changeGroupChatLeaderParams.mCallback;
            if (message4 != null) {
                sendCallback(message4, ImError.ENGINE_ERROR);
                return;
            }
            return;
        }
        FlatBufferBuilder flatBufferBuilder = new FlatBufferBuilder(0);
        int[] iArr = new int[changeGroupChatLeaderParams.mLeader.size()];
        Iterator<ImsUri> it = changeGroupChatLeaderParams.mLeader.iterator();
        while (it.hasNext()) {
            ImsUri next = it.next();
            iArr[0] = flatBufferBuilder.createString((CharSequence) next != null ? next.toString() : "");
        }
        String str2 = changeGroupChatLeaderParams.mReqKey;
        int createString = str2 != null ? flatBufferBuilder.createString((CharSequence) str2) : -1;
        int createReceiverVector = RequestUpdateParticipants.createReceiverVector(flatBufferBuilder, iArr);
        RequestUpdateParticipants.startRequestUpdateParticipants(flatBufferBuilder);
        RequestUpdateParticipants.addReceiver(flatBufferBuilder, createReceiverVector);
        if (createString != -1) {
            RequestUpdateParticipants.addReqKey(flatBufferBuilder, createString);
        }
        RequestUpdateParticipants.addSessionHandle(flatBufferBuilder, (long) imSession.mSessionHandle.intValue());
        RequestUpdateParticipants.addReqType(flatBufferBuilder, 2);
        int endRequestUpdateParticipants = RequestUpdateParticipants.endRequestUpdateParticipants(flatBufferBuilder);
        Request.startRequest(flatBufferBuilder);
        Request.addReqid(flatBufferBuilder, Id.REQUEST_GC_UPDATE_PARTICIPANTS);
        Request.addReqType(flatBufferBuilder, (byte) 54);
        Request.addReq(flatBufferBuilder, endRequestUpdateParticipants);
        sendRequestToStack(Id.REQUEST_GC_UPDATE_PARTICIPANTS, flatBufferBuilder, Request.endRequest(flatBufferBuilder), this.mStackResponseHandler.obtainMessage(19), userAgent);
    }

    private void handleChangeGroupChatSubjectRequest(ChangeGroupChatSubjectParams changeGroupChatSubjectParams) {
        String str = this.LOG_TAG;
        IMSLog.s(str, "handleChangeGcSubjectRequest: " + changeGroupChatSubjectParams);
        ImSession imSession = this.mSessions.get(Integer.valueOf(((Integer) changeGroupChatSubjectParams.mRawHandle).intValue()));
        if (imSession == null) {
            Log.e(this.LOG_TAG, "handleChangeGcSubjectRequest: Session not exist.");
            Message message = changeGroupChatSubjectParams.mCallback;
            if (message != null) {
                sendCallback(message, ImError.TRANSACTION_DOESNT_EXIST);
                return;
            }
            return;
        }
        Message message2 = changeGroupChatSubjectParams.mCallback;
        if (message2 != null) {
            message2.obj = changeGroupChatSubjectParams.mSubject;
            imSession.mChangeGCSubjectCallbacks.put(changeGroupChatSubjectParams.mReqKey, message2);
        }
        UserAgent userAgent = getUserAgent(imSession.mUaHandle);
        if (userAgent == null) {
            Log.e(this.LOG_TAG, "handleRemoveParticipantsRequest: User agent not found.");
            Message message3 = changeGroupChatSubjectParams.mCallback;
            if (message3 != null) {
                sendCallback(message3, ImError.ENGINE_ERROR);
                return;
            }
            return;
        }
        FlatBufferBuilder flatBufferBuilder = new FlatBufferBuilder(0);
        int createString = flatBufferBuilder.createString((CharSequence) parseStr(changeGroupChatSubjectParams.mSubject));
        String str2 = changeGroupChatSubjectParams.mReqKey;
        int createString2 = str2 != null ? flatBufferBuilder.createString((CharSequence) str2) : -1;
        RequestUpdateParticipants.startRequestUpdateParticipants(flatBufferBuilder);
        if (createString2 != -1) {
            RequestUpdateParticipants.addReqKey(flatBufferBuilder, createString2);
        }
        RequestUpdateParticipants.addSessionHandle(flatBufferBuilder, (long) imSession.mSessionHandle.intValue());
        RequestUpdateParticipants.addReqType(flatBufferBuilder, 4);
        RequestUpdateParticipants.addSubject(flatBufferBuilder, createString);
        int endRequestUpdateParticipants = RequestUpdateParticipants.endRequestUpdateParticipants(flatBufferBuilder);
        Request.startRequest(flatBufferBuilder);
        Request.addReqid(flatBufferBuilder, Id.REQUEST_GC_UPDATE_PARTICIPANTS);
        Request.addReqType(flatBufferBuilder, (byte) 54);
        Request.addReq(flatBufferBuilder, endRequestUpdateParticipants);
        sendRequestToStack(Id.REQUEST_GC_UPDATE_PARTICIPANTS, flatBufferBuilder, Request.endRequest(flatBufferBuilder), this.mStackResponseHandler.obtainMessage(22), userAgent);
    }

    /* JADX WARNING: Removed duplicated region for block: B:17:0x0073  */
    /* JADX WARNING: Removed duplicated region for block: B:18:0x0078  */
    /* JADX WARNING: Removed duplicated region for block: B:20:0x007b  */
    /* JADX WARNING: Removed duplicated region for block: B:21:0x0080  */
    /* JADX WARNING: Removed duplicated region for block: B:24:0x0086  */
    /* JADX WARNING: Removed duplicated region for block: B:26:0x008b  */
    /* Code decompiled incorrectly, please refer to instructions dump. */
    private void handleChangeGroupChatIconRequest(com.sec.internal.constants.ims.servicemodules.im.params.ChangeGroupChatIconParams r8) {
        /*
            r7 = this;
            java.lang.String r0 = r7.LOG_TAG
            java.lang.StringBuilder r1 = new java.lang.StringBuilder
            r1.<init>()
            java.lang.String r2 = "onChangeGroupChatIcon: "
            r1.append(r2)
            r1.append(r8)
            java.lang.String r1 = r1.toString()
            com.sec.internal.log.IMSLog.s(r0, r1)
            java.lang.Object r0 = r8.mRawHandle
            java.lang.Integer r0 = (java.lang.Integer) r0
            int r0 = r0.intValue()
            java.util.Map<java.lang.Integer, com.sec.internal.ims.core.handler.secims.ResipImHandler$ImSession> r1 = r7.mSessions
            java.lang.Integer r0 = java.lang.Integer.valueOf(r0)
            java.lang.Object r0 = r1.get(r0)
            com.sec.internal.ims.core.handler.secims.ResipImHandler$ImSession r0 = (com.sec.internal.ims.core.handler.secims.ResipImHandler.ImSession) r0
            if (r0 != 0) goto L_0x0034
            java.lang.String r7 = r7.LOG_TAG
            java.lang.String r8 = "onChangeGroupChatIcon: Session does not exist."
            android.util.Log.e(r7, r8)
            return
        L_0x0034:
            android.os.Message r1 = r8.mCallback
            if (r1 == 0) goto L_0x0043
            java.lang.String r2 = r8.mIconPath
            r1.obj = r2
            java.util.Map<java.lang.String, android.os.Message> r2 = r0.mChangeGCIconCallbacks
            java.lang.String r3 = r8.mReqKey
            r2.put(r3, r1)
        L_0x0043:
            com.google.flatbuffers.FlatBufferBuilder r1 = new com.google.flatbuffers.FlatBufferBuilder
            r2 = 0
            r1.<init>(r2)
            java.lang.String r2 = r8.mIconPath
            r3 = -1
            if (r2 == 0) goto L_0x00a1
            java.lang.String r4 = "."
            int r2 = r2.lastIndexOf(r4)
            if (r2 < 0) goto L_0x006d
            java.lang.String r4 = r8.mIconPath
            int r2 = r2 + 1
            int r5 = r4.length()
            java.lang.String r2 = r4.substring(r2, r5)
            boolean r4 = com.sec.internal.helper.translate.ContentTypeTranslator.isTranslationDefined(r2)
            if (r4 == 0) goto L_0x006d
            java.lang.String r2 = com.sec.internal.helper.translate.ContentTypeTranslator.translate(r2)
            goto L_0x006f
        L_0x006d:
            java.lang.String r2 = ""
        L_0x006f:
            java.lang.String r4 = r8.mIconPath
            if (r4 == 0) goto L_0x0078
            int r4 = r1.createString((java.lang.CharSequence) r4)
            goto L_0x0079
        L_0x0078:
            r4 = r3
        L_0x0079:
            if (r2 == 0) goto L_0x0080
            int r2 = r1.createString((java.lang.CharSequence) r2)
            goto L_0x0081
        L_0x0080:
            r2 = r3
        L_0x0081:
            com.sec.internal.ims.core.handler.secims.imsCommonStruc.ImFileAttr.startImFileAttr(r1)
            if (r4 == r3) goto L_0x0089
            com.sec.internal.ims.core.handler.secims.imsCommonStruc.ImFileAttr.addPath(r1, r4)
        L_0x0089:
            if (r2 == r3) goto L_0x008e
            com.sec.internal.ims.core.handler.secims.imsCommonStruc.ImFileAttr.addContentType(r1, r2)
        L_0x008e:
            java.io.File r2 = new java.io.File
            java.lang.String r4 = r8.mIconPath
            r2.<init>(r4)
            long r4 = r2.length()
            com.sec.internal.ims.core.handler.secims.imsCommonStruc.ImFileAttr.addSize(r1, r4)
            int r2 = com.sec.internal.ims.core.handler.secims.imsCommonStruc.ImFileAttr.endImFileAttr(r1)
            goto L_0x00a2
        L_0x00a1:
            r2 = r3
        L_0x00a2:
            java.lang.String r4 = r8.mReqKey
            if (r4 == 0) goto L_0x00ab
            int r4 = r1.createString((java.lang.CharSequence) r4)
            goto L_0x00ac
        L_0x00ab:
            r4 = r3
        L_0x00ac:
            com.sec.internal.ims.core.handler.secims.imsCommonStruc.Request_.RequestUpdateParticipants.startRequestUpdateParticipants(r1)
            java.lang.String r8 = r8.mIconPath
            if (r8 == 0) goto L_0x00b6
            com.sec.internal.ims.core.handler.secims.imsCommonStruc.Request_.RequestUpdateParticipants.addIconAttr(r1, r2)
        L_0x00b6:
            java.lang.Integer r8 = r0.mSessionHandle
            int r8 = r8.intValue()
            long r5 = (long) r8
            com.sec.internal.ims.core.handler.secims.imsCommonStruc.Request_.RequestUpdateParticipants.addSessionHandle(r1, r5)
            r8 = 5
            com.sec.internal.ims.core.handler.secims.imsCommonStruc.Request_.RequestUpdateParticipants.addReqType(r1, r8)
            if (r4 == r3) goto L_0x00c9
            com.sec.internal.ims.core.handler.secims.imsCommonStruc.Request_.RequestUpdateParticipants.addReqKey(r1, r4)
        L_0x00c9:
            int r8 = com.sec.internal.ims.core.handler.secims.imsCommonStruc.Request_.RequestUpdateParticipants.endRequestUpdateParticipants(r1)
            com.sec.internal.ims.core.handler.secims.imsCommonStruc.Request.startRequest(r1)
            r0 = 510(0x1fe, float:7.15E-43)
            com.sec.internal.ims.core.handler.secims.imsCommonStruc.Request.addReqid(r1, r0)
            r2 = 54
            com.sec.internal.ims.core.handler.secims.imsCommonStruc.Request.addReqType(r1, r2)
            com.sec.internal.ims.core.handler.secims.imsCommonStruc.Request.addReq(r1, r8)
            int r8 = com.sec.internal.ims.core.handler.secims.imsCommonStruc.Request.endRequest(r1)
            com.sec.internal.ims.core.handler.secims.ResipImResponseHandler r2 = r7.mStackResponseHandler
            r3 = 30
            android.os.Message r2 = r2.obtainMessage(r3)
            r7.sendRequestToStack(r0, r1, r8, r2)
            return
        */
        throw new UnsupportedOperationException("Method not decompiled: com.sec.internal.ims.core.handler.secims.ResipImHandler.handleChangeGroupChatIconRequest(com.sec.internal.constants.ims.servicemodules.im.params.ChangeGroupChatIconParams):void");
    }

    private void handleChangeGroupChatAliasRequest(ChangeGroupAliasParams changeGroupAliasParams) {
        String str = this.LOG_TAG;
        IMSLog.s(str, "handleChangeGcAliasRequest: " + changeGroupAliasParams);
        ImSession imSession = this.mSessions.get(Integer.valueOf(((Integer) changeGroupAliasParams.mRawHandle).intValue()));
        if (imSession == null) {
            Log.e(this.LOG_TAG, "handleChangeGcAliasRequest: Session not exist.");
            Message message = changeGroupAliasParams.mCallback;
            if (message != null) {
                sendCallback(message, ImError.TRANSACTION_DOESNT_EXIST);
                return;
            }
            return;
        }
        Message message2 = changeGroupAliasParams.mCallback;
        if (message2 != null) {
            message2.obj = changeGroupAliasParams.mAlias;
            imSession.mChangeGCAliasCallbacks.put(changeGroupAliasParams.mReqKey, message2);
        }
        UserAgent userAgent = getUserAgent(imSession.mUaHandle);
        if (userAgent == null) {
            Log.e(this.LOG_TAG, "handleChangeGcAliasRequest: User agent not found.");
            Message message3 = changeGroupAliasParams.mCallback;
            if (message3 != null) {
                sendCallback(message3, ImError.ENGINE_ERROR);
                return;
            }
            return;
        }
        FlatBufferBuilder flatBufferBuilder = new FlatBufferBuilder(0);
        String str2 = changeGroupAliasParams.mReqKey;
        int createString = str2 != null ? flatBufferBuilder.createString((CharSequence) str2) : -1;
        int createString2 = flatBufferBuilder.createString((CharSequence) parseStr(changeGroupAliasParams.mAlias));
        RequestUpdateParticipants.startRequestUpdateParticipants(flatBufferBuilder);
        if (createString != -1) {
            RequestUpdateParticipants.addReqKey(flatBufferBuilder, createString);
        }
        RequestUpdateParticipants.addSessionHandle(flatBufferBuilder, (long) imSession.mSessionHandle.intValue());
        RequestUpdateParticipants.addReqType(flatBufferBuilder, 3);
        RequestUpdateParticipants.addUserAlias(flatBufferBuilder, createString2);
        int endRequestUpdateParticipants = RequestUpdateParticipants.endRequestUpdateParticipants(flatBufferBuilder);
        Request.startRequest(flatBufferBuilder);
        Request.addReqid(flatBufferBuilder, Id.REQUEST_GC_UPDATE_PARTICIPANTS);
        Request.addReqType(flatBufferBuilder, (byte) 54);
        Request.addReq(flatBufferBuilder, endRequestUpdateParticipants);
        sendRequestToStack(Id.REQUEST_GC_UPDATE_PARTICIPANTS, flatBufferBuilder, Request.endRequest(flatBufferBuilder), this.mStackResponseHandler.obtainMessage(23), userAgent);
    }

    private void handleStartFtSessionRequest(SendFtSessionParams sendFtSessionParams) {
        UserAgent userAgent;
        String str;
        int i;
        int i2;
        int i3;
        int i4;
        int i5;
        int i6;
        String str2;
        Iterator<ImsUri> it;
        String str3;
        String str4;
        String str5;
        SendFtSessionParams sendFtSessionParams2 = sendFtSessionParams;
        IMSLog.s(this.LOG_TAG, "handleStartFtSessionRequest: " + sendFtSessionParams2);
        UserAgent userAgent2 = getUserAgent("ft", sendFtSessionParams2.mOwnImsi);
        if (userAgent2 == null) {
            Log.e(this.LOG_TAG, "handleStartFtSessionRequest(): UserAgent not found.");
            FtResult ftResult = new FtResult(ImError.ENGINE_ERROR, Result.Type.ENGINE_ERROR, (Object) null);
            Message message = sendFtSessionParams2.mCallback;
            if (message != null) {
                sendCallback(message, ftResult);
                return;
            }
            return;
        }
        FtSession ftSession = new FtSession();
        ftSession.mId = sendFtSessionParams2.mMessageId;
        ftSession.mStartCallback = sendFtSessionParams2.mCallback;
        ftSession.mStartSessionHandleCallback = sendFtSessionParams2.mSessionHandleCallback;
        ftSession.mUaHandle = userAgent2.getHandle();
        this.mPendingFtSessions.put(sendFtSessionParams2.mFileTransferID, ftSession);
        FlatBufferBuilder flatBufferBuilder = new FlatBufferBuilder(0);
        int createString = flatBufferBuilder.createString((CharSequence) parseStr(sendFtSessionParams2.mFileTransferID));
        int createString2 = flatBufferBuilder.createString((CharSequence) parseStr(sendFtSessionParams2.mUserAlias));
        int createString3 = flatBufferBuilder.createString((CharSequence) parseStr(sendFtSessionParams2.mContributionId));
        int createString4 = flatBufferBuilder.createString((CharSequence) parseStr(sendFtSessionParams2.mConversationId));
        int createString5 = flatBufferBuilder.createString((CharSequence) parseStr(sendFtSessionParams2.mInReplyToContributionId));
        int createString6 = flatBufferBuilder.createString((CharSequence) parseStr(sendFtSessionParams2.mFileName));
        int createString7 = flatBufferBuilder.createString((CharSequence) parseStr(adjustFilePath(sendFtSessionParams2.mFilePath)));
        int createString8 = flatBufferBuilder.createString((CharSequence) parseStr(sendFtSessionParams2.mContentType));
        int createString9 = flatBufferBuilder.createString((CharSequence) parseStr(sendFtSessionParams2.mFileFingerPrint));
        SendReportMsgParams sendReportMsgParams = sendFtSessionParams2.mReportMsgParams;
        if (sendReportMsgParams != null) {
            if (sendReportMsgParams.getSpamFrom() != null) {
                str4 = sendFtSessionParams2.mReportMsgParams.getSpamFrom().toString();
            } else {
                str4 = "";
            }
            int createString10 = flatBufferBuilder.createString((CharSequence) str4);
            if (sendFtSessionParams2.mReportMsgParams.getSpamTo() != null) {
                str5 = sendFtSessionParams2.mReportMsgParams.getSpamTo().toString();
            } else {
                str5 = "";
            }
            int createString11 = flatBufferBuilder.createString((CharSequence) str5);
            i3 = flatBufferBuilder.createString((CharSequence) parseStr(sendFtSessionParams2.mReportMsgParams.getSpamDate()));
            userAgent = userAgent2;
            i = createString10;
            i2 = createString11;
            str = "";
        } else {
            userAgent = userAgent2;
            int createString12 = flatBufferBuilder.createString((CharSequence) "");
            i2 = flatBufferBuilder.createString((CharSequence) "");
            i3 = flatBufferBuilder.createString((CharSequence) "");
            str = "";
            i = createString12;
        }
        int createString13 = flatBufferBuilder.createString((CharSequence) parseStr(sendFtSessionParams2.mImdnId));
        int i7 = createString9;
        int createNotiVector = ImdnParams.createNotiVector(flatBufferBuilder, ResipTranslatorCollection.translateFwImdnNoti(sendFtSessionParams2.mDispositionNotification));
        int createString14 = flatBufferBuilder.createString((CharSequence) Iso8601.formatMillis(sendFtSessionParams2.mImdnTime));
        ImsUri imsUri = sendFtSessionParams2.mConfUri;
        int i8 = -1;
        if (imsUri != null) {
            i5 = flatBufferBuilder.createString((CharSequence) imsUri.toString());
            i4 = createString13;
            i6 = -1;
        } else {
            int[] iArr = new int[sendFtSessionParams2.mRecipients.size()];
            i4 = createString13;
            Iterator<ImsUri> it2 = sendFtSessionParams2.mRecipients.iterator();
            int i9 = 0;
            while (it2.hasNext()) {
                ImsUri next = it2.next();
                int i10 = i9 + 1;
                if (next != null) {
                    String imsUri2 = next.toString();
                    it = it2;
                    str3 = imsUri2;
                } else {
                    it = it2;
                    str3 = str;
                }
                iArr[i9] = flatBufferBuilder.createString((CharSequence) str3);
                it2 = it;
                i9 = i10;
            }
            i6 = BaseSessionData.createReceiversVector(flatBufferBuilder, iArr);
            i5 = -1;
        }
        BaseSessionData.startBaseSessionData(flatBufferBuilder);
        BaseSessionData.addId(flatBufferBuilder, createString);
        BaseSessionData.addIsConference(flatBufferBuilder, sendFtSessionParams2.mConfUri != null);
        if (sendFtSessionParams2.mConfUri != null) {
            BaseSessionData.addSessionUri(flatBufferBuilder, i5);
        } else {
            BaseSessionData.addReceivers(flatBufferBuilder, i6);
        }
        if (sendFtSessionParams2.mUserAlias != null) {
            BaseSessionData.addUserAlias(flatBufferBuilder, createString2);
        }
        if (sendFtSessionParams2.mContributionId != null) {
            BaseSessionData.addContributionId(flatBufferBuilder, createString3);
        }
        if (sendFtSessionParams2.mConversationId != null) {
            BaseSessionData.addConversationId(flatBufferBuilder, createString4);
        }
        if (sendFtSessionParams2.mInReplyToContributionId != null) {
            BaseSessionData.addInReplyToContributionId(flatBufferBuilder, createString5);
        }
        int endBaseSessionData = BaseSessionData.endBaseSessionData(flatBufferBuilder);
        ImFileAttr.startImFileAttr(flatBufferBuilder);
        ImFileAttr.addName(flatBufferBuilder, createString6);
        ImFileAttr.addPath(flatBufferBuilder, createString7);
        ImFileAttr.addContentType(flatBufferBuilder, createString8);
        ImFileAttr.addSize(flatBufferBuilder, (long) ((int) sendFtSessionParams2.mFileSize));
        if (sendFtSessionParams2.mIsResuming) {
            ImFileAttr.addStart(flatBufferBuilder, Math.min(sendFtSessionParams2.mTransferredBytes + 1, sendFtSessionParams2.mFileSize));
            ImFileAttr.addEnd(flatBufferBuilder, sendFtSessionParams2.mFileSize);
        } else {
            ImFileAttr.addStart(flatBufferBuilder, 0);
            ImFileAttr.addEnd(flatBufferBuilder, 0);
        }
        ImFileAttr.addTimeDuration(flatBufferBuilder, (long) sendFtSessionParams2.mTimeDuration);
        int endImFileAttr = ImFileAttr.endImFileAttr(flatBufferBuilder);
        String str6 = sendFtSessionParams2.mThumbPath;
        if (str6 != null && sendFtSessionParams2.mDirection == ImDirection.OUTGOING) {
            int lastIndexOf = str6.lastIndexOf(".");
            if (lastIndexOf >= 0) {
                String str7 = sendFtSessionParams2.mThumbPath;
                String substring = str7.substring(lastIndexOf + 1, str7.length());
                if (ContentTypeTranslator.isTranslationDefined(substring)) {
                    str2 = ContentTypeTranslator.translate(substring);
                    int createString15 = flatBufferBuilder.createString((CharSequence) parseStr(adjustFilePath(sendFtSessionParams2.mThumbPath)));
                    int createString16 = flatBufferBuilder.createString((CharSequence) parseStr(str2));
                    ImFileAttr.startImFileAttr(flatBufferBuilder);
                    ImFileAttr.addPath(flatBufferBuilder, createString15);
                    ImFileAttr.addContentType(flatBufferBuilder, createString16);
                    ImFileAttr.addSize(flatBufferBuilder, new File(sendFtSessionParams2.mThumbPath).length());
                    i8 = ImFileAttr.endImFileAttr(flatBufferBuilder);
                }
            }
            str2 = str;
            int createString152 = flatBufferBuilder.createString((CharSequence) parseStr(adjustFilePath(sendFtSessionParams2.mThumbPath)));
            int createString162 = flatBufferBuilder.createString((CharSequence) parseStr(str2));
            ImFileAttr.startImFileAttr(flatBufferBuilder);
            ImFileAttr.addPath(flatBufferBuilder, createString152);
            ImFileAttr.addContentType(flatBufferBuilder, createString162);
            ImFileAttr.addSize(flatBufferBuilder, new File(sendFtSessionParams2.mThumbPath).length());
            i8 = ImFileAttr.endImFileAttr(flatBufferBuilder);
        }
        int i11 = i8;
        ReportMessageHdr.startReportMessageHdr(flatBufferBuilder);
        ReportMessageHdr.addSpamFrom(flatBufferBuilder, i);
        ReportMessageHdr.addSpamTo(flatBufferBuilder, i2);
        ReportMessageHdr.addSpamDate(flatBufferBuilder, i3);
        int endReportMessageHdr = ReportMessageHdr.endReportMessageHdr(flatBufferBuilder);
        if (sendFtSessionParams2.mReportMsgParams != null) {
            Log.i(this.LOG_TAG, "andleStartFtSessionRequest, mReportMsgParams=" + sendFtSessionParams2.mReportMsgParams);
        }
        ImdnParams.startImdnParams(flatBufferBuilder);
        if (sendFtSessionParams2.mImdnId != null) {
            ImdnParams.addMessageId(flatBufferBuilder, i4);
        }
        ImdnParams.addNoti(flatBufferBuilder, createNotiVector);
        ImdnParams.addDatetime(flatBufferBuilder, createString14);
        int endImdnParams = ImdnParams.endImdnParams(flatBufferBuilder);
        FtPayloadParam.startFtPayloadParam(flatBufferBuilder);
        ImDirection imDirection = sendFtSessionParams2.mDirection;
        ImDirection imDirection2 = ImDirection.OUTGOING;
        FtPayloadParam.addIsPush(flatBufferBuilder, imDirection == imDirection2);
        FtPayloadParam.addIsPublicAccountMsg(flatBufferBuilder, sendFtSessionParams2.mIsPublicAccountMsg);
        FtPayloadParam.addFileFingerPrint(flatBufferBuilder, i7);
        FtPayloadParam.addFileAttr(flatBufferBuilder, endImFileAttr);
        if (sendFtSessionParams2.mThumbPath != null && sendFtSessionParams2.mDirection == imDirection2) {
            FtPayloadParam.addIconAttr(flatBufferBuilder, i11);
        }
        FtPayloadParam.addImdn(flatBufferBuilder, endImdnParams);
        int endFtPayloadParam = FtPayloadParam.endFtPayloadParam(flatBufferBuilder);
        RequestStartFtSession.startRequestStartFtSession(flatBufferBuilder);
        RequestStartFtSession.addRegistrationHandle(flatBufferBuilder, (long) userAgent.getHandle());
        RequestStartFtSession.addSessionData(flatBufferBuilder, endBaseSessionData);
        RequestStartFtSession.addReportData(flatBufferBuilder, endReportMessageHdr);
        RequestStartFtSession.addPayload(flatBufferBuilder, endFtPayloadParam);
        int endRequestStartFtSession = RequestStartFtSession.endRequestStartFtSession(flatBufferBuilder);
        Request.startRequest(flatBufferBuilder);
        Request.addReqid(flatBufferBuilder, Id.REQUEST_FT_START_SESSION);
        Request.addReqType(flatBufferBuilder, (byte) 46);
        Request.addReq(flatBufferBuilder, endRequestStartFtSession);
        sendRequestToStack(Id.REQUEST_FT_START_SESSION, flatBufferBuilder, Request.endRequest(flatBufferBuilder), this.mStackResponseHandler.obtainMessage(8), userAgent);
    }

    private void handleStartFtMediaRequest(int i) {
        String str = this.LOG_TAG;
        IMSLog.s(str, "handleStartFtMediaRequest(): file transdfer session handle = " + i);
        FtSession ftSession = this.mFtSessions.get(Integer.valueOf(i));
        UserAgent userAgent = getUserAgent(ftSession.mUaHandle);
        if (userAgent == null) {
            String str2 = this.LOG_TAG;
            Log.e(str2, "handleStartFtMediaRequest(): UserAgent not found. UaHandle = " + ftSession.mUaHandle);
            FtResult ftResult = new FtResult(ImError.ENGINE_ERROR, Result.Type.ENGINE_ERROR, (Object) null);
            Message message = ftSession.mStartCallback;
            if (message != null) {
                sendCallback(message, ftResult);
                return;
            }
            return;
        }
        FlatBufferBuilder flatBufferBuilder = new FlatBufferBuilder(0);
        RequestStartMedia.startRequestStartMedia(flatBufferBuilder);
        RequestStartMedia.addSessionId(flatBufferBuilder, (long) i);
        int endRequestStartMedia = RequestStartMedia.endRequestStartMedia(flatBufferBuilder);
        Request.startRequest(flatBufferBuilder);
        Request.addReqid(flatBufferBuilder, Id.REQUEST_FT_START_MEDIA);
        Request.addReqType(flatBufferBuilder, (byte) 42);
        Request.addReq(flatBufferBuilder, endRequestStartMedia);
        sendRequestToStack(Id.REQUEST_FT_START_MEDIA, flatBufferBuilder, Request.endRequest(flatBufferBuilder), this.mStackResponseHandler.obtainMessage(18), userAgent);
    }

    private void handleAcceptFtSessionRequest(AcceptFtSessionParams acceptFtSessionParams) {
        String str = this.LOG_TAG;
        IMSLog.s(str, "handleAcceptFtSessionRequest(): " + acceptFtSessionParams);
        Integer num = (Integer) acceptFtSessionParams.mRawHandle;
        FtSession ftSession = this.mFtSessions.get(num);
        if (ftSession == null) {
            String str2 = this.LOG_TAG;
            Log.e(str2, "handleAcceptFtSessionRequest(): no session in map, return accept failure, id = " + num);
            Message message = acceptFtSessionParams.mCallback;
            if (message != null) {
                sendCallback(message, new FtResult(ImError.ENGINE_ERROR, Result.Type.ENGINE_ERROR, (Object) null));
                acceptFtSessionParams.mCallback = null;
                return;
            }
            return;
        }
        ftSession.mAcceptCallback = acceptFtSessionParams.mCallback;
        ftSession.mId = acceptFtSessionParams.mMessageId;
        UserAgent userAgent = getUserAgent(ftSession.mUaHandle);
        if (userAgent == null) {
            Log.e(this.LOG_TAG, "handleAcceptFtSessionRequest(): User agent not found!");
            Message message2 = acceptFtSessionParams.mCallback;
            if (message2 != null) {
                sendCallback(message2, new FtResult(ImError.ENGINE_ERROR, Result.Type.ENGINE_ERROR, (Object) null));
                acceptFtSessionParams.mCallback = null;
                return;
            }
            return;
        }
        FlatBufferBuilder flatBufferBuilder = new FlatBufferBuilder(0);
        int createString = flatBufferBuilder.createString((CharSequence) parseStr(adjustFilePath(acceptFtSessionParams.mFilePath)));
        int createString2 = flatBufferBuilder.createString((CharSequence) parseStr(acceptFtSessionParams.mUserAlias));
        RequestAcceptFtSession.startRequestAcceptFtSession(flatBufferBuilder);
        RequestAcceptFtSession.addSessionHandle(flatBufferBuilder, (long) ftSession.mHandle);
        RequestAcceptFtSession.addStart(flatBufferBuilder, acceptFtSessionParams.mStart);
        RequestAcceptFtSession.addEnd(flatBufferBuilder, acceptFtSessionParams.mEnd);
        RequestAcceptFtSession.addFilePath(flatBufferBuilder, createString);
        RequestAcceptFtSession.addUserAlias(flatBufferBuilder, createString2);
        int endRequestAcceptFtSession = RequestAcceptFtSession.endRequestAcceptFtSession(flatBufferBuilder);
        Request.startRequest(flatBufferBuilder);
        Request.addReqid(flatBufferBuilder, Id.REQUEST_FT_ACCEPT_SESSION);
        Request.addReqType(flatBufferBuilder, (byte) 48);
        Request.addReq(flatBufferBuilder, endRequestAcceptFtSession);
        sendRequestToStack(Id.REQUEST_FT_ACCEPT_SESSION, flatBufferBuilder, Request.endRequest(flatBufferBuilder), this.mStackResponseHandler.obtainMessage(5), userAgent);
    }

    private void handleCancelFtSessionRequest(RejectFtSessionParams rejectFtSessionParams) {
        String str = this.LOG_TAG;
        Log.i(str, "handleCancelFtSessionRequest: " + rejectFtSessionParams);
        FtSession ftSession = this.mPendingFtSessions.get(rejectFtSessionParams.mFileTransferId);
        if (ftSession != null) {
            Log.i(this.LOG_TAG, "handleCancelFtSessionRequest(): pending session - postpone");
            ftSession.mCancelParams = rejectFtSessionParams;
            return;
        }
        Integer num = (Integer) rejectFtSessionParams.mRawHandle;
        FtSession ftSession2 = this.mFtSessions.get(num);
        if (ftSession2 == null) {
            Log.i(this.LOG_TAG, "handleCancelFtSessionRequest(): unknown session!");
            Message message = rejectFtSessionParams.mCallback;
            if (message != null) {
                sendCallback(message, new FtResult(ImError.ENGINE_ERROR, Result.Type.ENGINE_ERROR, (Object) num));
                rejectFtSessionParams.mCallback = null;
            }
        } else if (ftSession2.mCancelParams != null) {
            Log.i(this.LOG_TAG, "handleCancelFtSessionRequest(): there is a ongoing cancel request, ignore further cancel request");
        } else {
            ftSession2.mCancelParams = rejectFtSessionParams;
            sendFtCancelRequestToStack(ftSession2);
        }
    }

    /* access modifiers changed from: protected */
    public void sendFtCancelRequestToStack(FtSession ftSession) {
        RejectFtSessionParams rejectFtSessionParams = ftSession.mCancelParams;
        if (rejectFtSessionParams == null) {
            Log.e(this.LOG_TAG, "sendFtCancelRequestToStack(): null reject params!");
            return;
        }
        UserAgent userAgent = getUserAgent(ftSession.mUaHandle);
        if (userAgent == null) {
            Log.e(this.LOG_TAG, "sendFtCancelRequestToStack(): User agent not found!");
            Message message = ftSession.mCancelParams.mCallback;
            if (message != null) {
                sendCallback(message, new FtResult(ImError.ENGINE_ERROR, Result.Type.ENGINE_ERROR, (Object) null));
                ftSession.mCancelParams.mCallback = null;
                return;
            }
            return;
        }
        FtRejectReason ftRejectReason = rejectFtSessionParams.mRejectReason;
        if (ftRejectReason == null) {
            ftRejectReason = FtRejectReason.DECLINE;
        }
        FlatBufferBuilder flatBufferBuilder = new FlatBufferBuilder(0);
        int createString = flatBufferBuilder.createString((CharSequence) parseStr(ftRejectReason.getWarningText()));
        WarningHdr.startWarningHdr(flatBufferBuilder);
        WarningHdr.addCode(flatBufferBuilder, ftRejectReason.getWarningCode());
        WarningHdr.addText(flatBufferBuilder, createString);
        int endWarningHdr = WarningHdr.endWarningHdr(flatBufferBuilder);
        RequestCancelFtSession.startRequestCancelFtSession(flatBufferBuilder);
        RequestCancelFtSession.addSessionHandle(flatBufferBuilder, (long) ftSession.mHandle);
        RequestCancelFtSession.addSipCode(flatBufferBuilder, ftRejectReason.getSipCode());
        RequestCancelFtSession.addWarningHdr(flatBufferBuilder, endWarningHdr);
        int endRequestCancelFtSession = RequestCancelFtSession.endRequestCancelFtSession(flatBufferBuilder);
        Request.startRequest(flatBufferBuilder);
        Request.addReqid(flatBufferBuilder, Id.REQUEST_FT_CANCEL_SESSION);
        Request.addReqType(flatBufferBuilder, (byte) 47);
        Request.addReq(flatBufferBuilder, endRequestCancelFtSession);
        sendRequestToStack(Id.REQUEST_FT_CANCEL_SESSION, flatBufferBuilder, Request.endRequest(flatBufferBuilder), this.mStackResponseHandler.obtainMessage(6), userAgent);
    }

    /* access modifiers changed from: protected */
    public void handleRejectFtSessionRequest(RejectFtSessionParams rejectFtSessionParams) {
        String str = this.LOG_TAG;
        IMSLog.s(str, "handleRejectFtSessionRequest: " + rejectFtSessionParams);
        Integer num = (Integer) rejectFtSessionParams.mRawHandle;
        FtSession ftSession = this.mFtSessions.get(num);
        if (ftSession == null) {
            String str2 = this.LOG_TAG;
            Log.e(str2, "handleRejectFtSessionRequest: no session in map, return reject failure id=" + num);
            Message message = rejectFtSessionParams.mCallback;
            if (message != null) {
                sendCallback(message, new FtResult(ImError.ENGINE_ERROR, Result.Type.ENGINE_ERROR, (Object) null));
                return;
            }
            return;
        }
        ftSession.mCancelParams = rejectFtSessionParams;
        FtRejectReason ftRejectReason = rejectFtSessionParams.mRejectReason;
        if (ftRejectReason == null) {
            ftRejectReason = FtRejectReason.DECLINE;
        }
        UserAgent userAgent = getUserAgent(ftSession.mUaHandle);
        if (userAgent == null) {
            Log.e(this.LOG_TAG, "handleRejectFtSessionRequest(): User Agent not found!");
            Message message2 = rejectFtSessionParams.mCallback;
            if (message2 != null) {
                sendCallback(message2, new FtResult(ImError.ENGINE_ERROR, Result.Type.ENGINE_ERROR, (Object) null));
                rejectFtSessionParams.mCallback = null;
                return;
            }
            return;
        }
        FlatBufferBuilder flatBufferBuilder = new FlatBufferBuilder(0);
        int createString = flatBufferBuilder.createString((CharSequence) parseStr(ftRejectReason.getWarningText()));
        WarningHdr.startWarningHdr(flatBufferBuilder);
        WarningHdr.addCode(flatBufferBuilder, ftRejectReason.getWarningCode());
        WarningHdr.addText(flatBufferBuilder, createString);
        int endWarningHdr = WarningHdr.endWarningHdr(flatBufferBuilder);
        RequestCancelFtSession.startRequestCancelFtSession(flatBufferBuilder);
        RequestCancelFtSession.addSessionHandle(flatBufferBuilder, (long) ftSession.mHandle);
        RequestCancelFtSession.addSipCode(flatBufferBuilder, ftRejectReason.getSipCode());
        RequestCancelFtSession.addWarningHdr(flatBufferBuilder, endWarningHdr);
        int endRequestCancelFtSession = RequestCancelFtSession.endRequestCancelFtSession(flatBufferBuilder);
        Request.startRequest(flatBufferBuilder);
        Request.addReqid(flatBufferBuilder, Id.REQUEST_FT_CANCEL_SESSION);
        Request.addReqType(flatBufferBuilder, (byte) 47);
        Request.addReq(flatBufferBuilder, endRequestCancelFtSession);
        sendRequestToStack(Id.REQUEST_FT_CANCEL_SESSION, flatBufferBuilder, Request.endRequest(flatBufferBuilder), this.mStackResponseHandler.obtainMessage(7), userAgent);
    }

    private void handleSetMoreInfoToSipUARequest(String str, int i) {
        String str2 = this.LOG_TAG;
        Log.i(str2, "handleSetMoreInfoToSipUARequest: " + str);
        if (!TextUtils.isEmpty(str)) {
            UserAgent userAgent = getUserAgent(i);
            if (userAgent == null) {
                Log.e(this.LOG_TAG, "handleSetMoreInfoToSipUARequest(): User Agent not found!");
                return;
            }
            FlatBufferBuilder flatBufferBuilder = new FlatBufferBuilder(0);
            int createString = flatBufferBuilder.createString((CharSequence) str);
            RequestImSetMoreInfoToSipUA.startRequestImSetMoreInfoToSipUA(flatBufferBuilder);
            RequestImSetMoreInfoToSipUA.addValue(flatBufferBuilder, createString);
            int endRequestImSetMoreInfoToSipUA = RequestImSetMoreInfoToSipUA.endRequestImSetMoreInfoToSipUA(flatBufferBuilder);
            Request.startRequest(flatBufferBuilder);
            Request.addReqid(flatBufferBuilder, Id.REQUEST_IM_SET_MORE_INFO_TO_SIP_UA);
            Request.addReqType(flatBufferBuilder, (byte) 59);
            Request.addReq(flatBufferBuilder, endRequestImSetMoreInfoToSipUA);
            sendRequestToStack(Id.REQUEST_IM_SET_MORE_INFO_TO_SIP_UA, flatBufferBuilder, Request.endRequest(flatBufferBuilder), this.mStackResponseHandler.obtainMessage(29), userAgent);
        }
    }

    private void handleReportChatbotAsSpam(ReportChatbotAsSpamParams reportChatbotAsSpamParams) {
        Log.i(this.LOG_TAG, "handleReportChatbotAsSpam");
        ImsUri imsUri = reportChatbotAsSpamParams.mChatbotUri;
        String str = reportChatbotAsSpamParams.mSpamInfo;
        String str2 = reportChatbotAsSpamParams.mRequestId;
        if (imsUri == null || TextUtils.isEmpty(imsUri.toString())) {
            Log.e(this.LOG_TAG, "handleReportChatbotAsSpam - Invalid ChatBotUrl");
            return;
        }
        UserAgent userAgent = getUserAgent(ServiceConstants.SERVICE_CHATBOT_COMMUNICATION, reportChatbotAsSpamParams.mPhoneId);
        if (userAgent == null) {
            Log.e(this.LOG_TAG, "handleReportChatbotAsSpam(): User Agent not found!");
            return;
        }
        FlatBufferBuilder flatBufferBuilder = new FlatBufferBuilder(0);
        int createString = flatBufferBuilder.createString((CharSequence) imsUri.toString());
        int createString2 = flatBufferBuilder.createString((CharSequence) parseStr(str));
        int createString3 = flatBufferBuilder.createString((CharSequence) parseStr(str2));
        RequestReportChatbotAsSpam.startRequestReportChatbotAsSpam(flatBufferBuilder);
        RequestReportChatbotAsSpam.addRegistrationHandle(flatBufferBuilder, (long) userAgent.getHandle());
        RequestReportChatbotAsSpam.addChatbotUri(flatBufferBuilder, createString);
        RequestReportChatbotAsSpam.addSpamInfo(flatBufferBuilder, createString2);
        RequestReportChatbotAsSpam.addRequestId(flatBufferBuilder, createString3);
        int endRequestReportChatbotAsSpam = RequestReportChatbotAsSpam.endRequestReportChatbotAsSpam(flatBufferBuilder);
        Request.startRequest(flatBufferBuilder);
        Request.addReqid(flatBufferBuilder, 1400);
        Request.addReqType(flatBufferBuilder, (byte) 53);
        Request.addReq(flatBufferBuilder, endRequestReportChatbotAsSpam);
        sendRequestToStack(1400, flatBufferBuilder, Request.endRequest(flatBufferBuilder), this.mStackResponseHandler.obtainMessage(31), userAgent);
    }

    private void handleRequestChatbotAnonymize(ChatbotAnonymizeParams chatbotAnonymizeParams) {
        Log.i(this.LOG_TAG, "handleRequestChatbotAnonymize");
        ImsUri imsUri = chatbotAnonymizeParams.mChatbotUri;
        String str = chatbotAnonymizeParams.mAliasXml;
        String str2 = chatbotAnonymizeParams.mCommandId;
        if (imsUri == null || TextUtils.isEmpty(imsUri.toString())) {
            Log.e(this.LOG_TAG, "handleRequestChatbotAnonymize - Invalid ChatBotUrl");
            return;
        }
        UserAgent userAgent = getUserAgent(ServiceConstants.SERVICE_CHATBOT_COMMUNICATION, chatbotAnonymizeParams.mPhoneId);
        if (userAgent == null) {
            Log.e(this.LOG_TAG, "handleRequestChatbotAnonymize(): User Agent not found!");
            return;
        }
        FlatBufferBuilder flatBufferBuilder = new FlatBufferBuilder(0);
        int createString = flatBufferBuilder.createString((CharSequence) imsUri.toString());
        int createString2 = flatBufferBuilder.createString((CharSequence) parseStr(str));
        int createString3 = flatBufferBuilder.createString((CharSequence) parseStr(str2));
        RequestChatbotAnonymize.startRequestChatbotAnonymize(flatBufferBuilder);
        RequestChatbotAnonymize.addRegistrationHandle(flatBufferBuilder, (long) userAgent.getHandle());
        RequestChatbotAnonymize.addChatbotUri(flatBufferBuilder, createString);
        RequestChatbotAnonymize.addAnonymizeInfo(flatBufferBuilder, createString2);
        RequestChatbotAnonymize.addCommandId(flatBufferBuilder, createString3);
        int endRequestChatbotAnonymize = RequestChatbotAnonymize.endRequestChatbotAnonymize(flatBufferBuilder);
        Request.startRequest(flatBufferBuilder);
        Request.addReqid(flatBufferBuilder, Id.REQUEST_CHATBOT_ANONYMIZE);
        Request.addReqType(flatBufferBuilder, (byte) 52);
        Request.addReq(flatBufferBuilder, endRequestChatbotAnonymize);
        sendRequestToStack(Id.REQUEST_CHATBOT_ANONYMIZE, flatBufferBuilder, Request.endRequest(flatBufferBuilder), this.mStackResponseHandler.obtainMessage(32), userAgent);
    }

    private void sendRequestToStack(int i, FlatBufferBuilder flatBufferBuilder, int i2, Message message) {
        UserAgent userAgent = getUserAgent();
        if (userAgent == null) {
            Log.e(this.LOG_TAG, "sendRequestToStack(): UserAgent not found.");
        } else {
            sendRequestToStack(i, flatBufferBuilder, i2, message, userAgent);
        }
    }

    private void sendRequestToStack(int i, FlatBufferBuilder flatBufferBuilder, int i2, Message message, UserAgent userAgent) {
        if (userAgent == null) {
            Log.e(this.LOG_TAG, "sendRequestToStack(): UserAgent not found.");
        } else {
            userAgent.sendRequestToStack(new ResipStackRequest(i, flatBufferBuilder, i2, message));
        }
    }

    /* access modifiers changed from: protected */
    public void sendCallback(Message message, Object obj) {
        AsyncResult.forMessage(message, obj, (Throwable) null);
        message.sendToTarget();
    }

    private int[] getStringOffsetArray(FlatBufferBuilder flatBufferBuilder, Iterable<String> iterable, int i) {
        int[] iArr = new int[i];
        int i2 = 0;
        for (String next : iterable) {
            if (next != null && !next.isEmpty()) {
                iArr[i2] = flatBufferBuilder.createString((CharSequence) next);
                i2++;
            }
        }
        return iArr;
    }

    private int[] getImsUriOffsetArray(FlatBufferBuilder flatBufferBuilder, Iterable<ImsUri> iterable, int i) {
        int[] iArr = new int[i];
        int i2 = 0;
        for (ImsUri next : iterable) {
            if (next != null && !next.toString().isEmpty()) {
                iArr[i2] = flatBufferBuilder.createString((CharSequence) next.toString());
                i2++;
            }
        }
        return iArr;
    }

    public void subscribeGroupChatList(int i, boolean z, String str) {
        Log.i(this.LOG_TAG, "subscribeGroupChatList()");
        sendMessage(obtainMessage(24, new GroupChatListParams(i, z, str)));
    }

    public void subscribeGroupChatInfo(Uri uri, String str) {
        String str2 = this.LOG_TAG;
        Log.i(str2, "subscribeGroupChatInfo() uri:" + uri.toString());
        sendMessage(obtainMessage(25, new GroupChatInfoParams(uri, str)));
    }

    public void registerForGroupChatListUpdate(Handler handler, int i, Object obj) {
        this.mGroupChatListRegistrants.add(new Registrant(handler, i, obj));
    }

    public void unRegisterForGroupChatListUpdate(Handler handler) {
        this.mGroupChatListRegistrants.remove(handler);
    }

    public void registerForGroupChatInfoUpdate(Handler handler, int i, Object obj) {
        this.mGroupChatInfoRegistrants.add(new Registrant(handler, i, obj));
    }

    public void unRegisterForGroupChatInfoUpdate(Handler handler) {
        this.mGroupChatInfoRegistrants.remove(handler);
    }

    public void registerForMessageRevokeResponse(Handler handler, int i, Object obj) {
        this.mMessageRevokeResponseRegistransts.add(new Registrant(handler, i, obj));
    }

    public void unregisterForMessageRevokeResponse(Handler handler) {
        this.mMessageRevokeResponseRegistransts.remove(handler);
    }

    public void registerForSendMessageRevokeDone(Handler handler, int i, Object obj) {
        this.mSendMessageRevokeResponseRegistransts.add(new Registrant(handler, i, obj));
    }

    public void unregisterForSendMessageRevokeDone(Handler handler) {
        this.mSendMessageRevokeResponseRegistransts.remove(handler);
    }

    public void setMoreInfoToSipUserAgent(String str, int i) {
        sendMessage(obtainMessage(29, i, 0, str));
    }

    public void requestChatbotAnonymize(ChatbotAnonymizeParams chatbotAnonymizeParams) {
        sendMessage(obtainMessage(32, chatbotAnonymizeParams));
    }

    public void registerForChatbotAnonymizeResp(Handler handler, int i, Object obj) {
        this.mChatbotAnonymizeResponseRegistrants.add(handler, i, obj);
    }

    public void unregisterForChatbotAnonymizeResp(Handler handler) {
        this.mChatbotAnonymizeResponseRegistrants.remove(handler);
    }

    public void registerForChatbotAnonymizeNotify(Handler handler, int i, Object obj) {
        this.mChatbotAnonymizeNotifyRegistrants.add(handler, i, obj);
    }

    public void unregisterForChatbotAnonymizeNotify(Handler handler) {
        this.mChatbotAnonymizeNotifyRegistrants.remove(handler);
    }

    public void reportChatbotAsSpam(ReportChatbotAsSpamParams reportChatbotAsSpamParams) {
        sendMessage(obtainMessage(31, reportChatbotAsSpamParams));
    }

    public void registerForChatbotAsSpamNotify(Handler handler, int i, Object obj) {
        this.mReportChatbotAsSpamRespRegistrants.add(handler, i, obj);
    }

    public void unregisterForChatbotAsSpamNotify(Handler handler) {
        this.mReportChatbotAsSpamRespRegistrants.remove(handler);
    }

    public UserAgent getUserAgent(String str, String str2) {
        return (UserAgent) this.mImsFramework.getRegistrationManager().getUserAgentByImsi(str, str2);
    }

    /* access modifiers changed from: protected */
    public UserAgent getUserAgent(String str) {
        return (UserAgent) this.mImsFramework.getRegistrationManager().getUserAgentByImsi("im", str);
    }

    /* access modifiers changed from: protected */
    public UserAgent getUserAgent(String str, int i) {
        return (UserAgent) this.mImsFramework.getRegistrationManager().getUserAgent(str, i);
    }

    /* access modifiers changed from: protected */
    public UserAgent getUserAgent(int i) {
        return (UserAgent) this.mImsFramework.getRegistrationManager().getUserAgent(i);
    }

    /* access modifiers changed from: protected */
    public UserAgent getUserAgent() {
        return (UserAgent) this.mImsFramework.getRegistrationManager().getUserAgent("im");
    }

    /* access modifiers changed from: protected */
    public String getImsiByUserAgent(UserAgent userAgent) {
        return this.mImsFramework.getRegistrationManager().getImsiByUserAgent(userAgent);
    }
}
