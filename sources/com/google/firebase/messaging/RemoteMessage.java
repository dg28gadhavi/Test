package com.google.firebase.messaging;

import android.os.Bundle;
import android.os.Parcel;
import android.os.Parcelable;
import android.support.v4.util.ArrayMap;
import com.google.android.gms.internal.zzbgl;
import com.google.android.gms.internal.zzbgo;
import java.util.Map;

public final class RemoteMessage extends zzbgl {
    public static final Parcelable.Creator<RemoteMessage> CREATOR = new zzf();
    Bundle mBundle;
    private Map<String, String> zzdvf;

    RemoteMessage(Bundle bundle) {
        this.mBundle = bundle;
    }

    public final Map<String, String> getData() {
        if (this.zzdvf == null) {
            this.zzdvf = new ArrayMap();
            for (String str : this.mBundle.keySet()) {
                Object obj = this.mBundle.get(str);
                if (obj instanceof String) {
                    String str2 = (String) obj;
                    if (!str.startsWith("google.") && !str.startsWith("gcm.") && !str.equals("from") && !str.equals("message_type") && !str.equals("collapse_key")) {
                        this.zzdvf.put(str, str2);
                    }
                }
            }
        }
        return this.zzdvf;
    }

    public final String getFrom() {
        return this.mBundle.getString("from");
    }

    public final String getMessageId() {
        String string = this.mBundle.getString("google.message_id");
        return string == null ? this.mBundle.getString("message_id") : string;
    }

    public final void writeToParcel(Parcel parcel, int i) {
        int zze = zzbgo.zze(parcel);
        zzbgo.zza(parcel, 2, this.mBundle, false);
        zzbgo.zzai(parcel, zze);
    }
}
