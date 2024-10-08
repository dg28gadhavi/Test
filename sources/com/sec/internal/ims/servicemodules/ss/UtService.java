package com.sec.internal.ims.servicemodules.ss;

import android.content.Context;
import android.os.RemoteException;
import com.sec.ims.ss.IImsUtEventListener;
import com.sec.ims.ss.IUtService;
import com.sec.internal.ims.servicemodules.base.ServiceModuleBase;

public class UtService extends IUtService.Stub {
    private static final String LOG_TAG = UtService.class.getSimpleName();
    private static final String PERMISSION = "com.sec.imsservice.PERMISSION";
    private Context mContext = null;
    private UtServiceModule mServiceModule = null;

    public UtService(ServiceModuleBase serviceModuleBase) {
        UtServiceModule utServiceModule = (UtServiceModule) serviceModuleBase;
        this.mServiceModule = utServiceModule;
        this.mContext = utServiceModule.getContext();
    }

    public void registerForUtEvent(int i, IImsUtEventListener iImsUtEventListener) throws RemoteException {
        this.mContext.enforceCallingOrSelfPermission(PERMISSION, LOG_TAG);
        this.mServiceModule.registerForUtEvent(i, iImsUtEventListener);
    }

    public void deRegisterForUtEvent(int i, IImsUtEventListener iImsUtEventListener) throws RemoteException {
        this.mContext.enforceCallingOrSelfPermission(PERMISSION, LOG_TAG);
        this.mServiceModule.deRegisterForUtEvent(i, iImsUtEventListener);
    }

    public int queryCallBarring(int i, int i2, int i3) throws RemoteException {
        this.mContext.enforceCallingOrSelfPermission(PERMISSION, LOG_TAG);
        return this.mServiceModule.queryCallBarring(i, i2, i3);
    }

    public int queryCallForward(int i, int i2, String str) throws RemoteException {
        this.mContext.enforceCallingOrSelfPermission(PERMISSION, LOG_TAG);
        return this.mServiceModule.queryCallForward(i, i2, str);
    }

    public int queryCallWaiting(int i) throws RemoteException {
        this.mContext.enforceCallingOrSelfPermission(PERMISSION, LOG_TAG);
        return this.mServiceModule.queryCallWaiting(i);
    }

    public int queryCLIR(int i) throws RemoteException {
        this.mContext.enforceCallingOrSelfPermission(PERMISSION, LOG_TAG);
        return this.mServiceModule.queryCLIR(i);
    }

    public int queryCLIP(int i) throws RemoteException {
        this.mContext.enforceCallingOrSelfPermission(PERMISSION, LOG_TAG);
        return this.mServiceModule.queryCLIP(i);
    }

    public int queryCOLR(int i) throws RemoteException {
        this.mContext.enforceCallingOrSelfPermission(PERMISSION, LOG_TAG);
        return this.mServiceModule.queryCOLR(i);
    }

    public int queryCOLP(int i) throws RemoteException {
        this.mContext.enforceCallingOrSelfPermission(PERMISSION, LOG_TAG);
        return this.mServiceModule.queryCOLP(i);
    }

    public int updateCallBarring(int i, int i2, int i3, int i4, String str, String[] strArr) throws RemoteException {
        this.mContext.enforceCallingOrSelfPermission(PERMISSION, LOG_TAG);
        return this.mServiceModule.updateCallBarring(i, i2, i3, i4, str, strArr);
    }

    public int updateCallForward(int i, int i2, int i3, String str, int i4, int i5) throws RemoteException {
        this.mContext.enforceCallingOrSelfPermission(PERMISSION, LOG_TAG);
        return this.mServiceModule.updateCallForward(i, i2, i3, str, i4, i5);
    }

    public int updateCallWaiting(int i, boolean z, int i2) throws RemoteException {
        this.mContext.enforceCallingOrSelfPermission(PERMISSION, LOG_TAG);
        return this.mServiceModule.updateCallWaiting(i, z, i2);
    }

    public int updateCLIR(int i, int i2) throws RemoteException {
        this.mContext.enforceCallingOrSelfPermission(PERMISSION, LOG_TAG);
        return this.mServiceModule.updateCLIR(i, i2);
    }

    public int updateCLIP(int i, boolean z) throws RemoteException {
        this.mContext.enforceCallingOrSelfPermission(PERMISSION, LOG_TAG);
        return this.mServiceModule.updateCLIP(i, z);
    }

    public int updateCOLR(int i, int i2) throws RemoteException {
        this.mContext.enforceCallingOrSelfPermission(PERMISSION, LOG_TAG);
        return this.mServiceModule.updateCOLR(i, i2);
    }

    public int updateCOLP(int i, boolean z) throws RemoteException {
        this.mContext.enforceCallingOrSelfPermission(PERMISSION, LOG_TAG);
        return this.mServiceModule.updateCOLP(i, z);
    }

    public boolean isUtEnabled(int i) throws RemoteException {
        this.mContext.enforceCallingOrSelfPermission(PERMISSION, LOG_TAG);
        return this.mServiceModule.isUtEnabled(i);
    }
}
