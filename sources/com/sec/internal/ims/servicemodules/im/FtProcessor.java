package com.sec.internal.ims.servicemodules.im;

import android.content.Context;
import android.media.MediaScannerConnection;
import android.net.Uri;
import android.os.Handler;
import android.os.Message;
import android.os.RemoteCallbackList;
import android.os.RemoteException;
import android.text.TextUtils;
import android.util.Log;
import com.sec.ims.ImsRegistration;
import com.sec.ims.ft.IImsOngoingFtEventListener;
import com.sec.ims.options.Capabilities;
import com.sec.ims.options.CapabilityRefreshType;
import com.sec.ims.util.ImsUri;
import com.sec.ims.util.SipError;
import com.sec.internal.constants.ims.MIMEContentType;
import com.sec.internal.constants.ims.SipMsg;
import com.sec.internal.constants.ims.servicemodules.im.ChatData;
import com.sec.internal.constants.ims.servicemodules.im.ChatMode;
import com.sec.internal.constants.ims.servicemodules.im.FileDisposition;
import com.sec.internal.constants.ims.servicemodules.im.ImConstants;
import com.sec.internal.constants.ims.servicemodules.im.ImDirection;
import com.sec.internal.constants.ims.servicemodules.im.ImError;
import com.sec.internal.constants.ims.servicemodules.im.ImSettings;
import com.sec.internal.constants.ims.servicemodules.im.NotificationStatus;
import com.sec.internal.constants.ims.servicemodules.im.RoutingType;
import com.sec.internal.constants.ims.servicemodules.im.event.FtIncomingSessionEvent;
import com.sec.internal.constants.ims.servicemodules.im.event.FtTransferProgressEvent;
import com.sec.internal.constants.ims.servicemodules.im.params.AcceptSlmParams;
import com.sec.internal.constants.ims.servicemodules.im.params.RejectFtSessionParams;
import com.sec.internal.constants.ims.servicemodules.im.params.RejectSlmParams;
import com.sec.internal.constants.ims.servicemodules.im.reason.CancelReason;
import com.sec.internal.constants.ims.servicemodules.im.reason.FtRejectReason;
import com.sec.internal.constants.ims.servicemodules.im.reason.ImSessionRejectReason;
import com.sec.internal.constants.ims.servicemodules.im.result.FtResult;
import com.sec.internal.constants.ims.servicemodules.im.result.Result;
import com.sec.internal.constants.ims.servicemodules.volte2.CallConstants;
import com.sec.internal.helper.AsyncResult;
import com.sec.internal.helper.BlockedNumberUtil;
import com.sec.internal.helper.CollectionUtils;
import com.sec.internal.helper.FilePathGenerator;
import com.sec.internal.helper.FileUtils;
import com.sec.internal.helper.SimUtil;
import com.sec.internal.helper.os.ImsGateConfig;
import com.sec.internal.ims.rcs.util.RcsUtils;
import com.sec.internal.ims.registry.ImsRegistry;
import com.sec.internal.ims.servicemodules.im.listener.FtMessageListener;
import com.sec.internal.ims.servicemodules.im.listener.IFtEventListener;
import com.sec.internal.ims.servicemodules.im.strategy.IMnoStrategy;
import com.sec.internal.ims.servicemodules.im.util.ImCpimNamespacesHelper;
import com.sec.internal.ims.settings.RcsPolicySettings;
import com.sec.internal.ims.util.RcsSettingsUtils;
import com.sec.internal.ims.util.ThumbnailTool;
import com.sec.internal.interfaces.ims.core.IRegistrationGovernor;
import com.sec.internal.interfaces.ims.servicemodules.im.IImServiceInterface;
import com.sec.internal.interfaces.ims.servicemodules.im.ISlmServiceInterface;
import com.sec.internal.interfaces.ims.servicemodules.options.ICapabilityDiscoveryModule;
import com.sec.internal.log.IMSLog;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.Future;
import java.util.concurrent.FutureTask;

public class FtProcessor extends Handler implements FtMessageListener {
    private static final int EVENT_REJECT_FT_RESUME_INVITE = 1;
    private static final String LOG_TAG = FtProcessor.class.getSimpleName();
    private ImCache mCache;
    private Context mContext;
    private final CollectionUtils.ArrayListMultimap<ImConstants.Type, IFtEventListener> mFtEventListeners = CollectionUtils.createArrayListMultimap();
    private ImModule mImModule;
    private final IImServiceInterface mImService;
    private ImSessionProcessor mImSessionProcessor;
    private ImTranslation mImTranslation;
    private final Map<Integer, RemoteCallbackList<IImsOngoingFtEventListener>> mImsFtListenerList = new HashMap();
    private RcsSettingsUtils mRcsSettingsUtils;
    private final ISlmServiceInterface mSlmService;
    private final ThumbnailTool mThumbnailTool;

    public FtProcessor(Context context, IImServiceInterface iImServiceInterface, ISlmServiceInterface iSlmServiceInterface, ImModule imModule, ImCache imCache) {
        super(imModule.getLooper());
        this.mContext = context;
        this.mImService = iImServiceInterface;
        this.mImModule = imModule;
        this.mCache = imCache;
        this.mSlmService = iSlmServiceInterface;
        this.mThumbnailTool = new ThumbnailTool(context, imModule.getLooper());
        this.mRcsSettingsUtils = RcsSettingsUtils.getInstance(this.mContext);
    }

    /* access modifiers changed from: protected */
    public void init(ImSessionProcessor imSessionProcessor, ImTranslation imTranslation) {
        this.mImSessionProcessor = imSessionProcessor;
        this.mImTranslation = imTranslation;
    }

    /* access modifiers changed from: protected */
    public void registerFtEventListener(ImConstants.Type type, IFtEventListener iFtEventListener) {
        this.mFtEventListeners.put(type, iFtEventListener);
    }

    public void handleMessage(Message message) {
        AsyncResult asyncResult;
        super.handleMessage(message);
        if (message.what == 1 && (asyncResult = (AsyncResult) message.obj) != null && ((FtResult) asyncResult.result).getImError() != ImError.SUCCESS) {
            Log.e(LOG_TAG, "CancelingState: Failed to reject transfer.");
        }
    }

    /* access modifiers changed from: protected */
    public void registerImsOngoingFtListenerByPhoneId(IImsOngoingFtEventListener iImsOngoingFtEventListener, int i) {
        String str = LOG_TAG;
        Log.i(str, "registerImsOngoingFtListener phoneId= " + i);
        if (!this.mImsFtListenerList.containsKey(Integer.valueOf(i))) {
            this.mImsFtListenerList.put(Integer.valueOf(i), new RemoteCallbackList());
        }
        synchronized (this.mImsFtListenerList) {
            RemoteCallbackList remoteCallbackList = this.mImsFtListenerList.get(Integer.valueOf(i));
            if (iImsOngoingFtEventListener != null) {
                remoteCallbackList.register(iImsOngoingFtEventListener);
                notifyOngoingFtEvent(this.mCache.hasProcessingFileTransfer(), i);
                return;
            }
            Log.e(str, "no registerImsOngoingFtListener and not work");
        }
    }

