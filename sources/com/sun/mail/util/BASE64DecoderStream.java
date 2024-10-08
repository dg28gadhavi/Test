package com.sun.mail.util;

import com.sec.internal.constants.ims.config.ConfigConstants;
import com.sec.internal.ims.core.cmc.CmcConstants;
import java.io.EOFException;
import java.io.FilterInputStream;
import java.io.IOException;
import java.io.InputStream;

public class BASE64DecoderStream extends FilterInputStream {
    private static final char[] pem_array = {'A', 'B', 'C', 'D', 'E', 'F', 'G', 'H', 'I', 'J', 'K', 'L', 'M', 'N', 'O', 'P', 'Q', 'R', 'S', 'T', 'U', 'V', 'W', 'X', 'Y', 'Z', 'a', 'b', 'c', 'd', 'e', 'f', 'g', 'h', 'i', 'j', 'k', 'l', 'm', 'n', 'o', 'p', 'q', 'r', 's', 't', 'u', 'v', 'w', 'x', 'y', 'z', '0', '1', '2', '3', '4', '5', '6', '7', '8', '9', '+', '/'};
    private static final byte[] pem_convert_array = new byte[256];
    private byte[] buffer = new byte[3];
    private int bufsize;
    private boolean ignoreErrors;
    private int index;
    private byte[] input_buffer;
    private int input_len;
    private int input_pos;

    public boolean markSupported() {
        return false;
    }

    public BASE64DecoderStream(InputStream inputStream) {
        super(inputStream);
        boolean z = false;
        this.bufsize = 0;
        this.index = 0;
        this.input_buffer = new byte[8190];
        this.input_pos = 0;
        this.input_len = 0;
        this.ignoreErrors = false;
        try {
            String property = System.getProperty("mail.mime.base64.ignoreerrors");
            if (property != null && !property.equalsIgnoreCase(ConfigConstants.VALUE.INFO_COMPLETED)) {
                z = true;
            }
            this.ignoreErrors = z;
        } catch (SecurityException unused) {
        }
    }

    public int read() throws IOException {
        if (this.index >= this.bufsize) {
            byte[] bArr = this.buffer;
            int decode = decode(bArr, 0, bArr.length);
            this.bufsize = decode;
            if (decode <= 0) {
                return -1;
            }
            this.index = 0;
        }
        byte[] bArr2 = this.buffer;
        int i = this.index;
        this.index = i + 1;
        return bArr2[i] & 255;
    }

    /* JADX WARNING: Removed duplicated region for block: B:6:0x001c  */
    /* Code decompiled incorrectly, please refer to instructions dump. */
    public int read(byte[] r6, int r7, int r8) throws java.io.IOException {
        /*
            r5 = this;
            r0 = r7
        L_0x0001:
            int r1 = r5.index
            int r2 = r5.bufsize
            if (r1 >= r2) goto L_0x001a
            if (r8 > 0) goto L_0x000a
            goto L_0x001a
        L_0x000a:
            int r2 = r0 + 1
            byte[] r3 = r5.buffer
            int r4 = r1 + 1
            r5.index = r4
            byte r1 = r3[r1]
            r6[r0] = r1
            int r8 = r8 + -1
            r0 = r2
            goto L_0x0001
        L_0x001a:
            if (r1 < r2) goto L_0x0021
            r1 = 0
            r5.index = r1
            r5.bufsize = r1
        L_0x0021:
            int r1 = r8 / 3
            int r1 = r1 * 3
            r2 = -1
            if (r1 <= 0) goto L_0x0035
            int r3 = r5.decode(r6, r0, r1)
            int r0 = r0 + r3
            int r8 = r8 - r3
            if (r3 == r1) goto L_0x0035
            if (r0 != r7) goto L_0x0033
            return r2
        L_0x0033:
            int r0 = r0 - r7
            return r0
        L_0x0035:
            if (r8 > 0) goto L_0x0038
            goto L_0x003e
        L_0x0038:
            int r1 = r5.read()
            if (r1 != r2) goto L_0x0043
        L_0x003e:
            if (r0 != r7) goto L_0x0041
            return r2
        L_0x0041:
            int r0 = r0 - r7
            return r0
        L_0x0043:
            int r3 = r0 + 1
            byte r1 = (byte) r1
            r6[r0] = r1
            int r8 = r8 + -1
            r0 = r3
            goto L_0x0035
        */
        throw new UnsupportedOperationException("Method not decompiled: com.sun.mail.util.BASE64DecoderStream.read(byte[], int, int):int");
    }

