package com.sec.internal.ims.cmstore.adapters;

import android.util.Log;
import com.sec.internal.ims.cmstore.MessageStoreClient;
import com.sec.internal.ims.cmstore.utils.RetryParam;
import com.sec.internal.interfaces.ims.cmstore.IAPICallFlowListener;
import com.sec.internal.interfaces.ims.cmstore.IHttpAPICommonInterface;
import java.util.HashMap;
import java.util.Map;

public class McsRetryMapAdapter {
    public String TAG;
    private final transient Map<String, RetryParam> mMap = new HashMap();
    private MessageStoreClient messageStoreClient;

    public McsRetryMapAdapter() {
        String simpleName = McsRetryMapAdapter.class.getSimpleName();
        this.TAG = simpleName;
        Log.i(simpleName, "McsRetryMapAdapter Constructor");
    }

    public synchronized void initRetryMapAdapter(MessageStoreClient messageStoreClient2) {
        this.messageStoreClient = messageStoreClient2;
        this.TAG += "[" + messageStoreClient2.getClientID() + "]";
    }

    public synchronized boolean checkAndIncreaseRetry(IHttpAPICommonInterface iHttpAPICommonInterface, int i) {
        if (iHttpAPICommonInterface != null) {
            if (this.mMap != null) {
                if (increaseRetryCount(iHttpAPICommonInterface, i)) {
                    return true;
                }
                Log.i(this.TAG, "checkAndIncreaseRetry, Already exhausted Max Counts");
                return false;
            }
        }
        Log.i(this.TAG, "checkAndIncreaseRetry, param or mMap is null");
        return false;
    }

    /* JADX WARNING: Code restructure failed: missing block: B:10:0x0032, code lost:
        return r1;
     */
    /* JADX WARNING: Code restructure failed: missing block: B:12:0x0034, code lost:
        return false;
     */
    /* Code decompiled incorrectly, please refer to instructions dump. */
    public synchronized boolean isAlreadyInRetry(java.lang.String r4) {
        /*
            r3 = this;
            monitor-enter(r3)
            java.lang.String r0 = r3.TAG     // Catch:{ all -> 0x0035 }
            java.lang.StringBuilder r1 = new java.lang.StringBuilder     // Catch:{ all -> 0x0035 }
            r1.<init>()     // Catch:{ all -> 0x0035 }
            java.lang.String r2 = " isAlreadyInRetry : "
            r1.append(r2)     // Catch:{ all -> 0x0035 }
            r1.append(r4)     // Catch:{ all -> 0x0035 }
            java.lang.String r1 = r1.toString()     // Catch:{ all -> 0x0035 }
            android.util.Log.i(r0, r1)     // Catch:{ all -> 0x0035 }
            java.util.Map<java.lang.String, com.sec.internal.ims.cmstore.utils.RetryParam> r0 = r3.mMap     // Catch:{ all -> 0x0035 }
            r1 = 0
            if (r0 == 0) goto L_0x0033
            boolean r0 = r0.containsKey(r4)     // Catch:{ all -> 0x0035 }
            if (r0 == 0) goto L_0x0033
            java.util.Map<java.lang.String, com.sec.internal.ims.cmstore.utils.RetryParam> r0 = r3.mMap     // Catch:{ all -> 0x0035 }
            java.lang.Object r4 = r0.get(r4)     // Catch:{ all -> 0x0035 }
            com.sec.internal.ims.cmstore.utils.RetryParam r4 = (com.sec.internal.ims.cmstore.utils.RetryParam) r4     // Catch:{ all -> 0x0035 }
            int r4 = r4.getRetryCount()     // Catch:{ all -> 0x0035 }
            if (r4 == 0) goto L_0x0031
            r1 = 1
        L_0x0031:
            monitor-exit(r3)
            return r1
        L_0x0033:
            monitor-exit(r3)
            return r1
        L_0x0035:
            r4 = move-exception
            monitor-exit(r3)
            throw r4
        */
        throw new UnsupportedOperationException("Method not decompiled: com.sec.internal.ims.cmstore.adapters.McsRetryMapAdapter.isAlreadyInRetry(java.lang.String):boolean");
    }

