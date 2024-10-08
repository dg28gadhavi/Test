package com.sec.internal.ims.entitlement.nsds.ericssonnsds.flow;

import android.content.Context;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.os.Message;
import android.os.Messenger;
import android.util.Log;
import com.sec.internal.constants.ims.entitilement.NSDSNamespaces;
import com.sec.internal.constants.ims.entitilement.data.NSDSRequest;
import com.sec.internal.constants.ims.entitilement.data.NSDSResponse;
import com.sec.internal.constants.ims.entitilement.data.ResponseManageConnectivity;
import com.sec.internal.ims.entitlement.nsds.ericssonnsds.NSDSClient;
import java.util.concurrent.atomic.AtomicInteger;

public class OperationUsingManageConnectivity extends NSDSBaseProcedure {
    private static final String LOG_TAG = OperationUsingManageConnectivity.class.getSimpleName();
    protected String mDeviceGroup;
    protected int mOperation;
    protected String mRemoteDeviceId;
    protected String mVIMSI;

    public OperationUsingManageConnectivity(Looper looper, Context context, BaseFlowImpl baseFlowImpl, Messenger messenger, String str, String str2, String str3) {
        super(looper, context, baseFlowImpl, messenger, str, str2, str3);
        Log.i(LOG_TAG, "created.");
    }

    public NSDSRequest[] buildRequests(NSDSCommonParameters nSDSCommonParameters) {
        NSDSClient nSDSClient = this.mBaseFlowImpl.getNSDSClient();
        AtomicInteger atomicInteger = new AtomicInteger();
        NSDSClient nSDSClient2 = nSDSClient;
        return new NSDSRequest[]{nSDSClient2.buildAuthenticationRequest(atomicInteger.incrementAndGet(), true, nSDSCommonParameters.getChallengeResponse(), nSDSCommonParameters.getAkaToken(), (String) null, nSDSCommonParameters.getImsiEap(), nSDSCommonParameters.getDeviceId()), nSDSClient2.buildManageConnectivityRequest(atomicInteger.incrementAndGet(), this.mOperation, this.mVIMSI, this.mRemoteDeviceId, this.mDeviceGroup, (String) null, nSDSCommonParameters.getDeviceId())};
    }

    /* access modifiers changed from: protected */
    public Message getResponseMessage() {
        int i = this.mOperation;
        return Message.obtain((Handler) null, i == 1 ? 109 : (i != 0 && i == 2) ? 111 : 102);
    }

    /* access modifiers changed from: protected */
    public boolean processResponse(Bundle bundle) {
        ResponseManageConnectivity responseManageConnectivity;
        if (bundle != null) {
            responseManageConnectivity = (ResponseManageConnectivity) bundle.getParcelable(NSDSNamespaces.NSDSMethodNamespace.MANAGE_CONNECTIVITY);
        } else {
            Log.e(LOG_TAG, "responseCollection is null");
            responseManageConnectivity = null;
        }
        return this.mOperation == 2 || !retryForServerError((NSDSResponse) responseManageConnectivity);
    }

    /* access modifiers changed from: protected */
    public void executeOperationWithChallenge() {
        int i = this.mOperation;
        this.mBaseFlowImpl.performOperation(i == 1 ? 38 : (i != 0 && i == 2) ? 40 : 31, this, new Messenger(this));
    }
}
