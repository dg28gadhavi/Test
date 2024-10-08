package com.sec.internal.ims.core.handler.secims;

import android.net.Uri;
import android.os.Looper;
import android.os.Message;
import android.text.TextUtils;
import android.util.Log;
import com.sec.ims.util.ImsUri;
import com.sec.internal.constants.ims.servicemodules.im.ChatbotXmlUtils;
import com.sec.internal.constants.ims.servicemodules.im.ImCpimNamespaces;
import com.sec.internal.constants.ims.servicemodules.im.ImError;
import com.sec.internal.constants.ims.servicemodules.im.ImImdnRecRoute;
import com.sec.internal.constants.ims.servicemodules.im.MessageRevokeResponse;
import com.sec.internal.constants.ims.servicemodules.im.SupportedFeature;
import com.sec.internal.constants.ims.servicemodules.im.event.ChatbotAnonymizeNotifyEvent;
import com.sec.internal.constants.ims.servicemodules.im.event.ChatbotAnonymizeRespEvent;
import com.sec.internal.constants.ims.servicemodules.im.event.FtIncomingSessionEvent;
import com.sec.internal.constants.ims.servicemodules.im.event.FtTransferProgressEvent;
import com.sec.internal.constants.ims.servicemodules.im.event.ImIncomingGroupChatListEvent;
import com.sec.internal.constants.ims.servicemodules.im.event.ImIncomingMessageEvent;
import com.sec.internal.constants.ims.servicemodules.im.event.ImIncomingSessionEvent;
import com.sec.internal.constants.ims.servicemodules.im.event.ImSessionClosedEvent;
import com.sec.internal.constants.ims.servicemodules.im.event.ImSessionEstablishedEvent;
import com.sec.internal.constants.ims.servicemodules.im.event.ReportChatbotAsSpamRespEvent;
import com.sec.internal.constants.ims.servicemodules.im.event.SendImdnFailedEvent;
import com.sec.internal.constants.ims.servicemodules.im.event.SendMessageFailedEvent;
import com.sec.internal.constants.ims.servicemodules.im.params.RejectFtSessionParams;
import com.sec.internal.constants.ims.servicemodules.im.params.RejectImSessionParams;
import com.sec.internal.constants.ims.servicemodules.im.reason.FtRejectReason;
import com.sec.internal.constants.ims.servicemodules.im.reason.ImSessionRejectReason;
import com.sec.internal.constants.ims.servicemodules.im.result.FtResult;
import com.sec.internal.constants.ims.servicemodules.im.result.RejectImSessionResult;
import com.sec.internal.constants.ims.servicemodules.im.result.Result;
import com.sec.internal.constants.ims.servicemodules.im.result.SendMessageResult;
import com.sec.internal.constants.ims.servicemodules.im.result.StartImSessionResult;
import com.sec.internal.constants.ims.servicemodules.im.result.StopImSessionResult;
import com.sec.internal.helper.AsyncResult;
import com.sec.internal.helper.Iso8601;
import com.sec.internal.ims.core.handler.BaseHandler;
import com.sec.internal.ims.core.handler.secims.ResipImHandler;
import com.sec.internal.ims.core.handler.secims.imsCommonStruc.AllowHdr;
import com.sec.internal.ims.core.handler.secims.imsCommonStruc.BaseSessionData;
import com.sec.internal.ims.core.handler.secims.imsCommonStruc.CpimNamespace;
import com.sec.internal.ims.core.handler.secims.imsCommonStruc.CpimNamespace_.Pair;
import com.sec.internal.ims.core.handler.secims.imsCommonStruc.FtPayloadParam;
import com.sec.internal.ims.core.handler.secims.imsCommonStruc.GroupChatInfo;
import com.sec.internal.ims.core.handler.secims.imsCommonStruc.Id;
import com.sec.internal.ims.core.handler.secims.imsCommonStruc.ImFileAttr;
import com.sec.internal.ims.core.handler.secims.imsCommonStruc.ImMessageParam;
import com.sec.internal.ims.core.handler.secims.imsCommonStruc.ImSessionParam;
import com.sec.internal.ims.core.handler.secims.imsCommonStruc.ImdnRecRoute;
import com.sec.internal.ims.core.handler.secims.imsCommonStruc.Notify;
import com.sec.internal.ims.core.handler.secims.imsCommonStruc.Notify_.FtIncomingSession;
import com.sec.internal.ims.core.handler.secims.imsCommonStruc.Notify_.FtProgress;
import com.sec.internal.ims.core.handler.secims.imsCommonStruc.Notify_.GroupChatListUpdated;
import com.sec.internal.ims.core.handler.secims.imsCommonStruc.Notify_.ImMessageReceived;
import com.sec.internal.ims.core.handler.secims.imsCommonStruc.Notify_.ImMessageReportReceived;
import com.sec.internal.ims.core.handler.secims.imsCommonStruc.Notify_.ImSessionInvited;
import com.sec.internal.ims.core.handler.secims.imsCommonStruc.Notify_.MessageRevokeResponseReceived;
import com.sec.internal.ims.core.handler.secims.imsCommonStruc.Notify_.ReportChatbotAsSpamResponse;
import com.sec.internal.ims.core.handler.secims.imsCommonStruc.Notify_.RequestChatbotAnonymizeResponse;
import com.sec.internal.ims.core.handler.secims.imsCommonStruc.Notify_.RequestChatbotAnonymizeResponseReceived;
import com.sec.internal.ims.core.handler.secims.imsCommonStruc.Notify_.SendMessageRevokeResponse;
import com.sec.internal.ims.core.handler.secims.imsCommonStruc.Notify_.SessionClosed;
import com.sec.internal.ims.core.handler.secims.imsCommonStruc.Notify_.SessionEstablished;
import com.sec.internal.ims.core.handler.secims.imsCommonStruc.Notify_.SessionStarted;
import com.sec.internal.ims.core.handler.secims.imsCommonStruc.Response_.CloseSessionResponse;
import com.sec.internal.ims.core.handler.secims.imsCommonStruc.Response_.GeneralResponse;
import com.sec.internal.ims.core.handler.secims.imsCommonStruc.Response_.SendImMessageResponse;
import com.sec.internal.ims.core.handler.secims.imsCommonStruc.Response_.SendMessageRevokeInternalResponse;
import com.sec.internal.ims.core.handler.secims.imsCommonStruc.Response_.StartSessionResponse;
import com.sec.internal.ims.core.handler.secims.imsCommonStruc.Response_.UpdateParticipantsResponse;
import com.sec.internal.ims.core.handler.secims.imsCommonStruc.RetryHdr;
import com.sec.internal.ims.translate.ResipTranslatorCollection;
import com.sec.internal.log.IMSLog;
import java.io.UnsupportedEncodingException;
import java.net.URLDecoder;
import java.text.ParseException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;
import java.util.EnumSet;
import java.util.List;
import java.util.Optional;

public class ResipImResponseHandler extends BaseHandler {
    private static final String GROUPCHAT_ROLE_ADMIN = "Administrator";
    private static final String GROUPCHAT_ROLE_CHAIRMAN = "chairman";
    ResipImHandler mResipImHandler;

    ResipImResponseHandler(Looper looper, ResipImHandler resipImHandler) {
        super(looper);
        this.mResipImHandler = resipImHandler;
    }

    public void handleMessage(Message message) {
        int i = message.what;
        if (i == 12) {
            handleAddParticipantsResponse((UpdateParticipantsResponse) ((AsyncResult) message.obj).result);
        } else if (i == 17) {
            handleRejectImSessionResponse((CloseSessionResponse) ((AsyncResult) message.obj).result);
        } else if (i == 19) {
            handleChangeGroupChatLeaderResponse((UpdateParticipantsResponse) ((AsyncResult) message.obj).result);
        } else if (i != 100) {
            switch (i) {
                case 1:
                    handleStartImSessionResponse((StartSessionResponse) ((AsyncResult) message.obj).result);
                    return;
                case 2:
                    handleAcceptImSessionResponse((StartSessionResponse) ((AsyncResult) message.obj).result);
                    return;
                case 3:
                    AsyncResult asyncResult = (AsyncResult) message.obj;
                    handleCloseImSessionResponse((CloseSessionResponse) asyncResult.result, (Message) asyncResult.userObj);
                    return;
                case 4:
                    handleSendMessageResponse((SendImMessageResponse) ((AsyncResult) message.obj).result);
                    return;
                case 5:
                    handleAcceptFtSessionResponse((StartSessionResponse) ((AsyncResult) message.obj).result);
                    return;
                case 6:
                    handleCancelFtSessionResponse((CloseSessionResponse) ((AsyncResult) message.obj).result);
                    return;
                case 7:
                    handleRejectFtSessionResponse((CloseSessionResponse) ((AsyncResult) message.obj).result);
                    return;
                case 8:
                    handleStartFtSessionResponse((StartSessionResponse) ((AsyncResult) message.obj).result);
                    return;
                default:
                    switch (i) {
                        case 21:
                            handleRemoveParticipantsResponse((UpdateParticipantsResponse) ((AsyncResult) message.obj).result);
                            return;
                        case 22:
                            handleChangeGroupChatSubjectResponse((UpdateParticipantsResponse) ((AsyncResult) message.obj).result);
                            return;
                        case 23:
                            handleChangeGroupChatAliasResponse((UpdateParticipantsResponse) ((AsyncResult) message.obj).result);
                            return;
                        case 24:
                            handleSubscribeGroupChatListResponse((GeneralResponse) ((AsyncResult) message.obj).result);
                            return;
                        case 25:
                            handleSubscribeGroupChatInfoResponse((GeneralResponse) ((AsyncResult) message.obj).result);
                            return;
                        default:
                            switch (i) {
                                case 28:
                                    AsyncResult asyncResult2 = (AsyncResult) message.obj;
                                    String str = this.LOG_TAG;
                                    Log.i(str, "EVENT_SEND_MESSAGE_REVOKE_REQUEST: " + message);
                                    handleSendMessageRevokeInternalResponse((Message) asyncResult2.userObj, (SendMessageRevokeInternalResponse) asyncResult2.result);
                                    return;
                                case 29:
                                    handleSetMoreInfoToSipUAResponse((GeneralResponse) ((AsyncResult) message.obj).result);
                                    return;
                                case 30:
                                    handleChangeGroupChatIconResponse((UpdateParticipantsResponse) ((AsyncResult) message.obj).result);
                                    return;
                                default:
                                    String str2 = this.LOG_TAG;
                                    Log.i(str2, "mStackResponseHandler.handleMessage(): unhandled event - " + message);
                                    return;
                            }
                    }
            }
        } else {
            handleNotify((Notify) ((AsyncResult) message.obj).result);
        }
    }

    private void handleStartImSessionResponse(StartSessionResponse startSessionResponse) {
        if (startSessionResponse == null) {
            Log.e(this.LOG_TAG, "response object is null!!");
            return;
        }
        int sessionHandle = (int) startSessionResponse.sessionHandle();
        String fwSessionId = startSessionResponse.fwSessionId();
        Result translateImResult = ResipTranslatorCollection.translateImResult(startSessionResponse.imError(), (Object) null);
        ImError imError = translateImResult.getImError();
        String str = this.LOG_TAG;
        Log.i(str, "handleStartImSessionResponse(): sessionHandle = " + sessionHandle + ", fwSessionId = " + fwSessionId + ", error = " + imError);
        ResipImHandler.ImSession remove = this.mResipImHandler.mPendingSessions.remove(fwSessionId);
        if (remove == null) {
            Log.e(this.LOG_TAG, "handleStartImSessionResponse(): cannot find session!");
            return;
        }
        ImError imError2 = ImError.SUCCESS;
        if (imError == imError2) {
            remove.mSessionHandle = Integer.valueOf(sessionHandle);
            this.mResipImHandler.mSessions.put(Integer.valueOf(sessionHandle), remove);
            String str2 = this.LOG_TAG;
            Log.i(str2, "handleStartImSessionResponse(): sessionHandle = " + sessionHandle + ", fwSessionId = " + fwSessionId + ", error = " + imError);
            Message message = remove.mStartSyncCallback;
            if (message != null) {
                this.mResipImHandler.sendCallback(message, Integer.valueOf(sessionHandle));
                remove.mStartSyncCallback = null;
                return;
            }
            return;
        }
        Message message2 = remove.mStartSyncCallback;
        if (message2 != null) {
            this.mResipImHandler.sendCallback(message2, fwSessionId);
            remove.mStartSyncCallback = null;
        }
        remove.mStartProvisionalCallback = null;
        Message message3 = remove.mStartCallback;
        if (message3 != null) {
            this.mResipImHandler.sendCallback(message3, new StartImSessionResult(translateImResult, (ImsUri) null, fwSessionId));
            remove.mStartCallback = null;
        }
        Message message4 = remove.mFirstMessageCallback;
        if (message4 != null) {
            if (imError == ImError.BUSY_HERE) {
                Log.i(this.LOG_TAG, "handle 486 response as SUCCESS for the message in INVITE.");
                this.mResipImHandler.sendCallback(remove.mFirstMessageCallback, new SendMessageResult(Integer.valueOf(sessionHandle), new Result(imError2, translateImResult)));
            } else {
                this.mResipImHandler.sendCallback(message4, new SendMessageResult(Integer.valueOf(sessionHandle), translateImResult));
            }
            remove.mFirstMessageCallback = null;
        }
    }

    private void handleAcceptImSessionResponse(StartSessionResponse startSessionResponse) {
        Log.e(this.LOG_TAG, "handleAcceptImSessionResponse() called!");
        if (startSessionResponse == null) {
            Log.e(this.LOG_TAG, "handleAcceptImSessionResponse(): response is null!!");
            return;
        }
        int sessionHandle = (int) startSessionResponse.sessionHandle();
        Result translateImResult = ResipTranslatorCollection.translateImResult(startSessionResponse.imError(), (Object) null);
        ImError imError = translateImResult.getImError();
        ResipImHandler.ImSession imSession = this.mResipImHandler.mSessions.get(Integer.valueOf(sessionHandle));
        if (imSession == null) {
            String str = this.LOG_TAG;
            Log.e(str, "handleAcceptImSessionResponse(): no session found sessionHandle = " + sessionHandle + ", error = " + imError);
            return;
        }
        String str2 = this.LOG_TAG;
        IMSLog.s(str2, "handleAcceptImSessionResponse(): sessionHandle = " + sessionHandle + ", chat id = " + imSession.mChatId + ", error = " + imError);
        Message message = imSession.mAcceptCallback;
        if (message != null) {
            this.mResipImHandler.sendCallback(message, new StartImSessionResult(translateImResult, (ImsUri) null, Integer.valueOf(sessionHandle)));
            imSession.mAcceptCallback = null;
        }
    }

    private void handleSendMessageResponse(SendImMessageResponse sendImMessageResponse) {
        Log.i(this.LOG_TAG, "handleSendMessageResponse()");
        Integer valueOf = Integer.valueOf((int) sendImMessageResponse.sessionId());
        ResipImHandler.ImSession imSession = this.mResipImHandler.mSessions.get(valueOf);
        if (imSession == null) {
            String str = this.LOG_TAG;
            Log.e(str, "handleSendMessageResponse(): no session found sessionHandle=" + valueOf);
            return;
        }
        Message findAndRemoveCallback = imSession.findAndRemoveCallback(sendImMessageResponse.imdnMessageId());
        if (findAndRemoveCallback == null) {
            String str2 = this.LOG_TAG;
            Log.e(str2, "handleSendMessageResponse(): no response callback set. sessionHandle = " + valueOf + " imdnid = " + sendImMessageResponse.imdnMessageId());
            return;
        }
        this.mResipImHandler.sendCallback(findAndRemoveCallback, new SendMessageResult(valueOf, ResipTranslatorCollection.translateImResult(sendImMessageResponse.imError(), (Object) null)));
    }

    private void handleChangeGroupChatLeaderResponse(UpdateParticipantsResponse updateParticipantsResponse) {
        String str = this.LOG_TAG;
        IMSLog.s(str, "handleChangeGroupChatLeaderResponse: " + updateParticipantsResponse);
        ResipImHandler.ImSession imSession = this.mResipImHandler.mSessions.get(Integer.valueOf((int) updateParticipantsResponse.sessionHandle()));
        if (imSession == null) {
            Log.e(this.LOG_TAG, "handleChangeGroupChatLeaderResponse(): no session found");
        } else if (TextUtils.isEmpty(updateParticipantsResponse.reqKey())) {
            Log.e(this.LOG_TAG, "handleChangeGroupChatLeaderResponse(): response has no request key");
        } else {
            ImError imError = ResipTranslatorCollection.translateImResult(updateParticipantsResponse.imError(), (Object) null).getImError();
            Message remove = imSession.mChangeGCLeaderCallbacks.remove(updateParticipantsResponse.reqKey());
            if (remove != null) {
                this.mResipImHandler.sendCallback(remove, imError);
            } else {
                Log.e(this.LOG_TAG, "handleChangeGroupChatLeaderResponse(): no callback set");
            }
        }
    }