    /* JADX WARNING: Code restructure failed: missing block: B:17:0x0092, code lost:
        return true;
     */
    /* Code decompiled incorrectly, please refer to instructions dump. */
    public synchronized boolean increaseRetryCount(com.sec.internal.interfaces.ims.cmstore.IHttpAPICommonInterface r6, int r7) {
        /*
            r5 = this;
            monitor-enter(r5)
            java.lang.Class r0 = r6.getClass()     // Catch:{ all -> 0x0095 }
            java.lang.String r0 = r0.getSimpleName()     // Catch:{ all -> 0x0095 }
            java.util.Map<java.lang.String, com.sec.internal.ims.cmstore.utils.RetryParam> r1 = r5.mMap     // Catch:{ all -> 0x0095 }
            r2 = 0
            if (r1 == 0) goto L_0x0093
            boolean r1 = r1.containsKey(r0)     // Catch:{ all -> 0x0095 }
            r3 = 1
            if (r1 == 0) goto L_0x0061
            java.util.Map<java.lang.String, com.sec.internal.ims.cmstore.utils.RetryParam> r6 = r5.mMap     // Catch:{ all -> 0x0095 }
            java.lang.Object r6 = r6.get(r0)     // Catch:{ all -> 0x0095 }
            com.sec.internal.ims.cmstore.utils.RetryParam r6 = (com.sec.internal.ims.cmstore.utils.RetryParam) r6     // Catch:{ all -> 0x0095 }
            java.lang.String r7 = r5.TAG     // Catch:{ all -> 0x0095 }
            java.lang.StringBuilder r1 = new java.lang.StringBuilder     // Catch:{ all -> 0x0095 }
            r1.<init>()     // Catch:{ all -> 0x0095 }
            java.lang.String r4 = "increaseRetryCount, Already Exists in Map with retried count :"
            r1.append(r4)     // Catch:{ all -> 0x0095 }
            int r4 = r6.getRetryCount()     // Catch:{ all -> 0x0095 }
            r1.append(r4)     // Catch:{ all -> 0x0095 }
            java.lang.String r1 = r1.toString()     // Catch:{ all -> 0x0095 }
            android.util.Log.i(r7, r1)     // Catch:{ all -> 0x0095 }
            int r7 = r6.getRetryCount()     // Catch:{ all -> 0x0095 }
            r1 = 3
            if (r7 != r1) goto L_0x0058
            java.lang.Class<com.sec.internal.ims.cmstore.omanetapi.nc.McsCreateNotificationChannel> r7 = com.sec.internal.ims.cmstore.omanetapi.nc.McsCreateNotificationChannel.class
            java.lang.String r7 = r7.getSimpleName()     // Catch:{ all -> 0x0095 }
            boolean r7 = r0.equalsIgnoreCase(r7)     // Catch:{ all -> 0x0095 }
            if (r7 != 0) goto L_0x0058
            java.lang.String r6 = r5.TAG     // Catch:{ all -> 0x0095 }
            java.lang.String r7 = "increaseRetryCount, removed key retry limit reached "
            android.util.Log.i(r6, r7)     // Catch:{ all -> 0x0095 }
            java.util.Map<java.lang.String, com.sec.internal.ims.cmstore.utils.RetryParam> r6 = r5.mMap     // Catch:{ all -> 0x0095 }
            r6.remove(r0)     // Catch:{ all -> 0x0095 }
            monitor-exit(r5)
            return r2
        L_0x0058:
            int r7 = r6.getRetryCount()     // Catch:{ all -> 0x0095 }
            int r7 = r7 + r3
            r6.setRetryCount(r7)     // Catch:{ all -> 0x0095 }
            goto L_0x0091
        L_0x0061:
            java.lang.String r1 = r5.TAG     // Catch:{ all -> 0x0095 }
            java.lang.StringBuilder r2 = new java.lang.StringBuilder     // Catch:{ all -> 0x0095 }
            r2.<init>()     // Catch:{ all -> 0x0095 }
            java.lang.String r4 = "increaseRetryCount, New to Map, added entry for request and errorCode: "
            r2.append(r4)     // Catch:{ all -> 0x0095 }
            java.lang.Class r4 = r6.getClass()     // Catch:{ all -> 0x0095 }
            java.lang.String r4 = r4.getSimpleName()     // Catch:{ all -> 0x0095 }
            r2.append(r4)     // Catch:{ all -> 0x0095 }
            java.lang.String r4 = " "
            r2.append(r4)     // Catch:{ all -> 0x0095 }
            r2.append(r7)     // Catch:{ all -> 0x0095 }
            java.lang.String r2 = r2.toString()     // Catch:{ all -> 0x0095 }
            android.util.Log.i(r1, r2)     // Catch:{ all -> 0x0095 }
            com.sec.internal.ims.cmstore.utils.RetryParam r1 = new com.sec.internal.ims.cmstore.utils.RetryParam     // Catch:{ all -> 0x0095 }
            r1.<init>(r6, r3, r7)     // Catch:{ all -> 0x0095 }
            java.util.Map<java.lang.String, com.sec.internal.ims.cmstore.utils.RetryParam> r6 = r5.mMap     // Catch:{ all -> 0x0095 }
            r6.put(r0, r1)     // Catch:{ all -> 0x0095 }
        L_0x0091:
            monitor-exit(r5)
            return r3
        L_0x0093:
            monitor-exit(r5)
            return r2
        L_0x0095:
            r6 = move-exception
            monitor-exit(r5)
            throw r6
        */
        throw new UnsupportedOperationException("Method not decompiled: com.sec.internal.ims.cmstore.adapters.McsRetryMapAdapter.increaseRetryCount(com.sec.internal.interfaces.ims.cmstore.IHttpAPICommonInterface, int):boolean");
    }

