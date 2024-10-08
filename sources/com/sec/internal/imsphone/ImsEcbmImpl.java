package com.sec.internal.imsphone;

import android.os.RemoteException;
import com.android.ims.internal.IImsEcbm;
import com.android.ims.internal.IImsEcbmListener;

public class ImsEcbmImpl extends IImsEcbm.Stub {
    private IImsEcbmListener miImsEcbmListener;

    public void setListener(IImsEcbmListener iImsEcbmListener) throws RemoteException {
        this.miImsEcbmListener = iImsEcbmListener;
    }

    public void exitEmergencyCallbackMode() throws RemoteException {
        this.miImsEcbmListener.exitedECBM();
    }

    public void enterEmergencyCallbackMode() throws RemoteException {
        this.miImsEcbmListener.enteredECBM();
    }
}
