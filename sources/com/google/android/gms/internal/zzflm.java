package com.google.android.gms.internal;

import com.google.android.gms.internal.zzflm;
import java.io.IOException;

public abstract class zzflm<M extends zzflm<M>> extends zzfls {
    protected zzflo zzpvl;

    /* access modifiers changed from: protected */
    public final boolean zza(zzflj zzflj, int i) throws IOException {
        zzflp zzflp;
        int position = zzflj.getPosition();
        if (!zzflj.zzlg(i)) {
            return false;
        }
        int i2 = i >>> 3;
        zzflu zzflu = new zzflu(i, zzflj.zzao(position, zzflj.getPosition() - position));
        zzflo zzflo = this.zzpvl;
        if (zzflo == null) {
            this.zzpvl = new zzflo();
            zzflp = null;
        } else {
            zzflp = zzflo.zzmz(i2);
        }
        if (zzflp == null) {
            zzflp = new zzflp();
            this.zzpvl.zza(i2, zzflp);
        }
        zzflp.zza(zzflu);
        return true;
    }

    /* renamed from: zzdck */
    public M clone() throws CloneNotSupportedException {
        M m = (zzflm) super.clone();
        zzflq.zza(this, m);
        return m;
    }

    public /* synthetic */ zzfls zzdcl() throws CloneNotSupportedException {
        return (zzflm) clone();
    }
}
