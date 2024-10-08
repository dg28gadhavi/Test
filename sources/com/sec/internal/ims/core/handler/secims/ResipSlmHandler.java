package com.sec.internal.ims.core.handler.secims;

import android.annotation.SuppressLint;
import android.os.Handler;
import android.os.Looper;
import android.os.Message;
import android.text.TextUtils;
import android.util.Log;
import android.webkit.MimeTypeMap;
import com.google.flatbuffers.FlatBufferBuilder;
import com.sec.ims.util.ImsUri;
import com.sec.internal.constants.ims.servicemodules.im.ImCpimNamespaces;
import com.sec.internal.constants.ims.servicemodules.im.ImError;
import com.sec.internal.constants.ims.servicemodules.im.ImImdnRecRoute;
import com.sec.internal.constants.ims.servicemodules.im.MaapNamespace;
import com.sec.internal.constants.ims.servicemodules.im.SlmMode;
import com.sec.internal.constants.ims.servicemodules.im.event.FtIncomingSessionEvent;
import com.sec.internal.constants.ims.servicemodules.im.event.FtTransferProgressEvent;
import com.sec.internal.constants.ims.servicemodules.im.event.SlmIncomingMessageEvent;
import com.sec.internal.constants.ims.servicemodules.im.event.SlmLMMIncomingSessionEvent;
import com.sec.internal.constants.ims.servicemodules.im.params.AcceptFtSessionParams;
import com.sec.internal.constants.ims.servicemodules.im.params.AcceptSlmParams;
import com.sec.internal.constants.ims.servicemodules.im.params.RejectFtSessionParams;
import com.sec.internal.constants.ims.servicemodules.im.params.RejectSlmParams;
import com.sec.internal.constants.ims.servicemodules.im.params.SendImdnParams;
import com.sec.internal.constants.ims.servicemodules.im.params.SendReportMsgParams;
import com.sec.internal.constants.ims.servicemodules.im.params.SendSlmFileTransferParams;
import com.sec.internal.constants.ims.servicemodules.im.params.SendSlmMessageParams;
import com.sec.internal.constants.ims.servicemodules.im.reason.FtRejectReason;
import com.sec.internal.constants.ims.servicemodules.im.reason.ImSessionRejectReason;
import com.sec.internal.constants.ims.servicemodules.im.result.FtResult;
import com.sec.internal.constants.ims.servicemodules.im.result.Result;
import com.sec.internal.constants.ims.servicemodules.im.result.SendSlmResult;
import com.sec.internal.helper.AsyncResult;
import com.sec.internal.helper.FileUtils;
import com.sec.internal.helper.Iso8601;
import com.sec.internal.helper.RegistrantList;
import com.sec.internal.ims.core.handler.SlmHandler;
import com.sec.internal.ims.core.handler.secims.imsCommonStruc.BaseSessionData;
import com.sec.internal.ims.core.handler.secims.imsCommonStruc.CpimNamespace;
import com.sec.internal.ims.core.handler.secims.imsCommonStruc.CpimNamespace_.Pair;
import com.sec.internal.ims.core.handler.secims.imsCommonStruc.FtPayloadParam;
import com.sec.internal.ims.core.handler.secims.imsCommonStruc.Id;
import com.sec.internal.ims.core.handler.secims.imsCommonStruc.ImFileAttr;
import com.sec.internal.ims.core.handler.secims.imsCommonStruc.ImMessageParam;
import com.sec.internal.ims.core.handler.secims.imsCommonStruc.ImdnParams;
import com.sec.internal.ims.core.handler.secims.imsCommonStruc.ImdnRecRoute;
import com.sec.internal.ims.core.handler.secims.imsCommonStruc.Notify;
import com.sec.internal.ims.core.handler.secims.imsCommonStruc.Notify_.FtIncomingSession;
import com.sec.internal.ims.core.handler.secims.imsCommonStruc.Notify_.SlmLMMInvited;
import com.sec.internal.ims.core.handler.secims.imsCommonStruc.Notify_.SlmMessageIncoming;
import com.sec.internal.ims.core.handler.secims.imsCommonStruc.Notify_.SlmProgress;
import com.sec.internal.ims.core.handler.secims.imsCommonStruc.Notify_.SlmSipResponseReceived;
import com.sec.internal.ims.core.handler.secims.imsCommonStruc.Participant;
import com.sec.internal.ims.core.handler.secims.imsCommonStruc.ReportMessageHdr;
import com.sec.internal.ims.core.handler.secims.imsCommonStruc.Request;
import com.sec.internal.ims.core.handler.secims.imsCommonStruc.Request_.RequestAcceptSlm;
import com.sec.internal.ims.core.handler.secims.imsCommonStruc.Request_.RequestCancelFtSession;
import com.sec.internal.ims.core.handler.secims.imsCommonStruc.Request_.RequestRejectSlm;
import com.sec.internal.ims.core.handler.secims.imsCommonStruc.Request_.RequestSendImSlmMessage;
import com.sec.internal.ims.core.handler.secims.imsCommonStruc.Request_.RequestSendSlmFile;
import com.sec.internal.ims.core.handler.secims.imsCommonStruc.Response_.CloseSessionResponse;
import com.sec.internal.ims.core.handler.secims.imsCommonStruc.Response_.SendSlmResponse;
import com.sec.internal.ims.core.handler.secims.imsCommonStruc.Response_.StartSessionResponse;
import com.sec.internal.ims.core.handler.secims.imsCommonStruc.WarningHdr;
import com.sec.internal.ims.translate.ResipTranslatorCollection;
import com.sec.internal.interfaces.ims.IImsFramework;
import com.sec.internal.interfaces.ims.core.IRegistrationManager;
import com.sec.internal.log.IMSLog;
import java.io.File;
import java.text.ParseException;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Locale;
import java.util.Map;

public class ResipSlmHandler extends SlmHandler {
    private static final int EVENT_ACCEPT_FT_SLM_MESSAGE = 2;
    private static final int EVENT_ACCEPT_SLM = 7;
    private static final int EVENT_CANCEL_FT_SLM_MESSAGE = 3;
    private static final int EVENT_REJECT_FT_SLM_MESSAGE = 4;
    private static final int EVENT_REJECT_SLM = 8;
    private static final int EVENT_SEND_DISPOSITION_NOTIFICATION = 6;
    private static final int EVENT_SEND_FT_SLM_MESSAGE = 5;
    private static final int EVENT_SEND_SLM_MESSAGE = 1;
    private static final int EVENT_STACK_NOTIFY = 100;
    /* access modifiers changed from: private */
    public static final String LOG_TAG = ResipSlmHandler.class.getSimpleName();
    private final ResipImdnHandler mImdnHandler;
    private final IImsFramework mImsFramework;
    private final RegistrantList mIncomingFileTransferRegistrants;
    private final RegistrantList mIncomingMessageRegistrants;
    private final RegistrantList mIncomingSlmLMMSessionRegistrants;
    private final Map<String, StandaloneMessage> mMessageSendRequests;
    @SuppressLint({"UseSparseArrays"})
    private final Map<Integer, StandaloneMessage> mMessages;
    private final StackIFHandler mStackResponseHandler;
    private final RegistrantList mTransferProgressRegistrants;

    public static final class StandaloneMessage {
        public boolean isFile;
        public RejectFtSessionParams mCancelParams = null;
        public long mFileSize;
        public int mId = -1;
        public FtIncomingSession mIncomingFtSession;
        public boolean mIsChatbotMessage;
        public SlmMode mMode;
        public Integer mSessionHandle = -1;
        public Message mStatusCallback;
        public int mUaHandle;
    }

    private String parseStr(String str) {
        return str != null ? str : "";
    }

    private final class StackIFHandler extends Handler {
        StackIFHandler(Looper looper) {
            super(looper);
        }

        public void handleMessage(Message message) {
            int i = message.what;
            if (i == 1) {
                ResipSlmHandler.this.handleSendMessageResponse((SendSlmResponse) ((AsyncResult) message.obj).result);
            } else if (i == 3) {
                ResipSlmHandler.this.handleCancelResponse((CloseSessionResponse) ((AsyncResult) message.obj).result);
            } else if (i == 5) {
                ResipSlmHandler.this.handleSendFileResponse((SendSlmResponse) ((AsyncResult) message.obj).result);
            } else if (i == 100) {
                ResipSlmHandler.this.handleNotify((Notify) ((AsyncResult) message.obj).result);
            } else if (i == 7) {
                ResipSlmHandler.this.handleAcceptSlmResponse((StartSessionResponse) ((AsyncResult) message.obj).result);
            } else if (i != 8) {
                String r2 = ResipSlmHandler.LOG_TAG;
                Log.e(r2, "mStackResponseHandler.handleMessage(): unhandled event - " + message);
            } else {
                ResipSlmHandler.this.handleRejectSlmResponse((CloseSessionResponse) ((AsyncResult) message.obj).result);
            }
        }
    }

    public ResipSlmHandler(Looper looper, IImsFramework iImsFramework) {
        this(looper, iImsFramework, new ResipImdnHandler(looper, iImsFramework));
    }

