package com.sec.internal.ims.servicemodules.im;

import android.content.Context;
import android.net.Network;
import android.os.Message;
import android.util.Log;
import com.sec.ims.util.ImsUri;
import com.sec.internal.constants.ims.MIMEContentType;
import com.sec.internal.constants.ims.servicemodules.im.ImCacheAction;
import com.sec.internal.constants.ims.servicemodules.im.ImConstants;
import com.sec.internal.constants.ims.servicemodules.im.ImDirection;
import com.sec.internal.constants.ims.servicemodules.im.ImImdnRecRoute;
import com.sec.internal.constants.ims.servicemodules.im.NotificationStatus;
import com.sec.internal.constants.ims.servicemodules.im.RoutingType;
import com.sec.internal.constants.ims.servicemodules.im.event.ImdnNotificationEvent;
import com.sec.internal.constants.ims.servicemodules.im.params.SendImdnParams;
import com.sec.internal.constants.ims.servicemodules.im.params.SendMessageRevokeParams;
import com.sec.internal.constants.ims.servicemodules.im.params.SendReportMsgParams;
import com.sec.internal.constants.ims.servicemodules.im.result.Result;
import com.sec.internal.helper.ImExtensionMNOHeadersHelper;
import com.sec.internal.helper.Preconditions;
import com.sec.internal.ims.registry.ImsRegistry;
import com.sec.internal.ims.servicemodules.im.interfaces.IModuleInterface;
import com.sec.internal.ims.servicemodules.im.strategy.IMnoStrategy;
import com.sec.internal.ims.util.ThumbnailTool;
import com.sec.internal.ims.util.UriGenerator;
import com.sec.internal.interfaces.ims.servicemodules.im.IImServiceInterface;
import com.sec.internal.interfaces.ims.servicemodules.im.ISlmServiceInterface;
import com.sec.internal.interfaces.ims.servicemodules.options.ICapabilityDiscoveryModule;
import com.sec.internal.log.IMSLog;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashSet;
import java.util.List;
import java.util.Observable;
import java.util.Set;

public abstract class MessageBase extends Observable {
    public static final int FLAG_FT_SMS = 1;
    public static final int FLAG_TEMPORARY = 2;
    protected static final String LOG_TAG = MessageBase.class.getSimpleName();
    protected String mBody;
    protected final String mChatId;
    private ImConstants.ChatbotMessagingTech mChatbotMessagingTech = ImConstants.ChatbotMessagingTech.UNKNOWN;
    protected final ImConfig mConfig;
    protected String mContentType;
    protected String mContributionId;
    protected String mConversationId;
    protected int mCurrentRetryCount;
    protected long mDeliveredTimestamp;
    protected NotificationStatus mDesiredNotificationStatus;
    protected String mDeviceId;
    protected final ImDirection mDirection;
    protected Set<NotificationStatus> mDispNotification;
    protected long mDisplayedTimestamp;
    protected String mExtInfo;
    protected int mFlagMask;
    protected int mId;
    protected final String mImdnId;
    protected String mImdnOriginalTo;
    protected List<ImImdnRecRoute> mImdnRecRouteList;
    protected final IImServiceInterface mImsService;
    protected final long mInsertedTimestamp;
    protected boolean mIsBroadcastMsg;
    protected boolean mIsRoutingMsg;
    protected boolean mIsSlmSvcMsg;
    protected boolean mIsVM2TextMsg;
    protected long mLastDisplayedTimestamp;
    protected NotificationStatus mLastNotificationType;
    protected String mMaapTrafficType;
    protected String mMessageCreator;
    protected ImConstants.MessagingTech mMessagingTech;
    protected IMnoStrategy mMnoStrategy;
    protected final IModuleInterface mModule;
    protected Network mNetwork;
    protected int mNotDisplayedCounter;
    protected ImsUri mNotificationParticipant;
    protected NotificationStatus mNotificationStatus;
    protected String mRcsTrafficType;
    protected String mReferenceImdnId;
    protected String mReferenceType;
    protected String mReferenceValue;
    protected ImsUri mRemoteUri;
    protected SendReportMsgParams mReportMsgParams;
    protected String mRequestMessageId;
    protected ImConstants.RevocationStatus mRevocationStatus = ImConstants.RevocationStatus.NONE;
    protected RoutingType mRoutingType = RoutingType.NONE;
    protected long mSentTimestamp;
    protected String mSimIMSI;
    protected final ISlmServiceInterface mSlmService;
    protected ImConstants.Status mStatus;
    protected String mSuggestion;
    protected final ThumbnailTool mThumbnailTool;
    protected ImConstants.Type mType;
    protected UriGenerator mUriGenerator;
    protected String mUserAlias;

    public abstract String getServiceTag();

    public void onSendMessageDone(Result result, IMnoStrategy.StrategyResponse strategyResponse) {
    }

