package com.sec.internal.ims.servicemodules.im;

import android.content.Context;
import android.net.Network;
import android.net.Uri;
import android.os.Looper;
import android.os.Message;
import android.util.Log;
import com.sec.ims.ImsRegistration;
import com.sec.ims.settings.ImsProfile;
import com.sec.internal.constants.ims.servicemodules.im.ImCacheAction;
import com.sec.internal.constants.ims.servicemodules.im.ImConstants;
import com.sec.internal.constants.ims.servicemodules.im.ImError;
import com.sec.internal.constants.ims.servicemodules.im.event.FtTransferProgressEvent;
import com.sec.internal.constants.ims.servicemodules.im.reason.CancelReason;
import com.sec.internal.constants.ims.servicemodules.im.result.FtResult;
import com.sec.internal.constants.ims.servicemodules.im.result.Result;
import com.sec.internal.helper.AsyncResult;
import com.sec.internal.helper.FileUtils;
import com.sec.internal.helper.HttpRequest;
import com.sec.internal.helper.IState;
import com.sec.internal.helper.PreciseAlarmManager;
import com.sec.internal.helper.State;
import com.sec.internal.helper.translate.MappingTranslator;
import com.sec.internal.ims.registry.ImsRegistry;
import com.sec.internal.ims.servicemodules.im.FtMessage;
import com.sec.internal.ims.servicemodules.im.UploadFileTask;
import com.sec.internal.ims.servicemodules.im.data.response.FileResizeResponse;
import com.sec.internal.ims.servicemodules.im.strategy.IMnoStrategy;
import com.sec.internal.ims.servicemodules.im.util.AsyncFileTask;
import com.sec.internal.ims.settings.RcsPolicySettings;
import com.sec.internal.ims.util.ChatbotUriUtil;
import com.sec.internal.imscr.LogClass;
import com.sec.internal.log.IMSLog;
import java.io.File;
import java.util.ArrayList;
import java.util.Objects;
import java.util.concurrent.Executor;

public class FtHttpOutgoingMessage extends FtMessage {
    private static final int EVENT_RETRY_UPLOAD = 305;
    private static final int EVENT_SEND_MESSAGE_DONE = 304;
    private static final int EVENT_UPLOAD_CANCELED = 303;
    private static final int EVENT_UPLOAD_COMPLETED = 302;
    private static final int EVENT_UPLOAD_PROGRESS = 201;
    /* access modifiers changed from: private */
    public static final String LOG_TAG = FtHttpOutgoingMessage.class.getSimpleName();

    public int getTransferMech() {
        return 1;
    }

    /* access modifiers changed from: protected */
    public void sendDeliveredNotification(Object obj, String str, String str2, Message message, String str3, boolean z, boolean z2) {
    }

    /* access modifiers changed from: protected */
    public void sendDisplayedNotification(Object obj, String str, String str2, Message message, String str3, boolean z, boolean z2) {
    }

    protected FtHttpOutgoingMessage(Builder<?> builder) {
        super(builder);
    }

    public static Builder<?> builder() {
        return new Builder2();
    }

    public boolean isAutoResumable() {
        return !getRcsStrategy().boolSetting(RcsPolicySettings.RcsPolicy.CANCEL_FOR_DEREGI_PROMPTLY);
    }

    /* access modifiers changed from: protected */
    public FtMessage.FtStateMachine createFtStateMachine(String str, Looper looper) {
        return new FtHttpStateMachine("FtHttpOutgoingMessage#" + str, looper);
    }

    public void onSendMessageDone(Result result, IMnoStrategy.StrategyResponse strategyResponse) {
        String str = LOG_TAG;
        Log.i(str, "onSendMessageDone: mid = " + this.mId + ", mStatus = " + this.mStatus + ", mBody = " + IMSLog.checker(this.mBody));
        if (result.getImError() == ImError.SUCCESS) {
            ImConstants.Status status = this.mStatus;
            ImConstants.Status status2 = ImConstants.Status.SENT;
            if (status != status2) {
                this.mListener.onTransferCompleted(this);
                setSentTimestamp(System.currentTimeMillis());
                updateStatus(status2);
                this.mListener.onMessageSendingSucceeded(this);
                return;
            }
            return;
        }
        updateStatus(ImConstants.Status.FAILED);
        this.mListener.onMessageSendingFailed(this, strategyResponse, result);
    }

    private int getImsRegistrationCurrentRat(int i) {
        try {
            ImsRegistration[] registrationInfoByPhoneId = ImsRegistry.getRegistrationManager().getRegistrationInfoByPhoneId(i);
            if (registrationInfoByPhoneId == null) {
                return -1;
            }
            for (ImsRegistration imsRegistration : registrationInfoByPhoneId) {
                if (imsRegistration.hasService("ft_http")) {
                    return imsRegistration.getCurrentRat();
                }
            }
            return -1;
        } catch (NullPointerException e) {
            Log.e(LOG_TAG, "getImsRegistrationCurrentRat: NullPointerException e = " + e);
            return -1;
        }
    }

    public static abstract class Builder<T extends Builder<T>> extends FtMessage.Builder<T> {
        public FtHttpOutgoingMessage build() {
            return new FtHttpOutgoingMessage(this);
        }
    }

    private static class Builder2 extends Builder<Builder2> {
        /* access modifiers changed from: protected */
        public Builder2 self() {
            return this;
        }

        private Builder2() {
        }
    }

    private class FtHttpStateMachine extends FtMessage.FtStateMachine {
        /* access modifiers changed from: private */
        public final AttachedState mAttachedState;
        private final CanceledNeedToNotifyState mCanceledNeedToNotifyState;
        /* access modifiers changed from: private */
        public final CanceledState mCanceledState;
        /* access modifiers changed from: private */
        public final CancelingState mCancelingState;
        /* access modifiers changed from: private */
        public final CompletedState mCompletedState;
        protected final MappingTranslator<Integer, State> mDbStateTranslator;
        private final DefaultState mDefaultState = new DefaultState();
        /* access modifiers changed from: private */
        public final InProgressState mInProgressState;
        private final InitialState mInitialState;
        private final SendingState mSendingState;
        protected final MappingTranslator<IState, Integer> mStateTranslator;

        protected FtHttpStateMachine(String str, Looper looper) {
            super(str, looper);
            InitialState initialState = new InitialState();
            this.mInitialState = initialState;
            AttachedState attachedState = new AttachedState();
            this.mAttachedState = attachedState;
            SendingState sendingState = new SendingState();
            this.mSendingState = sendingState;
            InProgressState inProgressState = new InProgressState();
            this.mInProgressState = inProgressState;
            CancelingState cancelingState = new CancelingState();
            this.mCancelingState = cancelingState;
            CanceledState canceledState = new CanceledState();
            this.mCanceledState = canceledState;
            CompletedState completedState = new CompletedState();
            this.mCompletedState = completedState;
            CanceledNeedToNotifyState canceledNeedToNotifyState = new CanceledNeedToNotifyState();
            this.mCanceledNeedToNotifyState = canceledNeedToNotifyState;
            this.mStateTranslator = new MappingTranslator.Builder().map(initialState, 0).map(attachedState, 6).map(sendingState, 9).map(inProgressState, 2).map(cancelingState, 7).map(canceledState, 4).map(completedState, 3).map(canceledNeedToNotifyState, 10).buildTranslator();
            this.mDbStateTranslator = new MappingTranslator.Builder().map(0, initialState).map(6, attachedState).map(9, canceledState).map(2, inProgressState).map(7, canceledState).map(4, canceledState).map(3, completedState).map(10, canceledNeedToNotifyState).buildTranslator();
        }

        /* access modifiers changed from: protected */
        public void initState(State state) {
            addState(this.mDefaultState);
            addState(this.mInitialState, this.mDefaultState);
            addState(this.mAttachedState, this.mDefaultState);
            addState(this.mSendingState, this.mDefaultState);
            addState(this.mInProgressState, this.mDefaultState);
            addState(this.mCancelingState, this.mDefaultState);
            addState(this.mCanceledState, this.mInitialState);
            addState(this.mCompletedState, this.mInitialState);
            addState(this.mCanceledNeedToNotifyState, this.mDefaultState);
            String r0 = FtHttpOutgoingMessage.LOG_TAG;
            Log.i(r0, "setting current state as " + state.getName() + " for messageId : " + FtHttpOutgoingMessage.this.mId);
            setInitialState(state);
            start();
        }

