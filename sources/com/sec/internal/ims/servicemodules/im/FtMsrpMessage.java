package com.sec.internal.ims.servicemodules.im;

import android.net.Uri;
import android.os.Looper;
import android.os.Message;
import android.text.TextUtils;
import android.util.Log;
import com.sec.ims.options.Capabilities;
import com.sec.ims.util.ImsUri;
import com.sec.internal.constants.ims.SipMsg;
import com.sec.internal.constants.ims.servicemodules.im.ImCacheAction;
import com.sec.internal.constants.ims.servicemodules.im.ImConstants;
import com.sec.internal.constants.ims.servicemodules.im.ImDirection;
import com.sec.internal.constants.ims.servicemodules.im.ImError;
import com.sec.internal.constants.ims.servicemodules.im.NotificationStatus;
import com.sec.internal.constants.ims.servicemodules.im.event.FtIncomingSessionEvent;
import com.sec.internal.constants.ims.servicemodules.im.event.FtTransferProgressEvent;
import com.sec.internal.constants.ims.servicemodules.im.params.AcceptFtSessionParams;
import com.sec.internal.constants.ims.servicemodules.im.params.RejectFtSessionParams;
import com.sec.internal.constants.ims.servicemodules.im.params.SendFtSessionParams;
import com.sec.internal.constants.ims.servicemodules.im.params.SendImdnParams;
import com.sec.internal.constants.ims.servicemodules.im.params.SendReportMsgParams;
import com.sec.internal.constants.ims.servicemodules.im.reason.CancelReason;
import com.sec.internal.constants.ims.servicemodules.im.reason.FtRejectReason;
import com.sec.internal.constants.ims.servicemodules.im.result.FtResult;
import com.sec.internal.constants.ims.servicemodules.im.result.Result;
import com.sec.internal.helper.AsyncResult;
import com.sec.internal.helper.FilePathGenerator;
import com.sec.internal.helper.FileUtils;
import com.sec.internal.helper.FingerprintGenerator;
import com.sec.internal.helper.IState;
import com.sec.internal.helper.PreciseAlarmManager;
import com.sec.internal.helper.PublicAccountUri;
import com.sec.internal.helper.State;
import com.sec.internal.helper.translate.MappingTranslator;
import com.sec.internal.ims.servicemodules.im.FtMessage;
import com.sec.internal.ims.servicemodules.im.data.FtResumableOption;
import com.sec.internal.ims.servicemodules.im.data.response.FileResizeResponse;
import com.sec.internal.ims.servicemodules.im.strategy.IMnoStrategy;
import com.sec.internal.ims.servicemodules.im.util.FileDurationUtil;
import com.sec.internal.ims.settings.RcsPolicySettings;
import com.sec.internal.ims.util.ChatbotUriUtil;
import com.sec.internal.ims.util.StringIdGenerator;
import com.sec.internal.ims.util.UriGenerator;
import com.sec.internal.imscr.LogClass;
import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashSet;
import java.util.Objects;
import java.util.Set;

public class FtMsrpMessage extends FtMessage {
    /* access modifiers changed from: private */
    public final String LOG_TAG;
    /* access modifiers changed from: private */
    public ImsUri mConferenceUri;
    /* access modifiers changed from: private */
    public int mRetryTimer = -1;
    /* access modifiers changed from: private */
    public boolean mSwapUriType;

    public int getTransferMech() {
        return 0;
    }

    protected FtMsrpMessage(Builder<?> builder) {
        super(builder);
        String substring = (TextUtils.isEmpty(this.mImdnId) || this.mImdnId.length() < 4) ? "" : TextUtils.substring(this.mImdnId, 0, 4);
        this.LOG_TAG = FtMsrpMessage.class.getSimpleName() + "#" + substring;
        this.mRawHandle = builder.mRawHandle;
    }

    public static Builder<?> builder() {
        return new Builder2();
    }

    public void receiveTransfer(Message message, FtIncomingSessionEvent ftIncomingSessionEvent, boolean z) {
        this.mIsResuming = z;
        this.mFtCompleteCallback = message;
        FtMessage.FtStateMachine ftStateMachine = this.mStateMachine;
        ftStateMachine.sendMessage(ftStateMachine.obtainMessage(10, (Object) ftIncomingSessionEvent));
    }

    public void startFileTransferTimer() {
        String str = this.LOG_TAG;
        Log.i(str, "startFileTransferTimer() : " + this.mId);
        this.mStateMachine.getHandler().removeMessages(23);
        FtMessage.FtStateMachine ftStateMachine = this.mStateMachine;
        ftStateMachine.sendMessageDelayed(ftStateMachine.obtainMessage(23), 300000);
    }

    /* access modifiers changed from: protected */
    public void sendDeliveredNotification(Object obj, String str, String str2, Message message, String str3, boolean z, boolean z2) {
        ImsUri networkPreferredUri = this.mUriGenerator.getNetworkPreferredUri(UriGenerator.URIServiceType.RCS_URI, this.mRemoteUri.getMsisdn(), (String) null);
        String str4 = this.mChatId;
        String str5 = this.mConversationId;
        SendImdnParams sendImdnParams = new SendImdnParams(obj, networkPreferredUri, str4, str5 == null ? str : str5, StringIdGenerator.generateContributionId(), str3, message, this.mDeviceId, getNewImdnData(NotificationStatus.DELIVERED), z, new Date(), z2, this.mModule.getUserAlias(this.mConfig.getPhoneId(), true));
        if (this.mIsSlmSvcMsg) {
            this.mSlmService.sendSlmDeliveredNotification(sendImdnParams);
        } else {
            this.mImsService.sendFtDeliveredNotification(sendImdnParams);
        }
    }

    /* access modifiers changed from: protected */
    public void sendDisplayedNotification(Object obj, String str, String str2, Message message, String str3, boolean z, boolean z2) {
        ImsUri networkPreferredUri = this.mUriGenerator.getNetworkPreferredUri(UriGenerator.URIServiceType.RCS_URI, this.mRemoteUri.getMsisdn(), (String) null);
        String str4 = this.mChatId;
        String str5 = this.mConversationId;
        SendImdnParams sendImdnParams = new SendImdnParams(obj, networkPreferredUri, str4, str5 == null ? str : str5, StringIdGenerator.generateContributionId(), str3, message, this.mDeviceId, getNewImdnData(NotificationStatus.DISPLAYED), z, new Date(), z2, this.mModule.getUserAlias(this.mConfig.getPhoneId(), true));
        if (this.mIsSlmSvcMsg) {
            this.mSlmService.sendSlmDisplayedNotification(sendImdnParams);
        } else {
            this.mImsService.sendFtDisplayedNotification(sendImdnParams);
        }
    }

    /* access modifiers changed from: protected */
    public void sendRejectFtSession(FtRejectReason ftRejectReason) {
        FtIncomingSessionEvent ftIncomingSessionEvent = new FtIncomingSessionEvent();
        ftIncomingSessionEvent.mRawHandle = this.mRawHandle;
        ftIncomingSessionEvent.mIsSlmSvcMsg = this.mIsSlmSvcMsg;
        sendRejectFtSession(ftRejectReason, ftIncomingSessionEvent);
    }

    /* access modifiers changed from: protected */
    public void sendRejectFtSession(FtRejectReason ftRejectReason, FtIncomingSessionEvent ftIncomingSessionEvent) {
        this.mRejectReason = ftRejectReason;
        RejectFtSessionParams rejectFtSessionParams = new RejectFtSessionParams(ftIncomingSessionEvent.mRawHandle, this.mStateMachine.obtainMessage(7), ftRejectReason, this.mFileTransferId, this.mImdnId);
        if (ftIncomingSessionEvent.mIsSlmSvcMsg) {
            this.mSlmService.rejectFtSlmMessage(rejectFtSessionParams);
        } else {
            this.mImsService.rejectFtSession(rejectFtSessionParams);
        }
    }

    /* access modifiers changed from: protected */
    public boolean renameFile() {
        try {
            File file = new File(this.mFilePath);
            String str = this.LOG_TAG;
            Log.i(str, "temporary file path: " + this.mFilePath);
            String parent = file.getParent();
            File file2 = new File(parent);
            if (!file2.exists()) {
                file2.mkdirs();
            }
            this.mFilePath = FilePathGenerator.generateUniqueFilePath(parent, this.mFileName, 128);
            String str2 = this.LOG_TAG;
            Log.i(str2, "new file path: " + this.mFilePath);
            if (file.renameTo(new File(this.mFilePath))) {
                Log.i(this.LOG_TAG, "file rename success");
                return true;
            }
            Log.e(this.LOG_TAG, "file rename fail");
            return false;
        } catch (Exception unused) {
            return false;
        }
    }

    public void setConferenceUri(ImsUri imsUri) {
        this.mConferenceUri = imsUri;
    }

    public Object getRawHandle() {
        return this.mRawHandle;
    }

    /* access modifiers changed from: protected */
    public FtMessage.FtStateMachine createFtStateMachine(String str, Looper looper) {
        return new FtMsrpStateMachine("FtMsrpMessage#" + str, looper);
    }

    /* access modifiers changed from: private */
    public void setCancelReasonBasedOnLineType() {
        if (isChatbotMessage() || this.mCancelReason == CancelReason.REJECTED_BY_REMOTE) {
            this.mCancelReason = CancelReason.FORBIDDEN_NO_RETRY_FALLBACK;
        } else {
            this.mCancelReason = CancelReason.REMOTE_TEMPORARILY_UNAVAILABLE;
        }
    }

    /* access modifiers changed from: private */
    public void updateFtMessageInfo(FtIncomingSessionEvent ftIncomingSessionEvent) {
        this.mRawHandle = ftIncomingSessionEvent.mRawHandle;
        this.mFilePath = ftIncomingSessionEvent.mFilePath;
        this.mFileName = ftIncomingSessionEvent.mFileName;
        this.mFileSize = ftIncomingSessionEvent.mFileSize;
        this.mContributionId = ftIncomingSessionEvent.mContributionId;
        this.mConversationId = ftIncomingSessionEvent.mConversationId;
        this.mContentType = ftIncomingSessionEvent.mContentType;
        setSlmSvcMsg(ftIncomingSessionEvent.mIsSlmSvcMsg);
    }

    public void setSlmSvcMsg(boolean z) {
        this.mIsSlmSvcMsg = z;
        if (z) {
            setMessagingTech(this.mFileSize > ((long) this.mConfig.getPagerModeLimit()) ? ImConstants.MessagingTech.SLM_LARGE_MODE : ImConstants.MessagingTech.SLM_PAGER_MODE);
        } else {
            setMessagingTech(ImConstants.MessagingTech.NORMAL);
        }
    }

    /* access modifiers changed from: private */
    public boolean isChatbotMessage() {
        return !this.mIsGroupChat && ChatbotUriUtil.hasChatbotUri(this.mListener.onRequestParticipantUris(this.mChatId), this.mConfig.getPhoneId());
    }

    /* access modifiers changed from: private */
    public void moveCachedFileToApp() {
        if (this.mContentUri != null) {
            long copyFile = FileUtils.copyFile(getContext(), this.mFilePath, this.mContentUri);
            if (copyFile != this.mFileSize) {
                String str = this.LOG_TAG;
                Log.i(str, "Incoming file move to APP failed. FileSize=" + this.mFileSize + ", Copied=" + copyFile);
                return;
            }
            deleteFile();
            return;
        }
        Log.i(this.LOG_TAG, "Incoming file copy to APP failed. mContentUri=null");
    }

    public static abstract class Builder<T extends Builder<T>> extends FtMessage.Builder<T> {
        Object mRawHandle;

        public T rawHandle(Object obj) {
            this.mRawHandle = obj;
            return (Builder) self();
        }

