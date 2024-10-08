package com.sec.internal.ims.servicemodules.openapi;

import android.os.RemoteException;
import com.sec.ims.IImsRegistrationListener;
import com.sec.ims.openapi.IImsStatusService;
import com.sec.ims.volte2.IImsCallEventListener;
import com.sec.internal.ims.servicemodules.base.ServiceModuleBase;

public class ImsStatusService extends IImsStatusService.Stub {
    ImsStatusServiceModule mModule;

    public ImsStatusService(ServiceModuleBase serviceModuleBase) {
        this.mModule = (ImsStatusServiceModule) serviceModuleBase;
    }

    public void registerImsCallEventListener(IImsCallEventListener iImsCallEventListener) throws RemoteException {
        this.mModule.registerImsCallEventListener(iImsCallEventListener);
    }

    public void unregisterImsCallEventListener(IImsCallEventListener iImsCallEventListener) throws RemoteException {
        this.mModule.unregisterImsCallEventListener(iImsCallEventListener);
    }

    public void registerImsRegistrationListener(IImsRegistrationListener iImsRegistrationListener) throws RemoteException {
        this.mModule.registerImsRegistrationListener(iImsRegistrationListener);
    }

    public void unregisterImsRegistrationListener(IImsRegistrationListener iImsRegistrationListener) throws RemoteException {
        this.mModule.unregisterImsRegistrationListener(iImsRegistrationListener);
    }

    public int[] getCallCount() throws RemoteException {
        return this.mModule.getCallCount();
    }
}
