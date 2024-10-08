package com.google.android.gms.internal;

import android.os.Bundle;
import android.os.Parcel;

public final class zzbgo {
    public static void zza(Parcel parcel, int i, Bundle bundle, boolean z) {
        if (bundle != null) {
            int zzag = zzag(parcel, i);
            parcel.writeBundle(bundle);
            zzah(parcel, zzag);
        }
    }

    private static int zzag(Parcel parcel, int i) {
        parcel.writeInt(i | -65536);
        parcel.writeInt(0);
        return parcel.dataPosition();
    }

    private static void zzah(Parcel parcel, int i) {
        int dataPosition = parcel.dataPosition();
        parcel.setDataPosition(i - 4);
        parcel.writeInt(dataPosition - i);
        parcel.setDataPosition(dataPosition);
    }

    public static void zzai(Parcel parcel, int i) {
        zzah(parcel, i);
    }

    public static int zze(Parcel parcel) {
        return zzag(parcel, 20293);
    }
}
