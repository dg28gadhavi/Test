package com.sec.internal.ims.servicemodules.tapi.service.api;

import android.location.Location;
import android.os.Binder;
import android.os.RemoteException;
import android.util.Log;
import com.gsma.services.rcs.Geoloc;
import com.gsma.services.rcs.chat.GroupChat;
import com.gsma.services.rcs.chat.IChatMessage;
import com.gsma.services.rcs.chat.IGroupChat;
import com.gsma.services.rcs.contact.ContactId;
import com.sec.ims.options.Capabilities;
import com.sec.ims.util.ImsUri;
import com.sec.internal.constants.ims.MIMEContentType;
import com.sec.internal.constants.ims.servicemodules.im.ImParticipant;
import com.sec.internal.constants.ims.servicemodules.im.NotificationStatus;
import com.sec.internal.ims.registry.ImsRegistry;
import com.sec.internal.ims.servicemodules.im.ImCache;
import com.sec.internal.ims.servicemodules.im.ImMessage;
import com.sec.internal.ims.servicemodules.im.ImSession;
import com.sec.internal.interfaces.ims.servicemodules.im.IImModule;
import java.util.ArrayList;
import java.util.EnumSet;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.Future;

public class GroupChatImpl extends IGroupChat.Stub {
    private static final String LOG_TAG = GroupChatImpl.class.getSimpleName();
    private GroupChat.State mGroupChatState = GroupChat.State.STARTED;
    private IImModule mImModule = null;
    private GroupChat.ReasonCode mReasonCode = GroupChat.ReasonCode.UNSPECIFIED;
    private ImSession mSession = null;
    private boolean mSessionLeaved = false;

    public long getTimestamp() {
        return 0;
    }

    public void openChat() throws RemoteException {
    }

    public GroupChatImpl(ImSession imSession) {
        this.mSession = imSession;
        this.mImModule = ImsRegistry.getServiceModuleManager().getImModule();
    }

    public GroupChatImpl(String str) {
        this.mSession = ImCache.getInstance().getImSession(str);
        this.mImModule = ImsRegistry.getServiceModuleManager().getImModule();
    }

