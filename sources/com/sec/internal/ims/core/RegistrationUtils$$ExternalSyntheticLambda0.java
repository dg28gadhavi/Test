package com.sec.internal.ims.core;

import com.sec.internal.interfaces.ims.core.IRegisterTask;
import java.util.function.Consumer;

/* compiled from: R8$$SyntheticClass */
public final /* synthetic */ class RegistrationUtils$$ExternalSyntheticLambda0 implements Consumer {
    public final /* synthetic */ IRegisterTask f$0;

    public /* synthetic */ RegistrationUtils$$ExternalSyntheticLambda0(IRegisterTask iRegisterTask) {
        this.f$0 = iRegisterTask;
    }

    public final void accept(Object obj) {
        RegistrationUtils.lambda$updateImsIcon$0(this.f$0, (ImsIconManager) obj);
    }
}
