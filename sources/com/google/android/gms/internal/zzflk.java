package com.google.android.gms.internal;

import com.sec.internal.imscr.LogClass;
import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;

public final class zzflk {
    private final ByteBuffer buffer;

    private zzflk(ByteBuffer byteBuffer) {
        this.buffer = byteBuffer;
        byteBuffer.order(ByteOrder.LITTLE_ENDIAN);
    }

    private zzflk(byte[] bArr, int i, int i2) {
        this(ByteBuffer.wrap(bArr, i, i2));
    }

    public static zzflk zzbf(byte[] bArr) {
        return zzp(bArr, 0, bArr.length);
    }

    public static int zzmf(int i) {
        if ((i & -128) == 0) {
            return 1;
        }
        if ((i & -16384) == 0) {
            return 2;
        }
        if ((-2097152 & i) == 0) {
            return 3;
        }
        return (i & LogClass.GEN_IMS_SERVICE_CREATED) == 0 ? 4 : 5;
    }

    private final void zzmx(int i) throws IOException {
        byte b = (byte) i;
        if (this.buffer.hasRemaining()) {
            this.buffer.put(b);
            return;
        }
        throw new zzfll(this.buffer.position(), this.buffer.limit());
    }

    public static zzflk zzp(byte[] bArr, int i, int i2) {
        return new zzflk(bArr, 0, i2);
    }

    public final void zzbh(byte[] bArr) throws IOException {
        int length = bArr.length;
        if (this.buffer.remaining() >= length) {
            this.buffer.put(bArr, 0, length);
            return;
        }
        throw new zzfll(this.buffer.position(), this.buffer.limit());
    }

    public final void zzmy(int i) throws IOException {
        while ((i & -128) != 0) {
            zzmx((i & 127) | 128);
            i >>>= 7;
        }
        zzmx(i);
    }
}
