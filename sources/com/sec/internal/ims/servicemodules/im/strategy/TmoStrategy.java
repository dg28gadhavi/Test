package com.sec.internal.ims.servicemodules.im.strategy;

import android.content.Context;
import android.net.Uri;
import android.os.Build;
import android.text.TextUtils;
import com.sec.ims.options.Capabilities;
import com.sec.ims.options.CapabilityRefreshType;
import com.sec.ims.util.ImsUri;
import com.sec.internal.constants.ims.SipMsg;
import com.sec.internal.constants.ims.entitilement.NSDSNamespaces;
import com.sec.internal.constants.ims.servicemodules.im.ChatData;
import com.sec.internal.constants.ims.servicemodules.im.ImDirection;
import com.sec.internal.constants.ims.servicemodules.im.ImError;
import com.sec.internal.constants.ims.servicemodules.im.reason.CancelReason;
import com.sec.internal.constants.ims.servicemodules.im.reason.ImSessionClosedReason;
import com.sec.internal.constants.ims.servicemodules.options.CapabilityConstants;
import com.sec.internal.constants.ims.servicemodules.presence.PresenceResponse;
import com.sec.internal.constants.ims.servicemodules.volte2.CallConstants;
import com.sec.internal.constants.ims.settings.GlobalSettingsConstants;
import com.sec.internal.helper.translate.MappingTranslator;
import com.sec.internal.ims.registry.ImsRegistry;
import com.sec.internal.ims.servicemodules.im.ImConfig;
import com.sec.internal.ims.servicemodules.im.data.FtResumableOption;
import com.sec.internal.ims.servicemodules.im.strategy.IMnoStrategy;
import com.sec.internal.ims.util.ChatbotUriUtil;
import com.sec.internal.interfaces.ims.servicemodules.options.ICapabilityDiscoveryModule;
import com.sec.internal.log.IMSLog;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Date;
import java.util.HashSet;
import java.util.Set;

public final class TmoStrategy extends DefaultRCSMnoStrategy {
    private static final String TAG = "TmoStrategy";
    private final int MAX_RETRY_COUNT = 1;
    private final int RETRY_AFTER_MAX = 5;
    private final ArrayList<Integer> mFtResumeRetryMOTimerList = new ArrayList<>();
    private final ArrayList<Integer> mFtResumeRetryMTTimerList = new ArrayList<>();
    private String mRcsPhaseVersion = "";
    private final HashSet<ImError> mTmoForceRefreshRemoteCapa = new HashSet<>();
    private final HashSet<ImError> mTmoRetryNeededErrorsForFt = new HashSet<>();
    private final HashSet<ImError> mTmoRetryNeededErrorsForGroupIm = new HashSet<>();
    private final HashSet<ImError> mTmoRetryNeededErrorsForIm = new HashSet<>();
    private final HashSet<ImError> mTmoRetryNeededErrors_AfterRegi = new HashSet<>();
    private final HashSet<ImError> mTmoRetryNeededFT_changecontact = new HashSet<>();
    private final HashSet<ImError> mTmoRetryNeededFT_retryafter = new HashSet<>();
    private final HashSet<ImError> mTmoRetryNeededIM_changecontact = new HashSet<>();
    private final HashSet<ImError> mTmoRetryNeededIM_retryafter = new HashSet<>();

    public long getThrottledDelay(long j) {
        return j + 3;
    }

    public boolean isDisplayBotError() {
        return true;
    }

    public boolean isDisplayWarnText() {
        return true;
    }

    public boolean isResendFTResume(boolean z) {
        return z;
    }

    public TmoStrategy(Context context, int i) {
        super(context, i);
        initTmoMaps();
    }

