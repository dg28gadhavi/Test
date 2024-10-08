package com.sec.internal.ims.cmstore.adapters;

import android.util.Log;
import android.util.Pair;
import com.sec.internal.constants.ims.cmstore.omanetapi.SyncMsgType;
import com.sec.internal.ims.cmstore.MessageStoreClient;
import com.sec.internal.ims.cmstore.utils.RetryParam;
import com.sec.internal.interfaces.ims.cmstore.IHttpAPICommonInterface;
import java.util.HashMap;
import java.util.Map;

public class RetryMapAdapter {
    private static final RetryMapAdapter sInstance = new RetryMapAdapter();
    public String TAG;
    private transient Map<Pair<IHttpAPICommonInterface, SyncMsgType>, RetryParam> mMap = new HashMap();
    private MessageStoreClient messageStoreClient;

    public RetryMapAdapter() {
        String simpleName = RetryMapAdapter.class.getSimpleName();
        this.TAG = simpleName;
        Log.i(simpleName, "RetryMapAdapter Constructor");
    }

    public static RetryMapAdapter getInstance() {
        return sInstance;
    }

    public synchronized void initRetryMapAdapter(MessageStoreClient messageStoreClient2) {
        this.messageStoreClient = messageStoreClient2;
        this.TAG += "[" + messageStoreClient2.getClientID() + "]";
    }

    public synchronized boolean increaseRetryCount(Pair<IHttpAPICommonInterface, SyncMsgType> pair, int i) {
        if (this.mMap.containsKey(pair)) {
            RetryParam retryParam = this.mMap.get(pair);
            String str = this.TAG;
            Log.i(str, "increaseRetryCount, Already Exists in Map with retried count :" + retryParam.getRetryCount());
            if (retryParam.getRetryCount() == 2) {
                Log.i(this.TAG, "increaseRetryCount, removed key ");
                this.mMap.remove(pair);
                return false;
            }
            retryParam.setRetryCount(retryParam.getRetryCount() + 1);
            return true;
        }
        String str2 = this.TAG;
        Log.i(str2, "increaseRetryCount, New to Map, added entry for errorCode: " + i);
        this.mMap.put(pair, new RetryParam((IHttpAPICommonInterface) pair.first, 1, i));
        return true;
    }

    /* JADX WARNING: Unknown top exception splitter block from list: {B:22:0x0032=Splitter:B:22:0x0032, B:14:0x001f=Splitter:B:14:0x001f} */
    /* Code decompiled incorrectly, please refer to instructions dump. */
    public synchronized boolean searchAndPush(android.util.Pair<com.sec.internal.interfaces.ims.cmstore.IHttpAPICommonInterface, com.sec.internal.constants.ims.cmstore.omanetapi.SyncMsgType> r4, int r5) {
        /*
            r3 = this;
            monitor-enter(r3)
            r0 = 0
            if (r4 == 0) goto L_0x0032
            java.util.Map<android.util.Pair<com.sec.internal.interfaces.ims.cmstore.IHttpAPICommonInterface, com.sec.internal.constants.ims.cmstore.omanetapi.SyncMsgType>, com.sec.internal.ims.cmstore.utils.RetryParam> r1 = r3.mMap     // Catch:{ all -> 0x003c }
            if (r1 != 0) goto L_0x0009
            goto L_0x0032
        L_0x0009:
            java.lang.Object r1 = r4.first     // Catch:{ all -> 0x003c }
            com.sec.internal.interfaces.ims.cmstore.IHttpAPICommonInterface r1 = (com.sec.internal.interfaces.ims.cmstore.IHttpAPICommonInterface) r1     // Catch:{ all -> 0x003c }
            boolean r2 = r1 instanceof com.sec.internal.ims.cmstore.ambs.globalsetting.BaseProvisionAPIRequest     // Catch:{ all -> 0x003c }
            if (r2 != 0) goto L_0x001f
            boolean r1 = r1 instanceof com.sec.internal.omanetapi.nms.BaseNMSRequest     // Catch:{ all -> 0x003c }
            if (r1 != 0) goto L_0x001f
            java.lang.String r4 = r3.TAG     // Catch:{ all -> 0x003c }
            java.lang.String r5 = "searchAndPush, returning because of wrong request"
            android.util.Log.i(r4, r5)     // Catch:{ all -> 0x003c }
            monitor-exit(r3)
            return r0
        L_0x001f:
            boolean r4 = r3.increaseRetryCount(r4, r5)     // Catch:{ all -> 0x003c }
            if (r4 != 0) goto L_0x002f
            java.lang.String r4 = r3.TAG     // Catch:{ all -> 0x003c }
            java.lang.String r5 = "searchAndPush, Already exhausted Max Counts"
            android.util.Log.i(r4, r5)     // Catch:{ all -> 0x003c }
            monitor-exit(r3)
            return r0
        L_0x002f:
            monitor-exit(r3)
            r3 = 1
            return r3
        L_0x0032:
            java.lang.String r4 = r3.TAG     // Catch:{ all -> 0x003c }
            java.lang.String r5 = "searchAndPush, param or mMap is null"
            android.util.Log.i(r4, r5)     // Catch:{ all -> 0x003c }
            monitor-exit(r3)
            return r0
        L_0x003c:
            r4 = move-exception
            monitor-exit(r3)
            throw r4
        */
        throw new UnsupportedOperationException("Method not decompiled: com.sec.internal.ims.cmstore.adapters.RetryMapAdapter.searchAndPush(android.util.Pair, int):boolean");
    }

