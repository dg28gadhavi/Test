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
import com.sec.internal.ims.util.ChatbotUriUtil;
import com.sec.internal.ims.util.ConfigUtil;
import com.sec.internal.interfaces.ims.servicemodules.im.IImModule;
import com.sec.internal.interfaces.ims.servicemodules.options.ICapabilityDiscoveryModule;
import com.sec.internal.log.IMSLog;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashSet;
import java.util.Set;

public final class VzwUPStrategy extends DefaultUPMnoStrategy {
    private static final String TAG = "VzwUPStrategy";
    private int lastNetworkType;
    private ICapabilityDiscoveryModule mDiscoveryModule = ImsRegistry.getServiceModuleManager().getCapabilityDiscoveryModule();
    private final HashSet<ImError> mForceRefreshRemoteCapa = new HashSet<>();
    private boolean mIsCapDiscoveryOption;
    private boolean mIsEABEnabled;
    private boolean mIsLocalConfigUsed;
    private boolean mIsVLTEnabled;
    private final TelephonyManager mTelephonyManager = ((TelephonyManager) this.mContext.getSystemService(PhoneConstants.PHONE_KEY));

    public long getThrottledDelay(long j) {
        return j + 3;
    }

    public boolean isPresenceReadyToRequest(boolean z, boolean z2) {
        return z && !z2;
    }

    public boolean needPoll(Capabilities capabilities, long j) {
        return true;
    }

    public VzwUPStrategy(Context context, int i) {
        super(context, i);
        Boolean bool = Boolean.FALSE;
        this.mIsVLTEnabled = DmConfigHelper.readBool(context, ConfigConstants.ConfigPath.OMADM_VOLTE_ENABLED, bool, i).booleanValue();
        this.mIsEABEnabled = DmConfigHelper.readBool(context, ConfigConstants.ConfigPath.OMADM_EAB_SETTING, bool, i).booleanValue();
        this.mIsCapDiscoveryOption = DmConfigHelper.readBool(context, ConfigConstants.ConfigPath.OMADM_CAP_DISCOVERY, bool, i).booleanValue();
        init();
    }

    private void init() {
        this.mForceRefreshRemoteCapa.add(ImError.REMOTE_USER_INVALID);
    }

    public boolean isCustomizedFeature(long j) {
        IImModule imModule;
        if (j != ((long) Capabilities.FEATURE_FT_VIA_SMS) || (imModule = getImModule()) == null || !imModule.getImConfig(this.mPhoneId).getFtHttpEnabled()) {
            return false;
        }
        return true;
    }

    public IMnoStrategy.StrategyResponse handleSendingMessageFailure(ImError imError, int i, int i2, ChatData.ChatType chatType, boolean z, boolean z2) {
        if (ChatData.ChatType.isGroupChatIdBasedGroupChat(chatType)) {
            return new IMnoStrategy.StrategyResponse(IMnoStrategy.StatusCode.NONE);
        }
        if (z) {
            return handleSlmFailure(imError, i);
        }
        IMnoStrategy.StatusCode retryStrategy = getRetryStrategy(i, imError, i2, chatType);
        if (retryStrategy != IMnoStrategy.StatusCode.NO_RETRY) {
            return new IMnoStrategy.StrategyResponse(retryStrategy);
        }
        IMnoStrategy.StrategyResponse handleImFailure = handleImFailure(imError, chatType);
        return (!z2 || handleImFailure.getStatusCode() != IMnoStrategy.StatusCode.FALLBACK_TO_SLM) ? handleImFailure : new IMnoStrategy.StrategyResponse(IMnoStrategy.StatusCode.FALLBACK_TO_LEGACY);
    }