    public ResipSlmHandler(Looper looper, IImsFramework iImsFramework, ResipImdnHandler resipImdnHandler) {
        super(looper);
        this.mIncomingMessageRegistrants = new RegistrantList();
        this.mIncomingFileTransferRegistrants = new RegistrantList();
        this.mTransferProgressRegistrants = new RegistrantList();
        this.mIncomingSlmLMMSessionRegistrants = new RegistrantList();
        this.mMessages = new HashMap();
        this.mMessageSendRequests = new HashMap();
        this.mImsFramework = iImsFramework;
        StackIFHandler stackIFHandler = new StackIFHandler(looper);
        this.mStackResponseHandler = stackIFHandler;
        this.mImdnHandler = resipImdnHandler;
        StackIF.getInstance().registerSlmHandler(stackIFHandler, 100, (Object) null);
    }

    public void sendSlmDeliveredNotification(SendImdnParams sendImdnParams) {
        sendMessage(obtainMessage(6, sendImdnParams));
    }

    public void sendSlmDisplayedNotification(SendImdnParams sendImdnParams) {
        sendMessage(obtainMessage(6, sendImdnParams));
    }

    public void sendSlmMessage(SendSlmMessageParams sendSlmMessageParams) {
        sendMessage(obtainMessage(1, sendSlmMessageParams));
    }

    public void sendFtSlmMessage(SendSlmFileTransferParams sendSlmFileTransferParams) {
        sendMessage(obtainMessage(5, sendSlmFileTransferParams));
    }

    public void acceptFtSlmMessage(AcceptFtSessionParams acceptFtSessionParams) {
        sendMessage(obtainMessage(2, acceptFtSessionParams));
    }

    public void rejectFtSlmMessage(RejectFtSessionParams rejectFtSessionParams) {
        sendMessage(obtainMessage(4, rejectFtSessionParams));
    }

    public void cancelFtSlmMessage(RejectFtSessionParams rejectFtSessionParams) {
        sendMessage(obtainMessage(3, rejectFtSessionParams));
    }

    public void registerForSlmIncomingMessage(Handler handler, int i, Object obj) {
        this.mIncomingMessageRegistrants.addUnique(handler, i, obj);
    }

    public void unregisterForSlmIncomingMessage(Handler handler) {
        this.mIncomingMessageRegistrants.remove(handler);
    }

    public void registerForSlmIncomingFileTransfer(Handler handler, int i, Object obj) {
        this.mIncomingFileTransferRegistrants.addUnique(handler, i, obj);
    }

    public void unregisterForSlmIncomingFileTransfer(Handler handler) {
        this.mIncomingFileTransferRegistrants.remove(handler);
    }

    public void registerForSlmTransferProgress(Handler handler, int i, Object obj) {
        this.mTransferProgressRegistrants.addUnique(handler, i, obj);
    }

    public void unregisterForSlmTransferProgress(Handler handler) {
        this.mTransferProgressRegistrants.remove(handler);
    }

    public void acceptSlm(AcceptSlmParams acceptSlmParams) {
        sendMessage(obtainMessage(7, acceptSlmParams));
    }

    public void rejectSlm(RejectSlmParams rejectSlmParams) {
        sendMessage(obtainMessage(8, rejectSlmParams));
    }

    public void registerForSlmLMMIncomingSession(Handler handler, int i, Object obj) {
        this.mIncomingSlmLMMSessionRegistrants.addUnique(handler, i, obj);
    }

    public void unregisterForSlmLMMIncomingSession(Handler handler) {
        this.mIncomingSlmLMMSessionRegistrants.remove(handler);
    }

    public void handleMessage(Message message) {
        switch (message.what) {
            case 1:
                handleSendMessageRequest((SendSlmMessageParams) message.obj);
                return;
            case 2:
                handleAcceptFileRequest((AcceptFtSessionParams) message.obj);
                return;
            case 3:
            case 4:
                handleCancelFileTransfer((RejectFtSessionParams) message.obj);
                return;
            case 5:
                handleSendFileRequest((SendSlmFileTransferParams) message.obj);
                return;
            case 6:
                this.mImdnHandler.sendDispositionNotification((SendImdnParams) message.obj, 0, -1);
                return;
            case 7:
                handleAcceptSlmRequest((AcceptSlmParams) message.obj);
                return;
            case 8:
                handleRejectSlmRequest((RejectSlmParams) message.obj);
                return;
            default:
                Log.e(LOG_TAG, "handleMessage: Undefined message.");
                return;
        }
    }

    private void handleSendMessageRequest(SendSlmMessageParams sendSlmMessageParams) {
        String str;
        String str2;
        String str3;
        int i;
        Iterator<ImsUri> it;
        String str4;
        SendSlmMessageParams sendSlmMessageParams2 = sendSlmMessageParams;
        String str5 = LOG_TAG;
        Log.i(str5, "handleSendMessageRequest(): " + sendSlmMessageParams2);
        UserAgent userAgent = (UserAgent) this.mImsFramework.getRegistrationManager().getUserAgentByImsi("slm", sendSlmMessageParams2.mOwnImsi);
        if (userAgent == null) {
            Log.e(str5, "handleSendMessageRequest(): UserAgent not found!");
            Message message = sendSlmMessageParams2.mCallback;
            if (message != null) {
                sendCallback(message, new SendSlmResult(new Result(ImError.ENGINE_ERROR, Result.Type.ENGINE_ERROR), (String) null));
                return;
            }
            return;
        }
        StandaloneMessage standaloneMessage = new StandaloneMessage();
        standaloneMessage.mId = sendSlmMessageParams2.mMessageId;
        standaloneMessage.mStatusCallback = sendSlmMessageParams2.mCallback;
        this.mMessageSendRequests.put(sendSlmMessageParams2.mImdnMessageId, standaloneMessage);
        standaloneMessage.mUaHandle = userAgent.getHandle();
        standaloneMessage.mIsChatbotMessage = sendSlmMessageParams2.mIsChatbotParticipant;
        if (!sendSlmMessageParams2.mContentType.toLowerCase(Locale.US).contains("charset=")) {
            Log.e(str5, "handleSendMessageRequest(): missed charset, use utf8!");
            sendSlmMessageParams2.mContentType += ";charset=UTF-8";
        }
        FlatBufferBuilder flatBufferBuilder = new FlatBufferBuilder(0);
        int[] iArr = new int[sendSlmMessageParams2.mReceivers.size()];
        String str6 = sendSlmMessageParams2.mUserAlias;
        if (str6 == null) {
            str6 = "";
        }
        int createString = flatBufferBuilder.createString((CharSequence) str6);
        String str7 = sendSlmMessageParams2.mInReplyToContributionId;
        if (str7 == null) {
            str7 = "";
        }
        int createString2 = flatBufferBuilder.createString((CharSequence) str7);
        String str8 = sendSlmMessageParams2.mContributionId;
        if (str8 == null) {
            str8 = "";
        }
        int createString3 = flatBufferBuilder.createString((CharSequence) str8);
        int createString4 = flatBufferBuilder.createString((CharSequence) parseStr(sendSlmMessageParams2.mConversationId));
        SendReportMsgParams sendReportMsgParams = sendSlmMessageParams2.mReportMsgParams;
        if (sendReportMsgParams != null) {
            str = sendReportMsgParams.getSpamFrom().toString();
        } else {
            str = "";
        }
        int createString5 = flatBufferBuilder.createString((CharSequence) str);
        SendReportMsgParams sendReportMsgParams2 = sendSlmMessageParams2.mReportMsgParams;
        if (sendReportMsgParams2 != null) {
            str2 = sendReportMsgParams2.getSpamTo().toString();
        } else {
            str2 = "";
        }
        int createString6 = flatBufferBuilder.createString((CharSequence) str2);
        SendReportMsgParams sendReportMsgParams3 = sendSlmMessageParams2.mReportMsgParams;
        if (sendReportMsgParams3 != null) {
            str3 = sendReportMsgParams3.getSpamDate();
        } else {
            str3 = "";
        }
        int createString7 = flatBufferBuilder.createString((CharSequence) str3);
        int createString8 = flatBufferBuilder.createString((CharSequence) parseStr(sendSlmMessageParams2.mImdnMessageId));
        int createNotiVector = ImdnParams.createNotiVector(flatBufferBuilder, ResipTranslatorCollection.translateFwImdnNoti(sendSlmMessageParams2.mDispositionNotification));
        int createString9 = flatBufferBuilder.createString((CharSequence) Iso8601.formatMillis(sendSlmMessageParams2.mImdnTime));
        String str9 = "";
        int createString10 = flatBufferBuilder.createString((CharSequence) parseStr(sendSlmMessageParams2.mBody));
        UserAgent userAgent2 = userAgent;
        int createString11 = flatBufferBuilder.createString((CharSequence) parseStr(sendSlmMessageParams2.mContentType));
        Iterator<ImsUri> it2 = sendSlmMessageParams2.mReceivers.iterator();
        int i2 = 0;
        while (it2.hasNext()) {
            ImsUri next = it2.next();
            if (next != null) {
                String imsUri = next.toString();
                it = it2;
                str4 = imsUri;
            } else {
                it = it2;
                str4 = str9;
            }
            int createString12 = flatBufferBuilder.createString((CharSequence) str4);
            Participant.startParticipant(flatBufferBuilder);
            Participant.addUri(flatBufferBuilder, createString12);
            if (sendSlmMessageParams2.mIsBroadcastMsg) {
                Participant.addCopyControl(flatBufferBuilder, 2);
            }
            iArr[i2] = Participant.endParticipant(flatBufferBuilder);
            i2++;
            it2 = it;
        }
        int createParticipantVector = RequestSendImSlmMessage.createParticipantVector(flatBufferBuilder, iArr);
        BaseSessionData.startBaseSessionData(flatBufferBuilder);
        BaseSessionData.addUserAlias(flatBufferBuilder, createString);
        BaseSessionData.addInReplyToContributionId(flatBufferBuilder, createString2);
        if (sendSlmMessageParams2.mConversationId != null) {
            BaseSessionData.addConversationId(flatBufferBuilder, createString4);
        }
        if (sendSlmMessageParams2.mContributionId != null) {
            BaseSessionData.addContributionId(flatBufferBuilder, createString3);
        }
        BaseSessionData.addIsChatbotParticipant(flatBufferBuilder, sendSlmMessageParams2.mIsChatbotParticipant);
        int endBaseSessionData = BaseSessionData.endBaseSessionData(flatBufferBuilder);
        ReportMessageHdr.startReportMessageHdr(flatBufferBuilder);
        ReportMessageHdr.addSpamFrom(flatBufferBuilder, createString5);
        ReportMessageHdr.addSpamTo(flatBufferBuilder, createString6);
        ReportMessageHdr.addSpamDate(flatBufferBuilder, createString7);
        int endReportMessageHdr = ReportMessageHdr.endReportMessageHdr(flatBufferBuilder);
        ImdnParams.startImdnParams(flatBufferBuilder);
        ImdnParams.addMessageId(flatBufferBuilder, createString8);
        if (sendSlmMessageParams2.mImdnTime != null) {
            ImdnParams.addDatetime(flatBufferBuilder, createString9);
        }
        ImdnParams.addNoti(flatBufferBuilder, createNotiVector);
        int endImdnParams = ImdnParams.endImdnParams(flatBufferBuilder);
        ArrayList arrayList = new ArrayList();
        if (sendSlmMessageParams2.mMaapTrafficType != null) {
            int createString13 = flatBufferBuilder.createString((CharSequence) MaapNamespace.NAME);
            int createString14 = flatBufferBuilder.createString((CharSequence) MaapNamespace.URI);
            int createString15 = flatBufferBuilder.createString((CharSequence) "Traffic-Type");
            int createString16 = flatBufferBuilder.createString((CharSequence) sendSlmMessageParams2.mMaapTrafficType);
            Pair.startPair(flatBufferBuilder);
            Pair.addKey(flatBufferBuilder, createString15);
            Pair.addValue(flatBufferBuilder, createString16);
            int createHeadersVector = CpimNamespace.createHeadersVector(flatBufferBuilder, new int[]{Pair.endPair(flatBufferBuilder)});
            CpimNamespace.startCpimNamespace(flatBufferBuilder);
            CpimNamespace.addName(flatBufferBuilder, createString13);
            CpimNamespace.addUri(flatBufferBuilder, createString14);
            CpimNamespace.addHeaders(flatBufferBuilder, createHeadersVector);
            arrayList.add(Integer.valueOf(CpimNamespace.endCpimNamespace(flatBufferBuilder)));
            int size = arrayList.size();
            int[] iArr2 = new int[size];
            for (int i3 = 0; i3 < size; i3++) {
                iArr2[i3] = ((Integer) arrayList.get(i3)).intValue();
            }
            i = ImMessageParam.createCpimNamespacesVector(flatBufferBuilder, iArr2);
        } else {
            i = -1;
        }
        ImMessageParam.startImMessageParam(flatBufferBuilder);
        ImMessageParam.addUserAlias(flatBufferBuilder, createString);
        ImMessageParam.addBody(flatBufferBuilder, createString10);
        ImMessageParam.addContentType(flatBufferBuilder, createString11);
        ImMessageParam.addIsPublicAccountMsg(flatBufferBuilder, sendSlmMessageParams2.mIsPublicAccountMsg);
        ImMessageParam.addImdn(flatBufferBuilder, endImdnParams);
        if (sendSlmMessageParams2.mMaapTrafficType != null) {
            ImMessageParam.addCpimNamespaces(flatBufferBuilder, i);
        }
        int endImMessageParam = ImMessageParam.endImMessageParam(flatBufferBuilder);
        RequestSendImSlmMessage.startRequestSendImSlmMessage(flatBufferBuilder);
        RequestSendImSlmMessage.addRegistrationHandle(flatBufferBuilder, (long) userAgent2.getHandle());
        RequestSendImSlmMessage.addMessageParam(flatBufferBuilder, endImMessageParam);
        RequestSendImSlmMessage.addReportData(flatBufferBuilder, endReportMessageHdr);
        RequestSendImSlmMessage.addSessionData(flatBufferBuilder, endBaseSessionData);
        RequestSendImSlmMessage.addParticipant(flatBufferBuilder, createParticipantVector);
        int endRequestSendImSlmMessage = RequestSendImSlmMessage.endRequestSendImSlmMessage(flatBufferBuilder);
        Request.startRequest(flatBufferBuilder);
        Request.addReq(flatBufferBuilder, endRequestSendImSlmMessage);
        Request.addReqid(flatBufferBuilder, Id.REQUEST_SLM_SEND_MSG);
        Request.addReqType(flatBufferBuilder, (byte) 49);
        sendRequestToStack(Id.REQUEST_SLM_SEND_MSG, flatBufferBuilder, Request.endRequest(flatBufferBuilder), this.mStackResponseHandler.obtainMessage(1), userAgent2);
    }

