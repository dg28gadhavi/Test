package com.sec.internal.ims.servicemodules.presence;

import android.content.ContentValues;
import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.telephony.ims.ImsException;
import android.telephony.ims.SipDetails;
import android.telephony.ims.stub.RcsCapabilityExchangeImplBase;
import android.util.Pair;
import com.sec.ims.ImsRegistration;
import com.sec.ims.options.Capabilities;
import com.sec.ims.presence.PresenceInfo;
import com.sec.ims.presence.ServiceTuple;
import com.sec.internal.constants.ims.DiagnosisConstants;
import com.sec.internal.constants.ims.ImsConstants;
import com.sec.internal.constants.ims.config.ConfigConstants;
import com.sec.internal.constants.ims.servicemodules.options.CapabilityConstants;
import com.sec.internal.constants.ims.servicemodules.presence.PresenceResponse;
import com.sec.internal.constants.ims.servicemodules.presence.PresenceSubscription;
import com.sec.internal.helper.DmConfigHelper;
import com.sec.internal.ims.diagnosis.ImsLogAgentUtil;
import com.sec.internal.ims.rcs.RcsPolicyManager;
import com.sec.internal.ims.rcs.util.RcsUtils;
import com.sec.internal.ims.registry.ImsRegistry;
import com.sec.internal.ims.servicemodules.options.CapabilityUtil;
import com.sec.internal.ims.settings.RcsPolicySettings;
import com.sec.internal.log.IMSLog;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

public class PresenceUtil {
    private static final String LOG_TAG = "PresenceUtil";
    private static final Map<Integer, RcsCapabilityExchangeImplBase.PublishResponseCallback> mPublishResponseCallback = new ConcurrentHashMap();
    private static final Map<String, RcsCapabilityExchangeImplBase.SubscribeResponseCallback> mSubscribeResponseCallback = new ConcurrentHashMap();

    PresenceUtil() {
    }

    static CapabilityConstants.CapExResult translateToCapExResult(PresenceInfo presenceInfo, long j, PresenceResponse.PresenceStatusCode presenceStatusCode, PresenceSubscription presenceSubscription) {
        if (presenceInfo.isFetchSuccess()) {
            if (presenceSubscription == null || !presenceSubscription.isSingleFetch()) {
                return CapabilityConstants.CapExResult.POLLING_SUCCESS;
            }
            presenceSubscription.updateState(4);
            return CapabilityConstants.CapExResult.SUCCESS;
        } else if (presenceStatusCode == PresenceResponse.PresenceStatusCode.PRESENCE_AT_NOT_REGISTERED) {
            return CapabilityConstants.CapExResult.USER_NOT_REGISTERED;
        } else {
            if (presenceSubscription != null && CapabilityUtil.hasFeature(j, (long) Capabilities.FEATURE_NON_RCS_USER)) {
                return CapabilityConstants.CapExResult.USER_NOT_FOUND;
            }
            if (presenceSubscription == null || !CapabilityUtil.hasFeature(j, Capabilities.FEATURE_CHATBOT_ROLE)) {
                return CapabilityConstants.CapExResult.FAILURE;
            }
            return CapabilityConstants.CapExResult.USER_NOT_FOUND;
        }
    }

    /* JADX WARNING: Removed duplicated region for block: B:11:0x0029  */
    /* Code decompiled incorrectly, please refer to instructions dump. */
    static com.sec.ims.util.ImsUri convertUriType(com.sec.ims.util.ImsUri r0, boolean r1, com.sec.ims.presence.PresenceInfo r2, com.sec.internal.constants.Mno r3, com.sec.internal.ims.util.UriGenerator r4, int r5) {
        /*
            if (r2 != 0) goto L_0x0004
        L_0x0002:
            r2 = r0
            goto L_0x0027
        L_0x0004:
            boolean r3 = r3.isKor()
            if (r3 == 0) goto L_0x0019
            java.lang.String r3 = r2.getTelUri()
            if (r3 == 0) goto L_0x0002
            java.lang.String r2 = r2.getTelUri()
            com.sec.ims.util.ImsUri r2 = com.sec.ims.util.ImsUri.parse(r2)
            goto L_0x0027
        L_0x0019:
            java.lang.String r3 = r2.getUri()
            if (r3 == 0) goto L_0x0002
            java.lang.String r2 = r2.getUri()
            com.sec.ims.util.ImsUri r2 = com.sec.ims.util.ImsUri.parse(r2)
        L_0x0027:
            if (r1 == 0) goto L_0x002d
            com.sec.ims.util.ImsUri r2 = r4.getNetworkPreferredUri((com.sec.ims.util.ImsUri) r0)
        L_0x002d:
            java.lang.StringBuilder r0 = new java.lang.StringBuilder
            r0.<init>()
            java.lang.String r1 = "convertUriType: requested uri = "
            r0.append(r1)
            r0.append(r2)
            java.lang.String r0 = r0.toString()
            java.lang.String r1 = "PresenceUtil"
            com.sec.internal.log.IMSLog.s(r1, r5, r0)
            return r2
        */
        throw new UnsupportedOperationException("Method not decompiled: com.sec.internal.ims.servicemodules.presence.PresenceUtil.convertUriType(com.sec.ims.util.ImsUri, boolean, com.sec.ims.presence.PresenceInfo, com.sec.internal.constants.Mno, com.sec.internal.ims.util.UriGenerator, int):com.sec.ims.util.ImsUri");
    }

