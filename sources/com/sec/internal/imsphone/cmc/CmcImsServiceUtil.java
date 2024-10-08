package com.sec.internal.imsphone.cmc;

import android.content.Intent;
import android.os.Bundle;
import android.os.RemoteException;
import android.os.SemSystemProperties;
import android.telephony.ims.ImsCallProfile;
import android.telephony.ims.aidl.IImsCallSessionListener;
import android.text.TextUtils;
import android.util.Log;
import com.android.internal.telephony.PublishDialog;
import com.sec.ims.ImsRegistration;
import com.sec.ims.volte2.IImsCallSession;
import com.sec.ims.volte2.data.CallProfile;
import com.sec.internal.helper.ImsCallUtil;
import com.sec.internal.helper.SimUtil;
import com.sec.internal.ims.registry.ImsRegistry;
import com.sec.internal.imsphone.DataTypeConvertor;
import com.sec.internal.imsphone.ImsCallSessionImpl;
import com.sec.internal.imsphone.MmTelFeatureImpl;
import com.sec.internal.imsphone.ServiceProfile;
import com.sec.internal.imsphone.cmc.ICmcConnectivityController;
import com.sec.internal.interfaces.ims.core.IRegistrationGovernor;
import com.sec.internal.interfaces.ims.servicemodules.volte2.IVolteServiceModule;
import java.util.Iterator;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

public class CmcImsServiceUtil {
    private static final String LOG_TAG = "CmcImsServiceUtil";
    private final int P2P_CALL_SESSION_ID = 999;
    private boolean mCmcReady = true;
    private int mCmcRegHandle = -1;
    ICmcConnectivityController mConnectivityController = null;
    MmTelFeatureImpl mMmTelFeatureImpl = null;
    ServiceProfile mServiceProfile = null;
    IVolteServiceModule mVolteServiceModule = null;
    private Map<Integer, IImsCallSession> mp2pSecSessionMap = new ConcurrentHashMap();

    public CmcImsServiceUtil(MmTelFeatureImpl mmTelFeatureImpl, ICmcConnectivityController iCmcConnectivityController, IVolteServiceModule iVolteServiceModule) {
        this.mMmTelFeatureImpl = mmTelFeatureImpl;
        this.mConnectivityController = iCmcConnectivityController;
        this.mVolteServiceModule = iVolteServiceModule;
    }

    public void acquireP2pNetwork() {
        if (this.mConnectivityController.getDeviceType() == ICmcConnectivityController.DeviceType.PDevice) {
            Log.i(LOG_TAG, "MO call by PD. startCmcP2pConnection!");
        }
    }

    public void setServiceProfile(ServiceProfile serviceProfile) {
        this.mServiceProfile = serviceProfile;
    }

    private int getCmcRegHandle(int i, int i2) {
        IRegistrationGovernor registrationGovernor;
        ImsRegistration[] registrationInfo = ImsRegistry.getRegistrationManager().getRegistrationInfo();
        int length = registrationInfo.length;
        int i3 = 0;
        while (i3 < length) {
            ImsRegistration imsRegistration = registrationInfo[i3];
            if (imsRegistration == null || ((imsRegistration.getPhoneId() != i && !ImsRegistry.getCmcAccountManager().isSupportDualSimCMC()) || !imsRegistration.hasVolteService() || imsRegistration.getImsProfile() == null || imsRegistration.getImsProfile().getCmcType() != i2)) {
                i3++;
            } else if (!ImsCallUtil.isP2pPrimaryType(i2) || ((registrationGovernor = ImsRegistry.getRegistrationManager().getRegistrationGovernor(imsRegistration.getHandle())) != null && registrationGovernor.getP2pListSize(i2) != 0)) {
                return imsRegistration.getHandle();
            } else {
                return -1;
            }
        }
        return -1;
    }

    private boolean setBoundSessionInfo(int i, ImsCallProfile imsCallProfile, CallProfile callProfile) {
        String str = LOG_TAG;
        Log.i(str, "setBoundSessionInfo()");
        Bundle bundle = imsCallProfile.mCallExtras.getBundle("android.telephony.ims.extra.OEM_EXTRAS");
        if (!this.mVolteServiceModule.getCmcServiceHelper().isCmcRegExist(i)) {
            return false;
        }
        if (bundle == null) {
            return true;
        }
        if (bundle.containsKey("com.samsung.telephony.extra.CMC_BOUND_SESSION_ID")) {
            int i2 = bundle.getInt("com.samsung.telephony.extra.CMC_BOUND_SESSION_ID");
            Log.i(str, "setBoundSessionInfo(), boundSessionId: " + i2);
            if (i2 > 0) {
                callProfile.setCmcBoundSessionId(i2);
            }
        }
        if (!bundle.containsKey("com.samsung.telephony.extra.CMC_DIAL_FROM")) {
            return true;
        }
        String string = bundle.getString("com.samsung.telephony.extra.CMC_DIAL_FROM");
        if (TextUtils.isEmpty(string)) {
            return true;
        }
        callProfile.setLetteringText(string);
        return true;
    }

