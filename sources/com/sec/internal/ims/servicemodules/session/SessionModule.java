package com.sec.internal.ims.servicemodules.session;

import android.content.ContentValues;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.pm.ResolveInfo;
import android.net.ConnectivityManager;
import android.net.Network;
import android.net.NetworkCapabilities;
import android.os.Looper;
import android.os.Message;
import android.provider.Settings;
import android.text.TextUtils;
import android.util.ArraySet;
import android.util.Log;
import com.sec.ims.ImsRegistration;
import com.sec.ims.options.Capabilities;
import com.sec.ims.settings.ImsProfile;
import com.sec.ims.util.ImsUri;
import com.sec.ims.util.NameAddr;
import com.sec.internal.constants.Mno;
import com.sec.internal.constants.ims.MIMEContentType;
import com.sec.internal.constants.ims.SipMsg;
import com.sec.internal.constants.ims.config.ConfigConstants;
import com.sec.internal.constants.ims.servicemodules.im.ChatMode;
import com.sec.internal.constants.ims.servicemodules.im.ImCacheAction;
import com.sec.internal.constants.ims.servicemodules.im.ImConstants;
import com.sec.internal.constants.ims.servicemodules.im.ImDirection;
import com.sec.internal.constants.ims.servicemodules.im.ImError;
import com.sec.internal.constants.ims.servicemodules.im.ImIconData;
import com.sec.internal.constants.ims.servicemodules.im.ImParticipant;
import com.sec.internal.constants.ims.servicemodules.im.ImSubjectData;
import com.sec.internal.constants.ims.servicemodules.im.NotificationStatus;
import com.sec.internal.constants.ims.servicemodules.im.event.ImIncomingMessageEvent;
import com.sec.internal.constants.ims.servicemodules.im.event.ImIncomingSessionEvent;
import com.sec.internal.constants.ims.servicemodules.im.event.ImSessionClosedEvent;
import com.sec.internal.constants.ims.servicemodules.im.event.ImSessionEstablishedEvent;
import com.sec.internal.constants.ims.servicemodules.im.reason.ImErrorReason;
import com.sec.internal.constants.ims.servicemodules.im.reason.ImSessionClosedReason;
import com.sec.internal.constants.ims.servicemodules.im.result.Result;
import com.sec.internal.helper.AsyncResult;
import com.sec.internal.helper.DmConfigHelper;
import com.sec.internal.helper.Preconditions;
import com.sec.internal.helper.RcsConfigurationHelper;
import com.sec.internal.helper.SimUtil;
import com.sec.internal.ims.core.handler.secims.imsCommonStruc.MNO;
import com.sec.internal.ims.core.sim.SimManagerFactory;
import com.sec.internal.ims.rcs.RcsPolicyManager;
import com.sec.internal.ims.registry.ImsRegistry;
import com.sec.internal.ims.servicemodules.base.ServiceModuleBase;
import com.sec.internal.ims.servicemodules.im.ImCache;
import com.sec.internal.ims.servicemodules.im.ImConfig;
import com.sec.internal.ims.servicemodules.im.ImMessage;
import com.sec.internal.ims.servicemodules.im.ImSession;
import com.sec.internal.ims.servicemodules.im.ImSessionBuilder;
import com.sec.internal.ims.servicemodules.im.MessageBase;
import com.sec.internal.ims.servicemodules.im.interfaces.IGetter;
import com.sec.internal.ims.servicemodules.im.interfaces.IRcsBigDataProcessor;
import com.sec.internal.ims.servicemodules.im.listener.ImMessageListener;
import com.sec.internal.ims.servicemodules.im.listener.ImSessionListener;
import com.sec.internal.ims.servicemodules.im.strategy.IMnoStrategy;
import com.sec.internal.ims.servicemodules.options.Intents;
import com.sec.internal.ims.settings.DeviceConfigManager;
import com.sec.internal.ims.settings.RcsPolicySettings;
import com.sec.internal.ims.util.ConfigUtil;
import com.sec.internal.ims.util.ImsUtil;
import com.sec.internal.ims.util.StringIdGenerator;
import com.sec.internal.ims.util.UriGenerator;
import com.sec.internal.ims.util.UriGeneratorFactory;
import com.sec.internal.interfaces.ims.imsservice.ICall;
import com.sec.internal.interfaces.ims.servicemodules.im.IImServiceInterface;
import com.sec.internal.interfaces.ims.servicemodules.options.ICapabilityDiscoveryModule;
import com.sec.internal.interfaces.ims.servicemodules.session.ISessionModule;
import java.io.File;
import java.nio.charset.Charset;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.Date;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Hashtable;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;

public class SessionModule extends ServiceModuleBase implements ISessionModule, ImSessionListener, ImMessageListener, IGetter {
    private static final int EVENT_CLOSE_SESSION = 7;
    private static final int EVENT_CONFIGURED = 8;
    private static final int EVENT_INCOMING_MESSAGE = 4;
    private static final int EVENT_INCOMING_SESSION = 1;
    private static final int EVENT_REJECT_SESSION = 6;
    private static final int EVENT_SEND_MESSAGE_FAILED = 5;
    private static final int EVENT_SESSION_CLOSED = 3;
    private static final int EVENT_SESSION_ESTABLISHED = 2;
    public static final String INTENT_FILTER_MESSAGE = "com.gsma.services.rcs.extension.action.NEW_MESSAGING_SESSION";
    public static final String INTENT_FILTER_STREAM = "com.gsma.services.rcs.extension.action.NEW_STREAMING_SESSION";
    private static final String LOG_TAG = SessionModule.class.getSimpleName();
    public static final String MIMETYPE_ALL = "com.gsma.services.rcs/*";
    public static final String MIMETYPE_PREFIX = "com.gsma.services.rcs/";
    public static final String NAME = SessionModule.class.getSimpleName();
    private static final String SERVICE_ID_CALL_COMPOSER = "gsma.callcomposer";
    private static final String SERVICE_ID_POST_CALL = "gsma.callunanswered";
    private static final String SERVICE_ID_SHARED_MAP = "gsma.sharedmap";
    private static final String SERVICE_ID_SHARED_SKETCH = "gsma.sharedsketch";
    private static long mInactivityTimeout = 0;
    private static final String[] sRequiredServices = {"ec"};
    private boolean canRegisterExt = false;
    private boolean isEnableFailedMedia = false;
    private boolean isWaitingForCloseTagSendingComplete = false;
    private final IRcsBigDataProcessor mBigDataProcessor;
    private int[] mCallComposerTimerIdle = {MNO.EVR_ESN, MNO.EVR_ESN};
    private List<ImsUri> mCallList = new ArrayList();
    private boolean[] mComposerAuth = {false, false};
    private ImConfig mConfig;
    private final Context mContext;
    private final List<String> mIariTypes = new ArrayList();
    private final IImServiceInterface mImService;
    private final List<IMessagingSessionListener> mListeners = new ArrayList();
    private final Map<String, ImSession.SessionState> mMessagingSessionStates = new HashMap();
    private final Map<String, ImSession> mMessagingSessions = new ConcurrentHashMap();
    private boolean[] mPostCallAuth = {false, false};
    private final Set<String> mRegisteredServices = new ArraySet();
    private int mRegistrationId = -1;
    private final List<String> mServiceIDsFromMetaData = new ArrayList();
    private boolean[] mSharedMapAuth = {false, false};
    private boolean[] mSharedSketchAuth = {false, false};
    private UriGenerator mUriGenerator;

