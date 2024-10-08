package com.sec.internal.ims.servicemodules.im.strategy;

import android.content.Context;
import com.sec.ims.options.Capabilities;
import com.sec.ims.options.CapabilityRefreshType;
import com.sec.ims.util.ImsUri;
import com.sec.internal.constants.ims.servicemodules.im.ChatData;
import com.sec.internal.constants.ims.servicemodules.im.ImDirection;
import com.sec.internal.constants.ims.servicemodules.im.ImError;
import com.sec.internal.constants.ims.servicemodules.im.reason.CancelReason;
import com.sec.internal.constants.ims.servicemodules.im.reason.ImSessionClosedReason;
import com.sec.internal.constants.ims.servicemodules.options.CapabilityConstants;
import com.sec.internal.constants.ims.servicemodules.presence.PresenceResponse;
import com.sec.internal.helper.HttpRequest;
import com.sec.internal.helper.RetryTimerUtil;
import com.sec.internal.ims.servicemodules.im.data.FtResumableOption;
import com.sec.internal.ims.servicemodules.im.strategy.IMnoStrategy;
import com.sec.internal.imscr.LogClass;
import com.sec.internal.log.IMSLog;
import java.util.Set;

public class DefaultUPMnoStrategy extends DefaultMnoStrategy {
    private static final String TAG = "DefaultUPMnoStrategy";
    protected final int MAX_RETRY_COUNT = 1;

    private boolean hasFeature(long j, long j2) {
        return (j & j2) == j2;
    }

    public long getThrottledDelay(long j) {
        return j;
    }

    public boolean isCloseSessionNeeded(ImError imError) {
        return false;
    }

    public final boolean isDeleteSessionSupported(ChatData.ChatType chatType, int i) {
        return true;
    }

    public boolean isDisplayWarnText() {
        return true;
    }

    public final boolean isFTHTTPAutoResumeAndCancelPerConnectionChange() {
        return true;
    }

    public final boolean isFirstMsgInvite(boolean z) {
        return z;
    }

    public boolean isFtHttpOnlySupported(boolean z) {
        return true;
    }

    public final boolean isResendFTResume(boolean z) {
        return false;
    }

    public boolean needStopAutoRejoin(ImError imError) {
        return false;
    }

    public long updateAvailableFeatures(Capabilities capabilities, long j, CapabilityConstants.CapExResult capExResult) {
        return j;
    }

    public DefaultUPMnoStrategy(Context context, int i) {
        super(context, i);
    }

    public IMnoStrategy.StrategyResponse handleSendingMessageFailure(ImError imError, int i, int i2, ChatData.ChatType chatType, boolean z, boolean z2) {
        if (ChatData.ChatType.isGroupChatIdBasedGroupChat(chatType)) {
            return new IMnoStrategy.StrategyResponse(IMnoStrategy.StatusCode.NONE);
        }
        if (z) {
            return handleSlmFailure(imError, i);
        }
        return handleImFailure(imError, chatType);
    }