    protected MessageBase(Builder<?> builder) {
        NotificationStatus notificationStatus = NotificationStatus.NONE;
        this.mNotificationStatus = notificationStatus;
        this.mDesiredNotificationStatus = notificationStatus;
        this.mLastNotificationType = notificationStatus;
        Preconditions.checkNotNull(builder.mModule);
        Preconditions.checkNotNull(builder.mModule.getContext());
        Preconditions.checkNotNull(builder.mImsService);
        Preconditions.checkNotNull(builder.mSlmService);
        Preconditions.checkNotNull(builder.mConfig);
        Preconditions.checkNotNull(builder.mUriGenerator);
        this.mModule = builder.mModule;
        this.mImsService = builder.mImsService;
        this.mSlmService = builder.mSlmService;
        this.mConfig = builder.mConfig;
        this.mThumbnailTool = builder.mThumbnailTool;
        this.mUriGenerator = builder.mUriGenerator;
        this.mId = builder.mId;
        this.mChatId = builder.mChatId;
        this.mImdnId = builder.mImdnId;
        this.mImdnOriginalTo = builder.mImdnOriginalTo;
        this.mImdnRecRouteList = builder.mImdnRecRouteList;
        this.mType = builder.mType;
        this.mIsSlmSvcMsg = builder.mIsSlmSvcMsg;
        this.mBody = builder.mBody;
        this.mSuggestion = builder.mSuggestion;
        this.mContentType = builder.mContentType;
        this.mStatus = builder.mStatus;
        this.mDirection = builder.mDirection;
        this.mInsertedTimestamp = builder.mInsertedTimestamp;
        this.mSentTimestamp = builder.mSentTimestamp;
        this.mDeliveredTimestamp = builder.mDeliveredTimestamp;
        this.mDisplayedTimestamp = builder.mDisplayedTimestamp;
        this.mRemoteUri = builder.mRemoteUri;
        this.mUserAlias = builder.mUserAlias;
        this.mDispNotification = builder.mDispNotification;
        this.mNotificationStatus = builder.mNotificationStatus;
        this.mDesiredNotificationStatus = builder.mDesiredNotificationStatus;
        this.mNotDisplayedCounter = builder.mNotDisplayedCounter;
        this.mRequestMessageId = builder.mRequestMessageId;
        this.mIsBroadcastMsg = builder.mIsBroadcastMsg;
        this.mIsVM2TextMsg = builder.mIsVM2TextMsg;
        this.mIsRoutingMsg = builder.mIsRoutingMsg;
        this.mRoutingType = builder.mRoutingType;
        this.mMnoStrategy = builder.mMnoStrategy;
        this.mNetwork = builder.mNetwork;
        this.mExtInfo = builder.mExtInfo;
        this.mConversationId = builder.mConversationId;
        this.mContributionId = builder.mContributionId;
        this.mDeviceId = builder.mDeviceId;
        this.mSimIMSI = builder.mSimIMSI;
        this.mFlagMask = builder.mFlagMask;
        this.mRevocationStatus = builder.mRevocationStatus;
        this.mMaapTrafficType = builder.mMaapTraficType;
        this.mMessagingTech = builder.mMessagingTech;
        this.mReferenceImdnId = builder.mReferenceImdnId;
        this.mReferenceType = builder.mReferenceType;
        this.mReferenceValue = builder.mReferenceValue;
        this.mRcsTrafficType = builder.mRcsTrafficType;
    }

    public static ImConstants.Type getType(String str) {
        if (str == null || (!str.contains(MIMEContentType.LOCATION_PUSH) && !str.contains(MIMEContentType.LOCATION_PULL))) {
            return ImConstants.Type.TEXT;
        }
        return ImConstants.Type.LOCATION;
    }

    /* access modifiers changed from: protected */
    public Context getContext() {
        return this.mModule.getContext();
    }

    /* access modifiers changed from: protected */
    public boolean isWifiConnected() {
        return this.mModule.isWifiConnected();
    }

    public int getId() {
        return this.mId;
    }

    public void setId(int i) {
        this.mId = i;
    }

    public String getBody() {
        return this.mBody;
    }

    public String getSuggestion() {
        return this.mSuggestion;
    }

    public String getContentType() {
        return this.mContentType;
    }

    public ImConstants.Type getType() {
        return this.mType;
    }

    public boolean getIsSlmSvcMsg() {
        return this.mIsSlmSvcMsg;
    }

    public String getChatId() {
        return this.mChatId;
    }

    public String getImdnId() {
        return this.mImdnId;
    }

    public String getImdnOriginalTo() {
        return this.mImdnOriginalTo;
    }

    public List<ImImdnRecRoute> getImdnRecRouteList() {
        return this.mImdnRecRouteList;
    }

    public void setImdnRecRouteList(List<ImImdnRecRoute> list) {
        this.mImdnRecRouteList = list;
    }

    public long getInsertedTimestamp() {
        return this.mInsertedTimestamp;
    }

    public long getSentTimestamp() {
        return this.mSentTimestamp;
    }

    public void setSentTimestamp(long j) {
        this.mSentTimestamp = j;
    }

    public long getDeliveredTimestamp() {
        return this.mDeliveredTimestamp;
    }

    public void setDeliveredTimestamp(long j) {
        this.mDeliveredTimestamp = j;
    }

    public Long getDisplayedTimestamp() {
        return Long.valueOf(this.mDisplayedTimestamp);
    }

    public void setDisplayedTimestamp(long j) {
        this.mDisplayedTimestamp = j;
    }

    public Long getLastDisplayedTimestamp() {
        return Long.valueOf(this.mLastDisplayedTimestamp);
    }

    public ImsUri getRemoteUri() {
        return this.mRemoteUri;
    }

    public void setUserAlias(String str) {
        this.mUserAlias = str;
    }

    public String getUserAlias() {
        return this.mUserAlias;
    }

    public ImDirection getDirection() {
        return this.mDirection;
    }

    public ImConstants.Status getStatus() {
        return this.mStatus;
    }

    public void setStatus(ImConstants.Status status) {
        this.mStatus = status;
    }

    public Set<NotificationStatus> getDispositionNotification() {
        return this.mDispNotification;
    }