    private void buildServiceConfig(String str) {
    }

    public void addToRevokingMessages(String str, String str2) {
    }

    public void deRegisterApp() {
    }

    public String getUserAlias(int i, boolean z) {
        return "";
    }

    public ImsUri normalizeUri(ImsUri imsUri) {
        return null;
    }

    public void onAddParticipantsFailed(String str, List<ImsUri> list, ImErrorReason imErrorReason) {
    }

    public void onAddParticipantsSucceeded(String str, List<ImsUri> list) {
    }

    public void onBlockedMessageReceived(ImIncomingMessageEvent imIncomingMessageEvent) {
    }

    public void onChangeGroupAliasFailed(String str, String str2, ImErrorReason imErrorReason) {
    }

    public void onChangeGroupAliasSucceeded(String str, String str2) {
    }

    public void onChangeGroupChatIconFailed(String str, String str2, ImErrorReason imErrorReason) {
    }

    public void onChangeGroupChatIconSuccess(String str, String str2) {
    }

    public void onChangeGroupChatLeaderFailed(String str, List<ImsUri> list, ImErrorReason imErrorReason) {
    }

    public void onChangeGroupChatLeaderSucceeded(String str, List<ImsUri> list) {
    }

    public void onChangeGroupChatSubjectFailed(String str, String str2, ImErrorReason imErrorReason) {
    }

    public void onChangeGroupChatSubjectSucceeded(String str, String str2) {
    }

    public void onChatClosed(ImSession imSession, ImSessionClosedReason imSessionClosedReason) {
    }

    public void onChatDeparted(ImSession imSession) {
    }

    public void onChatEstablished(ImSession imSession) {
    }

    public void onChatSubjectUpdated(String str, ImSubjectData imSubjectData) {
    }

    public void onComposingReceived(ImSession imSession, ImsUri imsUri, String str, boolean z, int i) {
    }

    public void onGroupChatIconDeleted(String str) {
    }

    public void onGroupChatIconUpdated(String str, ImIconData imIconData) {
    }

    public void onGroupChatLeaderChanged(ImSession imSession, String str) {
    }

    public void onGroupChatLeaderInformed(ImSession imSession, String str) {
    }

    public void onHandleParticipants(ImSession imSession, Collection<ImParticipant> collection, ImCacheAction imCacheAction) {
    }

    public void onImErrorReport(ImError imError, int i) {
    }

    public void onIncomingMessageProcessed(ImIncomingMessageEvent imIncomingMessageEvent, ImSession imSession) {
    }

    public void onIncomingSessionProcessed(ImIncomingMessageEvent imIncomingMessageEvent, ImSession imSession, boolean z) {
    }

    public void onMessageReceived(ImMessage imMessage) {
    }

    public void onMessageRevocationDone(ImConstants.RevocationStatus revocationStatus, Collection<MessageBase> collection, ImSession imSession) {
    }

    public void onMessageRevokeTimerExpired(String str, Collection<String> collection, String str2) {
    }

    public void onMessageSendResponse(ImMessage imMessage) {
    }

    public void onMessageSendResponseTimeout(ImMessage imMessage) {
    }

    public void onNotifyParticipantsAdded(ImSession imSession, Map<ImParticipant, Date> map) {
    }

    public void onNotifyParticipantsJoined(ImSession imSession, Map<ImParticipant, Date> map) {
    }

    public void onNotifyParticipantsKickedOut(ImSession imSession, Map<ImParticipant, Date> map) {
    }

    public void onNotifyParticipantsLeft(ImSession imSession, Map<ImParticipant, Date> map) {
    }

    public void onParticipantAliasUpdated(String str, ImParticipant imParticipant) {
    }

    public void onParticipantsDeleted(ImSession imSession, Collection<ImParticipant> collection) {
    }

    public void onParticipantsInserted(ImSession imSession, Collection<ImParticipant> collection) {
    }

    public void onParticipantsUpdated(ImSession imSession, Collection<ImParticipant> collection) {
    }

    public void onProcessingFileTransferChanged(ImSession imSession) {
    }

    public void onRemoveParticipantsFailed(String str, List<ImsUri> list, ImErrorReason imErrorReason) {
    }

    public void onRemoveParticipantsSucceeded(String str, List<ImsUri> list) {
    }

    public void onRequestSendMessage(ImSession imSession, MessageBase messageBase) {
    }

    public void onSendCanceledNotificationDone(String str, String str2, boolean z) {
    }

    public void removeFromRevokingMessages(Collection<String> collection) {
    }

    public void setLegacyLatching(ImsUri imsUri, boolean z, String str) {
    }

    public SessionModule(Looper looper, Context context, IImServiceInterface iImServiceInterface) {
        super(looper);
        this.mContext = context;
        this.mUriGenerator = UriGeneratorFactory.getInstance().get(UriGenerator.URIServiceType.RCS_URI);
        this.mBigDataProcessor = new EcBigDataProcessor(context, this);
        this.mConfig = ImsRegistry.getServiceModuleManager().getImModule().getImConfig();
        this.mImService = iImServiceInterface;
        log("SessionModule");
    }

