package com.sec.internal.ims.servicemodules.im;

import android.content.Context;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.util.Log;
import com.sec.ims.util.ImsUri;
import com.sec.internal.constants.ims.servicemodules.im.ChatData;
import com.sec.internal.constants.ims.servicemodules.im.ChatMode;
import com.sec.internal.constants.ims.servicemodules.im.ImConstants;
import com.sec.internal.constants.ims.servicemodules.im.ImDirection;
import com.sec.internal.constants.ims.servicemodules.im.ImError;
import com.sec.internal.constants.ims.servicemodules.im.ImParticipant;
import com.sec.internal.constants.ims.servicemodules.im.NotificationStatus;
import com.sec.internal.constants.ims.servicemodules.im.event.ImIncomingMessageEvent;
import com.sec.internal.constants.ims.servicemodules.im.event.SendMessageFailedEvent;
import com.sec.internal.constants.ims.servicemodules.im.result.Result;
import com.sec.internal.helper.CollectionUtils;
import com.sec.internal.ims.rcs.RcsPolicyManager;
import com.sec.internal.ims.servicemodules.im.listener.IMessageEventListener;
import com.sec.internal.ims.servicemodules.im.listener.ImMessageListener;
import com.sec.internal.ims.servicemodules.im.strategy.IMnoStrategy;
import com.sec.internal.ims.servicemodules.im.util.ImCpimNamespacesHelper;
import com.sec.internal.ims.settings.RcsPolicySettings;
import com.sec.internal.ims.util.ImsUtil;
import com.sec.internal.imscr.LogClass;
import com.sec.internal.interfaces.ims.servicemodules.im.ISlmServiceInterface;
import com.sec.internal.log.IMSLog;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Date;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.Future;
import java.util.concurrent.FutureTask;

public class ImProcessor extends Handler implements ImMessageListener {
    private static final String LOG_TAG = ImProcessor.class.getSimpleName();
    private ImCache mCache;
    private Context mContext;
    private ImModule mImModule;
    private ImSessionProcessor mImSessionProcessor;
    private ImTranslation mImTranslation;
    private final CollectionUtils.ArrayListMultimap<ImConstants.Type, IMessageEventListener> mMessageEventListeners = CollectionUtils.createArrayListMultimap();
    private final ISlmServiceInterface mSlmService;

    public ImProcessor(Context context, ISlmServiceInterface iSlmServiceInterface, ImModule imModule, ImCache imCache) {
        super(imModule.getLooper());
        this.mContext = context;
        this.mImModule = imModule;
        this.mSlmService = iSlmServiceInterface;
        this.mCache = imCache;
    }

    /* access modifiers changed from: protected */
    public void init(ImSessionProcessor imSessionProcessor, ImTranslation imTranslation) {
        this.mImSessionProcessor = imSessionProcessor;
        this.mImTranslation = imTranslation;
    }

    /* access modifiers changed from: protected */
    public void registerMessageEventListener(ImConstants.Type type, IMessageEventListener iMessageEventListener) {
        this.mMessageEventListeners.put(type, iMessageEventListener);
    }

    public void onMessageSendResponse(ImMessage imMessage) {
        List<String> participantsString;
        ImSession imSession = this.mCache.getImSession(imMessage.getChatId());
        if (imSession != null && ((participantsString = imSession.getParticipantsString()) == null || participantsString.isEmpty())) {
            Log.i(LOG_TAG, "onMessageSendResponse: no participants for this chat");
        }
        for (IMessageEventListener onMessageSendResponse : this.mMessageEventListeners.get(imMessage.getType())) {
            onMessageSendResponse.onMessageSendResponse(imMessage);
        }
    }

    public void onMessageReceived(ImMessage imMessage) {
        ImSession imSession = this.mCache.getImSession(imMessage.getChatId());
        if (imSession != null) {
            for (IMessageEventListener onMessageReceived : this.mMessageEventListeners.get(imMessage.getType())) {
                onMessageReceived.onMessageReceived(imMessage, imSession);
            }
        }
    }

    public void onMessageSendingSucceeded(MessageBase messageBase) {
        this.mImSessionProcessor.onMessageSendingSucceeded(messageBase);
    }

    public void onMessageSendResponseTimeout(ImMessage imMessage) {
        for (IMessageEventListener onMessageSendResponseTimeout : this.mMessageEventListeners.get(imMessage.getType())) {
            onMessageSendResponseTimeout.onMessageSendResponseTimeout(imMessage);
        }
    }

    public void onMessageSendingFailed(MessageBase messageBase, IMnoStrategy.StrategyResponse strategyResponse, Result result) {
        this.mImSessionProcessor.onMessageSendingFailed(messageBase, strategyResponse, result);
    }

    /* access modifiers changed from: protected */
    public void sendMessage(ImSession imSession, MessageBase messageBase) {
        String str = LOG_TAG;
        Log.i(str, "sendMessage: message id = " + messageBase.getId());
        int phoneIdByIMSI = this.mImModule.getPhoneIdByIMSI(imSession.getOwnImsi());
        if (this.mImModule.isRegistered(phoneIdByIMSI)) {
            ArrayList arrayList = new ArrayList();
            arrayList.add(ImsUtil.hideInfo(imSession.getConversationId(), 4));
            arrayList.add(ImsUtil.hideInfo(messageBase.getImdnId(), 4));
            arrayList.add(ImsUtil.hideInfo(imSession.getRequestMessageId(), 4));
            arrayList.add(" 0");
            ImsUtil.listToDumpFormat(LogClass.IM_SEND_IM, phoneIdByIMSI, messageBase.getChatId(), arrayList);
            imSession.setDirection(ImDirection.OUTGOING);
            imSession.sendImMessage(messageBase);
        } else if (RcsPolicyManager.getRcsStrategy(phoneIdByIMSI).boolSetting(RcsPolicySettings.RcsPolicy.PENDING_FOR_REGI)) {
            messageBase.updateStatus(ImConstants.Status.TO_SEND);
        } else {
            messageBase.onSendMessageDone(new Result(ImError.REMOTE_TEMPORARILY_UNAVAILABLE, Result.Type.NONE), new IMnoStrategy.StrategyResponse(IMnoStrategy.StatusCode.FALLBACK_TO_LEGACY));
        }
        this.mCache.updateActiveSession(imSession);
    }

    /* access modifiers changed from: protected */
    public Future<ImMessage> sendMessage(String str, String str2, Set<NotificationStatus> set, String str3, String str4, int i, boolean z, boolean z2, boolean z3, List<ImsUri> list, boolean z4, String str5, String str6, String str7, String str8) {
        String str9 = str5;
        String str10 = str6;
        String str11 = str7;
        ImProcessor$$ExternalSyntheticLambda0 imProcessor$$ExternalSyntheticLambda0 = r0;
        ImProcessor$$ExternalSyntheticLambda0 imProcessor$$ExternalSyntheticLambda02 = new ImProcessor$$ExternalSyntheticLambda0(this, str, str2, set, str3, str4, z, z2, z3, str9, str10, str11, i, z4, str8, list);
        FutureTask futureTask = new FutureTask(imProcessor$$ExternalSyntheticLambda0);
        post(futureTask);
        return futureTask;
    }

