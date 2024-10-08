package com.sec.internal.ims.servicemodules.im.strategy;

import android.content.Context;
import com.sec.ims.options.Capabilities;
import com.sec.ims.options.CapabilityRefreshType;
import com.sec.ims.util.ImsUri;
import com.sec.internal.constants.ims.servicemodules.im.ChatData;
import com.sec.internal.constants.ims.servicemodules.im.ImError;
import com.sec.internal.constants.ims.servicemodules.im.reason.ImSessionClosedReason;
import com.sec.internal.constants.ims.servicemodules.options.CapabilityConstants;
import com.sec.internal.constants.ims.servicemodules.presence.PresenceResponse;
import com.sec.internal.ims.servicemodules.im.strategy.IMnoStrategy;
import com.sec.internal.log.IMSLog;
import java.util.Set;

public class DefaultRCSMnoStrategy extends DefaultMnoStrategy {
    private static final String TAG = "DefaultRCSMnoStrategy";

    public long getThrottledDelay(long j) {
        return j;
    }

    public boolean isCloseSessionNeeded(ImError imError) {
        return false;
    }

    public boolean isCustomizedFeature(long j) {
        return false;
    }

    public boolean isDeleteSessionSupported(ChatData.ChatType chatType, int i) {
        return true;
    }

    public boolean isFTHTTPAutoResumeAndCancelPerConnectionChange() {
        return true;
    }

    public boolean isFirstMsgInvite(boolean z) {
        return z;
    }

    public boolean isResendFTResume(boolean z) {
        return false;
    }

    public boolean needStopAutoRejoin(ImError imError) {
        return false;
    }

    public long updateAvailableFeatures(Capabilities capabilities, long j, CapabilityConstants.CapExResult capExResult) {
        return j;
    }

    public DefaultRCSMnoStrategy(Context context, int i) {
        super(context, i);
    }

    public IMnoStrategy.StrategyResponse handleSendingMessageFailure(ImError imError, int i, int i2, ChatData.ChatType chatType, boolean z, boolean z2) {
        if (z && !ChatData.ChatType.isGroupChatIdBasedGroupChat(chatType)) {
            return handleSlmFailure(imError);
        }
        IMnoStrategy.StatusCode retryStrategy = getRetryStrategy(i, imError, i2, chatType);
        if (retryStrategy == IMnoStrategy.StatusCode.NO_RETRY) {
            return handleImFailure(imError, chatType);
        }
        return new IMnoStrategy.StrategyResponse(retryStrategy);
    }

    public IMnoStrategy.StrategyResponse checkCapability(Set<ImsUri> set, long j, ChatData.ChatType chatType, boolean z) {
        return checkCapability(set, j);
    }

    public IMnoStrategy.StrategyResponse checkCapability(Set<ImsUri> set, long j) {
        IMSLog.i(TAG, this.mPhoneId, "checkCapability");
        return new IMnoStrategy.StrategyResponse(IMnoStrategy.StatusCode.NONE);
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
        } else if (capabilityRefreshType == CapabilityRefreshType.FORCE_REFRESH_UCE || capabilityRefreshType == CapabilityRefreshType.ALWAYS_FORCE_REFRESH) {
            return true;
        } else {
            if (capabilityRefreshType == CapabilityRefreshType.ONLY_IF_NOT_FRESH && capabilities.isExpired(j)) {
                return true;
            }
            if (capabilityRefreshType != CapabilityRefreshType.ONLY_IF_NOT_FRESH_IN_MSG_CTX || !capabilities.isExpired(j4)) {
                return false;
            }
            return true;
        }
    }

    public boolean needCapabilitiesUpdate(CapabilityConstants.CapExResult capExResult, Capabilities capabilities, long j, long j2) {
        if (capExResult == CapabilityConstants.CapExResult.USER_UNAVAILABLE) {
            IMSLog.i(TAG, this.mPhoneId, "needCapabilitiesUpdate: User is offline");
            return false;
        } else if (capabilities == null || capExResult == CapabilityConstants.CapExResult.USER_NOT_FOUND) {
            IMSLog.i(TAG, this.mPhoneId, "needCapabilitiesUpdate: Capability is null");
            return true;
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
        if (capExResult != CapabilityConstants.CapExResult.USER_UNAVAILABLE || capabilities == null) {
            return j;
        }
        String str = TAG;
        int i = this.mPhoneId;
        IMSLog.i(str, i, "updateFeatures: keep old caps " + capabilities);
        return capabilities.getFeature();
    }

    public final IMnoStrategy.StrategyResponse handleSlmFailure(ImError imError) {
        if (imError == ImError.REMOTE_PARTY_DECLINED) {
            return new IMnoStrategy.StrategyResponse(IMnoStrategy.StatusCode.NONE);
        }
        return new IMnoStrategy.StrategyResponse(IMnoStrategy.StatusCode.FALLBACK_TO_LEGACY);
    }

    public boolean isNeedToReportToRegiGvn(ImError imError) {
        return imError.isOneOf(ImError.FORBIDDEN_NO_WARNING_HEADER);
    }

    public IMnoStrategy.StrategyResponse handleImFailure(ImError imError, ChatData.ChatType chatType) {
        return new IMnoStrategy.StrategyResponse(IMnoStrategy.StatusCode.NONE);
    }

    public IMnoStrategy.StrategyResponse handleFtFailure(ImError imError, ChatData.ChatType chatType) {
        return new IMnoStrategy.StrategyResponse(IMnoStrategy.StatusCode.NONE);
    }

    public PresenceResponse.PresenceStatusCode handlePresenceFailure(PresenceResponse.PresenceFailureReason presenceFailureReason, boolean z) {
        return PresenceResponse.PresenceStatusCode.NONE;
    }

    public IMnoStrategy.StrategyResponse handleFtMsrpInterruption(ImError imError) {
        return new IMnoStrategy.StrategyResponse(IMnoStrategy.StatusCode.DISPLAY_ERROR);
    }

    public IMnoStrategy.StrategyResponse handleAttachFileFailure(ImSessionClosedReason imSessionClosedReason) {
        return new IMnoStrategy.StrategyResponse(IMnoStrategy.StatusCode.FALLBACK_TO_LEGACY);
    }
}
