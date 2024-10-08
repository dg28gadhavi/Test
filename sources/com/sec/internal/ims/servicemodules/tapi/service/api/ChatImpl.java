package com.sec.internal.ims.servicemodules.tapi.service.api;

import android.location.Location;
import android.os.Binder;
import android.os.RemoteException;
import android.util.Log;
import com.gsma.services.rcs.Geoloc;
import com.gsma.services.rcs.chat.IChatMessage;
import com.gsma.services.rcs.chat.IOneToOneChat;
import com.gsma.services.rcs.contact.ContactId;
import com.sec.ims.options.Capabilities;
import com.sec.ims.util.ImsUri;
import com.sec.internal.constants.ims.MIMEContentType;
import com.sec.internal.constants.ims.servicemodules.im.NotificationStatus;
import com.sec.internal.ims.registry.ImsRegistry;
import com.sec.internal.ims.servicemodules.im.ImMessage;
import com.sec.internal.ims.servicemodules.im.ImSession;
import com.sec.internal.interfaces.ims.servicemodules.gls.IGlsModule;
import com.sec.internal.interfaces.ims.servicemodules.im.IImModule;
import java.util.ArrayList;
import java.util.EnumSet;
import java.util.List;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.Future;

public class ChatImpl extends IOneToOneChat.Stub {
    private static final String LOG_TAG = ChatImpl.class.getSimpleName();
    private ContactId contact;
    private IGlsModule mGlsModule = null;
    private IImModule mImModule = null;
    private ImSession mSession = null;

    public void openChat() throws ServerApiException {
    }

    public ChatImpl(String str, ImSession imSession, IImModule iImModule) {
        this.contact = new ContactId(str);
        this.mSession = imSession;
        this.mImModule = iImModule;
        this.mGlsModule = ImsRegistry.getServiceModuleManager().getGlsModule();
    }

    public void resendMessage(String str) throws ServerApiException {
        Log.d(LOG_TAG, "start : resendMessage()");
        this.mImModule.resendMessage(Integer.valueOf(str).intValue());
    }

    public ImSession getCoreSession() {
        return this.mSession;
    }

    public ContactId getRemoteContact() throws ServerApiException {
        return this.contact;
    }

    public IChatMessage sendMessage(String str) throws ServerApiException {
        Log.d(LOG_TAG, "start : sendMessage()");
        try {
            if (this.mImModule.getImSession(this.mSession.getChatId()) == null) {
                ArrayList arrayList = new ArrayList();
                arrayList.add(ImsUri.parse("tel:" + this.contact.toString()));
                this.mSession = this.mImModule.createChat(arrayList, ChatServiceImpl.SUBJECT, MIMEContentType.PLAIN_TEXT, -1, (String) null).get();
            }
            ImMessage imMessage = this.mImModule.sendMessage(this.mSession.getChatId(), str, EnumSet.of(NotificationStatus.DELIVERED, NotificationStatus.DISPLAYED), MIMEContentType.PLAIN_TEXT, (String) null, -1, false, false, false, (List<ImsUri>) null, false, (String) null, (String) null, (String) null, (String) null).get();
            if (imMessage != null) {
                return new ChatMessageImpl(String.valueOf(imMessage.getId()));
            }
            throw new ServerApiException("Can not make a message");
        } catch (InterruptedException e) {
            e.printStackTrace();
            return null;
        } catch (ExecutionException e2) {
            e2.printStackTrace();
            return null;
        }
    }

    public IChatMessage sendGeoloc(Geoloc geoloc) throws ServerApiException {
        Log.d(LOG_TAG, "start : send Geolocation Message()");
        Location location = new Location("gps");
        location.setLatitude(geoloc.getLatitude());
        location.setLongitude(geoloc.getLongitude());
        location.setAccuracy(geoloc.getAccuracy());
        try {
            Future<ImMessage> shareLocationInChat = this.mGlsModule.shareLocationInChat(this.mSession.getChatId(), EnumSet.of(NotificationStatus.DELIVERED, NotificationStatus.DISPLAYED), location, geoloc.getLabel(), (String) null, (String) null, this.mSession.getRemoteUri(), false, (String) null);
            ImMessage imMessage = shareLocationInChat != null ? shareLocationInChat.get() : null;
            if (imMessage != null) {
                return new ChatMessageImpl(String.valueOf(imMessage.getId()));
            }
            throw new ServerApiException("Can not make a message");
        } catch (InterruptedException e) {
            e.printStackTrace();
            return null;
        } catch (ExecutionException e2) {
            e2.printStackTrace();
            return null;
        }
    }

    public void sendIsComposingEvent(boolean z) throws ServerApiException {
        Log.d(LOG_TAG, "start : sendIsComposingEvent()");
        this.mSession.sendComposing(z, 3);
    }

    public boolean isAllowedToSendMessage() throws ServerApiException {
        long clearCallingIdentity = Binder.clearCallingIdentity();
        try {
            Capabilities ownCapabilities = ImsRegistry.getServiceModuleManager().getCapabilityDiscoveryModule().getOwnCapabilities();
            return ownCapabilities != null && ownCapabilities.hasFeature(Capabilities.FEATURE_CHAT_CPM);
        } finally {
            Binder.restoreCallingIdentity(clearCallingIdentity);
        }
    }

    public void setComposingStatus(boolean z) throws RemoteException {
        Log.d(LOG_TAG, "start : setComposingStatus()");
        ImSession imSession = this.mSession;
        if (imSession != null) {
            imSession.sendComposing(z, 3);
        }
    }
}
