package com.sec.internal.ims.servicemodules.im;

import android.content.Context;
import android.net.Uri;
import android.os.Handler;
import android.os.Message;
import android.os.RemoteException;
import android.text.TextUtils;
import android.util.Log;
import com.sec.ims.ImsRegistration;
import com.sec.ims.im.IImSessionListener;
import com.sec.ims.util.ImsUri;
import com.sec.ims.util.NameAddr;
import com.sec.internal.constants.ims.SipErrorBase;
import com.sec.internal.constants.ims.cmstore.data.MessageContextValues;
import com.sec.internal.constants.ims.servicemodules.im.ChatData;
import com.sec.internal.constants.ims.servicemodules.im.ChatMode;
import com.sec.internal.constants.ims.servicemodules.im.ImCacheAction;
import com.sec.internal.constants.ims.servicemodules.im.ImConstants;
import com.sec.internal.constants.ims.servicemodules.im.ImError;
import com.sec.internal.constants.ims.servicemodules.im.ImIconData;
import com.sec.internal.constants.ims.servicemodules.im.ImParticipant;
import com.sec.internal.constants.ims.servicemodules.im.ImSubjectData;
import com.sec.internal.constants.ims.servicemodules.im.NotificationStatus;
import com.sec.internal.constants.ims.servicemodules.im.event.ImComposingEvent;
import com.sec.internal.constants.ims.servicemodules.im.event.ImIncomingMessageEvent;
import com.sec.internal.constants.ims.servicemodules.im.event.ImIncomingSessionEvent;
import com.sec.internal.constants.ims.servicemodules.im.event.ImSessionClosedEvent;
import com.sec.internal.constants.ims.servicemodules.im.event.ImSessionConferenceInfoUpdateEvent;
import com.sec.internal.constants.ims.servicemodules.im.event.ImSessionEstablishedEvent;
import com.sec.internal.constants.ims.servicemodules.im.event.ImdnNotificationEvent;
import com.sec.internal.constants.ims.servicemodules.im.event.SendImdnFailedEvent;
import com.sec.internal.constants.ims.servicemodules.im.event.SlmLMMIncomingSessionEvent;
import com.sec.internal.constants.ims.servicemodules.im.params.AcceptImSessionParams;
import com.sec.internal.constants.ims.servicemodules.im.params.AcceptSlmParams;
import com.sec.internal.constants.ims.servicemodules.im.params.RejectImSessionParams;
import com.sec.internal.constants.ims.servicemodules.im.params.RejectSlmParams;
import com.sec.internal.constants.ims.servicemodules.im.reason.ImErrorReason;
import com.sec.internal.constants.ims.servicemodules.im.reason.ImSessionClosedReason;
import com.sec.internal.constants.ims.servicemodules.im.reason.ImSessionRejectReason;
import com.sec.internal.constants.ims.servicemodules.im.reason.ImSessionStopReason;
import com.sec.internal.constants.ims.servicemodules.im.result.Result;
import com.sec.internal.helper.BlockedNumberUtil;
import com.sec.internal.helper.CollectionUtils;
import com.sec.internal.helper.FileUtils;
import com.sec.internal.helper.SimUtil;
import com.sec.internal.helper.os.DeviceUtil;
import com.sec.internal.helper.os.ImsGateConfig;
import com.sec.internal.ims.rcs.RcsPolicyManager;
import com.sec.internal.ims.registry.ImsRegistry;
import com.sec.internal.ims.servicemodules.im.ImSession;
import com.sec.internal.ims.servicemodules.im.interfaces.IRcsBigDataProcessor;
import com.sec.internal.ims.servicemodules.im.listener.IChatEventListener;
import com.sec.internal.ims.servicemodules.im.listener.IFtEventListener;
import com.sec.internal.ims.servicemodules.im.listener.IMessageEventListener;
import com.sec.internal.ims.servicemodules.im.listener.ImSessionListener;
import com.sec.internal.ims.servicemodules.im.strategy.ChnStrategy;
import com.sec.internal.ims.servicemodules.im.strategy.IMnoStrategy;
import com.sec.internal.ims.settings.RcsPolicySettings;
import com.sec.internal.ims.util.ImsUtil;
import com.sec.internal.ims.util.StringIdGenerator;
import com.sec.internal.imscr.LogClass;
import com.sec.internal.interfaces.ims.core.IRegistrationGovernor;
import com.sec.internal.interfaces.ims.servicemodules.im.IImServiceInterface;
import com.sec.internal.interfaces.ims.servicemodules.im.ISlmServiceInterface;
import com.sec.internal.log.IMSLog;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Date;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.TreeMap;
import java.util.concurrent.Future;
import java.util.concurrent.FutureTask;

public class ImSessionProcessor extends Handler implements ImSessionListener {
    private static final int EVENT_RESET_INCOMING_SESSION_FOR_A2P = 2;
    private static final int EVENT_VOLUNTARY_DEPARTURE_GROUPCHAT = 1;
    private static final String LOG_TAG = ImSessionProcessor.class.getSimpleName();
    private final IRcsBigDataProcessor mBigDataProcessor;
    private ImCache mCache;
    protected final List<IChatEventListener> mChatEventListeners = new ArrayList();
    private Context mContext;
    private FtProcessor mFtProcessor;
    private GcmHandler mGcmHandler;
    private final List<ImSession> mGroupChatsForDeparture = new ArrayList();
    private ImModule mImModule;
    private ImProcessor mImProcessor;
    private final ImRevocationHandler mImRevocationHandler;
    private final IImServiceInterface mImService;
    private final Map<Integer, ArrayList<IImSessionListener>> mImSessionListener;
    private ImTranslation mImTranslation;
    private ImdnHandler mImdnHandler;
    private final ISlmServiceInterface mSlmService;

    public ImSessionProcessor(Context context, IImServiceInterface iImServiceInterface, ISlmServiceInterface iSlmServiceInterface, ImModule imModule, ImCache imCache) {
        super(imModule.getLooper());
        this.mContext = context;
        this.mImService = iImServiceInterface;
        this.mImModule = imModule;
        this.mCache = imCache;
        this.mSlmService = iSlmServiceInterface;
        this.mImSessionListener = new HashMap();
        this.mBigDataProcessor = new ImBigDataProcessor(context, imModule);
        this.mImRevocationHandler = new ImRevocationHandler(context, imModule, imCache, this);
    }

    /* access modifiers changed from: protected */
    public void init(ImProcessor imProcessor, FtProcessor ftProcessor, ImTranslation imTranslation) {
        this.mImProcessor = imProcessor;
        this.mFtProcessor = ftProcessor;
        this.mImTranslation = imTranslation;
        this.mImdnHandler = new ImdnHandler(this.mContext, this.mImModule, this.mCache, imProcessor, ftProcessor, this);
        this.mGcmHandler = new GcmHandler(this.mImModule, this.mCache, this, imTranslation);
    }

    /* access modifiers changed from: protected */
    public void registerChatEventListener(IChatEventListener iChatEventListener) {
        this.mChatEventListeners.add(iChatEventListener);
    }

    public void handleMessage(Message message) {
        super.handleMessage(message);
        int i = message.what;
        if (i == 1) {
            handleEventVoluntaryDeparture();
        } else if (i == 2) {
            int intValue = ((Integer) message.obj).intValue();
            String str = LOG_TAG;
            Log.i(str, "EVENT_RESET_INCOMING_SESSION_FOR_A2P: phoneId = " + intValue);
            this.mImModule.mHasIncomingSessionForA2P.put(intValue, Boolean.FALSE);
        }
    }

    public void onChatEstablished(ImSession imSession) {
        for (IChatEventListener onChatEstablished : this.mChatEventListeners) {
            onChatEstablished.onChatEstablished(imSession.getChatId(), imSession.getDirection(), imSession.getSessionUri(), imSession.mRemoteAcceptTypes, imSession.mRemoteAcceptWrappedTypes);
        }
        notifyImSessionEstablished(this.mImModule.getPhoneIdByIMSI(imSession.getOwnImsi()));
    }

    public void onChatStatusUpdate(ImSession imSession, ImSession.SessionState sessionState) {
        for (IChatEventListener onChatUpdateState : this.mChatEventListeners) {
            onChatUpdateState.onChatUpdateState(imSession.getChatId(), imSession.getDirection(), sessionState);
        }
    }

    public void onChatClosed(ImSession imSession, ImSessionClosedReason imSessionClosedReason) {
        if (imSessionClosedReason != ImSessionClosedReason.NONE) {
            for (IChatEventListener onChatClosed : this.mChatEventListeners) {
                onChatClosed.onChatClosed(imSession.getChatId(), imSession.getDirection(), imSessionClosedReason);
            }
        }
        this.mCache.removeActiveSession(imSession);
        notifyImSessionClosed(this.mImModule.getPhoneIdByIMSI(imSession.getOwnImsi()));
    }

    public void onChatDeparted(ImSession imSession) {
        if (imSession == null) {
            Log.e(LOG_TAG, "onChatDeparted : invalid ImSession");
            return;
        }
        String str = LOG_TAG;
        Log.i(str, "onChatDeparted : " + imSession.getChatId() + ", isReusable=" + imSession.isReusable());
        if (imSession.isReusable()) {
            imSession.updateChatState(ChatData.State.NONE);
        } else {
            this.mCache.deleteSession(imSession);
        }
        this.mGroupChatsForDeparture.remove(imSession);
    }

    /* access modifiers changed from: protected */
    public void onSendImdnFailed(SendImdnFailedEvent sendImdnFailedEvent) {
        this.mImdnHandler.onSendImdnFailed(sendImdnFailedEvent);
    }

    public void onComposingReceived(ImSession imSession, ImsUri imsUri, String str, boolean z, int i) {
        String str2 = LOG_TAG;
        Log.i(str2, "notifyComposingReceived: " + imSession.getChatId() + " isComposing:" + z);
        for (IChatEventListener onComposingNotificationReceived : this.mChatEventListeners) {
            onComposingNotificationReceived.onComposingNotificationReceived(imSession.getChatId(), imSession.isGroupChat(), imsUri, str, z, i);
        }
    }

    /* access modifiers changed from: protected */
    public void onImdnNotificationReceived(ImdnNotificationEvent imdnNotificationEvent) {
        if (imdnNotificationEvent.mStatus == NotificationStatus.CANCELED) {
            post(new ImSessionProcessor$$ExternalSyntheticLambda15(this, imdnNotificationEvent));
        } else {
            this.mImdnHandler.onImdnNotificationReceived(imdnNotificationEvent);
        }
    }

    /* access modifiers changed from: private */
    public /* synthetic */ void lambda$onImdnNotificationReceived$0(ImdnNotificationEvent imdnNotificationEvent) {
        this.mImdnHandler.onCanceledNotificationReceived(imdnNotificationEvent);
    }

    /* access modifiers changed from: private */
    public /* synthetic */ void lambda$onComposingNotificationReceived$1(ImComposingEvent imComposingEvent) {
        this.mImdnHandler.onComposingNotificationReceived(imComposingEvent);
    }

    /* access modifiers changed from: protected */
    public void onComposingNotificationReceived(ImComposingEvent imComposingEvent) {
        post(new ImSessionProcessor$$ExternalSyntheticLambda4(this, imComposingEvent));
    }

    public void getComposingActiveUris(String str) {
        post(new ImSessionProcessor$$ExternalSyntheticLambda2(this, str));
    }

    /* access modifiers changed from: private */
    public /* synthetic */ void lambda$getComposingActiveUris$2(String str) {
        String str2 = LOG_TAG;
        Log.i(str2, "getComposingActiveUris: chatId=" + str);
        ImSession imSession = this.mCache.getImSession(str);
        if (imSession == null) {
            Log.e(str2, "Session not found in the cache.");
            this.mImTranslation.notifyComposingActiveUris(str, (Set<ImsUri>) null);
            return;
        }
        this.mImTranslation.notifyComposingActiveUris(str, imSession.getComposingActiveUris());
    }

