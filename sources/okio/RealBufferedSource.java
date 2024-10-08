package okio;

import java.io.EOFException;
import java.nio.ByteBuffer;
import kotlin.jvm.internal.Intrinsics;
import okio.internal._BufferKt;
import org.jetbrains.annotations.NotNull;

/* compiled from: RealBufferedSource.kt */
public final class RealBufferedSource implements BufferedSource {
    @NotNull
    public final Buffer bufferField = new Buffer();
    public boolean closed;
    @NotNull
    public final Source source;

    public long read(@NotNull Buffer buffer, long j) {
        Intrinsics.checkNotNullParameter(buffer, "sink");
        if (!(j >= 0)) {
            throw new IllegalArgumentException(Intrinsics.stringPlus("byteCount < 0: ", Long.valueOf(j)).toString());
        } else if (!(!this.closed)) {
            throw new IllegalStateException("closed".toString());
        } else if (this.bufferField.size() == 0 && this.source.read(this.bufferField, 8192) == -1) {
            return -1;
        } else {
            return this.bufferField.read(buffer, Math.min(j, this.bufferField.size()));
        }
    }

    public boolean exhausted() {
        if (!(!this.closed)) {
            throw new IllegalStateException("closed".toString());
        } else if (!this.bufferField.exhausted() || this.source.read(this.bufferField, 8192) != -1) {
            return false;
        } else {
            return true;
        }
    }

    public RealBufferedSource(@NotNull Source source2) {
        Intrinsics.checkNotNullParameter(source2, "source");
        this.source = source2;
    }

    public void require(long j) {
        if (!request(j)) {
            throw new EOFException();
        }
    }

    public boolean request(long j) {
        if (!(j >= 0)) {
            throw new IllegalArgumentException(Intrinsics.stringPlus("byteCount < 0: ", Long.valueOf(j)).toString());
        } else if (!this.closed) {
            while (this.bufferField.size() < j) {
                if (this.source.read(this.bufferField, 8192) == -1) {
                    return false;
                }
            }
            return true;
        } else {
            throw new IllegalStateException("closed".toString());
        }
    }

    @NotNull
    public Buffer getBuffer() {
        return this.bufferField;
    }

    public int read(@NotNull ByteBuffer byteBuffer) {
        Intrinsics.checkNotNullParameter(byteBuffer, "sink");
        if (this.bufferField.size() == 0 && this.source.read(this.bufferField, 8192) == -1) {
            return -1;
        }
        return this.bufferField.read(byteBuffer);
    }

    @NotNull
    public byte[] readByteArray() {
        this.bufferField.writeAll(this.source);
        return this.bufferField.readByteArray();
    }

    public byte readByte() {
        require(1);
        return this.bufferField.readByte();
    }

    @NotNull
    public ByteString readByteString(long j) {
        require(j);
        return this.bufferField.readByteString(j);
    }

    @NotNull
    public String readUtf8LineStrict() {
        return readUtf8LineStrict(Long.MAX_VALUE);
    }

    @NotNull
    public byte[] readByteArray(long j) {
        require(j);
        return this.bufferField.readByteArray(j);
    }

    public long indexOf(byte b) {
        return indexOf(b, 0, Long.MAX_VALUE);
    }

    public boolean isOpen() {
        return !this.closed;
    }

    @NotNull
    public String readUtf8LineStrict(long j) {
        if (j >= 0) {
            long j2 = j == Long.MAX_VALUE ? Long.MAX_VALUE : j + 1;
            byte b = (byte) 10;
            long indexOf = indexOf(b, 0, j2);
            if (indexOf != -1) {
                return _BufferKt.readUtf8Line(this.bufferField, indexOf);
            }
            if (j2 < Long.MAX_VALUE && request(j2) && this.bufferField.getByte(j2 - 1) == ((byte) 13) && request(1 + j2) && this.bufferField.getByte(j2) == b) {
                return _BufferKt.readUtf8Line(this.bufferField, j2);
            }
            Buffer buffer = new Buffer();
            Buffer buffer2 = this.bufferField;
            buffer2.copyTo(buffer, 0, Math.min((long) 32, buffer2.size()));
            throw new EOFException("\\n not found: limit=" + Math.min(this.bufferField.size(), j) + " content=" + buffer.readByteString().hex() + 8230);
        }
        throw new IllegalArgumentException(Intrinsics.stringPlus("limit < 0: ", Long.valueOf(j)).toString());
    }

