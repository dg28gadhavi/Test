package com.sec.internal.ims.core;

import com.sec.internal.ims.core.SlotBasedConfig;
import com.sec.internal.interfaces.ims.core.IRegisterTask;
import java.util.function.Consumer;

/* compiled from: R8$$SyntheticClass */
public final /* synthetic */ class RegistrationManagerInternal$$ExternalSyntheticLambda7 implements Consumer {
    public final /* synthetic */ SlotBasedConfig.RegisterTaskList f$0;

    public /* synthetic */ RegistrationManagerInternal$$ExternalSyntheticLambda7(SlotBasedConfig.RegisterTaskList registerTaskList) {
        this.f$0 = registerTaskList;
    }

    public final void accept(Object obj) {
        this.f$0.remove((IRegisterTask) obj);
    }
}
