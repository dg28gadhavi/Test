package com.sec.internal.ims.servicemodules.options;

import android.content.Context;
import android.os.Handler;
import android.os.HandlerThread;
import android.util.Log;
import com.sec.ims.ImsRegistration;
import com.sec.ims.options.Capabilities;
import com.sec.ims.util.ImsUri;
import com.sec.internal.constants.Mno;
import com.sec.internal.constants.ims.entitilement.SoftphoneNamespaces;
import com.sec.internal.constants.ims.servicemodules.options.BotServiceIdTranslator;
import com.sec.internal.constants.ims.servicemodules.options.CapabilityConstants;
import com.sec.internal.helper.DmConfigHelper;
import com.sec.internal.helper.SimUtil;
import com.sec.internal.helper.SimpleEventLog;
import com.sec.internal.helper.UriUtil;
import com.sec.internal.ims.rcs.RcsPolicyManager;
import com.sec.internal.ims.rcs.util.RcsUtils;
import com.sec.internal.ims.registry.ImsRegistry;
import com.sec.internal.ims.servicemodules.base.ServiceModuleBase;
import com.sec.internal.ims.servicemodules.options.Contact;
import com.sec.internal.ims.util.ConfigUtil;
import com.sec.internal.ims.util.UriGenerator;
import com.sec.internal.ims.util.UriGeneratorFactory;
import com.sec.internal.imscr.LogClass;
import com.sec.internal.interfaces.ims.core.IRegistrationManager;
import com.sec.internal.log.IMSLog;
import java.util.ArrayList;
import java.util.Date;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;

public class CapabilityUpdate {
    private static final long LAST_SEEN_UNKNOWN = -1;
    private static final String LOG_TAG = "CapabilityUpdate";
    private static final long MAX_LAST_SEEN = 43200;
    private static final int MAX_RETRY_SYNC_CONTACT_COUNT = 10;
    private static final int MINUTE_DENOMINATION = 60000;
    private static final int RETRY_SYNC_CONTACT_DELAY = 30000;
    protected Handler mBackgroundHandler;
    private CapabilityDiscoveryModule mCapabilityDiscovery;
    private CapabilityUtil mCapabilityUtil;
    private SimpleEventLog mEventLog;
    IRegistrationManager mRegMan;

    CapabilityUpdate(CapabilityDiscoveryModule capabilityDiscoveryModule, CapabilityUtil capabilityUtil, IRegistrationManager iRegistrationManager, SimpleEventLog simpleEventLog) {
        this.mCapabilityDiscovery = capabilityDiscoveryModule;
        this.mCapabilityUtil = capabilityUtil;
        this.mRegMan = iRegistrationManager;
        this.mEventLog = simpleEventLog;
        HandlerThread handlerThread = new HandlerThread(LOG_TAG, 10);
        handlerThread.start();
        this.mBackgroundHandler = new Handler(handlerThread.getLooper());
    }

    /* access modifiers changed from: package-private */
    public void updateOwnCapabilities(Context context, Map<Integer, ImsRegistration> map, int i, boolean z, int i2) {
        long j = 0;
        for (ServiceModuleBase next : ImsRegistry.getAllServiceModules()) {
            if (!(next instanceof CapabilityDiscoveryModule)) {
                j |= next.getSupportFeature(i);
            }
        }
        if (this.mCapabilityDiscovery.getCapabilityConfig(i) != null && this.mCapabilityDiscovery.getCapabilityConfig(i).isLastSeenActive() && DmConfigHelper.getImsSwitchValue(context, "lastseen", i) == 1) {
            j |= Capabilities.FEATURE_LAST_SEEN_ACTIVE;
        }
        long j2 = j;
        Mno simMno = SimUtil.getSimMno(i);
        IMSLog.i(LOG_TAG, i, "updateOwnCapabilities: isConfiguredOnCapability is " + z + ", features from all module is " + Long.toHexString(j2));
        if (this.mRegMan != null && map.containsKey(Integer.valueOf(i)) && z && ConfigUtil.isRcsEur(simMno)) {
            if (simMno.isRjil()) {
                i2 = this.mRegMan.getCurrentNetworkByPhoneId(i);
                this.mCapabilityDiscovery.setNetworkType(i2, i);
            }
            int i3 = i2;
            j2 = this.mCapabilityUtil.filterFeaturesWithService(j2, this.mRegMan.getServiceForNetwork(map.get(Integer.valueOf(i)).getImsProfile(), i3, false, i), i3, i);
            if (RcsUtils.DualRcs.isDualRcsReg() && i != SimUtil.getActiveDataPhoneId()) {
                j2 = this.mCapabilityUtil.filterEnrichedCallFeatures(j2);
            }
        }
        IMSLog.s(LOG_TAG, i, "updateOwnCapabilities: filtered features is " + Long.toHexString(j2));
        Capabilities capabilities = this.mCapabilityDiscovery.getOwnList().get(Integer.valueOf(i));
        capabilities.setFeatures(j2);
        capabilities.setAvailableFeatures(j2);
        this.mCapabilityDiscovery.putOwnList(i, capabilities);
        this.mCapabilityDiscovery.setIsConfigured(true, i);
        this.mCapabilityDiscovery.setIsConfiguredOnCapability(true, i);
    }

