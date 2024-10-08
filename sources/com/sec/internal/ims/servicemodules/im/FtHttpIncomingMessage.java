package com.sec.internal.ims.servicemodules.im;

import android.content.Context;
import android.database.Cursor;
import android.net.Network;
import android.net.Uri;
import android.os.Environment;
import android.os.Looper;
import android.os.Message;
import android.text.TextUtils;
import android.util.Log;
import com.sec.ims.util.ImsUri;
import com.sec.internal.constants.ims.ImsConstants;
import com.sec.internal.constants.ims.cmstore.CloudMessageProviderContract;
import com.sec.internal.constants.ims.config.ConfigConstants;
import com.sec.internal.constants.ims.servicemodules.im.FtHttpFileInfo;
import com.sec.internal.constants.ims.servicemodules.im.ImCacheAction;
import com.sec.internal.constants.ims.servicemodules.im.ImConstants;
import com.sec.internal.constants.ims.servicemodules.im.NotificationStatus;
import com.sec.internal.constants.ims.servicemodules.im.reason.CancelReason;
import com.sec.internal.constants.ims.servicemodules.im.reason.FtRejectReason;
import com.sec.internal.helper.BlockedNumberUtil;
import com.sec.internal.helper.FilePathGenerator;
import com.sec.internal.helper.IState;
import com.sec.internal.helper.PreciseAlarmManager;
import com.sec.internal.helper.SimUtil;
import com.sec.internal.helper.State;
import com.sec.internal.helper.translate.MappingTranslator;
import com.sec.internal.ims.config.util.AKAEapAuthHelper;
import com.sec.internal.ims.rcs.util.RcsUtils;
import com.sec.internal.ims.servicemodules.im.DownloadFileTask;
import com.sec.internal.ims.servicemodules.im.FtMessage;
import com.sec.internal.ims.servicemodules.im.strategy.IMnoStrategy;
import com.sec.internal.ims.servicemodules.im.util.AsyncFileTask;
import com.sec.internal.ims.servicemodules.im.util.FtHttpXmlParser;
import com.sec.internal.ims.settings.RcsPolicySettings;
import com.sec.internal.imscr.LogClass;
import com.sec.internal.log.IMSLog;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;
import java.util.regex.Pattern;

public class FtHttpIncomingMessage extends FtMessage {
    private static final int EVENT_DOWNLOAD_CANCELED = 103;
    private static final int EVENT_DOWNLOAD_COMPLETED = 102;
    private static final int EVENT_DOWNLOAD_PROGRESS = 101;
    private static final int EVENT_DOWNLOAD_THUMBNAIL_CANCELED = 106;
    private static final int EVENT_DOWNLOAD_THUMBNAIL_COMPLETED = 104;
    private static final int EVENT_DOWNLOAD_THUMBNAIL_FAILED = 105;
    private static final int EVENT_RETRY_DOWNLOAD = 107;
    private static final int EVENT_RETRY_THUMBNAIL_DOWNLOAD = 108;
    private static final Pattern GSMA_FT_HTTP_URL_PATTERN = Pattern.compile("https://ftcontentserver\\.rcs\\.mnc\\d{3}\\.mcc\\d{3}\\.pub\\.3gppnetwork\\.org");
    /* access modifiers changed from: private */
    public static final String LOG_TAG = FtHttpIncomingMessage.class.getSimpleName();
    /* access modifiers changed from: private */
    public URL mDataUrl;

    public int getTransferMech() {
        return 1;
    }

    protected FtHttpIncomingMessage(Builder<?> builder) {
        super(builder);
        String str = LOG_TAG;
        Log.i(str, "data url=" + IMSLog.checker(builder.mDataUrl));
        try {
            if (builder.mDataUrl != null) {
                this.mDataUrl = new URL(builder.mDataUrl);
            }
        } catch (MalformedURLException e) {
            e.printStackTrace();
            throw new IllegalArgumentException("Malformed data url");
        }
    }

    public static Builder<?> builder() {
        return new Builder2();
    }

    public void receiveTransfer() {
        FtMessage.FtStateMachine ftStateMachine = this.mStateMachine;
        ftStateMachine.sendMessage(ftStateMachine.obtainMessage(10));
    }

    public boolean isAutoResumable() {
        return !getRcsStrategy().boolSetting(RcsPolicySettings.RcsPolicy.CANCEL_FOR_DEREGI_PROMPTLY);
    }

    public String getDataUrl() {
        URL url = this.mDataUrl;
        if (url != null) {
            return url.toString();
        }
        return null;
    }

    /* access modifiers changed from: private */
    public boolean isFtHttpUrlTrusted(String str) {
        if (GSMA_FT_HTTP_URL_PATTERN.matcher(str).find()) {
            return true;
        }
        for (String startsWith : getRcsStrategy().stringArraySetting(RcsPolicySettings.RcsPolicy.FTHTTP_NON_STANDARD_URLS)) {
            if (str.startsWith(startsWith)) {
                return true;
            }
        }
        return false;
    }

    /* access modifiers changed from: protected */
    public FtMessage.FtStateMachine createFtStateMachine(String str, Looper looper) {
        return new FtHttpStateMachine("FtHttpIncomingMessage#" + str, looper);
    }

    public Map<String, String> getParamsforDl(String str) {
        HashMap hashMap = new HashMap();
        if (!TextUtils.isEmpty(this.mConfig.getFtHttpDLUrl())) {
            hashMap.put(ImsConstants.FtDlParams.FT_DL_URL, str);
            if (!TextUtils.isEmpty(this.mImdnId)) {
                hashMap.put("id", this.mImdnId);
            }
            if (!TextUtils.isEmpty(this.mConversationId)) {
                hashMap.put(ImsConstants.FtDlParams.FT_DL_CONV_ID, this.mConversationId);
            }
            ImsUri imsUri = this.mRemoteUri;
            if (imsUri != null) {
                hashMap.put(ImsConstants.FtDlParams.FT_DL_OTHER_PARTY, imsUri.toString());
            }
        }
        if (this.mMnoStrategy.boolSetting(RcsPolicySettings.RcsPolicy.IS_EAP_SUPPORTED)) {
            hashMap.put("EAP_ID", AKAEapAuthHelper.composeRootNai(this.mConfig.getPhoneId()));
        }
        return hashMap;
    }

    public static abstract class Builder<T extends Builder<T>> extends FtMessage.Builder<T> {
        /* access modifiers changed from: private */
        public String mDataUrl;

        public T dataUrl(String str) {
            this.mDataUrl = str;
            return (Builder) self();
        }

