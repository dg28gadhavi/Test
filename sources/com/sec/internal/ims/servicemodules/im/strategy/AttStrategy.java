package com.sec.internal.ims.servicemodules.im.strategy;

import android.content.Context;
import android.net.Uri;
import android.text.TextUtils;
import com.sec.ims.options.Capabilities;
import com.sec.ims.options.CapabilityRefreshType;
import com.sec.ims.util.ImsUri;
import com.sec.internal.constants.ims.servicemodules.im.ChatData;
import com.sec.internal.constants.ims.servicemodules.im.ImDirection;
import com.sec.internal.constants.ims.servicemodules.im.ImError;
import com.sec.internal.constants.ims.servicemodules.im.reason.CancelReason;
import com.sec.internal.constants.ims.servicemodules.im.reason.ImSessionClosedReason;
import com.sec.internal.constants.ims.servicemodules.presence.PresenceResponse;
import com.sec.internal.constants.ims.servicemodules.presence.PresenceSubscription;
import com.sec.internal.constants.ims.settings.GlobalSettingsConstants;
import com.sec.internal.helper.DmConfigHelper;
import com.sec.internal.helper.translate.MappingTranslator;
import com.sec.internal.ims.registry.ImsRegistry;
import com.sec.internal.ims.servicemodules.im.ImConfig;
import com.sec.internal.ims.servicemodules.im.data.FtResumableOption;
import com.sec.internal.ims.servicemodules.im.strategy.IMnoStrategy;
import com.sec.internal.ims.settings.DeviceConfigManager;
import com.sec.internal.ims.settings.RcsPolicySettings;
import com.sec.internal.ims.util.ChatbotUriUtil;
import com.sec.internal.interfaces.ims.servicemodules.im.IImModule;
import com.sec.internal.interfaces.ims.servicemodules.options.ICapabilityDiscoveryModule;
import com.sec.internal.log.IMSLog;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Date;
import java.util.HashSet;
import java.util.Set;
import java.util.concurrent.ArrayBlockingQueue;

public final class AttStrategy extends DefaultRCSMnoStrategy {
    private static final String TAG = "AttStrategy";
    private final int CONSECUTIVE_SUBSCRIBE_THRESHOLD = 10;
    private final int LIMITED_SUBSCRIBE_INTERVAL = 1000;
    private final int MAX_RETRY_COUNT = 1;
    private final int RETRY_AFTER_MAX = 5;
    private final HashSet<ImError> mForceRefreshRemoteCapa = new HashSet<>();
    private final int[] mFtHttpMOSessionRetryTimerList = {0, 10, 20};
    private final int[] mFtResumeRetryMOTimerList = {1};
    private final int[] mFtResumeRetryMTTimerList = {1, 600, 3600, 86400, 172800, 259200};
    private String mRcsPhaseVersion;
    private final long[] mReconfigurationTimerList = {0, 14400000, 28800000, 57600000, 115200000};
    private final HashSet<ImError> mRetryNeededErrorsForFt = new HashSet<>();
    private final HashSet<ImError> mRetryNeededErrorsForIm = new HashSet<>();
    private final HashSet<ImError> mRetryNeededFT_changecontact = new HashSet<>();
    private final HashSet<ImError> mRetryNeededFT_retryafter = new HashSet<>();
    private final HashSet<ImError> mRetryNeededIM_changecontact = new HashSet<>();
    private final HashSet<ImError> mRetryNeededIM_retryafter = new HashSet<>();
    private final ArrayBlockingQueue<PresenceSubscription> mSubscriptionQueue = new ArrayBlockingQueue<>(10, true);

    public int getFtHttpRetryInterval(int i, int i2) {
        return i2 == 0 ? 5 : 3;
    }

    public boolean isDisplayBotError() {
        return true;
    }

    public boolean isPresenceReadyToRequest(boolean z, boolean z2) {
        return z && !z2;
    }

    public AttStrategy(Context context, int i) {
        super(context, i);
        init();
    }

