package com.sec.internal.ims.gba;

import android.os.RemoteException;
import android.telephony.IBootstrapAuthenticationCallback;
import android.telephony.gba.IGbaService;
import android.util.Base64;
import android.util.Log;
import android.util.Pair;
import android.util.SparseArray;
import com.sec.internal.helper.httpclient.HttpResponseParams;
import com.sec.internal.ims.servicemodules.base.ServiceModuleBase;
import com.sec.internal.interfaces.ims.gba.IGbaCallback;
import com.sec.internal.interfaces.ims.gba.IGbaServiceModule;

public class GbaService extends IGbaService.Stub {
    /* access modifiers changed from: private */
    public static final String LOG_TAG = GbaService.class.getSimpleName();
    /* access modifiers changed from: private */
    public final SparseArray<Pair<Integer, IBootstrapAuthenticationCallback>> mCallbacks = new SparseArray<>();
    private GbaHelper mGbaHelper;
    private IGbaServiceModule mModule;

    public GbaService(ServiceModuleBase serviceModuleBase) {
        this.mModule = (GbaServiceModule) serviceModuleBase;
        this.mGbaHelper = new GbaHelper();
    }

    /* JADX WARNING: Code restructure failed: missing block: B:11:0x0058, code lost:
        if (r2 != -1) goto L_0x007f;
     */
    /* JADX WARNING: Code restructure failed: missing block: B:12:0x005a, code lost:
        r6.getCallback().onAuthenticationFailure(r6.getToken(), 0);
        android.util.Log.e(r0, "authenticationRequest Fail " + r6.getToken());
     */
    /* JADX WARNING: Code restructure failed: missing block: B:13:0x007e, code lost:
        return;
     */
    /* JADX WARNING: Code restructure failed: missing block: B:14:0x007f, code lost:
        android.util.Log.d(r0, "authenticationRequest : Id " + r2);
        r0 = r5.mCallbacks;
     */
    /* JADX WARNING: Code restructure failed: missing block: B:15:0x0095, code lost:
        monitor-enter(r0);
     */
    /* JADX WARNING: Code restructure failed: missing block: B:17:?, code lost:
        r5.mCallbacks.put(r2, android.util.Pair.create(java.lang.Integer.valueOf(r6.getToken()), r6.getCallback()));
     */
    /* JADX WARNING: Code restructure failed: missing block: B:18:0x00ab, code lost:
        monitor-exit(r0);
     */
    /* JADX WARNING: Code restructure failed: missing block: B:19:0x00ac, code lost:
        return;
     */
    /* Code decompiled incorrectly, please refer to instructions dump. */
    public void authenticationRequest(android.telephony.gba.GbaAuthRequest r6) throws android.os.RemoteException {
        /*
            r5 = this;
            java.lang.String r0 = LOG_TAG
            java.lang.StringBuilder r1 = new java.lang.StringBuilder
            r1.<init>()
            java.lang.String r2 = "authenticationRequest : "
            r1.append(r2)
            java.lang.String r2 = r6.toString()
            r1.append(r2)
            java.lang.String r1 = r1.toString()
            android.util.Log.i(r0, r1)
            com.sec.internal.interfaces.ims.gba.IGbaServiceModule r1 = r5.mModule
            monitor-enter(r1)
            int r2 = r6.getSubId()     // Catch:{ all -> 0x00b0 }
            int r2 = com.sec.internal.ims.core.sim.SimManagerFactory.getSlotId(r2)     // Catch:{ all -> 0x00b0 }
            com.sec.internal.interfaces.ims.gba.IGbaServiceModule r3 = r5.mModule     // Catch:{ all -> 0x00b0 }
            android.net.Uri r4 = r6.getNafUrl()     // Catch:{ all -> 0x00b0 }
            java.lang.String r4 = r4.toString()     // Catch:{ all -> 0x00b0 }
            java.lang.String r4 = com.sec.internal.ims.gba.GbaUtility.getNafUrl(r4)     // Catch:{ all -> 0x00b0 }
            com.sec.internal.ims.gba.GbaValue r2 = r3.getGbaValue(r2, r4)     // Catch:{ all -> 0x00b0 }
            if (r2 == 0) goto L_0x004e
            android.telephony.IBootstrapAuthenticationCallback r5 = r6.getCallback()     // Catch:{ all -> 0x00b0 }
            int r6 = r6.getToken()     // Catch:{ all -> 0x00b0 }
            byte[] r0 = r2.getValue()     // Catch:{ all -> 0x00b0 }
            java.lang.String r2 = r2.getBtid()     // Catch:{ all -> 0x00b0 }
            r5.onKeysAvailable(r6, r0, r2)     // Catch:{ all -> 0x00b0 }
            monitor-exit(r1)     // Catch:{ all -> 0x00b0 }
            return
        L_0x004e:
            com.sec.internal.interfaces.ims.gba.IGbaServiceModule r2 = r5.mModule     // Catch:{ all -> 0x00b0 }
            com.sec.internal.ims.gba.GbaService$GbaHelper r3 = r5.mGbaHelper     // Catch:{ all -> 0x00b0 }
            int r2 = r2.getBtidAndGbaKey(r6, r3)     // Catch:{ all -> 0x00b0 }
            monitor-exit(r1)     // Catch:{ all -> 0x00b0 }
            r1 = -1
            if (r2 != r1) goto L_0x007f
            android.telephony.IBootstrapAuthenticationCallback r5 = r6.getCallback()
            int r1 = r6.getToken()
            r2 = 0
            r5.onAuthenticationFailure(r1, r2)
            java.lang.StringBuilder r5 = new java.lang.StringBuilder
            r5.<init>()
            java.lang.String r1 = "authenticationRequest Fail "
            r5.append(r1)
            int r6 = r6.getToken()
            r5.append(r6)
            java.lang.String r5 = r5.toString()
            android.util.Log.e(r0, r5)
            return
        L_0x007f:
            java.lang.StringBuilder r1 = new java.lang.StringBuilder
            r1.<init>()
            java.lang.String r3 = "authenticationRequest : Id "
            r1.append(r3)
            r1.append(r2)
            java.lang.String r1 = r1.toString()
            android.util.Log.d(r0, r1)
            android.util.SparseArray<android.util.Pair<java.lang.Integer, android.telephony.IBootstrapAuthenticationCallback>> r0 = r5.mCallbacks
            monitor-enter(r0)
            android.util.SparseArray<android.util.Pair<java.lang.Integer, android.telephony.IBootstrapAuthenticationCallback>> r5 = r5.mCallbacks     // Catch:{ all -> 0x00ad }
            int r1 = r6.getToken()     // Catch:{ all -> 0x00ad }
            java.lang.Integer r1 = java.lang.Integer.valueOf(r1)     // Catch:{ all -> 0x00ad }
            android.telephony.IBootstrapAuthenticationCallback r6 = r6.getCallback()     // Catch:{ all -> 0x00ad }
            android.util.Pair r6 = android.util.Pair.create(r1, r6)     // Catch:{ all -> 0x00ad }
            r5.put(r2, r6)     // Catch:{ all -> 0x00ad }
            monitor-exit(r0)     // Catch:{ all -> 0x00ad }
            return
        L_0x00ad:
            r5 = move-exception
            monitor-exit(r0)     // Catch:{ all -> 0x00ad }
            throw r5
        L_0x00b0:
            r5 = move-exception
            monitor-exit(r1)     // Catch:{ all -> 0x00b0 }
            throw r5
        */
        throw new UnsupportedOperationException("Method not decompiled: com.sec.internal.ims.gba.GbaService.authenticationRequest(android.telephony.gba.GbaAuthRequest):void");
    }

