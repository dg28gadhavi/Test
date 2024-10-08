package com.sec.internal.ims.servicemodules.im.strategy;

import android.content.Context;
import android.text.TextUtils;
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
import com.sec.internal.constants.ims.servicemodules.options.CapabilityConstants;
import com.sec.internal.constants.ims.servicemodules.presence.PresenceSubscription;
import com.sec.internal.helper.translate.MappingTranslator;
import com.sec.internal.ims.servicemodules.im.ImConfig;
import com.sec.internal.ims.servicemodules.im.MessageBase;
import com.sec.internal.ims.servicemodules.im.data.FtResumableOption;
import com.sec.internal.ims.servicemodules.im.strategy.IMnoStrategy;
import com.sec.internal.ims.settings.RcsPolicySettings;
import com.sec.internal.ims.util.ChatbotUriUtil;
import com.sec.internal.log.IMSLog;
import java.nio.charset.StandardCharsets;
import java.util.HashSet;
import java.util.Set;

public final class ChnStrategy extends DefaultRCSMnoStrategy {
    private static final String TAG = "ChnStrategy";
    private final int MAX_RETRY_COUNT = 1;
    private final int RETRY_AFTER_MAX = 4;
    private final HashSet<ImError> mRetryNeededErrorsForFt = new HashSet<>();
    private final HashSet<ImError> mRetryNeededErrorsForIm = new HashSet<>();
    private final HashSet<ImError> mRetryNeededFT_retryafter = new HashSet<>();
    private final HashSet<ImError> mRetryNeededIM_retryafter = new HashSet<>();

    public long updateFeatures(Capabilities capabilities, long j, CapabilityConstants.CapExResult capExResult) {
        return j;
    }

    public ChnStrategy(Context context, int i) {
        super(context, i);
        init();
    }

    private void init() {
        this.mRetryNeededIM_retryafter.add(ImError.SERVICE_UNAVAILABLE);
        HashSet<ImError> hashSet = this.mRetryNeededIM_retryafter;
        ImError imError = ImError.DEDICATED_BEARER_ERROR;
        hashSet.add(imError);
        HashSet<ImError> hashSet2 = this.mRetryNeededIM_retryafter;
        ImError imError2 = ImError.TRANSACTION_DOESNT_EXIST;
        hashSet2.add(imError2);
        HashSet<ImError> hashSet3 = this.mRetryNeededIM_retryafter;
        ImError imError3 = ImError.NETWORK_ERROR;
        hashSet3.add(imError3);
        HashSet<ImError> hashSet4 = this.mRetryNeededErrorsForIm;
        ImError imError4 = ImError.MSRP_REQUEST_UNINTELLIGIBLE;
        hashSet4.add(imError4);
        HashSet<ImError> hashSet5 = this.mRetryNeededErrorsForIm;
        ImError imError5 = ImError.MSRP_TRANSACTION_TIMED_OUT;
        hashSet5.add(imError5);
        HashSet<ImError> hashSet6 = this.mRetryNeededErrorsForIm;
        ImError imError6 = ImError.MSRP_DO_NOT_SEND_THIS_MESSAGE;
        hashSet6.add(imError6);
        HashSet<ImError> hashSet7 = this.mRetryNeededErrorsForIm;
        ImError imError7 = ImError.MSRP_SESSION_DOES_NOT_EXIST;
        hashSet7.add(imError7);
        HashSet<ImError> hashSet8 = this.mRetryNeededErrorsForIm;
        ImError imError8 = ImError.MSRP_UNKNOWN_CONTENT_TYPE;
        hashSet8.add(imError8);
        HashSet<ImError> hashSet9 = this.mRetryNeededErrorsForIm;
        ImError imError9 = ImError.MSRP_PARAMETERS_OUT_OF_BOUND;
        hashSet9.add(imError9);
        HashSet<ImError> hashSet10 = this.mRetryNeededErrorsForIm;
        ImError imError10 = ImError.MSRP_SESSION_ON_OTHER_CONNECTION;
        hashSet10.add(imError10);
        HashSet<ImError> hashSet11 = this.mRetryNeededErrorsForIm;
        ImError imError11 = ImError.MSRP_UNKNOWN_ERROR;
        hashSet11.add(imError11);
        HashSet<ImError> hashSet12 = this.mRetryNeededErrorsForIm;
        ImError imError12 = ImError.MSRP_UNKNOWN_METHOD;
        hashSet12.add(imError12);
        this.mRetryNeededFT_retryafter.add(imError3);
        this.mRetryNeededFT_retryafter.add(imError2);
        this.mRetryNeededFT_retryafter.add(imError);
        this.mRetryNeededFT_retryafter.add(ImError.NORMAL_RELEASE);
        this.mRetryNeededErrorsForFt.add(imError4);
        this.mRetryNeededErrorsForFt.add(imError5);
        this.mRetryNeededErrorsForFt.add(imError6);
        this.mRetryNeededErrorsForFt.add(imError9);
        this.mRetryNeededErrorsForFt.add(imError8);
        this.mRetryNeededErrorsForFt.add(imError7);
        this.mRetryNeededErrorsForFt.add(imError12);
        this.mRetryNeededErrorsForFt.add(imError10);
        this.mRetryNeededErrorsForFt.add(imError11);
    }