    private void handleAddParticipantsResponse(UpdateParticipantsResponse updateParticipantsResponse) {
        String str = this.LOG_TAG;
        IMSLog.s(str, "handleAddParticipantsResponse: " + updateParticipantsResponse);
        ResipImHandler.ImSession imSession = this.mResipImHandler.mSessions.get(Integer.valueOf((int) updateParticipantsResponse.sessionHandle()));
        if (imSession == null) {
            Log.e(this.LOG_TAG, "handleAddParticipantsResponse(): no session found");
        } else if (TextUtils.isEmpty(updateParticipantsResponse.reqKey())) {
            Log.e(this.LOG_TAG, "handleAddParticipantsResponse(): response has no request key");
        } else {
            ImError imError = ResipTranslatorCollection.translateImResult(updateParticipantsResponse.imError(), updateParticipantsResponse.warningHdr()).getImError();
            Message remove = imSession.mAddParticipantsCallbacks.remove(updateParticipantsResponse.reqKey());
            if (remove != null) {
                this.mResipImHandler.sendCallback(remove, imError);
            } else {
                Log.e(this.LOG_TAG, "handleAddParticipantsResponse(): no callback set");
            }
        }
    }

    private void handleRemoveParticipantsResponse(UpdateParticipantsResponse updateParticipantsResponse) {
        String str = this.LOG_TAG;
        IMSLog.s(str, "handleRemoveParticipantsResponse: " + updateParticipantsResponse);
        ResipImHandler.ImSession imSession = this.mResipImHandler.mSessions.get(Integer.valueOf((int) updateParticipantsResponse.sessionHandle()));
        if (imSession == null) {
            Log.e(this.LOG_TAG, "handleRemoveParticipantsResponse(): no session found");
        } else if (TextUtils.isEmpty(updateParticipantsResponse.reqKey())) {
            Log.e(this.LOG_TAG, "handleRemoveParticipantsResponse(): response has no request key");
        } else {
            ImError imError = ResipTranslatorCollection.translateImResult(updateParticipantsResponse.imError(), updateParticipantsResponse.warningHdr()).getImError();
            Message remove = imSession.mRemoveParticipantsCallbacks.remove(updateParticipantsResponse.reqKey());
            if (remove != null) {
                this.mResipImHandler.sendCallback(remove, imError);
            } else {
                Log.e(this.LOG_TAG, "handleRemoveParticipantsResponse(): no callback set");
            }
        }
    }

    private void handleStartFtSessionResponse(StartSessionResponse startSessionResponse) {
        int sessionHandle = (int) startSessionResponse.sessionHandle();
        String fwSessionId = startSessionResponse.fwSessionId();
        Result translateFtResult = ResipTranslatorCollection.translateFtResult(startSessionResponse.imError(), (Object) null);
        String str = this.LOG_TAG;
        Log.i(str, "handleStartFtSessionResponse(): sessionHandle = " + sessionHandle + ", FT id = " + fwSessionId + ", error = " + translateFtResult);
        ResipImHandler.FtSession remove = this.mResipImHandler.mPendingFtSessions.remove(fwSessionId);
        if (remove == null) {
            Log.e(this.LOG_TAG, "handleStartFtSessionResponse: cannot find session!");
        } else if (translateFtResult.getImError() == ImError.SUCCESS) {
            remove.mHandle = sessionHandle;
            Message message = remove.mStartSessionHandleCallback;
            if (message != null) {
                this.mResipImHandler.sendCallback(message, new FtResult(translateFtResult, Integer.valueOf(sessionHandle)));
                remove.mStartSessionHandleCallback = null;
            }
            this.mResipImHandler.mFtSessions.put(Integer.valueOf(sessionHandle), remove);
            if (remove.mCancelParams != null) {
                Log.i(this.LOG_TAG, "handleStartFtSessionResponse(): send postponed cancel request");
                this.mResipImHandler.sendFtCancelRequestToStack(remove);
            }
        } else {
            Message message2 = remove.mStartCallback;
            if (message2 != null) {
                this.mResipImHandler.sendCallback(message2, new FtResult(translateFtResult, Integer.valueOf(sessionHandle)));
                remove.mStartCallback = null;
            }
        }
    }

    private void handleAcceptFtSessionResponse(StartSessionResponse startSessionResponse) {
        Log.e(this.LOG_TAG, "handleAcceptFtSessionResponse() called!");
        int sessionHandle = (int) startSessionResponse.sessionHandle();
        Result translateFtResult = ResipTranslatorCollection.translateFtResult(startSessionResponse.imError(), (Object) null);
        ResipImHandler.FtSession ftSession = this.mResipImHandler.mFtSessions.get(Integer.valueOf(sessionHandle));
        if (ftSession == null) {
            String str = this.LOG_TAG;
            Log.e(str, "handleAcceptFtSessionResponse(): no session found sessionHandle = " + sessionHandle + ", result = " + translateFtResult);
            return;
        }
        String str2 = this.LOG_TAG;
        IMSLog.s(str2, "handleAcceptFtSessionResponse(): sessionHandle = " + sessionHandle + ", result = " + translateFtResult);
        if (translateFtResult.getImError() == ImError.SUCCESS) {
            Log.i(this.LOG_TAG, "handleAcceptFtSessionResponse INVITE response sent");
            return;
        }
        Message message = ftSession.mAcceptCallback;
        if (message != null) {
            this.mResipImHandler.sendCallback(message, new FtResult(translateFtResult, Integer.valueOf(sessionHandle)));
            ftSession.mAcceptCallback = null;
        }
    }

    private void handleCancelFtSessionResponse(CloseSessionResponse closeSessionResponse) {
        Message message;
        int sessionHandle = (int) closeSessionResponse.sessionHandle();
        Result translateFtResult = ResipTranslatorCollection.translateFtResult(closeSessionResponse.imError(), (Object) null);
        ResipImHandler.FtSession remove = this.mResipImHandler.mFtSessions.remove(Integer.valueOf(sessionHandle));
        if (remove == null) {
            String str = this.LOG_TAG;
            Log.e(str, "handleCancelFtSessionResponse(): cannot find ftsession sessionHandle = " + sessionHandle + ", result = " + translateFtResult);
            return;
        }
        String str2 = this.LOG_TAG;
        Log.i(str2, "handleCancelFtSessionResponse(): sessionHandle = " + sessionHandle + ", result = " + translateFtResult);
        RejectFtSessionParams rejectFtSessionParams = remove.mCancelParams;
        if (!(rejectFtSessionParams == null || (message = rejectFtSessionParams.mCallback) == null)) {
            this.mResipImHandler.sendCallback(message, new FtResult(translateFtResult, Integer.valueOf(sessionHandle)));
            remove.mCancelParams.mCallback = null;
        }
        remove.mCancelParams = null;
    }

    private void handleRejectFtSessionResponse(CloseSessionResponse closeSessionResponse) {
        Message message;
        int sessionHandle = (int) closeSessionResponse.sessionHandle();
        Result translateFtResult = ResipTranslatorCollection.translateFtResult(closeSessionResponse.imError(), (Object) null);
        ResipImHandler.FtSession remove = this.mResipImHandler.mFtSessions.remove(Integer.valueOf(sessionHandle));
        if (remove == null) {
            String str = this.LOG_TAG;
            Log.e(str, "handleRejectFtSessionResponse(): cannot find session sessionHandle = " + sessionHandle + ", result = " + translateFtResult);
            return;
        }
        String str2 = this.LOG_TAG;
        Log.i(str2, "handleRejectFtSessionResponse(): sessionHandle = " + sessionHandle + ", result = " + translateFtResult);
        RejectFtSessionParams rejectFtSessionParams = remove.mCancelParams;
        if (rejectFtSessionParams != null && (message = rejectFtSessionParams.mCallback) != null) {
            this.mResipImHandler.sendCallback(message, new FtResult(translateFtResult, Integer.valueOf(sessionHandle)));
            remove.mCancelParams.mCallback = null;
        }
    }

    private void handleRejectImSessionResponse(CloseSessionResponse closeSessionResponse) {
        int sessionHandle = (int) closeSessionResponse.sessionHandle();
        ImError imError = ResipTranslatorCollection.translateImResult(closeSessionResponse.imError(), (Object) null).getImError();
        ResipImHandler.ImSession remove = this.mResipImHandler.mSessions.remove(Integer.valueOf(sessionHandle));
        if (remove == null) {
            String str = this.LOG_TAG;
            Log.e(str, "handleRejectImSessionResponse(): no session found sessionHandle = " + sessionHandle + ", error = " + imError);
            return;
        }
        String str2 = this.LOG_TAG;
        Log.i(str2, "handleRejectImSessionResponse(): sessionHandle = " + sessionHandle + ", chat id = " + remove.mChatId + ", error = " + imError);
        Message message = remove.mRejectCallback;
        if (message != null) {
            this.mResipImHandler.sendCallback(message, new RejectImSessionResult(Integer.valueOf(sessionHandle), imError));
            remove.mRejectCallback = null;
        }
    }

    private void handleCloseImSessionResponse(CloseSessionResponse closeSessionResponse, Message message) {
        Log.e(this.LOG_TAG, "handleCloseImSessionResponse() called!");
        int sessionHandle = (int) closeSessionResponse.sessionHandle();
        ImError imError = ResipTranslatorCollection.translateImResult(closeSessionResponse.imError(), (Object) null).getImError();
        ResipImHandler.ImSession imSession = this.mResipImHandler.mSessions.get(Integer.valueOf(sessionHandle));
        if (imSession == null) {
            String str = this.LOG_TAG;
            Log.e(str, "handleCloseImSessionResponse(): no session found sessionHandle = " + sessionHandle + ", error = " + imError);
            return;
        }
        String str2 = this.LOG_TAG;
        IMSLog.s(str2, "handleCloseImSessionResponse(): sessionHandle = " + sessionHandle + ", chat id = " + imSession.mChatId + ", error = " + imError);
        if (message != null) {
            this.mResipImHandler.sendCallback(message, new StopImSessionResult(Integer.valueOf(sessionHandle), imError));
        }
    }

    private void handleChangeGroupChatSubjectResponse(UpdateParticipantsResponse updateParticipantsResponse) {
        String str = this.LOG_TAG;
        IMSLog.s(str, "handleChangeGcSubjectResponse: " + updateParticipantsResponse);
        ResipImHandler.ImSession imSession = this.mResipImHandler.mSessions.get(Integer.valueOf((int) updateParticipantsResponse.sessionHandle()));
        if (imSession == null) {
            Log.e(this.LOG_TAG, "handleChangeGcSubjectResponse(): no session found");
        } else if (TextUtils.isEmpty(updateParticipantsResponse.reqKey())) {
            Log.e(this.LOG_TAG, "handleChangeGcSubjectResponse(): response has no request key");
        } else {
            ImError imError = ResipTranslatorCollection.translateImResult(updateParticipantsResponse.imError(), (Object) null).getImError();
            Message remove = imSession.mChangeGCSubjectCallbacks.remove(updateParticipantsResponse.reqKey());
            if (remove != null) {
                this.mResipImHandler.sendCallback(remove, imError);
            } else {
                Log.e(this.LOG_TAG, "handleChangeGcSubjectResponse(): no callback set");
            }
        }
    }

    private void handleChangeGroupChatIconResponse(UpdateParticipantsResponse updateParticipantsResponse) {
        String str = this.LOG_TAG;
        IMSLog.s(str, "handleChangeGroupChatIconResponse: " + updateParticipantsResponse);
        ResipImHandler.ImSession imSession = this.mResipImHandler.mSessions.get(Integer.valueOf((int) updateParticipantsResponse.sessionHandle()));
        if (imSession == null) {
            Log.e(this.LOG_TAG, "handleChangeGroupChatIconResponse(): no session found");
            return;
        }
        ImError imError = ResipTranslatorCollection.translateImResult(updateParticipantsResponse.imError(), (Object) null).getImError();
        Message remove = imSession.mChangeGCIconCallbacks.remove(updateParticipantsResponse.reqKey());
        if (remove != null) {
            this.mResipImHandler.sendCallback(remove, imError);
        } else {
            Log.e(this.LOG_TAG, "handleChangeGroupChatIconResponse(): no callback set");
        }
    }

    private void handleChangeGroupChatAliasResponse(UpdateParticipantsResponse updateParticipantsResponse) {
        String str = this.LOG_TAG;
        IMSLog.s(str, "handleChangeGcAliasResponse: " + updateParticipantsResponse);
        ResipImHandler.ImSession imSession = this.mResipImHandler.mSessions.get(Integer.valueOf((int) updateParticipantsResponse.sessionHandle()));
        if (imSession == null) {
            Log.e(this.LOG_TAG, "handleChangeGcAliasResponse(): no session found");
        } else if (TextUtils.isEmpty(updateParticipantsResponse.reqKey())) {
            Log.e(this.LOG_TAG, "handleChangeGcAliasResponse(): response has no request key");
        } else {
            ImError imError = ResipTranslatorCollection.translateImResult(updateParticipantsResponse.imError(), (Object) null).getImError();
            Message remove = imSession.mChangeGCAliasCallbacks.remove(updateParticipantsResponse.reqKey());
            if (remove != null) {
                this.mResipImHandler.sendCallback(remove, imError);
            } else {
                Log.e(this.LOG_TAG, "handleChangeGcAliasResponse(): no callback set");
            }
        }
    }

    private void handleSubscribeGroupChatListResponse(GeneralResponse generalResponse) {
        String str = this.LOG_TAG;
        IMSLog.s(str, "handleSubscribeGroupChatListResponse: " + generalResponse);
    }

    private void handleSubscribeGroupChatInfoResponse(GeneralResponse generalResponse) {
        String str = this.LOG_TAG;
        IMSLog.s(str, "handleSubscribeGroupChatInfoResponse: " + generalResponse);
    }

    private void handleSendMessageRevokeInternalResponse(Message message, SendMessageRevokeInternalResponse sendMessageRevokeInternalResponse) {
        String str = this.LOG_TAG;
        Log.i(str, "handleSendMessageRevokeInternalResponse() msg : " + message + "response : " + sendMessageRevokeInternalResponse);
        if (sendMessageRevokeInternalResponse != null) {
            ImError imError = ResipTranslatorCollection.translateImResult(sendMessageRevokeInternalResponse.imError(), (Object) null).getImError();
            if (message != null) {
                this.mResipImHandler.sendCallback(message, imError);
            }
        }
    }

    private void handleSetMoreInfoToSipUAResponse(GeneralResponse generalResponse) {
        String str = this.LOG_TAG;
        Log.i(str, "handleSetMoreInfoToSipUAResponse: " + generalResponse);
    }

    private void handleNotify(Notify notify) {
        int notifyid = notify.notifyid();
        if (notifyid != 19000) {
            switch (notifyid) {
                case Id.NOTIFY_IM_SESSION_STARTED /*11001*/:
                    handleImSessionStartedNotify(notify);
                    return;
                case Id.NOTIFY_IM_SESSION_CLOSED /*11002*/:
                    handleImSessionClosedNotify(notify);
                    return;
                case Id.NOTIFY_IM_SESSION_ESTABLISHED /*11003*/:
                    handleImSessionEstablishedNotify(notify);
                    return;
                case Id.NOTIFY_IM_INCOMING_SESSION /*11004*/:
                    handleIncomingSessionNotify(notify);
                    return;
                case Id.NOTIFY_IM_MESSAGE_RECEIVED /*11005*/:
                    handleImMessageReceivedNotify(notify);
                    return;
                default:
                    switch (notifyid) {
                        case Id.NOTIFY_IM_COMPOSING_STATUS_RECEIVED /*11007*/:
                            handleImComposingStatusReceivedNotify(notify);
                            return;
                        case Id.NOTIFY_IM_MESSAGE_REPORT_RECEIVED /*11008*/:
                            handleImMessageReportReceivedNotify(notify);
                            return;
                        case Id.NOTIFY_GROUP_CHAT_SUBSCRIBE_STATUS /*11009*/:
                            handleGroupChatSubscribeStatusNotify();
                            return;
                        case Id.NOTIFY_GROUP_LIST_UPDATED /*11010*/:
                            handleGroupChatListNotify(notify);
                            return;
                        case Id.NOTIFY_GROUP_INFO_UPDATED /*11011*/:
                            handleGroupChatInfoNotify(notify);
                            return;
                        case Id.NOTIFY_IM_SESSION_PROVISIONAL_RESPONSE /*11012*/:
                            handleImSessionProvisionalResponseNotify(notify);
                            return;
                        case Id.NOTIFY_MESSAGE_REVOKE_RESPONSE_RECEIVED /*11013*/:
                            handleMessageRevokeResponseReceivedNotify(notify);
                            return;
                        case Id.NOTIFY_SEND_MESSAGE_REVOKE_RESPONSE /*11014*/:
                            handleSendMessageRevokeResponseNotify(notify);
                            return;
                        default:
                            switch (notifyid) {
                                case Id.NOTIFY_FT_SESSION_STARTED /*12001*/:
                                    handleFtSessionStartedNotify(notify);
                                    return;
                                case Id.NOTIFY_FT_SESSION_CLOSED /*12002*/:
                                    handleFtSessionClosedNotify(notify);
                                    return;
                                case Id.NOTIFY_FT_SESSION_ESTABLISHED /*12003*/:
                                    handleFtSessionEstablishedNotify(notify);
                                    return;
                                case Id.NOTIFY_FT_PROGRESS /*12004*/:
                                    handleFtProgressNotify(notify);
                                    return;
                                case Id.NOTIFY_FT_INCOMING_SESSION /*12005*/:
                                    handleFtIncomingSessionNotify(notify);
                                    return;
                                default:
                                    switch (notifyid) {
                                        case Id.NOTIFY_REPORT_CHATBOT_AS_SPAM_RESPONSE /*20011*/:
                                            handleReportChatbotAsSpamResponseNotify(notify);
                                            return;
                                        case Id.NOTIFY_REQUEST_CHATBOT_ANONYMIZE_RESPONSE /*20012*/:
                                            handleRequestChatbotAnonymizeResp(notify);
                                            return;
                                        case Id.NOTIFY_REQUEST_CHATBOT_ANONYMIZE_RESPONSE_RECEIVED /*20013*/:
                                            handleRequestChatbotAnonymizeNotify(notify);
                                            return;
                                        default:
                                            Log.w(this.LOG_TAG, "handleNotify(): unexpected id");
                                            return;
                                    }
                            }
                    }
            }
        } else {
            handleImConferenceInfoUpdateNotify(notify);
        }
    }