    /* access modifiers changed from: package-private */
    public void processContactChanged(boolean z, int i, boolean z2, long j) {
        this.mBackgroundHandler.post(new CapabilityUpdate$$ExternalSyntheticLambda0(this, z, i, z2, j));
    }

    /* access modifiers changed from: private */
    public /* synthetic */ void lambda$processContactChanged$0(boolean z, int i, boolean z2, long j) {
        long j2;
        ImsUri imsUri;
        int i2 = i;
        Map<String, Contact> contacts = this.mCapabilityDiscovery.getPhonebook().getContacts();
        this.mEventLog.logAndAdd(i2, "processContactChanged: " + contacts.size() + " contacts.");
        IMSLog.c(LogClass.CDM_CON_CHANGE, i2 + "," + contacts.size());
        boolean z3 = z;
        for (Contact next : contacts.values()) {
            boolean z4 = z3;
            for (Contact.ContactNumber next2 : next.getContactNumberList()) {
                if (next2.getNumber() != null && next2.getNumber().startsWith("*")) {
                    imsUri = null;
                } else if (next2.getNormalizedNumber() != null) {
                    imsUri = ImsUri.parse("tel:" + next2.getNormalizedNumber());
                } else {
                    imsUri = this.mCapabilityDiscovery.getUriGenerator().getNormalizedUri(next2.getNumber(), true);
                }
                ImsUri imsUri2 = imsUri;
                if (!this.mCapabilityUtil.blockOptionsToOwnUri(imsUri2, i2) && imsUri2 != null && !this.mCapabilityDiscovery.getUrisToRequest().get(Integer.valueOf(i)).contains(imsUri2)) {
                    Capabilities capabilities = this.mCapabilityDiscovery.getCapabilitiesCache(i2).get(imsUri2);
                    if (capabilities == null) {
                        this.mCapabilityDiscovery.getCapabilitiesCache(i2).persistCachedUri(imsUri2, RcsPolicyManager.getRcsStrategy(i).getCapabilitiesInitialInfo(i, imsUri2, UriUtil.getMsisdnNumber(imsUri2), next.getId(), -1, next.getName()));
                    } else if (capabilities.getContactId() == null) {
                        this.mCapabilityDiscovery.getCapabilitiesCache(i2).updateContactInfo(imsUri2, UriUtil.getMsisdnNumber(imsUri2), next.getId(), next.getName(), true, capabilities);
                    } else if (!capabilities.getContactId().equals(next.getId())) {
                        this.mCapabilityDiscovery.getCapabilitiesCache(i2).updateContactInfo(imsUri2, UriUtil.getMsisdnNumber(imsUri2), next.getId(), next.getName(), false, capabilities);
                    }
                    if (this.mCapabilityDiscovery.updatePollList(imsUri2, true, i2)) {
                        z4 = true;
                    }
                }
            }
            z3 = z4;
        }
        this.mEventLog.logAndAdd(i2, "processContactChanged: updatePollList done, " + this.mCapabilityDiscovery.getUrisToRequest().get(Integer.valueOf(i)).size() + " contacts added");
        this.mCapabilityUtil.handleRemovedNumbers(i2);
        if (this.mCapabilityDiscovery.getUrisToRequest().values().isEmpty() || z3 || !z2) {
            j2 = j;
        } else {
            IMSLog.i(LOG_TAG, i2, "processContactChanged: added an contact when RCS offline. need to poll");
            this.mCapabilityDiscovery.setIsOfflineAddedContact(false, i2);
            j2 = j;
            z3 = true;
        }
        if (!needPollOnContactChanged(z3, i2, j2)) {
            IMSLog.i(LOG_TAG, i2, "processContactChanged: no need to poll now");
        }
    }

