package com.sec.internal.ims.servicemodules.tapi.service.api;

import android.content.Context;
import android.database.Cursor;
import android.location.Location;
import android.os.IBinder;
import android.os.RemoteCallbackList;
import android.os.RemoteException;
import android.os.UserHandle;
import android.util.Log;
import com.gsma.services.rcs.Geoloc;
import com.gsma.services.rcs.IRcsServiceRegistrationListener;
import com.gsma.services.rcs.RcsServiceRegistration;
import com.gsma.services.rcs.contact.ContactId;
import com.gsma.services.rcs.sharing.geoloc.GeolocSharing;
import com.gsma.services.rcs.sharing.geoloc.IGeolocSharing;
import com.gsma.services.rcs.sharing.geoloc.IGeolocSharingListener;
import com.gsma.services.rcs.sharing.geoloc.IGeolocSharingService;
import com.sec.ims.ImsRegistration;
import com.sec.ims.extensions.ContextExt;
import com.sec.ims.util.ImsUri;
import com.sec.internal.constants.ims.MIMEContentType;
import com.sec.internal.constants.ims.servicemodules.im.ChatData;
import com.sec.internal.constants.ims.servicemodules.im.ImConstants;
import com.sec.internal.constants.ims.servicemodules.im.NotificationStatus;
import com.sec.internal.constants.ims.servicemodules.im.reason.CancelReason;
import com.sec.internal.constants.ims.servicemodules.im.result.Result;
import com.sec.internal.helper.SimUtil;
import com.sec.internal.helper.os.TelephonyUtilsWrapper;
import com.sec.internal.ims.registry.ImsRegistry;
import com.sec.internal.ims.servicemodules.im.FtMessage;
import com.sec.internal.ims.servicemodules.im.ImCache;
import com.sec.internal.ims.servicemodules.im.ImSession;
import com.sec.internal.ims.servicemodules.im.MessageBase;
import com.sec.internal.ims.servicemodules.im.listener.IFtEventListener;
import com.sec.internal.ims.servicemodules.im.listener.IMessageEventListener;
import com.sec.internal.ims.servicemodules.im.strategy.IMnoStrategy;
import com.sec.internal.ims.servicemodules.tapi.service.broadcaster.GeolocSharingEventBroadcaster;
import com.sec.internal.ims.servicemodules.tapi.service.broadcaster.IRegistrationStatusBroadcaster;
import com.sec.internal.ims.util.PhoneUtils;
import com.sec.internal.interfaces.ims.core.IRegistrationManager;
import com.sec.internal.interfaces.ims.servicemodules.gls.IGlsModule;
import java.util.ArrayList;
import java.util.Collection;
import java.util.EnumSet;
import java.util.Enumeration;
import java.util.HashSet;
import java.util.Hashtable;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.TreeMap;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.Future;

public class GeolocSharingServiceImpl extends IGeolocSharingService.Stub implements IMessageEventListener, IFtEventListener, IRegistrationStatusBroadcaster {
    private static final String LOG_TAG = GeolocSharingServiceImpl.class.getSimpleName();
    private Context mContext;
    private GeolocSharingEventBroadcaster mGeolocSharingEventBroadcaster = null;
    private IGlsModule mGlsModule;
    private final Hashtable<String, IGeolocSharing> mGshSessions = new Hashtable<>();
    private final Object mLock = new Object();
    private RemoteCallbackList<IRcsServiceRegistrationListener> mServiceListeners = new RemoteCallbackList<>();

    public int getServiceVersion() throws ServerApiException {
        return 2;
    }

    public void onCancelMessageResponse(String str, String str2, boolean z) {
    }

    public void onCancelRequestFailed(FtMessage ftMessage) {
    }

    public void onFileResizingNeeded(FtMessage ftMessage, long j) {
    }

    public void onFileTransferAttached(FtMessage ftMessage) {
    }

    public void onImdnNotificationReceived(FtMessage ftMessage, ImsUri imsUri, NotificationStatus notificationStatus, boolean z) {
    }

    public void onImdnNotificationReceived(MessageBase messageBase, ImsUri imsUri, NotificationStatus notificationStatus, boolean z) {
    }

    public void onMessageReceived(MessageBase messageBase, ImSession imSession) {
    }

    public void onMessageSendResponse(MessageBase messageBase) {
    }

    public void onMessageSendResponseFailed(String str, int i, int i2, String str2) {
    }

