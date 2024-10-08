package com.sec.internal.ims.config.util;

import java.util.Arrays;

public class AkaResponse {
    private final byte[] auts;
    private final byte[] ck;
    private final byte[] ik;
    private final byte[] res;

    public AkaResponse(byte[] bArr, byte[] bArr2, byte[] bArr3, byte[] bArr4) {
        this.ck = bArr;
        this.ik = bArr2;
        this.auts = bArr3;
        this.res = bArr4;
    }

    public byte[] getCk() {
        return this.ck;
    }

    public byte[] getIk() {
        return this.ik;
    }

    public byte[] getAuts() {
        return this.auts;
    }

    public byte[] getRes() {
        return this.res;
    }

    public String toString() {
        return "AkaResponse [ck=" + Arrays.toString(this.ck) + ", ik=" + Arrays.toString(this.ik) + ", auts=" + Arrays.toString(this.auts) + ", res=" + Arrays.toString(this.res) + "]";
    }
}
