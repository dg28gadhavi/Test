package com.sec.internal.ims.servicemodules.tapi.service.api;

import android.content.Context;
import android.content.Intent;
import android.database.ContentObserver;
import android.net.Uri;
import android.os.Binder;
import android.os.RemoteCallbackList;
import android.os.RemoteException;
import android.util.Log;
import com.gsma.services.rcs.IRcsServiceRegistrationListener;
import com.gsma.services.rcs.RcsServiceRegistration;
import com.gsma.services.rcs.capability.CapabilitiesLog;
import com.gsma.services.rcs.capability.ICapabilitiesListener;
import com.gsma.services.rcs.capability.ICapabilityService;
import com.gsma.services.rcs.contact.ContactId;
import com.sec.ims.ImsRegistration;
import com.sec.ims.options.Capabilities;
import com.sec.ims.options.CapabilityRefreshType;
import com.sec.ims.options.ICapabilityServiceEventListener;
import com.sec.ims.util.ImsUri;
import com.sec.internal.constants.ims.SipMsg;
import com.sec.internal.helper.SimUtil;
import com.sec.internal.ims.registry.ImsRegistry;
import com.sec.internal.ims.servicemodules.options.CapabilityDiscoveryService;
import com.sec.internal.ims.util.PhoneUtils;
import com.sec.internal.interfaces.ims.core.IRegistrationManager;
import com.sec.internal.log.IMSLog;
import java.util.HashMap;
import java.util.Hashtable;
import java.util.List;
import java.util.Map;

public class CapabilityServiceImpl extends ICapabilityService.Stub {
    private static final String LOG_TAG = CapabilityServiceImpl.class.getSimpleName();
    private static final String SERVICE_ID_CALL_COMPOSER = "gsma.callcomposer";
    private static final String SERVICE_ID_POST_CALL = "gsma.callunanswered";
    private static final String SERVICE_ID_SHARED_MAP = "gsma.sharedmap";
    private static final String SERVICE_ID_SHARED_SKETCH = "gsma.sharedsketch";
    private RemoteCallbackList<ICapabilitiesListener> mCapabilitiesListeners = new RemoteCallbackList<>();
    private CapabilityDiscoveryService mCapabilityDiscoveryService = null;
    private Hashtable<String, RemoteCallbackList<ICapabilitiesListener>> mContactCapalitiesListeners = new Hashtable<>();
    Context mContext;
    private Object mLock = new Object();
    private RemoteCallbackList<IRcsServiceRegistrationListener> mServiceListeners = new RemoteCallbackList<>();
    private ICapabilityServiceEventListener.Stub serviceEventListener = null;

    public int getServiceVersion() throws ServerApiException {
        return 2;
    }

    public CapabilityServiceImpl(Context context) {
        this.mContext = context;
        this.serviceEventListener = new ICapabilityServiceEventListener.Stub() {
            public void onCapabilityAndAvailabilityPublished(int i) throws RemoteException {
            }

            public void onMultipleCapabilitiesChanged(List<ImsUri> list, List<Capabilities> list2) throws RemoteException {
            }

            public void onOwnCapabilitiesChanged() throws RemoteException {
                CapabilityServiceImpl.this.notifyOwnCapabilityChange();
            }

            public void onCapabilitiesChanged(List<ImsUri> list, Capabilities capabilities) throws RemoteException {
                for (ImsUri imsUri : list) {
                    CapabilityServiceImpl.this.receiveCapabilities(imsUri.toString(), capabilities);
                }
            }
        };
        this.mCapabilityDiscoveryService = (CapabilityDiscoveryService) ImsRegistry.getBinder("options", (String) null);
        try {
            int phoneCount = SimUtil.getPhoneCount();
            for (int i = 0; i < phoneCount; i++) {
                this.mCapabilityDiscoveryService.registerListener(this.serviceEventListener, i);
            }
        } catch (RemoteException e) {
            e.printStackTrace();
        }
    }

    public boolean isServiceRegistered() throws ServerApiException {
        IRegistrationManager registrationManager = ImsRegistry.getRegistrationManager();
        if (registrationManager == null) {
            return false;
        }
        for (ImsRegistration imsRegistration : registrationManager.getRegistrationInfo()) {
            if (imsRegistration.hasService("options") || imsRegistration.hasService(SipMsg.EVENT_PRESENCE)) {
                return true;
            }
        }
        return false;
    }

    public void addEventListener(IRcsServiceRegistrationListener iRcsServiceRegistrationListener) throws ServerApiException {
        this.mServiceListeners.register(iRcsServiceRegistrationListener);
    }

    public void removeEventListener(IRcsServiceRegistrationListener iRcsServiceRegistrationListener) throws ServerApiException {
        this.mServiceListeners.unregister(iRcsServiceRegistrationListener);
    }

