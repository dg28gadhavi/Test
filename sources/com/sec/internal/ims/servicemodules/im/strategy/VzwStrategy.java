package com.sec.internal.ims.servicemodules.im.strategy;

import android.content.Context;
import android.os.SemSystemProperties;
import android.telephony.TelephonyManager;
import com.sec.ims.ImsRegistration;
import com.sec.ims.extensions.TelephonyManagerExt;
import com.sec.ims.options.Capabilities;
import com.sec.ims.options.CapabilityRefreshType;
import com.sec.ims.presence.ServiceTuple;
import com.sec.ims.util.ImsUri;
import com.sec.internal.constants.ims.ImsConstants;
import com.sec.internal.constants.ims.config.ConfigConstants;
import com.sec.internal.constants.ims.os.PhoneConstants;
import com.sec.internal.constants.ims.servicemodules.im.ChatData;
import com.sec.internal.constants.ims.servicemodules.im.ImError;
import com.sec.internal.constants.ims.servicemodules.options.CapabilityConstants;
import com.sec.internal.constants.ims.servicemodules.presence.PresenceResponse;
import com.sec.internal.constants.ims.servicemodules.presence.PresenceSubscription;
import com.sec.internal.helper.DmConfigHelper;
import com.sec.internal.ims.registry.ImsRegistry;
import com.sec.internal.ims.servicemodules.im.strategy.IMnoStrategy;
import com.sec.internal.interfaces.ims.servicemodules.options.ICapabilityDiscoveryModule;
import com.sec.internal.log.IMSLog;
import java.util.Date;

public final class VzwStrategy extends DefaultRCSMnoStrategy {
    private static final String TAG = "VzwStrategy";
    private int lastNetworkType;
    private ICapabilityDiscoveryModule mDiscoveryModule = ImsRegistry.getServiceModuleManager().getCapabilityDiscoveryModule();
    private boolean mIsCapDiscoveryOption;
    private boolean mIsEABEnabled;
    private boolean mIsVLTEnabled;
    private final TelephonyManager mTelephonyManager = ((TelephonyManager) this.mContext.getSystemService(PhoneConstants.PHONE_KEY));

    public long getThrottledDelay(long j) {
        return j + 3;
    }

    public boolean isLocalConfigUsed() {
        return true;
    }

    public boolean isPresenceReadyToRequest(boolean z, boolean z2) {
        return z && !z2;
    }

    public boolean needPoll(Capabilities capabilities, long j) {
        return true;
    }

    public VzwStrategy(Context context, int i) {
        super(context, i);
        Boolean bool = Boolean.FALSE;
        this.mIsVLTEnabled = DmConfigHelper.readBool(context, ConfigConstants.ConfigPath.OMADM_VOLTE_ENABLED, bool, i).booleanValue();
        this.mIsEABEnabled = DmConfigHelper.readBool(context, ConfigConstants.ConfigPath.OMADM_EAB_SETTING, bool, i).booleanValue();
        this.mIsCapDiscoveryOption = DmConfigHelper.readBool(context, ConfigConstants.ConfigPath.OMADM_CAP_DISCOVERY, bool, i).booleanValue();
    }

    public IMnoStrategy.StrategyResponse handleSendingMessageFailure(ImError imError, int i, int i2, ChatData.ChatType chatType, boolean z, boolean z2) {
        if (z) {
            return handleSlmFailure(imError);
        }
        IMnoStrategy.StatusCode retryStrategy = getRetryStrategy(i, imError, i2, chatType);
        return retryStrategy == IMnoStrategy.StatusCode.NO_RETRY ? handleImFailure(imError, chatType) : new IMnoStrategy.StrategyResponse(retryStrategy);
    }