    /* access modifiers changed from: protected */
    public void unregisterImsOngoingListenerByPhoneId(IImsOngoingFtEventListener iImsOngoingFtEventListener, int i) {
        String str = LOG_TAG;
        Log.i(str, "unregisterImsOngoingListener phoneId= " + i);
        if (this.mImsFtListenerList.containsKey(Integer.valueOf(i))) {
            synchronized (this.mImsFtListenerList) {
                RemoteCallbackList remoteCallbackList = this.mImsFtListenerList.get(Integer.valueOf(i));
                if (iImsOngoingFtEventListener != null) {
                    remoteCallbackList.unregister(iImsOngoingFtEventListener);
                }
            }
        }
    }

    public void onTransferCreated(FtMessage ftMessage) {
        String str = LOG_TAG;
        Log.i(str, "onTransferCreated: " + ftMessage);
        onNotifyCloudMsgFtEvent(ftMessage);
        if (this.mImSessionProcessor.isReportMsg(ftMessage)) {
            ImSession imSession = this.mCache.getImSession(ftMessage.getChatId());
            if (imSession != null) {
                imSession.sendFile(ftMessage);
                return;
            }
            return;
        }
        for (IFtEventListener next : this.mFtEventListeners.get(ftMessage.getType())) {
            next.onFileTransferCreated(ftMessage);
            next.onFileTransferAttached(ftMessage);
        }
    }

    public void onTransferReceived(FtMessage ftMessage) {
        String str = LOG_TAG;
        Log.i(str, "onFileTransferReceived: " + ftMessage);
        for (IFtEventListener onFileTransferReceived : this.mFtEventListeners.get(ftMessage.getType())) {
            onFileTransferReceived.onFileTransferReceived(ftMessage);
        }
    }

    public void onTransferProgressReceived(FtMessage ftMessage) {
        String str = LOG_TAG;
        Log.i(str, "onTransferProgressReceived: " + ftMessage.getId() + " " + ftMessage.getTransferredBytes() + "/" + ftMessage.getFileSize());
        if (!this.mImSessionProcessor.isReportMsg(ftMessage)) {
            for (IFtEventListener onTransferProgressReceived : this.mFtEventListeners.get(ftMessage.getType())) {
                onTransferProgressReceived.onTransferProgressReceived(ftMessage);
            }
        }
    }

    public void onTransferCompleted(FtMessage ftMessage) {
        String contentType;
        if (!this.mImSessionProcessor.isReportMsg(ftMessage)) {
            IMnoStrategy rcsStrategy = this.mImModule.getRcsStrategy(this.mImModule.getPhoneIdByIMSI(ftMessage.getOwnIMSI()));
            if (rcsStrategy != null && rcsStrategy.boolSetting(RcsPolicySettings.RcsPolicy.DISPLAY_FT_IN_GALLERY) && ftMessage.isIncoming() && (contentType = ftMessage.getContentType()) != null && (contentType.contains(SipMsg.FEATURE_TAG_MMTEL_VIDEO) || contentType.contains(CallConstants.ComposerData.IMAGE))) {
                String str = LOG_TAG;
                Log.i(str, "update gallery app: " + contentType);
                MediaScannerConnection.scanFile(this.mContext, new String[]{ftMessage.getFilePath()}, (String[]) null, (MediaScannerConnection.OnScanCompletedListener) null);
            }
            this.mImModule.setCountReconfiguration(0);
            this.mImModule.removeReconfigurationEvent();
            for (IFtEventListener onTransferCompleted : this.mFtEventListeners.get(ftMessage.getType())) {
                onTransferCompleted.onTransferCompleted(ftMessage);
            }
            if (!(ftMessage instanceof FtHttpOutgoingMessage)) {
                ImSession imSession = this.mCache.getImSession(ftMessage.getChatId());
                this.mCache.removeFromPendingList(ftMessage.getId());
                if (imSession != null && !this.mCache.hasFileTransferInprogress()) {
                    this.mImSessionProcessor.notifyImSessionClosed(this.mImModule.getPhoneIdByIMSI(imSession.getOwnImsi()));
                }
            }
        } else if (ftMessage.getReportMsgParams() != null) {
            this.mCache.deleteMessage(ftMessage.getId());
            this.mImTranslation.onMessageReportResponse(ftMessage.getReportMsgParams().getSpamMsgImdnId(), ftMessage.getChatId(), true);
        }
    }

    public void onTransferCanceled(FtMessage ftMessage) {
        if (!this.mImSessionProcessor.isReportMsg(ftMessage)) {
            if (ImsGateConfig.isGateEnabled()) {
                IMSLog.g("GATE", "<GATE-M>MMS_ERROR</GATE-M>");
            }
            if (ftMessage.getLastNotificationType() != NotificationStatus.CANCELED) {
                for (IFtEventListener onTransferCanceled : this.mFtEventListeners.get(ftMessage.getType())) {
                    onTransferCanceled.onTransferCanceled(ftMessage);
                }
            }
            ImSession imSession = this.mCache.getImSession(ftMessage.getChatId());
            if (imSession == null) {
                Log.e(LOG_TAG, "onTransferCanceled: session not found in the cache.");
            } else if (!this.mCache.hasFileTransferInprogress()) {
                this.mImSessionProcessor.notifyImSessionClosed(this.mImModule.getPhoneIdByIMSI(imSession.getOwnImsi()));
            }
        } else if (ftMessage.getReportMsgParams() != null) {
            this.mCache.deleteMessage(ftMessage.getId());
            this.mImTranslation.onMessageReportResponse(ftMessage.getReportMsgParams().getSpamMsgImdnId(), ftMessage.getChatId(), false);
        }
    }

    public void onTransferInProgress(FtMessage ftMessage) {
        for (IFtEventListener onTransferStarted : this.mFtEventListeners.get(ftMessage.getType())) {
            onTransferStarted.onTransferStarted(ftMessage);
        }
        ImSession imSession = this.mCache.getImSession(ftMessage.getChatId());
        if (imSession != null) {
            this.mImSessionProcessor.notifyImSessionEstablished(this.mImModule.getPhoneIdByIMSI(imSession.getOwnImsi()));
        }
    }

    public void onAutoResumeTransfer(FtMessage ftMessage) {
        post(new FtProcessor$$ExternalSyntheticLambda0(this, ftMessage));
    }

    /* access modifiers changed from: private */
    public /* synthetic */ void lambda$onAutoResumeTransfer$0(FtMessage ftMessage) {
        String str = LOG_TAG;
        Log.i(str, "onAutoResumeTransfer: messageId =" + ftMessage.getId());
        ImSession imSession = this.mCache.getImSession(ftMessage.getChatId());
        if (imSession == null) {
            Log.e(str, "onAutoResumeTransfer: session not found in the cache.");
            return;
        }
        if (!this.mImModule.isRegistered(this.mImModule.getPhoneIdByIMSI(imSession.getOwnImsi()))) {
            Log.e(str, "onAutoResumeTransfer: not registered");
            return;
        }
        if (imSession.isGroupChat() && !imSession.isBroadcastMsg(ftMessage)) {
            imSession.startSession();
        }
        this.mCache.addToPendingList(ftMessage);
        imSession.resumeTransferFile(ftMessage);
    }