        /* access modifiers changed from: private */
        public void handleFTHttpFailure() {
            logi("handleFTHttpFailure");
            IMnoStrategy rcsStrategy = FtHttpOutgoingMessage.this.getRcsStrategy();
            FtHttpOutgoingMessage ftHttpOutgoingMessage = FtHttpOutgoingMessage.this;
            IMnoStrategy.StrategyResponse handleFtHttpRequestFailure = rcsStrategy.handleFtHttpRequestFailure(ftHttpOutgoingMessage.mCancelReason, ftHttpOutgoingMessage.mDirection, ftHttpOutgoingMessage.mIsGroupChat);
            FtHttpOutgoingMessage ftHttpOutgoingMessage2 = FtHttpOutgoingMessage.this;
            boolean hasChatbotUri = ChatbotUriUtil.hasChatbotUri(ftHttpOutgoingMessage2.mListener.onRequestParticipantUris(ftHttpOutgoingMessage2.mChatId), FtHttpOutgoingMessage.this.mConfig.getPhoneId());
            if (handleFtHttpRequestFailure.getStatusCode() != IMnoStrategy.StatusCode.FALLBACK_TO_SLM || hasChatbotUri) {
                if (FtHttpOutgoingMessage.this.mConfig.getFtFallbackAllFail()) {
                    FtHttpOutgoingMessage ftHttpOutgoingMessage3 = FtHttpOutgoingMessage.this;
                    if (!ftHttpOutgoingMessage3.mIsGroupChat && !hasChatbotUri) {
                        ftHttpOutgoingMessage3.mCancelReason = CancelReason.REMOTE_TEMPORARILY_UNAVAILABLE;
                    }
                }
                transitionTo(this.mCanceledState);
                return;
            }
            FtHttpOutgoingMessage ftHttpOutgoingMessage4 = FtHttpOutgoingMessage.this;
            if (ftHttpOutgoingMessage4.mFileSize > ftHttpOutgoingMessage4.mConfig.getSlmMaxMsgSize()) {
                FtHttpOutgoingMessage ftHttpOutgoingMessage5 = FtHttpOutgoingMessage.this;
                if (!ftHttpOutgoingMessage5.mIsResizable || !ftHttpOutgoingMessage5.getRcsStrategy().boolSetting(RcsPolicySettings.RcsPolicy.SUPPORT_LARGE_MSG_RESIZING)) {
                    String r0 = FtHttpOutgoingMessage.LOG_TAG;
                    Log.i(r0, "File size is greater than allowed MaxSlmSize mFileSize:" + FtHttpOutgoingMessage.this.mFileSize + ", SLMMaxMsgSize:" + FtHttpOutgoingMessage.this.mConfig.getSlmMaxMsgSize());
                    if (FtHttpOutgoingMessage.this.mConfig.getFtFallbackAllFail()) {
                        FtHttpOutgoingMessage ftHttpOutgoingMessage6 = FtHttpOutgoingMessage.this;
                        if (!ftHttpOutgoingMessage6.mIsGroupChat) {
                            ftHttpOutgoingMessage6.mCancelReason = CancelReason.REMOTE_TEMPORARILY_UNAVAILABLE;
                        }
                    }
                    transitionTo(this.mCanceledState);
                    return;
                }
                FtHttpOutgoingMessage.this.mIsSlmSvcMsg = true;
                logi("request resizing for LMM");
                FtHttpOutgoingMessage ftHttpOutgoingMessage7 = FtHttpOutgoingMessage.this;
                ftHttpOutgoingMessage7.mListener.onFileResizingNeeded(ftHttpOutgoingMessage7, ftHttpOutgoingMessage7.mConfig.getSlmMaxMsgSize());
                transitionTo(this.mSendingState);
                return;
            }
            FtHttpOutgoingMessage ftHttpOutgoingMessage8 = FtHttpOutgoingMessage.this;
            ftHttpOutgoingMessage8.mIsSlmSvcMsg = true;
            Context context = ftHttpOutgoingMessage8.mModule.getContext();
            FtHttpOutgoingMessage ftHttpOutgoingMessage9 = FtHttpOutgoingMessage.this;
            ftHttpOutgoingMessage8.mFilePath = FileUtils.copyFileToCacheFromUri(context, ftHttpOutgoingMessage9.mFileName, ftHttpOutgoingMessage9.mContentUri);
            if (FtHttpOutgoingMessage.this.sendSlmFile(obtainMessage(12))) {
                FtHttpOutgoingMessage.this.mCancelReason = CancelReason.UNKNOWN;
                transitionTo(this.mSendingState);
                return;
            }
            if (FtHttpOutgoingMessage.this.mConfig.getFtFallbackAllFail()) {
                FtHttpOutgoingMessage ftHttpOutgoingMessage10 = FtHttpOutgoingMessage.this;
                if (!ftHttpOutgoingMessage10.mIsGroupChat) {
                    ftHttpOutgoingMessage10.mCancelReason = CancelReason.REMOTE_TEMPORARILY_UNAVAILABLE;
                }
            }
            transitionTo(this.mCanceledState);
        }

        /* access modifiers changed from: private */
        public void handleTransferProgress(FtTransferProgressEvent ftTransferProgressEvent) {
            if (!Objects.equals(FtHttpOutgoingMessage.this.mRawHandle, ftTransferProgressEvent.mRawHandle)) {
                logi("EVENT_TRANSFER_PROGRESS: unknown rawHandle, ignore it: mRawHandle=" + FtHttpOutgoingMessage.this.mRawHandle + ", event.mRawHandle=" + ftTransferProgressEvent.mRawHandle);
                return;
            }
            removeMessages(23);
            sendMessageDelayed(obtainMessage(23), 300000);
            int i = AnonymousClass1.$SwitchMap$com$sec$internal$constants$ims$servicemodules$im$event$FtTransferProgressEvent$State[ftTransferProgressEvent.mState.ordinal()];
            if (i == 1) {
                FtHttpOutgoingMessage.this.updateTransferredBytes(ftTransferProgressEvent.mTransferred);
                FtHttpOutgoingMessage ftHttpOutgoingMessage = FtHttpOutgoingMessage.this;
                ftHttpOutgoingMessage.mListener.onTransferProgressReceived(ftHttpOutgoingMessage);
            } else if (i == 2) {
                if (getCurrentState() == this.mInProgressState) {
                    FtHttpOutgoingMessage.this.mCancelReason = FtMessage.translateToCancelReason(ftTransferProgressEvent.mReason.getImError());
                }
                if (FtHttpOutgoingMessage.this.mConfig.getFtFallbackAllFail()) {
                    FtHttpOutgoingMessage ftHttpOutgoingMessage2 = FtHttpOutgoingMessage.this;
                    if (!ftHttpOutgoingMessage2.mIsGroupChat) {
                        ftHttpOutgoingMessage2.mCancelReason = CancelReason.REMOTE_TEMPORARILY_UNAVAILABLE;
                    }
                }
                transitionTo(this.mCanceledState);
            } else if (i == 3) {
                transitionTo(this.mCompletedState);
            } else if (i == 4) {
                FtHttpOutgoingMessage ftHttpOutgoingMessage3 = FtHttpOutgoingMessage.this;
                ftHttpOutgoingMessage3.mTransferredBytes = (ftHttpOutgoingMessage3.mFileSize - ftTransferProgressEvent.mTotal) + ftTransferProgressEvent.mTransferred;
                logi("INTERRUPTED mFileSize: " + FtHttpOutgoingMessage.this.mFileSize + " mTotal: " + ftTransferProgressEvent.mTotal + " mTransferred: " + ftTransferProgressEvent.mTransferred);
                if (getCurrentState() == this.mInProgressState) {
                    FtHttpOutgoingMessage.this.mCancelReason = FtMessage.translateToCancelReason(ftTransferProgressEvent.mReason.getImError());
                }
                if (FtHttpOutgoingMessage.this.mConfig.getFtFallbackAllFail()) {
                    FtHttpOutgoingMessage ftHttpOutgoingMessage4 = FtHttpOutgoingMessage.this;
                    if (!ftHttpOutgoingMessage4.mIsGroupChat) {
                        ftHttpOutgoingMessage4.mCancelReason = CancelReason.REMOTE_TEMPORARILY_UNAVAILABLE;
                    }
                }
                transitionTo(this.mCanceledState);
            }
        }

        /* access modifiers changed from: protected */
        public int getStateId() {
            Integer translate = this.mStateTranslator.translate(getCurrentState());
            if (translate == null) {
                return 0;
            }
            return translate.intValue();
        }

        /* access modifiers changed from: protected */
        public State getState(Integer num) {
            return this.mDbStateTranslator.translate(num);
        }

        private final class DefaultState extends State {
            private DefaultState() {
            }

            public boolean processMessage(Message message) {
                int i = message.what;
                if (FtHttpStateMachine.this.getCurrentState() == null) {
                    return false;
                }
                String r0 = FtHttpOutgoingMessage.LOG_TAG;
                Log.e(r0, "Unexpected event, current state is " + FtHttpStateMachine.this.getCurrentState().getName() + " event: " + message.what);
                return false;
            }
        }

        private final class InitialState extends State {
            private InitialState() {
            }

            public boolean processMessage(Message message) {
                int i = message.what;
                if (i == 1) {
                    long max = Math.max(FtHttpOutgoingMessage.this.mConfig.getMaxSizeExtraFileTr(), FtHttpOutgoingMessage.this.mConfig.getMaxSizeFileTr());
                    if (max == 0 || FtHttpOutgoingMessage.this.mFileSize <= max) {
                        FtHttpOutgoingMessage ftHttpOutgoingMessage = FtHttpOutgoingMessage.this;
                        ftHttpOutgoingMessage.mListener.onTransferCreated(ftHttpOutgoingMessage);
                        FtHttpStateMachine ftHttpStateMachine = FtHttpStateMachine.this;
                        ftHttpStateMachine.transitionTo(ftHttpStateMachine.mAttachedState);
                        return true;
                    }
                    String r7 = FtHttpOutgoingMessage.LOG_TAG;
                    Log.e(r7, "Attached file (" + FtHttpOutgoingMessage.this.mFileSize + ") exceeds MaxSizeFileTr (" + max + ")");
                    FtHttpStateMachine ftHttpStateMachine2 = FtHttpStateMachine.this;
                    FtHttpOutgoingMessage.this.mCancelReason = CancelReason.TOO_LARGE;
                    ftHttpStateMachine2.transitionTo(ftHttpStateMachine2.mCanceledState);
                    return true;
                } else if (i == 8) {
                    FtHttpStateMachine ftHttpStateMachine3 = FtHttpStateMachine.this;
                    FtHttpOutgoingMessage.this.mCancelReason = (CancelReason) message.obj;
                    ftHttpStateMachine3.transitionTo(ftHttpStateMachine3.mCanceledState);
                    return true;
                } else if (i != 16) {
                    return false;
                } else {
                    FtHttpStateMachine ftHttpStateMachine4 = FtHttpStateMachine.this;
                    FtHttpOutgoingMessage.this.mCancelReason = CancelReason.ERROR;
                    ftHttpStateMachine4.handleFTHttpFailure();
                    return true;
                }
            }
        }

        private final class AttachedState extends State {
            private AttachedState() {
            }

            public void enter() {
                String r0 = FtHttpOutgoingMessage.LOG_TAG;
                Log.i(r0, "AttachedState enter msgId : " + FtHttpOutgoingMessage.this.mId);
                FtHttpOutgoingMessage.this.updateState();
            }

            public boolean processMessage(Message message) {
                if (message.what != 11) {
                    return false;
                }
                FtHttpStateMachine ftHttpStateMachine = FtHttpStateMachine.this;
                ftHttpStateMachine.transitionTo(ftHttpStateMachine.mInProgressState);
                return true;
            }
        }

