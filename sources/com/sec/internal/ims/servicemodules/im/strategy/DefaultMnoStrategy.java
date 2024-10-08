package com.sec.internal.ims.servicemodules.im.strategy;

import android.content.Context;
import android.net.Network;
import android.net.Uri;
import com.sec.ims.ImsRegistration;
import com.sec.ims.options.Capabilities;
import com.sec.ims.options.CapabilityRefreshType;
import com.sec.ims.util.ImsUri;
import com.sec.internal.constants.ims.servicemodules.im.ChatData;
import com.sec.internal.constants.ims.servicemodules.im.ImConstants;
import com.sec.internal.constants.ims.servicemodules.im.ImDirection;
import com.sec.internal.constants.ims.servicemodules.im.ImError;
import com.sec.internal.constants.ims.servicemodules.im.RoutingType;
import com.sec.internal.constants.ims.servicemodules.im.reason.CancelReason;
import com.sec.internal.constants.ims.servicemodules.im.reason.ImErrorReason;
import com.sec.internal.constants.ims.servicemodules.im.reason.ImSessionClosedReason;
import com.sec.internal.constants.ims.servicemodules.im.reason.ImSessionStopReason;
import com.sec.internal.constants.ims.servicemodules.options.CapabilityConstants;
import com.sec.internal.constants.ims.servicemodules.presence.PresenceResponse;
import com.sec.internal.constants.ims.servicemodules.presence.PresenceSubscription;
import com.sec.internal.helper.HttpRequest;
import com.sec.internal.helper.RetryTimerUtil;
import com.sec.internal.ims.core.sim.SimManagerFactory;
import com.sec.internal.ims.registry.ImsRegistry;
import com.sec.internal.ims.servicemodules.im.ImConfig;
import com.sec.internal.ims.servicemodules.im.MessageBase;
import com.sec.internal.ims.servicemodules.im.data.FtResumableOption;
import com.sec.internal.ims.servicemodules.im.strategy.IMnoStrategy;
import com.sec.internal.ims.settings.RcsPolicySettings;
import com.sec.internal.ims.util.UriGenerator;
import com.sec.internal.interfaces.ims.core.ISimManager;
import com.sec.internal.interfaces.ims.servicemodules.im.IImModule;
import com.sec.internal.interfaces.ims.servicemodules.options.ICapabilityDiscoveryModule;
import com.sec.internal.log.IMSLog;
import java.util.Date;
import java.util.List;
import java.util.Set;

public abstract class DefaultMnoStrategy implements IMnoStrategy {
    protected static final int MAX_RETRY_COUNT_AFTER_REGI = 4;
    private static final String TAG = "DefaultMnoStrategy";
    protected Context mContext;
    protected final int mPhoneId;
    protected RcsPolicySettings mPolicySettings;
    protected RcsPolicySettings.RcsPolicyType mRcsPolicyType = RcsPolicySettings.RcsPolicyType.DEFAULT_RCS;

    public long calSubscribeDelayTime(PresenceSubscription presenceSubscription) {
        return 0;
    }

    public long calThrottledPublishRetryDelayTime(long j, long j2) {
        return 0;
    }

    public void changeServiceDescription() {
    }

    public boolean checkCapDiscoveryOption() {
        return true;
    }

    public abstract IMnoStrategy.StrategyResponse checkCapability(Set<ImsUri> set, long j);

    public abstract IMnoStrategy.StrategyResponse checkCapability(Set<ImsUri> set, long j, ChatData.ChatType chatType, boolean z);

    public boolean checkMainSwitchOff(Context context, int i) {
        return false;
    }

    public String checkNeedParsing(String str) {
        return str;
    }

    public boolean checkSlmFileType(String str) {
        return true;
    }

    public void forceRefreshCapability(Set<ImsUri> set, boolean z, ImError imError) {
    }

    public int getFtHttpRetryInterval(int i, int i2) {
        return i;
    }

    public int getFtHttpSessionRetryTimer(int i, ImError imError) {
        return -1;
    }

    public int getNextFileTransferAutoResumeTimer(ImDirection imDirection, int i) {
        return -1;
    }

    public abstract long getThrottledDelay(long j);