    public NotificationStatus getNotificationStatus() {
        return this.mNotificationStatus;
    }

    public NotificationStatus getDesiredNotificationStatus() {
        return this.mDesiredNotificationStatus;
    }

    public void setDesiredNotificationStatus(NotificationStatus notificationStatus) {
        this.mDesiredNotificationStatus = notificationStatus;
    }

    public NotificationStatus getLastNotificationType() {
        return this.mLastNotificationType;
    }

    public int getNotDisplayedCounter() {
        return this.mNotDisplayedCounter;
    }

    public String getExtInfo() {
        return this.mExtInfo;
    }

    public ImsUri getNotificationParticipant() {
        return this.mNotificationParticipant;
    }

    public void setNetwork(Network network) {
        this.mNetwork = network;
    }

    public void updateStatus(ImConstants.Status status) {
        if (!status.equals(this.mStatus)) {
            this.mStatus = status;
            triggerObservers(ImCacheAction.UPDATED);
        }
    }

    public void updateExtInfo(String str) {
        this.mExtInfo = str;
        triggerObservers(ImCacheAction.UPDATED);
    }

    public void updateDeliveredTimestamp(long j) {
        this.mDeliveredTimestamp = j;
        triggerObservers(ImCacheAction.UPDATED);
    }

    public void updateDisplayedTimestamp(long j) {
        this.mDisplayedTimestamp = j;
        triggerObservers(ImCacheAction.UPDATED);
    }

    public void updateDesiredNotificationStatus(NotificationStatus notificationStatus) {
        if (!notificationStatus.equals(this.mDesiredNotificationStatus)) {
            this.mDesiredNotificationStatus = notificationStatus;
            triggerObservers(ImCacheAction.UPDATED);
        }
    }

    public String getRequestMessageId() {
        return this.mRequestMessageId;
    }

    public void updateNotificationStatus(NotificationStatus notificationStatus) {
        if (!notificationStatus.equals(this.mNotificationStatus)) {
            this.mNotificationStatus = notificationStatus;
            triggerObservers(ImCacheAction.UPDATED);
        }
    }

    public void updateRevocationStatus(ImConstants.RevocationStatus revocationStatus) {
        if (!revocationStatus.equals(this.mRevocationStatus)) {
            this.mRevocationStatus = revocationStatus;
            triggerObservers(ImCacheAction.UPDATED);
        }
    }

    public boolean isDeliveredNotificationRequired() {
        return this.mDirection == ImDirection.INCOMING && this.mDispNotification.contains(NotificationStatus.DELIVERED);
    }

    public boolean isDisplayedNotificationRequired() {
        return this.mDirection == ImDirection.INCOMING && this.mDispNotification.contains(NotificationStatus.DISPLAYED);
    }

    public List<MessageBase> toList() {
        ArrayList arrayList = new ArrayList();
        arrayList.add(this);
        return arrayList;
    }

    public SendImdnParams.ImdnData getNewImdnData(NotificationStatus notificationStatus) {
        return new SendImdnParams.ImdnData(notificationStatus, this.mImdnId, getNewDate(this.mSentTimestamp), this.mImdnRecRouteList, this.mImdnOriginalTo);
    }

    public boolean isBroadcastMsg() {
        return this.mIsBroadcastMsg;
    }

    public boolean isVM2TextMsg() {
        return this.mIsVM2TextMsg;
    }

    public boolean isRoutingMsg() {
        return this.mIsRoutingMsg;
    }

    public RoutingType getRoutingType() {
        return this.mRoutingType;
    }

    public boolean isFtSms() {
        return (this.mFlagMask & 1) == 1;
    }

    public void setFtSms(boolean z) {
        this.mFlagMask = z ? this.mFlagMask | 1 : this.mFlagMask & -2;
    }

    public boolean isTemporary() {
        return (this.mFlagMask & 2) == 2;
    }

    public void setTemporary(boolean z) {
        this.mFlagMask = z ? this.mFlagMask | 2 : this.mFlagMask & -3;
    }

    public void setSlmSvcMsg(boolean z) {
        this.mIsSlmSvcMsg = z;
    }

    public ImConstants.MessagingTech getMessagingTech() {
        return this.mMessagingTech;
    }

    public void setMessagingTech(ImConstants.MessagingTech messagingTech) {
        this.mMessagingTech = messagingTech;
    }

    public ImConstants.ChatbotMessagingTech getChatbotMessagingTech() {
        return this.mChatbotMessagingTech;
    }

    public void setChatbotMessagingTech(ImConstants.ChatbotMessagingTech chatbotMessagingTech) {
        this.mChatbotMessagingTech = chatbotMessagingTech;
    }

    public int getFlagMask() {
        return this.mFlagMask;
    }

    public ImConstants.RevocationStatus getRevocationStatus() {
        return this.mRevocationStatus;
    }

    public void setRevocationStatus(ImConstants.RevocationStatus revocationStatus) {
        this.mRevocationStatus = revocationStatus;
    }

    public boolean isOutgoing() {
        return this.mDirection == ImDirection.OUTGOING;
    }

    public boolean isIncoming() {
        return this.mDirection == ImDirection.INCOMING;
    }

    public void setSpamInfo(ImsUri imsUri, ImsUri imsUri2, String str, String str2) {
        this.mReportMsgParams = new SendReportMsgParams(imsUri, imsUri2, str, str2);
    }

    public SendReportMsgParams getReportMsgParams() {
        return this.mReportMsgParams;
    }

