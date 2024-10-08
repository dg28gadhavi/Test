package com.google.firebase.iid;

import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.os.Looper;
import android.os.Message;
import android.os.Messenger;
import android.os.Parcelable;
import android.support.v4.util.SimpleArrayMap;
import android.util.Log;
import com.google.android.gms.tasks.TaskCompletionSource;
import com.google.android.gms.tasks.Tasks;
import com.google.firebase.iid.zzi;
import com.sec.internal.constants.tapi.UserConsentProviderContract;
import java.io.IOException;
import java.util.concurrent.ExecutionException;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

final class zzx {
    private static PendingIntent zzikb;
    private static int zzino;
    private final Context zzaiq;
    private Messenger zzikf;
    private Messenger zziny;
    private final zzw zzokq;
    private final SimpleArrayMap<String, TaskCompletionSource<Bundle>> zzolg = new SimpleArrayMap<>();
    private zzi zzolh;

    public zzx(Context context, zzw zzw) {
        this.zzaiq = context;
        this.zzokq = zzw;
        this.zzikf = new Messenger(new zzy(this, Looper.getMainLooper()));
    }

    /*  JADX ERROR: IndexOutOfBoundsException in pass: RegionMakerVisitor
        java.lang.IndexOutOfBoundsException: Index: 0, Size: 0
        	at java.util.ArrayList.rangeCheck(ArrayList.java:659)
        	at java.util.ArrayList.get(ArrayList.java:435)
        	at jadx.core.dex.nodes.InsnNode.getArg(InsnNode.java:101)
        	at jadx.core.dex.visitors.regions.RegionMaker.traverseMonitorExits(RegionMaker.java:611)
        	at jadx.core.dex.visitors.regions.RegionMaker.traverseMonitorExits(RegionMaker.java:619)
        	at jadx.core.dex.visitors.regions.RegionMaker.traverseMonitorExits(RegionMaker.java:619)
        	at jadx.core.dex.visitors.regions.RegionMaker.traverseMonitorExits(RegionMaker.java:619)
        	at jadx.core.dex.visitors.regions.RegionMaker.processMonitorEnter(RegionMaker.java:561)
        	at jadx.core.dex.visitors.regions.RegionMaker.traverse(RegionMaker.java:133)
        	at jadx.core.dex.visitors.regions.RegionMaker.makeRegion(RegionMaker.java:86)
        	at jadx.core.dex.visitors.regions.RegionMaker.processIf(RegionMaker.java:693)
        	at jadx.core.dex.visitors.regions.RegionMaker.traverse(RegionMaker.java:123)
        	at jadx.core.dex.visitors.regions.RegionMaker.makeRegion(RegionMaker.java:86)
        	at jadx.core.dex.visitors.regions.RegionMakerVisitor.visit(RegionMakerVisitor.java:49)
        */
    private final android.os.Bundle zzaa(android.os.Bundle r9) throws java.io.IOException {
        /*
            r8 = this;
            java.lang.String r0 = zzawx()
            com.google.android.gms.tasks.TaskCompletionSource r1 = new com.google.android.gms.tasks.TaskCompletionSource
            r1.<init>()
            android.support.v4.util.SimpleArrayMap<java.lang.String, com.google.android.gms.tasks.TaskCompletionSource<android.os.Bundle>> r2 = r8.zzolg
            monitor-enter(r2)
            android.support.v4.util.SimpleArrayMap<java.lang.String, com.google.android.gms.tasks.TaskCompletionSource<android.os.Bundle>> r3 = r8.zzolg     // Catch:{ all -> 0x011e }
            r3.put(r0, r1)     // Catch:{ all -> 0x011e }
            monitor-exit(r2)     // Catch:{ all -> 0x011e }
            com.google.firebase.iid.zzw r2 = r8.zzokq
            int r2 = r2.zzcll()
            if (r2 == 0) goto L_0x0116
            android.content.Intent r2 = new android.content.Intent
            r2.<init>()
            java.lang.String r3 = "com.google.android.gms"
            r2.setPackage(r3)
            com.google.firebase.iid.zzw r3 = r8.zzokq
            int r3 = r3.zzcll()
            r4 = 2
            if (r3 != r4) goto L_0x0030
            java.lang.String r3 = "com.google.iid.TOKEN_REQUEST"
            goto L_0x0032
        L_0x0030:
            java.lang.String r3 = "com.google.android.c2dm.intent.REGISTER"
        L_0x0032:
            r2.setAction(r3)
            r2.putExtras(r9)
            android.content.Context r9 = r8.zzaiq
            zzd(r9, r2)
            java.lang.String r9 = "kid"
            java.lang.String r3 = java.lang.String.valueOf(r0)
            int r3 = r3.length()
            int r3 = r3 + 5
            java.lang.StringBuilder r5 = new java.lang.StringBuilder
            r5.<init>(r3)
            java.lang.String r3 = "|ID|"
            r5.append(r3)
            r5.append(r0)
            java.lang.String r3 = "|"
            r5.append(r3)
            java.lang.String r3 = r5.toString()
            r2.putExtra(r9, r3)
            java.lang.String r9 = "FirebaseInstanceId"
            r3 = 3
            boolean r9 = android.util.Log.isLoggable(r9, r3)
            if (r9 == 0) goto L_0x0091
            java.lang.String r9 = "FirebaseInstanceId"
            android.os.Bundle r5 = r2.getExtras()
            java.lang.String r5 = java.lang.String.valueOf(r5)
            int r6 = r5.length()
            int r6 = r6 + 8
            java.lang.StringBuilder r7 = new java.lang.StringBuilder
            r7.<init>(r6)
            java.lang.String r6 = "Sending "
            r7.append(r6)
            r7.append(r5)
            java.lang.String r5 = r7.toString()
            android.util.Log.d(r9, r5)
        L_0x0091:
            java.lang.String r9 = "google.messenger"
            android.os.Messenger r5 = r8.zzikf
            r2.putExtra(r9, r5)
            android.os.Messenger r9 = r8.zziny
            if (r9 != 0) goto L_0x00a0
            com.google.firebase.iid.zzi r9 = r8.zzolh
            if (r9 == 0) goto L_0x00c3
        L_0x00a0:
            android.os.Message r9 = android.os.Message.obtain()
            r9.obj = r2
            android.os.Messenger r5 = r8.zziny     // Catch:{ RemoteException -> 0x00b4 }
            if (r5 == 0) goto L_0x00ae
            r5.send(r9)     // Catch:{ RemoteException -> 0x00b4 }
            goto L_0x00d6
        L_0x00ae:
            com.google.firebase.iid.zzi r5 = r8.zzolh     // Catch:{ RemoteException -> 0x00b4 }
            r5.send(r9)     // Catch:{ RemoteException -> 0x00b4 }
            goto L_0x00d6
        L_0x00b4:
            java.lang.String r9 = "FirebaseInstanceId"
            boolean r9 = android.util.Log.isLoggable(r9, r3)
            if (r9 == 0) goto L_0x00c3
            java.lang.String r9 = "FirebaseInstanceId"
            java.lang.String r3 = "Messenger failed, fallback to startService"
            android.util.Log.d(r9, r3)
        L_0x00c3:
            com.google.firebase.iid.zzw r9 = r8.zzokq
            int r9 = r9.zzcll()
            if (r9 != r4) goto L_0x00d1
            android.content.Context r9 = r8.zzaiq
            r9.sendBroadcast(r2)
            goto L_0x00d6
        L_0x00d1:
            android.content.Context r9 = r8.zzaiq
            r9.startService(r2)
        L_0x00d6:
            com.google.android.gms.tasks.Task r9 = r1.getTask()     // Catch:{ InterruptedException | TimeoutException -> 0x00fa, ExecutionException -> 0x00f3 }
            java.util.concurrent.TimeUnit r1 = java.util.concurrent.TimeUnit.MILLISECONDS     // Catch:{ InterruptedException | TimeoutException -> 0x00fa, ExecutionException -> 0x00f3 }
            r2 = 30000(0x7530, double:1.4822E-319)
            java.lang.Object r9 = com.google.android.gms.tasks.Tasks.await(r9, r2, r1)     // Catch:{ InterruptedException | TimeoutException -> 0x00fa, ExecutionException -> 0x00f3 }
            android.os.Bundle r9 = (android.os.Bundle) r9     // Catch:{ InterruptedException | TimeoutException -> 0x00fa, ExecutionException -> 0x00f3 }
            android.support.v4.util.SimpleArrayMap<java.lang.String, com.google.android.gms.tasks.TaskCompletionSource<android.os.Bundle>> r1 = r8.zzolg
            monitor-enter(r1)
            android.support.v4.util.SimpleArrayMap<java.lang.String, com.google.android.gms.tasks.TaskCompletionSource<android.os.Bundle>> r8 = r8.zzolg     // Catch:{ all -> 0x00ee }
            r8.remove(r0)     // Catch:{ all -> 0x00ee }
            monitor-exit(r1)     // Catch:{ all -> 0x00ee }
            return r9
        L_0x00ee:
            r8 = move-exception
            monitor-exit(r1)     // Catch:{ all -> 0x00ee }
            throw r8
        L_0x00f1:
            r9 = move-exception
            goto L_0x0109
        L_0x00f3:
            r9 = move-exception
            java.io.IOException r1 = new java.io.IOException     // Catch:{ all -> 0x00f1 }
            r1.<init>(r9)     // Catch:{ all -> 0x00f1 }
            throw r1     // Catch:{ all -> 0x00f1 }
        L_0x00fa:
            java.lang.String r9 = "FirebaseInstanceId"
            java.lang.String r1 = "No response"
            android.util.Log.w(r9, r1)     // Catch:{ all -> 0x00f1 }
            java.io.IOException r9 = new java.io.IOException     // Catch:{ all -> 0x00f1 }
            java.lang.String r1 = "TIMEOUT"
            r9.<init>(r1)     // Catch:{ all -> 0x00f1 }
            throw r9     // Catch:{ all -> 0x00f1 }
        L_0x0109:
            android.support.v4.util.SimpleArrayMap<java.lang.String, com.google.android.gms.tasks.TaskCompletionSource<android.os.Bundle>> r1 = r8.zzolg
            monitor-enter(r1)
            android.support.v4.util.SimpleArrayMap<java.lang.String, com.google.android.gms.tasks.TaskCompletionSource<android.os.Bundle>> r8 = r8.zzolg     // Catch:{ all -> 0x0113 }
            r8.remove(r0)     // Catch:{ all -> 0x0113 }
            monitor-exit(r1)     // Catch:{ all -> 0x0113 }
            throw r9
        L_0x0113:
            r8 = move-exception
            monitor-exit(r1)     // Catch:{ all -> 0x0113 }
            throw r8
        L_0x0116:
            java.io.IOException r8 = new java.io.IOException
            java.lang.String r9 = "MISSING_INSTANCEID_SERVICE"
            r8.<init>(r9)
            throw r8
        L_0x011e:
            r8 = move-exception
            monitor-exit(r2)     // Catch:{ all -> 0x011e }
            throw r8
        */
        throw new UnsupportedOperationException("Method not decompiled: com.google.firebase.iid.zzx.zzaa(android.os.Bundle):android.os.Bundle");
    }