    public IMnoStrategy.StrategyResponse handleSendingMessageFailure(ImError imError, int i, int i2, ChatData.ChatType chatType, boolean z, boolean z2) {
        if (z) {
            return handleSlmFailure(imError);
        }
        IMnoStrategy.StatusCode retryStrategy = getRetryStrategy(i, imError, i2, chatType);
        if (retryStrategy == IMnoStrategy.StatusCode.NO_RETRY) {
            return handleImFailure(imError, chatType);
        }
        return new IMnoStrategy.StrategyResponse(retryStrategy);
    }

    public IMnoStrategy.StrategyResponse handleSendingFtMsrpMessageFailure(ImError imError, int i, int i2, ChatData.ChatType chatType, boolean z) {
        IMnoStrategy.StatusCode ftMsrpRetryStrategy = getFtMsrpRetryStrategy(i, imError, i2);
        if (ftMsrpRetryStrategy == IMnoStrategy.StatusCode.NO_RETRY) {
            return handleFtFailure(imError, chatType);
        }
        return new IMnoStrategy.StrategyResponse(ftMsrpRetryStrategy);
    }

    public IMnoStrategy.StrategyResponse checkCapability(Set<ImsUri> set, long j, ChatData.ChatType chatType, boolean z) {
        String str = TAG;
        int i = this.mPhoneId;
        IMSLog.i(str, i, "checkCapability->capability:" + j + ",isBroadcastMsg:" + z);
        if (j == ((long) Capabilities.FEATURE_FT_SERVICE) || j == ((long) Capabilities.FEATURE_FT_HTTP)) {
            return new IMnoStrategy.StrategyResponse(IMnoStrategy.StatusCode.NONE);
        }
        if (z || !ChatData.ChatType.isGroupChat(chatType)) {
            return getStrategyResponse();
        }
        return new IMnoStrategy.StrategyResponse(IMnoStrategy.StatusCode.NONE);
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
        if (i >= 1) {
            return IMnoStrategy.StatusCode.NO_RETRY;
        }
        if (this.mRetryNeededErrorsForFt.contains(imError)) {
            return IMnoStrategy.StatusCode.RETRY_IMMEDIATE;
        }
        if (this.mRetryNeededFT_retryafter.contains(imError)) {
            if (i2 > 0 && i2 <= 4) {
                return IMnoStrategy.StatusCode.RETRY_AFTER;
            }
            if (i2 <= 0) {
                return IMnoStrategy.StatusCode.RETRY_AFTER_SESSION;
            }
        }
        return IMnoStrategy.StatusCode.NO_RETRY;
    }

    public FtResumableOption getftResumableOption(CancelReason cancelReason, boolean z, ImDirection imDirection, int i) {
        if (cancelReason == null) {
            return FtResumableOption.NOTRESUMABLE;
        }
        if (z) {
            return getResumableOptionGroupFt(cancelReason, imDirection);
        }
        return getResumableOptionSingleFt(cancelReason, imDirection);
    }

    private FtResumableOption getResumableOptionSingleFt(CancelReason cancelReason, ImDirection imDirection) {
        String str = TAG;
        int i = this.mPhoneId;
        IMSLog.i(str, i, "getResumableOptionSingleFt, cancelreason: " + cancelReason.getId() + " direction:" + imDirection.getId());
        return FtCancelReasonResumableOptionCodeMap.translateCancelReason(cancelReason);
    }

