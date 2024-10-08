package com.sec.internal.ims.servicemodules.options;

import android.telephony.ims.ImsException;
import android.telephony.ims.stub.RcsCapabilityExchangeImplBase;
import android.util.Log;
import com.sec.ims.ImsRegistration;
import com.sec.ims.options.Capabilities;
import com.sec.ims.options.CapabilityRefreshType;
import com.sec.ims.settings.ImsProfile;
import com.sec.ims.util.ImsUri;
import com.sec.internal.constants.Mno;
import com.sec.internal.constants.ims.settings.GlobalSettingsConstants;
import com.sec.internal.helper.SimUtil;
import com.sec.internal.ims.rcs.RcsPolicyManager;
import com.sec.internal.ims.rcs.util.RcsUtils;
import com.sec.internal.ims.registry.ImsRegistry;
import com.sec.internal.ims.servicemodules.im.strategy.IMnoStrategy;
import com.sec.internal.ims.servicemodules.presence.PresenceUtil;
import com.sec.internal.ims.settings.RcsPolicySettings;
import com.sec.internal.imscr.LogClass;
import com.sec.internal.interfaces.ims.core.IRegistrationManager;
import com.sec.internal.log.IMSLog;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;

public class CapabilityQuery {
    private static final String LOG_TAG = "CapabilityQuery";
    private static final long SHORT_NUMBER_DELAY = 2000;
    private static final int SHORT_NUMBER_LENGTH = 8;
    private CapabilityDiscoveryModule mCapabilityDiscovery;
    private CapabilityExchange mCapabilityExchange;
    private CapabilityUtil mCapabilityUtil;

    CapabilityQuery(CapabilityDiscoveryModule capabilityDiscoveryModule, CapabilityUtil capabilityUtil, CapabilityExchange capabilityExchange) {
        this.mCapabilityDiscovery = capabilityDiscoveryModule;
        this.mCapabilityUtil = capabilityUtil;
        this.mCapabilityExchange = capabilityExchange;
    }

    /* access modifiers changed from: package-private */
    public Capabilities getCapabilities(int i, int i2) {
        IMSLog.s(LOG_TAG, i2, "getCapabilities: Capex list id " + i);
        if (!this.mCapabilityUtil.checkModuleReady(i2)) {
            return null;
        }
        Capabilities capabilities = this.mCapabilityDiscovery.getCapabilitiesCache(i2).get(i);
        if (capabilities == null || !capabilities.isExpired((long) this.mCapabilityUtil.getCapInfoExpiry(capabilities, i2))) {
            IMSLog.i(LOG_TAG, i2, "getCapabilities: No need to refresh. capex [" + ((String) Optional.ofNullable(capabilities).map(new CapabilityQuery$$ExternalSyntheticLambda0()).orElse("null")) + "]");
        } else {
            IMSLog.i(LOG_TAG, i2, "getCapabilities: " + capabilities.getUri().toStringLimit() + " is expired. refresh it.");
            CapabilityDiscoveryModule capabilityDiscoveryModule = this.mCapabilityDiscovery;
            capabilityDiscoveryModule.sendMessage(capabilityDiscoveryModule.obtainMessage(6, 0, i2, capabilities.getUri()));
        }
        return capabilities;
    }

