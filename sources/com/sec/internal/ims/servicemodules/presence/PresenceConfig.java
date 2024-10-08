package com.sec.internal.ims.servicemodules.presence;

import android.content.Context;
import android.text.TextUtils;
import com.sec.ims.settings.ImsProfile;
import com.sec.ims.util.ImsUri;
import com.sec.internal.constants.Mno;
import com.sec.internal.constants.ims.SipMsg;
import com.sec.internal.constants.ims.config.ConfigConstants;
import com.sec.internal.helper.DmConfigHelper;
import com.sec.internal.helper.RcsConfigurationHelper;
import com.sec.internal.helper.SimUtil;
import com.sec.internal.ims.rcs.RcsPolicyManager;
import com.sec.internal.ims.registry.ImsRegistry;
import com.sec.internal.ims.servicemodules.im.strategy.IMnoStrategy;
import com.sec.internal.ims.util.ConfigUtil;
import com.sec.internal.imscr.LogClass;
import com.sec.internal.interfaces.ims.core.IRegistrationManager;
import com.sec.internal.log.IMSLog;

public class PresenceConfig {
    private static final String LOG_TAG = "PresenceConfig";
    private static final int MAX_NUMBER_LIMIT = 150;
    private long mBadEventExpiry;
    private Context mContext;
    private int mDefaultDisc;
    private boolean mIsGzipEnabled;
    private boolean mIsLocalConfigUsed;
    private boolean mIsSocialPresenceSupport;
    private int mMaxUri;
    private int mPhoneId;
    private long mPublishErrRetry;
    private long mPublishTimer;
    private long mPublishTimerExtended;
    private String mRcsProfile;
    private long mRetryPublishTimer;
    private ImsUri mRlsUri;
    private long mSourceThrottlePublish;
    private long mSourceThrottleSubscribe;
    private long mTdelayPublish;
    private boolean mUseAnonymousFetch;
    private boolean mUseSipUri;

    public static class Builder {
        long badEventExpiry = 259200;
        boolean isLocalConfigUsed = false;
        int maxUri = 100;
        long publishErrRetry = 21600;
        long publishTimer = 1200;
        long publishTimerExtended = 86400;
        long retryPublishTimer = 1200;
        long sourceThrottlePublish = 0;
        long sourceThrottleSubscribe = 0;
        long tDelayPublish = 5;
        boolean useAnonymousFetch = false;
    }

    PresenceConfig(Context context, int i) {
        this.mPublishTimer = 1200;
        this.mPublishTimerExtended = 86400;
        this.mMaxUri = 100;
        this.mDefaultDisc = 0;
        this.mRcsProfile = "";
        this.mContext = context;
        this.mPhoneId = i;
    }