    public String getConversationId() {
        return this.mConversationId;
    }

    public String getContributionId() {
        return this.mContributionId;
    }

    public String getDeviceId() {
        return this.mDeviceId;
    }

    public String getOwnIMSI() {
        return this.mSimIMSI;
    }

    public void updateOwnIMSI(String str) {
        if (str != null && !"".equals(str) && !str.equals(this.mSimIMSI)) {
            this.mSimIMSI = str;
            triggerObservers(ImCacheAction.UPDATED);
        }
    }

    public void updateRemoteUri(ImsUri imsUri) {
        this.mRemoteUri = imsUri;
        triggerObservers(ImCacheAction.UPDATED);
    }

    public String getMaapTrafficType() {
        return this.mMaapTrafficType;
    }

    public String getReferenceImdnId() {
        return this.mReferenceImdnId;
    }

    public String getReferenceType() {
        return this.mReferenceType;
    }

    public String getReferenceValue() {
        return this.mReferenceValue;
    }

    public String getRcsTrafficType() {
        return this.mRcsTrafficType;
    }

    private ImsUri getParticipantsNetworkPreferredUri(ImsUri imsUri) {
        ICapabilityDiscoveryModule capabilityDiscoveryModule = ImsRegistry.getServiceModuleManager().getCapabilityDiscoveryModule();
        ImsUri networkPreferredUri = capabilityDiscoveryModule != null ? capabilityDiscoveryModule.getNetworkPreferredUri(imsUri) : null;
        return networkPreferredUri == null ? this.mUriGenerator.getNetworkPreferredUri(UriGenerator.URIServiceType.RCS_URI, imsUri) : networkPreferredUri;
    }

    /* access modifiers changed from: protected */
    public void sendDeliveredNotification(Object obj, String str, String str2, Message message, String str3, boolean z, boolean z2) {
        ImsUri imsUri = this.mRemoteUri;
        if (imsUri == null) {
            onSendDeliveredNotificationDone();
            return;
        }
        ImsUri networkPreferredUri = this.mUriGenerator.getNetworkPreferredUri(UriGenerator.URIServiceType.RCS_URI, getParticipantsNetworkPreferredUri(imsUri));
        String str4 = this.mChatId;
        String str5 = this.mConversationId;
        String str6 = str5 == null ? str : str5;
        String str7 = this.mContributionId;
        SendImdnParams sendImdnParams = new SendImdnParams(obj, networkPreferredUri, str4, str6, str7 == null ? str2 : str7, str3, message, this.mDeviceId, getNewImdnData(NotificationStatus.DELIVERED), z, new Date(), z2, this.mModule.getUserAlias(this.mConfig.getPhoneId(), true));
        if (this.mIsSlmSvcMsg) {
            if (isVM2TextMsg()) {
                sendImdnParams.addImExtensionMNOHeaders(ImExtensionMNOHeadersHelper.addVM2TextHeaders());
            }
            this.mSlmService.sendSlmDeliveredNotification(sendImdnParams);
            return;
        }
        this.mImsService.sendDeliveredNotification(sendImdnParams);
    }

    /* access modifiers changed from: protected */
    public void sendDisplayedNotification(Object obj, String str, String str2, Message message, String str3, boolean z, boolean z2) {
        ImsUri imsUri = this.mRemoteUri;
        if (imsUri == null) {
            onSendDisplayedNotificationDone();
            return;
        }
        ImsUri networkPreferredUri = this.mUriGenerator.getNetworkPreferredUri(UriGenerator.URIServiceType.RCS_URI, getParticipantsNetworkPreferredUri(imsUri));
        String str4 = this.mChatId;
        String str5 = this.mConversationId;
        String str6 = str5 == null ? str : str5;
        String str7 = this.mContributionId;
        SendImdnParams sendImdnParams = new SendImdnParams(obj, networkPreferredUri, str4, str6, str7 == null ? str2 : str7, str3, message, this.mDeviceId, getNewImdnData(NotificationStatus.DISPLAYED), z, new Date(), z2, this.mModule.getUserAlias(this.mConfig.getPhoneId(), true));
        if (this.mIsSlmSvcMsg) {
            if (isVM2TextMsg()) {
                sendImdnParams.addImExtensionMNOHeaders(ImExtensionMNOHeadersHelper.addVM2TextHeaders());
            }
            this.mSlmService.sendSlmDisplayedNotification(sendImdnParams);
            return;
        }
        this.mImsService.sendDisplayedNotification(sendImdnParams);
    }

    /* access modifiers changed from: protected */
    public void sendCanceledNotification(Object obj, String str, String str2, Message message, String str3, boolean z, boolean z2) {
        ImsUri networkPreferredUri;
        if (z) {
            networkPreferredUri = new ImsUri("sip:anonymous@anonymous.invalid");
        } else {
            ImsUri imsUri = this.mRemoteUri;
            if (imsUri != null) {
                networkPreferredUri = this.mUriGenerator.getNetworkPreferredUri(UriGenerator.URIServiceType.RCS_URI, getParticipantsNetworkPreferredUri(imsUri));
            } else {
                Log.e(LOG_TAG, "mRemoteUri is null.");
                return;
            }
        }
        ImsUri imsUri2 = networkPreferredUri;
        String str4 = this.mChatId;
        String str5 = this.mConversationId;
        String str6 = str5 == null ? str : str5;
        String str7 = this.mContributionId;
        this.mImsService.sendCanceledNotification(new SendImdnParams(obj, imsUri2, str4, str6, str7 == null ? str2 : str7, str3, message, this.mDeviceId, getNewImdnData(NotificationStatus.CANCELED), z, new Date(), z2, this.mModule.getUserAlias(this.mConfig.getPhoneId(), true)));
    }

