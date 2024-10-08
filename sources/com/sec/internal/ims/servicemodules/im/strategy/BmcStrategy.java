package com.sec.internal.ims.servicemodules.im.strategy;

import android.content.Context;
import com.sec.ims.options.Capabilities;
import com.sec.ims.options.CapabilityRefreshType;
import com.sec.ims.util.ImsUri;
import com.sec.internal.constants.ims.servicemodules.im.ImError;
import com.sec.internal.constants.ims.servicemodules.options.CapabilityConstants;
import com.sec.internal.constants.ims.servicemodules.presence.PresenceResponse;
import com.sec.internal.interfaces.ims.servicemodules.options.ICapabilityDiscoveryModule;
import com.sec.internal.log.IMSLog;
import java.util.Set;

public final class BmcStrategy extends DefaultRCSMnoStrategy {
    private static final String TAG = "BmcStrategy";

    public long getThrottledDelay(long j) {
        return 3;
    }

    public boolean isPresenceReadyToRequest(boolean z, boolean z2) {
        return z && !z2;
    }

    public BmcStrategy(Context context, int i) {
        super(context, i);
    }

    /* JADX WARNING: Removed duplicated region for block: B:7:0x001e  */
    /* Code decompiled incorrectly, please refer to instructions dump. */
    public com.sec.internal.ims.servicemodules.im.strategy.IMnoStrategy.StrategyResponse checkCapability(java.util.Set<com.sec.ims.util.ImsUri> r7, long r8) {
        /*
            r6 = this;
            com.sec.internal.interfaces.ims.servicemodules.options.ICapabilityDiscoveryModule r0 = r6.getCapDiscModule()
            int r1 = r6.mPhoneId
            if (r0 != 0) goto L_0x0014
            java.lang.String r7 = TAG
            java.lang.String r8 = "checkCapability: capDiscModule is null"
            com.sec.internal.log.IMSLog.i(r7, r1, r8)
            com.sec.internal.ims.servicemodules.im.strategy.IMnoStrategy$StrategyResponse r6 = r6.getStrategyResponse()
            return r6
        L_0x0014:
            java.util.Iterator r7 = r7.iterator()
        L_0x0018:
            boolean r2 = r7.hasNext()
            if (r2 == 0) goto L_0x00ad
            java.lang.Object r2 = r7.next()
            com.sec.ims.util.ImsUri r2 = (com.sec.ims.util.ImsUri) r2
            com.sec.ims.options.CapabilityRefreshType r3 = com.sec.ims.options.CapabilityRefreshType.ONLY_IF_NOT_FRESH
            com.sec.ims.options.Capabilities r3 = r0.getCapabilities((com.sec.ims.util.ImsUri) r2, (com.sec.ims.options.CapabilityRefreshType) r3, (int) r1)
            if (r3 == 0) goto L_0x0038
            boolean r4 = r3.isAvailable()
            if (r4 == 0) goto L_0x0038
            boolean r4 = r6.hasOneOfFeaturesAvailable(r3, r8)
            if (r4 != 0) goto L_0x0018
        L_0x0038:
            java.lang.String r7 = TAG
            int r0 = r6.mPhoneId
            java.lang.StringBuilder r1 = new java.lang.StringBuilder
            r1.<init>()
            java.lang.String r4 = "checkCapability: No capabilities for "
            r1.append(r4)
            boolean r4 = com.sec.internal.log.IMSLog.isShipBuild()
            if (r4 == 0) goto L_0x0053
            if (r2 == 0) goto L_0x0053
            java.lang.String r4 = r2.toStringLimit()
            goto L_0x0054
        L_0x0053:
            r4 = r2
        L_0x0054:
            r1.append(r4)
            if (r3 != 0) goto L_0x005c
            java.lang.String r3 = ""
            goto L_0x0071
        L_0x005c:
            java.lang.StringBuilder r4 = new java.lang.StringBuilder
            r4.<init>()
            java.lang.String r5 = ": isAvailable="
            r4.append(r5)
            boolean r3 = r3.isAvailable()
            r4.append(r3)
            java.lang.String r3 = r4.toString()
        L_0x0071:
            r1.append(r3)
            java.lang.String r1 = r1.toString()
            com.sec.internal.log.IMSLog.i(r7, r0, r1)
            java.lang.StringBuilder r7 = new java.lang.StringBuilder
            r7.<init>()
            int r0 = r6.mPhoneId
            r7.append(r0)
            java.lang.String r0 = ","
            r7.append(r0)
            r7.append(r8)
            java.lang.String r8 = ",NOCAP,"
            r7.append(r8)
            if (r2 == 0) goto L_0x0099
            java.lang.String r8 = r2.toStringLimit()
            goto L_0x009c
        L_0x0099:
            java.lang.String r8 = "xx"
        L_0x009c:
            r7.append(r8)
            java.lang.String r7 = r7.toString()
            r8 = 302710784(0x120b0000, float:4.3860666E-28)
            com.sec.internal.log.IMSLog.c(r8, r7)
            com.sec.internal.ims.servicemodules.im.strategy.IMnoStrategy$StrategyResponse r6 = r6.getStrategyResponse()
            return r6
        L_0x00ad:
            com.sec.internal.ims.servicemodules.im.strategy.IMnoStrategy$StrategyResponse r6 = new com.sec.internal.ims.servicemodules.im.strategy.IMnoStrategy$StrategyResponse
            com.sec.internal.ims.servicemodules.im.strategy.IMnoStrategy$StatusCode r7 = com.sec.internal.ims.servicemodules.im.strategy.IMnoStrategy.StatusCode.NONE
            r6.<init>(r7)
            return r6
        */
        throw new UnsupportedOperationException("Method not decompiled: com.sec.internal.ims.servicemodules.im.strategy.BmcStrategy.checkCapability(java.util.Set, long):com.sec.internal.ims.servicemodules.im.strategy.IMnoStrategy$StrategyResponse");
    }

    public boolean isCapabilityValidUri(ImsUri imsUri) {
        return StrategyUtils.isCapabilityValidUriForUS(imsUri, this.mPhoneId);
    }

    public void forceRefreshCapability(Set<ImsUri> set, boolean z, ImError imError) {
        ICapabilityDiscoveryModule capDiscModule = getCapDiscModule();
        int i = this.mPhoneId;
        if (capDiscModule == null) {
            IMSLog.i(TAG, i, "forceRefreshCapability: capDiscModule is null");
            return;
        }
        String str = TAG;
        IMSLog.s(str, "forceRefreshCapability: uris " + set);
        if (z) {
            for (ImsUri msisdn : set) {
                capDiscModule.getCapabilities(msisdn.getMsisdn(), (long) (Capabilities.FEATURE_FT_HTTP | Capabilities.FEATURE_CHAT_CPM), i);
            }
        }
    }

    public boolean needRefresh(Capabilities capabilities, CapabilityRefreshType capabilityRefreshType, long j, long j2, long j3, long j4) {
        return needRefresh(capabilities, capabilityRefreshType, j, j3);
    }

    public boolean needCapabilitiesUpdate(CapabilityConstants.CapExResult capExResult, Capabilities capabilities, long j, long j2) {
        if (capabilities == null || capExResult == CapabilityConstants.CapExResult.USER_NOT_FOUND) {
            IMSLog.i(TAG, this.mPhoneId, "needCapabilitiesUpdate: Capability is null");
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

    public boolean isCloseSessionNeeded(ImError imError) {
        return imError.isOneOf(ImError.MSRP_ACTION_NOT_ALLOWED, ImError.REMOTE_PARTY_DECLINED);
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