    public void onAddParticipantsSucceeded(String str, List<ImsUri> list) {
        String str2 = LOG_TAG;
        Log.i(str2, "onAddParticipantsSucceeded: " + str);
        for (IChatEventListener onAddParticipantsSucceeded : this.mChatEventListeners) {
            onAddParticipantsSucceeded.onAddParticipantsSucceeded(str, list);
        }
    }

    public void onAddParticipantsFailed(String str, List<ImsUri> list, ImErrorReason imErrorReason) {
        ImSession imSession = this.mCache.getImSession(str);
        if (imSession != null) {
            int phoneId = imSession.getPhoneId();
            ImDump imDump = this.mImModule.getImDump();
            imDump.addEventLogs("onAddParticipantsFailed: chatId=" + imSession.getChatId() + ", convId=" + imSession.getConversationId() + ", contId=" + imSession.getContributionId() + ", " + IMSLog.numberChecker((Collection<ImsUri>) list) + ", error=" + imErrorReason);
            ArrayList arrayList = new ArrayList();
            arrayList.add(imErrorReason.toString());
            ImsUtil.listToDumpFormat(LogClass.IM_ADD_PARTICIPANT_RES, phoneId, str, arrayList);
            for (IChatEventListener onAddParticipantsFailed : this.mChatEventListeners) {
                onAddParticipantsFailed.onAddParticipantsFailed(str, list, imErrorReason);
            }
        }
    }

    public void onRemoveParticipantsSucceeded(String str, List<ImsUri> list) {
        for (IChatEventListener onRemoveParticipantsSucceeded : this.mChatEventListeners) {
            onRemoveParticipantsSucceeded.onRemoveParticipantsSucceeded(str, list);
        }
    }

    public void onRemoveParticipantsFailed(String str, List<ImsUri> list, ImErrorReason imErrorReason) {
        ImSession imSession = this.mCache.getImSession(str);
        if (imSession != null) {
            int phoneId = imSession.getPhoneId();
            ImDump imDump = this.mImModule.getImDump();
            imDump.addEventLogs("onRemoveParticipantsFailed: chatId=" + imSession.getChatId() + ", convId=" + imSession.getConversationId() + ", contId=" + imSession.getContributionId() + ", " + IMSLog.numberChecker((Collection<ImsUri>) list) + ", error=" + imErrorReason);
            ArrayList arrayList = new ArrayList();
            arrayList.add(imErrorReason.toString());
            ImsUtil.listToDumpFormat(LogClass.IM_REMOVE_PARTICIPANT_RES, phoneId, str, arrayList);
            for (IChatEventListener onRemoveParticipantsFailed : this.mChatEventListeners) {
                onRemoveParticipantsFailed.onRemoveParticipantsFailed(str, list, imErrorReason);
            }
        }
    }

    public void onChangeGroupChatLeaderSucceeded(String str, List<ImsUri> list) {
        for (IChatEventListener onChangeGroupChatLeaderSucceeded : this.mChatEventListeners) {
            onChangeGroupChatLeaderSucceeded.onChangeGroupChatLeaderSucceeded(str, list);
        }
    }

    public void onChangeGroupChatLeaderFailed(String str, List<ImsUri> list, ImErrorReason imErrorReason) {
        for (IChatEventListener onChangeGroupChatLeaderFailed : this.mChatEventListeners) {
            onChangeGroupChatLeaderFailed.onChangeGroupChatLeaderFailed(str, list, imErrorReason);
        }
    }

    public void onChangeGroupChatSubjectSucceeded(String str, String str2) {
        ImSession imSession = this.mCache.getImSession(str);
        if (imSession != null) {
            ImDump imDump = this.mImModule.getImDump();
            imDump.addEventLogs("onChangeGroupChatSubjectSucceeded: chatId=" + imSession.getChatId() + ", convId=" + imSession.getConversationId() + ", contId=" + imSession.getContributionId() + ", subject=" + IMSLog.checker(str2));
            for (IChatEventListener onChangeGroupChatSubjectSucceeded : this.mChatEventListeners) {
                onChangeGroupChatSubjectSucceeded.onChangeGroupChatSubjectSucceeded(str, str2);
            }
        }
    }

    public void onChangeGroupChatSubjectFailed(String str, String str2, ImErrorReason imErrorReason) {
        ImSession imSession = this.mCache.getImSession(str);
        if (imSession != null) {
            ImDump imDump = this.mImModule.getImDump();
            imDump.addEventLogs("onChangeGroupChatSubjectFailed: chatId=" + imSession.getChatId() + ", convId=" + imSession.getConversationId() + ", contId=" + imSession.getContributionId() + ", subject=" + IMSLog.checker(str2) + ", error=" + imErrorReason);
            for (IChatEventListener onChangeGroupChatSubjectFailed : this.mChatEventListeners) {
                onChangeGroupChatSubjectFailed.onChangeGroupChatSubjectFailed(str, str2, imErrorReason);
            }
        }
    }

    public void onChangeGroupChatIconSuccess(String str, String str2) {
        ImSession imSession = this.mCache.getImSession(str);
        if (imSession != null) {
            ImDump imDump = this.mImModule.getImDump();
            imDump.addEventLogs("onChangeGroupChatIconSuccess: chatId=" + imSession.getChatId() + ", convId=" + imSession.getConversationId() + ", contId=" + imSession.getContributionId());
            for (IChatEventListener onChangeGroupChatIconSuccess : this.mChatEventListeners) {
                onChangeGroupChatIconSuccess.onChangeGroupChatIconSuccess(str, str2);
            }
        }
    }

    public void onChangeGroupChatIconFailed(String str, String str2, ImErrorReason imErrorReason) {
        ImSession imSession = this.mCache.getImSession(str);
        if (imSession != null) {
            ImDump imDump = this.mImModule.getImDump();
            imDump.addEventLogs("onChangeGroupChatIconFailed: chatId=" + imSession.getChatId() + ", convId=" + imSession.getConversationId() + ", contId=" + imSession.getContributionId() + ", error=" + imErrorReason);
            for (IChatEventListener onChangeGroupChatIconFailed : this.mChatEventListeners) {
                onChangeGroupChatIconFailed.onChangeGroupChatIconFailed(str, str2, imErrorReason);
            }
        }
    }

    public void onChangeGroupAliasSucceeded(String str, String str2) {
        for (IChatEventListener onChangeGroupAliasSucceeded : this.mChatEventListeners) {
            onChangeGroupAliasSucceeded.onChangeGroupAliasSucceeded(str, str2);
        }
    }

    public void onChangeGroupAliasFailed(String str, String str2, ImErrorReason imErrorReason) {
        for (IChatEventListener onChangeGroupAliasFailed : this.mChatEventListeners) {
            onChangeGroupAliasFailed.onChangeGroupAliasFailed(str, str2, imErrorReason);
        }
    }

    public void onParticipantsInserted(ImSession imSession, Collection<ImParticipant> collection) {
        if (imSession != null) {
            String str = LOG_TAG;
            Log.i(str, "onParticipantsInserted: " + imSession.getChatId() + ", " + IMSLog.checker(collection));
            ImDump imDump = this.mImModule.getImDump();
            imDump.addEventLogs("onParticipantsInserted: chatId=" + imSession.getChatId() + ", convId=" + imSession.getConversationId() + ", contId=" + imSession.getContributionId() + ", " + collection);
            this.mCache.addParticipant(collection);
            imSession.addParticipant(collection);
        }
    }

    public void onParticipantsUpdated(ImSession imSession, Collection<ImParticipant> collection) {
        if (imSession != null) {
            String str = LOG_TAG;
            Log.i(str, "onParticipantsUpdated: " + imSession.getChatId() + ", " + collection);
            ImDump imDump = this.mImModule.getImDump();
            imDump.addEventLogs("onParticipantsUpdated: chatId=" + imSession.getChatId() + ", convId=" + imSession.getConversationId() + ", contId=" + imSession.getContributionId() + ", participants= " + collection);
            this.mCache.updateParticipant(collection);
        }
    }

    public void onParticipantsDeleted(ImSession imSession, Collection<ImParticipant> collection) {
        if (imSession != null) {
            String str = LOG_TAG;
            Log.i(str, "onParticipantsDeleted: " + imSession.getChatId() + ", " + collection);
            ImDump imDump = this.mImModule.getImDump();
            imDump.addEventLogs("onParticipantsDeleted: chatId=" + imSession.getChatId() + ", convId=" + imSession.getConversationId() + ", contId=" + imSession.getContributionId() + ", participants= " + collection);
            this.mCache.deleteParticipant(collection);
            imSession.deleteParticipant(collection);
        }
    }

    public void onHandleParticipants(ImSession imSession, Collection<ImParticipant> collection, ImCacheAction imCacheAction) {
        ImDump imDump = this.mImModule.getImDump();
        imDump.addEventLogs("onHandleParticipants: chatId=" + imSession.getChatId() + ", convId=" + imSession.getConversationId() + ", contId=" + imSession.getContributionId() + ", participants= " + collection + ", action=" + imCacheAction);
        if (imCacheAction == ImCacheAction.INSERTED) {
            this.mCache.addParticipantFromCloud(collection);
            imSession.addParticipant(collection);
        } else if (imCacheAction == ImCacheAction.DELETED) {
            this.mCache.deleteParticipantFromCloud(collection);
            imSession.deleteParticipant(collection);
        } else if (imCacheAction == ImCacheAction.UPDATED) {
            this.mCache.updateParticipant(collection);
        }
    }

    public void onNotifyParticipantsAdded(ImSession imSession, Map<ImParticipant, Date> map) {
        String str = LOG_TAG;
        Log.i(str, "onNotifyParticipantsAdded: " + imSession.getChatId() + ", " + map);
        ImDump imDump = this.mImModule.getImDump();
        imDump.addEventLogs("onNotifyParticipantsAdded: chatId=" + imSession.getChatId() + ", convId=" + imSession.getConversationId() + ", contId=" + imSession.getContributionId() + ", participants= " + map);
        makeNewSystemUserMessage(imSession, map, ImConstants.Type.SYSTEM_USER_JOINED);
        for (IChatEventListener onParticipantsAdded : this.mChatEventListeners) {
            onParticipantsAdded.onParticipantsAdded(imSession, map.keySet());
        }
    }

    public void onNotifyParticipantsJoined(ImSession imSession, Map<ImParticipant, Date> map) {
        if (imSession != null) {
            String str = LOG_TAG;
            Log.i(str, "onNotifyParticipantsJoined: " + imSession.getChatId() + ", " + map);
            ImDump imDump = this.mImModule.getImDump();
            imDump.addEventLogs("onNotifyParticipantsJoined: chatId=" + imSession.getChatId() + ", convId=" + imSession.getConversationId() + ", contId=" + imSession.getContributionId() + ", participants= " + map);
            makeNewSystemUserMessage(imSession, map, ImConstants.Type.SYSTEM_USER_JOINED);
            for (IChatEventListener onParticipantsJoined : this.mChatEventListeners) {
                onParticipantsJoined.onParticipantsJoined(imSession, map.keySet());
            }
        }
    }

    public void onNotifyParticipantsLeft(ImSession imSession, Map<ImParticipant, Date> map) {
        if (imSession != null) {
            String str = LOG_TAG;
            Log.i(str, "onNotifyParticipantsLeft: " + imSession.getChatId() + ", " + map);
            ImDump imDump = this.mImModule.getImDump();
            imDump.addEventLogs("onNotifyParticipantsLeft: chatId=" + imSession.getChatId() + ", convId=" + imSession.getConversationId() + ", contId=" + imSession.getContributionId() + ", participants= " + map);
            makeNewSystemUserMessage(imSession, map, ImConstants.Type.SYSTEM_USER_LEFT);
            for (IChatEventListener onParticipantsLeft : this.mChatEventListeners) {
                onParticipantsLeft.onParticipantsLeft(imSession, map.keySet());
            }
        }
    }

