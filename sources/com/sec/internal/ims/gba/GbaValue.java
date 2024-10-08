package com.sec.internal.ims.gba;

import java.util.Arrays;
import java.util.Date;

public class GbaValue {
    private String Btid;
    private boolean uicc;
    private Date validity;
    private byte[] value;

    public GbaValue(byte[] bArr, Date date, String str, boolean z) {
        this.value = bArr;
        this.validity = date;
        this.Btid = str;
        this.uicc = z;
    }

    public byte[] getValue() {
        return this.value;
    }

    public Date getValidity() {
        return this.validity;
    }

    public String getBtid() {
        return this.Btid;
    }

    public boolean isUicc() {
        return this.uicc;
    }

    public int hashCode() {
        Date date = this.validity;
        int i = 0;
        int hashCode = ((((date == null ? 0 : date.hashCode()) + 31) * 31) + Arrays.hashCode(this.value)) * 31;
        String str = this.Btid;
        if (str != null) {
            i = str.hashCode();
        }
        return hashCode + i;
    }

    public boolean equals(Object obj) {
        if (this == obj) {
            return true;
        }
        if (obj == null || !(obj instanceof GbaValue)) {
            return false;
        }
        GbaValue gbaValue = (GbaValue) obj;
        Date date = this.validity;
        if (date == null) {
            if (gbaValue.validity != null) {
                return false;
            }
        } else if (!date.equals(gbaValue.validity)) {
            return false;
        }
        if (!Arrays.equals(this.value, gbaValue.value)) {
            return false;
        }
        String str = this.Btid;
        if (str == null) {
            if (gbaValue.Btid != null) {
                return false;
            }
        } else if (!str.equals(gbaValue.Btid)) {
            return false;
        }
        return true;
    }

    public String toString() {
        return "GbaValue [value=" + Arrays.toString(this.value) + ", validity=" + this.validity + "]";
    }
}
