package com.google.android.gms.common.api.internal;

import android.app.Activity;
import android.app.Application;
import android.content.ComponentCallbacks2;
import android.content.res.Configuration;
import android.os.Bundle;
import java.util.ArrayList;
import java.util.concurrent.atomic.AtomicBoolean;

public final class zzk implements Application.ActivityLifecycleCallbacks, ComponentCallbacks2 {
    private static final zzk zzfuo = new zzk();
    private boolean zzdyq = false;
    private final AtomicBoolean zzfup = new AtomicBoolean();
    private final AtomicBoolean zzfuq = new AtomicBoolean();
    private final ArrayList<zzl> zzfur = new ArrayList<>();

    private zzk() {
    }

    public static void zza(Application application) {
        zzk zzk = zzfuo;
        synchronized (zzk) {
            if (!zzk.zzdyq) {
                application.registerActivityLifecycleCallbacks(zzk);
                application.registerComponentCallbacks(zzk);
                zzk.zzdyq = true;
            }
        }
    }

    public static zzk zzaij() {
        return zzfuo;
    }

    private final void zzbj(boolean z) {
        synchronized (zzfuo) {
            ArrayList<zzl> arrayList = this.zzfur;
            int size = arrayList.size();
            int i = 0;
            while (i < size) {
                zzl zzl = arrayList.get(i);
                i++;
                zzl.zzbj(z);
            }
        }
    }

    public final void onActivityCreated(Activity activity, Bundle bundle) {
        boolean compareAndSet = this.zzfup.compareAndSet(true, false);
        this.zzfuq.set(true);
        if (compareAndSet) {
            zzbj(false);
        }
    }

    public final void onActivityDestroyed(Activity activity) {
    }

    public final void onActivityPaused(Activity activity) {
    }

    public final void onActivityResumed(Activity activity) {
        boolean compareAndSet = this.zzfup.compareAndSet(true, false);
        this.zzfuq.set(true);
        if (compareAndSet) {
            zzbj(false);
        }
    }

    public final void onActivitySaveInstanceState(Activity activity, Bundle bundle) {
    }

    public final void onActivityStarted(Activity activity) {
    }

    public final void onActivityStopped(Activity activity) {
    }

    public final void onConfigurationChanged(Configuration configuration) {
    }

    public final void onLowMemory() {
    }

    public final void onTrimMemory(int i) {
        if (i == 20 && this.zzfup.compareAndSet(false, true)) {
            this.zzfuq.set(true);
            zzbj(true);
        }
    }

    public final void zza(zzl zzl) {
        synchronized (zzfuo) {
            this.zzfur.add(zzl);
        }
    }
}
