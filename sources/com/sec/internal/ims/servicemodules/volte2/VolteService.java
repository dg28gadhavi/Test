package com.sec.internal.ims.servicemodules.volte2;

import android.content.Context;
import android.os.RemoteException;
import com.sec.ims.IImsRegistrationListener;
import com.sec.ims.IRttEventListener;
import com.sec.ims.ImsRegistration;
import com.sec.ims.volte2.IImsCallEventListener;
import com.sec.ims.volte2.IImsCallSession;
import com.sec.ims.volte2.IVolteService;
import com.sec.ims.volte2.IVolteServiceEventListener;
import com.sec.ims.volte2.data.CallProfile;
import com.sec.ims.volte2.data.ImsCallInfo;
import com.sec.internal.ims.registry.ImsRegistry;
import com.sec.internal.ims.servicemodules.base.ServiceModuleBase;
import java.util.HashMap;
import java.util.Map;

public class VolteService extends IVolteService.Stub {
    private static final String LOG_TAG = VolteService.class.getSimpleName();
    private static final String PERMISSION = "com.sec.imsservice.PERMISSION";
    protected Context mContext = null;
    protected VolteServiceModule mServiceModule = null;

    public VolteService(ServiceModuleBase serviceModuleBase) {
        VolteServiceModule volteServiceModule = (VolteServiceModule) serviceModuleBase;
        this.mServiceModule = volteServiceModule;
        this.mContext = volteServiceModule.getContext();
    }

    public CallProfile createCallProfile(int i, int i2) throws RemoteException {
        this.mContext.enforceCallingOrSelfPermission(PERMISSION, LOG_TAG);
        return this.mServiceModule.createCallProfile(i, i2);
    }

    public ImsCallSession createSession(CallProfile callProfile) throws RemoteException {
        this.mContext.enforceCallingOrSelfPermission(PERMISSION, LOG_TAG);
        return this.mServiceModule.createSession(callProfile);
    }

    public ImsCallSession createSessionWithRegId(CallProfile callProfile, int i) throws RemoteException {
        this.mContext.enforceCallingOrSelfPermission(PERMISSION, LOG_TAG);
        return this.mServiceModule.createSession(callProfile, i);
    }

    public ImsCallSession getPendingSession(String str) throws RemoteException {
        this.mContext.enforceCallingOrSelfPermission(PERMISSION, LOG_TAG);
        return this.mServiceModule.getPendingSession(str);
    }

    public ImsCallSession getSession(int i) throws RemoteException {
        this.mContext.enforceCallingOrSelfPermission(PERMISSION, LOG_TAG);
        return this.mServiceModule.getSessionByCallId(i);
    }

    public void registerForVolteServiceEvent(int i, IVolteServiceEventListener iVolteServiceEventListener) throws RemoteException {
        this.mContext.enforceCallingOrSelfPermission(PERMISSION, LOG_TAG);
        this.mServiceModule.registerForVolteServiceEvent(i, iVolteServiceEventListener);
    }

    public void deRegisterForVolteServiceEvent(int i, IVolteServiceEventListener iVolteServiceEventListener) throws RemoteException {
        this.mContext.enforceCallingOrSelfPermission(PERMISSION, LOG_TAG);
        this.mServiceModule.deRegisterForVolteServiceEvent(i, iVolteServiceEventListener);
    }

    public void registerForCallStateEvent(IImsCallEventListener iImsCallEventListener) throws RemoteException {
        if (isPermissionGranted()) {
            this.mServiceModule.registerForCallStateEvent(iImsCallEventListener);
            return;
        }
        throw new SecurityException(LOG_TAG + " Permission denied");
    }

    public void deregisterForCallStateEvent(IImsCallEventListener iImsCallEventListener) throws RemoteException {
        if (isPermissionGranted()) {
            this.mServiceModule.deregisterForCallStateEvent(iImsCallEventListener);
            return;
        }
        throw new SecurityException(LOG_TAG + " Permission denied");
    }

    public void registerForCallStateEventForSlot(int i, IImsCallEventListener iImsCallEventListener) throws RemoteException {
        if (isPermissionGranted()) {
            this.mServiceModule.registerForCallStateEvent(i, iImsCallEventListener);
            return;
        }
        throw new SecurityException(LOG_TAG + " Permission denied");
    }

    public void deregisterForCallStateEventForSlot(int i, IImsCallEventListener iImsCallEventListener) throws RemoteException {
        if (isPermissionGranted()) {
            this.mServiceModule.deregisterForCallStateEvent(i, iImsCallEventListener);
            return;
        }
        throw new SecurityException(LOG_TAG + " Permission denied");
    }

    public void setTtyMode(int i) throws RemoteException {
        this.mContext.enforceCallingOrSelfPermission(PERMISSION, LOG_TAG);
        this.mServiceModule.setTtyMode(i);
    }

    public void enableCallWaitingRule(boolean z) throws RemoteException {
        this.mContext.enforceCallingOrSelfPermission(PERMISSION, LOG_TAG);
        this.mServiceModule.enableCallWaitingRule(z);
    }

