package com.sec.internal.ims.servicemodules.volte2;

import android.os.Looper;
import android.util.Log;
import com.sec.ims.ImsRegistration;
import com.sec.internal.constants.Mno;

public class ImsCallSessionFactory {
    private static final String LOG_TAG = "ImsCallSessionFactory";
    private static int mCallIdCounter;
    private IVolteServiceModuleInternal mModule;
    private Looper mServiceModuleLooper;

    public ImsCallSessionFactory(IVolteServiceModuleInternal iVolteServiceModuleInternal, Looper looper) {
        this.mModule = iVolteServiceModuleInternal;
        this.mServiceModuleLooper = looper;
    }

    /* JADX WARNING: type inference failed for: r10v1, types: [com.sec.internal.ims.servicemodules.volte2.ImsCallSession] */
    /* JADX WARNING: type inference failed for: r1v12, types: [com.sec.internal.ims.servicemodules.volte2.ImsConfSession] */
    /* JADX WARNING: type inference failed for: r1v13, types: [com.sec.internal.ims.servicemodules.volte2.ImsCallSession] */
    /* JADX WARNING: type inference failed for: r1v14, types: [com.sec.internal.ims.servicemodules.volte2.ImsCallSession] */
    /* JADX WARNING: type inference failed for: r1v15, types: [com.sec.internal.ims.servicemodules.volte2.ImsConfSession] */
    /* JADX WARNING: type inference failed for: r10v17 */
    /* JADX WARNING: Multi-variable type inference failed */
    /* Code decompiled incorrectly, please refer to instructions dump. */
    public synchronized com.sec.internal.ims.servicemodules.volte2.ImsCallSession create(com.sec.ims.volte2.data.CallProfile r8, com.sec.ims.ImsRegistration r9, boolean r10) {
        /*
            r7 = this;
            monitor-enter(r7)
            if (r9 != 0) goto L_0x000c
            int r0 = r8.getPhoneId()     // Catch:{ all -> 0x0127 }
            com.sec.internal.constants.Mno r0 = com.sec.internal.helper.SimUtil.getSimMno(r0)     // Catch:{ all -> 0x0127 }
            goto L_0x0018
        L_0x000c:
            com.sec.ims.settings.ImsProfile r0 = r9.getImsProfile()     // Catch:{ all -> 0x0127 }
            java.lang.String r0 = r0.getMnoName()     // Catch:{ all -> 0x0127 }
            com.sec.internal.constants.Mno r0 = com.sec.internal.constants.Mno.fromName(r0)     // Catch:{ all -> 0x0127 }
        L_0x0018:
            com.sec.internal.helper.Preconditions.checkNotNull(r8)     // Catch:{ all -> 0x0127 }
            int r1 = r8.getNetworkType()     // Catch:{ all -> 0x0127 }
            r2 = 15
            if (r1 == r2) goto L_0x0028
            if (r10 != 0) goto L_0x0028
            com.sec.internal.helper.Preconditions.checkNotNull(r9)     // Catch:{ all -> 0x0127 }
        L_0x0028:
            int r1 = r8.getNetworkType()     // Catch:{ all -> 0x0127 }
            if (r1 != r2) goto L_0x0046
            java.lang.String r10 = "ImsCallSessionFactory"
            java.lang.String r0 = "createImsCallSession: emergency session."
            android.util.Log.i(r10, r0)     // Catch:{ all -> 0x0127 }
            com.sec.internal.ims.servicemodules.volte2.ImsEmergencySession r10 = new com.sec.internal.ims.servicemodules.volte2.ImsEmergencySession     // Catch:{ all -> 0x0127 }
            com.sec.internal.ims.servicemodules.volte2.IVolteServiceModuleInternal r0 = r7.mModule     // Catch:{ all -> 0x0127 }
            android.content.Context r0 = r0.getContext()     // Catch:{ all -> 0x0127 }
            android.os.Looper r1 = r7.mServiceModuleLooper     // Catch:{ all -> 0x0127 }
            com.sec.internal.ims.servicemodules.volte2.IVolteServiceModuleInternal r2 = r7.mModule     // Catch:{ all -> 0x0127 }
            r10.<init>(r0, r8, r1, r2)     // Catch:{ all -> 0x0127 }
            goto L_0x00cc
        L_0x0046:
            if (r9 != 0) goto L_0x008c
            if (r10 == 0) goto L_0x008c
            int r10 = r8.getCmcType()     // Catch:{ all -> 0x0127 }
            if (r10 != 0) goto L_0x0072
            com.sec.internal.constants.Mno r10 = com.sec.internal.constants.Mno.SKT     // Catch:{ all -> 0x0127 }
            if (r0 == r10) goto L_0x0058
            com.sec.internal.constants.Mno r10 = com.sec.internal.constants.Mno.LGU     // Catch:{ all -> 0x0127 }
            if (r0 != r10) goto L_0x0072
        L_0x0058:
            java.lang.String r10 = "ImsCallSessionFactory"
            java.lang.String r0 = "createImsCallSession: conf call session without regi info"
            android.util.Log.i(r10, r0)     // Catch:{ all -> 0x0127 }
            com.sec.internal.ims.servicemodules.volte2.ImsConfSession r10 = new com.sec.internal.ims.servicemodules.volte2.ImsConfSession     // Catch:{ all -> 0x0127 }
            com.sec.internal.ims.servicemodules.volte2.IVolteServiceModuleInternal r0 = r7.mModule     // Catch:{ all -> 0x0127 }
            android.content.Context r2 = r0.getContext()     // Catch:{ all -> 0x0127 }
            android.os.Looper r5 = r7.mServiceModuleLooper     // Catch:{ all -> 0x0127 }
            com.sec.internal.ims.servicemodules.volte2.IVolteServiceModuleInternal r6 = r7.mModule     // Catch:{ all -> 0x0127 }
            r1 = r10
            r3 = r8
            r4 = r9
            r1.<init>(r2, r3, r4, r5, r6)     // Catch:{ all -> 0x0127 }
            goto L_0x00cc
        L_0x0072:
            java.lang.String r10 = "ImsCallSessionFactory"
            java.lang.String r0 = "createImsCallSession: normal call session without regi info"
            android.util.Log.i(r10, r0)     // Catch:{ all -> 0x0127 }
            com.sec.internal.ims.servicemodules.volte2.ImsCallSession r10 = new com.sec.internal.ims.servicemodules.volte2.ImsCallSession     // Catch:{ all -> 0x0127 }
            com.sec.internal.ims.servicemodules.volte2.IVolteServiceModuleInternal r0 = r7.mModule     // Catch:{ all -> 0x0127 }
            android.content.Context r2 = r0.getContext()     // Catch:{ all -> 0x0127 }
            android.os.Looper r5 = r7.mServiceModuleLooper     // Catch:{ all -> 0x0127 }
            com.sec.internal.ims.servicemodules.volte2.IVolteServiceModuleInternal r6 = r7.mModule     // Catch:{ all -> 0x0127 }
            r1 = r10
            r3 = r8
            r4 = r9
            r1.<init>(r2, r3, r4, r5, r6)     // Catch:{ all -> 0x0127 }
            goto L_0x00cc
        L_0x008c:
            boolean r10 = r8.isConferenceCall()     // Catch:{ all -> 0x0127 }
            if (r10 != 0) goto L_0x00b3
            boolean r10 = r7.isDefaultConfSession(r9)     // Catch:{ all -> 0x0127 }
            if (r10 == 0) goto L_0x0099
            goto L_0x00b3
        L_0x0099:
            java.lang.String r10 = "ImsCallSessionFactory"
            java.lang.String r0 = "createImsCallSession: normal call session"
            android.util.Log.i(r10, r0)     // Catch:{ all -> 0x0127 }
            com.sec.internal.ims.servicemodules.volte2.ImsCallSession r10 = new com.sec.internal.ims.servicemodules.volte2.ImsCallSession     // Catch:{ all -> 0x0127 }
            com.sec.internal.ims.servicemodules.volte2.IVolteServiceModuleInternal r0 = r7.mModule     // Catch:{ all -> 0x0127 }
            android.content.Context r2 = r0.getContext()     // Catch:{ all -> 0x0127 }
            android.os.Looper r5 = r7.mServiceModuleLooper     // Catch:{ all -> 0x0127 }
            com.sec.internal.ims.servicemodules.volte2.IVolteServiceModuleInternal r6 = r7.mModule     // Catch:{ all -> 0x0127 }
            r1 = r10
            r3 = r8
            r4 = r9
            r1.<init>(r2, r3, r4, r5, r6)     // Catch:{ all -> 0x0127 }
            goto L_0x00cc
        L_0x00b3:
            java.lang.String r10 = "ImsCallSessionFactory"
            java.lang.String r0 = "createImsCallSession: conference session."
            android.util.Log.i(r10, r0)     // Catch:{ all -> 0x0127 }
            com.sec.internal.ims.servicemodules.volte2.ImsConfSession r10 = new com.sec.internal.ims.servicemodules.volte2.ImsConfSession     // Catch:{ all -> 0x0127 }
            com.sec.internal.ims.servicemodules.volte2.IVolteServiceModuleInternal r0 = r7.mModule     // Catch:{ all -> 0x0127 }
            android.content.Context r2 = r0.getContext()     // Catch:{ all -> 0x0127 }
            android.os.Looper r5 = r7.mServiceModuleLooper     // Catch:{ all -> 0x0127 }
            com.sec.internal.ims.servicemodules.volte2.IVolteServiceModuleInternal r6 = r7.mModule     // Catch:{ all -> 0x0127 }
            r1 = r10
            r3 = r8
            r4 = r9
            r1.<init>(r2, r3, r4, r5, r6)     // Catch:{ all -> 0x0127 }
        L_0x00cc:
            com.sec.internal.interfaces.ims.core.handler.IHandlerFactory r0 = com.sec.internal.ims.registry.ImsRegistry.getHandlerFactory()     // Catch:{ all -> 0x0127 }
            com.sec.internal.interfaces.ims.core.handler.IVolteServiceInterface r0 = r0.getVolteStackAdaptor()     // Catch:{ all -> 0x0127 }
            com.sec.internal.interfaces.ims.core.IRegistrationManager r1 = com.sec.internal.ims.registry.ImsRegistry.getRegistrationManager()     // Catch:{ all -> 0x0127 }
            r10.init(r0, r1)     // Catch:{ all -> 0x0127 }
            int r0 = r7.createCallId()     // Catch:{ all -> 0x0127 }
            if (r0 >= 0) goto L_0x00e4
            monitor-exit(r7)
            r7 = 0
            return r7
        L_0x00e4:
            if (r9 == 0) goto L_0x0103
            com.sec.ims.settings.ImsProfile r1 = r9.getImsProfile()     // Catch:{ all -> 0x0127 }
            if (r1 == 0) goto L_0x0103
            com.sec.ims.settings.ImsProfile r8 = r9.getImsProfile()     // Catch:{ all -> 0x0127 }
            int r8 = r8.getCmcType()     // Catch:{ all -> 0x0127 }
            r10.setCmcType(r8)     // Catch:{ all -> 0x0127 }
            com.sec.ims.settings.ImsProfile r8 = r9.getImsProfile()     // Catch:{ all -> 0x0127 }
            int r8 = r8.getVideoCrbtSupportType()     // Catch:{ all -> 0x0127 }
            r10.setVideoCrbtSupportType(r8)     // Catch:{ all -> 0x0127 }
            goto L_0x0122
        L_0x0103:
            if (r9 != 0) goto L_0x0122
            int r9 = r8.getCmcType()     // Catch:{ all -> 0x0127 }
            r1 = 2
            if (r9 == r1) goto L_0x011b
            int r9 = r8.getCmcType()     // Catch:{ all -> 0x0127 }
            r1 = 4
            if (r9 == r1) goto L_0x011b
            int r9 = r8.getCmcType()     // Catch:{ all -> 0x0127 }
            r1 = 8
            if (r9 != r1) goto L_0x0122
        L_0x011b:
            int r8 = r8.getCmcType()     // Catch:{ all -> 0x0127 }
            r10.setCmcType(r8)     // Catch:{ all -> 0x0127 }
        L_0x0122:
            r10.setCallId(r0)     // Catch:{ all -> 0x0127 }
            monitor-exit(r7)
            return r10
        L_0x0127:
            r8 = move-exception
            monitor-exit(r7)
            throw r8
        */
        throw new UnsupportedOperationException("Method not decompiled: com.sec.internal.ims.servicemodules.volte2.ImsCallSessionFactory.create(com.sec.ims.volte2.data.CallProfile, com.sec.ims.ImsRegistration, boolean):com.sec.internal.ims.servicemodules.volte2.ImsCallSession");
    }

    private boolean isDefaultConfSession(ImsRegistration imsRegistration) {
        if (imsRegistration == null) {
            return false;
        }
        Mno fromName = Mno.fromName(imsRegistration.getImsProfile().getMnoName());
        return (fromName == Mno.SKT || fromName == Mno.LGU) && !imsRegistration.getImsProfile().isSamsungMdmnEnabled();
    }

    private int createCallId() {
        boolean z = false;
        while (true) {
            if (mCallIdCounter >= 255) {
                mCallIdCounter = 0;
                if (z) {
                    Log.e(LOG_TAG, "All CallId is allocated, session create fail");
                    return -1;
                }
                z = true;
            }
            int i = mCallIdCounter + 1;
            mCallIdCounter = i;
            if (this.mModule.getSessionByCallId(i) == null) {
                return mCallIdCounter;
            }
            Log.i(LOG_TAG, "Call " + mCallIdCounter + " is exist");
        }
    }
}