        final class SendingState extends State {
            SendingState() {
            }

            public void enter() {
                String r0 = FtHttpOutgoingMessage.LOG_TAG;
                Log.i(r0, "SendingState enter msgId : " + FtHttpOutgoingMessage.this.mId);
                FtHttpOutgoingMessage.this.updateStatus(ImConstants.Status.SENDING);
                FtHttpOutgoingMessage.this.updateState();
                FtHttpOutgoingMessage.this.acquireWakeLock();
            }

            public boolean processMessage(Message message) {
                int i = message.what;
                if (i == 8) {
                    FtHttpOutgoingMessage ftHttpOutgoingMessage = FtHttpOutgoingMessage.this;
                    CancelReason cancelReason = (CancelReason) message.obj;
                    ftHttpOutgoingMessage.mCancelReason = cancelReason;
                    if (ftHttpOutgoingMessage.mRawHandle == null) {
                        Log.i(FtHttpOutgoingMessage.LOG_TAG, "mRawHandle is null");
                        FtHttpStateMachine ftHttpStateMachine = FtHttpStateMachine.this;
                        ftHttpStateMachine.transitionTo(ftHttpStateMachine.mCanceledState);
                    } else {
                        ftHttpOutgoingMessage.sendCancelFtSession(cancelReason);
                        FtHttpStateMachine ftHttpStateMachine2 = FtHttpStateMachine.this;
                        ftHttpStateMachine2.transitionTo(ftHttpStateMachine2.mCancelingState);
                    }
                } else if (i == 12) {
                    AsyncResult asyncResult = (AsyncResult) message.obj;
                    if (asyncResult.exception == null) {
                        FtResult ftResult = (FtResult) asyncResult.result;
                        FtHttpStateMachine ftHttpStateMachine3 = FtHttpStateMachine.this;
                        ftHttpStateMachine3.logi("SLM send file done : " + ftResult.mRawHandle);
                        if (ftResult.getImError() == ImError.SUCCESS) {
                            FtHttpStateMachine ftHttpStateMachine4 = FtHttpStateMachine.this;
                            FtHttpOutgoingMessage.this.mRawHandle = ftResult.mRawHandle;
                            ftHttpStateMachine4.transitionTo(ftHttpStateMachine4.mInProgressState);
                        } else {
                            if (FtHttpOutgoingMessage.this.mConfig.getFtFallbackAllFail()) {
                                FtHttpOutgoingMessage ftHttpOutgoingMessage2 = FtHttpOutgoingMessage.this;
                                if (!ftHttpOutgoingMessage2.mIsGroupChat) {
                                    ftHttpOutgoingMessage2.mCancelReason = CancelReason.REMOTE_TEMPORARILY_UNAVAILABLE;
                                }
                            }
                            FtHttpStateMachine ftHttpStateMachine5 = FtHttpStateMachine.this;
                            ftHttpStateMachine5.transitionTo(ftHttpStateMachine5.mCanceledState);
                        }
                    }
                } else if (i != 20) {
                    return false;
                } else {
                    FileResizeResponse fileResizeResponse = (FileResizeResponse) message.obj;
                    if (FtHttpOutgoingMessage.this.validateFileResizeResponse(fileResizeResponse)) {
                        File file = new File(fileResizeResponse.resizedFilePath);
                        FtHttpOutgoingMessage.this.mFileSize = file.length();
                        FtHttpOutgoingMessage.this.mFileName = file.getName();
                        FtHttpOutgoingMessage ftHttpOutgoingMessage3 = FtHttpOutgoingMessage.this;
                        ftHttpOutgoingMessage3.mFilePath = fileResizeResponse.resizedFilePath;
                        ftHttpOutgoingMessage3.triggerObservers(ImCacheAction.UPDATED);
                        FtHttpStateMachine ftHttpStateMachine6 = FtHttpStateMachine.this;
                        if (!FtHttpOutgoingMessage.this.sendSlmFile(ftHttpStateMachine6.obtainMessage(12))) {
                            FtHttpStateMachine ftHttpStateMachine7 = FtHttpStateMachine.this;
                            ftHttpStateMachine7.transitionTo(ftHttpStateMachine7.mCanceledState);
                        }
                    } else {
                        if (FtHttpOutgoingMessage.this.mConfig.getFtFallbackAllFail()) {
                            FtHttpOutgoingMessage ftHttpOutgoingMessage4 = FtHttpOutgoingMessage.this;
                            if (!ftHttpOutgoingMessage4.mIsGroupChat) {
                                ftHttpOutgoingMessage4.mCancelReason = CancelReason.REMOTE_TEMPORARILY_UNAVAILABLE;
                            }
                        }
                        FtHttpStateMachine ftHttpStateMachine8 = FtHttpStateMachine.this;
                        ftHttpStateMachine8.transitionTo(ftHttpStateMachine8.mCanceledState);
                    }
                }
                return true;
            }
        }

        private final class InProgressState extends State {
            UploadFileTask uploadTask;

            private InProgressState() {
            }

            public void enter() {
                String r0 = FtHttpOutgoingMessage.LOG_TAG;
                Log.i(r0, "InProgressState enter msgId : " + FtHttpOutgoingMessage.this.mId);
                FtHttpStateMachine.this.removeMessages(305);
                FtHttpOutgoingMessage.this.setRetryCount(0);
                FtHttpOutgoingMessage.this.updateState();
                FtHttpOutgoingMessage ftHttpOutgoingMessage = FtHttpOutgoingMessage.this;
                if (ftHttpOutgoingMessage.mIsBootup && (ftHttpOutgoingMessage.getRcsStrategy().boolSetting(RcsPolicySettings.RcsPolicy.CANCEL_FOR_DEREGI_PROMPTLY) || FtHttpOutgoingMessage.this.getRcsStrategy().intSetting(RcsPolicySettings.RcsPolicy.DELAY_TO_CANCEL_FOR_DEREGI) > 0)) {
                    Log.i(FtHttpOutgoingMessage.LOG_TAG, "Do not auto resume message loaded from bootup");
                    FtHttpStateMachine ftHttpStateMachine = FtHttpStateMachine.this;
                    FtHttpOutgoingMessage ftHttpOutgoingMessage2 = FtHttpOutgoingMessage.this;
                    ftHttpOutgoingMessage2.mIsBootup = false;
                    ftHttpOutgoingMessage2.mCancelReason = CancelReason.DEVICE_UNREGISTERED;
                    ftHttpStateMachine.transitionTo(ftHttpStateMachine.mCanceledState);
                } else if (!FtHttpOutgoingMessage.this.mIsSlmSvcMsg) {
                    tryUpload();
                }
                FtHttpOutgoingMessage.this.acquireWakeLock();
            }

            public boolean processMessage(Message message) {
                int i = message.what;
                if (i != 1) {
                    if (i == 3) {
                        FtHttpStateMachine.this.handleTransferProgress((FtTransferProgressEvent) message.obj);
                        return true;
                    } else if (i == 8) {
                        handleCancelTransfer((CancelReason) message.obj);
                        return true;
                    } else if (i != 11) {
                        if (i == 23) {
                            FtHttpStateMachine ftHttpStateMachine = FtHttpStateMachine.this;
                            ftHttpStateMachine.logi("EVENT_TRANSFER_TIMER_TIMEOUT : " + FtHttpOutgoingMessage.this.mId);
                            FtHttpOutgoingMessage.this.cancelTransfer(CancelReason.CANCELED_BY_SYSTEM);
                            return true;
                        } else if (i != 201) {
                            switch (i) {
                                case 50:
                                    FtHttpStateMachine.this.removeMessages(51);
                                    tryUpload();
                                    return true;
                                case 51:
                                    FtHttpStateMachine ftHttpStateMachine2 = FtHttpStateMachine.this;
                                    ftHttpStateMachine2.sendMessage(ftHttpStateMachine2.obtainMessage(303, -1, -1, CancelReason.ERROR));
                                    return true;
                                case 52:
                                    String r5 = FtHttpOutgoingMessage.LOG_TAG;
                                    Log.i(r5, "EVENT_DELAY_CANCEL_TRANSFER mId=" + FtHttpOutgoingMessage.this.mId);
                                    UploadFileTask uploadFileTask = this.uploadTask;
                                    if (uploadFileTask != null) {
                                        uploadFileTask.cancel(true);
                                        this.uploadTask = null;
                                    }
                                    FtHttpStateMachine ftHttpStateMachine3 = FtHttpStateMachine.this;
                                    FtHttpOutgoingMessage.this.mCancelReason = CancelReason.CANCELED_BY_USER;
                                    ftHttpStateMachine3.transitionTo(ftHttpStateMachine3.mCanceledState);
                                    return true;
                                default:
                                    switch (i) {
                                        case 302:
                                            handleUploadCompleted((String) message.obj);
                                            return true;
                                        case 303:
                                            handleUploadCanceled(message);
                                            return true;
                                        case 304:
                                            return true;
                                        case 305:
                                            handleRetryUpload(message.arg2);
                                            return true;
                                        default:
                                            return false;
                                    }
                            }
                        } else {
                            FtHttpOutgoingMessage.this.updateTransferredBytes(((Long) message.obj).longValue());
                            String r52 = FtHttpOutgoingMessage.LOG_TAG;
                            Log.i(r52, "EVENT_UPLOAD_PROGRESS " + FtHttpOutgoingMessage.this.mTransferredBytes + "/" + FtHttpOutgoingMessage.this.mFileSize);
                            FtHttpOutgoingMessage ftHttpOutgoingMessage = FtHttpOutgoingMessage.this;
                            ftHttpOutgoingMessage.mListener.onTransferProgressReceived(ftHttpOutgoingMessage);
                            return true;
                        }
                    }
                }
                FtHttpStateMachine.this.removeMessages(305);
                PreciseAlarmManager.getInstance(FtHttpOutgoingMessage.this.getContext()).removeMessage(FtHttpStateMachine.this.obtainMessage(52));
                tryUpload();
                return true;
            }

