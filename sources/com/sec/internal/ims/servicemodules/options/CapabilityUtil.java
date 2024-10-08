package com.sec.internal.ims.servicemodules.options;

import android.content.ContentValues;
import android.content.Context;
import android.content.SharedPreferences;
import android.os.SystemProperties;
import android.telephony.ims.ImsException;
import android.telephony.ims.stub.RcsCapabilityExchangeImplBase;
import android.text.TextUtils;
import android.util.Log;
import com.google.i18n.phonenumbers.NumberParseException;
import com.google.i18n.phonenumbers.PhoneNumberUtil;
import com.google.i18n.phonenumbers.Phonenumber$PhoneNumber;
import com.sec.ims.ImsRegistration;
import com.sec.ims.extensions.Extensions;
import com.sec.ims.extensions.TelephonyManagerExt;
import com.sec.ims.options.Capabilities;
import com.sec.ims.settings.ImsProfile;
import com.sec.ims.util.ImsUri;
import com.sec.internal.constants.Mno;
import com.sec.internal.constants.ims.DiagnosisConstants;
import com.sec.internal.constants.ims.SipMsg;
import com.sec.internal.constants.ims.config.ConfigConstants;
import com.sec.internal.constants.ims.os.NetworkEvent;
import com.sec.internal.helper.DmConfigHelper;
import com.sec.internal.helper.NetworkUtil;
import com.sec.internal.helper.SimUtil;
import com.sec.internal.helper.SimpleEventLog;
import com.sec.internal.helper.UriUtil;
import com.sec.internal.helper.os.ImsGateConfig;
import com.sec.internal.ims.core.sim.SimManagerFactory;
import com.sec.internal.ims.diagnosis.ImsLogAgentUtil;
import com.sec.internal.ims.rcs.RcsPolicyManager;
import com.sec.internal.ims.rcs.util.RcsUtils;
import com.sec.internal.ims.registry.ImsRegistry;
import com.sec.internal.ims.servicemodules.im.strategy.IMnoStrategy;
import com.sec.internal.ims.settings.DeviceConfigManager;
import com.sec.internal.ims.settings.ServiceConstants;
import com.sec.internal.ims.util.ChatbotUriUtil;
import com.sec.internal.ims.util.ConfigUtil;
import com.sec.internal.ims.util.UriGenerator;
import com.sec.internal.imscr.LogClass;
import com.sec.internal.interfaces.ims.core.IRegistrationManager;
import com.sec.internal.interfaces.ims.core.ISimManager;
import com.sec.internal.interfaces.ims.servicemodules.presence.IPresenceModule;
import com.sec.internal.log.IMSLog;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Set;
import java.util.regex.Pattern;

public class CapabilityUtil {
    private static final String LOG_TAG = "CapabilityUtil";
    protected CapabilityDiscoveryModule mCapabilityDiscovery;
    private SimpleEventLog mEventLog;
    protected IPresenceModule mPresenceModule = ImsRegistry.getServiceModuleManager().getPresenceModule();

    public static boolean hasFeature(long j, long j2) {
        return (j & j2) == j2;
    }

    CapabilityUtil(CapabilityDiscoveryModule capabilityDiscoveryModule, SimpleEventLog simpleEventLog) {
        this.mCapabilityDiscovery = capabilityDiscoveryModule;
        this.mEventLog = simpleEventLog;
    }

    /* access modifiers changed from: package-private */
    public boolean isCheckRcsSwitch(Context context) {
        boolean z = false;
        for (ISimManager simSlotIndex : SimManagerFactory.getAllSimManagers()) {
            boolean z2 = true;
            if (DmConfigHelper.getImsSwitchValue(context, DeviceConfigManager.RCS_SWITCH, simSlotIndex.getSimSlotIndex()) != 1) {
                z2 = false;
            }
            z |= z2;
        }
        return z;
    }

    /* access modifiers changed from: package-private */
    public boolean isCapabilityDiscoveryDisabled(Context context, int i) {
        boolean z = DmConfigHelper.getImsSwitchValue(context, "options", i) == 1;
        boolean z2 = DmConfigHelper.getImsSwitchValue(context, SipMsg.EVENT_PRESENCE, i) == 1;
        if (z || z2) {
            return false;
        }
        return true;
    }

    /* access modifiers changed from: package-private */
    public String extractMsisdnFromUri(String str) {
        if (TextUtils.isEmpty(str)) {
            Log.e(LOG_TAG, "extractMsisdnFromUri uri is empty");
            return "";
        }
        int indexOf = str.indexOf(":");
        if (indexOf >= 0) {
            str = str.substring(indexOf + 1);
        }
        int indexOf2 = str.indexOf("@");
        return indexOf2 >= 0 ? str.substring(0, indexOf2) : str;
    }

