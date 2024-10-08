package com.sec.internal.ims.core.iil;

import android.os.AsyncResult;
import android.os.IBinder;
import android.os.Registrant;
import android.os.RemoteException;
import android.os.ServiceManager;
import com.sec.internal.log.IMSLog;
import java.util.Iterator;
import vendor.samsung.hardware.radio.channel.ISehRadioChannel;

public class IpcDispatcherAidl extends IpcDispatcher {
    /* access modifiers changed from: private */
    public long mBinderDeathCount;
    private final ImsSecRadioChannelCallback mImsSecRadioChannelCallback;
    protected volatile ISehRadioChannel mSecRadioChannelProxy;
    private final SecRadioChannelProxyDeathRecipient mSecRadioChannelProxyDeathRecipient;

    public IpcDispatcherAidl() {
        this(0);
    }

    public IpcDispatcherAidl(int i) {
        super(i);
        this.mSecRadioChannelProxy = null;
        this.mImsSecRadioChannelCallback = new ImsSecRadioChannelCallback(this);
        this.mSecRadioChannelProxyDeathRecipient = new SecRadioChannelProxyDeathRecipient();
        this.mBinderDeathCount = 0;
    }

    /* access modifiers changed from: protected */
    public void initChannel() {
        getSecRadioChannelProxy();
    }

    /* access modifiers changed from: protected */
    public void handleChannelProxyDead(long j) {
        int i = this.mSlotId;
        IMSLog.i("IpcDispatcher", i, "handleMessage: EVENT_SEC_RADIO_CHANNEL_PROXY_DEAD cookie = " + j + " mSecChannelProxyCookie = " + this.mChannelProxyCookie.get());
        if (j == this.mChannelProxyCookie.get()) {
            resetProxy();
            getSecRadioChannelProxy();
        }
    }

    /* access modifiers changed from: protected */
    public void resetProxy() {
        this.mSecRadioChannelProxy = null;
        this.mChannelProxyCookie.incrementAndGet();
    }

    /* access modifiers changed from: protected */
    public void handleSendIpc(byte[] bArr) {
        ISehRadioChannel secRadioChannelProxy = getSecRadioChannelProxy();
        if (secRadioChannelProxy != null) {
            try {
                IMSLog.i("IpcDispatcher", this.mSlotId, "ImsModemSender(): send");
                secRadioChannelProxy.send(bArr);
            } catch (RemoteException | RuntimeException e) {
                handleChannelProxyExceptionForRR("send", e);
            }
        }
    }

    final class SecRadioChannelProxyDeathRecipient implements IBinder.DeathRecipient {
        SecRadioChannelProxyDeathRecipient() {
        }

        public void binderDied() {
            IpcDispatcherAidl ipcDispatcherAidl = IpcDispatcherAidl.this;
            ipcDispatcherAidl.notifyChannelProxyDied(ipcDispatcherAidl.mBinderDeathCount);
            IpcDispatcherAidl ipcDispatcherAidl2 = IpcDispatcherAidl.this;
            ipcDispatcherAidl2.mBinderDeathCount = ipcDispatcherAidl2.mBinderDeathCount + 1;
        }
    }

    /* access modifiers changed from: protected */
    public ISehRadioChannel getSecRadioChannelProxy() {
        if (this.mSecRadioChannelProxy != null) {
            return this.mSecRadioChannelProxy;
        }
        try {
            createChannelBySlotCount();
        } catch (RemoteException | RuntimeException e) {
            this.mSecRadioChannelProxy = null;
            int i = this.mSlotId;
            IMSLog.e("IpcDispatcher", i, "SecRadioChannelProxy getService/setCallback: " + e);
        }
        if (this.mSecRadioChannelProxy == null) {
            publishChannelProxyDeadEvent(this.mChannelProxyCookie.incrementAndGet());
        }
        return this.mSecRadioChannelProxy;
    }

    private synchronized void createChannelBySlotCount() throws RemoteException {
        IBinder waitForDeclaredService = ServiceManager.waitForDeclaredService(ISehRadioChannel.DESCRIPTOR + "/" + IpcDispatcher.CHANNEL_SERVICE_NAME[this.mSlotId]);
        if (waitForDeclaredService != null) {
            this.mSecRadioChannelProxy = getSehRadioChannelInterface(waitForDeclaredService);
        }
        if (this.mSecRadioChannelProxy != null) {
            this.mSecRadioChannelProxy.asBinder().linkToDeath(this.mSecRadioChannelProxyDeathRecipient, 0);
            this.mSecRadioChannelProxy.setCallback(this.mImsSecRadioChannelCallback);
            IMSLog.s("IpcDispatcher", this.mSlotId, "notify IIL Connected");
            Iterator<Registrant> it = this.mRegistrantsForIilConnected.iterator();
            while (it.hasNext()) {
                it.next().notifyRegistrant(new AsyncResult((Object) null, (Object) null, (Throwable) null));
            }
        } else {
            IMSLog.e("IpcDispatcher", this.mSlotId, "getSecRadioChannelProxy: mSecRadioChannelProxy == null");
        }
    }

    /* access modifiers changed from: protected */
    public ISehRadioChannel getSehRadioChannelInterface(IBinder iBinder) {
        return ISehRadioChannel.Stub.asInterface(iBinder);
    }
}