            private void handleCancelTransfer(CancelReason cancelReason) {
                String r0 = FtHttpOutgoingMessage.LOG_TAG;
                Log.i(r0, "EVENT_CANCEL_TRANSFER " + FtHttpOutgoingMessage.this.mId + " CancelReason " + cancelReason);
                FtHttpStateMachine.this.removeMessages(305);
                PreciseAlarmManager.getInstance(FtHttpOutgoingMessage.this.getContext()).removeMessage(FtHttpStateMachine.this.obtainMessage(52));
                FtHttpOutgoingMessage ftHttpOutgoingMessage = FtHttpOutgoingMessage.this;
                if (ftHttpOutgoingMessage.mIsSlmSvcMsg) {
                    ftHttpOutgoingMessage.mCancelReason = cancelReason;
                    ftHttpOutgoingMessage.sendCancelFtSession(cancelReason);
                    FtHttpStateMachine ftHttpStateMachine = FtHttpStateMachine.this;
                    if (FtHttpOutgoingMessage.this.mCancelReason == CancelReason.CANCELED_BY_SYSTEM) {
                        ftHttpStateMachine.transitionTo(ftHttpStateMachine.mCanceledState);
                    } else {
                        ftHttpStateMachine.transitionTo(ftHttpStateMachine.mCancelingState);
                    }
                } else {
                    UploadFileTask uploadFileTask = this.uploadTask;
                    if (uploadFileTask != null) {
                        uploadFileTask.cancel(true);
                        HttpRequest httpRequest = this.uploadTask.mHttpRequest;
                        if (httpRequest != null) {
                            httpRequest.disconnect();
                        }
                        this.uploadTask = null;
                    }
                    if (cancelReason != CancelReason.DEVICE_UNREGISTERED || FtHttpOutgoingMessage.this.getRcsStrategy().boolSetting(RcsPolicySettings.RcsPolicy.CANCEL_FOR_DEREGI_PROMPTLY)) {
                        FtHttpStateMachine ftHttpStateMachine2 = FtHttpStateMachine.this;
                        FtHttpOutgoingMessage.this.mCancelReason = cancelReason;
                        ftHttpStateMachine2.transitionTo(ftHttpStateMachine2.mCanceledState);
                        return;
                    }
                    int intSetting = FtHttpOutgoingMessage.this.getRcsStrategy().intSetting(RcsPolicySettings.RcsPolicy.DELAY_TO_CANCEL_FOR_DEREGI);
                    if (intSetting > 0) {
                        PreciseAlarmManager.getInstance(FtHttpOutgoingMessage.this.getContext()).sendMessageDelayed(InProgressState.class.getSimpleName(), FtHttpStateMachine.this.obtainMessage(52), ((long) intSetting) * 1000);
                    }
                    FtHttpOutgoingMessage ftHttpOutgoingMessage2 = FtHttpOutgoingMessage.this;
                    IMnoStrategy rcsStrategy = ftHttpOutgoingMessage2.getRcsStrategy();
                    FtHttpOutgoingMessage ftHttpOutgoingMessage3 = FtHttpOutgoingMessage.this;
                    ftHttpOutgoingMessage2.updateResumeableOptionCode(rcsStrategy.getftResumableOption(cancelReason, ftHttpOutgoingMessage3.mIsGroupChat, ftHttpOutgoingMessage3.mDirection, ftHttpOutgoingMessage3.getTransferMech()).getId());
                }
            }

            private void handleRetryUpload(int i) {
                String r0 = FtHttpOutgoingMessage.LOG_TAG;
                Log.i(r0, "EVENT_RETRY_UPLOAD mId=" + FtHttpOutgoingMessage.this.mId + ", error: " + i + "Retry count=" + FtHttpOutgoingMessage.this.getRetryCount() + "/" + 3);
                FtHttpStateMachine.this.removeMessages(305);
                if (!FtHttpOutgoingMessage.this.mMnoStrategy.isFTHTTPAutoResumeAndCancelPerConnectionChange() || FtHttpOutgoingMessage.this.checkAvailableRetry()) {
                    if (FtHttpOutgoingMessage.this.getRetryCount() < 3) {
                        FtHttpOutgoingMessage ftHttpOutgoingMessage = FtHttpOutgoingMessage.this;
                        ftHttpOutgoingMessage.setRetryCount(ftHttpOutgoingMessage.getRetryCount() + 1);
                        tryUpload();
                        return;
                    }
                    if (i > 0) {
                        FtHttpOutgoingMessage.this.mTransferredBytes = 0;
                    }
                    FtHttpStateMachine.this.handleFTHttpFailure();
                } else if (FtHttpOutgoingMessage.this.getRcsStrategy().boolSetting(RcsPolicySettings.RcsPolicy.CANCEL_FOR_DEREGI_PROMPTLY)) {
                    FtHttpStateMachine ftHttpStateMachine = FtHttpStateMachine.this;
                    FtHttpOutgoingMessage.this.mCancelReason = CancelReason.CANCELED_BY_USER;
                    ftHttpStateMachine.transitionTo(ftHttpStateMachine.mCanceledState);
                }
            }

