package okio;

import java.io.EOFException;
import java.io.IOException;
import java.util.zip.DataFormatException;
import java.util.zip.Inflater;
import kotlin.jvm.internal.Intrinsics;
import org.jetbrains.annotations.NotNull;

/* compiled from: InflaterSource.kt */
public final class InflaterSource implements Source {
    private int bufferBytesHeldByInflater;
    private boolean closed;
    @NotNull
    private final Inflater inflater;
    @NotNull
    private final BufferedSource source;

    public InflaterSource(@NotNull BufferedSource bufferedSource, @NotNull Inflater inflater2) {
        Intrinsics.checkNotNullParameter(bufferedSource, "source");
        Intrinsics.checkNotNullParameter(inflater2, "inflater");
        this.source = bufferedSource;
        this.inflater = inflater2;
    }

    public long read(@NotNull Buffer buffer, long j) throws IOException {
        Intrinsics.checkNotNullParameter(buffer, "sink");
        do {
            long readOrInflate = readOrInflate(buffer, j);
            if (readOrInflate > 0) {
                return readOrInflate;
            }
            if (this.inflater.finished() || this.inflater.needsDictionary()) {
                return -1;
            }
        } while (!this.source.exhausted());
        throw new EOFException("source exhausted prematurely");
    }

    public final long readOrInflate(@NotNull Buffer buffer, long j) throws IOException {
        Intrinsics.checkNotNullParameter(buffer, "sink");
        int i = (j > 0 ? 1 : (j == 0 ? 0 : -1));
        if (!(i >= 0)) {
            throw new IllegalArgumentException(Intrinsics.stringPlus("byteCount < 0: ", Long.valueOf(j)).toString());
        } else if (!(!this.closed)) {
            throw new IllegalStateException("closed".toString());
        } else if (i == 0) {
            return 0;
        } else {
            try {
                Segment writableSegment$okio = buffer.writableSegment$okio(1);
                refill();
                int inflate = this.inflater.inflate(writableSegment$okio.data, writableSegment$okio.limit, (int) Math.min(j, (long) (8192 - writableSegment$okio.limit)));
                releaseBytesAfterInflate();
                if (inflate > 0) {
                    writableSegment$okio.limit += inflate;
                    long j2 = (long) inflate;
                    buffer.setSize$okio(buffer.size() + j2);
                    return j2;
                }
                if (writableSegment$okio.pos == writableSegment$okio.limit) {
                    buffer.head = writableSegment$okio.pop();
                    SegmentPool.recycle(writableSegment$okio);
                }
                return 0;
            } catch (DataFormatException e) {
                throw new IOException(e);
            }
        }
    }

    public final boolean refill() throws IOException {
        if (!this.inflater.needsInput()) {
            return false;
        }
        if (this.source.exhausted()) {
            return true;
        }
        Segment segment = this.source.getBuffer().head;
        Intrinsics.checkNotNull(segment);
        int i = segment.limit;
        int i2 = segment.pos;
        int i3 = i - i2;
        this.bufferBytesHeldByInflater = i3;
        this.inflater.setInput(segment.data, i2, i3);
        return false;
    }

    private final void releaseBytesAfterInflate() {
        int i = this.bufferBytesHeldByInflater;
        if (i != 0) {
            int remaining = i - this.inflater.getRemaining();
            this.bufferBytesHeldByInflater -= remaining;
            this.source.skip((long) remaining);
        }
    }

    @NotNull
    public Timeout timeout() {
        return this.source.timeout();
    }

    public void close() throws IOException {
        if (!this.closed) {
            this.inflater.end();
            this.closed = true;
            this.source.close();
        }
    }
}
