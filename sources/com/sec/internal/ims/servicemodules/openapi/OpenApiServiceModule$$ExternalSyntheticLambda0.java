package com.sec.internal.ims.servicemodules.openapi;

import android.os.IInterface;
import com.sec.ims.openapi.ISipDialogListener;
import com.sec.internal.helper.os.RemoteCallbackListWrapper;

/* compiled from: R8$$SyntheticClass */
public final /* synthetic */ class OpenApiServiceModule$$ExternalSyntheticLambda0 implements RemoteCallbackListWrapper.Broadcaster {
    public final /* synthetic */ int f$0;
    public final /* synthetic */ String f$1;
    public final /* synthetic */ boolean f$2;

    public /* synthetic */ OpenApiServiceModule$$ExternalSyntheticLambda0(int i, String str, boolean z) {
        this.f$0 = i;
        this.f$1 = str;
        this.f$2 = z;
    }

    public final void broadcast(IInterface iInterface) {
        ((ISipDialogListener) iInterface).onSipParamsReceived(this.f$0, this.f$1, this.f$2);
    }
}
