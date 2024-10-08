package com.sec.internal.ims.servicemodules.im;

import android.annotation.SuppressLint;
import android.content.ContentValues;
import android.database.Cursor;
import android.net.Network;
import android.net.Uri;
import android.os.Bundle;
import android.text.TextUtils;
import android.util.Log;
import android.util.LruCache;
import com.sec.ims.util.ImsUri;
import com.sec.internal.constants.ims.MIMEContentType;
import com.sec.internal.constants.ims.servicemodules.im.ChatData;
import com.sec.internal.constants.ims.servicemodules.im.ChatMode;
import com.sec.internal.constants.ims.servicemodules.im.FileDisposition;
import com.sec.internal.constants.ims.servicemodules.im.ImCacheAction;
import com.sec.internal.constants.ims.servicemodules.im.ImConstants;
import com.sec.internal.constants.ims.servicemodules.im.ImContract;
import com.sec.internal.constants.ims.servicemodules.im.ImDirection;
import com.sec.internal.constants.ims.servicemodules.im.ImParticipant;
import com.sec.internal.constants.ims.servicemodules.im.NotificationStatus;
import com.sec.internal.constants.ims.servicemodules.im.event.FtIncomingSessionEvent;
import com.sec.internal.constants.ims.servicemodules.im.event.ImIncomingMessageEvent;
import com.sec.internal.constants.ims.servicemodules.im.event.ImIncomingSessionEvent;
import com.sec.internal.constants.ims.servicemodules.im.event.SlmIncomingMessageEvent;
import com.sec.internal.constants.ims.servicemodules.im.reason.CancelReason;
import com.sec.internal.helper.FileUtils;
import com.sec.internal.helper.ImExtensionMNOHeadersHelper;
import com.sec.internal.ims.registry.ImsRegistry;
import com.sec.internal.ims.servicemodules.im.FtHttpIncomingMessage;
import com.sec.internal.ims.servicemodules.im.FtHttpOutgoingMessage;
import com.sec.internal.ims.servicemodules.im.FtMsrpMessage;
import com.sec.internal.ims.servicemodules.im.ImMessage;
import com.sec.internal.ims.servicemodules.im.data.MessageKey;
import com.sec.internal.ims.servicemodules.im.listener.IImCacheActionListener;
import com.sec.internal.ims.servicemodules.im.util.ImCpimNamespacesHelper;
import com.sec.internal.ims.util.StringIdGenerator;
import com.sec.internal.ims.util.UriGenerator;
import com.sec.internal.interfaces.ims.servicemodules.im.IImServiceInterface;
import com.sec.internal.log.IMSLog;
import java.io.File;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Date;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Observable;
import java.util.Observer;
import java.util.Set;

@SuppressLint({"UseSparseArrays"})
public class ImCache {
    private static final int DEFAULT_MAX_CONCURRENT_SESSION = 100;
    /* access modifiers changed from: private */
    public static final String LOG_TAG = "ImCache";
    private static final int MAX_CACHED_MESSAGE = 30;
    private static final int MAX_CACHED_SESSION = 500;
    private static ImCache sInstance;
    /* access modifiers changed from: private */
    public final LruCache<Integer, ImSession> mActiveSessions = new LruCache<Integer, ImSession>(100) {
        /* access modifiers changed from: protected */
        public void entryRemoved(boolean z, Integer num, ImSession imSession, ImSession imSession2) {
            if (z) {
                String r0 = ImCache.LOG_TAG;
                Log.i(r0, "mActiveSessions#entryRemoved: " + imSession.getChatId());
                imSession.closeSession();
            }
        }
    };
    private LruCache<MessageKey, MessageBase> mCachingMessages = new LruCache<MessageKey, MessageBase>(30) {
        /* access modifiers changed from: protected */
        public MessageBase create(MessageKey messageKey) {
            String r0 = ImCache.LOG_TAG;
            Log.i(r0, "Cache miss. attempt to load from db: " + messageKey);
            MessageBase queryMessage = ImCache.this.mPersister.queryMessage(messageKey.imdnId, messageKey.direction, messageKey.chatId);
            if (queryMessage != null) {
                return ImCache.this.loadExtras(queryMessage);
            }
            Log.i(ImCache.LOG_TAG, "Couldn't load from db.");
            return null;
        }

        /* access modifiers changed from: protected */
        public void entryRemoved(boolean z, MessageKey messageKey, MessageBase messageBase, MessageBase messageBase2) {
            if (z) {
                String r1 = ImCache.LOG_TAG;
                Log.i(r1, "CachingMessage#entryRemoved: id= " + messageBase.getId());
                ImCache.this.unregisterObserver(messageBase);
            }
        }
    };
    private final Map<String, Set<ImsUri>> mChatbotRoleUris = new HashMap();
    private CmStoreInvoker mCmStoreInvoker;
    private ImModule mImModule;
    private final LruCache<String, ImSession> mImSessions = new LruCache<String, ImSession>(500) {
        /* access modifiers changed from: protected */
        public ImSession create(String str) {
            String r0 = ImCache.LOG_TAG;
            Log.i(r0, "Cache miss. attempt to load from db: " + str);
            ChatData querySessionByChatId = ImCache.this.mPersister.querySessionByChatId(str);
            if (querySessionByChatId != null) {
                return ImCache.this.createSession(querySessionByChatId);
            }
            Log.i(ImCache.LOG_TAG, "Couldn't load from db.");
            return null;
        }

        /* access modifiers changed from: protected */
        public void entryRemoved(boolean z, String str, ImSession imSession, ImSession imSession2) {
            if (z) {
                ImCache.this.mActiveSessions.remove(Integer.valueOf(imSession.getId()));
                String r0 = ImCache.LOG_TAG;
                Log.i(r0, "ImSessions#entryRemoved: " + imSession.getChatId());
                imSession.closeSession();
            }
        }
    };
    private boolean mIsLoaded;
    private final Set<IImCacheActionListener> mListener = new HashSet();
    private final Observer mObserver = new ImCache$$ExternalSyntheticLambda2(this);
    private final MessageMap mPendingMessages = new MessageMap();
    /* access modifiers changed from: private */
    public ImPersister mPersister;

    /* access modifiers changed from: private */
    public /* synthetic */ void lambda$new$0(Observable observable, Object obj) {
        if (observable instanceof ChatData) {
            updateChat((ChatData) observable, (ImCacheAction) obj);
        } else if (observable instanceof MessageBase) {
            updateMessage((MessageBase) observable, (ImCacheAction) obj);
        } else if (observable instanceof ImParticipant) {
            updateParticipant((ImParticipant) observable, (ImCacheAction) obj);
        } else {
            String str = LOG_TAG;
            Log.e(str, "Unknown observable :" + observable + ", data : " + obj);
        }
    }

    protected ImCache() {
    }

    public static synchronized ImCache getInstance() {
        ImCache imCache;
        synchronized (ImCache.class) {
            if (sInstance == null) {
                sInstance = new ImCache();
            }
            imCache = sInstance;
        }
        return imCache;
    }

    public void initializeLruCache(int i) {
        if (i <= 0) {
            i = 100;
        }
        this.mActiveSessions.resize(Math.min(i, 500));
    }

    public void addImCacheActionListener(IImCacheActionListener iImCacheActionListener) {
        this.mListener.add(iImCacheActionListener);
    }

    public void removeImCacheActionListener(IImCacheActionListener iImCacheActionListener) {
        this.mListener.remove(iImCacheActionListener);
    }

    private void registerObserver(Observable observable) {
        observable.addObserver(this.mObserver);
    }

    /* access modifiers changed from: private */
    public void unregisterObserver(Observable observable) {
        observable.deleteObserver(this.mObserver);
    }

    private void updateChat(ChatData chatData, ImCacheAction imCacheAction) {
        this.mPersister.updateChat(chatData, imCacheAction);
    }

    private void updateMessage(MessageBase messageBase, ImCacheAction imCacheAction) {
        this.mPersister.updateMessage(messageBase, imCacheAction);
        for (IImCacheActionListener updateMessage : this.mListener) {
            updateMessage.updateMessage(messageBase, imCacheAction);
        }
    }

    private void updateMessage(Collection<MessageBase> collection, ImCacheAction imCacheAction) {
        this.mPersister.updateMessage(collection, imCacheAction);
        for (IImCacheActionListener updateMessage : this.mListener) {
            updateMessage.updateMessage(collection, imCacheAction);
        }
    }

    private void updateParticipant(ImParticipant imParticipant, ImCacheAction imCacheAction) {
        this.mPersister.updateParticipant(imParticipant, imCacheAction);
        for (IImCacheActionListener updateParticipant : this.mListener) {
            updateParticipant.updateParticipant(imParticipant, imCacheAction);
        }
    }

    private void updateParticipant(Collection<ImParticipant> collection, ImCacheAction imCacheAction) {
        this.mPersister.updateParticipant(collection, imCacheAction);
        for (IImCacheActionListener updateParticipant : this.mListener) {
            updateParticipant.updateParticipant(collection, imCacheAction);
        }
    }

    private void updateParticipantFromCloud(Collection<ImParticipant> collection, ImCacheAction imCacheAction) {
        for (IImCacheActionListener updateParticipant : this.mListener) {
            updateParticipant.updateParticipant(collection, imCacheAction);
        }
    }

    public synchronized void load(ImModule imModule) {
        if (this.mIsLoaded) {
            Log.i(LOG_TAG, "Alraedy loaded");
            return;
        }
        this.mImModule = imModule;
        this.mPersister = new ImPersister(this.mImModule.getContext(), this.mImModule);
        this.mCmStoreInvoker = new CmStoreInvoker(imModule);
        this.mIsLoaded = true;
    }

    public synchronized void loadImSessionByChatType(boolean z) {
        List<String> querySessionByChatType = this.mPersister.querySessionByChatType(z);
        String str = LOG_TAG;
        Log.i(str, "loadImSessionByChatType loaded chat ids : " + querySessionByChatType);
        if (querySessionByChatType != null && !querySessionByChatType.isEmpty()) {
            for (String str2 : querySessionByChatType) {
                this.mImSessions.get(str2);
            }
        }
    }

    public synchronized void loadImSessionForAutoRejoin(boolean z) {
        List<String> querySessionForAutoRejoin = this.mPersister.querySessionForAutoRejoin(z);
        String str = LOG_TAG;
        Log.i(str, "loadImSessionForAutoRejoin isForAll : " + z + ", Autorejoin chat ids : " + querySessionForAutoRejoin);
        if (!querySessionForAutoRejoin.isEmpty()) {
            for (String str2 : querySessionForAutoRejoin) {
                this.mImSessions.get(str2);
            }
        }
    }

    public synchronized void loadImSessionWithPendingMessages() {
        List<String> queryAllChatIDwithPendingMessages = this.mPersister.queryAllChatIDwithPendingMessages();
        String str = LOG_TAG;
        Log.i(str, "loadImSessionWithPendingMessages " + queryAllChatIDwithPendingMessages.size() + " pending message(s)");
        if (!queryAllChatIDwithPendingMessages.isEmpty()) {
            for (String str2 : queryAllChatIDwithPendingMessages) {
                this.mImSessions.get(str2);
            }
        }
    }

    public synchronized void loadImSessionWithFailedFTMessages() {
        List<String> queryAllChatIDwithFailedFTMessages = this.mPersister.queryAllChatIDwithFailedFTMessages();
        String str = LOG_TAG;
        Log.i(str, "loadImSessionWithFailedFTMessages " + queryAllChatIDwithFailedFTMessages.size() + " failed message(s)");
        if (!queryAllChatIDwithFailedFTMessages.isEmpty()) {
            for (String str2 : queryAllChatIDwithFailedFTMessages) {
                this.mImSessions.get(str2);
            }
        }
    }

    public synchronized void updateUriGenerator(int i) {
        Log.i(LOG_TAG, "updateUriGenerator");
        UriGenerator uriGenerator = this.mImModule.getUriGenerator(i);
        for (ImSession updateUriGenerator : this.mImSessions.snapshot().values()) {
            updateUriGenerator.updateUriGenerator(uriGenerator);
        }
    }

    public synchronized List<Bundle> loadLastSentMessages(List<String> list) {
        return this.mPersister.queryLastSentMessages(list);
    }

    public synchronized boolean isLoaded() {
        return this.mIsLoaded;
    }

    public synchronized void clear() {
        this.mImSessions.evictAll();
    }

    /* access modifiers changed from: private */
    public synchronized ImSession createSession(ChatData chatData) {
        HashMap hashMap;
        int phoneIdByIMSI;
        HashMap hashMap2;
        hashMap = new HashMap();
        for (ImParticipant next : this.mPersister.queryParticipantSet(chatData.getChatId())) {
            hashMap.put(next.getUri(), next);
            registerObserver(next);
        }
        phoneIdByIMSI = this.mImModule.getPhoneIdByIMSI(chatData.getOwnIMSI());
        String str = LOG_TAG;
        Log.i(str, "Load participants: size()=" + hashMap.size() + ", values()=" + IMSLog.checker(hashMap.values()));
        if (this.mImModule.getImConfig(phoneIdByIMSI).getImMsgTech() == ImConstants.ImMsgTech.CPM) {
            if (chatData.getConversationId() == null) {
                chatData.setConversationId(StringIdGenerator.generateConversationId());
            }
            if (chatData.getContributionId() == null) {
                chatData.setContributionId(chatData.isGroupChat() ? chatData.getConversationId() : StringIdGenerator.generateContributionId());
            }
        } else if (chatData.getContributionId() == null) {
            chatData.setContributionId(StringIdGenerator.generateContributionId());
        }
        loadPendingMessages(chatData);
        hashMap2 = new HashMap();
        if (this.mImModule.getImConfig(phoneIdByIMSI).getChatRevokeTimer() > 0) {
            for (MessageBase next2 : loadMessageListForRevoke(chatData)) {
                hashMap2.put(next2.getImdnId(), Integer.valueOf(next2.getId()));
            }
        }
        registerObserver(chatData);
        return new ImSessionBuilder().looper(this.mImModule.getLooper()).listener(this.mImModule.getImSessionProcessor()).config(this.mImModule.getImConfig(phoneIdByIMSI)).imsService(getImHandler()).slmService(ImsRegistry.getHandlerFactory().getSlmHandler()).uriGenerator(this.mImModule.getUriGenerator(phoneIdByIMSI)).chatData(chatData).participants(hashMap).needToRevokeMessages(hashMap2).getter(this.mImModule).build();
    }

    private void loadPendingMessages(ChatData chatData) {
        List<Integer> queryPendingMessageIds = this.mPersister.queryPendingMessageIds(chatData.getChatId());
        String str = LOG_TAG;
        Log.i(str, "pending messages count:" + queryPendingMessageIds.size());
        int phoneIdByIMSI = this.mImModule.getPhoneIdByIMSI(chatData.getOwnIMSI());
        ArrayList arrayList = new ArrayList();
        for (Integer intValue : queryPendingMessageIds) {
            int intValue2 = intValue.intValue();
            MessageBase messageBase = this.mPendingMessages.get(intValue2);
            if (messageBase == null) {
                arrayList.add(String.valueOf(intValue2));
            } else if ((messageBase instanceof FtHttpIncomingMessage) || (messageBase instanceof FtHttpOutgoingMessage)) {
                ImModule imModule = this.mImModule;
                messageBase.setNetwork(imModule.getNetwork(imModule.getImConfig(phoneIdByIMSI).isFtHttpOverDefaultPdn(), phoneIdByIMSI));
            } else {
                messageBase.setNetwork(this.mImModule.getNetwork(false, phoneIdByIMSI));
            }
        }
        for (MessageBase next : this.mPersister.queryMessages((Collection<String>) arrayList)) {
            next.setImdnRecRouteList(this.mPersister.queryImImdnRecRoute(next));
            if ((next instanceof FtHttpIncomingMessage) || (next instanceof FtHttpOutgoingMessage)) {
                ImModule imModule2 = this.mImModule;
                next.setNetwork(imModule2.getNetwork(imModule2.getImConfig(phoneIdByIMSI).isFtHttpOverDefaultPdn(), phoneIdByIMSI));
            } else {
                next.setNetwork(this.mImModule.getNetwork(false, phoneIdByIMSI));
            }
            if (next instanceof FtMessage) {
                FtMessage ftMessage = (FtMessage) next;
                ftMessage.setIsGroupChat(chatData.isGroupChat());
                ftMessage.setContributionId(chatData.getContributionId());
                ftMessage.setConversationId(chatData.getConversationId());
            }
            registerObserver(next);
            this.mPendingMessages.put(next);
        }
    }

