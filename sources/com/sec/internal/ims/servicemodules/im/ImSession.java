package com.sec.internal.ims.servicemodules.im;

import android.content.Context;
import android.net.Network;
import android.os.Message;
import android.os.PowerManager;
import android.text.TextUtils;
import android.util.ArrayMap;
import android.util.Log;
import com.sec.ims.util.ImsUri;
import com.sec.internal.constants.ims.MIMEContentType;
import com.sec.internal.constants.ims.SipMsg;
import com.sec.internal.constants.ims.servicemodules.im.ChatData;
import com.sec.internal.constants.ims.servicemodules.im.ChatMode;
import com.sec.internal.constants.ims.servicemodules.im.ImCacheAction;
import com.sec.internal.constants.ims.servicemodules.im.ImConstants;
import com.sec.internal.constants.ims.servicemodules.im.ImDirection;
import com.sec.internal.constants.ims.servicemodules.im.ImError;
import com.sec.internal.constants.ims.servicemodules.im.ImIconData;
import com.sec.internal.constants.ims.servicemodules.im.ImParticipant;
import com.sec.internal.constants.ims.servicemodules.im.ImSubjectData;
import com.sec.internal.constants.ims.servicemodules.im.NotificationStatus;
import com.sec.internal.constants.ims.servicemodules.im.SupportedFeature;
import com.sec.internal.constants.ims.servicemodules.im.event.FtIncomingSessionEvent;
import com.sec.internal.constants.ims.servicemodules.im.event.ImComposingEvent;
import com.sec.internal.constants.ims.servicemodules.im.event.ImIncomingMessageEvent;
import com.sec.internal.constants.ims.servicemodules.im.event.ImIncomingSessionEvent;
import com.sec.internal.constants.ims.servicemodules.im.event.ImSessionClosedEvent;
import com.sec.internal.constants.ims.servicemodules.im.event.ImSessionConferenceInfoUpdateEvent;
import com.sec.internal.constants.ims.servicemodules.im.event.ImSessionEstablishedEvent;
import com.sec.internal.constants.ims.servicemodules.im.event.SendImdnFailedEvent;
import com.sec.internal.constants.ims.servicemodules.im.event.SendMessageFailedEvent;
import com.sec.internal.constants.ims.servicemodules.im.params.AcceptImSessionParams;
import com.sec.internal.constants.ims.servicemodules.im.params.RejectImSessionParams;
import com.sec.internal.constants.ims.servicemodules.im.params.SendSlmMessageParams;
import com.sec.internal.constants.ims.servicemodules.im.reason.CancelReason;
import com.sec.internal.constants.ims.servicemodules.im.reason.ImErrorReason;
import com.sec.internal.constants.ims.servicemodules.im.reason.ImSessionClosedReason;
import com.sec.internal.constants.ims.servicemodules.im.reason.ImSessionRejectReason;
import com.sec.internal.constants.ims.servicemodules.im.reason.ImSessionStopReason;
import com.sec.internal.constants.ims.servicemodules.im.result.Result;
import com.sec.internal.constants.ims.servicemodules.im.result.SendMessageResult;
import com.sec.internal.helper.AsyncResult;
import com.sec.internal.helper.FileUtils;
import com.sec.internal.helper.IState;
import com.sec.internal.helper.PreciseAlarmManager;
import com.sec.internal.helper.Preconditions;
import com.sec.internal.helper.PublicAccountUri;
import com.sec.internal.helper.SimUtil;
import com.sec.internal.helper.StateMachine;
import com.sec.internal.ims.core.sim.SimManagerFactory;
import com.sec.internal.ims.servicemodules.im.data.event.ImSessionEvent;
import com.sec.internal.ims.servicemodules.im.data.info.ImSessionInfo;
import com.sec.internal.ims.servicemodules.im.interfaces.IGetter;
import com.sec.internal.ims.servicemodules.im.listener.ImSessionListener;
import com.sec.internal.ims.servicemodules.im.strategy.IMnoStrategy;
import com.sec.internal.ims.settings.RcsPolicySettings;
import com.sec.internal.ims.util.ChatbotUriUtil;
import com.sec.internal.ims.util.StringIdGenerator;
import com.sec.internal.ims.util.UriGenerator;
import com.sec.internal.imscr.LogClass;
import com.sec.internal.interfaces.ims.core.ISimManager;
import com.sec.internal.interfaces.ims.servicemodules.im.IImServiceInterface;
import com.sec.internal.interfaces.ims.servicemodules.im.ISlmServiceInterface;
import com.sec.internal.log.IMSLog;
import java.io.File;
import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.Date;
import java.util.EnumSet;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.TreeMap;

public class ImSession extends StateMachine {
    protected static final long CLOSE_SESSION_TIMEOUT_TIMER = 180000;
    private static final long DEFAULT_WAKE_LOCK_TIMEOUT = 3000;
    protected static final int DEFER_WITHOUT_STARTSESSION = 0;
    protected static final int DEFER_WITH_STARTSESSION = 1;
    private static final String LOG_TAG = "ImSession";
    private static final int MESSAGE_REVOKE_OPERATION_TIME = 10000;
    private static final int REQUEST_THRESHOLD_TIME = 5000;
    private static final int SEND_MESSAGE_THRESHOLD_TIME = 300;
    protected final List<String> mAcceptTypes;
    protected final List<String> mAcceptWrappedTypes;
    private final ChatData mChatData;
    private ChatFallbackMech mChatFallbackMech;
    private final String mChatId;
    protected ImSessionClosedEvent mClosedEvent;
    protected ImSessionClosedReason mClosedReason;
    protected final ImSessionClosedState mClosedState;
    private final ImSessionClosingState mClosingState;
    private final Set<ImsUri> mComposingActiveUris;
    protected int mComposingNotificationInterval;
    private ConferenceInfoUpdater mConferenceInfoUpdater;
    protected final ImConfig mConfig;
    protected final List<MessageBase> mCurrentCanceledMessages;
    protected final List<MessageBase> mCurrentMessages;
    private final ImSessionDefaultState mDefaultState;
    private final Map<IState, SessionState> mDetailedStateMap;
    private String mDeviceId;
    protected final ArrayList<ImSessionInfo> mEstablishedImSessionInfo;
    private final ImSessionEstablishedState mEstablishedState;
    protected final IGetter mGetter;
    protected final List<ImSessionInfo> mImSessionInfoList;
    protected final IImServiceInterface mImsService;
    protected Set<Message> mInProgressRequestCallbacks;
    private String mInReplyToContributionId;
    protected List<ImIncomingMessageEvent> mIncomingMessageEvents;
    private final ImSessionInitialState mInitialState;
    private ImsUri mInitiator;
    private String mInitiatorAlias;
    protected boolean mIsBlockedIncomingSession;
    protected boolean mIsComposing;
    protected boolean mIsOfflineGCInvitation;
    protected boolean mIsRevokeTimerRunning;
    private boolean mIsTimerExpired;
    private boolean mIsTokenUsed;
    protected String mLeaderParticipant;
    protected final ImSessionListener mListener;
    protected final ArrayDeque<MessageBase> mMessagesToSendDisplayNotification;
    private final Map<String, Integer> mNeedToRevokeMessages;
    private String mOwnImsi;
    protected final HashMap<ImsUri, ImParticipant> mParticipants;
    protected List<Message> mPendingEvents;
    protected final ArrayList<FtMessage> mPendingFileTransfer;
    private int mPhoneId;
    protected final ArrayList<FtMessage> mProcessingFileTransfer;
    private Object mRawHandle;
    protected List<String> mRemoteAcceptTypes;
    protected List<String> mRemoteAcceptWrappedTypes;
    private String mRequestMessageId;
    protected int mRetryTimer;
    private String mSdpContentType;
    private int mSendMessageResponseTimeout;
    private final String mServiceId;
    private final ISlmServiceInterface mSlmService;
    protected final ImSessionStartingState mStartingState;
    protected EnumSet<SupportedFeature> mSupportedFeatures;
    protected boolean mSwapUriType;
    private int mThreadId;
    protected UriGenerator mUriGenerator;
    private final PowerManager.WakeLock mWakeLock;

    protected enum ChatFallbackMech {
        NONE,
        MESSAGE_REVOCATION,
        NETWORK_INTERWORKING
    }

    public enum SessionState {
        INITIAL,
        STARTING,
        ESTABLISHED,
        CLOSING,
        CLOSED,
        FAILED_MEDIA
    }

    /* JADX WARNING: type inference failed for: r0v0, types: [com.sec.internal.ims.servicemodules.im.ImSession, com.sec.internal.helper.StateMachine] */
    protected ImSession(ImSessionBuilder imSessionBuilder) {
        HashMap<ImsUri, ImParticipant> hashMap;
        ImSession imSession;
        ImSessionBuilder imSessionBuilder2 = imSessionBuilder;
        StringBuilder sb = new StringBuilder();
        sb.append("ImSession#");
        ChatData chatData = imSessionBuilder2.mChatData;
        sb.append((chatData == null ? imSessionBuilder2.mChatId : chatData.getChatId()).substring(0, 4));
        ? stateMachine = new StateMachine(sb.toString(), imSessionBuilder2.mLooper);
        stateMachine.mProcessingFileTransfer = new ArrayList<>();
        stateMachine.mPendingFileTransfer = new ArrayList<>();
        stateMachine.mImSessionInfoList = new ArrayList();
        stateMachine.mEstablishedImSessionInfo = new ArrayList<>();
        HashMap<ImsUri, ImParticipant> hashMap2 = new HashMap<>();
        stateMachine.mParticipants = hashMap2;
        stateMachine.mComposingActiveUris = new HashSet();
        stateMachine.mDetailedStateMap = new ArrayMap();
        stateMachine.mMessagesToSendDisplayNotification = new ArrayDeque<>();
        stateMachine.mCurrentMessages = new ArrayList();
        stateMachine.mCurrentCanceledMessages = new ArrayList();
        stateMachine.mNeedToRevokeMessages = new HashMap();
        stateMachine.mThreadId = -1;
        stateMachine.mRetryTimer = -1;
        stateMachine.mOwnImsi = "";
        stateMachine.mPhoneId = 0;
        stateMachine.mClosedReason = ImSessionClosedReason.NONE;
        stateMachine.mIncomingMessageEvents = new ArrayList();
        stateMachine.mComposingNotificationInterval = 120;
        stateMachine.mChatFallbackMech = ChatFallbackMech.NONE;
        stateMachine.mInProgressRequestCallbacks = new HashSet();
        stateMachine.mPendingEvents = new ArrayList();
        stateMachine.mListener = imSessionBuilder2.mListener;
        stateMachine.mConfig = imSessionBuilder2.mConfig;
        stateMachine.mImsService = imSessionBuilder2.mImsService;
        stateMachine.mSlmService = imSessionBuilder2.mSlmService;
        stateMachine.mUriGenerator = imSessionBuilder2.mUriGenerator;
        ChatData chatData2 = imSessionBuilder2.mChatData;
        if (chatData2 != null) {
            stateMachine.mChatData = chatData2;
            hashMap = hashMap2;
            imSession = stateMachine;
        } else {
            hashMap = hashMap2;
            ImSession imSession2 = this;
            imSession2.mChatData = new ChatData(imSessionBuilder2.mChatId, imSessionBuilder2.mOwnNumber, imSessionBuilder2.mOwnGroupAlias, imSessionBuilder2.mSubject, imSessionBuilder2.mChatType, imSessionBuilder2.mDirection, imSessionBuilder2.mConversationId, imSessionBuilder2.mContributionId, imSessionBuilder2.mOwnIMSI, imSessionBuilder2.mIconPath, imSessionBuilder2.mChatMode, imSessionBuilder2.mSessionUri, imSessionBuilder2.mCreatedBy, imSessionBuilder2.mInvitedBy);
            imSession = imSession2;
        }
        imSession.mChatId = imSession.mChatData.getChatId();
        hashMap.putAll(imSessionBuilder2.mParticipants);
        if (imSessionBuilder2.mDirection == ImDirection.INCOMING) {
            for (ImsUri next : imSessionBuilder2.mParticipantsUri) {
                imSession.mParticipants.put(next, new ImParticipant(imSession.mChatId, ImParticipant.Status.INVITED, next));
            }
        } else {
            for (ImsUri next2 : imSessionBuilder2.mParticipantsUri) {
                imSession.mParticipants.put(next2, new ImParticipant(imSession.mChatId, ImParticipant.Status.INITIAL, next2));
            }
        }
        int phoneId = getPhoneId();
        imSession.mPhoneId = phoneId;
        imSession.mSdpContentType = imSessionBuilder2.mSdpContentType;
        imSession.mThreadId = imSessionBuilder2.mThreadId;
        imSession.mRequestMessageId = imSessionBuilder2.mRequestMessageId;
        imSession.mRawHandle = imSessionBuilder2.mRawHandle;
        imSession.mGetter = imSessionBuilder2.mGetter;
        imSession.mSendMessageResponseTimeout = imSession.getRcsStrategy(phoneId).intSetting(RcsPolicySettings.RcsPolicy.SENDMSG_RESP_TIMEOUT);
        imSession.mServiceId = imSessionBuilder2.mServiceId;
        imSession.mAcceptTypes = imSessionBuilder2.mAcceptTypes;
        imSession.mAcceptWrappedTypes = imSessionBuilder2.mAcceptWrappedTypes;
        imSession.mNeedToRevokeMessages.putAll(imSessionBuilder2.mNeedToRevokeMessages);
        PowerManager.WakeLock newWakeLock = ((PowerManager) getContext().getSystemService("power")).newWakeLock(1, getName());
        imSession.mWakeLock = newWakeLock;
        newWakeLock.setReferenceCounted(true);
        imSession.mDefaultState = new ImSessionDefaultState(imSession.mPhoneId, imSession);
        imSession.mInitialState = new ImSessionInitialState(imSession.mPhoneId, imSession);
        imSession.mStartingState = new ImSessionStartingState(imSession.mPhoneId, imSession);
        imSession.mEstablishedState = new ImSessionEstablishedState(imSession.mPhoneId, imSession);
        imSession.mClosingState = new ImSessionClosingState(imSession.mPhoneId, imSession);
        imSession.mClosedState = new ImSessionClosedState(imSession.mPhoneId, imSession);
        imSession.mConferenceInfoUpdater = null;
        initState();
    }

