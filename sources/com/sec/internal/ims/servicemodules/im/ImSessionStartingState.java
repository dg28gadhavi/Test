package com.sec.internal.ims.servicemodules.im;

import android.os.Message;
import android.text.TextUtils;
import android.util.Log;
import com.sec.ims.options.Capabilities;
import com.sec.ims.settings.ImsProfile;
import com.sec.ims.util.ImsUri;
import com.sec.internal.constants.ims.servicemodules.im.ChatData;
import com.sec.internal.constants.ims.servicemodules.im.ImConstants;
import com.sec.internal.constants.ims.servicemodules.im.ImDirection;
import com.sec.internal.constants.ims.servicemodules.im.ImError;
import com.sec.internal.constants.ims.servicemodules.im.ImParticipant;
import com.sec.internal.constants.ims.servicemodules.im.NotificationStatus;
import com.sec.internal.constants.ims.servicemodules.im.event.ImIncomingSessionEvent;
import com.sec.internal.constants.ims.servicemodules.im.event.ImSessionClosedEvent;
import com.sec.internal.constants.ims.servicemodules.im.event.ImSessionConferenceInfoUpdateEvent;
import com.sec.internal.constants.ims.servicemodules.im.params.RejectImSessionParams;
import com.sec.internal.constants.ims.servicemodules.im.params.SendMessageParams;
import com.sec.internal.constants.ims.servicemodules.im.reason.ImSessionClosedReason;
import com.sec.internal.constants.ims.servicemodules.im.reason.ImSessionRejectReason;
import com.sec.internal.constants.ims.servicemodules.im.reason.ImSessionStopReason;
import com.sec.internal.constants.ims.servicemodules.im.result.Result;
import com.sec.internal.constants.ims.servicemodules.im.result.StartImSessionResult;
import com.sec.internal.helper.AsyncResult;
import com.sec.internal.helper.SimUtil;
import com.sec.internal.ims.rcs.RcsPolicyManager;
import com.sec.internal.ims.registry.ImsRegistry;
import com.sec.internal.ims.servicemodules.im.ImSession;
import com.sec.internal.ims.servicemodules.im.data.event.ImSessionEvent;
import com.sec.internal.ims.servicemodules.im.data.info.ImSessionInfo;
import com.sec.internal.ims.servicemodules.im.strategy.IMnoStrategy;
import com.sec.internal.ims.settings.RcsPolicySettings;
import com.sec.internal.ims.util.ChatbotUriUtil;
import com.sec.internal.ims.util.ImsUtil;
import com.sec.internal.ims.util.StringIdGenerator;
import com.sec.internal.ims.util.UriGenerator;
import com.sec.internal.imscr.LogClass;
import com.sec.internal.interfaces.ims.servicemodules.options.ICapabilityDiscoveryModule;
import com.sec.internal.log.IMSLog;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Date;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;

public class ImSessionStartingState extends ImSessionStateBase {
    private static final String LOG_TAG = "StartingState";

    ImSessionStartingState(int i, ImSession imSession) {
        super(i, imSession);
    }

    public void enter() {
        ImSession imSession = this.mImSession;
        imSession.logi("StartingState enter. " + this.mImSession.getChatId());
        ImSession imSession2 = this.mImSession;
        imSession2.mListener.onChatStatusUpdate(imSession2, ImSession.SessionState.STARTING);
        ImSession imSession3 = this.mImSession;
        imSession3.mClosedReason = ImSessionClosedReason.NONE;
        if (!imSession3.isVoluntaryDeparture() && !this.mImSession.isAutoRejoinSession()) {
            this.mImSession.getChatData().updateState(ChatData.State.INACTIVE);
        }
    }

    /* access modifiers changed from: protected */
    public boolean processMessagingEvent(Message message) {
        ImSession imSession = this.mImSession;
        imSession.logi("StartingState, processMessagingEvent: " + message.what + " ChatId: " + this.mImSession.getChatId());
        int i = message.what;
        if (i == 3001) {
            onSendMessage((MessageBase) message.obj);
        } else if (i == 3010) {
            onSendDeliveredNotification((MessageBase) message.obj);
        } else if (i != 3025) {
            return false;
        } else {
            onSendCanceledNotification((List) message.obj);
        }
        return true;
    }

    /* access modifiers changed from: protected */
    public boolean processGroupChatManagementEvent(Message message) {
        ImSession imSession = this.mImSession;
        imSession.logi("StartingState, processGroupChatManagementEvent: " + message.what + " ChatId: " + this.mImSession.getChatId());
        int i = message.what;
        if (!(i == 2001 || i == 2008 || i == 2010 || i == 2012 || i == 2014)) {
            if (i == 2005) {
                this.mImSession.onConferenceInfoUpdated((ImSessionConferenceInfoUpdateEvent) message.obj);
                return true;
            } else if (i != 2006) {
                return false;
            }
        }
        message.arg1 = 1;
        this.mImSession.deferMessage(message);
        return true;
    }

    /* access modifiers changed from: protected */
    public boolean processSessionConnectionEvent(Message message) {
        ImSession imSession = this.mImSession;
        imSession.logi("StartingState, processSessionConnectionEvent: " + message.what + " ChatId: " + this.mImSession.getChatId());
        int i = message.what;
        if (i == 1002) {
            onStartSessionDone(message);
        } else if (i == 1007) {
            onAcceptSessionDone(message);
        } else if (i == 1016) {
            onStartSessionProvisionalResponse((StartImSessionResult) ((AsyncResult) message.obj).result);
        } else if (i == 1004) {
            this.mImSession.onEstablishmentTimeOut(message.obj);
        } else if (i == 1005) {
            onProcessIncomingSession((ImIncomingSessionEvent) message.obj);
        } else if (i == 1012) {
            return onCloseAllSession(message);
        } else {
            if (i != 1013) {
                return false;
            }
            this.mImSession.mClosedState.onCloseSessionDone(message);
        }
        return true;
    }

