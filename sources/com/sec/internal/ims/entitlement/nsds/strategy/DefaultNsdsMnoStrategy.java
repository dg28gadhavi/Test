package com.sec.internal.ims.entitlement.nsds.strategy;

import android.content.Context;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.os.SemSystemProperties;
import android.provider.Settings;
import android.text.TextUtils;
import com.sec.internal.constants.Mno;
import com.sec.internal.constants.ims.ImsConstants;
import com.sec.internal.constants.ims.VowifiConfig;
import com.sec.internal.constants.ims.entitilement.NSDSContractExt;
import com.sec.internal.constants.ims.entitilement.NSDSNamespaces;
import com.sec.internal.constants.ims.entitilement.data.LocAndTcWebSheetData;
import com.sec.internal.constants.ims.settings.GlobalSettingsConstants;
import com.sec.internal.helper.OmcCode;
import com.sec.internal.helper.SimUtil;
import com.sec.internal.helper.os.DeviceUtil;
import com.sec.internal.helper.os.ITelephonyManager;
import com.sec.internal.helper.os.TelephonyManagerWrapper;
import com.sec.internal.ims.cmstore.MStoreDebugTool;
import com.sec.internal.ims.core.cmc.CmcConstants;
import com.sec.internal.ims.entitlement.nsds.NSDSSimEventManager;
import com.sec.internal.ims.entitlement.nsds.app.flow.attwfcentitlement.AttSimSwapFlow;
import com.sec.internal.ims.entitlement.nsds.app.flow.attwfcentitlement.EntitlementAndE911AidCheckFlow;
import com.sec.internal.ims.entitlement.nsds.app.flow.ericssonnsds.SIMDeviceDeactivationFlow;
import com.sec.internal.ims.entitlement.nsds.app.flow.ericssonnsds.SIMDeviceImplicitActivation;
import com.sec.internal.ims.entitlement.nsds.app.flow.ericssonnsds.SimSwapFlow;
import com.sec.internal.ims.entitlement.nsds.app.flow.xaawfcentitlement.XaaEntitlementCheckFlow;
import com.sec.internal.ims.entitlement.nsds.app.flow.xaawfcentitlement.XaaSimDeviceImplicitActivation;
import com.sec.internal.ims.entitlement.nsds.ericssonnsds.flow.BaseFlowImpl;
import com.sec.internal.ims.entitlement.nsds.strategy.operation.ATTWfcEntitlementOperation;
import com.sec.internal.ims.entitlement.nsds.strategy.operation.DefaultNsdsOperation;
import com.sec.internal.ims.entitlement.nsds.strategy.operation.XAAEntitlementOperation;
import com.sec.internal.ims.entitlement.storagehelper.NSDSDatabaseHelper;
import com.sec.internal.ims.entitlement.storagehelper.NSDSSharedPrefHelper;
import com.sec.internal.ims.entitlement.util.EntFeatureDetector;
import com.sec.internal.ims.entitlement.util.NSDSConfigHelper;
import com.sec.internal.ims.registry.ImsRegistry;
import com.sec.internal.interfaces.ims.entitlement.nsds.IEntitlementCheck;
import com.sec.internal.interfaces.ims.entitlement.nsds.ISIMDeviceDeactivation;
import com.sec.internal.interfaces.ims.entitlement.nsds.ISIMDeviceImplicitActivation;
import com.sec.internal.interfaces.ims.entitlement.nsds.ISimSwapFlow;
import com.sec.internal.log.IMSLog;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;

public class DefaultNsdsMnoStrategy implements IMnoNsdsStrategy {
    public static final long ATT_NETWORK_ERROR_RETRY_INTERVAL = 30000;
    private static final String DEFAULT_URL_ENTITLEMENT_SERVER = "http://ses.ericsson-magic.net:10080/generic_devices";
    private static final String LOG_TAG = "DefaultNsdsMnoStrategy";
    public static final long XAA_NETWORK_ERROR_RETRY_INTERVAL = 30000;
    protected Context mContext;
    protected NsdsStrategyType mStrategyType = NsdsStrategyType.DEFAULT;
    protected final Map<String, Integer> sMapEntitlementServices;