    public int getMaxMsrpLengthForExtensions() {
        return RcsConfigurationHelper.readIntParam(this.mContext, ConfigConstants.ConfigTable.OTHER_EXTENSIONS_MAX_MSRP_SIZE, 0).intValue();
    }

    public long getInactivityTimeout() {
        return mInactivityTimeout;
    }

    public int getPhoneIdByIMSI(String str) {
        int simSlotPriority = SimUtil.getSimSlotPriority();
        int phoneId = SimManagerFactory.getPhoneId(str);
        return phoneId != -1 ? phoneId : simSlotPriority;
    }

    public static void setInactivityTimeout(long j) {
        String str = NAME;
        Log.d(str, "set InactivityTimeout=: " + j);
        mInactivityTimeout = j;
    }

    public void onMessageSendingSucceeded(MessageBase messageBase) {
        String str = LOG_TAG;
        Log.d(str, "onMessageSendingSucceeded");
        this.mBigDataProcessor.onMessageSendingSucceeded(messageBase);
        ImSession imSession = this.mMessagingSessions.get(messageBase.getChatId());
        if (imSession == null) {
            Log.e(str, "onMessageSendingSucceeded: Session not found.");
            return;
        }
        for (IMessagingSessionListener onMessagesFlushed : this.mListeners) {
            onMessagesFlushed.onMessagesFlushed(imSession);
        }
        if (this.isWaitingForCloseTagSendingComplete) {
            Log.d(LOG_TAG, "onMessageSendingSucceeded : EVENT_CLOSE_SESSION");
            removeMessages(7);
            sendMessage(obtainMessage(7, imSession.getChatId()));
        }
    }

    public void onMessageSendingFailed(MessageBase messageBase, IMnoStrategy.StrategyResponse strategyResponse, Result result) {
        if (result != null && result.getType() != Result.Type.NONE) {
            this.mBigDataProcessor.onMessageSendingFailed(messageBase, result, strategyResponse);
        }
    }

    public void onChatStatusUpdate(ImSession imSession, ImSession.SessionState sessionState) {
        ImSession.SessionState sessionState2 = this.mMessagingSessionStates.get(imSession.getChatId());
        if (sessionState == ImSession.SessionState.CLOSED) {
            if (this.isEnableFailedMedia && sessionState2 == ImSession.SessionState.ESTABLISHED) {
                sessionState = ImSession.SessionState.FAILED_MEDIA;
                Log.e(LOG_TAG, "onChatStatusUpdate: State is FAILED MEDIA");
            }
            this.mMessagingSessions.remove(imSession.getChatId());
        }
        String str = LOG_TAG;
        Log.i(str, "onChatStatusUpdate: isEnableFailedMedia = " + this.isEnableFailedMedia);
        for (IMessagingSessionListener onStateChanged : this.mListeners) {
            onStateChanged.onStateChanged(imSession, sessionState);
        }
        if (sessionState == ImSession.SessionState.CLOSED) {
            this.mMessagingSessionStates.remove(imSession.getChatId());
        } else if (sessionState != ImSession.SessionState.INITIAL) {
            this.mMessagingSessionStates.put(imSession.getChatId(), sessionState);
        }
    }

    public ImSession getMessagingSession(String str) {
        return this.mMessagingSessions.get(str);
    }

    public ImSession getMessagingSession(String str, ImsUri imsUri) {
        Preconditions.checkNotNull(str);
        Preconditions.checkNotNull(imsUri);
        for (ImSession next : this.mMessagingSessions.values()) {
            if (TextUtils.equals(next.getServiceId(), str) && imsUri.equals(next.getRemoteUri())) {
                return next;
            }
        }
        return null;
    }

    public void sendMultimediaMessage(String str, byte[] bArr, String str2) {
        ImSession imSession = this.mMessagingSessions.get(str);
        if (imSession == null) {
            Log.e(LOG_TAG, "sendMultimediaMessage: Session not found.");
        } else {
            imSession.sendImMessage(createOutgoingMessage(str, imSession.getRemoteUri(), bArr, str2));
        }
    }

    public void abortSession(String str) {
        this.isWaitingForCloseTagSendingComplete = true;
        sendMessageDelayed(obtainMessage(7, str), 1000);
    }

    public void closeSession(String str) {
        String str2 = LOG_TAG;
        Log.d(str2, "closeSession: " + str);
        ImSession imSession = this.mMessagingSessions.get(str);
        if (imSession == null) {
            Log.e(str2, "closeSession: Session not found.");
        } else {
            imSession.closeSession();
        }
    }

    private ImMessage createOutgoingMessage(String str, ImsUri imsUri, byte[] bArr, String str2) {
        return ((ImMessage.Builder) ((ImMessage.Builder) ((ImMessage.Builder) ((ImMessage.Builder) ((ImMessage.Builder) ((ImMessage.Builder) ((ImMessage.Builder) ((ImMessage.Builder) ((ImMessage.Builder) ((ImMessage.Builder) ((ImMessage.Builder) ((ImMessage.Builder) ((ImMessage.Builder) ((ImMessage.Builder) ((ImMessage.Builder) ((ImMessage.Builder) ImMessage.builder().module(this)).listener(this).config(this.mConfig)).uriGenerator(this.mUriGenerator)).imsService(this.mImService)).slmService(ImsRegistry.getHandlerFactory().getSlmHandler())).chatId(str)).remoteUri(imsUri)).body(new String(bArr, Charset.defaultCharset()))).imdnId(StringIdGenerator.generateImdn())).dispNotification(new HashSet(Arrays.asList(new NotificationStatus[]{NotificationStatus.NONE})))).contentType(str2)).direction(ImDirection.OUTGOING)).status(ImConstants.Status.TO_SEND)).type(ImConstants.Type.TEXT)).insertedTimestamp(System.currentTimeMillis())).mnoStrategy(RcsPolicyManager.getRcsStrategy(SimUtil.getSimSlotPriority()))).build();
    }