    /* JADX DEBUG: Multi-variable search result rejected for TypeSearchVarInfo{r12v9, resolved type: int} */
    /* JADX DEBUG: Multi-variable search result rejected for TypeSearchVarInfo{r12v12, resolved type: int} */
    /* JADX WARNING: type inference failed for: r12v13 */
    /* access modifiers changed from: private */
    /*  JADX ERROR: JadxRuntimeException in pass: IfRegionVisitor
        jadx.core.utils.exceptions.JadxRuntimeException: Don't wrap MOVE or CONST insns: 0x020b: MOVE  (r0v22 java.util.List) = (r38v0 java.util.List)
        	at jadx.core.dex.instructions.args.InsnArg.wrapArg(InsnArg.java:164)
        	at jadx.core.dex.visitors.shrink.CodeShrinkVisitor.assignInline(CodeShrinkVisitor.java:133)
        	at jadx.core.dex.visitors.shrink.CodeShrinkVisitor.checkInline(CodeShrinkVisitor.java:118)
        	at jadx.core.dex.visitors.shrink.CodeShrinkVisitor.shrinkBlock(CodeShrinkVisitor.java:65)
        	at jadx.core.dex.visitors.shrink.CodeShrinkVisitor.shrinkMethod(CodeShrinkVisitor.java:43)
        	at jadx.core.dex.visitors.regions.TernaryMod.makeTernaryInsn(TernaryMod.java:122)
        	at jadx.core.dex.visitors.regions.TernaryMod.visitRegion(TernaryMod.java:34)
        	at jadx.core.dex.visitors.regions.DepthRegionTraversal.traverseIterativeStepInternal(DepthRegionTraversal.java:73)
        	at jadx.core.dex.visitors.regions.DepthRegionTraversal.traverseIterativeStepInternal(DepthRegionTraversal.java:78)
        	at jadx.core.dex.visitors.regions.DepthRegionTraversal.traverseIterativeStepInternal(DepthRegionTraversal.java:78)
        	at jadx.core.dex.visitors.regions.DepthRegionTraversal.traverseIterativeStepInternal(DepthRegionTraversal.java:78)
        	at jadx.core.dex.visitors.regions.DepthRegionTraversal.traverseIterativeStepInternal(DepthRegionTraversal.java:78)
        	at jadx.core.dex.visitors.regions.DepthRegionTraversal.traverseIterativeStepInternal(DepthRegionTraversal.java:78)
        	at jadx.core.dex.visitors.regions.DepthRegionTraversal.traverseIterativeStepInternal(DepthRegionTraversal.java:78)
        	at jadx.core.dex.visitors.regions.DepthRegionTraversal.traverseIterativeStepInternal(DepthRegionTraversal.java:78)
        	at jadx.core.dex.visitors.regions.DepthRegionTraversal.traverseIterativeStepInternal(DepthRegionTraversal.java:78)
        	at jadx.core.dex.visitors.regions.DepthRegionTraversal.traverseIterativeStepInternal(DepthRegionTraversal.java:78)
        	at jadx.core.dex.visitors.regions.DepthRegionTraversal.traverseIterativeStepInternal(DepthRegionTraversal.java:78)
        	at jadx.core.dex.visitors.regions.DepthRegionTraversal.traverseIterativeStepInternal(DepthRegionTraversal.java:78)
        	at jadx.core.dex.visitors.regions.DepthRegionTraversal.traverseIterativeStepInternal(DepthRegionTraversal.java:78)
        	at jadx.core.dex.visitors.regions.DepthRegionTraversal.traverseIterativeStepInternal(DepthRegionTraversal.java:78)
        	at jadx.core.dex.visitors.regions.DepthRegionTraversal.traverseIterative(DepthRegionTraversal.java:27)
        	at jadx.core.dex.visitors.regions.IfRegionVisitor.visit(IfRegionVisitor.java:31)
        */
    /* JADX WARNING: Multi-variable type inference failed */
    /* JADX WARNING: Removed duplicated region for block: B:109:0x026a A[ADDED_TO_REGION] */
    /* JADX WARNING: Removed duplicated region for block: B:117:0x0292  */
    /* JADX WARNING: Removed duplicated region for block: B:60:0x0190 A[Catch:{ Exception -> 0x022c }, LOOP:2: B:58:0x018a->B:60:0x0190, LOOP_END] */
    /* JADX WARNING: Removed duplicated region for block: B:66:0x01c7 A[Catch:{ Exception -> 0x022a }] */
    /* JADX WARNING: Removed duplicated region for block: B:67:0x01ca A[Catch:{ Exception -> 0x022a }] */
    /* JADX WARNING: Removed duplicated region for block: B:75:0x01ea A[Catch:{ Exception -> 0x0226 }] */
    /* JADX WARNING: Removed duplicated region for block: B:77:0x0205 A[Catch:{ Exception -> 0x0226 }] */
    public /* synthetic */ com.sec.internal.ims.servicemodules.im.ImMessage lambda$sendMessage$0(java.lang.String r24, java.lang.String r25, java.util.Set r26, java.lang.String r27, java.lang.String r28, boolean r29, boolean r30, boolean r31, java.lang.String r32, java.lang.String r33, java.lang.String r34, int r35, boolean r36, java.lang.String r37, java.util.List r38) throws java.lang.Exception {
        /*
            r23 = this;
            r1 = r23
            r2 = r24
            r15 = r28
            r0 = r29
            r14 = r35
            r13 = r38
            r11 = 4
            r19 = 0
            java.lang.String r3 = LOG_TAG     // Catch:{ Exception -> 0x024b }
            java.lang.StringBuilder r4 = new java.lang.StringBuilder     // Catch:{ Exception -> 0x024b }
            r4.<init>()     // Catch:{ Exception -> 0x024b }
            java.lang.String r5 = "sendMessage: chatId="
            r4.append(r5)     // Catch:{ Exception -> 0x024b }
            r4.append(r2)     // Catch:{ Exception -> 0x024b }
            java.lang.String r5 = ", body="
            r4.append(r5)     // Catch:{ Exception -> 0x024b }
            java.lang.String r5 = com.sec.internal.log.IMSLog.checker(r25)     // Catch:{ Exception -> 0x024b }
            r4.append(r5)     // Catch:{ Exception -> 0x024b }
            java.lang.String r5 = ", disposition="
            r4.append(r5)     // Catch:{ Exception -> 0x024b }
            r7 = r26
            r4.append(r7)     // Catch:{ Exception -> 0x024b }
            java.lang.String r5 = ", contentType="
            r4.append(r5)     // Catch:{ Exception -> 0x024b }
            r8 = r27
            r4.append(r8)     // Catch:{ Exception -> 0x024b }
            java.lang.String r5 = ", requestMessageId="
            r4.append(r5)     // Catch:{ Exception -> 0x024b }
            r4.append(r15)     // Catch:{ Exception -> 0x024b }
            java.lang.String r5 = ", isBroadcastMsg="
            r4.append(r5)     // Catch:{ Exception -> 0x024b }
            r4.append(r0)     // Catch:{ Exception -> 0x024b }
            java.lang.String r5 = ", isprotectedAccountMsg="
            r4.append(r5)     // Catch:{ Exception -> 0x024b }
            r10 = r30
            r4.append(r10)     // Catch:{ Exception -> 0x024b }
            java.lang.String r5 = ", isGLSMsg="
            r4.append(r5)     // Catch:{ Exception -> 0x024b }
            r9 = r31
            r4.append(r9)     // Catch:{ Exception -> 0x024b }
            java.lang.String r5 = ", maapTrafficType="
            r4.append(r5)     // Catch:{ Exception -> 0x024b }
            r6 = r32
            r4.append(r6)     // Catch:{ Exception -> 0x024b }
            java.lang.String r5 = ", referenceMessageImdnId="
            r4.append(r5)     // Catch:{ Exception -> 0x024b }
            r5 = r33
            r4.append(r5)     // Catch:{ Exception -> 0x024b }
            java.lang.String r12 = ", referenceMessageType="
            r4.append(r12)     // Catch:{ Exception -> 0x024b }
            r12 = r34
            r4.append(r12)     // Catch:{ Exception -> 0x024b }
            java.lang.String r4 = r4.toString()     // Catch:{ Exception -> 0x024b }
            android.util.Log.i(r3, r4)     // Catch:{ Exception -> 0x024b }
            com.sec.internal.ims.servicemodules.im.ImCache r4 = r1.mCache     // Catch:{ Exception -> 0x024b }
            com.sec.internal.ims.servicemodules.im.ImSession r4 = r4.getImSession(r2)     // Catch:{ Exception -> 0x024b }
            if (r4 != 0) goto L_0x00b3
            java.lang.String r0 = "sendMessage: Session not found in the cache."
            android.util.Log.e(r3, r0)     // Catch:{ Exception -> 0x0245 }
            com.sec.internal.helper.CollectionUtils$ArrayListMultimap<com.sec.internal.constants.ims.servicemodules.im.ImConstants$Type, com.sec.internal.ims.servicemodules.im.listener.IMessageEventListener> r0 = r1.mMessageEventListeners     // Catch:{ Exception -> 0x0245 }
            com.sec.internal.constants.ims.servicemodules.im.ImConstants$Type r3 = com.sec.internal.constants.ims.servicemodules.im.ImConstants.Type.TEXT     // Catch:{ Exception -> 0x0245 }
            java.util.Collection r0 = r0.get(r3)     // Catch:{ Exception -> 0x0245 }
            java.util.Iterator r0 = r0.iterator()     // Catch:{ Exception -> 0x0245 }
        L_0x00a2:
            boolean r3 = r0.hasNext()     // Catch:{ Exception -> 0x0245 }
            if (r3 == 0) goto L_0x00b2
            java.lang.Object r3 = r0.next()     // Catch:{ Exception -> 0x0245 }
            com.sec.internal.ims.servicemodules.im.listener.IMessageEventListener r3 = (com.sec.internal.ims.servicemodules.im.listener.IMessageEventListener) r3     // Catch:{ Exception -> 0x0245 }
            r3.onMessageSendResponseFailed(r2, r14, r11, r15)     // Catch:{ Exception -> 0x0245 }
            goto L_0x00a2
        L_0x00b2:
            return r19
        L_0x00b3:
            com.sec.internal.ims.servicemodules.im.ImModule r3 = r1.mImModule     // Catch:{ Exception -> 0x0245 }
            java.lang.String r11 = r4.getOwnImsi()     // Catch:{ Exception -> 0x0241 }
            int r11 = r3.getPhoneIdByIMSI(r11)     // Catch:{ Exception -> 0x0241 }
            com.sec.internal.ims.servicemodules.im.strategy.IMnoStrategy r3 = com.sec.internal.ims.rcs.RcsPolicyManager.getRcsStrategy(r11)     // Catch:{ Exception -> 0x0241 }
            boolean r18 = r4.isGroupChat()     // Catch:{ Exception -> 0x0241 }
            if (r18 != 0) goto L_0x0105
            r18 = r3
            com.sec.internal.ims.servicemodules.im.ImModule r3 = r1.mImModule     // Catch:{ Exception -> 0x00fe }
            int r3 = r3.mKnoxBlockState     // Catch:{ Exception -> 0x00fe }
            r12 = 1
            if (r3 != r12) goto L_0x0107
            com.sec.ims.util.ImsUri r3 = r4.getRemoteUri()     // Catch:{ Exception -> 0x00fe }
            java.lang.String r3 = r3.getMsisdn()     // Catch:{ Exception -> 0x00fe }
            com.sec.internal.constants.ims.servicemodules.im.ImDirection r12 = com.sec.internal.constants.ims.servicemodules.im.ImDirection.OUTGOING     // Catch:{ Exception -> 0x00fe }
            boolean r3 = com.sec.internal.helper.BlockedNumberUtil.isKnoxBlockedNumber(r3, r12)     // Catch:{ Exception -> 0x00fe }
            if (r3 == 0) goto L_0x0107
            com.sec.internal.helper.CollectionUtils$ArrayListMultimap<com.sec.internal.constants.ims.servicemodules.im.ImConstants$Type, com.sec.internal.ims.servicemodules.im.listener.IMessageEventListener> r0 = r1.mMessageEventListeners     // Catch:{ Exception -> 0x00fe }
            com.sec.internal.constants.ims.servicemodules.im.ImConstants$Type r3 = com.sec.internal.constants.ims.servicemodules.im.ImConstants.Type.TEXT     // Catch:{ Exception -> 0x00fe }
            java.util.Collection r0 = r0.get(r3)     // Catch:{ Exception -> 0x00fe }
            java.util.Iterator r0 = r0.iterator()     // Catch:{ Exception -> 0x00fe }
        L_0x00ec:
            boolean r3 = r0.hasNext()     // Catch:{ Exception -> 0x00fe }
            if (r3 == 0) goto L_0x00fd
            java.lang.Object r3 = r0.next()     // Catch:{ Exception -> 0x00fe }
            com.sec.internal.ims.servicemodules.im.listener.IMessageEventListener r3 = (com.sec.internal.ims.servicemodules.im.listener.IMessageEventListener) r3     // Catch:{ Exception -> 0x00fe }
            r12 = 4
            r3.onMessageSendResponseFailed(r2, r14, r12, r15)     // Catch:{ Exception -> 0x023e }
            goto L_0x00ec
        L_0x00fd:
            return r19
        L_0x00fe:
            r0 = move-exception
            r5 = r15
            r3 = r19
            r8 = 4
            goto L_0x0251
        L_0x0105:
            r18 = r3
        L_0x0107:
            r12 = 4
            com.sec.internal.ims.servicemodules.im.ImModule r3 = r1.mImModule     // Catch:{ Exception -> 0x023e }
            boolean r3 = r3.isRegistered(r11)     // Catch:{ Exception -> 0x023e }
            if (r3 == 0) goto L_0x014a
            com.sec.internal.ims.servicemodules.im.ImModule r3 = r1.mImModule     // Catch:{ Exception -> 0x023e }
            java.lang.String r12 = "slm"
            boolean r3 = r3.isServiceRegistered(r11, r12)     // Catch:{ Exception -> 0x00fe }
            if (r3 == 0) goto L_0x014a
            com.sec.internal.ims.servicemodules.im.ImModule r3 = r1.mImModule     // Catch:{ Exception -> 0x00fe }
            com.sec.internal.ims.servicemodules.im.ImConfig r3 = r3.getImConfig(r11)     // Catch:{ Exception -> 0x00fe }
            boolean r3 = r3.getChatEnabled()     // Catch:{ Exception -> 0x00fe }
            if (r3 == 0) goto L_0x0129
            if (r0 == 0) goto L_0x014a
        L_0x0129:
            com.sec.internal.ims.servicemodules.im.ImModule r3 = r1.mImModule     // Catch:{ Exception -> 0x00fe }
            com.sec.internal.ims.servicemodules.im.ImConfig r3 = r3.getImConfig(r11)     // Catch:{ Exception -> 0x00fe }
            com.sec.internal.constants.ims.servicemodules.im.ImConstants$SlmAuth r3 = r3.getSlmAuth()     // Catch:{ Exception -> 0x00fe }
            com.sec.internal.constants.ims.servicemodules.im.ImConstants$SlmAuth r12 = com.sec.internal.constants.ims.servicemodules.im.ImConstants.SlmAuth.ENABLED     // Catch:{ Exception -> 0x00fe }
            if (r3 != r12) goto L_0x014a
            boolean r3 = r4.isGroupChat()     // Catch:{ Exception -> 0x00fe }
            if (r3 == 0) goto L_0x0147
            if (r0 != 0) goto L_0x0147
            com.sec.internal.constants.ims.servicemodules.im.ChatData$ChatType r3 = r4.getChatType()     // Catch:{ Exception -> 0x00fe }
            com.sec.internal.constants.ims.servicemodules.im.ChatData$ChatType r12 = com.sec.internal.constants.ims.servicemodules.im.ChatData.ChatType.ONE_TO_MANY_CHAT     // Catch:{ Exception -> 0x00fe }
            if (r3 != r12) goto L_0x014a
        L_0x0147:
            r20 = 1
            goto L_0x014d
        L_0x014a:
            r3 = 0
            r20 = r3
        L_0x014d:
            com.sec.internal.ims.servicemodules.im.ImCache r3 = r1.mCache     // Catch:{ Exception -> 0x0241 }
            java.lang.String r12 = r4.getOwnImsi()     // Catch:{ Exception -> 0x0241 }
            r0 = r18
            r21 = r4
            r4 = r12
            r5 = r21
            r6 = r25
            r7 = r26
            r8 = r27
            r9 = r28
            r10 = r20
            r12 = r11
            r2 = 4
            r11 = r30
            r22 = r12
            r12 = r29
            r13 = r31
            r14 = r36
            r15 = r32
            r16 = r33
            r17 = r34
            r18 = r37
            com.sec.internal.ims.servicemodules.im.ImMessage r3 = r3.makeNewOutgoingMessage(r4, r5, r6, r7, r8, r9, r10, r11, r12, r13, r14, r15, r16, r17, r18)     // Catch:{ Exception -> 0x0235 }
            com.sec.internal.helper.CollectionUtils$ArrayListMultimap<com.sec.internal.constants.ims.servicemodules.im.ImConstants$Type, com.sec.internal.ims.servicemodules.im.listener.IMessageEventListener> r4 = r1.mMessageEventListeners     // Catch:{ Exception -> 0x022c }
            com.sec.internal.constants.ims.servicemodules.im.ImConstants$Type r5 = r3.getType()     // Catch:{ Exception -> 0x022c }
            java.util.Collection r4 = r4.get(r5)     // Catch:{ Exception -> 0x022c }
            java.util.Iterator r4 = r4.iterator()     // Catch:{ Exception -> 0x022c }
        L_0x018a:
            boolean r5 = r4.hasNext()     // Catch:{ Exception -> 0x022c }
            if (r5 == 0) goto L_0x019a
            java.lang.Object r5 = r4.next()     // Catch:{ Exception -> 0x022c }
            com.sec.internal.ims.servicemodules.im.listener.IMessageEventListener r5 = (com.sec.internal.ims.servicemodules.im.listener.IMessageEventListener) r5     // Catch:{ Exception -> 0x022c }
            r5.onMessageSendResponse(r3)     // Catch:{ Exception -> 0x022c }
            goto L_0x018a
        L_0x019a:
            java.util.ArrayList r4 = new java.util.ArrayList     // Catch:{ Exception -> 0x022c }
            r4.<init>()     // Catch:{ Exception -> 0x022c }
            java.lang.String r5 = r21.getConversationId()     // Catch:{ Exception -> 0x022c }
            java.lang.String r5 = com.sec.internal.ims.util.ImsUtil.hideInfo(r5, r2)     // Catch:{ Exception -> 0x022c }
            r4.add(r5)     // Catch:{ Exception -> 0x022c }
            java.lang.String r5 = r3.getImdnId()     // Catch:{ Exception -> 0x022c }
            java.lang.String r5 = com.sec.internal.ims.util.ImsUtil.hideInfo(r5, r2)     // Catch:{ Exception -> 0x022c }
            r4.add(r5)     // Catch:{ Exception -> 0x022c }
            r5 = r28
            r4.add(r5)     // Catch:{ Exception -> 0x022a }
            java.lang.String r6 = com.sec.internal.log.IMSLog.checker(r25)     // Catch:{ Exception -> 0x022a }
            java.lang.String r6 = com.sec.internal.ims.util.ImsUtil.hideInfo(r6, r2)     // Catch:{ Exception -> 0x022a }
            r4.add(r6)     // Catch:{ Exception -> 0x022a }
            if (r20 == 0) goto L_0x01ca
            java.lang.String r6 = "1"
            goto L_0x01cc
        L_0x01ca:
            java.lang.String r6 = " 0"
        L_0x01cc:
            r4.add(r6)     // Catch:{ Exception -> 0x022a }
            r6 = 1073741825(0x40000001, float:2.0000002)
            r8 = r2
            r7 = r22
            r2 = r24
            com.sec.internal.ims.util.ImsUtil.listToDumpFormat(r6, r7, r2, r4)     // Catch:{ Exception -> 0x0226 }
            com.sec.internal.constants.ims.servicemodules.im.ChatData$ChatType r4 = r21.getChatType()     // Catch:{ Exception -> 0x0226 }
            com.sec.internal.constants.ims.servicemodules.im.ChatData$ChatType r6 = com.sec.internal.constants.ims.servicemodules.im.ChatData.ChatType.PARTICIPANT_BASED_GROUP_CHAT     // Catch:{ Exception -> 0x0226 }
            if (r4 != r6) goto L_0x0205
            java.lang.String r4 = "allow_only_opengroupchat"
            boolean r0 = r0.boolSetting(r4)     // Catch:{ Exception -> 0x0226 }
            if (r0 == 0) goto L_0x0205
            java.lang.String r0 = LOG_TAG     // Catch:{ Exception -> 0x0226 }
            java.lang.String r4 = "Only OpenGroupChat is allowed, fallback to legacy(MMS)"
            android.util.Log.i(r0, r4)     // Catch:{ Exception -> 0x0226 }
            com.sec.internal.constants.ims.servicemodules.im.result.Result r0 = new com.sec.internal.constants.ims.servicemodules.im.result.Result     // Catch:{ Exception -> 0x0226 }
            com.sec.internal.constants.ims.servicemodules.im.ImError r4 = com.sec.internal.constants.ims.servicemodules.im.ImError.REMOTE_TEMPORARILY_UNAVAILABLE     // Catch:{ Exception -> 0x0226 }
            com.sec.internal.constants.ims.servicemodules.im.result.Result$Type r6 = com.sec.internal.constants.ims.servicemodules.im.result.Result.Type.NONE     // Catch:{ Exception -> 0x0226 }
            r0.<init>((com.sec.internal.constants.ims.servicemodules.im.ImError) r4, (com.sec.internal.constants.ims.servicemodules.im.result.Result.Type) r6)     // Catch:{ Exception -> 0x0226 }
            com.sec.internal.ims.servicemodules.im.strategy.IMnoStrategy$StrategyResponse r4 = new com.sec.internal.ims.servicemodules.im.strategy.IMnoStrategy$StrategyResponse     // Catch:{ Exception -> 0x0226 }
            com.sec.internal.ims.servicemodules.im.strategy.IMnoStrategy$StatusCode r6 = com.sec.internal.ims.servicemodules.im.strategy.IMnoStrategy.StatusCode.FALLBACK_TO_LEGACY     // Catch:{ Exception -> 0x0226 }
            r4.<init>(r6)     // Catch:{ Exception -> 0x0226 }
            r3.onSendMessageDone(r0, r4)     // Catch:{ Exception -> 0x0226 }
            return r3
        L_0x0205:
            boolean r0 = r21.isGroupChat()     // Catch:{ Exception -> 0x0226 }
            if (r0 == 0) goto L_0x021e
            r0 = r38
            if (r0 == 0) goto L_0x021e
            boolean r4 = r38.isEmpty()     // Catch:{ Exception -> 0x0226 }
            if (r4 != 0) goto L_0x021e
            com.sec.internal.ims.servicemodules.im.ImModule r4 = r1.mImModule     // Catch:{ Exception -> 0x0226 }
            java.util.Set r0 = r4.normalizeUri((int) r7, (java.util.Collection<com.sec.ims.util.ImsUri>) r0)     // Catch:{ Exception -> 0x0226 }
            r3.setGroupCcListUri(r0)     // Catch:{ Exception -> 0x0226 }
        L_0x021e:
            r4 = r21
            r1.sendMessage(r4, r3)     // Catch:{ Exception -> 0x0224 }
            return r3
        L_0x0224:
            r0 = move-exception
            goto L_0x0251
        L_0x0226:
            r0 = move-exception
            r4 = r21
            goto L_0x0251
        L_0x022a:
            r0 = move-exception
            goto L_0x022f
        L_0x022c:
            r0 = move-exception
            r5 = r28
        L_0x022f:
            r8 = r2
            r4 = r21
            r2 = r24
            goto L_0x0251
        L_0x0235:
            r0 = move-exception
            r5 = r28
            r8 = r2
            r4 = r21
            r2 = r24
            goto L_0x0248
        L_0x023e:
            r0 = move-exception
            r8 = r12
            goto L_0x0247
        L_0x0241:
            r0 = move-exception
            r5 = r15
            r8 = 4
            goto L_0x0248
        L_0x0245:
            r0 = move-exception
            r8 = r11
        L_0x0247:
            r5 = r15
        L_0x0248:
            r3 = r19
            goto L_0x0251
        L_0x024b:
            r0 = move-exception
            r8 = r11
            r5 = r15
            r3 = r19
            r4 = r3
        L_0x0251:
            java.lang.String r6 = LOG_TAG
            java.lang.StringBuilder r7 = new java.lang.StringBuilder
            r7.<init>()
            java.lang.String r9 = "sendMessage Exception e = "
            r7.append(r9)
            r7.append(r0)
            java.lang.String r0 = r7.toString()
            android.util.Log.e(r6, r0)
            if (r3 != 0) goto L_0x0292
            if (r4 == 0) goto L_0x0291
            int r0 = r4.getParticipantsSize()
            r3 = 1
            if (r0 >= r3) goto L_0x0291
            com.sec.internal.helper.CollectionUtils$ArrayListMultimap<com.sec.internal.constants.ims.servicemodules.im.ImConstants$Type, com.sec.internal.ims.servicemodules.im.listener.IMessageEventListener> r0 = r1.mMessageEventListeners
            com.sec.internal.constants.ims.servicemodules.im.ImConstants$Type r1 = com.sec.internal.constants.ims.servicemodules.im.ImConstants.Type.TEXT
            java.util.Collection r0 = r0.get(r1)
            java.util.Iterator r0 = r0.iterator()
        L_0x027f:
            boolean r1 = r0.hasNext()
            if (r1 == 0) goto L_0x0291
            java.lang.Object r1 = r0.next()
            com.sec.internal.ims.servicemodules.im.listener.IMessageEventListener r1 = (com.sec.internal.ims.servicemodules.im.listener.IMessageEventListener) r1
            r3 = r35
            r1.onMessageSendResponseFailed(r2, r3, r8, r5)
            goto L_0x027f
        L_0x0291:
            return r19
        L_0x0292:
            java.lang.String r0 = "sendMessage Failed."
            android.util.Log.e(r6, r0)
            if (r4 == 0) goto L_0x02b3
            boolean r0 = r4.isGroupChat()
            if (r0 != 0) goto L_0x02b3
            com.sec.internal.constants.ims.servicemodules.im.result.Result r0 = new com.sec.internal.constants.ims.servicemodules.im.result.Result
            com.sec.internal.constants.ims.servicemodules.im.ImError r1 = com.sec.internal.constants.ims.servicemodules.im.ImError.REMOTE_TEMPORARILY_UNAVAILABLE
            com.sec.internal.constants.ims.servicemodules.im.result.Result$Type r2 = com.sec.internal.constants.ims.servicemodules.im.result.Result.Type.NONE
            r0.<init>((com.sec.internal.constants.ims.servicemodules.im.ImError) r1, (com.sec.internal.constants.ims.servicemodules.im.result.Result.Type) r2)
            com.sec.internal.ims.servicemodules.im.strategy.IMnoStrategy$StrategyResponse r1 = new com.sec.internal.ims.servicemodules.im.strategy.IMnoStrategy$StrategyResponse
            com.sec.internal.ims.servicemodules.im.strategy.IMnoStrategy$StatusCode r2 = com.sec.internal.ims.servicemodules.im.strategy.IMnoStrategy.StatusCode.FALLBACK_TO_LEGACY
            r1.<init>(r2)
            r3.onSendMessageDone(r0, r1)
        L_0x02b3:
            return r3
        */
        throw new UnsupportedOperationException("Method not decompiled: com.sec.internal.ims.servicemodules.im.ImProcessor.lambda$sendMessage$0(java.lang.String, java.lang.String, java.util.Set, java.lang.String, java.lang.String, boolean, boolean, boolean, java.lang.String, java.lang.String, java.lang.String, int, boolean, java.lang.String, java.util.List):com.sec.internal.ims.servicemodules.im.ImMessage");
    }

