package com.sec.internal.interfaces.ims.servicemodules.options;

import com.sec.ims.options.Capabilities;
import com.sec.ims.util.ImsUri;
import com.sec.internal.constants.ims.servicemodules.options.CapabilityConstants;
import java.util.List;
import java.util.Set;

public interface ICapabilityExchangeControl {

    public interface ICapabilityExchangeCallback {
        void onComplete(Capabilities capabilities);
    }

    void deRegisterService(List<String> list, int i);

    boolean isReadyToRequest(int i);

    void readConfig(int i);

    void registerService(String str, String str2, int i);

    int requestCapabilityExchange(Set<ImsUri> set, CapabilityConstants.RequestType requestType, int i, int i2);

    boolean requestCapabilityExchange(ImsUri imsUri, ICapabilityExchangeCallback iCapabilityExchangeCallback, CapabilityConstants.RequestType requestType, boolean z, long j, int i, String str, int i2);

    void reset(int i);

    boolean sendOptionsRequest(ImsUri imsUri, boolean z, Set<String> set, int i);
}
