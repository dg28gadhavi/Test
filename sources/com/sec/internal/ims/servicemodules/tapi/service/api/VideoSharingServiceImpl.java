package com.sec.internal.ims.servicemodules.tapi.service.api;

import android.os.IBinder;
import android.os.RemoteCallbackList;
import android.os.RemoteException;
import android.util.Log;
import com.gsma.services.rcs.IRcsServiceRegistrationListener;
import com.gsma.services.rcs.RcsServiceRegistration;
import com.gsma.services.rcs.contact.ContactId;
import com.gsma.services.rcs.sharing.video.IVideoPlayer;
import com.gsma.services.rcs.sharing.video.IVideoSharing;
import com.gsma.services.rcs.sharing.video.IVideoSharingListener;
import com.gsma.services.rcs.sharing.video.IVideoSharingService;
import com.gsma.services.rcs.sharing.video.VideoSharingServiceConfiguration;
import com.sec.ims.util.ImsUri;
import com.sec.internal.ims.registry.ImsRegistry;
import com.sec.internal.ims.servicemodules.csh.VideoShare;
import com.sec.internal.ims.servicemodules.csh.event.VshIntents;
import com.sec.internal.ims.servicemodules.tapi.service.broadcaster.IRegistrationStatusBroadcaster;
import com.sec.internal.interfaces.ims.core.IRegistrationManager;
import com.sec.internal.interfaces.ims.servicemodules.csh.IVideoShareModule;
import java.util.ArrayList;
import java.util.Enumeration;
import java.util.Hashtable;
import java.util.List;
import java.util.concurrent.ExecutionException;

public class VideoSharingServiceImpl extends IVideoSharingService.Stub implements IRegistrationStatusBroadcaster {
    private static String LOG_TAG = VideoSharingServiceImpl.class.getName();
    private static Hashtable<String, IVideoSharing> videoSharingSessions = new Hashtable<>();
    private static IVideoShareModule vshModule = null;
    private RemoteCallbackList<IVideoSharingListener> eventListeners = new RemoteCallbackList<>();
    private Object lock = new Object();
    private RemoteCallbackList<IRcsServiceRegistrationListener> serviceListeners = new RemoteCallbackList<>();

    public VideoSharingServiceImpl(IVideoShareModule iVideoShareModule) {
        vshModule = iVideoShareModule;
    }

    public static IVideoShareModule getModule() {
        return vshModule;
    }

    protected static void addVideoSharingSession(String str, VideoSharingImpl videoSharingImpl) {
        String str2 = LOG_TAG;
        Log.d(str2, "Add a vsh session (size=" + videoSharingSessions.size() + ") : sessionid = " + str);
        videoSharingSessions.put(str, videoSharingImpl);
    }

    protected static void removeVideoSharingSession(String str) {
        String str2 = LOG_TAG;
        Log.d(str2, "remove a vsh session (size=" + videoSharingSessions.size() + ") : sessionid = " + str);
        videoSharingSessions.remove(str);
    }

    public boolean isServiceRegistered() throws ServerApiException {
        IRegistrationManager registrationManager = ImsRegistry.getRegistrationManager();
        if (registrationManager == null) {
            return false;
        }
        boolean hasService = registrationManager.getRegistrationInfo()[0].hasService("vs");
        String str = LOG_TAG;
        Log.d(str, "isServiceRegistered() = " + hasService);
        return hasService;
    }

    public void addServiceRegistrationListener(IRcsServiceRegistrationListener iRcsServiceRegistrationListener) {
        synchronized (this.lock) {
            this.serviceListeners.register(iRcsServiceRegistrationListener);
        }
    }

    public void removeServiceRegistrationListener(IRcsServiceRegistrationListener iRcsServiceRegistrationListener) {
        synchronized (this.lock) {
            this.serviceListeners.unregister(iRcsServiceRegistrationListener);
        }
    }

    public void notifyRegistrationEvent(boolean z, RcsServiceRegistration.ReasonCode reasonCode) {
        synchronized (this.lock) {
            int beginBroadcast = this.serviceListeners.beginBroadcast();
            for (int i = 0; i < beginBroadcast; i++) {
                if (z) {
                    try {
                        this.serviceListeners.getBroadcastItem(i).onServiceRegistered();
                    } catch (RemoteException e) {
                        Log.e(LOG_TAG, "Can't notify listener", e);
                    }
                } else {
                    this.serviceListeners.getBroadcastItem(i).onServiceUnregistered(reasonCode);
                }
            }
            this.serviceListeners.finishBroadcast();
        }
    }

    public VideoSharingServiceConfiguration getConfiguration() throws ServerApiException {
        return new VideoSharingServiceConfiguration((long) vshModule.getMaxDurationTime());
    }