    /* access modifiers changed from: private */
    public void handleSendMessageResponse(SendSlmResponse sendSlmResponse) {
        String str = LOG_TAG;
        Log.i(str, "handleSendMessageResponse()");
        StandaloneMessage remove = this.mMessageSendRequests.remove(sendSlmResponse.imdnMessageId());
        if (remove == null) {
            Log.e(str, "no message found!");
            return;
        }
        remove.mMode = SlmMode.fromId((int) sendSlmResponse.slmMode());
        remove.mSessionHandle = Integer.valueOf((int) sendSlmResponse.sessionHandle());
        Result translateImResult = ResipTranslatorCollection.translateImResult(sendSlmResponse.imError(), (Object) null);
        Log.i(str, "handleSendMessageResponse(): sessionHandle = " + remove.mSessionHandle + ", result = " + translateImResult);
        if (remove.mStatusCallback == null || translateImResult.getImError() == ImError.SUCCESS) {
            this.mMessages.put(remove.mSessionHandle, remove);
            return;
        }
        Log.e(str, "request sendMessage is failed!");
        sendCallback(remove.mStatusCallback, new SendSlmResult(translateImResult, (String) null));
        remove.mStatusCallback = null;
    }

    private void handleSendFileRequest(SendSlmFileTransferParams sendSlmFileTransferParams) {
        Iterator<ImsUri> it;
        String str;
        SendSlmFileTransferParams sendSlmFileTransferParams2 = sendSlmFileTransferParams;
        String str2 = LOG_TAG;
        Log.i(str2, "handleSendFileRequest(): " + sendSlmFileTransferParams2);
        UserAgent userAgent = (UserAgent) this.mImsFramework.getRegistrationManager().getUserAgentByImsi("slm", sendSlmFileTransferParams2.mOwnImsi);
        if (userAgent == null) {
            Log.e(str2, "handleSendMessageRequest(): UserAgent not found!");
            Message message = sendSlmFileTransferParams2.mCallback;
            if (message != null) {
                sendCallback(message, new FtResult(ImError.ENGINE_ERROR, Result.Type.ENGINE_ERROR, (Object) null));
                sendSlmFileTransferParams2.mCallback = null;
                return;
            }
            return;
        }
        StandaloneMessage standaloneMessage = new StandaloneMessage();
        standaloneMessage.mId = sendSlmFileTransferParams2.mMessageId;
        standaloneMessage.mStatusCallback = sendSlmFileTransferParams2.mCallback;
        standaloneMessage.isFile = true;
        standaloneMessage.mFileSize = sendSlmFileTransferParams2.mFileSize;
        this.mMessageSendRequests.put(sendSlmFileTransferParams2.mImdnMsgId, standaloneMessage);
        standaloneMessage.mUaHandle = userAgent.getHandle();
        FlatBufferBuilder flatBufferBuilder = new FlatBufferBuilder(0);
        String str3 = sendSlmFileTransferParams2.mUserAlias;
        if (str3 == null) {
            str3 = "";
        }
        int createString = flatBufferBuilder.createString((CharSequence) str3);
        String str4 = sendSlmFileTransferParams2.mConfUri;
        if (str4 == null) {
            str4 = "";
        }
        int createString2 = flatBufferBuilder.createString((CharSequence) str4);
        String str5 = sendSlmFileTransferParams2.mInReplyToContributionId;
        if (str5 == null) {
            str5 = "";
        }
        int createString3 = flatBufferBuilder.createString((CharSequence) str5);
        int createString4 = flatBufferBuilder.createString((CharSequence) parseStr(sendSlmFileTransferParams2.mSdpContentType));
        String str6 = sendSlmFileTransferParams2.mContributionId;
        if (str6 == null) {
            str6 = "";
        }
        int createString5 = flatBufferBuilder.createString((CharSequence) str6);
        int createString6 = flatBufferBuilder.createString((CharSequence) parseStr(sendSlmFileTransferParams2.mConversationId));
        int createString7 = flatBufferBuilder.createString((CharSequence) parseStr(sendSlmFileTransferParams2.mFileName));
        int createString8 = flatBufferBuilder.createString((CharSequence) parseStr(sendSlmFileTransferParams2.mFilePath));
        int createString9 = flatBufferBuilder.createString((CharSequence) parseStr(sendSlmFileTransferParams2.mContentType));
        int createString10 = flatBufferBuilder.createString((CharSequence) parseStr(sendSlmFileTransferParams2.mImdnMsgId));
        int createNotiVector = ImdnParams.createNotiVector(flatBufferBuilder, ResipTranslatorCollection.translateFwImdnNoti(sendSlmFileTransferParams2.mDispositionNotification));
        String str7 = "";
        int[] iArr = new int[sendSlmFileTransferParams2.mRecipients.size()];
        Iterator<ImsUri> it2 = sendSlmFileTransferParams2.mRecipients.iterator();
        int i = 0;
        while (it2.hasNext()) {
            ImsUri next = it2.next();
            if (next != null) {
                String imsUri = next.toString();
                it = it2;
                str = imsUri;
            } else {
                it = it2;
                str = str7;
            }
            int createString11 = flatBufferBuilder.createString((CharSequence) str);
            Participant.startParticipant(flatBufferBuilder);
            Participant.addUri(flatBufferBuilder, createString11);
            if (sendSlmFileTransferParams2.mIsBroadcastMsg) {
                Participant.addCopyControl(flatBufferBuilder, 2);
            }
            iArr[i] = Participant.endParticipant(flatBufferBuilder);
            i++;
            it2 = it;
        }
        int createParticipantVector = RequestSendImSlmMessage.createParticipantVector(flatBufferBuilder, iArr);
        BaseSessionData.startBaseSessionData(flatBufferBuilder);
        BaseSessionData.addUserAlias(flatBufferBuilder, createString);
        BaseSessionData.addSessionUri(flatBufferBuilder, createString2);
        BaseSessionData.addInReplyToContributionId(flatBufferBuilder, createString3);
        BaseSessionData.addSdpContentType(flatBufferBuilder, createString4);
        if (sendSlmFileTransferParams2.mContributionId != null) {
            BaseSessionData.addContributionId(flatBufferBuilder, createString5);
        }
        if (sendSlmFileTransferParams2.mConversationId != null) {
            BaseSessionData.addConversationId(flatBufferBuilder, createString6);
        }
        int endBaseSessionData = BaseSessionData.endBaseSessionData(flatBufferBuilder);
        ImFileAttr.startImFileAttr(flatBufferBuilder);
        ImFileAttr.addName(flatBufferBuilder, createString7);
        ImFileAttr.addPath(flatBufferBuilder, createString8);
        ImFileAttr.addContentType(flatBufferBuilder, createString9);
        ImFileAttr.addSize(flatBufferBuilder, (long) ((int) sendSlmFileTransferParams2.mFileSize));
        int endImFileAttr = ImFileAttr.endImFileAttr(flatBufferBuilder);
        ImdnParams.startImdnParams(flatBufferBuilder);
        ImdnParams.addMessageId(flatBufferBuilder, createString10);
        ImdnParams.addNoti(flatBufferBuilder, createNotiVector);
        int endImdnParams = ImdnParams.endImdnParams(flatBufferBuilder);
        FtPayloadParam.startFtPayloadParam(flatBufferBuilder);
        FtPayloadParam.addImdn(flatBufferBuilder, endImdnParams);
        FtPayloadParam.addFileAttr(flatBufferBuilder, endImFileAttr);
        int endFtPayloadParam = FtPayloadParam.endFtPayloadParam(flatBufferBuilder);
        RequestSendSlmFile.startRequestSendSlmFile(flatBufferBuilder);
        RequestSendSlmFile.addRegistrationHandle(flatBufferBuilder, (long) userAgent.getHandle());
        RequestSendSlmFile.addParticipant(flatBufferBuilder, createParticipantVector);
        RequestSendSlmFile.addPayloadParam(flatBufferBuilder, endFtPayloadParam);
        RequestSendSlmFile.addSessionData(flatBufferBuilder, endBaseSessionData);
        int endRequestSendSlmFile = RequestSendSlmFile.endRequestSendSlmFile(flatBufferBuilder);
        Request.startRequest(flatBufferBuilder);
        Request.addReq(flatBufferBuilder, endRequestSendSlmFile);
        Request.addReqid(flatBufferBuilder, Id.REQUEST_SLM_SEND_FILE);
        Request.addReqType(flatBufferBuilder, (byte) 50);
        sendRequestToStack(Id.REQUEST_SLM_SEND_FILE, flatBufferBuilder, Request.endRequest(flatBufferBuilder), this.mStackResponseHandler.obtainMessage(5), userAgent);
    }