    public IMnoStrategy.StrategyResponse handleSendingFtMsrpMessageFailure(ImError imError, int i, int i2, ChatData.ChatType chatType, boolean z) {
        IMnoStrategy.StatusCode ftMsrpRetryStrategy = getFtMsrpRetryStrategy(i, imError, i2);
        if (ftMsrpRetryStrategy == IMnoStrategy.StatusCode.NO_RETRY) {
            return handleFtFailure(imError, chatType);
        }
        return new IMnoStrategy.StrategyResponse(ftMsrpRetryStrategy);
    }

    public boolean isCapabilityValidUri(ImsUri imsUri) {
        return StrategyUtils.isCapabilityValidUriForUS(imsUri, this.mPhoneId);
    }

    public boolean needCapabilitiesUpdate(CapabilityConstants.CapExResult capExResult, Capabilities capabilities, long j, long j2) {
        if (capExResult != CapabilityConstants.CapExResult.USER_NOT_FOUND && capExResult == CapabilityConstants.CapExResult.FAILURE && capabilities != null && capabilities.isAvailable()) {
            return isCapCacheExpired(capabilities, j2);
        }
        return true;
    }

    public boolean needRefresh(Capabilities capabilities, CapabilityRefreshType capabilityRefreshType, long j, long j2, long j3, long j4) {
        return needRefresh(capabilities, capabilityRefreshType, j, j3);
    }

    public boolean needRefresh(Capabilities capabilities, CapabilityRefreshType capabilityRefreshType, long j, long j2) {
        ICapabilityDiscoveryModule iCapabilityDiscoveryModule = this.mDiscoveryModule;
        if (iCapabilityDiscoveryModule != null && !iCapabilityDiscoveryModule.hasVideoOwnCapability(this.mPhoneId)) {
            IMSLog.i(TAG, this.mPhoneId, "needRefresh: no videoOwnCapability");
            return false;
        } else if (capabilityRefreshType == CapabilityRefreshType.DISABLED) {
            IMSLog.i(TAG, this.mPhoneId, "needRefresh: availability fetch disabled");
            return false;
        } else if (capabilityRefreshType != CapabilityRefreshType.ALWAYS_FORCE_REFRESH && capabilityRefreshType != CapabilityRefreshType.ONLY_IF_NOT_FRESH) {
            IMSLog.i(TAG, this.mPhoneId, "needRefresh: cannot process this availability fetch type");
            return false;
        } else if (capabilities == null) {
            IMSLog.i(TAG, this.mPhoneId, "needRefresh: capex is unknown");
            return true;
        } else if (isCapCacheExpired(capabilities, j2)) {
            IMSLog.i(TAG, this.mPhoneId, "needRefresh: capex is reset");
            return true;
        } else if (capabilityRefreshType == CapabilityRefreshType.ALWAYS_FORCE_REFRESH) {
            IMSLog.i(TAG, this.mPhoneId, "needRefresh: availability fetch forced");
            return true;
        } else if (capabilities.hasFeature(Capabilities.FEATURE_NON_RCS_USER)) {
            IMSLog.i(TAG, this.mPhoneId, "needRefresh: capex is nonRcsUser");
            return false;
        } else if (capabilities.isExpired(j)) {
            IMSLog.i(TAG, this.mPhoneId, "needRefresh: capex is expired based on capInfoExpiry or capex is reset");
            return true;
        } else {
            IMSLog.i(TAG, this.mPhoneId, "needRefresh: no need refresh");
            return false;
        }
    }

    private boolean isCapCacheExpired(Capabilities capabilities, long j) {
        boolean z = false;
        if (capabilities != null) {
            Date date = new Date();
            if (date.getTime() - capabilities.getTimestamp().getTime() >= 1000 * j && j > 0) {
                z = true;
            }
            if (z) {
                capabilities.resetFeatures();
                String str = TAG;
                int i = this.mPhoneId;
                IMSLog.i(str, i, "isCapCacheExpired: " + j + " current " + date.getTime() + " timestamp " + capabilities.getTimestamp().getTime() + " diff " + (date.getTime() - capabilities.getTimestamp().getTime()));
            }
            return z;
        }
        IMSLog.i(TAG, this.mPhoneId, "isCapCacheExpired: Capability is null");
        return false;
    }