    /* access modifiers changed from: protected */
    public void resendMessage(int i) {
        post(new ImProcessor$$ExternalSyntheticLambda6(this, i));
    }

    /* access modifiers changed from: private */
    public /* synthetic */ void lambda$resendMessage$1(int i) {
        ImMessage imMessage = this.mCache.getImMessage(i);
        if (imMessage == null) {
            Log.e(LOG_TAG, "resendMessage: message not found in the cache.");
            return;
        }
        ImSession imSession = this.mCache.getImSession(imMessage.getChatId());
        if (imSession == null) {
            for (IMessageEventListener onMessageSendResponse : this.mMessageEventListeners.get(imMessage.getType())) {
                onMessageSendResponse.onMessageSendResponse(imMessage);
            }
        } else if (imMessage.getStatus() == ImConstants.Status.FAILED) {
            sendMessage(imSession, imMessage);
        }
    }

    /* access modifiers changed from: protected */
    public void reportMessages(List<String> list, String str) {
        post(new ImProcessor$$ExternalSyntheticLambda1(this, list, str));
    }

    /* access modifiers changed from: private */
    public /* synthetic */ void lambda$reportMessages$2(List list, String str) {
        MessageBase message;
        ImsUri imsUri;
        String str2;
        HashSet hashSet;
        ImsUri imsUri2;
        ImsUri imsUri3;
        ImsUri imsUri4;
        ImSession imSession;
        if (this.mImModule.isRegistered()) {
            String str3 = LOG_TAG;
            Log.i(str3, "reportMessages: list=" + list);
            ImsUri parse = ImsUri.parse(this.mImModule.getRcsStrategy().stringSetting(RcsPolicySettings.RcsPolicy.ONEKEY_REPORT_PSI));
            if (parse == null) {
                Log.e(str3, "reportMessages: reportPSI is null");
                return;
            }
            HashSet hashSet2 = new HashSet();
            hashSet2.add(this.mImModule.normalizeUri(parse));
            Iterator it = list.iterator();
            while (it.hasNext() && (message = this.mCache.getMessage((String) it.next(), ImDirection.INCOMING, str)) != null) {
                Date date = new Date(message.getSentTimestamp());
                ImsUri remoteUri = message.getRemoteUri();
                ImsUri parse2 = ImsUri.parse("tel:+" + this.mImModule.getOwnPhoneNum());
                ImSession imSession2 = this.mCache.getImSession(message.getChatId());
                if (imSession2 == null || imSession2.getOwnImsi() == null) {
                    str2 = "";
                    imsUri = parse2;
                } else {
                    String ownImsi = imSession2.getOwnImsi();
                    StringBuilder sb = new StringBuilder();
                    sb.append("tel:+");
                    ImModule imModule = this.mImModule;
                    sb.append(imModule.getOwnPhoneNum(imModule.getPhoneIdByIMSI(ownImsi)));
                    imsUri = ImsUri.parse(sb.toString());
                    str2 = ownImsi;
                }
                if (remoteUri != null && imsUri != null) {
                    ImCache imCache = this.mCache;
                    ChatData.ChatType chatType = ChatData.ChatType.ONE_TO_ONE_CHAT;
                    ImSession imSessionByParticipants = imCache.getImSessionByParticipants(hashSet2, chatType, str2);
                    if (imSessionByParticipants == null) {
                        HashSet hashSet3 = hashSet2;
                        imsUri2 = imsUri;
                        hashSet = hashSet2;
                        imsUri3 = remoteUri;
                        imSessionByParticipants = this.mCache.makeNewOutgoingSession(str2, hashSet3, chatType, (String) null, (String) null, 0, "0", (String) null, ChatMode.OFF);
                    } else {
                        hashSet = hashSet2;
                        imsUri2 = imsUri;
                        imsUri3 = remoteUri;
                    }
                    ImSession imSession3 = imSessionByParticipants;
                    if (message instanceof ImMessage) {
                        ImMessage imMessage = (ImMessage) message;
                        ImMessage makeNewOutgoingMessage = this.mCache.makeNewOutgoingMessage(imSession3.getOwnImsi(), imSession3, imMessage.getBody(), NotificationStatus.toSet("display_delivery"), imMessage.getContentType(), "0", false, false, false, false, false, imMessage.getMaapTrafficType());
                        makeNewOutgoingMessage.setSpamInfo(imsUri3, imsUri2, date.toString(), message.getImdnId());
                        imSession3.setDirection(ImDirection.OUTGOING);
                        imSession3.sendImMessage(makeNewOutgoingMessage);
                        imsUri4 = parse;
                        imSession = imSession3;
                    } else {
                        FtMessage ftMessage = (FtMessage) message;
                        ImCache imCache2 = this.mCache;
                        String ownImsi2 = imSession3.getOwnImsi();
                        String fileName = ftMessage.getFileName();
                        Uri contentUri = ftMessage.getContentUri();
                        imsUri4 = parse;
                        FtMessage makeNewOutgoingFtMessage = imCache2.makeNewOutgoingFtMessage(ownImsi2, imSession3, fileName, contentUri, parse, NotificationStatus.toSet("display_delivery"), "1", (String) null, false, false, false, (String) null);
                        makeNewOutgoingFtMessage.setSpamInfo(imsUri3, imsUri2, date.toString(), message.getImdnId());
                        imSession = imSession3;
                        imSession.attachFile(makeNewOutgoingFtMessage);
                    }
                    this.mCache.updateActiveSession(imSession);
                    hashSet2 = hashSet;
                    parse = imsUri4;
                } else {
                    return;
                }
            }
            return;
        }
        Log.e(LOG_TAG, "reportMessages: not registered");
    }

