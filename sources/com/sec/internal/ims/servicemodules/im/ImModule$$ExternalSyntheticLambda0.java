package com.sec.internal.ims.servicemodules.im;

import com.sec.ims.util.ImsUri;

/* compiled from: R8$$SyntheticClass */
public final /* synthetic */ class ImModule$$ExternalSyntheticLambda0 implements Runnable {
    public final /* synthetic */ ImModule f$0;
    public final /* synthetic */ ImsUri f$1;
    public final /* synthetic */ int f$2;
    public final /* synthetic */ String f$3;
    public final /* synthetic */ String f$4;

    public /* synthetic */ ImModule$$ExternalSyntheticLambda0(ImModule imModule, ImsUri imsUri, int i, String str, String str2) {
        this.f$0 = imModule;
        this.f$1 = imsUri;
        this.f$2 = i;
        this.f$3 = str;
        this.f$4 = str2;
    }

    public final void run() {
        this.f$0.lambda$requestChatbotAnonymize$3(this.f$1, this.f$2, this.f$3, this.f$4);
    }
}