    private static synchronized String zzawx() {
        String num;
        synchronized (zzx.class) {
            int i = zzino;
            zzino = i + 1;
            num = Integer.toString(i);
        }
        return num;
    }

    private static synchronized void zzd(Context context, Intent intent) {
        synchronized (zzx.class) {
            if (zzikb == null) {
                Intent intent2 = new Intent();
                intent2.setPackage("com.google.example.invalidpackage");
                zzikb = PendingIntent.getBroadcast(context, 0, intent2, 0);
            }
            intent.putExtra("app", zzikb);
        }
    }

    /* access modifiers changed from: private */
    public final void zze(Message message) {
        if (message != null) {
            Object obj = message.obj;
            if (obj instanceof Intent) {
                Intent intent = (Intent) obj;
                intent.setExtrasClassLoader(new zzi.zza());
                if (intent.hasExtra("google.messenger")) {
                    Parcelable parcelableExtra = intent.getParcelableExtra("google.messenger");
                    if (parcelableExtra instanceof zzi) {
                        this.zzolh = (zzi) parcelableExtra;
                    }
                    if (parcelableExtra instanceof Messenger) {
                        this.zziny = (Messenger) parcelableExtra;
                    }
                }
                Intent intent2 = (Intent) message.obj;
                String action = intent2.getAction();
                if ("com.google.android.c2dm.intent.REGISTRATION".equals(action)) {
                    String stringExtra = intent2.getStringExtra("registration_id");
                    if (stringExtra == null) {
                        stringExtra = intent2.getStringExtra("unregistered");
                    }
                    if (stringExtra == null) {
                        zzr(intent2);
                        return;
                    }
                    Matcher matcher = Pattern.compile("\\|ID\\|([^|]+)\\|:?+(.*)").matcher(stringExtra);
                    if (matcher.matches()) {
                        String group = matcher.group(1);
                        String group2 = matcher.group(2);
                        Bundle extras = intent2.getExtras();
                        extras.putString("registration_id", group2);
                        zzh(group, extras);
                        return;
                    } else if (Log.isLoggable("FirebaseInstanceId", 3)) {
                        Log.d("FirebaseInstanceId", stringExtra.length() != 0 ? "Unexpected response string: ".concat(stringExtra) : new String("Unexpected response string: "));
                        return;
                    } else {
                        return;
                    }
                } else if (Log.isLoggable("FirebaseInstanceId", 3)) {
                    String valueOf = String.valueOf(action);
                    Log.d("FirebaseInstanceId", valueOf.length() != 0 ? "Unexpected response action: ".concat(valueOf) : new String("Unexpected response action: "));
                    return;
                } else {
                    return;
                }
            }
        }
        Log.w("FirebaseInstanceId", "Dropping invalid message");
    }

