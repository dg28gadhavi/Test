package javax.mail.util;

import java.io.ByteArrayInputStream;
import java.io.InputStream;
import javax.mail.internet.SharedInputStream;

public class SharedByteArrayInputStream extends ByteArrayInputStream implements SharedInputStream {
    protected int start;

    public SharedByteArrayInputStream(byte[] bArr) {
        super(bArr);
        this.start = 0;
    }

    public SharedByteArrayInputStream(byte[] bArr, int i, int i2) {
        super(bArr, i, i2);
        this.start = i;
    }

    public long getPosition() {
        return (long) (this.pos - this.start);
    }

    public InputStream newStream(long j, long j2) {
        if (j >= 0) {
            if (j2 == -1) {
                j2 = (long) (this.count - this.start);
            }
            return new SharedByteArrayInputStream(this.buf, this.start + ((int) j), (int) (j2 - j));
        }
        throw new IllegalArgumentException("start < 0");
    }
}