    public void onNotifyParticipantsKickedOut(ImSession imSession, Map<ImParticipant, Date> map) {
        if (imSession != null) {
            String str = LOG_TAG;
            Log.i(str, "onNotifyParticipantsKickedOut: " + imSession.getChatId() + ", " + map);
            ImDump imDump = this.mImModule.getImDump();
            imDump.addEventLogs("onNotifyParticipantsKickedOut: chatId=" + imSession.getChatId() + ", convId=" + imSession.getConversationId() + ", contId=" + imSession.getContributionId() + ", participants= " + map);
            makeNewSystemUserMessage(imSession, map, ImConstants.Type.SYSTEM_USER_KICKOUT);
            for (IChatEventListener onParticipantsLeft : this.mChatEventListeners) {
                onParticipantsLeft.onParticipantsLeft(imSession, map.keySet());
            }
        }
    }

    public void onGroupChatLeaderChanged(ImSession imSession, String str) {
        if (imSession != null) {
            String str2 = LOG_TAG;
            Log.i(str2, "onGroupChatLeaderChanged: " + imSession.getChatId() + ", " + str);
            ImDump imDump = this.mImModule.getImDump();
            imDump.addEventLogs("onGroupChatLeaderChanged: chatId=" + imSession.getChatId() + ", convId=" + imSession.getConversationId() + ", contId=" + imSession.getContributionId() + ", leader= " + str);
            this.mCache.makeNewSystemUserMessage(imSession, str, ImConstants.Type.SYSTEM_LEADER_CHANGED);
            for (IChatEventListener onGroupChatLeaderUpdated : this.mChatEventListeners) {
                onGroupChatLeaderUpdated.onGroupChatLeaderUpdated(imSession.getChatId(), str);
            }
        }
    }

    public void onGroupChatLeaderInformed(ImSession imSession, String str) {
        String str2 = LOG_TAG;
        Log.i(str2, "onGroupChatLeaderInformed: " + IMSLog.numberChecker(str));
        this.mCache.makeNewSystemUserMessage(imSession, str, ImConstants.Type.SYSTEM_LEADER_INFORMED);
    }

    public void onIncomingSessionProcessed(ImIncomingMessageEvent imIncomingMessageEvent, ImSession imSession, boolean z) {
        String str = LOG_TAG;
        Log.i(str, "onIncomingSessionProcessed, need to notify?: " + z);
        this.mCache.updateActiveSession(imSession);
        int phoneIdByIMSI = this.mImModule.getPhoneIdByIMSI(imSession.getChatData().getOwnIMSI());
        if (this.mImModule.getImConfig(phoneIdByIMSI).getUserAliasEnabled() && !this.mImModule.getImConfig(phoneIdByIMSI).getRealtimeUserAliasAuth() && !imSession.isGroupChat()) {
            if (imIncomingMessageEvent != null) {
                ImsUri normalizeUri = this.mImModule.normalizeUri(phoneIdByIMSI, imIncomingMessageEvent.mSender);
                if (normalizeUri != null) {
                    imSession.updateParticipantAlias(imIncomingMessageEvent.mUserAlias, imSession.getParticipant(normalizeUri));
                }
            } else if (!imSession.getParticipants().isEmpty()) {
                imSession.updateParticipantAlias(imSession.getInitiatorAlias(), imSession.getParticipants().iterator().next());
            }
        }
        if (z) {
            for (IChatEventListener onChatInvitationReceived : this.mChatEventListeners) {
                onChatInvitationReceived.onChatInvitationReceived(imSession);
            }
        }
        onIncomingMessageProcessed(imIncomingMessageEvent, imSession);
    }

    public void onIncomingMessageProcessed(ImIncomingMessageEvent imIncomingMessageEvent, ImSession imSession) {
        if (imIncomingMessageEvent != null && !TextUtils.isEmpty(imIncomingMessageEvent.mBody)) {
            String str = LOG_TAG;
            Log.i(str, "Received a message in INVITE : " + imIncomingMessageEvent.mImdnMessageId);
            imIncomingMessageEvent.mChatId = imSession.getChatId();
            this.mImProcessor.onIncomingMessageReceived(imIncomingMessageEvent);
        }
    }

    public void onImErrorReport(ImError imError, int i) {
        String str = LOG_TAG;
        Log.i(str, "onImErrorReport");
        ImsRegistration imsRegistration = this.mImModule.getImsRegistration(i);
        if (imsRegistration != null) {
            IRegistrationGovernor registrationGovernor = ImsRegistry.getRegistrationManager().getRegistrationGovernor(imsRegistration.getHandle());
            ArrayList arrayList = new ArrayList();
            arrayList.add(String.valueOf(imError.ordinal()));
            ImsUtil.listToDumpFormat(LogClass.IM_IMERRORREPORT, i, MessageContextValues.none, arrayList);
            if (registrationGovernor != null) {
                int i2 = AnonymousClass1.$SwitchMap$com$sec$internal$constants$ims$servicemodules$im$ImError[imError.ordinal()];
                if (i2 == 1) {
                    Log.i(str, "onImErrorReport : 403 forbidden no warning header, try re-regi");
                    registrationGovernor.onSipError("im", SipErrorBase.FORBIDDEN);
                } else if (i2 == 2) {
                    Log.i(str, "onImErrorReport : 403 forbidden service not authorised");
                    registrationGovernor.onSipError("im", SipErrorBase.FORBIDDEN_SERVICE_NOT_AUTHORISED);
                }
            }
        }
    }

    /* renamed from: com.sec.internal.ims.servicemodules.im.ImSessionProcessor$1  reason: invalid class name */
    static /* synthetic */ class AnonymousClass1 {
        static final /* synthetic */ int[] $SwitchMap$com$sec$internal$constants$ims$servicemodules$im$ImError;

        /* JADX WARNING: Can't wrap try/catch for region: R(6:0|1|2|3|4|6) */
        /* JADX WARNING: Code restructure failed: missing block: B:7:?, code lost:
            return;
         */
        /* JADX WARNING: Failed to process nested try/catch */
        /* JADX WARNING: Missing exception handler attribute for start block: B:3:0x0012 */
        static {
            /*
                com.sec.internal.constants.ims.servicemodules.im.ImError[] r0 = com.sec.internal.constants.ims.servicemodules.im.ImError.values()
                int r0 = r0.length
                int[] r0 = new int[r0]
                $SwitchMap$com$sec$internal$constants$ims$servicemodules$im$ImError = r0
                com.sec.internal.constants.ims.servicemodules.im.ImError r1 = com.sec.internal.constants.ims.servicemodules.im.ImError.FORBIDDEN_NO_WARNING_HEADER     // Catch:{ NoSuchFieldError -> 0x0012 }
                int r1 = r1.ordinal()     // Catch:{ NoSuchFieldError -> 0x0012 }
                r2 = 1
                r0[r1] = r2     // Catch:{ NoSuchFieldError -> 0x0012 }
            L_0x0012:
                int[] r0 = $SwitchMap$com$sec$internal$constants$ims$servicemodules$im$ImError     // Catch:{ NoSuchFieldError -> 0x001d }
                com.sec.internal.constants.ims.servicemodules.im.ImError r1 = com.sec.internal.constants.ims.servicemodules.im.ImError.FORBIDDEN_SERVICE_NOT_AUTHORISED     // Catch:{ NoSuchFieldError -> 0x001d }
                int r1 = r1.ordinal()     // Catch:{ NoSuchFieldError -> 0x001d }
                r2 = 2
                r0[r1] = r2     // Catch:{ NoSuchFieldError -> 0x001d }
            L_0x001d:
                return
            */
            throw new UnsupportedOperationException("Method not decompiled: com.sec.internal.ims.servicemodules.im.ImSessionProcessor.AnonymousClass1.<clinit>():void");
        }
    }

    public void onProcessingFileTransferChanged(ImSession imSession) {
        this.mFtProcessor.notifyOngoingFtEvent(imSession.mProcessingFileTransfer.isEmpty() && !this.mCache.hasProcessingFileTransfer(), this.mImModule.getPhoneIdByIMSI(imSession.getOwnImsi()));
    }

    public void onChatSubjectUpdated(String str, ImSubjectData imSubjectData) {
        ImSession imSession = this.mCache.getImSession(str);
        if (imSession != null) {
            ImDump imDump = this.mImModule.getImDump();
            imDump.addEventLogs("onChatSubjectUpdated: chatId=" + imSession.getChatId() + ", convId=" + imSession.getConversationId() + ", contId=" + imSession.getContributionId() + ", subject=" + IMSLog.checker(imSubjectData.getSubject()));
            for (IChatEventListener onChatSubjectUpdated : this.mChatEventListeners) {
                onChatSubjectUpdated.onChatSubjectUpdated(str, imSubjectData);
            }
        }
    }

    public void onGroupChatIconUpdated(String str, ImIconData imIconData) {
        ImSession imSession = this.mCache.getImSession(str);
        if (imSession != null) {
            ImDump imDump = this.mImModule.getImDump();
            imDump.addEventLogs("onGroupChatIconUpdated: chatId=" + imSession.getChatId() + ", convId=" + imSession.getConversationId() + ", contId=" + imSession.getContributionId());
            for (IChatEventListener onGroupChatIconUpdated : this.mChatEventListeners) {
                onGroupChatIconUpdated.onGroupChatIconUpdated(str, imIconData);
            }
        }
    }

    public void onGroupChatIconDeleted(String str) {
        ImSession imSession = this.mCache.getImSession(str);
        if (imSession != null) {
            ImDump imDump = this.mImModule.getImDump();
            imDump.addEventLogs("onGroupChatIconDeleted: chatId=" + imSession.getChatId() + ", convId=" + imSession.getConversationId() + ", contId=" + imSession.getContributionId());
            for (IChatEventListener onGroupChatIconDeleted : this.mChatEventListeners) {
                onGroupChatIconDeleted.onGroupChatIconDeleted(str);
            }
        }
    }

    public void onParticipantAliasUpdated(String str, ImParticipant imParticipant) {
        for (IChatEventListener onParticipantAliasUpdated : this.mChatEventListeners) {
            onParticipantAliasUpdated.onParticipantAliasUpdated(str, imParticipant);
        }
    }

    public void onBlockedMessageReceived(ImIncomingMessageEvent imIncomingMessageEvent) {
        this.mImProcessor.onIncomingMessageReceived(imIncomingMessageEvent);
    }

    public void onRequestSendMessage(ImSession imSession, MessageBase messageBase) {
        this.mImProcessor.sendMessage(imSession, messageBase);
    }

    /* access modifiers changed from: protected */
    public Future<ImSession> createChat(List<ImsUri> list, String str, String str2, int i, String str3) {
        return createChat(0, list, str, str2, i, str3, false, false, (String) null, (Uri) null, false, false, (String) null, (String) null, (ImsUri) null);
    }

    public Future<ImSession> createChat(int i, List<ImsUri> list, String str, String str2, int i2, String str3, boolean z, boolean z2, String str4, Uri uri, boolean z3, boolean z4) {
        return createChat(i, list, str, str2, i2, str3, z, z2, str4, uri, z3, z4, (String) null, (String) null, (ImsUri) null);
    }

    /* access modifiers changed from: protected */
    public Future<ImSession> createChat(int i, List<ImsUri> list, String str, String str2, int i2, String str3, boolean z, boolean z2, String str4, Uri uri, boolean z3, boolean z4, String str5, String str6, ImsUri imsUri) {
        ImSessionProcessor$$ExternalSyntheticLambda17 imSessionProcessor$$ExternalSyntheticLambda17 = r0;
        ImSessionProcessor$$ExternalSyntheticLambda17 imSessionProcessor$$ExternalSyntheticLambda172 = new ImSessionProcessor$$ExternalSyntheticLambda17(this, i, list, str, str2, i2, str3, z, z2, str4, uri, z3, z4, str5, str6, imsUri);
        FutureTask futureTask = new FutureTask(imSessionProcessor$$ExternalSyntheticLambda17);
        post(futureTask);
        return futureTask;
    }