    public ImSession initiateMessagingSession(String str, ImsUri imsUri, String[] strArr, String[] strArr2) {
        String adjustServiceId = adjustServiceId(str);
        ImSession createOutgoingSession = createOutgoingSession(SimManagerFactory.getSimManager().getImsi(), adjustServiceId, imsUri, adjustAcceptTypes(adjustServiceId, strArr), adjustAcceptWrappedTypes(adjustServiceId, strArr2));
        createOutgoingSession.startSession();
        this.isEnableFailedMedia = false;
        return createOutgoingSession;
    }

    private String adjustServiceId(String str) {
        str.hashCode();
        char c = 65535;
        switch (str.hashCode()) {
            case 136638338:
                if (str.equals(SERVICE_ID_POST_CALL)) {
                    c = 0;
                    break;
                }
                break;
            case 1028711913:
                if (str.equals(SERVICE_ID_SHARED_MAP)) {
                    c = 1;
                    break;
                }
                break;
            case 1482410284:
                if (str.equals(SERVICE_ID_CALL_COMPOSER)) {
                    c = 2;
                    break;
                }
                break;
            case 1945740287:
                if (str.equals(SERVICE_ID_SHARED_SKETCH)) {
                    c = 3;
                    break;
                }
                break;
        }
        switch (c) {
            case 0:
                return SipMsg.FEATURE_TAG_ENRICHED_POST_CALL;
            case 1:
                return SipMsg.FEATURE_TAG_ENRICHED_SHARED_MAP;
            case 2:
                return SipMsg.FEATURE_TAG_ENRICHED_CALL_COMPOSER;
            case 3:
                return SipMsg.FEATURE_TAG_ENRICHED_SHARED_SKETCH;
            default:
                return str;
        }
    }

    private String adjustServiceId2(String str) {
        str.hashCode();
        char c = 65535;
        switch (str.hashCode()) {
            case -1756044211:
                if (str.equals(SipMsg.FEATURE_TAG_ENRICHED_SHARED_SKETCH)) {
                    c = 0;
                    break;
                }
                break;
            case -749354161:
                if (str.equals(SipMsg.FEATURE_TAG_ENRICHED_SHARED_MAP)) {
                    c = 1;
                    break;
                }
                break;
            case -365814102:
                if (str.equals(SipMsg.FEATURE_TAG_ENRICHED_POST_CALL)) {
                    c = 2;
                    break;
                }
                break;
            case 1060594880:
                if (str.equals(SipMsg.FEATURE_TAG_ENRICHED_CALL_COMPOSER)) {
                    c = 3;
                    break;
                }
                break;
        }
        switch (c) {
            case 0:
                return SERVICE_ID_SHARED_SKETCH;
            case 1:
                return SERVICE_ID_SHARED_MAP;
            case 2:
                return SERVICE_ID_POST_CALL;
            case 3:
                return SERVICE_ID_CALL_COMPOSER;
            default:
                return str;
        }
    }

    private List<String> adjustAcceptTypes(String str, String[] strArr) {
        ArrayList arrayList = new ArrayList(strArr.length);
        Collections.addAll(arrayList, strArr);
        str.hashCode();
        char c = 65535;
        switch (str.hashCode()) {
            case -1756044211:
                if (str.equals(SipMsg.FEATURE_TAG_ENRICHED_SHARED_SKETCH)) {
                    c = 0;
                    break;
                }
                break;
            case -749354161:
                if (str.equals(SipMsg.FEATURE_TAG_ENRICHED_SHARED_MAP)) {
                    c = 1;
                    break;
                }
                break;
            case -365814102:
                if (str.equals(SipMsg.FEATURE_TAG_ENRICHED_POST_CALL)) {
                    c = 2;
                    break;
                }
                break;
            case 1060594880:
                if (str.equals(SipMsg.FEATURE_TAG_ENRICHED_CALL_COMPOSER)) {
                    c = 3;
                    break;
                }
                break;
        }
        switch (c) {
            case 0:
                arrayList.add(MIMEContentType.SHARED_SKETCH);
                break;
            case 1:
                arrayList.add(MIMEContentType.SHARED_MAP);
                break;
            case 2:
                arrayList.add(MIMEContentType.EN_CALL);
                arrayList.add(MIMEContentType.FT_HTTP);
                break;
            case 3:
                arrayList.add(MIMEContentType.EN_CALL);
                arrayList.add(MIMEContentType.CPIM);
                break;
        }
        return arrayList;
    }

    private List<String> adjustAcceptWrappedTypes(String str, String[] strArr) {
        ArrayList arrayList = new ArrayList(strArr.length);
        Collections.addAll(arrayList, strArr);
        str.hashCode();
        if (str.equals(SipMsg.FEATURE_TAG_ENRICHED_CALL_COMPOSER)) {
            arrayList.add(MIMEContentType.IMDN);
            arrayList.add(MIMEContentType.FT_HTTP);
        }
        return arrayList;
    }

    private ImSession createOutgoingSession(String str, String str2, ImsUri imsUri, List<String> list, List<String> list2) {
        HashSet hashSet = new HashSet();
        hashSet.add(imsUri);
        ImSession build = new ImSessionBuilder().looper(getLooper()).listener(this).config(this.mConfig).imsService(this.mImService).slmService(ImsRegistry.getHandlerFactory().getSlmHandler()).uriGenerator(this.mUriGenerator).chatId(StringIdGenerator.generateChatId(hashSet, str, true, ChatMode.OFF.getId())).participantsUri(hashSet).direction(ImDirection.OUTGOING).ownSimIMSI(str).getter(this).serviceId(str2).acceptTypes(list).acceptWrappedTypes(list2).build();
        this.mMessagingSessions.put(build.getChatId(), build);
        return build;
    }