    public long updateFeatures(Capabilities capabilities, long j, CapabilityConstants.CapExResult capExResult) {
        if (capabilities == null || capabilities.hasFeature(Capabilities.FEATURE_NOT_UPDATED) || capabilities.hasFeature(Capabilities.FEATURE_NON_RCS_USER)) {
            return j;
        }
        String str = TAG;
        int i = this.mPhoneId;
        IMSLog.i(str, i, "updateFeatures: updated features " + Capabilities.dumpFeature(capabilities.getFeature() | j));
        return capabilities.getFeature() | j;
    }

    public long isTdelay(long j) {
        boolean z = SemSystemProperties.getBoolean("ro.ril.svlte1x", false);
        if (z || j < 1) {
            String str = TAG;
            int i = this.mPhoneId;
            IMSLog.i(str, i, "SVLTE: " + z + ", delay: " + j);
            return 0;
        }
        int networkType = this.mTelephonyManager.getNetworkType();
        TelephonyManagerExt.NetworkTypeExt networkEnumType = TelephonyManagerExt.getNetworkEnumType(this.lastNetworkType);
        TelephonyManagerExt.NetworkTypeExt networkEnumType2 = TelephonyManagerExt.getNetworkEnumType(networkType);
        String str2 = TAG;
        int i2 = this.mPhoneId;
        IMSLog.i(str2, i2, "SRLTE, current network: " + networkEnumType2 + ", last network type : " + networkEnumType);
        this.lastNetworkType = networkType;
        if (networkEnumType == TelephonyManagerExt.NetworkTypeExt.NETWORK_TYPE_EHRPD && networkEnumType2 == TelephonyManagerExt.NetworkTypeExt.NETWORK_TYPE_LTE) {
            return (j - 1) * 1000;
        }
        return 0;
    }

    public boolean needUnpublish(int i) {
        TelephonyManagerExt.NetworkTypeExt networkEnumType = TelephonyManagerExt.getNetworkEnumType(this.mTelephonyManager.getNetworkType());
        if (networkEnumType == TelephonyManagerExt.NetworkTypeExt.NETWORK_TYPE_EHRPD) {
            String str = TAG;
            int i2 = this.mPhoneId;
            IMSLog.i(str, i2, "needUnpublish: network type: " + networkEnumType);
            return false;
        }
        boolean z = DmConfigHelper.getImsSwitchValue(this.mContext, "volte", i) == 1;
        if (!z) {
            String str2 = TAG;
            int i3 = this.mPhoneId;
            IMSLog.i(str2, i3, "needUnpublish: isVoLteEnabled: " + z);
            return true;
        } else if (DmConfigHelper.getImsSwitchValue(this.mContext, "mmtel", i) == 1 || DmConfigHelper.getImsSwitchValue(this.mContext, "mmtel-video", i) == 1) {
            return false;
        } else {
            IMSLog.i(TAG, this.mPhoneId, "needUnpublish: mmtel/mmtel-video: off");
            return true;
        }
    }

    public boolean needUnpublish(ImsRegistration imsRegistration, ImsRegistration imsRegistration2) {
        if (imsRegistration == null) {
            IMSLog.i(TAG, this.mPhoneId, "needUnpublish: oldInfo: empty");
            return false;
        }
        int i = ImsConstants.SystemSettings.VOLTE_SLOT1.get(this.mContext, 0);
        String str = TAG;
        int i2 = this.mPhoneId;
        StringBuilder sb = new StringBuilder();
        sb.append("needUnpublish: getVoiceTechType: ");
        sb.append(i == 0 ? "VOLTE" : "CS");
        IMSLog.i(str, i2, sb.toString());
        if ((imsRegistration.hasService("mmtel") || imsRegistration.hasService("mmtel-video")) && !imsRegistration2.hasService("mmtel") && !imsRegistration2.hasService("mmtel-video") && i == 1) {
            return true;
        }
        return false;
    }

