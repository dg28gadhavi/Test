package javax.mail.util;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import javax.activation.DataSource;

public class ByteArrayDataSource implements DataSource {
    private byte[] data;
    private int len = -1;
    private String name = "";
    private String type;

    static class DSByteArrayOutputStream extends ByteArrayOutputStream {
        DSByteArrayOutputStream() {
        }

        public byte[] getBuf() {
            return this.buf;
        }

        public int getCount() {
            return this.count;
        }
    }

    public ByteArrayDataSource(InputStream inputStream, String str) throws IOException {
        DSByteArrayOutputStream dSByteArrayOutputStream = new DSByteArrayOutputStream();
        byte[] bArr = new byte[8192];
        while (true) {
            int read = inputStream.read(bArr);
            if (read <= 0) {
                break;
            }
            dSByteArrayOutputStream.write(bArr, 0, read);
        }
        this.data = dSByteArrayOutputStream.getBuf();
        int count = dSByteArrayOutputStream.getCount();
        this.len = count;
        if (this.data.length - count > 262144) {
            byte[] byteArray = dSByteArrayOutputStream.toByteArray();
            this.data = byteArray;
            this.len = byteArray.length;
        }
        this.type = str;
    }

    public ByteArrayDataSource(byte[] bArr, String str) {
        this.data = bArr;
        this.type = str;
    }

    public InputStream getInputStream() throws IOException {
        byte[] bArr = this.data;
        if (bArr != null) {
            if (this.len < 0) {
                this.len = bArr.length;
            }
            return new SharedByteArrayInputStream(this.data, 0, this.len);
        }
        throw new IOException("no data");
    }

    public String getContentType() {
        return this.type;
    }
}