    /* access modifiers changed from: protected */
    public void onSendDeliveredNotificationDone() {
        if (this.mNotificationStatus != NotificationStatus.DISPLAYED) {
            updateNotificationStatus(NotificationStatus.DELIVERED);
        }
    }

    /* access modifiers changed from: protected */
    public void onSendDisplayedNotificationDone() {
        updateNotificationStatus(NotificationStatus.DISPLAYED);
    }

    /* access modifiers changed from: protected */
    public void onSendCanceledNotificationDone() {
        updateStatus(ImConstants.Status.CANCELLATION);
    }

    public void onImdnNotificationReceived(ImdnNotificationEvent imdnNotificationEvent) {
        NotificationStatus notificationStatus;
        if (this.mDirection != ImDirection.OUTGOING) {
            Log.e(LOG_TAG, "Incoming message received imdn notification, ignore.");
            return;
        }
        int i = AnonymousClass1.$SwitchMap$com$sec$internal$constants$ims$servicemodules$im$NotificationStatus[imdnNotificationEvent.mStatus.ordinal()];
        if (i == 1) {
            NotificationStatus notificationStatus2 = NotificationStatus.DELIVERED;
            this.mNotificationStatus = notificationStatus2;
            this.mLastNotificationType = notificationStatus2;
            setDeliveredTimestamp(imdnNotificationEvent.mCpimDate.getTime());
            setRevocationStatus(ImConstants.RevocationStatus.NONE);
            this.mNotificationParticipant = imdnNotificationEvent.mRemoteUri;
            triggerObservers(ImCacheAction.UPDATED);
        } else if (i == 2) {
            if (this.mNotificationStatus == NotificationStatus.NONE) {
                this.mNotificationStatus = NotificationStatus.DELIVERED;
                setRevocationStatus(ImConstants.RevocationStatus.NONE);
                setDeliveredTimestamp(imdnNotificationEvent.mCpimDate.getTime());
            }
            NotificationStatus notificationStatus3 = NotificationStatus.DISPLAYED;
            this.mLastNotificationType = notificationStatus3;
            this.mLastDisplayedTimestamp = imdnNotificationEvent.mCpimDate.getTime();
            if (this.mNotDisplayedCounter > 0) {
                String str = LOG_TAG;
                Log.i(str, "onImdnNotificationReceived: Decrease mNotDisplayedCounter " + this.mNotDisplayedCounter);
                this.mNotDisplayedCounter = this.mNotDisplayedCounter - 1;
            }
            if (this.mNotDisplayedCounter == 0) {
                this.mNotificationStatus = notificationStatus3;
                setDisplayedTimestamp(imdnNotificationEvent.mCpimDate.getTime());
            }
            this.mNotificationParticipant = imdnNotificationEvent.mRemoteUri;
            triggerObservers(ImCacheAction.UPDATED);
        } else if (i == 3) {
            NotificationStatus notificationStatus4 = this.mNotificationStatus;
            if (notificationStatus4 != NotificationStatus.DELIVERED && notificationStatus4 != NotificationStatus.DISPLAYED) {
                NotificationStatus notificationStatus5 = NotificationStatus.INTERWORKING_SMS;
                this.mNotificationStatus = notificationStatus5;
                this.mLastNotificationType = notificationStatus5;
                setDeliveredTimestamp(imdnNotificationEvent.mCpimDate.getTime());
                setRevocationStatus(ImConstants.RevocationStatus.NONE);
                this.mNotificationParticipant = imdnNotificationEvent.mRemoteUri;
                triggerObservers(ImCacheAction.UPDATED);
            }
        } else if (i == 4 && (notificationStatus = this.mNotificationStatus) != NotificationStatus.DELIVERED && notificationStatus != NotificationStatus.DISPLAYED) {
            NotificationStatus notificationStatus6 = NotificationStatus.INTERWORKING_MMS;
            this.mNotificationStatus = notificationStatus6;
            this.mLastNotificationType = notificationStatus6;
            setDeliveredTimestamp(imdnNotificationEvent.mCpimDate.getTime());
            setRevocationStatus(ImConstants.RevocationStatus.NONE);
            this.mNotificationParticipant = imdnNotificationEvent.mRemoteUri;
            triggerObservers(ImCacheAction.UPDATED);
        }
    }