    /* access modifiers changed from: protected */
    public FutureTask<Boolean> deleteMessages(List<String> list, boolean z) {
        FutureTask<Boolean> futureTask = new FutureTask<>(new ImProcessor$$ExternalSyntheticLambda2(this, list, z));
        post(futureTask);
        return futureTask;
    }

    /* access modifiers changed from: private */
    public /* synthetic */ Boolean lambda$deleteMessages$3(List list, boolean z) throws Exception {
        String str = LOG_TAG;
        Log.i(str, "deleteMessage: list=" + list + " localWipeout: " + z);
        this.mCache.deleteMessagesforCloudSyncUsingMsgId(list, z);
        Iterator it = list.iterator();
        while (it.hasNext()) {
            String str2 = (String) it.next();
            if (str2 != null) {
                this.mCache.deleteMessage(Integer.valueOf(str2).intValue());
            }
        }
        return Boolean.TRUE;
    }

    /* access modifiers changed from: protected */
    public FutureTask<Boolean> deleteMessagesByImdnId(Map<String, Integer> map, String str, boolean z) {
        FutureTask<Boolean> futureTask = new FutureTask<>(new ImProcessor$$ExternalSyntheticLambda7(this, map, z, str));
        post(futureTask);
        return futureTask;
    }

    /* access modifiers changed from: private */
    public /* synthetic */ Boolean lambda$deleteMessagesByImdnId$4(Map map, boolean z, String str) throws Exception {
        String str2 = LOG_TAG;
        Log.i(str2, "deleteMessage: imdnIds=" + map + " localWipeout: " + z);
        this.mCache.deleteMessagesforCloudSyncUsingImdnId(map, z, str);
        this.mCache.deleteMessages(map, str);
        return Boolean.TRUE;
    }

