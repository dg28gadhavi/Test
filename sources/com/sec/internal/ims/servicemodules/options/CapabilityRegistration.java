package com.sec.internal.ims.servicemodules.options;

import android.content.Context;
import android.content.SharedPreferences;
import android.util.Base64;
import android.util.Log;
import com.sec.ims.ImsRegistration;
import com.sec.ims.options.Capabilities;
import com.sec.ims.settings.ImsProfile;
import com.sec.ims.util.ImsUri;
import com.sec.ims.util.NameAddr;
import com.sec.internal.constants.Mno;
import com.sec.internal.constants.ims.SipMsg;
import com.sec.internal.constants.ims.core.SimConstants;
import com.sec.internal.constants.ims.servicemodules.options.CapabilityConstants;
import com.sec.internal.constants.ims.settings.GlobalSettingsConstants;
import com.sec.internal.helper.CollectionUtils;
import com.sec.internal.helper.SimUtil;
import com.sec.internal.ims.core.sim.SimManagerFactory;
import com.sec.internal.ims.rcs.RcsPolicyManager;
import com.sec.internal.ims.rcs.util.RcsUtils;
import com.sec.internal.ims.registry.ImsRegistry;
import com.sec.internal.ims.settings.RcsPolicySettings;
import com.sec.internal.ims.util.ConfigUtil;
import com.sec.internal.ims.util.ImsUtil;
import com.sec.internal.ims.util.UriGenerator;
import com.sec.internal.ims.util.UriGeneratorFactory;
import com.sec.internal.interfaces.ims.core.IRegistrationManager;
import com.sec.internal.log.IMSLog;
import java.util.Date;
import java.util.Iterator;
import java.util.Map;

public class CapabilityRegistration {
    private static final long LAST_SEEN_UNKNOWN = -1;
    private static final String LOG_TAG = "CapabilityRegistration";
    private static final int SET_OWN_CAPABILITIES_DELAY = 500;
    private CapabilityDiscoveryModule mCapabilityDiscovery;
    private CapabilityUtil mCapabilityUtil;
    private IRegistrationManager mRegMan;

    CapabilityRegistration(CapabilityDiscoveryModule capabilityDiscoveryModule, CapabilityUtil capabilityUtil, IRegistrationManager iRegistrationManager) {
        this.mCapabilityDiscovery = capabilityDiscoveryModule;
        this.mCapabilityUtil = capabilityUtil;
        this.mRegMan = iRegistrationManager;
    }

    /* access modifiers changed from: package-private */
    public void onRegistered(Context context, ImsRegistration imsRegistration, Map<Integer, ImsRegistration> map, CapabilityConstants.CapExResult capExResult, long j) {
        int phoneId = imsRegistration.getPhoneId();
        setAvailablePhoneId(phoneId);
        IMSLog.i(LOG_TAG, phoneId, "onRegistered: RAT = " + imsRegistration.getRegiRat() + ", Services = " + imsRegistration.getServices());
        if (!this.mCapabilityUtil.isRegistrationSupported(imsRegistration)) {
            Log.e(LOG_TAG, "onRegistered: registration is not supported, return");
            return;
        }
        CapabilityDiscoveryModule capabilityDiscoveryModule = this.mCapabilityDiscovery;
        capabilityDiscoveryModule.sendMessage(capabilityDiscoveryModule.obtainMessage(50, phoneId, 0, ConfigUtil.getRcsProfileWithFeature(context, phoneId, imsRegistration.getImsProfile())));
        boolean needPublish = needPublish(context, imsRegistration, phoneId, map, capExResult, j);
        boolean needUnpublish = RcsPolicyManager.getRcsStrategy(phoneId).needUnpublish(map.get(Integer.valueOf(phoneId)), imsRegistration);
        this.mCapabilityDiscovery.setImsRegInfoList(phoneId, imsRegistration);
        setUriGenerator(imsRegistration, imsRegistration, phoneId);
        fallbackToOptions(imsRegistration, phoneId);
        updateOwnCapabilitiesOnRegi(context, imsRegistration.getOwnNumber(), imsRegistration.getImpi(), phoneId);
        RcsPolicyManager.getRcsStrategy(phoneId).startServiceBasedOnOmaDmNodes(phoneId);
        this.mCapabilityDiscovery.setNetworkType(this.mRegMan.getCurrentNetwork(imsRegistration.getHandle()), phoneId);
        publish(needUnpublish, needPublish, phoneId, imsRegistration);
        startPoll(phoneId);
        triggerCapexForIncallRegiDeregi(phoneId, imsRegistration, imsRegistration);
        loadUserLastActiveTimeStamp(context, phoneId);
        callContactSync(phoneId);
    }