    /* JADX WARNING: Removed duplicated region for block: B:17:0x00b2  */
    /* JADX WARNING: Removed duplicated region for block: B:18:0x00bb  */
    /* JADX WARNING: Removed duplicated region for block: B:21:0x00e0  */
    /* JADX WARNING: Removed duplicated region for block: B:32:0x0177  */
    /* JADX WARNING: Removed duplicated region for block: B:33:0x017a  */
    /* JADX WARNING: Removed duplicated region for block: B:40:0x01b9  */
    /* JADX WARNING: Removed duplicated region for block: B:41:0x01bf  */
    /* JADX WARNING: Removed duplicated region for block: B:44:0x01ca  */
    /* Code decompiled incorrectly, please refer to instructions dump. */
    public void onStartSession(com.sec.internal.ims.servicemodules.im.MessageBase r34, com.sec.internal.ims.servicemodules.im.data.info.ImSessionInfo.StartingReason r35, boolean r36) {
        /*
            r33 = this;
            r0 = r33
            r1 = r34
            r2 = r35
            r15 = r36
            com.sec.internal.ims.servicemodules.im.ImSession r3 = r0.mImSession
            java.lang.String r4 = "onStartSession"
            r3.logi(r4)
            com.sec.internal.ims.servicemodules.im.ImSession r3 = r0.mImSession
            java.util.Set r3 = r3.getParticipantsUri()
            com.sec.internal.ims.servicemodules.im.ImSession r4 = r0.mImSession
            boolean r4 = r4.isGroupChat()
            r14 = 1
            if (r4 != 0) goto L_0x002b
            int r4 = r3.size()
            if (r4 <= r14) goto L_0x002b
            com.sec.internal.ims.servicemodules.im.ImSession r4 = r0.mImSession
            com.sec.internal.constants.ims.servicemodules.im.ChatData$ChatType r5 = com.sec.internal.constants.ims.servicemodules.im.ChatData.ChatType.REGULAR_GROUP_CHAT
            r4.updateChatType(r5)
        L_0x002b:
            int r4 = r3.size()
            r0.dumpOnStartSession(r4, r2, r15)
            java.util.List r6 = r0.generateReceivers(r3)
            com.sec.internal.ims.servicemodules.im.strategy.IMnoStrategy$StrategyResponse r3 = r0.preCheckToStartSession(r1, r3, r6)
            com.sec.internal.ims.servicemodules.im.strategy.IMnoStrategy$StatusCode r4 = r3.getStatusCode()
            com.sec.internal.ims.servicemodules.im.strategy.IMnoStrategy$StatusCode r5 = com.sec.internal.ims.servicemodules.im.strategy.IMnoStrategy.StatusCode.NONE
            if (r4 == r5) goto L_0x0046
            r0.handleStartSessionFailure(r1, r3)
            return
        L_0x0046:
            if (r1 == 0) goto L_0x0066
            com.sec.internal.ims.servicemodules.im.ImSession r3 = r0.mImSession
            java.lang.String r4 = r34.getBody()
            boolean r3 = r3.isFirstMessageInStart(r4)
            if (r3 == 0) goto L_0x005f
            com.sec.internal.ims.servicemodules.im.ImSession r3 = r0.mImSession
            r3.setSessionTimeoutThreshold(r1)
            com.sec.internal.constants.ims.servicemodules.im.params.SendMessageParams r3 = r33.createFirstMessageParams(r34)
            r12 = r3
            goto L_0x0067
        L_0x005f:
            com.sec.internal.ims.servicemodules.im.ImSession r3 = r0.mImSession
            java.util.List<com.sec.internal.ims.servicemodules.im.MessageBase> r3 = r3.mCurrentMessages
            r3.add(r1)
        L_0x0066:
            r12 = 0
        L_0x0067:
            r33.generateSessionIds()
            java.lang.String r3 = com.sec.internal.ims.util.StringIdGenerator.generateUuid()
            com.sec.internal.ims.servicemodules.im.ImSession r4 = r0.mImSession
            java.lang.StringBuilder r5 = new java.lang.StringBuilder
            r5.<init>()
            java.lang.String r7 = "onStartSession, sendMessage IM. temporary sessionKey : "
            r5.append(r7)
            r5.append(r3)
            java.lang.String r7 = ", msgParams : "
            r5.append(r7)
            r5.append(r12)
            java.lang.String r5 = r5.toString()
            r4.logi(r5)
            com.sec.internal.ims.servicemodules.im.ImSession r4 = r0.mImSession
            r5 = 1002(0x3ea, float:1.404E-42)
            android.os.Message r25 = r4.obtainMessage((int) r5, (java.lang.Object) r1)
            com.sec.internal.ims.servicemodules.im.ImSession r4 = r0.mImSession
            r5 = 1017(0x3f9, float:1.425E-42)
            android.os.Message r26 = r4.obtainMessage((int) r5, (java.lang.Object) r3)
            com.sec.internal.ims.servicemodules.im.ImSession r4 = r0.mImSession
            r5 = 1016(0x3f8, float:1.424E-42)
            android.os.Message r28 = r4.obtainMessage(r5)
            com.sec.internal.ims.servicemodules.im.data.info.ImSessionInfo r4 = new com.sec.internal.ims.servicemodules.im.data.info.ImSessionInfo
            com.sec.internal.ims.servicemodules.im.data.info.ImSessionInfo$ImSessionState r18 = com.sec.internal.ims.servicemodules.im.data.info.ImSessionInfo.ImSessionState.INITIAL
            com.sec.internal.constants.ims.servicemodules.im.ImDirection r19 = com.sec.internal.constants.ims.servicemodules.im.ImDirection.OUTGOING
            com.sec.internal.ims.servicemodules.im.ImSession r5 = r0.mImSession
            boolean r5 = r5.isRejoinable()
            if (r5 == 0) goto L_0x00bb
            com.sec.internal.ims.servicemodules.im.ImSession r5 = r0.mImSession
            com.sec.ims.util.ImsUri r5 = r5.getSessionUri()
            r20 = r5
            goto L_0x00bd
        L_0x00bb:
            r20 = 0
        L_0x00bd:
            com.sec.internal.ims.servicemodules.im.ImSession r5 = r0.mImSession
            java.lang.String r21 = r5.getContributionId()
            com.sec.internal.ims.servicemodules.im.ImSession r5 = r0.mImSession
            java.lang.String r22 = r5.getConversationId()
            com.sec.internal.ims.servicemodules.im.ImSession r5 = r0.mImSession
            java.lang.String r23 = r5.getInReplyToContributionId()
            com.sec.internal.ims.servicemodules.im.ImSession r5 = r0.mImSession
            java.lang.String r24 = r5.getSdpContentType()
            r16 = r4
            r17 = r3
            r16.<init>(r17, r18, r19, r20, r21, r22, r23, r24)
            r4.mIsTryToLeave = r15
            if (r15 == 0) goto L_0x00e8
            com.sec.internal.ims.servicemodules.im.ImSession r3 = r0.mImSession
            com.sec.internal.ims.servicemodules.im.ImSessionClosedState r3 = r3.mClosedState
            com.sec.internal.constants.ims.servicemodules.im.reason.ImSessionStopReason r5 = com.sec.internal.constants.ims.servicemodules.im.reason.ImSessionStopReason.VOLUNTARILY
            r3.mStopReason = r5
        L_0x00e8:
            r4.mStartingReason = r2
            com.sec.internal.ims.servicemodules.im.ImSession r3 = r0.mImSession
            r3.addImSessionInfo(r4)
            com.sec.internal.ims.servicemodules.im.ImSession r3 = r0.mImSession
            r3.updateSessionInfo(r4)
            r33.checkIconUpdateRequired()
            com.sec.internal.ims.servicemodules.im.ImSession r3 = r0.mImSession
            java.lang.String r3 = r3.getSdpContentType()
            if (r3 != 0) goto L_0x010b
            if (r1 == 0) goto L_0x010b
            java.lang.String r4 = r34.getContentType()
            if (r4 == 0) goto L_0x010b
            java.lang.String r3 = r34.getContentType()
        L_0x010b:
            r16 = r3
            com.sec.internal.constants.ims.servicemodules.im.params.StartImSessionParams r11 = new com.sec.internal.constants.ims.servicemodules.im.params.StartImSessionParams
            com.sec.internal.ims.servicemodules.im.ImSession r3 = r0.mImSession
            java.lang.String r4 = r3.getChatId()
            com.sec.internal.ims.servicemodules.im.ImSession r3 = r0.mImSession
            java.lang.String r5 = r3.getSubject()
            com.sec.internal.ims.servicemodules.im.ImSession r3 = r0.mImSession
            java.lang.String r7 = r3.getContributionId()
            com.sec.internal.ims.servicemodules.im.ImSession r3 = r0.mImSession
            java.lang.String r9 = r3.getUserAlias()
            com.sec.internal.constants.ims.servicemodules.im.params.StartImSessionParams$ServiceType r10 = com.sec.internal.constants.ims.servicemodules.im.params.StartImSessionParams.ServiceType.NORMAL
            com.sec.internal.ims.servicemodules.im.ImSession r3 = r0.mImSession
            boolean r17 = r3.isGroupChat()
            com.sec.internal.ims.servicemodules.im.ImSession r3 = r0.mImSession
            java.lang.String r18 = r3.getConversationId()
            com.sec.internal.ims.servicemodules.im.ImSession r3 = r0.mImSession
            java.lang.String r19 = r3.getInReplyToContributionId()
            com.sec.internal.ims.servicemodules.im.ImSession r3 = r0.mImSession
            boolean r20 = r3.isRejoinable()
            com.sec.internal.ims.servicemodules.im.ImSession r3 = r0.mImSession
            com.sec.internal.constants.ims.servicemodules.im.ChatData$ChatType r3 = r3.getChatType()
            boolean r21 = com.sec.internal.constants.ims.servicemodules.im.ChatData.ChatType.isClosedGroupChat(r3)
            com.sec.internal.ims.servicemodules.im.ImSession r3 = r0.mImSession
            java.lang.String r22 = r3.getServiceId()
            com.sec.internal.ims.servicemodules.im.ImSession r3 = r0.mImSession
            java.util.List<java.lang.String> r8 = r3.mAcceptTypes
            r24 = r8
            java.util.List<java.lang.String> r8 = r3.mAcceptWrappedTypes
            com.sec.internal.constants.ims.servicemodules.im.ChatData r3 = r3.getChatData()
            java.lang.String r29 = r3.getOwnIMSI()
            com.sec.internal.ims.servicemodules.im.ImSession r3 = r0.mImSession
            boolean r3 = r3.isGroupChat()
            if (r3 != 0) goto L_0x017a
            com.sec.internal.ims.servicemodules.im.ImSession r3 = r0.mImSession
            java.util.Set r3 = r3.getParticipantsUri()
            int r13 = r0.mPhoneId
            boolean r3 = com.sec.internal.ims.util.ChatbotUriUtil.hasChatbotUri(r3, r13)
            if (r3 == 0) goto L_0x017a
            r30 = r14
            goto L_0x017d
        L_0x017a:
            r3 = 0
            r30 = r3
        L_0x017d:
            com.sec.internal.ims.servicemodules.im.ImSession r3 = r0.mImSession
            com.sec.internal.constants.ims.servicemodules.im.ChatMode r27 = r3.getChatMode()
            r3 = r11
            r23 = r24
            r24 = r8
            r8 = 0
            r13 = r11
            r11 = r17
            r31 = r12
            r12 = r16
            r32 = r13
            r13 = r25
            r14 = r28
            r15 = r26
            r16 = r31
            r17 = r18
            r18 = r19
            r19 = r20
            r20 = r21
            r21 = r36
            r25 = r29
            r26 = r30
            r3.<init>(r4, r5, r6, r7, r8, r9, r10, r11, r12, r13, r14, r15, r16, r17, r18, r19, r20, r21, r22, r23, r24, r25, r26, r27)
            if (r1 == 0) goto L_0x01bf
            java.lang.String r3 = r1.mContentType
            if (r3 == 0) goto L_0x01bf
            java.lang.String r4 = "application/vnd.gsma.rcspushlocation+xml"
            boolean r3 = r3.contains(r4)
            if (r3 == 0) goto L_0x01bf
            r3 = r32
            r4 = 1
            r3.mIsGeolocationPush = r4
            goto L_0x01c1
        L_0x01bf:
            r3 = r32
        L_0x01c1:
            com.sec.internal.ims.servicemodules.im.ImSession r4 = r0.mImSession
            com.sec.internal.interfaces.ims.servicemodules.im.IImServiceInterface r4 = r4.mImsService
            r4.startImSession(r3)
            if (r31 == 0) goto L_0x01cf
            com.sec.internal.ims.servicemodules.im.ImSession r3 = r0.mImSession
            r3.onMessageSending(r1)
        L_0x01cf:
            com.sec.internal.ims.servicemodules.im.ImSession r1 = r0.mImSession
            boolean r1 = r1.isRejoinable()
            if (r1 != 0) goto L_0x01e4
            if (r36 != 0) goto L_0x01e4
            com.sec.internal.ims.servicemodules.im.data.info.ImSessionInfo$StartingReason r1 = com.sec.internal.ims.servicemodules.im.data.info.ImSessionInfo.StartingReason.RESTARTING
            if (r2 == r1) goto L_0x01e4
            com.sec.internal.ims.servicemodules.im.ImSession r1 = r0.mImSession
            com.sec.internal.constants.ims.servicemodules.im.ImParticipant$Status r2 = com.sec.internal.constants.ims.servicemodules.im.ImParticipant.Status.INVITED
            r1.updateParticipantsStatus(r2)
        L_0x01e4:
            com.sec.internal.ims.servicemodules.im.ImSession r0 = r0.mImSession
            r1 = 0
            r0.mClosedEvent = r1
            r0.transitionToProperState()
            return
        */
        throw new UnsupportedOperationException("Method not decompiled: com.sec.internal.ims.servicemodules.im.ImSessionStartingState.onStartSession(com.sec.internal.ims.servicemodules.im.MessageBase, com.sec.internal.ims.servicemodules.im.data.info.ImSessionInfo$StartingReason, boolean):void");
    }