    private ImSession createIncomingImSession(ImIncomingSessionEvent imIncomingSessionEvent) {
        ArrayList arrayList = new ArrayList();
        arrayList.add(imIncomingSessionEvent.mInitiator);
        ImSession build = new ImSessionBuilder().looper(getLooper()).listener(this).config(this.mConfig).imsService(this.mImService).slmService(ImsRegistry.getHandlerFactory().getSlmHandler()).uriGenerator(this.mUriGenerator).chatId(StringIdGenerator.generateChatId(new HashSet(imIncomingSessionEvent.mRecipients), imIncomingSessionEvent.mOwnImsi, true, ChatMode.OFF.getId())).participantsUri(arrayList).sdpContentType(imIncomingSessionEvent.mSdpContentType).direction(ImDirection.INCOMING).rawHandle(imIncomingSessionEvent.mRawHandle).sessionType(imIncomingSessionEvent.mSessionType).ownSimIMSI(imIncomingSessionEvent.mOwnImsi).getter(this).serviceId(imIncomingSessionEvent.mServiceId).build();
        this.mMessagingSessions.put(build.getChatId(), build);
        return build;
    }

    public void registerMessagingSessionListener(IMessagingSessionListener iMessagingSessionListener) {
        this.mListeners.add(iMessagingSessionListener);
    }

    public String[] getServicesRequiring() {
        return sRequiredServices;
    }

    public void handleIntent(Intent intent) {
        log("handleIntent" + intent);
    }

    public void init() {
        super.init();
        log("SessionModule init");
        updateAppInfo();
    }

    public boolean isServiceRegistered() {
        return getImsRegistration() != null;
    }

    public void log(String str) {
        Log.d(NAME, str);
    }

    public boolean needDeRegister(String str) {
        log("needDeRegister " + str);
        Hashtable hashtable = new Hashtable(AppInfo.ALL);
        updateAppInfo();
        return !this.canRegisterExt && isServiceRegistered() && hashtable.containsKey(str) && !AppInfo.ALL.containsKey(str);
    }

    public boolean needRegister(String str) {
        log("needRegister " + str);
        Hashtable hashtable = new Hashtable(AppInfo.ALL);
        updateAppInfo();
        return this.canRegisterExt && !isServiceRegistered() && !hashtable.containsKey(str) && AppInfo.ALL.containsKey(str);
    }

    public void onDeregistered(ImsRegistration imsRegistration, int i) {
        super.onDeregistered(imsRegistration, i);
        log("onDeregistered " + imsRegistration.toString() + "\n errorcode=" + i);
        this.mRegistrationId = -1;
        this.isEnableFailedMedia = true;
    }

    public void onConfigured(int i) {
        super.onConfigured(i);
        sendMessage(obtainMessage(8, Integer.valueOf(i)));
    }

    public void onDeregistering(ImsRegistration imsRegistration) {
        super.onDeregistering(imsRegistration);
        this.isEnableFailedMedia = true;
        log("onDeregistering " + imsRegistration.toString());
        if (SimManagerFactory.getSimManager().getSimMno() == Mno.RJIL) {
            for (String closeSession : this.mMessagingSessions.keySet()) {
                closeSession(closeSession);
            }
        }
    }

    public void onRegistered(ImsRegistration imsRegistration) {
        ICapabilityDiscoveryModule capabilityDiscoveryModule;
        if (imsRegistration == null) {
            Log.d(LOG_TAG, "regiInfo is null");
            return;
        }
        super.onRegistered(imsRegistration);
        int phoneId = imsRegistration.getPhoneId();
        log("onRegistered " + imsRegistration.toString());
        if (imsRegistration.getImsProfile() != null) {
            this.mRegistrationId = getRegistrationInfoId(imsRegistration);
        }
        this.isEnableFailedMedia = false;
        this.mUriGenerator = UriGeneratorFactory.getInstance().get(imsRegistration.getPreferredImpu().getUri(), UriGenerator.URIServiceType.RCS_URI);
        if (RcsPolicyManager.getRcsStrategy(SimUtil.getSimSlotPriority()).boolSetting(RcsPolicySettings.RcsPolicy.USE_SIPURI_FOR_URIGENERATOR)) {
            Iterator it = imsRegistration.getImpuList().iterator();
            while (true) {
                if (!it.hasNext()) {
                    break;
                }
                NameAddr nameAddr = (NameAddr) it.next();
                if (nameAddr.getUri().getUriType() == ImsUri.UriType.SIP_URI) {
                    this.mUriGenerator = UriGeneratorFactory.getInstance().get(nameAddr.getUri(), UriGenerator.URIServiceType.RCS_URI);
                    break;
                }
            }
        }
        if (imsRegistration.hasService("options") && imsRegistration.hasService("ec") && !imsRegistration.hasService("vs") && (capabilityDiscoveryModule = getServiceModuleManager().getCapabilityDiscoveryModule()) != null) {
            capabilityDiscoveryModule.exchangeCapabilitiesForVSHOnRegi(false, phoneId);
        }
    }

    public void onSimChanged(int i) {
        super.onSimChanged(i);
    }

    public void registerApp() {
        log("registerApp");
        if (getImsRegistration() != null) {
            for (String buildServiceConfig : this.mIariTypes) {
                buildServiceConfig(buildServiceConfig);
            }
            log("register ext done");
        }
    }

    public void onServiceSwitched(int i, ContentValues contentValues) {
        String str = LOG_TAG;
        Log.d(str, "onServiceSwitched: " + i);
        updateFeatures(i);
    }

    public void start() {
        if (!isRunning()) {
            super.start();
            log("SessionModule start");
            this.mImService.registerForImIncomingSession(this, 1, (Object) null);
            this.mImService.registerForImSessionEstablished(this, 2, (Object) null);
            this.mImService.registerForImSessionClosed(this, 3, (Object) null);
            this.mImService.registerForImIncomingMessage(this, 4, (Object) null);
            this.mImService.registerForMessageFailed(this, 5, (Object) null);
        }
    }

    public void stop() {
        super.stop();
        this.mRegisteredServices.clear();
        this.mImService.unregisterForImIncomingSession(this);
        this.mImService.unregisterForImSessionEstablished(this);
        this.mImService.unregisterForImSessionClosed(this);
        this.mImService.unregisterForImIncomingMessage(this);
        this.mImService.unregisterForMessageFailed(this);
        log("SessionModule stop");
    }

