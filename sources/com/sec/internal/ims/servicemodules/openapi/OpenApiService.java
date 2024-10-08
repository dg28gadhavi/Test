package com.sec.internal.ims.servicemodules.openapi;

import android.os.RemoteException;
import com.sec.ims.IDialogEventListener;
import com.sec.ims.IImsRegistrationListener;
import com.sec.ims.openapi.IOpenApiService;
import com.sec.ims.openapi.ISipDialogListener;
import com.sec.ims.util.ImsUri;
import com.sec.ims.volte2.IImsCallEventListener;
import com.sec.internal.ims.servicemodules.base.ServiceModuleBase;

public class OpenApiService extends IOpenApiService.Stub {
    OpenApiServiceModule mModule;

    public OpenApiService(ServiceModuleBase serviceModuleBase) {
        this.mModule = (OpenApiServiceModule) serviceModuleBase;
    }

    public void setFeatureTags(String[] strArr) {
        this.mModule.setFeatureTags(strArr);
    }

    public void registerIncomingSipMessageListener(ISipDialogListener iSipDialogListener) {
        this.mModule.registerIncomingSipMessageListener(iSipDialogListener);
    }

    public void unregisterIncomingSipMessageListener(ISipDialogListener iSipDialogListener) {
        this.mModule.unregisterIncomingSipMessageListener(iSipDialogListener);
    }

    public void registerImsCallEventListener(IImsCallEventListener iImsCallEventListener) throws RemoteException {
        this.mModule.registerImsCallEventListener(iImsCallEventListener);
    }

    public void unregisterImsCallEventListener(IImsCallEventListener iImsCallEventListener) throws RemoteException {
        this.mModule.unregisterImsCallEventListener(iImsCallEventListener);
    }

    public void registerDialogEventListener(IDialogEventListener iDialogEventListener) throws RemoteException {
        this.mModule.registerDialogEventListener(iDialogEventListener);
    }

    public void unregisterDialogEventListener(IDialogEventListener iDialogEventListener) throws RemoteException {
        this.mModule.unregisterDialogEventListener(iDialogEventListener);
    }

    public void registerImsRegistrationListener(IImsRegistrationListener iImsRegistrationListener) throws RemoteException {
        this.mModule.registerImsRegistrationListener(iImsRegistrationListener);
    }

    public void unregisterImsRegistrationListener(IImsRegistrationListener iImsRegistrationListener) throws RemoteException {
        this.mModule.unregisterImsRegistrationListener(iImsRegistrationListener);
    }

    public boolean sendSip(ImsUri imsUri, String str, ISipDialogListener iSipDialogListener) {
        return this.mModule.sendSip(str, iSipDialogListener);
    }

    public void setupMediaPath(String[] strArr) {
        this.mModule.setupMediaPath(strArr);
    }
}