    /* renamed from: com.sec.internal.ims.servicemodules.im.strategy.DefaultUPMnoStrategy$1  reason: invalid class name */
    static /* synthetic */ class AnonymousClass1 {
        static final /* synthetic */ int[] $SwitchMap$com$sec$internal$constants$ims$servicemodules$im$ImError;

        /* JADX WARNING: Can't wrap try/catch for region: R(8:0|1|2|3|4|5|6|(3:7|8|10)) */
        /* JADX WARNING: Failed to process nested try/catch */
        /* JADX WARNING: Missing exception handler attribute for start block: B:3:0x0012 */
        /* JADX WARNING: Missing exception handler attribute for start block: B:5:0x001d */
        /* JADX WARNING: Missing exception handler attribute for start block: B:7:0x0028 */
        static {
            /*
                com.sec.internal.constants.ims.servicemodules.im.ImError[] r0 = com.sec.internal.constants.ims.servicemodules.im.ImError.values()
                int r0 = r0.length
                int[] r0 = new int[r0]
                $SwitchMap$com$sec$internal$constants$ims$servicemodules$im$ImError = r0
                com.sec.internal.constants.ims.servicemodules.im.ImError r1 = com.sec.internal.constants.ims.servicemodules.im.ImError.ALTERNATE_SERVICE     // Catch:{ NoSuchFieldError -> 0x0012 }
                int r1 = r1.ordinal()     // Catch:{ NoSuchFieldError -> 0x0012 }
                r2 = 1
                r0[r1] = r2     // Catch:{ NoSuchFieldError -> 0x0012 }
            L_0x0012:
                int[] r0 = $SwitchMap$com$sec$internal$constants$ims$servicemodules$im$ImError     // Catch:{ NoSuchFieldError -> 0x001d }
                com.sec.internal.constants.ims.servicemodules.im.ImError r1 = com.sec.internal.constants.ims.servicemodules.im.ImError.SESSION_TIMED_OUT     // Catch:{ NoSuchFieldError -> 0x001d }
                int r1 = r1.ordinal()     // Catch:{ NoSuchFieldError -> 0x001d }
                r2 = 2
                r0[r1] = r2     // Catch:{ NoSuchFieldError -> 0x001d }
            L_0x001d:
                int[] r0 = $SwitchMap$com$sec$internal$constants$ims$servicemodules$im$ImError     // Catch:{ NoSuchFieldError -> 0x0028 }
                com.sec.internal.constants.ims.servicemodules.im.ImError r1 = com.sec.internal.constants.ims.servicemodules.im.ImError.BUSY_HERE     // Catch:{ NoSuchFieldError -> 0x0028 }
                int r1 = r1.ordinal()     // Catch:{ NoSuchFieldError -> 0x0028 }
                r2 = 3
                r0[r1] = r2     // Catch:{ NoSuchFieldError -> 0x0028 }
            L_0x0028:
                int[] r0 = $SwitchMap$com$sec$internal$constants$ims$servicemodules$im$ImError     // Catch:{ NoSuchFieldError -> 0x0033 }
                com.sec.internal.constants.ims.servicemodules.im.ImError r1 = com.sec.internal.constants.ims.servicemodules.im.ImError.CONNECTION_RELEASED     // Catch:{ NoSuchFieldError -> 0x0033 }
                int r1 = r1.ordinal()     // Catch:{ NoSuchFieldError -> 0x0033 }
                r2 = 4
                r0[r1] = r2     // Catch:{ NoSuchFieldError -> 0x0033 }
            L_0x0033:
                return
            */
            throw new UnsupportedOperationException("Method not decompiled: com.sec.internal.ims.servicemodules.im.strategy.DefaultUPMnoStrategy.AnonymousClass1.<clinit>():void");
        }
    }

    /* access modifiers changed from: protected */
    public IMnoStrategy.StrategyResponse handleSlmFailure(ImError imError, int i) {
        IMnoStrategy.StatusCode statusCode = IMnoStrategy.StatusCode.FALLBACK_TO_LEGACY;
        int i2 = AnonymousClass1.$SwitchMap$com$sec$internal$constants$ims$servicemodules$im$ImError[imError.ordinal()];
        if (!(i2 == 1 || i2 == 2 || i2 == 3 || i2 == 4 || i >= 1)) {
            statusCode = IMnoStrategy.StatusCode.RETRY_IMMEDIATE;
        }
        return new IMnoStrategy.StrategyResponse(statusCode);
    }

    public FtResumableOption getftResumableOption(CancelReason cancelReason, boolean z, ImDirection imDirection, int i) {
        if (cancelReason == CancelReason.CANCELED_BY_USER || cancelReason == CancelReason.DEVICE_UNREGISTERED || cancelReason == CancelReason.LOW_MEMORY || cancelReason == CancelReason.ERROR || cancelReason == CancelReason.WIFI_DISCONNECTED) {
            return FtResumableOption.MANUALLY_RESUMABLE_ONLY;
        }
        return FtResumableOption.NOTRESUMABLE;
    }