    public final String getNSDSApiVersion() {
        return "1.0";
    }

    public final boolean isSIMDeviceActivationRequired() {
        return true;
    }

    public DefaultNsdsMnoStrategy(Context context) {
        HashMap hashMap = new HashMap();
        this.sMapEntitlementServices = hashMap;
        this.mContext = context;
        hashMap.put("vowifi", 1);
    }

    public Map<String, Integer> getEntitlementServicesMap() {
        return this.sMapEntitlementServices;
    }

    public String getEntitlementServerUrl(String str, String str2) {
        if (this.mStrategyType.isOneOf(NsdsStrategyType.ATT)) {
            NSDSSimEventManager.getInstance().getSimManager(str);
            String entitlementServerUrl = NSDSSharedPrefHelper.getEntitlementServerUrl(this.mContext, str2, "https://sentitlement2.mobile.att.net/WFC");
            if (TextUtils.isEmpty(entitlementServerUrl) || !entitlementServerUrl.contains("t-mobile")) {
                return entitlementServerUrl;
            }
            NSDSSharedPrefHelper.clearEntitlementServerUrl(this.mContext, str2);
            return "https://sentitlement2.mobile.att.net/WFC";
        } else if (this.mStrategyType.isOneOf(NsdsStrategyType.TMOUS)) {
            String str3 = null;
            String entitlementServerUrl2 = NSDSSharedPrefHelper.getEntitlementServerUrl(this.mContext, str2, (String) null);
            if (TextUtils.isEmpty(entitlementServerUrl2) || !entitlementServerUrl2.contains("att")) {
                str3 = entitlementServerUrl2;
            } else {
                NSDSSharedPrefHelper.clearEntitlementServerUrl(this.mContext, str2);
            }
            String str4 = LOG_TAG;
            IMSLog.i(str4, "getEntitlementServerUrl: url in sp " + str3);
            return str3 == null ? NSDSConfigHelper.getConfigValue(this.mContext, str, NSDSConfigHelper.KEY_URL_ENTITLEMENT_SERVER, MStoreDebugTool.DEFAULT_PRO_ENTITLEMENT) : str3;
        } else if (this.mStrategyType.isOneOf(NsdsStrategyType.XAA)) {
            return NSDSSharedPrefHelper.getEntitlementServerUrl(this.mContext, str2, "https://ses.epdg.gci.net/generic_devices");
        } else {
            return NSDSSharedPrefHelper.getEntitlementServerUrl(this.mContext, str2, DEFAULT_URL_ENTITLEMENT_SERVER);
        }
    }

    public final boolean supportEntitlementCheck() {
        return this.mStrategyType.isOneOf(NsdsStrategyType.ATT, NsdsStrategyType.XAA);
    }

    public final boolean needGetMSISDNForEntitlement() {
        return this.mStrategyType.isOneOf(NsdsStrategyType.XAA);
    }

    public final boolean isNsdsUIAppSwitchOn(String str, int i) {
        if (this.mStrategyType.isOneOf(NsdsStrategyType.TMOUS)) {
            return true;
        }
        if (!this.mStrategyType.isOneOf(NsdsStrategyType.XAA, NsdsStrategyType.ATT)) {
            return false;
        }
        String str2 = NSDSSharedPrefHelper.get(this.mContext, str, NSDSNamespaces.NSDSSharedPref.PREF_AUTO_ACTIVATE_AFTER_OOS);
        if (!EntFeatureDetector.checkWFCAutoOnEnabled(i) || str2 == null || "completed".equals(str2)) {
            boolean isEnabled = VowifiConfig.isEnabled(this.mContext, i);
            String str3 = LOG_TAG;
            IMSLog.i(str3, "isNsdsUIAppSwitchOn: WFC switch [" + isEnabled + "]");
            return isEnabled;
        }
        IMSLog.i(LOG_TAG, "[ATT_AutoOn] isNsdsUIAppSwitchOn: In process autoOn ");
        return true;
    }

