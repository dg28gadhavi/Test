package com.sec.internal.ims.servicemodules.im;

import android.net.Uri;

/* compiled from: R8$$SyntheticClass */
public final /* synthetic */ class ImSessionProcessor$$ExternalSyntheticLambda9 implements Runnable {
    public final /* synthetic */ ImSessionProcessor f$0;
    public final /* synthetic */ String f$1;
    public final /* synthetic */ String f$2;
    public final /* synthetic */ Uri f$3;

    public /* synthetic */ ImSessionProcessor$$ExternalSyntheticLambda9(ImSessionProcessor imSessionProcessor, String str, String str2, Uri uri) {
        this.f$0 = imSessionProcessor;
        this.f$1 = str;
        this.f$2 = str2;
        this.f$3 = uri;
    }

    public final void run() {
        this.f$0.lambda$changeGroupChatIcon$8(this.f$1, this.f$2, this.f$3);
    }
}
