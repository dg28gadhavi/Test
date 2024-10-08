package com.sec.internal.constants.ims.core;

import android.net.LinkProperties;
import android.net.Network;
import com.sec.internal.interfaces.ims.core.PdnEventListener;

public class LinkPropertiesChangedEvent {
    private final LinkProperties mLinkProperties;
    private final PdnEventListener mListener;
    private final Network mNetwork;

    public LinkPropertiesChangedEvent(Network network, PdnEventListener pdnEventListener, LinkProperties linkProperties) {
        this.mNetwork = network;
        this.mListener = pdnEventListener;
        this.mLinkProperties = linkProperties;
    }

    public Network getNetwork() {
        return this.mNetwork;
    }

    public PdnEventListener getListener() {
        return this.mListener;
    }

    public LinkProperties getLinkProperties() {
        return this.mLinkProperties;
    }
}