    /* access modifiers changed from: protected */
    public void deleteAllMessages(List<String> list, boolean z) {
        post(new ImProcessor$$ExternalSyntheticLambda3(this, list, z));
    }

    /* access modifiers changed from: private */
    public /* synthetic */ void lambda$deleteAllMessages$5(List list, boolean z) {
        String str = LOG_TAG;
        Log.i(str, "deleteAllMessages: list=" + list);
        this.mCache.deleteMessagesforCloudSyncUsingChatId(list, z);
        Iterator it = list.iterator();
        while (it.hasNext()) {
            this.mCache.deleteAllMessages((String) it.next());
        }
    }

    /* access modifiers changed from: protected */
    public void onSendMessageHandleReportFailed(SendMessageFailedEvent sendMessageFailedEvent) {
        String str = LOG_TAG;
        Log.i(str, "onSendMessageHandleReportFailed: " + sendMessageFailedEvent);
        ImSession imSession = this.mCache.getImSession(sendMessageFailedEvent.mChatId);
        if (imSession != null) {
            MessageBase message = this.mCache.getMessage(sendMessageFailedEvent.mImdnId, ImDirection.OUTGOING, sendMessageFailedEvent.mChatId);
            if (message != null) {
                this.mImModule.mNeedToRemoveFromPendingList.remove(Integer.valueOf(message.getId()));
                imSession.onSendMessageHandleReportFailed(sendMessageFailedEvent, message);
                return;
            }
            Log.e(str, "onSendMessageHandleReportFailed: Message not found.");
            return;
        }
        Log.e(str, "onSendMessageHandleReportFailed: Session not found.");
    }

    /* access modifiers changed from: protected */
    public void getLastSentMessagesStatus(List<String> list) {
        post(new ImProcessor$$ExternalSyntheticLambda5(this, list));
    }

    /* access modifiers changed from: private */
    public /* synthetic */ void lambda$getLastSentMessagesStatus$6(List list) {
        List<Bundle> loadLastSentMessages = this.mCache.loadLastSentMessages(list);
        String str = LOG_TAG;
        Log.i(str, "getLastSentMessagesStatus " + loadLastSentMessages.size() + " messages(s)");
        if (loadLastSentMessages.isEmpty()) {
            this.mImTranslation.notifyLastSentMessagesStatus((List<Bundle>) null);
        } else {
            this.mImTranslation.notifyLastSentMessagesStatus(loadLastSentMessages);
        }
    }

