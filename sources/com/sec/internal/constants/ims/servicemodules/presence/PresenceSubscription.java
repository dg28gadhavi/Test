package com.sec.internal.constants.ims.servicemodules.presence;

import android.util.Log;
import com.sec.ims.util.ImsUri;
import com.sec.internal.constants.ims.servicemodules.options.CapabilityConstants;
import com.sec.internal.ims.core.RegistrationGovernor;
import java.util.Date;
import java.util.HashSet;
import java.util.Set;

public class PresenceSubscription implements Cloneable {
    public static final int EXPIRED = 2;
    public static final int FAILED = 6;
    public static final int FETCH_DONE = 4;
    private static final String LOG_TAG = "PresenceSubscription";
    public static final int ONLINE = 1;
    public static final int REQUESTED = 0;
    public static final int RETRIED = 5;
    private Set<ImsUri> mDropUris = new HashSet();
    private int mExpiry;
    private String mId;
    private int mPhoneId;
    private int mRetryCount;
    private boolean mSingleFetch;
    private int mState;
    private Date mTimestamp;
    private CapabilityConstants.RequestType mType;
    private Set<ImsUri> mUriList = new HashSet();

    public PresenceSubscription(String str) {
        this.mId = str;
        this.mState = 0;
        this.mTimestamp = new Date();
        this.mExpiry = 0;
        this.mType = CapabilityConstants.RequestType.REQUEST_TYPE_NONE;
        this.mRetryCount = 0;
        this.mSingleFetch = true;
    }

    public Set<ImsUri> getUriList() {
        return this.mUriList;
    }

    public void addUri(ImsUri imsUri) {
        this.mUriList.add(imsUri);
    }

    public void addUriAll(Set<ImsUri> set) {
        this.mUriList.addAll(set);
    }

    public void remove(ImsUri imsUri) {
        this.mUriList.remove(imsUri);
    }

    public boolean contains(ImsUri imsUri) {
        return this.mUriList.contains(imsUri);
    }

    public Set<ImsUri> getDropUris() {
        return this.mDropUris;
    }

    public void addDropUriAll(Set<ImsUri> set) {
        this.mDropUris.addAll(set);
    }

    public void removeDropUri(ImsUri imsUri) {
        this.mDropUris.remove(imsUri);
    }

    public boolean containsDropUri(ImsUri imsUri) {
        return this.mDropUris.contains(imsUri);
    }

    public void setExpiry(int i) {
        this.mExpiry = i;
    }

    public int getExpiry() {
        return this.mExpiry;
    }

    public void setPhoneId(int i) {
        this.mPhoneId = i;
    }

    public int getPhoneId() {
        return this.mPhoneId;
    }

    public String getSubscriptionId() {
        return this.mId;
    }

    public void updateState(int i) {
        this.mState = i;
    }

    public int getState() {
        return this.mState;
    }

    public void updateTimestamp() {
        this.mTimestamp = new Date();
    }

    public Date getTimestamp() {
        return this.mTimestamp;
    }

    public void setSingleFetch(boolean z) {
        this.mSingleFetch = z;
    }

    public boolean isSingleFetch() {
        return this.mSingleFetch;
    }

    public boolean isExpired() {
        int i = this.mState;
        if (i == 2 || i == 4) {
            return true;
        }
        Date date = new Date();
        if (this.mState == 5 || date.getTime() - this.mTimestamp.getTime() < ((long) this.mExpiry) * 1000) {
            return false;
        }
        this.mState = 2;
        return true;
    }

    public void setRequestType(CapabilityConstants.RequestType requestType) {
        this.mType = requestType;
    }

    public CapabilityConstants.RequestType getRequestType() {
        return this.mType;
    }

    public void setRetryCount(int i) {
        this.mRetryCount = i;
    }

    public int getRetryCount() {
        return this.mRetryCount;
    }

    public void retrySubscription() {
        this.mRetryCount++;
    }

    public boolean isLongLivedSubscription() {
        Date date = new Date();
        long time = date.getTime() - this.mTimestamp.getTime();
        Log.d(LOG_TAG, "isLongLivedSubscription: interval from " + this.mTimestamp + " to " + date.getTime() + ", offset " + time);
        return time > RegistrationGovernor.RETRY_AFTER_PDNLOST_MS;
    }

    public PresenceSubscription clone() throws CloneNotSupportedException {
        return (PresenceSubscription) super.clone();
    }
}