    /* access modifiers changed from: protected */
    public void acquireWakeLock(Object obj) {
        logi("acquireWakeLock: " + getChatId() + " : " + obj);
        this.mWakeLock.acquire(3000);
    }

    /* access modifiers changed from: protected */
    public void releaseWakeLock(Object obj) {
        if (this.mWakeLock.isHeld()) {
            logi("releaseWakeLock: " + getChatId() + " : " + obj);
            this.mWakeLock.release();
        }
    }

    /* access modifiers changed from: protected */
    public void updateSessionInfo(ImSessionInfo imSessionInfo) {
        setRawHandle(imSessionInfo.mRawHandle);
        setContributionId(imSessionInfo.mContributionId);
        setConversationId(imSessionInfo.mConversationId);
        this.mInReplyToContributionId = imSessionInfo.mInReplyToContributionId;
        this.mSdpContentType = imSessionInfo.mSdpContentType;
        setSessionUri(imSessionInfo.mSessionUri);
        setDirection(imSessionInfo.mDirection);
        this.mChatData.triggerObservers(ImCacheAction.UPDATED);
    }

    /* access modifiers changed from: protected */
    public void updateConferenceTimestamp(ImSessionConferenceInfoUpdateEvent imSessionConferenceInfoUpdateEvent) {
        if (!TextUtils.isEmpty(imSessionConferenceInfoUpdateEvent.mTimeStamp)) {
            this.mChatData.setInsertedTimeStamp(Long.valueOf(imSessionConferenceInfoUpdateEvent.mTimeStamp).longValue());
            this.mChatData.triggerObservers(ImCacheAction.UPDATED);
        }
    }

    /* access modifiers changed from: protected */
    public Context getContext() {
        return this.mGetter.getContext();
    }

    /* access modifiers changed from: protected */
    public ChatData getChatData() {
        return this.mChatData;
    }

    public String getChatId() {
        return this.mChatId;
    }

    /* access modifiers changed from: protected */
    public String getOwnPhoneNum() {
        return this.mChatData.getOwnPhoneNum();
    }

    /* access modifiers changed from: protected */
    public void setOwnPhoneNum(String str) {
        this.mChatData.setOwnPhoneNum(str);
    }

    public String getOwnImsi() {
        return this.mChatData.getOwnIMSI();
    }

    /* access modifiers changed from: protected */
    public void setOwnImsi(String str) {
        this.mChatData.setOwnIMSI(str);
    }

    public int getId() {
        return this.mChatData.getId();
    }

    public int getChatStateId() {
        return this.mChatData.getState().getId();
    }

    /* access modifiers changed from: protected */
    public void updateChatState(ChatData.State state) {
        this.mChatData.updateState(state);
    }

    /* access modifiers changed from: protected */
    public boolean isChatState(ChatData.State state) {
        return getChatStateId() == state.getId();
    }

    public boolean isGroupChat() {
        return this.mChatData.isGroupChat();
    }

    /* access modifiers changed from: protected */
    public int getPhoneId() {
        int simSlotPriority = SimUtil.getSimSlotPriority();
        int phoneId = SimManagerFactory.getPhoneId(this.mChatData.getOwnIMSI());
        return phoneId != -1 ? phoneId : simSlotPriority;
    }

    /* access modifiers changed from: protected */
    public boolean isChatbotRole() {
        return this.mChatData.isChatbotRole();
    }

    /* access modifiers changed from: protected */
    public boolean isBotSessionAnonymized() {
        return !this.mConfig.getBotPrivacyDisable() && isChatbotRole() && getIsTokenUsed();
    }

    /* access modifiers changed from: protected */
    /* JADX WARNING: Code restructure failed: missing block: B:4:0x0017, code lost:
        r2 = r2.mInitiator;
     */
    /* Code decompiled incorrectly, please refer to instructions dump. */
    public boolean isChatbotManualAcceptUsed() {
        /*
            r2 = this;
            boolean r0 = r2.isChatbotRole()
            if (r0 == 0) goto L_0x0025
            int r0 = r2.getPhoneId()
            com.sec.internal.ims.servicemodules.im.strategy.IMnoStrategy r0 = r2.getRcsStrategy(r0)
            java.lang.String r1 = "use_chatbot_manualaccept"
            boolean r0 = r0.boolSetting(r1)
            if (r0 == 0) goto L_0x0025
            com.sec.ims.util.ImsUri r2 = r2.mInitiator
            if (r2 == 0) goto L_0x0025
            com.sec.ims.util.ImsUri$UriType r2 = r2.getUriType()
            com.sec.ims.util.ImsUri$UriType r0 = com.sec.ims.util.ImsUri.UriType.SIP_URI
            if (r2 != r0) goto L_0x0025
            r2 = 1
            goto L_0x0026
        L_0x0025:
            r2 = 0
        L_0x0026:
            return r2
        */
        throw new UnsupportedOperationException("Method not decompiled: com.sec.internal.ims.servicemodules.im.ImSession.isChatbotManualAcceptUsed():boolean");
    }

    /* access modifiers changed from: protected */
    public void updateIsChatbotRole(boolean z) {
        ImsUri remoteUri = getRemoteUri();
        if (!(z == this.mChatData.isChatbotRole() || remoteUri == null)) {
            if (z) {
                ImCache.getInstance().addToChatbotRoleUris(remoteUri, this.mChatData.getOwnIMSI());
            } else {
                ImCache.getInstance().removeFromChatbotRoleUris(remoteUri, this.mChatData.getOwnIMSI());
            }
        }
        this.mChatData.updateIsChatbotRole(z);
    }

    /* access modifiers changed from: protected */
    public ChatData.ChatType getChatType() {
        return this.mChatData.getChatType();
    }

    /* access modifiers changed from: protected */
    public void updateChatType(ChatData.ChatType chatType) {
        this.mChatData.updateChatType(chatType);
    }

    public ChatMode getChatMode() {
        return this.mChatData.getChatMode();
    }

    public String getSubject() {
        return this.mChatData.getSubject();
    }

    private void setSubject(String str) {
        this.mChatData.setSubject(str);
    }

    /* access modifiers changed from: protected */
    public ImSubjectData getSubjectData() {
        return this.mChatData.getSubjectData();
    }

    /* access modifiers changed from: protected */
    public ImIconData getIconData() {
        return this.mChatData.getIconData();
    }

    /* access modifiers changed from: protected */
    public String getInitiatorAlias() {
        return this.mInitiatorAlias;
    }

    /* access modifiers changed from: protected */
    public void setInitiatorAlias(String str) {
        this.mInitiatorAlias = str;
    }

    public boolean getIsTokenUsed() {
        return this.mIsTokenUsed;
    }

    /* access modifiers changed from: protected */
    public void setIsTokenUsed(boolean z) {
        this.mIsTokenUsed = z;
    }

    /* access modifiers changed from: protected */
    public String getDeviceId() {
        return this.mDeviceId;
    }

    /* access modifiers changed from: protected */
    public void setDeviceId(String str) {
        this.mDeviceId = str;
    }

    /* access modifiers changed from: protected */
    public String getUserAlias() {
        return getUserAlias(false);
    }

    /* access modifiers changed from: protected */
    public String getUserAlias(boolean z) {
        return this.mGetter.getUserAlias(this.mPhoneId, z);
    }

    /* access modifiers changed from: protected */
    public boolean isMuted() {
        return this.mChatData.isMuted();
    }

    /* access modifiers changed from: protected */
    public String getContributionId() {
        return this.mChatData.getContributionId();
    }

    /* access modifiers changed from: protected */
    public void setContributionId(String str) {
        this.mChatData.setContributionId(str);
    }

    /* access modifiers changed from: protected */
    public boolean isBroadcastMsg(MessageBase messageBase) {
        return this.mChatData.getChatType() == ChatData.ChatType.ONE_TO_MANY_CHAT || (messageBase != null && messageBase.isBroadcastMsg());
    }

    /* access modifiers changed from: protected */
    public ImsUri getSessionUri() {
        return this.mChatData.getSessionUri();
    }

    /* access modifiers changed from: protected */
    public void setSessionUri(ImsUri imsUri) {
        this.mChatData.setSessionUri(imsUri);
    }

    public ImSessionClosedEvent getImSessionClosedEvent() {
        return this.mClosedEvent;
    }

    public Set<ImParticipant> getParticipants() {
        return new HashSet(this.mParticipants.values());
    }

    public List<String> getParticipantsString() {
        ArrayList arrayList = new ArrayList();
        for (ImsUri imsUri : this.mParticipants.keySet()) {
            arrayList.add(imsUri.toString());
        }
        return arrayList;
    }

    /* access modifiers changed from: protected */
    public ImParticipant getParticipant(ImsUri imsUri) {
        if (imsUri != null) {
            return this.mParticipants.get(imsUri);
        }
        return null;
    }

    /* access modifiers changed from: protected */
    public String getRequestMessageId() {
        return this.mRequestMessageId;
    }

    /* access modifiers changed from: protected */
    public Set<ImsUri> getParticipantsUri() {
        return new HashSet(this.mParticipants.keySet());
    }

    /* access modifiers changed from: protected */
    public int getParticipantsSize() {
        return this.mParticipants.size();
    }

    public ImsUri getRemoteUri() {
        if (this.mParticipants.size() == 1) {
            return this.mParticipants.keySet().iterator().next();
        }
        return null;
    }

    public int getMaxParticipantsCount() {
        return this.mChatData.getMaxParticipantsCount();
    }

    /* access modifiers changed from: protected */
    public int getThreadId() {
        return this.mThreadId;
    }

    /* access modifiers changed from: protected */
    public String getSdpContentType() {
        return this.mSdpContentType;
    }

    /* access modifiers changed from: protected */
    public void updateSubjectData(ImSubjectData imSubjectData) {
        this.mChatData.updateSubjectData(imSubjectData);
    }

    /* access modifiers changed from: protected */
    public void updateIconData(ImIconData imIconData) {
        this.mChatData.updateIconData(imIconData);
    }

    /* access modifiers changed from: protected */
    public String getConversationId() {
        return this.mChatData.getConversationId();
    }

    /* access modifiers changed from: protected */
    public void setConversationId(String str) {
        this.mChatData.setConversationId(str);
    }

    /* access modifiers changed from: protected */
    public String getInReplyToContributionId() {
        return this.mInReplyToContributionId;
    }

    /* access modifiers changed from: protected */
    public void setInReplyToContributionId(String str) {
        this.mInReplyToContributionId = str;
    }

    public ImDirection getDirection() {
        return this.mChatData.getDirection();
    }

