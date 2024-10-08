package com.sec.internal.ims.gba.params;

public class GbaData {
    String cipkey;
    String intkey;
    String password;
    int phoneId = 0;

    public GbaData(String str, String str2, String str3) {
        this.password = str;
        this.cipkey = str2;
        this.intkey = str3;
    }

    public String getPassword() {
        return this.password;
    }

    public String getCipkey() {
        return this.cipkey;
    }

    public String getIntkey() {
        return this.intkey;
    }

    public void setPhoneId(int i) {
        this.phoneId = i;
    }

    public int getPhoneId() {
        return this.phoneId;
    }
}