    /* access modifiers changed from: package-private */
    public int getCapInfoExpiry(Capabilities capabilities, int i) {
        CapabilityConfig capabilityConfig = this.mCapabilityDiscovery.getCapabilityConfig(i);
        if (capabilities != null && capabilities.hasFeature(Capabilities.FEATURE_NON_RCS_USER)) {
            return capabilityConfig.getNonRCScapInfoExpiry();
        }
        if (capabilities == null || !ChatbotUriUtil.hasUriBotPlatform(capabilities.getUri(), i) || capabilities.getFeature() != ((long) Capabilities.FEATURE_OFFLINE_RCS_USER)) {
            return capabilityConfig.getCapInfoExpiry();
        }
        IMSLog.d(LOG_TAG, i, "getCapInfoExpiry: capex.uri() [" + capabilities.getUri() + "] is chatbot & offline ");
        return 0;
    }

    /* access modifiers changed from: package-private */
    public boolean isAllowedPrefixesUri(ImsUri imsUri, int i) {
        CapabilityConfig capabilityConfig = this.mCapabilityDiscovery.getCapabilityConfig(i);
        if (capabilityConfig == null) {
            return false;
        }
        String msisdnNumber = UriUtil.getMsisdnNumber(imsUri);
        if (imsUri != null && imsUri.getUriType() == ImsUri.UriType.SIP_URI && msisdnNumber == null) {
            return true;
        }
        if (SimUtil.getSimMno(i).isKor() && msisdnNumber != null && msisdnNumber.equals("+82114")) {
            return false;
        }
        Set<Pattern> capAllowedPrefixes = capabilityConfig.getCapAllowedPrefixes();
        if (capAllowedPrefixes.isEmpty()) {
            return true;
        }
        if (msisdnNumber == null) {
            return false;
        }
        for (Pattern matcher : capAllowedPrefixes) {
            if (matcher.matcher(msisdnNumber).find()) {
                return true;
            }
        }
        return false;
    }

    /* access modifiers changed from: package-private */
    public long getDelayTimeToPoll(long j, int i) {
        if (j == -1) {
            return 0;
        }
        long throttledDelay = (RcsPolicyManager.getRcsStrategy(i).getThrottledDelay((long) this.mCapabilityDiscovery.getCapabilityConfig(i).getPollListSubExpiry()) * 1000) - (new Date().getTime() - j);
        IMSLog.i(LOG_TAG, i, "getDelayTimeToPoll: delay = " + throttledDelay + ", lastListSubscribeStamp = " + j);
        if (throttledDelay > 0) {
            return throttledDelay;
        }
        return 0;
    }

    /* access modifiers changed from: package-private */
    public boolean isRegistrationSupported(ImsRegistration imsRegistration) {
        if (!imsRegistration.hasService(SipMsg.EVENT_PRESENCE) && !imsRegistration.hasService("options")) {
            Log.e(LOG_TAG, "isRegistrationSupported: no presence and options in service list");
            return false;
        } else if (RcsPolicyManager.getRcsStrategy(imsRegistration.getPhoneId()) == null) {
            Log.e(LOG_TAG, "isRegistrationSupported: getRcsStrategy is null");
            return false;
        } else if (!RcsPolicyManager.getRcsStrategy(imsRegistration.getPhoneId()).checkImsiBasedRegi(imsRegistration)) {
            return true;
        } else {
            Log.e(LOG_TAG, "isRegistrationSupported: isImsiBasedRegi is true");
            return false;
        }
    }

    /* access modifiers changed from: package-private */
    public ImsUri getNetworkPreferredUri(ImsUri imsUri) {
        CapabilitiesCache capabilitiesCache = this.mCapabilityDiscovery.getCapabilitiesCache();
        String str = null;
        Capabilities capabilities = capabilitiesCache != null ? capabilitiesCache.get(imsUri) : null;
        if (capabilities == null) {
            return null;
        }
        Iterator it = capabilities.getPAssertedId().iterator();
        while (true) {
            if (!it.hasNext()) {
                break;
            }
            ImsUri imsUri2 = (ImsUri) it.next();
            if (imsUri2.getUriType() == ImsUri.UriType.SIP_URI) {
                str = imsUri2.getHost();
                break;
            }
        }
        return this.mCapabilityDiscovery.getUriGenerator().getNetworkPreferredUri(UriGenerator.URIServiceType.RCS_URI, imsUri, str);
    }

