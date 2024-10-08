package com.sec.internal.ims.servicemodules.im.strategy;

import android.content.Context;
import android.net.Uri;
import com.sec.ims.options.Capabilities;
import com.sec.ims.options.CapabilityRefreshType;
import com.sec.ims.util.ImsUri;
import com.sec.internal.constants.ims.servicemodules.im.ChatData;
import com.sec.internal.constants.ims.servicemodules.im.ImConstants;
import com.sec.internal.constants.ims.servicemodules.im.ImError;
import com.sec.internal.constants.ims.servicemodules.presence.PresenceResponse;
import com.sec.internal.ims.servicemodules.im.ImConfig;
import com.sec.internal.ims.servicemodules.im.strategy.IMnoStrategy;
import com.sec.internal.ims.util.ChatbotUriUtil;
import com.sec.internal.interfaces.ims.servicemodules.options.ICapabilityDiscoveryModule;
import com.sec.internal.log.IMSLog;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashSet;
import java.util.Set;

public class SecUPStrategy extends DefaultUPMnoStrategy {
    private static final String TAG = "SecUPStrategy";
    protected final int MAX_RETRY_COUNT = 1;
    private final HashSet<ImError> mForceRefreshRemoteCapa = new HashSet<>();

    public SecUPStrategy(Context context, int i) {
        super(context, i);
        init();
    }

    private void init() {
        this.mForceRefreshRemoteCapa.add(ImError.REMOTE_USER_INVALID);
    }

    /* access modifiers changed from: protected */
    public IMnoStrategy.StrategyResponse getStrategyResponse() {
        return new IMnoStrategy.StrategyResponse(IMnoStrategy.StatusCode.FALLBACK_TO_LEGACY);
    }

    public IMnoStrategy.StrategyResponse handleSendingMessageFailure(ImError imError, int i, int i2, ChatData.ChatType chatType, boolean z, boolean z2, boolean z3) {
        IMnoStrategy.StrategyResponse handleSendingMessageFailure = handleSendingMessageFailure(imError, i, i2, chatType, z, z3);
        if (!z2) {
            return handleSendingMessageFailure;
        }
        if (handleSendingMessageFailure.getStatusCode() != IMnoStrategy.StatusCode.FALLBACK_TO_SLM && handleSendingMessageFailure.getStatusCode() != IMnoStrategy.StatusCode.FALLBACK_TO_LEGACY && imError != ImError.GONE && imError != ImError.REQUEST_PENDING) {
            return handleSendingMessageFailure;
        }
        if (i < 1) {
            return new IMnoStrategy.StrategyResponse(IMnoStrategy.StatusCode.RETRY_AFTER);
        }
        return new IMnoStrategy.StrategyResponse(IMnoStrategy.StatusCode.NO_RETRY);
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
        ArrayList arrayList = new ArrayList(set);
        if (z) {
            capDiscModule.getCapabilities(arrayList, CapabilityRefreshType.ONLY_IF_NOT_FRESH, (long) (Capabilities.FEATURE_FT_HTTP | Capabilities.FEATURE_CHAT_SIMPLE_IM), this.mPhoneId);
        } else if (imError != null && this.mForceRefreshRemoteCapa.contains(imError)) {
            capDiscModule.getCapabilities(arrayList, CapabilityRefreshType.ALWAYS_FORCE_REFRESH, (long) (Capabilities.FEATURE_FT_HTTP | Capabilities.FEATURE_CHAT_SIMPLE_IM), this.mPhoneId);
        }
    }

    public Uri getFtHttpCsUri(ImConfig imConfig, Set<ImsUri> set, boolean z, boolean z2) {
        if (imConfig.getCbftHTTPCSURI() != null && !z2 && ChatbotUriUtil.hasChatbotUri(set, this.mPhoneId)) {
            return imConfig.getCbftHTTPCSURI();
        }
        if (imConfig.getFtHTTPExtraCSURI() == null || !z) {
            return imConfig.getFtHttpCsUri();
        }
        return imConfig.getFtHTTPExtraCSURI();
    }

