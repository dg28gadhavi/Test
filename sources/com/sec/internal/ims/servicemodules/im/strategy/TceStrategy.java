package com.sec.internal.ims.servicemodules.im.strategy;

import android.content.Context;
import com.sec.internal.constants.ims.servicemodules.im.ChatData;
import com.sec.internal.constants.ims.servicemodules.im.ImError;
import com.sec.internal.ims.servicemodules.im.strategy.IMnoStrategy;
import java.util.HashSet;

public final class TceStrategy extends DefaultRCSMnoStrategy {
    private final int MAX_RETRY_COUNT = 1;
    private final int RETRY_AFTER_MAX = 4;
    private final HashSet<ImError> mRetryNeededErrorsForFt = new HashSet<>();
    private final HashSet<ImError> mRetryNeededErrorsForIm = new HashSet<>();
    private final HashSet<ImError> mRetryNeededFT_retryafter = new HashSet<>();
    private final HashSet<ImError> mRetryNeededIM_retryafter = new HashSet<>();

    public TceStrategy(Context context, int i) {
        super(context, i);
        init();
    }

    private void init() {
        HashSet<ImError> hashSet = this.mRetryNeededFT_retryafter;
        ImError imError = ImError.NETWORK_ERROR;
        hashSet.add(imError);
        HashSet<ImError> hashSet2 = this.mRetryNeededFT_retryafter;
        ImError imError2 = ImError.DEDICATED_BEARER_ERROR;
        hashSet2.add(imError2);
        HashSet<ImError> hashSet3 = this.mRetryNeededFT_retryafter;
        ImError imError3 = ImError.TRANSACTION_DOESNT_EXIST;
        hashSet3.add(imError3);
        HashSet<ImError> hashSet4 = this.mRetryNeededErrorsForFt;
        ImError imError4 = ImError.MSRP_REQUEST_UNINTELLIGIBLE;
        hashSet4.add(imError4);
        HashSet<ImError> hashSet5 = this.mRetryNeededErrorsForFt;
        ImError imError5 = ImError.MSRP_TRANSACTION_TIMED_OUT;
        hashSet5.add(imError5);
        HashSet<ImError> hashSet6 = this.mRetryNeededErrorsForFt;
        ImError imError6 = ImError.MSRP_DO_NOT_SEND_THIS_MESSAGE;
        hashSet6.add(imError6);
        HashSet<ImError> hashSet7 = this.mRetryNeededErrorsForFt;
        ImError imError7 = ImError.MSRP_UNKNOWN_CONTENT_TYPE;
        hashSet7.add(imError7);
        HashSet<ImError> hashSet8 = this.mRetryNeededErrorsForFt;
        ImError imError8 = ImError.MSRP_SESSION_DOES_NOT_EXIST;
        hashSet8.add(imError8);
        HashSet<ImError> hashSet9 = this.mRetryNeededErrorsForFt;
        ImError imError9 = ImError.MSRP_UNKNOWN_METHOD;
        hashSet9.add(imError9);
        HashSet<ImError> hashSet10 = this.mRetryNeededErrorsForFt;
        ImError imError10 = ImError.MSRP_PARAMETERS_OUT_OF_BOUND;
        hashSet10.add(imError10);
        HashSet<ImError> hashSet11 = this.mRetryNeededErrorsForFt;
        ImError imError11 = ImError.MSRP_SESSION_ON_OTHER_CONNECTION;
        hashSet11.add(imError11);
        HashSet<ImError> hashSet12 = this.mRetryNeededErrorsForFt;
        ImError imError12 = ImError.MSRP_UNKNOWN_ERROR;
        hashSet12.add(imError12);
        this.mRetryNeededIM_retryafter.add(ImError.SERVICE_UNAVAILABLE);
        this.mRetryNeededIM_retryafter.add(imError);
        this.mRetryNeededIM_retryafter.add(imError2);
        this.mRetryNeededIM_retryafter.add(imError3);
        this.mRetryNeededErrorsForIm.add(imError4);
        this.mRetryNeededErrorsForIm.add(imError5);
        this.mRetryNeededErrorsForIm.add(imError6);
        this.mRetryNeededErrorsForIm.add(imError10);
        this.mRetryNeededErrorsForIm.add(imError8);
        this.mRetryNeededErrorsForIm.add(imError11);
        this.mRetryNeededErrorsForIm.add(imError7);
        this.mRetryNeededErrorsForIm.add(imError9);
        this.mRetryNeededErrorsForIm.add(imError12);
    }