    /* renamed from: com.sec.internal.ims.servicemodules.im.MessageBase$1  reason: invalid class name */
    static /* synthetic */ class AnonymousClass1 {
        static final /* synthetic */ int[] $SwitchMap$com$sec$internal$constants$ims$servicemodules$im$NotificationStatus;

        /* JADX WARNING: Can't wrap try/catch for region: R(8:0|1|2|3|4|5|6|(3:7|8|10)) */
        /* JADX WARNING: Failed to process nested try/catch */
        /* JADX WARNING: Missing exception handler attribute for start block: B:3:0x0012 */
        /* JADX WARNING: Missing exception handler attribute for start block: B:5:0x001d */
        /* JADX WARNING: Missing exception handler attribute for start block: B:7:0x0028 */
        static {
            /*
                com.sec.internal.constants.ims.servicemodules.im.NotificationStatus[] r0 = com.sec.internal.constants.ims.servicemodules.im.NotificationStatus.values()
                int r0 = r0.length
                int[] r0 = new int[r0]
                $SwitchMap$com$sec$internal$constants$ims$servicemodules$im$NotificationStatus = r0
                com.sec.internal.constants.ims.servicemodules.im.NotificationStatus r1 = com.sec.internal.constants.ims.servicemodules.im.NotificationStatus.DELIVERED     // Catch:{ NoSuchFieldError -> 0x0012 }
                int r1 = r1.ordinal()     // Catch:{ NoSuchFieldError -> 0x0012 }
                r2 = 1
                r0[r1] = r2     // Catch:{ NoSuchFieldError -> 0x0012 }
            L_0x0012:
                int[] r0 = $SwitchMap$com$sec$internal$constants$ims$servicemodules$im$NotificationStatus     // Catch:{ NoSuchFieldError -> 0x001d }
                com.sec.internal.constants.ims.servicemodules.im.NotificationStatus r1 = com.sec.internal.constants.ims.servicemodules.im.NotificationStatus.DISPLAYED     // Catch:{ NoSuchFieldError -> 0x001d }
                int r1 = r1.ordinal()     // Catch:{ NoSuchFieldError -> 0x001d }
                r2 = 2
                r0[r1] = r2     // Catch:{ NoSuchFieldError -> 0x001d }
            L_0x001d:
                int[] r0 = $SwitchMap$com$sec$internal$constants$ims$servicemodules$im$NotificationStatus     // Catch:{ NoSuchFieldError -> 0x0028 }
                com.sec.internal.constants.ims.servicemodules.im.NotificationStatus r1 = com.sec.internal.constants.ims.servicemodules.im.NotificationStatus.INTERWORKING_SMS     // Catch:{ NoSuchFieldError -> 0x0028 }
                int r1 = r1.ordinal()     // Catch:{ NoSuchFieldError -> 0x0028 }
                r2 = 3
                r0[r1] = r2     // Catch:{ NoSuchFieldError -> 0x0028 }
            L_0x0028:
                int[] r0 = $SwitchMap$com$sec$internal$constants$ims$servicemodules$im$NotificationStatus     // Catch:{ NoSuchFieldError -> 0x0033 }
                com.sec.internal.constants.ims.servicemodules.im.NotificationStatus r1 = com.sec.internal.constants.ims.servicemodules.im.NotificationStatus.INTERWORKING_MMS     // Catch:{ NoSuchFieldError -> 0x0033 }
                int r1 = r1.ordinal()     // Catch:{ NoSuchFieldError -> 0x0033 }
                r2 = 4
                r0[r1] = r2     // Catch:{ NoSuchFieldError -> 0x0033 }
            L_0x0033:
                return
            */
            throw new UnsupportedOperationException("Method not decompiled: com.sec.internal.ims.servicemodules.im.MessageBase.AnonymousClass1.<clinit>():void");
        }
    }

    public void onCanceledNotificationReceived(ImdnNotificationEvent imdnNotificationEvent) {
        if (this.mDirection != ImDirection.INCOMING) {
            Log.e(LOG_TAG, "Outgoing message received canceled notification, ignore.");
            return;
        }
        this.mLastNotificationType = NotificationStatus.CANCELED;
        this.mNotificationParticipant = this.mUriGenerator.normalize(imdnNotificationEvent.mRemoteUri);
        if (this.mStatus == ImConstants.Status.READ) {
            updateStatus(ImConstants.Status.CANCELLATION);
        } else {
            updateStatus(ImConstants.Status.CANCELLATION_UNREAD);
        }
    }

    public int getCurrentRetryCount() {
        return this.mCurrentRetryCount;
    }

    public void incrementRetryCount() {
        this.mCurrentRetryCount++;
    }

    public void triggerObservers(ImCacheAction imCacheAction) {
        setChanged();
        notifyObservers(imCacheAction);
    }

    public IMnoStrategy getRcsStrategy() {
        return this.mMnoStrategy;
    }

    public String getMessageCreator() {
        return this.mMessageCreator;
    }

    public void setMessageCreator(String str) {
        this.mMessageCreator = str;
    }

    public int hashCode() {
        String str = this.mChatId;
        return (((str == null ? 0 : str.hashCode()) + 31) * 31) + this.mId;
    }

    public String toString() {
        return "MessageBase [mChatId=" + this.mChatId + ", mId=" + this.mId + ", mBody=" + IMSLog.checker(this.mBody) + ", mImdnId=" + this.mImdnId + ", mRemoteUri=" + IMSLog.numberChecker(this.mRemoteUri) + ", mType=" + this.mType + ", mContentType=" + this.mContentType + ", mImdnOriginalTo=" + IMSLog.checker(this.mImdnOriginalTo) + ", mImdnRecRouteList=" + this.mImdnRecRouteList + ", mStatus=" + this.mStatus + ", mInsertedTimestamp=" + this.mInsertedTimestamp + ", mSentTimestamp=" + this.mSentTimestamp + ", mDeliveredTimestamp=" + this.mDeliveredTimestamp + ", mDisplayedTimestamp=" + this.mDisplayedTimestamp + ", mDirection=" + this.mDirection + ", mUserAlias=" + IMSLog.checker(this.mUserAlias) + ", mCurrentRetryCount=" + this.mCurrentRetryCount + ", mDispNotification=" + this.mDispNotification + ", mNotificationStatus=" + this.mNotificationStatus + ", mDesiredNotificationStatus=" + this.mDesiredNotificationStatus + ", mNotDisplayedCounter=" + this.mNotDisplayedCounter + ", mIsBroadcastMsg=" + this.mIsBroadcastMsg + ", mDeviceId=" + this.mDeviceId + ", mMaapTrafficType=" + this.mMaapTrafficType + ", mReferenceImdnId=" + this.mReferenceImdnId + ", mReferenceType=" + this.mReferenceType + ", mReferenceValue=" + this.mReferenceValue + ", mRcsTrafficType=" + this.mRcsTrafficType + "]";
    }