    public IMnoStrategy.StatusCode getRetryStrategy(int i, ImError imError, int i2, ChatData.ChatType chatType) {
        if (ImError.MSRP_SESSION_DOES_NOT_EXIST.equals(imError) && i < 1) {
            String str = TAG;
            IMSLog.i(str, "getRetryStrategy MSRP_SESSION_DOES_NOT_EXIST; currentRetryCount= " + i);
            return IMnoStrategy.StatusCode.RETRY_AFTER_SESSION;
        } else if (!ImError.FORBIDDEN_NO_WARNING_HEADER.equals(imError) || i >= 4) {
            return IMnoStrategy.StatusCode.NO_RETRY;
        } else {
            String str2 = TAG;
            IMSLog.i(str2, "getRetryStrategy FORBIDDEN_NO_WARNING_HEADER; currentRetryCount= " + i);
            return IMnoStrategy.StatusCode.RETRY_AFTER_REGI;
        }
    }

    public void forceRefreshCapability(Set<ImsUri> set, boolean z, ImError imError) {
        ICapabilityDiscoveryModule capDiscModule = getCapDiscModule();
        int i = this.mPhoneId;
        if (capDiscModule == null) {
            IMSLog.i(TAG, i, "forceRefreshCapability: capDiscModule is null");
            return;
        }
        String str = TAG;
        StringBuilder sb = new StringBuilder();
        sb.append("forceRefreshCapability");
        sb.append(IMSLog.isShipBuild() ? "" : set);
        IMSLog.i(str, i, sb.toString());
        ArrayList arrayList = new ArrayList(set);
        if (z) {
            capDiscModule.getCapabilities(arrayList, CapabilityRefreshType.ONLY_IF_NOT_FRESH_IN_MSG_CTX, (long) (Capabilities.FEATURE_FT_HTTP | Capabilities.FEATURE_CHAT_SIMPLE_IM), i);
        } else if (imError != null && this.mForceRefreshRemoteCapa.contains(imError)) {
            capDiscModule.getCapabilities(arrayList, CapabilityRefreshType.ALWAYS_FORCE_REFRESH, (long) (Capabilities.FEATURE_FT_HTTP | Capabilities.FEATURE_CHAT_SIMPLE_IM), i);
        }
    }

    /* access modifiers changed from: protected */
    /* JADX WARNING: Removed duplicated region for block: B:7:0x001b  */
    /* Code decompiled incorrectly, please refer to instructions dump. */
    public boolean checkFtHttpCapability(java.util.Set<com.sec.ims.util.ImsUri> r8) {
        /*
            r7 = this;
            com.sec.internal.interfaces.ims.servicemodules.options.ICapabilityDiscoveryModule r0 = r7.getCapDiscModule()
            r1 = 0
            if (r0 != 0) goto L_0x0011
            java.lang.String r8 = TAG
            int r7 = r7.mPhoneId
            java.lang.String r0 = "checkFtHttpCapability: capDiscModule is null"
            com.sec.internal.log.IMSLog.i(r8, r7, r0)
            return r1
        L_0x0011:
            java.util.Iterator r8 = r8.iterator()
        L_0x0015:
            boolean r2 = r8.hasNext()
            if (r2 == 0) goto L_0x0052
            java.lang.Object r2 = r8.next()
            com.sec.ims.util.ImsUri r2 = (com.sec.ims.util.ImsUri) r2
            com.sec.ims.options.CapabilityRefreshType r3 = com.sec.ims.options.CapabilityRefreshType.ONLY_IF_NOT_FRESH_IN_MSG_CTX
            int r4 = r7.mPhoneId
            com.sec.ims.options.Capabilities r2 = r0.getCapabilities((com.sec.ims.util.ImsUri) r2, (com.sec.ims.options.CapabilityRefreshType) r3, (int) r4)
            java.lang.String r3 = TAG
            int r4 = r7.mPhoneId
            java.lang.StringBuilder r5 = new java.lang.StringBuilder
            r5.<init>()
            java.lang.String r6 = "checkFtHttpCapability, capx: = "
            r5.append(r6)
            r5.append(r2)
            java.lang.String r5 = r5.toString()
            com.sec.internal.log.IMSLog.i(r3, r4, r5)
            if (r2 == 0) goto L_0x0051
            int r3 = com.sec.ims.options.Capabilities.FEATURE_FT_HTTP
            boolean r3 = r2.hasFeature(r3)
            if (r3 == 0) goto L_0x0051
            boolean r2 = r2.isAvailable()
            if (r2 != 0) goto L_0x0015
        L_0x0051:
            return r1
        L_0x0052:
            r7 = 1
            return r7
        */
        throw new UnsupportedOperationException("Method not decompiled: com.sec.internal.ims.servicemodules.im.strategy.VzwUPStrategy.checkFtHttpCapability(java.util.Set):boolean");
    }