    /* access modifiers changed from: package-private */
    public Capabilities getCapabilities(String str, CapabilityRefreshType capabilityRefreshType, boolean z, int i, String str2) {
        Capabilities capabilities;
        long j;
        String str3 = str;
        CapabilityRefreshType capabilityRefreshType2 = capabilityRefreshType;
        boolean z2 = z;
        int i2 = i;
        IMSLog.i(LOG_TAG, i2, "getCapabilities: refreshType " + capabilityRefreshType2 + ", lazyQuery: " + z2);
        StringBuilder sb = new StringBuilder();
        sb.append("getCapabilities: number ");
        sb.append(str3);
        IMSLog.s(LOG_TAG, i2, sb.toString());
        this.mCapabilityDiscovery.removeMessages(8);
        if (!this.mCapabilityUtil.checkModuleReady(i2)) {
            return null;
        }
        if (!RcsPolicyManager.getRcsStrategy(i).checkCapDiscoveryOption()) {
            Capabilities capabilities2 = new Capabilities();
            capabilities2.addFeature((long) (Capabilities.FEATURE_MMTEL_VIDEO | Capabilities.FEATURE_PRESENCE_DISCOVERY));
            capabilities2.setAvailiable(true);
            return capabilities2;
        }
        String checkNeedParsing = RcsPolicyManager.getRcsStrategy(i).checkNeedParsing(str3);
        ImsUri normalizedUri = this.mCapabilityDiscovery.getUriGenerator().getNormalizedUri(checkNeedParsing, true);
        if (normalizedUri == null) {
            Log.i(LOG_TAG, "getCapabilities: uri is null");
            return null;
        } else if (this.mCapabilityUtil.blockOptionsToOwnUri(normalizedUri, i2)) {
            return null;
        } else {
            if (capabilityRefreshType2 != CapabilityRefreshType.DISABLED) {
                IMSLog.c(LogClass.CDM_GET_CAPA, i2 + ",GETCAPA," + capabilityRefreshType.ordinal() + "," + z2 + "," + normalizedUri.toStringLimit());
            }
            if (ImsProfile.isRcsUpProfile(str2) && this.mCapabilityDiscovery.getCapabilityConfig(i2).getDefaultDisc() == 2) {
                return copyToOwnCapabilities(normalizedUri, checkNeedParsing);
            }
            Capabilities capabilities3 = this.mCapabilityDiscovery.getCapabilitiesCache(i2).get(normalizedUri);
            if (isNeedToRefreshInMsgCtxForResolvingLatching(capabilityRefreshType2, i2, normalizedUri)) {
                capabilityRefreshType2 = CapabilityRefreshType.ALWAYS_FORCE_REFRESH;
                Log.d(LOG_TAG, "refreshType changes to ALWAYS_FORCE_REFRESH");
                j = 0;
                capabilities = null;
            } else {
                capabilities = capabilities3;
                j = -1;
            }
            needCapabilityRefresh(capabilities, capabilityRefreshType2, normalizedUri, j, checkNeedParsing.length() <= 8, z, i);
            return capabilities;
        }
    }

    /* access modifiers changed from: package-private */
    public Capabilities getCapabilities(String str, long j, int i, String str2) {
        IMSLog.i(LOG_TAG, i, "getCapabilities: feature " + Capabilities.dumpFeature(j));
        IMSLog.s(LOG_TAG, i, "getCapabilities: number " + str);
        if (!this.mCapabilityUtil.checkModuleReady(i)) {
            return null;
        }
        ImsUri normalizedUri = this.mCapabilityDiscovery.getUriGenerator().getNormalizedUri(str, true);
        if (normalizedUri == null) {
            Log.i(LOG_TAG, "getCapabilities: uri is null");
            return null;
        } else if (this.mCapabilityUtil.blockOptionsToOwnUri(normalizedUri, i)) {
            return null;
        } else {
            if (ImsProfile.isRcsUpProfile(str2) && this.mCapabilityDiscovery.getCapabilityConfig(i).getDefaultDisc() == 2) {
                return copyToOwnCapabilities(normalizedUri, str);
            }
            Capabilities capabilities = this.mCapabilityDiscovery.getCapabilitiesCache(i).get(normalizedUri);
            needCapabilityRefresh(capabilities, CapabilityRefreshType.ONLY_IF_NOT_FRESH, normalizedUri, j, false, false, i);
            return capabilities;
        }
    }