    private void handleIncomingSessionNotify(Notify notify) {
        boolean z;
        Log.i(this.LOG_TAG, "handleIncomingSessionNotify()");
        if (notify.notiType() != 31) {
            Log.e(this.LOG_TAG, "handleIncomingSessionNotify(): invalid notify");
            return;
        }
        ImSessionInvited imSessionInvited = (ImSessionInvited) notify.noti(new ImSessionInvited());
        UserAgent userAgent = this.mResipImHandler.getUserAgent((int) imSessionInvited.userHandle());
        if (userAgent == null) {
            Log.e(this.LOG_TAG, "handleIncomingSessionNotify(): UserAgent not found. UserHandle = " + ((int) imSessionInvited.userHandle()));
            return;
        }
        ImSessionParam session = imSessionInvited.session();
        if (session == null) {
            Log.e(this.LOG_TAG, "handleIncomingSessionNotify(): invalid notify data");
            return;
        }
        BaseSessionData baseSessionData = session.baseSessionData();
        ImMessageParam messageParam = imSessionInvited.messageParam();
        if (baseSessionData == null) {
            Log.e(this.LOG_TAG, "handleIncomingSessionNotify(): invalid notify data");
            return;
        }
        ImIncomingSessionEvent imIncomingSessionEvent = new ImIncomingSessionEvent();
        Integer valueOf = Integer.valueOf((int) baseSessionData.sessionHandle());
        imIncomingSessionEvent.mRawHandle = valueOf;
        imIncomingSessionEvent.mOwnImsi = this.mResipImHandler.getImsiByUserAgent(userAgent);
        imIncomingSessionEvent.mIsDeferred = imSessionInvited.isDeferred();
        imIncomingSessionEvent.mIsForStoredNoti = imSessionInvited.isForStoredNoti();
        this.mResipImHandler.mSessions.put(valueOf, new ResipImHandler.ImSession(valueOf.intValue(), imSessionInvited.isDeferred(), userAgent.getHandle()));
        imIncomingSessionEvent.mIsMsgRevokeSupported = session.isMsgRevokeSupported();
        imIncomingSessionEvent.mIsMsgFallbackSupported = session.isMsgFallbackSupported();
        imIncomingSessionEvent.mIsSendOnly = session.isSendOnly();
        imIncomingSessionEvent.mIsChatbotRole = baseSessionData.isChatbotParticipant();
        ImsUri parse = ImsUri.parse(session.sender());
        imIncomingSessionEvent.mInitiator = parse;
        if (!imIncomingSessionEvent.mIsChatbotRole || parse == null || parse.getParam("tk") == null) {
            z = false;
        } else {
            z = imIncomingSessionEvent.mInitiator.getParam("tk").equals("on");
            imIncomingSessionEvent.mInitiator.removeParam("tk");
        }
        imIncomingSessionEvent.mIsTokenUsed = z;
        if (!imIncomingSessionEvent.mIsDeferred || baseSessionData.isConference()) {
            imIncomingSessionEvent.mRecipients = new ArrayList();
            for (int i = 0; i < baseSessionData.receiversLength(); i++) {
                imIncomingSessionEvent.mRecipients.add(ImsUri.parse(baseSessionData.receivers(i)));
            }
        } else {
            ArrayList arrayList = new ArrayList();
            imIncomingSessionEvent.mRecipients = arrayList;
            arrayList.add(imIncomingSessionEvent.mInitiator);
        }
        if (baseSessionData.isConference()) {
            imIncomingSessionEvent.mSessionType = ImIncomingSessionEvent.ImSessionType.CONFERENCE;
            imIncomingSessionEvent.mIsClosedGroupChat = session.isClosedGroupchat();
            imIncomingSessionEvent.mSessionUri = ImsUri.parse(baseSessionData.sessionUri());
            if (imSessionInvited.createdBy() != null && !imSessionInvited.createdBy().isEmpty()) {
                imIncomingSessionEvent.mCreatedBy = ImsUri.parse(imSessionInvited.createdBy());
            }
            if (imSessionInvited.invitedBy() != null && !imSessionInvited.invitedBy().isEmpty()) {
                imIncomingSessionEvent.mInvitedBy = ImsUri.parse(imSessionInvited.invitedBy());
            }
            IMSLog.s(this.LOG_TAG, "handleIncomingSessionNotify(): session uri = " + imIncomingSessionEvent.mSessionUri);
        } else {
            imIncomingSessionEvent.mSessionType = ImIncomingSessionEvent.ImSessionType.SINGLE;
            imIncomingSessionEvent.mInitiatorAlias = baseSessionData.userAlias();
            imIncomingSessionEvent.mSessionUri = null;
        }
        if (baseSessionData.sdpContentType() != null && !baseSessionData.sdpContentType().isEmpty()) {
            imIncomingSessionEvent.mSdpContentType = baseSessionData.sdpContentType();
        }
        if (baseSessionData.serviceId() != null && !baseSessionData.serviceId().isEmpty()) {
            imIncomingSessionEvent.mServiceId = baseSessionData.serviceId();
        }
        if (!(messageParam == null || messageParam.imdn() == null)) {
            imIncomingSessionEvent.mDeviceId = messageParam.imdn().deviceId();
        }
        imIncomingSessionEvent.mSubject = (session.subject() == null || session.subject().isEmpty()) ? null : session.subject();
        imIncomingSessionEvent.mServiceType = ImIncomingSessionEvent.ImServiceType.NORMAL;
        imIncomingSessionEvent.mIsParticipantNtfy = false;
        imIncomingSessionEvent.mConversationId = (baseSessionData.conversationId() == null || baseSessionData.conversationId().isEmpty()) ? null : baseSessionData.conversationId();
        imIncomingSessionEvent.mContributionId = (baseSessionData.contributionId() == null || baseSessionData.contributionId().isEmpty()) ? null : baseSessionData.contributionId();
        if (baseSessionData.sessionReplaces() == null || baseSessionData.sessionReplaces().isEmpty()) {
            imIncomingSessionEvent.mPrevContributionId = null;
        } else {
            imIncomingSessionEvent.mPrevContributionId = baseSessionData.sessionReplaces();
        }
        imIncomingSessionEvent.mInReplyToContributionId = baseSessionData.inReplyToContributionId();
        imIncomingSessionEvent.mRemoteMsrpAddress = (imSessionInvited.remoteMsrpAddr() == null || imSessionInvited.remoteMsrpAddr().isEmpty()) ? null : imSessionInvited.remoteMsrpAddr();
        ArrayList arrayList2 = new ArrayList();
        for (int i2 = 0; i2 < session.acceptTypesLength(); i2++) {
            String acceptTypes = session.acceptTypes(i2);
            if (acceptTypes != null) {
                arrayList2.addAll(Arrays.asList(acceptTypes.split(" ")));
            }
        }
        ArrayList arrayList3 = new ArrayList();
        for (int i3 = 0; i3 < session.acceptWrappedTypesLength(); i3++) {
            String acceptWrappedTypes = session.acceptWrappedTypes(i3);
            if (acceptWrappedTypes != null) {
                arrayList3.addAll(Arrays.asList(acceptWrappedTypes.split(" ")));
            }
        }
        imIncomingSessionEvent.mAcceptTypes = arrayList2;
        imIncomingSessionEvent.mAcceptWrappedTypes = arrayList3;
        if (imSessionInvited.messageParam() != null) {
            ImIncomingMessageEvent parseImMessageParam = parseImMessageParam(imSessionInvited.messageParam());
            if (parseImMessageParam != null) {
                parseImMessageParam.mRawHandle = valueOf;
                parseImMessageParam.mUserAlias = baseSessionData.userAlias();
                Log.i(this.LOG_TAG, "handleIncomingSessionNotify(): " + parseImMessageParam);
            }
            imIncomingSessionEvent.mReceivedMessage = parseImMessageParam;
        }
        AsyncResult asyncResult = new AsyncResult((Object) null, imIncomingSessionEvent, (Throwable) null);
        if (this.mResipImHandler.mIncomingSessionRegistrants.size() != 0) {
            this.mResipImHandler.mIncomingSessionRegistrants.notifyRegistrants(asyncResult);
        } else {
            Log.i(this.LOG_TAG, "handleIncomingSessionNotify(): Empty registrants, reject handle=" + valueOf);
            this.mResipImHandler.handleRejectImSessionRequest(new RejectImSessionParams((String) null, valueOf, ImSessionRejectReason.FORBIDDEN, (Message) null));
        }
        Log.i(this.LOG_TAG, "handleIncomingSessionNotify(): " + imIncomingSessionEvent);
    }

    private void handleImSessionStartedNotify(Notify notify) {
        Result result;
        int i;
        ImError imError;
        ResipImHandler.ImSession imSession;
        Message message;
        ImError imError2;
        ResipImResponseHandler resipImResponseHandler;
        Message message2;
        if (notify.notiType() != 28) {
            Log.e(this.LOG_TAG, "handleImSessionStartedNotify(): invalid notify");
            return;
        }
        SessionStarted sessionStarted = (SessionStarted) notify.noti(new SessionStarted());
        int sessionHandle = (int) sessionStarted.sessionHandle();
        String sessionUri = sessionStarted.sessionUri();
        String displayName = sessionStarted.displayName();
        Result translateImResult = ResipTranslatorCollection.translateImResult(sessionStarted.imError(), sessionStarted.warningHdr());
        ImError imError3 = translateImResult.getImError();
        IMSLog.s(this.LOG_TAG, "handleImSessionStartedNotify(): sessionHandle = " + sessionHandle + ", error = " + imError3 + ", sessionUri = " + sessionUri + ", displayName = " + displayName);
        ResipImHandler.ImSession imSession2 = this.mResipImHandler.mSessions.get(Integer.valueOf(sessionHandle));
        if (imSession2 == null) {
            Log.e(this.LOG_TAG, "handleImSessionStartedNotify(): Unknown session handle: " + sessionHandle);
            return;
        }
        Message message3 = imSession2.mStartSyncCallback;
        if (message3 != null) {
            this.mResipImHandler.sendCallback(message3, Integer.valueOf(sessionHandle));
            imSession2.mStartSyncCallback = null;
        }
        if (imSession2.mStartCallback != null) {
            RetryHdr retryHdr = sessionStarted.retryHdr();
            AllowHdr allowHdr = sessionStarted.allowHdr();
            ResipImHandler resipImHandler = this.mResipImHandler;
            Message message4 = imSession2.mStartCallback;
            ImsUri parse = TextUtils.isEmpty(sessionUri) ? null : ImsUri.parse(sessionUri);
            Integer valueOf = Integer.valueOf(sessionHandle);
            int retryTimer = retryHdr != null ? retryHdr.retryTimer() : 0;
            String text = allowHdr != null ? allowHdr.text() : null;
            boolean isMsgRevokeSupported = sessionStarted.isMsgRevokeSupported();
            boolean isMsgFallbackSupported = sessionStarted.isMsgFallbackSupported();
            boolean isChatbotRole = sessionStarted.isChatbotRole();
            if (displayName == null) {
                displayName = "";
            }
            Result result2 = translateImResult;
            result = translateImResult;
            StartImSessionResult startImSessionResult = r7;
            i = sessionHandle;
            message = null;
            imError = imError3;
            imSession = imSession2;
            StartImSessionResult startImSessionResult2 = new StartImSessionResult(result2, parse, valueOf, retryTimer, text, isMsgRevokeSupported, isMsgFallbackSupported, isChatbotRole, displayName);
            resipImHandler.sendCallback(message4, startImSessionResult);
            imSession.mStartCallback = null;
        } else {
            i = sessionHandle;
            result = translateImResult;
            imError = imError3;
            message = null;
            imSession = imSession2;
        }
        Message message5 = imSession.mFirstMessageCallback;
        if (message5 != null) {
            imError2 = imError;
            if (imError2 == ImError.BUSY_HERE) {
                message2 = message;
                resipImResponseHandler = this;
                Log.i(resipImResponseHandler.LOG_TAG, "handle 486 response as SUCCESS for the message in INVITE.");
                resipImResponseHandler.mResipImHandler.sendCallback(imSession.mFirstMessageCallback, new SendMessageResult(Integer.valueOf(i), new Result(ImError.SUCCESS, result)));
            } else {
                message2 = message;
                resipImResponseHandler = this;
                resipImResponseHandler.mResipImHandler.sendCallback(message5, new SendMessageResult(Integer.valueOf(i), result));
            }
            imSession.mFirstMessageCallback = message2;
        } else {
            resipImResponseHandler = this;
            imError2 = imError;
        }
        if (imError2 != ImError.SUCCESS) {
            resipImResponseHandler.mResipImHandler.mSessions.remove(Integer.valueOf(i));
        }
    }

    private void handleImSessionClosedNotify(Notify notify) {
        ImsUri imsUri;
        if (notify.notiType() != 29) {
            Log.e(this.LOG_TAG, "handleImSessionClosedNotify(): invalid notify");
            return;
        }
        Log.i(this.LOG_TAG, "handleImSessionClosedNotify");
        SessionClosed sessionClosed = (SessionClosed) notify.noti(new SessionClosed());
        int sessionHandle = (int) sessionClosed.sessionHandle();
        Result translateImResult = ResipTranslatorCollection.translateImResult(sessionClosed.imError(), sessionClosed.reasonHdr());
        ImError imError = translateImResult.getImError();
        String referredBy = sessionClosed.referredBy();
        if (referredBy != null) {
            referredBy = referredBy.replaceAll("\\<|\\>", "");
            IMSLog.s(this.LOG_TAG, "handleImSessionClosedNotify() Referred By =" + IMSLog.numberChecker(referredBy));
            imsUri = ImsUri.parse(referredBy);
        } else {
            imsUri = null;
        }
        ResipImHandler.ImSession imSession = this.mResipImHandler.mSessions.get(Integer.valueOf(sessionHandle));
        if (imSession == null) {
            Log.e(this.LOG_TAG, "handleImSessionClosedNotify(): no session found sessionHandle = " + sessionHandle + ", error = " + imError);
            return;
        }
        IMSLog.s(this.LOG_TAG, "handleImSessionClosedNotify(): sessionHandle = " + sessionHandle + ", chat id = " + imSession.mChatId + ", error = " + imError + ", referredBy = " + referredBy);
        if (!(imError == ImError.NORMAL_RELEASE || imError == ImError.NORMAL_RELEASE_GONE || imError == ImError.CONFERENCE_PARTY_BOOTED || imError == ImError.CONFERENCE_CALL_COMPLETED)) {
            Log.e(this.LOG_TAG, "handleImSessionClosedNotify(): abnormal close");
            Message message = imSession.mStartSyncCallback;
            if (message != null) {
                this.mResipImHandler.sendCallback(message, Integer.valueOf(sessionHandle));
                imSession.mStartSyncCallback = null;
            }
            Message message2 = imSession.mStartCallback;
            if (message2 != null) {
                this.mResipImHandler.sendCallback(message2, new StartImSessionResult(translateImResult, (ImsUri) null, Integer.valueOf(sessionHandle)));
                imSession.mStartCallback = null;
            }
            Message message3 = imSession.mFirstMessageCallback;
            if (message3 != null && imError == ImError.DEVICE_UNREGISTERED) {
                this.mResipImHandler.sendCallback(message3, new SendMessageResult(Integer.valueOf(sessionHandle), translateImResult));
                imSession.mFirstMessageCallback = null;
            } else if (message3 != null) {
                this.mResipImHandler.sendCallback(message3, new SendMessageResult(Integer.valueOf(sessionHandle), new Result(ImError.SUCCESS, translateImResult)));
                imSession.mFirstMessageCallback = null;
            }
            Message message4 = imSession.mAcceptCallback;
            if (message4 != null) {
                this.mResipImHandler.sendCallback(message4, new StartImSessionResult(translateImResult, (ImsUri) null, Integer.valueOf(sessionHandle)));
                imSession.mAcceptCallback = null;
            }
        }
        this.mResipImHandler.mSessionClosedRegistrants.notifyRegistrants(new AsyncResult((Object) null, new ImSessionClosedEvent(Integer.valueOf(sessionHandle), imSession.mChatId, translateImResult, imsUri), (Throwable) null));
        ResipImHandler.ImSession remove = this.mResipImHandler.mSessions.remove(Integer.valueOf(sessionHandle));
        if (remove != null) {
            for (Message next : remove.mSendMessageCallbacks.values()) {
                if (next != null) {
                    this.mResipImHandler.sendCallback(next, new SendMessageResult(Integer.valueOf(sessionHandle), new Result(ImError.NETWORK_ERROR, Result.Type.NETWORK_ERROR)));
                }
            }
            for (Message next2 : remove.mAddParticipantsCallbacks.values()) {
                if (next2 != null) {
                    this.mResipImHandler.sendCallback(next2, ImError.NETWORK_ERROR);
                }
            }
            for (Message next3 : remove.mRemoveParticipantsCallbacks.values()) {
                if (next3 != null) {
                    this.mResipImHandler.sendCallback(next3, ImError.NETWORK_ERROR);
                }
            }
            for (Message next4 : remove.mChangeGCAliasCallbacks.values()) {
                if (next4 != null) {
                    this.mResipImHandler.sendCallback(next4, ImError.NETWORK_ERROR);
                }
            }
            for (Message next5 : remove.mChangeGCLeaderCallbacks.values()) {
                if (next5 != null) {
                    this.mResipImHandler.sendCallback(next5, ImError.NETWORK_ERROR);
                }
            }
            for (Message next6 : remove.mChangeGCSubjectCallbacks.values()) {
                if (next6 != null) {
                    this.mResipImHandler.sendCallback(next6, ImError.NETWORK_ERROR);
                }
            }
            for (Message next7 : remove.mChangeGCIconCallbacks.values()) {
                if (next7 != null) {
                    this.mResipImHandler.sendCallback(next7, ImError.NETWORK_ERROR);
                }
            }
        }
    }

