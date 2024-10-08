package com.sec.internal.ims.servicemodules.quantumencryption;

import com.voltecrypt.service.SxHangUpEntity;

/* compiled from: R8$$SyntheticClass */
public final /* synthetic */ class QuantumEncryptionServiceModule$$ExternalSyntheticLambda0 implements Runnable {
    public final /* synthetic */ QuantumEncryptionServiceModule f$0;
    public final /* synthetic */ SxHangUpEntity f$1;

    public /* synthetic */ QuantumEncryptionServiceModule$$ExternalSyntheticLambda0(QuantumEncryptionServiceModule quantumEncryptionServiceModule, SxHangUpEntity sxHangUpEntity) {
        this.f$0 = quantumEncryptionServiceModule;
        this.f$1 = sxHangUpEntity;
    }

    public final void run() {
        this.f$0.lambda$onHangUp$6(this.f$1);
    }
}
