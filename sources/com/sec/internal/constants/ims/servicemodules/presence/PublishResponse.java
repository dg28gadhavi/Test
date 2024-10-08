package com.sec.internal.constants.ims.servicemodules.presence;

public class PublishResponse extends PresenceResponse {
    private String mEtag;
    private long mExpiresTimer;
    private boolean mIsRefresh;
    private long mRetryAfter;

    /* JADX INFO: super call moved to the top of the method (can break code semantics) */
    public PublishResponse(boolean z, int i, String str, int i2, String str2, long j, boolean z2, long j2, int i3) {
        super(z, i, str, i2, i3);
        this.mEtag = str2;
        this.mExpiresTimer = j;
        this.mIsRefresh = z2;
        this.mRetryAfter = j2;
    }

    public String getEtag() {
        return this.mEtag;
    }

    public long getExpiresTimer() {
        return this.mExpiresTimer;
    }

    public boolean isRefresh() {
        return this.mIsRefresh;
    }

    public long getRetryAfter() {
        return this.mRetryAfter;
    }
}