    /* access modifiers changed from: private */
    public void handleSendFileResponse(SendSlmResponse sendSlmResponse) {
        String str = LOG_TAG;
        Log.i(str, "handleSendFileResponse()");
        StandaloneMessage remove = this.mMessageSendRequests.remove(sendSlmResponse.imdnMessageId());
        if (remove == null) {
            Log.e(str, "handleSendFileResponse(): no StandaloneMessage found!");
            return;
        }
        remove.mMode = SlmMode.fromId((int) sendSlmResponse.slmMode());
        if (remove.mCancelParams != null) {
            Log.i(str, "handleSendFileResponse(): send pending cancel request");
            sendCancelRequestToStack(remove);
            return;
        }
        remove.mSessionHandle = Integer.valueOf((int) sendSlmResponse.sessionHandle());
        Result translateFtResult = ResipTranslatorCollection.translateFtResult(sendSlmResponse.imError(), (Object) null);
        Log.i(str, "handleSendFileResponse(): sessionHandle = " + remove.mSessionHandle + ", result = " + translateFtResult);
        Message message = remove.mStatusCallback;
        if (message != null) {
            sendCallback(message, new FtResult(translateFtResult, remove.mSessionHandle));
            remove.mStatusCallback = null;
        }
        if (translateFtResult.getImError() != ImError.SUCCESS) {
            Log.e(str, "request sendFile is failed!");
            return;
        }
        this.mMessages.put(remove.mSessionHandle, remove);
        if (remove.mMode == SlmMode.PAGER) {
            AsyncResult asyncResult = new AsyncResult((Object) null, new FtTransferProgressEvent(Integer.valueOf((int) sendSlmResponse.sessionHandle()), remove.mId, remove.mFileSize, 0, FtTransferProgressEvent.State.TRANSFERRING, translateFtResult), (Throwable) null);
            if (this.mTransferProgressRegistrants.size() != 0) {
                this.mTransferProgressRegistrants.notifyRegistrants(asyncResult);
            } else {
                Log.e(str, "handleSendFileResponse(): no listener!");
            }
        }
    }

    private void handleAcceptFileRequest(AcceptFtSessionParams acceptFtSessionParams) {
        AcceptFtSessionParams acceptFtSessionParams2 = acceptFtSessionParams;
        String str = LOG_TAG;
        Log.i(str, "handleAcceptFileRequest(): " + acceptFtSessionParams2);
        int intValue = ((Integer) acceptFtSessionParams2.mRawHandle).intValue();
        Message message = acceptFtSessionParams2.mCallback;
        if (message != null) {
            sendCallback(message, new FtResult(ImError.SUCCESS, Result.Type.SUCCESS, (Object) Integer.valueOf(intValue)));
            acceptFtSessionParams2.mCallback = null;
        }
        StandaloneMessage remove = this.mMessages.remove(Integer.valueOf(intValue));
        if (remove == null) {
            Log.e(str, "handleAcceptFileRequest(): session not found!");
            if (this.mTransferProgressRegistrants.size() != 0) {
                this.mTransferProgressRegistrants.notifyRegistrants(new AsyncResult((Object) null, new FtTransferProgressEvent(Integer.valueOf(intValue), acceptFtSessionParams2.mMessageId, 0, 0, FtTransferProgressEvent.State.CANCELED, (Result) null), (Throwable) null));
            } else {
                Log.e(str, "handleAcceptFileRequest(): no listener!");
            }
        } else {
            FtPayloadParam payload = remove.mIncomingFtSession.payload();
            if (payload == null) {
                Log.e(str, "handleAcceptFileRequest(): ftpayload is null");
                return;
            }
            ImFileAttr fileAttr = payload.fileAttr();
            if (fileAttr == null) {
                Log.e(str, "handleAcceptFileRequest(): fileAttr is null");
                return;
            }
            String path = fileAttr.path();
            String contentType = fileAttr.contentType();
            if (path == null) {
                Log.e(str, "handleAcceptFileRequest(): file info is null");
                return;
            }
            MimeTypeMap singleton = MimeTypeMap.getSingleton();
            if (contentType != null && singleton.hasMimeType(contentType)) {
                path = path + "." + singleton.getExtensionFromMimeType(contentType);
            }
            File file = new File(path);
            boolean copyFile = FileUtils.copyFile(file, new File(acceptFtSessionParams2.mFilePath));
            try {
                file.delete();
            } catch (Exception e) {
                e.printStackTrace();
            }
            if (this.mTransferProgressRegistrants.size() != 0) {
                this.mTransferProgressRegistrants.notifyRegistrants(new AsyncResult((Object) null, new FtTransferProgressEvent(Integer.valueOf(intValue), acceptFtSessionParams2.mMessageId, fileAttr.size(), fileAttr.size(), copyFile ? FtTransferProgressEvent.State.COMPLETED : FtTransferProgressEvent.State.CANCELED, (Result) null), (Throwable) null));
            } else {
                Log.e(LOG_TAG, "handleAcceptFileRequest(): no listener!");
            }
        }
    }