    /* access modifiers changed from: package-private */
    public boolean isCapabilityCacheEmpty(int i) {
        for (Capabilities contactId : this.mCapabilityDiscovery.getCapabilitiesCache(i).getCapabilitiesCache()) {
            if (contactId.getContactId() != null) {
                return false;
            }
        }
        return true;
    }

    /* access modifiers changed from: package-private */
    public void changeParalysed(boolean z, int i) {
        IMSLog.i(LOG_TAG, i, "changeParalysed");
        if (this.mPresenceModule.getParalysed(i) != z) {
            this.mPresenceModule.setParalysed(z, i);
            CapabilityConfig capabilityConfig = this.mCapabilityDiscovery.getCapabilityConfig(i);
            if (z && capabilityConfig != null && capabilityConfig.usePresence()) {
                Log.i(LOG_TAG, "call unpublish");
                this.mPresenceModule.unpublish(i);
            }
            if (!z && isCapabilityCacheEmpty(i)) {
                this.mCapabilityDiscovery.onContactChanged(true);
            }
        }
    }

    /* access modifiers changed from: package-private */
    public void handleRemovedNumbers(int i) {
        List<String> andFlushRemovedNumbers = this.mCapabilityDiscovery.getPhonebook().getAndFlushRemovedNumbers();
        IMSLog.s(LOG_TAG, i, "handleRemovedNumbers: removed numbers " + andFlushRemovedNumbers);
        ArrayList arrayList = new ArrayList();
        for (String normalizedUri : andFlushRemovedNumbers) {
            ImsUri normalizedUri2 = this.mCapabilityDiscovery.getUriGenerator().getNormalizedUri(normalizedUri, true);
            if (normalizedUri2 != null) {
                arrayList.add(normalizedUri2);
                if (this.mCapabilityDiscovery.updatePollList(normalizedUri2, false, i)) {
                    IMSLog.s(LOG_TAG, i, "handleRemovedNumbers: updatePollList, removed uri = " + normalizedUri2);
                }
            }
        }
        if (arrayList.size() > 0) {
            this.mCapabilityDiscovery.getCapabilitiesCache(i).remove(arrayList);
            CapabilityConfig capabilityConfig = this.mCapabilityDiscovery.getCapabilityConfig(i);
            if (capabilityConfig != null && capabilityConfig.usePresence()) {
                this.mPresenceModule.removePresenceCache(arrayList, i);
            }
        }
    }

    /* access modifiers changed from: package-private */
    public long filterFeaturesWithService(long j, Set<String> set, int i, int i2) {
        if (set == null) {
            return j;
        }
        IMSLog.s(LOG_TAG, i2, "filterFeaturesWithService: features=" + Long.toHexString(j) + ", services=" + set + ", networkType=" + i);
        return j & (checkRcsFeatures(set, i, i2) | checkChatFeatures(set) | 0 | checkCshFeatures(set));
    }

    private long checkChatFeatures(Set<String> set) {
        long j = 0;
        if (set.contains(ServiceConstants.SERVICE_CHATBOT_COMMUNICATION)) {
            j = 0 | Capabilities.FEATURE_CHATBOT_CHAT_SESSION | Capabilities.FEATURE_CHATBOT_STANDALONE_MSG | Capabilities.FEATURE_CHATBOT_EXTENDED_MSG;
        }
        if (set.contains("ft_http")) {
            j |= (long) (Capabilities.FEATURE_FT_HTTP | Capabilities.FEATURE_FT_VIA_SMS);
        }
        if (set.contains("slm")) {
            j |= ((long) Capabilities.FEATURE_STANDALONE_MSG) | Capabilities.FEATURE_PUBLIC_MSG;
        }
        if (set.contains("im")) {
            j |= ((long) (Capabilities.FEATURE_CHAT_CPM | Capabilities.FEATURE_CHAT_SIMPLE_IM | Capabilities.FEATURE_INTEGRATED_MSG | Capabilities.FEATURE_SF_GROUP_CHAT | Capabilities.FEATURE_STICKER)) | Capabilities.FEATURE_CANCEL_MESSAGE;
        }
        if (set.contains("ft")) {
            j |= (long) (Capabilities.FEATURE_FT | Capabilities.FEATURE_FT_STORE | Capabilities.FEATURE_FT_THUMBNAIL | Capabilities.FEATURE_FT_VIA_SMS);
        }
        return set.contains("plug-in") ? j | Capabilities.FEATURE_PLUG_IN : j;
    }