    public final long calEntitlementCheckExpRetryTime(int i) {
        if (!this.mStrategyType.isOneOf(NsdsStrategyType.ATT)) {
            return 0;
        }
        long[] jArr = {1800, 1800};
        if (i <= 2) {
            return jArr[i - 1] * 1000;
        }
        IMSLog.i(LOG_TAG, "calEntitlementCheckExpRetryTime: retry exceeded max tries");
        return 0;
    }

    public final long getEntitlementCheckExpirationTime() {
        return this.mStrategyType.isOneOf(NsdsStrategyType.ATT, NsdsStrategyType.XAA) ? 86400000 : 0;
    }

    public final boolean shouldChangedUriTriggerNsdsService(Uri uri) {
        if (this.mStrategyType.isOneOf(NsdsStrategyType.TMOUS)) {
            return NSDSContractExt.NsdsConfigs.CONTENT_URI.equals(uri);
        }
        if (!this.mStrategyType.isOneOf(NsdsStrategyType.ATT, NsdsStrategyType.XAA)) {
            return false;
        }
        if (NSDSContractExt.DeviceConfig.CONTENT_URI.equals(uri) || NSDSContractExt.NsdsConfigs.CONTENT_URI.equals(uri)) {
            return true;
        }
        return false;
    }

    public final boolean shouldIgnoreDeviceConfigValidity() {
        return !this.mStrategyType.isOneOf(NsdsStrategyType.TMOUS);
    }

    public final long getRetryInterval() {
        return this.mStrategyType.isOneOf(NsdsStrategyType.ATT) ? 30000 : 0;
    }

    public final boolean isNsdsServiceEnabled() {
        return this.mStrategyType.isOneOf(NsdsStrategyType.ATT, NsdsStrategyType.XAA);
    }

    public final String getSimAuthenticationType() {
        return this.mStrategyType.isOneOf(NsdsStrategyType.ATT) ? NSDSNamespaces.NSDSSimAuthType.USIM : NSDSNamespaces.NSDSSimAuthType.UNKNOWN;
    }

    public final String getGcmSenderId(String str, String str2) {
        if (this.mStrategyType.isOneOf(NsdsStrategyType.TMOUS)) {
            return NSDSConfigHelper.getConfigValue(this.mContext, str2, NSDSConfigHelper.KEY_GCM_PUSH_MSG_SENDER_ID);
        }
        if (!this.mStrategyType.isOneOf(NsdsStrategyType.ATT)) {
            return null;
        }
        String configValue = NSDSConfigHelper.getConfigValue(this.mContext, str2, NSDSConfigHelper.KEY_GCM_EVT_LST_MSG_SENDER_ID);
        return configValue == null ? NSDSSharedPrefHelper.getGcmSenderId(this.mContext, str, "418816648224") : configValue;
    }

    public final String getDeviceGroup(int i, String str) {
        boolean isOneOf = this.mStrategyType.isOneOf(NsdsStrategyType.TMOUS);
        String str2 = NSDSNamespaces.NSDSSettings.DEVICE_GROUP_20;
        if (isOneOf) {
            boolean z = ImsRegistry.getBoolean(i, GlobalSettingsConstants.Entitlement.DEVICE_GROUP, false);
            OmcCode.get();
            if (DeviceUtil.isTablet() && DeviceUtil.isSupport5G(this.mContext)) {
                z = true;
            }
            if (z && Mno.Country.US.getCountryIso().equalsIgnoreCase(SemSystemProperties.getCountryIso())) {
                String str3 = Build.MODEL;
                String channelName = getChannelName(i, str, str3);
                if (str3.contains(CmcConstants.E_NUM_SLOT_SPLIT)) {
                    String[] split = str3.split(CmcConstants.E_NUM_SLOT_SPLIT);
                    if (!TextUtils.isEmpty(split[1])) {
                        str3 = split[1];
                    }
                }
                str2 = NSDSNamespaces.NSDSSettings.DEVICE_GROUP + '-' + str3 + '-' + channelName + '-' + Build.VERSION.INCREMENTAL;
            }
            IMSLog.s(LOG_TAG, "getDeviceGroup: " + str2);
        }
        return str2;
    }

