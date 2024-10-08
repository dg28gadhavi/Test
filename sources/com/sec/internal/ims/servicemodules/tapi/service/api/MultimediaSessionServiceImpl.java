package com.sec.internal.ims.servicemodules.tapi.service.api;

import android.content.Context;
import android.content.Intent;
import android.os.IBinder;
import android.os.RemoteCallbackList;
import android.os.RemoteException;
import android.os.UserHandle;
import android.text.TextUtils;
import android.util.Log;
import com.gsma.services.rcs.IRcsServiceRegistrationListener;
import com.gsma.services.rcs.RcsServiceRegistration;
import com.gsma.services.rcs.contact.ContactId;
import com.gsma.services.rcs.extension.IMultimediaMessagingSession;
import com.gsma.services.rcs.extension.IMultimediaMessagingSessionListener;
import com.gsma.services.rcs.extension.IMultimediaSessionService;
import com.gsma.services.rcs.extension.IMultimediaSessionServiceConfiguration;
import com.gsma.services.rcs.extension.IMultimediaStreamingSession;
import com.gsma.services.rcs.extension.IMultimediaStreamingSessionListener;
import com.gsma.services.rcs.extension.MultimediaSession;
import com.sec.ims.ImsRegistration;
import com.sec.ims.extensions.ContextExt;
import com.sec.ims.util.ImsUri;
import com.sec.internal.constants.ims.servicemodules.im.ImDirection;
import com.sec.internal.constants.ims.servicemodules.im.ImError;
import com.sec.internal.constants.ims.servicemodules.im.event.ImSessionClosedEvent;
import com.sec.internal.helper.SimUtil;
import com.sec.internal.helper.UriUtil;
import com.sec.internal.helper.os.IntentUtil;
import com.sec.internal.helper.os.TelephonyUtilsWrapper;
import com.sec.internal.ims.registry.ImsRegistry;
import com.sec.internal.ims.servicemodules.im.ImSession;
import com.sec.internal.ims.servicemodules.options.Intents;
import com.sec.internal.ims.servicemodules.session.IMessagingSessionListener;
import com.sec.internal.ims.servicemodules.session.SessionModule;
import com.sec.internal.ims.servicemodules.tapi.service.broadcaster.IRegistrationStatusBroadcaster;
import com.sec.internal.ims.servicemodules.tapi.service.broadcaster.MultimediaMessagingSessionEventBroadcaster;
import com.sec.internal.ims.servicemodules.tapi.service.broadcaster.MultimediaStreamingSessionEventBroadcaster;
import com.sec.internal.ims.util.UriGenerator;
import com.sec.internal.ims.util.UriGeneratorFactory;
import com.sec.internal.imscr.LogClass;
import com.sec.internal.interfaces.ims.servicemodules.session.ISessionModule;
import com.sec.internal.log.IMSLog;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class MultimediaSessionServiceImpl extends IMultimediaSessionService.Stub implements IMessagingSessionListener, IRegistrationStatusBroadcaster {
    private static final String LOG_TAG = MultimediaSessionServiceImpl.class.getSimpleName();
    private final Object lock = new Object();
    private final Context mContext;
    private final Map<String, IMultimediaMessagingSession> mMultimediaMessagingCache = new HashMap();
    private final MultimediaMessagingSessionEventBroadcaster mMultimediaMessagingSessionEventBroadcaster = new MultimediaMessagingSessionEventBroadcaster();
    private final Map<String, IMultimediaStreamingSession> mMultimediaStreamingCache = new HashMap();
    private final MultimediaStreamingSessionEventBroadcaster mMultimediaStreamingSessionEventBroadcaster = new MultimediaStreamingSessionEventBroadcaster();
    private final Map<String, Boolean> mSessionEstablishCache = new HashMap();
    private final ISessionModule mSessionModule;
    private UriGenerator mUriGenerator;
    private RemoteCallbackList<IRcsServiceRegistrationListener> serviceListeners = new RemoteCallbackList<>();

    public MultimediaSessionServiceImpl(ISessionModule iSessionModule) {
        this.mSessionModule = iSessionModule;
        this.mContext = ImsRegistry.getContext();
        iSessionModule.registerMessagingSessionListener(this);
        this.mUriGenerator = UriGeneratorFactory.getInstance().get(UriGenerator.URIServiceType.RCS_URI);
    }

    public void addEventListener(IRcsServiceRegistrationListener iRcsServiceRegistrationListener) {
        synchronized (this.lock) {
            this.serviceListeners.register(iRcsServiceRegistrationListener);
        }
    }

    public void addEventListener2(IMultimediaMessagingSessionListener iMultimediaMessagingSessionListener) {
        synchronized (this.lock) {
            this.mMultimediaMessagingSessionEventBroadcaster.addMultimediaMessagingEventListener(iMultimediaMessagingSessionListener);
        }
    }

    public void addEventListener3(IMultimediaStreamingSessionListener iMultimediaStreamingSessionListener) {
        synchronized (this.lock) {
            this.mMultimediaStreamingSessionEventBroadcaster.addMultimediaStreamingEventListener(iMultimediaStreamingSessionListener);
        }
    }

    private void addMultimediaMessaging(MultimediaMessagingSessionImpl multimediaMessagingSessionImpl) {
        this.mMultimediaMessagingCache.put(multimediaMessagingSessionImpl.getSessionId(), multimediaMessagingSessionImpl);
        this.mSessionEstablishCache.put(multimediaMessagingSessionImpl.getSessionId(), Boolean.FALSE);
    }

    public IMultimediaSessionServiceConfiguration getConfiguration() {
        return MultimediaSessionServiceConfigurationImpl.getInstance(this.mSessionModule);
    }

    public IMultimediaMessagingSession getMessagingSession(String str) throws ServerApiException {
        IMultimediaMessagingSession iMultimediaMessagingSession = this.mMultimediaMessagingCache.get(str);
        if (iMultimediaMessagingSession != null) {
            return iMultimediaMessagingSession;
        }
        ImSession messagingSession = this.mSessionModule.getMessagingSession(str);
        if (messagingSession == null) {
            Log.e(LOG_TAG, "Session not exists.");
            return null;
        }
        MultimediaMessagingSessionImpl multimediaMessagingSessionImpl = new MultimediaMessagingSessionImpl(this.mSessionModule, messagingSession);
        addMultimediaMessaging(multimediaMessagingSessionImpl);
        return multimediaMessagingSessionImpl;
    }

    public List<IBinder> getMessagingSessions(String str) throws ServerApiException {
        try {
            ArrayList arrayList = new ArrayList();
            for (IMultimediaMessagingSession next : this.mMultimediaMessagingCache.values()) {
                if (next.getServiceId().contains(str)) {
                    arrayList.add(next.asBinder());
                }
            }
            return arrayList;
        } catch (RemoteException e) {
            throw new ServerApiException(e.getMessage());
        }
    }

    public IMultimediaStreamingSession getStreamingSession(String str) throws ServerApiException {
        throw new ServerApiException("Unsupported operation");
    }

    public List<IBinder> getStreamingSessions(String str) throws ServerApiException {
        try {
            ArrayList arrayList = new ArrayList();
            for (IMultimediaStreamingSession next : this.mMultimediaStreamingCache.values()) {
                if (next.getServiceId().contains(str)) {
                    arrayList.add(next.asBinder());
                }
            }
            return arrayList;
        } catch (RemoteException e) {
            throw new ServerApiException(e.getMessage());
        }
    }

    public IMultimediaMessagingSession initiateMessagingSession(String str, ContactId contactId, String[] strArr, String[] strArr2) throws ServerApiException {
        String str2 = LOG_TAG;
        Log.d(str2, "initiateMessagingSession: " + str + " ContactId = " + IMSLog.checker(contactId));
        ImsRegistration imsRegistration = this.mSessionModule.getImsRegistration();
        if (imsRegistration == null) {
            return null;
        }
        UriGenerator uriGenerator = UriGeneratorFactory.getInstance().get(imsRegistration.getPreferredImpu().getUri(), UriGenerator.URIServiceType.RCS_URI);
        this.mUriGenerator = uriGenerator;
        ImsUri normalizedUri = uriGenerator.getNormalizedUri(contactId.toString(), true);
        if (normalizedUri == null || TextUtils.isEmpty(str)) {
            return null;
        }
        MultimediaMessagingSessionImpl multimediaMessagingSessionImpl = new MultimediaMessagingSessionImpl(this.mSessionModule, this.mSessionModule.initiateMessagingSession(str, normalizedUri, strArr, strArr2));
        addMultimediaMessaging(multimediaMessagingSessionImpl);
        return multimediaMessagingSessionImpl;
    }

    public IMultimediaStreamingSession initiateStreamingSession(String str, ContactId contactId) throws ServerApiException {
        throw new ServerApiException("Unsupported operation");
    }

    public boolean isServiceRegistered() {
        return this.mSessionModule.isServiceRegistered();
    }

    public void notifyRegistrationEvent(boolean z, RcsServiceRegistration.ReasonCode reasonCode) {
        synchronized (this.lock) {
            int beginBroadcast = this.serviceListeners.beginBroadcast();
            for (int i = 0; i < beginBroadcast; i++) {
                if (z) {
                    try {
                        this.serviceListeners.getBroadcastItem(i).onServiceRegistered();
                    } catch (RemoteException e) {
                        e.printStackTrace();
                    } catch (NullPointerException e2) {
                        e2.printStackTrace();
                    }
                } else {
                    this.serviceListeners.getBroadcastItem(i).onServiceUnregistered(reasonCode);
                }
            }
            this.serviceListeners.finishBroadcast();
        }
    }

    public void removeEventListener(IRcsServiceRegistrationListener iRcsServiceRegistrationListener) {
        synchronized (this.lock) {
            this.serviceListeners.unregister(iRcsServiceRegistrationListener);
        }
    }

    public void removeEventListener2(IMultimediaMessagingSessionListener iMultimediaMessagingSessionListener) {
        synchronized (this.lock) {
            this.mMultimediaMessagingSessionEventBroadcaster.removeMultimediaMessagingEventListener(iMultimediaMessagingSessionListener);
        }
    }

    public void removeEventListener3(IMultimediaStreamingSessionListener iMultimediaStreamingSessionListener) {
        synchronized (this.lock) {
            this.mMultimediaStreamingSessionEventBroadcaster.removeMultimediaStreamingEventListener(iMultimediaStreamingSessionListener);
        }
    }

    public void setInactivityTimeout(long j) throws ServerApiException {
        try {
            SessionModule.setInactivityTimeout(j);
        } catch (Exception e) {
            throw new ServerApiException(e.getMessage());
        }
    }

    public void sendInstantMultimediaMessage(String str, ContactId contactId, byte[] bArr, String str2) throws ServerApiException {
        String str3 = LOG_TAG;
        Log.d(str3, "sendInstantMultimediaMessage,serviceId=" + str + "contactId=" + IMSLog.checker(contactId));
        if (contactId != null) {
            try {
                this.mSessionModule.sendInstantMultimediaMessage(str, UriUtil.parseNumber(contactId.toString()), bArr, str2);
            } catch (Exception e) {
                throw new ServerApiException(e.getMessage());
            }
        }
    }

    /* access modifiers changed from: package-private */
    public void removeMultimediaMessaging(String str) {
        this.mMultimediaMessagingCache.remove(str);
        this.mSessionEstablishCache.remove(str);
    }

    public void onIncomingSessionInvited(ImSession imSession, String str) {
        String str2 = LOG_TAG;
        Log.d(str2, "onIncomingSessionInvited: " + imSession.getChatId());
        Intent intent = new Intent(SessionModule.INTENT_FILTER_MESSAGE);
        intent.addCategory(Intents.INTENT_CATEGORY);
        intent.addCategory("android.intent.category.LAUNCHER");
        intent.addFlags(LogClass.SIM_EVENT);
        intent.setType(str);
        intent.putExtra("sessionId", imSession.getChatId());
        UserHandle subscriptionUserHandle = TelephonyUtilsWrapper.getSubscriptionUserHandle(this.mContext, SimUtil.getSubId(this.mSessionModule.getPhoneIdByIMSI(imSession.getOwnImsi())));
        if (subscriptionUserHandle != null) {
            IntentUtil.sendBroadcast(this.mContext, intent, subscriptionUserHandle, "com.gsma.services.permission.RCS");
        } else {
            IntentUtil.sendBroadcast(this.mContext, intent, ContextExt.CURRENT_OR_SELF, "com.gsma.services.permission.RCS");
        }
    }

    public void onStateChanged(ImSession imSession, ImSession.SessionState sessionState) {
        String str = LOG_TAG;
        Log.d(str, "onStateChanged: id=" + imSession.getChatId() + ", state=" + sessionState);
        ImsUri remoteUri = imSession.getRemoteUri();
        ImSessionClosedEvent imSessionClosedEvent = imSession.getImSessionClosedEvent();
        boolean isTimerExpired = imSession.isTimerExpired();
        MultimediaSession.ReasonCode reasonCode = MultimediaSession.ReasonCode.UNSPECIFIED;
        if (remoteUri != null) {
            if (sessionState == ImSession.SessionState.ESTABLISHED) {
                this.mSessionEstablishCache.put(imSession.getChatId(), Boolean.TRUE);
            }
            if (imSessionClosedEvent != null) {
                reasonCode = translateError(imSessionClosedEvent.mResult.getImError(), sessionState);
            }
            ImSession.SessionState sessionState2 = ImSession.SessionState.CLOSED;
            if (sessionState == sessionState2 && isTimerExpired) {
                reasonCode = MultimediaSession.ReasonCode.ABORTED_BY_INACTIVITY;
            }
            if (sessionState == sessionState2 || sessionState == ImSession.SessionState.FAILED_MEDIA) {
                removeMultimediaMessaging(imSession.getChatId());
            }
            this.mMultimediaMessagingSessionEventBroadcaster.broadcastStateChanged(new ContactId(remoteUri.getMsisdn()), imSession.getChatId(), translateState(sessionState, imSession.getDirection()), reasonCode);
        }
    }

    public void onMessageReceived(ImSession imSession, byte[] bArr, String str) {
        ImsUri remoteUri = imSession.getRemoteUri();
        this.mMultimediaMessagingSessionEventBroadcaster.broadcastMessageReceived(remoteUri != null ? new ContactId(remoteUri.getMsisdn()) : null, imSession.getChatId(), bArr, str);
    }

    public void onMessagesFlushed(ImSession imSession) {
        String str = LOG_TAG;
        Log.d(str, "onMessagesFlushed: " + imSession.getChatId());
        this.mMultimediaMessagingSessionEventBroadcaster.broadcastMessagesFlushed(new ContactId(imSession.getRemoteUri().getMsisdn()), imSession.getChatId());
    }

    private static MultimediaSession.ReasonCode translateError(ImError imError, ImSession.SessionState sessionState) {
        if (sessionState == ImSession.SessionState.FAILED_MEDIA) {
            return MultimediaSession.ReasonCode.FAILED_MEDIA;
        }
        switch (AnonymousClass1.$SwitchMap$com$sec$internal$constants$ims$servicemodules$im$ImError[imError.ordinal()]) {
            case 1:
                return MultimediaSession.ReasonCode.REJECT_REASON_BUSY;
            case 2:
                return MultimediaSession.ReasonCode.REJECT_REASON_DECLINE;
            case 3:
                return MultimediaSession.ReasonCode.REJECT_REASON_TEMP_UNAVAILABLE;
            case 4:
                return MultimediaSession.ReasonCode.REJECT_REASON_BAD_REQUEST;
            case 5:
                return MultimediaSession.ReasonCode.REJECT_REASON_REQ_TERMINATED;
            case 6:
                return MultimediaSession.ReasonCode.REJECT_REASON_SERVICE_UNAVAILABLE;
            case 7:
                return MultimediaSession.ReasonCode.REJECT_REASON_USER_CALL_BLOCK;
            case 8:
                return MultimediaSession.ReasonCode.REJECTED_BY_TIMEOUT;
            case 9:
                return MultimediaSession.ReasonCode.REJECT_REASON_TEMP_NOT_ACCEPTABLE;
            case 10:
                return MultimediaSession.ReasonCode.REJECT_REASON_REQUEST_PENDING;
            case 11:
                return MultimediaSession.ReasonCode.REJECT_REASON_REMOTE_USER_INVALID;
            case 12:
                return MultimediaSession.ReasonCode.REJECT_REASON_NOT_IMPLEMENTED;
            case 13:
                return MultimediaSession.ReasonCode.REJECT_REASON_SERVER_TIMEOUT;
            default:
                return MultimediaSession.ReasonCode.UNSPECIFIED;
        }
    }

    /* renamed from: com.sec.internal.ims.servicemodules.tapi.service.api.MultimediaSessionServiceImpl$1  reason: invalid class name */
    static /* synthetic */ class AnonymousClass1 {
        static final /* synthetic */ int[] $SwitchMap$com$sec$internal$constants$ims$servicemodules$im$ImError;
        static final /* synthetic */ int[] $SwitchMap$com$sec$internal$ims$servicemodules$im$ImSession$SessionState;

        /* JADX WARNING: Can't wrap try/catch for region: R(40:0|(2:1|2)|3|(2:5|6)|7|9|10|11|(2:13|14)|15|(2:17|18)|19|21|22|23|25|26|27|28|29|30|31|32|33|34|35|36|37|38|39|40|41|42|43|44|45|46|47|48|(3:49|50|52)) */
        /* JADX WARNING: Can't wrap try/catch for region: R(44:0|(2:1|2)|3|5|6|7|9|10|11|(2:13|14)|15|17|18|19|21|22|23|25|26|27|28|29|30|31|32|33|34|35|36|37|38|39|40|41|42|43|44|45|46|47|48|49|50|52) */
        /* JADX WARNING: Can't wrap try/catch for region: R(46:0|1|2|3|5|6|7|9|10|11|13|14|15|17|18|19|21|22|23|25|26|27|28|29|30|31|32|33|34|35|36|37|38|39|40|41|42|43|44|45|46|47|48|49|50|52) */
        /* JADX WARNING: Failed to process nested try/catch */
        /* JADX WARNING: Missing exception handler attribute for start block: B:27:0x005a */
        /* JADX WARNING: Missing exception handler attribute for start block: B:29:0x0064 */
        /* JADX WARNING: Missing exception handler attribute for start block: B:31:0x006e */
        /* JADX WARNING: Missing exception handler attribute for start block: B:33:0x0078 */
        /* JADX WARNING: Missing exception handler attribute for start block: B:35:0x0082 */
        /* JADX WARNING: Missing exception handler attribute for start block: B:37:0x008c */
        /* JADX WARNING: Missing exception handler attribute for start block: B:39:0x0097 */
        /* JADX WARNING: Missing exception handler attribute for start block: B:41:0x00a3 */
        /* JADX WARNING: Missing exception handler attribute for start block: B:43:0x00af */
        /* JADX WARNING: Missing exception handler attribute for start block: B:45:0x00bb */
        /* JADX WARNING: Missing exception handler attribute for start block: B:47:0x00c7 */
        /* JADX WARNING: Missing exception handler attribute for start block: B:49:0x00d3 */
        static {
            /*
                com.sec.internal.ims.servicemodules.im.ImSession$SessionState[] r0 = com.sec.internal.ims.servicemodules.im.ImSession.SessionState.values()
                int r0 = r0.length
                int[] r0 = new int[r0]
                $SwitchMap$com$sec$internal$ims$servicemodules$im$ImSession$SessionState = r0
                r1 = 1
                com.sec.internal.ims.servicemodules.im.ImSession$SessionState r2 = com.sec.internal.ims.servicemodules.im.ImSession.SessionState.INITIAL     // Catch:{ NoSuchFieldError -> 0x0012 }
                int r2 = r2.ordinal()     // Catch:{ NoSuchFieldError -> 0x0012 }
                r0[r2] = r1     // Catch:{ NoSuchFieldError -> 0x0012 }
            L_0x0012:
                r0 = 2
                int[] r2 = $SwitchMap$com$sec$internal$ims$servicemodules$im$ImSession$SessionState     // Catch:{ NoSuchFieldError -> 0x001d }
                com.sec.internal.ims.servicemodules.im.ImSession$SessionState r3 = com.sec.internal.ims.servicemodules.im.ImSession.SessionState.STARTING     // Catch:{ NoSuchFieldError -> 0x001d }
                int r3 = r3.ordinal()     // Catch:{ NoSuchFieldError -> 0x001d }
                r2[r3] = r0     // Catch:{ NoSuchFieldError -> 0x001d }
            L_0x001d:
                r2 = 3
                int[] r3 = $SwitchMap$com$sec$internal$ims$servicemodules$im$ImSession$SessionState     // Catch:{ NoSuchFieldError -> 0x0028 }
                com.sec.internal.ims.servicemodules.im.ImSession$SessionState r4 = com.sec.internal.ims.servicemodules.im.ImSession.SessionState.ESTABLISHED     // Catch:{ NoSuchFieldError -> 0x0028 }
                int r4 = r4.ordinal()     // Catch:{ NoSuchFieldError -> 0x0028 }
                r3[r4] = r2     // Catch:{ NoSuchFieldError -> 0x0028 }
            L_0x0028:
                r3 = 4
                int[] r4 = $SwitchMap$com$sec$internal$ims$servicemodules$im$ImSession$SessionState     // Catch:{ NoSuchFieldError -> 0x0033 }
                com.sec.internal.ims.servicemodules.im.ImSession$SessionState r5 = com.sec.internal.ims.servicemodules.im.ImSession.SessionState.CLOSING     // Catch:{ NoSuchFieldError -> 0x0033 }
                int r5 = r5.ordinal()     // Catch:{ NoSuchFieldError -> 0x0033 }
                r4[r5] = r3     // Catch:{ NoSuchFieldError -> 0x0033 }
            L_0x0033:
                r4 = 5
                int[] r5 = $SwitchMap$com$sec$internal$ims$servicemodules$im$ImSession$SessionState     // Catch:{ NoSuchFieldError -> 0x003e }
                com.sec.internal.ims.servicemodules.im.ImSession$SessionState r6 = com.sec.internal.ims.servicemodules.im.ImSession.SessionState.CLOSED     // Catch:{ NoSuchFieldError -> 0x003e }
                int r6 = r6.ordinal()     // Catch:{ NoSuchFieldError -> 0x003e }
                r5[r6] = r4     // Catch:{ NoSuchFieldError -> 0x003e }
            L_0x003e:
                r5 = 6
                int[] r6 = $SwitchMap$com$sec$internal$ims$servicemodules$im$ImSession$SessionState     // Catch:{ NoSuchFieldError -> 0x0049 }
                com.sec.internal.ims.servicemodules.im.ImSession$SessionState r7 = com.sec.internal.ims.servicemodules.im.ImSession.SessionState.FAILED_MEDIA     // Catch:{ NoSuchFieldError -> 0x0049 }
                int r7 = r7.ordinal()     // Catch:{ NoSuchFieldError -> 0x0049 }
                r6[r7] = r5     // Catch:{ NoSuchFieldError -> 0x0049 }
            L_0x0049:
                com.sec.internal.constants.ims.servicemodules.im.ImError[] r6 = com.sec.internal.constants.ims.servicemodules.im.ImError.values()
                int r6 = r6.length
                int[] r6 = new int[r6]
                $SwitchMap$com$sec$internal$constants$ims$servicemodules$im$ImError = r6
                com.sec.internal.constants.ims.servicemodules.im.ImError r7 = com.sec.internal.constants.ims.servicemodules.im.ImError.BUSY_HERE     // Catch:{ NoSuchFieldError -> 0x005a }
                int r7 = r7.ordinal()     // Catch:{ NoSuchFieldError -> 0x005a }
                r6[r7] = r1     // Catch:{ NoSuchFieldError -> 0x005a }
            L_0x005a:
                int[] r1 = $SwitchMap$com$sec$internal$constants$ims$servicemodules$im$ImError     // Catch:{ NoSuchFieldError -> 0x0064 }
                com.sec.internal.constants.ims.servicemodules.im.ImError r6 = com.sec.internal.constants.ims.servicemodules.im.ImError.REMOTE_PARTY_DECLINED     // Catch:{ NoSuchFieldError -> 0x0064 }
                int r6 = r6.ordinal()     // Catch:{ NoSuchFieldError -> 0x0064 }
                r1[r6] = r0     // Catch:{ NoSuchFieldError -> 0x0064 }
            L_0x0064:
                int[] r0 = $SwitchMap$com$sec$internal$constants$ims$servicemodules$im$ImError     // Catch:{ NoSuchFieldError -> 0x006e }
                com.sec.internal.constants.ims.servicemodules.im.ImError r1 = com.sec.internal.constants.ims.servicemodules.im.ImError.REMOTE_TEMPORARILY_UNAVAILABLE     // Catch:{ NoSuchFieldError -> 0x006e }
                int r1 = r1.ordinal()     // Catch:{ NoSuchFieldError -> 0x006e }
                r0[r1] = r2     // Catch:{ NoSuchFieldError -> 0x006e }
            L_0x006e:
                int[] r0 = $SwitchMap$com$sec$internal$constants$ims$servicemodules$im$ImError     // Catch:{ NoSuchFieldError -> 0x0078 }
                com.sec.internal.constants.ims.servicemodules.im.ImError r1 = com.sec.internal.constants.ims.servicemodules.im.ImError.INVALID_REQUEST     // Catch:{ NoSuchFieldError -> 0x0078 }
                int r1 = r1.ordinal()     // Catch:{ NoSuchFieldError -> 0x0078 }
                r0[r1] = r3     // Catch:{ NoSuchFieldError -> 0x0078 }
            L_0x0078:
                int[] r0 = $SwitchMap$com$sec$internal$constants$ims$servicemodules$im$ImError     // Catch:{ NoSuchFieldError -> 0x0082 }
                com.sec.internal.constants.ims.servicemodules.im.ImError r1 = com.sec.internal.constants.ims.servicemodules.im.ImError.CONNECTION_RELEASED     // Catch:{ NoSuchFieldError -> 0x0082 }
                int r1 = r1.ordinal()     // Catch:{ NoSuchFieldError -> 0x0082 }
                r0[r1] = r4     // Catch:{ NoSuchFieldError -> 0x0082 }
            L_0x0082:
                int[] r0 = $SwitchMap$com$sec$internal$constants$ims$servicemodules$im$ImError     // Catch:{ NoSuchFieldError -> 0x008c }
                com.sec.internal.constants.ims.servicemodules.im.ImError r1 = com.sec.internal.constants.ims.servicemodules.im.ImError.SERVICE_UNAVAILABLE     // Catch:{ NoSuchFieldError -> 0x008c }
                int r1 = r1.ordinal()     // Catch:{ NoSuchFieldError -> 0x008c }
                r0[r1] = r5     // Catch:{ NoSuchFieldError -> 0x008c }
            L_0x008c:
                int[] r0 = $SwitchMap$com$sec$internal$constants$ims$servicemodules$im$ImError     // Catch:{ NoSuchFieldError -> 0x0097 }
                com.sec.internal.constants.ims.servicemodules.im.ImError r1 = com.sec.internal.constants.ims.servicemodules.im.ImError.FORBIDDEN_NO_WARNING_HEADER     // Catch:{ NoSuchFieldError -> 0x0097 }
                int r1 = r1.ordinal()     // Catch:{ NoSuchFieldError -> 0x0097 }
                r2 = 7
                r0[r1] = r2     // Catch:{ NoSuchFieldError -> 0x0097 }
            L_0x0097:
                int[] r0 = $SwitchMap$com$sec$internal$constants$ims$servicemodules$im$ImError     // Catch:{ NoSuchFieldError -> 0x00a3 }
                com.sec.internal.constants.ims.servicemodules.im.ImError r1 = com.sec.internal.constants.ims.servicemodules.im.ImError.SESSION_TIMED_OUT     // Catch:{ NoSuchFieldError -> 0x00a3 }
                int r1 = r1.ordinal()     // Catch:{ NoSuchFieldError -> 0x00a3 }
                r2 = 8
                r0[r1] = r2     // Catch:{ NoSuchFieldError -> 0x00a3 }
            L_0x00a3:
                int[] r0 = $SwitchMap$com$sec$internal$constants$ims$servicemodules$im$ImError     // Catch:{ NoSuchFieldError -> 0x00af }
                com.sec.internal.constants.ims.servicemodules.im.ImError r1 = com.sec.internal.constants.ims.servicemodules.im.ImError.NOT_ACCEPTABLE_HERE     // Catch:{ NoSuchFieldError -> 0x00af }
                int r1 = r1.ordinal()     // Catch:{ NoSuchFieldError -> 0x00af }
                r2 = 9
                r0[r1] = r2     // Catch:{ NoSuchFieldError -> 0x00af }
            L_0x00af:
                int[] r0 = $SwitchMap$com$sec$internal$constants$ims$servicemodules$im$ImError     // Catch:{ NoSuchFieldError -> 0x00bb }
                com.sec.internal.constants.ims.servicemodules.im.ImError r1 = com.sec.internal.constants.ims.servicemodules.im.ImError.REQUEST_PENDING     // Catch:{ NoSuchFieldError -> 0x00bb }
                int r1 = r1.ordinal()     // Catch:{ NoSuchFieldError -> 0x00bb }
                r2 = 10
                r0[r1] = r2     // Catch:{ NoSuchFieldError -> 0x00bb }
            L_0x00bb:
                int[] r0 = $SwitchMap$com$sec$internal$constants$ims$servicemodules$im$ImError     // Catch:{ NoSuchFieldError -> 0x00c7 }
                com.sec.internal.constants.ims.servicemodules.im.ImError r1 = com.sec.internal.constants.ims.servicemodules.im.ImError.REMOTE_USER_INVALID     // Catch:{ NoSuchFieldError -> 0x00c7 }
                int r1 = r1.ordinal()     // Catch:{ NoSuchFieldError -> 0x00c7 }
                r2 = 11
                r0[r1] = r2     // Catch:{ NoSuchFieldError -> 0x00c7 }
            L_0x00c7:
                int[] r0 = $SwitchMap$com$sec$internal$constants$ims$servicemodules$im$ImError     // Catch:{ NoSuchFieldError -> 0x00d3 }
                com.sec.internal.constants.ims.servicemodules.im.ImError r1 = com.sec.internal.constants.ims.servicemodules.im.ImError.NOT_IMPLEMENTED     // Catch:{ NoSuchFieldError -> 0x00d3 }
                int r1 = r1.ordinal()     // Catch:{ NoSuchFieldError -> 0x00d3 }
                r2 = 12
                r0[r1] = r2     // Catch:{ NoSuchFieldError -> 0x00d3 }
            L_0x00d3:
                int[] r0 = $SwitchMap$com$sec$internal$constants$ims$servicemodules$im$ImError     // Catch:{ NoSuchFieldError -> 0x00df }
                com.sec.internal.constants.ims.servicemodules.im.ImError r1 = com.sec.internal.constants.ims.servicemodules.im.ImError.SERVER_TIMEOUT     // Catch:{ NoSuchFieldError -> 0x00df }
                int r1 = r1.ordinal()     // Catch:{ NoSuchFieldError -> 0x00df }
                r2 = 13
                r0[r1] = r2     // Catch:{ NoSuchFieldError -> 0x00df }
            L_0x00df:
                return
            */
            throw new UnsupportedOperationException("Method not decompiled: com.sec.internal.ims.servicemodules.tapi.service.api.MultimediaSessionServiceImpl.AnonymousClass1.<clinit>():void");
        }
    }

    private static MultimediaSession.State translateState(ImSession.SessionState sessionState, ImDirection imDirection) {
        switch (AnonymousClass1.$SwitchMap$com$sec$internal$ims$servicemodules$im$ImSession$SessionState[sessionState.ordinal()]) {
            case 1:
                if (imDirection == ImDirection.OUTGOING) {
                    return MultimediaSession.State.INITIATING;
                }
                return MultimediaSession.State.INVITED;
            case 2:
                if (imDirection == ImDirection.OUTGOING) {
                    return MultimediaSession.State.RINGING;
                }
                return MultimediaSession.State.ACCEPTING;
            case 3:
                return MultimediaSession.State.STARTED;
            case 4:
                return MultimediaSession.State.ABORTED;
            case 5:
                return MultimediaSession.State.ABORTED;
            case 6:
                return MultimediaSession.State.ABORTED;
            default:
                return MultimediaSession.State.FAILED;
        }
    }
}
