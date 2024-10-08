package com.sec.internal.ims.servicemodules.openapi;

import android.os.Bundle;
import android.os.IInterface;
import com.sec.ims.openapi.ISipDialogListener;
import com.sec.internal.helper.AsyncResult;
import com.sec.internal.helper.os.RemoteCallbackListWrapper;

/* compiled from: R8$$SyntheticClass */
public final /* synthetic */ class OpenApiServiceModule$$ExternalSyntheticLambda1 implements RemoteCallbackListWrapper.Broadcaster {
    public final /* synthetic */ AsyncResult f$0;

    public /* synthetic */ OpenApiServiceModule$$ExternalSyntheticLambda1(AsyncResult asyncResult) {
        this.f$0 = asyncResult;
    }

    public final void broadcast(IInterface iInterface) {
        ((ISipDialogListener) iInterface).onSipReceived(((Bundle) this.f$0.result).getString("message"));
    }
}