    public void notifyProgressIncomingCall(int i, Map map) throws RemoteException {
        this.mContext.enforceCallingOrSelfPermission(PERMISSION, LOG_TAG);
        if (map instanceof HashMap) {
            this.mServiceModule.notifyProgressIncomingCall(i, (HashMap) map);
        }
    }

    public int[] getCallCount() throws RemoteException {
        if (isPermissionGranted()) {
            return new int[]{this.mServiceModule.getTotalCallCount(-1), this.mServiceModule.getVideoCallCount(-1), this.mServiceModule.getDowngradedCallCount(-1), this.mServiceModule.getE911CallCount(-1)};
        }
        throw new SecurityException(LOG_TAG + " Permission denied");
    }

    public int getRttMode() throws RemoteException {
        this.mContext.enforceCallingOrSelfPermission(PERMISSION, LOG_TAG);
        return this.mServiceModule.getRttMode();
    }

    public void setAutomaticMode(int i, boolean z) throws RemoteException {
        this.mContext.enforceCallingOrSelfPermission(PERMISSION, LOG_TAG);
        this.mServiceModule.setAutomaticMode(i, z);
    }

    public void sendRttSessionModifyResponse(int i, boolean z) throws RemoteException {
        this.mContext.enforceCallingOrSelfPermission(PERMISSION, LOG_TAG);
        this.mServiceModule.sendRttSessionModifyResponse(i, z);
    }

    public void sendRttSessionModifyRequest(int i, boolean z) throws RemoteException {
        this.mContext.enforceCallingOrSelfPermission(PERMISSION, LOG_TAG);
        this.mServiceModule.sendRttSessionModifyRequest(i, z);
    }

    public void registerRttEventListener(int i, IRttEventListener iRttEventListener) throws RemoteException {
        this.mContext.enforceCallingOrSelfPermission(PERMISSION, LOG_TAG);
        this.mServiceModule.registerRttEventListener(i, iRttEventListener);
    }

    public void unregisterRttEventListener(int i, IRttEventListener iRttEventListener) throws RemoteException {
        this.mContext.enforceCallingOrSelfPermission(PERMISSION, LOG_TAG);
        this.mServiceModule.unregisterRttEventListener(i, iRttEventListener);
    }

    public int getParticipantIdForMerge(int i, int i2) throws RemoteException {
        this.mContext.enforceCallingOrSelfPermission(PERMISSION, LOG_TAG);
        return this.mServiceModule.getParticipantIdForMerge(i, i2);
    }

    public IImsCallSession getSessionByCallId(int i) throws RemoteException {
        this.mContext.enforceCallingOrSelfPermission(PERMISSION, LOG_TAG);
        return this.mServiceModule.getSessionByCallId(i);
    }

    public void registerImsRegistrationListener(IImsRegistrationListener iImsRegistrationListener, boolean z, int i) throws RemoteException {
        ImsRegistry.registerImsRegistrationListener(iImsRegistrationListener, z, i);
    }

    public void unregisterImsRegistrationListener(IImsRegistrationListener iImsRegistrationListener) throws RemoteException {
        ImsRegistry.unregisterImsRegistrationListener(iImsRegistrationListener);
    }

    public ImsRegistration[] getRegistrationInfoByPhoneId(int i) throws RemoteException {
        return ImsRegistry.getRegistrationInfoByPhoneId(i);
    }

    public int getNetworkType(int i) throws RemoteException {
        return ImsRegistry.getNetworkType(i);
    }

    public String updateEccUrn(int i, String str) throws RemoteException {
        this.mContext.enforceCallingOrSelfPermission(PERMISSION, LOG_TAG);
        return this.mServiceModule.updateEccUrn(i, str);
    }

    private boolean isPermissionGranted() {
        return this.mContext.checkCallingOrSelfPermission("android.permission.READ_PHONE_STATE") == 0 || this.mContext.checkCallingOrSelfPermission(PERMISSION) == 0;
    }

    public void changeAudioPath(int i, int i2) throws RemoteException {
        this.mContext.enforceCallingOrSelfPermission(PERMISSION, LOG_TAG);
        this.mServiceModule.updateAudioInterface(i, i2);
    }

    public int startLocalRingBackTone(int i, int i2, int i3) throws RemoteException {
        this.mContext.enforceCallingOrSelfPermission(PERMISSION, LOG_TAG);
        return this.mServiceModule.startLocalRingBackTone(i, i2, i3);
    }

    public int stopLocalRingBackTone() throws RemoteException {
        this.mContext.enforceCallingOrSelfPermission(PERMISSION, LOG_TAG);
        return this.mServiceModule.stopLocalRingBackTone();
    }

    public String getTrn(String str, String str2) throws RemoteException {
        this.mContext.enforceCallingOrSelfPermission(PERMISSION, LOG_TAG);
        return this.mServiceModule.getTrn(str, str2);
    }

    public ImsCallInfo[] getImsCallInfos(int i) throws RemoteException {
        this.mContext.enforceCallingOrSelfPermission(PERMISSION, LOG_TAG);
        return this.mServiceModule.getImsCallInfos(i);
    }
}
