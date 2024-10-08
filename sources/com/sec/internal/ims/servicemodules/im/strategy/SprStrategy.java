package com.sec.internal.ims.servicemodules.im.strategy;

import android.content.Context;
import com.sec.ims.options.Capabilities;
import com.sec.ims.options.CapabilityRefreshType;
import com.sec.ims.util.ImsUri;
import com.sec.internal.constants.ims.servicemodules.im.ChatData;
import com.sec.internal.constants.ims.servicemodules.im.ImDirection;
import com.sec.internal.constants.ims.servicemodules.im.ImError;
import com.sec.internal.constants.ims.servicemodules.im.reason.CancelReason;
import com.sec.internal.ims.servicemodules.im.ImMessage;
import com.sec.internal.ims.servicemodules.im.MessageBase;
import com.sec.internal.ims.servicemodules.im.data.FtResumableOption;
import com.sec.internal.ims.servicemodules.im.strategy.IMnoStrategy;
import com.sec.internal.interfaces.ims.servicemodules.options.ICapabilityDiscoveryModule;
import com.sec.internal.log.IMSLog;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashSet;
import java.util.Set;

public final class SprStrategy extends DefaultRCSMnoStrategy {
    private static final String TAG = "SprStrategy";
    private final int MAX_RETRY_COUNT = 1;
    private final int RETRY_AFTER_MAX = 4;
    private final HashSet<ImError> mForceRefreshRemoteCapa = new HashSet<>();
    private final HashSet<ImError> mRetryNeededErrorsForFt = new HashSet<>();
    private final HashSet<ImError> mRetryNeededErrorsForIm = new HashSet<>();
    private final HashSet<ImError> mRetryNeededFT_retryafter = new HashSet<>();
    private final HashSet<ImError> mRetryNeededIM_retryafter = new HashSet<>();

    public boolean isFtHttpOnlySupported(boolean z) {
        return true;
    }

    public SprStrategy(Context context, int i) {
        super(context, i);
        init();
    }

    private void init() {
        HashSet<ImError> hashSet = this.mRetryNeededErrorsForIm;
        ImError imError = ImError.MSRP_REQUEST_UNINTELLIGIBLE;
        hashSet.add(imError);
        HashSet<ImError> hashSet2 = this.mRetryNeededErrorsForIm;
        ImError imError2 = ImError.MSRP_TRANSACTION_TIMED_OUT;
        hashSet2.add(imError2);
        HashSet<ImError> hashSet3 = this.mRetryNeededErrorsForIm;
        ImError imError3 = ImError.MSRP_DO_NOT_SEND_THIS_MESSAGE;
        hashSet3.add(imError3);
        HashSet<ImError> hashSet4 = this.mRetryNeededErrorsForIm;
        ImError imError4 = ImError.MSRP_UNKNOWN_CONTENT_TYPE;
        hashSet4.add(imError4);
        HashSet<ImError> hashSet5 = this.mRetryNeededErrorsForIm;
        ImError imError5 = ImError.MSRP_PARAMETERS_OUT_OF_BOUND;
        hashSet5.add(imError5);
        HashSet<ImError> hashSet6 = this.mRetryNeededErrorsForIm;
        ImError imError6 = ImError.MSRP_SESSION_DOES_NOT_EXIST;
        hashSet6.add(imError6);
        HashSet<ImError> hashSet7 = this.mRetryNeededErrorsForIm;
        ImError imError7 = ImError.MSRP_UNKNOWN_METHOD;
        hashSet7.add(imError7);
        HashSet<ImError> hashSet8 = this.mRetryNeededErrorsForIm;
        ImError imError8 = ImError.MSRP_SESSION_ON_OTHER_CONNECTION;
        hashSet8.add(imError8);
        HashSet<ImError> hashSet9 = this.mRetryNeededErrorsForIm;
        ImError imError9 = ImError.MSRP_UNKNOWN_ERROR;
        hashSet9.add(imError9);
        HashSet<ImError> hashSet10 = this.mRetryNeededIM_retryafter;
        ImError imError10 = ImError.NETWORK_ERROR;
        hashSet10.add(imError10);
        this.mRetryNeededIM_retryafter.add(ImError.SERVICE_UNAVAILABLE);
        HashSet<ImError> hashSet11 = this.mRetryNeededIM_retryafter;
        ImError imError11 = ImError.DEDICATED_BEARER_ERROR;
        hashSet11.add(imError11);
        HashSet<ImError> hashSet12 = this.mRetryNeededIM_retryafter;
        ImError imError12 = ImError.TRANSACTION_DOESNT_EXIST;
        hashSet12.add(imError12);
        this.mRetryNeededErrorsForFt.add(imError2);
        this.mRetryNeededErrorsForFt.add(imError);
        this.mRetryNeededErrorsForFt.add(imError3);
        this.mRetryNeededErrorsForFt.add(imError4);
        this.mRetryNeededErrorsForFt.add(imError5);
        this.mRetryNeededErrorsForFt.add(imError6);
        this.mRetryNeededErrorsForFt.add(imError7);
        this.mRetryNeededErrorsForFt.add(imError9);
        this.mRetryNeededErrorsForFt.add(imError8);
        this.mRetryNeededFT_retryafter.add(imError10);
        this.mRetryNeededFT_retryafter.add(imError11);
        this.mRetryNeededFT_retryafter.add(imError12);
        this.mForceRefreshRemoteCapa.add(ImError.REMOTE_USER_INVALID);
    }