    public Integer onRequestRegistrationType() {
        return this.mImModule.onRequestRegistrationType();
    }

    public String onRequestIncomingFtTransferPath() {
        return FilePathGenerator.getFileDownloadPath(this.mContext, false);
    }

    public void onFileResizingNeeded(FtMessage ftMessage, long j) {
        for (IFtEventListener onFileResizingNeeded : this.mFtEventListeners.get(ftMessage.getType())) {
            onFileResizingNeeded.onFileResizingNeeded(ftMessage, j);
        }
    }

    public void onCancelRequestFailed(FtMessage ftMessage) {
        for (IFtEventListener onCancelRequestFailed : this.mFtEventListeners.get(ftMessage.getType())) {
            onCancelRequestFailed.onCancelRequestFailed(ftMessage);
        }
    }

    public void onSendDeliveredNotification(FtMessage ftMessage) {
        post(new FtProcessor$$ExternalSyntheticLambda1(this, ftMessage));
    }

    /* access modifiers changed from: private */
    public /* synthetic */ void lambda$onSendDeliveredNotification$1(FtMessage ftMessage) {
        String str = LOG_TAG;
        Log.i(str, "onSendDeliveredNotification: msgId=" + ftMessage.getId());
        ImSession imSession = this.mCache.getImSession(ftMessage.getChatId());
        if (imSession != null) {
            ImDump imDump = this.mImModule.getImDump();
            imDump.addEventLogs("sendDeliveredNotification: chatId=" + imSession.getChatId() + ", convId=" + imSession.getConversationId() + ", contId=" + imSession.getContributionId() + ", imdnId" + ftMessage.getImdnId());
            imSession.sendDeliveredNotification(ftMessage);
            return;
        }
        Log.e(str, "session not found in the cache.");
    }

    public void onFtErrorReport(ImError imError) {
        IRegistrationGovernor registrationGovernor;
        String str = LOG_TAG;
        Log.i(str, "onFtErrorReport");
        ImsRegistration imsRegistration = this.mImModule.getImsRegistration();
        if (imsRegistration != null && (registrationGovernor = ImsRegistry.getRegistrationManager().getRegistrationGovernor(imsRegistration.getHandle())) != null && imError == ImError.FORBIDDEN_NO_WARNING_HEADER) {
            Log.i(str, "onFtErrorReport : 403 forbidden w/o warning header");
            registrationGovernor.onSipError("ft", new SipError(403, "Forbidden"));
        }
    }

    public void onMessageSendingSucceeded(MessageBase messageBase) {
        this.mImSessionProcessor.onMessageSendingSucceeded(messageBase);
    }

    public void onMessageSendingFailed(MessageBase messageBase, IMnoStrategy.StrategyResponse strategyResponse, Result result) {
        this.mImSessionProcessor.onMessageSendingFailed(messageBase, strategyResponse, result);
    }

    public ChatData.ChatType onRequestChatType(String str) {
        ImSession imSession = this.mImSessionProcessor.getImSession(str);
        if (imSession != null) {
            return imSession.getChatType();
        }
        return null;
    }

    public Message onRequestCompleteCallback(String str) {
        ImSession imSession = this.mCache.getImSession(str);
        if (imSession != null) {
            return imSession.getFtCompleteCallback();
        }
        return null;
    }

    public Set<ImsUri> onRequestParticipantUris(String str) {
        ImSession imSession = this.mCache.getImSession(str);
        if (imSession != null) {
            return imSession.getParticipantsUri();
        }
        return new HashSet();
    }

    public void onImdnNotificationReceived(FtMessage ftMessage, ImsUri imsUri, NotificationStatus notificationStatus, boolean z) {
        for (IFtEventListener onImdnNotificationReceived : this.mFtEventListeners.get(ftMessage.getType())) {
            onImdnNotificationReceived.onImdnNotificationReceived(ftMessage, imsUri, notificationStatus, z);
        }
    }

    /* access modifiers changed from: protected */
    public ThumbnailTool getThumbnailTool() {
        return this.mThumbnailTool;
    }

    /* access modifiers changed from: protected */
    public void acceptFileTransfer(String str, ImDirection imDirection, String str2, Uri uri) {
        post(new FtProcessor$$ExternalSyntheticLambda2(this, str, imDirection, str2, uri));
    }

    /* access modifiers changed from: private */
    public /* synthetic */ void lambda$acceptFileTransfer$2(String str, ImDirection imDirection, String str2, Uri uri) {
        String str3 = LOG_TAG;
        Log.i(str3, "acceptFileTransfer: messageId=" + str);
        FtMessage ftMessage = this.mCache.getFtMessage(str, imDirection, str2);
        if (ftMessage == null) {
            Log.e(str3, "FT not found in the cache.");
            return;
        }
        if (!this.mImModule.isRegistered(this.mImModule.getPhoneIdByIMSI(ftMessage.getOwnIMSI()))) {
            Log.i(str3, "acceptFileTransfer: not registered");
            if (this.mCache.getImSession(ftMessage.getChatId()) == null) {
                Log.e(str3, "acceptFileTransfer: No session");
            } else {
                notifyRegistrationError(ftMessage);
            }
        } else {
            ftMessage.acceptTransfer(uri);
        }
    }

    /* access modifiers changed from: protected */
    public Future<FtMessage> attachFileToSingleChat(int i, String str, Uri uri, ImsUri imsUri, Set<NotificationStatus> set, String str2, String str3, boolean z, boolean z2, boolean z3, boolean z4, String str4, FileDisposition fileDisposition, boolean z5, boolean z6) {
        FtProcessor$$ExternalSyntheticLambda10 ftProcessor$$ExternalSyntheticLambda10 = r0;
        FtProcessor$$ExternalSyntheticLambda10 ftProcessor$$ExternalSyntheticLambda102 = new FtProcessor$$ExternalSyntheticLambda10(this, i, str, uri, imsUri, set, str2, str3, z, z2, z3, z4, fileDisposition, z5, z6, str4);
        FutureTask futureTask = new FutureTask(ftProcessor$$ExternalSyntheticLambda10);
        post(futureTask);
        return futureTask;
    }

