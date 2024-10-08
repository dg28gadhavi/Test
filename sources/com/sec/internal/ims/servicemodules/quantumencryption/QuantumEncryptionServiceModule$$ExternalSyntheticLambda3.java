package com.sec.internal.ims.servicemodules.quantumencryption;

import com.voltecrypt.service.SxRequestAuthenticationEntity;

/* compiled from: R8$$SyntheticClass */
public final /* synthetic */ class QuantumEncryptionServiceModule$$ExternalSyntheticLambda3 implements Runnable {
    public final /* synthetic */ QuantumEncryptionServiceModule f$0;
    public final /* synthetic */ SxRequestAuthenticationEntity f$1;

    public /* synthetic */ QuantumEncryptionServiceModule$$ExternalSyntheticLambda3(QuantumEncryptionServiceModule quantumEncryptionServiceModule, SxRequestAuthenticationEntity sxRequestAuthenticationEntity) {
        this.f$0 = quantumEncryptionServiceModule;
        this.f$1 = sxRequestAuthenticationEntity;
    }

    public final void run() {
        this.f$0.lambda$onRequestAuthentication$0(this.f$1);
    }
}