    /* access modifiers changed from: private */
    public /* synthetic */ ImSession lambda$createChat$3(int i, List list, String str, String str2, int i2, String str3, boolean z, boolean z2, String str4, Uri uri, boolean z3, boolean z4, String str5, String str6, ImsUri imsUri) throws Exception {
        String str7;
        IMnoStrategy iMnoStrategy;
        ChatData.ChatType chatType;
        boolean z5;
        ChatMode chatMode;
        ImSession imSession;
        boolean z6;
        ChatData.ChatType chatType2;
        IMnoStrategy iMnoStrategy2;
        int i3 = i;
        int i4 = i2;
        String str8 = str3;
        boolean z7 = z;
        boolean z8 = z2;
        String str9 = str4;
        Uri uri2 = uri;
        boolean z9 = z3;
        boolean z10 = z4;
        String str10 = LOG_TAG;
        IMSLog.i(str10, i3, "createChat: participants=" + IMSLog.numberChecker((Collection<ImsUri>) list) + ", subject=" + IMSLog.checker(str) + ", sdpContentType=" + str2 + ", threadId=" + i4 + ", requestMessageId=" + str8 + ", isBroadcastChat=" + z7 + ", isClosedGC=" + z8 + ", iconName=" + str9 + ", iconUri=" + uri2 + ", isTokenUsed=" + z9 + ", isTokenLink=" + z10 + ", conversationId=" + str5 + ", contributionId=" + str6 + ", sessionUri=" + imsUri);
        String imsiFromPhoneId = this.mImModule.getImsiFromPhoneId(i3);
        ImSession imSession2 = null;
        if (this.mImModule.isRegistered(i3) || RcsPolicyManager.getRcsStrategy(i).boolSetting(RcsPolicySettings.RcsPolicy.PENDING_FOR_REGI)) {
            Set<ImsUri> normalizeUri = this.mImModule.normalizeUri(i3, (Collection<ImsUri>) new HashSet(list));
            if (normalizeUri == null || normalizeUri.isEmpty()) {
                int i5 = i3;
                Log.i(str10, "createChat: normalizedParticipants is null or empty");
                for (IChatEventListener onCreateChatFailed : this.mChatEventListeners) {
                    onCreateChatFailed.onCreateChatFailed(i5, i4, ImErrorReason.INVALID, str8);
                }
                return null;
            }
            boolean z11 = normalizeUri.size() > 1 || !TextUtils.isEmpty(str6);
            if (z7 || !z11 || this.mImModule.getImConfig(i3).getGroupChatEnabled()) {
                if (!z11 || uri2 == null) {
                    str7 = null;
                } else {
                    String copyFileToCacheFromUri = FileUtils.copyFileToCacheFromUri(this.mContext, str9, uri2);
                    if (TextUtils.isEmpty(copyFileToCacheFromUri)) {
                        Log.e(str10, "icon file doesn't exist");
                        for (IChatEventListener onCreateChatFailed2 : this.mChatEventListeners) {
                            onCreateChatFailed2.onCreateChatFailed(i3, i4, ImErrorReason.INVALID_ICON_PATH, str8);
                        }
                        return null;
                    }
                    str7 = copyFileToCacheFromUri;
                }
                IMnoStrategy rcsStrategy = RcsPolicyManager.getRcsStrategy(i);
                ChatData.ChatType generateChatType = generateChatType(z11, z8 && rcsStrategy.boolSetting(RcsPolicySettings.RcsPolicy.PARTICIPANTBASED_CLOSED_GROUPCHAT), z7);
                ChatMode chatMode2 = ChatMode.OFF;
                if (z9 && !z10) {
                    chatMode2 = ChatMode.ON;
                }
                ChatMode chatMode3 = chatMode2;
                if (!z11 || generateChatType == ChatData.ChatType.PARTICIPANT_BASED_GROUP_CHAT) {
                    imSession2 = this.mCache.getImSessionByParticipants(normalizeUri, generateChatType, imsiFromPhoneId, chatMode3);
                }
                if (z11 || imSession2 != null) {
                    chatType = generateChatType;
                    iMnoStrategy = rcsStrategy;
                    chatMode = chatMode3;
                    z5 = z11;
                    imSession = imSession2;
                } else {
                    String generateChatId = StringIdGenerator.generateChatId(normalizeUri, imsiFromPhoneId, false, chatMode3.getId());
                    imSession = this.mCache.getImSession(generateChatId);
                    chatType = generateChatType;
                    if (imSession != null) {
                        iMnoStrategy = rcsStrategy;
                        if (imSession.getParticipantsSize() < 1) {
                            ArrayList arrayList = new ArrayList();
                            chatMode = chatMode3;
                            z5 = z11;
                            arrayList.add(new ImParticipant(generateChatId, ImParticipant.Status.INVITED, normalizeUri.iterator().next()));
                            Log.e(str10, "createChat() : error, participant table is empty");
                            onParticipantsInserted(imSession, arrayList);
                        }
                    } else {
                        iMnoStrategy = rcsStrategy;
                    }
                    chatMode = chatMode3;
                    z5 = z11;
                }
                if (imSession == null) {
                    chatType2 = chatType;
                    ChatMode chatMode4 = chatMode;
                    z6 = z5;
                    iMnoStrategy2 = iMnoStrategy;
                    imSession = this.mCache.makeNewOutgoingSession(imsiFromPhoneId, normalizeUri, chatType2, str, str2, i2, str3, str7, chatMode4, str5, str6, imsUri);
                } else {
                    chatType2 = chatType;
                    iMnoStrategy2 = iMnoStrategy;
                    z6 = z5;
                    imSession.restartSession(i4, str8, str);
                }
                if (iMnoStrategy2.boolSetting(RcsPolicySettings.RcsPolicy.START_SESSION_WHEN_CREATE_GROUPCHAT) && ChatData.ChatType.isGroupChatIdBasedGroupChat(chatType2)) {
                    imSession.startSession();
                }
                for (IChatEventListener onCreateChatSucceeded : this.mChatEventListeners) {
                    onCreateChatSucceeded.onCreateChatSucceeded(imSession);
                }
                ArrayList arrayList2 = new ArrayList();
                arrayList2.add(String.valueOf(chatType2.getId()));
                String str11 = "1";
                arrayList2.add(z6 ? str11 : "0");
                if (!z8) {
                    str11 = "0";
                }
                arrayList2.add(str11);
                ImsUtil.listToDumpFormat(LogClass.IM_CREATE_CHAT, i, imSession.getChatId(), arrayList2);
                return imSession;
            }
            Log.i(str10, "GroupChat is disabled. getGroupChatEnabled=false");
            for (IChatEventListener onCreateChatFailed3 : this.mChatEventListeners) {
                onCreateChatFailed3.onCreateChatFailed(i3, i4, ImErrorReason.FRAMEWORK_ERROR_FALLBACKFAILED, str8);
            }
            return null;
        }
        for (IChatEventListener onCreateChatFailed4 : this.mChatEventListeners) {
            onCreateChatFailed4.onCreateChatFailed(i3, i4, ImErrorReason.INVALID, str8);
        }
        return null;
    }

    /* access modifiers changed from: protected */
    public void addParticipants(String str, List<ImsUri> list) {
        post(new ImSessionProcessor$$ExternalSyntheticLambda12(this, str, list));
    }

    /* access modifiers changed from: private */
    public /* synthetic */ void lambda$addParticipants$4(String str, List list) {
        this.mGcmHandler.addParticipants(str, list);
    }

    /* access modifiers changed from: protected */
    public void removeParticipants(String str, List<ImsUri> list) {
        post(new ImSessionProcessor$$ExternalSyntheticLambda24(this, str, list));
    }

    /* access modifiers changed from: private */
    public /* synthetic */ void lambda$removeParticipants$5(String str, List list) {
        this.mGcmHandler.removeParticipants(str, list);
    }

    /* access modifiers changed from: protected */
    public void changeGroupChatLeader(String str, List<ImsUri> list) {
        post(new ImSessionProcessor$$ExternalSyntheticLambda13(this, str, list));
    }

    /* access modifiers changed from: private */
    public /* synthetic */ void lambda$changeGroupChatLeader$6(String str, List list) {
        this.mGcmHandler.changeGroupChatLeader(str, list);
    }

    /* access modifiers changed from: protected */
    public void changeGroupChatSubject(String str, String str2) {
        post(new ImSessionProcessor$$ExternalSyntheticLambda8(this, str, str2));
    }

    /* access modifiers changed from: private */
    public /* synthetic */ void lambda$changeGroupChatSubject$7(String str, String str2) {
        this.mGcmHandler.changeGroupChatSubject(str, str2);
    }

    /* access modifiers changed from: protected */
    public void changeGroupChatIcon(String str, String str2, Uri uri) {
        post(new ImSessionProcessor$$ExternalSyntheticLambda9(this, str, str2, uri));
    }

    /* access modifiers changed from: private */
    public /* synthetic */ void lambda$changeGroupChatIcon$8(String str, String str2, Uri uri) {
        this.mGcmHandler.changeGroupChatIcon(this.mContext, str, str2, uri);
    }

    /* access modifiers changed from: protected */
    public void changeGroupAlias(String str, String str2) {
        post(new ImSessionProcessor$$ExternalSyntheticLambda23(this, str, str2));
    }

    /* access modifiers changed from: private */
    public /* synthetic */ void lambda$changeGroupAlias$9(String str, String str2) {
        this.mGcmHandler.changeGroupAlias(str, str2);
    }

    /* access modifiers changed from: protected */
    public FutureTask<Boolean> deleteChats(List<String> list, boolean z) {
        FutureTask<Boolean> futureTask = new FutureTask<>(new ImSessionProcessor$$ExternalSyntheticLambda1(this, list, z));
        post(futureTask);
        return futureTask;
    }

    /* access modifiers changed from: private */
    public /* synthetic */ Boolean lambda$deleteChats$10(List list, boolean z) throws Exception {
        Set<ImSession> allImSessionByParticipants;
        String str = LOG_TAG;
        Log.i(str, "deleteChats: " + list);
        this.mCache.deleteMessagesforCloudSyncUsingChatId(list, z);
        ArrayList arrayList = new ArrayList();
        ArrayList<String> arrayList2 = new ArrayList<>();
        Iterator it = list.iterator();
        while (it.hasNext()) {
            String str2 = (String) it.next();
            ImSession imSession = this.mCache.getImSession(str2);
            if (imSession != null && !imSession.isGroupChat() && (allImSessionByParticipants = this.mCache.getAllImSessionByParticipants(imSession.getParticipantsUri(), ChatData.ChatType.ONE_TO_ONE_CHAT)) != null && !allImSessionByParticipants.isEmpty()) {
                for (ImSession chatId : allImSessionByParticipants) {
                    arrayList2.add(chatId.getChatId());
                }
            }
            arrayList2.add(str2);
        }
        for (String str3 : arrayList2) {
            this.mCache.deleteAllMessages(str3);
            ImSession imSession2 = this.mCache.getImSession(str3);
            if (imSession2 != null) {
                int chatStateId = imSession2.getChatStateId();
                int phoneIdByIMSI = this.mImModule.getPhoneIdByIMSI(imSession2.getOwnImsi());
                boolean boolSetting = RcsPolicyManager.getRcsStrategy(phoneIdByIMSI).boolSetting(RcsPolicySettings.RcsPolicy.WAIT_DEACTVAING_DELETE_CHAT);
                String str4 = LOG_TAG;
                Log.i(str4, "deleteChats, stateId=" + chatStateId);
                ImsUtil.listToDumpFormat(LogClass.IM_DELETE_CHAT, phoneIdByIMSI, str3);
                if (RcsPolicyManager.getRcsStrategy(phoneIdByIMSI).isDeleteSessionSupported(imSession2.getChatType(), chatStateId)) {
                    if (imSession2.isGroupChat()) {
                        arrayList.add(imSession2);
                    } else if (!boolSetting) {
                        imSession2.closeSession();
                        this.mCache.deleteSession(imSession2);
                    }
                }
            }
        }
        if (!arrayList.isEmpty()) {
            handleVoluntaryDeparture(arrayList, false);
        }
        return Boolean.TRUE;
    }