    /* access modifiers changed from: package-private */
    public Capabilities getCapabilities(ImsUri imsUri, long j, int i, String str) {
        IMSLog.i(LOG_TAG, i, "getCapabilities: feature " + Capabilities.dumpFeature(j));
        IMSLog.s(LOG_TAG, i, "getCapabilities: uri " + imsUri);
        if (!this.mCapabilityUtil.checkModuleReady(i) || imsUri == null) {
            Log.i(LOG_TAG, "getCapabilities: failed");
            return null;
        }
        ImsUri normalize = imsUri.getUriType() == ImsUri.UriType.SIP_URI ? this.mCapabilityDiscovery.getUriGenerator().normalize(imsUri) : imsUri;
        if (this.mCapabilityUtil.blockOptionsToOwnUri(normalize, i)) {
            return null;
        }
        if (ImsProfile.isRcsUpProfile(str) && this.mCapabilityDiscovery.getCapabilityConfig(i).getDefaultDisc() == 2) {
            return copyToOwnCapabilities(normalize, normalize.getMsisdn());
        }
        Capabilities capabilities = this.mCapabilityDiscovery.getCapabilitiesCache(i).get(normalize);
        needCapabilityRefresh(capabilities, CapabilityRefreshType.ONLY_IF_NOT_FRESH, normalize, j, false, false, i);
        return capabilities;
    }