    /* access modifiers changed from: package-private */
    public void setOwnCapabilities(int i, boolean z, Map<Integer, ImsRegistration> map, int i2, boolean z2, String str) {
        long j;
        int i3 = i;
        IMSLog.i(LOG_TAG, i, "setOwnCapabilities:");
        Capabilities capabilities = this.mCapabilityDiscovery.getOwnList().get(Integer.valueOf(i));
        Map<Integer, ImsRegistration> map2 = map;
        int i4 = i2;
        Set<String> filterServicesWithReg = this.mCapabilityUtil.filterServicesWithReg(map, this.mRegMan, i2, i);
        if (filterServicesWithReg != null) {
            long filterFeaturesWithService = this.mCapabilityUtil.filterFeaturesWithService(capabilities.getFeature(), filterServicesWithReg, i2, i);
            if (!RcsUtils.DualRcs.isDualRcsReg() || i3 == SimUtil.getActiveDataPhoneId()) {
                j = this.mCapabilityUtil.filterFeaturesWithCallState(filterFeaturesWithService, z2, str);
            } else {
                j = this.mCapabilityUtil.filterEnrichedCallFeatures(filterFeaturesWithService);
            }
            this.mCapabilityDiscovery.setHasVideoOwnCapability(CapabilityUtil.hasFeature(j, (long) Capabilities.FEATURE_MMTEL_VIDEO), i);
            IMSLog.i(LOG_TAG, i, "setOwnCapabilities: mHasVideoOwn = " + this.mCapabilityDiscovery.hasVideoOwnCapability(i));
            IMSLog.c(LogClass.CDM_SET_OWNCAPA, i + ",SETOWN:" + j);
            this.mCapabilityDiscovery.getOptionsModule().setOwnCapabilities(j, i);
            if (this.mCapabilityDiscovery.getCapabilityConfig(i) != null && this.mCapabilityDiscovery.getCapabilityConfig(i).usePresence()) {
                this.mCapabilityDiscovery.getPresenceModule().setOwnCapabilities(j, i);
            }
        }
        if (capabilities.getUri() != null) {
            if (this.mCapabilityDiscovery.getCapabilitiesCache(i).get(capabilities.getUri()) == null) {
                IMSLog.i(LOG_TAG, i, "setOwnCapabilities: Add ownCap to CapabilitiesCache");
                try {
                    this.mCapabilityDiscovery.getCapabilitiesCache(i).add(capabilities.clone());
                } catch (CloneNotSupportedException e) {
                    e.printStackTrace();
                }
            } else {
                IMSLog.i(LOG_TAG, i, "setOwnCapabilities: updateOwnCapabilities");
                this.mCapabilityDiscovery.updateOwnCapabilities(i);
            }
        }
        if (z) {
            this.mRegMan.setOwnCapabilities(i, capabilities);
        }
        this.mCapabilityDiscovery.notifyOwnCapabilitiesChanged(i);
    }

    /* access modifiers changed from: package-private */
    public boolean needPollOnContactChanged(boolean z, int i, long j) {
        if (!z) {
            IMSLog.i(LOG_TAG, i, "needPollOnContactChanged: isPollRequired is false.");
            this.mCapabilityDiscovery.getPhonebook().setThrottleContactSync(false, i);
            return false;
        } else if (this.mCapabilityDiscovery.getUrisToRequest().values().isEmpty()) {
            IMSLog.i(LOG_TAG, i, "needPollOnContactChanged: No URI to request.");
            this.mCapabilityDiscovery.getPhonebook().setThrottleContactSync(false, i);
            return false;
        } else {
            Mno simMno = SimUtil.getSimMno(i);
            if (this.mCapabilityDiscovery.getCapabilityConfig(i) != null && this.mCapabilityDiscovery.getCapabilityConfig(i).isDisableInitialScan() && (simMno == Mno.RJIL || simMno.isChn() || simMno == Mno.VODAFONE_INDIA || simMno == Mno.IDEA_INDIA || simMno == Mno.VZW)) {
                IMSLog.i(LOG_TAG, i, "needPollOnContactChanged: Address book scan disabled.");
                return false;
            } else if (this.mCapabilityDiscovery.isPollingInProgress(i)) {
                if (this.mCapabilityDiscovery.getThrottledIntent(i) == null) {
                    IMSLog.i(LOG_TAG, i, "needPollOnContactChanged: posting delayed poll event");
                    CapabilityDiscoveryModule capabilityDiscoveryModule = this.mCapabilityDiscovery;
                    capabilityDiscoveryModule.sendMessageDelayed(capabilityDiscoveryModule.obtainMessage(1, Integer.valueOf(i)), this.mCapabilityUtil.getDelayTimeToPoll(j, i));
                    return true;
                }
                IMSLog.i(LOG_TAG, i, "needPollOnContactChanged: polling already in progress");
                return false;
            } else if (this.mCapabilityDiscovery.getCapabilityControl(i) == null || !this.mCapabilityDiscovery.getCapabilityControl(i).isReadyToRequest(i) || !this.mCapabilityDiscovery.isRunning()) {
                IMSLog.i(LOG_TAG, i, "needPollOnContactChanged: new contact was added but RCS not work");
                this.mCapabilityDiscovery.setIsOfflineAddedContact(true, i);
                return false;
            } else {
                IMSLog.i(LOG_TAG, i, "needPollOnContactChanged: posting poll event");
                this.mCapabilityDiscovery.removeMessages(1, Integer.valueOf(i));
                CapabilityDiscoveryModule capabilityDiscoveryModule2 = this.mCapabilityDiscovery;
                capabilityDiscoveryModule2.sendMessageDelayed(capabilityDiscoveryModule2.obtainMessage(1, Integer.valueOf(i)), this.mCapabilityUtil.getDelayTimeToPoll(j, i));
                return true;
            }
        }
    }