    /* JADX WARNING: Removed duplicated region for block: B:35:0x00d0 A[Catch:{ RemoteException -> 0x0124 }] */
    /* JADX WARNING: Removed duplicated region for block: B:36:0x00d6 A[Catch:{ RemoteException -> 0x0124 }] */
    /* JADX WARNING: Removed duplicated region for block: B:39:0x00df A[Catch:{ RemoteException -> 0x0124 }] */
    /* JADX WARNING: Removed duplicated region for block: B:40:0x00f7 A[Catch:{ RemoteException -> 0x0124 }] */
    /* Code decompiled incorrectly, please refer to instructions dump. */
    public int prepareCallSession(int r7, android.telephony.ims.ImsCallProfile r8, com.sec.ims.volte2.data.CallProfile r9, int r10) throws android.os.RemoteException, java.lang.UnsupportedOperationException {
        /*
            r6 = this;
            r0 = -1
            r6.mCmcRegHandle = r0
            r1 = 1
            r6.mCmcReady = r1
            java.util.Map<java.lang.Integer, com.sec.ims.volte2.IImsCallSession> r2 = r6.mp2pSecSessionMap
            r2.clear()
            boolean r2 = r6.setBoundSessionInfo(r10, r8, r9)     // Catch:{ RemoteException -> 0x0124 }
            java.lang.String r3 = LOG_TAG     // Catch:{ RemoteException -> 0x0124 }
            java.lang.StringBuilder r4 = new java.lang.StringBuilder     // Catch:{ RemoteException -> 0x0124 }
            r4.<init>()     // Catch:{ RemoteException -> 0x0124 }
            java.lang.String r5 = "prepareCallSession(), isCmcRegExist: "
            r4.append(r5)     // Catch:{ RemoteException -> 0x0124 }
            r4.append(r2)     // Catch:{ RemoteException -> 0x0124 }
            java.lang.String r4 = r4.toString()     // Catch:{ RemoteException -> 0x0124 }
            android.util.Log.i(r3, r4)     // Catch:{ RemoteException -> 0x0124 }
            r4 = 0
            if (r2 == 0) goto L_0x011f
            if (r7 != r1) goto L_0x00a4
            com.sec.internal.interfaces.ims.core.IRegistrationManager r8 = com.sec.internal.ims.registry.ImsRegistry.getRegistrationManager()     // Catch:{ RemoteException -> 0x0124 }
            int r8 = r8.getCmcLineSlotIndex()     // Catch:{ RemoteException -> 0x0124 }
            int r1 = r6.getCmcRegHandle(r10, r7)     // Catch:{ RemoteException -> 0x0124 }
            r6.mCmcRegHandle = r1     // Catch:{ RemoteException -> 0x0124 }
            if (r1 != r0) goto L_0x0043
            java.lang.String r1 = "CMC PD is not registered."
            android.util.Log.e(r3, r1)     // Catch:{ RemoteException -> 0x0124 }
            r6.mCmcReady = r4     // Catch:{ RemoteException -> 0x0124 }
            goto L_0x005d
        L_0x0043:
            if (r8 == r10) goto L_0x0057
            com.sec.internal.interfaces.ims.core.ICmcAccountManager r1 = com.sec.internal.ims.registry.ImsRegistry.getCmcAccountManager()     // Catch:{ RemoteException -> 0x0124 }
            boolean r1 = r1.isSupportDualSimCMC()     // Catch:{ RemoteException -> 0x0124 }
            if (r1 != 0) goto L_0x0057
            java.lang.String r1 = "phoneId and cmcLineSlotIndex are not matched"
            android.util.Log.e(r3, r1)     // Catch:{ RemoteException -> 0x0124 }
            r6.mCmcReady = r4     // Catch:{ RemoteException -> 0x0124 }
            goto L_0x005d
        L_0x0057:
            java.lang.String r1 = "prepareCallSession, create session on CMC-PD"
            android.util.Log.i(r3, r1)     // Catch:{ RemoteException -> 0x0124 }
        L_0x005d:
            java.lang.StringBuilder r1 = new java.lang.StringBuilder     // Catch:{ RemoteException -> 0x0124 }
            r1.<init>()     // Catch:{ RemoteException -> 0x0124 }
            java.lang.String r2 = "cmcLineSlotIndex: "
            r1.append(r2)     // Catch:{ RemoteException -> 0x0124 }
            r1.append(r8)     // Catch:{ RemoteException -> 0x0124 }
            java.lang.String r2 = ", phoneId: "
            r1.append(r2)     // Catch:{ RemoteException -> 0x0124 }
            r1.append(r10)     // Catch:{ RemoteException -> 0x0124 }
            java.lang.String r1 = r1.toString()     // Catch:{ RemoteException -> 0x0124 }
            android.util.Log.i(r3, r1)     // Catch:{ RemoteException -> 0x0124 }
            if (r8 != r10) goto L_0x0121
            com.sec.internal.imsphone.cmc.ICmcConnectivityController r8 = r6.mConnectivityController     // Catch:{ RemoteException -> 0x0124 }
            boolean r8 = r8.isEnabledWifiDirectFeature()     // Catch:{ RemoteException -> 0x0124 }
            if (r8 == 0) goto L_0x0085
            r8 = 7
            goto L_0x0086
        L_0x0085:
            r8 = 5
        L_0x0086:
            r1 = 3
        L_0x0087:
            if (r1 > r8) goto L_0x0121
            int r2 = r6.getCmcRegHandle(r10, r1)     // Catch:{ RemoteException -> 0x0124 }
            if (r2 == r0) goto L_0x00a1
            r9.setCmcType(r1)     // Catch:{ RemoteException -> 0x0124 }
            java.util.Map<java.lang.Integer, com.sec.ims.volte2.IImsCallSession> r3 = r6.mp2pSecSessionMap     // Catch:{ RemoteException -> 0x0124 }
            java.lang.Integer r4 = java.lang.Integer.valueOf(r2)     // Catch:{ RemoteException -> 0x0124 }
            com.sec.internal.interfaces.ims.servicemodules.volte2.IVolteServiceModule r5 = r6.mVolteServiceModule     // Catch:{ RemoteException -> 0x0124 }
            com.sec.ims.volte2.IImsCallSession r2 = r5.createSession(r9, r2)     // Catch:{ RemoteException -> 0x0124 }
            r3.put(r4, r2)     // Catch:{ RemoteException -> 0x0124 }
        L_0x00a1:
            int r1 = r1 + 2
            goto L_0x0087
        L_0x00a4:
            com.sec.internal.interfaces.ims.core.ICmcAccountManager r1 = com.sec.internal.ims.registry.ImsRegistry.getCmcAccountManager()     // Catch:{ RemoteException -> 0x0124 }
            boolean r1 = r1.isSecondaryDevice()     // Catch:{ RemoteException -> 0x0124 }
            if (r1 == 0) goto L_0x0121
            java.lang.String r1 = "CallPull"
            boolean r8 = r8.getCallExtraBoolean(r1)     // Catch:{ RemoteException -> 0x0124 }
            r1 = 2
            if (r8 == 0) goto L_0x00b9
        L_0x00b7:
            r7 = r1
            goto L_0x00c8
        L_0x00b9:
            android.content.Context r8 = com.sec.internal.ims.registry.ImsRegistry.getContext()     // Catch:{ RemoteException -> 0x0124 }
            com.sec.internal.helper.os.ITelephonyManager r8 = com.sec.internal.helper.os.TelephonyManagerWrapper.getInstance(r8)     // Catch:{ RemoteException -> 0x0124 }
            boolean r8 = r8.isVoiceCapable()     // Catch:{ RemoteException -> 0x0124 }
            if (r8 != 0) goto L_0x00c8
            goto L_0x00b7
        L_0x00c8:
            int r8 = r6.getCmcRegHandle(r10, r1)     // Catch:{ RemoteException -> 0x0124 }
            r6.mCmcRegHandle = r8     // Catch:{ RemoteException -> 0x0124 }
            if (r8 == r0) goto L_0x00d6
            java.lang.String r8 = "create session on CMC SD"
            android.util.Log.i(r3, r8)     // Catch:{ RemoteException -> 0x0124 }
            goto L_0x00d8
        L_0x00d6:
            r6.mCmcReady = r4     // Catch:{ RemoteException -> 0x0124 }
        L_0x00d8:
            r8 = 4
            int r1 = r6.getCmcRegHandle(r10, r8)     // Catch:{ RemoteException -> 0x0124 }
            if (r1 == r0) goto L_0x00f7
            java.lang.String r10 = "create session on WIFI-AP SD"
            android.util.Log.i(r3, r10)     // Catch:{ RemoteException -> 0x0124 }
            r9.setCmcType(r8)     // Catch:{ RemoteException -> 0x0124 }
            java.util.Map<java.lang.Integer, com.sec.ims.volte2.IImsCallSession> r8 = r6.mp2pSecSessionMap     // Catch:{ RemoteException -> 0x0124 }
            java.lang.Integer r10 = java.lang.Integer.valueOf(r1)     // Catch:{ RemoteException -> 0x0124 }
            com.sec.internal.interfaces.ims.servicemodules.volte2.IVolteServiceModule r6 = r6.mVolteServiceModule     // Catch:{ RemoteException -> 0x0124 }
            com.sec.ims.volte2.IImsCallSession r6 = r6.createSession(r9, r1)     // Catch:{ RemoteException -> 0x0124 }
            r8.put(r10, r6)     // Catch:{ RemoteException -> 0x0124 }
            goto L_0x0121
        L_0x00f7:
            com.sec.internal.imsphone.cmc.ICmcConnectivityController r8 = r6.mConnectivityController     // Catch:{ RemoteException -> 0x0124 }
            boolean r8 = r8.isEnabledWifiDirectFeature()     // Catch:{ RemoteException -> 0x0124 }
            if (r8 == 0) goto L_0x0121
            r8 = 8
            int r10 = r6.getCmcRegHandle(r10, r8)     // Catch:{ RemoteException -> 0x0124 }
            if (r10 == r0) goto L_0x0121
            java.lang.String r0 = "create session on WIFI-DIRECT SD"
            android.util.Log.i(r3, r0)     // Catch:{ RemoteException -> 0x0124 }
            r9.setCmcType(r8)     // Catch:{ RemoteException -> 0x0124 }
            java.util.Map<java.lang.Integer, com.sec.ims.volte2.IImsCallSession> r8 = r6.mp2pSecSessionMap     // Catch:{ RemoteException -> 0x0124 }
            java.lang.Integer r0 = java.lang.Integer.valueOf(r10)     // Catch:{ RemoteException -> 0x0124 }
            com.sec.internal.interfaces.ims.servicemodules.volte2.IVolteServiceModule r6 = r6.mVolteServiceModule     // Catch:{ RemoteException -> 0x0124 }
            com.sec.ims.volte2.IImsCallSession r6 = r6.createSession(r9, r10)     // Catch:{ RemoteException -> 0x0124 }
            r8.put(r0, r6)     // Catch:{ RemoteException -> 0x0124 }
            goto L_0x0121
        L_0x011f:
            r6.mCmcReady = r4     // Catch:{ RemoteException -> 0x0124 }
        L_0x0121:
            r9.setCmcType(r7)     // Catch:{ RemoteException -> 0x0124 }
        L_0x0124:
            return r7
        */
        throw new UnsupportedOperationException("Method not decompiled: com.sec.internal.imsphone.cmc.CmcImsServiceUtil.prepareCallSession(int, android.telephony.ims.ImsCallProfile, com.sec.ims.volte2.data.CallProfile, int):int");
    }