    /* access modifiers changed from: package-private */
    public void onDeregistering(ImsRegistration imsRegistration, Map<Integer, ImsRegistration> map) {
        int phoneId = imsRegistration.getPhoneId();
        IMSLog.i(LOG_TAG, phoneId, "onDeregistering");
        if (imsRegistration.getImsProfile() != null && Mno.fromName(imsRegistration.getImsProfile().getMnoName()).isRjil()) {
            if (map.containsKey(Integer.valueOf(phoneId))) {
                this.mCapabilityDiscovery.triggerCapexForIncallRegiDeregi(phoneId, map.get(Integer.valueOf(phoneId)));
            }
            this.mCapabilityDiscovery.notifyOwnCapabilitiesChanged(phoneId);
        }
    }

    /* access modifiers changed from: package-private */
    public void onDeregistered(ImsRegistration imsRegistration, Map<Integer, ImsRegistration> map) {
        Log.i(LOG_TAG, "onDeregistered");
        processDeregistered(imsRegistration, map);
        if (this.mCapabilityUtil.isRegistrationSupported(imsRegistration)) {
            CapabilityDiscoveryModule capabilityDiscoveryModule = this.mCapabilityDiscovery;
            capabilityDiscoveryModule.sendMessage(capabilityDiscoveryModule.obtainMessage(51, imsRegistration.getPhoneId(), 0));
        }
    }

    /* access modifiers changed from: package-private */
    public void processDeregistered(ImsRegistration imsRegistration, Map<Integer, ImsRegistration> map) {
        this.mCapabilityDiscovery.post(new CapabilityRegistration$$ExternalSyntheticLambda0(this, imsRegistration, map));
    }

    /* access modifiers changed from: private */
    public /* synthetic */ void lambda$processDeregistered$0(ImsRegistration imsRegistration, Map map) {
        int phoneId = imsRegistration.getPhoneId();
        IMSLog.i(LOG_TAG, phoneId, "processDeregistered");
        if (!map.containsKey(Integer.valueOf(phoneId))) {
            Log.i(LOG_TAG, "processDeregistered: already deregistered");
            return;
        }
        Capabilities capabilities = this.mCapabilityDiscovery.getOwnList().get(Integer.valueOf(phoneId));
        capabilities.setAvailiable(false);
        this.mCapabilityDiscovery.getOwnList().put(Integer.valueOf(phoneId), capabilities);
        IMSLog.i(LOG_TAG, phoneId, "processDeregistered: mIsConfiguredOnCapability sets as false.");
        this.mCapabilityDiscovery.setIsConfiguredOnCapability(false, phoneId);
        this.mCapabilityDiscovery.removeImsRegInfoList(phoneId);
        this.mCapabilityDiscovery.removePublishedServiceList(phoneId);
        this.mCapabilityDiscovery.removeMessages(5, Integer.valueOf(phoneId));
        CapabilityDiscoveryModule capabilityDiscoveryModule = this.mCapabilityDiscovery;
        capabilityDiscoveryModule.sendMessageDelayed(capabilityDiscoveryModule.obtainMessage(5, 0, 0, Integer.valueOf(phoneId)), 500);
    }

