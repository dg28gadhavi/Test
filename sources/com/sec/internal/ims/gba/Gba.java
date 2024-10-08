package com.sec.internal.ims.gba;

import android.util.Log;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;
import java.util.TimeZone;

public class Gba {
    private static final String LOG_TAG = "Gba";
    private GbaStore gbaStore;
    private SimpleDateFormat sdf;
    private int threshold;

    public Gba(int i) {
        this.sdf = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss'Z'", Locale.US);
        this.threshold = i;
        this.gbaStore = new GbaStore();
    }

    public Gba() {
        this(0);
    }

    public void storeGbaKey(byte[] bArr, byte[] bArr2, byte[] bArr3, String str, String str2, boolean z) {
        storeGbaKey(bArr, bArr2, bArr3, str, str2, z, 0);
    }

    public void storeGbaKey(byte[] bArr, byte[] bArr2, byte[] bArr3, String str, String str2, boolean z, int i) {
        Date date;
        this.sdf.setTimeZone(TimeZone.getTimeZone("GMT"));
        try {
            date = this.sdf.parse(str);
        } catch (ParseException e) {
            e.printStackTrace();
            date = null;
        }
        if (!isKeyExpired(date)) {
            this.gbaStore.putKeys(new Gbakey(bArr2, bArr, i), new GbaValue(bArr3, date, str2, z));
        }
    }

    public GbaValue getGbaValue(byte[] bArr, byte[] bArr2) {
        return getGbaValue(bArr, bArr2, 0);
    }

    public GbaValue getGbaValue(byte[] bArr, byte[] bArr2, int i) {
        if (bArr == null || bArr2 == null) {
            return null;
        }
        Gbakey gbakey = new Gbakey(bArr, bArr2, i);
        if (!this.gbaStore.hasKey(gbakey)) {
            return null;
        }
        GbaValue keys = this.gbaStore.getKeys(gbakey);
        if (!isKeyExpired(keys.getValidity())) {
            return keys;
        }
        this.gbaStore.removeKey(gbakey);
        return null;
    }

    public boolean isKeyExpired(Date date) {
        Date date2 = new Date();
        this.sdf.setTimeZone(TimeZone.getTimeZone("GMT"));
        if (date == null || date.getTime() <= date2.getTime() + (((long) this.threshold) * 1000)) {
            return true;
        }
        String str = LOG_TAG;
        Log.d(str, "Current Date and time in GMT: " + this.sdf.format(date2) + "  key life time in GMT: " + this.sdf.format(date));
        return false;
    }

    public void removeGbaKey(byte[] bArr, byte[] bArr2, int i) {
        if (bArr != null && bArr2 != null) {
            Gbakey gbakey = new Gbakey(bArr, bArr2, i);
            if (this.gbaStore.hasKey(gbakey)) {
                this.gbaStore.removeKey(gbakey);
            }
        }
    }

    public int hashCode() {
        GbaStore gbaStore2 = this.gbaStore;
        return 31 + (gbaStore2 == null ? 0 : gbaStore2.hashCode());
    }

    public boolean equals(Object obj) {
        if (this == obj) {
            return true;
        }
        if (obj == null || !(obj instanceof Gba)) {
            return false;
        }
        Gba gba = (Gba) obj;
        GbaStore gbaStore2 = this.gbaStore;
        if (gbaStore2 == null) {
            if (gba.gbaStore != null) {
                return false;
            }
        } else if (!gbaStore2.equals(gba.gbaStore)) {
            return false;
        }
        return true;
    }

    public String toString() {
        return "Gba [gbaStore=" + this.gbaStore + "]";
    }
}
