package com.sec.internal.ims.cmstore.adapters;

import android.text.TextUtils;
import android.util.Base64;
import android.util.Log;
import com.sec.internal.constants.ims.cmstore.utils.CloudMessagePreferenceConstants;
import com.sec.internal.ims.cmstore.MessageStoreClient;
import com.sec.internal.interfaces.ims.cmstore.IAPICallFlowListener;
import com.sec.internal.interfaces.ims.cmstore.ICloudMessageManagerHelper;
import com.sec.internal.interfaces.ims.cmstore.IHttpAPICommonInterface;
import com.sec.internal.interfaces.ims.cmstore.IRetryStackAdapterHelper;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.OptionalDataException;
import java.util.Stack;

public class RetryStackAdapter {
    private static final RetryStackAdapter sInstance = new RetryStackAdapter();
    public String TAG = RetryStackAdapter.class.getSimpleName();
    private Stack<IHttpAPICommonInterface> mStack = new Stack<>();
    private MessageStoreClient mStoreClient;

    public static RetryStackAdapter getInstance() {
        return sInstance;
    }

    /* JADX WARNING: Code restructure failed: missing block: B:11:0x002b, code lost:
        return false;
     */
    /* Code decompiled incorrectly, please refer to instructions dump. */
    public synchronized boolean checkRequestRetried(com.sec.internal.interfaces.ims.cmstore.IHttpAPICommonInterface r2) {
        /*
            r1 = this;
            monitor-enter(r1)
            java.util.Stack<com.sec.internal.interfaces.ims.cmstore.IHttpAPICommonInterface> r0 = r1.mStack     // Catch:{ all -> 0x002d }
            if (r0 == 0) goto L_0x002a
            boolean r0 = r0.empty()     // Catch:{ all -> 0x002d }
            if (r0 == 0) goto L_0x000c
            goto L_0x002a
        L_0x000c:
            java.util.Stack<com.sec.internal.interfaces.ims.cmstore.IHttpAPICommonInterface> r0 = r1.mStack     // Catch:{ all -> 0x002d }
            java.lang.Object r0 = r0.peek()     // Catch:{ all -> 0x002d }
            com.sec.internal.interfaces.ims.cmstore.IHttpAPICommonInterface r0 = (com.sec.internal.interfaces.ims.cmstore.IHttpAPICommonInterface) r0     // Catch:{ all -> 0x002d }
            java.lang.Class r0 = r0.getClass()     // Catch:{ all -> 0x002d }
            java.lang.String r0 = r0.getSimpleName()     // Catch:{ all -> 0x002d }
            java.lang.Class r2 = r2.getClass()     // Catch:{ all -> 0x002d }
            java.lang.String r2 = r2.getSimpleName()     // Catch:{ all -> 0x002d }
            boolean r2 = r0.equals(r2)     // Catch:{ all -> 0x002d }
            monitor-exit(r1)
            return r2
        L_0x002a:
            monitor-exit(r1)
            r1 = 0
            return r1
        L_0x002d:
            r2 = move-exception
            monitor-exit(r1)
            throw r2
        */
        throw new UnsupportedOperationException("Method not decompiled: com.sec.internal.ims.cmstore.adapters.RetryStackAdapter.checkRequestRetried(com.sec.internal.interfaces.ims.cmstore.IHttpAPICommonInterface):boolean");
    }

    /* JADX WARNING: Code restructure failed: missing block: B:11:0x0017, code lost:
        return null;
     */
    /* Code decompiled incorrectly, please refer to instructions dump. */
    public synchronized com.sec.internal.interfaces.ims.cmstore.IHttpAPICommonInterface getLastFailedRequest() {
        /*
            r1 = this;
            monitor-enter(r1)
            java.util.Stack<com.sec.internal.interfaces.ims.cmstore.IHttpAPICommonInterface> r0 = r1.mStack     // Catch:{ all -> 0x0019 }
            if (r0 == 0) goto L_0x0016
            boolean r0 = r0.empty()     // Catch:{ all -> 0x0019 }
            if (r0 == 0) goto L_0x000c
            goto L_0x0016
        L_0x000c:
            java.util.Stack<com.sec.internal.interfaces.ims.cmstore.IHttpAPICommonInterface> r0 = r1.mStack     // Catch:{ all -> 0x0019 }
            java.lang.Object r0 = r0.peek()     // Catch:{ all -> 0x0019 }
            com.sec.internal.interfaces.ims.cmstore.IHttpAPICommonInterface r0 = (com.sec.internal.interfaces.ims.cmstore.IHttpAPICommonInterface) r0     // Catch:{ all -> 0x0019 }
            monitor-exit(r1)
            return r0
        L_0x0016:
            monitor-exit(r1)
            r1 = 0
            return r1
        L_0x0019:
            r0 = move-exception
            monitor-exit(r1)
            throw r0
        */
        throw new UnsupportedOperationException("Method not decompiled: com.sec.internal.ims.cmstore.adapters.RetryStackAdapter.getLastFailedRequest():com.sec.internal.interfaces.ims.cmstore.IHttpAPICommonInterface");
    }