    private Set<ImsUri> getParticipantsNetworkPreferredUri(Set<ImsUri> set) {
        String str;
        ICapabilityDiscoveryModule capabilityDiscoveryModule = ImsRegistry.getServiceModuleManager().getCapabilityDiscoveryModule();
        HashSet hashSet = new HashSet();
        for (ImsUri next : set) {
            if (capabilityDiscoveryModule == null || capabilityDiscoveryModule.getCapabilitiesCache() == null) {
                hashSet.add(this.mImSession.mUriGenerator.getNetworkPreferredUri(UriGenerator.URIServiceType.RCS_URI, next));
            } else {
                Capabilities capabilities = capabilityDiscoveryModule.getCapabilitiesCache().get(next);
                if (capabilities == null) {
                    hashSet.add(this.mImSession.mUriGenerator.getNetworkPreferredUri(UriGenerator.URIServiceType.RCS_URI, next));
                } else {
                    Iterator it = capabilities.getPAssertedId().iterator();
                    while (true) {
                        if (!it.hasNext()) {
                            str = null;
                            break;
                        }
                        ImsUri imsUri = (ImsUri) it.next();
                        if (imsUri.getUriType() == ImsUri.UriType.SIP_URI) {
                            str = imsUri.getHost();
                            break;
                        }
                    }
                    hashSet.add(this.mImSession.mUriGenerator.getNetworkPreferredUri(UriGenerator.URIServiceType.RCS_URI, next, str));
                }
            }
        }
        return hashSet;
    }

