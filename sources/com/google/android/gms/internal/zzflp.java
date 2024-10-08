package com.google.android.gms.internal;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

final class zzflp implements Cloneable {
    private Object value;
    private List<zzflu> zzpvs = new ArrayList();

    zzflp() {
    }

    private final byte[] toByteArray() throws IOException {
        byte[] bArr = new byte[zzq()];
        zza(zzflk.zzbf(bArr));
        return bArr;
    }

    /* access modifiers changed from: private */
    /* renamed from: zzdcm */
    public zzflp clone() {
        Object clone;
        zzflp zzflp = new zzflp();
        try {
            List<zzflu> list = this.zzpvs;
            if (list == null) {
                zzflp.zzpvs = null;
            } else {
                zzflp.zzpvs.addAll(list);
            }
            Object obj = this.value;
            if (obj != null) {
                if (obj instanceof zzfls) {
                    clone = (zzfls) ((zzfls) obj).clone();
                } else if (obj instanceof byte[]) {
                    clone = ((byte[]) obj).clone();
                } else {
                    int i = 0;
                    if (obj instanceof byte[][]) {
                        byte[][] bArr = (byte[][]) obj;
                        byte[][] bArr2 = new byte[bArr.length][];
                        zzflp.value = bArr2;
                        while (i < bArr.length) {
                            bArr2[i] = (byte[]) bArr[i].clone();
                            i++;
                        }
                    } else if (obj instanceof boolean[]) {
                        clone = ((boolean[]) obj).clone();
                    } else if (obj instanceof int[]) {
                        clone = ((int[]) obj).clone();
                    } else if (obj instanceof long[]) {
                        clone = ((long[]) obj).clone();
                    } else if (obj instanceof float[]) {
                        clone = ((float[]) obj).clone();
                    } else if (obj instanceof double[]) {
                        clone = ((double[]) obj).clone();
                    } else if (obj instanceof zzfls[]) {
                        zzfls[] zzflsArr = (zzfls[]) obj;
                        zzfls[] zzflsArr2 = new zzfls[zzflsArr.length];
                        zzflp.value = zzflsArr2;
                        while (i < zzflsArr.length) {
                            zzflsArr2[i] = (zzfls) zzflsArr[i].clone();
                            i++;
                        }
                    }
                }
                zzflp.value = clone;
            }
            return zzflp;
        } catch (CloneNotSupportedException e) {
            throw new AssertionError(e);
        }
    }

    public final boolean equals(Object obj) {
        List<zzflu> list;
        if (obj == this) {
            return true;
        }
        if (!(obj instanceof zzflp)) {
            return false;
        }
        zzflp zzflp = (zzflp) obj;
        if (this.value == null || zzflp.value == null) {
            List<zzflu> list2 = this.zzpvs;
            if (list2 != null && (list = zzflp.zzpvs) != null) {
                return list2.equals(list);
            }
            try {
                return Arrays.equals(toByteArray(), zzflp.toByteArray());
            } catch (IOException e) {
                throw new IllegalStateException(e);
            }
        } else {
            throw null;
        }
    }

    public final int hashCode() {
        try {
            return Arrays.hashCode(toByteArray()) + 527;
        } catch (IOException e) {
            throw new IllegalStateException(e);
        }
    }

    /* access modifiers changed from: package-private */
    public final void zza(zzflk zzflk) throws IOException {
        if (this.value == null) {
            for (zzflu next : this.zzpvs) {
                zzflk.zzmy(next.tag);
                zzflk.zzbh(next.zzjwl);
            }
            return;
        }
        throw null;
    }

    /* access modifiers changed from: package-private */
    public final void zza(zzflu zzflu) {
        this.zzpvs.add(zzflu);
    }

    /* access modifiers changed from: package-private */
    public final int zzq() {
        if (this.value == null) {
            int i = 0;
            for (zzflu next : this.zzpvs) {
                i += zzflk.zzmf(next.tag) + 0 + next.zzjwl.length;
            }
            return i;
        }
        throw null;
    }
}