    /* JADX WARNING: Code restructure failed: missing block: B:21:0x0035, code lost:
        return false;
     */
    /* Code decompiled incorrectly, please refer to instructions dump. */
    public synchronized boolean searchAndPush(com.sec.internal.interfaces.ims.cmstore.IHttpAPICommonInterface r6) {
        /*
            r5 = this;
            monitor-enter(r5)
            r0 = 0
            if (r6 == 0) goto L_0x0034
            java.util.Stack<com.sec.internal.interfaces.ims.cmstore.IHttpAPICommonInterface> r1 = r5.mStack     // Catch:{ all -> 0x0031 }
            if (r1 == 0) goto L_0x0034
            boolean r1 = r6 instanceof com.sec.internal.ims.cmstore.ambs.globalsetting.BaseProvisionAPIRequest     // Catch:{ all -> 0x0031 }
            if (r1 != 0) goto L_0x000d
            goto L_0x0034
        L_0x000d:
            boolean r1 = r5.checkRequestRetried(r6)     // Catch:{ all -> 0x0031 }
            java.lang.String r2 = r5.TAG     // Catch:{ all -> 0x0031 }
            java.lang.StringBuilder r3 = new java.lang.StringBuilder     // Catch:{ all -> 0x0031 }
            r3.<init>()     // Catch:{ all -> 0x0031 }
            java.lang.String r4 = "Retried: "
            r3.append(r4)     // Catch:{ all -> 0x0031 }
            r3.append(r1)     // Catch:{ all -> 0x0031 }
            java.lang.String r3 = r3.toString()     // Catch:{ all -> 0x0031 }
            android.util.Log.i(r2, r3)     // Catch:{ all -> 0x0031 }
            if (r1 == 0) goto L_0x002b
            monitor-exit(r5)
            return r0
        L_0x002b:
            r5.push(r6)     // Catch:{ all -> 0x0031 }
            monitor-exit(r5)
            r5 = 1
            return r5
        L_0x0031:
            r6 = move-exception
            monitor-exit(r5)
            throw r6
        L_0x0034:
            monitor-exit(r5)
            return r0
        */
        throw new UnsupportedOperationException("Method not decompiled: com.sec.internal.ims.cmstore.adapters.RetryStackAdapter.searchAndPush(com.sec.internal.interfaces.ims.cmstore.IHttpAPICommonInterface):boolean");
    }

    private void push(IHttpAPICommonInterface iHttpAPICommonInterface) {
        if (iHttpAPICommonInterface != null) {
            this.mStack.push(iHttpAPICommonInterface);
            saveRetryStack();
        }
    }

    /* JADX WARNING: Code restructure failed: missing block: B:11:0x001a, code lost:
        return null;
     */
    /* Code decompiled incorrectly, please refer to instructions dump. */
    public synchronized com.sec.internal.interfaces.ims.cmstore.IHttpAPICommonInterface pop() {
        /*
            r1 = this;
            monitor-enter(r1)
            java.util.Stack<com.sec.internal.interfaces.ims.cmstore.IHttpAPICommonInterface> r0 = r1.mStack     // Catch:{ all -> 0x001c }
            if (r0 == 0) goto L_0x0019
            boolean r0 = r0.empty()     // Catch:{ all -> 0x001c }
            if (r0 == 0) goto L_0x000c
            goto L_0x0019
        L_0x000c:
            java.util.Stack<com.sec.internal.interfaces.ims.cmstore.IHttpAPICommonInterface> r0 = r1.mStack     // Catch:{ all -> 0x001c }
            java.lang.Object r0 = r0.pop()     // Catch:{ all -> 0x001c }
            com.sec.internal.interfaces.ims.cmstore.IHttpAPICommonInterface r0 = (com.sec.internal.interfaces.ims.cmstore.IHttpAPICommonInterface) r0     // Catch:{ all -> 0x001c }
            r1.saveRetryStack()     // Catch:{ all -> 0x001c }
            monitor-exit(r1)
            return r0
        L_0x0019:
            monitor-exit(r1)
            r1 = 0
            return r1
        L_0x001c:
            r0 = move-exception
            monitor-exit(r1)
            throw r0
        */
        throw new UnsupportedOperationException("Method not decompiled: com.sec.internal.ims.cmstore.adapters.RetryStackAdapter.pop():com.sec.internal.interfaces.ims.cmstore.IHttpAPICommonInterface");
    }

