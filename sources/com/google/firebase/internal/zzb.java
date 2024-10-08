package com.google.firebase.internal;

import android.content.Context;
import com.google.firebase.FirebaseApp;
import java.util.concurrent.atomic.AtomicReference;

public final class zzb {
    private static final AtomicReference<zzb> zzmmz = new AtomicReference<>();

    private zzb(Context context) {
    }

    public static zzb zzfb(Context context) {
        AtomicReference<zzb> atomicReference = zzmmz;
        atomicReference.compareAndSet((Object) null, new zzb(context));
        return atomicReference.get();
    }

    public static void zzg(FirebaseApp firebaseApp) {
    }
}
