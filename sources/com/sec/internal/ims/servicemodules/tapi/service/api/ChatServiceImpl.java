package com.sec.internal.ims.servicemodules.tapi.service.api;

import android.content.Context;
import android.content.Intent;
import android.database.ContentObserver;
import android.database.Cursor;
import android.net.Uri;
import android.os.Binder;
import android.os.RemoteCallbackList;
import android.os.RemoteException;
import android.os.UserHandle;
import android.util.Log;
import com.gsma.services.rcs.IRcsServiceRegistrationListener;
import com.gsma.services.rcs.RcsServiceRegistration;
import com.gsma.services.rcs.chat.ChatLog;
import com.gsma.services.rcs.chat.GroupChat;
import com.gsma.services.rcs.chat.IChatMessage;
import com.gsma.services.rcs.chat.IChatService;
import com.gsma.services.rcs.chat.IChatServiceConfiguration;
import com.gsma.services.rcs.chat.IGroupChat;
import com.gsma.services.rcs.chat.IGroupChatListener;
import com.gsma.services.rcs.chat.IOneToOneChat;
import com.gsma.services.rcs.chat.IOneToOneChatListener;
import com.gsma.services.rcs.contact.ContactId;
import com.gsma.services.rcs.groupdelivery.GroupDeliveryInfo;
import com.sec.ims.ImsRegistration;
import com.sec.ims.extensions.ContextExt;
import com.sec.ims.options.Capabilities;
import com.sec.ims.util.ImsUri;
import com.sec.internal.constants.ims.MIMEContentType;
import com.sec.internal.constants.ims.servicemodules.im.ChatData;
import com.sec.internal.constants.ims.servicemodules.im.ImConstants;
import com.sec.internal.constants.ims.servicemodules.im.ImContract;
import com.sec.internal.constants.ims.servicemodules.im.ImDirection;
import com.sec.internal.constants.ims.servicemodules.im.ImIconData;
import com.sec.internal.constants.ims.servicemodules.im.ImParticipant;
import com.sec.internal.constants.ims.servicemodules.im.ImSettings;
import com.sec.internal.constants.ims.servicemodules.im.ImSubjectData;
import com.sec.internal.constants.ims.servicemodules.im.NotificationStatus;
import com.sec.internal.constants.ims.servicemodules.im.reason.ImErrorReason;
import com.sec.internal.constants.ims.servicemodules.im.reason.ImSessionClosedReason;
import com.sec.internal.constants.ims.servicemodules.im.result.Result;
import com.sec.internal.helper.Preconditions;
import com.sec.internal.helper.SimUtil;
import com.sec.internal.helper.os.IntentUtil;
import com.sec.internal.helper.os.TelephonyUtilsWrapper;
import com.sec.internal.ims.registry.ImsRegistry;
import com.sec.internal.ims.servicemodules.csh.event.ICshConstants;
import com.sec.internal.ims.servicemodules.im.ImCache;
import com.sec.internal.ims.servicemodules.im.ImMessage;
import com.sec.internal.ims.servicemodules.im.ImSession;
import com.sec.internal.ims.servicemodules.im.MessageBase;
import com.sec.internal.ims.servicemodules.im.listener.IChatEventListener;
import com.sec.internal.ims.servicemodules.im.listener.IMessageEventListener;
import com.sec.internal.ims.servicemodules.im.strategy.IMnoStrategy;
import com.sec.internal.ims.servicemodules.tapi.service.broadcaster.IRegistrationStatusBroadcaster;
import com.sec.internal.ims.servicemodules.tapi.service.broadcaster.OneToOneChatEventBroadcaster;
import com.sec.internal.ims.util.PhoneUtils;
import com.sec.internal.ims.util.RcsSettingsUtils;
import com.sec.internal.interfaces.ims.core.IRegistrationManager;
import com.sec.internal.interfaces.ims.servicemodules.im.IImModule;
import com.sec.internal.log.IMSLog;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashSet;
import java.util.Hashtable;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.TreeMap;
import java.util.concurrent.ExecutionException;

public class ChatServiceImpl extends IChatService.Stub implements IChatEventListener, IMessageEventListener, IRegistrationStatusBroadcaster {
    private static final String LOG_TAG = ChatServiceImpl.class.getSimpleName();
    public static final String SUBJECT = "chat";
    private static Hashtable<String, IOneToOneChat> mChatSessions = new Hashtable<>();
    private static Hashtable<String, IGroupChat> mGroupChatSessions = new Hashtable<>();
    private Context mContext = null;
    private RemoteCallbackList<IGroupChatListener> mGroupChatListeners = new RemoteCallbackList<>();
    private IImModule mImModule = null;
    private Object mLock = new Object();
    private OneToOneChatEventBroadcaster mOneToOneChatEventBroadcaster = null;
    private RemoteCallbackList<IRcsServiceRegistrationListener> mServiceListeners = new RemoteCallbackList<>();

    public void clearMessageDeliveryExpiration(List<String> list) throws RemoteException {
    }

    public int getServiceVersion() throws ServerApiException {
        return 2;
    }

    public void onAddParticipantsFailed(String str, Collection<ImsUri> collection, ImErrorReason imErrorReason) {
    }

    public void onAddParticipantsSucceeded(String str, Collection<ImsUri> collection) {
    }