    public abstract IMnoStrategy.StrategyResponse handleAttachFileFailure(ImSessionClosedReason imSessionClosedReason);

    public abstract IMnoStrategy.StrategyResponse handleFtFailure(ImError imError, ChatData.ChatType chatType);

    public abstract IMnoStrategy.StrategyResponse handleFtMsrpInterruption(ImError imError);

    public abstract IMnoStrategy.StrategyResponse handleImFailure(ImError imError, ChatData.ChatType chatType);

    public abstract PresenceResponse.PresenceStatusCode handlePresenceFailure(PresenceResponse.PresenceFailureReason presenceFailureReason, boolean z);

    public abstract IMnoStrategy.StrategyResponse handleSendingMessageFailure(ImError imError, int i, int i2, ChatData.ChatType chatType, boolean z, boolean z2);

    public abstract IMnoStrategy.StrategyResponse handleSlmFailure(ImError imError);

    public boolean isBMode(boolean z) {
        return false;
    }

    public boolean isCapabilityValidUri(ImsUri imsUri) {
        return true;
    }

    public abstract boolean isCloseSessionNeeded(ImError imError);

    public abstract boolean isCustomizedFeature(long j);

    public abstract boolean isDeleteSessionSupported(ChatData.ChatType chatType, int i);

    public boolean isDisplayBotError() {
        return false;
    }

    public boolean isDisplayWarnText() {
        return false;
    }

    public abstract boolean isFTHTTPAutoResumeAndCancelPerConnectionChange();

    public abstract boolean isFirstMsgInvite(boolean z);

    public boolean isFtHttpOnlySupported(boolean z) {
        return false;
    }

    public boolean isHTTPUsedForEmptyFtHttpPOST() {
        return false;
    }

    public boolean isLocalConfigUsed() {
        return false;
    }

    public abstract boolean isNeedToReportToRegiGvn(ImError imError);

    public boolean isPresenceReadyToRequest(boolean z, boolean z2) {
        return true;
    }

    public boolean isRemoteConfigNeeded(int i) {
        return false;
    }

    public abstract boolean isResendFTResume(boolean z);

    public boolean isRevocationAvailableMessage(MessageBase messageBase) {
        return true;
    }

    public long isTdelay(long j) {
        return 0;
    }

    public abstract boolean needCapabilitiesUpdate(CapabilityConstants.CapExResult capExResult, Capabilities capabilities, long j, long j2);

    public abstract boolean needRefresh(Capabilities capabilities, CapabilityRefreshType capabilityRefreshType, long j, long j2, long j3, long j4);

    public abstract boolean needStopAutoRejoin(ImError imError);

    public boolean needToCapabilityCheckForImdn(boolean z) {
        return false;
    }

    public boolean needUnpublish(int i) {
        return false;
    }

    public boolean needUnpublish(ImsRegistration imsRegistration, ImsRegistration imsRegistration2) {
        return false;
    }

    public void startServiceBasedOnOmaDmNodes(int i) {
    }

    public abstract long updateAvailableFeatures(Capabilities capabilities, long j, CapabilityConstants.CapExResult capExResult);

    public void updateCapDiscoveryOption() {
    }

    public abstract long updateFeatures(Capabilities capabilities, long j, CapabilityConstants.CapExResult capExResult);

    public void updateLocalConfigUsedState(boolean z) {
    }

    public void updateOmaDmNodes(int i) {
    }

    public DefaultMnoStrategy(Context context, int i) {
        this.mContext = context;
        this.mPhoneId = i;
        this.mPolicySettings = new RcsPolicySettings(context, i);
    }

    public RcsPolicySettings.RcsPolicyType getPolicyType() {
        return this.mRcsPolicyType;
    }

    public void setPolicyType(RcsPolicySettings.RcsPolicyType rcsPolicyType) {
        this.mRcsPolicyType = rcsPolicyType;
    }

    public boolean boolSetting(String str) {
        return this.mPolicySettings.readBool(str);
    }

    public int intSetting(String str) {
        return this.mPolicySettings.readInt(str);
    }

    public String stringSetting(String str) {
        return this.mPolicySettings.readString(str);
    }

    public List<String> stringArraySetting(String str) {
        return this.mPolicySettings.readStringArray(str);
    }