    /* access modifiers changed from: protected */
    public FutureTask<Boolean> deleteChatsForUnsubscribe() {
        FutureTask<Boolean> futureTask = new FutureTask<>(new ImSessionProcessor$$ExternalSyntheticLambda3(this));
        post(futureTask);
        return futureTask;
    }

    /* access modifiers changed from: private */
    public /* synthetic */ Boolean lambda$deleteChatsForUnsubscribe$11() throws Exception {
        Log.i(LOG_TAG, "deleteChatsForUnsubscribe");
        this.mCache.loadImSessionByChatType(true);
        for (ImSession next : this.mCache.getAllImSessions()) {
            if (next != null && next.getChatType() == ChatData.ChatType.REGULAR_GROUP_CHAT) {
                this.mCache.deleteAllMessages(next.getChatId());
                this.mCache.deleteSession(next);
                for (IChatEventListener onChatClosed : this.mChatEventListeners) {
                    onChatClosed.onChatClosed(next.getChatId(), next.getDirection(), ImSessionClosedReason.LEFT_BY_SERVER);
                }
            }
        }
        return Boolean.TRUE;
    }

    /* access modifiers changed from: protected */
    public FutureTask<Boolean> deleteAllChats() {
        FutureTask<Boolean> futureTask = new FutureTask<>(new ImSessionProcessor$$ExternalSyntheticLambda19(this));
        post(futureTask);
        return futureTask;
    }

    /* access modifiers changed from: private */
    public /* synthetic */ Boolean lambda$deleteAllChats$12() throws Exception {
        Log.i(LOG_TAG, "deleteAllChats");
        for (ImSession next : this.mCache.getAllImSessions()) {
            IMnoStrategy rcsStrategy = RcsPolicyManager.getRcsStrategy(this.mImModule.getPhoneIdByIMSI(next.getOwnImsi()));
            this.mCache.deleteAllMessages(next.getChatId());
            int chatStateId = next.getChatStateId();
            String str = LOG_TAG;
            Log.i(str, "deleteChats, stateId=" + chatStateId);
            if (rcsStrategy.isDeleteSessionSupported(next.getChatType(), chatStateId)) {
                this.mCache.deleteSession(next);
            }
        }
        return Boolean.TRUE;
    }

    /* access modifiers changed from: protected */
    public void answerGcSession(String str, boolean z) {
        post(new ImSessionProcessor$$ExternalSyntheticLambda11(this, str, z));
    }

    /* access modifiers changed from: private */
    public /* synthetic */ void lambda$answerGcSession$13(String str, boolean z) {
        Log.i(LOG_TAG, String.format("answerSession: %s %b", new Object[]{str, Boolean.valueOf(z)}));
        ImSession imSession = this.mCache.getImSession(str);
        if (imSession != null) {
            if (z) {
                imSession.acceptSession(true);
            } else {
                imSession.rejectSession();
            }
        }
    }

    /* access modifiers changed from: protected */
    public void readMessages(String str, List<String> list) {
        readMessages(str, list, false);
    }

    /* access modifiers changed from: protected */
    public void readMessages(String str, List<String> list, boolean z) {
        this.mImdnHandler.readMessages(str, list, z);
    }

    /* access modifiers changed from: protected */
    public void cancelMessages(String str, List<String> list) {
        this.mImdnHandler.cancelMessages(str, list);
    }

    /* access modifiers changed from: protected */
    public void ignoreIncomingMsgSet(String str, boolean z) {
        post(new ImSessionProcessor$$ExternalSyntheticLambda22(this, str, z));
    }

    /* access modifiers changed from: private */
    public /* synthetic */ void lambda$ignoreIncomingMsgSet$14(String str, boolean z) {
        String str2 = LOG_TAG;
        Log.i(str2, "ignoreIncomingMsgSet: chatId=" + str + " isIgnore=" + z);
        ImSession imSession = this.mCache.getImSession(str);
        if (imSession != null) {
            if (this.mImModule.isRegistered(this.mImModule.getPhoneIdByIMSI(imSession.getOwnImsi()))) {
                imSession.getChatData().updateIsMuted(z);
                this.mImTranslation.onIgnoreIncomingMsgSetResponse(str, true);
                return;
            }
        }
        this.mImTranslation.onIgnoreIncomingMsgSetResponse(str, false);
    }

    public void sendComposingNotification(String str, int i, boolean z) {
        post(new ImSessionProcessor$$ExternalSyntheticLambda14(this, str, i, z));
    }

    /* access modifiers changed from: private */
    public /* synthetic */ void lambda$sendComposingNotification$15(String str, int i, boolean z) {
        this.mImdnHandler.sendComposingNotification(str, i, z);
    }

    /* access modifiers changed from: protected */
    public void acceptChat(String str, boolean z, int i) {
        post(new ImSessionProcessor$$ExternalSyntheticLambda16(this, str, z, i));
    }

    /* access modifiers changed from: private */
    public /* synthetic */ void lambda$acceptChat$16(String str, boolean z, int i) {
        String str2 = LOG_TAG;
        Log.i(str2, "acceptChat: chatId=" + str + "isAccept=" + z + ", reason=" + i);
        ImSession imSession = this.mCache.getImSession(str);
        if (imSession == null) {
            Log.e(str2, "acceptChat: Session not found in the cache");
            return;
        }
        if (this.mImModule.isRegistered(this.mImModule.getPhoneIdByIMSI(imSession.getOwnImsi()))) {
            if (z) {
                imSession.acceptSession(false);
            } else {
                imSession.rejectSession(i);
            }
        }
    }

    public void openChat(String str, boolean z) {
        post(new ImSessionProcessor$$ExternalSyntheticLambda6(this, str, z));
    }

    /* access modifiers changed from: private */
    public /* synthetic */ void lambda$openChat$17(String str, boolean z) {
        String str2 = LOG_TAG;
        Log.i(str2, "openChat: chatId=" + str + ", has Invitation UI=" + z);
        ImSession imSession = this.mCache.getImSession(str);
        if (imSession == null) {
            Log.e(str2, "openChat: Session not found in the cache");
            return;
        }
        int phoneIdByIMSI = this.mImModule.getPhoneIdByIMSI(imSession.getChatData().getOwnIMSI());
        if (this.mImModule.isRegistered(phoneIdByIMSI) && !imSession.isAutoAccept() && this.mImModule.getImConfig(phoneIdByIMSI).getImSessionStart() == ImConstants.ImSessionStart.WHEN_OPENS_CHAT_WINDOW && !z) {
            imSession.acceptSession(false);
        }
    }

    /* access modifiers changed from: protected */
    public void closeChat(String str) {
        ArrayList arrayList = new ArrayList();
        arrayList.add(str);
        post(new ImSessionProcessor$$ExternalSyntheticLambda18(this, arrayList));
    }

    /* access modifiers changed from: private */
    public /* synthetic */ void lambda$closeChat$18(List list) {
        lambda$closeChat$19(list, true, false);
    }

    /* access modifiers changed from: protected */
    public void closeChat(List<String> list, boolean z, boolean z2) {
        post(new ImSessionProcessor$$ExternalSyntheticLambda0(this, list, z, z2));
    }

    /* access modifiers changed from: private */
    /* renamed from: closeChatInternal */
    public void lambda$closeChat$19(List<String> list, boolean z, boolean z2) {
        String str = LOG_TAG;
        Log.i(str, "closeChatInternal: chatId=" + list);
        ArrayList arrayList = new ArrayList();
        ArrayList<ImSession> arrayList2 = new ArrayList<>();
        for (String imSession : list) {
            ImSession imSession2 = this.mCache.getImSession(imSession);
            if (imSession2 == null) {
                Log.e(LOG_TAG, "Session not found in the cache.");
            } else {
                arrayList2.add(imSession2);
            }
        }
        for (ImSession imSession3 : arrayList2) {
            int phoneIdByIMSI = this.mImModule.getPhoneIdByIMSI(imSession3.getOwnImsi());
            ImsUtil.listToDumpFormat(LogClass.IM_CLOSE_CHAT, phoneIdByIMSI, imSession3.getChatId());
            if (z2) {
                if (z && imSession3.isGroupChat() && RcsPolicyManager.getRcsStrategy(phoneIdByIMSI) != null && (RcsPolicyManager.getRcsStrategy(phoneIdByIMSI) instanceof ChnStrategy)) {
                    imSession3.updateChatState(ChatData.State.CLOSED_VOLUNTARILY);
                }
                imSession3.closeSession(true, ImSessionStopReason.GC_FORCE_CLOSE);
            } else if (!z) {
                imSession3.closeSession();
            } else if (imSession3.isGroupChat()) {
                arrayList.add(imSession3);
            } else {
                imSession3.closeSession(true, ImSessionStopReason.VOLUNTARILY);
            }
            this.mCache.removeActiveSession(imSession3);
        }
        if (!arrayList.isEmpty()) {
            handleVoluntaryDeparture(arrayList, true);
        }
    }

    /* access modifiers changed from: protected */
    public void processRejoinGCSession(int i) {
        IMnoStrategy rcsStrategy = RcsPolicyManager.getRcsStrategy(i);
        String imsiFromPhoneId = this.mImModule.getImsiFromPhoneId(i);
        ArrayList<ImSession> arrayList = new ArrayList<>();
        for (ImSession next : this.mCache.getAllImSessions()) {
            if (next.isAutoRejoinSession()) {
                arrayList.add(next);
            }
        }
        int intSetting = rcsStrategy.intSetting(RcsPolicySettings.RcsPolicy.MAX_SIPINVITE_ATONCE);
        String str = LOG_TAG;
        Log.i(str, "rejoinSession: list size : " + arrayList.size() + " limit : " + intSetting);
        if (intSetting > 0) {
            Iterator it = CollectionUtils.partition(arrayList, intSetting).iterator();
            int i2 = 0;
            while (it.hasNext()) {
                postDelayed(new ImSessionProcessor$$ExternalSyntheticLambda20(this, (List) it.next(), i, imsiFromPhoneId), ((long) i2) * 1000);
                i2++;
            }
            return;
        }
        for (ImSession imSession : arrayList) {
            if (this.mImModule.isRegistered(i) && imSession.isGroupChat() && TextUtils.equals(imSession.getChatData().getOwnIMSI(), imsiFromPhoneId)) {
                imSession.processRejoinGCSession();
            }
        }
    }

    /* access modifiers changed from: private */
    public /* synthetic */ void lambda$processRejoinGCSession$20(List list, int i, String str) {
        Iterator it = list.iterator();
        while (it.hasNext()) {
            ImSession imSession = (ImSession) it.next();
            if (this.mImModule.isRegistered(i) && imSession.isGroupChat() && TextUtils.equals(imSession.getChatData().getOwnIMSI(), str)) {
                imSession.processRejoinGCSession();
            }
        }
    }

