package com.sec.internal.interfaces.ims.servicemodules.options;

import android.net.Uri;
import android.telephony.ims.stub.RcsCapabilityExchangeImplBase;
import com.sec.ims.options.Capabilities;
import com.sec.ims.options.CapabilityRefreshType;
import com.sec.ims.util.ImsUri;
import com.sec.internal.ims.servicemodules.options.CapabilitiesCache;
import com.sec.internal.ims.servicemodules.options.ContactCache;
import com.sec.internal.interfaces.ims.servicemodules.base.IServiceModule;
import java.util.Collection;
import java.util.List;
import java.util.Set;

public interface ICapabilityDiscoveryModule extends IServiceModule {
    void changeParalysed(boolean z, int i);

    boolean checkSenderCapability(ImsUri imsUri);

    void clearCapabilitiesCache(int i);

    void exchangeCapabilitiesForVSH(int i, boolean z);

    void exchangeCapabilitiesForVSHOnRegi(boolean z, int i);

    int getCapCacheExpiry(int i);

    int getCapInfoExpiry(int i);

    int getCapPollInterval(int i);

    Capabilities getCapabilities(ImsUri imsUri, long j, int i);

    Capabilities getCapabilities(ImsUri imsUri, CapabilityRefreshType capabilityRefreshType, int i);

    Capabilities getCapabilities(String str, long j, int i);

    Capabilities[] getCapabilities(List<ImsUri> list, CapabilityRefreshType capabilityRefreshType, long j, int i);

    Capabilities[] getCapabilitiesByContactId(String str, CapabilityRefreshType capabilityRefreshType, int i);

    CapabilitiesCache getCapabilitiesCache();

    CapabilitiesCache getCapabilitiesCache(int i);

    ImsUri getNetworkPreferredUri(ImsUri imsUri);

    Capabilities getOwnCapabilities();

    Capabilities getOwnCapabilitiesBase(int i);

    ContactCache getPhonebook();

    int getServiceAvailabilityInfoExpiry(int i);

    boolean hasVideoOwnCapability(int i);

    int isCapDiscEnabled(int i);

    boolean isConfigured(int i);

    void onDefaultSmsPackageChanged();

    void onPackageUpdated(String str);

    void publishCapabilities(String str, RcsCapabilityExchangeImplBase.PublishResponseCallback publishResponseCallback, int i);

    void removePublishedServiceList(int i);

    void sendOptionsCapabilityRequest(Uri uri, Set<String> set, RcsCapabilityExchangeImplBase.OptionsResponseCallback optionsResponseCallback, int i);

    boolean setLegacyLatching(ImsUri imsUri, boolean z, int i);

    void setPublishedServiceList(int i, Set<String> set);

    void subscribeForCapabilities(Collection<Uri> collection, RcsCapabilityExchangeImplBase.SubscribeResponseCallback subscribeResponseCallback, int i);

    void updateOwnCapabilities(int i);
}
