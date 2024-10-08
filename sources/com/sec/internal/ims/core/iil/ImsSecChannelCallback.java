package com.sec.internal.ims.core.iil;

import com.sec.internal.log.IMSLog;
import java.util.ArrayList;
import vendor.samsung.hardware.radio.channel.V2_0.ISehChannelCallback;

public class ImsSecChannelCallback extends ISehChannelCallback.Stub {
    static final String LOG_TAG = "ImsSecChannelCallback";
    private final IpcDispatcherHidl mIpcDispatcher;

    public ImsSecChannelCallback(IpcDispatcherHidl ipcDispatcherHidl) {
        this.mIpcDispatcher = ipcDispatcherHidl;
    }

    public void receive(ArrayList<Byte> arrayList) {
        IMSLog.i(LOG_TAG, "receive");
        try {
            byte[] arrayListToPrimitiveArray = IpcUtil.arrayListToPrimitiveArray(arrayList);
            if (arrayListToPrimitiveArray != null) {
                this.mIpcDispatcher.processResponse(arrayListToPrimitiveArray, arrayListToPrimitiveArray.length);
            }
        } catch (RuntimeException e) {
            IMSLog.e(LOG_TAG, "receive " + e);
        }
    }
}