    private List<MessageBase> loadMessageListForRevoke(ChatData chatData) {
        List<Integer> queryMessagesIdsForRevoke = this.mPersister.queryMessagesIdsForRevoke(chatData.getChatId());
        String str = LOG_TAG;
        Log.i(str, "revoke messages count:" + queryMessagesIdsForRevoke.size());
        ArrayList arrayList = new ArrayList();
        ArrayList arrayList2 = new ArrayList();
        for (Integer intValue : queryMessagesIdsForRevoke) {
            int intValue2 = intValue.intValue();
            if (this.mPendingMessages.containsKey(intValue2)) {
                arrayList.add(this.mPendingMessages.get(intValue2));
            } else {
                arrayList2.add(String.valueOf(intValue2));
            }
        }
        for (MessageBase next : this.mPersister.queryMessages((Collection<String>) arrayList2)) {
            this.mPendingMessages.put(next);
            next.setImdnRecRouteList(this.mPersister.queryImImdnRecRoute(next));
            registerObserver(next);
            arrayList.add(next);
        }
        return arrayList;
    }

    private IImServiceInterface getImHandler() {
        return this.mImModule.getImHandler();
    }

    public ImSession getImSession(String str) {
        ImSession imSession;
        if (str == null) {
            return null;
        }
        synchronized (this) {
            imSession = this.mImSessions.get(str);
        }
        return imSession;
    }

    public synchronized ImSession getImSessionByContributionId(String str, String str2, boolean z) {
        if (str2 == null) {
            return null;
        }
        for (ImSession next : this.mImSessions.snapshot().values()) {
            if (str.equals(next.getOwnImsi()) && str2.equals(next.getContributionId()) && next.isGroupChat() == z) {
                return next;
            }
        }
        ChatData querySessionByContributionId = this.mPersister.querySessionByContributionId(str, str2, z);
        if (querySessionByContributionId == null) {
            Log.i(LOG_TAG, "getImSessionByContributionId: Couldn't load from db.");
            return null;
        }
        return this.mImSessions.get(querySessionByContributionId.getChatId());
    }

    public synchronized ImSession getImSessionByConversationId(String str, String str2, boolean z) {
        String str3 = LOG_TAG;
        IMSLog.s(str3, "getImSessionByConversationId cid=" + str2 + " isGroupChat=" + z);
        if (str2 == null) {
            return null;
        }
        for (ImSession next : this.mImSessions.snapshot().values()) {
            if (str.equals(next.getOwnImsi()) && next.isGroupChat() == z && str2.equals(next.getConversationId())) {
                return next;
            }
        }
        ChatData querySessionByConversationId = this.mPersister.querySessionByConversationId(str, str2, z);
        if (querySessionByConversationId == null) {
            Log.i(LOG_TAG, "getImSessionByConversationId: Couldn't load from db.");
            return null;
        }
        return this.mImSessions.get(querySessionByConversationId.getChatId());
    }

    public synchronized ImSession getImSessionByRawHandle(Object obj) {
        for (ImSession next : this.mImSessions.snapshot().values()) {
            if (next.hasImSessionInfo(obj)) {
                return next;
            }
        }
        return null;
    }

    public ImSession getImSessionByParticipants(Set<ImsUri> set, ChatData.ChatType chatType, String str) {
        return getImSessionByParticipants(set, chatType, str, ChatMode.OFF);
    }

    /* JADX WARNING: Code restructure failed: missing block: B:30:0x00b8, code lost:
        return null;
     */
    /* Code decompiled incorrectly, please refer to instructions dump. */
    public synchronized com.sec.internal.ims.servicemodules.im.ImSession getImSessionByParticipants(java.util.Set<com.sec.ims.util.ImsUri> r8, com.sec.internal.constants.ims.servicemodules.im.ChatData.ChatType r9, java.lang.String r10, com.sec.internal.constants.ims.servicemodules.im.ChatMode r11) {
        /*
            r7 = this;
            monitor-enter(r7)
            java.lang.String r0 = LOG_TAG     // Catch:{ all -> 0x00b9 }
            java.lang.StringBuilder r1 = new java.lang.StringBuilder     // Catch:{ all -> 0x00b9 }
            r1.<init>()     // Catch:{ all -> 0x00b9 }
            java.lang.String r2 = "getImSessionByParticipants chatType= "
            r1.append(r2)     // Catch:{ all -> 0x00b9 }
            r1.append(r9)     // Catch:{ all -> 0x00b9 }
            java.lang.String r2 = " participants="
            r1.append(r2)     // Catch:{ all -> 0x00b9 }
            java.lang.String r2 = com.sec.internal.log.IMSLog.numberChecker((java.util.Collection<com.sec.ims.util.ImsUri>) r8)     // Catch:{ all -> 0x00b9 }
            r1.append(r2)     // Catch:{ all -> 0x00b9 }
            java.lang.String r2 = " imsi="
            r1.append(r2)     // Catch:{ all -> 0x00b9 }
            java.lang.String r2 = com.sec.internal.log.IMSLog.numberChecker((java.lang.String) r10)     // Catch:{ all -> 0x00b9 }
            r1.append(r2)     // Catch:{ all -> 0x00b9 }
            java.lang.String r1 = r1.toString()     // Catch:{ all -> 0x00b9 }
            android.util.Log.i(r0, r1)     // Catch:{ all -> 0x00b9 }
            r0 = 0
            if (r8 == 0) goto L_0x00b7
            boolean r1 = r8.isEmpty()     // Catch:{ all -> 0x00b9 }
            if (r1 == 0) goto L_0x003a
            goto L_0x00b7
        L_0x003a:
            android.util.LruCache<java.lang.String, com.sec.internal.ims.servicemodules.im.ImSession> r1 = r7.mImSessions     // Catch:{ all -> 0x00b9 }
            java.util.Map r1 = r1.snapshot()     // Catch:{ all -> 0x00b9 }
            java.util.Collection r1 = r1.values()     // Catch:{ all -> 0x00b9 }
            java.util.Iterator r1 = r1.iterator()     // Catch:{ all -> 0x00b9 }
        L_0x0048:
            boolean r2 = r1.hasNext()     // Catch:{ all -> 0x00b9 }
            if (r2 == 0) goto L_0x0098
            java.lang.Object r2 = r1.next()     // Catch:{ all -> 0x00b9 }
            com.sec.internal.ims.servicemodules.im.ImSession r2 = (com.sec.internal.ims.servicemodules.im.ImSession) r2     // Catch:{ all -> 0x00b9 }
            java.lang.String r3 = r2.getOwnImsi()     // Catch:{ all -> 0x00b9 }
            java.lang.String r4 = LOG_TAG     // Catch:{ all -> 0x00b9 }
            java.lang.StringBuilder r5 = new java.lang.StringBuilder     // Catch:{ all -> 0x00b9 }
            r5.<init>()     // Catch:{ all -> 0x00b9 }
            java.lang.String r6 = "chat Type "
            r5.append(r6)     // Catch:{ all -> 0x00b9 }
            com.sec.internal.constants.ims.servicemodules.im.ChatData$ChatType r6 = r2.getChatType()     // Catch:{ all -> 0x00b9 }
            r5.append(r6)     // Catch:{ all -> 0x00b9 }
            java.lang.String r6 = " imsi="
            r5.append(r6)     // Catch:{ all -> 0x00b9 }
            java.lang.String r6 = com.sec.internal.log.IMSLog.checker(r3)     // Catch:{ all -> 0x00b9 }
            r5.append(r6)     // Catch:{ all -> 0x00b9 }
            java.lang.String r5 = r5.toString()     // Catch:{ all -> 0x00b9 }
            android.util.Log.i(r4, r5)     // Catch:{ all -> 0x00b9 }
            com.sec.internal.constants.ims.servicemodules.im.ChatData$ChatType r4 = r2.getChatType()     // Catch:{ all -> 0x00b9 }
            if (r4 != r9) goto L_0x0048
            if (r3 == 0) goto L_0x0048
            boolean r3 = r3.equals(r10)     // Catch:{ all -> 0x00b9 }
            if (r3 == 0) goto L_0x0048
            java.util.Set r3 = r2.getParticipantsUri()     // Catch:{ all -> 0x00b9 }
            boolean r3 = r8.equals(r3)     // Catch:{ all -> 0x00b9 }
            if (r3 == 0) goto L_0x0048
            monitor-exit(r7)
            return r2
        L_0x0098:
            com.sec.internal.ims.servicemodules.im.ImPersister r1 = r7.mPersister     // Catch:{ all -> 0x00b9 }
            com.sec.internal.constants.ims.servicemodules.im.ChatData r8 = r1.querySessionByParticipants(r8, r9, r10, r11)     // Catch:{ all -> 0x00b9 }
            if (r8 != 0) goto L_0x00a9
            java.lang.String r8 = LOG_TAG     // Catch:{ all -> 0x00b9 }
            java.lang.String r9 = "getImSessionByParticipants: Couldn't load from db."
            android.util.Log.i(r8, r9)     // Catch:{ all -> 0x00b9 }
            monitor-exit(r7)
            return r0
        L_0x00a9:
            android.util.LruCache<java.lang.String, com.sec.internal.ims.servicemodules.im.ImSession> r9 = r7.mImSessions     // Catch:{ all -> 0x00b9 }
            java.lang.String r8 = r8.getChatId()     // Catch:{ all -> 0x00b9 }
            java.lang.Object r8 = r9.get(r8)     // Catch:{ all -> 0x00b9 }
            com.sec.internal.ims.servicemodules.im.ImSession r8 = (com.sec.internal.ims.servicemodules.im.ImSession) r8     // Catch:{ all -> 0x00b9 }
            monitor-exit(r7)
            return r8
        L_0x00b7:
            monitor-exit(r7)
            return r0
        L_0x00b9:
            r8 = move-exception
            monitor-exit(r7)
            throw r8
        */
        throw new UnsupportedOperationException("Method not decompiled: com.sec.internal.ims.servicemodules.im.ImCache.getImSessionByParticipants(java.util.Set, com.sec.internal.constants.ims.servicemodules.im.ChatData$ChatType, java.lang.String, com.sec.internal.constants.ims.servicemodules.im.ChatMode):com.sec.internal.ims.servicemodules.im.ImSession");
    }

    /* JADX WARNING: Code restructure failed: missing block: B:24:0x0067, code lost:
        return null;
     */
    /* Code decompiled incorrectly, please refer to instructions dump. */
    public synchronized java.util.Set<com.sec.internal.ims.servicemodules.im.ImSession> getAllImSessionByParticipants(java.util.Set<com.sec.ims.util.ImsUri> r5, com.sec.internal.constants.ims.servicemodules.im.ChatData.ChatType r6) {
        /*
            r4 = this;
            monitor-enter(r4)
            java.lang.String r0 = LOG_TAG     // Catch:{ all -> 0x0068 }
            java.lang.StringBuilder r1 = new java.lang.StringBuilder     // Catch:{ all -> 0x0068 }
            r1.<init>()     // Catch:{ all -> 0x0068 }
            java.lang.String r2 = "getAllImSessionByParticipants chatType= "
            r1.append(r2)     // Catch:{ all -> 0x0068 }
            r1.append(r6)     // Catch:{ all -> 0x0068 }
            java.lang.String r2 = " participants="
            r1.append(r2)     // Catch:{ all -> 0x0068 }
            java.lang.String r2 = com.sec.internal.log.IMSLog.numberChecker((java.util.Collection<com.sec.ims.util.ImsUri>) r5)     // Catch:{ all -> 0x0068 }
            r1.append(r2)     // Catch:{ all -> 0x0068 }
            java.lang.String r1 = r1.toString()     // Catch:{ all -> 0x0068 }
            android.util.Log.i(r0, r1)     // Catch:{ all -> 0x0068 }
            java.util.HashSet r1 = new java.util.HashSet     // Catch:{ all -> 0x0068 }
            r1.<init>()     // Catch:{ all -> 0x0068 }
            r2 = 0
            if (r5 == 0) goto L_0x0066
            boolean r3 = r5.isEmpty()     // Catch:{ all -> 0x0068 }
            if (r3 == 0) goto L_0x0032
            goto L_0x0066
        L_0x0032:
            com.sec.internal.ims.servicemodules.im.ImPersister r3 = r4.mPersister     // Catch:{ all -> 0x0068 }
            java.util.List r5 = r3.queryAllSessionByParticipant(r5, r6)     // Catch:{ all -> 0x0068 }
            if (r5 == 0) goto L_0x005f
            boolean r6 = r5.isEmpty()     // Catch:{ all -> 0x0068 }
            if (r6 == 0) goto L_0x0041
            goto L_0x005f
        L_0x0041:
            java.util.Iterator r5 = r5.iterator()     // Catch:{ all -> 0x0068 }
        L_0x0045:
            boolean r6 = r5.hasNext()     // Catch:{ all -> 0x0068 }
            if (r6 == 0) goto L_0x005d
            java.lang.Object r6 = r5.next()     // Catch:{ all -> 0x0068 }
            java.lang.String r6 = (java.lang.String) r6     // Catch:{ all -> 0x0068 }
            android.util.LruCache<java.lang.String, com.sec.internal.ims.servicemodules.im.ImSession> r0 = r4.mImSessions     // Catch:{ all -> 0x0068 }
            java.lang.Object r6 = r0.get(r6)     // Catch:{ all -> 0x0068 }
            com.sec.internal.ims.servicemodules.im.ImSession r6 = (com.sec.internal.ims.servicemodules.im.ImSession) r6     // Catch:{ all -> 0x0068 }
            r1.add(r6)     // Catch:{ all -> 0x0068 }
            goto L_0x0045
        L_0x005d:
            monitor-exit(r4)
            return r1
        L_0x005f:
            java.lang.String r5 = "getImSessionByParticipants: Couldn't load from db."
            android.util.Log.i(r0, r5)     // Catch:{ all -> 0x0068 }
            monitor-exit(r4)
            return r2
        L_0x0066:
            monitor-exit(r4)
            return r2
        L_0x0068:
            r5 = move-exception
            monitor-exit(r4)
            throw r5
        */
        throw new UnsupportedOperationException("Method not decompiled: com.sec.internal.ims.servicemodules.im.ImCache.getAllImSessionByParticipants(java.util.Set, com.sec.internal.constants.ims.servicemodules.im.ChatData$ChatType):java.util.Set");
    }

