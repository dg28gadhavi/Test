package com.sec.internal.ims.servicemodules.tapi.service.api;

import android.os.IBinder;
import android.os.RemoteCallbackList;
import android.os.RemoteException;
import android.util.Log;
import com.gsma.services.rcs.IRcsServiceRegistrationListener;
import com.gsma.services.rcs.RcsServiceRegistration;
import com.gsma.services.rcs.contact.ContactId;
import com.gsma.services.rcs.sharing.image.IImageSharing;
import com.gsma.services.rcs.sharing.image.IImageSharingListener;
import com.gsma.services.rcs.sharing.image.IImageSharingService;
import com.gsma.services.rcs.sharing.image.ImageSharingServiceConfiguration;
import com.sec.ims.util.ImsUri;
import com.sec.internal.ims.registry.ImsRegistry;
import com.sec.internal.ims.servicemodules.csh.IImageShareEventListener;
import com.sec.internal.ims.servicemodules.csh.ImageShare;
import com.sec.internal.ims.servicemodules.tapi.service.broadcaster.IRegistrationStatusBroadcaster;
import com.sec.internal.interfaces.ims.core.IRegistrationManager;
import com.sec.internal.interfaces.ims.servicemodules.csh.IImageShareModule;
import java.util.ArrayList;
import java.util.Enumeration;
import java.util.Hashtable;
import java.util.Iterator;
import java.util.List;
import java.util.concurrent.ExecutionException;

public class ImageSharingServiceImpl extends IImageSharingService.Stub implements IRegistrationStatusBroadcaster, IImageShareEventListener {
    private static final String LOG_TAG = ImageSharingServiceImpl.class.getSimpleName();
    private IImageShareModule ishModule;
    private RemoteCallbackList<IImageSharingListener> mImageSharingListeners = new RemoteCallbackList<>();
    private Object mIshListenerLock = new Object();
    private Hashtable<String, IImageSharing> mIshSessionsMap = new Hashtable<>();
    private Object mServiceListenerlock = new Object();
    private RemoteCallbackList<IRcsServiceRegistrationListener> mServiceListeners = new RemoteCallbackList<>();

    public int getServiceVersion() throws RemoteException {
        return 0;
    }

    public ImageSharingServiceImpl(IImageShareModule iImageShareModule) {
        this.ishModule = iImageShareModule;
        iImageShareModule.registerImageShareEventListener(this);
    }

    /* access modifiers changed from: package-private */
    public void addImageSharingSession(String str, ImageSharingImpl imageSharingImpl) {
        this.mIshSessionsMap.put(str, imageSharingImpl);
    }

    public void notifyRegistrationEvent(boolean z, RcsServiceRegistration.ReasonCode reasonCode) {
        synchronized (this.mServiceListenerlock) {
            int beginBroadcast = this.mServiceListeners.beginBroadcast();
            for (int i = 0; i < beginBroadcast; i++) {
                if (z) {
                    try {
                        this.mServiceListeners.getBroadcastItem(i).onServiceRegistered();
                    } catch (Exception unused) {
                    }
                } else {
                    this.mServiceListeners.getBroadcastItem(i).onServiceUnregistered(reasonCode);
                }
            }
            this.mServiceListeners.finishBroadcast();
        }
    }

    public void notifyImageSharingProgress(String str, long j) {
        synchronized (this.mIshListenerLock) {
            ImageSharingImpl imageSharingImpl = this.mIshSessionsMap.get(str);
            if (imageSharingImpl == null) {
                Log.d(LOG_TAG, "notifyImageSharingProgress(): session is null");
                return;
            }
            ContactId remoteContact = imageSharingImpl.getRemoteContact();
            String sharingId = imageSharingImpl.getSharingId();
            long fileSize = imageSharingImpl.getFileSize();
            int beginBroadcast = this.mImageSharingListeners.beginBroadcast();
            for (int i = 0; i < beginBroadcast; i++) {
                try {
                    this.mImageSharingListeners.getBroadcastItem(i).onProgressUpdate(remoteContact, sharingId, j, fileSize);
                } catch (RemoteException e) {
                    e.printStackTrace();
                }
            }
            this.mImageSharingListeners.finishBroadcast();
        }
    }