    public boolean equals(Object obj) {
        if (this == obj) {
            return true;
        }
        if (obj == null || getClass() != obj.getClass()) {
            return false;
        }
        MessageBase messageBase = (MessageBase) obj;
        String str = this.mChatId;
        if (str == null) {
            if (messageBase.mChatId != null) {
                return false;
            }
        } else if (!str.equals(messageBase.mChatId)) {
            return false;
        }
        if (this.mId == messageBase.mId) {
            return true;
        }
        return false;
    }

    public void sendMessageRevokeRequest(String str, String str2, Message message, String str3) {
        ImsUri imsUri = this.mRemoteUri;
        if (imsUri == null) {
            Log.e(LOG_TAG, "remote uri is null");
            return;
        }
        this.mImsService.sendMessageRevokeRequest(new SendMessageRevokeParams(this.mUriGenerator.getNetworkPreferredUri(UriGenerator.URIServiceType.RCS_URI, imsUri.getMsisdn(), (String) null), this.mImdnId, message, str, str2, str3));
    }

    public Date getNewDate(long j) {
        if (j == 0) {
            j = System.currentTimeMillis();
        }
        return new Date(j);
    }

    public int getPhoneId() {
        return this.mConfig.getPhoneId();
    }

    public static abstract class Builder<T extends Builder<T>> {
        /* access modifiers changed from: private */
        public String mBody;
        /* access modifiers changed from: private */
        public String mChatId;
        /* access modifiers changed from: private */
        public ImConfig mConfig;
        /* access modifiers changed from: private */
        public String mContentType;
        /* access modifiers changed from: private */
        public String mContributionId;
        /* access modifiers changed from: private */
        public String mConversationId;
        /* access modifiers changed from: private */
        public long mDeliveredTimestamp;
        /* access modifiers changed from: private */
        public NotificationStatus mDesiredNotificationStatus;
        /* access modifiers changed from: private */
        public String mDeviceId;
        /* access modifiers changed from: private */
        public ImDirection mDirection;
        /* access modifiers changed from: private */
        public final Set<NotificationStatus> mDispNotification = new HashSet();
        /* access modifiers changed from: private */
        public long mDisplayedTimestamp;
        /* access modifiers changed from: private */
        public String mExtInfo;
        /* access modifiers changed from: private */
        public int mFlagMask;
        /* access modifiers changed from: private */
        public int mId;
        /* access modifiers changed from: private */
        public String mImdnId;
        /* access modifiers changed from: private */
        public String mImdnOriginalTo;
        /* access modifiers changed from: private */
        public List<ImImdnRecRoute> mImdnRecRouteList;
        /* access modifiers changed from: private */
        public IImServiceInterface mImsService;
        /* access modifiers changed from: private */
        public long mInsertedTimestamp;
        /* access modifiers changed from: private */
        public boolean mIsBroadcastMsg;
        /* access modifiers changed from: private */
        public boolean mIsRoutingMsg;
        /* access modifiers changed from: private */
        public boolean mIsSlmSvcMsg;
        /* access modifiers changed from: private */
        public boolean mIsVM2TextMsg;
        /* access modifiers changed from: private */
        public String mMaapTraficType;
        /* access modifiers changed from: private */
        public ImConstants.MessagingTech mMessagingTech;
        /* access modifiers changed from: private */
        public IMnoStrategy mMnoStrategy;
        /* access modifiers changed from: private */
        public IModuleInterface mModule;
        /* access modifiers changed from: private */
        public Network mNetwork;
        /* access modifiers changed from: private */
        public int mNotDisplayedCounter;
        /* access modifiers changed from: private */
        public NotificationStatus mNotificationStatus;
        private ImsUri mPreferredUri;
        /* access modifiers changed from: private */
        public String mRcsTrafficType;
        /* access modifiers changed from: private */
        public String mReferenceImdnId;
        /* access modifiers changed from: private */
        public String mReferenceType;
        /* access modifiers changed from: private */
        public String mReferenceValue;
        /* access modifiers changed from: private */
        public ImsUri mRemoteUri;
        /* access modifiers changed from: private */
        public String mRequestMessageId;
        /* access modifiers changed from: private */
        public ImConstants.RevocationStatus mRevocationStatus;
        /* access modifiers changed from: private */
        public RoutingType mRoutingType;
        /* access modifiers changed from: private */
        public long mSentTimestamp;
        /* access modifiers changed from: private */
        public String mSimIMSI;
        /* access modifiers changed from: private */
        public ISlmServiceInterface mSlmService;
        /* access modifiers changed from: private */
        public ImConstants.Status mStatus;
        /* access modifiers changed from: private */
        public String mSuggestion;
        /* access modifiers changed from: private */
        public ThumbnailTool mThumbnailTool;
        /* access modifiers changed from: private */
        public ImConstants.Type mType;
        /* access modifiers changed from: private */
        public UriGenerator mUriGenerator;
        /* access modifiers changed from: private */
        public String mUserAlias;

        /* access modifiers changed from: protected */
        public abstract T self();

