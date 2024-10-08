package okio;

import java.nio.ByteBuffer;
import kotlin.jvm.internal.Intrinsics;
import org.jetbrains.annotations.NotNull;

/* compiled from: RealBufferedSink.kt */
public final class RealBufferedSink implements BufferedSink {
    @NotNull
    public final Buffer bufferField = new Buffer();
    public boolean closed;
    @NotNull
    public final Sink sink;

    public void write(@NotNull Buffer buffer, long j) {
        Intrinsics.checkNotNullParameter(buffer, "source");
        if (!this.closed) {
            this.bufferField.write(buffer, j);
            emitCompleteSegments();
            return;
        }
        throw new IllegalStateException("closed".toString());
    }

    @NotNull
    public BufferedSink write(@NotNull ByteString byteString) {
        Intrinsics.checkNotNullParameter(byteString, "byteString");
        if (!this.closed) {
            this.bufferField.write(byteString);
            return emitCompleteSegments();
        }
        throw new IllegalStateException("closed".toString());
    }

    public RealBufferedSink(@NotNull Sink sink2) {
        Intrinsics.checkNotNullParameter(sink2, "sink");
        this.sink = sink2;
    }

    @NotNull
    public Buffer getBuffer() {
        return this.bufferField;
    }

    public long writeAll(@NotNull Source source) {
        Intrinsics.checkNotNullParameter(source, "source");
        long j = 0;
        while (true) {
            long read = source.read(this.bufferField, 8192);
            if (read == -1) {
                return j;
            }
            j += read;
            emitCompleteSegments();
        }
    }

    @NotNull
    public BufferedSink writeUtf8(@NotNull String str) {
        Intrinsics.checkNotNullParameter(str, "string");
        if (!this.closed) {
            this.bufferField.writeUtf8(str);
            return emitCompleteSegments();
        }
        throw new IllegalStateException("closed".toString());
    }

    @NotNull
    public BufferedSink write(@NotNull byte[] bArr) {
        Intrinsics.checkNotNullParameter(bArr, "source");
        if (!this.closed) {
            this.bufferField.write(bArr);
            return emitCompleteSegments();
        }
        throw new IllegalStateException("closed".toString());
    }

    @NotNull
    public BufferedSink write(@NotNull byte[] bArr, int i, int i2) {
        Intrinsics.checkNotNullParameter(bArr, "source");
        if (!this.closed) {
            this.bufferField.write(bArr, i, i2);
            return emitCompleteSegments();
        }
        throw new IllegalStateException("closed".toString());
    }

    public int write(@NotNull ByteBuffer byteBuffer) {
        Intrinsics.checkNotNullParameter(byteBuffer, "source");
        if (!this.closed) {
            int write = this.bufferField.write(byteBuffer);
            emitCompleteSegments();
            return write;
        }
        throw new IllegalStateException("closed".toString());
    }

    @NotNull
    public BufferedSink writeByte(int i) {
        if (!this.closed) {
            this.bufferField.writeByte(i);
            return emitCompleteSegments();
        }
        throw new IllegalStateException("closed".toString());
    }

    @NotNull
    public BufferedSink writeShort(int i) {
        if (!this.closed) {
            this.bufferField.writeShort(i);
            return emitCompleteSegments();
        }
        throw new IllegalStateException("closed".toString());
    }

    @NotNull
    public BufferedSink writeInt(int i) {
        if (!this.closed) {
            this.bufferField.writeInt(i);
            return emitCompleteSegments();
        }
        throw new IllegalStateException("closed".toString());
    }

    public boolean isOpen() {
        return !this.closed;
    }

    @NotNull
    public BufferedSink writeDecimalLong(long j) {
        if (!this.closed) {
            this.bufferField.writeDecimalLong(j);
            return emitCompleteSegments();
        }
        throw new IllegalStateException("closed".toString());
    }

    @NotNull
    public BufferedSink writeHexadecimalUnsignedLong(long j) {
        if (!this.closed) {
            this.bufferField.writeHexadecimalUnsignedLong(j);
            return emitCompleteSegments();
        }
        throw new IllegalStateException("closed".toString());
    }

    @NotNull
    public BufferedSink emitCompleteSegments() {
        if (!this.closed) {
            long completeSegmentByteCount = this.bufferField.completeSegmentByteCount();
            if (completeSegmentByteCount > 0) {
                this.sink.write(this.bufferField, completeSegmentByteCount);
            }
            return this;
        }
        throw new IllegalStateException("closed".toString());
    }

    public void flush() {
        if (!this.closed) {
            if (this.bufferField.size() > 0) {
                Sink sink2 = this.sink;
                Buffer buffer = this.bufferField;
                sink2.write(buffer, buffer.size());
            }
            this.sink.flush();
            return;
        }
        throw new IllegalStateException("closed".toString());
    }

    public void close() {
        if (!this.closed) {
            try {
                if (this.bufferField.size() > 0) {
                    Sink sink2 = this.sink;
                    Buffer buffer = this.bufferField;
                    sink2.write(buffer, buffer.size());
                }
                th = null;
            } catch (Throwable th) {
                th = th;
            }
            try {
                this.sink.close();
            } catch (Throwable th2) {
                if (th == null) {
                    th = th2;
                }
            }
            this.closed = true;
            if (th != null) {
                throw th;
            }
        }
    }

    @NotNull
    public Timeout timeout() {
        return this.sink.timeout();
    }

    @NotNull
    public String toString() {
        return "buffer(" + this.sink + ')';
    }
}
