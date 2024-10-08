package com.sec.internal.ims.servicemodules.im;

import android.net.Uri;
import com.sec.internal.constants.ims.servicemodules.im.ImDirection;

/* compiled from: R8$$SyntheticClass */
public final /* synthetic */ class FtProcessor$$ExternalSyntheticLambda2 implements Runnable {
    public final /* synthetic */ FtProcessor f$0;
    public final /* synthetic */ String f$1;
    public final /* synthetic */ ImDirection f$2;
    public final /* synthetic */ String f$3;
    public final /* synthetic */ Uri f$4;

    public /* synthetic */ FtProcessor$$ExternalSyntheticLambda2(FtProcessor ftProcessor, String str, ImDirection imDirection, String str2, Uri uri) {
        this.f$0 = ftProcessor;
        this.f$1 = str;
        this.f$2 = imDirection;
        this.f$3 = str2;
        this.f$4 = uri;
    }

    public final void run() {
        this.f$0.lambda$acceptFileTransfer$2(this.f$1, this.f$2, this.f$3, this.f$4);
    }
}