    private FtResumableOption getResumableOptionGroupFt(CancelReason cancelReason, ImDirection imDirection) {
        String str = TAG;
        int i = this.mPhoneId;
        IMSLog.i(str, i, "getResumableOptionGroupFt, cancelreason: " + cancelReason.getId() + " direction:" + imDirection.getId());
        if (imDirection == ImDirection.INCOMING && (cancelReason == CancelReason.CANCELED_BY_REMOTE || cancelReason == CancelReason.ERROR)) {
            return FtResumableOption.MANUALLY_RESUMABLE_ONLY;
        }
        if (cancelReason == CancelReason.CANCELED_BY_REMOTE || cancelReason == CancelReason.CANCELED_NOTIFICATION) {
            return FtResumableOption.NOTRESUMABLE;
        }
        if (cancelReason == CancelReason.CANCELED_BY_USER) {
            return FtResumableOption.MANUALLY_RESUMABLE_ONLY;
        }
        if (cancelReason == CancelReason.ERROR || cancelReason == CancelReason.DEVICE_UNREGISTERED) {
            return FtResumableOption.MANUALLY_AUTOMATICALLY_RESUMABLE;
        }
        return FtResumableOption.MANUALLY_RESUMABLE_ONLY;
    }

    public boolean isSubscribeThrottled(PresenceSubscription presenceSubscription, long j, boolean z, boolean z2) {
        IMSLog.i(TAG, this.mPhoneId, "isSubscribeThrottled");
        return false;
    }

    public boolean needRefresh(Capabilities capabilities, CapabilityRefreshType capabilityRefreshType, long j, long j2, long j3, long j4) {
        return needRefresh(capabilities, capabilityRefreshType, j, j3);
    }

    public boolean needCapabilitiesUpdate(CapabilityConstants.CapExResult capExResult, Capabilities capabilities, long j, long j2) {
        if (capabilities == null) {
            IMSLog.i(TAG, this.mPhoneId, "needCapabilitiesUpdate: Capability is null");
            return true;
        } else if (capExResult != CapabilityConstants.CapExResult.FAILURE) {
            return true;
        } else {
            return false;
        }
    }

    public boolean isCustomizedFeature(long j) {
        return ((long) Capabilities.FEATURE_GEO_VIA_SMS) == j;
    }

    public ImConstants.ChatbotMessagingTech checkChatbotMessagingTech(ImConfig imConfig, boolean z, Set<ImsUri> set) {
        if (z || set.size() != 1 || !ChatbotUriUtil.hasChatbotUri(set, this.mPhoneId)) {
            return ImConstants.ChatbotMessagingTech.NONE;
        }
        if (imConfig.getChatbotMsgTech() == ImConstants.ChatbotMsgTechConfig.DISABLED) {
            return ImConstants.ChatbotMessagingTech.NOT_AVAILABLE;
        }
        IMSLog.i(TAG, this.mPhoneId, "checkChatbotMessagingTech: force to STANDALONE_MESSAGING");
        return ImConstants.ChatbotMessagingTech.STANDALONE_MESSAGING;
    }

    public RoutingType getMsgRoutingType(ImsUri imsUri, ImsUri imsUri2, ImsUri imsUri3, ImsUri imsUri4, boolean z) {
        String str = TAG;
        int i = this.mPhoneId;
        IMSLog.s(str, i, "getMsgRoutingType->requestUri:" + imsUri + ", pAssertedId:" + imsUri2 + ", sender:" + imsUri3 + ", receiver:" + imsUri4 + ", isGroupChat:" + z);
        RoutingType routingType = RoutingType.NONE;
        String msisdn = imsUri != null ? imsUri.getMsisdn() : null;
        if (z) {
            String msisdn2 = imsUri3.getMsisdn();
            if (!TextUtils.isEmpty(msisdn2) && !TextUtils.isEmpty(msisdn)) {
                routingType = msisdn2.contains(msisdn) ? RoutingType.SENT : RoutingType.RECEIVED;
            }
        } else if (!TextUtils.isEmpty(msisdn) && imsUri2 != null && !TextUtils.isEmpty(imsUri2.toString())) {
            routingType = imsUri2.toString().contains(msisdn) ? RoutingType.SENT : RoutingType.RECEIVED;
        }
        int i2 = this.mPhoneId;
        IMSLog.i(str, i2, "getMsgRoutingType routingType:" + routingType);
        return routingType;
    }

