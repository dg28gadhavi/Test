package com.sec.internal.ims.core;

import android.net.Network;
import java.util.LinkedList;
import java.util.concurrent.atomic.AtomicBoolean;

/* compiled from: R8$$SyntheticClass */
public final /* synthetic */ class RegistrationUtils$$ExternalSyntheticLambda1 implements Runnable {
    public final /* synthetic */ int f$0;
    public final /* synthetic */ Network f$1;
    public final /* synthetic */ String f$2;
    public final /* synthetic */ LinkedList f$3;
    public final /* synthetic */ AtomicBoolean f$4;

    public /* synthetic */ RegistrationUtils$$ExternalSyntheticLambda1(int i, Network network, String str, LinkedList linkedList, AtomicBoolean atomicBoolean) {
        this.f$0 = i;
        this.f$1 = network;
        this.f$2 = str;
        this.f$3 = linkedList;
        this.f$4 = atomicBoolean;
    }

    public final void run() {
        RegistrationUtils.lambda$getAllByNameWithThread$2(this.f$0, this.f$1, this.f$2, this.f$3, this.f$4);
    }
}
