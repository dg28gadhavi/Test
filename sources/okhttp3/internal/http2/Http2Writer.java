package okhttp3.internal.http2;

import java.io.Closeable;
import java.io.IOException;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;
import kotlin.jvm.internal.DefaultConstructorMarker;
import kotlin.jvm.internal.Intrinsics;
import okhttp3.internal.Util;
import okhttp3.internal.http2.Hpack;
import okio.Buffer;
import okio.BufferedSink;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

/* compiled from: Http2Writer.kt */
public final class Http2Writer implements Closeable {
    @NotNull
    public static final Companion Companion = new Companion((DefaultConstructorMarker) null);
    private static final Logger logger = Logger.getLogger(Http2.class.getName());
    private final boolean client;
    private boolean closed;
    @NotNull
    private final Buffer hpackBuffer;
    @NotNull
    private final Hpack.Writer hpackWriter;
    private int maxFrameSize = 16384;
    @NotNull
    private final BufferedSink sink;

    public Http2Writer(@NotNull BufferedSink bufferedSink, boolean z) {
        Intrinsics.checkNotNullParameter(bufferedSink, "sink");
        this.sink = bufferedSink;
        this.client = z;
        Buffer buffer = new Buffer();
        this.hpackBuffer = buffer;
        this.hpackWriter = new Hpack.Writer(0, false, buffer, 3, (DefaultConstructorMarker) null);
    }

    public final synchronized void connectionPreface() throws IOException {
        if (this.closed) {
            throw new IOException("closed");
        } else if (this.client) {
            Logger logger2 = logger;
            if (logger2.isLoggable(Level.FINE)) {
                logger2.fine(Util.format(Intrinsics.stringPlus(">> CONNECTION ", Http2.CONNECTION_PREFACE.hex()), new Object[0]));
            }
            this.sink.write(Http2.CONNECTION_PREFACE);
            this.sink.flush();
        }
    }

    public final synchronized void applyAndAckSettings(@NotNull Settings settings) throws IOException {
        Intrinsics.checkNotNullParameter(settings, "peerSettings");
        if (!this.closed) {
            this.maxFrameSize = settings.getMaxFrameSize(this.maxFrameSize);
            if (settings.getHeaderTableSize() != -1) {
                this.hpackWriter.resizeHeaderTable(settings.getHeaderTableSize());
            }
            frameHeader(0, 0, 4, 1);
            this.sink.flush();
        } else {
            throw new IOException("closed");
        }
    }

    public final synchronized void pushPromise(int i, int i2, @NotNull List<Header> list) throws IOException {
        Intrinsics.checkNotNullParameter(list, "requestHeaders");
        if (!this.closed) {
            this.hpackWriter.writeHeaders(list);
            long size = this.hpackBuffer.size();
            int min = (int) Math.min(((long) this.maxFrameSize) - 4, size);
            int i3 = min + 4;
            long j = (long) min;
            int i4 = (size > j ? 1 : (size == j ? 0 : -1));
            frameHeader(i, i3, 5, i4 == 0 ? 4 : 0);
            this.sink.writeInt(i2 & Integer.MAX_VALUE);
            this.sink.write(this.hpackBuffer, j);
            if (i4 > 0) {
                writeContinuationFrames(i, size - j);
            }
        } else {
            throw new IOException("closed");
        }
    }

    public final synchronized void flush() throws IOException {
        if (!this.closed) {
            this.sink.flush();
        } else {
            throw new IOException("closed");
        }
    }

    public final synchronized void rstStream(int i, @NotNull ErrorCode errorCode) throws IOException {
        Intrinsics.checkNotNullParameter(errorCode, "errorCode");
        if (!this.closed) {
            if (errorCode.getHttpCode() != -1) {
                frameHeader(i, 4, 3, 0);
                this.sink.writeInt(errorCode.getHttpCode());
                this.sink.flush();
            } else {
                throw new IllegalArgumentException("Failed requirement.".toString());
            }
        } else {
            throw new IOException("closed");
        }
    }

    public final int maxDataLength() {
        return this.maxFrameSize;
    }

    public final synchronized void data(boolean z, int i, @Nullable Buffer buffer, int i2) throws IOException {
        if (!this.closed) {
            dataFrame(i, z ? 1 : 0, buffer, i2);
        } else {
            throw new IOException("closed");
        }
    }

    public final void dataFrame(int i, int i2, @Nullable Buffer buffer, int i3) throws IOException {
        frameHeader(i, i3, 0, i2);
        if (i3 > 0) {
            BufferedSink bufferedSink = this.sink;
            Intrinsics.checkNotNull(buffer);
            bufferedSink.write(buffer, (long) i3);
        }
    }

