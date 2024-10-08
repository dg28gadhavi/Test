package com.sec.internal.ims.servicemodules.presence;

import com.sec.ims.options.Capabilities;
import com.sec.ims.presence.PresenceInfo;
import com.sec.ims.util.ImsUri;
import com.sec.internal.ims.util.UriGenerator;
import com.sec.internal.interfaces.ims.servicemodules.options.ICapabilityDiscoveryModule;
import com.sec.internal.log.IMSLog;
import java.util.List;
import java.util.Map;
import java.util.Set;

public class PresenceCacheController {
    private static final String LOG_TAG = "PresenceCacheController";
    private final PresenceModule mPresence;

    PresenceCacheController(PresenceModule presenceModule) {
        this.mPresence = presenceModule;
    }

    /* access modifiers changed from: package-private */
    public PresenceInfo getPresenceInfo(ImsUri imsUri, int i) {
        IMSLog.s(LOG_TAG, i, "getPresenceInfo: uri " + imsUri);
        return this.mPresence.getPresenceModuleInfo(i).mPresenceCache.get(imsUri);
    }

    /* access modifiers changed from: package-private */
    public PresenceInfo getPresenceInfoByContactId(String str, List<String> list, int i) {
        IMSLog.s(LOG_TAG, i, "getPresenceInfoByContactId: contactId " + str);
        if (list == null || list.isEmpty()) {
            return null;
        }
        return this.mPresence.getPresenceModuleInfo(i).mPresenceCache.get(ImsUri.parse("tel:" + list.get(0)));
    }

    /* access modifiers changed from: package-private */
    public void removePresenceCache(List<ImsUri> list, int i) {
        this.mPresence.getPresenceModuleInfo(i).mPresenceCache.remove(list);
        PresenceSubscriptionController.removeSubscription(list);
    }

    /* access modifiers changed from: package-private */
    public void clearPresenceInfo(int i) {
        IMSLog.s(LOG_TAG, i, "clearPresenceInfo");
        this.mPresence.getPresenceModuleInfo(i).mPresenceCache.clear();
    }

    /* access modifiers changed from: package-private */
    public void updatePresenceDatabase(List<ImsUri> list, PresenceInfo presenceInfo, ICapabilityDiscoveryModule iCapabilityDiscoveryModule, UriGenerator uriGenerator, int i) {
        Capabilities capabilities;
        String contactId;
        if (this.mPresence.checkModuleReady(i) && list != null && list.size() != 0) {
            IMSLog.s(LOG_TAG, i, "updatePresenceDatabase: uris " + list);
            PresenceInfo presenceInfo2 = presenceInfo;
            for (ImsUri next : list) {
                ImsUri normalize = uriGenerator.normalize(next);
                if (!presenceInfo.isFetchSuccess()) {
                    presenceInfo2 = getPresenceInfo(normalize, i);
                    if (presenceInfo2 == null) {
                        presenceInfo2 = new PresenceInfo(presenceInfo.getSubscriptionId(), i);
                    } else if (list.size() > 1) {
                    }
                }
                if (!(iCapabilityDiscoveryModule.getCapabilitiesCache(i) == null || (capabilities = iCapabilityDiscoveryModule.getCapabilitiesCache(i).get(normalize)) == null || (contactId = capabilities.getContactId()) == null)) {
                    presenceInfo2.setContactId(contactId);
                }
                PresenceInfo presenceInfo3 = this.mPresence.getPresenceModuleInfo(i).mPresenceCache.get(normalize);
                if (presenceInfo3 != null) {
                    presenceInfo2.setId(presenceInfo3.getId());
                }
                presenceInfo2.setTelUri(normalize.toString());
                presenceInfo2.setUri(next.toString());
                presenceInfo2.addService(presenceInfo.getServiceList());
                this.mPresence.getPresenceModuleInfo(i).mPresenceCache.update(normalize, presenceInfo2);
            }
        }
    }

    /* access modifiers changed from: package-private */
    public void loadPresenceStorage(Set<ImsUri> set, int i) {
        Map<ImsUri, PresenceInfo> map = this.mPresence.getPresenceModuleInfo(i).mPresenceCache.get(set);
        if (map == null || map.size() <= 0) {
            for (ImsUri imsUri : set) {
                PresenceInfo presenceInfo = new PresenceInfo(i);
                presenceInfo.setTelUri(imsUri.toString());
                this.mPresence.getPresenceModuleInfo(i).mPresenceCache.add(presenceInfo);
            }
            return;
        }
        IMSLog.i(LOG_TAG, i, "loadPresenceStorage: found " + map.size() + " presenceInfo from DB");
        for (ImsUri next : set) {
            PresenceInfo presenceInfo2 = map.get(next);
            if (presenceInfo2 == null) {
                presenceInfo2 = new PresenceInfo(i);
                presenceInfo2.setTelUri(next.toString());
            }
            this.mPresence.getPresenceModuleInfo(i).mPresenceCache.add(presenceInfo2);
        }
    }
}