    public boolean isCapabilityValidUri(ImsUri imsUri) {
        if (ChatbotUriUtil.hasUriBotPlatform(imsUri, this.mPhoneId)) {
            return true;
        }
        return StrategyUtils.isCapabilityValidUriForUS(imsUri, this.mPhoneId);
    }

    /* JADX WARNING: Removed duplicated region for block: B:13:0x004b  */
    /* JADX WARNING: Removed duplicated region for block: B:35:0x00af  */
    /* Code decompiled incorrectly, please refer to instructions dump. */
    public com.sec.internal.ims.servicemodules.im.strategy.IMnoStrategy.StrategyResponse checkCapability(java.util.Set<com.sec.ims.util.ImsUri> r10, long r11) {
        /*
            r9 = this;
            com.sec.internal.interfaces.ims.servicemodules.options.ICapabilityDiscoveryModule r0 = r9.getCapDiscModule()
            int r1 = r9.mPhoneId
            if (r0 != 0) goto L_0x0017
            java.lang.String r9 = TAG
            java.lang.String r10 = "checkCapability: capDiscModule is null"
            com.sec.internal.log.IMSLog.i(r9, r1, r10)
            com.sec.internal.ims.servicemodules.im.strategy.IMnoStrategy$StrategyResponse r9 = new com.sec.internal.ims.servicemodules.im.strategy.IMnoStrategy$StrategyResponse
            com.sec.internal.ims.servicemodules.im.strategy.IMnoStrategy$StatusCode r10 = com.sec.internal.ims.servicemodules.im.strategy.IMnoStrategy.StatusCode.FALLBACK_TO_LEGACY
            r9.<init>(r10)
            return r9
        L_0x0017:
            com.sec.internal.interfaces.ims.servicemodules.im.IImModule r2 = r9.getImModule()
            if (r2 == 0) goto L_0x0026
            com.sec.internal.ims.servicemodules.im.ImConfig r3 = r2.getImConfig(r1)
            boolean r3 = r3.isImCapAlwaysOn()
            goto L_0x0027
        L_0x0026:
            r3 = 0
        L_0x0027:
            java.lang.String r4 = TAG
            int r5 = r9.mPhoneId
            java.lang.StringBuilder r6 = new java.lang.StringBuilder
            r6.<init>()
            java.lang.String r7 = "checkCapability: isCapAlwaysOn = "
            r6.append(r7)
            r6.append(r3)
            java.lang.String r6 = r6.toString()
            com.sec.internal.log.IMSLog.i(r4, r5, r6)
            if (r3 == 0) goto L_0x00a5
            java.util.Iterator r10 = r10.iterator()
        L_0x0045:
            boolean r3 = r10.hasNext()
            if (r3 == 0) goto L_0x00dd
            java.lang.Object r3 = r10.next()
            com.sec.ims.util.ImsUri r3 = (com.sec.ims.util.ImsUri) r3
            com.sec.ims.options.CapabilityRefreshType r4 = com.sec.ims.options.CapabilityRefreshType.ONLY_IF_NOT_FRESH_IN_MSG_CTX
            com.sec.ims.options.Capabilities r4 = r0.getCapabilities((com.sec.ims.util.ImsUri) r3, (com.sec.ims.options.CapabilityRefreshType) r4, (int) r1)
            if (r4 == 0) goto L_0x0078
            long r5 = r4.getFeature()
            int r7 = com.sec.ims.options.Capabilities.FEATURE_OFFLINE_RCS_USER
            long r7 = (long) r7
            int r5 = (r5 > r7 ? 1 : (r5 == r7 ? 0 : -1))
            if (r5 != 0) goto L_0x0078
            com.sec.internal.ims.servicemodules.im.ImConfig r5 = r2.getImConfig(r1)
            com.sec.internal.constants.ims.servicemodules.im.ImConstants$ImMsgTech r5 = r5.getImMsgTech()
            com.sec.internal.constants.ims.servicemodules.im.ImConstants$ImMsgTech r6 = com.sec.internal.constants.ims.servicemodules.im.ImConstants.ImMsgTech.SIMPLE_IM
            if (r5 != r6) goto L_0x0078
            com.sec.internal.ims.servicemodules.im.strategy.IMnoStrategy$StrategyResponse r9 = new com.sec.internal.ims.servicemodules.im.strategy.IMnoStrategy$StrategyResponse
            com.sec.internal.ims.servicemodules.im.strategy.IMnoStrategy$StatusCode r10 = com.sec.internal.ims.servicemodules.im.strategy.IMnoStrategy.StatusCode.NONE
            r9.<init>(r10)
            return r9
        L_0x0078:
            if (r4 == 0) goto L_0x0086
            boolean r5 = r4.isAvailable()
            if (r5 == 0) goto L_0x0086
            boolean r5 = r9.hasOneOfFeatures(r4, r11)
            if (r5 != 0) goto L_0x0045
        L_0x0086:
            r9.logNoCapability(r3, r4, r11)
            boolean r10 = r9.isNonRcs(r4)
            if (r10 == 0) goto L_0x00a0
            java.lang.String r10 = TAG
            int r9 = r9.mPhoneId
            java.lang.String r11 = "checkCapability: Non-RCS user"
            com.sec.internal.log.IMSLog.i(r10, r9, r11)
            com.sec.internal.ims.servicemodules.im.strategy.IMnoStrategy$StrategyResponse r9 = new com.sec.internal.ims.servicemodules.im.strategy.IMnoStrategy$StrategyResponse
            com.sec.internal.ims.servicemodules.im.strategy.IMnoStrategy$StatusCode r10 = com.sec.internal.ims.servicemodules.im.strategy.IMnoStrategy.StatusCode.FALLBACK_TO_LEGACY
            r9.<init>(r10)
            return r9
        L_0x00a0:
            com.sec.internal.ims.servicemodules.im.strategy.IMnoStrategy$StrategyResponse r9 = r9.getStrategyResponse()
            return r9
        L_0x00a5:
            java.util.Iterator r10 = r10.iterator()
        L_0x00a9:
            boolean r2 = r10.hasNext()
            if (r2 == 0) goto L_0x00dd
            java.lang.Object r2 = r10.next()
            com.sec.ims.util.ImsUri r2 = (com.sec.ims.util.ImsUri) r2
            com.sec.ims.options.CapabilityRefreshType r3 = com.sec.ims.options.CapabilityRefreshType.ONLY_IF_NOT_FRESH_IN_MSG_CTX
            com.sec.ims.options.Capabilities r3 = r0.getCapabilities((com.sec.ims.util.ImsUri) r2, (com.sec.ims.options.CapabilityRefreshType) r3, (int) r1)
            if (r3 == 0) goto L_0x00c9
            boolean r4 = r3.isAvailable()
            if (r4 == 0) goto L_0x00c9
            boolean r4 = r9.hasOneOfFeatures(r3, r11)
            if (r4 != 0) goto L_0x00a9
        L_0x00c9:
            java.lang.String r10 = TAG
            int r0 = r9.mPhoneId
            java.lang.String r1 = "isCapAlwaysOn is off"
            com.sec.internal.log.IMSLog.i(r10, r0, r1)
            r9.logNoCapability(r2, r3, r11)
            com.sec.internal.ims.servicemodules.im.strategy.IMnoStrategy$StrategyResponse r9 = new com.sec.internal.ims.servicemodules.im.strategy.IMnoStrategy$StrategyResponse
            com.sec.internal.ims.servicemodules.im.strategy.IMnoStrategy$StatusCode r10 = com.sec.internal.ims.servicemodules.im.strategy.IMnoStrategy.StatusCode.FALLBACK_TO_LEGACY
            r9.<init>(r10)
            return r9
        L_0x00dd:
            com.sec.internal.ims.servicemodules.im.strategy.IMnoStrategy$StrategyResponse r9 = new com.sec.internal.ims.servicemodules.im.strategy.IMnoStrategy$StrategyResponse
            com.sec.internal.ims.servicemodules.im.strategy.IMnoStrategy$StatusCode r10 = com.sec.internal.ims.servicemodules.im.strategy.IMnoStrategy.StatusCode.NONE
            r9.<init>(r10)
            return r9
        */
        throw new UnsupportedOperationException("Method not decompiled: com.sec.internal.ims.servicemodules.im.strategy.VzwUPStrategy.checkCapability(java.util.Set, long):com.sec.internal.ims.servicemodules.im.strategy.IMnoStrategy$StrategyResponse");
    }

