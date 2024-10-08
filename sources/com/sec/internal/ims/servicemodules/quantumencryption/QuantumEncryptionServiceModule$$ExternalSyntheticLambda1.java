package com.sec.internal.ims.servicemodules.quantumencryption;

import com.voltecrypt.service.SxRequestQMKeyEntity;

/* compiled from: R8$$SyntheticClass */
public final /* synthetic */ class QuantumEncryptionServiceModule$$ExternalSyntheticLambda1 implements Runnable {
    public final /* synthetic */ QuantumEncryptionServiceModule f$0;
    public final /* synthetic */ SxRequestQMKeyEntity f$1;

    public /* synthetic */ QuantumEncryptionServiceModule$$ExternalSyntheticLambda1(QuantumEncryptionServiceModule quantumEncryptionServiceModule, SxRequestQMKeyEntity sxRequestQMKeyEntity) {
        this.f$0 = quantumEncryptionServiceModule;
        this.f$1 = sxRequestQMKeyEntity;
    }

    public final void run() {
        this.f$0.lambda$onRequestQMKey$2(this.f$1);
    }
}