    public final synchronized void settings(@NotNull Settings settings) throws IOException {
        Intrinsics.checkNotNullParameter(settings, "settings");
        if (!this.closed) {
            int i = 0;
            frameHeader(0, settings.size() * 6, 4, 0);
            while (i < 10) {
                int i2 = i + 1;
                if (settings.isSet(i)) {
                    this.sink.writeShort(i != 4 ? i != 7 ? i : 4 : 3);
                    this.sink.writeInt(settings.get(i));
                }
                i = i2;
            }
            this.sink.flush();
        } else {
            throw new IOException("closed");
        }
    }

    public final synchronized void ping(boolean z, int i, int i2) throws IOException {
        if (!this.closed) {
            frameHeader(0, 8, 6, z ? 1 : 0);
            this.sink.writeInt(i);
            this.sink.writeInt(i2);
            this.sink.flush();
        } else {
            throw new IOException("closed");
        }
    }

    public final synchronized void goAway(int i, @NotNull ErrorCode errorCode, @NotNull byte[] bArr) throws IOException {
        Intrinsics.checkNotNullParameter(errorCode, "errorCode");
        Intrinsics.checkNotNullParameter(bArr, "debugData");
        if (!this.closed) {
            boolean z = false;
            if (errorCode.getHttpCode() != -1) {
                frameHeader(0, bArr.length + 8, 7, 0);
                this.sink.writeInt(i);
                this.sink.writeInt(errorCode.getHttpCode());
                if (bArr.length == 0) {
                    z = true;
                }
                if (!z) {
                    this.sink.write(bArr);
                }
                this.sink.flush();
            } else {
                throw new IllegalArgumentException("errorCode.httpCode == -1".toString());
            }
        } else {
            throw new IOException("closed");
        }
    }

    public final synchronized void windowUpdate(int i, long j) throws IOException {
        if (!this.closed) {
            if (j != 0 && j <= 2147483647L) {
                frameHeader(i, 4, 8, 0);
                this.sink.writeInt((int) j);
                this.sink.flush();
            } else {
                throw new IllegalArgumentException(Intrinsics.stringPlus("windowSizeIncrement == 0 || windowSizeIncrement > 0x7fffffffL: ", Long.valueOf(j)).toString());
            }
        } else {
            throw new IOException("closed");
        }
    }

    public final void frameHeader(int i, int i2, int i3, int i4) throws IOException {
        Logger logger2 = logger;
        if (logger2.isLoggable(Level.FINE)) {
            logger2.fine(Http2.INSTANCE.frameLog(false, i, i2, i3, i4));
        }
        boolean z = true;
        if (i2 <= this.maxFrameSize) {
            if ((Integer.MIN_VALUE & i) != 0) {
                z = false;
            }
            if (z) {
                Util.writeMedium(this.sink, i2);
                this.sink.writeByte(i3 & 255);
                this.sink.writeByte(i4 & 255);
                this.sink.writeInt(i & Integer.MAX_VALUE);
                return;
            }
            throw new IllegalArgumentException(Intrinsics.stringPlus("reserved bit set: ", Integer.valueOf(i)).toString());
        }
        throw new IllegalArgumentException(("FRAME_SIZE_ERROR length > " + this.maxFrameSize + ": " + i2).toString());
    }

    public synchronized void close() throws IOException {
        this.closed = true;
        this.sink.close();
    }

    private final void writeContinuationFrames(int i, long j) throws IOException {
        while (j > 0) {
            long min = Math.min((long) this.maxFrameSize, j);
            j -= min;
            frameHeader(i, (int) min, 9, j == 0 ? 4 : 0);
            this.sink.write(this.hpackBuffer, min);
        }
    }

    public final synchronized void headers(boolean z, int i, @NotNull List<Header> list) throws IOException {
        Intrinsics.checkNotNullParameter(list, "headerBlock");
        if (!this.closed) {
            this.hpackWriter.writeHeaders(list);
            long size = this.hpackBuffer.size();
            long min = Math.min((long) this.maxFrameSize, size);
            int i2 = (size > min ? 1 : (size == min ? 0 : -1));
            int i3 = i2 == 0 ? 4 : 0;
            if (z) {
                i3 |= 1;
            }
            frameHeader(i, (int) min, 1, i3);
            this.sink.write(this.hpackBuffer, min);
            if (i2 > 0) {
                writeContinuationFrames(i, size - min);
            }
        } else {
            throw new IOException("closed");
        }
    }

    /* compiled from: Http2Writer.kt */
    public static final class Companion {
        public /* synthetic */ Companion(DefaultConstructorMarker defaultConstructorMarker) {
            this();
        }

        private Companion() {
        }
    }
}