    /* access modifiers changed from: protected */
    public void setDirection(ImDirection imDirection) {
        this.mChatData.setDirection(imDirection);
    }

    /* access modifiers changed from: protected */
    public void updateParticipantsStatus(ImParticipant.Status status) {
        ArrayList arrayList = new ArrayList();
        for (ImParticipant next : this.mParticipants.values()) {
            if (next.getStatus() != status) {
                next.setStatus(status);
                arrayList.add(next);
            }
        }
        if (!arrayList.isEmpty()) {
            this.mListener.onParticipantsUpdated(this, arrayList);
        }
    }

    /* access modifiers changed from: protected */
    public void updateParticipantAlias(String str, ImParticipant imParticipant) {
        if (imParticipant == null) {
            IMSLog.e(LOG_TAG, "updateParticipantAlias, skipping update");
        } else if (!hasImSessionInfo(ImSessionInfo.SessionType.NORMAL) && TextUtils.isEmpty(str) && !this.mConfig.getRealtimeUserAliasAuth()) {
            IMSLog.i(LOG_TAG, "updateParticipantAlias, SnF session and alias empty - do not update");
        } else if (imParticipant.getUserAlias() == null || !imParticipant.getUserAlias().equals(str)) {
            imParticipant.setUserAlias(str);
            ArrayList arrayList = new ArrayList();
            arrayList.add(imParticipant);
            this.mListener.onParticipantsUpdated(this, arrayList);
            this.mListener.onParticipantAliasUpdated(this.mChatId, imParticipant);
        } else {
            IMSLog.i(LOG_TAG, "updateParticipantAlias, participant alias is up to date");
        }
    }

    /* access modifiers changed from: protected */
    public boolean isReusable() {
        return this.mChatData.isReusable();
    }

    /* access modifiers changed from: protected */
    public boolean isRejoinable() {
        return isGroupChat() && getSessionUri() != null;
    }

    public boolean hasImSessionInfo(Object obj) {
        return getImSessionInfo(obj) != null;
    }

    public String getServiceId() {
        return this.mServiceId;
    }

    private void initState() {
        addState(this.mDefaultState);
        addState(this.mInitialState, this.mDefaultState);
        addState(this.mStartingState, this.mDefaultState);
        addState(this.mEstablishedState, this.mDefaultState);
        addState(this.mClosingState, this.mDefaultState);
        addState(this.mClosedState, this.mInitialState);
        setInitialState(this.mInitialState);
        start();
        this.mDetailedStateMap.put(this.mInitialState, SessionState.INITIAL);
        this.mDetailedStateMap.put(this.mStartingState, SessionState.STARTING);
        this.mDetailedStateMap.put(this.mEstablishedState, SessionState.ESTABLISHED);
        this.mDetailedStateMap.put(this.mClosingState, SessionState.CLOSING);
        this.mDetailedStateMap.put(this.mClosedState, SessionState.CLOSED);
    }

    public SessionState getDetailedState() {
        return this.mDetailedStateMap.get(getCurrentState());
    }

    /* access modifiers changed from: protected */
    public IState getCurrentSessionState() {
        return getCurrentState();
    }

    public void startSession() {
        if (isBroadcastMsg((MessageBase) null)) {
            logi("broadcast message just use SLM, should never start session");
        } else {
            sendMessage(obtainMessage(1001));
        }
    }

    public void processIncomingSession(ImIncomingSessionEvent imIncomingSessionEvent) {
        Object obj = imIncomingSessionEvent.mRawHandle;
        if (obj != null) {
            acquireWakeLock(obj);
            if (imIncomingSessionEvent.mIsDeferred) {
                sendMessage(obtainMessage(1010, (Object) imIncomingSessionEvent));
            } else {
                sendMessage(obtainMessage(1005, (Object) imIncomingSessionEvent));
            }
        }
    }

    public void acceptSession(boolean z) {
        sendMessage(obtainMessage(1006, (Object) Boolean.valueOf(z)));
    }

    public void rejectSession() {
        sendMessage(obtainMessage(1008));
    }

    /* access modifiers changed from: protected */
    public void rejectSession(int i) {
        sendMessage(obtainMessage(1008, (Object) Integer.valueOf(i)));
    }

    public void receiveSessionEstablished(ImSessionEstablishedEvent imSessionEstablishedEvent) {
        sendMessage(obtainMessage(1003, (Object) imSessionEstablishedEvent));
    }

    public void receiveSessionClosed(ImSessionClosedEvent imSessionClosedEvent) {
        sendMessage(obtainMessage(1014, (Object) imSessionClosedEvent));
    }

    /* access modifiers changed from: protected */
    public void receiveConferenceInfo(ImSessionConferenceInfoUpdateEvent imSessionConferenceInfoUpdateEvent) {
        sendMessage(obtainMessage((int) ImSessionEvent.CONFERENCE_INFO_UPDATED, (Object) imSessionConferenceInfoUpdateEvent));
    }

    /* access modifiers changed from: protected */
    public void receiveComposingNotification(ImComposingEvent imComposingEvent) {
        int i = imComposingEvent.mInterval;
        if (i != 0) {
            this.mComposingNotificationInterval = i;
        }
        ImsUri normalizeUri = this.mGetter.normalizeUri(ImsUri.parse(imComposingEvent.mUri));
        if (imComposingEvent.mIsComposing) {
            this.mComposingActiveUris.add(normalizeUri);
            removeMessages(ImSessionEvent.RECEIVE_ISCOMPOSING_TIMEOUT);
            sendMessageDelayed((int) ImSessionEvent.RECEIVE_ISCOMPOSING_TIMEOUT, ((long) this.mComposingNotificationInterval) * 1000);
            checkAndUpdateSessionTimeout();
            return;
        }
        this.mComposingActiveUris.remove(normalizeUri);
    }

    /* access modifiers changed from: protected */
    public void restartSession(int i, String str, String str2) {
        this.mThreadId = i;
        this.mRequestMessageId = str;
        setSubject(str2);
    }

    public void closeSession() {
        closeSession(true, getRcsStrategy(this.mPhoneId).getSessionStopReason(isGroupChat()));
    }

    /* access modifiers changed from: protected */
    public void closeSession(boolean z, ImSessionStopReason imSessionStopReason) {
        this.mChatData.updateIsReusable(z);
        this.mClosedState.mStopReason = imSessionStopReason;
        if (imSessionStopReason == ImSessionStopReason.VOLUNTARILY) {
            forceCancelFt(true, CancelReason.CANCELED_BY_USER);
        }
        sendMessage(obtainMessage(1012, (Object) imSessionStopReason));
    }

    /* access modifiers changed from: protected */
    public void forceCloseSession() {
        sendMessage(obtainMessage(1015));
    }

    /* access modifiers changed from: protected */
    public void addParticipants(List<ImsUri> list) {
        if (isGroupChat()) {
            sendMessage(obtainMessage(ImSessionEvent.ADD_PARTICIPANTS, 0, 0, list));
            return;
        }
        startSession();
        sendMessage(obtainMessage((int) ImSessionEvent.EXTEND_TO_GROUP_CHAT, (Object) list));
    }

    /* access modifiers changed from: protected */
    public void removeParticipants(List<ImsUri> list) {
        if (isGroupChat()) {
            sendMessage(obtainMessage(ImSessionEvent.REMOVE_PARTICIPANTS, 0, 0, list));
        }
    }

    /* access modifiers changed from: protected */
    public void changeGroupChatSubject(String str) {
        if (isGroupChat()) {
            sendMessage(obtainMessage((int) ImSessionEvent.CHANGE_GC_SUBJECT, (Object) str));
        }
    }

    /* access modifiers changed from: protected */
    public void changeGroupChatIcon(String str) {
        if (isGroupChat()) {
            sendMessage(obtainMessage((int) ImSessionEvent.CHANGE_GC_ICON, (Object) str));
        }
    }

    /* access modifiers changed from: protected */
    public void changeGroupAlias(String str) {
        if (isGroupChat()) {
            sendMessage(obtainMessage((int) ImSessionEvent.CHANGE_GROUP_ALIAS, (Object) str));
        }
    }

    /* access modifiers changed from: protected */
    public void changeGroupChatLeader(List<ImsUri> list) {
        if (isGroupChat()) {
            sendMessage(obtainMessage((int) ImSessionEvent.CHANGE_GC_LEADER, (Object) list));
        }
    }

    /* access modifiers changed from: protected */
    public void receiveDeliveryTimeout() {
        sendMessage(obtainMessage(ImSessionEvent.DELIVERY_TIMEOUT));
    }

    /* access modifiers changed from: protected */
    public ImsUri getInitiator() {
        return this.mInitiator;
    }

    /* access modifiers changed from: protected */
    public void setInitiator(ImsUri imsUri) {
        this.mInitiator = imsUri;
    }

    /* access modifiers changed from: protected */
    public ImsUri getCreatedBy() {
        return this.mChatData.getCreatedBy();
    }

    /* access modifiers changed from: protected */
    public void updateCreatedBy(ImsUri imsUri) {
        this.mChatData.updateCreatedBy(imsUri);
    }

    /* access modifiers changed from: protected */
    public ImsUri getInvitedBy() {
        return this.mChatData.getInvitedBy();
    }

    /* access modifiers changed from: protected */
    public void updateInvitedBy(ImsUri imsUri) {
        this.mChatData.updateInvitedBy(imsUri);
    }

    /* access modifiers changed from: protected */
    public void addParticipant(Collection<ImParticipant> collection) {
        for (ImParticipant next : collection) {
            this.mParticipants.put(next.getUri(), next);
        }
    }

    /* access modifiers changed from: protected */
    public void deleteParticipant(Collection<ImParticipant> collection) {
        for (ImParticipant uri : collection) {
            this.mParticipants.remove(uri.getUri());
        }
    }

    public void sendComposing(boolean z, int i) {
        this.mComposingNotificationInterval = i;
        sendMessage(obtainMessage((int) ImSessionEvent.SEND_ISCOMPOSING_NOTIFICATION, (Object) Boolean.valueOf(z)));
    }

    /* access modifiers changed from: protected */
    public Set<ImsUri> getComposingActiveUris() {
        return this.mComposingActiveUris;
    }

    public void sendImMessage(MessageBase messageBase) {
        logi("sendImMessage: ChatbotMessagingTech = " + messageBase.getChatbotMessagingTech());
        if (messageBase.getBody() != null) {
            if (messageBase.getChatbotMessagingTech() == ImConstants.ChatbotMessagingTech.UNKNOWN) {
                messageBase.setChatbotMessagingTech(getRcsStrategy(this.mPhoneId).checkChatbotMessagingTech(this.mConfig, isGroupChat(), getParticipantsUri()));
            }
            boolean z = true;
            if (messageBase.getChatbotMessagingTech() != ImConstants.ChatbotMessagingTech.NONE) {
                if (messageBase.getChatbotMessagingTech() == ImConstants.ChatbotMessagingTech.NOT_AVAILABLE) {
                    messageBase.onSendMessageDone(new Result(ImError.REMOTE_TEMPORARILY_UNAVAILABLE, Result.Type.NONE), new IMnoStrategy.StrategyResponse(IMnoStrategy.StatusCode.DISPLAY_ERROR));
                    return;
                }
                messageBase.setSlmSvcMsg(messageBase.getChatbotMessagingTech() == ImConstants.ChatbotMessagingTech.STANDALONE_MESSAGING);
            }
            if (!ChatbotUriUtil.hasChatbotUri(getParticipantsUri(), this.mPhoneId)) {
                if (this.mConfig.getChatEnabled() || this.mConfig.getSlmAuth() != ImConstants.SlmAuth.ENABLED) {
                    z = false;
                }
                messageBase.setSlmSvcMsg(z);
            }
            if (messageBase.getIsSlmSvcMsg()) {
                sendMessage(obtainMessage((int) ImSessionEvent.SEND_SLM_MESSAGE, (Object) messageBase));
            } else {
                sendMessage(obtainMessage((int) ImSessionEvent.SEND_MESSAGE, (Object) messageBase));
            }
            if ((messageBase instanceof ImMessage) && getRcsStrategy(this.mPhoneId).boolSetting(RcsPolicySettings.RcsPolicy.SUPPORT_SENDMSG_RESP_TIMEOUT)) {
                sendMessageDelayed(obtainMessage((int) ImSessionEvent.SEND_MESSAGE_RESPONSE_TIMEOUT, (Object) messageBase), ((long) this.mSendMessageResponseTimeout) * 1000);
            }
        }
    }