    /* JADX WARNING: Code restructure failed: missing block: B:27:0x006e, code lost:
        return null;
     */
    /* Code decompiled incorrectly, please refer to instructions dump. */
    public synchronized com.sec.internal.ims.servicemodules.im.FtMessage getFtMessageforFtRequest(java.lang.String r5, java.lang.String r6, long r7, java.lang.String r9) {
        /*
            r4 = this;
            monitor-enter(r4)
            java.lang.String r0 = LOG_TAG     // Catch:{ all -> 0x006f }
            java.lang.StringBuilder r1 = new java.lang.StringBuilder     // Catch:{ all -> 0x006f }
            r1.<init>()     // Catch:{ all -> 0x006f }
            java.lang.String r2 = "getFtMessageforFtRequest chatid:"
            r1.append(r2)     // Catch:{ all -> 0x006f }
            r1.append(r5)     // Catch:{ all -> 0x006f }
            java.lang.String r2 = " fileName:"
            r1.append(r2)     // Catch:{ all -> 0x006f }
            r1.append(r6)     // Catch:{ all -> 0x006f }
            java.lang.String r2 = " fileSize:"
            r1.append(r2)     // Catch:{ all -> 0x006f }
            r1.append(r7)     // Catch:{ all -> 0x006f }
            java.lang.String r2 = " fileTransferId:"
            r1.append(r2)     // Catch:{ all -> 0x006f }
            r1.append(r9)     // Catch:{ all -> 0x006f }
            java.lang.String r1 = r1.toString()     // Catch:{ all -> 0x006f }
            com.sec.internal.log.IMSLog.s(r0, r1)     // Catch:{ all -> 0x006f }
            r1 = 0
            if (r5 == 0) goto L_0x006d
            if (r6 == 0) goto L_0x006d
            r2 = 0
            int r2 = (r7 > r2 ? 1 : (r7 == r2 ? 0 : -1))
            if (r2 <= 0) goto L_0x006d
            if (r9 != 0) goto L_0x003d
            goto L_0x006d
        L_0x003d:
            android.util.LruCache<java.lang.String, com.sec.internal.ims.servicemodules.im.ImSession> r2 = r4.mImSessions     // Catch:{ all -> 0x006f }
            java.lang.Object r2 = r2.get(r5)     // Catch:{ all -> 0x006f }
            com.sec.internal.ims.servicemodules.im.ImSession r2 = (com.sec.internal.ims.servicemodules.im.ImSession) r2     // Catch:{ all -> 0x006f }
            if (r2 == 0) goto L_0x004c
            com.sec.internal.ims.servicemodules.im.FtMessage r6 = r2.findFtMessage(r6, r7, r9)     // Catch:{ all -> 0x006f }
            goto L_0x004d
        L_0x004c:
            r6 = r1
        L_0x004d:
            if (r6 != 0) goto L_0x006b
            java.lang.String r6 = "getFtMessageforFtRequest Couldn't find a FtMessage in ImSession."
            android.util.Log.i(r0, r6)     // Catch:{ all -> 0x006f }
            com.sec.internal.ims.servicemodules.im.ImPersister r6 = r4.mPersister     // Catch:{ all -> 0x006f }
            com.sec.internal.ims.servicemodules.im.FtMessage r5 = r6.queryFtMessageByFileTransferId(r9, r5)     // Catch:{ all -> 0x006f }
            if (r5 == 0) goto L_0x0064
            com.sec.internal.ims.servicemodules.im.MessageBase r5 = r4.loadExtras(r5)     // Catch:{ all -> 0x006f }
            com.sec.internal.ims.servicemodules.im.FtMessage r5 = (com.sec.internal.ims.servicemodules.im.FtMessage) r5     // Catch:{ all -> 0x006f }
            monitor-exit(r4)
            return r5
        L_0x0064:
            java.lang.String r5 = "getFtMessageforFtRequest Couldn't find a FtMessage by fileTransferId in db."
            android.util.Log.i(r0, r5)     // Catch:{ all -> 0x006f }
            monitor-exit(r4)
            return r1
        L_0x006b:
            monitor-exit(r4)
            return r6
        L_0x006d:
            monitor-exit(r4)
            return r1
        L_0x006f:
            r5 = move-exception
            monitor-exit(r4)
            throw r5
        */
        throw new UnsupportedOperationException("Method not decompiled: com.sec.internal.ims.servicemodules.im.ImCache.getFtMessageforFtRequest(java.lang.String, java.lang.String, long, java.lang.String):com.sec.internal.ims.servicemodules.im.FtMessage");
    }

    public synchronized Collection<ImSession> getAllImSessions() {
        return this.mImSessions.snapshot().values();
    }

    public Cursor querySessions(String[] strArr, String str, String[] strArr2, String str2) {
        return this.mPersister.querySessions(strArr, str, strArr2, str2);
    }

    public int cloudUpdateSession(String str, ContentValues contentValues) {
        return this.mPersister.cloudUpdateSession(str, contentValues);
    }

    public Cursor queryMessages(String[] strArr, String str, String[] strArr2, String str2) {
        return this.mPersister.queryMessages(strArr, str, strArr2, str2);
    }

    public Cursor queryChatMessagesForTapi(String[] strArr, String str, String[] strArr2, String str2) {
        return this.mPersister.queryChatMessagesForTapi(strArr, str, strArr2, str2);
    }

    public Cursor queryFtMessagesForTapi(String[] strArr, String str, String[] strArr2, String str2) {
        return this.mPersister.queryFtMessagesForTapi(strArr, str, strArr2, str2);
    }

    public Cursor queryParticipants(String[] strArr, String str, String[] strArr2, String str2) {
        return this.mPersister.queryParticipants(strArr, str, strArr2, str2);
    }

    public Cursor queryMessageNotification(String[] strArr, String str, String[] strArr2, String str2) {
        return this.mPersister.queryMessageNotification(strArr, str, strArr2, str2);
    }

    public Uri cloudInsertMessage(Uri uri, ContentValues contentValues) {
        return this.mPersister.cloudInsertMessage(uri, contentValues);
    }

    public synchronized int cloudDeleteMessage(String str) {
        String str2 = LOG_TAG;
        Log.i(str2, "cloud delete message: " + str);
        int parseInt = Integer.parseInt(str);
        MessageBase message = getMessage(parseInt);
        if (message != null) {
            if (message instanceof FtMessage) {
                handleDeleteFtMessage((FtMessage) message);
            }
            unregisterMessage(message);
        }
        this.mPersister.deleteMessage(parseInt);
        return 1;
    }

    public int cloudUpdateMessage(String str, ContentValues contentValues) {
        return this.mPersister.cloudUpdateMessage(str, contentValues);
    }

    public Uri cloudInsertNotification(Uri uri, ContentValues contentValues) {
        return this.mPersister.cloudInsertNotification(uri, contentValues);
    }

    public int cloudupdateNotification(String str, ContentValues contentValues, String str2, String[] strArr) {
        return this.mPersister.cloudUpdateNotification(str, contentValues, str2, strArr);
    }

    public Uri cloudInsertParticipant(Uri uri, ContentValues contentValues) {
        return this.mPersister.cloudInsertParticipant(uri, contentValues);
    }

    public int cloudDeleteParticipant(String str) {
        return this.mPersister.cloudDeleteParticipant(str);
    }

    public int cloudUpdateParticipant(String str, ContentValues contentValues) {
        return this.mPersister.cloudUpdateParticipant(str, contentValues);
    }

    /* JADX WARNING: Code restructure failed: missing block: B:55:0x014f, code lost:
        return 0;
     */
    /* Code decompiled incorrectly, please refer to instructions dump. */
    public synchronized int cloudsearchAndInsertSession(android.net.Uri r13, android.content.ContentValues r14, android.content.ContentValues[] r15) {
        /*
            r12 = this;
            monitor-enter(r12)
            java.lang.String r0 = LOG_TAG     // Catch:{ all -> 0x015f }
            java.lang.StringBuilder r1 = new java.lang.StringBuilder     // Catch:{ all -> 0x015f }
            r1.<init>()     // Catch:{ all -> 0x015f }
            java.lang.String r2 = "cloudsearchAndInsertSession: "
            r1.append(r2)     // Catch:{ all -> 0x015f }
            r1.append(r13)     // Catch:{ all -> 0x015f }
            java.lang.String r13 = r1.toString()     // Catch:{ all -> 0x015f }
            com.sec.internal.log.IMSLog.s(r0, r13)     // Catch:{ all -> 0x015f }
            r13 = 0
            if (r14 == 0) goto L_0x0152
            if (r15 != 0) goto L_0x001e
            goto L_0x0152
        L_0x001e:
            java.util.HashSet r0 = new java.util.HashSet     // Catch:{ NullPointerException -> 0x0159 }
            r0.<init>()     // Catch:{ NullPointerException -> 0x0159 }
            int r1 = r15.length     // Catch:{ NullPointerException -> 0x0159 }
            r2 = r13
        L_0x0025:
            if (r2 >= r1) goto L_0x003a
            r3 = r15[r2]     // Catch:{ NullPointerException -> 0x0159 }
            java.lang.String r4 = "uri"
            java.lang.String r3 = r3.getAsString(r4)     // Catch:{ NullPointerException -> 0x0159 }
            com.sec.ims.util.ImsUri r3 = com.sec.ims.util.ImsUri.parse(r3)     // Catch:{ NullPointerException -> 0x0159 }
            r0.add(r3)     // Catch:{ NullPointerException -> 0x0159 }
            int r2 = r2 + 1
            goto L_0x0025
        L_0x003a:
            com.sec.internal.constants.ims.servicemodules.im.ChatData r1 = r12.cloudSessionTranslation(r14)     // Catch:{ NullPointerException -> 0x0159 }
            com.sec.internal.ims.servicemodules.im.ImModule r2 = r12.mImModule     // Catch:{ NullPointerException -> 0x0159 }
            java.lang.String r3 = r1.getOwnIMSI()     // Catch:{ NullPointerException -> 0x0159 }
            int r2 = r2.getPhoneIdByIMSI(r3)     // Catch:{ NullPointerException -> 0x0159 }
            com.sec.internal.ims.servicemodules.im.ImModule r3 = r12.mImModule     // Catch:{ NullPointerException -> 0x0159 }
            com.sec.internal.ims.servicemodules.im.strategy.IMnoStrategy r3 = r3.getRcsStrategy(r2)     // Catch:{ NullPointerException -> 0x0159 }
            if (r3 == 0) goto L_0x005e
            com.sec.internal.ims.servicemodules.im.ImModule r3 = r12.mImModule     // Catch:{ NullPointerException -> 0x0159 }
            com.sec.internal.ims.servicemodules.im.strategy.IMnoStrategy r3 = r3.getRcsStrategy(r2)     // Catch:{ NullPointerException -> 0x0159 }
            java.lang.String r4 = "central_msg_store"
            boolean r3 = r3.boolSetting(r4)     // Catch:{ NullPointerException -> 0x0159 }
            if (r3 != 0) goto L_0x006a
        L_0x005e:
            com.sec.internal.ims.servicemodules.im.ImModule r3 = r12.mImModule     // Catch:{ NullPointerException -> 0x0159 }
            android.content.Context r3 = r3.getContext()     // Catch:{ NullPointerException -> 0x0159 }
            boolean r2 = com.sec.internal.ims.cmstore.utils.CmsUtil.isMcsSupported(r3, r2)     // Catch:{ NullPointerException -> 0x0159 }
            if (r2 == 0) goto L_0x0150
        L_0x006a:
            com.sec.internal.constants.ims.servicemodules.im.ChatData$ChatType r2 = r1.getChatType()     // Catch:{ NullPointerException -> 0x0159 }
            java.lang.String r3 = LOG_TAG     // Catch:{ NullPointerException -> 0x0159 }
            java.lang.StringBuilder r4 = new java.lang.StringBuilder     // Catch:{ NullPointerException -> 0x0159 }
            r4.<init>()     // Catch:{ NullPointerException -> 0x0159 }
            java.lang.String r5 = "chatType = "
            r4.append(r5)     // Catch:{ NullPointerException -> 0x0159 }
            java.lang.String r5 = r2.toString()     // Catch:{ NullPointerException -> 0x0159 }
            r4.append(r5)     // Catch:{ NullPointerException -> 0x0159 }
            java.lang.String r4 = r4.toString()     // Catch:{ NullPointerException -> 0x0159 }
            android.util.Log.i(r3, r4)     // Catch:{ NullPointerException -> 0x0159 }
            java.lang.String r3 = "conversation_id"
            java.lang.String r3 = r14.getAsString(r3)     // Catch:{ NullPointerException -> 0x0159 }
            java.lang.String r4 = "status"
            java.lang.Integer r4 = r14.getAsInteger(r4)     // Catch:{ NullPointerException -> 0x0159 }
            if (r4 == 0) goto L_0x00a3
            java.lang.String r4 = "status"
            java.lang.Integer r4 = r14.getAsInteger(r4)     // Catch:{ NullPointerException -> 0x0159 }
            int r4 = r4.intValue()     // Catch:{ NullPointerException -> 0x0159 }
            goto L_0x00a4
        L_0x00a3:
            r4 = r13
        L_0x00a4:
            java.lang.Integer r4 = java.lang.Integer.valueOf(r4)     // Catch:{ NullPointerException -> 0x0159 }
            java.lang.String r5 = "inserted_time_stamp"
            java.lang.String r5 = r14.getAsString(r5)     // Catch:{ NullPointerException -> 0x0159 }
            boolean r5 = android.text.TextUtils.isEmpty(r5)     // Catch:{ NullPointerException -> 0x0159 }
            r6 = 0
            if (r5 != 0) goto L_0x00c5
            java.lang.String r5 = "inserted_time_stamp"
            java.lang.String r14 = r14.getAsString(r5)     // Catch:{ NullPointerException -> 0x0159 }
            java.lang.Long r14 = java.lang.Long.valueOf(r14)     // Catch:{ NullPointerException -> 0x0159 }
            long r8 = r14.longValue()     // Catch:{ NullPointerException -> 0x0159 }
            goto L_0x00c6
        L_0x00c5:
            r8 = r6
        L_0x00c6:
            com.sec.internal.constants.ims.servicemodules.im.ChatData$ChatType r14 = com.sec.internal.constants.ims.servicemodules.im.ChatData.ChatType.REGULAR_GROUP_CHAT     // Catch:{ NullPointerException -> 0x0159 }
            r5 = 1
            if (r14 != r2) goto L_0x00d6
            com.sec.internal.ims.servicemodules.im.ImPersister r14 = r12.mPersister     // Catch:{ NullPointerException -> 0x0159 }
            java.lang.String r0 = r1.getOwnIMSI()     // Catch:{ NullPointerException -> 0x0159 }
            com.sec.internal.constants.ims.servicemodules.im.ChatData r14 = r14.querySessionByConversationId(r0, r3, r5)     // Catch:{ NullPointerException -> 0x0159 }
            goto L_0x00e4
        L_0x00d6:
            com.sec.internal.ims.servicemodules.im.ImPersister r14 = r12.mPersister     // Catch:{ NullPointerException -> 0x0159 }
            java.lang.String r10 = r1.getOwnIMSI()     // Catch:{ NullPointerException -> 0x0159 }
            com.sec.internal.constants.ims.servicemodules.im.ChatMode r11 = r1.getChatMode()     // Catch:{ NullPointerException -> 0x0159 }
            com.sec.internal.constants.ims.servicemodules.im.ChatData r14 = r14.querySessionByParticipants(r0, r2, r10, r11)     // Catch:{ NullPointerException -> 0x0159 }
        L_0x00e4:
            if (r14 == 0) goto L_0x0100
            if (r3 == 0) goto L_0x00fa
            java.lang.String r15 = r14.getConversationId()     // Catch:{ NullPointerException -> 0x0159 }
            boolean r15 = r3.equals(r15)     // Catch:{ NullPointerException -> 0x0159 }
            if (r15 == 0) goto L_0x00fa
            r14.setConversationId(r3)     // Catch:{ NullPointerException -> 0x0159 }
            com.sec.internal.ims.servicemodules.im.ImPersister r15 = r12.mPersister     // Catch:{ NullPointerException -> 0x0159 }
            r15.onSessionUpdated(r14)     // Catch:{ NullPointerException -> 0x0159 }
        L_0x00fa:
            int r13 = r14.getId()     // Catch:{ NullPointerException -> 0x0159 }
            monitor-exit(r12)
            return r13
        L_0x0100:
            java.util.ArrayList r14 = new java.util.ArrayList     // Catch:{ NullPointerException -> 0x0159 }
            r14.<init>()     // Catch:{ NullPointerException -> 0x0159 }
            int r0 = r15.length     // Catch:{ NullPointerException -> 0x0159 }
            r2 = r13
        L_0x0107:
            if (r2 >= r0) goto L_0x0115
            r3 = r15[r2]     // Catch:{ NullPointerException -> 0x0159 }
            com.sec.internal.constants.ims.servicemodules.im.ImParticipant r3 = r12.cloudParticipantTranslation(r3)     // Catch:{ NullPointerException -> 0x0159 }
            r14.add(r3)     // Catch:{ NullPointerException -> 0x0159 }
            int r2 = r2 + 1
            goto L_0x0107
        L_0x0115:
            com.sec.internal.ims.servicemodules.im.ImPersister r15 = r12.mPersister     // Catch:{ NullPointerException -> 0x0159 }
            r15.insertParticipant((java.util.Collection<com.sec.internal.constants.ims.servicemodules.im.ImParticipant>) r14)     // Catch:{ NullPointerException -> 0x0159 }
            java.util.Iterator r14 = r14.iterator()     // Catch:{ NullPointerException -> 0x0159 }
        L_0x011e:
            boolean r15 = r14.hasNext()     // Catch:{ NullPointerException -> 0x0159 }
            if (r15 == 0) goto L_0x0131
            java.lang.Object r15 = r14.next()     // Catch:{ NullPointerException -> 0x0159 }
            com.sec.internal.constants.ims.servicemodules.im.ImParticipant r15 = (com.sec.internal.constants.ims.servicemodules.im.ImParticipant) r15     // Catch:{ NullPointerException -> 0x0159 }
            int r15 = r15.getId()     // Catch:{ NullPointerException -> 0x0159 }
            if (r15 > 0) goto L_0x011e
            r5 = r13
        L_0x0131:
            if (r5 == 0) goto L_0x014e
            if (r4 == 0) goto L_0x014e
            int r14 = (r8 > r6 ? 1 : (r8 == r6 ? 0 : -1))
            if (r14 <= 0) goto L_0x013c
            r1.setInsertedTimeStamp(r8)     // Catch:{ NullPointerException -> 0x0159 }
        L_0x013c:
            int r14 = r4.intValue()     // Catch:{ NullPointerException -> 0x0159 }
            r1.setState(r14)     // Catch:{ NullPointerException -> 0x0159 }
            com.sec.internal.ims.servicemodules.im.ImPersister r14 = r12.mPersister     // Catch:{ NullPointerException -> 0x0159 }
            r14.insertSession(r1)     // Catch:{ NullPointerException -> 0x0159 }
            int r13 = r1.getId()     // Catch:{ NullPointerException -> 0x0159 }
            monitor-exit(r12)
            return r13
        L_0x014e:
            monitor-exit(r12)
            return r13
        L_0x0150:
            monitor-exit(r12)
            return r13
        L_0x0152:
            java.lang.String r14 = "cloudsearchAndInsertSession: no values inserted"
            android.util.Log.i(r0, r14)     // Catch:{ NullPointerException -> 0x0159 }
            monitor-exit(r12)
            return r13
        L_0x0159:
            r14 = move-exception
            r14.printStackTrace()     // Catch:{ all -> 0x015f }
            monitor-exit(r12)
            return r13
        L_0x015f:
            r13 = move-exception
            monitor-exit(r12)
            throw r13
        */
        throw new UnsupportedOperationException("Method not decompiled: com.sec.internal.ims.servicemodules.im.ImCache.cloudsearchAndInsertSession(android.net.Uri, android.content.ContentValues, android.content.ContentValues[]):int");
    }