    public IMnoStrategy.StrategyResponse checkCapability(Set<ImsUri> set, long j, ChatData.ChatType chatType, boolean z) {
        if (chatType == ChatData.ChatType.REGULAR_GROUP_CHAT) {
            return new IMnoStrategy.StrategyResponse(IMnoStrategy.StatusCode.NONE);
        }
        return checkCapability(set, j);
    }

    /* access modifiers changed from: protected */
    public void logNoCapability(ImsUri imsUri, Capabilities capabilities, long j) {
        String str;
        String str2 = TAG;
        int i = this.mPhoneId;
        StringBuilder sb = new StringBuilder();
        sb.append("checkCapability: No capabilities for ");
        sb.append((!IMSLog.isShipBuild() || imsUri == null) ? imsUri : imsUri.toStringLimit());
        if (capabilities == null) {
            str = "";
        } else {
            str = ": isAvailable=" + capabilities.isAvailable();
        }
        sb.append(str);
        IMSLog.i(str2, i, sb.toString());
        StringBuilder sb2 = new StringBuilder();
        sb2.append(this.mPhoneId);
        sb2.append(",");
        sb2.append(j);
        sb2.append(",NOCAP,");
        sb2.append(imsUri != null ? imsUri.toStringLimit() : "xx");
        IMSLog.c(LogClass.STRATEGY_CHECKCAPA, sb2.toString());
    }