    /* access modifiers changed from: protected */
    public void setSessionTimeoutThreshold(MessageBase messageBase) {
        Preconditions.checkNotNull(messageBase, "msg cannot be null");
        if (messageBase instanceof ImMessage) {
            sendMessageDelayed(obtainMessage(1019, (Object) messageBase), 300000);
        }
    }

    /* access modifiers changed from: protected */
    public void receiveMessage(MessageBase messageBase, Object obj) {
        ImSessionInfo imSessionInfo;
        if (!(messageBase == null || obj == null || (imSessionInfo = getImSessionInfo(obj)) == null || !imSessionInfo.isSnFSession())) {
            imSessionInfo.mReceivedMessageIds.add(Integer.valueOf(messageBase.getId()));
        }
        sendMessage(obtainMessage((int) ImSessionEvent.RECEIVE_MESSAGE, (Object) messageBase));
    }

    /* access modifiers changed from: protected */
    public void cancelMessages(List<String> list) {
        List<MessageBase> messages = this.mGetter.getMessages(list, ImDirection.OUTGOING, (String) null);
        for (MessageBase next : messages) {
            if (getNeedToRevokeMessages().containsKey(next.getImdnId())) {
                next.updateRevocationStatus(ImConstants.RevocationStatus.NONE);
                removeMsgFromListForRevoke(next.getImdnId());
            }
            list.remove(next.getImdnId());
        }
        for (String onSendCanceledNotificationDone : list) {
            this.mListener.onSendCanceledNotificationDone(this.mChatId, onSendCanceledNotificationDone, false);
        }
        if (!messages.isEmpty()) {
            sendMessage(obtainMessage((int) ImSessionEvent.SEND_CANCELED_NOTIFICATION, (Object) messages));
        }
    }

    /* access modifiers changed from: protected */
    public void onSendImdnFailed(SendImdnFailedEvent sendImdnFailedEvent, MessageBase messageBase) {
        logi("onSendImdnFailed event: " + sendImdnFailedEvent + ", msg: " + messageBase);
        if ((messageBase instanceof ImMessage) || (messageBase instanceof FtHttpIncomingMessage)) {
            this.mClosedState.handleCloseSession(sendImdnFailedEvent.mRawHandle, ImSessionStopReason.INVOLUNTARILY);
            transitionToProperState();
        }
        NotificationStatus notificationStatus = messageBase.getNotificationStatus();
        if (notificationStatus == NotificationStatus.DELIVERED || notificationStatus == NotificationStatus.DISPLAYED) {
            messageBase.sendDeliveredNotification((Object) null, getConversationId(), getContributionId(), obtainMessage((int) ImSessionEvent.SEND_DELIVERED_NOTIFICATION_DONE, (Object) messageBase), getChatData().getOwnIMSI(), isGroupChat(), isBotSessionAnonymized());
            if (notificationStatus == NotificationStatus.DISPLAYED && isRespondDisplay()) {
                messageBase.sendDisplayedNotification((Object) null, getConversationId(), getContributionId(), obtainMessage((int) ImSessionEvent.SEND_DISPLAYED_NOTIFICATION_DONE, (Object) messageBase.toList()), getChatData().getOwnIMSI(), isGroupChat(), isBotSessionAnonymized());
            }
        }
    }

    /* access modifiers changed from: protected */
    public void onSendMessageHandleReportFailed(SendMessageFailedEvent sendMessageFailedEvent, MessageBase messageBase) {
        Message obtainMessage = obtainMessage((int) ImSessionEvent.SEND_MESSAGE_DONE, (Object) messageBase);
        AsyncResult.forMessage(obtainMessage, new SendMessageResult(sendMessageFailedEvent.mRawHandle, sendMessageFailedEvent.mResult), (Throwable) null);
        obtainMessage.sendToTarget();
    }

    /* access modifiers changed from: protected */
    public void receiveSlmMessage(MessageBase messageBase) {
        sendMessage(obtainMessage((int) ImSessionEvent.RECEIVE_SLM_MESSAGE, (Object) messageBase));
    }

    /* access modifiers changed from: protected */
    public boolean isRespondDisplay() {
        return isGroupChat() || this.mConfig.getRespondDisplay();
    }

    /* access modifiers changed from: protected */
    public void processPendingMessages(int i) {
        String str;
        ISimManager simManagerFromSimSlot = SimManagerFactory.getSimManagerFromSimSlot(i);
        if (simManagerFromSimSlot == null) {
            str = "";
        } else {
            str = simManagerFromSimSlot.getImsi();
        }
        this.mOwnImsi = str;
        logi("processPendingMessages phoneId = " + i);
        this.mProcessingFileTransfer.clear();
        this.mListener.onProcessingFileTransferChanged(this);
        TreeMap treeMap = new TreeMap();
        if (TextUtils.isEmpty(this.mOwnImsi)) {
            loge("processPendingMessages: ownImsi is not loaded.");
            return;
        }
        for (MessageBase next : this.mGetter.getAllPendingMessages(this.mChatId)) {
            if (TextUtils.isEmpty(next.getOwnIMSI())) {
                next.updateOwnIMSI(this.mOwnImsi);
                treeMap.put(Long.valueOf(next.getInsertedTimestamp()), next);
            } else if (next.getOwnIMSI().equals(this.mOwnImsi)) {
                treeMap.put(Long.valueOf(next.getInsertedTimestamp()), next);
            }
        }
        for (MessageBase messageBase : treeMap.values()) {
            if (messageBase instanceof ImMessage) {
                processPendingImMessage((ImMessage) messageBase);
            } else if (messageBase instanceof FtMessage) {
                processPendingFtMessage((FtMessage) messageBase);
            }
        }
    }

    /* access modifiers changed from: protected */
    public void processPendingFtHttp(int i) {
        String str;
        ISimManager simManagerFromSimSlot = SimManagerFactory.getSimManagerFromSimSlot(i);
        if (simManagerFromSimSlot == null) {
            str = "";
        } else {
            str = simManagerFromSimSlot.getImsi();
        }
        this.mOwnImsi = str;
        logi("processPendingFtHttp");
        this.mProcessingFileTransfer.clear();
        this.mListener.onProcessingFileTransferChanged(this);
        ArrayList<MessageBase> arrayList = new ArrayList<>();
        for (MessageBase next : this.mGetter.getAllPendingMessages(this.mChatId)) {
            String ownIMSI = next.getOwnIMSI();
            logi("IMSI of SIM sent this message = " + IMSLog.numberChecker(ownIMSI) + ", IMSI of current SIM = " + IMSLog.numberChecker(this.mOwnImsi));
            if (TextUtils.isEmpty(ownIMSI)) {
                logi("current status of this message = " + next.getStatus());
                if (next.getStatus() == ImConstants.Status.SENDING || next.getStatus() == ImConstants.Status.TO_SEND) {
                    next.updateStatus(ImConstants.Status.FAILED);
                }
            } else if (ownIMSI.equals(this.mOwnImsi)) {
                arrayList.add(next);
            }
        }
        arrayList.sort(new ImSession$$ExternalSyntheticLambda0());
        for (MessageBase messageBase : arrayList) {
            if (messageBase instanceof FtMessage) {
                processPendingFtMessage((FtMessage) messageBase);
            }
        }
    }

    /* access modifiers changed from: private */
    public static /* synthetic */ int lambda$processPendingFtHttp$0(MessageBase messageBase, MessageBase messageBase2) {
        int i = ((messageBase.getInsertedTimestamp() - messageBase2.getInsertedTimestamp()) > 0 ? 1 : ((messageBase.getInsertedTimestamp() - messageBase2.getInsertedTimestamp()) == 0 ? 0 : -1));
        if (i == 0) {
            if (messageBase.getId() < messageBase2.getId()) {
                return -1;
            }
            return 1;
        } else if (i < 0) {
            return -1;
        } else {
            return 1;
        }
    }

    /* access modifiers changed from: protected */
    public void processPendingNotifications(List<MessageBase> list) {
        boolean isRespondDisplay = isRespondDisplay();
        ArrayList<MessageBase> arrayList = new ArrayList<>(list);
        arrayList.sort(new ImSession$$ExternalSyntheticLambda1());
        ArrayList<MessageBase> arrayList2 = new ArrayList<>();
        for (MessageBase messageBase : arrayList) {
            if (messageBase.getDirection() == ImDirection.INCOMING) {
                NotificationStatus notificationStatus = messageBase.getNotificationStatus();
                NotificationStatus desiredNotificationStatus = messageBase.getDesiredNotificationStatus();
                logi("sendDispositionNotification current : " + notificationStatus + " desired : " + desiredNotificationStatus);
                NotificationStatus notificationStatus2 = NotificationStatus.DELIVERED;
                if (desiredNotificationStatus == notificationStatus2 && notificationStatus == NotificationStatus.NONE) {
                    arrayList2.add(messageBase);
                } else if (desiredNotificationStatus == NotificationStatus.DISPLAYED) {
                    messageBase.updateStatus(ImConstants.Status.READ);
                    if (notificationStatus == notificationStatus2 && isRespondDisplay) {
                        this.mMessagesToSendDisplayNotification.add(messageBase);
                    } else if (notificationStatus == NotificationStatus.NONE) {
                        arrayList2.add(messageBase);
                        if (isRespondDisplay) {
                            this.mMessagesToSendDisplayNotification.add(messageBase);
                        }
                    }
                }
            }
        }
        for (MessageBase sendDeliveredNotification : arrayList2) {
            sendDeliveredNotification(sendDeliveredNotification);
        }
        if (!this.mMessagesToSendDisplayNotification.isEmpty()) {
            sendMessage(obtainMessage(ImSessionEvent.SEND_DISPLAYED_NOTIFICATION));
        }
    }

    /* access modifiers changed from: private */
    public static /* synthetic */ int lambda$processPendingNotifications$1(MessageBase messageBase, MessageBase messageBase2) {
        int i = ((messageBase.getInsertedTimestamp() - messageBase2.getInsertedTimestamp()) > 0 ? 1 : ((messageBase.getInsertedTimestamp() - messageBase2.getInsertedTimestamp()) == 0 ? 0 : -1));
        if (i == 0) {
            if (messageBase.getId() < messageBase2.getId()) {
                return -1;
            }
            return 1;
        } else if (i < 0) {
            return -1;
        } else {
            return 1;
        }
    }

    private void processPendingImMessage(ImMessage imMessage) {
        ImConstants.Status status = imMessage.getStatus();
        if (!imMessage.isOutgoing()) {
            return;
        }
        if (status == ImConstants.Status.TO_SEND || status == ImConstants.Status.SENDING) {
            sendImMessage(imMessage);
        }
    }

    private void processPendingFtMessage(FtMessage ftMessage) {
        if (!ftMessage.isAutoResumable()) {
            return;
        }
        if (ftMessage.isOutgoing() && ftMessage.getStateId() == 2) {
            resumeTransferFile(ftMessage);
        } else if ((ftMessage instanceof FtHttpIncomingMessage) && ftMessage.getStateId() == 2) {
            receiveTransfer(ftMessage, (FtIncomingSessionEvent) null, true);
        } else if ((ftMessage instanceof FtHttpOutgoingMessage) && ftMessage.getStateId() == 3) {
            ImConstants.Status status = ftMessage.getStatus();
            if (!ftMessage.isOutgoing()) {
                return;
            }
            if (status == ImConstants.Status.TO_SEND || status == ImConstants.Status.SENDING) {
                sendImMessage(ftMessage);
            }
        } else if (getRcsStrategy(this.mPhoneId).boolSetting(RcsPolicySettings.RcsPolicy.AUTO_RESEND_FAILED_FT) && ftMessage.isOutgoing() && ftMessage.getCancelReason() != CancelReason.CANCELED_BY_USER) {
            resumeTransferFile(ftMessage);
        }
    }

    /* access modifiers changed from: protected */
    public void attachFile(FtMessage ftMessage) {
        sendMessage(obtainMessage((int) ImSessionEvent.ATTACH_FILE, (Object) ftMessage));
    }

    /* access modifiers changed from: protected */
    public void processRejoinGCSession() {
        if (isRejoinable() && getRcsStrategy(this.mPhoneId).boolSetting(RcsPolicySettings.RcsPolicy.GROUPCHAT_AUTO_REJOIN) && (isChatState(ChatData.State.ACTIVE) || isChatState(ChatData.State.CLOSED_INVOLUNTARILY))) {
            logi("processRejoinGCSession : " + getChatId());
            sendMessage(obtainMessage(1020));
        } else if (isRejoinable() && isChatState(ChatData.State.CLOSED_VOLUNTARILY)) {
            logi("processRejoinGCSession for bye : " + getChatId());
            sendMessage(obtainMessage(1021));
        }
    }