    public void notifyRegistrationEvent(boolean z, RcsServiceRegistration.ReasonCode reasonCode) {
        Log.d(LOG_TAG, "start : notifyRegistrationEvent()");
        synchronized (this.mLock) {
            int beginBroadcast = this.mServiceListeners.beginBroadcast();
            for (int i = 0; i < beginBroadcast; i++) {
                if (z) {
                    try {
                        this.mServiceListeners.getBroadcastItem(i).onServiceRegistered();
                    } catch (Exception e) {
                        String str = LOG_TAG;
                        Log.d(str, "Can't notify listener : " + e.getMessage());
                    }
                } else {
                    this.mServiceListeners.getBroadcastItem(i).onServiceUnregistered(reasonCode);
                }
            }
            this.mServiceListeners.finishBroadcast();
        }
    }

    public com.gsma.services.rcs.capability.Capabilities getMyCapabilities() throws ServerApiException {
        long clearCallingIdentity = Binder.clearCallingIdentity();
        com.gsma.services.rcs.capability.Capabilities capabilities = null;
        try {
            Capabilities ownCapabilities = this.mCapabilityDiscoveryService.getOwnCapabilities(SimUtil.getActiveDataPhoneId());
            if (ownCapabilities != null) {
                capabilities = transferCapabilities(ownCapabilities);
            }
            Binder.restoreCallingIdentity(clearCallingIdentity);
            String str = LOG_TAG;
            Log.d(str, "getMyCapabilities: " + capabilities);
            return capabilities;
        } catch (RemoteException e) {
            e.printStackTrace();
            Binder.restoreCallingIdentity(clearCallingIdentity);
            return null;
        } catch (Throwable th) {
            Binder.restoreCallingIdentity(clearCallingIdentity);
            throw th;
        }
    }

    public com.gsma.services.rcs.capability.Capabilities getContactCapabilities(ContactId contactId) throws ServerApiException {
        com.gsma.services.rcs.capability.Capabilities capabilities = null;
        try {
            Capabilities capabilities2 = this.mCapabilityDiscoveryService.getCapabilities(ImsUri.parse("tel:" + PhoneUtils.extractNumberFromUri(contactId.toString())), CapabilityRefreshType.DISABLED.ordinal(), SimUtil.getActiveDataPhoneId());
            if (capabilities2 != null) {
                capabilities = transferCapabilities(capabilities2);
            }
            String str = LOG_TAG;
            Log.d(str, "getContactCapabilities: contact = " + contactId + ", ret = " + capabilities);
            return capabilities;
        } catch (RemoteException e) {
            e.printStackTrace();
            return null;
        }
    }

    public Map<String, com.gsma.services.rcs.capability.Capabilities> getAllContactCapabilities() {
        Log.d(LOG_TAG, "start : getAllContactCapabilities()");
        HashMap hashMap = null;
        try {
            Capabilities[] allCapabilities = this.mCapabilityDiscoveryService.getAllCapabilities(SimUtil.getActiveDataPhoneId());
            if (allCapabilities == null) {
                return null;
            }
            HashMap hashMap2 = new HashMap();
            try {
                for (Capabilities capabilities : allCapabilities) {
                    hashMap2.put(capabilities.getNumber(), transferCapabilities(capabilities));
                }
                return hashMap2;
            } catch (RemoteException e) {
                e = e;
                hashMap = hashMap2;
                e.printStackTrace();
                return hashMap;
            }
        } catch (RemoteException e2) {
            e = e2;
            e.printStackTrace();
            return hashMap;
        }
    }

    public void requestContactCapabilities(ContactId contactId) throws ServerApiException {
        Log.d(LOG_TAG, "start : requestContactCapabilities(String contact)");
        try {
            this.mCapabilityDiscoveryService.getCapabilities(ImsUri.parse("tel:" + PhoneUtils.extractNumberFromUri(contactId.toString())), CapabilityRefreshType.ALWAYS_FORCE_REFRESH.ordinal(), SimUtil.getActiveDataPhoneId());
        } catch (RemoteException e) {
            throw new ServerApiException(e.getMessage());
        }
    }

    public void receiveCapabilities(String str, Capabilities capabilities) {
        synchronized (this.mLock) {
            String str2 = LOG_TAG;
            IMSLog.s(str2, "receiveCapabilities() contact = " + str + " capabilities = " + capabilities);
            com.gsma.services.rcs.capability.Capabilities transferCapabilities = transferCapabilities(capabilities);
            String extractNumberFromUri = PhoneUtils.extractNumberFromUri(str);
            notifyListeners(extractNumberFromUri, transferCapabilities, this.mCapabilitiesListeners);
            RemoteCallbackList remoteCallbackList = this.mContactCapalitiesListeners.get(extractNumberFromUri);
            if (remoteCallbackList != null) {
                notifyListeners(extractNumberFromUri, transferCapabilities, remoteCallbackList);
            }
        }
    }