    private long checkCshFeatures(Set<String> set) {
        long j = 0;
        if (set.contains("is")) {
            j = 0 | ((long) Capabilities.FEATURE_ISH);
        }
        if (set.contains("vs")) {
            j |= (long) Capabilities.FEATURE_VSH;
        }
        if (set.contains("gls")) {
            j |= (long) (Capabilities.FEATURE_GEOLOCATION_PULL | Capabilities.FEATURE_GEOLOCATION_PULL_FT | Capabilities.FEATURE_GEOLOCATION_PUSH | Capabilities.FEATURE_GEO_VIA_SMS);
        }
        return set.contains("ec") ? j | Capabilities.FEATURE_ENRICHED_CALL_COMPOSER | Capabilities.FEATURE_ENRICHED_SHARED_MAP | Capabilities.FEATURE_ENRICHED_SHARED_SKETCH | Capabilities.FEATURE_ENRICHED_POST_CALL : j;
    }

    private long checkRcsFeatures(Set<String> set, int i, int i2) {
        long j = 0;
        if (set.contains(SipMsg.EVENT_PRESENCE)) {
            j = 0 | ((long) (Capabilities.FEATURE_PRESENCE_DISCOVERY | Capabilities.FEATURE_SOCIAL_PRESENCE));
        }
        if (set.contains("lastseen")) {
            j |= Capabilities.FEATURE_LAST_SEEN_ACTIVE;
        }
        if (set.contains("mmtel") && isMmtelServiceAvailable(i, i2)) {
            j |= (long) (Capabilities.FEATURE_MMTEL | Capabilities.FEATURE_IPCALL);
        }
        ISimManager simManagerFromSimSlot = SimManagerFactory.getSimManagerFromSimSlot(i2);
        if (set.contains("mmtel-video") && ((simManagerFromSimSlot != null && simManagerFromSimSlot.getSimMno().isKor()) || isMmtelServiceAvailable(i, i2))) {
            j |= (long) (Capabilities.FEATURE_MMTEL_VIDEO | Capabilities.FEATURE_IPCALL_VIDEO | Capabilities.FEATURE_IPCALL_VIDEO_ONLY);
        }
        return (!isMmtelServiceAvailable(i, i2) || !set.contains("mmtel") || !set.contains("mmtel-call-composer")) ? j : j | Capabilities.FEATURE_MMTEL_CALL_COMPOSER;
    }

    /* access modifiers changed from: package-private */
    public long filterFeaturesWithCallState(long j, boolean z, String str) {
        if (z && str != null) {
            return j;
        }
        Log.i(LOG_TAG, "filterFeaturesWithCallState: disable ISH, VSH, ShareMap and ShareSketch");
        return j & ((long) (~Capabilities.FEATURE_VSH)) & ((long) (~Capabilities.FEATURE_ISH)) & (~Capabilities.FEATURE_ENRICHED_SHARED_MAP) & (~Capabilities.FEATURE_ENRICHED_SHARED_SKETCH);
    }

    /* access modifiers changed from: package-private */
    public long filterEnrichedCallFeatures(long j) {
        Log.i(LOG_TAG, "filterEnrichedCallFeatures: disable CallComposer, PostCall, ISH, VSH, ShareMap and ShareSketch");
        return j & ((long) (~Capabilities.FEATURE_VSH)) & ((long) (~Capabilities.FEATURE_ISH)) & (~Capabilities.FEATURE_ENRICHED_SHARED_MAP) & (~Capabilities.FEATURE_ENRICHED_SHARED_SKETCH) & (~Capabilities.FEATURE_ENRICHED_CALL_COMPOSER) & (~Capabilities.FEATURE_ENRICHED_POST_CALL);
    }

