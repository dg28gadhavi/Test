package okio;

import com.sec.internal.ims.servicemodules.euc.test.EucTestIntent;
import java.io.OutputStream;
import kotlin.jvm.internal.Intrinsics;
import org.jetbrains.annotations.NotNull;

/* compiled from: JvmOkio.kt */
final class OutputStreamSink implements Sink {
    @NotNull
    private final OutputStream out;
    @NotNull
    private final Timeout timeout;

    public OutputStreamSink(@NotNull OutputStream outputStream, @NotNull Timeout timeout2) {
        Intrinsics.checkNotNullParameter(outputStream, "out");
        Intrinsics.checkNotNullParameter(timeout2, EucTestIntent.Extras.TIMEOUT);
        this.out = outputStream;
        this.timeout = timeout2;
    }

    public void write(@NotNull Buffer buffer, long j) {
        Intrinsics.checkNotNullParameter(buffer, "source");
        _UtilKt.checkOffsetAndCount(buffer.size(), 0, j);
        while (j > 0) {
            this.timeout.throwIfReached();
            Segment segment = buffer.head;
            Intrinsics.checkNotNull(segment);
            int min = (int) Math.min(j, (long) (segment.limit - segment.pos));
            this.out.write(segment.data, segment.pos, min);
            segment.pos += min;
            long j2 = (long) min;
            j -= j2;
            buffer.setSize$okio(buffer.size() - j2);
            if (segment.pos == segment.limit) {
                buffer.head = segment.pop();
                SegmentPool.recycle(segment);
            }
        }
    }

    public void flush() {
        this.out.flush();
    }

    public void close() {
        this.out.close();
    }

    @NotNull
    public Timeout timeout() {
        return this.timeout;
    }

    @NotNull
    public String toString() {
        return "sink(" + this.out + ')';
    }
}
