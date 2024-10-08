package com.sec.internal.ims.servicemodules.options;

import android.content.ContentResolver;
import android.content.Context;
import android.database.ContentObserver;
import android.net.Uri;
import com.sec.ims.settings.ImsProfile;
import com.sec.internal.constants.Mno;
import com.sec.internal.constants.ims.ImsConstants;
import com.sec.internal.constants.ims.SipMsg;
import com.sec.internal.constants.ims.cmstore.McsConstants;
import com.sec.internal.constants.ims.config.ConfigConstants;
import com.sec.internal.constants.ims.settings.GlobalSettingsConstants;
import com.sec.internal.helper.DmConfigHelper;
import com.sec.internal.helper.RcsConfigurationHelper;
import com.sec.internal.helper.SimUtil;
import com.sec.internal.ims.rcs.RcsPolicyManager;
import com.sec.internal.ims.registry.ImsRegistry;
import com.sec.internal.ims.servicemodules.im.strategy.IMnoStrategy;
import com.sec.internal.ims.settings.RcsPolicySettings;
import com.sec.internal.ims.util.ConfigUtil;
import com.sec.internal.ims.util.ImsUtil;
import com.sec.internal.interfaces.ims.core.IRegistrationManager;
import com.sec.internal.log.IMSLog;
import java.util.List;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.regex.Pattern;
import java.util.regex.PatternSyntaxException;

public class CapabilityConfig {
    private static final String LOG_TAG = "CapabilityConfig";
    private boolean isVzwCapabilitypolicy = false;
    private Set<Pattern> mAllowedPrefixes = ConcurrentHashMap.newKeySet();
    private int mCapCacheExpiry = 7776000;
    private boolean mCapDiscCommonStack = false;
    private int mCapInfoExpiry = 60;
    private Context mContext;
    private boolean mDefaultDisableInitialScan = false;
    private int mDefaultDisc = 0;
    private boolean mDisableInitialScan = false;
    private boolean mForceDisableInitialScan = false;
    protected boolean mIsAvailable = false;
    protected boolean mIsLocalConfigUsed = false;
    private boolean mIsPollingPeriodUpdated = false;
    private boolean mIsRcsUpProfile = false;
    protected boolean mLastSeenActive = false;
    private Mno mMno = Mno.DEFAULT;
    private int mMsgcapvalidity = 30;
    private int mNonRCScapInfoExpiry = 60;
    private int mPhoneId = 0;
    private int mPollListSubExpiry = 3;
    private int mPollingPeriod = 0;
    private int mPollingRate = 10;
    private long mPollingRatePeriod = 10;
    private String mRcsProfile = "";
    private int mServiceAvailabilityInfoExpiry = 60;
    protected int mServiceType = 0;

    public static class Builder {
        int capCacheExpiry = 0;
        int capInfoExpiry = 0;
        int defaultDisc = 0;
        boolean isAvailable = false;
        boolean isLastseenAvailable = false;
        int pollingPeriod = 120;
        int pollingRate = 10;
        long pollingRatePeriod = 10;
    }

    public CapabilityConfig(Context context, int i) {
        this.mContext = context;
        this.mPhoneId = i;
    }