    static int getPollListSubExp(Context context, int i) {
        return DmConfigHelper.readInt(context, ConfigConstants.ConfigPath.OMADM_POLL_LIST_SUB_EXP, 30, i).intValue();
    }

    static void triggerOmadmTreeSync(Context context, int i) {
        IMSLog.s(LOG_TAG, i, "triggerOmadmTreeSync:");
        Intent intent = new Intent("com.samsung.sdm.START_DM_SYNC_SESSION");
        intent.setPackage(ImsConstants.Packages.PACKAGE_SDM);
        context.sendBroadcast(intent);
    }

    static boolean isRegProhibited(ImsRegistration imsRegistration, int i) {
        ImsRegistration imsRegistration2;
        if (imsRegistration == null || (imsRegistration2 = ImsRegistry.getRegistrationManager().getRegistrationList().get(Integer.valueOf(imsRegistration.getImsProfile().getId()))) == null) {
            return false;
        }
        imsRegistration.setProhibited(imsRegistration2.isProhibited());
        IMSLog.s(LOG_TAG, i, "isRegProhibited: " + imsRegistration2.isProhibited());
        return imsRegistration2.isProhibited();
    }

    static long getPublishExpBackOffRetryTime(int i, int i2) {
        if (RcsPolicyManager.getRcsStrategy(i).getPolicyType().isOneOf(RcsPolicySettings.RcsPolicyType.VZW, RcsPolicySettings.RcsPolicyType.VZW_UP)) {
            return calPublishExponentialBackOffRetryTime(i, i2);
        }
        if (RcsPolicyManager.getRcsStrategy(i).getPolicyType().isOneOf(RcsPolicySettings.RcsPolicyType.SEC_UP, RcsPolicySettings.RcsPolicyType.KT_UP)) {
            return calPublishExpBackOffRetryTimeUnlimit(i, i2);
        }
        if (RcsPolicyManager.getRcsStrategy(i).getPolicyType().isOneOf(RcsPolicySettings.RcsPolicyType.ATT)) {
            return calPublishExpBackOffRetryTimeForAtt(i, i2);
        }
        return 0;
    }

    static long getSubscribeExpBackOffRetryTime(int i, int i2) {
        if (RcsPolicyManager.getRcsStrategy(i).getPolicyType().isOneOf(RcsPolicySettings.RcsPolicyType.VZW, RcsPolicySettings.RcsPolicyType.VZW_UP)) {
            return calSubscribeExponentialBackOffRetryTime(i, i2);
        }
        return 0;
    }

    static long getListSubscribeExpBackOffRetryTime(int i, int i2) {
        if (RcsPolicyManager.getRcsStrategy(i).getPolicyType().isOneOf(RcsPolicySettings.RcsPolicyType.VZW, RcsPolicySettings.RcsPolicyType.VZW_UP)) {
            return calListSubscribeExponentialBackOffRetryTime(i, i2);
        }
        return 0;
    }

    static boolean getExtendedPublishTimerCond(int i, List<ServiceTuple> list) {
        return RcsPolicyManager.getRcsStrategy(i).getPolicyType().isOneOf(RcsPolicySettings.RcsPolicyType.VZW, RcsPolicySettings.RcsPolicyType.VZW_UP) && isExtendedPublishTimerCond(i, list);
    }

    private static long calPublishExponentialBackOffRetryTime(int i, int i2) {
        long[] jArr = {60, 120, 240, 480};
        if (i2 <= 4 && i2 > 0) {
            return jArr[i2 - 1];
        }
        IMSLog.s(LOG_TAG, i, "calPublishExponentialBackOffRetryTime: invalid retryCount: " + i2);
        return 0;
    }

