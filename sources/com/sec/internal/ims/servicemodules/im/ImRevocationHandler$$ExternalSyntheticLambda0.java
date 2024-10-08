package com.sec.internal.ims.servicemodules.im;

import java.util.List;

/* compiled from: R8$$SyntheticClass */
public final /* synthetic */ class ImRevocationHandler$$ExternalSyntheticLambda0 implements Runnable {
    public final /* synthetic */ ImRevocationHandler f$0;
    public final /* synthetic */ String f$1;
    public final /* synthetic */ boolean f$2;
    public final /* synthetic */ int f$3;
    public final /* synthetic */ List f$4;

    public /* synthetic */ ImRevocationHandler$$ExternalSyntheticLambda0(ImRevocationHandler imRevocationHandler, String str, boolean z, int i, List list) {
        this.f$0 = imRevocationHandler;
        this.f$1 = str;
        this.f$2 = z;
        this.f$3 = i;
        this.f$4 = list;
    }

    public final void run() {
        this.f$0.lambda$requestMessageRevocation$0(this.f$1, this.f$2, this.f$3, this.f$4);
    }
}
