package com.sec.internal.ims.servicemodules.presence;

import android.util.Log;
import com.sec.ims.util.ImsUri;
import com.sec.internal.constants.ims.servicemodules.options.CapabilityConstants;
import com.sec.internal.constants.ims.servicemodules.presence.PresenceSubscription;
import com.sec.internal.log.IMSLog;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.Queue;

public class PresenceSubscriptionController {
    private static final String LOG_TAG = "PresenceSubscriptionController";
    private static Queue<ImsUri> mLazySubscriptionQueue = new LinkedList();
    private static List<PresenceSubscription> mPendingSubscriptionList = new ArrayList();
    private static List<PresenceSubscription> mSubscriptionList = new ArrayList();

    PresenceSubscriptionController() {
    }

    public static class SubscriptionRequest {
        public int internalRequestId;
        public boolean isAlwaysForce;
        public int phoneId;
        public CapabilityConstants.RequestType type;
        public ImsUri uri;

        public SubscriptionRequest(ImsUri imsUri, CapabilityConstants.RequestType requestType, boolean z, int i, int i2) {
            this.uri = imsUri;
            this.type = requestType;
            this.isAlwaysForce = z;
            this.phoneId = i;
            this.internalRequestId = i2;
        }
    }

    static void addSubscription(PresenceSubscription presenceSubscription) {
        mSubscriptionList.add(presenceSubscription);
    }

    static void addLazySubscription(ImsUri imsUri) {
        synchronized (mLazySubscriptionQueue) {
            mLazySubscriptionQueue.add(imsUri);
        }
    }

    static void addPendingSubscription(PresenceSubscription presenceSubscription) {
        mPendingSubscriptionList.add(presenceSubscription);
    }

    static List<PresenceSubscription> getPendingSubscription() {
        return mPendingSubscriptionList;
    }

    static void clearPendingSubscription() {
        mPendingSubscriptionList.clear();
    }

    static PresenceSubscription getSubscription(ImsUri imsUri, boolean z, int i) {
        if (imsUri == null) {
            IMSLog.e(LOG_TAG, i, "getSubscription: uri is null");
            return null;
        }
        for (PresenceSubscription next : mSubscriptionList) {
            if (next.isSingleFetch() == z && next.getPhoneId() == i) {
                if ((z || !next.isExpired()) && next.contains(imsUri)) {
                    return next;
                }
            }
        }
        return null;
    }

    static PresenceSubscription getSubscription(String str, int i) {
        if (str == null) {
            Log.e(LOG_TAG, "getSubscription: subscriptionId is null");
            return null;
        }
        for (PresenceSubscription next : mSubscriptionList) {
            if (next.getSubscriptionId().equals(str) && next.getPhoneId() == i) {
                return next;
            }
        }
        return null;
    }

    static boolean hasSubscription(ImsUri imsUri) {
        for (PresenceSubscription next : mSubscriptionList) {
            if (!next.isExpired() && next.contains(imsUri)) {
                return true;
            }
        }
        return false;
    }

    static void cleanExpiredSubscription() {
        Iterator<PresenceSubscription> it = mSubscriptionList.iterator();
        while (it.hasNext()) {
            PresenceSubscription next = it.next();
            if (!next.isSingleFetch() && next.getState() == 2) {
                IMSLog.s(LOG_TAG, "cleanExpiredSubscription(): expired uri " + next.getUriList() + " (" + next.getTimestamp() + ")");
                PresenceUtil.removeSubscribeResponseCallback(next.getSubscriptionId());
                it.remove();
            }
        }
    }

    static void removeSubscription(List<ImsUri> list) {
        if (list == null || list.size() == 0) {
            Log.e(LOG_TAG, "removeSubscription: uris null");
        } else if (list.size() > 10) {
            Log.e(LOG_TAG, "removeSubscription: uris size is over " + list.size());
        } else {
            Iterator<PresenceSubscription> it = mSubscriptionList.iterator();
            while (it.hasNext()) {
                PresenceSubscription next = it.next();
                if (next.isSingleFetch()) {
                    Iterator<ImsUri> it2 = list.iterator();
                    while (true) {
                        if (it2.hasNext()) {
                            if (next.contains(it2.next())) {
                                it.remove();
                                break;
                            }
                        } else {
                            break;
                        }
                    }
                } else {
                    for (ImsUri next2 : list) {
                        if (next.contains(next2)) {
                            next.remove(next2);
                        }
                    }
                }
            }
        }
    }

    /* JADX WARNING: Code restructure failed: missing block: B:16:0x0034, code lost:
        return false;
     */
    /* Code decompiled incorrectly, please refer to instructions dump. */
    static boolean checkLazySubscription(com.sec.ims.util.ImsUri r2, boolean r3) {
        /*
            java.util.Queue<com.sec.ims.util.ImsUri> r0 = mLazySubscriptionQueue
            monitor-enter(r0)
            java.util.Queue<com.sec.ims.util.ImsUri> r1 = mLazySubscriptionQueue     // Catch:{ all -> 0x0036 }
            boolean r1 = r1.isEmpty()     // Catch:{ all -> 0x0036 }
            if (r1 != 0) goto L_0x0026
            java.util.Queue<com.sec.ims.util.ImsUri> r1 = mLazySubscriptionQueue     // Catch:{ all -> 0x0036 }
            java.lang.Object r1 = r1.peek()     // Catch:{ all -> 0x0036 }
            if (r1 == 0) goto L_0x0026
            java.util.Queue<com.sec.ims.util.ImsUri> r1 = mLazySubscriptionQueue     // Catch:{ all -> 0x0036 }
            java.lang.Object r1 = r1.peek()     // Catch:{ all -> 0x0036 }
            com.sec.ims.util.ImsUri r1 = (com.sec.ims.util.ImsUri) r1     // Catch:{ all -> 0x0036 }
            boolean r2 = r1.equals(r2)     // Catch:{ all -> 0x0036 }
            if (r2 != 0) goto L_0x0026
            if (r3 != 0) goto L_0x0026
            monitor-exit(r0)     // Catch:{ all -> 0x0036 }
            r2 = 1
            return r2
        L_0x0026:
            java.util.Queue<com.sec.ims.util.ImsUri> r2 = mLazySubscriptionQueue     // Catch:{ all -> 0x0036 }
            boolean r2 = r2.isEmpty()     // Catch:{ all -> 0x0036 }
            if (r2 != 0) goto L_0x0033
            java.util.Queue<com.sec.ims.util.ImsUri> r2 = mLazySubscriptionQueue     // Catch:{ all -> 0x0036 }
            r2.remove()     // Catch:{ all -> 0x0036 }
        L_0x0033:
            monitor-exit(r0)     // Catch:{ all -> 0x0036 }
            r2 = 0
            return r2
        L_0x0036:
            r2 = move-exception
            monitor-exit(r0)     // Catch:{ all -> 0x0036 }
            throw r2
        */
        throw new UnsupportedOperationException("Method not decompiled: com.sec.internal.ims.servicemodules.presence.PresenceSubscriptionController.checkLazySubscription(com.sec.ims.util.ImsUri, boolean):boolean");
    }
}