    public boolean isServiceRegistered() {
        IRegistrationManager registrationManager = ImsRegistry.getRegistrationManager();
        if (registrationManager == null) {
            return false;
        }
        boolean hasService = registrationManager.getRegistrationInfo()[0].hasService("is");
        String str = LOG_TAG;
        Log.d(str, "isServiceRegistered() = " + hasService);
        return hasService;
    }

    public void addServiceRegistrationListener(IRcsServiceRegistrationListener iRcsServiceRegistrationListener) {
        synchronized (this.mServiceListenerlock) {
            this.mServiceListeners.register(iRcsServiceRegistrationListener);
        }
    }

    public void removeServiceRegistrationListener(IRcsServiceRegistrationListener iRcsServiceRegistrationListener) {
        synchronized (this.mServiceListenerlock) {
            this.mServiceListeners.unregister(iRcsServiceRegistrationListener);
        }
    }

    public ImageSharingServiceConfiguration getConfiguration() {
        return new ImageSharingServiceConfiguration(this.ishModule.getMaxSize(), this.ishModule.getWarnSize());
    }

    public List<IBinder> getImageSharings() throws RemoteException {
        try {
            ArrayList arrayList = new ArrayList(this.mIshSessionsMap.size());
            Enumeration<IImageSharing> elements = this.mIshSessionsMap.elements();
            while (elements.hasMoreElements()) {
                arrayList.add(elements.nextElement().asBinder());
            }
            return arrayList;
        } catch (Exception e) {
            throw new ServerApiException(e.getMessage());
        }
    }

    public IImageSharing getImageSharing(String str) throws RemoteException {
        return this.mIshSessionsMap.get(str);
    }

    public IImageSharing shareImage(ContactId contactId, String str) throws RemoteException {
        try {
            ImageShare imageShare = this.ishModule.createShare(ImsUri.parse("tel:" + contactId), str).get();
            if (imageShare != null) {
                ImageSharingImpl imageSharingImpl = new ImageSharingImpl(imageShare);
                String str2 = LOG_TAG;
                Log.d(str2, "shareImage: sharingId = " + imageSharingImpl.getSharingId());
                addImageSharingSession(imageSharingImpl.getSharingId(), imageSharingImpl);
                return imageSharingImpl;
            }
            throw new RemoteException("session is null");
        } catch (InterruptedException e) {
            e.printStackTrace();
            return null;
        } catch (ExecutionException e2) {
            e2.printStackTrace();
            return null;
        }
    }

    public void addEventListener(IImageSharingListener iImageSharingListener) throws RemoteException {
        synchronized (this.mIshListenerLock) {
            this.mImageSharingListeners.register(iImageSharingListener);
        }
    }

    public void removeEventListener(IImageSharingListener iImageSharingListener) throws RemoteException {
        synchronized (this.mIshListenerLock) {
            this.mImageSharingListeners.unregister(iImageSharingListener);
        }
    }

    public void deleteAllImageSharings() throws RemoteException {
        try {
            Enumeration<IImageSharing> elements = this.mIshSessionsMap.elements();
            while (elements.hasMoreElements()) {
                elements.nextElement().abortSharing();
            }
            this.mIshSessionsMap.clear();
        } catch (Exception e) {
            throw new ServerApiException(e.getMessage());
        }
    }

    public void deleteImageSharings(ContactId contactId) throws RemoteException {
        Iterator it = (Iterator) this.mIshSessionsMap.keySet();
        while (it.hasNext()) {
            String str = (String) it.next();
            IImageSharing iImageSharing = this.mIshSessionsMap.get(str);
            if (contactId.equals(iImageSharing.getRemoteContact())) {
                iImageSharing.abortSharing();
                this.mIshSessionsMap.remove(str);
            }
        }
    }

    public void deleteImageSharing(String str) throws RemoteException {
        IImageSharing iImageSharing = this.mIshSessionsMap.get(str);
        if (iImageSharing != null) {
            iImageSharing.abortSharing();
            this.mIshSessionsMap.remove(str);
        }
    }

    public void onIshTransferProgressEvent(String str, long j) {
        notifyImageSharingProgress(str, j);
    }
}