    private ChatData cloudSessionTranslation(ContentValues contentValues) {
        ContentValues contentValues2 = contentValues;
        String asString = contentValues2.getAsString("chat_id");
        String asString2 = contentValues2.getAsString(ImContract.ImSession.OWN_PHONE_NUMBER);
        String asString3 = contentValues2.getAsString("session_uri");
        Integer asInteger = contentValues2.getAsInteger("direction");
        if (asInteger == null) {
            asInteger = Integer.valueOf(ImDirection.INCOMING.getId());
        }
        String asString4 = contentValues2.getAsString("conversation_id");
        String asString5 = contentValues2.getAsString("contribution_id");
        boolean z = (contentValues2.getAsInteger("is_group_chat") == null || contentValues2.getAsInteger("is_group_chat").intValue() == 0) ? false : true;
        String asString6 = contentValues2.getAsString("subject");
        Integer asInteger2 = contentValues2.getAsInteger(ImContract.ImSession.CHAT_TYPE);
        if (asInteger2 == null) {
            asInteger2 = Integer.valueOf((z ? ChatData.ChatType.PARTICIPANT_BASED_GROUP_CHAT : ChatData.ChatType.ONE_TO_ONE_CHAT).getId());
        }
        Integer asInteger3 = contentValues2.getAsInteger(ImContract.ImSession.CHAT_MODE);
        String str = LOG_TAG;
        Log.i(str, "set own sim imsi: " + contentValues2.getAsString("sim_imsi"));
        if (asInteger3 == null) {
            asInteger3 = Integer.valueOf(ChatMode.OFF.getId());
        }
        ImsUri parse = !TextUtils.isEmpty(asString3) ? ImsUri.parse(asString3) : null;
        String asString7 = contentValues2.getAsString("created_by");
        String asString8 = contentValues2.getAsString("invited_by");
        return new ChatData(asString, asString2, "", asString6, ChatData.ChatType.fromId(asInteger2.intValue()), ImDirection.fromId(asInteger.intValue()), asString4, asString5, contentValues2.getAsString("sim_imsi"), (String) null, ChatMode.fromId(asInteger3.intValue()), parse, !TextUtils.isEmpty(asString7) ? ImsUri.parse(asString7) : null, !TextUtils.isEmpty(asString8) ? ImsUri.parse(asString8) : null);
    }

    private ImParticipant cloudParticipantTranslation(ContentValues contentValues) {
        return new ImParticipant(contentValues.getAsString("chat_id"), ImParticipant.Status.fromId(Integer.valueOf(contentValues.getAsInteger("status") != null ? contentValues.getAsInteger("status").intValue() : 0).intValue()), ImsUri.parse(contentValues.getAsString("uri")));
    }

    public synchronized MessageBase getMessage(int i) {
        MessageBase messageBase = this.mPendingMessages.get(i);
        if (messageBase != null) {
            return messageBase;
        }
        MessageBase queryMessage = this.mPersister.queryMessage(String.valueOf(i));
        if (queryMessage == null) {
            return null;
        }
        return loadExtras(queryMessage);
    }

    public synchronized List<MessageBase> getMessages(Collection<String> collection) {
        ArrayList arrayList;
        arrayList = new ArrayList();
        ArrayList arrayList2 = new ArrayList();
        for (String next : collection) {
            try {
                MessageBase messageBase = this.mPendingMessages.get(Integer.valueOf(next).intValue());
                if (messageBase != null) {
                    arrayList.add(messageBase);
                } else {
                    arrayList2.add(next);
                }
            } catch (NumberFormatException e) {
                e.printStackTrace();
            }
        }
        if (!arrayList2.isEmpty()) {
            for (MessageBase next2 : this.mPersister.queryMessages((Collection<String>) arrayList2)) {
                loadExtras(next2);
                arrayList.add(next2);
            }
        }
        return arrayList;
    }

    public synchronized List<MessageBase> getMessagesUsingChatId(List<String> list) {
        ArrayList arrayList;
        arrayList = new ArrayList();
        for (String next : list) {
            ArrayList arrayList2 = new ArrayList();
            List<MessageBase> all = this.mPendingMessages.getAll(next);
            if (all.size() > 0) {
                for (MessageBase next2 : all) {
                    arrayList2.add(String.valueOf(next2.getId()));
                    arrayList.add(next2);
                }
                List<MessageBase> queryMessagesUsingChatIDExceptPending = this.mPersister.queryMessagesUsingChatIDExceptPending(next, arrayList2);
                if (queryMessagesUsingChatIDExceptPending != null) {
                    for (MessageBase next3 : queryMessagesUsingChatIDExceptPending) {
                        loadExtras(next3);
                        arrayList.add(next3);
                    }
                }
            } else {
                List<MessageBase> queryMessagesUsingChatID = this.mPersister.queryMessagesUsingChatID(next);
                if (queryMessagesUsingChatID != null) {
                    for (MessageBase next4 : queryMessagesUsingChatID) {
                        loadExtras(next4);
                        arrayList.add(next4);
                    }
                }
            }
        }
        return arrayList;
    }

    public synchronized List<MessageBase> getMessages(Collection<String> collection, ImDirection imDirection, String str) {
        ArrayList arrayList;
        arrayList = new ArrayList();
        ArrayList arrayList2 = new ArrayList();
        for (String next : collection) {
            MessageBase messageBase = this.mPendingMessages.get(next, imDirection, str);
            if (messageBase != null) {
                arrayList.add(messageBase);
            } else {
                arrayList2.add(next);
            }
        }
        if (!arrayList2.isEmpty()) {
            for (MessageBase next2 : this.mPersister.queryMessages(arrayList2, imDirection, str)) {
                loadExtras(next2);
                arrayList.add(next2);
            }
        }
        return arrayList;
    }

    public MessageBase getPendingMessage(int i) {
        return this.mPendingMessages.get(i);
    }

    public List<MessageBase> getAllPendingMessages(String str) {
        return this.mPendingMessages.getAll(str);
    }

    public ImMessage getImMessage(int i) {
        MessageBase message = getMessage(i);
        if (message instanceof ImMessage) {
            return (ImMessage) message;
        }
        return null;
    }

    public ImMessage getImMessage(String str, ImDirection imDirection, String str2) {
        MessageBase message = getMessage(str, imDirection, str2);
        if (message instanceof ImMessage) {
            return (ImMessage) message;
        }
        return null;
    }

    public FtMessage getFtMessage(int i) {
        MessageBase message = getMessage(i);
        if (message instanceof FtMessage) {
            return (FtMessage) message;
        }
        return null;
    }

    public FtMessage getFtMessage(String str, ImDirection imDirection, String str2) {
        MessageBase message = getMessage(str, imDirection, str2);
        if (message instanceof FtMessage) {
            return (FtMessage) message;
        }
        return null;
    }

    public FtMsrpMessage getFtMsrpMessage(Object obj) {
        if (obj == null) {
            return null;
        }
        for (MessageBase next : this.mPendingMessages.getAll()) {
            if (next instanceof FtMsrpMessage) {
                FtMsrpMessage ftMsrpMessage = (FtMsrpMessage) next;
                if (obj.equals(ftMsrpMessage.getRawHandle())) {
                    return ftMsrpMessage;
                }
            }
        }
        return null;
    }

    public synchronized List<MessageBase> getMessagesForPendingNotificationByChatId(String str) {
        ArrayList arrayList;
        List<Integer> queryMessageIdsForPendingNotification = this.mPersister.queryMessageIdsForPendingNotification(str);
        String str2 = LOG_TAG;
        Log.i(str2, "pending notifications count:" + queryMessageIdsForPendingNotification.size());
        arrayList = new ArrayList();
        ArrayList arrayList2 = new ArrayList();
        for (Integer intValue : queryMessageIdsForPendingNotification) {
            int intValue2 = intValue.intValue();
            if (this.mPendingMessages.containsKey(intValue2)) {
                arrayList.add(this.mPendingMessages.get(intValue2));
            } else {
                arrayList2.add(String.valueOf(intValue2));
            }
        }
        for (MessageBase next : this.mPersister.queryMessages((Collection<String>) arrayList2)) {
            loadExtras(next);
            arrayList.add(next);
        }
        return arrayList;
    }

    public synchronized MessageBase getMessage(String str, ImDirection imDirection, String str2) {
        if (TextUtils.isEmpty(str)) {
            return null;
        }
        MessageBase messageBase = this.mPendingMessages.get(str, imDirection, str2);
        if (messageBase != null) {
            return messageBase;
        }
        return this.mCachingMessages.get(new MessageKey(str, imDirection, str2));
    }

    /* access modifiers changed from: private */
    /* JADX WARNING: Removed duplicated region for block: B:11:0x003d  */
    /* Code decompiled incorrectly, please refer to instructions dump. */
    public synchronized com.sec.internal.ims.servicemodules.im.MessageBase loadExtras(com.sec.internal.ims.servicemodules.im.MessageBase r4) {
        /*
            r3 = this;
            monitor-enter(r3)
            com.sec.internal.ims.servicemodules.im.ImPersister r0 = r3.mPersister     // Catch:{ all -> 0x0066 }
            java.util.List r0 = r0.queryImImdnRecRoute(r4)     // Catch:{ all -> 0x0066 }
            com.sec.internal.ims.servicemodules.im.ImModule r1 = r3.mImModule     // Catch:{ all -> 0x0066 }
            java.lang.String r2 = r4.getOwnIMSI()     // Catch:{ all -> 0x0066 }
            int r1 = r1.getPhoneIdByIMSI(r2)     // Catch:{ all -> 0x0066 }
            r4.setImdnRecRouteList(r0)     // Catch:{ all -> 0x0066 }
            boolean r0 = r4 instanceof com.sec.internal.ims.servicemodules.im.FtHttpIncomingMessage     // Catch:{ all -> 0x0066 }
            if (r0 != 0) goto L_0x0028
            boolean r0 = r4 instanceof com.sec.internal.ims.servicemodules.im.FtHttpOutgoingMessage     // Catch:{ all -> 0x0066 }
            if (r0 == 0) goto L_0x001d
            goto L_0x0028
        L_0x001d:
            com.sec.internal.ims.servicemodules.im.ImModule r0 = r3.mImModule     // Catch:{ all -> 0x0066 }
            r2 = 0
            android.net.Network r0 = r0.getNetwork(r2, r1)     // Catch:{ all -> 0x0066 }
            r4.setNetwork(r0)     // Catch:{ all -> 0x0066 }
            goto L_0x0039
        L_0x0028:
            com.sec.internal.ims.servicemodules.im.ImModule r0 = r3.mImModule     // Catch:{ all -> 0x0066 }
            com.sec.internal.ims.servicemodules.im.ImConfig r2 = r0.getImConfig(r1)     // Catch:{ all -> 0x0066 }
            boolean r2 = r2.isFtHttpOverDefaultPdn()     // Catch:{ all -> 0x0066 }
            android.net.Network r0 = r0.getNetwork(r2, r1)     // Catch:{ all -> 0x0066 }
            r4.setNetwork(r0)     // Catch:{ all -> 0x0066 }
        L_0x0039:
            boolean r0 = r4 instanceof com.sec.internal.ims.servicemodules.im.FtMessage     // Catch:{ all -> 0x0066 }
            if (r0 == 0) goto L_0x0061
            r0 = r4
            com.sec.internal.ims.servicemodules.im.FtMessage r0 = (com.sec.internal.ims.servicemodules.im.FtMessage) r0     // Catch:{ all -> 0x0066 }
            com.sec.internal.ims.servicemodules.im.ImPersister r1 = r3.mPersister     // Catch:{ all -> 0x0066 }
            java.lang.String r2 = r0.getChatId()     // Catch:{ all -> 0x0066 }
            com.sec.internal.constants.ims.servicemodules.im.ChatData r1 = r1.querySessionByChatId(r2)     // Catch:{ all -> 0x0066 }
            if (r1 == 0) goto L_0x0061
            boolean r2 = r1.isGroupChat()     // Catch:{ all -> 0x0066 }
            r0.setIsGroupChat(r2)     // Catch:{ all -> 0x0066 }
            java.lang.String r2 = r1.getContributionId()     // Catch:{ all -> 0x0066 }
            r0.setContributionId(r2)     // Catch:{ all -> 0x0066 }
            java.lang.String r1 = r1.getConversationId()     // Catch:{ all -> 0x0066 }
            r0.setConversationId(r1)     // Catch:{ all -> 0x0066 }
        L_0x0061:
            r3.registerObserver(r4)     // Catch:{ all -> 0x0066 }
            monitor-exit(r3)
            return r4
        L_0x0066:
            r4 = move-exception
            monitor-exit(r3)
            throw r4
        */
        throw new UnsupportedOperationException("Method not decompiled: com.sec.internal.ims.servicemodules.im.ImCache.loadExtras(com.sec.internal.ims.servicemodules.im.MessageBase):com.sec.internal.ims.servicemodules.im.MessageBase");
    }