    public synchronized void saveRetryLastFailedTime(long j) {
        this.mStoreClient.getPrerenceManager().saveLastRetryTime(j);
    }

    public synchronized void clearRetryHistory() {
        Log.i(this.TAG, "clearRetryCounter: retry history cleared");
        Stack<IHttpAPICommonInterface> stack = this.mStack;
        if (stack != null) {
            stack.clear();
            saveRetryStack();
        }
        this.mStoreClient.getPrerenceManager().removeKey(CloudMessagePreferenceConstants.RETRY_TOTAL_COUNTER);
        this.mStoreClient.getPrerenceManager().removeKey(CloudMessagePreferenceConstants.LAST_RETRY_TIME);
    }

    public synchronized boolean isEmpty() {
        return this.mStack.isEmpty();
    }

    private void saveRetryStack() {
        Log.i(this.TAG, "save retryStack");
        try {
            ByteArrayOutputStream byteArrayOutputStream = new ByteArrayOutputStream();
            ObjectOutputStream objectOutputStream = new ObjectOutputStream(byteArrayOutputStream);
            objectOutputStream.writeObject(this.mStack);
            objectOutputStream.flush();
            this.mStoreClient.getPrerenceManager().saveRetryStackData(Base64.encodeToString(byteArrayOutputStream.toByteArray(), 0));
        } catch (IOException e) {
            Log.e(this.TAG, e.getMessage());
            e.printStackTrace();
        }
    }

    public boolean isRetryTimesFinished(ICloudMessageManagerHelper iCloudMessageManagerHelper) {
        int totalRetryCounter = this.mStoreClient.getPrerenceManager().getTotalRetryCounter();
        String str = this.TAG;
        Log.i(str, "totalCounter: " + totalRetryCounter);
        return this.mStoreClient.getCloudMessageStrategyManager().getStrategy().getMaxRetryCounter() <= totalRetryCounter;
    }

    public void retryApi(IHttpAPICommonInterface iHttpAPICommonInterface, IAPICallFlowListener iAPICallFlowListener, ICloudMessageManagerHelper iCloudMessageManagerHelper, IRetryStackAdapterHelper iRetryStackAdapterHelper) {
        if (iAPICallFlowListener != null) {
            String str = this.TAG;
            Log.i(str, "retryApi: " + iHttpAPICommonInterface.getClass().getSimpleName());
            this.mStoreClient.getHttpController().execute(iHttpAPICommonInterface.getRetryInstance(iAPICallFlowListener, this.mStoreClient, iCloudMessageManagerHelper, iRetryStackAdapterHelper));
        }
    }

    public synchronized boolean retryLastApi(IAPICallFlowListener iAPICallFlowListener, ICloudMessageManagerHelper iCloudMessageManagerHelper, IRetryStackAdapterHelper iRetryStackAdapterHelper) {
        Stack<IHttpAPICommonInterface> stack = this.mStack;
        IHttpAPICommonInterface peek = (stack == null || stack.isEmpty() || iAPICallFlowListener == null) ? null : this.mStack.peek();
        if (peek == null) {
            return false;
        }
        String str = this.TAG;
        Log.i(str, "retryLastApi: " + peek.getClass().getSimpleName());
        this.mStoreClient.getHttpController().execute(peek.getRetryInstance(iAPICallFlowListener, this.mStoreClient, iCloudMessageManagerHelper, iRetryStackAdapterHelper));
        return true;
    }

    public synchronized void initRetryStackAdapter(MessageStoreClient messageStoreClient) {
        this.mStoreClient = messageStoreClient;
        this.TAG += "[" + messageStoreClient.getClientID() + "]";
        String retryStackData = this.mStoreClient.getPrerenceManager().getRetryStackData();
        try {
            if (!TextUtils.isEmpty(retryStackData)) {
                this.mStack = (Stack) new ObjectInputStream(new ByteArrayInputStream(Base64.decode(retryStackData, 0))).readObject();
            }
        } catch (OptionalDataException e) {
            Log.e(getClass().getSimpleName(), e.getMessage());
        } catch (IOException e2) {
            Log.e(getClass().getSimpleName(), e2.getMessage());
        } catch (ClassNotFoundException e3) {
            Log.e(getClass().getSimpleName(), e3.getMessage());
        } catch (IllegalArgumentException e4) {
            Log.e(getClass().getSimpleName(), e4.getMessage());
            this.mStack = new Stack<>();
            clearRetryHistory();
        }
    }
}