    /* access modifiers changed from: package-private */
    public Capabilities[] getCapabilities(List<ImsUri> list, CapabilityRefreshType capabilityRefreshType, long j, int i, String str, RcsCapabilityExchangeImplBase.SubscribeResponseCallback subscribeResponseCallback) {
        int i2;
        Capabilities[] capabilitiesArr;
        ArrayList arrayList;
        IMnoStrategy iMnoStrategy;
        HashSet hashSet;
        RcsCapabilityExchangeImplBase.SubscribeResponseCallback subscribeResponseCallback2;
        String str2;
        ImsUri imsUri;
        String str3;
        ImsUri imsUri2;
        List<ImsUri> list2 = list;
        int i3 = i;
        RcsCapabilityExchangeImplBase.SubscribeResponseCallback subscribeResponseCallback3 = subscribeResponseCallback;
        String str4 = LOG_TAG;
        IMSLog.i(str4, i3, "getCapabilities: refreshType " + capabilityRefreshType + ", feature " + Capabilities.dumpFeature(j) + ", callback : " + subscribeResponseCallback3);
        Capabilities[] capabilitiesArr2 = null;
        if (list2 == null) {
            Log.i(str4, "getCapabilities: uris is null.");
            if (subscribeResponseCallback3 != null) {
                try {
                    subscribeResponseCallback3.onCommandError(2);
                } catch (ImsException e) {
                    e.printStackTrace();
                }
            }
            return null;
        }
        IMSLog.s(str4, i3, "getCapabilities: uris " + list.toString());
        while (list2.contains((Object) null)) {
            IMSLog.i(str4, i3, "remove invalid list elements(null)" + list.toString());
            list2.remove((Object) null);
        }
        IMSLog.s(str4, i3, "getCapabilities: uris " + list.toString());
        if (!this.mCapabilityUtil.checkModuleReady(i3)) {
            if (subscribeResponseCallback3 != null) {
                try {
                    subscribeResponseCallback3.onCommandError(9);
                } catch (ImsException e2) {
                    e2.printStackTrace();
                }
            }
            return null;
        }
        HashSet hashSet2 = new HashSet();
        ArrayList arrayList2 = new ArrayList();
        CapabilityConfig capabilityConfig = this.mCapabilityDiscovery.getCapabilityConfig(i3);
        if (!ImsProfile.isRcsUpProfile(str) || capabilityConfig.getDefaultDisc() != 2) {
            IMnoStrategy rcsStrategy = RcsPolicyManager.getRcsStrategy(i);
            for (ImsUri next : list) {
                if (!this.mCapabilityUtil.blockOptionsToOwnUri(next, i3)) {
                    Capabilities capabilities = this.mCapabilityDiscovery.getCapabilitiesCache(i3).get(next);
                    if (capabilities != null) {
                        arrayList2.add(capabilities);
                    }
                    if (capabilities != null) {
                        String str5 = str4;
                        if (capabilities.isFeatureAvailable(j)) {
                            arrayList = arrayList2;
                            Capabilities capabilities2 = capabilities;
                            imsUri = next;
                            str3 = "subscribeForCapabilities internalRequestId : ";
                            iMnoStrategy = rcsStrategy;
                            capabilitiesArr = capabilitiesArr2;
                            hashSet = hashSet2;
                            if (!rcsStrategy.needRefresh(capabilities, capabilityRefreshType, (long) this.mCapabilityUtil.getCapInfoExpiry(capabilities, i3), (long) capabilityConfig.getServiceAvailabilityInfoExpiry(), capabilityConfig.getCapCacheExpiry(), capabilityConfig.getMsgcapvalidity())) {
                                str2 = str5;
                                IMSLog.i(str2, i3, "getCapabilities: No need to refresh. " + capabilities2.toString());
                                subscribeResponseCallback2 = subscribeResponseCallback;
                                str4 = str2;
                                hashSet2 = hashSet;
                                rcsStrategy = iMnoStrategy;
                                arrayList2 = arrayList;
                                capabilitiesArr2 = capabilitiesArr;
                                CapabilityRefreshType capabilityRefreshType2 = capabilityRefreshType;
                                subscribeResponseCallback3 = subscribeResponseCallback2;
                            }
                        } else {
                            imsUri = next;
                            str3 = "subscribeForCapabilities internalRequestId : ";
                            iMnoStrategy = rcsStrategy;
                            arrayList = arrayList2;
                            capabilitiesArr = capabilitiesArr2;
                            hashSet = hashSet2;
                        }
                        str2 = str5;
                    } else {
                        imsUri = next;
                        iMnoStrategy = rcsStrategy;
                        arrayList = arrayList2;
                        capabilitiesArr = capabilitiesArr2;
                        hashSet = hashSet2;
                        str2 = str4;
                        str3 = "subscribeForCapabilities internalRequestId : ";
                    }
                    IMSLog.i(str2, i3, "getCapabilities: " + imsUri.toStringLimit() + " is expired. refresh it");
                    if (!iMnoStrategy.boolSetting(RcsPolicySettings.RcsPolicy.ALLOW_LIST_CAPEX)) {
                        imsUri2 = imsUri;
                        subscribeResponseCallback2 = subscribeResponseCallback;
                    } else if (SimUtil.getSimMno(i) != Mno.TMOUS || !capabilityConfig.isDisableInitialScan()) {
                        hashSet.add(imsUri);
                        subscribeResponseCallback2 = subscribeResponseCallback;
                        str4 = str2;
                        hashSet2 = hashSet;
                        rcsStrategy = iMnoStrategy;
                        arrayList2 = arrayList;
                        capabilitiesArr2 = capabilitiesArr;
                        CapabilityRefreshType capabilityRefreshType22 = capabilityRefreshType;
                        subscribeResponseCallback3 = subscribeResponseCallback2;
                    } else {
                        subscribeResponseCallback2 = subscribeResponseCallback;
                        imsUri2 = imsUri;
                    }
                    if (subscribeResponseCallback2 == null) {
                        CapabilityDiscoveryModule capabilityDiscoveryModule = this.mCapabilityDiscovery;
                        capabilityDiscoveryModule.sendMessage(capabilityDiscoveryModule.obtainMessage(6, capabilityRefreshType.ordinal(), i3, imsUri2));
                    } else {
                        int addSubscribeResponseCallback = PresenceUtil.addSubscribeResponseCallback(i3, subscribeResponseCallback2);
                        IMSLog.i(str2, i3, str3 + addSubscribeResponseCallback);
                        CapabilityDiscoveryModule capabilityDiscoveryModule2 = this.mCapabilityDiscovery;
                        capabilityDiscoveryModule2.sendMessage(capabilityDiscoveryModule2.obtainMessage(54, addSubscribeResponseCallback, i3, imsUri2));
                    }
                    str4 = str2;
                    hashSet2 = hashSet;
                    rcsStrategy = iMnoStrategy;
                    arrayList2 = arrayList;
                    capabilitiesArr2 = capabilitiesArr;
                    CapabilityRefreshType capabilityRefreshType222 = capabilityRefreshType;
                    subscribeResponseCallback3 = subscribeResponseCallback2;
                }
            }
            ArrayList arrayList3 = arrayList2;
            Capabilities[] capabilitiesArr3 = capabilitiesArr2;
            HashSet hashSet3 = hashSet2;
            RcsCapabilityExchangeImplBase.SubscribeResponseCallback subscribeResponseCallback4 = subscribeResponseCallback3;
            String str6 = str4;
            String str7 = "subscribeForCapabilities internalRequestId : ";
            IMnoStrategy iMnoStrategy2 = rcsStrategy;
            if (hashSet3.size() > 1) {
                if (subscribeResponseCallback4 != null) {
                    i2 = PresenceUtil.addSubscribeResponseCallback(i3, subscribeResponseCallback4);
                    IMSLog.i(str6, i3, str7 + i2);
                } else {
                    i2 = 0;
                }
                CapabilityDiscoveryModule capabilityDiscoveryModule3 = this.mCapabilityDiscovery;
                capabilityDiscoveryModule3.sendMessage(capabilityDiscoveryModule3.obtainMessage(33, i3, i2, hashSet3));
            } else if (hashSet3.size() == 1) {
                if (subscribeResponseCallback4 == null) {
                    CapabilityDiscoveryModule capabilityDiscoveryModule4 = this.mCapabilityDiscovery;
                    capabilityDiscoveryModule4.sendMessage(capabilityDiscoveryModule4.obtainMessage(6, capabilityRefreshType.ordinal(), i3, hashSet3.iterator().next()));
                } else {
                    int addSubscribeResponseCallback2 = PresenceUtil.addSubscribeResponseCallback(i3, subscribeResponseCallback4);
                    IMSLog.i(str6, i3, str7 + addSubscribeResponseCallback2);
                    CapabilityDiscoveryModule capabilityDiscoveryModule5 = this.mCapabilityDiscovery;
                    capabilityDiscoveryModule5.sendMessage(capabilityDiscoveryModule5.obtainMessage(54, addSubscribeResponseCallback2, i3, hashSet3.iterator().next()));
                }
            }
            if (arrayList3.size() == 0 || (iMnoStrategy2.boolSetting(RcsPolicySettings.RcsPolicy.CAPA_SKIP_NOTIFY_FORCE_REFRESH_SYNC) && capabilityRefreshType == CapabilityRefreshType.FORCE_REFRESH_SYNC)) {
                return capabilitiesArr3;
            }
            return (Capabilities[]) arrayList3.toArray(new Capabilities[0]);
        }
        for (ImsUri next2 : list) {
            arrayList2.add(copyToOwnCapabilities(next2, next2.getMsisdn()));
        }
        return (Capabilities[]) arrayList2.toArray(new Capabilities[0]);
    }

