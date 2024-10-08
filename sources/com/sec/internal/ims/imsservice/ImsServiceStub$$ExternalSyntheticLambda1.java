package com.sec.internal.ims.imsservice;

import com.sec.ims.IImsRegistrationListener;
import com.sec.internal.interfaces.ims.core.ISimManager;
import java.util.function.Consumer;

/* compiled from: R8$$SyntheticClass */
public final /* synthetic */ class ImsServiceStub$$ExternalSyntheticLambda1 implements Consumer {
    public final /* synthetic */ ImsServiceStub f$0;
    public final /* synthetic */ IImsRegistrationListener f$1;

    public /* synthetic */ ImsServiceStub$$ExternalSyntheticLambda1(ImsServiceStub imsServiceStub, IImsRegistrationListener iImsRegistrationListener) {
        this.f$0 = imsServiceStub;
        this.f$1 = iImsRegistrationListener;
    }

    public final void accept(Object obj) {
        this.f$0.lambda$registerImsRegistrationListener$13(this.f$1, (ISimManager) obj);
    }
}