    public IMnoStrategy.StatusCode getRetryStrategy(int i, ImError imError, int i2, ChatData.ChatType chatType) {
        if (i < 1) {
            if (this.mRetryNeededErrorsForIm.contains(imError)) {
                return IMnoStrategy.StatusCode.RETRY_IMMEDIATE;
            }
            if (this.mRetryNeededIM_retryafter.contains(imError)) {
                if (i2 > 0 && i2 <= 4) {
                    return IMnoStrategy.StatusCode.RETRY_AFTER;
                }
                if (i2 <= 0) {
                    return IMnoStrategy.StatusCode.RETRY_AFTER_SESSION;
                }
            }
        }
        return IMnoStrategy.StatusCode.NO_RETRY;
    }

    public IMnoStrategy.StatusCode getFtMsrpRetryStrategy(int i, ImError imError, int i2) {
        if (i < 1) {
            if (this.mRetryNeededErrorsForFt.contains(imError)) {
                return IMnoStrategy.StatusCode.RETRY_IMMEDIATE;
            }
            if (this.mRetryNeededFT_retryafter.contains(imError)) {
                if (i2 <= 0) {
                    return IMnoStrategy.StatusCode.RETRY_AFTER_SESSION;
                }
                if (i2 > 0 && i2 <= 4) {
                    return IMnoStrategy.StatusCode.RETRY_AFTER;
                }
            }
        }
        return IMnoStrategy.StatusCode.NO_RETRY;
    }

    public IMnoStrategy.StrategyResponse handleSendingMessageFailure(ImError imError, int i, int i2, ChatData.ChatType chatType, boolean z, boolean z2) {
        if (z) {
            return handleSlmFailure(imError);
        }
        IMnoStrategy.StatusCode retryStrategy = getRetryStrategy(i, imError, i2, chatType);
        if (retryStrategy == IMnoStrategy.StatusCode.NO_RETRY) {
            return ChatData.ChatType.isGroupChat(chatType) ? new IMnoStrategy.StrategyResponse(IMnoStrategy.StatusCode.DISPLAY_ERROR) : handleImFailure(imError, chatType);
        }
        return new IMnoStrategy.StrategyResponse(retryStrategy);
    }

    public IMnoStrategy.StrategyResponse handleSendingFtMsrpMessageFailure(ImError imError, int i, int i2, ChatData.ChatType chatType, boolean z) {
        IMnoStrategy.StatusCode ftMsrpRetryStrategy = getFtMsrpRetryStrategy(i, imError, i2);
        if (ftMsrpRetryStrategy == IMnoStrategy.StatusCode.NO_RETRY) {
            return ChatData.ChatType.isGroupChat(chatType) ? new IMnoStrategy.StrategyResponse(IMnoStrategy.StatusCode.DISPLAY_ERROR) : handleFtFailure(imError, chatType);
        }
        return new IMnoStrategy.StrategyResponse(ftMsrpRetryStrategy);
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

    public boolean isCloseSessionNeeded(ImError imError) {
        return imError.isOneOf(ImError.FORBIDDEN_NO_WARNING_HEADER);
    }

    public IMnoStrategy.StrategyResponse handleFtFailure(ImError imError, ChatData.ChatType chatType) {
        if (imError.isOneOf(ImError.FORBIDDEN_SERVICE_NOT_AUTHORISED, ImError.FORBIDDEN_VERSION_NOT_SUPPORTED)) {
            return new IMnoStrategy.StrategyResponse(IMnoStrategy.StatusCode.FALLBACK_TO_LEGACY);
        }
        return new IMnoStrategy.StrategyResponse(IMnoStrategy.StatusCode.DISPLAY_ERROR);
    }
}