        public FtMsrpMessage build() {
            return new FtMsrpMessage(this);
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

    private class FtMsrpStateMachine extends FtMessage.FtStateMachine {
        /* access modifiers changed from: private */
        public final State mAcceptingState;
        private final State mAttachedState;
        /* access modifiers changed from: private */
        public final State mCanceledState;
        /* access modifiers changed from: private */
        public final State mCancelingState;
        /* access modifiers changed from: private */
        public final State mCompletedState;
        protected final MappingTranslator<Integer, State> mDbStateTranslator;
        private final State mDefaultState = new DefaultState();
        /* access modifiers changed from: private */
        public final State mInProgressState;
        private final State mInitialState;
        private final State mSendingState;
        protected final MappingTranslator<IState, Integer> mStateTranslator;

        protected FtMsrpStateMachine(String str, Looper looper) {
            super(str, looper);
            InitialState initialState = new InitialState();
            this.mInitialState = initialState;
            AttachedState attachedState = new AttachedState();
            this.mAttachedState = attachedState;
            SendingState sendingState = new SendingState();
            this.mSendingState = sendingState;
            AcceptingState acceptingState = new AcceptingState();
            this.mAcceptingState = acceptingState;
            InProgressState inProgressState = new InProgressState();
            this.mInProgressState = inProgressState;
            CompletedState completedState = new CompletedState();
            this.mCompletedState = completedState;
            CancelingState cancelingState = new CancelingState();
            this.mCancelingState = cancelingState;
            CanceledState canceledState = new CanceledState();
            this.mCanceledState = canceledState;
            this.mStateTranslator = new MappingTranslator.Builder().map(initialState, 0).map(attachedState, 6).map(sendingState, 9).map(acceptingState, 1).map(inProgressState, 2).map(completedState, 3).map(cancelingState, 7).map(canceledState, 4).buildTranslator();
            this.mDbStateTranslator = new MappingTranslator.Builder().map(0, initialState).map(6, canceledState).map(2, canceledState).map(1, canceledState).map(3, completedState).map(7, canceledState).map(4, canceledState).map(5, canceledState).map(9, canceledState).buildTranslator();
        }

        /* access modifiers changed from: protected */
        public void initState(State state) {
            addState(this.mDefaultState);
            addState(this.mInitialState, this.mDefaultState);
            addState(this.mAttachedState, this.mDefaultState);
            addState(this.mSendingState, this.mDefaultState);
            addState(this.mAcceptingState, this.mDefaultState);
            addState(this.mInProgressState, this.mDefaultState);
            addState(this.mCompletedState, this.mDefaultState);
            addState(this.mCancelingState, this.mDefaultState);
            addState(this.mCanceledState, this.mDefaultState);
            logi("setting current state as " + state.getName() + " for messageId : " + FtMsrpMessage.this.mId);
            setInitialState(state);
            start();
        }

        /* access modifiers changed from: private */
        public void onAttachSlmFile() {
            logi("onAttachSlmFile()");
            if (FtMsrpMessage.this.isChatbotMessage()) {
                loge("onAttachSlmFile: Chatbot, Display Error");
                FtMsrpMessage.this.mCancelReason = CancelReason.FORBIDDEN_NO_RETRY_FALLBACK;
                transitionTo(this.mCanceledState);
                return;
            }
            FtMsrpMessage ftMsrpMessage = FtMsrpMessage.this;
            if (ftMsrpMessage.mFileSize > ftMsrpMessage.mConfig.getSlmMaxMsgSize()) {
                FtMsrpMessage ftMsrpMessage2 = FtMsrpMessage.this;
                if (!ftMsrpMessage2.mIsResizable || !ftMsrpMessage2.getRcsStrategy().boolSetting(RcsPolicySettings.RcsPolicy.SUPPORT_LARGE_MSG_RESIZING)) {
                    String r0 = FtMsrpMessage.this.LOG_TAG;
                    Log.i(r0, "File size is greater than allowed MaxSlmSize mFileSize:" + FtMsrpMessage.this.mFileSize + ", SLMMaxMsgSize:" + FtMsrpMessage.this.mConfig.getSlmMaxMsgSize());
                    FtMsrpMessage.this.setCancelReasonBasedOnLineType();
                    transitionTo(this.mCanceledState);
                    return;
                }
                logi("request resizing for LMM");
                FtMsrpMessage ftMsrpMessage3 = FtMsrpMessage.this;
                ftMsrpMessage3.mListener.onFileResizingNeeded(ftMsrpMessage3, ftMsrpMessage3.mConfig.getSlmMaxMsgSize());
                FtMsrpMessage.this.setSlmSvcMsg(true);
                return;
            }
            FtMsrpMessage.this.setSlmSvcMsg(true);
            transitionTo(this.mAttachedState);
        }

        /* access modifiers changed from: private */
        public void onAttachFile(boolean z) {
            if (z) {
                IMnoStrategy rcsStrategy = FtMsrpMessage.this.getRcsStrategy();
                FtMsrpMessage ftMsrpMessage = FtMsrpMessage.this;
                IMnoStrategy.StatusCode statusCode = rcsStrategy.checkCapability(ftMsrpMessage.mListener.onRequestParticipantUris(ftMsrpMessage.mChatId), (long) Capabilities.FEATURE_FT_SERVICE).getStatusCode();
                if (statusCode == IMnoStrategy.StatusCode.FALLBACK_TO_LEGACY || statusCode == IMnoStrategy.StatusCode.FALLBACK_TO_SLM) {
                    logi("onAttachFile: Capability checking failed.");
                    if (FtMsrpMessage.this.isChatbotMessage()) {
                        log("onAttachFile: Chatbot messgage no fallback");
                        FtMsrpMessage.this.mCancelReason = CancelReason.FORBIDDEN_NO_RETRY_FALLBACK;
                    } else if (statusCode != IMnoStrategy.StatusCode.FALLBACK_TO_SLM || FtMsrpMessage.this.mIsResuming) {
                        FtMsrpMessage.this.setCancelReasonBasedOnLineType();
                        logi("onAttachFile: mCancelReason = " + FtMsrpMessage.this.mCancelReason);
                    } else {
                        logi("onAttachFile: fallback to SLM");
                        onAttachSlmFile();
                        return;
                    }
                    transitionTo(this.mCanceledState);
                    return;
                }
            }
            long max = Math.max(FtMsrpMessage.this.mConfig.getMaxSizeExtraFileTr(), FtMsrpMessage.this.mConfig.getMaxSizeFileTr());
            if (!FtMsrpMessage.this.isOutgoing() || max == 0 || FtMsrpMessage.this.mFileSize <= max) {
                if (FtMsrpMessage.this.isOutgoing() && (FtMsrpMessage.this.mContentType.startsWith(SipMsg.FEATURE_TAG_MMTEL_VIDEO) || FtMsrpMessage.this.mContentType.startsWith("audio"))) {
                    FtMsrpMessage ftMsrpMessage2 = FtMsrpMessage.this;
                    ftMsrpMessage2.mTimeDuration = FileDurationUtil.getFileDurationTime(ftMsrpMessage2.mFilePath);
                }
                FtMsrpMessage ftMsrpMessage3 = FtMsrpMessage.this;
                if (ftMsrpMessage3.mIsResuming) {
                    ftMsrpMessage3.mContributionId = StringIdGenerator.generateContributionId();
                }
                if (FtMsrpMessage.this.getRcsStrategy().boolSetting(RcsPolicySettings.RcsPolicy.SUPPORT_QUICKFT)) {
                    FtMsrpMessage.this.mFileFingerPrint = FingerprintGenerator.generateFromFile(new File(FtMsrpMessage.this.mFilePath), "SHA1");
                    log("getFileMD5: mFilePath: " + FtMsrpMessage.this.mFilePath + " mFileFingerPrint: " + FtMsrpMessage.this.mFileFingerPrint);
                    FtMsrpMessage ftMsrpMessage4 = FtMsrpMessage.this;
                    if (ftMsrpMessage4.mFileFingerPrint == null) {
                        ftMsrpMessage4.mFileFingerPrint = "";
                    }
                }
                if (FtMsrpMessage.this.isOutgoing() && FtMsrpMessage.this.mConfig.isFtThumb()) {
                    FtMsrpMessage ftMsrpMessage5 = FtMsrpMessage.this;
                    if (ftMsrpMessage5.mThumbnailPath == null && ftMsrpMessage5.mThumbnailTool.isSupported(ftMsrpMessage5.mContentType)) {
                        String thumbSavedDirectory = FtMsrpMessage.this.mThumbnailTool.getThumbSavedDirectory();
                        FtMsrpMessage ftMsrpMessage6 = FtMsrpMessage.this;
                        ftMsrpMessage6.mThumbnailTool.createThumb(ftMsrpMessage6.mFilePath, thumbSavedDirectory, ftMsrpMessage6.MAX_SIZE_THUMBNAIL, obtainMessage(19));
                        return;
                    }
                }
                transitionTo(this.mAttachedState);
                return;
            }
            loge("Attached file (" + FtMsrpMessage.this.mFileSize + ") exceeds MaxSizeFileTr (" + max + ")");
            FtMsrpMessage.this.mCancelReason = CancelReason.TOO_LARGE;
            transitionTo(this.mCanceledState);
        }

        /* access modifiers changed from: private */
        public void onCreateThumbnail() {
            transitionTo(this.mAttachedState);
        }

        /* access modifiers changed from: private */
        public void onFileTransferInviteReceived(boolean z) {
            FtMsrpMessage ftMsrpMessage = FtMsrpMessage.this;
            ftMsrpMessage.mListener.onNotifyCloudMsgFtEvent(ftMsrpMessage);
            FtMsrpMessage ftMsrpMessage2 = FtMsrpMessage.this;
            if (ftMsrpMessage2.mStatus == ImConstants.Status.BLOCKED) {
                logi("Auto reject file transfer, session blocked");
                FtMsrpMessage.this.sendRejectFtSession(FtRejectReason.DECLINE);
                FtMsrpMessage.this.mCancelReason = CancelReason.CANCELED_BY_SYSTEM;
                transitionTo(this.mCancelingState);
                return;
            }
            long max = Math.max(ftMsrpMessage2.mConfig.getMaxSizeExtraFileTr(), FtMsrpMessage.this.mConfig.getMaxSizeFileTr());
            if (FtMsrpMessage.this.mConfig.getMaxSizeFileTrIncoming() != -1) {
                max = FtMsrpMessage.this.mConfig.getMaxSizeFileTrIncoming();
            }
            logi("onFileTransferInviteReceived(): mFileSize = " + FtMsrpMessage.this.mFileSize + " maxSizeFileTr = " + max);
            if (max != 0 && FtMsrpMessage.this.mFileSize > max) {
                loge("Auto reject file transfer, larger than max size mFileSize:" + FtMsrpMessage.this.mFileSize + ",MaxSizeFileTr:" + max);
                FtMsrpMessage.this.sendRejectFtSession(FtRejectReason.FORBIDDEN_MAX_SIZE_EXCEEDED);
                FtMsrpMessage.this.mCancelReason = CancelReason.CANCELED_BY_SYSTEM;
                transitionTo(this.mCancelingState);
            } else if (!FtMsrpMessage.this.isExternalStorageAvailable()) {
                loge("Auto reject file transfer, ExternalStorage is not Available");
                FtMsrpMessage.this.sendRejectFtSession(FtRejectReason.DECLINE);
                FtMsrpMessage.this.mCancelReason = CancelReason.CANCELED_BY_SYSTEM;
                transitionTo(this.mCancelingState);
            } else {
                try {
                    String incomingFileDestinationDir = FilePathGenerator.getIncomingFileDestinationDir(FtMsrpMessage.this.getContext(), FtMsrpMessage.this.mListener.onRequestIncomingFtTransferPath());
                    if (!z) {
                        if (FtMsrpMessage.this.getRcsStrategy().boolSetting(RcsPolicySettings.RcsPolicy.USE_TEMPFILE_WHEN_DOWNLOAD)) {
                            FtMsrpMessage ftMsrpMessage3 = FtMsrpMessage.this;
                            ftMsrpMessage3.mFilePath = FilePathGenerator.generateUniqueFilePath(incomingFileDestinationDir, FtMsrpMessage.this.mFileName + ".tmp", 128);
                        } else {
                            FtMsrpMessage ftMsrpMessage4 = FtMsrpMessage.this;
                            ftMsrpMessage4.mFilePath = FilePathGenerator.generateUniqueFilePath(incomingFileDestinationDir, ftMsrpMessage4.mFileName, 128);
                        }
                        if (new File(FtMsrpMessage.this.mFilePath).createNewFile()) {
                            logi("Created a file for received FT: " + FtMsrpMessage.this.mFilePath);
                        } else {
                            loge("Auto reject file transfer, Failed to create a file for received FT");
                            FtMsrpMessage.this.sendRejectFtSession(FtRejectReason.DECLINE);
                            FtMsrpMessage.this.mCancelReason = CancelReason.CANCELED_BY_SYSTEM;
                            transitionTo(this.mCancelingState);
                            return;
                        }
                    }
                    FtMsrpMessage ftMsrpMessage5 = FtMsrpMessage.this;
                    if (!FtMessage.checkAvailableStorage(incomingFileDestinationDir, ftMsrpMessage5.mFileSize - ftMsrpMessage5.mTransferredBytes)) {
                        loge("Auto reject file transfer, disk space not available");
                        FtMsrpMessage.this.sendRejectFtSession(FtRejectReason.DECLINE);
                        FtMsrpMessage.this.mCancelReason = CancelReason.CANCELED_BY_SYSTEM;
                        transitionTo(this.mCancelingState);
                        return;
                    }
                    IMnoStrategy rcsStrategy = FtMsrpMessage.this.getRcsStrategy();
                    FtMsrpMessage ftMsrpMessage6 = FtMsrpMessage.this;
                    rcsStrategy.forceRefreshCapability(ftMsrpMessage6.mListener.onRequestParticipantUris(ftMsrpMessage6.mChatId), true, (ImError) null);
                    FtMsrpMessage ftMsrpMessage7 = FtMsrpMessage.this;
                    ftMsrpMessage7.mListener.onTransferReceived(ftMsrpMessage7);
                    FtMsrpMessage ftMsrpMessage8 = FtMsrpMessage.this;
                    NotificationStatus notificationStatus = ftMsrpMessage8.mLastNotificationType;
                    if (notificationStatus == NotificationStatus.CANCELED) {
                        ftMsrpMessage8.mListener.onImdnNotificationReceived(ftMsrpMessage8, ftMsrpMessage8.mRemoteUri, notificationStatus, ftMsrpMessage8.mIsGroupChat);
                        FtMsrpMessage.this.mCancelReason = CancelReason.CANCELED_NOTIFICATION;
                        transitionTo(this.mCanceledState);
                        return;
                    }
                    transitionTo(this.mAcceptingState);
                } catch (IOException unused) {
                    loge("Auto reject file transfer, internal error");
                    FtMsrpMessage.this.sendRejectFtSession(FtRejectReason.NOT_ACCEPTABLE_HERE);
                    FtMsrpMessage.this.mCancelReason = CancelReason.CANCELED_BY_SYSTEM;
                    transitionTo(this.mCancelingState);
                }
            }
        }

        /* access modifiers changed from: private */
        public void onSendFile() {
            Log.i(FtMsrpMessage.this.LOG_TAG, "onSendFile");
            FtMsrpMessage ftMsrpMessage = FtMsrpMessage.this;
            boolean z = ftMsrpMessage.mIsResuming;
            Set<ImsUri> networkPreferredUri = ftMsrpMessage.mUriGenerator.getNetworkPreferredUri(UriGenerator.URIServiceType.RCS_URI, ftMsrpMessage.mListener.onRequestParticipantUris(ftMsrpMessage.mChatId));
            if (FtMsrpMessage.this.mSwapUriType) {
                Set<ImsUri> swapUriType = FtMsrpMessage.this.mUriGenerator.swapUriType(new ArrayList(networkPreferredUri));
                networkPreferredUri.clear();
                networkPreferredUri.addAll(swapUriType);
                FtMsrpMessage.this.mSwapUriType = false;
            }
            boolean z2 = FtMsrpMessage.this.getRcsStrategy().isResendFTResume(FtMsrpMessage.this.mIsGroupChat) ? false : z;
            if (FtMsrpMessage.this.getRcsStrategy().boolSetting(RcsPolicySettings.RcsPolicy.RESUME_WITH_COMPLETE_FILE)) {
                logi("resume resend complete file ");
                FtMsrpMessage.this.mTransferredBytes = 0;
            }
            if (FtMsrpMessage.this.getType() == ImConstants.Type.MULTIMEDIA_PUBLICACCOUNT) {
                HashSet hashSet = new HashSet();
                for (ImsUri imsUri : networkPreferredUri) {
                    hashSet.add(PublicAccountUri.convertToPublicAccountUri(imsUri.toString()));
                }
                networkPreferredUri = hashSet;
            }
            FtMsrpMessage ftMsrpMessage2 = FtMsrpMessage.this;
            int i = ftMsrpMessage2.mId;
            String str = ftMsrpMessage2.mContributionId;
            String str2 = ftMsrpMessage2.mConversationId;
            String str3 = ftMsrpMessage2.mInReplyToContributionId;
            Message obtainMessage = obtainMessage(2);
            Message obtainMessage2 = obtainMessage(22);
            ArrayList arrayList = new ArrayList(networkPreferredUri);
            ImsUri r13 = FtMsrpMessage.this.mConferenceUri;
            FtMsrpMessage ftMsrpMessage3 = FtMsrpMessage.this;
            String str4 = ftMsrpMessage3.mUserAlias;
            String str5 = ftMsrpMessage3.mFileName;
            String str6 = ftMsrpMessage3.mFilePath;
            long j = ftMsrpMessage3.mFileSize;
            String str7 = ftMsrpMessage3.mContentType;
            ImDirection imDirection = ftMsrpMessage3.mDirection;
            String str8 = str6;
            String str9 = str7;
            long j2 = ftMsrpMessage3.mTransferredBytes;
            Set<NotificationStatus> set = ftMsrpMessage3.mDispNotification;
            String str10 = ftMsrpMessage3.mImdnId;
            Date date = new Date();
            FtMsrpMessage ftMsrpMessage4 = FtMsrpMessage.this;
            String str11 = str10;
            String str12 = ftMsrpMessage4.mFileTransferId;
            String str13 = ftMsrpMessage4.mThumbnailPath;
            int i2 = ftMsrpMessage4.mTimeDuration;
            ImDirection imDirection2 = imDirection;
            boolean z3 = ftMsrpMessage4.getType() == ImConstants.Type.MULTIMEDIA_PUBLICACCOUNT;
            FtMsrpMessage ftMsrpMessage5 = FtMsrpMessage.this;
            SendFtSessionParams sendFtSessionParams = new SendFtSessionParams(i, str, str2, str3, obtainMessage, obtainMessage2, arrayList, r13, str4, str5, str8, j, str9, imDirection2, z2, j2, set, str11, date, str12, str13, i2, z3, ftMsrpMessage5.mFileFingerPrint, ftMsrpMessage5.mSimIMSI);
            FtMsrpMessage ftMsrpMessage6 = FtMsrpMessage.this;
            SendReportMsgParams sendReportMsgParams = ftMsrpMessage6.mReportMsgParams;
            if (sendReportMsgParams != null) {
                sendFtSessionParams.mReportMsgParams = sendReportMsgParams;
            }
            ftMsrpMessage6.mImsService.sendFtSession(sendFtSessionParams);
            if (!(FtMsrpMessage.this.getRcsStrategy().intSetting(RcsPolicySettings.RcsPolicy.SESSION_ESTABLISH_TIMER) <= 0 || FtMsrpMessage.this.mListener.onRequestRegistrationType() == null || FtMsrpMessage.this.mListener.onRequestRegistrationType().intValue() == 18)) {
                logi(getName() + " Stack response timer starts");
                removeMessages(17);
                sendMessageDelayed(obtainMessage(17), ((long) FtMsrpMessage.this.getRcsStrategy().intSetting(RcsPolicySettings.RcsPolicy.SESSION_ESTABLISH_TIMER)) * 1000);
            }
            transitionTo(this.mSendingState);
        }

        /* access modifiers changed from: private */
        public void onSendSlmFile() {
            if (FtMsrpMessage.this.sendSlmFile(obtainMessage(12))) {
                transitionTo(this.mSendingState);
            } else {
                transitionTo(this.mCanceledState);
            }
        }

        /* access modifiers changed from: private */
        public void handleFTFailure(IMnoStrategy.StatusCode statusCode, ImError imError) {
            int i = AnonymousClass1.$SwitchMap$com$sec$internal$ims$servicemodules$im$strategy$IMnoStrategy$StatusCode[statusCode.ordinal()];
            if (i == 1) {
                if (imError == ImError.UNSUPPORTED_URI_SCHEME) {
                    logi("onSendFileDone retry with other URI format");
                    FtMsrpMessage.this.mSwapUriType = true;
                }
                sendMessage(obtainMessage(18));
            } else if (i == 2) {
                sendMessageDelayed(obtainMessage(18), ((long) FtMsrpMessage.this.mRetryTimer) * 1000);
            } else if (i == 3) {
                sendMessageDelayed(obtainMessage(18), 1000);
            } else if (i == 4) {
                FtMsrpMessage.this.mCancelReason = FtMessage.translateToCancelReason(imError);
                IMnoStrategy rcsStrategy = FtMsrpMessage.this.getRcsStrategy();
                FtMsrpMessage ftMsrpMessage = FtMsrpMessage.this;
                rcsStrategy.forceRefreshCapability(ftMsrpMessage.mListener.onRequestParticipantUris(ftMsrpMessage.mChatId), false, imError);
                if (FtMsrpMessage.this.mDirection == ImDirection.INCOMING) {
                    transitionTo(this.mCanceledState);
                    return;
                }
                logi("SendingState: fallback to FtSLM: " + FtMsrpMessage.this.mCancelReason);
                FtMsrpMessage.this.mCancelReason = CancelReason.UNKNOWN;
                handleFallbackToSlm();
            } else if (i != 5) {
                setCancelReason(imError, false);
            } else {
                setCancelReason(imError, true);
            }
        }

        private void setCancelReason(ImError imError, boolean z) {
            FtMsrpMessage.this.mCancelReason = FtMessage.translateToCancelReason(imError);
            IMnoStrategy rcsStrategy = FtMsrpMessage.this.getRcsStrategy();
            FtMsrpMessage ftMsrpMessage = FtMsrpMessage.this;
            rcsStrategy.forceRefreshCapability(ftMsrpMessage.mListener.onRequestParticipantUris(ftMsrpMessage.mChatId), false, imError);
            if (z) {
                FtMsrpMessage ftMsrpMessage2 = FtMsrpMessage.this;
                if (ftMsrpMessage2.mDirection == ImDirection.OUTGOING) {
                    ftMsrpMessage2.setCancelReasonBasedOnLineType();
                }
            }
            transitionTo(this.mCanceledState);
        }

        /* access modifiers changed from: private */
        public void handleFallbackToSlm() {
            if (FtMsrpMessage.this.isChatbotMessage()) {
                logi("handleFallbackToSlm: Chatbot, Display Error");
                FtMsrpMessage.this.mCancelReason = CancelReason.FORBIDDEN_NO_RETRY_FALLBACK;
                transitionTo(this.mCanceledState);
                return;
            }
            FtMsrpMessage ftMsrpMessage = FtMsrpMessage.this;
            if (ftMsrpMessage.mFileSize > ftMsrpMessage.mConfig.getSlmMaxMsgSize()) {
                FtMsrpMessage ftMsrpMessage2 = FtMsrpMessage.this;
                if (!ftMsrpMessage2.mIsResizable || !ftMsrpMessage2.getRcsStrategy().boolSetting(RcsPolicySettings.RcsPolicy.SUPPORT_LARGE_MSG_RESIZING)) {
                    String r0 = FtMsrpMessage.this.LOG_TAG;
                    Log.i(r0, "File size is greater than allowed MaxSlmSize mFileSize:" + FtMsrpMessage.this.mFileSize + ", SLMMaxMsgSize:" + FtMsrpMessage.this.mConfig.getSlmMaxMsgSize());
                    FtMsrpMessage.this.setCancelReasonBasedOnLineType();
                    transitionTo(this.mCanceledState);
                    return;
                }
                FtMsrpMessage.this.setSlmSvcMsg(true);
                FtMsrpMessage.this.mRawHandle = null;
                logi("request resizing for LMM");
                FtMsrpMessage ftMsrpMessage3 = FtMsrpMessage.this;
                ftMsrpMessage3.mListener.onFileResizingNeeded(ftMsrpMessage3, ftMsrpMessage3.mConfig.getSlmMaxMsgSize());
                transitionTo(this.mSendingState);
                return;
            }
            FtMsrpMessage.this.setSlmSvcMsg(true);
            onSendSlmFile();
        }

        /* access modifiers changed from: private */
        public void handleRaceCondition(FtIncomingSessionEvent ftIncomingSessionEvent) {
            logi("handleRaceCondition msgId=" + FtMsrpMessage.this.mId);
            if (FtMsrpMessage.this.isOutgoing()) {
                FtMsrpMessage ftMsrpMessage = FtMsrpMessage.this;
                if (ftMsrpMessage.mTransferredBytes != 0) {
                    ftMsrpMessage.mRawHandle = ftIncomingSessionEvent.mRawHandle;
                    transitionTo(this.mAcceptingState);
                    sendMessage(4);
                    return;
                }
            }
            if (FtMsrpMessage.this.isOutgoing() || FtMsrpMessage.this.getIsSlmSvcMsg() || !ftIncomingSessionEvent.mIsSlmSvcMsg) {
                logi("Cancel Incoming FT");
                FtMsrpMessage.this.sendRejectFtSession(FtRejectReason.BUSY_HERE, ftIncomingSessionEvent);
                return;
            }
            Log.i(FtMsrpMessage.this.LOG_TAG, "updateFtMsrpMessageInfo: service has been changed to SLM by sender.");
            FtMsrpMessage.this.updateFtMessageInfo(ftIncomingSessionEvent);
            transitionTo(this.mAcceptingState);
            sendMessage(4);
        }

        /* access modifiers changed from: private */
        public void onHandleFileResizeResponse(FileResizeResponse fileResizeResponse) {
            if (FtMsrpMessage.this.validateFileResizeResponse(fileResizeResponse)) {
                FtMsrpMessage.this.deleteFile();
                File file = new File(fileResizeResponse.resizedFilePath);
                FtMsrpMessage.this.mFileSize = file.length();
                FtMsrpMessage.this.mFileName = file.getName();
                FtMsrpMessage ftMsrpMessage = FtMsrpMessage.this;
                ftMsrpMessage.mFilePath = fileResizeResponse.resizedFilePath;
                ftMsrpMessage.triggerObservers(ImCacheAction.UPDATED);
                if (getCurrentState() == this.mInitialState) {
                    transitionTo(this.mAttachedState);
                } else {
                    onSendSlmFile();
                }
            } else {
                FtMsrpMessage.this.setSlmSvcMsg(false);
                FtMsrpMessage.this.setCancelReasonBasedOnLineType();
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

        final class DefaultState extends State {
            DefaultState() {
            }

            public boolean processMessage(Message message) {
                int i = message.what;
                if (i == 13) {
                    FtMsrpMessage.this.onSendDeliveredNotificationDone();
                } else if (i == 15) {
                    FtMsrpStateMachine ftMsrpStateMachine = FtMsrpStateMachine.this;
                    ftMsrpStateMachine.logi(getName() + " EVENT_AUTOACCEPT_RESUMING : " + FtMsrpMessage.this.mId);
                    FtMsrpStateMachine ftMsrpStateMachine2 = FtMsrpStateMachine.this;
                    FtMsrpMessage ftMsrpMessage = FtMsrpMessage.this;
                    int i2 = ftMsrpMessage.mId;
                    Object obj = ftMsrpMessage.mRawHandle;
                    String str = ftMsrpMessage.mFilePath;
                    String str2 = ftMsrpMessage.mUserAlias;
                    Message obtainMessage = ftMsrpStateMachine2.obtainMessage(5);
                    FtMsrpMessage ftMsrpMessage2 = FtMsrpMessage.this;
                    FtMsrpMessage.this.mImsService.acceptFtSession(new AcceptFtSessionParams(i2, obj, str, str2, obtainMessage, ftMsrpMessage2.mTransferredBytes + 1, ftMsrpMessage2.mFileSize));
                    FtMsrpMessage.this.acquireWakeLock();
                    FtMsrpStateMachine ftMsrpStateMachine3 = FtMsrpStateMachine.this;
                    ftMsrpStateMachine3.transitionTo(ftMsrpStateMachine3.mAcceptingState);
                } else if (i != 23) {
                    if (FtMsrpStateMachine.this.getCurrentState() != null) {
                        FtMsrpStateMachine ftMsrpStateMachine4 = FtMsrpStateMachine.this;
                        ftMsrpStateMachine4.loge("Unexpected event, current state is " + FtMsrpStateMachine.this.getCurrentState().getName() + " event: " + message.what);
                    }
                    return false;
                } else {
                    FtMsrpStateMachine ftMsrpStateMachine5 = FtMsrpStateMachine.this;
                    ftMsrpStateMachine5.logi("EVENT_TRANSFER_TIMER_TIMEOUT : " + FtMsrpMessage.this.mId);
                    FtMsrpMessage.this.cancelTransfer(CancelReason.CANCELED_BY_SYSTEM);
                }
                return true;
            }
        }

        final class InitialState extends State {
            InitialState() {
            }

            public boolean processMessage(Message message) {
                int i = message.what;
                boolean z = false;
                if (i != 1) {
                    if (i == 8) {
                        FtMsrpStateMachine ftMsrpStateMachine = FtMsrpStateMachine.this;
                        FtMsrpMessage.this.mCancelReason = (CancelReason) message.obj;
                        ftMsrpStateMachine.transitionTo(ftMsrpStateMachine.mCanceledState);
                    } else if (i == 14) {
                        FtMsrpMessage.this.updateStatus(ImConstants.Status.QUEUED);
                    } else if (i == 16) {
                        FtMsrpStateMachine.this.onAttachSlmFile();
                    } else if (i == 10) {
                        FtMsrpStateMachine.this.onFileTransferInviteReceived(false);
                    } else if (i == 11) {
                        FtMsrpStateMachine.this.deferMessage(message);
                    } else if (i == 19) {
                        FtMsrpStateMachine ftMsrpStateMachine2 = FtMsrpStateMachine.this;
                        FtMsrpMessage.this.mThumbnailPath = (String) ((AsyncResult) message.obj).result;
                        ftMsrpStateMachine2.onCreateThumbnail();
                    } else if (i != 20) {
                        return false;
                    } else {
                        FtMsrpStateMachine.this.onHandleFileResizeResponse((FileResizeResponse) message.obj);
                    }
                } else if (FtMsrpMessage.this.isBroadcastMsg()) {
                    FtMsrpStateMachine.this.onAttachSlmFile();
                } else {
                    FtMsrpStateMachine ftMsrpStateMachine3 = FtMsrpStateMachine.this;
                    if (message.arg1 == 1) {
                        z = true;
                    }
                    ftMsrpStateMachine3.onAttachFile(z);
                }
                return true;
            }
        }

        final class AttachedState extends State {
            AttachedState() {
            }

            public void enter() {
                FtMsrpStateMachine ftMsrpStateMachine = FtMsrpStateMachine.this;
                ftMsrpStateMachine.logi(getName() + " enter msgId : " + FtMsrpMessage.this.mId);
                FtMsrpStateMachine ftMsrpStateMachine2 = FtMsrpStateMachine.this;
                FtMsrpMessage ftMsrpMessage = FtMsrpMessage.this;
                if (ftMsrpMessage.mIsResuming) {
                    ftMsrpStateMachine2.sendMessage(11);
                } else {
                    ftMsrpMessage.mListener.onTransferCreated(ftMsrpMessage);
                }
                FtMsrpMessage.this.updateState();
            }

            public boolean processMessage(Message message) {
                int i = message.what;
                if (i == 8) {
                    FtMsrpStateMachine ftMsrpStateMachine = FtMsrpStateMachine.this;
                    FtMsrpMessage.this.mCancelReason = (CancelReason) message.obj;
                    ftMsrpStateMachine.transitionTo(ftMsrpStateMachine.mCanceledState);
                } else if (i == 10) {
                    FtMsrpStateMachine ftMsrpStateMachine2 = FtMsrpStateMachine.this;
                    FtMsrpMessage.this.mRawHandle = ((FtIncomingSessionEvent) message.obj).mRawHandle;
                    ftMsrpStateMachine2.transitionTo(ftMsrpStateMachine2.mAcceptingState);
                    FtMsrpStateMachine.this.sendMessage(4);
                } else if (i != 11) {
                    return false;
                } else {
                    FtMsrpStateMachine ftMsrpStateMachine3 = FtMsrpStateMachine.this;
                    if (FtMsrpMessage.this.mIsSlmSvcMsg) {
                        ftMsrpStateMachine3.onSendSlmFile();
                    } else {
                        ftMsrpStateMachine3.onSendFile();
                    }
                }
                return true;
            }
        }

        final class SendingState extends State {
            SendingState() {
            }

            public void enter() {
                FtMsrpStateMachine ftMsrpStateMachine = FtMsrpStateMachine.this;
                ftMsrpStateMachine.logi(getName() + " enter msgId : " + FtMsrpMessage.this.mId);
                FtMsrpMessage.this.updateStatus(ImConstants.Status.SENDING);
                FtMsrpMessage.this.updateState();
                FtMsrpMessage.this.acquireWakeLock();
            }

            public boolean processMessage(Message message) {
                int i = message.what;
                if (i == 2) {
                    onSendFileDone((AsyncResult) message.obj);
                } else if (i == 3) {
                    FtTransferProgressEvent ftTransferProgressEvent = (FtTransferProgressEvent) message.obj;
                    if (!Objects.equals(FtMsrpMessage.this.mRawHandle, ftTransferProgressEvent.mRawHandle)) {
                        FtMsrpStateMachine ftMsrpStateMachine = FtMsrpStateMachine.this;
                        ftMsrpStateMachine.logi("EVENT_TRANSFER_PROGRESS: unknown rawHandle, ignore it: mRawHandle=" + FtMsrpMessage.this.mRawHandle + ", event.mRawHandle=" + ftTransferProgressEvent.mRawHandle);
                    } else {
                        FtMsrpStateMachine.this.removeMessages(17);
                        FtMsrpStateMachine ftMsrpStateMachine2 = FtMsrpStateMachine.this;
                        ftMsrpStateMachine2.logi("SendingState: EVENT_TRANSFER_PROGRESS event.mState = " + ftTransferProgressEvent.mState);
                        FtTransferProgressEvent.State state = ftTransferProgressEvent.mState;
                        if (state == FtTransferProgressEvent.State.COMPLETED) {
                            FtMsrpStateMachine ftMsrpStateMachine3 = FtMsrpStateMachine.this;
                            ftMsrpStateMachine3.transitionTo(ftMsrpStateMachine3.mCompletedState);
                        } else if (state == FtTransferProgressEvent.State.TRANSFERRING) {
                            FtMsrpStateMachine ftMsrpStateMachine4 = FtMsrpStateMachine.this;
                            ftMsrpStateMachine4.transitionTo(ftMsrpStateMachine4.mInProgressState);
                        } else {
                            FtMsrpMessage.this.mCancelReason = FtMessage.translateToCancelReason(ftTransferProgressEvent.mReason.getImError());
                            FtMsrpMessage ftMsrpMessage = FtMsrpMessage.this;
                            if (ftMsrpMessage.mIsSlmSvcMsg) {
                                ftMsrpMessage.setCancelReasonBasedOnLineType();
                            }
                            FtMsrpStateMachine ftMsrpStateMachine5 = FtMsrpStateMachine.this;
                            ftMsrpStateMachine5.transitionTo(ftMsrpStateMachine5.mCanceledState);
                        }
                    }
                } else if (i == 8) {
                    FtMsrpMessage ftMsrpMessage2 = FtMsrpMessage.this;
                    CancelReason cancelReason = (CancelReason) message.obj;
                    ftMsrpMessage2.mCancelReason = cancelReason;
                    if (ftMsrpMessage2.mRawHandle == null) {
                        Log.i(ftMsrpMessage2.LOG_TAG, "mRawHandle is null");
                        FtMsrpStateMachine ftMsrpStateMachine6 = FtMsrpStateMachine.this;
                        ftMsrpStateMachine6.transitionTo(ftMsrpStateMachine6.mCanceledState);
                    } else {
                        ftMsrpMessage2.sendCancelFtSession(cancelReason);
                        FtMsrpStateMachine ftMsrpStateMachine7 = FtMsrpStateMachine.this;
                        if (FtMsrpMessage.this.mCancelReason == CancelReason.CANCELED_BY_SYSTEM) {
                            ftMsrpStateMachine7.transitionTo(ftMsrpStateMachine7.mCanceledState);
                        } else {
                            ftMsrpStateMachine7.transitionTo(ftMsrpStateMachine7.mCancelingState);
                        }
                    }
                } else if (i == 10) {
                    FtMsrpStateMachine.this.handleRaceCondition((FtIncomingSessionEvent) message.obj);
                } else if (i == 12) {
                    AsyncResult asyncResult = (AsyncResult) message.obj;
                    if (asyncResult.exception == null) {
                        FtResult ftResult = (FtResult) asyncResult.result;
                        FtMsrpStateMachine ftMsrpStateMachine8 = FtMsrpStateMachine.this;
                        ftMsrpStateMachine8.logi("SLM send file done : " + ftResult.mRawHandle);
                        if (ftResult.getImError() == ImError.SUCCESS) {
                            FtMsrpMessage ftMsrpMessage3 = FtMsrpMessage.this;
                            ftMsrpMessage3.mRawHandle = ftResult.mRawHandle;
                            if (ftMsrpMessage3.getRcsStrategy().intSetting(RcsPolicySettings.RcsPolicy.SESSION_ESTABLISH_TIMER) <= 0 || FtMsrpMessage.this.mListener.onRequestRegistrationType() == null || FtMsrpMessage.this.mListener.onRequestRegistrationType().intValue() == 18) {
                                FtMsrpStateMachine ftMsrpStateMachine9 = FtMsrpStateMachine.this;
                                ftMsrpStateMachine9.transitionTo(ftMsrpStateMachine9.mInProgressState);
                            } else {
                                FtMsrpStateMachine ftMsrpStateMachine10 = FtMsrpStateMachine.this;
                                ftMsrpStateMachine10.logi(getName() + " Stack response timer starts");
                                FtMsrpStateMachine.this.removeMessages(17);
                                FtMsrpStateMachine ftMsrpStateMachine11 = FtMsrpStateMachine.this;
                                ftMsrpStateMachine11.sendMessageDelayed(ftMsrpStateMachine11.obtainMessage(17), ((long) FtMsrpMessage.this.getRcsStrategy().intSetting(RcsPolicySettings.RcsPolicy.SESSION_ESTABLISH_TIMER)) * 1000);
                            }
                        } else {
                            FtMsrpMessage.this.setCancelReasonBasedOnLineType();
                            FtMsrpStateMachine ftMsrpStateMachine12 = FtMsrpStateMachine.this;
                            ftMsrpStateMachine12.transitionTo(ftMsrpStateMachine12.mCanceledState);
                        }
                    }
                } else if (i == 20) {
                    FtMsrpStateMachine.this.onHandleFileResizeResponse((FileResizeResponse) message.obj);
                } else if (i == 22) {
                    FtMsrpStateMachine ftMsrpStateMachine13 = FtMsrpStateMachine.this;
                    FtMsrpMessage.this.mRawHandle = ((FtResult) ((AsyncResult) message.obj).result).mRawHandle;
                    ftMsrpStateMachine13.logi("update session handle mRawHandle=" + FtMsrpMessage.this.mRawHandle + " id = " + FtMsrpMessage.this.getId());
                } else if (i == 17) {
                    FtMsrpStateMachine.this.logi("Stack response timer expires, cancel file and fallback");
                    FtMsrpMessage.this.sendCancelFtSession(CancelReason.DEDICATED_BEARER_UNAVAILABLE_TIMEOUT);
                    FtMsrpStateMachine ftMsrpStateMachine14 = FtMsrpStateMachine.this;
                    ftMsrpStateMachine14.transitionTo(ftMsrpStateMachine14.mCancelingState);
                } else if (i != 18) {
                    return false;
                } else {
                    FtMsrpMessage.this.mContributionId = StringIdGenerator.generateContributionId();
                    FtMsrpStateMachine.this.onSendFile();
                    FtMsrpMessage.this.incrementRetryCount();
                }
                return true;
            }

            private void onSendFileDone(AsyncResult asyncResult) {
                if (asyncResult.exception != null) {
                    FtMsrpStateMachine ftMsrpStateMachine = FtMsrpStateMachine.this;
                    ftMsrpStateMachine.transitionTo(ftMsrpStateMachine.mCanceledState);
                    return;
                }
                FtResult ftResult = (FtResult) asyncResult.result;
                ImError imError = ftResult.getImError();
                FtMsrpMessage.this.mRetryTimer = ftResult.mRetryTimer;
                FtMsrpStateMachine.this.removeMessages(17);
                FtMsrpStateMachine ftMsrpStateMachine2 = FtMsrpStateMachine.this;
                ftMsrpStateMachine2.logi("onSendFileDone : " + imError + " retryTimer: " + FtMsrpMessage.this.mRetryTimer);
                if (imError == ImError.SUCCESS) {
                    FtMsrpStateMachine ftMsrpStateMachine3 = FtMsrpStateMachine.this;
                    FtMsrpMessage.this.mRawHandle = ftResult.mRawHandle;
                    ftMsrpStateMachine3.transitionTo(ftMsrpStateMachine3.mInProgressState);
                    return;
                }
                IMnoStrategy rcsStrategy = FtMsrpMessage.this.getRcsStrategy();
                int currentRetryCount = FtMsrpMessage.this.getCurrentRetryCount();
                int r3 = FtMsrpMessage.this.mRetryTimer;
                FtMsrpMessage ftMsrpMessage = FtMsrpMessage.this;
                IMnoStrategy.StrategyResponse handleSendingFtMsrpMessageFailure = rcsStrategy.handleSendingFtMsrpMessageFailure(imError, currentRetryCount, r3, ftMsrpMessage.mListener.onRequestChatType(ftMsrpMessage.mChatId), false);
                FtMsrpMessage.this.mErrorNotificationId = handleSendingFtMsrpMessageFailure.getErrorNotificationId();
                IMnoStrategy.StatusCode statusCode = handleSendingFtMsrpMessageFailure.getStatusCode();
                FtMsrpStateMachine ftMsrpStateMachine4 = FtMsrpStateMachine.this;
                ftMsrpStateMachine4.logi("SendingState: onSendFileDone. statusCode : " + statusCode);
                if (FtMsrpMessage.this.getRcsStrategy().isNeedToReportToRegiGvn(imError)) {
                    FtMsrpMessage ftMsrpMessage2 = FtMsrpMessage.this;
                    if (ftMsrpMessage2.mDirection == ImDirection.OUTGOING) {
                        ftMsrpMessage2.mListener.onFtErrorReport(imError);
                    }
                }
                if (!FtMsrpMessage.this.isChatbotMessage() || !(statusCode == IMnoStrategy.StatusCode.FALLBACK_TO_LEGACY || statusCode == IMnoStrategy.StatusCode.FALLBACK_TO_SLM || imError == ImError.GONE || imError == ImError.REQUEST_PENDING)) {
                    FtMsrpStateMachine.this.handleFTFailure(statusCode, imError);
                } else {
                    FtMsrpStateMachine.this.handleFTFailure(IMnoStrategy.StatusCode.DISPLAY_ERROR, ImError.FORBIDDEN_SERVICE_NOT_AUTHORISED);
                }
            }
        }

        final class AcceptingState extends State {
            AcceptingState() {
            }

            public void enter() {
                FtMsrpStateMachine ftMsrpStateMachine = FtMsrpStateMachine.this;
                ftMsrpStateMachine.logi(getName() + " enter msgId : " + FtMsrpMessage.this.mId);
                FtMsrpMessage ftMsrpMessage = FtMsrpMessage.this;
                ftMsrpMessage.mImsService.setFtMessageId(ftMsrpMessage.mRawHandle, ftMsrpMessage.mId);
                FtMsrpMessage.this.updateState();
            }

            /* JADX WARNING: Removed duplicated region for block: B:28:0x00d4  */
            /* JADX WARNING: Removed duplicated region for block: B:29:0x00e4  */
            /* JADX WARNING: Removed duplicated region for block: B:32:0x0108  */
            /* JADX WARNING: Removed duplicated region for block: B:33:0x012c  */
            /* Code decompiled incorrectly, please refer to instructions dump. */
            public boolean processMessage(android.os.Message r23) {
                /*
                    r22 = this;
                    r0 = r22
                    r1 = r23
                    int r2 = r1.what
                    r3 = 3
                    if (r2 == r3) goto L_0x013a
                    r3 = 4
                    r4 = 5
                    if (r2 == r3) goto L_0x0097
                    if (r2 == r4) goto L_0x005c
                    r3 = 6
                    if (r2 == r3) goto L_0x0042
                    r3 = 8
                    if (r2 == r3) goto L_0x0019
                    r0 = 0
                    goto L_0x01c6
                L_0x0019:
                    com.sec.internal.ims.servicemodules.im.FtMsrpMessage$FtMsrpStateMachine r2 = com.sec.internal.ims.servicemodules.im.FtMsrpMessage.FtMsrpStateMachine.this
                    com.sec.internal.ims.servicemodules.im.FtMsrpMessage r2 = com.sec.internal.ims.servicemodules.im.FtMsrpMessage.this
                    java.lang.Object r1 = r1.obj
                    com.sec.internal.constants.ims.servicemodules.im.reason.CancelReason r1 = (com.sec.internal.constants.ims.servicemodules.im.reason.CancelReason) r1
                    r2.mCancelReason = r1
                    r2.sendCancelFtSession(r1)
                    com.sec.internal.ims.servicemodules.im.FtMsrpMessage$FtMsrpStateMachine r0 = com.sec.internal.ims.servicemodules.im.FtMsrpMessage.FtMsrpStateMachine.this
                    com.sec.internal.ims.servicemodules.im.FtMsrpMessage r1 = com.sec.internal.ims.servicemodules.im.FtMsrpMessage.this
                    com.sec.internal.constants.ims.servicemodules.im.reason.CancelReason r1 = r1.mCancelReason
                    com.sec.internal.constants.ims.servicemodules.im.reason.CancelReason r2 = com.sec.internal.constants.ims.servicemodules.im.reason.CancelReason.CANCELED_BY_SYSTEM
                    if (r1 != r2) goto L_0x0039
                    com.sec.internal.helper.State r1 = r0.mCanceledState
                    r0.transitionTo(r1)
                    goto L_0x01c5
                L_0x0039:
                    com.sec.internal.helper.State r1 = r0.mCancelingState
                    r0.transitionTo(r1)
                    goto L_0x01c5
                L_0x0042:
                    com.sec.internal.ims.servicemodules.im.FtMsrpMessage$FtMsrpStateMachine r1 = com.sec.internal.ims.servicemodules.im.FtMsrpMessage.FtMsrpStateMachine.this
                    com.sec.internal.ims.servicemodules.im.FtMsrpMessage r1 = com.sec.internal.ims.servicemodules.im.FtMsrpMessage.this
                    com.sec.internal.constants.ims.servicemodules.im.reason.FtRejectReason r2 = com.sec.internal.constants.ims.servicemodules.im.reason.FtRejectReason.DECLINE
                    r1.sendRejectFtSession(r2)
                    com.sec.internal.ims.servicemodules.im.FtMsrpMessage$FtMsrpStateMachine r0 = com.sec.internal.ims.servicemodules.im.FtMsrpMessage.FtMsrpStateMachine.this
                    com.sec.internal.ims.servicemodules.im.FtMsrpMessage r1 = com.sec.internal.ims.servicemodules.im.FtMsrpMessage.this
                    com.sec.internal.constants.ims.servicemodules.im.reason.CancelReason r2 = com.sec.internal.constants.ims.servicemodules.im.reason.CancelReason.CANCELED_BY_USER
                    r1.mCancelReason = r2
                    com.sec.internal.helper.State r1 = r0.mCancelingState
                    r0.transitionTo(r1)
                    goto L_0x01c5
                L_0x005c:
                    java.lang.Object r1 = r1.obj
                    com.sec.internal.helper.AsyncResult r1 = (com.sec.internal.helper.AsyncResult) r1
                    java.lang.Object r1 = r1.result
                    com.sec.internal.constants.ims.servicemodules.im.result.FtResult r1 = (com.sec.internal.constants.ims.servicemodules.im.result.FtResult) r1
                    com.sec.internal.constants.ims.servicemodules.im.ImError r2 = r1.getImError()
                    com.sec.internal.constants.ims.servicemodules.im.ImError r3 = com.sec.internal.constants.ims.servicemodules.im.ImError.SUCCESS
                    if (r2 != r3) goto L_0x0077
                    com.sec.internal.ims.servicemodules.im.FtMsrpMessage$FtMsrpStateMachine r0 = com.sec.internal.ims.servicemodules.im.FtMsrpMessage.FtMsrpStateMachine.this
                    com.sec.internal.helper.State r1 = r0.mInProgressState
                    r0.transitionTo(r1)
                    goto L_0x01c5
                L_0x0077:
                    com.sec.internal.ims.servicemodules.im.FtMsrpMessage$FtMsrpStateMachine r2 = com.sec.internal.ims.servicemodules.im.FtMsrpMessage.FtMsrpStateMachine.this
                    java.lang.String r3 = "AcceptingState: Failed to accept transfer."
                    r2.loge(r3)
                    com.sec.internal.ims.servicemodules.im.FtMsrpMessage$FtMsrpStateMachine r2 = com.sec.internal.ims.servicemodules.im.FtMsrpMessage.FtMsrpStateMachine.this
                    com.sec.internal.ims.servicemodules.im.FtMsrpMessage r2 = com.sec.internal.ims.servicemodules.im.FtMsrpMessage.this
                    com.sec.internal.constants.ims.servicemodules.im.ImError r1 = r1.getImError()
                    com.sec.internal.constants.ims.servicemodules.im.reason.CancelReason r1 = com.sec.internal.ims.servicemodules.im.FtMessage.translateToCancelReason(r1)
                    r2.mCancelReason = r1
                    com.sec.internal.ims.servicemodules.im.FtMsrpMessage$FtMsrpStateMachine r0 = com.sec.internal.ims.servicemodules.im.FtMsrpMessage.FtMsrpStateMachine.this
                    com.sec.internal.helper.State r1 = r0.mCanceledState
                    r0.transitionTo(r1)
                    goto L_0x01c5
                L_0x0097:
                    com.sec.internal.ims.servicemodules.im.FtMsrpMessage$FtMsrpStateMachine r1 = com.sec.internal.ims.servicemodules.im.FtMsrpMessage.FtMsrpStateMachine.this
                    com.sec.internal.ims.servicemodules.im.FtMsrpMessage r1 = com.sec.internal.ims.servicemodules.im.FtMsrpMessage.this
                    long r2 = r1.mTransferredBytes
                    r5 = 0
                    int r7 = (r2 > r5 ? 1 : (r2 == r5 ? 0 : -1))
                    r8 = 1
                    if (r7 <= 0) goto L_0x00c2
                    long r2 = r2 + r8
                    long r10 = r1.mFileSize
                    com.sec.internal.ims.servicemodules.im.strategy.IMnoStrategy r1 = r1.getRcsStrategy()
                    com.sec.internal.ims.servicemodules.im.FtMsrpMessage$FtMsrpStateMachine r7 = com.sec.internal.ims.servicemodules.im.FtMsrpMessage.FtMsrpStateMachine.this
                    com.sec.internal.ims.servicemodules.im.FtMsrpMessage r7 = com.sec.internal.ims.servicemodules.im.FtMsrpMessage.this
                    boolean r7 = r7.mIsGroupChat
                    boolean r1 = r1.isResendFTResume(r7)
                    if (r1 == 0) goto L_0x00c0
                    com.sec.internal.ims.servicemodules.im.FtMsrpMessage$FtMsrpStateMachine r1 = com.sec.internal.ims.servicemodules.im.FtMsrpMessage.FtMsrpStateMachine.this
                    java.lang.String r2 = "Force FT to resume from the beginning"
                    r1.logi(r2)
                    goto L_0x00c2
                L_0x00c0:
                    r5 = r2
                    goto L_0x00c3
                L_0x00c2:
                    r10 = r5
                L_0x00c3:
                    com.sec.internal.ims.servicemodules.im.FtMsrpMessage$FtMsrpStateMachine r1 = com.sec.internal.ims.servicemodules.im.FtMsrpMessage.FtMsrpStateMachine.this
                    com.sec.internal.ims.servicemodules.im.FtMsrpMessage r1 = com.sec.internal.ims.servicemodules.im.FtMsrpMessage.this
                    com.sec.internal.ims.servicemodules.im.strategy.IMnoStrategy r1 = r1.getRcsStrategy()
                    java.lang.String r2 = "resume_with_complete_file"
                    boolean r1 = r1.boolSetting(r2)
                    if (r1 == 0) goto L_0x00e4
                    com.sec.internal.ims.servicemodules.im.FtMsrpMessage$FtMsrpStateMachine r1 = com.sec.internal.ims.servicemodules.im.FtMsrpMessage.FtMsrpStateMachine.this
                    java.lang.String r2 = "Request complete file"
                    r1.logi(r2)
                    com.sec.internal.ims.servicemodules.im.FtMsrpMessage$FtMsrpStateMachine r1 = com.sec.internal.ims.servicemodules.im.FtMsrpMessage.FtMsrpStateMachine.this
                    com.sec.internal.ims.servicemodules.im.FtMsrpMessage r1 = com.sec.internal.ims.servicemodules.im.FtMsrpMessage.this
                    long r10 = r1.mFileSize
                    r18 = r8
                    goto L_0x00e6
                L_0x00e4:
                    r18 = r5
                L_0x00e6:
                    r20 = r10
                    com.sec.internal.constants.ims.servicemodules.im.params.AcceptFtSessionParams r1 = new com.sec.internal.constants.ims.servicemodules.im.params.AcceptFtSessionParams
                    com.sec.internal.ims.servicemodules.im.FtMsrpMessage$FtMsrpStateMachine r2 = com.sec.internal.ims.servicemodules.im.FtMsrpMessage.FtMsrpStateMachine.this
                    com.sec.internal.ims.servicemodules.im.FtMsrpMessage r3 = com.sec.internal.ims.servicemodules.im.FtMsrpMessage.this
                    int r13 = r3.mId
                    java.lang.Object r14 = r3.mRawHandle
                    java.lang.String r15 = r3.mFilePath
                    java.lang.String r3 = r3.mUserAlias
                    android.os.Message r17 = r2.obtainMessage(r4)
                    r12 = r1
                    r16 = r3
                    r12.<init>(r13, r14, r15, r16, r17, r18, r20)
                    com.sec.internal.ims.servicemodules.im.FtMsrpMessage$FtMsrpStateMachine r2 = com.sec.internal.ims.servicemodules.im.FtMsrpMessage.FtMsrpStateMachine.this
                    com.sec.internal.ims.servicemodules.im.FtMsrpMessage r3 = com.sec.internal.ims.servicemodules.im.FtMsrpMessage.this
                    boolean r4 = r3.mIsSlmSvcMsg
                    if (r4 == 0) goto L_0x012c
                    java.lang.StringBuilder r3 = new java.lang.StringBuilder
                    r3.<init>()
                    java.lang.String r4 = "Accepting SLM message, msgId : "
                    r3.append(r4)
                    com.sec.internal.ims.servicemodules.im.FtMsrpMessage$FtMsrpStateMachine r4 = com.sec.internal.ims.servicemodules.im.FtMsrpMessage.FtMsrpStateMachine.this
                    com.sec.internal.ims.servicemodules.im.FtMsrpMessage r4 = com.sec.internal.ims.servicemodules.im.FtMsrpMessage.this
                    int r4 = r4.mId
                    r3.append(r4)
                    java.lang.String r3 = r3.toString()
                    r2.logi(r3)
                    com.sec.internal.ims.servicemodules.im.FtMsrpMessage$FtMsrpStateMachine r2 = com.sec.internal.ims.servicemodules.im.FtMsrpMessage.FtMsrpStateMachine.this
                    com.sec.internal.ims.servicemodules.im.FtMsrpMessage r2 = com.sec.internal.ims.servicemodules.im.FtMsrpMessage.this
                    com.sec.internal.interfaces.ims.servicemodules.im.ISlmServiceInterface r2 = r2.mSlmService
                    r2.acceptFtSlmMessage(r1)
                    goto L_0x0131
                L_0x012c:
                    com.sec.internal.interfaces.ims.servicemodules.im.IImServiceInterface r2 = r3.mImsService
                    r2.acceptFtSession(r1)
                L_0x0131:
                    com.sec.internal.ims.servicemodules.im.FtMsrpMessage$FtMsrpStateMachine r0 = com.sec.internal.ims.servicemodules.im.FtMsrpMessage.FtMsrpStateMachine.this
                    com.sec.internal.ims.servicemodules.im.FtMsrpMessage r0 = com.sec.internal.ims.servicemodules.im.FtMsrpMessage.this
                    r0.acquireWakeLock()
                    goto L_0x01c5
                L_0x013a:
                    java.lang.Object r1 = r1.obj
                    com.sec.internal.constants.ims.servicemodules.im.event.FtTransferProgressEvent r1 = (com.sec.internal.constants.ims.servicemodules.im.event.FtTransferProgressEvent) r1
                    com.sec.internal.ims.servicemodules.im.FtMsrpMessage$FtMsrpStateMachine r2 = com.sec.internal.ims.servicemodules.im.FtMsrpMessage.FtMsrpStateMachine.this
                    com.sec.internal.ims.servicemodules.im.FtMsrpMessage r2 = com.sec.internal.ims.servicemodules.im.FtMsrpMessage.this
                    java.lang.Object r2 = r2.mRawHandle
                    java.lang.Object r3 = r1.mRawHandle
                    boolean r2 = java.util.Objects.equals(r2, r3)
                    if (r2 != 0) goto L_0x0173
                    com.sec.internal.ims.servicemodules.im.FtMsrpMessage$FtMsrpStateMachine r2 = com.sec.internal.ims.servicemodules.im.FtMsrpMessage.FtMsrpStateMachine.this
                    java.lang.StringBuilder r3 = new java.lang.StringBuilder
                    r3.<init>()
                    java.lang.String r4 = "EVENT_TRANSFER_PROGRESS: unknown rawHandle, ignore it: mRawHandle="
                    r3.append(r4)
                    com.sec.internal.ims.servicemodules.im.FtMsrpMessage$FtMsrpStateMachine r0 = com.sec.internal.ims.servicemodules.im.FtMsrpMessage.FtMsrpStateMachine.this
                    com.sec.internal.ims.servicemodules.im.FtMsrpMessage r0 = com.sec.internal.ims.servicemodules.im.FtMsrpMessage.this
                    java.lang.Object r0 = r0.mRawHandle
                    r3.append(r0)
                    java.lang.String r0 = ", event.mRawHandle="
                    r3.append(r0)
                    java.lang.Object r0 = r1.mRawHandle
                    r3.append(r0)
                    java.lang.String r0 = r3.toString()
                    r2.logi(r0)
                    goto L_0x01c5
                L_0x0173:
                    com.sec.internal.ims.servicemodules.im.FtMsrpMessage$FtMsrpStateMachine r2 = com.sec.internal.ims.servicemodules.im.FtMsrpMessage.FtMsrpStateMachine.this
                    r3 = 23
                    r2.removeMessages(r3)
                    com.sec.internal.ims.servicemodules.im.FtMsrpMessage$FtMsrpStateMachine r2 = com.sec.internal.ims.servicemodules.im.FtMsrpMessage.FtMsrpStateMachine.this
                    android.os.Message r3 = r2.obtainMessage(r3)
                    r4 = 300000(0x493e0, double:1.482197E-318)
                    r2.sendMessageDelayed((android.os.Message) r3, (long) r4)
                    com.sec.internal.constants.ims.servicemodules.im.event.FtTransferProgressEvent$State r2 = r1.mState
                    com.sec.internal.constants.ims.servicemodules.im.event.FtTransferProgressEvent$State r3 = com.sec.internal.constants.ims.servicemodules.im.event.FtTransferProgressEvent.State.CANCELED
                    if (r2 != r3) goto L_0x01a6
                    com.sec.internal.ims.servicemodules.im.FtMsrpMessage$FtMsrpStateMachine r2 = com.sec.internal.ims.servicemodules.im.FtMsrpMessage.FtMsrpStateMachine.this
                    com.sec.internal.ims.servicemodules.im.FtMsrpMessage r2 = com.sec.internal.ims.servicemodules.im.FtMsrpMessage.this
                    com.sec.internal.constants.ims.servicemodules.im.result.Result r1 = r1.mReason
                    com.sec.internal.constants.ims.servicemodules.im.ImError r1 = r1.getImError()
                    com.sec.internal.constants.ims.servicemodules.im.reason.CancelReason r1 = com.sec.internal.ims.servicemodules.im.FtMessage.translateToCancelReason(r1)
                    r2.mCancelReason = r1
                    com.sec.internal.ims.servicemodules.im.FtMsrpMessage$FtMsrpStateMachine r0 = com.sec.internal.ims.servicemodules.im.FtMsrpMessage.FtMsrpStateMachine.this
                    com.sec.internal.helper.State r1 = r0.mCanceledState
                    r0.transitionTo(r1)
                    goto L_0x01c5
                L_0x01a6:
                    com.sec.internal.ims.servicemodules.im.FtMsrpMessage$FtMsrpStateMachine r2 = com.sec.internal.ims.servicemodules.im.FtMsrpMessage.FtMsrpStateMachine.this
                    java.lang.StringBuilder r3 = new java.lang.StringBuilder
                    r3.<init>()
                    java.lang.String r0 = r22.getName()
                    r3.append(r0)
                    java.lang.String r0 = ": Unexpected progress state "
                    r3.append(r0)
                    com.sec.internal.constants.ims.servicemodules.im.event.FtTransferProgressEvent$State r0 = r1.mState
                    r3.append(r0)
                    java.lang.String r0 = r3.toString()
                    r2.loge(r0)
                L_0x01c5:
                    r0 = 1
                L_0x01c6:
                    return r0
                */
                throw new UnsupportedOperationException("Method not decompiled: com.sec.internal.ims.servicemodules.im.FtMsrpMessage.FtMsrpStateMachine.AcceptingState.processMessage(android.os.Message):boolean");
            }
        }

        final class InProgressState extends State {
            InProgressState() {
            }

            public void enter() {
                FtMsrpStateMachine ftMsrpStateMachine = FtMsrpStateMachine.this;
                ftMsrpStateMachine.logi(getName() + " enter msgId : " + FtMsrpMessage.this.mId + " isSlm : " + FtMsrpMessage.this.mIsSlmSvcMsg);
                FtMsrpMessage.this.updateState();
                FtMsrpMessage ftMsrpMessage = FtMsrpMessage.this;
                ftMsrpMessage.mListener.onTransferInProgress(ftMsrpMessage);
            }

            public boolean processMessage(Message message) {
                int i = message.what;
                if (i == 3) {
                    FtTransferProgressEvent ftTransferProgressEvent = (FtTransferProgressEvent) message.obj;
                    if (!Objects.equals(FtMsrpMessage.this.mRawHandle, ftTransferProgressEvent.mRawHandle)) {
                        FtMsrpStateMachine ftMsrpStateMachine = FtMsrpStateMachine.this;
                        ftMsrpStateMachine.logi("EVENT_TRANSFER_PROGRESS: unknown rawHandle, ignore it: mRawHandle=" + FtMsrpMessage.this.mRawHandle + ", event.mRawHandle=" + ftTransferProgressEvent.mRawHandle);
                        return true;
                    }
                    FtMsrpStateMachine.this.removeMessages(23);
                    FtMsrpStateMachine ftMsrpStateMachine2 = FtMsrpStateMachine.this;
                    ftMsrpStateMachine2.sendMessageDelayed(ftMsrpStateMachine2.obtainMessage(23), 300000);
                    int i2 = AnonymousClass1.$SwitchMap$com$sec$internal$constants$ims$servicemodules$im$event$FtTransferProgressEvent$State[ftTransferProgressEvent.mState.ordinal()];
                    if (i2 == 1) {
                        FtMsrpMessage ftMsrpMessage = FtMsrpMessage.this;
                        ftMsrpMessage.updateTransferredBytes((ftMsrpMessage.mFileSize - ftTransferProgressEvent.mTotal) + ftTransferProgressEvent.mTransferred);
                        FtMsrpMessage ftMsrpMessage2 = FtMsrpMessage.this;
                        ftMsrpMessage2.mListener.onTransferProgressReceived(ftMsrpMessage2);
                        return true;
                    } else if (i2 == 2) {
                        FtMsrpMessage.this.mCancelReason = FtMessage.translateToCancelReason(ftTransferProgressEvent.mReason.getImError());
                        FtMsrpMessage ftMsrpMessage3 = FtMsrpMessage.this;
                        if (ftMsrpMessage3.mIsSlmSvcMsg) {
                            ftMsrpMessage3.setCancelReasonBasedOnLineType();
                        }
                        FtMsrpStateMachine ftMsrpStateMachine3 = FtMsrpStateMachine.this;
                        ftMsrpStateMachine3.transitionTo(ftMsrpStateMachine3.mCanceledState);
                        return true;
                    } else if (i2 == 3) {
                        FtMsrpMessage ftMsrpMessage4 = FtMsrpMessage.this;
                        if ((ftMsrpMessage4.mDirection == ImDirection.INCOMING || ftMsrpMessage4.mFilePath.endsWith(".tmp")) && !FtMsrpMessage.this.renameFile()) {
                            FtMsrpStateMachine ftMsrpStateMachine4 = FtMsrpStateMachine.this;
                            FtMsrpMessage.this.mCancelReason = CancelReason.CANCELED_BY_SYSTEM;
                            ftMsrpStateMachine4.transitionTo(ftMsrpStateMachine4.mCanceledState);
                            return true;
                        }
                        FtMsrpStateMachine ftMsrpStateMachine5 = FtMsrpStateMachine.this;
                        ftMsrpStateMachine5.transitionTo(ftMsrpStateMachine5.mCompletedState);
                        return true;
                    } else if (i2 != 4) {
                        return true;
                    } else {
                        FtMsrpStateMachine ftMsrpStateMachine6 = FtMsrpStateMachine.this;
                        FtMsrpMessage ftMsrpMessage5 = FtMsrpMessage.this;
                        ftMsrpMessage5.mTransferredBytes = (ftMsrpMessage5.mFileSize - ftTransferProgressEvent.mTotal) + ftTransferProgressEvent.mTransferred;
                        ftMsrpStateMachine6.logi("INTERRUPTED mFileSize: " + FtMsrpMessage.this.mFileSize + " mTotal: " + ftTransferProgressEvent.mTotal + " mTransferred: " + ftTransferProgressEvent.mTransferred);
                        FtMsrpMessage.this.mCancelReason = FtMessage.translateToCancelReason(ftTransferProgressEvent.mReason.getImError());
                        FtMsrpMessage ftMsrpMessage6 = FtMsrpMessage.this;
                        if (ftMsrpMessage6.mIsSlmSvcMsg) {
                            if (ftMsrpMessage6.mCancelReason != CancelReason.REJECTED_BY_REMOTE) {
                                ftMsrpMessage6.setCancelReasonBasedOnLineType();
                            }
                            FtMsrpStateMachine ftMsrpStateMachine7 = FtMsrpStateMachine.this;
                            ftMsrpStateMachine7.transitionTo(ftMsrpStateMachine7.mCanceledState);
                            return true;
                        } else if (ftMsrpMessage6.mDirection != ImDirection.INCOMING || !ftMsrpMessage6.mFilePath.endsWith(".tmp") || ftTransferProgressEvent.mTotal != ftTransferProgressEvent.mTransferred || !FtMsrpMessage.this.renameFile()) {
                            onTransferInterrupted(ftTransferProgressEvent);
                            return true;
                        } else {
                            FtMsrpStateMachine.this.logi("Transferred size is same with total size");
                            FtMsrpStateMachine ftMsrpStateMachine8 = FtMsrpStateMachine.this;
                            ftMsrpStateMachine8.transitionTo(ftMsrpStateMachine8.mCompletedState);
                            return true;
                        }
                    }
                } else if (i == 8) {
                    FtMsrpMessage ftMsrpMessage7 = FtMsrpMessage.this;
                    CancelReason cancelReason = (CancelReason) message.obj;
                    ftMsrpMessage7.mCancelReason = cancelReason;
                    ftMsrpMessage7.sendCancelFtSession(cancelReason);
                    FtMsrpStateMachine ftMsrpStateMachine9 = FtMsrpStateMachine.this;
                    if (FtMsrpMessage.this.mCancelReason == CancelReason.CANCELED_BY_SYSTEM) {
                        ftMsrpStateMachine9.transitionTo(ftMsrpStateMachine9.mCanceledState);
                        return true;
                    }
                    ftMsrpStateMachine9.transitionTo(ftMsrpStateMachine9.mCancelingState);
                    return true;
                } else if (i != 10) {
                    return false;
                } else {
                    FtMsrpStateMachine.this.handleRaceCondition((FtIncomingSessionEvent) message.obj);
                    return true;
                }
            }

            private void onTransferInterrupted(FtTransferProgressEvent ftTransferProgressEvent) {
                Result result = ftTransferProgressEvent.mReason;
                IMnoStrategy.StatusCode statusCode = FtMsrpMessage.this.getRcsStrategy().handleFtMsrpInterruption(result.getImError()).getStatusCode();
                FtMsrpStateMachine ftMsrpStateMachine = FtMsrpStateMachine.this;
                ftMsrpStateMachine.logi("onTransferInterrupted() : errorReason : " + result + ", statusCode : " + statusCode);
                ArrayList arrayList = new ArrayList();
                arrayList.add(String.valueOf(result.getReasonHdr() != null ? result.getReasonHdr().getCode() : 0));
                arrayList.add(String.valueOf(FtMsrpMessage.this.getRetryCount()));
                FtMsrpMessage.this.listToDumpFormat(LogClass.FT_MSRP_CANCEL, 0, arrayList);
                int i = AnonymousClass1.$SwitchMap$com$sec$internal$ims$servicemodules$im$strategy$IMnoStrategy$StatusCode[statusCode.ordinal()];
                if (i == 4) {
                    FtMsrpStateMachine ftMsrpStateMachine2 = FtMsrpStateMachine.this;
                    if (FtMsrpMessage.this.mDirection == ImDirection.INCOMING) {
                        ftMsrpStateMachine2.transitionTo(ftMsrpStateMachine2.mCanceledState);
                        return;
                    }
                    ftMsrpStateMachine2.logi("onTransferInterrupted : fallback to FtSLM: " + FtMsrpMessage.this.mCancelReason);
                    FtMsrpStateMachine ftMsrpStateMachine3 = FtMsrpStateMachine.this;
                    FtMsrpMessage.this.mCancelReason = CancelReason.UNKNOWN;
                    ftMsrpStateMachine3.handleFallbackToSlm();
                } else if (i != 5) {
                    FtMsrpStateMachine ftMsrpStateMachine4 = FtMsrpStateMachine.this;
                    ftMsrpStateMachine4.transitionTo(ftMsrpStateMachine4.mCanceledState);
                } else {
                    FtMsrpMessage.this.setCancelReasonBasedOnLineType();
                    FtMsrpStateMachine ftMsrpStateMachine5 = FtMsrpStateMachine.this;
                    ftMsrpStateMachine5.transitionTo(ftMsrpStateMachine5.mCanceledState);
                }
            }
        }

        final class CompletedState extends State {
            CompletedState() {
            }

            public void enter() {
                FtMsrpStateMachine ftMsrpStateMachine = FtMsrpStateMachine.this;
                ftMsrpStateMachine.log(getName() + " enter msgId : " + FtMsrpMessage.this.mId);
                FtMsrpStateMachine ftMsrpStateMachine2 = FtMsrpStateMachine.this;
                FtMsrpMessage ftMsrpMessage = FtMsrpMessage.this;
                if (ftMsrpMessage.mIsBootup) {
                    ftMsrpStateMachine2.logi("Message is loaded from bootup, no need for notifications");
                    FtMsrpMessage.this.mIsBootup = false;
                    return;
                }
                if (ftMsrpMessage.getDirection() == ImDirection.OUTGOING) {
                    FtMsrpMessage.this.setSentTimestamp(System.currentTimeMillis());
                    FtMsrpMessage.this.updateStatus(ImConstants.Status.SENT);
                    FtMsrpMessage.this.deleteThumbnail();
                    FtMsrpMessage.this.deleteFile();
                } else {
                    FtMsrpMessage.this.moveCachedFileToApp();
                    FtMsrpMessage.this.updateStatus(ImConstants.Status.UNREAD);
                }
                if (FtMsrpMessage.this.isDeliveredNotificationRequired()) {
                    FtMsrpMessage.this.setDesiredNotificationStatus(NotificationStatus.DELIVERED);
                    FtMsrpMessage.this.updateDeliveredTimestamp(System.currentTimeMillis());
                    FtMsrpMessage ftMsrpMessage2 = FtMsrpMessage.this;
                    ftMsrpMessage2.mListener.onSendDeliveredNotification(ftMsrpMessage2);
                }
                FtMsrpMessage ftMsrpMessage3 = FtMsrpMessage.this;
                ftMsrpMessage3.mIsConferenceUriChanged = false;
                ftMsrpMessage3.invokeFtQueueCallBack();
                FtMsrpStateMachine.this.removeMessages(21);
                FtMsrpStateMachine.this.removeMessages(23);
                PreciseAlarmManager.getInstance(FtMsrpMessage.this.getContext()).removeMessage(FtMsrpStateMachine.this.obtainMessage(21));
                FtMsrpMessage.this.updateState();
                FtMsrpMessage.this.listToDumpFormat(LogClass.FT_MSRP_COMPLETE, 0, new ArrayList());
                FtMsrpMessage.this.releaseWakeLock();
                FtMsrpMessage ftMsrpMessage4 = FtMsrpMessage.this;
                ftMsrpMessage4.mListener.onTransferCompleted(ftMsrpMessage4);
            }

            public boolean processMessage(Message message) {
                int i = message.what;
                if (i == 4) {
                    FtMsrpStateMachine ftMsrpStateMachine = FtMsrpStateMachine.this;
                    ftMsrpStateMachine.logi("FT already complete. Try to file copy to APP. msgId : " + FtMsrpMessage.this.mId);
                    FtMsrpMessage.this.moveCachedFileToApp();
                    return true;
                } else if (i == 8) {
                    FtMsrpMessage ftMsrpMessage = FtMsrpMessage.this;
                    ftMsrpMessage.mListener.onCancelRequestFailed(ftMsrpMessage);
                    return true;
                } else if (i != 10) {
                    return false;
                } else {
                    FtMsrpStateMachine ftMsrpStateMachine2 = FtMsrpStateMachine.this;
                    ftMsrpStateMachine2.logi(getName() + " msgId : " + FtMsrpMessage.this.mId + " resuming request after ft is completed");
                    FtMsrpStateMachine ftMsrpStateMachine3 = FtMsrpStateMachine.this;
                    FtMsrpMessage ftMsrpMessage2 = FtMsrpMessage.this;
                    if (ftMsrpMessage2.mDirection != ImDirection.OUTGOING || ftMsrpMessage2.mIsSlmSvcMsg) {
                        ftMsrpMessage2.sendRejectFtSession(FtRejectReason.DECLINE, (FtIncomingSessionEvent) message.obj);
                        FtMsrpMessage.this.invokeFtQueueCallBack();
                        return true;
                    }
                    ftMsrpMessage2.mRawHandle = ((FtIncomingSessionEvent) message.obj).mRawHandle;
                    ftMsrpStateMachine3.onFileTransferInviteReceived(true);
                    return true;
                }
            }
        }

        final class CancelingState extends State {
            CancelingState() {
            }

            public void enter() {
                FtMsrpStateMachine ftMsrpStateMachine = FtMsrpStateMachine.this;
                ftMsrpStateMachine.logi(getName() + " enter msgId : " + FtMsrpMessage.this.mId);
                FtMsrpMessage.this.updateState();
            }

            public boolean processMessage(Message message) {
                int i = message.what;
                if (i == 2) {
                    AsyncResult asyncResult = (AsyncResult) message.obj;
                    if (asyncResult == null) {
                        return true;
                    }
                    ImError imError = ((FtResult) asyncResult.result).getImError();
                    FtMsrpStateMachine ftMsrpStateMachine = FtMsrpStateMachine.this;
                    ftMsrpStateMachine.logi("onSendFileDone in CancelingState: " + imError);
                    return true;
                } else if (i == 3) {
                    FtTransferProgressEvent ftTransferProgressEvent = (FtTransferProgressEvent) message.obj;
                    if (!Objects.equals(FtMsrpMessage.this.mRawHandle, ftTransferProgressEvent.mRawHandle)) {
                        FtMsrpStateMachine ftMsrpStateMachine2 = FtMsrpStateMachine.this;
                        ftMsrpStateMachine2.logi("EVENT_TRANSFER_PROGRESS: unknown rawHandle, ignore it: mRawHandle=" + FtMsrpMessage.this.mRawHandle + ", event.mRawHandle=" + ftTransferProgressEvent.mRawHandle);
                        return true;
                    }
                    FtMsrpStateMachine.this.removeMessages(23);
                    FtMsrpStateMachine ftMsrpStateMachine3 = FtMsrpStateMachine.this;
                    ftMsrpStateMachine3.sendMessageDelayed(ftMsrpStateMachine3.obtainMessage(23), 300000);
                    int i2 = AnonymousClass1.$SwitchMap$com$sec$internal$constants$ims$servicemodules$im$event$FtTransferProgressEvent$State[ftTransferProgressEvent.mState.ordinal()];
                    if (i2 != 1) {
                        if (i2 != 2) {
                            if (i2 == 3) {
                                FtMsrpMessage ftMsrpMessage = FtMsrpMessage.this;
                                if ((ftMsrpMessage.mDirection == ImDirection.INCOMING || ftMsrpMessage.mFilePath.endsWith(".tmp")) && !FtMsrpMessage.this.renameFile()) {
                                    FtMsrpStateMachine ftMsrpStateMachine4 = FtMsrpStateMachine.this;
                                    FtMsrpMessage.this.mCancelReason = CancelReason.CANCELED_BY_SYSTEM;
                                    ftMsrpStateMachine4.transitionTo(ftMsrpStateMachine4.mCanceledState);
                                    return true;
                                }
                                FtMsrpStateMachine ftMsrpStateMachine5 = FtMsrpStateMachine.this;
                                ftMsrpStateMachine5.transitionTo(ftMsrpStateMachine5.mCompletedState);
                                return true;
                            } else if (i2 != 4) {
                                return true;
                            }
                        }
                        FtMsrpStateMachine ftMsrpStateMachine6 = FtMsrpStateMachine.this;
                        if (FtMsrpMessage.this.mCancelReason == CancelReason.DEDICATED_BEARER_UNAVAILABLE_TIMEOUT) {
                            handleFTTimeout(ImError.DEDICATED_BEARER_FALLBACK);
                            return true;
                        }
                        ftMsrpStateMachine6.transitionTo(ftMsrpStateMachine6.mCanceledState);
                        return true;
                    }
                    FtMsrpMessage ftMsrpMessage2 = FtMsrpMessage.this;
                    ftMsrpMessage2.updateTransferredBytes((ftMsrpMessage2.mFileSize - ftTransferProgressEvent.mTotal) + ftTransferProgressEvent.mTransferred);
                    return true;
                } else if (i == 7) {
                    AsyncResult asyncResult2 = (AsyncResult) message.obj;
                    if (asyncResult2 == null) {
                        return true;
                    }
                    if (((FtResult) asyncResult2.result).getImError() != ImError.SUCCESS) {
                        FtMsrpStateMachine.this.loge("CancelingState: Failed to cancel transfer.");
                    }
                    FtMsrpStateMachine ftMsrpStateMachine7 = FtMsrpStateMachine.this;
                    ftMsrpStateMachine7.transitionTo(ftMsrpStateMachine7.mCanceledState);
                    return true;
                } else if (i == 8) {
                    CancelReason cancelReason = (CancelReason) message.obj;
                    FtMsrpStateMachine ftMsrpStateMachine8 = FtMsrpStateMachine.this;
                    ftMsrpStateMachine8.logi("cancel transfer in cancelingState reason = " + cancelReason);
                    CancelReason cancelReason2 = CancelReason.CANCELED_BY_SYSTEM;
                    if (cancelReason != cancelReason2) {
                        return true;
                    }
                    FtMsrpStateMachine ftMsrpStateMachine9 = FtMsrpStateMachine.this;
                    FtMsrpMessage.this.mCancelReason = cancelReason2;
                    ftMsrpStateMachine9.transitionTo(ftMsrpStateMachine9.mCanceledState);
                    return true;
                } else if (i != 9) {
                    return false;
                } else {
                    AsyncResult asyncResult3 = (AsyncResult) message.obj;
                    if (asyncResult3 == null) {
                        return true;
                    }
                    String r0 = FtMsrpMessage.this.LOG_TAG;
                    Log.i(r0, "CancelingState: cancel transfer result = " + ((FtResult) asyncResult3.result));
                    FtMsrpStateMachine ftMsrpStateMachine10 = FtMsrpStateMachine.this;
                    if (FtMsrpMessage.this.mCancelReason == CancelReason.DEDICATED_BEARER_UNAVAILABLE_TIMEOUT) {
                        handleFTTimeout(ImError.DEDICATED_BEARER_FALLBACK);
                        return true;
                    }
                    ftMsrpStateMachine10.transitionTo(ftMsrpStateMachine10.mCanceledState);
                    return true;
                }
            }

            private void handleFTTimeout(ImError imError) {
                IMnoStrategy rcsStrategy = FtMsrpMessage.this.getRcsStrategy();
                FtMsrpMessage ftMsrpMessage = FtMsrpMessage.this;
                IMnoStrategy.StrategyResponse handleFtFailure = rcsStrategy.handleFtFailure(imError, ftMsrpMessage.mListener.onRequestChatType(ftMsrpMessage.mChatId));
                if (handleFtFailure.getStatusCode() == IMnoStrategy.StatusCode.FALLBACK_TO_SLM) {
                    FtMsrpStateMachine ftMsrpStateMachine = FtMsrpStateMachine.this;
                    if (FtMsrpMessage.this.mIsSlmSvcMsg) {
                        ftMsrpStateMachine.logi("handleFTTimeout: FALLBACK_TO_LEGACY for slm FT");
                        FtMsrpStateMachine.this.handleFTFailure(IMnoStrategy.StatusCode.FALLBACK_TO_LEGACY, imError);
                        return;
                    }
                }
                FtMsrpStateMachine.this.handleFTFailure(handleFtFailure.getStatusCode(), imError);
            }
        }

        final class CanceledState extends State {
            CanceledState() {
            }

            public void enter() {
                FtMsrpStateMachine.this.logi(getName() + " enter msgId : " + FtMsrpMessage.this.mId);
                FtMsrpStateMachine ftMsrpStateMachine = FtMsrpStateMachine.this;
                FtMsrpMessage ftMsrpMessage = FtMsrpMessage.this;
                if (ftMsrpMessage.mIsBootup) {
                    ftMsrpStateMachine.logi("Message is loaded from bootup, no need for notifications");
                    FtMsrpMessage.this.mIsBootup = false;
                    return;
                }
                IMnoStrategy rcsStrategy = ftMsrpMessage.getRcsStrategy();
                if (rcsStrategy == null) {
                    FtMsrpStateMachine.this.loge("mnoStrategy is null");
                    return;
                }
                FtMsrpMessage ftMsrpMessage2 = FtMsrpMessage.this;
                if (ftMsrpMessage2.mIsSlmSvcMsg) {
                    ftMsrpMessage2.mResumableOptionCode = FtResumableOption.NOTRESUMABLE.getId();
                } else {
                    ftMsrpMessage2.mResumableOptionCode = rcsStrategy.getftResumableOption(ftMsrpMessage2.mCancelReason, ftMsrpMessage2.mIsGroupChat, ftMsrpMessage2.mDirection, ftMsrpMessage2.getTransferMech()).getId();
                }
                FtMsrpStateMachine.this.logi(getName() + " mResumableOptionCode: " + FtMsrpMessage.this.mResumableOptionCode);
                FtMsrpMessage.this.updateStatus(ImConstants.Status.FAILED);
                FtMsrpMessage ftMsrpMessage3 = FtMsrpMessage.this;
                int nextFileTransferAutoResumeTimer = rcsStrategy.getNextFileTransferAutoResumeTimer(ftMsrpMessage3.mDirection, ftMsrpMessage3.mRetryCount);
                if (FtMsrpMessage.this.mResumableOptionCode != FtResumableOption.MANUALLY_AUTOMATICALLY_RESUMABLE.getId() || nextFileTransferAutoResumeTimer < 0) {
                    FtMsrpMessage ftMsrpMessage4 = FtMsrpMessage.this;
                    if (ftMsrpMessage4.mDirection != ImDirection.OUTGOING || ftMsrpMessage4.isAutoResumable()) {
                        FtMsrpMessage ftMsrpMessage5 = FtMsrpMessage.this;
                        if (ftMsrpMessage5.mDirection == ImDirection.INCOMING && ftMsrpMessage5.mTransferredBytes > 0) {
                            ftMsrpMessage5.moveCachedFileToApp();
                        }
                    } else {
                        FtMsrpMessage.this.deleteThumbnail();
                        FtMsrpMessage.this.deleteFile();
                    }
                } else {
                    FtMsrpStateMachine.this.logi(getName() + " start ft auto resume timer: " + nextFileTransferAutoResumeTimer);
                    if (nextFileTransferAutoResumeTimer < 10) {
                        FtMsrpStateMachine ftMsrpStateMachine2 = FtMsrpStateMachine.this;
                        ftMsrpStateMachine2.sendMessageDelayed(ftMsrpStateMachine2.obtainMessage(21), ((long) nextFileTransferAutoResumeTimer) * 1000);
                    } else {
                        PreciseAlarmManager.getInstance(FtMsrpMessage.this.getContext()).sendMessageDelayed(CanceledState.class.getSimpleName(), FtMsrpStateMachine.this.obtainMessage(21), ((long) nextFileTransferAutoResumeTimer) * 1000);
                    }
                    FtMsrpMessage.this.mRetryCount++;
                }
                FtMsrpMessage.this.mSwapUriType = false;
                FtMsrpMessage ftMsrpMessage6 = FtMsrpMessage.this;
                ftMsrpMessage6.mListener.onTransferCanceled(ftMsrpMessage6);
                FtMsrpStateMachine ftMsrpStateMachine3 = FtMsrpStateMachine.this;
                FtMsrpMessage.this.mIsConferenceUriChanged = false;
                ftMsrpStateMachine3.removeMessages(23);
                FtMsrpMessage.this.invokeFtQueueCallBack();
                FtMsrpMessage.this.updateState();
                FtMsrpMessage.this.releaseWakeLock();
            }

            public boolean processMessage(Message message) {
                int i = message.what;
                boolean z = false;
                if (i == 1) {
                    FtMsrpStateMachine ftMsrpStateMachine = FtMsrpStateMachine.this;
                    if (message.arg1 == 1) {
                        z = true;
                    }
                    ftMsrpStateMachine.onAttachFile(z);
                } else if (i == 4) {
                    FtMsrpStateMachine ftMsrpStateMachine2 = FtMsrpStateMachine.this;
                    if (FtMsrpMessage.this.mTransferredBytes > 0) {
                        ftMsrpStateMachine2.logi("FT already canceled. Try to file copy to APP. msgId : " + FtMsrpMessage.this.mId);
                        FtMsrpMessage.this.moveCachedFileToApp();
                    }
                } else if (i == 8) {
                    FtMsrpMessage ftMsrpMessage = FtMsrpMessage.this;
                    ftMsrpMessage.mCancelReason = (CancelReason) message.obj;
                    ftMsrpMessage.mListener.onTransferCanceled(ftMsrpMessage);
                    FtMsrpMessage.this.invokeFtQueueCallBack();
                } else if (i == 10) {
                    FtMsrpStateMachine.this.removeMessages(21);
                    FtIncomingSessionEvent ftIncomingSessionEvent = (FtIncomingSessionEvent) message.obj;
                    if (!FtMsrpMessage.this.isOutgoing() && !FtMsrpMessage.this.getIsSlmSvcMsg() && ftIncomingSessionEvent.mIsSlmSvcMsg) {
                        Log.i(FtMsrpMessage.this.LOG_TAG, "updateFtMsrpMessageInfo: service has been changed to SLM by sender.");
                        FtMsrpMessage.this.updateFtMessageInfo(ftIncomingSessionEvent);
                        FtMsrpStateMachine.this.onFileTransferInviteReceived(false);
                    } else if (!FtMsrpMessage.this.isOutgoing() || ftIncomingSessionEvent.mIsSlmSvcMsg || !FtMsrpMessage.this.getIsSlmSvcMsg()) {
                        FtMsrpStateMachine ftMsrpStateMachine3 = FtMsrpStateMachine.this;
                        FtMsrpMessage.this.mRawHandle = ftIncomingSessionEvent.mRawHandle;
                        ftMsrpStateMachine3.onFileTransferInviteReceived(true);
                    } else {
                        FtMsrpMessage.this.sendRejectFtSession(FtRejectReason.DECLINE, (FtIncomingSessionEvent) message.obj);
                        FtMsrpMessage.this.invokeFtQueueCallBack();
                    }
                } else if (i != 16) {
                    switch (i) {
                        case 19:
                            FtMsrpStateMachine ftMsrpStateMachine4 = FtMsrpStateMachine.this;
                            FtMsrpMessage.this.mThumbnailPath = (String) ((AsyncResult) message.obj).result;
                            ftMsrpStateMachine4.onCreateThumbnail();
                            break;
                        case 20:
                            FtMsrpStateMachine.this.onHandleFileResizeResponse((FileResizeResponse) message.obj);
                            break;
                        case 21:
                            if (FtMsrpMessage.this.mListener.onRequestRegistrationType() == null) {
                                FtMsrpStateMachine.this.logi("unregistered, schedule auto resume");
                                IMnoStrategy rcsStrategy = FtMsrpMessage.this.getRcsStrategy();
                                FtMsrpMessage ftMsrpMessage2 = FtMsrpMessage.this;
                                int nextFileTransferAutoResumeTimer = rcsStrategy.getNextFileTransferAutoResumeTimer(ftMsrpMessage2.mDirection, ftMsrpMessage2.mRetryCount);
                                if (FtMsrpMessage.this.mResumableOptionCode == FtResumableOption.MANUALLY_AUTOMATICALLY_RESUMABLE.getId() && nextFileTransferAutoResumeTimer >= 0) {
                                    FtMsrpStateMachine.this.logi(getName() + " start ft auto resume timer: " + nextFileTransferAutoResumeTimer);
                                    if (nextFileTransferAutoResumeTimer < 10) {
                                        FtMsrpStateMachine ftMsrpStateMachine5 = FtMsrpStateMachine.this;
                                        ftMsrpStateMachine5.sendMessageDelayed(ftMsrpStateMachine5.obtainMessage(21), ((long) nextFileTransferAutoResumeTimer) * 1000);
                                    } else {
                                        PreciseAlarmManager.getInstance(FtMsrpMessage.this.getContext()).sendMessageDelayed(CanceledState.class.getSimpleName(), FtMsrpStateMachine.this.obtainMessage(21), ((long) nextFileTransferAutoResumeTimer) * 1000);
                                    }
                                    FtMsrpMessage.this.mRetryCount++;
                                    break;
                                }
                            } else {
                                FtMsrpStateMachine.this.removeMessages(21);
                                PreciseAlarmManager.getInstance(FtMsrpMessage.this.getContext()).removeMessage(FtMsrpStateMachine.this.obtainMessage(21));
                                FtMsrpMessage ftMsrpMessage3 = FtMsrpMessage.this;
                                ftMsrpMessage3.mListener.onAutoResumeTransfer(ftMsrpMessage3);
                                break;
                            }
                        default:
                            return false;
                    }
                } else {
                    FtMsrpStateMachine.this.onAttachSlmFile();
                }
                return true;
            }
        }
    }

    /* renamed from: com.sec.internal.ims.servicemodules.im.FtMsrpMessage$1  reason: invalid class name */
    static /* synthetic */ class AnonymousClass1 {
        static final /* synthetic */ int[] $SwitchMap$com$sec$internal$constants$ims$servicemodules$im$event$FtTransferProgressEvent$State;
        static final /* synthetic */ int[] $SwitchMap$com$sec$internal$ims$servicemodules$im$strategy$IMnoStrategy$StatusCode;

        /* JADX WARNING: Can't wrap try/catch for region: R(21:0|(2:1|2)|3|(2:5|6)|7|9|10|11|(2:13|14)|15|17|18|19|20|21|22|23|24|25|26|(3:27|28|30)) */
        /* JADX WARNING: Can't wrap try/catch for region: R(25:0|1|2|3|(2:5|6)|7|9|10|11|13|14|15|17|18|19|20|21|22|23|24|25|26|27|28|30) */
        /* JADX WARNING: Can't wrap try/catch for region: R(26:0|1|2|3|5|6|7|9|10|11|13|14|15|17|18|19|20|21|22|23|24|25|26|27|28|30) */
        /* JADX WARNING: Failed to process nested try/catch */
        /* JADX WARNING: Missing exception handler attribute for start block: B:19:0x0044 */
        /* JADX WARNING: Missing exception handler attribute for start block: B:21:0x004e */
        /* JADX WARNING: Missing exception handler attribute for start block: B:23:0x0058 */
        /* JADX WARNING: Missing exception handler attribute for start block: B:25:0x0062 */
        /* JADX WARNING: Missing exception handler attribute for start block: B:27:0x006d */
        static {
            /*
                com.sec.internal.constants.ims.servicemodules.im.event.FtTransferProgressEvent$State[] r0 = com.sec.internal.constants.ims.servicemodules.im.event.FtTransferProgressEvent.State.values()
                int r0 = r0.length
                int[] r0 = new int[r0]
                $SwitchMap$com$sec$internal$constants$ims$servicemodules$im$event$FtTransferProgressEvent$State = r0
                r1 = 1
                com.sec.internal.constants.ims.servicemodules.im.event.FtTransferProgressEvent$State r2 = com.sec.internal.constants.ims.servicemodules.im.event.FtTransferProgressEvent.State.TRANSFERRING     // Catch:{ NoSuchFieldError -> 0x0012 }
                int r2 = r2.ordinal()     // Catch:{ NoSuchFieldError -> 0x0012 }
                r0[r2] = r1     // Catch:{ NoSuchFieldError -> 0x0012 }
            L_0x0012:
                r0 = 2
                int[] r2 = $SwitchMap$com$sec$internal$constants$ims$servicemodules$im$event$FtTransferProgressEvent$State     // Catch:{ NoSuchFieldError -> 0x001d }
                com.sec.internal.constants.ims.servicemodules.im.event.FtTransferProgressEvent$State r3 = com.sec.internal.constants.ims.servicemodules.im.event.FtTransferProgressEvent.State.CANCELED     // Catch:{ NoSuchFieldError -> 0x001d }
                int r3 = r3.ordinal()     // Catch:{ NoSuchFieldError -> 0x001d }
                r2[r3] = r0     // Catch:{ NoSuchFieldError -> 0x001d }
            L_0x001d:
                r2 = 3
                int[] r3 = $SwitchMap$com$sec$internal$constants$ims$servicemodules$im$event$FtTransferProgressEvent$State     // Catch:{ NoSuchFieldError -> 0x0028 }
                com.sec.internal.constants.ims.servicemodules.im.event.FtTransferProgressEvent$State r4 = com.sec.internal.constants.ims.servicemodules.im.event.FtTransferProgressEvent.State.COMPLETED     // Catch:{ NoSuchFieldError -> 0x0028 }
                int r4 = r4.ordinal()     // Catch:{ NoSuchFieldError -> 0x0028 }
                r3[r4] = r2     // Catch:{ NoSuchFieldError -> 0x0028 }
            L_0x0028:
                r3 = 4
                int[] r4 = $SwitchMap$com$sec$internal$constants$ims$servicemodules$im$event$FtTransferProgressEvent$State     // Catch:{ NoSuchFieldError -> 0x0033 }
                com.sec.internal.constants.ims.servicemodules.im.event.FtTransferProgressEvent$State r5 = com.sec.internal.constants.ims.servicemodules.im.event.FtTransferProgressEvent.State.INTERRUPTED     // Catch:{ NoSuchFieldError -> 0x0033 }
                int r5 = r5.ordinal()     // Catch:{ NoSuchFieldError -> 0x0033 }
                r4[r5] = r3     // Catch:{ NoSuchFieldError -> 0x0033 }
            L_0x0033:
                com.sec.internal.ims.servicemodules.im.strategy.IMnoStrategy$StatusCode[] r4 = com.sec.internal.ims.servicemodules.im.strategy.IMnoStrategy.StatusCode.values()
                int r4 = r4.length
                int[] r4 = new int[r4]
                $SwitchMap$com$sec$internal$ims$servicemodules$im$strategy$IMnoStrategy$StatusCode = r4
                com.sec.internal.ims.servicemodules.im.strategy.IMnoStrategy$StatusCode r5 = com.sec.internal.ims.servicemodules.im.strategy.IMnoStrategy.StatusCode.RETRY_IMMEDIATE     // Catch:{ NoSuchFieldError -> 0x0044 }
                int r5 = r5.ordinal()     // Catch:{ NoSuchFieldError -> 0x0044 }
                r4[r5] = r1     // Catch:{ NoSuchFieldError -> 0x0044 }
            L_0x0044:
                int[] r1 = $SwitchMap$com$sec$internal$ims$servicemodules$im$strategy$IMnoStrategy$StatusCode     // Catch:{ NoSuchFieldError -> 0x004e }
                com.sec.internal.ims.servicemodules.im.strategy.IMnoStrategy$StatusCode r4 = com.sec.internal.ims.servicemodules.im.strategy.IMnoStrategy.StatusCode.RETRY_AFTER     // Catch:{ NoSuchFieldError -> 0x004e }
                int r4 = r4.ordinal()     // Catch:{ NoSuchFieldError -> 0x004e }
                r1[r4] = r0     // Catch:{ NoSuchFieldError -> 0x004e }
            L_0x004e:
                int[] r0 = $SwitchMap$com$sec$internal$ims$servicemodules$im$strategy$IMnoStrategy$StatusCode     // Catch:{ NoSuchFieldError -> 0x0058 }
                com.sec.internal.ims.servicemodules.im.strategy.IMnoStrategy$StatusCode r1 = com.sec.internal.ims.servicemodules.im.strategy.IMnoStrategy.StatusCode.RETRY_AFTER_SESSION     // Catch:{ NoSuchFieldError -> 0x0058 }
                int r1 = r1.ordinal()     // Catch:{ NoSuchFieldError -> 0x0058 }
                r0[r1] = r2     // Catch:{ NoSuchFieldError -> 0x0058 }
            L_0x0058:
                int[] r0 = $SwitchMap$com$sec$internal$ims$servicemodules$im$strategy$IMnoStrategy$StatusCode     // Catch:{ NoSuchFieldError -> 0x0062 }
                com.sec.internal.ims.servicemodules.im.strategy.IMnoStrategy$StatusCode r1 = com.sec.internal.ims.servicemodules.im.strategy.IMnoStrategy.StatusCode.FALLBACK_TO_SLM     // Catch:{ NoSuchFieldError -> 0x0062 }
                int r1 = r1.ordinal()     // Catch:{ NoSuchFieldError -> 0x0062 }
                r0[r1] = r3     // Catch:{ NoSuchFieldError -> 0x0062 }
            L_0x0062:
                int[] r0 = $SwitchMap$com$sec$internal$ims$servicemodules$im$strategy$IMnoStrategy$StatusCode     // Catch:{ NoSuchFieldError -> 0x006d }
                com.sec.internal.ims.servicemodules.im.strategy.IMnoStrategy$StatusCode r1 = com.sec.internal.ims.servicemodules.im.strategy.IMnoStrategy.StatusCode.FALLBACK_TO_LEGACY     // Catch:{ NoSuchFieldError -> 0x006d }
                int r1 = r1.ordinal()     // Catch:{ NoSuchFieldError -> 0x006d }
                r2 = 5
                r0[r1] = r2     // Catch:{ NoSuchFieldError -> 0x006d }
            L_0x006d:
                int[] r0 = $SwitchMap$com$sec$internal$ims$servicemodules$im$strategy$IMnoStrategy$StatusCode     // Catch:{ NoSuchFieldError -> 0x0078 }
                com.sec.internal.ims.servicemodules.im.strategy.IMnoStrategy$StatusCode r1 = com.sec.internal.ims.servicemodules.im.strategy.IMnoStrategy.StatusCode.DISPLAY_ERROR     // Catch:{ NoSuchFieldError -> 0x0078 }
                int r1 = r1.ordinal()     // Catch:{ NoSuchFieldError -> 0x0078 }
                r2 = 6
                r0[r1] = r2     // Catch:{ NoSuchFieldError -> 0x0078 }
            L_0x0078:
                return
            */
            throw new UnsupportedOperationException("Method not decompiled: com.sec.internal.ims.servicemodules.im.FtMsrpMessage.AnonymousClass1.<clinit>():void");
        }
    }

    public void acceptTransfer(Uri uri) {
        this.mContentUri = uri;
        if (this.mDirection == ImDirection.OUTGOING || this.mTransferredBytes > 0) {
            this.mFilePath = FileUtils.copyFileToCacheFromUri(getContext(), getFileName(), this.mContentUri);
        }
        FtMessage.FtStateMachine ftStateMachine = this.mStateMachine;
        ftStateMachine.sendMessage(ftStateMachine.obtainMessage(4));
    }
}
