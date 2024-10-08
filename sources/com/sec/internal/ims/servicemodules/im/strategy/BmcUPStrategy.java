package com.sec.internal.ims.servicemodules.im.strategy;

import android.content.Context;
import com.sec.ims.options.Capabilities;
import com.sec.ims.options.CapabilityRefreshType;
import com.sec.ims.util.ImsUri;
import com.sec.internal.constants.ims.servicemodules.im.ChatData;
import com.sec.internal.constants.ims.servicemodules.im.ImError;
import com.sec.internal.constants.ims.servicemodules.options.CapabilityConstants;
import com.sec.internal.constants.ims.servicemodules.presence.PresenceResponse;
import com.sec.internal.constants.ims.servicemodules.presence.PresenceSubscription;
import com.sec.internal.ims.servicemodules.im.strategy.IMnoStrategy;
import com.sec.internal.ims.util.ChatbotUriUtil;
import com.sec.internal.interfaces.ims.servicemodules.options.ICapabilityDiscoveryModule;
import com.sec.internal.log.IMSLog;
import java.util.Collection;
import java.util.Date;
import java.util.Set;

public final class BmcUPStrategy extends DefaultRCSMnoStrategy {
    private static final String TAG = "BmcUPStrategy";
    protected final int MAX_RETRY_COUNT = 1;

    public long getThrottledDelay(long j) {
        return 3;
    }

    public boolean isCustomizedFeature(long j) {
        return false;
    }

    public boolean isFirstMsgInvite(boolean z) {
        return false;
    }

    public boolean isFtHttpOnlySupported(boolean z) {
        return true;
    }

    public boolean isPresenceReadyToRequest(boolean z, boolean z2) {
        return z && !z2;
    }

    public BmcUPStrategy(Context context, int i) {
        super(context, i);
    }

    public void forceRefreshCapability(Set<ImsUri> set, boolean z, ImError imError) {
        ICapabilityDiscoveryModule capDiscModule = getCapDiscModule();
        if (capDiscModule == null) {
            IMSLog.i(TAG, this.mPhoneId, "forceRefreshCapability: capDiscModule is null");
            return;
        }
        String str = TAG;
        int i = this.mPhoneId;
        IMSLog.i(str, i, "forceRefreshCapability: uris " + IMSLog.numberChecker((Collection<ImsUri>) set));
        if (z) {
            for (ImsUri msisdn : set) {
                capDiscModule.getCapabilities(msisdn.getMsisdn(), (long) (Capabilities.FEATURE_FT_HTTP | Capabilities.FEATURE_CHAT_CPM), this.mPhoneId);
            }
        }
    }

    public boolean isCapabilityValidUri(ImsUri imsUri) {
        if (ChatbotUriUtil.hasUriBotPlatform(imsUri, this.mPhoneId)) {
            return true;
        }
        return StrategyUtils.isCapabilityValidUriForUS(imsUri, this.mPhoneId);
    }

    public boolean needCapabilitiesUpdate(CapabilityConstants.CapExResult capExResult, Capabilities capabilities, long j, long j2) {
        if (capabilities == null) {
            IMSLog.i(TAG, this.mPhoneId, "needCapabilitiesUpdate: Capability is null");
            return true;
        } else if (capExResult == CapabilityConstants.CapExResult.USER_NOT_FOUND) {
            IMSLog.i(TAG, this.mPhoneId, "needCapabilitiesUpdate: Capability USER_NOT_FOUND");
            return true;
        } else if (capExResult == CapabilityConstants.CapExResult.UNCLASSIFIED_ERROR) {
            IMSLog.i(TAG, this.mPhoneId, "needCapabilitiesUpdate: UNCLASSIFIED_ERROR. do not change anything");
            return false;
        } else if (capabilities.getFeature() == ((long) Capabilities.FEATURE_NOT_UPDATED)) {
            IMSLog.i(TAG, this.mPhoneId, "needCapabilitiesUpdate: Capability is not_updated");
            return true;
        } else if (capExResult != CapabilityConstants.CapExResult.FAILURE) {
            return true;
        } else {
            return false;
        }
    }

    public boolean needRefresh(Capabilities capabilities, CapabilityRefreshType capabilityRefreshType, long j, long j2, long j3, long j4) {
        return needRefresh(capabilities, capabilityRefreshType, j, j3);
    }

    public boolean isSubscribeThrottled(PresenceSubscription presenceSubscription, long j, boolean z, boolean z2) {
        if (presenceSubscription.getState() == 5) {
            IMSLog.i(TAG, this.mPhoneId, "isSubscribeThrottled: retried subscription");
            return false;
        } else if (z2) {
            IMSLog.i(TAG, this.mPhoneId, "isSubscribeThrottled: isAlwaysForce true");
            return false;
        } else {
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

    /* renamed from: com.sec.internal.ims.servicemodules.im.strategy.BmcUPStrategy$1  reason: invalid class name */
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
            throw new UnsupportedOperationException("Method not decompiled: com.sec.internal.ims.servicemodules.im.strategy.BmcUPStrategy.AnonymousClass1.<clinit>():void");
        }
    }