    public com.android.ims.internal.IImsCallSession createCallSession(int i, ImsCallProfile imsCallProfile, CallProfile callProfile) throws RemoteException, UnsupportedOperationException {
        IImsCallSession iImsCallSession;
        CmcCallSessionManager cmcCallSessionManager;
        int i2;
        String str = LOG_TAG;
        Log.i(str, "createCallSession(), cmcType: " + i);
        try {
            Log.i(str, "mCmcRegHandle: " + this.mCmcRegHandle + ", mCmcReady: " + this.mCmcReady);
            StringBuilder sb = new StringBuilder();
            sb.append("mp2pSecSessionMap size: ");
            sb.append(this.mp2pSecSessionMap.size());
            Log.i(str, sb.toString());
            boolean z = false;
            if (ImsRegistry.getCmcAccountManager().isSupportDualSimCMC()) {
                if (ImsCallUtil.isCmcPrimaryType(i)) {
                    i2 = this.mVolteServiceModule.getIncomingSessionPhoneIdForCmc();
                    if (i2 < 0) {
                        i2 = this.mVolteServiceModule.getCmcServiceHelper().getCsCallPhoneIdByState(5);
                    }
                } else {
                    Bundle bundle = imsCallProfile.mCallExtras.getBundle("android.telephony.ims.extra.OEM_EXTRAS");
                    i2 = (bundle == null || !bundle.containsKey("com.samsung.telephony.extra.CMC_EXTERNAL_CALL_SLOT")) ? 0 : bundle.getInt("com.samsung.telephony.extra.CMC_EXTERNAL_CALL_SLOT");
                    String str2 = SemSystemProperties.get("persist.cmc.pref_callslot", "");
                    if (!TextUtils.isEmpty(str2)) {
                        i2 = Integer.parseInt(str2);
                    }
                }
                Log.i(str, "cmcEdCallSlot: " + i2);
                callProfile.setCmcEdCallSlot(i2);
            }
            Iterator<Map.Entry<Integer, IImsCallSession>> it = this.mp2pSecSessionMap.entrySet().iterator();
            while (true) {
                boolean z2 = true;
                if (!it.hasNext()) {
                    break;
                }
                IImsCallSession iImsCallSession2 = (IImsCallSession) it.next().getValue();
                if (imsCallProfile.getCallExtraInt("android.telephony.ims.extra.CALL_NETWORK_TYPE") != 18) {
                    z2 = false;
                }
                iImsCallSession2.setEpdgState(z2);
            }
            boolean z3 = this.mp2pSecSessionMap.size() > 0;
            if (this.mCmcReady) {
                iImsCallSession = this.mVolteServiceModule.createSession(callProfile, this.mCmcRegHandle);
            } else {
                iImsCallSession = (i != 2 || z3) ? null : this.mVolteServiceModule.createSession(callProfile);
            }
            if (!this.mConnectivityController.isEnabledWifiDirectFeature() || this.mCmcReady || z3 || !(i == 1 || i == 2 || i == 7 || i == 8)) {
                if (iImsCallSession == null) {
                    if (!z3) {
                        throw new UnsupportedOperationException();
                    }
                }
                if (iImsCallSession != null) {
                    if (imsCallProfile.getCallExtraInt("android.telephony.ims.extra.CALL_NETWORK_TYPE") == 18) {
                        z = true;
                    }
                    iImsCallSession.setEpdgState(z);
                    cmcCallSessionManager = new CmcCallSessionManager(iImsCallSession, this.mConnectivityController, this.mVolteServiceModule);
                    if (z3) {
                        for (Map.Entry<Integer, IImsCallSession> value : this.mp2pSecSessionMap.entrySet()) {
                            cmcCallSessionManager.addP2pSession((IImsCallSession) value.getValue());
                        }
                    }
                } else {
                    CmcCallSessionManager cmcCallSessionManager2 = null;
                    for (Map.Entry next : this.mp2pSecSessionMap.entrySet()) {
                        if (!z) {
                            cmcCallSessionManager2 = new CmcCallSessionManager((IImsCallSession) next.getValue(), this.mConnectivityController, this.mVolteServiceModule);
                            z = true;
                        } else {
                            cmcCallSessionManager2.addP2pSession((IImsCallSession) next.getValue());
                        }
                    }
                    cmcCallSessionManager = cmcCallSessionManager2;
                }
                Log.i(LOG_TAG, "createCallSession, create imsCallSessionImpl for [CMC+D2D volte call]");
                CmcImsCallSessionImpl cmcImsCallSessionImpl = new CmcImsCallSessionImpl(imsCallProfile, cmcCallSessionManager, (IImsCallSessionListener) null, this.mMmTelFeatureImpl);
                this.mMmTelFeatureImpl.setCallSession(cmcImsCallSessionImpl.getCallIdInt(), cmcImsCallSessionImpl);
                this.mConnectivityController.setP2pCallSessionId(cmcImsCallSessionImpl.getCallIdInt());
                return cmcImsCallSessionImpl;
            }
            if (this.mConnectivityController.getDeviceType() == ICmcConnectivityController.DeviceType.PDevice) {
                Log.i(LOG_TAG, "[P2P] create fake p2pSessionManager in PD");
            } else if (this.mConnectivityController.getDeviceType() == ICmcConnectivityController.DeviceType.SDevice) {
                String str3 = LOG_TAG;
                Log.i(str3, "[P2P] create fake sessionManager in SD ");
                Log.i(str3, "there is no cmc, startCmcP2pConnection!");
            } else {
                Log.e(LOG_TAG, "[P2P] error: please check your P2pSwitchEnabled");
                throw new UnsupportedOperationException();
            }
            this.mConnectivityController.setP2pCallSessionId(999);
            String str4 = LOG_TAG;
            Log.i(str4, "createCallSession, create imsCallSessionImpl for [P2P volte call]");
            CmcCallSessionManager cmcCallSessionManager3 = new CmcCallSessionManager((IImsCallSession) null, this.mConnectivityController, this.mVolteServiceModule);
            CmcImsCallSessionImpl cmcImsCallSessionImpl2 = new CmcImsCallSessionImpl(imsCallProfile, cmcCallSessionManager3, (IImsCallSessionListener) null, this.mMmTelFeatureImpl);
            cmcCallSessionManager3.setReservedCallProfile(callProfile);
            this.mMmTelFeatureImpl.setCallSession(999, cmcImsCallSessionImpl2);
            Log.i(str4, "createCallSession, need timeout to wait p2p registration between PD and SD.");
            return cmcImsCallSessionImpl2;
        } catch (RemoteException unused) {
            throw new UnsupportedOperationException();
        }
    }