    public void onCancelMessageResponse(String str, String str2, boolean z) {
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

    public void onChatEstablished(String str, ImDirection imDirection, ImsUri imsUri, List<String> list, List<String> list2) {
    }

    public void onChatSubjectUpdated(String str, ImSubjectData imSubjectData) {
    }

    public void onCreateChatFailed(int i, int i2, ImErrorReason imErrorReason, String str) {
    }

    public void onCreateChatSucceeded(ImSession imSession) {
    }

    public void onGroupChatIconDeleted(String str) {
    }

    public void onGroupChatIconUpdated(String str, ImIconData imIconData) {
    }

    public void onGroupChatLeaderUpdated(String str, String str2) {
    }

    public void onMessageRevokeTimerExpired(String str, Collection<String> collection) {
    }

    public void onMessageSendResponse(MessageBase messageBase) {
    }

    public void onMessageSendResponseFailed(String str, int i, int i2, String str2) {
    }

    public void onMessageSendResponseTimeout(MessageBase messageBase) {
    }

    public void onParticipantAliasUpdated(String str, ImParticipant imParticipant) {
    }

    public void onRemoveParticipantsFailed(String str, Collection<ImsUri> collection, ImErrorReason imErrorReason) {
    }

    public void onRemoveParticipantsSucceeded(String str, Collection<ImsUri> collection) {
    }

    public ChatServiceImpl(Context context, IImModule iImModule) {
        Preconditions.checkNotNull(context);
        Preconditions.checkNotNull(iImModule);
        this.mOneToOneChatEventBroadcaster = new OneToOneChatEventBroadcaster(context);
        this.mImModule = iImModule;
        this.mContext = context;
        iImModule.registerChatEventListener(this);
        this.mImModule.registerMessageEventListener(ImConstants.Type.TEXT, this);
        this.mImModule.registerMessageEventListener(ImConstants.Type.LOCATION, this);
    }

    public boolean isServiceRegistered() throws ServerApiException {
        IRegistrationManager registrationManager = ImsRegistry.getRegistrationManager();
        if (registrationManager == null) {
            return false;
        }
        for (ImsRegistration hasService : registrationManager.getRegistrationInfo()) {
            if (hasService.hasService("im")) {
                return true;
            }
        }
        return false;
    }

    public void addEventListener(IRcsServiceRegistrationListener iRcsServiceRegistrationListener) throws ServerApiException {
        synchronized (this.mLock) {
            this.mServiceListeners.register(iRcsServiceRegistrationListener);
        }
    }

    public void removeEventListener(IRcsServiceRegistrationListener iRcsServiceRegistrationListener) throws ServerApiException {
        synchronized (this.mLock) {
            this.mServiceListeners.unregister(iRcsServiceRegistrationListener);
        }
    }

    public void notifyRegistrationEvent(boolean z, RcsServiceRegistration.ReasonCode reasonCode) {
        synchronized (this.mLock) {
            int beginBroadcast = this.mServiceListeners.beginBroadcast();
            for (int i = 0; i < beginBroadcast; i++) {
                if (z) {
                    try {
                        this.mServiceListeners.getBroadcastItem(i).onServiceRegistered();
                    } catch (RemoteException e) {
                        e.printStackTrace();
                    }
                } else {
                    this.mServiceListeners.getBroadcastItem(i).onServiceUnregistered(reasonCode);
                }
            }
            this.mServiceListeners.finishBroadcast();
        }
    }

    public void notifyGroupChatStateChanged(String str, GroupChat.State state, GroupChat.ReasonCode reasonCode) {
        String str2;
        String str3 = LOG_TAG;
        StringBuilder sb = new StringBuilder();
        sb.append("notifyGroupChateStateChanged  chatId = ");
        sb.append(str);
        sb.append(", state = ");
        sb.append(state.name());
        sb.append(",  reasonCode = ");
        if (reasonCode == null) {
            str2 = "";
        } else {
            str2 = reasonCode.name();
        }
        sb.append(str2);
        Log.i(str3, sb.toString());
        synchronized (this.mLock) {
            if (mGroupChatSessions.get(str) == null) {
                Log.i(str3, "notifyMessageGroupDeliveryInfoChanged: Not group chat, drop out");
                return;
            }
            int beginBroadcast = this.mGroupChatListeners.beginBroadcast();
            for (int i = 0; i < beginBroadcast; i++) {
                if (reasonCode != null) {
                    try {
                        this.mGroupChatListeners.getBroadcastItem(i).onStateChanged(str, state.ordinal(), reasonCode.ordinal());
                    } catch (RemoteException e) {
                        e.printStackTrace();
                    }
                }
            }
            this.mGroupChatListeners.finishBroadcast();
        }
    }

    public void notifyGroupMessageStateChanged(MessageBase messageBase, ChatLog.Message.Content.Status status, ChatLog.Message.Content.ReasonCode reasonCode) {
        String str = LOG_TAG;
        Log.i(str, "notifyGroupMessageStateChanged");
        synchronized (this.mLock) {
            String valueOf = String.valueOf(messageBase.getId());
            String chatId = messageBase.getChatId();
            String contentType = messageBase.getContentType();
            ImSession imSession = this.mImModule.getImSession(chatId);
            if (imSession == null || imSession.isGroupChat()) {
                int beginBroadcast = this.mGroupChatListeners.beginBroadcast();
                for (int i = 0; i < beginBroadcast; i++) {
                    try {
                        this.mGroupChatListeners.getBroadcastItem(i).onMessageStatusChanged(chatId, contentType, valueOf, status.ordinal(), reasonCode.ordinal());
                    } catch (RemoteException e) {
                        e.printStackTrace();
                    }
                }
                this.mGroupChatListeners.finishBroadcast();
                return;
            }
            Log.i(str, "notifyMessageGroupDeliveryInfoChanged: Not group chat, drop out");
        }
    }

    public void notifyMessageGroupDeliveryInfoChanged(ImMessage imMessage, ImsUri imsUri, GroupDeliveryInfo.Status status, GroupDeliveryInfo.ReasonCode reasonCode) {
        String str = LOG_TAG;
        Log.i(str, "notifyGroupDeliveryInfoChanged");
        synchronized (this.mLock) {
            String chatId = imMessage.getChatId();
            if (this.mImModule.getImSession(chatId) == null) {
                Log.i(str, "notifyMessageGroupDeliveryInfoChanged: Session is null, drop out");
                return;
            }
            String valueOf = String.valueOf(imMessage.getId());
            String contentType = imMessage.getContentType();
            if (imsUri != null) {
                ContactId contactId = new ContactId(PhoneUtils.extractNumberFromUri(imsUri.toString()));
                int beginBroadcast = this.mGroupChatListeners.beginBroadcast();
                for (int i = 0; i < beginBroadcast; i++) {
                    try {
                        this.mGroupChatListeners.getBroadcastItem(i).onMessageGroupDeliveryInfoChanged(chatId, contactId, contentType, valueOf, status.ordinal(), reasonCode.ordinal());
                    } catch (RemoteException e) {
                        e.printStackTrace();
                    }
                }
                this.mGroupChatListeners.finishBroadcast();
            }
        }
    }

    public void notifyGroupParticipantInfoChanged(ImParticipant imParticipant) {
        Log.i(LOG_TAG, "notifyGroupParticipantInfoChanged");
        synchronized (this.mLock) {
            ImParticipant.Status status = imParticipant.getStatus();
            ContactId contactId = new ContactId(PhoneUtils.extractNumberFromUri(imParticipant.getUri().toString()));
            GroupChat.ParticipantStatus convertParticipantStatus = convertParticipantStatus(status);
            int beginBroadcast = this.mGroupChatListeners.beginBroadcast();
            for (int i = 0; i < beginBroadcast; i++) {
                try {
                    this.mGroupChatListeners.getBroadcastItem(i).onParticipantStatusChanged(imParticipant.getChatId(), contactId, convertParticipantStatus);
                } catch (RemoteException e) {
                    e.printStackTrace();
                }
            }
            this.mGroupChatListeners.finishBroadcast();
        }
    }

    public void notifyGroupChatDeleted(List<String> list) {
        Log.i(LOG_TAG, "notifyGroupChatDeleted");
        synchronized (this.mLock) {
            int beginBroadcast = this.mGroupChatListeners.beginBroadcast();
            for (int i = 0; i < beginBroadcast; i++) {
                try {
                    this.mGroupChatListeners.getBroadcastItem(i).onDeleted(list);
                } catch (RemoteException e) {
                    e.printStackTrace();
                }
            }
            this.mGroupChatListeners.finishBroadcast();
        }
    }

    private void notifyMessageStateChanged(ContactId contactId, MessageBase messageBase, ChatLog.Message.Content.Status status, ChatLog.Message.Content.ReasonCode reasonCode) {
        ImSession imSession = this.mImModule.getImSession(messageBase.getChatId());
        if (!(imSession != null && imSession.isGroupChat())) {
            this.mOneToOneChatEventBroadcaster.broadcastMessageStatusChanged(contactId, messageBase.getContentType(), String.valueOf(messageBase.getId()), status, reasonCode);
        } else {
            notifyGroupMessageStateChanged(messageBase, status, reasonCode);
        }
    }

    private GroupChat.ParticipantStatus convertParticipantStatus(ImParticipant.Status status) {
        GroupChat.ParticipantStatus participantStatus = GroupChat.ParticipantStatus.DISCONNECTED;
        switch (AnonymousClass1.$SwitchMap$com$sec$internal$constants$ims$servicemodules$im$ImParticipant$Status[status.ordinal()]) {
            case 1:
                return GroupChat.ParticipantStatus.CONNECTED;
            case 2:
                return GroupChat.ParticipantStatus.INVITED;
            case 3:
            case 4:
                return GroupChat.ParticipantStatus.CONNECTED;
            case 5:
                return GroupChat.ParticipantStatus.DECLINED;
            case 6:
                return GroupChat.ParticipantStatus.DEPARTED;
            case 7:
                return GroupChat.ParticipantStatus.TIMEOUT;
            case 8:
                return GroupChat.ParticipantStatus.INVITING;
            default:
                return GroupChat.ParticipantStatus.DISCONNECTED;
        }
    }

    public IOneToOneChat getOneToOneChat(ContactId contactId) throws ServerApiException {
        List<String> participantsString;
        try {
            String str = LOG_TAG;
            Log.d(str, "start : openSingleChat()");
            String extractNumberFromUri = PhoneUtils.extractNumberFromUri(contactId.toString());
            ImCache instance = ImCache.getInstance();
            ChatImpl chatSession = getChatSession(extractNumberFromUri);
            ImSession imSession = null;
            if (chatSession == null) {
                Iterator<ImSession> it = instance.getAllImSessions().iterator();
                while (true) {
                    if (!it.hasNext()) {
                        break;
                    }
                    ImSession next = it.next();
                    if (!next.isGroupChat() && (participantsString = next.getParticipantsString()) != null) {
                        if (participantsString.size() != 0) {
                            if (PhoneUtils.extractNumberFromUri(participantsString.get(0)).equals(extractNumberFromUri)) {
                                imSession = next;
                                break;
                            }
                        }
                    }
                }
            } else {
                ImSession coreSession = chatSession.getCoreSession();
                if (coreSession != null) {
                    Log.d(str, "Core chat session already exist: " + coreSession.getChatId());
                    if (instance.getImSession(coreSession.getChatId()) != null) {
                        return chatSession;
                    }
                    removeChatSession(extractNumberFromUri);
                } else {
                    removeChatSession(extractNumberFromUri);
                }
            }
            if (imSession == null) {
                String str2 = LOG_TAG;
                Log.d(str2, "Create a new chat session with " + IMSLog.checker(extractNumberFromUri));
                ArrayList arrayList = new ArrayList();
                arrayList.add(ImsUri.parse("tel:" + contactId.toString()));
                try {
                    imSession = this.mImModule.createChat(arrayList, SUBJECT, MIMEContentType.PLAIN_TEXT, -1, (String) null).get();
                } catch (InterruptedException e) {
                    e.printStackTrace();
                } catch (ExecutionException e2) {
                    e2.printStackTrace();
                }
            }
            if (imSession != null) {
                ChatImpl chatImpl = new ChatImpl(extractNumberFromUri, imSession, this.mImModule);
                addChatSession(extractNumberFromUri, chatImpl);
                return chatImpl;
            }
            Log.e(LOG_TAG, "getOneToOneChat: session is error...");
            throw new ServerApiException("session is error...");
        } catch (RemoteException e3) {
            throw new ServerApiException(e3.getMessage());
        }
    }

    public static void addChatSession(String str, ChatImpl chatImpl) {
        mChatSessions.put(PhoneUtils.extractNumberFromUri(str), chatImpl);
    }

    protected static IOneToOneChat getChatSession(String str) {
        return mChatSessions.get(PhoneUtils.extractNumberFromUri(str));
    }

    protected static void removeChatSession(String str) {
        String extractNumberFromUri = PhoneUtils.extractNumberFromUri(str);
        Hashtable<String, IOneToOneChat> hashtable = mChatSessions;
        if (hashtable != null && str != null) {
            hashtable.remove(extractNumberFromUri);
        }
    }

    public void onChatUpdateState(String str, ImDirection imDirection, ImSession.SessionState sessionState) {
        ImSessionClosedReason imSessionClosedReason = ImSessionClosedReason.NONE;
        GroupChat.State translateState = translateState(sessionState, imDirection, imSessionClosedReason);
        if (translateState != null) {
            notifyGroupChatStateChanged(str, translateState, translateReasonCode(imSessionClosedReason));
        }
    }

    public void onChatClosed(String str, ImDirection imDirection, ImSessionClosedReason imSessionClosedReason) {
        GroupChat.State translateState = translateState(ImSession.SessionState.CLOSED, imDirection, imSessionClosedReason);
        if (translateState != null) {
            notifyGroupChatStateChanged(str, translateState, translateReasonCode(imSessionClosedReason));
        }
    }

    public void onChatInvitationReceived(ImSession imSession) {
        String str = LOG_TAG;
        Log.d(str, "start : onChatInvitationReceived()");
        if (imSession.isGroupChat()) {
            addGroupChatSession(new GroupChatImpl(imSession));
            Intent intent = new Intent("com.gsma.services.rcs.chat.action.NEW_GROUP_CHAT");
            intent.putExtra("chatId", imSession.getChatId());
            UserHandle subscriptionUserHandle = TelephonyUtilsWrapper.getSubscriptionUserHandle(this.mContext, SimUtil.getSubId(this.mImModule.getPhoneIdByIMSI(imSession.getOwnImsi())));
            if (subscriptionUserHandle != null) {
                IntentUtil.sendBroadcast(this.mContext, intent, subscriptionUserHandle, "com.gsma.services.permission.RCS");
            } else {
                IntentUtil.sendBroadcast(this.mContext, intent, ContextExt.CURRENT_OR_SELF, "com.gsma.services.permission.RCS");
            }
        }
        if (imSession.getParticipantsString().size() != 1) {
            Log.d(str, "session.getParticipantsString().size() != 1");
            return;
        }
        String extractNumberFromUri = PhoneUtils.extractNumberFromUri(imSession.getParticipantsString().get(0));
        if (!mChatSessions.containsKey(extractNumberFromUri)) {
            addChatSession(extractNumberFromUri, new ChatImpl(extractNumberFromUri, imSession, this.mImModule));
        }
    }

    public void receiveGroupChatMessage(MessageBase messageBase) {
        Log.d(LOG_TAG, "start : receiveGroupChatMessage()");
        Intent intent = new Intent("com.gsma.services.rcs.chat.action.NEW_GROUP_CHAT_MESSAGE");
        intent.putExtra("messageId", Integer.toString(messageBase.getId()));
        intent.putExtra("mimeType", messageBase.getContentType());
        UserHandle subscriptionUserHandle = TelephonyUtilsWrapper.getSubscriptionUserHandle(this.mContext, SimUtil.getSubId(this.mImModule.getPhoneIdByMessageId(messageBase.getId())));
        if (subscriptionUserHandle != null) {
            IntentUtil.sendBroadcast(this.mContext, intent, subscriptionUserHandle, "com.gsma.services.permission.RCS");
        } else {
            IntentUtil.sendBroadcast(this.mContext, intent, ContextExt.CURRENT_OR_SELF, "com.gsma.services.permission.RCS");
        }
    }

    protected static void addGroupChatSession(GroupChatImpl groupChatImpl) {
        mGroupChatSessions.put(groupChatImpl.getChatId(), groupChatImpl);
    }

    protected static void removeGroupChatSession(String str) {
        mGroupChatSessions.remove(str);
    }

    /* JADX INFO: finally extract failed */
    public boolean isAllowedToInitiateGroupChat() throws RemoteException {
        long clearCallingIdentity = Binder.clearCallingIdentity();
        try {
            Capabilities ownCapabilities = ImsRegistry.getServiceModuleManager().getCapabilityDiscoveryModule().getOwnCapabilities();
            if (ownCapabilities == null || !ownCapabilities.hasFeature(Capabilities.FEATURE_SF_GROUP_CHAT)) {
                Binder.restoreCallingIdentity(clearCallingIdentity);
                return false;
            }
            Binder.restoreCallingIdentity(clearCallingIdentity);
            return true;
        } catch (Throwable th) {
            Binder.restoreCallingIdentity(clearCallingIdentity);
            throw th;
        }
    }

    public boolean canInitiateGroupChat(ContactId contactId) throws RemoteException {
        Capabilities capabilities;
        if (contactId == null || (capabilities = ImsRegistry.getServiceModuleManager().getCapabilityDiscoveryModule().getCapabilities(contactId.toString(), (long) Capabilities.FEATURE_SF_GROUP_CHAT, 0)) == null || !capabilities.hasFeature(Capabilities.FEATURE_SF_GROUP_CHAT)) {
            return false;
        }
        return true;
    }

    public IGroupChat initiateGroupChat(List<ContactId> list, String str) throws ServerApiException {
        Log.d(LOG_TAG, "start : initiateGroupChat()");
        ArrayList arrayList = new ArrayList();
        for (ContactId contactId : list) {
            arrayList.add(ImsUri.parse("tel:" + contactId.toString()));
        }
        try {
            ImSession imSession = this.mImModule.createChat(arrayList, str, MIMEContentType.PLAIN_TEXT, -1, (String) null).get();
            if (imSession != null) {
                GroupChatImpl groupChatImpl = new GroupChatImpl(imSession.getChatId());
                addGroupChatSession(groupChatImpl);
                return groupChatImpl;
            }
            Log.e(LOG_TAG, "initiateGroupChat: session is error...");
            throw new ServerApiException("session is error...");
        } catch (InterruptedException e) {
            e.printStackTrace();
            return null;
        } catch (ExecutionException e2) {
            e2.printStackTrace();
            return null;
        }
    }

    public IGroupChat getGroupChat(String str) throws ServerApiException {
        return mGroupChatSessions.get(str);
    }

    public void addOneToOneChatEventListener(IOneToOneChatListener iOneToOneChatListener) throws RemoteException {
        synchronized (this.mLock) {
            this.mOneToOneChatEventBroadcaster.addOneToOneChatEventListener(iOneToOneChatListener);
        }
    }

    public void removeOneToOneChatEventListener(IOneToOneChatListener iOneToOneChatListener) throws RemoteException {
        synchronized (this.mLock) {
            this.mOneToOneChatEventBroadcaster.removeOneToOneChatEventListener(iOneToOneChatListener);
        }
    }

    public void addGroupChatEventListener(IGroupChatListener iGroupChatListener) throws ServerApiException {
        synchronized (this.mLock) {
            this.mGroupChatListeners.register(iGroupChatListener);
        }
    }

    public void removeGroupChatEventListener(IGroupChatListener iGroupChatListener) throws ServerApiException {
        synchronized (this.mLock) {
            this.mGroupChatListeners.unregister(iGroupChatListener);
        }
    }

    public IChatServiceConfiguration getConfiguration() throws ServerApiException {
        return new ChatServiceConfigurationImpl(this.mImModule.getImConfig());
    }

    public void markMessageAsRead(String str) throws ServerApiException {
        Log.d(LOG_TAG, "start : markMessageAsRead()");
        ImMessage imMessage = ImCache.getInstance().getImMessage(Integer.valueOf(str).intValue());
        if (imMessage != null) {
            ArrayList arrayList = new ArrayList();
            arrayList.add(str);
            this.mImModule.readMessages(imMessage.getChatId(), arrayList);
            imMessage.updateNotificationStatus(NotificationStatus.DELIVERED);
            ContactId contactId = new ContactId(PhoneUtils.extractNumberFromUri(getRemoteUserByChatId(imMessage.getChatId())));
            synchronized (this.mLock) {
                notifyMessageStateChanged(contactId, imMessage, ChatLog.Message.Content.Status.DISPLAYED, ChatLog.Message.Content.ReasonCode.UNSPECIFIED);
            }
        }
    }

    public void notifyChangeForDelete() {
        this.mContext.getContentResolver().notifyChange(ChatLog.Message.CONTENT_URI, (ContentObserver) null);
    }

    public void deleteOneToOneChats() throws RemoteException {
        Log.d(LOG_TAG, "start : deleteOneToOneChats()");
        Map<String, Set<String>> messages = getMessages(false, "is_filetransfer != 1");
        if (messages != null) {
            ArrayList arrayList = new ArrayList();
            for (Map.Entry next : messages.entrySet()) {
                arrayList.add((String) next.getKey());
                String remoteUserByChatId = getRemoteUserByChatId((String) next.getKey());
                synchronized (this.mLock) {
                    this.mOneToOneChatEventBroadcaster.broadcastMessageDeleted(PhoneUtils.extractNumberFromUri(remoteUserByChatId), (Set) next.getValue());
                }
            }
            this.mImModule.deleteChats(arrayList, false);
            mChatSessions.clear();
            notifyChangeForDelete();
        }
    }

    public void deleteGroupChats() throws RemoteException {
        Log.d(LOG_TAG, "start : delete All GroupChats()");
        mGroupChatSessions.clear();
        ArrayList arrayList = new ArrayList();
        for (ImSession next : ImCache.getInstance().getAllImSessions()) {
            if (next.isGroupChat()) {
                arrayList.add(next.getChatId());
            }
        }
        this.mImModule.deleteChats(arrayList, false);
        notifyGroupChatDeleted(arrayList);
        notifyChangeForDelete();
    }

    public void deleteOneToOneChat(ContactId contactId) throws RemoteException {
        String str = LOG_TAG;
        Log.d(str, "start : deleteOneToOneChat()");
        HashSet hashSet = new HashSet();
        hashSet.add(ImsUri.parse("tel:" + PhoneUtils.extractNumberFromUri(contactId.toString())));
        ImSession imSessionByParticipants = ImCache.getInstance().getImSessionByParticipants(hashSet, ChatData.ChatType.ONE_TO_ONE_CHAT, "");
        if (imSessionByParticipants == null) {
            Log.e(str, "there is no session for ft");
            return;
        }
        Map<String, Set<String>> messages = getMessages(false, "is_filetransfer != 1 and chat_id = '" + imSessionByParticipants.getChatId() + "'");
        if (messages != null) {
            ArrayList arrayList = new ArrayList();
            for (Map.Entry next : messages.entrySet()) {
                arrayList.add((String) next.getKey());
                String remoteUserByChatId = getRemoteUserByChatId((String) next.getKey());
                synchronized (this.mLock) {
                    this.mOneToOneChatEventBroadcaster.broadcastMessageDeleted(PhoneUtils.extractNumberFromUri(remoteUserByChatId), (Set) next.getValue());
                }
            }
            this.mImModule.deleteChats(arrayList, false);
            removeChatSession(PhoneUtils.extractNumberFromUri(contactId.toString()));
            notifyChangeForDelete();
        }
    }

    public void deleteGroupChat(String str) throws RemoteException {
        Log.d(LOG_TAG, "start : deleteGroupChat()");
        mGroupChatSessions.remove(str);
        ArrayList arrayList = new ArrayList();
        arrayList.add(str);
        this.mImModule.deleteChats(arrayList, false);
        notifyGroupChatDeleted(arrayList);
        notifyChangeForDelete();
    }

    public void deleteMessage(String str) throws RemoteException {
        String str2 = LOG_TAG;
        IMSLog.s(str2, "start : deleteMessage() msgId:" + str);
        Cursor query = this.mContext.getContentResolver().query(Uri.withAppendedPath(ChatLog.Message.CONTENT_URI, str), (String[]) null, (String) null, (String[]) null, (String) null);
        if (query != null) {
            try {
                if (query.getCount() != 0) {
                    query.moveToFirst();
                    String string = query.getString(query.getColumnIndex("chat_id"));
                    String string2 = query.getString(query.getColumnIndex(ICshConstants.ShareDatabase.KEY_TARGET_CONTACT));
                    query.close();
                    ImSession imSession = this.mImModule.getImSession(string);
                    boolean z = imSession != null && imSession.isGroupChat();
                    HashSet hashSet = new HashSet();
                    hashSet.add(str);
                    ArrayList arrayList = new ArrayList();
                    arrayList.add(str);
                    this.mImModule.deleteMessages(arrayList, false);
                    if (!z) {
                        synchronized (this.mLock) {
                            this.mOneToOneChatEventBroadcaster.broadcastMessageDeleted(new ContactId(PhoneUtils.extractNumberFromUri(string2)).toString(), hashSet);
                        }
                    } else {
                        ArrayList arrayList2 = new ArrayList(hashSet);
                        synchronized (this.mLock) {
                            int beginBroadcast = this.mGroupChatListeners.beginBroadcast();
                            for (int i = 0; i < beginBroadcast; i++) {
                                try {
                                    this.mGroupChatListeners.getBroadcastItem(i).onMessagesDeleted(string, arrayList2);
                                } catch (RemoteException e) {
                                    e.printStackTrace();
                                }
                            }
                            this.mGroupChatListeners.finishBroadcast();
                        }
                    }
                    notifyChangeForDelete();
                    return;
                }
            } catch (Throwable th) {
                th.addSuppressed(th);
            }
        }
        if (query != null) {
            query.close();
            return;
        }
        return;
        throw th;
    }

    public IChatMessage getChatMessage(String str) throws RemoteException {
        return new ChatMessageImpl(str);
    }

    public void setRespondToDisplayReports(boolean z) throws RemoteException {
        String str = LOG_TAG;
        IMSLog.s(str, "start : setRespondToDisplayReports() enable:" + z);
        RcsSettingsUtils instance = RcsSettingsUtils.getInstance();
        if (instance != null) {
            instance.writeBoolean(ImSettings.CHAT_RESPOND_TO_DISPLAY_REPORTS, z);
        }
    }

    public List<String> getUndeliveredMessages(ContactId contactId) throws RemoteException {
        Log.d(LOG_TAG, "start : getUndeliveredMessages()");
        ImsUri parse = ImsUri.parse("tel:" + contactId.toString());
        HashSet hashSet = new HashSet();
        hashSet.add(parse);
        ImSession imSessionByParticipants = ImCache.getInstance().getImSessionByParticipants(hashSet, ChatData.ChatType.ONE_TO_ONE_CHAT, "");
        ArrayList arrayList = new ArrayList();
        if (imSessionByParticipants == null) {
            return arrayList;
        }
        Cursor queryMessages = ImCache.getInstance().queryMessages(new String[]{"_id"}, "chat_id = '" + imSessionByParticipants.getChatId() + "' and " + "notification_status" + " = " + NotificationStatus.NONE.getId() + " and " + "direction" + " = " + ImDirection.OUTGOING.getId() + " and " + ImContract.ChatItem.IS_FILE_TRANSFER + " = 0", (String[]) null, (String) null);
        if (queryMessages != null) {
            while (queryMessages.moveToNext()) {
                try {
                    arrayList.add(String.valueOf(queryMessages.getInt(0)));
                } catch (Throwable th) {
                    th.addSuppressed(th);
                }
            }
        }
        if (queryMessages != null) {
            queryMessages.close();
        }
        return arrayList;
        throw th;
    }

    public void markUndeliveredMessagesAsProcessed(List<String> list) throws RemoteException {
        Log.d(LOG_TAG, "start : markUndeliveredMessagesAsProcessed()");
        ImCache instance = ImCache.getInstance();
        for (String next : list) {
            ImMessage imMessage = instance.getImMessage(Integer.valueOf(next).intValue());
            if (imMessage != null) {
                imMessage.updateStatus(ImConstants.Status.SENT);
                instance.removeFromPendingList(Integer.valueOf(next).intValue());
            }
        }
    }

    public Map<String, Set<String>> getMessages(boolean z, String str) {
        String str2 = LOG_TAG;
        Log.d(str2, "start : deleteFileTransfers()");
        ImCache instance = ImCache.getInstance();
        TreeMap treeMap = new TreeMap();
        Cursor queryMessages = instance.queryMessages(new String[]{"_id", "chat_id"}, str, (String[]) null, (String) null);
        if (queryMessages != null) {
            try {
                if (queryMessages.getCount() != 0) {
                    while (queryMessages.moveToNext()) {
                        String string = queryMessages.getString(queryMessages.getColumnIndexOrThrow("chat_id"));
                        ImSession imSession = instance.getImSession(string);
                        if (imSession != null) {
                            if (imSession.isGroupChat() == z) {
                                addRecord(string, String.valueOf(queryMessages.getInt(queryMessages.getColumnIndexOrThrow("_id"))), treeMap);
                            }
                        }
                    }
                    queryMessages.close();
                    return treeMap;
                }
            } catch (Throwable th) {
                th.addSuppressed(th);
            }
        }
        Log.e(str2, "deleteOneToOneFileTransfers: Message not found.");
        if (queryMessages != null) {
            queryMessages.close();
        }
        return null;
        throw th;
    }

    private void addRecord(String str, String str2, Map<String, Set<String>> map) {
        Set set = map.get(str);
        if (set == null) {
            HashSet hashSet = new HashSet();
            hashSet.add(str2);
            map.put(str, hashSet);
            return;
        }
        set.add(str2);
    }

    /* JADX WARNING: Code restructure failed: missing block: B:21:?, code lost:
        return;
     */
    /* Code decompiled incorrectly, please refer to instructions dump. */
    public void handleReceiveMessage(com.sec.internal.ims.servicemodules.im.MessageBase r9, boolean r10) {
        /*
            r8 = this;
            com.sec.internal.interfaces.ims.servicemodules.im.IImModule r0 = r8.mImModule
            java.lang.String r1 = r9.getChatId()
            com.sec.internal.ims.servicemodules.im.ImSession r0 = r0.getImSession(r1)
            if (r0 == 0) goto L_0x0072
            boolean r0 = r0.isGroupChat()
            java.lang.Object r1 = r8.mLock
            monitor-enter(r1)
            if (r0 == 0) goto L_0x001b
            r8.receiveGroupChatMessage(r9)     // Catch:{ all -> 0x0019 }
            goto L_0x006e
        L_0x0019:
            r8 = move-exception
            goto L_0x0070
        L_0x001b:
            if (r10 == 0) goto L_0x001f
            monitor-exit(r1)     // Catch:{ all -> 0x0019 }
            return
        L_0x001f:
            com.sec.ims.util.ImsUri r10 = r9.getRemoteUri()     // Catch:{ all -> 0x0019 }
            java.lang.String r10 = r10.toString()     // Catch:{ all -> 0x0019 }
            com.gsma.services.rcs.contact.ContactId r3 = new com.gsma.services.rcs.contact.ContactId     // Catch:{ all -> 0x0019 }
            java.lang.String r0 = com.sec.internal.ims.util.PhoneUtils.extractNumberFromUri(r10)     // Catch:{ all -> 0x0019 }
            r3.<init>(r0)     // Catch:{ all -> 0x0019 }
            com.sec.internal.interfaces.ims.servicemodules.im.IImModule r0 = r8.mImModule     // Catch:{ all -> 0x0019 }
            int r2 = r9.getId()     // Catch:{ all -> 0x0019 }
            int r0 = r0.getPhoneIdByMessageId(r2)     // Catch:{ all -> 0x0019 }
            android.content.Context r2 = r8.mContext     // Catch:{ all -> 0x0019 }
            int r0 = com.sec.internal.helper.SimUtil.getSubId(r0)     // Catch:{ all -> 0x0019 }
            android.os.UserHandle r0 = com.sec.internal.helper.os.TelephonyUtilsWrapper.getSubscriptionUserHandle(r2, r0)     // Catch:{ all -> 0x0019 }
            if (r0 != 0) goto L_0x0048
            android.os.UserHandle r0 = com.sec.ims.extensions.ContextExt.CURRENT_OR_SELF     // Catch:{ all -> 0x0019 }
        L_0x0048:
            com.sec.internal.ims.servicemodules.tapi.service.broadcaster.OneToOneChatEventBroadcaster r2 = r8.mOneToOneChatEventBroadcaster     // Catch:{ all -> 0x0019 }
            java.lang.String r4 = r9.getContentType()     // Catch:{ all -> 0x0019 }
            int r5 = r9.getId()     // Catch:{ all -> 0x0019 }
            java.lang.String r5 = java.lang.String.valueOf(r5)     // Catch:{ all -> 0x0019 }
            com.gsma.services.rcs.chat.ChatLog$Message$Content$Status r6 = com.gsma.services.rcs.chat.ChatLog.Message.Content.Status.RECEIVED     // Catch:{ all -> 0x0019 }
            com.gsma.services.rcs.chat.ChatLog$Message$Content$ReasonCode r7 = com.gsma.services.rcs.chat.ChatLog.Message.Content.ReasonCode.UNSPECIFIED     // Catch:{ all -> 0x0019 }
            r2.broadcastMessageStatusChanged(r3, r4, r5, r6, r7)     // Catch:{ all -> 0x0019 }
            com.sec.internal.ims.servicemodules.tapi.service.broadcaster.OneToOneChatEventBroadcaster r8 = r8.mOneToOneChatEventBroadcaster     // Catch:{ all -> 0x0019 }
            int r2 = r9.getId()     // Catch:{ all -> 0x0019 }
            java.lang.String r2 = java.lang.String.valueOf(r2)     // Catch:{ all -> 0x0019 }
            java.lang.String r9 = r9.getContentType()     // Catch:{ all -> 0x0019 }
            r8.broadcastMessageReceived(r2, r9, r10, r0)     // Catch:{ all -> 0x0019 }
        L_0x006e:
            monitor-exit(r1)     // Catch:{ all -> 0x0019 }
            goto L_0x0072
        L_0x0070:
            monitor-exit(r1)     // Catch:{ all -> 0x0019 }
            throw r8
        L_0x0072:
            return
        */
        throw new UnsupportedOperationException("Method not decompiled: com.sec.internal.ims.servicemodules.tapi.service.api.ChatServiceImpl.handleReceiveMessage(com.sec.internal.ims.servicemodules.im.MessageBase, boolean):void");
    }

    public GroupChat.State translateState(ImSession.SessionState sessionState, ImDirection imDirection, ImSessionClosedReason imSessionClosedReason) {
        int i = AnonymousClass1.$SwitchMap$com$sec$internal$ims$servicemodules$im$ImSession$SessionState[sessionState.ordinal()];
        if (i != 1) {
            if (i != 2) {
                if (i == 3) {
                    return GroupChat.State.STARTED;
                }
                if ((i == 4 || i == 5) && imSessionClosedReason != ImSessionClosedReason.NONE) {
                    return GroupChat.State.ABORTED;
                }
                return null;
            } else if (imDirection == ImDirection.INCOMING) {
                return GroupChat.State.ACCEPTING;
            } else {
                return null;
            }
        } else if (imDirection == ImDirection.INCOMING) {
            return GroupChat.State.INVITED;
        } else {
            if (imDirection == ImDirection.OUTGOING) {
                return GroupChat.State.INITIATING;
            }
            return null;
        }
    }

    /* renamed from: com.sec.internal.ims.servicemodules.tapi.service.api.ChatServiceImpl$1  reason: invalid class name */
    static /* synthetic */ class AnonymousClass1 {
        static final /* synthetic */ int[] $SwitchMap$com$sec$internal$constants$ims$servicemodules$im$ImParticipant$Status;
        static final /* synthetic */ int[] $SwitchMap$com$sec$internal$constants$ims$servicemodules$im$reason$ImSessionClosedReason;
        static final /* synthetic */ int[] $SwitchMap$com$sec$internal$ims$servicemodules$im$ImSession$SessionState;

        /* JADX WARNING: Can't wrap try/catch for region: R(32:0|(2:1|2)|3|(2:5|6)|7|9|10|(2:11|12)|13|(2:15|16)|17|(2:19|20)|21|(2:23|24)|25|27|28|29|30|31|32|33|34|35|36|37|38|39|40|41|42|44) */
        /* JADX WARNING: Can't wrap try/catch for region: R(33:0|(2:1|2)|3|(2:5|6)|7|9|10|(2:11|12)|13|15|16|17|(2:19|20)|21|(2:23|24)|25|27|28|29|30|31|32|33|34|35|36|37|38|39|40|41|42|44) */
        /* JADX WARNING: Can't wrap try/catch for region: R(34:0|(2:1|2)|3|5|6|7|9|10|(2:11|12)|13|15|16|17|(2:19|20)|21|(2:23|24)|25|27|28|29|30|31|32|33|34|35|36|37|38|39|40|41|42|44) */
        /* JADX WARNING: Can't wrap try/catch for region: R(37:0|1|2|3|5|6|7|9|10|11|12|13|15|16|17|(2:19|20)|21|23|24|25|27|28|29|30|31|32|33|34|35|36|37|38|39|40|41|42|44) */
        /* JADX WARNING: Code restructure failed: missing block: B:45:?, code lost:
            return;
         */
        /* JADX WARNING: Failed to process nested try/catch */
        /* JADX WARNING: Missing exception handler attribute for start block: B:11:0x002e */
        /* JADX WARNING: Missing exception handler attribute for start block: B:29:0x006a */
        /* JADX WARNING: Missing exception handler attribute for start block: B:31:0x0074 */
        /* JADX WARNING: Missing exception handler attribute for start block: B:33:0x007e */
        /* JADX WARNING: Missing exception handler attribute for start block: B:35:0x0088 */
        /* JADX WARNING: Missing exception handler attribute for start block: B:37:0x0092 */
        /* JADX WARNING: Missing exception handler attribute for start block: B:39:0x009d */
        /* JADX WARNING: Missing exception handler attribute for start block: B:41:0x00a8 */
        static {
            /*
                com.sec.internal.constants.ims.servicemodules.im.reason.ImSessionClosedReason[] r0 = com.sec.internal.constants.ims.servicemodules.im.reason.ImSessionClosedReason.values()
                int r0 = r0.length
                int[] r0 = new int[r0]
                $SwitchMap$com$sec$internal$constants$ims$servicemodules$im$reason$ImSessionClosedReason = r0
                r1 = 1
                com.sec.internal.constants.ims.servicemodules.im.reason.ImSessionClosedReason r2 = com.sec.internal.constants.ims.servicemodules.im.reason.ImSessionClosedReason.CLOSED_BY_REMOTE     // Catch:{ NoSuchFieldError -> 0x0012 }
                int r2 = r2.ordinal()     // Catch:{ NoSuchFieldError -> 0x0012 }
                r0[r2] = r1     // Catch:{ NoSuchFieldError -> 0x0012 }
            L_0x0012:
                r0 = 2
                int[] r2 = $SwitchMap$com$sec$internal$constants$ims$servicemodules$im$reason$ImSessionClosedReason     // Catch:{ NoSuchFieldError -> 0x001d }
                com.sec.internal.constants.ims.servicemodules.im.reason.ImSessionClosedReason r3 = com.sec.internal.constants.ims.servicemodules.im.reason.ImSessionClosedReason.CLOSED_BY_LOCAL     // Catch:{ NoSuchFieldError -> 0x001d }
                int r3 = r3.ordinal()     // Catch:{ NoSuchFieldError -> 0x001d }
                r2[r3] = r0     // Catch:{ NoSuchFieldError -> 0x001d }
            L_0x001d:
                com.sec.internal.ims.servicemodules.im.ImSession$SessionState[] r2 = com.sec.internal.ims.servicemodules.im.ImSession.SessionState.values()
                int r2 = r2.length
                int[] r2 = new int[r2]
                $SwitchMap$com$sec$internal$ims$servicemodules$im$ImSession$SessionState = r2
                com.sec.internal.ims.servicemodules.im.ImSession$SessionState r3 = com.sec.internal.ims.servicemodules.im.ImSession.SessionState.INITIAL     // Catch:{ NoSuchFieldError -> 0x002e }
                int r3 = r3.ordinal()     // Catch:{ NoSuchFieldError -> 0x002e }
                r2[r3] = r1     // Catch:{ NoSuchFieldError -> 0x002e }
            L_0x002e:
                int[] r2 = $SwitchMap$com$sec$internal$ims$servicemodules$im$ImSession$SessionState     // Catch:{ NoSuchFieldError -> 0x0038 }
                com.sec.internal.ims.servicemodules.im.ImSession$SessionState r3 = com.sec.internal.ims.servicemodules.im.ImSession.SessionState.STARTING     // Catch:{ NoSuchFieldError -> 0x0038 }
                int r3 = r3.ordinal()     // Catch:{ NoSuchFieldError -> 0x0038 }
                r2[r3] = r0     // Catch:{ NoSuchFieldError -> 0x0038 }
            L_0x0038:
                r2 = 3
                int[] r3 = $SwitchMap$com$sec$internal$ims$servicemodules$im$ImSession$SessionState     // Catch:{ NoSuchFieldError -> 0x0043 }
                com.sec.internal.ims.servicemodules.im.ImSession$SessionState r4 = com.sec.internal.ims.servicemodules.im.ImSession.SessionState.ESTABLISHED     // Catch:{ NoSuchFieldError -> 0x0043 }
                int r4 = r4.ordinal()     // Catch:{ NoSuchFieldError -> 0x0043 }
                r3[r4] = r2     // Catch:{ NoSuchFieldError -> 0x0043 }
            L_0x0043:
                r3 = 4
                int[] r4 = $SwitchMap$com$sec$internal$ims$servicemodules$im$ImSession$SessionState     // Catch:{ NoSuchFieldError -> 0x004e }
                com.sec.internal.ims.servicemodules.im.ImSession$SessionState r5 = com.sec.internal.ims.servicemodules.im.ImSession.SessionState.CLOSING     // Catch:{ NoSuchFieldError -> 0x004e }
                int r5 = r5.ordinal()     // Catch:{ NoSuchFieldError -> 0x004e }
                r4[r5] = r3     // Catch:{ NoSuchFieldError -> 0x004e }
            L_0x004e:
                r4 = 5
                int[] r5 = $SwitchMap$com$sec$internal$ims$servicemodules$im$ImSession$SessionState     // Catch:{ NoSuchFieldError -> 0x0059 }
                com.sec.internal.ims.servicemodules.im.ImSession$SessionState r6 = com.sec.internal.ims.servicemodules.im.ImSession.SessionState.CLOSED     // Catch:{ NoSuchFieldError -> 0x0059 }
                int r6 = r6.ordinal()     // Catch:{ NoSuchFieldError -> 0x0059 }
                r5[r6] = r4     // Catch:{ NoSuchFieldError -> 0x0059 }
            L_0x0059:
                com.sec.internal.constants.ims.servicemodules.im.ImParticipant$Status[] r5 = com.sec.internal.constants.ims.servicemodules.im.ImParticipant.Status.values()
                int r5 = r5.length
                int[] r5 = new int[r5]
                $SwitchMap$com$sec$internal$constants$ims$servicemodules$im$ImParticipant$Status = r5
                com.sec.internal.constants.ims.servicemodules.im.ImParticipant$Status r6 = com.sec.internal.constants.ims.servicemodules.im.ImParticipant.Status.INITIAL     // Catch:{ NoSuchFieldError -> 0x006a }
                int r6 = r6.ordinal()     // Catch:{ NoSuchFieldError -> 0x006a }
                r5[r6] = r1     // Catch:{ NoSuchFieldError -> 0x006a }
            L_0x006a:
                int[] r1 = $SwitchMap$com$sec$internal$constants$ims$servicemodules$im$ImParticipant$Status     // Catch:{ NoSuchFieldError -> 0x0074 }
                com.sec.internal.constants.ims.servicemodules.im.ImParticipant$Status r5 = com.sec.internal.constants.ims.servicemodules.im.ImParticipant.Status.INVITED     // Catch:{ NoSuchFieldError -> 0x0074 }
                int r5 = r5.ordinal()     // Catch:{ NoSuchFieldError -> 0x0074 }
                r1[r5] = r0     // Catch:{ NoSuchFieldError -> 0x0074 }
            L_0x0074:
                int[] r0 = $SwitchMap$com$sec$internal$constants$ims$servicemodules$im$ImParticipant$Status     // Catch:{ NoSuchFieldError -> 0x007e }
                com.sec.internal.constants.ims.servicemodules.im.ImParticipant$Status r1 = com.sec.internal.constants.ims.servicemodules.im.ImParticipant.Status.ACCEPTED     // Catch:{ NoSuchFieldError -> 0x007e }
                int r1 = r1.ordinal()     // Catch:{ NoSuchFieldError -> 0x007e }
                r0[r1] = r2     // Catch:{ NoSuchFieldError -> 0x007e }
            L_0x007e:
                int[] r0 = $SwitchMap$com$sec$internal$constants$ims$servicemodules$im$ImParticipant$Status     // Catch:{ NoSuchFieldError -> 0x0088 }
                com.sec.internal.constants.ims.servicemodules.im.ImParticipant$Status r1 = com.sec.internal.constants.ims.servicemodules.im.ImParticipant.Status.PENDING     // Catch:{ NoSuchFieldError -> 0x0088 }
                int r1 = r1.ordinal()     // Catch:{ NoSuchFieldError -> 0x0088 }
                r0[r1] = r3     // Catch:{ NoSuchFieldError -> 0x0088 }
            L_0x0088:
                int[] r0 = $SwitchMap$com$sec$internal$constants$ims$servicemodules$im$ImParticipant$Status     // Catch:{ NoSuchFieldError -> 0x0092 }
                com.sec.internal.constants.ims.servicemodules.im.ImParticipant$Status r1 = com.sec.internal.constants.ims.servicemodules.im.ImParticipant.Status.DECLINED     // Catch:{ NoSuchFieldError -> 0x0092 }
                int r1 = r1.ordinal()     // Catch:{ NoSuchFieldError -> 0x0092 }
                r0[r1] = r4     // Catch:{ NoSuchFieldError -> 0x0092 }
            L_0x0092:
                int[] r0 = $SwitchMap$com$sec$internal$constants$ims$servicemodules$im$ImParticipant$Status     // Catch:{ NoSuchFieldError -> 0x009d }
                com.sec.internal.constants.ims.servicemodules.im.ImParticipant$Status r1 = com.sec.internal.constants.ims.servicemodules.im.ImParticipant.Status.GONE     // Catch:{ NoSuchFieldError -> 0x009d }
                int r1 = r1.ordinal()     // Catch:{ NoSuchFieldError -> 0x009d }
                r2 = 6
                r0[r1] = r2     // Catch:{ NoSuchFieldError -> 0x009d }
            L_0x009d:
                int[] r0 = $SwitchMap$com$sec$internal$constants$ims$servicemodules$im$ImParticipant$Status     // Catch:{ NoSuchFieldError -> 0x00a8 }
                com.sec.internal.constants.ims.servicemodules.im.ImParticipant$Status r1 = com.sec.internal.constants.ims.servicemodules.im.ImParticipant.Status.TIMEOUT     // Catch:{ NoSuchFieldError -> 0x00a8 }
                int r1 = r1.ordinal()     // Catch:{ NoSuchFieldError -> 0x00a8 }
                r2 = 7
                r0[r1] = r2     // Catch:{ NoSuchFieldError -> 0x00a8 }
            L_0x00a8:
                int[] r0 = $SwitchMap$com$sec$internal$constants$ims$servicemodules$im$ImParticipant$Status     // Catch:{ NoSuchFieldError -> 0x00b4 }
                com.sec.internal.constants.ims.servicemodules.im.ImParticipant$Status r1 = com.sec.internal.constants.ims.servicemodules.im.ImParticipant.Status.TO_INVITE     // Catch:{ NoSuchFieldError -> 0x00b4 }
                int r1 = r1.ordinal()     // Catch:{ NoSuchFieldError -> 0x00b4 }
                r2 = 8
                r0[r1] = r2     // Catch:{ NoSuchFieldError -> 0x00b4 }
            L_0x00b4:
                return
            */
            throw new UnsupportedOperationException("Method not decompiled: com.sec.internal.ims.servicemodules.tapi.service.api.ChatServiceImpl.AnonymousClass1.<clinit>():void");
        }
    }

    public GroupChat.ReasonCode translateReasonCode(ImSessionClosedReason imSessionClosedReason) {
        GroupChat.ReasonCode reasonCode = GroupChat.ReasonCode.UNSPECIFIED;
        int i = AnonymousClass1.$SwitchMap$com$sec$internal$constants$ims$servicemodules$im$reason$ImSessionClosedReason[imSessionClosedReason.ordinal()];
        if (i == 1) {
            return GroupChat.ReasonCode.ABORTED_BY_REMOTE;
        }
        if (i != 2) {
            return GroupChat.ReasonCode.UNSPECIFIED;
        }
        return GroupChat.ReasonCode.ABORTED_BY_USER;
    }

    public static ChatLog.Message.Content.Status translateStatus(ImConstants.Status status) {
        ChatLog.Message.Content.Status status2 = ChatLog.Message.Content.Status.DISPLAY_REPORT_REQUESTED;
        if (ImConstants.Status.SENDING == status) {
            return ChatLog.Message.Content.Status.SENDING;
        }
        if (ImConstants.Status.SENT == status) {
            return ChatLog.Message.Content.Status.SENT;
        }
        if (ImConstants.Status.FAILED == status) {
            return ChatLog.Message.Content.Status.FAILED;
        }
        if (ImConstants.Status.TO_SEND == status) {
            return ChatLog.Message.Content.Status.QUEUED;
        }
        return ImConstants.Status.READ == status ? ChatLog.Message.Content.Status.DISPLAYED : status2;
    }

    /* JADX WARNING: Code restructure failed: missing block: B:2:0x000a, code lost:
        r0 = r0.getParticipantsString();
     */
    /* Code decompiled incorrectly, please refer to instructions dump. */
    public java.lang.String getRemoteUserByChatId(java.lang.String r1) {
        /*
            r0 = this;
            com.sec.internal.ims.servicemodules.im.ImCache r0 = com.sec.internal.ims.servicemodules.im.ImCache.getInstance()
            com.sec.internal.ims.servicemodules.im.ImSession r0 = r0.getImSession(r1)
            if (r0 == 0) goto L_0x001e
            java.util.List r0 = r0.getParticipantsString()
            if (r0 == 0) goto L_0x001e
            int r1 = r0.size()
            if (r1 <= 0) goto L_0x001e
            r1 = 0
            java.lang.Object r0 = r0.get(r1)
            java.lang.String r0 = (java.lang.String) r0
            return r0
        L_0x001e:
            java.lang.String r0 = ""
            return r0
        */
        throw new UnsupportedOperationException("Method not decompiled: com.sec.internal.ims.servicemodules.tapi.service.api.ChatServiceImpl.getRemoteUserByChatId(java.lang.String):java.lang.String");
    }

    public void onComposingNotificationReceived(String str, boolean z, ImsUri imsUri, String str2, boolean z2, int i) {
        ContactId contactId;
        Log.i(LOG_TAG, "onComposingNotificationReceived");
        synchronized (this.mLock) {
            if (imsUri != null) {
                try {
                    contactId = new ContactId(PhoneUtils.extractNumberFromUri(imsUri.toString()));
                } catch (RemoteException e) {
                    e.printStackTrace();
                } catch (Throwable th) {
                    throw th;
                }
            } else {
                contactId = null;
            }
            if (z) {
                int beginBroadcast = this.mGroupChatListeners.beginBroadcast();
                for (int i2 = 0; i2 < beginBroadcast; i2++) {
                    this.mGroupChatListeners.getBroadcastItem(i2).onComposingEvent(str, contactId, z2);
                }
                this.mGroupChatListeners.finishBroadcast();
            } else {
                this.mOneToOneChatEventBroadcaster.broadcastComposingEvent(contactId, z2);
            }
        }
    }

    public void onParticipantsAdded(ImSession imSession, Collection<ImParticipant> collection) {
        for (ImParticipant notifyGroupParticipantInfoChanged : collection) {
            notifyGroupParticipantInfoChanged(notifyGroupParticipantInfoChanged);
        }
    }

    public void onParticipantsJoined(ImSession imSession, Collection<ImParticipant> collection) {
        for (ImParticipant notifyGroupParticipantInfoChanged : collection) {
            notifyGroupParticipantInfoChanged(notifyGroupParticipantInfoChanged);
        }
    }

    public void onParticipantsLeft(ImSession imSession, Collection<ImParticipant> collection) {
        for (ImParticipant notifyGroupParticipantInfoChanged : collection) {
            notifyGroupParticipantInfoChanged(notifyGroupParticipantInfoChanged);
        }
    }

    public void onMessageReceived(MessageBase messageBase, ImSession imSession) {
        if (imSession.isGroupChat()) {
            receiveGroupChatMessage(messageBase);
            return;
        }
        String imsUri = messageBase.getRemoteUri().toString();
        ContactId contactId = new ContactId(PhoneUtils.extractNumberFromUri(imsUri));
        UserHandle subscriptionUserHandle = TelephonyUtilsWrapper.getSubscriptionUserHandle(this.mContext, SimUtil.getSubId(this.mImModule.getPhoneIdByIMSI(imSession.getOwnImsi())));
        if (subscriptionUserHandle == null) {
            subscriptionUserHandle = ContextExt.CURRENT_OR_SELF;
        }
        this.mOneToOneChatEventBroadcaster.broadcastMessageStatusChanged(contactId, messageBase.getContentType(), String.valueOf(messageBase.getId()), ChatLog.Message.Content.Status.RECEIVED, ChatLog.Message.Content.ReasonCode.UNSPECIFIED);
        this.mOneToOneChatEventBroadcaster.broadcastMessageReceived(String.valueOf(messageBase.getId()), messageBase.getContentType(), imsUri, subscriptionUserHandle);
    }

    public void onMessageSendingSucceeded(MessageBase messageBase) {
        Log.d(LOG_TAG, "onMessageSendingSucceeded():");
        notifyMessageStateChanged(new ContactId(PhoneUtils.extractNumberFromUri(getRemoteUserByChatId(messageBase.getChatId()))), messageBase, ChatLog.Message.Content.Status.SENT, ChatLog.Message.Content.ReasonCode.UNSPECIFIED);
    }

    public void onMessageSendingFailed(MessageBase messageBase, IMnoStrategy.StrategyResponse strategyResponse, Result result) {
        Log.d(LOG_TAG, "onMessageSendingFailed():");
        notifyMessageStateChanged(new ContactId(PhoneUtils.extractNumberFromUri(getRemoteUserByChatId(messageBase.getChatId()))), messageBase, ChatLog.Message.Content.Status.FAILED, ChatLog.Message.Content.ReasonCode.FAILED_SEND);
    }

    public void onImdnNotificationReceived(MessageBase messageBase, ImsUri imsUri, NotificationStatus notificationStatus, boolean z) {
        ContactId contactId = new ContactId(PhoneUtils.extractNumberFromUri(getRemoteUserByChatId(messageBase.getChatId())));
        Log.d(LOG_TAG, "onImdnNotificationReceived()");
        if (NotificationStatus.DELIVERED == notificationStatus) {
            if (!z) {
                this.mOneToOneChatEventBroadcaster.broadcastMessageStatusChanged(contactId, messageBase.getContentType(), String.valueOf(messageBase.getId()), ChatLog.Message.Content.Status.DELIVERED, ChatLog.Message.Content.ReasonCode.UNSPECIFIED);
            } else {
                notifyMessageGroupDeliveryInfoChanged((ImMessage) messageBase, imsUri, GroupDeliveryInfo.Status.DELIVERED, GroupDeliveryInfo.ReasonCode.UNSPECIFIED);
            }
        } else if (NotificationStatus.DISPLAYED != notificationStatus) {
        } else {
            if (!z) {
                this.mOneToOneChatEventBroadcaster.broadcastMessageStatusChanged(contactId, messageBase.getContentType(), String.valueOf(messageBase.getId()), ChatLog.Message.Content.Status.DISPLAYED, ChatLog.Message.Content.ReasonCode.UNSPECIFIED);
            } else {
                notifyMessageGroupDeliveryInfoChanged((ImMessage) messageBase, imsUri, GroupDeliveryInfo.Status.DISPLAYED, GroupDeliveryInfo.ReasonCode.UNSPECIFIED);
            }
        }
    }
}