    /* renamed from: com.sec.internal.ims.servicemodules.tapi.service.api.GroupChatImpl$1  reason: invalid class name */
    static /* synthetic */ class AnonymousClass1 {
        static final /* synthetic */ int[] $SwitchMap$com$sec$internal$constants$ims$servicemodules$im$ImParticipant$Status;

        /* JADX WARNING: Can't wrap try/catch for region: R(18:0|1|2|3|4|5|6|7|8|9|10|11|12|13|14|15|16|18) */
        /* JADX WARNING: Code restructure failed: missing block: B:19:?, code lost:
            return;
         */
        /* JADX WARNING: Failed to process nested try/catch */
        /* JADX WARNING: Missing exception handler attribute for start block: B:11:0x003e */
        /* JADX WARNING: Missing exception handler attribute for start block: B:13:0x0049 */
        /* JADX WARNING: Missing exception handler attribute for start block: B:15:0x0054 */
        /* JADX WARNING: Missing exception handler attribute for start block: B:3:0x0012 */
        /* JADX WARNING: Missing exception handler attribute for start block: B:5:0x001d */
        /* JADX WARNING: Missing exception handler attribute for start block: B:7:0x0028 */
        /* JADX WARNING: Missing exception handler attribute for start block: B:9:0x0033 */
        static {
            /*
                com.sec.internal.constants.ims.servicemodules.im.ImParticipant$Status[] r0 = com.sec.internal.constants.ims.servicemodules.im.ImParticipant.Status.values()
                int r0 = r0.length
                int[] r0 = new int[r0]
                $SwitchMap$com$sec$internal$constants$ims$servicemodules$im$ImParticipant$Status = r0
                com.sec.internal.constants.ims.servicemodules.im.ImParticipant$Status r1 = com.sec.internal.constants.ims.servicemodules.im.ImParticipant.Status.INITIAL     // Catch:{ NoSuchFieldError -> 0x0012 }
                int r1 = r1.ordinal()     // Catch:{ NoSuchFieldError -> 0x0012 }
                r2 = 1
                r0[r1] = r2     // Catch:{ NoSuchFieldError -> 0x0012 }
            L_0x0012:
                int[] r0 = $SwitchMap$com$sec$internal$constants$ims$servicemodules$im$ImParticipant$Status     // Catch:{ NoSuchFieldError -> 0x001d }
                com.sec.internal.constants.ims.servicemodules.im.ImParticipant$Status r1 = com.sec.internal.constants.ims.servicemodules.im.ImParticipant.Status.INVITED     // Catch:{ NoSuchFieldError -> 0x001d }
                int r1 = r1.ordinal()     // Catch:{ NoSuchFieldError -> 0x001d }
                r2 = 2
                r0[r1] = r2     // Catch:{ NoSuchFieldError -> 0x001d }
            L_0x001d:
                int[] r0 = $SwitchMap$com$sec$internal$constants$ims$servicemodules$im$ImParticipant$Status     // Catch:{ NoSuchFieldError -> 0x0028 }
                com.sec.internal.constants.ims.servicemodules.im.ImParticipant$Status r1 = com.sec.internal.constants.ims.servicemodules.im.ImParticipant.Status.ACCEPTED     // Catch:{ NoSuchFieldError -> 0x0028 }
                int r1 = r1.ordinal()     // Catch:{ NoSuchFieldError -> 0x0028 }
                r2 = 3
                r0[r1] = r2     // Catch:{ NoSuchFieldError -> 0x0028 }
            L_0x0028:
                int[] r0 = $SwitchMap$com$sec$internal$constants$ims$servicemodules$im$ImParticipant$Status     // Catch:{ NoSuchFieldError -> 0x0033 }
                com.sec.internal.constants.ims.servicemodules.im.ImParticipant$Status r1 = com.sec.internal.constants.ims.servicemodules.im.ImParticipant.Status.PENDING     // Catch:{ NoSuchFieldError -> 0x0033 }
                int r1 = r1.ordinal()     // Catch:{ NoSuchFieldError -> 0x0033 }
                r2 = 4
                r0[r1] = r2     // Catch:{ NoSuchFieldError -> 0x0033 }
            L_0x0033:
                int[] r0 = $SwitchMap$com$sec$internal$constants$ims$servicemodules$im$ImParticipant$Status     // Catch:{ NoSuchFieldError -> 0x003e }
                com.sec.internal.constants.ims.servicemodules.im.ImParticipant$Status r1 = com.sec.internal.constants.ims.servicemodules.im.ImParticipant.Status.DECLINED     // Catch:{ NoSuchFieldError -> 0x003e }
                int r1 = r1.ordinal()     // Catch:{ NoSuchFieldError -> 0x003e }
                r2 = 5
                r0[r1] = r2     // Catch:{ NoSuchFieldError -> 0x003e }
            L_0x003e:
                int[] r0 = $SwitchMap$com$sec$internal$constants$ims$servicemodules$im$ImParticipant$Status     // Catch:{ NoSuchFieldError -> 0x0049 }
                com.sec.internal.constants.ims.servicemodules.im.ImParticipant$Status r1 = com.sec.internal.constants.ims.servicemodules.im.ImParticipant.Status.GONE     // Catch:{ NoSuchFieldError -> 0x0049 }
                int r1 = r1.ordinal()     // Catch:{ NoSuchFieldError -> 0x0049 }
                r2 = 6
                r0[r1] = r2     // Catch:{ NoSuchFieldError -> 0x0049 }
            L_0x0049:
                int[] r0 = $SwitchMap$com$sec$internal$constants$ims$servicemodules$im$ImParticipant$Status     // Catch:{ NoSuchFieldError -> 0x0054 }
                com.sec.internal.constants.ims.servicemodules.im.ImParticipant$Status r1 = com.sec.internal.constants.ims.servicemodules.im.ImParticipant.Status.TIMEOUT     // Catch:{ NoSuchFieldError -> 0x0054 }
                int r1 = r1.ordinal()     // Catch:{ NoSuchFieldError -> 0x0054 }
                r2 = 7
                r0[r1] = r2     // Catch:{ NoSuchFieldError -> 0x0054 }
            L_0x0054:
                int[] r0 = $SwitchMap$com$sec$internal$constants$ims$servicemodules$im$ImParticipant$Status     // Catch:{ NoSuchFieldError -> 0x0060 }
                com.sec.internal.constants.ims.servicemodules.im.ImParticipant$Status r1 = com.sec.internal.constants.ims.servicemodules.im.ImParticipant.Status.TO_INVITE     // Catch:{ NoSuchFieldError -> 0x0060 }
                int r1 = r1.ordinal()     // Catch:{ NoSuchFieldError -> 0x0060 }
                r2 = 8
                r0[r1] = r2     // Catch:{ NoSuchFieldError -> 0x0060 }
            L_0x0060:
                return
            */
            throw new UnsupportedOperationException("Method not decompiled: com.sec.internal.ims.servicemodules.tapi.service.api.GroupChatImpl.AnonymousClass1.<clinit>():void");
        }
    }