    private void init() {
        this.mRcsPhaseVersion = ImsRegistry.getString(this.mPhoneId, GlobalSettingsConstants.RCS.RCS_PHASE_VERSION, "");
        HashSet<ImError> hashSet = this.mRetryNeededErrorsForIm;
        ImError imError = ImError.GONE;
        hashSet.add(imError);
        HashSet<ImError> hashSet2 = this.mRetryNeededErrorsForIm;
        ImError imError2 = ImError.UNSUPPORTED_URI_SCHEME;
        hashSet2.add(imError2);
        this.mRetryNeededErrorsForIm.add(ImError.NETWORK_ERROR);
        HashSet<ImError> hashSet3 = this.mRetryNeededErrorsForIm;
        ImError imError3 = ImError.BAD_GATEWAY;
        hashSet3.add(imError3);
        this.mRetryNeededErrorsForIm.add(ImError.MSRP_DO_NOT_SEND_THIS_MESSAGE);
        HashSet<ImError> hashSet4 = this.mRetryNeededErrorsForIm;
        ImError imError4 = ImError.REQUEST_PENDING;
        hashSet4.add(imError4);
        HashSet<ImError> hashSet5 = this.mRetryNeededErrorsForIm;
        ImError imError5 = ImError.TRANSACTION_DOESNT_EXIST;
        hashSet5.add(imError5);
        HashSet<ImError> hashSet6 = this.mRetryNeededErrorsForIm;
        ImError imError6 = ImError.TRANSACTION_DOESNT_EXIST_RETRY_FALLBACK;
        hashSet6.add(imError6);
        HashSet<ImError> hashSet7 = this.mRetryNeededErrorsForIm;
        ImError imError7 = ImError.LOOP_DETECTED;
        hashSet7.add(imError7);
        HashSet<ImError> hashSet8 = this.mRetryNeededErrorsForIm;
        ImError imError8 = ImError.TOO_MANY_HOPS;
        hashSet8.add(imError8);
        this.mRetryNeededErrorsForFt.add(ImError.FORBIDDEN_RETRY_FALLBACK);
        this.mRetryNeededErrorsForFt.add(imError2);
        this.mRetryNeededErrorsForFt.add(imError3);
        this.mRetryNeededErrorsForFt.add(imError4);
        this.mRetryNeededErrorsForFt.add(imError5);
        this.mRetryNeededErrorsForFt.add(imError6);
        this.mRetryNeededErrorsForFt.add(imError);
        this.mRetryNeededErrorsForFt.add(imError7);
        this.mRetryNeededErrorsForFt.add(imError8);
        HashSet<ImError> hashSet9 = this.mRetryNeededFT_retryafter;
        ImError imError9 = ImError.INTERNAL_SERVER_ERROR;
        hashSet9.add(imError9);
        this.mRetryNeededFT_retryafter.add(ImError.SERVICE_UNAVAILABLE);
        HashSet<ImError> hashSet10 = this.mRetryNeededFT_retryafter;
        ImError imError10 = ImError.BUSY_EVERYWHERE;
        hashSet10.add(imError10);
        this.mRetryNeededIM_retryafter.add(imError9);
        this.mRetryNeededIM_retryafter.add(imError10);
        HashSet<ImError> hashSet11 = this.mRetryNeededFT_changecontact;
        ImError imError11 = ImError.MULTIPLE_CHOICES;
        hashSet11.add(imError11);
        HashSet<ImError> hashSet12 = this.mRetryNeededFT_changecontact;
        ImError imError12 = ImError.MOVED_PERMANENTLY;
        hashSet12.add(imError12);
        HashSet<ImError> hashSet13 = this.mRetryNeededFT_changecontact;
        ImError imError13 = ImError.MOVED_TEMPORARILY;
        hashSet13.add(imError13);
        HashSet<ImError> hashSet14 = this.mRetryNeededFT_changecontact;
        ImError imError14 = ImError.USE_PROXY;
        hashSet14.add(imError14);
        this.mRetryNeededIM_changecontact.add(imError11);
        this.mRetryNeededIM_changecontact.add(imError12);
        this.mRetryNeededIM_changecontact.add(imError13);
        this.mRetryNeededIM_changecontact.add(imError14);
        this.mForceRefreshRemoteCapa.add(ImError.REMOTE_USER_INVALID);
        this.mForceRefreshRemoteCapa.add(imError);
    }

    public IMnoStrategy.StrategyResponse handleSendingMessageFailure(ImError imError, int i, int i2, ChatData.ChatType chatType, boolean z, boolean z2, boolean z3) {
        IMnoStrategy.StrategyResponse strategyResponse;
        if (z) {
            strategyResponse = handleSlmFailure(imError);
        } else if (!z3 || z2) {
            IMnoStrategy.StatusCode retryStrategy = getRetryStrategy(i, imError, i2, chatType);
            if (retryStrategy == IMnoStrategy.StatusCode.NO_RETRY) {
                strategyResponse = handleImFailure(imError, chatType);
            } else {
                strategyResponse = new IMnoStrategy.StrategyResponse(retryStrategy);
            }
        } else {
            strategyResponse = handleSendingFtHttpMessageFailure(imError, i);
        }
        if (z2) {
            return (strategyResponse.getStatusCode() == IMnoStrategy.StatusCode.FALLBACK_TO_SLM || strategyResponse.getStatusCode() == IMnoStrategy.StatusCode.FALLBACK_TO_LEGACY || imError == ImError.GONE || imError == ImError.REQUEST_PENDING) ? new IMnoStrategy.StrategyResponse(IMnoStrategy.StatusCode.NO_RETRY) : strategyResponse;
        }
        return strategyResponse;
    }