    private final void zzh(String str, Bundle bundle) {
        synchronized (this.zzolg) {
            TaskCompletionSource remove = this.zzolg.remove(str);
            if (remove == null) {
                String valueOf = String.valueOf(str);
                Log.w("FirebaseInstanceId", valueOf.length() != 0 ? "Missing callback for ".concat(valueOf) : new String("Missing callback for "));
                return;
            }
            remove.setResult(bundle);
        }
    }

    private final void zzr(Intent intent) {
        String stringExtra = intent.getStringExtra("error");
        if (stringExtra == null) {
            String valueOf = String.valueOf(intent.getExtras());
            StringBuilder sb = new StringBuilder(valueOf.length() + 49);
            sb.append("Unexpected response, no error or registration id ");
            sb.append(valueOf);
            Log.w("FirebaseInstanceId", sb.toString());
            return;
        }
        if (Log.isLoggable("FirebaseInstanceId", 3)) {
            Log.d("FirebaseInstanceId", stringExtra.length() != 0 ? "Received InstanceID error ".concat(stringExtra) : new String("Received InstanceID error "));
        }
        if (stringExtra.startsWith("|")) {
            String[] split = stringExtra.split("\\|");
            if (split.length <= 2 || !UserConsentProviderContract.UserConsentList.ID.equals(split[1])) {
                Log.w("FirebaseInstanceId", stringExtra.length() != 0 ? "Unexpected structured response ".concat(stringExtra) : new String("Unexpected structured response "));
                return;
            }
            String str = split[2];
            String str2 = split[3];
            if (str2.startsWith(":")) {
                str2 = str2.substring(1);
            }
            zzh(str, intent.putExtra("error", str2).getExtras());
            return;
        }
        synchronized (this.zzolg) {
            for (int i = 0; i < this.zzolg.size(); i++) {
                zzh(this.zzolg.keyAt(i), intent.getExtras());
            }
        }
    }

