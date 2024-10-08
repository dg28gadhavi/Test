package com.sec.internal.ims.servicemodules.euc;

import com.sec.internal.ims.servicemodules.euc.data.EucSendResponseStatus;
import com.sec.internal.ims.servicemodules.euc.data.IEucData;
import com.sec.internal.ims.servicemodules.euc.snf.IEucStoreAndForward;

/* compiled from: R8$$SyntheticClass */
public final /* synthetic */ class EucModule$$ExternalSyntheticLambda0 implements IEucStoreAndForward.IResponseCallback {
    public final /* synthetic */ IEucData f$0;

    public /* synthetic */ EucModule$$ExternalSyntheticLambda0(IEucData iEucData) {
        this.f$0 = iEucData;
    }

    public final void onStatus(EucSendResponseStatus eucSendResponseStatus) {
        EucModule.lambda$handleEucTestSendResponse$0(this.f$0, eucSendResponseStatus);
    }
}