    private void initTmoMaps() {
        this.mRcsPhaseVersion = ImsRegistry.getString(this.mPhoneId, GlobalSettingsConstants.RCS.RCS_PHASE_VERSION, "");
        HashSet<ImError> hashSet = this.mTmoRetryNeededErrorsForIm;
        ImError imError = ImError.UNSUPPORTED_URI_SCHEME;
        hashSet.add(imError);
        this.mTmoRetryNeededErrorsForIm.add(ImError.NETWORK_ERROR);
        HashSet<ImError> hashSet2 = this.mTmoRetryNeededErrorsForIm;
        ImError imError2 = ImError.BAD_GATEWAY;
        hashSet2.add(imError2);
        this.mTmoRetryNeededErrorsForIm.add(ImError.MSRP_TRANSACTION_TIMED_OUT);
        HashSet<ImError> hashSet3 = this.mTmoRetryNeededErrorsForIm;
        ImError imError3 = ImError.MSRP_DO_NOT_SEND_THIS_MESSAGE;
        hashSet3.add(imError3);
        HashSet<ImError> hashSet4 = this.mTmoRetryNeededErrorsForIm;
        ImError imError4 = ImError.MSRP_SESSION_DOES_NOT_EXIST;
        hashSet4.add(imError4);
        HashSet<ImError> hashSet5 = this.mTmoRetryNeededErrorsForIm;
        ImError imError5 = ImError.REQUEST_PENDING;
        hashSet5.add(imError5);
        HashSet<ImError> hashSet6 = this.mTmoRetryNeededErrorsForIm;
        ImError imError6 = ImError.FORBIDDEN_RETRY_FALLBACK;
        hashSet6.add(imError6);
        HashSet<ImError> hashSet7 = this.mTmoRetryNeededErrorsForIm;
        ImError imError7 = ImError.TRANSACTION_DOESNT_EXIST;
        hashSet7.add(imError7);
        HashSet<ImError> hashSet8 = this.mTmoRetryNeededErrorsForIm;
        ImError imError8 = ImError.TRANSACTION_DOESNT_EXIST_RETRY_FALLBACK;
        hashSet8.add(imError8);
        HashSet<ImError> hashSet9 = this.mTmoRetryNeededErrorsForIm;
        ImError imError9 = ImError.LOOP_DETECTED;
        hashSet9.add(imError9);
        HashSet<ImError> hashSet10 = this.mTmoRetryNeededErrorsForIm;
        ImError imError10 = ImError.TOO_MANY_HOPS;
        hashSet10.add(imError10);
        HashSet<ImError> hashSet11 = this.mTmoRetryNeededErrorsForIm;
        ImError imError11 = ImError.REMOTE_TEMPORARILY_UNAVAILABLE;
        hashSet11.add(imError11);
        this.mTmoRetryNeededErrorsForGroupIm.add(imError4);
        this.mTmoRetryNeededErrorsForGroupIm.add(ImError.MSRP_ACTION_NOT_ALLOWED);
        this.mTmoRetryNeededErrorsForFt.add(imError6);
        this.mTmoRetryNeededErrorsForFt.add(imError);
        this.mTmoRetryNeededErrorsForFt.add(imError2);
        this.mTmoRetryNeededErrorsForFt.add(imError3);
        this.mTmoRetryNeededErrorsForFt.add(imError5);
        this.mTmoRetryNeededErrorsForFt.add(imError7);
        this.mTmoRetryNeededErrorsForFt.add(imError8);
        this.mTmoRetryNeededErrorsForFt.add(ImError.GONE);
        this.mTmoRetryNeededErrorsForFt.add(imError9);
        this.mTmoRetryNeededErrorsForFt.add(imError10);
        this.mTmoRetryNeededErrorsForFt.add(imError11);
        this.mTmoRetryNeededFT_retryafter.add(imError11);
        HashSet<ImError> hashSet12 = this.mTmoRetryNeededFT_retryafter;
        ImError imError12 = ImError.INTERNAL_SERVER_ERROR;
        hashSet12.add(imError12);
        HashSet<ImError> hashSet13 = this.mTmoRetryNeededFT_retryafter;
        ImError imError13 = ImError.SERVICE_UNAVAILABLE;
        hashSet13.add(imError13);
        HashSet<ImError> hashSet14 = this.mTmoRetryNeededFT_retryafter;
        ImError imError14 = ImError.BUSY_EVERYWHERE;
        hashSet14.add(imError14);
        this.mTmoRetryNeededIM_retryafter.add(imError11);
        this.mTmoRetryNeededIM_retryafter.add(imError12);
        this.mTmoRetryNeededIM_retryafter.add(imError13);
        this.mTmoRetryNeededIM_retryafter.add(imError14);
        HashSet<ImError> hashSet15 = this.mTmoRetryNeededIM_retryafter;
        ImError imError15 = ImError.REMOTE_USER_INVALID;
        hashSet15.add(imError15);
        HashSet<ImError> hashSet16 = this.mTmoRetryNeededFT_changecontact;
        ImError imError16 = ImError.MULTIPLE_CHOICES;
        hashSet16.add(imError16);
        HashSet<ImError> hashSet17 = this.mTmoRetryNeededFT_changecontact;
        ImError imError17 = ImError.MOVED_PERMANENTLY;
        hashSet17.add(imError17);
        HashSet<ImError> hashSet18 = this.mTmoRetryNeededFT_changecontact;
        ImError imError18 = ImError.MOVED_TEMPORARILY;
        hashSet18.add(imError18);
        HashSet<ImError> hashSet19 = this.mTmoRetryNeededFT_changecontact;
        ImError imError19 = ImError.USE_PROXY;
        hashSet19.add(imError19);
        this.mTmoRetryNeededIM_changecontact.add(imError16);
        this.mTmoRetryNeededIM_changecontact.add(imError17);
        this.mTmoRetryNeededIM_changecontact.add(imError18);
        this.mTmoRetryNeededIM_changecontact.add(imError19);
        this.mTmoForceRefreshRemoteCapa.add(ImError.SESSION_TIMED_OUT);
        this.mTmoForceRefreshRemoteCapa.add(imError11);
        this.mTmoForceRefreshRemoteCapa.add(imError15);
        this.mTmoRetryNeededErrors_AfterRegi.add(ImError.FORBIDDEN_NO_WARNING_HEADER);
        this.mFtResumeRetryMTTimerList.add(1);
        this.mFtResumeRetryMOTimerList.add(1);
    }