    /* access modifiers changed from: protected */
    public ICapabilityDiscoveryModule getCapDiscModule() {
        if (ImsRegistry.isReady()) {
            return ImsRegistry.getServiceModuleManager().getCapabilityDiscoveryModule();
        }
        IMSLog.i(TAG, this.mPhoneId, "getCapDiscModule: getInstance is null");
        return null;
    }

    /* access modifiers changed from: protected */
    public IImModule getImModule() {
        if (ImsRegistry.isReady()) {
            return ImsRegistry.getServiceModuleManager().getImModule();
        }
        IMSLog.i(TAG, this.mPhoneId, "getImModule: getInstance is null");
        return null;
    }

    /* access modifiers changed from: package-private */
    public boolean hasOneOfFeatures(Capabilities capabilities, long j) {
        boolean z = false;
        if (capabilities != null) {
            if ((capabilities.getFeature() & j) > 0) {
                z = true;
            }
            if (!z) {
                String str = TAG;
                int i = this.mPhoneId;
                StringBuilder sb = new StringBuilder();
                sb.append("hasOneOfFeatures:");
                sb.append(capabilities.getUri() == null ? "" : capabilities.getUri().toStringLimit());
                sb.append(" getFeature()=");
                sb.append(capabilities.getFeature());
                sb.append(", features=");
                sb.append(j);
                sb.append(", ret=false");
                IMSLog.i(str, i, sb.toString());
            }
        }
        return z;
    }

    /* access modifiers changed from: package-private */
    public boolean hasOneOfFeaturesAvailable(Capabilities capabilities, long j) {
        boolean z = false;
        if (capabilities != null) {
            if ((capabilities.getAvailableFeatures() & j) > 0 || j == ((long) Capabilities.FEATURE_OFFLINE_RCS_USER)) {
                z = true;
            }
            if (!z) {
                String str = TAG;
                int i = this.mPhoneId;
                StringBuilder sb = new StringBuilder();
                sb.append("hasOneOfFeaturesAvailable:");
                sb.append(capabilities.getUri() == null ? "" : capabilities.getUri().toStringLimit());
                sb.append(" getFeature()=");
                sb.append(capabilities.getFeature());
                sb.append(", features=");
                sb.append(j);
                sb.append(", ret=false");
                IMSLog.i(str, i, sb.toString());
            }
        }
        return z;
    }

    /* access modifiers changed from: protected */
    public boolean isSlmEnabled() {
        IImModule imModule = getImModule();
        return imModule != null && imModule.isServiceRegistered(this.mPhoneId, "slm") && imModule.getImConfig(this.mPhoneId).getSlmAuth() == ImConstants.SlmAuth.ENABLED;
    }

    /* access modifiers changed from: protected */
    public IMnoStrategy.StrategyResponse getStrategyResponse() {
        return new IMnoStrategy.StrategyResponse(isSlmEnabled() ? IMnoStrategy.StatusCode.FALLBACK_TO_SLM : IMnoStrategy.StatusCode.FALLBACK_TO_LEGACY);
    }

    public IMnoStrategy.StrategyResponse handleSendingMessageFailure(ImError imError, int i, int i2, ChatData.ChatType chatType, boolean z, boolean z2, boolean z3) {
        IMnoStrategy.StrategyResponse handleSendingMessageFailure = handleSendingMessageFailure(imError, i, i2, chatType, z, z3);
        if (z2) {
            return (handleSendingMessageFailure.getStatusCode() == IMnoStrategy.StatusCode.FALLBACK_TO_SLM || handleSendingMessageFailure.getStatusCode() == IMnoStrategy.StatusCode.FALLBACK_TO_LEGACY || imError == ImError.GONE || imError == ImError.REQUEST_PENDING) ? new IMnoStrategy.StrategyResponse(IMnoStrategy.StatusCode.NO_RETRY) : handleSendingMessageFailure;
        }
        return handleSendingMessageFailure;
    }

    public IMnoStrategy.StrategyResponse handleSendingFtMsrpMessageFailure(ImError imError, int i, int i2, ChatData.ChatType chatType, boolean z) {
        return new IMnoStrategy.StrategyResponse(IMnoStrategy.StatusCode.NONE);
    }