    /* access modifiers changed from: package-private */
    public Capabilities getCapabilities(ImsUri imsUri, CapabilityRefreshType capabilityRefreshType, int i, String str) {
        IMSLog.i(LOG_TAG, i, "getCapabilities: refreshType " + capabilityRefreshType);
        IMSLog.s(LOG_TAG, i, "getCapabilities: uri " + imsUri);
        if (!this.mCapabilityUtil.checkModuleReady(i) || imsUri == null) {
            Log.i(LOG_TAG, "getCapabilities: failed");
            return null;
        }
        ImsUri normalize = imsUri.getUriType() == ImsUri.UriType.SIP_URI ? this.mCapabilityDiscovery.getUriGenerator().normalize(imsUri) : imsUri;
        if (normalize == null || this.mCapabilityUtil.blockOptionsToOwnUri(normalize, i)) {
            return null;
        }
        if (ImsProfile.isRcsUpProfile(str) && this.mCapabilityDiscovery.getCapabilityConfig(i).getDefaultDisc() == 2) {
            return copyToOwnCapabilities(normalize, normalize.getMsisdn());
        }
        Capabilities capabilities = this.mCapabilityDiscovery.getCapabilitiesCache(i).get(normalize);
        needCapabilityRefresh(capabilities, capabilityRefreshType, normalize, -1, false, false, i);
        return capabilities;
    }