    /* access modifiers changed from: protected */
    public IMnoStrategy.StrategyResponse getStrategyResponse() {
        return getStrategyResponse(ChatData.ChatType.ONE_TO_ONE_CHAT);
    }

    private IMnoStrategy.StrategyResponse getStrategyResponse(ChatData.ChatType chatType) {
        if (ChatData.ChatType.isGroupChatIdBasedGroupChat(chatType)) {
            return new IMnoStrategy.StrategyResponse(IMnoStrategy.StatusCode.DISPLAY_ERROR);
        }
        return super.getStrategyResponse();
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

    public IMnoStrategy.StrategyResponse handleFtHttpRequestFailure(CancelReason cancelReason, ImDirection imDirection, boolean z) {
        if (imDirection != ImDirection.OUTGOING || !cancelReason.equals(CancelReason.ERROR) || z) {
            return new IMnoStrategy.StrategyResponse(IMnoStrategy.StatusCode.NONE);
        }
        return getStrategyResponse();
    }

    public IMnoStrategy.StrategyResponse checkCapability(Set<ImsUri> set, long j, ChatData.ChatType chatType, boolean z) {
        String str = TAG;
        int i = this.mPhoneId;
        IMSLog.i(str, i, "checkCapability->features:" + j + ", isBroadcastMsg:" + z);
        if (ChatData.ChatType.isGroupChat(chatType) && z) {
            return getStrategyResponse();
        }
        if (ChatData.ChatType.isGroupChatIdBasedGroupChat(chatType)) {
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
        throw new UnsupportedOperationException("Method not decompiled: com.sec.internal.ims.servicemodules.im.strategy.TmoStrategy.checkCapability(java.util.Set, long):com.sec.internal.ims.servicemodules.im.strategy.IMnoStrategy$StrategyResponse");
    }

    public IMnoStrategy.StatusCode getRetryStrategy(int i, ImError imError, int i2, ChatData.ChatType chatType) {
        if (this.mTmoRetryNeededErrors_AfterRegi.contains(imError) && i < 4) {
            return IMnoStrategy.StatusCode.RETRY_AFTER_REGI;
        }
        if (i >= 1) {
            return IMnoStrategy.StatusCode.NO_RETRY;
        }
        if (this.mTmoRetryNeededIM_retryafter.contains(imError)) {
            if (i2 <= 0 || i2 > 5) {
                return IMnoStrategy.StatusCode.NO_RETRY;
            }
            return IMnoStrategy.StatusCode.RETRY_AFTER;
        } else if (ChatData.ChatType.isGroupChat(chatType) && this.mTmoRetryNeededErrorsForGroupIm.contains(imError)) {
            return IMnoStrategy.StatusCode.RETRY_IMMEDIATE;
        } else {
            if (this.mTmoRetryNeededErrorsForIm.contains(imError)) {
                return IMnoStrategy.StatusCode.RETRY_IMMEDIATE;
            }
            if (this.mTmoRetryNeededIM_changecontact.contains(imError)) {
                return IMnoStrategy.StatusCode.NO_RETRY;
            }
            return IMnoStrategy.StatusCode.NO_RETRY;
        }
    }

    public IMnoStrategy.StatusCode getFtMsrpRetryStrategy(int i, ImError imError, int i2) {
        if (!this.mTmoRetryNeededFT_retryafter.contains(imError)) {
            if (i < 1) {
                if (this.mTmoRetryNeededErrorsForFt.contains(imError)) {
                    return IMnoStrategy.StatusCode.RETRY_IMMEDIATE;
                }
                if (this.mTmoRetryNeededFT_changecontact.contains(imError)) {
                    return IMnoStrategy.StatusCode.NO_RETRY;
                }
            }
            return IMnoStrategy.StatusCode.NO_RETRY;
        } else if (i2 <= 0 || i2 > 5) {
            return IMnoStrategy.StatusCode.NO_RETRY;
        } else {
            return IMnoStrategy.StatusCode.RETRY_AFTER;
        }
    }

    public boolean isCapabilityValidUri(ImsUri imsUri) {
        if (!ChatbotUriUtil.hasUriBotPlatform(imsUri, this.mPhoneId)) {
            return StrategyUtils.isCapabilityValidUriForUS(imsUri, this.mPhoneId);
        }
        return true;
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
            for (ImsUri capabilities : set) {
                capDiscModule.getCapabilities(capabilities, (long) (Capabilities.FEATURE_FT_SERVICE | Capabilities.FEATURE_CHAT_CPM), i);
            }
        } else if (imError != null && this.mTmoForceRefreshRemoteCapa.contains(imError)) {
            for (ImsUri capabilities2 : set) {
                capDiscModule.getCapabilities(capabilities2, CapabilityRefreshType.ALWAYS_FORCE_REFRESH, i);
            }
        }
    }

    public FtResumableOption getftResumableOption(CancelReason cancelReason, boolean z, ImDirection imDirection, int i) {
        if (cancelReason == null) {
            return FtResumableOption.NOTRESUMABLE;
        }
        return z ? getResumableOptionGroupFt(cancelReason, imDirection, i) : getResumableOptionSingleFt(cancelReason, imDirection);
    }

    private FtResumableOption getResumableOptionSingleFt(CancelReason cancelReason, ImDirection imDirection) {
        String str = TAG;
        int i = this.mPhoneId;
        IMSLog.i(str, i, "getResumableOptionSingleFt, cancelreason: " + cancelReason.getId() + " direction:" + imDirection.getId());
        FtResumableOption translateCancelReason = TMOFtCancelReasonResumableOptionCodeMap.translateCancelReason(cancelReason);
        return translateCancelReason == null ? FtResumableOption.NOTRESUMABLE : translateCancelReason;
    }

    private FtResumableOption getResumableOptionGroupFt(CancelReason cancelReason, ImDirection imDirection, int i) {
        String str = TAG;
        int i2 = this.mPhoneId;
        IMSLog.i(str, i2, "getResumableOptionGroupFt, cancelreason: " + cancelReason.getId() + " direction:" + imDirection.getId() + " transferMech:" + i);
        if ((imDirection == ImDirection.INCOMING && i == 0) || cancelReason == CancelReason.CANCELED_BY_REMOTE || cancelReason == CancelReason.CANCELED_NOTIFICATION) {
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

    public boolean needRefresh(Capabilities capabilities, CapabilityRefreshType capabilityRefreshType, long j, long j2) {
        if (capabilityRefreshType == CapabilityRefreshType.DISABLED) {
            return false;
        }
        if (capabilities == null) {
            IMSLog.i(TAG, this.mPhoneId, "needRefresh: Capability is null");
            return true;
        } else if (isCapCacheExpired(capabilities, j2)) {
            IMSLog.i(TAG, this.mPhoneId, "needRefresh: Capability cache is expired");
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
            } else if (capabilityRefreshType == CapabilityRefreshType.FORCE_REFRESH_SYNC) {
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
    }

    public boolean needCapabilitiesUpdate(CapabilityConstants.CapExResult capExResult, Capabilities capabilities, long j, long j2) {
        if (capabilities == null || capExResult == CapabilityConstants.CapExResult.USER_NOT_FOUND) {
            IMSLog.i(TAG, this.mPhoneId, "needCapabilitiesUpdate: Capability is null");
            return true;
        } else if (capExResult == CapabilityConstants.CapExResult.FAILURE) {
            return isCapCacheExpired(capabilities, j2);
        } else {
            return true;
        }
    }

    public boolean needRefresh(Capabilities capabilities, CapabilityRefreshType capabilityRefreshType, long j, long j2, long j3, long j4) {
        return needRefresh(capabilities, capabilityRefreshType, j, j3);
    }

    private boolean isCapCacheExpired(Capabilities capabilities, long j) {
        boolean z = true;
        if (capabilities == null) {
            IMSLog.i(TAG, this.mPhoneId, "isCapCacheExpired: Capability is null");
            return true;
        }
        Date date = new Date();
        if (date.getTime() - capabilities.getTimestamp().getTime() < 1000 * j || j <= 0) {
            z = false;
        }
        if (z) {
            capabilities.resetFeatures();
            String str = TAG;
            int i = this.mPhoneId;
            IMSLog.i(str, i, "isCapCacheExpired: " + j + " current " + date.getTime() + " timestamp " + capabilities.getTimestamp().getTime() + " diff " + (date.getTime() - capabilities.getTimestamp().getTime()));
        }
        return z;
    }

    public int getNextFileTransferAutoResumeTimer(ImDirection imDirection, int i) {
        if (imDirection != ImDirection.INCOMING || i >= this.mFtResumeRetryMTTimerList.size()) {
            return -1;
        }
        return this.mFtResumeRetryMTTimerList.get(i).intValue();
    }

    public ImDirection convertToImDirection(String str) {
        return "Out".equals(str) ? ImDirection.OUTGOING : ImDirection.INCOMING;
    }

    /* access modifiers changed from: protected */
    /* JADX WARNING: Removed duplicated region for block: B:7:0x001b  */
    /* Code decompiled incorrectly, please refer to instructions dump. */
    public boolean checkFtHttpCapability(java.util.Set<com.sec.ims.util.ImsUri> r10) {
        /*
            r9 = this;
            com.sec.internal.interfaces.ims.servicemodules.options.ICapabilityDiscoveryModule r0 = r9.getCapDiscModule()
            int r1 = r9.mPhoneId
            r2 = 0
            if (r0 != 0) goto L_0x0011
            java.lang.String r9 = TAG
            java.lang.String r10 = "checkCapability: capDiscModule is null"
            com.sec.internal.log.IMSLog.i(r9, r1, r10)
            return r2
        L_0x0011:
            java.util.Iterator r10 = r10.iterator()
        L_0x0015:
            boolean r3 = r10.hasNext()
            if (r3 == 0) goto L_0x0056
            java.lang.Object r3 = r10.next()
            com.sec.ims.util.ImsUri r3 = (com.sec.ims.util.ImsUri) r3
            com.sec.ims.options.CapabilityRefreshType r4 = com.sec.ims.options.CapabilityRefreshType.ONLY_IF_NOT_FRESH
            com.sec.ims.options.Capabilities r4 = r0.getCapabilities((com.sec.ims.util.ImsUri) r3, (com.sec.ims.options.CapabilityRefreshType) r4, (int) r1)
            java.lang.String r5 = TAG
            int r6 = r9.mPhoneId
            java.lang.StringBuilder r7 = new java.lang.StringBuilder
            r7.<init>()
            java.lang.String r8 = "isFTViaHttp, uri = "
            r7.append(r8)
            java.lang.String r3 = com.sec.internal.log.IMSLog.numberChecker((com.sec.ims.util.ImsUri) r3)
            r7.append(r3)
            java.lang.String r3 = ", capx = "
            r7.append(r3)
            r7.append(r4)
            java.lang.String r3 = r7.toString()
            com.sec.internal.log.IMSLog.i(r5, r6, r3)
            if (r4 == 0) goto L_0x0055
            int r3 = com.sec.ims.options.Capabilities.FEATURE_FT_HTTP
            boolean r3 = r4.hasFeature(r3)
            if (r3 != 0) goto L_0x0015
        L_0x0055:
            return r2
        L_0x0056:
            r9 = 1
            return r9
        */
        throw new UnsupportedOperationException("Method not decompiled: com.sec.internal.ims.servicemodules.im.strategy.TmoStrategy.checkFtHttpCapability(java.util.Set):boolean");
    }

    public boolean isFTViaHttp(ImConfig imConfig, Set<ImsUri> set, ChatData.ChatType chatType) {
        Uri ftHttpCsUri = imConfig.getFtHttpCsUri();
        if (ftHttpCsUri == null || TextUtils.isEmpty(ftHttpCsUri.toString()) || !isFtHttpRegistered()) {
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

    public String getFtHttpUserAgent(ImConfig imConfig) {
        return buildFTHTTPUserAgentForTMOUS() + " 3gpp-gba";
    }

    private String buildFTHTTPUserAgentForTMOUS() {
        return "UP1" + ' ' + Build.VERSION.INCREMENTAL + ' ' + new SimpleDateFormat("yyyy-MM-dd").format(new Date()) + ' ' + NSDSNamespaces.NSDSSettings.OS + ' ' + Build.VERSION.RELEASE + ' ' + Build.MODEL;
    }

    public boolean isCustomizedFeature(long j) {
        return ((long) Capabilities.FEATURE_GEO_VIA_SMS) == j;
    }

    public boolean isFTHTTPAutoResumeAndCancelPerConnectionChange() {
        return !"RCS_TMB_PHASE2".equals(this.mRcsPhaseVersion);
    }

    public boolean isCloseSessionNeeded(ImError imError) {
        return imError.isOneOf(ImError.REMOTE_PARTY_DECLINED, ImError.FORBIDDEN_NO_WARNING_HEADER, ImError.MSRP_ACTION_NOT_ALLOWED, ImError.MSRP_SESSION_DOES_NOT_EXIST, ImError.MSRP_SESSION_ON_OTHER_CONNECTION);
    }

    public IMnoStrategy.StrategyResponse handleImFailure(ImError imError, ChatData.ChatType chatType) {
        if (imError.isOneOf(ImError.REMOTE_PARTY_DECLINED, ImError.FORBIDDEN_NO_WARNING_HEADER, ImError.MSRP_ACTION_NOT_ALLOWED, ImError.MSRP_SESSION_DOES_NOT_EXIST, ImError.MSRP_SESSION_ON_OTHER_CONNECTION, ImError.OUTOFSERVICE)) {
            return new IMnoStrategy.StrategyResponse(IMnoStrategy.StatusCode.DISPLAY_ERROR);
        }
        if (imError.isOneOf(ImError.GROUPCHAT_DISABLED)) {
            return new IMnoStrategy.StrategyResponse(ChatData.ChatType.isGroupChatIdBasedGroupChat(chatType) ? IMnoStrategy.StatusCode.DISPLAY_ERROR : IMnoStrategy.StatusCode.FALLBACK_TO_LEGACY);
        } else if (ChatData.ChatType.isGroupChatIdBasedGroupChat(chatType)) {
            return new IMnoStrategy.StrategyResponse(IMnoStrategy.StatusCode.DISPLAY_ERROR);
        } else {
            return getStrategyResponse();
        }
    }

    public IMnoStrategy.StrategyResponse handleFtFailure(ImError imError, ChatData.ChatType chatType) {
        if (imError.isOneOf(ImError.REMOTE_PARTY_DECLINED, ImError.FORBIDDEN_NO_WARNING_HEADER, ImError.BUSY_HERE, ImError.REMOTE_PARTY_CANCELED)) {
            return new IMnoStrategy.StrategyResponse(IMnoStrategy.StatusCode.DISPLAY_ERROR);
        }
        if (imError.isOneOf(ImError.DEVICE_UNREGISTERED)) {
            return new IMnoStrategy.StrategyResponse(ChatData.ChatType.isGroupChatIdBasedGroupChat(chatType) ? IMnoStrategy.StatusCode.DISPLAY_ERROR : IMnoStrategy.StatusCode.NONE);
        } else if (ChatData.ChatType.isGroupChatIdBasedGroupChat(chatType)) {
            return new IMnoStrategy.StrategyResponse(IMnoStrategy.StatusCode.DISPLAY_ERROR);
        } else {
            return getStrategyResponse();
        }
    }

    public PresenceResponse.PresenceStatusCode handlePresenceFailure(PresenceResponse.PresenceFailureReason presenceFailureReason, boolean z) {
        if (z) {
            if (presenceFailureReason.isOneOf(PresenceResponse.PresenceFailureReason.USER_NOT_REGISTERED, PresenceResponse.PresenceFailureReason.USER_NOT_PROVISIONED, PresenceResponse.PresenceFailureReason.FORBIDDEN)) {
                return PresenceResponse.PresenceStatusCode.PRESENCE_RE_REGISTRATION;
            }
            if (presenceFailureReason.isOneOf(PresenceResponse.PresenceFailureReason.INTERVAL_TOO_SHORT, PresenceResponse.PresenceFailureReason.INVALID_REQUEST, PresenceResponse.PresenceFailureReason.CONDITIONAL_REQUEST_FAILED, PresenceResponse.PresenceFailureReason.UNSUPPORTED_MEDIA_TYPE, PresenceResponse.PresenceFailureReason.BAD_EVENT)) {
                return PresenceResponse.PresenceStatusCode.PRESENCE_REQUIRE_FULL_PUBLISH;
            }
            if (presenceFailureReason.isOneOf(PresenceResponse.PresenceFailureReason.USER_NOT_FOUND, PresenceResponse.PresenceFailureReason.ENTITY_TOO_LARGE, PresenceResponse.PresenceFailureReason.TEMPORARILY_UNAVAILABLE, PresenceResponse.PresenceFailureReason.BUSY_HERE, PresenceResponse.PresenceFailureReason.SERVER_INTERNAL_ERROR, PresenceResponse.PresenceFailureReason.SERVICE_UNAVAILABLE, PresenceResponse.PresenceFailureReason.BUSY_EVERYWHERE, PresenceResponse.PresenceFailureReason.DECLINE)) {
                return PresenceResponse.PresenceStatusCode.PRESENCE_REQUIRE_RETRY_PUBLISH_AFTER;
            }
            return PresenceResponse.PresenceStatusCode.PRESENCE_REQUIRE_RETRY_PUBLISH;
        } else if (presenceFailureReason.isOneOf(PresenceResponse.PresenceFailureReason.USER_NOT_FOUND, PresenceResponse.PresenceFailureReason.METHOD_NOT_ALLOWED, PresenceResponse.PresenceFailureReason.USER_NOT_REGISTERED, PresenceResponse.PresenceFailureReason.USER_NOT_PROVISIONED, PresenceResponse.PresenceFailureReason.FORBIDDEN)) {
            return PresenceResponse.PresenceStatusCode.PRESENCE_NO_SUBSCRIBE;
        } else {
            return PresenceResponse.PresenceStatusCode.NONE;
        }
    }

    public IMnoStrategy.StrategyResponse handleFtMsrpInterruption(ImError imError) {
        if (imError.isOneOf(ImError.MSRP_ACTION_NOT_ALLOWED, ImError.MSRP_SESSION_DOES_NOT_EXIST, ImError.MSRP_SESSION_ON_OTHER_CONNECTION, ImError.NETWORK_ERROR, ImError.DEVICE_UNREGISTERED, ImError.REMOTE_PARTY_CANCELED, ImError.SESSION_TIMED_OUT, ImError.SERVICE_UNAVAILABLE, ImError.NORMAL_RELEASE)) {
            return new IMnoStrategy.StrategyResponse(IMnoStrategy.StatusCode.DISPLAY_ERROR);
        }
        return getStrategyResponse();
    }

    public IMnoStrategy.StrategyResponse handleAttachFileFailure(ImSessionClosedReason imSessionClosedReason) {
        if (isSlmEnabled()) {
            return new IMnoStrategy.StrategyResponse(IMnoStrategy.StatusCode.FALLBACK_TO_SLM);
        }
        return new IMnoStrategy.StrategyResponse(IMnoStrategy.StatusCode.FALLBACK_TO_LEGACY);
    }

    public boolean isDeleteSessionSupported(ChatData.ChatType chatType, int i) {
        return chatType == ChatData.ChatType.REGULAR_GROUP_CHAT;
    }

    public boolean checkSlmFileType(String str) {
        return !TextUtils.isEmpty(str) && (str.contains(CallConstants.ComposerData.IMAGE) || str.contains(SipMsg.FEATURE_TAG_MMTEL_VIDEO) || str.contains("audio") || "text/x-vCard".equalsIgnoreCase(str) || "text/vcard".equalsIgnoreCase(str) || "text/x-vCalendar".equalsIgnoreCase(str) || "text/x-vNote".equalsIgnoreCase(str) || "text/x-vtodo".equalsIgnoreCase(str) || "application/ogg".equalsIgnoreCase(str));
    }

    public IMnoStrategy.StrategyResponse getUploadedFileFallbackSLMTech() {
        return isSlmEnabled() ? new IMnoStrategy.StrategyResponse(IMnoStrategy.StatusCode.FALLBACK_TO_SLM_FILE) : new IMnoStrategy.StrategyResponse(IMnoStrategy.StatusCode.FALLBACK_TO_LEGACY);
    }

    public static class TMOFtCancelReasonResumableOptionCodeMap {
        private static final MappingTranslator<CancelReason, FtResumableOption> mTMOFtResumableOptionTranslator;

        static {
            MappingTranslator.Builder builder = new MappingTranslator.Builder();
            CancelReason cancelReason = CancelReason.UNKNOWN;
            FtResumableOption ftResumableOption = FtResumableOption.MANUALLY_RESUMABLE_ONLY;
            MappingTranslator.Builder map = builder.map(cancelReason, ftResumableOption).map(CancelReason.CANCELED_BY_USER, ftResumableOption);
            CancelReason cancelReason2 = CancelReason.CANCELED_BY_REMOTE;
            FtResumableOption ftResumableOption2 = FtResumableOption.NOTRESUMABLE;
            mTMOFtResumableOptionTranslator = map.map(cancelReason2, ftResumableOption2).map(CancelReason.CANCELED_BY_SYSTEM, ftResumableOption2).map(CancelReason.REJECTED_BY_REMOTE, ftResumableOption2).map(CancelReason.TIME_OUT, ftResumableOption).map(CancelReason.REMOTE_TEMPORARILY_UNAVAILABLE, ftResumableOption).map(CancelReason.ERROR, ftResumableOption).map(CancelReason.CONNECTION_RELEASED, ftResumableOption).map(CancelReason.DEVICE_UNREGISTERED, FtResumableOption.MANUALLY_AUTOMATICALLY_RESUMABLE).map(CancelReason.NOT_AUTHORIZED, ftResumableOption2).map(CancelReason.FORBIDDEN_NO_RETRY_FALLBACK, ftResumableOption2).map(CancelReason.MSRP_SESSION_ERROR_NO_RESUME, ftResumableOption2).map(CancelReason.CANCELED_NOTIFICATION, ftResumableOption2).buildTranslator();
        }

        public static FtResumableOption translateCancelReason(CancelReason cancelReason) {
            MappingTranslator<CancelReason, FtResumableOption> mappingTranslator = mTMOFtResumableOptionTranslator;
            if (mappingTranslator.isTranslationDefined(cancelReason)) {
                return mappingTranslator.translate(cancelReason);
            }
            return FtResumableOption.MANUALLY_RESUMABLE_ONLY;
        }
    }

    public ImSessionClosedReason handleSessionFailure(ImError imError, boolean z) {
        if (!z || imError != ImError.FORBIDDEN_SERVICE_NOT_AUTHORISED) {
            return ImSessionClosedReason.NONE;
        }
        return ImSessionClosedReason.KICKED_OUT_BY_LEADER;
    }
}