    public void handleMessage(Message message) {
        super.handleMessage(message);
        switch (message.what) {
            case 1:
                onIncomingSessionReceived((ImIncomingSessionEvent) ((AsyncResult) message.obj).result);
                return;
            case 2:
                onSessionEstablished((ImSessionEstablishedEvent) ((AsyncResult) message.obj).result);
                return;
            case 3:
                onSessionClosed((ImSessionClosedEvent) ((AsyncResult) message.obj).result);
                return;
            case 4:
                onIncomingMessageReceived((ImIncomingMessageEvent) ((AsyncResult) message.obj).result);
                return;
            case 6:
                onRejectSession((ImSession) message.obj);
                return;
            case 7:
                this.isWaitingForCloseTagSendingComplete = false;
                closeSession((String) message.obj);
                return;
            case 8:
                updateConfig(((Integer) message.obj).intValue());
                return;
            default:
                return;
        }
    }

    private void updateConfig(int i) {
        updateFeatures(i);
        updateAppInfo();
        ImsProfile imsProfile = ImsRegistry.getRegistrationManager().getImsProfile(i, ImsProfile.PROFILE_TYPE.CHAT);
        if (((Boolean) Optional.ofNullable(imsProfile).map(new SessionModule$$ExternalSyntheticLambda0()).orElse(Boolean.TRUE)).booleanValue()) {
            Log.e(LOG_TAG, "profile is null, return !!!");
            return;
        }
        String rcsProfileWithFeature = ConfigUtil.getRcsProfileWithFeature(this.mContext, i, imsProfile);
        String str = LOG_TAG;
        Log.d(str, "rcsProfile = " + rcsProfileWithFeature);
        if (ImsRegistry.getServiceModuleManager().getImModule() != null) {
            this.mConfig = ImsRegistry.getServiceModuleManager().getImModule().getImConfig();
        }
    }

    /* access modifiers changed from: private */
    public static /* synthetic */ Boolean lambda$updateConfig$0(ImsProfile imsProfile) {
        return Boolean.valueOf(!imsProfile.hasService("im") && !imsProfile.hasService("slm"));
    }

    private void onIncomingSessionReceived(ImIncomingSessionEvent imIncomingSessionEvent) {
        String str = LOG_TAG;
        Log.d(str, "onIncomingSessionReceived: " + imIncomingSessionEvent);
        this.isEnableFailedMedia = false;
        if (this.mRegisteredServices.contains(imIncomingSessionEvent.mServiceId)) {
            ImSession createIncomingImSession = createIncomingImSession(imIncomingSessionEvent);
            String adjustServiceId2 = adjustServiceId2(imIncomingSessionEvent.mServiceId);
            boolean activeCall = getActiveCall(imIncomingSessionEvent.mInitiator);
            Log.d(str, "getActiveCall result = " + activeCall);
            if (activeCall || (!adjustServiceId2.equals(SERVICE_ID_SHARED_MAP) && !adjustServiceId2.equals(SERVICE_ID_SHARED_SKETCH))) {
                for (IMessagingSessionListener onIncomingSessionInvited : this.mListeners) {
                    onIncomingSessionInvited.onIncomingSessionInvited(createIncomingImSession, MIMETYPE_PREFIX + adjustServiceId2);
                }
                createIncomingImSession.processIncomingSession(imIncomingSessionEvent);
                return;
            }
            Log.d(str, "Number not in call, reject invite. ServiceID: " + adjustServiceId2);
            createIncomingImSession.processIncomingSession(imIncomingSessionEvent);
            sendMessage(obtainMessage(6, createIncomingImSession));
        }
    }

    private void onSessionEstablished(ImSessionEstablishedEvent imSessionEstablishedEvent) {
        String str = LOG_TAG;
        Log.d(str, "onSessionEstablished: " + imSessionEstablishedEvent);
        ImSession imSession = this.mMessagingSessions.get(imSessionEstablishedEvent.mChatId);
        if (imSession == null) {
            Log.e(str, "onSessionEstablished: Session not found.");
        } else {
            imSession.receiveSessionEstablished(imSessionEstablishedEvent);
        }
    }

    private void onSessionClosed(ImSessionClosedEvent imSessionClosedEvent) {
        ImSession imSession;
        String str = LOG_TAG;
        Log.d(str, "onSessionClosed: " + imSessionClosedEvent);
        String str2 = imSessionClosedEvent.mChatId;
        if (str2 == null) {
            imSession = getImSessionByRawHandle(imSessionClosedEvent.mRawHandle);
        } else {
            imSession = this.mMessagingSessions.get(str2);
        }
        if (imSession == null) {
            Log.e(str, "onSessionClosed: Session not found.");
            return;
        }
        ImError imError = imSessionClosedEvent.mResult.getImError();
        if (imError == ImError.NETWORK_ERROR || imError == ImError.DEVICE_UNREGISTERED || imError == ImError.DEDICATED_BEARER_ERROR) {
            Log.e(str, "onSessionClosed: Session closed by " + imError);
            this.isEnableFailedMedia = true;
        }
        imSession.receiveSessionClosed(imSessionClosedEvent);
    }

    public ImSession getImSessionByRawHandle(Object obj) {
        for (ImSession next : this.mMessagingSessions.values()) {
            if (next.hasImSessionInfo(obj)) {
                return next;
            }
        }
        return null;
    }

    private void onIncomingMessageReceived(ImIncomingMessageEvent imIncomingMessageEvent) {
        String str = LOG_TAG;
        Log.d(str, "onIncomingMessageReceived: " + imIncomingMessageEvent);
        String str2 = imIncomingMessageEvent.mChatId;
        if (str2 == null) {
            Log.e(str, "onIncomingMessageReceived: mChatId is null.");
            return;
        }
        ImSession imSession = this.mMessagingSessions.get(str2);
        if (imSession == null) {
            Log.e(str, "onIncomingMessageReceived: Session not found.");
            return;
        }
        for (IMessagingSessionListener onMessageReceived : this.mListeners) {
            onMessageReceived.onMessageReceived(imSession, imIncomingMessageEvent.mBody.getBytes(Charset.defaultCharset()), imIncomingMessageEvent.mContentType);
        }
        this.mBigDataProcessor.onMessageReceived((MessageBase) null, imSession);
    }

    private void onRejectSession(ImSession imSession) {
        Log.d(LOG_TAG, "onRejectSession");
        imSession.rejectSession();
    }