    public List<String> getMessageIdsForDisplayAggregation(String str, ImDirection imDirection, Long l) {
        List<Integer> queryMessageIdsForDisplayAggregation = this.mPersister.queryMessageIdsForDisplayAggregation(str, imDirection, l);
        ArrayList arrayList = new ArrayList();
        for (Integer intValue : queryMessageIdsForDisplayAggregation) {
            arrayList.add(String.valueOf(intValue.intValue()));
        }
        String str2 = LOG_TAG;
        Log.i(str2, "getMessageIdsForDisplayAggregation: list=" + arrayList);
        return arrayList;
    }

    public Set<ImParticipant> getParticipants(String str) {
        ImSession imSession = getImSession(str);
        if (imSession != null) {
            return imSession.getParticipants();
        }
        return null;
    }

    public NotificationStatus getNotificationStatus(String str, ImsUri imsUri) {
        if (str == null || imsUri == null) {
            return null;
        }
        return this.mPersister.queryNotificationStatus(str, imsUri);
    }

    public synchronized MessageBase queryMessageForOpenApi(String str) {
        return this.mPersister.queryMessage(str);
    }

    public synchronized ImSession makeNewOutgoingSession(String str, Set<ImsUri> set, ChatData.ChatType chatType, String str2, String str3, int i, String str4, String str5, ChatMode chatMode) {
        try {
        } catch (Throwable th) {
            throw th;
        }
        return makeNewOutgoingSession(str, set, chatType, str2, str3, i, str4, str5, chatMode, (String) null, (String) null, (ImsUri) null);
    }

    public synchronized ImSession makeNewOutgoingSession(String str, Set<ImsUri> set, ChatData.ChatType chatType, String str2, String str3, int i, String str4, String str5, ChatMode chatMode, String str6, String str7, ImsUri imsUri) {
        String str8;
        String str9;
        ImSession build;
        String str10 = str;
        Set<ImsUri> set2 = set;
        ChatData.ChatType chatType2 = chatType;
        synchronized (this) {
            String str11 = LOG_TAG;
            IMSLog.i(str11, "makeNewOutgoingSession: chatType=" + chatType + " participants=" + IMSLog.numberChecker((Collection<ImsUri>) set) + " imsi= " + IMSLog.numberChecker(str));
            int phoneIdByIMSI = this.mImModule.getPhoneIdByIMSI(str);
            if (!TextUtils.isEmpty(str6) || !TextUtils.isEmpty(str7)) {
                str9 = str6;
                str8 = str7;
            } else if (this.mImModule.getImConfig(phoneIdByIMSI).getImMsgTech() == ImConstants.ImMsgTech.CPM) {
                str9 = StringIdGenerator.generateConversationId();
                str8 = ChatData.ChatType.isGroupChat(chatType) ? str9 : StringIdGenerator.generateContributionId();
            } else {
                str8 = StringIdGenerator.generateContributionId();
                str9 = str6;
            }
            String ownPhoneNum = this.mImModule.getOwnPhoneNum(phoneIdByIMSI);
            ImsUri normalizedUri = (!ChatData.ChatType.isGroupChat(chatType) || TextUtils.isEmpty(ownPhoneNum)) ? null : this.mImModule.getUriGenerator(phoneIdByIMSI).getNormalizedUri(ownPhoneNum, true);
            build = new ImSessionBuilder().looper(this.mImModule.getLooper()).listener(this.mImModule.getImSessionProcessor()).config(this.mImModule.getImConfig(phoneIdByIMSI)).imsService(getImHandler()).slmService(ImsRegistry.getHandlerFactory().getSlmHandler()).uriGenerator(this.mImModule.getUriGenerator(phoneIdByIMSI)).chatId(StringIdGenerator.generateChatId(set, str, ChatData.ChatType.isGroupChat(chatType), chatMode.getId())).participantsUri(set).chatType(chatType).chatMode(chatMode).ownPhoneNum(ownPhoneNum).ownSimIMSI(str).ownGroupAlias("").subject(str2).iconPath(str5).sdpContentType(str3).threadId(i).requestMessageId(str4).contributionId(str8).conversationId(str9).direction(ImDirection.OUTGOING).getter(this.mImModule).sessionUri(imsUri).createdBy(normalizedUri).invitedBy(normalizedUri).build();
            registerSession(build);
            registerParticipant(build.getParticipants());
            this.mCmStoreInvoker.onCreateSession(phoneIdByIMSI, build);
        }
        return build;
    }

    public synchronized ImSession makeNewIncomingSession(ImIncomingSessionEvent imIncomingSessionEvent, Set<ImsUri> set, ChatData.ChatType chatType, ChatMode chatMode) {
        ImSession build;
        String str = LOG_TAG;
        Log.i(str, "makeNewIncomingSession: chatType=" + chatType + " participants=" + IMSLog.numberChecker((Collection<ImsUri>) set));
        int phoneIdByIMSI = this.mImModule.getPhoneIdByIMSI(imIncomingSessionEvent.mOwnImsi);
        build = new ImSessionBuilder().looper(this.mImModule.getLooper()).listener(this.mImModule.getImSessionProcessor()).config(this.mImModule.getImConfig(phoneIdByIMSI)).imsService(getImHandler()).slmService(ImsRegistry.getHandlerFactory().getSlmHandler()).uriGenerator(this.mImModule.getUriGenerator()).chatId(StringIdGenerator.generateChatId(set, imIncomingSessionEvent.mOwnImsi, ChatData.ChatType.isGroupChat(chatType), chatMode.getId())).participantsUri(set).chatType(chatType).chatMode(chatMode).ownPhoneNum(this.mImModule.getOwnPhoneNum(phoneIdByIMSI)).ownSimIMSI(imIncomingSessionEvent.mOwnImsi).ownGroupAlias("").subject(imIncomingSessionEvent.mSubject).contributionId(imIncomingSessionEvent.mContributionId).conversationId(imIncomingSessionEvent.mConversationId).sdpContentType(imIncomingSessionEvent.mSdpContentType).direction(ImDirection.INCOMING).rawHandle(imIncomingSessionEvent.mIsDeferred ? null : imIncomingSessionEvent.mRawHandle).sessionType(imIncomingSessionEvent.mSessionType).createdBy(imIncomingSessionEvent.mCreatedBy).invitedBy(imIncomingSessionEvent.mInvitedBy).getter(this.mImModule).build();
        registerSession(build);
        registerParticipant(build.getParticipants());
        this.mCmStoreInvoker.onCreateSession(phoneIdByIMSI, build);
        return build;
    }

    public ImSession makeNewEmptySession(String str, Set<ImsUri> set, ChatData.ChatType chatType, ImDirection imDirection) {
        return makeNewEmptySession(str, set, chatType, imDirection, ChatMode.OFF);
    }

    public synchronized ImSession makeNewEmptySession(String str, Set<ImsUri> set, ChatData.ChatType chatType, ImDirection imDirection, ChatMode chatMode) {
        String str2;
        String str3;
        ImSession build;
        String str4 = LOG_TAG;
        Log.i(str4, "makeNewEmptySession: chatType=" + chatType + " participants=" + IMSLog.numberChecker((Collection<ImsUri>) set) + " ownImsi= " + IMSLog.numberChecker(str));
        int phoneIdByIMSI = this.mImModule.getPhoneIdByIMSI(str);
        if (this.mImModule.getImConfig(phoneIdByIMSI).getImMsgTech() == ImConstants.ImMsgTech.CPM) {
            str3 = StringIdGenerator.generateConversationId();
            str2 = ChatData.ChatType.isGroupChat(chatType) ? str3 : StringIdGenerator.generateContributionId();
        } else {
            str2 = StringIdGenerator.generateContributionId();
            str3 = null;
        }
        build = new ImSessionBuilder().looper(this.mImModule.getLooper()).listener(this.mImModule.getImSessionProcessor()).config(this.mImModule.getImConfig(phoneIdByIMSI)).imsService(getImHandler()).slmService(ImsRegistry.getHandlerFactory().getSlmHandler()).uriGenerator(this.mImModule.getUriGenerator(phoneIdByIMSI)).chatId(StringIdGenerator.generateChatId(set, str, ChatData.ChatType.isGroupChat(chatType), chatMode.getId())).participantsUri(set).chatType(chatType).chatMode(chatMode).ownSimIMSI(str).ownPhoneNum(this.mImModule.getOwnPhoneNum(phoneIdByIMSI)).contributionId(str2).conversationId(str3).direction(imDirection).getter(this.mImModule).build();
        registerSession(build);
        registerParticipant(build.getParticipants());
        this.mCmStoreInvoker.onCreateSession(phoneIdByIMSI, build);
        return build;
    }

    public synchronized ImMessage makeNewOutgoingMessage(String str, ImSession imSession, String str2, Set<NotificationStatus> set, String str3, String str4, boolean z, boolean z2, boolean z3, boolean z4, boolean z5, String str5) {
        try {
        } catch (Throwable th) {
            throw th;
        }
        return makeNewOutgoingMessage(str, imSession, str2, set, str3, str4, z, z2, z3, z4, z5, str5, (String) null, (String) null, (String) null);
    }

    public synchronized ImMessage makeNewOutgoingMessage(String str, ImSession imSession, String str2, Set<NotificationStatus> set, String str3, String str4, boolean z, boolean z2, boolean z3, boolean z4, boolean z5, String str5, String str6, String str7, String str8) {
        ImConstants.Type type;
        ImMessage build;
        String str9 = str;
        synchronized (this) {
            if (z2) {
                try {
                    type = ImConstants.Type.TEXT_PUBLICACCOUNT;
                } catch (Throwable th) {
                    throw th;
                }
            } else if (z4) {
                type = ImConstants.Type.LOCATION;
            } else {
                type = MessageBase.getType(str3);
            }
            int i = z5 ? 2 : 0;
            int phoneIdByIMSI = this.mImModule.getPhoneIdByIMSI(str);
            String str10 = str2;
            Set<NotificationStatus> set2 = set;
            String str11 = str3;
            String str12 = str4;
            boolean z6 = z;
            build = ((ImMessage.Builder) ((ImMessage.Builder) ((ImMessage.Builder) ((ImMessage.Builder) ((ImMessage.Builder) ((ImMessage.Builder) ((ImMessage.Builder) ((ImMessage.Builder) ((ImMessage.Builder) ((ImMessage.Builder) ((ImMessage.Builder) ((ImMessage.Builder) ((ImMessage.Builder) ((ImMessage.Builder) ((ImMessage.Builder) ((ImMessage.Builder) ((ImMessage.Builder) ((ImMessage.Builder) ((ImMessage.Builder) ((ImMessage.Builder) ((ImMessage.Builder) ((ImMessage.Builder) ((ImMessage.Builder) ((ImMessage.Builder) ((ImMessage.Builder) ((ImMessage.Builder) ((ImMessage.Builder) ((ImMessage.Builder) ImMessage.builder().module(this.mImModule)).listener(this.mImModule.getImProcessor()).config(this.mImModule.getImConfig(phoneIdByIMSI))).uriGenerator(this.mImModule.getUriGenerator(phoneIdByIMSI))).imsService(getImHandler())).slmService(ImsRegistry.getHandlerFactory().getSlmHandler())).chatId(imSession.getChatId())).remoteUri(imSession.isGroupChat() ? null : ImsUri.parse(imSession.getParticipantsString().get(0)))).body(str2)).userAlias(this.mImModule.getUserAlias(phoneIdByIMSI, false))).imdnId(StringIdGenerator.generateImdn())).dispNotification(set)).contentType(str3)).direction(ImDirection.OUTGOING)).status(ImConstants.Status.TO_SEND)).type(type)).notDisplayedCounter(imSession.getParticipantsSize())).requestMessageId(str4)).insertedTimestamp(System.currentTimeMillis())).isSlmSvcMsg(z)).isBroadcastMsg(z3)).mnoStrategy(this.mImModule.getRcsStrategy(phoneIdByIMSI))).simIMSI(str)).maapTrafficType(str5)).messagingTech(ImConstants.MessagingTech.NORMAL)).flagMask(i)).referenceImdnId(str6)).referenceType(str7)).referenceValue(str8)).build();
            registerMessage(build);
            addToPendingList(build);
        }
        return build;
    }

    public synchronized ImMessage makeNewIncomingMessage(String str, ImSession imSession, ImIncomingMessageEvent imIncomingMessageEvent, Network network, String str2) {
        ImMessage build;
        int phoneIdByIMSI = this.mImModule.getPhoneIdByIMSI(str);
        ImDirection extractImDirection = ImCpimNamespacesHelper.extractImDirection(phoneIdByIMSI, imIncomingMessageEvent.mCpimNamespaces);
        String extractMaapTrafficType = ImCpimNamespacesHelper.extractMaapTrafficType(imIncomingMessageEvent.mCpimNamespaces);
        String extractRcsReferenceId = ImCpimNamespacesHelper.extractRcsReferenceId(imIncomingMessageEvent.mCpimNamespaces);
        String extractRcsReferenceType = ImCpimNamespacesHelper.extractRcsReferenceType(imIncomingMessageEvent.mCpimNamespaces);
        String extractRcsReferenceValue = ImCpimNamespacesHelper.extractRcsReferenceValue(imIncomingMessageEvent.mCpimNamespaces);
        String extractRcsTrafficType = ImCpimNamespacesHelper.extractRcsTrafficType(imIncomingMessageEvent.mCpimNamespaces);
        ImMessage.Builder builder = (ImMessage.Builder) ((ImMessage.Builder) ((ImMessage.Builder) ((ImMessage.Builder) ((ImMessage.Builder) ((ImMessage.Builder) ((ImMessage.Builder) ((ImMessage.Builder) ((ImMessage.Builder) ((ImMessage.Builder) ((ImMessage.Builder) ((ImMessage.Builder) ((ImMessage.Builder) ((ImMessage.Builder) ((ImMessage.Builder) ((ImMessage.Builder) ((ImMessage.Builder) ((ImMessage.Builder) ImMessage.builder().module(this.mImModule)).imsService(getImHandler())).slmService(ImsRegistry.getHandlerFactory().getSlmHandler())).listener(this.mImModule.getImProcessor()).config(this.mImModule.getImConfig(phoneIdByIMSI))).uriGenerator(this.mImModule.getUriGenerator(phoneIdByIMSI))).chatId(imSession.getChatId())).body(imIncomingMessageEvent.mBody)).suggestion(str2)).remoteUri(this.mImModule.normalizeUri(phoneIdByIMSI, imIncomingMessageEvent.mSender))).userAlias(imIncomingMessageEvent.mUserAlias)).imdnId(imIncomingMessageEvent.mImdnMessageId)).imdnIdOriginalTo(imIncomingMessageEvent.mOriginalToHdr)).direction(extractImDirection)).type(MessageBase.getType(imIncomingMessageEvent.mContentType))).contentType(imIncomingMessageEvent.mContentType)).status(ImConstants.Status.UNREAD)).dispNotification(imIncomingMessageEvent.mDispositionNotification)).insertedTimestamp(System.currentTimeMillis());
        Date date = imIncomingMessageEvent.mImdnTime;
        build = ((ImMessage.Builder) ((ImMessage.Builder) ((ImMessage.Builder) ((ImMessage.Builder) ((ImMessage.Builder) ((ImMessage.Builder) ((ImMessage.Builder) ((ImMessage.Builder) ((ImMessage.Builder) ((ImMessage.Builder) ((ImMessage.Builder) ((ImMessage.Builder) ((ImMessage.Builder) ((ImMessage.Builder) ((ImMessage.Builder) ((ImMessage.Builder) builder.sentTimestamp(date != null ? date.getTime() : System.currentTimeMillis())).imdnRecordRouteList(imIncomingMessageEvent.mImdnRecRouteList)).mnoStrategy(this.mImModule.getRcsStrategy(phoneIdByIMSI))).notDisplayedCounter(extractImDirection == ImDirection.OUTGOING ? imSession.getParticipantsSize() : 0)).isRoutingMsg(imIncomingMessageEvent.mIsRoutingMsg)).routingType(imIncomingMessageEvent.mRoutingType)).network(network)).conversationId(imSession.getConversationId())).contributionId(imSession.getContributionId())).deviceId(imIncomingMessageEvent.mDeviceId)).simIMSI(str)).maapTrafficType(extractMaapTrafficType)).referenceImdnId(extractRcsReferenceId)).referenceType(extractRcsReferenceType)).referenceValue(extractRcsReferenceValue)).rcsTrafficType(extractRcsTrafficType)).build();
        registerMessage(build);
        this.mCmStoreInvoker.onReceiveRcsMessage(str, build.getId(), build.getImdnId());
        return build;
    }