    /* access modifiers changed from: protected */
    /* JADX WARNING: Code restructure failed: missing block: B:9:0x0091, code lost:
        if (com.sec.internal.helper.BlockedNumberUtil.isKnoxBlockedNumber(r1.getRemoteUri().getMsisdn(), com.sec.internal.constants.ims.servicemodules.im.ImDirection.INCOMING) != false) goto L_0x0095;
     */
    /* JADX WARNING: Removed duplicated region for block: B:36:0x012c  */
    /* JADX WARNING: Removed duplicated region for block: B:37:0x0148  */
    /* Code decompiled incorrectly, please refer to instructions dump. */
    public void onIncomingMessageReceived(com.sec.internal.constants.ims.servicemodules.im.event.ImIncomingMessageEvent r14) {
        /*
            r13 = this;
            java.lang.String r0 = LOG_TAG
            java.lang.StringBuilder r1 = new java.lang.StringBuilder
            r1.<init>()
            java.lang.String r2 = "onIncomingMessageReceived: "
            r1.append(r2)
            r1.append(r14)
            java.lang.String r1 = r1.toString()
            android.util.Log.i(r0, r1)
            com.sec.internal.ims.servicemodules.im.ImCache r1 = r13.mCache
            java.lang.String r2 = r14.mChatId
            com.sec.internal.ims.servicemodules.im.ImSession r1 = r1.getImSession(r2)
            if (r1 != 0) goto L_0x0027
            java.lang.String r13 = "session not found"
            android.util.Log.e(r0, r13)
            return
        L_0x0027:
            com.sec.internal.ims.servicemodules.im.ImModule r2 = r13.mImModule
            com.sec.internal.constants.ims.servicemodules.im.ChatData r3 = r1.getChatData()
            java.lang.String r3 = r3.getOwnIMSI()
            int r2 = r2.getPhoneIdByIMSI(r3)
            com.sec.internal.ims.servicemodules.im.ImModule r3 = r13.mImModule
            com.sec.internal.ims.servicemodules.im.ImDump r3 = r3.getImDump()
            java.lang.StringBuilder r4 = new java.lang.StringBuilder
            r4.<init>()
            java.lang.String r5 = "onIncomingMessageReceived: chatId="
            r4.append(r5)
            java.lang.String r5 = r1.getChatId()
            r4.append(r5)
            java.lang.String r5 = ", convId="
            r4.append(r5)
            java.lang.String r5 = r1.getConversationId()
            r4.append(r5)
            java.lang.String r5 = ", contId="
            r4.append(r5)
            java.lang.String r5 = r1.getContributionId()
            r4.append(r5)
            java.lang.String r5 = ", imdnId="
            r4.append(r5)
            java.lang.String r5 = r14.mImdnMessageId
            r4.append(r5)
            java.lang.String r4 = r4.toString()
            r3.addEventLogs(r4)
            boolean r3 = r1.isGroupChat()
            r11 = 0
            if (r3 != 0) goto L_0x0094
            com.sec.internal.ims.servicemodules.im.ImModule r3 = r13.mImModule
            int r3 = r3.mKnoxBlockState
            r4 = 1
            if (r3 != r4) goto L_0x0094
            com.sec.ims.util.ImsUri r3 = r1.getRemoteUri()
            java.lang.String r3 = r3.getMsisdn()
            com.sec.internal.constants.ims.servicemodules.im.ImDirection r5 = com.sec.internal.constants.ims.servicemodules.im.ImDirection.INCOMING
            boolean r3 = com.sec.internal.helper.BlockedNumberUtil.isKnoxBlockedNumber(r3, r5)
            if (r3 == 0) goto L_0x0094
            goto L_0x0095
        L_0x0094:
            r4 = r11
        L_0x0095:
            r1.mIsBlockedIncomingSession = r4
            boolean r3 = r13.isDuplicateMessage(r2, r1, r14)
            if (r3 == 0) goto L_0x009e
            return
        L_0x009e:
            com.sec.internal.constants.ims.servicemodules.im.ChatData r3 = r1.getChatData()
            boolean r3 = r3.isMuted()
            if (r3 == 0) goto L_0x00ae
            java.lang.String r13 = "onIncomingMessageReceived, user reject GC text."
            android.util.Log.i(r0, r13)
            return
        L_0x00ae:
            java.lang.String r3 = r1.getDeviceId()
            r14.mDeviceId = r3
            r13.updateMessageSenderAlias(r2, r1, r14)
            boolean r3 = r1.isGroupChat()
            if (r3 != 0) goto L_0x00e5
            boolean r3 = r1.isChatbotRole()
            if (r3 == 0) goto L_0x00c9
            com.sec.ims.util.ImsUri r3 = r14.mSender
            com.sec.ims.util.ImsUri.removeUriParametersAndHeaders(r3)
            goto L_0x00e5
        L_0x00c9:
            com.sec.internal.ims.servicemodules.im.ImSessionProcessor r3 = r13.mImSessionProcessor
            com.sec.ims.util.ImsUri r4 = r1.getRemoteUri()
            com.sec.internal.constants.ims.servicemodules.im.ChatData r5 = r1.getChatData()
            java.lang.String r5 = r5.getOwnIMSI()
            r3.setLegacyLatching(r4, r11, r5)
            com.sec.internal.ims.servicemodules.im.ImModule r3 = r13.mImModule
            com.sec.internal.ims.servicemodules.im.ImLatchingProcessor r3 = r3.getLatchingProcessor()
            com.sec.ims.util.ImsUri r4 = r14.mSender
            r3.removeUriFromLatchingList(r4, r2)
        L_0x00e5:
            java.lang.String r3 = r14.mBody
            boolean r3 = android.text.TextUtils.isEmpty(r3)
            if (r3 != 0) goto L_0x011e
            java.lang.String r3 = r14.mContentType
            boolean r3 = com.sec.internal.ims.servicemodules.im.ImMultipart.isMultipart(r3)
            if (r3 == 0) goto L_0x011e
            com.sec.internal.ims.servicemodules.im.ImMultipart r3 = new com.sec.internal.ims.servicemodules.im.ImMultipart
            java.lang.String r4 = r14.mBody
            java.lang.String r5 = r14.mContentType
            r3.<init>(r4, r5)
            java.lang.String r4 = r3.getSuggestion()
            boolean r4 = android.text.TextUtils.isEmpty(r4)
            if (r4 != 0) goto L_0x011e
            java.lang.String r4 = "onIncomingMessageReceived: message includes suggestion"
            android.util.Log.i(r0, r4)
            java.lang.String r4 = r3.getBody()
            r14.mBody = r4
            java.lang.String r4 = r3.getContentType()
            r14.mContentType = r4
            java.lang.String r3 = r3.getSuggestion()
            goto L_0x011f
        L_0x011e:
            r3 = 0
        L_0x011f:
            r12 = r3
            java.lang.String r3 = r14.mContentType
            if (r3 == 0) goto L_0x0148
            java.lang.String r4 = "application/vnd.gsma.rcs-ft-http+xml"
            boolean r3 = r3.startsWith(r4)
            if (r3 == 0) goto L_0x0148
            com.sec.internal.ims.servicemodules.im.ImCache r3 = r13.mCache
            java.lang.String r4 = r1.getOwnImsi()
            com.sec.internal.ims.servicemodules.im.ImModule r0 = r13.mImModule
            com.sec.internal.ims.servicemodules.im.ImConfig r5 = r0.getImConfig(r2)
            boolean r5 = r5.isFtHttpOverDefaultPdn()
            android.net.Network r7 = r0.getNetwork(r5, r2)
            r5 = r1
            r6 = r14
            r8 = r12
            com.sec.internal.ims.servicemodules.im.FtHttpIncomingMessage r0 = r3.makeNewIncomingFtHttpMessage((java.lang.String) r4, (com.sec.internal.ims.servicemodules.im.ImSession) r5, (com.sec.internal.constants.ims.servicemodules.im.event.ImIncomingMessageEvent) r6, (android.net.Network) r7, (java.lang.String) r8)
            goto L_0x01b2
        L_0x0148:
            boolean r3 = r14.mIsRoutingMsg
            if (r3 == 0) goto L_0x016f
            com.sec.internal.ims.servicemodules.im.ImModule r4 = r13.mImModule
            com.sec.ims.util.ImsUri r5 = r14.mRequestUri
            com.sec.ims.util.ImsUri r6 = r14.mPAssertedId
            com.sec.ims.util.ImsUri r7 = r14.mSender
            com.sec.ims.util.ImsUri r8 = r14.mReceiver
            boolean r9 = r1.isGroupChat()
            r10 = r2
            com.sec.internal.constants.ims.servicemodules.im.RoutingType r3 = r4.getMsgRoutingType(r5, r6, r7, r8, r9, r10)
            r14.mRoutingType = r3
            com.sec.internal.constants.ims.servicemodules.im.RoutingType r4 = com.sec.internal.constants.ims.servicemodules.im.RoutingType.SENT
            if (r3 != r4) goto L_0x016f
            boolean r3 = r1.isGroupChat()
            if (r3 != 0) goto L_0x016f
            com.sec.ims.util.ImsUri r3 = r14.mReceiver
            r14.mSender = r3
        L_0x016f:
            com.sec.internal.ims.servicemodules.im.ImCache r3 = r13.mCache
            java.lang.String r4 = r1.getOwnImsi()
            com.sec.internal.ims.servicemodules.im.ImModule r5 = r13.mImModule
            android.net.Network r7 = r5.getNetwork(r11, r2)
            r5 = r1
            r6 = r14
            r8 = r12
            com.sec.internal.ims.servicemodules.im.ImMessage r3 = r3.makeNewIncomingMessage((java.lang.String) r4, (com.sec.internal.ims.servicemodules.im.ImSession) r5, (com.sec.internal.constants.ims.servicemodules.im.event.ImIncomingMessageEvent) r6, (android.net.Network) r7, (java.lang.String) r8)
            boolean r4 = r1.isGroupChat()
            if (r4 == 0) goto L_0x01b1
            java.util.List<com.sec.ims.util.ImsUri> r4 = r14.mCcParticipants
            if (r4 == 0) goto L_0x01b1
            boolean r4 = r4.isEmpty()
            if (r4 != 0) goto L_0x01b1
            com.sec.internal.ims.servicemodules.im.ImModule r4 = r13.mImModule
            java.util.List<com.sec.ims.util.ImsUri> r5 = r14.mCcParticipants
            java.util.Set r4 = r4.normalizeUri((int) r2, (java.util.Collection<com.sec.ims.util.ImsUri>) r5)
            r3.setGroupCcListUri(r4)
            java.lang.StringBuilder r5 = new java.lang.StringBuilder
            r5.<init>()
            java.lang.String r6 = "onIncomingMessageReceived, groupCcList="
            r5.append(r6)
            r5.append(r4)
            java.lang.String r4 = r5.toString()
            android.util.Log.i(r0, r4)
        L_0x01b1:
            r0 = r3
        L_0x01b2:
            com.sec.internal.ims.servicemodules.im.ImModule r3 = r13.mImModule
            com.sec.internal.ims.servicemodules.im.ImDump r3 = r3.getImDump()
            boolean r4 = r1.isGroupChat()
            java.lang.String r5 = r1.getChatId()
            java.lang.String r6 = r0.getImdnId()
            r3.dumpIncomingMessageReceived(r2, r4, r5, r6)
            java.lang.Object r3 = r14.mRawHandle
            r1.receiveMessage(r0, r3)
            com.sec.internal.ims.servicemodules.im.ImModule r3 = r13.mImModule
            com.sec.internal.constants.ims.servicemodules.im.ChatData r4 = r1.getChatData()
            java.lang.String r4 = r4.getOwnIMSI()
            com.sec.ims.util.ImsUri r5 = r14.mSender
            java.util.Date r14 = r14.mImdnTime
            r3.updateServiceAvailability(r4, r5, r14)
            com.sec.internal.ims.servicemodules.im.ImSessionProcessor r13 = r13.mImSessionProcessor
            com.sec.internal.ims.servicemodules.im.interfaces.IRcsBigDataProcessor r13 = r13.getBigDataProcessor()
            r13.onMessageReceived(r2, r0, r1)
            return
        */
        throw new UnsupportedOperationException("Method not decompiled: com.sec.internal.ims.servicemodules.im.ImProcessor.onIncomingMessageReceived(com.sec.internal.constants.ims.servicemodules.im.event.ImIncomingMessageEvent):void");
    }