    public void load() {
        IRegistrationManager registrationManager = ImsRegistry.getRegistrationManager();
        if (registrationManager == null) {
            IMSLog.e(LOG_TAG, this.mPhoneId, "load: registrationManager is null");
            return;
        }
        ImsProfile imsProfile = registrationManager.getImsProfile(this.mPhoneId, ImsProfile.PROFILE_TYPE.RCS);
        if (imsProfile == null || !imsProfile.hasService(SipMsg.EVENT_PRESENCE)) {
            IMSLog.e(LOG_TAG, this.mPhoneId, "load: profile is null");
            return;
        }
        this.mRcsProfile = ConfigUtil.getRcsProfileWithFeature(this.mContext, this.mPhoneId, imsProfile);
        RcsConfigurationHelper.ConfigData configData = RcsConfigurationHelper.getConfigData(this.mContext, "root/application/*", this.mPhoneId);
        if (configData == null) {
            IMSLog.e(LOG_TAG, this.mPhoneId, "load: configData is not found");
            return;
        }
        this.mIsSocialPresenceSupport = configData.readBool(ConfigConstants.ConfigTable.SERVICES_PRESENCE_PRFL, Boolean.FALSE).booleanValue();
        long publishTimer = (long) imsProfile.getPublishTimer();
        this.mPublishTimer = publishTimer;
        if (publishTimer <= 0) {
            this.mPublishTimer = configData.readLong(ConfigConstants.ConfigTable.PRESENCE_PUBLISH_TIMER, 1200L).longValue();
        }
        long longValue = configData.readLong(ConfigConstants.ConfigTable.PRESENCE_THROTTLE_PUBLISH, 0L).longValue();
        this.mSourceThrottlePublish = longValue;
        this.mSourceThrottleSubscribe = longValue;
        Mno simMno = SimUtil.getSimMno(this.mPhoneId);
        if (imsProfile.getSubscribeMaxEntry() == 0) {
            this.mMaxUri = configData.readInt(ConfigConstants.ConfigTable.PRESENCE_MAX_SUBSCRIPTION_LIST, 100).intValue();
            if (simMno.isKor() && this.mMaxUri > 150) {
                this.mMaxUri = 150;
            }
        } else {
            this.mMaxUri = imsProfile.getSubscribeMaxEntry();
        }
        String readString = configData.readString(ConfigConstants.ConfigTable.PRESENCE_RLS_URI, "");
        if (!TextUtils.isEmpty(readString)) {
            this.mRlsUri = ImsUri.parse(readString);
        }
        this.mUseAnonymousFetch = imsProfile.isAnonymousFetch();
        this.mDefaultDisc = configData.readInt(ConfigConstants.ConfigTable.CAPDISCOVERY_DEFAULT_DISC, 0).intValue();
        if (imsProfile.getBadEventExpiry() == 259200) {
            this.mBadEventExpiry = configData.readLong(ConfigConstants.ConfigTable.CAPDISCOVERY_NON_RCS_CAPINFO_EXPIRY, 259200L).longValue();
        } else {
            this.mBadEventExpiry = (long) imsProfile.getBadEventExpiry();
        }
        this.mPublishErrRetry = (long) imsProfile.getPublishErrRetryTimer();
        Context context = ImsRegistry.getContext();
        this.mTdelayPublish = (long) DmConfigHelper.readInt(context, "t_delay", 5, this.mPhoneId).intValue();
        this.mPublishTimerExtended = (long) imsProfile.getExtendedPublishTimer();
        this.mUseSipUri = false;
        this.mIsGzipEnabled = imsProfile.isGzipEnabled();
        if (simMno == Mno.VZW) {
            this.mPublishTimer = DmConfigHelper.readLong(context, ConfigConstants.ConfigPath.OMADM_PUBLISH_TIMER, 1200L, this.mPhoneId).longValue();
            this.mPublishTimerExtended = DmConfigHelper.readLong(context, ConfigConstants.ConfigPath.OMADM_PUBLISH_TIMER_EXTEND, 86400L, this.mPhoneId).longValue();
            this.mPublishErrRetry = DmConfigHelper.readLong(context, ConfigConstants.ConfigPath.OMADM_PUBLISH_ERR_RETRY_TIMER, 21600L, this.mPhoneId).longValue();
            long longValue2 = DmConfigHelper.readLong(this.mContext, ConfigConstants.ConfigPath.OMADM_SRC_THROTTLE_PUBLISH, 60L, this.mPhoneId).longValue();
            this.mSourceThrottlePublish = longValue2;
            this.mSourceThrottleSubscribe = longValue2;
            this.mMaxUri = DmConfigHelper.readInt(context, ConfigConstants.ConfigPath.OMADM_SUBSCRIBE_MAX_ENTRY, 100, this.mPhoneId).intValue();
            int i = this.mPhoneId;
            IMSLog.s(LOG_TAG, i, "load: mSourceThrottlePublishFromDM: " + this.mSourceThrottlePublish + ", mSourceThrottleSubscribeFromDM: " + this.mSourceThrottleSubscribe + ", mMaxUriFromDM: " + this.mMaxUri);
            IMnoStrategy rcsStrategy = RcsPolicyManager.getRcsStrategy(this.mPhoneId);
            boolean z = rcsStrategy != null && rcsStrategy.isLocalConfigUsed();
            this.mIsLocalConfigUsed = z;
            if (!z) {
                long longValue3 = configData.readLong(ConfigConstants.ConfigTable.PRESENCE_THROTTLE_PUBLISH, 0L).longValue();
                if (longValue3 > 0) {
                    int i2 = this.mPhoneId;
                    IMSLog.s(LOG_TAG, i2, "load: change mSourceThrottlePublish to " + longValue3);
                    this.mSourceThrottlePublish = longValue3;
                }
                int intValue = configData.readInt(ConfigConstants.ConfigTable.PRESENCE_MAX_SUBSCRIPTION_LIST, 0).intValue();
                if (intValue > 0) {
                    int i3 = this.mPhoneId;
                    IMSLog.s(LOG_TAG, i3, "load: change mMaxUri to " + intValue);
                    this.mMaxUri = intValue;
                }
            }
        }
        long j = this.mPublishTimer;
        this.mRetryPublishTimer = j;
        if (j == 0) {
            this.mRetryPublishTimer = (long) imsProfile.getPublishExpiry();
        }
        int i4 = this.mPhoneId;
        IMSLog.i(LOG_TAG, i4, "load: " + toString());
        IMSLog.c(LogClass.PM_READ_CONF, this.mPhoneId + "," + this.mPublishTimer + "," + this.mRetryPublishTimer + "," + this.mSourceThrottlePublish + "," + this.mSourceThrottleSubscribe + "," + this.mMaxUri + "," + this.mUseAnonymousFetch + "," + this.mBadEventExpiry);
        StringBuilder sb = new StringBuilder();
        sb.append(this.mPhoneId);
        sb.append(",");
        sb.append(this.mRlsUri);
        sb.append(",");
        sb.append(this.mPublishErrRetry);
        sb.append(",");
        sb.append(this.mTdelayPublish);
        sb.append(",");
        sb.append(this.mPublishTimerExtended);
        sb.append(",");
        sb.append(this.mUseSipUri);
        sb.append(",");
        sb.append(this.mDefaultDisc);
        IMSLog.c(LogClass.PM_READ_CONF, sb.toString());
    }

