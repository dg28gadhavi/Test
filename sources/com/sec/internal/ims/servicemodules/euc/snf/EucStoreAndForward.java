package com.sec.internal.ims.servicemodules.euc.snf;

import android.os.Handler;
import android.os.Looper;
import android.os.Message;
import android.util.Log;
import com.sec.internal.helper.AsyncResult;
import com.sec.internal.ims.servicemodules.euc.data.EucResponseData;
import com.sec.internal.ims.servicemodules.euc.data.EucSendResponseStatus;
import com.sec.internal.ims.servicemodules.euc.data.IEucData;
import com.sec.internal.ims.servicemodules.euc.snf.IEucStoreAndForward;
import com.sec.internal.interfaces.ims.servicemodules.euc.IEucServiceInterface;
import com.sec.internal.log.IMSLog;
import java.util.HashSet;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.Set;

public class EucStoreAndForward extends Handler implements IEucStoreAndForward {
    private static final int EVENT_SEND_RESPONSE_RESPONSE = 1;
    private static final String LOG_TAG = EucStoreAndForward.class.getSimpleName();
    private IEucServiceInterface mEucService;
    private Set<String> mOwnIdentitiesInForwardState = new HashSet();
    /* access modifiers changed from: private */
    public List<IEucStoreAndForwardResponseData> storedResponses = new LinkedList();

    public EucStoreAndForward(IEucServiceInterface iEucServiceInterface, Looper looper) {
        super(looper);
        this.mEucService = iEucServiceInterface;
    }

    public IEucStoreAndForward.IResponseHandle sendResponse(IEucData iEucData, EucResponseData.Response response, IEucStoreAndForward.IResponseCallback iResponseCallback) {
        AnonymousClass1 r7 = new IEucStoreAndForward.IResponseHandle() {
            public void invalidate() {
                Iterator it = EucStoreAndForward.this.storedResponses.iterator();
                while (it.hasNext()) {
                    if (this == ((IEucStoreAndForwardResponseData) it.next()).getResponseToWorkflowHandle()) {
                        it.remove();
                    }
                }
            }
        };
        this.storedResponses.add(createEUCStoreAndForwardResponseData(iEucData, response, (String) null, iResponseCallback, r7));
        if (this.mOwnIdentitiesInForwardState.contains(iEucData.getOwnIdentity())) {
            this.mEucService.sendEucResponse(new EucResponseData(iEucData.getId(), iEucData.getType(), (String) null, iEucData.getRemoteUri(), iEucData.getOwnIdentity(), response, obtainMessage(1)));
        }
        return r7;
    }

    public IEucStoreAndForward.IResponseHandle sendResponse(IEucData iEucData, EucResponseData.Response response, String str, IEucStoreAndForward.IResponseCallback iResponseCallback) {
        AnonymousClass2 r7 = new IEucStoreAndForward.IResponseHandle() {
            public void invalidate() {
                Iterator it = EucStoreAndForward.this.storedResponses.iterator();
                while (it.hasNext()) {
                    if (this == ((IEucStoreAndForwardResponseData) it.next()).getResponseToWorkflowHandle()) {
                        it.remove();
                    }
                }
            }
        };
        this.storedResponses.add(createEUCStoreAndForwardResponseData(iEucData, response, str, iResponseCallback, r7));
        if (this.mOwnIdentitiesInForwardState.contains(iEucData.getOwnIdentity())) {
            this.mEucService.sendEucResponse(new EucResponseData(iEucData.getId(), iEucData.getType(), str, iEucData.getRemoteUri(), iEucData.getOwnIdentity(), response, obtainMessage(1)));
        }
        return r7;
    }

    public void store(String str) {
        this.mOwnIdentitiesInForwardState.remove(str);
        String str2 = LOG_TAG;
        Log.i(str2, "state for ownIdentity = " + IMSLog.checker(str) + " set to STORE");
    }

    public void forward(String str) {
        this.mOwnIdentitiesInForwardState.add(str);
        String str2 = LOG_TAG;
        Log.i(str2, "state for ownIdentity = " + IMSLog.checker(str) + " set to FORWARD");
        for (IEucStoreAndForwardResponseData next : this.storedResponses) {
            IEucData eUCData = next.getEUCData();
            if (eUCData.getOwnIdentity().equals(str)) {
                this.mEucService.sendEucResponse(new EucResponseData(eUCData.getId(), eUCData.getType(), next.getPIN(), eUCData.getRemoteUri(), eUCData.getOwnIdentity(), next.getResponse(), obtainMessage(1)));
            }
        }
    }

    private IEucStoreAndForwardResponseData createEUCStoreAndForwardResponseData(IEucData iEucData, EucResponseData.Response response, String str, IEucStoreAndForward.IResponseCallback iResponseCallback, IEucStoreAndForward.IResponseHandle iResponseHandle) {
        final IEucData iEucData2 = iEucData;
        final EucResponseData.Response response2 = response;
        final String str2 = str;
        final IEucStoreAndForward.IResponseCallback iResponseCallback2 = iResponseCallback;
        final IEucStoreAndForward.IResponseHandle iResponseHandle2 = iResponseHandle;
        return new IEucStoreAndForwardResponseData() {
            public IEucData getEUCData() {
                return iEucData2;
            }

            public EucResponseData.Response getResponse() {
                return response2;
            }

            public String getPIN() {
                return str2;
            }

            public IEucStoreAndForward.IResponseCallback getCallback() {
                return iResponseCallback2;
            }

            public IEucStoreAndForward.IResponseHandle getResponseToWorkflowHandle() {
                return iResponseHandle2;
            }
        };
    }

    public void handleMessage(Message message) {
        if (1 == message.what) {
            EucSendResponseStatus eucSendResponseStatus = (EucSendResponseStatus) ((AsyncResult) message.obj).result;
            IEucStoreAndForwardResponseData iEucStoreAndForwardResponseData = null;
            for (IEucStoreAndForwardResponseData next : this.storedResponses) {
                if (next.getEUCData().getKey().equals(eucSendResponseStatus.getKey()) && iEucStoreAndForwardResponseData == null) {
                    iEucStoreAndForwardResponseData = next;
                }
            }
            if (iEucStoreAndForwardResponseData != null) {
                iEucStoreAndForwardResponseData.getCallback().onStatus(eucSendResponseStatus);
                this.storedResponses.remove(iEucStoreAndForwardResponseData);
                return;
            }
            return;
        }
        Log.e(LOG_TAG, "handleMessage: Undefined message, ignoring!");
    }
}