    /* access modifiers changed from: package-private */
    public boolean needPublish(Context context, ImsRegistration imsRegistration, int i, Map<Integer, ImsRegistration> map, CapabilityConstants.CapExResult capExResult, long j) {
        if (!map.containsKey(Integer.valueOf(i))) {
            return true;
        }
        if (!imsRegistration.hasService(SipMsg.EVENT_PRESENCE)) {
            Log.e(LOG_TAG, "needPublish: do not publish, Presence is not registered.");
        } else if (!map.get(Integer.valueOf(i)).getServices().equals(imsRegistration.getServices()) || capExResult == CapabilityConstants.CapExResult.USER_NOT_REGISTERED) {
            return true;
        } else {
            if ((this.mCapabilityDiscovery.getCapabilityConfig(i) == null || this.mCapabilityDiscovery.getCapabilityConfig(i).usePresence()) && !this.mCapabilityDiscovery.getPresenceModule().isOwnCapPublished()) {
                return true;
            }
            Mno fromName = Mno.fromName(imsRegistration.getImsProfile().getMnoName());
            if (fromName.isKor() && imsRegistration.hasRcsService()) {
                long feature = this.mCapabilityDiscovery.getOwnList().get(Integer.valueOf(i)).getFeature();
                if (j != feature) {
                    this.mCapabilityDiscovery.setOldFeature(feature, i);
                    IMSLog.e(LOG_TAG, i, "needPublish: do publish, service list is same, but different Features.(KOR RCS only)");
                    return true;
                }
                IMSLog.e(LOG_TAG, i, "needPublish: do not publish, service list & feature list are same.");
            } else if (fromName == Mno.ATT && !map.get(Integer.valueOf(i)).getEpdgStatus() && imsRegistration.getEpdgStatus() && !this.mCapabilityUtil.isMmtelServiceAvailable(map.get(Integer.valueOf(i)).getRegiRat(), i)) {
                IMSLog.i(LOG_TAG, i, "needPublish: do publish, epdg handover");
                return true;
            } else if (fromName == Mno.TMOUS) {
                return ImsUtil.needForceRegiOrPublishForMmtelCallComposer(context, this.mRegMan.getImsProfile(i, ImsProfile.PROFILE_TYPE.VOLTE), i);
            } else {
                IMSLog.e(LOG_TAG, i, "needPublish: do not publish, service list is same.");
            }
        }
        return false;
    }

    /* access modifiers changed from: package-private */
    public void updateOwnCapabilitiesOnRegi(Context context, String str, String str2, int i) {
        Capabilities capabilities = this.mCapabilityDiscovery.getOwnList().get(Integer.valueOf(i));
        if (ImsRegistry.getBoolean(i, GlobalSettingsConstants.RCS.ENABLE_RCS_EXTENSIONS, false)) {
            for (Map.Entry<String, ?> value : context.getSharedPreferences("iari_app_association", 0).getAll().entrySet()) {
                String replaceAll = new String(Base64.decode(value.getValue().toString(), 0)).replaceAll(":", "%3A");
                if (!CollectionUtils.isNullOrEmpty(replaceAll) && !"default-tag".equals(replaceAll)) {
                    capabilities.addExtFeature(replaceAll);
                }
            }
            IMSLog.i(LOG_TAG, i, "updateOwnCapabilitiesOnRegi: extFeature = " + capabilities.getExtFeature());
        }
        if (str != null) {
            capabilities.setNumber(str);
            ImsUri normalizedUri = this.mCapabilityDiscovery.getUriGenerator().getNormalizedUri(capabilities.getNumber(), true);
            if (normalizedUri != null) {
                capabilities.setUri(normalizedUri);
            }
        } else if (str2 != null && this.mCapabilityDiscovery.getCapabilityControl(i) == this.mCapabilityDiscovery.getOptionsModule()) {
            capabilities.setNumber(this.mCapabilityUtil.extractMsisdnFromUri(str2));
            ImsUri normalizedUri2 = this.mCapabilityDiscovery.getUriGenerator().getNormalizedUri(capabilities.getNumber(), true);
            if (normalizedUri2 != null) {
                capabilities.setUri(normalizedUri2);
            }
        }
        IMSLog.s(LOG_TAG, i, "updateOwnCapabilitiesOnRegi: own number: " + capabilities.getNumber());
        if (capabilities.getUri() != null) {
            IMSLog.i(LOG_TAG, i, "updateOwnCapabilitiesOnRegi: own uri: " + capabilities.getUri().toStringLimit());
        }
        capabilities.setAvailiable(true);
        capabilities.setTimestamp(new Date());
        capabilities.setPhoneId(i);
        this.mCapabilityDiscovery.putOwnList(i, capabilities);
    }

