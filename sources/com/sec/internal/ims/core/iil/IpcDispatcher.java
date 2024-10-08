package com.sec.internal.ims.core.iil;

import android.os.AsyncResult;
import android.os.Handler;
import android.os.HandlerThread;
import android.os.Looper;
import android.os.Message;
import android.os.Registrant;
import com.sec.internal.helper.SimUtil;
import com.sec.internal.log.IMSLog;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.concurrent.atomic.AtomicLong;

public abstract class IpcDispatcher {
    static final int CHANNEL_GET_SERVICE_DELAY_MILLIS = 4000;
    static final String[] CHANNEL_SERVICE_NAME = {"imsd", "imsd2"};
    static final int EVENT_SEC_CHANNEL_PROXY_DEAD = 1;
    protected static final int EVENT_SEND_IPC = 1;
    protected static final String LOG_TAG = "IpcDispatcher";
    protected static final int VOLTE_TYPE_DUAL = 3;
    protected static final int VOLTE_TYPE_SINGLE = 1;
    protected final AtomicLong mChannelProxyCookie = new AtomicLong(0);
    protected final ArrayList<Registrant> mRegistrants;
    protected ArrayList<Registrant> mRegistrantsForIilConnected;
    protected final SecChannelHandler mSecChannelHandler;
    private ImsModemSender mSender;
    private HandlerThread mSenderThread;
    protected final int mSlotId;
    protected int mSupportVolteType;

    /* access modifiers changed from: package-private */
    public abstract void handleChannelProxyDead(long j);

    /* access modifiers changed from: package-private */
    public abstract void handleSendIpc(byte[] bArr);

    /* access modifiers changed from: package-private */
    public abstract void initChannel();

    /* access modifiers changed from: package-private */
    public abstract void resetProxy();

    public IpcDispatcher(int i) {
        if (SimUtil.getPhoneCount() > 1) {
            this.mSupportVolteType = 3;
        } else {
            this.mSupportVolteType = 1;
        }
        IMSLog.i(LOG_TAG, i, "IpcDispatcher Support Volte Type = " + this.mSupportVolteType);
        this.mSlotId = i;
        this.mRegistrants = new ArrayList<>();
        this.mRegistrantsForIilConnected = new ArrayList<>();
        this.mSecChannelHandler = new SecChannelHandler(Looper.myLooper());
    }

    protected class SecChannelHandler extends Handler {
        public SecChannelHandler(Looper looper) {
            super(looper);
        }

        public void handleMessage(Message message) {
            if (message.what == 1) {
                IpcDispatcher.this.handleChannelProxyDead(((Long) message.obj).longValue());
            }
        }
    }

    private class ImsModemSender extends Handler implements Runnable {
        public void run() {
        }

        public ImsModemSender(Looper looper) {
            super(looper);
        }

        public void handleMessage(Message message) {
            byte[] bArr = (byte[]) message.obj;
            if (message.what == 1) {
                IpcDispatcher.this.handleSendIpc(bArr);
            }
        }
    }

    public void initDispatcher() {
        HandlerThread handlerThread = new HandlerThread("ImsModemSender" + this.mSlotId);
        this.mSenderThread = handlerThread;
        handlerThread.start();
        this.mSender = new ImsModemSender(this.mSenderThread.getLooper());
        initChannel();
    }

    public boolean setRegistrant(int i, Handler handler) {
        this.mRegistrants.add(new Registrant(handler, i, (Object) null));
        return true;
    }

    public boolean setRegistrantForIilConnected(int i, Handler handler) {
        this.mRegistrantsForIilConnected.add(new Registrant(handler, i, (Object) null));
        return true;
    }

    /* access modifiers changed from: package-private */
    public void processResponse(byte[] bArr, int i) {
        IpcMessage parseIpc = IpcMessage.parseIpc(bArr, i);
        if (parseIpc == null) {
            IMSLog.e(LOG_TAG, this.mSlotId, "cannot parse ipc");
            return;
        }
        int i2 = this.mSlotId;
        IMSLog.i(LOG_TAG, i2, "[Rx]: (M)" + parseIpc.mainCmdStr() + " (S)" + parseIpc.subCmdStr() + " (T)" + parseIpc.typeStr() + " l:" + parseIpc.getLength());
        int i3 = this.mSlotId;
        StringBuilder sb = new StringBuilder();
        sb.append("[Rx]: ");
        sb.append(IpcUtil.dumpHex(parseIpc.getBody()));
        IMSLog.s(LOG_TAG, i3, sb.toString());
        Iterator<Registrant> it = this.mRegistrants.iterator();
        while (it.hasNext()) {
            it.next().notifyRegistrant(new AsyncResult((Object) null, parseIpc, (Throwable) null));
        }
    }

    public boolean sendMessage(IpcMessage ipcMessage) {
        if (ipcMessage == null) {
            IMSLog.e(LOG_TAG, this.mSlotId, "send IPC message error");
            return false;
        }
        ipcMessage.setDir(1);
        int i = this.mSlotId;
        IMSLog.i(LOG_TAG, i, "[Tx]: (M)" + ipcMessage.mainCmdStr() + " (S)" + ipcMessage.subCmdStr() + " (T)" + ipcMessage.typeStr() + " l:" + ipcMessage.getLength());
        int i2 = this.mSlotId;
        StringBuilder sb = new StringBuilder();
        sb.append("[Tx]: ");
        sb.append(IpcUtil.dumpHex(ipcMessage.getBody()));
        IMSLog.s(LOG_TAG, i2, sb.toString());
        ImsModemSender imsModemSender = this.mSender;
        if (imsModemSender != null) {
            imsModemSender.obtainMessage(1, ipcMessage.getData()).sendToTarget();
        }
        return true;
    }

    public boolean sendGeneralResponse(boolean z, IpcMessage ipcMessage) {
        return sendGeneralResponse(z ? 32768 : IpcMessage.IPC_GEN_ERR_INVALID_STATE, ipcMessage);
    }

    public boolean sendGeneralResponse(int i, IpcMessage ipcMessage) {
        IpcMessage ipcMessage2 = new IpcMessage(128, 1, 2);
        ipcMessage2.encodeGeneralResponse(i, ipcMessage);
        return sendMessage(ipcMessage2);
    }

    /* access modifiers changed from: protected */
    public void notifyChannelProxyDied(long j) {
        Iterator<Registrant> it = this.mRegistrants.iterator();
        while (it.hasNext()) {
            it.next().notifyRegistrant(new AsyncResult((Object) null, (Object) null, new IOException("Disconnected from '" + CHANNEL_SERVICE_NAME[this.mSlotId] + "'")));
        }
        IMSLog.i(LOG_TAG, this.mSlotId, "serviceDied");
        publishChannelProxyDeadEvent(j);
    }

    /* access modifiers changed from: protected */
    public void handleChannelProxyExceptionForRR(String str, Exception exc) {
        int i = this.mSlotId;
        IMSLog.e(LOG_TAG, i, str + ": " + exc);
        resetProxy();
        publishChannelProxyDeadEvent(this.mChannelProxyCookie.incrementAndGet());
    }

    /* access modifiers changed from: protected */
    public void publishChannelProxyDeadEvent(long j) {
        SecChannelHandler secChannelHandler = this.mSecChannelHandler;
        secChannelHandler.sendMessageDelayed(secChannelHandler.obtainMessage(1, Long.valueOf(j)), 4000);
    }
}