    public void load() {
        IRegistrationManager registrationManager = ImsRegistry.getRegistrationManager();
        if (registrationManager == null) {
            IMSLog.e(LOG_TAG, this.mPhoneId, "load: registrationManager is null");
            return;
        }
        this.mServiceType = 0;
        ImsProfile imsProfile = registrationManager.getImsProfile(this.mPhoneId, ImsProfile.PROFILE_TYPE.RCS);
        boolean z = true;
        if (imsProfile != null) {
            if (imsProfile.hasService(SipMsg.EVENT_PRESENCE)) {
                this.mServiceType = 2;
            } else if (imsProfile.hasService("options")) {
                this.mServiceType = 1;
            }
        }
        if (this.mServiceType == 0) {
            IMSLog.e(LOG_TAG, this.mPhoneId, "load: mServiceType is zero");
            return;
        }
        RcsConfigurationHelper.ConfigData configData = RcsConfigurationHelper.getConfigData(this.mContext, "root/*", this.mPhoneId);
        if (configData == null) {
            IMSLog.e(LOG_TAG, this.mPhoneId, "load: configData is not found");
            return;
        }
        String rcsProfileWithFeature = ConfigUtil.getRcsProfileWithFeature(this.mContext, this.mPhoneId, imsProfile);
        this.mRcsProfile = rcsProfileWithFeature;
        this.mIsRcsUpProfile = ImsProfile.isRcsUpProfile(rcsProfileWithFeature);
        IMnoStrategy rcsStrategy = RcsPolicyManager.getRcsStrategy(this.mPhoneId);
        this.mIsLocalConfigUsed = rcsStrategy != null && rcsStrategy.isLocalConfigUsed();
        this.mMno = SimUtil.getSimMno(this.mPhoneId);
        int intValue = configData.readInt("version", 0).intValue();
        this.mDefaultDisc = configData.readInt(ConfigConstants.ConfigTable.CAPDISCOVERY_DEFAULT_DISC, 0).intValue();
        notifyDefaultDiscChange();
        this.mIsAvailable = intValue > 0 && (this.mDefaultDisc != 2 || this.mIsRcsUpProfile);
        Boolean bool = Boolean.FALSE;
        this.mCapDiscCommonStack = configData.readBool(ConfigConstants.ConfigTable.CAPDISCOVERY_CAP_DISC_COMMON_STACK, bool).booleanValue();
        boolean z2 = ImsRegistry.getBoolean(this.mPhoneId, GlobalSettingsConstants.RCS.RCS_DEFAULT_DISABLE_INITIAL_SCAN, false);
        this.mDefaultDisableInitialScan = z2;
        this.mDisableInitialScan = configData.readBool(ConfigConstants.ConfigTable.CAPDISCOVERY_DISABLE_INITIAL_SCAN, Boolean.valueOf(z2)).booleanValue();
        boolean z3 = ImsRegistry.getBoolean(this.mPhoneId, GlobalSettingsConstants.RCS.RCS_FORCE_DISABLE_INITIAL_SCAN, false);
        this.mForceDisableInitialScan = z3;
        if (!z3) {
            z3 = this.mDisableInitialScan;
        }
        this.mDisableInitialScan = z3;
        this.mPollingRate = configData.readInt(ConfigConstants.ConfigTable.CAPDISCOVERY_POLLING_RATE, 10).intValue();
        this.mPollingRatePeriod = (long) configData.readInt(ConfigConstants.ConfigTable.CAPDISCOVERY_POLLING_RATE_PERIOD, 10).intValue();
        StringBuilder sb = new StringBuilder();
        if (this.mPollingRate == 0 && this.mPollingRatePeriod == 0) {
            this.mPollingRate = 10;
            this.mPollingRatePeriod = 3;
            sb.append("load: change mPollingRate to ");
            sb.append(this.mPollingRate);
            sb.append(" and change mPollingRatePeriod to ");
            sb.append(this.mPollingRatePeriod);
        }
        String acsServerType = ConfigUtil.getAcsServerType(this.mPhoneId);
        if (this.mMno != Mno.VZW || ImsConstants.RCS_AS.JIBE.equals(acsServerType)) {
            z = false;
        }
        this.isVzwCapabilitypolicy = z;
        int i = !z ? 0 : 625000;
        updatePollingPeriod(i, imsProfile.getCapPollInterval(), configData.readInt(ConfigConstants.ConfigTable.CAPDISCOVERY_POLLING_PERIOD, Integer.valueOf(i)).intValue());
        int i2 = !this.mIsRcsUpProfile ? 60 : McsConstants.ServerConfig.DEFAULT_CMS_TTL_VALUE;
        if (this.isVzwCapabilitypolicy && !this.mIsLocalConfigUsed) {
            i2 = 604800;
        }
        updateCapInfoExpiry(i2, imsProfile.getAvailCacheExpiry(), configData.readInt(ConfigConstants.ConfigTable.CAPDISCOVERY_CAPINFO_EXPIRY, Integer.valueOf(i2)).intValue());
        this.mNonRCScapInfoExpiry = configData.readInt(ConfigConstants.ConfigTable.CAPDISCOVERY_NON_RCS_CAPINFO_EXPIRY, Integer.valueOf(this.mCapInfoExpiry)).intValue();
        updateCapDiscoveryAllowedPrefixes(configData.readListString(ConfigConstants.ConfigTable.CAPDISCOVERY_ALLOWED_PREFIXES));
        int intValue2 = configData.readInt(ConfigConstants.ConfigTable.CLIENTCONTROL_SERVICE_AVAILABILITY_INFO_EXPIRY, 60).intValue();
        this.mServiceAvailabilityInfoExpiry = intValue2;
        int i3 = 30;
        if (!this.mIsRcsUpProfile || (this.isVzwCapabilitypolicy && this.mIsLocalConfigUsed)) {
            this.mMsgcapvalidity = configData.readInt(ConfigConstants.ConfigTable.CAPDISCOVERY_JOYN_MSGCAPVALIDITY, 30).intValue();
        } else {
            this.mMsgcapvalidity = configData.readInt(ConfigConstants.ConfigTable.CAPDISCOVERY_JOYN_MSGCAPVALIDITY, Integer.valueOf(intValue2)).intValue();
        }
        this.mLastSeenActive = configData.readBool(ConfigConstants.ConfigTable.CAPDISCOVERY_JOYN_LASTSEENACTIVE, bool).booleanValue();
        if (!this.isVzwCapabilitypolicy) {
            i3 = 0;
        }
        this.mPollListSubExpiry = DmConfigHelper.readInt(this.mContext, ConfigConstants.ConfigPath.OMADM_POLL_LIST_SUB_EXP, Integer.valueOf(i3), this.mPhoneId).intValue();
        if (rcsStrategy != null) {
            this.mCapCacheExpiry = !this.isVzwCapabilitypolicy ? this.mCapInfoExpiry : 7776000;
            if (rcsStrategy.boolSetting(RcsPolicySettings.RcsPolicy.USE_CAPCACHE_EXPIRY)) {
                this.mCapCacheExpiry += DmConfigHelper.readInt(this.mContext, ConfigConstants.ConfigPath.OMADM_CAP_CACHE_EXP, 0, this.mPhoneId).intValue();
            } else {
                this.mCapCacheExpiry = DmConfigHelper.readInt(this.mContext, ConfigConstants.ConfigPath.OMADM_CAP_CACHE_EXP, 0, this.mPhoneId).intValue();
            }
            rcsStrategy.updateCapDiscoveryOption();
        }
        sb.append(" load: mServiceType: ");
        sb.append(this.mServiceType);
        sb.append(" mRcsProfile: ");
        sb.append(this.mRcsProfile);
        sb.append(" mIsRcsUpProfile: ");
        sb.append(this.mIsRcsUpProfile);
        sb.append(" mIsLocalConfigUsed: ");
        sb.append(this.mIsLocalConfigUsed);
        sb.append(" rcsVersion: ");
        sb.append(intValue);
        sb.append(" mDefaultDisc: ");
        sb.append(this.mDefaultDisc);
        sb.append(" mIsAvailable: ");
        sb.append(this.mIsAvailable);
        sb.append(" mCapDiscCommonStack: ");
        sb.append(this.mCapDiscCommonStack);
        sb.append(" mDisableInitialScan: ");
        sb.append(this.mDisableInitialScan);
        sb.append(" mDefaultDisableInitialScan: ");
        sb.append(this.mDefaultDisableInitialScan);
        sb.append(" mForceDisableInitialScan: ");
        sb.append(this.mForceDisableInitialScan);
        sb.append(" mPollingRate: ");
        sb.append(this.mPollingRate);
        sb.append(" mPollingRatePeriod: ");
        sb.append(this.mPollingRatePeriod);
        sb.append(" mNonRCScapInfoExpiry: ");
        sb.append(this.mNonRCScapInfoExpiry);
        sb.append(" mMsgcapvalidity: ");
        sb.append(this.mMsgcapvalidity);
        sb.append(" mServiceAvailabilityInfoExpiry: ");
        sb.append(this.mServiceAvailabilityInfoExpiry);
        sb.append(" mLastSeenActive: ");
        sb.append(this.mLastSeenActive);
        sb.append(" mPollListSubExpiry: ");
        sb.append(this.mPollListSubExpiry);
        sb.append(" mCapCacheExpiry: ");
        sb.append(this.mCapCacheExpiry);
        IMSLog.i(LOG_TAG, this.mPhoneId, sb.toString());
    }