    public static long calPublishExpBackOffRetryTimeUnlimit(int i, int i2) {
        long[] jArr = {120, 240, 480, 960, 3600};
        if (i2 <= 0) {
            IMSLog.s(LOG_TAG, i, "calPublishExponentialBackOffRetryTime: invalid retryCount: " + i2);
            return 0;
        } else if (i2 > 5) {
            return jArr[4];
        } else {
            return jArr[i2 - 1];
        }
    }

    private static long calPublishExpBackOffRetryTimeForAtt(int i, int i2) {
        long[] jArr = {1200, 3600, 7200, 14400, 28800};
        if (i2 <= 5 && i2 > 0) {
            return jArr[i2 - 1];
        }
        IMSLog.i(LOG_TAG, i, "calPublishExpBackOffRetryTimeForAtt: invalid retryCount: " + i2);
        return 0;
    }

    private static long calSubscribeExponentialBackOffRetryTime(int i, int i2) {
        long[] jArr = {60, 120, 240, 480};
        if (i2 <= 4 && i2 > 0) {
            return jArr[i2 - 1];
        }
        IMSLog.s(LOG_TAG, i, "calSubscribeExponentialBackOffRetryTime: invalid retryCount: " + i2);
        return 0;
    }

    private static long calListSubscribeExponentialBackOffRetryTime(int i, int i2) {
        long[] jArr = {1800, 3600, 7200, 14400, 28800};
        if (i2 <= 5 && i2 > 0) {
            return jArr[i2 - 1];
        }
        IMSLog.s(LOG_TAG, i, "calListSubscribeExponentialBackOffRetryTime: invalid retryCount: " + i2);
        return 0;
    }

    private static boolean isExtendedPublishTimerCond(int i, List<ServiceTuple> list) {
        long features = ServiceTuple.getFeatures(list);
        IMSLog.i(LOG_TAG, i, "isExtendedPublishTimerCond: services: " + list);
        return (((long) Capabilities.FEATURE_MMTEL_VIDEO) & features) == 0 && (((long) Capabilities.FEATURE_CHAT_CPM) & features) == 0 && (((long) Capabilities.FEATURE_FT) & features) == 0;
    }

    static void sendRCSPPubInfoToHQM(Context context, int i, String str, int i2) {
        if (i2 < 0) {
            i2 = 0;
        }
        ContentValues contentValues = new ContentValues();
        contentValues.put("ERRC", String.valueOf(i));
        contentValues.put(DiagnosisConstants.RCSP_KEY_ERES, str);
        ImsLogAgentUtil.sendLogToAgent(i2, context, DiagnosisConstants.FEATURE_RCSP, contentValues);
    }

    static void sendRCSPSubInfoToHQM(Context context, int i, int i2) {
        if (i != 403 && i != 404) {
            if (i2 < 0) {
                i2 = 0;
            }
            ContentValues contentValues = new ContentValues();
            contentValues.put(DiagnosisConstants.RCSP_KEY_SERR, String.valueOf(i));
            ImsLogAgentUtil.sendLogToAgent(i2, context, DiagnosisConstants.FEATURE_RCSP, contentValues);
        }
    }

    static void addPublishResponseCallback(int i, RcsCapabilityExchangeImplBase.PublishResponseCallback publishResponseCallback) {
        Map<Integer, RcsCapabilityExchangeImplBase.PublishResponseCallback> map = mPublishResponseCallback;
        if (map.containsKey(Integer.valueOf(i)) || publishResponseCallback == null) {
            IMSLog.e(LOG_TAG, i, "addPublishResponseCallback: already exist");
            return;
        }
        IMSLog.i(LOG_TAG, i, "addPublishResponseCallback: " + publishResponseCallback);
        map.put(Integer.valueOf(i), publishResponseCallback);
    }

    static RcsCapabilityExchangeImplBase.PublishResponseCallback getPublishResponseCallback(int i) {
        return mPublishResponseCallback.get(Integer.valueOf(i));
    }

    static void notifyPublishCommandError(int i, Context context, int i2) {
        if (RcsUtils.isImsSingleRegiRequired(context, i)) {
            IMSLog.i(LOG_TAG, i, "notifyPublishCommandError: code = " + i2);
            try {
                Map<Integer, RcsCapabilityExchangeImplBase.PublishResponseCallback> map = mPublishResponseCallback;
                if (map.containsKey(Integer.valueOf(i))) {
                    map.get(Integer.valueOf(i)).onCommandError(i2);
                    map.remove(Integer.valueOf(i));
                }
            } catch (ImsException e) {
                IMSLog.e(LOG_TAG, i, "notifyPublishCommandError: failed: " + e.getMessage());
            }
        }
    }

