package com.sec.internal.ims.servicemodules.tapi.service.extension.utils;

public class SignatureInfo {
    private final CertificateInfo entityCert;
    private final CertificateInfo rootCert;

    public SignatureInfo(CertificateInfo certificateInfo, CertificateInfo certificateInfo2) {
        this.rootCert = certificateInfo;
        this.entityCert = certificateInfo2;
    }

    public CertificateInfo getEntityCertificate() {
        return this.entityCert;
    }

    public String toString() {
        return "\n" + this.entityCert.toString() + this.rootCert.toString();
    }
}
