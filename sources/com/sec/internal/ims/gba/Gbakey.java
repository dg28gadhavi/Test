package com.sec.internal.ims.gba;

import android.util.Log;
import com.sec.internal.helper.SimUtil;
import java.nio.charset.StandardCharsets;
import java.util.Arrays;

public class Gbakey {
    private static final String LOG_TAG = "Gbakey";
    private byte[] id;
    private byte[] phoneId;
    private byte[] type;

    public Gbakey(byte[] bArr, byte[] bArr2) {
        this.type = bArr2;
        this.id = bArr;
        this.phoneId = (String.valueOf(0) + String.valueOf(SimUtil.getSubId(0))).getBytes(StandardCharsets.UTF_8);
        String str = LOG_TAG;
        Log.d(str, "Gbakey: " + toString());
    }

    public Gbakey(byte[] bArr, byte[] bArr2, int i) {
        this.type = bArr2;
        this.id = bArr;
        this.phoneId = (String.valueOf(i) + String.valueOf(SimUtil.getSubId(i))).getBytes(StandardCharsets.UTF_8);
        String str = LOG_TAG;
        Log.d(str, "Gbakey: " + toString());
    }

    public int hashCode() {
        return ((((Arrays.hashCode(this.id) + 31) * 31) + Arrays.hashCode(this.type)) * 31) + Arrays.hashCode(this.phoneId);
    }

    public boolean equals(Object obj) {
        if (this == obj) {
            return true;
        }
        if (obj == null || !(obj instanceof Gbakey)) {
            return false;
        }
        Gbakey gbakey = (Gbakey) obj;
        return Arrays.equals(this.id, gbakey.id) && Arrays.equals(this.type, gbakey.type) && Arrays.equals(this.phoneId, gbakey.phoneId);
    }

    public String toString() {
        return "Gbakey [type=" + Arrays.toString(this.type) + ", id=" + Arrays.toString(this.id) + ", phoneId=" + Arrays.toString(this.phoneId) + "]";
    }
}