            /* JADX WARNING: Removed duplicated region for block: B:16:0x00ba A[Catch:{ IOException | NullPointerException | XmlPullParserException -> 0x013a }] */
            /* Code decompiled incorrectly, please refer to instructions dump. */
            private void handleUploadCompleted(java.lang.String r8) {
                /*
                    r7 = this;
                    boolean r0 = android.text.TextUtils.isEmpty(r8)
                    if (r0 == 0) goto L_0x000c
                    com.sec.internal.ims.servicemodules.im.FtHttpOutgoingMessage$FtHttpStateMachine r7 = com.sec.internal.ims.servicemodules.im.FtHttpOutgoingMessage.FtHttpStateMachine.this
                    r7.handleFTHttpFailure()
                    return
                L_0x000c:
                    com.sec.internal.ims.servicemodules.im.FtHttpOutgoingMessage$FtHttpStateMachine r0 = com.sec.internal.ims.servicemodules.im.FtHttpOutgoingMessage.FtHttpStateMachine.this
                    com.sec.internal.ims.servicemodules.im.FtHttpOutgoingMessage r0 = com.sec.internal.ims.servicemodules.im.FtHttpOutgoingMessage.this
                    r0.mBody = r8
                    java.lang.String r1 = "application/vnd.gsma.rcs-ft-http+xml"
                    r0.mContentType = r1
                    r1 = 0
                    r0.mFileExpire = r1
                    java.lang.String r0 = com.sec.internal.ims.servicemodules.im.FtHttpOutgoingMessage.LOG_TAG
                    java.lang.StringBuilder r1 = new java.lang.StringBuilder
                    r1.<init>()
                    java.lang.String r2 = "EVENT_UPLOAD_COMPLETED Result = "
                    r1.append(r2)
                    r1.append(r8)
                    java.lang.String r8 = r1.toString()
                    android.util.Log.d(r0, r8)
                    com.sec.internal.ims.servicemodules.im.FtHttpOutgoingMessage$FtHttpStateMachine r8 = com.sec.internal.ims.servicemodules.im.FtHttpOutgoingMessage.FtHttpStateMachine.this     // Catch:{ IOException | NullPointerException | XmlPullParserException -> 0x013a }
                    com.sec.internal.ims.servicemodules.im.FtHttpOutgoingMessage r8 = com.sec.internal.ims.servicemodules.im.FtHttpOutgoingMessage.this     // Catch:{ IOException | NullPointerException | XmlPullParserException -> 0x013a }
                    java.lang.String r8 = r8.mBody     // Catch:{ IOException | NullPointerException | XmlPullParserException -> 0x013a }
                    com.sec.internal.constants.ims.servicemodules.im.FtHttpFileInfo r8 = com.sec.internal.ims.servicemodules.im.util.FtHttpXmlParser.parse(r8)     // Catch:{ IOException | NullPointerException | XmlPullParserException -> 0x013a }
                    if (r8 == 0) goto L_0x011b
                    com.sec.internal.ims.servicemodules.im.FtHttpOutgoingMessage$FtHttpStateMachine r0 = com.sec.internal.ims.servicemodules.im.FtHttpOutgoingMessage.FtHttpStateMachine.this     // Catch:{ IOException | NullPointerException | XmlPullParserException -> 0x013a }
                    com.sec.internal.ims.servicemodules.im.FtHttpOutgoingMessage r0 = com.sec.internal.ims.servicemodules.im.FtHttpOutgoingMessage.this     // Catch:{ IOException | NullPointerException | XmlPullParserException -> 0x013a }
                    java.lang.String r1 = r8.getBrandedUrl()     // Catch:{ IOException | NullPointerException | XmlPullParserException -> 0x013a }
                    r0.mFileBrandedUrl = r1     // Catch:{ IOException | NullPointerException | XmlPullParserException -> 0x013a }
                    com.sec.internal.ims.servicemodules.im.FtHttpOutgoingMessage$FtHttpStateMachine r0 = com.sec.internal.ims.servicemodules.im.FtHttpOutgoingMessage.FtHttpStateMachine.this     // Catch:{ IOException | NullPointerException | XmlPullParserException -> 0x013a }
                    com.sec.internal.ims.servicemodules.im.FtHttpOutgoingMessage r0 = com.sec.internal.ims.servicemodules.im.FtHttpOutgoingMessage.this     // Catch:{ IOException | NullPointerException | XmlPullParserException -> 0x013a }
                    java.lang.String r1 = r8.getDataUntil()     // Catch:{ IOException | NullPointerException | XmlPullParserException -> 0x013a }
                    r0.mFileExpire = r1     // Catch:{ IOException | NullPointerException | XmlPullParserException -> 0x013a }
                    com.sec.internal.ims.servicemodules.im.FtHttpOutgoingMessage$FtHttpStateMachine r0 = com.sec.internal.ims.servicemodules.im.FtHttpOutgoingMessage.FtHttpStateMachine.this     // Catch:{ IOException | NullPointerException | XmlPullParserException -> 0x013a }
                    com.sec.internal.ims.servicemodules.im.FtHttpOutgoingMessage r0 = com.sec.internal.ims.servicemodules.im.FtHttpOutgoingMessage.this     // Catch:{ IOException | NullPointerException | XmlPullParserException -> 0x013a }
                    java.net.URL r1 = r8.getDataUrl()     // Catch:{ IOException | NullPointerException | XmlPullParserException -> 0x013a }
                    java.lang.String r1 = r1.toString()     // Catch:{ IOException | NullPointerException | XmlPullParserException -> 0x013a }
                    r0.mFileDataUrl = r1     // Catch:{ IOException | NullPointerException | XmlPullParserException -> 0x013a }
                    com.sec.internal.ims.servicemodules.im.FtHttpOutgoingMessage$FtHttpStateMachine r0 = com.sec.internal.ims.servicemodules.im.FtHttpOutgoingMessage.FtHttpStateMachine.this     // Catch:{ IOException | NullPointerException | XmlPullParserException -> 0x013a }
                    com.sec.internal.ims.servicemodules.im.FtHttpOutgoingMessage r0 = com.sec.internal.ims.servicemodules.im.FtHttpOutgoingMessage.this     // Catch:{ IOException | NullPointerException | XmlPullParserException -> 0x013a }
                    java.lang.String r0 = r0.mFileDataUrl     // Catch:{ IOException | NullPointerException | XmlPullParserException -> 0x013a }
                    boolean r0 = com.sec.internal.ims.servicemodules.im.util.FtFallbackHttpUrlUtil.areFallbackParamsPresent(r0)     // Catch:{ IOException | NullPointerException | XmlPullParserException -> 0x013a }
                    com.sec.internal.constants.ims.servicemodules.im.FileDisposition r1 = com.sec.internal.constants.ims.servicemodules.im.FileDisposition.RENDER     // Catch:{ IOException | NullPointerException | XmlPullParserException -> 0x013a }
                    com.sec.internal.ims.servicemodules.im.FtHttpOutgoingMessage$FtHttpStateMachine r2 = com.sec.internal.ims.servicemodules.im.FtHttpOutgoingMessage.FtHttpStateMachine.this     // Catch:{ IOException | NullPointerException | XmlPullParserException -> 0x013a }
                    com.sec.internal.ims.servicemodules.im.FtHttpOutgoingMessage r2 = com.sec.internal.ims.servicemodules.im.FtHttpOutgoingMessage.this     // Catch:{ IOException | NullPointerException | XmlPullParserException -> 0x013a }
                    com.sec.internal.constants.ims.servicemodules.im.FileDisposition r3 = r2.mFileDisposition     // Catch:{ IOException | NullPointerException | XmlPullParserException -> 0x013a }
                    if (r1 != r3) goto L_0x00a9
                    android.content.Context r1 = r2.getContext()     // Catch:{ IOException | NullPointerException | XmlPullParserException -> 0x013a }
                    com.sec.internal.ims.servicemodules.im.FtHttpOutgoingMessage$FtHttpStateMachine r3 = com.sec.internal.ims.servicemodules.im.FtHttpOutgoingMessage.FtHttpStateMachine.this     // Catch:{ IOException | NullPointerException | XmlPullParserException -> 0x013a }
                    com.sec.internal.ims.servicemodules.im.FtHttpOutgoingMessage r3 = com.sec.internal.ims.servicemodules.im.FtHttpOutgoingMessage.this     // Catch:{ IOException | NullPointerException | XmlPullParserException -> 0x013a }
                    android.net.Uri r3 = r3.mContentUri     // Catch:{ IOException | NullPointerException | XmlPullParserException -> 0x013a }
                    int r1 = com.sec.internal.ims.servicemodules.im.util.FileDurationUtil.getFileDurationTime(r1, r3)     // Catch:{ IOException | NullPointerException | XmlPullParserException -> 0x013a }
                    int r1 = r1 / 1000
                    r2.mPlayingLength = r1     // Catch:{ IOException | NullPointerException | XmlPullParserException -> 0x013a }
                    com.sec.internal.ims.servicemodules.im.FtHttpOutgoingMessage$FtHttpStateMachine r1 = com.sec.internal.ims.servicemodules.im.FtHttpOutgoingMessage.FtHttpStateMachine.this     // Catch:{ IOException | NullPointerException | XmlPullParserException -> 0x013a }
                    com.sec.internal.ims.servicemodules.im.FtHttpOutgoingMessage r1 = com.sec.internal.ims.servicemodules.im.FtHttpOutgoingMessage.this     // Catch:{ IOException | NullPointerException | XmlPullParserException -> 0x013a }
                    int r1 = r1.mPlayingLength     // Catch:{ IOException | NullPointerException | XmlPullParserException -> 0x013a }
                    if (r1 < 0) goto L_0x00a9
                    java.lang.String r1 = com.sec.internal.ims.servicemodules.im.FtHttpOutgoingMessage.LOG_TAG     // Catch:{ IOException | NullPointerException | XmlPullParserException -> 0x013a }
                    java.lang.String r2 = "Assumed that Audio Message is being sent!"
                    android.util.Log.w(r1, r2)     // Catch:{ IOException | NullPointerException | XmlPullParserException -> 0x013a }
                    com.sec.internal.ims.servicemodules.im.util.FtHttpXmlComposer r1 = new com.sec.internal.ims.servicemodules.im.util.FtHttpXmlComposer     // Catch:{ IOException | NullPointerException | XmlPullParserException -> 0x013a }
                    r1.<init>()     // Catch:{ IOException | NullPointerException | XmlPullParserException -> 0x013a }
                    com.sec.internal.ims.servicemodules.im.FtHttpOutgoingMessage$FtHttpStateMachine r2 = com.sec.internal.ims.servicemodules.im.FtHttpOutgoingMessage.FtHttpStateMachine.this     // Catch:{ IOException | NullPointerException | XmlPullParserException -> 0x013a }
                    com.sec.internal.ims.servicemodules.im.FtHttpOutgoingMessage r2 = com.sec.internal.ims.servicemodules.im.FtHttpOutgoingMessage.this     // Catch:{ IOException | NullPointerException | XmlPullParserException -> 0x013a }
                    int r3 = r2.mPlayingLength     // Catch:{ IOException | NullPointerException | XmlPullParserException -> 0x013a }
                    java.lang.String r1 = r1.composeXmlForAudioMessage(r8, r3)     // Catch:{ IOException | NullPointerException | XmlPullParserException -> 0x013a }
                    r2.mBody = r1     // Catch:{ IOException | NullPointerException | XmlPullParserException -> 0x013a }
                    r1 = 1
                    goto L_0x00aa
                L_0x00a9:
                    r1 = 0
                L_0x00aa:
                    com.sec.internal.ims.servicemodules.im.FtHttpOutgoingMessage$FtHttpStateMachine r2 = com.sec.internal.ims.servicemodules.im.FtHttpOutgoingMessage.FtHttpStateMachine.this     // Catch:{ IOException | NullPointerException | XmlPullParserException -> 0x013a }
                    com.sec.internal.ims.servicemodules.im.FtHttpOutgoingMessage r2 = com.sec.internal.ims.servicemodules.im.FtHttpOutgoingMessage.this     // Catch:{ IOException | NullPointerException | XmlPullParserException -> 0x013a }
                    com.sec.internal.ims.servicemodules.im.ImConfig r2 = r2.mConfig     // Catch:{ IOException | NullPointerException | XmlPullParserException -> 0x013a }
                    java.lang.String r2 = r2.getRcsProfile()     // Catch:{ IOException | NullPointerException | XmlPullParserException -> 0x013a }
                    boolean r2 = com.sec.ims.settings.ImsProfile.isRcsUpProfile(r2)     // Catch:{ IOException | NullPointerException | XmlPullParserException -> 0x013a }
                    if (r2 == 0) goto L_0x011b
                    if (r0 != 0) goto L_0x00f4
                    java.lang.String r0 = com.sec.internal.ims.servicemodules.im.FtHttpOutgoingMessage.LOG_TAG     // Catch:{ IOException | NullPointerException | XmlPullParserException -> 0x013a }
                    java.lang.String r2 = "Fallback params are not present in the content URL returned from fthttp content server!"
                    android.util.Log.i(r0, r2)     // Catch:{ IOException | NullPointerException | XmlPullParserException -> 0x013a }
                    com.sec.internal.ims.servicemodules.im.FtHttpOutgoingMessage$FtHttpStateMachine r0 = com.sec.internal.ims.servicemodules.im.FtHttpOutgoingMessage.FtHttpStateMachine.this     // Catch:{ IOException | NullPointerException | XmlPullParserException -> 0x013a }
                    com.sec.internal.ims.servicemodules.im.FtHttpOutgoingMessage r0 = com.sec.internal.ims.servicemodules.im.FtHttpOutgoingMessage.this     // Catch:{ IOException | NullPointerException | XmlPullParserException -> 0x013a }
                    java.lang.String r2 = r0.mFileDataUrl     // Catch:{ IOException | NullPointerException | XmlPullParserException -> 0x013a }
                    long r3 = r8.getFileSize()     // Catch:{ IOException | NullPointerException | XmlPullParserException -> 0x013a }
                    java.lang.String r5 = r8.getContentType()     // Catch:{ IOException | NullPointerException | XmlPullParserException -> 0x013a }
                    java.lang.String r6 = r8.getDataUntil()     // Catch:{ IOException | NullPointerException | XmlPullParserException -> 0x013a }
                    java.lang.String r2 = com.sec.internal.ims.servicemodules.im.util.FtFallbackHttpUrlUtil.addFtFallbackParams(r2, r3, r5, r6)     // Catch:{ IOException | NullPointerException | XmlPullParserException -> 0x013a }
                    r0.mFileDataUrl = r2     // Catch:{ IOException | NullPointerException | XmlPullParserException -> 0x013a }
                    com.sec.internal.constants.ims.servicemodules.im.FtHttpFileInfo$Data r0 = new com.sec.internal.constants.ims.servicemodules.im.FtHttpFileInfo$Data     // Catch:{ IOException | NullPointerException | XmlPullParserException -> 0x013a }
                    java.net.URL r2 = new java.net.URL     // Catch:{ IOException | NullPointerException | XmlPullParserException -> 0x013a }
                    com.sec.internal.ims.servicemodules.im.FtHttpOutgoingMessage$FtHttpStateMachine r3 = com.sec.internal.ims.servicemodules.im.FtHttpOutgoingMessage.FtHttpStateMachine.this     // Catch:{ IOException | NullPointerException | XmlPullParserException -> 0x013a }
                    com.sec.internal.ims.servicemodules.im.FtHttpOutgoingMessage r3 = com.sec.internal.ims.servicemodules.im.FtHttpOutgoingMessage.this     // Catch:{ IOException | NullPointerException | XmlPullParserException -> 0x013a }
                    java.lang.String r3 = r3.mFileDataUrl     // Catch:{ IOException | NullPointerException | XmlPullParserException -> 0x013a }
                    r2.<init>(r3)     // Catch:{ IOException | NullPointerException | XmlPullParserException -> 0x013a }
                    java.lang.String r3 = r8.getDataUntil()     // Catch:{ IOException | NullPointerException | XmlPullParserException -> 0x013a }
                    r0.<init>(r2, r3)     // Catch:{ IOException | NullPointerException | XmlPullParserException -> 0x013a }
                    r8.setData(r0)     // Catch:{ IOException | NullPointerException | XmlPullParserException -> 0x013a }
                L_0x00f4:
                    if (r1 == 0) goto L_0x011b
                    com.sec.internal.ims.servicemodules.im.FtHttpOutgoingMessage$FtHttpStateMachine r0 = com.sec.internal.ims.servicemodules.im.FtHttpOutgoingMessage.FtHttpStateMachine.this     // Catch:{ IOException | NullPointerException | XmlPullParserException -> 0x013a }
                    com.sec.internal.ims.servicemodules.im.FtHttpOutgoingMessage r0 = com.sec.internal.ims.servicemodules.im.FtHttpOutgoingMessage.this     // Catch:{ IOException | NullPointerException | XmlPullParserException -> 0x013a }
                    java.lang.String r1 = r0.mFileDataUrl     // Catch:{ IOException | NullPointerException | XmlPullParserException -> 0x013a }
                    int r2 = r0.mPlayingLength     // Catch:{ IOException | NullPointerException | XmlPullParserException -> 0x013a }
                    java.lang.String r1 = com.sec.internal.ims.servicemodules.im.util.FtFallbackHttpUrlUtil.addDurationFtFallbackParam(r1, r2)     // Catch:{ IOException | NullPointerException | XmlPullParserException -> 0x013a }
                    r0.mFileDataUrl = r1     // Catch:{ IOException | NullPointerException | XmlPullParserException -> 0x013a }
                    com.sec.internal.constants.ims.servicemodules.im.FtHttpFileInfo$Data r0 = new com.sec.internal.constants.ims.servicemodules.im.FtHttpFileInfo$Data     // Catch:{ IOException | NullPointerException | XmlPullParserException -> 0x013a }
                    java.net.URL r1 = new java.net.URL     // Catch:{ IOException | NullPointerException | XmlPullParserException -> 0x013a }
                    com.sec.internal.ims.servicemodules.im.FtHttpOutgoingMessage$FtHttpStateMachine r2 = com.sec.internal.ims.servicemodules.im.FtHttpOutgoingMessage.FtHttpStateMachine.this     // Catch:{ IOException | NullPointerException | XmlPullParserException -> 0x013a }
                    com.sec.internal.ims.servicemodules.im.FtHttpOutgoingMessage r2 = com.sec.internal.ims.servicemodules.im.FtHttpOutgoingMessage.this     // Catch:{ IOException | NullPointerException | XmlPullParserException -> 0x013a }
                    java.lang.String r2 = r2.mFileDataUrl     // Catch:{ IOException | NullPointerException | XmlPullParserException -> 0x013a }
                    r1.<init>(r2)     // Catch:{ IOException | NullPointerException | XmlPullParserException -> 0x013a }
                    java.lang.String r2 = r8.getDataUntil()     // Catch:{ IOException | NullPointerException | XmlPullParserException -> 0x013a }
                    r0.<init>(r1, r2)     // Catch:{ IOException | NullPointerException | XmlPullParserException -> 0x013a }
                    r8.setData(r0)     // Catch:{ IOException | NullPointerException | XmlPullParserException -> 0x013a }
                L_0x011b:
                    java.lang.String r8 = com.sec.internal.ims.servicemodules.im.FtHttpOutgoingMessage.LOG_TAG     // Catch:{ IOException | NullPointerException | XmlPullParserException -> 0x013a }
                    java.lang.StringBuilder r0 = new java.lang.StringBuilder     // Catch:{ IOException | NullPointerException | XmlPullParserException -> 0x013a }
                    r0.<init>()     // Catch:{ IOException | NullPointerException | XmlPullParserException -> 0x013a }
                    java.lang.String r1 = "EVENT_UPLOAD_COMPLETED file expiration: "
                    r0.append(r1)     // Catch:{ IOException | NullPointerException | XmlPullParserException -> 0x013a }
                    com.sec.internal.ims.servicemodules.im.FtHttpOutgoingMessage$FtHttpStateMachine r1 = com.sec.internal.ims.servicemodules.im.FtHttpOutgoingMessage.FtHttpStateMachine.this     // Catch:{ IOException | NullPointerException | XmlPullParserException -> 0x013a }
                    com.sec.internal.ims.servicemodules.im.FtHttpOutgoingMessage r1 = com.sec.internal.ims.servicemodules.im.FtHttpOutgoingMessage.this     // Catch:{ IOException | NullPointerException | XmlPullParserException -> 0x013a }
                    java.lang.String r1 = r1.mFileExpire     // Catch:{ IOException | NullPointerException | XmlPullParserException -> 0x013a }
                    r0.append(r1)     // Catch:{ IOException | NullPointerException | XmlPullParserException -> 0x013a }
                    java.lang.String r0 = r0.toString()     // Catch:{ IOException | NullPointerException | XmlPullParserException -> 0x013a }
                    android.util.Log.i(r8, r0)     // Catch:{ IOException | NullPointerException | XmlPullParserException -> 0x013a }
                    goto L_0x013e
                L_0x013a:
                    r8 = move-exception
                    r8.printStackTrace()
                L_0x013e:
                    com.sec.internal.ims.servicemodules.im.FtHttpOutgoingMessage$FtHttpStateMachine r7 = com.sec.internal.ims.servicemodules.im.FtHttpOutgoingMessage.FtHttpStateMachine.this
                    com.sec.internal.ims.servicemodules.im.FtHttpOutgoingMessage$FtHttpStateMachine$CompletedState r8 = r7.mCompletedState
                    r7.transitionTo(r8)
                    return
                */
                throw new UnsupportedOperationException("Method not decompiled: com.sec.internal.ims.servicemodules.im.FtHttpOutgoingMessage.FtHttpStateMachine.InProgressState.handleUploadCompleted(java.lang.String):void");
            }