    public void updateAppInfo() {
        this.canRegisterExt = false;
        AppInfo.ALL.clear();
        this.mIariTypes.clear();
        updateAppInfo(INTENT_FILTER_MESSAGE);
        updateAppInfo(INTENT_FILTER_STREAM);
        if (!AppInfo.ALL.isEmpty()) {
            this.canRegisterExt = true;
        }
    }

    public void updateAppInfo(String str) {
        AppInfo appInfo;
        Intent intent = new Intent();
        intent.setType(MIMETYPE_ALL);
        intent.addCategory(Intents.INTENT_CATEGORY);
        intent.addCategory("android.intent.category.LAUNCHER");
        intent.setAction(str);
        List<ResolveInfo> queryBroadcastReceivers = this.mContext.getPackageManager().queryBroadcastReceivers(intent, 64);
        if (queryBroadcastReceivers != null) {
            for (ResolveInfo next : queryBroadcastReceivers) {
                String str2 = next.activityInfo.packageName;
                log("new app name = " + str2);
                synchronized (AppInfo.ALL) {
                    if (AppInfo.ALL.containsKey(str2)) {
                        appInfo = AppInfo.ALL.get(str2);
                    } else {
                        appInfo = new AppInfo(str2);
                    }
                    IntentFilter intentFilter = next.filter;
                    if (intentFilter != null) {
                        int countDataTypes = intentFilter.countDataTypes();
                        ArrayList arrayList = new ArrayList();
                        for (int i = 0; i < countDataTypes; i++) {
                            String dataType = next.filter.getDataType(i);
                            String substring = dataType.substring(dataType.lastIndexOf("/") + 1);
                            arrayList.add(substring);
                            if (!this.mIariTypes.contains(substring)) {
                                this.mIariTypes.add(substring);
                            }
                        }
                        if (arrayList.size() > 0) {
                            appInfo.addType(str, arrayList);
                        }
                    }
                }
            }
        }
    }

    public boolean isServiceActivated(String str) {
        log("isServiceActivated,serviceId= " + str);
        int activeDataPhoneId = SimUtil.getActiveDataPhoneId();
        if (!str.startsWith("gsma")) {
            int currentNetworkByPhoneId = ImsRegistry.getRegistrationManager().getCurrentNetworkByPhoneId(activeDataPhoneId);
            if (currentNetworkByPhoneId == 1 || currentNetworkByPhoneId == 2) {
                log("isServiceActivated: current network is 2G, return ");
                return false;
            }
            if (Settings.System.getInt(this.mContext.getContentResolver(), "easy_mode_switch", 1) == 0) {
                log("Easymode on, return ");
                return false;
            }
            for (String equalsIgnoreCase : this.mServiceIDsFromMetaData) {
                if (equalsIgnoreCase.equalsIgnoreCase(str)) {
                    return true;
                }
            }
        } else {
            String substring = str.substring(5);
            if ("callunanswered".equalsIgnoreCase(substring)) {
                return this.mPostCallAuth[activeDataPhoneId];
            }
            if ("callcomposer".equalsIgnoreCase(substring)) {
                return this.mComposerAuth[activeDataPhoneId];
            }
            if ("sharedmap".equalsIgnoreCase(substring)) {
                return this.mSharedMapAuth[activeDataPhoneId];
            }
            if ("sharedsketch".equalsIgnoreCase(substring)) {
                return this.mSharedSketchAuth[activeDataPhoneId];
            }
            for (String equalsIgnoreCase2 : this.mServiceIDsFromMetaData) {
                if (equalsIgnoreCase2.equalsIgnoreCase(str)) {
                    return true;
                }
            }
        }
        return false;
    }

    public void sendInstantMultimediaMessage(String str, ImsUri imsUri, byte[] bArr, String str2) {
        log("sendInstantMultimediaMessage,serviceId= " + str + ",contact=" + imsUri + ",contentType=" + str2);
        ImSession messagingSession = getMessagingSession(str, imsUri);
        if (messagingSession != null) {
            messagingSession.sendImMessage(createOutgoingMessage(messagingSession.getChatId(), imsUri, bArr, str2));
        }
    }

    public static class AppInfo {
        public static Hashtable<String, AppInfo> ALL = new Hashtable<>();
        private Hashtable<String, List<String>> mExtTable = new Hashtable<>();

        AppInfo(String str) {
            ALL.put(str, this);
        }

        public void addType(String str, List<String> list) {
            if (!this.mExtTable.containsKey(str)) {
                this.mExtTable.put(str, list);
            }
        }
    }

    public ImsRegistration getImsRegistration() {
        if (this.mRegistrationId != -1) {
            return ImsRegistry.getRegistrationManager().getRegistrationInfo(this.mRegistrationId);
        }
        return null;
    }

    public Context getContext() {
        return this.mContext;
    }

    public boolean isWifiConnected() {
        ConnectivityManager connectivityManager = (ConnectivityManager) getContext().getSystemService("connectivity");
        Network activeNetwork = connectivityManager.getActiveNetwork();
        if (activeNetwork == null) {
            Log.i(LOG_TAG, "isWifiConnected: Default NW is null");
            return false;
        }
        NetworkCapabilities networkCapabilities = connectivityManager.getNetworkCapabilities(activeNetwork);
        if (networkCapabilities == null || !networkCapabilities.hasCapability(12) || !networkCapabilities.hasTransport(1)) {
            return false;
        }
        return true;
    }

    public IRcsBigDataProcessor getBigDataProcessor() {
        return this.mBigDataProcessor;
    }

    public IMnoStrategy getRcsStrategy() {
        return RcsPolicyManager.getRcsStrategy(SimUtil.getSimSlotPriority());
    }

    public IMnoStrategy getRcsStrategy(int i) {
        return RcsPolicyManager.getRcsStrategy(i);
    }

    public MessageBase getMessage(int i) {
        return ImCache.getInstance().getMessage(i);
    }

    public MessageBase getMessage(String str, ImDirection imDirection, String str2) {
        return ImCache.getInstance().getMessage(str, imDirection, str2);
    }

    public List<MessageBase> getMessages(Collection<String> collection) {
        return ImCache.getInstance().getMessages(collection);
    }

    public List<MessageBase> getMessages(Collection<String> collection, ImDirection imDirection, String str) {
        return ImCache.getInstance().getMessages(collection, imDirection, str);
    }