    public void forceRefreshCapability(Set<ImsUri> set, boolean z, ImError imError) {
        ICapabilityDiscoveryModule capDiscModule = getCapDiscModule();
        int i = this.mPhoneId;
        if (capDiscModule == null) {
            IMSLog.i(TAG, i, "forceRefreshCapability: capDiscModule is null");
            return;
        }
        String str = TAG;
        IMSLog.i(str, i, "forceRefreshCapability: uris " + IMSLog.numberChecker((Collection<ImsUri>) set));
        if (z) {
            capDiscModule.getCapabilities(new ArrayList(set), CapabilityRefreshType.ONLY_IF_NOT_FRESH, (long) (Capabilities.FEATURE_FT_HTTP | Capabilities.FEATURE_CHAT_SIMPLE_IM), i);
        } else if (imError != null && this.mForceRefreshRemoteCapa.contains(imError)) {
            capDiscModule.getCapabilities(new ArrayList(set), CapabilityRefreshType.ALWAYS_FORCE_REFRESH, (long) (Capabilities.FEATURE_FT_HTTP | Capabilities.FEATURE_CHAT_SIMPLE_IM), i);
        }
    }

    public IMnoStrategy.StatusCode getRetryStrategy(int i, ImError imError, int i2, ChatData.ChatType chatType) {
        if (i < 1) {
            if (this.mRetryNeededErrorsForIm.contains(imError)) {
                return IMnoStrategy.StatusCode.RETRY_IMMEDIATE;
            }
            if (this.mRetryNeededIM_retryafter.contains(imError)) {
                if (i2 <= 0) {
                    return IMnoStrategy.StatusCode.RETRY_AFTER_SESSION;
                }
                if (i2 <= 4) {
                    return IMnoStrategy.StatusCode.RETRY_AFTER;
                }
            }
        }
        return IMnoStrategy.StatusCode.NO_RETRY;
    }

    public boolean isCapabilityValidUri(ImsUri imsUri) {
        return StrategyUtils.isCapabilityValidUriForUS(imsUri, this.mPhoneId);
    }

    public IMnoStrategy.StatusCode getFtMsrpRetryStrategy(int i, ImError imError, int i2) {
        if (i >= 1) {
            return IMnoStrategy.StatusCode.NO_RETRY;
        }
        if (this.mRetryNeededErrorsForFt.contains(imError)) {
            return IMnoStrategy.StatusCode.RETRY_IMMEDIATE;
        }
        if (this.mRetryNeededFT_retryafter.contains(imError)) {
            if (i2 <= 0) {
                return IMnoStrategy.StatusCode.RETRY_AFTER_SESSION;
            }
            if (i2 <= 4) {
                return IMnoStrategy.StatusCode.RETRY_AFTER;
            }
        }
        return IMnoStrategy.StatusCode.NO_RETRY;
    }

    public IMnoStrategy.StrategyResponse handleSendingMessageFailure(ImError imError, int i, int i2, ChatData.ChatType chatType, boolean z, boolean z2) {
        if (z) {
            return handleSlmFailure(imError);
        }
        IMnoStrategy.StatusCode retryStrategy = getRetryStrategy(i, imError, i2, chatType);
        if (retryStrategy != IMnoStrategy.StatusCode.NO_RETRY) {
            return new IMnoStrategy.StrategyResponse(retryStrategy);
        }
        if (ChatData.ChatType.isGroupChat(chatType)) {
            return new IMnoStrategy.StrategyResponse(IMnoStrategy.StatusCode.DISPLAY_ERROR);
        }
        return handleImFailure(imError, chatType);
    }

