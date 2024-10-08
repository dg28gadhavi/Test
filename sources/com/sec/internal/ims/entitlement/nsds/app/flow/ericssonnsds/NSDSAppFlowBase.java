package com.sec.internal.ims.entitlement.nsds.app.flow.ericssonnsds;

import android.content.Context;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.os.Message;
import android.os.Messenger;
import android.os.RemoteException;
import com.sec.internal.constants.ims.entitilement.NSDSNamespaces;
import com.sec.internal.ims.entitlement.nsds.ericssonnsds.flow.BaseFlowImpl;
import com.sec.internal.ims.entitlement.nsds.strategy.IMnoNsdsStrategy;
import com.sec.internal.ims.entitlement.nsds.strategy.MnoNsdsStrategyCreator;
import com.sec.internal.ims.entitlement.storagehelper.NSDSDatabaseHelper;
import com.sec.internal.log.IMSLog;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public abstract class NSDSAppFlowBase extends Handler {
    private static final String LOG_TAG = NSDSAppFlowBase.class.getSimpleName();
    protected static final List<Messenger> sEvtMessengers = new ArrayList();
    protected BaseFlowImpl mBaseFlowImpl;
    protected final Context mContext;
    protected ArrayList<Message> mDeferredMessages = new ArrayList<>();
    protected int mDeviceEventType;
    protected String mDeviceGroup = null;
    protected String mImeiForUA = null;
    protected Handler mModuleHandler = null;
    protected NSDSDatabaseHelper mNSDSDatabaseHelper;
    protected int mRetryCount = 0;
    protected int mSlotId = 0;
    protected String mUserAgent = null;

    /* access modifiers changed from: protected */
    public abstract void notifyNSDSFlowResponse(boolean z, String str, int i, int i2);

    /* access modifiers changed from: protected */
    public abstract void queueOperation(int i, Bundle bundle);

    public NSDSAppFlowBase(Looper looper, Context context, BaseFlowImpl baseFlowImpl, NSDSDatabaseHelper nSDSDatabaseHelper) {
        super(looper);
        this.mContext = context;
        this.mBaseFlowImpl = baseFlowImpl;
        this.mNSDSDatabaseHelper = nSDSDatabaseHelper;
        init();
    }

    public NSDSAppFlowBase(Looper looper, Context context, BaseFlowImpl baseFlowImpl, NSDSDatabaseHelper nSDSDatabaseHelper, Handler handler) {
        super(looper);
        this.mContext = context;
        this.mBaseFlowImpl = baseFlowImpl;
        this.mNSDSDatabaseHelper = nSDSDatabaseHelper;
        this.mModuleHandler = handler;
        init();
    }

    private void init() {
        String str;
        this.mSlotId = this.mBaseFlowImpl.getSimManager().getSimSlotIndex();
        IMnoNsdsStrategy mnoNsdsStrategy = getMnoNsdsStrategy();
        String str2 = null;
        if (mnoNsdsStrategy == null) {
            str = null;
        } else {
            str = mnoNsdsStrategy.getUserAgent();
        }
        this.mUserAgent = str;
        if (mnoNsdsStrategy != null) {
            str2 = mnoNsdsStrategy.getDeviceGroup(this.mSlotId, this.mBaseFlowImpl.getSimManager().getSimMnoName());
        }
        this.mDeviceGroup = str2;
    }

    /* access modifiers changed from: protected */
    public <T> Iterable<T> emptyIfNull(Iterable<T> iterable) {
        return iterable == null ? Collections.emptyList() : iterable;
    }

    /* access modifiers changed from: protected */
    public IMnoNsdsStrategy getMnoNsdsStrategy() {
        return MnoNsdsStrategyCreator.getInstance(this.mContext, this.mBaseFlowImpl.getSimManager().getSimSlotIndex()).getMnoStrategy();
    }

    /* access modifiers changed from: protected */
    public final void deferMessage(Message message) {
        String str = LOG_TAG;
        IMSLog.i(str, "deferMessage: msg=" + message.what);
        this.mDeferredMessages.add(Message.obtain(message));
    }

    /* access modifiers changed from: protected */
    public int getHttpErrRespCode(Bundle bundle) {
        int i = -1;
        if (bundle != null) {
            i = bundle.getInt(NSDSNamespaces.NSDSDataMapKey.HTTP_RESP_CODE, -1);
        }
        String str = LOG_TAG;
        IMSLog.i(str, "getHttpErrRespCode: " + i);
        return i;
    }

    /* access modifiers changed from: protected */
    public String getHttpErrRespReason(Bundle bundle) {
        String str = null;
        if (bundle != null) {
            str = bundle.getString(NSDSNamespaces.NSDSDataMapKey.HTTP_RESP_REASON, (String) null);
        }
        String str2 = LOG_TAG;
        IMSLog.i(str2, "getHttpErrRespReason: " + str);
        return str;
    }

    /* access modifiers changed from: protected */
    public void moveDeferredMessageAtFrontOfQueue() {
        for (int size = this.mDeferredMessages.size() - 1; size >= 0; size += -1) {
            Message message = this.mDeferredMessages.get(size);
            IMSLog.i(LOG_TAG, "moveDeferredMessageAtFrontOfQueue: what = " + message.what);
            sendMessageAtFrontOfQueue(message);
        }
        this.mDeferredMessages.clear();
    }

    /* access modifiers changed from: protected */
    public void clearDeferredMessage() {
        IMSLog.i(LOG_TAG, "clearDeferredMessage()");
        this.mDeferredMessages.clear();
    }

    public static void registerEventMessenger(Messenger messenger) {
        List<Messenger> list = sEvtMessengers;
        synchronized (list) {
            String str = LOG_TAG;
            IMSLog.i(str, "registerEventMessenger: " + list.size());
            if (messenger != null) {
                list.add(messenger);
            }
        }
    }

    public static void unregisterEventMessenger(Messenger messenger) {
        List<Messenger> list = sEvtMessengers;
        synchronized (list) {
            String str = LOG_TAG;
            IMSLog.i(str, "unregisterEventMessenger: " + list.size());
            if (messenger != null) {
                list.remove(messenger);
            }
        }
    }

    protected class NSDSResponseStatus {
        public int failedOperation;
        public String methodName;
        public int responseCode;

        public NSDSResponseStatus(int i, String str, int i2) {
            this.responseCode = i;
            this.methodName = str;
            this.failedOperation = i2;
        }
    }

    /* access modifiers changed from: protected */
    public void performNextOperationIf(int i, NSDSResponseStatus nSDSResponseStatus, Bundle bundle) {
        boolean z = false;
        if (getMnoNsdsStrategy() != null) {
            int nextOperation = getMnoNsdsStrategy().getNextOperation(this.mDeviceEventType, i, nSDSResponseStatus.responseCode, bundle);
            String str = LOG_TAG;
            IMSLog.i(str, "performNextOperationIf: nextOperation " + nextOperation);
            if (nextOperation == -1) {
                int i2 = nSDSResponseStatus.responseCode;
                if (i2 == 1000) {
                    z = true;
                }
                notifyNSDSFlowResponse(z, nSDSResponseStatus.methodName, nSDSResponseStatus.failedOperation, i2);
                return;
            }
            queueOperation(nextOperation, bundle);
            return;
        }
        notifyNSDSFlowResponse(false, (String) null, -1, -1);
    }

    /* access modifiers changed from: protected */
    public void notifyCallbackForNsdsEvent(int i, int i2) {
        List<Messenger> list = sEvtMessengers;
        synchronized (list) {
            IMSLog.i(LOG_TAG, "notifyCallbackForNsdsEvent: eventType=" + i + ":" + list.size());
            for (int size = list.size() - 1; size >= 0; size--) {
                try {
                    sEvtMessengers.get(size).send(obtainMessage(i, i2, -1));
                } catch (RemoteException e) {
                    IMSLog.s(LOG_TAG, "notifyCallbackForNsdsEvent: dead messenger, removed " + e.getMessage());
                    sEvtMessengers.remove(size);
                }
            }
        }
    }
}