    private String getChannelName(int i, String str, String str2) {
        String str3;
        String str4 = SemSystemProperties.get("ro.simbased.changetype", "");
        if (SemSystemProperties.getInt(ImsConstants.SystemProperties.FIRST_API_VERSION, 0) >= 33 || !str4.contains("SED") || str2.contains("G98")) {
            ITelephonyManager instance = TelephonyManagerWrapper.getInstance(this.mContext);
            String groupIdLevel1 = instance != null ? instance.getGroupIdLevel1(SimUtil.getSubId(i)) : "-1";
            String str5 = LOG_TAG;
            IMSLog.i(str5, "getChannelName - gid1: " + groupIdLevel1);
            if (str != null && (str.contains("DSH") || str.contains("Boost"))) {
                str3 = NSDSNamespaces.NSDSSettings.CHANNEL_NAME_DISH;
            } else if ("6D38".equalsIgnoreCase(groupIdLevel1)) {
                str3 = ImsRegistry.getString(i, GlobalSettingsConstants.Entitlement.CHANNEL_NAME, "");
                if (TextUtils.isEmpty(str3)) {
                    str3 = NSDSNamespaces.NSDSSettings.CHANNEL_NAME_TMK;
                }
            } else {
                str3 = "1A53".equalsIgnoreCase(groupIdLevel1) ? NSDSNamespaces.NSDSSettings.CHANNEL_NAME_ASW : NSDSNamespaces.NSDSSettings.CHANNEL_NAME_TMO;
            }
        } else {
            str3 = NSDSNamespaces.NSDSSettings.CHANNEL_NAME_SE_DEVICE;
        }
        String str6 = LOG_TAG;
        IMSLog.i(str6, "getChannelName : " + str3);
        return str3;
    }

    public final int getNextOperation(int i, int i2, int i3, Bundle bundle) {
        if (this.mStrategyType.isOneOf(NsdsStrategyType.XAA)) {
            return XAAEntitlementOperation.getOperation(i, i2, i3, bundle);
        }
        if (this.mStrategyType.isOneOf(NsdsStrategyType.ATT)) {
            return ATTWfcEntitlementOperation.getOperation(i, i2, i3, bundle);
        }
        return DefaultNsdsOperation.getOperation(i, i2, i3, bundle);
    }

    public final List<String> getServiceListForPushToken() {
        ArrayList arrayList = new ArrayList();
        if (this.mStrategyType.isOneOf(NsdsStrategyType.XAA)) {
            arrayList.add("vowifi");
        } else if (this.mStrategyType.isOneOf(NsdsStrategyType.ATT)) {
            arrayList.add(NSDSNamespaces.NSDSServices.SERVICE_VOWIFI_AND_VVM);
        } else {
            arrayList.add(NSDSNamespaces.NSDSServices.SERVICE_CONNECTIVITY_MANAGER);
            arrayList.add("vowifi");
        }
        return arrayList;
    }

    public final int getEntitlementCheckMaxRetry() {
        return this.mStrategyType.isOneOf(NsdsStrategyType.ATT, NsdsStrategyType.XAA) ? 2 : 0;
    }

