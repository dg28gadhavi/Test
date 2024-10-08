package com.sec.internal.ims.core;

import android.net.Network;
import com.sec.internal.interfaces.ims.core.IRegisterTask;
import com.sec.internal.interfaces.ims.rcs.IRcsPolicyManager;
import java.util.List;

/* compiled from: R8$$SyntheticClass */
public final /* synthetic */ class RegistrationUtils$$ExternalSyntheticLambda2 implements Runnable {
    public final /* synthetic */ int f$0;
    public final /* synthetic */ Network f$1;
    public final /* synthetic */ String f$2;
    public final /* synthetic */ List f$3;
    public final /* synthetic */ RegistrationManagerHandler f$4;
    public final /* synthetic */ IRcsPolicyManager f$5;
    public final /* synthetic */ IRegisterTask f$6;

    public /* synthetic */ RegistrationUtils$$ExternalSyntheticLambda2(int i, Network network, String str, List list, RegistrationManagerHandler registrationManagerHandler, IRcsPolicyManager iRcsPolicyManager, IRegisterTask iRegisterTask) {
        this.f$0 = i;
        this.f$1 = network;
        this.f$2 = str;
        this.f$3 = list;
        this.f$4 = registrationManagerHandler;
        this.f$5 = iRcsPolicyManager;
        this.f$6 = iRegisterTask;
    }

    public final void run() {
        RegistrationUtils.lambda$getHostAddressWithThread$1(this.f$0, this.f$1, this.f$2, this.f$3, this.f$4, this.f$5, this.f$6);
    }
}