    public int available() throws IOException {
        return ((this.in.available() * 3) / 4) + (this.bufsize - this.index);
    }

    static {
        int i = 0;
        for (int i2 = 0; i2 < 255; i2++) {
            pem_convert_array[i2] = -1;
        }
        while (true) {
            char[] cArr = pem_array;
            if (i < cArr.length) {
                pem_convert_array[cArr[i]] = (byte) i;
                i++;
            } else {
                return;
            }
        }
    }

    private int decode(byte[] bArr, int i, int i2) throws IOException {
        int i3 = i;
        while (i2 >= 3) {
            boolean z = false;
            int i4 = 0;
            int i5 = 0;
            while (i4 < 4) {
                int i6 = getByte();
                if (i6 == -1 || i6 == -2) {
                    if (i6 == -1) {
                        if (i4 == 0) {
                            return i3 - i;
                        }
                        if (this.ignoreErrors) {
                            z = true;
                        } else {
                            throw new IOException("Error in encoded stream: needed 4 valid base64 characters but only got " + i4 + " before EOF" + recentChars());
                        }
                    } else if (i4 < 2 && !this.ignoreErrors) {
                        throw new IOException("Error in encoded stream: needed at least 2 valid base64 characters, but only got " + i4 + " before padding character (=)" + recentChars());
                    } else if (i4 == 0) {
                        return i3 - i;
                    }
                    int i7 = i4 - 1;
                    if (i7 == 0) {
                        i7 = 1;
                    }
                    int i8 = i5 << 6;
                    for (int i9 = i4 + 1; i9 < 4; i9++) {
                        if (!z) {
                            int i10 = getByte();
                            if (i10 == -1) {
                                if (!this.ignoreErrors) {
                                    throw new IOException("Error in encoded stream: hit EOF while looking for padding characters (=)" + recentChars());
                                }
                            } else if (i10 != -2 && !this.ignoreErrors) {
                                throw new IOException("Error in encoded stream: found valid base64 character after a padding character (=)" + recentChars());
                            }
                        }
                        i8 <<= 6;
                    }
                    int i11 = i8 >> 8;
                    if (i7 == 2) {
                        bArr[i3 + 1] = (byte) (i11 & 255);
                    }
                    bArr[i3] = (byte) ((i11 >> 8) & 255);
                    return (i3 + i7) - i;
                }
                i4++;
                i5 = (i5 << 6) | i6;
            }
            bArr[i3 + 2] = (byte) (i5 & 255);
            int i12 = i5 >> 8;
            bArr[i3 + 1] = (byte) (i12 & 255);
            bArr[i3] = (byte) ((i12 >> 8) & 255);
            i2 -= 3;
            i3 += 3;
        }
        return i3 - i;
    }

    private int getByte() throws IOException {
        byte b;
        do {
            if (this.input_pos >= this.input_len) {
                try {
                    int read = this.in.read(this.input_buffer);
                    this.input_len = read;
                    if (read <= 0) {
                        return -1;
                    }
                    this.input_pos = 0;
                } catch (EOFException unused) {
                    return -1;
                }
            }
            byte[] bArr = this.input_buffer;
            int i = this.input_pos;
            this.input_pos = i + 1;
            byte b2 = bArr[i] & 255;
            if (b2 == 61) {
                return -2;
            }
            b = pem_convert_array[b2];
        } while (b == -1);
        return b;
    }

    private String recentChars() {
        String str;
        int i = this.input_pos;
        if (i > 10) {
            i = 10;
        }
        if (i <= 0) {
            return "";
        }
        String str2 = "" + ", the " + i + " most recent characters were: \"";
        for (int i2 = this.input_pos - i; i2 < this.input_pos; i2++) {
            char c = (char) (this.input_buffer[i2] & 255);
            if (c == 9) {
                str = String.valueOf(str2) + "\\t";
            } else if (c == 10) {
                str = String.valueOf(str2) + "\\n";
            } else if (c == 13) {
                str = String.valueOf(str2) + "\\r";
            } else if (c < ' ' || c >= 127) {
                str = String.valueOf(str2) + "\\" + c;
            } else {
                str = String.valueOf(str2) + c;
            }
            str2 = str;
        }
        return String.valueOf(str2) + CmcConstants.E_NUM_STR_QUOTE;
    }
}