    /* access modifiers changed from: protected */
    /* JADX WARNING: Removed duplicated region for block: B:34:0x017a  */
    /* JADX WARNING: Removed duplicated region for block: B:36:0x0180  */
    /* Code decompiled incorrectly, please refer to instructions dump. */
    public void onIncomingSlmMessage(com.sec.internal.constants.ims.servicemodules.im.event.SlmIncomingMessageEvent r23) {
        /*
            r22 = this;
            r0 = r22
            r7 = r23
            java.lang.String r1 = LOG_TAG
            java.lang.StringBuilder r2 = new java.lang.StringBuilder
            r2.<init>()
            java.lang.String r3 = "onIncomingSlmMessageReceived: "
            r2.append(r3)
            r2.append(r7)
            java.lang.String r2 = r2.toString()
            android.util.Log.i(r1, r2)
            com.sec.internal.ims.servicemodules.im.ImModule r2 = r0.mImModule
            java.lang.String r3 = r7.mOwnImsi
            int r15 = r2.getPhoneIdByIMSI(r3)
            com.sec.internal.ims.servicemodules.im.ImSessionProcessor r2 = r0.mImSessionProcessor
            java.util.List<com.sec.ims.util.ImsUri> r3 = r7.mParticipants
            com.sec.ims.util.ImsUri r4 = r7.mSender
            java.util.Set r2 = r2.getNormalizedParticipants(r15, r3, r4)
            int r3 = r2.size()
            r14 = 0
            r13 = 1
            if (r3 <= r13) goto L_0x0036
            r3 = r13
            goto L_0x0037
        L_0x0036:
            r3 = r14
        L_0x0037:
            boolean r4 = r7.mIsLMM
            if (r4 != 0) goto L_0x0077
            com.sec.internal.ims.servicemodules.im.ImModule r4 = r0.mImModule
            com.sec.ims.util.ImsUri r5 = r7.mSender
            boolean r4 = r4.isBlockedNumber(r15, r5, r3)
            if (r4 == 0) goto L_0x0059
            com.sec.internal.interfaces.ims.servicemodules.im.ISlmServiceInterface r0 = r0.mSlmService
            com.sec.internal.constants.ims.servicemodules.im.params.RejectSlmParams r8 = new com.sec.internal.constants.ims.servicemodules.im.params.RejectSlmParams
            r2 = 0
            java.lang.Object r3 = r7.mRawHandle
            com.sec.internal.constants.ims.servicemodules.im.reason.ImSessionRejectReason r4 = com.sec.internal.constants.ims.servicemodules.im.reason.ImSessionRejectReason.BUSY_HERE
            r5 = 0
            java.lang.String r6 = r7.mOwnImsi
            r1 = r8
            r1.<init>(r2, r3, r4, r5, r6)
            r0.rejectSlm(r8)
            return
        L_0x0059:
            com.sec.internal.interfaces.ims.servicemodules.im.ISlmServiceInterface r4 = r0.mSlmService
            com.sec.internal.constants.ims.servicemodules.im.params.AcceptSlmParams r5 = new com.sec.internal.constants.ims.servicemodules.im.params.AcceptSlmParams
            r17 = 0
            com.sec.internal.ims.servicemodules.im.ImModule r6 = r0.mImModule
            java.lang.String r18 = r6.getUserAlias(r15, r14)
            java.lang.Object r6 = r7.mRawHandle
            r20 = 0
            java.lang.String r8 = r7.mOwnImsi
            r16 = r5
            r19 = r6
            r21 = r8
            r16.<init>(r17, r18, r19, r20, r21)
            r4.acceptSlm(r5)
        L_0x0077:
            if (r3 == 0) goto L_0x007c
            com.sec.internal.constants.ims.servicemodules.im.ChatData$ChatType r3 = com.sec.internal.constants.ims.servicemodules.im.ChatData.ChatType.PARTICIPANT_BASED_GROUP_CHAT
            goto L_0x007e
        L_0x007c:
            com.sec.internal.constants.ims.servicemodules.im.ChatData$ChatType r3 = com.sec.internal.constants.ims.servicemodules.im.ChatData.ChatType.ONE_TO_ONE_CHAT
        L_0x007e:
            com.sec.internal.constants.ims.servicemodules.im.ImCpimNamespaces r4 = r7.mCpimNamespaces
            com.sec.internal.constants.ims.servicemodules.im.ImDirection r4 = com.sec.internal.ims.servicemodules.im.util.ImCpimNamespacesHelper.extractImDirection(r15, r4)
            com.sec.internal.ims.servicemodules.im.ImCache r5 = r0.mCache
            java.lang.String r6 = r7.mOwnImsi
            com.sec.internal.constants.ims.servicemodules.im.ChatMode r8 = com.sec.internal.constants.ims.servicemodules.im.ChatMode.OFF
            com.sec.internal.ims.servicemodules.im.ImSession r5 = r5.getImSessionByParticipants(r2, r3, r6, r8)
            if (r5 != 0) goto L_0x0098
            com.sec.internal.ims.servicemodules.im.ImCache r5 = r0.mCache
            java.lang.String r6 = r7.mOwnImsi
            com.sec.internal.ims.servicemodules.im.ImSession r5 = r5.makeNewEmptySession(r6, r2, r3, r4)
        L_0x0098:
            r12 = r5
            com.sec.internal.ims.servicemodules.im.ImModule r2 = r0.mImModule
            com.sec.internal.ims.servicemodules.im.ImDump r2 = r2.getImDump()
            java.lang.StringBuilder r3 = new java.lang.StringBuilder
            r3.<init>()
            java.lang.String r5 = "onIncomingSlmMessageReceived: chatId="
            r3.append(r5)
            java.lang.String r5 = r12.getChatId()
            r3.append(r5)
            java.lang.String r5 = ", convId="
            r3.append(r5)
            java.lang.String r5 = r12.getConversationId()
            r3.append(r5)
            java.lang.String r5 = ", contId="
            r3.append(r5)
            java.lang.String r5 = r12.getContributionId()
            r3.append(r5)
            java.lang.String r5 = ", imdnId="
            r3.append(r5)
            java.lang.String r5 = r7.mImdnMessageId
            r3.append(r5)
            java.lang.String r3 = r3.toString()
            r2.addEventLogs(r3)
            boolean r2 = r12.isGroupChat()
            if (r2 != 0) goto L_0x00f0
            com.sec.internal.ims.servicemodules.im.ImSessionProcessor r2 = r0.mImSessionProcessor
            com.sec.ims.util.ImsUri r3 = r12.getRemoteUri()
            com.sec.internal.constants.ims.servicemodules.im.ChatData r5 = r12.getChatData()
            java.lang.String r5 = r5.getOwnIMSI()
            r2.setLegacyLatching(r3, r14, r5)
        L_0x00f0:
            java.lang.String r2 = r7.mContributionId
            r12.setContributionId(r2)
            java.lang.String r2 = r7.mConversationId
            r12.setConversationId(r2)
            java.lang.String r2 = r7.mContributionId
            r12.setInReplyToContributionId(r2)
            com.sec.internal.ims.servicemodules.im.ImModule r2 = r0.mImModule
            java.lang.String r2 = r2.getOwnPhoneNum(r15)
            r12.setOwnPhoneNum(r2)
            com.sec.internal.ims.servicemodules.im.ImModule r2 = r0.mImModule
            java.lang.String r2 = r2.getImsiFromPhoneId(r15)
            r12.setOwnImsi(r2)
            boolean r2 = r7.mIsTokenUsed
            r12.setIsTokenUsed(r2)
            com.sec.internal.ims.servicemodules.im.ImCache r2 = r0.mCache
            java.lang.String r3 = r7.mImdnMessageId
            java.lang.String r5 = r12.getChatId()
            com.sec.internal.ims.servicemodules.im.MessageBase r2 = r2.getMessage(r3, r4, r5)
            java.lang.String r3 = r7.mBody
            if (r3 == 0) goto L_0x0172
            java.lang.String r3 = r7.mContentType
            if (r3 == 0) goto L_0x0172
            boolean r3 = com.sec.internal.ims.servicemodules.im.ImMultipart.isMultipart(r3)
            if (r3 == 0) goto L_0x0172
            java.lang.String r3 = "onIncomingSlmMessage: isMultipart"
            android.util.Log.i(r1, r3)
            com.sec.internal.ims.servicemodules.im.ImMultipart r3 = new com.sec.internal.ims.servicemodules.im.ImMultipart
            java.lang.String r4 = r7.mBody
            java.lang.String r5 = r7.mContentType
            r3.<init>(r4, r5)
            java.lang.String r4 = r3.getSuggestion()
            boolean r4 = android.text.TextUtils.isEmpty(r4)
            if (r4 != 0) goto L_0x0172
            java.lang.String r4 = "onIncomingSlmMessage: message includes suggestion"
            android.util.Log.i(r1, r4)
            java.lang.String r4 = r3.getBody()
            r7.mBody = r4
            java.lang.String r4 = r3.getContentType()
            r7.mContentType = r4
            java.lang.String r3 = r3.getSuggestion()
            java.lang.StringBuilder r4 = new java.lang.StringBuilder
            r4.<init>()
            java.lang.String r5 = "onIncomingSlmMessage: suggestion ="
            r4.append(r5)
            r4.append(r3)
            java.lang.String r4 = r4.toString()
            android.util.Log.i(r1, r4)
            goto L_0x0173
        L_0x0172:
            r3 = 0
        L_0x0173:
            r6 = r3
            if (r2 == 0) goto L_0x0180
            boolean r2 = r7.mIsPublicAccountMsg
            if (r2 != 0) goto L_0x0180
            java.lang.String r0 = "duplicate message, ignore"
            android.util.Log.e(r1, r0)
            return
        L_0x0180:
            java.lang.String r1 = r7.mContentType
            if (r1 == 0) goto L_0x01ac
            java.lang.String r2 = "application/vnd.gsma.rcs-ft-http+xml"
            boolean r1 = r1.startsWith(r2)
            if (r1 == 0) goto L_0x01ac
            com.sec.internal.ims.servicemodules.im.ImCache r1 = r0.mCache
            java.lang.String r2 = r12.getOwnImsi()
            com.sec.internal.ims.servicemodules.im.ImModule r3 = r0.mImModule
            com.sec.internal.ims.servicemodules.im.ImConfig r4 = r3.getImConfig(r15)
            boolean r4 = r4.isFtHttpOverDefaultPdn()
            android.net.Network r5 = r3.getNetwork(r4, r15)
            r3 = r12
            r4 = r23
            com.sec.internal.ims.servicemodules.im.FtHttpIncomingMessage r1 = r1.makeNewIncomingFtHttpMessage((java.lang.String) r2, (com.sec.internal.ims.servicemodules.im.ImSession) r3, (com.sec.internal.constants.ims.servicemodules.im.event.SlmIncomingMessageEvent) r4, (android.net.Network) r5, (java.lang.String) r6)
            r10 = r12
            r9 = r13
            r16 = r14
            goto L_0x01ef
        L_0x01ac:
            boolean r1 = r7.mIsRoutingMsg
            if (r1 == 0) goto L_0x01da
            com.sec.internal.ims.servicemodules.im.ImModule r8 = r0.mImModule
            com.sec.ims.util.ImsUri r9 = r7.mRequestUri
            com.sec.ims.util.ImsUri r10 = r7.mPAssertedId
            com.sec.ims.util.ImsUri r11 = r7.mSender
            com.sec.ims.util.ImsUri r1 = r7.mReceiver
            boolean r2 = r12.isGroupChat()
            r5 = r12
            r12 = r1
            r4 = r13
            r13 = r2
            r16 = r14
            r14 = r15
            com.sec.internal.constants.ims.servicemodules.im.RoutingType r1 = r8.getMsgRoutingType(r9, r10, r11, r12, r13, r14)
            r7.mRoutingType = r1
            com.sec.internal.constants.ims.servicemodules.im.RoutingType r2 = com.sec.internal.constants.ims.servicemodules.im.RoutingType.SENT
            if (r1 != r2) goto L_0x01de
            boolean r1 = r5.isGroupChat()
            if (r1 != 0) goto L_0x01de
            com.sec.ims.util.ImsUri r1 = r7.mReceiver
            r7.mSender = r1
            goto L_0x01de
        L_0x01da:
            r5 = r12
            r4 = r13
            r16 = r14
        L_0x01de:
            com.sec.internal.ims.servicemodules.im.ImCache r1 = r0.mCache
            java.lang.String r2 = r5.getOwnImsi()
            r8 = 0
            r3 = r5
            r9 = r4
            r4 = r23
            r10 = r5
            r5 = r8
            com.sec.internal.ims.servicemodules.im.ImMessage r1 = r1.makeNewIncomingMessage((java.lang.String) r2, (com.sec.internal.ims.servicemodules.im.ImSession) r3, (com.sec.internal.constants.ims.servicemodules.im.event.SlmIncomingMessageEvent) r4, (android.net.Network) r5, (java.lang.String) r6)
        L_0x01ef:
            boolean r2 = r10.isGroupChat()
            if (r2 != 0) goto L_0x0201
            boolean r2 = r7.mIsChatbotRole
            if (r2 == 0) goto L_0x0201
            com.sec.internal.constants.ims.servicemodules.im.ImConstants$ChatbotMessagingTech r2 = com.sec.internal.constants.ims.servicemodules.im.ImConstants.ChatbotMessagingTech.STANDALONE_MESSAGING
            r1.setChatbotMessagingTech(r2)
            r10.updateIsChatbotRole(r9)
        L_0x0201:
            boolean r2 = r10.isGroupChat()
            if (r2 != 0) goto L_0x021f
            com.sec.internal.ims.servicemodules.im.ImModule r2 = r0.mImModule
            int r2 = r2.mKnoxBlockState
            if (r2 != r9) goto L_0x021f
            com.sec.ims.util.ImsUri r2 = r10.getRemoteUri()
            java.lang.String r2 = r2.getMsisdn()
            com.sec.internal.constants.ims.servicemodules.im.ImDirection r3 = com.sec.internal.constants.ims.servicemodules.im.ImDirection.INCOMING
            boolean r2 = com.sec.internal.helper.BlockedNumberUtil.isKnoxBlockedNumber(r2, r3)
            if (r2 == 0) goto L_0x021f
            r14 = r9
            goto L_0x0221
        L_0x021f:
            r14 = r16
        L_0x0221:
            r10.mIsBlockedIncomingSession = r14
            r10.receiveSlmMessage(r1)
            com.sec.internal.ims.servicemodules.im.ImSessionProcessor r0 = r0.mImSessionProcessor
            com.sec.internal.ims.servicemodules.im.interfaces.IRcsBigDataProcessor r0 = r0.getBigDataProcessor()
            r0.onMessageReceived(r15, r1, r10)
            return
        */
        throw new UnsupportedOperationException("Method not decompiled: com.sec.internal.ims.servicemodules.im.ImProcessor.onIncomingSlmMessage(com.sec.internal.constants.ims.servicemodules.im.event.SlmIncomingMessageEvent):void");
    }