    /* access modifiers changed from: package-private */
    public void loadUserLastActiveTimeStamp(Context context, int i) {
        SharedPreferences sharedPreferences = context.getSharedPreferences("capdiscovery_" + i, 0);
        IMSLog.i(LOG_TAG, i, "load last seen active");
        this.mCapabilityDiscovery.putUserLastActive(i, sharedPreferences.getLong("lastseenactive_" + SimManagerFactory.getImsiFromPhoneId(i), -1));
    }

    /* access modifiers changed from: package-private */
    public void publish(boolean z, boolean z2, int i, ImsRegistration imsRegistration) {
        if (z) {
            IMSLog.i(LOG_TAG, i, "onRegistered : need unpublish, invoke presenceModule to trigger unpublish");
            if (this.mCapabilityDiscovery.getCapabilityConfig(i) != null && this.mCapabilityDiscovery.getCapabilityConfig(i).usePresence()) {
                this.mCapabilityDiscovery.removeMessages(5, Integer.valueOf(i));
                this.mCapabilityDiscovery.getPresenceModule().unpublish(i);
            }
        } else if (z2 || (imsRegistration.hasService("options") && SimConstants.DSDS_DI.equals(SimUtil.getConfigDualIMS()) && Mno.fromName(imsRegistration.getImsProfile().getMnoName()).isRjil())) {
            IMSLog.i(LOG_TAG, i, "onRegistered : need PUBLISH, expecting EVT_SET_OWN_CAPABILITIES(5) after this");
            if (this.mCapabilityDiscovery.getCapabilityConfig(i) == null || !this.mCapabilityDiscovery.getCapabilityConfig(i).usePresence()) {
                this.mCapabilityDiscovery.removeMessages(5, Integer.valueOf(i));
                CapabilityDiscoveryModule capabilityDiscoveryModule = this.mCapabilityDiscovery;
                capabilityDiscoveryModule.sendMessageDelayed(capabilityDiscoveryModule.obtainMessage(5, 0, 0, Integer.valueOf(i)), 1000);
            } else if (this.mCapabilityDiscovery.getPresenceModule().getRegiInfoUpdater(i)) {
                this.mCapabilityDiscovery.removeMessages(5, Integer.valueOf(i));
                CapabilityDiscoveryModule capabilityDiscoveryModule2 = this.mCapabilityDiscovery;
                capabilityDiscoveryModule2.sendMessageDelayed(capabilityDiscoveryModule2.obtainMessage(5, 0, 0, Integer.valueOf(i)), 500);
                this.mCapabilityDiscovery.getPresenceModule().setRegiInfoUpdater(i, false);
            } else {
                CapabilityDiscoveryModule capabilityDiscoveryModule3 = this.mCapabilityDiscovery;
                capabilityDiscoveryModule3.sendMessageDelayed(capabilityDiscoveryModule3.obtainMessage(53, 0, 0, Integer.valueOf(i)), 500);
            }
        }
    }

    /* access modifiers changed from: package-private */
    public void callContactSync(int i) {
        if (this.mCapabilityDiscovery.getPhonebook().getBlockedInitialContactSyncBeforeRegi()) {
            IMSLog.i(LOG_TAG, i, "callContactSync : set the current time to skip the contact scan.");
            if (this.mCapabilityDiscovery.getPhonebook().getLastRefreshTime(i) == 0) {
                if (this.mCapabilityDiscovery.getCapabilityConfig(i).isDisableInitialScan()) {
                    this.mCapabilityDiscovery.getPhonebook().setLastRefreshTime(new Date().getTime(), i);
                } else {
                    this.mCapabilityDiscovery.getPhonebook().setLastRefreshTime(1, i);
                }
            }
        }
        if (this.mCapabilityDiscovery.getPhonebook().getIsBlockedContactChange() || this.mCapabilityDiscovery.getPhonebook().getBlockedInitialContactSyncBeforeRegi()) {
            IMSLog.i(LOG_TAG, i, "callContactSync : call contact sync if the contact change is blocked.");
            this.mCapabilityDiscovery.getPhonebook().sendMessageContactSync();
        }
    }