    public boolean isSubscribeThrottled(PresenceSubscription presenceSubscription, long j, boolean z, boolean z2) {
        if (z2) {
            IMSLog.i(TAG, this.mPhoneId, "refresh type is always force.");
            return false;
        } else if (!z || !(presenceSubscription.getRequestType() == CapabilityConstants.RequestType.REQUEST_TYPE_PERIODIC || presenceSubscription.getRequestType() == CapabilityConstants.RequestType.REQUEST_TYPE_CONTACT_CHANGE)) {
            Date date = new Date();
            long time = date.getTime() - presenceSubscription.getTimestamp().getTime();
            String str = TAG;
            int i = this.mPhoneId;
            IMSLog.i(str, i, "isSubscribeThrottled: interval from " + presenceSubscription.getTimestamp().getTime() + " to " + date.getTime() + ", offset " + time + " sourceThrottlePublish " + j);
            if (time < j) {
                return true;
            }
            return false;
        } else {
            IMSLog.i(TAG, this.mPhoneId, "isSubscribeThrottled: avail fetch after poll, not throttled");
            return false;
        }
    }

    public void startServiceBasedOnOmaDmNodes(int i) {
        String str = TAG;
        IMSLog.i(str, this.mPhoneId, "startServiceBasedOnOmaDmNodes");
        if (this.mDiscoveryModule != null) {
            int i2 = this.mPhoneId;
            IMSLog.i(str, i2, "startServiceBasedOnOmaDmNodes: mIsVLTEnabled: " + this.mIsVLTEnabled + " mIsEABEnabled: " + this.mIsEABEnabled);
            if (!this.mIsVLTEnabled) {
                this.mDiscoveryModule.clearCapabilitiesCache(i);
                this.mDiscoveryModule.changeParalysed(true, i);
            }
        }
    }

    public void updateOmaDmNodes(int i) {
        boolean z;
        Context context = this.mContext;
        Boolean bool = Boolean.FALSE;
        boolean booleanValue = DmConfigHelper.readBool(context, ConfigConstants.ConfigPath.OMADM_VOLTE_ENABLED, bool, i).booleanValue();
        boolean z2 = true;
        if (this.mIsVLTEnabled != booleanValue) {
            this.mIsVLTEnabled = booleanValue;
            z = true;
        } else {
            z = false;
        }
        boolean booleanValue2 = DmConfigHelper.readBool(this.mContext, ConfigConstants.ConfigPath.OMADM_EAB_SETTING, bool, i).booleanValue();
        if (this.mIsEABEnabled != booleanValue2) {
            this.mIsEABEnabled = booleanValue2;
        } else {
            z2 = z;
        }
        IMSLog.i(TAG, this.mPhoneId, "updateOmaDmNodes: mIsVLTEnabled: " + this.mIsVLTEnabled + " mIsEABEnabled: " + this.mIsEABEnabled + " modified = " + z2);
        if (z2) {
            startServiceBasedOnOmaDmNodes(i);
        }
    }

    public String checkNeedParsing(String str) {
        if (str == null) {
            return str;
        }
        if (!str.startsWith("*67") && !str.startsWith("*82")) {
            return str;
        }
        String substring = str.substring(3);
        IMSLog.i(TAG, this.mPhoneId, "parsing for special character");
        return substring;
    }

    public void updateCapDiscoveryOption() {
        this.mIsCapDiscoveryOption = DmConfigHelper.readBool(this.mContext, ConfigConstants.ConfigPath.OMADM_CAP_DISCOVERY, Boolean.FALSE, this.mPhoneId).booleanValue();
        String str = TAG;
        int i = this.mPhoneId;
        IMSLog.i(str, i, "update CapDiscoveryOption: " + this.mIsCapDiscoveryOption);
    }