    private void notifyListeners(String str, com.gsma.services.rcs.capability.Capabilities capabilities, RemoteCallbackList<ICapabilitiesListener> remoteCallbackList) {
        String str2 = LOG_TAG;
        IMSLog.s(str2, "start : notifyListeners() contact = " + str + " capabilities = " + capabilities);
        ContactId contactId = new ContactId(str);
        try {
            int beginBroadcast = remoteCallbackList.beginBroadcast();
            for (int i = 0; i < beginBroadcast; i++) {
                remoteCallbackList.getBroadcastItem(i).onCapabilitiesReceived(contactId, capabilities);
            }
        } catch (RemoteException e) {
            e.printStackTrace();
        } catch (NullPointerException e2) {
            e2.printStackTrace();
        } catch (IllegalStateException e3) {
            e3.printStackTrace();
        }
        try {
            remoteCallbackList.finishBroadcast();
        } catch (NullPointerException e4) {
            e4.printStackTrace();
        } catch (IllegalStateException e5) {
            e5.printStackTrace();
        }
    }

    public void requestAllContactsCapabilities() throws ServerApiException {
        Log.i(LOG_TAG, "start : requestAllContactsCapabilities()");
        this.mContext.sendBroadcast(new Intent("com.sec.internal.ims.servicemodules.options.poll_timeout"));
    }

    public void addCapabilitiesListener(ICapabilitiesListener iCapabilitiesListener) throws ServerApiException {
        synchronized (this.mLock) {
            this.mCapabilitiesListeners.register(iCapabilitiesListener);
        }
    }

    public void removeCapabilitiesListener(ICapabilitiesListener iCapabilitiesListener) throws ServerApiException {
        synchronized (this.mLock) {
            this.mCapabilitiesListeners.unregister(iCapabilitiesListener);
        }
    }

    public void addContactCapabilitiesListener(ContactId contactId, ICapabilitiesListener iCapabilitiesListener) throws ServerApiException {
        synchronized (this.mLock) {
            Log.i(LOG_TAG, "start : addContactCapabilitiesListener()");
            String extractNumberFromUri = PhoneUtils.extractNumberFromUri(contactId.toString());
            RemoteCallbackList remoteCallbackList = this.mContactCapalitiesListeners.get(extractNumberFromUri);
            if (remoteCallbackList == null) {
                remoteCallbackList = new RemoteCallbackList();
                this.mContactCapalitiesListeners.put(extractNumberFromUri, remoteCallbackList);
            }
            remoteCallbackList.register(iCapabilitiesListener);
        }
    }

    public void removeContactCapabilitiesListener(ContactId contactId, ICapabilitiesListener iCapabilitiesListener) throws ServerApiException {
        synchronized (this.mLock) {
            Log.i(LOG_TAG, "start : removeContactCapabilitiesListener()");
            RemoteCallbackList remoteCallbackList = this.mContactCapalitiesListeners.get(PhoneUtils.extractNumberFromUri(contactId.toString()));
            if (remoteCallbackList != null) {
                remoteCallbackList.unregister(iCapabilitiesListener);
            }
        }
    }