    /* access modifiers changed from: package-private */
    public long filterInCallFeatures(long j, ImsUri imsUri, String str) {
        ImsUri imsUri2;
        ImsUri imsUri3;
        long j2;
        long j3;
        if (imsUri == null) {
            Log.i(LOG_TAG, "Request URI is null, return existing availFeatures");
            return j;
        }
        String msisdn = imsUri.getMsisdn();
        Log.i(LOG_TAG, "request uri[" + IMSLog.checker(msisdn) + "] callNumber[" + IMSLog.checker(str) + "]");
        if (str == null) {
            j2 = j & ((long) (~Capabilities.FEATURE_VSH)) & ((long) (~Capabilities.FEATURE_ISH)) & (~Capabilities.FEATURE_ENRICHED_SHARED_MAP);
            j3 = Capabilities.FEATURE_ENRICHED_SHARED_SKETCH;
        } else {
            UriGenerator uriGenerator = this.mCapabilityDiscovery.getUriGenerator();
            if (uriGenerator != null) {
                imsUri3 = uriGenerator.getNormalizedUri(msisdn, true);
                imsUri2 = uriGenerator.getNormalizedUri(str, true);
            } else {
                imsUri3 = null;
                imsUri2 = null;
            }
            if (imsUri3 != null) {
                imsUri = imsUri3;
            }
            Log.i(LOG_TAG, "normalizedReqUri[" + IMSLog.checker(imsUri) + "] normalizedCallNumber[" + IMSLog.checker(imsUri2) + "]");
            if (!imsUri.equals(imsUri2)) {
                Log.i(LOG_TAG, "we're not in call with " + IMSLog.checker(msisdn) + ", remove incall features");
                j2 = j & ((long) (~Capabilities.FEATURE_VSH)) & ((long) (~Capabilities.FEATURE_ISH)) & (~Capabilities.FEATURE_ENRICHED_SHARED_MAP);
                j3 = Capabilities.FEATURE_ENRICHED_SHARED_SKETCH;
            } else {
                Log.i(LOG_TAG, "we're in call with " + IMSLog.checker(msisdn) + ", don't change incall features");
                return j;
            }
        }
        return j2 & (~j3);
    }

    /* access modifiers changed from: package-private */
    public Set<String> filterServicesWithReg(Map<Integer, ImsRegistration> map, IRegistrationManager iRegistrationManager, int i, int i2) {
        if (!map.containsKey(Integer.valueOf(i2))) {
            return null;
        }
        ImsProfile imsProfile = map.get(Integer.valueOf(i2)).getImsProfile();
        int handle = map.get(Integer.valueOf(i2)).getHandle();
        Set<String> services = map.get(Integer.valueOf(i2)).getServices();
        int currentNetwork = iRegistrationManager.getCurrentNetwork(handle);
        if (!ConfigUtil.isRcsEur(SimUtil.getSimMno(i2))) {
            i = currentNetwork;
        }
        Set<String> serviceForNetwork = iRegistrationManager.getServiceForNetwork(imsProfile, i, false, i2);
        HashSet hashSet = new HashSet();
        if (serviceForNetwork != null) {
            for (String str : services) {
                if (serviceForNetwork.contains(str)) {
                    hashSet.add(str);
                }
            }
        }
        return hashSet;
    }

    /* access modifiers changed from: package-private */
    public boolean isMmtelServiceAvailable(int i, int i2) {
        Mno simMno = SimUtil.getSimMno(i2);
        if (simMno == Mno.ATT) {
            if (!NetworkUtil.is3gppPsVoiceNetwork(i) && i != 18) {
                ISimManager simManagerFromSimSlot = SimManagerFactory.getSimManagerFromSimSlot(i2);
                if (simManagerFromSimSlot == null || !simManagerFromSimSlot.hasVsim()) {
                    return false;
                }
                return true;
            }
        } else if (simMno.isKor()) {
            return NetworkUtil.is3gppPsVoiceNetwork(i);
        }
        return true;
    }

    /* access modifiers changed from: package-private */
    public boolean isPhoneLockState(Context context) {
        String str = SystemProperties.get("ro.crypto.type", "");
        String str2 = SystemProperties.get("vold.decrypt", "");
        if ("block".equals(str) && !"trigger_restart_framework".equals(str2)) {
            Log.i(LOG_TAG, "isPhoneLockState: not required sync contact in lock state");
            IMSLog.c(LogClass.CDM_BOOT_COMP, "N,LOCKED");
            return true;
        } else if (isCheckRcsSwitch(context)) {
            return false;
        } else {
            Log.i(LOG_TAG, "isPhoneLockState : rcs switch is disabled");
            return true;
        }
    }

    /* access modifiers changed from: package-private */
    public void sendGateMessage(ImsUri imsUri, long j, int i) {
        try {
            if (ImsGateConfig.isGateEnabled()) {
                IMSLog.i(LOG_TAG, i, "sendGateMessage");
                PhoneNumberUtil instance = PhoneNumberUtil.getInstance();
                Phonenumber$PhoneNumber parse = instance.parse(UriUtil.getMsisdnNumber(imsUri), "");
                String replace = instance.format(parse, PhoneNumberUtil.PhoneNumberFormat.NATIONAL).replace(" ", "");
                String format = String.format(Locale.US, "%02d", new Object[]{Integer.valueOf(parse.getCountryCode())});
                String str = "OFF";
                if (hasFeature(j, (long) Capabilities.FEATURE_CHAT_CPM) || hasFeature(j, (long) Capabilities.FEATURE_FT_SERVICE)) {
                    str = "ON";
                }
                IMSLog.g("GATE", "<GATE-M>IPME_CAPABILITY_" + str + "_+" + format + replace + "</GATE-M>");
            }
        } catch (NumberParseException unused) {
            IMSLog.s(LOG_TAG, "Failed to parse uri : " + imsUri);
        }
    }