    public final LocAndTcWebSheetData getLocAndTcWebSheetData(String str, String str2) {
        String str3;
        String str4;
        String str5 = LOG_TAG;
        IMSLog.i(str5, "LocAndTcWebSheetData: url-" + str + ", data-" + str2);
        if (str == null || str2 == null) {
            return null;
        }
        String str6 = "Wi-Fi Calling";
        if (this.mStrategyType.isOneOf(NsdsStrategyType.ATT)) {
            str4 = "WiFiCallingWebViewController";
            str3 = str6;
        } else {
            str4 = "NsdsWebSheetController";
            str3 = "Location and TC";
        }
        if (!this.mStrategyType.isOneOf(NsdsStrategyType.XAA)) {
            str6 = str3;
        }
        return new LocAndTcWebSheetData(str, str2, str6, str4);
    }

    /* JADX WARNING: Code restructure failed: missing block: B:11:0x002f, code lost:
        if (com.sec.internal.constants.Mno.TMOUS != r5) goto L_0x0019;
     */
    /* JADX WARNING: Code restructure failed: missing block: B:15:0x0042, code lost:
        if (com.sec.internal.constants.Mno.ATT == r5) goto L_0x0018;
     */
    /* JADX WARNING: Code restructure failed: missing block: B:3:0x0016, code lost:
        if (com.sec.internal.constants.Mno.GCI == r5) goto L_0x0018;
     */
    /* Code decompiled incorrectly, please refer to instructions dump. */
    public final boolean isSimSupportedForNsds(com.sec.internal.interfaces.ims.core.ISimManager r5) {
        /*
            r4 = this;
            com.sec.internal.constants.Mno r5 = r5.getSimMno()
            com.sec.internal.ims.entitlement.nsds.strategy.DefaultNsdsMnoStrategy$NsdsStrategyType r0 = r4.mStrategyType
            com.sec.internal.ims.entitlement.nsds.strategy.DefaultNsdsMnoStrategy$NsdsStrategyType r1 = com.sec.internal.ims.entitlement.nsds.strategy.DefaultNsdsMnoStrategy.NsdsStrategyType.XAA
            com.sec.internal.ims.entitlement.nsds.strategy.DefaultNsdsMnoStrategy$NsdsStrategyType[] r1 = new com.sec.internal.ims.entitlement.nsds.strategy.DefaultNsdsMnoStrategy.NsdsStrategyType[]{r1}
            boolean r0 = r0.isOneOf(r1)
            r1 = 0
            r2 = 1
            if (r0 == 0) goto L_0x001b
            com.sec.internal.constants.Mno r4 = com.sec.internal.constants.Mno.GCI
            if (r4 != r5) goto L_0x0019
        L_0x0018:
            r1 = r2
        L_0x0019:
            r2 = r1
            goto L_0x0045
        L_0x001b:
            com.sec.internal.ims.entitlement.nsds.strategy.DefaultNsdsMnoStrategy$NsdsStrategyType r0 = r4.mStrategyType
            com.sec.internal.ims.entitlement.nsds.strategy.DefaultNsdsMnoStrategy$NsdsStrategyType r3 = com.sec.internal.ims.entitlement.nsds.strategy.DefaultNsdsMnoStrategy.NsdsStrategyType.TMOUS
            com.sec.internal.ims.entitlement.nsds.strategy.DefaultNsdsMnoStrategy$NsdsStrategyType[] r3 = new com.sec.internal.ims.entitlement.nsds.strategy.DefaultNsdsMnoStrategy.NsdsStrategyType[]{r3}
            boolean r0 = r0.isOneOf(r3)
            if (r0 == 0) goto L_0x0032
            com.sec.internal.constants.Mno r4 = com.sec.internal.constants.Mno.TMOBILE
            if (r4 == r5) goto L_0x0018
            com.sec.internal.constants.Mno r4 = com.sec.internal.constants.Mno.TMOUS
            if (r4 != r5) goto L_0x0019
            goto L_0x0018
        L_0x0032:
            com.sec.internal.ims.entitlement.nsds.strategy.DefaultNsdsMnoStrategy$NsdsStrategyType r4 = r4.mStrategyType
            com.sec.internal.ims.entitlement.nsds.strategy.DefaultNsdsMnoStrategy$NsdsStrategyType r0 = com.sec.internal.ims.entitlement.nsds.strategy.DefaultNsdsMnoStrategy.NsdsStrategyType.ATT
            com.sec.internal.ims.entitlement.nsds.strategy.DefaultNsdsMnoStrategy$NsdsStrategyType[] r0 = new com.sec.internal.ims.entitlement.nsds.strategy.DefaultNsdsMnoStrategy.NsdsStrategyType[]{r0}
            boolean r4 = r4.isOneOf(r0)
            if (r4 == 0) goto L_0x0045
            com.sec.internal.constants.Mno r4 = com.sec.internal.constants.Mno.ATT
            if (r4 != r5) goto L_0x0019
            goto L_0x0018
        L_0x0045:
            java.lang.String r4 = LOG_TAG
            java.lang.StringBuilder r5 = new java.lang.StringBuilder
            r5.<init>()
            java.lang.String r0 = "isSimSupportedForNsds: "
            r5.append(r0)
            r5.append(r2)
            java.lang.String r5 = r5.toString()
            com.sec.internal.log.IMSLog.i(r4, r5)
            return r2
        */
        throw new UnsupportedOperationException("Method not decompiled: com.sec.internal.ims.entitlement.nsds.strategy.DefaultNsdsMnoStrategy.isSimSupportedForNsds(com.sec.internal.interfaces.ims.core.ISimManager):boolean");
    }

