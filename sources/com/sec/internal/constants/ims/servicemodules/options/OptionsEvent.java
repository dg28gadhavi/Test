package com.sec.internal.constants.ims.servicemodules.options;

import com.sec.ims.util.ImsUri;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;

public class OptionsEvent {
    private List<String> featureList;
    private String mExtFeature;
    private long mFeatures;
    private boolean mIsResponse;
    private boolean mIsTokenUsed;
    private Set<ImsUri> mPAssertedIdSet;
    private int mPhoneId;
    private OptionsFailureReason mReason;
    private String mReasonHdr;
    private int mRespCode;
    private int mSessionId;
    private boolean mSuccess;
    private String mTxId;
    private ImsUri mUri;
    private List<String> mfeatureTags;
    private int mlastSeen = -1;

    public enum OptionsFailureReason {
        USER_NOT_AVAILABLE,
        DOES_NOT_EXIST_ANYWHERE,
        USER_NOT_REGISTERED,
        USER_NOT_REACHABLE,
        FORBIDDEN_403,
        REQUEST_TIMED_OUT,
        AUTOMATA_PRESENT,
        INVALID_DATA,
        USER_AVAILABLE_OFFLINE,
        ERROR
    }

    public OptionsEvent(boolean z, ImsUri imsUri, long j, int i, boolean z2, int i2, String str, Set<ImsUri> set, String str2) {
        this.mSuccess = z;
        this.mUri = imsUri;
        this.mFeatures = j;
        this.mPhoneId = i;
        this.mIsResponse = z2;
        this.mSessionId = i2;
        this.mTxId = str;
        this.mExtFeature = str2;
        this.mPAssertedIdSet = set;
        this.mIsTokenUsed = false;
        this.featureList = new ArrayList();
    }

    public ImsUri getUri() {
        return this.mUri;
    }

    public boolean isSuccess() {
        return this.mSuccess;
    }

    public boolean isResponse() {
        return this.mIsResponse;
    }

    public long getFeatures() {
        return this.mFeatures;
    }

    public boolean getIsTokenUsed() {
        return this.mIsTokenUsed;
    }

    public void setFeatures(long j) {
        this.mFeatures = j;
    }

    public int getPhoneId() {
        return this.mPhoneId;
    }

    public void setReason(OptionsFailureReason optionsFailureReason) {
        this.mReason = optionsFailureReason;
    }

    public void setIsTokenUsed(boolean z) {
        this.mIsTokenUsed = z;
    }

    public OptionsFailureReason getReason() {
        return this.mReason;
    }

    public String getTxId() {
        return this.mTxId;
    }

    public int getSessionId() {
        return this.mSessionId;
    }

    public int getLastSeen() {
        return this.mlastSeen;
    }

    public void setLastSeen(int i) {
        this.mlastSeen = i;
    }

    public String getExtFeature() {
        return this.mExtFeature;
    }

    public int getRespCode() {
        return this.mRespCode;
    }

    public void setRespCode(int i) {
        this.mRespCode = i;
    }

    public String getReasonHdr() {
        return this.mReasonHdr;
    }

    public void setReasonHdr(String str) {
        this.mReasonHdr = str;
    }

    public List<String> getfeatureTags() {
        return this.mfeatureTags;
    }

    public void setfeatureTags(List<String> list) {
        this.mfeatureTags = list;
    }

    public Set<ImsUri> getPAssertedIdSet() {
        return this.mPAssertedIdSet;
    }

    public List<String> getFeatureList() {
        return this.featureList;
    }

    public void setFeatureList(List<String> list) {
        this.featureList = list;
    }

    public String toString() {
        return "OptionsEvent [mUri=" + this.mUri + ", mSuccess=" + this.mSuccess + ", mFeatures=" + this.mFeatures + ", mPhoneId=" + this.mPhoneId + ", mIsResponse=" + this.mIsResponse + ", mReason=" + this.mReason + ", mSessionId=" + this.mSessionId + ", mPAssertedIdSet=" + this.mPAssertedIdSet + ", mExtFeature=" + this.mExtFeature + "]";
    }
}
