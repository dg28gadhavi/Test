package com.sec.internal.constants.ims.config;

import com.sec.internal.log.IMSLog;

public final class RcsConfig {
    private int mCbMsgTech;
    private String mConfUri;
    private String mDownloadsPath;
    private String mEndUserConfReqId;
    private String mExploderUri;
    private int mFtChunkSize;
    private boolean mIsAggrImdnSupported;
    private boolean mIsConfSubscribeEnabled;
    private boolean mIsMsrpCema;
    private boolean mIsPrivacyDisable;
    private int mIshChunkSize;
    private int mPagerModeLimit;
    private String mSupportBotVersions;
    private int mSupportCancelMessage;
    private boolean mSupportRealtimeUserAlias;
    private boolean mUseMsrpDiscardPort;

    public RcsConfig(int i, int i2, String str, boolean z, String str2, boolean z2, String str3, int i3, boolean z3, boolean z4, boolean z5, int i4, String str4, String str5, int i5, boolean z6) {
        this.mFtChunkSize = i;
        this.mIshChunkSize = i2;
        this.mConfUri = str;
        this.mIsMsrpCema = z;
        this.mDownloadsPath = str2;
        this.mIsConfSubscribeEnabled = z2;
        this.mExploderUri = str3;
        this.mPagerModeLimit = i3;
        this.mUseMsrpDiscardPort = z3;
        this.mIsAggrImdnSupported = z4;
        this.mIsPrivacyDisable = z5;
        this.mCbMsgTech = i4;
        this.mEndUserConfReqId = str4;
        this.mSupportBotVersions = str5;
        this.mSupportCancelMessage = i5;
        this.mSupportRealtimeUserAlias = z6;
    }

    public int getFtChunkSize() {
        return this.mFtChunkSize;
    }

    public int getIshChunkSize() {
        return this.mIshChunkSize;
    }

    public String getConfUri() {
        return this.mConfUri;
    }

    public boolean isMsrpCema() {
        return this.mIsMsrpCema;
    }

    public String getDownloadsPath() {
        return this.mDownloadsPath;
    }

    public boolean isConfSubscribeEnabled() {
        return this.mIsConfSubscribeEnabled;
    }

    public String getExploderUri() {
        return this.mExploderUri;
    }

    public int getPagerModeLimit() {
        return this.mPagerModeLimit;
    }

    public boolean isUseMsrpDiscardPort() {
        return this.mUseMsrpDiscardPort;
    }

    public boolean isAggrImdnSupported() {
        return this.mIsAggrImdnSupported;
    }

    public boolean isPrivacyDisable() {
        return this.mIsPrivacyDisable;
    }

    public int getCbMsgTech() {
        return this.mCbMsgTech;
    }

    public String getEndUserConfReqId() {
        return this.mEndUserConfReqId;
    }

    public String getSupportBotVersions() {
        return this.mSupportBotVersions;
    }

    public int getSupportCancelMessage() {
        return this.mSupportCancelMessage;
    }

    public boolean getSupportRealtimeUserAlias() {
        return this.mSupportRealtimeUserAlias;
    }

    public String toString() {
        return "RcsConfig[chunksize = " + this.mFtChunkSize + " / " + this.mIshChunkSize + ", confuri = " + IMSLog.checker(this.mConfUri) + ", is msrp cema = " + this.mIsMsrpCema + ", downloads path = " + this.mDownloadsPath + ", conf.subscribe enabled = " + this.mIsConfSubscribeEnabled + ", exploderUri = " + this.mExploderUri + ", pagerModeLimit = " + this.mPagerModeLimit + ", useMsrpDiscardPort = " + this.mUseMsrpDiscardPort + ", aggr.imdn supported = " + this.mIsAggrImdnSupported + ", privacyDisable = " + this.mIsPrivacyDisable + ", cbMsgTech = " + this.mCbMsgTech + ", endUserConfReqId = " + IMSLog.checker(this.mEndUserConfReqId) + ", mSupportBotVersions = " + this.mSupportBotVersions + ", supportCancelMessage = " + this.mSupportCancelMessage + ", supportRealtimeUserAlias = " + this.mSupportRealtimeUserAlias + "]";
    }
}