    /* access modifiers changed from: private */
    public /* synthetic */ FtMessage lambda$attachFileToSingleChat$3(int i, String str, Uri uri, ImsUri imsUri, Set set, String str2, String str3, boolean z, boolean z2, boolean z3, boolean z4, FileDisposition fileDisposition, boolean z5, boolean z6, String str4) throws Exception {
        ImSession imSession;
        FtMessage ftMessage;
        int i2 = i;
        ImsUri imsUri2 = imsUri;
        Set set2 = set;
        String str5 = str3;
        boolean z7 = z4;
        boolean z8 = z5;
        boolean z9 = z6;
        String str6 = LOG_TAG;
        IMSLog.i(str6, i2, "attachFileToSingleChat: fileName=" + IMSLog.checker(str) + " contentUri=" + IMSLog.checker(uri) + " contactUri=" + IMSLog.numberChecker(imsUri) + " disp=" + set2 + " requestMessageId=" + str2 + " contentType=" + str5 + " isprotectedAccountMsg=" + z + " isResizable=" + z2 + " isExtraft=" + z3 + " isFtSms=" + z7 + " fileDisposition=" + fileDisposition + " isTokenUsed=" + z8 + " isTokenLink=" + z9);
        HashSet hashSet = new HashSet();
        hashSet.add(this.mImModule.normalizeUri(i2, imsUri2));
        ChatMode chatMode = ChatMode.OFF;
        if (z8 && !z9) {
            chatMode = ChatMode.ON;
        }
        ImCache imCache = this.mCache;
        ChatData.ChatType chatType = ChatData.ChatType.ONE_TO_ONE_CHAT;
        ImSession imSessionByParticipants = imCache.getImSessionByParticipants(hashSet, chatType, this.mImModule.getImsiFromPhoneId(i2), chatMode);
        if (imSessionByParticipants == null) {
            ArrayList arrayList = new ArrayList();
            arrayList.add(imsUri2);
            imSessionByParticipants = this.mCache.makeNewEmptySession(this.mImModule.getImsiFromPhoneId(i2), this.mImModule.normalizeUri(i2, (Collection<ImsUri>) arrayList), chatType, ImDirection.OUTGOING, chatMode);
            IMSLog.i(str6, "session not found, new session created: " + imSessionByParticipants);
        }
        ImSession imSession2 = imSessionByParticipants;
        IMnoStrategy rcsStrategy = this.mImModule.getRcsStrategy(i2);
        boolean z10 = false;
        boolean z11 = rcsStrategy.isFTViaHttp(this.mImModule.getImConfig(i2), imSession2.getParticipantsUri(), imSession2.getChatType()) || z7;
        if ((z11 || rcsStrategy.isFtHttpOnlySupported(false)) && !MIMEContentType.LOCATION_PUSH.equals(str5)) {
            ImCache imCache2 = this.mCache;
            String imsiFromPhoneId = this.mImModule.getImsiFromPhoneId(i2);
            ImModule imModule = this.mImModule;
            imSession = imSession2;
            String str7 = str6;
            ftMessage = imCache2.makeNewOutgoingFtHttpMessage(imsiFromPhoneId, imSession2, str, uri, imsUri, set, str2, str3, z3, imModule.getNetwork(imModule.getImConfig(i2).isFtHttpOverDefaultPdn(), i2), z4, false, false, fileDisposition, z2);
            if (!z11) {
                Log.e(str7, "attachFileToSingleChat: isFTViaHttp is false");
                ftMessage.cancelTransfer(CancelReason.REMOTE_TEMPORARILY_UNAVAILABLE);
                return null;
            }
            str6 = str7;
        } else {
            imSession = imSession2;
            HashSet hashSet2 = new HashSet();
            if (this.mImModule.getImConfig(i2).getImMsgTech() == ImConstants.ImMsgTech.CPM) {
                z10 = true;
            }
            boolean isFtStAndFwEnabled = this.mImModule.getImConfig(i2).isFtStAndFwEnabled();
            ICapabilityDiscoveryModule capabilityDiscoveryModule = ImsRegistry.getServiceModuleManager().getCapabilityDiscoveryModule();
            Capabilities capabilities = capabilityDiscoveryModule != null ? capabilityDiscoveryModule.getCapabilities(imsUri2, CapabilityRefreshType.DISABLED, i2) : null;
            if (z10) {
                hashSet2.addAll(set2);
            } else {
                if (capabilities != null && isFtStAndFwEnabled && capabilities.hasFeature(Capabilities.FEATURE_FT_STORE)) {
                    hashSet2.add(NotificationStatus.DELIVERED);
                } else if (MIMEContentType.LOCATION_PUSH.equals(str5)) {
                    hashSet2.addAll(set2);
                } else {
                    hashSet2.add(NotificationStatus.NONE);
                }
                Log.i(str6, "IMDN modified: [" + set2 + "] to [" + hashSet2 + "]");
            }
            ftMessage = this.mCache.makeNewOutgoingFtMessage(imSession.getOwnImsi(), imSession, str, uri, imsUri, hashSet2, str2, str3, z, z2, false, str4);
            if (TextUtils.isEmpty(ftMessage.getFilePath())) {
                Log.e(str6, "attachFileToSingleChat: File copy failed");
                ftMessage.cancelTransfer(CancelReason.ERROR);
                return null;
            }
        }
        IMSLog.s(str6, "attachFileToSingleChat: Make new outgoing ft " + ftMessage);
        if (!this.mImModule.isRegistered(i2)) {
            Log.e(str6, "attachFileToSingleChat: not registered");
            notifyRegistrationError(ftMessage);
            return null;
        } else if (this.mImModule.mKnoxBlockState == 1 && BlockedNumberUtil.isKnoxBlockedNumber(imSession.getRemoteUri().getMsisdn(), ImDirection.OUTGOING)) {
            ftMessage.cancelTransfer(CancelReason.ERROR);
            return null;
        } else if ("UNSUPPORTED TYPE".equalsIgnoreCase(ftMessage.getContentType())) {
            ftMessage.cancelTransfer(CancelReason.ERROR);
            return null;
        } else if (!FileUtils.exists(this.mContext, uri)) {
            Log.e(str6, "attachFileToSingleChat: No files found");
            ftMessage.cancelTransfer(CancelReason.ERROR);
            return null;
        } else {
            ImSession imSession3 = imSession;
            this.mCache.updateActiveSession(imSession3);
            imSession3.attachFile(ftMessage);
            return ftMessage;
        }
    }

    /* access modifiers changed from: protected */
    public Future<FtMessage> attachFileToGroupChat(String str, String str2, Uri uri, Set<NotificationStatus> set, String str3, String str4, boolean z, boolean z2, boolean z3, boolean z4, String str5, FileDisposition fileDisposition) {
        FutureTask futureTask = new FutureTask(new FtProcessor$$ExternalSyntheticLambda7(this, str, str2, uri, set, str3, z4, str4, z, z2, fileDisposition, z3, str5));
        post(futureTask);
        return futureTask;
    }