    public GroupChat.ParticipantStatus convertStatus(ImParticipant.Status status) {
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

    public String getChatId() {
        return this.mSession.getChatId();
    }

    public String getSubject() {
        return this.mSession.getSubject();
    }

    public Map getParticipants() {
        HashMap hashMap = new HashMap();
        for (ImParticipant next : this.mSession.getParticipants()) {
            hashMap.put(next.getUri().toString(), Integer.valueOf(convertStatus(next.getStatus()).ordinal()));
        }
        return hashMap;
    }

    public int getDirection() {
        return this.mSession.getDirection().getId();
    }

    public GroupChat.State getState() {
        return this.mGroupChatState;
    }

    public GroupChat.ReasonCode getReasonCode() {
        return this.mReasonCode;
    }

    public boolean canSendMessage() throws RemoteException {
        return !this.mSessionLeaved;
    }

    public IChatMessage sendMessage(String str) {
        Log.d(LOG_TAG, "start : sendMessage()");
        try {
            IImModule iImModule = this.mImModule;
            String chatId = this.mSession.getChatId();
            EnumSet of = EnumSet.of(NotificationStatus.DELIVERED, NotificationStatus.DISPLAYED);
            ImMessage imMessage = iImModule.sendMessage(chatId, str, of, MIMEContentType.PLAIN_TEXT, System.currentTimeMillis() + "", -1, false, false, false, (List<ImsUri>) null, false, (String) null, (String) null, (String) null, (String) null).get();
            if (imMessage == null) {
                return null;
            }
            return new ChatMessageImpl(String.valueOf(imMessage.getId()));
        } catch (InterruptedException e) {
            e.printStackTrace();
            return null;
        } catch (ExecutionException e2) {
            e2.printStackTrace();
            return null;
        }
    }

    public IChatMessage sendGeoloc(Geoloc geoloc) throws RemoteException {
        Location location = new Location("gps");
        location.setLatitude(geoloc.getLatitude());
        location.setLongitude(geoloc.getLongitude());
        location.setAccuracy(geoloc.getAccuracy());
        try {
            Future<ImMessage> shareLocationInChat = ImsRegistry.getServiceModuleManager().getGlsModule().shareLocationInChat(this.mSession.getChatId(), EnumSet.of(NotificationStatus.DELIVERED, NotificationStatus.DISPLAYED), location, geoloc.getLabel(), (String) null, (String) null, (ImsUri) null, true, (String) null);
            ImMessage imMessage = shareLocationInChat != null ? shareLocationInChat.get() : null;
            if (imMessage != null) {
                return new ChatMessageImpl(String.valueOf(imMessage.getId()));
            }
            throw new ServerApiException("Can not get imMessage with messageId ");
        } catch (InterruptedException e) {
            e.printStackTrace();
            return null;
        } catch (ExecutionException e2) {
            e2.printStackTrace();
            return null;
        }
    }

    public void sendIsComposingEvent(boolean z) {
        Log.d(LOG_TAG, "start : sendIsComposingEvent()");
        this.mSession.sendComposing(z, 1);
    }

    public boolean canAddParticipants() throws RemoteException {
        long clearCallingIdentity = Binder.clearCallingIdentity();
        try {
            Capabilities ownCapabilities = ImsRegistry.getServiceModuleManager().getCapabilityDiscoveryModule().getOwnCapabilities();
            boolean z = false;
            if (ownCapabilities == null) {
                return false;
            }
            if (!this.mSessionLeaved && ownCapabilities.hasFeature(Capabilities.FEATURE_SF_GROUP_CHAT)) {
                z = true;
            }
            Binder.restoreCallingIdentity(clearCallingIdentity);
            return z;
        } finally {
            Binder.restoreCallingIdentity(clearCallingIdentity);
        }
    }

    /* JADX WARNING: Removed duplicated region for block: B:6:0x001c  */
    /* Code decompiled incorrectly, please refer to instructions dump. */
    public boolean canAddListParticipants(java.util.List<com.gsma.services.rcs.contact.ContactId> r6) throws android.os.RemoteException {
        /*
            r5 = this;
            int r0 = com.sec.internal.helper.SimUtil.getActiveDataPhoneId()
            boolean r5 = r5.mSessionLeaved
            r1 = 0
            if (r5 == 0) goto L_0x000a
            return r1
        L_0x000a:
            com.sec.internal.interfaces.ims.servicemodules.IServiceModuleManager r5 = com.sec.internal.ims.registry.ImsRegistry.getServiceModuleManager()
            com.sec.internal.interfaces.ims.servicemodules.options.ICapabilityDiscoveryModule r5 = r5.getCapabilityDiscoveryModule()
            java.util.Iterator r6 = r6.iterator()
        L_0x0016:
            boolean r2 = r6.hasNext()
            if (r2 == 0) goto L_0x0038
            java.lang.Object r2 = r6.next()
            com.gsma.services.rcs.contact.ContactId r2 = (com.gsma.services.rcs.contact.ContactId) r2
            java.lang.String r2 = r2.toString()
            int r3 = com.sec.ims.options.Capabilities.FEATURE_SF_GROUP_CHAT
            long r3 = (long) r3
            com.sec.ims.options.Capabilities r2 = r5.getCapabilities((java.lang.String) r2, (long) r3, (int) r0)
            if (r2 == 0) goto L_0x0037
            int r3 = com.sec.ims.options.Capabilities.FEATURE_SF_GROUP_CHAT
            boolean r2 = r2.hasFeature(r3)
            if (r2 != 0) goto L_0x0016
        L_0x0037:
            return r1
        L_0x0038:
            r5 = 1
            return r5
        */
        throw new UnsupportedOperationException("Method not decompiled: com.sec.internal.ims.servicemodules.tapi.service.api.GroupChatImpl.canAddListParticipants(java.util.List):boolean");
    }

    public void addParticipants(List<ContactId> list) {
        Log.d(LOG_TAG, "start : addParticipants()");
        ArrayList arrayList = new ArrayList();
        for (ContactId next : list) {
            if (next != null) {
                arrayList.add(ImsUri.parse("tel:" + next.toString()));
            }
        }
        this.mImModule.addParticipants(this.mSession.getChatId(), arrayList);
    }

    public int getMaxParticipants() {
        return this.mSession.getMaxParticipantsCount();
    }

    public void leave() throws RemoteException {
        Log.d(LOG_TAG, "start : leave()");
        this.mImModule.closeChat(this.mSession.getChatId());
        this.mSessionLeaved = true;
    }

    public boolean isAllowedToLeave() throws RemoteException {
        return !this.mSessionLeaved;
    }

    public String getRemoteContact() throws RemoteException {
        for (ImParticipant next : this.mSession.getParticipants()) {
            if (next.getType() == ImParticipant.Type.CHAIRMAN) {
                return next.getUri().toString();
            }
        }
        return null;
    }

    public void setComposingStatus(boolean z) throws RemoteException {
        String str = LOG_TAG;
        Log.d(str, "start : setComposingStatus() ongoing=" + z);
        ImSession imSession = this.mSession;
        if (imSession != null) {
            imSession.sendComposing(z, 3);
        }
    }
}