            private void handleUploadCanceled(Message message) {
                Object obj = message.obj;
                if (obj != null) {
                    FtHttpOutgoingMessage.this.mCancelReason = (CancelReason) obj;
                }
                int ftHttpRetryInterval = FtHttpOutgoingMessage.this.getRcsStrategy().getFtHttpRetryInterval(message.arg1, FtHttpOutgoingMessage.this.getRetryCount());
                if (ftHttpRetryInterval >= 0) {
                    String r1 = FtHttpOutgoingMessage.LOG_TAG;
                    Log.i(r1, "EVENT_UPLOAD_CANCELED: msgId=" + FtHttpOutgoingMessage.this.mId + ", retry upload after " + ftHttpRetryInterval + " secs");
                    FtHttpStateMachine ftHttpStateMachine = FtHttpStateMachine.this;
                    ftHttpStateMachine.sendMessageDelayed(ftHttpStateMachine.obtainMessage(305, 0, message.arg2), ((long) ftHttpRetryInterval) * 1000);
                    return;
                }
                FtHttpStateMachine.this.handleFTHttpFailure();
            }

            private void tryUpload() {
                FtHttpOutgoingMessage ftHttpOutgoingMessage = FtHttpOutgoingMessage.this;
                ftHttpOutgoingMessage.mIsWifiUsed = ftHttpOutgoingMessage.getRcsStrategy().boolSetting(RcsPolicySettings.RcsPolicy.CANCEL_FT_WIFI_DISCONNECTED) && FtHttpOutgoingMessage.this.isWifiConnected();
                IMnoStrategy rcsStrategy = FtHttpOutgoingMessage.this.getRcsStrategy();
                FtHttpOutgoingMessage ftHttpOutgoingMessage2 = FtHttpOutgoingMessage.this;
                Uri ftHttpCsUri = rcsStrategy.getFtHttpCsUri(ftHttpOutgoingMessage2.mConfig, ftHttpOutgoingMessage2.mListener.onRequestParticipantUris(ftHttpOutgoingMessage2.mChatId), FtHttpOutgoingMessage.this.getExtraFt(), FtHttpOutgoingMessage.this.mIsGroupChat);
                if (ftHttpCsUri != null) {
                    UploadFileTask uploadFileTask = this.uploadTask;
                    if (uploadFileTask != null && uploadFileTask.getState() != AsyncFileTask.State.FINISHED) {
                        Log.i(FtHttpOutgoingMessage.LOG_TAG, "Task is already running or pending.");
                    } else if (FtHttpOutgoingMessage.this.needToAcquireNetworkForFT()) {
                        FtHttpOutgoingMessage ftHttpOutgoingMessage3 = FtHttpOutgoingMessage.this;
                        ftHttpOutgoingMessage3.acquireNetworkForFT(ftHttpOutgoingMessage3.getRcsStrategy().intSetting(RcsPolicySettings.RcsPolicy.FT_NET_CAPABILITY));
                    } else {
                        if (FtHttpOutgoingMessage.this.getFtCallback() == null) {
                            FtHttpOutgoingMessage ftHttpOutgoingMessage4 = FtHttpOutgoingMessage.this;
                            ftHttpOutgoingMessage4.setFtCompleteCallback(ftHttpOutgoingMessage4.mListener.onRequestCompleteCallback(ftHttpOutgoingMessage4.mChatId));
                        }
                        FtHttpOutgoingMessage ftHttpOutgoingMessage5 = FtHttpOutgoingMessage.this;
                        if (ftHttpOutgoingMessage5.mTransferredBytes <= 0) {
                            ftHttpOutgoingMessage5.mFileTransferId = FtMessage.sTidGenerator.generate().toString();
                        }
                        UploadFileTask.UploadRequest createUploadFileTaskRequest = createUploadFileTaskRequest(ftHttpCsUri.toString());
                        if (createUploadFileTaskRequest.isValid()) {
                            if (FtHttpOutgoingMessage.this.mTransferredBytes > 0) {
                                this.uploadTask = new UploadResumeFileTask(FtHttpOutgoingMessage.this.mConfig.getPhoneId(), FtHttpOutgoingMessage.this.getContext(), FtHttpStateMachine.this.getHandler().getLooper(), createUploadFileTaskRequest);
                            } else {
                                this.uploadTask = new UploadFileTask(FtHttpOutgoingMessage.this.mConfig.getPhoneId(), FtHttpOutgoingMessage.this.getContext(), FtHttpStateMachine.this.getHandler().getLooper(), createUploadFileTaskRequest);
                            }
                            runUploadTask();
                            return;
                        }
                        Log.e(FtHttpOutgoingMessage.LOG_TAG, "Download request not valid, can't transfer file");
                        FtHttpStateMachine ftHttpStateMachine = FtHttpStateMachine.this;
                        ftHttpStateMachine.sendMessage(ftHttpStateMachine.obtainMessage(303, -1, -1, CancelReason.ERROR));
                    }
                } else {
                    Log.e(FtHttpOutgoingMessage.LOG_TAG, "getHttpCsUri is null, can't transfer file");
                    FtHttpStateMachine ftHttpStateMachine2 = FtHttpStateMachine.this;
                    ftHttpStateMachine2.sendMessage(ftHttpStateMachine2.obtainMessage(303, -1, -1, CancelReason.ERROR));
                }
            }