    public boolean isSocialPresenceSupport() {
        return this.mIsSocialPresenceSupport;
    }

    public boolean useAnonymousFetch() {
        return this.mUseAnonymousFetch;
    }

    public boolean useSipUri() {
        return this.mUseSipUri;
    }

    public boolean isGzipEnabled() {
        return this.mIsGzipEnabled;
    }

    public boolean isLocalConfigUsed() {
        return this.mIsLocalConfigUsed;
    }

    public long getSourceThrottlePublish() {
        return this.mSourceThrottlePublish;
    }

    public long getSourceThrottleSubscribe() {
        return this.mSourceThrottleSubscribe;
    }

    public long getTdelayPublish() {
        return this.mTdelayPublish;
    }

    public long getPublishTimer() {
        return this.mPublishTimer;
    }

    public long getPublishErrRetry() {
        return this.mPublishErrRetry;
    }

    public void setPublishErrRetry(long j) {
        this.mPublishErrRetry = j;
    }

    public long getPublishTimerExtended() {
        return this.mPublishTimerExtended;
    }

    public long getRetryPublishTimer() {
        return this.mRetryPublishTimer;
    }

    public long getBadEventExpiry() {
        return this.mBadEventExpiry;
    }

    public ImsUri getRlsUri() {
        return this.mRlsUri;
    }

    public int getMaxUri() {
        return this.mMaxUri;
    }

    public String getRcsProfile() {
        return this.mRcsProfile;
    }

    public int getDefaultDisc() {
        return this.mDefaultDisc;
    }

    public PresenceConfig(Builder builder) {
        this.mPhoneId = 0;
        this.mPublishTimer = 1200;
        this.mPublishTimerExtended = 86400;
        this.mMaxUri = 100;
        this.mDefaultDisc = 0;
        this.mRcsProfile = "";
        this.mUseAnonymousFetch = builder.useAnonymousFetch;
        this.mIsLocalConfigUsed = builder.isLocalConfigUsed;
        this.mSourceThrottlePublish = builder.sourceThrottlePublish;
        this.mSourceThrottleSubscribe = builder.sourceThrottleSubscribe;
        this.mTdelayPublish = builder.tDelayPublish;
        this.mPublishTimer = builder.publishTimer;
        this.mRetryPublishTimer = builder.retryPublishTimer;
        this.mPublishTimerExtended = builder.publishTimerExtended;
        this.mPublishErrRetry = builder.publishErrRetry;
        this.mBadEventExpiry = builder.badEventExpiry;
        this.mMaxUri = builder.maxUri;
    }

    public String toString() {
        return "PresenceConfig [mPhoneId=" + this.mPhoneId + ", mUseAnonymousFetch=" + this.mUseAnonymousFetch + ", mIsLocalConfigUsed=" + this.mIsLocalConfigUsed + ", mSourceThrottlePublish=" + this.mSourceThrottlePublish + ", mSourceThrottleSubscribe=" + this.mSourceThrottleSubscribe + ", mTdelayPublish=" + this.mTdelayPublish + ", mPublishTimer=" + this.mPublishTimer + ", mRetryPublishTimer=" + this.mRetryPublishTimer + ", mPublishTimerExtended=" + this.mPublishTimerExtended + ", mPublishErrRetry=" + this.mPublishErrRetry + ", mBadEventExpiry=" + this.mBadEventExpiry + ", mMaxUri=" + this.mMaxUri + ", mDefaultDisc=" + this.mDefaultDisc + "]";
    }
}
