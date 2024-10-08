package com.sec.internal.imsphone;

import android.os.Bundle;
import android.os.RemoteException;
import com.android.ims.internal.IImsUt;
import com.android.ims.internal.IImsUtListener;
import com.sec.ims.ss.IImsUtEventListener;
import com.sec.internal.ims.registry.ImsRegistry;
import com.sec.internal.interfaces.ims.servicemodules.ss.IUtServiceModule;

public class ImsUtImpl extends IImsUt.Stub {
    protected IImsUtListener mListener = null;
    private int mPhoneId;
    private IImsUtEventListener mUtEventListener = null;
    private IUtServiceModule mUtService = null;

    public void close() throws RemoteException {
    }

    public int transact(Bundle bundle) throws RemoteException {
        return -1;
    }

    public ImsUtImpl(int i) {
        this.mPhoneId = i;
        this.mUtService = ImsRegistry.getServiceModuleManager().getUtServiceModule();
        ImsUtEventListener imsUtEventListener = new ImsUtEventListener(this);
        this.mUtEventListener = imsUtEventListener;
        IUtServiceModule iUtServiceModule = this.mUtService;
        if (iUtServiceModule != null) {
            iUtServiceModule.registerForUtEvent(this.mPhoneId, imsUtEventListener);
        }
    }

    public int queryCallBarring(int i) throws RemoteException {
        IUtServiceModule iUtServiceModule = this.mUtService;
        if (iUtServiceModule == null) {
            return -1;
        }
        return iUtServiceModule.queryCallBarring(this.mPhoneId, i, 255);
    }

    public int queryCallForward(int i, String str) throws RemoteException {
        IUtServiceModule iUtServiceModule = this.mUtService;
        if (iUtServiceModule == null) {
            return -1;
        }
        return iUtServiceModule.queryCallForward(this.mPhoneId, i, str);
    }

    public int queryCallWaiting() throws RemoteException {
        IUtServiceModule iUtServiceModule = this.mUtService;
        if (iUtServiceModule == null) {
            return -1;
        }
        return iUtServiceModule.queryCallWaiting(this.mPhoneId);
    }

    public int queryCLIR() throws RemoteException {
        IUtServiceModule iUtServiceModule = this.mUtService;
        if (iUtServiceModule == null) {
            return -1;
        }
        return iUtServiceModule.queryCLIR(this.mPhoneId);
    }

    public int queryCLIP() throws RemoteException {
        IUtServiceModule iUtServiceModule = this.mUtService;
        if (iUtServiceModule == null) {
            return -1;
        }
        return iUtServiceModule.queryCLIP(this.mPhoneId);
    }

    public int queryCOLR() throws RemoteException {
        IUtServiceModule iUtServiceModule = this.mUtService;
        if (iUtServiceModule == null) {
            return -1;
        }
        return iUtServiceModule.queryCOLR(this.mPhoneId);
    }

    public int queryCOLP() throws RemoteException {
        IUtServiceModule iUtServiceModule = this.mUtService;
        if (iUtServiceModule == null) {
            return -1;
        }
        return iUtServiceModule.queryCOLP(this.mPhoneId);
    }

    public int updateCallBarring(int i, int i2, String[] strArr) throws RemoteException {
        IUtServiceModule iUtServiceModule = this.mUtService;
        if (iUtServiceModule == null) {
            return -1;
        }
        return iUtServiceModule.updateCallBarring(this.mPhoneId, i, i2, 255, (String) null, strArr);
    }

    public int updateCallBarringWithPassword(int i, int i2, String[] strArr, int i3, String str) throws RemoteException {
        IUtServiceModule iUtServiceModule = this.mUtService;
        if (iUtServiceModule == null) {
            return -1;
        }
        return iUtServiceModule.updateCallBarring(this.mPhoneId, i, i2, i3, str, strArr);
    }

    public int updateCallForward(int i, int i2, String str, int i3, int i4) throws RemoteException {
        IUtServiceModule iUtServiceModule = this.mUtService;
        if (iUtServiceModule == null) {
            return -1;
        }
        return iUtServiceModule.updateCallForward(this.mPhoneId, i, i2, str, i3, i4);
    }

    public int updateCallWaiting(boolean z, int i) throws RemoteException {
        IUtServiceModule iUtServiceModule = this.mUtService;
        if (iUtServiceModule == null) {
            return -1;
        }
        return iUtServiceModule.updateCallWaiting(this.mPhoneId, z, i);
    }

    public int updateCLIR(int i) throws RemoteException {
        IUtServiceModule iUtServiceModule = this.mUtService;
        if (iUtServiceModule == null) {
            return -1;
        }
        return iUtServiceModule.updateCLIR(this.mPhoneId, i);
    }

    public int updateCLIP(boolean z) throws RemoteException {
        IUtServiceModule iUtServiceModule = this.mUtService;
        if (iUtServiceModule == null) {
            return -1;
        }
        return iUtServiceModule.updateCLIP(this.mPhoneId, z);
    }

    public int updateCOLR(int i) throws RemoteException {
        IUtServiceModule iUtServiceModule = this.mUtService;
        if (iUtServiceModule == null) {
            return -1;
        }
        return iUtServiceModule.updateCOLR(this.mPhoneId, i);
    }

    public int updateCOLP(boolean z) throws RemoteException {
        IUtServiceModule iUtServiceModule = this.mUtService;
        if (iUtServiceModule == null) {
            return -1;
        }
        return iUtServiceModule.updateCOLP(this.mPhoneId, z);
    }

    public void setListener(IImsUtListener iImsUtListener) throws RemoteException {
        this.mListener = iImsUtListener;
    }

    public int queryCallBarringForServiceClass(int i, int i2) throws RemoteException {
        IUtServiceModule iUtServiceModule = this.mUtService;
        if (iUtServiceModule == null) {
            return -1;
        }
        return iUtServiceModule.queryCallBarring(this.mPhoneId, i, i2);
    }

    public int updateCallBarringForServiceClass(int i, int i2, String[] strArr, int i3) throws RemoteException {
        IUtServiceModule iUtServiceModule = this.mUtService;
        if (iUtServiceModule == null) {
            return -1;
        }
        return iUtServiceModule.updateCallBarring(this.mPhoneId, i, i2, i3, (String) null, strArr);
    }

    public boolean isUssdEnabled() throws RemoteException {
        return this.mUtService.isUssdEnabled(this.mPhoneId);
    }
}