    /* access modifiers changed from: package-private */
    public void startPoll(int i) {
        if (!this.mCapabilityDiscovery.hasMessages(1, Integer.valueOf(i)) && !this.mCapabilityDiscovery.hasMessages(18, Integer.valueOf(i)) && this.mCapabilityDiscovery.getCapabilityConfig(i) != null && this.mCapabilityDiscovery.getCapabilityConfig(i).getPollingPeriod() != 0 && this.mCapabilityDiscovery.getCapabilityConfig(i).getPollingRate() != 0) {
            this.mCapabilityDiscovery.startPoll(i);
        }
    }

    /* access modifiers changed from: package-private */
    public void triggerCapexForIncallRegiDeregi(int i, ImsRegistration imsRegistration, ImsRegistration imsRegistration2) {
        if (imsRegistration2.getImsProfile() != null && Mno.fromName(imsRegistration2.getImsProfile().getMnoName()).isRjil()) {
            this.mCapabilityDiscovery.triggerCapexForIncallRegiDeregi(i, imsRegistration);
        }
    }

    /* access modifiers changed from: package-private */
    public void setAvailablePhoneId(int i) {
        if (RcsUtils.DualRcs.isDualRcsReg() && (i = SimUtil.getActiveDataPhoneId()) == -1) {
            i = 0;
        }
        this.mCapabilityDiscovery.setAvailablePhoneId(i);
    }

    private void setUriGenerator(ImsRegistration imsRegistration, ImsRegistration imsRegistration2, int i) {
        UriGenerator uriGenerator = UriGeneratorFactory.getInstance().get(imsRegistration2.getPreferredImpu().getUri(), UriGenerator.URIServiceType.RCS_URI);
        if (RcsPolicyManager.getRcsStrategy(i).boolSetting(RcsPolicySettings.RcsPolicy.USE_SIPURI_FOR_URIGENERATOR)) {
            Iterator it = imsRegistration.getImpuList().iterator();
            while (true) {
                if (!it.hasNext()) {
                    break;
                }
                NameAddr nameAddr = (NameAddr) it.next();
                if (nameAddr.getUri().getUriType() == ImsUri.UriType.SIP_URI) {
                    uriGenerator = UriGeneratorFactory.getInstance().get(nameAddr.getUri(), UriGenerator.URIServiceType.RCS_URI);
                    break;
                }
            }
        }
        this.mCapabilityDiscovery.setUriGenerator(uriGenerator);
        this.mCapabilityDiscovery.getPhonebook().setUriGenerator(uriGenerator);
    }

    private void fallbackToOptions(ImsRegistration imsRegistration, int i) {
        if (!imsRegistration.getImsProfile().getServiceSet(Integer.valueOf(imsRegistration.getRegiRat())).contains(SipMsg.EVENT_PRESENCE) && this.mCapabilityDiscovery.getCapabilityConfig(i) != null && this.mCapabilityDiscovery.getCapabilityConfig(i).getDefaultDisc() != 2 && this.mCapabilityDiscovery.getOptionsModule().isRunning()) {
            Log.e(LOG_TAG, "fallbackToOptions: Presence is not enabled in ImsProfile.");
            if (this.mCapabilityDiscovery.getCapabilityConfig(i) != null) {
                this.mCapabilityDiscovery.getCapabilityConfig(i).setUsePresence(false);
            }
            CapabilityDiscoveryModule capabilityDiscoveryModule = this.mCapabilityDiscovery;
            capabilityDiscoveryModule.putCapabilityControlForOptionsModule(i, capabilityDiscoveryModule.getOptionsModule());
        }
    }
}
