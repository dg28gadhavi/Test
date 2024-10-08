package com.sec.internal.ims.servicemodules.quantumencryption;

import com.voltecrypt.service.SxRequestPeerProfileEntity;

/* compiled from: R8$$SyntheticClass */
public final /* synthetic */ class QuantumEncryptionServiceModule$$ExternalSyntheticLambda4 implements Runnable {
    public final /* synthetic */ QuantumEncryptionServiceModule f$0;
    public final /* synthetic */ SxRequestPeerProfileEntity f$1;

    public /* synthetic */ QuantumEncryptionServiceModule$$ExternalSyntheticLambda4(QuantumEncryptionServiceModule quantumEncryptionServiceModule, SxRequestPeerProfileEntity sxRequestPeerProfileEntity) {
        this.f$0 = quantumEncryptionServiceModule;
        this.f$1 = sxRequestPeerProfileEntity;
    }

    public final void run() {
        this.f$0.lambda$onRequestPeerProfileStatus$1(this.f$1);
    }
}