    public void onMessageSendResponseTimeout(MessageBase messageBase) {
    }

    public void onMessageSendingFailed(MessageBase messageBase, IMnoStrategy.StrategyResponse strategyResponse, Result result) {
    }

    public void onMessageSendingSucceeded(MessageBase messageBase) {
    }

    public void onNotifyCloudMsgFtEvent(FtMessage ftMessage) {
    }

    public GeolocSharingServiceImpl(Context context, IGlsModule iGlsModule) {
        this.mGlsModule = iGlsModule;
        this.mContext = context;
        this.mGeolocSharingEventBroadcaster = new GeolocSharingEventBroadcaster(context);
        IGlsModule iGlsModule2 = this.mGlsModule;
        ImConstants.Type type = ImConstants.Type.LOCATION;
        iGlsModule2.registerMessageEventListener(type, this);
        this.mGlsModule.registerFtEventListener(type, this);
    }

    private void addGeolocSharingSession(GeolocSharingImpl geolocSharingImpl) {
        try {
            this.mGshSessions.put(geolocSharingImpl.getSharingId(), geolocSharingImpl);
        } catch (ServerApiException e) {
            e.printStackTrace();
        }
    }

    private void removeGeolocSharingSession(String str) {
        this.mGshSessions.remove(str);
    }

    public boolean isServiceRegistered() throws ServerApiException {
        IRegistrationManager registrationManager = ImsRegistry.getRegistrationManager();
        if (registrationManager == null) {
            return false;
        }
        for (ImsRegistration hasService : registrationManager.getRegistrationInfo()) {
            if (hasService.hasService("gls")) {
                return true;
            }
        }
        return false;
    }

    public void addServiceRegistrationListener(IRcsServiceRegistrationListener iRcsServiceRegistrationListener) throws ServerApiException {
        synchronized (this.mLock) {
            this.mServiceListeners.register(iRcsServiceRegistrationListener);
        }
    }