    private void handleAcceptSlmRequest(AcceptSlmParams acceptSlmParams) {
        String str = LOG_TAG;
        IMSLog.s(str, "handleAcceptSlmRequest(): params " + acceptSlmParams);
        int intValue = ((Integer) acceptSlmParams.mRawHandle).intValue();
        UserAgent userAgent = (UserAgent) this.mImsFramework.getRegistrationManager().getUserAgentByImsi("slm", acceptSlmParams.mOwnImsi);
        if (userAgent == null) {
            Log.e(str, "handleAcceptSlmRequest(): User agent not found!");
            Message message = acceptSlmParams.mCallback;
            if (message != null) {
                sendCallback(message, ImError.ENGINE_ERROR);
                acceptSlmParams.mCallback = null;
                return;
            }
            return;
        }
        FlatBufferBuilder flatBufferBuilder = new FlatBufferBuilder(0);
        String str2 = acceptSlmParams.mUserAlias;
        if (str2 == null) {
            str2 = "";
        }
        int createString = flatBufferBuilder.createString((CharSequence) str2);
        RequestAcceptSlm.startRequestAcceptSlm(flatBufferBuilder);
        RequestAcceptSlm.addSessionId(flatBufferBuilder, (long) intValue);
        RequestAcceptSlm.addUserAlias(flatBufferBuilder, createString);
        int endRequestAcceptSlm = RequestAcceptSlm.endRequestAcceptSlm(flatBufferBuilder);
        Request.startRequest(flatBufferBuilder);
        Request.addReqid(flatBufferBuilder, Id.REQUEST_ACCEPT_SLM);
        Request.addReqType(flatBufferBuilder, (byte) 57);
        Request.addReq(flatBufferBuilder, endRequestAcceptSlm);
        sendRequestToStack(Id.REQUEST_ACCEPT_SLM, flatBufferBuilder, Request.endRequest(flatBufferBuilder), this.mStackResponseHandler.obtainMessage(7), userAgent);
    }

    private void handleRejectSlmRequest(RejectSlmParams rejectSlmParams) {
        String str = LOG_TAG;
        IMSLog.s(str, "handleRejectSlmRequest: " + rejectSlmParams);
        int intValue = ((Integer) rejectSlmParams.mRawHandle).intValue();
        UserAgent userAgent = (UserAgent) this.mImsFramework.getRegistrationManager().getUserAgentByImsi("slm", rejectSlmParams.mOwnImsi);
        if (userAgent == null) {
            Log.e(str, "handleRejectSlmRequest(): User agent not found!");
            Message message = rejectSlmParams.mCallback;
            if (message != null) {
                sendCallback(message, ImError.ENGINE_ERROR);
                rejectSlmParams.mCallback = null;
                return;
            }
            return;
        }
        FlatBufferBuilder flatBufferBuilder = new FlatBufferBuilder(0);
        ImSessionRejectReason imSessionRejectReason = rejectSlmParams.mSessionRejectReason;
        int createString = flatBufferBuilder.createString((CharSequence) imSessionRejectReason != null ? imSessionRejectReason.getWarningText() : "");
        WarningHdr.startWarningHdr(flatBufferBuilder);
        ImSessionRejectReason imSessionRejectReason2 = rejectSlmParams.mSessionRejectReason;
        if (imSessionRejectReason2 != null) {
            WarningHdr.addCode(flatBufferBuilder, imSessionRejectReason2.getWarningCode());
            WarningHdr.addText(flatBufferBuilder, createString);
        }
        int endWarningHdr = WarningHdr.endWarningHdr(flatBufferBuilder);
        RequestRejectSlm.startRequestRejectSlm(flatBufferBuilder);
        RequestRejectSlm.addSessionHandle(flatBufferBuilder, (long) intValue);
        ImSessionRejectReason imSessionRejectReason3 = rejectSlmParams.mSessionRejectReason;
        if (imSessionRejectReason3 != null) {
            RequestRejectSlm.addSipCode(flatBufferBuilder, (long) imSessionRejectReason3.getSipCode());
        }
        RequestRejectSlm.addWarningHdr(flatBufferBuilder, endWarningHdr);
        int endRequestRejectSlm = RequestRejectSlm.endRequestRejectSlm(flatBufferBuilder);
        Request.startRequest(flatBufferBuilder);
        Request.addReq(flatBufferBuilder, endRequestRejectSlm);
        Request.addReqid(flatBufferBuilder, Id.REQUEST_REJECT_SLM);
        Request.addReqType(flatBufferBuilder, (byte) 58);
        sendRequestToStack(Id.REQUEST_REJECT_SLM, flatBufferBuilder, Request.endRequest(flatBufferBuilder), this.mStackResponseHandler.obtainMessage(8), userAgent);
    }

    /* access modifiers changed from: private */
    public void handleAcceptSlmResponse(StartSessionResponse startSessionResponse) {
        int sessionHandle = (int) startSessionResponse.sessionHandle();
        ImError imError = ResipTranslatorCollection.translateImResult(startSessionResponse.imError(), (Object) null).getImError();
        String str = LOG_TAG;
        Log.e(str, "handleAcceptSlmResponse() sessionHandle = " + sessionHandle + ", error = " + imError);
    }

    /* access modifiers changed from: private */
    public void handleRejectSlmResponse(CloseSessionResponse closeSessionResponse) {
        int sessionHandle = (int) closeSessionResponse.sessionHandle();
        ImError imError = ResipTranslatorCollection.translateImResult(closeSessionResponse.imError(), (Object) null).getImError();
        String str = LOG_TAG;
        Log.e(str, "handleRejectSlmResponse() sessionHandle = " + sessionHandle + ", error = " + imError);
    }