    /* access modifiers changed from: package-private */
    public Capabilities[] getCapabilitiesByContactId(String str, CapabilityRefreshType capabilityRefreshType, int i, String str2) {
        IMSLog.i(LOG_TAG, i, "getCapabilitiesByContactId: contactId " + str + ", refreshType " + capabilityRefreshType);
        if (!this.mCapabilityUtil.checkModuleReady(i)) {
            return null;
        }
        if ("FORCE_CAPA_POLLING".equals(str)) {
            this.mCapabilityExchange.forcePoll(i);
            return null;
        }
        ArrayList arrayList = new ArrayList();
        List<String> numberlistByContactId = this.mCapabilityDiscovery.getPhonebook().getNumberlistByContactId(str);
        if (numberlistByContactId != null) {
            for (String str3 : numberlistByContactId) {
                ImsUri parse = ImsUri.parse("tel:" + str3);
                IMSLog.s(LOG_TAG, i, "getCapabilitiesByContactId: contactId " + str + ", teluri " + parse);
                arrayList.add(parse);
            }
        }
        return getCapabilities(arrayList, capabilityRefreshType, (long) Capabilities.FEATURE_OFFLINE_RCS_USER, i, str2, (RcsCapabilityExchangeImplBase.SubscribeResponseCallback) null);
    }

    /* access modifiers changed from: package-private */
    public Capabilities copyToOwnCapabilities(ImsUri imsUri, String str) {
        IMSLog.s(LOG_TAG, "copyToOwnCapabilities: CAPABILITY DISCOVERY MECHANISM is off. Copy to OwnCapabilities");
        Capabilities ownCapabilities = this.mCapabilityDiscovery.getOwnCapabilities();
        if (ownCapabilities != null) {
            long feature = ownCapabilities.getFeature();
            ownCapabilities.setUri(imsUri);
            ownCapabilities.setAvailableFeatures(feature);
            ownCapabilities.setNumber(str);
        }
        return ownCapabilities;
    }