    /* access modifiers changed from: protected */
    public boolean isAutoRejoinSession() {
        if (isRejoinable() && getRcsStrategy(this.mPhoneId).boolSetting(RcsPolicySettings.RcsPolicy.GROUPCHAT_AUTO_REJOIN) && (isChatState(ChatData.State.ACTIVE) || isChatState(ChatData.State.CLOSED_INVOLUNTARILY))) {
            return true;
        }
        if (!isRejoinable() || !isChatState(ChatData.State.CLOSED_VOLUNTARILY)) {
            return false;
        }
        return true;
    }

    /* access modifiers changed from: protected */
    public void onMessageSending(MessageBase messageBase) {
        Preconditions.checkNotNull(messageBase, "msg cannot be null");
        ImConstants.Status status = messageBase.getStatus();
        ImConstants.Status status2 = ImConstants.Status.SENDING;
        if (!(status == status2 || messageBase.getStatus() == ImConstants.Status.SENT)) {
            messageBase.updateStatus(status2);
        }
        if (!this.mIsComposing || !isBroadcastMsg(messageBase)) {
            this.mIsComposing = false;
            removeMessages(ImSessionEvent.SEND_ISCOMPOSING_REFRESH);
            removeMessages(ImSessionEvent.SEND_ISCOMPOSING_TIMEOUT);
            return;
        }
        sendMessage(obtainMessage((int) ImSessionEvent.SEND_ISCOMPOSING_NOTIFICATION, (Object) Boolean.FALSE));
    }

    /* access modifiers changed from: protected */
    public void onSendSlmMessage(MessageBase messageBase) {
        HashSet hashSet;
        ImConstants.MessagingTech messagingTech;
        MessageBase messageBase2 = messageBase;
        Preconditions.checkNotNull(messageBase2, "msg cannot be null");
        logi("onSendSlmMessage");
        Set<ImsUri> networkPreferredUri = getRcsStrategy(this.mPhoneId).getNetworkPreferredUri(this.mUriGenerator, getParticipantsUri());
        if (messageBase.getType() == ImConstants.Type.TEXT_PUBLICACCOUNT) {
            HashSet hashSet2 = new HashSet();
            for (ImsUri imsUri : networkPreferredUri) {
                hashSet2.add(PublicAccountUri.convertToPublicAccountUri(imsUri.toString()));
            }
            hashSet = hashSet2;
        } else {
            hashSet = networkPreferredUri;
        }
        if (!messageBase.getContentType().contains(MIMEContentType.BOT_SUGGESTION_RESPONSE) && !messageBase.getContentType().contains(MIMEContentType.BOT_SHARED_CLIENT_DATA)) {
            this.mInReplyToContributionId = null;
        }
        SendSlmMessageParams sendSlmMessageParams = new SendSlmMessageParams(messageBase.getId(), this.mChatId, messageBase.getBody(), messageBase.getContentType(), messageBase.getUserAlias(), messageBase.getImdnId(), new Date(), messageBase.getDispositionNotification(), StringIdGenerator.generateContributionId(), this.mChatData.getConversationId(), this.mInReplyToContributionId, hashSet, obtainMessage((int) ImSessionEvent.SEND_SLM_MESSAGE_DONE, (Object) messageBase2), messageBase.getType() == ImConstants.Type.TEXT_PUBLICACCOUNT, isBroadcastMsg(messageBase), this.mChatData.getOwnIMSI(), !isGroupChat() && ChatbotUriUtil.hasChatbotUri(getParticipantsUri(), this.mPhoneId), messageBase.getMaapTrafficType());
        if (messageBase.getReportMsgParams() != null) {
            sendSlmMessageParams.mReportMsgParams = messageBase.getReportMsgParams();
        }
        if (messageBase.getBody().length() > this.mConfig.getPagerModeLimit()) {
            messagingTech = ImConstants.MessagingTech.SLM_LARGE_MODE;
        } else {
            messagingTech = ImConstants.MessagingTech.SLM_PAGER_MODE;
        }
        messageBase.setMessagingTech(messagingTech);
        this.mSlmService.sendSlmMessage(sendSlmMessageParams);
        onMessageSending(messageBase);
        setSessionTimeoutThreshold(messageBase);
    }

    /* access modifiers changed from: protected */
    public void sendDeliveredNotification(MessageBase messageBase) {
        sendMessage(obtainMessage((int) ImSessionEvent.SEND_DELIVERED_NOTIFICATION, (Object) messageBase));
    }

    /* access modifiers changed from: protected */
    public void onAddParticipantsSucceeded(List<ImsUri> list) {
        ArrayList arrayList = new ArrayList();
        for (ImsUri normalizeUri : list) {
            ImsUri normalizeUri2 = this.mGetter.normalizeUri(normalizeUri);
            if (normalizeUri2 != null && getParticipant(normalizeUri2) == null) {
                arrayList.add(new ImParticipant(this.mChatId, ImParticipant.Status.INVITED, normalizeUri2));
            }
        }
        if (!isGroupChat() && getParticipantsSize() > 1) {
            updateChatType(ChatData.ChatType.REGULAR_GROUP_CHAT);
        }
        if (!arrayList.isEmpty()) {
            this.mListener.onParticipantsInserted(this, arrayList);
        }
        this.mListener.onAddParticipantsSucceeded(this.mChatId, list);
    }

    /* access modifiers changed from: protected */
    public void onAddParticipantsFailed(List<ImsUri> list, ImErrorReason imErrorReason) {
        this.mListener.onAddParticipantsFailed(this.mChatId, list, imErrorReason);
    }

    /* access modifiers changed from: protected */
    public void onRemoveParticipantsFailed(List<ImsUri> list, ImErrorReason imErrorReason) {
        this.mListener.onRemoveParticipantsFailed(this.mChatId, list, imErrorReason);
    }

    /* access modifiers changed from: protected */
    public void onChangeGroupChatLeaderFailed(List<ImsUri> list, ImErrorReason imErrorReason) {
        this.mListener.onChangeGroupChatLeaderFailed(this.mChatId, list, imErrorReason);
    }

    /* access modifiers changed from: protected */
    public void onChangeGroupChatSubjectFailed(String str, ImErrorReason imErrorReason) {
        this.mListener.onChangeGroupChatSubjectFailed(this.mChatId, str, imErrorReason);
    }

    /* access modifiers changed from: protected */
    public void onChangeGroupChatIconFailed(String str, ImErrorReason imErrorReason) {
        this.mListener.onChangeGroupChatIconFailed(this.mChatId, str, imErrorReason);
    }

    /* access modifiers changed from: protected */
    public void onChangeGroupAliasFailed(String str, ImErrorReason imErrorReason) {
        this.mListener.onChangeGroupAliasFailed(this.mChatId, str, imErrorReason);
    }

    /* access modifiers changed from: protected */
    public void onConferenceInfoUpdated(ImSessionConferenceInfoUpdateEvent imSessionConferenceInfoUpdateEvent) {
        if (this.mConferenceInfoUpdater == null) {
            ImsUri normalizedUri = this.mUriGenerator.getNormalizedUri(getOwnPhoneNum(), true);
            Context context = getContext();
            int i = this.mPhoneId;
            this.mConferenceInfoUpdater = new ConferenceInfoUpdater(context, this, i, normalizedUri, getRcsStrategy(i), this.mUriGenerator, this.mListener);
        }
        this.mConferenceInfoUpdater.onConferenceInfoUpdated(imSessionConferenceInfoUpdateEvent, this.mLeaderParticipant);
    }

    /* access modifiers changed from: protected */
    public void onIncomingSessionProcessed(ImIncomingMessageEvent imIncomingMessageEvent, boolean z) {
        this.mListener.onIncomingSessionProcessed(imIncomingMessageEvent, this, z);
    }

    /* access modifiers changed from: protected */
    public void failCurrentMessages(Object obj, Result result) {
        failCurrentMessages(obj, result, (String) null);
    }

    /* access modifiers changed from: protected */
    public void failCurrentMessages(Object obj, Result result, String str) {
        for (MessageBase obtainMessage : this.mCurrentMessages) {
            Message obtainMessage2 = obtainMessage((int) ImSessionEvent.SEND_MESSAGE_DONE, (Object) obtainMessage);
            AsyncResult.forMessage(obtainMessage2, new SendMessageResult(obj, result, str), (Throwable) null);
            obtainMessage2.sendToTarget();
        }
        this.mCurrentMessages.clear();
    }

    /* access modifiers changed from: protected */
    public void failCurrentCanceledMessages() {
        for (MessageBase imdnId : this.mCurrentCanceledMessages) {
            this.mListener.onSendCanceledNotificationDone(this.mChatId, imdnId.getImdnId(), false);
        }
        this.mCurrentCanceledMessages.clear();
    }

    /* access modifiers changed from: protected */
    public void updateNetworkForPendingMessage(Network network, Network network2) {
        List<MessageBase> allPendingMessages = this.mGetter.getAllPendingMessages(this.mChatId);
        String str = LOG_TAG;
        Log.i(str, "updateNetworkForPendingMessage: " + allPendingMessages.size() + " pended message(s) in " + this.mChatId + " with " + network + ", " + network2);
        for (MessageBase next : allPendingMessages) {
            if ((next instanceof FtHttpOutgoingMessage) || (next instanceof FtHttpIncomingMessage)) {
                next.setNetwork(network2);
            } else {
                next.setNetwork(network);
            }
        }
    }

    /* access modifiers changed from: protected */
    public FtMessage findFtMessage(String str, long j, String str2) {
        Preconditions.checkNotNull(str);
        Preconditions.checkNotNull(str2);
        for (MessageBase next : this.mGetter.getAllPendingMessages(this.mChatId)) {
            if (next instanceof FtMessage) {
                FtMessage ftMessage = (FtMessage) next;
                if (str.equals(ftMessage.getFileName()) && j == ftMessage.getFileSize() && str2.equals(ftMessage.getFileTransferId())) {
                    return ftMessage;
                }
            }
        }
        return null;
    }

    /* access modifiers changed from: protected */
    public void processDeregistration() {
        logi("processDeregistration :" + getChatId());
        forceCloseSession();
        if (isMsgRevocationSupported() && !this.mNeedToRevokeMessages.isEmpty()) {
            ArrayList arrayList = new ArrayList();
            for (String next : this.mNeedToRevokeMessages.keySet()) {
                MessageBase message = this.mGetter.getMessage(next, ImDirection.OUTGOING, (String) null);
                if (message != null && message.getRevocationStatus() == ImConstants.RevocationStatus.SENT) {
                    arrayList.add(message);
                    stopMsgRevokeOperationTimer(next);
                }
            }
            this.mListener.onMessageRevocationDone(ImConstants.RevocationStatus.SUCCESS, arrayList, this);
            this.mIsRevokeTimerRunning = false;
            this.mNeedToRevokeMessages.clear();
            PreciseAlarmManager.getInstance(getContext()).removeMessage(obtainMessage(ImSessionEvent.MESSAGE_REVOKE_TIMER_EXPIRED));
        }
        processCancelMessages(true, (ImError) null);
    }

    /* access modifiers changed from: protected */
    public void abortAllHttpFtOperations() {
        logi("abortAllHttpFtOperations :" + getChatId());
        for (MessageBase next : this.mGetter.getAllPendingMessages(this.mChatId)) {
            if (next instanceof FtHttpOutgoingMessage) {
                FtMessage ftMessage = (FtMessage) next;
                if (ftMessage.getStateId() == 2) {
                    logi("processDeregistration : mPendingMessages FtMessage.getStateId() = " + ftMessage.getStateId());
                    ftMessage.setFtCompleteCallback((Message) null);
                    ftMessage.cancelTransfer(CancelReason.DEVICE_UNREGISTERED);
                }
            } else if (next instanceof FtHttpIncomingMessage) {
                FtMessage ftMessage2 = (FtMessage) next;
                if (ftMessage2.getStateId() == 2) {
                    logi("processDeregistration : mPendingMessages FtMessage.getStateId() = " + ftMessage2.getStateId());
                    ftMessage2.cancelTransfer(CancelReason.DEVICE_UNREGISTERED);
                }
            }
        }
        synchronized (this.mPendingFileTransfer) {
            for (int i = 0; i < this.mPendingFileTransfer.size(); i++) {
                FtMessage ftMessage3 = this.mPendingFileTransfer.get(i);
                logi("cancel pending file transfer : " + ftMessage3.getId());
                ftMessage3.setFtCompleteCallback((Message) null);
                ftMessage3.cancelTransfer(CancelReason.DEVICE_UNREGISTERED);
            }
        }
    }