    private class GbaHelper implements IGbaCallback {
        private GbaHelper() {
        }

        public void onComplete(int i, String str, String str2, boolean z, HttpResponseParams httpResponseParams) {
            int i2;
            IBootstrapAuthenticationCallback iBootstrapAuthenticationCallback;
            String r5 = GbaService.LOG_TAG;
            Log.d(r5, "authenticationRequest : onComplete " + i);
            synchronized (GbaService.this.mCallbacks) {
                if (GbaService.this.mCallbacks.get(i) != null) {
                    i2 = ((Integer) ((Pair) GbaService.this.mCallbacks.get(i)).first).intValue();
                    iBootstrapAuthenticationCallback = (IBootstrapAuthenticationCallback) ((Pair) GbaService.this.mCallbacks.get(i)).second;
                    GbaService.this.mCallbacks.remove(i);
                } else {
                    iBootstrapAuthenticationCallback = null;
                    i2 = 0;
                }
            }
            if (iBootstrapAuthenticationCallback != null) {
                try {
                    iBootstrapAuthenticationCallback.onKeysAvailable(i2, Base64.decode(str2, 2), str);
                } catch (RemoteException unused) {
                }
            }
        }

        public void onFail(int i, GbaException gbaException) {
            IBootstrapAuthenticationCallback iBootstrapAuthenticationCallback;
            int i2;
            String r0 = GbaService.LOG_TAG;
            Log.d(r0, "authenticationRequest : onFail : " + i);
            synchronized (GbaService.this.mCallbacks) {
                if (GbaService.this.mCallbacks.get(i) != null) {
                    i2 = ((Integer) ((Pair) GbaService.this.mCallbacks.get(i)).first).intValue();
                    iBootstrapAuthenticationCallback = (IBootstrapAuthenticationCallback) ((Pair) GbaService.this.mCallbacks.get(i)).second;
                    GbaService.this.mCallbacks.remove(i);
                } else {
                    iBootstrapAuthenticationCallback = null;
                    i2 = 0;
                }
            }
            if (iBootstrapAuthenticationCallback != null) {
                try {
                    iBootstrapAuthenticationCallback.onAuthenticationFailure(i2, gbaException.getCode());
                } catch (RemoteException unused) {
                }
            }
        }
    }
}