    private void handleIncomingSlmMessageNotify(Notify notify) {
        ImsUri imsUri;
        String str = LOG_TAG;
        Log.i(str, "handleIncomingSlmMessageNotify()");
        if (notify.notiType() != 71) {
            Log.e(str, "handleIncomingSlmMessageNotify(): invalid notify!");
            return;
        }
        SlmMessageIncoming slmMessageIncoming = (SlmMessageIncoming) notify.noti(new SlmMessageIncoming());
        ImMessageParam msg = slmMessageIncoming.msg();
        BaseSessionData sessionData = slmMessageIncoming.sessionData();
        if (msg == null || sessionData == null) {
            Log.e(str, "handleIncomingSlmMessageNotify(): invalid data.");
            return;
        }
        SlmIncomingMessageEvent slmIncomingMessageEvent = new SlmIncomingMessageEvent();
        slmIncomingMessageEvent.mRawHandle = Integer.valueOf((int) sessionData.sessionHandle());
        IRegistrationManager registrationManager = this.mImsFramework.getRegistrationManager();
        UserAgent userAgent = (UserAgent) registrationManager.getUserAgent((int) slmMessageIncoming.userHandle());
        if (userAgent == null) {
            Log.e(str, "handleIncomingSlmMessageNotify(): User agent not found!");
            return;
        }
        slmIncomingMessageEvent.mOwnImsi = registrationManager.getImsiByUserAgent(userAgent);
        if (msg.sender() == null) {
            Log.i(str, "Invalid sender uri, return. uri=" + msg.sender());
            return;
        }
        ImsUri parse = ImsUri.parse(msg.sender());
        slmIncomingMessageEvent.mSender = parse;
        if (parse == null) {
            Log.i(str, "Invalid sender uri, return. uri=" + msg.sender());
            return;
        }
        slmIncomingMessageEvent.mUserAlias = msg.userAlias();
        slmIncomingMessageEvent.mIsPublicAccountMsg = msg.isPublicAccountMsg();
        slmIncomingMessageEvent.mParticipants = new ArrayList();
        boolean z = false;
        for (int i = 0; i < sessionData.receiversLength(); i++) {
            slmIncomingMessageEvent.mParticipants.add(ImsUri.parse(sessionData.receivers(i)));
        }
        slmIncomingMessageEvent.mContentType = msg.contentType();
        String adjustMessageBody = ResipTranslatorCollection.adjustMessageBody(msg.body(), slmIncomingMessageEvent.mContentType);
        slmIncomingMessageEvent.mBody = adjustMessageBody;
        if (adjustMessageBody != null) {
            boolean silenceSupported = msg.silenceSupported();
            slmIncomingMessageEvent.mIsRoutingMsg = silenceSupported;
            if (silenceSupported) {
                Log.i(LOG_TAG, "handleIncomingSlmMessageNotify -> routing message");
                slmIncomingMessageEvent.mRequestUri = ImsUri.parse(msg.requestUri());
                slmIncomingMessageEvent.mPAssertedId = ImsUri.parse(msg.pAssertedId());
                slmIncomingMessageEvent.mReceiver = ImsUri.parse(msg.receiver());
            }
            if (!(msg.imdn() == null || msg.imdn().messageId() == null)) {
                slmIncomingMessageEvent.mImdnMessageId = msg.imdn().messageId();
            }
            if (!(msg.imdn() == null || msg.imdn().originalToHdr() == null)) {
                slmIncomingMessageEvent.mOriginalToHdr = msg.imdn().originalToHdr();
            }
            try {
                slmIncomingMessageEvent.mImdnTime = (msg.imdn() == null || msg.imdn().datetime() == null) ? null : Iso8601.parse(msg.imdn().datetime());
            } catch (ParseException e) {
                e.printStackTrace();
            }
            ArrayList arrayList = new ArrayList();
            if (msg.imdn() != null) {
                for (int i2 = 0; i2 < msg.imdn().notiLength(); i2++) {
                    arrayList.add(Integer.valueOf(msg.imdn().noti(i2)));
                }
            }
            slmIncomingMessageEvent.mDispositionNotification = ResipTranslatorCollection.translateStackImdnNoti(arrayList);
            slmIncomingMessageEvent.mContributionId = sessionData.contributionId();
            slmIncomingMessageEvent.mConversationId = sessionData.conversationId();
            if (msg.imdn() != null && msg.imdn().recRouteLength() > 0) {
                slmIncomingMessageEvent.mImdnRecRouteList = new ArrayList();
                for (int i3 = 0; i3 < msg.imdn().recRouteLength(); i3++) {
                    ImdnRecRoute recRoute = msg.imdn().recRoute(i3);
                    if (recRoute != null) {
                        Log.i(LOG_TAG, "imdn route: " + recRoute.uri());
                        slmIncomingMessageEvent.mImdnRecRouteList.add(new ImImdnRecRoute(slmIncomingMessageEvent.mImdnMessageId, recRoute.uri(), recRoute.name()));
                    }
                }
            }
            slmIncomingMessageEvent.mCpimNamespaces = new ImCpimNamespaces();
            for (int i4 = 0; i4 < msg.cpimNamespacesLength(); i4++) {
                CpimNamespace cpimNamespaces = msg.cpimNamespaces(i4);
                if (cpimNamespaces != null) {
                    slmIncomingMessageEvent.mCpimNamespaces.addNamespace(cpimNamespaces.name(), cpimNamespaces.uri());
                    for (int i5 = 0; i5 < cpimNamespaces.headersLength(); i5++) {
                        Pair headers = cpimNamespaces.headers(i5);
                        if (headers != null && !TextUtils.isEmpty(headers.key())) {
                            slmIncomingMessageEvent.mCpimNamespaces.getNamespace(cpimNamespaces.name()).addHeader(headers.key(), headers.value());
                        }
                    }
                }
            }
            String[] parseEmailOverSlm = parseEmailOverSlm(slmIncomingMessageEvent.mSender, slmIncomingMessageEvent.mBody);
            if (parseEmailOverSlm != null) {
                slmIncomingMessageEvent.mSender = ImsUri.parse("sip:" + parseEmailOverSlm[0]);
                slmIncomingMessageEvent.mUserAlias = "";
                slmIncomingMessageEvent.mBody = parseEmailOverSlm[1];
            }
            if (slmMessageIncoming.extension() != null) {
                slmIncomingMessageEvent.mImExtensionMNOHeaders = ResipTranslatorCollection.translateFwImExtensionHeaders(slmMessageIncoming.extension());
            }
            slmIncomingMessageEvent.mIsLMM = slmMessageIncoming.isLmm();
            boolean isChatbotParticipant = sessionData.isChatbotParticipant();
            slmIncomingMessageEvent.mIsChatbotRole = isChatbotParticipant;
            if (!(!isChatbotParticipant || (imsUri = slmIncomingMessageEvent.mSender) == null || imsUri.getParam("tk") == null)) {
                z = slmIncomingMessageEvent.mSender.getParam("tk").equals("on");
                slmIncomingMessageEvent.mSender.removeParam("tk");
            }
            slmIncomingMessageEvent.mIsTokenUsed = z;
            String str2 = LOG_TAG;
            Log.i(str2, "handleIncomingSlmMessageNotify(): " + slmIncomingMessageEvent);
            RegistrantList registrantList = this.mIncomingMessageRegistrants;
            if (registrantList != null) {
                registrantList.notifyRegistrants(new AsyncResult((Object) null, slmIncomingMessageEvent, (Throwable) null));
            } else {
                Log.e(str2, "handleIncomingSlmMessageNotify(): no listener!");
            }
        }
    }

    private void handleIncomingSlmFileNotify(Notify notify) {
        String str = LOG_TAG;
        Log.i(str, "handleIncomingSlmFileNotify()");
        if (notify.notiType() != 42) {
            Log.e(str, "handleIncomingSlmFileNotify(): invalid notify");
            return;
        }
        FtIncomingSession ftIncomingSession = (FtIncomingSession) notify.noti(new FtIncomingSession());
        BaseSessionData session = ftIncomingSession.session();
        FtPayloadParam payload = ftIncomingSession.payload();
        if (session == null || payload == null) {
            Log.i(str, "handleIncomingSlmFileNotify(): invalid data");
            return;
        }
        ImFileAttr fileAttr = payload.fileAttr();
        if (fileAttr == null) {
            Log.i(str, "handleIncomingSlmFileNotify(): fileAttr is null");
            return;
        }
        IRegistrationManager registrationManager = this.mImsFramework.getRegistrationManager();
        UserAgent userAgent = (UserAgent) registrationManager.getUserAgent((int) ftIncomingSession.userHandle());
        if (userAgent == null) {
            Log.e(str, "handleIncomingSlmFileNotify(): User agent not found!");
            return;
        }
        int sessionHandle = (int) session.sessionHandle();
        StandaloneMessage standaloneMessage = new StandaloneMessage();
        standaloneMessage.mSessionHandle = Integer.valueOf(sessionHandle);
        standaloneMessage.mIncomingFtSession = ftIncomingSession;
        this.mMessages.put(Integer.valueOf(sessionHandle), standaloneMessage);
        standaloneMessage.mUaHandle = (int) ftIncomingSession.userHandle();
        FtIncomingSessionEvent ftIncomingSessionEvent = new FtIncomingSessionEvent();
        ftIncomingSessionEvent.mRawHandle = Integer.valueOf(sessionHandle);
        ftIncomingSessionEvent.mIsSlmSvcMsg = true;
        ftIncomingSessionEvent.mIsLMM = ftIncomingSession.isLmm();
        ftIncomingSessionEvent.mOwnImsi = registrationManager.getImsiByUserAgent(userAgent);
        ftIncomingSessionEvent.mSenderUri = ImsUri.parse(session.sessionUri());
        ftIncomingSessionEvent.mUserAlias = session.userAlias();
        ftIncomingSessionEvent.mParticipants = new ArrayList();
        for (int i = 0; i < session.receiversLength(); i++) {
            ImsUri parse = ImsUri.parse(session.receivers(i));
            if (parse == null) {
                Log.e(LOG_TAG, "participant has Wrong Uri.");
            } else {
                ftIncomingSessionEvent.mParticipants.add(parse);
            }
        }
        ftIncomingSessionEvent.mContentType = fileAttr.contentType();
        ftIncomingSessionEvent.mSdpContentType = session.sdpContentType();
        ftIncomingSessionEvent.mFileName = fileAttr.name();
        ftIncomingSessionEvent.mFilePath = fileAttr.path();
        ftIncomingSessionEvent.mFileSize = fileAttr.size();
        MimeTypeMap singleton = MimeTypeMap.getSingleton();
        if (singleton.hasMimeType(ftIncomingSessionEvent.mContentType)) {
            File file = new File(ftIncomingSessionEvent.mFilePath);
            ftIncomingSessionEvent.mFilePath += "." + singleton.getExtensionFromMimeType(ftIncomingSessionEvent.mContentType);
            File file2 = new File(ftIncomingSessionEvent.mFilePath);
            if (!file.exists()) {
                Log.e(LOG_TAG, "handleIncomingSlmFileNotify(): file doesn't exist! " + file.getPath());
            } else if (!file.renameTo(file2)) {
                Log.e(LOG_TAG, "handleIncomingSlmFileNotify(): failed to rename! " + file2.getPath());
            }
        }
        ftIncomingSessionEvent.mContributionId = session.contributionId();
        if (session.conversationId() != null) {
            ftIncomingSessionEvent.mConversationId = session.conversationId();
        }
        if (session.inReplyToContributionId() != null) {
            ftIncomingSessionEvent.mInReplyToConversationId = session.inReplyToContributionId();
        }
        ftIncomingSessionEvent.mStart = (int) fileAttr.start();
        ftIncomingSessionEvent.mEnd = (int) fileAttr.end();
        ftIncomingSessionEvent.mPush = payload.isPush();
        if (payload.imdn() != null) {
            ftIncomingSessionEvent.mImdnId = payload.imdn().messageId();
            ArrayList arrayList = new ArrayList();
            for (int i2 = 0; i2 < payload.imdn().notiLength(); i2++) {
                arrayList.add(Integer.valueOf(payload.imdn().noti(i2)));
            }
            ftIncomingSessionEvent.mDisposition = ResipTranslatorCollection.translateStackImdnNoti(arrayList);
            ftIncomingSessionEvent.mDeviceId = payload.imdn().deviceId();
            ftIncomingSessionEvent.mOriginalToHdr = payload.imdn().originalToHdr();
            ftIncomingSessionEvent.mRecRouteList = new ArrayList();
            for (int i3 = 0; i3 < payload.imdn().recRouteLength(); i3++) {
                ImdnRecRoute recRoute = payload.imdn().recRoute(i3);
                if (recRoute != null) {
                    ftIncomingSessionEvent.mRecRouteList.add(new ImImdnRecRoute(ftIncomingSessionEvent.mImdnId, recRoute.uri(), recRoute.name()));
                }
            }
            try {
                ftIncomingSessionEvent.mImdnTime = payload.imdn().datetime() != null ? Iso8601.parse(payload.imdn().datetime()) : new Date();
            } catch (ParseException e) {
                e.printStackTrace();
                ftIncomingSessionEvent.mImdnTime = new Date();
            }
        }
        ftIncomingSessionEvent.mCpimNamespaces = new ImCpimNamespaces();
        for (int i4 = 0; i4 < payload.cpimNamespacesLength(); i4++) {
            CpimNamespace cpimNamespaces = payload.cpimNamespaces(i4);
            if (cpimNamespaces != null) {
                ftIncomingSessionEvent.mCpimNamespaces.addNamespace(cpimNamespaces.name(), cpimNamespaces.uri());
                for (int i5 = 0; i5 < cpimNamespaces.headersLength(); i5++) {
                    Pair headers = cpimNamespaces.headers(i5);
                    if (headers != null && !TextUtils.isEmpty(headers.key())) {
                        ftIncomingSessionEvent.mCpimNamespaces.getNamespace(cpimNamespaces.name()).addHeader(headers.key(), headers.value());
                    }
                }
            }
        }
        String str2 = LOG_TAG;
        Log.i(str2, "handleIncomingSlmFileNotify(): " + ftIncomingSessionEvent);
        RegistrantList registrantList = this.mIncomingFileTransferRegistrants;
        if (registrantList != null) {
            registrantList.notifyRegistrants(new AsyncResult((Object) null, ftIncomingSessionEvent, (Throwable) null));
        } else {
            Log.e(str2, "handleIncomingSlmFileNotify(): no listener!");
        }
    }