    /* access modifiers changed from: package-private */
    public boolean isPollingInProgress(int i, List<Date> list) {
        if (this.mCapabilityDiscovery.getCapabilityConfig(i) == null) {
            IMSLog.e(LOG_TAG, i, "isPollingInProgress: mConfig for phoneId: " + i + " is null");
            return false;
        } else if (this.mCapabilityDiscovery.getCapabilityConfig(i).isPollingPeriodUpdated()) {
            this.mCapabilityDiscovery.getCapabilityConfig(i).resetPollingPeriodUpdated();
            IMSLog.i(LOG_TAG, i, "isPollingPeriodUpdated: " + this.mCapabilityDiscovery.getCapabilityConfig(i).isPollingPeriodUpdated());
            return false;
        } else if (this.mCapabilityDiscovery.getThrottledIntent(i) != null) {
            IMSLog.i(LOG_TAG, i, "isPollingInProgress: subscribe throttle in progress");
            return true;
        } else {
            for (Date time : list) {
                if (new Date().getTime() - time.getTime() < ((long) this.mCapabilityDiscovery.getCapabilityConfig(i).getPollListSubExpiry()) * 1000) {
                    return true;
                }
            }
            return false;
        }
    }

    /* access modifiers changed from: package-private */
    public void onUpdateCapabilities(List<ImsUri> list, long j, CapabilityConstants.CapExResult capExResult, String str, int i, List<ImsUri> list2, int i2, boolean z, String str2, String str3) {
        CapabilityConstants.CapExResult capExResult2 = capExResult;
        int i3 = i2;
        if (list == null) {
            IMSLog.i(LOG_TAG, i3, "onUpdateCapabilities: uris null, return");
            return;
        }
        ArrayList arrayList = new ArrayList();
        for (ImsUri next : list) {
            if (next != null) {
                arrayList.add(next.toStringLimit());
            }
        }
        long j2 = j;
        long filterInCallFeatures = this.mCapabilityUtil.filterInCallFeatures(j, list.get(0), str3);
        IMSLog.s(LOG_TAG, i3, "onUpdateCapabilities: uriList " + list);
        IMSLog.i(LOG_TAG, i3, "onUpdateCapabilities: " + arrayList + " result " + capExResult + " features " + Capabilities.dumpFeature(filterInCallFeatures));
        this.mCapabilityDiscovery.setLastCapExResult(capExResult, i3);
        if (this.mCapabilityUtil.checkModuleReady(i3)) {
            processUpdateCapabilities(list, filterInCallFeatures, capExResult, str, i, list2, i2, z, str2);
        }
    }

    private void processUpdateCapabilities(List<ImsUri> list, long j, CapabilityConstants.CapExResult capExResult, String str, int i, List<ImsUri> list2, int i2, boolean z, String str2) {
        this.mBackgroundHandler.post(new CapabilityUpdate$$ExternalSyntheticLambda1(this, list, i2, j, capExResult, str, i, list2, str2));
    }

