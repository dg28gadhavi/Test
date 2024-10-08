package okio;

import java.io.IOException;
import java.nio.channels.ReadableByteChannel;
import org.jetbrains.annotations.NotNull;

/* compiled from: BufferedSource.kt */
public interface BufferedSource extends Source, ReadableByteChannel {
    boolean exhausted() throws IOException;

    @NotNull
    Buffer getBuffer();

    byte readByte() throws IOException;

    @NotNull
    byte[] readByteArray() throws IOException;

    @NotNull
    byte[] readByteArray(long j) throws IOException;

    @NotNull
    ByteString readByteString(long j) throws IOException;

    long readHexadecimalUnsignedLong() throws IOException;

    int readInt() throws IOException;

    short readShort() throws IOException;

    @NotNull
    String readUtf8LineStrict() throws IOException;

    @NotNull
    String readUtf8LineStrict(long j) throws IOException;

    void require(long j) throws IOException;

    void skip(long j) throws IOException;
}