    /* access modifiers changed from: protected */
    public void onConferenceInfoUpdated(ImSessionConferenceInfoUpdateEvent imSessionConferenceInfoUpdateEvent) {
        String str = LOG_TAG;
        Log.i(str, "onConferenceInfoUpdated: " + imSessionConferenceInfoUpdateEvent);
        ImSession imSession = this.mCache.getImSession(imSessionConferenceInfoUpdateEvent.mChatId);
        if (imSession == null) {
            Log.e(str, "onConferenceInfoUpdated: Session not found.");
        } else if (imSession.getChatType() == ChatData.ChatType.PARTICIPANT_BASED_GROUP_CHAT) {
            Log.i(str, "onConferenceInfoUpdated: ignore the event.");
        } else {
            if (ImsUtil.isMcsSupported(imSession.getPhoneId())) {
                try {
                    if (this.mCache.getConferenceTimestamp(imSessionConferenceInfoUpdateEvent.mChatId) >= Long.parseLong(imSessionConferenceInfoUpdateEvent.mTimeStamp)) {
                        imSessionConferenceInfoUpdateEvent.setRequestByMcs(true);
                    }
                } catch (NumberFormatException unused) {
                    Log.e(LOG_TAG, "NumberFormatException");
                }
            }
            imSession.receiveConferenceInfo(imSessionConferenceInfoUpdateEvent);
        }
    }

    /* access modifiers changed from: protected */
    public void onSessionEstablished(ImSessionEstablishedEvent imSessionEstablishedEvent) {
        String str = LOG_TAG;
        Log.i(str, "onSessionEstablished: " + imSessionEstablishedEvent);
        ImSession imSession = this.mCache.getImSession(imSessionEstablishedEvent.mChatId);
        ArrayList arrayList = new ArrayList();
        if (imSession == null) {
            Log.e(str, "onSessionEstablished: Session not found.");
            return;
        }
        ImDump imDump = this.mImModule.getImDump();
        imDump.addEventLogs("onSessionEstablished: chatId=" + imSession.getChatId() + ", convId=" + imSession.getConversationId() + ", contId=" + imSession.getContributionId());
        arrayList.add(imSession.getConversationId() != null ? imSession.getConversationId() : MessageContextValues.none);
        imSession.receiveSessionEstablished(imSessionEstablishedEvent);
        ImsUtil.listToDumpFormat(LogClass.IM_SESSION_ESTABLISHED, imSession.getPhoneId(), imSessionEstablishedEvent.mChatId, arrayList);
    }

    /* access modifiers changed from: protected */
    public void onSessionClosed(ImSessionClosedEvent imSessionClosedEvent) {
        ImSession imSession;
        String str = LOG_TAG;
        Log.i(str, "onSessionClosed: " + imSessionClosedEvent);
        String str2 = imSessionClosedEvent.mChatId;
        if (str2 == null) {
            imSession = this.mCache.getImSessionByRawHandle(imSessionClosedEvent.mRawHandle);
        } else {
            imSession = this.mCache.getImSession(str2);
        }
        if (imSession == null) {
            Log.e(str, "onSessionClosed: Session not found.");
            return;
        }
        ImDump imDump = this.mImModule.getImDump();
        imDump.addEventLogs("onSessionClosed: chatId=" + imSession.getChatId() + ", convId=" + imSession.getConversationId() + ", contId=" + imSession.getContributionId() + imSessionClosedEvent.mResult.toString());
        ArrayList arrayList = new ArrayList();
        arrayList.add(String.valueOf(imSessionClosedEvent.mResult.getType().ordinal()));
        ImsUtil.listToDumpFormat(LogClass.IM_SESSION_CLOSED, imSession.getPhoneId(), imSessionClosedEvent.mChatId, arrayList);
        imSession.receiveSessionClosed(imSessionClosedEvent);
    }

    /* access modifiers changed from: protected */
    public void onIncomingSessionReceived(ImIncomingSessionEvent imIncomingSessionEvent) {
        new Thread(new ImSessionProcessor$$ExternalSyntheticLambda7(this, imIncomingSessionEvent)).start();
    }

    /* access modifiers changed from: private */
    public /* synthetic */ void lambda$onIncomingSessionReceived$21(ImIncomingSessionEvent imIncomingSessionEvent) {
        ImIncomingSessionEvent imIncomingSessionEvent2 = imIncomingSessionEvent;
        this.mImModule.acquireWakeLock(imIncomingSessionEvent2.mRawHandle);
        if (!TextUtils.isEmpty(imIncomingSessionEvent2.mServiceId)) {
            this.mImModule.releaseWakeLock(imIncomingSessionEvent2.mRawHandle);
            return;
        }
        String str = LOG_TAG;
        Log.i(str, "onIncomingSessionReceived: " + imIncomingSessionEvent2);
        this.mImModule.getImDump().addEventLogs("onIncomingSessionReceived: convId=" + imIncomingSessionEvent2.mConversationId + ", contId=" + imIncomingSessionEvent2.mContributionId);
        int phoneIdByIMSI = this.mImModule.getPhoneIdByIMSI(imIncomingSessionEvent2.mOwnImsi);
        IMnoStrategy rcsStrategy = RcsPolicyManager.getRcsStrategy(phoneIdByIMSI);
        ImsUri imsUri = imIncomingSessionEvent2.mSessionUri;
        Set<ImsUri> normalizedParticipants = getNormalizedParticipants(phoneIdByIMSI, imIncomingSessionEvent2.mRecipients, (imsUri == null || !imsUri.equals(imIncomingSessionEvent2.mInitiator)) ? imIncomingSessionEvent2.mInitiator : null);
        boolean z = true;
        boolean z2 = normalizedParticipants.size() > 1 || imIncomingSessionEvent2.mSessionType == ImIncomingSessionEvent.ImSessionType.CONFERENCE;
        ImSessionRejectReason checkForRejectIncomingSession = checkForRejectIncomingSession(phoneIdByIMSI, z2, imIncomingSessionEvent2.mIsClosedGroupChat);
        if (checkForRejectIncomingSession != null) {
            Log.i(str, "onIncomingSessionReceived: reject");
            this.mImService.rejectImSession(new RejectImSessionParams((String) null, imIncomingSessionEvent2.mRawHandle, checkForRejectIncomingSession, (Message) null));
            this.mImModule.releaseWakeLock(imIncomingSessionEvent2.mRawHandle);
            return;
        }
        if (!imIncomingSessionEvent2.mIsClosedGroupChat || !rcsStrategy.boolSetting(RcsPolicySettings.RcsPolicy.PARTICIPANTBASED_CLOSED_GROUPCHAT)) {
            z = false;
        }
        ChatData.ChatType generateChatType = generateChatType(z2, z, false);
        imIncomingSessionEvent2.mFromBlocked = this.mImModule.isBlockedNumber(phoneIdByIMSI, imIncomingSessionEvent2.mInitiator, z2);
        synchronized (this) {
            ChatData.ChatType chatType = generateChatType;
            boolean z3 = z2;
            ImSession findSession = findSession(phoneIdByIMSI, imIncomingSessionEvent2.mOwnImsi, z2, generateChatType, imIncomingSessionEvent2.mPrevContributionId, imIncomingSessionEvent2.mContributionId, imIncomingSessionEvent2.mConversationId, normalizedParticipants, imIncomingSessionEvent2.mIsTokenUsed ? ChatMode.ON : ChatMode.OFF);
            this.mGcmHandler.updateParticipants(findSession, normalizedParticipants);
            if (findSession == null) {
                if (imIncomingSessionEvent2.mIsForStoredNoti) {
                    Log.i(str, "onIncomingSessionReceived: no session. accept rcse-standfw invite");
                    this.mImService.acceptImSession(new AcceptImSessionParams((String) null, this.mImModule.getUserAlias(phoneIdByIMSI, false), imIncomingSessionEvent2.mRawHandle, true, (Message) null));
                    this.mImModule.releaseWakeLock(imIncomingSessionEvent2.mRawHandle);
                    return;
                }
                Log.i(str, "onIncomingSessionReceived: Make new incoming session.");
                findSession = this.mCache.makeNewIncomingSession(imIncomingSessionEvent2, normalizedParticipants, chatType, imIncomingSessionEvent2.mIsTokenUsed ? ChatMode.ON : ChatMode.OFF);
                if (RcsPolicyManager.getRcsStrategy(phoneIdByIMSI).boolSetting(RcsPolicySettings.RcsPolicy.CHECK_INITIATOR_SESSIONURI) && z3 && this.mImModule.normalizeUri(phoneIdByIMSI, imIncomingSessionEvent2.mInitiator).equals(this.mImModule.normalizeUri(phoneIdByIMSI, imIncomingSessionEvent2.mSessionUri))) {
                    findSession.updateChatState(ChatData.State.CLOSED_BY_USER);
                }
            }
            this.mImModule.getImDump().dumpIncomingSession(phoneIdByIMSI, findSession, imIncomingSessionEvent2.mIsDeferred, imIncomingSessionEvent2.mIsForStoredNoti);
            if (this.mImModule.getImConfig(phoneIdByIMSI).getUserAliasEnabled() && !this.mImModule.getImConfig(phoneIdByIMSI).getRealtimeUserAliasAuth() && !z3) {
                findSession.setInitiatorAlias(imIncomingSessionEvent2.mInitiatorAlias);
            }
            if (z3) {
                findSession.setInitiator(this.mImModule.normalizeUri(phoneIdByIMSI, imIncomingSessionEvent2.mInitiator));
                findSession.updateCreatedBy(this.mImModule.normalizeUri(phoneIdByIMSI, imIncomingSessionEvent2.mCreatedBy));
                findSession.updateInvitedBy(this.mImModule.normalizeUri(phoneIdByIMSI, imIncomingSessionEvent2.mInvitedBy));
            } else if (imIncomingSessionEvent2.mIsChatbotRole) {
                Log.i(str, "onIncomingSessionReceived: event.mIsChatbotRole=true, event.mInitiator=" + IMSLog.numberChecker(imIncomingSessionEvent2.mInitiator));
                ImsUri.removeUriParametersAndHeaders(imIncomingSessionEvent2.mInitiator);
                findSession.setInitiator(imIncomingSessionEvent2.mInitiator);
                int intSetting = RcsPolicyManager.getRcsStrategy(phoneIdByIMSI).intSetting(RcsPolicySettings.RcsPolicy.DELAY_TO_DEREGI_FOR_A2P_SESSION);
                if (intSetting > 0) {
                    processIncomingSessionForA2P(phoneIdByIMSI, intSetting);
                }
            }
            findSession.setIsTokenUsed(imIncomingSessionEvent2.mIsTokenUsed);
            findSession.setDeviceId(imIncomingSessionEvent2.mDeviceId);
            if (!imIncomingSessionEvent2.mIsDeferred) {
                findSession.setNetworkFallbackMech(imIncomingSessionEvent2.mIsMsgFallbackSupported, imIncomingSessionEvent2.mIsMsgRevokeSupported);
                findSession.mRemoteAcceptTypes = imIncomingSessionEvent2.mAcceptTypes;
                findSession.mRemoteAcceptWrappedTypes = imIncomingSessionEvent2.mAcceptWrappedTypes;
            }
            findSession.updateIsChatbotRole(imIncomingSessionEvent2.mIsChatbotRole);
            findSession.processIncomingSession(imIncomingSessionEvent2);
            ImIncomingMessageEvent imIncomingMessageEvent = imIncomingSessionEvent2.mReceivedMessage;
            if (imIncomingMessageEvent != null) {
                this.mImModule.updateServiceAvailability(imIncomingSessionEvent2.mOwnImsi, imIncomingMessageEvent.mSender, imIncomingMessageEvent.mImdnTime);
            }
            this.mImModule.releaseWakeLock(imIncomingSessionEvent2.mRawHandle);
        }
    }