    public synchronized ImMessage makeNewIncomingMessage(String str, ImSession imSession, SlmIncomingMessageEvent slmIncomingMessageEvent, Network network, String str2) {
        ImMessage build;
        ImConstants.Type type = MessageBase.getType(slmIncomingMessageEvent.mContentType);
        if (slmIncomingMessageEvent.mIsPublicAccountMsg) {
            type = ImConstants.Type.TEXT_PUBLICACCOUNT;
        }
        if (slmIncomingMessageEvent.mBody.toLowerCase().startsWith("geo")) {
            type = ImConstants.Type.LOCATION;
        }
        int phoneIdByIMSI = this.mImModule.getPhoneIdByIMSI(str);
        ImConstants.MessagingTech messagingTech = slmIncomingMessageEvent.mIsLMM ? ImConstants.MessagingTech.SLM_LARGE_MODE : ImConstants.MessagingTech.SLM_PAGER_MODE;
        ImDirection extractImDirection = ImCpimNamespacesHelper.extractImDirection(phoneIdByIMSI, slmIncomingMessageEvent.mCpimNamespaces);
        String extractMaapTrafficType = ImCpimNamespacesHelper.extractMaapTrafficType(slmIncomingMessageEvent.mCpimNamespaces);
        String extractRcsTrafficType = ImCpimNamespacesHelper.extractRcsTrafficType(slmIncomingMessageEvent.mCpimNamespaces);
        ImMessage.Builder builder = (ImMessage.Builder) ((ImMessage.Builder) ((ImMessage.Builder) ((ImMessage.Builder) ((ImMessage.Builder) ((ImMessage.Builder) ((ImMessage.Builder) ((ImMessage.Builder) ((ImMessage.Builder) ((ImMessage.Builder) ((ImMessage.Builder) ((ImMessage.Builder) ((ImMessage.Builder) ((ImMessage.Builder) ((ImMessage.Builder) ((ImMessage.Builder) ((ImMessage.Builder) ((ImMessage.Builder) ((ImMessage.Builder) ImMessage.builder().module(this.mImModule)).imsService(getImHandler())).slmService(ImsRegistry.getHandlerFactory().getSlmHandler())).listener(this.mImModule.getImProcessor()).config(this.mImModule.getImConfig(phoneIdByIMSI))).uriGenerator(this.mImModule.getUriGenerator(phoneIdByIMSI))).chatId(imSession.getChatId())).body(slmIncomingMessageEvent.mBody)).suggestion(str2)).remoteUri(this.mImModule.normalizeUri(phoneIdByIMSI, slmIncomingMessageEvent.mSender))).userAlias(slmIncomingMessageEvent.mUserAlias)).imdnId(slmIncomingMessageEvent.mImdnMessageId)).imdnIdOriginalTo(slmIncomingMessageEvent.mOriginalToHdr)).direction(extractImDirection)).type(type)).isSlmSvcMsg(true)).contentType(slmIncomingMessageEvent.mContentType)).status(ImConstants.Status.UNREAD)).dispNotification(slmIncomingMessageEvent.mDispositionNotification)).insertedTimestamp(System.currentTimeMillis());
        Date date = slmIncomingMessageEvent.mImdnTime;
        build = ((ImMessage.Builder) ((ImMessage.Builder) ((ImMessage.Builder) ((ImMessage.Builder) ((ImMessage.Builder) ((ImMessage.Builder) ((ImMessage.Builder) ((ImMessage.Builder) ((ImMessage.Builder) ((ImMessage.Builder) ((ImMessage.Builder) ((ImMessage.Builder) ((ImMessage.Builder) ((ImMessage.Builder) builder.sentTimestamp(date != null ? date.getTime() : System.currentTimeMillis())).imdnRecordRouteList(slmIncomingMessageEvent.mImdnRecRouteList)).mnoStrategy(this.mImModule.getRcsStrategy(phoneIdByIMSI))).notDisplayedCounter(extractImDirection == ImDirection.OUTGOING ? imSession.getParticipantsSize() : 0)).isRoutingMsg(slmIncomingMessageEvent.mIsRoutingMsg)).routingType(slmIncomingMessageEvent.mRoutingType)).isVM2TextMsg(ImExtensionMNOHeadersHelper.isVM2TextMsg(slmIncomingMessageEvent.mImExtensionMNOHeaders))).network(network)).conversationId(slmIncomingMessageEvent.mConversationId)).contributionId(slmIncomingMessageEvent.mContributionId)).simIMSI(str)).maapTrafficType(extractMaapTrafficType)).messagingTech(messagingTech)).rcsTrafficType(extractRcsTrafficType)).build();
        registerMessage(build);
        this.mCmStoreInvoker.onReceiveRcsMessage(str, build.getId(), build.getImdnId());
        return build;
    }

    public synchronized ImMessage makeNewSystemUserMessage(ImSession imSession, String str, ImConstants.Type type, Date date) {
        ImMessage build;
        int phoneIdByIMSI = this.mImModule.getPhoneIdByIMSI(imSession.getOwnImsi());
        build = ((ImMessage.Builder) ((ImMessage.Builder) ((ImMessage.Builder) ((ImMessage.Builder) ((ImMessage.Builder) ((ImMessage.Builder) ((ImMessage.Builder) ((ImMessage.Builder) ((ImMessage.Builder) ((ImMessage.Builder) ((ImMessage.Builder) ((ImMessage.Builder) ((ImMessage.Builder) ((ImMessage.Builder) ImMessage.builder().module(this.mImModule)).imsService(getImHandler())).slmService(ImsRegistry.getHandlerFactory().getSlmHandler())).listener(this.mImModule.getImProcessor()).config(this.mImModule.getImConfig(phoneIdByIMSI))).uriGenerator(this.mImModule.getUriGenerator(phoneIdByIMSI))).chatId(imSession.getChatId())).body(str)).imdnId(StringIdGenerator.generateImdn())).direction(ImDirection.IRRELEVANT)).status(ImConstants.Status.UNREAD)).type(type)).insertedTimestamp(System.currentTimeMillis())).sentTimestamp(date == null ? System.currentTimeMillis() : date.getTime())).mnoStrategy(this.mImModule.getRcsStrategy(phoneIdByIMSI))).build();
        registerMessage(build);
        return build;
    }

    public synchronized ImMessage makeNewSystemUserMessage(ImSession imSession, String str, ImConstants.Type type) {
        return makeNewSystemUserMessage(imSession, str, type, (Date) null);
    }

    public synchronized FtMessage makeNewIncomingFtMessage(String str, ImSession imSession, FtIncomingSessionEvent ftIncomingSessionEvent, boolean z) {
        FtMsrpMessage build;
        ImConstants.Type type = FtMessage.getType(ftIncomingSessionEvent.mContentType);
        if (ftIncomingSessionEvent.mIsPublicAccountMsg) {
            type = ImConstants.Type.MULTIMEDIA_PUBLICACCOUNT;
        }
        int phoneIdByIMSI = this.mImModule.getPhoneIdByIMSI(str);
        ImDirection extractImDirection = ImCpimNamespacesHelper.extractImDirection(phoneIdByIMSI, ftIncomingSessionEvent.mCpimNamespaces);
        String extractMaapTrafficType = ImCpimNamespacesHelper.extractMaapTrafficType(ftIncomingSessionEvent.mCpimNamespaces);
        String str2 = LOG_TAG;
        Log.i(str2, "makeNewIncomingFtMessage msgType: " + type);
        ImConstants.MessagingTech messagingTech = ImConstants.MessagingTech.NORMAL;
        if (ftIncomingSessionEvent.mIsSlmSvcMsg) {
            messagingTech = ftIncomingSessionEvent.mIsLMM ? ImConstants.MessagingTech.SLM_LARGE_MODE : ImConstants.MessagingTech.SLM_PAGER_MODE;
        }
        FtMsrpMessage.Builder builder = (FtMsrpMessage.Builder) ((FtMsrpMessage.Builder) ((FtMsrpMessage.Builder) ((FtMsrpMessage.Builder) ((FtMsrpMessage.Builder) ((FtMsrpMessage.Builder) ((FtMsrpMessage.Builder) ((FtMsrpMessage.Builder) ((FtMsrpMessage.Builder) FtMsrpMessage.builder().module(this.mImModule)).imsService(getImHandler())).slmService(ImsRegistry.getHandlerFactory().getSlmHandler())).listener(this.mImModule.getFtProcessor())).looper(this.mImModule.getLooper())).config(this.mImModule.getImConfig(phoneIdByIMSI))).uriGenerator(this.mImModule.getUriGenerator(phoneIdByIMSI))).chatId(imSession.getChatId())).direction(extractImDirection);
        String str3 = ftIncomingSessionEvent.mFilePath;
        if (str3 == null) {
            str3 = ftIncomingSessionEvent.mFileName;
        }
        int i = 0;
        FtMsrpMessage.Builder builder2 = (FtMsrpMessage.Builder) ((FtMsrpMessage.Builder) ((FtMsrpMessage.Builder) ((FtMsrpMessage.Builder) ((FtMsrpMessage.Builder) ((FtMsrpMessage.Builder) ((FtMsrpMessage.Builder) ((FtMsrpMessage.Builder) ((FtMsrpMessage.Builder) ((FtMsrpMessage.Builder) ((FtMsrpMessage.Builder) ((FtMsrpMessage.Builder) ((FtMsrpMessage.Builder) ((FtMsrpMessage.Builder) ((FtMsrpMessage.Builder) ((FtMsrpMessage.Builder) ((FtMsrpMessage.Builder) ((FtMsrpMessage.Builder) ((FtMsrpMessage.Builder) ((FtMsrpMessage.Builder) ((FtMsrpMessage.Builder) ((FtMsrpMessage.Builder) builder.filePath(str3)).fileName(ftIncomingSessionEvent.mFileName)).fileSize(ftIncomingSessionEvent.mFileSize)).thumbnailPath(ftIncomingSessionEvent.mThumbPath)).thumbnailTool(this.mImModule.getFtProcessor().getThumbnailTool())).timeDuration(ftIncomingSessionEvent.mTimeDuration)).remoteUri(this.mImModule.normalizeUri(phoneIdByIMSI, ftIncomingSessionEvent.mSenderUri))).userAlias(ftIncomingSessionEvent.mUserAlias)).rawHandle(ftIncomingSessionEvent.mRawHandle).isGroupChat(imSession.isGroupChat())).status(ImConstants.Status.UNREAD)).type(type)).isSlmSvcMsg(z)).contentType(ftIncomingSessionEvent.mContentType)).insertedTimestamp(System.currentTimeMillis())).conversationId(imSession.getConversationId())).contributionId(ftIncomingSessionEvent.mContributionId)).inReplyToConversationId(ftIncomingSessionEvent.mInReplyToConversationId)).imdnId(ftIncomingSessionEvent.mImdnId)).imdnIdOriginalTo(ftIncomingSessionEvent.mOriginalToHdr)).dispNotification(ftIncomingSessionEvent.mDisposition)).fileTransferId(ftIncomingSessionEvent.mFileTransferId)).setState(0);
        Date date = ftIncomingSessionEvent.mImdnTime;
        FtMsrpMessage.Builder builder3 = (FtMsrpMessage.Builder) ((FtMsrpMessage.Builder) ((FtMsrpMessage.Builder) builder2.sentTimestamp(date != null ? date.getTime() : System.currentTimeMillis())).imdnRecordRouteList(ftIncomingSessionEvent.mRecRouteList)).mnoStrategy(this.mImModule.getRcsStrategy(phoneIdByIMSI));
        if (extractImDirection == ImDirection.OUTGOING) {
            i = imSession.getParticipantsSize();
        }
        build = ((FtMsrpMessage.Builder) ((FtMsrpMessage.Builder) ((FtMsrpMessage.Builder) ((FtMsrpMessage.Builder) ((FtMsrpMessage.Builder) ((FtMsrpMessage.Builder) ((FtMsrpMessage.Builder) builder3.notDisplayedCounter(i)).isRoutingMsg(ftIncomingSessionEvent.mIsRoutingMsg)).routingType(ftIncomingSessionEvent.mRoutingType)).deviceId(imSession.getDeviceId())).simIMSI(str)).maapTrafficType(extractMaapTrafficType)).messagingTech(messagingTech)).build();
        registerMessage(build);
        addToPendingList(build);
        return build;
    }

    public synchronized FtMessage makeNewOutgoingFtMessage(String str, ImSession imSession, String str2, Uri uri, ImsUri imsUri, Set<NotificationStatus> set, String str3, String str4, boolean z, boolean z2, boolean z3, String str5) {
        String str6;
        String str7;
        FtMsrpMessage build;
        String str8 = str;
        String str9 = str2;
        Uri uri2 = uri;
        String str10 = str4;
        synchronized (this) {
            if (imSession.getDirection() == ImDirection.INCOMING) {
                str6 = imSession.getInReplyToContributionId();
                imSession.setDirection(ImDirection.OUTGOING);
            } else {
                ImSession imSession2 = imSession;
                str6 = null;
            }
            if (TextUtils.isEmpty(str4) || !str10.equalsIgnoreCase(MIMEContentType.LOCATION_PUSH)) {
                str7 = FileUtils.copyFileToCacheFromUri(this.mImModule.getContext(), str9, uri2);
            } else {
                str7 = this.mImModule.getContext().getExternalCacheDir().getAbsolutePath() + "/" + str9;
            }
            File file = new File(str7);
            int phoneIdByIMSI = this.mImModule.getPhoneIdByIMSI(str);
            if (str10 == null) {
                str10 = FileUtils.getContentType(file);
            }
            ImConstants.Type type = FtMessage.getType(str10);
            if (z) {
                type = ImConstants.Type.MULTIMEDIA_PUBLICACCOUNT;
            }
            Log.i(LOG_TAG, "makeNewOutgoingFtMessage msgType: " + type);
            build = ((FtMsrpMessage.Builder) ((FtMsrpMessage.Builder) ((FtMsrpMessage.Builder) ((FtMsrpMessage.Builder) ((FtMsrpMessage.Builder) ((FtMsrpMessage.Builder) ((FtMsrpMessage.Builder) ((FtMsrpMessage.Builder) ((FtMsrpMessage.Builder) ((FtMsrpMessage.Builder) ((FtMsrpMessage.Builder) ((FtMsrpMessage.Builder) ((FtMsrpMessage.Builder) ((FtMsrpMessage.Builder) ((FtMsrpMessage.Builder) ((FtMsrpMessage.Builder) ((FtMsrpMessage.Builder) ((FtMsrpMessage.Builder) ((FtMsrpMessage.Builder) ((FtMsrpMessage.Builder) ((FtMsrpMessage.Builder) ((FtMsrpMessage.Builder) ((FtMsrpMessage.Builder) ((FtMsrpMessage.Builder) ((FtMsrpMessage.Builder) ((FtMsrpMessage.Builder) ((FtMsrpMessage.Builder) ((FtMsrpMessage.Builder) ((FtMsrpMessage.Builder) ((FtMsrpMessage.Builder) ((FtMsrpMessage.Builder) ((FtMsrpMessage.Builder) ((FtMsrpMessage.Builder) ((FtMsrpMessage.Builder) ((FtMsrpMessage.Builder) ((FtMsrpMessage.Builder) ((FtMsrpMessage.Builder) ((FtMsrpMessage.Builder) FtMsrpMessage.builder().module(this.mImModule)).imsService(getImHandler())).slmService(ImsRegistry.getHandlerFactory().getSlmHandler())).listener(this.mImModule.getFtProcessor())).looper(this.mImModule.getLooper())).config(this.mImModule.getImConfig(phoneIdByIMSI))).thumbnailTool(this.mImModule.getFtProcessor().getThumbnailTool())).uriGenerator(this.mImModule.getUriGenerator(phoneIdByIMSI))).chatId(imSession.getChatId())).direction(ImDirection.OUTGOING)).filePath(str7)).contentUri(uri2)).fileName(file.getName())).fileSize(file.length())).thumbnailPath((String) null)).timeDuration(0)).remoteUri(imsUri)).userAlias(this.mImModule.getUserAlias(phoneIdByIMSI, false))).contributionId(StringIdGenerator.generateContributionId())).isGroupChat(imSession.isGroupChat())).status(ImConstants.Status.TO_SEND)).type(type)).contentType(str10)).insertedTimestamp(System.currentTimeMillis())).conversationId(imSession.getConversationId())).inReplyToConversationId(str6)).dispNotification(set)).imdnId(StringIdGenerator.generateImdn())).fileTransferId(StringIdGenerator.generateFileTransferId())).setState(0)).notDisplayedCounter(imSession.getParticipantsSize())).requestMessageId(str3)).isResizable(z2)).isBroadcastMsg(z3)).mnoStrategy(this.mImModule.getRcsStrategy(phoneIdByIMSI))).extinfo(str5)).simIMSI(str)).messagingTech(ImConstants.MessagingTech.NORMAL)).build();
            registerMessage(build);
            addToPendingList(build);
        }
        return build;
    }