    /* access modifiers changed from: private */
    public /* synthetic */ FtMessage lambda$attachFileToGroupChat$4(String str, String str2, Uri uri, Set set, String str3, boolean z, String str4, boolean z2, boolean z3, FileDisposition fileDisposition, boolean z4, String str5) throws Exception {
        ImSession imSession;
        String str6;
        FtMessage ftMessage;
        String str7 = str;
        boolean z5 = z3;
        String str8 = LOG_TAG;
        Log.i(str8, "attachFileToGroupChat: chatId=" + str7 + ", fileName=" + IMSLog.checker(str2) + ", contentUri=" + IMSLog.checker(uri) + ", disp=" + set + ", requestMessageId=" + str3 + "isFtSms=" + z + ", contentType=" + str4 + ", isResizable=" + z2 + ", isBroadcast=" + z5 + ", fileDisposition=" + fileDisposition);
        ImSession imSession2 = this.mCache.getImSession(str7);
        if (imSession2 == null) {
            Log.e(str8, "attachFileToGroupChat: chat not exist - " + str7);
            return null;
        }
        int phoneIdByIMSI = this.mImModule.getPhoneIdByIMSI(imSession2.getOwnImsi());
        IMnoStrategy rcsStrategy = this.mImModule.getRcsStrategy(phoneIdByIMSI);
        boolean isFTViaHttp = rcsStrategy.isFTViaHttp(this.mImModule.getImConfig(phoneIdByIMSI), imSession2.getParticipantsUri(), imSession2.getChatType());
        if (isFTViaHttp || rcsStrategy.isFtHttpOnlySupported(true)) {
            int i = phoneIdByIMSI;
            String str9 = "attachFileToGroupChat: not registered";
            ImSession imSession3 = imSession2;
            int i2 = i;
            boolean z6 = this.mImModule.isRegistered(i2) && this.mImModule.isServiceRegistered(i2, "slm") && (!this.mImModule.getImConfig(i2).getChatEnabled() || z5) && this.mImModule.getImConfig(i2).getSlmAuth() == ImConstants.SlmAuth.ENABLED && ((!imSession3.isGroupChat() || z5 || imSession3.getChatType() == ChatData.ChatType.ONE_TO_MANY_CHAT) && !this.mImModule.isServiceRegistered(i2, "ft_http"));
            ImCache imCache = this.mCache;
            String imsiFromPhoneId = this.mImModule.getImsiFromPhoneId(i2);
            ImsUri sessionUri = imSession3.getSessionUri();
            ImModule imModule = this.mImModule;
            imSession = imSession3;
            String str10 = str9;
            int i3 = i2;
            String str11 = str8;
            ftMessage = imCache.makeNewOutgoingFtHttpMessage(imsiFromPhoneId, imSession3, str2, uri, sessionUri, set, str3, str4, z4, imModule.getNetwork(imModule.getImConfig(i2).isFtHttpOverDefaultPdn(), i2), z, z3, z6, fileDisposition, z2);
            if (!this.mImModule.isRegistered(i3)) {
                IMSLog.i(str11, str10);
                notifyRegistrationError(ftMessage);
                return null;
            }
            str6 = str11;
            if (!isFTViaHttp) {
                Log.e(str6, "attachFileToGroupChat: FT MSRP is not supported");
                ftMessage.cancelTransfer(imSession.getChatType() == ChatData.ChatType.PARTICIPANT_BASED_GROUP_CHAT ? CancelReason.REMOTE_TEMPORARILY_UNAVAILABLE : CancelReason.ERROR);
                return null;
            }
        } else {
            String str12 = "attachFileToGroupChat: not registered";
            int i4 = phoneIdByIMSI;
            ImSession imSession4 = imSession2;
            ftMessage = this.mCache.makeNewOutgoingFtMessage(this.mImModule.getImsiFromPhoneId(phoneIdByIMSI), imSession2, str2, uri, imSession2.getSessionUri(), set, str3, str4, false, z2, z3, str5);
            IMSLog.s(str8, "attachFileToGroupChat: Make new outgoing ft " + ftMessage);
            if (TextUtils.isEmpty(ftMessage.getFilePath())) {
                Log.e(str8, "attachFileToSingleChat: File copy failed");
                ftMessage.cancelTransfer(CancelReason.ERROR);
                return null;
            }
            int i5 = i4;
            if (!this.mImModule.isRegistered(i5)) {
                IMSLog.i(str8, str12);
                notifyRegistrationError(ftMessage);
                return null;
            }
            IMnoStrategy.StrategyResponse checkCapability = this.mImModule.getRcsStrategy(i5).checkCapability(imSession4.getParticipantsUri(), (long) Capabilities.FEATURE_FT_SERVICE, imSession4.getChatType(), imSession4.isBroadcastMsg(ftMessage));
            if (!imSession4.isBroadcastMsg(ftMessage) && checkCapability.getStatusCode() == IMnoStrategy.StatusCode.NONE) {
                imSession4.startSession();
            }
            imSession = imSession4;
            str6 = str8;
        }
        if (!FileUtils.exists(this.mContext, uri)) {
            Log.e(str6, "attachFileToGroupChat: No files found");
            ftMessage.cancelTransfer(CancelReason.ERROR);
            return null;
        }
        ImSession imSession5 = imSession;
        this.mCache.updateActiveSession(imSession5);
        imSession5.attachFile(ftMessage);
        return ftMessage;
    }

    /* access modifiers changed from: protected */
    public void sendFile(long j) {
        sendFile(this.mCache.getFtMessage((int) j));
    }

    /* access modifiers changed from: protected */
    public void sendFile(String str) {
        sendFile((FtMessage) this.mCache.getMessage(str, ImDirection.OUTGOING, (String) null));
    }

    /* access modifiers changed from: protected */
    public void sendFile(FtMessage ftMessage) {
        post(new FtProcessor$$ExternalSyntheticLambda9(this, ftMessage));
    }

    /* access modifiers changed from: private */
    public /* synthetic */ void lambda$sendFile$5(FtMessage ftMessage) {
        if (ftMessage == null) {
            Log.e(LOG_TAG, "sendFile: Message not found in cache");
            return;
        }
        String str = LOG_TAG;
        Log.i(str, "sendFile: messageId=" + ftMessage.getId());
        ImSession imSession = this.mCache.getImSession(ftMessage.getChatId());
        if (imSession == null) {
            Log.e(str, "sendFile: Session not found in the cache");
            return;
        }
        if (this.mImModule.isRegistered(this.mImModule.getPhoneIdByIMSI(imSession.getOwnImsi())) || !(ftMessage instanceof FtMsrpMessage)) {
            if (imSession.isGroupChat() && !imSession.isBroadcastMsg(ftMessage) && !ftMessage.mIsSlmSvcMsg) {
                imSession.startSession();
            }
            imSession.sendFile(ftMessage);
            return;
        }
        IMSLog.i(str, "sendFile: not registered");
        notifyRegistrationError(ftMessage);
    }

    /* access modifiers changed from: protected */
    public void rejectFileTransfer(String str, ImDirection imDirection, String str2) {
        post(new FtProcessor$$ExternalSyntheticLambda11(this, str, imDirection, str2));
    }

    /* access modifiers changed from: private */
    public /* synthetic */ void lambda$rejectFileTransfer$6(String str, ImDirection imDirection, String str2) {
        String str3 = LOG_TAG;
        Log.i(str3, "rejectFileTransfer: messageId=" + str);
        FtMessage ftMessage = this.mCache.getFtMessage(str, imDirection, str2);
        if (ftMessage == null) {
            Log.e(str3, "FT not found in the cache.");
        } else {
            ftMessage.rejectTransfer();
        }
    }

