package com.sec.internal.ims.servicemodules.im;

import android.content.Context;
import android.os.Handler;
import android.os.Message;
import android.util.Log;
import com.sec.ims.util.ImsUri;
import com.sec.internal.constants.ims.servicemodules.im.ImConstants;
import com.sec.internal.constants.ims.servicemodules.im.MessageRevokeResponse;
import com.sec.internal.constants.ims.servicemodules.im.result.Result;
import com.sec.internal.helper.PhoneIdKeyMap;
import com.sec.internal.helper.PreciseAlarmManager;
import com.sec.internal.ims.core.sim.SimManagerFactory;
import com.sec.internal.ims.registry.ImsRegistry;
import com.sec.internal.ims.servicemodules.im.listener.IChatEventListener;
import com.sec.internal.ims.servicemodules.im.listener.IFtEventListener;
import com.sec.internal.ims.servicemodules.im.listener.IMessageEventListener;
import com.sec.internal.ims.servicemodules.im.strategy.IMnoStrategy;
import com.sec.internal.ims.util.ImsUtil;
import com.sec.internal.imscr.LogClass;
import com.sec.internal.log.IMSLog;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class ImRevocationHandler extends Handler {
    private static final String LOG_TAG = ImRevocationHandler.class.getSimpleName();
    private final ImCache mCache;
    private final Context mContext;
    private final ImModule mImModule;
    private final ImSessionProcessor mImSessionProcessor;
    private final PhoneIdKeyMap<Boolean> mIsReconnectGuardTimersRunning;
    private final Map<String, String> mRevokingMessages = new HashMap();

    public ImRevocationHandler(Context context, ImModule imModule, ImCache imCache, ImSessionProcessor imSessionProcessor) {
        super(imModule.getLooper());
        this.mContext = context;
        this.mImModule = imModule;
        this.mCache = imCache;
        this.mImSessionProcessor = imSessionProcessor;
        this.mIsReconnectGuardTimersRunning = new PhoneIdKeyMap<>(SimManagerFactory.getAllSimManagers().size(), Boolean.FALSE);
    }

    public void handleMessage(Message message) {
        super.handleMessage(message);
        if (message.what == 26) {
            handleEventReconnectGuardTimerExpired(((Integer) message.obj).intValue());
        }
    }

    /* access modifiers changed from: protected */
    public void setLegacyLatching(ImsUri imsUri, boolean z, String str) {
        int phoneIdByIMSI = this.mImModule.getPhoneIdByIMSI(str);
        if (this.mImModule.getImConfig(phoneIdByIMSI).getLegacyLatching() && ImsRegistry.getServiceModuleManager().getCapabilityDiscoveryModule().setLegacyLatching(imsUri, z, phoneIdByIMSI)) {
            String str2 = LOG_TAG;
            Log.i(str2, "setLegacyLatching: Uri = " + IMSLog.checker(imsUri) + ", bool = " + z);
        }
    }

    /* access modifiers changed from: protected */
    public void onMessageRevokeTimerExpired(String str, Collection<String> collection, String str2) {
        int phoneIdByIMSI = this.mImModule.getPhoneIdByIMSI(str2);
        ImsUtil.listToDumpFormat(LogClass.IM_REVOKE_TIMEOUT, phoneIdByIMSI, str);
        if (!this.mImModule.isRegistered(phoneIdByIMSI) || isReconnectGuardTimersRunning(phoneIdByIMSI)) {
            Log.e(LOG_TAG, "onMessageRevokeTimerExpired: Deregi state or ReconnectGuardTimerRunning");
            return;
        }
        for (IChatEventListener onMessageRevokeTimerExpired : this.mImSessionProcessor.getChatEventListeners()) {
            onMessageRevokeTimerExpired.onMessageRevokeTimerExpired(str, collection);
        }
    }

    /* access modifiers changed from: protected */
    public void requestMessageRevocation(String str, List<String> list, boolean z, int i) {
        post(new ImRevocationHandler$$ExternalSyntheticLambda0(this, str, z, i, list));
    }

    /* access modifiers changed from: private */
    public /* synthetic */ void lambda$requestMessageRevocation$0(String str, boolean z, int i, List list) {
        ImSession imSession = this.mCache.getImSession(str);
        if (imSession == null) {
            Log.e(LOG_TAG, "requestMessageRevocation(): Session not found in the cache.");
            return;
        }
        int phoneIdByIMSI = this.mImModule.getPhoneIdByIMSI(imSession.getOwnImsi());
        if (this.mImModule.isRegistered(phoneIdByIMSI)) {
            ArrayList arrayList = new ArrayList();
            arrayList.add(z ? "1" : "0");
            arrayList.add(String.valueOf(i));
            ImsUtil.listToDumpFormat(LogClass.IM_REVOKE_REQ, phoneIdByIMSI, str, arrayList);
            if (list != null) {
                imSession.messageRevocationRequest(list, z, i);
            } else {
                imSession.messageRevocationRequestAll(z, i);
            }
        } else {
            Log.e(LOG_TAG, "requestMessageRevocation(): Deregi state");
        }
    }

    /* access modifiers changed from: protected */
    public void onMessageRevocationDone(ImConstants.RevocationStatus revocationStatus, Collection<MessageBase> collection, ImSession imSession) {
        String str = LOG_TAG;
        Log.i(str, "onMessageRevocationDone() : Status : " + revocationStatus);
        ArrayList arrayList = new ArrayList();
        for (MessageBase next : collection) {
            next.updateRevocationStatus(revocationStatus);
            arrayList.add(next.getImdnId());
        }
        int i = AnonymousClass1.$SwitchMap$com$sec$internal$constants$ims$servicemodules$im$ImConstants$RevocationStatus[revocationStatus.ordinal()];
        if (i == 1 || i == 2) {
            IMnoStrategy.StrategyResponse strategyResponse = new IMnoStrategy.StrategyResponse(IMnoStrategy.StatusCode.FALLBACK_TO_LEGACY_CFS);
            for (MessageBase next2 : collection) {
                if (next2 instanceof ImMessage) {
                    for (IMessageEventListener onMessageSendingFailed : this.mImSessionProcessor.getMessageEventListener(next2.getType())) {
                        onMessageSendingFailed.onMessageSendingFailed(next2, strategyResponse, (Result) null);
                    }
                } else if (next2 instanceof FtMessage) {
                    for (IFtEventListener onMessageSendingFailed2 : this.mImSessionProcessor.getFtEventListener(next2.getType())) {
                        onMessageSendingFailed2.onMessageSendingFailed(next2, strategyResponse, (Result) null);
                    }
                }
            }
        }
        for (MessageBase id : collection) {
            this.mCache.removeFromPendingList(id.getId());
        }
        imSession.removeMsgFromListForRevoke((Collection<String>) arrayList);
    }

    /* renamed from: com.sec.internal.ims.servicemodules.im.ImRevocationHandler$1  reason: invalid class name */
    static /* synthetic */ class AnonymousClass1 {
        static final /* synthetic */ int[] $SwitchMap$com$sec$internal$constants$ims$servicemodules$im$ImConstants$RevocationStatus;

        /* JADX WARNING: Can't wrap try/catch for region: R(6:0|1|2|3|4|(3:5|6|8)) */
        /* JADX WARNING: Failed to process nested try/catch */
        /* JADX WARNING: Missing exception handler attribute for start block: B:3:0x0012 */
        /* JADX WARNING: Missing exception handler attribute for start block: B:5:0x001d */
        static {
            /*
                com.sec.internal.constants.ims.servicemodules.im.ImConstants$RevocationStatus[] r0 = com.sec.internal.constants.ims.servicemodules.im.ImConstants.RevocationStatus.values()
                int r0 = r0.length
                int[] r0 = new int[r0]
                $SwitchMap$com$sec$internal$constants$ims$servicemodules$im$ImConstants$RevocationStatus = r0
                com.sec.internal.constants.ims.servicemodules.im.ImConstants$RevocationStatus r1 = com.sec.internal.constants.ims.servicemodules.im.ImConstants.RevocationStatus.NONE     // Catch:{ NoSuchFieldError -> 0x0012 }
                int r1 = r1.ordinal()     // Catch:{ NoSuchFieldError -> 0x0012 }
                r2 = 1
                r0[r1] = r2     // Catch:{ NoSuchFieldError -> 0x0012 }
            L_0x0012:
                int[] r0 = $SwitchMap$com$sec$internal$constants$ims$servicemodules$im$ImConstants$RevocationStatus     // Catch:{ NoSuchFieldError -> 0x001d }
                com.sec.internal.constants.ims.servicemodules.im.ImConstants$RevocationStatus r1 = com.sec.internal.constants.ims.servicemodules.im.ImConstants.RevocationStatus.SUCCESS     // Catch:{ NoSuchFieldError -> 0x001d }
                int r1 = r1.ordinal()     // Catch:{ NoSuchFieldError -> 0x001d }
                r2 = 2
                r0[r1] = r2     // Catch:{ NoSuchFieldError -> 0x001d }
            L_0x001d:
                int[] r0 = $SwitchMap$com$sec$internal$constants$ims$servicemodules$im$ImConstants$RevocationStatus     // Catch:{ NoSuchFieldError -> 0x0028 }
                com.sec.internal.constants.ims.servicemodules.im.ImConstants$RevocationStatus r1 = com.sec.internal.constants.ims.servicemodules.im.ImConstants.RevocationStatus.FAILED     // Catch:{ NoSuchFieldError -> 0x0028 }
                int r1 = r1.ordinal()     // Catch:{ NoSuchFieldError -> 0x0028 }
                r2 = 3
                r0[r1] = r2     // Catch:{ NoSuchFieldError -> 0x0028 }
            L_0x0028:
                return
            */
            throw new UnsupportedOperationException("Method not decompiled: com.sec.internal.ims.servicemodules.im.ImRevocationHandler.AnonymousClass1.<clinit>():void");
        }
    }

    /* access modifiers changed from: protected */
    public void addToRevokingMessages(String str, String str2) {
        this.mRevokingMessages.put(str, str2);
    }

    /* access modifiers changed from: protected */
    public void removeFromRevokingMessages(Collection<String> collection) {
        this.mRevokingMessages.keySet().removeAll(collection);
    }

    /* access modifiers changed from: protected */
    public void onSendMessageRevokeRequestDone(MessageRevokeResponse messageRevokeResponse) {
        MessageBase message;
        String str = LOG_TAG;
        Log.i(str, "onSendMessageRevokeRequestDone(): " + messageRevokeResponse);
        ImSession imSession = this.mCache.getImSession(this.mRevokingMessages.get(messageRevokeResponse.mImdnId));
        if (imSession == null) {
            Log.e(str, "onSendMessageRevokeRequestDone(): Session not found.");
            return;
        }
        ImDump imDump = this.mImModule.getImDump();
        imDump.addEventLogs("onSendMessageRevokeRequestDone: chatId=" + imSession.getChatId() + ", convId=" + imSession.getConversationId() + ", contId=" + imSession.getContributionId() + ", imdnId=" + messageRevokeResponse.mImdnId);
        int phoneIdByIMSI = this.mImModule.getPhoneIdByIMSI(imSession.getChatData().getOwnIMSI());
        if (this.mImModule.getImConfig(phoneIdByIMSI).isCfsTrigger()) {
            ArrayList arrayList = new ArrayList();
            arrayList.add(ImsUtil.hideInfo(messageRevokeResponse.mImdnId, 4));
            arrayList.add(messageRevokeResponse.mResult ? "1" : "0");
            ImsUtil.listToDumpFormat(LogClass.IM_REVOKE_REQ_RES, phoneIdByIMSI, imSession.getChatId(), arrayList);
            Integer num = imSession.getNeedToRevokeMessages().get(messageRevokeResponse.mImdnId);
            if (num != null && (message = this.mCache.getMessage(num.intValue())) != null) {
                if (messageRevokeResponse.mResult) {
                    message.updateRevocationStatus(ImConstants.RevocationStatus.SENT);
                    imSession.startMsgRevokeOperationTimer(message.getImdnId());
                    return;
                }
                ArrayList arrayList2 = new ArrayList();
                arrayList2.add(message);
                onMessageRevocationDone(ImConstants.RevocationStatus.NONE, arrayList2, imSession);
            }
        }
    }

    /* access modifiers changed from: protected */
    public void onMessageRevokeResponseReceived(MessageRevokeResponse messageRevokeResponse) {
        String str = LOG_TAG;
        Log.i(str, "onMessageRevokeResponseReceived(): " + messageRevokeResponse);
        ImSession imSession = this.mCache.getImSession(this.mRevokingMessages.remove(messageRevokeResponse.mImdnId));
        if (imSession == null) {
            Log.e(str, "onSendMessageRevokeRequestDone(): Session not found.");
            return;
        }
        ImDump imDump = this.mImModule.getImDump();
        imDump.addEventLogs("onMessageRevokeResponseReceived: chatId=" + imSession.getChatId() + ", convId=" + imSession.getConversationId() + ", contId=" + imSession.getContributionId() + ", imdnId=" + messageRevokeResponse.mImdnId + ", result=" + messageRevokeResponse.mResult);
        int phoneIdByIMSI = this.mImModule.getPhoneIdByIMSI(imSession.getChatData().getOwnIMSI());
        ArrayList arrayList = new ArrayList();
        String str2 = "1";
        arrayList.add(this.mImModule.getImConfig(phoneIdByIMSI).isCfsTrigger() ? str2 : "0");
        if (!this.mImModule.getImConfig(phoneIdByIMSI).isCfsTrigger()) {
            ImsUtil.listToDumpFormat(LogClass.IM_REVOKE_RES, phoneIdByIMSI, imSession.getChatId(), arrayList);
            return;
        }
        arrayList.add(ImsUtil.hideInfo(messageRevokeResponse.mImdnId, 4));
        if (!messageRevokeResponse.mResult) {
            str2 = "0";
        }
        arrayList.add(str2);
        ImsUtil.listToDumpFormat(LogClass.IM_REVOKE_RES, phoneIdByIMSI, imSession.getChatId(), arrayList);
        MessageBase message = this.mCache.getMessage(imSession.getNeedToRevokeMessages().get(messageRevokeResponse.mImdnId).intValue());
        if (message == null) {
            Log.e(str, "onSendMessageRevokeRequestDone(): message not found.");
            return;
        }
        imSession.stopMsgRevokeOperationTimer(message.getImdnId());
        ArrayList arrayList2 = new ArrayList();
        arrayList2.add(message);
        if (messageRevokeResponse.mResult) {
            onMessageRevocationDone(ImConstants.RevocationStatus.SUCCESS, arrayList2, imSession);
        } else {
            onMessageRevocationDone(ImConstants.RevocationStatus.FAILED, arrayList2, imSession);
        }
    }

    /* access modifiers changed from: protected */
    public void stopReconnectGuardTimer(int i) {
        if (this.mIsReconnectGuardTimersRunning.get(i).booleanValue()) {
            this.mIsReconnectGuardTimersRunning.remove(i);
            PreciseAlarmManager.getInstance(this.mContext).removeMessage(obtainMessage(26, Integer.valueOf(i)));
        }
        this.mRevokingMessages.clear();
    }

    /* access modifiers changed from: protected */
    public void handleEventReconnectGuardTimerExpired(int i) {
        Log.i(LOG_TAG, "handleEventReconnectGuardTimerExpired()");
        if (this.mIsReconnectGuardTimersRunning.get(i).booleanValue()) {
            this.mIsReconnectGuardTimersRunning.put(i, Boolean.FALSE);
            for (ImSession next : this.mCache.getAllImSessions()) {
                if (!next.getNeedToRevokeMessages().isEmpty()) {
                    next.reconnectGuardTimerExpired();
                }
            }
        }
    }

    /* access modifiers changed from: protected */
    public void startReconnectGuardTiemer(int i) {
        int reconnectGuardTimer = this.mImModule.getImConfig(i).getReconnectGuardTimer();
        for (ImSession next : this.mCache.getAllImSessions()) {
            if (!this.mIsReconnectGuardTimersRunning.get(i).booleanValue() && reconnectGuardTimer >= 0 && !next.getNeedToRevokeMessages().isEmpty()) {
                String str = LOG_TAG;
                IMSLog.s(str, "mIsReconnectGuardTimersRunning:" + this.mIsReconnectGuardTimersRunning.get(i) + " reconnectGuardTimer:" + reconnectGuardTimer + " list : " + next.getNeedToRevokeMessages().size());
                this.mIsReconnectGuardTimersRunning.put(i, Boolean.TRUE);
                PreciseAlarmManager.getInstance(this.mContext).sendMessageDelayed(getClass().getSimpleName(), obtainMessage(26, Integer.valueOf(i)), ((long) reconnectGuardTimer) * 1000);
                next.handleSendingStateRevokeMessages();
            }
        }
    }

    /* access modifiers changed from: protected */
    public boolean isReconnectGuardTimersRunning(int i) {
        return this.mIsReconnectGuardTimersRunning.get(i).booleanValue();
    }
}
