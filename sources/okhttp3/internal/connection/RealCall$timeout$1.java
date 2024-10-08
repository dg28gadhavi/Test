package okhttp3.internal.connection;

import okio.AsyncTimeout;

/* compiled from: RealCall.kt */
public final class RealCall$timeout$1 extends AsyncTimeout {
    final /* synthetic */ RealCall this$0;

    RealCall$timeout$1(RealCall realCall) {
        this.this$0 = realCall;
    }

    /* access modifiers changed from: protected */
    public void timedOut() {
        this.this$0.cancel();
    }
}