        public FtHttpIncomingMessage build() {
            return new FtHttpIncomingMessage(this);
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
        public final AcceptingState mAcceptingState;
        /* access modifiers changed from: private */
        public final CanceledState mCanceledState;
        /* access modifiers changed from: private */
        public final CompletedState mCompletedState;
        protected final MappingTranslator<Integer, State> mDbStateTranslator;
        private final DefaultState mDefaultState = new DefaultState();
        /* access modifiers changed from: private */
        public final InProgressState mInProgressState;
        private final InitialState mInitialState;
        protected final MappingTranslator<IState, Integer> mStateTranslator;

        protected FtHttpStateMachine(String str, Looper looper) {
            super(str, looper);
            InitialState initialState = new InitialState();
            this.mInitialState = initialState;
            AcceptingState acceptingState = new AcceptingState();
            this.mAcceptingState = acceptingState;
            InProgressState inProgressState = new InProgressState();
            this.mInProgressState = inProgressState;
            CanceledState canceledState = new CanceledState();
            this.mCanceledState = canceledState;
            CompletedState completedState = new CompletedState();
            this.mCompletedState = completedState;
            this.mStateTranslator = new MappingTranslator.Builder().map(initialState, 0).map(acceptingState, 1).map(inProgressState, 2).map(canceledState, 4).map(completedState, 3).buildTranslator();
            this.mDbStateTranslator = new MappingTranslator.Builder().map(0, initialState).map(1, acceptingState).map(2, inProgressState).map(4, canceledState).map(3, completedState).buildTranslator();
        }

        /* access modifiers changed from: protected */
        public void initState(State state) {
            addState(this.mDefaultState);
            addState(this.mInitialState, this.mDefaultState);
            addState(this.mAcceptingState, this.mDefaultState);
            addState(this.mInProgressState, this.mDefaultState);
            addState(this.mCanceledState, this.mDefaultState);
            addState(this.mCompletedState, this.mDefaultState);
            String r0 = FtHttpIncomingMessage.LOG_TAG;
            Log.i(r0, "setting current state as " + state.getName() + " for messageId : " + FtHttpIncomingMessage.this.mId);
            setInitialState(state);
            start();
        }

        /* access modifiers changed from: protected */
        public State getState(Integer num) {
            return this.mDbStateTranslator.translate(num);
        }

        /* access modifiers changed from: protected */
        public int getStateId() {
            Integer translate = this.mStateTranslator.translate(getCurrentState());
            if (translate == null) {
                return 0;
            }
            return translate.intValue();
        }

        private final class DefaultState extends State {
            private DefaultState() {
            }

            public boolean processMessage(Message message) {
                if (message.what != 13) {
                    if (FtHttpStateMachine.this.getCurrentState() != null) {
                        String r0 = FtHttpIncomingMessage.LOG_TAG;
                        Log.e(r0, "Unexpected event, current state is " + FtHttpStateMachine.this.getCurrentState().getName() + " event: " + message.what);
                    }
                    return false;
                }
                FtHttpIncomingMessage.this.onSendDeliveredNotificationDone();
                return true;
            }
        }

        private final class InitialState extends State {
            DownloadFileTask thumbnailDownloadTask;

            private InitialState() {
            }

            public void enter() {
                String r0 = FtHttpIncomingMessage.LOG_TAG;
                Log.i(r0, getName() + " enter msgId : " + FtHttpIncomingMessage.this.mId);
                FtHttpIncomingMessage.this.updateStatus(ImConstants.Status.UNREAD);
            }

            public boolean processMessage(Message message) {
                IState iState;
                int i = message.what;
                if (i == 4) {
                    FtHttpStateMachine ftHttpStateMachine = FtHttpStateMachine.this;
                    ftHttpStateMachine.transitionTo(ftHttpStateMachine.mInProgressState);
                    return true;
                } else if (i == 8) {
                    FtHttpStateMachine ftHttpStateMachine2 = FtHttpStateMachine.this;
                    FtHttpIncomingMessage.this.mCancelReason = CancelReason.CANCELED_BY_USER;
                    ftHttpStateMachine2.transitionTo(ftHttpStateMachine2.mCanceledState);
                    return true;
                } else if (i == 10) {
                    FtHttpIncomingMessage ftHttpIncomingMessage = FtHttpIncomingMessage.this;
                    ftHttpIncomingMessage.mListener.onNotifyCloudMsgFtEvent(ftHttpIncomingMessage);
                    if (FtHttpIncomingMessage.this.needToAcquireNetworkForFT()) {
                        FtHttpIncomingMessage ftHttpIncomingMessage2 = FtHttpIncomingMessage.this;
                        ftHttpIncomingMessage2.acquireNetworkForFT(ftHttpIncomingMessage2.getRcsStrategy().intSetting(RcsPolicySettings.RcsPolicy.FT_NET_CAPABILITY));
                        return true;
                    }
                    handleReceiverTransferEvent();
                    return true;
                } else if (i == 108) {
                    String r14 = FtHttpIncomingMessage.LOG_TAG;
                    Log.i(r14, "EVENT_RETRY_THUMBNAIL_DOWNLOAD mId=" + FtHttpIncomingMessage.this.mId + ", Retry count=" + FtHttpIncomingMessage.this.getRetryCount());
                    FtHttpStateMachine.this.removeMessages(108);
                    if (!FtHttpIncomingMessage.this.checkAvailableRetry() || FtHttpIncomingMessage.this.getRetryCount() >= 3) {
                        FtHttpIncomingMessage.this.setRetryCount(0);
                        FtHttpStateMachine ftHttpStateMachine3 = FtHttpStateMachine.this;
                        FtHttpIncomingMessage.this.mThumbnailPath = null;
                        ftHttpStateMachine3.sendMessage(104);
                        return true;
                    }
                    FtHttpIncomingMessage ftHttpIncomingMessage3 = FtHttpIncomingMessage.this;
                    ftHttpIncomingMessage3.setRetryCount(ftHttpIncomingMessage3.getRetryCount() + 1);
                    tryThumbnailDownload();
                    return true;
                } else if (i == 50) {
                    FtHttpStateMachine.this.removeMessages(51);
                    handleReceiverTransferEvent();
                    return true;
                } else if (i != 51) {
                    switch (i) {
                        case 104:
                            long ftWarnSize = FtHttpIncomingMessage.this.mConfig.getFtWarnSize();
                            long maxSizeFileTrIncoming = FtHttpIncomingMessage.this.mConfig.getMaxSizeFileTrIncoming();
                            FtHttpIncomingMessage.this.mIsAutoDownload = false;
                            String r142 = FtHttpIncomingMessage.LOG_TAG;
                            Log.i(r142, "EVENT_DOWNLOAD_THUMBNAIL_COMPLETED: maxSizeFileTrIncoming(" + maxSizeFileTrIncoming + "), warnSizeFileTr(" + ftWarnSize + ")");
                            if (!FtHttpIncomingMessage.this.getRcsStrategy().boolSetting(RcsPolicySettings.RcsPolicy.BLOCK_MSG) || !BlockedNumberUtil.isBlockedNumber(FtHttpIncomingMessage.this.getContext(), FtHttpIncomingMessage.this.mRemoteUri.getMsisdn())) {
                                FtHttpStateMachine ftHttpStateMachine4 = FtHttpStateMachine.this;
                                FtHttpIncomingMessage ftHttpIncomingMessage4 = FtHttpIncomingMessage.this;
                                if (ftHttpIncomingMessage4.mLastNotificationType == NotificationStatus.CANCELED) {
                                    ftHttpIncomingMessage4.mCancelReason = CancelReason.CANCELED_NOTIFICATION;
                                    iState = ftHttpStateMachine4.mCanceledState;
                                } else if (maxSizeFileTrIncoming == 0 || ftHttpIncomingMessage4.mFileSize <= maxSizeFileTrIncoming) {
                                    String path = Environment.getDataDirectory().getPath();
                                    FtHttpIncomingMessage ftHttpIncomingMessage5 = FtHttpIncomingMessage.this;
                                    if (FtMessage.checkAvailableStorage(path, ftHttpIncomingMessage5.mFileSize - ftHttpIncomingMessage5.mTransferredBytes) || !FtHttpIncomingMessage.this.mConfig.getFtCancelMemoryFull()) {
                                        if ((FtHttpIncomingMessage.this.mConfig.isFtAutAccept() || ((FtHttpIncomingMessage.this.getRcsStrategy().boolSetting(RcsPolicySettings.RcsPolicy.FTHTTP_FORCE_AUTO_ACCEPT_ON_WIFI) && FtHttpIncomingMessage.this.isWifiConnected()) || (FtHttpIncomingMessage.this.getRcsStrategy().boolSetting(RcsPolicySettings.RcsPolicy.AUTO_ACCEPT_RRAM) && "audio/amr".equals(FtHttpIncomingMessage.this.mContentType) && FtHttpIncomingMessage.this.mPlayingLength > 0))) && !FtHttpIncomingMessage.this.getRcsStrategy().isBMode(false)) {
                                            IMnoStrategy rcsStrategy = FtHttpIncomingMessage.this.getRcsStrategy();
                                            FtHttpIncomingMessage ftHttpIncomingMessage6 = FtHttpIncomingMessage.this;
                                            if (!rcsStrategy.isWarnSizeFile(ftHttpIncomingMessage6.mNetwork, ftHttpIncomingMessage6.mFileSize, ftWarnSize, ftHttpIncomingMessage6.isWifiConnected()) && !isAutodownloadBlocked()) {
                                                Log.e(FtHttpIncomingMessage.LOG_TAG, "Enable auto download");
                                                FtHttpIncomingMessage.this.mIsAutoDownload = true;
                                            }
                                        }
                                        iState = FtHttpStateMachine.this.mAcceptingState;
                                    } else {
                                        Log.e(FtHttpIncomingMessage.LOG_TAG, "Auto cancel file transfer, disk space not available");
                                        FtHttpStateMachine ftHttpStateMachine5 = FtHttpStateMachine.this;
                                        FtHttpIncomingMessage ftHttpIncomingMessage7 = FtHttpIncomingMessage.this;
                                        ftHttpIncomingMessage7.mRejectReason = FtRejectReason.DECLINE;
                                        ftHttpIncomingMessage7.mCancelReason = CancelReason.LOW_MEMORY;
                                        iState = ftHttpStateMachine5.mCanceledState;
                                    }
                                } else {
                                    Log.e(FtHttpIncomingMessage.LOG_TAG, "Auto cancel file transfer, max size exceeded");
                                    FtHttpStateMachine ftHttpStateMachine6 = FtHttpStateMachine.this;
                                    FtHttpIncomingMessage ftHttpIncomingMessage8 = FtHttpIncomingMessage.this;
                                    ftHttpIncomingMessage8.mRejectReason = FtRejectReason.FORBIDDEN_MAX_SIZE_EXCEEDED;
                                    ftHttpIncomingMessage8.mCancelReason = CancelReason.CANCELED_BY_SYSTEM;
                                    iState = ftHttpStateMachine6.mCanceledState;
                                }
                            } else {
                                Log.i(FtHttpIncomingMessage.LOG_TAG, "from blocked number.. go to CanceledState.");
                                FtHttpStateMachine ftHttpStateMachine7 = FtHttpStateMachine.this;
                                FtHttpIncomingMessage.this.mCancelReason = CancelReason.CANCELED_BY_USER;
                                iState = ftHttpStateMachine7.mCanceledState;
                            }
                            FtHttpIncomingMessage ftHttpIncomingMessage9 = FtHttpIncomingMessage.this;
                            ftHttpIncomingMessage9.mListener.onTransferReceived(ftHttpIncomingMessage9);
                            FtHttpIncomingMessage ftHttpIncomingMessage10 = FtHttpIncomingMessage.this;
                            NotificationStatus notificationStatus = ftHttpIncomingMessage10.mLastNotificationType;
                            if (notificationStatus == NotificationStatus.CANCELED) {
                                ftHttpIncomingMessage10.mListener.onImdnNotificationReceived(ftHttpIncomingMessage10, ftHttpIncomingMessage10.mRemoteUri, notificationStatus, ftHttpIncomingMessage10.mIsGroupChat);
                            }
                            FtHttpStateMachine.this.transitionTo(iState);
                            return true;
                        case 105:
                            int ftHttpRetryInterval = FtHttpIncomingMessage.this.getRcsStrategy().getFtHttpRetryInterval(message.arg1, FtHttpIncomingMessage.this.getRetryCount());
                            if (ftHttpRetryInterval >= 0) {
                                String r1 = FtHttpIncomingMessage.LOG_TAG;
                                Log.e(r1, "EVENT_DOWNLOAD_THUMBNAIL_FAILED: " + FtHttpIncomingMessage.this.mId + " retry download after " + ftHttpRetryInterval + " secs");
                                FtHttpStateMachine ftHttpStateMachine8 = FtHttpStateMachine.this;
                                ftHttpStateMachine8.sendMessageDelayed(ftHttpStateMachine8.obtainMessage(108, 0, message.arg2), ((long) ftHttpRetryInterval) * 1000);
                                return true;
                            }
                            FtHttpStateMachine ftHttpStateMachine9 = FtHttpStateMachine.this;
                            FtHttpIncomingMessage.this.mThumbnailPath = null;
                            ftHttpStateMachine9.sendMessage(104);
                            return true;
                        case 106:
                            DownloadFileTask downloadFileTask = this.thumbnailDownloadTask;
                            if (downloadFileTask != null) {
                                downloadFileTask.cancel(true);
                                this.thumbnailDownloadTask = null;
                            }
                            FtHttpIncomingMessage ftHttpIncomingMessage11 = FtHttpIncomingMessage.this;
                            ftHttpIncomingMessage11.mThumbnailPath = null;
                            ftHttpIncomingMessage11.mIsAutoDownload = false;
                            ftHttpIncomingMessage11.mListener.onTransferReceived(ftHttpIncomingMessage11);
                            FtHttpStateMachine ftHttpStateMachine10 = FtHttpStateMachine.this;
                            FtHttpIncomingMessage ftHttpIncomingMessage12 = FtHttpIncomingMessage.this;
                            NotificationStatus notificationStatus2 = ftHttpIncomingMessage12.mLastNotificationType;
                            if (notificationStatus2 == NotificationStatus.CANCELED) {
                                ftHttpIncomingMessage12.mListener.onImdnNotificationReceived(ftHttpIncomingMessage12, ftHttpIncomingMessage12.mRemoteUri, notificationStatus2, ftHttpIncomingMessage12.mIsGroupChat);
                                FtHttpStateMachine ftHttpStateMachine11 = FtHttpStateMachine.this;
                                FtHttpIncomingMessage.this.mCancelReason = CancelReason.CANCELED_NOTIFICATION;
                                ftHttpStateMachine11.transitionTo(ftHttpStateMachine11.mCanceledState);
                                return true;
                            }
                            ftHttpStateMachine10.transitionTo(ftHttpStateMachine10.mAcceptingState);
                            return true;
                        default:
                            return false;
                    }
                } else {
                    FtHttpStateMachine ftHttpStateMachine12 = FtHttpStateMachine.this;
                    FtHttpIncomingMessage.this.mCancelReason = CancelReason.ERROR;
                    ftHttpStateMachine12.transitionTo(ftHttpStateMachine12.mCanceledState);
                    return true;
                }
            }

            private boolean isAutodownloadBlocked() {
                Cursor query;
                try {
                    query = FtHttpIncomingMessage.this.getContext().getContentResolver().query(ImsConstants.Uris.MMS_PREFERENCE_PROVIDER_DATASAVER_URI, (String[]) null, (String) null, (String[]) null, (String) null);
                    String str = ConfigConstants.VALUE.INFO_COMPLETED;
                    if (query != null) {
                        if (query.moveToNext()) {
                            str = query.getString(query.getColumnIndexOrThrow("pref_value"));
                        }
                    }
                    String r3 = FtHttpIncomingMessage.LOG_TAG;
                    Log.i(r3, " enable datasaver : " + str);
                    if (CloudMessageProviderContract.JsonData.TRUE.equals(str)) {
                        if (query != null) {
                            query.close();
                        }
                        return true;
                    }
                    if (query != null) {
                        query.close();
                    }
                    FtHttpIncomingMessage ftHttpIncomingMessage = FtHttpIncomingMessage.this;
                    if ((ftHttpIncomingMessage.mIsGroupChat && !ftHttpIncomingMessage.getRcsStrategy().boolSetting(RcsPolicySettings.RcsPolicy.BLOCK_FT_AUTO_DOWNLOAD_FOR_GC)) || !BlockedNumberUtil.isBlockedNumber(FtHttpIncomingMessage.this.getContext(), FtHttpIncomingMessage.this.mRemoteUri.getMsisdn())) {
                        return false;
                    }
                    Log.i(FtHttpIncomingMessage.LOG_TAG, "It is blocked number.");
                    return true;
                } catch (IllegalStateException unused) {
                    Log.e(FtHttpIncomingMessage.LOG_TAG, "isAutodownloadBlocked: IllegalStateException");
                } catch (Throwable th) {
                    th.addSuppressed(th);
                }
                throw th;
            }

            /* JADX WARNING: Code restructure failed: missing block: B:23:0x0189, code lost:
                r0 = move-exception;
             */
            /* JADX WARNING: Code restructure failed: missing block: B:24:0x018a, code lost:
                r0.printStackTrace();
                r0 = r5.this$1;
                r0.transitionTo(com.sec.internal.ims.servicemodules.im.FtHttpIncomingMessage.FtHttpStateMachine.m923$$Nest$fgetmCanceledState(r0));
             */
            /* JADX WARNING: Failed to process nested try/catch */
            /* JADX WARNING: Removed duplicated region for block: B:23:0x0189 A[ExcHandler: TranslationException | IOException | NullPointerException | XmlPullParserException (r0v2 'e' java.lang.Exception A[CUSTOM_DECLARE]), Splitter:B:1:0x002b] */
            /* Code decompiled incorrectly, please refer to instructions dump. */
            private void handleReceiverTransferEvent() {
                /*
                    r5 = this;
                    java.lang.String r0 = "."
                    java.lang.String r1 = com.sec.internal.ims.servicemodules.im.FtHttpIncomingMessage.LOG_TAG
                    java.lang.StringBuilder r2 = new java.lang.StringBuilder
                    r2.<init>()
                    java.lang.String r3 = "handleReceiverTransferEvent: "
                    r2.append(r3)
                    com.sec.internal.ims.servicemodules.im.FtHttpIncomingMessage$FtHttpStateMachine r3 = com.sec.internal.ims.servicemodules.im.FtHttpIncomingMessage.FtHttpStateMachine.this
                    com.sec.internal.ims.servicemodules.im.FtHttpIncomingMessage r3 = com.sec.internal.ims.servicemodules.im.FtHttpIncomingMessage.this
                    java.lang.String r3 = r3.mBody
                    r2.append(r3)
                    java.lang.String r2 = r2.toString()
                    com.sec.internal.log.IMSLog.s(r1, r2)
                    com.sec.internal.ims.servicemodules.im.FtHttpIncomingMessage$FtHttpStateMachine r1 = com.sec.internal.ims.servicemodules.im.FtHttpIncomingMessage.FtHttpStateMachine.this
                    com.sec.internal.ims.servicemodules.im.FtHttpIncomingMessage r1 = com.sec.internal.ims.servicemodules.im.FtHttpIncomingMessage.this
                    long r2 = java.lang.System.currentTimeMillis()
                    r1.updateDeliveredTimestamp(r2)
                    com.sec.internal.ims.servicemodules.im.FtHttpIncomingMessage$FtHttpStateMachine r1 = com.sec.internal.ims.servicemodules.im.FtHttpIncomingMessage.FtHttpStateMachine.this     // Catch:{ TranslationException | IOException | NullPointerException | XmlPullParserException -> 0x0189 }
                    com.sec.internal.ims.servicemodules.im.FtHttpIncomingMessage r1 = com.sec.internal.ims.servicemodules.im.FtHttpIncomingMessage.this     // Catch:{ TranslationException | IOException | NullPointerException | XmlPullParserException -> 0x0189 }
                    java.lang.String r1 = r1.mBody     // Catch:{ TranslationException | IOException | NullPointerException | XmlPullParserException -> 0x0189 }
                    com.sec.internal.constants.ims.servicemodules.im.FtHttpFileInfo r1 = com.sec.internal.ims.servicemodules.im.util.FtHttpXmlParser.parse(r1)     // Catch:{ TranslationException | IOException | NullPointerException | XmlPullParserException -> 0x0189 }
                    com.sec.internal.ims.servicemodules.im.FtHttpIncomingMessage$FtHttpStateMachine r2 = com.sec.internal.ims.servicemodules.im.FtHttpIncomingMessage.FtHttpStateMachine.this     // Catch:{ TranslationException | IOException | NullPointerException | XmlPullParserException -> 0x0189 }
                    com.sec.internal.ims.servicemodules.im.FtHttpIncomingMessage r2 = com.sec.internal.ims.servicemodules.im.FtHttpIncomingMessage.this     // Catch:{ TranslationException | IOException | NullPointerException | XmlPullParserException -> 0x0189 }
                    java.net.URL r3 = r1.getDataUrl()     // Catch:{ TranslationException | IOException | NullPointerException | XmlPullParserException -> 0x0189 }
                    r2.mDataUrl = r3     // Catch:{ TranslationException | IOException | NullPointerException | XmlPullParserException -> 0x0189 }
                    com.sec.internal.ims.servicemodules.im.FtHttpIncomingMessage$FtHttpStateMachine r2 = com.sec.internal.ims.servicemodules.im.FtHttpIncomingMessage.FtHttpStateMachine.this     // Catch:{ TranslationException | IOException | NullPointerException | XmlPullParserException -> 0x0189 }
                    com.sec.internal.ims.servicemodules.im.FtHttpIncomingMessage r2 = com.sec.internal.ims.servicemodules.im.FtHttpIncomingMessage.this     // Catch:{ TranslationException | IOException | NullPointerException | XmlPullParserException -> 0x0189 }
                    com.sec.internal.ims.servicemodules.im.strategy.IMnoStrategy r2 = r2.getRcsStrategy()     // Catch:{ TranslationException | IOException | NullPointerException | XmlPullParserException -> 0x0189 }
                    java.lang.String r3 = "fthttp_ignore_when_untrusted_url"
                    boolean r2 = r2.boolSetting(r3)     // Catch:{ TranslationException | IOException | NullPointerException | XmlPullParserException -> 0x0189 }
                    if (r2 == 0) goto L_0x00b6
                    boolean r2 = r1.isThumbnailExist()     // Catch:{ TranslationException | IOException | NullPointerException | XmlPullParserException -> 0x0189 }
                    com.sec.internal.ims.servicemodules.im.FtHttpIncomingMessage$FtHttpStateMachine r3 = com.sec.internal.ims.servicemodules.im.FtHttpIncomingMessage.FtHttpStateMachine.this     // Catch:{ TranslationException | IOException | NullPointerException | XmlPullParserException -> 0x0189 }
                    com.sec.internal.ims.servicemodules.im.FtHttpIncomingMessage r3 = com.sec.internal.ims.servicemodules.im.FtHttpIncomingMessage.this     // Catch:{ TranslationException | IOException | NullPointerException | XmlPullParserException -> 0x0189 }
                    java.net.URL r4 = r3.mDataUrl     // Catch:{ TranslationException | IOException | NullPointerException | XmlPullParserException -> 0x0189 }
                    java.lang.String r4 = r4.toString()     // Catch:{ TranslationException | IOException | NullPointerException | XmlPullParserException -> 0x0189 }
                    boolean r3 = r3.isFtHttpUrlTrusted(r4)     // Catch:{ TranslationException | IOException | NullPointerException | XmlPullParserException -> 0x0189 }
                    if (r3 == 0) goto L_0x007a
                    if (r2 == 0) goto L_0x00b6
                    com.sec.internal.ims.servicemodules.im.FtHttpIncomingMessage$FtHttpStateMachine r2 = com.sec.internal.ims.servicemodules.im.FtHttpIncomingMessage.FtHttpStateMachine.this     // Catch:{ TranslationException | IOException | NullPointerException | XmlPullParserException -> 0x0189 }
                    com.sec.internal.ims.servicemodules.im.FtHttpIncomingMessage r2 = com.sec.internal.ims.servicemodules.im.FtHttpIncomingMessage.this     // Catch:{ TranslationException | IOException | NullPointerException | XmlPullParserException -> 0x0189 }
                    java.net.URL r3 = r1.getThumbnailDataUrl()     // Catch:{ TranslationException | IOException | NullPointerException | XmlPullParserException -> 0x0189 }
                    java.lang.String r3 = r3.toString()     // Catch:{ TranslationException | IOException | NullPointerException | XmlPullParserException -> 0x0189 }
                    boolean r2 = r2.isFtHttpUrlTrusted(r3)     // Catch:{ TranslationException | IOException | NullPointerException | XmlPullParserException -> 0x0189 }
                    if (r2 != 0) goto L_0x00b6
                L_0x007a:
                    java.lang.String r0 = com.sec.internal.ims.servicemodules.im.FtHttpIncomingMessage.LOG_TAG     // Catch:{ TranslationException | IOException | NullPointerException | XmlPullParserException -> 0x0189 }
                    java.lang.StringBuilder r1 = new java.lang.StringBuilder     // Catch:{ TranslationException | IOException | NullPointerException | XmlPullParserException -> 0x0189 }
                    r1.<init>()     // Catch:{ TranslationException | IOException | NullPointerException | XmlPullParserException -> 0x0189 }
                    java.lang.String r2 = "FT["
                    r1.append(r2)     // Catch:{ TranslationException | IOException | NullPointerException | XmlPullParserException -> 0x0189 }
                    com.sec.internal.ims.servicemodules.im.FtHttpIncomingMessage$FtHttpStateMachine r2 = com.sec.internal.ims.servicemodules.im.FtHttpIncomingMessage.FtHttpStateMachine.this     // Catch:{ TranslationException | IOException | NullPointerException | XmlPullParserException -> 0x0189 }
                    com.sec.internal.ims.servicemodules.im.FtHttpIncomingMessage r2 = com.sec.internal.ims.servicemodules.im.FtHttpIncomingMessage.this     // Catch:{ TranslationException | IOException | NullPointerException | XmlPullParserException -> 0x0189 }
                    int r2 = r2.mId     // Catch:{ TranslationException | IOException | NullPointerException | XmlPullParserException -> 0x0189 }
                    r1.append(r2)     // Catch:{ TranslationException | IOException | NullPointerException | XmlPullParserException -> 0x0189 }
                    java.lang.String r2 = "] was silently cancelled due to untrusted file or thumbnail URL"
                    r1.append(r2)     // Catch:{ TranslationException | IOException | NullPointerException | XmlPullParserException -> 0x0189 }
                    java.lang.String r1 = r1.toString()     // Catch:{ TranslationException | IOException | NullPointerException | XmlPullParserException -> 0x0189 }
                    android.util.Log.i(r0, r1)     // Catch:{ TranslationException | IOException | NullPointerException | XmlPullParserException -> 0x0189 }
                    com.sec.internal.ims.servicemodules.im.FtHttpIncomingMessage$FtHttpStateMachine r0 = com.sec.internal.ims.servicemodules.im.FtHttpIncomingMessage.FtHttpStateMachine.this     // Catch:{ TranslationException | IOException | NullPointerException | XmlPullParserException -> 0x0189 }
                    com.sec.internal.ims.servicemodules.im.FtHttpIncomingMessage r1 = com.sec.internal.ims.servicemodules.im.FtHttpIncomingMessage.this     // Catch:{ TranslationException | IOException | NullPointerException | XmlPullParserException -> 0x0189 }
                    com.sec.internal.constants.ims.servicemodules.im.reason.CancelReason r2 = com.sec.internal.constants.ims.servicemodules.im.reason.CancelReason.INVALID_URL_TEMPLATE     // Catch:{ TranslationException | IOException | NullPointerException | XmlPullParserException -> 0x0189 }
                    r1.mCancelReason = r2     // Catch:{ TranslationException | IOException | NullPointerException | XmlPullParserException -> 0x0189 }
                    com.sec.internal.ims.servicemodules.im.FtHttpIncomingMessage$FtHttpStateMachine$CanceledState r1 = r0.mCanceledState     // Catch:{ TranslationException | IOException | NullPointerException | XmlPullParserException -> 0x0189 }
                    r0.transitionTo(r1)     // Catch:{ TranslationException | IOException | NullPointerException | XmlPullParserException -> 0x0189 }
                    com.sec.internal.ims.servicemodules.im.FtHttpIncomingMessage$FtHttpStateMachine r0 = com.sec.internal.ims.servicemodules.im.FtHttpIncomingMessage.FtHttpStateMachine.this     // Catch:{ TranslationException | IOException | NullPointerException | XmlPullParserException -> 0x0189 }
                    com.sec.internal.ims.servicemodules.im.FtHttpIncomingMessage r0 = com.sec.internal.ims.servicemodules.im.FtHttpIncomingMessage.this     // Catch:{ TranslationException | IOException | NullPointerException | XmlPullParserException -> 0x0189 }
                    com.sec.internal.constants.ims.servicemodules.im.ImCacheAction r1 = com.sec.internal.constants.ims.servicemodules.im.ImCacheAction.UPDATED     // Catch:{ TranslationException | IOException | NullPointerException | XmlPullParserException -> 0x0189 }
                    r0.triggerObservers(r1)     // Catch:{ TranslationException | IOException | NullPointerException | XmlPullParserException -> 0x0189 }
                    return
                L_0x00b6:
                    com.sec.internal.ims.servicemodules.im.FtHttpIncomingMessage$FtHttpStateMachine r2 = com.sec.internal.ims.servicemodules.im.FtHttpIncomingMessage.FtHttpStateMachine.this     // Catch:{ TranslationException | IOException | NullPointerException | XmlPullParserException -> 0x0189 }
                    com.sec.internal.ims.servicemodules.im.FtHttpIncomingMessage r2 = com.sec.internal.ims.servicemodules.im.FtHttpIncomingMessage.this     // Catch:{ TranslationException | IOException | NullPointerException | XmlPullParserException -> 0x0189 }
                    java.lang.String r3 = r1.getFileName()     // Catch:{ TranslationException | IOException | NullPointerException | XmlPullParserException -> 0x0189 }
                    r2.mFileName = r3     // Catch:{ TranslationException | IOException | NullPointerException | XmlPullParserException -> 0x0189 }
                    com.sec.internal.ims.servicemodules.im.FtHttpIncomingMessage$FtHttpStateMachine r2 = com.sec.internal.ims.servicemodules.im.FtHttpIncomingMessage.FtHttpStateMachine.this     // Catch:{ TranslationException | IOException | NullPointerException | XmlPullParserException -> 0x0189 }
                    com.sec.internal.ims.servicemodules.im.FtHttpIncomingMessage r2 = com.sec.internal.ims.servicemodules.im.FtHttpIncomingMessage.this     // Catch:{ TranslationException | IOException | NullPointerException | XmlPullParserException -> 0x0189 }
                    long r3 = r1.getFileSize()     // Catch:{ TranslationException | IOException | NullPointerException | XmlPullParserException -> 0x0189 }
                    r2.mFileSize = r3     // Catch:{ TranslationException | IOException | NullPointerException | XmlPullParserException -> 0x0189 }
                    com.sec.internal.ims.servicemodules.im.FtHttpIncomingMessage$FtHttpStateMachine r2 = com.sec.internal.ims.servicemodules.im.FtHttpIncomingMessage.FtHttpStateMachine.this     // Catch:{ TranslationException | IOException | NullPointerException | XmlPullParserException -> 0x0189 }
                    com.sec.internal.ims.servicemodules.im.FtHttpIncomingMessage r2 = com.sec.internal.ims.servicemodules.im.FtHttpIncomingMessage.this     // Catch:{ TranslationException | IOException | NullPointerException | XmlPullParserException -> 0x0189 }
                    java.lang.String r3 = r1.getContentType()     // Catch:{ TranslationException | IOException | NullPointerException | XmlPullParserException -> 0x0189 }
                    r2.mContentType = r3     // Catch:{ TranslationException | IOException | NullPointerException | XmlPullParserException -> 0x0189 }
                    com.sec.internal.ims.servicemodules.im.FtHttpIncomingMessage$FtHttpStateMachine r2 = com.sec.internal.ims.servicemodules.im.FtHttpIncomingMessage.FtHttpStateMachine.this     // Catch:{ TranslationException | IOException | NullPointerException | XmlPullParserException -> 0x0189 }
                    com.sec.internal.ims.servicemodules.im.FtHttpIncomingMessage r2 = com.sec.internal.ims.servicemodules.im.FtHttpIncomingMessage.this     // Catch:{ TranslationException | IOException | NullPointerException | XmlPullParserException -> 0x0189 }
                    java.lang.String r3 = r2.mContentType     // Catch:{ TranslationException | IOException | NullPointerException | XmlPullParserException -> 0x0189 }
                    com.sec.internal.constants.ims.servicemodules.im.ImConstants$Type r3 = com.sec.internal.ims.servicemodules.im.FtMessage.getType(r3)     // Catch:{ TranslationException | IOException | NullPointerException | XmlPullParserException -> 0x0189 }
                    r2.mType = r3     // Catch:{ TranslationException | IOException | NullPointerException | XmlPullParserException -> 0x0189 }
                    com.sec.internal.ims.servicemodules.im.FtHttpIncomingMessage$FtHttpStateMachine r2 = com.sec.internal.ims.servicemodules.im.FtHttpIncomingMessage.FtHttpStateMachine.this     // Catch:{ TranslationException | IOException | NullPointerException | XmlPullParserException -> 0x0189 }
                    com.sec.internal.ims.servicemodules.im.FtHttpIncomingMessage r2 = com.sec.internal.ims.servicemodules.im.FtHttpIncomingMessage.this     // Catch:{ TranslationException | IOException | NullPointerException | XmlPullParserException -> 0x0189 }
                    java.lang.String r3 = r1.getDataUntil()     // Catch:{ TranslationException | IOException | NullPointerException | XmlPullParserException -> 0x0189 }
                    r2.mFileExpire = r3     // Catch:{ TranslationException | IOException | NullPointerException | XmlPullParserException -> 0x0189 }
                    com.sec.internal.ims.servicemodules.im.FtHttpIncomingMessage$FtHttpStateMachine r2 = com.sec.internal.ims.servicemodules.im.FtHttpIncomingMessage.FtHttpStateMachine.this     // Catch:{ TranslationException | IOException | NullPointerException | XmlPullParserException -> 0x0189 }
                    com.sec.internal.ims.servicemodules.im.FtHttpIncomingMessage r2 = com.sec.internal.ims.servicemodules.im.FtHttpIncomingMessage.this     // Catch:{ TranslationException | IOException | NullPointerException | XmlPullParserException -> 0x0189 }
                    com.sec.internal.constants.ims.servicemodules.im.FileDisposition r3 = r1.getFileDisposition()     // Catch:{ TranslationException | IOException | NullPointerException | XmlPullParserException -> 0x0189 }
                    r2.mFileDisposition = r3     // Catch:{ TranslationException | IOException | NullPointerException | XmlPullParserException -> 0x0189 }
                    com.sec.internal.ims.servicemodules.im.FtHttpIncomingMessage$FtHttpStateMachine r2 = com.sec.internal.ims.servicemodules.im.FtHttpIncomingMessage.FtHttpStateMachine.this     // Catch:{ TranslationException | IOException | NullPointerException | XmlPullParserException -> 0x0189 }
                    com.sec.internal.ims.servicemodules.im.FtHttpIncomingMessage r2 = com.sec.internal.ims.servicemodules.im.FtHttpIncomingMessage.this     // Catch:{ TranslationException | IOException | NullPointerException | XmlPullParserException -> 0x0189 }
                    int r3 = r1.getPlayingLength()     // Catch:{ TranslationException | IOException | NullPointerException | XmlPullParserException -> 0x0189 }
                    r2.mPlayingLength = r3     // Catch:{ TranslationException | IOException | NullPointerException | XmlPullParserException -> 0x0189 }
                    com.sec.internal.ims.servicemodules.im.FtHttpIncomingMessage$FtHttpStateMachine r2 = com.sec.internal.ims.servicemodules.im.FtHttpIncomingMessage.FtHttpStateMachine.this     // Catch:{ UnsupportedEncodingException | IllegalArgumentException -> 0x0181, TranslationException | IOException | NullPointerException | XmlPullParserException -> 0x0189, TranslationException | IOException | NullPointerException | XmlPullParserException -> 0x0189, TranslationException | IOException | NullPointerException | XmlPullParserException -> 0x0189 }
                    com.sec.internal.ims.servicemodules.im.FtHttpIncomingMessage r2 = com.sec.internal.ims.servicemodules.im.FtHttpIncomingMessage.this     // Catch:{ UnsupportedEncodingException | IllegalArgumentException -> 0x0181, TranslationException | IOException | NullPointerException | XmlPullParserException -> 0x0189, TranslationException | IOException | NullPointerException | XmlPullParserException -> 0x0189, TranslationException | IOException | NullPointerException | XmlPullParserException -> 0x0189 }
                    java.lang.String r2 = r2.mFileName     // Catch:{ UnsupportedEncodingException | IllegalArgumentException -> 0x0181, TranslationException | IOException | NullPointerException | XmlPullParserException -> 0x0189, TranslationException | IOException | NullPointerException | XmlPullParserException -> 0x0189, TranslationException | IOException | NullPointerException | XmlPullParserException -> 0x0189 }
                    boolean r2 = android.text.TextUtils.isEmpty(r2)     // Catch:{ UnsupportedEncodingException | IllegalArgumentException -> 0x0181, TranslationException | IOException | NullPointerException | XmlPullParserException -> 0x0189, TranslationException | IOException | NullPointerException | XmlPullParserException -> 0x0189, TranslationException | IOException | NullPointerException | XmlPullParserException -> 0x0189 }
                    if (r2 == 0) goto L_0x0170
                    com.sec.internal.ims.servicemodules.im.FtHttpIncomingMessage$FtHttpStateMachine r1 = com.sec.internal.ims.servicemodules.im.FtHttpIncomingMessage.FtHttpStateMachine.this     // Catch:{ UnsupportedEncodingException | IllegalArgumentException -> 0x0181, TranslationException | IOException | NullPointerException | XmlPullParserException -> 0x0189, TranslationException | IOException | NullPointerException | XmlPullParserException -> 0x0189, TranslationException | IOException | NullPointerException | XmlPullParserException -> 0x0189 }
                    com.sec.internal.ims.servicemodules.im.FtHttpIncomingMessage r1 = com.sec.internal.ims.servicemodules.im.FtHttpIncomingMessage.this     // Catch:{ UnsupportedEncodingException | IllegalArgumentException -> 0x0181, TranslationException | IOException | NullPointerException | XmlPullParserException -> 0x0189, TranslationException | IOException | NullPointerException | XmlPullParserException -> 0x0189, TranslationException | IOException | NullPointerException | XmlPullParserException -> 0x0189 }
                    java.net.URL r2 = r1.mDataUrl     // Catch:{ UnsupportedEncodingException | IllegalArgumentException -> 0x0181, TranslationException | IOException | NullPointerException | XmlPullParserException -> 0x0189, TranslationException | IOException | NullPointerException | XmlPullParserException -> 0x0189, TranslationException | IOException | NullPointerException | XmlPullParserException -> 0x0189 }
                    java.lang.String r2 = r2.toString()     // Catch:{ UnsupportedEncodingException | IllegalArgumentException -> 0x0181, TranslationException | IOException | NullPointerException | XmlPullParserException -> 0x0189, TranslationException | IOException | NullPointerException | XmlPullParserException -> 0x0189, TranslationException | IOException | NullPointerException | XmlPullParserException -> 0x0189 }
                    com.sec.internal.ims.servicemodules.im.FtHttpIncomingMessage$FtHttpStateMachine r3 = com.sec.internal.ims.servicemodules.im.FtHttpIncomingMessage.FtHttpStateMachine.this     // Catch:{ UnsupportedEncodingException | IllegalArgumentException -> 0x0181, TranslationException | IOException | NullPointerException | XmlPullParserException -> 0x0189, TranslationException | IOException | NullPointerException | XmlPullParserException -> 0x0189, TranslationException | IOException | NullPointerException | XmlPullParserException -> 0x0189 }
                    com.sec.internal.ims.servicemodules.im.FtHttpIncomingMessage r3 = com.sec.internal.ims.servicemodules.im.FtHttpIncomingMessage.this     // Catch:{ UnsupportedEncodingException | IllegalArgumentException -> 0x0181, TranslationException | IOException | NullPointerException | XmlPullParserException -> 0x0189, TranslationException | IOException | NullPointerException | XmlPullParserException -> 0x0189, TranslationException | IOException | NullPointerException | XmlPullParserException -> 0x0189 }
                    java.net.URL r3 = r3.mDataUrl     // Catch:{ UnsupportedEncodingException | IllegalArgumentException -> 0x0181, TranslationException | IOException | NullPointerException | XmlPullParserException -> 0x0189, TranslationException | IOException | NullPointerException | XmlPullParserException -> 0x0189, TranslationException | IOException | NullPointerException | XmlPullParserException -> 0x0189 }
                    java.lang.String r3 = r3.toString()     // Catch:{ UnsupportedEncodingException | IllegalArgumentException -> 0x0181, TranslationException | IOException | NullPointerException | XmlPullParserException -> 0x0189, TranslationException | IOException | NullPointerException | XmlPullParserException -> 0x0189, TranslationException | IOException | NullPointerException | XmlPullParserException -> 0x0189 }
                    java.lang.String r4 = "/"
                    int r3 = r3.lastIndexOf(r4)     // Catch:{ UnsupportedEncodingException | IllegalArgumentException -> 0x0181, TranslationException | IOException | NullPointerException | XmlPullParserException -> 0x0189, TranslationException | IOException | NullPointerException | XmlPullParserException -> 0x0189, TranslationException | IOException | NullPointerException | XmlPullParserException -> 0x0189 }
                    int r3 = r3 + 1
                    java.lang.String r2 = r2.substring(r3)     // Catch:{ UnsupportedEncodingException | IllegalArgumentException -> 0x0181, TranslationException | IOException | NullPointerException | XmlPullParserException -> 0x0189, TranslationException | IOException | NullPointerException | XmlPullParserException -> 0x0189, TranslationException | IOException | NullPointerException | XmlPullParserException -> 0x0189 }
                    r1.mFileName = r2     // Catch:{ UnsupportedEncodingException | IllegalArgumentException -> 0x0181, TranslationException | IOException | NullPointerException | XmlPullParserException -> 0x0189, TranslationException | IOException | NullPointerException | XmlPullParserException -> 0x0189, TranslationException | IOException | NullPointerException | XmlPullParserException -> 0x0189 }
                    com.sec.internal.ims.servicemodules.im.FtHttpIncomingMessage$FtHttpStateMachine r1 = com.sec.internal.ims.servicemodules.im.FtHttpIncomingMessage.FtHttpStateMachine.this     // Catch:{ UnsupportedEncodingException | IllegalArgumentException -> 0x0181, TranslationException | IOException | NullPointerException | XmlPullParserException -> 0x0189, TranslationException | IOException | NullPointerException | XmlPullParserException -> 0x0189, TranslationException | IOException | NullPointerException | XmlPullParserException -> 0x0189 }
                    com.sec.internal.ims.servicemodules.im.FtHttpIncomingMessage r1 = com.sec.internal.ims.servicemodules.im.FtHttpIncomingMessage.this     // Catch:{ UnsupportedEncodingException | IllegalArgumentException -> 0x0181, TranslationException | IOException | NullPointerException | XmlPullParserException -> 0x0189, TranslationException | IOException | NullPointerException | XmlPullParserException -> 0x0189, TranslationException | IOException | NullPointerException | XmlPullParserException -> 0x0189 }
                    java.lang.String r1 = r1.mFileName     // Catch:{ UnsupportedEncodingException | IllegalArgumentException -> 0x0181, TranslationException | IOException | NullPointerException | XmlPullParserException -> 0x0189, TranslationException | IOException | NullPointerException | XmlPullParserException -> 0x0189, TranslationException | IOException | NullPointerException | XmlPullParserException -> 0x0189 }
                    boolean r1 = r1.contains(r0)     // Catch:{ UnsupportedEncodingException | IllegalArgumentException -> 0x0181, TranslationException | IOException | NullPointerException | XmlPullParserException -> 0x0189, TranslationException | IOException | NullPointerException | XmlPullParserException -> 0x0189, TranslationException | IOException | NullPointerException | XmlPullParserException -> 0x0189 }
                    if (r1 != 0) goto L_0x0185
                    java.text.SimpleDateFormat r1 = new java.text.SimpleDateFormat     // Catch:{ UnsupportedEncodingException | IllegalArgumentException -> 0x0181, TranslationException | IOException | NullPointerException | XmlPullParserException -> 0x0189, TranslationException | IOException | NullPointerException | XmlPullParserException -> 0x0189, TranslationException | IOException | NullPointerException | XmlPullParserException -> 0x0189 }
                    java.lang.String r2 = "yyMMdd_HHmmss"
                    r1.<init>(r2)     // Catch:{ UnsupportedEncodingException | IllegalArgumentException -> 0x0181, TranslationException | IOException | NullPointerException | XmlPullParserException -> 0x0189, TranslationException | IOException | NullPointerException | XmlPullParserException -> 0x0189, TranslationException | IOException | NullPointerException | XmlPullParserException -> 0x0189 }
                    com.sec.internal.ims.servicemodules.im.FtHttpIncomingMessage$FtHttpStateMachine r2 = com.sec.internal.ims.servicemodules.im.FtHttpIncomingMessage.FtHttpStateMachine.this     // Catch:{ UnsupportedEncodingException | IllegalArgumentException -> 0x0181, TranslationException | IOException | NullPointerException | XmlPullParserException -> 0x0189, TranslationException | IOException | NullPointerException | XmlPullParserException -> 0x0189, TranslationException | IOException | NullPointerException | XmlPullParserException -> 0x0189 }
                    com.sec.internal.ims.servicemodules.im.FtHttpIncomingMessage r2 = com.sec.internal.ims.servicemodules.im.FtHttpIncomingMessage.this     // Catch:{ UnsupportedEncodingException | IllegalArgumentException -> 0x0181, TranslationException | IOException | NullPointerException | XmlPullParserException -> 0x0189, TranslationException | IOException | NullPointerException | XmlPullParserException -> 0x0189, TranslationException | IOException | NullPointerException | XmlPullParserException -> 0x0189 }
                    java.lang.StringBuilder r3 = new java.lang.StringBuilder     // Catch:{ UnsupportedEncodingException | IllegalArgumentException -> 0x0181, TranslationException | IOException | NullPointerException | XmlPullParserException -> 0x0189, TranslationException | IOException | NullPointerException | XmlPullParserException -> 0x0189, TranslationException | IOException | NullPointerException | XmlPullParserException -> 0x0189 }
                    r3.<init>()     // Catch:{ UnsupportedEncodingException | IllegalArgumentException -> 0x0181, TranslationException | IOException | NullPointerException | XmlPullParserException -> 0x0189, TranslationException | IOException | NullPointerException | XmlPullParserException -> 0x0189, TranslationException | IOException | NullPointerException | XmlPullParserException -> 0x0189 }
                    java.util.Date r4 = new java.util.Date     // Catch:{ UnsupportedEncodingException | IllegalArgumentException -> 0x0181, TranslationException | IOException | NullPointerException | XmlPullParserException -> 0x0189, TranslationException | IOException | NullPointerException | XmlPullParserException -> 0x0189, TranslationException | IOException | NullPointerException | XmlPullParserException -> 0x0189 }
                    r4.<init>()     // Catch:{ UnsupportedEncodingException | IllegalArgumentException -> 0x0181, TranslationException | IOException | NullPointerException | XmlPullParserException -> 0x0189, TranslationException | IOException | NullPointerException | XmlPullParserException -> 0x0189, TranslationException | IOException | NullPointerException | XmlPullParserException -> 0x0189 }
                    java.lang.String r1 = r1.format(r4)     // Catch:{ UnsupportedEncodingException | IllegalArgumentException -> 0x0181, TranslationException | IOException | NullPointerException | XmlPullParserException -> 0x0189, TranslationException | IOException | NullPointerException | XmlPullParserException -> 0x0189, TranslationException | IOException | NullPointerException | XmlPullParserException -> 0x0189 }
                    r3.append(r1)     // Catch:{ UnsupportedEncodingException | IllegalArgumentException -> 0x0181, TranslationException | IOException | NullPointerException | XmlPullParserException -> 0x0189, TranslationException | IOException | NullPointerException | XmlPullParserException -> 0x0189, TranslationException | IOException | NullPointerException | XmlPullParserException -> 0x0189 }
                    r3.append(r0)     // Catch:{ UnsupportedEncodingException | IllegalArgumentException -> 0x0181, TranslationException | IOException | NullPointerException | XmlPullParserException -> 0x0189, TranslationException | IOException | NullPointerException | XmlPullParserException -> 0x0189, TranslationException | IOException | NullPointerException | XmlPullParserException -> 0x0189 }
                    com.sec.internal.ims.servicemodules.im.FtHttpIncomingMessage$FtHttpStateMachine r0 = com.sec.internal.ims.servicemodules.im.FtHttpIncomingMessage.FtHttpStateMachine.this     // Catch:{ UnsupportedEncodingException | IllegalArgumentException -> 0x0181, TranslationException | IOException | NullPointerException | XmlPullParserException -> 0x0189, TranslationException | IOException | NullPointerException | XmlPullParserException -> 0x0189, TranslationException | IOException | NullPointerException | XmlPullParserException -> 0x0189 }
                    com.sec.internal.ims.servicemodules.im.FtHttpIncomingMessage r0 = com.sec.internal.ims.servicemodules.im.FtHttpIncomingMessage.this     // Catch:{ UnsupportedEncodingException | IllegalArgumentException -> 0x0181, TranslationException | IOException | NullPointerException | XmlPullParserException -> 0x0189, TranslationException | IOException | NullPointerException | XmlPullParserException -> 0x0189, TranslationException | IOException | NullPointerException | XmlPullParserException -> 0x0189 }
                    java.lang.String r0 = r0.mContentType     // Catch:{ UnsupportedEncodingException | IllegalArgumentException -> 0x0181, TranslationException | IOException | NullPointerException | XmlPullParserException -> 0x0189, TranslationException | IOException | NullPointerException | XmlPullParserException -> 0x0189, TranslationException | IOException | NullPointerException | XmlPullParserException -> 0x0189 }
                    java.lang.String r0 = com.sec.internal.helper.translate.FileExtensionTranslator.translate(r0)     // Catch:{ UnsupportedEncodingException | IllegalArgumentException -> 0x0181, TranslationException | IOException | NullPointerException | XmlPullParserException -> 0x0189, TranslationException | IOException | NullPointerException | XmlPullParserException -> 0x0189, TranslationException | IOException | NullPointerException | XmlPullParserException -> 0x0189 }
                    r3.append(r0)     // Catch:{ UnsupportedEncodingException | IllegalArgumentException -> 0x0181, TranslationException | IOException | NullPointerException | XmlPullParserException -> 0x0189, TranslationException | IOException | NullPointerException | XmlPullParserException -> 0x0189, TranslationException | IOException | NullPointerException | XmlPullParserException -> 0x0189 }
                    java.lang.String r0 = r3.toString()     // Catch:{ UnsupportedEncodingException | IllegalArgumentException -> 0x0181, TranslationException | IOException | NullPointerException | XmlPullParserException -> 0x0189, TranslationException | IOException | NullPointerException | XmlPullParserException -> 0x0189, TranslationException | IOException | NullPointerException | XmlPullParserException -> 0x0189 }
                    r2.mFileName = r0     // Catch:{ UnsupportedEncodingException | IllegalArgumentException -> 0x0181, TranslationException | IOException | NullPointerException | XmlPullParserException -> 0x0189, TranslationException | IOException | NullPointerException | XmlPullParserException -> 0x0189, TranslationException | IOException | NullPointerException | XmlPullParserException -> 0x0189 }
                    goto L_0x0185
                L_0x0170:
                    com.sec.internal.ims.servicemodules.im.FtHttpIncomingMessage$FtHttpStateMachine r0 = com.sec.internal.ims.servicemodules.im.FtHttpIncomingMessage.FtHttpStateMachine.this     // Catch:{ UnsupportedEncodingException | IllegalArgumentException -> 0x0181, TranslationException | IOException | NullPointerException | XmlPullParserException -> 0x0189, TranslationException | IOException | NullPointerException | XmlPullParserException -> 0x0189, TranslationException | IOException | NullPointerException | XmlPullParserException -> 0x0189 }
                    com.sec.internal.ims.servicemodules.im.FtHttpIncomingMessage r0 = com.sec.internal.ims.servicemodules.im.FtHttpIncomingMessage.this     // Catch:{ UnsupportedEncodingException | IllegalArgumentException -> 0x0181, TranslationException | IOException | NullPointerException | XmlPullParserException -> 0x0189, TranslationException | IOException | NullPointerException | XmlPullParserException -> 0x0189, TranslationException | IOException | NullPointerException | XmlPullParserException -> 0x0189 }
                    java.lang.String r1 = r1.getFileName()     // Catch:{ UnsupportedEncodingException | IllegalArgumentException -> 0x0181, TranslationException | IOException | NullPointerException | XmlPullParserException -> 0x0189, TranslationException | IOException | NullPointerException | XmlPullParserException -> 0x0189, TranslationException | IOException | NullPointerException | XmlPullParserException -> 0x0189 }
                    java.lang.String r2 = "UTF-8"
                    java.lang.String r1 = java.net.URLDecoder.decode(r1, r2)     // Catch:{ UnsupportedEncodingException | IllegalArgumentException -> 0x0181, TranslationException | IOException | NullPointerException | XmlPullParserException -> 0x0189, TranslationException | IOException | NullPointerException | XmlPullParserException -> 0x0189, TranslationException | IOException | NullPointerException | XmlPullParserException -> 0x0189 }
                    r0.mFileName = r1     // Catch:{ UnsupportedEncodingException | IllegalArgumentException -> 0x0181, TranslationException | IOException | NullPointerException | XmlPullParserException -> 0x0189, TranslationException | IOException | NullPointerException | XmlPullParserException -> 0x0189, TranslationException | IOException | NullPointerException | XmlPullParserException -> 0x0189 }
                    goto L_0x0185
                L_0x0181:
                    r0 = move-exception
                    r0.printStackTrace()     // Catch:{ TranslationException | IOException | NullPointerException | XmlPullParserException -> 0x0189 }
                L_0x0185:
                    r5.tryThumbnailDownload()     // Catch:{ TranslationException | IOException | NullPointerException | XmlPullParserException -> 0x0189 }
                    goto L_0x0196
                L_0x0189:
                    r0 = move-exception
                    r0.printStackTrace()
                    com.sec.internal.ims.servicemodules.im.FtHttpIncomingMessage$FtHttpStateMachine r0 = com.sec.internal.ims.servicemodules.im.FtHttpIncomingMessage.FtHttpStateMachine.this
                    com.sec.internal.ims.servicemodules.im.FtHttpIncomingMessage$FtHttpStateMachine$CanceledState r1 = r0.mCanceledState
                    r0.transitionTo(r1)
                L_0x0196:
                    com.sec.internal.ims.servicemodules.im.FtHttpIncomingMessage$FtHttpStateMachine r5 = com.sec.internal.ims.servicemodules.im.FtHttpIncomingMessage.FtHttpStateMachine.this
                    com.sec.internal.ims.servicemodules.im.FtHttpIncomingMessage r5 = com.sec.internal.ims.servicemodules.im.FtHttpIncomingMessage.this
                    com.sec.internal.constants.ims.servicemodules.im.ImCacheAction r0 = com.sec.internal.constants.ims.servicemodules.im.ImCacheAction.UPDATED
                    r5.triggerObservers(r0)
                    return
                */
                throw new UnsupportedOperationException("Method not decompiled: com.sec.internal.ims.servicemodules.im.FtHttpIncomingMessage.FtHttpStateMachine.InitialState.handleReceiverTransferEvent():void");
            }

            private void tryThumbnailDownload() {
                try {
                    FtHttpFileInfo parse = FtHttpXmlParser.parse(FtHttpIncomingMessage.this.mBody);
                    long thumbnailSize = getThumbnailSize(parse);
                    if (thumbnailSize == 0) {
                        FtHttpStateMachine ftHttpStateMachine = FtHttpStateMachine.this;
                        FtHttpIncomingMessage.this.mThumbnailPath = null;
                        ftHttpStateMachine.sendMessage(104);
                        return;
                    }
                    if (parse != null) {
                        FtHttpIncomingMessage ftHttpIncomingMessage = FtHttpIncomingMessage.this;
                        if (ftHttpIncomingMessage.mThumbnailContentType == null) {
                            ftHttpIncomingMessage.mThumbnailContentType = parse.getThumbnailContentType();
                        }
                    }
                    FtHttpIncomingMessage ftHttpIncomingMessage2 = FtHttpIncomingMessage.this;
                    if (ftHttpIncomingMessage2.mThumbnailPath == null) {
                        Context context = ftHttpIncomingMessage2.getContext();
                        FtHttpIncomingMessage ftHttpIncomingMessage3 = FtHttpIncomingMessage.this;
                        ftHttpIncomingMessage2.mThumbnailPath = FilePathGenerator.generateUniqueThumbnailPath(context, ftHttpIncomingMessage3.mFileName, ftHttpIncomingMessage3.mThumbnailContentType, ftHttpIncomingMessage3.mListener.onRequestIncomingFtTransferPath(), 128);
                    }
                    String r3 = FtHttpIncomingMessage.LOG_TAG;
                    Log.i(r3, "tryThumbnailDownload: thumbnailContentType=" + FtHttpIncomingMessage.this.mThumbnailContentType + ", thumbnailPath=" + FtHttpIncomingMessage.this.mThumbnailPath);
                    downloadThumbnail(FtHttpIncomingMessage.this.mThumbnailPath, parse != null ? parse.getThumbnailDataUrl().toString() : "", thumbnailSize);
                } catch (Exception e) {
                    e.printStackTrace();
                    onThumbnailDownloadFailed();
                }
            }

            private void onThumbnailDownloadFailed() {
                FtHttpStateMachine ftHttpStateMachine = FtHttpStateMachine.this;
                FtHttpIncomingMessage.this.mThumbnailPath = null;
                ftHttpStateMachine.sendMessage(104);
            }

            private long getThumbnailSize(FtHttpFileInfo ftHttpFileInfo) {
                if (ftHttpFileInfo == null || !ftHttpFileInfo.isThumbnailExist() || ftHttpFileInfo.getThumbnailFileSize() > FtHttpIncomingMessage.this.MAX_SIZE_DOWNLOAD_THUMBNAIL) {
                    return 0;
                }
                return ftHttpFileInfo.getThumbnailFileSize();
            }

            private void downloadThumbnail(String str, String str2, long j) {
                Network network;
                boolean boolSetting = FtHttpIncomingMessage.this.getRcsStrategy().boolSetting(RcsPolicySettings.RcsPolicy.FT_INTERNET_PDN);
                String ftHttpUserAgent = FtHttpIncomingMessage.this.getFtHttpUserAgent();
                if (boolSetting) {
                    network = null;
                } else {
                    network = FtHttpIncomingMessage.this.mNetwork;
                }
                String str3 = str2;
                final long j2 = j;
                String str4 = str;
                DownloadFileTask.DownloadRequest downloadRequest = r2;
                DownloadFileTask.DownloadRequest downloadRequest2 = new DownloadFileTask.DownloadRequest(str3, j2, 0, str4, (Uri) null, ftHttpUserAgent, network, FtHttpIncomingMessage.this.mConfig.isFtHttpTrustAllCerts(), FtHttpIncomingMessage.this.mConfig.getFtHttpDLUrl(), FtHttpIncomingMessage.this.getParamsforDl(str3), new DownloadFileTask.DownloadTaskCallback() {
                    public void onProgressUpdate(long j) {
                        long j2 = j2;
                        FtHttpStateMachine ftHttpStateMachine = FtHttpStateMachine.this;
                        if (j > j2 + FtHttpIncomingMessage.this.FT_SIZE_MARGIN) {
                            ftHttpStateMachine.sendMessage(ftHttpStateMachine.obtainMessage(106));
                        }
                    }

                    public void onCompleted(long j) {
                        long j2 = j2;
                        FtHttpStateMachine ftHttpStateMachine = FtHttpStateMachine.this;
                        FtHttpIncomingMessage ftHttpIncomingMessage = FtHttpIncomingMessage.this;
                        long j3 = ftHttpIncomingMessage.FT_SIZE_MARGIN;
                        if (j < j2 - j3 || j > j2 + j3) {
                            ftHttpStateMachine.sendMessage(ftHttpStateMachine.obtainMessage(106));
                            return;
                        }
                        String renameThumbnail = FilePathGenerator.renameThumbnail(ftHttpIncomingMessage.mThumbnailPath, ftHttpIncomingMessage.mThumbnailContentType, ftHttpIncomingMessage.mFileName, 128);
                        if (renameThumbnail != null) {
                            FtHttpIncomingMessage ftHttpIncomingMessage2 = FtHttpIncomingMessage.this;
                            ftHttpIncomingMessage2.mThumbnailPath = renameThumbnail;
                            ftHttpIncomingMessage2.triggerObservers(ImCacheAction.UPDATED);
                        }
                        FtHttpStateMachine.this.sendMessage(104, (Object) Long.valueOf(j));
                    }

                    public void onCanceled(CancelReason cancelReason, int i, int i2) {
                        FtHttpStateMachine ftHttpStateMachine = FtHttpStateMachine.this;
                        ftHttpStateMachine.sendMessage(ftHttpStateMachine.obtainMessage(105, i, i2, cancelReason));
                    }
                });
                if (downloadRequest.isValid()) {
                    DownloadFileTask downloadFileTask = new DownloadFileTask(FtHttpIncomingMessage.this.mConfig.getPhoneId(), FtHttpIncomingMessage.this.getContext(), FtHttpStateMachine.this.getHandler().getLooper(), downloadRequest);
                    this.thumbnailDownloadTask = downloadFileTask;
                    downloadFileTask.execute(AsyncFileTask.THREAD_THUMBNAIL_POOL_EXECUTOR);
                    return;
                }
                onThumbnailDownloadFailed();
            }
        }

        private final class AcceptingState extends State {
            private AcceptingState() {
            }

            public void enter() {
                String r0 = FtHttpIncomingMessage.LOG_TAG;
                Log.i(r0, getName() + " enter msgId : " + FtHttpIncomingMessage.this.mId);
                FtHttpIncomingMessage.this.updateState();
            }

            public boolean processMessage(Message message) {
                int i = message.what;
                if (i == 4) {
                    FtHttpStateMachine ftHttpStateMachine = FtHttpStateMachine.this;
                    ftHttpStateMachine.transitionTo(ftHttpStateMachine.mInProgressState);
                } else if (i == 6) {
                    FtHttpStateMachine ftHttpStateMachine2 = FtHttpStateMachine.this;
                    FtHttpIncomingMessage ftHttpIncomingMessage = FtHttpIncomingMessage.this;
                    ftHttpIncomingMessage.mRejectReason = FtRejectReason.DECLINE;
                    ftHttpIncomingMessage.mCancelReason = CancelReason.REJECTED_BY_USER;
                    ftHttpStateMachine2.transitionTo(ftHttpStateMachine2.mCanceledState);
                } else if (i != 8) {
                    return false;
                } else {
                    FtHttpStateMachine ftHttpStateMachine3 = FtHttpStateMachine.this;
                    FtHttpIncomingMessage.this.mCancelReason = (CancelReason) message.obj;
                    ftHttpStateMachine3.transitionTo(ftHttpStateMachine3.mCanceledState);
                }
                return true;
            }
        }

        private final class InProgressState extends State {
            DownloadFileTask downloadTask;

            private InProgressState() {
            }

            public void enter() {
                String r0 = FtHttpIncomingMessage.LOG_TAG;
                Log.i(r0, getName() + " enter msgId : " + FtHttpIncomingMessage.this.mId);
                if (FtHttpIncomingMessage.this.needToAcquireNetworkForFT()) {
                    FtHttpIncomingMessage ftHttpIncomingMessage = FtHttpIncomingMessage.this;
                    ftHttpIncomingMessage.acquireNetworkForFT(ftHttpIncomingMessage.getRcsStrategy().intSetting(RcsPolicySettings.RcsPolicy.FT_NET_CAPABILITY));
                    FtHttpIncomingMessage.this.acquireWakeLock();
                    return;
                }
                FtHttpStateMachine.this.removeMessages(107);
                FtHttpIncomingMessage.this.setRetryCount(0);
                FtHttpIncomingMessage.this.updateState();
                FtHttpIncomingMessage ftHttpIncomingMessage2 = FtHttpIncomingMessage.this;
                ftHttpIncomingMessage2.mListener.onTransferInProgress(ftHttpIncomingMessage2);
                FtHttpStateMachine ftHttpStateMachine = FtHttpStateMachine.this;
                FtHttpIncomingMessage ftHttpIncomingMessage3 = FtHttpIncomingMessage.this;
                long j = ftHttpIncomingMessage3.mTransferredBytes;
                if (j >= ftHttpIncomingMessage3.mFileSize) {
                    ftHttpStateMachine.sendMessage(102, (Object) Long.valueOf(j));
                } else if (!ftHttpIncomingMessage3.mIsBootup || (!ftHttpIncomingMessage3.getRcsStrategy().boolSetting(RcsPolicySettings.RcsPolicy.CANCEL_FOR_DEREGI_PROMPTLY) && FtHttpIncomingMessage.this.getRcsStrategy().intSetting(RcsPolicySettings.RcsPolicy.DELAY_TO_CANCEL_FOR_DEREGI) <= 0)) {
                    FtHttpStateMachine ftHttpStateMachine2 = FtHttpStateMachine.this;
                    ftHttpStateMachine2.sendMessage(101, (Object) Long.valueOf(FtHttpIncomingMessage.this.mTransferredBytes));
                    tryDownload();
                } else {
                    Log.i(FtHttpIncomingMessage.LOG_TAG, "Do not auto resume message loaded from bootup");
                    FtHttpStateMachine ftHttpStateMachine3 = FtHttpStateMachine.this;
                    FtHttpIncomingMessage ftHttpIncomingMessage4 = FtHttpIncomingMessage.this;
                    ftHttpIncomingMessage4.mIsBootup = false;
                    ftHttpIncomingMessage4.mCancelReason = CancelReason.DEVICE_UNREGISTERED;
                    ftHttpStateMachine3.transitionTo(ftHttpStateMachine3.mCanceledState);
                }
                FtHttpIncomingMessage.this.acquireWakeLock();
            }

            public boolean processMessage(Message message) {
                int i = message.what;
                if (i == 6) {
                    String r11 = FtHttpIncomingMessage.LOG_TAG;
                    Log.i(r11, getName() + " EVENT_REJECT_TRANSFER");
                    DownloadFileTask downloadFileTask = this.downloadTask;
                    if (downloadFileTask != null) {
                        downloadFileTask.cancel(true);
                        this.downloadTask = null;
                    }
                    FtHttpStateMachine.this.removeMessages(107);
                    FtHttpStateMachine ftHttpStateMachine = FtHttpStateMachine.this;
                    ftHttpStateMachine.transitionTo(ftHttpStateMachine.mCanceledState);
                    return true;
                } else if (i == 8) {
                    CancelReason cancelReason = (CancelReason) message.obj;
                    String r0 = FtHttpIncomingMessage.LOG_TAG;
                    Log.i(r0, getName() + " EVENT_CANCEL_TRANSFER CancelReason " + cancelReason);
                    FtHttpStateMachine.this.removeMessages(107);
                    PreciseAlarmManager.getInstance(FtHttpIncomingMessage.this.getContext()).removeMessage(FtHttpStateMachine.this.obtainMessage(52));
                    DownloadFileTask downloadFileTask2 = this.downloadTask;
                    if (downloadFileTask2 != null) {
                        downloadFileTask2.cancel(true);
                        this.downloadTask = null;
                    }
                    if (cancelReason != CancelReason.DEVICE_UNREGISTERED || FtHttpIncomingMessage.this.getRcsStrategy().boolSetting(RcsPolicySettings.RcsPolicy.CANCEL_FOR_DEREGI_PROMPTLY)) {
                        FtHttpStateMachine ftHttpStateMachine2 = FtHttpStateMachine.this;
                        FtHttpIncomingMessage.this.mCancelReason = cancelReason;
                        ftHttpStateMachine2.transitionTo(ftHttpStateMachine2.mCanceledState);
                        return true;
                    }
                    int intSetting = FtHttpIncomingMessage.this.getRcsStrategy().intSetting(RcsPolicySettings.RcsPolicy.DELAY_TO_CANCEL_FOR_DEREGI);
                    if (intSetting <= 0) {
                        return true;
                    }
                    PreciseAlarmManager.getInstance(FtHttpIncomingMessage.this.getContext()).sendMessageDelayed(InProgressState.class.getSimpleName(), FtHttpStateMachine.this.obtainMessage(52), ((long) intSetting) * 1000);
                    return true;
                } else if (i == 10) {
                    FtHttpStateMachine.this.removeMessages(107);
                    PreciseAlarmManager.getInstance(FtHttpIncomingMessage.this.getContext()).removeMessage(FtHttpStateMachine.this.obtainMessage(52));
                    FtHttpStateMachine ftHttpStateMachine3 = FtHttpStateMachine.this;
                    FtHttpIncomingMessage ftHttpIncomingMessage = FtHttpIncomingMessage.this;
                    long j = ftHttpIncomingMessage.mTransferredBytes;
                    if (j < ftHttpIncomingMessage.mFileSize) {
                        tryDownload();
                        return true;
                    }
                    ftHttpStateMachine3.sendMessage(102, (Object) Long.valueOf(j));
                    return true;
                } else if (i != 107) {
                    switch (i) {
                        case 50:
                            FtHttpStateMachine.this.removeMessages(51);
                            FtHttpStateMachine.this.removeMessages(107);
                            FtHttpIncomingMessage.this.setRetryCount(0);
                            FtHttpIncomingMessage.this.updateState();
                            FtHttpIncomingMessage ftHttpIncomingMessage2 = FtHttpIncomingMessage.this;
                            ftHttpIncomingMessage2.mListener.onTransferInProgress(ftHttpIncomingMessage2);
                            FtHttpStateMachine ftHttpStateMachine4 = FtHttpStateMachine.this;
                            FtHttpIncomingMessage ftHttpIncomingMessage3 = FtHttpIncomingMessage.this;
                            long j2 = ftHttpIncomingMessage3.mTransferredBytes;
                            if (j2 < ftHttpIncomingMessage3.mFileSize) {
                                ftHttpStateMachine4.sendMessage(101, (Object) Long.valueOf(j2));
                                tryDownload();
                                return true;
                            }
                            ftHttpStateMachine4.sendMessage(102, (Object) Long.valueOf(j2));
                            return true;
                        case 51:
                            FtHttpStateMachine ftHttpStateMachine5 = FtHttpStateMachine.this;
                            FtHttpIncomingMessage.this.mCancelReason = CancelReason.ERROR;
                            ftHttpStateMachine5.transitionTo(ftHttpStateMachine5.mCanceledState);
                            return true;
                        case 52:
                            String r112 = FtHttpIncomingMessage.LOG_TAG;
                            Log.i(r112, "EVENT_DELAY_CANCEL_TRANSFER mId=" + FtHttpIncomingMessage.this.mId);
                            DownloadFileTask downloadFileTask3 = this.downloadTask;
                            if (downloadFileTask3 != null) {
                                downloadFileTask3.cancel(true);
                                this.downloadTask = null;
                            }
                            FtHttpStateMachine ftHttpStateMachine6 = FtHttpStateMachine.this;
                            FtHttpIncomingMessage.this.mCancelReason = CancelReason.CANCELED_BY_USER;
                            ftHttpStateMachine6.transitionTo(ftHttpStateMachine6.mCanceledState);
                            return true;
                        default:
                            switch (i) {
                                case 101:
                                    FtHttpIncomingMessage.this.updateTransferredBytes(((Long) message.obj).longValue());
                                    String r113 = FtHttpIncomingMessage.LOG_TAG;
                                    Log.i(r113, "EVENT_DOWNLOAD_PROGRESS " + FtHttpIncomingMessage.this.mTransferredBytes + "/" + FtHttpIncomingMessage.this.mFileSize);
                                    FtHttpIncomingMessage ftHttpIncomingMessage4 = FtHttpIncomingMessage.this;
                                    ftHttpIncomingMessage4.mListener.onTransferProgressReceived(ftHttpIncomingMessage4);
                                    return true;
                                case 102:
                                    FtHttpIncomingMessage.this.mTransferredBytes = ((Long) message.obj).longValue();
                                    FtHttpStateMachine ftHttpStateMachine7 = FtHttpStateMachine.this;
                                    ftHttpStateMachine7.transitionTo(ftHttpStateMachine7.mCompletedState);
                                    return true;
                                case 103:
                                    FtHttpIncomingMessage ftHttpIncomingMessage5 = FtHttpIncomingMessage.this;
                                    ftHttpIncomingMessage5.mCancelReason = (CancelReason) message.obj;
                                    this.downloadTask = null;
                                    int ftHttpRetryInterval = ftHttpIncomingMessage5.getRcsStrategy().getFtHttpRetryInterval(message.arg1, FtHttpIncomingMessage.this.getRetryCount());
                                    if (ftHttpRetryInterval >= 0) {
                                        String r2 = FtHttpIncomingMessage.LOG_TAG;
                                        Log.i(r2, "EVENT_RETRY_DOWNLOAD: " + FtHttpIncomingMessage.this.mId + " retry download after " + ftHttpRetryInterval + " secs");
                                        FtHttpStateMachine ftHttpStateMachine8 = FtHttpStateMachine.this;
                                        ftHttpStateMachine8.sendMessageDelayed(ftHttpStateMachine8.obtainMessage(107, 0, message.arg2), ((long) ftHttpRetryInterval) * 1000);
                                        return true;
                                    }
                                    FtHttpStateMachine ftHttpStateMachine9 = FtHttpStateMachine.this;
                                    ftHttpStateMachine9.transitionTo(ftHttpStateMachine9.mCanceledState);
                                    return true;
                                default:
                                    return false;
                            }
                    }
                } else {
                    String r02 = FtHttpIncomingMessage.LOG_TAG;
                    Log.i(r02, "EVENT_RETRY_DOWNLOAD mId=" + FtHttpIncomingMessage.this.mId + "Retry count = " + FtHttpIncomingMessage.this.getRetryCount());
                    int i2 = message.arg2;
                    FtHttpStateMachine.this.removeMessages(107);
                    if (RcsUtils.DualRcs.isDualRcsSettings()) {
                        FtHttpIncomingMessage ftHttpIncomingMessage6 = FtHttpIncomingMessage.this;
                        if (((ImModule) ftHttpIncomingMessage6.mModule).getPhoneIdByChatId(ftHttpIncomingMessage6.mChatId) != SimUtil.getSimSlotPriority()) {
                            return true;
                        }
                    }
                    if (!FtHttpIncomingMessage.this.checkAvailableRetry()) {
                        if (!FtHttpIncomingMessage.this.getRcsStrategy().boolSetting(RcsPolicySettings.RcsPolicy.CANCEL_FOR_DEREGI_PROMPTLY)) {
                            return true;
                        }
                        FtHttpStateMachine ftHttpStateMachine10 = FtHttpStateMachine.this;
                        FtHttpIncomingMessage.this.mCancelReason = CancelReason.CANCELED_BY_USER;
                        ftHttpStateMachine10.transitionTo(ftHttpStateMachine10.mCanceledState);
                        return true;
                    } else if (i2 == 503) {
                        tryDownload();
                        return true;
                    } else if (FtHttpIncomingMessage.this.getRetryCount() < 3) {
                        FtHttpIncomingMessage ftHttpIncomingMessage7 = FtHttpIncomingMessage.this;
                        ftHttpIncomingMessage7.setRetryCount(ftHttpIncomingMessage7.getRetryCount() + 1);
                        tryDownload();
                        return true;
                    } else {
                        FtHttpStateMachine ftHttpStateMachine11 = FtHttpStateMachine.this;
                        ftHttpStateMachine11.transitionTo(ftHttpStateMachine11.mCanceledState);
                        return true;
                    }
                }
            }

            private void tryDownload() {
                FtHttpIncomingMessage ftHttpIncomingMessage = FtHttpIncomingMessage.this;
                ftHttpIncomingMessage.mIsWifiUsed = ftHttpIncomingMessage.getRcsStrategy().boolSetting(RcsPolicySettings.RcsPolicy.CANCEL_FT_WIFI_DISCONNECTED) && FtHttpIncomingMessage.this.isWifiConnected();
                if (!FtHttpIncomingMessage.this.checkValidPeriod()) {
                    Log.e(FtHttpIncomingMessage.LOG_TAG, "Auto cancel file transfer, file has expired");
                    FtHttpStateMachine ftHttpStateMachine = FtHttpStateMachine.this;
                    FtHttpIncomingMessage ftHttpIncomingMessage2 = FtHttpIncomingMessage.this;
                    ftHttpIncomingMessage2.mRejectReason = FtRejectReason.DECLINE;
                    ftHttpIncomingMessage2.mCancelReason = CancelReason.VALIDITY_EXPIRED;
                    ftHttpStateMachine.transitionTo(ftHttpStateMachine.mCanceledState);
                } else if (FtHttpIncomingMessage.this.mDataUrl == null) {
                    String r0 = FtHttpIncomingMessage.LOG_TAG;
                    Log.e(r0, getName() + ": Data url is null, go to Canceled");
                    FtHttpStateMachine ftHttpStateMachine2 = FtHttpStateMachine.this;
                    ftHttpStateMachine2.transitionTo(ftHttpStateMachine2.mCanceledState);
                } else {
                    DownloadFileTask downloadFileTask = this.downloadTask;
                    if (downloadFileTask != null && downloadFileTask.getState() != AsyncFileTask.State.FINISHED) {
                        Log.i(FtHttpIncomingMessage.LOG_TAG, "Task is already running or pending.");
                    } else if (FtHttpIncomingMessage.this.needToAcquireNetworkForFT()) {
                        FtHttpIncomingMessage ftHttpIncomingMessage3 = FtHttpIncomingMessage.this;
                        ftHttpIncomingMessage3.acquireNetworkForFT(ftHttpIncomingMessage3.getRcsStrategy().intSetting(RcsPolicySettings.RcsPolicy.FT_NET_CAPABILITY));
                        FtHttpIncomingMessage.this.acquireWakeLock();
                    } else {
                        createDownloadTask(FtHttpIncomingMessage.this.mDataUrl.toString());
                    }
                }
            }

            private void createDownloadTask(String str) {
                Network network;
                boolean boolSetting = FtHttpIncomingMessage.this.getRcsStrategy().boolSetting(RcsPolicySettings.RcsPolicy.FT_INTERNET_PDN);
                FtHttpIncomingMessage ftHttpIncomingMessage = FtHttpIncomingMessage.this;
                long j = ftHttpIncomingMessage.mFileSize;
                long j2 = ftHttpIncomingMessage.mTransferredBytes;
                Uri uri = ftHttpIncomingMessage.mContentUri;
                String ftHttpUserAgent = ftHttpIncomingMessage.getFtHttpUserAgent();
                if (boolSetting) {
                    network = null;
                } else {
                    network = FtHttpIncomingMessage.this.mNetwork;
                }
                String str2 = str;
                DownloadFileTask.DownloadRequest downloadRequest = r2;
                DownloadFileTask.DownloadRequest downloadRequest2 = new DownloadFileTask.DownloadRequest(str2, j, j2, (String) null, uri, ftHttpUserAgent, network, FtHttpIncomingMessage.this.mConfig.isFtHttpTrustAllCerts(), FtHttpIncomingMessage.this.mConfig.getFtHttpDLUrl(), FtHttpIncomingMessage.this.getParamsforDl(str2), new DownloadFileTask.DownloadTaskCallback() {
                    public void onProgressUpdate(long j) {
                        FtHttpStateMachine ftHttpStateMachine = FtHttpStateMachine.this;
                        FtHttpIncomingMessage ftHttpIncomingMessage = FtHttpIncomingMessage.this;
                        if (j > ftHttpIncomingMessage.mFileSize + ftHttpIncomingMessage.FT_SIZE_MARGIN) {
                            ftHttpStateMachine.sendMessage(ftHttpStateMachine.obtainMessage(8, (Object) CancelReason.INVALID_FT_FILE_SIZE));
                        } else {
                            ftHttpStateMachine.sendMessage(101, (Object) Long.valueOf(j));
                        }
                    }

                    public void onCompleted(long j) {
                        FtHttpStateMachine ftHttpStateMachine = FtHttpStateMachine.this;
                        FtHttpIncomingMessage ftHttpIncomingMessage = FtHttpIncomingMessage.this;
                        long j2 = ftHttpIncomingMessage.mFileSize;
                        long j3 = ftHttpIncomingMessage.FT_SIZE_MARGIN;
                        if (j < j2 - j3 || j > j2 + j3) {
                            ftHttpStateMachine.sendMessage(ftHttpStateMachine.obtainMessage(8, (Object) CancelReason.INVALID_FT_FILE_SIZE));
                            return;
                        }
                        ftHttpStateMachine.sendMessage(102, (Object) Long.valueOf(j));
                        FtHttpIncomingMessage.this.listToDumpFormat(LogClass.FT_HTTP_DOWNLOAD_COMPLETE, 0, new ArrayList());
                    }

                    public void onCanceled(CancelReason cancelReason, int i, int i2) {
                        FtHttpStateMachine ftHttpStateMachine = FtHttpStateMachine.this;
                        ftHttpStateMachine.sendMessage(ftHttpStateMachine.obtainMessage(103, i, i2, cancelReason));
                        if (i2 != -1) {
                            ArrayList arrayList = new ArrayList();
                            arrayList.add(String.valueOf(i2));
                            arrayList.add(String.valueOf(FtHttpIncomingMessage.this.getRetryCount()));
                            FtHttpIncomingMessage.this.listToDumpFormat(LogClass.FT_HTTP_DOWNLOAD_CANCEL, 0, arrayList);
                        }
                    }
                });
                if (downloadRequest.isValid()) {
                    DownloadFileTask downloadFileTask = new DownloadFileTask(FtHttpIncomingMessage.this.mConfig.getPhoneId(), FtHttpIncomingMessage.this.getContext(), FtHttpStateMachine.this.getHandler().getLooper(), downloadRequest);
                    this.downloadTask = downloadFileTask;
                    downloadFileTask.execute(AsyncFileTask.THREAD_POOL_EXECUTOR);
                    return;
                }
                Log.e(FtHttpIncomingMessage.LOG_TAG, "Download request param not valid");
                FtHttpStateMachine ftHttpStateMachine = FtHttpStateMachine.this;
                ftHttpStateMachine.transitionTo(ftHttpStateMachine.mCanceledState);
            }
        }

        private final class CanceledState extends State {
            private CanceledState() {
            }

            public void enter() {
                String r0 = FtHttpIncomingMessage.LOG_TAG;
                Log.i(r0, getName() + " enter msgId : " + FtHttpIncomingMessage.this.mId);
                FtHttpIncomingMessage.this.updateState();
                FtHttpIncomingMessage ftHttpIncomingMessage = FtHttpIncomingMessage.this;
                if (ftHttpIncomingMessage.mIsNetworkRequested) {
                    ftHttpIncomingMessage.releaseNetworkAcquiredForFT();
                }
                FtHttpIncomingMessage ftHttpIncomingMessage2 = FtHttpIncomingMessage.this;
                if (ftHttpIncomingMessage2.mIsBootup) {
                    Log.i(FtHttpIncomingMessage.LOG_TAG, "Message is loaded from bootup, no need for notifications");
                    FtHttpIncomingMessage.this.mIsBootup = false;
                    return;
                }
                IMnoStrategy rcsStrategy = ftHttpIncomingMessage2.getRcsStrategy();
                FtHttpIncomingMessage ftHttpIncomingMessage3 = FtHttpIncomingMessage.this;
                ftHttpIncomingMessage2.mResumableOptionCode = rcsStrategy.getftResumableOption(ftHttpIncomingMessage3.mCancelReason, ftHttpIncomingMessage3.mIsGroupChat, ftHttpIncomingMessage3.mDirection, ftHttpIncomingMessage3.getTransferMech()).getId();
                String r02 = FtHttpIncomingMessage.LOG_TAG;
                Log.i(r02, "mResumableOptionCode : " + FtHttpIncomingMessage.this.mResumableOptionCode);
                FtHttpIncomingMessage.this.updateStatus(ImConstants.Status.FAILED);
                FtHttpIncomingMessage ftHttpIncomingMessage4 = FtHttpIncomingMessage.this;
                ftHttpIncomingMessage4.mListener.onTransferCanceled(ftHttpIncomingMessage4);
                FtHttpIncomingMessage.this.releaseWakeLock();
            }

            public boolean processMessage(Message message) {
                int i = message.what;
                if (i != 8) {
                    if (i != 10) {
                        return false;
                    }
                    FtHttpStateMachine ftHttpStateMachine = FtHttpStateMachine.this;
                    ftHttpStateMachine.transitionTo(ftHttpStateMachine.mInProgressState);
                } else if (message.obj != CancelReason.INVALID_FT_FILE_SIZE) {
                    FtHttpIncomingMessage ftHttpIncomingMessage = FtHttpIncomingMessage.this;
                    ftHttpIncomingMessage.mListener.onCancelRequestFailed(ftHttpIncomingMessage);
                }
                return true;
            }
        }

        private final class CompletedState extends State {
            private CompletedState() {
            }

            public void enter() {
                String r0 = FtHttpIncomingMessage.LOG_TAG;
                Log.i(r0, getName() + " enter msgId : " + FtHttpIncomingMessage.this.mId);
                FtHttpIncomingMessage.this.updateState();
                FtHttpIncomingMessage ftHttpIncomingMessage = FtHttpIncomingMessage.this;
                if (ftHttpIncomingMessage.mIsNetworkRequested) {
                    ftHttpIncomingMessage.releaseNetworkAcquiredForFT();
                }
                FtHttpIncomingMessage ftHttpIncomingMessage2 = FtHttpIncomingMessage.this;
                if (ftHttpIncomingMessage2.mIsBootup) {
                    Log.i(FtHttpIncomingMessage.LOG_TAG, "Message is loaded from bootup, no need for notifications");
                    FtHttpIncomingMessage.this.mIsBootup = false;
                    return;
                }
                ftHttpIncomingMessage2.mListener.onTransferCompleted(ftHttpIncomingMessage2);
                FtHttpIncomingMessage.this.releaseWakeLock();
            }

            public boolean processMessage(Message message) {
                if (message.what != 8) {
                    return false;
                }
                if (message.obj != CancelReason.INVALID_FT_FILE_SIZE) {
                    FtHttpIncomingMessage ftHttpIncomingMessage = FtHttpIncomingMessage.this;
                    ftHttpIncomingMessage.mListener.onCancelRequestFailed(ftHttpIncomingMessage);
                }
                return true;
            }
        }
    }
}