    public synchronized FtHttpOutgoingMessage makeNewOutgoingFtHttpMessage(String str, ImSession imSession, String str2, Uri uri, ImsUri imsUri, Set<NotificationStatus> set, String str3, String str4, boolean z, Network network, boolean z2, boolean z3, boolean z4, FileDisposition fileDisposition, boolean z5) {
        FtHttpOutgoingMessage build;
        String str5 = str;
        String str6 = str2;
        Uri uri2 = uri;
        synchronized (this) {
            long sizeFromUri = FileUtils.getSizeFromUri(this.mImModule.getContext(), uri);
            int phoneIdByIMSI = this.mImModule.getPhoneIdByIMSI(str);
            String contentType = str4 == null ? FileUtils.getContentType(this.mImModule.getContext(), str2, uri) : str4;
            ImsUri imsUri2 = imsUri;
            build = ((FtHttpOutgoingMessage.Builder) ((FtHttpOutgoingMessage.Builder) ((FtHttpOutgoingMessage.Builder) ((FtHttpOutgoingMessage.Builder) ((FtHttpOutgoingMessage.Builder) ((FtHttpOutgoingMessage.Builder) ((FtHttpOutgoingMessage.Builder) ((FtHttpOutgoingMessage.Builder) ((FtHttpOutgoingMessage.Builder) ((FtHttpOutgoingMessage.Builder) ((FtHttpOutgoingMessage.Builder) ((FtHttpOutgoingMessage.Builder) ((FtHttpOutgoingMessage.Builder) ((FtHttpOutgoingMessage.Builder) ((FtHttpOutgoingMessage.Builder) ((FtHttpOutgoingMessage.Builder) ((FtHttpOutgoingMessage.Builder) ((FtHttpOutgoingMessage.Builder) ((FtHttpOutgoingMessage.Builder) ((FtHttpOutgoingMessage.Builder) ((FtHttpOutgoingMessage.Builder) ((FtHttpOutgoingMessage.Builder) ((FtHttpOutgoingMessage.Builder) ((FtHttpOutgoingMessage.Builder) ((FtHttpOutgoingMessage.Builder) ((FtHttpOutgoingMessage.Builder) ((FtHttpOutgoingMessage.Builder) ((FtHttpOutgoingMessage.Builder) ((FtHttpOutgoingMessage.Builder) ((FtHttpOutgoingMessage.Builder) ((FtHttpOutgoingMessage.Builder) ((FtHttpOutgoingMessage.Builder) FtHttpOutgoingMessage.builder().module(this.mImModule)).imsService(getImHandler())).slmService(ImsRegistry.getHandlerFactory().getSlmHandler())).listener(this.mImModule.getFtProcessor())).looper(this.mImModule.getLooper())).config(this.mImModule.getImConfig(phoneIdByIMSI))).uriGenerator(this.mImModule.getUriGenerator(phoneIdByIMSI))).chatId(imSession.getChatId())).contentUri(uri)).fileName(str2)).fileSize(sizeFromUri)).contentType(contentType)).remoteUri(imsUri)).userAlias(this.mImModule.getUserAlias(phoneIdByIMSI, false))).imdnId(StringIdGenerator.generateImdn())).direction(ImDirection.OUTGOING)).type(FtMessage.getType(contentType))).status(ImConstants.Status.TO_SEND)).dispNotification(set)).insertedTimestamp(System.currentTimeMillis())).setState(0)).notDisplayedCounter(imSession.getParticipantsSize())).requestMessageId(str3)).isGroupChat(imSession.isGroupChat())).mnoStrategy(this.mImModule.getRcsStrategy(phoneIdByIMSI))).setFileDisposition(fileDisposition)).network(network)).extraFt(z)).isBroadcastMsg(z3)).isSlmSvcMsg(z4)).simIMSI(str)).isResizable(z5)).build();
            registerMessage(build);
            build.setFtSms(z2);
            addToPendingList(build);
        }
        return build;
    }

    public synchronized FtHttpIncomingMessage makeNewIncomingFtHttpMessage(String str, ImSession imSession, ImIncomingMessageEvent imIncomingMessageEvent, Network network, String str2) {
        FtHttpIncomingMessage build;
        int phoneIdByIMSI = this.mImModule.getPhoneIdByIMSI(str);
        ImDirection extractImDirection = ImCpimNamespacesHelper.extractImDirection(phoneIdByIMSI, imIncomingMessageEvent.mCpimNamespaces);
        String extractMaapTrafficType = ImCpimNamespacesHelper.extractMaapTrafficType(imIncomingMessageEvent.mCpimNamespaces);
        String extractRcsTrafficType = ImCpimNamespacesHelper.extractRcsTrafficType(imIncomingMessageEvent.mCpimNamespaces);
        FtHttpIncomingMessage.Builder builder = (FtHttpIncomingMessage.Builder) ((FtHttpIncomingMessage.Builder) ((FtHttpIncomingMessage.Builder) ((FtHttpIncomingMessage.Builder) ((FtHttpIncomingMessage.Builder) ((FtHttpIncomingMessage.Builder) ((FtHttpIncomingMessage.Builder) ((FtHttpIncomingMessage.Builder) ((FtHttpIncomingMessage.Builder) ((FtHttpIncomingMessage.Builder) ((FtHttpIncomingMessage.Builder) ((FtHttpIncomingMessage.Builder) ((FtHttpIncomingMessage.Builder) ((FtHttpIncomingMessage.Builder) ((FtHttpIncomingMessage.Builder) ((FtHttpIncomingMessage.Builder) ((FtHttpIncomingMessage.Builder) ((FtHttpIncomingMessage.Builder) ((FtHttpIncomingMessage.Builder) FtHttpIncomingMessage.builder().module(this.mImModule)).imsService(getImHandler())).slmService(ImsRegistry.getHandlerFactory().getSlmHandler())).listener(this.mImModule.getFtProcessor())).looper(this.mImModule.getLooper())).config(this.mImModule.getImConfig(phoneIdByIMSI))).uriGenerator(this.mImModule.getUriGenerator(phoneIdByIMSI))).chatId(imSession.getChatId())).body(imIncomingMessageEvent.mBody)).remoteUri(this.mImModule.normalizeUri(phoneIdByIMSI, imIncomingMessageEvent.mSender))).userAlias(imIncomingMessageEvent.mUserAlias)).imdnId(imIncomingMessageEvent.mImdnMessageId)).imdnIdOriginalTo(imIncomingMessageEvent.mOriginalToHdr)).direction(extractImDirection)).type(FtMessage.getType(imIncomingMessageEvent.mContentType))).contentType(imIncomingMessageEvent.mContentType)).status(ImConstants.Status.UNREAD)).dispNotification(imIncomingMessageEvent.mDispositionNotification)).insertedTimestamp(System.currentTimeMillis());
        Date date = imIncomingMessageEvent.mImdnTime;
        build = ((FtHttpIncomingMessage.Builder) ((FtHttpIncomingMessage.Builder) ((FtHttpIncomingMessage.Builder) ((FtHttpIncomingMessage.Builder) ((FtHttpIncomingMessage.Builder) ((FtHttpIncomingMessage.Builder) ((FtHttpIncomingMessage.Builder) ((FtHttpIncomingMessage.Builder) ((FtHttpIncomingMessage.Builder) ((FtHttpIncomingMessage.Builder) ((FtHttpIncomingMessage.Builder) ((FtHttpIncomingMessage.Builder) ((FtHttpIncomingMessage.Builder) builder.sentTimestamp(date != null ? date.getTime() : System.currentTimeMillis())).setState(0)).imdnRecordRouteList(imIncomingMessageEvent.mImdnRecRouteList)).mnoStrategy(this.mImModule.getRcsStrategy(phoneIdByIMSI))).network(network)).conversationId(imSession.getConversationId())).contributionId(imSession.getContributionId())).deviceId(imSession.getDeviceId())).simIMSI(str)).suggestion(str2)).maapTrafficType(extractMaapTrafficType)).isGroupChat(imSession.isGroupChat())).rcsTrafficType(extractRcsTrafficType)).build();
        registerMessage(build);
        addToPendingList(build);
        return build;
    }

    public synchronized FtHttpIncomingMessage makeNewIncomingFtHttpMessage(String str, ImSession imSession, SlmIncomingMessageEvent slmIncomingMessageEvent, Network network, String str2) {
        FtHttpIncomingMessage build;
        int phoneIdByIMSI = this.mImModule.getPhoneIdByIMSI(str);
        ImDirection extractImDirection = ImCpimNamespacesHelper.extractImDirection(phoneIdByIMSI, slmIncomingMessageEvent.mCpimNamespaces);
        String extractMaapTrafficType = ImCpimNamespacesHelper.extractMaapTrafficType(slmIncomingMessageEvent.mCpimNamespaces);
        String extractRcsTrafficType = ImCpimNamespacesHelper.extractRcsTrafficType(slmIncomingMessageEvent.mCpimNamespaces);
        FtHttpIncomingMessage.Builder builder = (FtHttpIncomingMessage.Builder) ((FtHttpIncomingMessage.Builder) ((FtHttpIncomingMessage.Builder) ((FtHttpIncomingMessage.Builder) ((FtHttpIncomingMessage.Builder) ((FtHttpIncomingMessage.Builder) ((FtHttpIncomingMessage.Builder) ((FtHttpIncomingMessage.Builder) ((FtHttpIncomingMessage.Builder) ((FtHttpIncomingMessage.Builder) ((FtHttpIncomingMessage.Builder) ((FtHttpIncomingMessage.Builder) ((FtHttpIncomingMessage.Builder) ((FtHttpIncomingMessage.Builder) ((FtHttpIncomingMessage.Builder) ((FtHttpIncomingMessage.Builder) ((FtHttpIncomingMessage.Builder) ((FtHttpIncomingMessage.Builder) ((FtHttpIncomingMessage.Builder) ((FtHttpIncomingMessage.Builder) FtHttpIncomingMessage.builder().module(this.mImModule)).imsService(getImHandler())).slmService(ImsRegistry.getHandlerFactory().getSlmHandler())).listener(this.mImModule.getFtProcessor())).looper(this.mImModule.getLooper())).config(this.mImModule.getImConfig(phoneIdByIMSI))).uriGenerator(this.mImModule.getUriGenerator(phoneIdByIMSI))).chatId(imSession.getChatId())).body(slmIncomingMessageEvent.mBody)).remoteUri(this.mImModule.normalizeUri(phoneIdByIMSI, slmIncomingMessageEvent.mSender))).userAlias(slmIncomingMessageEvent.mUserAlias)).imdnId(slmIncomingMessageEvent.mImdnMessageId)).imdnIdOriginalTo(slmIncomingMessageEvent.mOriginalToHdr)).direction(extractImDirection)).type(FtMessage.getType(slmIncomingMessageEvent.mContentType))).isSlmSvcMsg(true)).contentType(slmIncomingMessageEvent.mContentType)).status(ImConstants.Status.UNREAD)).dispNotification(slmIncomingMessageEvent.mDispositionNotification)).insertedTimestamp(System.currentTimeMillis());
        Date date = slmIncomingMessageEvent.mImdnTime;
        build = ((FtHttpIncomingMessage.Builder) ((FtHttpIncomingMessage.Builder) ((FtHttpIncomingMessage.Builder) ((FtHttpIncomingMessage.Builder) ((FtHttpIncomingMessage.Builder) ((FtHttpIncomingMessage.Builder) ((FtHttpIncomingMessage.Builder) ((FtHttpIncomingMessage.Builder) ((FtHttpIncomingMessage.Builder) ((FtHttpIncomingMessage.Builder) ((FtHttpIncomingMessage.Builder) ((FtHttpIncomingMessage.Builder) builder.sentTimestamp(date != null ? date.getTime() : System.currentTimeMillis())).setState(0)).imdnRecordRouteList(slmIncomingMessageEvent.mImdnRecRouteList)).mnoStrategy(this.mImModule.getRcsStrategy(phoneIdByIMSI))).network(network)).conversationId(imSession.getConversationId())).contributionId(imSession.getContributionId())).deviceId(imSession.getDeviceId())).simIMSI(str)).suggestion(str2)).maapTrafficType(extractMaapTrafficType)).rcsTrafficType(extractRcsTrafficType)).build();
        registerMessage(build);
        addToPendingList(build);
        return build;
    }

    public synchronized void addParticipant(Collection<ImParticipant> collection) {
        registerParticipant(collection);
    }

    public synchronized void addParticipantFromCloud(Collection<ImParticipant> collection) {
        registerParticipantFromCloud(collection);
    }

    public synchronized void deleteParticipant(Collection<ImParticipant> collection) {
        unregisterParticipant(collection);
    }

    public synchronized void deleteParticipantFromCloud(Collection<ImParticipant> collection) {
        unregisterParticipantFromCloud(collection);
    }

    public synchronized void updateParticipant(Collection<ImParticipant> collection) {
        updateParticipant(collection, ImCacheAction.UPDATED);
    }

    public synchronized void deleteSession(ImSession imSession) {
        unregisterMessage(this.mPendingMessages.getAll(imSession.getChatId()));
        this.mPersister.deleteParticipant((Collection<ImParticipant>) imSession.getParticipants());
        unregisterSession(imSession);
        removeActiveSession(imSession);
    }

    private void handleDeleteFtMessage(FtMessage ftMessage) {
        String str = LOG_TAG;
        Log.i(str, "handleDeleteFtMessage: msgId:" + ftMessage.getId() + " direction:" + ftMessage.getDirection() + " transferState:" + ftMessage.getStateId());
        ftMessage.removeAutoResumeFileTimer();
        if (!(ftMessage.getStateId() == 3 || ftMessage.getStateId() == 4 || ftMessage.getStateId() == 1)) {
            ftMessage.cancelTransfer(CancelReason.CANCELED_BY_SYSTEM);
        }
        if (ftMessage.getDirection() == ImDirection.INCOMING) {
            boolean deleteFile = ftMessage.deleteFile();
            boolean deleteThumbnail = ftMessage.deleteThumbnail();
            Log.i(str, "handleDeleteFtMessage: msgId:" + ftMessage.getId() + " isDeleted:" + deleteFile + " isThumbnailDeleted:" + deleteThumbnail);
        }
    }

    public void readMessagesforCloudSync(int i, List<String> list) {
        this.mCmStoreInvoker.onReadRcsMessageList(i, list);
    }

    public void cancelMessagesforCloudSync(int i, List<String> list) {
        this.mCmStoreInvoker.onCancelRcsMessageList(i, list);
    }