    public boolean isNonRcs(Capabilities capabilities) {
        return capabilities == null || capabilities.getFeature() == ((long) Capabilities.FEATURE_OFFLINE_RCS_USER) || capabilities.getFeature() == ((long) Capabilities.FEATURE_NON_RCS_USER);
    }

    public boolean needCapabilitiesUpdate(CapabilityConstants.CapExResult capExResult, Capabilities capabilities, long j, long j2) {
        if (capExResult != CapabilityConstants.CapExResult.USER_NOT_FOUND && capExResult == CapabilityConstants.CapExResult.FAILURE && capabilities != null && capabilities.isAvailable()) {
            return isCapCacheExpired(capabilities, j2);
        }
        return true;
    }

    public boolean needRefresh(Capabilities capabilities, CapabilityRefreshType capabilityRefreshType, long j, long j2, long j3, long j4) {
        ICapabilityDiscoveryModule iCapabilityDiscoveryModule;
        if (this.mIsLocalConfigUsed && (iCapabilityDiscoveryModule = this.mDiscoveryModule) != null && !iCapabilityDiscoveryModule.hasVideoOwnCapability(this.mPhoneId)) {
            IMSLog.i(TAG, this.mPhoneId, "needRefresh: no videoOwnCapability");
            return false;
        } else if (capabilityRefreshType == CapabilityRefreshType.DISABLED) {
            IMSLog.i(TAG, this.mPhoneId, "needRefresh: availability fetch disabled");
            return false;
        } else if (capabilityRefreshType != CapabilityRefreshType.ALWAYS_FORCE_REFRESH && capabilityRefreshType != CapabilityRefreshType.ONLY_IF_NOT_FRESH && capabilityRefreshType != CapabilityRefreshType.ONLY_IF_NOT_FRESH_IN_MSG_CTX) {
            IMSLog.i(TAG, this.mPhoneId, "needRefresh: cannot process this availability fetch type");
            return false;
        } else if (capabilities == null) {
            IMSLog.i(TAG, this.mPhoneId, "needRefresh: capex is unknown");
            return true;
        } else if (isCapCacheExpired(capabilities, j3)) {
            IMSLog.i(TAG, this.mPhoneId, "needRefresh: capex is reset");
            return true;
        } else if (capabilityRefreshType == CapabilityRefreshType.ALWAYS_FORCE_REFRESH) {
            IMSLog.i(TAG, this.mPhoneId, "needRefresh: availability fetch forced");
            return true;
        } else if (capabilities.hasFeature(Capabilities.FEATURE_NON_RCS_USER)) {
            IMSLog.i(TAG, this.mPhoneId, "needRefresh: capex is nonRcsUser");
            return false;
        } else if (this.mIsLocalConfigUsed || capabilityRefreshType == CapabilityRefreshType.ONLY_IF_NOT_FRESH_IN_MSG_CTX) {
            IMSLog.i(TAG, this.mPhoneId, "needRefresh: check if capex is expired based on capInfoExpiry");
            return capabilities.isExpired(j);
        } else if (capabilities.isExpired(j2)) {
            IMSLog.i(TAG, this.mPhoneId, "needRefresh: capex is expired based on serviceAvailabilityInfoExpiry");
            return true;
        } else {
            IMSLog.i(TAG, this.mPhoneId, "needRefresh: no need refresh");
            return false;
        }
    }