    private void handleSlmProgress(Notify notify) {
        if (notify.notiType() != 39) {
            Log.e(LOG_TAG, "handleSlmProgress(): invalid notify");
            return;
        }
        SlmProgress slmProgress = (SlmProgress) notify.noti(new SlmProgress());
        String str = LOG_TAG;
        Log.i(str, "handleSlmProgress(): total = " + slmProgress.total() + ", transferred = " + slmProgress.transferred() + ", imdnMessageId = " + slmProgress.imdnMessageId() + ", sessionHandle = " + slmProgress.sessionHandle());
        StandaloneMessage standaloneMessage = this.mMessages.get(Integer.valueOf((int) slmProgress.sessionHandle()));
        FtTransferProgressEvent.State translateFtProgressState = ResipTranslatorCollection.translateFtProgressState((int) slmProgress.state());
        if (standaloneMessage == null) {
            Log.e(str, "handleSlmProgress(): no StandaloneMessage found!");
            return;
        }
        if (translateFtProgressState != FtTransferProgressEvent.State.TRANSFERRING) {
            this.mMessages.remove(standaloneMessage.mSessionHandle);
        }
        if (standaloneMessage.isFile) {
            AsyncResult asyncResult = new AsyncResult((Object) null, new FtTransferProgressEvent(Integer.valueOf((int) slmProgress.sessionHandle()), standaloneMessage.mId, slmProgress.total(), slmProgress.transferred(), translateFtProgressState, ResipTranslatorCollection.translateFtResult(slmProgress.imError(), (Object) null)), (Throwable) null);
            if (this.mTransferProgressRegistrants.size() != 0) {
                this.mTransferProgressRegistrants.notifyRegistrants(asyncResult);
            } else {
                Log.e(str, "handleSlmProgress(): no listener!");
            }
        } else {
            Result translateImResult = ResipTranslatorCollection.translateImResult(slmProgress.imError(), (Object) null);
            if (translateFtProgressState == FtTransferProgressEvent.State.COMPLETED || translateImResult.getImError() != ImError.SUCCESS) {
                sendCallback(standaloneMessage.mStatusCallback, new SendSlmResult(translateImResult, (String) null));
                standaloneMessage.mStatusCallback = null;
            }
        }
    }

    private void handleSlmSipResponseReceived(Notify notify) {
        String str = LOG_TAG;
        Log.i(str, "HandleSlmSipResponseReceived() Enter");
        if (notify.notiType() != 38) {
            Log.e(str, "handlSlmSipResponseReceived(): invalid notify");
            return;
        }
        SlmSipResponseReceived slmSipResponseReceived = (SlmSipResponseReceived) notify.noti(new SlmSipResponseReceived());
        StandaloneMessage standaloneMessage = this.mMessages.get(Integer.valueOf((int) slmSipResponseReceived.sessionHandle()));
        if (standaloneMessage != null) {
            boolean z = standaloneMessage.isFile;
            if (!z || standaloneMessage.mMode != SlmMode.PAGER) {
                SlmMode slmMode = standaloneMessage.mMode;
                if (slmMode != SlmMode.PAGER || z) {
                    SlmMode slmMode2 = SlmMode.LARGE_MESSAGE;
                    if (slmMode == slmMode2 && z) {
                        Result translateFtResult = ResipTranslatorCollection.translateFtResult(slmSipResponseReceived.imError(), slmSipResponseReceived.warningHdr());
                        if (translateFtResult.getImError() != ImError.SUCCESS) {
                            AsyncResult asyncResult = new AsyncResult((Object) null, new FtTransferProgressEvent(Integer.valueOf((int) slmSipResponseReceived.sessionHandle()), standaloneMessage.mId, 0, 0, FtTransferProgressEvent.State.CANCELED, translateFtResult), (Throwable) null);
                            this.mMessages.remove(standaloneMessage.mSessionHandle);
                            if (this.mTransferProgressRegistrants.size() != 0) {
                                this.mTransferProgressRegistrants.notifyRegistrants(asyncResult);
                            } else {
                                Log.e(str, "handlSlmSipResponseReceived(): no listener!");
                            }
                        }
                    } else if (slmMode == slmMode2 && !z) {
                        Result translateImResult = ResipTranslatorCollection.translateImResult(slmSipResponseReceived.imError(), slmSipResponseReceived.warningHdr());
                        if (translateImResult.getImError() != ImError.SUCCESS) {
                            Log.e(str, "handleSlmSipResponseReceived(): SipResponse is not 200 OK, result= " + translateImResult);
                            sendCallback(standaloneMessage.mStatusCallback, new SendSlmResult(translateImResult, (String) null));
                            standaloneMessage.mStatusCallback = null;
                            this.mMessages.remove(standaloneMessage.mSessionHandle);
                        }
                    }
                } else {
                    Result translateImResult2 = ResipTranslatorCollection.translateImResult(slmSipResponseReceived.imError(), slmSipResponseReceived.warningHdr());
                    Log.i(str, "handleSlmSipResponseReceived(), result= " + translateImResult2);
                    sendCallback(standaloneMessage.mStatusCallback, new SendSlmResult(translateImResult2, slmSipResponseReceived.passertedId()));
                    standaloneMessage.mStatusCallback = null;
                    this.mMessages.remove(standaloneMessage.mSessionHandle);
                }
            } else {
                Result translateFtResult2 = ResipTranslatorCollection.translateFtResult(slmSipResponseReceived.imError(), slmSipResponseReceived.warningHdr());
                Integer valueOf = Integer.valueOf((int) slmSipResponseReceived.sessionHandle());
                int i = standaloneMessage.mId;
                long j = standaloneMessage.mFileSize;
                ImError imError = translateFtResult2.getImError();
                ImError imError2 = ImError.SUCCESS;
                AsyncResult asyncResult2 = new AsyncResult((Object) null, new FtTransferProgressEvent(valueOf, i, j, imError == imError2 ? standaloneMessage.mFileSize : 0, translateFtResult2.getImError() == imError2 ? FtTransferProgressEvent.State.COMPLETED : FtTransferProgressEvent.State.CANCELED, translateFtResult2), (Throwable) null);
                if (this.mTransferProgressRegistrants.size() != 0) {
                    this.mTransferProgressRegistrants.notifyRegistrants(asyncResult2);
                } else {
                    Log.e(str, "handleSlmSipResponseReceived(): no listener!");
                }
                this.mMessages.remove(standaloneMessage.mSessionHandle);
            }
        } else {
            Log.e(str, "handleSlmSipResponseReceived(): no StandaloneMessage found!, ImdnMessageId : " + slmSipResponseReceived.imdnMessageId() + ", SessionHandle : " + slmSipResponseReceived.sessionHandle());
        }
    }

    private void handleSlmLMMIncomingSession(Notify notify) {
        String str = LOG_TAG;
        Log.i(str, "handleSlmLMMIncomingSession()");
        if (notify.notiType() != 40) {
            Log.e(str, "handleSlmLMMIncomingSession(): invalid notify");
            return;
        }
        SlmLMMInvited slmLMMInvited = (SlmLMMInvited) notify.noti(new SlmLMMInvited());
        IRegistrationManager registrationManager = this.mImsFramework.getRegistrationManager();
        UserAgent userAgent = (UserAgent) registrationManager.getUserAgent((int) slmLMMInvited.userHandle());
        if (userAgent == null) {
            Log.e(str, "handleSlmLMMIncomingSession(): UserAgent not found.");
            return;
        }
        SlmLMMIncomingSessionEvent slmLMMIncomingSessionEvent = new SlmLMMIncomingSessionEvent();
        Integer valueOf = Integer.valueOf((int) slmLMMInvited.sessionHandle());
        slmLMMIncomingSessionEvent.mRawHandle = valueOf;
        slmLMMIncomingSessionEvent.mInitiator = ImsUri.parse(slmLMMInvited.sender());
        slmLMMIncomingSessionEvent.mInitiatorAlias = slmLMMInvited.userAlias();
        slmLMMIncomingSessionEvent.mOwnImsi = registrationManager.getImsiByUserAgent(userAgent);
        slmLMMIncomingSessionEvent.mIsGroup = slmLMMInvited.isGroup();
        AsyncResult asyncResult = new AsyncResult((Object) null, slmLMMIncomingSessionEvent, (Throwable) null);
        if (this.mIncomingSlmLMMSessionRegistrants.size() != 0) {
            this.mIncomingSlmLMMSessionRegistrants.notifyRegistrants(asyncResult);
            return;
        }
        Log.i(str, "handleSlmLMMIncomingSession(): Empty registrants, reject handle=" + valueOf);
        handleRejectSlmRequest(new RejectSlmParams((String) null, valueOf, ImSessionRejectReason.BUSY_HERE, (Message) null, registrationManager.getImsiByUserAgent(userAgent)));
    }