    public void createP2pCallSession() throws RemoteException {
        boolean z;
        String str = LOG_TAG;
        Log.i(str, "createP2pCallSession()");
        boolean z2 = false;
        this.mConnectivityController.needP2pCallSession(false);
        int p2pCallSessionId = this.mConnectivityController.getP2pCallSessionId();
        Log.i(str, "p2pSessionId: " + p2pCallSessionId);
        if (p2pCallSessionId == -1) {
            Log.e(str, "sub(wifi-direct) session is already created, just return");
            return;
        }
        ImsCallSessionImpl callSession = this.mMmTelFeatureImpl.getCallSession(p2pCallSessionId);
        this.mConnectivityController.setP2pCallSessionId(-1);
        IVolteServiceModule iVolteServiceModule = this.mVolteServiceModule;
        if (iVolteServiceModule != null) {
            IImsCallSession foregroundSession = iVolteServiceModule.getForegroundSession();
            if (foregroundSession != null) {
                Log.e(str, "foreSession.getCmcType(): " + foregroundSession.getCmcType());
                Log.e(str, "pdcall is already connected. don't create subcallsession, just return");
                return;
            } else if (this.mVolteServiceModule.getExtMoCall()) {
                Log.e(str, "the call is MOcall. don't create subcallsession, just return");
                return;
            }
        }
        if (this.mConnectivityController.getP2pDeviceType() == ICmcConnectivityController.DeviceType.None) {
            Log.e(str, "Not support p2p");
        } else if (callSession == null) {
            Log.e(str, "sessionImpl is null");
        } else {
            ImsCallProfile callProfile = callSession.getCallProfile();
            CallProfile convertToSecCallProfile = DataTypeConvertor.convertToSecCallProfile(SimUtil.getActiveDataPhoneId(), callSession.getCallProfile(), false);
            Bundle bundle = callProfile.mCallExtras.getBundle("android.telephony.ims.extra.OEM_EXTRAS");
            if (bundle != null) {
                if (bundle.containsKey("com.samsung.telephony.extra.CMC_BOUND_SESSION_ID")) {
                    int i = bundle.getInt("com.samsung.telephony.extra.CMC_BOUND_SESSION_ID");
                    Log.e(str, "boundSessionId: " + i);
                    if (i > 0) {
                        convertToSecCallProfile.setCmcBoundSessionId(i);
                    }
                }
                if (bundle.containsKey("com.samsung.telephony.extra.CMC_DIAL_FROM")) {
                    String string = bundle.getString("com.samsung.telephony.extra.CMC_DIAL_FROM");
                    if (!TextUtils.isEmpty(string)) {
                        convertToSecCallProfile.setLetteringText(string);
                    }
                }
            }
            ConcurrentHashMap concurrentHashMap = new ConcurrentHashMap();
            if (this.mVolteServiceModule != null) {
                ICmcConnectivityController.DeviceType p2pDeviceType = this.mConnectivityController.getP2pDeviceType();
                if (p2pDeviceType == ICmcConnectivityController.DeviceType.PDevice) {
                    int cmcRegHandle = getCmcRegHandle(convertToSecCallProfile.getPhoneId(), 7);
                    if (cmcRegHandle != -1) {
                        Log.i(str, "create session on P2P-SD");
                        concurrentHashMap.put(Integer.valueOf(cmcRegHandle), this.mVolteServiceModule.createSession(convertToSecCallProfile, cmcRegHandle));
                    } else {
                        Log.i(str, "not found P2P-SD");
                    }
                } else if (p2pDeviceType == ICmcConnectivityController.DeviceType.SDevice) {
                    int cmcRegHandle2 = getCmcRegHandle(convertToSecCallProfile.getPhoneId(), 8);
                    if (cmcRegHandle2 != -1) {
                        Log.i(str, "create session on P2P-SD");
                        concurrentHashMap.put(Integer.valueOf(cmcRegHandle2), this.mVolteServiceModule.createSession(convertToSecCallProfile, cmcRegHandle2));
                    } else {
                        Log.i(str, "not found P2P-SD");
                    }
                } else {
                    Log.e(str, "not found P2P device, exception case");
                }
            }
            Log.i(str, "p2pSecSessionMap size: " + concurrentHashMap.size());
            Iterator it = concurrentHashMap.entrySet().iterator();
            while (true) {
                boolean z3 = true;
                if (!it.hasNext()) {
                    break;
                }
                IImsCallSession iImsCallSession = (IImsCallSession) ((Map.Entry) it.next()).getValue();
                if (callProfile.getCallExtraInt("android.telephony.ims.extra.CALL_NETWORK_TYPE") != 18) {
                    z3 = false;
                }
                iImsCallSession.setEpdgState(z3);
            }
            if (concurrentHashMap.size() > 0) {
                int i2 = 1;
                while (true) {
                    if (i2 > 5) {
                        z = false;
                        break;
                    } else if (getCmcRegHandle(convertToSecCallProfile.getPhoneId(), i2) != -1) {
                        z = true;
                        break;
                    } else {
                        i2 += 2;
                    }
                }
                Log.i(LOG_TAG, "existMainSession: " + z);
                CmcCallSessionManager cmcCallSessionManager = callSession.getCmcCallSessionManager();
                for (Map.Entry entry : concurrentHashMap.entrySet()) {
                    if (z2) {
                        cmcCallSessionManager.addP2pSession((IImsCallSession) entry.getValue());
                    } else if (!z) {
                        cmcCallSessionManager.setMainSession((IImsCallSession) entry.getValue());
                        z2 = true;
                    } else {
                        cmcCallSessionManager.addP2pSession((IImsCallSession) entry.getValue());
                    }
                }
                Log.i(LOG_TAG, "mSubSessionList size: " + cmcCallSessionManager.getP2pSessionSize());
                callSession.initP2pImpl();
                this.mMmTelFeatureImpl.setCallSession(callSession.getCallIdInt(), callSession);
                cmcCallSessionManager.startP2pSessions(z2);
            }
        }
    }