    private boolean isCapCacheExpired(Capabilities capabilities, long j) {
        boolean z = false;
        if (capabilities == null) {
            IMSLog.i(TAG, this.mPhoneId, "isCapCacheExpired: Capability is null");
            return false;
        }
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

    public long isTdelay(long j) {
        boolean z = SemSystemProperties.getBoolean("ro.ril.svlte1x", false);
        if (z || j < 1) {
            String str = TAG;
            int i = this.mPhoneId;
            IMSLog.i(str, i, "SVLTE: " + z + ", delay: " + j);
            return 0;
        }
        int networkType = this.mTelephonyManager.getNetworkType();
        TelephonyManagerExt.NetworkTypeExt networkEnumType = TelephonyManagerExt.getNetworkEnumType(networkType);
        TelephonyManagerExt.NetworkTypeExt networkEnumType2 = TelephonyManagerExt.getNetworkEnumType(this.lastNetworkType);
        String str2 = TAG;
        int i2 = this.mPhoneId;
        IMSLog.i(str2, i2, "SRLTE, current network: " + networkEnumType + ", last network type : " + networkEnumType2);
        this.lastNetworkType = networkType;
        if (networkEnumType2 == TelephonyManagerExt.NetworkTypeExt.NETWORK_TYPE_EHRPD && networkEnumType == TelephonyManagerExt.NetworkTypeExt.NETWORK_TYPE_LTE) {
            return (j - 1) * 1000;
        }
        return 0;
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

    public boolean needUnpublish(int i) {
        TelephonyManagerExt.NetworkTypeExt networkEnumType = TelephonyManagerExt.getNetworkEnumType(this.mTelephonyManager.getNetworkType());
        if (networkEnumType == TelephonyManagerExt.NetworkTypeExt.NETWORK_TYPE_EHRPD) {
            String str = TAG;
            int i2 = this.mPhoneId;
            IMSLog.i(str, i2, "needUnpublish: network type: " + networkEnumType);
            return false;
        } else if (DmConfigHelper.getImsSwitchValue(this.mContext, "volte", i) != 1) {
            IMSLog.i(TAG, this.mPhoneId, "needUnpublish: isVoLteEnabled: off");
            return true;
        } else if (DmConfigHelper.getImsSwitchValue(this.mContext, "mmtel", i) == 1 || DmConfigHelper.getImsSwitchValue(this.mContext, "mmtel-video", i) == 1) {
            return false;
        } else {
            IMSLog.i(TAG, this.mPhoneId, "needUnpublish: mmtel/mmtel-video: off");
            return true;
        }
    }

    public boolean isSubscribeThrottled(PresenceSubscription presenceSubscription, long j, boolean z, boolean z2) {
        if (z2) {
            IMSLog.i(TAG, this.mPhoneId, "refresh type is always force.");
            return false;
        }
        CapabilityConstants.RequestType requestType = presenceSubscription.getRequestType();
        if (!z || !(requestType == CapabilityConstants.RequestType.REQUEST_TYPE_PERIODIC || requestType == CapabilityConstants.RequestType.REQUEST_TYPE_CONTACT_CHANGE)) {
            Date date = new Date();
            long time = date.getTime() - presenceSubscription.getTimestamp().getTime();
            String str = TAG;
            int i = this.mPhoneId;
            IMSLog.i(str, i, "isSubscribeThrottled: interval from " + presenceSubscription.getTimestamp().getTime() + " to " + date.getTime() + ", offset " + time + " sourceThrottlePublish " + j);
            if (time < j) {
                return true;
            }
            return false;
        }
        IMSLog.i(TAG, this.mPhoneId, "isSubscribeThrottled: avail fetch after poll, not throttled");
        return false;
    }

    public void updateOmaDmNodes(int i) {
        boolean z;
        Context context = this.mContext;
        Boolean bool = Boolean.FALSE;
        boolean booleanValue = DmConfigHelper.readBool(context, ConfigConstants.ConfigPath.OMADM_EAB_SETTING, bool, i).booleanValue();
        boolean z2 = true;
        if (this.mIsEABEnabled != booleanValue) {
            this.mIsEABEnabled = booleanValue;
            z = true;
        } else {
            z = false;
        }
        boolean booleanValue2 = DmConfigHelper.readBool(this.mContext, ConfigConstants.ConfigPath.OMADM_VOLTE_ENABLED, bool, i).booleanValue();
        if (this.mIsVLTEnabled != booleanValue2) {
            this.mIsVLTEnabled = booleanValue2;
        } else {
            z2 = z;
        }
        IMSLog.i(TAG, this.mPhoneId, "updateOmaDmNodes: mIsVLTEnabled: " + this.mIsVLTEnabled + " mIsEABEnabled: " + this.mIsEABEnabled + " modified = " + z2);
        if (z2) {
            startServiceBasedOnOmaDmNodes(i);
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

    public boolean checkCapDiscoveryOption() {
        if (TelephonyManagerExt.getNetworkEnumType(this.mTelephonyManager.getNetworkType()) != TelephonyManagerExt.NetworkTypeExt.NETWORK_TYPE_LTE) {
            return true;
        }
        String str = TAG;
        int i = this.mPhoneId;
        IMSLog.i(str, i, "return CapDiscoveryOption: " + this.mIsCapDiscoveryOption);
        return this.mIsCapDiscoveryOption;
    }

    public void updateCapDiscoveryOption() {
        this.mIsCapDiscoveryOption = DmConfigHelper.readBool(this.mContext, ConfigConstants.ConfigPath.OMADM_CAP_DISCOVERY, Boolean.FALSE, this.mPhoneId).booleanValue();
        String str = TAG;
        int i = this.mPhoneId;
        IMSLog.i(str, i, "update CapDiscoveryOption: " + this.mIsCapDiscoveryOption);
    }

    public boolean isLocalConfigUsed() {
        String str = TAG;
        int i = this.mPhoneId;
        IMSLog.i(str, i, "isLocalConfigUsed: " + this.mIsLocalConfigUsed);
        return this.mIsLocalConfigUsed;
    }

    public void updateLocalConfigUsedState(boolean z) {
        String str = TAG;
        int i = this.mPhoneId;
        IMSLog.i(str, i, "updateLocalConfigUsedState: change mIsLocalConfigUsed(" + this.mIsLocalConfigUsed + ") to useLocalConfig(" + z + ")");
        this.mIsLocalConfigUsed = z;
    }

    public boolean isRemoteConfigNeeded(int i) {
        return ConfigUtil.getAutoconfigSourceWithFeature(i, 0) == 0;
    }

    public void changeServiceDescription() {
        IMSLog.i(TAG, this.mPhoneId, "changeServiceDescription: VoLTE Capabilities Discovery");
        ServiceTuple.setServiceDescription((long) Capabilities.FEATURE_PRESENCE_DISCOVERY, "VoLTE Capabilities Discovery");
    }

    public boolean needStopAutoRejoin(ImError imError) {
        return imError.isOneOf(ImError.REMOTE_USER_INVALID, ImError.FORBIDDEN_RETRY_FALLBACK, ImError.FORBIDDEN_SERVICE_NOT_AUTHORISED, ImError.FORBIDDEN_VERSION_NOT_SUPPORTED, ImError.FORBIDDEN_RESTART_GC_CLOSED, ImError.FORBIDDEN_SIZE_EXCEEDED, ImError.FORBIDDEN_ANONYMITY_NOT_ALLOWED, ImError.FORBIDDEN_NO_DESTINATIONS, ImError.FORBIDDEN_NO_WARNING_HEADER);
    }

    public IMnoStrategy.StrategyResponse handleImFailure(ImError imError, ChatData.ChatType chatType) {
        if (imError.isOneOf(ImError.MSRP_TRANSACTION_TIMED_OUT, ImError.MSRP_DO_NOT_SEND_THIS_MESSAGE, ImError.MSRP_UNKNOWN_CONTENT_TYPE, ImError.MSRP_PARAMETERS_OUT_OF_BOUND, ImError.MSRP_SESSION_DOES_NOT_EXIST, ImError.MSRP_SESSION_ON_OTHER_CONNECTION, ImError.MSRP_UNKNOWN_ERROR)) {
            return new IMnoStrategy.StrategyResponse(IMnoStrategy.StatusCode.NONE);
        }
        return new IMnoStrategy.StrategyResponse(isSlmEnabled() ? IMnoStrategy.StatusCode.FALLBACK_TO_SLM : IMnoStrategy.StatusCode.FALLBACK_TO_LEGACY);
    }

    public PresenceResponse.PresenceStatusCode handlePresenceFailure(PresenceResponse.PresenceFailureReason presenceFailureReason, boolean z) {
        if (presenceFailureReason.isOneOf(PresenceResponse.PresenceFailureReason.FORBIDDEN)) {
            return PresenceResponse.PresenceStatusCode.PRESENCE_FORBIDDEN;
        }
        if (presenceFailureReason.isOneOf(PresenceResponse.PresenceFailureReason.USER_NOT_PROVISIONED)) {
            return PresenceResponse.PresenceStatusCode.PRESENCE_AT_NOT_PROVISIONED;
        }
        if (presenceFailureReason.isOneOf(PresenceResponse.PresenceFailureReason.USER_NOT_REGISTERED)) {
            return PresenceResponse.PresenceStatusCode.PRESENCE_AT_NOT_REGISTERED;
        }
        if (presenceFailureReason.isOneOf(PresenceResponse.PresenceFailureReason.USER_NOT_FOUND)) {
            return PresenceResponse.PresenceStatusCode.PRESENCE_NOT_FOUND;
        }
        if (presenceFailureReason.isOneOf(PresenceResponse.PresenceFailureReason.REQUEST_TIMEOUT)) {
            return PresenceResponse.PresenceStatusCode.PRESENCE_REQUEST_TIMEOUT;
        }
        if (presenceFailureReason.isOneOf(PresenceResponse.PresenceFailureReason.REQUEST_TIMEOUT_RETRY, PresenceResponse.PresenceFailureReason.CONDITIONAL_REQUEST_FAILED)) {
            return PresenceResponse.PresenceStatusCode.PRESENCE_REQUIRE_FULL_PUBLISH;
        }
        if (presenceFailureReason.isOneOf(PresenceResponse.PresenceFailureReason.INTERVAL_TOO_SHORT)) {
            return PresenceResponse.PresenceStatusCode.PRESENCE_INTERVAL_TOO_SHORT;
        }
        if (presenceFailureReason.isOneOf(PresenceResponse.PresenceFailureReason.SERVER_INTERNAL_ERROR, PresenceResponse.PresenceFailureReason.SERVICE_UNAVAILABLE, PresenceResponse.PresenceFailureReason.DECLINE)) {
            return PresenceResponse.PresenceStatusCode.PRESENCE_RETRY_EXP_BACKOFF;
        }
        if (presenceFailureReason.isOneOf(PresenceResponse.PresenceFailureReason.NO_RESPONSE)) {
            return PresenceResponse.PresenceStatusCode.PRESENCE_NO_RESPONSE;
        }
        return PresenceResponse.PresenceStatusCode.PRESENCE_DISABLE_MODE;
    }

    public Capabilities getCapabilitiesInitialInfo(int i, ImsUri imsUri, String str, String str2, long j, String str3) {
        Capabilities capabilities = new Capabilities(imsUri, str, str2, j, str3);
        capabilities.resetFeatures();
        capabilities.setPhoneId(i);
        capabilities.setTimestamp(new Date(0));
        return capabilities;
    }
}