    private IMnoStrategy.StrategyResponse handleSlmFailure(ImError imError, int i) {
        IMnoStrategy.StatusCode statusCode = IMnoStrategy.StatusCode.FALLBACK_TO_LEGACY;
        int i2 = AnonymousClass1.$SwitchMap$com$sec$internal$constants$ims$servicemodules$im$ImError[imError.ordinal()];
        if (!(i2 == 1 || i2 == 2 || i2 == 3 || i2 == 4 || i >= 1)) {
            statusCode = IMnoStrategy.StatusCode.RETRY_IMMEDIATE;
        }
        return new IMnoStrategy.StrategyResponse(statusCode);
    }

    public IMnoStrategy.StrategyResponse checkCapability(Set<ImsUri> set, long j, ChatData.ChatType chatType, boolean z) {
        if (chatType == ChatData.ChatType.REGULAR_GROUP_CHAT) {
            return new IMnoStrategy.StrategyResponse(IMnoStrategy.StatusCode.NONE);
        }
        return checkCapability(set, j);
    }

    /* JADX WARNING: Removed duplicated region for block: B:7:0x001e  */
    /* Code decompiled incorrectly, please refer to instructions dump. */
    public com.sec.internal.ims.servicemodules.im.strategy.IMnoStrategy.StrategyResponse checkCapability(java.util.Set<com.sec.ims.util.ImsUri> r7, long r8) {
        /*
            r6 = this;
            com.sec.internal.interfaces.ims.servicemodules.options.ICapabilityDiscoveryModule r0 = r6.getCapDiscModule()
            if (r0 != 0) goto L_0x0014
            java.lang.String r7 = TAG
            int r8 = r6.mPhoneId
            java.lang.String r9 = "checkCapability: capDiscModule is null"
            com.sec.internal.log.IMSLog.i(r7, r8, r9)
            com.sec.internal.ims.servicemodules.im.strategy.IMnoStrategy$StrategyResponse r6 = r6.getStrategyResponse()
            return r6
        L_0x0014:
            java.util.Iterator r7 = r7.iterator()
        L_0x0018:
            boolean r1 = r7.hasNext()
            if (r1 == 0) goto L_0x00af
            java.lang.Object r1 = r7.next()
            com.sec.ims.util.ImsUri r1 = (com.sec.ims.util.ImsUri) r1
            com.sec.ims.options.CapabilityRefreshType r2 = com.sec.ims.options.CapabilityRefreshType.ONLY_IF_NOT_FRESH
            int r3 = r6.mPhoneId
            com.sec.ims.options.Capabilities r2 = r0.getCapabilities((com.sec.ims.util.ImsUri) r1, (com.sec.ims.options.CapabilityRefreshType) r2, (int) r3)
            if (r2 == 0) goto L_0x003a
            boolean r3 = r2.isAvailable()
            if (r3 == 0) goto L_0x003a
            boolean r3 = r6.hasOneOfFeaturesAvailable(r2, r8)
            if (r3 != 0) goto L_0x0018
        L_0x003a:
            java.lang.String r7 = TAG
            int r0 = r6.mPhoneId
            java.lang.StringBuilder r3 = new java.lang.StringBuilder
            r3.<init>()
            java.lang.String r4 = "checkCapability: No capabilities for "
            r3.append(r4)
            boolean r4 = com.sec.internal.log.IMSLog.isShipBuild()
            if (r4 == 0) goto L_0x0055
            if (r1 == 0) goto L_0x0055
            java.lang.String r4 = r1.toStringLimit()
            goto L_0x0056
        L_0x0055:
            r4 = r1
        L_0x0056:
            r3.append(r4)
            if (r2 != 0) goto L_0x005e
            java.lang.String r2 = ""
            goto L_0x0073
        L_0x005e:
            java.lang.StringBuilder r4 = new java.lang.StringBuilder
            r4.<init>()
            java.lang.String r5 = ": isAvailable="
            r4.append(r5)
            boolean r2 = r2.isAvailable()
            r4.append(r2)
            java.lang.String r2 = r4.toString()
        L_0x0073:
            r3.append(r2)
            java.lang.String r2 = r3.toString()
            com.sec.internal.log.IMSLog.i(r7, r0, r2)
            java.lang.StringBuilder r7 = new java.lang.StringBuilder
            r7.<init>()
            int r0 = r6.mPhoneId
            r7.append(r0)
            java.lang.String r0 = ","
            r7.append(r0)
            r7.append(r8)
            java.lang.String r8 = ",NOCAP,"
            r7.append(r8)
            if (r1 == 0) goto L_0x009b
            java.lang.String r8 = r1.toStringLimit()
            goto L_0x009e
        L_0x009b:
            java.lang.String r8 = "xx"
        L_0x009e:
            r7.append(r8)
            java.lang.String r7 = r7.toString()
            r8 = 302710784(0x120b0000, float:4.3860666E-28)
            com.sec.internal.log.IMSLog.c(r8, r7)
            com.sec.internal.ims.servicemodules.im.strategy.IMnoStrategy$StrategyResponse r6 = r6.getStrategyResponse()
            return r6
        L_0x00af:
            com.sec.internal.ims.servicemodules.im.strategy.IMnoStrategy$StrategyResponse r6 = new com.sec.internal.ims.servicemodules.im.strategy.IMnoStrategy$StrategyResponse
            com.sec.internal.ims.servicemodules.im.strategy.IMnoStrategy$StatusCode r7 = com.sec.internal.ims.servicemodules.im.strategy.IMnoStrategy.StatusCode.NONE
            r6.<init>(r7)
            return r6
        */
        throw new UnsupportedOperationException("Method not decompiled: com.sec.internal.ims.servicemodules.im.strategy.BmcUPStrategy.checkCapability(java.util.Set, long):com.sec.internal.ims.servicemodules.im.strategy.IMnoStrategy$StrategyResponse");
    }

