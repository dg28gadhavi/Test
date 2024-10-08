package com.google.android.gms.internal;

import com.sec.internal.ims.core.handler.secims.imsCommonStruc.MNO;
import java.io.IOException;

public final class zzfmv extends zzflm<zzfmv> {
    public long zzgoc = 0;
    public String zzpzs = "";
    public String zzpzt = "";
    public long zzpzu = 0;
    public String zzpzv = "";
    public long zzpzw = 0;
    public String zzpzx = "";
    public String zzpzy = "";
    public String zzpzz = "";
    public String zzqaa = "";
    public String zzqab = "";
    public int zzqac = 0;
    public zzfmu[] zzqad = zzfmu.zzddi();

    public zzfmv() {
        this.zzpvl = null;
        this.zzpnr = -1;
    }

    public static zzfmv zzbi(byte[] bArr) throws zzflr {
        return (zzfmv) zzfls.zza(new zzfmv(), bArr);
    }

    public final /* synthetic */ zzfls zza(zzflj zzflj) throws IOException {
        while (true) {
            int zzcxx = zzflj.zzcxx();
            switch (zzcxx) {
                case 0:
                    return this;
                case 10:
                    this.zzpzs = zzflj.readString();
                    break;
                case 18:
                    this.zzpzt = zzflj.readString();
                    break;
                case 24:
                    this.zzpzu = zzflj.zzcxz();
                    break;
                case 34:
                    this.zzpzv = zzflj.readString();
                    break;
                case 40:
                    this.zzpzw = zzflj.zzcxz();
                    break;
                case 48:
                    this.zzgoc = zzflj.zzcxz();
                    break;
                case 58:
                    this.zzpzx = zzflj.readString();
                    break;
                case 66:
                    this.zzpzy = zzflj.readString();
                    break;
                case 74:
                    this.zzpzz = zzflj.readString();
                    break;
                case 82:
                    this.zzqaa = zzflj.readString();
                    break;
                case MNO.DLOG /*90*/:
                    this.zzqab = zzflj.readString();
                    break;
                case 96:
                    this.zzqac = zzflj.zzcya();
                    break;
                case 106:
                    int zzb = zzflv.zzb(zzflj, 106);
                    zzfmu[] zzfmuArr = this.zzqad;
                    int length = zzfmuArr == null ? 0 : zzfmuArr.length;
                    int i = zzb + length;
                    zzfmu[] zzfmuArr2 = new zzfmu[i];
                    if (length != 0) {
                        System.arraycopy(zzfmuArr, 0, zzfmuArr2, 0, length);
                    }
                    while (length < i - 1) {
                        zzfmu zzfmu = new zzfmu();
                        zzfmuArr2[length] = zzfmu;
                        zzflj.zza(zzfmu);
                        zzflj.zzcxx();
                        length++;
                    }
                    zzfmu zzfmu2 = new zzfmu();
                    zzfmuArr2[length] = zzfmu2;
                    zzflj.zza(zzfmu2);
                    this.zzqad = zzfmuArr2;
                    break;
                default:
                    if (super.zza(zzflj, zzcxx)) {
                        break;
                    } else {
                        return this;
                    }
            }
        }
    }
}