    public int getCapInfoExpiry() {
        return this.mCapInfoExpiry;
    }

    public int getNonRCScapInfoExpiry() {
        return this.mNonRCScapInfoExpiry;
    }

    public long getCapCacheExpiry() {
        return (long) this.mCapCacheExpiry;
    }

    public boolean isDisableInitialScan() {
        return this.mDisableInitialScan;
    }

    public int getPollingPeriod() {
        return this.mPollingPeriod;
    }

    public int getPollListSubExpiry() {
        return this.mPollListSubExpiry;
    }

    public int getPollingRate() {
        return this.mPollingRate;
    }

    public long getPollingRatePeriod() {
        return this.mPollingRatePeriod;
    }

    public long getMsgcapvalidity() {
        return (long) this.mMsgcapvalidity;
    }

    public boolean isPollingPeriodUpdated() {
        return this.mIsPollingPeriodUpdated;
    }

    public void resetPollingPeriodUpdated() {
        this.mIsPollingPeriodUpdated = false;
    }

    public boolean isLastSeenActive() {
        return this.mLastSeenActive;
    }

    public boolean usePresence() {
        return this.mDefaultDisc == 1;
    }

    public void setUsePresence(boolean z) {
        this.mDefaultDisc = z ? 1 : 0;
    }

    public boolean isAvailable() {
        return this.mIsAvailable;
    }