    /* access modifiers changed from: package-private */
    public void sendRCSLInfoToHQM(Context context, boolean z, int i) {
        if (i < 0) {
            Log.e(LOG_TAG, "sendRCSLInfoToHQM : phoneId is invalid " + i);
            i = 0;
        }
        ContentValues contentValues = new ContentValues();
        contentValues.put(DiagnosisConstants.RCSL_KEY_LTCH, String.valueOf(z ^ true ? 1 : 0));
        ImsLogAgentUtil.sendLogToAgent(i, context, DiagnosisConstants.FEATURE_RCSL, contentValues);
    }

    /* access modifiers changed from: package-private */
    public void sendRCSCInfoToHQM(int i) {
        if (i < 0) {
            Log.e(LOG_TAG, "sendRCSCInfoToHQM : phoneId is invalid " + i);
            i = 0;
        }
        this.mCapabilityDiscovery.getCapabilitiesCache(i).sendRCSCInfoToHQM();
    }

    /* access modifiers changed from: package-private */
    public void onImsSettingsUpdate(Context context, int i) {
        this.mCapabilityDiscovery.removeMessages(7);
        if (this.mCapabilityDiscovery.getCapabilityControl(i) != null && this.mCapabilityDiscovery.getCapabilityControl(i) == this.mCapabilityDiscovery.getPresenceModule()) {
            if (!DmConfigHelper.readBool(context, ConfigConstants.ConfigPath.OMADM_EAB_SETTING, Boolean.FALSE, i).booleanValue()) {
                this.mCapabilityDiscovery.getCapabilityControl(i).reset(i);
                this.mCapabilityDiscovery.clearCapabilitiesCache(i);
                this.mCapabilityDiscovery.changeParalysed(true, i);
                IMnoStrategy rcsStrategy = RcsPolicyManager.getRcsStrategy(i);
                if (rcsStrategy != null) {
                    rcsStrategy.updateOmaDmNodes(i);
                    return;
                }
                return;
            } else if (!this.mCapabilityDiscovery.getPresenceModule().getBadEventProgress(i) && !this.mCapabilityDiscovery.getPresenceModule().isPublishNotFoundProgress(i)) {
                this.mCapabilityDiscovery.changeParalysed(false, i);
            }
        }
        if (this.mCapabilityDiscovery.getCapabilityConfig(i) == null || this.mCapabilityDiscovery.getCapabilityControl(i) == null) {
            Log.i(LOG_TAG, "onImsSettingsUpdate: not ready");
            return;
        }
        IMSLog.i(LOG_TAG, i, "onImsSettingsUpdate: refresh configuration");
        this.mCapabilityDiscovery.getCapabilityControl(i).readConfig(i);
        IMnoStrategy rcsStrategy2 = RcsPolicyManager.getRcsStrategy(i);
        if (rcsStrategy2 != null) {
            rcsStrategy2.updateOmaDmNodes(i);
        }
        if (this.mCapabilityDiscovery.getCapabilityConfig(i).isPollingPeriodUpdated() && this.mCapabilityDiscovery.getCapabilityControl(i).isReadyToRequest(i)) {
            CapabilityDiscoveryModule capabilityDiscoveryModule = this.mCapabilityDiscovery;
            capabilityDiscoveryModule.sendMessage(capabilityDiscoveryModule.obtainMessage(3, Integer.valueOf(i)));
        }
    }

