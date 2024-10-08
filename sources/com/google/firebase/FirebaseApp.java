package com.google.firebase;

import android.annotation.TargetApi;
import android.app.Application;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.support.v4.content.ContextCompat;
import android.support.v4.util.ArrayMap;
import android.util.Log;
import com.google.android.gms.common.api.internal.zzk;
import com.google.android.gms.common.api.internal.zzl;
import com.google.android.gms.common.internal.zzbg;
import com.google.android.gms.common.internal.zzbq;
import com.google.android.gms.common.util.zzu;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicReference;

public class FirebaseApp {
    /* access modifiers changed from: private */
    public static final Object sLock = new Object();
    static final Map<String, FirebaseApp> zzimu = new ArrayMap();
    private static final List<String> zzmmm = Arrays.asList(new String[]{"com.google.firebase.auth.FirebaseAuth", "com.google.firebase.iid.FirebaseInstanceId"});
    private static final List<String> zzmmn = Collections.singletonList("com.google.firebase.crash.FirebaseCrash");
    private static final List<String> zzmmo = Arrays.asList(new String[]{"com.google.android.gms.measurement.AppMeasurement"});
    private static final List<String> zzmmp = Arrays.asList(new String[0]);
    private static final Set<String> zzmmq = Collections.emptySet();
    private final Context mApplicationContext;
    private final String mName;
    private final FirebaseOptions zzmmr;
    private final AtomicBoolean zzmms = new AtomicBoolean(false);
    private final AtomicBoolean zzmmt = new AtomicBoolean();
    private final List<Object> zzmmu = new CopyOnWriteArrayList();
    private final List<zza> zzmmv = new CopyOnWriteArrayList();
    private final List<Object> zzmmw = new CopyOnWriteArrayList();
    private zzb zzmmy;

    public interface zza {
        void zzbj(boolean z);
    }

    public interface zzb {
    }

    @TargetApi(24)
    static class zzc extends BroadcastReceiver {
        private static AtomicReference<zzc> zzmmz = new AtomicReference<>();
        private final Context mApplicationContext;

        private zzc(Context context) {
            this.mApplicationContext = context;
        }

        /* access modifiers changed from: private */
        public static void zzew(Context context) {
            if (zzmmz.get() == null) {
                zzc zzc = new zzc(context);
                if (zzmmz.compareAndSet((Object) null, zzc)) {
                    context.registerReceiver(zzc, new IntentFilter("android.intent.action.USER_UNLOCKED"));
                }
            }
        }

        public final void onReceive(Context context, Intent intent) {
            synchronized (FirebaseApp.sLock) {
                for (FirebaseApp zza : FirebaseApp.zzimu.values()) {
                    zza.zzbsx();
                }
            }
            this.mApplicationContext.unregisterReceiver(this);
        }
    }

    private FirebaseApp(Context context, String str, FirebaseOptions firebaseOptions) {
        this.mApplicationContext = (Context) zzbq.checkNotNull(context);
        this.mName = zzbq.zzgv(str);
        this.zzmmr = (FirebaseOptions) zzbq.checkNotNull(firebaseOptions);
        this.zzmmy = new com.google.firebase.internal.zza();
    }

    public static FirebaseApp getInstance() {
        FirebaseApp firebaseApp;
        synchronized (sLock) {
            firebaseApp = zzimu.get("[DEFAULT]");
            if (firebaseApp == null) {
                String zzany = zzu.zzany();
                StringBuilder sb = new StringBuilder(String.valueOf(zzany).length() + 116);
                sb.append("Default FirebaseApp is not initialized in this process ");
                sb.append(zzany);
                sb.append(". Make sure to call FirebaseApp.initializeApp(Context) first.");
                throw new IllegalStateException(sb.toString());
            }
        }
        return firebaseApp;
    }

    public static FirebaseApp initializeApp(Context context, FirebaseOptions firebaseOptions) {
        return initializeApp(context, firebaseOptions, "[DEFAULT]");
    }

