package com.google.firebase;

import com.google.android.gms.common.internal.zzbg;
import com.google.android.gms.common.internal.zzbq;
import com.google.android.gms.common.util.zzw;
import java.util.Arrays;

public final class FirebaseOptions {
    private final String zzetb;
    private final String zzmna;
    private final String zzmnb;
    private final String zzmnc;
    private final String zzmnd;
    private final String zzmne;
    private final String zzmnf;

    public static final class Builder {
        private String zzetb;
        private String zzmna;
        private String zzmnb;
        private String zzmnc;
        private String zzmnd;
        private String zzmne;
        private String zzmnf;

        public final FirebaseOptions build() {
            return new FirebaseOptions(this.zzetb, this.zzmna, this.zzmnb, this.zzmnc, this.zzmnd, this.zzmne, this.zzmnf);
        }

        public final Builder setApiKey(String str) {
            this.zzmna = zzbq.zzh(str, "ApiKey must be set.");
            return this;
        }

        public final Builder setApplicationId(String str) {
            this.zzetb = zzbq.zzh(str, "ApplicationId must be set.");
            return this;
        }

        public final Builder setDatabaseUrl(String str) {
            this.zzmnb = str;
            return this;
        }

        public final Builder setGcmSenderId(String str) {
            this.zzmnd = str;
            return this;
        }

        public final Builder setProjectId(String str) {
            this.zzmnf = str;
            return this;
        }

        public final Builder setStorageBucket(String str) {
            this.zzmne = str;
            return this;
        }
    }

    private FirebaseOptions(String str, String str2, String str3, String str4, String str5, String str6, String str7) {
        zzbq.zza(!zzw.zzhb(str), "ApplicationId must be set.");
        this.zzetb = str;
        this.zzmna = str2;
        this.zzmnb = str3;
        this.zzmnc = str4;
        this.zzmnd = str5;
        this.zzmne = str6;
        this.zzmnf = str7;
    }

    public final boolean equals(Object obj) {
        if (!(obj instanceof FirebaseOptions)) {
            return false;
        }
        FirebaseOptions firebaseOptions = (FirebaseOptions) obj;
        return zzbg.equal(this.zzetb, firebaseOptions.zzetb) && zzbg.equal(this.zzmna, firebaseOptions.zzmna) && zzbg.equal(this.zzmnb, firebaseOptions.zzmnb) && zzbg.equal(this.zzmnc, firebaseOptions.zzmnc) && zzbg.equal(this.zzmnd, firebaseOptions.zzmnd) && zzbg.equal(this.zzmne, firebaseOptions.zzmne) && zzbg.equal(this.zzmnf, firebaseOptions.zzmnf);
    }

    public final String getApplicationId() {
        return this.zzetb;
    }

    public final String getGcmSenderId() {
        return this.zzmnd;
    }

    public final int hashCode() {
        return Arrays.hashCode(new Object[]{this.zzetb, this.zzmna, this.zzmnb, this.zzmnc, this.zzmnd, this.zzmne, this.zzmnf});
    }

    public final String toString() {
        return zzbg.zzx(this).zzg("applicationId", this.zzetb).zzg("apiKey", this.zzmna).zzg("databaseUrl", this.zzmnb).zzg("gcmSenderId", this.zzmnd).zzg("storageBucket", this.zzmne).zzg("projectId", this.zzmnf).toString();
    }
}