    public MessageBase getPendingMessage(int i) {
        return ImCache.getInstance().getPendingMessage(i);
    }

    public List<MessageBase> getAllPendingMessages(String str) {
        return ImCache.getInstance().getAllPendingMessages(str);
    }

    public String onRequestIncomingFtTransferPath() {
        File externalFilesDir = this.mContext.getExternalFilesDir((String) null);
        if (externalFilesDir != null) {
            return externalFilesDir.getAbsolutePath();
        }
        return null;
    }

    public Network getNetwork(int i) {
        ImsRegistration imsRegistration = getImsRegistration(i);
        if (imsRegistration == null || this.mConfig.isFtHttpOverDefaultPdn()) {
            return null;
        }
        return imsRegistration.getNetwork();
    }

    public Set<ImsUri> getOwnUris(int i) {
        HashSet hashSet = new HashSet();
        ImsRegistration imsRegistration = getImsRegistration();
        if (imsRegistration != null) {
            for (NameAddr uri : imsRegistration.getImpuList()) {
                hashSet.add(this.mUriGenerator.normalize(uri.getUri()));
            }
        }
        return hashSet;
    }

    public void onCallStateChanged(int i, List<ICall> list) {
        this.mCallList.clear();
        int i2 = 0;
        for (ICall next : list) {
            if (next.isConnected()) {
                i2++;
                ImsUri normalizedUri = this.mUriGenerator.getNormalizedUri(next.getNumber(), true);
                if (normalizedUri != null && !this.mCallList.contains(normalizedUri)) {
                    this.mCallList.add(normalizedUri);
                }
            }
        }
        String str = LOG_TAG;
        Log.d(str, "nConnecteCalls = " + i2);
        if (i2 > 1) {
            this.mCallList.clear();
        }
    }

    private boolean getActiveCall(ImsUri imsUri) {
        ImsUri normalizedUri = this.mUriGenerator.getNormalizedUri(imsUri.getMsisdn(), true);
        for (ImsUri next : this.mCallList) {
            if (next != null && next.equals(normalizedUri)) {
                return true;
            }
        }
        return false;
    }

    private synchronized void updateFeatures(int i) {
        String str = LOG_TAG;
        Log.d(str, "updateFeatures: phoneId = " + i);
        boolean z = false;
        if (!(DmConfigHelper.getImsSwitchValue(this.mContext, DeviceConfigManager.RCS, i) == 1)) {
            Log.d(str, "updateFeatures: RCS is disabled, return");
            this.mEnabledFeatures[i] = 0;
            return;
        }
        this.mCallComposerTimerIdle[i] = RcsConfigurationHelper.readIntParam(this.mContext, ImsUtil.getPathWithPhoneId(ConfigConstants.ConfigTable.OTHER_CALL_COMPOSER_TIMER_IDLE, i), Integer.valueOf(MNO.EVR_ESN)).intValue();
        log("updateFeatures: mCallComposerTimerIdle=" + this.mCallComposerTimerIdle[i]);
        int intValue = RcsConfigurationHelper.readIntParam(this.mContext, ImsUtil.getPathWithPhoneId(ConfigConstants.ConfigTable.SERVICES_COMPOSER_AUTH, i), 0).intValue();
        boolean[] zArr = this.mComposerAuth;
        if (intValue == 1 || intValue == 3) {
            z = true;
        }
        zArr[i] = z;
        log("updateFeatures: Composer enable :" + this.mComposerAuth[i]);
        boolean[] zArr2 = this.mSharedMapAuth;
        Context context = this.mContext;
        String pathWithPhoneId = ImsUtil.getPathWithPhoneId(ConfigConstants.ConfigTable.SERVICES_SHARED_MAP_AUTH, i);
        Boolean bool = Boolean.FALSE;
        zArr2[i] = RcsConfigurationHelper.readBoolParam(context, pathWithPhoneId, bool).booleanValue();
        log("updateFeatures: SharedMapAuth enable " + this.mSharedMapAuth[i]);
        this.mSharedSketchAuth[i] = RcsConfigurationHelper.readBoolParam(this.mContext, ImsUtil.getPathWithPhoneId(ConfigConstants.ConfigTable.SERVICES_SHARED_SKETCH_AUTH, i), bool).booleanValue();
        log("updateFeatures: SharedSketchAuth enable " + this.mSharedSketchAuth[i]);
        this.mPostCallAuth[i] = RcsConfigurationHelper.readBoolParam(this.mContext, ImsUtil.getPathWithPhoneId(ConfigConstants.ConfigTable.SERVICES_POST_CALL_AUTH, i), bool).booleanValue();
        log("updateFeatures: PostCallAuth enable " + this.mPostCallAuth[i]);
        long[] jArr = this.mEnabledFeatures;
        jArr[i] = 0;
        if (this.mComposerAuth[i]) {
            jArr[i] = Capabilities.FEATURE_ENRICHED_CALL_COMPOSER | 0;
            this.mRegisteredServices.add(SipMsg.FEATURE_TAG_ENRICHED_CALL_COMPOSER);
        }
        if (this.mSharedMapAuth[i]) {
            long[] jArr2 = this.mEnabledFeatures;
            jArr2[i] = jArr2[i] | Capabilities.FEATURE_ENRICHED_SHARED_MAP;
            this.mRegisteredServices.add(SipMsg.FEATURE_TAG_ENRICHED_SHARED_MAP);
        }
        if (this.mSharedSketchAuth[i]) {
            long[] jArr3 = this.mEnabledFeatures;
            jArr3[i] = jArr3[i] | Capabilities.FEATURE_ENRICHED_SHARED_SKETCH;
            this.mRegisteredServices.add(SipMsg.FEATURE_TAG_ENRICHED_SHARED_SKETCH);
        }
        if (this.mPostCallAuth[i]) {
            long[] jArr4 = this.mEnabledFeatures;
            jArr4[i] = jArr4[i] | Capabilities.FEATURE_ENRICHED_POST_CALL;
            this.mRegisteredServices.add(SipMsg.FEATURE_TAG_ENRICHED_POST_CALL);
        }
        log("updateFeatures: mEnabledFeatures=" + this.mEnabledFeatures[i]);
    }
}