    /* access modifiers changed from: protected */
    public void forceCancelFt(boolean z, CancelReason cancelReason) {
        this.mClosedState.forceCancelFt(z, cancelReason, false);
    }

    /* access modifiers changed from: protected */
    public void processCancelMessages(boolean z, ImError imError) {
        logi("processCancelMessages :" + getChatId());
        if (imError == null) {
            imError = ImError.UNKNOWN_ERROR;
        }
        cancelInProgressMessages(z, imError);
        cancelPendingFilesInQueue();
    }

    /* access modifiers changed from: protected */
    public void cancelInProgressMessages(boolean z, ImError imError) {
        for (MessageBase next : this.mGetter.getAllPendingMessages(this.mChatId)) {
            if (next instanceof ImMessage) {
                cancelInProgressChatMsg((ImMessage) next, z, imError);
            } else if (next instanceof FtHttpOutgoingMessage) {
                cancelInProgressFTOutGoingMsg((FtMessage) next, z, imError);
            } else if (next instanceof FtHttpIncomingMessage) {
                cancelInProgressFTInComingMsg((FtMessage) next, z, imError);
            }
        }
    }

    private void cancelInProgressChatMsg(ImMessage imMessage, boolean z, ImError imError) {
        if (imMessage.getDirection() != ImDirection.OUTGOING) {
            return;
        }
        if ((imMessage.getStatus() == ImConstants.Status.TO_SEND || imMessage.getStatus() == ImConstants.Status.SENDING) && imError != ImError.OUTOFSERVICE) {
            logi("cancelInProgressChatMsg : mark msg failed " + imMessage.getId());
            if (!z) {
                imMessage.onSendMessageDone(new Result(imError, Result.Type.DEVICE_UNREGISTERED), new IMnoStrategy.StrategyResponse(IMnoStrategy.StatusCode.DISPLAY_ERROR));
            } else if (isGroupChat() || !ChatbotUriUtil.hasChatbotUri(getParticipantsUri(), this.mPhoneId)) {
                imMessage.onSendMessageDone(new Result(ImError.REMOTE_TEMPORARILY_UNAVAILABLE, Result.Type.DEVICE_UNREGISTERED), new IMnoStrategy.StrategyResponse(IMnoStrategy.StatusCode.FALLBACK_TO_LEGACY));
            } else {
                logi("cancelInProgressChatMsg : no fallback in case of chatbots");
                imMessage.onSendMessageDone(new Result(imError, Result.Type.DEVICE_UNREGISTERED), new IMnoStrategy.StrategyResponse(IMnoStrategy.StatusCode.DISPLAY_ERROR));
            }
        }
    }

    private void cancelInProgressFTInComingMsg(FtMessage ftMessage, boolean z, ImError imError) {
        if (ftMessage.getStateId() == 2) {
            logi("cancelInProgressFTInComingMsg : mPendingMessages FtMessage.getStateId() = " + ftMessage.getStateId());
            if (ftMessage.mIsWifiUsed) {
                ftMessage.cancelTransfer(CancelReason.WIFI_DISCONNECTED);
            } else {
                ftMessage.cancelTransfer(CancelReason.DEVICE_UNREGISTERED);
            }
        }
    }

    private void cancelInProgressFTOutGoingMsg(FtMessage ftMessage, boolean z, ImError imError) {
        if (ftMessage.getStateId() == 2) {
            logi("cancelInProgressFTOutGoingMsg : mPendingMessages FtMessage.getStateId() = " + ftMessage.getStateId());
            ftMessage.setFtCompleteCallback((Message) null);
            if (ftMessage.mIsWifiUsed) {
                ftMessage.cancelTransfer(CancelReason.WIFI_DISCONNECTED);
            } else {
                ftMessage.cancelTransfer(CancelReason.DEVICE_UNREGISTERED);
            }
        } else if (ftMessage.getStateId() == 3 && ftMessage.getStatus() != ImConstants.Status.SENT && !ftMessage.isFtSms()) {
            if (!z || (!isGroupChat() && ChatbotUriUtil.hasChatbotUri(getParticipantsUri(), this.mPhoneId))) {
                ftMessage.onSendMessageDone(new Result(imError, Result.Type.DEVICE_UNREGISTERED), new IMnoStrategy.StrategyResponse(IMnoStrategy.StatusCode.DISPLAY_ERROR));
            } else {
                ftMessage.onSendMessageDone(new Result(ImError.REMOTE_TEMPORARILY_UNAVAILABLE, Result.Type.DEVICE_UNREGISTERED), new IMnoStrategy.StrategyResponse(IMnoStrategy.StatusCode.FALLBACK_TO_LEGACY));
            }
        }
    }

    /* access modifiers changed from: protected */
    public void cancelPendingFilesInQueue() {
        synchronized (this.mPendingFileTransfer) {
            for (int i = 0; i < this.mPendingFileTransfer.size(); i++) {
                FtMessage ftMessage = this.mPendingFileTransfer.get(i);
                logi("cancel pending file transfer : " + ftMessage.getId());
                ftMessage.setFtCompleteCallback((Message) null);
                ftMessage.cancelTransfer(CancelReason.DEVICE_UNREGISTERED);
            }
        }
    }

    /* access modifiers changed from: protected */
    public void sendFile(FtMessage ftMessage) {
        logi("sendFile::entering .... queue size: " + this.mProcessingFileTransfer.size());
        if (ftMessage instanceof FtHttpOutgoingMessage) {
            if (!ftMessage.isFtSms()) {
                ftMessage.setFtCompleteCallback(obtainMessage(ImSessionEvent.FILE_COMPLETE));
            }
            ftMessage.sendFile();
        } else if (this.mProcessingFileTransfer.isEmpty()) {
            ftMessage.setFtCompleteCallback(obtainMessage(ImSessionEvent.FILE_COMPLETE));
            sendMessage(obtainMessage((int) ImSessionEvent.SEND_FILE, (Object) ftMessage));
            addToProcessingFileTransfer(ftMessage);
            this.mListener.onProcessingFileTransferChanged(this);
        } else if (!this.mPendingFileTransfer.contains(ftMessage) && !this.mProcessingFileTransfer.contains(ftMessage)) {
            ftMessage.setFtCompleteCallback(obtainMessage(ImSessionEvent.FILE_COMPLETE));
            this.mPendingFileTransfer.add(ftMessage);
        }
    }

    /* access modifiers changed from: protected */
    public void resumeTransferFile(FtMessage ftMessage) {
        Preconditions.checkNotNull(ftMessage);
        logi("resumeTransferFile: " + ftMessage.getId() + " mProcessingFileTransfer size: " + this.mProcessingFileTransfer.size());
        ftMessage.setConversationId(getConversationId());
        ftMessage.setContributionId(getContributionId());
        ftMessage.setIsResuming(true);
        ftMessage.setFtCompleteCallback(obtainMessage(ImSessionEvent.FILE_COMPLETE));
        if (!(ftMessage instanceof FtHttpOutgoingMessage)) {
            if (TextUtils.isEmpty(ftMessage.mFilePath) || !new File(ftMessage.mFilePath).exists()) {
                ftMessage.mFilePath = FileUtils.copyFileToCacheFromUri(getContext(), ftMessage.getFileName(), ftMessage.getContentUri());
            }
            if (this.mProcessingFileTransfer.isEmpty()) {
                if (isGroupChat()) {
                    attachFile(ftMessage);
                } else {
                    ftMessage.sendFile();
                }
                addToProcessingFileTransfer(ftMessage);
                this.mListener.onProcessingFileTransferChanged(this);
            } else if (!this.mProcessingFileTransfer.contains(ftMessage) && !this.mPendingFileTransfer.contains(ftMessage)) {
                ftMessage.updateQueued();
                this.mPendingFileTransfer.add(ftMessage);
            }
        } else if (isVoluntaryDeparture()) {
            ftMessage.cancelTransfer(CancelReason.CANCELED_BY_USER);
        } else if (isGroupChat()) {
            attachFile(ftMessage);
        } else {
            ftMessage.sendFile();
        }
    }

    /* access modifiers changed from: protected */
    public void receiveTransfer(FtMessage ftMessage, FtIncomingSessionEvent ftIncomingSessionEvent, boolean z) {
        logi("receiveTransfer: mProcessingFileTransfer size: " + this.mProcessingFileTransfer.size());
        ftMessage.receiveTransfer(obtainMessage(ImSessionEvent.FILE_COMPLETE), ftIncomingSessionEvent, z);
        if ((ftMessage instanceof FtMsrpMessage) && !this.mProcessingFileTransfer.contains(ftMessage)) {
            this.mPendingFileTransfer.remove(ftMessage);
            addToProcessingFileTransfer(ftMessage);
            this.mListener.onProcessingFileTransferChanged(this);
        }
    }

    /* access modifiers changed from: protected */
    public boolean isFirstMessageInStart(String str) {
        return getRcsStrategy(this.mPhoneId).isFirstMsgInvite(this.mConfig.isFirstMsgInvite()) && (!isGroupChat() || getRcsStrategy(this.mPhoneId).boolSetting(RcsPolicySettings.RcsPolicy.FIRSTMSG_GROUPCHAT_INVITE)) && !getRcsStrategy(this.mPhoneId).boolSetting(RcsPolicySettings.RcsPolicy.USE_MSRP);
    }

    private boolean isSessionTimeoutSupported() {
        return this.mConfig.getTimerIdle() != 0 && !isGroupChat() && isSessionTimeoutRequired();
    }

    /* access modifiers changed from: protected */
    public void checkAndUpdateSessionTimeout() {
        if (isSessionTimeoutSupported()) {
            removeMessages(1018);
            if (SipMsg.FEATURE_TAG_ENRICHED_CALL_COMPOSER.equalsIgnoreCase(this.mServiceId)) {
                logi("checkAndUpdateSessionTimeout serviceId = " + this.mServiceId + ", " + (this.mConfig.getCallComposerTimerIdle() * 1000));
                if (this.mConfig.getCallComposerTimerIdle() > 0) {
                    sendMessageDelayed(obtainMessage(1018), ((long) this.mConfig.getCallComposerTimerIdle()) * 1000);
                    return;
                }
                return;
            }
            logi("checkAndUpdateSessionTimeout " + (this.mConfig.getTimerIdle() * 1000));
            sendMessageDelayed(obtainMessage(1018), ((long) this.mConfig.getTimerIdle()) * 1000);
        }
    }

    private boolean isSessionTimeoutRequired() {
        return getServiceId() == null || (!getServiceId().equalsIgnoreCase(SipMsg.FEATURE_TAG_ENRICHED_SHARED_SKETCH) && !getServiceId().equalsIgnoreCase(SipMsg.FEATURE_TAG_ENRICHED_SHARED_MAP));
    }

    /* access modifiers changed from: protected */
    public void addToProcessingFileTransfer(FtMessage ftMessage) {
        if (!this.mProcessingFileTransfer.contains(ftMessage)) {
            this.mProcessingFileTransfer.add(ftMessage);
            ftMessage.startFileTransferTimer();
        }
    }

    /* access modifiers changed from: protected */
    public ImSessionInfo addImSessionInfo(ImIncomingSessionEvent imIncomingSessionEvent, ImSessionInfo.ImSessionState imSessionState) {
        ImSessionInfo imSessionInfo = new ImSessionInfo(imIncomingSessionEvent.mRawHandle, imSessionState, ImDirection.INCOMING, imIncomingSessionEvent.mSessionUri, imIncomingSessionEvent.mContributionId, imIncomingSessionEvent.mConversationId, (String) null, imIncomingSessionEvent.mSdpContentType);
        if (imIncomingSessionEvent.mIsDeferred) {
            imSessionInfo.mSessionType = imIncomingSessionEvent.mIsForStoredNoti ? ImSessionInfo.SessionType.SNF_NOTIFICATION_SESSION : ImSessionInfo.SessionType.SNF_SESSION;
        }
        addImSessionInfo(imSessionInfo);
        return imSessionInfo;
    }

    /* access modifiers changed from: protected */
    public void addImSessionInfo(ImSessionInfo imSessionInfo) {
        this.mImSessionInfoList.add(0, imSessionInfo);
    }