    public IVideoSharing getVideoSharing(String str) throws ServerApiException {
        String str2 = LOG_TAG;
        Log.i(str2, "Get video sharing session " + str);
        return videoSharingSessions.get(str);
    }

    public void deleteVideoSharing(String str) throws ServerApiException {
        String str2;
        String str3 = LOG_TAG;
        Log.i(str3, "delete video sharing session " + str);
        ArrayList arrayList = new ArrayList();
        Hashtable<String, IVideoSharing> hashtable = videoSharingSessions;
        if (hashtable != null && hashtable.size() != 0) {
            try {
                str2 = videoSharingSessions.get(str).getRemoteContact();
            } catch (RemoteException e) {
                e.printStackTrace();
                str2 = null;
            }
            ContactId contactId = new ContactId(str2);
            arrayList.add(str);
            videoSharingSessions.remove(str);
            handleVideoSessionDeleted(contactId, arrayList);
        }
    }

    public VideoSharingImpl getVideoSharingByID(String str) {
        String str2 = LOG_TAG;
        Log.i(str2, "Get video sharing session " + str + "; videoSharingSessions = " + videoSharingSessions.size());
        return videoSharingSessions.get(str);
    }

    public List<IBinder> getVideoSharings() throws ServerApiException {
        ArrayList arrayList = new ArrayList(videoSharingSessions.size());
        Enumeration<IVideoSharing> elements = videoSharingSessions.elements();
        while (elements.hasMoreElements()) {
            arrayList.add(elements.nextElement().asBinder());
        }
        return arrayList;
    }

    public void deleteVideoSharings() throws ServerApiException {
        Hashtable<String, IVideoSharing> hashtable = videoSharingSessions;
        if (hashtable != null && hashtable.size() != 0) {
            videoSharingSessions.clear();
        }
    }

    public void addEventListener(IVideoSharingListener iVideoSharingListener) throws ServerApiException {
        synchronized (this.lock) {
            this.eventListeners.register(iVideoSharingListener);
        }
    }

    public void removeEventListener(IVideoSharingListener iVideoSharingListener) throws ServerApiException {
        synchronized (this.lock) {
            this.eventListeners.unregister(iVideoSharingListener);
        }
    }

    public IVideoSharing shareVideo(ContactId contactId, IVideoPlayer iVideoPlayer) throws ServerApiException {
        if (contactId == null) {
            Log.e(LOG_TAG, "Cannot initiate a live video session, contact is null");
            return null;
        }
        String str = LOG_TAG;
        Log.i(str, "Initiate a live video session with contact " + contactId.toString());
        if (iVideoPlayer != null) {
            try {
                VideoShare videoShare = vshModule.createShare(ImsUri.parse(contactId.toString()), VshIntents.LIVE_VIDEO_CONTENTPATH).get();
                if (videoShare != null) {
                    VideoSharingImpl videoSharingImpl = new VideoSharingImpl(videoShare, iVideoPlayer);
                    addVideoSharingSession(String.valueOf(videoShare.getContent().shareId), videoSharingImpl);
                    return videoSharingImpl;
                }
                throw new ServerApiException("session is null");
            } catch (InterruptedException e) {
                e.printStackTrace();
                return null;
            } catch (ExecutionException e2) {
                e2.printStackTrace();
                return null;
            }
        } else {
            throw new ServerApiException("Missing video player");
        }
    }

    public void deleteVideoSharingsByContactId(ContactId contactId) throws RemoteException {
        if (contactId == null) {
            Log.e(LOG_TAG, "Cannot delete video sharing session, contact is null");
            return;
        }
        String str = LOG_TAG;
        Log.i(str, "delete video sharing session " + contactId.toString());
        Hashtable<String, IVideoSharing> hashtable = videoSharingSessions;
        if (hashtable != null && hashtable.size() != 0) {
            videoSharingSessions.remove(contactId.toString());
        }
    }

    public void handleVideoSessionDeleted(ContactId contactId, List<String> list) {
        String str = LOG_TAG;
        Log.d(str, "handleVideoSessionDeleted: contactid = " + contactId + " ,sharingIds = " + list);
        synchronized (this.lock) {
            int beginBroadcast = this.eventListeners.beginBroadcast();
            for (int i = 0; i < beginBroadcast; i++) {
                try {
                    this.eventListeners.getBroadcastItem(i).onDeleted(contactId, list);
                } catch (RemoteException e) {
                    Log.i(LOG_TAG, "Can't notify listener", e);
                }
            }
            this.eventListeners.finishBroadcast();
        }
    }
}