    public final String[] getNSDSServices() {
        if (this.mStrategyType.isOneOf(NsdsStrategyType.XAA)) {
            return new String[]{"vowifi"};
        }
        if (this.mStrategyType.isOneOf(NsdsStrategyType.ATT)) {
            return new String[]{NSDSNamespaces.NSDSServices.SERVICE_VOWIFI_AND_VVM};
        }
        return null;
    }

    public final String getUserAgent() {
        if (!this.mStrategyType.isOneOf(NsdsStrategyType.TMOUS)) {
            return null;
        }
        String str = "TMK".equals(OmcCode.get()) ? "Metro" : "T-Mobile";
        return str + ' ' + "UP2 VVM" + ' ' + Build.VERSION.INCREMENTAL + ' ' + new SimpleDateFormat("yyyy-MM-dd", Locale.US).format(new Date()) + ' ' + NSDSNamespaces.NSDSSettings.OS + ' ' + Build.VERSION.RELEASE + ' ' + Build.MODEL;
    }

    public final IEntitlementCheck getEntitlementCheckImpl(Looper looper, Context context, BaseFlowImpl baseFlowImpl, NSDSDatabaseHelper nSDSDatabaseHelper, Handler handler) {
        if (this.mStrategyType.isOneOf(NsdsStrategyType.XAA)) {
            return new XaaEntitlementCheckFlow(looper, context, baseFlowImpl, nSDSDatabaseHelper);
        }
        if (this.mStrategyType.isOneOf(NsdsStrategyType.ATT)) {
            return new EntitlementAndE911AidCheckFlow(looper, context, baseFlowImpl, nSDSDatabaseHelper);
        }
        return null;
    }

    public final ISIMDeviceImplicitActivation getSimDeviceActivationImpl(Looper looper, Context context, BaseFlowImpl baseFlowImpl, NSDSDatabaseHelper nSDSDatabaseHelper) {
        if (this.mStrategyType.isOneOf(NsdsStrategyType.XAA)) {
            return new XaaSimDeviceImplicitActivation(looper, context, baseFlowImpl, nSDSDatabaseHelper);
        }
        return new SIMDeviceImplicitActivation(looper, context, baseFlowImpl, nSDSDatabaseHelper);
    }

    public final ISIMDeviceDeactivation getSimDeviceDeactivationImpl(Looper looper, Context context, BaseFlowImpl baseFlowImpl, NSDSDatabaseHelper nSDSDatabaseHelper) {
        return new SIMDeviceDeactivationFlow(looper, context, baseFlowImpl, nSDSDatabaseHelper);
    }

