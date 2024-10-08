package com.sec.internal.ims.core.iil;

import android.os.RemoteException;
import com.sec.internal.log.IMSLog;
import vendor.samsung.hardware.radio.channel.ISehRadioChannelCallback;

public class ImsSecRadioChannelCallback extends ISehRadioChannelCallback.Stub {
    static final String LOG_TAG = "ImsSecRadioChannelCallback";
    private final IpcDispatcherAidl mIpcDispatcher;

    public String getInterfaceHash() throws RemoteException {
        return null;
    }

    public int getInterfaceVersion() throws RemoteException {
        return 0;
    }

    public ImsSecRadioChannelCallback(IpcDispatcherAidl ipcDispatcherAidl) {
        this.mIpcDispatcher = ipcDispatcherAidl;
    }

    public void receive(byte[] bArr) throws RemoteException {
        IMSLog.i(LOG_TAG, "receive");
        byte[] arrayListToPrimitiveArray = IpcUtil.arrayListToPrimitiveArray(bArr);
        if (arrayListToPrimitiveArray != null) {
            try {
                this.mIpcDispatcher.processResponse(arrayListToPrimitiveArray, arrayListToPrimitiveArray.length);
            } catch (RuntimeException e) {
                IMSLog.e(LOG_TAG, "receive " + e);
            }
        }
    }
}