    /* access modifiers changed from: protected */
    public void handleAcceptSession(ImSessionInfo imSessionInfo) {
        if (imSessionInfo != null) {
            acquireWakeLock(imSessionInfo.mRawHandle);
            boolean z = imSessionInfo.mSessionType != ImSessionInfo.SessionType.NORMAL;
            imSessionInfo.mState = ImSessionInfo.ImSessionState.ACCEPTING;
            this.mImsService.acceptImSession(new AcceptImSessionParams(this.mChatId, getUserAlias(), imSessionInfo.mRawHandle, z, obtainMessage(z ? 1011 : 1007)));
        }
    }

    /* access modifiers changed from: protected */
    public void handleCloseAllSession(ImSessionStopReason imSessionStopReason) {
        for (ImSessionInfo imSessionInfo : new ArrayList(this.mImSessionInfoList)) {
            this.mClosedState.handleCloseSession(imSessionInfo.mRawHandle, imSessionStopReason);
        }
    }

    /* access modifiers changed from: protected */
    public ImSessionInfo getImSessionInfo(Object obj) {
        if (obj == null) {
            return null;
        }
        for (ImSessionInfo next : this.mImSessionInfoList) {
            if (obj.equals(next.mRawHandle)) {
                return next;
            }
        }
        return null;
    }

    /* access modifiers changed from: protected */
    public ImSessionInfo removeImSessionInfo(Object obj) {
        ImSessionInfo imSessionInfo = getImSessionInfo(obj);
        if (imSessionInfo == null) {
            return null;
        }
        this.mImSessionInfoList.remove(imSessionInfo);
        return imSessionInfo;
    }

    /* access modifiers changed from: protected */
    public boolean removeImSessionInfo(ImSessionInfo imSessionInfo) {
        return this.mImSessionInfoList.remove(imSessionInfo);
    }

    /* access modifiers changed from: protected */
    public ImSessionInfo getLatestActiveImSessionInfo() {
        ImSessionInfo.ImSessionState imSessionState;
        for (ImSessionInfo next : this.mImSessionInfoList) {
            if (!next.isSnFSession() && (imSessionState = next.mState) != ImSessionInfo.ImSessionState.PENDING_INVITE && imSessionState != ImSessionInfo.ImSessionState.CLOSING) {
                return next;
            }
        }
        return null;
    }

    /* access modifiers changed from: protected */
    public boolean hasActiveImSessionInfo() {
        return getLatestActiveImSessionInfo() != null;
    }

    private boolean hasImSessionInfo(ImSessionInfo.ImSessionState imSessionState) {
        for (ImSessionInfo imSessionInfo : this.mImSessionInfoList) {
            if (imSessionInfo.mState == imSessionState) {
                return true;
            }
        }
        return false;
    }

    private boolean hasImSessionInfo(ImSessionInfo.SessionType sessionType) {
        for (ImSessionInfo imSessionInfo : this.mImSessionInfoList) {
            if (imSessionInfo.mSessionType == sessionType) {
                return true;
            }
        }
        return false;
    }

    /* access modifiers changed from: protected */
    public ImSessionInfo getImSessionInfoByMessageId(int i) {
        for (ImSessionInfo next : this.mImSessionInfoList) {
            if (next.mReceivedMessageIds.contains(Integer.valueOf(i))) {
                return next;
            }
        }
        return null;
    }

    /* access modifiers changed from: protected */
    public void transitionToProperState() {
        IState iState;
        HashSet hashSet = new HashSet();
        for (ImSessionInfo next : this.mImSessionInfoList) {
            logi("transitionToProperState : ImSessionInfo = " + next);
            if (!next.isSnFSession()) {
                hashSet.add(next.mState);
            }
        }
        if (hashSet.isEmpty()) {
            iState = this.mClosedState;
        } else if (hashSet.contains(ImSessionInfo.ImSessionState.ESTABLISHED)) {
            iState = this.mEstablishedState;
        } else if (hashSet.contains(ImSessionInfo.ImSessionState.ACCEPTING) || hashSet.contains(ImSessionInfo.ImSessionState.INITIAL) || hashSet.contains(ImSessionInfo.ImSessionState.STARTED) || hashSet.contains(ImSessionInfo.ImSessionState.STARTING)) {
            iState = this.mStartingState;
        } else if (hashSet.contains(ImSessionInfo.ImSessionState.CLOSING)) {
            iState = this.mClosingState;
        } else {
            iState = this.mClosedState;
        }
        if (iState != getCurrentState()) {
            transitionTo(iState);
        }
    }

    /* access modifiers changed from: protected */
    public void onSimRefresh(int i) {
        String str;
        String str2 = LOG_TAG;
        IMSLog.s(str2, "onSimRefresh : " + i);
        ISimManager simManagerFromSimSlot = SimManagerFactory.getSimManagerFromSimSlot(i);
        if (simManagerFromSimSlot == null) {
            str = null;
        } else {
            str = simManagerFromSimSlot.getImsi();
        }
        if (this.mPhoneId != i && getOwnImsi().equals(str)) {
            IMSLog.s(str2, "update previous phoneId : " + this.mPhoneId + "to :" + i);
            this.mPhoneId = i;
        }
    }

    public String toString() {
        return "ImSession [mChatData=" + this.mChatData + ", mSdpContentType=" + this.mSdpContentType + ", mThreadId=" + this.mThreadId + ", mSupportedFeatures=" + this.mSupportedFeatures + ", mRemoteAcceptTypes=" + this.mRemoteAcceptTypes + ", mRemoteAcceptWrappedTypes=" + this.mRemoteAcceptWrappedTypes + ", mInReplyToContributionId=" + this.mInReplyToContributionId + ", mIsComposing=" + this.mIsComposing + ", mParticipants=" + IMSLog.checker(this.mParticipants) + ", mRawHandle=" + this.mRawHandle + ", mClosedReason=" + this.mClosedReason + ", mComposingNotificationInterval=" + this.mComposingNotificationInterval + ", mComposingActiveUris=" + this.mComposingActiveUris + ", mProcessingFileTransfer=" + this.mProcessingFileTransfer + ", mPendingFileTransfer=" + this.mPendingFileTransfer + ", mRequestMessageId=" + this.mRequestMessageId + ", mCurrentMessages=" + this.mCurrentMessages + ", mRawHandle=" + this.mRawHandle + ", mServiceId=" + this.mServiceId + ", mAcceptTypes=" + this.mAcceptTypes + ", mAcceptWrappedTypes=" + this.mAcceptWrappedTypes + "]";
    }

    /* access modifiers changed from: protected */
    public String toStringForDump() {
        return "ImSession [ChatId=" + this.mChatData.getChatId() + ", ConvId=" + this.mChatData.getConversationId() + ", ContId=" + this.mChatData.getContributionId() + ", ChatType=" + this.mChatData.getChatType() + ", Participants=" + IMSLog.checker(this.mParticipants) + ", Status=" + this.mChatData.getState() + ", ClosedReason=" + this.mClosedReason + "]";
    }

    public int hashCode() {
        ChatData chatData = this.mChatData;
        return 31 + (chatData == null ? 0 : chatData.hashCode());
    }

    public boolean equals(Object obj) {
        if (this == obj) {
            return true;
        }
        if (obj == null || getClass() != obj.getClass()) {
            return false;
        }
        ImSession imSession = (ImSession) obj;
        ChatData chatData = this.mChatData;
        if (chatData != null) {
            return chatData.equals(imSession.mChatData);
        }
        if (imSession.mChatData == null) {
            return true;
        }
        return false;
    }

    /* access modifiers changed from: protected */
    public void updateUriGenerator(UriGenerator uriGenerator) {
        this.mUriGenerator = uriGenerator;
    }

    /* access modifiers changed from: protected */
    public boolean needToUseGroupChatInvitationUI() {
        boolean z = isGroupChat() && this.mGetter.getRcsStrategy(this.mPhoneId).boolSetting(RcsPolicySettings.RcsPolicy.GROUPCHAT_INVITATIONUI_USED) && !this.mConfig.isAutAcceptGroupChat();
        logi("needToUseGroupChatInvitationUI, ChatState=" + this.mChatData.getState() + ", ret=" + z);
        return z;
    }

    /* access modifiers changed from: protected */
    public boolean isAutoAccept() {
        if (getRcsStrategy(this.mPhoneId) != null && getRcsStrategy(this.mPhoneId).boolSetting(RcsPolicySettings.RcsPolicy.FORCE_AUTO_ACCEPT)) {
            return true;
        }
        if (isGroupChat()) {
            return this.mConfig.isAutAcceptGroupChat();
        }
        return this.mConfig.isAutAccept();
    }

    /* access modifiers changed from: protected */
    public boolean isEstablishedState() {
        return getCurrentState() == this.mEstablishedState;
    }

    /* access modifiers changed from: protected */
    public boolean isEmptySession() {
        return isChatState(ChatData.State.NONE) && !hasImSessionInfo(ImSessionInfo.ImSessionState.PENDING_INVITE);
    }

    /* access modifiers changed from: protected */
    public boolean isVoluntaryDeparture() {
        return isGroupChat() && isChatState(ChatData.State.CLOSED_VOLUNTARILY);
    }

    /* access modifiers changed from: protected */
    public IMnoStrategy getRcsStrategy(int i) {
        return this.mGetter.getRcsStrategy(i);
    }

    /* access modifiers changed from: protected */
    public Message getFtCompleteCallback() {
        return obtainMessage(ImSessionEvent.FILE_COMPLETE);
    }

    /* access modifiers changed from: protected */
    public void setNetworkFallbackMech(boolean z, boolean z2) {
        this.mChatFallbackMech = ChatFallbackMech.NONE;
        if (!isGroupChat()) {
            if (z) {
                this.mChatFallbackMech = ChatFallbackMech.NETWORK_INTERWORKING;
            } else if (z2 && this.mConfig.getChatRevokeTimer() > 0) {
                this.mChatFallbackMech = ChatFallbackMech.MESSAGE_REVOCATION;
            }
        }
        logi("setNetworkFallbackMech: isMsgFallbackSupported=" + z + ", isMsgRevokeSupported=" + z2 + ", isGroupChat()=" + isGroupChat() + ", getChatRevokeTimer()=" + this.mConfig.getChatRevokeTimer() + ", mChatFallbackMech=" + this.mChatFallbackMech);
    }

    /* access modifiers changed from: protected */
    public void setNetworkFallbackMech(ChatFallbackMech chatFallbackMech) {
        this.mChatFallbackMech = chatFallbackMech;
        logi("setNetworkFallbackMech: mChatFallbackMech=" + this.mChatFallbackMech);
    }

    /* access modifiers changed from: protected */
    public boolean isMsgFallbackSupported() {
        return this.mChatFallbackMech == ChatFallbackMech.NETWORK_INTERWORKING;
    }

    /* access modifiers changed from: protected */
    public boolean isMsgRevocationSupported() {
        return this.mChatFallbackMech == ChatFallbackMech.MESSAGE_REVOCATION;
    }

    public boolean isTimerExpired() {
        return this.mIsTimerExpired;
    }

    /* access modifiers changed from: protected */
    public void setIsTimerExpired(boolean z) {
        this.mIsTimerExpired = z;
    }

    /* access modifiers changed from: protected */
    public void removeMsgFromListForRevoke(String str) {
        removeMsgFromListForRevoke((Collection<String>) Collections.singletonList(str));
    }

    /* access modifiers changed from: protected */
    public void removeMsgFromListForRevoke(Collection<String> collection) {
        this.mNeedToRevokeMessages.keySet().removeAll(collection);
        this.mListener.removeFromRevokingMessages(collection);
        logi("removeMsgFromListForRevoke() : msg imdnId : " + collection + ", remaining list size : " + this.mNeedToRevokeMessages.size());
        if (this.mNeedToRevokeMessages.isEmpty()) {
            this.mIsRevokeTimerRunning = false;
            PreciseAlarmManager.getInstance(getContext()).removeMessage(obtainMessage(ImSessionEvent.MESSAGE_REVOKE_TIMER_EXPIRED));
        }
    }

    /* access modifiers changed from: protected */
    public Map<String, Integer> getNeedToRevokeMessages() {
        return this.mNeedToRevokeMessages;
    }

    /* access modifiers changed from: protected */
    public void reconnectGuardTimerExpired() {
        if (!this.mIsRevokeTimerRunning) {
            sendMessage((int) ImSessionEvent.MESSAGE_REVOKE_TIMER_EXPIRED);
        }
    }

