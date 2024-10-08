package com.sec.internal.ims.servicemodules.im;

import com.sec.internal.constants.ims.servicemodules.im.ImDirection;

/* compiled from: R8$$SyntheticClass */
public final /* synthetic */ class FtProcessor$$ExternalSyntheticLambda11 implements Runnable {
    public final /* synthetic */ FtProcessor f$0;
    public final /* synthetic */ String f$1;
    public final /* synthetic */ ImDirection f$2;
    public final /* synthetic */ String f$3;

    public /* synthetic */ FtProcessor$$ExternalSyntheticLambda11(FtProcessor ftProcessor, String str, ImDirection imDirection, String str2) {
        this.f$0 = ftProcessor;
        this.f$1 = str;
        this.f$2 = imDirection;
        this.f$3 = str2;
    }

    public final void run() {
        this.f$0.lambda$rejectFileTransfer$6(this.f$1, this.f$2, this.f$3);
    }
}