    /* access modifiers changed from: package-private */
    public void needCapabilityRefresh(Capabilities capabilities, CapabilityRefreshType capabilityRefreshType, ImsUri imsUri, long j, boolean z, boolean z2, int i) {
        Capabilities capabilities2;
        CapabilityRefreshType capabilityRefreshType2 = capabilityRefreshType;
        ImsUri imsUri2 = imsUri;
        int i2 = i;
        long j2 = j;
        if (RcsPolicyManager.getRcsStrategy(i).needRefresh(capabilities, capabilityRefreshType, (long) this.mCapabilityUtil.getCapInfoExpiry(capabilities, i2), (long) this.mCapabilityDiscovery.getCapabilityConfig(i2).getServiceAvailabilityInfoExpiry(), this.mCapabilityDiscovery.getCapabilityConfig(i2).getCapCacheExpiry(), this.mCapabilityDiscovery.getCapabilityConfig(i2).getMsgcapvalidity())) {
            IMSLog.i(LOG_TAG, i2, "needCapabilityRefresh: true, missing capabilities for " + imsUri.toStringLimit());
            if (!z2) {
                CapabilityDiscoveryModule capabilityDiscoveryModule = this.mCapabilityDiscovery;
                capabilityDiscoveryModule.sendMessage(capabilityDiscoveryModule.obtainMessage(6, capabilityRefreshType.ordinal(), i2, imsUri2));
            } else if (z) {
                CapabilityDiscoveryModule capabilityDiscoveryModule2 = this.mCapabilityDiscovery;
                capabilityDiscoveryModule2.sendMessageDelayed(capabilityDiscoveryModule2.obtainMessage(8, capabilityRefreshType.ordinal(), i2, imsUri2), 2000);
            } else {
                CapabilityDiscoveryModule capabilityDiscoveryModule3 = this.mCapabilityDiscovery;
                capabilityDiscoveryModule3.sendMessage(capabilityDiscoveryModule3.obtainMessage(8, capabilityRefreshType.ordinal(), i2, imsUri2));
            }
        } else {
            if (j2 >= 0) {
                capabilities2 = capabilities;
                long j3 = j2;
                if (capabilities2 == null || !capabilities.isAvailable() || !capabilities2.isFeatureAvailable(j3)) {
                    IMSLog.i(LOG_TAG, i2, "needCapabilityRefresh: true, missing features for " + imsUri.toStringLimit());
                    CapabilityDiscoveryModule capabilityDiscoveryModule4 = this.mCapabilityDiscovery;
                    capabilityDiscoveryModule4.sendMessage(capabilityDiscoveryModule4.obtainMessage(6, capabilityRefreshType.ordinal(), i2, imsUri2));
                    return;
                }
            } else {
                capabilities2 = capabilities;
            }
            if (capabilities2 != null) {
                IMSLog.i(LOG_TAG, i2, "needCapabilityRefresh: false, capex is " + capabilities.toString());
                if (capabilityRefreshType != CapabilityRefreshType.DISABLED) {
                    IMSLog.c(LogClass.CDM_NO_REFRESH, i2 + ",NOREF," + capabilities.getFeature() + "," + capabilities.getAvailableFeatures());
                    return;
                }
                return;
            }
            CapabilityRefreshType capabilityRefreshType3 = capabilityRefreshType;
            IMSLog.i(LOG_TAG, i2, "needCapabilityRefresh: false, capex is null for " + imsUri.toStringLimit());
            if (capabilityRefreshType3 != CapabilityRefreshType.DISABLED) {
                IMSLog.c(LogClass.CDM_NO_REFRESH, i2 + ",NOREF,NOCAP");
            }
        }
    }

    /* access modifiers changed from: package-private */
    public Capabilities getOwnCapabilitiesBase(int i, Capabilities capabilities) {
        IMSLog.i(LOG_TAG, i, "getOwnCapabilitiesBase:");
        Capabilities capabilities2 = null;
        if (!this.mCapabilityUtil.checkModuleReady(i)) {
            try {
                capabilities2 = capabilities.clone();
            } catch (CloneNotSupportedException e) {
                e.printStackTrace();
            }
            if (capabilities2 != null) {
                IMSLog.i(LOG_TAG, i, "getOwnCapabilitiesBase: module is not ready, " + Capabilities.dumpFeature(capabilities2.getFeature()));
            }
            return capabilities2;
        }
        this.mCapabilityDiscovery.updateOwnCapabilities(i);
        try {
            capabilities2 = capabilities.clone();
        } catch (CloneNotSupportedException e2) {
            e2.printStackTrace();
        }
        if (capabilities2 != null) {
            IMSLog.i(LOG_TAG, i, "getOwnCapabilitiesBase: " + Capabilities.dumpFeature(capabilities2.getFeature()));
        }
        return capabilities2;
    }

