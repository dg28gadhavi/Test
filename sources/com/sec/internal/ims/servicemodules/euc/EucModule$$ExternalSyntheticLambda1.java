package com.sec.internal.ims.servicemodules.euc;

import com.sec.internal.ims.servicemodules.euc.data.EucResponseData;
import com.sec.internal.ims.servicemodules.euc.data.IEucData;
import com.sec.internal.ims.servicemodules.euc.snf.IEucStoreAndForward;

/* compiled from: R8$$SyntheticClass */
public final /* synthetic */ class EucModule$$ExternalSyntheticLambda1 implements Runnable {
    public final /* synthetic */ EucModule f$0;
    public final /* synthetic */ String f$1;
    public final /* synthetic */ IEucData f$2;
    public final /* synthetic */ EucResponseData.Response f$3;
    public final /* synthetic */ IEucStoreAndForward.IResponseCallback f$4;

    public /* synthetic */ EucModule$$ExternalSyntheticLambda1(EucModule eucModule, String str, IEucData iEucData, EucResponseData.Response response, IEucStoreAndForward.IResponseCallback iResponseCallback) {
        this.f$0 = eucModule;
        this.f$1 = str;
        this.f$2 = iEucData;
        this.f$3 = response;
        this.f$4 = iResponseCallback;
    }

    public final void run() {
        this.f$0.lambda$handleEucTestSendResponse$1(this.f$1, this.f$2, this.f$3, this.f$4);
    }
}