    public final ISimSwapFlow getSimSwapFlow(Looper looper, Context context, BaseFlowImpl baseFlowImpl, NSDSDatabaseHelper nSDSDatabaseHelper) {
        if (this.mStrategyType.isOneOf(NsdsStrategyType.ATT)) {
            return new AttSimSwapFlow(looper, context, baseFlowImpl, nSDSDatabaseHelper);
        }
        return new SimSwapFlow(looper, context, baseFlowImpl, nSDSDatabaseHelper);
    }

    public final int getBaseOperationMaxRetry() {
        return this.mStrategyType.isOneOf(NsdsStrategyType.ATT, NsdsStrategyType.XAA) ? 0 : 4;
    }

    public final boolean requireRetryBootupProcedure() {
        return this.mStrategyType.isOneOf(NsdsStrategyType.TMOUS);
    }

    public final boolean isGcmTokenRequired() {
        return this.mStrategyType.isOneOf(NsdsStrategyType.ATT, NsdsStrategyType.TMOUS);
    }

    public final long getWaitTimeForForcedSimSwap() {
        return this.mStrategyType.isOneOf(NsdsStrategyType.ATT, NsdsStrategyType.TMOUS) ? 10000 : 0;
    }

    public final List<Integer> getOperationsForBootupInit(String str, int i) {
        ArrayList arrayList = new ArrayList();
        if (this.mStrategyType.isOneOf(NsdsStrategyType.ATT)) {
            arrayList.add(43);
            String str2 = NSDSSharedPrefHelper.get(this.mContext, str, NSDSNamespaces.NSDSSharedPref.PREF_AUTO_ACTIVATE_AFTER_OOS);
            if (!EntFeatureDetector.checkWFCAutoOnEnabled(i) || (!TextUtils.isEmpty(str2) && TextUtils.equals("completed", str2))) {
                IMSLog.i(LOG_TAG, "[ATT_AutoOn] getOperationsForBootupInit: already started");
                if (isNsdsUIAppSwitchOn(str, i) && NSDSSharedPrefHelper.isDeviceActivated(this.mContext, str)) {
                    arrayList.add(44);
                }
            } else {
                NSDSSharedPrefHelper.save(this.mContext, str, NSDSNamespaces.NSDSSharedPref.PREF_AUTO_ACTIVATE_AFTER_OOS, NSDSNamespaces.VowifiAutoOnOperation.AUTOON_IN_PROGRESS);
                IMSLog.i(LOG_TAG, "[ATT_AutoOn] getOperationsForBootupInit: add EVT_REFRESH_ENTITLEMENT_AND_911_AID");
                arrayList.add(51);
            }
        }
        return arrayList;
    }

    public final boolean isDeviceProvisioned() {
        boolean z = true;
        if (this.mStrategyType.isOneOf(NsdsStrategyType.ATT)) {
            return true;
        }
        if (Settings.Global.getInt(this.mContext.getContentResolver(), "device_provisioned", 0) == 0) {
            z = false;
        }
        IMSLog.i(LOG_TAG, "isDeviceProvisioned: " + z);
        return z;
    }

    public final long getDeviceInfoTime() {
        return this.mStrategyType.isOneOf(NsdsStrategyType.TMOUS) ? 172800000 : 0;
    }

    protected enum NsdsStrategyType {
        DEFAULT,
        ATT,
        TMOUS,
        XAA,
        END_OF_NSDSSTRATEGY;

        /* access modifiers changed from: protected */
        public boolean isOneOf(NsdsStrategyType... nsdsStrategyTypeArr) {
            for (NsdsStrategyType nsdsStrategyType : nsdsStrategyTypeArr) {
                if (this == nsdsStrategyType) {
                    return true;
                }
            }
            return false;
        }
    }
}