            private void runUploadTask() {
                Executor executor = AsyncFileTask.THREAD_POOL_EXECUTOR;
                if (FtHttpOutgoingMessage.this.getRcsStrategy().boolSetting(RcsPolicySettings.RcsPolicy.FTHTTP_SINGLE_THREAD) || ImsProfile.isRcsUpProfile(FtHttpOutgoingMessage.this.mConfig.getRcsProfile())) {
                    executor = AsyncFileTask.SERIAL_EXECUTOR;
                }
                this.uploadTask.execute(executor);
            }

            private UploadFileTask.UploadRequest createUploadFileTaskRequest(String str) {
                Network network;
                boolean z = FtHttpOutgoingMessage.this.getExtraFt() || FtHttpOutgoingMessage.this.getRcsStrategy().boolSetting(RcsPolicySettings.RcsPolicy.FT_INTERNET_PDN);
                FtHttpOutgoingMessage ftHttpOutgoingMessage = FtHttpOutgoingMessage.this;
                long j = ftHttpOutgoingMessage.mFileSize;
                String str2 = ftHttpOutgoingMessage.mFileName;
                Uri uri = ftHttpOutgoingMessage.mContentUri;
                String str3 = ftHttpOutgoingMessage.mFileTransferId;
                String ftHttpUserAgent = ftHttpOutgoingMessage.getFtHttpUserAgent();
                if (z) {
                    network = null;
                } else {
                    network = FtHttpOutgoingMessage.this.mNetwork;
                }
                return new UploadFileTask.UploadRequest(str, j, str2, uri, true, str3, ftHttpUserAgent, network, FtHttpOutgoingMessage.this.mConfig.isFtHttpTrustAllCerts(), new UploadFileTask.UploadTaskCallback() {
                    public void onFinished() {
                    }

                    public void onStarted() {
                        if (InProgressState.this.uploadTask instanceof UploadFileTask) {
                            Log.i(FtHttpOutgoingMessage.LOG_TAG, "Posting Started event");
                            FtHttpOutgoingMessage ftHttpOutgoingMessage = FtHttpOutgoingMessage.this;
                            ftHttpOutgoingMessage.mListener.onTransferInProgress(ftHttpOutgoingMessage);
                        }
                    }

                    public void onProgressUpdate(long j) {
                        FtHttpStateMachine.this.sendMessage(201, (Object) Long.valueOf(j));
                    }

                    public void onCompleted(String str) {
                        FtHttpStateMachine.this.sendMessage(302, (Object) str);
                        FtHttpOutgoingMessage.this.listToDumpFormat(LogClass.FT_HTTP_UPLOAD_COMPLETE, 0, new ArrayList());
                    }

                    public void onCanceled(CancelReason cancelReason, int i, int i2, boolean z) {
                        if (z) {
                            FtHttpOutgoingMessage.this.mTransferredBytes = 0;
                        }
                        if (i2 == 500) {
                            FtHttpOutgoingMessage ftHttpOutgoingMessage = FtHttpOutgoingMessage.this;
                            ftHttpOutgoingMessage.mFileName = FileUtils.deAccent(ftHttpOutgoingMessage.mFileName, true);
                        }
                        FtHttpStateMachine ftHttpStateMachine = FtHttpStateMachine.this;
                        ftHttpStateMachine.sendMessage(ftHttpStateMachine.obtainMessage(303, i, i2, cancelReason));
                        if (i2 != -1) {
                            FtHttpOutgoingMessage.this.mModule.getBigDataProcessor().onMessageSendingFailed(FtHttpOutgoingMessage.this, new Result(ImError.NETWORK_ERROR, Result.Type.HTTP_ERROR), String.valueOf(i2), (IMnoStrategy.StrategyResponse) null);
                            ArrayList arrayList = new ArrayList();
                            arrayList.add(String.valueOf(i2));
                            arrayList.add(String.valueOf(FtHttpOutgoingMessage.this.getRetryCount()));
                            FtHttpOutgoingMessage.this.listToDumpFormat(LogClass.FT_HTTP_UPLOAD_CANCEL, 0, arrayList);
                        }
                    }
                }, FtHttpOutgoingMessage.this.mContentType);
            }
        }

        private final class CancelingState extends State {
            private CancelingState() {
            }

            public void enter() {
                String r0 = FtHttpOutgoingMessage.LOG_TAG;
                Log.i(r0, "CancelingState enter msgId : " + FtHttpOutgoingMessage.this.mId);
                FtHttpOutgoingMessage.this.updateState();
            }

            public boolean processMessage(Message message) {
                int i = message.what;
                if (i == 3) {
                    FtHttpStateMachine.this.handleTransferProgress((FtTransferProgressEvent) message.obj);
                } else if (i == 23) {
                    FtHttpStateMachine ftHttpStateMachine = FtHttpStateMachine.this;
                    ftHttpStateMachine.logi("EVENT_TRANSFER_TIMER_TIMEOUT : " + FtHttpOutgoingMessage.this.mId);
                    FtHttpOutgoingMessage.this.cancelTransfer(CancelReason.CANCELED_BY_SYSTEM);
                } else if (i == 8) {
                    CancelReason cancelReason = (CancelReason) message.obj;
                    FtHttpStateMachine ftHttpStateMachine2 = FtHttpStateMachine.this;
                    ftHttpStateMachine2.logi("cancel transfer in cancelingState reason = " + cancelReason);
                    CancelReason cancelReason2 = CancelReason.CANCELED_BY_SYSTEM;
                    if (cancelReason == cancelReason2) {
                        FtHttpStateMachine ftHttpStateMachine3 = FtHttpStateMachine.this;
                        FtHttpOutgoingMessage.this.mCancelReason = cancelReason2;
                        ftHttpStateMachine3.transitionTo(ftHttpStateMachine3.mCanceledState);
                    }
                } else if (i != 9) {
                    return false;
                } else {
                    AsyncResult asyncResult = (AsyncResult) message.obj;
                    if (asyncResult != null) {
                        String r0 = FtHttpOutgoingMessage.LOG_TAG;
                        Log.i(r0, "CancelingState: cancel transfer result = " + ((FtResult) asyncResult.result));
                        FtHttpStateMachine ftHttpStateMachine4 = FtHttpStateMachine.this;
                        ftHttpStateMachine4.transitionTo(ftHttpStateMachine4.mCanceledState);
                    }
                }
                return true;
            }
        }

        private final class CanceledState extends State {
            private CanceledState() {
            }

            public void enter() {
                String r0 = FtHttpOutgoingMessage.LOG_TAG;
                Log.i(r0, "CanceledState enter msgId : " + FtHttpOutgoingMessage.this.mId);
                FtHttpOutgoingMessage.this.updateState();
                FtHttpStateMachine ftHttpStateMachine = FtHttpStateMachine.this;
                FtHttpOutgoingMessage ftHttpOutgoingMessage = FtHttpOutgoingMessage.this;
                if (ftHttpOutgoingMessage.mIsSlmSvcMsg) {
                    ftHttpOutgoingMessage.mIsSlmSvcMsg = false;
                    ftHttpStateMachine.removeMessages(23);
                }
                FtHttpOutgoingMessage ftHttpOutgoingMessage2 = FtHttpOutgoingMessage.this;
                if (ftHttpOutgoingMessage2.mIsNetworkRequested) {
                    ftHttpOutgoingMessage2.releaseNetworkAcquiredForFT();
                }
                FtHttpOutgoingMessage ftHttpOutgoingMessage3 = FtHttpOutgoingMessage.this;
                if (ftHttpOutgoingMessage3.mIsBootup) {
                    Log.i(FtHttpOutgoingMessage.LOG_TAG, "Message is loaded from bootup, no need for notifications");
                    FtHttpOutgoingMessage.this.mIsBootup = false;
                    return;
                }
                ftHttpOutgoingMessage3.releaseWakeLock();
                FtHttpOutgoingMessage ftHttpOutgoingMessage4 = FtHttpOutgoingMessage.this;
                IMnoStrategy rcsStrategy = ftHttpOutgoingMessage4.getRcsStrategy();
                FtHttpOutgoingMessage ftHttpOutgoingMessage5 = FtHttpOutgoingMessage.this;
                ftHttpOutgoingMessage4.mResumableOptionCode = rcsStrategy.getftResumableOption(ftHttpOutgoingMessage5.mCancelReason, ftHttpOutgoingMessage5.mIsGroupChat, ftHttpOutgoingMessage5.mDirection, ftHttpOutgoingMessage5.getTransferMech()).getId();
                FtHttpOutgoingMessage.this.updateStatus(ImConstants.Status.FAILED);
                FtHttpOutgoingMessage ftHttpOutgoingMessage6 = FtHttpOutgoingMessage.this;
                ftHttpOutgoingMessage6.mListener.onTransferCanceled(ftHttpOutgoingMessage6);
                FtHttpOutgoingMessage.this.setFtCompleteCallback((Message) null);
                FtHttpOutgoingMessage.this.deleteFile();
            }