    private IMnoStrategy.StrategyResponse handleSendingFtHttpMessageFailure(ImError imError, int i) {
        int ftHttpSessionRetryTimer = getFtHttpSessionRetryTimer(i, imError);
        if (ftHttpSessionRetryTimer == -1) {
            return new IMnoStrategy.StrategyResponse(IMnoStrategy.StatusCode.DISPLAY_ERROR);
        }
        if (ftHttpSessionRetryTimer == 0) {
            return new IMnoStrategy.StrategyResponse(IMnoStrategy.StatusCode.RETRY_IMMEDIATE);
        }
        return new IMnoStrategy.StrategyResponse(IMnoStrategy.StatusCode.RETRY_AFTER);
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
        IMSLog.i(str, i, "checkCapability->capability:" + j + ", isBroadcastMsg:" + z);
        if (ChatData.ChatType.isGroupChat(chatType) && z) {
            return getStrategyResponse();
        }
        if (ChatData.ChatType.isGroupChatIdBasedGroupChat(chatType)) {
            return new IMnoStrategy.StrategyResponse(IMnoStrategy.StatusCode.NONE);
        }
        return checkCapability(set, j);
    }

    /* JADX WARNING: Removed duplicated region for block: B:7:0x002c  */
    /* Code decompiled incorrectly, please refer to instructions dump. */
    public com.sec.internal.ims.servicemodules.im.strategy.IMnoStrategy.StrategyResponse checkCapability(java.util.Set<com.sec.ims.util.ImsUri> r7, long r8) {
        /*
            r6 = this;
            com.sec.internal.interfaces.ims.servicemodules.options.ICapabilityDiscoveryModule r0 = r6.getCapDiscModule()
            int r5 = r6.mPhoneId
            if (r0 != 0) goto L_0x0014
            java.lang.String r7 = TAG
            java.lang.String r8 = "checkCapability: capDiscModule is null"
            com.sec.internal.log.IMSLog.i(r7, r5, r8)
            com.sec.internal.ims.servicemodules.im.strategy.IMnoStrategy$StrategyResponse r6 = r6.getStrategyResponse()
            return r6
        L_0x0014:
            java.util.ArrayList r1 = new java.util.ArrayList
            r1.<init>(r7)
            com.sec.ims.options.CapabilityRefreshType r2 = com.sec.ims.options.CapabilityRefreshType.ONLY_IF_NOT_FRESH
            int r3 = com.sec.ims.options.Capabilities.FEATURE_OFFLINE_RCS_USER
            long r3 = (long) r3
            com.sec.ims.options.Capabilities[] r0 = r0.getCapabilities(r1, r2, r3, r5)
            java.util.Iterator r7 = r7.iterator()
        L_0x0026:
            boolean r1 = r7.hasNext()
            if (r1 == 0) goto L_0x00b9
            java.lang.Object r1 = r7.next()
            com.sec.ims.util.ImsUri r1 = (com.sec.ims.util.ImsUri) r1
            com.sec.ims.options.Capabilities r2 = r6.findMatchingCapabilities(r1, r0)
            if (r2 == 0) goto L_0x0044
            boolean r3 = r2.isAvailable()
            if (r3 == 0) goto L_0x0044
            boolean r3 = r6.hasOneOfFeaturesAvailable(r2, r8)
            if (r3 != 0) goto L_0x0026
        L_0x0044:
            java.lang.String r7 = TAG
            int r0 = r6.mPhoneId
            java.lang.StringBuilder r3 = new java.lang.StringBuilder
            r3.<init>()
            java.lang.String r4 = "checkCapability: No capabilities for "
            r3.append(r4)
            boolean r4 = com.sec.internal.log.IMSLog.isShipBuild()
            if (r4 == 0) goto L_0x005f
            if (r1 == 0) goto L_0x005f
            java.lang.String r4 = r1.toStringLimit()
            goto L_0x0060
        L_0x005f:
            r4 = r1
        L_0x0060:
            r3.append(r4)
            if (r2 != 0) goto L_0x0068
            java.lang.String r2 = ""
            goto L_0x007d
        L_0x0068:
            java.lang.StringBuilder r4 = new java.lang.StringBuilder
            r4.<init>()
            java.lang.String r5 = ": isAvailable="
            r4.append(r5)
            boolean r2 = r2.isAvailable()
            r4.append(r2)
            java.lang.String r2 = r4.toString()
        L_0x007d:
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
            if (r1 == 0) goto L_0x00a5
            java.lang.String r8 = r1.toStringLimit()
            goto L_0x00a8
        L_0x00a5:
            java.lang.String r8 = "xx"
        L_0x00a8:
            r7.append(r8)
            java.lang.String r7 = r7.toString()
            r8 = 302710784(0x120b0000, float:4.3860666E-28)
            com.sec.internal.log.IMSLog.c(r8, r7)
            com.sec.internal.ims.servicemodules.im.strategy.IMnoStrategy$StrategyResponse r6 = r6.getStrategyResponse()
            return r6
        L_0x00b9:
            com.sec.internal.ims.servicemodules.im.strategy.IMnoStrategy$StrategyResponse r6 = new com.sec.internal.ims.servicemodules.im.strategy.IMnoStrategy$StrategyResponse
            com.sec.internal.ims.servicemodules.im.strategy.IMnoStrategy$StatusCode r7 = com.sec.internal.ims.servicemodules.im.strategy.IMnoStrategy.StatusCode.NONE
            r6.<init>(r7)
            return r6
        */
        throw new UnsupportedOperationException("Method not decompiled: com.sec.internal.ims.servicemodules.im.strategy.AttStrategy.checkCapability(java.util.Set, long):com.sec.internal.ims.servicemodules.im.strategy.IMnoStrategy$StrategyResponse");
    }