    private void handleImMessageReceivedNotify(Notify notify) {
        if (notify.notiType() != 32) {
            Log.e(this.LOG_TAG, "handleImMessageReceivedNotify(): invalid notify");
            return;
        }
        ImMessageReceived imMessageReceived = (ImMessageReceived) notify.noti(new ImMessageReceived());
        BaseSessionData sessionData = imMessageReceived.sessionData();
        ImMessageParam messageParam = imMessageReceived.messageParam();
        if (sessionData == null || messageParam == null) {
            Log.e(this.LOG_TAG, "handleImMessageReceivedNotify(): invalid message notify data");
            return;
        }
        int sessionHandle = (int) sessionData.sessionHandle();
        String str = this.LOG_TAG;
        Log.i(str, "handleImMessageReceivedNotify(): sessionId = " + sessionHandle);
        ResipImHandler.ImSession imSession = this.mResipImHandler.mSessions.get(Integer.valueOf(sessionHandle));
        if (imSession == null) {
            String str2 = this.LOG_TAG;
            Log.e(str2, "handleImMessageReceivedNotify(): Unknown session handle - " + sessionHandle);
            return;
        }
        ImIncomingMessageEvent parseImMessageParam = parseImMessageParam(messageParam);
        if (parseImMessageParam != null) {
            parseImMessageParam.mRawHandle = Integer.valueOf(sessionHandle);
            parseImMessageParam.mChatId = imSession.mChatId;
            String str3 = this.LOG_TAG;
            IMSLog.s(str3, "handleImMessageReceivedNotify(): " + parseImMessageParam);
            this.mResipImHandler.mIncomingMessageRegistrants.notifyRegistrants(new AsyncResult((Object) null, parseImMessageParam, (Throwable) null));
        }
    }

    /* JADX WARNING: Code restructure failed: missing block: B:20:0x0089, code lost:
        r2 = r2.replaceAll("\\<|\\>", "");
     */
    /* Code decompiled incorrectly, please refer to instructions dump. */
    private void handleImComposingStatusReceivedNotify(com.sec.internal.ims.core.handler.secims.imsCommonStruc.Notify r12) {
        /*
            r11 = this;
            java.lang.String r0 = r11.LOG_TAG
            java.lang.String r1 = "handleImComposingStatusReceivedNotify"
            android.util.Log.i(r0, r1)
            byte r0 = r12.notiType()
            r1 = 33
            if (r0 == r1) goto L_0x0017
            java.lang.String r11 = r11.LOG_TAG
            java.lang.String r12 = "handleImComposingStatusReceivedNotify(): invalid notify"
            android.util.Log.e(r11, r12)
            return
        L_0x0017:
            com.sec.internal.ims.core.handler.secims.imsCommonStruc.Notify_.ImComposingStatusReceived r0 = new com.sec.internal.ims.core.handler.secims.imsCommonStruc.Notify_.ImComposingStatusReceived
            r0.<init>()
            com.google.flatbuffers.Table r12 = r12.noti(r0)
            com.sec.internal.ims.core.handler.secims.imsCommonStruc.Notify_.ImComposingStatusReceived r12 = (com.sec.internal.ims.core.handler.secims.imsCommonStruc.Notify_.ImComposingStatusReceived) r12
            long r0 = r12.sessionId()
            int r0 = (int) r0
            com.sec.internal.ims.core.handler.secims.ResipImHandler r1 = r11.mResipImHandler
            java.util.Map<java.lang.Integer, com.sec.internal.ims.core.handler.secims.ResipImHandler$ImSession> r1 = r1.mSessions
            java.lang.Integer r0 = java.lang.Integer.valueOf(r0)
            java.lang.Object r0 = r1.get(r0)
            com.sec.internal.ims.core.handler.secims.ResipImHandler$ImSession r0 = (com.sec.internal.ims.core.handler.secims.ResipImHandler.ImSession) r0
            if (r0 != 0) goto L_0x0052
            java.lang.String r11 = r11.LOG_TAG
            java.lang.StringBuilder r0 = new java.lang.StringBuilder
            r0.<init>()
            java.lang.String r1 = "Unkown session id "
            r0.append(r1)
            long r1 = r12.sessionId()
            r0.append(r1)
            java.lang.String r12 = r0.toString()
            android.util.Log.e(r11, r12)
            return
        L_0x0052:
            com.sec.internal.ims.core.handler.secims.imsCommonStruc.ImComposingStatus r1 = r12.status()
            if (r1 != 0) goto L_0x0060
            java.lang.String r11 = r11.LOG_TAG
            java.lang.String r12 = "handleImComposingStatusReceivedNotify(): invalid notify data"
            android.util.Log.e(r11, r12)
            return
        L_0x0060:
            java.lang.String r2 = r12.uri()
            java.lang.String r3 = r12.userAlias()
            r4 = 0
            if (r3 == 0) goto L_0x0082
            java.lang.String r3 = r12.userAlias()
            boolean r3 = r3.isEmpty()
            if (r3 != 0) goto L_0x0082
            java.lang.String r3 = r11.LOG_TAG
            java.lang.String r5 = "handleImComposingStatusReceivedNotify: found userAlias"
            android.util.Log.i(r3, r5)
            java.lang.String r12 = r12.userAlias()
            r8 = r12
            goto L_0x0083
        L_0x0082:
            r8 = r4
        L_0x0083:
            boolean r12 = android.text.TextUtils.isEmpty(r2)
            if (r12 != 0) goto L_0x00a7
            java.lang.String r12 = "\\<|\\>"
            java.lang.String r3 = ""
            java.lang.String r2 = r2.replaceAll(r12, r3)
            com.sec.ims.util.ImsUri r12 = com.sec.ims.util.ImsUri.parse(r2)
            if (r12 == 0) goto L_0x00a7
            java.lang.String r3 = "tk"
            java.lang.String r5 = r12.getParam(r3)
            if (r5 == 0) goto L_0x00a7
            r12.removeParam(r3)
            java.lang.String r2 = r12.toString()
        L_0x00a7:
            r7 = r2
            com.sec.internal.constants.ims.servicemodules.im.event.ImComposingEvent r12 = new com.sec.internal.constants.ims.servicemodules.im.event.ImComposingEvent
            java.lang.String r6 = r0.mChatId
            boolean r9 = r1.isActive()
            long r0 = r1.interval()
            int r10 = (int) r0
            r5 = r12
            r5.<init>(r6, r7, r8, r9, r10)
            java.lang.String r0 = r11.LOG_TAG
            java.lang.StringBuilder r1 = new java.lang.StringBuilder
            r1.<init>()
            java.lang.String r2 = "handleImComposingStatusReceivedNotify: "
            r1.append(r2)
            r1.append(r12)
            java.lang.String r1 = r1.toString()
            com.sec.internal.log.IMSLog.s(r0, r1)
            com.sec.internal.ims.core.handler.secims.ResipImHandler r11 = r11.mResipImHandler
            com.sec.internal.helper.RegistrantList r11 = r11.mComposingRegistrants
            com.sec.internal.helper.AsyncResult r0 = new com.sec.internal.helper.AsyncResult
            r0.<init>(r4, r12, r4)
            r11.notifyRegistrants(r0)
            return
        */
        throw new UnsupportedOperationException("Method not decompiled: com.sec.internal.ims.core.handler.secims.ResipImResponseHandler.handleImComposingStatusReceivedNotify(com.sec.internal.ims.core.handler.secims.imsCommonStruc.Notify):void");
    }

    private void handleFtSessionStartedNotify(Notify notify) {
        if (notify.notiType() != 28) {
            Log.e(this.LOG_TAG, "handleFtSessionStartedNotify(): invalid notify");
            return;
        }
        SessionStarted sessionStarted = (SessionStarted) notify.noti(new SessionStarted());
        int sessionHandle = (int) sessionStarted.sessionHandle();
        Result translateFtResult = ResipTranslatorCollection.translateFtResult(sessionStarted.imError(), sessionStarted.warningHdr());
        String str = this.LOG_TAG;
        IMSLog.s(str, "handleFtSessionStartedNotify(): sessionHandle = " + sessionHandle + ", error = " + translateFtResult);
        ResipImHandler.FtSession ftSession = this.mResipImHandler.mFtSessions.get(Integer.valueOf(sessionHandle));
        if (ftSession == null) {
            String str2 = this.LOG_TAG;
            Log.e(str2, "handleFtSessionStartedNotify(): Unknown session handle: " + sessionStarted.sessionHandle());
        } else if (translateFtResult.getImError() == ImError.SUCCESS) {
            Log.i(this.LOG_TAG, "handleFtSessionStartedNotify(): SUCCESS");
        } else {
            if (ftSession.mStartCallback != null) {
                RetryHdr retryHdr = sessionStarted.retryHdr();
                this.mResipImHandler.sendCallback(ftSession.mStartCallback, new FtResult(translateFtResult, (Object) Integer.valueOf(sessionHandle), retryHdr != null ? retryHdr.retryTimer() : 0));
                ftSession.mStartCallback = null;
            }
            this.mResipImHandler.mFtSessions.remove(Integer.valueOf(sessionHandle));
        }
    }

    private void handleFtProgressNotify(Notify notify) {
        if (notify.notiType() != 41) {
            Log.e(this.LOG_TAG, "handleFtProgressNotify(): invalid notify");
            return;
        }
        FtProgress ftProgress = (FtProgress) notify.noti(new FtProgress());
        int sessionHandle = (int) ftProgress.sessionHandle();
        ResipImHandler.FtSession ftSession = this.mResipImHandler.mFtSessions.get(Integer.valueOf(sessionHandle));
        if (ftSession == null) {
            String str = this.LOG_TAG;
            Log.e(str, "Unkown session id " + ftProgress.sessionHandle());
            return;
        }
        FtTransferProgressEvent.State translateFtProgressState = ResipTranslatorCollection.translateFtProgressState((int) ftProgress.state());
        if (translateFtProgressState != FtTransferProgressEvent.State.TRANSFERRING) {
            this.mResipImHandler.mFtSessions.remove(Integer.valueOf(sessionHandle));
        }
        Result translateFtResult = ResipTranslatorCollection.translateFtResult(ftProgress.imError(), ftProgress.reasonHdr());
        String str2 = this.LOG_TAG;
        IMSLog.s(str2, "handleFtProgressNotify(): session handle = " + sessionHandle + ", state = " + ftProgress.state() + ", fail reason = " + translateFtResult + ", total = " + ftProgress.total() + ", transferred = " + ftProgress.transferred());
        if (this.mResipImHandler.mTransferProgressRegistrants.size() != 0) {
            this.mResipImHandler.mTransferProgressRegistrants.notifyRegistrants(new AsyncResult((Object) null, new FtTransferProgressEvent(Integer.valueOf(sessionHandle), ftSession.mId, ftProgress.total(), ftProgress.transferred(), translateFtProgressState, translateFtResult), (Throwable) null));
            return;
        }
        String str3 = this.LOG_TAG;
        Log.e(str3, "No TransferProgressRegistrant for handle = " + ftSession.mHandle);
    }