    /* access modifiers changed from: private */
    public /* synthetic */ void lambda$processUpdateCapabilities$1(List list, int i, long j, CapabilityConstants.CapExResult capExResult, String str, int i2, List list2, String str2) {
        Capabilities capabilities;
        int i3 = i;
        long j2 = j;
        CapabilityConstants.CapExResult capExResult2 = capExResult;
        ArrayList arrayList = new ArrayList();
        ArrayList arrayList2 = new ArrayList();
        Iterator it = list.iterator();
        while (it.hasNext()) {
            arrayList2.add(this.mCapabilityDiscovery.getUriGenerator().normalize((ImsUri) it.next()));
        }
        IMSLog.s(LOG_TAG, i3, "processUpdateCapabilities,run, normalizedUris " + arrayList2);
        Iterator it2 = arrayList2.iterator();
        int i4 = 0;
        boolean z = false;
        while (it2.hasNext()) {
            ImsUri imsUri = (ImsUri) it2.next();
            Capabilities capabilities2 = this.mCapabilityDiscovery.getCapabilitiesCache(i3).get(imsUri);
            long updateAvailableFeatures = RcsPolicyManager.getRcsStrategy(i).updateAvailableFeatures(capabilities2, j2, capExResult2);
            long updateFeatures = RcsPolicyManager.getRcsStrategy(i).updateFeatures(capabilities2, updateAvailableFeatures, capExResult2);
            long j3 = updateAvailableFeatures;
            ImsUri imsUri2 = imsUri;
            Iterator it3 = it2;
            int i5 = i4;
            if (!RcsPolicyManager.getRcsStrategy(i).needCapabilitiesUpdate(capExResult, capabilities2, updateFeatures, this.mCapabilityDiscovery.getCapabilityConfig(i3).getCapCacheExpiry())) {
                arrayList.add(imsUri2);
                capExResult2 = capExResult;
            } else {
                boolean update = z | this.mCapabilityDiscovery.getCapabilitiesCache(i3).update(imsUri2, updateFeatures, j3, str, (long) i2, new Date(), list2, str2);
                StringBuilder sb = new StringBuilder();
                sb.append("processUpdateCapabilities: ");
                sb.append(imsUri2 != null ? imsUri2.toStringLimit() : null);
                sb.append(" is updated, features: ");
                sb.append(Long.toHexString(updateFeatures));
                sb.append(", hasCapChanged: ");
                sb.append(update);
                IMSLog.i(LOG_TAG, i3, sb.toString());
                StringBuilder sb2 = new StringBuilder();
                sb2.append(i3);
                sb2.append(",");
                sb2.append(imsUri2 != null ? imsUri2.toStringLimit() : "xx");
                sb2.append(",");
                sb2.append(Long.toHexString(updateFeatures));
                sb2.append(",");
                sb2.append(j2);
                IMSLog.c(LogClass.CDM_UPD_CAPA, sb2.toString());
                this.mCapabilityUtil.sendGateMessage(imsUri2, j3, i3);
                capExResult2 = capExResult;
                z = update;
            }
            i4 = i5;
            it2 = it3;
        }
        int i6 = i4;
        arrayList2.removeAll(arrayList);
        if (arrayList2.size() > 0 && (capabilities = this.mCapabilityDiscovery.getCapabilitiesCache(i3).get((ImsUri) arrayList2.get(i6))) != null) {
            if (CapabilityUtil.hasFeature(capabilities.getFeature(), Capabilities.FEATURE_CHATBOT_ROLE)) {
                capabilities.setBotServiceId(BotServiceIdTranslator.getInstance().translate(((ImsUri) list.get(i6)).getMsisdn(), i3));
            }
            this.mCapabilityDiscovery.notifyCapabilitiesChanged(arrayList2, capabilities, i3);
        }
    }

    /* access modifiers changed from: package-private */
    public void _syncContact(Mno mno) {
        if (RcsPolicyManager.getRcsStrategy(SimUtil.getSimSlotPriority()) == null) {
            Log.e(LOG_TAG, "_syncContact: MnoStrategy is null");
            CapabilityDiscoveryModule capabilityDiscoveryModule = this.mCapabilityDiscovery;
            capabilityDiscoveryModule.sendMessageDelayed(capabilityDiscoveryModule.obtainMessage(10, mno), 1000);
            return;
        }
        if (this.mCapabilityDiscovery.getUriGenerator() == null) {
            this.mCapabilityDiscovery.setUriGenerator(UriGeneratorFactory.getInstance().get(UriGenerator.URIServiceType.RCS_URI));
            this.mCapabilityDiscovery.getPhonebook().setUriGenerator(this.mCapabilityDiscovery.getUriGenerator());
        }
        Log.i(LOG_TAG, "_syncContact: initial startContactSync");
        this.mCapabilityDiscovery.getPhonebook().setMno(mno);
        if (this.mCapabilityDiscovery.getPhonebook().getContactProviderStatus() >= 0) {
            this.mCapabilityDiscovery.getPhonebook().sendMessageContactSync();
            return;
        }
        Log.i(LOG_TAG, "_syncContact: contactProvider is not yet ready");
        IMSLog.c(LogClass.CDM_SYNC_CONT, "N,CP:NOTREADY");
    }

    /* access modifiers changed from: package-private */
    public void onOwnCapabilitiesChanged(int i) {
        this.mCapabilityDiscovery.updateOwnCapabilities(i);
        this.mRegMan.setOwnCapabilities(i, this.mCapabilityDiscovery.getOwnList().get(Integer.valueOf(i)));
        IMSLog.i(LOG_TAG, i, "onOwnCapabilitiesChanged: " + Capabilities.dumpFeature(this.mCapabilityDiscovery.getOwnList().get(Integer.valueOf(i)).getFeature()));
    }