    /* JADX WARNING: Removed duplicated region for block: B:13:0x0048  */
    /* JADX WARNING: Removed duplicated region for block: B:29:0x0089  */
    /* Code decompiled incorrectly, please refer to instructions dump. */
    public com.sec.internal.ims.servicemodules.im.strategy.IMnoStrategy.StrategyResponse checkCapability(java.util.Set<com.sec.ims.util.ImsUri> r9, long r10) {
        /*
            r8 = this;
            com.sec.internal.interfaces.ims.servicemodules.options.ICapabilityDiscoveryModule r0 = r8.getCapDiscModule()
            int r1 = r8.mPhoneId
            if (r0 != 0) goto L_0x0014
            java.lang.String r9 = TAG
            java.lang.String r10 = "checkCapability: capDiscModule is null"
            com.sec.internal.log.IMSLog.i(r9, r1, r10)
            com.sec.internal.ims.servicemodules.im.strategy.IMnoStrategy$StrategyResponse r8 = r8.getStrategyResponse()
            return r8
        L_0x0014:
            com.sec.internal.interfaces.ims.servicemodules.im.IImModule r2 = r8.getImModule()
            if (r2 == 0) goto L_0x0023
            com.sec.internal.ims.servicemodules.im.ImConfig r2 = r2.getImConfig(r1)
            boolean r2 = r2.isImCapAlwaysOn()
            goto L_0x0024
        L_0x0023:
            r2 = 0
        L_0x0024:
            java.lang.String r3 = TAG
            int r4 = r8.mPhoneId
            java.lang.StringBuilder r5 = new java.lang.StringBuilder
            r5.<init>()
            java.lang.String r6 = "checkCapability: isCapAlwaysOn = "
            r5.append(r6)
            r5.append(r2)
            java.lang.String r5 = r5.toString()
            com.sec.internal.log.IMSLog.i(r3, r4, r5)
            if (r2 == 0) goto L_0x007f
            java.util.Iterator r9 = r9.iterator()
        L_0x0042:
            boolean r2 = r9.hasNext()
            if (r2 == 0) goto L_0x00ab
            java.lang.Object r2 = r9.next()
            com.sec.ims.util.ImsUri r2 = (com.sec.ims.util.ImsUri) r2
            com.sec.ims.options.CapabilityRefreshType r3 = com.sec.ims.options.CapabilityRefreshType.ONLY_IF_NOT_FRESH
            com.sec.ims.options.Capabilities r3 = r0.getCapabilities((com.sec.ims.util.ImsUri) r2, (com.sec.ims.options.CapabilityRefreshType) r3, (int) r1)
            if (r3 == 0) goto L_0x0069
            long r4 = r3.getFeature()
            int r6 = com.sec.ims.options.Capabilities.FEATURE_OFFLINE_RCS_USER
            long r6 = (long) r6
            int r4 = (r4 > r6 ? 1 : (r4 == r6 ? 0 : -1))
            if (r4 != 0) goto L_0x0069
            com.sec.internal.ims.servicemodules.im.strategy.IMnoStrategy$StrategyResponse r8 = new com.sec.internal.ims.servicemodules.im.strategy.IMnoStrategy$StrategyResponse
            com.sec.internal.ims.servicemodules.im.strategy.IMnoStrategy$StatusCode r9 = com.sec.internal.ims.servicemodules.im.strategy.IMnoStrategy.StatusCode.NONE
            r8.<init>(r9)
            return r8
        L_0x0069:
            if (r3 == 0) goto L_0x0077
            boolean r4 = r3.isAvailable()
            if (r4 == 0) goto L_0x0077
            boolean r4 = r8.hasOneOfFeatures(r3, r10)
            if (r4 != 0) goto L_0x0042
        L_0x0077:
            r8.logNoCapability(r2, r3, r10)
            com.sec.internal.ims.servicemodules.im.strategy.IMnoStrategy$StrategyResponse r8 = r8.getStrategyResponse()
            return r8
        L_0x007f:
            java.util.Iterator r9 = r9.iterator()
        L_0x0083:
            boolean r2 = r9.hasNext()
            if (r2 == 0) goto L_0x00ab
            java.lang.Object r2 = r9.next()
            com.sec.ims.util.ImsUri r2 = (com.sec.ims.util.ImsUri) r2
            com.sec.ims.options.CapabilityRefreshType r3 = com.sec.ims.options.CapabilityRefreshType.ONLY_IF_NOT_FRESH
            com.sec.ims.options.Capabilities r3 = r0.getCapabilities((com.sec.ims.util.ImsUri) r2, (com.sec.ims.options.CapabilityRefreshType) r3, (int) r1)
            if (r3 == 0) goto L_0x00a3
            boolean r4 = r3.isAvailable()
            if (r4 == 0) goto L_0x00a3
            boolean r4 = r8.hasOneOfFeaturesAvailable(r3, r10)
            if (r4 != 0) goto L_0x0083
        L_0x00a3:
            r8.logNoCapability(r2, r3, r10)
            com.sec.internal.ims.servicemodules.im.strategy.IMnoStrategy$StrategyResponse r8 = r8.getStrategyResponse()
            return r8
        L_0x00ab:
            com.sec.internal.ims.servicemodules.im.strategy.IMnoStrategy$StrategyResponse r8 = new com.sec.internal.ims.servicemodules.im.strategy.IMnoStrategy$StrategyResponse
            com.sec.internal.ims.servicemodules.im.strategy.IMnoStrategy$StatusCode r9 = com.sec.internal.ims.servicemodules.im.strategy.IMnoStrategy.StatusCode.NONE
            r8.<init>(r9)
            return r8
        */
        throw new UnsupportedOperationException("Method not decompiled: com.sec.internal.ims.servicemodules.im.strategy.DefaultUPMnoStrategy.checkCapability(java.util.Set, long):com.sec.internal.ims.servicemodules.im.strategy.IMnoStrategy$StrategyResponse");
    }

    public boolean needCapabilitiesUpdate(CapabilityConstants.CapExResult capExResult, Capabilities capabilities, long j, long j2) {
        if (capabilities == null || capExResult == CapabilityConstants.CapExResult.USER_NOT_FOUND) {
            IMSLog.i(TAG, this.mPhoneId, "needCapabilitiesUpdate: Capability is null");
            return true;
        } else if (capExResult == CapabilityConstants.CapExResult.USER_UNAVAILABLE && !capabilities.hasFeature(Capabilities.FEATURE_NOT_UPDATED)) {
            IMSLog.i(TAG, this.mPhoneId, "needCapabilitiesUpdate: User is offline");
            return false;
        } else if (capExResult == CapabilityConstants.CapExResult.UNCLASSIFIED_ERROR || capExResult == CapabilityConstants.CapExResult.FORBIDDEN_403) {
            IMSLog.i(TAG, this.mPhoneId, "needCapabilitiesUpdate: do not change anything");
            return false;
        } else if (capExResult != CapabilityConstants.CapExResult.FAILURE) {
            return true;
        } else {
            return false;
        }
    }

