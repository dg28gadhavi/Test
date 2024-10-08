package com.sec.internal.helper;

import android.util.Log;
import java.util.Arrays;

public class ByteArrayWriter {
    private final byte[] buffer;
    private int curPosition = 0;

    public ByteArrayWriter(int i) {
        if (i > 0) {
            this.buffer = new byte[i];
            return;
        }
        throw new IllegalArgumentException("The length must be greater then 0.");
    }

    public void write(byte[] bArr) {
        if (bArr != null) {
            int length = bArr.length;
            int i = this.curPosition;
            int i2 = i + length;
            byte[] bArr2 = this.buffer;
            if (i2 > bArr2.length) {
                throw new IllegalStateException("The buffer is overflowed.");
            } else if (length > 0) {
                System.arraycopy(bArr, 0, bArr2, i, length);
                this.curPosition += length;
            }
        } else {
            throw new IllegalArgumentException("The item must be not null.");
        }
    }

    public byte[] getResult() {
        Log.v("ByteArrayWriter", toString());
        int i = this.curPosition;
        byte[] bArr = this.buffer;
        if (i == bArr.length) {
            return bArr;
        }
        throw new IllegalStateException("The result is not completed yet.");
    }

    public String toString() {
        return "ByteArrayWriter [buffer=" + Arrays.toString(this.buffer) + ", curPosition=" + this.curPosition + "]";
    }
}