    public boolean checkCapDiscoveryOption() {
        if (TelephonyManagerExt.getNetworkEnumType(this.mTelephonyManager.getNetworkType()) != TelephonyManagerExt.NetworkTypeExt.NETWORK_TYPE_LTE) {
            return true;
        }
        boolean z = this.mIsCapDiscoveryOption;
        String str = TAG;
        int i = this.mPhoneId;
        IMSLog.i(str, i, "return CapDiscoveryOption: " + z);
        return z;
    }

    public PresenceResponse.PresenceStatusCode handlePresenceFailure(PresenceResponse.PresenceFailureReason presenceFailureReason, boolean z) {
        if (presenceFailureReason.isOneOf(PresenceResponse.PresenceFailureReason.FORBIDDEN)) {
            return PresenceResponse.PresenceStatusCode.PRESENCE_FORBIDDEN;
        }
        if (presenceFailureReason.isOneOf(PresenceResponse.PresenceFailureReason.USER_NOT_REGISTERED)) {
            return PresenceResponse.PresenceStatusCode.PRESENCE_AT_NOT_REGISTERED;
        }
        if (presenceFailureReason.isOneOf(PresenceResponse.PresenceFailureReason.USER_NOT_PROVISIONED)) {
            return PresenceResponse.PresenceStatusCode.PRESENCE_AT_NOT_PROVISIONED;
        }
        if (presenceFailureReason.isOneOf(PresenceResponse.PresenceFailureReason.USER_NOT_FOUND)) {
            return PresenceResponse.PresenceStatusCode.PRESENCE_NOT_FOUND;
        }
        if (presenceFailureReason.isOneOf(PresenceResponse.PresenceFailureReason.REQUEST_TIMEOUT)) {
            return PresenceResponse.PresenceStatusCode.PRESENCE_REQUEST_TIMEOUT;
        }
        if (presenceFailureReason.isOneOf(PresenceResponse.PresenceFailureReason.INTERVAL_TOO_SHORT)) {
            return PresenceResponse.PresenceStatusCode.PRESENCE_INTERVAL_TOO_SHORT;
        }
        if (presenceFailureReason.isOneOf(PresenceResponse.PresenceFailureReason.NO_RESPONSE)) {
            return PresenceResponse.PresenceStatusCode.PRESENCE_NO_RESPONSE;
        }
        if (presenceFailureReason.isOneOf(PresenceResponse.PresenceFailureReason.SERVER_INTERNAL_ERROR, PresenceResponse.PresenceFailureReason.SERVICE_UNAVAILABLE, PresenceResponse.PresenceFailureReason.DECLINE)) {
            return PresenceResponse.PresenceStatusCode.PRESENCE_RETRY_EXP_BACKOFF;
        }
        if (presenceFailureReason.isOneOf(PresenceResponse.PresenceFailureReason.REQUEST_TIMEOUT_RETRY, PresenceResponse.PresenceFailureReason.CONDITIONAL_REQUEST_FAILED)) {
            return PresenceResponse.PresenceStatusCode.PRESENCE_REQUIRE_FULL_PUBLISH;
        }
        return PresenceResponse.PresenceStatusCode.PRESENCE_DISABLE_MODE;
    }

    public void changeServiceDescription() {
        IMSLog.i(TAG, this.mPhoneId, "changeServiceDescription: VoLTE Capabilities Discovery");
        ServiceTuple.setServiceDescription((long) Capabilities.FEATURE_PRESENCE_DISCOVERY, "VoLTE Capabilities Discovery");
    }

    public Capabilities getCapabilitiesInitialInfo(int i, ImsUri imsUri, String str, String str2, long j, String str3) {
        Capabilities capabilities = new Capabilities(imsUri, str, str2, j, str3);
        capabilities.resetFeatures();
        capabilities.setPhoneId(i);
        capabilities.setTimestamp(new Date(0));
        return capabilities;
    }
}