    /* access modifiers changed from: package-private */
    /* JADX WARNING: Removed duplicated region for block: B:14:0x009b  */
    /* JADX WARNING: Removed duplicated region for block: B:17:0x00b4  */
    /* JADX WARNING: Removed duplicated region for block: B:31:0x0113  */
    /* JADX WARNING: Removed duplicated region for block: B:43:0x019e  */
    /* JADX WARNING: Removed duplicated region for block: B:47:? A[RETURN, SYNTHETIC] */
    /* Code decompiled incorrectly, please refer to instructions dump. */
    public void prepareResponse(android.content.Context r18, java.util.List<com.sec.ims.util.ImsUri> r19, long r20, java.lang.String r22, int r23, java.lang.String r24, java.util.Map<java.lang.Integer, com.sec.ims.ImsRegistration> r25, int r26, java.lang.String r27) {
        /*
            r17 = this;
            r0 = r17
            r1 = r19
            r2 = r20
            r10 = r23
            r11 = r25
            java.lang.String r4 = "prepareResponse"
            java.lang.String r12 = "CapabilityUpdate"
            com.sec.internal.log.IMSLog.i(r12, r10, r4)
            com.sec.internal.interfaces.ims.core.IRegistrationManager r4 = r0.mRegMan
            if (r4 == 0) goto L_0x01b6
            java.lang.Integer r4 = java.lang.Integer.valueOf(r23)
            boolean r4 = r11.containsKey(r4)
            if (r4 != 0) goto L_0x0022
            goto L_0x01b6
        L_0x0022:
            com.sec.internal.interfaces.ims.core.IRegistrationManager r4 = r0.mRegMan
            java.lang.Integer r5 = java.lang.Integer.valueOf(r23)
            java.lang.Object r5 = r11.get(r5)
            com.sec.ims.ImsRegistration r5 = (com.sec.ims.ImsRegistration) r5
            com.sec.ims.settings.ImsProfile r5 = r5.getImsProfile()
            r13 = 0
            r8 = r26
            java.util.Set r7 = r4.getServiceForNetwork(r5, r8, r13, r10)
            int r4 = com.sec.ims.options.Capabilities.FEATURE_OFFLINE_RCS_USER
            long r4 = (long) r4
            com.sec.internal.ims.servicemodules.options.CapabilityDiscoveryModule r6 = r0.mCapabilityDiscovery
            java.util.Map r6 = r6.getOwnList()
            java.lang.Integer r9 = java.lang.Integer.valueOf(r23)
            java.lang.Object r6 = r6.get(r9)
            com.sec.ims.options.Capabilities r6 = (com.sec.ims.options.Capabilities) r6
            java.util.List r6 = r6.getExtFeature()
            boolean r9 = com.sec.internal.helper.CollectionUtils.isNullOrEmpty((java.lang.String) r24)
            java.lang.String r14 = ""
            if (r9 != 0) goto L_0x007e
            boolean r9 = com.sec.internal.helper.CollectionUtils.isNullOrEmpty((java.util.Collection<?>) r6)
            if (r9 != 0) goto L_0x007e
            java.util.ArrayList r9 = new java.util.ArrayList
            java.lang.String r15 = ","
            r13 = r24
            java.lang.String[] r13 = r13.split(r15)
            java.util.List r13 = java.util.Arrays.asList(r13)
            r9.<init>(r13)
            r9.retainAll(r6)
            boolean r6 = com.sec.internal.helper.CollectionUtils.isNullOrEmpty((java.util.Collection<?>) r9)
            if (r6 != 0) goto L_0x007e
            java.lang.String r6 = java.lang.String.join(r15, r9)
            r13 = r6
            goto L_0x007f
        L_0x007e:
            r13 = r14
        L_0x007f:
            java.lang.StringBuilder r6 = new java.lang.StringBuilder
            r6.<init>()
            java.lang.String r9 = "Common extfeature: "
            r6.append(r9)
            r6.append(r13)
            java.lang.String r6 = r6.toString()
            android.util.Log.i(r12, r6)
            com.sec.internal.ims.servicemodules.options.CapabilityDiscoveryModule r6 = r0.mCapabilityDiscovery
            com.sec.internal.ims.servicemodules.options.CapabilityConfig r15 = r6.getCapabilityConfig(r10)
            if (r15 == 0) goto L_0x009f
            java.lang.String r14 = r15.getRcsProfile()
        L_0x009f:
            r6 = 0
            java.lang.Object r9 = r1.get(r6)
            com.sec.ims.util.ImsUri r9 = (com.sec.ims.util.ImsUri) r9
            java.lang.String r6 = r9.getMsisdn()
            r9 = r18
            boolean r6 = com.sec.internal.helper.BlockedNumberUtil.isBlockedNumber(r9, r6)
            r16 = -1
            if (r6 != 0) goto L_0x0113
            com.sec.internal.ims.servicemodules.options.CapabilityUtil r4 = r0.mCapabilityUtil
            com.sec.internal.ims.servicemodules.options.CapabilityDiscoveryModule r5 = r0.mCapabilityDiscovery
            java.util.Map r5 = r5.getOwnList()
            java.lang.Integer r6 = java.lang.Integer.valueOf(r23)
            java.lang.Object r5 = r5.get(r6)
            com.sec.ims.options.Capabilities r5 = (com.sec.ims.options.Capabilities) r5
            long r5 = r5.getFeature()
            r8 = r26
            r9 = r23
            long r4 = r4.filterFeaturesWithService(r5, r7, r8, r9)
            int r6 = com.sec.ims.options.Capabilities.FEATURE_OFFLINE_RCS_USER
            long r6 = (long) r6
            int r6 = (r2 > r6 ? 1 : (r2 == r6 ? 0 : -1))
            if (r6 == 0) goto L_0x00e0
            boolean r6 = com.sec.ims.settings.ImsProfile.isRcsUpProfile(r14)
            if (r6 != 0) goto L_0x00e0
            long r4 = r4 & r2
        L_0x00e0:
            if (r15 == 0) goto L_0x0102
            boolean r6 = r15.isLastSeenActive()
            if (r6 == 0) goto L_0x0102
            java.lang.String r6 = "lastseen"
            boolean r6 = r0.isServiceRegistered(r6, r11)
            if (r6 == 0) goto L_0x0102
            long r6 = com.sec.ims.options.Capabilities.FEATURE_LAST_SEEN_ACTIVE
            boolean r2 = com.sec.internal.ims.servicemodules.options.CapabilityUtil.hasFeature(r2, r6)
            if (r2 == 0) goto L_0x0102
            java.lang.String r2 = "setting last seen active"
            com.sec.internal.log.IMSLog.s(r12, r2)
            int r16 = r0.getLastSeen(r10)
        L_0x0102:
            com.sec.internal.ims.servicemodules.options.CapabilityUtil r2 = r0.mCapabilityUtil
            r3 = 0
            java.lang.Object r6 = r1.get(r3)
            com.sec.ims.util.ImsUri r6 = (com.sec.ims.util.ImsUri) r6
            r3 = r27
            long r2 = r2.filterInCallFeatures(r4, r6, r3)
            r4 = r2
            goto L_0x016f
        L_0x0113:
            com.sec.internal.ims.servicemodules.im.strategy.IMnoStrategy r6 = com.sec.internal.ims.rcs.RcsPolicyManager.getRcsStrategy(r23)
            java.lang.String r9 = "block_msg"
            boolean r6 = r6.boolSetting(r9)
            if (r6 == 0) goto L_0x0171
            java.util.HashSet r4 = new java.util.HashSet
            java.lang.String[] r5 = com.sec.ims.settings.ImsProfile.getChatServiceList()
            java.util.List r5 = java.util.Arrays.asList(r5)
            r4.<init>(r5)
            r7.retainAll(r4)
            com.sec.internal.ims.servicemodules.options.CapabilityUtil r4 = r0.mCapabilityUtil
            com.sec.internal.ims.servicemodules.options.CapabilityDiscoveryModule r5 = r0.mCapabilityDiscovery
            java.util.Map r5 = r5.getOwnList()
            java.lang.Integer r6 = java.lang.Integer.valueOf(r23)
            java.lang.Object r5 = r5.get(r6)
            com.sec.ims.options.Capabilities r5 = (com.sec.ims.options.Capabilities) r5
            long r5 = r5.getFeature()
            r8 = r26
            r9 = r23
            long r4 = r4.filterFeaturesWithService(r5, r7, r8, r9)
            int r6 = com.sec.ims.options.Capabilities.FEATURE_OFFLINE_RCS_USER
            long r6 = (long) r6
            int r6 = (r2 > r6 ? 1 : (r2 == r6 ? 0 : -1))
            if (r6 == 0) goto L_0x015b
            boolean r6 = com.sec.ims.settings.ImsProfile.isRcsUpProfile(r14)
            if (r6 != 0) goto L_0x015b
            long r4 = r4 & r2
        L_0x015b:
            java.lang.StringBuilder r2 = new java.lang.StringBuilder
            r2.<init>()
            java.lang.String r3 = "Number is blocked respond with Chat tag : "
            r2.append(r3)
            r2.append(r4)
            java.lang.String r2 = r2.toString()
            com.sec.internal.log.IMSLog.s(r12, r2)
        L_0x016f:
            r3 = 0
            goto L_0x0198
        L_0x0171:
            java.lang.StringBuilder r2 = new java.lang.StringBuilder
            r2.<init>()
            java.lang.String r3 = "Number is blocked respond with empty tags : "
            r2.append(r3)
            r2.append(r4)
            java.lang.String r3 = " "
            r2.append(r3)
            r3 = 0
            java.lang.Object r6 = r1.get(r3)
            com.sec.ims.util.ImsUri r6 = (com.sec.ims.util.ImsUri) r6
            java.lang.String r6 = r6.getMsisdn()
            r2.append(r6)
            java.lang.String r2 = r2.toString()
            com.sec.internal.log.IMSLog.s(r12, r2)
        L_0x0198:
            boolean r2 = r19.isEmpty()
            if (r2 != 0) goto L_0x01b5
            com.sec.internal.ims.servicemodules.options.CapabilityDiscoveryModule r0 = r0.mCapabilityDiscovery
            com.sec.internal.ims.servicemodules.options.OptionsModule r0 = r0.getOptionsModule()
            java.lang.Object r1 = r1.get(r3)
            com.sec.ims.util.ImsUri r1 = (com.sec.ims.util.ImsUri) r1
            r2 = r4
            r4 = r22
            r5 = r16
            r6 = r23
            r7 = r13
            r0.sendCapexResponse(r1, r2, r4, r5, r6, r7)
        L_0x01b5:
            return
        L_0x01b6:
            java.lang.String r0 = "prepareResponse: mRegMan or mImsRegInfo is null"
            com.sec.internal.log.IMSLog.i(r12, r10, r0)
            return
        */
        throw new UnsupportedOperationException("Method not decompiled: com.sec.internal.ims.servicemodules.options.CapabilityUpdate.prepareResponse(android.content.Context, java.util.List, long, java.lang.String, int, java.lang.String, java.util.Map, int, java.lang.String):void");
    }