    private final Bundle zzz(Bundle bundle) throws IOException {
        Bundle zzaa = zzaa(bundle);
        if (zzaa == null || !zzaa.containsKey("google.messenger")) {
            return zzaa;
        }
        Bundle zzaa2 = zzaa(bundle);
        if (zzaa2 == null || !zzaa2.containsKey("google.messenger")) {
            return zzaa2;
        }
        return null;
    }

    /* access modifiers changed from: package-private */
    public final Bundle zzah(Bundle bundle) throws IOException {
        if (this.zzokq.zzclo() < 12000000) {
            return zzz(bundle);
        }
        try {
            return (Bundle) Tasks.await(zzk.zzfa(this.zzaiq).zzj(1, bundle));
        } catch (InterruptedException | ExecutionException e) {
            if (Log.isLoggable("FirebaseInstanceId", 3)) {
                String valueOf = String.valueOf(e);
                StringBuilder sb = new StringBuilder(valueOf.length() + 22);
                sb.append("Error making request: ");
                sb.append(valueOf);
                Log.d("FirebaseInstanceId", sb.toString());
            }
            if (!(e.getCause() instanceof zzu) || ((zzu) e.getCause()).getErrorCode() != 4) {
                return null;
            }
            return zzz(bundle);
        }
    }
}