    /* JADX WARNING: Code restructure failed: missing block: B:11:0x001a, code lost:
        return;
     */
    /* JADX WARNING: Code restructure failed: missing block: B:13:0x001c, code lost:
        return;
     */
    /* Code decompiled incorrectly, please refer to instructions dump. */
    public synchronized void remove(android.util.Pair<com.sec.internal.interfaces.ims.cmstore.IHttpAPICommonInterface, com.sec.internal.constants.ims.cmstore.omanetapi.SyncMsgType> r2) {
        /*
            r1 = this;
            monitor-enter(r1)
            java.util.Map<android.util.Pair<com.sec.internal.interfaces.ims.cmstore.IHttpAPICommonInterface, com.sec.internal.constants.ims.cmstore.omanetapi.SyncMsgType>, com.sec.internal.ims.cmstore.utils.RetryParam> r0 = r1.mMap     // Catch:{ all -> 0x001d }
            if (r0 == 0) goto L_0x001b
            int r0 = r0.size()     // Catch:{ all -> 0x001d }
            if (r0 != 0) goto L_0x000c
            goto L_0x001b
        L_0x000c:
            java.util.Map<android.util.Pair<com.sec.internal.interfaces.ims.cmstore.IHttpAPICommonInterface, com.sec.internal.constants.ims.cmstore.omanetapi.SyncMsgType>, com.sec.internal.ims.cmstore.utils.RetryParam> r0 = r1.mMap     // Catch:{ all -> 0x001d }
            boolean r0 = r0.containsKey(r2)     // Catch:{ all -> 0x001d }
            if (r0 == 0) goto L_0x0019
            java.util.Map<android.util.Pair<com.sec.internal.interfaces.ims.cmstore.IHttpAPICommonInterface, com.sec.internal.constants.ims.cmstore.omanetapi.SyncMsgType>, com.sec.internal.ims.cmstore.utils.RetryParam> r0 = r1.mMap     // Catch:{ all -> 0x001d }
            r0.remove(r2)     // Catch:{ all -> 0x001d }
        L_0x0019:
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
        throw new UnsupportedOperationException("Method not decompiled: com.sec.internal.ims.cmstore.adapters.RetryMapAdapter.remove(android.util.Pair):void");
    }

    public synchronized void clearRetryHistory() {
        Log.i(this.TAG, "clearRetryCounter: retry history cleared");
        Map<Pair<IHttpAPICommonInterface, SyncMsgType>, RetryParam> map = this.mMap;
        if (map != null) {
            map.clear();
        }
    }

    /* JADX WARNING: Removed duplicated region for block: B:20:0x00bc  */
    /* JADX WARNING: Removed duplicated region for block: B:23:0x00de A[DONT_GENERATE] */
    /* Code decompiled incorrectly, please refer to instructions dump. */
    public synchronized boolean retryApi(android.util.Pair<com.sec.internal.interfaces.ims.cmstore.IHttpAPICommonInterface, com.sec.internal.constants.ims.cmstore.omanetapi.SyncMsgType> r9, com.sec.internal.interfaces.ims.cmstore.IAPICallFlowListener r10, com.sec.internal.interfaces.ims.cmstore.ICloudMessageManagerHelper r11, com.sec.internal.interfaces.ims.cmstore.IRetryStackAdapterHelper r12) {
        /*
            r8 = this;
            monitor-enter(r8)
            java.lang.String r0 = r8.TAG     // Catch:{ all -> 0x00e1 }
            java.lang.StringBuilder r1 = new java.lang.StringBuilder     // Catch:{ all -> 0x00e1 }
            r1.<init>()     // Catch:{ all -> 0x00e1 }
            java.lang.String r2 = "retryApi : Second "
            r1.append(r2)     // Catch:{ all -> 0x00e1 }
            java.lang.Object r2 = r9.second     // Catch:{ all -> 0x00e1 }
            com.sec.internal.constants.ims.cmstore.omanetapi.SyncMsgType r2 = (com.sec.internal.constants.ims.cmstore.omanetapi.SyncMsgType) r2     // Catch:{ all -> 0x00e1 }
            java.lang.String r2 = r2.toString()     // Catch:{ all -> 0x00e1 }
            r1.append(r2)     // Catch:{ all -> 0x00e1 }
            java.lang.String r1 = r1.toString()     // Catch:{ all -> 0x00e1 }
            android.util.Log.i(r0, r1)     // Catch:{ all -> 0x00e1 }
            java.util.Map<android.util.Pair<com.sec.internal.interfaces.ims.cmstore.IHttpAPICommonInterface, com.sec.internal.constants.ims.cmstore.omanetapi.SyncMsgType>, com.sec.internal.ims.cmstore.utils.RetryParam> r0 = r8.mMap     // Catch:{ all -> 0x00e1 }
            if (r0 == 0) goto L_0x00b9
            boolean r0 = r0.isEmpty()     // Catch:{ all -> 0x00e1 }
            if (r0 != 0) goto L_0x00b9
            if (r10 == 0) goto L_0x00b9
            java.util.Map<android.util.Pair<com.sec.internal.interfaces.ims.cmstore.IHttpAPICommonInterface, com.sec.internal.constants.ims.cmstore.omanetapi.SyncMsgType>, com.sec.internal.ims.cmstore.utils.RetryParam> r0 = r8.mMap     // Catch:{ all -> 0x00e1 }
            java.lang.Object r0 = r0.get(r9)     // Catch:{ all -> 0x00e1 }
            com.sec.internal.ims.cmstore.utils.RetryParam r0 = (com.sec.internal.ims.cmstore.utils.RetryParam) r0     // Catch:{ all -> 0x00e1 }
            if (r0 == 0) goto L_0x00b9
            int r1 = r0.getErrorCode()     // Catch:{ all -> 0x00e1 }
            com.sec.internal.ims.cmstore.MessageStoreClient r2 = r8.messageStoreClient     // Catch:{ all -> 0x00e1 }
            com.sec.internal.ims.cmstore.strategy.CloudMessageStrategyManager r2 = r2.getCloudMessageStrategyManager()     // Catch:{ all -> 0x00e1 }
            com.sec.internal.interfaces.ims.cmstore.ICloudMessageStrategy r2 = r2.getStrategy()     // Catch:{ all -> 0x00e1 }
            long r1 = r2.getTimerValue(r1)     // Catch:{ all -> 0x00e1 }
            long r3 = java.lang.System.currentTimeMillis()     // Catch:{ all -> 0x00e1 }
            long r5 = r0.getLastExecuted()     // Catch:{ all -> 0x00e1 }
            long r5 = r5 + r1
            int r0 = (r3 > r5 ? 1 : (r3 == r5 ? 0 : -1))
            if (r0 < 0) goto L_0x00b9
            java.lang.Object r0 = r9.first     // Catch:{ all -> 0x00e1 }
            com.sec.internal.interfaces.ims.cmstore.IHttpAPICommonInterface r0 = (com.sec.internal.interfaces.ims.cmstore.IHttpAPICommonInterface) r0     // Catch:{ all -> 0x00e1 }
            java.lang.String r1 = r8.TAG     // Catch:{ all -> 0x00e1 }
            java.lang.StringBuilder r2 = new java.lang.StringBuilder     // Catch:{ all -> 0x00e1 }
            r2.<init>()     // Catch:{ all -> 0x00e1 }
            java.lang.String r3 = "Time Exhausted, Last Retry Val: "
            r2.append(r3)     // Catch:{ all -> 0x00e1 }
            java.lang.Class r3 = r0.getClass()     // Catch:{ all -> 0x00e1 }
            java.lang.String r3 = r3.getSimpleName()     // Catch:{ all -> 0x00e1 }
            r2.append(r3)     // Catch:{ all -> 0x00e1 }
            java.lang.String r2 = r2.toString()     // Catch:{ all -> 0x00e1 }
            android.util.Log.i(r1, r2)     // Catch:{ all -> 0x00e1 }
            java.lang.Object r1 = r9.second     // Catch:{ all -> 0x00e1 }
            com.sec.internal.constants.ims.cmstore.omanetapi.SyncMsgType r1 = (com.sec.internal.constants.ims.cmstore.omanetapi.SyncMsgType) r1     // Catch:{ all -> 0x00e1 }
            com.sec.internal.constants.ims.cmstore.omanetapi.SyncMsgType r2 = com.sec.internal.constants.ims.cmstore.omanetapi.SyncMsgType.VM     // Catch:{ all -> 0x00e1 }
            boolean r1 = r1.equals(r2)     // Catch:{ all -> 0x00e1 }
            if (r1 != 0) goto L_0x00a0
            java.lang.Object r1 = r9.second     // Catch:{ all -> 0x00e1 }
            com.sec.internal.constants.ims.cmstore.omanetapi.SyncMsgType r1 = (com.sec.internal.constants.ims.cmstore.omanetapi.SyncMsgType) r1     // Catch:{ all -> 0x00e1 }
            com.sec.internal.constants.ims.cmstore.omanetapi.SyncMsgType r2 = com.sec.internal.constants.ims.cmstore.omanetapi.SyncMsgType.VM_GREETINGS     // Catch:{ all -> 0x00e1 }
            boolean r1 = r1.equals(r2)     // Catch:{ all -> 0x00e1 }
            if (r1 == 0) goto L_0x0090
            goto L_0x00a0
        L_0x0090:
            com.sec.internal.ims.cmstore.MessageStoreClient r9 = r8.messageStoreClient     // Catch:{ all -> 0x00e1 }
            com.sec.internal.ims.cmstore.utils.CmsHttpController r9 = r9.getHttpController()     // Catch:{ all -> 0x00e1 }
            com.sec.internal.ims.cmstore.MessageStoreClient r1 = r8.messageStoreClient     // Catch:{ all -> 0x00e1 }
            com.sec.internal.helper.httpclient.HttpRequestParams r10 = r0.getRetryInstance(r10, r1, r11, r12)     // Catch:{ all -> 0x00e1 }
            r9.execute(r10)     // Catch:{ all -> 0x00e1 }
            goto L_0x00ba
        L_0x00a0:
            com.sec.internal.ims.cmstore.MessageStoreClient r1 = r8.messageStoreClient     // Catch:{ all -> 0x00e1 }
            com.sec.internal.ims.cmstore.utils.CmsHttpController r7 = r1.getHttpController()     // Catch:{ all -> 0x00e1 }
            java.lang.Object r9 = r9.second     // Catch:{ all -> 0x00e1 }
            r2 = r9
            com.sec.internal.constants.ims.cmstore.omanetapi.SyncMsgType r2 = (com.sec.internal.constants.ims.cmstore.omanetapi.SyncMsgType) r2     // Catch:{ all -> 0x00e1 }
            com.sec.internal.ims.cmstore.MessageStoreClient r4 = r8.messageStoreClient     // Catch:{ all -> 0x00e1 }
            r1 = r0
            r3 = r10
            r5 = r11
            r6 = r12
            com.sec.internal.helper.httpclient.HttpRequestParams r9 = r1.getRetryInstance(r2, r3, r4, r5, r6)     // Catch:{ all -> 0x00e1 }
            r7.execute(r9)     // Catch:{ all -> 0x00e1 }
            goto L_0x00ba
        L_0x00b9:
            r0 = 0
        L_0x00ba:
            if (r0 == 0) goto L_0x00de
            java.lang.String r9 = r8.TAG     // Catch:{ all -> 0x00e1 }
            java.lang.StringBuilder r10 = new java.lang.StringBuilder     // Catch:{ all -> 0x00e1 }
            r10.<init>()     // Catch:{ all -> 0x00e1 }
            java.lang.String r11 = "retryLastApi: "
            r10.append(r11)     // Catch:{ all -> 0x00e1 }
            java.lang.Class r11 = r0.getClass()     // Catch:{ all -> 0x00e1 }
            java.lang.String r11 = r11.getSimpleName()     // Catch:{ all -> 0x00e1 }
            r10.append(r11)     // Catch:{ all -> 0x00e1 }
            java.lang.String r10 = r10.toString()     // Catch:{ all -> 0x00e1 }
            android.util.Log.i(r9, r10)     // Catch:{ all -> 0x00e1 }
            monitor-exit(r8)
            r8 = 1
            return r8
        L_0x00de:
            monitor-exit(r8)
            r8 = 0
            return r8
        L_0x00e1:
            r9 = move-exception
            monitor-exit(r8)
            throw r9
        */
        throw new UnsupportedOperationException("Method not decompiled: com.sec.internal.ims.cmstore.adapters.RetryMapAdapter.retryApi(android.util.Pair, com.sec.internal.interfaces.ims.cmstore.IAPICallFlowListener, com.sec.internal.interfaces.ims.cmstore.ICloudMessageManagerHelper, com.sec.internal.interfaces.ims.cmstore.IRetryStackAdapterHelper):boolean");
    }
}