    /* access modifiers changed from: protected */
    public void onIncomingSlmLMMSessionReceived(SlmLMMIncomingSessionEvent slmLMMIncomingSessionEvent) {
        String str = LOG_TAG;
        Log.i(str, "onIncomingSlmLMMSessionReceived: " + slmLMMIncomingSessionEvent);
        int phoneIdByIMSI = this.mImModule.getPhoneIdByIMSI(slmLMMIncomingSessionEvent.mOwnImsi);
        if (this.mImModule.isBlockedNumber(phoneIdByIMSI, slmLMMIncomingSessionEvent.mInitiator, slmLMMIncomingSessionEvent.mIsGroup)) {
            this.mSlmService.rejectSlm(new RejectSlmParams((String) null, slmLMMIncomingSessionEvent.mRawHandle, ImSessionRejectReason.BUSY_HERE, (Message) null, slmLMMIncomingSessionEvent.mOwnImsi));
        } else {
            this.mSlmService.acceptSlm(new AcceptSlmParams((String) null, this.mImModule.getUserAlias(phoneIdByIMSI, false), slmLMMIncomingSessionEvent.mRawHandle, (Message) null, slmLMMIncomingSessionEvent.mOwnImsi));
        }
    }

    /* access modifiers changed from: protected */
    public Set<ImsUri> getNormalizedParticipants(int i, List<ImsUri> list, ImsUri imsUri) {
        Set<ImsUri> set;
        if (list != null) {
            set = this.mImModule.normalizeUri(i, (Collection<ImsUri>) list);
        } else {
            set = new HashSet<>();
        }
        removeOwnNumberFromParticipants(set, this.mImModule.normalizeUri(i, imsUri), i);
        return set;
    }

    /* access modifiers changed from: protected */
    public void removeOwnNumberFromParticipants(Set<ImsUri> set, ImsUri imsUri, int i) {
        String str = LOG_TAG;
        IMSLog.s(str, "removeOwnNumberFromParticipants participants=" + set + " ,sender=" + imsUri);
        if (imsUri != null) {
            set.add(imsUri);
        }
        ImsRegistration imsRegistration = this.mImModule.getImsRegistration(i);
        if (imsRegistration != null) {
            ArrayList arrayList = new ArrayList();
            for (NameAddr uri : imsRegistration.getImpuList()) {
                arrayList.add(this.mImModule.normalizeUri(i, uri.getUri()));
            }
            if (set.size() > 1) {
                set.removeAll(arrayList);
            }
        }
    }

    /* access modifiers changed from: protected */
    public ImSession getImSession(String str) {
        return this.mCache.getImSession(str);
    }

    /* access modifiers changed from: protected */
    public void onMessageSendingSucceeded(MessageBase messageBase) {
        this.mBigDataProcessor.onMessageSendingSucceeded(messageBase);
        ImSession imSession = this.mCache.getImSession(messageBase.getChatId());
        if (imSession == null) {
            Log.e(LOG_TAG, "onMessageSendingSucceeded: session not found.");
            return;
        }
        ImDump imDump = this.mImModule.getImDump();
        imDump.addEventLogs("onMessageSendingSucceeded: type= " + messageBase.getType() + ", chatId=" + imSession.getChatId() + ", convId=" + imSession.getConversationId() + ", contId=" + imSession.getContributionId() + ", imdnId=" + messageBase.getImdnId());
        if (!isReportMsg(messageBase)) {
            for (IMessageEventListener onMessageSendingSucceeded : this.mImProcessor.getMessageEventListener(messageBase.getType())) {
                onMessageSendingSucceeded.onMessageSendingSucceeded(messageBase);
            }
            if (messageBase.isTemporary()) {
                this.mCache.deleteMessage(messageBase.getId());
                return;
            }
            if (messageBase.getRevocationStatus() != ImConstants.RevocationStatus.AVAILABLE) {
                this.mImModule.removeFromPendingListWithDelay(messageBase.getId());
            }
            this.mCache.sentMessageForCloudSync(imSession.getOwnImsi(), messageBase.getId(), messageBase.getImdnId());
        } else if (messageBase.getReportMsgParams() != null) {
            this.mCache.deleteMessage(messageBase.getId());
            this.mImTranslation.onMessageReportResponse(messageBase.getReportMsgParams().getSpamMsgImdnId(), messageBase.getChatId(), true);
        }
    }

    /* access modifiers changed from: protected */
    public void onMessageSendingFailed(MessageBase messageBase, IMnoStrategy.StrategyResponse strategyResponse, Result result) {
        if (messageBase == null) {
            Log.e(LOG_TAG, "onMessageSendingFailed: msg is null.");
        } else if (messageBase.isTemporary()) {
            Log.i(LOG_TAG, "onMessageSendingFailed: temporary message.");
            for (IMessageEventListener onMessageSendingFailed : this.mImProcessor.getMessageEventListener(messageBase.getType())) {
                onMessageSendingFailed.onMessageSendingFailed(messageBase, new IMnoStrategy.StrategyResponse(IMnoStrategy.StatusCode.NONE), (Result) null);
            }
            this.mCache.deleteMessage(messageBase.getId());
        } else {
            ImSession imSession = this.mCache.getImSession(messageBase.getChatId());
            if (result != null && result.getImError() == ImError.ENGINE_ERROR) {
                ImModule imModule = this.mImModule;
                if (imModule.getRegistrationType(imModule.getPhoneIdByIMSI(messageBase.getOwnIMSI())) == null && imSession != null && !imSession.isGroupChat()) {
                    Log.e(LOG_TAG, "onMessageSendingFailed: engine error and deregistered. fallback to legacy.");
                    strategyResponse = new IMnoStrategy.StrategyResponse(IMnoStrategy.StatusCode.FALLBACK_TO_LEGACY);
                }
            }
            if (!(result == null || result.getType() == Result.Type.NONE)) {
                this.mBigDataProcessor.onMessageSendingFailed(messageBase, result, strategyResponse);
            }
            if (imSession == null) {
                Log.e(LOG_TAG, "onMessageSendingFailed: session not found.");
                return;
            }
            this.mImModule.getImDump().dumpMessageSendingFailed(this.mImModule.getPhoneIdByChatId(messageBase.getChatId()), imSession, result, messageBase.mImdnId, strategyResponse.getStatusCode().toString());
            if (!isReportMsg(messageBase) || messageBase.getReportMsgParams() == null) {
                if (ImsGateConfig.isGateEnabled()) {
                    IMSLog.g("GATE", "<GATE-M>MMS_ERROR</GATE-M>");
                }
                ImDump imDump = this.mImModule.getImDump();
                StringBuilder sb = new StringBuilder();
                sb.append("onMessageSendingFailed: type=");
                sb.append(messageBase.getType());
                sb.append("chatId=");
                sb.append(imSession.getChatId());
                sb.append(", convId=");
                sb.append(imSession.getConversationId());
                sb.append(", contId=");
                sb.append(imSession.getContributionId());
                sb.append(", imdnId=");
                sb.append(messageBase.getImdnId());
                sb.append("result=");
                sb.append(result != null ? result.toString() : "");
                sb.append(", required_action=");
                sb.append(strategyResponse.getStatusCode().toString());
                imDump.addEventLogs(sb.toString());
                if (messageBase instanceof ImMessage) {
                    String str = LOG_TAG;
                    Log.e(str, "onMessageSendingFailed ImMessage: id=" + messageBase.getId() + ", strategy=" + strategyResponse + ", result=" + result);
                    for (IMessageEventListener onMessageSendingFailed2 : this.mImProcessor.getMessageEventListener(messageBase.getType())) {
                        onMessageSendingFailed2.onMessageSendingFailed(messageBase, strategyResponse, result);
                    }
                } else if (messageBase instanceof FtMessage) {
                    String str2 = LOG_TAG;
                    Log.e(str2, "onMessageSendingFailed FtMessage: id=" + messageBase.getId() + ", strategy=" + strategyResponse + ", result=" + result);
                    for (IFtEventListener onMessageSendingFailed3 : this.mFtProcessor.getFtEventListener(messageBase.getType())) {
                        onMessageSendingFailed3.onMessageSendingFailed(messageBase, strategyResponse, result);
                    }
                }
                this.mCache.removeFromPendingList(messageBase.getId());
                return;
            }
            this.mCache.deleteMessage(messageBase.getId());
            this.mImTranslation.onMessageReportResponse(messageBase.getReportMsgParams().getSpamMsgImdnId(), messageBase.getChatId(), false);
        }
    }

    /* access modifiers changed from: protected */
    public void notifyImSessionEstablished(int i) {
        if (this.mCache.isEstablishedSessionExist() || this.mCache.hasFileTransferInprogress()) {
            post(new ImSessionProcessor$$ExternalSyntheticLambda10(this, i));
        }
    }

    /* access modifiers changed from: private */
    public /* synthetic */ void lambda$notifyImSessionEstablished$22(int i) {
        Log.i(LOG_TAG, "notifyImSessionEstablished");
        if (this.mImSessionListener.containsKey(Integer.valueOf(i))) {
            Iterator it = this.mImSessionListener.get(Integer.valueOf(i)).iterator();
            while (it.hasNext()) {
                try {
                    ((IImSessionListener) it.next()).onImSessionEstablished(true);
                } catch (RemoteException unused) {
                    Log.e(LOG_TAG, "notifyImSessionEstablished failed to send IImSessionListener.onImSessionEstablished");
                }
            }
        }
    }

    /* access modifiers changed from: protected */
    public void notifyImSessionClosed(int i) {
        if (!this.mCache.isEstablishedSessionExist() && !this.mCache.hasFileTransferInprogress()) {
            post(new ImSessionProcessor$$ExternalSyntheticLambda21(this, i));
        }
    }

    /* access modifiers changed from: private */
    public /* synthetic */ void lambda$notifyImSessionClosed$23(int i) {
        Log.i(LOG_TAG, "notifyImSessionClosed");
        if (this.mImModule.getImsRegistration() != null) {
            ImsRegistry.getRegistrationManager().doPendingUpdateRegistration();
        }
        if (this.mImSessionListener.containsKey(Integer.valueOf(i))) {
            Iterator it = this.mImSessionListener.get(Integer.valueOf(i)).iterator();
            while (it.hasNext()) {
                try {
                    ((IImSessionListener) it.next()).onImSessionEstablished(false);
                } catch (RemoteException unused) {
                    Log.e(LOG_TAG, "notifyImSessionClosed failed to send IImSessionListener.onImSessionEstablished");
                }
            }
        }
    }

    /* access modifiers changed from: protected */
    public boolean hasEstablishedSession() {
        boolean z = this.mImModule.getImsRegistration() != null && (this.mCache.isEstablishedSessionExist() || this.mCache.hasFileTransferInprogress());
        String str = LOG_TAG;
        Log.i(str, "hasEstablishedSession : " + z);
        return z;
    }

    /* access modifiers changed from: protected */
    public void receiveDeliveryTimeout(String str) {
        post(new ImSessionProcessor$$ExternalSyntheticLambda5(this, str));
    }

    /* access modifiers changed from: private */
    public /* synthetic */ void lambda$receiveDeliveryTimeout$24(String str) {
        ImSession imSession = this.mCache.getImSession(str);
        if (imSession == null) {
            Log.i(LOG_TAG, "receiveDeliveryTimeout session not found");
        } else {
            imSession.receiveDeliveryTimeout();
        }
    }

    private void makeNewSystemUserMessage(ImSession imSession, Map<ImParticipant, Date> map, ImConstants.Type type) {
        TreeMap treeMap = new TreeMap();
        HashSet hashSet = new HashSet();
        for (Map.Entry next : map.entrySet()) {
            Date date = (Date) next.getValue();
            String imsUri = ((ImParticipant) next.getKey()).getUri().toString();
            if (date != null) {
                StringBuilder sb = (StringBuilder) treeMap.get(date);
                if (sb == null) {
                    StringBuilder sb2 = new StringBuilder();
                    sb2.append(imsUri);
                    treeMap.put(date, sb2);
                } else {
                    sb.append(";");
                    sb.append(imsUri);
                }
            } else {
                hashSet.add(imsUri);
            }
        }
        for (Map.Entry entry : treeMap.entrySet()) {
            this.mCache.makeNewSystemUserMessage(imSession, ((StringBuilder) entry.getValue()).toString(), type, (Date) entry.getKey());
        }
        if (!hashSet.isEmpty()) {
            this.mCache.makeNewSystemUserMessage(imSession, TextUtils.join(";", hashSet), type);
        }
    }