    public String getErrorReasonForStrategyResponse(MessageBase messageBase, IMnoStrategy.StrategyResponse strategyResponse) {
        if (strategyResponse == null) {
            return null;
        }
        if (strategyResponse.getStatusCode() != IMnoStrategy.StatusCode.FALLBACK_TO_LEGACY && strategyResponse.getStatusCode() != IMnoStrategy.StatusCode.FALLBACK_TO_LEGACY_CFS) {
            return null;
        }
        if (messageBase == null || ((messageBase.getType() != ImConstants.Type.TEXT && messageBase.getType() != ImConstants.Type.TEXT_PUBLICACCOUNT) || messageBase.getBody() == null)) {
            return ImErrorReason.FRAMEWORK_ERROR_FALLBACKFAILED.toString();
        }
        if (messageBase.getBody().getBytes(StandardCharsets.UTF_8).length > 900) {
            IMSLog.i(TAG, this.mPhoneId, "getErrorReasonForStrategyResponse(), > 900");
            return "";
        }
        IMSLog.i(TAG, this.mPhoneId, "getErrorReasonForStrategyResponse(), <= 900");
        return ImErrorReason.FRAMEWORK_ERROR_FALLBACKFAILED.toString();
    }

    public boolean isDeleteSessionSupported(ChatData.ChatType chatType, int i) {
        return chatType != ChatData.ChatType.REGULAR_GROUP_CHAT || i == ChatData.State.NONE.getId() || i == ChatData.State.CLOSED_BY_USER.getId();
    }

    public static class FtCancelReasonResumableOptionCodeMap {
        private static final MappingTranslator<CancelReason, FtResumableOption> mChnFtResumableOptionTranslator;

        static {
            MappingTranslator.Builder builder = new MappingTranslator.Builder();
            CancelReason cancelReason = CancelReason.UNKNOWN;
            FtResumableOption ftResumableOption = FtResumableOption.MANUALLY_RESUMABLE_ONLY;
            MappingTranslator.Builder map = builder.map(cancelReason, ftResumableOption).map(CancelReason.CANCELED_BY_USER, ftResumableOption).map(CancelReason.CANCELED_BY_REMOTE, ftResumableOption);
            CancelReason cancelReason2 = CancelReason.CANCELED_BY_SYSTEM;
            FtResumableOption ftResumableOption2 = FtResumableOption.NOTRESUMABLE;
            mChnFtResumableOptionTranslator = map.map(cancelReason2, ftResumableOption2).map(CancelReason.REJECTED_BY_REMOTE, ftResumableOption2).map(CancelReason.TIME_OUT, ftResumableOption).map(CancelReason.REMOTE_TEMPORARILY_UNAVAILABLE, ftResumableOption).map(CancelReason.ERROR, ftResumableOption).map(CancelReason.CONNECTION_RELEASED, ftResumableOption).map(CancelReason.DEVICE_UNREGISTERED, FtResumableOption.MANUALLY_AUTOMATICALLY_RESUMABLE).map(CancelReason.NOT_AUTHORIZED, ftResumableOption2).map(CancelReason.FORBIDDEN_NO_RETRY_FALLBACK, ftResumableOption2).map(CancelReason.MSRP_SESSION_ERROR_NO_RESUME, ftResumableOption2).map(CancelReason.CANCELED_NOTIFICATION, ftResumableOption2).buildTranslator();
        }

        public static FtResumableOption translateCancelReason(CancelReason cancelReason) {
            MappingTranslator<CancelReason, FtResumableOption> mappingTranslator = mChnFtResumableOptionTranslator;
            if (mappingTranslator.isTranslationDefined(cancelReason)) {
                return mappingTranslator.translate(cancelReason);
            }
            return FtResumableOption.MANUALLY_RESUMABLE_ONLY;
        }
    }

    public boolean isFTViaHttp(ImConfig imConfig, Set<ImsUri> set, ChatData.ChatType chatType) {
        return imConfig.getFtHttpEnabled();
    }

    public String getFtHttpUserAgent(ImConfig imConfig) {
        String userAgent = imConfig.getUserAgent();
        if (!boolSetting(RcsPolicySettings.RcsPolicy.FT_WITH_GBA)) {
            return userAgent;
        }
        return userAgent + " 3gpp-gba";
    }
}
