package com.sec.internal.interfaces.ims.core;

import android.net.Network;
import java.util.List;

public interface PdnEventListener {
    void onConnected(int i, Network network) {
    }

    void onDisconnected(int i) {
    }

    void onLocalIpChanged(int i, Network network, boolean z) {
    }

    void onNetworkRequestFail() {
    }

    void onPcscfAddressChanged(int i, Network network, List<String> list) {
    }

    void onPcscfRestorationNotified(int i, List<String> list) {
    }

    void onResumed(int i) {
    }

    void onResumedBySnapshot(int i) {
    }

    void onSuspended(int i) {
    }

    void onSuspendedBySnapshot(int i) {
    }
}
