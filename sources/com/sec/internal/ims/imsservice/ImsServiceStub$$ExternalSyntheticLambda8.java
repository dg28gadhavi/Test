package com.sec.internal.ims.imsservice;

import com.sec.ims.ISimMobilityStatusListener;
import com.sec.internal.interfaces.ims.core.ISimManager;
import java.util.function.Consumer;

/* compiled from: R8$$SyntheticClass */
public final /* synthetic */ class ImsServiceStub$$ExternalSyntheticLambda8 implements Consumer {
    public final /* synthetic */ ImsServiceStub f$0;
    public final /* synthetic */ ISimMobilityStatusListener f$1;

    public /* synthetic */ ImsServiceStub$$ExternalSyntheticLambda8(ImsServiceStub imsServiceStub, ISimMobilityStatusListener iSimMobilityStatusListener) {
        this.f$0 = imsServiceStub;
        this.f$1 = iSimMobilityStatusListener;
    }

    public final void accept(Object obj) {
        this.f$0.lambda$unregisterSimMobilityStatusListenerByPhoneId$5(this.f$1, (ISimManager) obj);
    }
}