    public Set<Pattern> getCapAllowedPrefixes() {
        return this.mAllowedPrefixes;
    }

    public String getRcsProfile() {
        return this.mRcsProfile;
    }

    public int getServiceAvailabilityInfoExpiry() {
        return this.mServiceAvailabilityInfoExpiry;
    }

    public int getDefaultDisc() {
        return this.mDefaultDisc;
    }

    /* access modifiers changed from: package-private */
    public int getDefaultDisc(int i) {
        return RcsConfigurationHelper.readIntParam(this.mContext, ImsUtil.getPathWithPhoneId(ConfigConstants.ConfigTable.CAPDISCOVERY_DEFAULT_DISC, i), 2).intValue();
    }

    private void notifyDefaultDiscChange() {
        ContentResolver contentResolver = this.mContext.getContentResolver();
        StringBuilder sb = new StringBuilder();
        Uri uri = ConfigConstants.CONTENT_URI;
        sb.append(uri);
        sb.append("root/application/1/capdiscovery/defaultdisc");
        contentResolver.notifyChange(Uri.parse(sb.toString()), (ContentObserver) null);
        contentResolver.notifyChange(Uri.parse(uri + "parameter/defaultdisc"), (ContentObserver) null);
    }

    /* access modifiers changed from: package-private */
    public void updatePollingPeriod(int i, int i2, int i3) {
        StringBuilder sb = new StringBuilder("updatePollingPeriod() ");
        boolean z = true;
        if (this.mIsLocalConfigUsed) {
            int intValue = DmConfigHelper.readInt(this.mContext, ConfigConstants.ConfigPath.OMADM_CAP_POLL_INTERVAL, Integer.valueOf(i), this.mPhoneId).intValue();
            sb.append(", capPollIntervalFromDM: ");
            sb.append(intValue);
            if (intValue > 0) {
                if (this.mPollingPeriod == intValue) {
                    z = false;
                }
                this.mIsPollingPeriodUpdated = z;
                this.mPollingPeriod = intValue;
            } else {
                if (this.mPollingPeriod == i) {
                    z = false;
                }
                this.mIsPollingPeriodUpdated = z;
                this.mPollingPeriod = i;
            }
        } else if (this.mServiceType != 2 || i2 <= 0 || this.isVzwCapabilitypolicy) {
            sb.append(", pollingPeriodFromConfigDB: ");
            sb.append(i3);
            if (i3 >= 0) {
                if (this.mPollingPeriod == i3) {
                    z = false;
                }
                this.mIsPollingPeriodUpdated = z;
                this.mPollingPeriod = i3;
            } else {
                if (this.mPollingPeriod == i) {
                    z = false;
                }
                this.mIsPollingPeriodUpdated = z;
                this.mPollingPeriod = i;
            }
        } else {
            sb.append(", capPollIntervalFromProfile: ");
            sb.append(i2);
            if (this.mPollingPeriod == i2) {
                z = false;
            }
            this.mIsPollingPeriodUpdated = z;
            this.mPollingPeriod = i2;
        }
        sb.append(", mPollingPeriod: ");
        sb.append(this.mPollingPeriod);
        sb.append(", mIsPollingPeriodUpdated: ");
        sb.append(this.mIsPollingPeriodUpdated);
        IMSLog.i(LOG_TAG, this.mPhoneId, sb.toString());
    }