    /* access modifiers changed from: package-private */
    public boolean isServiceRegistered(String str, Map<Integer, ImsRegistration> map) {
        if (!map.containsKey(Integer.valueOf(this.mCapabilityDiscovery.getActiveDataPhoneId())) || str == null) {
            return false;
        }
        ImsRegistration imsRegistration = map.get(Integer.valueOf(this.mCapabilityDiscovery.getActiveDataPhoneId()));
        Log.i(LOG_TAG, "isServiceRegistered: " + str + " : " + imsRegistration.getServices());
        return imsRegistration.hasService(str);
    }

    /* access modifiers changed from: package-private */
    public int getLastSeen(int i) {
        long userLastActive = this.mCapabilityDiscovery.getUserLastActive(i);
        if (userLastActive > 0) {
            long currentTimeMillis = System.currentTimeMillis();
            Log.i(LOG_TAG, "last active timestamp " + new Date(userLastActive).toString() + "Current Time Stamp " + new Date(currentTimeMillis).toString());
            userLastActive = (long) ((int) ((currentTimeMillis - userLastActive) / SoftphoneNamespaces.SoftphoneSettings.LONG_BACKOFF));
            if (userLastActive >= MAX_LAST_SEEN) {
                userLastActive = 43200;
            }
        }
        Log.i(LOG_TAG, " last seen value " + userLastActive);
        return (int) userLastActive;
    }