    /* access modifiers changed from: protected */
    public void resumeSendingTransfer(String str, Uri uri, boolean z) {
        post(new FtProcessor$$ExternalSyntheticLambda4(this, str, z, uri));
    }

    /* access modifiers changed from: private */
    public /* synthetic */ void lambda$resumeSendingTransfer$7(String str, boolean z, Uri uri) {
        String str2 = LOG_TAG;
        Log.i(str2, "resumeSendingTransfer: messageId=" + str);
        FtMessage ftMessage = this.mCache.getFtMessage(str, ImDirection.OUTGOING, (String) null);
        if (ftMessage == null) {
            Log.e(str2, "resumeSendingTransfer: FT not found in the cache.");
            return;
        }
        ftMessage.setIsResizable(z);
        ftMessage.setContentUri(uri);
        ImSession imSession = this.mCache.getImSession(ftMessage.getChatId());
        if (imSession == null) {
            Log.e(str2, "resumeSendingTransfer: FT not found in the cache.");
            return;
        }
        if (this.mImModule.isRegistered(this.mImModule.getPhoneIdByIMSI(imSession.getOwnImsi())) || !(ftMessage instanceof FtMsrpMessage)) {
            if (imSession.isGroupChat() && !imSession.isBroadcastMsg(ftMessage)) {
                imSession.startSession();
            }
            this.mCache.addToPendingList(ftMessage);
            ftMessage.removeAutoResumeFileTimer();
            imSession.resumeTransferFile(ftMessage);
            return;
        }
        IMSLog.i(str2, "resumeSendingTransfer: not registered");
        notifyRegistrationError(ftMessage);
    }

    /* access modifiers changed from: protected */
    public void resumeReceivingTransfer(String str, String str2, Uri uri) {
        post(new FtProcessor$$ExternalSyntheticLambda8(this, str, str2, uri));
    }

    /* access modifiers changed from: private */
    public /* synthetic */ void lambda$resumeReceivingTransfer$8(String str, String str2, Uri uri) {
        String str3 = LOG_TAG;
        Log.i(str3, "resumeReceivingTransfer: messageId=" + str);
        FtMessage ftMessage = this.mCache.getFtMessage(str, ImDirection.INCOMING, str2);
        if (ftMessage == null) {
            Log.e(str3, "resumeReceivingTransfer: FT not found in the cache.");
            return;
        }
        ftMessage.setContentUri(uri);
        ImSession imSession = this.mCache.getImSession(ftMessage.getChatId());
        if (imSession == null) {
            Log.e(str3, "resumeReceivingTransfer: Session not found in the cache.");
            return;
        }
        if (this.mImModule.isRegistered(this.mImModule.getPhoneIdByIMSI(imSession.getOwnImsi())) || !(ftMessage instanceof FtMsrpMessage)) {
            this.mCache.addToPendingList(ftMessage);
            if (ftMessage instanceof FtMsrpMessage) {
                Log.i(str3, "request resuming FT to sender using INVITE");
                ftMessage.removeAutoResumeFileTimer();
                imSession.resumeTransferFile(ftMessage);
                return;
            }
            imSession.receiveTransfer(ftMessage, (FtIncomingSessionEvent) null, true);
            return;
        }
        Log.e(str3, "resumeReceivingTransfer: not registered");
        notifyRegistrationError(ftMessage);
    }

    /* access modifiers changed from: protected */
    public void cancelFileTransfer(String str, ImDirection imDirection, String str2) {
        post(new FtProcessor$$ExternalSyntheticLambda5(this, str, imDirection, str2));
    }

    /* access modifiers changed from: private */
    public /* synthetic */ void lambda$cancelFileTransfer$9(String str, ImDirection imDirection, String str2) {
        String str3 = LOG_TAG;
        Log.i(str3, "cancelFileTransfer: messageId=" + str);
        FtMessage ftMessage = this.mCache.getFtMessage(str, imDirection, str2);
        if (ftMessage == null) {
            Log.e(str3, "FT not found in the cache.");
        } else {
            ftMessage.cancelTransfer(CancelReason.CANCELED_BY_USER);
        }
    }

    /* access modifiers changed from: protected */
    public void setAutoAcceptFt(int i, int i2) {
        post(new FtProcessor$$ExternalSyntheticLambda6(this, i, i2));
    }

    /* access modifiers changed from: private */
    public /* synthetic */ void lambda$setAutoAcceptFt$10(int i, int i2) {
        if (!RcsUtils.DualRcs.isDualRcsSettings()) {
            i = SimUtil.getSimSlotPriority();
        }
        Log.i(LOG_TAG, "setAutoAcceptFt: accept=" + i2 + " isRoaming=" + this.mImModule.isDataRoaming(i));
        this.mImModule.getImConfig(i).setFtAutAccept(this.mContext, i2, this.mImModule.isDataRoaming(i));
        boolean z = false;
        boolean z2 = i2 == 1 || i2 == 2;
        if (i2 == 2) {
            z = true;
        }
        RcsSettingsUtils rcsSettingsUtils = this.mRcsSettingsUtils;
        if (rcsSettingsUtils != null) {
            rcsSettingsUtils.writeBoolean(ImSettings.AUTO_ACCEPT_FILE_TRANSFER, z2);
            this.mRcsSettingsUtils.writeBoolean(ImSettings.AUTO_ACCEPT_FT_IN_ROAMING, z);
        }
    }

