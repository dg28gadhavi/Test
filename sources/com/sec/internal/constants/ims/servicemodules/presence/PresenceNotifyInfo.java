package com.sec.internal.constants.ims.servicemodules.presence;

import android.net.Uri;
import android.util.Pair;
import java.util.ArrayList;
import java.util.List;

public class PresenceNotifyInfo {
    private int mPhoneId;
    private List<String> mPidfXmls = new ArrayList();
    private String mSubscriptionId;
    private String mSubscriptionState = "";
    private String mSubscriptionStateReason = "";
    private List<Pair<Uri, String>> mUriTerminatedReason = new ArrayList();

    public PresenceNotifyInfo(int i, String str) {
        this.mPhoneId = i;
        this.mSubscriptionId = str;
    }

    public int getPhoneId() {
        return this.mPhoneId;
    }

    public String getSubscriptionId() {
        return this.mSubscriptionId;
    }

    public String getSubscriptionState() {
        return this.mSubscriptionState;
    }

    public void setSubscriptionState(String str) {
        this.mSubscriptionState = str;
    }

    public String getSubscriptionStateReason() {
        return this.mSubscriptionStateReason;
    }

    public void setSubscriptionStateReason(String str) {
        this.mSubscriptionStateReason = str;
    }

    public List<String> getPidfXmls() {
        return this.mPidfXmls;
    }

    public void setPidfXmls(List<String> list) {
        this.mPidfXmls = list;
    }

    public void addPidfXmls(String str) {
        if (!this.mPidfXmls.contains(str)) {
            this.mPidfXmls.add(str);
        }
    }

    public List<Pair<Uri, String>> getUriTerminatedReason() {
        return this.mUriTerminatedReason;
    }

    public void setUriTerminatedReason(List<Pair<Uri, String>> list) {
        this.mUriTerminatedReason = list;
    }

    public void addUriTerminatedReason(Uri uri, String str) {
        this.mUriTerminatedReason.add(new Pair(uri, str));
    }
}