    private void handleFtIncomingSessionNotify(Notify notify) {
        if (notify.notiType() != 42) {
            Log.e(this.LOG_TAG, "handleFtIncomingSessionNotify(): invalid notify");
            return;
        }
        FtIncomingSession ftIncomingSession = (FtIncomingSession) notify.noti(new FtIncomingSession());
        BaseSessionData session = ftIncomingSession.session();
        FtPayloadParam payload = ftIncomingSession.payload();
        if (payload == null || session == null) {
            Log.e(this.LOG_TAG, "handleFtIncomingSessionNotify(): invalid notify data");
            return;
        }
        int sessionHandle = (int) session.sessionHandle();
        Log.i(this.LOG_TAG, "handleFtIncomingSessionNotify(): session handle = " + sessionHandle);
        UserAgent userAgent = this.mResipImHandler.getUserAgent((int) ftIncomingSession.userHandle());
        if (userAgent == null) {
            Log.e(this.LOG_TAG, "handleFtIncomingSessionNotify(): UserAgent not found. UserHandle = " + ((int) ftIncomingSession.userHandle()));
            return;
        }
        ResipImHandler.FtSession ftSession = new ResipImHandler.FtSession();
        ftSession.mHandle = sessionHandle;
        ftSession.mUaHandle = (int) ftIncomingSession.userHandle();
        this.mResipImHandler.mFtSessions.put(Integer.valueOf(sessionHandle), ftSession);
        FtIncomingSessionEvent ftIncomingSessionEvent = new FtIncomingSessionEvent();
        ftIncomingSessionEvent.mRawHandle = Integer.valueOf(sessionHandle);
        ftIncomingSessionEvent.mIsSlmSvcMsg = false;
        ftIncomingSessionEvent.mOwnImsi = this.mResipImHandler.getImsiByUserAgent(userAgent);
        ftIncomingSessionEvent.mSenderUri = ImsUri.parse(session.sessionUri());
        ftIncomingSessionEvent.mParticipants = new ArrayList();
        if (session.isConference()) {
            ftIncomingSessionEvent.mParticipants.add(ftIncomingSessionEvent.mSenderUri);
        } else {
            for (int i = 0; i < session.receiversLength(); i++) {
                ftIncomingSessionEvent.mParticipants.add(ImsUri.parse(session.receivers(i)));
            }
        }
        ftIncomingSessionEvent.mUserAlias = session.userAlias();
        ftIncomingSessionEvent.mSdpContentType = session.sdpContentType();
        ftIncomingSessionEvent.mIsConference = session.isConference();
        boolean silenceSupported = payload.silenceSupported();
        ftIncomingSessionEvent.mIsRoutingMsg = silenceSupported;
        if (silenceSupported) {
            Log.i(this.LOG_TAG, "handleFtIncomingSessionNotify -> routing message");
            ftIncomingSessionEvent.mRequestUri = ImsUri.parse(payload.requestUri());
            ftIncomingSessionEvent.mPAssertedId = ImsUri.parse(payload.pAssertedId());
            ftIncomingSessionEvent.mReceiver = ImsUri.parse(payload.receiver());
        }
        ImFileAttr fileAttr = payload.fileAttr();
        if (fileAttr != null) {
            ftIncomingSessionEvent.mContentType = fileAttr.contentType();
            ftIncomingSessionEvent.mFileName = (String) Optional.ofNullable(fileAttr.name()).map(new ResipImResponseHandler$$ExternalSyntheticLambda0()).orElse((Object) null);
            ftIncomingSessionEvent.mFilePath = fileAttr.path();
            ftIncomingSessionEvent.mFileSize = (long) ((int) fileAttr.size());
            ftIncomingSessionEvent.mStart = (int) fileAttr.start();
            ftIncomingSessionEvent.mEnd = (int) fileAttr.end();
            ftIncomingSessionEvent.mTimeDuration = (int) fileAttr.timeDuration();
        }
        ImFileAttr iconAttr = payload.iconAttr();
        if (iconAttr == null || iconAttr.path() == null || iconAttr.path().isEmpty()) {
            ftIncomingSessionEvent.mThumbPath = null;
        } else {
            ftIncomingSessionEvent.mThumbPath = iconAttr.path();
        }
        ftIncomingSessionEvent.mContributionId = session.contributionId();
        if (session.conversationId() != null) {
            ftIncomingSessionEvent.mConversationId = session.conversationId();
        }
        if (session.inReplyToContributionId() != null) {
            ftIncomingSessionEvent.mInReplyToConversationId = session.inReplyToContributionId();
        }
        ftIncomingSessionEvent.mFileTransferId = session.id();
        ftIncomingSessionEvent.mPush = payload.isPush();
        if (payload.imdn() != null) {
            ftIncomingSessionEvent.mImdnId = payload.imdn().messageId();
            try {
                String datetime = payload.imdn().datetime();
                ftIncomingSessionEvent.mImdnTime = !TextUtils.isEmpty(datetime) ? Iso8601.parse(datetime) : new Date();
            } catch (ParseException e) {
                e.printStackTrace();
                ftIncomingSessionEvent.mImdnTime = new Date();
            }
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
        IMSLog.s(this.LOG_TAG, "handleFtIncomingSessionNotify(): " + ftIncomingSessionEvent);
        if (this.mResipImHandler.mIncomingFileTransferRegistrants.size() != 0) {
            this.mResipImHandler.mIncomingFileTransferRegistrants.notifyRegistrants(new AsyncResult((Object) null, ftIncomingSessionEvent, (Throwable) null));
            return;
        }
        Log.i(this.LOG_TAG, "Empty registrants, reject handle=" + sessionHandle);
        this.mResipImHandler.handleRejectFtSessionRequest(new RejectFtSessionParams(Integer.valueOf(sessionHandle), (Message) null, FtRejectReason.FORBIDDEN_SERVICE_NOT_AUTHORIZED, ftIncomingSessionEvent.mFileTransferId));
    }

    /* access modifiers changed from: private */
    public static /* synthetic */ String lambda$handleFtIncomingSessionNotify$0(String str) {
        try {
            return URLDecoder.decode(str, "UTF-8");
        } catch (UnsupportedEncodingException | IllegalArgumentException e) {
            e.printStackTrace();
            return str;
        }
    }

    private void handleImSessionEstablishedNotify(Notify notify) {
        if (notify.notiType() != 30) {
            Log.e(this.LOG_TAG, "handleImSessionEstablishedNotify(): invalid notify");
            return;
        }
        SessionEstablished sessionEstablished = (SessionEstablished) notify.noti(new SessionEstablished());
        int sessionHandle = (int) sessionEstablished.sessionHandle();
        ImError imError = ResipTranslatorCollection.translateImResult(sessionEstablished.imError(), (Object) null).getImError();
        ResipImHandler.ImSession imSession = this.mResipImHandler.mSessions.get(Integer.valueOf(sessionHandle));
        if (imSession == null) {
            Log.e(this.LOG_TAG, "handleImSessionEstablishedNotify(): no session found sessionHandle = " + sessionHandle + ", error = " + imError);
            return;
        }
        IMSLog.s(this.LOG_TAG, "handleImSessionEstablishedNotify(): sessionHandle = " + sessionHandle + ", chat id = " + imSession.mChatId + ", error = " + imError);
        if (imError != ImError.SUCCESS) {
            Log.e(this.LOG_TAG, "handleImSessionEstablishedNotify(): failed");
            return;
        }
        ArrayList arrayList = new ArrayList();
        for (int i = 0; i < sessionEstablished.acceptContentLength(); i++) {
            String acceptContent = sessionEstablished.acceptContent(i);
            if (acceptContent != null) {
                arrayList.addAll(Arrays.asList(acceptContent.split(" ")));
            }
        }
        ArrayList arrayList2 = new ArrayList();
        for (int i2 = 0; i2 < sessionEstablished.acceptWrappedContentLength(); i2++) {
            String acceptWrappedContent = sessionEstablished.acceptWrappedContent(i2);
            if (acceptWrappedContent != null) {
                arrayList2.addAll(Arrays.asList(acceptWrappedContent.split(" ")));
            }
        }
        ArrayList<String> arrayList3 = new ArrayList<>();
        arrayList3.addAll(arrayList);
        arrayList3.addAll(arrayList2);
        IMSLog.s(this.LOG_TAG, "handleStartImMediaResponse(): acceptContent = " + arrayList3);
        EnumSet<E> noneOf = EnumSet.noneOf(SupportedFeature.class);
        for (String translateAcceptContent : arrayList3) {
            SupportedFeature translateAcceptContent2 = ResipTranslatorCollection.translateAcceptContent(translateAcceptContent);
            if (translateAcceptContent2 != null) {
                noneOf.add(translateAcceptContent2);
            }
        }
        imSessionEstablished(sessionHandle, (String) null, noneOf, arrayList, arrayList2);
    }

    private void imSessionEstablished(int i, String str, EnumSet<SupportedFeature> enumSet, List<String> list, List<String> list2) {
        ImsUri imsUri;
        IMSLog.s(this.LOG_TAG, "imSessionEstablished(): sessionHandle = " + i + ", session uri = " + str + ", features = " + enumSet);
        ResipImHandler.ImSession imSession = this.mResipImHandler.mSessions.get(Integer.valueOf(i));
        if (imSession != null) {
            String str2 = imSession.mChatId;
            IMSLog.s(this.LOG_TAG, "imSessionEstablished(): chatid = " + str2);
            if (str2 == null) {
                Log.e(this.LOG_TAG, "imSessionEstablished(): Failed to find chat id.");
                return;
            }
            if (str == null) {
                imsUri = null;
            } else {
                imsUri = ImsUri.parse(str);
            }
            this.mResipImHandler.mSessionEstablishedRegistrants.notifyRegistrants(new AsyncResult((Object) null, new ImSessionEstablishedEvent(Integer.valueOf(i), str2, imsUri, enumSet, list, list2), (Throwable) null));
        }
    }

    private void handleFtSessionEstablishedNotify(Notify notify) {
        if (notify.notiType() != 30) {
            Log.e(this.LOG_TAG, "handleFtSessionEstablishedNotify(): invalid notify");
            return;
        }
        SessionEstablished sessionEstablished = (SessionEstablished) notify.noti(new SessionEstablished());
        int sessionHandle = (int) sessionEstablished.sessionHandle();
        Result translateFtResult = ResipTranslatorCollection.translateFtResult(sessionEstablished.imError(), (Object) null);
        ResipImHandler.FtSession ftSession = this.mResipImHandler.mFtSessions.get(Integer.valueOf(sessionHandle));
        if (ftSession == null) {
            String str = this.LOG_TAG;
            Log.e(str, "handleFtSessionEstablishedNotify(): no session found sessionHandle = " + sessionHandle + ", result = " + translateFtResult);
            return;
        }
        String str2 = this.LOG_TAG;
        IMSLog.s(str2, "handleFtSessionEstablishedNotify(): sessionHandle = " + sessionHandle + ", result = " + translateFtResult);
        if (translateFtResult.getImError() != ImError.SUCCESS) {
            Log.e(this.LOG_TAG, "handleFtSessionEstablishedNotify(): failed");
            return;
        }
        Message message = ftSession.mStartCallback;
        if (message != null) {
            this.mResipImHandler.sendCallback(message, new FtResult(translateFtResult, Integer.valueOf(sessionHandle)));
            ftSession.mStartCallback = null;
            return;
        }
        Message message2 = ftSession.mAcceptCallback;
        if (message2 != null) {
            this.mResipImHandler.sendCallback(message2, new FtResult(translateFtResult, Integer.valueOf(sessionHandle)));
            ftSession.mAcceptCallback = null;
        }
    }

    private void handleFtSessionClosedNotify(Notify notify) {
        if (notify.notiType() != 29) {
            Log.e(this.LOG_TAG, "handleFtSessionClosedNotify(): invalid notify");
            return;
        }
        SessionClosed sessionClosed = (SessionClosed) notify.noti(new SessionClosed());
        int sessionHandle = (int) sessionClosed.sessionHandle();
        Result translateFtResult = ResipTranslatorCollection.translateFtResult(sessionClosed.imError(), (Object) null);
        ResipImHandler.FtSession ftSession = this.mResipImHandler.mFtSessions.get(Integer.valueOf(sessionHandle));
        if (ftSession == null) {
            String str = this.LOG_TAG;
            Log.e(str, "handleFtSessionClosedNotify(): no session found sessionHandle = " + sessionHandle + ", error = " + translateFtResult);
            return;
        }
        String str2 = this.LOG_TAG;
        IMSLog.s(str2, "handleFtSessionClosedNotify(): sessionHandle = " + sessionHandle + ", error = " + translateFtResult);
        if (translateFtResult.getImError() != ImError.SUCCESS) {
            Log.e(this.LOG_TAG, "handleFtSessionClosedNotify(): abnormal close");
            Message message = ftSession.mStartCallback;
            if (message != null) {
                this.mResipImHandler.sendCallback(message, new FtResult(translateFtResult, Integer.valueOf(sessionHandle)));
                ftSession.mStartCallback = null;
            }
            Message message2 = ftSession.mAcceptCallback;
            if (message2 != null) {
                this.mResipImHandler.sendCallback(message2, new FtResult(translateFtResult, Integer.valueOf(sessionHandle)));
                ftSession.mAcceptCallback = null;
            } else if (this.mResipImHandler.mTransferProgressRegistrants.size() != 0) {
                IMSLog.s(this.LOG_TAG, "handleFtSessionClosedNotify(): post cancelled to progress registrants");
                this.mResipImHandler.mTransferProgressRegistrants.notifyRegistrants(new AsyncResult((Object) null, new FtTransferProgressEvent(Integer.valueOf(sessionHandle), ftSession.mId, -1, -1, FtTransferProgressEvent.State.CANCELED, translateFtResult), (Throwable) null));
            } else {
                String str3 = this.LOG_TAG;
                Log.e(str3, "No TransferProgressRegistrant for handle = " + ftSession.mHandle);
            }
        } else {
            IMSLog.s(this.LOG_TAG, "handleFtSessionClosedNotify(): get unexpected SessionClosed notify");
        }
        this.mResipImHandler.mFtSessions.remove(Integer.valueOf(sessionHandle));
    }

    /* JADX WARNING: Removed duplicated region for block: B:112:0x028b  */
    /* JADX WARNING: Removed duplicated region for block: B:113:0x0294  */
    /* JADX WARNING: Removed duplicated region for block: B:115:0x0297  */
    /* JADX WARNING: Removed duplicated region for block: B:125:0x02e0  */
    /* JADX WARNING: Removed duplicated region for block: B:128:0x02ec  */
    /* JADX WARNING: Removed duplicated region for block: B:142:0x0334  */
    /* JADX WARNING: Removed duplicated region for block: B:144:0x033c  */
    /* JADX WARNING: Removed duplicated region for block: B:49:0x0136  */
    /* JADX WARNING: Removed duplicated region for block: B:59:0x0166  */
    /* JADX WARNING: Removed duplicated region for block: B:60:0x016d  */
    /* JADX WARNING: Removed duplicated region for block: B:63:0x0179 A[SYNTHETIC, Splitter:B:63:0x0179] */
    /* JADX WARNING: Removed duplicated region for block: B:68:0x019c  */
    /* JADX WARNING: Removed duplicated region for block: B:70:0x01ab  */
    /* JADX WARNING: Removed duplicated region for block: B:73:0x01ce  */
    /* Code decompiled incorrectly, please refer to instructions dump. */
    private void handleImConferenceInfoUpdateNotify(com.sec.internal.ims.core.handler.secims.imsCommonStruc.Notify r23) {
        /*
            r22 = this;
            r1 = r22
            java.lang.String r0 = r1.LOG_TAG
            java.lang.String r2 = "handleImConferenceInfoUpdateNotify"
            android.util.Log.i(r0, r2)
            byte r0 = r23.notiType()
            r2 = 72
            if (r0 == r2) goto L_0x0019
            java.lang.String r0 = r1.LOG_TAG
            java.lang.String r1 = "handleImConferenceInfoUpdateNotify(): invalid notify"
            android.util.Log.e(r0, r1)
            return
        L_0x0019:
            com.sec.internal.ims.core.handler.secims.imsCommonStruc.Notify_.ImConfInfoUpdated r0 = new com.sec.internal.ims.core.handler.secims.imsCommonStruc.Notify_.ImConfInfoUpdated
            r0.<init>()
            r2 = r23
            com.google.flatbuffers.Table r0 = r2.noti(r0)
            r2 = r0
            com.sec.internal.ims.core.handler.secims.imsCommonStruc.Notify_.ImConfInfoUpdated r2 = (com.sec.internal.ims.core.handler.secims.imsCommonStruc.Notify_.ImConfInfoUpdated) r2
            long r3 = r2.sessionId()
            int r0 = (int) r3
            com.sec.internal.ims.core.handler.secims.ResipImHandler r3 = r1.mResipImHandler
            java.util.Map<java.lang.Integer, com.sec.internal.ims.core.handler.secims.ResipImHandler$ImSession> r3 = r3.mSessions
            java.lang.Integer r4 = java.lang.Integer.valueOf(r0)
            java.lang.Object r3 = r3.get(r4)
            com.sec.internal.ims.core.handler.secims.ResipImHandler$ImSession r3 = (com.sec.internal.ims.core.handler.secims.ResipImHandler.ImSession) r3
            if (r3 == 0) goto L_0x0367
            java.lang.String r4 = r3.mChatId
            if (r4 != 0) goto L_0x0042
            goto L_0x0367
        L_0x0042:
            java.lang.String r14 = r2.timestamp()
            com.sec.internal.ims.core.handler.secims.ResipImHandler r0 = r1.mResipImHandler
            int r4 = r3.mUaHandle
            com.sec.internal.ims.core.handler.secims.UserAgent r4 = r0.getUserAgent((int) r4)
            if (r4 != 0) goto L_0x0058
            java.lang.String r0 = r1.LOG_TAG
            java.lang.String r1 = "handleImConferenceInfoUpdateNotify(): User Agent not found!"
            android.util.Log.e(r0, r1)
            return
        L_0x0058:
            com.sec.internal.constants.ims.servicemodules.im.event.ImSessionConferenceInfoUpdateEvent$ImConferenceInfoType r0 = com.sec.internal.constants.ims.servicemodules.im.event.ImSessionConferenceInfoUpdateEvent.ImConferenceInfoType.FULL
            java.lang.String r5 = r2.state()
            boolean r6 = android.text.TextUtils.isEmpty(r5)
            if (r6 != 0) goto L_0x007d
            r5.hashCode()
            java.lang.String r6 = "partial"
            boolean r6 = r5.equals(r6)
            if (r6 != 0) goto L_0x007b
            java.lang.String r6 = "deleted"
            boolean r5 = r5.equals(r6)
            if (r5 != 0) goto L_0x0078
            goto L_0x007d
        L_0x0078:
            com.sec.internal.constants.ims.servicemodules.im.event.ImSessionConferenceInfoUpdateEvent$ImConferenceInfoType r0 = com.sec.internal.constants.ims.servicemodules.im.event.ImSessionConferenceInfoUpdateEvent.ImConferenceInfoType.DELETED
            goto L_0x007d
        L_0x007b:
            com.sec.internal.constants.ims.servicemodules.im.event.ImSessionConferenceInfoUpdateEvent$ImConferenceInfoType r0 = com.sec.internal.constants.ims.servicemodules.im.event.ImSessionConferenceInfoUpdateEvent.ImConferenceInfoType.PARTIAL
        L_0x007d:
            r7 = r0
            com.sec.internal.ims.core.handler.secims.imsCommonStruc.Notify_.ImConfInfoUpdated_.SubjectExt r0 = r2.subjectData()
            java.lang.String r5 = ""
            if (r0 == 0) goto L_0x0129
            java.lang.String r6 = r1.LOG_TAG
            java.lang.StringBuilder r8 = new java.lang.StringBuilder
            r8.<init>()
            java.lang.String r9 = "Received sub info: subject ="
            r8.append(r9)
            java.lang.String r9 = r0.subject()
            r8.append(r9)
            java.lang.String r9 = ", participant ="
            r8.append(r9)
            java.lang.String r9 = r0.participant()
            r8.append(r9)
            java.lang.String r9 = ", timestamp ="
            r8.append(r9)
            java.lang.String r9 = r0.timestamp()
            r8.append(r9)
            java.lang.String r8 = r8.toString()
            android.util.Log.i(r6, r8)
            java.lang.String r6 = r0.subject()
            boolean r6 = android.text.TextUtils.isEmpty(r6)
            if (r6 == 0) goto L_0x00d6
            java.lang.String r6 = r0.participant()
            boolean r6 = android.text.TextUtils.isEmpty(r6)
            if (r6 == 0) goto L_0x00d6
            java.lang.String r6 = r0.timestamp()
            boolean r6 = android.text.TextUtils.isEmpty(r6)
            if (r6 != 0) goto L_0x0129
        L_0x00d6:
            java.lang.String r6 = r0.subject()
            boolean r6 = android.text.TextUtils.isEmpty(r6)
            if (r6 == 0) goto L_0x00e2
            r6 = r5
            goto L_0x00e6
        L_0x00e2:
            java.lang.String r6 = r0.subject()
        L_0x00e6:
            java.lang.String r8 = r0.participant()
            boolean r9 = android.text.TextUtils.isEmpty(r8)
            if (r9 != 0) goto L_0x00f5
            com.sec.ims.util.ImsUri r8 = com.sec.ims.util.ImsUri.parse(r8)
            goto L_0x00f6
        L_0x00f5:
            r8 = 0
        L_0x00f6:
            java.lang.String r0 = r0.timestamp()
            boolean r9 = android.text.TextUtils.isEmpty(r0)
            if (r9 != 0) goto L_0x0121
            java.util.Date r0 = com.sec.internal.helper.Iso8601.parse(r0)     // Catch:{ ParseException -> 0x0105 }
            goto L_0x0122
        L_0x0105:
            java.lang.String r9 = r1.LOG_TAG
            java.lang.StringBuilder r10 = new java.lang.StringBuilder
            r10.<init>()
            java.lang.String r11 = "parsing subject timestamp failed : "
            r10.append(r11)
            r10.append(r0)
            java.lang.String r0 = r10.toString()
            android.util.Log.i(r9, r0)
            java.util.Date r0 = new java.util.Date
            r0.<init>()
            goto L_0x0122
        L_0x0121:
            r0 = 0
        L_0x0122:
            com.sec.internal.constants.ims.servicemodules.im.ImSubjectData r9 = new com.sec.internal.constants.ims.servicemodules.im.ImSubjectData
            r9.<init>(r6, r8, r0)
            r10 = r9
            goto L_0x012a
        L_0x0129:
            r10 = 0
        L_0x012a:
            com.sec.internal.ims.core.handler.secims.imsCommonStruc.Notify_.ImConfInfoUpdated_.Icon r0 = r2.iconData()
            if (r0 == 0) goto L_0x01ab
            int r6 = r0.icontype()
            if (r6 == 0) goto L_0x01ab
            com.sec.internal.constants.ims.servicemodules.im.ImIconData$IconType r6 = com.sec.internal.constants.ims.servicemodules.im.ImIconData.IconType.ICON_TYPE_NONE
            int r8 = r0.icontype()
            com.sec.internal.constants.ims.servicemodules.im.ImIconData$IconType r9 = com.sec.internal.constants.ims.servicemodules.im.ImIconData.IconType.ICON_TYPE_FILE
            int r11 = r9.ordinal()
            if (r8 != r11) goto L_0x0147
            r17 = r9
            goto L_0x015a
        L_0x0147:
            com.sec.internal.constants.ims.servicemodules.im.ImIconData$IconType r9 = com.sec.internal.constants.ims.servicemodules.im.ImIconData.IconType.ICON_TYPE_URI
            int r11 = r9.ordinal()
            if (r8 != r11) goto L_0x0158
            java.lang.String r6 = r0.iconLocation()
            r21 = r6
            r17 = r9
            goto L_0x015c
        L_0x0158:
            r17 = r6
        L_0x015a:
            r21 = 0
        L_0x015c:
            java.lang.String r6 = r0.participant()
            boolean r8 = android.text.TextUtils.isEmpty(r6)
            if (r8 != 0) goto L_0x016d
            com.sec.ims.util.ImsUri r6 = com.sec.ims.util.ImsUri.parse(r6)
            r18 = r6
            goto L_0x016f
        L_0x016d:
            r18 = 0
        L_0x016f:
            java.lang.String r6 = r0.timestamp()
            boolean r8 = android.text.TextUtils.isEmpty(r6)
            if (r8 != 0) goto L_0x019c
            java.util.Date r6 = com.sec.internal.helper.Iso8601.parse(r6)     // Catch:{ ParseException -> 0x0180 }
        L_0x017d:
            r19 = r6
            goto L_0x019e
        L_0x0180:
            java.lang.String r8 = r1.LOG_TAG
            java.lang.StringBuilder r9 = new java.lang.StringBuilder
            r9.<init>()
            java.lang.String r11 = "parsing icon timestamp failed : "
            r9.append(r11)
            r9.append(r6)
            java.lang.String r6 = r9.toString()
            android.util.Log.i(r8, r6)
            java.util.Date r6 = new java.util.Date
            r6.<init>()
            goto L_0x017d
        L_0x019c:
            r19 = 0
        L_0x019e:
            com.sec.internal.constants.ims.servicemodules.im.ImIconData r6 = new com.sec.internal.constants.ims.servicemodules.im.ImIconData
            java.lang.String r20 = r0.iconLocation()
            r16 = r6
            r16.<init>(r17, r18, r19, r20, r21)
            r13 = r6
            goto L_0x01ac
        L_0x01ab:
            r13 = 0
        L_0x01ac:
            java.util.ArrayList r8 = new java.util.ArrayList
            r8.<init>()
            int r6 = r2.usersLength()
            java.lang.String r0 = r1.LOG_TAG
            java.lang.StringBuilder r9 = new java.lang.StringBuilder
            r9.<init>()
            java.lang.String r11 = "handleOptionsReceived: tagLength "
            r9.append(r11)
            r9.append(r6)
            java.lang.String r9 = r9.toString()
            android.util.Log.i(r0, r9)
            r11 = 0
        L_0x01cc:
            if (r11 >= r6) goto L_0x031c
            com.sec.internal.ims.core.handler.secims.imsCommonStruc.ImConfUser r12 = r2.users(r11)
            if (r12 == 0) goto L_0x030c
            com.sec.internal.constants.ims.servicemodules.im.ImConferenceParticipantInfo r9 = new com.sec.internal.constants.ims.servicemodules.im.ImConferenceParticipantInfo
            r9.<init>()
            java.lang.String r0 = r12.entity()
            com.sec.ims.util.ImsUri r0 = com.sec.ims.util.ImsUri.parse(r0)
            r9.mUri = r0
            java.lang.String r0 = r12.state()
            com.sec.internal.constants.ims.servicemodules.im.ImConferenceParticipantInfo$ImConferenceUserElemState r0 = com.sec.internal.ims.translate.ResipTranslatorCollection.translateImConferenceUserElemState(r0)
            r9.mUserElemState = r0
            com.sec.internal.ims.core.handler.secims.imsCommonStruc.ImConfUserEndpoint r0 = r12.endpoint()
            if (r0 == 0) goto L_0x020a
            com.sec.internal.ims.core.handler.secims.imsCommonStruc.ImConfUserEndpoint r0 = r12.endpoint()
            java.lang.String r0 = r0.status()
            if (r0 == 0) goto L_0x020a
            com.sec.internal.ims.core.handler.secims.imsCommonStruc.ImConfUserEndpoint r0 = r12.endpoint()
            java.lang.String r0 = r0.status()
            com.sec.internal.constants.ims.servicemodules.im.ImConferenceParticipantInfo$ImConferenceParticipantStatus r0 = com.sec.internal.ims.translate.ResipTranslatorCollection.translateToImConferenceParticipantStatus(r0)
            goto L_0x020c
        L_0x020a:
            com.sec.internal.constants.ims.servicemodules.im.ImConferenceParticipantInfo$ImConferenceParticipantStatus r0 = com.sec.internal.constants.ims.servicemodules.im.ImConferenceParticipantInfo.ImConferenceParticipantStatus.INVALID
        L_0x020c:
            r9.mParticipantStatus = r0
            boolean r0 = r12.yourOwn()
            r9.mIsOwn = r0
            com.sec.internal.ims.core.handler.secims.imsCommonStruc.ImConfUserEndpoint r0 = r12.endpoint()
            if (r0 == 0) goto L_0x0223
            com.sec.internal.ims.core.handler.secims.imsCommonStruc.ImConfUserEndpoint r0 = r12.endpoint()
            java.lang.String r0 = r0.disconnectMethod()
            goto L_0x0224
        L_0x0223:
            r0 = r5
        L_0x0224:
            boolean r16 = android.text.TextUtils.isEmpty(r0)
            if (r16 != 0) goto L_0x0230
            com.sec.internal.constants.ims.servicemodules.im.ImConferenceParticipantInfo$ImConferenceDisconnectionReason r0 = com.sec.internal.ims.translate.ResipTranslatorCollection.translateToImConferenceDisconnectionReason(r0)
            r9.mDisconnectionReason = r0
        L_0x0230:
            com.sec.internal.ims.core.handler.secims.imsCommonStruc.ImConfUserEndpoint r0 = r12.endpoint()
            if (r0 == 0) goto L_0x023f
            com.sec.internal.ims.core.handler.secims.imsCommonStruc.ImConfUserEndpoint r0 = r12.endpoint()
            com.sec.internal.ims.core.handler.secims.imsCommonStruc.ImConfUserJoiningInfo r0 = r0.joininginfo()
            goto L_0x0240
        L_0x023f:
            r0 = 0
        L_0x0240:
            if (r0 == 0) goto L_0x0281
            java.lang.String r0 = r0.when()
            boolean r16 = android.text.TextUtils.isEmpty(r0)
            if (r16 != 0) goto L_0x0281
            java.lang.String r15 = r1.LOG_TAG     // Catch:{ ParseException -> 0x0271 }
            r17 = r5
            java.lang.StringBuilder r5 = new java.lang.StringBuilder     // Catch:{ ParseException -> 0x026f }
            r5.<init>()     // Catch:{ ParseException -> 0x026f }
            r18 = r6
            java.lang.String r6 = "parsing joiningInfo timestamp failed : "
            r5.append(r6)     // Catch:{ ParseException -> 0x026d }
            r5.append(r0)     // Catch:{ ParseException -> 0x026d }
            java.lang.String r5 = r5.toString()     // Catch:{ ParseException -> 0x026d }
            android.util.Log.i(r15, r5)     // Catch:{ ParseException -> 0x026d }
            java.util.Date r0 = com.sec.internal.helper.Iso8601.parse(r0)     // Catch:{ ParseException -> 0x026d }
            r9.mJoiningTime = r0     // Catch:{ ParseException -> 0x026d }
            goto L_0x0285
        L_0x026d:
            r0 = move-exception
            goto L_0x0276
        L_0x026f:
            r0 = move-exception
            goto L_0x0274
        L_0x0271:
            r0 = move-exception
            r17 = r5
        L_0x0274:
            r18 = r6
        L_0x0276:
            r0.printStackTrace()
            java.util.Date r0 = new java.util.Date
            r0.<init>()
            r9.mJoiningTime = r0
            goto L_0x0285
        L_0x0281:
            r17 = r5
            r18 = r6
        L_0x0285:
            com.sec.internal.ims.core.handler.secims.imsCommonStruc.ImConfUserEndpoint r0 = r12.endpoint()
            if (r0 == 0) goto L_0x0294
            com.sec.internal.ims.core.handler.secims.imsCommonStruc.ImConfUserEndpoint r0 = r12.endpoint()
            com.sec.internal.ims.core.handler.secims.imsCommonStruc.ImConfUserDisconnectionInfo r0 = r0.disconnectioninfo()
            goto L_0x0295
        L_0x0294:
            r0 = 0
        L_0x0295:
            if (r0 == 0) goto L_0x02e0
            java.lang.String r5 = r0.when()
            boolean r6 = android.text.TextUtils.isEmpty(r5)
            if (r6 != 0) goto L_0x02c8
            java.util.Date r6 = com.sec.internal.helper.Iso8601.parse(r5)     // Catch:{ ParseException -> 0x02a8 }
            r9.mDisconnectionTime = r6     // Catch:{ ParseException -> 0x02a8 }
            goto L_0x02c8
        L_0x02a8:
            java.lang.String r6 = r1.LOG_TAG
            java.lang.StringBuilder r15 = new java.lang.StringBuilder
            r15.<init>()
            r19 = r14
            java.lang.String r14 = "parsing disconnectionInfo timestamp failed : "
            r15.append(r14)
            r15.append(r5)
            java.lang.String r5 = r15.toString()
            android.util.Log.i(r6, r5)
            java.util.Date r5 = new java.util.Date
            r5.<init>()
            r9.mDisconnectionTime = r5
            goto L_0x02ca
        L_0x02c8:
            r19 = r14
        L_0x02ca:
            java.lang.String r0 = r0.reason()
            boolean r5 = android.text.TextUtils.isEmpty(r0)
            if (r5 != 0) goto L_0x02e2
            int r0 = r1.parseReasonHeader(r0)
            r5 = 0
            com.sec.internal.constants.ims.servicemodules.im.ImError r0 = com.sec.internal.ims.translate.ResipTranslatorCollection.translateSIPError(r0, r5)
            r9.mDisconnectionCause = r0
            goto L_0x02e2
        L_0x02e0:
            r19 = r14
        L_0x02e2:
            java.lang.String r0 = r12.roles()
            boolean r5 = android.text.TextUtils.isEmpty(r0)
            if (r5 != 0) goto L_0x0302
            java.lang.String r5 = "chairman"
            boolean r5 = r5.equalsIgnoreCase(r0)
            if (r5 != 0) goto L_0x02ff
            java.lang.String r5 = "Administrator"
            boolean r0 = r5.equalsIgnoreCase(r0)
            if (r0 == 0) goto L_0x02fd
            goto L_0x02ff
        L_0x02fd:
            r0 = 0
            goto L_0x0300
        L_0x02ff:
            r0 = 1
        L_0x0300:
            r9.mIsChairman = r0
        L_0x0302:
            java.lang.String r0 = r12.userAlias()
            r9.mDispName = r0
            r8.add(r9)
            goto L_0x0312
        L_0x030c:
            r17 = r5
            r18 = r6
            r19 = r14
        L_0x0312:
            int r11 = r11 + 1
            r5 = r17
            r6 = r18
            r14 = r19
            goto L_0x01cc
        L_0x031c:
            r19 = r14
            boolean r0 = r8.isEmpty()
            if (r0 == 0) goto L_0x033c
            int r0 = r4.getPhoneId()
            com.sec.internal.ims.servicemodules.im.strategy.IMnoStrategy r0 = com.sec.internal.ims.rcs.RcsPolicyManager.getRcsStrategy(r0)
            java.lang.String r5 = "confinfo_update_not_allowed"
            boolean r0 = r0.boolSetting(r5)
            if (r0 == 0) goto L_0x033c
            java.lang.String r0 = r1.LOG_TAG
            java.lang.String r1 = "imConferenceInfoUpdate: Drop the invalid info"
            com.sec.internal.log.IMSLog.s(r0, r1)
            return
        L_0x033c:
            com.sec.internal.constants.ims.servicemodules.im.event.ImSessionConferenceInfoUpdateEvent r0 = new com.sec.internal.constants.ims.servicemodules.im.event.ImSessionConferenceInfoUpdateEvent
            java.lang.String r6 = r3.mChatId
            long r11 = r2.maxUserCnt()
            int r9 = (int) r11
            long r2 = r2.sessionId()
            java.lang.Long r11 = java.lang.Long.valueOf(r2)
            com.sec.internal.ims.core.handler.secims.ResipImHandler r2 = r1.mResipImHandler
            java.lang.String r12 = r2.getImsiByUserAgent(r4)
            r5 = r0
            r14 = r19
            r5.<init>(r6, r7, r8, r9, r10, r11, r12, r13, r14)
            com.sec.internal.ims.core.handler.secims.ResipImHandler r1 = r1.mResipImHandler
            com.sec.internal.helper.RegistrantList r1 = r1.mConferenceInfoUpdateRegistrants
            com.sec.internal.helper.AsyncResult r2 = new com.sec.internal.helper.AsyncResult
            r3 = 0
            r2.<init>(r3, r0, r3)
            r1.notifyRegistrants(r2)
            return
        L_0x0367:
            java.lang.String r1 = r1.LOG_TAG
            java.lang.StringBuilder r2 = new java.lang.StringBuilder
            r2.<init>()
            java.lang.String r3 = "Unknown sessionId - "
            r2.append(r3)
            r2.append(r0)
            java.lang.String r0 = r2.toString()
            android.util.Log.e(r1, r0)
            return
        */
        throw new UnsupportedOperationException("Method not decompiled: com.sec.internal.ims.core.handler.secims.ResipImResponseHandler.handleImConferenceInfoUpdateNotify(com.sec.internal.ims.core.handler.secims.imsCommonStruc.Notify):void");
    }

    private int parseReasonHeader(String str) {
        int i;
        if (TextUtils.isEmpty(str)) {
            return 0;
        }
        try {
            int indexOf = str.indexOf("cause=");
            if (indexOf == -1 || (i = indexOf + 9) > str.length()) {
                return 0;
            }
            String substring = str.substring(indexOf + 6, i);
            String str2 = this.LOG_TAG;
            IMSLog.s(str2, "parseReasonHeader : " + substring);
            return Integer.parseInt(substring);
        } catch (NumberFormatException e) {
            e.printStackTrace();
            return 0;
        }
    }

    private void handleImMessageReportReceivedNotify(Notify notify) {
        if (notify.notiType() != 36) {
            Log.e(this.LOG_TAG, "handleImMessageReportReceivedNotify(): invalid notify");
            return;
        }
        ImMessageReportReceived imMessageReportReceived = (ImMessageReportReceived) notify.noti(new ImMessageReportReceived());
        int sessionId = (int) imMessageReportReceived.sessionId();
        String str = this.mResipImHandler.mSessions.get(Integer.valueOf(sessionId)).mChatId;
        String imdnMessageId = imMessageReportReceived.imdnMessageId();
        Result translateImResult = ResipTranslatorCollection.translateImResult(imMessageReportReceived.imError(), (Object) null);
        boolean isChat = imMessageReportReceived.isChat();
        String str2 = this.LOG_TAG;
        IMSLog.s(str2, "handleImMessageReportReceivedNotify(): sessionId = " + sessionId + " chatId = " + str + " imdnId = " + imdnMessageId + " result = " + translateImResult + " isChat = " + isChat);
        if (translateImResult.getImError() == ImError.SUCCESS) {
            return;
        }
        if (isChat) {
            this.mResipImHandler.mMessageFailedRegistrants.notifyRegistrants(new AsyncResult((Object) null, new SendMessageFailedEvent(Integer.valueOf(sessionId), str, imdnMessageId, translateImResult), (Throwable) null));
        } else {
            this.mResipImHandler.mImdnFailedRegistrants.notifyRegistrants(new AsyncResult((Object) null, new SendImdnFailedEvent(Integer.valueOf(sessionId), str, imdnMessageId, translateImResult), (Throwable) null));
        }
    }

    private void handleGroupChatSubscribeStatusNotify() {
        IMSLog.s(this.LOG_TAG, "handleGroupChatSubscribeStatusNotify()");
    }

    private void handleGroupChatListNotify(Notify notify) {
        IMSLog.s(this.LOG_TAG, "handleGroupChatListNotify()");
        GroupChatListUpdated groupChatListUpdated = (GroupChatListUpdated) notify.noti(new GroupChatListUpdated());
        int version = (int) groupChatListUpdated.version();
        UserAgent userAgent = this.mResipImHandler.getUserAgent((int) groupChatListUpdated.uaHandle());
        if (userAgent == null) {
            Log.e(this.LOG_TAG, "handleGcListNotify(): User Agent not found!");
            return;
        }
        int groupChatsLength = groupChatListUpdated.groupChatsLength();
        ArrayList arrayList = new ArrayList();
        for (int i = 0; i < groupChatsLength; i++) {
            GroupChatInfo groupChats = groupChatListUpdated.groupChats(i);
            if (groupChats != null) {
                arrayList.add(new ImIncomingGroupChatListEvent.Entry(Uri.parse(groupChats.uri()), groupChats.conversationId(), groupChats.subject()));
            }
        }
        this.mResipImHandler.mGroupChatListRegistrants.notifyRegistrants(new AsyncResult((Object) null, new ImIncomingGroupChatListEvent(version, arrayList, this.mResipImHandler.getImsiByUserAgent(userAgent)), (Throwable) null));
    }

    /* JADX WARNING: Removed duplicated region for block: B:46:0x00eb A[Catch:{ ParseException -> 0x00fa }] */
    /* JADX WARNING: Removed duplicated region for block: B:47:0x00f4 A[Catch:{ ParseException -> 0x00fa }] */
    /* JADX WARNING: Removed duplicated region for block: B:54:0x0119  */
    /* Code decompiled incorrectly, please refer to instructions dump. */
    private void handleGroupChatInfoNotify(com.sec.internal.ims.core.handler.secims.imsCommonStruc.Notify r20) {
        /*
            r19 = this;
            r1 = r19
            java.lang.String r0 = r1.LOG_TAG
            java.lang.String r2 = "handleGroupChatInfoNotify()"
            com.sec.internal.log.IMSLog.s(r0, r2)
            com.sec.internal.ims.core.handler.secims.imsCommonStruc.Notify_.GroupChatInfoUpdated r0 = new com.sec.internal.ims.core.handler.secims.imsCommonStruc.Notify_.GroupChatInfoUpdated
            r0.<init>()
            r2 = r20
            com.google.flatbuffers.Table r0 = r2.noti(r0)
            com.sec.internal.ims.core.handler.secims.imsCommonStruc.Notify_.GroupChatInfoUpdated r0 = (com.sec.internal.ims.core.handler.secims.imsCommonStruc.Notify_.GroupChatInfoUpdated) r0
            java.lang.String r3 = r0.uri()
            com.sec.internal.ims.core.handler.secims.imsCommonStruc.Notify_.ImConfInfoUpdated r2 = r0.info()
            if (r2 != 0) goto L_0x0028
            java.lang.String r0 = r1.LOG_TAG
            java.lang.String r1 = "handleGroupChatInfoNotify(): info is null, return"
            android.util.Log.e(r0, r1)
            return
        L_0x0028:
            long r4 = r0.uaHandle()
            int r0 = (int) r4
            com.sec.internal.ims.core.handler.secims.ResipImHandler r4 = r1.mResipImHandler
            com.sec.internal.ims.core.handler.secims.UserAgent r4 = r4.getUserAgent((int) r0)
            if (r4 != 0) goto L_0x003d
            java.lang.String r0 = r1.LOG_TAG
            java.lang.String r1 = "handleGroupChatInfoNotify(): User Agent not found!"
            android.util.Log.e(r0, r1)
            return
        L_0x003d:
            java.lang.String r11 = r2.timestamp()
            com.sec.internal.constants.ims.servicemodules.im.event.ImSessionConferenceInfoUpdateEvent$ImConferenceInfoType r0 = com.sec.internal.constants.ims.servicemodules.im.event.ImSessionConferenceInfoUpdateEvent.ImConferenceInfoType.FULL
            java.lang.String r5 = r2.state()
            if (r5 == 0) goto L_0x0066
            java.lang.String r5 = r2.state()
            r5.hashCode()
            java.lang.String r6 = "partial"
            boolean r6 = r5.equals(r6)
            if (r6 != 0) goto L_0x0064
            java.lang.String r6 = "deleted"
            boolean r5 = r5.equals(r6)
            if (r5 != 0) goto L_0x0061
            goto L_0x0066
        L_0x0061:
            com.sec.internal.constants.ims.servicemodules.im.event.ImSessionConferenceInfoUpdateEvent$ImConferenceInfoType r0 = com.sec.internal.constants.ims.servicemodules.im.event.ImSessionConferenceInfoUpdateEvent.ImConferenceInfoType.DELETED
            goto L_0x0066
        L_0x0064:
            com.sec.internal.constants.ims.servicemodules.im.event.ImSessionConferenceInfoUpdateEvent$ImConferenceInfoType r0 = com.sec.internal.constants.ims.servicemodules.im.event.ImSessionConferenceInfoUpdateEvent.ImConferenceInfoType.PARTIAL
        L_0x0066:
            r5 = r0
            com.sec.internal.ims.core.handler.secims.imsCommonStruc.Notify_.ImConfInfoUpdated_.SubjectExt r6 = r2.subjectData()
            r12 = 0
            if (r6 == 0) goto L_0x00a4
            java.lang.String r0 = r6.participant()
            if (r0 == 0) goto L_0x007e
            java.lang.String r0 = r6.participant()
            com.sec.ims.util.ImsUri r0 = com.sec.ims.util.ImsUri.parse(r0)
            r7 = r0
            goto L_0x007f
        L_0x007e:
            r7 = r12
        L_0x007f:
            java.lang.String r0 = r6.timestamp()
            if (r0 == 0) goto L_0x0098
            java.lang.String r0 = r6.timestamp()     // Catch:{ ParseException -> 0x008e }
            java.util.Date r0 = com.sec.internal.helper.Iso8601.parse(r0)     // Catch:{ ParseException -> 0x008e }
            goto L_0x0099
        L_0x008e:
            r0 = move-exception
            r0.printStackTrace()
            java.util.Date r0 = new java.util.Date
            r0.<init>()
            goto L_0x0099
        L_0x0098:
            r0 = r12
        L_0x0099:
            com.sec.internal.constants.ims.servicemodules.im.ImSubjectData r8 = new com.sec.internal.constants.ims.servicemodules.im.ImSubjectData
            java.lang.String r6 = r6.subject()
            r8.<init>(r6, r7, r0)
            r7 = r8
            goto L_0x00a5
        L_0x00a4:
            r7 = r12
        L_0x00a5:
            com.sec.internal.ims.core.handler.secims.imsCommonStruc.Notify_.ImConfInfoUpdated_.Icon r0 = r2.iconData()
            if (r0 == 0) goto L_0x010d
            int r6 = r0.icontype()
            com.sec.internal.constants.ims.servicemodules.im.ImIconData$IconType r8 = com.sec.internal.constants.ims.servicemodules.im.ImIconData.IconType.ICON_TYPE_NONE
            int r9 = r8.ordinal()
            if (r6 == r9) goto L_0x010d
            java.lang.String r6 = r0.participant()
            com.sec.ims.util.ImsUri r15 = com.sec.ims.util.ImsUri.parse(r6)
            java.lang.String r17 = r0.iconLocation()
            int r6 = r0.icontype()
            com.sec.internal.constants.ims.servicemodules.im.ImIconData$IconType r9 = com.sec.internal.constants.ims.servicemodules.im.ImIconData.IconType.ICON_TYPE_FILE
            int r10 = r9.ordinal()
            if (r6 != r10) goto L_0x00d3
            r14 = r9
        L_0x00d0:
            r18 = r12
            goto L_0x00e5
        L_0x00d3:
            com.sec.internal.constants.ims.servicemodules.im.ImIconData$IconType r9 = com.sec.internal.constants.ims.servicemodules.im.ImIconData.IconType.ICON_TYPE_URI
            int r10 = r9.ordinal()
            if (r6 != r10) goto L_0x00e3
            java.lang.String r6 = r0.iconLocation()
            r18 = r6
            r14 = r9
            goto L_0x00e5
        L_0x00e3:
            r14 = r8
            goto L_0x00d0
        L_0x00e5:
            java.lang.String r6 = r0.timestamp()     // Catch:{ ParseException -> 0x00fa }
            if (r6 == 0) goto L_0x00f4
            java.lang.String r0 = r0.timestamp()     // Catch:{ ParseException -> 0x00fa }
            java.util.Date r0 = com.sec.internal.helper.Iso8601.parse(r0)     // Catch:{ ParseException -> 0x00fa }
            goto L_0x0103
        L_0x00f4:
            java.util.Date r0 = new java.util.Date     // Catch:{ ParseException -> 0x00fa }
            r0.<init>()     // Catch:{ ParseException -> 0x00fa }
            goto L_0x0103
        L_0x00fa:
            r0 = move-exception
            r0.printStackTrace()
            java.util.Date r0 = new java.util.Date
            r0.<init>()
        L_0x0103:
            r16 = r0
            com.sec.internal.constants.ims.servicemodules.im.ImIconData r0 = new com.sec.internal.constants.ims.servicemodules.im.ImIconData
            r13 = r0
            r13.<init>(r14, r15, r16, r17, r18)
            r10 = r0
            goto L_0x010e
        L_0x010d:
            r10 = r12
        L_0x010e:
            java.util.ArrayList r6 = new java.util.ArrayList
            r6.<init>()
            int r0 = r2.usersLength()
            if (r0 <= 0) goto L_0x0207
            r0 = 0
            r8 = r0
        L_0x011b:
            int r0 = r2.usersLength()
            if (r8 >= r0) goto L_0x0207
            com.sec.internal.ims.core.handler.secims.imsCommonStruc.ImConfUser r9 = r2.users(r8)
            if (r9 == 0) goto L_0x0203
            com.sec.internal.constants.ims.servicemodules.im.ImConferenceParticipantInfo r13 = new com.sec.internal.constants.ims.servicemodules.im.ImConferenceParticipantInfo
            r13.<init>()
            java.lang.String r0 = r9.entity()
            com.sec.ims.util.ImsUri r0 = com.sec.ims.util.ImsUri.parse(r0)
            r13.mUri = r0
            java.lang.String r0 = r9.state()
            com.sec.internal.constants.ims.servicemodules.im.ImConferenceParticipantInfo$ImConferenceUserElemState r0 = com.sec.internal.ims.translate.ResipTranslatorCollection.translateImConferenceUserElemState(r0)
            r13.mUserElemState = r0
            boolean r0 = r9.yourOwn()
            r13.mIsOwn = r0
            com.sec.internal.ims.core.handler.secims.imsCommonStruc.ImConfUserEndpoint r0 = r9.endpoint()
            if (r0 == 0) goto L_0x01e8
            com.sec.internal.ims.core.handler.secims.imsCommonStruc.ImConfUserEndpoint r0 = r9.endpoint()
            java.lang.String r0 = r0.status()
            if (r0 == 0) goto L_0x0164
            com.sec.internal.ims.core.handler.secims.imsCommonStruc.ImConfUserEndpoint r0 = r9.endpoint()
            java.lang.String r0 = r0.status()
            com.sec.internal.constants.ims.servicemodules.im.ImConferenceParticipantInfo$ImConferenceParticipantStatus r0 = com.sec.internal.ims.translate.ResipTranslatorCollection.translateToImConferenceParticipantStatus(r0)
            r13.mParticipantStatus = r0
        L_0x0164:
            com.sec.internal.ims.core.handler.secims.imsCommonStruc.ImConfUserEndpoint r0 = r9.endpoint()
            java.lang.String r0 = r0.disconnectMethod()
            if (r0 == 0) goto L_0x017c
            com.sec.internal.ims.core.handler.secims.imsCommonStruc.ImConfUserEndpoint r0 = r9.endpoint()
            java.lang.String r0 = r0.disconnectMethod()
            com.sec.internal.constants.ims.servicemodules.im.ImConferenceParticipantInfo$ImConferenceDisconnectionReason r0 = com.sec.internal.ims.translate.ResipTranslatorCollection.translateToImConferenceDisconnectionReason(r0)
            r13.mDisconnectionReason = r0
        L_0x017c:
            com.sec.internal.ims.core.handler.secims.imsCommonStruc.ImConfUserEndpoint r0 = r9.endpoint()
            com.sec.internal.ims.core.handler.secims.imsCommonStruc.ImConfUserJoiningInfo r0 = r0.joininginfo()
            if (r0 == 0) goto L_0x01b2
            com.sec.internal.ims.core.handler.secims.imsCommonStruc.ImConfUserEndpoint r0 = r9.endpoint()
            com.sec.internal.ims.core.handler.secims.imsCommonStruc.ImConfUserJoiningInfo r0 = r0.joininginfo()
            java.lang.String r0 = r0.when()
            if (r0 == 0) goto L_0x01b2
            com.sec.internal.ims.core.handler.secims.imsCommonStruc.ImConfUserEndpoint r0 = r9.endpoint()     // Catch:{ ParseException -> 0x01a7 }
            com.sec.internal.ims.core.handler.secims.imsCommonStruc.ImConfUserJoiningInfo r0 = r0.joininginfo()     // Catch:{ ParseException -> 0x01a7 }
            java.lang.String r0 = r0.when()     // Catch:{ ParseException -> 0x01a7 }
            java.util.Date r0 = com.sec.internal.helper.Iso8601.parse(r0)     // Catch:{ ParseException -> 0x01a7 }
            r13.mJoiningTime = r0     // Catch:{ ParseException -> 0x01a7 }
            goto L_0x01b2
        L_0x01a7:
            r0 = move-exception
            r0.printStackTrace()
            java.util.Date r0 = new java.util.Date
            r0.<init>()
            r13.mJoiningTime = r0
        L_0x01b2:
            com.sec.internal.ims.core.handler.secims.imsCommonStruc.ImConfUserEndpoint r0 = r9.endpoint()
            com.sec.internal.ims.core.handler.secims.imsCommonStruc.ImConfUserDisconnectionInfo r0 = r0.disconnectioninfo()
            if (r0 == 0) goto L_0x01e8
            com.sec.internal.ims.core.handler.secims.imsCommonStruc.ImConfUserEndpoint r0 = r9.endpoint()
            com.sec.internal.ims.core.handler.secims.imsCommonStruc.ImConfUserDisconnectionInfo r0 = r0.disconnectioninfo()
            java.lang.String r0 = r0.when()
            if (r0 == 0) goto L_0x01e8
            com.sec.internal.ims.core.handler.secims.imsCommonStruc.ImConfUserEndpoint r0 = r9.endpoint()     // Catch:{ ParseException -> 0x01dd }
            com.sec.internal.ims.core.handler.secims.imsCommonStruc.ImConfUserDisconnectionInfo r0 = r0.disconnectioninfo()     // Catch:{ ParseException -> 0x01dd }
            java.lang.String r0 = r0.when()     // Catch:{ ParseException -> 0x01dd }
            java.util.Date r0 = com.sec.internal.helper.Iso8601.parse(r0)     // Catch:{ ParseException -> 0x01dd }
            r13.mDisconnectionTime = r0     // Catch:{ ParseException -> 0x01dd }
            goto L_0x01e8
        L_0x01dd:
            r0 = move-exception
            r0.printStackTrace()
            java.util.Date r0 = new java.util.Date
            r0.<init>()
            r13.mDisconnectionTime = r0
        L_0x01e8:
            java.lang.String r0 = r9.roles()
            if (r0 == 0) goto L_0x01fa
            java.lang.String r0 = r9.roles()
            java.lang.String r14 = "chairman"
            boolean r0 = r0.equals(r14)
            r13.mIsChairman = r0
        L_0x01fa:
            java.lang.String r0 = r9.userAlias()
            r13.mDispName = r0
            r6.add(r13)
        L_0x0203:
            int r8 = r8 + 1
            goto L_0x011b
        L_0x0207:
            com.sec.internal.constants.ims.servicemodules.im.event.ImSessionConferenceInfoUpdateEvent r0 = new com.sec.internal.constants.ims.servicemodules.im.event.ImSessionConferenceInfoUpdateEvent
            long r8 = r2.maxUserCnt()
            int r8 = (int) r8
            r9 = 0
            com.sec.internal.ims.core.handler.secims.ResipImHandler r2 = r1.mResipImHandler
            java.lang.String r13 = r2.getImsiByUserAgent(r4)
            r2 = r0
            r4 = r5
            r5 = r6
            r6 = r8
            r8 = r9
            r9 = r13
            r2.<init>(r3, r4, r5, r6, r7, r8, r9, r10, r11)
            com.sec.internal.ims.core.handler.secims.ResipImHandler r1 = r1.mResipImHandler
            com.sec.internal.helper.RegistrantList r1 = r1.mGroupChatInfoRegistrants
            com.sec.internal.helper.AsyncResult r2 = new com.sec.internal.helper.AsyncResult
            r2.<init>(r12, r0, r12)
            r1.notifyRegistrants(r2)
            return
        */
        throw new UnsupportedOperationException("Method not decompiled: com.sec.internal.ims.core.handler.secims.ResipImResponseHandler.handleGroupChatInfoNotify(com.sec.internal.ims.core.handler.secims.imsCommonStruc.Notify):void");
    }

    private void handleImSessionProvisionalResponseNotify(Notify notify) {
        if (notify.notiType() != 28) {
            Log.e(this.LOG_TAG, "handleImSessionProvisionalResponseNotify(): invalid notify");
            return;
        }
        SessionStarted sessionStarted = (SessionStarted) notify.noti(new SessionStarted());
        int sessionHandle = (int) sessionStarted.sessionHandle();
        Result translateImResult = ResipTranslatorCollection.translateImResult(sessionStarted.imError(), (Object) null);
        ImError imError = translateImResult.getImError();
        String str = this.LOG_TAG;
        IMSLog.s(str, "handleImSessionProvisionalResponseNotify(): sessionHandle = " + sessionHandle + ", response = " + imError);
        ResipImHandler.ImSession imSession = this.mResipImHandler.mSessions.get(Integer.valueOf(sessionHandle));
        if (imSession == null) {
            String str2 = this.LOG_TAG;
            Log.e(str2, "handleImSessionProvisionalResponseNotify(): Unknown session handle: " + sessionHandle);
            return;
        }
        Message message = imSession.mStartSyncCallback;
        if (message != null) {
            this.mResipImHandler.sendCallback(message, Integer.valueOf(sessionHandle));
            imSession.mStartSyncCallback = null;
        }
        Message message2 = imSession.mStartProvisionalCallback;
        if (message2 != null) {
            this.mResipImHandler.sendCallback(Message.obtain(message2), new StartImSessionResult(translateImResult, (ImsUri) null, Integer.valueOf(sessionHandle), true));
        }
        if (imSession.mFirstMessageCallback != null) {
            String str3 = this.LOG_TAG;
            Log.i(str3, "handleImSessionProvisionalResponseNotify(): handle provisional response as SUCCESS for the message in INVITE. sessionHandle = " + sessionHandle + ", response = " + imError);
            this.mResipImHandler.sendCallback(Message.obtain(imSession.mFirstMessageCallback), new SendMessageResult((Object) Integer.valueOf(sessionHandle), new Result(ImError.SUCCESS, translateImResult), true));
        }
    }

    private void handleMessageRevokeResponseReceivedNotify(Notify notify) {
        if (notify.notiType() != 46) {
            Log.e(this.LOG_TAG, "handleMessageRevokeResponseReceivedNotify(): invalid notify");
            return;
        }
        MessageRevokeResponseReceived messageRevokeResponseReceived = (MessageRevokeResponseReceived) notify.noti(new MessageRevokeResponseReceived());
        ImsUri parse = ImsUri.parse(messageRevokeResponseReceived.uri());
        if (parse == null) {
            String str = this.LOG_TAG;
            Log.i(str, "Invalid remote uri, return. uri=" + messageRevokeResponseReceived.uri());
            return;
        }
        MessageRevokeResponse messageRevokeResponse = new MessageRevokeResponse(parse, messageRevokeResponseReceived.imdnMessageId(), messageRevokeResponseReceived.result());
        String str2 = this.LOG_TAG;
        IMSLog.s(str2, "handleMessageRevokeResponseReceivedNotify: " + messageRevokeResponse);
        this.mResipImHandler.mMessageRevokeResponseRegistransts.notifyRegistrants(new AsyncResult((Object) null, messageRevokeResponse, (Throwable) null));
    }

    private void handleSendMessageRevokeResponseNotify(Notify notify) {
        if (notify.notiType() != 47) {
            Log.e(this.LOG_TAG, "handleSendMessageRevokeResponseNotify(): invalid notify");
            return;
        }
        SendMessageRevokeResponse sendMessageRevokeResponse = (SendMessageRevokeResponse) notify.noti(new SendMessageRevokeResponse());
        if (sendMessageRevokeResponse != null) {
            MessageRevokeResponse messageRevokeResponse = new MessageRevokeResponse((ImsUri) null, sendMessageRevokeResponse.imdnMessageId(), ResipTranslatorCollection.translateImResult(sendMessageRevokeResponse.imError(), (Object) null).getImError() == ImError.SUCCESS);
            String str = this.LOG_TAG;
            IMSLog.s(str, "handleSendMessageRevokeResponseNotify: " + messageRevokeResponse);
            this.mResipImHandler.mSendMessageRevokeResponseRegistransts.notifyRegistrants(new AsyncResult((Object) null, messageRevokeResponse, (Throwable) null));
        }
    }

    private void handleRequestChatbotAnonymizeResp(Notify notify) {
        if (notify.notiType() != 48) {
            Log.e(this.LOG_TAG, "handleRequestChatbotAnonymizeResp(): invalid notify");
            return;
        }
        RequestChatbotAnonymizeResponse requestChatbotAnonymizeResponse = (RequestChatbotAnonymizeResponse) notify.noti(new RequestChatbotAnonymizeResponse());
        if (requestChatbotAnonymizeResponse != null) {
            ChatbotAnonymizeRespEvent chatbotAnonymizeRespEvent = new ChatbotAnonymizeRespEvent(requestChatbotAnonymizeResponse.uri(), ResipTranslatorCollection.translateImResult(requestChatbotAnonymizeResponse.imError(), (Object) null).getImError(), requestChatbotAnonymizeResponse.commandId(), requestChatbotAnonymizeResponse.retryAfter());
            String str = this.LOG_TAG;
            IMSLog.s(str, "ChatbotAnonymizeRespEvent: " + chatbotAnonymizeRespEvent);
            this.mResipImHandler.mChatbotAnonymizeResponseRegistrants.notifyRegistrants(new AsyncResult((Object) null, chatbotAnonymizeRespEvent, (Throwable) null));
        }
    }

    private void handleRequestChatbotAnonymizeNotify(Notify notify) {
        String str;
        String str2;
        if (notify.notiType() != 49) {
            Log.e(this.LOG_TAG, "handleSetChatbotAnonymizeResponseNotify(): invalid notify");
            return;
        }
        RequestChatbotAnonymizeResponseReceived requestChatbotAnonymizeResponseReceived = (RequestChatbotAnonymizeResponseReceived) notify.noti(new RequestChatbotAnonymizeResponseReceived());
        String uri = requestChatbotAnonymizeResponseReceived.uri();
        String result = requestChatbotAnonymizeResponseReceived.result();
        ChatbotXmlUtils instance = ChatbotXmlUtils.getInstance();
        String str3 = "";
        if (result != null) {
            try {
                str2 = instance.parseXml(result, "AM/result");
                try {
                    str3 = instance.parseXml(result, "AM/Command-ID");
                } catch (Exception e) {
                    e = e;
                    e.printStackTrace();
                    str = str3;
                    str3 = str2;
                    ChatbotAnonymizeNotifyEvent chatbotAnonymizeNotifyEvent = new ChatbotAnonymizeNotifyEvent(uri, str3, str);
                    String str4 = this.LOG_TAG;
                    IMSLog.s(str4, "ChatbotAnonymizeNotifyEvent: " + chatbotAnonymizeNotifyEvent);
                    this.mResipImHandler.mChatbotAnonymizeNotifyRegistrants.notifyRegistrants(new AsyncResult((Object) null, chatbotAnonymizeNotifyEvent, (Throwable) null));
                }
            } catch (Exception e2) {
                e = e2;
                str2 = str3;
                e.printStackTrace();
                str = str3;
                str3 = str2;
                ChatbotAnonymizeNotifyEvent chatbotAnonymizeNotifyEvent2 = new ChatbotAnonymizeNotifyEvent(uri, str3, str);
                String str42 = this.LOG_TAG;
                IMSLog.s(str42, "ChatbotAnonymizeNotifyEvent: " + chatbotAnonymizeNotifyEvent2);
                this.mResipImHandler.mChatbotAnonymizeNotifyRegistrants.notifyRegistrants(new AsyncResult((Object) null, chatbotAnonymizeNotifyEvent2, (Throwable) null));
            }
            str = str3;
            str3 = str2;
        } else {
            str = str3;
        }
        ChatbotAnonymizeNotifyEvent chatbotAnonymizeNotifyEvent22 = new ChatbotAnonymizeNotifyEvent(uri, str3, str);
        String str422 = this.LOG_TAG;
        IMSLog.s(str422, "ChatbotAnonymizeNotifyEvent: " + chatbotAnonymizeNotifyEvent22);
        this.mResipImHandler.mChatbotAnonymizeNotifyRegistrants.notifyRegistrants(new AsyncResult((Object) null, chatbotAnonymizeNotifyEvent22, (Throwable) null));
    }

    private void handleReportChatbotAsSpamResponseNotify(Notify notify) {
        if (notify.notiType() != 50) {
            Log.e(this.LOG_TAG, "handleChatbotAsSpamResp(): invalid notify");
            return;
        }
        ReportChatbotAsSpamResponse reportChatbotAsSpamResponse = (ReportChatbotAsSpamResponse) notify.noti(new ReportChatbotAsSpamResponse());
        if (reportChatbotAsSpamResponse != null) {
            ReportChatbotAsSpamRespEvent reportChatbotAsSpamRespEvent = new ReportChatbotAsSpamRespEvent(reportChatbotAsSpamResponse.uri(), reportChatbotAsSpamResponse.requestId(), ResipTranslatorCollection.translateImResult(reportChatbotAsSpamResponse.imError(), (Object) null).getImError());
            String str = this.LOG_TAG;
            IMSLog.s(str, "handleReportChatbotAsSpamResponseNotify: " + reportChatbotAsSpamRespEvent);
            this.mResipImHandler.mReportChatbotAsSpamRespRegistrants.notifyRegistrants(new AsyncResult((Object) null, reportChatbotAsSpamRespEvent, (Throwable) null));
        }
    }

    private ImIncomingMessageEvent parseImMessageParam(ImMessageParam imMessageParam) {
        ImIncomingMessageEvent imIncomingMessageEvent = new ImIncomingMessageEvent();
        if (imMessageParam == null) {
            return imIncomingMessageEvent;
        }
        ArrayList arrayList = new ArrayList();
        if (imMessageParam.imdn() != null) {
            imIncomingMessageEvent.mImdnMessageId = imMessageParam.imdn().messageId();
            for (int i = 0; i < imMessageParam.imdn().notiLength(); i++) {
                arrayList.add(Integer.valueOf(imMessageParam.imdn().noti(i)));
            }
            imIncomingMessageEvent.mDispositionNotification = ResipTranslatorCollection.translateStackImdnNoti(arrayList);
            try {
                if (!TextUtils.isEmpty(imIncomingMessageEvent.mImdnMessageId)) {
                    String datetime = imMessageParam.imdn().datetime();
                    imIncomingMessageEvent.mImdnTime = !TextUtils.isEmpty(datetime) ? Iso8601.parse(datetime) : new Date();
                }
            } catch (ParseException e) {
                e.printStackTrace();
                imIncomingMessageEvent.mImdnTime = new Date();
            }
            if (!TextUtils.isEmpty(imMessageParam.imdn().originalToHdr())) {
                imIncomingMessageEvent.mOriginalToHdr = imMessageParam.imdn().originalToHdr();
            }
            if (imMessageParam.imdn().recRouteLength() > 0) {
                imIncomingMessageEvent.mImdnRecRouteList = new ArrayList();
                for (int i2 = 0; i2 < imMessageParam.imdn().recRouteLength(); i2++) {
                    ImdnRecRoute recRoute = imMessageParam.imdn().recRoute(i2);
                    if (recRoute != null) {
                        IMSLog.s(this.LOG_TAG, "imdn route: " + recRoute.uri());
                        IMSLog.s(this.LOG_TAG, "mImdnMessageId: " + imIncomingMessageEvent.mImdnMessageId);
                        imIncomingMessageEvent.mImdnRecRouteList.add(new ImImdnRecRoute(imIncomingMessageEvent.mImdnMessageId, recRoute.uri(), recRoute.name()));
                    }
                }
            }
        }
        imIncomingMessageEvent.mContentType = imMessageParam.contentType();
        String adjustMessageBody = ResipTranslatorCollection.adjustMessageBody(imMessageParam.body(), imIncomingMessageEvent.mContentType);
        imIncomingMessageEvent.mBody = adjustMessageBody;
        if (adjustMessageBody == null) {
            return null;
        }
        String sender = imMessageParam.sender();
        if (sender != null) {
            String replaceAll = sender.replaceAll("\\<|\\>", "");
            IMSLog.s(this.LOG_TAG, "parseImMessageParam sender=" + replaceAll);
            imIncomingMessageEvent.mSender = ImsUri.parse(replaceAll);
        }
        boolean silenceSupported = imMessageParam.silenceSupported();
        imIncomingMessageEvent.mIsRoutingMsg = silenceSupported;
        if (silenceSupported) {
            Log.i(this.LOG_TAG, "parseImMessageParam -> routing message");
            imIncomingMessageEvent.mRequestUri = ImsUri.parse(imMessageParam.requestUri());
            imIncomingMessageEvent.mPAssertedId = ImsUri.parse(imMessageParam.pAssertedId());
            imIncomingMessageEvent.mReceiver = ImsUri.parse(imMessageParam.receiver());
        }
        imIncomingMessageEvent.mUserAlias = imMessageParam.userAlias();
        imIncomingMessageEvent.mCpimNamespaces = new ImCpimNamespaces();
        for (int i3 = 0; i3 < imMessageParam.cpimNamespacesLength(); i3++) {
            CpimNamespace cpimNamespaces = imMessageParam.cpimNamespaces(i3);
            if (cpimNamespaces != null) {
                imIncomingMessageEvent.mCpimNamespaces.addNamespace(cpimNamespaces.name(), cpimNamespaces.uri());
                for (int i4 = 0; i4 < cpimNamespaces.headersLength(); i4++) {
                    Pair headers = cpimNamespaces.headers(i4);
                    if (headers != null && !TextUtils.isEmpty(headers.key())) {
                        imIncomingMessageEvent.mCpimNamespaces.getNamespace(cpimNamespaces.name()).addHeader(headers.key(), headers.value());
                    }
                }
            }
        }
        if (imMessageParam.ccParticipantsLength() > 0) {
            imIncomingMessageEvent.mCcParticipants = new ArrayList();
            for (int i5 = 0; i5 < imMessageParam.ccParticipantsLength(); i5++) {
                imIncomingMessageEvent.mCcParticipants.add(ImsUri.parse(imMessageParam.ccParticipants(i5)));
            }
            IMSLog.s(this.LOG_TAG, "parseImMessageParam event.mCcParticipants=" + imIncomingMessageEvent.mCcParticipants);
        }
        return imIncomingMessageEvent;
    }
}
