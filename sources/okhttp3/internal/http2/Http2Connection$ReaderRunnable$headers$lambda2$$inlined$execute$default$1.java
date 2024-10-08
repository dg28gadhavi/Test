package okhttp3.internal.http2;

import java.io.IOException;
import kotlin.jvm.internal.Intrinsics;
import okhttp3.internal.concurrent.Task;
import okhttp3.internal.platform.Platform;

/* renamed from: okhttp3.internal.http2.Http2Connection$ReaderRunnable$headers$lambda-2$$inlined$execute$default$1  reason: invalid class name */
/* compiled from: TaskQueue.kt */
public final class Http2Connection$ReaderRunnable$headers$lambda2$$inlined$execute$default$1 extends Task {
    final /* synthetic */ boolean $cancelable;
    final /* synthetic */ String $name;
    final /* synthetic */ Http2Stream $newStream$inlined;
    final /* synthetic */ Http2Connection this$0;

    /* JADX INFO: super call moved to the top of the method (can break code semantics) */
    public Http2Connection$ReaderRunnable$headers$lambda2$$inlined$execute$default$1(String str, boolean z, Http2Connection http2Connection, Http2Stream http2Stream) {
        super(str, z);
        this.$name = str;
        this.$cancelable = z;
        this.this$0 = http2Connection;
        this.$newStream$inlined = http2Stream;
    }

    public long runOnce() {
        try {
            this.this$0.getListener$okhttp().onStream(this.$newStream$inlined);
            return -1;
        } catch (IOException e) {
            Platform.Companion.get().log(Intrinsics.stringPlus("Http2Connection.Listener failure for ", this.this$0.getConnectionName$okhttp()), 4, e);
            try {
                this.$newStream$inlined.close(ErrorCode.PROTOCOL_ERROR, e);
                return -1;
            } catch (IOException unused) {
                return -1;
            }
        }
    }
}