    public IMnoStrategy.StrategyResponse handleSendingFtMsrpMessageFailure(ImError imError, int i, int i2, ChatData.ChatType chatType, boolean z) {
        IMnoStrategy.StatusCode ftMsrpRetryStrategy = getFtMsrpRetryStrategy(i, imError, i2);
        if (ftMsrpRetryStrategy == IMnoStrategy.StatusCode.NO_RETRY) {
            return ChatData.ChatType.isGroupChat(chatType) ? new IMnoStrategy.StrategyResponse(IMnoStrategy.StatusCode.DISPLAY_ERROR) : handleFtFailure(imError, chatType);
        }
        return new IMnoStrategy.StrategyResponse(ftMsrpRetryStrategy);
    }

    public boolean isRevocationAvailableMessage(MessageBase messageBase) {
        return messageBase instanceof ImMessage;
    }

    public FtResumableOption getftResumableOption(CancelReason cancelReason, boolean z, ImDirection imDirection, int i) {
        if (cancelReason == CancelReason.CANCELED_BY_USER || cancelReason == CancelReason.DEVICE_UNREGISTERED || cancelReason == CancelReason.LOW_MEMORY) {
            return FtResumableOption.MANUALLY_RESUMABLE_ONLY;
        }
        return FtResumableOption.NOTRESUMABLE;
    }

    public boolean needStopAutoRejoin(ImError imError) {
        return imError.isOneOf(ImError.REMOTE_USER_INVALID, ImError.FORBIDDEN_RETRY_FALLBACK, ImError.FORBIDDEN_SERVICE_NOT_AUTHORISED, ImError.FORBIDDEN_VERSION_NOT_SUPPORTED, ImError.FORBIDDEN_RESTART_GC_CLOSED, ImError.FORBIDDEN_SIZE_EXCEEDED, ImError.FORBIDDEN_ANONYMITY_NOT_ALLOWED, ImError.FORBIDDEN_NO_DESTINATIONS, ImError.FORBIDDEN_NO_WARNING_HEADER);
    }

    public boolean isCloseSessionNeeded(ImError imError) {
        return imError.isOneOf(ImError.FORBIDDEN_NO_WARNING_HEADER);
    }

    public IMnoStrategy.StrategyResponse handleFtFailure(ImError imError, ChatData.ChatType chatType) {
        if (imError.isOneOf(ImError.FORBIDDEN_SERVICE_NOT_AUTHORISED, ImError.FORBIDDEN_VERSION_NOT_SUPPORTED)) {
            return new IMnoStrategy.StrategyResponse(IMnoStrategy.StatusCode.FALLBACK_TO_LEGACY);
        }
        return new IMnoStrategy.StrategyResponse(IMnoStrategy.StatusCode.DISPLAY_ERROR);
    }

    public IMnoStrategy.StrategyResponse handleImFailure(ImError imError, ChatData.ChatType chatType) {
        ImError imError2 = imError;
        if (imError2.isOneOf(ImError.INVALID_REQUEST, ImError.REMOTE_USER_INVALID, ImError.FORBIDDEN_RETRY_FALLBACK, ImError.TRANSACTION_DOESNT_EXIST_RETRY_FALLBACK, ImError.FORBIDDEN_SERVICE_NOT_AUTHORISED, ImError.FORBIDDEN_VERSION_NOT_SUPPORTED, ImError.FORBIDDEN_RESTART_GC_CLOSED, ImError.FORBIDDEN_SIZE_EXCEEDED, ImError.FORBIDDEN_ANONYMITY_NOT_ALLOWED, ImError.FORBIDDEN_NO_DESTINATIONS, ImError.FORBIDDEN_NO_WARNING_HEADER, ImError.UNSUPPORTED_MEDIA_TYPE, ImError.REMOTE_TEMPORARILY_UNAVAILABLE, ImError.INTERNAL_SERVER_ERROR, ImError.SERVICE_UNAVAILABLE, ImError.NO_DNS_RESULTS)) {
            return new IMnoStrategy.StrategyResponse(IMnoStrategy.StatusCode.FALLBACK_TO_LEGACY);
        }
        if (imError2.isOneOf(ImError.BUSY_HERE)) {
            return new IMnoStrategy.StrategyResponse(IMnoStrategy.StatusCode.SUCCESS);
        }
        return new IMnoStrategy.StrategyResponse(IMnoStrategy.StatusCode.DISPLAY_ERROR);
    }
}