    public static FirebaseApp initializeApp(Context context, FirebaseOptions firebaseOptions, String str) {
        FirebaseApp firebaseApp;
        com.google.firebase.internal.zzb.zzfb(context);
        if (context.getApplicationContext() instanceof Application) {
            zzk.zza((Application) context.getApplicationContext());
            zzk.zzaij().zza((zzl) new zza());
        }
        String trim = str.trim();
        if (context.getApplicationContext() != null) {
            context = context.getApplicationContext();
        }
        synchronized (sLock) {
            Map<String, FirebaseApp> map = zzimu;
            boolean z = !map.containsKey(trim);
            StringBuilder sb = new StringBuilder(String.valueOf(trim).length() + 33);
            sb.append("FirebaseApp name ");
            sb.append(trim);
            sb.append(" already exists!");
            zzbq.zza(z, sb.toString());
            zzbq.checkNotNull(context, "Application context cannot be null.");
            firebaseApp = new FirebaseApp(context, trim, firebaseOptions);
            map.put(trim, firebaseApp);
        }
        com.google.firebase.internal.zzb.zzg(firebaseApp);
        firebaseApp.zza(FirebaseApp.class, firebaseApp, zzmmm);
        if (firebaseApp.zzbsu()) {
            firebaseApp.zza(FirebaseApp.class, firebaseApp, zzmmn);
            firebaseApp.zza(Context.class, firebaseApp.getApplicationContext(), zzmmo);
        }
        return firebaseApp;
    }

    private final <T> void zza(Class<T> cls, T t, Iterable<String> iterable) {
        boolean isDeviceProtectedStorage = ContextCompat.isDeviceProtectedStorage(this.mApplicationContext);
        if (isDeviceProtectedStorage) {
            zzc.zzew(this.mApplicationContext);
        }
        for (String next : iterable) {
            if (isDeviceProtectedStorage) {
                try {
                    if (!zzmmp.contains(next)) {
                    }
                } catch (ClassNotFoundException unused) {
                    if (!zzmmq.contains(next)) {
                        Log.d("FirebaseApp", String.valueOf(next).concat(" is not linked. Skipping initialization."));
                    } else {
                        throw new IllegalStateException(String.valueOf(next).concat(" is missing, but is required. Check if it has been removed by Proguard."));
                    }
                } catch (NoSuchMethodException unused2) {
                    throw new IllegalStateException(String.valueOf(next).concat("#getInstance has been removed by Proguard. Add keep rule to prevent it."));
                } catch (InvocationTargetException e) {
                    Log.wtf("FirebaseApp", "Firebase API initialization failure.", e);
                } catch (IllegalAccessException e2) {
                    String valueOf = String.valueOf(next);
                    Log.wtf("FirebaseApp", valueOf.length() != 0 ? "Failed to initialize ".concat(valueOf) : new String("Failed to initialize "), e2);
                }
            }
            Method method = Class.forName(next).getMethod("getInstance", new Class[]{cls});
            int modifiers = method.getModifiers();
            if (Modifier.isPublic(modifiers) && Modifier.isStatic(modifiers)) {
                method.invoke((Object) null, new Object[]{t});
            }
        }
    }

    public static void zzbj(boolean z) {
        synchronized (sLock) {
            ArrayList arrayList = new ArrayList(zzimu.values());
            int size = arrayList.size();
            int i = 0;
            while (i < size) {
                Object obj = arrayList.get(i);
                i++;
                FirebaseApp firebaseApp = (FirebaseApp) obj;
                if (firebaseApp.zzmms.get()) {
                    firebaseApp.zzci(z);
                }
            }
        }
    }

    private final void zzbst() {
        zzbq.zza(!this.zzmmt.get(), "FirebaseApp was deleted");
    }

    /* access modifiers changed from: private */
    public final void zzbsx() {
        Class<FirebaseApp> cls = FirebaseApp.class;
        zza(cls, this, zzmmm);
        if (zzbsu()) {
            zza(cls, this, zzmmn);
            zza(Context.class, this.mApplicationContext, zzmmo);
        }
    }

    private final void zzci(boolean z) {
        Log.d("FirebaseApp", "Notifying background state change listeners.");
        for (zza zzbj : this.zzmmv) {
            zzbj.zzbj(z);
        }
    }

    public boolean equals(Object obj) {
        if (!(obj instanceof FirebaseApp)) {
            return false;
        }
        return this.mName.equals(((FirebaseApp) obj).getName());
    }

    public Context getApplicationContext() {
        zzbst();
        return this.mApplicationContext;
    }

    public String getName() {
        zzbst();
        return this.mName;
    }

    public FirebaseOptions getOptions() {
        zzbst();
        return this.zzmmr;
    }

    public int hashCode() {
        return this.mName.hashCode();
    }

    public String toString() {
        return zzbg.zzx(this).zzg("name", this.mName).zzg("options", this.zzmmr).toString();
    }

    public final boolean zzbsu() {
        return "[DEFAULT]".equals(getName());
    }
}