    public void removeServiceRegistrationListener(IRcsServiceRegistrationListener iRcsServiceRegistrationListener) throws ServerApiException {
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

    public IGeolocSharing shareGeoloc(ContactId contactId, Geoloc geoloc) throws ServerApiException {
        ImsUri parse = ImsUri.parse("tel:" + contactId.toString());
        Location location = new Location("gps");
        if (geoloc != null) {
            location.setLatitude(geoloc.getLatitude());
            location.setLongitude(geoloc.getLongitude());
            location.setAccuracy(geoloc.getAccuracy());
            IGlsModule iGlsModule = this.mGlsModule;
            if (iGlsModule == null) {
                Log.e(LOG_TAG, "GLS module is not created");
                return null;
            }
            Future<FtMessage> createInCallLocationShare = iGlsModule.createInCallLocationShare((String) null, parse, EnumSet.of(NotificationStatus.DELIVERED), location, geoloc.getLabel(), (String) null, false, false);
            if (createInCallLocationShare == null) {
                Log.e(LOG_TAG, "sharing geolocation  failed, return null!");
                return null;
            }
            try {
                FtMessage ftMessage = createInCallLocationShare.get();
                if (ftMessage == null) {
                    Log.e(LOG_TAG, "sharing geolocation  failed, return null!");
                    return null;
                }
                GeolocSharingImpl geolocSharingImpl = new GeolocSharingImpl(ftMessage, this.mGlsModule);
                addGeolocSharingSession(geolocSharingImpl);
                return geolocSharingImpl;
            } catch (InterruptedException e) {
                e.printStackTrace();
            } catch (ExecutionException e2) {
                e2.printStackTrace();
            }
        }
        return null;
    }

    public List<IBinder> getGeolocSharings() throws ServerApiException {
        Log.d(LOG_TAG, "Get geoloc sharing sessions");
        ArrayList arrayList = new ArrayList(this.mGshSessions.size());
        Enumeration<IGeolocSharing> elements = this.mGshSessions.elements();
        while (elements.hasMoreElements()) {
            arrayList.add(elements.nextElement().asBinder());
        }
        return arrayList;
    }

    public IGeolocSharing getGeolocSharing(String str) throws ServerApiException {
        return this.mGshSessions.get(str);
    }

    public void addEventListener(IGeolocSharingListener iGeolocSharingListener) throws ServerApiException {
        synchronized (this.mLock) {
            this.mGeolocSharingEventBroadcaster.addEventListener(iGeolocSharingListener);
        }
    }

    public void removeEventListener(IGeolocSharingListener iGeolocSharingListener) throws ServerApiException {
        synchronized (this.mLock) {
            this.mGeolocSharingEventBroadcaster.removeEventListener(iGeolocSharingListener);
        }
    }

    public void deleteAllGeolocSharings() throws ServerApiException {
        Map<String, Set<String>> geoMessage = getGeoMessage("content_type ='application/vnd.gsma.rcspushlocation+xml'");
        if (geoMessage == null) {
            Log.e(LOG_TAG, "deleteAllGeolocSharings: Message not found.");
            return;
        }
        ArrayList<String> arrayList = new ArrayList<>();
        for (Map.Entry next : geoMessage.entrySet()) {
            arrayList.addAll((Collection) next.getValue());
            ContactId contactId = new ContactId(PhoneUtils.extractNumberFromUri(getImSessionByChatId((String) next.getKey())));
            synchronized (this.mLock) {
                this.mGeolocSharingEventBroadcaster.broadcastDeleted(contactId, new ArrayList((Collection) next.getValue()));
            }
        }
        this.mGlsModule.deleteGeolocSharings(arrayList);
        for (String removeGeolocSharingSession : arrayList) {
            removeGeolocSharingSession(removeGeolocSharingSession);
        }
    }

    public void deleteGeolocSharingsByContactId(ContactId contactId) throws ServerApiException {
        if (contactId != null) {
            HashSet hashSet = new HashSet();
            hashSet.add(ImsUri.parse("tel:" + PhoneUtils.extractNumberFromUri(contactId.toString())));
            ImSession imSessionByParticipants = ImCache.getInstance().getImSessionByParticipants(hashSet, ChatData.ChatType.ONE_TO_ONE_CHAT, "");
            if (imSessionByParticipants == null) {
                Log.e(LOG_TAG, "deleteGeolocSharingsByContactId: No session for geoloc");
                return;
            }
            Map<String, Set<String>> geoMessage = getGeoMessage("is_filetransfer = 1 and chat_id = '" + imSessionByParticipants.getChatId() + "' and " + "content_type" + " ='" + MIMEContentType.LOCATION_PUSH + "'");
            if (geoMessage == null) {
                Log.e(LOG_TAG, "deleteGeolocSharingsByContactId: Message not found.");
                return;
            }
            ArrayList<String> arrayList = new ArrayList<>();
            for (Map.Entry next : geoMessage.entrySet()) {
                arrayList.addAll((Collection) next.getValue());
                synchronized (this.mLock) {
                    this.mGeolocSharingEventBroadcaster.broadcastDeleted(contactId, new ArrayList((Collection) next.getValue()));
                }
            }
            this.mGlsModule.deleteGeolocSharings(arrayList);
            for (String removeGeolocSharingSession : arrayList) {
                removeGeolocSharingSession(removeGeolocSharingSession);
            }
        }
    }

    public void deleteGeolocSharingBySharingId(String str) throws ServerApiException {
        ArrayList arrayList = new ArrayList();
        arrayList.add(str);
        this.mGlsModule.deleteGeolocSharings(arrayList);
        GeolocSharingImpl geolocSharing = getGeolocSharing(str);
        if (geolocSharing == null) {
            String str2 = LOG_TAG;
            Log.e(str2, "deleteGeolocSharingBySharingId, id:" + str + ", GeolocSharingImpl not found.");
            return;
        }
        synchronized (this.mLock) {
            this.mGeolocSharingEventBroadcaster.broadcastDeleted(geolocSharing.getRemoteContact(), arrayList);
        }
        removeGeolocSharingSession(str);
    }

    private String getImSessionByChatId(String str) {
        ImSession imSession = ImCache.getInstance().getImSession(str);
        if (imSession == null) {
            return null;
        }
        return imSession.getParticipantsString().get(0);
    }

    private Map<String, Set<String>> getGeoMessage(String str) {
        ImCache instance = ImCache.getInstance();
        TreeMap treeMap = new TreeMap();
        Cursor queryMessages = instance.queryMessages(new String[]{"_id", "chat_id"}, str, (String[]) null, (String) null);
        if (queryMessages != null) {
            try {
                if (queryMessages.getCount() != 0) {
                    while (queryMessages.moveToNext()) {
                        String string = queryMessages.getString(queryMessages.getColumnIndexOrThrow("chat_id"));
                        if (instance.getImSession(string) != null) {
                            String valueOf = String.valueOf(queryMessages.getInt(queryMessages.getColumnIndexOrThrow("_id")));
                            Set set = (Set) treeMap.get(string);
                            if (set == null) {
                                HashSet hashSet = new HashSet();
                                hashSet.add(valueOf);
                                treeMap.put(string, hashSet);
                            } else {
                                set.add(valueOf);
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
        if (queryMessages != null) {
            queryMessages.close();
        }
        return null;
        throw th;
    }

    private ContactId getContactId(FtMessage ftMessage) {
        return new ContactId(ftMessage.getRemoteUri().getMsisdn());
    }

    private String getSharingId(FtMessage ftMessage) {
        return String.valueOf(ftMessage.getId());
    }

    public static GeolocSharing.ReasonCode translateToReasonCode(CancelReason cancelReason) {
        String str = LOG_TAG;
        Log.d(str, "translateToReasonCode(), CancelReason: " + cancelReason);
        switch (AnonymousClass1.$SwitchMap$com$sec$internal$constants$ims$servicemodules$im$reason$CancelReason[cancelReason.ordinal()]) {
            case 1:
            case 2:
                return GeolocSharing.ReasonCode.ABORTED_BY_SYSTEM;
            case 3:
                return GeolocSharing.ReasonCode.ABORTED_BY_USER;
            case 4:
                return GeolocSharing.ReasonCode.ABORTED_BY_REMOTE;
            case 5:
                return GeolocSharing.ReasonCode.REJECTED_BY_REMOTE;
            case 6:
                return GeolocSharing.ReasonCode.FAILED_SHARING;
            default:
                return GeolocSharing.ReasonCode.UNSPECIFIED;
        }
    }

    /* renamed from: com.sec.internal.ims.servicemodules.tapi.service.api.GeolocSharingServiceImpl$1  reason: invalid class name */
    static /* synthetic */ class AnonymousClass1 {
        static final /* synthetic */ int[] $SwitchMap$com$sec$internal$constants$ims$servicemodules$im$reason$CancelReason;

        /* JADX WARNING: Can't wrap try/catch for region: R(38:0|1|2|3|4|5|6|7|8|9|10|11|12|13|14|15|16|17|18|19|20|21|22|23|24|25|26|27|28|29|30|31|32|33|34|35|36|(3:37|38|40)) */
        /* JADX WARNING: Can't wrap try/catch for region: R(40:0|1|2|3|4|5|6|7|8|9|10|11|12|13|14|15|16|17|18|19|20|21|22|23|24|25|26|27|28|29|30|31|32|33|34|35|36|37|38|40) */
        /* JADX WARNING: Failed to process nested try/catch */
        /* JADX WARNING: Missing exception handler attribute for start block: B:11:0x003e */
        /* JADX WARNING: Missing exception handler attribute for start block: B:13:0x0049 */
        /* JADX WARNING: Missing exception handler attribute for start block: B:15:0x0054 */
        /* JADX WARNING: Missing exception handler attribute for start block: B:17:0x0060 */
        /* JADX WARNING: Missing exception handler attribute for start block: B:19:0x006c */
        /* JADX WARNING: Missing exception handler attribute for start block: B:21:0x0078 */
        /* JADX WARNING: Missing exception handler attribute for start block: B:23:0x0084 */
        /* JADX WARNING: Missing exception handler attribute for start block: B:25:0x0090 */
        /* JADX WARNING: Missing exception handler attribute for start block: B:27:0x009c */
        /* JADX WARNING: Missing exception handler attribute for start block: B:29:0x00a8 */
        /* JADX WARNING: Missing exception handler attribute for start block: B:31:0x00b4 */
        /* JADX WARNING: Missing exception handler attribute for start block: B:33:0x00c0 */
        /* JADX WARNING: Missing exception handler attribute for start block: B:35:0x00cc */
        /* JADX WARNING: Missing exception handler attribute for start block: B:37:0x00d8 */
        /* JADX WARNING: Missing exception handler attribute for start block: B:3:0x0012 */
        /* JADX WARNING: Missing exception handler attribute for start block: B:5:0x001d */
        /* JADX WARNING: Missing exception handler attribute for start block: B:7:0x0028 */
        /* JADX WARNING: Missing exception handler attribute for start block: B:9:0x0033 */
        static {
            /*
                com.sec.internal.constants.ims.servicemodules.im.reason.CancelReason[] r0 = com.sec.internal.constants.ims.servicemodules.im.reason.CancelReason.values()
                int r0 = r0.length
                int[] r0 = new int[r0]
                $SwitchMap$com$sec$internal$constants$ims$servicemodules$im$reason$CancelReason = r0
                com.sec.internal.constants.ims.servicemodules.im.reason.CancelReason r1 = com.sec.internal.constants.ims.servicemodules.im.reason.CancelReason.TIME_OUT     // Catch:{ NoSuchFieldError -> 0x0012 }
                int r1 = r1.ordinal()     // Catch:{ NoSuchFieldError -> 0x0012 }
                r2 = 1
                r0[r1] = r2     // Catch:{ NoSuchFieldError -> 0x0012 }
            L_0x0012:
                int[] r0 = $SwitchMap$com$sec$internal$constants$ims$servicemodules$im$reason$CancelReason     // Catch:{ NoSuchFieldError -> 0x001d }
                com.sec.internal.constants.ims.servicemodules.im.reason.CancelReason r1 = com.sec.internal.constants.ims.servicemodules.im.reason.CancelReason.CANCELED_BY_SYSTEM     // Catch:{ NoSuchFieldError -> 0x001d }
                int r1 = r1.ordinal()     // Catch:{ NoSuchFieldError -> 0x001d }
                r2 = 2
                r0[r1] = r2     // Catch:{ NoSuchFieldError -> 0x001d }
            L_0x001d:
                int[] r0 = $SwitchMap$com$sec$internal$constants$ims$servicemodules$im$reason$CancelReason     // Catch:{ NoSuchFieldError -> 0x0028 }
                com.sec.internal.constants.ims.servicemodules.im.reason.CancelReason r1 = com.sec.internal.constants.ims.servicemodules.im.reason.CancelReason.CANCELED_BY_USER     // Catch:{ NoSuchFieldError -> 0x0028 }
                int r1 = r1.ordinal()     // Catch:{ NoSuchFieldError -> 0x0028 }
                r2 = 3
                r0[r1] = r2     // Catch:{ NoSuchFieldError -> 0x0028 }
            L_0x0028:
                int[] r0 = $SwitchMap$com$sec$internal$constants$ims$servicemodules$im$reason$CancelReason     // Catch:{ NoSuchFieldError -> 0x0033 }
                com.sec.internal.constants.ims.servicemodules.im.reason.CancelReason r1 = com.sec.internal.constants.ims.servicemodules.im.reason.CancelReason.CANCELED_BY_REMOTE     // Catch:{ NoSuchFieldError -> 0x0033 }
                int r1 = r1.ordinal()     // Catch:{ NoSuchFieldError -> 0x0033 }
                r2 = 4
                r0[r1] = r2     // Catch:{ NoSuchFieldError -> 0x0033 }
            L_0x0033:
                int[] r0 = $SwitchMap$com$sec$internal$constants$ims$servicemodules$im$reason$CancelReason     // Catch:{ NoSuchFieldError -> 0x003e }
                com.sec.internal.constants.ims.servicemodules.im.reason.CancelReason r1 = com.sec.internal.constants.ims.servicemodules.im.reason.CancelReason.REJECTED_BY_REMOTE     // Catch:{ NoSuchFieldError -> 0x003e }
                int r1 = r1.ordinal()     // Catch:{ NoSuchFieldError -> 0x003e }
                r2 = 5
                r0[r1] = r2     // Catch:{ NoSuchFieldError -> 0x003e }
            L_0x003e:
                int[] r0 = $SwitchMap$com$sec$internal$constants$ims$servicemodules$im$reason$CancelReason     // Catch:{ NoSuchFieldError -> 0x0049 }
                com.sec.internal.constants.ims.servicemodules.im.reason.CancelReason r1 = com.sec.internal.constants.ims.servicemodules.im.reason.CancelReason.DEVICE_UNREGISTERED     // Catch:{ NoSuchFieldError -> 0x0049 }
                int r1 = r1.ordinal()     // Catch:{ NoSuchFieldError -> 0x0049 }
                r2 = 6
                r0[r1] = r2     // Catch:{ NoSuchFieldError -> 0x0049 }
            L_0x0049:
                int[] r0 = $SwitchMap$com$sec$internal$constants$ims$servicemodules$im$reason$CancelReason     // Catch:{ NoSuchFieldError -> 0x0054 }
                com.sec.internal.constants.ims.servicemodules.im.reason.CancelReason r1 = com.sec.internal.constants.ims.servicemodules.im.reason.CancelReason.NOT_AUTHORIZED     // Catch:{ NoSuchFieldError -> 0x0054 }
                int r1 = r1.ordinal()     // Catch:{ NoSuchFieldError -> 0x0054 }
                r2 = 7
                r0[r1] = r2     // Catch:{ NoSuchFieldError -> 0x0054 }
            L_0x0054:
                int[] r0 = $SwitchMap$com$sec$internal$constants$ims$servicemodules$im$reason$CancelReason     // Catch:{ NoSuchFieldError -> 0x0060 }
                com.sec.internal.constants.ims.servicemodules.im.reason.CancelReason r1 = com.sec.internal.constants.ims.servicemodules.im.reason.CancelReason.REMOTE_BLOCKED     // Catch:{ NoSuchFieldError -> 0x0060 }
                int r1 = r1.ordinal()     // Catch:{ NoSuchFieldError -> 0x0060 }
                r2 = 8
                r0[r1] = r2     // Catch:{ NoSuchFieldError -> 0x0060 }
            L_0x0060:
                int[] r0 = $SwitchMap$com$sec$internal$constants$ims$servicemodules$im$reason$CancelReason     // Catch:{ NoSuchFieldError -> 0x006c }
                com.sec.internal.constants.ims.servicemodules.im.reason.CancelReason r1 = com.sec.internal.constants.ims.servicemodules.im.reason.CancelReason.ERROR     // Catch:{ NoSuchFieldError -> 0x006c }
                int r1 = r1.ordinal()     // Catch:{ NoSuchFieldError -> 0x006c }
                r2 = 9
                r0[r1] = r2     // Catch:{ NoSuchFieldError -> 0x006c }
            L_0x006c:
                int[] r0 = $SwitchMap$com$sec$internal$constants$ims$servicemodules$im$reason$CancelReason     // Catch:{ NoSuchFieldError -> 0x0078 }
                com.sec.internal.constants.ims.servicemodules.im.reason.CancelReason r1 = com.sec.internal.constants.ims.servicemodules.im.reason.CancelReason.REMOTE_TEMPORARILY_UNAVAILABLE     // Catch:{ NoSuchFieldError -> 0x0078 }
                int r1 = r1.ordinal()     // Catch:{ NoSuchFieldError -> 0x0078 }
                r2 = 10
                r0[r1] = r2     // Catch:{ NoSuchFieldError -> 0x0078 }
            L_0x0078:
                int[] r0 = $SwitchMap$com$sec$internal$constants$ims$servicemodules$im$reason$CancelReason     // Catch:{ NoSuchFieldError -> 0x0084 }
                com.sec.internal.constants.ims.servicemodules.im.reason.CancelReason r1 = com.sec.internal.constants.ims.servicemodules.im.reason.CancelReason.VALIDITY_EXPIRED     // Catch:{ NoSuchFieldError -> 0x0084 }
                int r1 = r1.ordinal()     // Catch:{ NoSuchFieldError -> 0x0084 }
                r2 = 11
                r0[r1] = r2     // Catch:{ NoSuchFieldError -> 0x0084 }
            L_0x0084:
                int[] r0 = $SwitchMap$com$sec$internal$constants$ims$servicemodules$im$reason$CancelReason     // Catch:{ NoSuchFieldError -> 0x0090 }
                com.sec.internal.constants.ims.servicemodules.im.reason.CancelReason r1 = com.sec.internal.constants.ims.servicemodules.im.reason.CancelReason.INVALID_REQUEST     // Catch:{ NoSuchFieldError -> 0x0090 }
                int r1 = r1.ordinal()     // Catch:{ NoSuchFieldError -> 0x0090 }
                r2 = 12
                r0[r1] = r2     // Catch:{ NoSuchFieldError -> 0x0090 }
            L_0x0090:
                int[] r0 = $SwitchMap$com$sec$internal$constants$ims$servicemodules$im$reason$CancelReason     // Catch:{ NoSuchFieldError -> 0x009c }
                com.sec.internal.constants.ims.servicemodules.im.reason.CancelReason r1 = com.sec.internal.constants.ims.servicemodules.im.reason.CancelReason.REMOTE_USER_INVALID     // Catch:{ NoSuchFieldError -> 0x009c }
                int r1 = r1.ordinal()     // Catch:{ NoSuchFieldError -> 0x009c }
                r2 = 13
                r0[r1] = r2     // Catch:{ NoSuchFieldError -> 0x009c }
            L_0x009c:
                int[] r0 = $SwitchMap$com$sec$internal$constants$ims$servicemodules$im$reason$CancelReason     // Catch:{ NoSuchFieldError -> 0x00a8 }
                com.sec.internal.constants.ims.servicemodules.im.reason.CancelReason r1 = com.sec.internal.constants.ims.servicemodules.im.reason.CancelReason.NO_RESPONSE     // Catch:{ NoSuchFieldError -> 0x00a8 }
                int r1 = r1.ordinal()     // Catch:{ NoSuchFieldError -> 0x00a8 }
                r2 = 14
                r0[r1] = r2     // Catch:{ NoSuchFieldError -> 0x00a8 }
            L_0x00a8:
                int[] r0 = $SwitchMap$com$sec$internal$constants$ims$servicemodules$im$reason$CancelReason     // Catch:{ NoSuchFieldError -> 0x00b4 }
                com.sec.internal.constants.ims.servicemodules.im.reason.CancelReason r1 = com.sec.internal.constants.ims.servicemodules.im.reason.CancelReason.FORBIDDEN_NO_RETRY_FALLBACK     // Catch:{ NoSuchFieldError -> 0x00b4 }
                int r1 = r1.ordinal()     // Catch:{ NoSuchFieldError -> 0x00b4 }
                r2 = 15
                r0[r1] = r2     // Catch:{ NoSuchFieldError -> 0x00b4 }
            L_0x00b4:
                int[] r0 = $SwitchMap$com$sec$internal$constants$ims$servicemodules$im$reason$CancelReason     // Catch:{ NoSuchFieldError -> 0x00c0 }
                com.sec.internal.constants.ims.servicemodules.im.reason.CancelReason r1 = com.sec.internal.constants.ims.servicemodules.im.reason.CancelReason.CONTENT_REACHED_DOWNSIZE     // Catch:{ NoSuchFieldError -> 0x00c0 }
                int r1 = r1.ordinal()     // Catch:{ NoSuchFieldError -> 0x00c0 }
                r2 = 16
                r0[r1] = r2     // Catch:{ NoSuchFieldError -> 0x00c0 }
            L_0x00c0:
                int[] r0 = $SwitchMap$com$sec$internal$constants$ims$servicemodules$im$reason$CancelReason     // Catch:{ NoSuchFieldError -> 0x00cc }
                com.sec.internal.constants.ims.servicemodules.im.reason.CancelReason r1 = com.sec.internal.constants.ims.servicemodules.im.reason.CancelReason.LOCALLY_ABORTED     // Catch:{ NoSuchFieldError -> 0x00cc }
                int r1 = r1.ordinal()     // Catch:{ NoSuchFieldError -> 0x00cc }
                r2 = 17
                r0[r1] = r2     // Catch:{ NoSuchFieldError -> 0x00cc }
            L_0x00cc:
                int[] r0 = $SwitchMap$com$sec$internal$constants$ims$servicemodules$im$reason$CancelReason     // Catch:{ NoSuchFieldError -> 0x00d8 }
                com.sec.internal.constants.ims.servicemodules.im.reason.CancelReason r1 = com.sec.internal.constants.ims.servicemodules.im.reason.CancelReason.CONNECTION_RELEASED     // Catch:{ NoSuchFieldError -> 0x00d8 }
                int r1 = r1.ordinal()     // Catch:{ NoSuchFieldError -> 0x00d8 }
                r2 = 18
                r0[r1] = r2     // Catch:{ NoSuchFieldError -> 0x00d8 }
            L_0x00d8:
                int[] r0 = $SwitchMap$com$sec$internal$constants$ims$servicemodules$im$reason$CancelReason     // Catch:{ NoSuchFieldError -> 0x00e4 }
                com.sec.internal.constants.ims.servicemodules.im.reason.CancelReason r1 = com.sec.internal.constants.ims.servicemodules.im.reason.CancelReason.UNKNOWN     // Catch:{ NoSuchFieldError -> 0x00e4 }
                int r1 = r1.ordinal()     // Catch:{ NoSuchFieldError -> 0x00e4 }
                r2 = 19
                r0[r1] = r2     // Catch:{ NoSuchFieldError -> 0x00e4 }
            L_0x00e4:
                return
            */
            throw new UnsupportedOperationException("Method not decompiled: com.sec.internal.ims.servicemodules.tapi.service.api.GeolocSharingServiceImpl.AnonymousClass1.<clinit>():void");
        }
    }

    private void notifyStateChanged(FtMessage ftMessage, GeolocSharing.State state, GeolocSharing.ReasonCode reasonCode) {
        String str = LOG_TAG;
        Log.d(str, "notifyStateChanged state=" + state + ", reason=" + reasonCode);
        if (ftMessage.getRemoteUri() != null) {
            synchronized (this.mLock) {
                this.mGeolocSharingEventBroadcaster.broadcastGeolocSharingStateChanged(getContactId(ftMessage), getSharingId(ftMessage), state, reasonCode);
            }
        }
    }

    public void handleGeolocSharingProgress(FtMessage ftMessage) {
        String str = LOG_TAG;
        Log.d(str, "handleSharingProgress id:" + ftMessage.getId() + "  progress:" + ((ftMessage.getTransferredBytes() * 100) / ftMessage.getFileSize()) + "%.");
        if (ftMessage.getRemoteUri() != null) {
            synchronized (this.mLock) {
                this.mGeolocSharingEventBroadcaster.broadcastGeolocSharingprogress(getContactId(ftMessage), getSharingId(ftMessage), ftMessage.getTransferredBytes(), ftMessage.getFileSize());
            }
        }
    }

    public void onFileTransferCreated(FtMessage ftMessage) {
        if (this.mGshSessions.containsKey(String.valueOf(ftMessage.getId()))) {
            this.mGlsModule.startLocationShareInCall(ftMessage.getImdnId());
            notifyStateChanged(ftMessage, GeolocSharing.State.INITIATING, GeolocSharing.ReasonCode.UNSPECIFIED);
        }
    }

    public void onFileTransferReceived(FtMessage ftMessage) {
        addGeolocSharingSession(new GeolocSharingImpl(ftMessage, this.mGlsModule));
        notifyStateChanged(ftMessage, GeolocSharing.State.INVITED, GeolocSharing.ReasonCode.UNSPECIFIED);
        UserHandle subscriptionUserHandle = TelephonyUtilsWrapper.getSubscriptionUserHandle(this.mContext, SimUtil.getSubId(this.mGlsModule.getPhoneIdByMessageId(ftMessage.getId())));
        if (subscriptionUserHandle == null) {
            subscriptionUserHandle = ContextExt.CURRENT_OR_SELF;
        }
        this.mGeolocSharingEventBroadcaster.broadcastGeolocSharingInvitation(getSharingId(ftMessage), subscriptionUserHandle);
    }

    public void onTransferProgressReceived(FtMessage ftMessage) {
        handleGeolocSharingProgress(ftMessage);
        if (ftMessage.getRemoteUri() != null) {
            synchronized (this.mLock) {
                this.mGeolocSharingEventBroadcaster.broadcastGeolocSharingprogress(getContactId(ftMessage), getSharingId(ftMessage), ftMessage.getTransferredBytes(), ftMessage.getFileSize());
            }
        }
    }

    public void onTransferStarted(FtMessage ftMessage) {
        notifyStateChanged(ftMessage, GeolocSharing.State.STARTED, GeolocSharing.ReasonCode.UNSPECIFIED);
    }

    public void onTransferCompleted(FtMessage ftMessage) {
        notifyStateChanged(ftMessage, GeolocSharing.State.TRANSFERRED, GeolocSharing.ReasonCode.UNSPECIFIED);
    }

    public void onTransferCanceled(FtMessage ftMessage) {
        CancelReason cancelReason = ftMessage.getCancelReason();
        GeolocSharing.ReasonCode reasonCode = GeolocSharing.ReasonCode.UNSPECIFIED;
        if (cancelReason != null) {
            reasonCode = translateToReasonCode(cancelReason);
        }
        notifyStateChanged(ftMessage, GeolocSharing.State.ABORTED, reasonCode);
    }
}