    public boolean needStopAutoRejoin(ImError imError) {
        return imError.isOneOf(ImError.REMOTE_USER_INVALID, ImError.FORBIDDEN_RETRY_FALLBACK, ImError.FORBIDDEN_SERVICE_NOT_AUTHORISED, ImError.FORBIDDEN_VERSION_NOT_SUPPORTED, ImError.FORBIDDEN_RESTART_GC_CLOSED, ImError.FORBIDDEN_SIZE_EXCEEDED, ImError.FORBIDDEN_ANONYMITY_NOT_ALLOWED, ImError.FORBIDDEN_NO_DESTINATIONS, ImError.FORBIDDEN_NO_WARNING_HEADER);
    }

    public IMnoStrategy.StrategyResponse handleImFailure(ImError imError, ChatData.ChatType chatType) {
        if (imError.isOneOf(ImError.MSRP_DO_NOT_SEND_THIS_MESSAGE, ImError.MSRP_UNKNOWN_CONTENT_TYPE, ImError.MSRP_PARAMETERS_OUT_OF_BOUND, ImError.MSRP_SESSION_DOES_NOT_EXIST, ImError.MSRP_SESSION_ON_OTHER_CONNECTION, ImError.MSRP_UNKNOWN_ERROR)) {
            return new IMnoStrategy.StrategyResponse(IMnoStrategy.StatusCode.NONE);
        }
        if (imError.isOneOf(ImError.MSRP_TRANSACTION_TIMED_OUT)) {
            return new IMnoStrategy.StrategyResponse(IMnoStrategy.StatusCode.FALLBACK_TO_LEGACY);
        }
        if (imError.isOneOf(ImError.MSRP_ACTION_NOT_ALLOWED, ImError.REMOTE_PARTY_DECLINED)) {
            return new IMnoStrategy.StrategyResponse(IMnoStrategy.StatusCode.DISPLAY_ERROR);
        }
        return new IMnoStrategy.StrategyResponse(isSlmEnabled() ? IMnoStrategy.StatusCode.FALLBACK_TO_SLM : IMnoStrategy.StatusCode.FALLBACK_TO_LEGACY);
    }

    public PresenceResponse.PresenceStatusCode handlePresenceFailure(PresenceResponse.PresenceFailureReason presenceFailureReason, boolean z) {
        if (presenceFailureReason.isOneOf(PresenceResponse.PresenceFailureReason.USER_NOT_FOUND, PresenceResponse.PresenceFailureReason.METHOD_NOT_ALLOWED, PresenceResponse.PresenceFailureReason.USER_NOT_REGISTERED, PresenceResponse.PresenceFailureReason.USER_NOT_PROVISIONED, PresenceResponse.PresenceFailureReason.FORBIDDEN)) {
            return PresenceResponse.PresenceStatusCode.PRESENCE_NO_SUBSCRIBE;
        }
        if (presenceFailureReason.isOneOf(PresenceResponse.PresenceFailureReason.INTERVAL_TOO_SHORT, PresenceResponse.PresenceFailureReason.INVALID_REQUEST, PresenceResponse.PresenceFailureReason.REQUEST_TIMEOUT_RETRY, PresenceResponse.PresenceFailureReason.CONDITIONAL_REQUEST_FAILED)) {
            return PresenceResponse.PresenceStatusCode.PRESENCE_REQUIRE_FULL_PUBLISH;
        }
        return PresenceResponse.PresenceStatusCode.PRESENCE_REQUIRE_RETRY_PUBLISH;
    }
}
