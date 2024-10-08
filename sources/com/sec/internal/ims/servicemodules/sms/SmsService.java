package com.sec.internal.ims.servicemodules.sms;

import android.os.RemoteException;
import com.sec.ims.sms.ISmsService;
import com.sec.ims.sms.ISmsServiceEventListener;
import com.sec.internal.ims.servicemodules.base.ServiceModuleBase;

public class SmsService extends ISmsService.Stub {
    private final SmsServiceModule mServiceModule;

    public SmsService(ServiceModuleBase serviceModuleBase) {
        this.mServiceModule = (SmsServiceModule) serviceModuleBase;
    }

    public void registerForSMSStateChange(int i, ISmsServiceEventListener iSmsServiceEventListener) throws RemoteException {
        this.mServiceModule.registerForSMSStateChange(i, iSmsServiceEventListener);
    }

    public void deRegisterForSMSStateChange(int i, ISmsServiceEventListener iSmsServiceEventListener) throws RemoteException {
        this.mServiceModule.deRegisterForSMSStateChange(i, iSmsServiceEventListener);
    }

    public void sendSMSOverIMS(int i, byte[] bArr, String str, String str2, int i2) throws RemoteException {
        this.mServiceModule.sendSMSOverIMS(i, bArr, str, str2, i2, false);
    }

    public void sendSMSResponse(boolean z, int i) throws RemoteException {
        this.mServiceModule.sendSMSResponse(z, i);
    }

    public void sendDeliverReport(int i, byte[] bArr) throws RemoteException {
        this.mServiceModule.sendDeliverReport(i, bArr);
    }

    public boolean getSmsFallback(int i) throws RemoteException {
        return this.mServiceModule.getSmsFallback(i);
    }
}
