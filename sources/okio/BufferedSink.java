package okio;

import java.io.IOException;
import java.nio.channels.WritableByteChannel;
import org.jetbrains.annotations.NotNull;

/* compiled from: BufferedSink.kt */
public interface BufferedSink extends Sink, WritableByteChannel {
    void flush() throws IOException;

    @NotNull
    Buffer getBuffer();

    @NotNull
    BufferedSink write(@NotNull ByteString byteString) throws IOException;

    @NotNull
    BufferedSink write(@NotNull byte[] bArr) throws IOException;

    @NotNull
    BufferedSink write(@NotNull byte[] bArr, int i, int i2) throws IOException;

    long writeAll(@NotNull Source source) throws IOException;

    @NotNull
    BufferedSink writeByte(int i) throws IOException;

    @NotNull
    BufferedSink writeDecimalLong(long j) throws IOException;

    @NotNull
    BufferedSink writeHexadecimalUnsignedLong(long j) throws IOException;

    @NotNull
    BufferedSink writeInt(int i) throws IOException;

    @NotNull
    BufferedSink writeShort(int i) throws IOException;

    @NotNull
    BufferedSink writeUtf8(@NotNull String str) throws IOException;
}