    public long updateFeatures(Capabilities capabilities, long j, CapabilityConstants.CapExResult capExResult) {
        if (capabilities == null || capabilities.hasFeature(Capabilities.FEATURE_NOT_UPDATED) || capabilities.hasFeature(Capabilities.FEATURE_NON_RCS_USER) || j == ((long) Capabilities.FEATURE_NON_RCS_USER)) {
            String str = TAG;
            int i = this.mPhoneId;
            IMSLog.i(str, i, "updateFeatures: set features " + Capabilities.dumpFeature(j));
            return j;
        } else if (j == ((long) Capabilities.FEATURE_NOT_UPDATED)) {
            String str2 = TAG;
            int i2 = this.mPhoneId;
            IMSLog.i(str2, i2, "updateFeatures: feature is NOT_UPDATED, remains previous features " + Capabilities.dumpFeature(capabilities.getFeature()));
            return capabilities.getFeature();
        } else if (!capabilities.hasFeature(Capabilities.FEATURE_CHATBOT_ROLE) || hasFeature(j, Capabilities.FEATURE_CHATBOT_ROLE) || (!hasFeature(j, Capabilities.FEATURE_CHATBOT_CHAT_SESSION) && !hasFeature(j, Capabilities.FEATURE_CHATBOT_STANDALONE_MSG))) {
            String str3 = TAG;
            int i3 = this.mPhoneId;
            IMSLog.i(str3, i3, "updateFeatures: updated features " + Capabilities.dumpFeature(capabilities.getFeature() | j));
            return capabilities.getFeature() | j;
        } else {
            String str4 = TAG;
            int i4 = this.mPhoneId;
            IMSLog.i(str4, i4, "updateFeatures: remove chatbot role feature " + Capabilities.dumpFeature(j));
            return j;
        }
    }

    public boolean isCustomizedFeature(long j) {
        return ((long) Capabilities.FEATURE_GEO_VIA_SMS) == j || ((long) Capabilities.FEATURE_FT_VIA_SMS) == j;
    }

    public boolean needRefresh(Capabilities capabilities, CapabilityRefreshType capabilityRefreshType, long j, long j2, long j3, long j4) {
        if (capabilityRefreshType == CapabilityRefreshType.DISABLED) {
            return false;
        }
        if (capabilities == null) {
            IMSLog.i(TAG, this.mPhoneId, "needRefresh: Capability is null");
            return true;
        } else if (capabilities.hasFeature(Capabilities.FEATURE_NOT_UPDATED)) {
            IMSLog.i(TAG, this.mPhoneId, "needRefresh: fetch failed capabilities");
            return true;
        } else if ((capabilityRefreshType == CapabilityRefreshType.FORCE_REFRESH_UCE && capabilities.isExpired(j2)) || capabilityRefreshType == CapabilityRefreshType.ALWAYS_FORCE_REFRESH) {
            return true;
        } else {
            if (capabilityRefreshType == CapabilityRefreshType.ONLY_IF_NOT_FRESH && capabilities.isExpired(j)) {
                return true;
            }
            if (capabilityRefreshType != CapabilityRefreshType.ONLY_IF_NOT_FRESH_IN_MSG_CTX) {
                return false;
            }
            if (capabilities.getLegacyLatching()) {
                j = j2;
            }
            if (capabilities.isExpired(j)) {
                return true;
            }
            return false;
        }
    }

    public final boolean isNeedToReportToRegiGvn(ImError imError) {
        return imError.isOneOf(ImError.FORBIDDEN_NO_WARNING_HEADER);
    }

    public final IMnoStrategy.StrategyResponse handleFtMsrpInterruption(ImError imError) {
        return new IMnoStrategy.StrategyResponse(IMnoStrategy.StatusCode.DISPLAY_ERROR);
    }