    /* access modifiers changed from: package-private */
    public void onNetworkChanged(Context context, NetworkEvent networkEvent, int i, int i2, Map<Integer, ImsRegistration> map, NetworkEvent networkEvent2) {
        IMSLog.s(LOG_TAG, i, "onNetworkChanged: " + networkEvent);
        if (RcsUtils.DualRcs.isDualRcsReg() || i2 == i) {
            int i3 = networkEvent.network;
            if ((i3 != 0 && (networkEvent2 == null || networkEvent2.network != i3)) || networkEvent.isWifiConnected) {
                Mno simMno = SimUtil.getSimMno(i);
                if (!networkEvent.isWifiConnected || (simMno.isRjil() && networkEvent.network != 0)) {
                    this.mCapabilityDiscovery.setNetworkType(networkEvent.network, i);
                } else {
                    this.mCapabilityDiscovery.setNetworkType(18, i);
                }
                this.mCapabilityDiscovery.setNetworkEvent(networkEvent, i);
                this.mCapabilityDiscovery.setNetworkClass(TelephonyManagerExt.getNetworkClass(networkEvent.network), i);
                if (simMno == Mno.ATT) {
                    if (map.containsKey(Integer.valueOf(i)) && networkEvent2 != null && isMmtelServiceAvailable(networkEvent2.network, i) != isMmtelServiceAvailable(networkEvent.network, i)) {
                        if (networkEvent2.isWifiConnected || !networkEvent.isWifiConnected || networkEvent.isEpdgConnected) {
                            this.mEventLog.logAndAdd(i, "onNetworkChanged: update capability");
                            this.mCapabilityDiscovery.setOwnCapabilities(i, true);
                            return;
                        }
                        IMSLog.i(LOG_TAG, i, "onNetworkChanged: wifi connected, but epdg is not yet");
                    }
                } else if (ConfigUtil.isRcsEur(simMno)) {
                    Log.i(LOG_TAG, "onNetworkChanged: setOwnCapabilities(false) is called");
                    if (RcsUtils.DualRcs.isDualRcsReg()) {
                        for (int i4 = 0; i4 < 2; i4++) {
                            if (RcsUtils.UiUtils.isRcsEnabledinSettings(context, i4) && map.containsKey(Integer.valueOf(i4))) {
                                this.mCapabilityDiscovery.updateOwnCapabilities(i4);
                                this.mCapabilityDiscovery.setOwnCapabilities(i4, false);
                            }
                        }
                    } else if (map.containsKey(Integer.valueOf(i))) {
                        this.mCapabilityDiscovery.updateOwnCapabilities(i);
                        this.mCapabilityDiscovery.setOwnCapabilities(i, false);
                    }
                }
            }
        } else {
            IMSLog.i(LOG_TAG, i, "onNetworkChanged: mAvailablePhoneId = ! phoneId");
        }
    }

    /* access modifiers changed from: package-private */
    public boolean blockOptionsToOwnUri(ImsUri imsUri, int i) {
        if (imsUri == null || this.mCapabilityDiscovery.getCapabilityControl(i) == null || this.mCapabilityDiscovery.getCapabilityControl(i) != this.mCapabilityDiscovery.getOptionsModule()) {
            return false;
        }
        for (Capabilities next : this.mCapabilityDiscovery.getOwnList().values()) {
            if (next.isAvailable() && next.getUri() != null && imsUri.equals(next.getUri())) {
                if (!RcsUtils.DualRcs.isDualRcsReg() || i == next.getPhoneId()) {
                    IMSLog.s(LOG_TAG, "blockOptionsToOwnUri: Block for sending OPTIONS to own number " + next.getUri());
                    return true;
                }
            }
        }
        return false;
    }

    /* access modifiers changed from: package-private */
    public boolean checkModuleReady(int i) {
        if (!this.mCapabilityDiscovery.isRunning()) {
            IMSLog.e(LOG_TAG, i, "checkModuleReady: module is disabled");
            return false;
        } else if (this.mCapabilityDiscovery.getUriGenerator() == null) {
            IMSLog.e(LOG_TAG, i, "checkModuleReady: uriGenerator is null");
            return false;
        } else if (RcsPolicyManager.getRcsStrategy(i) == null) {
            IMSLog.e(LOG_TAG, i, "checkModuleReady: MnoStrategy is null");
            return false;
        } else if (this.mCapabilityDiscovery.getCapabilityConfig(i) == null) {
            IMSLog.e(LOG_TAG, i, "checkModuleReady: config is null");
            return false;
        } else if (this.mCapabilityDiscovery.getCapabilityConfig(i).isAvailable()) {
            return true;
        } else {
            IMSLog.e(LOG_TAG, i, "checkModuleReady: mConfig.isAvailable == false");
            return false;
        }
    }