    static void notifyPublishNetworkResponse(int i, Context context, int i2, String str) {
        if (RcsUtils.isImsSingleRegiRequired(context, i)) {
            IMSLog.i(LOG_TAG, i, "notifyPublishNetworkResponse: sipCode = " + i2 + ", reason = " + str);
            try {
                Map<Integer, RcsCapabilityExchangeImplBase.PublishResponseCallback> map = mPublishResponseCallback;
                if (map.containsKey(Integer.valueOf(i))) {
                    SipDetails.Builder builder = new SipDetails.Builder(2);
                    if (str == null) {
                        str = "";
                    }
                    map.get(Integer.valueOf(i)).onNetworkResponse(builder.setSipResponseCode(i2, str).build());
                    map.remove(Integer.valueOf(i));
                }
            } catch (ImsException e) {
                IMSLog.e(LOG_TAG, i, "notifyPublishNetworkResponse: failed: " + e.getMessage());
            }
        }
    }

    static void notifyPublishNetworkResponse(int i, Context context, int i2, String str, int i3, String str2) {
        if (RcsUtils.isImsSingleRegiRequired(context, i)) {
            IMSLog.i(LOG_TAG, i, "notifyPublishNetworkResponse: sipCode = " + i2 + ", reasonPhrase = " + str + ", reasonHeaderCause = " + i3 + ", reasonHeaderText = " + str2);
            try {
                Map<Integer, RcsCapabilityExchangeImplBase.PublishResponseCallback> map = mPublishResponseCallback;
                if (map.containsKey(Integer.valueOf(i))) {
                    SipDetails.Builder builder = new SipDetails.Builder(2);
                    if (str == null) {
                        str = "";
                    }
                    SipDetails.Builder sipResponseCode = builder.setSipResponseCode(i2, str);
                    if (str2 == null) {
                        str2 = "";
                    }
                    map.get(Integer.valueOf(i)).onNetworkResponse(sipResponseCode.setSipResponseReasonHeader(i3, str2).build());
                    map.remove(Integer.valueOf(i));
                }
            } catch (ImsException e) {
                IMSLog.e(LOG_TAG, i, "notifyPublishNetworkResponse: failed: " + e.getMessage());
            }
        }
    }

    public static int addSubscribeResponseCallback(int i, RcsCapabilityExchangeImplBase.SubscribeResponseCallback subscribeResponseCallback) {
        if (subscribeResponseCallback == null) {
            return 0;
        }
        int random = (int) ((Math.random() * 1.0E8d) + 1.0d);
        String valueOf = String.valueOf(random);
        Map<String, RcsCapabilityExchangeImplBase.SubscribeResponseCallback> map = mSubscribeResponseCallback;
        if (map.getOrDefault(valueOf, (Object) null) == null) {
            map.put(valueOf, subscribeResponseCallback);
            IMSLog.i(LOG_TAG, "addSubscribeResponseCallback : internalRequestId : " + random);
            return random;
        }
        IMSLog.i(LOG_TAG, "addSubscribeResponseCallback : have same internalRequestId : " + random);
        return addSubscribeResponseCallback(i, subscribeResponseCallback);
    }

    private static RcsCapabilityExchangeImplBase.SubscribeResponseCallback getSubscribeResponseCallback(String str) {
        IMSLog.i(LOG_TAG, "getSubscribeResponseCallback : subscriptionId : " + str);
        return mSubscribeResponseCallback.get(str);
    }

    public static void replaceSubscribeResponseCbSubsId(int i, String str) {
        String valueOf = String.valueOf(i);
        IMSLog.i(LOG_TAG, "replaceSubscribeResponseCbSubsId : internalRequestId : " + i + ", subscriptionId : " + str);
        Map<String, RcsCapabilityExchangeImplBase.SubscribeResponseCallback> map = mSubscribeResponseCallback;
        if (map.containsKey(valueOf)) {
            map.remove(valueOf);
            map.put(str, map.get(valueOf));
        }
    }

    public static void removeSubscribeResponseCallback(String str) {
        Map<String, RcsCapabilityExchangeImplBase.SubscribeResponseCallback> map = mSubscribeResponseCallback;
        if (map.containsKey(str)) {
            IMSLog.i(LOG_TAG, "removeSubscribeResponseCallback subscriptionId : " + str);
            map.remove(str);
            return;
        }
        IMSLog.i(LOG_TAG, "removeSubscribeResponseCallback there is no callback " + str);
    }

