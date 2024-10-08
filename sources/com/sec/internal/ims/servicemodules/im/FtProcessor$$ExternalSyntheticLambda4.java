package com.sec.internal.ims.servicemodules.im;

import android.net.Uri;

/* compiled from: R8$$SyntheticClass */
public final /* synthetic */ class FtProcessor$$ExternalSyntheticLambda4 implements Runnable {
    public final /* synthetic */ FtProcessor f$0;
    public final /* synthetic */ String f$1;
    public final /* synthetic */ boolean f$2;
    public final /* synthetic */ Uri f$3;

    public /* synthetic */ FtProcessor$$ExternalSyntheticLambda4(FtProcessor ftProcessor, String str, boolean z, Uri uri) {
        this.f$0 = ftProcessor;
        this.f$1 = str;
        this.f$2 = z;
        this.f$3 = uri;
    }

    public final void run() {
        this.f$0.lambda$resumeSendingTransfer$7(this.f$1, this.f$2, this.f$3);
    }
}
