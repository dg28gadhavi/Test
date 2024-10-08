package com.google.android.gms.common.util;

import android.content.Context;
import android.content.pm.PackageManager;
import com.google.android.gms.internal.zzbih;

public final class zzd {
    public static boolean zzv(Context context, String str) {
        "com.google.android.gms".equals(str);
        try {
            return (zzbih.zzdd(context).getApplicationInfo(str, 0).flags & 2097152) != 0;
        } catch (PackageManager.NameNotFoundException unused) {
        }
    }
}