    public void deleteMessagesforCloudSyncUsingMsgId(List<String> list, boolean z) {
        Log.i(LOG_TAG, "deleteMessagesforCloudSyncUsingMsgId: " + list);
        List<MessageBase> messages = getMessages(list);
        ArrayList arrayList = new ArrayList();
        ArrayList arrayList2 = new ArrayList();
        String str = null;
        String str2 = null;
        for (MessageBase next : messages) {
            String ownIMSI = next.getOwnIMSI();
            Log.d(LOG_TAG, "message imsi " + IMSLog.checker(ownIMSI) + "getphoneid " + this.mCmStoreInvoker.getPhoneIdByIMSI(ownIMSI));
            if (this.mCmStoreInvoker.getPhoneIdByIMSI(ownIMSI) == 0) {
                arrayList.add(String.valueOf(next.getId()));
                str = ownIMSI;
            } else {
                arrayList2.add(String.valueOf(next.getId()));
                str2 = ownIMSI;
            }
        }
        if (arrayList.size() != 0) {
            this.mCmStoreInvoker.onDeleteRcsMessagesUsingMsgId(arrayList, z, str);
        }
        if (arrayList2.size() != 0) {
            this.mCmStoreInvoker.onDeleteRcsMessagesUsingMsgId(arrayList2, z, str2);
        }
    }

    public void deleteMessagesforCloudSyncUsingImdnId(Map<String, Integer> map, boolean z, String str) {
        Log.i(LOG_TAG, "deleteMessagesforCloudSyncUsingImdnId: " + map);
        ArrayList arrayList = new ArrayList();
        ArrayList arrayList2 = new ArrayList();
        String str2 = null;
        String str3 = null;
        for (Map.Entry next : map.entrySet()) {
            MessageBase message = getMessage((String) next.getKey(), ImDirection.fromId(((Integer) next.getValue()).intValue()), str);
            if (message != null) {
                String ownIMSI = message.getOwnIMSI();
                if (this.mCmStoreInvoker.getPhoneIdByIMSI(ownIMSI) == 0) {
                    arrayList.add(String.valueOf(message.getImdnId()));
                    str2 = ownIMSI;
                } else {
                    arrayList2.add(String.valueOf(message.getImdnId()));
                    str3 = ownIMSI;
                }
            }
        }
        Log.d(LOG_TAG, "deleteMessagesforCloudSyncUsingImdnId: msgListSlot1.size = " + arrayList.size() + " msgListSlot2.size = " + arrayList2.size());
        if (arrayList.size() != 0) {
            this.mCmStoreInvoker.onDeleteRcsMessagesUsingImdnId(arrayList, z, str2);
        }
        if (arrayList2.size() != 0) {
            this.mCmStoreInvoker.onDeleteRcsMessagesUsingImdnId(arrayList2, z, str3);
        }
    }

    public synchronized void deleteMessagesforCloudSyncUsingChatId(List<String> list, boolean z) {
        Log.i(LOG_TAG, "deleteMessagesforCloudSyncUsingChatId: " + list);
        List<MessageBase> messagesUsingChatId = getMessagesUsingChatId(list);
        ArrayList arrayList = new ArrayList();
        ArrayList arrayList2 = new ArrayList();
        String str = null;
        String str2 = null;
        for (MessageBase next : messagesUsingChatId) {
            String ownIMSI = next.getOwnIMSI();
            if (this.mCmStoreInvoker.getPhoneIdByIMSI(ownIMSI) == 0) {
                if (!arrayList.contains(next.getChatId())) {
                    arrayList.add(String.valueOf(next.getChatId()));
                    str = ownIMSI;
                }
            } else if (!arrayList2.contains(next.getChatId())) {
                arrayList2.add(String.valueOf(next.getChatId()));
                str2 = ownIMSI;
            }
        }
        if (arrayList.size() != 0) {
            this.mCmStoreInvoker.onDeleteRcsMessagesUsingChatId(arrayList, z, str);
        }
        if (arrayList2.size() != 0) {
            this.mCmStoreInvoker.onDeleteRcsMessagesUsingChatId(arrayList2, z, str2);
        }
    }

    public synchronized void sentMessageForCloudSync(String str, int i, String str2) {
        this.mCmStoreInvoker.onSentMessage(str, i, str2);
    }

    public synchronized void notifyCloudMsgFtEvent(String str, int i, String str2, ImDirection imDirection) {
        this.mCmStoreInvoker.notifyFtEvent(str, i, str2, imDirection);
    }

    public synchronized void deleteMessage(int i) {
        deleteMessage(getMessage(i));
    }

    public synchronized void deleteMessages(Map<String, Integer> map, String str) {
        for (Map.Entry next : map.entrySet()) {
            deleteMessage(getMessage((String) next.getKey(), ImDirection.fromId(((Integer) next.getValue()).intValue()), str));
        }
    }

    private void deleteMessage(MessageBase messageBase) {
        if (messageBase != null) {
            if (messageBase instanceof FtMessage) {
                handleDeleteFtMessage((FtMessage) messageBase);
            }
            unregisterMessage(messageBase);
            this.mPersister.deleteMessage(messageBase.mId);
        }
    }

    public synchronized void deleteAllMessages(String str) {
        if (!TextUtils.isEmpty(str)) {
            List<Integer> queryAllMessageIdsByChatId = this.mPersister.queryAllMessageIdsByChatId(str, true);
            String str2 = LOG_TAG;
            Log.i(str2, "deleteAllMessages ft message ids : " + queryAllMessageIdsByChatId);
            for (Integer intValue : queryAllMessageIdsByChatId) {
                FtMessage ftMessage = getFtMessage(intValue.intValue());
                if (ftMessage != null) {
                    handleDeleteFtMessage(ftMessage);
                }
            }
            HashSet hashSet = new HashSet();
            hashSet.addAll(this.mPendingMessages.getAll(str));
            this.mCachingMessages.snapshot().values().stream().filter(new ImCache$$ExternalSyntheticLambda0(str)).forEach(new ImCache$$ExternalSyntheticLambda1(hashSet));
            unregisterMessage((List<MessageBase>) new ArrayList(hashSet));
        }
        this.mPersister.deleteMessage(str);
    }

    private void registerSession(ImSession imSession) {
        ChatData chatData = imSession.getChatData();
        registerObserver(chatData);
        chatData.triggerObservers(ImCacheAction.INSERTED);
        this.mImSessions.put(imSession.getChatId(), imSession);
    }

    private void unregisterSession(ImSession imSession) {
        ChatData chatData = imSession.getChatData();
        chatData.triggerObservers(ImCacheAction.DELETED);
        unregisterObserver(chatData);
        this.mImSessions.remove(imSession.getChatId());
    }

    private void registerMessage(MessageBase messageBase) {
        registerObserver(messageBase);
        messageBase.triggerObservers(ImCacheAction.INSERTED);
    }

    public void addToPendingList(MessageBase messageBase) {
        if (messageBase != null) {
            this.mPendingMessages.put(messageBase);
        } else {
            Log.w(LOG_TAG, "Message is null.");
        }
    }

    public void removeFromPendingList(int i) {
        MessageBase messageBase = this.mPendingMessages.get(i);
        if (messageBase != null) {
            unregisterObserver(messageBase);
            this.mPendingMessages.remove(i);
            String str = LOG_TAG;
            Log.i(str, "removed message from cache:" + i);
            return;
        }
        String str2 = LOG_TAG;
        Log.w(str2, "Message is not in the cache:" + i);
    }

    private void unregisterMessage(MessageBase messageBase) {
        messageBase.triggerObservers(ImCacheAction.DELETED);
        unregisterObserver(messageBase);
        this.mPendingMessages.remove(messageBase.getId());
        this.mCachingMessages.remove(new MessageKey(messageBase.getImdnId(), messageBase.getDirection(), messageBase.getChatId()));
    }

    private void unregisterMessage(List<MessageBase> list) {
        updateMessage((Collection<MessageBase>) list, ImCacheAction.DELETED);
        for (MessageBase next : list) {
            unregisterObserver(next);
            this.mPendingMessages.remove(next.getId());
            this.mCachingMessages.remove(new MessageKey(next.getImdnId(), next.getDirection(), next.getChatId()));
        }
    }

    private void registerParticipant(Collection<ImParticipant> collection) {
        for (ImParticipant registerObserver : collection) {
            registerObserver(registerObserver);
        }
        updateParticipant(collection, ImCacheAction.INSERTED);
    }

    private void registerParticipantFromCloud(Collection<ImParticipant> collection) {
        for (ImParticipant next : collection) {
            List<ImParticipant> queryParticipant = this.mPersister.queryParticipant(next.getChatId(), next.getUri().toString());
            if (!queryParticipant.isEmpty()) {
                if (next.getId() != queryParticipant.get(0).getId()) {
                    next.setId(queryParticipant.get(0).getId());
                }
                registerObserver(next);
            }
        }
        updateParticipantFromCloud(collection, ImCacheAction.INSERTED);
    }

    private void unregisterParticipant(Collection<ImParticipant> collection) {
        updateParticipant(collection, ImCacheAction.DELETED);
        for (ImParticipant unregisterObserver : collection) {
            unregisterObserver(unregisterObserver);
        }
    }

    private void unregisterParticipantFromCloud(Collection<ImParticipant> collection) {
        updateParticipantFromCloud(collection, ImCacheAction.DELETED);
        for (ImParticipant unregisterObserver : collection) {
            unregisterObserver(unregisterObserver);
        }
    }

    public void updateActiveSession(ImSession imSession) {
        this.mActiveSessions.put(Integer.valueOf(imSession.getId()), imSession);
    }

    public void removeActiveSession(ImSession imSession) {
        this.mActiveSessions.remove(Integer.valueOf(imSession.getId()));
    }

    public Collection<ImSession> getActiveSessions() {
        return this.mActiveSessions.snapshot().values();
    }

    public boolean isEstablishedSessionExist() {
        for (ImSession isEstablishedState : this.mActiveSessions.snapshot().values()) {
            if (isEstablishedState.isEstablishedState()) {
                return true;
            }
        }
        return false;
    }

    public boolean hasFileTransferInprogress() {
        for (MessageBase messageBase : new ArrayList(this.mPendingMessages.getAll())) {
            if ((messageBase instanceof FtMsrpMessage) && ((FtMessage) messageBase).getStateId() == 2) {
                return true;
            }
        }
        return false;
    }

    /* JADX WARNING: Code restructure failed: missing block: B:10:0x002c, code lost:
        return true;
     */
    /* JADX WARNING: Removed duplicated region for block: B:5:0x0015  */
    /* Code decompiled incorrectly, please refer to instructions dump. */
    public synchronized boolean hasProcessingFileTransfer() {
        /*
            r3 = this;
            monitor-enter(r3)
            android.util.LruCache<java.lang.String, com.sec.internal.ims.servicemodules.im.ImSession> r0 = r3.mImSessions     // Catch:{ all -> 0x0031 }
            java.util.Map r0 = r0.snapshot()     // Catch:{ all -> 0x0031 }
            java.util.Collection r0 = r0.values()     // Catch:{ all -> 0x0031 }
            java.util.Iterator r0 = r0.iterator()     // Catch:{ all -> 0x0031 }
        L_0x000f:
            boolean r1 = r0.hasNext()     // Catch:{ all -> 0x0031 }
            if (r1 == 0) goto L_0x002e
            java.lang.Object r1 = r0.next()     // Catch:{ all -> 0x0031 }
            com.sec.internal.ims.servicemodules.im.ImSession r1 = (com.sec.internal.ims.servicemodules.im.ImSession) r1     // Catch:{ all -> 0x0031 }
            java.util.ArrayList<com.sec.internal.ims.servicemodules.im.FtMessage> r2 = r1.mProcessingFileTransfer     // Catch:{ all -> 0x0031 }
            boolean r2 = r2.isEmpty()     // Catch:{ all -> 0x0031 }
            if (r2 == 0) goto L_0x002b
            java.util.ArrayList<com.sec.internal.ims.servicemodules.im.FtMessage> r1 = r1.mPendingFileTransfer     // Catch:{ all -> 0x0031 }
            boolean r1 = r1.isEmpty()     // Catch:{ all -> 0x0031 }
            if (r1 != 0) goto L_0x000f
        L_0x002b:
            monitor-exit(r3)
            r3 = 1
            return r3
        L_0x002e:
            monitor-exit(r3)
            r3 = 0
            return r3
        L_0x0031:
            r0 = move-exception
            monitor-exit(r3)
            throw r0
        */
        throw new UnsupportedOperationException("Method not decompiled: com.sec.internal.ims.servicemodules.im.ImCache.hasProcessingFileTransfer():boolean");
    }

    public void updateDesiredNotificationStatusAsDisplay(List<MessageBase> list) {
        Long valueOf = Long.valueOf(System.currentTimeMillis());
        ArrayList arrayList = new ArrayList();
        for (MessageBase next : list) {
            if (next != null) {
                next.setDesiredNotificationStatus(NotificationStatus.DISPLAYED);
                next.setDisplayedTimestamp(valueOf.longValue());
                if (next.getStatus() != ImConstants.Status.FAILED) {
                    next.setStatus(ImConstants.Status.READ);
                }
                arrayList.add(String.valueOf(next.getId()));
            }
        }
        this.mPersister.updateDesiredNotificationStatusAsDisplayed(arrayList, NotificationStatus.DISPLAYED.getId(), valueOf.longValue());
    }

    public synchronized void addToChatbotRoleUris(ImsUri imsUri, String str) {
        if (!(imsUri == null || str == null)) {
            if (this.mIsLoaded) {
                String str2 = LOG_TAG;
                Log.i(str2, "addToChatbotRoleUris: uri = " + IMSLog.checker(imsUri) + " " + IMSLog.checker(this.mChatbotRoleUris));
                getOrLoadChatbotRoleUris(str).add(imsUri);
            }
        }
    }

    public synchronized void removeFromChatbotRoleUris(ImsUri imsUri, String str) {
        if (!(imsUri == null || str == null)) {
            if (this.mIsLoaded) {
                String str2 = LOG_TAG;
                Log.i(str2, "removeFromChatbotRoleUris: uri = " + IMSLog.checker(imsUri) + " " + IMSLog.checker(this.mChatbotRoleUris));
                getOrLoadChatbotRoleUris(str).remove(imsUri);
            }
        }
    }

    /* JADX WARNING: Code restructure failed: missing block: B:13:0x0017, code lost:
        return false;
     */
    /* Code decompiled incorrectly, please refer to instructions dump. */
    public synchronized boolean isChatbotRoleUri(com.sec.ims.util.ImsUri r2, java.lang.String r3) {
        /*
            r1 = this;
            monitor-enter(r1)
            if (r2 == 0) goto L_0x0016
            if (r3 == 0) goto L_0x0016
            boolean r0 = r1.mIsLoaded     // Catch:{ all -> 0x0013 }
            if (r0 == 0) goto L_0x0016
            java.util.Set r3 = r1.getOrLoadChatbotRoleUris(r3)     // Catch:{ all -> 0x0013 }
            boolean r2 = r3.contains(r2)     // Catch:{ all -> 0x0013 }
            monitor-exit(r1)
            return r2
        L_0x0013:
            r2 = move-exception
            monitor-exit(r1)
            throw r2
        L_0x0016:
            monitor-exit(r1)
            r1 = 0
            return r1
        */
        throw new UnsupportedOperationException("Method not decompiled: com.sec.internal.ims.servicemodules.im.ImCache.isChatbotRoleUri(com.sec.ims.util.ImsUri, java.lang.String):boolean");
    }

    private synchronized Set<ImsUri> getOrLoadChatbotRoleUris(String str) {
        Set<ImsUri> set;
        Log.i(LOG_TAG, "getOrloadChatbotRoleUris()");
        set = this.mChatbotRoleUris.get(str);
        if (set == null) {
            set = new HashSet<>();
            set.addAll(this.mPersister.queryChatbotRoleUris(str));
            this.mChatbotRoleUris.put(str, set);
        }
        return set;
    }

    public void closeDB() {
        this.mPersister.closeDB();
    }

    public ImPersister getPersister() {
        return this.mPersister;
    }

    public synchronized long getConferenceTimestamp(String str) {
        if (TextUtils.isEmpty(str)) {
            return -1;
        }
        return this.mPersister.querySessionByChatId(str).getInsertedTimeStamp();
    }
}