    private Capabilities findMatchingCapabilities(ImsUri imsUri, Capabilities[] capabilitiesArr) {
        if (capabilitiesArr == null) {
            IMSLog.e(TAG, this.mPhoneId, "findMatchingCapabilities: capexList is null");
            return null;
        }
        for (Capabilities capabilities : capabilitiesArr) {
            if (capabilities.getUri().equals(imsUri)) {
                return capabilities;
            }
        }
        return null;
    }

    public IMnoStrategy.StatusCode getRetryStrategy(int i, ImError imError, int i2, ChatData.ChatType chatType) {
        if (i < 1) {
            if (this.mRetryNeededErrorsForIm.contains(imError)) {
                if (imError != ImError.GONE || !ChatData.ChatType.isGroupChatIdBasedGroupChat(chatType)) {
                    return IMnoStrategy.StatusCode.RETRY_IMMEDIATE;
                }
                return IMnoStrategy.StatusCode.NO_RETRY;
            } else if (this.mRetryNeededIM_retryafter.contains(imError)) {
                if (i2 <= 0 || i2 > 5) {
                    return IMnoStrategy.StatusCode.NO_RETRY;
                }
                return IMnoStrategy.StatusCode.RETRY_AFTER;
            } else if (this.mRetryNeededIM_changecontact.contains(imError)) {
                return IMnoStrategy.StatusCode.NO_RETRY;
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
                if (i2 <= 0 || i2 > 5) {
                    return IMnoStrategy.StatusCode.NO_RETRY;
                }
                return IMnoStrategy.StatusCode.RETRY_AFTER;
            } else if (this.mRetryNeededFT_changecontact.contains(imError)) {
                return IMnoStrategy.StatusCode.NO_RETRY;
            }
        }
        return IMnoStrategy.StatusCode.NO_RETRY;
    }

    public boolean isCapabilityValidUri(ImsUri imsUri) {
        if (ChatbotUriUtil.hasUriBotPlatform(imsUri, this.mPhoneId)) {
            return true;
        }
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
        IMSLog.i(str, i, "forceRefreshCapability: uris " + IMSLog.numberChecker((Collection<ImsUri>) set));
        if (z) {
            capDiscModule.getCapabilities(new ArrayList(set), CapabilityRefreshType.ONLY_IF_NOT_FRESH, (long) (Capabilities.FEATURE_FT_SERVICE | Capabilities.FEATURE_CHAT_CPM), i);
        }
        if (imError != null && this.mForceRefreshRemoteCapa.contains(imError)) {
            capDiscModule.getCapabilities(new ArrayList(set), CapabilityRefreshType.ALWAYS_FORCE_REFRESH, Capabilities.FEATURE_CHATBOT_CHAT_SESSION | Capabilities.FEATURE_CHATBOT_STANDALONE_MSG, i);
        }
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
        return AttFtCancelReasonResumableOptionCodeMap.translateCancelReason(cancelReason);
    }

    private FtResumableOption getResumableOptionGroupFt(CancelReason cancelReason, ImDirection imDirection) {
        String str = TAG;
        int i = this.mPhoneId;
        IMSLog.i(str, i, "getResumableOptionGroupFt, cancelreason: " + cancelReason.getId() + " direction:" + imDirection.getId());
        if ((imDirection == ImDirection.INCOMING && cancelReason == CancelReason.CANCELED_BY_REMOTE) || cancelReason == CancelReason.CANCELED_NOTIFICATION) {
            return FtResumableOption.NOTRESUMABLE;
        }
        if (cancelReason == CancelReason.ERROR || cancelReason == CancelReason.DEVICE_UNREGISTERED) {
            return FtResumableOption.MANUALLY_AUTOMATICALLY_RESUMABLE;
        }
        return FtResumableOption.MANUALLY_RESUMABLE_ONLY;
    }

