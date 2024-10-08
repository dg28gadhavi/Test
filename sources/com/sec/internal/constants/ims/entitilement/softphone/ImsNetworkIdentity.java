package com.sec.internal.constants.ims.entitilement.softphone;

import java.util.ArrayList;
import java.util.List;

public class ImsNetworkIdentity {
    private List<String> mAddressList;
    private String mAppId;
    private String mImpi;
    private String mImpu;

    public ImsNetworkIdentity() {
        this.mImpi = null;
        this.mImpu = null;
        this.mAddressList = new ArrayList();
        this.mAppId = null;
    }

    public ImsNetworkIdentity(String str, String str2, List<String> list, String str3) {
        this.mImpi = null;
        this.mImpu = null;
        new ArrayList();
        this.mImpi = str;
        this.mImpu = str2;
        this.mAddressList = list;
        this.mAppId = str3;
    }

    public String getImpi() {
        return this.mImpi;
    }

    public String getImpu() {
        return this.mImpu;
    }

    public List<String> getAddressList() {
        return this.mAddressList;
    }

    public String getAppId() {
        return this.mAppId;
    }

    public boolean impiEmpty() {
        return this.mImpi == null;
    }

    public void clear() {
        this.mImpi = null;
        this.mImpu = null;
        this.mAddressList.clear();
        this.mAppId = null;
    }

    public String toString() {
        return "[impi: " + this.mImpi + " impu: " + this.mImpu + " address: " + this.mAddressList + " app-id: " + this.mAppId + "]";
    }
}