    /* access modifiers changed from: protected */
    public void onIncomingFileTransferReceived(FtIncomingSessionEvent ftIncomingSessionEvent) {
        FtIncomingSessionEvent ftIncomingSessionEvent2 = ftIncomingSessionEvent;
        String str = LOG_TAG;
        Log.i(str, "onIncomingFileTransferReceived: " + ftIncomingSessionEvent2);
        ImDump imDump = this.mImModule.getImDump();
        imDump.addEventLogs("onIncomingFileTransferReceived: conversationId=" + ftIncomingSessionEvent2.mConversationId + ", imdnId=" + ftIncomingSessionEvent2.mImdnId + ", isSLM=" + ftIncomingSessionEvent2.mIsSlmSvcMsg);
        int phoneIdByIMSI = this.mImModule.getPhoneIdByIMSI(ftIncomingSessionEvent2.mOwnImsi);
        Set<ImsUri> normalizedParticipants = this.mImSessionProcessor.getNormalizedParticipants(phoneIdByIMSI, ftIncomingSessionEvent2.mParticipants, ftIncomingSessionEvent2.mSenderUri);
        StringBuilder sb = new StringBuilder();
        sb.append("onIncomingFileTransferReceived normalizedParticipants : ");
        sb.append(IMSLog.numberChecker((Collection<ImsUri>) normalizedParticipants));
        Log.i(str, sb.toString());
        boolean z = normalizedParticipants.size() > 1 || ftIncomingSessionEvent2.mIsConference;
        boolean z2 = ftIncomingSessionEvent2.mStart != 0;
        if (ftIncomingSessionEvent2.mIsSlmSvcMsg && !ftIncomingSessionEvent2.mIsLMM) {
            if (this.mImModule.isBlockedNumber(phoneIdByIMSI, ftIncomingSessionEvent2.mSenderUri, z)) {
                this.mSlmService.rejectSlm(new RejectSlmParams((String) null, ftIncomingSessionEvent2.mRawHandle, ImSessionRejectReason.BUSY_HERE, (Message) null, ftIncomingSessionEvent2.mOwnImsi));
                return;
            }
            this.mSlmService.acceptSlm(new AcceptSlmParams((String) null, this.mImModule.getUserAlias(phoneIdByIMSI, false), ftIncomingSessionEvent2.mRawHandle, (Message) null, ftIncomingSessionEvent2.mOwnImsi));
        }
        ChatData.ChatType generateChatType = this.mImSessionProcessor.generateChatType(z, ftIncomingSessionEvent2.mIsSlmSvcMsg || this.mImModule.getRcsStrategy(phoneIdByIMSI).boolSetting(RcsPolicySettings.RcsPolicy.PARTICIPANTBASED_CLOSED_GROUPCHAT), false);
        ImSessionProcessor imSessionProcessor = this.mImSessionProcessor;
        String str2 = ftIncomingSessionEvent2.mOwnImsi;
        String str3 = ftIncomingSessionEvent2.mConversationId;
        ImSession findSession = imSessionProcessor.findSession(phoneIdByIMSI, str2, z, generateChatType, (String) null, str3, str3, normalizedParticipants, ChatMode.OFF);
        ImDirection extractImDirection = ImCpimNamespacesHelper.extractImDirection(phoneIdByIMSI, ftIncomingSessionEvent2.mCpimNamespaces);
        FtMessage findFileTransfer = findFileTransfer(findSession, ftIncomingSessionEvent2, extractImDirection);
        ChatData.ChatType chatType = generateChatType;
        boolean z3 = z2;
        Set<ImsUri> set = normalizedParticipants;
        int i = phoneIdByIMSI;
        RejectFtSessionParams checkForRejectIncomingFileTransfer = checkForRejectIncomingFileTransfer(phoneIdByIMSI, ftIncomingSessionEvent, z, findSession != null, findSession != null && findSession.getChatData().isMuted(), findFileTransfer != null, z3, findFileTransfer != null && findFileTransfer.getStatus() == ImConstants.Status.SENT);
        if (checkForRejectIncomingFileTransfer != null) {
            rejectFtSession(checkForRejectIncomingFileTransfer);
            return;
        }
        boolean z4 = z3;
        if (z4 && ftIncomingSessionEvent2.mPush && findFileTransfer != null) {
            Log.i(str, "onIncomingFileTransferReceived, resume invite");
            int i2 = ftIncomingSessionEvent2.mStart;
            findFileTransfer.setTransferredBytes(i2 > 0 ? i2 - 1 : 0);
        }
        if (findSession == null) {
            Log.e(str, "onIncomingFileTransferReceived: Session not found by participants.");
            findSession = this.mCache.makeNewEmptySession(ftIncomingSessionEvent2.mOwnImsi, set, chatType, extractImDirection);
        }
        findSession.setConversationId(ftIncomingSessionEvent2.mConversationId);
        findSession.setContributionId(ftIncomingSessionEvent2.mContributionId);
        findSession.setDirection(extractImDirection);
        if (findFileTransfer != null) {
            this.mCache.addToPendingList(findFileTransfer);
            findFileTransfer.setConversationId(ftIncomingSessionEvent2.mConversationId);
            findFileTransfer.setContributionId(ftIncomingSessionEvent2.mContributionId);
        } else {
            if (ftIncomingSessionEvent2.mIsRoutingMsg) {
                RoutingType msgRoutingType = this.mImModule.getMsgRoutingType(ftIncomingSessionEvent2.mRequestUri, ftIncomingSessionEvent2.mPAssertedId, ftIncomingSessionEvent2.mSenderUri, ftIncomingSessionEvent2.mReceiver, findSession.isGroupChat(), i);
                ftIncomingSessionEvent2.mRoutingType = msgRoutingType;
                if (msgRoutingType == RoutingType.SENT && !findSession.isGroupChat()) {
                    ftIncomingSessionEvent2.mSenderUri = ftIncomingSessionEvent2.mReceiver;
                }
            }
            findFileTransfer = this.mCache.makeNewIncomingFtMessage(findSession.getOwnImsi(), findSession, ftIncomingSessionEvent2, ftIncomingSessionEvent2.mIsSlmSvcMsg);
        }
        if (!findSession.isGroupChat()) {
            this.mImSessionProcessor.setLegacyLatching(findSession.getRemoteUri(), false, findSession.getChatData().getOwnIMSI());
        }
        findSession.receiveTransfer(findFileTransfer, ftIncomingSessionEvent2, z4);
        this.mImModule.updateServiceAvailability(ftIncomingSessionEvent2.mOwnImsi, ftIncomingSessionEvent2.mSenderUri, ftIncomingSessionEvent2.mImdnTime);
    }

    /* access modifiers changed from: protected */
    public void handleFileResizeResponse(String str, boolean z, Uri uri) {
        post(new FtProcessor$$ExternalSyntheticLambda12(this, str, z, uri));
    }

    /* access modifiers changed from: private */
    public /* synthetic */ void lambda$handleFileResizeResponse$11(String str, boolean z, Uri uri) {
        FtMessage ftMessage = this.mCache.getFtMessage(str, ImDirection.OUTGOING, (String) null);
        if (ftMessage != null) {
            ImSession imSession = this.mCache.getImSession(ftMessage.getChatId());
            if (imSession == null) {
                Log.e(LOG_TAG, "handleFileResizeResponse: FT not found in the cache.");
                return;
            }
            if (!this.mImModule.isRegistered(this.mImModule.getPhoneIdByIMSI(imSession.getOwnImsi()))) {
                IMSLog.i(LOG_TAG, "handleFileResizeResponse: not registered");
                notifyRegistrationError(ftMessage);
            } else if (ftMessage.getCancelReason() == CancelReason.CANCELED_BY_USER) {
                Log.e(LOG_TAG, "handleFileResizeResponse: FT is cancelled already!");
            } else {
                ftMessage.handleFileResizeResponse(z, uri);
            }
        } else {
            Log.e(LOG_TAG, "Message not found");
        }
    }

    /* access modifiers changed from: protected */
    public void notifyOngoingFtEvent(boolean z, int i) {
        post(new FtProcessor$$ExternalSyntheticLambda3(this, z, i));
    }