    public static void onSubscribeCommandError(String str, Context context, int i, int i2) {
        if (RcsUtils.isImsSingleRegiRequired(context, i)) {
            IMSLog.i(LOG_TAG, "onSubscribeCommandError " + i2);
            RcsCapabilityExchangeImplBase.SubscribeResponseCallback subscribeResponseCallback = mSubscribeResponseCallback.get(str);
            if (subscribeResponseCallback != null) {
                try {
                    subscribeResponseCallback.onCommandError(i2);
                } catch (ImsException e) {
                    e.printStackTrace();
                }
            }
        }
    }

    public static void onSubscribeNetworkResponse(String str, Context context, int i, int i2, String str2) {
        if (RcsUtils.isImsSingleRegiRequired(context, i)) {
            IMSLog.i(LOG_TAG, "onSubscribeNetworkResponse : sipCode " + i2 + ", reason : " + str2 + ", subscriptionId : " + str);
            RcsCapabilityExchangeImplBase.SubscribeResponseCallback subscribeResponseCallback = mSubscribeResponseCallback.get(str);
            if (subscribeResponseCallback != null) {
                try {
                    SipDetails.Builder builder = new SipDetails.Builder(3);
                    if (str2 == null) {
                        str2 = "";
                    }
                    subscribeResponseCallback.onNetworkResponse(builder.setSipResponseCode(i2, str2).build());
                } catch (ImsException e) {
                    e.printStackTrace();
                }
            }
        }
    }

    public static void onSubscribeNetworkResponse(String str, Context context, int i, int i2, String str2, int i3, String str3) {
        if (RcsUtils.isImsSingleRegiRequired(context, i)) {
            IMSLog.i(LOG_TAG, "onSubscribeNetworkResponse : sipCode " + i2 + ", reason : " + str2 + ", subscriptionId : " + str);
            RcsCapabilityExchangeImplBase.SubscribeResponseCallback subscribeResponseCallback = mSubscribeResponseCallback.get(str);
            if (subscribeResponseCallback != null) {
                try {
                    SipDetails.Builder builder = new SipDetails.Builder(3);
                    if (str2 == null) {
                        str2 = "";
                    }
                    SipDetails.Builder sipResponseCode = builder.setSipResponseCode(i2, str2);
                    if (str3 == null) {
                        str3 = "";
                    }
                    subscribeResponseCallback.onNetworkResponse(sipResponseCode.setSipResponseReasonHeader(i3, str3).build());
                } catch (ImsException e) {
                    e.printStackTrace();
                }
            }
        }
    }

    public static void onSubscribeNotifyCapabilitiesUpdate(String str, Context context, int i, List<String> list) {
        if (RcsUtils.isImsSingleRegiRequired(context, i)) {
            IMSLog.i(LOG_TAG, "onSubscribeNotifyCapabilitiesUpdate, subscriptionId : " + str);
            RcsCapabilityExchangeImplBase.SubscribeResponseCallback subscribeResponseCallback = mSubscribeResponseCallback.get(str);
            if (subscribeResponseCallback != null) {
                try {
                    subscribeResponseCallback.onNotifyCapabilitiesUpdate(list);
                } catch (ImsException e) {
                    e.printStackTrace();
                }
            }
        }
    }

    public static void onSubscribeResourceTerminated(String str, Context context, int i, List<Pair<Uri, String>> list) {
        if (RcsUtils.isImsSingleRegiRequired(context, i)) {
            IMSLog.i(LOG_TAG, "onSubscribeResourceTerminated, subscriptionId : " + str);
            RcsCapabilityExchangeImplBase.SubscribeResponseCallback subscribeResponseCallback = mSubscribeResponseCallback.get(str);
            if (subscribeResponseCallback != null) {
                try {
                    subscribeResponseCallback.onResourceTerminated(list);
                } catch (ImsException e) {
                    e.printStackTrace();
                }
            }
        }
    }

    public static void onSubscribeTerminated(String str, Context context, int i, String str2, long j) {
        if (RcsUtils.isImsSingleRegiRequired(context, i)) {
            IMSLog.i(LOG_TAG, "onSubscribeTerminated reason : " + str2 + ", retryAfterMilliseconds : " + j + ", subscriptionId : " + str);
            RcsCapabilityExchangeImplBase.SubscribeResponseCallback subscribeResponseCallback = mSubscribeResponseCallback.get(str);
            if (subscribeResponseCallback != null) {
                try {
                    subscribeResponseCallback.onTerminated(str2, j);
                } catch (ImsException e) {
                    e.printStackTrace();
                }
            }
        }
    }
}