    /* access modifiers changed from: package-private */
    public void onRetrySyncContact(int i) {
        Log.i(LOG_TAG, "onRetrySyncContact");
        IMSLog.c(LogClass.CDM_SYNC_CONT_RETRY, "N," + i);
        this.mCapabilityDiscovery.removeMessages(13);
        if (i == 10) {
            Log.i(LOG_TAG, "onRetrySyncContact: max retry count exceed");
        } else if (this.mCapabilityDiscovery.getPhonebook().getContactProviderStatus() >= 0) {
            this.mCapabilityDiscovery.setRetrySyncContactCount(0);
            this.mCapabilityDiscovery.syncContact();
        } else {
            int i2 = i + 1;
            this.mCapabilityDiscovery.setRetrySyncContactCount(i2);
            Log.i(LOG_TAG, "onRetrySyncContact: contactProvider is not yet ready, retrycount = " + i2);
            CapabilityDiscoveryModule capabilityDiscoveryModule = this.mCapabilityDiscovery;
            capabilityDiscoveryModule.sendMessageDelayed(capabilityDiscoveryModule.obtainMessage(13), 30000);
        }
    }

    /* access modifiers changed from: package-private */
    public void onContactChanged(boolean z, int i, boolean z2, long j) {
        if (this.mCapabilityDiscovery.getUriGenerator() == null) {
            Log.i(LOG_TAG, "onContactChanged: mUriGenerator is null");
            return;
        }
        IMSLog.i(LOG_TAG, i, "onContactChanged: initial = " + z);
        processContactChanged(z, i, z2, j);
    }

    /* access modifiers changed from: package-private */
    public boolean setLegacyLatching(Context context, ImsUri imsUri, boolean z, int i) {
        StringBuilder sb = new StringBuilder();
        sb.append("setLegacyLatching: ");
        sb.append(imsUri != null ? imsUri.toStringLimit() : null);
        sb.append(" isLatching = ");
        sb.append(z);
        IMSLog.i(LOG_TAG, i, sb.toString());
        Capabilities capabilities = this.mCapabilityDiscovery.getCapabilitiesCache(i).get(imsUri);
        if (capabilities == null || capabilities.getLegacyLatching() == z) {
            return false;
        }
        IMSLog.i(LOG_TAG, i, "setLegacyLatching: Latching is changed to " + z);
        capabilities.setLegacyLatching(z);
        this.mCapabilityDiscovery.getCapabilitiesCache(i).persistCachedUri(imsUri, capabilities);
        ArrayList arrayList = new ArrayList();
        arrayList.add(imsUri);
        this.mCapabilityDiscovery.notifyCapabilitiesChanged(arrayList, capabilities, i);
        this.mCapabilityUtil.sendRCSLInfoToHQM(context, z, i);
        IMSLog.c(LogClass.CDM_SET_LATCHING, i + "," + z);
        return true;
    }
}