    public ImConstants.ChatbotMessagingTech checkChatbotMessagingTech(ImConfig imConfig, boolean z, Set<ImsUri> set) {
        Capabilities capabilities;
        if (!z && set.size() == 1 && ChatbotUriUtil.hasChatbotUri(set, this.mPhoneId)) {
            ImConstants.ChatbotMsgTechConfig chatbotMsgTech = imConfig.getChatbotMsgTech();
            if (chatbotMsgTech == ImConstants.ChatbotMsgTechConfig.DISABLED) {
                return ImConstants.ChatbotMessagingTech.NOT_AVAILABLE;
            }
            ICapabilityDiscoveryModule capDiscModule = getCapDiscModule();
            if (!(capDiscModule == null || (capabilities = capDiscModule.getCapabilities(set.iterator().next(), CapabilityRefreshType.ONLY_IF_NOT_FRESH, imConfig.getPhoneId())) == null)) {
                if (capabilities.hasFeature(Capabilities.FEATURE_CHATBOT_CHAT_SESSION) && (chatbotMsgTech == ImConstants.ChatbotMsgTechConfig.SESSION_ONLY || chatbotMsgTech == ImConstants.ChatbotMsgTechConfig.BOTH_SESSION_AND_SLM)) {
                    return ImConstants.ChatbotMessagingTech.SESSION;
                }
                if (!capabilities.hasFeature(Capabilities.FEATURE_CHATBOT_STANDALONE_MSG) || (chatbotMsgTech != ImConstants.ChatbotMsgTechConfig.SLM_ONLY && chatbotMsgTech != ImConstants.ChatbotMsgTechConfig.BOTH_SESSION_AND_SLM)) {
                    return ImConstants.ChatbotMessagingTech.NOT_AVAILABLE;
                }
                return ImConstants.ChatbotMessagingTech.STANDALONE_MESSAGING;
            }
        }
        return ImConstants.ChatbotMessagingTech.NONE;
    }

    public boolean needStopAutoRejoin(ImError imError) {
        return imError.isOneOf(ImError.REMOTE_USER_INVALID, ImError.FORBIDDEN_RETRY_FALLBACK, ImError.FORBIDDEN_SERVICE_NOT_AUTHORISED, ImError.FORBIDDEN_VERSION_NOT_SUPPORTED, ImError.FORBIDDEN_RESTART_GC_CLOSED, ImError.FORBIDDEN_SIZE_EXCEEDED, ImError.FORBIDDEN_ANONYMITY_NOT_ALLOWED, ImError.FORBIDDEN_NO_DESTINATIONS, ImError.FORBIDDEN_NO_WARNING_HEADER, ImError.ADDRESS_INCOMPLETE);
    }

    public IMnoStrategy.StrategyResponse handleImFailure(ImError imError, ChatData.ChatType chatType) {
        if (imError.isOneOf(ImError.MSRP_TRANSACTION_TIMED_OUT, ImError.MSRP_DO_NOT_SEND_THIS_MESSAGE, ImError.MSRP_UNKNOWN_CONTENT_TYPE, ImError.MSRP_PARAMETERS_OUT_OF_BOUND, ImError.MSRP_SESSION_DOES_NOT_EXIST, ImError.MSRP_SESSION_ON_OTHER_CONNECTION, ImError.MSRP_UNKNOWN_ERROR)) {
            return new IMnoStrategy.StrategyResponse(IMnoStrategy.StatusCode.NONE);
        }
        return new IMnoStrategy.StrategyResponse(isSlmEnabled() ? IMnoStrategy.StatusCode.FALLBACK_TO_SLM : IMnoStrategy.StatusCode.FALLBACK_TO_LEGACY);
    }

    public PresenceResponse.PresenceStatusCode handlePresenceFailure(PresenceResponse.PresenceFailureReason presenceFailureReason, boolean z) {
        if (z) {
            if (presenceFailureReason.isOneOf(PresenceResponse.PresenceFailureReason.INVALID_REQUEST, PresenceResponse.PresenceFailureReason.USER_NOT_REGISTERED, PresenceResponse.PresenceFailureReason.USER_NOT_PROVISIONED, PresenceResponse.PresenceFailureReason.FORBIDDEN, PresenceResponse.PresenceFailureReason.CONDITIONAL_REQUEST_FAILED, PresenceResponse.PresenceFailureReason.INTERVAL_TOO_SHORT, PresenceResponse.PresenceFailureReason.BAD_EVENT)) {
                return PresenceResponse.PresenceStatusCode.PRESENCE_REQUIRE_FULL_PUBLISH;
            }
            if (presenceFailureReason.isOneOf(PresenceResponse.PresenceFailureReason.REQUEST_TIMEOUT, PresenceResponse.PresenceFailureReason.SERVER_INTERNAL_ERROR, PresenceResponse.PresenceFailureReason.SERVICE_UNAVAILABLE)) {
                return PresenceResponse.PresenceStatusCode.PRESENCE_RETRY_EXP_BACKOFF;
            }
            return PresenceResponse.PresenceStatusCode.PRESENCE_REQUIRE_RETRY_PUBLISH;
        } else if (presenceFailureReason.isOneOf(PresenceResponse.PresenceFailureReason.USER_NOT_FOUND, PresenceResponse.PresenceFailureReason.DOES_NOT_EXIST_ANYWHERE, PresenceResponse.PresenceFailureReason.METHOD_NOT_ALLOWED, PresenceResponse.PresenceFailureReason.USER_NOT_REGISTERED, PresenceResponse.PresenceFailureReason.USER_NOT_PROVISIONED)) {
            return PresenceResponse.PresenceStatusCode.PRESENCE_NO_SUBSCRIBE;
        } else {
            return PresenceResponse.PresenceStatusCode.NONE;
        }
    }
}
