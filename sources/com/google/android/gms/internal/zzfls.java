package com.google.android.gms.internal;

import java.io.IOException;

public abstract class zzfls {
    protected volatile int zzpnr = -1;

    public static final <T extends zzfls> T zza(T t, byte[] bArr) throws zzflr {
        return zza(t, bArr, 0, bArr.length);
    }

    private static <T extends zzfls> T zza(T t, byte[] bArr, int i, int i2) throws zzflr {
        try {
            zzflj zzo = zzflj.zzo(bArr, 0, i2);
            t.zza(zzo);
            zzo.zzlf(0);
            return t;
        } catch (zzflr e) {
            throw e;
        } catch (IOException e2) {
            throw new RuntimeException("Reading from a byte array threw an IOException (should never happen).", e2);
        }
    }

    public String toString() {
        return zzflt.zzd(this);
    }

    public abstract zzfls zza(zzflj zzflj) throws IOException;

    /* renamed from: zzdcl */
    public zzfls clone() throws CloneNotSupportedException {
        return (zzfls) super.clone();
    }
}