    public final IMnoStrategy.StrategyResponse handleSlmFailure(ImError imError) {
        if (imError == ImError.REMOTE_PARTY_DECLINED) {
            return new IMnoStrategy.StrategyResponse(IMnoStrategy.StatusCode.NONE);
        }
        return new IMnoStrategy.StrategyResponse(IMnoStrategy.StatusCode.FALLBACK_TO_LEGACY);
    }

    public final IMnoStrategy.StrategyResponse handleAttachFileFailure(ImSessionClosedReason imSessionClosedReason) {
        return new IMnoStrategy.StrategyResponse(IMnoStrategy.StatusCode.FALLBACK_TO_LEGACY);
    }

    public IMnoStrategy.StrategyResponse handleImFailure(ImError imError, ChatData.ChatType chatType) {
        if (imError.isOneOf(ImError.MSRP_TRANSACTION_TIMED_OUT, ImError.MSRP_DO_NOT_SEND_THIS_MESSAGE, ImError.MSRP_UNKNOWN_CONTENT_TYPE, ImError.MSRP_PARAMETERS_OUT_OF_BOUND, ImError.MSRP_SESSION_DOES_NOT_EXIST, ImError.MSRP_SESSION_ON_OTHER_CONNECTION, ImError.MSRP_UNKNOWN_ERROR)) {
            return new IMnoStrategy.StrategyResponse(IMnoStrategy.StatusCode.NONE);
        }
        return new IMnoStrategy.StrategyResponse(isSlmEnabled() ? IMnoStrategy.StatusCode.FALLBACK_TO_SLM : IMnoStrategy.StatusCode.FALLBACK_TO_LEGACY);
    }

    public IMnoStrategy.StrategyResponse handleFtFailure(ImError imError, ChatData.ChatType chatType) {
        return new IMnoStrategy.StrategyResponse(IMnoStrategy.StatusCode.NONE);
    }

    public IMnoStrategy.HttpStrategyResponse handleFtHttpDownloadError(HttpRequest httpRequest) {
        CancelReason cancelReason = CancelReason.ERROR;
        int code = httpRequest.code();
        int i = 3;
        if (code == 403) {
            handleFtHttpRequestFailure(CancelReason.FORBIDDEN_FT_HTTP, ImDirection.INCOMING, false);
        } else if (code == 404 || code == 410) {
            cancelReason = CancelReason.VALIDITY_EXPIRED;
            i = -1;
        } else if (code == 503) {
            i = RetryTimerUtil.getRetryAfter(httpRequest.header(HttpRequest.HEADER_RETRY_AFTER));
        }
        return new IMnoStrategy.HttpStrategyResponse(cancelReason, i);
    }

    public PresenceResponse.PresenceStatusCode handlePresenceFailure(PresenceResponse.PresenceFailureReason presenceFailureReason, boolean z) {
        if (presenceFailureReason.isOneOf(PresenceResponse.PresenceFailureReason.USER_NOT_FOUND, PresenceResponse.PresenceFailureReason.DOES_NOT_EXIST_ANYWHERE, PresenceResponse.PresenceFailureReason.METHOD_NOT_ALLOWED, PresenceResponse.PresenceFailureReason.USER_NOT_REGISTERED, PresenceResponse.PresenceFailureReason.USER_NOT_PROVISIONED)) {
            return PresenceResponse.PresenceStatusCode.PRESENCE_NO_SUBSCRIBE;
        }
        if (presenceFailureReason.isOneOf(PresenceResponse.PresenceFailureReason.INTERVAL_TOO_SHORT, PresenceResponse.PresenceFailureReason.INVALID_REQUEST, PresenceResponse.PresenceFailureReason.REQUEST_TIMEOUT_RETRY, PresenceResponse.PresenceFailureReason.CONDITIONAL_REQUEST_FAILED)) {
            return PresenceResponse.PresenceStatusCode.PRESENCE_REQUIRE_FULL_PUBLISH;
        }
        return PresenceResponse.PresenceStatusCode.PRESENCE_REQUIRE_RETRY_PUBLISH;
    }
}