    public synchronized void clearRetryHistory() {
        Log.i(this.TAG, "clearRetryCounter: retry history cleared");
        Map<String, RetryParam> map = this.mMap;
        if (map != null) {
            map.clear();
        }
    }

    /* JADX WARNING: Code restructure failed: missing block: B:11:0x001c, code lost:
        return;
     */
    /* Code decompiled incorrectly, please refer to instructions dump. */
    public synchronized void remove(com.sec.internal.interfaces.ims.cmstore.IHttpAPICommonInterface r2) {
        /*
            r1 = this;
            monitor-enter(r1)
            java.lang.Class r2 = r2.getClass()     // Catch:{ all -> 0x001d }
            java.lang.String r2 = r2.getSimpleName()     // Catch:{ all -> 0x001d }
            java.util.Map<java.lang.String, com.sec.internal.ims.cmstore.utils.RetryParam> r0 = r1.mMap     // Catch:{ all -> 0x001d }
            if (r0 == 0) goto L_0x001b
            int r0 = r0.size()     // Catch:{ all -> 0x001d }
            if (r0 != 0) goto L_0x0014
            goto L_0x001b
        L_0x0014:
            java.util.Map<java.lang.String, com.sec.internal.ims.cmstore.utils.RetryParam> r0 = r1.mMap     // Catch:{ all -> 0x001d }
            r0.remove(r2)     // Catch:{ all -> 0x001d }
            monitor-exit(r1)
            return
        L_0x001b:
            monitor-exit(r1)
            return
        L_0x001d:
            r2 = move-exception
            monitor-exit(r1)
            throw r2
        */
        throw new UnsupportedOperationException("Method not decompiled: com.sec.internal.ims.cmstore.adapters.McsRetryMapAdapter.remove(com.sec.internal.interfaces.ims.cmstore.IHttpAPICommonInterface):void");
    }

    public synchronized boolean retryApi(IHttpAPICommonInterface iHttpAPICommonInterface, IAPICallFlowListener iAPICallFlowListener) {
        String str = this.TAG;
        Log.i(str, "retryApi : " + iHttpAPICommonInterface.getClass().getSimpleName());
        Map<String, RetryParam> map = this.mMap;
        if (map == null || map.isEmpty() || iAPICallFlowListener == null || this.mMap.get(iHttpAPICommonInterface.getClass().getSimpleName()) == null) {
            iHttpAPICommonInterface = null;
        } else {
            String str2 = this.TAG;
            Log.i(str2, "Time Exhausted, Last Retry Val: " + iHttpAPICommonInterface.getClass().getSimpleName());
            this.messageStoreClient.getHttpController().execute(iHttpAPICommonInterface.getRetryInstance(iAPICallFlowListener, this.messageStoreClient));
        }
        if (iHttpAPICommonInterface == null) {
            return false;
        }
        String str3 = this.TAG;
        Log.i(str3, "retryLastApi: " + iHttpAPICommonInterface.getClass().getSimpleName());
        return true;
    }

    /* JADX WARNING: Code restructure failed: missing block: B:10:0x001a, code lost:
        return 0;
     */
    /* Code decompiled incorrectly, please refer to instructions dump. */
    public synchronized int getRetryCount(java.lang.String r2) {
        /*
            r1 = this;
            monitor-enter(r1)
            java.util.Map<java.lang.String, com.sec.internal.ims.cmstore.utils.RetryParam> r0 = r1.mMap     // Catch:{ all -> 0x001c }
            if (r0 == 0) goto L_0x0019
            boolean r0 = r0.containsKey(r2)     // Catch:{ all -> 0x001c }
            if (r0 == 0) goto L_0x0019
            java.util.Map<java.lang.String, com.sec.internal.ims.cmstore.utils.RetryParam> r0 = r1.mMap     // Catch:{ all -> 0x001c }
            java.lang.Object r2 = r0.get(r2)     // Catch:{ all -> 0x001c }
            com.sec.internal.ims.cmstore.utils.RetryParam r2 = (com.sec.internal.ims.cmstore.utils.RetryParam) r2     // Catch:{ all -> 0x001c }
            int r2 = r2.getRetryCount()     // Catch:{ all -> 0x001c }
            monitor-exit(r1)
            return r2
        L_0x0019:
            monitor-exit(r1)
            r1 = 0
            return r1
        L_0x001c:
            r2 = move-exception
            monitor-exit(r1)
            throw r2
        */
        throw new UnsupportedOperationException("Method not decompiled: com.sec.internal.ims.cmstore.adapters.McsRetryMapAdapter.getRetryCount(java.lang.String):int");
    }

    public synchronized RetryParam getRetryParam(String str) {
        Map<String, RetryParam> map;
        map = this.mMap;
        return (map == null || !map.containsKey(str)) ? null : this.mMap.get(str);
    }
}
