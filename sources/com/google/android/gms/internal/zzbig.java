package com.google.android.gms.internal;

import android.content.Context;
import android.content.pm.ApplicationInfo;
import android.content.pm.PackageManager;

public final class zzbig {
    private Context mContext;

    public zzbig(Context context) {
        this.mContext = context;
    }

    public final ApplicationInfo getApplicationInfo(String str, int i) throws PackageManager.NameNotFoundException {
        return this.mContext.getPackageManager().getApplicationInfo(str, i);
    }
}