    public long calSubscribeDelayTime(PresenceSubscription presenceSubscription) {
        long j;
        String str = TAG;
        IMSLog.i(str, this.mPhoneId, "calSubscribeDelayTime");
        try {
            PresenceSubscription clone = presenceSubscription.clone();
            if (this.mSubscriptionQueue.remainingCapacity() == 0) {
                IMSLog.i(str, this.mPhoneId, "calSubscribeDelayTime: threshold is maxed");
                Date timestamp = this.mSubscriptionQueue.peek() != null ? this.mSubscriptionQueue.peek().getTimestamp() : null;
                if (timestamp != null) {
                    j = clone.getTimestamp().getTime() - timestamp.getTime();
                    IMSLog.i(str, this.mPhoneId, "calSubscribeDelayTime: interval from " + timestamp.getTime() + " to " + clone.getTimestamp().getTime() + ", offset " + j);
                } else {
                    j = 0;
                }
                if (j >= 0 && j < 1000) {
                    return 1000 - j;
                }
                try {
                    this.mSubscriptionQueue.take();
                } catch (InterruptedException unused) {
                    IMSLog.e(TAG, this.mPhoneId, "calSubscribeDelayTime: current queue is empty");
                }
            }
            this.mSubscriptionQueue.add(clone);
            return 0;
        } catch (CloneNotSupportedException e) {
            e.printStackTrace();
            return 0;
        }
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
        IMSLog.i(str, i, "isSubscribeThrottled: state " + presenceSubscription.getState() + " interval from " + presenceSubscription.getTimestamp().getTime() + " to " + date.getTime() + ", offset " + time + " sourceThrottlePublish " + j + " isAlwaysForce " + z2);
        if (z2) {
            if (presenceSubscription.getState() != 0 || time >= j) {
                return false;
            }
            return true;
        } else if (time < j) {
            return true;
        } else {
            return false;
        }
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
        } else if (capabilityRefreshType == CapabilityRefreshType.ALWAYS_FORCE_REFRESH) {
            return true;
        } else {
            if (capabilityRefreshType == CapabilityRefreshType.FORCE_REFRESH_UCE) {
                if (!capabilities.hasFeature(Capabilities.FEATURE_NON_RCS_USER)) {
                    return true;
                }
                IMSLog.i(TAG, this.mPhoneId, "needRefresh: non capabilitydisovery capable user");
                return capabilities.isExpired(j);
            } else if ((capabilityRefreshType == CapabilityRefreshType.ONLY_IF_NOT_FRESH && capabilities.isExpired(j)) || capabilityRefreshType == CapabilityRefreshType.FORCE_REFRESH_SYNC) {
                return true;
            } else {
                if (capabilityRefreshType != CapabilityRefreshType.ONLY_IF_NOT_FRESH_IN_MSG_CTX || !capabilities.isExpired(j)) {
                    return false;
                }
                return true;
            }
        }
    }

    public boolean needRefresh(Capabilities capabilities, CapabilityRefreshType capabilityRefreshType, long j, long j2, long j3, long j4) {
        return needRefresh(capabilities, capabilityRefreshType, j, j3);
    }

    public int getNextFileTransferAutoResumeTimer(ImDirection imDirection, int i) {
        if (imDirection == ImDirection.INCOMING) {
            int[] iArr = this.mFtResumeRetryMTTimerList;
            if (i < iArr.length) {
                return iArr[i];
            }
        }
        if (imDirection != ImDirection.OUTGOING) {
            return -1;
        }
        int[] iArr2 = this.mFtResumeRetryMOTimerList;
        if (i < iArr2.length) {
            return iArr2[i];
        }
        return -1;
    }

    public long calThrottledPublishRetryDelayTime(long j, long j2) {
        Date date = new Date();
        if (j2 > 0 && j > 0) {
            long j3 = j2 * 1000;
            if (date.getTime() - j < j3) {
                long time = (j + j3) - date.getTime();
                String str = TAG;
                int i = this.mPhoneId;
                IMSLog.i(str, i, "calThrottledPublishRetryDelayTime: throttled. retry in " + time + "ms");
                return time;
            }
        }
        return 0;
    }

    public boolean isFTViaHttp(ImConfig imConfig, Set<ImsUri> set, ChatData.ChatType chatType) {
        Uri ftHttpCsUri = imConfig.getFtHttpCsUri();
        if (!"RCS_ATT_PHASE2".equals(this.mRcsPhaseVersion) || ftHttpCsUri == null || TextUtils.isEmpty(ftHttpCsUri.toString()) || !isFtHttpRegistered()) {
            return false;
        }
        if (chatType == ChatData.ChatType.ONE_TO_ONE_CHAT) {
            return checkFtHttpCapability(set);
        }
        if (chatType == ChatData.ChatType.REGULAR_GROUP_CHAT) {
            return true;
        }
        return false;
    }

    public boolean isFtHttpOnlySupported(boolean z) {
        return "RCS_ATT_PHASE2".equals(this.mRcsPhaseVersion) && z;
    }

    public int getFtHttpSessionRetryTimer(int i, ImError imError) {
        if (!(ImError.MSRP_UNKNOWN_CONTENT_TYPE == imError || ImError.MSRP_TRANSACTION_TIMED_OUT == imError)) {
            int[] iArr = this.mFtHttpMOSessionRetryTimerList;
            if (i < iArr.length) {
                return iArr[i];
            }
        }
        return -1;
    }

    public IMnoStrategy.StrategyResponse handleFtHttpRequestFailure(CancelReason cancelReason, ImDirection imDirection, boolean z) {
        if (cancelReason.equals(CancelReason.FORBIDDEN_FT_HTTP) || cancelReason.equals(CancelReason.UNAUTHORIZED)) {
            IImModule imModule = getImModule();
            if (imModule != null) {
                imModule.reconfiguration(this.mReconfigurationTimerList);
            }
            return new IMnoStrategy.StrategyResponse(IMnoStrategy.StatusCode.NONE);
        } else if (imDirection != ImDirection.OUTGOING || !cancelReason.equals(CancelReason.ERROR) || z) {
            return new IMnoStrategy.StrategyResponse(IMnoStrategy.StatusCode.NONE);
        } else {
            return getStrategyResponse();
        }
    }

    public boolean checkMainSwitchOff(Context context, int i) {
        return "RCS_ATT_PHASE2".equals(this.mRcsPhaseVersion) && DmConfigHelper.getImsSwitchValue(context, DeviceConfigManager.RCS, i) != 1;
    }

    public boolean isHTTPUsedForEmptyFtHttpPOST() {
        return "RCS_ATT_PHASE2".equals(this.mRcsPhaseVersion);
    }

    public boolean isFTHTTPAutoResumeAndCancelPerConnectionChange() {
        return !"RCS_ATT_PHASE2".equals(this.mRcsPhaseVersion);
    }

    public boolean isCloseSessionNeeded(ImError imError) {
        return imError.isOneOf(ImError.REMOTE_PARTY_DECLINED, ImError.FORBIDDEN_NO_WARNING_HEADER, ImError.FORBIDDEN_SERVICE_NOT_AUTHORISED, ImError.EXCEED_MAXIMUM_RECIPIENTS);
    }

    public boolean isNeedToReportToRegiGvn(ImError imError) {
        if ("RCS_ATT_PHASE2".equals(this.mRcsPhaseVersion)) {
            return false;
        }
        return imError.isOneOf(ImError.FORBIDDEN_SERVICE_NOT_AUTHORISED, ImError.FORBIDDEN_NO_WARNING_HEADER);
    }

    public IMnoStrategy.StrategyResponse handleImFailure(ImError imError, ChatData.ChatType chatType) {
        if (imError.isOneOf(ImError.REMOTE_PARTY_DECLINED, ImError.FORBIDDEN_NO_WARNING_HEADER, ImError.SESSION_DOESNT_EXIST)) {
            return new IMnoStrategy.StrategyResponse(IMnoStrategy.StatusCode.DISPLAY_ERROR);
        }
        if (imError.isOneOf(ImError.FORBIDDEN_VERSION_NOT_SUPPORTED, ImError.NORMAL_RELEASE_BEARER_UNAVAILABLE, ImError.GROUPCHAT_DISABLED)) {
            return new IMnoStrategy.StrategyResponse(ChatData.ChatType.isGroupChatIdBasedGroupChat(chatType) ? IMnoStrategy.StatusCode.DISPLAY_ERROR : IMnoStrategy.StatusCode.FALLBACK_TO_LEGACY);
        } else if (imError.isOneOf(ImError.EXCEED_MAXIMUM_RECIPIENTS)) {
            return new IMnoStrategy.StrategyResponse(IMnoStrategy.StatusCode.DISPLAY_ERROR, IMnoStrategy.ErrorNotificationId.EXCEED_MAXIMUM_RECIPIENTS);
        } else {
            return new IMnoStrategy.StrategyResponse(ChatData.ChatType.isGroupChatIdBasedGroupChat(chatType) ? IMnoStrategy.StatusCode.DISPLAY_ERROR : IMnoStrategy.StatusCode.FALLBACK_TO_SLM);
        }
    }

    public IMnoStrategy.StrategyResponse handleFtFailure(ImError imError, ChatData.ChatType chatType) {
        if (imError.isOneOf(ImError.REMOTE_PARTY_DECLINED, ImError.FORBIDDEN_NO_WARNING_HEADER, ImError.REMOTE_PARTY_CANCELED, ImError.SESSION_DOESNT_EXIST)) {
            return new IMnoStrategy.StrategyResponse(IMnoStrategy.StatusCode.DISPLAY_ERROR);
        }
        if (imError.isOneOf(ImError.FORBIDDEN_VERSION_NOT_SUPPORTED)) {
            return new IMnoStrategy.StrategyResponse(ChatData.ChatType.isGroupChatIdBasedGroupChat(chatType) ? IMnoStrategy.StatusCode.DISPLAY_ERROR : IMnoStrategy.StatusCode.FALLBACK_TO_LEGACY);
        } else if (imError.isOneOf(ImError.DEVICE_UNREGISTERED)) {
            return new IMnoStrategy.StrategyResponse(IMnoStrategy.StatusCode.NONE);
        } else {
            if (imError.isOneOf(ImError.EXCEED_MAXIMUM_RECIPIENTS)) {
                return new IMnoStrategy.StrategyResponse(IMnoStrategy.StatusCode.DISPLAY_ERROR, IMnoStrategy.ErrorNotificationId.EXCEED_MAXIMUM_RECIPIENTS);
            }
            return new IMnoStrategy.StrategyResponse(ChatData.ChatType.isGroupChatIdBasedGroupChat(chatType) ? IMnoStrategy.StatusCode.DISPLAY_ERROR : IMnoStrategy.StatusCode.FALLBACK_TO_SLM);
        }
    }

    public PresenceResponse.PresenceStatusCode handlePresenceFailure(PresenceResponse.PresenceFailureReason presenceFailureReason, boolean z) {
        if (z) {
            if (presenceFailureReason.isOneOf(PresenceResponse.PresenceFailureReason.BAD_EVENT)) {
                return PresenceResponse.PresenceStatusCode.PRESENCE_AT_BAD_EVENT;
            }
            if (presenceFailureReason.isOneOf(PresenceResponse.PresenceFailureReason.USER_NOT_FOUND)) {
                return PresenceResponse.PresenceStatusCode.PRESENCE_RETRY_EXP_BACKOFF;
            }
            if (presenceFailureReason.isOneOf(PresenceResponse.PresenceFailureReason.INTERVAL_TOO_SHORT, PresenceResponse.PresenceFailureReason.INVALID_REQUEST)) {
                return PresenceResponse.PresenceStatusCode.PRESENCE_REQUIRE_FULL_PUBLISH;
            }
            return PresenceResponse.PresenceStatusCode.PRESENCE_REQUIRE_RETRY_PUBLISH;
        } else if (presenceFailureReason.isOneOf(PresenceResponse.PresenceFailureReason.USER_NOT_FOUND)) {
            return PresenceResponse.PresenceStatusCode.PRESENCE_NO_SUBSCRIBE;
        } else {
            if (presenceFailureReason.isOneOf(PresenceResponse.PresenceFailureReason.NO_RESPONSE)) {
                return PresenceResponse.PresenceStatusCode.PRESENCE_NO_RESPONSE;
            }
            return PresenceResponse.PresenceStatusCode.NONE;
        }
    }

    public IMnoStrategy.StrategyResponse handleAttachFileFailure(ImSessionClosedReason imSessionClosedReason) {
        if (imSessionClosedReason == ImSessionClosedReason.CLOSED_WITH_480_REASON_CODE) {
            return new IMnoStrategy.StrategyResponse(IMnoStrategy.StatusCode.FALLBACK_TO_LEGACY);
        }
        return getStrategyResponse();
    }

    public boolean isDeleteSessionSupported(ChatData.ChatType chatType, int i) {
        return chatType == ChatData.ChatType.REGULAR_GROUP_CHAT;
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

    public IMnoStrategy.StrategyResponse getUploadedFileFallbackSLMTech() {
        return new IMnoStrategy.StrategyResponse(IMnoStrategy.StatusCode.FALLBACK_TO_LEGACY);
    }

    public boolean needToCapabilityCheckForImdn(boolean z) {
        if (!z) {
            return true;
        }
        IMSLog.i(TAG, this.mPhoneId, "needToCapabilityCheckForImdn: failed");
        return false;
    }

    /* renamed from: com.sec.internal.ims.servicemodules.im.strategy.AttStrategy$1  reason: invalid class name */
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
                com.sec.internal.constants.ims.servicemodules.im.ImError r1 = com.sec.internal.constants.ims.servicemodules.im.ImError.TRANSACTION_DOESNT_EXIST     // Catch:{ NoSuchFieldError -> 0x001d }
                int r1 = r1.ordinal()     // Catch:{ NoSuchFieldError -> 0x001d }
                r2 = 2
                r0[r1] = r2     // Catch:{ NoSuchFieldError -> 0x001d }
            L_0x001d:
                int[] r0 = $SwitchMap$com$sec$internal$constants$ims$servicemodules$im$ImError     // Catch:{ NoSuchFieldError -> 0x0028 }
                com.sec.internal.constants.ims.servicemodules.im.ImError r1 = com.sec.internal.constants.ims.servicemodules.im.ImError.TRANSACTION_DOESNT_EXIST_RETRY_FALLBACK     // Catch:{ NoSuchFieldError -> 0x0028 }
                int r1 = r1.ordinal()     // Catch:{ NoSuchFieldError -> 0x0028 }
                r2 = 3
                r0[r1] = r2     // Catch:{ NoSuchFieldError -> 0x0028 }
            L_0x0028:
                int[] r0 = $SwitchMap$com$sec$internal$constants$ims$servicemodules$im$ImError     // Catch:{ NoSuchFieldError -> 0x0033 }
                com.sec.internal.constants.ims.servicemodules.im.ImError r1 = com.sec.internal.constants.ims.servicemodules.im.ImError.GONE     // Catch:{ NoSuchFieldError -> 0x0033 }
                int r1 = r1.ordinal()     // Catch:{ NoSuchFieldError -> 0x0033 }
                r2 = 4
                r0[r1] = r2     // Catch:{ NoSuchFieldError -> 0x0033 }
            L_0x0033:
                int[] r0 = $SwitchMap$com$sec$internal$constants$ims$servicemodules$im$ImError     // Catch:{ NoSuchFieldError -> 0x003e }
                com.sec.internal.constants.ims.servicemodules.im.ImError r1 = com.sec.internal.constants.ims.servicemodules.im.ImError.SESSION_DOESNT_EXIST     // Catch:{ NoSuchFieldError -> 0x003e }
                int r1 = r1.ordinal()     // Catch:{ NoSuchFieldError -> 0x003e }
                r2 = 5
                r0[r1] = r2     // Catch:{ NoSuchFieldError -> 0x003e }
            L_0x003e:
                return
            */
            throw new UnsupportedOperationException("Method not decompiled: com.sec.internal.ims.servicemodules.im.strategy.AttStrategy.AnonymousClass1.<clinit>():void");
        }
    }

    public boolean shouldRestartSession(ImError imError) {
        int i = AnonymousClass1.$SwitchMap$com$sec$internal$constants$ims$servicemodules$im$ImError[imError.ordinal()];
        if (i == 1 || i == 2 || i == 3) {
            return true;
        }
        if (i == 4 && !boolSetting(RcsPolicySettings.RcsPolicy.GONE_SHOULD_ENDSESSION)) {
            return true;
        }
        return false;
    }

    public static class AttFtCancelReasonResumableOptionCodeMap {
        private static final MappingTranslator<CancelReason, FtResumableOption> mAttFtResumableOptionTranslator;

        static {
            MappingTranslator.Builder builder = new MappingTranslator.Builder();
            CancelReason cancelReason = CancelReason.UNKNOWN;
            FtResumableOption ftResumableOption = FtResumableOption.MANUALLY_RESUMABLE_ONLY;
            MappingTranslator.Builder map = builder.map(cancelReason, ftResumableOption).map(CancelReason.CANCELED_BY_USER, ftResumableOption);
            CancelReason cancelReason2 = CancelReason.CANCELED_BY_REMOTE;
            FtResumableOption ftResumableOption2 = FtResumableOption.NOTRESUMABLE;
            MappingTranslator.Builder map2 = map.map(cancelReason2, ftResumableOption2).map(CancelReason.CANCELED_BY_SYSTEM, ftResumableOption2).map(CancelReason.REJECTED_BY_REMOTE, ftResumableOption2).map(CancelReason.TIME_OUT, ftResumableOption).map(CancelReason.REMOTE_TEMPORARILY_UNAVAILABLE, ftResumableOption);
            CancelReason cancelReason3 = CancelReason.ERROR;
            FtResumableOption ftResumableOption3 = FtResumableOption.MANUALLY_AUTOMATICALLY_RESUMABLE;
            mAttFtResumableOptionTranslator = map2.map(cancelReason3, ftResumableOption3).map(CancelReason.CONNECTION_RELEASED, ftResumableOption).map(CancelReason.DEVICE_UNREGISTERED, ftResumableOption3).map(CancelReason.NOT_AUTHORIZED, ftResumableOption2).map(CancelReason.FORBIDDEN_NO_RETRY_FALLBACK, ftResumableOption2).map(CancelReason.MSRP_SESSION_ERROR_NO_RESUME, ftResumableOption2).map(CancelReason.CANCELED_NOTIFICATION, ftResumableOption2).buildTranslator();
        }

        public static FtResumableOption translateCancelReason(CancelReason cancelReason) {
            MappingTranslator<CancelReason, FtResumableOption> mappingTranslator = mAttFtResumableOptionTranslator;
            if (mappingTranslator.isTranslationDefined(cancelReason)) {
                return mappingTranslator.translate(cancelReason);
            }
            return FtResumableOption.MANUALLY_AUTOMATICALLY_RESUMABLE;
        }
    }

    public ImSessionClosedReason handleSessionFailure(ImError imError, boolean z) {
        int i;
        if (!z || ((i = AnonymousClass1.$SwitchMap$com$sec$internal$constants$ims$servicemodules$im$ImError[imError.ordinal()]) != 4 && i != 5)) {
            return ImSessionClosedReason.NONE;
        }
        return ImSessionClosedReason.ALL_PARTICIPANTS_LEFT;
    }
}