    private void handleCancelFileTransfer(RejectFtSessionParams rejectFtSessionParams) {
        String str = LOG_TAG;
        Log.i(str, "handleCancelFileTransfer(): " + rejectFtSessionParams);
        Object obj = rejectFtSessionParams.mRawHandle;
        if (obj == null) {
            Log.i(str, "handleCancelFileTransfer: params.mRawHandle is null!");
            Message message = rejectFtSessionParams.mCallback;
            if (message != null) {
                sendCallback(message, new FtResult(ImError.UNKNOWN_ERROR, Result.Type.UNKNOWN_ERROR, (Object) null));
                rejectFtSessionParams.mCallback = null;
                return;
            }
            return;
        }
        int intValue = ((Integer) obj).intValue();
        String str2 = rejectFtSessionParams.mImdnMessageId;
        StandaloneMessage standaloneMessage = str2 != null ? this.mMessageSendRequests.get(str2) : null;
        if (standaloneMessage != null) {
            Log.i(str, "handleCancelFileTransfer(): pending message - postpone");
            standaloneMessage.mCancelParams = rejectFtSessionParams;
            return;
        }
        StandaloneMessage standaloneMessage2 = this.mMessages.get(Integer.valueOf(intValue));
        if (standaloneMessage2 == null) {
            Log.e(str, "handleCancelFileTransfer(): unknown session!");
            Message message2 = rejectFtSessionParams.mCallback;
            if (message2 != null) {
                sendCallback(message2, new FtResult(ImError.ENGINE_ERROR, Result.Type.UNKNOWN_ERROR, (Object) Integer.valueOf(intValue)));
                rejectFtSessionParams.mCallback = null;
            }
        } else if (standaloneMessage2.mCancelParams != null) {
            Log.e(str, "handleCancelFileTransfer(): cancel already in progress!");
        } else {
            standaloneMessage2.mCancelParams = rejectFtSessionParams;
            sendCancelRequestToStack(standaloneMessage2);
        }
    }

    private void sendCancelRequestToStack(StandaloneMessage standaloneMessage) {
        String str = LOG_TAG;
        Log.i(str, "sendCancelRequestToStack(): session handle = " + standaloneMessage.mSessionHandle);
        UserAgent userAgent = (UserAgent) this.mImsFramework.getRegistrationManager().getUserAgent(standaloneMessage.mUaHandle);
        if (userAgent == null) {
            Log.e(str, "sendCancelRequestToStack(): UserAgent not found.");
            Message message = standaloneMessage.mStatusCallback;
            if (message != null) {
                sendCallback(message, new FtResult(new Result(ImError.ENGINE_ERROR, Result.Type.ENGINE_ERROR), (Object) null));
                standaloneMessage.mStatusCallback = null;
                return;
            }
            return;
        }
        RejectFtSessionParams rejectFtSessionParams = standaloneMessage.mCancelParams;
        if (rejectFtSessionParams == null) {
            Log.e(str, "sendCancelRequestToStack(): null reject params!");
            return;
        }
        FtRejectReason ftRejectReason = rejectFtSessionParams.mRejectReason;
        if (ftRejectReason == null) {
            ftRejectReason = FtRejectReason.DECLINE;
        }
        FlatBufferBuilder flatBufferBuilder = new FlatBufferBuilder(0);
        int createString = flatBufferBuilder.createString((CharSequence) ftRejectReason.getWarningText());
        WarningHdr.startWarningHdr(flatBufferBuilder);
        WarningHdr.addCode(flatBufferBuilder, ftRejectReason.getWarningCode());
        WarningHdr.addText(flatBufferBuilder, createString);
        int endWarningHdr = WarningHdr.endWarningHdr(flatBufferBuilder);
        RequestCancelFtSession.startRequestCancelFtSession(flatBufferBuilder);
        RequestCancelFtSession.addSipCode(flatBufferBuilder, ftRejectReason.getSipCode());
        RequestCancelFtSession.addSessionHandle(flatBufferBuilder, (long) standaloneMessage.mSessionHandle.intValue());
        RequestCancelFtSession.addWarningHdr(flatBufferBuilder, endWarningHdr);
        int endRequestCancelFtSession = RequestCancelFtSession.endRequestCancelFtSession(flatBufferBuilder);
        Request.startRequest(flatBufferBuilder);
        Request.addReq(flatBufferBuilder, endRequestCancelFtSession);
        Request.addReqid(flatBufferBuilder, Id.REQUEST_SLM_CANCEL_SESSION);
        Request.addReqType(flatBufferBuilder, (byte) 47);
        sendRequestToStack(Id.REQUEST_SLM_CANCEL_SESSION, flatBufferBuilder, Request.endRequest(flatBufferBuilder), this.mStackResponseHandler.obtainMessage(3), userAgent);
    }

    /* access modifiers changed from: private */
    public void handleCancelResponse(CloseSessionResponse closeSessionResponse) {
        Message message;
        int sessionHandle = (int) closeSessionResponse.sessionHandle();
        Result translateImResult = ResipTranslatorCollection.translateImResult(closeSessionResponse.imError(), (Object) null);
        String str = LOG_TAG;
        Log.i(str, "handleCancelResponse(): sessionHandle = " + sessionHandle + ", error = " + translateImResult);
        StandaloneMessage remove = this.mMessages.remove(Integer.valueOf(sessionHandle));
        if (remove == null) {
            Log.e(str, "handleCancelResponse(): cannot find ftsession");
            return;
        }
        RejectFtSessionParams rejectFtSessionParams = remove.mCancelParams;
        if (rejectFtSessionParams == null || (message = rejectFtSessionParams.mCallback) == null) {
            Log.e(str, "handleCancelResponse(): no callback set");
            return;
        }
        sendCallback(message, new FtResult(translateImResult, Integer.valueOf(sessionHandle)));
        remove.mCancelParams.mCallback = null;
    }

    /* access modifiers changed from: private */
    public void handleNotify(Notify notify) {
        switch (notify.notifyid()) {
            case Id.NOTIFY_SLM_MSG_INCOMING /*18000*/:
                handleIncomingSlmMessageNotify(notify);
                return;
            case Id.NOTIFY_SLM_FILE_INCOMING /*18001*/:
                handleIncomingSlmFileNotify(notify);
                return;
            case Id.NOTIFY_SLM_PROGRESS /*18003*/:
                handleSlmProgress(notify);
                return;
            case Id.NOTIFY_SLM_LMM_INCOMING_SESSION /*18004*/:
                handleSlmLMMIncomingSession(notify);
                return;
            case Id.NOTIFY_SLM_SIP_RESPONSE_RECEIVED /*18005*/:
                handleSlmSipResponseReceived(notify);
                return;
            default:
                Log.w(LOG_TAG, "handleNotify(): unexpected id");
                return;
        }
    }

    private void sendRequestToStack(int i, FlatBufferBuilder flatBufferBuilder, int i2, Message message, UserAgent userAgent) {
        if (userAgent == null) {
            Log.e(LOG_TAG, "sendRequestToStack(): UserAgent not found.");
        } else {
            userAgent.sendRequestToStack(new ResipStackRequest(i, flatBufferBuilder, i2, message));
        }
    }

    private String[] parseEmailOverSlm(ImsUri imsUri, String str) {
        String str2 = LOG_TAG;
        Log.i(str2, "parseEmailOverSlm");
        if (imsUri == null || imsUri.getUser() == null || couldBeEmailGateway(imsUri.getUser())) {
            String[] split = str.split("( /)|( )", 2);
            for (String str3 : split) {
                Log.i(LOG_TAG, "parseEmailOverSlm: part: " + IMSLog.checker(str3));
            }
            if (split.length < 2) {
                Log.i(LOG_TAG, "parseEmailOverSlm: message type is not email");
                return null;
            } else if (!ResipUtils.validateEmailAddressFormat(split[0])) {
                return null;
            } else {
                Log.i(LOG_TAG, "parseEmailOverSlm: email type message");
                return split;
            }
        } else {
            Log.i(str2, "parseEmailOverSlm: No email gateway");
            return null;
        }
    }

    private boolean couldBeEmailGateway(String str) {
        String str2 = LOG_TAG;
        StringBuilder sb = new StringBuilder();
        sb.append("couldBeEmailGateway(");
        sb.append(str);
        sb.append(") = ");
        sb.append(str.length() <= 4);
        Log.i(str2, sb.toString());
        if (str.length() <= 4) {
            return true;
        }
        return false;
    }

    private void sendCallback(Message message, Object obj) {
        AsyncResult.forMessage(message, obj, (Throwable) null);
        message.sendToTarget();
    }
}