    /* JADX WARNING: Code restructure failed: missing block: B:17:0x0072, code lost:
        if ((((long) r2) & r0) == ((long) r2)) goto L_0x0074;
     */
    /* Code decompiled incorrectly, please refer to instructions dump. */
    public static com.gsma.services.rcs.capability.Capabilities transferCapabilities(com.sec.ims.options.Capabilities r13) {
        /*
            long r0 = r13.getFeature()
            java.util.List r2 = r13.getExtFeature()
            boolean r3 = r13.isAvailable()
            if (r3 == 0) goto L_0x0014
            java.util.HashSet r4 = new java.util.HashSet
            r4.<init>(r2)
            goto L_0x0019
        L_0x0014:
            java.util.HashSet r4 = new java.util.HashSet
            r4.<init>()
        L_0x0019:
            r7 = r4
            java.util.Date r2 = r13.getTimestamp()
            if (r2 == 0) goto L_0x0029
            java.util.Date r2 = r13.getTimestamp()
            long r4 = r2.getTime()
            goto L_0x002b
        L_0x0029:
            r4 = 0
        L_0x002b:
            r9 = r4
            java.lang.String r2 = LOG_TAG
            java.lang.StringBuilder r4 = new java.lang.StringBuilder
            r4.<init>()
            java.lang.String r5 = "transferCapabilities, bValid : "
            r4.append(r5)
            r4.append(r3)
            java.lang.String r3 = ", bAutomata:"
            r4.append(r3)
            r3 = 0
            r4.append(r3)
            java.lang.String r4 = r4.toString()
            android.util.Log.d(r2, r4)
            int r2 = com.sec.ims.options.Capabilities.FEATURE_ISH
            long r4 = (long) r2
            long r4 = r4 & r0
            long r11 = (long) r2
            int r2 = (r4 > r11 ? 1 : (r4 == r11 ? 0 : -1))
            if (r2 != 0) goto L_0x0057
            r3 = 8
        L_0x0057:
            int r2 = com.sec.ims.options.Capabilities.FEATURE_VSH
            long r4 = (long) r2
            long r4 = r4 & r0
            long r11 = (long) r2
            int r2 = (r4 > r11 ? 1 : (r4 == r11 ? 0 : -1))
            if (r2 != 0) goto L_0x0062
            r3 = r3 | 16
        L_0x0062:
            int r2 = com.sec.ims.options.Capabilities.FEATURE_CHAT_CPM
            long r4 = (long) r2
            long r4 = r4 & r0
            long r11 = (long) r2
            int r2 = (r4 > r11 ? 1 : (r4 == r11 ? 0 : -1))
            if (r2 == 0) goto L_0x0074
            int r2 = com.sec.ims.options.Capabilities.FEATURE_CHAT_SIMPLE_IM
            long r4 = (long) r2
            long r4 = r4 & r0
            long r11 = (long) r2
            int r2 = (r4 > r11 ? 1 : (r4 == r11 ? 0 : -1))
            if (r2 != 0) goto L_0x0076
        L_0x0074:
            r3 = r3 | 2
        L_0x0076:
            int r2 = com.sec.ims.options.Capabilities.FEATURE_FT
            long r4 = (long) r2
            long r4 = r4 & r0
            long r11 = (long) r2
            int r2 = (r4 > r11 ? 1 : (r4 == r11 ? 0 : -1))
            if (r2 != 0) goto L_0x0081
            r3 = r3 | 1
        L_0x0081:
            int r2 = com.sec.ims.options.Capabilities.FEATURE_GEOLOCATION_PUSH
            long r4 = (long) r2
            long r0 = r0 & r4
            long r4 = (long) r2
            int r0 = (r0 > r4 ? 1 : (r0 == r4 ? 0 : -1))
            if (r0 != 0) goto L_0x008e
            r0 = r3 | 4
            r6 = r0
            goto L_0x008f
        L_0x008e:
            r6 = r3
        L_0x008f:
            long r0 = com.sec.ims.options.Capabilities.FEATURE_ENRICHED_CALL_COMPOSER
            boolean r0 = r13.hasFeature(r0)
            if (r0 == 0) goto L_0x009c
            java.lang.String r0 = "gsma.callcomposer"
            r7.add(r0)
        L_0x009c:
            long r0 = com.sec.ims.options.Capabilities.FEATURE_ENRICHED_SHARED_MAP
            boolean r0 = r13.hasFeature(r0)
            if (r0 == 0) goto L_0x00a9
            java.lang.String r0 = "gsma.sharedmap"
            r7.add(r0)
        L_0x00a9:
            long r0 = com.sec.ims.options.Capabilities.FEATURE_ENRICHED_SHARED_SKETCH
            boolean r0 = r13.hasFeature(r0)
            if (r0 == 0) goto L_0x00b6
            java.lang.String r0 = "gsma.sharedsketch"
            r7.add(r0)
        L_0x00b6:
            long r0 = com.sec.ims.options.Capabilities.FEATURE_ENRICHED_POST_CALL
            boolean r0 = r13.hasFeature(r0)
            if (r0 == 0) goto L_0x00c3
            java.lang.String r0 = "gsma.callunanswered"
            r7.add(r0)
        L_0x00c3:
            com.gsma.services.rcs.capability.Capabilities r0 = new com.gsma.services.rcs.capability.Capabilities
            long r11 = r13.getLastSeen()
            r8 = 0
            r5 = r0
            r5.<init>(r6, r7, r8, r9, r11)
            return r0
        */
        throw new UnsupportedOperationException("Method not decompiled: com.sec.internal.ims.servicemodules.tapi.service.api.CapabilityServiceImpl.transferCapabilities(com.sec.ims.options.Capabilities):com.gsma.services.rcs.capability.Capabilities");
    }

    /* access modifiers changed from: private */
    public void notifyOwnCapabilityChange() {
        Log.d(LOG_TAG, "notifyOwnCapabilityChange");
        this.mContext.getContentResolver().notifyChange(Uri.withAppendedPath(CapabilitiesLog.CONTENT_URI, "own"), (ContentObserver) null);
    }

    public void setUserActive(boolean z) {
        try {
            this.mCapabilityDiscoveryService.setUserActivity(z, SimUtil.getActiveDataPhoneId());
        } catch (RemoteException e) {
            e.printStackTrace();
        }
    }
}