    public IMnoStrategy.StatusCode getRetryStrategy(int i, ImError imError, int i2, ChatData.ChatType chatType) {
        if (!ImError.FORBIDDEN_NO_WARNING_HEADER.equals(imError) || i >= 4) {
            return IMnoStrategy.StatusCode.NO_RETRY;
        }
        String str = TAG;
        IMSLog.i(str, "getRetryStrategy FORBIDDEN_NO_WARNING_HEADER; currentRetryCount= " + i);
        return IMnoStrategy.StatusCode.RETRY_AFTER_REGI;
    }

    public IMnoStrategy.StatusCode getFtMsrpRetryStrategy(int i, ImError imError, int i2) {
        return IMnoStrategy.StatusCode.NO_RETRY;
    }

    public FtResumableOption getftResumableOption(CancelReason cancelReason, boolean z, ImDirection imDirection, int i) {
        if (cancelReason == CancelReason.CANCELED_BY_USER || cancelReason == CancelReason.LOW_MEMORY) {
            return FtResumableOption.MANUALLY_RESUMABLE_ONLY;
        }
        return FtResumableOption.NOTRESUMABLE;
    }

    public boolean isSubscribeThrottled(PresenceSubscription presenceSubscription, long j, boolean z, boolean z2) {
        if (presenceSubscription.getState() == 5) {
            IMSLog.i(TAG, this.mPhoneId, "isSubscribeThrottled: retried subscription");
            return false;
        }
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

    public boolean needRefresh(Capabilities capabilities, CapabilityRefreshType capabilityRefreshType, long j, long j2) {
        if (capabilityRefreshType == CapabilityRefreshType.DISABLED) {
            return false;
        }
        if (capabilities == null) {
            IMSLog.i(TAG, this.mPhoneId, "needRefresh: Capability is null");
            return true;
        } else if (capabilities.hasFeature(Capabilities.FEATURE_NOT_UPDATED)) {
            IMSLog.i(TAG, this.mPhoneId, "needRefresh: fetch failed capabilities");
            return true;
        } else if (capabilityRefreshType == CapabilityRefreshType.FORCE_REFRESH_UCE || capabilityRefreshType == CapabilityRefreshType.ALWAYS_FORCE_REFRESH || capabilityRefreshType == CapabilityRefreshType.FORCE_REFRESH_SYNC) {
            return true;
        } else {
            if (capabilityRefreshType == CapabilityRefreshType.ONLY_IF_NOT_FRESH && capabilities.isExpired(j)) {
                return true;
            }
            if (capabilityRefreshType != CapabilityRefreshType.ONLY_IF_NOT_FRESH_IN_MSG_CTX || !capabilities.isExpired(j)) {
                return false;
            }
            return true;
        }
    }

    public boolean needPoll(Capabilities capabilities, long j) {
        return needRefresh(capabilities, CapabilityRefreshType.ONLY_IF_NOT_FRESH, j, 0);
    }

    public ImDirection convertToImDirection(String str) {
        return ImDirection.INCOMING;
    }

    public boolean isFTViaHttp(ImConfig imConfig, Set<ImsUri> set, ChatData.ChatType chatType) {
        return imConfig.getFtDefaultMech() == ImConstants.FtMech.HTTP && isFtHttpRegistered() && (ChatData.ChatType.isGroupChatIdBasedGroupChat(chatType) || checkFtHttpCapability(set));
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
            if (r2 == 0) goto L_0x0061
            java.lang.Object r2 = r8.next()
            com.sec.ims.util.ImsUri r2 = (com.sec.ims.util.ImsUri) r2
            com.sec.ims.options.CapabilityRefreshType r3 = com.sec.ims.options.CapabilityRefreshType.ONLY_IF_NOT_FRESH
            int r4 = r7.mPhoneId
            com.sec.ims.options.Capabilities r2 = r0.getCapabilities((com.sec.ims.util.ImsUri) r2, (com.sec.ims.options.CapabilityRefreshType) r3, (int) r4)
            java.lang.String r3 = TAG
            int r4 = r7.mPhoneId
            java.lang.StringBuilder r5 = new java.lang.StringBuilder
            r5.<init>()
            java.lang.String r6 = "checkFtHttpCapability, capx: = "
            r5.append(r6)
            if (r2 == 0) goto L_0x003e
            java.lang.String r6 = r2.toString()
            goto L_0x003f
        L_0x003e:
            r6 = 0
        L_0x003f:
            r5.append(r6)
            java.lang.String r5 = r5.toString()
            com.sec.internal.log.IMSLog.i(r3, r4, r5)
            if (r2 == 0) goto L_0x0059
            int r4 = com.sec.ims.options.Capabilities.FEATURE_FT_HTTP
            boolean r4 = r2.hasFeature(r4)
            if (r4 == 0) goto L_0x0059
            boolean r2 = r2.isAvailable()
            if (r2 != 0) goto L_0x0015
        L_0x0059:
            int r7 = r7.mPhoneId
            java.lang.String r8 = "No FT HTTP capability"
            com.sec.internal.log.IMSLog.i(r3, r7, r8)
            return r1
        L_0x0061:
            r7 = 1
            return r7
        */
        throw new UnsupportedOperationException("Method not decompiled: com.sec.internal.ims.servicemodules.im.strategy.DefaultMnoStrategy.checkFtHttpCapability(java.util.Set):boolean");
    }

    /* access modifiers changed from: protected */
    public boolean isFtHttpRegistered() {
        IImModule imModule = getImModule();
        return imModule != null && imModule.isServiceRegistered(this.mPhoneId, "ft_http");
    }

    public RoutingType getMsgRoutingType(ImsUri imsUri, ImsUri imsUri2, ImsUri imsUri3, ImsUri imsUri4, boolean z) {
        return RoutingType.NONE;
    }

    public IMnoStrategy.StrategyResponse handleFtHttpRequestFailure(CancelReason cancelReason, ImDirection imDirection, boolean z) {
        return new IMnoStrategy.StrategyResponse(IMnoStrategy.StatusCode.NONE);
    }

    public IMnoStrategy.HttpStrategyResponse handleFtHttpDownloadError(HttpRequest httpRequest) {
        CancelReason cancelReason = CancelReason.ERROR;
        int code = httpRequest.code();
        int i = 3;
        if (code == 401) {
            handleFtHttpRequestFailure(CancelReason.UNAUTHORIZED, ImDirection.INCOMING, false);
        } else if (code == 403) {
            handleFtHttpRequestFailure(CancelReason.FORBIDDEN_FT_HTTP, ImDirection.INCOMING, false);
        } else if (code == 503) {
            i = RetryTimerUtil.getRetryAfter(httpRequest.header(HttpRequest.HEADER_RETRY_AFTER));
        }
        return new IMnoStrategy.HttpStrategyResponse(cancelReason, i);
    }

    public String getFtHttpUserAgent(ImConfig imConfig) {
        return imConfig.getUserAgent();
    }

    public Uri getFtHttpCsUri(ImConfig imConfig, Set<ImsUri> set, boolean z, boolean z2) {
        return imConfig.getFtHttpCsUri();
    }

    /* access modifiers changed from: protected */
    public boolean checkUserAvailableOffline(Set<ImsUri> set) {
        ICapabilityDiscoveryModule capDiscModule = getCapDiscModule();
        if (capDiscModule == null) {
            IMSLog.i(TAG, this.mPhoneId, "checkUserAvailableOffline: capDiscModule is null");
            return false;
        }
        for (ImsUri capabilities : set) {
            Capabilities capabilities2 = capDiscModule.getCapabilities(capabilities, CapabilityRefreshType.ONLY_IF_NOT_FRESH, this.mPhoneId);
            String str = TAG;
            int i = this.mPhoneId;
            StringBuilder sb = new StringBuilder();
            sb.append("checkUserAvailableOffline, capx: = ");
            sb.append(capabilities2 != null ? capabilities2.toString() : null);
            IMSLog.i(str, i, sb.toString());
            boolean z = capabilities2 != null;
            boolean z2 = z && capabilities2.hasFeature(Capabilities.FEATURE_NON_RCS_USER);
            boolean z3 = z && capabilities2.hasFeature(Capabilities.FEATURE_NOT_UPDATED);
            if (z && !capabilities2.isAvailable() && !z2 && !z3) {
                IMSLog.i(str, this.mPhoneId, "USER_AVAILABLE_OFFLINE..!!");
                return true;
            }
        }
        return false;
    }

    public Set<ImsUri> getNetworkPreferredUri(UriGenerator uriGenerator, Set<ImsUri> set) {
        return uriGenerator.getNetworkPreferredUri(UriGenerator.URIServiceType.RCS_URI, set);
    }

    public String getErrorReasonForStrategyResponse(MessageBase messageBase, IMnoStrategy.StrategyResponse strategyResponse) {
        if (strategyResponse == null) {
            return null;
        }
        if (strategyResponse.getStatusCode() == IMnoStrategy.StatusCode.FALLBACK_TO_LEGACY || strategyResponse.getStatusCode() == IMnoStrategy.StatusCode.FALLBACK_TO_LEGACY_CFS) {
            return ImErrorReason.FRAMEWORK_ERROR_FALLBACKFAILED.toString();
        }
        return null;
    }

    public ImSessionStopReason getSessionStopReason(boolean z) {
        return z ? ImSessionStopReason.INVOLUNTARILY : ImSessionStopReason.CLOSE_1_TO_1_SESSION;
    }

    public final boolean checkImsiBasedRegi(ImsRegistration imsRegistration) {
        ISimManager simManagerFromSimSlot;
        if (!boolSetting(RcsPolicySettings.RcsPolicy.CHECK_IMSIBASED_REGI) || (simManagerFromSimSlot = SimManagerFactory.getSimManagerFromSimSlot(this.mPhoneId)) == null) {
            return false;
        }
        String imsi = simManagerFromSimSlot.getImsi();
        ImsUri registeredImpu = imsRegistration.getRegisteredImpu();
        String str = TAG;
        IMSLog.s(str, "checkImsiBasedRegi: impu " + registeredImpu);
        if (!(registeredImpu == null || imsi == null || registeredImpu.getUser() == null)) {
            return registeredImpu.getUser().contains(imsi);
        }
        return false;
    }

    public final boolean isWarnSizeFile(Network network, long j, long j2, boolean z) {
        if (j2 == 0 || j <= j2) {
            return false;
        }
        return !boolSetting(RcsPolicySettings.RcsPolicy.IGNORE_WIFI_WARNSIZE) || !z;
    }

    public ImConstants.ChatbotMessagingTech checkChatbotMessagingTech(ImConfig imConfig, boolean z, Set<ImsUri> set) {
        return ImConstants.ChatbotMessagingTech.NONE;
    }

    public Capabilities getCapabilitiesInitialInfo(int i, ImsUri imsUri, String str, String str2, long j, String str3) {
        Capabilities capabilities = new Capabilities(imsUri, str, str2, j, str3);
        capabilities.resetFeatures();
        capabilities.setPhoneId(i);
        return capabilities;
    }

    public IMnoStrategy.StrategyResponse getUploadedFileFallbackSLMTech() {
        return getStrategyResponse();
    }

    /* renamed from: com.sec.internal.ims.servicemodules.im.strategy.DefaultMnoStrategy$1  reason: invalid class name */
    static /* synthetic */ class AnonymousClass1 {
        static final /* synthetic */ int[] $SwitchMap$com$sec$internal$constants$ims$servicemodules$im$ImError;

        /* JADX WARNING: Can't wrap try/catch for region: R(12:0|1|2|3|4|5|6|7|8|9|10|12) */
        /* JADX WARNING: Code restructure failed: missing block: B:13:?, code lost:
            return;
         */
        /* JADX WARNING: Failed to process nested try/catch */
        /* JADX WARNING: Missing exception handler attribute for start block: B:3:0x0012 */
        /* JADX WARNING: Missing exception handler attribute for start block: B:5:0x001d */
        /* JADX WARNING: Missing exception handler attribute for start block: B:7:0x0028 */
        /* JADX WARNING: Missing exception handler attribute for start block: B:9:0x0033 */
        static {
            /*
                com.sec.internal.constants.ims.servicemodules.im.ImError[] r0 = com.sec.internal.constants.ims.servicemodules.im.ImError.values()
                int r0 = r0.length
                int[] r0 = new int[r0]
                $SwitchMap$com$sec$internal$constants$ims$servicemodules$im$ImError = r0
                com.sec.internal.constants.ims.servicemodules.im.ImError r1 = com.sec.internal.constants.ims.servicemodules.im.ImError.REMOTE_USER_INVALID     // Catch:{ NoSuchFieldError -> 0x0012 }
                int r1 = r1.ordinal()     // Catch:{ NoSuchFieldError -> 0x0012 }
                r2 = 1
                r0[r1] = r2     // Catch:{ NoSuchFieldError -> 0x0012 }
            L_0x0012:
                int[] r0 = $SwitchMap$com$sec$internal$constants$ims$servicemodules$im$ImError     // Catch:{ NoSuchFieldError -> 0x001d }
                com.sec.internal.constants.ims.servicemodules.im.ImError r1 = com.sec.internal.constants.ims.servicemodules.im.ImError.SESSION_DOESNT_EXIST     // Catch:{ NoSuchFieldError -> 0x001d }
                int r1 = r1.ordinal()     // Catch:{ NoSuchFieldError -> 0x001d }
                r2 = 2
                r0[r1] = r2     // Catch:{ NoSuchFieldError -> 0x001d }
            L_0x001d:
                int[] r0 = $SwitchMap$com$sec$internal$constants$ims$servicemodules$im$ImError     // Catch:{ NoSuchFieldError -> 0x0028 }
                com.sec.internal.constants.ims.servicemodules.im.ImError r1 = com.sec.internal.constants.ims.servicemodules.im.ImError.TRANSACTION_DOESNT_EXIST     // Catch:{ NoSuchFieldError -> 0x0028 }
                int r1 = r1.ordinal()     // Catch:{ NoSuchFieldError -> 0x0028 }
                r2 = 3
                r0[r1] = r2     // Catch:{ NoSuchFieldError -> 0x0028 }
            L_0x0028:
                int[] r0 = $SwitchMap$com$sec$internal$constants$ims$servicemodules$im$ImError     // Catch:{ NoSuchFieldError -> 0x0033 }
                com.sec.internal.constants.ims.servicemodules.im.ImError r1 = com.sec.internal.constants.ims.servicemodules.im.ImError.TRANSACTION_DOESNT_EXIST_RETRY_FALLBACK     // Catch:{ NoSuchFieldError -> 0x0033 }
                int r1 = r1.ordinal()     // Catch:{ NoSuchFieldError -> 0x0033 }
                r2 = 4
                r0[r1] = r2     // Catch:{ NoSuchFieldError -> 0x0033 }
            L_0x0033:
                int[] r0 = $SwitchMap$com$sec$internal$constants$ims$servicemodules$im$ImError     // Catch:{ NoSuchFieldError -> 0x003e }
                com.sec.internal.constants.ims.servicemodules.im.ImError r1 = com.sec.internal.constants.ims.servicemodules.im.ImError.GONE     // Catch:{ NoSuchFieldError -> 0x003e }
                int r1 = r1.ordinal()     // Catch:{ NoSuchFieldError -> 0x003e }
                r2 = 5
                r0[r1] = r2     // Catch:{ NoSuchFieldError -> 0x003e }
            L_0x003e:
                return
            */
            throw new UnsupportedOperationException("Method not decompiled: com.sec.internal.ims.servicemodules.im.strategy.DefaultMnoStrategy.AnonymousClass1.<clinit>():void");
        }
    }

    public boolean shouldRestartSession(ImError imError) {
        int i = AnonymousClass1.$SwitchMap$com$sec$internal$constants$ims$servicemodules$im$ImError[imError.ordinal()];
        if (i == 1 || i == 2 || i == 3 || i == 4) {
            return true;
        }
        if (i == 5 && !boolSetting(RcsPolicySettings.RcsPolicy.GONE_SHOULD_ENDSESSION)) {
            return true;
        }
        return false;
    }

    public boolean loadRcsSettings(boolean z) {
        return this.mPolicySettings.load(z);
    }

    public ImSessionClosedReason handleSessionFailure(ImError imError, boolean z) {
        return ImSessionClosedReason.NONE;
    }
}