    private int getCmcPhoneId(IImsCallSession iImsCallSession) throws RemoteException {
        int cmcLineSlotIndex;
        if (ImsRegistry.getCmcAccountManager().isSupportDualSimCMC()) {
            cmcLineSlotIndex = iImsCallSession.getCallProfile().getCmcEdCallSlot();
            if (cmcLineSlotIndex == -1) {
                return 0;
            }
        } else {
            cmcLineSlotIndex = ImsRegistry.getRegistrationManager().getCmcLineSlotIndex();
            if (cmcLineSlotIndex == -1) {
                return 0;
            }
        }
        return cmcLineSlotIndex;
    }

    public void getPendingCallSession(int i, ImsCallProfile imsCallProfile, IImsCallSession iImsCallSession) throws RemoteException {
        String str = LOG_TAG;
        Log.i(str, "getPendingCallSession()");
        int i2 = 1;
        if (this.mVolteServiceModule.getCmcServiceHelper().isCmcRegExist(i)) {
            Bundle bundle = new Bundle();
            int cmcType = iImsCallSession.getCmcType();
            int sessionId = iImsCallSession.getSessionId();
            if (!ImsCallUtil.isCmcPrimaryType(cmcType)) {
                i2 = ImsCallUtil.isCmcSecondaryType(cmcType) ? 2 : cmcType;
            }
            Log.i(str, "getPendingCallSession(), SEM_EXTRA_CMC_TYPE: (" + iImsCallSession.getCmcType() + " -> " + i2 + ")");
            bundle.putInt("com.samsung.telephony.extra.CMC_SESSION_ID", sessionId);
            bundle.putInt("com.samsung.telephony.extra.CMC_TYPE", i2);
            if (ImsCallUtil.isCmcPrimaryType(i2)) {
                bundle.putInt("com.samsung.telephony.extra.CMC_PHONE_ID", getCmcPhoneId(iImsCallSession));
            } else if (ImsCallUtil.isCmcSecondaryType(i2) && ImsRegistry.getCmcAccountManager().isSupportDualSimCMC()) {
                Log.i(str, "put CMC_EXTERNAL_CALL_SLOT: " + iImsCallSession.getCallProfile().getCmcEdCallSlot());
                bundle.putInt("com.samsung.telephony.extra.CMC_EXTERNAL_CALL_SLOT", iImsCallSession.getCallProfile().getCmcEdCallSlot());
            }
            imsCallProfile.mCallExtras.putBundle("android.telephony.ims.extra.OEM_EXTRAS", bundle);
        } else if (this.mConnectivityController.isEnabledWifiDirectFeature() && this.mConnectivityController.getP2pDeviceType() == ICmcConnectivityController.DeviceType.PDevice) {
            Bundle bundle2 = new Bundle();
            int sessionId2 = iImsCallSession.getSessionId();
            Log.i(str, "getPendingCallSession(), SEM_EXTRA_CMC_TYPE: (" + iImsCallSession.getCmcType() + " -> " + 1 + ")");
            bundle2.putInt("com.samsung.telephony.extra.CMC_SESSION_ID", sessionId2);
            bundle2.putInt("com.samsung.telephony.extra.CMC_TYPE", 1);
            imsCallProfile.mCallExtras.putBundle("android.telephony.ims.extra.OEM_EXTRAS", bundle2);
        }
    }

