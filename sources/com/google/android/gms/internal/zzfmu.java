package com.google.android.gms.internal;

import java.io.IOException;

public final class zzfmu extends zzflm<zzfmu> {
    private static volatile zzfmu[] zzpzr;
    public String zzpzs = "";

    public zzfmu() {
        this.zzpvl = null;
        this.zzpnr = -1;
    }

    public static zzfmu[] zzddi() {
        if (zzpzr == null) {
            synchronized (zzflq.zzpvt) {
                if (zzpzr == null) {
                    zzpzr = new zzfmu[0];
                }
            }
        }
        return zzpzr;
    }

    public final /* synthetic */ zzfls zza(zzflj zzflj) throws IOException {
        while (true) {
            int zzcxx = zzflj.zzcxx();
            if (zzcxx == 0) {
                return this;
            }
            if (zzcxx == 10) {
                this.zzpzs = zzflj.readString();
            } else if (!super.zza(zzflj, zzcxx)) {
                return this;
            }
        }
    }
}