    /* access modifiers changed from: private */
    public /* synthetic */ void lambda$notifyOngoingFtEvent$12(boolean z, int i) {
        String str = LOG_TAG;
        Log.i(str, "notifyOngoingFtEvent [" + z + "] phoneId = " + i);
        try {
            if (this.mImsFtListenerList.containsKey(Integer.valueOf(i))) {
                RemoteCallbackList remoteCallbackList = this.mImsFtListenerList.get(Integer.valueOf(i));
                int beginBroadcast = remoteCallbackList.beginBroadcast();
                for (int i2 = 0; i2 < beginBroadcast; i2++) {
                    remoteCallbackList.getBroadcastItem(i2).onFtStateChanged(z);
                }
                remoteCallbackList.finishBroadcast();
            }
        } catch (RemoteException | IllegalStateException e) {
            e.printStackTrace();
        }
    }

    private void notifyRegistrationError(FtMessage ftMessage) {
        IMnoStrategy rcsStrategy = this.mImModule.getRcsStrategy(this.mImModule.getPhoneIdByIMSI(ftMessage.getOwnIMSI()));
        if (rcsStrategy == null || !rcsStrategy.boolSetting(RcsPolicySettings.RcsPolicy.FT_FALLBACK_DIRECTLY_OFFLINE)) {
            ftMessage.cancelTransfer(CancelReason.DEVICE_UNREGISTERED);
        } else {
            ftMessage.cancelTransfer(CancelReason.REMOTE_TEMPORARILY_UNAVAILABLE);
        }
    }

    /* access modifiers changed from: protected */
    public void handleFileTransferProgress(FtTransferProgressEvent ftTransferProgressEvent) {
        FtMessage ftMessage;
        String str = LOG_TAG;
        Log.i(str, "handleFileTransferProgress: " + ftTransferProgressEvent);
        if (ftTransferProgressEvent != null) {
            int i = ftTransferProgressEvent.mId;
            if (i != -1) {
                ftMessage = this.mCache.getFtMessage(i);
            } else {
                Object obj = ftTransferProgressEvent.mRawHandle;
                ftMessage = obj != null ? this.mCache.getFtMsrpMessage(obj) : null;
            }
            if (ftMessage != null) {
                ftMessage.handleTransferProgress(ftTransferProgressEvent);
            } else {
                Log.i(str, "handleFileTransferProgress: cannot get FtMessage.");
            }
        }
    }

    public void onNotifyCloudMsgFtEvent(FtMessage ftMessage) {
        ImSession imSession = this.mCache.getImSession(ftMessage.getChatId());
        if (imSession == null) {
            Log.e(LOG_TAG, "onNotifyCloudMsgFtEvent: session not found.");
        } else {
            this.mCache.notifyCloudMsgFtEvent(imSession.getOwnImsi(), ftMessage.getId(), ftMessage.getImdnId(), ftMessage.getDirection());
        }
    }

    private void rejectFtSession(RejectFtSessionParams rejectFtSessionParams) {
        if (rejectFtSessionParams.mIsSlmSvcMsg) {
            this.mSlmService.rejectFtSlmMessage(rejectFtSessionParams);
        } else {
            this.mImService.rejectFtSession(rejectFtSessionParams);
        }
    }

    /* access modifiers changed from: protected */
    public Collection<IFtEventListener> getFtEventListener(ImConstants.Type type) {
        return this.mFtEventListeners.get(type);
    }

    private RejectFtSessionParams checkForRejectIncomingFileTransfer(int i, FtIncomingSessionEvent ftIncomingSessionEvent, boolean z, boolean z2, boolean z3, boolean z4, boolean z5, boolean z6) {
        String str = ftIncomingSessionEvent.mContentType;
        if (str != null && str.contains(MIMEContentType.LOCATION_PUSH)) {
            ImModule imModule = this.mImModule;
            if (!imModule.getActiveCall(imModule.normalizeUri(i, ftIncomingSessionEvent.mSenderUri)) && this.mImModule.getImConfig(i).getImMsgTech() == ImConstants.ImMsgTech.SIMPLE_IM) {
                Log.i(LOG_TAG, "Receive geolocation Push via MSRP FT during inactive call!!.");
                return new RejectFtSessionParams(ftIncomingSessionEvent.mRawHandle, (Message) null, FtRejectReason.DECLINE, ftIncomingSessionEvent.mFileTransferId);
            }
        }
        if (z) {
            if (!z2 && !ftIncomingSessionEvent.mIsSlmSvcMsg) {
                Log.i(LOG_TAG, "onIncomingFileTransferReceived, no GC session for GC FT. auto reject");
                return new RejectFtSessionParams(ftIncomingSessionEvent.mRawHandle, (Message) null, FtRejectReason.NOT_ACCEPTABLE_HERE, ftIncomingSessionEvent.mFileTransferId);
            } else if (z2 && z3) {
                Log.i(LOG_TAG, "onIncomingFileTransferReceived, user reject GC FT.");
                return new RejectFtSessionParams(ftIncomingSessionEvent.mRawHandle, (Message) null, FtRejectReason.DECLINE, ftIncomingSessionEvent.mFileTransferId);
            }
        }
        if (z4 && !z5 && z6) {
            String str2 = LOG_TAG;
            Log.i(str2, "onIncomingFileTransferReceived, duplicate message with imdnid: " + ftIncomingSessionEvent.mImdnId);
            return new RejectFtSessionParams(ftIncomingSessionEvent.mRawHandle, Message.obtain(this, 1), FtRejectReason.NOT_ACCEPTABLE_HERE, (String) null, ftIncomingSessionEvent.mIsSlmSvcMsg);
        } else if (z4 || !z5 || !ftIncomingSessionEvent.mPush) {
            return null;
        } else {
            Log.i(LOG_TAG, "onIncomingFileTransferReceived, resume invite from MT cannot find history, auto reject");
            return new RejectFtSessionParams(ftIncomingSessionEvent.mRawHandle, Message.obtain(this, 1), FtRejectReason.NOT_ACCEPTABLE_HERE, (String) null, ftIncomingSessionEvent.mIsSlmSvcMsg);
        }
    }

    private FtMessage findFileTransfer(ImSession imSession, FtIncomingSessionEvent ftIncomingSessionEvent, ImDirection imDirection) {
        if (imSession == null) {
            return null;
        }
        FtMessage ftMessageforFtRequest = this.mCache.getFtMessageforFtRequest(imSession.getChatId(), ftIncomingSessionEvent.mFileName, ftIncomingSessionEvent.mFileSize, ftIncomingSessionEvent.mFileTransferId);
        if (ftMessageforFtRequest != null || TextUtils.isEmpty(ftIncomingSessionEvent.mImdnId)) {
            return ftMessageforFtRequest;
        }
        MessageBase message = this.mCache.getMessage(ftIncomingSessionEvent.mImdnId, imDirection, imSession.getChatId());
        if (!(message instanceof FtMessage)) {
            return ftMessageforFtRequest;
        }
        String str = LOG_TAG;
        Log.i(str, "onIncomingFileTransferReceived, found messageByImdn: " + ftIncomingSessionEvent.mImdnId);
        return (FtMessage) message;
    }
}