    public void sendPublishDialog(int i, PublishDialog publishDialog) throws RemoteException {
        int i2 = this.mConnectivityController.isEnabledWifiDirectFeature() ? 7 : 5;
        String str = LOG_TAG;
        Log.i(str, "sendPublishDialog() callCnt: " + publishDialog.getCallCount());
        for (int i3 = 1; i3 <= i2; i3 += 2) {
            this.mVolteServiceModule.getCmcServiceHelper().sendPublishDialog(i, publishDialog, i3);
        }
    }

    public void postProcessForCmcIncomingCall(int i, Intent intent, IImsCallSession iImsCallSession) {
        try {
            if (this.mVolteServiceModule.getCmcServiceHelper().isCmcRegExist(i)) {
                CallProfile callProfile = iImsCallSession.getCallProfile();
                int cmcType = iImsCallSession.getCmcType();
                int sessionId = iImsCallSession.getSessionId();
                if (ImsCallUtil.isCmcPrimaryType(cmcType)) {
                    cmcType = 1;
                } else if (ImsCallUtil.isCmcSecondaryType(cmcType)) {
                    cmcType = 2;
                }
                String str = LOG_TAG;
                Log.i(str, "postProcessForCmcIncomingCall(), SEM_EXTRA_CMC_TYPE: (" + iImsCallSession.getCmcType() + " -> " + cmcType + ")");
                intent.putExtra("com.samsung.telephony.extra.CMC_TYPE", cmcType);
                intent.putExtra("com.samsung.telephony.extra.CMC_SESSION_ID", sessionId);
                if (cmcType == 1) {
                    intent.putExtra("com.samsung.telephony.extra.CMC_DIAL_TO", callProfile.getDialingNumber());
                    if (!TextUtils.isEmpty(callProfile.getReplaceSipCallId())) {
                        intent.putExtra("com.samsung.telephony.extra.CMC_REPLACE_CALL_ID", callProfile.getReplaceSipCallId());
                        intent.putExtra("com.samsung.telephony.extra.CMC_DEVICE_ID_BY_SD", callProfile.getCmcDeviceId());
                    } else if (!TextUtils.isEmpty(callProfile.getCmcDeviceId())) {
                        intent.putExtra("com.samsung.telephony.extra.CMC_DEVICE_ID", callProfile.getCmcDeviceId());
                    }
                    intent.putExtra("com.samsung.telephony.extra.CMC_PHONE_ID", getCmcPhoneId(iImsCallSession));
                } else if (ImsRegistry.getCmcAccountManager().isSupportDualSimCMC()) {
                    Log.i(str, "postProcessForCmcIncomingCall(), cmcEdCallSlot:" + callProfile.getCmcEdCallSlot());
                    intent.putExtra("com.samsung.telephony.extra.CMC_EXTERNAL_CALL_SLOT", callProfile.getCmcEdCallSlot());
                }
            }
            if (this.mConnectivityController.isEnabledWifiDirectFeature() && iImsCallSession.getCmcType() == 0 && this.mConnectivityController.getP2pDeviceType() == ICmcConnectivityController.DeviceType.None && getCmcRegHandle(i, 7) == -1) {
                Log.e(LOG_TAG, "onIncomingCall: need wifi-direct connection, startCmcP2pConnection!");
                this.mConnectivityController.setP2pPD();
                this.mConnectivityController.setCmcActivation(true);
            }
        } catch (RemoteException e) {
            e.printStackTrace();
        }
    }
}
