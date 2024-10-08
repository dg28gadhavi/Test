package com.sun.mail.util;

import java.io.FilterInputStream;
import java.io.IOException;
import java.io.InputStream;

public class UUDecoderStream extends FilterInputStream {
    private byte[] buffer;
    private int bufsize = 0;
    private boolean gotEnd = false;
    private boolean gotPrefix = false;
    private int index = 0;
    private LineInputStream lin;
    private int mode;
    private String name;

    public boolean markSupported() {
        return false;
    }

    public UUDecoderStream(InputStream inputStream) {
        super(inputStream);
        this.lin = new LineInputStream(inputStream);
        this.buffer = new byte[45];
    }

    public int read() throws IOException {
        if (this.index >= this.bufsize) {
            readPrefix();
            if (!decode()) {
                return -1;
            }
            this.index = 0;
        }
        byte[] bArr = this.buffer;
        int i = this.index;
        this.index = i + 1;
        return bArr[i] & 255;
    }

    public int read(byte[] bArr, int i, int i2) throws IOException {
        int i3 = 0;
        while (i3 < i2) {
            int read = read();
            if (read != -1) {
                bArr[i + i3] = (byte) read;
                i3++;
            } else if (i3 == 0) {
                return -1;
            } else {
                return i3;
            }
        }
        return i3;
    }

    public int available() throws IOException {
        return ((this.in.available() * 3) / 4) + (this.bufsize - this.index);
    }

    private void readPrefix() throws IOException {
        String readLine;
        if (!this.gotPrefix) {
            do {
                readLine = this.lin.readLine();
                if (readLine == null) {
                    throw new IOException("UUDecoder error: No Begin");
                }
            } while (!readLine.regionMatches(true, 0, "begin", 0, 5));
            try {
                this.mode = Integer.parseInt(readLine.substring(6, 9));
                this.name = readLine.substring(10);
                this.gotPrefix = true;
            } catch (NumberFormatException e) {
                throw new IOException("UUDecoder error: " + e.toString());
            }
        }
    }

    private boolean decode() throws IOException {
        String readLine;
        if (this.gotEnd) {
            return false;
        }
        this.bufsize = 0;
        do {
            readLine = this.lin.readLine();
            if (readLine == null) {
                throw new IOException("Missing End");
            } else if (readLine.regionMatches(true, 0, "end", 0, 3)) {
                this.gotEnd = true;
                return false;
            }
        } while (readLine.length() == 0);
        char charAt = readLine.charAt(0);
        if (charAt >= ' ') {
            int i = (charAt - ' ') & 63;
            if (i == 0) {
                String readLine2 = this.lin.readLine();
                if (readLine2 == null || !readLine2.regionMatches(true, 0, "end", 0, 3)) {
                    throw new IOException("Missing End");
                }
                this.gotEnd = true;
                return false;
            } else if (readLine.length() >= (((i * 8) + 5) / 6) + 1) {
                int i2 = 1;
                while (this.bufsize < i) {
                    int i3 = i2 + 1;
                    int i4 = i3 + 1;
                    byte charAt2 = (byte) ((readLine.charAt(i3) - ' ') & 63);
                    byte[] bArr = this.buffer;
                    int i5 = this.bufsize;
                    int i6 = i5 + 1;
                    this.bufsize = i6;
                    bArr[i5] = (byte) (((((byte) ((readLine.charAt(i2) - ' ') & 63)) << 2) & 252) | ((charAt2 >>> 4) & 3));
                    if (i6 < i) {
                        i2 = i4 + 1;
                        byte charAt3 = (byte) ((readLine.charAt(i4) - ' ') & 63);
                        byte[] bArr2 = this.buffer;
                        int i7 = this.bufsize;
                        this.bufsize = i7 + 1;
                        bArr2[i7] = (byte) (((charAt2 << 4) & 240) | ((charAt3 >>> 2) & 15));
                        charAt2 = charAt3;
                    } else {
                        i2 = i4;
                    }
                    if (this.bufsize < i) {
                        byte[] bArr3 = this.buffer;
                        int i8 = this.bufsize;
                        this.bufsize = i8 + 1;
                        bArr3[i8] = (byte) ((((byte) ((readLine.charAt(i2) - ' ') & 63)) & 63) | ((charAt2 << 6) & 192));
                        i2++;
                    }
                }
                return true;
            } else {
                throw new IOException("Short buffer error");
            }
        } else {
            throw new IOException("Buffer format error");
        }
    }
}