        public Builder() {
            NotificationStatus notificationStatus = NotificationStatus.NONE;
            this.mNotificationStatus = notificationStatus;
            this.mDesiredNotificationStatus = notificationStatus;
            this.mRevocationStatus = ImConstants.RevocationStatus.NONE;
            this.mMessagingTech = ImConstants.MessagingTech.NORMAL;
        }

        public T module(IModuleInterface iModuleInterface) {
            this.mModule = iModuleInterface;
            return self();
        }

        public T imsService(IImServiceInterface iImServiceInterface) {
            this.mImsService = iImServiceInterface;
            return self();
        }

        public T slmService(ISlmServiceInterface iSlmServiceInterface) {
            this.mSlmService = iSlmServiceInterface;
            return self();
        }

        public T uriGenerator(UriGenerator uriGenerator) {
            this.mUriGenerator = uriGenerator;
            return self();
        }

        public T config(ImConfig imConfig) {
            this.mConfig = imConfig;
            return self();
        }

        public T thumbnailTool(ThumbnailTool thumbnailTool) {
            this.mThumbnailTool = thumbnailTool;
            return self();
        }

        public T id(int i) {
            this.mId = i;
            return self();
        }

        public T chatId(String str) {
            this.mChatId = str;
            return self();
        }

        public T imdnId(String str) {
            this.mImdnId = str;
            return self();
        }

        public T imdnIdOriginalTo(String str) {
            this.mImdnOriginalTo = str;
            return self();
        }

        public T imdnRecordRouteList(List<ImImdnRecRoute> list) {
            if (list != null) {
                this.mImdnRecRouteList = new ArrayList(list);
            }
            return self();
        }

        public T contentType(String str) {
            this.mContentType = str;
            return self();
        }

        public T type(ImConstants.Type type) {
            this.mType = type;
            return self();
        }

        public T isSlmSvcMsg(boolean z) {
            this.mIsSlmSvcMsg = z;
            return self();
        }

        public T status(ImConstants.Status status) {
            this.mStatus = status;
            return self();
        }

        public T sentTimestamp(long j) {
            this.mSentTimestamp = j;
            return self();
        }

        public T insertedTimestamp(long j) {
            this.mInsertedTimestamp = j;
            return self();
        }

        public T deliveredTimestamp(long j) {
            this.mDeliveredTimestamp = j;
            return self();
        }

        public T displayedTimestamp(long j) {
            this.mDisplayedTimestamp = j;
            return self();
        }

        public T remoteUri(ImsUri imsUri) {
            this.mRemoteUri = imsUri;
            return self();
        }

        public T direction(ImDirection imDirection) {
            this.mDirection = imDirection;
            return self();
        }

        public T userAlias(String str) {
            this.mUserAlias = str;
            return self();
        }

        public T dispNotification(Set<NotificationStatus> set) {
            if (set != null) {
                this.mDispNotification.addAll(set);
            }
            return self();
        }

        public T notificationStatus(NotificationStatus notificationStatus) {
            this.mNotificationStatus = notificationStatus;
            return self();
        }

        public T desiredNotificationStatus(NotificationStatus notificationStatus) {
            this.mDesiredNotificationStatus = notificationStatus;
            return self();
        }

        public T notDisplayedCounter(int i) {
            this.mNotDisplayedCounter = i;
            return self();
        }

        public T requestMessageId(String str) {
            this.mRequestMessageId = str;
            return self();
        }

        public T body(String str) {
            this.mBody = str;
            return self();
        }

        public T suggestion(String str) {
            this.mSuggestion = str;
            return self();
        }

        public T isBroadcastMsg(boolean z) {
            this.mIsBroadcastMsg = z;
            return self();
        }

        public T isVM2TextMsg(boolean z) {
            this.mIsVM2TextMsg = z;
            return self();
        }

        public T isRoutingMsg(boolean z) {
            this.mIsRoutingMsg = z;
            return self();
        }

        public T routingType(RoutingType routingType) {
            this.mRoutingType = routingType;
            return self();
        }

        public T mnoStrategy(IMnoStrategy iMnoStrategy) {
            this.mMnoStrategy = iMnoStrategy;
            return self();
        }

        public T network(Network network) {
            this.mNetwork = network;
            return self();
        }

        public T extinfo(String str) {
            this.mExtInfo = str;
            return self();
        }

        public T contributionId(String str) {
            this.mContributionId = str;
            return self();
        }

        public T conversationId(String str) {
            this.mConversationId = str;
            return self();
        }

        public T deviceId(String str) {
            this.mDeviceId = str;
            return self();
        }

        public T simIMSI(String str) {
            this.mSimIMSI = str;
            return self();
        }

        public T flagMask(int i) {
            this.mFlagMask = i;
            return self();
        }

        public T revocationStatus(ImConstants.RevocationStatus revocationStatus) {
            this.mRevocationStatus = revocationStatus;
            return self();
        }

        public T maapTrafficType(String str) {
            this.mMaapTraficType = str;
            return self();
        }

        public T messagingTech(ImConstants.MessagingTech messagingTech) {
            this.mMessagingTech = messagingTech;
            return self();
        }

        public T referenceImdnId(String str) {
            this.mReferenceImdnId = str;
            return self();
        }

        public T referenceType(String str) {
            this.mReferenceType = str;
            return self();
        }

        public T referenceValue(String str) {
            this.mReferenceValue = str;
            return self();
        }

        public T rcsTrafficType(String str) {
            this.mRcsTrafficType = str;
            return self();
        }
    }
}
