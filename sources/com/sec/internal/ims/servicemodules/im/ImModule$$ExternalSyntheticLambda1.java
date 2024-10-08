package com.sec.internal.ims.servicemodules.im;

import com.sec.ims.util.ImsUri;
import java.util.List;

/* compiled from: R8$$SyntheticClass */
public final /* synthetic */ class ImModule$$ExternalSyntheticLambda1 implements Runnable {
    public final /* synthetic */ ImModule f$0;
    public final /* synthetic */ ImsUri f$1;
    public final /* synthetic */ String f$2;
    public final /* synthetic */ List f$3;
    public final /* synthetic */ String f$4;
    public final /* synthetic */ String f$5;
    public final /* synthetic */ int f$6;

    public /* synthetic */ ImModule$$ExternalSyntheticLambda1(ImModule imModule, ImsUri imsUri, String str, List list, String str2, String str3, int i) {
        this.f$0 = imModule;
        this.f$1 = imsUri;
        this.f$2 = str;
        this.f$3 = list;
        this.f$4 = str2;
        this.f$5 = str3;
        this.f$6 = i;
    }

    public final void run() {
        this.f$0.lambda$reportChatbotAsSpam$4(this.f$1, this.f$2, this.f$3, this.f$4, this.f$5, this.f$6);
    }
}