    public void onStartSessionDone(Message message) {
        AsyncResult asyncResult = (AsyncResult) message.obj;
        StartImSessionResult startImSessionResult = (StartImSessionResult) asyncResult.result;
        ImError imError = startImSessionResult.mResult.getImError();
        this.mImSession.mRetryTimer = startImSessionResult.mRetryTimer;
        ArrayList arrayList = new ArrayList();
        arrayList.add(String.valueOf(imError.ordinal()));
        arrayList.add(startImSessionResult.toCriticalLog());
        ImsUtil.listToDumpFormat(LogClass.IM_START_SESSION_DONE, this.mPhoneId, this.mImSession.getChatId(), arrayList);
        ImSession imSession = this.mImSession;
        imSession.logi("onStartSessionDone : " + startImSessionResult);
        ImSessionInfo imSessionInfo = this.mImSession.getImSessionInfo(startImSessionResult.mRawHandle);
        if (imSessionInfo == null) {
            ImSession imSession2 = this.mImSession;
            imSession2.loge("onStartSessionDone unknown rawHandle : " + startImSessionResult.mRawHandle);
        } else if (imError == ImError.SUCCESS) {
            onStartSessionDoneSuccess(startImSessionResult, imSessionInfo);
        } else {
            onStartSessionDoneFailure(startImSessionResult, imSessionInfo, imError, (MessageBase) asyncResult.userObj);
        }
    }

    private boolean shouldRestartSessionWithNewID(ImError imError) {
        if (!this.mImSession.isGroupChat()) {
            return false;
        }
        int i = AnonymousClass1.$SwitchMap$com$sec$internal$constants$ims$servicemodules$im$ImError[imError.ordinal()];
        if ((i == 1 || i == 2) && this.mImSession.mConfig.getImMsgTech() == ImConstants.ImMsgTech.SIMPLE_IM) {
            return true;
        }
        return false;
    }