            public boolean processMessage(Message message) {
                int i = message.what;
                if (i != 1) {
                    if (i == 8) {
                        FtHttpOutgoingMessage ftHttpOutgoingMessage = FtHttpOutgoingMessage.this;
                        ftHttpOutgoingMessage.mListener.onCancelRequestFailed(ftHttpOutgoingMessage);
                        return true;
                    } else if (i != 11) {
                        return false;
                    }
                }
                FtHttpStateMachine ftHttpStateMachine = FtHttpStateMachine.this;
                if (!FtHttpOutgoingMessage.this.mIsResuming) {
                    return true;
                }
                ftHttpStateMachine.transitionTo(ftHttpStateMachine.mInProgressState);
                return true;
            }
        }

        private final class CompletedState extends State {
            private CompletedState() {
            }

            public void enter() {
                String r0 = FtHttpOutgoingMessage.LOG_TAG;
                Log.i(r0, "CompletedState enter msgId : " + FtHttpOutgoingMessage.this.mId);
                FtHttpOutgoingMessage.this.updateState();
                FtHttpOutgoingMessage ftHttpOutgoingMessage = FtHttpOutgoingMessage.this;
                boolean z = ftHttpOutgoingMessage.mIsSlmSvcMsg;
                if (ftHttpOutgoingMessage.isFtSms() || z) {
                    FtHttpStateMachine.this.removeMessages(23);
                    FtHttpOutgoingMessage.this.updateStatus(ImConstants.Status.SENT);
                    FtHttpOutgoingMessage.this.setSentTimestamp(System.currentTimeMillis());
                    FtHttpOutgoingMessage.this.mIsSlmSvcMsg = false;
                }
                FtHttpOutgoingMessage ftHttpOutgoingMessage2 = FtHttpOutgoingMessage.this;
                if (ftHttpOutgoingMessage2.mIsNetworkRequested) {
                    ftHttpOutgoingMessage2.releaseNetworkAcquiredForFT();
                }
                FtHttpOutgoingMessage ftHttpOutgoingMessage3 = FtHttpOutgoingMessage.this;
                if (ftHttpOutgoingMessage3.mIsBootup) {
                    Log.i(FtHttpOutgoingMessage.LOG_TAG, "Message is loaded from bootup, no need for notifications");
                    FtHttpOutgoingMessage.this.mIsBootup = false;
                    return;
                }
                ftHttpOutgoingMessage3.releaseWakeLock();
                if (FtHttpOutgoingMessage.this.isFtSms() || z) {
                    FtHttpOutgoingMessage ftHttpOutgoingMessage4 = FtHttpOutgoingMessage.this;
                    ftHttpOutgoingMessage4.mListener.onTransferCompleted(ftHttpOutgoingMessage4);
                } else {
                    FtHttpOutgoingMessage.this.invokeFtQueueCallBack();
                }
                FtHttpOutgoingMessage.this.deleteFile();
            }

            public boolean processMessage(Message message) {
                int i = message.what;
                if (i == 1) {
                    FtHttpStateMachine ftHttpStateMachine = FtHttpStateMachine.this;
                    if (!FtHttpOutgoingMessage.this.mIsResuming) {
                        return true;
                    }
                    ftHttpStateMachine.transitionTo(ftHttpStateMachine.mInProgressState);
                    return true;
                } else if (i != 8) {
                    return false;
                } else {
                    FtHttpOutgoingMessage ftHttpOutgoingMessage = FtHttpOutgoingMessage.this;
                    ftHttpOutgoingMessage.mListener.onCancelRequestFailed(ftHttpOutgoingMessage);
                    return true;
                }
            }
        }

        private final class CanceledNeedToNotifyState extends State {
            private CanceledNeedToNotifyState() {
            }

            public void enter() {
                String r0 = FtHttpOutgoingMessage.LOG_TAG;
                Log.i(r0, "CanceledState enter msgId : " + FtHttpOutgoingMessage.this.mId);
                FtHttpOutgoingMessage.this.updateState();
                FtHttpOutgoingMessage ftHttpOutgoingMessage = FtHttpOutgoingMessage.this;
                ftHttpOutgoingMessage.mResumableOptionCode = 0;
                ftHttpOutgoingMessage.updateStatus(ImConstants.Status.FAILED);
                FtHttpOutgoingMessage ftHttpOutgoingMessage2 = FtHttpOutgoingMessage.this;
                ftHttpOutgoingMessage2.mListener.onTransferCanceled(ftHttpOutgoingMessage2);
                if (!FtHttpOutgoingMessage.this.isFtSms()) {
                    FtHttpOutgoingMessage.this.invokeFtQueueCallBack();
                }
                FtHttpStateMachine ftHttpStateMachine = FtHttpStateMachine.this;
                ftHttpStateMachine.transitionTo(ftHttpStateMachine.mCanceledState);
            }

            public boolean processMessage(Message message) {
                int i = message.what;
                return false;
            }
        }
    }

    /* renamed from: com.sec.internal.ims.servicemodules.im.FtHttpOutgoingMessage$1  reason: invalid class name */
    static /* synthetic */ class AnonymousClass1 {
        static final /* synthetic */ int[] $SwitchMap$com$sec$internal$constants$ims$servicemodules$im$event$FtTransferProgressEvent$State;

        /* JADX WARNING: Can't wrap try/catch for region: R(8:0|1|2|3|4|5|6|(3:7|8|10)) */
        /* JADX WARNING: Failed to process nested try/catch */
        /* JADX WARNING: Missing exception handler attribute for start block: B:3:0x0012 */
        /* JADX WARNING: Missing exception handler attribute for start block: B:5:0x001d */
        /* JADX WARNING: Missing exception handler attribute for start block: B:7:0x0028 */
        static {
            /*
                com.sec.internal.constants.ims.servicemodules.im.event.FtTransferProgressEvent$State[] r0 = com.sec.internal.constants.ims.servicemodules.im.event.FtTransferProgressEvent.State.values()
                int r0 = r0.length
                int[] r0 = new int[r0]
                $SwitchMap$com$sec$internal$constants$ims$servicemodules$im$event$FtTransferProgressEvent$State = r0
                com.sec.internal.constants.ims.servicemodules.im.event.FtTransferProgressEvent$State r1 = com.sec.internal.constants.ims.servicemodules.im.event.FtTransferProgressEvent.State.TRANSFERRING     // Catch:{ NoSuchFieldError -> 0x0012 }
                int r1 = r1.ordinal()     // Catch:{ NoSuchFieldError -> 0x0012 }
                r2 = 1
                r0[r1] = r2     // Catch:{ NoSuchFieldError -> 0x0012 }
            L_0x0012:
                int[] r0 = $SwitchMap$com$sec$internal$constants$ims$servicemodules$im$event$FtTransferProgressEvent$State     // Catch:{ NoSuchFieldError -> 0x001d }
                com.sec.internal.constants.ims.servicemodules.im.event.FtTransferProgressEvent$State r1 = com.sec.internal.constants.ims.servicemodules.im.event.FtTransferProgressEvent.State.CANCELED     // Catch:{ NoSuchFieldError -> 0x001d }
                int r1 = r1.ordinal()     // Catch:{ NoSuchFieldError -> 0x001d }
                r2 = 2
                r0[r1] = r2     // Catch:{ NoSuchFieldError -> 0x001d }
            L_0x001d:
                int[] r0 = $SwitchMap$com$sec$internal$constants$ims$servicemodules$im$event$FtTransferProgressEvent$State     // Catch:{ NoSuchFieldError -> 0x0028 }
                com.sec.internal.constants.ims.servicemodules.im.event.FtTransferProgressEvent$State r1 = com.sec.internal.constants.ims.servicemodules.im.event.FtTransferProgressEvent.State.COMPLETED     // Catch:{ NoSuchFieldError -> 0x0028 }
                int r1 = r1.ordinal()     // Catch:{ NoSuchFieldError -> 0x0028 }
                r2 = 3
                r0[r1] = r2     // Catch:{ NoSuchFieldError -> 0x0028 }
            L_0x0028:
                int[] r0 = $SwitchMap$com$sec$internal$constants$ims$servicemodules$im$event$FtTransferProgressEvent$State     // Catch:{ NoSuchFieldError -> 0x0033 }
                com.sec.internal.constants.ims.servicemodules.im.event.FtTransferProgressEvent$State r1 = com.sec.internal.constants.ims.servicemodules.im.event.FtTransferProgressEvent.State.INTERRUPTED     // Catch:{ NoSuchFieldError -> 0x0033 }
                int r1 = r1.ordinal()     // Catch:{ NoSuchFieldError -> 0x0033 }
                r2 = 4
                r0[r1] = r2     // Catch:{ NoSuchFieldError -> 0x0033 }
            L_0x0033:
                return
            */
            throw new UnsupportedOperationException("Method not decompiled: com.sec.internal.ims.servicemodules.im.FtHttpOutgoingMessage.AnonymousClass1.<clinit>():void");
        }
    }
}