    /* access modifiers changed from: package-private */
    public void onServiceSwitched(int i, ContentValues contentValues, Map<Integer, Boolean> map, Map<Integer, Boolean> map2, boolean z) {
        boolean z2;
        boolean z3;
        boolean z4;
        IMSLog.i(LOG_TAG, i, "onServiceSwitched: ");
        if (contentValues != null) {
            z3 = ((Integer) contentValues.get(SipMsg.EVENT_PRESENCE)).intValue() == 1;
            z2 = ((Integer) contentValues.get("options")).intValue() == 1;
        } else {
            z2 = false;
            z3 = false;
        }
        if (map.get(Integer.valueOf(i)).booleanValue() != z3) {
            this.mCapabilityDiscovery.setPresenceSwitch(i, z3);
            IMSLog.i(LOG_TAG, i, "onServiceSwitched: presence changed: " + z3);
            z4 = true;
        } else {
            z4 = false;
        }
        if (map2.get(Integer.valueOf(i)).booleanValue() != z2) {
            this.mCapabilityDiscovery.settOptionsSwitch(i, z2);
            IMSLog.i(LOG_TAG, i, "onServiceSwitched: options changed: " + z2);
            z4 = true;
        }
        if (z4) {
            Boolean bool = Boolean.TRUE;
            if (!map.containsValue(bool) && !map2.containsValue(bool)) {
                this.mCapabilityDiscovery.setCapabilityModuleOn(false);
                this.mCapabilityDiscovery.stop();
            } else if (!z) {
                this.mCapabilityDiscovery.setCapabilityModuleOn(true);
                this.mCapabilityDiscovery.start();
            }
        }
    }

    /* access modifiers changed from: package-private */
    public void onUserSwitched() {
        int currentUser = Extensions.ActivityManager.getCurrentUser();
        Log.i(LOG_TAG, "onUserSwitched: userId = " + currentUser);
        for (Integer next : this.mCapabilityDiscovery.getUrisToRequest().keySet()) {
            Set set = this.mCapabilityDiscovery.getUrisToRequest().get(next);
            synchronized (set) {
                set.clear();
            }
            this.mCapabilityDiscovery.putUrisToRequestList(next.intValue(), set);
        }
        CapabilityDiscoveryModule capabilityDiscoveryModule = this.mCapabilityDiscovery;
        if (capabilityDiscoveryModule.getCapabilityConfig(capabilityDiscoveryModule.getActiveDataPhoneId()) != null) {
            CapabilityDiscoveryModule capabilityDiscoveryModule2 = this.mCapabilityDiscovery;
            if (!capabilityDiscoveryModule2.getCapabilityConfig(capabilityDiscoveryModule2.getActiveDataPhoneId()).isDisableInitialScan()) {
                Log.i(LOG_TAG, "onUserSwitched: start ContactCache");
                this.mCapabilityDiscovery.getPhonebook().stop();
                this.mCapabilityDiscovery.getPhonebook().start();
                this.mCapabilityDiscovery.getPhonebook().sendMessageContactSync();
            }
        }
    }

    /* access modifiers changed from: package-private */
    public long getRandomizedDelayForPeriodicPolling(int i, long j) {
        IMSLog.i(LOG_TAG, i, "getRandomizedDelayForPeriodicPolling: delay: " + (1000 * j));
        return (long) (((Math.random() * 0.2d) + 0.9d) * ((double) j));
    }

    /* access modifiers changed from: package-private */
    public void migrateSharedprefWithPhoneId(Context context) {
        SharedPreferences sharedPreferences = context.getSharedPreferences("capdiscovery", 0);
        if (sharedPreferences == null) {
            IMSLog.e(LOG_TAG, "migrateSharedprefWithPhoneId: open error");
            return;
        }
        int activeDataPhoneId = SimUtil.getActiveDataPhoneId();
        IMSLog.i(LOG_TAG, activeDataPhoneId, "migrateSharedprefWithPhoneId");
        SharedPreferences.Editor edit = context.getSharedPreferences("capdiscovery_" + activeDataPhoneId, 0).edit();
        for (Map.Entry next : sharedPreferences.getAll().entrySet()) {
            Object value = next.getValue();
            if (value instanceof Boolean) {
                edit.putBoolean((String) next.getKey(), ((Boolean) value).booleanValue());
            } else if (value instanceof Integer) {
                edit.putInt((String) next.getKey(), ((Integer) value).intValue());
            } else if (value instanceof Long) {
                edit.putLong((String) next.getKey(), ((Long) value).longValue());
            } else if (value instanceof String) {
                edit.putString((String) next.getKey(), (String) value);
            }
        }
        edit.apply();
        sharedPreferences.edit().clear().apply();
        if (!context.deleteSharedPreferences("capdiscovery")) {
            IMSLog.e(LOG_TAG, "Failed delete shared preferences");
        }
    }

    public static void reportErrorToApp(RcsCapabilityExchangeImplBase.OptionsResponseCallback optionsResponseCallback, int i) {
        try {
            optionsResponseCallback.onCommandError(i);
        } catch (ImsException e) {
            IMSLog.e(LOG_TAG, "reportErrorToApp: failed: " + e.getMessage());
        }
    }
}