    /* renamed from: com.sec.internal.ims.servicemodules.im.ImSessionStartingState$1  reason: invalid class name */
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
                com.sec.internal.constants.ims.servicemodules.im.ImError r1 = com.sec.internal.constants.ims.servicemodules.im.ImError.FORBIDDEN_SERVICE_NOT_AUTHORISED     // Catch:{ NoSuchFieldError -> 0x0012 }
                int r1 = r1.ordinal()     // Catch:{ NoSuchFieldError -> 0x0012 }
                r2 = 1
                r0[r1] = r2     // Catch:{ NoSuchFieldError -> 0x0012 }
            L_0x0012:
                int[] r0 = $SwitchMap$com$sec$internal$constants$ims$servicemodules$im$ImError     // Catch:{ NoSuchFieldError -> 0x001d }
                com.sec.internal.constants.ims.servicemodules.im.ImError r1 = com.sec.internal.constants.ims.servicemodules.im.ImError.FORBIDDEN_VERSION_NOT_SUPPORTED     // Catch:{ NoSuchFieldError -> 0x001d }
                int r1 = r1.ordinal()     // Catch:{ NoSuchFieldError -> 0x001d }
                r2 = 2
                r0[r1] = r2     // Catch:{ NoSuchFieldError -> 0x001d }
            L_0x001d:
                return
            */
            throw new UnsupportedOperationException("Method not decompiled: com.sec.internal.ims.servicemodules.im.ImSessionStartingState.AnonymousClass1.<clinit>():void");
        }
    }

    private void restartSession(boolean z, boolean z2) {
        this.mImSession.setSessionUri((ImsUri) null);
        ImSessionInfo.StartingReason startingReason = ImSessionInfo.StartingReason.RESTARTING;
        if (z2) {
            if (this.mImSession.mConfig.getImMsgTech() == ImConstants.ImMsgTech.SIMPLE_IM) {
                this.mImSession.setContributionId(StringIdGenerator.generateContributionId());
            } else {
                String generateConversationId = StringIdGenerator.generateConversationId();
                this.mImSession.setConversationId(generateConversationId);
                ImSession imSession = this.mImSession;
                if (!imSession.isGroupChat()) {
                    generateConversationId = StringIdGenerator.generateContributionId();
                }
                imSession.setContributionId(generateConversationId);
            }
            startingReason = ImSessionInfo.StartingReason.RESTARTING_WITH_NEW_ID;
        }
        HashSet hashSet = new HashSet();
        for (ImsUri participant : this.mImSession.mGetter.getOwnUris(SimUtil.getSimSlotPriority())) {
            ImParticipant participant2 = this.mImSession.getParticipant(participant);
            if (participant2 != null) {
                participant2.setStatus(ImParticipant.Status.DECLINED);
                hashSet.add(participant2);
            }
        }
        if (!hashSet.isEmpty()) {
            Log.e(LOG_TAG, "restartSession: remove own uris from participants list");
            ImSession imSession2 = this.mImSession;
            imSession2.mListener.onParticipantsDeleted(imSession2, hashSet);
        }
        onStartSession((MessageBase) null, startingReason, z);
    }

    private void onSendMessage(MessageBase messageBase) {
        ImError imError;
        ImSessionInfo latestActiveImSessionInfo = this.mImSession.getLatestActiveImSessionInfo();
        if (!this.mImSession.isFirstMessageInStart(messageBase.getBody()) || !this.mImSession.mCurrentMessages.isEmpty() || (latestActiveImSessionInfo != null && (latestActiveImSessionInfo.mState != ImSessionInfo.ImSessionState.STARTING || latestActiveImSessionInfo.mLastProvisionalResponse == null))) {
            this.mImSession.logi("Starting Session, send message after session establishment");
            this.mImSession.mCurrentMessages.add(messageBase);
        } else if (!this.mImSession.getRcsStrategy(this.mPhoneId).boolSetting(RcsPolicySettings.RcsPolicy.TRIGGER_INVITE_AFTER_18X)) {
            onStartSession(messageBase, ImSessionInfo.StartingReason.NORMAL, false);
        } else if (latestActiveImSessionInfo == null || !((imError = latestActiveImSessionInfo.mLastProvisionalResponse) == ImError.RINGING || imError == ImError.CALL_IS_BEING_FORWARDED || imError == ImError.SESSION_PROGRESS)) {
            this.mImSession.mCurrentMessages.add(messageBase);
        } else {
            onStartSession(messageBase, ImSessionInfo.StartingReason.NORMAL, false);
        }
    }

    private void onSendCanceledNotification(List<MessageBase> list) {
        this.mImSession.logi("Starting Session, send canceled notification after session establishment");
        this.mImSession.mCurrentCanceledMessages.addAll(list);
    }

    private void onSendDeliveredNotification(MessageBase messageBase) {
        ImSessionInfo imSessionInfoByMessageId = this.mImSession.getImSessionInfoByMessageId(messageBase.getId());
        messageBase.sendDeliveredNotification((imSessionInfoByMessageId == null || !imSessionInfoByMessageId.isSnFSession() || imSessionInfoByMessageId.mState != ImSessionInfo.ImSessionState.ESTABLISHED) ? null : imSessionInfoByMessageId.mRawHandle, this.mImSession.getConversationId(), this.mImSession.getContributionId(), this.mImSession.obtainMessage((int) ImSessionEvent.SEND_DELIVERED_NOTIFICATION_DONE, (Object) messageBase), this.mImSession.getChatData().getOwnIMSI(), this.mImSession.isGroupChat(), this.mImSession.isBotSessionAnonymized());
    }

    private void onProcessIncomingSession(ImIncomingSessionEvent imIncomingSessionEvent) {
        ImSession imSession = this.mImSession;
        imSession.logi("race-condition : mRawHandle=" + imIncomingSessionEvent.mRawHandle);
        IMSLog.c(LogClass.IM_INCOMING_SESSION_ERR, " race : " + imIncomingSessionEvent.mRawHandle);
        if (this.mImSession.isVoluntaryDeparture()) {
            this.mImSession.logi("Explicit departure is in progress. Reject the incoming invite");
            this.mImSession.leaveSessionWithReject(imIncomingSessionEvent.mRawHandle);
            return;
        }
        if (!this.mImSession.isGroupChat()) {
            if (this.mImSession.getDirection() == ImDirection.OUTGOING) {
                ImSession imSession2 = this.mImSession;
                imSession2.mImsService.rejectImSession(new RejectImSessionParams(imSession2.getChatId(), imIncomingSessionEvent.mRawHandle, ImSessionRejectReason.BUSY_HERE, (Message) null));
                this.mImSession.onIncomingSessionProcessed(imIncomingSessionEvent.mReceivedMessage, false);
                return;
            }
            ImSession imSession3 = this.mImSession;
            imSession3.mClosedState.handleCloseSession(imSession3.getRawHandle(), ImSessionStopReason.INVOLUNTARILY);
        }
        ImSessionInfo addImSessionInfo = this.mImSession.addImSessionInfo(imIncomingSessionEvent, ImSessionInfo.ImSessionState.ACCEPTING);
        this.mImSession.updateSessionInfo(addImSessionInfo);
        this.mImSession.handleAcceptSession(addImSessionInfo);
        this.mImSession.onIncomingSessionProcessed(imIncomingSessionEvent.mReceivedMessage, false);
        this.mImSession.transitionToProperState();
        this.mImSession.releaseWakeLock(imIncomingSessionEvent.mRawHandle);
    }

    private boolean onCloseAllSession(Message message) {
        if (!this.mImSession.isVoluntaryDeparture()) {
            return false;
        }
        this.mImSession.logi("Voluntary departure in StartingState. DeferMessage.");
        this.mImSession.deferMessage(message);
        return true;
    }

    private void onStartSessionProvisionalResponse(StartImSessionResult startImSessionResult) {
        ImError imError;
        ImSessionInfo imSessionInfo = this.mImSession.getImSessionInfo(startImSessionResult.mRawHandle);
        ImSession imSession = this.mImSession;
        imSession.logi("START_SESSION_PROVISIONAL_RESPONSE : response=" + startImSessionResult);
        ImError imError2 = startImSessionResult.mResult.getImError();
        if (imSessionInfo != null) {
            if (!this.mImSession.mCurrentMessages.isEmpty()) {
                ImSession imSession2 = this.mImSession;
                if (imSession2.isFirstMessageInStart(imSession2.mCurrentMessages.get(0).getBody()) && imSessionInfo.equals(this.mImSession.getLatestActiveImSessionInfo()) && ((imError = imSessionInfo.mLastProvisionalResponse) == null || imError == ImError.TRYING)) {
                    if (this.mImSession.getRcsStrategy(this.mPhoneId).boolSetting(RcsPolicySettings.RcsPolicy.TRIGGER_INVITE_AFTER_18X)) {
                        if (imError2 == ImError.RINGING || imError2 == ImError.CALL_IS_BEING_FORWARDED || imError2 == ImError.SESSION_PROGRESS) {
                            onStartSession(this.mImSession.mCurrentMessages.remove(0), ImSessionInfo.StartingReason.NORMAL, false);
                        }
                    } else if (imSessionInfo.mLastProvisionalResponse == null) {
                        onStartSession(this.mImSession.mCurrentMessages.remove(0), ImSessionInfo.StartingReason.NORMAL, false);
                    }
                }
            }
            imSessionInfo.mLastProvisionalResponse = imError2;
        }
    }

    private void dumpOnStartSession(int i, ImSessionInfo.StartingReason startingReason, boolean z) {
        ArrayList arrayList = new ArrayList();
        arrayList.add(String.valueOf(i));
        String str = "1";
        arrayList.add(this.mImSession.isGroupChat() ? str : "0");
        arrayList.add(String.valueOf(startingReason.ordinal()));
        if (!z) {
            str = "0";
        }
        arrayList.add(str);
        ImsUtil.listToDumpFormat(LogClass.IM_START_SESSION, this.mPhoneId, this.mImSession.getChatId(), arrayList);
    }

    private IMnoStrategy.StrategyResponse preCheckToStartSession(MessageBase messageBase, Set<ImsUri> set, List<ImsUri> list) {
        IMnoStrategy.StrategyResponse strategyResponse;
        if (this.mImSession.isGroupChat() && !this.mImSession.mConfig.getGroupChatEnabled()) {
            strategyResponse = this.mImSession.getRcsStrategy(this.mPhoneId).handleImFailure(ImError.GROUPCHAT_DISABLED, this.mImSession.getChatType());
        } else if (this.mImSession.getChatType() != ChatData.ChatType.PARTICIPANT_BASED_GROUP_CHAT || !this.mImSession.getRcsStrategy(this.mPhoneId).boolSetting(RcsPolicySettings.RcsPolicy.ALLOW_ONLY_OPENGROUPCHAT)) {
            strategyResponse = this.mImSession.getRcsStrategy(this.mPhoneId).checkCapability(set, Capabilities.FEATURE_IM_SERVICE, this.mImSession.getChatType(), this.mImSession.isBroadcastMsg(messageBase));
        } else if (this.mImSession.mConfig.getSlmAuth() == ImConstants.SlmAuth.ENABLED) {
            strategyResponse = new IMnoStrategy.StrategyResponse(IMnoStrategy.StatusCode.FALLBACK_TO_SLM);
        } else {
            strategyResponse = new IMnoStrategy.StrategyResponse(IMnoStrategy.StatusCode.FALLBACK_TO_LEGACY);
        }
        if (messageBase != null && messageBase.getType() == ImConstants.Type.LOCATION && strategyResponse.getStatusCode() == IMnoStrategy.StatusCode.FALLBACK_TO_SLM) {
            this.mImSession.logi("onStartSession : GLS fallback to legacy");
            strategyResponse = new IMnoStrategy.StrategyResponse(IMnoStrategy.StatusCode.FALLBACK_TO_LEGACY);
        }
        if (list.isEmpty()) {
            this.mImSession.loge("onStartSession : Invalid receiver");
            strategyResponse = new IMnoStrategy.StrategyResponse(IMnoStrategy.StatusCode.DISPLAY_ERROR);
        }
        if (!this.mImSession.isGroupChat() && ChatbotUriUtil.hasChatbotUri(this.mImSession.getParticipantsUri(), this.mPhoneId) && (strategyResponse.getStatusCode() == IMnoStrategy.StatusCode.FALLBACK_TO_SLM || strategyResponse.getStatusCode() == IMnoStrategy.StatusCode.FALLBACK_TO_LEGACY)) {
            strategyResponse = new IMnoStrategy.StrategyResponse(IMnoStrategy.StatusCode.NO_RETRY);
        }
        ImSession imSession = this.mImSession;
        imSession.logi("onStartSession: statusCode=" + strategyResponse.getStatusCode());
        return strategyResponse;
    }

    private void handleStartSessionFailure(MessageBase messageBase, IMnoStrategy.StrategyResponse strategyResponse) {
        if (strategyResponse.getStatusCode() == IMnoStrategy.StatusCode.FALLBACK_TO_SLM) {
            if (messageBase == null) {
                this.mImSession.transitionToProperState();
            } else if (messageBase instanceof FtHttpOutgoingMessage) {
                this.mImSession.handleUploadedFileFallback((FtHttpOutgoingMessage) messageBase);
            } else {
                this.mImSession.logi("onStartSession, sendMessage SLM");
                ImSession imSession = this.mImSession;
                imSession.sendMessage(imSession.obtainMessage((int) ImSessionEvent.SEND_SLM_MESSAGE, (Object) messageBase));
            }
        } else if (messageBase != null) {
            this.mImSession.logi("onStartSession, display error or sendMessage error");
            messageBase.onSendMessageDone(new Result(ImError.REMOTE_TEMPORARILY_UNAVAILABLE, Result.Type.NONE), strategyResponse);
        } else {
            this.mImSession.transitionToProperState();
        }
    }

    private void generateSessionIds() {
        if (this.mImSession.mConfig.getImMsgTech() == ImConstants.ImMsgTech.CPM) {
            if (this.mImSession.getDirection() == ImDirection.INCOMING) {
                ImSession imSession = this.mImSession;
                imSession.setInReplyToContributionId(imSession.getContributionId());
                this.mImSession.setDirection(ImDirection.OUTGOING);
            }
            if (!this.mImSession.isGroupChat()) {
                this.mImSession.setContributionId(StringIdGenerator.generateContributionId());
            }
            if (TextUtils.isEmpty(this.mImSession.getConversationId())) {
                this.mImSession.setConversationId(StringIdGenerator.generateConversationId());
            }
            if (TextUtils.isEmpty(this.mImSession.getContributionId())) {
                ImSession imSession2 = this.mImSession;
                imSession2.setContributionId(imSession2.isGroupChat() ? this.mImSession.getConversationId() : StringIdGenerator.generateContributionId());
            }
        } else if (TextUtils.isEmpty(this.mImSession.getContributionId())) {
            this.mImSession.setContributionId(StringIdGenerator.generateContributionId());
        }
    }

    private List<ImsUri> generateReceivers(Set<ImsUri> set) {
        ArrayList arrayList = new ArrayList();
        if (this.mImSession.isRejoinable()) {
            arrayList.add(this.mImSession.getSessionUri());
        } else {
            arrayList.addAll(getParticipantsNetworkPreferredUri(set));
        }
        ImSession imSession = this.mImSession;
        if (imSession.mSwapUriType) {
            Set<ImsUri> swapUriType = imSession.mUriGenerator.swapUriType(arrayList);
            arrayList.clear();
            arrayList.addAll(swapUriType);
            this.mImSession.mSwapUriType = false;
        }
        return arrayList;
    }

    private void checkIconUpdateRequired() {
        this.mImSession.getChatData().setIconUpdatedRequiredOnSessionEstablished(this.mImSession.isGroupChat() && this.mImSession.getDirection() == ImDirection.OUTGOING && !this.mImSession.isRejoinable());
    }

    private void onStartSessionDoneSuccess(StartImSessionResult startImSessionResult, ImSessionInfo imSessionInfo) {
        imSessionInfo.mState = ImSessionInfo.ImSessionState.STARTED;
        imSessionInfo.mSessionUri = startImSessionResult.mSessionUri;
        if (this.mImSession.mEstablishedImSessionInfo.isEmpty()) {
            this.mImSession.updateSessionInfo(imSessionInfo);
        }
        this.mImSession.setNetworkFallbackMech(startImSessionResult.mIsMsgFallbackSupported, startImSessionResult.mIsMsgRevokeSupported);
        if (!this.mImSession.isMsgRevocationSupported() && !this.mImSession.getNeedToRevokeMessages().isEmpty()) {
            Map<String, Integer> needToRevokeMessages = this.mImSession.getNeedToRevokeMessages();
            for (String next : needToRevokeMessages.keySet()) {
                MessageBase message = this.mImSession.mGetter.getMessage(next, ImDirection.OUTGOING, (String) null);
                if (message != null) {
                    message.updateRevocationStatus(ImConstants.RevocationStatus.NONE);
                } else {
                    ImSession imSession = this.mImSession;
                    imSession.loge("message from mGetter is null. imdnId : " + next);
                }
            }
            this.mImSession.removeMsgFromListForRevoke((Collection<String>) needToRevokeMessages.keySet());
        }
        this.mImSession.updateIsChatbotRole(startImSessionResult.mIsChatbotRole);
        startSessionEstablishmentTimer(imSessionInfo.mRawHandle);
        if (this.mImSession.mConfig.getUserAliasEnabled() && !this.mImSession.mConfig.getRealtimeUserAliasAuth() && !this.mImSession.isGroupChat()) {
            this.mImSession.updateParticipantAlias(startImSessionResult.mRemoteUserDisplayName, this.mImSession.getParticipants().iterator().next());
        }
    }

    private void onStartSessionDoneFailure(StartImSessionResult startImSessionResult, ImSessionInfo imSessionInfo, ImError imError, MessageBase messageBase) {
        this.mImSession.getHandler().removeMessages(1004, imSessionInfo.mRawHandle);
        this.mImSession.removeImSessionInfo(imSessionInfo);
        if (this.mImSession.hasActiveImSessionInfo()) {
            this.mImSession.logi("onStartSessionDone : race condition, waiting events of another session");
            return;
        }
        MessageBase messageBase2 = null;
        if (imSessionInfo.mStartingReason == ImSessionInfo.StartingReason.AUTOMATIC_REJOINING) {
            this.mImSession.loge("onStartSessionDone : automatic rejoining was unsuccessful. Ignore the startResult");
            if (this.mImSession.getRcsStrategy(this.mPhoneId).needStopAutoRejoin(imError)) {
                if (imSessionInfo.mIsTryToLeave) {
                    this.mImSession.setSessionUri((ImsUri) null);
                } else {
                    this.mImSession.getChatData().updateState(ChatData.State.CLOSED_BY_USER);
                }
            }
            this.mImSession.transitionToProperState();
        } else if (this.mImSession.isRejoinable() && RcsPolicyManager.getRcsStrategy(this.mPhoneId).shouldRestartSession(imError)) {
            this.mImSession.loge("onStartSessionDone : Rejoining groupchat was unsuccessful. Restart groupchat");
            restartSession(imSessionInfo.mIsTryToLeave, false);
        } else if (!shouldRestartSessionWithNewID(imError) || imSessionInfo.mIsTryToLeave) {
            if (!this.mImSession.mCurrentCanceledMessages.isEmpty()) {
                this.mImSession.failCurrentCanceledMessages();
            }
            if (!this.mImSession.mCurrentMessages.isEmpty()) {
                if (messageBase == null || !this.mImSession.isFirstMessageInStart(messageBase.getBody()) || imError != ImError.BUSY_HERE) {
                    this.mImSession.failCurrentMessages(startImSessionResult.mRawHandle, startImSessionResult.mResult, startImSessionResult.mAllowedMethods);
                } else {
                    this.mImSession.logi("onStartSessionDone : handle 486 response as SUCCESS for the message in INVITE.");
                    ImSession imSession = this.mImSession;
                    if (imSession.isFirstMessageInStart(imSession.mCurrentMessages.get(0).getBody())) {
                        messageBase2 = this.mImSession.mCurrentMessages.remove(0);
                    }
                    onStartSession(messageBase2, ImSessionInfo.StartingReason.NORMAL, false);
                }
            }
            if (imError == ImError.FORBIDDEN_MAX_GROUP_NUMBER) {
                this.mImSession.mClosedReason = ImSessionClosedReason.MAX_GROUP_NUMBER_REACHED;
            } else if (imError == ImError.GONE && this.mImSession.getRcsStrategy(this.mPhoneId).boolSetting(RcsPolicySettings.RcsPolicy.GONE_SHOULD_ENDSESSION)) {
                this.mImSession.mClosedReason = ImSessionClosedReason.GROUP_CHAT_DISMISSED;
            } else if (imError != ImError.FORBIDDEN_RESTART_GC_CLOSED || !this.mImSession.getRcsStrategy(this.mPhoneId).boolSetting(RcsPolicySettings.RcsPolicy.SUPPORT_CHAT_CLOSE_BY_SERVER)) {
                this.mImSession.mClosedReason = RcsPolicyManager.getRcsStrategy(this.mPhoneId).handleSessionFailure(imError, this.mImSession.isGroupChat());
            } else {
                this.mImSession.logi("onStartSessionDone : Chat Close by Server ");
                if (imSessionInfo.mIsTryToLeave) {
                    ImSession imSession2 = this.mImSession;
                    imSession2.mClosedReason = ImSessionClosedReason.CLOSED_BY_LOCAL;
                    imSession2.mListener.onChatDeparted(imSession2);
                } else {
                    this.mImSession.mClosedReason = ImSessionClosedReason.LEFT_BY_SERVER;
                }
            }
            if (this.mImSession.getRcsStrategy(this.mPhoneId).isNeedToReportToRegiGvn(imError)) {
                this.mImSession.mListener.onImErrorReport(imError, this.mPhoneId);
            }
            ImSession imSession3 = this.mImSession;
            imSession3.mClosedEvent = new ImSessionClosedEvent(startImSessionResult.mRawHandle, imSession3.getChatId(), startImSessionResult.mResult);
            this.mImSession.transitionToProperState();
        } else {
            this.mImSession.loge("onStartSessionDone : User is not authorized to rejoin the group. start new chat");
            restartSession(false, true);
        }
    }

    /* access modifiers changed from: protected */
    public void onAcceptSessionDone(Message message) {
        StartImSessionResult startImSessionResult = (StartImSessionResult) ((AsyncResult) message.obj).result;
        ImSession imSession = this.mImSession;
        imSession.logi("onAcceptSessionDone : " + startImSessionResult);
        if (startImSessionResult.mResult.getImError() == ImError.SUCCESS) {
            startSessionEstablishmentTimer(startImSessionResult.mRawHandle);
        } else {
            this.mImSession.removeImSessionInfo(startImSessionResult.mRawHandle);
            if (!this.mImSession.hasActiveImSessionInfo()) {
                this.mImSession.failCurrentMessages(startImSessionResult, startImSessionResult.mResult);
            }
            this.mImSession.transitionToProperState();
        }
        this.mImSession.releaseWakeLock(startImSessionResult.mRawHandle);
    }

    /* access modifiers changed from: protected */
    public void startSessionEstablishmentTimer(Object obj) {
        if (RcsPolicyManager.getRcsStrategy(this.mPhoneId).intSetting(RcsPolicySettings.RcsPolicy.SESSION_ESTABLISH_TIMER) > 0 && this.mImSession.getChatType() != ChatData.ChatType.REGULAR_GROUP_CHAT) {
            ImSession imSession = this.mImSession;
            imSession.logi("Stack response timer starts" + toString());
            this.mImSession.getHandler().removeMessages(1004, obj);
            ImSession imSession2 = this.mImSession;
            imSession2.sendMessageDelayed(imSession2.obtainMessage(1004, obj), ((long) RcsPolicyManager.getRcsStrategy(this.mPhoneId).intSetting(RcsPolicySettings.RcsPolicy.SESSION_ESTABLISH_TIMER)) * 1000);
        }
    }

    private SendMessageParams createFirstMessageParams(MessageBase messageBase) {
        ImSession imSession = this.mImSession;
        imSession.logi("initializing SendMessageParams: " + this.mImSession.mConfig.isFirstMsgInvite());
        Set<NotificationStatus> dispositionNotification = messageBase.getDispositionNotification();
        if ((ImsProfile.isRcsUpProfile(this.mImSession.mConfig.getRcsProfile()) && this.mImSession.mConfig.getImMsgTech() == ImConstants.ImMsgTech.SIMPLE_IM) || this.mImSession.isMsgFallbackSupported()) {
            dispositionNotification.add(NotificationStatus.INTERWORKING_SMS);
        }
        return new SendMessageParams((Object) null, messageBase.getBody(), this.mImSession.getUserAlias(), messageBase.getContentType(), messageBase.getImdnId(), new Date(), dispositionNotification, (Set<ImsUri>) null, this.mImSession.obtainMessage((int) ImSessionEvent.SEND_MESSAGE_DONE, (Object) messageBase), messageBase.getMaapTrafficType(), messageBase.getReferenceImdnId(), messageBase.getReferenceType(), messageBase.getReferenceValue());
    }
}