    /* access modifiers changed from: package-private */
    public Capabilities getOwnCapabilities(int i, int i2, Map<Integer, ImsRegistration> map, IRegistrationManager iRegistrationManager, int i3, boolean z, String str, Capabilities capabilities) {
        Capabilities capabilities2;
        int i4 = i;
        if (!this.mCapabilityUtil.checkModuleReady(i)) {
            return null;
        }
        if (RcsUtils.DualRcs.isDualRcsReg() || i2 == i4) {
            this.mCapabilityDiscovery.updateOwnCapabilities(i);
            Capabilities capabilities3 = new Capabilities();
            try {
                capabilities2 = capabilities.clone();
                try {
                    Map<Integer, ImsRegistration> map2 = map;
                    IRegistrationManager iRegistrationManager2 = iRegistrationManager;
                    int i5 = i3;
                    Set<String> filterServicesWithReg = this.mCapabilityUtil.filterServicesWithReg(map, iRegistrationManager, i3, i);
                    if (filterServicesWithReg != null) {
                        long filterFeaturesWithCallState = this.mCapabilityUtil.filterFeaturesWithCallState(this.mCapabilityUtil.filterFeaturesWithService(capabilities2.getFeature(), filterServicesWithReg, i3, i), z, str);
                        capabilities2.setFeatures(filterFeaturesWithCallState);
                        capabilities2.setAvailableFeatures(filterFeaturesWithCallState);
                    }
                } catch (CloneNotSupportedException e) {
                    e = e;
                    capabilities3 = capabilities2;
                    e.printStackTrace();
                    capabilities2 = capabilities3;
                    IMSLog.i(LOG_TAG, i, "getOwnCapabilities: feature=" + Long.toHexString(capabilities2.getFeature()) + ", detail=" + Capabilities.dumpFeature(capabilities2.getFeature()));
                    return capabilities2;
                }
            } catch (CloneNotSupportedException e2) {
                e = e2;
                e.printStackTrace();
                capabilities2 = capabilities3;
                IMSLog.i(LOG_TAG, i, "getOwnCapabilities: feature=" + Long.toHexString(capabilities2.getFeature()) + ", detail=" + Capabilities.dumpFeature(capabilities2.getFeature()));
                return capabilities2;
            }
            IMSLog.i(LOG_TAG, i, "getOwnCapabilities: feature=" + Long.toHexString(capabilities2.getFeature()) + ", detail=" + Capabilities.dumpFeature(capabilities2.getFeature()));
            return capabilities2;
        }
        IMSLog.s(LOG_TAG, i, "getOwnCapabilities: mAvailablePhoneId = ! phoneId");
        return null;
    }

    /* access modifiers changed from: package-private */
    public Capabilities[] getAllCapabilities(int i) {
        IMSLog.s(LOG_TAG, i, "getAllCapabilities:");
        if (this.mCapabilityDiscovery.isRunning()) {
            return (Capabilities[]) this.mCapabilityDiscovery.getCapabilitiesCache(i).getAllCapabilities().toArray(new Capabilities[0]);
        }
        Log.i(LOG_TAG, "getAllCapabilities: CapabilityDiscoveryModule is disabled");
        return null;
    }

    private boolean isNeedToRefreshInMsgCtxForResolvingLatching(CapabilityRefreshType capabilityRefreshType, int i, ImsUri imsUri) {
        if (capabilityRefreshType != CapabilityRefreshType.ONLY_IF_NOT_FRESH_IN_MSG_CTX || !ImsRegistry.getBoolean(i, GlobalSettingsConstants.RCS.USE_XMS_RECEIVER_FOR_RESOLVING_LATCHING, false) || !this.mCapabilityDiscovery.getImModule().getLatchingProcessor().isExistInLatchingList(imsUri, i) || !this.mCapabilityDiscovery.getImModule().getLatchingProcessor().checkTimestampInOptionsList(imsUri, System.currentTimeMillis(), i)) {
            return false;
        }
        return true;
    }
}