    /* access modifiers changed from: package-private */
    public void updateCapInfoExpiry(int i, int i2, int i3) {
        StringBuilder sb = new StringBuilder("updateCapInfoExpiry() ");
        if (this.mIsLocalConfigUsed) {
            int intValue = DmConfigHelper.readInt(this.mContext, ConfigConstants.ConfigPath.OMADM_AVAIL_CACHE_EXP, 60, this.mPhoneId).intValue();
            sb.append(", availCacheExpFromDM: ");
            sb.append(intValue);
            if (intValue > 0) {
                this.mCapInfoExpiry = intValue;
            } else {
                this.mCapInfoExpiry = 60;
            }
        } else if (this.mServiceType != 2 || i2 <= 0 || this.isVzwCapabilitypolicy) {
            sb.append(", capInfoExpiryFromConfigDB: ");
            sb.append(i3);
            if (i3 > 0) {
                sb.append(", use capInfoExpiryFromConfigDB: ");
                this.mCapInfoExpiry = i3;
            } else if (i3 == 0 && ConfigUtil.isRcsEur(this.mPhoneId)) {
                sb.append(", change capInfoExpiryFromConfigDB to ");
                sb.append(i);
                sb.append(" for eur");
                this.mCapInfoExpiry = i;
            } else if (i3 != 0 || !this.mIsRcsUpProfile || this.isVzwCapabilitypolicy) {
                sb.append(", use defaultCapInfoExpiry");
                this.mCapInfoExpiry = i;
            } else {
                this.mCapInfoExpiry = i3;
            }
        } else {
            sb.append(", profileCapInfoExpiry: ");
            sb.append(i2);
            this.mCapInfoExpiry = i2;
        }
        sb.append(", mCapInfoExpiry: ");
        sb.append(this.mCapInfoExpiry);
        IMSLog.i(LOG_TAG, this.mPhoneId, sb.toString());
    }

    private void updateCapDiscoveryAllowedPrefixes(List<String> list) {
        Pattern pattern;
        int i = this.mPhoneId;
        IMSLog.i(LOG_TAG, i, "updateCapDiscoveryAllowedPrefixes: allowedPrefixes = " + list);
        for (String next : list) {
            if (next.startsWith("!")) {
                try {
                    pattern = Pattern.compile(next.substring(1));
                } catch (PatternSyntaxException unused) {
                    int i2 = this.mPhoneId;
                    IMSLog.e(LOG_TAG, i2, "updateCapDiscoveryAllowedPrefixes: patternSyntaxException on prefix: " + next.substring(1));
                    pattern = null;
                }
            } else {
                pattern = Pattern.compile("^(" + next.replaceAll("\\+", "\\\\+") + ")");
            }
            if (pattern != null) {
                this.mAllowedPrefixes.add(pattern);
            }
        }
    }

    public CapabilityConfig(Builder builder) {
        this.mCapInfoExpiry = builder.capInfoExpiry;
        this.mCapCacheExpiry = builder.capCacheExpiry;
        this.mPollingPeriod = builder.pollingPeriod;
        this.mPollingRate = builder.pollingRate;
        this.mPollingRatePeriod = builder.pollingRatePeriod;
        this.mDefaultDisc = builder.defaultDisc;
        this.mIsAvailable = builder.isAvailable;
        this.mLastSeenActive = builder.isLastseenAvailable;
    }

    public String toString() {
        return "CapabilityConfig [mContext=" + this.mContext + ", mPhoneId=" + this.mPhoneId + ", mCapInfoExpiry=" + this.mCapInfoExpiry + ", mNonRCScapInfoExpiry=" + this.mNonRCScapInfoExpiry + ", mPollingPeriod=" + this.mPollingPeriod + ", mCapCacheExpiry=" + this.mCapCacheExpiry + ", mPollingRate=" + this.mPollingRate + ", mPollListSubExpiry=" + this.mPollListSubExpiry + ", mPollingRatePeriod=" + this.mPollingRatePeriod + ", mServiceAvailabilityInfoExpiry=" + this.mServiceAvailabilityInfoExpiry + ", mDefaultDisc=" + this.mDefaultDisc + ", mIsLocalConfigUsed=" + this.mIsLocalConfigUsed + ", mIsPollingPeriodUpdated=" + this.mIsPollingPeriodUpdated + ", mDisableInitialScan=" + this.mDisableInitialScan + ", mDefaultDisableInitialScan=" + this.mDefaultDisableInitialScan + ", mForceDisableInitialScan=" + this.mForceDisableInitialScan + ", mAllowedPrefixes=" + this.mAllowedPrefixes + ", mCapDiscCommonStack=" + this.mCapDiscCommonStack + "]";
    }
}