    /* access modifiers changed from: protected */
    public void messageRevocationRequestAll(boolean z, int i) {
        messageRevocationRequest(new ArrayList(this.mNeedToRevokeMessages.keySet()), z, i);
    }

    /* access modifiers changed from: protected */
    public void messageRevocationRequest(List<String> list, boolean z, int i) {
        ArrayList arrayList = new ArrayList();
        logi("messageRevocationRequest() : imdnIds : " + list + " userSelectResult : " + z + " userSelectType : " + i);
        if (z) {
            if (i == 1) {
                for (String message : list) {
                    MessageBase message2 = this.mGetter.getMessage(message, ImDirection.OUTGOING, (String) null);
                    if (!(message2 instanceof ImMessage)) {
                        addPendingRevocationMessage(arrayList, message2);
                    }
                }
            } else if (i == 2) {
                for (String message3 : list) {
                    MessageBase message4 = this.mGetter.getMessage(message3, ImDirection.OUTGOING, (String) null);
                    if (!(message4 instanceof FtMessage)) {
                        addPendingRevocationMessage(arrayList, message4);
                    }
                }
            }
            if (!arrayList.isEmpty()) {
                this.mListener.onMessageRevocationDone(ImConstants.RevocationStatus.FAILED, arrayList, this);
            }
            sendMessage(obtainMessage((int) ImSessionEvent.SEND_MESSAGE_REVOKE_REQUEST, (Object) list));
            return;
        }
        for (String message5 : list) {
            addPendingRevocationMessage(arrayList, this.mGetter.getMessage(message5, ImDirection.OUTGOING, (String) null));
        }
        if (!arrayList.isEmpty()) {
            this.mListener.onMessageRevocationDone(ImConstants.RevocationStatus.FAILED, arrayList, this);
        }
    }

    private void addPendingRevocationMessage(Collection<MessageBase> collection, MessageBase messageBase) {
        if (messageBase != null && collection != null && messageBase.getRevocationStatus() == ImConstants.RevocationStatus.PENDING) {
            collection.add(messageBase);
        }
    }

    /* access modifiers changed from: package-private */
    public void setNotEmptyConversationId() {
        setConversationId(TextUtils.isEmpty(getConversationId()) ? StringIdGenerator.generateConversationId() : getConversationId());
    }

    /* access modifiers changed from: package-private */
    public void setNotEmptyContributionId() {
        setContributionId(TextUtils.isEmpty(getContributionId()) ? StringIdGenerator.generateContributionId() : getContributionId());
    }

    /* access modifiers changed from: protected */
    public void startMsgRevokeOperationTimer(String str) {
        logi("startMsgRevokeOperationTimer() : imdnId : " + str);
        sendMessageDelayed(obtainMessage((int) ImSessionEvent.MESSAGE_REVOKE_OPERATION_TIMEOUT, (Object) str), 10000);
    }

    /* access modifiers changed from: protected */
    public void stopMsgRevokeOperationTimer(String str) {
        logi("stopMsgRevokeOperationTimer() : imdnId : " + str);
        getHandler().removeMessages(ImSessionEvent.MESSAGE_REVOKE_OPERATION_TIMEOUT, str);
    }

    /* access modifiers changed from: protected */
    public void handleSendingStateRevokeMessages() {
        sendMessage((int) ImSessionEvent.RESEND_MESSAGE_REVOKE_REQUEST);
    }

    /* access modifiers changed from: protected */
    public void onSendDisplayedNotification(List<MessageBase> list) {
        String str = LOG_TAG;
        IMSLog.s(str, "onSendDisplayedNotification : messages = " + list);
        for (MessageBase next : list) {
            ImSessionInfo imSessionInfoByMessageId = getImSessionInfoByMessageId(next.getId());
            Object obj = this.mRawHandle;
            if (imSessionInfoByMessageId != null && imSessionInfoByMessageId.isSnFSession() && imSessionInfoByMessageId.mState == ImSessionInfo.ImSessionState.ESTABLISHED) {
                obj = imSessionInfoByMessageId.mRawHandle;
            }
            next.sendDisplayedNotification(obj, getConversationId(), getContributionId(), obtainMessage((int) ImSessionEvent.SEND_DISPLAYED_NOTIFICATION_DONE, (Object) next.toList()), getChatData().getOwnIMSI(), isGroupChat(), isBotSessionAnonymized());
        }
        if (!this.mMessagesToSendDisplayNotification.isEmpty()) {
            sendMessageDelayed((int) ImSessionEvent.SEND_DISPLAYED_NOTIFICATION, 1500);
        }
    }

    /* access modifiers changed from: protected */
    public void onSendCanceledNotification(List<MessageBase> list) {
        String str = LOG_TAG;
        IMSLog.s(str, "onSendCanceledNotification : messages = " + list);
        for (MessageBase next : list) {
            next.sendCanceledNotification(this.mRawHandle, getConversationId(), getContributionId(), obtainMessage((int) ImSessionEvent.SEND_CANCELED_NOTIFICATION_DONE, (Object) next), getChatData().getOwnIMSI(), isGroupChat(), isBotSessionAnonymized());
        }
    }

    /* renamed from: com.sec.internal.ims.servicemodules.im.ImSession$1  reason: invalid class name */
    static /* synthetic */ class AnonymousClass1 {
        static final /* synthetic */ int[] $SwitchMap$com$sec$internal$ims$servicemodules$im$strategy$IMnoStrategy$StatusCode;

        /* JADX WARNING: Can't wrap try/catch for region: R(6:0|1|2|3|4|(3:5|6|8)) */
        /* JADX WARNING: Failed to process nested try/catch */
        /* JADX WARNING: Missing exception handler attribute for start block: B:3:0x0012 */
        /* JADX WARNING: Missing exception handler attribute for start block: B:5:0x001d */
        static {
            /*
                com.sec.internal.ims.servicemodules.im.strategy.IMnoStrategy$StatusCode[] r0 = com.sec.internal.ims.servicemodules.im.strategy.IMnoStrategy.StatusCode.values()
                int r0 = r0.length
                int[] r0 = new int[r0]
                $SwitchMap$com$sec$internal$ims$servicemodules$im$strategy$IMnoStrategy$StatusCode = r0
                com.sec.internal.ims.servicemodules.im.strategy.IMnoStrategy$StatusCode r1 = com.sec.internal.ims.servicemodules.im.strategy.IMnoStrategy.StatusCode.FALLBACK_TO_SLM     // Catch:{ NoSuchFieldError -> 0x0012 }
                int r1 = r1.ordinal()     // Catch:{ NoSuchFieldError -> 0x0012 }
                r2 = 1
                r0[r1] = r2     // Catch:{ NoSuchFieldError -> 0x0012 }
            L_0x0012:
                int[] r0 = $SwitchMap$com$sec$internal$ims$servicemodules$im$strategy$IMnoStrategy$StatusCode     // Catch:{ NoSuchFieldError -> 0x001d }
                com.sec.internal.ims.servicemodules.im.strategy.IMnoStrategy$StatusCode r1 = com.sec.internal.ims.servicemodules.im.strategy.IMnoStrategy.StatusCode.FALLBACK_TO_SLM_FILE     // Catch:{ NoSuchFieldError -> 0x001d }
                int r1 = r1.ordinal()     // Catch:{ NoSuchFieldError -> 0x001d }
                r2 = 2
                r0[r1] = r2     // Catch:{ NoSuchFieldError -> 0x001d }
            L_0x001d:
                int[] r0 = $SwitchMap$com$sec$internal$ims$servicemodules$im$strategy$IMnoStrategy$StatusCode     // Catch:{ NoSuchFieldError -> 0x0028 }
                com.sec.internal.ims.servicemodules.im.strategy.IMnoStrategy$StatusCode r1 = com.sec.internal.ims.servicemodules.im.strategy.IMnoStrategy.StatusCode.FALLBACK_TO_LEGACY     // Catch:{ NoSuchFieldError -> 0x0028 }
                int r1 = r1.ordinal()     // Catch:{ NoSuchFieldError -> 0x0028 }
                r2 = 3
                r0[r1] = r2     // Catch:{ NoSuchFieldError -> 0x0028 }
            L_0x0028:
                return
            */
            throw new UnsupportedOperationException("Method not decompiled: com.sec.internal.ims.servicemodules.im.ImSession.AnonymousClass1.<clinit>():void");
        }
    }

    /* access modifiers changed from: protected */
    public void handleUploadedFileFallback(FtHttpOutgoingMessage ftHttpOutgoingMessage) {
        int i = AnonymousClass1.$SwitchMap$com$sec$internal$ims$servicemodules$im$strategy$IMnoStrategy$StatusCode[getRcsStrategy(this.mPhoneId).getUploadedFileFallbackSLMTech().getStatusCode().ordinal()];
        if (i == 1) {
            sendMessage(obtainMessage((int) ImSessionEvent.SEND_SLM_MESSAGE, (Object) ftHttpOutgoingMessage));
        } else if (i == 2) {
            ftHttpOutgoingMessage.attachSlmFile();
        } else if (i == 3) {
            ftHttpOutgoingMessage.onSendMessageDone(new Result(ImError.REMOTE_TEMPORARILY_UNAVAILABLE, Result.Type.NONE), new IMnoStrategy.StrategyResponse(IMnoStrategy.StatusCode.FALLBACK_TO_LEGACY));
        }
    }

    /* access modifiers changed from: protected */
    public void onEstablishmentTimeOut(Object obj) {
        ImSessionInfo imSessionInfo = getImSessionInfo(obj);
        logi("SESSION_ESTABLISHMENT_TIMEOUT : " + imSessionInfo);
        if (imSessionInfo != null && imSessionInfo.mState != ImSessionInfo.ImSessionState.ESTABLISHED) {
            this.mClosedState.handleCloseSession(imSessionInfo.mRawHandle, ImSessionStopReason.NO_RESPONSE);
        }
    }

    /* access modifiers changed from: protected */
    public Object getRawHandle() {
        return this.mRawHandle;
    }

    /* access modifiers changed from: protected */
    public void setRawHandle(Object obj) {
        this.mRawHandle = obj;
    }

    /* access modifiers changed from: protected */
    public void transitionToStartingState() {
        transitionTo(this.mStartingState);
    }

    /* access modifiers changed from: protected */
    public void addInProgressRequestCallback(Message message) {
        logi("addInProgressRequestCallback: " + message.what);
        removeMessages(ImSessionEvent.EVENT_REQUEST_TIMEOUT);
        sendMessageDelayed(obtainMessage(ImSessionEvent.EVENT_REQUEST_TIMEOUT), 5000);
        this.mInProgressRequestCallbacks.add(message);
    }

    /* access modifiers changed from: protected */
    public void removeInProgressRequestCallback(Message message) {
        logi("removeInProgressRequestCallback: " + message.what);
        this.mInProgressRequestCallbacks.remove(message);
        if (this.mInProgressRequestCallbacks.isEmpty()) {
            removeMessages(ImSessionEvent.EVENT_REQUEST_TIMEOUT);
            handlePendingEvents();
        }
    }

    /* access modifiers changed from: protected */
    public void handleRequestTimeout() {
        logi("handleRequestTimeout: " + this.mInProgressRequestCallbacks);
        for (Message next : this.mInProgressRequestCallbacks) {
            if (next.what != 2009) {
                logi("handleRequestTimeout: Unexpected event " + next.what);
            } else {
                onChangeGroupChatLeaderFailed((List) next.obj, ImErrorReason.ENGINE_ERROR);
            }
        }
        this.mInProgressRequestCallbacks.clear();
        handlePendingEvents();
    }

    /* access modifiers changed from: protected */
    public void handlePendingEvents() {
        logi("handlePendingEvents: " + this.mPendingEvents);
        for (Message sendMessage : this.mPendingEvents) {
            sendMessage(sendMessage);
        }
        this.mPendingEvents.clear();
    }

    /* access modifiers changed from: protected */
    public void leaveSessionWithReject(Object obj) {
        IMSLog.c(LogClass.IM_INCOMING_SESSION_ERR, "User left");
        this.mImsService.rejectImSession(new RejectImSessionParams(this.mChatId, obj, ImSessionRejectReason.VOLUNTARILY, (Message) null));
        this.mClosedReason = ImSessionClosedReason.CLOSED_BY_LOCAL;
        handleCloseAllSession(ImSessionStopReason.VOLUNTARILY);
        updateChatState(ChatData.State.NONE);
        this.mListener.onChatDeparted(this);
        transitionToProperState();
        releaseWakeLock(obj);
    }
}