    private void handleVoluntaryDeparture(List<ImSession> list, boolean z) {
        if (list != null && !list.isEmpty()) {
            for (ImSession next : list) {
                int phoneIdByIMSI = this.mImModule.getPhoneIdByIMSI(next.getOwnImsi());
                if (!next.isEmptySession()) {
                    next.getChatData().updateIsReusable(z);
                    next.updateChatState(ChatData.State.CLOSED_VOLUNTARILY);
                    if (this.mImModule.isOwnNumberChanged(next)) {
                        if (!z) {
                            this.mCache.deleteSession(next);
                        }
                    } else if (!this.mImModule.isRegistered(phoneIdByIMSI)) {
                        for (MessageBase updateDesiredNotificationStatus : this.mCache.getMessagesForPendingNotificationByChatId(next.getChatId())) {
                            updateDesiredNotificationStatus.updateDesiredNotificationStatus(NotificationStatus.NONE);
                        }
                        next.processCancelMessages(false, (ImError) null);
                    } else if (next.isEstablishedState()) {
                        next.closeSession(z, ImSessionStopReason.VOLUNTARILY);
                    } else {
                        this.mGroupChatsForDeparture.add(next);
                    }
                } else if (!z) {
                    this.mCache.deleteSession(next);
                }
            }
        }
        if (!this.mGroupChatsForDeparture.isEmpty() && !hasMessages(1)) {
            handleEventVoluntaryDeparture();
        }
    }

    /* access modifiers changed from: protected */
    public void handleEventVoluntaryDeparture() {
        String str = LOG_TAG;
        Log.i(str, "handleEventVoluntaryDeparture: mGroupChatsForDeparture size=" + this.mGroupChatsForDeparture.size());
        if (!this.mGroupChatsForDeparture.isEmpty()) {
            int intSetting = this.mImModule.getRcsStrategy().intSetting(RcsPolicySettings.RcsPolicy.MAX_SIPINVITE_ATONCE);
            if (intSetting <= 0 || !this.mImModule.isRegistered()) {
                for (ImSession next : this.mGroupChatsForDeparture) {
                    if (this.mImModule.isRegistered()) {
                        next.closeSession(next.isReusable(), ImSessionStopReason.VOLUNTARILY);
                    } else {
                        for (IChatEventListener onChatClosed : this.mChatEventListeners) {
                            onChatClosed.onChatClosed(next.getChatId(), next.getDirection(), ImSessionClosedReason.LEAVE_SESSION_PENDING);
                        }
                    }
                }
                this.mGroupChatsForDeparture.clear();
                return;
            }
            List<ImSession> list = CollectionUtils.partition(this.mGroupChatsForDeparture, intSetting).get(0);
            for (ImSession imSession : list) {
                imSession.closeSession(imSession.isReusable(), ImSessionStopReason.VOLUNTARILY);
            }
            this.mGroupChatsForDeparture.removeAll(list);
            if (!this.mGroupChatsForDeparture.isEmpty()) {
                removeMessages(1);
                sendEmptyMessageDelayed(1, 1000);
            }
        }
    }

    /* access modifiers changed from: protected */
    public void handleEventBlocklistChanged() {
        Log.i(LOG_TAG, "handleEventBlocklistChanged()");
        for (String normalizedUri : BlockedNumberUtil.getBlockedNumbersList(this.mContext)) {
            HashSet hashSet = new HashSet();
            ImsUri normalizedUri2 = this.mImModule.getUriGenerator(SimUtil.getSimSlotPriority()).getNormalizedUri(normalizedUri, true);
            if (normalizedUri2 != null) {
                hashSet.add(normalizedUri2);
                ImSession imSessionByParticipants = this.mCache.getImSessionByParticipants(hashSet, ChatData.ChatType.ONE_TO_ONE_CHAT, "");
                if (imSessionByParticipants != null && imSessionByParticipants.getDetailedState() == ImSession.SessionState.ESTABLISHED) {
                    imSessionByParticipants.closeSession();
                }
            }
        }
    }

    /* access modifiers changed from: protected */
    public void registerImSessionListenerByPhoneId(IImSessionListener iImSessionListener, int i) {
        String str = LOG_TAG;
        Log.i(str, "registerImSessionListener phoneId = " + i);
        if (iImSessionListener != null) {
            if (!this.mImSessionListener.containsKey(Integer.valueOf(i))) {
                this.mImSessionListener.put(Integer.valueOf(i), new ArrayList());
            }
            ArrayList arrayList = this.mImSessionListener.get(Integer.valueOf(i));
            if (arrayList != null) {
                arrayList.add(iImSessionListener);
            }
            notifyImSessionEstablished(i);
            return;
        }
        Log.e(str, "no registerImSessionListener and not work");
    }

    /* access modifiers changed from: protected */
    public void unregisterImSessionListenerByPhoneId(IImSessionListener iImSessionListener, int i) {
        String str = LOG_TAG;
        Log.i(str, "unregisterImSessionListener phoneId = " + i);
        if (this.mImSessionListener.containsKey(Integer.valueOf(i))) {
            this.mImSessionListener.get(Integer.valueOf(i)).remove(iImSessionListener);
        }
    }

    /* access modifiers changed from: protected */
    public boolean isReportMsg(MessageBase messageBase) {
        ImsUri parse = ImsUri.parse(RcsPolicyManager.getRcsStrategy(this.mImModule.getPhoneIdByIMSI(messageBase.getOwnIMSI())).stringSetting(RcsPolicySettings.RcsPolicy.ONEKEY_REPORT_PSI));
        return (parse == null || messageBase.getRemoteUri() == null || !parse.equals(messageBase.getRemoteUri())) ? false : true;
    }

    /* access modifiers changed from: protected */
    public IRcsBigDataProcessor getBigDataProcessor() {
        return this.mBigDataProcessor;
    }

    public void setLegacyLatching(ImsUri imsUri, boolean z, String str) {
        this.mImRevocationHandler.setLegacyLatching(imsUri, z, str);
    }

    public void onMessageRevokeTimerExpired(String str, Collection<String> collection, String str2) {
        this.mImRevocationHandler.onMessageRevokeTimerExpired(str, collection, str2);
    }

    public void onMessageRevocationDone(ImConstants.RevocationStatus revocationStatus, Collection<MessageBase> collection, ImSession imSession) {
        this.mImRevocationHandler.onMessageRevocationDone(revocationStatus, collection, imSession);
    }

    public void addToRevokingMessages(String str, String str2) {
        this.mImRevocationHandler.addToRevokingMessages(str, str2);
    }

    public void removeFromRevokingMessages(Collection<String> collection) {
        this.mImRevocationHandler.removeFromRevokingMessages(collection);
    }

    /* access modifiers changed from: protected */
    public List<IChatEventListener> getChatEventListeners() {
        return this.mChatEventListeners;
    }

    /* access modifiers changed from: protected */
    public ImRevocationHandler getImRevocationHandler() {
        return this.mImRevocationHandler;
    }

    /* access modifiers changed from: protected */
    public Collection<IMessageEventListener> getMessageEventListener(ImConstants.Type type) {
        return this.mImProcessor.getMessageEventListener(type);
    }

    /* access modifiers changed from: protected */
    public Collection<IFtEventListener> getFtEventListener(ImConstants.Type type) {
        return this.mFtProcessor.getFtEventListener(type);
    }

    /* access modifiers changed from: protected */
    public ChatData.ChatType generateChatType(boolean z, boolean z2, boolean z3) {
        if (z3) {
            return ChatData.ChatType.ONE_TO_MANY_CHAT;
        }
        if (!z) {
            return ChatData.ChatType.ONE_TO_ONE_CHAT;
        }
        if (z2) {
            return ChatData.ChatType.PARTICIPANT_BASED_GROUP_CHAT;
        }
        return ChatData.ChatType.REGULAR_GROUP_CHAT;
    }

    private ImSessionRejectReason checkForRejectIncomingSession(int i, boolean z, boolean z2) {
        IMnoStrategy rcsStrategy = RcsPolicyManager.getRcsStrategy(i);
        if (rcsStrategy.boolSetting(RcsPolicySettings.RcsPolicy.CHECK_MSGAPP_IMSESSION_REJECT) && !this.mImModule.isDefaultMessageAppInUse()) {
            Log.e(LOG_TAG, "checkForRejectIncomingSession: default message app is not samsung");
            return ImSessionRejectReason.INVOLUNTARILY;
        } else if (rcsStrategy.checkMainSwitchOff(this.mContext, i)) {
            Log.e(LOG_TAG, "checkForRejectIncomingSession: main Switch Off");
            return z ? ImSessionRejectReason.INVOLUNTARILY : ImSessionRejectReason.TEMPORARILY_UNAVAILABLE;
        } else if (z2 && !RcsPolicyManager.getRcsStrategy(i).boolSetting(RcsPolicySettings.RcsPolicy.PARTICIPANTBASED_CLOSED_GROUPCHAT)) {
            Log.e(LOG_TAG, "checkForRejectIncomingSession: group chat type mismatched");
            return ImSessionRejectReason.VOLUNTARILY;
        } else if (!DeviceUtil.getGcfMode() || !rcsStrategy.boolSetting(RcsPolicySettings.RcsPolicy.AUTH_BASED_SESSION_CONTROL) || this.mImModule.getImConfig(i).getGroupChatEnabled()) {
            return null;
        } else {
            Log.e(LOG_TAG, "GroupChatAuth is disabled");
            return ImSessionRejectReason.NOT_ACCEPTABLE_HERE;
        }
    }

    /* access modifiers changed from: protected */
    public ImSession findSession(int i, String str, boolean z, ChatData.ChatType chatType, String str2, String str3, String str4, Set<ImsUri> set, ChatMode chatMode) {
        ChatData.ChatType chatType2;
        if (!z) {
            return this.mCache.getImSessionByParticipants(set, ChatData.ChatType.ONE_TO_ONE_CHAT, str, chatMode);
        }
        ImSession imSessionByContributionId = !TextUtils.isEmpty(str2) ? this.mCache.getImSessionByContributionId(str, str2, false) : null;
        if (imSessionByContributionId == null) {
            if (this.mImModule.getImConfig(i).getImMsgTech() == ImConstants.ImMsgTech.CPM) {
                imSessionByContributionId = this.mCache.getImSessionByConversationId(str, str4, z);
            } else {
                imSessionByContributionId = this.mCache.getImSessionByContributionId(str, str3, z);
            }
        }
        if (imSessionByContributionId == null && chatType == (chatType2 = ChatData.ChatType.PARTICIPANT_BASED_GROUP_CHAT)) {
            return this.mCache.getImSessionByParticipants(set, chatType2, str, ChatMode.OFF);
        }
        return imSessionByContributionId;
    }

    private void processIncomingSessionForA2P(int i, int i2) {
        String str = LOG_TAG;
        Log.i(str, "processIncomingSessionForA2P: phoneId = " + i + ", delay = " + i2);
        ImsRegistration imsRegistration = this.mImModule.getImsRegistration(i);
        if (imsRegistration != null && imsRegistration.getRegiRat() != 18) {
            removeMessages(2);
            this.mImModule.mHasIncomingSessionForA2P.put(i, Boolean.TRUE);
            sendMessageDelayed(obtainMessage(2, Integer.valueOf(i)), ((long) i2) * 1000);
        }
    }

    public void onSendCanceledNotificationDone(String str, String str2, boolean z) {
        this.mImTranslation.onCancelMessageResponse(str, str2, z);
    }
}