    public short readShort() {
        require(2);
        return this.bufferField.readShort();
    }

    public short readShortLe() {
        require(2);
        return this.bufferField.readShortLe();
    }

    public int readInt() {
        require(4);
        return this.bufferField.readInt();
    }

    public int readIntLe() {
        require(4);
        return this.bufferField.readIntLe();
    }

    /* JADX WARNING: Removed duplicated region for block: B:17:0x003a  */
    /* Code decompiled incorrectly, please refer to instructions dump. */
    public long readHexadecimalUnsignedLong() {
        /*
            r5 = this;
            r0 = 1
            r5.require(r0)
            r0 = 0
        L_0x0006:
            int r1 = r0 + 1
            long r2 = (long) r1
            boolean r2 = r5.request(r2)
            if (r2 == 0) goto L_0x0059
            okio.Buffer r2 = r5.bufferField
            long r3 = (long) r0
            byte r2 = r2.getByte(r3)
            r3 = 48
            byte r3 = (byte) r3
            if (r2 < r3) goto L_0x0020
            r3 = 57
            byte r3 = (byte) r3
            if (r2 <= r3) goto L_0x0035
        L_0x0020:
            r3 = 97
            byte r3 = (byte) r3
            if (r2 < r3) goto L_0x002a
            r3 = 102(0x66, float:1.43E-43)
            byte r3 = (byte) r3
            if (r2 <= r3) goto L_0x0035
        L_0x002a:
            r3 = 65
            byte r3 = (byte) r3
            if (r2 < r3) goto L_0x0037
            r3 = 70
            byte r3 = (byte) r3
            if (r2 <= r3) goto L_0x0035
            goto L_0x0037
        L_0x0035:
            r0 = r1
            goto L_0x0006
        L_0x0037:
            if (r0 == 0) goto L_0x003a
            goto L_0x0059
        L_0x003a:
            java.lang.NumberFormatException r5 = new java.lang.NumberFormatException
            r0 = 16
            int r0 = kotlin.text.CharsKt__CharJVMKt.checkRadix(r0)
            int r0 = kotlin.text.CharsKt__CharJVMKt.checkRadix(r0)
            java.lang.String r0 = java.lang.Integer.toString(r2, r0)
            java.lang.String r1 = "java.lang.Integer.toStri…(this, checkRadix(radix))"
            kotlin.jvm.internal.Intrinsics.checkNotNullExpressionValue(r0, r1)
            java.lang.String r1 = "Expected leading [0-9a-fA-F] character but was 0x"
            java.lang.String r0 = kotlin.jvm.internal.Intrinsics.stringPlus(r1, r0)
            r5.<init>(r0)
            throw r5
        L_0x0059:
            okio.Buffer r5 = r5.bufferField
            long r0 = r5.readHexadecimalUnsignedLong()
            return r0
        */
        throw new UnsupportedOperationException("Method not decompiled: okio.RealBufferedSource.readHexadecimalUnsignedLong():long");
    }

    public void skip(long j) {
        if (!this.closed) {
            while (j > 0) {
                if (this.bufferField.size() == 0 && this.source.read(this.bufferField, 8192) == -1) {
                    throw new EOFException();
                }
                long min = Math.min(j, this.bufferField.size());
                this.bufferField.skip(min);
                j -= min;
            }
            return;
        }
        throw new IllegalStateException("closed".toString());
    }

    public long indexOf(byte b, long j, long j2) {
        boolean z = true;
        if (!this.closed) {
            if (0 > j || j > j2) {
                z = false;
            }
            if (z) {
                while (j < j2) {
                    long indexOf = this.bufferField.indexOf(b, j, j2);
                    if (indexOf != -1) {
                        return indexOf;
                    }
                    long size = this.bufferField.size();
                    if (size >= j2 || this.source.read(this.bufferField, 8192) == -1) {
                        return -1;
                    }
                    j = Math.max(j, size);
                }
                return -1;
            }
            throw new IllegalArgumentException(("fromIndex=" + j + " toIndex=" + j2).toString());
        }
        throw new IllegalStateException("closed".toString());
    }

    public void close() {
        if (!this.closed) {
            this.closed = true;
            this.source.close();
            this.bufferField.clear();
        }
    }

    @NotNull
    public Timeout timeout() {
        return this.source.timeout();
    }

    @NotNull
    public String toString() {
        return "buffer(" + this.source + ')';
    }
}
