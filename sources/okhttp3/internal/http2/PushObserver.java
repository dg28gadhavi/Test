package okhttp3.internal.http2;

import java.io.IOException;
import java.util.List;
import kotlin.jvm.internal.Intrinsics;
import okio.BufferedSource;
import org.jetbrains.annotations.NotNull;

/* compiled from: PushObserver.kt */
public interface PushObserver {
    @NotNull
    public static final PushObserver CANCEL = new Companion.PushObserverCancel();
    @NotNull
    public static final Companion Companion = Companion.$$INSTANCE;

    boolean onData(int i, @NotNull BufferedSource bufferedSource, int i2, boolean z) throws IOException;

    boolean onHeaders(int i, @NotNull List<Header> list, boolean z);

    boolean onRequest(int i, @NotNull List<Header> list);

    void onReset(int i, @NotNull ErrorCode errorCode);

    /* compiled from: PushObserver.kt */
    public static final class Companion {
        static final /* synthetic */ Companion $$INSTANCE = new Companion();

        private Companion() {
        }

        /* compiled from: PushObserver.kt */
        private static final class PushObserverCancel implements PushObserver {
            public boolean onHeaders(int i, @NotNull List<Header> list, boolean z) {
                Intrinsics.checkNotNullParameter(list, "responseHeaders");
                return true;
            }

            public boolean onRequest(int i, @NotNull List<Header> list) {
                Intrinsics.checkNotNullParameter(list, "requestHeaders");
                return true;
            }

            public void onReset(int i, @NotNull ErrorCode errorCode) {
                Intrinsics.checkNotNullParameter(errorCode, "errorCode");
            }

            public boolean onData(int i, @NotNull BufferedSource bufferedSource, int i2, boolean z) throws IOException {
                Intrinsics.checkNotNullParameter(bufferedSource, "source");
                bufferedSource.skip((long) i2);
                return true;
            }
        }
    }
}