    /* access modifiers changed from: protected */
    public void onProcessPendingMessages(int i) {
        Log.i(LOG_TAG, "EVENT_PROCESS_PENDING_MESSAGES");
        int intSetting = RcsPolicyManager.getRcsStrategy(i).intSetting(RcsPolicySettings.RcsPolicy.NUM_OF_DISPLAY_NOTIFICATION_ATONCE);
        for (ImSession next : this.mCache.getAllImSessions()) {
            if (this.mImModule.isRegistered(i)) {
                next.processPendingMessages(i);
                List<MessageBase> messagesForPendingNotificationByChatId = this.mCache.getMessagesForPendingNotificationByChatId(next.getChatId());
                String str = LOG_TAG;
                Log.i(str, "pending notification list size : " + messagesForPendingNotificationByChatId.size() + " limit : " + intSetting);
                if (intSetting > 0) {
                    Iterator<E> it = CollectionUtils.partition(messagesForPendingNotificationByChatId, intSetting).iterator();
                    int i2 = 0;
                    while (it.hasNext()) {
                        postDelayed(new ImProcessor$$ExternalSyntheticLambda4(this, i, next, (List) it.next()), ((long) i2) * 1000);
                        i2++;
                    }
                } else {
                    next.processPendingNotifications(messagesForPendingNotificationByChatId);
                }
            }
        }
    }

    /* access modifiers changed from: private */
    public /* synthetic */ void lambda$onProcessPendingMessages$7(int i, ImSession imSession, List list) {
        if (this.mImModule.isRegistered(i)) {
            imSession.processPendingNotifications(list);
        }
    }

    /* access modifiers changed from: protected */
    public Collection<IMessageEventListener> getMessageEventListener(ImConstants.Type type) {
        return this.mMessageEventListeners.get(type);
    }

    private boolean isDuplicateMessage(int i, ImSession imSession, ImIncomingMessageEvent imIncomingMessageEvent) {
        MessageBase message = this.mCache.getMessage(imIncomingMessageEvent.mImdnMessageId, ImCpimNamespacesHelper.extractImDirection(i, imIncomingMessageEvent.mCpimNamespaces), imSession.getChatId());
        if (message == null) {
            return false;
        }
        String str = LOG_TAG;
        Log.e(str, "Duplicated message: " + message);
        if (!message.isDeliveredNotificationRequired()) {
            return true;
        }
        ImDump imDump = this.mImModule.getImDump();
        imDump.addEventLogs("sendDeliveredNotification: chatId=" + imSession.getChatId() + ", convId=" + imSession.getConversationId() + ", contId=" + imSession.getContributionId() + ", imdnId=" + imIncomingMessageEvent.mImdnMessageId);
        imSession.sendDeliveredNotification(message);
        return true;
    }

    private void updateMessageSenderAlias(int i, ImSession imSession, ImIncomingMessageEvent imIncomingMessageEvent) {
        ImsUri normalizeUri = this.mImModule.normalizeUri(i, imIncomingMessageEvent.mSender);
        if (normalizeUri != null) {
            if (!this.mImModule.getImConfig(i).getUserAliasEnabled()) {
                imIncomingMessageEvent.mUserAlias = "";
            } else if (!imSession.isGroupChat() && imIncomingMessageEvent.mUserAlias.isEmpty() && !this.mImModule.getImConfig(i).getRealtimeUserAliasAuth()) {
                ImParticipant participant = imSession.getParticipant(normalizeUri);
                if (participant == null) {
                    IMSLog.e(LOG_TAG, "Participant is null");
                } else {
                    imIncomingMessageEvent.mUserAlias = participant.getUserAlias();
                }
            }
            if (imSession.isGroupChat() || this.mImModule.getImConfig(i).getRealtimeUserAliasAuth()) {
                imSession.updateParticipantAlias(imIncomingMessageEvent.mUserAlias, imSession.getParticipant(normalizeUri));
            }
        }
    }
}
