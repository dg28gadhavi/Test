package com.sec.internal.ims.core.iil;

import android.os.AsyncResult;
import android.os.IHwBinder;
import android.os.Registrant;
import android.os.RemoteException;
import com.sec.internal.log.IMSLog;
import java.util.Iterator;
import vendor.samsung.hardware.radio.channel.V2_0.ISehChannel;

public class IpcDispatcherHidl extends IpcDispatcher {
    private final ImsSecChannelCallback mImsSecChannelCallback;
    protected volatile ISehChannel mSecChannelProxy;
    private final SecChannelProxyDeathRecipient mSecChannelProxyDeathRecipient;

    public IpcDispatcherHidl() {
        this(0);
    }

    public IpcDispatcherHidl(int i) {
        super(i);
        this.mSecChannelProxy = null;
        this.mImsSecChannelCallback = new ImsSecChannelCallback(this);
        this.mSecChannelProxyDeathRecipient = new SecChannelProxyDeathRecipient();
    }

    /* access modifiers changed from: protected */
    public void initChannel() {
        getSecChannelProxy();
    }

    /* access modifiers changed from: protected */
    public void handleSendIpc(byte[] bArr) {
        ISehChannel secChannelProxy = getSecChannelProxy();
        IMSLog.i("IpcDispatcher", "getSecChannelProxy");
        if (secChannelProxy != null) {
            try {
                IMSLog.i("IpcDispatcher", this.mSlotId, "ImsModemSender(): send");
                secChannelProxy.send(IpcUtil.primitiveArrayToArrayList(bArr));
            } catch (RemoteException | RuntimeException e) {
                handleChannelProxyExceptionForRR("send", e);
            }
        }
    }

    /* access modifiers changed from: protected */
    public void handleChannelProxyDead(long j) {
        int i = this.mSlotId;
        IMSLog.i("IpcDispatcher", i, "handleMessage: EVENT_SEC_CHANNEL_PROXY_DEAD cookie = " + j + " mSecChannelProxyCookie = " + this.mChannelProxyCookie.get());
        if (j == this.mChannelProxyCookie.get()) {
            resetProxy();
            getSecChannelProxy();
        }
    }

    /* access modifiers changed from: protected */
    public void resetProxy() {
        this.mSecChannelProxy = null;
        this.mChannelProxyCookie.incrementAndGet();
    }

    final class SecChannelProxyDeathRecipient implements IHwBinder.DeathRecipient {
        SecChannelProxyDeathRecipient() {
        }

        public void serviceDied(long j) {
            IpcDispatcherHidl.this.notifyChannelProxyDied(j);
        }
    }

    /* access modifiers changed from: protected */
    public ISehChannel getSecChannelProxy() {
        if (this.mSecChannelProxy != null) {
            return this.mSecChannelProxy;
        }
        try {
            createChannelProxyBySimSlotCount();
        } catch (RemoteException | RuntimeException e) {
            this.mSecChannelProxy = null;
            int i = this.mSlotId;
            IMSLog.e("IpcDispatcher", i, "SecChannelProxy getService/setCallback: " + e);
        }
        if (this.mSecChannelProxy == null) {
            publishChannelProxyDeadEvent(this.mChannelProxyCookie.incrementAndGet());
        }
        return this.mSecChannelProxy;
    }

    private synchronized void createChannelProxyBySimSlotCount() throws RemoteException {
        this.mSecChannelProxy = ISehChannel.getService(IpcDispatcher.CHANNEL_SERVICE_NAME[this.mSlotId]);
        if (this.mSecChannelProxy != null) {
            this.mSecChannelProxy.linkToDeath(this.mSecChannelProxyDeathRecipient, this.mChannelProxyCookie.incrementAndGet());
            this.mSecChannelProxy.setCallback(this.mImsSecChannelCallback);
            IMSLog.s("IpcDispatcher", this.mSlotId, "notify IIL Connected");
            Iterator<Registrant> it = this.mRegistrantsForIilConnected.iterator();
            while (it.hasNext()) {
                it.next().notifyRegistrant(new AsyncResult((Object) null, (Object) null, (Throwable) null));
            }
        } else {
            IMSLog.e("IpcDispatcher", this.mSlotId, "getSecChannelProxy: mSecChannelProxy == null");
        }
    }
}
